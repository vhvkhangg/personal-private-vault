-- Fresh runtime identity is required by both Java and restricted SQL worker authority.
CREATE TABLE rag.runtime_state(singleton boolean PRIMARY KEY DEFAULT true CHECK(singleton),current_instance UUID NOT NULL);
GRANT SELECT,INSERT,UPDATE ON rag.runtime_state TO rag_runtime;
REVOKE ALL ON rag.runtime_state FROM PUBLIC,rag_worker;

CREATE FUNCTION rag.require_current_consent()
RETURNS trigger LANGUAGE plpgsql SECURITY DEFINER SET search_path=pg_catalog,rag AS $$
DECLARE node jsonb; lease rag.job_leases%ROWTYPE; current_id uuid;
BEGIN
    IF NOT NEW.worker_receipt THEN RETURN NEW; END IF;
    SELECT current_instance INTO current_id FROM rag.runtime_state WHERE singleton;
    SELECT * INTO lease FROM rag.job_leases WHERE job_id=NEW.job_id AND attempt_id=NEW.attempt_id FOR UPDATE;
    IF current_id IS NULL THEN RAISE EXCEPTION 'Runtime authority unavailable' USING ERRCODE='check_violation'; END IF;
    FOR node IN SELECT * FROM jsonb_array_elements(lease.dependency_manifest::jsonb->'nodes') LOOP
        IF NOT EXISTS (SELECT 1 FROM rag.sources WHERE id=(node->>'source_id')::bigint AND approval_instance=current_id) THEN
            RAISE EXCEPTION 'Restored or stale consent denied' USING ERRCODE='check_violation'; END IF;
    END LOOP;
    RETURN NEW;
END $$;
REVOKE ALL ON FUNCTION rag.require_current_consent() FROM PUBLIC,rag_worker;
CREATE TRIGGER trg_current_worker_consent BEFORE INSERT OR UPDATE ON rag.staged_outputs
    FOR EACH ROW EXECUTE FUNCTION rag.require_current_consent();

