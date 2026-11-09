package com.vhvkhangg.personalprivatevault.authentication.internal.application.google;

import com.vhvkhangg.personalprivatevault.authentication.internal.domain.GoogleLoginAttempt;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.oauth.GoogleSignInProviderClient;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInProperties;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInResources;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.GoogleSignInTransactionCrypto;
import com.vhvkhangg.personalprivatevault.authentication.view.AuthTokensView;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Objects;
import java.util.UUID;
import java.util.regex.Pattern;

@Service
@ConditionalOnProperty(name = "ppv.security.google-signin.enabled", havingValue = "true")
@RequiredArgsConstructor
public class GoogleSignInService {

    public static final String DEFAULT_CALLBACK_URI = "http://localhost:8080/api/v1/auth/google/callback";
    public static final String DEFAULT_AUTH_ENDPOINT = "https://accounts.google.com/o/oauth2/v2/auth";

    private static final Pattern HEX_64 = Pattern.compile("^[0-9a-f]{64}$");
    private static final Pattern BASE64URL_43 = Pattern.compile("^[a-zA-Z0-9_-]{43}$");

    private final GoogleSignInProperties properties;
    private final GoogleSignInStore store;
    private final GoogleSignInTransactionCrypto crypto;
    private final GoogleSignInProviderClient providerClient;
    private final GoogleSignInResources resources;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    private volatile String authEndpoint = DEFAULT_AUTH_ENDPOINT;
    private volatile String callbackUri = DEFAULT_CALLBACK_URI;

    public record StartResult(
            UUID attemptId,
            String authorizationUrl,
            String bindingSecret,
            int expiresIn
    ) {
        @Override
        public String toString() {
            return "StartResult[attemptId=" + attemptId + ", authorizationUrl=[REDACTED]" +
                    ", bindingSecret=[REDACTED], expiresIn=" + expiresIn + "]";
        }
    }

    public record CallbackOutcome(
            boolean success,
            String ticket,
            String errorMessage
    ) {
        @Override
        public String toString() {
            return "CallbackOutcome[success=" + success + ", ticket=" + (ticket != null ? "[REDACTED]" : "null") +
                    ", errorMessage=" + errorMessage + "]";
        }
    }

    public void setCustomAuthEndpoint(String authEndpoint) {
        this.authEndpoint = Objects.requireNonNull(authEndpoint);
    }

    public void setCustomCallbackUri(String callbackUri) {
        this.callbackUri = Objects.requireNonNull(callbackUri);
    }

    public StartResult startLogin(String handoffChallenge) {
        try (var slot = resources.acquireEndpoint()) {
            validateChallenge(handoffChallenge);
            Instant now = clock.instant();

            String state = generateToken();
            String nonce = generateToken();
            String codeVerifier = generateToken();
            String bindingSecret = generateToken();

            String stateHash = sha256Hex(state);
            String bindingHash = sha256Hex(bindingSecret);
            String nonceHash = sha256Hex(nonce);
            String challengeHash = handoffChallenge.toLowerCase();

            UUID attemptId = UUID.randomUUID();
            GoogleSignInTransactionCrypto.EncryptedPkce encryptedPkce = crypto.encryptPkce(
                    codeVerifier,
                    attemptId,
                    GoogleLoginAttempt.PURPOSE_LOGIN,
                    null,
                    challengeHash,
                    properties.getClientId()
            );

            store.prepareLoginStart(attemptId, stateHash, bindingHash, nonceHash, challengeHash, encryptedPkce, now);

            String codeChallenge = computeS256CodeChallenge(codeVerifier);
            String authUrl = buildAuthorizationUrl(state, nonce, codeChallenge);

            return new StartResult(attemptId, authUrl, bindingSecret, 300);
        } catch (GoogleSignInException e) {
            throw e;
        } catch (Exception e) {
            throw GoogleSignInException.unavailable();
        }
    }

    public StartResult startLink(String handoffChallenge, String rawPassword) {
        try (var slot = resources.acquireEndpoint()) {
            validateChallenge(handoffChallenge);
            if (rawPassword == null || rawPassword.isEmpty() || rawPassword.length() > 128) {
                throw GoogleSignInException.invalidRequest("Invalid password");
            }

            Instant now = clock.instant();
            String state = generateToken();
            String nonce = generateToken();
            String codeVerifier = generateToken();
            String bindingSecret = generateToken();

            String stateHash = sha256Hex(state);
            String bindingHash = sha256Hex(bindingSecret);
            String nonceHash = sha256Hex(nonce);
            String challengeHash = handoffChallenge.toLowerCase();

            UUID attemptId = UUID.randomUUID();
            GoogleSignInTransactionCrypto.EncryptedPkce encryptedPkce = crypto.encryptPkce(
                    codeVerifier,
                    attemptId,
                    GoogleLoginAttempt.PURPOSE_LINK,
                    (short) 1,
                    challengeHash,
                    properties.getClientId()
            );

            GoogleSignInStore.LinkStartOutcome outcome = store.prepareLinkStart(
                    attemptId,
                    stateHash,
                    bindingHash,
                    nonceHash,
                    challengeHash,
                    encryptedPkce,
                    rawPassword,
                    now
            );

            if (!outcome.passwordValid()) {
                throw GoogleSignInException.invalidCredentials();
            }

            String codeChallenge = computeS256CodeChallenge(codeVerifier);
            String authUrl = buildAuthorizationUrl(state, nonce, codeChallenge);

            return new StartResult(attemptId, authUrl, bindingSecret, 300);
        } catch (GoogleSignInException e) {
            throw e;
        } catch (Exception e) {
            throw GoogleSignInException.unavailable();
        }
    }

    public CallbackOutcome processCallback(String state, String code, String error, String bindingSecret) {
        resources.checkRate("callback");
        try (var slot = resources.acquireEndpoint()) {
            Instant now = clock.instant();

            if (state == null || !BASE64URL_43.matcher(state).matches()
                    || bindingSecret == null || !BASE64URL_43.matcher(bindingSecret).matches()) {
                return new CallbackOutcome(false, null, "INVALID_REQUEST");
            }

            // Code XOR error check
            boolean hasCode = code != null && !code.isBlank();
            boolean hasError = error != null && !error.isBlank();
            if ((hasCode && hasError) || (!hasCode && !hasError) || (hasCode && code.length() > 4096)) {
                return new CallbackOutcome(false, null, "INVALID_REQUEST");
            }

            if (hasError) {
                return new CallbackOutcome(false, null, "PROVIDER_ERROR");
            }

            String stateHash = sha256Hex(state);
            String bindingHash = sha256Hex(bindingSecret);

            GoogleSignInStore.ClaimedAttempt claimed;
            try {
                claimed = store.claimAttempt(stateHash, bindingHash, now);
            } catch (Exception e) {
                return new CallbackOutcome(false, null, "CLAIM_FAILED");
            }

            String codeVerifier;
            try {
                codeVerifier = crypto.decryptPkce(
                        claimed.pkceNonce(),
                        claimed.pkceCiphertext(),
                        claimed.attemptId(),
                        claimed.purpose(),
                        claimed.ownerId(),
                        claimed.handoffChallengeHash(),
                        properties.getClientId()
                );
            } catch (Exception e) {
                store.markAttemptFailed(claimed.attemptId(), now);
                return new CallbackOutcome(false, null, "CRYPTO_FAILED");
            }

            GoogleSignInProviderClient.ValidatedIdToken validated;
            try {
                validated = providerClient.exchangeCodeAndValidate(
                        code,
                        codeVerifier,
                        claimed.nonceHash(),
                        callbackUri
                );
            } catch (Exception e) {
                store.markAttemptFailed(claimed.attemptId(), now);
                return new CallbackOutcome(false, null, "VALIDATION_FAILED");
            }

            String ticket = generateToken();
            String ticketHash = sha256Hex(ticket);

            try {
                store.finalizeCompletion(
                        claimed.attemptId(),
                        claimed.claimEpoch(),
                        validated.issuer(),
                        validated.sub(),
                        ticketHash
                );
                return new CallbackOutcome(true, ticket, null);
            } catch (GoogleSignInException e) {
                store.markAttemptFailed(claimed.attemptId(), clock.instant());
                return new CallbackOutcome(false, null, e.getErrorCode());
            } catch (Exception e) {
                store.markAttemptFailed(claimed.attemptId(), clock.instant());
                return new CallbackOutcome(false, null, "COMPLETION_FAILED");
            }
        }
    }

    public AuthTokensView redeemHandoff(String ticket, String handoffVerifier) {
        resources.checkRate("handoff");
        try (var slot = resources.acquireEndpoint()) {
            if (ticket == null || !BASE64URL_43.matcher(ticket).matches()
                    || handoffVerifier == null || !BASE64URL_43.matcher(handoffVerifier).matches()) {
                throw GoogleSignInException.invalidHandoff();
            }

            String ticketHash = sha256Hex(ticket);
            return store.redeemTicket(ticketHash, handoffVerifier);
        }
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String computeS256CodeChallenge(String verifier) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String sha256Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.US_ASCII));
            StringBuilder sb = new StringBuilder(64);
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void validateChallenge(String challenge) {
        if (challenge == null || !HEX_64.matcher(challenge).matches()) {
            throw GoogleSignInException.invalidRequest("handoffChallenge must be lowercase 64-hex SHA-256");
        }
    }

    private String buildAuthorizationUrl(String state, String nonce, String codeChallenge) {
        return authEndpoint +
                "?response_type=code" +
                "&client_id=" + URLEncoder.encode(properties.getClientId(), StandardCharsets.UTF_8) +
                "&redirect_uri=" + URLEncoder.encode(callbackUri, StandardCharsets.UTF_8) +
                "&scope=openid%20email" +
                "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8) +
                "&nonce=" + URLEncoder.encode(nonce, StandardCharsets.UTF_8) +
                "&code_challenge=" + URLEncoder.encode(codeChallenge, StandardCharsets.UTF_8) +
                "&code_challenge_method=S256";
    }
}
