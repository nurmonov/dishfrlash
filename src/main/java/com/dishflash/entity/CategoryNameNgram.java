package com.dishflash.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "category_name_ngrams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryNameNgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "ngram_blind_index", nullable = false, length = 64)
    private String ngramBlindIndex;
}
