package com.dishflash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class AuditLogResponse {
    private Long id;
    private Long userId;
    private String action;
    private String entityName;
    private Long entityId;
    private LocalDateTime timestamp;
}
