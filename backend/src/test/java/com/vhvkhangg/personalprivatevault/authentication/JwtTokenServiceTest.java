package com.vhvkhangg.personalprivatevault.authentication;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.JwtTokenService;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.JwtProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;
    private JwtDecoder jwtDecoder;
    private JwtProperties jwtProperties;
    private Clock clock;

    private Instant fixedNow;

    @BeforeEach
    void setUp() {
        fixedNow = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        clock = Clock.fixed(fixedNow, ZoneOffset.UTC);

        byte[] keyBytes = new byte[32];
        for (int i = 0; i < 32; i++) {
            keyBytes[i] = (byte) (i + 1);
        }
        String base64Secret = Base64.getEncoder().encodeToString(keyBytes);

        jwtProperties = new JwtProperties();
        jwtProperties.setIssuer("personal-private-vault");
        jwtProperties.setAccessTokenLifetime(Duration.ofMinutes(15));
        jwtProperties.setSecret(base64Secret);
        jwtProperties.afterPropertiesSet();

        SecretKey secretKey = jwtProperties.toSecretKey();
        JwtEncoder jwtEncoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));

        jwtDecoder = NimbusJwtDecoder.withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        jwtTokenService = new JwtTokenService(jwtEncoder, jwtProperties, clock);
    }

    @Test
    @DisplayName("Issues JWT access token with required claims and exactly 15m expiration")
    void issuesValidJwtWithRequiredClaims() {
        String tokenValue = jwtTokenService.issueAccessToken();
        assertThat(tokenValue).isNotBlank();

        Jwt jwt = jwtDecoder.decode(tokenValue);

        assertThat(jwt.getClaimAsString("iss")).isEqualTo("personal-private-vault");
        assertThat(jwt.getSubject()).isEqualTo("1");
        assertThat(jwt.getIssuedAt()).isEqualTo(fixedNow);
        assertThat(jwt.getExpiresAt()).isEqualTo(fixedNow.plus(Duration.ofMinutes(15)));
        assertThat(jwt.getId()).isNotBlank();
        // Check that jti is a valid UUID
        assertThat(UUID.fromString(jwt.getId())).isNotNull();

        // Claims size: iss, sub, iat, exp, jti
        assertThat(jwt.getClaims()).containsOnlyKeys("iss", "sub", "iat", "exp", "jti");
    }
}
