-- Migration V5: Secure attempt-scoped staging definer function, reverse lookup indexes,
-- and least-privilege token protection for rag_worker.

-- 1. Reverse lookup indexes on leases and dependencies
CREATE INDEX IF NOT EXISTS idx_rag_job_leases_source_id ON rag.job_leases(source_id);
CREATE INDEX IF NOT EXISTS idx_rag_dependencies_child_source_id ON rag.dependencies(child_source_id);

-- 1b. Immutable dependency manifest text on job leases
ALTER TABLE rag.job_leases
    ADD COLUMN IF NOT EXISTS dependency_manifest TEXT NOT NULL DEFAULT '';

-- 2. Enhanced staging fence trigger function for direct writes
CREATE OR REPLACE FUNCTION rag.enforce_staging_lease_fence()
RETURNS trigger AS $$
DECLARE
    v_status VARCHAR(32);
    v_leased_until TIMESTAMPTZ;
    v_lease_token VARCHAR(128);
    v_source_id BIGINT;
    v_source_status VARCHAR(32);
    v_classification VARCHAR(32);
    v_admitted_epoch BIGINT;
    v_admitted_policy_ver INT;
    v_source_epoch BIGINT;
    v_source_policy_ver INT;
BEGIN
    IF NEW.lease_token IS NULL OR trim(NEW.lease_token) = '' THEN
        RAISE EXCEPTION 'Lease fence violation: lease_token must be provided for job_id=% attempt_id=%', NEW.job_id, NEW.attempt_id
            USING ERRCODE = 'check_violation';
    END IF;

    IF NEW.payload IS NULL OR trim(NEW.payload) = '' THEN
        RAISE EXCEPTION 'Lease fence violation: payload must not be empty'
            USING ERRCODE = 'check_violation';
    END IF;

    -- Enforce 1MB payload ceiling
    IF octet_length(NEW.payload) > 1048576 THEN
        RAISE EXCEPTION 'Lease fence violation: payload exceeds 1MB limit (size=%)', octet_length(NEW.payload)
            USING ERRCODE = 'check_violation';
    END IF;

    -- Stage type allowlist
    IF NEW.stage_type NOT IN ('CHUNK', 'CHUNKS', 'SYNTHETIC_STAGE', 'SYNTHETIC_TEST') THEN
        RAISE EXCEPTION 'Lease fence violation: unknown or unauthorized stage_type=%', NEW.stage_type
            USING ERRCODE = 'check_violation';
    END IF;

    SELECT l.status, l.leased_until, l.lease_token, l.source_id, l.admitted_epoch, l.admitted_field_policy_version,
           s.access_state, s.classification, s.authorization_epoch, s.field_policy_version
    INTO v_status, v_leased_until, v_lease_token, v_source_id, v_admitted_epoch, v_admitted_policy_ver,
         v_source_status, v_classification, v_source_epoch, v_source_policy_ver
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

    -- Source classification must be strictly NON_PRIVATE
    IF v_classification <> 'NON_PRIVATE' THEN
        RAISE EXCEPTION 'Lease fence violation: source is not NON_PRIVATE (classification=%)', v_classification
            USING ERRCODE = 'check_violation';
    END IF;

    -- Disallow writes under stale epoch or modified field policy
    IF v_source_epoch <> v_admitted_epoch THEN
        RAISE EXCEPTION 'Lease fence violation: source authorization epoch changed (admitted=% current=%)', v_admitted_epoch, v_source_epoch
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_source_policy_ver <> v_admitted_policy_ver THEN
        RAISE EXCEPTION 'Lease fence violation: source field policy changed (admitted=% current=%)', v_admitted_policy_ver, v_source_policy_ver
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = rag, pg_temp;

-- 3. Dedicated attempt-scoped definer staging function
CREATE OR REPLACE FUNCTION rag.stage_worker_output(
    p_job_id UUID,
    p_attempt_id UUID,
    p_lease_token VARCHAR,
    p_stage_type VARCHAR,
    p_payload TEXT
) RETURNS BIGINT AS $$
DECLARE
    v_new_id BIGINT;
BEGIN
    INSERT INTO rag.staged_outputs (job_id, attempt_id, stage_type, payload, lease_token, created_at)
    VALUES (p_job_id, p_attempt_id, p_stage_type, p_payload, p_lease_token, clock_timestamp())
    RETURNING id INTO v_new_id;

    RETURN v_new_id;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = rag, pg_temp;

-- 4. Restrict rag_worker least-privilege rights:
-- Revoke direct SELECT, INSERT, UPDATE, DELETE on staged_outputs from rag_worker.
-- Worker CANNOT read any tokens or payloads from staged_outputs.
-- Worker can only write via the security-definer function rag.stage_worker_output.
REVOKE ALL ON rag.staged_outputs FROM rag_worker;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA rag FROM rag_worker;
GRANT USAGE ON SCHEMA rag TO rag_worker;
GRANT EXECUTE ON FUNCTION rag.stage_worker_output(UUID, UUID, VARCHAR, VARCHAR, TEXT) TO rag_worker;
