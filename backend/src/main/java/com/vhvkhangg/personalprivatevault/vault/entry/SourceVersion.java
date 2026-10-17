package com.vhvkhangg.personalprivatevault.vault.entry;

import java.util.UUID;

/** Retained source identity and authorization history; contains no source content. */
public record SourceVersion(long sourceId, String sourceType, long sourceVersion,
                            long consentResetVersion, boolean deleted, UUID reservedBy) {
    public SourceVersion {
        if (sourceId <= 0 || sourceVersion <= 0 || consentResetVersion <= 0
                || consentResetVersion > sourceVersion || sourceType == null) {
            throw new IllegalArgumentException("Invalid source history");
        }
    }
}
