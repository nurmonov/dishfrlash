package com.dishflash.service;

import com.dishflash.crypto.CryptoService;
import com.dishflash.dto.request.OrderRequest;
import com.dishflash.dto.response.OrderResponse;
import com.dishflash.entity.*;
import com.dishflash.enums.OrderStatus;
import com.dishflash.enums.PaymentStatus;
import com.dishflash.exception.ResourceNotFoundException;
import com.dishflash.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final String CONTEXT_DELIVERY_ADDRESS = "orders:delivery_address";
    private static final String CONTEXT_RECIPIENT_PHONE = "orders:recipient_phone";
    private static final String CONTEXT_CARD_NUMBER = "payments:card_number";
    private static final String CONTEXT_CARD_HOLDER = "payments:card_holder";
    private static final String CONTEXT_USER_FULL_NAME = "users:full_name";
    private static final String CONTEXT_ORDER_STATUS = "orders:status";
    private static final String CONTEXT_ORDER_TOTAL = "orders:total_price";
    private static final String CONTEXT_ORDER_CREATED_AT = "orders:created_at";
    private static final String CONTEXT_ITEM_QUANTITY = "order_items:quantity";
    private static final String CONTEXT_ITEM_PRICE = "order_items:price_at_order";
    private static final String CONTEXT_PRODUCT_TITLE = "products:title";
    private static final String CONTEXT_PRODUCT_PRICE = "products:price";

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CryptoService cryptoService;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setStatusEncrypted(cryptoService.encrypt(
                OrderStatus.PENDING.name().getBytes(StandardCharsets.UTF_8), CONTEXT_ORDER_STATUS));
        order.setStatusBlindIndex(cryptoService.blindIndex(OrderStatus.PENDING.name(), CONTEXT_ORDER_STATUS));
        order.setDeliveryAddressEncrypted(cryptoService.encrypt(
                request.getDeliveryAddress().getBytes(StandardCharsets.UTF_8), CONTEXT_DELIVERY_ADDRESS));
        order.setRecipientPhoneEncrypted(cryptoService.encrypt(
                request.getRecipientPhone().getBytes(StandardCharsets.UTF_8), CONTEXT_RECIPIENT_PHONE));
        order.setRecipientPhoneBlindIndex(cryptoService.blindIndex(
                request.getRecipientPhone(), CONTEXT_RECIPIENT_PHONE));

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (OrderRequest.OrderItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + itemReq.getProductId()));

            byte[] priceDec = cryptoService.decrypt(product.getPriceEncrypted(), CONTEXT_PRODUCT_PRICE);
            BigDecimal price = new BigDecimal(new String(priceDec, StandardCharsets.UTF_8));

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(product);
            item.setQuantityEncrypted(cryptoService.encrypt(
                    String.valueOf(itemReq.getQuantity()).getBytes(StandardCharsets.UTF_8), CONTEXT_ITEM_QUANTITY));
            item.setPriceAtOrderEncrypted(cryptoService.encrypt(
                    price.toPlainString().getBytes(StandardCharsets.UTF_8), CONTEXT_ITEM_PRICE));

            orderItems.add(item);
            total = total.add(price.multiply(BigDecimal.valueOf(itemReq.getQuantity())));
        }

        order.setTotalPriceEncrypted(cryptoService.encrypt(
                total.toPlainString().getBytes(StandardCharsets.UTF_8), CONTEXT_ORDER_TOTAL));
        order.setCreatedAtEncrypted(cryptoService.encrypt(
                LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8), CONTEXT_ORDER_CREATED_AT));
        order.setOrderItems(orderItems);
        order = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmountEncrypted(cryptoService.encrypt(
                total.toPlainString().getBytes(StandardCharsets.UTF_8), "payments:amount"));
        payment.setStatusEncrypted(cryptoService.encrypt(
                PaymentStatus.SUCCESS.name().getBytes(StandardCharsets.UTF_8), "payments:status"));
        payment.setStatusBlindIndex(cryptoService.blindIndex(PaymentStatus.SUCCESS.name(), "payments:status"));

        String cardNumber = "9860" + String.format("%010d",
                java.util.concurrent.ThreadLocalRandom.current().nextLong(1_000_000_000_0L));
        String cardLast4 = cardNumber.substring(cardNumber.length() - 4);

        payment.setCardNumberEncrypted(cryptoService.encrypt(
                cardNumber.getBytes(StandardCharsets.UTF_8), CONTEXT_CARD_NUMBER));
        payment.setCardLast4BlindIndex(cryptoService.blindIndex(cardLast4, CONTEXT_CARD_NUMBER));

        byte[] decryptedName = cryptoService.decrypt(user.getFullNameEncrypted(), CONTEXT_USER_FULL_NAME);
        payment.setCardHolderNameEncrypted(cryptoService.encrypt(
                decryptedName != null ? decryptedName : "Holder".getBytes(StandardCharsets.UTF_8),
                CONTEXT_CARD_HOLDER));
        payment.setCreatedAtEncrypted(cryptoService.encrypt(
                LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8), "payments:created_at"));

        paymentRepository.save(payment);

        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> findOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId, PageRequest.of(0, 100))
                .getContent().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return toResponse(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> findAll(int page, int size) {
        return orderRepository.findAll(PageRequest.of(page, size)).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> searchByStatus(String statusQuery) {
        if (statusQuery == null || statusQuery.isBlank()) return List.of();
        String blindIndex = cryptoService.blindIndex(statusQuery, CONTEXT_ORDER_STATUS);
        return orderRepository.findByStatusBlindIndex(blindIndex).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public OrderResponse toResponse(Order order) {
        if (order == null) return null;
        byte[] deliveryBytes = cryptoService.decrypt(order.getDeliveryAddressEncrypted(), CONTEXT_DELIVERY_ADDRESS);
        byte[] phoneBytes = cryptoService.decrypt(order.getRecipientPhoneEncrypted(), CONTEXT_RECIPIENT_PHONE);
        byte[] statusBytes = cryptoService.decrypt(order.getStatusEncrypted(), CONTEXT_ORDER_STATUS);
        byte[] totalBytes = cryptoService.decrypt(order.getTotalPriceEncrypted(), CONTEXT_ORDER_TOTAL);
        byte[] createdAtBytes = cryptoService.decrypt(order.getCreatedAtEncrypted(), CONTEXT_ORDER_CREATED_AT);

        List<OrderResponse.OrderItemResponse> items = order.getOrderItems() != null
                ? order.getOrderItems().stream().map(item -> {
                    byte[] qtyBytes = cryptoService.decrypt(item.getQuantityEncrypted(), CONTEXT_ITEM_QUANTITY);
                    byte[] priceBytes = cryptoService.decrypt(item.getPriceAtOrderEncrypted(), CONTEXT_ITEM_PRICE);
                    byte[] prodTitleBytes = cryptoService.decrypt(item.getProduct().getTitleEncrypted(), CONTEXT_PRODUCT_TITLE);
                    return OrderResponse.OrderItemResponse.builder()
                            .productId(item.getProduct().getId())
                            .productTitle(prodTitleBytes != null ? new String(prodTitleBytes, StandardCharsets.UTF_8) : null)
                            .quantity(qtyBytes != null ? Integer.parseInt(new String(qtyBytes, StandardCharsets.UTF_8)) : null)
                            .priceAtOrder(priceBytes != null ? new BigDecimal(new String(priceBytes, StandardCharsets.UTF_8)) : null)
                            .build();
                }).toList()
                : List.of();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser() != null ? order.getUser().getId() : null)
                .status(statusBytes != null ? new String(statusBytes, StandardCharsets.UTF_8) : null)
                .totalPrice(totalBytes != null ? new BigDecimal(new String(totalBytes, StandardCharsets.UTF_8)) : null)
                .deliveryAddress(deliveryBytes != null ? new String(deliveryBytes, StandardCharsets.UTF_8) : null)
                .recipientPhone(phoneBytes != null ? new String(phoneBytes, StandardCharsets.UTF_8) : null)
                .items(items)
                .createdAt(createdAtBytes != null ? LocalDateTime.parse(new String(createdAtBytes, StandardCharsets.UTF_8)) : null)
                .build();
    }
}
