package com.vhvkhangg.personalprivatevault.authentication;

import com.vhvkhangg.personalprivatevault.authentication.fixture.GoogleSignInTestSupport;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.GoogleHandoffTicket;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.GoogleLoginAttempt;
import com.vhvkhangg.personalprivatevault.authentication.internal.domain.OwnerGoogleIdentity;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.GoogleHandoffTicketRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.GoogleLoginAttemptRepository;
import com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.persistence.OwnerGoogleIdentityRepository;
import com.vhvkhangg.personalprivatevault.portability.internal.infrastructure.snapshot.PortabilitySnapshotAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoogleSignInPersistenceIntegrationTest extends GoogleSignInTestSupport {

    @Autowired
    private OwnerGoogleIdentityRepository identityRepository;

    @Autowired
    private GoogleLoginAttemptRepository attemptRepository;

    @Autowired
    private GoogleHandoffTicketRepository ticketRepository;

    @Autowired
    private javax.sql.DataSource dataSource;

    @BeforeEach
    void setUpOwner() {
        ensureOwnerBootstrapped();
    }

    @Test
    @DisplayName("A-09: Portability snapshot exclusions contain all three new security relations")
    void portabilitySnapshotExcludesGoogleSecurityTables() {
        assertThat(PortabilitySnapshotAdapter.EXCLUDED_SECURITY_TABLES)
                .contains(
                        "ppv_owner_google_identities",
                        "ppv_google_login_attempts",
                        "ppv_google_handoff_tickets"
                );
    }

    @Test
    @DisplayName("A-09: ppv_owner_google_identities enforces singleton owner_id = 1 check constraint")
    void ownerIdentityEnforcesSingletonCheck() {
        // Owner 1 succeeds
        jdbcTemplate.update("""
                INSERT INTO ppv_owner_google_identities (owner_id, issuer, google_sub, linked_at)
                VALUES (1, 'https://accounts.google.com', 'sub-1001', NOW())
                """);

        // Attempting owner_id = 2 violates check constraint
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_owner_google_identities (owner_id, issuer, google_sub, linked_at)
                VALUES (2, 'https://accounts.google.com', 'sub-1002', NOW())
                """)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("A-09: ppv_owner_google_identities enforces unique(issuer, google_sub)")
    void ownerIdentityEnforcesUniqueIssuerSub() {
        jdbcTemplate.update("""
                INSERT INTO ppv_owner_google_identities (owner_id, issuer, google_sub, linked_at)
                VALUES (1, 'https://accounts.google.com', 'sub-unique-1', NOW())
                """);

        // Second insert with same issuer & sub for another user (or re-insert) violates unique constraint
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_owner_google_identities (owner_id, issuer, google_sub, linked_at)
                VALUES (1, 'https://accounts.google.com', 'sub-unique-1', NOW())
                """)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("A-09: ppv_google_login_attempts enforces purpose/owner matching constraint")
    void loginAttemptEnforcesPurposeOwnerMatch() {
        UUID id1 = UUID.randomUUID();
        // LOGIN requires owner_id NULL -> succeeds
        jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, pkce_nonce, pkce_ciphertext, status, created_at, updated_at, expires_at
                ) VALUES (
                    ?, 'OIDC', 'LOGIN', NULL, '1111111111111111111111111111111111111111111111111111111111111111',
                    '2222222222222222222222222222222222222222222222222222222222222222',
                    '3333333333333333333333333333333333333333333333333333333333333333',
                    '4444444444444444444444444444444444444444444444444444444444444444',
                    decode('0102030405060708090a0b0c', 'hex'),
                    decode('000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a', 'hex'),
                    'PENDING', NOW(), NOW(), NOW() + INTERVAL '300 seconds'
                )
                """, id1);

        // LOGIN with owner_id = 1 violates check constraint
        UUID id2 = UUID.randomUUID();
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, pkce_nonce, pkce_ciphertext, status, created_at, updated_at, expires_at
                ) VALUES (
                    ?, 'OIDC', 'LOGIN', 1, '5555555555555555555555555555555555555555555555555555555555555555',
                    '6666666666666666666666666666666666666666666666666666666666666666',
                    '7777777777777777777777777777777777777777777777777777777777777777',
                    '8888888888888888888888888888888888888888888888888888888888888888',
                    decode('0102030405060708090a0b0c', 'hex'),
                    decode('000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a', 'hex'),
                    'PENDING', NOW(), NOW(), NOW() + INTERVAL '300 seconds'
                )
                """, id2)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("A-09: ppv_google_login_attempts enforces exact PKCE lengths (12 nonce, 59 ciphertext)")
    void loginAttemptEnforcesPkceLengths() {
        UUID id = UUID.randomUUID();
        // 11 bytes nonce instead of 12 violates check constraint
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, pkce_nonce, pkce_ciphertext, status, created_at, updated_at, expires_at
                ) VALUES (
                    ?, 'OIDC', 'LOGIN', NULL, '1111111111111111111111111111111111111111111111111111111111111111',
                    '2222222222222222222222222222222222222222222222222222222222222222',
                    '3333333333333333333333333333333333333333333333333333333333333333',
                    '4444444444444444444444444444444444444444444444444444444444444444',
                    decode('0102030405060708090a0b', 'hex'),
                    decode('000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a', 'hex'),
                    'PENDING', NOW(), NOW(), NOW() + INTERVAL '300 seconds'
                )
                """, id)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("A-09: ppv_google_handoff_tickets enforces 60s TTL and singleton owner_id = 1")
    void handoffTicketEnforcesConstraints() {
        UUID attemptId = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, pkce_nonce, pkce_ciphertext, status, created_at, updated_at, expires_at
                ) VALUES (
                    ?, 'OIDC', 'LOGIN', NULL, '1111111111111111111111111111111111111111111111111111111111111111',
                    '2222222222222222222222222222222222222222222222222222222222222222',
                    '3333333333333333333333333333333333333333333333333333333333333333',
                    '4444444444444444444444444444444444444444444444444444444444444444',
                    decode('0102030405060708090a0b0c', 'hex'),
                    decode('000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a', 'hex'),
                    'PENDING', NOW(), NOW(), NOW() + INTERVAL '300 seconds'
                )
                """, attemptId);

        // Valid ticket (60s duration, owner_id = 1) -> succeeds
        jdbcTemplate.update("""
                INSERT INTO ppv_google_handoff_tickets (
                    ticket_hash, attempt_id, owner_id, purpose, handoff_challenge_hash, status, issued_at, expires_at
                ) VALUES (
                    'aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa',
                    ?, 1, 'LOGIN', '4444444444444444444444444444444444444444444444444444444444444444',
                    'PENDING', NOW(), NOW() + INTERVAL '60 seconds'
                )
                """, attemptId);

        // Ticket with wrong duration (e.g. 120s) violates check constraint
        UUID attempt2 = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, pkce_nonce, pkce_ciphertext, status, created_at, updated_at, expires_at
                ) VALUES (
                    ?, 'OIDC', 'LOGIN', NULL, '2111111111111111111111111111111111111111111111111111111111111111',
                    '3222222222222222222222222222222222222222222222222222222222222222',
                    '4333333333333333333333333333333333333333333333333333333333333333',
                    '5444444444444444444444444444444444444444444444444444444444444444',
                    decode('0102030405060708090a0b0c', 'hex'),
                    decode('000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a', 'hex'),
                    'PENDING', NOW(), NOW(), NOW() + INTERVAL '300 seconds'
                )
                """, attempt2);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_google_handoff_tickets (
                    ticket_hash, attempt_id, owner_id, purpose, handoff_challenge_hash, status, issued_at, expires_at
                ) VALUES (
                    'bbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbbb',
                    ?, 1, 'LOGIN', '5444444444444444444444444444444444444444444444444444444444444444',
                    'PENDING', NOW(), NOW() + INTERVAL '120 seconds'
                )
                """, attempt2)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("A-09: Pruning queries delete terminal and expired pending attempts/tickets")
    void pruningQueriesDeleteOldRecords() {
        UUID terminalAttempt = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, pkce_nonce, pkce_ciphertext, status, claim_epoch, created_at, updated_at, expires_at, completed_at
                ) VALUES (
                    ?, 'OIDC', 'LOGIN', NULL,
                    '1111111111111111111111111111111111111111111111111111111111111111',
                    '2222222222222222222222222222222222222222222222222222222222222222',
                    '7777777777777777777777777777777777777777777777777777777777777777',
                    '8888888888888888888888888888888888888888888888888888888888888888',
                    NULL, NULL, 'COMPLETED', gen_random_uuid(), NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours',
                    NOW() - INTERVAL '2 hours' + INTERVAL '300 seconds', NOW() - INTERVAL '2 hours'
                )
                """, terminalAttempt);

        UUID failedAttempt = UUID.randomUUID();
        jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, pkce_nonce, pkce_ciphertext, status, created_at, updated_at, expires_at, completed_at
                ) VALUES (
                    ?, 'LINK_PASSWORD_FAILURE', 'LINK', 1, NULL, NULL, NULL, NULL,
                    NULL, NULL, 'FAILED', NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours',
                    NOW() - INTERVAL '2 hours', NOW() - INTERVAL '2 hours'
                )
                """, failedAttempt);

        Instant cutoff = Instant.now().minusSeconds(3600);
        int prunedAttempts = attemptRepository.pruneTerminalAttempts(cutoff, cutoff, 100);
        assertThat(prunedAttempts).isGreaterThanOrEqualTo(2);
    }

    @Test
    @DisplayName("AUTH-FR-08: ppv_google_login_attempts rejects NULL pkce_nonce or NULL pkce_ciphertext on active attempts")
    void loginAttemptEnforcesNullPkceCheck() {
        UUID id1 = UUID.randomUUID();
        // NULL pkce_nonce on PENDING attempt violates check constraint
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, pkce_nonce, pkce_ciphertext, status, created_at, updated_at, expires_at
                ) VALUES (
                    ?, 'OIDC', 'LOGIN', NULL, '1111111111111111111111111111111111111111111111111111111111111111',
                    '2222222222222222222222222222222222222222222222222222222222222222',
                    '3333333333333333333333333333333333333333333333333333333333333333',
                    '4444444444444444444444444444444444444444444444444444444444444444',
                    NULL,
                    decode('000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a', 'hex'),
                    'PENDING', NOW(), NOW(), NOW() + INTERVAL '300 seconds'
                )
                """, id1)).isInstanceOf(DataIntegrityViolationException.class);

        UUID id2 = UUID.randomUUID();
        // NULL pkce_ciphertext on PENDING attempt violates check constraint
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, pkce_nonce, pkce_ciphertext, status, created_at, updated_at, expires_at
                ) VALUES (
                    ?, 'OIDC', 'LOGIN', NULL, '1111111111111111111111111111111111111111111111111111111111111111',
                    '2222222222222222222222222222222222222222222222222222222222222222',
                    '3333333333333333333333333333333333333333333333333333333333333333',
                    '4444444444444444444444444444444444444444444444444444444444444444',
                    decode('0102030405060708090a0b0c', 'hex'),
                    NULL,
                    'PENDING', NOW(), NOW(), NOW() + INTERVAL '300 seconds'
                )
                """, id2)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("AUTH-FR-08: ppv_google_login_attempts rejects NULL owner_id on purpose LINK")
    void loginAttemptEnforcesNonNullOwnerOnLink() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, pkce_nonce, pkce_ciphertext, status, created_at, updated_at, expires_at
                ) VALUES (
                    ?, 'OIDC', 'LINK', NULL, '1111111111111111111111111111111111111111111111111111111111111111',
                    '2222222222222222222222222222222222222222222222222222222222222222',
                    '3333333333333333333333333333333333333333333333333333333333333333',
                    '4444444444444444444444444444444444444444444444444444444444444444',
                    decode('0102030405060708090a0b0c', 'hex'),
                    decode('000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f202122232425262728292a2b2c2d2e2f303132333435363738393a', 'hex'),
                    'PENDING', NOW(), NOW(), NOW() + INTERVAL '300 seconds'
                )
                """, id)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("AUTH-FR-08: ppv_owner_google_identities enforces canonical issuer and non-whitespace sub")
    void ownerIdentityEnforcesCanonicalIssuerAndCleanSub() {
        // Non-canonical issuer violates check constraint
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_owner_google_identities (owner_id, issuer, google_sub, linked_at)
                VALUES (1, 'https://insecure-issuer.com', 'sub-canonical-1', NOW())
                """)).isInstanceOf(DataIntegrityViolationException.class);

        // Sub with leading/trailing/embedded whitespace or blank violates check constraint
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_owner_google_identities (owner_id, issuer, google_sub, linked_at)
                VALUES (1, 'https://accounts.google.com', 'sub with space', NOW())
                """)).isInstanceOf(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_owner_google_identities (owner_id, issuer, google_sub, linked_at)
                VALUES (1, 'https://accounts.google.com', '  ', NOW())
                """)).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("AUTH-FR-08: LINK_PASSWORD_FAILURE with NULL owner_id violates CHECK constraint")
    void linkPasswordFailureWithNullOwnerViolatesCheckConstraint() {
        assertThatThrownBy(() -> jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, status, created_at, updated_at, expires_at, completed_at
                ) VALUES (
                    ?, 'LINK_PASSWORD_FAILURE', 'LINK', NULL, 'FAILED', NOW(), NOW(), NOW(), NOW()
                )
                """, UUID.randomUUID()))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("AUTH-FR-07: Pruning terminal attempts does not delete parent attempt referenced by existing ticket")
    void pruningTerminalAttemptsDoesNotViolateForeignKeyConstraint() {
        UUID attemptWithTicket = UUID.randomUUID();
        UUID attemptWithoutTicket = UUID.randomUUID();
        Instant oldTime = Instant.now().minus(java.time.Duration.ofHours(48));
        java.sql.Timestamp oldTimestamp = java.sql.Timestamp.from(oldTime);

        // Attempt 1: Has a ticket referencing it
        jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, status, claim_epoch, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, created_at, updated_at, expires_at, completed_at
                ) VALUES (
                    ?, 'OIDC', 'LOGIN', NULL, 'COMPLETED', ?,
                    '1111111111111111111111111111111111111111111111111111111111111111',
                    '1111111111111111111111111111111111111111111111111111111111111111',
                    '1111111111111111111111111111111111111111111111111111111111111111',
                    '1111111111111111111111111111111111111111111111111111111111111111',
                    ?, ?, ?::timestamptz + INTERVAL '300 seconds', ?
                )
                """, attemptWithTicket, UUID.randomUUID(), oldTimestamp, oldTimestamp, oldTimestamp, oldTimestamp);

        // Child ticket referencing attempt 1
        String ticketHash = "2222222222222222222222222222222222222222222222222222222222222222";
        jdbcTemplate.update("""
                INSERT INTO ppv_google_handoff_tickets (
                    ticket_hash, attempt_id, owner_id, purpose, handoff_challenge_hash, status, issued_at, expires_at, consumed_at
                ) VALUES (
                    ?, ?, 1, 'LOGIN',
                    '1111111111111111111111111111111111111111111111111111111111111111',
                    'CONSUMED', ?, ?::timestamptz + INTERVAL '60 seconds', ?
                )
                """, ticketHash, attemptWithTicket, oldTimestamp, oldTimestamp, oldTimestamp);

        // Attempt 2: No ticket referencing it
        jdbcTemplate.update("""
                INSERT INTO ppv_google_login_attempts (
                    attempt_id, record_kind, purpose, owner_id, status, claim_epoch, state_hash, binding_hash, nonce_hash,
                    handoff_challenge_hash, created_at, updated_at, expires_at, completed_at
                ) VALUES (
                    ?, 'OIDC', 'LOGIN', NULL, 'COMPLETED', ?,
                    '3333333333333333333333333333333333333333333333333333333333333333',
                    '3333333333333333333333333333333333333333333333333333333333333333',
                    '3333333333333333333333333333333333333333333333333333333333333333',
                    '3333333333333333333333333333333333333333333333333333333333333333',
                    ?, ?, ?::timestamptz + INTERVAL '300 seconds', ?
                )
                """, attemptWithoutTicket, UUID.randomUUID(), oldTimestamp, oldTimestamp, oldTimestamp, oldTimestamp);

        // Run pruning: cutoff is 24 hours ago
        Instant cutoff = Instant.now().minus(java.time.Duration.ofHours(24));
        Instant recentWindow = Instant.now().minus(java.time.Duration.ofMinutes(15));

        int deleted = attemptRepository.pruneTerminalAttempts(cutoff, recentWindow, 32);
        // Only attemptWithoutTicket should be deleted (1 row deleted) without FK 23503 error
        assertThat(deleted).isEqualTo(1);
        assertThat(attemptRepository.findById(attemptWithoutTicket)).isEmpty();
        assertThat(attemptRepository.findById(attemptWithTicket)).isPresent();

        // Now prune the child ticket
        ticketRepository.deleteById(ticketHash);

        // Now prune attempts again: attemptWithTicket can now safely be deleted
        int deletedSecondPass = attemptRepository.pruneTerminalAttempts(cutoff, recentWindow, 32);
        assertThat(deletedSecondPass).isEqualTo(1);
        assertThat(attemptRepository.findById(attemptWithTicket)).isEmpty();
    }

    @Test
    @DisplayName("AUTH-FR-11: Genuine seeded schema v27 to v28 upgrade migrates cleanly and preserves legacy state")
    void seededV27ToV28UpgradeCompatibility() throws Exception {
        String testDbName = "test_upgrade_v27_v28";
        try (var conn = dataSource.getConnection()) {
            String catalog = conn.getCatalog();
            String metaUrl = conn.getMetaData().getURL();
            String user = conn.getMetaData().getUserName();
            String password = com.vhvkhangg.personalprivatevault.support.AbstractPostgresIntegrationTest.POSTGRES.getPassword();

            try (var stmt = conn.createStatement()) {
                stmt.execute("DROP DATABASE IF EXISTS " + testDbName);
                stmt.execute("CREATE DATABASE " + testDbName);
            }

            String testDbUrl = metaUrl.replace(catalog, testDbName);

            try {
                // Step 1: Migrate up to V27 in the isolated database
                org.flywaydb.core.Flyway flywayV27 = org.flywaydb.core.Flyway.configure()
                        .dataSource(testDbUrl, user, password)
                        .target("27")
                        .load();
                flywayV27.migrate();

                // Step 2: Seed legacy state in V27
                try (var testConn = java.sql.DriverManager.getConnection(testDbUrl, user, password);
                     var stmt = testConn.createStatement()) {
                    stmt.execute("""
                            INSERT INTO app_users (id, email, username, password_hash, pin_hash, created_at, updated_at)
                            VALUES (1, 'legacy_owner@vault.local', 'legacy_owner', '$2a$10$legacyPasswordHash1234567890123456789012345678901234', '$2a$10$legacyPinHash1234567890123456789012345678901234567890', NOW(), NOW())
                            """);

                    stmt.execute("""
                            INSERT INTO refresh_tokens (id, user_id, token_hash, expires_at, created_at)
                            VALUES (1, 1, 'legacytokenhash123456789012345678901234567890123456789012345678', NOW() + INTERVAL '30 days', NOW())
                            """);
                }

                // Step 3: Run Flyway migration to V28
                org.flywaydb.core.Flyway flywayV28 = org.flywaydb.core.Flyway.configure()
                        .dataSource(testDbUrl, user, password)
                        .target("28")
                        .load();
                flywayV28.migrate();

                // Step 4: Verify legacy user and refresh token data are perfectly preserved
                try (var testConn = java.sql.DriverManager.getConnection(testDbUrl, user, password);
                     var stmt = testConn.createStatement()) {
                    try (var rs = stmt.executeQuery("SELECT email FROM app_users WHERE id = 1")) {
                        assertThat(rs.next()).isTrue();
                        assertThat(rs.getString("email")).isEqualTo("legacy_owner@vault.local");
                    }

                    try (var rs = stmt.executeQuery("SELECT token_hash FROM refresh_tokens WHERE id = 1")) {
                        assertThat(rs.next()).isTrue();
                        assertThat(rs.getString("token_hash")).isEqualTo("legacytokenhash123456789012345678901234567890123456789012345678");
                    }

                    // Step 5: Verify new V28 tables exist and are functional in upgraded database
                    stmt.execute("""
                            INSERT INTO ppv_owner_google_identities (owner_id, issuer, google_sub, linked_at)
                            VALUES (1, 'https://accounts.google.com', 'sub-upgraded-owner-1', NOW())
                            """);

                    try (var rs = stmt.executeQuery("SELECT google_sub FROM ppv_owner_google_identities WHERE owner_id = 1")) {
                        assertThat(rs.next()).isTrue();
                        assertThat(rs.getString("google_sub")).isEqualTo("sub-upgraded-owner-1");
                    }
                }
            } finally {
                try (var dropConn = dataSource.getConnection();
                     var stmt = dropConn.createStatement()) {
                    stmt.execute("DROP DATABASE IF EXISTS " + testDbName);
                } catch (Exception ignored) {
                }
            }
        }
    }
}
