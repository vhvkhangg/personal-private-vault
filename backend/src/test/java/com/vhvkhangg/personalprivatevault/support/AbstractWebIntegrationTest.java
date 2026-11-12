package com.vhvkhangg.personalprivatevault.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vhvkhangg.personalprivatevault.authentication.internal.application.token.JwtTokenService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

/**
 * Base class for web / MockMvc integration tests backed by Testcontainers PostgreSQL and Spring Security.
 */
public abstract class AbstractWebIntegrationTest extends AbstractPostgresIntegrationTest {

    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    @Autowired
    protected JwtTokenService jwtTokenService;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired(required = false)
    protected com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapOperations bootstrapOperations;

    @Autowired(required = false)
    protected com.vhvkhangg.personalprivatevault.authentication.session.SessionOperations sessionOperations;

    @Autowired(required = false)
    protected com.vhvkhangg.personalprivatevault.authentication.internal.application.session.ProfileSessionService profileSessionService;

    @Autowired(required = false)
    protected org.springframework.security.oauth2.jwt.JwtDecoder jwtDecoder;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private com.vhvkhangg.personalprivatevault.rag.internal.application.RagRecoveryService ragRecovery;
    @Autowired
    protected org.springframework.transaction.PlatformTransactionManager fixtureTransactions;

    @BeforeEach
    void setUpMockMvcAndReferenceData() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        cleanUpDatabase();
        // Multiple cached Spring test contexts share this disposable database. Reinitialize the
        // current empty fixture through the production startup fence, rather than replay consent.
        ragRecovery.afterSingletonsInstantiated();

        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('US', 'United States', 'Hoa Kỳ')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO countries (code, name_en, name_vi) VALUES ('VN', 'Vietnam', 'Việt Nam')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol) VALUES ('USD', 'US Dollar', '$')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO currencies (code, name, symbol) VALUES ('VND', 'Vietnamese Dong', '₫')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO languages (code, name_en, name_vi) VALUES ('en', 'English', 'Tiếng Anh')
                ON CONFLICT (code) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO platforms (name, kind, url) VALUES ('Web Platform', 'WEB', 'https://platform.example.com')
                ON CONFLICT (name) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO story_archetypes (name, description) VALUES ('Hero Journey', 'Classic monomyth')
                ON CONFLICT (name) DO NOTHING
                """);
        jdbcTemplate.update("""
                INSERT INTO world_settings (name, description) VALUES ('Cyberpunk Metropolis', 'High-tech neon dystopia')
                ON CONFLICT (name) DO NOTHING
                """);
    }

    @AfterEach
    protected void cleanUpDatabase() {
        new org.springframework.transaction.support.TransactionTemplate(fixtureTransactions).executeWithoutResult(status -> {
        // Serialize disposable test-table reset with asynchronous RAG cleanup's production barrier.
        jdbcTemplate.queryForObject("SELECT pg_advisory_xact_lock(?)",Object.class,
                com.vhvkhangg.personalprivatevault.rag.internal.application.RagDependencyGraph.LOCK_ID);
        jdbcTemplate.execute("""
                TRUNCATE TABLE
                    public.vault_rag_source_state,
                    rag.runs,
                    rag.dependencies,
                    rag.staged_outputs,
                    rag.job_leases,
                    rag.index_generations,
                    rag.sources,
                    follower_snapshot_entries,
                    follower_snapshots,
                    external_account_relationships,
                    external_accounts,
                    saved_resource_conversions,
                    saved_resources,
                    feed_items,
                    feed_sources,
                    import_job_items,
                    import_jobs,
                    financial_transaction_entries,
                    recurring_rule_entries,
                    recurring_rule_weekdays,
                    recurring_transaction_rules,
                    subscriptions,
                    financial_transactions,
                    wallets,
                    transaction_categories,
                    diary_entries,
                    personal_profiles,
                    vocabulary_reviews,
                    vocabulary_items,
                    information_items,
                    notes,
                    study_items,
                    music_track_people,
                    music_tracks,
                    shopping_items,
                    software_item_platforms,
                    software_items,
                    location_dining_service_styles,
                    location_business_hours,
                    location_category_assignments,
                    locations,
                    location_categories,
                    addresses,
                    brands,
                    images,
                    albums,
                    film_credits,
                    film_links,
                    film_world_settings,
                    film_story_archetypes,
                    film_genre_assignments,
                    films,
                    film_genres,
                    fiction_links,
                    fiction_world_settings,
                    fiction_story_archetypes,
                    fictions,
                    fiction_genres,
                    creator_group_members,
                    creator_groups,
                    person_roles,
                    persons,
                    vault_entry_tags,
                    tags,
                    favorites,
                    ratings,
                    vault_entries,
                    ppv_login_sessions,
                    ppv_public_login_credential,
                    refresh_tokens,
                    app_users,
                    app_settings
                CASCADE
                """);
            cachedBearerToken = null;
        });
    }

    protected static final String DEFAULT_OWNER_EMAIL = "owner@vault.local";
    protected static final String DEFAULT_OWNER_USERNAME = "vaultowner";
    protected static final String DEFAULT_OWNER_PASSWORD = "MasterVaultPassword123!";
    protected static final String DEFAULT_OWNER_PIN = "123456";
    protected static final String DEFAULT_PUBLIC_PASSWORD = "PublicPass123!";

    private String cachedBearerToken;

    protected String bearerToken() {
        if (cachedBearerToken != null) {
            return cachedBearerToken;
        }

        if (bootstrapOperations != null && !bootstrapOperations.isBootstrapped()) {
            bootstrapOperations.bootstrap(new com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand(
                    DEFAULT_OWNER_EMAIL,
                    DEFAULT_OWNER_USERNAME,
                    DEFAULT_OWNER_PASSWORD,
                    DEFAULT_OWNER_PIN
            ));
        }

        if (sessionOperations != null) {
            try {
                var tokens = sessionOperations.login(new com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand(
                        DEFAULT_OWNER_USERNAME,
                        DEFAULT_OWNER_PASSWORD
                ));
                cachedBearerToken = tokens.accessToken();
                return cachedBearerToken;
            } catch (Exception e) {
                var tokens = sessionOperations.login(new com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand(
                        "owner",
                        "VaultPass123!"
                ));
                cachedBearerToken = tokens.accessToken();
                return cachedBearerToken;
            }
        }

        throw new IllegalStateException("Neither sessionOperations nor bootstrapOperations available");
    }

    protected String publicBearerToken() {
        // Legitimate Origin 1: Configured PUBLIC profile login
        String privateToken = bearerToken();
        org.springframework.security.oauth2.jwt.Jwt jwt = jwtDecoder.decode(privateToken);
        java.util.UUID sessionId = java.util.UUID.fromString(jwt.getClaimAsString("sid"));
        long generation = ((Number) jwt.getClaim("generation")).longValue();

        Integer configured = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM ppv_public_login_credential WHERE owner_id = 1 AND password_hash IS NOT NULL",
                Integer.class
        );
        if (configured == null || configured == 0) {
            try {
                profileSessionService.configureOrResetPublicCredential(sessionId, generation, DEFAULT_OWNER_PASSWORD, DEFAULT_PUBLIC_PASSWORD);
            } catch (Exception e) {
                profileSessionService.configureOrResetPublicCredential(sessionId, generation, "VaultPass123!", DEFAULT_PUBLIC_PASSWORD);
            }
        }

        return profileSessionService.publicLogin(DEFAULT_PUBLIC_PASSWORD).accessToken();
    }

    protected String publicBearerTokenViaDowngrade() {
        // Legitimate Origin 2: Actual downgrade from active private session
        if (bootstrapOperations != null && !bootstrapOperations.isBootstrapped()) {
            bootstrapOperations.bootstrap(new com.vhvkhangg.personalprivatevault.authentication.bootstrap.BootstrapCommand(
                    DEFAULT_OWNER_EMAIL,
                    DEFAULT_OWNER_USERNAME,
                    DEFAULT_OWNER_PASSWORD,
                    DEFAULT_OWNER_PIN
            ));
        }
        var tokens = sessionOperations.login(new com.vhvkhangg.personalprivatevault.authentication.session.LoginCommand(
                DEFAULT_OWNER_USERNAME,
                DEFAULT_OWNER_PASSWORD
        ));
        org.springframework.security.oauth2.jwt.Jwt jwt = jwtDecoder.decode(tokens.accessToken());
        java.util.UUID sessionId = java.util.UUID.fromString(jwt.getClaimAsString("sid"));
        long generation = ((Number) jwt.getClaim("generation")).longValue();

        return profileSessionService.downgradePublic(sessionId, generation).accessToken();
    }

    protected String bearerHeader() {
        return "Bearer " + bearerToken();
    }

    protected void awaitCompetingLock(java.time.Duration timeout) {
        java.time.Instant deadline = java.time.Instant.now().plus(timeout);
        while (java.time.Instant.now().isBefore(deadline)) {
            Integer waiting = jdbcTemplate.queryForObject(
                    "SELECT count(*) FROM pg_locks l JOIN pg_stat_activity a ON l.pid = a.pid " +
                    "WHERE NOT l.granted AND a.pid <> pg_backend_pid()",
                    Integer.class
            );
            if (waiting != null && waiting > 0) {
                return;
            }
            try {
                Thread.sleep(25);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException(e);
            }
        }
        throw new AssertionError("Timed out waiting for competing ungranted PostgreSQL lock");
    }
}
