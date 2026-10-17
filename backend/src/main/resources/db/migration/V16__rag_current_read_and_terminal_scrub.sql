-- A short read authorization may coexist with a durable indexing reservation.
-- This is a Vault metadata boundary, not worker/business SQL access.
CREATE FUNCTION public.vault_rag_lock_for_read(p_ids bigint[])
RETURNS SETOF public.vault_rag_source_state LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE item bigint; source_row public.vault_rag_source_state%ROWTYPE;
BEGIN
    IF p_ids IS NULL OR cardinality(p_ids) NOT BETWEEN 1 AND 100 OR array_position(p_ids,NULL) IS NOT NULL
        OR cardinality(p_ids)<>(SELECT count(DISTINCT value) FROM unnest(p_ids) value) THEN
        RAISE EXCEPTION 'Source read scope denied' USING ERRCODE='check_violation'; END IF;
    FOR item IN SELECT unnest(p_ids) ORDER BY 1 LOOP
        SELECT * INTO source_row FROM public.vault_rag_source_state WHERE source_id=item FOR SHARE;
        IF NOT FOUND THEN RAISE EXCEPTION 'Source history unavailable' USING ERRCODE='check_violation'; END IF;
        RETURN NEXT source_row;
    END LOOP;
END $$;
REVOKE ALL ON FUNCTION public.vault_rag_lock_for_read(bigint[]) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION public.vault_rag_lock_for_read(bigint[]) TO rag_runtime;

-- Verified cleanup retains the historical outcome but removes attempt-specific
-- digests, field selections and capabilities after all content/writer routes close.
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
        AND NOT EXISTS(SELECT 1 FROM rag.index_generations WHERE job_id=OLD.job_id AND is_active);
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
