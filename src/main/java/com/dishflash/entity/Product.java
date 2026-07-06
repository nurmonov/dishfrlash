package com.dishflash.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title_encrypted")
    private byte[] titleEncrypted;

    @Column(name = "title_blind_index", length = 64)
    private String titleBlindIndex;

    @Column(name = "description_encrypted")
    private byte[] descriptionEncrypted;

    @Column(name = "price_encrypted")
    private byte[] priceEncrypted;

    @Column(name = "stock_quantity_encrypted")
    private byte[] stockQuantityEncrypted;

    @Column(name = "rating_encrypted")
    private byte[] ratingEncrypted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @Column(name = "created_at_encrypted")
    private byte[] createdAtEncrypted;
}
