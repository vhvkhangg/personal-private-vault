# Backend Phase 14 — Backend Integration Hardening + Portability/Object Storage Closure

Status: **READY FOR HANDOFF** (2026-10-06)

Codex [pre-handoff acceptance](reviews/2026-10-06-phase-14-pre-handoff-codex-acceptance.md) closes P14-1–P14-3.
The [initial review](reviews/2026-10-06-phase-14-pre-handoff-codex-review.md) and
[re-review 1](reviews/2026-10-06-phase-14-pre-handoff-codex-rereview-1.md) remain unchanged as historical evidence.
Next: owner commits/pushes this accepted preparation, then invokes `$codex-create-handoff`.

Baseline: owner commit `ef92d94e4b551ec6c7449f251f5186f3e376e0c3`
(`feat(api): expose module capabilities through shared REST contracts`).

Backend Phase 13 is complete/frozen after final Codex acceptance and owner commit/push. There is no active
implementation handoff.

No Phase 14 production implementation is authorized until:

1. `$codex-pre-handoff-review` returns `READY FOR HANDOFF`;
2. the owner commits/pushes the accepted preparation;
3. `$codex-create-handoff` creates the Phase 14 implementation handoff.

Phase 14 is not a milestone phase. The next milestone occurs after Phase 15.

## Owner approval

On 2026-10-06 the owner explicitly approved the prepared Phase 14 concept and ADR-0017 for pre-handoff
preparation review, including:

- the new `portability` Spring Modulith leaf module;
- the narrow, read-only JDBC snapshot exception exclusively for portable export;
- no cross-module JPA/repository access, database writes, or unrelated architectural bypasses;
- preservation of all otherwise-frozen architecture, schema, and module ownership boundaries.

This approval authorizes preparation review and relevant preparation/governance documentation updates only.
It does not authorize Phase 14 implementation or production code changes. The three implementation gates above
remain mandatory; the owner-approval prerequisite is satisfied and is not an outstanding finding.

## Goal

Close the non-RAG backend integration gaps required before frontend work without choosing a production deployment
provider.

Phase 14 implements three bounded areas:

1. S3-compatible media binary I/O under the existing `media` module;
2. owner-controlled portable export through a new read-only `portability` leaf module;
3. operational readiness/configuration hardening needed by those integrations.

It does not reopen unrelated frozen domain behavior.

## Architecture authority

Use:

- `docs/adr/0009-s3-compatible-object-storage.md`
- `docs/adr/0012-import-and-data-portability.md`
- `docs/adr/0014-defer-frontend-rag-and-deployment.md`
- `docs/adr/0017-portability-readonly-snapshot-module.md`
- `docs/architecture/storage-backup-import.md`
- `docs/architecture/integration-and-eventing.md`
- `docs/architecture/api-architecture.md`
- `docs/architecture/security-architecture.md`
- `docs/architecture/module-boundaries.md`
- `docs/architecture/module-dependency-matrix.md`

No database schema/Flyway/DBML change is planned.

## A. `portability` module

Add one top-level Spring Modulith module:

```text
portability/
├── package-info.java
└── internal/
    ├── application/
    ├── infrastructure/snapshot/
    └── web/
```

`portability` owns no table and has no application-module dependency on business modules. Same-module controllers
and tests do not justify a public `@NamedInterface`; add a public package only if an actual cross-module Java caller
appears in authorized implementation. Add `package-info.java` only to meaningful packages that are actually created.

### Read-only snapshot exception

ADR-0017 permits only the portability snapshot adapter to read other application tables directly through JDBC.

Required safeguards:

- explicit table allowlist; never enumerate arbitrary runtime tables and dump them blindly;
- exclude `app_users`, `refresh_tokens`, and `flyway_schema_history`;
- include every other table intentionally approved by the portable format;
- one PostgreSQL transaction: `READ ONLY`, `REPEATABLE READ`;
- no `INSERT`, `UPDATE`, `DELETE`, DDL, repository injection, JPA entity use, or cross-module internal import;
- deterministic table order;
- deterministic primary-key order inside every exported table;
- bounded/streaming row processing; do not load the entire vault into memory;
- exact decimal preservation;
- JSONB retained as JSON;
- enum values as strings;
- timestamps/dates/times encoded using stable ISO representations;
- explicit nulls preserved.

Architecture/schema tests must fail when a new non-auth application table appears without an explicit export
allowlist decision.

## Portable ZIP format v1

Export success is a streamed ZIP:

```text
personal-private-vault-export-v1.zip
├── manifest.json
├── tables/
│   └── <table>.jsonl
├── markdown/
│   ├── knowledge/notes/<id>.md
│   ├── knowledge/information/<id>.md
│   └── journal/diary-entries/<id>.md
└── media/
    └── manifest.jsonl
```

`manifest.json` includes at least:

- `formatVersion = 1`;
- generation timestamp in UTC;
- highest applied Flyway version;
- exported-table inventory;
- explicit excluded security tables;
- checksum/entry-count metadata where it can be computed without buffering the whole archive.

### Inclusion policy

The owner export is loss-preserving:

- include active rows;
- include Vault trash;
- include module soft-deleted rows;
- include retained relationship/history/child rows;
- include feed/import provenance and JSON metadata;
- include safe application settings/reference data needed to understand records;
- include image object key, MIME type, byte size and SHA-256 metadata.

Never include:

- password/PIN hashes;
- refresh-token hashes or token rows;
- JWT signing material;
- Authorization headers;
- S3 credentials;
- database credentials;
- runtime environment secrets;
- server logs.

Binary image/media objects are **not** copied into the portable ZIP in Phase 14.

### Snapshot lifetime and export streaming boundary

The read-only snapshot must remain active for the **actual database row consumption that generates the archive**,
not merely while constructing/returning a streaming callback. The implementation choice remains open: direct response
streaming, bounded temporary-file spooling, or another bounded approach is acceptable if all of these invariants hold:

- the PostgreSQL `READ ONLY` + `REPEATABLE READ` transaction starts before the first exported table read and ends only
  after the last database row needed by the archive has been consumed;
- if asynchronous MVC streaming is used, the transaction is opened/executed on the worker thread that performs the
  database reads; no thread-bound JDBC transaction/cursor is created on the request thread and consumed later on a
  different thread;
- JDBC statements/result sets/cursors, ZIP streams, object/input streams, and any temporary files are closed/deleted
  in `finally`/equivalent cleanup on success, generation failure, timeout, cancellation, and client disconnect;
- no JDBC cursor/transaction survives response completion or cancellation;
- no implementation is required to buffer the complete archive in heap.

HTTP failure handling is split at the servlet response-commit boundary:

- **pre-commit failure** (validation, authorization, snapshot establishment, storage open/read preparation, or archive
  generation failure before headers/body are committed) -> return the canonical JSON `ApiResponse` error;
- **post-commit failure** (database/storage read failure, socket write failure, timeout, or client disconnect after any
  response bytes are committed) -> abort/close the transfer. Never append JSON to a binary body, never reset a
  committed response, and never present a truncated archive/object as a completed download.

A portability export is complete only when ZIP finalization (including the central directory) succeeds and the HTTP
body completes normally. A truncated/non-finalized ZIP is incomplete and must be rejected by callers as a failed
export. For image download, the retained `Content-Length` (when known) also lets callers detect truncation. Phase 14
does not add a persisted export job or a generic resumable-streaming framework.

### HTTP endpoint

Add:

```text
POST /api/v1/portability/exports
```

Requirements:

- bearer protected;
- success: `200 application/zip`;
- `Content-Disposition: attachment`;
- `Cache-Control: no-store`;
- filename contains only a safe timestamp/version, no personal content;
- pre-commit errors use the canonical JSON `ApiResponse`; post-commit streaming failures abort the transfer as
  defined above and must not append/reset to JSON;
- do not log export body, Markdown, row values, object keys, or generated archive bytes.

No persisted export-job table is added.

## B. S3-compatible media binary integration

Object-storage integration is owned by `media`.

Add a small owner-local storage port and S3-compatible infrastructure adapter. The domain/application contracts must
not contain AWS/MinIO provider classes.

Implementation direction:

- S3-compatible API;
- production provider remains unfrozen;
- custom endpoint/path-style support for compatible development providers;
- credentials only through runtime configuration/environment;
- local/integration testing with small MinIO/S3-compatible test objects;
- no owner personal media fixtures in the repository.

A stable AWS S3-compatible SDK is acceptable when isolated behind the media-owned port. Prefer a BOM/current stable
version compatible with the existing Spring Boot stack; do not scatter provider SDK types through domain/web DTOs.

### Managed image upload

Add:

```text
POST /api/v1/images/upload
```

Multipart request:

- one binary file;
- explicit metadata fields needed by the existing Image creation contract except storage-derived fields.

The managed upload workflow:

1. validate structural metadata/file size using configured limits;
2. spool/stream without buffering an unbounded file into heap;
3. compute SHA-256 and authoritative byte size;
4. generate a server-owned opaque object key unique to this upload attempt;
5. upload the object;
6. create Image/Vault metadata through the existing Media application/domain path inside an explicit database
   transaction boundary;
7. classify the database outcome using the commit rules below;
8. return success only after storage upload is confirmed and metadata commit is confirmed (or a fresh reconciliation
   read proves the exact metadata row committed after an initially uncertain commit result).

The managed-upload orchestration must not keep an ambient transaction open across remote object upload and then make
a compensation decision before the database commit actually occurs. The metadata step must have a boundary whose
normal return means the transaction manager has completed commit (for example the existing transactional service
called without an outer transaction, `TransactionTemplate`, or an equivalent explicit boundary).

#### Metadata commit and compensation rules

There is no cross-system atomic transaction between PostgreSQL and object storage. Phase 14 therefore uses these
explicit states:

- **confirmed metadata commit**: commit completed successfully, or a fresh post-failure read by this attempt's unique
  object key confirms the expected Image row/checksum/size. Keep the object; never compensate/delete it.
- **confirmed metadata rollback/not committed**: validation/constraint failure or transaction outcome is known to have
  rolled back. Attempt compensation deletion of only this attempt's server-owned object key.
- **uncertain metadata commit**: commit raised an error/connection loss and PostgreSQL outcome is not yet known. Do
  not blindly delete. A fresh Media-owned read by object key may resolve the outcome only in the positive direction:
  if the exact expected row/checksum/size exists, treat it as committed and keep the object. An absent or mismatched
  row is **not** proof of rollback because the original transaction may still be in flight and later commit. In that
  case return a privacy-safe failure and retain the object pending manual recovery. Automatic compensation is allowed
  only if authoritative outcome evidence from the original transaction boundary itself reclassifies the metadata
  transaction as **confirmed rollback/not committed** (for example, rollback/abort completion is reported for that
  transaction before success can be committed). A later SELECT/inventory result, timeout, connection loss/closure, or
  absence of the row is never sufficient rollback evidence.
- **uncertain storage upload**: if object upload times out/fails without a definitive provider outcome, do not create
  metadata until storage state is resolved. Probe the attempt's key when possible; delete it if existence is confirmed
  and metadata was never committed. If storage remains unavailable, return failure and accept the bounded residual
  orphan risk described below.

Compensation is best-effort and bounded: at most **3 delete attempts total per request** with short bounded backoff;
it must never retry indefinitely. A compensation failure must not mask/replace the primary upload/metadata failure and
must not expose the object key, bucket, endpoint, provider exception text, or credentials in the API response/logs.
A privacy-safe warning may carry only a request/correlation identifier and a generic `manual cleanup required` signal.

If compensation cannot be completed or commit/upload outcome remains unresolved, Phase 14 explicitly accepts a
possible residual orphan object. Owner recovery is manual and privileged, and deletion is permitted only during a
**quiescent managed-upload writer state**: stop/disable managed-upload ingress, allow or force all outstanding upload
requests and their PostgreSQL transactions to settle/terminate, and do not proceed while any original writer could
still commit. After quiescence is established, take a fresh comparison of the private object-store inventory against
committed `images.object_key` values and re-check the candidate key immediately before deletion; delete only an
object still confirmed unreferenced. If writer quiescence or settled transaction outcomes cannot be established,
retain the object and defer cleanup. This procedure uses provider/database administration access directly; it does
not require a new outbox/job table, scheduler, hard-delete API, two-phase-commit framework, or reconciliation
platform.

The existing JSON metadata-create endpoint remains supported for already-managed/external object metadata. Phase 14
does not silently change that frozen behavior.

Duplicate-checksum races must prove loser-object compensation against real PostgreSQL + S3-compatible test storage.

### Binary download

Add:

```text
GET /api/v1/images/{id}/content
```

Requirements:

- bearer protected;
- stream bytes; do not load the complete object into heap;
- content type from retained metadata;
- content length when known;
- `Cache-Control: private, no-store`;
- missing Image metadata keeps existing Media not-found behavior;
- metadata exists but object is missing -> stable safe storage-integrity error (409), without exposing bucket/key;
- a storage-open failure before response commitment uses canonical JSON; a read/write failure after response commitment
  aborts the transfer and must not append a JSON envelope;
- all object input streams/resources close on success, failure, timeout, cancellation, and client disconnect;
- no raw storage-provider error body or credential detail.

### Lifecycle

Vault trash/restore does not delete/recreate the object. Phase 14 adds no hard-delete or binary-replacement semantics.

Do not add presigned public URLs. Backend proxy streaming is sufficient for the single-user v1 closure.

## C. Operational readiness

When object storage is enabled:

- fail fast on structurally invalid required configuration;
- add a privacy-safe object-storage health contributor;
- readiness must fail when required storage is unavailable;
- liveness must not depend on temporary external-storage availability.

Expose Spring Boot health groups under:

```text
/actuator/health
/actuator/health/liveness
/actuator/health/readiness
```

Phase 14 may narrowly change the security matcher from exact `/actuator/health` to `/actuator/health/**`.

Unauthenticated health responses must use `show-details=never` (or equivalent) and never expose bucket names,
endpoints, credentials, exception messages, SQL, or private data.

No Prometheus/Grafana/vendor observability stack is introduced.

## Existing HTTP/OpenAPI integration

Phase 14 must update the Phase 13 OpenAPI contract only for the approved new surfaces:

- multipart managed image upload;
- binary image content download;
- binary portability export;
- corresponding safe **pre-commit** JSON error responses plus documented post-commit transfer-abort semantics.

The existing **211-route Phase 13 manifest** is a frozen baseline. New Phase 14 routes are additive only.

All existing JSON endpoints keep the Phase 13 `ApiResponse` contract.

## Explicit operational deferrals

The following remain deferred rather than being invented as part of Phase 14:

- production object-storage provider/bucket topology;
- provider-level binary backup destination/retention/encryption;
- automated backup scheduling despite existing settings fields;
- raw ImportData file-object upload/storage workflow;
- binary-inclusive portable export;
- full-archive round-trip import;
- live Feed provider HTTP adapters/scheduler runtime;
- Finance recurring auto-post/subscription scheduler;
- hard/permanent deletion;
- CORS/browser token-storage/cookie policy;
- production deployment/reverse proxy/TLS/CI-CD;
- frontend;
- RAG.

These require separate product/provider/business scheduling decisions and are not silently implied by “operational
closure”.

## Dependencies/configuration

Phase 14 may change `backend/pom.xml` only for dependencies directly required by:

- S3-compatible media storage;
- deterministic test storage infrastructure.

Do not add an object-storage provider framework, message broker, cache, scheduler framework, or generic integration
platform.

Configuration additions must:

- use typed validated Spring configuration properties;
- default storage integration to disabled unless a complete development/runtime configuration is supplied;
- keep secrets out of checked-in defaults;
- allow tests to inject synthetic credentials/endpoints.

Local Docker/Compose MinIO support is allowed as development-only infrastructure under ADR-0014.

## Testing contract

Preserve all **867** Phase 13 baseline tests.

### Portability

Use real PostgreSQL Testcontainers and prove:

- one repeatable-read, read-only snapshot;
- explicit allowlist covers every intended non-auth application table;
- authentication/Flyway tables are absent;
- active + trash + soft-deleted/history rows are exported;
- deterministic ordering and reproducible logical content;
- decimal, JSONB, enum, date/time/timestamp fidelity;
- exact Markdown preservation;
- media manifest includes object-key/checksum/type/size metadata;
- no credentials/security rows/private diagnostic data;
- ZIP is valid and streamable;
- pre-commit export failure returns canonical JSON and writes no binary success body;
- post-commit export failure produces an aborted/truncated transfer, never JSON appended to ZIP bytes;
- snapshot consistency is preserved during **actual row consumption**, including the selected async/temp-file execution
  strategy rather than only callback construction;
- JDBC cursors/transactions, ZIP streams, and temporary files are released after success, generation failure, timeout,
  cancellation, and simulated client disconnect;
- export does not mutate database state;
- no unbounded list materialization.

A concurrency test should mutate data in a second transaction after export snapshot establishment and prove the
archive is internally consistent with one committed snapshot, not a mixed before/after view. The test must consume
the export through the real execution path so it proves the snapshot remains alive for row consumption.

### Object storage

Use S3-compatible integration storage (for example MinIO in Testcontainers) and PostgreSQL together.

Cover:

- upload -> metadata -> download byte equality;
- authoritative SHA-256 and byte size;
- server-generated key;
- MIME/content headers;
- confirmed metadata rollback/failure -> bounded object compensation;
- database **commit-time failure** is classified as confirmed rollback vs uncertain commit; a fresh positive
  object-key reconciliation may prove commit, but an absent/mismatched lookup must retain the object and must not
  authorize deletion unless independent evidence proves the original transaction terminated without commit;
- deterministic uncertain-commit race: hold the metadata transaction open after Image/Vault writes, reconcile while
  the row is invisible to another PostgreSQL transaction, verify the object is not deleted, then commit the original
  transaction and prove the retained Image/Vault metadata still resolves to the binary;
- manual residual-orphan cleanup requires quiescent managed-upload writers, settled/terminated outstanding metadata
  transactions, a fresh committed-row/object inventory comparison, and a final unreferenced-key re-check before delete;
- failed compensation (all 3 attempts) preserves the primary failure, returns no false success, leaks no key/provider
  detail, and leaves only the explicitly accepted residual-orphan/manual-recovery condition;
- duplicate-checksum database race -> loser object cleanup, including compensation-failure behavior;
- storage failure/uncertain upload -> no Image/Vault metadata partial state and no blind metadata creation;
- no compensation path can delete an object after confirmed metadata success;
- missing object -> safe 409;
- pre-commit download/open failure -> canonical JSON; post-commit read/write failure -> aborted transfer;
- object streams/resources close after success, failure, timeout, cancellation, and client disconnect;
- Vault trash/restore keeps the binary;
- large-enough test stream proves the implementation does not require whole-file heap buffering;
- no storage credentials/raw keys/provider error detail in application logs/errors.

### Operational/API

Cover:

- storage disabled startup;
- enabled invalid config fails fast;
- readiness/liveness behavior;
- health details remain private;
- multipart and binary OpenAPI schemas/statuses;
- binary success routes bypass JSON envelope only as authorized;
- binary-route **pre-commit** errors remain canonical JSON; post-commit failures abort the binary transfer without JSON;
- callers can distinguish completed vs truncated/incomplete export/download behavior;
- existing 211 Phase 13 routes remain unchanged.

### Architecture

Verify:

- `portability` is a Spring Modulith leaf module with no business-module dependency;
- it owns no JPA entity/repository/table;
- direct JDBC access exists only in the approved portability snapshot adapter;
- no other module gains cross-table JDBC;
- media SDK/provider classes remain internal infrastructure;
- no provider type leaks into public/domain/web contracts;
- no new schema migration;
- frozen module dependencies remain unchanged except addition of the `portability` row/ADR exception.

Final verification:

```text
mvn -f backend/pom.xml -ntp clean verify
git diff --check
```

Record exact evidence in:

```text
docs/implementation/phase-14/test-evidence.md
```

## Preparation tooling

Added:

- `.agents/skills/backend-integration-portability-storage/SKILL.md`
- `.agents/rules/backend-phase-14-integration-closure.md`
- ADR-0017

Reused:

- `backend-implementer`
- `architecture-auditor`
- `media-domain-modeling`
- `rest-api-http-contracts`
- Java/Spring, modular-monolith, PostgreSQL/testing, security/privacy and reuse skills
- repository safety hook

No new custom agent/hook is planned.

## Next gate

Owner commit/push of accepted preparation first, then:

```text
$codex-create-handoff
```

Preparation review is `READY FOR HANDOFF`; do not create the Phase 14 handoff until the owner commits/pushes the
accepted preparation. Production implementation still requires that active handoff.
