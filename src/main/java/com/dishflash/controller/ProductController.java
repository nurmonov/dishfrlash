package com.dishflash.controller;

import com.dishflash.dto.response.PagedResponse;
import com.dishflash.dto.response.ProductResponse;
import com.dishflash.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<PagedResponse<ProductResponse>> search(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size) {
        return ResponseEntity.ok(productService.searchProducts(query, category, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<ProductResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var productPage = productService.findAll(page, size);
        return ResponseEntity.ok(new PagedResponse<>(
                productPage.getContent(), productPage.getNumber(), productPage.getSize(),
                productPage.getTotalElements(), productPage.getTotalPages(), productPage.isLast()));
    }

    @GetMapping("/search/title")
    public ResponseEntity<List<ProductResponse>> searchByTitle(@RequestParam String q) {
        return ResponseEntity.ok(productService.searchByTitle(q));
    }
}
