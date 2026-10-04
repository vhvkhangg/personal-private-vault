package com.vhvkhangg.personalprivatevault.knowledge.note.search;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;

public record NoteSearchDocument(
        Long vaultEntryId,
        VaultEntryType entryType,
        String primaryText,
        String secondaryText
) {}
