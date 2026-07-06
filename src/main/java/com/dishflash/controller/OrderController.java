package com.dishflash.controller;

import com.dishflash.dto.request.OrderRequest;
import com.dishflash.dto.response.OrderResponse;
import com.dishflash.dto.response.PagedResponse;
import com.dishflash.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.findById(id));
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@RequestParam Long userId) {
        return ResponseEntity.ok(orderService.findOrdersByUserId(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<OrderResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var orderPage = orderService.findAll(page, size);
        return ResponseEntity.ok(new PagedResponse<>(
                orderPage.getContent(), orderPage.getNumber(), orderPage.getSize(),
                orderPage.getTotalElements(), orderPage.getTotalPages(), orderPage.isLast()));
    }

    @GetMapping("/search/status")
    public ResponseEntity<List<OrderResponse>> searchByStatus(@RequestParam String q) {
        return ResponseEntity.ok(orderService.searchByStatus(q));
    }
}
