package com.vhvkhangg.personalprivatevault.drive.internal.files.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity @Table(name = "drive_upload_intents") @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriveUploadIntent {
    @EmbeddedId private DriveUploadKey id;
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    @Column(name="request_fingerprint", nullable=false, columnDefinition="char(64)") private String requestFingerprint;
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.CHAR)
    @Column(name="content_md5", nullable=false, columnDefinition="char(32)") private String contentMd5;
    @Column(name="size_bytes", nullable=false) private long sizeBytes;
    @Column(name="safe_name", nullable=false, length=255) private String safeName;
    @Column(name="mime_type", nullable=false, length=127) private String mimeType;
    @Column(name="provider_file_id", length=255) private String providerFileId;
    @Column(name="status", nullable=false, length=32) private String status;
    @Column(name="intent_epoch", nullable=false) private UUID intentEpoch;
    @Column(name="root_binding_epoch", nullable=false) private UUID rootBindingEpoch;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Column(name="updated_at", nullable=false) private Instant updatedAt;
    @Column(name="completed_at") private Instant completedAt;
}
