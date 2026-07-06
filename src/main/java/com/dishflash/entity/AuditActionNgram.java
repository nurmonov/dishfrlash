package com.dishflash.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_action_ngrams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditActionNgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "audit_log_id", nullable = false)
    private AuditLog auditLog;

    @Column(name = "ngram_blind_index", nullable = false, length = 64)
    private String ngramBlindIndex;
}
