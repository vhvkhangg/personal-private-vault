-- Issued, attempt-scoped foundation worker grants and typed durable receipts.
-- Existing migrations remain immutable. Every legacy attempt requires fresh admission.
ALTER ROLE rag_worker NOLOGIN PASSWORD NULL NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS NOREPLICATION;
DO $$
DECLARE membership record;
BEGIN
    FOR membership IN SELECT r.rolname FROM pg_catalog.pg_auth_members m
        JOIN pg_catalog.pg_roles r ON r.oid=m.roleid
        WHERE m.member=(SELECT oid FROM pg_catalog.pg_roles WHERE rolname='rag_worker')
    LOOP EXECUTE format('REVOKE %I FROM rag_worker', membership.rolname); END LOOP;
    IF EXISTS (SELECT 1 FROM pg_catalog.pg_class WHERE relowner=(SELECT oid FROM pg_catalog.pg_roles WHERE rolname='rag_worker'))
        OR EXISTS (SELECT 1 FROM pg_catalog.pg_namespace WHERE nspowner=(SELECT oid FROM pg_catalog.pg_roles WHERE rolname='rag_worker'))
        OR EXISTS (SELECT 1 FROM pg_catalog.pg_proc WHERE proowner=(SELECT oid FROM pg_catalog.pg_roles WHERE rolname='rag_worker')) THEN
        RAISE EXCEPTION 'Unsafe existing worker ownership requires operator repair';
    END IF;
    IF NOT EXISTS (SELECT 1 FROM pg_catalog.pg_roles WHERE rolname='rag_runtime') THEN
        CREATE ROLE rag_runtime NOLOGIN NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS NOREPLICATION;
    END IF;
END $$;
GRANT USAGE ON SCHEMA public,rag TO rag_runtime;
GRANT SELECT,INSERT,UPDATE,DELETE ON ALL TABLES IN SCHEMA public,rag TO rag_runtime;
GRANT USAGE,SELECT ON ALL SEQUENCES IN SCHEMA public,rag TO rag_runtime;
REVOKE INSERT,UPDATE,DELETE ON public.flyway_schema_history FROM rag_runtime;
REVOKE ALL ON ALL TABLES IN SCHEMA public,rag FROM rag_worker;
REVOKE ALL ON ALL SEQUENCES IN SCHEMA public,rag FROM rag_worker;
REVOKE ALL ON SCHEMA public FROM rag_worker;
REVOKE EXECUTE ON ALL FUNCTIONS IN SCHEMA rag FROM PUBLIC,rag_worker;
GRANT USAGE ON SCHEMA rag TO rag_worker;
ALTER ROLE rag_worker SET search_path = pg_catalog,rag;
ALTER ROLE rag_worker SET statement_timeout = '5s';
ALTER ROLE rag_worker SET lock_timeout = '3s';
ALTER ROLE rag_worker SET idle_in_transaction_session_timeout = '5s';
ALTER ROLE rag_worker SET transaction_timeout = '10s';

CREATE TABLE rag.worker_grants (
    job_id UUID NOT NULL,
    attempt_id UUID NOT NULL,
    session_id UUID NOT NULL UNIQUE,
    session_until TIMESTAMPTZ NOT NULL,
    input_digest VARCHAR(64) NOT NULL CHECK (input_digest ~ '^[0-9a-f]{64}$'),
    scope_digest VARCHAR(64) NOT NULL CHECK (scope_digest ~ '^[0-9a-f]{64}$'),
    output_digest VARCHAR(64) NOT NULL CHECK (output_digest ~ '^[0-9a-f]{64}$'),
    stage_type VARCHAR(32) NOT NULL CHECK (stage_type IN ('SYNTHETIC_STAGE','SYNTHETIC_TEST')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY(job_id,attempt_id),
    FOREIGN KEY(job_id,attempt_id) REFERENCES rag.job_leases(job_id,attempt_id)
);
ALTER TABLE rag.staged_outputs ADD COLUMN worker_receipt BOOLEAN NOT NULL DEFAULT false;
GRANT SELECT,INSERT,DELETE ON rag.worker_grants TO rag_runtime;
REVOKE ALL ON rag.worker_grants FROM PUBLIC,rag_worker;
UPDATE rag.job_leases SET status='CANCELLED' WHERE status IN ('ADMITTED','ACTIVE');

CREATE FUNCTION rag.verify_worker_grant(p_job UUID,p_attempt UUID,p_token VARCHAR,p_stage VARCHAR,p_input VARCHAR,p_scope VARCHAR)
RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,rag AS $$
DECLARE l rag.job_leases%ROWTYPE; g rag.worker_grants%ROWTYPE; n jsonb; manifest jsonb; s rag.sources%ROWTYPE;
BEGIN
    IF p_token IS NULL OR p_stage IS NULL OR p_input IS NULL OR p_scope IS NULL THEN
        RAISE EXCEPTION 'Worker grant denied' USING ERRCODE='check_violation'; END IF;
    SELECT * INTO l FROM rag.job_leases WHERE job_id=p_job AND attempt_id=p_attempt FOR UPDATE;
    IF NOT FOUND OR l.lease_token<>p_token OR l.status NOT IN ('ADMITTED','ACTIVE')
        OR l.leased_until<=clock_timestamp() THEN RAISE EXCEPTION 'Worker grant denied' USING ERRCODE='check_violation'; END IF;
    SELECT * INTO g FROM rag.worker_grants WHERE job_id=p_job AND attempt_id=p_attempt;
    IF NOT FOUND OR g.session_until<=clock_timestamp() OR g.stage_type<>p_stage
        OR g.input_digest<>p_input OR g.scope_digest<>p_scope THEN
        RAISE EXCEPTION 'Worker grant denied' USING ERRCODE='check_violation';
    END IF;
    manifest:=l.dependency_manifest::jsonb;
    IF manifest->>'contract_version'<>'2.0' OR jsonb_typeof(manifest->'nodes')<>'array'
        OR jsonb_array_length(manifest->'nodes') NOT BETWEEN 1 AND 101 THEN
        RAISE EXCEPTION 'Worker manifest denied' USING ERRCODE='check_violation';
    END IF;
    FOR n IN SELECT * FROM jsonb_array_elements(manifest->'nodes') LOOP
        SELECT * INTO s FROM rag.sources WHERE id=(n->>'source_id')::bigint;
        IF NOT FOUND OR s.source_type NOT IN ('FICTION','FILM') OR s.access_state<>'APPROVED'
            OR s.classification<>'NON_PRIVATE' OR s.authorization_epoch<>(n->>'epoch')::bigint
            OR s.field_policy_version<>(n->>'field_version')::int OR s.source_revision_hash<>n->>'content_revision'
            OR to_jsonb(string_to_array(s.approved_fields_text,','))<>n->'fields'
            OR (n->>'description_revision'<>'' AND s.description_revision_hash<>n->>'description_revision') THEN
            RAISE EXCEPTION 'Worker source authority denied' USING ERRCODE='check_violation';
        END IF;
    END LOOP;
    IF EXISTS (
        SELECT 1 FROM rag.dependencies d
        WHERE d.parent_source_id IN (SELECT (v->>'source_id')::bigint FROM jsonb_array_elements(manifest->'nodes') v)
        AND NOT EXISTS (SELECT 1 FROM jsonb_array_elements(manifest->'edges') e
            WHERE (e->>0)::bigint=d.parent_source_id AND (e->>1)::bigint=d.child_source_id)
    ) OR EXISTS (
        SELECT 1 FROM jsonb_array_elements(manifest->'edges') e
        WHERE NOT EXISTS (SELECT 1 FROM rag.dependencies d WHERE d.parent_source_id=(e->>0)::bigint AND d.child_source_id=(e->>1)::bigint)
    ) THEN RAISE EXCEPTION 'Worker topology authority denied' USING ERRCODE='check_violation'; END IF;
    RETURN true;
END $$;
REVOKE ALL ON FUNCTION rag.verify_worker_grant(UUID,UUID,VARCHAR,VARCHAR,VARCHAR,VARCHAR) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION rag.verify_worker_grant(UUID,UUID,VARCHAR,VARCHAR,VARCHAR,VARCHAR) TO rag_worker;

CREATE FUNCTION rag.stage_worker_receipt(p_job UUID,p_attempt UUID,p_token VARCHAR,p_receipt TEXT)
RETURNS bigint LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,rag AS $$
DECLARE r jsonb; g rag.worker_grants%ROWTYPE; result_id bigint;
BEGIN
    IF session_user<>'rag_worker' THEN RAISE EXCEPTION 'Restricted worker identity required' USING ERRCODE='insufficient_privilege'; END IF;
    IF octet_length(p_receipt)>4096 THEN RAISE EXCEPTION 'Receipt budget exceeded' USING ERRCODE='check_violation'; END IF;
    r:=p_receipt::jsonb;
    IF jsonb_typeof(r) IS DISTINCT FROM 'object'
        OR NOT r ?& ARRAY['contract_version','status','job_id','attempt_id','output_kind','stage_type','input_digest','scope_digest','output_digest']
        OR (SELECT count(*) FROM jsonb_object_keys(r))<>9
        OR EXISTS(SELECT 1 FROM jsonb_each(r) field WHERE jsonb_typeof(field.value) IS DISTINCT FROM 'string') THEN
        RAISE EXCEPTION 'Invalid worker receipt' USING ERRCODE='check_violation'; END IF;
    IF r->>'contract_version'<>'2.0' OR r->>'status'<>'STAGED'
        OR r->>'job_id'<>p_job::text OR r->>'attempt_id'<>p_attempt::text
        OR r->>'output_kind'<>'SYNTHETIC_DIGEST' THEN
        RAISE EXCEPTION 'Invalid worker receipt' USING ERRCODE='check_violation';
    END IF;
    PERFORM rag.verify_worker_grant(p_job,p_attempt,p_token,(r->>'stage_type')::varchar,
        (r->>'input_digest')::varchar,(r->>'scope_digest')::varchar);
    SELECT * INTO g FROM rag.worker_grants WHERE job_id=p_job AND attempt_id=p_attempt;
    IF r->>'output_digest' IS DISTINCT FROM g.output_digest THEN
        RAISE EXCEPTION 'Invalid worker digest' USING ERRCODE='check_violation'; END IF;
    SELECT id INTO result_id FROM rag.staged_outputs WHERE job_id=p_job AND attempt_id=p_attempt;
    IF FOUND THEN
        IF NOT EXISTS (SELECT 1 FROM rag.staged_outputs WHERE id=result_id AND payload::jsonb=r) THEN
            RAISE EXCEPTION 'Conflicting duplicate receipt' USING ERRCODE='check_violation'; END IF;
        RETURN result_id;
    END IF;
    INSERT INTO rag.staged_outputs(job_id,attempt_id,lease_token,stage_type,payload,created_at,worker_receipt)
        VALUES(p_job,p_attempt,p_token,r->>'stage_type',p_receipt,clock_timestamp(),true) RETURNING id INTO result_id;
    RETURN result_id;
END $$;
REVOKE ALL ON FUNCTION rag.stage_worker_receipt(UUID,UUID,VARCHAR,TEXT) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION rag.stage_worker_receipt(UUID,UUID,VARCHAR,TEXT) TO rag_worker;
