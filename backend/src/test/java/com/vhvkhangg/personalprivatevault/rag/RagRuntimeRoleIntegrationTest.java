package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.rag.api.RagOperations;
import com.vhvkhangg.personalprivatevault.rag.source.RagSourceType;
import com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.*;
import org.springframework.test.annotation.DirtiesContext;
import java.sql.*;
import java.util.*;
import static org.assertj.core.api.Assertions.*;

@Timeout(30)
@DirtiesContext(classMode=DirtiesContext.ClassMode.AFTER_CLASS)
@org.springframework.boot.test.context.SpringBootTest
@org.springframework.test.context.ActiveProfiles("rag-runtime")
class RagRuntimeRoleIntegrationTest {
    private static final org.testcontainers.containers.PostgreSQLContainer<?> POSTGRES=AbstractPostgresIntegrationTest.POSTGRES;
    static final String PASSWORD=AbstractRagIntegrationTest.PASSWORD;
    @Autowired JdbcTemplate jdbc;
    @Autowired RagOperations rag;
    static String runtimeUser() {
        try(var connection=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
            var statement=connection.createStatement()) {
            statement.execute("DO $$ BEGIN IF NOT EXISTS(SELECT 1 FROM pg_roles WHERE rolname='rag_runtime') THEN CREATE ROLE rag_runtime; END IF; END $$");
            statement.execute("ALTER ROLE rag_runtime LOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS NOREPLICATION PASSWORD '"+PASSWORD+"'");
            return "rag_runtime";
        }catch(SQLException failure){throw new IllegalStateException(failure);}
    }
    @DynamicPropertySource static void identities(DynamicPropertyRegistry properties) {
        org.flywaydb.core.Flyway.configure().dataSource(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword())
                .locations("classpath:db/migration").load().migrate();
        runtimeUser();
        properties.add("spring.datasource.url",POSTGRES::getJdbcUrl);
        properties.add("spring.datasource.username",()->"rag_runtime");
        properties.add("spring.datasource.password",()->PASSWORD);
        properties.add("spring.flyway.enabled",()->false);
        properties.add("ppv.rag.engine.service-key",()->"synthetic-runtime-profile-key-000000000");
        properties.add("ppv.security.jwt.secret",()->"dGVzdC1zZWNyZXQta2V5LWZvci1wZXJzb25hbC1wcml2YXRlLXZhdWx0LXRlc3RzLTEyMzQ1Ng==");
    }
    @BeforeEach void isolatedFixture() throws Exception {
        try(var connection=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
            var statement=connection.createStatement()) {
            statement.execute("TRUNCATE public.vault_rag_source_state,rag.runs,rag.dependencies,rag.staged_outputs,rag.job_leases,rag.index_generations,rag.sources,public.vault_entries,public.fiction_genres CASCADE");
        }
    }
    @Test void fullJavaAuthorityUsesOneNonOwnerRuntimeDatasourceWhileFlywayUsesMigrator() throws Exception {
        var provision=new ProcessBuilder("java","--class-path",System.getProperty("java.class.path"),
                "backend/dev/rag-db/ProvisionRagDevelopment.java");
        provision.directory(java.nio.file.Path.of("..").toAbsolutePath().normalize().toFile());
        provision.environment().put("PPV_RAG_MIGRATOR_URL",POSTGRES.getJdbcUrl().replace("localhost","127.0.0.1"));
        provision.environment().put("PPV_RAG_MIGRATOR_USERNAME",POSTGRES.getUsername());
        provision.environment().put("PPV_RAG_MIGRATOR_PASSWORD",POSTGRES.getPassword());
        provision.environment().put("PPV_RAG_RUNTIME_PASSWORD",PASSWORD);
        provision.environment().put("PPV_RAG_WORKER_PASSWORD",UUID.randomUUID().toString().replace("-",""));
        provision.redirectErrorStream(true);
        var process=provision.start();
        try {
            assertThat(process.waitFor(15,java.util.concurrent.TimeUnit.SECONDS)).isTrue();
            String output=new String(process.getInputStream().readAllBytes(),java.nio.charset.StandardCharsets.UTF_8);
            assertThat(process.exitValue()).as(output).isZero();
            assertThat(output).doesNotContain(PASSWORD);
        }finally {process.destroyForcibly();process.waitFor(3,java.util.concurrent.TimeUnit.SECONDS);}
        assertThat(jdbc.queryForObject("SELECT current_user",String.class)).isEqualTo("rag_runtime");
        assertThat(jdbc.queryForObject("SELECT rolname FROM pg_roles WHERE oid=(SELECT relowner FROM pg_class WHERE oid='rag.sources'::regclass)",String.class))
                .isEqualTo(POSTGRES.getUsername());
        assertThat(jdbc.queryForObject("SELECT rolsuper OR rolcreatedb OR rolcreaterole OR rolinherit OR rolbypassrls OR rolreplication FROM pg_roles WHERE rolname=current_user",Boolean.class)).isFalse();
        Long genre=jdbc.queryForObject("INSERT INTO fiction_genres(name) VALUES ('Synthetic runtime genre') RETURNING id",Long.class);
        Long person=jdbc.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('PERSON') RETURNING id",Long.class);
        jdbc.update("INSERT INTO persons(id,name) VALUES (?,'Synthetic runtime person')",person);
        Long domain=jdbc.queryForObject("INSERT INTO vault_entries(entry_type) VALUES ('FICTION') RETURNING id",Long.class);
        jdbc.update("INSERT INTO fictions(id,title,format,genre_id,author_person_id,is_nsfw) VALUES (?,'Synthetic runtime work','NOVEL',?,?,false)",domain,genre,person);
        var source=RagTestCommands.approve(rag, RagTestCommands.register(rag, RagSourceType.FICTION,domain.toString()).id(),Set.of("title"));
        var attempt=RagTestCommands.index(rag, source.id());
        assertThat(attempt.jobId()).isNotNull();
        rag.cancelJob(attempt.jobId());
        assertThat(rag.getJobStatus(attempt.jobId()).status()).isEqualTo("CANCELLED");
        assertThatThrownBy(()->jdbc.execute("CREATE TABLE rag.runtime_exploit(id int)")).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThatThrownBy(()->jdbc.execute("CREATE TEMP TABLE runtime_exploit(id int)")).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThatThrownBy(()->jdbc.execute("SET ROLE "+POSTGRES.getUsername())).isInstanceOf(org.springframework.dao.DataAccessException.class);
        assertThatThrownBy(()->jdbc.execute("UPDATE flyway_schema_history SET success=false")).isInstanceOf(org.springframework.dao.DataAccessException.class);
    }
}
