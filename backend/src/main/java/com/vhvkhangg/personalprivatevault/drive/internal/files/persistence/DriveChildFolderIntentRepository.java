package com.vhvkhangg.personalprivatevault.drive.internal.files.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriveChildFolderIntentRepository extends JpaRepository<DriveChildFolderIntent, DriveChildFolderKey> {

    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO drive_child_folder_intents (
            google_sub, root_binding_id, idempotency_key, root_binding_epoch, intent_epoch,
            safe_name, request_fingerprint, provider_folder_id, status, created_at, updated_at, completed_at
        ) VALUES (
            :sub, :root, :key, :rootEpoch, :intentEpoch,
            :name, :fingerprint, NULL, 'PREPARING', :now, :now, NULL
        ) ON CONFLICT DO NOTHING
        """, nativeQuery = true)
    int insertPreparing(
            @Param("sub") String sub,
            @Param("root") UUID root,
            @Param("key") String key,
            @Param("rootEpoch") UUID rootEpoch,
            @Param("intentEpoch") UUID intentEpoch,
            @Param("name") String name,
            @Param("fingerprint") String fingerprint,
            @Param("now") Instant now
    );

    @Query("select count(i) from DriveChildFolderIntent i where i.id.googleSub = :sub and i.id.rootBindingId = :root")
    long countRetained(@Param("sub") String sub, @Param("root") UUID root);

    @Query("select count(i) from DriveChildFolderIntent i where i.id.googleSub = :sub and i.id.rootBindingId = :root and i.completedAt is null")
    long countNonterminal(@Param("sub") String sub, @Param("root") UUID root);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
        update DriveChildFolderIntent i
        set i.providerFolderId = :folderId, i.status = 'RESERVED', i.updatedAt = :now
        where i.id = :key and i.intentEpoch = :expected and i.status = 'PREPARING' and i.providerFolderId is null
        """)
    int reserve(
            @Param("key") DriveChildFolderKey key,
            @Param("expected") UUID expected,
            @Param("folderId") String folderId,
            @Param("now") Instant now
    );

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
        update DriveChildFolderIntent i
        set i.status = :status, i.updatedAt = :now, i.completedAt = :completed
        where i.id = :key and i.intentEpoch = :expected and i.status = :expectedStatus
        """)
    int transitionFrom(
            @Param("key") DriveChildFolderKey key,
            @Param("expected") UUID expected,
            @Param("expectedStatus") String expectedStatus,
            @Param("status") String status,
            @Param("now") Instant now,
            @Param("completed") Instant completed
    );

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
        update DriveChildFolderIntent i
        set i.status = :status, i.updatedAt = :now, i.completedAt = :completed
        where i.id = :key and i.intentEpoch = :expected and i.status in (:expectedStatuses)
        """)
    int transitionFromAny(
            @Param("key") DriveChildFolderKey key,
            @Param("expected") UUID expected,
            @Param("expectedStatuses") List<String> expectedStatuses,
            @Param("status") String status,
            @Param("now") Instant now,
            @Param("completed") Instant completed
    );

    @Query("""
        select i from DriveChildFolderIntent i
        where i.id.googleSub = :sub and i.id.rootBindingId = :root and i.providerFolderId = :folderId
        """)
    Optional<DriveChildFolderIntent> findBySubAndRootAndProviderFolderId(
            @Param("sub") String sub,
            @Param("root") UUID root,
            @Param("folderId") String folderId
    );

    @Query("""
        select i from DriveChildFolderIntent i
        where i.id.googleSub = :sub and i.id.rootBindingId = :root and i.status = 'COMPLETED'
        """)
    List<DriveChildFolderIntent> findCompletedBySubAndRoot(
            @Param("sub") String sub,
            @Param("root") UUID root
    );

    Optional<DriveChildFolderIntent> findByProviderFolderId(String providerFolderId);
}
