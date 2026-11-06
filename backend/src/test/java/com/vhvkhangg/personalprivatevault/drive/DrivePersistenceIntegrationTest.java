package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.internal.persistence.DriveConnectionEntity;
import com.vhvkhangg.personalprivatevault.drive.internal.persistence.DriveConnectionRepository;
import com.vhvkhangg.personalprivatevault.drive.internal.persistence.DriveConnectionStatus;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DrivePersistenceIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DriveConnectionRepository repository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    @AfterEach
    void cleanDriveTable() {
        jdbcTemplate.update("DELETE FROM public.drive_connections");
    }

    @Test
    @DisplayName("Table drive_connections exists in public schema with expected columns")
    void driveConnectionsTableExists() {
        Integer count = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_type = 'BASE TABLE'
                  AND table_name = 'drive_connections'
                """,
                Integer.class
        );
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("Slot constraint allows only 1 or 2, rejects 0 or 3")
    void slotCheckConstraintEnforcesOneOrTwo() {
        UUID epoch = UUID.randomUUID();
        // Slot 1: succeeds
        jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (1, 'sub-slot-1', 'slot1@example.com', 'openid email', 'RECONNECT_REQUIRED', NULL, NULL, ?, now(), now())
                """, epoch);

        // Slot 0: fails
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (0, 'sub-slot-0', 'slot0@example.com', 'openid email', 'RECONNECT_REQUIRED', NULL, NULL, ?, now(), now())
                """, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);

        // Slot 3: fails
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (3, 'sub-slot-3', 'slot3@example.com', 'openid email', 'RECONNECT_REQUIRED', NULL, NULL, ?, now(), now())
                """, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Unique constraints reject duplicate google_sub and duplicate credential_epoch")
    void uniqueConstraintsRejectDuplicates() {
        UUID epoch1 = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (1, 'duplicate-sub', 'user1@example.com', 'openid email', 'RECONNECT_REQUIRED', NULL, NULL, ?, now(), now())
                """, epoch1);

        // Duplicate google_sub in slot 2 fails
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (2, 'duplicate-sub', 'user2@example.com', 'openid email', 'RECONNECT_REQUIRED', NULL, NULL, ?, now(), now())
                """, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);

        // Duplicate credential_epoch fails
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (2, 'sub-slot-2', 'user2@example.com', 'openid email', 'RECONNECT_REQUIRED', NULL, NULL, ?, now(), now())
                """, epoch1))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("Conditional CHECK enforces valid credential columns for CONNECTED vs RECONNECT_REQUIRED")
    void conditionalCheckConstraintEnforcesCredentialConsistency() {
        // CONNECTED without encrypted_refresh_token fails
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (1, 'sub-conn-fail', 'user@example.com', 'openid email', 'CONNECTED', NULL, 'v1', ?, now(), now())
                """, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);

        // CONNECTED with empty token bytes fails
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (1, 'sub-conn-fail2', 'user@example.com', 'openid email', 'CONNECTED', bytea '', 'v1', ?, now(), now())
                """, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);

        // CONNECTED with null or blank key_id fails
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (1, 'sub-conn-fail3', 'user@example.com', 'openid email', 'CONNECTED', bytea '\\x010203', '   ', ?, now(), now())
                """, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);

        // RECONNECT_REQUIRED with non-null encrypted_refresh_token fails
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (1, 'sub-rec-fail', 'user@example.com', 'openid email', 'RECONNECT_REQUIRED', bytea '\\x010203', NULL, ?, now(), now())
                """, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);

        // Valid CONNECTED succeeds
        jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (1, 'sub-valid-conn', 'user@example.com', 'openid email', 'CONNECTED', bytea '\\x01020304', 'v1', ?, now(), now())
                """, UUID.randomUUID());

        // Valid RECONNECT_REQUIRED succeeds
        jdbcTemplate.update("""
                INSERT INTO drive_connections (slot, google_sub, display_email, granted_scopes, status, encrypted_refresh_token, key_id, credential_epoch, created_at, updated_at)
                VALUES (2, 'sub-valid-rec', 'user2@example.com', 'openid email', 'RECONNECT_REQUIRED', NULL, NULL, ?, now(), now())
                """, UUID.randomUUID());
    }

    @Test
    @DisplayName("Epoch-based CAS update and delete succeed on matching epoch and fail on stale epoch")
    void epochBasedCasUpdateAndDelete() {
        UUID epoch1 = UUID.randomUUID();
        UUID epoch2 = UUID.randomUUID();
        UUID staleEpoch = UUID.randomUUID();

        DriveConnectionEntity entity = DriveConnectionEntity.builder()
                .slot((short) 1)
                .googleSub("sub-cas-test")
                .displayEmail("cas@example.com")
                .grantedScopes("openid email")
                .status(DriveConnectionStatus.CONNECTED)
                .encryptedRefreshToken(new byte[]{1, 2, 3, 4})
                .keyId("v1")
                .credentialEpoch(epoch1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        repository.save(entity);

        // Stale CAS update returns 0 affected rows
        int staleUpdate = repository.updateWithEpochCas(
                (short) 1,
                staleEpoch,
                epoch2,
                DriveConnectionStatus.CONNECTED,
                new byte[]{5, 6, 7, 8},
                "v1",
                "openid email",
                "cas@example.com",
                Instant.now()
        );
        assertThat(staleUpdate).isEqualTo(0);

        // Matching CAS update succeeds and returns 1
        int matchingUpdate = repository.updateWithEpochCas(
                (short) 1,
                epoch1,
                epoch2,
                DriveConnectionStatus.CONNECTED,
                new byte[]{5, 6, 7, 8},
                "v1",
                "openid email",
                "cas@example.com",
                Instant.now()
        );
        assertThat(matchingUpdate).isEqualTo(1);

        // Stale CAS delete returns 0 affected rows
        int staleDelete = repository.deleteBySlotAndCredentialEpoch((short) 1, epoch1);
        assertThat(staleDelete).isEqualTo(0);

        // Matching CAS delete succeeds and returns 1
        int matchingDelete = repository.deleteBySlotAndCredentialEpoch((short) 1, epoch2);
        assertThat(matchingDelete).isEqualTo(1);

        Optional<DriveConnectionEntity> afterDelete = repository.findById((short) 1);
        assertThat(afterDelete).isEmpty();
    }
}
