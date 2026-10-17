-- PostgreSQL SQL-route regressions, not native-process or HTTP proof.
-- Execute whole script on an isolated V15 database as migration test authority.
BEGIN;
SET LOCAL statement_timeout='5s';
SET LOCAL lock_timeout='1s';
CREATE FUNCTION pg_temp.expect_denied(command text,expected_state text) RETURNS void LANGUAGE plpgsql AS $$
BEGIN
    BEGIN EXECUTE command;
    EXCEPTION WHEN OTHERS THEN IF SQLSTATE=expected_state THEN RETURN; END IF; RAISE; END;
    RAISE EXCEPTION 'Expected denial did not occur: %',expected_state;
END $$;
CREATE FUNCTION pg_temp.expect_true(value boolean,label text) RETURNS void LANGUAGE plpgsql AS $$
BEGIN IF value IS DISTINCT FROM true THEN RAISE EXCEPTION 'Assertion failed: %',label; END IF; END $$;
INSERT INTO public.vault_entries(id,entry_type) VALUES(910001,'FILM');
INSERT INTO public.films(id,title,format,production_style) VALUES(910001,'Synthetic single job','MOVIE','ANIMATION');
INSERT INTO rag.sources(source_type,source_id,classification,access_state,source_revision_hash,description_revision_hash,
    approved_fields_text,approved_owner_reset_version,created_at,updated_at)
    VALUES('FILM','910001','NON_PRIVATE','APPROVED',repeat('a',64),'','title',1,clock_timestamp(),clock_timestamp());
INSERT INTO rag.runtime_state(singleton,current_instance) VALUES(true,'00000000-0000-0000-0000-000000009100')
    ON CONFLICT(singleton) DO UPDATE SET current_instance=excluded.current_instance;
CREATE TEMP TABLE sql_worker_fixture AS
    SELECT jsonb_build_object('contract_version','2.0','root_id',r.id,'nodes',jsonb_build_array(jsonb_build_object(
        'source_id',r.id,'source_type','FILM','domain_id','910001','epoch',r.authorization_epoch,
        'field_version',r.field_policy_version,'fields',jsonb_build_array('title'),'content_revision',repeat('a',64),
        'description_revision','','owner_source_version',s.source_version,'owner_reset_version',s.consent_reset_version)),
        'edges','[]'::jsonb)::text AS manifest,
        r.id AS source_id,repeat('b',64)::text AS input_digest,repeat('c',64)::text AS output_digest
    FROM rag.sources r JOIN public.vault_rag_source_state s ON r.source_id=s.source_id::text
    WHERE r.source_type='FILM' AND r.source_id='910001';
ALTER TABLE pg_temp.sql_worker_fixture ADD COLUMN scope_digest text;
ALTER TABLE pg_temp.sql_worker_fixture ADD COLUMN receipt text;
UPDATE pg_temp.sql_worker_fixture SET scope_digest=encode(sha256(convert_to(manifest,'UTF8')),'hex');
UPDATE pg_temp.sql_worker_fixture SET receipt=jsonb_build_object('contract_version','2.0','status','STAGED',
    'job_id','00000000-0000-0000-0000-000000000101','attempt_id','00000000-0000-0000-0000-000000000102',
    'stage_type','SYNTHETIC_TEST','input_digest',input_digest,'scope_digest',scope_digest,
    'output_digest',output_digest,'output_kind','SYNTHETIC_DIGEST')::text;
GRANT SELECT ON pg_temp.sql_worker_fixture TO rag_worker;
SELECT public.vault_rag_reserve(ARRAY[910001]::bigint[],'00000000-0000-0000-0000-000000000102');
INSERT INTO rag.job_leases(job_id,attempt_id,source_id,status,lease_token,leased_until,created_at,updated_at,
    reservation_token,controller_identity,worker_identity,dependency_manifest)
    SELECT '00000000-0000-0000-0000-000000000101','00000000-0000-0000-0000-000000000102',source_id,'ACTIVE',
        repeat('d',64),clock_timestamp()+interval '5 minutes',clock_timestamp(),clock_timestamp(),
        '00000000-0000-0000-0000-000000000102',
        jsonb_build_object('process_id',42,'started_at',clock_timestamp()::text,'runtime_id','00000000-0000-0000-0000-000000009100'),
        jsonb_build_object('process_id',43,'started_at',clock_timestamp()::text,'endpoint_nonce','00000000-0000-0000-0000-000000009101'),manifest
    FROM pg_temp.sql_worker_fixture;
INSERT INTO rag.worker_grants(job_id,attempt_id,session_id,session_until,input_digest,scope_digest,output_digest,stage_type)
    SELECT '00000000-0000-0000-0000-000000000101','00000000-0000-0000-0000-000000000102',
        '00000000-0000-0000-0000-000000000103',clock_timestamp()+interval '5 minutes',input_digest,scope_digest,output_digest,'SYNTHETIC_TEST'
    FROM pg_temp.sql_worker_fixture;

-- Every nonterminal status, independent of its expired work deadline, retains capacity.
DO $$ DECLARE state text;
BEGIN
    FOREACH state IN ARRAY ARRAY['ADMITTED','STARTING','ACTIVE','STOPPING','RECOVERY_REQUIRED','FINALIZING'] LOOP
        UPDATE rag.job_leases SET status=state WHERE attempt_id='00000000-0000-0000-0000-000000000102';
        PERFORM pg_temp.expect_denied(format('INSERT INTO rag.job_leases(job_id,attempt_id,source_id,status,lease_token,leased_until,created_at,updated_at,reservation_token)
            SELECT gen_random_uuid(),gen_random_uuid(),source_id,%L,''other'',clock_timestamp()-interval ''1 hour'',clock_timestamp(),clock_timestamp(),gen_random_uuid()
            FROM pg_temp.sql_worker_fixture',state),'23505');
    END LOOP;
END $$;
UPDATE rag.job_leases SET status='ACTIVE' WHERE attempt_id='00000000-0000-0000-0000-000000000102';
SELECT pg_temp.expect_denied('DELETE FROM rag.job_leases WHERE attempt_id=''00000000-0000-0000-0000-000000000102''','23514');
SELECT pg_temp.expect_denied('UPDATE rag.job_leases SET status=''ABORTED'' WHERE attempt_id=''00000000-0000-0000-0000-000000000102''','23514');
SELECT pg_temp.expect_denied('UPDATE rag.job_leases SET dependency_manifest=''{}'' WHERE attempt_id=''00000000-0000-0000-0000-000000000102''','23514');
SELECT pg_temp.expect_denied('SELECT rag.claim_worker_input(''00000000-0000-0000-0000-000000000101'',''00000000-0000-0000-0000-000000000102'',repeat(''d'',64),''00000000-0000-0000-0000-000000000103'')','42501');

-- Change the actual SQL session authorization, not merely a mocked user name.
-- Native/HTTP tests must separately use the restricted authenticated connection.
SET LOCAL SESSION AUTHORIZATION rag_worker;
SELECT pg_temp.expect_denied('SELECT rag.verify_worker_grant(''00000000-0000-0000-0000-000000000101'',''00000000-0000-0000-0000-000000000102'',repeat(''d'',64),''SYNTHETIC_TEST'',input_digest::varchar,scope_digest::varchar) FROM pg_temp.sql_worker_fixture','23514');
SELECT pg_temp.expect_denied('SELECT rag.claim_worker_input(''00000000-0000-0000-0000-000000000101'',''00000000-0000-0000-0000-000000000102'',repeat(''d'',64),''00000000-0000-0000-0000-000000000104'')','23514');
SELECT pg_temp.expect_true(rag.claim_worker_input('00000000-0000-0000-0000-000000000101','00000000-0000-0000-0000-000000000102',repeat('d',64),'00000000-0000-0000-0000-000000000103'),'first claim succeeds');
SELECT pg_temp.expect_denied('SELECT rag.claim_worker_input(''00000000-0000-0000-0000-000000000101'',''00000000-0000-0000-0000-000000000102'',repeat(''d'',64),''00000000-0000-0000-0000-000000000103'')','23514');
SELECT pg_temp.expect_true(rag.verify_worker_grant('00000000-0000-0000-0000-000000000101','00000000-0000-0000-0000-000000000102',repeat('d',64),'SYNTHETIC_TEST',input_digest::varchar,scope_digest::varchar),'claimed exact grant verifies') FROM pg_temp.sql_worker_fixture;
SELECT pg_temp.expect_denied('SELECT rag.check_worker_attempt(''00000000-0000-0000-0000-000000000101'',''00000000-0000-0000-0000-000000000102'',repeat(''d'',64),true)','42501');
SELECT pg_temp.expect_denied('UPDATE rag.worker_grants SET consumed_at=NULL','42501');
SELECT pg_temp.expect_denied('UPDATE rag.job_leases SET status=''COMPLETED'',staging_closed=true','42501');
SELECT pg_temp.expect_denied('SELECT public.vault_rag_release(ARRAY[910001]::bigint[],''00000000-0000-0000-0000-000000000102'')','42501');
SELECT pg_temp.expect_denied('SELECT rag.stage_worker_output(''00000000-0000-0000-0000-000000000101'',''00000000-0000-0000-0000-000000000102'',repeat(''d'',64),''CHUNK'',''{"stage_type":"CHUNK"}'')','23514');
SELECT rag.stage_worker_receipt('00000000-0000-0000-0000-000000000101','00000000-0000-0000-0000-000000000102',repeat('d',64),receipt) FROM pg_temp.sql_worker_fixture;
SELECT rag.stage_worker_output('00000000-0000-0000-0000-000000000101','00000000-0000-0000-0000-000000000102',repeat('d',64),'SYNTHETIC_TEST',receipt) FROM pg_temp.sql_worker_fixture;
SET CONSTRAINTS rag.trg_stage_commit_deadline IMMEDIATE;
RESET SESSION AUTHORIZATION;
SELECT pg_temp.expect_true((SELECT count(*)=1 FROM rag.staged_outputs WHERE attempt_id='00000000-0000-0000-0000-000000000102'),'legacy receipt retries are idempotent');
UPDATE rag.job_leases SET status='STOPPING',staging_closed=true WHERE attempt_id='00000000-0000-0000-0000-000000000102';
SET LOCAL SESSION AUTHORIZATION rag_worker;
SELECT pg_temp.expect_denied('SELECT rag.stage_worker_receipt(''00000000-0000-0000-0000-000000000101'',''00000000-0000-0000-0000-000000000102'',repeat(''d'',64),receipt) FROM pg_temp.sql_worker_fixture','23514');
RESET SESSION AUTHORIZATION;
SELECT pg_temp.expect_denied('UPDATE rag.job_leases SET staging_closed=false WHERE attempt_id=''00000000-0000-0000-0000-000000000102''','23514');
UPDATE rag.job_leases SET status='ABORTED' WHERE attempt_id='00000000-0000-0000-0000-000000000102';
SELECT public.vault_rag_release(ARRAY[910001]::bigint[],'00000000-0000-0000-0000-000000000102');
SELECT pg_temp.expect_true((SELECT reserved_by IS NULL FROM public.vault_rag_source_state WHERE source_id=910001),'safe negative finish releases exact reservation');
SELECT 'SINGLE_JOB_STAGING_SQL_ASSERTIONS_PASSED' AS result;
ROLLBACK;
