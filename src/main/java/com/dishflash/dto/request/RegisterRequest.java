package com.dishflash.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    @Pattern(regexp = "^\\+998\\d{9}$", message = "Phone must be in +998XXXXXXXXX format")
    private String phone;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    private LocalDate birthDate;
}
