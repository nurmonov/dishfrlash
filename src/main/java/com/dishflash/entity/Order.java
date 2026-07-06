package com.dishflash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "status_encrypted")
    private byte[] statusEncrypted;

    @Column(name = "status_blind_index", length = 64)
    private String statusBlindIndex;

    @Column(name = "total_price_encrypted")
    private byte[] totalPriceEncrypted;

    @Column(name = "delivery_address_encrypted")
    private byte[] deliveryAddressEncrypted;

    @Column(name = "recipient_phone_encrypted")
    private byte[] recipientPhoneEncrypted;

    @Column(name = "recipient_phone_blind_index", length = 64)
    private String recipientPhoneBlindIndex;

    @Column(name = "created_at_encrypted")
    private byte[] createdAtEncrypted;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Payment payment;
}
