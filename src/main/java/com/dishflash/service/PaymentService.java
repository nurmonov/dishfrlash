package com.dishflash.service;

import com.dishflash.crypto.CryptoService;
import com.dishflash.dto.response.PaymentResponse;
import com.dishflash.entity.Payment;
import com.dishflash.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String CONTEXT_CARD_NUMBER = "payments:card_number";
    private static final String CONTEXT_AMOUNT = "payments:amount";
    private static final String CONTEXT_STATUS = "payments:status";
    private static final String CONTEXT_CREATED_AT = "payments:created_at";

    private final PaymentRepository paymentRepository;
    private final CryptoService cryptoService;

    @Transactional(readOnly = true)
    public List<PaymentResponse> searchByCardLast4(String cardLast4) {
        if (cardLast4 == null || cardLast4.isBlank()) return List.of();
        String blindIndex = cryptoService.blindIndex(cardLast4, CONTEXT_CARD_NUMBER);
        return paymentRepository.findByCardLast4BlindIndex(blindIndex).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> searchByStatus(String statusQuery) {
        if (statusQuery == null || statusQuery.isBlank()) return List.of();
        String blindIndex = cryptoService.blindIndex(statusQuery, CONTEXT_STATUS);
        return paymentRepository.findByStatusBlindIndex(blindIndex).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PaymentResponse findById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
        return toResponse(payment);
    }

    @Transactional(readOnly = true)
    public Page<PaymentResponse> findAll(int page, int size) {
        return paymentRepository.findAll(PageRequest.of(page, size)).map(this::toResponse);
    }

    public PaymentResponse toResponse(Payment payment) {
        if (payment == null) return null;
        byte[] amountBytes = cryptoService.decrypt(payment.getAmountEncrypted(), CONTEXT_AMOUNT);
        byte[] statusBytes = cryptoService.decrypt(payment.getStatusEncrypted(), CONTEXT_STATUS);
        byte[] createdAtBytes = cryptoService.decrypt(payment.getCreatedAtEncrypted(), CONTEXT_CREATED_AT);

        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder() != null ? payment.getOrder().getId() : null)
                .amount(amountBytes != null ? new BigDecimal(new String(amountBytes, StandardCharsets.UTF_8)) : null)
                .status(statusBytes != null ? new String(statusBytes, StandardCharsets.UTF_8) : null)
                .createdAt(createdAtBytes != null ? LocalDateTime.parse(new String(createdAtBytes, StandardCharsets.UTF_8)) : null)
                .build();
    }
}
