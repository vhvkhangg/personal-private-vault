# Database

## Logical architecture baseline

`personal-private-vault-schema-v1-FROZEN-final.dbml` is the frozen logical Database Schema v1 (v1.3).
Do not edit it for implementation convenience.

## Executable schema baseline

Backend Phase 1 introduces `backend/src/main/resources/db/migration/V1__create_schema_v1.sql`.
Flyway is the executable PostgreSQL schema source of truth. V1 is derived from the frozen DBML and creates
all 41 enum types and all 69 tables plus structural constraints, foreign keys, and DBML indexes. It also
includes clearly specified PostgreSQL-only case-insensitive uniqueness and the active-self partial unique
index from the frozen implementation notes.

Cross-table/business invariants whose exact SQL semantics are not yet fully specified remain in the owning
application module or a later focused migration rather than being guessed in V1.

## Migration discipline

- Never use Hibernate auto-DDL to evolve the schema.
- Do not put real personal data or secrets in migrations.
- After V1 is frozen/applied to durable state, do not edit it; use `V2__...` and later migrations.
- V2 supplies the accepted search-specific index delta; search ownership remains unchanged.

## Phase 17 candidate migration inventory

V1 remains frozen. V2 is the accepted search-index migration. The existing unaccepted Phase 17 candidate contains the following historical RAG schema/functions/roles. They are not the approved Alternative B target or proof of its guarantees:

| Versions | Candidate purpose |
| --- | --- |
| V3–V6 | Foundation schema, attempt/staging fences, privilege and aggregate stage limits |
| V7 | Issued worker grants, strict durable receipts, restricted worker and separate Java runtime roles |
| V8 | Deferred deadline checks and runtime-bound consent in the prior candidate; no physical-instant deadline proof; target amended by ADR-0020 |
| V9 | Prior runtime authority/startup invalidation, superseded by persistent consent/completed-index target |
| V10 | Complete worker authority, one verified receipt, policy constraints |
| V11 | Disabled external-source tombstones and hardened role audit |
| V12 | Exact dependency identity/topology proof and receipt-writer provenance fence |
| V13 | Effective database CREATE/TEMP denial, including PUBLIC's inherited TEMP privilege |

See [setup](../../backend/dev/rag-db/README.md) for separate migration/provisioning and the exact PostgreSQL 18.6 / pgvector 0.8.6 compatibility image. The compatibility test installs the extension in its disposable database and verifies casts/version; no vector feature/table is introduced in this foundation. Runtime Flyway is disabled for the isolated RAG profile. Business DBML, V1/V2 and authentication schema remain unchanged. This is an implementation candidate awaiting independent review, not a frozen schema acceptance.


## Approved Alternative B additive logical target — not deployed

[ADR-0020](../adr/0020-phase-17-single-job-reservations-e1.md) and
[Phase 17 additive DBML](phase-17-alternative-b.dbml) amend only the bounded
Phase 17 extension. Frozen Schema v1 and V1–V13 are unchanged. No migration runs
in contract synchronization; separately authorized implementation appends after V13.

- Vault owns retained public.vault_rag_source_state (stable owner ID/type/version/
  reset/deleted/reservation). No deletion cascade/history reset/reverse RAG FK.
- Mandatory owner-local guard/version triggers cover all maintained owner/policy/
  dependency write routes with NOWAIT earlier-lock fallback and atomic rollback.
- RAG reuses existing sources/jobs/grants/staging/dependencies/generations/runs;
  exact owner reset binding and description confirmation, static complete
  nonterminal unique constraint, process identities, one-use claim and attempt seal.
- E1 gate checks replace claimed strict physical-C deadlines; completed CURRENT
  index/consent is independent of work TTL/service boot. Atomic P-or-abort/seal/
  terminal/reservation release; unknown retains job row/reservations, never retention
  eviction. Consent/config and historical job/index freshness remain separate.
- No global F/V/H/DRAIN/ticket tables or worker-to-Vault SQL. Actual fixed-search_path
  roles/triggers/grants/COMMITs and restored-history seeding require real PG tests.

DBML is a canonical logical extension/inventory, not executable DDL or a claim
that newly listed fields/APIs exist. Exact physical reuse/key/index/trigger mapping
must be reviewed with the future append-only migration. No schema acceptance here.

## Phase 18 D18-A additive logical target — preparation, not executable

[Phase 18 document-index DBML](phase-18-document-indexing.dbml), [ADR-0021](../adr/0021-phase-18-bounded-multi-file-verified-indexing.md) and [typed API inventory](../architecture/rag-phase-18-api-extension.md) record owner-approved *proposed* RAG-owned external source, multi-root job outcome, chunk/vector and per-generation manifest responsibilities. The historical *candidate* V3–V13 list above reflects its original preparation time, **not** the present Flyway tip. The Phase 17 foundation reached **V17**; only **V18 or later append-only migrations** may implement Phase 18 after independent gates and an active handoff. V1–V17, Schema v1, frozen Phase 17 DBML/ADR and deployed roles remain unchanged by this documentation.
