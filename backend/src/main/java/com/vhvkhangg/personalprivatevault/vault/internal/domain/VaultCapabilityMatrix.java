package com.vhvkhangg.personalprivatevault.vault.internal.domain;

import com.vhvkhangg.personalprivatevault.vault.VaultEntryType;

import java.util.Set;

/**
 * Capability matrix defining which {@link VaultEntryType}s support favorites, ratings, and tags.
 *
 * <p>Explicitly enumerates every current {@link VaultEntryType} and fails closed on unmapped/null types.</p>
 */
public final class VaultCapabilityMatrix {

    private static final Set<VaultEntryType> FAVORITEABLE_TYPES = Set.of(
            VaultEntryType.FICTION,
            VaultEntryType.FILM,
            VaultEntryType.PERSON,
            VaultEntryType.FILM_CREDIT,
            VaultEntryType.IMAGE,
            VaultEntryType.ALBUM,
            VaultEntryType.BRAND,
            VaultEntryType.LOCATION,
            VaultEntryType.STUDY,
            VaultEntryType.INFORMATION,
            VaultEntryType.VOCABULARY,
            VaultEntryType.MUSIC,
            VaultEntryType.SHOPPING,
            VaultEntryType.SOFTWARE,
            VaultEntryType.NOTE,
            VaultEntryType.SAVED_RESOURCE,
            VaultEntryType.EXTERNAL_ACCOUNT
    );

    private static final Set<VaultEntryType> RATEABLE_TYPES = Set.of(
            VaultEntryType.FICTION,
            VaultEntryType.FILM,
            VaultEntryType.PERSON,
            VaultEntryType.IMAGE,
            VaultEntryType.ALBUM,
            VaultEntryType.BRAND,
            VaultEntryType.LOCATION,
            VaultEntryType.STUDY,
            VaultEntryType.INFORMATION,
            VaultEntryType.VOCABULARY,
            VaultEntryType.MUSIC,
            VaultEntryType.SHOPPING,
            VaultEntryType.SOFTWARE,
            VaultEntryType.NOTE,
            VaultEntryType.SAVED_RESOURCE,
            VaultEntryType.EXTERNAL_ACCOUNT
    );

    private static final Set<VaultEntryType> TAGGABLE_TYPES = Set.of(
            VaultEntryType.FICTION,
            VaultEntryType.FILM,
            VaultEntryType.PERSON,
            VaultEntryType.IMAGE,
            VaultEntryType.ALBUM,
            VaultEntryType.BRAND,
            VaultEntryType.LOCATION,
            VaultEntryType.STUDY,
            VaultEntryType.INFORMATION,
            VaultEntryType.VOCABULARY,
            VaultEntryType.MUSIC,
            VaultEntryType.SHOPPING,
            VaultEntryType.SOFTWARE,
            VaultEntryType.NOTE,
            VaultEntryType.SAVED_RESOURCE,
            VaultEntryType.EXTERNAL_ACCOUNT
    );

    private VaultCapabilityMatrix() {
    }

    public static boolean canFavorite(VaultEntryType type) {
        if (type == null) {
            throw new IllegalArgumentException("VaultEntryType must not be null");
        }
        return FAVORITEABLE_TYPES.contains(type);
    }

    public static boolean canRate(VaultEntryType type) {
        if (type == null) {
            throw new IllegalArgumentException("VaultEntryType must not be null");
        }
        return RATEABLE_TYPES.contains(type);
    }

    public static boolean canTag(VaultEntryType type) {
        if (type == null) {
            throw new IllegalArgumentException("VaultEntryType must not be null");
        }
        return TAGGABLE_TYPES.contains(type);
    }

    public static void assertCanFavorite(VaultEntryType type) {
        if (!canFavorite(type)) {
            throw new IllegalStateException("VaultEntryType " + type + " does not support favorites");
        }
    }

    public static void assertCanRate(VaultEntryType type) {
        if (!canRate(type)) {
            throw new IllegalStateException("VaultEntryType " + type + " does not support ratings");
        }
    }

    public static void assertCanTag(VaultEntryType type) {
        if (!canTag(type)) {
            throw new IllegalStateException("VaultEntryType " + type + " does not support tags");
        }
    }
}
