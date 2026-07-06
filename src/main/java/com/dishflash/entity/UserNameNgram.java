package com.dishflash.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_name_ngrams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNameNgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "ngram_blind_index", nullable = false, length = 64)
    private String ngramBlindIndex;
}
