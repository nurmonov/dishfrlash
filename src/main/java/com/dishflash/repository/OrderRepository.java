package com.dishflash.repository;

import com.dishflash.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserId(Long userId, Pageable pageable);

    List<Order> findByRecipientPhoneBlindIndex(String phoneBlindIndex);

    List<Order> findByStatusBlindIndex(String statusBlindIndex);
}
