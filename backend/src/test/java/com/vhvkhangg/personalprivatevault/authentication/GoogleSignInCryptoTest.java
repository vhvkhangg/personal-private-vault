package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.internal.application.google.GoogleSignInException;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.GoogleLoginAttempt;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInProperties;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInTransactionCrypto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleSignInCryptoTest {

    private GoogleSignInTransactionCrypto crypto;
    private final String clientId = "test-google-client-id";

    @BeforeEach
    void setUp() throws Exception {
        Path keyFile = Files.createTempFile("ppv-crypto-test-", ".key");
        byte[] key32 = new byte[32];
        new SecureRandom().nextBytes(key32);
        Files.write(keyFile, key32);
        keyFile.toFile().deleteOnExit();

        GoogleSignInProperties props = new GoogleSignInProperties();
        props.setEnabled(true);
        props.setClientId(clientId);
        props.setTxnKeyFile(keyFile.toAbsolutePath().toString());

        crypto = new GoogleSignInTransactionCrypto(props);
    }

    private String generate43CharVerifier() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @Test
    @DisplayName("A-09: Valid 43-char PKCE verifier encrypts to 12-byte nonce and 59-byte ciphertext (43 + 16 GCM tag)")
    void encryptsPkceVerifierToExactLengths() {
        String verifier = generate43CharVerifier();
        assertThat(verifier).hasSize(43);

        UUID attemptId = UUID.randomUUID();
        String purpose = GoogleLoginAttempt.PURPOSE_LOGIN;
        Short ownerId = null;
        String challengeHash = "a".repeat(64);

        GoogleSignInTransactionCrypto.EncryptedPkce encrypted = crypto.encryptPkce(
                verifier,
                attemptId,
                purpose,
                ownerId,
                challengeHash,
                clientId
        );

        assertThat(encrypted.nonce()).hasSize(12);
        assertThat(encrypted.ciphertext()).hasSize(59);

        // Decrypts successfully back to exact verifier
        String decrypted = crypto.decryptPkce(
                encrypted.nonce(),
                encrypted.ciphertext(),
                attemptId,
                purpose,
                ownerId,
                challengeHash,
                clientId
        );
        assertThat(decrypted).isEqualTo(verifier);
    }

    @Test
    @DisplayName("A-09: Successive encryptions of same verifier use fresh 12-byte nonce and produce different ciphertexts")
    void encryptionProducesFreshNonces() {
        String verifier = generate43CharVerifier();
        UUID attemptId = UUID.randomUUID();
        String challengeHash = "b".repeat(64);

        var enc1 = crypto.encryptPkce(verifier, attemptId, GoogleLoginAttempt.PURPOSE_LINK, (short) 1, challengeHash, clientId);
        var enc2 = crypto.encryptPkce(verifier, attemptId, GoogleLoginAttempt.PURPOSE_LINK, (short) 1, challengeHash, clientId);

        assertThat(enc1.nonce()).isNotEqualTo(enc2.nonce());
        assertThat(enc1.ciphertext()).isNotEqualTo(enc2.ciphertext());
    }

    @Test
    @DisplayName("A-09: AAD binding tampering rejects decryption (attemptId, purpose, ownerId, challenge, clientId)")
    void aadTamperingRejectsDecryption() {
        String verifier = generate43CharVerifier();
        UUID attemptId = UUID.randomUUID();
        String purpose = GoogleLoginAttempt.PURPOSE_LINK;
        Short ownerId = 1;
        String challengeHash = "c".repeat(64);

        var enc = crypto.encryptPkce(verifier, attemptId, purpose, ownerId, challengeHash, clientId);

        // Tamper attemptId
        assertThatThrownBy(() -> crypto.decryptPkce(enc.nonce(), enc.ciphertext(), UUID.randomUUID(), purpose, ownerId, challengeHash, clientId))
                .isInstanceOf(GoogleSignInException.class);

        // Tamper purpose
        assertThatThrownBy(() -> crypto.decryptPkce(enc.nonce(), enc.ciphertext(), attemptId, GoogleLoginAttempt.PURPOSE_LOGIN, ownerId, challengeHash, clientId))
                .isInstanceOf(GoogleSignInException.class);

        // Tamper ownerId
        assertThatThrownBy(() -> crypto.decryptPkce(enc.nonce(), enc.ciphertext(), attemptId, purpose, (short) 2, challengeHash, clientId))
                .isInstanceOf(GoogleSignInException.class);

        // Tamper challengeHash
        assertThatThrownBy(() -> crypto.decryptPkce(enc.nonce(), enc.ciphertext(), attemptId, purpose, ownerId, "d".repeat(64), clientId))
                .isInstanceOf(GoogleSignInException.class);

        // Tamper clientId
        assertThatThrownBy(() -> crypto.decryptPkce(enc.nonce(), enc.ciphertext(), attemptId, purpose, ownerId, challengeHash, "wrong-client-id"))
                .isInstanceOf(GoogleSignInException.class);
    }

    @Test
    @DisplayName("A-09: Ciphertext or nonce tampering rejects decryption")
    void payloadTamperingRejectsDecryption() {
        String verifier = generate43CharVerifier();
        UUID attemptId = UUID.randomUUID();
        String challengeHash = "e".repeat(64);

        var enc = crypto.encryptPkce(verifier, attemptId, GoogleLoginAttempt.PURPOSE_LOGIN, null, challengeHash, clientId);

        // Tamper ciphertext byte
        byte[] tamperedCiphertext = enc.ciphertext().clone();
        tamperedCiphertext[0] ^= 0x01;
        assertThatThrownBy(() -> crypto.decryptPkce(enc.nonce(), tamperedCiphertext, attemptId, GoogleLoginAttempt.PURPOSE_LOGIN, null, challengeHash, clientId))
                .isInstanceOf(GoogleSignInException.class);

        // Tamper nonce byte
        byte[] tamperedNonce = enc.nonce().clone();
        tamperedNonce[0] ^= 0x01;
        assertThatThrownBy(() -> crypto.decryptPkce(tamperedNonce, enc.ciphertext(), attemptId, GoogleLoginAttempt.PURPOSE_LOGIN, null, challengeHash, clientId))
                .isInstanceOf(GoogleSignInException.class);
    }

    @Test
    @DisplayName("Invalid PKCE verifier length fails validation before encryption")
    void invalidVerifierLengthFails() {
        UUID attemptId = UUID.randomUUID();
        String challengeHash = "f".repeat(64);

        assertThatThrownBy(() -> crypto.encryptPkce("too-short", attemptId, GoogleLoginAttempt.PURPOSE_LOGIN, null, challengeHash, clientId))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> crypto.encryptPkce("a".repeat(44), attemptId, GoogleLoginAttempt.PURPOSE_LOGIN, null, challengeHash, clientId))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
