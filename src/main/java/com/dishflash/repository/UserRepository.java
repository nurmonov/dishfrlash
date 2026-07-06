package com.dishflash.repository;

import com.dishflash.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByPhoneBlindIndex(String phoneBlindIndex);

    Optional<User> findByEmailBlindIndex(String emailBlindIndex);

    Optional<User> findByFullNameBlindIndex(String fullNameBlindIndex);

    boolean existsByPhoneBlindIndex(String phoneBlindIndex);

    boolean existsByEmailBlindIndex(String emailBlindIndex);
}
