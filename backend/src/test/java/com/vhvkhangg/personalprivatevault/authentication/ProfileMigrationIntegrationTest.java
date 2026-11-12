package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.TokenGenerator;
import com.vhvkhangg.personalprivatevault.support.AbstractWebIntegrationTest;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProfileMigrationIntegrationTest extends AbstractWebIntegrationTest {

    @Autowired
    private TokenGenerator tokenGenerator;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final String DEFAULT_PRIVATE_PASS = "master-vault-password-1234";

    private void ensureOwnerExists() {
        Integer userCount = jdbcTemplate.queryForObject("SELECT count(*) FROM app_users WHERE id = 1", Integer.class);
        String hash = passwordEncoder.encode(DEFAULT_PRIVATE_PASS);
        if (userCount == null || userCount == 0) {
            jdbcTemplate.update("""
                    INSERT INTO app_users (id, email, username, password_hash, pin_hash, created_at, updated_at)
                    VALUES (1, 'owner@vault.local', 'owner', ?, 'pinhash', now(), now())
                    ON CONFLICT (id) DO UPDATE SET password_hash = ?
                    """, hash, hash);
            jdbcTemplate.update("""
                    INSERT INTO ppv_public_login_credential (owner_id, credential_epoch, created_at, updated_at)
                    VALUES (1, 0, now(), now())
                    ON CONFLICT (owner_id) DO NOTHING
                    """);
        } else {
            jdbcTemplate.update("UPDATE app_users SET password_hash = ? WHERE id = 1", hash);
        }
    }

    @Test
    @DisplayName("DPA-01: Schema check constraints enforce owner_id = 1, epoch state, and session invariants")
    void schemaConstraintsEnforced() {
        ensureOwnerExists();

        // 1. Cannot insert public credential with invalid epoch state (epoch > 0 but password_hash is null)
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_public_login_credential (owner_id, password_hash, credential_epoch, created_at, updated_at)
                VALUES (1, null, 5, now(), now())
                ON CONFLICT (owner_id) DO UPDATE SET credential_epoch = 5, password_hash = null
                """))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 2. Cannot insert login session with invalid profile name
        UUID sid = UUID.randomUUID();
        Instant now = Instant.now();
        Instant exp = now.plus(30, ChronoUnit.DAYS);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_login_sessions (id, owner_id, active_profile, generation, public_credential_epoch, current_refresh_token_id, created_at, expires_at)
                VALUES (?, 1, 'INVALID_PROFILE', 1, null, 1, ?, ?)
                """, sid, Timestamp.from(now), Timestamp.from(exp)))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 3. Cannot insert login session with generation <= 0
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_login_sessions (id, owner_id, active_profile, generation, public_credential_epoch, current_refresh_token_id, created_at, expires_at)
                VALUES (?, 1, 'PRIVATE', 0, null, 1, ?, ?)
                """, sid, Timestamp.from(now), Timestamp.from(exp)))
                .isInstanceOf(DataIntegrityViolationException.class);

        // 4. Cannot insert login session with expires_at <= created_at
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_login_sessions (id, owner_id, active_profile, generation, public_credential_epoch, current_refresh_token_id, created_at, expires_at)
                VALUES (?, 1, 'PRIVATE', 1, null, 1, ?, ?)
                """, sid, Timestamp.from(now), Timestamp.from(now.minusSeconds(10))))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("DPA-01, FR-DPA-07: Circular deferred FK constraint between refresh_tokens and ppv_login_sessions enforced at commit, not insert")
    void circularDeferredFkEnforcedAtCommit() {
        ensureOwnerExists();

        UUID sid = UUID.randomUUID();
        Instant now = Instant.now();
        Instant exp = now.plus(30, ChronoUnit.DAYS);
        TransactionTemplate tx = new TransactionTemplate(fixtureTransactions);

        // 1. Inserting only refresh_tokens referencing non-existent session: flush succeeds, commit fails with deferred constraint
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            jdbcTemplate.update("""
                    INSERT INTO refresh_tokens (user_id, session_id, profile, session_generation, token_hash, expires_at, created_at)
                    VALUES (1, ?, 'PRIVATE', 1, 'dummyhash-1', ?, ?)
                    """, sid, Timestamp.from(exp), Timestamp.from(now));
        }))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_refresh_session_owner");

        // 2. Inserting only ppv_login_sessions referencing non-existent refresh token: flush succeeds, commit fails with deferred constraint
        assertThatThrownBy(() -> tx.executeWithoutResult(status -> {
            jdbcTemplate.update("""
                    INSERT INTO ppv_login_sessions (id, owner_id, active_profile, generation, public_credential_epoch, current_refresh_token_id, created_at, expires_at)
                    VALUES (?, 1, 'PRIVATE', 1, null, 99999999, ?, ?)
                    """, sid, Timestamp.from(now), Timestamp.from(exp));
        }))
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasMessageContaining("fk_session_current_refresh");

        // 3. Mutually referencing rows inserted in same transaction commit successfully
        UUID validSid = UUID.randomUUID();
        tx.executeWithoutResult(status -> {
            Long tid = jdbcTemplate.queryForObject("""
                    INSERT INTO refresh_tokens (user_id, session_id, profile, session_generation, token_hash, expires_at, created_at)
                    VALUES (1, ?, 'PRIVATE', 1, 'dummyhash-valid', ?, ?)
                    RETURNING id
                    """, Long.class, validSid, Timestamp.from(exp), Timestamp.from(now));

            jdbcTemplate.update("""
                    INSERT INTO ppv_login_sessions (id, owner_id, active_profile, generation, public_credential_epoch, current_refresh_token_id, created_at, expires_at)
                    VALUES (?, 1, 'PRIVATE', 1, null, ?, ?, ?)
                    """, validSid, tid, Timestamp.from(now), Timestamp.from(exp));
        });

        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM ppv_login_sessions WHERE id = ?", Integer.class, validSid);
        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("DPA-01, FR-DPA-07: Legacy unprofiled tokens satisfy ck_refresh_profile_binding when revoked")
    void legacyRevokedTokensSatisfyProfileBindingConstraint() {
        ensureOwnerExists();

        Instant now = Instant.now();
        Instant exp = now.plus(30, ChronoUnit.DAYS);

        // Legacy V28 token (session_id IS NULL, profile IS NULL, session_generation IS NULL) can be inserted if revoked_at IS NOT NULL
        jdbcTemplate.update("""
                INSERT INTO refresh_tokens (user_id, session_id, profile, session_generation, token_hash, expires_at, created_at, revoked_at)
                VALUES (1, null, null, null, 'legacy-hash', ?, ?, ?)
                """, Timestamp.from(exp), Timestamp.from(now), Timestamp.from(now));

        // Active unprofiled token (revoked_at IS NULL) must violate ck_refresh_profile_binding
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO refresh_tokens (user_id, session_id, profile, session_generation, token_hash, expires_at, created_at, revoked_at)
                VALUES (1, null, null, null, 'active-legacy-hash', ?, ?, null)
                """, Timestamp.from(exp), Timestamp.from(now)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("DPA-16, FR-DPA-07, FR-DPA-08: Disaster recovery invalidation runbook invalidates live sessions and tokens, and fresh login recovers")
    void disasterRecoveryInvalidationRunbookExecution() throws Exception {
        ensureOwnerExists();

        TransactionTemplate tx = new TransactionTemplate(fixtureTransactions);
        Instant now = Instant.now();
        Instant exp = now.plus(30, ChronoUnit.DAYS);

        // 1. Set configured state
        jdbcTemplate.update("UPDATE ppv_public_login_credential SET password_hash = 'validhash', credential_epoch = 1 WHERE owner_id = 1");

        // 2. Insert live PRIVATE session and bound refresh token with real hash
        String rawPrivateRefresh = tokenGenerator.generateRawRefreshToken();
        String hashedPrivateRefresh = tokenGenerator.hashToken(rawPrivateRefresh);
        UUID privateSid = UUID.randomUUID();
        tx.executeWithoutResult(status -> {
            Long tid = jdbcTemplate.queryForObject("""
                    INSERT INTO refresh_tokens (user_id, session_id, profile, session_generation, token_hash, expires_at, created_at, revoked_at)
                    VALUES (1, ?, 'PRIVATE', 1, ?, ?, ?, null)
                    RETURNING id
                    """, Long.class, privateSid, hashedPrivateRefresh, Timestamp.from(exp), Timestamp.from(now));

            jdbcTemplate.update("""
                    INSERT INTO ppv_login_sessions (id, owner_id, active_profile, generation, public_credential_epoch, current_refresh_token_id, created_at, expires_at, revoked_at)
                    VALUES (?, 1, 'PRIVATE', 1, null, ?, ?, ?, null)
                    """, privateSid, tid, Timestamp.from(now), Timestamp.from(exp));
        });

        // 3. Insert live PUBLIC session and bound refresh token with real hash
        String rawPublicRefresh = tokenGenerator.generateRawRefreshToken();
        String hashedPublicRefresh = tokenGenerator.hashToken(rawPublicRefresh);
        UUID publicSid = UUID.randomUUID();
        tx.executeWithoutResult(status -> {
            Long tid = jdbcTemplate.queryForObject("""
                    INSERT INTO refresh_tokens (user_id, session_id, profile, session_generation, token_hash, expires_at, created_at, revoked_at)
                    VALUES (1, ?, 'PUBLIC', 1, ?, ?, ?, null)
                    RETURNING id
                    """, Long.class, publicSid, hashedPublicRefresh, Timestamp.from(exp), Timestamp.from(now));

            jdbcTemplate.update("""
                    INSERT INTO ppv_login_sessions (id, owner_id, active_profile, generation, public_credential_epoch, current_refresh_token_id, created_at, expires_at, revoked_at)
                    VALUES (?, 1, 'PUBLIC', 1, 1, ?, ?, ?, null)
                    """, publicSid, tid, Timestamp.from(now), Timestamp.from(exp));
        });

        // Assert live unrevoked rows exist
        Integer activeSessionsBefore = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ppv_login_sessions WHERE revoked_at IS NULL", Integer.class);
        Integer activeTokensBefore = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM refresh_tokens WHERE revoked_at IS NULL", Integer.class);
        assertThat(activeSessionsBefore).isGreaterThanOrEqualTo(2);
        assertThat(activeTokensBefore).isGreaterThanOrEqualTo(2);

        // 4. Execute the EXACT Disaster Recovery Runbook SQL (Runbook lines 45-53)
        tx.executeWithoutResult(status -> {
            // Revoke all active login sessions
            jdbcTemplate.update("UPDATE ppv_login_sessions SET revoked_at = now() WHERE revoked_at IS NULL");
            // Revoke all refresh tokens
            jdbcTemplate.update("UPDATE refresh_tokens SET revoked_at = now() WHERE revoked_at IS NULL");
            // Bump credential epoch to invalidate any ever-PUBLIC tokens if public credential was configured
            jdbcTemplate.update("UPDATE ppv_public_login_credential SET credential_epoch = credential_epoch + 1 WHERE owner_id = 1 AND password_hash IS NOT NULL");
        });

        // 5. Assert all sessions and tokens are revoked, and epoch is incremented
        Integer activeSessionsAfter = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ppv_login_sessions WHERE revoked_at IS NULL", Integer.class);
        Integer activeTokensAfter = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM refresh_tokens WHERE revoked_at IS NULL", Integer.class);
        Long epochConfigured = jdbcTemplate.queryForObject(
                "SELECT credential_epoch FROM ppv_public_login_credential WHERE owner_id = 1", Long.class);

        assertThat(activeSessionsAfter).isEqualTo(0);
        assertThat(activeTokensAfter).isEqualTo(0);
        assertThat(epochConfigured).isEqualTo(2L);

        // 6. Verify attempting to refresh with revoked live token yields 401 AUTH_INVALID_REFRESH_TOKEN
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"refreshToken\":\"" + rawPrivateRefresh + "\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.error.code").value("AUTH_INVALID_REFRESH_TOKEN"));

        // 6b. Verify fresh PRIVATE login recovers and succeeds after invalidation
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content("{\"identifier\":\"owner\",\"password\":\"" + DEFAULT_PRIVATE_PASS + "\"}"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.accessToken").isString());

        // 7. Case of Unconfigured state runbook execution: epoch must remain 0
        jdbcTemplate.update("UPDATE ppv_public_login_credential SET password_hash = NULL, credential_epoch = 0 WHERE owner_id = 1");
        tx.executeWithoutResult(status -> {
            jdbcTemplate.update("UPDATE ppv_login_sessions SET revoked_at = now() WHERE revoked_at IS NULL");
            jdbcTemplate.update("UPDATE refresh_tokens SET revoked_at = now() WHERE revoked_at IS NULL");
            jdbcTemplate.update("UPDATE ppv_public_login_credential SET credential_epoch = credential_epoch + 1 WHERE owner_id = 1 AND password_hash IS NOT NULL");
        });
        Long epochUnconfigured = jdbcTemplate.queryForObject(
                "SELECT credential_epoch FROM ppv_public_login_credential WHERE owner_id = 1", Long.class);
        assertThat(epochUnconfigured).isEqualTo(0L);
    }


    @Autowired private com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.JwtProperties jwtProperties;

    private String databaseUrl(String name) {
        String base = POSTGRES.getJdbcUrl();
        return base.substring(0, base.lastIndexOf('/')) + "/" + name;
    }

    private void databaseCommand(String sql) throws Exception {
        try (var conn = DriverManager.getConnection(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword());
             var statement = conn.createStatement()) { statement.execute(sql); }
    }

    private org.springframework.test.web.servlet.MockMvc restoredMvc(org.springframework.context.ConfigurableApplicationContext app) {
        return org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup((org.springframework.web.context.WebApplicationContext)app)
                .apply(org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity()).build();
    }

    private String runbookSql() throws Exception {
        var root = java.nio.file.Path.of("").toAbsolutePath();
        if (root.getFileName().toString().equals("backend")) root = root.getParent();
        String document = java.nio.file.Files.readString(root.resolve("docs/implementation/maintenance/2026-11-11-dual-login-profiles-preparation/dp-a-cutover-and-recovery-2026-11-11.md"));
        var matcher = java.util.regex.Pattern.compile("```sql\\s*(BEGIN;.*?)```", java.util.regex.Pattern.DOTALL).matcher(document);
        assertThat(matcher.find()).as("actual transactional invalidation block exists in runbook").isTrue();
        return matcher.group(1).strip();
    }

    @Test
    @DisplayName("DPA-01: populated actual V28 to V29 preserves domain/settings/FKs/replacement history and usable credentials")
    void syntheticV28ToV29CutoverPreservesDataAndRevokesLegacyTokens() throws Exception {
        String name = "dpa_upgrade_" + UUID.randomUUID().toString().replace("-", "");
        databaseCommand("CREATE DATABASE " + name);
        try {
            var ds = new DriverManagerDataSource(databaseUrl(name), POSTGRES.getUsername(), POSTGRES.getPassword());
            var jdbc = new JdbcTemplate(ds);
            Flyway.configure().dataSource(ds).target("28").load().migrate();
            String passwordHash = passwordEncoder.encode(DEFAULT_PRIVATE_PASS);
            String pinHash = passwordEncoder.encode("123456");
            jdbc.update("INSERT INTO app_users(id,email,username,password_hash,pin_hash) VALUES (1,'synthetic@vault.local','owner',?,?)", passwordHash, pinHash);
            com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbc);
            var domainBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbc);
            var checksumsBefore = jdbc.queryForList("SELECT version,checksum FROM flyway_schema_history WHERE version IS NOT NULL ORDER BY installed_rank");
            Instant now = Instant.now().truncatedTo(ChronoUnit.SECONDS);
            String activeRaw = tokenGenerator.generateRawRefreshToken();
            String consumedRaw = tokenGenerator.generateRawRefreshToken();
            String revokedRaw = tokenGenerator.generateRawRefreshToken();
            // Actual V28 replacement chain, active successor and independently revoked token.
            long successor = jdbc.queryForObject("INSERT INTO refresh_tokens(user_id,token_hash,expires_at,created_at) VALUES (1,?,?,?) RETURNING id", Long.class,
                    tokenGenerator.hashToken(activeRaw), Timestamp.from(now.plus(30,ChronoUnit.DAYS)), Timestamp.from(now));
            jdbc.update("INSERT INTO refresh_tokens(user_id,token_hash,expires_at,created_at,revoked_at,replaced_by_token_id) VALUES (1,?,?,?,?,?)",
                    tokenGenerator.hashToken(consumedRaw), Timestamp.from(now.plus(30,ChronoUnit.DAYS)), Timestamp.from(now.minusSeconds(60)), Timestamp.from(now), successor);
            jdbc.update("INSERT INTO refresh_tokens(user_id,token_hash,expires_at,created_at,revoked_at) VALUES (1,?,?,?,?)",
                    tokenGenerator.hashToken(revokedRaw), Timestamp.from(now.plus(30,ChronoUnit.DAYS)), Timestamp.from(now.minusSeconds(60)), Timestamp.from(now));
            var tokenBefore = jdbc.queryForList("SELECT id,user_id,token_hash,expires_at,created_at,replaced_by_token_id FROM refresh_tokens ORDER BY id");
            var revokedBefore = jdbc.queryForList("SELECT id,revoked_at FROM refresh_tokens WHERE revoked_at IS NOT NULL ORDER BY id");
            var result = Flyway.configure().dataSource(ds).target("29").load().migrate();
            assertThat(result.success).isTrue(); assertThat(result.migrationsExecuted).isEqualTo(1);
            assertThat(com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbc)).isEqualTo(domainBefore);
            assertThat(jdbc.queryForList("SELECT version,checksum FROM flyway_schema_history WHERE version::int <= 28 ORDER BY installed_rank")).isEqualTo(checksumsBefore);
            assertThat(jdbc.queryForList("SELECT id,user_id,token_hash,expires_at,created_at,replaced_by_token_id FROM refresh_tokens ORDER BY id")).isEqualTo(tokenBefore);
            assertThat(jdbc.queryForList("SELECT id,revoked_at FROM refresh_tokens WHERE id <> ? ORDER BY id", successor)).isEqualTo(revokedBefore);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM refresh_tokens WHERE revoked_at IS NULL", Integer.class)).isZero();
            assertThat(jdbc.queryForObject("SELECT credential_epoch FROM ppv_public_login_credential", Long.class)).isZero();
            assertThat(jdbc.queryForObject("SELECT password_hash FROM ppv_public_login_credential", String.class)).isNull();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM ppv_login_sessions", Integer.class)).isZero();
            assertThat(jdbc.queryForObject("SELECT count(*) FROM pg_constraint WHERE contype='f' AND NOT convalidated", Integer.class)).isZero();
            assertThat(passwordEncoder.matches(DEFAULT_PRIVATE_PASS, jdbc.queryForObject("SELECT password_hash FROM app_users", String.class))).isTrue();
            assertThat(passwordEncoder.matches("123456", jdbc.queryForObject("SELECT pin_hash FROM app_users", String.class))).isTrue();
            try (var app = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.restoredApplication(databaseUrl(name), POSTGRES.getUsername(), POSTGRES.getPassword(), jwtProperties.getSecret())) {
                var mvc = restoredMvc(app);
                for (String raw : java.util.List.of(activeRaw, consumedRaw, revokedRaw)) mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + raw + "\"}"))
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
                var login = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"identifier\":\"owner\",\"password\":\"" + DEFAULT_PRIVATE_PASS + "\"}"))
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andReturn();
                String bearer = objectMapper.readTree(login.getResponse().getContentAsString()).path("data").path("accessToken").asText();
                mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/private-pin/verify")
                        .header("Authorization", "Bearer " + bearer).contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"pin\":\"123456\"}"))
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk())
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.data.verified").value(true));
            }
        } finally { databaseCommand("DROP DATABASE " + name + " WITH (FORCE)"); }
    }

    @Test
    @DisplayName("DPA-16: real pg_dump/pg_restore, restored admission, exact invalidation, key rotation and fresh PRIVATE login")
    void disposablePostgresqlBackupRestoreAndKeyRotation() throws Exception {
        String privateBearer = bearerToken();
        publicBearerToken(); // legitimate setup; no credential-row synthesis
        var purePrivate = sessionOperations.login(new com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand(DEFAULT_OWNER_USERNAME, DEFAULT_OWNER_PASSWORD));
        var publicPair = profileSessionService.publicLogin(DEFAULT_PUBLIC_PASSWORD);
        var everPublic = profileSessionService.publicLogin(DEFAULT_PUBLIC_PASSWORD);
        var publicJwt = jwtDecoder.decode(everPublic.accessToken());
        var elevated = profileSessionService.elevatePrivate(UUID.fromString(publicJwt.getClaimAsString("sid")), 1, DEFAULT_OWNER_PASSWORD);
        com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.seedDomain(jdbcTemplate);
        var domainBefore = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbcTemplate);
        String name = "dpa_restore_" + UUID.randomUUID().toString().replace("-", "");
        String dump = "/tmp/" + name + ".dump";
        databaseCommand("CREATE DATABASE " + name);
        try {
            var backup = POSTGRES.execInContainer("pg_dump", "-U", POSTGRES.getUsername(), "-d", POSTGRES.getDatabaseName(), "-Fc", "-f", dump);
            assertThat(backup.getExitCode()).as("pg_dump exit").isZero();
            var restore = POSTGRES.execInContainer("pg_restore", "-U", POSTGRES.getUsername(), "-d", name, "--no-owner", "--no-privileges", "--exit-on-error", dump);
            assertThat(restore.getExitCode()).as("pg_restore exit: %s", restore.getStderr()).isZero();
            var jdbc = new JdbcTemplate(new DriverManagerDataSource(databaseUrl(name), POSTGRES.getUsername(), POSTGRES.getPassword()));
            assertThat(com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbc)).isEqualTo(domainBefore);
            assertThat(jdbc.queryForObject("SELECT count(*) FROM ppv_login_sessions WHERE revoked_at IS NULL", Integer.class)).isGreaterThanOrEqualTo(3);
            var pairs = java.util.List.of(purePrivate, publicPair, elevated);
            String restoredSuccessorRefresh;
            try (var app = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.restoredApplication(databaseUrl(name), POSTGRES.getUsername(), POSTGRES.getPassword(), jwtProperties.getSecret())) {
                var mvc = restoredMvc(app);
                for (var pair : pairs) mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries")
                        .header("Authorization", "Bearer " + pair.accessToken()))
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().is(pair == publicPair ? 403 : 200));
                var rotated = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + purePrivate.refreshToken() + "\"}"))
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andReturn();
                restoredSuccessorRefresh = objectMapper.readTree(rotated.getResponse().getContentAsString()).path("data").path("refreshToken").asText();
                jdbc.execute(runbookSql());
                assertThat(jdbc.queryForObject("SELECT count(*) FROM ppv_login_sessions WHERE revoked_at IS NULL", Integer.class)).isZero();
                assertThat(jdbc.queryForObject("SELECT count(*) FROM refresh_tokens WHERE revoked_at IS NULL", Integer.class)).isZero();
                assertThat(jdbc.queryForObject("SELECT credential_epoch FROM ppv_public_login_credential", Long.class)).isEqualTo(2);
                // Same signing key here isolates persisted authority invalidation from signature rejection.
                for (var pair : pairs) mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries")
                        .header("Authorization", "Bearer " + pair.accessToken()))
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
            }
            byte[] newKey = new byte[32]; new java.security.SecureRandom().nextBytes(newKey);
            String rotatedKey = java.util.Base64.getEncoder().encodeToString(newKey);
            assertThat(rotatedKey).isNotEqualTo(jwtProperties.getSecret());
            try (var app = com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.restoredApplication(databaseUrl(name), POSTGRES.getUsername(), POSTGRES.getPassword(), rotatedKey)) {
                var mvc = restoredMvc(app);
                var decoder = app.getBean(org.springframework.security.oauth2.jwt.JwtDecoder.class);
                for (var pair : pairs) {
                    assertThatThrownBy(() -> decoder.decode(pair.accessToken())).isInstanceOf(org.springframework.security.oauth2.jwt.JwtException.class);
                    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries").header("Authorization", "Bearer " + pair.accessToken()))
                            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
                    mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                            .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + pair.refreshToken() + "\"}"))
                            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
                }
                mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/refresh")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"refreshToken\":\"" + restoredSuccessorRefresh + "\"}"))
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isUnauthorized());
                var fresh = mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/auth/login")
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON).content("{\"identifier\":\"" + DEFAULT_OWNER_USERNAME + "\",\"password\":\"" + DEFAULT_OWNER_PASSWORD + "\"}"))
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk()).andReturn();
                String token = objectMapper.readTree(fresh.getResponse().getContentAsString()).path("data").path("accessToken").asText();
                assertThat(decoder.decode(token).getClaimAsString("profile")).isEqualTo("PRIVATE");
                assertThatThrownBy(() -> jwtDecoder.decode(token)).isInstanceOf(org.springframework.security.oauth2.jwt.JwtException.class);
                mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries").header("Authorization", "Bearer " + token))
                        .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
                assertThat(com.vhvkhangg.personalprivatevault.authentication.fixture.ProfileAuthFixture.domainSnapshot(jdbc)).isEqualTo(domainBefore);
            }
            // Target-only recovery operations must not revoke source database authority.
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/v1/reference/countries").header("Authorization", "Bearer " + privateBearer))
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        } finally {
            assertThat(POSTGRES.execInContainer("rm", "-f", dump).getExitCode()).isZero();
            databaseCommand("DROP DATABASE " + name + " WITH (FORCE)");
        }
    }
}
