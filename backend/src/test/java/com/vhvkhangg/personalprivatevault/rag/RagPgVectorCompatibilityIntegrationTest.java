package com.vhvkhangg.personalprivatevault.rag;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import java.sql.*;
import java.time.Duration;
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
            assertThat(migrated.migrationsExecuted).isEqualTo(11);
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
            assertThat(migrations.migrationsExecuted).isEqualTo(17);
            try(var connection=DriverManager.getConnection(database.getJdbcUrl(),database.getUsername(),database.getPassword());
                var statement=connection.createStatement()) {
                try(var result=statement.executeQuery("SELECT current_setting('server_version_num')::int,(SELECT extversion FROM pg_extension WHERE extname='vector'),'[1,2]'::vector <-> '[1,2]'::vector")) {
                    result.next();assertThat(result.getInt(1)).isEqualTo(180006);
                    assertThat(result.getString(2)).isEqualTo("0.8.6");assertThat(result.getDouble(3)).isZero();
                }
                try(var result=statement.executeQuery("SELECT count(*) FROM pg_proc p JOIN pg_namespace n ON n.oid=p.pronamespace JOIN pg_roles r ON r.oid=p.proowner WHERE n.nspname='rag' AND r.rolname IN ('rag_worker','rag_runtime')")) {
                    result.next();assertThat(result.getInt(1)).isZero();
                }
            }
        }
    }
}
