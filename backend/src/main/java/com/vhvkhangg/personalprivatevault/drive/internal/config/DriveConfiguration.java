package com.vhvkhangg.personalprivatevault.drive.internal.config;

import com.vhvkhangg.personalprivatevault.drive.internal.application.DriveConnectionService;
import com.vhvkhangg.personalprivatevault.drive.internal.crypto.DriveCredentialCrypto;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.DriveOAuthClient;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.OAuthAttemptManager;
import com.vhvkhangg.personalprivatevault.drive.internal.oauth.SubjectLifecycleManager;
import com.vhvkhangg.personalprivatevault.drive.internal.persistence.DriveConnectionRepository;
import com.vhvkhangg.personalprivatevault.drive.internal.web.controller.DriveController;
import com.vhvkhangg.personalprivatevault.drive.internal.web.controller.DriveControllerAdvice;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;

@Configuration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "ppv.drive", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(DriveProperties.class)
public class DriveConfiguration {

    @Bean
    public DriveCredentialCrypto driveCredentialCrypto(DriveProperties properties) {
        String keyFile = properties.getTokenKeyFile();
        if (keyFile == null || keyFile.isBlank()) {
            throw new IllegalStateException("ppv.drive.token-key-file is required when Google Drive integration is enabled");
        }

        Path keyPath = Path.of(keyFile);
        if (!Files.exists(keyPath)) {
            throw new IllegalStateException("Configured token key file does not exist: " + keyFile);
        }
        if (!Files.isRegularFile(keyPath)) {
            throw new IllegalStateException("Configured token key path is not a regular file: " + keyFile);
        }
        if (!Files.isReadable(keyPath)) {
            throw new IllegalStateException("Configured token key file is not readable: " + keyFile);
        }

        try {
            long size = Files.size(keyPath);
            if (size != 32) {
                throw new IllegalStateException("Token key file must be exactly 32 bytes for AES-256, found: " + size + " bytes");
            }
            byte[] keyBytes = Files.readAllBytes(keyPath);
            return new DriveCredentialCrypto(keyBytes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read token key file", e);
        }
    }

    @Bean
    public OAuthAttemptManager driveOAuthAttemptManager(Clock clock) {
        return new OAuthAttemptManager(clock);
    }

    @Bean
    public SubjectLifecycleManager driveSubjectLifecycleManager(OAuthAttemptManager attempts, DriveConnectionRepository repository) {
        SubjectLifecycleManager manager = new SubjectLifecycleManager(attempts);
        manager.initializeFromDatabase(repository.findAll());
        return manager;
    }

    @Bean
    public DriveOAuthClient driveOAuthClient(
            DriveProperties properties,
            Clock clock) {

        DriveProperties.OAuth oauth = properties.getOauth();
        if (oauth.getClientId() == null || oauth.getClientId().isBlank()) {
            throw new IllegalStateException("ppv.drive.oauth.client-id is required when Google Drive integration is enabled");
        }
        if (oauth.getClientSecret() == null || oauth.getClientSecret().isBlank()) {
            throw new IllegalStateException("ppv.drive.oauth.client-secret is required when Google Drive integration is enabled");
        }
        if (!DriveProperties.LOCAL_CALLBACK.equals(oauth.getRedirectUri())) {
            throw new IllegalStateException("ppv.drive.oauth.redirect-uri must equal the approved D07 localhost callback");
        }
        if (!DriveProperties.AUTHORIZATION_ENDPOINT.equals(oauth.getAuthorizationEndpoint())
                || !DriveProperties.TOKEN_ENDPOINT.equals(oauth.getTokenEndpoint())
                || !DriveProperties.JWKS_URI.equals(oauth.getJwksUri())
                || !DriveProperties.REVOKE_ENDPOINT.equals(oauth.getRevokeEndpoint())) {
            throw new IllegalStateException("Google Drive provider endpoints are fixed trusted HTTPS endpoints");
        }

        return new DriveOAuthClient(
                oauth.getClientId(),
                oauth.getClientSecret(),
                oauth.getTokenEndpoint(),
                oauth.getRevokeEndpoint(),
                oauth.getJwksUri(),
                clock,
                null
        );
    }

    @Bean
    public DriveConnectionService driveConnectionService(
            DriveProperties properties,
            DriveConnectionRepository repository,
            DriveCredentialCrypto crypto,
            OAuthAttemptManager attemptManager,
            SubjectLifecycleManager lifecycleManager,
            DriveOAuthClient oauthClient,
            Clock clock,
            PlatformTransactionManager transactions) {
        return new DriveConnectionService(
                properties,
                repository,
                crypto,
                attemptManager,
                lifecycleManager,
                oauthClient,
                clock,
                transactions
        );
    }

    @Bean
    public DriveController driveController(DriveConnectionService connectionService) {
        return new DriveController(connectionService);
    }

    @Bean
    public DriveControllerAdvice driveControllerAdvice() {
        return new DriveControllerAdvice();
    }
}
