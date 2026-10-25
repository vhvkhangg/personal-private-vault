package com.vhvkhangg.personalprivatevault.rag;

import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService;
import java.lang.reflect.Method;
import java.sql.*;
import java.util.UUID;

/** Production services use the actual isolated runtime; only fixture reset/migration use admin. */
@org.springframework.context.annotation.Import(AbstractRagIntegrationTest.RuntimeDatasource.class)
@org.springframework.boot.test.context.SpringBootTest(classes=com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class)
@ExtendWith(AbstractRagIntegrationTest.QueryCountInvocation.class)
abstract class AbstractRagIntegrationTest extends AbstractWebIntegrationTest {
    /** Only the frozen SessionFactory-wide query measurement needs recovery coordination. */
    static class QueryCountInvocation implements InvocationInterceptor {
        @Override
        public void interceptTestMethod(Invocation<Void> invocation,
                ReflectiveInvocationContext<Method> method, ExtensionContext context) throws Throwable {
            if (!context.getRequiredTestClass().getName().equals(
                    "com.vhvkhangg.personalprivatevault.rag.RagSourceAuthorityIntegrationTest")
                    || !method.getExecutable().getName().equals(
                    "genuinelyFreshBatchProjectionsHaveConstantQueryCountsAndEnforceByteBudget")) {
                invocation.proceed();
                return;
            }
            var recovery=SpringExtension.getApplicationContext(context).getBean(RagRecoveryService.class);
            // Intrinsic monitor release also propagates assertions, exceptions and timeouts.
            synchronized (recovery) {
                invocation.proceed();
            }
        }
    }
    static final java.nio.file.Path SNAPSHOT_TEST_ROOT;
    static {
        try { SNAPSHOT_TEST_ROOT=java.nio.file.Files.createTempDirectory("ppv-rag-integration-store-"); }
        catch (java.io.IOException failure) { throw new ExceptionInInitializerError(failure); }
    }
    static final String PASSWORD=UUID.randomUUID().toString().replace("-","");
    @org.springframework.beans.factory.annotation.Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagRuntimeIdentity fixtureRuntime;

    @org.junit.jupiter.api.BeforeEach
    void bindEmptyIsolatedFixtureToItsCurrentSpringContext() {
        // Cached Spring contexts share only this disposable test database. Never
        // steal an active slot or impersonate recovery of a real consumer.
        int bound=jdbcTemplate.update("""
                INSERT INTO rag.runtime_state(singleton,current_instance)
                SELECT true,CAST(? AS uuid)
                WHERE NOT EXISTS(SELECT 1 FROM rag.job_leases WHERE status IN
                    ('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING'))
                  AND NOT EXISTS(SELECT 1 FROM public.vault_rag_source_state WHERE reserved_by IS NOT NULL)
                ON CONFLICT(singleton) DO UPDATE SET current_instance=excluded.current_instance
                """,fixtureRuntime.instance());
        org.assertj.core.api.Assertions.assertThat(bound)
                .as("prior fixture must leave no unresolved active slot or source reservation").isEqualTo(1);
    }
    @org.springframework.boot.test.context.TestConfiguration(proxyBeanMethods=false)
    static class RuntimeDatasource {
        @org.springframework.context.annotation.Bean
        @org.springframework.context.annotation.Primary
        javax.sql.DataSource ragRuntimeDatasource(org.springframework.core.env.Environment environment) {
            var config=new com.zaxxer.hikari.HikariConfig();
            config.setJdbcUrl(environment.getProperty("ppv.rag.test-runtime-jdbc-url",POSTGRES.getJdbcUrl()));
            if (environment.containsProperty("ppv.rag.test-runtime-jdbc-url")) config.setConnectionTimeout(5000);
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setIdleTimeout(10000);
            config.setUsername("rag_runtime");config.setPassword(PASSWORD);
            return new com.zaxxer.hikari.HikariDataSource(config);
        }
    }
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        org.flywaydb.core.Flyway.configure().dataSource(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword())
                .locations("classpath:db/migration").load().migrate();
        try(var connection=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
            var statement=connection.createStatement()) {
            statement.execute("ALTER ROLE rag_runtime LOGIN PASSWORD '"+PASSWORD+"'");
        }catch(SQLException failure){throw new IllegalStateException(failure);}
        registry.add("ppv.rag.storage.snapshot-root",SNAPSHOT_TEST_ROOT::toString);
        registry.add("spring.datasource.url",POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username",()->"rag_runtime");
        registry.add("spring.datasource.password",()->PASSWORD);
        registry.add("spring.flyway.enabled",()->false);
        registry.add("ppv.security.jwt.secret",()->"dGVzdC1zZWNyZXQta2V5LWZvci1wZXJzb25hbC1wcml2YXRlLXZhdWx0LXRlc3RzLTEyMzQ1Ng==");
    }
    @Override
    @AfterEach
    protected void cleanUpDatabase() {
        try(var connection=DriverManager.getConnection(POSTGRES.getJdbcUrl(),POSTGRES.getUsername(),POSTGRES.getPassword());
            var statement=connection.createStatement()) {
            connection.setAutoCommit(false);
            statement.execute("SELECT pg_advisory_xact_lock("+com.vhvkhangg.personalprivatevault.rag.internal.application.RagDependencyGraph.LOCK_ID+")");
            // Administrative fixture reset only. Production source tombstones must never be truncated.
            statement.execute("TRUNCATE public.vault_rag_source_state,rag.external_file_sources,rag.batch_roots,rag.file_outcomes,rag.generation_roots,rag.document_chunks,rag.staged_document_outputs,rag.runs,rag.dependencies,rag.staged_outputs,rag.worker_grants,rag.job_leases,rag.index_generations,rag.sources,public.vault_entries,public.fiction_genres,public.film_genres CASCADE");
            connection.commit();

        }catch(SQLException failure){throw new IllegalStateException(failure);}
    }
}
