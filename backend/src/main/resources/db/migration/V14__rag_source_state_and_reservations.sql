-- Alternative B Group A: retained Vault authority and owner-local mutation backstops.
-- No worker rights, execution enablement, expired-reservation release or global epoch.
CREATE TABLE public.vault_rag_source_state (
    source_id bigint PRIMARY KEY,
    source_type varchar(32) NOT NULL CHECK (source_type IN ('FICTION','FILM')),
    source_version bigint NOT NULL DEFAULT 1 CHECK (source_version>0),
    consent_reset_version bigint NOT NULL DEFAULT 1 CHECK (consent_reset_version>0 AND consent_reset_version<=source_version),
    deleted boolean NOT NULL DEFAULT false,
    reserved_by uuid
);
CREATE INDEX idx_vault_rag_reservation ON public.vault_rag_source_state(reserved_by) WHERE reserved_by IS NOT NULL;
INSERT INTO public.vault_rag_source_state(source_id,source_type,deleted)
    SELECT id,entry_type::text,deleted_at IS NOT NULL FROM public.vault_entries
    WHERE entry_type::text IN ('FICTION','FILM');
REVOKE ALL ON public.vault_rag_source_state FROM PUBLIC,rag_worker,rag_runtime;
GRANT SELECT ON public.vault_rag_source_state TO rag_runtime;

ALTER TABLE rag.sources ADD COLUMN approved_owner_reset_version bigint CHECK (approved_owner_reset_version>0);
-- Prior approvals do not bind retained owner history and cannot silently acquire it.
UPDATE rag.sources SET access_state='DENIED',authorization_epoch=authorization_epoch+1,
    approved_owner_reset_version=NULL WHERE access_state='APPROVED';

CREATE FUNCTION public.vault_rag_require_editable(p_ids bigint[]) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE item bigint; state public.vault_rag_source_state%ROWTYPE;
BEGIN
    IF p_ids IS NULL OR array_position(p_ids,NULL) IS NOT NULL THEN
        RAISE EXCEPTION 'Source authority unavailable' USING ERRCODE='check_violation'; END IF;
    FOR item IN SELECT DISTINCT unnest(p_ids) ORDER BY 1 LOOP
        SELECT * INTO state FROM public.vault_rag_source_state WHERE source_id=item FOR UPDATE NOWAIT;
        IF NOT FOUND THEN
            IF EXISTS(SELECT 1 FROM public.vault_entries WHERE id=item AND entry_type::text NOT IN ('FICTION','FILM')) THEN
                CONTINUE;
            END IF;
            RAISE EXCEPTION 'Source authority unavailable' USING ERRCODE='check_violation';
        END IF;
        IF state.reserved_by IS NOT NULL THEN
            RAISE EXCEPTION 'RAG_SOURCE_BUSY' USING ERRCODE='P1701'; END IF;
    END LOOP;
EXCEPTION WHEN lock_not_available THEN
    RAISE EXCEPTION 'RAG_SOURCE_BUSY' USING ERRCODE='P1701';
END $$;

CREATE FUNCTION public.vault_rag_lock_reserved(p_ids bigint[],p_token uuid)
RETURNS SETOF public.vault_rag_source_state
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE item bigint; state public.vault_rag_source_state%ROWTYPE;
BEGIN
    IF p_token IS NULL OR p_ids IS NULL OR cardinality(p_ids) NOT BETWEEN 1 AND 100
        OR array_position(p_ids,NULL) IS NOT NULL
        OR cardinality(p_ids)<>(SELECT count(DISTINCT value) FROM unnest(p_ids) value) THEN
        RAISE EXCEPTION 'Reservation scope denied' USING ERRCODE='check_violation'; END IF;
    FOR item IN SELECT unnest(p_ids) ORDER BY 1 LOOP
        SELECT * INTO state FROM public.vault_rag_source_state WHERE source_id=item FOR UPDATE;
        IF NOT FOUND OR state.reserved_by IS DISTINCT FROM p_token OR state.deleted THEN
            RAISE EXCEPTION 'Reservation authority denied' USING ERRCODE='check_violation'; END IF;
        RETURN NEXT state;
    END LOOP;
    IF (SELECT count(*) FROM public.vault_rag_source_state WHERE reserved_by=p_token)<>cardinality(p_ids) THEN
        RAISE EXCEPTION 'Incomplete reservation scope denied' USING ERRCODE='check_violation'; END IF;
EXCEPTION WHEN lock_not_available THEN
    RAISE EXCEPTION 'RAG_SOURCE_BUSY' USING ERRCODE='P1701';
END $$;

CREATE FUNCTION public.vault_rag_reserve(p_ids bigint[],p_token uuid) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE item bigint; state public.vault_rag_source_state%ROWTYPE;
BEGIN
    IF p_token IS NULL OR p_ids IS NULL OR cardinality(p_ids) NOT BETWEEN 1 AND 100
        OR array_position(p_ids,NULL) IS NOT NULL
        OR cardinality(p_ids)<>(SELECT count(DISTINCT value) FROM unnest(p_ids) value)
        OR EXISTS(SELECT 1 FROM public.vault_rag_source_state WHERE reserved_by=p_token) THEN
        RAISE EXCEPTION 'Reservation scope denied' USING ERRCODE='check_violation'; END IF;
    FOR item IN SELECT unnest(p_ids) ORDER BY 1 LOOP
        SELECT * INTO state FROM public.vault_rag_source_state WHERE source_id=item FOR UPDATE;
        IF NOT FOUND OR state.deleted THEN
            RAISE EXCEPTION 'Source authority unavailable' USING ERRCODE='check_violation'; END IF;
        IF state.reserved_by IS NOT NULL THEN
            RAISE EXCEPTION 'RAG_SOURCE_BUSY' USING ERRCODE='P1701'; END IF;
        UPDATE public.vault_rag_source_state SET reserved_by=p_token WHERE source_id=item;
    END LOOP;
EXCEPTION WHEN lock_not_available THEN
    RAISE EXCEPTION 'RAG_SOURCE_BUSY' USING ERRCODE='P1701';
END $$;

CREATE FUNCTION public.vault_rag_release(p_ids bigint[],p_token uuid) RETURNS void
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN
    PERFORM public.vault_rag_lock_reserved(p_ids,p_token);
    UPDATE public.vault_rag_source_state SET reserved_by=NULL WHERE source_id=ANY(p_ids) AND reserved_by=p_token;
END $$;

REVOKE ALL ON FUNCTION public.vault_rag_require_editable(bigint[]),public.vault_rag_reserve(bigint[],uuid),
    public.vault_rag_lock_reserved(bigint[],uuid),public.vault_rag_release(bigint[],uuid) FROM PUBLIC,rag_worker;
GRANT EXECUTE ON FUNCTION public.vault_rag_require_editable(bigint[]),public.vault_rag_reserve(bigint[],uuid),
    public.vault_rag_lock_reserved(bigint[],uuid),public.vault_rag_release(bigint[],uuid) TO rag_runtime;

-- Private canonical mutation helper. Runtime has no table DML or direct helper right.
-- SQL backstops may already hold a later owner row: NOWAIT prevents reverse waits.
CREATE FUNCTION public.vault_rag_apply_change(p_ids bigint[],p_projected boolean,p_reset boolean,p_deleted boolean DEFAULT NULL)
RETURNS void LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN
    PERFORM public.vault_rag_require_editable(p_ids);
    IF p_projected OR p_reset THEN
        UPDATE public.vault_rag_source_state SET
            source_version=source_version+1,
            consent_reset_version=CASE WHEN p_reset THEN source_version+1 ELSE consent_reset_version END,
            deleted=coalesce(p_deleted,deleted)
            WHERE source_id=ANY(p_ids);
    END IF;
END $$;
REVOKE ALL ON FUNCTION public.vault_rag_apply_change(bigint[],boolean,boolean,boolean) FROM PUBLIC,rag_worker,rag_runtime;

CREATE FUNCTION public.vault_rag_lifecycle_backstop() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
BEGIN
    IF TG_OP='INSERT' THEN
        IF NEW.entry_type::text IN ('FICTION','FILM') THEN
            -- Deliberately no ON CONFLICT: retained identities can never be reused.
            INSERT INTO public.vault_rag_source_state(source_id,source_type,deleted)
                VALUES(NEW.id,NEW.entry_type::text,NEW.deleted_at IS NOT NULL);
        END IF;
        RETURN NEW;
    END IF;
    IF TG_OP='UPDATE' AND (NEW.id IS DISTINCT FROM OLD.id OR NEW.entry_type IS DISTINCT FROM OLD.entry_type) THEN
        RAISE EXCEPTION 'Vault source identity is immutable' USING ERRCODE='check_violation'; END IF;
    IF TG_OP='DELETE' THEN
        IF OLD.entry_type::text IN ('FICTION','FILM') THEN
            PERFORM public.vault_rag_apply_change(ARRAY[OLD.id],true,true,true);
        END IF;
        RETURN OLD;
    END IF;
    IF OLD.entry_type::text IN ('FICTION','FILM') THEN
        PERFORM public.vault_rag_apply_change(ARRAY[OLD.id],NEW.deleted_at IS DISTINCT FROM OLD.deleted_at,
            NEW.deleted_at IS DISTINCT FROM OLD.deleted_at,NEW.deleted_at IS NOT NULL);
    END IF;
    RETURN NEW;
END $$;
REVOKE ALL ON FUNCTION public.vault_rag_lifecycle_backstop() FROM PUBLIC,rag_worker,rag_runtime;
CREATE TRIGGER trg_vault_rag_lifecycle BEFORE INSERT OR UPDATE OR DELETE ON public.vault_entries
    FOR EACH ROW EXECUTE FUNCTION public.vault_rag_lifecycle_backstop();

CREATE FUNCTION public.vault_rag_owner_backstop() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE previous jsonb; current_value jsonb; ids bigint[]; item bigint; column_name text;
    projected boolean; reset_consent boolean; field text;
BEGIN
    previous:=CASE WHEN TG_OP='INSERT' THEN '{}'::jsonb ELSE to_jsonb(OLD) END;
    current_value:=CASE WHEN TG_OP='DELETE' THEN '{}'::jsonb ELSE to_jsonb(NEW) END;
    column_name:=TG_ARGV[0];
    SELECT array_agg(DISTINCT value ORDER BY value) INTO ids FROM (
        SELECT (previous->>column_name)::bigint AS value UNION SELECT (current_value->>column_name)::bigint
    ) affected WHERE value IS NOT NULL;
    IF ids IS NULL THEN RAISE EXCEPTION 'Owner source scope unavailable' USING ERRCODE='check_violation'; END IF;
    IF TG_ARGV[1]='entity' THEN
        IF TG_OP='UPDATE' AND previous->column_name IS DISTINCT FROM current_value->column_name THEN
            RAISE EXCEPTION 'Owner source identity is immutable' USING ERRCODE='check_violation'; END IF;
        IF TG_OP='INSERT' THEN
            FOR item IN SELECT unnest(ids) LOOP
                IF NOT EXISTS(SELECT 1 FROM public.vault_rag_source_state s JOIN public.vault_entries v ON v.id=s.source_id
                    WHERE s.source_id=item AND NOT s.deleted AND s.source_type=CASE TG_TABLE_NAME WHEN 'fictions' THEN 'FICTION' ELSE 'FILM' END
                    AND v.entry_type::text=s.source_type) THEN
                    RAISE EXCEPTION 'Owner source identity denied' USING ERRCODE='check_violation'; END IF;
            END LOOP;
        END IF;
        projected:=TG_OP<>'UPDATE';
        IF TG_OP='UPDATE' THEN
            FOREACH field IN ARRAY string_to_array(TG_ARGV[2],',') LOOP
                projected:=projected OR previous->field IS DISTINCT FROM current_value->field;
            END LOOP;
        END IF;
        reset_consent:=TG_OP='DELETE' OR (TG_OP='UPDATE' AND previous->'is_nsfw' IS DISTINCT FROM current_value->'is_nsfw');
        PERFORM public.vault_rag_apply_change(ids,projected,reset_consent,CASE WHEN TG_OP='DELETE' THEN true ELSE NULL END);
    ELSIF column_name='vault_entry_id' THEN
        -- Nonparticipating Vault types retain their existing cascade semantics. A
        -- missing guard for a still-present eligible entry is never silently skipped.
        SELECT array_agg(value ORDER BY value) INTO ids FROM unnest(ids) value
            WHERE EXISTS(SELECT 1 FROM public.vault_rag_source_state WHERE source_id=value)
                OR EXISTS(SELECT 1 FROM public.vault_entries WHERE id=value AND entry_type::text IN ('FICTION','FILM'));
        IF ids IS NOT NULL THEN PERFORM public.vault_rag_apply_change(ids,false,false); END IF;
    ELSE
        PERFORM public.vault_rag_apply_change(ids,TG_ARGV[1]='projected',false);
    END IF;
    IF TG_OP='DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END $$;
REVOKE ALL ON FUNCTION public.vault_rag_owner_backstop() FROM PUBLIC,rag_worker,rag_runtime;
CREATE TRIGGER trg_fiction_rag_guard BEFORE INSERT OR UPDATE OR DELETE ON public.fictions FOR EACH ROW
    EXECUTE FUNCTION public.vault_rag_owner_backstop('id','entity','title,original_title,format,nationality_code,genre_id,description,progress_status,consumption_status,total_chapters,is_nsfw');
CREATE TRIGGER trg_film_rag_guard BEFORE INSERT OR UPDATE OR DELETE ON public.films FOR EACH ROW
    EXECUTE FUNCTION public.vault_rag_owner_backstop('id','entity','title,original_title,format,production_style,nationality_code,description,progress_status,consumption_status,total_episodes,is_nsfw');

-- Cover OLD and NEW parent IDs, including FK cascades and direct/bulk association writes.
DO $$
DECLARE item record;
BEGIN
    FOR item IN SELECT * FROM (VALUES
        ('fiction_story_archetypes','fiction_id','projected'),('fiction_world_settings','fiction_id','projected'),
        ('film_genre_assignments','film_id','projected'),('film_story_archetypes','film_id','projected'),('film_world_settings','film_id','projected'),
        ('fiction_links','fiction_id','configuration'),('film_links','film_id','configuration'),('film_credits','film_id','configuration'),
        ('favorites','vault_entry_id','configuration'),('ratings','vault_entry_id','configuration'),('vault_entry_tags','vault_entry_id','configuration')
    ) AS coverage(table_name,parent_column,change_kind) LOOP
        EXECUTE format('CREATE TRIGGER trg_owner_rag_guard AFTER INSERT OR UPDATE OR DELETE ON public.%I FOR EACH ROW EXECUTE FUNCTION public.vault_rag_owner_backstop(%L,%L)',
            item.table_name,item.parent_column,item.change_kind);
    END LOOP;
END $$;

CREATE FUNCTION rag.guard_source_policy() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE ids bigint[];
BEGIN
    IF TG_OP='UPDATE' AND (NEW.id IS DISTINCT FROM OLD.id OR NEW.source_type IS DISTINCT FROM OLD.source_type OR
        (NEW.source_id IS DISTINCT FROM OLD.source_id AND NOT (OLD.source_type='EXTERNAL_FILE'
            AND NEW.source_id='disabled:'||OLD.id::text AND NEW.access_state IN ('DENIED','REVOKED')
            AND NEW.file_identity IS NULL AND NEW.source_revision_hash='DISABLED'
            AND NEW.description_revision_hash='DISABLED' AND NEW.approved_fields_text=''))) THEN
        RAISE EXCEPTION 'RAG source identity is immutable' USING ERRCODE='check_violation'; END IF;
    IF TG_OP='DELETE' THEN
        IF OLD.source_type IN ('FICTION','FILM') THEN PERFORM public.vault_rag_require_editable(ARRAY[OLD.source_id::bigint]); END IF;
        RETURN OLD;
    END IF;
    IF NEW.source_type IN ('FICTION','FILM') THEN
        ids:=ARRAY[NEW.source_id::bigint];
        PERFORM public.vault_rag_require_editable(ids);
        IF NOT EXISTS(SELECT 1 FROM public.vault_rag_source_state WHERE source_id=NEW.source_id::bigint AND source_type=NEW.source_type AND NOT deleted) THEN
            RAISE EXCEPTION 'Owner source authority denied' USING ERRCODE='check_violation'; END IF;
    END IF;
    IF TG_OP='UPDATE' THEN
        IF NEW.classification IS DISTINCT FROM OLD.classification THEN
            NEW.access_state:='REVOKED'; NEW.approved_owner_reset_version:=NULL;
            NEW.authorization_epoch:=greatest(NEW.authorization_epoch,OLD.authorization_epoch+1);
        END IF;
        IF NEW.approved_fields_text IS DISTINCT FROM OLD.approved_fields_text THEN
            NEW.authorization_epoch:=greatest(NEW.authorization_epoch,OLD.authorization_epoch+1);
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
CREATE TRIGGER trg_source_policy_reservation BEFORE INSERT OR UPDATE OR DELETE ON rag.sources
    FOR EACH ROW EXECUTE FUNCTION rag.guard_source_policy();

CREATE FUNCTION rag.guard_dependency_change() RETURNS trigger
LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog AS $$
DECLARE parents bigint[]; ancestors bigint[]; owners bigint[];
BEGIN
    IF NOT pg_try_advisory_xact_lock(5927097059089141831) THEN
        RAISE EXCEPTION 'Dependency graph busy' USING ERRCODE='P1701'; END IF;
    parents:=CASE TG_OP WHEN 'INSERT' THEN ARRAY[NEW.parent_source_id] WHEN 'DELETE' THEN ARRAY[OLD.parent_source_id]
        ELSE ARRAY[OLD.parent_source_id,NEW.parent_source_id] END;
    WITH RECURSIVE affected(id) AS (
        SELECT unnest(parents) UNION SELECT d.parent_source_id FROM rag.dependencies d JOIN affected a ON d.child_source_id=a.id
    ) SELECT array_agg(id ORDER BY id) INTO ancestors FROM affected;
    IF cardinality(ancestors) NOT BETWEEN 1 AND 100 THEN
        RAISE EXCEPTION 'Dependency mutation scope exceeds budget' USING ERRCODE='check_violation'; END IF;
    IF EXISTS(SELECT 1 FROM rag.sources WHERE id=ANY(ancestors) AND source_type NOT IN ('FICTION','FILM')) THEN
        RAISE EXCEPTION 'Dependency owner scope unavailable' USING ERRCODE='check_violation'; END IF;
    SELECT array_agg(DISTINCT source_id::bigint ORDER BY source_id::bigint) INTO owners FROM rag.sources WHERE id=ANY(ancestors);
    IF cardinality(owners) IS DISTINCT FROM cardinality(ancestors) THEN
        RAISE EXCEPTION 'Dependency owner scope unavailable' USING ERRCODE='check_violation'; END IF;
    PERFORM public.vault_rag_apply_change(owners,true,false);
    IF TG_OP='DELETE' THEN RETURN OLD; END IF;
    RETURN NEW;
END $$;
REVOKE ALL ON FUNCTION rag.guard_dependency_change() FROM PUBLIC,rag_worker,rag_runtime;
CREATE TRIGGER trg_dependency_source_reservation BEFORE INSERT OR UPDATE OR DELETE ON rag.dependencies
    FOR EACH ROW EXECUTE FUNCTION rag.guard_dependency_change();
