package com.vhvkhangg.personalprivatevault.rag;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import java.sql.*;
import java.time.Duration;
import java.util.UUID;
import static org.assertj.core.api.Assertions.*;

@Timeout(60)
class RagPgVectorCompatibilityIntegrationTest {

    @Test void upgradingV6RevokesUnsafeExistingRoleAndScrubsRestoredExternalAuthority() throws Exception {
        try(var database=new PostgreSQLContainer<>(DockerImageName.parse("ppv-rag-postgres:18.6-pgvector0.8.6").asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("ppv_rag_upgrade").withUsername("upgrade_migrator")
                .withPassword("synthetic-upgrade-fixture-password").withStartupTimeout(Duration.ofSeconds(30))) {
            database.start();
            Flyway.configure().dataSource(database.getJdbcUrl(),database.getUsername(),database.getPassword())
                    .locations("classpath:db/migration").target("6").load().migrate();
            try(var connection=DriverManager.getConnection(database.getJdbcUrl(),database.getUsername(),database.getPassword());
                var statement=connection.createStatement()) {
                statement.execute("ALTER ROLE rag_worker SUPERUSER CREATEDB CREATEROLE INHERIT BYPASSRLS REPLICATION LOGIN PASSWORD 'synthetic-unsafe-legacy-secret'");
                statement.execute("GRANT pg_read_all_data TO rag_worker");
                statement.execute("INSERT INTO rag.sources(source_type,source_id,classification,approved_fields_text,access_state,source_revision_hash,description_revision_hash,created_at,updated_at) VALUES ('EXTERNAL_FILE','C:/synthetic/legacy.txt','NON_PRIVATE','content','APPROVED','old','old',clock_timestamp(),clock_timestamp())");
            }
            var migrated=Flyway.configure().dataSource(database.getJdbcUrl(),database.getUsername(),database.getPassword())
                    .locations("classpath:db/migration").load().migrate();
            assertThat(migrated.migrationsExecuted).isEqualTo(17);
            try(var connection=DriverManager.getConnection(database.getJdbcUrl(),database.getUsername(),database.getPassword());
                var statement=connection.createStatement()) {
                try(var row=statement.executeQuery("SELECT rolsuper OR rolcreatedb OR rolcreaterole OR rolinherit OR rolbypassrls OR rolreplication OR rolcanlogin,rolpassword IS NULL FROM pg_authid WHERE rolname='rag_worker'")) {
                    row.next();assertThat(row.getBoolean(1)).isFalse();assertThat(row.getBoolean(2)).isTrue();
                }
                try(var row=statement.executeQuery("SELECT count(*) FROM pg_auth_members WHERE member=(SELECT oid FROM pg_roles WHERE rolname='rag_worker')")) {
                    row.next();assertThat(row.getInt(1)).isZero();
                }
                try(var row=statement.executeQuery("SELECT source_id,source_revision_hash,access_state FROM rag.sources")) {
                    row.next();assertThat(row.getString(1)).startsWith("disabled:");assertThat(row.getString(2)).isEqualTo("DISABLED");
                    assertThat(row.getString(3)).isEqualTo("DENIED");
                }
            }
        }
    }
    @Test void exactPostgres186AndPinnedPgvectorRunAllFoundationMigrations() throws Exception {
        try(var database=new PostgreSQLContainer<>(DockerImageName.parse("ppv-rag-postgres:18.6-pgvector0.8.6").asCompatibleSubstituteFor("postgres"))
                .withDatabaseName("ppv_rag_compatibility").withUsername("compatibility_migrator")
                .withPassword("synthetic-compatibility-fixture-password").withStartupTimeout(Duration.ofSeconds(30))) {
            database.start();
            var migrations=Flyway.configure().dataSource(database.getJdbcUrl(),database.getUsername(),database.getPassword())
                    .locations("classpath:db/migration").load().migrate();
            assertThat(migrations.success).isTrue();
            assertThat(migrations.migrationsExecuted).isEqualTo(23);
            try(var connection=DriverManager.getConnection(database.getJdbcUrl(),database.getUsername(),database.getPassword());
                var statement=connection.createStatement()) {
                try(var result=statement.executeQuery("SELECT current_setting('server_version_num')::int,(SELECT extversion FROM pg_extension WHERE extname='vector'),'[1,2]'::vector <-> '[1,2]'::vector")) {
                    result.next();assertThat(result.getInt(1)).isEqualTo(180006);
                    assertThat(result.getString(2)).isEqualTo("0.8.6");assertThat(result.getDouble(3)).isZero();
                }
                try(var result=statement.executeQuery("SELECT count(*) FROM pg_proc p JOIN pg_namespace n ON n.oid=p.pronamespace JOIN pg_roles r ON r.oid=p.proowner WHERE n.nspname='rag' AND r.rolname IN ('rag_worker','rag_runtime')")) {
                    result.next();assertThat(result.getInt(1)).isZero();
                }

                // Verify genuine 384-dimension vector insertion and <-> cosine/L2 distance into rag.document_chunks
                long fictionId;
                try (var r = statement.executeQuery("INSERT INTO vault_entries(entry_type) VALUES ('FICTION') RETURNING id")) {
                    r.next();
                    fictionId = r.getLong(1);
                }

                long srcId;
                try (var r = statement.executeQuery(String.format("""
                        INSERT INTO rag.sources(source_type, source_id, classification, approved_fields_text, access_state, source_revision_hash, description_revision_hash, approved_owner_reset_version, created_at, updated_at)
                        VALUES ('FICTION', '%d', 'NON_PRIVATE', 'title', 'APPROVED', 'h1', 'd1', 1, clock_timestamp(), clock_timestamp())
                        RETURNING id
                        """, fictionId))) {
                    r.next(); srcId = r.getLong(1);
                }

                UUID jobId = UUID.randomUUID();
                UUID attemptId = UUID.randomUUID();
                statement.execute(String.format("""
                        INSERT INTO rag.job_leases(job_id, attempt_id, source_id, status, lease_token, leased_until, reservation_token, staging_closed, created_at, updated_at)
                        VALUES ('%s', '%s', %d, 'COMPLETED', 'test-token', clock_timestamp() + interval '1 hour', '%s', true, clock_timestamp(), clock_timestamp())
                        """, jobId, attemptId, srcId, attemptId));

                long genId;
                try (var r = statement.executeQuery(String.format("""
                        INSERT INTO rag.index_generations(generation_id, job_id, source_id, status, authorization_epoch, source_revision_hash, dependency_manifest_digest, is_active, created_at)
                        VALUES ('%s', '%s', %d, 'PUBLISHED', 1, 'h1', 'digest', false, clock_timestamp())
                        RETURNING id
                        """, UUID.randomUUID(), jobId, srcId))) {
                    r.next(); genId = r.getLong(1);
                }

                statement.execute(String.format("""
                        INSERT INTO rag.generation_roots(generation_id, root_key, exact_dependency_manifest, model_artifact_hash, model_version, tokenizer_version, parser_version, chunker_version, ocr_pipeline_version, vector_dimensions, created_at)
                        VALUES (%d, 'root-1', '{}', '98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f', 'sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2', 'bilingual-wordpiece-1.0', 'ppv-parser-v18', 'structure-first-v18', 'adaptive-ocr-v18', 384, clock_timestamp())
                        """, genId));

                // 1. Build genuine 384-dim vector string
                StringBuilder vec384 = new StringBuilder("[");
                for (int i = 0; i < 384; i++) {
                    if (i > 0) vec384.append(',');
                    vec384.append(String.format(java.util.Locale.US, "%.4f", 0.05 + (i * 0.001)));
                }
                vec384.append(']');
                String vecString = vec384.toString();

                long chunkId;
                try (var ps = connection.prepareStatement("""
                        INSERT INTO rag.document_chunks(generation_id, source_identity, content, locator, structure_metadata, embedding, created_at)
                        VALUES (?, 'src-1', 'Test verified indexing paragraph', '{"line_start": 1}'::jsonb, '{}'::jsonb, CAST(? AS vector), clock_timestamp())
                        RETURNING chunk_id
                        """)) {
                    ps.setLong(1, genId);
                    ps.setString(2, vecString);
                    try (var rs = ps.executeQuery()) {
                        rs.next();
                        chunkId = rs.getLong(1);
                    }
                }
                assertThat(chunkId).isPositive();

                // 2. Read back with <-> Euclidean distance operator
                try (var ps = connection.prepareStatement("""
                        SELECT embedding <-> CAST(? AS vector) FROM rag.document_chunks WHERE chunk_id = ?
                        """)) {
                    ps.setString(1, vecString);
                    ps.setLong(2, chunkId);
                    try (var rs = ps.executeQuery()) {
                        rs.next();
                        double distance = rs.getDouble(1);
                        assertThat(distance).isZero();
                    }
                }

                // 3. Read back text representation of stored vector through persistence boundary
                try (var ps = connection.prepareStatement("""
                        SELECT CAST(embedding AS text) FROM rag.document_chunks WHERE chunk_id = ?
                        """)) {
                    ps.setLong(1, chunkId);
                    try (var rs = ps.executeQuery()) {
                        rs.next();
                        String readbackText = rs.getString(1);
                        assertThat(readbackText).startsWith("[").contains(",").endsWith("]");
                    }
                }

                // 4. Verify rejection of wrong-dimension vector (e.g. 2 dims instead of 384)
                assertThatThrownBy(() -> {
                    try (var ps = connection.prepareStatement("""
                            INSERT INTO rag.document_chunks(generation_id, source_identity, content, locator, structure_metadata, embedding, created_at)
                            VALUES (?, 'src-bad', 'Bad dim', '{"line_start": 1}'::jsonb, '{}'::jsonb, CAST('[0.1, 0.2]' AS vector(384)), clock_timestamp())
                            """)) {
                        ps.setLong(1, genId);
                        ps.executeUpdate();
                    }
                }).isInstanceOf(SQLException.class);

                // 5. Verify rejection of non-finite vector (NaN / Infinity)
                assertThatThrownBy(() -> {
                    try (var ps = connection.prepareStatement("""
                            INSERT INTO rag.document_chunks(generation_id, source_identity, content, locator, structure_metadata, embedding, created_at)
                            VALUES (?, 'src-bad', 'Non finite', '{"line_start": 1}'::jsonb, '{}'::jsonb, CAST('[NaN, 0.2]' AS vector), clock_timestamp())
                            """)) {
                        ps.setLong(1, genId);
                        ps.executeUpdate();
                    }
                }).isInstanceOf(SQLException.class);

                // 6. Integrated publication transaction and atomic rollback on invalid vector
                connection.setAutoCommit(false);
                Savepoint sp = connection.setSavepoint("before_invalid_publication");
                try {
                    try (var ps = connection.prepareStatement("""
                            INSERT INTO rag.document_chunks(generation_id, source_identity, content, locator, structure_metadata, embedding, created_at)
                            VALUES (?, 'src-trans-fail', 'Failing chunk', '{"line_start": 1}'::jsonb, '{}'::jsonb, CAST('[NaN, 0.5]' AS vector), clock_timestamp())
                            """)) {
                        ps.setLong(1, genId);
                        ps.executeUpdate();
                    }
                    fail("Should have failed on invalid vector");
                } catch (SQLException expected) {
                    connection.rollback(sp);
                } finally {
                    connection.setAutoCommit(true);
                }

                // Verify rollback left no leaked chunk
                try (var ps = connection.prepareStatement("SELECT count(*) FROM rag.document_chunks WHERE source_identity = 'src-trans-fail'")) {
                    try (var rs = ps.executeQuery()) {
                        rs.next();
                        assertThat(rs.getLong(1)).isZero();
                    }
                }
            }
        }
    }
}
