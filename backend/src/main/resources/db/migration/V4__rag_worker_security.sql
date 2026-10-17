-- Migration V4: RAG worker security hardening, immutable lease grants, and wall-clock expiry enforcement.

-- 1. Immutable grant fields on job leases
ALTER TABLE rag.job_leases
    ADD COLUMN IF NOT EXISTS admitted_epoch BIGINT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS admitted_field_policy_version INT NOT NULL DEFAULT 1,
    ADD COLUMN IF NOT EXISTS approved_fields_text TEXT NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS content_revision_hash VARCHAR(128) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS description_revision_hash VARCHAR(128) NOT NULL DEFAULT '';

-- 2. Windows NTFS file identity on external file sources
ALTER TABLE rag.sources
    ADD COLUMN IF NOT EXISTS file_identity VARCHAR(128);

-- 3. Job ID linkage on index generations for safe cancellation invalidation
ALTER TABLE rag.index_generations
    ADD COLUMN IF NOT EXISTS job_id UUID;

-- 4. Mandatory lease token on worker staged outputs
ALTER TABLE rag.staged_outputs
    ADD COLUMN IF NOT EXISTS lease_token VARCHAR(128);

-- 5. Supporting query indexes
CREATE INDEX IF NOT EXISTS idx_rag_staged_outputs_job_attempt ON rag.staged_outputs(job_id, attempt_id);
CREATE INDEX IF NOT EXISTS idx_rag_index_generations_job_id ON rag.index_generations(job_id);
CREATE INDEX IF NOT EXISTS idx_rag_sources_file_identity ON rag.sources(file_identity) WHERE file_identity IS NOT NULL;

-- 6. Wall-clock fence enforcement trigger function
-- Uses clock_timestamp() to prevent pre-started transaction expiry bypass,
-- validates lease_token matches active lease, and verifies source remains APPROVED and non-private.
CREATE OR REPLACE FUNCTION rag.enforce_staging_lease_fence()
RETURNS trigger AS $$
DECLARE
    v_status VARCHAR(32);
    v_leased_until TIMESTAMPTZ;
    v_lease_token VARCHAR(128);
    v_source_id BIGINT;
    v_source_status VARCHAR(32);
    v_classification VARCHAR(32);
BEGIN
    IF NEW.lease_token IS NULL OR trim(NEW.lease_token) = '' THEN
        RAISE EXCEPTION 'Lease fence violation: lease_token must be provided for job_id=% attempt_id=%', NEW.job_id, NEW.attempt_id
            USING ERRCODE = 'check_violation';
    END IF;

    SELECT l.status, l.leased_until, l.lease_token, l.source_id, s.access_state, s.classification
    INTO v_status, v_leased_until, v_lease_token, v_source_id, v_source_status, v_classification
    FROM rag.job_leases l
    JOIN rag.sources s ON s.id = l.source_id
    WHERE l.job_id = NEW.job_id AND l.attempt_id = NEW.attempt_id
    FOR UPDATE OF l;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Lease fence violation: no active lease or source found for job_id=% attempt_id=%', NEW.job_id, NEW.attempt_id
            USING ERRCODE = 'check_violation';
    END IF;

    IF NEW.lease_token <> v_lease_token THEN
        RAISE EXCEPTION 'Lease fence violation: invalid lease_token for job_id=% attempt_id=%', NEW.job_id, NEW.attempt_id
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_status NOT IN ('ACTIVE', 'ADMITTED') THEN
        RAISE EXCEPTION 'Lease fence violation: job attempt is not active (status=%)', v_status
            USING ERRCODE = 'check_violation';
    END IF;

    -- clock_timestamp() checks actual wall-clock time at evaluation, not fixed transaction-start time
    IF v_leased_until < clock_timestamp() THEN
        RAISE EXCEPTION 'Lease fence violation: lease expired at % (wall-clock=%)', v_leased_until, clock_timestamp()
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_source_status <> 'APPROVED' THEN
        RAISE EXCEPTION 'Lease fence violation: source is not APPROVED (status=%)', v_source_status
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_classification = 'PRIVATE' THEN
        RAISE EXCEPTION 'Lease fence violation: private source cannot be staged (source_id=%)', v_source_id
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = rag, pg_temp;

-- 7. Least-privilege role adjustments for rag_worker
-- Worker can only INSERT and SELECT on staged_outputs, cannot read sources, leases, or index generations directly,
-- and cannot DELETE or UPDATE staged outputs.
REVOKE ALL ON rag.sources FROM rag_worker;
REVOKE ALL ON rag.job_leases FROM rag_worker;
REVOKE ALL ON rag.index_generations FROM rag_worker;
REVOKE DELETE, UPDATE ON rag.staged_outputs FROM rag_worker;
GRANT SELECT, INSERT ON rag.staged_outputs TO rag_worker;
GRANT USAGE, SELECT ON ALL SEQUENCES IN SCHEMA rag TO rag_worker;
