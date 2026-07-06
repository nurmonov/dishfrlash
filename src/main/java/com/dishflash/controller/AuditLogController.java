package com.dishflash.controller;

import com.dishflash.dto.response.AuditLogResponse;
import com.dishflash.dto.response.PagedResponse;
import com.dishflash.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditService auditService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<AuditLogResponse>> getByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(auditService.findByUserId(userId));
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<AuditLogResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var auditPage = auditService.findAll(page, size);
        return ResponseEntity.ok(new PagedResponse<>(
                auditPage.getContent(), auditPage.getNumber(), auditPage.getSize(),
                auditPage.getTotalElements(), auditPage.getTotalPages(), auditPage.isLast()));
    }

    @GetMapping("/search/action")
    public ResponseEntity<List<AuditLogResponse>> searchByAction(@RequestParam String q) {
        return ResponseEntity.ok(auditService.searchByAction(q));
    }

    @GetMapping("/search/entity-name")
    public ResponseEntity<List<AuditLogResponse>> searchByEntityName(@RequestParam String q) {
        return ResponseEntity.ok(auditService.searchByEntityName(q));
    }
}
