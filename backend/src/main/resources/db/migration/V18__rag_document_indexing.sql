-- V18__rag_document_indexing.sql
-- Phase 18 ADR-0021 Bounded Multi-Root Document Indexing and Atomic Publication
-- Retains single active attempt, worker attempt-to-staging isolation, and through-commit fences.

-- 1. RAG-owned external file sources and reservations
CREATE TABLE IF NOT EXISTS rag.external_file_sources (
    id BIGSERIAL PRIMARY KEY,
    file_source_id UUID NOT NULL UNIQUE,
    canonical_identity_digest VARCHAR(128) NOT NULL,
    source_version BIGINT NOT NULL DEFAULT 1 CHECK (source_version > 0),
    consent_reset_version BIGINT NOT NULL DEFAULT 1 CHECK (consent_reset_version > 0 AND consent_reset_version <= source_version),
    authorization_epoch BIGINT NOT NULL DEFAULT 1 CHECK (authorization_epoch > 0),
    field_policy_version INT NOT NULL DEFAULT 1 CHECK (field_policy_version > 0),
    eligible BOOLEAN NOT NULL DEFAULT true,
    reserved_by UUID,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_rag_external_reservation ON rag.external_file_sources(reserved_by) WHERE reserved_by IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_rag_external_digest ON rag.external_file_sources(canonical_identity_digest);

REVOKE ALL ON rag.external_file_sources FROM PUBLIC, rag_worker, rag_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON rag.external_file_sources TO rag_runtime;

CREATE OR REPLACE FUNCTION rag.guard_external_file_sources_mutation()
RETURNS trigger LANGUAGE plpgsql AS $$
BEGIN
    IF TG_OP = 'DELETE' THEN
        IF OLD.reserved_by IS NOT NULL THEN
            RAISE EXCEPTION 'Cannot delete reserved external file source' USING ERRCODE='check_violation';
        END IF;
        RETURN OLD;
    ELSIF TG_OP = 'UPDATE' THEN
        IF OLD.reserved_by IS DISTINCT FROM NEW.reserved_by THEN
            IF OLD.file_source_id IS DISTINCT FROM NEW.file_source_id
                OR OLD.canonical_identity_digest IS DISTINCT FROM NEW.canonical_identity_digest
                OR OLD.source_version IS DISTINCT FROM NEW.source_version
                OR OLD.consent_reset_version IS DISTINCT FROM NEW.consent_reset_version
                OR OLD.authorization_epoch IS DISTINCT FROM NEW.authorization_epoch
                OR OLD.field_policy_version IS DISTINCT FROM NEW.field_policy_version THEN
                RAISE EXCEPTION 'Cannot modify identity, versions, or epoch of external file source during reservation' USING ERRCODE='check_violation';
            END IF;
        ELSE
            IF OLD.reserved_by IS NOT NULL THEN
                RAISE EXCEPTION 'Cannot modify reserved external file source' USING ERRCODE='check_violation';
            END IF;
            IF OLD.file_source_id IS DISTINCT FROM NEW.file_source_id
                OR OLD.canonical_identity_digest IS DISTINCT FROM NEW.canonical_identity_digest THEN
                RAISE EXCEPTION 'Immutable identity on external file source' USING ERRCODE='check_violation';
            END IF;
        END IF;
        RETURN NEW;
    END IF;
    RETURN NEW;
END $$;

DROP TRIGGER IF EXISTS trg_external_file_sources_guard ON rag.external_file_sources;
CREATE TRIGGER trg_external_file_sources_guard
    BEFORE UPDATE OR DELETE ON rag.external_file_sources
    FOR EACH ROW EXECUTE FUNCTION rag.guard_external_file_sources_mutation();

-- Locking and reservation functions for external files (ADR-0021 Clause 2, order slot 3)
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
        RETURN NEXT state;
    END LOOP;
    IF (SELECT count(*) FROM rag.external_file_sources WHERE reserved_by=p_token) <> cardinality(p_ids) THEN
        RAISE EXCEPTION 'Incomplete external file reservation scope denied' USING ERRCODE='check_violation';
    END IF;
EXCEPTION WHEN lock_not_available THEN
    RAISE EXCEPTION 'RAG_SOURCE_BUSY' USING ERRCODE='P1701';
END $$;

CREATE OR REPLACE FUNCTION rag.external_file_release(p_ids UUID[], p_token UUID) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN
    PERFORM rag.external_file_lock_reserved(p_ids, p_token);
    UPDATE rag.external_file_sources SET reserved_by=NULL, updated_at=clock_timestamp()
        WHERE file_source_id=ANY(p_ids) AND reserved_by=p_token;
END $$;

REVOKE ALL ON FUNCTION rag.external_file_require_editable(UUID[]),
    rag.external_file_reserve(UUID[], UUID),
    rag.external_file_lock_reserved(UUID[], UUID),
    rag.external_file_release(UUID[], UUID) FROM PUBLIC, rag_worker;
GRANT EXECUTE ON FUNCTION rag.external_file_require_editable(UUID[]),
    rag.external_file_reserve(UUID[], UUID),
    rag.external_file_lock_reserved(UUID[], UUID),
    rag.external_file_release(UUID[], UUID) TO rag_runtime;

-- 2. Explicit manual batch root admission set
CREATE TABLE IF NOT EXISTS rag.batch_roots (
    job_id UUID NOT NULL,
    root_key VARCHAR(255) NOT NULL,
    source_id BIGINT NOT NULL REFERENCES rag.sources(id) ON DELETE CASCADE,
    expected_source_version BIGINT NOT NULL,
    expected_authorization_epoch BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (job_id, root_key)
);

CREATE INDEX IF NOT EXISTS idx_rag_batch_roots_job ON rag.batch_roots(job_id);

REVOKE ALL ON rag.batch_roots FROM PUBLIC, rag_worker, rag_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON rag.batch_roots TO rag_runtime;

-- 3. Per-file outcome reporting table
CREATE TABLE IF NOT EXISTS rag.file_outcomes (
    job_id UUID NOT NULL,
    root_key VARCHAR(255) NOT NULL,
    result VARCHAR(32) NOT NULL,
    candidate_generation_id UUID,
    warning_codes JSONB NOT NULL DEFAULT '[]'::jsonb,
    chunk_count INT NOT NULL DEFAULT 0,
    page_count INT NOT NULL DEFAULT 0,
    error_code VARCHAR(64),
    error_message TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    PRIMARY KEY (job_id, root_key)
);

CREATE INDEX IF NOT EXISTS idx_rag_file_outcomes_job ON rag.file_outcomes(job_id);

REVOKE ALL ON rag.file_outcomes FROM PUBLIC, rag_worker, rag_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON rag.file_outcomes TO rag_runtime;

-- 4. Per-generation root metadata
CREATE TABLE IF NOT EXISTS rag.generation_roots (
    generation_id BIGINT PRIMARY KEY REFERENCES rag.index_generations(id) ON DELETE CASCADE,
    root_key VARCHAR(255) NOT NULL,
    exact_dependency_manifest JSONB NOT NULL,
    model_artifact_hash VARCHAR(128) NOT NULL,
    model_version VARCHAR(128) NOT NULL,
    tokenizer_version VARCHAR(128) NOT NULL,
    parser_version VARCHAR(128) NOT NULL,
    chunker_version VARCHAR(128) NOT NULL,
    ocr_pipeline_version VARCHAR(128) NOT NULL,
    vector_dimensions INT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX IF NOT EXISTS idx_rag_generation_roots_key ON rag.generation_roots(root_key);

REVOKE ALL ON rag.generation_roots FROM PUBLIC, rag_worker, rag_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON rag.generation_roots TO rag_runtime;

-- 5. Published document chunks with mandatory locators and vectors
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'vector') THEN
        CREATE TABLE IF NOT EXISTS rag.document_chunks (
            chunk_id BIGSERIAL PRIMARY KEY,
            generation_id BIGINT NOT NULL REFERENCES rag.generation_roots(generation_id) ON DELETE CASCADE,
            source_identity VARCHAR(255) NOT NULL,
            content TEXT NOT NULL,
            locator JSONB NOT NULL,
            structure_metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
            embedding vector(384) DEFAULT NULL,
            created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
        );
    ELSE
        CREATE TABLE IF NOT EXISTS rag.document_chunks (
            chunk_id BIGSERIAL PRIMARY KEY,
            generation_id BIGINT NOT NULL REFERENCES rag.generation_roots(generation_id) ON DELETE CASCADE,
            source_identity VARCHAR(255) NOT NULL,
            content TEXT NOT NULL,
            locator JSONB NOT NULL,
            structure_metadata JSONB NOT NULL DEFAULT '{}'::jsonb,
            embedding BYTEA DEFAULT ''::bytea,
            created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
        );
    END IF;

END $$;

CREATE INDEX IF NOT EXISTS idx_rag_doc_chunks_gen ON rag.document_chunks(generation_id);
CREATE INDEX IF NOT EXISTS idx_rag_doc_chunks_src ON rag.document_chunks(source_identity);

REVOKE ALL ON rag.document_chunks FROM PUBLIC, rag_worker, rag_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON rag.document_chunks TO rag_runtime;
GRANT USAGE, SELECT ON SEQUENCE rag.document_chunks_chunk_id_seq TO rag_runtime;

-- 6. Audited restricted staging table for v3 document worker
CREATE TABLE IF NOT EXISTS rag.staged_document_outputs (
    id BIGSERIAL PRIMARY KEY,
    job_id UUID NOT NULL,
    attempt_id UUID NOT NULL,
    root_key VARCHAR(255) NOT NULL,
    success BOOLEAN NOT NULL,
    reason_code VARCHAR(64) NOT NULL,
    warning_codes JSONB NOT NULL DEFAULT '[]'::jsonb,
    chunks_payload JSONB NOT NULL DEFAULT '[]'::jsonb,
    resource_report JSONB NOT NULL DEFAULT '{}'::jsonb,
    worker_receipt BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    CONSTRAINT uq_staged_doc_root UNIQUE (job_id, attempt_id, root_key)
);

CREATE INDEX IF NOT EXISTS idx_rag_staged_doc_attempt ON rag.staged_document_outputs(job_id, attempt_id);

REVOKE ALL ON rag.staged_document_outputs FROM PUBLIC, rag_worker, rag_runtime;
GRANT SELECT, INSERT, UPDATE, DELETE ON rag.staged_document_outputs TO rag_runtime;
GRANT USAGE, SELECT ON SEQUENCE rag.staged_document_outputs_id_seq TO rag_runtime, rag_worker;

DROP TRIGGER IF EXISTS trg_staged_document_outputs_lease_fence ON rag.staged_document_outputs;
CREATE CONSTRAINT TRIGGER trg_staged_document_outputs_lease_fence
    AFTER INSERT OR UPDATE ON rag.staged_document_outputs
    DEFERRABLE INITIALLY DEFERRED
    FOR EACH ROW EXECUTE FUNCTION rag.commit_stage_deadline();

-- 7. Allow DOCUMENT_V3 stage type in worker_grants
ALTER TABLE rag.worker_grants DROP CONSTRAINT IF EXISTS worker_grants_stage_type_check;
ALTER TABLE rag.worker_grants ADD CONSTRAINT worker_grants_stage_type_check
    CHECK (stage_type IN ('SYNTHETIC_STAGE', 'SYNTHETIC_TEST', 'DOCUMENT_V3'));

-- Restricted staging function for worker
CREATE OR REPLACE FUNCTION rag.stage_document_result(
    p_job_id UUID,
    p_attempt_id UUID,
    p_lease_token VARCHAR,
    p_root_key VARCHAR,
    p_success BOOLEAN,
    p_reason_code VARCHAR,
    p_warning_codes JSONB,
    p_chunks_payload JSONB,
    p_resource_report JSONB
) RETURNS BIGINT LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE
    lease rag.job_leases%ROWTYPE;
    grant_rec rag.worker_grants%ROWTYPE;
    staged_id BIGINT;
BEGIN
    IF session_user <> 'rag_worker' THEN
        RAISE EXCEPTION 'Worker execution denied' USING ERRCODE='insufficient_privilege';
    END IF;
    IF p_job_id IS NULL OR p_attempt_id IS NULL OR p_lease_token IS NULL OR p_root_key IS NULL
        OR p_reason_code IS NULL OR p_chunks_payload IS NULL THEN
        RAISE EXCEPTION 'Invalid document staging payload' USING ERRCODE='check_violation';
    END IF;

    lease := rag.check_worker_attempt(p_job_id, p_attempt_id, p_lease_token, true);

    -- Check stage type DOCUMENT_V3 in active worker grant
    SELECT * INTO grant_rec FROM rag.worker_grants
        WHERE job_id=p_job_id AND attempt_id=p_attempt_id;
    IF NOT FOUND OR grant_rec.stage_type <> 'DOCUMENT_V3' THEN
        RAISE EXCEPTION 'Stage contract denied: worker grant is not DOCUMENT_V3' USING ERRCODE='check_violation';
    END IF;

    -- Verify root is admitted for this job
    IF NOT EXISTS (SELECT 1 FROM rag.batch_roots WHERE job_id=p_job_id AND root_key=p_root_key) THEN
        RAISE EXCEPTION 'Root key is not admitted for job' USING ERRCODE='check_violation';
    END IF;

    IF p_success AND jsonb_array_length(p_chunks_payload) = 0 THEN
        RAISE EXCEPTION 'Success requires non-empty chunks' USING ERRCODE='check_violation';
    END IF;

    IF jsonb_array_length(p_chunks_payload) > 1000 THEN
        RAISE EXCEPTION 'Chunk count budget exceeded' USING ERRCODE='check_violation';
    END IF;

    INSERT INTO rag.staged_document_outputs (
        job_id, attempt_id, root_key, success, reason_code, warning_codes, chunks_payload, resource_report, worker_receipt
    ) VALUES (
        p_job_id, p_attempt_id, p_root_key, p_success, p_reason_code,
        coalesce(p_warning_codes, '[]'::jsonb), p_chunks_payload,
        coalesce(p_resource_report, '{}'::jsonb), true
    ) RETURNING id INTO staged_id;

    RETURN staged_id;
END $$;

REVOKE ALL ON FUNCTION rag.stage_document_result(UUID, UUID, VARCHAR, VARCHAR, BOOLEAN, VARCHAR, JSONB, JSONB, JSONB)
    FROM PUBLIC, rag_runtime;
GRANT EXECUTE ON FUNCTION rag.stage_document_result(UUID, UUID, VARCHAR, VARCHAR, BOOLEAN, VARCHAR, JSONB, JSONB, JSONB)
    TO rag_worker;

-- 8. Discriminated verify_worker_grant supporting both legacy v2 and Phase 18 DOCUMENT_V3
CREATE OR REPLACE FUNCTION rag.verify_worker_grant(
    p_job UUID, p_attempt UUID, p_token VARCHAR, p_stage VARCHAR, p_input VARCHAR, p_scope VARCHAR
) RETURNS boolean LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE
    lease rag.job_leases%ROWTYPE;
    grant_row rag.worker_grants%ROWTYPE;
    source_row rag.sources%ROWTYPE;
    ext_file_row rag.external_file_sources%ROWTYPE;
    manifest jsonb;
    node jsonb;
    edge jsonb;
    root_item jsonb;
    selected_ids bigint[];
    actual_ids bigint[];
BEGIN
    lease := rag.check_worker_attempt(p_job, p_attempt, p_token, true);
    SELECT * INTO grant_row FROM rag.worker_grants WHERE job_id = p_job AND attempt_id = p_attempt;
    IF p_stage IS NULL OR p_input IS NULL OR p_scope IS NULL OR grant_row.stage_type <> p_stage
        OR grant_row.input_digest <> p_input OR grant_row.scope_digest <> p_scope
        OR grant_row.scope_digest <> encode(sha256(convert_to(lease.dependency_manifest, 'UTF8')), 'hex') THEN
        RAISE EXCEPTION 'Worker issuance denied' USING ERRCODE = 'check_violation';
    END IF;

    manifest := lease.dependency_manifest::jsonb;
    IF jsonb_typeof(manifest) IS DISTINCT FROM 'object' THEN
        RAISE EXCEPTION 'Worker manifest denied' USING ERRCODE = 'check_violation';
    END IF;

    -- Discriminate based on manifest contract_version / grant stage_type
    IF p_stage = 'DOCUMENT_V3' OR manifest->>'contract_version' = '3.0' THEN
        IF manifest->>'contract_version' IS DISTINCT FROM '3.0'
            OR jsonb_typeof(manifest->'roots') IS DISTINCT FROM 'array'
            OR jsonb_array_length(manifest->'roots') NOT BETWEEN 1 AND 100 THEN
            RAISE EXCEPTION 'Worker document manifest denied' USING ERRCODE = 'check_violation';
        END IF;

        FOR root_item IN SELECT * FROM jsonb_array_elements(manifest->'roots') LOOP
            IF jsonb_typeof(root_item) IS DISTINCT FROM 'object'
                OR NOT root_item ?& ARRAY['root_key', 'source_type', 'source_id', 'rag_source_id', 'epoch', 'source_version', 'consent_reset_version', 'field_policy_version', 'fields'] THEN
                RAISE EXCEPTION 'Malformed document root authority denied' USING ERRCODE = 'check_violation';
            END IF;

            -- Check batch_roots membership
            IF NOT EXISTS (
                SELECT 1 FROM rag.batch_roots
                WHERE job_id = p_job AND root_key = root_item->>'root_key'
            ) THEN
                RAISE EXCEPTION 'Unadmitted document root denied' USING ERRCODE = 'check_violation';
            END IF;

            -- Check policy row
            SELECT * INTO source_row FROM rag.sources WHERE id = (root_item->>'rag_source_id')::bigint;
            IF NOT FOUND OR source_row.access_state <> 'APPROVED' OR source_row.classification <> 'NON_PRIVATE'
                OR source_row.authorization_epoch <> (root_item->>'epoch')::bigint
                OR source_row.field_policy_version <> (root_item->>'field_policy_version')::int THEN
                RAISE EXCEPTION 'Document root policy authority denied' USING ERRCODE = 'check_violation';
            END IF;

            -- If external file, check external_file_sources
            IF root_item->>'source_type' = 'EXTERNAL_FILE' THEN
                SELECT * INTO ext_file_row FROM rag.external_file_sources
                WHERE file_source_id = (root_item->>'source_id')::uuid;
                IF NOT FOUND OR NOT ext_file_row.eligible
                    OR ext_file_row.reserved_by IS DISTINCT FROM lease.reservation_token
                    OR ext_file_row.source_version <> (root_item->>'source_version')::bigint
                    OR ext_file_row.consent_reset_version <> (root_item->>'consent_reset_version')::bigint
                    OR ext_file_row.authorization_epoch <> (root_item->>'epoch')::bigint
                    OR ext_file_row.field_policy_version <> (root_item->>'field_policy_version')::int THEN
                    RAISE EXCEPTION 'External file authority denied' USING ERRCODE = 'check_violation';
                END IF;
            END IF;
        END LOOP;
        RETURN true;
    END IF;

    -- Legacy single-root Foundation v2 contract
    IF manifest->>'contract_version' IS DISTINCT FROM '2.0'
        OR jsonb_typeof(manifest->'nodes') IS DISTINCT FROM 'array' OR jsonb_typeof(manifest->'edges') IS DISTINCT FROM 'array'
        OR (manifest->>'root_id')::bigint IS DISTINCT FROM lease.source_id OR (SELECT count(*) FROM jsonb_object_keys(manifest)) <> 4
        OR jsonb_array_length(manifest->'nodes') NOT BETWEEN 1 AND 100 THEN
        RAISE EXCEPTION 'Worker manifest denied' USING ERRCODE = 'check_violation';
    END IF;
    SELECT array_agg(DISTINCT (value->>'source_id')::bigint ORDER BY (value->>'source_id')::bigint)
        INTO selected_ids FROM jsonb_array_elements(manifest->'nodes');
    IF cardinality(selected_ids) <> jsonb_array_length(manifest->'nodes') THEN
        RAISE EXCEPTION 'Duplicate source denied' USING ERRCODE = 'check_violation';
    END IF;
    FOR node IN SELECT * FROM jsonb_array_elements(manifest->'nodes') LOOP
        IF jsonb_typeof(node) IS DISTINCT FROM 'object' OR NOT node ?& ARRAY[
            'source_id','source_type','domain_id','epoch','field_version','fields','content_revision','description_revision',
            'owner_source_version','owner_reset_version'] OR (SELECT count(*) FROM jsonb_object_keys(node)) <> 10
            OR jsonb_typeof(node->'source_id') IS DISTINCT FROM 'number'
            OR jsonb_typeof(node->'epoch') IS DISTINCT FROM 'number' OR jsonb_typeof(node->'field_version') IS DISTINCT FROM 'number'
            OR jsonb_typeof(node->'owner_source_version') IS DISTINCT FROM 'number' OR jsonb_typeof(node->'owner_reset_version') IS DISTINCT FROM 'number'
            OR (node->>'owner_source_version')::bigint <= 0 OR (node->>'owner_reset_version')::bigint <= 0
            OR (node->>'owner_reset_version')::bigint > (node->>'owner_source_version')::bigint
            OR jsonb_typeof(node->'fields') IS DISTINCT FROM 'array' OR jsonb_typeof(node->'source_type') IS DISTINCT FROM 'string'
            OR jsonb_typeof(node->'domain_id') IS DISTINCT FROM 'string' OR jsonb_typeof(node->'content_revision') IS DISTINCT FROM 'string'
            OR jsonb_typeof(node->'description_revision') IS DISTINCT FROM 'string' THEN
            RAISE EXCEPTION 'Malformed source authority denied' USING ERRCODE = 'check_violation';
        END IF;
        SELECT * INTO source_row FROM rag.sources WHERE id = (node->>'source_id')::bigint;
        IF NOT FOUND OR source_row.source_type NOT IN ('FICTION','FILM') OR source_row.access_state <> 'APPROVED'
            OR source_row.classification <> 'NON_PRIVATE' OR source_row.source_type <> node->>'source_type'
            OR source_row.source_id <> node->>'domain_id' OR source_row.authorization_epoch <> (node->>'epoch')::bigint
            OR source_row.field_policy_version <> (node->>'field_version')::int
            OR source_row.approved_owner_reset_version IS DISTINCT FROM (node->>'owner_reset_version')::bigint
            OR source_row.source_revision_hash <> node->>'content_revision'
            OR to_jsonb(string_to_array(source_row.approved_fields_text, ',')) <> node->'fields'
            OR (node->>'description_revision' <> '' AND source_row.description_revision_hash <> node->>'description_revision') THEN
            RAISE EXCEPTION 'Worker source authority denied' USING ERRCODE = 'check_violation';
        END IF;
    END LOOP;
    WITH RECURSIVE closure(id) AS (
        SELECT lease.source_id UNION SELECT d.child_source_id FROM rag.dependencies d JOIN closure c ON d.parent_source_id = c.id
    ) SELECT array_agg(id ORDER BY id) INTO actual_ids FROM closure;
    IF actual_ids IS DISTINCT FROM selected_ids THEN
        RAISE EXCEPTION 'Incomplete dependency closure denied' USING ERRCODE = 'check_violation';
    END IF;
    FOR edge IN SELECT * FROM jsonb_array_elements(manifest->'edges') LOOP
        IF jsonb_typeof(edge) IS DISTINCT FROM 'array' OR jsonb_array_length(edge) <> 2
            OR jsonb_typeof(edge->0) IS DISTINCT FROM 'number' OR jsonb_typeof(edge->1) IS DISTINCT FROM 'number'
            OR NOT (edge->>0)::bigint = ANY(selected_ids) OR NOT (edge->>1)::bigint = ANY(selected_ids) THEN
            RAISE EXCEPTION 'Malformed dependency topology denied' USING ERRCODE = 'check_violation';
        END IF;
    END LOOP;
    IF (SELECT count(DISTINCT value) FROM jsonb_array_elements(manifest->'edges')) <> jsonb_array_length(manifest->'edges')
        OR EXISTS(SELECT 1 FROM rag.dependencies d WHERE d.parent_source_id = ANY(selected_ids) AND NOT EXISTS(
            SELECT 1 FROM jsonb_array_elements(manifest->'edges') e WHERE (e->>0)::bigint = d.parent_source_id AND (e->>1)::bigint = d.child_source_id))
        OR EXISTS(SELECT 1 FROM jsonb_array_elements(manifest->'edges') e WHERE NOT EXISTS(
            SELECT 1 FROM rag.dependencies d WHERE d.parent_source_id = (e->>0)::bigint AND d.child_source_id = (e->>1)::bigint)) THEN
        RAISE EXCEPTION 'Changed dependency topology denied' USING ERRCODE = 'check_violation';
    END IF;
    RETURN true;
END $$;

REVOKE ALL ON FUNCTION rag.verify_worker_grant(UUID, UUID, VARCHAR, VARCHAR, VARCHAR, VARCHAR) FROM PUBLIC, rag_runtime;
GRANT EXECUTE ON FUNCTION rag.verify_worker_grant(UUID, UUID, VARCHAR, VARCHAR, VARCHAR, VARCHAR) TO rag_worker;

