package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Duration;
import java.util.Base64;

@ConfigurationProperties(prefix = "ppv.security.jwt")
@Getter
@Setter
@ToString(exclude = "secret")
public class JwtProperties implements InitializingBean {

    private String issuer = "personal-private-vault";
    private Duration accessTokenLifetime = Duration.ofMinutes(15);
    private Duration refreshTokenLifetime = Duration.ofDays(30);
    private String secret;

    @Override
    public void afterPropertiesSet() {
        validateIssuer();
        validateLifetimes();
        validateSecret();
    }

    private void validateIssuer() {
        if (issuer == null || issuer.isBlank()) {
            throw new IllegalStateException("JWT issuer (ppv.security.jwt.issuer) must not be blank");
        }
    }

    private void validateLifetimes() {
        if (accessTokenLifetime == null || accessTokenLifetime.isNegative() || accessTokenLifetime.isZero()) {
            throw new IllegalStateException("JWT access token lifetime (ppv.security.jwt.access-token-lifetime) must be positive");
        }
        if (refreshTokenLifetime == null || refreshTokenLifetime.isNegative() || refreshTokenLifetime.isZero()) {
            throw new IllegalStateException("JWT refresh token lifetime (ppv.security.jwt.refresh-token-lifetime) must be positive");
        }
    }

    public SecretKey toSecretKey() {
        validateSecret();
        byte[] decoded = Base64.getDecoder().decode(secret.trim());
        return new SecretKeySpec(decoded, "HmacSHA256");
    }

    private void validateSecret() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("JWT signing secret (ppv.security.jwt.secret) must not be blank");
        }
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(secret.trim());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("JWT signing secret (ppv.security.jwt.secret) must be valid Base64", ex);
        }
        if (decoded.length < 32) {
            throw new IllegalStateException("JWT signing secret (ppv.security.jwt.secret) must be at least 256 bits (32 bytes)");
        }
    }
}
