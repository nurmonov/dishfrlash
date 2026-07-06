package com.dishflash.service;

import com.dishflash.crypto.CryptoService;
import com.dishflash.dto.response.PagedResponse;
import com.dishflash.dto.response.ProductResponse;
import com.dishflash.entity.Product;
import com.dishflash.repository.ProductRepository;
import com.dishflash.repository.ProductTitleNgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private static final String CONTEXT_TITLE = "products:title";
    private static final String CONTEXT_DESCRIPTION = "products:description";
    private static final String CONTEXT_PRICE = "products:price";
    private static final String CONTEXT_STOCK = "products:stock_quantity";
    private static final String CONTEXT_RATING = "products:rating";
    private static final String CONTEXT_CREATED_AT = "products:created_at";
    private static final String CONTEXT_CATEGORY_NAME = "categories:name";

    private final ProductRepository productRepository;
    private final ProductTitleNgramRepository titleNgramRepository;
    private final CryptoService cryptoService;

    @Transactional(readOnly = true)
    public PagedResponse<ProductResponse> searchProducts(String query, Long categoryId, int page, int size) {
        PageRequest pageable = PageRequest.of(page, size);
        String queryLower = (query != null) ? query.trim().toLowerCase() : null;

        // No text query — use DB-level pagination (fast path)
        if (query == null || query.isBlank()) {
            Page<Product> productPage = (categoryId != null)
                    ? productRepository.findByCategoryId(categoryId, pageable)
                    : productRepository.findAll(pageable);
            List<ProductResponse> content = productPage.getContent().stream()
                    .map(this::toResponse)
                    .toList();
            return new PagedResponse<>(
                    content, productPage.getNumber(), productPage.getSize(),
                    productPage.getTotalElements(), productPage.getTotalPages(), productPage.isLast());
        }

        // Text query — n-gram pre-filter, then decrypt + filter in memory, then paginate
        List<Long> ids = searchProductIdsByTitle(query);
        List<Product> candidates = ids.isEmpty() ? List.of() : productRepository.findAllById(ids);

        List<ProductResponse> allFiltered = new ArrayList<>();
        for (Product p : candidates) {
            String title = cryptoService.decryptToString(p.getTitleEncrypted(), CONTEXT_TITLE);
            if (title != null && startsWithAnyWord(title, queryLower)) {
                if (categoryId == null || (p.getCategory() != null && p.getCategory().getId().equals(categoryId))) {
                    allFiltered.add(toResponse(p));
                }
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), allFiltered.size());
        List<ProductResponse> pageContent = start < allFiltered.size()
                ? allFiltered.subList(start, end)
                : List.of();

        int totalPages = (int) Math.ceil((double) allFiltered.size() / pageable.getPageSize());
        return new PagedResponse<>(
                pageContent, pageable.getPageNumber(), pageable.getPageSize(),
                allFiltered.size(), totalPages, end >= allFiltered.size());
    }

    private boolean startsWithAnyWord(String text, String prefix) {
        if (text == null || prefix == null || prefix.isEmpty()) return false;
        String lower = text.toLowerCase();
        if (lower.startsWith(prefix)) return true;
        for (String word : lower.split("\\s+")) {
            if (word.startsWith(prefix)) return true;
        }
        return false;
    }

    private List<Long> searchProductIdsByTitle(String query) {
        if (query == null || query.isBlank()) return List.of();
        List<String> ngramIndices = cryptoService.generateNgramBlindIndicesForSearch(query, CONTEXT_TITLE);
        if (ngramIndices.isEmpty()) return List.of();
        return titleNgramRepository.findByNgramBlindIndexIn(ngramIndices).stream()
                .map(n -> n.getProduct().getId())
                .distinct()
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponse> searchByTitle(String titleQuery) {
        if (titleQuery == null || titleQuery.isBlank()) return List.of();
        String queryLower = titleQuery.trim().toLowerCase();
        List<Long> ids = searchProductIdsByTitle(titleQuery);
        if (ids.isEmpty()) return List.of();
        return productRepository.findAllById(ids).stream()
                .filter(product -> {
                    String title = cryptoService.decryptToString(product.getTitleEncrypted(), CONTEXT_TITLE);
                    return title != null && startsWithAnyWord(title, queryLower);
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductResponse findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> findAll(int page, int size) {
        return productRepository.findAll(PageRequest.of(page, size)).map(this::toResponse);
    }

    public ProductResponse toResponse(Product product) {
        if (product == null) return null;
        byte[] titleBytes = cryptoService.decrypt(product.getTitleEncrypted(), CONTEXT_TITLE);
        byte[] descBytes = cryptoService.decrypt(product.getDescriptionEncrypted(), CONTEXT_DESCRIPTION);
        byte[] priceBytes = cryptoService.decrypt(product.getPriceEncrypted(), CONTEXT_PRICE);
        byte[] stockBytes = cryptoService.decrypt(product.getStockQuantityEncrypted(), CONTEXT_STOCK);
        byte[] ratingBytes = cryptoService.decrypt(product.getRatingEncrypted(), CONTEXT_RATING);
        byte[] createdAtBytes = cryptoService.decrypt(product.getCreatedAtEncrypted(), CONTEXT_CREATED_AT);
        byte[] catNameBytes = product.getCategory() != null
                ? cryptoService.decrypt(product.getCategory().getNameEncrypted(), CONTEXT_CATEGORY_NAME)
                : null;

        return ProductResponse.builder()
                .id(product.getId())
                .title(titleBytes != null ? new String(titleBytes, StandardCharsets.UTF_8) : null)
                .description(descBytes != null ? new String(descBytes, StandardCharsets.UTF_8) : null)
                .price(priceBytes != null ? new BigDecimal(new String(priceBytes, StandardCharsets.UTF_8)) : null)
                .categoryName(catNameBytes != null ? new String(catNameBytes, StandardCharsets.UTF_8) : null)
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
                .sellerId(product.getSeller() != null ? product.getSeller().getId() : null)
                .stockQuantity(stockBytes != null ? Integer.parseInt(new String(stockBytes, StandardCharsets.UTF_8)) : null)
                .rating(ratingBytes != null ? Double.parseDouble(new String(ratingBytes, StandardCharsets.UTF_8)) : null)
                .createdAt(createdAtBytes != null ? LocalDateTime.parse(new String(createdAtBytes, StandardCharsets.UTF_8)) : null)
                .build();
    }
}
