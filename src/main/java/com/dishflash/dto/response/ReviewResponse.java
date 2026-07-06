package com.dishflash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ReviewResponse {
    private Long id;
    private Long productId;
    private Long userId;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}
