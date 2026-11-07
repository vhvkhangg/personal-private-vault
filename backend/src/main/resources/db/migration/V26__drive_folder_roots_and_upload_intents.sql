CREATE TABLE public.drive_folder_roots (
    google_sub VARCHAR(255) PRIMARY KEY CHECK (btrim(google_sub) <> ''),
    root_file_id VARCHAR(255) NOT NULL UNIQUE CHECK (root_file_id ~ '^[A-Za-z0-9_-]{1,112}$'),
    root_binding_id UUID NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL CHECK (status IN ('PENDING','READY','RECONCILIATION_REQUIRED')),
    binding_epoch UUID NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    UNIQUE (google_sub, root_binding_id)
);

CREATE TABLE public.drive_upload_intents (
    google_sub VARCHAR(255) NOT NULL,
    root_binding_id UUID NOT NULL,
    idempotency_key VARCHAR(53) NOT NULL UNIQUE,
    request_fingerprint CHAR(64) NOT NULL CHECK (request_fingerprint ~ '^[0-9a-f]{64}$'),
    content_md5 CHAR(32) NOT NULL CHECK (content_md5 ~ '^[0-9a-f]{32}$'),
    size_bytes BIGINT NOT NULL CHECK (size_bytes BETWEEN 0 AND 52428800),
    safe_name VARCHAR(255) NOT NULL CHECK (octet_length(safe_name) BETWEEN 1 AND 255),
    mime_type VARCHAR(127) NOT NULL CHECK (mime_type ~ '^[a-z0-9!#$&^_.+-]+/[a-z0-9!#$&^_.+-]+$'),
    provider_file_id VARCHAR(255) UNIQUE CHECK (provider_file_id IS NULL OR provider_file_id ~ '^[A-Za-z0-9_-]{1,112}$'),
    status VARCHAR(32) NOT NULL CHECK (status IN ('PREPARING','RESERVED','DISPATCHED','RECONCILIATION_REQUIRED','COMPLETED','FAILED_DEFINITIVE')),
    intent_epoch UUID NOT NULL UNIQUE,
    root_binding_epoch UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    PRIMARY KEY (google_sub, root_binding_id, idempotency_key),
    FOREIGN KEY (google_sub, root_binding_id) REFERENCES public.drive_folder_roots (google_sub, root_binding_id),
    CHECK ((status = 'PREPARING' AND provider_file_id IS NULL)
        OR (status IN ('RESERVED','DISPATCHED','RECONCILIATION_REQUIRED','COMPLETED') AND provider_file_id IS NOT NULL)
        OR status = 'FAILED_DEFINITIVE'),
    CHECK ((status IN ('COMPLETED','FAILED_DEFINITIVE') AND completed_at IS NOT NULL)
        OR (status NOT IN ('COMPLETED','FAILED_DEFINITIVE') AND completed_at IS NULL))
);
