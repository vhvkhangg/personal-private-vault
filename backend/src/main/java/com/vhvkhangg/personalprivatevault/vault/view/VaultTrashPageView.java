package com.vhvkhangg.personalprivatevault.vault.view;

import java.util.List;

/**
 * Public read model for a bounded page of trashed vault entries.
 */
public record VaultTrashPageView(List<VaultEntryView> items, String nextCursor) {

    public VaultTrashPageView {
        items = items == null ? List.of() : List.copyOf(items);
    }
}
