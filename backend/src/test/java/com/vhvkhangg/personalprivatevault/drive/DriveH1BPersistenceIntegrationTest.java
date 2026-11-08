package com.vhvkhangg.personalprivatevault.drive;

import com.vhvkhangg.personalprivatevault.drive.fixture.DriveH1BTestSupport;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderIntent;
import com.vhvkhangg.personalprivatevault.drive.internal.files.persistence.DriveChildFolderKey;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DriveH1BPersistenceIntegrationTest extends DriveH1BTestSupport {

    @Test
    @DisplayName("B-01: V27 schema, FK, unique, status, provider_folder_id and terminal-time constraints reject invalid data")
    void v27ConstraintsEnforceDatabaseIntegrity() {
        connect(1, "SUB_B01");
        ensure(1);
        var root = roots.findById("SUB_B01").orElseThrow();
        UUID rootBinding = root.getRootBindingId();
        UUID rootEpoch = root.getBindingEpoch();
        String validKey = key();
        Instant now = clock.instant();

        // 1. Valid insert succeeds
        int inserted = childFoldersRepo.insertPreparing(
                "SUB_B01", rootBinding, validKey, rootEpoch, UUID.randomUUID(),
                "SafeFolder", "0".repeat(64), now
        );
        assertThat(inserted).isEqualTo(1);

        var keyObj = new DriveChildFolderKey("SUB_B01", rootBinding, validKey);
        DriveChildFolderIntent intent = childFoldersRepo.findById(keyObj).orElseThrow();
        assertThat(intent.getStatus()).isEqualTo("PREPARING");
        assertThat(intent.getProviderFolderId()).isNull();
        assertThat(intent.getCompletedAt()).isNull();

        // 2. Duplicate idempotency key fails unique constraint
        assertThatThrownBy(() -> jdbc.update("""
            INSERT INTO drive_child_folder_intents (
                google_sub, root_binding_id, idempotency_key, root_binding_epoch, intent_epoch,
                safe_name, request_fingerprint, status, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, 'PREPARING', ?, ?)
            """, "SUB_B01", rootBinding, validKey, rootEpoch, UUID.randomUUID(), "Other", "0".repeat(64), Timestamp.from(now), Timestamp.from(now)))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 3. Foreign root fails FK constraint
        assertThatThrownBy(() -> childFoldersRepo.insertPreparing(
                "SUB_B01", UUID.randomUUID(), key(), rootEpoch, UUID.randomUUID(),
                "SafeFolder", "0".repeat(64), now
        )).isInstanceOf(DataIntegrityViolationException.class);

        // 4. Invalid status fails CHECK
        assertThatThrownBy(() -> jdbc.update("""
            INSERT INTO drive_child_folder_intents (
                google_sub, root_binding_id, idempotency_key, root_binding_epoch, intent_epoch,
                safe_name, request_fingerprint, status, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, 'BOGUS_STATUS', ?, ?)
            """, "SUB_B01", rootBinding, key(), rootEpoch, UUID.randomUUID(), "Safe", "0".repeat(64), Timestamp.from(now), Timestamp.from(now)))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 5. Invalid request_fingerprint fails CHECK
        assertThatThrownBy(() -> childFoldersRepo.insertPreparing(
                "SUB_B01", rootBinding, key(), rootEpoch, UUID.randomUUID(),
                "SafeFolder", "not-hex-64", now
        )).isInstanceOf(DataIntegrityViolationException.class);

        // 6. Empty safe_name fails CHECK
        assertThatThrownBy(() -> childFoldersRepo.insertPreparing(
                "SUB_B01", rootBinding, key(), rootEpoch, UUID.randomUUID(),
                "", "0".repeat(64), now
        )).isInstanceOf(DataIntegrityViolationException.class);

        // 7. PREPARING with non-null provider_folder_id fails CHECK
        assertThatThrownBy(() -> jdbc.update("""
            INSERT INTO drive_child_folder_intents (
                google_sub, root_binding_id, idempotency_key, root_binding_epoch, intent_epoch,
                safe_name, request_fingerprint, provider_folder_id, status, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, 'folder-1', 'PREPARING', ?, ?)
            """, "SUB_B01", rootBinding, key(), rootEpoch, UUID.randomUUID(), "Safe", "0".repeat(64), Timestamp.from(now), Timestamp.from(now)))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 8. RESERVED without provider_folder_id fails CHECK
        assertThatThrownBy(() -> jdbc.update("""
            INSERT INTO drive_child_folder_intents (
                google_sub, root_binding_id, idempotency_key, root_binding_epoch, intent_epoch,
                safe_name, request_fingerprint, provider_folder_id, status, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, NULL, 'RESERVED', ?, ?)
            """, "SUB_B01", rootBinding, key(), rootEpoch, UUID.randomUUID(), "Safe", "0".repeat(64), Timestamp.from(now), Timestamp.from(now)))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 9. COMPLETED without completed_at fails CHECK
        assertThatThrownBy(() -> jdbc.update("""
            INSERT INTO drive_child_folder_intents (
                google_sub, root_binding_id, idempotency_key, root_binding_epoch, intent_epoch,
                safe_name, request_fingerprint, provider_folder_id, status, created_at, updated_at, completed_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, 'folder-2', 'COMPLETED', ?, ?, NULL)
            """, "SUB_B01", rootBinding, key(), rootEpoch, UUID.randomUUID(), "Safe", "0".repeat(64), Timestamp.from(now), Timestamp.from(now)))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 10. PREPARING with completed_at fails CHECK
        assertThatThrownBy(() -> jdbc.update("""
            INSERT INTO drive_child_folder_intents (
                google_sub, root_binding_id, idempotency_key, root_binding_epoch, intent_epoch,
                safe_name, request_fingerprint, status, created_at, updated_at, completed_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, 'PREPARING', ?, ?, ?)
            """, "SUB_B01", rootBinding, key(), rootEpoch, UUID.randomUUID(), "Safe", "0".repeat(64), Timestamp.from(now), Timestamp.from(now), Timestamp.from(now)))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 11. Deleting root violates FK constraint
        assertThatThrownBy(() -> jdbc.update("DELETE FROM drive_folder_roots WHERE google_sub = 'SUB_B01'"))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 12. target_parent_id format CHECK on drive_upload_intents
        assertThatThrownBy(() -> jdbc.update("""
            INSERT INTO drive_upload_intents (
                google_sub, root_binding_id, idempotency_key, request_fingerprint, content_md5,
                size_bytes, safe_name, mime_type, status, intent_epoch, root_binding_epoch,
                created_at, updated_at, target_parent_id
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PREPARING', ?, ?, ?, ?, ?)
            """, "SUB_B01", rootBinding, key(), "0".repeat(64), "0".repeat(32), 0L, "f.bin", "text/plain",
                UUID.randomUUID(), rootEpoch, Timestamp.from(now), Timestamp.from(now), "bad parent with spaces!"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("B-01/B-04: Seeded V26 to V27 Flyway upgrade preserves pre-H1 intents/fingerprints and safely adds target_parent_id")
    void seededV26ToV27UpgradePreservesPreH1IntentsAndAddsParentAffinity() throws Exception {
        String baseUrl = POSTGRES.getJdbcUrl();
        String upgradeDb = "v26_upgrade_test";
        String upgradeUrl = baseUrl.substring(0, baseUrl.lastIndexOf('/')) + "/" + upgradeDb + "?loggerLevel=OFF";

        try (var rootConn = DriverManager.getConnection(baseUrl, POSTGRES.getUsername(), POSTGRES.getPassword());
             var stmt = rootConn.createStatement()) {
            stmt.execute("DROP DATABASE IF EXISTS " + upgradeDb + " WITH (FORCE)");
            stmt.execute("CREATE DATABASE " + upgradeDb);
        }

        try {
            var ds = new DriverManagerDataSource(upgradeUrl, POSTGRES.getUsername(), POSTGRES.getPassword());
            ds.setDriverClassName("org.postgresql.Driver");
            var upgradeJdbc = new JdbcTemplate(ds);

            // 1. Run migrations up to V26
            Flyway.configure()
                    .dataSource(ds)
                    .target("26")
                    .load()
                    .migrate();

            // 2. Seed V26 data: root binding and pre-H1 upload intent
            String sub = "SUB_UPGRADE_V26";
            UUID rootBinding = UUID.randomUUID();
            UUID rootEpoch = UUID.randomUUID();
            String rootFileId = "root-folder-v26";
            Instant seededTime = Instant.parse("2026-11-01T10:00:00Z");

            upgradeJdbc.update("""
                INSERT INTO drive_folder_roots (
                    google_sub, root_file_id, root_binding_id, status, binding_epoch, created_at, updated_at
                ) VALUES (?, ?, ?, 'READY', ?, ?, ?)
                """, sub, rootFileId, rootBinding, rootEpoch, Timestamp.from(seededTime), Timestamp.from(seededTime));

            String uploadKey = "v26-key-" + UUID.randomUUID();
            String fp = "a".repeat(64);
            String md5 = "b".repeat(32);
            UUID intentEpoch = UUID.randomUUID();

            upgradeJdbc.update("""
                INSERT INTO drive_upload_intents (
                    google_sub, root_binding_id, idempotency_key, request_fingerprint, content_md5,
                    size_bytes, safe_name, mime_type, status, intent_epoch, root_binding_epoch,
                    created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PREPARING', ?, ?, ?, ?)
                """, sub, rootBinding, uploadKey, fp, md5, 1024L, "pre-h1-doc.pdf", "application/pdf",
                    intentEpoch, rootEpoch, Timestamp.from(seededTime), Timestamp.from(seededTime));

            // 3. Migrate to V27
            Flyway.configure()
                    .dataSource(ds)
                    .target("27")
                    .load()
                    .migrate();

            // 4. Verify upgraded upload intent preserves all values and has target_parent_id IS NULL
            var row = upgradeJdbc.queryForMap(
                    "SELECT * FROM drive_upload_intents WHERE google_sub = ? AND idempotency_key = ?",
                    sub, uploadKey
            );
            assertThat(row.get("target_parent_id")).isNull();
            assertThat(row.get("request_fingerprint")).isEqualTo(fp);
            assertThat(row.get("content_md5")).isEqualTo(md5);
            assertThat(((Number) row.get("size_bytes")).longValue()).isEqualTo(1024L);
            assertThat(row.get("safe_name")).isEqualTo("pre-h1-doc.pdf");
            assertThat(row.get("mime_type")).isEqualTo("application/pdf");
            assertThat(row.get("status")).isEqualTo("PREPARING");
            assertThat(row.get("intent_epoch")).isEqualTo(intentEpoch);
            assertThat(row.get("root_binding_epoch")).isEqualTo(rootEpoch);

            // 5. Verify new V27 drive_child_folder_intents table functions properly
            String childKey = "child-v27-" + UUID.randomUUID();
            UUID childEpoch = UUID.randomUUID();
            int insertedChild = upgradeJdbc.update("""
                INSERT INTO drive_child_folder_intents (
                    google_sub, root_binding_id, idempotency_key, root_binding_epoch, intent_epoch,
                    safe_name, request_fingerprint, status, created_at, updated_at
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'PREPARING', ?, ?)
                """, sub, rootBinding, childKey, rootEpoch, childEpoch, "UpgradedChild", "c".repeat(64),
                    Timestamp.from(seededTime), Timestamp.from(seededTime));
            assertThat(insertedChild).isEqualTo(1);

            // 6. Verify target_parent_id constraint holds on new inserts in upgraded schema
            assertThatThrownBy(() -> upgradeJdbc.update("""
                INSERT INTO drive_upload_intents (
                    google_sub, root_binding_id, idempotency_key, request_fingerprint, content_md5,
                    size_bytes, safe_name, mime_type, status, intent_epoch, root_binding_epoch,
                    created_at, updated_at, target_parent_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PREPARING', ?, ?, ?, ?, ?)
                """, sub, rootBinding, "new-upload-invalid", "d".repeat(64), "e".repeat(32), 0L, "test.bin", "text/plain",
                    UUID.randomUUID(), rootEpoch, Timestamp.from(seededTime), Timestamp.from(seededTime), "bad parent id!"))
                    .isInstanceOf(DataIntegrityViolationException.class);

        } finally {
            try (var rootConn = DriverManager.getConnection(baseUrl, POSTGRES.getUsername(), POSTGRES.getPassword());
                 var stmt = rootConn.createStatement()) {
                stmt.execute("DROP DATABASE IF EXISTS " + upgradeDb + " WITH (FORCE)");
            }
        }
    }

    @Test
    @DisplayName("B-01: Application-write guards transitionFrom and transitionFromAny reject stale/terminal overwrites")
    void applicationWriteGuardsRejectStaleTransitions() {
        connect(1, "SUB_B01_GUARD");
        ensure(1);
        var root = roots.findById("SUB_B01_GUARD").orElseThrow();
        UUID rootBinding = root.getRootBindingId();
        UUID rootEpoch = root.getBindingEpoch();
        String k = key();
        UUID intentEpoch = UUID.randomUUID();
        Instant now = clock.instant();

        childFoldersRepo.insertPreparing("SUB_B01_GUARD", rootBinding, k, rootEpoch, intentEpoch, "FolderGuard", "0".repeat(64), now);
        var keyObj = new DriveChildFolderKey("SUB_B01_GUARD", rootBinding, k);

        // Transition from wrong status ("RESERVED" when currently "PREPARING") fails -> 0 rows updated
        int updated = childFoldersRepo.transitionFrom(keyObj, intentEpoch, "RESERVED", "DISPATCHED", now, null);
        assertThat(updated).isZero();

        // Transition with wrong intentEpoch fails -> 0 rows updated
        updated = childFoldersRepo.transitionFrom(keyObj, UUID.randomUUID(), "PREPARING", "RESERVED", now, null);
        assertThat(updated).isZero();

        // Reserve properly
        int reserved = childFoldersRepo.reserve(keyObj, intentEpoch, "folder-id-1", now);
        assertThat(reserved).isEqualTo(1);

        // Transition from RESERVED to DISPATCHED succeeds
        updated = childFoldersRepo.transitionFrom(keyObj, intentEpoch, "RESERVED", "DISPATCHED", now, null);
        assertThat(updated).isEqualTo(1);

        // Stale transitionFrom ("RESERVED" -> "DISPATCHED" again) fails -> 0 rows updated
        updated = childFoldersRepo.transitionFrom(keyObj, intentEpoch, "RESERVED", "DISPATCHED", now, null);
        assertThat(updated).isZero();

        // Complete from DISPATCHED succeeds
        updated = childFoldersRepo.transitionFromAny(keyObj, intentEpoch, List.of("DISPATCHED", "RECONCILIATION_REQUIRED"), "COMPLETED", now, now);
        assertThat(updated).isEqualTo(1);

        // Further transition from terminal COMPLETED fails
        updated = childFoldersRepo.transitionFromAny(keyObj, intentEpoch, List.of("DISPATCHED", "RECONCILIATION_REQUIRED"), "RECONCILIATION_REQUIRED", now, null);
        assertThat(updated).isZero();
    }
}
