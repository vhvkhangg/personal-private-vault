package com.vhvkhangg.personalprivatevault.vault.entry;

import java.util.SortedSet;
import java.util.UUID;

/** Vault metadata authority. Reserve/lock/release must join the caller's writable TX. */
public interface SourceIndexReservations {
    SourceVersions read(SortedSet<Long> ids);
    SourceVersions lockForRead(SortedSet<Long> ids);
    void reserve(SortedSet<Long> ids, UUID reservation);
    SourceVersions lockReserved(SortedSet<Long> ids, UUID reservation);
    void release(SortedSet<Long> ids, UUID reservation);
}
