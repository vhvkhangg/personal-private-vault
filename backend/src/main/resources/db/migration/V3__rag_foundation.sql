-- V3__rag_foundation.sql
-- Establishes the minimal rag schema, authority/lease/staging tables, DB-enforced lease fence,
-- and least-privilege rag_worker role.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_available_extensions WHERE name = 'vector') THEN
        CREATE EXTENSION IF NOT EXISTS vector;
    END IF;
END $$;

CREATE SCHEMA IF NOT EXISTS rag;

-- Authoritative source registry and classification/field-policy state
CREATE TABLE IF NOT EXISTS rag.sources (
    id BIGSERIAL PRIMARY KEY,
    source_type VARCHAR(32) NOT NULL,
    source_id VARCHAR(255) NOT NULL,
    classification VARCHAR(32) NOT NULL,
    authorization_epoch BIGINT NOT NULL DEFAULT 1,
    field_policy_version INT NOT NULL DEFAULT 1,
    approved_fields_text TEXT NOT NULL DEFAULT '',
    access_state VARCHAR(32) NOT NULL DEFAULT 'APPROVED',
    source_revision_hash VARCHAR(128) NOT NULL,
    description_revision_hash VARCHAR(128) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    CONSTRAINT uq_rag_sources_type_id UNIQUE (source_type, source_id)
);

CREATE INDEX IF NOT EXISTS idx_rag_sources_lookup ON rag.sources(source_type, source_id);

-- Transactionally guarded job attempt lease rows
CREATE TABLE IF NOT EXISTS rag.job_leases (
    id BIGSERIAL PRIMARY KEY,
    job_id UUID NOT NULL,
    attempt_id UUID NOT NULL,
    source_id BIGINT NOT NULL REFERENCES rag.sources(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL DEFAULT 'ADMITTED',
    lease_token VARCHAR(128) NOT NULL,
    leased_until TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_rag_job_leases_job_attempt UNIQUE (job_id, attempt_id)
);

CREATE INDEX IF NOT EXISTS idx_rag_job_leases_job_id ON rag.job_leases(job_id);

-- Disposable worker staging outputs guarded by DB commit fence
CREATE TABLE IF NOT EXISTS rag.staged_outputs (
    id BIGSERIAL PRIMARY KEY,
    job_id UUID NOT NULL,
    attempt_id UUID NOT NULL,
    stage_type VARCHAR(64) NOT NULL,
    payload TEXT,
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_rag_staged_outputs_job_attempt ON rag.staged_outputs(job_id, attempt_id);

-- Transitive transclusion and document dependencies
CREATE TABLE IF NOT EXISTS rag.dependencies (
    id BIGSERIAL PRIMARY KEY,
    parent_source_id BIGINT NOT NULL REFERENCES rag.sources(id) ON DELETE CASCADE,
    child_source_id BIGINT NOT NULL REFERENCES rag.sources(id) ON DELETE CASCADE,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_rag_dependencies_parent_child UNIQUE (parent_source_id, child_source_id)
);

-- Index generation tracking with active generation pointer
CREATE TABLE IF NOT EXISTS rag.index_generations (
    id BIGSERIAL PRIMARY KEY,
    generation_id UUID NOT NULL UNIQUE,
    source_id BIGINT NOT NULL REFERENCES rag.sources(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL DEFAULT 'STAGED',
    authorization_epoch BIGINT NOT NULL,
    source_revision_hash VARCHAR(128) NOT NULL,
    dependency_manifest_digest VARCHAR(128) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL,
    published_at TIMESTAMPTZ,
    revoked_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_rag_index_generations_source_active ON rag.index_generations(source_id, is_active);

-- Execution run audit and sanitized operational metrics
CREATE TABLE IF NOT EXISTS rag.runs (
    id BIGSERIAL PRIMARY KEY,
    run_id UUID NOT NULL UNIQUE,
    job_id UUID,
    source_id BIGINT REFERENCES rag.sources(id) ON DELETE SET NULL,
    authorization_epoch BIGINT NOT NULL,
    status VARCHAR(32) NOT NULL,
    metrics JSONB,
    purge_status VARCHAR(32) NOT NULL DEFAULT 'NONE',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

-- Mandatory DB-enforced commit fence for worker staging writes:
-- Obtains a row lock on rag.job_leases and rechecks active lease validity before staging write commits.
CREATE OR REPLACE FUNCTION rag.enforce_staging_lease_fence()
RETURNS trigger AS $$
DECLARE
    v_status VARCHAR(32);
    v_leased_until TIMESTAMPTZ;
BEGIN
    SELECT status, leased_until INTO v_status, v_leased_until
    FROM rag.job_leases
    WHERE job_id = NEW.job_id AND attempt_id = NEW.attempt_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Lease fence violation: no lease record for job_id=% attempt_id=%', NEW.job_id, NEW.attempt_id
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_status NOT IN ('ACTIVE', 'ADMITTED') THEN
        RAISE EXCEPTION 'Lease fence violation: job attempt is not active (status=%)', v_status
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_leased_until < CURRENT_TIMESTAMP THEN
        RAISE EXCEPTION 'Lease fence violation: lease expired at %', v_leased_until
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = rag, pg_temp;

DROP TRIGGER IF EXISTS trg_staged_outputs_lease_fence ON rag.staged_outputs;
CREATE TRIGGER trg_staged_outputs_lease_fence
BEFORE INSERT OR UPDATE ON rag.staged_outputs
FOR EACH ROW EXECUTE FUNCTION rag.enforce_staging_lease_fence();

-- Restricted non-owner rag_worker role
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'rag_worker') THEN
        CREATE ROLE rag_worker WITH LOGIN PASSWORD 'rag_worker_local_pass'
            NOBYPASSRLS NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT;
    ELSE
        ALTER ROLE rag_worker WITH PASSWORD 'rag_worker_local_pass';
    END IF;
END $$;

GRANT USAGE ON SCHEMA rag TO rag_worker;
GRANT SELECT ON rag.sources TO rag_worker;
GRANT SELECT ON rag.job_leases TO rag_worker;
GRANT SELECT, INSERT, UPDATE, DELETE ON rag.staged_outputs TO rag_worker;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA rag TO rag_worker;

REVOKE ALL ON ALL TABLES IN SCHEMA public FROM rag_worker;
REVOKE ALL ON SCHEMA public FROM rag_worker;
