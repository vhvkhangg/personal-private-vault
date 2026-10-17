-- Alternative B execution/staging SQL. Upgrade requires actual quiescence of
-- legacy consumer processes; a migration cannot infer Windows process death.
-- Refuse unresolved historical attempts, rather than freeing capacity by fiat.
DO $$ BEGIN
    IF EXISTS(SELECT 1 FROM rag.job_leases WHERE status NOT IN ('COMPLETED','PURGED')) THEN
        RAISE EXCEPTION 'Verified quiescent upgrade required for legacy RAG attempts';
    END IF;
END $$;

ALTER TABLE rag.job_leases
    ADD COLUMN reservation_token uuid,
    ADD COLUMN staging_closed boolean NOT NULL DEFAULT false,
    ADD COLUMN controller_identity jsonb,
    ADD COLUMN worker_identity jsonb;
UPDATE rag.job_leases SET reservation_token=attempt_id,staging_closed=true;
ALTER TABLE rag.job_leases ALTER COLUMN reservation_token SET NOT NULL;
ALTER TABLE rag.job_leases DROP CONSTRAINT ck_rag_job_state;
ALTER TABLE rag.job_leases ADD CONSTRAINT ck_rag_job_state CHECK(status IN (
    'ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING','COMPLETED','CANCELLED','ABORTED','PURGED'));
ALTER TABLE rag.job_leases ADD CONSTRAINT ck_rag_terminal_seal CHECK(
    status NOT IN ('COMPLETED','CANCELLED','ABORTED','PURGED') OR staging_closed);
CREATE UNIQUE INDEX uq_rag_single_active_attempt ON rag.job_leases((true))
    WHERE status IN ('ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING');
ALTER TABLE rag.worker_grants ADD COLUMN consumed_at timestamptz;
REVOKE UPDATE ON rag.worker_grants FROM PUBLIC,rag_worker,rag_runtime;

CREATE FUNCTION rag.guard_attempt_state() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN
    IF TG_OP='DELETE' THEN
        IF NOT OLD.staging_closed OR OLD.status NOT IN ('COMPLETED','CANCELLED','ABORTED','PURGED') THEN
            RAISE EXCEPTION 'Nonterminal attempt retention denied' USING ERRCODE='check_violation'; END IF;
        RETURN OLD;
    END IF;
    IF TG_OP='UPDATE' THEN
        IF (NEW.job_id,NEW.attempt_id,NEW.source_id,NEW.lease_token,NEW.leased_until,NEW.reservation_token,
            NEW.admitted_epoch,NEW.admitted_field_policy_version,NEW.approved_fields_text,
            NEW.content_revision_hash,NEW.description_revision_hash,NEW.dependency_manifest)
            IS DISTINCT FROM
            (OLD.job_id,OLD.attempt_id,OLD.source_id,OLD.lease_token,OLD.leased_until,OLD.reservation_token,
            OLD.admitted_epoch,OLD.admitted_field_policy_version,OLD.approved_fields_text,
            OLD.content_revision_hash,OLD.description_revision_hash,OLD.dependency_manifest)
            OR (OLD.staging_closed AND NOT NEW.staging_closed)
            OR (OLD.controller_identity IS NOT NULL AND NEW.controller_identity IS DISTINCT FROM OLD.controller_identity)
            OR (OLD.worker_identity IS NOT NULL AND NEW.worker_identity IS DISTINCT FROM OLD.worker_identity)
            OR (OLD.status IN ('COMPLETED','CANCELLED','ABORTED','PURGED') AND NEW.status<>OLD.status) THEN
            RAISE EXCEPTION 'Immutable attempt authority denied' USING ERRCODE='check_violation'; END IF;
    END IF;
    RETURN NEW;
END $$;
REVOKE ALL ON FUNCTION rag.guard_attempt_state() FROM PUBLIC,rag_worker,rag_runtime;
CREATE TRIGGER trg_attempt_authority BEFORE UPDATE OR DELETE ON rag.job_leases
    FOR EACH ROW EXECUTE FUNCTION rag.guard_attempt_state();

-- This private helper contains only RAG execution policy, never Vault/business
-- reads. Its caller retains the attempt lock through claim/staging physical C.
CREATE FUNCTION rag.check_worker_attempt(p_job uuid,p_attempt uuid,p_token varchar,p_consumed boolean)
RETURNS rag.job_leases LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE lease rag.job_leases%ROWTYPE; grant_row rag.worker_grants%ROWTYPE; runtime uuid;
BEGIN
    IF session_user<>'rag_worker' OR p_job IS NULL OR p_attempt IS NULL OR p_token IS NULL THEN
        RAISE EXCEPTION 'Worker execution denied' USING ERRCODE='insufficient_privilege'; END IF;
    SELECT * INTO lease FROM rag.job_leases WHERE job_id=p_job AND attempt_id=p_attempt FOR UPDATE;
    IF NOT FOUND OR lease.lease_token<>p_token OR lease.status NOT IN ('ADMITTED','ACTIVE')
        OR lease.staging_closed OR lease.leased_until<=clock_timestamp() THEN
        RAISE EXCEPTION 'Worker execution denied' USING ERRCODE='check_violation'; END IF;
    SELECT current_instance INTO runtime FROM rag.runtime_state WHERE singleton;
    IF runtime IS NULL OR jsonb_typeof(lease.controller_identity) IS DISTINCT FROM 'object'
        OR jsonb_typeof(lease.worker_identity) IS DISTINCT FROM 'object'
        OR octet_length(lease.controller_identity::text)>4096 OR octet_length(lease.worker_identity::text)>4096
        OR NOT lease.controller_identity ?& ARRAY['process_id','started_at','runtime_id']
        OR NOT lease.worker_identity ?& ARRAY['process_id','started_at','endpoint_nonce']
        OR jsonb_typeof(lease.controller_identity->'process_id') IS DISTINCT FROM 'number'
        OR jsonb_typeof(lease.worker_identity->'process_id') IS DISTINCT FROM 'number'
        OR jsonb_typeof(lease.controller_identity->'started_at') IS DISTINCT FROM 'string'
        OR jsonb_typeof(lease.worker_identity->'started_at') IS DISTINCT FROM 'string'
        OR (lease.controller_identity->>'runtime_id')::uuid IS DISTINCT FROM runtime
        OR (lease.controller_identity->>'process_id')::bigint<=0 OR (lease.worker_identity->>'process_id')::bigint<=0
        OR (lease.controller_identity->>'started_at')::timestamptz IS NULL OR (lease.worker_identity->>'started_at')::timestamptz IS NULL
        OR (lease.worker_identity->>'endpoint_nonce')::uuid IS NULL THEN
        RAISE EXCEPTION 'Unbound worker process denied' USING ERRCODE='check_violation'; END IF;
    SELECT * INTO grant_row FROM rag.worker_grants WHERE job_id=p_job AND attempt_id=p_attempt;
    IF NOT FOUND OR grant_row.session_until<=clock_timestamp() OR (p_consumed AND grant_row.consumed_at IS NULL) THEN
        RAISE EXCEPTION 'Worker issuance denied' USING ERRCODE='check_violation'; END IF;
    RETURN lease;
END $$;
REVOKE ALL ON FUNCTION rag.check_worker_attempt(uuid,uuid,varchar,boolean) FROM PUBLIC,rag_worker,rag_runtime;

CREATE FUNCTION rag.claim_worker_input(p_job uuid,p_attempt uuid,p_token varchar,p_session uuid) RETURNS boolean
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN
    PERFORM rag.check_worker_attempt(p_job,p_attempt,p_token,false);
    UPDATE rag.worker_grants SET consumed_at=clock_timestamp()
        WHERE job_id=p_job AND attempt_id=p_attempt AND session_id=p_session AND consumed_at IS NULL
            AND session_until>clock_timestamp();
    IF NOT FOUND THEN RAISE EXCEPTION 'Worker input already consumed or denied' USING ERRCODE='check_violation'; END IF;
    RETURN true;
END $$;
REVOKE ALL ON FUNCTION rag.claim_worker_input(uuid,uuid,varchar,uuid) FROM PUBLIC,rag_runtime;
GRANT EXECUTE ON FUNCTION rag.claim_worker_input(uuid,uuid,varchar,uuid) TO rag_worker;

CREATE OR REPLACE FUNCTION rag.verify_worker_grant(p_job UUID,p_attempt UUID,p_token VARCHAR,p_stage VARCHAR,p_input VARCHAR,p_scope VARCHAR)
RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE lease rag.job_leases%ROWTYPE; grant_row rag.worker_grants%ROWTYPE; source_row rag.sources%ROWTYPE;
    manifest jsonb; node jsonb; edge jsonb; selected_ids bigint[]; actual_ids bigint[];
BEGIN
    lease:=rag.check_worker_attempt(p_job,p_attempt,p_token,true);
    SELECT * INTO grant_row FROM rag.worker_grants WHERE job_id=p_job AND attempt_id=p_attempt;
    IF p_stage IS NULL OR p_input IS NULL OR p_scope IS NULL OR grant_row.stage_type<>p_stage
        OR grant_row.input_digest<>p_input OR grant_row.scope_digest<>p_scope
        OR grant_row.scope_digest<>encode(sha256(convert_to(lease.dependency_manifest,'UTF8')),'hex') THEN
        RAISE EXCEPTION 'Worker issuance denied' USING ERRCODE='check_violation'; END IF;
    manifest:=lease.dependency_manifest::jsonb;
    IF jsonb_typeof(manifest) IS DISTINCT FROM 'object' OR manifest->>'contract_version' IS DISTINCT FROM '2.0'
        OR jsonb_typeof(manifest->'nodes') IS DISTINCT FROM 'array' OR jsonb_typeof(manifest->'edges') IS DISTINCT FROM 'array'
        OR (manifest->>'root_id')::bigint IS DISTINCT FROM lease.source_id OR (SELECT count(*) FROM jsonb_object_keys(manifest))<>4
        OR jsonb_array_length(manifest->'nodes') NOT BETWEEN 1 AND 100 THEN
        RAISE EXCEPTION 'Worker manifest denied' USING ERRCODE='check_violation'; END IF;
    SELECT array_agg(DISTINCT (value->>'source_id')::bigint ORDER BY (value->>'source_id')::bigint)
        INTO selected_ids FROM jsonb_array_elements(manifest->'nodes');
    IF cardinality(selected_ids)<>jsonb_array_length(manifest->'nodes') THEN
        RAISE EXCEPTION 'Duplicate source denied' USING ERRCODE='check_violation'; END IF;
    FOR node IN SELECT * FROM jsonb_array_elements(manifest->'nodes') LOOP
        IF jsonb_typeof(node) IS DISTINCT FROM 'object' OR NOT node ?& ARRAY[
            'source_id','source_type','domain_id','epoch','field_version','fields','content_revision','description_revision',
            'owner_source_version','owner_reset_version'] OR (SELECT count(*) FROM jsonb_object_keys(node))<>10
            OR jsonb_typeof(node->'source_id') IS DISTINCT FROM 'number'
            OR jsonb_typeof(node->'epoch') IS DISTINCT FROM 'number' OR jsonb_typeof(node->'field_version') IS DISTINCT FROM 'number'
            OR jsonb_typeof(node->'owner_source_version') IS DISTINCT FROM 'number' OR jsonb_typeof(node->'owner_reset_version') IS DISTINCT FROM 'number'
            OR (node->>'owner_source_version')::bigint<=0 OR (node->>'owner_reset_version')::bigint<=0
            OR (node->>'owner_reset_version')::bigint>(node->>'owner_source_version')::bigint
            OR jsonb_typeof(node->'fields') IS DISTINCT FROM 'array' OR jsonb_typeof(node->'source_type') IS DISTINCT FROM 'string'
            OR jsonb_typeof(node->'domain_id') IS DISTINCT FROM 'string' OR jsonb_typeof(node->'content_revision') IS DISTINCT FROM 'string'
            OR jsonb_typeof(node->'description_revision') IS DISTINCT FROM 'string' THEN
            RAISE EXCEPTION 'Malformed source authority denied' USING ERRCODE='check_violation'; END IF;
        SELECT * INTO source_row FROM rag.sources WHERE id=(node->>'source_id')::bigint;
        IF NOT FOUND OR source_row.source_type NOT IN ('FICTION','FILM') OR source_row.access_state<>'APPROVED'
            OR source_row.classification<>'NON_PRIVATE' OR source_row.source_type<>node->>'source_type'
            OR source_row.source_id<>node->>'domain_id' OR source_row.authorization_epoch<>(node->>'epoch')::bigint
            OR source_row.field_policy_version<>(node->>'field_version')::int
            OR source_row.approved_owner_reset_version IS DISTINCT FROM (node->>'owner_reset_version')::bigint
            OR source_row.source_revision_hash<>node->>'content_revision'
            OR to_jsonb(string_to_array(source_row.approved_fields_text,','))<>node->'fields'
            OR (node->>'description_revision'<>'' AND source_row.description_revision_hash<>node->>'description_revision') THEN
            RAISE EXCEPTION 'Worker source authority denied' USING ERRCODE='check_violation'; END IF;
    END LOOP;
    WITH RECURSIVE closure(id) AS (
        SELECT lease.source_id UNION SELECT d.child_source_id FROM rag.dependencies d JOIN closure c ON d.parent_source_id=c.id
    ) SELECT array_agg(id ORDER BY id) INTO actual_ids FROM closure;
    IF actual_ids IS DISTINCT FROM selected_ids THEN
        RAISE EXCEPTION 'Incomplete dependency closure denied' USING ERRCODE='check_violation'; END IF;
    FOR edge IN SELECT * FROM jsonb_array_elements(manifest->'edges') LOOP
        IF jsonb_typeof(edge) IS DISTINCT FROM 'array' OR jsonb_array_length(edge)<>2
            OR jsonb_typeof(edge->0) IS DISTINCT FROM 'number' OR jsonb_typeof(edge->1) IS DISTINCT FROM 'number'
            OR NOT (edge->>0)::bigint=ANY(selected_ids) OR NOT (edge->>1)::bigint=ANY(selected_ids) THEN
            RAISE EXCEPTION 'Malformed dependency topology denied' USING ERRCODE='check_violation'; END IF;
    END LOOP;
    IF (SELECT count(DISTINCT value) FROM jsonb_array_elements(manifest->'edges'))<>jsonb_array_length(manifest->'edges')
        OR EXISTS(SELECT 1 FROM rag.dependencies d WHERE d.parent_source_id=ANY(selected_ids) AND NOT EXISTS(
            SELECT 1 FROM jsonb_array_elements(manifest->'edges') e WHERE (e->>0)::bigint=d.parent_source_id AND (e->>1)::bigint=d.child_source_id))
        OR EXISTS(SELECT 1 FROM jsonb_array_elements(manifest->'edges') e WHERE NOT EXISTS(
            SELECT 1 FROM rag.dependencies d WHERE d.parent_source_id=(e->>0)::bigint AND d.child_source_id=(e->>1)::bigint)) THEN
        RAISE EXCEPTION 'Changed dependency topology denied' USING ERRCODE='check_violation'; END IF;
    RETURN true;
END $$;

-- Consolidate the obsolete boot-bound consent trigger into the complete verifier.
-- Completed index readability never uses this execution-only runtime gate.
DROP TRIGGER trg_current_worker_consent ON rag.staged_outputs;
DROP FUNCTION rag.require_current_consent();
CREATE OR REPLACE FUNCTION rag.enforce_staging_lease_fence() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE receipt jsonb; row_count bigint; payload_bytes bigint;
BEGIN
    IF NOT NEW.worker_receipt OR NEW.payload IS NULL OR octet_length(NEW.payload)>4096
        OR NEW.stage_type NOT IN ('SYNTHETIC_STAGE','SYNTHETIC_TEST') THEN
        RAISE EXCEPTION 'Typed worker receipt required' USING ERRCODE='check_violation'; END IF;
    receipt:=NEW.payload::jsonb;
    PERFORM rag.verify_worker_grant(NEW.job_id,NEW.attempt_id,NEW.lease_token,NEW.stage_type,
        (receipt->>'input_digest')::varchar,(receipt->>'scope_digest')::varchar);
    SELECT count(*),coalesce(sum(octet_length(payload)),0) INTO row_count,payload_bytes
        FROM rag.staged_outputs WHERE job_id=NEW.job_id AND attempt_id=NEW.attempt_id;
    IF row_count>=50 OR payload_bytes+octet_length(NEW.payload)>10485760 THEN
        RAISE EXCEPTION 'Staging budget denied' USING ERRCODE='check_violation'; END IF;
    RETURN NEW;
END $$;

-- Preserve the signature, not a raw-payload bypass. Every legacy route receives
-- exactly the same typed receipt, grant, producer, attempt and deferred fences.
CREATE OR REPLACE FUNCTION rag.stage_worker_output(p_job_id UUID,p_attempt_id UUID,p_lease_token VARCHAR,p_stage_type VARCHAR,p_payload TEXT)
RETURNS bigint LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN
    IF p_payload IS NULL OR octet_length(p_payload)>4096 THEN
        RAISE EXCEPTION 'Stage contract denied' USING ERRCODE='check_violation'; END IF;
    IF (p_payload::jsonb->>'stage_type') IS DISTINCT FROM p_stage_type THEN
        RAISE EXCEPTION 'Stage contract denied' USING ERRCODE='check_violation'; END IF;
    RETURN rag.stage_worker_receipt(p_job_id,p_attempt_id,p_lease_token,p_payload);
END $$;

CREATE OR REPLACE FUNCTION rag.commit_stage_deadline() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE lease rag.job_leases%ROWTYPE; session_deadline timestamptz;
BEGIN
    SELECT * INTO lease FROM rag.job_leases WHERE job_id=NEW.job_id AND attempt_id=NEW.attempt_id FOR UPDATE;
    SELECT session_until INTO session_deadline FROM rag.worker_grants WHERE job_id=NEW.job_id AND attempt_id=NEW.attempt_id;
    IF lease.leased_until IS NULL OR lease.leased_until<=clock_timestamp() OR lease.staging_closed
        OR lease.status NOT IN ('ADMITTED','ACTIVE') OR session_deadline IS NULL OR session_deadline<=clock_timestamp() THEN
        RAISE EXCEPTION 'Deferred stage deadline denied' USING ERRCODE='check_violation'; END IF;
    RETURN NULL;
END $$;
CREATE OR REPLACE FUNCTION rag.commit_publication_deadline() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE lease rag.job_leases%ROWTYPE; session_deadline timestamptz;
BEGIN
    IF NOT EXISTS(SELECT 1 FROM rag.index_generations WHERE id=NEW.id AND is_active) THEN RETURN NULL; END IF;
    SELECT * INTO lease FROM rag.job_leases WHERE job_id=NEW.job_id FOR UPDATE;
    SELECT session_until INTO session_deadline FROM rag.worker_grants WHERE job_id=NEW.job_id;
    IF lease.leased_until IS NULL OR lease.leased_until<=clock_timestamp() OR lease.status<>'COMPLETED'
        OR NOT lease.staging_closed OR session_deadline IS NULL OR session_deadline<=clock_timestamp() THEN
        RAISE EXCEPTION 'Deferred publication deadline denied' USING ERRCODE='check_violation'; END IF;
    RETURN NULL;
END $$;
-- E1: these retained default-deferred observations are not assertions about the
-- final physical COMMIT instant. Arbitrary pauses may follow a successful gate.
REVOKE ALL ON FUNCTION rag.enforce_staging_lease_fence(),rag.commit_stage_deadline(),rag.commit_publication_deadline()
    FROM PUBLIC,rag_worker,rag_runtime;
REVOKE ALL ON FUNCTION rag.verify_worker_grant(UUID,UUID,VARCHAR,VARCHAR,VARCHAR,VARCHAR),
    rag.stage_worker_output(UUID,UUID,VARCHAR,VARCHAR,TEXT) FROM PUBLIC,rag_runtime;
GRANT EXECUTE ON FUNCTION rag.verify_worker_grant(UUID,UUID,VARCHAR,VARCHAR,VARCHAR,VARCHAR),
    rag.stage_worker_output(UUID,UUID,VARCHAR,VARCHAR,TEXT) TO rag_worker;
