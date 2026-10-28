# Flyway Migrations

Backend Phase 1 introduces the initial executable PostgreSQL baseline:

`V1__create_schema_v1.sql`

It is derived from `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` (logical schema v1.3) and creates all 41 PostgreSQL enum types and 69 frozen tables plus DBML structural constraints, foreign keys, and indexes.

V1 contains no seed data. Cross-table business invariants whose exact SQL behavior is not fully specified remain application-owned or are deferred to later focused migrations instead of being guessed.

Once Phase 1 is frozen or V1 has been applied to non-disposable state, do not edit V1. Use append-only `V2__...` and later migrations.

## Phase 19 — Markdown Retrieval Readers (Option A / ADR-0025)

`V24__rag_markdown_retrieval_readers.sql`

Introduces single-reader durable exclusion (`rag.retrieval_readers`) and preregistered authority incident inspections (`rag.retrieval_inspections`), with short CAS entry points (`retrieval_reader_admit`, `retrieval_inspection_begin`, `retrieval_inspection_record`, `retrieval_reader_request_stop`, `retrieval_reader_begin_emission`, `retrieval_reader_finish`) and triggers guarding `document_chunks`, `generation_roots`, and `index_generations` against incompatible modification or purge while an active reader is admitted.

The takeover candidate adds canonical complete-scope/current-runtime checks at C_A/C_E, PostgreSQL scope digests, exact worker PID/creation-FILETIME binding, owned reader inspections, guarded observation, and empty-reader admission. All finish outcomes retain unresolved obligations. Exact observed tuple denial and terminal release share one physical transaction with affected-row checks; unknown stop/commit retains exclusion. Raw stale recovery cannot release active unknown readers. Runtime table DML remains revoked; only named SECURITY DEFINER entry points may mutate state. V1–V23 are unchanged. Disposable tests validate the V23 baseline before applying V24; this does not certify migration on an operational database or independently accept S19-1.

Corrective attempt 2 adds immutable `inspection_proof` and original-UUID reuse. The owned `retrieval_inspection_begin(UUID,UUID,VARCHAR,VARCHAR,UUID,UUID,BIGINT,BIGINT,VARCHAR)` returns the actual UUID and checks runtime registration under the graph guard; the six-argument compatibility entry records unknown process ownership, which cannot authorize blind recovery. `retrieval_reader_inspection_begin` reuses an attached unresolved operation after exact token/runtime checks. Capacity stays 4,096, compacting only bounded CLEAN unassociated/terminal history.

`retrieval_inspection_blocks` and `external_file_inspection_blocks` supply exact revision (unknown revision conservatively) predicates to currentness and additive reservation, edit, generation/root/chunk and cleanup guards. Safe unapproved/unselected pending replacement preserves the original obligation. `retrieval_inspection_deny` handles positively classified standalone incidents using exact revision/hash/length CAS; reserved tuples remain for the reader finisher. Private `retrieval_revoke_inspected_generations` uses each generation's own contributing manifest, preserving unrelated siblings. Helper/business mutation privileges remain revoked from PUBLIC/worker; runtime receives only named trusted inspection entry points/read predicates. V24 remains an unapplied-to-operational-state S19-1 candidate, not a change to frozen migrations.
