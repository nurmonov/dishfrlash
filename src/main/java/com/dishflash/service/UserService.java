package com.dishflash.service;

import com.dishflash.crypto.CryptoService;
import com.dishflash.dto.request.RegisterRequest;
import com.dishflash.dto.response.UserResponse;
import com.dishflash.entity.User;
import com.dishflash.entity.UserNameNgram;
import com.dishflash.enums.Role;
import com.dishflash.exception.DuplicateResourceException;
import com.dishflash.exception.ResourceNotFoundException;
import com.dishflash.repository.UserNameNgramRepository;
import com.dishflash.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private static final String CONTEXT_FULL_NAME = "users:full_name";
    private static final String CONTEXT_PHONE = "users:phone";
    private static final String CONTEXT_EMAIL = "users:email";
    private static final String CONTEXT_BIRTH_DATE = "users:birth_date";
    private static final String CONTEXT_ROLE = "users:role";
    private static final String CONTEXT_CREATED_AT = "users:created_at";

    private final UserRepository userRepository;
    private final UserNameNgramRepository ngramRepository;
    private final CryptoService cryptoService;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String phoneBlindIndex = cryptoService.blindIndex(request.getPhone(), CONTEXT_PHONE);
        String emailBlindIndex = cryptoService.blindIndex(request.getEmail(), CONTEXT_EMAIL);

        if (userRepository.existsByPhoneBlindIndex(phoneBlindIndex)) {
            throw new DuplicateResourceException("Phone already registered");
        }
        if (userRepository.existsByEmailBlindIndex(emailBlindIndex)) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = new User();
        user.setFullNameEncrypted(cryptoService.encrypt(
                request.getFullName().getBytes(StandardCharsets.UTF_8), CONTEXT_FULL_NAME));
        user.setFullNameBlindIndex(cryptoService.blindIndex(request.getFullName(), CONTEXT_FULL_NAME));
        user.setPhoneEncrypted(cryptoService.encrypt(
                request.getPhone().getBytes(StandardCharsets.UTF_8), CONTEXT_PHONE));
        user.setPhoneBlindIndex(phoneBlindIndex);
        user.setEmailEncrypted(cryptoService.encrypt(
                request.getEmail().getBytes(StandardCharsets.UTF_8), CONTEXT_EMAIL));
        user.setEmailBlindIndex(emailBlindIndex);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        if (request.getBirthDate() != null) {
            user.setBirthDateEncrypted(cryptoService.encrypt(
                    request.getBirthDate().toString().getBytes(StandardCharsets.UTF_8), CONTEXT_BIRTH_DATE));
        }
        user.setRoleEncrypted(cryptoService.encrypt(
                Role.CUSTOMER.name().getBytes(StandardCharsets.UTF_8), CONTEXT_ROLE));
        user.setRoleBlindIndex(cryptoService.blindIndex(Role.CUSTOMER.name(), CONTEXT_ROLE));
        user.setCreatedAtEncrypted(cryptoService.encrypt(
                LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8), CONTEXT_CREATED_AT));

        user = userRepository.save(user);
        saveNgramIndices(user.getId(), request.getFullName());

        return toResponse(user);
    }

    private void saveNgramIndices(Long userId, String fullName) {
        List<String> ngramBlindIndices = cryptoService.generateNgramBlindIndices(fullName, CONTEXT_FULL_NAME);
        User userRef = userRepository.getReferenceById(userId);
        List<UserNameNgram> ngrams = ngramBlindIndices.stream()
                .map(index -> UserNameNgram.builder()
                        .user(userRef)
                        .ngramBlindIndex(index)
                        .build())
                .toList();
        ngramRepository.saveAll(ngrams);
    }

    @Transactional(readOnly = true)
    public UserResponse findByPhone(String phone) {
        String phoneBlindIndex = cryptoService.blindIndex(phone, CONTEXT_PHONE);
        User user = userRepository.findByPhoneBlindIndex(phoneBlindIndex)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> searchByName(String nameQuery) {
        if (nameQuery == null || nameQuery.isBlank()) return List.of();
        String queryLower = nameQuery.trim().toLowerCase();
        List<String> queryNgramIndices = cryptoService.generateNgramBlindIndicesForSearch(nameQuery, CONTEXT_FULL_NAME);
        if (queryNgramIndices.isEmpty()) return List.of();
        List<UserNameNgram> matching = ngramRepository.findByNgramBlindIndexIn(queryNgramIndices);
        if (matching.isEmpty()) return List.of();
        List<Long> userIds = matching.stream()
                .map(n -> n.getUser().getId())
                .distinct()
                .toList();
        return userRepository.findAllById(userIds).stream()
                .filter(user -> {
                    String name = cryptoService.decryptToString(user.getFullNameEncrypted(), CONTEXT_FULL_NAME);
                    return name != null && name.toLowerCase().contains(queryLower);
                })
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> findAll(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size)).map(this::toResponse);
    }

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        byte[] fullNameBytes = cryptoService.decrypt(user.getFullNameEncrypted(), CONTEXT_FULL_NAME);
        byte[] phoneBytes = cryptoService.decrypt(user.getPhoneEncrypted(), CONTEXT_PHONE);
        byte[] emailBytes = cryptoService.decrypt(user.getEmailEncrypted(), CONTEXT_EMAIL);
        byte[] roleBytes = cryptoService.decrypt(user.getRoleEncrypted(), CONTEXT_ROLE);
        byte[] createdAtBytes = cryptoService.decrypt(user.getCreatedAtEncrypted(), CONTEXT_CREATED_AT);

        return UserResponse.builder()
                .id(user.getId())
                .fullName(fullNameBytes != null ? new String(fullNameBytes, StandardCharsets.UTF_8) : null)
                .phone(phoneBytes != null ? new String(phoneBytes, StandardCharsets.UTF_8) : null)
                .email(emailBytes != null ? new String(emailBytes, StandardCharsets.UTF_8) : null)
                .role(roleBytes != null ? new String(roleBytes, StandardCharsets.UTF_8) : null)
                .createdAt(createdAtBytes != null ? LocalDateTime.parse(new String(createdAtBytes, StandardCharsets.UTF_8)) : null)
                .build();
    }
}
