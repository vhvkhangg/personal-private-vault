-- Phase 18 has one document vector representation. Do not silently run against BYTEA.
-- V18 remains append-only. Refuse an unsupported installation rather than dropping data.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_extension WHERE extname='vector') THEN
        RAISE EXCEPTION 'Phase 18 requires the pinned pgvector installation';
    END IF;
    IF EXISTS (SELECT 1 FROM information_schema.columns
               WHERE table_schema='rag' AND table_name='document_chunks'
                 AND column_name='embedding' AND data_type='bytea') THEN
        RAISE EXCEPTION 'BYTEA document storage requires explicit migration before Phase 18';
    END IF;
END $$;

-- Terminal v3 generations own their manifests. Scrub the no-longer-needed union
-- without revoking an unrelated active sibling; legacy v2 retains its original rule.
CREATE OR REPLACE FUNCTION rag.guard_attempt_state() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE scrub boolean;
BEGIN
    IF TG_OP='DELETE' THEN
        IF NOT OLD.staging_closed OR OLD.status NOT IN ('COMPLETED','CANCELLED','ABORTED','PURGED') THEN
            RAISE EXCEPTION 'Nonterminal attempt retention denied' USING ERRCODE='check_violation'; END IF;
        RETURN OLD;
    END IF;
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
    IF (NEW.job_id,NEW.attempt_id,NEW.source_id,NEW.leased_until,NEW.reservation_token,NEW.admitted_epoch,NEW.admitted_field_policy_version)
            IS DISTINCT FROM (OLD.job_id,OLD.attempt_id,OLD.source_id,OLD.leased_until,OLD.reservation_token,OLD.admitted_epoch,OLD.admitted_field_policy_version)
        OR (NOT scrub AND (NEW.lease_token,NEW.approved_fields_text,NEW.content_revision_hash,NEW.description_revision_hash,NEW.dependency_manifest)
            IS DISTINCT FROM (OLD.lease_token,OLD.approved_fields_text,OLD.content_revision_hash,OLD.description_revision_hash,OLD.dependency_manifest))
        OR (OLD.staging_closed AND NOT NEW.staging_closed)
        OR (OLD.controller_identity IS NOT NULL AND NEW.controller_identity IS DISTINCT FROM OLD.controller_identity)
        OR (OLD.worker_identity IS NOT NULL AND NEW.worker_identity IS DISTINCT FROM OLD.worker_identity)
        OR (OLD.status IN ('COMPLETED','CANCELLED','ABORTED','PURGED') AND NEW.status<>OLD.status) THEN
        RAISE EXCEPTION 'Immutable attempt authority denied' USING ERRCODE='check_violation'; END IF;
    RETURN NEW;
END $$;
REVOKE ALL ON FUNCTION rag.guard_attempt_state() FROM PUBLIC,rag_worker,rag_runtime;
