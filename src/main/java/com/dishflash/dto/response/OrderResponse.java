package com.dishflash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private String status;
    private BigDecimal totalPrice;
    private String deliveryAddress;
    private String recipientPhone;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;

    @Data
    @AllArgsConstructor
    @Builder
    public static class OrderItemResponse {
        private Long productId;
        private String productTitle;
        private Integer quantity;
        private BigDecimal priceAtOrder;
    }
}
