package com.vhvkhangg.personalprivatevault.drive.internal.files.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drive_child_folder_intents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriveChildFolderIntent {
    @EmbeddedId
    private DriveChildFolderKey id;

    @Column(name = "root_binding_epoch", nullable = false)
    private UUID rootBindingEpoch;

    @Column(name = "intent_epoch", nullable = false, unique = true)
    private UUID intentEpoch;

    @Column(name = "safe_name", nullable = false, length = 255)
    private String safeName;

    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    @Column(name = "request_fingerprint", nullable = false, columnDefinition = "char(64)")
    private String requestFingerprint;

    @Column(name = "provider_folder_id", length = 255, unique = true)
    private String providerFolderId;

    @Column(name = "status", nullable = false, length = 32)
    private String status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;
}
