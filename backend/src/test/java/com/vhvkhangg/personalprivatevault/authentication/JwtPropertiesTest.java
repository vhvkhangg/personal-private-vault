package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtPropertiesTest {

    private static final String VALID_SECRET = Base64.getEncoder().encodeToString(new byte[32]);

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Fails closed when JWT issuer is null or blank")
    void rejectsBlankIssuer(String issuer) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(VALID_SECRET);
        properties.setIssuer(issuer);

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT issuer (ppv.security.jwt.issuer) must not be blank");
    }

    @Test
    @DisplayName("Fails closed when access token lifetime is null")
    void rejectsNullAccessTokenLifetime() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(VALID_SECRET);
        properties.setAccessTokenLifetime(null);

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT access token lifetime (ppv.security.jwt.access-token-lifetime) must be positive");
    }

    @Test
    @DisplayName("Fails closed when access token lifetime is zero")
    void rejectsZeroAccessTokenLifetime() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(VALID_SECRET);
        properties.setAccessTokenLifetime(Duration.ZERO);

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT access token lifetime (ppv.security.jwt.access-token-lifetime) must be positive");
    }

    @Test
    @DisplayName("Fails closed when access token lifetime is negative")
    void rejectsNegativeAccessTokenLifetime() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(VALID_SECRET);
        properties.setAccessTokenLifetime(Duration.ofMinutes(-1));

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT access token lifetime (ppv.security.jwt.access-token-lifetime) must be positive");
    }

    @Test
    @DisplayName("Fails closed when refresh token lifetime is null")
    void rejectsNullRefreshTokenLifetime() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(VALID_SECRET);
        properties.setRefreshTokenLifetime(null);

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT refresh token lifetime (ppv.security.jwt.refresh-token-lifetime) must be positive");
    }

    @Test
    @DisplayName("Fails closed when refresh token lifetime is zero")
    void rejectsZeroRefreshTokenLifetime() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(VALID_SECRET);
        properties.setRefreshTokenLifetime(Duration.ZERO);

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT refresh token lifetime (ppv.security.jwt.refresh-token-lifetime) must be positive");
    }

    @Test
    @DisplayName("Fails closed when refresh token lifetime is negative")
    void rejectsNegativeRefreshTokenLifetime() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(VALID_SECRET);
        properties.setRefreshTokenLifetime(Duration.ofDays(-5));

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("JWT refresh token lifetime (ppv.security.jwt.refresh-token-lifetime) must be positive");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("Fails closed when JWT secret is null or blank")
    void rejectsBlankSecret(String secret) {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(secret);

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must not be blank");
    }

    @Test
    @DisplayName("Fails closed when JWT secret is not valid Base64")
    void rejectsMalformedBase64Secret() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("not-valid-base64-!@#$%^&*()");

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be valid Base64");
    }

    @Test
    @DisplayName("Fails closed when JWT secret is shorter than 256 bits (32 bytes)")
    void rejectsShortSecret() {
        // 16 bytes encoded in Base64 (128 bits)
        String shortSecret = Base64.getEncoder().encodeToString("1234567890123456".getBytes());
        JwtProperties properties = new JwtProperties();
        properties.setSecret(shortSecret);

        assertThatThrownBy(properties::afterPropertiesSet)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must be at least 256 bits (32 bytes)");
    }

    @Test
    @DisplayName("Accepts valid 256-bit (or larger) Base64 secret and creates HMAC-SHA256 SecretKey")
    void acceptsValidSecret() {
        // 32 bytes (256 bits)
        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = (byte) (i + 1);
        }
        String validSecret = Base64.getEncoder().encodeToString(keyBytes);

        JwtProperties properties = new JwtProperties();
        properties.setSecret(validSecret);
        properties.afterPropertiesSet();

        SecretKey secretKey = properties.toSecretKey();
        assertThat(secretKey).isNotNull();
        assertThat(secretKey.getAlgorithm()).isEqualTo("HmacSHA256");
        assertThat(secretKey.getEncoded()).hasSize(32);
    }

    @Test
    @DisplayName("toString() redacts the secret property")
    void toStringRedactsSecret() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret("dGVzdC1zZWNyZXQta2V5LWZvci1wZXJzb25hbC1wcml2YXRlLXZhdWx0LXRlc3RzLTEyMzQ1Ng==");

        String representation = properties.toString();
        assertThat(representation).doesNotContain("dGVzdC1zZWNyZXQ");
    }
}
