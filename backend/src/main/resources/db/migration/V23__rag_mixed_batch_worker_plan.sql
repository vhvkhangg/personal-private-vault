-- V23: Approved NO_CHANGE worker plan support for mixed document batches (ADR-0021, ADR-0022, Phase 18 Slice 4)
-- Narrow forward migration: allows rag.seal_document_result to verify subset worker plans recorded in authoritative run metadata.

CREATE OR REPLACE FUNCTION rag.seal_document_result(p_job uuid, p_attempt uuid, p_token varchar) RETURNS text
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE
    l rag.job_leases%ROWTYPE;
    result text;
    expected_roots jsonb;
BEGIN
    l := rag.check_worker_attempt(p_job, p_attempt, p_token, true);
    IF NOT EXISTS(SELECT 1 FROM rag.worker_grants WHERE job_id = p_job AND attempt_id = p_attempt
        AND stage_type = 'DOCUMENT_V3' AND output_digest = encode(sha256(convert_to('DOCUMENT_V3:' || input_digest, 'UTF8')), 'hex')) THEN
        RAISE EXCEPTION 'Incomplete or already sealed document result' USING ERRCODE = 'check_violation';
    END IF;

    -- Authoritative worker plan from run metadata if present; otherwise default to all roots in dependency_manifest
    SELECT (r.metrics->'worker_plan') INTO expected_roots
    FROM rag.runs r WHERE r.job_id = p_job AND r.metrics ? 'worker_plan';

    IF expected_roots IS NULL THEN
        SELECT jsonb_agg(root->'root_key' ORDER BY ordinal) INTO expected_roots
        FROM jsonb_array_elements(l.dependency_manifest::jsonb->'roots') WITH ORDINALITY AS r(root, ordinal);
    END IF;

    IF EXISTS(SELECT value FROM jsonb_array_elements_text(expected_roots) AS e(value)
            EXCEPT SELECT root_key FROM rag.staged_document_outputs WHERE job_id = p_job AND attempt_id = p_attempt)
        OR EXISTS(SELECT root_key FROM rag.staged_document_outputs WHERE job_id = p_job AND attempt_id = p_attempt
            EXCEPT SELECT value FROM jsonb_array_elements_text(expected_roots) AS e(value))
        OR expected_roots IS DISTINCT FROM (
            SELECT jsonb_agg(to_jsonb(root_key) ORDER BY id) FROM rag.staged_document_outputs WHERE job_id = p_job AND attempt_id = p_attempt)
        OR EXISTS(SELECT root_key FROM rag.staged_document_outputs WHERE job_id = p_job AND attempt_id = p_attempt
            EXCEPT SELECT root_key FROM rag.batch_roots WHERE job_id = p_job)
        OR EXISTS(SELECT 1 FROM rag.staged_document_outputs WHERE job_id = p_job AND attempt_id = p_attempt
            AND (NOT worker_receipt OR (NOT success AND (jsonb_array_length(chunks_payload) <> 0 OR reason_code IN ('SUCCESS', 'UNKNOWN'))))) THEN
        RAISE EXCEPTION 'Incomplete or already sealed document result' USING ERRCODE = 'check_violation';
    END IF;

    result := rag.document_result_digest(p_job, p_attempt);
    UPDATE rag.worker_grants SET output_digest = result WHERE job_id = p_job AND attempt_id = p_attempt;
    RETURN result;
END $$;

REVOKE ALL ON FUNCTION rag.seal_document_result(uuid, uuid, varchar) FROM PUBLIC, rag_runtime;
GRANT EXECUTE ON FUNCTION rag.seal_document_result(uuid, uuid, varchar) TO rag_worker;
