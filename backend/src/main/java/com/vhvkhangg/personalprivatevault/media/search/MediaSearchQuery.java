package com.vhvkhangg.personalprivatevault.media.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import java.util.Set;

public record MediaSearchQuery(
        String query,
        Set<VaultEntryType> entryTypes,
        Set<Long> requiredTagIds,
        int limit
) {
    public MediaSearchQuery {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Query must not be blank");
        }
        query = query.trim();
        if (query.length() > 200) {
            throw new IllegalArgumentException("Query length must not exceed 200 characters");
        }
        if (limit < 1 || limit > 601) {
            throw new IllegalArgumentException("Limit must be between 1 and 601");
        }
        if (requiredTagIds != null) {
            if (requiredTagIds.size() > 10) {
                throw new IllegalArgumentException("At most 10 required tag IDs may be provided");
            }
            for (Long tagId : requiredTagIds) {
                if (tagId == null || tagId <= 0) {
                    throw new IllegalArgumentException("Tag ID must be a positive number");
                }
            }
            requiredTagIds = Set.copyOf(requiredTagIds);
        } else {
            requiredTagIds = Set.of();
        }
        entryTypes = entryTypes != null ? Set.copyOf(entryTypes) : Set.of();
    }
}
