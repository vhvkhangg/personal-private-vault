-- Disabled external capability retains only source-free tombstones, never legacy paths/identity.
UPDATE rag.sources SET source_id='disabled:'||id::text,file_identity=NULL,source_revision_hash='DISABLED',
    description_revision_hash='DISABLED',approved_fields_text='',classification='UNKNOWN',access_state='DENIED',
    authorization_epoch=authorization_epoch+1 WHERE source_type='EXTERNAL_FILE';
ALTER TABLE rag.job_leases ADD CONSTRAINT ck_rag_job_state
    CHECK(status IN ('ADMITTED','ACTIVE','COMPLETED','REVOKED','CANCELLED','PURGED'));
-- Existing privileged worker ownership must be repaired explicitly by the operator.
DO $$
BEGIN
    IF EXISTS(SELECT 1 FROM pg_catalog.pg_database WHERE datdba=(SELECT oid FROM pg_catalog.pg_roles WHERE rolname='rag_worker'))
        OR EXISTS(SELECT 1 FROM pg_catalog.pg_type WHERE typowner=(SELECT oid FROM pg_catalog.pg_roles WHERE rolname='rag_worker'))
        OR has_database_privilege('rag_worker',current_database(),'CREATE') THEN
        RAISE EXCEPTION 'Unsafe worker database/type privileges require operator repair';
    END IF;
END $$;

