package com.vhvkhangg.personalprivatevault.authentication.internal.application.token;

import com.vhvkhangg.personalprivatevault.authentication.internal.domain.AppUser;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.JwtProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    public String issueAccessToken(LoginProfile profile, UUID sessionId, long generation) {
        Objects.requireNonNull(profile, "profile must not be null");
        Objects.requireNonNull(sessionId, "sessionId must not be null");
        if (generation <= 0) {
            throw new IllegalArgumentException("generation must be positive");
        }

        Instant now = clock.instant();
        Instant expiresAt = now.plus(jwtProperties.getAccessTokenLifetime());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProperties.getIssuer())
                .subject(String.valueOf(AppUser.SINGLETON_ID))
                .claim("profile", profile.name())
                .claim("sid", sessionId.toString())
                .claim("generation", generation)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    @Deprecated
    public String issueAccessToken() {
        throw new UnsupportedOperationException("Unprofiled access tokens are deprecated and prohibited");
    }
}
