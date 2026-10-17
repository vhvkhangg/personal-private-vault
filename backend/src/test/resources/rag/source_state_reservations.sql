-- Executable PostgreSQL boundary regressions. Run with psql ON_ERROR_STOP=1
-- against an isolated migrated database. All fixture changes are rolled back.
BEGIN;
SET LOCAL statement_timeout='5s';
SET LOCAL lock_timeout='1s';
CREATE FUNCTION pg_temp.expect_denied(command text,expected_state text) RETURNS void
LANGUAGE plpgsql AS $$
BEGIN
    BEGIN EXECUTE command;
    EXCEPTION WHEN OTHERS THEN
        IF SQLSTATE=expected_state THEN RETURN; END IF;
        RAISE;
    END;
    RAISE EXCEPTION 'Expected denial did not occur: %',expected_state;
END $$;
CREATE FUNCTION pg_temp.expect_true(value boolean,label text) RETURNS void
LANGUAGE plpgsql AS $$
BEGIN IF value IS DISTINCT FROM true THEN RAISE EXCEPTION 'Assertion failed: %',label; END IF; END $$;

INSERT INTO public.vault_entries(id,entry_type) VALUES
    (900001,'FILM'),(900002,'FILM'),(900003,'FILM'),(900004,'FILM'),(900005,'FILM'),
    (900010,'FICTION'),(900011,'FICTION'),(900100,'PERSON');
INSERT INTO public.persons(id,name) VALUES(900100,'Synthetic author');
INSERT INTO public.fiction_genres(id,name) VALUES(900001,'Synthetic genre');
INSERT INTO public.fictions(id,title,format,genre_id,author_person_id)
    VALUES(900010,'Synthetic fiction','NOVEL',900001,900100),
          (900011,'Synthetic fiction unreserved','NOVEL',900001,900100);
INSERT INTO public.films(id,title,format,production_style) VALUES
    (900001,'Synthetic A','MOVIE','ANIMATION'),(900002,'Synthetic B','MOVIE','ANIMATION'),
    (900003,'Synthetic C','MOVIE','ANIMATION'),(900004,'Synthetic D','MOVIE','ANIMATION'),(900005,'Synthetic E','MOVIE','ANIMATION');
INSERT INTO public.story_archetypes(id,name) VALUES(900001,'Synthetic story'),(900002,'Synthetic story 2');
INSERT INTO public.film_story_archetypes(film_id,story_archetype_id) VALUES(900001,900001);
INSERT INTO public.fiction_story_archetypes(fiction_id,story_archetype_id) VALUES(900010,900001);
INSERT INTO public.favorites(vault_entry_id) VALUES(900001),(900100);
CREATE TEMP TABLE observed_source_state AS SELECT * FROM public.vault_rag_source_state WHERE source_id IN (900001,900010);

-- Ordinary projected edits advance r; configuration-only edits do not reset or outdate.
UPDATE public.films SET title='Synthetic A revised' WHERE id=900001;
SELECT pg_temp.expect_true((SELECT s.source_version=o.source_version+1 AND s.consent_reset_version=o.consent_reset_version
    FROM public.vault_rag_source_state s JOIN observed_source_state o USING(source_id) WHERE s.source_id=900001),'ordinary edit advances source version only');
UPDATE public.films SET review='Excluded review' WHERE id=900001;
SELECT pg_temp.expect_true((SELECT s.source_version=o.source_version+1 FROM public.vault_rag_source_state s
    JOIN observed_source_state o USING(source_id) WHERE s.source_id=900001),'excluded field does not outdate an index');
UPDATE public.fictions SET title='Synthetic fiction revised' WHERE id=900010;
SELECT pg_temp.expect_true((SELECT s.source_version=o.source_version+1 FROM public.vault_rag_source_state s
    JOIN observed_source_state o USING(source_id) WHERE s.source_id=900010),'fiction owner advances source version');

INSERT INTO rag.sources(source_type,source_id,classification,access_state,source_revision_hash,description_revision_hash,created_at,updated_at)
    SELECT 'FILM',id::text,'NON_PRIVATE','DENIED','synthetic','synthetic',clock_timestamp(),clock_timestamp()
    FROM public.films WHERE id BETWEEN 900001 AND 900005;
UPDATE rag.sources SET access_state='APPROVED',approved_owner_reset_version=(
    SELECT consent_reset_version FROM public.vault_rag_source_state WHERE source_id=900001),authorization_epoch=authorization_epoch+1
    WHERE source_type='FILM' AND source_id='900001';
UPDATE public.films SET is_nsfw=true WHERE id=900001;
UPDATE public.films SET is_nsfw=false WHERE id=900001;
SELECT pg_temp.expect_true((SELECT s.consent_reset_version>r.approved_owner_reset_version
    FROM public.vault_rag_source_state s JOIN rag.sources r ON r.source_id=s.source_id::text
    WHERE s.source_id=900001 AND r.source_type='FILM'),'lost notifications cannot restore pre-NSFW consent');
UPDATE rag.sources SET classification='PRIVATE' WHERE source_type='FILM' AND source_id='900001';
UPDATE rag.sources SET classification='NON_PRIVATE' WHERE source_type='FILM' AND source_id='900001';
SELECT pg_temp.expect_true((SELECT access_state='REVOKED' AND approved_owner_reset_version IS NULL FROM rag.sources
    WHERE source_type='FILM' AND source_id='900001'),'Private restoration retains revocation');
SELECT pg_temp.expect_denied('UPDATE rag.sources SET access_state=''APPROVED'' WHERE source_type=''FILM'' AND source_id=''900001''','23514');

-- Exact complete token scope, all selected writes, and unrelated edit availability.
SELECT public.vault_rag_reserve(ARRAY[900001,900010]::bigint[],'00000000-0000-0000-0000-000000000001');
SELECT pg_temp.expect_denied('UPDATE public.films SET is_nsfw=true WHERE id=900001','P1701');
SELECT pg_temp.expect_denied('UPDATE public.fictions SET review=''excluded but busy'' WHERE id=900010','P1701');
SELECT pg_temp.expect_denied('UPDATE public.vault_entries SET deleted_at=clock_timestamp() WHERE id=900001','P1701');
SELECT pg_temp.expect_denied('DELETE FROM public.favorites WHERE vault_entry_id=900001','P1701');
SELECT pg_temp.expect_denied('DELETE FROM public.film_story_archetypes WHERE film_id=900001','P1701');
SELECT pg_temp.expect_denied('UPDATE public.fiction_story_archetypes SET fiction_id=900011 WHERE fiction_id=900010','P1701');
INSERT INTO public.fiction_story_archetypes(fiction_id,story_archetype_id) VALUES(900011,900002);
SELECT pg_temp.expect_denied('UPDATE public.fiction_story_archetypes SET fiction_id=900010 WHERE fiction_id=900011','P1701');
DELETE FROM public.fiction_story_archetypes WHERE fiction_id=900011;
SELECT pg_temp.expect_denied('INSERT INTO public.film_links(film_id,url) VALUES(900001,''synthetic'')','P1701');
SELECT pg_temp.expect_denied('UPDATE rag.sources SET classification=''PRIVATE'' WHERE source_type=''FILM'' AND source_id=''900001''','P1701');
SELECT pg_temp.expect_denied('SELECT public.vault_rag_release(ARRAY[900001]::bigint[],''00000000-0000-0000-0000-000000000001'')','23514');
SELECT pg_temp.expect_denied('SELECT public.vault_rag_release(ARRAY[900001,900010]::bigint[],''00000000-0000-0000-0000-000000000002'')','23514');
UPDATE public.films SET title='Unrelated edit succeeds' WHERE id=900002;
SELECT pg_temp.expect_true((SELECT count(*)=2 FROM public.vault_rag_source_state
    WHERE reserved_by='00000000-0000-0000-0000-000000000001'),'failed releases preserve the entire reservation');
SELECT pg_temp.expect_denied('UPDATE public.films SET review=''bulk must roll back'' WHERE id IN (900001,900002)','P1701');
SELECT pg_temp.expect_true((SELECT review IS NULL FROM public.films WHERE id=900002),'bulk conflict is atomic');
SELECT pg_temp.expect_denied('DELETE FROM public.story_archetypes WHERE id=900001','23503');
SELECT pg_temp.expect_true((SELECT count(*)=1 FROM public.story_archetypes WHERE id=900001),'existing reference RESTRICT remains effective');
SELECT public.vault_rag_release(ARRAY[900001,900010]::bigint[],'00000000-0000-0000-0000-000000000001');

-- Scope history changes on edge removal/readdition, including affected ancestors.
INSERT INTO rag.dependencies(parent_source_id,child_source_id,created_at)
    SELECT p.id,c.id,clock_timestamp() FROM rag.sources p CROSS JOIN rag.sources c
    WHERE p.source_type='FILM' AND c.source_type='FILM' AND p.source_id='900003' AND c.source_id='900001';
CREATE TEMP TABLE observed_scope_state AS SELECT * FROM public.vault_rag_source_state WHERE source_id IN (900001,900003);
INSERT INTO rag.dependencies(parent_source_id,child_source_id,created_at)
    SELECT p.id,c.id,clock_timestamp() FROM rag.sources p CROSS JOIN rag.sources c
    WHERE p.source_type='FILM' AND c.source_type='FILM' AND p.source_id='900001' AND c.source_id='900002';
DELETE FROM rag.dependencies WHERE parent_source_id=(SELECT id FROM rag.sources WHERE source_type='FILM' AND source_id='900001');
INSERT INTO rag.dependencies(parent_source_id,child_source_id,created_at)
    SELECT p.id,c.id,clock_timestamp() FROM rag.sources p CROSS JOIN rag.sources c
    WHERE p.source_type='FILM' AND c.source_type='FILM' AND p.source_id='900001' AND c.source_id='900002';
SELECT pg_temp.expect_true((SELECT count(*)=2 FROM public.vault_rag_source_state s JOIN observed_scope_state o USING(source_id)
    WHERE s.source_version=o.source_version+3 AND s.consent_reset_version=o.consent_reset_version),'edge round trip permanently stales all affected ancestor manifests');
SELECT public.vault_rag_reserve(ARRAY[900003]::bigint[],'00000000-0000-0000-0000-000000000003');
SELECT pg_temp.expect_denied('DELETE FROM rag.dependencies WHERE parent_source_id=(SELECT id FROM rag.sources WHERE source_type=''FILM'' AND source_id=''900001'')','P1701');
SELECT pg_temp.expect_denied('DELETE FROM rag.sources WHERE source_type=''FILM'' AND source_id=''900002''','23514');
SELECT pg_temp.expect_true((SELECT count(*)=1 FROM rag.sources WHERE source_type='FILM' AND source_id='900002'),'retained consent history blocks destructive catalog cascade');
SELECT public.vault_rag_release(ARRAY[900003]::bigint[],'00000000-0000-0000-0000-000000000003');

-- Retained deletion identity, missing history and unaffected non-RAG cascades.
UPDATE public.vault_entries SET deleted_at=clock_timestamp() WHERE id=900004;
UPDATE public.vault_entries SET deleted_at=NULL WHERE id=900004;
SELECT pg_temp.expect_true((SELECT consent_reset_version=source_version AND NOT deleted FROM public.vault_rag_source_state
    WHERE source_id=900004),'trash and restore each reset consent history');
DELETE FROM public.films WHERE id=900005;
DELETE FROM public.vault_entries WHERE id=900005;
SELECT pg_temp.expect_true((SELECT deleted AND consent_reset_version=source_version FROM public.vault_rag_source_state
    WHERE source_id=900005),'hard-delete tombstone remains');
SELECT pg_temp.expect_denied('INSERT INTO public.vault_entries(id,entry_type) VALUES(900005,''FILM'')','23505');
DELETE FROM public.vault_rag_source_state WHERE source_id=900004;
SELECT pg_temp.expect_denied('UPDATE public.films SET title=''must deny missing metadata'' WHERE id=900004','23514');
DELETE FROM public.fiction_story_archetypes WHERE fiction_id IN (900010,900011);
DELETE FROM public.fictions WHERE id IN (900010,900011);
DELETE FROM public.persons WHERE id=900100;
DELETE FROM public.favorites WHERE vault_entry_id=900100;
DELETE FROM public.vault_entries WHERE id IN (900100,900011);
SELECT pg_temp.expect_true((SELECT count(*)=0 FROM public.favorites WHERE vault_entry_id=900100),'unrelated non-RAG metadata lifecycle is preserved');

-- Effective worker/runtime privileges: no table DML, private helper, source-state
-- function chain, trigger disable or TRUNCATE route.
SET LOCAL ROLE rag_worker;
SELECT pg_temp.expect_denied('SELECT * FROM public.vault_rag_source_state','42501');
SELECT pg_temp.expect_denied('SELECT public.vault_rag_reserve(ARRAY[900002]::bigint[],gen_random_uuid())','42501');
SELECT pg_temp.expect_denied('SELECT public.vault_rag_apply_change(ARRAY[900002]::bigint[],true,true,NULL)','42501');
RESET ROLE;
SET LOCAL ROLE rag_runtime;
SELECT pg_temp.expect_denied('UPDATE public.vault_rag_source_state SET reserved_by=NULL','42501');
SELECT pg_temp.expect_denied('DELETE FROM public.vault_rag_source_state','42501');
SELECT pg_temp.expect_denied('SELECT public.vault_rag_apply_change(ARRAY[900002]::bigint[],true,true,NULL)','42501');
SELECT pg_temp.expect_denied('ALTER TABLE public.films DISABLE TRIGGER ALL','42501');
SELECT pg_temp.expect_denied('TRUNCATE public.films CASCADE','42501');
RESET ROLE;
SELECT 'SOURCE_STATE_RESERVATION_SQL_ASSERTIONS_PASSED' AS result;
ROLLBACK;
