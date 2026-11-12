package com.vhvkhangg.personalprivatevault.authentication.internal.application.session;

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
import com.vhvkhangg.personalprivatevault.authentication.session.InvalidCredentialsException;
import com.vhvkhangg.personalprivatevault.authentication.session.InvalidRefreshTokenException;
import com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand;
import com.vhvkhangg.personalprivatevault.authentication.session.SessionOperations;
import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService implements SessionOperations {

    private final AppUserRepository appUserRepository;
    private final PublicLoginCredentialRepository publicLoginCredentialRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final TokenGenerator tokenGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    @Override
    @Transactional
    public AuthTokensView login(LoginCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        if (command.identifier() == null || command.identifier().isBlank()
                || command.password() == null || command.password().isBlank()) {
            throw new InvalidCredentialsException();
        }

        String trimmedIdentifier = command.identifier().trim();
        Optional<AppUser> userOpt = appUserRepository.findByIdentifierIgnoreCase(trimmedIdentifier);

        if (userOpt.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        AppUser user = userOpt.get();
        if (!passwordEncoder.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        publicLoginCredentialRepository.findByIdForShare(AppUser.SINGLETON_ID)
                .orElseThrow(InvalidCredentialsException::new);

        UUID sessionId = UUID.randomUUID();
        Instant now = clock.instant();
        String rawRefreshToken = tokenGenerator.generateRawRefreshToken();
        String tokenHash = tokenGenerator.hashToken(rawRefreshToken);
        Instant refreshExpiresAt = now.plus(jwtProperties.getRefreshTokenLifetime());

        RefreshToken refreshToken = new RefreshToken(
                user,
                sessionId,
                LoginProfile.PRIVATE,
                1L,
                tokenHash,
                refreshExpiresAt,
                now
        );
        refreshToken = refreshTokenRepository.saveAndFlush(refreshToken);

        LoginSession session = new LoginSession(
                sessionId,
                user.getId(),
                LoginProfile.PRIVATE,
                1L,
                null,
                refreshToken.getId(),
                now,
                refreshExpiresAt
        );
        loginSessionRepository.saveAndFlush(session);

        String accessToken = jwtTokenService.issueAccessToken(LoginProfile.PRIVATE, sessionId, 1L);
        long expiresInSeconds = jwtProperties.getAccessTokenLifetime().toSeconds();

        return new AuthTokensView(accessToken, rawRefreshToken, "Bearer", expiresInSeconds);
    }

    @Override
    @Transactional
    public AuthTokensView rotate(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            throw new InvalidRefreshTokenException("Refresh token must not be null or blank");
        }

        String tokenHash = tokenGenerator.hashToken(rawRefreshToken);
        RefreshToken candidate = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        Instant now = clock.instant();
        if (candidate.isRevoked() || candidate.isReplaced() || candidate.isExpired(now) || candidate.getSessionId() == null) {
            throw new InvalidRefreshTokenException("Refresh token is expired, revoked, or replaced");
        }

        UUID sessionId = candidate.getSessionId();

        // 1. Lock ppv_public_login_credential FOR SHARE
        PublicLoginCredential credential = publicLoginCredentialRepository.findByIdForShare(AppUser.SINGLETON_ID)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid credential state"));

        // 2. Lock target lineage FOR UPDATE
        LoginSession session = loginSessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid session"));

        // 3. Lock presented refresh token FOR UPDATE
        RefreshToken predecessor = refreshTokenRepository.findByIdWithLock(candidate.getId())
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        // Refresh authoritative current time after all locks have been acquired
        now = clock.instant();

        if (session.isRevoked() || session.isExpired(now)) {
            throw new InvalidRefreshTokenException("Session is revoked or expired");
        }

        if (session.getPublicCredentialEpoch() != null
                && !Objects.equals(session.getPublicCredentialEpoch(), credential.getCredentialEpoch())) {
            throw new InvalidRefreshTokenException("Session public credential epoch mismatch");
        }

        if (predecessor.isRevoked() || predecessor.isReplaced() || predecessor.isExpired(now)) {
            throw new InvalidRefreshTokenException("Refresh token is expired, revoked, or replaced");
        }

        if (!Objects.equals(session.getCurrentRefreshTokenId(), predecessor.getId())) {
            throw new InvalidRefreshTokenException("Refresh token is not current for session");
        }

        if (!Objects.equals(predecessor.getSessionGeneration(), session.getGeneration())) {
            throw new InvalidRefreshTokenException("Refresh token generation mismatch");
        }

        if (predecessor.getProfile() != session.getActiveProfile()) {
            throw new InvalidRefreshTokenException("Refresh token profile mismatch");
        }

        AppUser user = predecessor.getUser();
        String newRawToken = tokenGenerator.generateRawRefreshToken();
        String newTokenHash = tokenGenerator.hashToken(newRawToken);
        Instant newExpiresAt = now.plus(jwtProperties.getRefreshTokenLifetime());

        RefreshToken successor = new RefreshToken(
                user,
                session.getId(),
                session.getActiveProfile(),
                session.getGeneration(),
                newTokenHash,
                newExpiresAt,
                now
        );
        successor = refreshTokenRepository.saveAndFlush(successor);

        predecessor.replaceWith(successor.getId(), now);
        refreshTokenRepository.saveAndFlush(predecessor);

        session.updateCurrentRefresh(successor.getId(), newExpiresAt);
        loginSessionRepository.saveAndFlush(session);

        String accessToken = jwtTokenService.issueAccessToken(session.getActiveProfile(), session.getId(), session.getGeneration());
        long expiresInSeconds = jwtProperties.getAccessTokenLifetime().toSeconds();

        return new AuthTokensView(accessToken, newRawToken, "Bearer", expiresInSeconds);
    }

    @Override
    @Transactional
    public void revoke(String rawRefreshToken) {
        if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
            return;
        }

        String tokenHash = tokenGenerator.hashToken(rawRefreshToken);
        Optional<RefreshToken> candidateOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (candidateOpt.isEmpty()) {
            return;
        }

        RefreshToken candidate = candidateOpt.get();
        Instant now = clock.instant();
        if (candidate.getSessionId() == null || candidate.isRevoked() || candidate.isReplaced() || candidate.isExpired(now)) {
            return;
        }

        publicLoginCredentialRepository.findByIdForShare(AppUser.SINGLETON_ID);

        Optional<LoginSession> sessionOpt = loginSessionRepository.findByIdForUpdate(candidate.getSessionId());
        if (sessionOpt.isEmpty()) {
            return;
        }
        LoginSession session = sessionOpt.get();

        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByIdWithLock(candidate.getId());
        if (tokenOpt.isEmpty()) {
            return;
        }
        RefreshToken token = tokenOpt.get();

        // Refresh authoritative current time after all locks have been acquired
        now = clock.instant();

        if (session.isRevoked() || session.isExpired(now)) {
            return;
        }
        if (token.isRevoked() || token.isReplaced() || token.isExpired(now)) {
            return;
        }

        if (!Objects.equals(session.getCurrentRefreshTokenId(), token.getId())) {
            return;
        }

        token.revoke(now);
        refreshTokenRepository.saveAndFlush(token);
        session.revoke(now);
        loginSessionRepository.saveAndFlush(session);
    }
}
