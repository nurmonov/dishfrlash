package com.dishflash.repository;

import com.dishflash.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    List<AuditLog> findByUserId(Long userId);

    List<AuditLog> findByActionBlindIndex(String actionBlindIndex);

    List<AuditLog> findByEntityNameBlindIndex(String entityNameBlindIndex);
}
