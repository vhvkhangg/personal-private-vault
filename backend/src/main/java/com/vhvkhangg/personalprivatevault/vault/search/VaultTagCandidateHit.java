package com.vhvkhangg.personalprivatevault.vault.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

/**
 * Normalized tag-origin candidate hit produced by Vault tag search before feature document materialization.
 */
public record VaultTagCandidateHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        double similarity,
        String matchedTagName
) {}
