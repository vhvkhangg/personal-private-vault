package com.vhvkhangg.personalprivatevault.knowledge.note.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record NoteSearchHit(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText,
        String snippet,
        int rankBucket,
        double similarity,
        String matchKind
) {}
