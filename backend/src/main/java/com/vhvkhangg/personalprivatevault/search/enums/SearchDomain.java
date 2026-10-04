package com.vhvkhangg.personalprivatevault.search.enums;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

/**
 * High-level business domains supported by global search.
 */
public enum SearchDomain {
    PEOPLE(Set.of(VaultEntryType.PERSON)),
    FICTION(Set.of(VaultEntryType.FICTION)),
    FILM(Set.of(VaultEntryType.FILM, VaultEntryType.FILM_CREDIT)),
    MEDIA(Set.of(VaultEntryType.ALBUM, VaultEntryType.IMAGE)),
    LOCATION(Set.of(VaultEntryType.BRAND, VaultEntryType.LOCATION)),
    KNOWLEDGE(Set.of(VaultEntryType.STUDY, VaultEntryType.INFORMATION, VaultEntryType.VOCABULARY, VaultEntryType.NOTE)),
    COLLECTION(Set.of(VaultEntryType.MUSIC, VaultEntryType.SHOPPING, VaultEntryType.SOFTWARE)),
    ACCOUNT(Set.of(VaultEntryType.EXTERNAL_ACCOUNT)),
    FEED(Set.of(VaultEntryType.SAVED_RESOURCE));

    private final Set<VaultEntryType> supportedEntryTypes;

    SearchDomain(Set<VaultEntryType> supportedEntryTypes) {
        this.supportedEntryTypes = Collections.unmodifiableSet(EnumSet.copyOf(supportedEntryTypes));
    }

    public Set<VaultEntryType> supportedEntryTypes() {
        return supportedEntryTypes;
    }

    public static Optional<SearchDomain> forEntryType(VaultEntryType entryType) {
        if (entryType == null) {
            return Optional.empty();
        }
        for (SearchDomain domain : values()) {
            if (domain.supportedEntryTypes.contains(entryType)) {
                return Optional.of(domain);
            }
        }
        return Optional.empty();
    }
}
