package com.vhvkhangg.personalprivatevault.media.internal.infrastructure.storage;

import com.vhvkhangg.personalprivatevault.media.internal.application.storage.MediaStoragePort;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(MediaStorageProperties.class)
public class MediaStorageConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "vault.media.storage", name = "enabled", havingValue = "true")
    public S3Client s3Client(MediaStorageProperties properties) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(properties.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(properties.getAccessKey(), properties.getSecretKey())
                ));

        if (properties.getEndpoint() != null && !properties.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(properties.getEndpoint().trim()));
        }

        if (properties.isPathStyleAccess()) {
            builder.serviceConfiguration(S3Configuration.builder()
                    .pathStyleAccessEnabled(true)
                    .build());
        }

        return builder.build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "vault.media.storage", name = "enabled", havingValue = "true")
    public MediaStoragePort s3MediaStoragePort(S3Client s3Client, MediaStorageProperties properties) {
        return new S3MediaStorageAdapter(s3Client, properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "vault.media.storage", name = "enabled", havingValue = "false", matchIfMissing = true)
    public MediaStoragePort disabledMediaStoragePort() {
        return new DisabledMediaStorageAdapter();
    }
}
