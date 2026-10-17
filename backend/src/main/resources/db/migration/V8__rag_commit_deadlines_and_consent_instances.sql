-- Physical COMMIT expiry fences and restart/restore consent fencing.
ALTER TABLE rag.sources ADD COLUMN approval_instance UUID;
UPDATE rag.sources SET access_state='DENIED',authorization_epoch=authorization_epoch+1
    WHERE access_state='APPROVED';
UPDATE rag.job_leases SET status='CANCELLED' WHERE status IN ('ADMITTED','ACTIVE');
UPDATE rag.index_generations SET is_active=false,status='REVOKED',revoked_at=clock_timestamp() WHERE is_active;

CREATE FUNCTION rag.commit_stage_deadline()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,rag AS $$
DECLARE deadline timestamptz; state varchar; session_deadline timestamptz;
BEGIN
    -- The immediate staging function/trigger already holds this attempt lock.
    SELECT leased_until,status INTO deadline,state FROM rag.job_leases
        WHERE job_id=NEW.job_id AND attempt_id=NEW.attempt_id FOR UPDATE;
    SELECT session_until INTO session_deadline FROM rag.worker_grants
        WHERE job_id=NEW.job_id AND attempt_id=NEW.attempt_id;
    IF deadline IS NULL OR deadline<=clock_timestamp() OR state NOT IN ('ADMITTED','ACTIVE')
        OR (NEW.worker_receipt AND (session_deadline IS NULL OR session_deadline<=clock_timestamp())) THEN
        RAISE EXCEPTION 'Stage COMMIT deadline denied' USING ERRCODE='check_violation';
    END IF;
    RETURN NULL;
END $$;
REVOKE ALL ON FUNCTION rag.commit_stage_deadline() FROM PUBLIC,rag_worker;
CREATE CONSTRAINT TRIGGER trg_stage_commit_deadline AFTER INSERT OR UPDATE ON rag.staged_outputs
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION rag.commit_stage_deadline();

CREATE FUNCTION rag.commit_publication_deadline()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,rag AS $$
DECLARE deadline timestamptz; session_deadline timestamptz; state varchar;
BEGIN
    -- Use the final persisted row: a superseded/revoked transient update is not activation.
    IF NOT EXISTS (SELECT 1 FROM rag.index_generations WHERE id=NEW.id AND is_active) THEN RETURN NULL; END IF;
    SELECT leased_until,status INTO deadline,state FROM rag.job_leases
        WHERE job_id=NEW.job_id FOR UPDATE;
    SELECT session_until INTO session_deadline FROM rag.worker_grants WHERE job_id=NEW.job_id;
    IF deadline IS NULL OR deadline<=clock_timestamp() OR state<>'COMPLETED'
        OR session_deadline IS NULL OR session_deadline<=clock_timestamp() THEN
        RAISE EXCEPTION 'Publication COMMIT deadline denied' USING ERRCODE='check_violation';
    END IF;
    RETURN NULL;
END $$;
REVOKE ALL ON FUNCTION rag.commit_publication_deadline() FROM PUBLIC,rag_worker;
CREATE CONSTRAINT TRIGGER trg_publication_commit_deadline AFTER INSERT OR UPDATE ON rag.index_generations
    DEFERRABLE INITIALLY DEFERRED FOR EACH ROW EXECUTE FUNCTION rag.commit_publication_deadline();

-- Non-owner Java runtime must remain non-elevated even when provisioned earlier.
ALTER ROLE rag_runtime NOSUPERUSER NOCREATEDB NOCREATEROLE NOINHERIT NOBYPASSRLS NOREPLICATION;
DO $$
DECLARE membership record;
BEGIN
    FOR membership IN SELECT r.rolname FROM pg_catalog.pg_auth_members m JOIN pg_catalog.pg_roles r ON r.oid=m.roleid
        WHERE m.member=(SELECT oid FROM pg_catalog.pg_roles WHERE rolname='rag_runtime')
    LOOP EXECUTE format('REVOKE %I FROM rag_runtime',membership.rolname); END LOOP;
END $$;

