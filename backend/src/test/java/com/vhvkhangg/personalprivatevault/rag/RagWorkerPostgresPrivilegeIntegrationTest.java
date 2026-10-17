package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PSQLException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@org.junit.jupiter.api.Timeout(60)
class RagWorkerPostgresPrivilegeIntegrationTest extends AbstractEnabledRagIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private com.vhvkhangg.personalprivatevault.rag.api.RagOperations rag;

    private Connection createWorkerConnection() throws SQLException {
        return RagWorkerFixture.connection();
    }

    @Test
    @DisplayName("rag_worker is denied direct SELECT and DML on staged_outputs, sources, job_leases, and index_generations")
    void workerPrivilegesOnRagTables() throws SQLException {
        try (Connection conn = createWorkerConnection();
             Statement stmt = conn.createStatement()) {
            assertThatThrownBy(() -> stmt.executeQuery("SELECT count(*) FROM rag.staged_outputs"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for table staged_outputs");

            assertThatThrownBy(() -> stmt.executeQuery("SELECT count(*) FROM rag.job_leases"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for table job_leases");

            assertThatThrownBy(() -> stmt.executeQuery("SELECT count(*) FROM rag.sources"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for table sources");

            assertThatThrownBy(() -> stmt.executeQuery("SELECT count(*) FROM rag.index_generations"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for table index_generations");

            assertThatThrownBy(() -> stmt.executeUpdate("INSERT INTO rag.staged_outputs (job_id, attempt_id, stage_type, payload, lease_token) VALUES (gen_random_uuid(), gen_random_uuid(), 'CHUNK', 'x', 't')"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for table staged_outputs");

            assertThatThrownBy(() -> stmt.executeUpdate("DELETE FROM rag.staged_outputs"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for table staged_outputs");

            assertThatThrownBy(() -> stmt.executeUpdate("UPDATE rag.staged_outputs SET payload = 'tampered'"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for table staged_outputs");
        }
    }

    @Test
    @DisplayName("rag_worker writes only issued typed receipts with token, scope, digest and budget checks")
    void stageWorkerOutputFunctionEnforcement() throws Exception {
        Long genre=jdbcTemplate.queryForObject("INSERT INTO fiction_genres(name) VALUES ('Synthetic role genre') RETURNING id",Long.class);
        Long person=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('PERSON') RETURNING id",Long.class);
        jdbcTemplate.update("INSERT INTO persons(id,name) VALUES (?,'Synthetic role person')",person);
        Long id=jdbcTemplate.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FICTION') RETURNING id",Long.class);
        jdbcTemplate.update("INSERT INTO fictions(id,title,format,genre_id,author_person_id,is_nsfw) VALUES (?,'Synthetic','NOVEL',?,?,false)",id,genre,person);
        Long source=RagTestCommands.register(rag, com.vhvkhangg.personalprivatevault.rag.source.RagSourceType.FICTION,id.toString()).id();
        RagTestCommands.approve(rag, source,java.util.Set.of("title"));
        var attempt=RagTestCommands.index(rag, source);
        RagWorkerFixture.stage(rag,attempt);
        String staged=jdbcTemplate.queryForObject("SELECT payload FROM rag.staged_outputs WHERE job_id=?",String.class,attempt.jobId());
        var issued=new com.fasterxml.jackson.databind.ObjectMapper().readTree(staged);
        var receipt=new java.util.TreeMap<String,Object>(java.util.Map.of("contract_version","2.0","status","STAGED",
                "job_id",attempt.jobId().toString(),"attempt_id",attempt.attemptId().toString(),
                "stage_type",issued.path("stage_type").asText(),"input_digest",issued.path("input_digest").asText(),"scope_digest",issued.path("scope_digest").asText(),
                "output_digest",issued.path("output_digest").asText(),"output_kind","SYNTHETIC_DIGEST"));
        var mapper=new com.fasterxml.jackson.databind.ObjectMapper();
        try(var worker=createWorkerConnection();var query=worker.prepareStatement("SELECT rag.stage_worker_receipt(?,?,?,?)")) {
            query.setObject(1,attempt.jobId());query.setObject(2,attempt.attemptId());query.setString(3,attempt.leaseToken());
            query.setString(4,mapper.writeValueAsString(receipt));
            try(var result=query.executeQuery()){assertThat(result.next()).isTrue();assertThat(result.getLong(1)).isPositive();}
            query.setString(3,"wrong");
            assertThatThrownBy(query::executeQuery).isInstanceOf(PSQLException.class).hasMessageContaining("denied");
            query.setString(3,attempt.leaseToken());
            for(String field:java.util.List.of("stage_type","input_digest","scope_digest","output_digest")) {
                var wrong=new java.util.TreeMap<>(receipt);wrong.put(field,"invalid");
                query.setString(4,mapper.writeValueAsString(wrong));
                assertThatThrownBy(query::executeQuery).isInstanceOf(PSQLException.class);
            }
            query.setString(4,"x".repeat(4097));
            assertThatThrownBy(query::executeQuery).isInstanceOf(PSQLException.class).hasMessageContaining("Receipt budget");
            query.setObject(1,java.util.UUID.randomUUID());
            query.setString(4,mapper.writeValueAsString(receipt));
            assertThatThrownBy(query::executeQuery).isInstanceOf(PSQLException.class);
        }
    }

    @Test
    @DisplayName("rag_worker is strictly forbidden from reading vault_entries and application domain tables")
    void workerCannotReadDomainVaultTables() throws SQLException {
        try (Connection conn = createWorkerConnection();
             Statement stmt = conn.createStatement()) {

            assertThatThrownBy(() -> stmt.executeQuery("SELECT * FROM public.vault_entries"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for table vault_entries");

            assertThatThrownBy(() -> stmt.executeQuery("SELECT * FROM public.fictions"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for table fictions");

            assertThatThrownBy(() -> stmt.executeQuery("SELECT * FROM public.films"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for table films");

            assertThatThrownBy(() -> stmt.executeQuery("SELECT * FROM public.app_users"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for table app_users");
        }
    }

    @Test
    @DisplayName("rag_worker is strictly forbidden from executing DDL statements")
    void workerCannotExecuteDdl() throws SQLException {
        try (Connection conn = createWorkerConnection();
             Statement stmt = conn.createStatement()) {

            assertThatThrownBy(() -> stmt.execute("CREATE TABLE rag.worker_exploit (id int)"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for schema rag");

            assertThatThrownBy(() -> stmt.execute("CREATE TABLE public.worker_exploit (id int)"))
                    .isInstanceOf(PSQLException.class)
                    .hasMessageContaining("permission denied for schema public");
        }
    }
}
