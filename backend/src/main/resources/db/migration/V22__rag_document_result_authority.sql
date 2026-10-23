-- Slice 3 corrective checkpoint. Existing RAG tables/columns and attempt fence only.
-- Historical V1-V21 remain immutable. No role gains business/publication authority.
REVOKE INSERT, UPDATE ON rag.staged_document_outputs FROM rag_runtime;

-- Retain immutable Foundation identity; v3 may bind ONE DB-credential-free source
-- verifier before resume, preserving the original producer identity in the same row.
CREATE OR REPLACE FUNCTION rag.guard_attempt_state() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE verification_binding boolean:=false; scrub boolean;
BEGIN
    IF TG_OP='DELETE' THEN
        IF NOT OLD.staging_closed OR OLD.status NOT IN ('COMPLETED','CANCELLED','ABORTED','PURGED') THEN
            RAISE EXCEPTION 'Nonterminal attempt retention denied' USING ERRCODE='check_violation'; END IF;
        RETURN OLD;
    END IF;
    IF TG_OP='UPDATE' THEN
        scrub:=OLD.staging_closed AND OLD.status IN ('COMPLETED','CANCELLED','ABORTED','PURGED')
            AND NEW.lease_token='SCRUBBED' AND NEW.approved_fields_text='' AND NEW.content_revision_hash=''
            AND NEW.description_revision_hash='' AND NEW.dependency_manifest='SCRUBBED'
            AND NOT EXISTS(SELECT 1 FROM rag.staged_outputs WHERE job_id=OLD.job_id)
            AND NOT EXISTS(SELECT 1 FROM rag.worker_grants WHERE job_id=OLD.job_id)
            AND (NOT EXISTS(SELECT 1 FROM rag.index_generations WHERE job_id=OLD.job_id AND is_active)
                OR (EXISTS(SELECT 1 FROM rag.batch_roots WHERE job_id=OLD.job_id)
                    AND NOT EXISTS(SELECT 1 FROM rag.index_generations g
                        WHERE g.job_id=OLD.job_id AND g.is_active AND NOT EXISTS(
                            SELECT 1 FROM rag.generation_roots r WHERE r.generation_id=g.id
                            AND jsonb_typeof(r.exact_dependency_manifest->'manifest_text')='string'))));
        verification_binding:=OLD.status='ACTIVE' AND NEW.status='ACTIVE' AND NOT OLD.staging_closed
            AND OLD.worker_identity IS NOT NULL AND NOT OLD.worker_identity ? 'verification'
            AND NEW.worker_identity - 'verification'=OLD.worker_identity
            AND jsonb_typeof(NEW.worker_identity->'verification')='object'
            AND NEW.worker_identity->'verification' ?& ARRAY['process_id','creation_file_time']
            AND jsonb_typeof(NEW.worker_identity->'verification'->'process_id')='number'
            AND (NEW.worker_identity->'verification'->>'process_id')::bigint>0
            AND (NEW.worker_identity->'verification'->>'creation_file_time')::bigint>0
            AND octet_length(NEW.worker_identity::text)<=4096
            AND EXISTS(SELECT 1 FROM rag.worker_grants WHERE job_id=OLD.job_id AND attempt_id=OLD.attempt_id
                AND stage_type='DOCUMENT_V3' AND consumed_at IS NOT NULL);
        IF (NEW.job_id,NEW.attempt_id,NEW.source_id,NEW.leased_until,NEW.reservation_token,NEW.admitted_epoch,NEW.admitted_field_policy_version)
                IS DISTINCT FROM (OLD.job_id,OLD.attempt_id,OLD.source_id,OLD.leased_until,OLD.reservation_token,OLD.admitted_epoch,OLD.admitted_field_policy_version)
            OR (NOT scrub AND (NEW.lease_token,NEW.approved_fields_text,NEW.content_revision_hash,NEW.description_revision_hash,NEW.dependency_manifest)
                IS DISTINCT FROM (OLD.lease_token,OLD.approved_fields_text,OLD.content_revision_hash,OLD.description_revision_hash,OLD.dependency_manifest))
            OR (OLD.staging_closed AND NOT NEW.staging_closed)
            OR (OLD.controller_identity IS NOT NULL AND NEW.controller_identity IS DISTINCT FROM OLD.controller_identity)
            OR (OLD.worker_identity IS NOT NULL AND NEW.worker_identity IS DISTINCT FROM OLD.worker_identity
                AND NOT coalesce(verification_binding,false))
            OR (OLD.status IN ('COMPLETED','CANCELLED','ABORTED','PURGED') AND NEW.status<>OLD.status) THEN
            RAISE EXCEPTION 'Immutable attempt authority denied' USING ERRCODE='check_violation'; END IF;
    END IF;
    RETURN NEW;
END $$;

CREATE FUNCTION rag.guard_document_result_authority() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE l rag.job_leases%ROWTYPE;
BEGIN
    SELECT * INTO l FROM rag.job_leases WHERE job_id=OLD.job_id FOR UPDATE;
    IF NOT FOUND OR NOT l.staging_closed OR l.status NOT IN ('COMPLETED','ABORTED','CANCELLED','PURGED') THEN
        RAISE EXCEPTION 'Nonterminal document evidence retention denied' USING ERRCODE='insufficient_privilege';
    END IF;
    RETURN OLD;
END $$;
REVOKE ALL ON FUNCTION rag.guard_document_result_authority() FROM PUBLIC,rag_runtime,rag_worker;
CREATE TRIGGER trg_document_result_retention BEFORE DELETE ON rag.staged_document_outputs
    FOR EACH ROW EXECUTE FUNCTION rag.guard_document_result_authority();

CREATE FUNCTION rag.guard_document_grant_authority() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE l rag.job_leases%ROWTYPE;
BEGIN
    IF TG_OP='INSERT' THEN
        IF NEW.stage_type='DOCUMENT_V3' AND (NEW.consumed_at IS NOT NULL OR NEW.output_digest <>
                encode(sha256(convert_to('DOCUMENT_V3:' || NEW.input_digest,'UTF8')),'hex')) THEN
            RAISE EXCEPTION 'Pre-consumed document issuance denied' USING ERRCODE='insufficient_privilege';
        END IF;
        RETURN NEW;
    END IF;
    IF OLD.stage_type='DOCUMENT_V3' THEN
        SELECT * INTO l FROM rag.job_leases WHERE job_id=OLD.job_id FOR UPDATE;
        IF NOT FOUND OR NOT l.staging_closed OR l.status NOT IN ('COMPLETED','ABORTED','CANCELLED','PURGED') THEN
            RAISE EXCEPTION 'Nonterminal document issuance retention denied' USING ERRCODE='insufficient_privilege';
        END IF;
    END IF;
    RETURN OLD;
END $$;
REVOKE ALL ON FUNCTION rag.guard_document_grant_authority() FROM PUBLIC,rag_runtime,rag_worker;
CREATE TRIGGER trg_document_grant_authority BEFORE INSERT OR DELETE ON rag.worker_grants
    FOR EACH ROW EXECUTE FUNCTION rag.guard_document_grant_authority();

-- PG jsonb canonical form is used on BOTH sides of seal comparison; array order is significant.
CREATE FUNCTION rag.document_result_digest(p_job uuid,p_attempt uuid) RETURNS text
LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog AS $$
    SELECT encode(sha256(convert_to(jsonb_build_object(
        'job',g.job_id,'attempt',g.attempt_id,'input',g.input_digest,'scope',g.scope_digest,
        'results',(SELECT jsonb_agg(jsonb_build_object('root_key',s.root_key,'success',s.success,
            'reason_code',s.reason_code,'warnings',s.warning_codes,'chunks',s.chunks_payload,
            'resources',s.resource_report,'worker_receipt',s.worker_receipt) ORDER BY s.id)
            FROM rag.staged_document_outputs s WHERE s.job_id=g.job_id AND s.attempt_id=g.attempt_id)
        )::text,'UTF8')),'hex') FROM rag.worker_grants g
        WHERE g.job_id=p_job AND g.attempt_id=p_attempt AND g.stage_type='DOCUMENT_V3';
$$;
REVOKE ALL ON FUNCTION rag.document_result_digest(uuid,uuid) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION rag.document_result_digest(uuid,uuid) TO rag_runtime;

CREATE FUNCTION rag.seal_document_result(p_job uuid,p_attempt uuid,p_token varchar) RETURNS text
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE l rag.job_leases%ROWTYPE; result text;
BEGIN
    l:=rag.check_worker_attempt(p_job,p_attempt,p_token,true);
    IF NOT EXISTS(SELECT 1 FROM rag.worker_grants WHERE job_id=p_job AND attempt_id=p_attempt
        AND stage_type='DOCUMENT_V3' AND output_digest=encode(sha256(convert_to('DOCUMENT_V3:'||input_digest,'UTF8')),'hex'))
        OR EXISTS(SELECT root_key FROM rag.batch_roots WHERE job_id=p_job
            EXCEPT SELECT root_key FROM rag.staged_document_outputs WHERE job_id=p_job AND attempt_id=p_attempt)
        OR EXISTS(SELECT root_key FROM rag.staged_document_outputs WHERE job_id=p_job AND attempt_id=p_attempt
            EXCEPT SELECT root_key FROM rag.batch_roots WHERE job_id=p_job)
        OR (SELECT jsonb_agg(root->'root_key' ORDER BY ordinal) FROM jsonb_array_elements(l.dependency_manifest::jsonb->'roots') WITH ORDINALITY AS r(root,ordinal))
            IS DISTINCT FROM (SELECT jsonb_agg(to_jsonb(root_key) ORDER BY id) FROM rag.staged_document_outputs WHERE job_id=p_job AND attempt_id=p_attempt)
        OR EXISTS(SELECT 1 FROM rag.staged_document_outputs WHERE job_id=p_job AND attempt_id=p_attempt
            AND (NOT worker_receipt OR (NOT success AND (jsonb_array_length(chunks_payload)<>0 OR reason_code IN ('SUCCESS','UNKNOWN'))))) THEN
        RAISE EXCEPTION 'Incomplete or already sealed document result' USING ERRCODE='check_violation';
    END IF;
    result:=rag.document_result_digest(p_job,p_attempt);
    UPDATE rag.worker_grants SET output_digest=result WHERE job_id=p_job AND attempt_id=p_attempt;
    RETURN result;
END $$;
REVOKE ALL ON FUNCTION rag.seal_document_result(uuid,uuid,varchar) FROM PUBLIC,rag_runtime;
GRANT EXECUTE ON FUNCTION rag.seal_document_result(uuid,uuid,varchar) TO rag_worker;

CREATE OR REPLACE FUNCTION rag.stage_document_result(
    p_job_id uuid,p_attempt_id uuid,p_lease_token varchar,p_root_key varchar,p_success boolean,
    p_reason_code varchar,p_warning_codes jsonb,p_chunks_payload jsonb,p_resource_report jsonb
) RETURNS bigint LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE l rag.job_leases%ROWTYPE; g rag.worker_grants%ROWTYPE; staged_id bigint;
BEGIN
    l:=rag.check_worker_attempt(p_job_id,p_attempt_id,p_lease_token,true);
    SELECT * INTO g FROM rag.worker_grants WHERE job_id=p_job_id AND attempt_id=p_attempt_id;
    IF NOT FOUND OR g.stage_type<>'DOCUMENT_V3'
        OR g.output_digest<>encode(sha256(convert_to('DOCUMENT_V3:'||g.input_digest,'UTF8')),'hex')
        OR p_root_key IS NULL OR p_reason_code IS NULL OR p_chunks_payload IS NULL OR p_success IS NULL
        OR jsonb_typeof(p_chunks_payload)<>'array' OR jsonb_typeof(p_warning_codes)<>'array'
        OR jsonb_typeof(p_resource_report)<>'object'
        OR jsonb_array_length(p_chunks_payload)>1000 OR octet_length(p_chunks_payload::text)>33554432
        OR (p_success AND (p_reason_code<>'SUCCESS' OR jsonb_array_length(p_chunks_payload)=0))
        OR (NOT p_success AND (p_reason_code IN ('SUCCESS','UNKNOWN') OR jsonb_array_length(p_chunks_payload)<>0))
        OR NOT EXISTS(SELECT 1 FROM rag.batch_roots WHERE job_id=p_job_id AND root_key=p_root_key) THEN
        RAISE EXCEPTION 'Document staging contract denied' USING ERRCODE='check_violation';
    END IF;
    INSERT INTO rag.staged_document_outputs(job_id,attempt_id,root_key,success,reason_code,warning_codes,chunks_payload,resource_report,worker_receipt)
        VALUES(p_job_id,p_attempt_id,p_root_key,p_success,p_reason_code,p_warning_codes,p_chunks_payload,p_resource_report,true)
        RETURNING id INTO staged_id;
    RETURN staged_id;
END $$;

-- Dependency authority uses the same graph -> Vault -> external order as admission.
CREATE OR REPLACE FUNCTION rag.guard_dependency_change() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE parents bigint[]; ancestors bigint[]; owners bigint[]; externals uuid[];
BEGIN
    IF NOT pg_try_advisory_xact_lock(5927097059089141831) THEN
        RAISE EXCEPTION 'Dependency graph busy' USING ERRCODE='P1701'; END IF;
    parents:=CASE TG_OP WHEN 'INSERT' THEN ARRAY[NEW.parent_source_id] WHEN 'DELETE' THEN ARRAY[OLD.parent_source_id]
        ELSE ARRAY[OLD.parent_source_id,NEW.parent_source_id] END;
    WITH RECURSIVE affected(id) AS (
        SELECT unnest(parents) UNION SELECT d.parent_source_id FROM rag.dependencies d JOIN affected a ON d.child_source_id=a.id
    ) SELECT array_agg(id ORDER BY id) INTO ancestors FROM affected;
    IF cardinality(ancestors) NOT BETWEEN 1 AND 100
        OR (SELECT count(*) FROM rag.sources WHERE id=ANY(ancestors))<>cardinality(ancestors)
        OR EXISTS(SELECT 1 FROM rag.sources WHERE id=ANY(ancestors) AND source_type NOT IN ('FICTION','FILM','EXTERNAL_FILE')) THEN
        RAISE EXCEPTION 'Dependency owner scope unavailable' USING ERRCODE='check_violation'; END IF;
    SELECT array_agg(DISTINCT source_id::bigint ORDER BY source_id::bigint) INTO owners
        FROM rag.sources WHERE id=ANY(ancestors) AND source_type IN ('FICTION','FILM');
    SELECT array_agg(DISTINCT source_id::uuid ORDER BY source_id::uuid) INTO externals
        FROM rag.sources WHERE id=ANY(ancestors) AND source_type='EXTERNAL_FILE';
    IF cardinality(owners)>0 THEN PERFORM public.vault_rag_apply_change(owners,true,false); END IF;
    IF cardinality(externals)>0 THEN
        PERFORM rag.external_file_require_editable(externals);
        UPDATE rag.external_file_sources SET source_version=source_version+1,updated_at=clock_timestamp()
            WHERE file_source_id=ANY(externals);
    END IF;
    IF TG_OP='DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END $$;
