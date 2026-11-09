package com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence;

import com.vhvkhangg.personalprivatevault.authentication.internal.domain.GoogleLoginAttempt;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface GoogleLoginAttemptRepository extends JpaRepository<GoogleLoginAttempt, UUID> {

    Optional<GoogleLoginAttempt> findByStateHash(String stateHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM GoogleLoginAttempt a WHERE a.stateHash = :stateHash")
    Optional<GoogleLoginAttempt> findByStateHashForUpdate(@Param("stateHash") String stateHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM GoogleLoginAttempt a WHERE a.attemptId = :attemptId")
    Optional<GoogleLoginAttempt> findByIdForUpdate(@Param("attemptId") UUID attemptId);

    @Query("SELECT COUNT(a) FROM GoogleLoginAttempt a WHERE a.recordKind = 'LINK_PASSWORD_FAILURE' AND a.owner.id = :ownerId AND a.createdAt >= :since")
    long countRecentPasswordFailures(@Param("ownerId") Short ownerId, @Param("since") Instant since);

    @Query("SELECT COUNT(a) FROM GoogleLoginAttempt a WHERE a.recordKind = 'OIDC' AND a.status IN ('PENDING', 'PROCESSING') AND a.expiresAt > :now")
    long countActiveOidcFlows(@Param("now") Instant now);

    @Query("SELECT COUNT(a) FROM GoogleLoginAttempt a WHERE a.recordKind = 'OIDC' AND a.createdAt >= :since")
    long countRecentOidcStarts(@Param("since") Instant since);

    long countByRecordKind(String recordKind);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "UPDATE ppv_google_login_attempts SET status = 'EXPIRED', completed_at = expires_at, updated_at = :now, " +
            "pkce_nonce = NULL, pkce_ciphertext = NULL " +
            "WHERE status IN ('PENDING', 'PROCESSING') AND expires_at <= :now", nativeQuery = true)
    int expireOldAttempts(@Param("now") Instant now);

    @Modifying
    @org.springframework.transaction.annotation.Transactional
    @Query(value = "DELETE FROM ppv_google_login_attempts WHERE attempt_id IN (" +
            "SELECT a.attempt_id FROM ppv_google_login_attempts a " +
            "WHERE a.status IN ('COMPLETED', 'FAILED', 'EXPIRED') " +
            "AND a.completed_at < :cutoff " +
            "AND NOT (a.record_kind = 'LINK_PASSWORD_FAILURE' AND a.created_at >= :recentWindow) " +
            "AND NOT EXISTS (SELECT 1 FROM ppv_google_handoff_tickets t WHERE t.attempt_id = a.attempt_id) " +
            "LIMIT :limit)", nativeQuery = true)
    int pruneTerminalAttempts(@Param("cutoff") Instant cutoff, @Param("recentWindow") Instant recentWindow, @Param("limit") int limit);
}
