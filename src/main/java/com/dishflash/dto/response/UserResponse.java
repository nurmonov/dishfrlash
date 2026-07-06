package com.dishflash.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long id;
    private String fullName;
    private String phone;
    private String email;
    private String role;
    private LocalDateTime createdAt;
}
