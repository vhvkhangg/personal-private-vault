package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.internal.application.session.SessionService;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.JwtTokenService;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.TokenGenerator;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.AppUser;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginProfile;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.LoginSession;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.PublicLoginCredential;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.RefreshToken;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.AppUserRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.LoginSessionRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.PublicLoginCredentialRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.RefreshTokenRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.JwtProperties;
import com.vhvkhangg.personalprivatevault.authentication.session.InvalidRefreshTokenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SessionServiceTest {

    private AppUserRepository appUserRepository;
    private PublicLoginCredentialRepository publicLoginCredentialRepository;
    private LoginSessionRepository loginSessionRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private JwtTokenService jwtTokenService;
    private TokenGenerator tokenGenerator;
    private PasswordEncoder passwordEncoder;
    private JwtProperties jwtProperties;

    private static final Instant EXACT_EXPIRY = Instant.parse("2026-09-28T12:00:00Z");

    @BeforeEach
    void setUp() {
        appUserRepository = mock(AppUserRepository.class);
        publicLoginCredentialRepository = mock(PublicLoginCredentialRepository.class);
        loginSessionRepository = mock(LoginSessionRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        jwtTokenService = mock(JwtTokenService.class);
        tokenGenerator = new TokenGenerator();
        passwordEncoder = mock(PasswordEncoder.class);

        jwtProperties = new JwtProperties();
        jwtProperties.setSecret(Base64.getEncoder().encodeToString(new byte[32]));
        jwtProperties.afterPropertiesSet();
    }

    @Test
    @DisplayName("Fails closed when rotating at exact expiration boundary (now == expiresAt)")
    void rejectsTokenAtExactExpirationInstant() {
        Clock fixedClockAtExpiry = Clock.fixed(EXACT_EXPIRY, ZoneOffset.UTC);
        SessionService sessionService = new SessionService(
                appUserRepository,
                publicLoginCredentialRepository,
                loginSessionRepository,
                refreshTokenRepository,
                jwtTokenService,
                tokenGenerator,
                passwordEncoder,
                jwtProperties,
                fixedClockAtExpiry
        );

        String rawToken = tokenGenerator.generateRawRefreshToken();
        String tokenHash = tokenGenerator.hashToken(rawToken);

        UUID sessionId = UUID.randomUUID();
        AppUser dummyUser = new AppUser("user@vault.local", "user", "hash", "hash", EXACT_EXPIRY.minus(Duration.ofDays(1)));
        RefreshToken predecessor = new RefreshToken(
                dummyUser,
                sessionId,
                LoginProfile.PRIVATE,
                1L,
                tokenHash,
                EXACT_EXPIRY,
                EXACT_EXPIRY.minus(Duration.ofDays(1))
        );

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(predecessor));

        assertThatThrownBy(() -> sessionService.rotate(rawToken))
                .isInstanceOf(InvalidRefreshTokenException.class)
                .hasMessageContaining("expired, revoked, or replaced");
    }

    @Test
    @DisplayName("Succeeds when rotating right before expiration instant (now < expiresAt)")
    void allowsRotationJustBeforeExpirationInstant() {
        Instant justBeforeExpiry = EXACT_EXPIRY.minusMillis(1);
        Clock fixedClockJustBefore = Clock.fixed(justBeforeExpiry, ZoneOffset.UTC);
        SessionService sessionService = new SessionService(
                appUserRepository,
                publicLoginCredentialRepository,
                loginSessionRepository,
                refreshTokenRepository,
                jwtTokenService,
                tokenGenerator,
                passwordEncoder,
                jwtProperties,
                fixedClockJustBefore
        );

        String rawToken = tokenGenerator.generateRawRefreshToken();
        String tokenHash = tokenGenerator.hashToken(rawToken);

        UUID sessionId = UUID.randomUUID();
        AppUser dummyUser = new AppUser("user@vault.local", "user", "hash", "hash", EXACT_EXPIRY.minus(Duration.ofDays(1)));
        RefreshToken predecessor = new RefreshToken(
                dummyUser,
                sessionId,
                LoginProfile.PRIVATE,
                1L,
                tokenHash,
                EXACT_EXPIRY,
                EXACT_EXPIRY.minus(Duration.ofDays(1))
        );
        // Set id via reflection or mock
        org.springframework.test.util.ReflectionTestUtils.setField(predecessor, "id", 100L);

        PublicLoginCredential credential = PublicLoginCredential.unconfigured(dummyUser, justBeforeExpiry);
        LoginSession session = new LoginSession(
                sessionId,
                (short) 1,
                LoginProfile.PRIVATE,
                1L,
                null,
                100L,
                EXACT_EXPIRY.minus(Duration.ofDays(1)),
                EXACT_EXPIRY
        );

        when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(predecessor));
        when(publicLoginCredentialRepository.findByIdForShare(AppUser.SINGLETON_ID)).thenReturn(Optional.of(credential));
        when(loginSessionRepository.findByIdForUpdate(sessionId)).thenReturn(Optional.of(session));
        when(refreshTokenRepository.findByIdWithLock(100L)).thenReturn(Optional.of(predecessor));

        when(refreshTokenRepository.saveAndFlush(any(RefreshToken.class))).thenAnswer(invocation -> {
            RefreshToken saved = invocation.getArgument(0);
            if (saved.getId() == null) {
                org.springframework.test.util.ReflectionTestUtils.setField(saved, "id", 101L);
            }
            return saved;
        });
        when(loginSessionRepository.saveAndFlush(any(LoginSession.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtTokenService.issueAccessToken(eq(LoginProfile.PRIVATE), eq(sessionId), eq(1L))).thenReturn("issued-access-token");

        var result = sessionService.rotate(rawToken);

        assertThat(result).isNotNull();
        assertThat(result.accessToken()).isEqualTo("issued-access-token");

        boolean hasNonBlankSuccessor = result.refreshToken() != null && !result.refreshToken().isBlank();
        boolean distinctFromPredecessor = !Objects.equals(result.refreshToken(), rawToken);
        assertThat(hasNonBlankSuccessor).as("Rotated refresh token must be non-blank").isTrue();
        assertThat(distinctFromPredecessor).as("Rotated refresh token must differ from predecessor").isTrue();
    }
}
