package com.dishflash.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "action_encrypted")
    private byte[] actionEncrypted;

    @Column(name = "action_blind_index", length = 64)
    private String actionBlindIndex;

    @Column(name = "entity_name_encrypted")
    private byte[] entityNameEncrypted;

    @Column(name = "entity_name_blind_index", length = 64)
    private String entityNameBlindIndex;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "ip_address_encrypted")
    private byte[] ipAddressEncrypted;

    @Column(name = "timestamp_encrypted")
    private byte[] timestampEncrypted;
}
