package com.vhvkhangg.personalprivatevault.authentication.internal.application.google;

import com.vhvkhangg.personalprivatevault.authentication.internal.application.session.SessionService;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.AppUser;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.GoogleHandoffTicket;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.GoogleLoginAttempt;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.OwnerGoogleIdentity;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.AppUserRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.GoogleHandoffTicketRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.GoogleLoginAttemptRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.OwnerGoogleIdentityRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInTransactionCrypto;
import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
@ConditionalOnProperty(name = "ppv.security.google-signin.enabled", havingValue = "true")
@RequiredArgsConstructor
public class GoogleSignInStore {

    private final AppUserRepository appUserRepository;
    private final OwnerGoogleIdentityRepository identityRepository;
    private final GoogleLoginAttemptRepository attemptRepository;
    private final GoogleHandoffTicketRepository ticketRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public record LinkStartOutcome(GoogleLoginAttempt attempt, boolean passwordValid) {}

    public record ClaimedAttempt(
            UUID attemptId,
            UUID claimEpoch,
            String purpose,
            Short ownerId,
            String handoffChallengeHash,
            String nonceHash,
            byte[] pkceNonce,
            byte[] pkceCiphertext
    ) {}

    @Transactional
    public GoogleLoginAttempt prepareLoginStart(
            UUID attemptId,
            String stateHash,
            String bindingHash,
            String nonceHash,
            String challengeHash,
            GoogleSignInTransactionCrypto.EncryptedPkce encryptedPkce,
            Instant now
    ) {
        AppUser owner = appUserRepository.findByIdForUpdate(AppUser.SINGLETON_ID)
                .orElseThrow(GoogleSignInException::ownerNotReady);

        Optional<OwnerGoogleIdentity> identity = identityRepository.findById(AppUser.SINGLETON_ID);
        if (identity.isEmpty()) {
            throw GoogleSignInException.ownerNotReady();
        }

        enforceAdmissionAndCapacity(now);

        GoogleLoginAttempt attempt = GoogleLoginAttempt.createOidc(
                attemptId,
                GoogleLoginAttempt.PURPOSE_LOGIN,
                null,
                stateHash,
                bindingHash,
                nonceHash,
                challengeHash,
                encryptedPkce.nonce(),
                encryptedPkce.ciphertext(),
                now
        );
        return attemptRepository.save(attempt);
    }

    @Transactional
    public LinkStartOutcome prepareLinkStart(
            UUID attemptId,
            String stateHash,
            String bindingHash,
            String nonceHash,
            String challengeHash,
            GoogleSignInTransactionCrypto.EncryptedPkce encryptedPkce,
            String rawPassword,
            Instant now
    ) {
        AppUser owner = appUserRepository.findByIdForUpdate(AppUser.SINGLETON_ID)
                .orElseThrow(GoogleSignInException::ownerNotReady);

        long recentFailures = attemptRepository.countRecentPasswordFailures(owner.getId(), now.minus(Duration.ofMinutes(15)));
        if (recentFailures >= 5) {
            throw GoogleSignInException.rateLimited();
        }

        if (!passwordEncoder.matches(rawPassword, owner.getPasswordHash())) {
            enforceCapacity(now);
            GoogleLoginAttempt failureRecord = GoogleLoginAttempt.createPasswordFailure(UUID.randomUUID(), owner, now);
            attemptRepository.save(failureRecord);
            return new LinkStartOutcome(null, false);
        }

        enforceAdmissionAndCapacity(now);

        GoogleLoginAttempt attempt = GoogleLoginAttempt.createOidc(
                attemptId,
                GoogleLoginAttempt.PURPOSE_LINK,
                owner,
                stateHash,
                bindingHash,
                nonceHash,
                challengeHash,
                encryptedPkce.nonce(),
                encryptedPkce.ciphertext(),
                now
        );
        attempt = attemptRepository.save(attempt);
        return new LinkStartOutcome(attempt, true);
    }

    private void enforceAdmissionAndCapacity(Instant now) {
        enforceCapacity(now);

        long activeFlows = attemptRepository.countActiveOidcFlows(now) + ticketRepository.countActivePendingTickets(now);
        if (activeFlows >= 4) {
            throw GoogleSignInException.unavailable();
        }

        long recentStarts = attemptRepository.countRecentOidcStarts(now.minusSeconds(60));
        if (recentStarts >= 20) {
            throw GoogleSignInException.rateLimited();
        }
    }

    private void enforceCapacity(Instant now) {
        ticketRepository.expireOldTickets(now);
        attemptRepository.expireOldAttempts(now);

        long attemptsCount = attemptRepository.count();
        long ticketsCount = ticketRepository.count();
        if (attemptsCount >= 1024 || ticketsCount >= 1024) {
            Instant cutoff = now.minus(Duration.ofHours(24));
            Instant recentWindow = now.minus(Duration.ofMinutes(15));
            ticketRepository.pruneTerminalTickets(cutoff, 32);
            attemptRepository.pruneTerminalAttempts(cutoff, recentWindow, 32);
            if (attemptRepository.count() >= 1024 || ticketRepository.count() >= 1024) {
                throw GoogleSignInException.unavailable();
            }
        }
    }

    @Transactional
    public ClaimedAttempt claimAttempt(String stateHash, String bindingHash, Instant now) {
        GoogleLoginAttempt attempt = attemptRepository.findByStateHashForUpdate(stateHash)
                .orElseThrow(() -> GoogleSignInException.invalidRequest("State not found"));

        Instant claimNow = clock.instant();
        if (!GoogleLoginAttempt.STATUS_PENDING.equals(attempt.getStatus()) || !attempt.getExpiresAt().isAfter(claimNow)) {
            throw GoogleSignInException.invalidRequest("Attempt is not pending or has expired");
        }

        if (!attempt.getBindingHash().equals(bindingHash)) {
            throw GoogleSignInException.invalidRequest("Binding cookie mismatch");
        }

        UUID claimEpoch = UUID.randomUUID();
        attempt.claim(claimEpoch, claimNow);
        attemptRepository.save(attempt);

        return new ClaimedAttempt(
                attempt.getAttemptId(),
                claimEpoch,
                attempt.getPurpose(),
                attempt.getOwner() != null ? attempt.getOwner().getId() : null,
                attempt.getHandoffChallengeHash(),
                attempt.getNonceHash(),
                attempt.getPkceNonce(),
                attempt.getPkceCiphertext()
        );
    }

    @Transactional
    public void markAttemptFailed(UUID attemptId, Instant now) {
        attemptRepository.findByIdForUpdate(attemptId).ifPresent(attempt -> {
            attempt.fail(now);
            attemptRepository.save(attempt);
        });
    }

    @Transactional
    public void finalizeCompletion(
            UUID attemptId,
            UUID claimEpoch,
            String issuer,
            String googleSub,
            String ticketHash
    ) {
        AppUser owner = appUserRepository.findByIdForUpdate(AppUser.SINGLETON_ID)
                .orElseThrow(GoogleSignInException::ownerNotReady);

        GoogleLoginAttempt attempt = attemptRepository.findByIdForUpdate(attemptId)
                .orElseThrow(GoogleSignInException::invalidHandoff);

        Instant now = clock.instant();

        if (!GoogleLoginAttempt.STATUS_PROCESSING.equals(attempt.getStatus())
                || !claimEpoch.equals(attempt.getClaimEpoch())
                || !attempt.getExpiresAt().isAfter(now)) {
            throw GoogleSignInException.invalidHandoff();
        }

        if (GoogleLoginAttempt.PURPOSE_LINK.equals(attempt.getPurpose())) {
            Optional<OwnerGoogleIdentity> existing = identityRepository.findById(AppUser.SINGLETON_ID);
            if (existing.isPresent()) {
                OwnerGoogleIdentity id = existing.get();
                if (!id.getIssuer().equals(issuer) || !id.getGoogleSub().equals(googleSub)) {
                    throw GoogleSignInException.identityConflict();
                }
            } else {
                OwnerGoogleIdentity newIdentity = new OwnerGoogleIdentity(owner, issuer, googleSub, now);
                try {
                    identityRepository.saveAndFlush(newIdentity);
                } catch (org.springframework.dao.DataIntegrityViolationException ex) {
                    OwnerGoogleIdentity id = identityRepository.findById(AppUser.SINGLETON_ID)
                            .orElseThrow(GoogleSignInException::identityConflict);
                    if (!id.getIssuer().equals(issuer) || !id.getGoogleSub().equals(googleSub)) {
                        throw GoogleSignInException.identityConflict();
                    }
                }
            }
        } else {
            OwnerGoogleIdentity id = identityRepository.findById(AppUser.SINGLETON_ID)
                    .orElseThrow(GoogleSignInException::ownerNotReady);
            if (!id.getIssuer().equals(issuer) || !id.getGoogleSub().equals(googleSub)) {
                throw GoogleSignInException.identityConflict();
            }
        }

        attempt.complete(now);
        attemptRepository.save(attempt);

        GoogleHandoffTicket ticket = new GoogleHandoffTicket(
                ticketHash,
                attempt,
                owner,
                attempt.getPurpose(),
                attempt.getHandoffChallengeHash(),
                now
        );
        ticketRepository.save(ticket);
    }

    @Transactional
    public AuthTokensView redeemTicket(String ticketHash, String handoffVerifier) {
        throw GoogleSignInException.unavailable();
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        return MessageDigest.isEqual(
                a.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                b.getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
    }
}
