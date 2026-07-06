package com.dishflash.service;

import com.dishflash.crypto.CryptoService;
import com.dishflash.dto.response.AuditLogResponse;
import com.dishflash.entity.AuditActionNgram;
import com.dishflash.entity.AuditEntityNameNgram;
import com.dishflash.entity.AuditLog;
import com.dishflash.entity.User;
import com.dishflash.repository.AuditActionNgramRepository;
import com.dishflash.repository.AuditEntityNameNgramRepository;
import com.dishflash.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditService {

    private static final String CONTEXT_IP = "audit:ip_address";
    private static final String CONTEXT_ACTION = "audit:action";
    private static final String CONTEXT_ENTITY_NAME = "audit:entity_name";
    private static final String CONTEXT_TIMESTAMP = "audit:timestamp";

    private final AuditLogRepository auditLogRepository;
    private final AuditActionNgramRepository actionNgramRepository;
    private final AuditEntityNameNgramRepository entityNameNgramRepository;
    private final CryptoService cryptoService;

    @Transactional
    public void log(Long userId, String action, String entityName, Long entityId, String ipAddress) {
        AuditLog auditLog = AuditLog.builder()
                .user(userId != null ? User.builder().id(userId).build() : null)
                .actionEncrypted(cryptoService.encrypt(
                        action.getBytes(StandardCharsets.UTF_8), CONTEXT_ACTION))
                .actionBlindIndex(cryptoService.blindIndex(action, CONTEXT_ACTION))
                .entityNameEncrypted(cryptoService.encrypt(
                        entityName.getBytes(StandardCharsets.UTF_8), CONTEXT_ENTITY_NAME))
                .entityNameBlindIndex(cryptoService.blindIndex(entityName, CONTEXT_ENTITY_NAME))
                .entityId(entityId)
                .ipAddressEncrypted(ipAddress != null
                        ? cryptoService.encrypt(ipAddress.getBytes(StandardCharsets.UTF_8), CONTEXT_IP)
                        : null)
                .timestampEncrypted(cryptoService.encrypt(
                        LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8), CONTEXT_TIMESTAMP))
                .build();
        auditLog = auditLogRepository.save(auditLog);

        saveActionNgrams(auditLog, action);
        saveEntityNameNgrams(auditLog, entityName);
    }

    private void saveActionNgrams(AuditLog auditLog, String action) {
        List<String> ngrams = cryptoService.generateNgramBlindIndices(action, CONTEXT_ACTION);
        List<AuditActionNgram> entities = ngrams.stream()
                .map(idx -> AuditActionNgram.builder().auditLog(auditLog).ngramBlindIndex(idx).build())
                .toList();
        actionNgramRepository.saveAll(entities);
    }

    private void saveEntityNameNgrams(AuditLog auditLog, String entityName) {
        List<String> ngrams = cryptoService.generateNgramBlindIndices(entityName, CONTEXT_ENTITY_NAME);
        List<AuditEntityNameNgram> entities = ngrams.stream()
                .map(idx -> AuditEntityNameNgram.builder().auditLog(auditLog).ngramBlindIndex(idx).build())
                .toList();
        entityNameNgramRepository.saveAll(entities);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByUserId(Long userId) {
        return auditLogRepository.findByUserId(userId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> searchByAction(String actionQuery) {
        if (actionQuery == null || actionQuery.isBlank()) return List.of();
        String queryLower = actionQuery.trim().toLowerCase();
        List<String> ngramIndices = cryptoService.generateNgramBlindIndicesForSearch(actionQuery, CONTEXT_ACTION);
        if (ngramIndices.isEmpty()) return List.of();
        List<Long> ids = actionNgramRepository.findByNgramBlindIndexIn(ngramIndices).stream()
                .map(n -> n.getAuditLog().getId())
                .distinct()
                .toList();
        if (ids.isEmpty()) return List.of();
        return auditLogRepository.findAllById(ids).stream()
                .filter(log -> {
                    String action = cryptoService.decryptToString(log.getActionEncrypted(), CONTEXT_ACTION);
                    return action != null && action.toLowerCase().contains(queryLower);
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> searchByEntityName(String entityNameQuery) {
        if (entityNameQuery == null || entityNameQuery.isBlank()) return List.of();
        String queryLower = entityNameQuery.trim().toLowerCase();
        List<String> ngramIndices = cryptoService.generateNgramBlindIndicesForSearch(entityNameQuery, CONTEXT_ENTITY_NAME);
        if (ngramIndices.isEmpty()) return List.of();
        List<Long> ids = entityNameNgramRepository.findByNgramBlindIndexIn(ngramIndices).stream()
                .map(n -> n.getAuditLog().getId())
                .distinct()
                .toList();
        if (ids.isEmpty()) return List.of();
        return auditLogRepository.findAllById(ids).stream()
                .filter(log -> {
                    String entityName = cryptoService.decryptToString(log.getEntityNameEncrypted(), CONTEXT_ENTITY_NAME);
                    return entityName != null && entityName.toLowerCase().contains(queryLower);
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAll(int page, int size) {
        return auditLogRepository.findAll(PageRequest.of(page, size)).map(this::toResponse);
    }

    public AuditLogResponse toResponse(AuditLog auditLog) {
        if (auditLog == null) return null;
        byte[] actionBytes = cryptoService.decrypt(auditLog.getActionEncrypted(), CONTEXT_ACTION);
        byte[] entityNameBytes = cryptoService.decrypt(auditLog.getEntityNameEncrypted(), CONTEXT_ENTITY_NAME);
        byte[] timestampBytes = cryptoService.decrypt(auditLog.getTimestampEncrypted(), CONTEXT_TIMESTAMP);

        return AuditLogResponse.builder()
                .id(auditLog.getId())
                .userId(auditLog.getUser() != null ? auditLog.getUser().getId() : null)
                .action(actionBytes != null ? new String(actionBytes, StandardCharsets.UTF_8) : null)
                .entityName(entityNameBytes != null ? new String(entityNameBytes, StandardCharsets.UTF_8) : null)
                .entityId(auditLog.getEntityId())
                .timestamp(timestampBytes != null ? LocalDateTime.parse(new String(timestampBytes, StandardCharsets.UTF_8)) : null)
                .build();
    }
}
