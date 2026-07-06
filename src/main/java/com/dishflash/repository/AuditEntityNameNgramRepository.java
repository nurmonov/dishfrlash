package com.dishflash.repository;

import com.dishflash.entity.AuditEntityNameNgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditEntityNameNgramRepository extends JpaRepository<AuditEntityNameNgram, Long> {

    List<AuditEntityNameNgram> findByNgramBlindIndexIn(List<String> ngramBlindIndices);

    @Modifying
    @Query("DELETE FROM AuditEntityNameNgram n WHERE n.auditLog.id = :auditLogId")
    void deleteByAuditLogId(Long auditLogId);
}
