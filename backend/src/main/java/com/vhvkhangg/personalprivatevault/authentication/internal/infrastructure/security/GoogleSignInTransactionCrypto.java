package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security;

import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Objects;
import java.util.UUID;

public class GoogleSignInTransactionCrypto {

    public static final int NONCE_LENGTH = 12;
    public static final int TAG_LENGTH_BITS = 128;
    public static final int CIPHERTEXT_LENGTH = 59; // 43 plaintext bytes + 16-byte tag
    public static final int KEY_LENGTH_BYTES = 32;

    private final SecretKey key;
    private final SecureRandom random;

    public record EncryptedPkce(byte[] nonce, byte[] ciphertext) {}

    public GoogleSignInTransactionCrypto(GoogleSignInProperties properties) {
        this(loadKeyFromFile(properties.getTxnKeyFile()), new SecureRandom());
    }

    public GoogleSignInTransactionCrypto(byte[] rawKey) {
        this(rawKey, new SecureRandom());
    }

    public GoogleSignInTransactionCrypto(byte[] rawKey, SecureRandom random) {
        Objects.requireNonNull(rawKey, "rawKey must not be null");
        if (rawKey.length != KEY_LENGTH_BYTES) {
            throw new IllegalArgumentException("Transaction key must be exactly 32 bytes");
        }
        this.key = new SecretKeySpec(rawKey, "AES");
        this.random = Objects.requireNonNull(random, "random must not be null");
    }

    private static byte[] loadKeyFromFile(String keyFilePath) {
        if (keyFilePath == null || keyFilePath.isBlank()) {
            throw new IllegalStateException("PPV_GOOGLE_SIGNIN_TXN_KEY_FILE must be configured");
        }
        Path path = Path.of(keyFilePath);
        if (!Files.exists(path) || Files.isSymbolicLink(path) || !Files.isRegularFile(path)) {
            throw new IllegalStateException("Transaction key file does not exist or is not a regular file");
        }
        try {
            byte[] bytes = Files.readAllBytes(path);
            if (bytes.length != KEY_LENGTH_BYTES) {
                throw new IllegalStateException("Transaction key file must contain exactly 32 bytes");
            }
            return bytes;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read transaction key file", e);
        }
    }

    public EncryptedPkce encryptPkce(
            String codeVerifier,
            UUID attemptId,
            String purpose,
            Short ownerId,
            String challengeHash,
            String clientId
    ) {
        Objects.requireNonNull(codeVerifier, "codeVerifier must not be null");
        byte[] plaintext = codeVerifier.getBytes(StandardCharsets.US_ASCII);
        if (plaintext.length != 43) {
            throw new IllegalArgumentException("PKCE code verifier must be 43 ASCII bytes");
        }
        byte[] nonce = new byte[NONCE_LENGTH];
        random.nextBytes(nonce);
        byte[] aad = buildAad(attemptId, purpose, ownerId, challengeHash, clientId);

        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            cipher.updateAAD(aad);
            byte[] ciphertext = cipher.doFinal(plaintext);
            if (ciphertext.length != CIPHERTEXT_LENGTH) {
                throw new IllegalStateException("Unexpected ciphertext length: " + ciphertext.length);
            }
            return new EncryptedPkce(nonce, ciphertext);
        } catch (Exception e) {
            throw new GoogleSignInException("GOOGLE_SIGNIN_CRYPTO_ERROR", 500);
        }
    }

    public String decryptPkce(
            byte[] nonce,
            byte[] ciphertext,
            UUID attemptId,
            String purpose,
            Short ownerId,
            String challengeHash,
            String clientId
    ) {
        if (nonce == null || nonce.length != NONCE_LENGTH || ciphertext == null || ciphertext.length != CIPHERTEXT_LENGTH) {
            throw new GoogleSignInException("GOOGLE_SIGNIN_CRYPTO_ERROR", 500);
        }
        byte[] aad = buildAad(attemptId, purpose, ownerId, challengeHash, clientId);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, nonce));
            cipher.updateAAD(aad);
            byte[] plaintext = cipher.doFinal(ciphertext);
            return new String(plaintext, StandardCharsets.US_ASCII);
        } catch (Exception e) {
            throw new GoogleSignInException("GOOGLE_SIGNIN_CRYPTO_ERROR", 500);
        }
    }

    private static byte[] buildAad(UUID attemptId, String purpose, Short ownerId, String challengeHash, String clientId) {
        String aad = "v1|" + attemptId + "|" + purpose + "|" + (ownerId != null ? ownerId : "anon") + "|" + challengeHash + "|" + clientId;
        return aad.getBytes(StandardCharsets.UTF_8);
    }
}
