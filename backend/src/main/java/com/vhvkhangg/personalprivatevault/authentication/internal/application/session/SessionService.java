package com.vhvkhangg.personalprivatevault.authentication.internal.application.session;

import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.JwtTokenService;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.TokenGenerator;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.AppUser;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.RefreshToken;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.AppUserRepository;
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

@Service
@RequiredArgsConstructor
public class SessionService implements SessionOperations {

    private final AppUserRepository appUserRepository;
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

        Instant now = clock.instant();
        String rawRefreshToken = tokenGenerator.generateRawRefreshToken();
        String tokenHash = tokenGenerator.hashToken(rawRefreshToken);
        Instant refreshExpiresAt = now.plus(jwtProperties.getRefreshTokenLifetime());

        RefreshToken refreshToken = new RefreshToken(user, tokenHash, refreshExpiresAt, now);
        refreshTokenRepository.save(refreshToken);

        String accessToken = jwtTokenService.issueAccessToken();
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
        RefreshToken predecessor = refreshTokenRepository.findByTokenHashWithLock(tokenHash)
                .orElseThrow(() -> new InvalidRefreshTokenException("Invalid refresh token"));

        Instant now = clock.instant();
        if (predecessor.isRevoked() || predecessor.isReplaced() || predecessor.isExpired(now)) {
            throw new InvalidRefreshTokenException("Refresh token is expired, revoked, or replaced");
        }

        AppUser user = predecessor.getUser();
        String newRawToken = tokenGenerator.generateRawRefreshToken();
        String newTokenHash = tokenGenerator.hashToken(newRawToken);
        Instant newExpiresAt = now.plus(jwtProperties.getRefreshTokenLifetime());

        RefreshToken successor = new RefreshToken(user, newTokenHash, newExpiresAt, now);
        successor = refreshTokenRepository.saveAndFlush(successor);

        predecessor.replaceWith(successor.getId(), now);
        refreshTokenRepository.save(predecessor);

        String accessToken = jwtTokenService.issueAccessToken();
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
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByTokenHash(tokenHash);

        if (tokenOpt.isPresent()) {
            RefreshToken token = tokenOpt.get();
            if (!token.isRevoked()) {
                token.revoke(clock.instant());
                refreshTokenRepository.save(token);
            }
        }
    }
}
