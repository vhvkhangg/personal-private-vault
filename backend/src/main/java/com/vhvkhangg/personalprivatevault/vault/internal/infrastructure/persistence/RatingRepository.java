package com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.vault.internal.domain.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface RatingRepository extends JpaRepository<Rating, Long> {

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = """
            INSERT INTO ratings (vault_entry_id, grade, created_at, updated_at)
            VALUES (:vaultEntryId, CAST(:grade AS rating_grade), :now, :now)
            ON CONFLICT (vault_entry_id)
            DO UPDATE SET grade = EXCLUDED.grade, updated_at = EXCLUDED.updated_at
            """, nativeQuery = true)
    int upsert(
            @Param("vaultEntryId") Long vaultEntryId,
            @Param("grade") String grade,
            @Param("now") Instant now
    );
}
