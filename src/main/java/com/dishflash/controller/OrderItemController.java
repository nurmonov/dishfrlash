package com.dishflash.controller;

import com.dishflash.dto.response.OrderItemResponse;
import com.dishflash.dto.response.PagedResponse;
import com.dishflash.service.OrderItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order-items")
@RequiredArgsConstructor
public class OrderItemController {

    private final OrderItemService orderItemService;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<OrderItemResponse>> getByOrder(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderItemService.findByOrderId(orderId));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<OrderItemResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var itemPage = orderItemService.findAll(page, size);
        return ResponseEntity.ok(new PagedResponse<>(
                itemPage.getContent(), itemPage.getNumber(), itemPage.getSize(),
                itemPage.getTotalElements(), itemPage.getTotalPages(), itemPage.isLast()));
    }
}
