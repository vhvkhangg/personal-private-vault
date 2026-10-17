-- Migration V6: Hardened staging definer execution privileges, attempt cumulative
-- staging budget enforcement, and cancellation of unversioned/legacy job manifests.

-- 1. Explicitly revoke EXECUTE from PUBLIC on rag.stage_worker_output
REVOKE EXECUTE ON FUNCTION rag.stage_worker_output(UUID, UUID, VARCHAR, VARCHAR, TEXT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION rag.stage_worker_output(UUID, UUID, VARCHAR, VARCHAR, TEXT) TO rag_worker;

-- 2. Enhanced staging fence trigger with attempt cumulative budget bounds
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
    v_staged_count BIGINT;
    v_total_payload_bytes BIGINT;
BEGIN
    IF NEW.lease_token IS NULL OR trim(NEW.lease_token) = '' THEN
        RAISE EXCEPTION 'Lease fence violation: lease_token must be provided for job_id=% attempt_id=%', NEW.job_id, NEW.attempt_id
            USING ERRCODE = 'check_violation';
    END IF;

    IF NEW.payload IS NULL OR trim(NEW.payload) = '' THEN
        RAISE EXCEPTION 'Lease fence violation: payload must not be empty'
            USING ERRCODE = 'check_violation';
    END IF;

    -- Enforce 1MB single payload ceiling
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

    -- clock_timestamp() checks actual wall-clock time at evaluation
    IF v_leased_until < clock_timestamp() THEN
        RAISE EXCEPTION 'Lease fence violation: lease expired at % (wall-clock=%)', v_leased_until, clock_timestamp()
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_source_status <> 'APPROVED' THEN
        RAISE EXCEPTION 'Lease fence violation: source is not APPROVED (status=%)', v_source_status
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_classification <> 'NON_PRIVATE' THEN
        RAISE EXCEPTION 'Lease fence violation: source is not NON_PRIVATE (classification=%)', v_classification
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_source_epoch <> v_admitted_epoch THEN
        RAISE EXCEPTION 'Lease fence violation: source authorization epoch changed (admitted=% current=%)', v_admitted_epoch, v_source_epoch
            USING ERRCODE = 'check_violation';
    END IF;

    IF v_source_policy_ver <> v_admitted_policy_ver THEN
        RAISE EXCEPTION 'Lease fence violation: source field policy changed (admitted=% current=%)', v_admitted_policy_ver, v_source_policy_ver
            USING ERRCODE = 'check_violation';
    END IF;

    -- Enforce cumulative attempt budget (max 50 rows, max 10MB cumulative payload)
    SELECT count(*), COALESCE(sum(octet_length(payload)), 0)
    INTO v_staged_count, v_total_payload_bytes
    FROM rag.staged_outputs
    WHERE job_id = NEW.job_id AND attempt_id = NEW.attempt_id;

    IF v_staged_count >= 50 THEN
        RAISE EXCEPTION 'Lease fence violation: attempt cumulative staged row count budget exceeded (count=%)', v_staged_count
            USING ERRCODE = 'check_violation';
    END IF;

    IF (v_total_payload_bytes + octet_length(NEW.payload)) > 10485760 THEN
        RAISE EXCEPTION 'Lease fence violation: attempt cumulative staged payload bytes budget exceeded (total=%)', (v_total_payload_bytes + octet_length(NEW.payload))
            USING ERRCODE = 'check_violation';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql SECURITY DEFINER SET search_path = rag, pg_temp;

-- 3. Cancel any unversioned or legacy active/admitted job manifests
UPDATE rag.job_leases
SET status = 'CANCELLED'
WHERE status IN ('ADMITTED', 'ACTIVE')
  AND dependency_manifest NOT LIKE 'v1:%';
