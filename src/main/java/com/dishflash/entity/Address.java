package com.dishflash.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "region_encrypted")
    private byte[] regionEncrypted;

    @Column(name = "region_blind_index", length = 64)
    private String regionBlindIndex;

    @Column(name = "city_encrypted")
    private byte[] cityEncrypted;

    @Column(name = "city_blind_index", length = 64)
    private String cityBlindIndex;

    @Column(name = "street_encrypted")
    private byte[] streetEncrypted;

    @Column(name = "house_apartment_encrypted")
    private byte[] houseApartmentEncrypted;

    @Column(name = "postal_code_encrypted")
    private byte[] postalCodeEncrypted;

    @Column(name = "postal_code_blind_index", length = 64)
    private String postalCodeBlindIndex;
}
