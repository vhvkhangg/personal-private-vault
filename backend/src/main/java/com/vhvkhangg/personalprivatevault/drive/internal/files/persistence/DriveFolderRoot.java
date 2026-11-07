package com.vhvkhangg.personalprivatevault.drive.internal.files.persistence;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "drive_folder_roots")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriveFolderRoot {
    @Id @Column(name = "google_sub", length = 255, nullable = false) private String googleSub;
    @Column(name = "root_file_id", length = 255, nullable = false) private String rootFileId;
    @Column(name = "root_binding_id", nullable = false) private UUID rootBindingId;
    @Column(name = "status", length = 32, nullable = false) private String status;
    @Column(name = "binding_epoch", nullable = false) private UUID bindingEpoch;
    @Column(name = "created_at", nullable = false) private Instant createdAt;
    @Column(name = "updated_at", nullable = false) private Instant updatedAt;
}
