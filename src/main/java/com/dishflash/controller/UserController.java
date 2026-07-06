package com.dishflash.controller;

import com.dishflash.dto.response.PagedResponse;
import com.dishflash.dto.response.UserResponse;
import com.dishflash.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody com.dishflash.dto.request.RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> search(
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String name) {
        if (phone != null) {
            return ResponseEntity.ok(List.of(userService.findByPhone(phone)));
        }
        if (name != null) {
            return ResponseEntity.ok(userService.searchByName(name));
        }
        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/all")
    public ResponseEntity<PagedResponse<UserResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var userPage = userService.findAll(page, size);
        return ResponseEntity.ok(new PagedResponse<>(
                userPage.getContent(), userPage.getNumber(), userPage.getSize(),
                userPage.getTotalElements(), userPage.getTotalPages(), userPage.isLast()));
    }
}
