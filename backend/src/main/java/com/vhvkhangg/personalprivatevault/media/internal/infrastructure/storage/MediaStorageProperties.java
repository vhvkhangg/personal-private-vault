package com.vhvkhangg.personalprivatevault.media.internal.infrastructure.storage;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "vault.media.storage")
public class MediaStorageProperties {

    private boolean enabled = false;
    private String endpoint;
    private String region = "us-east-1";
    private String bucket;
    private String accessKey;
    private String secretKey;
    private boolean pathStyleAccess = true;
    private long maxFileSize = 52428800L; // 50MB default

    @PostConstruct
    public void validate() {
        if (enabled) {
            if (bucket == null || bucket.isBlank()) {
                throw new IllegalStateException("vault.media.storage.bucket must not be blank when storage is enabled");
            }
            if (region == null || region.isBlank()) {
                throw new IllegalStateException("vault.media.storage.region must not be blank when storage is enabled");
            }
            if (accessKey == null || accessKey.isBlank()) {
                throw new IllegalStateException("vault.media.storage.access-key must not be blank when storage is enabled");
            }
            if (secretKey == null || secretKey.isBlank()) {
                throw new IllegalStateException("vault.media.storage.secret-key must not be blank when storage is enabled");
            }
            if (maxFileSize <= 0) {
                throw new IllegalStateException("vault.media.storage.max-file-size must be positive");
            }
        }
    }
}
