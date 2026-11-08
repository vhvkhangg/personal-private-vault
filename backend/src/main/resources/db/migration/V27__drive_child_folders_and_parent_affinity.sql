CREATE TABLE public.drive_child_folder_intents (
    google_sub VARCHAR(255) NOT NULL,
    root_binding_id UUID NOT NULL,
    idempotency_key VARCHAR(53) NOT NULL,
    root_binding_epoch UUID NOT NULL,
    intent_epoch UUID NOT NULL UNIQUE,
    safe_name VARCHAR(255) NOT NULL,
    request_fingerprint CHAR(64) NOT NULL,
    provider_folder_id VARCHAR(255) UNIQUE,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ,
    PRIMARY KEY (google_sub, root_binding_id, idempotency_key),
    UNIQUE (idempotency_key),
    FOREIGN KEY (google_sub, root_binding_id)
        REFERENCES public.drive_folder_roots (google_sub, root_binding_id),
    CHECK (status IN ('PREPARING','RESERVED','DISPATCHED',
        'RECONCILIATION_REQUIRED','COMPLETED','FAILED_DEFINITIVE')),
    CHECK (request_fingerprint ~ '^[0-9a-f]{64}$'),
    CHECK (safe_name <> '' AND octet_length(safe_name) BETWEEN 1 AND 255),
    CHECK (provider_folder_id IS NULL OR
        provider_folder_id ~ '^[A-Za-z0-9_-]{1,112}$'),
    CHECK ((status = 'PREPARING' AND provider_folder_id IS NULL)
        OR (status IN ('RESERVED','DISPATCHED','RECONCILIATION_REQUIRED','COMPLETED')
            AND provider_folder_id IS NOT NULL)
        OR status = 'FAILED_DEFINITIVE'),
    CHECK ((status IN ('COMPLETED','FAILED_DEFINITIVE'))
        = (completed_at IS NOT NULL))
);

ALTER TABLE public.drive_upload_intents
    ADD COLUMN target_parent_id VARCHAR(255)
    CHECK (target_parent_id IS NULL OR
        target_parent_id ~ '^[A-Za-z0-9_-]{1,112}$');
