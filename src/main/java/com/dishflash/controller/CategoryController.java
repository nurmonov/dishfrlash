package com.dishflash.controller;

import com.dishflash.dto.response.CategoryResponse;
import com.dishflash.dto.response.PagedResponse;
import com.dishflash.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getAll() {
        return ResponseEntity.ok(categoryService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<CategoryResponse>> getAllPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var catPage = categoryService.findAllPaged(page, size);
        return ResponseEntity.ok(new PagedResponse<>(
                catPage.getContent(), catPage.getNumber(), catPage.getSize(),
                catPage.getTotalElements(), catPage.getTotalPages(), catPage.isLast()));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> searchByName(@RequestParam String q) {
        return ResponseEntity.ok(categoryService.searchByName(q));
    }
}
