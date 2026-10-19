package com.vhvkhangg.personalprivatevault.rag.internal.application;

import com.vhvkhangg.personalprivatevault.rag.internal.domain.RagExternalFileSource;
import com.vhvkhangg.personalprivatevault.rag.internal.infrastructure.persistence.RagExternalFileSourceRepository;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Manages RAG-owned external file source authority, versioning, and reservations (ADR-0021 Clause 2, 3).
 * External file identities are NEVER coerced to Vault IDs.
 */
@Component
public class RagExternalFileAuthority {

    private final RagExternalFileSourceRepository repository;
    private final EntityManager entityManager;

    public RagExternalFileAuthority(RagExternalFileSourceRepository repository, EntityManager entityManager) {
        this.repository = Objects.requireNonNull(repository, "repository must not be null");
        this.entityManager = Objects.requireNonNull(entityManager, "entityManager must not be null");
    }

    public void requireEditable(Collection<UUID> ids) {
        if (ids == null || ids.isEmpty()) return;
        UUID[] sortedArray = ids.stream().sorted().toArray(UUID[]::new);
        entityManager.createNativeQuery("SELECT rag.external_file_require_editable(:ids)")
                .setParameter("ids", sortedArray)
                .getSingleResult();
    }

    public void reserve(Collection<UUID> ids, UUID token) {
        if (ids == null || ids.isEmpty()) return;
        UUID[] sortedArray = ids.stream().sorted().toArray(UUID[]::new);
        entityManager.createNativeQuery("SELECT rag.external_file_reserve(:ids, :token)")
                .setParameter("ids", sortedArray)
                .setParameter("token", token)
                .getSingleResult();
    }

    public void release(Collection<UUID> ids, UUID token) {
        if (ids == null || ids.isEmpty()) return;
        UUID[] sortedArray = ids.stream().sorted().toArray(UUID[]::new);
        entityManager.createNativeQuery("SELECT rag.external_file_release(:ids, :token)")
                .setParameter("ids", sortedArray)
                .setParameter("token", token)
                .getSingleResult();
    }

    public List<RagExternalFileSource> lockReserved(Collection<UUID> ids, UUID token) {
        if (ids == null || ids.isEmpty()) return List.of();
        UUID[] sortedArray = ids.stream().sorted().toArray(UUID[]::new);
        entityManager.createNativeQuery("SELECT rag.external_file_lock_reserved(:ids, :token)")
                .setParameter("ids", sortedArray)
                .setParameter("token", token)
                .getResultList();
        return repository.findAllByFileSourceIdInSorted(List.of(sortedArray));
    }

    public RagExternalFileSource registerOrGet(UUID fileSourceId, String canonicalDigest, Instant now) {
        return repository.findByFileSourceId(fileSourceId).orElseGet(() -> {
            RagExternalFileSource created = new RagExternalFileSource(
                    fileSourceId, canonicalDigest, 1L, 1L, 1L, 1, true, now
            );
            return repository.save(created);
        });
    }

    public RagExternalFileSource findByFileSourceId(UUID fileSourceId) {
        return repository.findByFileSourceId(fileSourceId).orElse(null);
    }
}
