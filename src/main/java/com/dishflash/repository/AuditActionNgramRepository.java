package com.dishflash.repository;

import com.dishflash.entity.AuditActionNgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditActionNgramRepository extends JpaRepository<AuditActionNgram, Long> {

    List<AuditActionNgram> findByNgramBlindIndexIn(List<String> ngramBlindIndices);

    @Modifying
    @Query("DELETE FROM AuditActionNgram n WHERE n.auditLog.id = :auditLogId")
    void deleteByAuditLogId(Long auditLogId);
}
