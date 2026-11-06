package com.vhvkhangg.personalprivatevault.drive.internal.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drive_connections")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriveConnectionEntity {

    @Id
    @Column(name = "slot", nullable = false)
    private Short slot;

    @Column(name = "google_sub", nullable = false, unique = true, length = 255)
    private String googleSub;

    @Column(name = "display_email", length = 320)
    private String displayEmail;

    @Column(name = "granted_scopes", nullable = false, columnDefinition = "TEXT")
    private String grantedScopes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private DriveConnectionStatus status;

    @Column(name = "encrypted_refresh_token")
    private byte[] encryptedRefreshToken;

    @Column(name = "key_id", length = 64)
    private String keyId;

    @Column(name = "credential_epoch", nullable = false, unique = true)
    private UUID credentialEpoch;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
}
