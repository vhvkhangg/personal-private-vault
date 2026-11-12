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
import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileSessionService {

    private final AppUserRepository appUserRepository;
    private final PublicLoginCredentialRepository publicLoginCredentialRepository;
    private final LoginSessionRepository loginSessionRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenService jwtTokenService;
    private final TokenGenerator tokenGenerator;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;
    private final Clock clock;

    @Transactional
    public AuthTokensView publicLogin(String password) {
        if (password == null || password.isBlank() || password.length() > 128) {
            throw new InvalidCredentialsException();
        }

        // Lock ppv_public_login_credential FOR SHARE
        PublicLoginCredential credential = publicLoginCredentialRepository.findByIdForShare(AppUser.SINGLETON_ID)
                .orElseThrow(InvalidCredentialsException::new);

        if (!credential.isConfigured() || !passwordEncoder.matches(password, credential.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        AppUser owner = appUserRepository.findById(AppUser.SINGLETON_ID)
                .orElseThrow(InvalidCredentialsException::new);

        UUID sessionId = UUID.randomUUID();
        Instant now = clock.instant();
        Instant refreshExpiresAt = now.plus(jwtProperties.getRefreshTokenLifetime());
        String rawRefreshToken = tokenGenerator.generateRawRefreshToken();
        String tokenHash = tokenGenerator.hashToken(rawRefreshToken);

        RefreshToken refreshToken = new RefreshToken(
                owner,
                sessionId,
                LoginProfile.PUBLIC,
                1L,
                tokenHash,
                refreshExpiresAt,
                now
        );
        refreshToken = refreshTokenRepository.saveAndFlush(refreshToken);

        LoginSession session = new LoginSession(
                sessionId,
                owner.getId(),
                LoginProfile.PUBLIC,
                1L,
                credential.getCredentialEpoch(),
                refreshToken.getId(),
                now,
                refreshExpiresAt
        );
        loginSessionRepository.saveAndFlush(session);

        String accessToken = jwtTokenService.issueAccessToken(LoginProfile.PUBLIC, sessionId, 1L);
        long expiresIn = jwtProperties.getAccessTokenLifetime().toSeconds();

        return new AuthTokensView(accessToken, rawRefreshToken, "Bearer", expiresIn);
    }

    @Transactional
    public AuthTokensView elevatePrivate(UUID sessionId, long callerGeneration, String privatePassword) {
        if (privatePassword == null || privatePassword.isBlank() || privatePassword.length() > 128) {
            throw new InvalidCredentialsException();
        }

        // Lock ppv_public_login_credential FOR SHARE
        PublicLoginCredential credential = publicLoginCredentialRepository.findByIdForShare(AppUser.SINGLETON_ID)
                .orElseThrow(InvalidCredentialsException::new);

        // Lock LoginSession FOR UPDATE
        LoginSession session = loginSessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(InvalidCredentialsException::new);

        // Lock current refresh token FOR UPDATE
        RefreshToken currentRefresh = refreshTokenRepository.findByIdWithLock(session.getCurrentRefreshTokenId())
                .orElseThrow(InvalidCredentialsException::new);

        // Refresh authoritative current time after all locks have been acquired
        Instant now = clock.instant();

        if (session.isRevoked() || session.isExpired(now)) {
            throw new InvalidCredentialsException("Session is revoked or expired");
        }
        if (session.getActiveProfile() != LoginProfile.PUBLIC) {
            throw new AccessDeniedException("Session is not in PUBLIC profile");
        }
        if (session.getGeneration() != callerGeneration) {
            throw new InvalidCredentialsException("Session generation mismatch");
        }
        if (session.getPublicCredentialEpoch() == null || !Objects.equals(session.getPublicCredentialEpoch(), credential.getCredentialEpoch())) {
            throw new InvalidCredentialsException("Public credential epoch mismatch");
        }
        if (currentRefresh.isRevoked() || currentRefresh.isReplaced() || currentRefresh.isExpired(now)) {
            throw new InvalidCredentialsException("Current refresh token is invalid");
        }

        AppUser owner = appUserRepository.findById(AppUser.SINGLETON_ID)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(privatePassword, owner.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        long nextGeneration = session.getGeneration() + 1;
        Instant newExpiresAt = now.plus(jwtProperties.getRefreshTokenLifetime());
        String newRawToken = tokenGenerator.generateRawRefreshToken();
        String newTokenHash = tokenGenerator.hashToken(newRawToken);

        RefreshToken successor = new RefreshToken(
                owner,
                session.getId(),
                LoginProfile.PRIVATE,
                nextGeneration,
                newTokenHash,
                newExpiresAt,
                now
        );
        successor = refreshTokenRepository.saveAndFlush(successor);

        currentRefresh.replaceWith(successor.getId(), now);
        refreshTokenRepository.saveAndFlush(currentRefresh);

        session.transitionProfile(LoginProfile.PRIVATE, successor.getId(), newExpiresAt, null);
        loginSessionRepository.saveAndFlush(session);

        String accessToken = jwtTokenService.issueAccessToken(LoginProfile.PRIVATE, session.getId(), nextGeneration);
        long expiresIn = jwtProperties.getAccessTokenLifetime().toSeconds();

        return new AuthTokensView(accessToken, newRawToken, "Bearer", expiresIn);
    }

    @Transactional
    public AuthTokensView downgradePublic(UUID sessionId, long callerGeneration) {
        // Lock ppv_public_login_credential FOR SHARE
        PublicLoginCredential credential = publicLoginCredentialRepository.findByIdForShare(AppUser.SINGLETON_ID)
                .orElseThrow(InvalidCredentialsException::new);

        if (!credential.isConfigured()) {
            throw new PublicNotConfiguredException("Public profile is not configured");
        }

        // Lock LoginSession FOR UPDATE
        LoginSession session = loginSessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(InvalidCredentialsException::new);

        // Lock current refresh token FOR UPDATE
        RefreshToken currentRefresh = refreshTokenRepository.findByIdWithLock(session.getCurrentRefreshTokenId())
                .orElseThrow(InvalidCredentialsException::new);

        // Refresh authoritative current time after all locks have been acquired
        Instant now = clock.instant();

        if (session.isRevoked() || session.isExpired(now)) {
            throw new InvalidCredentialsException("Session is revoked or expired");
        }
        if (session.getActiveProfile() != LoginProfile.PRIVATE) {
            throw new AccessDeniedException("Session is not in PRIVATE profile");
        }
        if (session.getGeneration() != callerGeneration) {
            throw new InvalidCredentialsException("Session generation mismatch");
        }
        if (session.getPublicCredentialEpoch() != null
                && !Objects.equals(session.getPublicCredentialEpoch(), credential.getCredentialEpoch())) {
            throw new InvalidCredentialsException("Public credential epoch mismatch");
        }
        if (currentRefresh.isRevoked() || currentRefresh.isReplaced() || currentRefresh.isExpired(now)) {
            throw new InvalidCredentialsException("Current refresh token is invalid");
        }

        AppUser owner = appUserRepository.findById(AppUser.SINGLETON_ID)
                .orElseThrow(InvalidCredentialsException::new);

        long nextGeneration = session.getGeneration() + 1;
        Instant newExpiresAt = now.plus(jwtProperties.getRefreshTokenLifetime());
        String newRawToken = tokenGenerator.generateRawRefreshToken();
        String newTokenHash = tokenGenerator.hashToken(newRawToken);

        RefreshToken successor = new RefreshToken(
                owner,
                session.getId(),
                LoginProfile.PUBLIC,
                nextGeneration,
                newTokenHash,
                newExpiresAt,
                now
        );
        successor = refreshTokenRepository.saveAndFlush(successor);

        currentRefresh.replaceWith(successor.getId(), now);
        refreshTokenRepository.saveAndFlush(currentRefresh);

        session.transitionProfile(LoginProfile.PUBLIC, successor.getId(), newExpiresAt, credential.getCredentialEpoch());
        loginSessionRepository.saveAndFlush(session);

        String accessToken = jwtTokenService.issueAccessToken(LoginProfile.PUBLIC, session.getId(), nextGeneration);
        long expiresIn = jwtProperties.getAccessTokenLifetime().toSeconds();

        return new AuthTokensView(accessToken, newRawToken, "Bearer", expiresIn);
    }

    @Transactional
    public void configureOrResetPublicCredential(
            UUID sessionId,
            long callerGeneration,
            String privatePassword,
            String publicPassword
    ) {
        if (privatePassword == null || privatePassword.isBlank() || privatePassword.length() > 128) {
            throw new InvalidCredentialsException();
        }
        if (publicPassword == null || publicPassword.length() < 12 || publicPassword.length() > 128) {
            throw new IllegalArgumentException("Public password must be between 12 and 128 characters");
        }

        AppUser owner = appUserRepository.findById(AppUser.SINGLETON_ID)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(privatePassword, owner.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid private password");
        }

        if (passwordEncoder.matches(publicPassword, owner.getPasswordHash())) {
            throw new PasswordNotDistinctException("Public password must be distinct from private password");
        }

        // Lock ppv_public_login_credential FOR UPDATE from the start
        PublicLoginCredential credential = publicLoginCredentialRepository.findByIdForUpdate(AppUser.SINGLETON_ID)
                .orElseThrow(() -> new IllegalStateException("Public login credential row not found"));

        // Lock caller's LoginSession FOR UPDATE
        LoginSession session = loginSessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(InvalidCredentialsException::new);

        Instant now = clock.instant();
        if (session.isRevoked() || session.isExpired(now)) {
            throw new InvalidCredentialsException("Session is revoked or expired");
        }
        if (session.getActiveProfile() != LoginProfile.PRIVATE) {
            throw new AccessDeniedException("Caller must be in PRIVATE profile");
        }
        if (session.getGeneration() != callerGeneration) {
            throw new InvalidCredentialsException("Session generation mismatch");
        }
        if (session.getPublicCredentialEpoch() != null
                && !Objects.equals(session.getPublicCredentialEpoch(), credential.getCredentialEpoch())) {
            throw new InvalidCredentialsException("Public credential epoch mismatch");
        }

        String newHash = passwordEncoder.encode(publicPassword);
        if (!credential.isConfigured()) {
            credential.configure(newHash, now);
        } else {
            credential.reset(newHash, now);
        }
        publicLoginCredentialRepository.saveAndFlush(credential);
    }
}
