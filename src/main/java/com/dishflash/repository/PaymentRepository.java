package com.dishflash.repository;

import com.dishflash.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByCardLast4BlindIndex(String cardLast4BlindIndex);

    Payment findByOrderId(Long orderId);

    List<Payment> findByStatusBlindIndex(String statusBlindIndex);
}
