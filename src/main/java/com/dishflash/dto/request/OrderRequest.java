package com.dishflash.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String deliveryAddress;

    @NotBlank
    private String recipientPhone;

    @NotNull
    private List<OrderItemRequest> items;

    @Data
    public static class OrderItemRequest {
        @NotNull
        private Long productId;

        @NotNull
        private Integer quantity;
    }
}
