package com.dishflash.controller;

import com.dishflash.dto.response.PagedResponse;
import com.dishflash.dto.response.PaymentResponse;
import com.dishflash.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @GetMapping("/search/card")
    public ResponseEntity<List<PaymentResponse>> searchByCardLast4(@RequestParam String cardLast4) {
        return ResponseEntity.ok(paymentService.searchByCardLast4(cardLast4));
    }

    @GetMapping("/search/status")
    public ResponseEntity<List<PaymentResponse>> searchByStatus(@RequestParam String q) {
        return ResponseEntity.ok(paymentService.searchByStatus(q));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.findById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<PaymentResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var paymentPage = paymentService.findAll(page, size);
        return ResponseEntity.ok(new PagedResponse<>(
                paymentPage.getContent(), paymentPage.getNumber(), paymentPage.getSize(),
                paymentPage.getTotalElements(), paymentPage.getTotalPages(), paymentPage.isLast()));
    }
}
