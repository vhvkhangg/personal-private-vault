-- Actual PostgreSQL policy-history regressions. Roll back every synthetic fixture.
BEGIN;
SET LOCAL statement_timeout='5s';
CREATE FUNCTION pg_temp.expect_denied(command text,expected_state text) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    BEGIN EXECUTE command;
    EXCEPTION WHEN OTHERS THEN IF SQLSTATE=expected_state THEN RETURN; END IF; RAISE; END;
    RAISE EXCEPTION 'Expected denial did not occur: %',expected_state;
END $$;
CREATE FUNCTION pg_temp.expect_true(value boolean,label text) RETURNS void LANGUAGE plpgsql AS $$
BEGIN IF value IS DISTINCT FROM true THEN RAISE EXCEPTION 'Assertion failed: %',label; END IF; END $$;
INSERT INTO public.vault_entries(id,entry_type) VALUES(930001,'FILM');
INSERT INTO public.films(id,title,format,production_style) VALUES(930001,'Synthetic consent history','MOVIE','ANIMATION');
INSERT INTO rag.sources(source_type,source_id,classification,access_state,source_revision_hash,description_revision_hash,
    approved_fields_text,approved_owner_reset_version,created_at,updated_at)
    VALUES('FILM','930001','NON_PRIVATE','APPROVED','synthetic','synthetic','title',1,clock_timestamp(),clock_timestamp());
CREATE TEMP TABLE previous_approval AS SELECT authorization_epoch,field_policy_version FROM rag.sources WHERE source_id='930001' AND source_type='FILM';
UPDATE rag.sources SET access_state='REVOKED' WHERE source_id='930001' AND source_type='FILM';
SELECT pg_temp.expect_true((SELECT r.authorization_epoch=p.authorization_epoch+1 AND r.field_policy_version=p.field_policy_version
    AND r.approved_owner_reset_version IS NULL AND r.approved_fields_text='title'
    FROM rag.sources r CROSS JOIN previous_approval p WHERE r.source_id='930001' AND r.source_type='FILM'),'raw SQL revoke advances history and preserves settings');
WITH stale_approval AS (UPDATE rag.sources SET access_state='APPROVED',approved_owner_reset_version=1
    WHERE source_id='930001' AND source_type='FILM' AND authorization_epoch=(SELECT authorization_epoch FROM previous_approval)
    AND field_policy_version=(SELECT field_policy_version FROM previous_approval) RETURNING id)
    SELECT pg_temp.expect_true((SELECT count(*)=0 FROM stale_approval),'delayed approval CAS cannot cross direct SQL revocation');
UPDATE rag.sources SET access_state='DENIED' WHERE source_id='930001' AND source_type='FILM';
SELECT pg_temp.expect_true((SELECT r.authorization_epoch=p.authorization_epoch+2 AND r.approved_owner_reset_version IS NULL
    FROM rag.sources r CROSS JOIN previous_approval p WHERE r.source_id='930001' AND r.source_type='FILM'),'every access-state intent change invalidates stale requests');
SELECT pg_temp.expect_denied('DELETE FROM rag.sources WHERE source_id=''930001'' AND source_type=''FILM''','23514');
SELECT pg_temp.expect_true((SELECT count(*)=1 FROM rag.sources WHERE source_id='930001' AND source_type='FILM'),'catalog identity and consent configuration retained');
SET LOCAL ROLE rag_runtime;
SELECT pg_temp.expect_denied('DELETE FROM rag.sources WHERE source_id=''930001'' AND source_type=''FILM''','42501');
RESET ROLE;
SELECT pg_temp.expect_true((SELECT r.authorization_epoch=p.authorization_epoch+2 FROM rag.sources r CROSS JOIN previous_approval p
    WHERE r.source_id='930001' AND r.source_type='FILM'),'denied deletion and denied stale CAS do not mutate history');
SELECT 'RETAINED_CONSENT_HISTORY_SQL_ASSERTIONS_PASSED' AS result;
ROLLBACK;
