package com.dishflash.crypto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CryptoServiceTest {

    private CryptoService cryptoService;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoService();
        ReflectionTestUtils.setField(cryptoService, "masterKeyBase64",
                "dGhpcyBpcyBhIDMyLWJ5dGUgbWFzdGVyIGtleSBmb3IgdGVzdGluZw==");
        cryptoService.init();
    }

    @Test
    void encryptDecrypt_shouldReturnOriginal() {
        String plaintext = "Sherali Abdullayev";
        String fieldContext = "users:full_name";

        byte[] encrypted = cryptoService.encrypt(plaintext.getBytes(StandardCharsets.UTF_8), fieldContext);
        assertNotNull(encrypted);
        assertTrue(encrypted.length > 12); // nonce + ciphertext + tag

        byte[] decrypted = cryptoService.decrypt(encrypted, fieldContext);
        assertNotNull(decrypted);
        assertEquals(plaintext, new String(decrypted, StandardCharsets.UTF_8));
    }

    @Test
    void differentKeys_shouldProduceDifferentCiphertexts() {
        String plaintext = "+998901234567";
        byte[] enc1 = cryptoService.encrypt(plaintext.getBytes(StandardCharsets.UTF_8), "users:phone");
        byte[] enc2 = cryptoService.encrypt(plaintext.getBytes(StandardCharsets.UTF_8), "users:phone");
        assertNotNull(enc1);
        assertNotNull(enc2);
        // Even with same key, different nonces produce different ciphertexts
        assertNotEquals(java.util.Base64.getEncoder().encodeToString(enc1),
                java.util.Base64.getEncoder().encodeToString(enc2));
    }

    @Test
    void blindIndex_shouldBeDeterministic() {
        String value = "+998901234567";
        String context = "users:phone";

        String hash1 = cryptoService.blindIndex(value, context);
        String hash2 = cryptoService.blindIndex(value, context);
        assertEquals(hash1, hash2);
    }

    @Test
    void blindIndex_differentValues_shouldProduceDifferentHashes() {
        String hash1 = cryptoService.blindIndex("+998901234567", "users:phone");
        String hash2 = cryptoService.blindIndex("+998901234568", "users:phone");
        assertNotEquals(hash1, hash2);
    }

    @Test
    void blindIndex_caseInsensitive() {
        String hash1 = cryptoService.blindIndex("Sherali", "users:full_name");
        String hash2 = cryptoService.blindIndex("sherali", "users:full_name");
        assertEquals(hash1, hash2);
    }

    @Test
    void blindIndex_trimmed() {
        String hash1 = cryptoService.blindIndex("Sherali", "users:full_name");
        String hash2 = cryptoService.blindIndex("  Sherali  ", "users:full_name");
        assertEquals(hash1, hash2);
    }

    @Test
    void decrypt_wrongContext_shouldReturnNull() {
        String plaintext = "secret data";
        byte[] encrypted = cryptoService.encrypt(plaintext.getBytes(StandardCharsets.UTF_8), "correct:context");
        assertNotNull(encrypted);

        byte[] wrong = cryptoService.decrypt(encrypted, "wrong:context");
        assertNull(wrong); // GCM authentication fails -> returns null
    }

    @Test
    void ngramGeneration_shouldProduceCorrectBlindIndices() {
        String name = "Sherali";
        List<String> ngrams = cryptoService.generateNgramBlindIndices(name, "users:full_name");
        // "sherali" → 7 chars
        // bigrams (6): sh, he, er, ra, al, li
        // trigrams (5): she, her, era, ral, ali
        // total: 11 ngrams
        assertEquals(11, ngrams.size());
        assertFalse(ngrams.get(0).isEmpty());
    }

    @Test
    void encryptDecrypt_nullInput_shouldReturnNull() {
        assertNull(cryptoService.encrypt(null, "test"));
        assertNull(cryptoService.decrypt(null, "test"));
        assertNull(cryptoService.blindIndex(null, "test"));
    }

    @Test
    void differentContexts_shouldProduceDifferentBlindIndices() {
        String value = "Sherali";
        String hash1 = cryptoService.blindIndex(value, "users:full_name");
        String hash2 = cryptoService.blindIndex(value, "users:phone");
        assertNotEquals(hash1, hash2);
    }
}
