-- Consolidated exact, fail-closed worker authorization. No business-schema reads.
CREATE OR REPLACE FUNCTION rag.verify_worker_grant(p_job UUID,p_attempt UUID,p_token VARCHAR,p_stage VARCHAR,p_input VARCHAR,p_scope VARCHAR)
RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,rag AS $$
DECLARE
    l rag.job_leases%ROWTYPE; g rag.worker_grants%ROWTYPE; s rag.sources%ROWTYPE;
    manifest jsonb; node jsonb; edge jsonb; current_id uuid; node_ids bigint[]; actual_ids bigint[];
BEGIN
    IF session_user<>'rag_worker' OR p_job IS NULL OR p_attempt IS NULL OR p_token IS NULL
        OR p_stage IS NULL OR p_input IS NULL OR p_scope IS NULL THEN
        RAISE EXCEPTION 'Worker grant denied' USING ERRCODE='check_violation'; END IF;
    SELECT * INTO l FROM rag.job_leases WHERE job_id=p_job AND attempt_id=p_attempt FOR UPDATE;
    IF NOT FOUND OR l.lease_token<>p_token OR l.status NOT IN ('ADMITTED','ACTIVE') OR l.leased_until<=clock_timestamp() THEN
        RAISE EXCEPTION 'Worker grant denied' USING ERRCODE='check_violation'; END IF;
    SELECT * INTO g FROM rag.worker_grants WHERE job_id=p_job AND attempt_id=p_attempt;
    IF NOT FOUND OR g.session_until<=clock_timestamp() OR g.stage_type<>p_stage OR g.input_digest<>p_input OR g.scope_digest<>p_scope
        OR g.scope_digest<>encode(sha256(convert_to(l.dependency_manifest,'UTF8')),'hex') THEN
        RAISE EXCEPTION 'Worker issuance denied' USING ERRCODE='check_violation'; END IF;
    manifest:=l.dependency_manifest::jsonb;
    SELECT current_instance INTO current_id FROM rag.runtime_state WHERE singleton;
    IF current_id IS NULL OR jsonb_typeof(manifest) IS DISTINCT FROM 'object'
        OR manifest->>'contract_version' IS DISTINCT FROM '2.0'
        OR jsonb_typeof(manifest->'nodes') IS DISTINCT FROM 'array'
        OR jsonb_typeof(manifest->'edges') IS DISTINCT FROM 'array'
        OR (manifest->>'root_id')::bigint IS DISTINCT FROM l.source_id
        OR (SELECT count(*) FROM jsonb_object_keys(manifest))<>4 THEN
        RAISE EXCEPTION 'Worker manifest denied' USING ERRCODE='check_violation'; END IF;
    IF jsonb_array_length(manifest->'nodes') NOT BETWEEN 1 AND 100 THEN
        RAISE EXCEPTION 'Worker selection budget denied' USING ERRCODE='check_violation'; END IF;
    SELECT array_agg(DISTINCT (v->>'source_id')::bigint ORDER BY (v->>'source_id')::bigint)
        INTO node_ids FROM jsonb_array_elements(manifest->'nodes') v;
    IF cardinality(node_ids)<>jsonb_array_length(manifest->'nodes') THEN
        RAISE EXCEPTION 'Duplicate dependency denied' USING ERRCODE='check_violation'; END IF;
    FOR node IN SELECT * FROM jsonb_array_elements(manifest->'nodes') LOOP
        IF jsonb_typeof(node) IS DISTINCT FROM 'object'
            OR NOT node ?& ARRAY['source_id','source_type','domain_id','epoch','field_version','fields','content_revision','description_revision']
            OR (SELECT count(*) FROM jsonb_object_keys(node))<>8
            OR jsonb_typeof(node->'source_id') IS DISTINCT FROM 'number'
            OR jsonb_typeof(node->'epoch') IS DISTINCT FROM 'number'
            OR jsonb_typeof(node->'field_version') IS DISTINCT FROM 'number'
            OR jsonb_typeof(node->'fields') IS DISTINCT FROM 'array'
            OR jsonb_typeof(node->'source_type') IS DISTINCT FROM 'string'
            OR jsonb_typeof(node->'domain_id') IS DISTINCT FROM 'string'
            OR jsonb_typeof(node->'content_revision') IS DISTINCT FROM 'string'
            OR jsonb_typeof(node->'description_revision') IS DISTINCT FROM 'string' THEN
            RAISE EXCEPTION 'Malformed source authority denied' USING ERRCODE='check_violation'; END IF;
        SELECT * INTO s FROM rag.sources WHERE id=(node->>'source_id')::bigint;
        IF NOT FOUND OR s.source_type NOT IN ('FICTION','FILM') OR s.access_state<>'APPROVED' OR s.classification<>'NON_PRIVATE'
            OR s.approval_instance IS DISTINCT FROM current_id OR s.source_type<>node->>'source_type' OR s.source_id<>node->>'domain_id'
            OR s.authorization_epoch<>(node->>'epoch')::bigint OR s.field_policy_version<>(node->>'field_version')::int
            OR s.source_revision_hash<>node->>'content_revision'
            OR to_jsonb(string_to_array(s.approved_fields_text,','))<>node->'fields'
            OR (node->>'description_revision'<>'' AND s.description_revision_hash<>node->>'description_revision') THEN
            RAISE EXCEPTION 'Worker source authority denied' USING ERRCODE='check_violation'; END IF;
    END LOOP;
    WITH RECURSIVE closure(id) AS (
        SELECT l.source_id UNION SELECT d.child_source_id FROM rag.dependencies d JOIN closure c ON d.parent_source_id=c.id
    ) SELECT array_agg(id ORDER BY id) INTO actual_ids FROM closure;
    IF actual_ids IS DISTINCT FROM node_ids THEN
        RAISE EXCEPTION 'Incomplete dependency closure denied' USING ERRCODE='check_violation'; END IF;
    FOR edge IN SELECT * FROM jsonb_array_elements(manifest->'edges') LOOP
        IF jsonb_typeof(edge) IS DISTINCT FROM 'array' OR jsonb_array_length(edge)<>2
            OR jsonb_typeof(edge->0) IS DISTINCT FROM 'number' OR jsonb_typeof(edge->1) IS DISTINCT FROM 'number'
            OR NOT (edge->>0)::bigint=ANY(node_ids) OR NOT (edge->>1)::bigint=ANY(node_ids) THEN
            RAISE EXCEPTION 'Malformed topology denied' USING ERRCODE='check_violation'; END IF;
    END LOOP;
    IF (SELECT count(DISTINCT e) FROM jsonb_array_elements(manifest->'edges') e)<>jsonb_array_length(manifest->'edges')
        OR EXISTS(SELECT 1 FROM rag.dependencies d WHERE d.parent_source_id=ANY(node_ids)
            AND NOT EXISTS(SELECT 1 FROM jsonb_array_elements(manifest->'edges') e WHERE (e->>0)::bigint=d.parent_source_id AND (e->>1)::bigint=d.child_source_id))
        OR EXISTS(SELECT 1 FROM jsonb_array_elements(manifest->'edges') e
            WHERE NOT EXISTS(SELECT 1 FROM rag.dependencies d WHERE d.parent_source_id=(e->>0)::bigint AND d.child_source_id=(e->>1)::bigint)) THEN
        RAISE EXCEPTION 'Changed topology denied' USING ERRCODE='check_violation'; END IF;
    RETURN true;
END $$;
REVOKE ALL ON FUNCTION rag.verify_worker_grant(UUID,UUID,VARCHAR,VARCHAR,VARCHAR,VARCHAR) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION rag.verify_worker_grant(UUID,UUID,VARCHAR,VARCHAR,VARCHAR,VARCHAR) TO rag_worker;
-- Superseded private helpers cannot be used as alternate authority routes.
DROP FUNCTION rag.verify_worker_grant_scope(UUID,UUID,VARCHAR,VARCHAR,VARCHAR,VARCHAR);

-- Provenance cannot be manufactured by the non-owner Java runtime's raw staging DML.
CREATE FUNCTION rag.enforce_receipt_writer() RETURNS trigger LANGUAGE plpgsql
    SECURITY DEFINER SET search_path=pg_catalog,rag AS $$
BEGIN
    IF (NEW.worker_receipt OR (TG_OP='UPDATE' AND OLD.worker_receipt)) AND session_user<>'rag_worker' THEN
        RAISE EXCEPTION 'Restricted receipt writer required' USING ERRCODE='insufficient_privilege';
    END IF;
    RETURN NEW;
END $$;
REVOKE ALL ON FUNCTION rag.enforce_receipt_writer() FROM PUBLIC,rag_runtime,rag_worker;
CREATE TRIGGER trg_receipt_writer BEFORE INSERT OR UPDATE ON rag.staged_outputs
    FOR EACH ROW EXECUTE FUNCTION rag.enforce_receipt_writer();
