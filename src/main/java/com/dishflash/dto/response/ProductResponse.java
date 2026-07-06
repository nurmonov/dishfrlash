package com.dishflash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String categoryName;
    private Long categoryId;
    private String sellerName;
    private Long sellerId;
    private Integer stockQuantity;
    private Double rating;
    private LocalDateTime createdAt;
}
