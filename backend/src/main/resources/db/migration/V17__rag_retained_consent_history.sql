-- Source configuration/consent history is retained. Deleting and recreating a
-- catalog row must never reset approval CAS history for the same owner identity.
REVOKE DELETE ON rag.sources FROM PUBLIC,rag_runtime,rag_worker;
CREATE OR REPLACE FUNCTION rag.guard_source_policy() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE ids bigint[];
BEGIN
    IF TG_OP='DELETE' THEN
        IF OLD.source_type IN ('FICTION','FILM') THEN
            PERFORM public.vault_rag_require_editable(ARRAY[OLD.source_id::bigint]);
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
    IF TG_OP='UPDATE' THEN
        IF NEW.classification IS DISTINCT FROM OLD.classification THEN NEW.access_state:='REVOKED'; END IF;
        -- Canonical history includes indirect SQL denial/revocation, not only the
        -- maintained Java command. A delayed old approval must fail all current CAS.
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
