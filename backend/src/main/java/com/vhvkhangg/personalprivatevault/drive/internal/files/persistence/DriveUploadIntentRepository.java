package com.vhvkhangg.personalprivatevault.drive.internal.files.persistence;

import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.UUID;

public interface DriveUploadIntentRepository extends JpaRepository<DriveUploadIntent, DriveUploadKey> {
    @Modifying
    @Query(value="""
        INSERT INTO drive_upload_intents (google_sub,root_binding_id,idempotency_key,request_fingerprint,content_md5,
            size_bytes,safe_name,mime_type,status,intent_epoch,root_binding_epoch,created_at,updated_at,target_parent_id)
        VALUES (:sub,:root,:key,:fingerprint,:md5,:size,:name,:mime,'PREPARING',:epoch,:rootEpoch,:now,:now,:targetParent)
        ON CONFLICT DO NOTHING
        """,nativeQuery=true)
    int insertPreparing(@Param("sub") String sub,@Param("root") UUID root,@Param("key") String key,
            @Param("fingerprint") String fingerprint,@Param("md5") String md5,@Param("size") long size,
            @Param("name") String name,@Param("mime") String mime,@Param("epoch") UUID epoch,
            @Param("rootEpoch") UUID rootEpoch,@Param("now") Instant now,@Param("targetParent") String targetParent);

    @Query("select count(i) from DriveUploadIntent i where i.id.googleSub = :sub and i.id.rootBindingId = :root")
    long countRetained(@Param("sub") String sub,@Param("root") UUID root);
    @Query("select count(i) from DriveUploadIntent i where i.id.googleSub = :sub and i.id.rootBindingId = :root and i.completedAt is null")
    long countNonterminal(@Param("sub") String sub,@Param("root") UUID root);

    @Modifying(clearAutomatically=true)
    @Query(value="""
        DELETE FROM drive_upload_intents WHERE idempotency_key IN (
            SELECT idempotency_key FROM drive_upload_intents WHERE google_sub=:sub AND root_binding_id=:root
            AND status IN ('COMPLETED','FAILED_DEFINITIVE') AND completed_at < :cutoff AND idempotency_key NOT IN (:protectedKeys)
            ORDER BY completed_at,idempotency_key LIMIT 32)
        """,nativeQuery=true)
    int pruneTerminal(@Param("sub") String sub,@Param("root") UUID root,@Param("cutoff") Instant cutoff,@Param("protectedKeys") java.util.List<String> protectedKeys);

    @Modifying(clearAutomatically=true)
    @Query("""
        update DriveUploadIntent i set i.intentEpoch = :next, i.rootBindingEpoch = :rootEpoch, i.updatedAt = :now
        where i.id = :key and i.intentEpoch = :expected
        """)
    int claim(@Param("key") DriveUploadKey key,@Param("expected") UUID expected,@Param("next") UUID next,
            @Param("rootEpoch") UUID rootEpoch,@Param("now") Instant now);

    @Modifying(clearAutomatically=true)
    @Query("""
        update DriveUploadIntent i set i.providerFileId=:file, i.status='RESERVED',i.intentEpoch=:next,i.updatedAt=:now
        where i.id=:key and i.intentEpoch=:expected and i.status='PREPARING' and i.providerFileId is null
        """)
    int reserve(@Param("key") DriveUploadKey key,@Param("expected") UUID expected,@Param("next") UUID next,
            @Param("file") String file,@Param("now") Instant now);

    @Modifying(clearAutomatically=true)
    @Query("""
        update DriveUploadIntent i set i.status=:status,i.intentEpoch=:next,i.updatedAt=:now,i.completedAt=:completed
        where i.id=:key and i.intentEpoch=:expected
        """)
    int transition(@Param("key") DriveUploadKey key,@Param("expected") UUID expected,@Param("next") UUID next,
            @Param("status") String status,@Param("now") Instant now,@Param("completed") Instant completed);
}
