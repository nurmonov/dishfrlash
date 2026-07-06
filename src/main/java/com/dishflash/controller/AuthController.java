package com.dishflash.controller;

import com.dishflash.crypto.CryptoService;
import com.dishflash.dto.request.LoginRequest;
import com.dishflash.dto.request.RegisterRequest;
import com.dishflash.dto.response.AuthResponse;
import com.dishflash.dto.response.UserResponse;
import com.dishflash.entity.User;
import com.dishflash.exception.UnauthorizedException;
import com.dishflash.repository.UserRepository;
import com.dishflash.security.JwtTokenProvider;
import com.dishflash.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final String CONTEXT_PHONE = "users:phone";
    private static final String CONTEXT_ROLE = "users:role";

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final CryptoService cryptoService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        String phoneBlindIndex = cryptoService.blindIndex(request.getPhone(), CONTEXT_PHONE);
        User user = userRepository.findByPhoneBlindIndex(phoneBlindIndex)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }

        String role = cryptoService.decrypt(user.getRoleEncrypted(), CONTEXT_ROLE) != null
                ? new String(cryptoService.decrypt(user.getRoleEncrypted(), CONTEXT_ROLE), StandardCharsets.UTF_8)
                : "CUSTOMER";
        String token = tokenProvider.generateToken(user.getId(), role);
        UserResponse userResponse = userService.toResponse(user);

        return ResponseEntity.ok(new AuthResponse(token, userResponse));
    }
}
