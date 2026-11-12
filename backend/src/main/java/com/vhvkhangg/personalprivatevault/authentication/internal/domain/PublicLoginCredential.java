package com.vhvkhangg.personalprivatevault.authentication.internal.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "ppv_public_login_credential")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PublicLoginCredential {

    @Id
    @Column(name = "owner_id")
    private Short ownerId;

    @Column(name = "password_hash", length = 255)
    private String passwordHash;

    @Column(name = "credential_epoch", nullable = false)
    private Long credentialEpoch;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public static PublicLoginCredential unconfigured(AppUser owner, Instant now) {
        PublicLoginCredential credential = new PublicLoginCredential();
        credential.ownerId = owner.getId();
        credential.passwordHash = null;
        credential.credentialEpoch = 0L;
        credential.createdAt = Objects.requireNonNull(now, "now must not be null");
        credential.updatedAt = now;
        return credential;
    }

    public boolean isConfigured() {
        return passwordHash != null && !passwordHash.isBlank() && credentialEpoch != null && credentialEpoch > 0;
    }

    public void configure(String newPasswordHash, Instant now) {
        Objects.requireNonNull(newPasswordHash, "newPasswordHash must not be null");
        Objects.requireNonNull(now, "now must not be null");
        this.passwordHash = newPasswordHash;
        if (this.credentialEpoch == null || this.credentialEpoch == 0) {
            this.credentialEpoch = 1L;
        } else {
            this.credentialEpoch++;
        }
        this.updatedAt = now;
    }

    public void reset(String newPasswordHash, Instant now) {
        Objects.requireNonNull(newPasswordHash, "newPasswordHash must not be null");
        Objects.requireNonNull(now, "now must not be null");
        this.passwordHash = newPasswordHash;
        if (this.credentialEpoch == null || this.credentialEpoch == 0) {
            this.credentialEpoch = 1L;
        } else {
            if (this.credentialEpoch == Long.MAX_VALUE) {
                throw new IllegalStateException("Credential epoch overflow");
            }
            this.credentialEpoch++;
        }
        this.updatedAt = now;
    }
}
