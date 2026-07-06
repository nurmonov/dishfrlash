package com.dishflash.service;

import com.dishflash.crypto.CryptoService;
import com.dishflash.dto.response.ReviewResponse;
import com.dishflash.entity.Review;
import com.dishflash.repository.ReviewCommentNgramRepository;
import com.dishflash.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final String CONTEXT_RATING = "reviews:rating";
    private static final String CONTEXT_COMMENT = "reviews:comment";
    private static final String CONTEXT_CREATED_AT = "reviews:created_at";

    private final ReviewRepository reviewRepository;
    private final ReviewCommentNgramRepository ngramRepository;
    private final CryptoService cryptoService;

    @Transactional(readOnly = true)
    public Page<ReviewResponse> findByProductId(Long productId, int page, int size) {
        return reviewRepository.findByProductId(productId, PageRequest.of(page, size)).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> findByUserId(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> searchByComment(String commentQuery) {
        if (commentQuery == null || commentQuery.isBlank()) return List.of();
        String queryLower = commentQuery.trim().toLowerCase();
        List<String> ngramIndices = cryptoService.generateNgramBlindIndicesForSearch(commentQuery, CONTEXT_COMMENT);
        if (ngramIndices.isEmpty()) return List.of();
        List<Long> ids = ngramRepository.findByNgramBlindIndexIn(ngramIndices).stream()
                .map(n -> n.getReview().getId())
                .distinct()
                .toList();
        if (ids.isEmpty()) return List.of();
        return reviewRepository.findAllById(ids).stream()
                .filter(review -> {
                    String comment = cryptoService.decryptToString(review.getCommentEncrypted(), CONTEXT_COMMENT);
                    return comment != null && comment.toLowerCase().contains(queryLower);
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> findAll(int page, int size) {
        return reviewRepository.findAll(PageRequest.of(page, size)).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public ReviewResponse findById(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found"));
        return toResponse(review);
    }

    public ReviewResponse toResponse(Review review) {
        if (review == null) return null;
        byte[] ratingBytes = cryptoService.decrypt(review.getRatingEncrypted(), CONTEXT_RATING);
        byte[] commentBytes = cryptoService.decrypt(review.getCommentEncrypted(), CONTEXT_COMMENT);
        byte[] createdAtBytes = cryptoService.decrypt(review.getCreatedAtEncrypted(), CONTEXT_CREATED_AT);

        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .rating(ratingBytes != null ? Integer.parseInt(new String(ratingBytes, StandardCharsets.UTF_8)) : null)
                .comment(commentBytes != null ? new String(commentBytes, StandardCharsets.UTF_8) : null)
                .createdAt(createdAtBytes != null ? LocalDateTime.parse(new String(createdAtBytes, StandardCharsets.UTF_8)) : null)
                .build();
    }
}
