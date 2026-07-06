package com.dishflash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@Builder
public class OrderItemResponse {
    private Long id;
    private Long orderId;
    private Long productId;
    private String productTitle;
    private Integer quantity;
    private BigDecimal priceAtOrder;
}
