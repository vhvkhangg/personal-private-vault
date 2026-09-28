package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.TokenGenerator;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.AppUser;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.RefreshToken;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class TokenGeneratorTest {

    private final TokenGenerator tokenGenerator = new TokenGenerator();

    @Test
    @DisplayName("Generates raw refresh token with 256 bits of entropy encoded as Base64url without padding")
    void generatesSecureRawRefreshToken() {
        String rawToken = tokenGenerator.generateRawRefreshToken();

        boolean isNotBlank = rawToken != null && !rawToken.isBlank();
        boolean containsNoPadding = rawToken != null && !rawToken.contains("=");
        int entropyByteLength = 0;
        try {
            entropyByteLength = Base64.getUrlDecoder().decode(rawToken).length;
        } catch (IllegalArgumentException ignored) {
        }

        assertThat(isNotBlank).as("Generated refresh token must be non-blank").isTrue();
        assertThat(containsNoPadding).as("Generated refresh token must not contain padding").isTrue();
        assertThat(entropyByteLength).as("Decoded token entropy byte length must be 32 (256 bits)").isEqualTo(32);
    }

    @Test
    @DisplayName("Hashes raw token into deterministic 64-character SHA-256 hex string")
    void hashesRawTokenDeterministically() {
        String rawToken = "test-raw-token-value-12345";

        String hash1 = tokenGenerator.hashToken(rawToken);
        String hash2 = tokenGenerator.hashToken(rawToken);

        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64); // 32 bytes * 2 hex chars
        assertThat(hash1).matches("^[a-f0-9]{64}$");
    }

    @Test
    @DisplayName("RefreshToken entity manages validity, expiration, revocation, and replacement correctly")
    void managesRefreshTokenStateTransitions() {
        AppUser dummyUser = new AppUser("user@vault.local", "user", "hash", "hash", Instant.now());
        Instant createdAt = Instant.parse("2026-09-28T08:00:00Z");
        Instant expiresAt = Instant.parse("2026-10-28T08:00:00Z");

        RefreshToken token = new RefreshToken(dummyUser, "hash1", expiresAt, createdAt);

        Instant checkTime = Instant.parse("2026-09-28T09:00:00Z");
        assertThat(token.isValid(checkTime)).isTrue();
        assertThat(token.isExpired(checkTime)).isFalse();
        assertThat(token.isRevoked()).isFalse();
        assertThat(token.isReplaced()).isFalse();

        // Check expiration
        Instant afterExpiry = Instant.parse("2026-10-29T00:00:00Z");
        assertThat(token.isExpired(afterExpiry)).isTrue();
        assertThat(token.isValid(afterExpiry)).isFalse();

        // Exact boundary: now == expiresAt must be treated as expired
        assertThat(token.isExpired(expiresAt)).isTrue();
        assertThat(token.isValid(expiresAt)).isFalse();

        // Check revocation
        token.revoke(checkTime);
        assertThat(token.isRevoked()).isTrue();
        assertThat(token.isValid(checkTime)).isFalse();

        // Check replacement
        RefreshToken token2 = new RefreshToken(dummyUser, "hash2", expiresAt, createdAt);
        token2.replaceWith(99L, checkTime);
        assertThat(token2.isReplaced()).isTrue();
        assertThat(token2.isRevoked()).isTrue();
        assertThat(token2.getReplacedByTokenId()).isEqualTo(99L);
        assertThat(token2.isValid(checkTime)).isFalse();
    }
}
