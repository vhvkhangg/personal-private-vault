-- V24: Markdown retrieval active-reader exclusion and incident inspection (P19-OD-01 Option A, ADR-0025)
-- Establishes single-reader durable exclusion, inspection fences, emission CAS, and generation guards.

CREATE TABLE IF NOT EXISTS rag.retrieval_readers (
    reader_id UUID PRIMARY KEY,
    reservation_token UUID NOT NULL UNIQUE,
    scope_digest VARCHAR(64) NOT NULL,
    scope_payload JSONB NOT NULL,
    state VARCHAR(32) NOT NULL,
    stop_requested BOOLEAN NOT NULL DEFAULT false,
    emission_started BOOLEAN NOT NULL DEFAULT false,
    emission_disposition VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    controller_instance VARCHAR(255) NOT NULL,
    worker_pid BIGINT,
    worker_created_at TIMESTAMPTZ,
    worker_creation_file_time BIGINT,
    finish_disposition VARCHAR(32),
    quiescence_evidence JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT chk_retrieval_reader_state CHECK (
        state IN ('ADMITTED', 'CONSUMING', 'STOP_REQUESTED', 'EMITTING', 'RECOVERY_REQUIRED', 'TERMINAL')
    ),
    CONSTRAINT chk_retrieval_reader_emission_disposition CHECK (
        emission_disposition IN ('PENDING', 'EMITTING', 'EMITTED', 'ABORTED', 'FAILED')
    )
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_rag_retrieval_readers_active
    ON rag.retrieval_readers ((true))
    WHERE state <> 'TERMINAL';

CREATE TABLE IF NOT EXISTS rag.retrieval_inspections (
    inspection_id UUID PRIMARY KEY,
    reader_id UUID REFERENCES rag.retrieval_readers(reader_id),
    producer VARCHAR(64) NOT NULL,
    scope_digest VARCHAR(64) NOT NULL,
    affected_file_source_id UUID NOT NULL,
    affected_revision_id UUID,
    state VARCHAR(32) NOT NULL,
    deny_new_admission BOOLEAN NOT NULL DEFAULT false,
    inspection_proof JSONB NOT NULL DEFAULT '{}'::jsonb,
    details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT chk_retrieval_inspection_state CHECK (
        state IN ('UNRESOLVED', 'CLEAN', 'INCIDENT_PENDING', 'DENIED')
    )
);

CREATE INDEX IF NOT EXISTS idx_rag_retrieval_inspections_unresolved
    ON rag.retrieval_inspections(affected_file_source_id)
    WHERE state IN ('UNRESOLVED', 'INCIDENT_PENDING', 'DENIED') OR deny_new_admission;

CREATE INDEX IF NOT EXISTS idx_rag_retrieval_inspections_reader
    ON rag.retrieval_inspections(reader_id)
    WHERE reader_id IS NOT NULL;

-- Shared exact revision predicate; an unknown revision conservatively fences its file.
CREATE OR REPLACE FUNCTION rag.retrieval_inspection_blocks(p_file UUID,p_revision UUID,p_pending_only BOOLEAN DEFAULT false)
RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog AS $$
    SELECT EXISTS(SELECT 1 FROM rag.retrieval_inspections i WHERE i.affected_file_source_id=p_file
        AND (i.affected_revision_id IS NULL OR i.affected_revision_id=p_revision)
        AND (i.state IN ('UNRESOLVED','INCIDENT_PENDING')
            OR (NOT p_pending_only AND (i.state='DENIED' OR i.deny_new_admission))))
$$;
REVOKE ALL ON FUNCTION rag.retrieval_inspection_blocks(UUID,UUID,BOOLEAN) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION rag.retrieval_inspection_blocks(UUID,UUID,BOOLEAN) TO rag_runtime;

-- Complete metadata proof shared by C_A and C_E; expensive managed-byte hashing stays in Java.
CREATE OR REPLACE FUNCTION rag.retrieval_inspection_in_scope(p_scope JSONB,p_file UUID,p_revision UUID)
RETURNS boolean LANGUAGE sql IMMUTABLE SET search_path=pg_catalog AS $$
    SELECT EXISTS(SELECT 1 FROM jsonb_array_elements(coalesce(p_scope->'sources','[]'::jsonb)) t
        WHERE (t->>'file_source_id')::uuid=p_file
          AND (p_revision IS NULL OR (t->>'revision_id')::uuid=p_revision))
$$;
REVOKE ALL ON FUNCTION rag.retrieval_inspection_in_scope(JSONB,UUID,UUID) FROM PUBLIC,rag_worker,rag_runtime;

CREATE OR REPLACE FUNCTION rag.retrieval_scope_require_current(
    p_scope JSONB,p_files UUID[],p_controller VARCHAR
) RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE
    item jsonb;
    node jsonb;
    manifest jsonb;
    manifest_text text;
    policy rag.sources%ROWTYPE;
    file rag.external_file_sources%ROWTYPE;
    generation rag.index_generations%ROWTYPE;
    root rag.generation_roots%ROWTYPE;
    lease rag.job_leases%ROWTYPE;
    ids bigint[];
    supplied_files uuid[];
    actual_ids bigint[];
    root_ids bigint[];
    manifest_ids bigint[];
    root_closure bigint[];
BEGIN
    PERFORM set_config('lock_timeout','2s',true);
    PERFORM set_config('statement_timeout','5s',true);
    PERFORM set_config('transaction_timeout','10s',true);
    PERFORM pg_advisory_xact_lock(5927097059089141831);
    IF NOT EXISTS(SELECT 1 FROM rag.runtime_state WHERE current_instance::text=p_controller)
        OR jsonb_typeof(p_scope->'sources') IS DISTINCT FROM 'array'
        OR jsonb_typeof(p_scope->'authority_manifests') IS DISTINCT FROM 'array'
        OR jsonb_typeof(p_scope->'producer_manifest_digests') IS DISTINCT FROM 'array'
        OR jsonb_array_length(p_scope->'authority_manifests') NOT BETWEEN 1 AND 100
        OR jsonb_array_length(p_scope->'sources') NOT BETWEEN 1 AND 100
        OR p_scope->>'controller_pid' IS NULL OR (p_scope->>'controller_pid')::bigint<=0
        OR p_scope->>'controller_creation_file_time' IS NULL OR (p_scope->>'controller_creation_file_time')::bigint<=0 THEN
        RAISE EXCEPTION 'Retrieval scope/runtime proof denied' USING ERRCODE='check_violation';
    END IF;
    SELECT array_agg((s->>'source_id')::bigint ORDER BY (s->>'source_id')::bigint),
           array_agg((s->>'file_source_id')::uuid ORDER BY (s->>'file_source_id')::uuid)
        INTO ids,supplied_files FROM jsonb_array_elements(p_scope->'sources') s;
    IF cardinality(ids)<>(SELECT count(DISTINCT x) FROM unnest(ids) x)
        OR cardinality(supplied_files)<>(SELECT count(DISTINCT x) FROM unnest(supplied_files) x)
        OR supplied_files IS DISTINCT FROM (SELECT array_agg(x ORDER BY x) FROM unnest(p_files) x)
        OR supplied_files IS DISTINCT FROM (SELECT array_agg(x::uuid ORDER BY x::uuid)
            FROM jsonb_array_elements_text(p_scope->'external_file_ids') x)
        OR ids IS DISTINCT FROM (SELECT array_agg(x::bigint ORDER BY x::bigint)
            FROM jsonb_array_elements_text(p_scope->'source_ids') x) THEN
        RAISE EXCEPTION 'Retrieval complete scope mismatch' USING ERRCODE='check_violation';
    END IF;
    PERFORM 1 FROM rag.external_file_sources WHERE file_source_id=ANY(supplied_files) ORDER BY file_source_id FOR UPDATE;
    PERFORM 1 FROM rag.sources WHERE id=ANY(ids) ORDER BY id FOR UPDATE;
    FOR item IN SELECT s FROM jsonb_array_elements(p_scope->'sources') s LOOP
        SELECT * INTO policy FROM rag.sources WHERE id=(item->>'source_id')::bigint;
        IF NOT FOUND THEN RAISE EXCEPTION 'Retrieval source missing' USING ERRCODE='check_violation'; END IF;
        SELECT * INTO file FROM rag.external_file_sources WHERE file_source_id=(item->>'file_source_id')::uuid;
        IF NOT FOUND OR policy.source_type<>'EXTERNAL_FILE' OR policy.source_id<>file.file_source_id::text
            OR policy.classification<>'NON_PRIVATE' OR policy.access_state<>'APPROVED'
            OR NOT file.eligible OR file.snapshot_integrity_failed
            OR file.selected_revision_id IS DISTINCT FROM (item->>'revision_id')::uuid
            OR file.source_version IS DISTINCT FROM (item->>'owner_source_version')::bigint
            OR file.consent_reset_version IS DISTINCT FROM (item->>'owner_reset_version')::bigint
            OR policy.approved_owner_reset_version IS DISTINCT FROM file.consent_reset_version
            OR policy.authorization_epoch IS DISTINCT FROM (item->>'authorization_epoch')::bigint
            OR file.authorization_epoch IS DISTINCT FROM policy.authorization_epoch
            OR policy.field_policy_version IS DISTINCT FROM (item->>'field_policy_version')::int
            OR file.field_policy_version IS DISTINCT FROM policy.field_policy_version
            OR policy.file_identity IS DISTINCT FROM item->>'file_identity'
            OR policy.source_revision_hash IS DISTINCT FROM file.selected_revision_hash
            OR policy.approved_fields_text<>'content' OR item->'approved_fields' IS DISTINCT FROM '["content"]'::jsonb THEN
            RAISE EXCEPTION 'Retrieval source authority changed' USING ERRCODE='check_violation';
        END IF;
    END LOOP;
    SELECT array_agg((m::jsonb->>'root_id')::bigint ORDER BY (m::jsonb->>'root_id')::bigint)
        INTO root_ids FROM jsonb_array_elements_text(p_scope->'authority_manifests') m;
    IF cardinality(root_ids)<>(SELECT count(DISTINCT x) FROM unnest(root_ids) x)
        OR cardinality(root_ids)<>(SELECT count(*) FROM jsonb_array_elements(p_scope->'sources') t WHERE t->>'role'='TARGET') THEN
        RAISE EXCEPTION 'Retrieval root proof mismatch' USING ERRCODE='check_violation';
    END IF;
    WITH RECURSIVE closure(id) AS (
        SELECT unnest(root_ids) UNION SELECT d.child_source_id FROM rag.dependencies d JOIN closure c ON d.parent_source_id=c.id
    ) SELECT array_agg(id ORDER BY id) INTO actual_ids FROM closure;
    IF ids IS DISTINCT FROM actual_ids THEN
        RAISE EXCEPTION 'Retrieval dependency closure changed' USING ERRCODE='check_violation';
    END IF;
    FOR manifest_text IN SELECT m FROM jsonb_array_elements_text(p_scope->'authority_manifests') m LOOP
        manifest:=manifest_text::jsonb;
        SELECT t INTO item FROM jsonb_array_elements(p_scope->'sources') t
            WHERE (t->>'source_id')::bigint=(manifest->>'root_id')::bigint AND t->>'role'='TARGET';
        IF NOT FOUND THEN RAISE EXCEPTION 'Retrieval root missing' USING ERRCODE='check_violation'; END IF;
        SELECT * INTO generation FROM rag.index_generations WHERE id=(item->>'generation_row_id')::bigint FOR SHARE;
        IF NOT FOUND OR generation.generation_id IS DISTINCT FROM (item->>'generation_id')::uuid
            OR generation.source_id IS DISTINCT FROM (item->>'source_id')::bigint
            OR NOT generation.is_active OR generation.status<>'PUBLISHED'
            OR generation.dependency_manifest_digest<>encode(sha256(convert_to(manifest_text,'UTF8')),'hex') THEN
            RAISE EXCEPTION 'Retrieval generation changed' USING ERRCODE='check_violation';
        END IF;
        IF NOT coalesce(p_scope->'generation_ids' @> to_jsonb(generation.generation_id::text),false)
            OR NOT coalesce(p_scope->'generation_row_ids' @> to_jsonb(generation.id),false) THEN
            RAISE EXCEPTION 'Retrieval generation protection scope missing' USING ERRCODE='check_violation';
        END IF;
        IF jsonb_typeof(manifest->'nodes') IS DISTINCT FROM 'array'
            OR jsonb_typeof(manifest->'edges') IS DISTINCT FROM 'array'
            OR NOT EXISTS(SELECT 1 FROM jsonb_array_elements(manifest->'nodes') n
                WHERE (n->>'source_id')::bigint=(manifest->>'root_id')::bigint) THEN
            RAISE EXCEPTION 'Retrieval canonical manifest missing' USING ERRCODE='check_violation';
        END IF;
        SELECT array_agg((n->>'source_id')::bigint ORDER BY (n->>'source_id')::bigint) INTO manifest_ids
            FROM jsonb_array_elements(manifest->'nodes') n;
        WITH RECURSIVE closure(id) AS (
            SELECT (manifest->>'root_id')::bigint UNION
            SELECT d.child_source_id FROM rag.dependencies d JOIN closure c ON d.parent_source_id=c.id
        ) SELECT array_agg(id ORDER BY id) INTO root_closure FROM closure;
        IF manifest_ids IS DISTINCT FROM root_closure THEN
            RAISE EXCEPTION 'Retrieval root manifest closure changed' USING ERRCODE='check_violation';
        END IF;
        SELECT * INTO root FROM rag.generation_roots WHERE generation_id=generation.id FOR SHARE;
        IF NOT FOUND OR root.exact_dependency_manifest->>'manifest_text' IS DISTINCT FROM manifest_text
            OR root.model_artifact_hash<>'98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f'
            OR root.model_version<>'2.0.0' OR root.tokenizer_version<>'hf-tokenizers-0.23.3'
            OR root.parser_version<>'ppv-parser-v18.6-d17-html-container-boundaries'
            OR root.chunker_version<>'structure-first-v18.5-compact-source-context'
            OR root.ocr_pipeline_version<>'rapidocr-1.4.4-en-hans-script-admission-v18.2'
            OR root.vector_dimensions<>384 THEN
            RAISE EXCEPTION 'Retrieval pipeline/manifest changed' USING ERRCODE='check_violation';
        END IF;
        SELECT * INTO lease FROM rag.job_leases WHERE job_id=generation.job_id ORDER BY id LIMIT 1 FOR SHARE;
        IF NOT FOUND OR lease.status<>'COMPLETED' OR NOT lease.staging_closed
            OR NOT (p_scope->'producer_manifest_digests' @> to_jsonb(encode(sha256(convert_to(
                coalesce(root.exact_dependency_manifest->>'input_manifest_text',lease.dependency_manifest),'UTF8')),'hex'))) THEN
            RAISE EXCEPTION 'Retrieval producer proof changed' USING ERRCODE='check_violation';
        END IF;
        FOR node IN SELECT n FROM jsonb_array_elements(manifest->'nodes') n LOOP
            SELECT * INTO policy FROM rag.sources WHERE id=(node->>'source_id')::bigint;
            SELECT * INTO file FROM rag.external_file_sources WHERE file_source_id::text=policy.source_id;
            IF policy.id IS NULL OR file.id IS NULL OR node->>'source_type' IS DISTINCT FROM 'EXTERNAL_FILE'
                OR node->>'domain_id' IS DISTINCT FROM policy.source_id
                OR (node->>'owner_source_version')::bigint IS DISTINCT FROM file.source_version
                OR (node->>'owner_reset_version')::bigint IS DISTINCT FROM file.consent_reset_version
                OR (node->>'epoch')::bigint IS DISTINCT FROM policy.authorization_epoch
                OR (node->>'field_version')::int IS DISTINCT FROM policy.field_policy_version
                OR node->'fields' IS DISTINCT FROM '["content"]'::jsonb
                OR node->'snapshot'->>'revision_id' IS DISTINCT FROM file.selected_revision_id::text
                OR node->'snapshot'->>'sha256' IS DISTINCT FROM file.selected_revision_hash
                OR (node->'snapshot'->>'byte_length')::bigint IS DISTINCT FROM file.selected_byte_length
                OR NOT policy.id=ANY(ids) THEN
                RAISE EXCEPTION 'Retrieval manifest authority changed' USING ERRCODE='check_violation';
            END IF;
        END LOOP;
        IF EXISTS(
            (SELECT d.parent_source_id,d.child_source_id FROM rag.dependencies d
                WHERE d.parent_source_id IN (SELECT (n->>'source_id')::bigint FROM jsonb_array_elements(manifest->'nodes') n)
             EXCEPT SELECT (e->>0)::bigint,(e->>1)::bigint FROM jsonb_array_elements(manifest->'edges') e)
            UNION ALL
            (SELECT (e->>0)::bigint,(e->>1)::bigint FROM jsonb_array_elements(manifest->'edges') e
             EXCEPT SELECT d.parent_source_id,d.child_source_id FROM rag.dependencies d
                WHERE d.parent_source_id IN (SELECT (n->>'source_id')::bigint FROM jsonb_array_elements(manifest->'nodes') n))
        ) THEN RAISE EXCEPTION 'Retrieval manifest topology changed' USING ERRCODE='check_violation'; END IF;
    END LOOP;
END $$;
REVOKE ALL ON FUNCTION rag.retrieval_scope_require_current(JSONB,UUID[],VARCHAR) FROM PUBLIC,rag_worker,rag_runtime;

-- 1. Admission CAS
CREATE OR REPLACE FUNCTION rag.retrieval_reader_admit(
    p_reader_id UUID,
    p_reservation_token UUID,
    p_scope_digest VARCHAR(64),
    p_scope_payload JSONB,
    p_external_file_ids UUID[],
    p_controller_instance VARCHAR(255)
) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE
    sorted_ext_ids UUID[];
BEGIN
    IF p_reader_id IS NULL OR p_reservation_token IS NULL OR p_scope_digest IS NULL
        OR p_scope_payload IS NULL OR p_external_file_ids IS NULL
        OR cardinality(p_external_file_ids) NOT BETWEEN 1 AND 100
        OR array_position(p_external_file_ids, NULL) IS NOT NULL
        OR p_controller_instance IS NULL OR p_controller_instance = '' THEN
        RAISE EXCEPTION 'Retrieval reader admission parameter denied' USING ERRCODE='check_violation';
    END IF;

    PERFORM rag.retrieval_scope_require_current(p_scope_payload,p_external_file_ids,p_controller_instance);
    -- Canonical sorted lock on external file sources to serialize with inspections and mutations
    SELECT array_agg(x ORDER BY x) INTO sorted_ext_ids FROM unnest(p_external_file_ids) AS x;
    PERFORM 1 FROM rag.external_file_sources
    WHERE file_source_id = ANY(sorted_ext_ids)
    ORDER BY file_source_id FOR UPDATE;

    IF EXISTS (SELECT 1 FROM rag.retrieval_readers WHERE state <> 'TERMINAL') THEN
        RAISE EXCEPTION 'RAG_READER_BUSY' USING ERRCODE='P1701';
    END IF;

    IF EXISTS (
        SELECT 1 FROM rag.retrieval_inspections
        WHERE (state IN ('UNRESOLVED', 'INCIDENT_PENDING', 'DENIED') OR deny_new_admission)
          AND affected_file_source_id = ANY(sorted_ext_ids)
          AND rag.retrieval_inspection_in_scope(p_scope_payload,affected_file_source_id,affected_revision_id)
    ) THEN
        RAISE EXCEPTION 'RAG_SOURCE_UNVERIFIED' USING ERRCODE='check_violation';
    END IF;

    PERFORM rag.external_file_reserve(p_external_file_ids, p_reservation_token);

    INSERT INTO rag.retrieval_readers (
        reader_id, reservation_token, scope_digest, scope_payload, state,
        stop_requested, emission_started, emission_disposition, controller_instance,
        created_at, updated_at
    ) VALUES (
        p_reader_id, p_reservation_token, encode(sha256(convert_to(p_scope_payload::text,'UTF8')),'hex'), p_scope_payload, 'ADMITTED',
        false, false, 'PENDING', p_controller_instance,
        clock_timestamp(), clock_timestamp()
    );
END $$;

-- 2. Inspection preregistration
CREATE OR REPLACE FUNCTION rag.retrieval_reader_admit_empty(
    p_reader UUID,p_token UUID,p_controller VARCHAR,p_pid BIGINT,p_creation BIGINT
) RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE scope JSONB;
BEGIN
    PERFORM pg_advisory_xact_lock(5927097059089141831);
    IF NOT EXISTS(SELECT 1 FROM rag.runtime_state WHERE current_instance::text=p_controller)
        OR p_pid IS NULL OR p_pid<=0 OR p_creation IS NULL OR p_creation<=0 THEN
        RAISE EXCEPTION 'Empty reader identity denied' USING ERRCODE='check_violation';
    END IF;
    IF EXISTS(SELECT 1 FROM rag.retrieval_readers WHERE state<>'TERMINAL') THEN
        RAISE EXCEPTION 'RAG_READER_BUSY' USING ERRCODE='P1701';
    END IF;
    scope:=jsonb_build_object('empty_scope',true,'sources','[]'::jsonb,'external_file_ids','[]'::jsonb,
        'generation_ids','[]'::jsonb,'generation_row_ids','[]'::jsonb,'controller_pid',p_pid,
        'controller_creation_file_time',p_creation::text);
    INSERT INTO rag.retrieval_readers(reader_id,reservation_token,scope_digest,scope_payload,state,controller_instance)
        VALUES(p_reader,p_token,encode(sha256(convert_to(scope::text,'UTF8')),'hex'),scope,'ADMITTED',p_controller);
END $$;
REVOKE ALL ON FUNCTION rag.retrieval_reader_admit_empty(UUID,UUID,VARCHAR,BIGINT,BIGINT) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION rag.retrieval_reader_admit_empty(UUID,UUID,VARCHAR,BIGINT,BIGINT) TO rag_runtime;

CREATE OR REPLACE FUNCTION rag.retrieval_reader_bind_worker(
    p_reader UUID,p_controller VARCHAR,p_pid BIGINT,p_created TIMESTAMPTZ,p_file_time BIGINT
) RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE r rag.retrieval_readers%ROWTYPE;
BEGIN
    SELECT * INTO r FROM rag.retrieval_readers WHERE reader_id=p_reader FOR UPDATE;
    IF NOT FOUND OR r.state NOT IN ('ADMITTED','CONSUMING') OR r.stop_requested
        OR r.worker_pid IS NOT NULL OR r.controller_instance<>p_controller
        OR p_pid<=0 OR p_created IS NULL OR p_file_time<=0
        OR NOT EXISTS(SELECT 1 FROM rag.runtime_state WHERE current_instance::text=p_controller) THEN
        RAISE EXCEPTION 'Query native binding denied' USING ERRCODE='check_violation';
    END IF;
    UPDATE rag.retrieval_readers SET worker_pid=p_pid,worker_created_at=p_created,
        worker_creation_file_time=p_file_time,state='CONSUMING',updated_at=clock_timestamp() WHERE reader_id=p_reader;
END $$;
REVOKE ALL ON FUNCTION rag.retrieval_reader_bind_worker(UUID,VARCHAR,BIGINT,TIMESTAMPTZ,BIGINT) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION rag.retrieval_reader_bind_worker(UUID,VARCHAR,BIGINT,TIMESTAMPTZ,BIGINT) TO rag_runtime;

CREATE OR REPLACE FUNCTION rag.retrieval_inspection_begin(
    p_inspection_id UUID,
    p_reader_id UUID,
    p_producer VARCHAR(64),
    p_scope_digest VARCHAR(64),
    p_file_source_id UUID,
    p_revision_id UUID, p_controller_pid BIGINT, p_controller_creation BIGINT, p_controller_instance VARCHAR
) RETURNS UUID
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE
    r_state VARCHAR(32);
    r_scope JSONB;
    existing_id UUID;
    proof JSONB;
BEGIN
    PERFORM pg_advisory_xact_lock(5927097059089141831);
    IF p_controller_instance IS NOT NULL AND NOT EXISTS(SELECT 1 FROM rag.runtime_state WHERE current_instance::text=p_controller_instance) THEN
        RAISE EXCEPTION 'Inspection controller runtime changed' USING ERRCODE='check_violation';
    END IF;
    PERFORM pg_advisory_xact_lock(5927097059089141832);
    IF p_inspection_id IS NULL OR p_producer IS NULL OR p_scope_digest IS NULL
        OR p_file_source_id IS NULL THEN
        RAISE EXCEPTION 'Inspection begin parameters denied' USING ERRCODE='check_violation';
    END IF;

    -- Ordered lock on affected external file source to coordinate with emission/admission
    PERFORM 1 FROM rag.external_file_sources WHERE file_source_id = p_file_source_id FOR UPDATE;

    IF EXISTS (
        SELECT 1 FROM rag.retrieval_inspections
        WHERE inspection_id = p_inspection_id
    ) THEN
        IF NOT EXISTS(SELECT 1 FROM rag.retrieval_inspections WHERE inspection_id=p_inspection_id
            AND producer=p_producer AND scope_digest=p_scope_digest
            AND affected_file_source_id=p_file_source_id AND affected_revision_id IS NOT DISTINCT FROM p_revision_id
            AND reader_id IS NOT DISTINCT FROM p_reader_id) THEN
            RAISE EXCEPTION 'Inspection identity reuse denied' USING ERRCODE='check_violation';
        END IF;
        RETURN p_inspection_id;
    END IF;
    SELECT inspection_id INTO existing_id FROM rag.retrieval_inspections
        WHERE producer=p_producer AND scope_digest=p_scope_digest
            AND affected_file_source_id=p_file_source_id AND affected_revision_id IS NOT DISTINCT FROM p_revision_id
            AND reader_id IS NOT DISTINCT FROM p_reader_id AND state='UNRESOLVED'
        ORDER BY created_at,inspection_id LIMIT 1;
    IF FOUND THEN RETURN existing_id; END IF;
    IF (SELECT count(*) FROM rag.retrieval_inspections)>=4096 THEN
        DELETE FROM rag.retrieval_inspections WHERE inspection_id IN (
            SELECT i.inspection_id FROM rag.retrieval_inspections i
            WHERE i.state='CLEAN' AND NOT i.deny_new_admission
              AND (i.reader_id IS NULL OR EXISTS(SELECT 1 FROM rag.retrieval_readers r WHERE r.reader_id=i.reader_id AND r.state='TERMINAL'))
            ORDER BY i.created_at,i.inspection_id LIMIT 256);
        IF (SELECT count(*) FROM rag.retrieval_inspections)>=4096 THEN
            RAISE EXCEPTION 'Inspection capacity unavailable; obligations retained' USING ERRCODE='check_violation';
        END IF;
    END IF;

    IF p_reader_id IS NOT NULL THEN
        SELECT state,scope_payload INTO r_state,r_scope FROM rag.retrieval_readers WHERE reader_id = p_reader_id FOR UPDATE;
        IF NOT FOUND OR r_state='TERMINAL' OR NOT rag.retrieval_inspection_in_scope(r_scope,p_file_source_id,p_revision_id) THEN
            RAISE EXCEPTION 'Inspection reader tuple mismatch' USING ERRCODE='check_violation';
        END IF;
        IF r_state IN ('ADMITTED', 'CONSUMING') THEN
            UPDATE rag.retrieval_readers
            SET stop_requested = true,
                state = 'STOP_REQUESTED',
                updated_at = clock_timestamp()
            WHERE reader_id = p_reader_id;
        END IF;
    END IF;

    SELECT jsonb_build_object('sha256',CASE WHEN selected_revision_id=p_revision_id THEN selected_revision_hash
            WHEN pending_revision_id=p_revision_id THEN pending_revision_hash END,
        'byte_length',CASE WHEN selected_revision_id=p_revision_id THEN selected_byte_length
            WHEN pending_revision_id=p_revision_id THEN pending_byte_length END,
        'controller_instance',coalesce(p_controller_instance,(SELECT current_instance::text FROM rag.runtime_state WHERE singleton)),
        'controller_pid',p_controller_pid,'controller_creation_file_time',p_controller_creation::text)
        INTO proof FROM rag.external_file_sources WHERE file_source_id=p_file_source_id;
    INSERT INTO rag.retrieval_inspections (
        inspection_id, reader_id, producer, scope_digest, affected_file_source_id,
        affected_revision_id, state, deny_new_admission, inspection_proof, created_at, updated_at
    ) VALUES (
        p_inspection_id, p_reader_id, p_producer, p_scope_digest, p_file_source_id,
        p_revision_id, 'UNRESOLVED', true, coalesce(proof,'{}'::jsonb), clock_timestamp(), clock_timestamp()
    );
    RETURN p_inspection_id;
END $$;

CREATE OR REPLACE FUNCTION rag.retrieval_inspection_begin(UUID,UUID,VARCHAR,VARCHAR,UUID,UUID)
RETURNS UUID LANGUAGE sql SECURITY DEFINER SET search_path=pg_catalog AS $$
    SELECT rag.retrieval_inspection_begin($1,$2,$3,$4,$5,$6,NULL,NULL,NULL)
$$;
REVOKE ALL ON FUNCTION rag.retrieval_inspection_begin(UUID,UUID,VARCHAR,VARCHAR,UUID,UUID,BIGINT,BIGINT,VARCHAR) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION rag.retrieval_inspection_begin(UUID,UUID,VARCHAR,VARCHAR,UUID,UUID,BIGINT,BIGINT,VARCHAR) TO rag_runtime;

-- A query can inspect its own admitted immutable tuple without inventing cancel intent.
-- The preregistered UNRESOLVED row still fences C_E until exact clean classification.
CREATE OR REPLACE FUNCTION rag.retrieval_reader_inspection_begin(
    p_inspection UUID,p_reader UUID,p_token UUID,p_file UUID,p_revision UUID
) RETURNS UUID LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE r rag.retrieval_readers%ROWTYPE; actual UUID;
BEGIN
    PERFORM pg_advisory_xact_lock(5927097059089141831);
    PERFORM 1 FROM rag.external_file_sources WHERE file_source_id=p_file FOR UPDATE;
    SELECT * INTO r FROM rag.retrieval_readers WHERE reader_id=p_reader FOR UPDATE;
    IF NOT FOUND OR r.reservation_token IS DISTINCT FROM p_token OR r.stop_requested OR r.state NOT IN ('ADMITTED','CONSUMING')
        OR NOT rag.retrieval_inspection_in_scope(r.scope_payload,p_file,p_revision)
        OR NOT EXISTS(SELECT 1 FROM rag.runtime_state WHERE current_instance::text=r.controller_instance) THEN
        RAISE EXCEPTION 'Reader inspection stopped or authority changed' USING ERRCODE='check_violation';
    END IF;
    IF p_inspection IS NULL THEN
        RAISE EXCEPTION 'Reader inspection identity missing' USING ERRCODE='check_violation';
    END IF;
    SELECT inspection_id INTO actual FROM rag.retrieval_inspections
        WHERE reader_id=p_reader AND producer='QUERY_EVIDENCE' AND scope_digest=r.scope_digest
            AND affected_file_source_id=p_file AND affected_revision_id IS NOT DISTINCT FROM p_revision
            AND state='UNRESOLVED' ORDER BY created_at,inspection_id LIMIT 1;
    IF FOUND THEN RETURN actual; END IF;
    actual:=rag.retrieval_inspection_begin(p_inspection,NULL,'QUERY_EVIDENCE',r.scope_digest,p_file,p_revision,
        (r.scope_payload->>'controller_pid')::bigint,(r.scope_payload->>'controller_creation_file_time')::bigint,r.controller_instance);
    UPDATE rag.retrieval_inspections SET reader_id=p_reader WHERE inspection_id=actual AND (reader_id IS NULL OR reader_id=p_reader);
    IF NOT FOUND THEN RAISE EXCEPTION 'Reader inspection binding mismatch' USING ERRCODE='check_violation'; END IF;
    RETURN actual;
END $$;
REVOKE ALL ON FUNCTION rag.retrieval_reader_inspection_begin(UUID,UUID,UUID,UUID,UUID) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION rag.retrieval_reader_inspection_begin(UUID,UUID,UUID,UUID,UUID) TO rag_runtime;

-- 3. Inspection classification recording
CREATE OR REPLACE FUNCTION rag.retrieval_inspection_record(
    p_inspection_id UUID,
    p_state VARCHAR(32),
    p_deny_new_admission BOOLEAN,
    p_details JSONB
) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE
    r_id UUID;
    f_id UUID;
    curr_state VARCHAR(32);
    r_state VARCHAR(32);
    revision_id UUID;
BEGIN
    IF p_inspection_id IS NULL OR p_state NOT IN ('CLEAN', 'INCIDENT_PENDING', 'DENIED') THEN
        RAISE EXCEPTION 'Inspection record parameters denied' USING ERRCODE='check_violation';
    END IF;

    -- Snapshot read of inspection target without locking
    SELECT affected_file_source_id, reader_id, state,affected_revision_id INTO f_id, r_id, curr_state,revision_id
    FROM rag.retrieval_inspections WHERE inspection_id = p_inspection_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Inspection not found' USING ERRCODE='check_violation';
    END IF;

    -- Enforce monotonic state progression: cannot demote from DENIED or INCIDENT_PENDING to CLEAN
    IF curr_state = 'DENIED' THEN
        IF p_state <> 'DENIED' THEN
            RAISE EXCEPTION 'Cannot demote denied inspection' USING ERRCODE='check_violation';
        END IF;
    ELSIF curr_state = 'INCIDENT_PENDING' THEN
        IF p_state = 'CLEAN' THEN
            RAISE EXCEPTION 'Cannot demote incident inspection to clean' USING ERRCODE='check_violation';
        END IF;
    END IF;

    -- 1. Lock external file source FIRST in canonical hierarchy
    IF f_id IS NOT NULL THEN
        PERFORM 1 FROM rag.external_file_sources WHERE file_source_id = f_id FOR UPDATE;
    END IF;

    -- 2. Lock reader SECOND in canonical hierarchy
    IF r_id IS NOT NULL THEN
        SELECT state INTO r_state FROM rag.retrieval_readers WHERE reader_id = r_id FOR UPDATE;
        IF r_state IN ('ADMITTED', 'CONSUMING') AND p_state IN ('INCIDENT_PENDING', 'DENIED') THEN
            UPDATE rag.retrieval_readers
            SET stop_requested = true,
                state = 'STOP_REQUESTED',
                updated_at = clock_timestamp()
            WHERE reader_id = r_id;
        END IF;
    ELSIF f_id IS NOT NULL AND p_state IN ('INCIDENT_PENDING', 'DENIED') THEN
        UPDATE rag.retrieval_readers
        SET stop_requested = true,
            state = CASE WHEN state IN ('ADMITTED', 'CONSUMING') THEN 'STOP_REQUESTED' ELSE state END,
            updated_at = clock_timestamp()
        WHERE state <> 'TERMINAL'
          AND rag.retrieval_inspection_in_scope(scope_payload,f_id,revision_id);
    END IF;

    -- 3. Lock inspection THIRD in canonical hierarchy and update
    UPDATE rag.retrieval_inspections
    SET state = p_state,
        deny_new_admission = p_deny_new_admission,
        details = p_details,
        updated_at = clock_timestamp()
    WHERE inspection_id = p_inspection_id;
END $$;

-- 4. Stop request CAS
CREATE OR REPLACE FUNCTION rag.retrieval_revoke_inspected_generations(p_file UUID,p_revision UUID)
RETURNS void LANGUAGE sql SECURITY DEFINER SET search_path=pg_catalog AS $$
    UPDATE rag.index_generations g SET is_active=false,status='REVOKED',revoked_at=clock_timestamp()
    WHERE g.is_active AND EXISTS (
        SELECT 1 FROM rag.job_leases l LEFT JOIN rag.generation_roots gr ON gr.generation_id=g.id
        CROSS JOIN LATERAL jsonb_array_elements(
            CASE WHEN pg_input_is_valid(coalesce(gr.exact_dependency_manifest->>'manifest_text',l.dependency_manifest),'jsonb')
                THEN coalesce(gr.exact_dependency_manifest->>'manifest_text',l.dependency_manifest)::jsonb->'nodes'
                ELSE '[]'::jsonb END) n
        WHERE l.job_id=g.job_id AND n->>'source_type'='EXTERNAL_FILE'
            AND n->>'domain_id'=p_file::text AND n->'snapshot'->>'revision_id'=p_revision::text)
$$;
REVOKE ALL ON FUNCTION rag.retrieval_revoke_inspected_generations(UUID,UUID) FROM PUBLIC,rag_worker,rag_runtime;

CREATE OR REPLACE FUNCTION rag.retrieval_inspection_deny(p_id UUID)
RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE i rag.retrieval_inspections%ROWTYPE; f rag.external_file_sources%ROWTYPE; affected integer;
BEGIN
    PERFORM pg_advisory_xact_lock(5927097059089141831);
    SELECT * INTO i FROM rag.retrieval_inspections WHERE inspection_id=p_id;
    IF NOT FOUND OR i.state<>'INCIDENT_PENDING' OR i.affected_revision_id IS NULL THEN RETURN; END IF;
    SELECT * INTO f FROM rag.external_file_sources WHERE file_source_id=i.affected_file_source_id FOR UPDATE;
    IF NOT FOUND THEN RETURN; END IF;
    IF f.reserved_by IS NOT NULL AND i.affected_revision_id IN (f.selected_revision_id,f.pending_revision_id) THEN RETURN; END IF;
    IF i.affected_revision_id IN (f.selected_revision_id,f.pending_revision_id) THEN
        UPDATE rag.external_file_sources SET snapshot_integrity_failed=true,updated_at=clock_timestamp()
            WHERE file_source_id=f.file_source_id AND reserved_by IS NULL
                AND ((selected_revision_id=i.affected_revision_id AND selected_revision_hash=i.inspection_proof->>'sha256'
                        AND selected_byte_length=(i.inspection_proof->>'byte_length')::bigint)
                    OR (pending_revision_id=i.affected_revision_id AND pending_revision_hash=i.inspection_proof->>'sha256'
                        AND pending_byte_length=(i.inspection_proof->>'byte_length')::bigint));
        GET DIAGNOSTICS affected=ROW_COUNT;
        IF affected<>1 THEN RAISE EXCEPTION 'Inspection denial tuple changed' USING ERRCODE='check_violation'; END IF;
    END IF;
    PERFORM rag.retrieval_inspection_record(p_id,'DENIED',true,'{"reason":"EXACT_RECOVERED_INSPECTION"}'::jsonb);
    PERFORM rag.retrieval_revoke_inspected_generations(i.affected_file_source_id,i.affected_revision_id);
END $$;
REVOKE ALL ON FUNCTION rag.retrieval_inspection_deny(UUID) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION rag.retrieval_inspection_deny(UUID) TO rag_runtime;

CREATE OR REPLACE FUNCTION rag.retrieval_reader_request_stop(
    p_reader_id UUID,
    p_reason VARCHAR(64)
) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN
    UPDATE rag.retrieval_readers
    SET stop_requested = true,
        state = CASE WHEN state IN ('ADMITTED', 'CONSUMING') THEN 'STOP_REQUESTED' ELSE state END,
        updated_at = clock_timestamp()
    WHERE reader_id = p_reader_id AND state <> 'TERMINAL';
END $$;

-- 5. Begin emission CAS
CREATE OR REPLACE FUNCTION rag.retrieval_reader_begin_emission(
    p_reader_id UUID
) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE
    r rag.retrieval_readers%ROWTYPE;
    ext_ids uuid[];
BEGIN
    -- Snapshot read of reader without locking to discover external file sources
    SELECT * INTO r FROM rag.retrieval_readers WHERE reader_id = p_reader_id;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Retrieval reader not found' USING ERRCODE='check_violation';
    END IF;
    IF r.state = 'TERMINAL' OR r.stop_requested THEN
        RAISE EXCEPTION 'Retrieval reader stopped or terminal' USING ERRCODE='check_violation';
    END IF;
    IF r.state NOT IN ('ADMITTED', 'CONSUMING') THEN
        RAISE EXCEPTION 'Retrieval reader invalid state for emission' USING ERRCODE='check_violation';
    END IF;

    IF r.scope_payload->'empty_scope'='true'::jsonb AND r.scope_payload->'sources'='[]'::jsonb
        AND r.scope_payload->'external_file_ids'='[]'::jsonb AND r.scope_payload->'generation_ids'='[]'::jsonb
        AND r.scope_payload->'generation_row_ids'='[]'::jsonb THEN
        IF NOT EXISTS(SELECT 1 FROM rag.runtime_state WHERE current_instance::text=r.controller_instance) THEN
            RAISE EXCEPTION 'Empty reader runtime changed' USING ERRCODE='check_violation';
        END IF;
    ELSE
        PERFORM rag.retrieval_scope_require_current(r.scope_payload,
            ARRAY(SELECT x::uuid FROM jsonb_array_elements_text(r.scope_payload->'external_file_ids') x),r.controller_instance);
    END IF;
    IF r.scope_digest<>encode(sha256(convert_to(r.scope_payload::text,'UTF8')),'hex') THEN
        RAISE EXCEPTION 'Retrieval scope digest mismatch' USING ERRCODE='check_violation';
    END IF;

    SELECT array_agg(DISTINCT (x.val)::text::uuid ORDER BY (x.val)::text::uuid) INTO ext_ids
    FROM jsonb_array_elements_text(r.scope_payload->'external_file_ids') AS x(val);

    -- 1. Lock external file sources FIRST in canonical sorted order
    IF ext_ids IS NOT NULL AND cardinality(ext_ids) > 0 THEN
        PERFORM 1 FROM rag.external_file_sources
        WHERE file_source_id = ANY(ext_ids)
        ORDER BY file_source_id FOR UPDATE;
    END IF;

    -- 2. Lock retrieval_readers SECOND in canonical hierarchy
    SELECT * INTO r FROM rag.retrieval_readers WHERE reader_id = p_reader_id FOR UPDATE;
    IF r.state = 'TERMINAL' OR r.stop_requested THEN
        RAISE EXCEPTION 'Retrieval reader stopped during emission coordination' USING ERRCODE='check_violation';
    END IF;
    IF r.state NOT IN ('ADMITTED', 'CONSUMING') THEN
        RAISE EXCEPTION 'Retrieval reader invalid state for emission' USING ERRCODE='check_violation';
    END IF;

    -- Check for any inspections in UNRESOLVED, INCIDENT_PENDING, or DENIED, or deny_new_admission=true
    IF EXISTS (
        SELECT 1 FROM rag.retrieval_inspections
        WHERE (state IN ('UNRESOLVED', 'INCIDENT_PENDING', 'DENIED') OR deny_new_admission)
          AND rag.retrieval_inspection_in_scope(r.scope_payload,affected_file_source_id,affected_revision_id)
    ) THEN
        RAISE EXCEPTION 'Retrieval reader has unresolved inspection or incident' USING ERRCODE='check_violation';
    END IF;

    UPDATE rag.retrieval_readers
    SET state = 'EMITTING',
        emission_started = true,
        emission_disposition = 'EMITTING',
        updated_at = clock_timestamp()
    WHERE reader_id = p_reader_id;
END $$;

-- 6. Sole exact-scope finisher: C_D and C_RL are one physical transaction.
CREATE OR REPLACE FUNCTION rag.retrieval_reader_finish(
    p_reader_id UUID, p_reservation_token UUID,
    p_finish_disposition VARCHAR(32), p_emission_disposition VARCHAR(32),
    p_quiescence_evidence JSONB, p_incident_file_ids UUID[], p_incident_revisions UUID[]
) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE
    r rag.retrieval_readers%ROWTYPE;
    ext_ids uuid[];
    incident record;
    affected integer;
    has_incidents boolean := false;
BEGIN
    SELECT * INTO r FROM rag.retrieval_readers
    WHERE reader_id=p_reader_id AND reservation_token=p_reservation_token;
    IF NOT FOUND THEN
        RAISE EXCEPTION 'Retrieval reader authority denied' USING ERRCODE='check_violation';
    END IF;
    SELECT coalesce(array_agg(x::uuid ORDER BY x::uuid),ARRAY[]::uuid[]) INTO ext_ids
    FROM jsonb_array_elements_text(r.scope_payload->'external_file_ids') x;
    PERFORM 1 FROM rag.external_file_sources WHERE file_source_id=ANY(ext_ids)
    ORDER BY file_source_id FOR UPDATE;
    SELECT * INTO r FROM rag.retrieval_readers
    WHERE reader_id=p_reader_id AND reservation_token=p_reservation_token FOR UPDATE;
    IF r.state='TERMINAL' THEN RETURN; END IF;

    -- Unknown inspection classification retains exclusion for EVERY finish outcome.
    IF EXISTS(SELECT 1 FROM rag.retrieval_inspections i
        WHERE rag.retrieval_inspection_in_scope(r.scope_payload,i.affected_file_source_id,i.affected_revision_id)
        AND (i.state='UNRESOLVED' OR (i.state='INCIDENT_PENDING' AND i.affected_revision_id IS NULL))) THEN
        RAISE EXCEPTION 'Unresolved inspection retains reader' USING ERRCODE='check_violation';
    END IF;
    IF coalesce(cardinality(p_incident_file_ids),0)<>coalesce(cardinality(p_incident_revisions),0)
        OR array_position(p_incident_file_ids,NULL) IS NOT NULL
        OR array_position(p_incident_revisions,NULL) IS NOT NULL THEN
        RAISE EXCEPTION 'Exact incident tuples required' USING ERRCODE='check_violation';
    END IF;
    -- Do not accept independent file/revision sets or a caller-invented incident.
    FOR incident IN SELECT f.file_id,v.revision_id
        FROM unnest(p_incident_file_ids) WITH ORDINALITY f(file_id,n)
        JOIN unnest(p_incident_revisions) WITH ORDINALITY v(revision_id,n) USING(n)
    LOOP
        IF NOT EXISTS(SELECT 1 FROM jsonb_array_elements(r.scope_payload->'sources') t
            WHERE (t->>'file_source_id')::uuid=incident.file_id
            AND (t->>'revision_id')::uuid=incident.revision_id)
            OR NOT EXISTS(SELECT 1 FROM rag.retrieval_inspections i
                WHERE i.affected_file_source_id=incident.file_id
                AND i.affected_revision_id=incident.revision_id
                AND i.state IN ('INCIDENT_PENDING','DENIED')) THEN
            RAISE EXCEPTION 'Incident outside admitted observed tuple' USING ERRCODE='check_violation';
        END IF;
    END LOOP;
    IF (SELECT count(*) FROM rag.external_file_sources
        WHERE file_source_id=ANY(ext_ids) AND reserved_by=p_reservation_token)
        <>cardinality(ext_ids) THEN
        RAISE EXCEPTION 'Reader reservation changed' USING ERRCODE='check_violation';
    END IF;
    SELECT EXISTS(SELECT 1 FROM rag.retrieval_inspections i
        WHERE i.affected_file_source_id=ANY(ext_ids) AND i.state IN ('INCIDENT_PENDING','DENIED')
        AND EXISTS(SELECT 1 FROM jsonb_array_elements(r.scope_payload->'sources') t
            WHERE (t->>'file_source_id')::uuid=i.affected_file_source_id
            AND (t->>'revision_id')::uuid=i.affected_revision_id))
        INTO has_incidents;

    UPDATE rag.retrieval_readers SET state='TERMINAL',
        finish_disposition=CASE WHEN has_incidents THEN 'INCIDENT_DENIED' ELSE p_finish_disposition END,
        emission_disposition=p_emission_disposition, quiescence_evidence=p_quiescence_evidence,
        updated_at=clock_timestamp() WHERE reader_id=p_reader_id;
    -- The old reservation trigger requires release and policy mutation separately.
    -- Both occur under the same source locks/COMMIT; there is no admission gap.
    UPDATE rag.external_file_sources SET reserved_by=NULL,updated_at=clock_timestamp()
        WHERE file_source_id=ANY(ext_ids) AND reserved_by=p_reservation_token;
    GET DIAGNOSTICS affected=ROW_COUNT;
    IF affected<>cardinality(ext_ids) THEN
        RAISE EXCEPTION 'Reader release affected-row mismatch' USING ERRCODE='check_violation';
    END IF;
    FOR incident IN SELECT DISTINCT i.affected_file_source_id file_id,i.affected_revision_id revision_id
        FROM rag.retrieval_inspections i
        WHERE i.affected_file_source_id=ANY(ext_ids) AND i.state IN ('INCIDENT_PENDING','DENIED')
        AND EXISTS(SELECT 1 FROM jsonb_array_elements(r.scope_payload->'sources') t
            WHERE (t->>'file_source_id')::uuid=i.affected_file_source_id
            AND (t->>'revision_id')::uuid=i.affected_revision_id)
    LOOP
        UPDATE rag.external_file_sources SET snapshot_integrity_failed=true,updated_at=clock_timestamp()
            WHERE file_source_id=incident.file_id AND selected_revision_id=incident.revision_id
            AND reserved_by IS NULL;
        GET DIAGNOSTICS affected=ROW_COUNT;
        IF affected<>1 THEN
            RAISE EXCEPTION 'Incident denial affected-row mismatch' USING ERRCODE='check_violation';
        END IF;
        UPDATE rag.retrieval_inspections SET state='DENIED',deny_new_admission=true,updated_at=clock_timestamp()
            WHERE affected_file_source_id=incident.file_id AND affected_revision_id=incident.revision_id
            AND state='INCIDENT_PENDING';
    END LOOP;
    IF has_incidents THEN
        FOR incident IN SELECT DISTINCT i.affected_file_source_id file_id,i.affected_revision_id revision_id
            FROM rag.retrieval_inspections i WHERE i.state='DENIED'
            AND rag.retrieval_inspection_in_scope(r.scope_payload,i.affected_file_source_id,i.affected_revision_id)
        LOOP
            PERFORM rag.retrieval_revoke_inspected_generations(incident.file_id,incident.revision_id);
        END LOOP;
    END IF;
END $$;

-- 6b. A boot identifier, missing PID or elapsed time is never stop proof.
CREATE OR REPLACE FUNCTION rag.retrieval_reader_observe(p_reader UUID,p_token UUID)
RETURNS VARCHAR LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE r rag.retrieval_readers%ROWTYPE;
BEGIN
    PERFORM set_config('lock_timeout','2s',true);
    PERFORM set_config('statement_timeout','5s',true);
    PERFORM set_config('transaction_timeout','10s',true);
    PERFORM pg_advisory_xact_lock(5927097059089141831);
    SELECT * INTO r FROM rag.retrieval_readers WHERE reader_id=p_reader;
    IF NOT FOUND THEN RETURN NULL; END IF;
    IF r.reservation_token<>p_token THEN RETURN 'FOREIGN'; END IF;
    PERFORM 1 FROM rag.external_file_sources WHERE file_source_id IN
        (SELECT x::uuid FROM jsonb_array_elements_text(r.scope_payload->'external_file_ids') x)
        ORDER BY file_source_id FOR UPDATE;
    SELECT * INTO r FROM rag.retrieval_readers WHERE reader_id=p_reader AND reservation_token=p_token FOR UPDATE;
    IF NOT FOUND THEN RAISE EXCEPTION 'Reader read-back identity changed' USING ERRCODE='check_violation'; END IF;
    RETURN r.state;
END $$;
REVOKE ALL ON FUNCTION rag.retrieval_reader_observe(UUID,UUID) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION rag.retrieval_reader_observe(UUID,UUID) TO rag_runtime;

-- Unknown readers and inspections remain durable. Only their owned finisher can release.
CREATE OR REPLACE FUNCTION rag.retrieval_reader_recover_stale(p_current_instance VARCHAR(64)) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN
    IF EXISTS(SELECT 1 FROM rag.retrieval_readers WHERE state<>'TERMINAL') THEN
        RAISE EXCEPTION 'Reader recovery requires positive controller/sender stop proof'
            USING ERRCODE='check_violation';
    END IF;
END $$;

-- 7. Trigger to guard index generations, roots and chunks against mutation while reader is active
CREATE OR REPLACE FUNCTION rag.retrieval_generation_has_pending(p_id BIGINT)
RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog AS $$
    SELECT EXISTS(SELECT 1 FROM rag.index_generations g JOIN rag.job_leases l ON l.job_id=g.job_id
        LEFT JOIN rag.generation_roots gr ON gr.generation_id=g.id
        CROSS JOIN LATERAL jsonb_array_elements(
            CASE WHEN pg_input_is_valid(coalesce(gr.exact_dependency_manifest->>'manifest_text',l.dependency_manifest),'jsonb')
                THEN coalesce(gr.exact_dependency_manifest->>'manifest_text',l.dependency_manifest)::jsonb->'nodes'
                ELSE '[]'::jsonb END) n
        WHERE g.id=p_id AND n->>'source_type'='EXTERNAL_FILE'
            AND rag.retrieval_inspection_blocks((n->>'domain_id')::uuid,(n->'snapshot'->>'revision_id')::uuid,true))
$$;
REVOKE ALL ON FUNCTION rag.retrieval_generation_has_pending(BIGINT) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION rag.retrieval_generation_has_pending(BIGINT) TO rag_runtime;

CREATE OR REPLACE FUNCTION rag.guard_retrieval_protected_generations()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM rag.retrieval_readers r
        WHERE r.state <> 'TERMINAL'
          AND (
              r.scope_payload->'generation_ids' @> to_jsonb(OLD.generation_id::text)
              OR r.scope_payload->'generation_row_ids' @> to_jsonb(OLD.id)
          )
    ) THEN
        RAISE EXCEPTION 'Cannot mutate or purge index generation while active retrieval reader is admitted' USING ERRCODE='check_violation';
    END IF;
    IF rag.retrieval_generation_has_pending(OLD.id) AND NOT (TG_OP='UPDATE' AND NOT NEW.is_active
        AND NEW.status IN ('REVOKED','OUTDATED')
        AND to_jsonb(NEW)-ARRAY['is_active','status','revoked_at']=to_jsonb(OLD)-ARRAY['is_active','status','revoked_at']) THEN
        RAISE EXCEPTION 'Pending inspection retains generation artifacts' USING ERRCODE='check_violation';
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END $$;

CREATE OR REPLACE FUNCTION rag.guard_retrieval_protected_roots()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM rag.retrieval_readers r
        WHERE r.state <> 'TERMINAL'
          AND (
              r.scope_payload->'generation_row_ids' @> to_jsonb(OLD.generation_id)
              OR EXISTS (
                  SELECT 1 FROM rag.index_generations g
                  WHERE g.id = OLD.generation_id
                    AND r.scope_payload->'generation_ids' @> to_jsonb(g.generation_id::text)
              )
          )
    ) THEN
        RAISE EXCEPTION 'Cannot mutate or purge generation roots while active retrieval reader is admitted' USING ERRCODE='check_violation';
    END IF;
    IF rag.retrieval_generation_has_pending(OLD.generation_id) THEN
        RAISE EXCEPTION 'Pending inspection retains generation artifacts' USING ERRCODE='check_violation';
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END $$;

CREATE OR REPLACE FUNCTION rag.guard_retrieval_protected_chunks()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM rag.retrieval_readers r
        WHERE r.state <> 'TERMINAL'
          AND (
              r.scope_payload->'generation_row_ids' @> to_jsonb(OLD.generation_id)
              OR EXISTS (
                  SELECT 1 FROM rag.index_generations g
                  WHERE g.id = OLD.generation_id
                    AND r.scope_payload->'generation_ids' @> to_jsonb(g.generation_id::text)
              )
          )
    ) THEN
        RAISE EXCEPTION 'Cannot mutate or purge document chunks while active retrieval reader is admitted' USING ERRCODE='check_violation';
    END IF;
    IF rag.retrieval_generation_has_pending(OLD.generation_id) THEN
        RAISE EXCEPTION 'Pending inspection retains generation artifacts' USING ERRCODE='check_violation';
    END IF;
    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;
    RETURN NEW;
END $$;

DROP TRIGGER IF EXISTS trg_guard_retrieval_chunks ON rag.document_chunks;
CREATE TRIGGER trg_guard_retrieval_chunks
    BEFORE DELETE OR UPDATE ON rag.document_chunks
    FOR EACH ROW EXECUTE FUNCTION rag.guard_retrieval_protected_chunks();

DROP TRIGGER IF EXISTS trg_guard_retrieval_roots ON rag.generation_roots;
CREATE TRIGGER trg_guard_retrieval_roots
    BEFORE DELETE OR UPDATE ON rag.generation_roots
    FOR EACH ROW EXECUTE FUNCTION rag.guard_retrieval_protected_roots();

DROP TRIGGER IF EXISTS trg_guard_retrieval_generations ON rag.index_generations;
CREATE TRIGGER trg_guard_retrieval_generations
    BEFORE DELETE OR UPDATE ON rag.index_generations
    FOR EACH ROW EXECUTE FUNCTION rag.guard_retrieval_protected_generations();

-- 8. Mutation trigger on external_file_sources to prevent direct uncoordinated release while reader is active
CREATE OR REPLACE FUNCTION rag.guard_external_file_sources_retrieval_reservation()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'UPDATE' THEN
        IF OLD.reserved_by IS NOT NULL AND NEW.reserved_by IS DISTINCT FROM OLD.reserved_by THEN
            IF EXISTS (
                SELECT 1 FROM rag.retrieval_readers
                WHERE reservation_token = OLD.reserved_by
                  AND state <> 'TERMINAL'
            ) THEN
                RAISE EXCEPTION 'Cannot release or change reservation held by active retrieval reader'
                    USING ERRCODE='check_violation';
            END IF;
        END IF;
    END IF;
    IF TG_OP = 'DELETE' THEN
        IF OLD.reserved_by IS NOT NULL AND EXISTS (
            SELECT 1 FROM rag.retrieval_readers
            WHERE reservation_token = OLD.reserved_by AND state <> 'TERMINAL'
        ) THEN
            RAISE EXCEPTION 'Cannot delete external file source held by active retrieval reader'
                USING ERRCODE='check_violation';
        END IF;
        RETURN OLD;
    END IF;
    RETURN NEW;
END $$;

DROP TRIGGER IF EXISTS trg_guard_external_file_sources_reservation ON rag.external_file_sources;
CREATE TRIGGER trg_guard_external_file_sources_reservation
    BEFORE UPDATE OR DELETE ON rag.external_file_sources
    FOR EACH ROW EXECUTE FUNCTION rag.guard_external_file_sources_retrieval_reservation();

-- 9. Mutation trigger on retrieval_readers to enforce immutability and require quiescence on terminalization
CREATE OR REPLACE FUNCTION rag.guard_retrieval_readers_mutation()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        RAISE EXCEPTION 'Retrieval readers are immutable and cannot be deleted' USING ERRCODE='check_violation';
    END IF;
    IF TG_OP = 'UPDATE' THEN
        IF NEW.reader_id IS DISTINCT FROM OLD.reader_id
            OR NEW.reservation_token IS DISTINCT FROM OLD.reservation_token
            OR NEW.scope_digest IS DISTINCT FROM OLD.scope_digest
            OR NEW.scope_payload IS DISTINCT FROM OLD.scope_payload
            OR NEW.controller_instance IS DISTINCT FROM OLD.controller_instance
            OR NEW.created_at IS DISTINCT FROM OLD.created_at THEN
            RAISE EXCEPTION 'Retrieval reader identity and scope are immutable' USING ERRCODE='check_violation';
        END IF;
        IF OLD.state = 'TERMINAL' THEN
            RAISE EXCEPTION 'Terminal retrieval reader cannot be updated' USING ERRCODE='check_violation';
        END IF;
        IF OLD.worker_pid IS NOT NULL AND (NEW.worker_pid IS DISTINCT FROM OLD.worker_pid
                OR NEW.worker_created_at IS DISTINCT FROM OLD.worker_created_at
                OR NEW.worker_creation_file_time IS DISTINCT FROM OLD.worker_creation_file_time) THEN
            RAISE EXCEPTION 'Bound query worker identity is immutable' USING ERRCODE='check_violation';
        END IF;
        IF NEW.state = 'TERMINAL' THEN
            IF NEW.quiescence_evidence IS NULL
                OR NEW.quiescence_evidence = '{}'::jsonb
                OR NEW.quiescence_evidence->'sender_quiescent' IS DISTINCT FROM 'true'::jsonb
                OR NEW.quiescence_evidence->'native_quiescent' IS DISTINCT FROM 'true'::jsonb
                OR NEW.quiescence_evidence->'transport_quiescent' IS DISTINCT FROM 'true'::jsonb
                OR NEW.quiescence_evidence->'buffers_discarded' IS DISTINCT FROM 'true'::jsonb
                OR NOT (coalesce(NEW.quiescence_evidence->'context_complete'='true'::jsonb,false)
                    OR coalesce(NEW.quiescence_evidence->'controller_stopped'='true'::jsonb,false)
                    OR coalesce(NEW.quiescence_evidence->'no_sender_started'='true'::jsonb,false))
                OR (NOT (NEW.quiescence_evidence ? 'startup_nanos')
                    AND NOT (NEW.quiescence_evidence ? 'reason')
                    AND NOT (NEW.quiescence_evidence ? 'at')
                    AND NOT (NEW.quiescence_evidence ? 'stopped_at')) THEN
                RAISE EXCEPTION 'Terminal retrieval reader requires verified non-empty quiescence evidence'
                    USING ERRCODE='check_violation';
            END IF;
            IF NEW.finish_disposition IS NULL
                OR NEW.finish_disposition NOT IN ('COMPLETED', 'CANCELLED', 'INCIDENT_DENIED', 'EXECUTION_FAILURE', 'RECOVERY_REQUIRED') THEN
                RAISE EXCEPTION 'Terminal retrieval reader requires valid finish disposition'
                    USING ERRCODE='check_violation';
            END IF;
            -- Prevent clean terminalization if unreconciled incidents or unresolved inspections exist on scope
            IF EXISTS (
                SELECT 1 FROM rag.retrieval_inspections
                WHERE rag.retrieval_inspection_in_scope(NEW.scope_payload,affected_file_source_id,affected_revision_id)
                AND (state='UNRESOLVED' OR (state='INCIDENT_PENDING' AND NEW.finish_disposition<>'INCIDENT_DENIED'))
            ) THEN
                RAISE EXCEPTION 'Cannot terminalize reader with unreconciled incidents'
                    USING ERRCODE='check_violation';
            END IF;
        END IF;
    END IF;
    RETURN NEW;
END $$;

DROP TRIGGER IF EXISTS trg_guard_retrieval_readers_mutation ON rag.retrieval_readers;
CREATE TRIGGER trg_guard_retrieval_readers_mutation
    BEFORE UPDATE OR DELETE ON rag.retrieval_readers
    FOR EACH ROW EXECUTE FUNCTION rag.guard_retrieval_readers_mutation();

-- 10. Mutation trigger on retrieval_inspections to enforce immutability and prevent deletion
CREATE OR REPLACE FUNCTION rag.guard_retrieval_inspections_mutation()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        IF current_user<>'rag_runtime' AND OLD.state='CLEAN' AND NOT OLD.deny_new_admission
            AND (OLD.reader_id IS NULL OR EXISTS(SELECT 1 FROM rag.retrieval_readers r WHERE r.reader_id=OLD.reader_id AND r.state='TERMINAL')) THEN
            RETURN OLD;
        END IF;
        RAISE EXCEPTION 'Retrieval inspections cannot be deleted' USING ERRCODE='check_violation';
    END IF;
    IF TG_OP = 'UPDATE' THEN
        IF NEW.inspection_id IS DISTINCT FROM OLD.inspection_id
            OR NEW.producer IS DISTINCT FROM OLD.producer
            OR NEW.scope_digest IS DISTINCT FROM OLD.scope_digest
            OR NEW.affected_file_source_id IS DISTINCT FROM OLD.affected_file_source_id
            OR NEW.affected_revision_id IS DISTINCT FROM OLD.affected_revision_id
            OR NEW.created_at IS DISTINCT FROM OLD.created_at THEN
            RAISE EXCEPTION 'Retrieval inspection identity and scope are immutable' USING ERRCODE='check_violation';
        END IF;
        IF NEW.inspection_proof IS DISTINCT FROM OLD.inspection_proof THEN
            RAISE EXCEPTION 'Inspection proof is immutable' USING ERRCODE='check_violation';
        END IF;
        IF OLD.state = 'DENIED' AND NEW.state <> 'DENIED' THEN
            RAISE EXCEPTION 'Inspection state DENIED is terminal and immutable' USING ERRCODE='check_violation';
        END IF;
        IF OLD.state = 'INCIDENT_PENDING' AND NEW.state = 'CLEAN' THEN
            RAISE EXCEPTION 'Inspection state INCIDENT_PENDING cannot be demoted to CLEAN' USING ERRCODE='check_violation';
        END IF;
        IF OLD.reader_id IS NOT NULL AND NEW.reader_id IS DISTINCT FROM OLD.reader_id THEN
            RAISE EXCEPTION 'Inspection reader association is immutable' USING ERRCODE='check_violation';
        END IF;
    END IF;
    RETURN NEW;
END $$;

DROP TRIGGER IF EXISTS trg_guard_retrieval_inspections_mutation ON rag.retrieval_inspections;
CREATE TRIGGER trg_guard_retrieval_inspections
    BEFORE UPDATE OR DELETE ON rag.retrieval_inspections
    FOR EACH ROW EXECUTE FUNCTION rag.guard_retrieval_inspections_mutation();

-- 11. Prevent ordinary release from bypassing retrieval reader
CREATE OR REPLACE FUNCTION rag.external_file_release(p_ids UUID[], p_token UUID) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN
    PERFORM rag.external_file_lock_reserved(p_ids, p_token);
    IF EXISTS(SELECT 1 FROM rag.retrieval_readers WHERE reservation_token = p_token AND state <> 'TERMINAL') THEN
        RAISE EXCEPTION 'Cannot release active retrieval reader reservation' USING ERRCODE='check_violation';
    END IF;
    UPDATE rag.external_file_sources SET reserved_by=NULL, updated_at=clock_timestamp()
        WHERE file_source_id=ANY(p_ids) AND reserved_by=p_token;
END $$;

-- 12. Replace guard_source_policy to also check external_file_require_editable for external files
-- The same durable predicate also fences ordinary indexing/promotion/release.
CREATE OR REPLACE FUNCTION rag.external_file_inspection_blocks(p_file UUID,p_pending_only BOOLEAN)
RETURNS boolean LANGUAGE sql STABLE SECURITY DEFINER SET search_path=pg_catalog AS $$
    SELECT EXISTS(SELECT 1 FROM rag.external_file_sources f JOIN rag.retrieval_inspections i
        ON i.affected_file_source_id=f.file_source_id
        WHERE f.file_source_id=p_file
          AND (i.affected_revision_id IS NULL OR i.affected_revision_id IN (f.selected_revision_id,f.pending_revision_id))
          AND (i.state IN ('UNRESOLVED','INCIDENT_PENDING')
            OR (NOT p_pending_only AND (i.state='DENIED' OR i.deny_new_admission))))
$$;
REVOKE ALL ON FUNCTION rag.external_file_inspection_blocks(UUID,BOOLEAN) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION rag.external_file_inspection_blocks(UUID,BOOLEAN) TO rag_runtime;

CREATE OR REPLACE FUNCTION rag.external_file_require_editable(p_ids UUID[]) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE item UUID; state rag.external_file_sources%ROWTYPE;
BEGIN
    IF p_ids IS NULL OR array_position(p_ids, NULL) IS NOT NULL THEN
        RAISE EXCEPTION 'External file authority unavailable' USING ERRCODE='check_violation';
    END IF;
    FOR item IN SELECT DISTINCT unnest(p_ids) ORDER BY 1 LOOP
        SELECT * INTO state FROM rag.external_file_sources WHERE file_source_id=item FOR UPDATE NOWAIT;
        IF NOT FOUND THEN
            RAISE EXCEPTION 'External file source authority unavailable' USING ERRCODE='check_violation';
        END IF;
        IF state.reserved_by IS NOT NULL THEN
            RAISE EXCEPTION 'RAG_SOURCE_BUSY' USING ERRCODE='P1701';
        END IF;
        IF rag.retrieval_inspection_blocks(item,coalesce(state.approved_pending_revision_id,state.selected_revision_id),true) THEN
            RAISE EXCEPTION 'External snapshot inspection unresolved or denied' USING ERRCODE='check_violation';
        END IF;
    END LOOP;
EXCEPTION WHEN lock_not_available THEN
    RAISE EXCEPTION 'RAG_SOURCE_BUSY' USING ERRCODE='P1701';
END $$;

CREATE OR REPLACE FUNCTION rag.external_file_reserve(p_ids UUID[], p_token UUID) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE item UUID; state rag.external_file_sources%ROWTYPE;
BEGIN
    IF p_token IS NULL OR p_ids IS NULL OR cardinality(p_ids) NOT BETWEEN 1 AND 100
        OR array_position(p_ids, NULL) IS NOT NULL
        OR cardinality(p_ids) <> (SELECT count(DISTINCT value) FROM unnest(p_ids) value)
        OR EXISTS(SELECT 1 FROM rag.external_file_sources WHERE reserved_by=p_token) THEN
        RAISE EXCEPTION 'External file reservation scope denied' USING ERRCODE='check_violation';
    END IF;
    FOR item IN SELECT unnest(p_ids) ORDER BY 1 LOOP
        SELECT * INTO state FROM rag.external_file_sources WHERE file_source_id=item FOR UPDATE;
        IF NOT FOUND OR NOT state.eligible THEN
            RAISE EXCEPTION 'External file source unavailable' USING ERRCODE='check_violation';
        END IF;
        IF rag.external_file_inspection_blocks(item,false) THEN
            RAISE EXCEPTION 'External snapshot inspection unresolved or denied' USING ERRCODE='check_violation';
        END IF;
        IF state.reserved_by IS NOT NULL THEN
            RAISE EXCEPTION 'RAG_SOURCE_BUSY' USING ERRCODE='P1701';
        END IF;
        UPDATE rag.external_file_sources SET reserved_by=p_token, updated_at=clock_timestamp() WHERE file_source_id=item;
    END LOOP;
EXCEPTION WHEN lock_not_available THEN
    RAISE EXCEPTION 'RAG_SOURCE_BUSY' USING ERRCODE='P1701';
END $$;

CREATE OR REPLACE FUNCTION rag.external_file_lock_reserved(p_ids UUID[], p_token UUID)
RETURNS SETOF rag.external_file_sources
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE item UUID; state rag.external_file_sources%ROWTYPE;
BEGIN
    IF p_token IS NULL OR p_ids IS NULL OR cardinality(p_ids) NOT BETWEEN 1 AND 100
        OR array_position(p_ids, NULL) IS NOT NULL
        OR cardinality(p_ids) <> (SELECT count(DISTINCT value) FROM unnest(p_ids) value) THEN
        RAISE EXCEPTION 'External file reservation scope denied' USING ERRCODE='check_violation';
    END IF;
    FOR item IN SELECT unnest(p_ids) ORDER BY 1 LOOP
        SELECT * INTO state FROM rag.external_file_sources WHERE file_source_id=item FOR UPDATE;
        IF NOT FOUND OR state.reserved_by IS DISTINCT FROM p_token OR NOT state.eligible THEN
            RAISE EXCEPTION 'External file reservation authority denied' USING ERRCODE='check_violation';
        END IF;
        IF rag.external_file_inspection_blocks(item,false) THEN
            RAISE EXCEPTION 'External snapshot inspection unresolved or denied' USING ERRCODE='check_violation';
        END IF;
        RETURN NEXT state;
    END LOOP;
    IF (SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=p_token) <> cardinality(p_ids) THEN
        RAISE EXCEPTION 'Incomplete external file reservation scope denied' USING ERRCODE='check_violation';
    END IF;
EXCEPTION WHEN lock_not_available THEN
    RAISE EXCEPTION 'RAG_SOURCE_BUSY' USING ERRCODE='P1701';
END $$;

CREATE OR REPLACE FUNCTION rag.guard_source_policy() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE ids bigint[];
BEGIN
    IF TG_OP='DELETE' THEN
        IF OLD.source_type IN ('FICTION','FILM') THEN
            PERFORM public.vault_rag_require_editable(ARRAY[OLD.source_id::bigint]);
        END IF;
        IF OLD.source_type = 'EXTERNAL_FILE' AND pg_input_is_valid(OLD.source_id,'uuid') THEN
            PERFORM rag.external_file_require_editable(ARRAY[OLD.source_id::uuid]);
        END IF;
        RAISE EXCEPTION 'Source consent history must be retained' USING ERRCODE='check_violation';
    END IF;
    IF TG_OP='UPDATE' AND (NEW.id IS DISTINCT FROM OLD.id OR NEW.source_type IS DISTINCT FROM OLD.source_type OR
        (NEW.source_id IS DISTINCT FROM OLD.source_id AND NOT (OLD.source_type='EXTERNAL_FILE'
            AND NEW.source_id='disabled:'||OLD.id::text AND NEW.access_state IN ('DENIED','REVOKED')
            AND NEW.file_identity IS NULL AND NEW.source_revision_hash='DISABLED'
            AND NEW.description_revision_hash='DISABLED' AND NEW.approved_fields_text=''))) THEN
        RAISE EXCEPTION 'RAG source identity is immutable' USING ERRCODE='check_violation'; END IF;
    IF NEW.source_type IN ('FICTION','FILM') THEN
        ids:=ARRAY[NEW.source_id::bigint];
        PERFORM public.vault_rag_require_editable(ids);
        IF NOT EXISTS(SELECT 1 FROM public.vault_rag_source_state WHERE source_id=NEW.source_id::bigint
            AND source_type=NEW.source_type AND NOT deleted) THEN
            RAISE EXCEPTION 'Owner source authority denied' USING ERRCODE='check_violation'; END IF;
    END IF;
    IF NEW.source_type = 'EXTERNAL_FILE' AND pg_input_is_valid(NEW.source_id,'uuid') THEN
        PERFORM rag.external_file_require_editable(ARRAY[NEW.source_id::uuid]);
    END IF;
    IF TG_OP='UPDATE' THEN
        IF NEW.classification IS DISTINCT FROM OLD.classification THEN NEW.access_state:='REVOKED'; END IF;
        IF NEW.classification IS DISTINCT FROM OLD.classification OR NEW.access_state IS DISTINCT FROM OLD.access_state
            OR NEW.approved_fields_text IS DISTINCT FROM OLD.approved_fields_text THEN
            NEW.authorization_epoch:=greatest(NEW.authorization_epoch,OLD.authorization_epoch+1);
        END IF;
        IF NEW.access_state<>'APPROVED' THEN NEW.approved_owner_reset_version:=NULL; END IF;
        IF NEW.approved_fields_text IS DISTINCT FROM OLD.approved_fields_text THEN
            NEW.field_policy_version:=greatest(NEW.field_policy_version,OLD.field_policy_version+1);
        END IF;
        IF NEW.authorization_epoch<OLD.authorization_epoch OR NEW.field_policy_version<OLD.field_policy_version THEN
            RAISE EXCEPTION 'Policy history cannot move backwards' USING ERRCODE='check_violation'; END IF;
    END IF;
    IF NEW.classification<>'NON_PRIVATE' AND NEW.access_state='APPROVED' THEN
        RAISE EXCEPTION 'Private or unknown approval denied' USING ERRCODE='check_violation'; END IF;
    IF NEW.access_state='APPROVED' AND NEW.source_type IN ('FICTION','FILM') AND NOT EXISTS(
        SELECT 1 FROM public.vault_rag_source_state WHERE source_id=NEW.source_id::bigint AND NOT deleted
            AND consent_reset_version=NEW.approved_owner_reset_version) THEN
        RAISE EXCEPTION 'Unbound owner consent denied' USING ERRCODE='check_violation'; END IF;
    RETURN NEW;
END $$;

REVOKE ALL ON FUNCTION rag.guard_source_policy() FROM PUBLIC,rag_worker,rag_runtime;

-- 13. Privileges
CREATE OR REPLACE FUNCTION rag.guard_external_pending_inspections()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF rag.external_file_inspection_blocks(OLD.file_source_id,true) THEN
        -- Preserve frozen unapproved-pending replacement: it grants/promotes nothing,
        -- and the old operation/revision remains retained until classification.
        IF TG_OP='UPDATE' AND OLD.selected_revision_id IS NULL AND NEW.selected_revision_id IS NULL
            AND OLD.approved_pending_revision_id IS NULL AND NEW.approved_pending_revision_id IS NULL
            AND NEW.pending_revision_id IS DISTINCT FROM OLD.pending_revision_id
            AND to_jsonb(NEW)-ARRAY['pending_revision_id','pending_revision_hash','pending_byte_length','pending_sealed_at',
                'expected_selected_revision_id','expected_source_version','pending_consent_context','updated_at']
                =to_jsonb(OLD)-ARRAY['pending_revision_id','pending_revision_hash','pending_byte_length','pending_sealed_at',
                'expected_selected_revision_id','expected_source_version','pending_consent_context','updated_at'] THEN RETURN NEW; END IF;
        IF TG_OP='UPDATE' AND current_user<>'rag_runtime' AND (
            (OLD.reserved_by IS NOT NULL AND NEW.reserved_by IS NULL
                AND EXISTS(SELECT 1 FROM rag.retrieval_readers r WHERE r.reservation_token=OLD.reserved_by AND r.state='TERMINAL'))
            OR (NEW.snapshot_integrity_failed AND NOT OLD.snapshot_integrity_failed
                AND to_jsonb(NEW)-ARRAY['snapshot_integrity_failed','updated_at']=to_jsonb(OLD)-ARRAY['snapshot_integrity_failed','updated_at']
                AND NOT EXISTS(SELECT 1 FROM rag.retrieval_inspections i WHERE i.affected_file_source_id=OLD.file_source_id
                    AND i.state='UNRESOLVED' AND (i.affected_revision_id IS NULL OR i.affected_revision_id IN (OLD.selected_revision_id,OLD.pending_revision_id)))
                AND EXISTS(SELECT 1 FROM rag.retrieval_inspections i WHERE i.affected_file_source_id=OLD.file_source_id
                    AND i.state='INCIDENT_PENDING' AND i.affected_revision_id IN (OLD.selected_revision_id,OLD.pending_revision_id)))
        ) THEN RETURN NEW; END IF;
        RAISE EXCEPTION 'External snapshot inspection unresolved or pending' USING ERRCODE='check_violation';
    END IF;
    IF TG_OP='DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END $$;
CREATE TRIGGER trg_external_pending_inspections BEFORE UPDATE OR DELETE ON rag.external_file_sources
    FOR EACH ROW EXECUTE FUNCTION rag.guard_external_pending_inspections();

REVOKE ALL ON rag.retrieval_readers, rag.retrieval_inspections FROM PUBLIC, rag_worker;
REVOKE INSERT, UPDATE, DELETE ON rag.retrieval_readers, rag.retrieval_inspections FROM rag_runtime;
GRANT SELECT ON rag.retrieval_readers, rag.retrieval_inspections TO rag_runtime;

REVOKE ALL ON FUNCTION
    rag.retrieval_reader_admit(UUID, UUID, VARCHAR, JSONB, UUID[], VARCHAR),
    rag.retrieval_inspection_begin(UUID, UUID, VARCHAR, VARCHAR, UUID, UUID),
    rag.retrieval_inspection_record(UUID, VARCHAR, BOOLEAN, JSONB),
    rag.retrieval_reader_request_stop(UUID, VARCHAR),
    rag.retrieval_reader_begin_emission(UUID),
    rag.retrieval_reader_finish(UUID, UUID, VARCHAR, VARCHAR, JSONB, UUID[], UUID[]),
    rag.retrieval_reader_recover_stale(VARCHAR)
FROM PUBLIC, rag_worker;

GRANT EXECUTE ON FUNCTION
    rag.retrieval_reader_admit(UUID, UUID, VARCHAR, JSONB, UUID[], VARCHAR),
    rag.retrieval_inspection_begin(UUID, UUID, VARCHAR, VARCHAR, UUID, UUID),
    rag.retrieval_inspection_record(UUID, VARCHAR, BOOLEAN, JSONB),
    rag.retrieval_reader_request_stop(UUID, VARCHAR),
    rag.retrieval_reader_begin_emission(UUID),
    rag.retrieval_reader_finish(UUID, UUID, VARCHAR, VARCHAR, JSONB, UUID[], UUID[]),
    rag.retrieval_reader_recover_stale(VARCHAR)
TO rag_runtime;
