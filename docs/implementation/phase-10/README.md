# Backend Phase 10 — Feed + ImportData Workflows

Status: **COMPLETE — FROZEN (2026-10-01)**

Current review: [`reviews/2026-10-01-phase-10-final-codex-acceptance.md`](reviews/2026-10-01-phase-10-final-codex-acceptance.md).
Preparation was owner committed/pushed as `0e530f2`. F1–F7 are closed; independent full verification passed
685 tests with zero failures/errors/skips. The owner subsequently committed/pushed the accepted Phase 10
implementation. The completed handoff is archived in [`handoff.md`](handoff.md).

Phase 10 implements two top-level Spring Modulith modules:

- `feed`
- `importdata`

Both modules use only public `vault` + parent `knowledge` contracts. Neither may access nested Knowledge modules or
their repositories.

Phase 10 implementation is complete, final-reviewed, owner committed/pushed, and frozen.

The preparation and implementation contracts below remain as historical records. Future Feed/ImportData changes
require a new owner-approved feature or maintenance slice.

Phase 10 is **not** a milestone phase. The next milestone remains after Phase 12.

## Owned Schema v1 tables

### Feed

- `feed_sources`
- `feed_items`
- `saved_resources`
- `saved_resource_conversions`

### ImportData

- `import_jobs`
- `import_job_items`

No DBML/Flyway/schema change is planned.

## Frozen top-level dependency direction

```text
feed       -> vault, knowledge
importdata -> vault, knowledge
```

Rules:

- no direct dependency on `knowledge.study`, `knowledge.information`, `knowledge.vocabulary`, or `knowledge.note`;
- no injection of Knowledge repositories/entities;
- Knowledge writes/reads go through parent `KnowledgeOperations` and parent-owned Knowledge API types;
- Feed/ImportData own their own persistence and transaction boundaries.

## Shared JSON snapshot rule

Phase 10 introduces JSONB values in:

- `feed_sources.config`;
- `feed_items.raw_metadata`;
- `saved_resources.raw_metadata`;
- `import_job_items.parsed_payload`.

The managed-state isolation defect already fixed for Note must not be recreated here.

For every JSONB command/entity/view boundary:

- caller-owned maps/lists must not remain aliased to managed persistence state;
- public views must expose deep immutable or equivalently isolated snapshots;
- nested maps/lists, JSON null, strings, booleans, and ordinary integral/decimal numbers must round-trip;
- cyclic, unsupported, or invalid-key graphs must fail with stable owner-domain validation without logging payloads;
- do not expose a mutable managed JSON graph through a record accessor;
- keep JSON policy owner-local; do not introduce a repository-wide generic JSON base framework.

Feed and ImportData may each use a small owner-local snapshot helper. Do not import the Knowledge Note helper merely
to reuse an implementation across forbidden module boundaries.

# Feed

## Public capability direction

Use semantic public packages/named interfaces such as:

```text
feed/
├── source/
├── item/
├── resource/
├── conversion/
├── view/
├── enums/
└── internal/
```

Exact package names may be refined by the handoff, but no JPA entity/repository may escape `internal`.

## Feed source contract

`feed_sources` is not Vault-backed.

Required create/update fields:

- nonblank `name`, max 255;
- required type:
  - `GITHUB_TRENDING`
  - `HACKER_NEWS`
  - `REDDIT`
  - `RSS`
  - `WEBSITE`

Optional:

- `source_url`;
- `feed_url`;
- JSON `config`.

Scheduling state:

- `enabled` defaults to `true` when null/omitted on create/update;
- `scheduled_refresh_enabled` defaults to `false` when null/omitted on create/update;
- `refresh_interval_minutes`, when present, must be `> 0`;
- scheduled refresh enabled requires a non-null interval;
- scheduled refresh disabled may still retain a positive interval because Schema v1 permits it;
- `last_fetched_at` and `next_fetch_at` are service-managed, not arbitrary caller replacement fields.

When schedule settings change:

- disabling scheduled refresh clears `next_fetch_at`;
- enabling schedule with no prior successful fetch leaves `next_fetch_at = null`, meaning due immediately;
- if a prior `last_fetched_at` exists, recompute `next_fetch_at = last_fetched_at + refresh_interval_minutes`.

Required reads:

- source by ID;
- due scheduled sources by `cutoff` + positive `limit`.

Due predicate:

```text
enabled = true
AND scheduled_refresh_enabled = true
AND (next_fetch_at IS NULL OR next_fetch_at <= cutoff)
```

Order due sources:

1. null `next_fetch_at` first;
2. non-null `next_fetch_at ASC`;
3. `id ASC`.

The due read is side-effect free.

Do not invent type-specific URL requirements that are absent from frozen Schema v1.

## Feed fetch/ingestion boundary

Phase 10 does **not** implement live GitHub/Hacker News/Reddit/RSS/website HTTP clients or an `@Scheduled` runtime.

Instead, expose an adapter-ready application capability that accepts one normalized successful fetch result:

```text
ingestFetch(sourceId, fetchedAt, normalizedItems)
```

This keeps source-specific network payloads out of stable domain contracts and lets later infrastructure adapters/manual
refresh triggers call the same use case.

Rules:

- source must exist and be enabled;
- `fetchedAt` is required;
- the whole normalized batch + source fetch timestamps commit atomically;
- after a successful fetch:
  - `last_fetched_at = fetchedAt`;
  - if scheduling is enabled, `next_fetch_at = fetchedAt + refresh_interval_minutes`;
  - otherwise `next_fetch_at = null`;
- a failed batch must not advance source fetch timestamps.

## Feed item contract

A normalized feed item requires:

- nonblank title, max 1000;
- nonblank URL, max 2048;
- optional external ID, author, summary, published time, raw metadata.

`url_hash` is computed internally as lowercase SHA-256 hex of the **trimmed URL string exactly as stored**, encoded
as UTF-8. Phase 10 does not invent URL canonicalization beyond trimming.

Frozen uniqueness per source:

```text
(feed_source_id, external_id) UNIQUE
(feed_source_id, url_hash) UNIQUE
```

`external_id` may be null; `url_hash` is always present.

Refresh ingestion uses update-or-insert semantics:

- if neither key matches an existing row, insert a new item;
- if exactly one key matches, update that row with the newest normalized metadata/fetched time;
- if both keys match the same row, update that row;
- if external ID and URL hash resolve to **different** rows, reject the batch with a stable Feed-domain conflict;
- if `external_id` is null, URL hash is the identity key for that candidate;
- repeated identical candidates in the same batch must not create duplicates;
- conflicting same-key candidates inside one submitted batch must be rejected rather than first-wins/last-wins.

PostgreSQL uniqueness remains the final race arbiter. Expected conflicts must retain privacy-safe logging: do not log
private external IDs, URLs, hashes, or raw vendor detail.

Required reads:

- item by ID;
- recent items for one source with positive `limit`, ordered by `published_at DESC NULLS LAST`, then `id DESC`.

Do not add a global unbounded feed-item list.

## SavedResource contract

`saved_resources` is Vault-backed `SAVED_RESOURCE`: `saved_resources.id == vault_entries.id`.

Create the Vault Entry and SavedResource row in one transaction. Failure must leave no orphan Vault Entry.

Supported kinds:

- `ARTICLE`
- `REPOSITORY`
- `SOCIAL_POST`
- `WEB_PAGE`
- `OTHER`

Required:

- nonblank title;
- nonblank resource URL.

`resource_url_hash` is lowercase SHA-256 hex of the trimmed stored resource URL, UTF-8, with no extra URL
canonicalization.

The global unique `resource_url_hash` is a duplicate detector:

- duplicate sequential save is a stable `SavedResourceConflictException`, not silent reuse;
- concurrent duplicate saves have one winner and domain-conflict losers;
- the losing Vault creation must roll back;
- privacy-safe conflict logging applies to URL/hash values.

Provide two save paths:

1. save an existing Feed Item, copying its current provenance/metadata snapshot into the SavedResource;
2. save a manual/social/web resource without a Feed Item.

For Feed Item saves, `feed_item_id` is retained as provenance. A manual resource has it null.

Facebook/Instagram/social saves remain metadata/link-only; Phase 10 does not mirror source content.

Required reads:

- SavedResource by ID;
- optional single lookup by `resource_url_hash`;
- bounded recent SavedResources ordered `saved_at DESC, id DESC`.

Vault favorite/rating/tag/recycle behavior remains Vault-owned.

## SavedResource conversion contract

A SavedResource may be explicitly converted into:

- Study;
- Information;
- Note.

Vocabulary conversion is not supported by frozen `saved_resource_conversions` intent and is out of Phase 10.

Provide explicit Feed-owned conversion operations that internally call the parent `KnowledgeOperations` API. The
public Feed conversion API may accept **parent Knowledge API command types** because `feed -> knowledge` is a frozen
legal dependency; it must never accept nested Knowledge-module types.

Conversion transaction:

1. load the SavedResource;
2. call the relevant parent Knowledge create operation;
3. persist `(saved_resource_id, target_vault_entry_id, created_at)`;
4. commit target creation + provenance link atomically.

If provenance insertion fails, the created Knowledge/Vault target must roll back with the same transaction.

The same SavedResource may be converted multiple times into distinct target entries because frozen Schema v1 defines
no one-conversion-per-type rule.

Provide bounded conversion-history reads for one SavedResource, ordered `created_at DESC, target_vault_entry_id DESC`.

Do not invent Feed-to-Vocabulary conversion, automatic conversion, or an event solely to avoid the synchronous call.

# ImportData

## Public capability direction

Use semantic public packages/named interfaces such as:

```text
importdata/
├── job/
├── view/
├── enums/
└── internal/
    ├── application/
    ├── parsing/
    ├── domain/
    └── infrastructure/persistence/
```

ImportData owns orchestration/job state. Knowledge remains authoritative for target business validation/persistence.

## Import target + format matrix

Frozen targets:

- `STUDY`
- `INFORMATION`
- `VOCABULARY`
- `NOTE`

Frozen formats:

- `CSV`
- `JSON`
- `MARKDOWN`

Phase 10 supported matrix:

| Target | CSV | JSON | MARKDOWN |
| --- | --- | --- | --- |
| Study | yes | yes | no |
| Information | yes | yes | no |
| Vocabulary | yes | yes | no |
| Note | yes | yes | yes |

Reject unsupported target/format combinations before item persistence.

Text input is UTF-8. Phase 10 does not implement HTTP upload or object-storage I/O. `raw_file_object_key` is optional
metadata only; callers may provide it if some external upload layer already produced an object key.

## Import job creation

Create job requires:

- target type;
- format;
- nonblank original file name, max 500;
- optional raw file object key, max 1024.

Initial state:

```text
status = CREATED
all counts = 0
file_hash = null
```

The parser operation accepts raw text, computes lowercase SHA-256 hex of its exact UTF-8 bytes, and stores it in
`file_hash`.

## Job/item state model

Approved job progression:

```text
CREATED -> PARSED -> VALIDATED -> IMPORTED
   |          |          |
   +----------+----------+-> CANCELLED
```

`FAILED` is reserved for unrecoverable whole-job parse/orchestration failure before target-domain writes commit.

No state may move backward.

### Job-level transition serialization

Every **mutating** operation for one Import Job must share the same owner-local serialization guard:

```text
parse(jobId, ...)
validate(jobId)
execute(jobId, decisions)
cancel(jobId)
```

The preferred schema-compatible implementation is a pessimistic write lock on the existing `import_jobs` row.

Required guard semantics:

1. enter the caller's existing transaction;
2. acquire the job row guard before making any transition decision or target-domain write;
3. after the guard is acquired, obtain **fresh authoritative database state** for the job even if the entity was
   already loaded in the current persistence context;
4. recheck the allowed transition against that fresh state;
5. hold the guard until the complete transition transaction commits or rolls back.

A correct owner-local JPA implementation may use a `PESSIMISTIC_WRITE` repository query plus an explicit
`EntityManager.refresh(...)` under the same transaction, or an equivalently correct approach. Do not use
`REQUIRES_NEW`, a global persistence-context clear, retry framework, advisory-lock framework, or schema change merely
to serialize one job.

### Loser/repeat behavior

Phase 10 chooses **stable invalid-transition rejection**, not idempotent replay, for a caller that acquires the guard
and observes a status incompatible with the requested mutation.

Examples:

- a second `execute` that waits behind a successful execution rechecks `IMPORTED` and throws a stable
  ImportData-domain transition exception without any target write;
- `cancel` that waits behind successful execution rechecks `IMPORTED` and is rejected without overwriting the terminal
  state;
- `execute` that waits behind successful cancellation rechecks `CANCELLED` and is rejected without any target write;
- competing pre-import transitions recheck the fresh state and only the operation compatible with that state may
  continue.

The exception must report only safe metadata such as job ID, current status, and requested transition; never include
raw parsed payloads or import content.

Terminal states `IMPORTED`, `FAILED`, and `CANCELLED` are irreversible in Phase 10. No mutating operation may regress
a terminal job or silently execute the same transition twice.

### Guard ownership across failure

The same guard remains held through the whole transition:

- `parse` through item replacement/classification + job status update;
- `validate` through item reclassification/count recomputation + job status update;
- `execute` through **all** selected Knowledge target writes, item execution statuses/counts, and final `IMPORTED`
  status;
- `cancel` through the `CANCELLED` status write.

If `execute` fails because any selected Knowledge write fails, the whole transaction rolls back while the guard is
still owned. The previously committed job therefore remains `VALIDATED`; no partial target/item/job state commits.

If cancellation wins the guard first, execution cannot create/update Knowledge targets. If execution wins first and
commits, cancellation cannot overwrite `IMPORTED`.

### Parse

`parse(jobId, rawText)` is valid only from `CREATED`.

Parsing must not create/update final Knowledge records.

Persist one `import_job_items` row per logical item with deterministic zero-based `item_index`.

Because item status has no `PARSED` enum:

- structurally parseable/canonicalizable items receive provisional `VALID`;
- structurally malformed items receive `INVALID` plus a payload-free stable error message;
- job status becomes `PARSED`;
- counts are recomputed from stored item rows.

A catastrophic top-level parse failure that yields no meaningful item boundaries may set the job to `FAILED`
without creating target records.

### Validate / duplicate classification

`validate(jobId)` is valid only from `PARSED`.

Validation is **ImportData structural/integration validation**, not a copy of Knowledge business rules.

- keep structurally valid items as `VALID`;
- classify known target duplicates as `DUPLICATE` and set `duplicate_vault_entry_id`;
- invalid integration payloads become `INVALID` with safe error text;
- job status becomes `VALIDATED`;
- recompute `valid_items`, `duplicate_items`, `invalid_items`.

Target-domain validation remains authoritative inside parent `KnowledgeOperations` during execution.

### Duplicate detection available in Phase 10

Do not bypass the Knowledge parent boundary or add new frozen Knowledge query methods merely for import.

Phase 10 supports database duplicate detection where the frozen parent API already supports it:

- Note: `imported_file_hash` through `KnowledgeOperations.findNoteByImportedFileHash`.

For Study, Information, and Vocabulary, no approved natural-key lookup exists in the parent Knowledge API. Do not
invent repository access or silent natural-key uniqueness in ImportData. Structurally valid items therefore remain
`VALID` unless a future owner-approved public lookup strategy is added.

Within-file structural duplicates may be diagnosed by the parser where unambiguous, but must not create fake
cross-database duplicate IDs.

## Canonical parsed payloads

`parsed_payload` stores a target-specific canonical JSON object whose fields correspond to **parent Knowledge API
create/update command fields**, not nested-module command types.

Rules:

- unknown top-level CSV/JSON fields are validation errors rather than silently discarded;
- enum text is trimmed and parsed case-insensitively to the canonical enum value;
- IDs are numeric;
- timestamps use ISO-8601;
- blank text maps to null only where the target field is nullable;
- required target fields must be structurally present before an item is provisional `VALID`;
- parsed payload uses deep JSON snapshot isolation.

### CSV

- first record is the header;
- each subsequent record is one item;
- honor quoted delimiters/quotes/newlines correctly;
- use a mature CSV parser if necessary rather than a split-on-comma implementation.

### JSON

- top-level value is an array of item objects;
- each array element becomes one job item;
- reject non-object array elements;
- do not enable polymorphic/default Java type deserialization.

### Markdown -> Note

One Markdown job produces one Note item.

- preserve the **entire raw Markdown text exactly** as `contentMarkdown`;
- parse YAML frontmatter, when present, into the Note `frontmatter` snapshot while keeping the raw Markdown unchanged;
- retain unknown frontmatter keys;
- title = nonblank frontmatter `title` when present, otherwise original file-name stem;
- `importedFileName` = job original file name;
- `importedFileHash` = job `file_hash`;
- use a safe YAML mode: no arbitrary Java object construction/tags.

A minimal mature parser dependency may be added by the implementation handoff if the current dependency set cannot
correctly parse RFC-style CSV or safe YAML. Do not hand-roll unsafe general parsers merely to avoid a justified small
dependency.

## User decision + transactional import

After `VALIDATED`, execution requires an explicit decision for every item:

For `VALID`:

- `IMPORT`
- `SKIP`

For `DUPLICATE`:

- `UPDATE`
- `SKIP`

For `INVALID`:

- only `SKIP`

`CANCEL` is job-level via a separate cancel operation, not an item decision.

These decision names are Java/API concepts only; no new database enum/table is required.

Execution semantics (after acquiring/rechecking the shared job-level guard):

- all selected `IMPORT`/`UPDATE` target writes for one job execute in one transaction;
- call parent `KnowledgeOperations` create/update methods only;
- `UPDATE` requires `duplicate_vault_entry_id` and must verify through public Vault metadata that the entry type matches
  the import target before invoking the parent Knowledge update;
- `IMPORT` stores returned ID in `imported_vault_entry_id` and sets item status `IMPORTED`;
- `UPDATE` stores the updated target ID in `imported_vault_entry_id` and sets item status `UPDATED`;
- `SKIP` sets item status `SKIPPED`;
- on successful execution, job status becomes `IMPORTED`;
- `imported_items` counts `IMPORTED + UPDATED`; classification counts remain the validated snapshot;
- any target Knowledge failure rolls back **all target writes and all item/job execution-state changes** for that
  execution attempt, leaving the committed job in `VALIDATED` for correction/retry;
- the shared job-level guard is held across the complete execute transaction, including target writes and final
  job/item state changes;
- a waiting/repeated execution must recheck fresh status under the guard and never create a second target set;
- do not use per-item `REQUIRES_NEW` transactions.

Cancellation (under the same job-level guard):

- allowed from `CREATED`, `PARSED`, or `VALIDATED` after a fresh guarded status recheck;
- sets job to `CANCELLED`;
- performs no target writes;
- if cancellation waits behind a committed execution, it observes `IMPORTED` and is rejected;
- if execution waits behind committed cancellation, it observes `CANCELLED` and is rejected before target writes;
- cannot resume or mutate a cancelled job.

## Bounded ImportData reads

Required reads:

- job by ID;
- job items for one job with positive `limit`, ordered `item_index ASC`;
- optional recent jobs with positive `limit`, ordered `created_at DESC, id DESC`.

No unbounded job/item list.

## Security, privacy, and error handling

- never log raw imported payloads, frontmatter, URLs, hashes, or source metadata;
- parser/validation errors stored in `error_message` must be safe summaries, not raw payload fragments or stack traces;
- expected unique conflicts preserve the repository privacy-safe logging baseline;
- JSON/YAML parsing must not enable arbitrary Java type construction;
- exact user-provided Markdown is data, never executable configuration.

# Testing contract

Use PostgreSQL Testcontainers and unchanged Flyway V1.

## Feed tests

Cover:

- all four Feed-owned tables;
- FeedSource create/update/default/schedule checks;
- due-source predicate/order/limit and no read-side mutation;
- successful fetch timestamps and rollback on batch failure;
- FeedItem dual-key insert/update/conflict rules, including real concurrent PostgreSQL races;
- private external ID/URL/hash conflict values do not leak to logs;
- JSON config/raw-metadata source/view isolation;
- SavedResource Vault identity/rollback;
- SavedResource URL-hash sequential/concurrent duplicate conflict with losing Vault rollback;
- save-from-feed provenance and manual save;
- Study/Information/Note conversions through **parent Knowledge API only**;
- conversion rollback if provenance persistence fails;
- conversion-history bounded ordering;
- no Feed-to-Vocabulary conversion API;
- exact Modulith dependency verification: only public Vault + parent Knowledge.

## ImportData tests

Cover:

- both ImportData-owned tables;
- target/format matrix;
- `CREATED -> PARSED -> VALIDATED -> IMPORTED` lifecycle and invalid backward/terminal transitions;
- shared job-level pessimistic serialization + fresh-state recheck for parse/validate/execute/cancel;
- deterministic PostgreSQL **execute-vs-execute** contention on one `VALIDATED` job using at least one target without
  natural uniqueness (prefer Information or Vocabulary): exactly one execution may create/update target/Vault rows;
  the loser waits on the real job-row lock, then rejects from fresh `IMPORTED` state with zero additional target rows;
- deterministic PostgreSQL **execute-vs-cancel** in both winner orders:
  - cancel wins -> job `CANCELLED`, zero target writes, waiting execute rejects;
  - execute wins -> exactly one target write set, job `IMPORTED`, waiting cancel rejects without status overwrite;
- deterministic competing pre-import transitions, including at least:
  - parse-vs-cancel from `CREATED`, and
  - validate-vs-cancel from `PARSED`;
  loser behavior must follow the fresh guarded status rather than stale pre-lock state;
- contention proof must observe actual PostgreSQL lock waiting (for example `pg_locks`/`pg_stat_activity` or an
  equivalent explicit lock-wait signal) and explicit transaction completion; timing-only sleeps are not sufficient;
- every contention test asserts target/Vault counts, job status, item statuses, classification counts,
  `imported_items`, and rollback/no-write behavior;
- cancellation from all approved pre-import states;
- CSV quoting/newlines, JSON array/object shape, Markdown exact-text preservation + safe frontmatter extraction;
- stable UTF-8 SHA-256 file hash;
- deep isolation of parsed JSON payloads;
- Note hash duplicate detection through parent Knowledge only;
- Study/Information/Vocabulary no-invented-dedupe behavior;
- decision matrix for VALID/DUPLICATE/INVALID items;
- target create/update through parent Knowledge API only;
- whole-job transactional rollback when one selected target write fails;
- imported/updated/skipped statuses and count invariants;
- positive bounded job-item/recent-job reads with deterministic order;
- privacy-safe stored/logged errors;
- exact Modulith dependency verification: only public Vault + parent Knowledge.

Final required commands:

```text
mvn -f backend/pom.xml -ntp clean verify
git diff --check
```

Record exact commands, versions, test totals, PostgreSQL/Testcontainers version, Flyway/Hibernate and Spring Modulith
results in `docs/implementation/phase-10/test-evidence.md`.

# Out of scope

- live GitHub Trending/Hacker News/Reddit/RSS/website HTTP adapters;
- scheduler runtime / `@Scheduled` trigger wiring;
- browser automation/scraping;
- REST/controllers/OpenAPI;
- frontend;
- object-storage upload/download for raw import files;
- automatic feed-to-Knowledge conversion;
- SavedResource-to-Vocabulary conversion;
- new Knowledge nested-module APIs or repository access;
- cross-module search;
- Phase 11+ domains;
- RAG/embeddings/vector search;
- DBML/Flyway/schema redesign;
- delete/permanent-delete behavior unless separately approved;
- generic JSON/parser/persistence/event frameworks;
- speculative placeholder events.

## Preparation tooling

Added for Phase 10:

- `.agents/skills/feed-import-workflow-modeling/SKILL.md`
- `.agents/rules/backend-phase-10-feed-importdata.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/AGENTS.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/importdata/AGENTS.md`

Reused without new custom agents/hooks:

- `backend-implementer`
- `architecture-auditor`
- repository safety hook

No new custom agent or hook is justified for Phase 10.


## Completion record

- Owner preparation commit/push: `0e530f2`.
- Final Codex acceptance: `READY FOR OWNER COMMIT`.
- Owner implementation commit/push: completed 2026-10-01.
- Final verification: `mvn -f backend/pom.xml -ntp clean verify` — **685 tests**, 0 failures/errors/skips.
- Phase 10 additions: 87 domain tests + 4 architecture tests over the 594-test pre-Phase-10 baseline.
- PostgreSQL 18.6 Testcontainers, Flyway/Hibernate schema validation, and Spring Modulith verification passed.
- Scope review F7 was resolved in-scope: frozen Knowledge and application-wide serialization remained unchanged.
- Completed handoff: [`handoff.md`](handoff.md).
- Verification evidence: [`test-evidence.md`](test-evidence.md).
- Final acceptance:
  [`reviews/2026-10-01-phase-10-final-codex-acceptance.md`](reviews/2026-10-01-phase-10-final-codex-acceptance.md).
- Status: **COMPLETE — FROZEN**.
- Next gate: Phase 11 `$codex-pre-handoff-review`.
