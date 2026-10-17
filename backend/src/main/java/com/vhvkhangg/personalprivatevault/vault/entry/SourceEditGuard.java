package com.vhvkhangg.personalprivatevault.vault.entry;

import java.util.SortedSet;

/** Same-transaction, guard-before-mutation busy check for participating owners. */
public interface SourceEditGuard {
    void requireEditable(SortedSet<Long> ids);
}
