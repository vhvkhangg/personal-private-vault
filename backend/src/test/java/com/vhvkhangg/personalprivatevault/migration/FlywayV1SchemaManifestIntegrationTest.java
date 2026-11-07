package com.vhvkhangg.personalprivatevault.migration;

import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class FlywayV1SchemaManifestIntegrationTest extends AbstractPostgresIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("Flyway V1 migration succeeds and matches the frozen manifest baseline")
    void flywayV1MatchesManifestBaseline() {
        // Assert Flyway migration was applied successfully
        Boolean flywaySuccess = jdbcTemplate.queryForObject(
                "SELECT success FROM flyway_schema_history WHERE version = '1'",
                Boolean.class
        );
        assertThat(flywaySuccess).isTrue();

        // 1. Assert 41 named enum types in public schema
        Integer enumCount = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM pg_type t
                JOIN pg_namespace n ON n.oid = t.typnamespace
                WHERE t.typtype = 'e' AND n.nspname = 'public'
                """,
                Integer.class
        );
        assertThat(enumCount)
                .as("Flyway V1 must define exactly 41 named PostgreSQL enum types")
                .isEqualTo(41);

        // 2. Assert 69 application tables (excluding flyway_schema_history and post-v1 system/security tables)
        Integer tableCount = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_type = 'BASE TABLE'
                  AND table_name NOT IN ('flyway_schema_history', 'vault_rag_source_state', 'drive_connections', 'drive_folder_roots', 'drive_upload_intents')
                """,
                Integer.class
        );
        assertThat(tableCount)
                .as("Flyway V1 must create exactly 69 application tables")
                .isEqualTo(69);

        // 3. Assert drive_connections table exists in public schema
        Integer driveTableCount = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_type = 'BASE TABLE'
                  AND table_name = 'drive_connections'
                """,
                Integer.class
        );
        assertThat(driveTableCount)
                .as("drive_connections table must exist in public schema")
                .isEqualTo(1);

        // 4. Assert 103 foreign key constraints
        Integer foreignKeyCount = jdbcTemplate.queryForObject(
                """
                SELECT count(*)
                FROM information_schema.table_constraints
                WHERE constraint_schema = 'public'
                  AND constraint_type = 'FOREIGN KEY'
                  AND table_name NOT IN ('drive_folder_roots','drive_upload_intents')
                """,
                Integer.class
        );
        assertThat(foreignKeyCount)
                .as("Flyway V1 must define exactly 103 foreign keys")
                .isEqualTo(103);
    }

    @Test
    @DisplayName("Portability allowlist exactly matches all 67 non-auth application tables in physical schema")
    void portabilityAllowlistMatchesPhysicalSchema() {
        var physicalTables = jdbcTemplate.queryForList(
                """
                SELECT table_name
                FROM information_schema.tables
                WHERE table_schema = 'public'
                  AND table_type = 'BASE TABLE'
                  AND table_name NOT IN ('flyway_schema_history', 'app_users', 'refresh_tokens', 'vault_rag_source_state', 'drive_connections', 'drive_folder_roots', 'drive_upload_intents')
                ORDER BY table_name
                """,
                String.class
        );

        assertThat(physicalTables)
                .as("Expected exactly 67 non-auth application tables in PostgreSQL")
                .hasSize(67);

        assertThat(com.vhvkhangg.personalprivatevault.portability.internal.infrastructure.snapshot.PortabilitySnapshotAdapter.ALLOWED_TABLES)
                .as("Portability ALLOWED_TABLES must match physical schema non-auth tables exactly")
                .containsExactlyInAnyOrderElementsOf(physicalTables);
    }
}
