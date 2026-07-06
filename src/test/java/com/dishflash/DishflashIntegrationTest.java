package com.dishflash;

import com.dishflash.crypto.CryptoService;
import com.dishflash.dto.request.RegisterRequest;
import com.dishflash.dto.response.AuthResponse;
import com.dishflash.dto.response.UserResponse;
import com.dishflash.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
class DishflashIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CryptoService cryptoService;

    @Test
    void contextLoads() {
        assertNotNull(restTemplate);
        assertNotNull(userRepository);
    }

    @Test
    void registerAndLogin() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test User");
        request.setPhone("+998901234567");
        request.setEmail("test@example.com");
        request.setPassword("password123");

        ResponseEntity<UserResponse> registerResponse = restTemplate.postForEntity(
                "/auth/register", request, UserResponse.class);
        assertEquals(HttpStatus.CREATED, registerResponse.getStatusCode());
        assertNotNull(registerResponse.getBody());
        assertEquals("Test User", registerResponse.getBody().getFullName());
        assertEquals("+998901234567", registerResponse.getBody().getPhone());

        // Login
        var loginRequest = new com.dishflash.dto.request.LoginRequest();
        loginRequest.setPhone("+998901234567");
        loginRequest.setPassword("password123");

        ResponseEntity<AuthResponse> loginResponse = restTemplate.postForEntity(
                "/auth/login", loginRequest, AuthResponse.class);
        assertEquals(HttpStatus.OK, loginResponse.getStatusCode());
        assertNotNull(loginResponse.getBody());
        assertNotNull(loginResponse.getBody().getToken());
    }
}
