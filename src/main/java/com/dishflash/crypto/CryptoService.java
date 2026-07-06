package com.dishflash.crypto;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
public class CryptoService {

    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final String AES = "AES";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int KEY_LENGTH = 32;
    private static final int[] NGRAM_SIZES = {2, 3};
    private static final int MIN_NGRAM_SIZE = 2;

    private byte[] masterKey;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.encryption.master-key}")
    private String masterKeyBase64;

    @PostConstruct
    public void init() {
        if (masterKeyBase64 == null || masterKeyBase64.isBlank()) {
            log.warn("Master encryption key not set! Generating random key for development.");
            byte[] randomKey = new byte[KEY_LENGTH];
            secureRandom.nextBytes(randomKey);
            this.masterKey = randomKey;
        } else {
            this.masterKey = Base64.getDecoder().decode(masterKeyBase64.trim());
            if (this.masterKey.length != KEY_LENGTH) {
                throw new IllegalArgumentException("Master key must be " + KEY_LENGTH + " bytes (base64 encoded)");
            }
        }
        log.info("CryptoService initialized successfully");
    }

    private byte[] deriveSubKey(String context) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(masterKey, HMAC_SHA256));
            byte[] hash = mac.doFinal(context.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(hash, KEY_LENGTH);
        } catch (Exception e) {
            throw new RuntimeException("Failed to derive subkey for: " + context, e);
        }
    }

    public byte[] encrypt(byte[] plaintext, String fieldContext) {
        if (plaintext == null) return null;
        try {
            byte[] key = deriveSubKey("enc:" + fieldContext);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, AES), spec);
            byte[] ciphertext = cipher.doFinal(plaintext);
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + ciphertext.length);
            buffer.put(iv);
            buffer.put(ciphertext);
            return buffer.array();
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed for: " + fieldContext, e);
        }
    }

    public byte[] decrypt(byte[] encrypted, String fieldContext) {
        if (encrypted == null) return null;
        try {
            byte[] key = deriveSubKey("enc:" + fieldContext);
            Cipher cipher = Cipher.getInstance(AES_GCM);
            ByteBuffer buffer = ByteBuffer.wrap(encrypted);
            byte[] iv = new byte[GCM_IV_LENGTH];
            buffer.get(iv);
            byte[] ciphertext = new byte[buffer.remaining()];
            buffer.get(ciphertext);
            GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, AES), spec);
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            log.error("Decryption failed for: {}", fieldContext);
            return null;
        }
    }

    public String decryptToString(byte[] encrypted, String fieldContext) {
        if (encrypted == null) return null;
        byte[] decrypted = decrypt(encrypted, fieldContext);
        return decrypted != null ? new String(decrypted, StandardCharsets.UTF_8) : null;
    }

    public String encryptString(String plaintext, String fieldContext) {
        if (plaintext == null) return null;
        byte[] encrypted = encrypt(plaintext.getBytes(StandardCharsets.UTF_8), fieldContext);
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decryptString(String encryptedBase64, String fieldContext) {
        if (encryptedBase64 == null) return null;
        byte[] decrypted = decrypt(Base64.getDecoder().decode(encryptedBase64), fieldContext);
        return decrypted != null ? new String(decrypted, StandardCharsets.UTF_8) : null;
    }

    public String blindIndex(String value, String fieldContext) {
        if (value == null) return null;
        try {
            byte[] blindKey = deriveSubKey("blind:" + fieldContext);
            Mac mac = Mac.getInstance(HMAC_SHA256);
            mac.init(new SecretKeySpec(blindKey, HMAC_SHA256));
            String normalized = value.trim().toLowerCase();
            byte[] hash = mac.doFinal(normalized.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute blind index for: " + fieldContext, e);
        }
    }

    public String encryptEnum(Enum<?> value, String fieldContext) {
        if (value == null) return null;
        return Base64.getEncoder().encodeToString(encrypt(value.name().getBytes(StandardCharsets.UTF_8), fieldContext));
    }

    public <T extends Enum<T>> T decryptEnum(String encryptedBase64, String fieldContext, Class<T> enumType) {
        if (encryptedBase64 == null) return null;
        byte[] decrypted = decrypt(Base64.getDecoder().decode(encryptedBase64), fieldContext);
        if (decrypted == null) return null;
        return Enum.valueOf(enumType, new String(decrypted, StandardCharsets.UTF_8));
    }

    public String encryptBigDecimal(BigDecimal value, String fieldContext) {
        if (value == null) return null;
        return encryptString(value.toPlainString(), fieldContext);
    }

    public BigDecimal decryptBigDecimal(String encryptedBase64, String fieldContext) {
        String decrypted = decryptString(encryptedBase64, fieldContext);
        if (decrypted == null) return null;
        return new BigDecimal(decrypted);
    }

    public String encryptLocalDateTime(LocalDateTime value, String fieldContext) {
        if (value == null) return null;
        return encryptString(value.toString(), fieldContext);
    }

    public LocalDateTime decryptLocalDateTime(String encryptedBase64, String fieldContext) {
        String decrypted = decryptString(encryptedBase64, fieldContext);
        if (decrypted == null) return null;
        return LocalDateTime.parse(decrypted);
    }

    public Integer decryptInteger(String encryptedBase64, String fieldContext) {
        String decrypted = decryptString(encryptedBase64, fieldContext);
        if (decrypted == null) return null;
        return Integer.parseInt(decrypted);
    }

    public Double decryptDouble(String encryptedBase64, String fieldContext) {
        String decrypted = decryptString(encryptedBase64, fieldContext);
        if (decrypted == null) return null;
        return Double.parseDouble(decrypted);
    }

    public List<String> generateNgramBlindIndices(String value, String fieldContext) {
        List<String> result = new ArrayList<>();
        if (value == null) return result;
        String normalized = value.trim().toLowerCase().replaceAll("\\s+", "");
        for (int size : NGRAM_SIZES) {
            if (normalized.length() < size) continue;
            for (int i = 0; i <= normalized.length() - size; i++) {
                String ngram = normalized.substring(i, i + size);
                result.add(blindIndex(ngram, fieldContext));
            }
        }
        return result;
    }

    public List<String> generateNgramBlindIndicesForSearch(String query, String fieldContext) {
        if (query == null || query.length() < MIN_NGRAM_SIZE) return List.of();
        String normalized = query.trim().toLowerCase().replaceAll("\\s+", "");
        List<String> ngrams = new ArrayList<>();
        for (int size : NGRAM_SIZES) {
            if (normalized.length() < size) continue;
            for (int i = 0; i <= normalized.length() - size; i++) {
                ngrams.add(normalized.substring(i, i + size));
            }
        }
        return ngrams.stream()
                .map(ng -> blindIndex(ng, fieldContext))
                .distinct()
                .toList();
    }
}
