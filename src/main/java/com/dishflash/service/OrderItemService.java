package com.dishflash.service;

import com.dishflash.crypto.CryptoService;
import com.dishflash.dto.response.OrderItemResponse;
import com.dishflash.entity.OrderItem;
import com.dishflash.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderItemService {

    private static final String CONTEXT_QUANTITY = "order_items:quantity";
    private static final String CONTEXT_PRICE = "order_items:price_at_order";
    private static final String CONTEXT_PRODUCT_TITLE = "products:title";

    private final OrderItemRepository orderItemRepository;
    private final CryptoService cryptoService;

    @Transactional(readOnly = true)
    public List<OrderItemResponse> findByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<OrderItemResponse> findAll(int page, int size) {
        return orderItemRepository.findAll(PageRequest.of(page, size)).map(this::toResponse);
    }

    public OrderItemResponse toResponse(OrderItem item) {
        if (item == null) return null;
        byte[] qtyBytes = cryptoService.decrypt(item.getQuantityEncrypted(), CONTEXT_QUANTITY);
        byte[] priceBytes = cryptoService.decrypt(item.getPriceAtOrderEncrypted(), CONTEXT_PRICE);
        byte[] titleBytes = cryptoService.decrypt(item.getProduct().getTitleEncrypted(), CONTEXT_PRODUCT_TITLE);

        return OrderItemResponse.builder()
                .id(item.getId())
                .orderId(item.getOrder() != null ? item.getOrder().getId() : null)
                .productId(item.getProduct() != null ? item.getProduct().getId() : null)
                .productTitle(titleBytes != null ? new String(titleBytes, StandardCharsets.UTF_8) : null)
                .quantity(qtyBytes != null ? Integer.parseInt(new String(qtyBytes, StandardCharsets.UTF_8)) : null)
                .priceAtOrder(priceBytes != null ? new BigDecimal(new String(priceBytes, StandardCharsets.UTF_8)) : null)
                .build();
    }
}
