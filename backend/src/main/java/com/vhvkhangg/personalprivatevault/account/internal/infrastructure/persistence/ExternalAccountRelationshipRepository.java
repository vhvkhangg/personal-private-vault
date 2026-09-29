package com.vhvkhangg.personalprivatevault.account.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.account.internal.domain.ExternalAccountRelationship;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExternalAccountRelationshipRepository extends JpaRepository<ExternalAccountRelationship, Long> {

    Optional<ExternalAccountRelationship> findByOwnerAccountIdAndTargetAccountId(
            Long ownerAccountId,
            Long targetAccountId
    );

    @Query("SELECT r FROM ExternalAccountRelationship r WHERE r.ownerAccountId = :ownerAccountId ORDER BY r.updatedAt DESC, r.id DESC")
    List<ExternalAccountRelationship> findRecentByOwnerAccountId(
            @Param("ownerAccountId") Long ownerAccountId,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(nativeQuery = true, value = """
        INSERT INTO external_account_relationships (
            owner_account_id,
            target_account_id,
            follower_status,
            source,
            follow_status,
            has_liked_post,
            note,
            created_at,
            updated_at
        ) VALUES (
            :ownerAccountId,
            :targetAccountId,
            CASE WHEN :followerStatus IS NOT NULL THEN (:followerStatus)::follower_status ELSE NULL END,
            (:source)::relationship_source,
            (:followStatus)::follow_status,
            :hasLikedPost,
            :note,
            now(),
            now()
        )
        ON CONFLICT (owner_account_id, target_account_id) DO UPDATE SET
            follower_status = EXCLUDED.follower_status,
            follow_status = EXCLUDED.follow_status,
            has_liked_post = EXCLUDED.has_liked_post,
            note = EXCLUDED.note,
            updated_at = CASE
                WHEN (
                    external_account_relationships.follower_status IS DISTINCT FROM EXCLUDED.follower_status
                    OR external_account_relationships.follow_status IS DISTINCT FROM EXCLUDED.follow_status
                    OR external_account_relationships.has_liked_post IS DISTINCT FROM EXCLUDED.has_liked_post
                    OR external_account_relationships.note IS DISTINCT FROM EXCLUDED.note
                ) THEN now()
                ELSE external_account_relationships.updated_at
            END
    """)
    void upsert(
            @Param("ownerAccountId") Long ownerAccountId,
            @Param("targetAccountId") Long targetAccountId,
            @Param("followerStatus") String followerStatus,
            @Param("source") String source,
            @Param("followStatus") String followStatus,
            @Param("hasLikedPost") boolean hasLikedPost,
            @Param("note") String note
    );
}
