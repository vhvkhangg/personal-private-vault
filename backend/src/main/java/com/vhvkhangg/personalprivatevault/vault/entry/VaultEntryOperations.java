package com.vhvkhangg.personalprivatevault.vault.entry;

import com.vhvkhangg.personalprivatevault.vault.enums.VaultEntryType;
import com.vhvkhangg.personalprivatevault.vault.view.VaultEntryView;

import java.util.Optional;

/** Public synchronous API for shared vault identity and recycle-bin operations. */
public interface VaultEntryOperations {
    VaultEntryView create(VaultEntryType entryType);
    Optional<VaultEntryView> find(Long vaultEntryId);
    VaultEntryView moveToTrash(Long vaultEntryId);
    VaultEntryView restore(Long vaultEntryId);
}
