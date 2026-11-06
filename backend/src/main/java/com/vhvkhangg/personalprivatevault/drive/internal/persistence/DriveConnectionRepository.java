package com.vhvkhangg.personalprivatevault.drive.internal.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface DriveConnectionRepository extends JpaRepository<DriveConnectionEntity, Short> {

    Optional<DriveConnectionEntity> findByGoogleSub(String googleSub);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO drive_connections
            (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token,
             key_id, credential_epoch, created_at, updated_at)
        VALUES (:slot, :sub, :email, :scopes, :status, :ciphertext, :keyId, :epoch, :now, :now)
        ON CONFLICT DO NOTHING
        """, nativeQuery = true)
    int insertIfAbsent(@Param("slot") short slot, @Param("sub") String sub,
            @Param("email") String email, @Param("scopes") String scopes, @Param("status") String status,
            @Param("ciphertext") byte[] ciphertext, @Param("keyId") String keyId,
            @Param("epoch") UUID epoch, @Param("now") Instant now);

    @Modifying
    @Transactional
    @Query("""
        UPDATE DriveConnectionEntity d
        SET d.status = :status,
            d.encryptedRefreshToken = :encryptedRefreshToken,
            d.keyId = :keyId,
            d.credentialEpoch = :newEpoch,
            d.grantedScopes = :grantedScopes,
            d.displayEmail = :displayEmail,
            d.updatedAt = :updatedAt
        WHERE d.slot = :slot AND d.credentialEpoch = :expectedEpoch
    """)
    int updateWithEpochCas(
            @Param("slot") Short slot,
            @Param("expectedEpoch") UUID expectedEpoch,
            @Param("newEpoch") UUID newEpoch,
            @Param("status") DriveConnectionStatus status,
            @Param("encryptedRefreshToken") byte[] encryptedRefreshToken,
            @Param("keyId") String keyId,
            @Param("grantedScopes") String grantedScopes,
            @Param("displayEmail") String displayEmail,
            @Param("updatedAt") Instant updatedAt
    );

    @Modifying
    @Transactional
    @Query("""
        UPDATE DriveConnectionEntity d
        SET d.status = 'RECONNECT_REQUIRED',
            d.encryptedRefreshToken = NULL,
            d.keyId = NULL,
            d.credentialEpoch = :newEpoch,
            d.updatedAt = :updatedAt
        WHERE d.slot = :slot AND d.credentialEpoch = :expectedEpoch
    """)
    int downgradeToReconnectRequiredWithCas(
            @Param("slot") Short slot,
            @Param("expectedEpoch") UUID expectedEpoch,
            @Param("newEpoch") UUID newEpoch,
            @Param("updatedAt") Instant updatedAt
    );

    @Modifying
    @Transactional
    @Query("DELETE FROM DriveConnectionEntity d WHERE d.slot = :slot AND d.credentialEpoch = :expectedEpoch")
    int deleteBySlotAndCredentialEpoch(@Param("slot") Short slot, @Param("expectedEpoch") UUID expectedEpoch);
}
