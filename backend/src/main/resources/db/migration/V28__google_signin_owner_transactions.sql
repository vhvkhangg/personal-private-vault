-- V28__google_signin_owner_transactions.sql
-- GDI26-S3-H1-AUTH: Spring-owned Google Sign-In and singleton Owner identity transactions

CREATE TABLE ppv_owner_google_identities (
    owner_id SMALLINT PRIMARY KEY REFERENCES app_users(id),
    issuer VARCHAR(128) NOT NULL,
    google_sub VARCHAR(255) NOT NULL,
    linked_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_ppv_owner_google_identities_owner_id CHECK (owner_id = 1),
    CONSTRAINT chk_ppv_owner_google_identities_issuer CHECK (issuer = 'https://accounts.google.com'),
    CONSTRAINT chk_ppv_owner_google_identities_sub CHECK (length(trim(google_sub)) > 0 AND trim(google_sub) = google_sub AND google_sub !~ '[\x00-\x1F\x7F\s]'),
    CONSTRAINT uq_ppv_owner_google_identities_issuer_sub UNIQUE (issuer, google_sub)
);

CREATE TABLE ppv_google_login_attempts (
    attempt_id UUID PRIMARY KEY,
    record_kind VARCHAR(24) NOT NULL,
    purpose VARCHAR(8) NOT NULL,
    owner_id SMALLINT REFERENCES app_users(id),
    state_hash CHAR(64),
    binding_hash CHAR(64),
    nonce_hash CHAR(64),
    handoff_challenge_hash CHAR(64),
    pkce_nonce BYTEA,
    pkce_ciphertext BYTEA,
    status VARCHAR(16) NOT NULL,
    claim_epoch UUID,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    CONSTRAINT chk_ppv_google_login_attempts_kind CHECK (record_kind IN ('OIDC', 'LINK_PASSWORD_FAILURE')),
    CONSTRAINT chk_ppv_google_login_attempts_purpose CHECK (purpose IN ('LOGIN', 'LINK')),
    CONSTRAINT chk_ppv_google_login_attempts_status CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'EXPIRED')),
    CONSTRAINT chk_ppv_google_login_attempts_owner_id CHECK (owner_id IS NULL OR owner_id = 1),
    CONSTRAINT chk_ppv_google_login_attempts_state_hash CHECK (state_hash IS NULL OR state_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_ppv_google_login_attempts_binding_hash CHECK (binding_hash IS NULL OR binding_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_ppv_google_login_attempts_nonce_hash CHECK (nonce_hash IS NULL OR nonce_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_ppv_google_login_attempts_challenge_hash CHECK (handoff_challenge_hash IS NULL OR handoff_challenge_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_ppv_google_login_attempts_terminal_completed_at CHECK (
        (status IN ('COMPLETED', 'FAILED', 'EXPIRED') AND completed_at IS NOT NULL) OR
        (status IN ('PENDING', 'PROCESSING') AND completed_at IS NULL)
    ),
    CONSTRAINT chk_ppv_google_login_attempts_claim_epoch CHECK (
        (status = 'PENDING' AND claim_epoch IS NULL) OR
        (status IN ('PROCESSING', 'COMPLETED') AND claim_epoch IS NOT NULL) OR
        (status IN ('FAILED', 'EXPIRED'))
    ),
    CONSTRAINT chk_ppv_google_login_attempts_kind_oidc CHECK (
        record_kind <> 'OIDC' OR (
            state_hash IS NOT NULL AND
            binding_hash IS NOT NULL AND
            nonce_hash IS NOT NULL AND
            handoff_challenge_hash IS NOT NULL AND
            expires_at = created_at + INTERVAL '300 seconds' AND
            ((purpose = 'LINK' AND owner_id IS NOT NULL AND owner_id = 1) OR (purpose = 'LOGIN' AND owner_id IS NULL)) AND
            ((status IN ('PENDING', 'PROCESSING') AND pkce_nonce IS NOT NULL AND octet_length(pkce_nonce) = 12 AND pkce_ciphertext IS NOT NULL AND octet_length(pkce_ciphertext) = 59) OR
             (status IN ('COMPLETED', 'FAILED', 'EXPIRED') AND pkce_nonce IS NULL AND pkce_ciphertext IS NULL))
        )
    ),
    CONSTRAINT chk_ppv_google_login_attempts_kind_failure CHECK (
        record_kind <> 'LINK_PASSWORD_FAILURE' OR (
            purpose = 'LINK' AND
            owner_id IS NOT NULL AND
            owner_id = 1 AND
            status = 'FAILED' AND
            created_at = expires_at AND
            created_at = completed_at AND
            state_hash IS NULL AND
            binding_hash IS NULL AND
            nonce_hash IS NULL AND
            handoff_challenge_hash IS NULL AND
            pkce_nonce IS NULL AND
            pkce_ciphertext IS NULL AND
            claim_epoch IS NULL
        )
    )
);

CREATE UNIQUE INDEX uq_ppv_google_login_attempts_state_hash
    ON ppv_google_login_attempts (state_hash)
    WHERE state_hash IS NOT NULL;

CREATE INDEX idx_ppv_google_login_attempts_kind_created
    ON ppv_google_login_attempts (record_kind, created_at);

CREATE INDEX idx_ppv_google_login_attempts_status_expires
    ON ppv_google_login_attempts (status, expires_at);

CREATE TABLE ppv_google_handoff_tickets (
    ticket_hash CHAR(64) PRIMARY KEY,
    attempt_id UUID NOT NULL UNIQUE REFERENCES ppv_google_login_attempts(attempt_id),
    owner_id SMALLINT NOT NULL REFERENCES app_users(id),
    purpose VARCHAR(8) NOT NULL,
    handoff_challenge_hash CHAR(64) NOT NULL,
    status VARCHAR(16) NOT NULL,
    issued_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    consumed_at TIMESTAMPTZ,
    CONSTRAINT chk_ppv_google_handoff_tickets_hash CHECK (ticket_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_ppv_google_handoff_tickets_owner_id CHECK (owner_id = 1),
    CONSTRAINT chk_ppv_google_handoff_tickets_purpose CHECK (purpose IN ('LOGIN', 'LINK')),
    CONSTRAINT chk_ppv_google_handoff_tickets_challenge_hash CHECK (handoff_challenge_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT chk_ppv_google_handoff_tickets_status CHECK (status IN ('PENDING', 'CONSUMED', 'EXPIRED')),
    CONSTRAINT chk_ppv_google_handoff_tickets_expiry CHECK (expires_at = issued_at + INTERVAL '60 seconds'),
    CONSTRAINT chk_ppv_google_handoff_tickets_consumed CHECK (
        (status = 'CONSUMED' AND consumed_at IS NOT NULL) OR
        (status IN ('PENDING', 'EXPIRED') AND consumed_at IS NULL)
    )
);

CREATE INDEX idx_ppv_google_handoff_tickets_status_expires
    ON ppv_google_handoff_tickets (status, expires_at);
