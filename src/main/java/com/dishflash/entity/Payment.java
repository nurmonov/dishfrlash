package com.dishflash.entity;

import com.dishflash.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false, unique = true)
    private Order order;

    @Column(name = "amount_encrypted")
    private byte[] amountEncrypted;

    @Column(name = "status_encrypted")
    private byte[] statusEncrypted;

    @Column(name = "status_blind_index", length = 64)
    private String statusBlindIndex;

    @Column(name = "card_number_encrypted")
    private byte[] cardNumberEncrypted;

    @Column(name = "card_last4_blind_index", length = 64)
    private String cardLast4BlindIndex;

    @Column(name = "card_holder_name_encrypted")
    private byte[] cardHolderNameEncrypted;

    @Column(name = "created_at_encrypted")
    private byte[] createdAtEncrypted;
}
