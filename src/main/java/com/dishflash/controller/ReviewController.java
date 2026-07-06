package com.dishflash.controller;

import com.dishflash.dto.response.PagedResponse;
import com.dishflash.dto.response.ReviewResponse;
import com.dishflash.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/product/{productId}")
    public ResponseEntity<PagedResponse<ReviewResponse>> getByProduct(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var reviewPage = reviewService.findByProductId(productId, page, size);
        return ResponseEntity.ok(new PagedResponse<>(
                reviewPage.getContent(), reviewPage.getNumber(), reviewPage.getSize(),
                reviewPage.getTotalElements(), reviewPage.getTotalPages(), reviewPage.isLast()));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ReviewResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.findByUserId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReviewResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<ReviewResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var reviewPage = reviewService.findAll(page, size);
        return ResponseEntity.ok(new PagedResponse<>(
                reviewPage.getContent(), reviewPage.getNumber(), reviewPage.getSize(),
                reviewPage.getTotalElements(), reviewPage.getTotalPages(), reviewPage.isLast()));
    }

    @GetMapping("/search/comment")
    public ResponseEntity<List<ReviewResponse>> searchByComment(@RequestParam String q) {
        return ResponseEntity.ok(reviewService.searchByComment(q));
    }
}
