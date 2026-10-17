-- Strengthen all worker entry checks without exposing the legacy helper.
ALTER FUNCTION rag.verify_worker_grant(UUID,UUID,VARCHAR,VARCHAR,VARCHAR,VARCHAR) RENAME TO verify_worker_grant_scope;
REVOKE EXECUTE ON FUNCTION rag.verify_worker_grant_scope(UUID,UUID,VARCHAR,VARCHAR,VARCHAR,VARCHAR) FROM PUBLIC,rag_worker;
CREATE FUNCTION rag.verify_worker_grant(p_job UUID,p_attempt UUID,p_token VARCHAR,p_stage VARCHAR,p_input VARCHAR,p_scope VARCHAR)
RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,rag AS $$
DECLARE manifest jsonb; node jsonb; current_id uuid; node_count int; closure_count int;
BEGIN
    PERFORM rag.verify_worker_grant_scope(p_job,p_attempt,p_token,p_stage,p_input,p_scope);
    SELECT dependency_manifest::jsonb INTO manifest FROM rag.job_leases WHERE job_id=p_job AND attempt_id=p_attempt;
    SELECT current_instance INTO current_id FROM rag.runtime_state WHERE singleton;
    IF current_id IS NULL OR jsonb_typeof(manifest) IS DISTINCT FROM 'object'
        OR manifest->>'contract_version' IS DISTINCT FROM '2.0'
        OR jsonb_typeof(manifest->'nodes') IS DISTINCT FROM 'array'
        OR jsonb_typeof(manifest->'edges') IS DISTINCT FROM 'array'
        OR (manifest->>'root_id')::bigint IS DISTINCT FROM
            (SELECT source_id FROM rag.job_leases WHERE job_id=p_job AND attempt_id=p_attempt) THEN
        RAISE EXCEPTION 'Worker runtime or manifest denied' USING ERRCODE='check_violation';
    END IF;
    node_count:=jsonb_array_length(manifest->'nodes');
    IF node_count NOT BETWEEN 1 AND 100 OR node_count<>
        (SELECT count(DISTINCT (v->>'source_id')::bigint) FROM jsonb_array_elements(manifest->'nodes') v) THEN
        RAISE EXCEPTION 'Malformed dependency selection denied' USING ERRCODE='check_violation';
    END IF;
    FOR node IN SELECT * FROM jsonb_array_elements(manifest->'nodes') LOOP
        IF jsonb_typeof(node) IS DISTINCT FROM 'object'
            OR NOT node ?& ARRAY['source_id','source_type','domain_id','epoch','field_version','fields','content_revision','description_revision']
            OR NOT EXISTS (SELECT 1 FROM rag.sources WHERE id=(node->>'source_id')::bigint
                AND approval_instance=current_id AND source_type=node->>'source_type' AND source_id=node->>'domain_id') THEN
            RAISE EXCEPTION 'Stale source consent denied' USING ERRCODE='check_violation';
        END IF;
    END LOOP;
    -- UNION (not UNION ALL) bounds traversal by the selected, capped catalog and terminates corrupt cycles.
    WITH RECURSIVE closure(id) AS (
        SELECT (manifest->>'root_id')::bigint
        UNION
        SELECT d.child_source_id FROM rag.dependencies d JOIN closure c ON d.parent_source_id=c.id
    ) SELECT count(*) INTO closure_count FROM closure;
    IF closure_count<>node_count THEN RAISE EXCEPTION 'Incomplete dependency closure denied' USING ERRCODE='check_violation'; END IF;
    RETURN true;
END $$;
REVOKE ALL ON FUNCTION rag.verify_worker_grant(UUID,UUID,VARCHAR,VARCHAR,VARCHAR,VARCHAR) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION rag.verify_worker_grant(UUID,UUID,VARCHAR,VARCHAR,VARCHAR,VARCHAR) TO rag_worker;
CREATE UNIQUE INDEX uq_rag_worker_receipt_attempt ON rag.staged_outputs(job_id,attempt_id) WHERE worker_receipt;

ALTER TABLE rag.sources ADD CONSTRAINT ck_rag_source_authority CHECK (
    source_type IN ('FICTION','FILM','EXTERNAL_FILE')
    AND classification IN ('UNKNOWN','NON_PRIVATE','PRIVATE')
    AND access_state IN ('DENIED','APPROVED','REVOKED')
    AND authorization_epoch>0 AND field_policy_version>0);

