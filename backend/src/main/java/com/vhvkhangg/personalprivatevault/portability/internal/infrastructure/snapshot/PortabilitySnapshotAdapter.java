package com.vhvkhangg.personalprivatevault.portability.internal.infrastructure.snapshot;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * ADR-0017 read-only JDBC snapshot adapter for portable vault archives.
 * <p>
 * Performs a strictly read-only, REPEATABLE READ snapshot across an explicit allowlist
 * of application tables, excluding authentication secrets and system metadata.
 */
@Component
public class PortabilitySnapshotAdapter {

    public static final List<String> ALLOWED_TABLES = List.of(
            "addresses",
            "albums",
            "app_settings",
            "brands",
            "countries",
            "creator_group_members",
            "creator_groups",
            "currencies",
            "diary_entries",
            "external_account_relationships",
            "external_accounts",
            "favorites",
            "feed_items",
            "feed_sources",
            "fiction_genres",
            "fiction_links",
            "fiction_story_archetypes",
            "fiction_world_settings",
            "fictions",
            "film_credits",
            "film_genre_assignments",
            "film_genres",
            "film_links",
            "film_story_archetypes",
            "film_world_settings",
            "films",
            "financial_transaction_entries",
            "financial_transactions",
            "follower_snapshot_entries",
            "follower_snapshots",
            "images",
            "import_job_items",
            "import_jobs",
            "information_items",
            "languages",
            "location_business_hours",
            "location_categories",
            "location_category_assignments",
            "location_dining_service_styles",
            "locations",
            "music_track_people",
            "music_tracks",
            "notes",
            "person_roles",
            "personal_profiles",
            "persons",
            "platforms",
            "ratings",
            "recurring_rule_entries",
            "recurring_rule_weekdays",
            "recurring_transaction_rules",
            "saved_resource_conversions",
            "saved_resources",
            "shopping_items",
            "software_item_platforms",
            "software_items",
            "story_archetypes",
            "study_items",
            "subscriptions",
            "tags",
            "transaction_categories",
            "vault_entries",
            "vault_entry_tags",
            "vocabulary_items",
            "vocabulary_reviews",
            "wallets",
            "world_settings"
    );

    public static final Set<String> EXCLUDED_SECURITY_TABLES = Set.of(
            "app_users",
            "refresh_tokens"
    );

    public static final Set<String> EXCLUDED_SYSTEM_TABLES = Set.of(
            "flyway_schema_history"
    );

    public record RawJsonValue(String value) implements com.fasterxml.jackson.databind.JsonSerializable {
        @Override
        public void serialize(com.fasterxml.jackson.core.JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers) throws java.io.IOException {
            if (value == null) {
                gen.writeNull();
            } else {
                gen.writeRawValue(value);
            }
        }

        @Override
        public void serializeWithType(com.fasterxml.jackson.core.JsonGenerator gen, com.fasterxml.jackson.databind.SerializerProvider serializers, com.fasterxml.jackson.databind.jsontype.TypeSerializer typeSer) throws java.io.IOException {
            serialize(gen, serializers);
        }
    }

    private final DataSource dataSource;
    private final ObjectMapper objectMapper = new ObjectMapper(
            com.fasterxml.jackson.core.JsonFactory.builder()
                    .streamReadConstraints(com.fasterxml.jackson.core.StreamReadConstraints.builder()
                            .maxNumberLength(1_000_000)
                            .maxStringLength(100_000_000)
                            .maxNestingDepth(10_000)
                            .build())
                    .build()
    )
            .findAndRegisterModules()
            .enable(com.fasterxml.jackson.databind.DeserializationFeature.USE_BIG_DECIMAL_FOR_FLOATS)
            .enable(com.fasterxml.jackson.core.JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN);

    public PortabilitySnapshotAdapter(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public record TableInventoryEntry(String name, long rowCount) {}

    public record ExportManifest(
            int formatVersion,
            String generatedAt,
            String highestFlywayVersion,
            List<TableInventoryEntry> tableInventory,
            List<String> excludedSecurityTables
    ) {}

    /**
     * Executes the read-only snapshot export to the specified target ZIP file.
     * All database reads occur in one REPEATABLE READ read-only transaction.
     *
     * @param targetZip target file path where ZIP archive will be written
     */
    public void exportSnapshot(Path targetZip) {
        exportSnapshot(targetZip, () -> {});
    }

    public void exportSnapshot(Path targetZip, Runnable onSnapshotEstablished) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            connection.setReadOnly(true);
            connection.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);
            try {
                validateSchemaAllowlist(connection);
                if (onSnapshotEstablished != null) {
                    onSnapshotEstablished.run();
                }

                try (OutputStream fos = Files.newOutputStream(targetZip);
                     BufferedOutputStream bos = new BufferedOutputStream(fos);
                     ZipOutputStream zos = new ZipOutputStream(bos, StandardCharsets.UTF_8)) {

                    String highestFlywayVersion = resolveHighestFlywayVersion(connection);
                    List<TableInventoryEntry> inventory = new ArrayList<>();
                    for (String table : ALLOWED_TABLES) {
                        long count = queryTableRowCount(connection, table);
                        inventory.add(new TableInventoryEntry(table, count));
                    }

                    // 1. Write manifest.json
                    writeManifestEntry(zos, highestFlywayVersion, inventory);

                    // 2. Write tables/<table>.jsonl
                    for (String table : ALLOWED_TABLES) {
                        writeTableJsonlEntry(zos, connection, table);
                    }

                    // 3. Write markdown entries
                    writeMarkdownEntries(zos, connection);

                    // 4. Write media manifest
                    writeMediaManifestEntry(zos, connection);

                    zos.finish();
                }

                connection.commit();
            } catch (Exception ex) {
                try {
                    connection.rollback();
                } catch (Exception ignored) {
                }
                throw ex;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Snapshot export failed", e);
        } catch (IOException e) {
            throw new UncheckedIOException("Snapshot export IO failed", e);
        }
    }

    public void validateSchemaAllowlist(Connection connection) throws SQLException {
        List<String> actualTables = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("""
                     SELECT table_name
                     FROM information_schema.tables
                     WHERE table_schema = 'public'
                       AND table_type = 'BASE TABLE'
                     ORDER BY table_name
                     """)) {
            while (rs.next()) {
                actualTables.add(rs.getString("table_name"));
            }
        }
        for (String table : actualTables) {
            if (EXCLUDED_SECURITY_TABLES.contains(table) || EXCLUDED_SYSTEM_TABLES.contains(table)) {
                continue;
            }
            if (!ALLOWED_TABLES.contains(table)) {
                throw new IllegalStateException("Unapproved table in public schema: " + table
                        + ". All non-auth tables must be explicitly evaluated for export allowlist.");
            }
        }
    }

    private String resolveHighestFlywayVersion(Connection connection) {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("""
                     SELECT version
                     FROM flyway_schema_history
                     WHERE success = true
                     ORDER BY installed_rank DESC
                     LIMIT 1
                     """)) {
            if (rs.next()) {
                return rs.getString("version");
            }
        } catch (SQLException ignored) {
        }
        return "1";
    }

    private long queryTableRowCount(Connection connection, String table) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM public.\"" + table + "\"")) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        }
        return 0L;
    }

    private void writeManifestEntry(ZipOutputStream zos, String highestFlywayVersion, List<TableInventoryEntry> inventory) throws IOException {
        ExportManifest manifest = new ExportManifest(
                1,
                Instant.now().toString(),
                highestFlywayVersion,
                inventory,
                new ArrayList<>(EXCLUDED_SECURITY_TABLES)
        );
        ZipEntry entry = new ZipEntry("manifest.json");
        zos.putNextEntry(entry);
        zos.write(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(manifest));
        zos.closeEntry();
    }

    private void writeTableJsonlEntry(ZipOutputStream zos, Connection connection, String table) throws SQLException, IOException {
        String orderBy = resolveOrderByClause(connection, table);
        String sql = "SELECT * FROM public.\"" + table + "\" " + orderBy;

        ZipEntry entry = new ZipEntry("tables/" + table + ".jsonl");
        zos.putNextEntry(entry);

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setFetchSize(500);
            try (ResultSet rs = stmt.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int colCount = meta.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>(colCount);
                    for (int i = 1; i <= colCount; i++) {
                        String colName = meta.getColumnName(i);
                        Object val = extractColumnValue(rs, i, meta.getColumnType(i), meta.getColumnTypeName(i));
                        row.put(colName, val);
                    }
                    byte[] lineBytes = (objectMapper.writeValueAsString(row) + "\n").getBytes(StandardCharsets.UTF_8);
                    zos.write(lineBytes);
                }
            }
        }
        zos.closeEntry();
    }

    private String resolveOrderByClause(Connection connection, String table) throws SQLException {
        record PkCol(int seq, String name) {}
        List<PkCol> cols = new ArrayList<>();
        try (ResultSet rs = connection.getMetaData().getPrimaryKeys(null, "public", table)) {
            while (rs.next()) {
                cols.add(new PkCol(rs.getInt("KEY_SEQ"), rs.getString("COLUMN_NAME")));
            }
        }
        cols.sort(Comparator.comparingInt(PkCol::seq));
        if (cols.isEmpty()) {
            return "ORDER BY ctid ASC";
        }
        return "ORDER BY " + cols.stream()
                .map(c -> "\"" + c.name() + "\" ASC")
                .collect(Collectors.joining(", "));
    }

    private void writeMarkdownEntries(ZipOutputStream zos, Connection connection) throws SQLException, IOException {
        // 1. Knowledge Notes
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, content_markdown FROM public.notes WHERE content_markdown IS NOT NULL ORDER BY id ASC")) {
            stmt.setFetchSize(500);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String markdown = rs.getString("content_markdown");
                    if (markdown != null) {
                        ZipEntry entry = new ZipEntry("markdown/knowledge/notes/" + id + ".md");
                        zos.putNextEntry(entry);
                        zos.write(markdown.getBytes(StandardCharsets.UTF_8));
                        zos.closeEntry();
                    }
                }
            }
        }

        // 2. Knowledge Information items
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, content_markdown FROM public.information_items WHERE content_markdown IS NOT NULL ORDER BY id ASC")) {
            stmt.setFetchSize(500);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String markdown = rs.getString("content_markdown");
                    if (markdown != null) {
                        ZipEntry entry = new ZipEntry("markdown/knowledge/information/" + id + ".md");
                        zos.putNextEntry(entry);
                        zos.write(markdown.getBytes(StandardCharsets.UTF_8));
                        zos.closeEntry();
                    }
                }
            }
        }

        // 3. Journal Diary entries
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT id, content_markdown FROM public.diary_entries WHERE content_markdown IS NOT NULL ORDER BY id ASC")) {
            stmt.setFetchSize(500);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    long id = rs.getLong("id");
                    String markdown = rs.getString("content_markdown");
                    if (markdown != null) {
                        ZipEntry entry = new ZipEntry("markdown/journal/diary-entries/" + id + ".md");
                        zos.putNextEntry(entry);
                        zos.write(markdown.getBytes(StandardCharsets.UTF_8));
                        zos.closeEntry();
                    }
                }
            }
        }
    }

    private void writeMediaManifestEntry(ZipOutputStream zos, Connection connection) throws SQLException, IOException {
        ZipEntry entry = new ZipEntry("media/manifest.jsonl");
        zos.putNextEntry(entry);

        try (PreparedStatement stmt = connection.prepareStatement("""
                SELECT i.id, i.object_key, i.checksum_sha256, i.mime_type, i.size_bytes, v.created_at
                FROM public.images i
                JOIN public.vault_entries v ON v.id = i.id
                ORDER BY i.id ASC
                """)) {
            stmt.setFetchSize(500);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("id", rs.getLong("id"));
                    item.put("object_key", rs.getString("object_key"));
                    item.put("checksum_sha256", rs.getString("checksum_sha256"));
                    item.put("mime_type", rs.getString("mime_type"));
                    item.put("size_bytes", rs.getObject("size_bytes") != null ? rs.getLong("size_bytes") : null);
                    Timestamp createdAt = rs.getTimestamp("created_at");
                    item.put("created_at", createdAt != null ? createdAt.toInstant().toString() : null);

                    byte[] lineBytes = (objectMapper.writeValueAsString(item) + "\n").getBytes(StandardCharsets.UTF_8);
                    zos.write(lineBytes);
                }
            }
        }
        zos.closeEntry();
    }

    private Object extractColumnValue(ResultSet rs, int colIndex, int columnType, String typeName) throws SQLException {
        Object val = rs.getObject(colIndex);
        if (val == null) {
            return null;
        }
        if ("time".equalsIgnoreCase(typeName) || "timetz".equalsIgnoreCase(typeName) || val instanceof Time || val instanceof LocalTime) {
            String timeStr = rs.getString(colIndex);
            if (timeStr != null) {
                return timeStr;
            }
            if (val instanceof LocalTime lt) {
                return lt.toString();
            }
            if (val instanceof Time t) {
                return t.toLocalTime().toString();
            }
        }
        if (val instanceof Timestamp ts) {
            return ts.toInstant().toString();
        }
        if (val instanceof Date d) {
            return d.toLocalDate().toString();
        }
        if (val instanceof OffsetDateTime odt) {
            return odt.toInstant().toString();
        }
        if (val instanceof Instant instant) {
            return instant.toString();
        }
        if (val instanceof LocalDate ld) {
            return ld.toString();
        }
        if (val instanceof LocalTime lt) {
            return lt.toString();
        }
        if (val instanceof LocalDateTime ldt) {
            return ldt.atZone(ZoneOffset.UTC).toInstant().toString();
        }
        if ("jsonb".equalsIgnoreCase(typeName) || "json".equalsIgnoreCase(typeName)) {
            String jsonStr = rs.getString(colIndex);
            if (jsonStr == null) {
                return null;
            }
            String trimmed = jsonStr.trim();
            if (trimmed.isEmpty() || "null".equalsIgnoreCase(trimmed)) {
                return null;
            }
            return new RawJsonValue(trimmed);
        }
        if (val instanceof BigDecimal || val instanceof Number || val instanceof Boolean || val instanceof String) {
            return val;
        }
        return val.toString();
    }
}
