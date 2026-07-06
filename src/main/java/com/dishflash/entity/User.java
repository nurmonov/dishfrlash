package com.dishflash.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name_encrypted")
    private byte[] fullNameEncrypted;

    @Column(name = "full_name_blind_index", length = 64)
    private String fullNameBlindIndex;

    @Column(name = "phone_encrypted")
    private byte[] phoneEncrypted;

    @Column(name = "phone_blind_index", length = 64)
    private String phoneBlindIndex;

    @Column(name = "email_encrypted")
    private byte[] emailEncrypted;

    @Column(name = "email_blind_index", length = 64)
    private String emailBlindIndex;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "birth_date_encrypted")
    private byte[] birthDateEncrypted;

    @Column(name = "role_encrypted")
    private byte[] roleEncrypted;

    @Column(name = "role_blind_index", length = 64)
    private String roleBlindIndex;

    @Column(name = "created_at_encrypted")
    private byte[] createdAtEncrypted;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Order> orders = new ArrayList<>();
}
