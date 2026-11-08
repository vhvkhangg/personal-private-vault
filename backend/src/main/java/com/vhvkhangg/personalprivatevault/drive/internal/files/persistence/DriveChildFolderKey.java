package com.vhvkhangg.personalprivatevault.drive.internal.files.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Embeddable
@Getter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DriveChildFolderKey implements Serializable {
    @Column(name = "google_sub", nullable = false, length = 255)
    private String googleSub;

    @Column(name = "root_binding_id", nullable = false)
    private UUID rootBindingId;

    @Column(name = "idempotency_key", nullable = false, length = 53)
    private String idempotencyKey;

    @Override
    public String toString() {
        return "DriveChildFolderKey[REDACTED]";
    }
}
