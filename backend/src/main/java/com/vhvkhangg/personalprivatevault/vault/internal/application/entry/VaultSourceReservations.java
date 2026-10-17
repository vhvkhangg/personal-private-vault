package com.vhvkhangg.personalprivatevault.vault.internal.application.entry;

import com.vhvkhangg.personalprivatevault.vault.entry.SourceEditGuard;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceIndexReservations;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceVersion;
import com.vhvkhangg.personalprivatevault.vault.entry.SourceVersions;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.Objects;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.UUID;

/** Owns the narrow SQL metadata boundary, on the same connection as JPA owner commands. */
@Service
@Transactional(readOnly = true)
public class VaultSourceReservations implements SourceEditGuard, SourceIndexReservations {
    private final JdbcTemplate jdbc;

    public VaultSourceReservations(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public SourceVersions read(SortedSet<Long> ids) {
        return query(ids, null, false);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public SourceVersions lockForRead(SortedSet<Long> ids) {
        requireWritable();
        return query(ids, null, true);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void requireEditable(SortedSet<Long> ids) {
        mutate("SELECT public.vault_rag_require_editable(?)", ids, null);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void reserve(SortedSet<Long> ids, UUID reservation) {
        mutate("SELECT public.vault_rag_reserve(?, ?)", ids, Objects.requireNonNull(reservation));
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public SourceVersions lockReserved(SortedSet<Long> ids, UUID reservation) {
        requireWritable();
        return query(ids, Objects.requireNonNull(reservation), true);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void release(SortedSet<Long> ids, UUID reservation) {
        mutate("SELECT public.vault_rag_release(?, ?)", ids, Objects.requireNonNull(reservation));
    }

    private void mutate(String sql, SortedSet<Long> ids, UUID token) {
        requireWritable();
        validate(ids);
        jdbc.execute((ConnectionCallback<Void>) connection -> {
            var array = connection.createArrayOf("bigint", ids.toArray(Long[]::new));
            try (var statement = connection.prepareStatement(sql)) {
                statement.setArray(1, array);
                if (token != null) statement.setObject(2, token);
                statement.execute();
                return null;
            } finally {
                array.free();
            }
        });
    }

    private SourceVersions query(SortedSet<Long> ids, UUID token, boolean locked) {
        validate(ids);
        return jdbc.execute((ConnectionCallback<SourceVersions>) connection -> {
            var array = connection.createArrayOf("bigint", ids.toArray(Long[]::new));
            String sql = locked ? (token == null ? "SELECT * FROM public.vault_rag_lock_for_read(?)"
                    : "SELECT * FROM public.vault_rag_lock_reserved(?, ?)")
                    : "SELECT * FROM public.vault_rag_source_state WHERE source_id = ANY(?) ORDER BY source_id";
            try (var statement = connection.prepareStatement(sql)) {
                statement.setArray(1, array);
                if (locked && token != null) statement.setObject(2, token);
                Map<Long, SourceVersion> versions = new TreeMap<>();
                try (var result = statement.executeQuery()) {
                    while (result.next()) {
                        var value = new SourceVersion(result.getLong("source_id"), result.getString("source_type"),
                                result.getLong("source_version"), result.getLong("consent_reset_version"),
                                result.getBoolean("deleted"), result.getObject("reserved_by", UUID.class));
                        versions.put(value.sourceId(), value);
                    }
                }
                if (!versions.keySet().equals(ids)) throw new IllegalStateException("Source history unavailable");
                return new SourceVersions(versions);
            } finally {
                array.free();
            }
        });
    }

    private static void validate(SortedSet<Long> ids) {
        if (ids == null || ids.isEmpty() || ids.size() > 100 || ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new IllegalArgumentException("Invalid source set");
        }
    }

    private static void requireWritable() {
        if (!TransactionSynchronizationManager.isActualTransactionActive()
                || TransactionSynchronizationManager.isCurrentTransactionReadOnly()) {
            throw new IllegalStateException("Source reservation requires a writable caller transaction");
        }
    }
}
