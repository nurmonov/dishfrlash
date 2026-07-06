package com.dishflash.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "review_comment_ngrams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCommentNgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Column(name = "ngram_blind_index", nullable = false, length = 64)
    private String ngramBlindIndex;
}
