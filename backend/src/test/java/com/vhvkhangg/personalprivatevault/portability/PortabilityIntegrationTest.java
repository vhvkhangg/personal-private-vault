package com.vhvkhangg.personalprivatevault.portability;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vhvkhangg.personalprivatevault.ApiExceptionHandler;
import com.vhvkhangg.personalprivatevault.portability.internal.application.PortabilityExportService;
import com.vhvkhangg.personalprivatevault.portability.internal.infrastructure.snapshot.PortabilitySnapshotAdapter;
import com.vhvkhangg.personalprivatevault.portability.internal.web.controller.PortabilityController;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.jdbc.datasource.DelegatingDataSource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PortabilityIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PortabilitySnapshotAdapter snapshotAdapter;

    @Test
    @DisplayName("POST /api/v1/portability/exports returns complete, valid, lossless ZIP archive with all 67 tables, JSONB precision/boundaries, and fractional time")
    void exportArchiveProducesCompleteLosslessZip() throws Exception {
        // 1. Seed sample data across markdown, media, domain tables, JSONB precision, boundary numbers, and fractional time
        long noteId = 9101L;
        String noteContent = "# My Private Note\nLossless portable export verification.";
        jdbcTemplate.update("""
                INSERT INTO vault_entries (id, entry_type)
                VALUES (?, 'NOTE'::vault_entry_type)
                ON CONFLICT (id) DO NOTHING
                """, noteId);
        jdbcTemplate.update("""
                INSERT INTO notes (id, title, content_markdown)
                VALUES (?, 'Test Note', ?)
                ON CONFLICT (id) DO NOTHING
                """, noteId, noteContent);

        long diaryId = 9102L;
        String diaryContent = "Dear Diary, today we verified Phase 14 portability.";
        jdbcTemplate.update("""
                INSERT INTO diary_entries (id, entry_date, title, content_markdown)
                VALUES (?, '2026-10-06'::date, 'Phase 14 Diary', ?)
                ON CONFLICT (id) DO NOTHING
                """, diaryId, diaryContent);

        long imageId = 9103L;
        jdbcTemplate.update("""
                INSERT INTO vault_entries (id, entry_type)
                VALUES (?, 'IMAGE'::vault_entry_type)
                ON CONFLICT (id) DO NOTHING
                """, imageId);
        jdbcTemplate.update("""
                INSERT INTO images (id, title, image_type, object_key, size_bytes, mime_type, checksum_sha256)
                VALUES (?, 'Vault Image', 'PHOTO', 'images/managed/test-image.jpg', 2048, 'image/jpeg', 'abcd1234efgh5678')
                ON CONFLICT (id) DO NOTHING
                """, imageId);

        // Seed JSONB high-precision floating point number
        long importJobId = 9104L;
        long importJobItemId = 9105L;
        jdbcTemplate.update("""
                INSERT INTO import_jobs (id, target_type, format, original_file_name)
                VALUES (?, 'NOTE'::import_target_type, 'JSON'::import_format, 'test-precision.json')
                ON CONFLICT (id) DO NOTHING
                """, importJobId);
        jdbcTemplate.update("""
                INSERT INTO import_job_items (id, import_job_id, item_index, parsed_payload, status)
                VALUES (?, ?, 0, '{"n":1234567890.1234567890123456789}'::jsonb, 'VALID'::import_item_status)
                ON CONFLICT (id) DO NOTHING
                """, importJobItemId, importJobId);

        // Seed JSONB parser-boundary number (1e1100 -> 1101 digits in PostgreSQL)
        long boundaryJobId = 9109L;
        long boundaryItemId = 9110L;
        jdbcTemplate.update("""
                INSERT INTO import_jobs (id, target_type, format, original_file_name)
                VALUES (?, 'NOTE'::import_target_type, 'JSON'::import_format, 'test-boundary.json')
                ON CONFLICT (id) DO NOTHING
                """, boundaryJobId);
        jdbcTemplate.update("""
                INSERT INTO import_job_items (id, import_job_id, item_index, parsed_payload, status)
                VALUES (?, ?, 0, '{"n":1e1100}'::jsonb, 'VALID'::import_item_status)
                ON CONFLICT (id) DO NOTHING
                """, boundaryItemId, boundaryJobId);

        // Seed fractional microsecond time
        long addressId = 9106L;
        long locationId = 9107L;
        long businessHoursId = 9108L;
        jdbcTemplate.update("""
                INSERT INTO addresses (id, country_code)
                VALUES (?, 'US')
                ON CONFLICT (id) DO NOTHING
                """, addressId);
        jdbcTemplate.update("""
                INSERT INTO vault_entries (id, entry_type)
                VALUES (?, 'LOCATION'::vault_entry_type)
                ON CONFLICT (id) DO NOTHING
                """, locationId);
        jdbcTemplate.update("""
                INSERT INTO locations (id, name, address_id)
                VALUES (?, 'Test Hours Location', ?)
                ON CONFLICT (id) DO NOTHING
                """, locationId, addressId);
        jdbcTemplate.update("""
                INSERT INTO location_business_hours (id, location_id, day_of_week, sequence, open_time, close_time)
                VALUES (?, ?, 'MONDAY'::day_of_week, 1, '12:34:56.123456'::time, '23:45:01.654321'::time)
                ON CONFLICT (id) DO NOTHING
                """, businessHoursId, locationId);

        // 2. Perform export request with bearer token
        byte[] zipBytes = mockMvc.perform(post("/api/v1/portability/exports")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, "application/zip"))
                .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "no-store"))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.startsWith("attachment; filename=\"personal-private-vault-export-v1-")))
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        assertThat(zipBytes).isNotEmpty();

        // 3. Inspect ZIP entries
        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.put(entry.getName(), zis.readAllBytes());
                zis.closeEntry();
            }
        }

        // 4. Assert manifest.json contents
        assertThat(entries).containsKey("manifest.json");
        JsonNode manifestNode = objectMapper.readTree(entries.get("manifest.json"));
        assertThat(manifestNode.path("formatVersion").asInt()).isEqualTo(1);
        assertThat(manifestNode.hasNonNull("generatedAt")).isTrue();
        assertThat(manifestNode.path("highestFlywayVersion").asText()).isEqualTo("2");

        JsonNode excludedNode = manifestNode.path("excludedSecurityTables");
        assertThat(excludedNode.isArray()).isTrue();
        List<String> excludedList = new ArrayList<>();
        excludedNode.forEach(n -> excludedList.add(n.asText()));
        assertThat(excludedList).containsExactlyInAnyOrder("app_users", "refresh_tokens");

        JsonNode inventoryNode = manifestNode.path("tableInventory");
        assertThat(inventoryNode.isArray()).isTrue();
        assertThat(inventoryNode.size()).isEqualTo(67);

        // 5. Assert all 67 tables exist under tables/ and excluded security tables do not exist
        for (String table : PortabilitySnapshotAdapter.ALLOWED_TABLES) {
            assertThat(entries).containsKey("tables/" + table + ".jsonl");
        }
        assertThat(entries).doesNotContainKey("tables/app_users.jsonl");
        assertThat(entries).doesNotContainKey("tables/refresh_tokens.jsonl");
        assertThat(entries).doesNotContainKey("tables/flyway_schema_history.jsonl");

        // 6. Assert markdown files are exact copies
        String noteEntryName = "markdown/knowledge/notes/" + noteId + ".md";
        assertThat(entries).containsKey(noteEntryName);
        assertThat(new String(entries.get(noteEntryName), StandardCharsets.UTF_8)).isEqualTo(noteContent);

        String diaryEntryName = "markdown/journal/diary-entries/" + diaryId + ".md";
        assertThat(entries).containsKey(diaryEntryName);
        assertThat(new String(entries.get(diaryEntryName), StandardCharsets.UTF_8)).isEqualTo(diaryContent);

        // 7. Assert media/manifest.jsonl contents
        assertThat(entries).containsKey("media/manifest.jsonl");
        String mediaManifestContent = new String(entries.get("media/manifest.jsonl"), StandardCharsets.UTF_8);
        assertThat(mediaManifestContent).contains("\"object_key\":\"images/managed/test-image.jpg\"");
        assertThat(mediaManifestContent).contains("\"checksum_sha256\":\"abcd1234efgh5678\"");
        assertThat(mediaManifestContent).contains("\"size_bytes\":2048");

        // 8. Assert JSONB high-precision decimal and parser-boundary fidelity (FR14-3)
        assertThat(entries).containsKey("tables/import_job_items.jsonl");
        String importJobItemsContent = new String(entries.get("tables/import_job_items.jsonl"), StandardCharsets.UTF_8);
        assertThat(importJobItemsContent)
                .as("JSONB numeric precision must be preserved verbatim without scientific notation")
                .contains("1234567890.1234567890123456789")
                .doesNotContain("1.2345678901234567E9");

        assertThat(importJobItemsContent)
                .as("JSONB parser boundary value (1e1100) must export as raw JSON object, never coerced to quoted string")
                .contains("\"parsed_payload\":{\"n\":")
                .doesNotContain("\"parsed_payload\":\"{\\\"n\\\":");

        // 9. Assert PostgreSQL fractional microsecond time fidelity without truncation
        assertThat(entries).containsKey("tables/location_business_hours.jsonl");
        String locationHoursContent = new String(entries.get("tables/location_business_hours.jsonl"), StandardCharsets.UTF_8);
        assertThat(locationHoursContent)
                .as("PostgreSQL fractional microsecond time must be preserved verbatim")
                .contains("\"12:34:56.123456\"")
                .contains("\"23:45:01.654321\"");
    }

    @Test
    @DisplayName("Pre-commit export failure without authentication returns canonical JSON error")
    void unauthenticatedExportReturnsCanonicalJsonError() throws Exception {
        mockMvc.perform(post("/api/v1/portability/exports"))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, org.hamcrest.Matchers.containsString("application/json")))
                .andExpect(jsonPath("$.error.code").value("AUTHENTICATION_REQUIRED"))
                .andExpect(jsonPath("$.error.message").isNotEmpty());
    }

    @Test
    @DisplayName("Pre-commit export opening failure returns HTTP 500 canonical JSON error")
    void preCommitExportOpenFailureReturnsCanonicalJsonError() throws Exception {
        PortabilityExportService exportService = mock(PortabilityExportService.class);
        Path unreadableDir = Files.createTempDirectory("ppv-test-dir-not-archive-");
        when(exportService.createExportArchive()).thenReturn(unreadableDir);

        MockMvc standaloneMvc = MockMvcBuilders.standaloneSetup(new PortabilityController(exportService))
                .setControllerAdvice(new ApiExceptionHandler())
                .build();

        try {
            MvcResult export = standaloneMvc.perform(post("/api/v1/portability/exports")).andReturn();
            assertThat(export.getResponse().getStatus()).isEqualTo(500);
            assertThat(export.getResponse().getContentType()).contains("application/json");
            assertThat(export.getResponse().getContentAsString()).contains("\"code\":\"INTERNAL_ERROR\"");
        } finally {
            Files.deleteIfExists(unreadableDir);
        }
    }

    @Test
    @DisplayName("Schema guard rejects unapproved table in physical public schema")
    void schemaGuardRejectsUnapprovedTable() throws Exception {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE public.unapproved_test_table (id bigint PRIMARY KEY)");
            try {
                assertThatThrownBy(() -> snapshotAdapter.validateSchemaAllowlist(conn))
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("Unapproved table in public schema: unapproved_test_table");
            } finally {
                stmt.execute("DROP TABLE IF EXISTS public.unapproved_test_table");
            }
        }
    }

    @Test
    @DisplayName("Snapshot consistency preserves repeatable-read snapshot during concurrent committed writes (FR14-4)")
    void snapshotConsistencyPreservedDuringConcurrentWrites() throws Exception {
        long initialNoteId = 99881L;
        long concurrentNoteId = 99882L;

        jdbcTemplate.update("""
                INSERT INTO vault_entries (id, entry_type) VALUES (?, 'NOTE'::vault_entry_type) ON CONFLICT DO NOTHING
                """, initialNoteId);
        jdbcTemplate.update("""
                INSERT INTO notes (id, title, content_markdown) VALUES (?, 'Initial Note', 'Initial Markdown Content') ON CONFLICT DO NOTHING
                """, initialNoteId);

        Path tempZip = Files.createTempFile("ppv-snapshot-concurrency-", ".zip");
        try {
            // Synchronize after the adapter establishes its snapshot in PostgreSQL REPEATABLE READ:
            snapshotAdapter.exportSnapshot(tempZip, () -> {
                // Concurrently commit a new note while the snapshot is established:
                jdbcTemplate.update("""
                        INSERT INTO vault_entries (id, entry_type) VALUES (?, 'NOTE'::vault_entry_type) ON CONFLICT DO NOTHING
                        """, concurrentNoteId);
                jdbcTemplate.update("""
                        INSERT INTO notes (id, title, content_markdown) VALUES (?, 'Concurrent Note', 'Concurrent Markdown Content') ON CONFLICT DO NOTHING
                        """, concurrentNoteId);
            });

            // Inspect the exported archive:
            Map<String, byte[]> entries = new HashMap<>();
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(tempZip))) {
                ZipEntry entry;
                while ((entry = zis.getNextEntry()) != null) {
                    entries.put(entry.getName(), zis.readAllBytes());
                    zis.closeEntry();
                }
            }

            // The archive MUST contain initialNoteId, and MUST NOT contain concurrentNoteId
            String notesJsonl = new String(entries.get("tables/notes.jsonl"), StandardCharsets.UTF_8);
            assertThat(notesJsonl).contains("\"id\":" + initialNoteId);
            assertThat(notesJsonl)
                    .as("Archive must NOT contain note committed after snapshot establishment")
                    .doesNotContain("\"id\":" + concurrentNoteId);

            assertThat(entries).containsKey("markdown/knowledge/notes/" + initialNoteId + ".md");
            assertThat(entries)
                    .as("Markdown directory must NOT contain note committed after snapshot establishment")
                    .doesNotContainKey("markdown/knowledge/notes/" + concurrentNoteId + ".md");

            // Fresh query to database confirms the concurrent note is committed in PostgreSQL
            Integer visibleCount = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM notes WHERE id = ?",
                    Integer.class,
                    concurrentNoteId
            );
            assertThat(visibleCount).isEqualTo(1);
        } finally {
            Files.deleteIfExists(tempZip);
        }
    }

    @Test
    @DisplayName("Export archive preserves trashed vault entries and historical follower snapshots")
    void exportArchivePreservesTrashedAndHistoryRecords() throws Exception {
        // Seed a trashed vault entry (deleted_at IS NOT NULL)
        long trashedNoteId = 9301L;
        jdbcTemplate.update("""
                INSERT INTO vault_entries (id, entry_type, deleted_at)
                VALUES (?, 'NOTE'::vault_entry_type, CURRENT_TIMESTAMP)
                ON CONFLICT (id) DO UPDATE SET deleted_at = CURRENT_TIMESTAMP
                """, trashedNoteId);
        jdbcTemplate.update("""
                INSERT INTO notes (id, title, content_markdown)
                VALUES (?, 'Trashed Test Note', '# Content of trashed note')
                ON CONFLICT (id) DO NOTHING
                """, trashedNoteId);

        // Seed an active account and historical follower snapshot
        Long platformId = jdbcTemplate.query(
                "SELECT id FROM platforms WHERE name = 'Twitter'",
                (rs, rowNum) -> rs.getLong("id")
        ).stream().findFirst().orElseGet(() -> jdbcTemplate.queryForObject(
                "INSERT INTO platforms (name, kind, url) VALUES ('Twitter', 'SOCIAL'::platform_kind, 'https://twitter.com') RETURNING id",
                Long.class
        ));

        long ownerAccountId = 9302L;
        long targetAccountId = 9303L;
        jdbcTemplate.update("INSERT INTO vault_entries (id, entry_type) VALUES (?, 'EXTERNAL_ACCOUNT'::vault_entry_type) ON CONFLICT DO NOTHING", ownerAccountId);
        jdbcTemplate.update("INSERT INTO external_accounts (id, platform_id, ownership, account_type, username) VALUES (?, ?, 'OWNED'::external_account_ownership, 'SOCIAL'::external_account_type, 'owner_user') ON CONFLICT DO NOTHING", ownerAccountId, platformId);
        jdbcTemplate.update("INSERT INTO vault_entries (id, entry_type) VALUES (?, 'EXTERNAL_ACCOUNT'::vault_entry_type) ON CONFLICT DO NOTHING", targetAccountId);
        jdbcTemplate.update("INSERT INTO external_accounts (id, platform_id, ownership, account_type, username) VALUES (?, ?, 'TRACKED'::external_account_ownership, 'SOCIAL'::external_account_type, 'target_user') ON CONFLICT DO NOTHING", targetAccountId, platformId);

        long snapshotId = 9304L;
        jdbcTemplate.update("""
                INSERT INTO follower_snapshots (id, owner_account_id, captured_at, source, reported_total_count)
                VALUES (?, ?, CURRENT_TIMESTAMP, 'MANUAL'::follower_snapshot_source, 1)
                ON CONFLICT (id) DO NOTHING
                """, snapshotId, ownerAccountId);
        jdbcTemplate.update("""
                INSERT INTO follower_snapshot_entries (snapshot_id, target_account_id, username_snapshot, display_name_snapshot)
                VALUES (?, ?, 'target_snapshot_user', 'Target Display')
                ON CONFLICT DO NOTHING
                """, snapshotId, targetAccountId);

        // Export archive
        byte[] zipBytes = mockMvc.perform(post("/api/v1/portability/exports")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.put(entry.getName(), zis.readAllBytes());
                zis.closeEntry();
            }
        }

        // Assert trashed vault entry is present in vault_entries.jsonl with deleted_at
        assertThat(entries).containsKey("tables/vault_entries.jsonl");
        String vaultEntriesContent = new String(entries.get("tables/vault_entries.jsonl"), StandardCharsets.UTF_8);
        assertThat(vaultEntriesContent).contains("\"id\":" + trashedNoteId);
        assertThat(vaultEntriesContent).contains("\"deleted_at\":");

        // Assert notes.jsonl contains the note content
        assertThat(entries).containsKey("tables/notes.jsonl");
        String notesContent = new String(entries.get("tables/notes.jsonl"), StandardCharsets.UTF_8);
        assertThat(notesContent).contains("\"id\":" + trashedNoteId);

        // Assert follower_snapshots and follower_snapshot_entries are present
        assertThat(entries).containsKey("tables/follower_snapshots.jsonl");
        String snapshotsContent = new String(entries.get("tables/follower_snapshots.jsonl"), StandardCharsets.UTF_8);
        assertThat(snapshotsContent).contains("\"id\":" + snapshotId);

        assertThat(entries).containsKey("tables/follower_snapshot_entries.jsonl");
        String entriesContent = new String(entries.get("tables/follower_snapshot_entries.jsonl"), StandardCharsets.UTF_8);
        assertThat(entriesContent).contains("\"target_account_id\":" + targetAccountId);
        assertThat(entriesContent).contains("\"username_snapshot\":\"target_snapshot_user\"");
    }

    @Test
    @DisplayName("Export is strictly read-only and causes zero database mutations")
    void exportIsStrictlyReadOnlyAndZeroMutations() throws Exception {
        // Seed sample row to verify microsecond audit timestamps and row values remain untouched
        long sampleNoteId = 9401L;
        jdbcTemplate.update("""
                INSERT INTO vault_entries (id, entry_type) VALUES (?, 'NOTE'::vault_entry_type)
                ON CONFLICT (id) DO NOTHING
                """, sampleNoteId);
        jdbcTemplate.update("""
                INSERT INTO notes (id, title, content_markdown) VALUES (?, 'Audit Sample Note', '# Content')
                ON CONFLICT (id) DO NOTHING
                """, sampleNoteId);

        Map<String, Object> noteRowBefore = jdbcTemplate.queryForMap(
                "SELECT id, title, content_markdown FROM notes WHERE id = ?", sampleNoteId);
        Map<String, Object> vaultRowBefore = jdbcTemplate.queryForMap(
                "SELECT id, entry_type, created_at, updated_at FROM vault_entries WHERE id = ?", sampleNoteId);

        // Intercept snapshot connection to assert isReadOnly == true, TRANSACTION_REPEATABLE_READ, and write rejection
        AtomicReference<Connection> capturedConnection = new AtomicReference<>();
        DelegatingDataSource capturingDataSource = new DelegatingDataSource(dataSource) {
            @Override
            public Connection getConnection() throws SQLException {
                Connection conn = super.getConnection();
                capturedConnection.set(conn);
                return conn;
            }
        };

        PortabilitySnapshotAdapter testSnapshotAdapter = new PortabilitySnapshotAdapter(capturingDataSource);
        Path testArchive = Files.createTempFile("ppv-test-readonly-", ".zip");
        try {
            testSnapshotAdapter.exportSnapshot(testArchive, () -> {
                Connection conn = capturedConnection.get();
                assertThat(conn).isNotNull();
                try {
                    assertThat(conn.isReadOnly())
                            .as("Snapshot connection must have isReadOnly == true")
                            .isTrue();
                    assertThat(conn.getTransactionIsolation())
                            .as("Snapshot connection must use TRANSACTION_REPEATABLE_READ")
                            .isEqualTo(Connection.TRANSACTION_REPEATABLE_READ);

                    // Verify PostgreSQL rejects write operations in this read-only transaction with SQLState 25006
                    java.sql.Savepoint sp = conn.setSavepoint();
                    try {
                        assertThatThrownBy(() -> {
                            try (Statement stmt = conn.createStatement()) {
                                stmt.executeUpdate("INSERT INTO platforms (name, kind, url) VALUES ('RejectedPlatform', 'SOCIAL'::platform_kind, 'https://reject.example.com')");
                            }
                        })
                                .isInstanceOf(SQLException.class)
                                .satisfies(ex -> {
                                    SQLException sqlEx = (SQLException) ex;
                                    assertThat(sqlEx.getSQLState())
                                            .as("PostgreSQL must reject INSERT in read-only transaction with SQLState 25006")
                                            .isEqualTo("25006");
                                });
                    } finally {
                        conn.rollback(sp);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } finally {
            Files.deleteIfExists(testArchive);
        }

        // Collect row counts across several distinct tables before export
        List<String> tablesToCheck = List.of(
                "vault_entries", "notes", "diary_entries", "images", "albums",
                "import_jobs", "import_job_items", "feed_sources", "feed_items",
                "follower_snapshots", "follower_snapshot_entries", "platforms"
        );

        Map<String, Integer> countsBefore = new HashMap<>();
        for (String table : tablesToCheck) {
            Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Integer.class);
            countsBefore.put(table, count);
        }

        // Execute export
        mockMvc.perform(post("/api/v1/portability/exports")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk());

        // Verify counts after export are identical
        for (String table : tablesToCheck) {
            Integer countAfter = jdbcTemplate.queryForObject("SELECT count(*) FROM " + table, Integer.class);
            assertThat(countAfter)
                    .as("Table %s must not have mutations during read-only export", table)
                    .isEqualTo(countsBefore.get(table));
        }

        // Verify sampled row values and microsecond audit timestamps are identical before and after export
        Map<String, Object> noteRowAfter = jdbcTemplate.queryForMap(
                "SELECT id, title, content_markdown FROM notes WHERE id = ?", sampleNoteId);
        Map<String, Object> vaultRowAfter = jdbcTemplate.queryForMap(
                "SELECT id, entry_type, created_at, updated_at FROM vault_entries WHERE id = ?", sampleNoteId);

        assertThat(noteRowAfter).isEqualTo(noteRowBefore);
        assertThat(vaultRowAfter).isEqualTo(vaultRowBefore);
        java.sql.Timestamp vaultCreatedAtBefore = (java.sql.Timestamp) vaultRowBefore.get("created_at");
        java.sql.Timestamp vaultCreatedAtAfter = (java.sql.Timestamp) vaultRowAfter.get("created_at");
        assertThat(vaultCreatedAtAfter.getNanos()).isEqualTo(vaultCreatedAtBefore.getNanos());
    }

    @Test
    @DisplayName("Export temporary archive is cleaned up after completion")
    void exportTemporaryArchiveCleanupOnSuccess() throws Exception {
        PortabilityExportService trackingService = new PortabilityExportService(snapshotAdapter);
        Path archive = trackingService.createExportArchive();
        assertThat(Files.exists(archive)).isTrue();

        PortabilityController controller = new PortabilityController(new PortabilityExportService(snapshotAdapter) {
            @Override
            public Path createExportArchive() {
                return archive;
            }
        });

        org.springframework.mock.web.MockHttpServletResponse response = new org.springframework.mock.web.MockHttpServletResponse();
        controller.exportSnapshotArchive(response);

        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(Files.exists(archive))
                .as("Temporary archive file must be deleted after export completion")
                .isFalse();
    }

    @Test
    @DisplayName("Export temporary archive is cleaned up even if stream writing fails")
    void exportTemporaryArchiveCleanupOnStreamFailure() throws Exception {
        Path archive = Files.createTempFile("ppv-test-export-fail-", ".zip");
        Files.writeString(archive, "dummy content");
        assertThat(Files.exists(archive)).isTrue();

        PortabilityController controller = new PortabilityController(new PortabilityExportService(snapshotAdapter) {
            @Override
            public Path createExportArchive() {
                return archive;
            }
        });

        jakarta.servlet.http.HttpServletResponse failingResponse = mock(jakarta.servlet.http.HttpServletResponse.class);
        when(failingResponse.getOutputStream()).thenThrow(new IOException("Simulated network stream failure"));

        assertThatThrownBy(() -> controller.exportSnapshotArchive(failingResponse))
                .isInstanceOf(IOException.class);

        assertThat(Files.exists(archive))
                .as("Temporary archive file must be deleted even if streaming fails")
                .isFalse();
    }

    @Test
    @DisplayName("Export archive preserves large number JSONB directly without string coercion (FR14-3)")
    void exportArchivePreservesLargeNumberJsonbDirectly() throws Exception {
        long jobId = 9911L;
        long itemId = 9912L;
        jdbcTemplate.update("""
                INSERT INTO import_jobs (id, target_type, format, original_file_name)
                VALUES (?, 'NOTE'::import_target_type, 'JSON'::import_format, 'boundary.json')
                ON CONFLICT (id) DO NOTHING
                """, jobId);

        String complexJsonb = """
                {
                  "exactInteger": 1e1100,
                  "nestedArray": ["alpha", "beta", 42, {"innerKey": "innerVal"}],
                  "highPrecisionDecimal": 1234567890.1234567890123456789
                }
                """;
        jdbcTemplate.update("""
                INSERT INTO import_job_items (id, import_job_id, item_index, parsed_payload, status)
                VALUES (?, ?, 0, ?::jsonb, 'VALID'::import_item_status)
                ON CONFLICT (id) DO UPDATE SET parsed_payload = EXCLUDED.parsed_payload
                """, itemId, jobId, complexJsonb);

        byte[] zipBytes = mockMvc.perform(post("/api/v1/portability/exports")
                        .header("Authorization", bearerHeader()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsByteArray();

        Map<String, byte[]> entries = new HashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.put(entry.getName(), zis.readAllBytes());
                zis.closeEntry();
            }
        }

        assertThat(entries).containsKey("tables/import_job_items.jsonl");
        String importJobItemsContent = new String(entries.get("tables/import_job_items.jsonl"), StandardCharsets.UTF_8);

        // Parse with Jackson configured with large number length support and BigDecimal for floats
        com.fasterxml.jackson.databind.ObjectMapper testMapper = new com.fasterxml.jackson.databind.ObjectMapper(
                com.fasterxml.jackson.core.JsonFactory.builder()
                        .streamReadConstraints(com.fasterxml.jackson.core.StreamReadConstraints.builder()
                                .maxNumberLength(1_000_000)
                                .build())
                        .build()
        ).enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS);

        JsonNode targetRow = null;
        for (String line : importJobItemsContent.split("\n")) {
            if (line.isBlank()) continue;
            JsonNode node = testMapper.readTree(line);
            if (node.path("id").asLong() == itemId) {
                targetRow = node;
                break;
            }
        }

        assertThat(targetRow).isNotNull();
        JsonNode payloadNode = targetRow.path("parsed_payload");
        assertThat(payloadNode.isObject())
                .as("parsed_payload must be an ObjectNode, never text or string-coerced")
                .isTrue();

        ObjectNode objNode = (ObjectNode) payloadNode;

        // 1. exact 10^1100 integer assertions
        JsonNode integerNode = objNode.path("exactInteger");
        assertThat(integerNode.isNumber()).isTrue();
        String integerText = integerNode.asText();
        assertThat(integerText)
                .as("exactInteger must have length 1101 matching 10^1100")
                .hasSize(1101)
                .startsWith("1")
                .endsWith("0");
        assertThat(integerText)
                .isEqualTo("1" + "0".repeat(1100));

        // 2. nestedArray assertions
        JsonNode arrayNode = objNode.path("nestedArray");
        assertThat(arrayNode.isArray()).isTrue();
        assertThat(arrayNode).hasSize(4);
        assertThat(arrayNode.get(0).asText()).isEqualTo("alpha");
        assertThat(arrayNode.get(1).asText()).isEqualTo("beta");
        assertThat(arrayNode.get(2).asInt()).isEqualTo(42);
        assertThat(arrayNode.get(3).isObject()).isTrue();
        assertThat(arrayNode.get(3).path("innerKey").asText()).isEqualTo("innerVal");

        // 3. highPrecisionDecimal assertions
        JsonNode decimalNode = objNode.path("highPrecisionDecimal");
        assertThat(decimalNode.isNumber()).isTrue();
        assertThat(decimalNode.asText())
                .as("highPrecisionDecimal must match exact decimal text without exponent")
                .isEqualTo("1234567890.1234567890123456789");

        // 4. Raw text check ensuring no string coercion or escaping
        assertThat(importJobItemsContent)
                .as("Raw JSONL must contain direct object and not quoted escaped string")
                .contains("\"exactInteger\": 10000000")
                .doesNotContain("\"parsed_payload\":\"{")
                .doesNotContain("\\\"exactInteger\\\"");
    }

    @Test
    @DisplayName("Real HTTP client detects transfer failure on committed export archive stream and temp ZIP is deleted")
    void realHttpClientDetectsTransferFailureOnCommittedExportArchiveStream() throws Exception {
        org.apache.catalina.startup.Tomcat tomcat = new org.apache.catalina.startup.Tomcat();
        java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("ppv-tomcat-export-abort-");
        try {
            tomcat.setBaseDir(tempDir.toAbsolutePath().toString());
            tomcat.setPort(0);
            tomcat.getConnector();
            var context = tomcat.addContext("", tempDir.toAbsolutePath().toString());

            AtomicReference<Path> createdArchiveRef = new AtomicReference<>();
            PortabilityExportService trackingExportService = new PortabilityExportService(snapshotAdapter) {
                @Override
                public Path createExportArchive() {
                    Path archive = super.createExportArchive();
                    createdArchiveRef.set(archive);
                    return archive;
                }
            };

            var app = new org.springframework.web.context.support.GenericWebApplicationContext();
            app.registerBean("exportService", PortabilityExportService.class, () -> trackingExportService);
            app.registerBean("portabilityController", PortabilityController.class, () -> new PortabilityController(trackingExportService));
            app.registerBean("advice", ApiExceptionHandler.class, ApiExceptionHandler::new);
            app.refresh();

            // Register a filter that wraps the response to abort after 8192 bytes
            jakarta.servlet.Filter abortingFilter = new jakarta.servlet.Filter() {
                @Override
                public void doFilter(jakarta.servlet.ServletRequest request, jakarta.servlet.ServletResponse response, jakarta.servlet.FilterChain chain) throws IOException, jakarta.servlet.ServletException {
                    jakarta.servlet.http.HttpServletResponse httpResp = (jakarta.servlet.http.HttpServletResponse) response;
                    jakarta.servlet.http.HttpServletResponseWrapper wrapper = new jakarta.servlet.http.HttpServletResponseWrapper(httpResp) {
                        private jakarta.servlet.ServletOutputStream wrappedOut;

                        @Override
                        public jakarta.servlet.ServletOutputStream getOutputStream() throws IOException {
                            if (wrappedOut == null) {
                                jakarta.servlet.ServletOutputStream orig = super.getOutputStream();
                                wrappedOut = new jakarta.servlet.ServletOutputStream() {
                                    private int writtenBytes = 0;

                                    @Override
                                    public boolean isReady() {
                                        return orig.isReady();
                                    }

                                    @Override
                                    public void setWriteListener(jakarta.servlet.WriteListener writeListener) {
                                        orig.setWriteListener(writeListener);
                                    }

                                    @Override
                                    public void write(int b) throws IOException {
                                        if (writtenBytes >= 8192) {
                                            throw new IOException("Simulated network transfer abort mid-stream");
                                        }
                                        orig.write(b);
                                        writtenBytes++;
                                    }

                                    @Override
                                    public void write(byte[] b, int off, int len) throws IOException {
                                        if (writtenBytes >= 8192) {
                                            throw new IOException("Simulated network transfer abort mid-stream");
                                        }
                                        int chunk = Math.min(len, 8192 - writtenBytes);
                                        orig.write(b, off, chunk);
                                        orig.flush();
                                        writtenBytes += chunk;
                                        if (writtenBytes >= 8192) {
                                            throw new IOException("Simulated network transfer abort mid-stream");
                                        }
                                    }

                                    @Override
                                    public void flush() throws IOException {
                                        orig.flush();
                                    }

                                    @Override
                                    public void close() throws IOException {
                                        orig.close();
                                    }
                                };
                            }
                            return wrappedOut;
                        }
                    };
                    chain.doFilter(request, wrapper);
                }
            };

            var filterDef = new org.apache.tomcat.util.descriptor.web.FilterDef();
            filterDef.setFilterName("abortingFilter");
            filterDef.setFilter(abortingFilter);
            context.addFilterDef(filterDef);

            var filterMap = new org.apache.tomcat.util.descriptor.web.FilterMap();
            filterMap.setFilterName("abortingFilter");
            filterMap.addURLPattern("/*");
            context.addFilterMap(filterMap);

            var servletWrapper = org.apache.catalina.startup.Tomcat.addServlet(context, "dispatcher", new org.springframework.web.servlet.DispatcherServlet(app));
            servletWrapper.setAsyncSupported(true);
            servletWrapper.setLoadOnStartup(1);
            context.addServletMappingDecoded("/*", "dispatcher");

            tomcat.start();

            int port = tomcat.getConnector().getLocalPort();
            java.net.http.HttpClient client = java.net.http.HttpClient.newBuilder()
                    .connectTimeout(java.time.Duration.ofSeconds(5))
                    .build();
            java.net.URI uri = java.net.URI.create("http://localhost:" + port + "/api/v1/portability/exports");

            java.io.ByteArrayOutputStream received = new java.io.ByteArrayOutputStream();
            assertThatThrownBy(() -> {
                java.net.http.HttpResponse<java.io.InputStream> response = client.send(
                        java.net.http.HttpRequest.newBuilder(uri).POST(java.net.http.HttpRequest.BodyPublishers.noBody()).build(),
                        java.net.http.HttpResponse.BodyHandlers.ofInputStream()
                );
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.headers().firstValue("Content-Type")).contains("application/zip");
                try (java.io.InputStream body = response.body()) {
                    byte[] buf = new byte[1024];
                    int r;
                    while ((r = body.read(buf)) != -1) {
                        received.write(buf, 0, r);
                    }
                }
            })
                    .as("Client must observe transfer failure (IOException) on aborted export stream")
                    .isInstanceOf(IOException.class);

            // Assert that no trailing JSON was sent
            String receivedContent = received.toString(StandardCharsets.UTF_8);
            assertThat(receivedContent)
                    .as("Committed stream failure must not deliver trailing JSON error response")
                    .doesNotContain("\"error\":")
                    .doesNotContain("\"code\":");

            // Assert that the created temporary archive file is deleted
            Path createdArchive = createdArchiveRef.get();
            assertThat(createdArchive).isNotNull();
            assertThat(Files.exists(createdArchive))
                    .as("Temporary export ZIP file must be deleted after stream failure")
                    .isFalse();

            app.close();
        } finally {
            try { tomcat.stop(); } catch (Exception ignored) {}
            try { tomcat.destroy(); } catch (Exception ignored) {}
            try (var s = java.nio.file.Files.walk(tempDir)) {
                s.sorted(java.util.Comparator.reverseOrder()).forEach(p -> {
                    try { java.nio.file.Files.deleteIfExists(p); } catch (Exception ignored) {}
                });
            } catch (Exception ignored) {}
        }
    }

    @Test
    @DisplayName("Early snapshot failure before row consumption releases statements, rolls back transaction, closes connection, and deletes temporary archive")
    void earlySnapshotFailureReleasesResourcesAndDeletesTempFile() {
        AtomicReference<Path> capturedZipRef = new AtomicReference<>();
        AtomicBoolean rolledBack = new AtomicBoolean(false);
        AtomicBoolean connectionClosed = new AtomicBoolean(false);
        AtomicInteger statementsCreated = new AtomicInteger(0);
        AtomicInteger statementsClosed = new AtomicInteger(0);

        DataSource instrumentedDataSource = new DelegatingDataSource(dataSource) {
            @Override
            public Connection getConnection() throws SQLException {
                Connection rawConn = super.getConnection();
                return (Connection) Proxy.newProxyInstance(
                        Connection.class.getClassLoader(),
                        new Class<?>[]{Connection.class},
                        (proxy, method, args) -> {
                            if ("rollback".equals(method.getName())) {
                                rolledBack.set(true);
                            } else if ("close".equals(method.getName())) {
                                connectionClosed.set(true);
                            } else if ("createStatement".equals(method.getName()) || "prepareStatement".equals(method.getName())) {
                                Statement rawStmt = (Statement) method.invoke(rawConn, args);
                                statementsCreated.incrementAndGet();
                                return (Statement) Proxy.newProxyInstance(
                                        Statement.class.getClassLoader(),
                                        new Class<?>[]{method.getReturnType()},
                                        (stmtProxy, stmtMethod, stmtArgs) -> {
                                            if ("close".equals(stmtMethod.getName())) {
                                                statementsClosed.incrementAndGet();
                                            }
                                            return stmtMethod.invoke(rawStmt, stmtArgs);
                                        }
                                );
                            }
                            return method.invoke(rawConn, args);
                        }
                );
            }
        };

        PortabilitySnapshotAdapter failingAdapter = new PortabilitySnapshotAdapter(instrumentedDataSource) {
            @Override
            public void exportSnapshot(Path targetZip) {
                capturedZipRef.set(targetZip);
                super.exportSnapshot(targetZip, () -> {
                    throw new RuntimeException("Simulated early snapshot failure");
                });
            }
        };

        PortabilityExportService service = new PortabilityExportService(failingAdapter);

        assertThatThrownBy(service::createExportArchive)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Simulated early snapshot failure");

        assertThat(rolledBack.get()).as("Transaction must be rolled back on early snapshot failure").isTrue();
        assertThat(connectionClosed.get()).as("Database connection must be closed/released to pool").isTrue();
        assertThat(statementsCreated.get()).as("Database statements were created during schema validation").isGreaterThan(0);
        assertThat(statementsClosed.get()).as("All created statements must be closed").isEqualTo(statementsCreated.get());

        Path capturedZip = capturedZipRef.get();
        assertThat(capturedZip).isNotNull();
        assertThat(Files.exists(capturedZip)).as("Temporary ZIP archive file must be deleted upon early failure").isFalse();
    }

    @Test
    @DisplayName("Active snapshot row consumption query cancellation/timeout closes cursors, rolls back transaction, releases connection, and deletes temporary archive")
    void activeSnapshotRowConsumptionCancellationReleasesResourcesAndDeletesTempArchive() {
        InstrumentedStreamState state = new InstrumentedStreamState();
        state.injectErrorAfterRows = 1;
        state.errorToInjectOnNext = new SQLException("Simulated snapshot query cancellation mid-stream", "57014");

        DataSource instrumentedDataSource = createInstrumentedDataSource(dataSource, state);

        PortabilitySnapshotAdapter adapter = new PortabilitySnapshotAdapter(instrumentedDataSource) {
            @Override
            public void exportSnapshot(Path targetZip) {
                state.capturedZipRef.set(targetZip);
                super.exportSnapshot(targetZip);
            }
        };

        PortabilityExportService service = new PortabilityExportService(adapter);

        assertThatThrownBy(service::createExportArchive)
                .isInstanceOf(RuntimeException.class)
                .hasRootCauseInstanceOf(SQLException.class);

        assertThat(state.rowsRead.get()).as("Active rows must be consumed before cancellation fault").isGreaterThanOrEqualTo(1);
        assertThat(state.resultSetsCreated.get()).as("Active table streaming created ResultSets").isGreaterThan(0);
        assertThat(state.resultSetsClosed.get()).as("Active streaming ResultSets must all be closed on cancellation").isEqualTo(state.resultSetsCreated.get());
        assertThat(state.preparedStatementsCreated.get()).as("PreparedStatements were created for table streaming").isGreaterThan(0);
        assertThat(state.preparedStatementsClosed.get()).as("All PreparedStatements must be closed on cancellation").isEqualTo(state.preparedStatementsCreated.get());
        assertThat(state.statementsClosed.get()).as("All Statements must be closed on cancellation").isEqualTo(state.statementsCreated.get());
        assertThat(state.rolledBack.get()).as("Transaction must be rolled back on cancellation").isTrue();
        assertThat(state.connectionClosed.get()).as("Database connection must be closed/released to pool on cancellation").isTrue();

        Path capturedZip = state.capturedZipRef.get();
        assertThat(capturedZip).isNotNull();
        assertThat(Files.exists(capturedZip)).as("Temporary ZIP archive must be deleted upon cancellation during active row streaming").isFalse();
    }

    @Test
    @DisplayName("Active snapshot row consumption disconnect aborts streaming, closes cursors, rolls back transaction, releases connection, and deletes temporary archive")
    void activeSnapshotRowConsumptionDisconnectAbortsStreamAndDeletesTempArchive() {
        InstrumentedStreamState state = new InstrumentedStreamState();
        state.injectErrorAfterRows = 1;
        state.errorToInjectOnNext = new SQLException("Simulated client/network disconnect mid-stream", "08006");

        DataSource instrumentedDataSource = createInstrumentedDataSource(dataSource, state);

        PortabilitySnapshotAdapter adapter = new PortabilitySnapshotAdapter(instrumentedDataSource) {
            @Override
            public void exportSnapshot(Path targetZip) {
                state.capturedZipRef.set(targetZip);
                super.exportSnapshot(targetZip);
            }
        };

        PortabilityExportService service = new PortabilityExportService(adapter);

        assertThatThrownBy(service::createExportArchive)
                .isInstanceOf(RuntimeException.class)
                .hasRootCauseInstanceOf(SQLException.class);

        assertThat(state.rowsRead.get()).as("Active rows must be consumed before disconnect fault").isGreaterThanOrEqualTo(1);
        assertThat(state.resultSetsCreated.get()).as("Active streaming ResultSets created").isGreaterThan(0);
        assertThat(state.resultSetsClosed.get()).as("Active streaming ResultSets must all be closed on disconnect").isEqualTo(state.resultSetsCreated.get());
        assertThat(state.preparedStatementsCreated.get()).as("PreparedStatements were created for table streaming").isGreaterThan(0);
        assertThat(state.preparedStatementsClosed.get()).as("All PreparedStatements must be closed on disconnect").isEqualTo(state.preparedStatementsCreated.get());
        assertThat(state.statementsClosed.get()).as("All Statements must be closed on disconnect").isEqualTo(state.statementsCreated.get());
        assertThat(state.rolledBack.get()).as("Transaction must be rolled back on disconnect").isTrue();
        assertThat(state.connectionClosed.get()).as("Database connection must be closed/released to pool on disconnect").isTrue();

        Path capturedZip = state.capturedZipRef.get();
        assertThat(capturedZip).isNotNull();
        assertThat(Files.exists(capturedZip)).as("Temporary ZIP archive must be deleted upon disconnect during active row streaming").isFalse();
    }

    @Test
    @DisplayName("Active snapshot successful stream release closes all cursors and statements, commits transaction, releases connection, and retains archive")
    void activeSnapshotSuccessfulStreamReleaseClosesAllCursorsAndRetainsArchive() throws Exception {
        InstrumentedStreamState state = new InstrumentedStreamState();
        state.injectErrorAfterRows = -1;
        state.errorToInjectOnNext = null;

        DataSource instrumentedDataSource = createInstrumentedDataSource(dataSource, state);

        PortabilitySnapshotAdapter adapter = new PortabilitySnapshotAdapter(instrumentedDataSource) {
            @Override
            public void exportSnapshot(Path targetZip) {
                state.capturedZipRef.set(targetZip);
                super.exportSnapshot(targetZip);
            }
        };

        PortabilityExportService service = new PortabilityExportService(adapter);

        Path exportedZip = service.createExportArchive();
        try {
            assertThat(state.rowsRead.get()).as("Active rows were consumed during full export").isGreaterThan(0);
            assertThat(state.resultSetsCreated.get()).as("ResultSets created during full export").isGreaterThan(0);
            assertThat(state.resultSetsClosed.get()).as("All ResultSets must be closed on success").isEqualTo(state.resultSetsCreated.get());
            assertThat(state.preparedStatementsCreated.get()).as("PreparedStatements created during full export").isGreaterThan(0);
            assertThat(state.preparedStatementsClosed.get()).as("All PreparedStatements must be closed on success").isEqualTo(state.preparedStatementsCreated.get());
            assertThat(state.statementsClosed.get()).as("All Statements must be closed on success").isEqualTo(state.statementsCreated.get());
            assertThat(state.committed.get()).as("Transaction must be committed on success").isTrue();
            assertThat(state.connectionClosed.get()).as("Database connection must be closed/released to pool on success").isTrue();

            assertThat(Files.exists(exportedZip)).as("Exported ZIP archive must exist on success").isTrue();
            assertThat(Files.size(exportedZip)).as("Exported ZIP archive size must be greater than zero").isGreaterThan(0L);
        } finally {
            if (exportedZip != null) {
                Files.deleteIfExists(exportedZip);
            }
        }
    }

    static class InstrumentedStreamState {
        final AtomicReference<Path> capturedZipRef = new AtomicReference<>();
        final AtomicBoolean rolledBack = new AtomicBoolean(false);
        final AtomicBoolean committed = new AtomicBoolean(false);
        final AtomicBoolean connectionClosed = new AtomicBoolean(false);
        final AtomicInteger statementsCreated = new AtomicInteger(0);
        final AtomicInteger statementsClosed = new AtomicInteger(0);
        final AtomicInteger preparedStatementsCreated = new AtomicInteger(0);
        final AtomicInteger preparedStatementsClosed = new AtomicInteger(0);
        final AtomicInteger resultSetsCreated = new AtomicInteger(0);
        final AtomicInteger resultSetsClosed = new AtomicInteger(0);
        final AtomicInteger rowsRead = new AtomicInteger(0);

        SQLException errorToInjectOnNext = null;
        int injectErrorAfterRows = -1;
    }

    private DataSource createInstrumentedDataSource(DataSource realDataSource, InstrumentedStreamState state) {
        return new DelegatingDataSource(realDataSource) {
            @Override
            public Connection getConnection() throws SQLException {
                Connection rawConn = super.getConnection();
                return (Connection) Proxy.newProxyInstance(
                        Connection.class.getClassLoader(),
                        new Class<?>[]{Connection.class},
                        (connProxy, connMethod, connArgs) -> {
                            String name = connMethod.getName();
                            if ("rollback".equals(name)) {
                                state.rolledBack.set(true);
                            } else if ("commit".equals(name)) {
                                state.committed.set(true);
                            } else if ("close".equals(name)) {
                                state.connectionClosed.set(true);
                            } else if ("createStatement".equals(name)) {
                                Statement rawStmt = (Statement) connMethod.invoke(rawConn, connArgs);
                                state.statementsCreated.incrementAndGet();
                                return (Statement) Proxy.newProxyInstance(
                                        Statement.class.getClassLoader(),
                                        new Class<?>[]{connMethod.getReturnType()},
                                        (stmtProxy, stmtMethod, stmtArgs) -> {
                                            if ("close".equals(stmtMethod.getName())) {
                                                state.statementsClosed.incrementAndGet();
                                            } else if ("executeQuery".equals(stmtMethod.getName())) {
                                                ResultSet rawRs = (ResultSet) stmtMethod.invoke(rawStmt, stmtArgs);
                                                state.resultSetsCreated.incrementAndGet();
                                                return (ResultSet) Proxy.newProxyInstance(
                                                        ResultSet.class.getClassLoader(),
                                                        new Class<?>[]{ResultSet.class},
                                                        (rsProxy, rsMethod, rsArgs) -> {
                                                            if ("close".equals(rsMethod.getName())) {
                                                                state.resultSetsClosed.incrementAndGet();
                                                            }
                                                            return rsMethod.invoke(rawRs, rsArgs);
                                                        }
                                                );
                                            }
                                            return stmtMethod.invoke(rawStmt, stmtArgs);
                                        }
                                );
                            } else if ("prepareStatement".equals(name)) {
                                PreparedStatement rawPstmt = (PreparedStatement) connMethod.invoke(rawConn, connArgs);
                                state.preparedStatementsCreated.incrementAndGet();
                                return (PreparedStatement) Proxy.newProxyInstance(
                                        PreparedStatement.class.getClassLoader(),
                                        new Class<?>[]{connMethod.getReturnType()},
                                        (pstmtProxy, pstmtMethod, pstmtArgs) -> {
                                            if ("close".equals(pstmtMethod.getName())) {
                                                state.preparedStatementsClosed.incrementAndGet();
                                            } else if ("executeQuery".equals(pstmtMethod.getName())) {
                                                ResultSet rawRs = (ResultSet) pstmtMethod.invoke(rawPstmt, pstmtArgs);
                                                state.resultSetsCreated.incrementAndGet();
                                                return (ResultSet) Proxy.newProxyInstance(
                                                        ResultSet.class.getClassLoader(),
                                                        new Class<?>[]{ResultSet.class},
                                                        (rsProxy, rsMethod, rsArgs) -> {
                                                            if ("close".equals(rsMethod.getName())) {
                                                                state.resultSetsClosed.incrementAndGet();
                                                            } else if ("next".equals(rsMethod.getName())) {
                                                                boolean hasNext = (boolean) rsMethod.invoke(rawRs, rsArgs);
                                                                if (hasNext) {
                                                                    int count = state.rowsRead.incrementAndGet();
                                                                    if (state.errorToInjectOnNext != null && count >= state.injectErrorAfterRows) {
                                                                        throw state.errorToInjectOnNext;
                                                                    }
                                                                }
                                                                return hasNext;
                                                            }
                                                            return rsMethod.invoke(rawRs, rsArgs);
                                                        }
                                                );
                                            }
                                            return pstmtMethod.invoke(rawPstmt, pstmtArgs);
                                        }
                                );
                            }
                            return connMethod.invoke(rawConn, connArgs);
                        }
                );
            }
        };
    }
}
