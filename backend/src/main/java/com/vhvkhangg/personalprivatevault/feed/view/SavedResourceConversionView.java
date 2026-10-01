package com.vhvkhangg.personalprivatevault.feed.view;

import java.time.Instant;

/**
 * Immutable read model for conversion provenance linking a saved resource to a Knowledge vault entry.
 */
public record SavedResourceConversionView(
        Long savedResourceId,
        Long targetVaultEntryId,
        Instant createdAt
) {
}
