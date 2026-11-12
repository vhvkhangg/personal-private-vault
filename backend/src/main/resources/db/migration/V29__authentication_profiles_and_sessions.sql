CREATE TABLE ppv_public_login_credential (
    owner_id smallint PRIMARY KEY REFERENCES app_users(id),
    password_hash varchar(255),
    credential_epoch bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_public_credential_owner CHECK (owner_id = 1),
    CONSTRAINT ck_public_credential_state CHECK (
        (password_hash IS NULL AND credential_epoch = 0) OR
        (password_hash IS NOT NULL AND length(password_hash) > 0 AND credential_epoch > 0)
    )
);

-- Unconfigured metadata only: no seeded/copied credential or plaintext.
INSERT INTO ppv_public_login_credential(owner_id) SELECT id FROM app_users;

CREATE TABLE ppv_login_sessions (
    id uuid PRIMARY KEY,
    owner_id smallint NOT NULL REFERENCES app_users(id),
    active_profile varchar(7) NOT NULL,
    generation bigint NOT NULL,
    public_credential_epoch bigint,
    current_refresh_token_id bigint NOT NULL,
    created_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz,
    CONSTRAINT uq_login_session_owner UNIQUE (id, owner_id),
    CONSTRAINT ck_login_session_owner CHECK (owner_id = 1),
    CONSTRAINT ck_login_session_profile CHECK (active_profile IN ('PUBLIC','PRIVATE')),
    CONSTRAINT ck_login_session_generation CHECK (generation > 0),
    CONSTRAINT ck_login_session_epoch CHECK (
        (public_credential_epoch IS NULL OR public_credential_epoch > 0) AND
        (active_profile <> 'PUBLIC' OR public_credential_epoch IS NOT NULL)
    ),
    CONSTRAINT ck_login_session_expiry CHECK (expires_at > created_at)
);

ALTER TABLE refresh_tokens
    ADD COLUMN session_id uuid,
    ADD COLUMN profile varchar(7),
    ADD COLUMN session_generation bigint;

UPDATE refresh_tokens SET revoked_at = COALESCE(revoked_at, now());

ALTER TABLE refresh_tokens
    ADD CONSTRAINT ck_refresh_profile_binding CHECK (
        (session_id IS NULL AND profile IS NULL AND session_generation IS NULL
            AND revoked_at IS NOT NULL) OR
        (session_id IS NOT NULL AND profile IS NOT NULL AND session_generation IS NOT NULL
            AND profile IN ('PUBLIC','PRIVATE') AND session_generation > 0)
    ),
    ADD CONSTRAINT uq_refresh_session_authority
        UNIQUE (id, session_id, user_id, profile, session_generation),
    ADD CONSTRAINT fk_refresh_session_owner
        FOREIGN KEY (session_id, user_id) REFERENCES ppv_login_sessions(id, owner_id)
        DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ppv_login_sessions
    ADD CONSTRAINT fk_session_current_refresh
        FOREIGN KEY (current_refresh_token_id, id, owner_id, active_profile, generation)
        REFERENCES refresh_tokens(id, session_id, user_id, profile, session_generation)
        DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX idx_refresh_session_id ON refresh_tokens(session_id)
    WHERE session_id IS NOT NULL;
