package com.dishflash.service;

import com.dishflash.crypto.CryptoService;
import com.dishflash.dto.response.CategoryResponse;
import com.dishflash.entity.Category;
import com.dishflash.repository.CategoryNameNgramRepository;
import com.dishflash.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final String CONTEXT_NAME = "categories:name";

    private final CategoryRepository categoryRepository;
    private final CategoryNameNgramRepository ngramRepository;
    private final CryptoService cryptoService;

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CategoryResponse> findAllPaged(int page, int size) {
        return categoryRepository.findAll(PageRequest.of(page, size)).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CategoryResponse findById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return toResponse(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> searchByName(String nameQuery) {
        if (nameQuery == null || nameQuery.isBlank()) return List.of();
        String queryLower = nameQuery.trim().toLowerCase();
        List<String> ngramIndices = cryptoService.generateNgramBlindIndicesForSearch(nameQuery, CONTEXT_NAME);
        if (ngramIndices.isEmpty()) return List.of();
        List<Long> ids = ngramRepository.findByNgramBlindIndexIn(ngramIndices).stream()
                .map(n -> n.getCategory().getId())
                .distinct()
                .toList();
        if (ids.isEmpty()) return List.of();
        return categoryRepository.findAllById(ids).stream()
                .filter(category -> {
                    String name = cryptoService.decryptToString(category.getNameEncrypted(), CONTEXT_NAME);
                    return name != null && name.toLowerCase().contains(queryLower);
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse toResponse(Category category) {
        if (category == null) return null;
        byte[] nameBytes = cryptoService.decrypt(category.getNameEncrypted(), CONTEXT_NAME);
        return CategoryResponse.builder()
                .id(category.getId())
                .name(nameBytes != null ? new String(nameBytes, StandardCharsets.UTF_8) : null)
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }
}
