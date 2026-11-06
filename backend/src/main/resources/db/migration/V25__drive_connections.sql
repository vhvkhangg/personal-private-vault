-- V25__drive_connections.sql
-- Additive Google Drive connection credentials and authority table.
-- Frozen Schema v1 and existing tables remain untouched.

CREATE TABLE public.drive_connections (
    slot SMALLINT PRIMARY KEY,
    google_sub VARCHAR(255) NOT NULL,
    display_email VARCHAR(320),
    granted_scopes TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    encrypted_refresh_token BYTEA,
    key_id VARCHAR(64),
    credential_epoch UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT chk_drive_connections_slot CHECK (slot IN (1, 2)),
    CONSTRAINT uq_drive_connections_google_sub UNIQUE (google_sub),
    CONSTRAINT uq_drive_connections_credential_epoch UNIQUE (credential_epoch),
    CONSTRAINT chk_drive_connections_status CHECK (status IN ('CONNECTED', 'RECONNECT_REQUIRED')),
    CONSTRAINT chk_drive_connections_credentials CHECK (
        (status = 'CONNECTED' AND encrypted_refresh_token IS NOT NULL AND length(encrypted_refresh_token) > 0 AND key_id IS NOT NULL AND btrim(key_id) <> '')
        OR
        (status = 'RECONNECT_REQUIRED' AND encrypted_refresh_token IS NULL AND key_id IS NULL)
    )
);
