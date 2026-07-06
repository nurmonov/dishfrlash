package com.dishflash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String status;
    private String cardLast4;
    private LocalDateTime createdAt;
}
