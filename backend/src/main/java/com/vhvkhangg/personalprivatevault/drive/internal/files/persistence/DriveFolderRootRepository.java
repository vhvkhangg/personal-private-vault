package com.vhvkhangg.personalprivatevault.drive.internal.files.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface DriveFolderRootRepository extends JpaRepository<DriveFolderRoot, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from DriveFolderRoot r where r.googleSub = :sub")
    Optional<DriveFolderRoot> lockBySubject(@Param("sub") String sub);

    @Modifying
    @Query(value = """
        INSERT INTO drive_folder_roots (google_sub,root_file_id,root_binding_id,status,binding_epoch,created_at,updated_at)
        VALUES (:sub,:file,:binding,'PENDING',:epoch,:now,:now) ON CONFLICT DO NOTHING
        """, nativeQuery = true)
    int insertPending(@Param("sub") String sub, @Param("file") String file, @Param("binding") UUID binding,
            @Param("epoch") UUID epoch, @Param("now") Instant now);

    @Modifying(clearAutomatically = true)
    @Query("update DriveFolderRoot r set r.status = :status, r.bindingEpoch = :next, r.updatedAt = :now where r.googleSub = :sub and r.bindingEpoch = :expected")
    int transition(@Param("sub") String sub, @Param("expected") UUID expected, @Param("next") UUID next,
            @Param("status") String status, @Param("now") Instant now);
}
