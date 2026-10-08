package com.vhvkhangg.personalprivatevault.audit;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * BA15-1 regression test:
 * Verifies that Spring Boot-managed Flyway runs automatically on fresh empty PostgreSQL
 * before Hibernate schema validation without any manual test migrator prerequisite,
 * and that restart against migrated database succeeds.
 */
@SpringBootTest
@Testcontainers
class FlywayStartupIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> CLEAN_POSTGRES =
            new PostgreSQLContainer<>(DockerImageName.parse("postgres:18.6-alpine"))
                    .withDatabaseName("ppv_clean_startup")
                    .withUsername("ppv_clean")
                    .withPassword("ppv_clean_pass");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", CLEAN_POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", CLEAN_POSTGRES::getUsername);
        registry.add("spring.datasource.password", CLEAN_POSTGRES::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("ppv.security.jwt.secret", () -> "dGVzdC1zZWNyZXQta2V5LWZvci1wZXJzb25hbC1wcml2YXRlLXZhdWx0LXRlc3RzLTEyMzQ1Ng==");
    }

    @Autowired
    private Flyway flyway;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("BA15-1: Fresh startup applies migrations automatically and validates schema")
    void freshStartupAppliesFlywayMigrationsAndValidatesSchema() throws Exception {
        assertThat(applicationContext).isNotNull();
        assertThat(flyway).isNotNull();

        // Verify Flyway applied migrations
        var info = flyway.info();
        assertThat(info.applied()).isNotEmpty();
        assertThat(info.current().getVersion().getVersion()).isNotBlank();

        // Verify key tables exist and can be queried
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM flyway_schema_history WHERE success = true")) {
            assertThat(rs.next()).isTrue();
            assertThat(rs.getInt(1)).isGreaterThanOrEqualTo(2);
        }

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT count(*) FROM addresses")) {
            assertThat(rs.next()).isTrue();
        }
    }

    @Test
    @DisplayName("BA15-1: Actual restart against migrated database validates schema without re-running applied migrations")
    void restartAgainstMigratedDatabaseSucceeds() {
        try (var restartedContext = new org.springframework.boot.builder.SpringApplicationBuilder(
                com.vhvkhangg.personalprivatevault.PersonalPrivateVaultApplication.class)
                .run(
                        "--spring.datasource.url=" + CLEAN_POSTGRES.getJdbcUrl(),
                        "--spring.datasource.username=" + CLEAN_POSTGRES.getUsername(),
                        "--spring.datasource.password=" + CLEAN_POSTGRES.getPassword(),
                        "--spring.jpa.hibernate.ddl-auto=validate",
                        "--spring.flyway.enabled=true",
                        "--ppv.security.jwt.secret=dGVzdC1zZWNyZXQta2V5LWZvci1wZXJzb25hbC1wcml2YXRlLXZhdWx0LXRlc3RzLTEyMzQ1Ng=="
                )) {
            assertThat(restartedContext.isRunning()).isTrue();
            Flyway restartedFlyway = restartedContext.getBean(Flyway.class);
            var info = restartedFlyway.info();
            assertThat(info.pending()).isEmpty();
            assertThat(info.applied()).hasSizeGreaterThanOrEqualTo(2);
            assertThat(info.current().getVersion().getVersion()).isEqualTo("2");
        }
    }
}
