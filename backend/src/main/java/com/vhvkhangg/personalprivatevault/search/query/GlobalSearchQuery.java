package com.vhvkhangg.personalprivatevault.search.query;

import com.vhvkhangg.personalprivatevault.search.enums.SearchDomain;
import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

import java.util.Set;

/**
 * Validated query specification for cross-module global search.
 */
public record GlobalSearchQuery(
        String query,
        Set<SearchDomain> domains,
        Set<VaultEntryType> entryTypes,
        Set<Long> requiredTagIds,
        int offset,
        int limit
) {
    public static final int DEFAULT_LIMIT = 20;

    public GlobalSearchQuery {
        if (query == null || query.trim().isEmpty()) {
            throw new IllegalArgumentException("Search query must not be blank");
        }
        String trimmed = query.trim();
        if (trimmed.length() > 200) {
            throw new IllegalArgumentException("Search query length must not exceed 200 characters");
        }
        query = trimmed;

        if (offset < 0 || offset > 500) {
            throw new IllegalArgumentException("Offset must be between 0 and 500");
        }
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Limit must be between 1 and 100");
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

        domains = domains != null ? Set.copyOf(domains) : Set.of();
        entryTypes = entryTypes != null ? Set.copyOf(entryTypes) : Set.of();
    }

    public static GlobalSearchQuery of(String query) {
        return new GlobalSearchQuery(query, Set.of(), Set.of(), Set.of(), 0, DEFAULT_LIMIT);
    }
}
