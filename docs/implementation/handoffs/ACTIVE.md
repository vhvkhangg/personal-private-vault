# Active Implementation Handoff

- Handoff ID: `phase-14-portability-storage`
- Created by: Codex
- Created: 2026-10-06
- Status: `READY FOR OWNER COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Accepted preparation baseline: `6f1fd00d89a2d0674677cf4c38b814bc434ec253` (HEAD and `origin/main` at creation)
- Frozen Phase 13 implementation baseline: `ef92d94e4b551ec6c7449f251f5186f3e376e0c3`

## Goal

Implement the accepted Phase 14 closure: Media-owned S3-compatible image upload/download, owner-triggered portable
export through a read-only `portability` leaf module, and the configuration/readiness support needed by these
integrations. Preserve frozen domain behavior, Schema v1, module ownership, and Phase 13 HTTP contracts.

## Sources of truth

- [Phase 14 scope](../phase-14/README.md): all behavior, portable-format, deferral, and testing requirements are mandatory.
- [Preparation review](../phase-14/preparation-review.md) and
  [acceptance](../phase-14/reviews/2026-10-06-phase-14-pre-handoff-codex-acceptance.md): `READY FOR HANDOFF`; P14-1–P14-3 closed.
- [ADR-0017](../../adr/0017-portability-readonly-snapshot-module.md): the sole export-only, read-only cross-table JDBC exception.
- [ADR-0009](../../adr/0009-s3-compatible-object-storage.md), [ADR-0012](../../adr/0012-import-and-data-portability.md),
  [ADR-0014](../../adr/0014-defer-frontend-rag-and-deployment.md), and [ADR-0016](../../adr/0016-root-http-contract-module-local-adapters.md).
- [Module boundaries](../../architecture/module-boundaries.md), [dependency matrix](../../architecture/module-dependency-matrix.md),
  and [package tree](../../repository/repository-package-tree.md): only the approved `portability` delta; no new dependency edge.
- [Storage/export](../../architecture/storage-backup-import.md), [integration](../../architecture/integration-and-eventing.md),
  [HTTP](../../architecture/api-architecture.md), and [security](../../architecture/security-architecture.md).
- [Frozen DBML](../../database/personal-private-vault-schema-v1-FROZEN-final.dbml) and existing
  `backend/src/main/resources/db/migration/`: unchanged schema sources.
- Root/backend/module `AGENTS.md`, [handoff workflow](README.md), and relevant engineering skills below.

Owner concept/ADR-0017 approval was preparation-only. Preparation has now been accepted and owner committed/pushed
as `6f1fd00`; the owner's current `$codex-create-handoff` request creates this separate implementation contract.
This handoff authorizes Antigravity only within this scope; Codex has not implemented production code.

## Implementation targets

Java paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.

- New `portability/package-info.java`, `portability/internal/application`, `portability/internal/infrastructure/snapshot`,
  and `portability/internal/web`: bounded snapshot/export and its controller. Only meaningful packages/descriptors;
  no speculative public named interface, owned persistence, or generic exporter.
- `media/internal/application` and `media/internal/infrastructure`: small Media-owned storage port, S3 adapter,
  upload/download use cases, typed configuration and health. Reuse `ImageService.create` for atomic Image/Vault
  metadata creation; any fresh reconciliation query stays inside Media's `ImageRepository`/application boundary.
- `media/internal/web` (existing `controller/ImageController.java`, DTOs, `advice/MediaExceptionAdvice.java`, or focused
  owner-local additions): multipart/binary adapters; existing metadata endpoints unchanged.
- Existing root `OpenApiConfiguration.java` and, only where needed for framework multipart/pre-commit errors,
  `ApiExceptionHandler.java`: narrow updates for the three approved API operations; no root technical package.
- `authentication/internal/infrastructure/security/SecurityConfiguration.java`: only the approved health matcher
  extension from `/actuator/health` to `/actuator/health/**`; JWT/authentication lifecycle remains frozen.
- `backend/src/main/resources/application.yml`, `backend/pom.xml`: storage/health/multipart configuration and directly
  required SDK/test dependencies. Development-only MinIO support in `backend/compose.dev.yml` is allowed.
- Tests under `backend/src/test/java/com/vhvkhangg/personalprivatevault/`: Portability/Media/operational coverage and
  focused updates to `ApplicationArchitectureTests.java`, `migration/FlywayV1SchemaManifestIntegrationTest.java`,
  `web/OpenApiRouteInventoryIntegrationTest.java`, and security tests as required by this additive scope.
- `docs/implementation/phase-14/test-evidence.md` and concise development/manual-recovery guidance: real evidence
  and safe operation; do not rewrite accepted preparation/review history.

## Required behavior / invariants

### Portability

- Leaf with no business-module dependencies or inbound business dependency, table, JPA entity/repository. Only the
  snapshot adapter may read the explicit application-table allowlist through JDBC. No writes/DDL, foreign JPA/entity/
  repository/internal imports, or generic reporting SQL. A schema guard fails on any undecided new non-auth table.
- Exclude `app_users`, `refresh_tokens`, and `flyway_schema_history` from exported data; include all other explicitly
  approved retained rows (trash, soft deletes, histories/relationships, provenance, safe settings/reference and media
  metadata). No security state, credentials/runtime secrets, logs, media binaries, or Java serialization.
- One actual PostgreSQL `READ ONLY`, `REPEATABLE READ` transaction spans all archive row consumption, not callback
  construction. Async readers open it on the consuming worker; no cross-thread cursor/transaction. Deterministic
  table/primary-key order and bounded processing, never whole-vault materialization.
- ZIP format v1 must match the scope/ADR: manifest, JSONL tables, exact Markdown copies and media manifest. Preserve
  decimals, JSONB, enums, ISO temporal values/UTC timestamps, explicit nulls and Markdown. Obtain the safe highest
  applied Flyway version for the manifest without exporting Flyway metadata rows.

### Managed image storage

- Media owns the port/adapter; AWS/S3/MinIO types stay in its infrastructure, never domain/application/public/web
  contracts. Runtime-only credentials and compatible endpoint/path-style support; synthetic fixtures only.
- Validate metadata/configured size bounds; spool/stream without whole-file heap buffering; derive SHA-256/byte size
  and a unique opaque server-owned attempt key. Confirm object upload before existing atomic Image/Vault creation.
  No ambient database transaction across remote upload; metadata success requires completed commit, not pending outer commit.
- Confirmed commit, or a fresh positive lookup proving this attempt's exact row/key/checksum/size, retains the object.
  Success requires confirmed storage and metadata. Confirmed rollback/not-committed metadata permits compensation
  of this attempt's key only. Never delete after confirmed commit.
- Uncertain commit retains the object unless authoritative rollback/abort evidence comes from the original transaction
  boundary itself. An absent/mismatched fresh lookup, inventory, timeout or connection loss/closure never proves
  rollback. Positive reconciliation may prove commit; unresolved outcome returns privacy-safe failure and retains the object.
- Resolve/probe uncertain storage upload before metadata creation; unresolved failure creates no partial Image/Vault
  state. Compensation has at most 3 delete attempts total per request with short bounded backoff; failed cleanup
  preserves the primary failure, returns no false success and leaks no key/bucket/endpoint/provider text/credentials.
- Residual orphans are an explicitly accepted risk. Document privileged manual recovery: quiesce managed-upload
  ingress, settle/terminate all outstanding metadata writers/transactions, compare fresh inventory with committed
  `images.object_key`, and re-check each unreferenced key immediately before deletion. Any possible later commit means retain/defer.
- Preserve existing JSON metadata creation/updates, uniqueness/validation, and Image/Vault identity. Duplicate-checksum
  losers clean up only their own attempt. Vault trash/restore retains binaries; no hard delete/replacement.

### HTTP and operational support

- Add only these bearer-protected API operations:
  - `POST /api/v1/images/upload`: multipart file plus explicit existing Image metadata except derived storage fields;
    canonical JSON success/error envelope and truthful OpenAPI contract.
  - `GET /api/v1/images/{id}/content`: streamed retained MIME/length when known, `private, no-store`; existing metadata
    not-found behavior and safe missing-object 409.
  - `POST /api/v1/portability/exports`: `200 application/zip`, attachment with safe version/timestamp filename, `no-store`.
- Before servlet response commitment, errors use canonical JSON `ApiResponse`. After commitment, read/write/database/
  storage/timeout/disconnect failures abort transfer: never append JSON, reset a committed response, or present
  truncation as completion. Export completion requires ZIP finalization and normal HTTP body completion.
- Close JDBC statements/result sets/cursors/transactions, ZIP/object streams and temporary files on success, failure,
  timeout, cancellation and client disconnect. No transaction/cursor outlives the response; no complete archive in heap.
- Storage defaults disabled; typed validated config fails fast on enabled structural errors. Required-storage failure
  affects readiness, not liveness. Approved health/liveness/readiness responses hide details (`show-details=never` or
  equivalent); no broader public Actuator exposure. Never log private payloads, keys, secrets or provider/SQL diagnostics.
- Preserve the original 211 Phase 13 method/path/operation/status/DTO contracts and JSON envelopes. Extend inventory
  tests with exactly the three additive operations and multipart/binary schemas; do not weaken the original manifest
  or apply JSON-success assumptions to binary bodies. Preserve framework constraint-log privacy protections.

## Non-goals

No schema/Flyway/DBML changes, unrelated frozen-domain refactors, new dependency edges, shared JPA access, persisted
export/cleanup jobs, outbox/2PC/reconciliation platform, broker/cache/scheduler framework, binary-inclusive export,
full-archive import, raw ImportData object-upload workflow, hard delete/replacement, public presigned URLs, production
provider/topology/deployment/observability stack, automated backups, Feed providers/scheduling, recurring Finance
posting, browser CORS/cookie/token policy, frontend, RAG, multi-user support, or new authentication features.
All explicit operational deferrals in the Phase 14 scope remain authoritative.

## Test/evidence contract

- Focused tests: JUnit/AssertJ, real PostgreSQL Testcontainers plus real S3-compatible storage (e.g. MinIO), HTTP real
  execution-path streaming tests and Spring Modulith/architecture checks. No H2 or personal fixtures. Implement the
  entire [Phase 14 testing contract](../phase-14/README.md#testing-contract), including:
  - actual-consumption snapshot consistency during concurrent committed writes; read-only enforcement, complete
    allowlist/security exclusions, fidelity, ordering/ZIP/manifest and no database mutation;
  - upload/download byte equality, hash/size/key/MIME, confirmed commit/rollback and commit-time failures, maximum-3
    failed-compensation privacy/primary-error preservation, duplicate-checksum races and uncertain upload isolation;
  - deterministic delayed-commit regression: hold original PostgreSQL Image/Vault writes uncommitted, reconcile from
    another transaction while absent, prove no deletion, commit the original writer and prove its binary remains;
  - pre-/post-response-commit errors, truncation detection, bounded memory and cleanup through failure/timeout/
    cancellation/client disconnect in the actual selected streaming strategy, plus successful resource release;
  - disabled startup, invalid enabled config, readiness vs liveness, public health privacy, bearer protection and
    runtime/OpenAPI agreement; preserve the original 211 routes and all 867 Phase 13 baseline tests;
  - leaf/no-table/no-cycle/no-foreign-internals/provider isolation; no new migration or cross-table JDBC outside the
    snapshot adapter. Do not disable assertions or suppress tests to preserve a numeric count.
- Final commands: `mvn -f backend/pom.xml -ntp clean verify` and `git diff --check`.
- Required evidence: create `docs/implementation/phase-14/test-evidence.md` during implementation. Record exact
  focused/full commands, results, test/suite counts, failures/retries and diagnostics. Do not claim unrun tests or
  rewrite retained Phase 13 evidence. Follow the active-handoff implementation workflow for result reporting.
- Acceptance criteria: all accepted Phase 14 requirements and focused regressions satisfied, final commands green,
  frozen baselines preserved, and implementation/evidence/manual-recovery guidance ready for Codex final review.

## Constraints / risks

- PostgreSQL and object storage are not atomic together. Residual orphans are accepted only under the conservative
  failure/manual-recovery rules above; committed metadata losing its binary is not accepted.
- Slow clients/long snapshots require bounded resources and cancellation cleanup. Direct streaming versus bounded
  temporary-file spooling is an implementation choice, not permission to weaken snapshot/HTTP semantics.
- SDK/BOM/test dependencies must fit the frozen stack and stay behind Media infrastructure; no unrelated framework/
  runtime upgrades or production provider selection. If broader scope/frozen conflict is required, stop and report it.
- Agents never commit, push, tag, create/merge PRs, or use owner data. Phase 14 is not a milestone; the next is Phase 15.

Relevant skills: `backend-integration-portability-storage`, `media-domain-modeling`, `rest-api-http-contracts`,
`java-spring-coding-standards`, `modular-monolith-architecture`, `jpa-postgresql-persistence`, `backend-testing`,
`reuse-and-consistency`, `pragmatic-solid-design`, `design-pattern-selection` (only for a concrete need), and
`authentication-security` for the narrow health matcher/security regressions only. Use `/antigravity-implement-handoff`.

## Implementation result

The following is Antigravity's submitted remediation result for FR14-1 through FR14-8, retained as reported.
Historical re-review 1 independently verified the 891-test build and then accepted only FR14-6–FR14-8 in full.
The final acceptance report now closes FR14-1–FR14-9. Retained submissions below are reported history;
the final review section and formal acceptance are the current Codex authority.

- **Scope Implemented & Remediated:** Complete Backend Phase 14 backend integration hardening and portability/object storage closure.
  - Resolved FR14-1: Conservative retention on ambiguous metadata outcome; positive reconciliation verifies exact attempt (key, checksum, size); confirmed rollback compensates; upload-path delayed-commit regression passing.
  - Resolved FR14-2: Pre- vs post-commitment response separation in `ApiExceptionHandler`; post-commit streaming failures abort transfer without appending JSON error bodies; opened streams closed on async executor rejection, timeout, error, or completion.
  - Resolved FR14-3: Lossless JSONB numeric precision (`USE_BIG_DECIMAL_FOR_FLOATS`, `WRITE_BIGDECIMAL_AS_PLAIN`) and PostgreSQL microsecond fractional time preservation in `PortabilitySnapshotAdapter`.
  - Resolved FR14-4: Entire accepted testing contract implemented against real execution paths (PostgreSQL Testcontainers + MinIO); ArchUnit guards strengthened; honest commands, test names, and metrics reported.
  - Resolved FR14-5: Truthful 4xx mappings for multipart errors (`MissingServletRequestPartException` -> 400 `MALFORMED_REQUEST`, `MaxUploadSizeExceededException` -> 413 `PAYLOAD_TOO_LARGE`); explicit binary response schemas in OpenAPI and inventory test.
  - Resolved FR14-6: Bounded resolution of uncertain storage uploads: probes existence, compensates known object if present (metadata never initiated), retains residual orphans on probe failure.
  - Resolved FR14-7: Fail-closed manual recovery runbook: authoritatively verifies writer quiescence via `pg_stat_activity` looking for `(state = 'idle in transaction' OR xact_start IS NOT NULL)`; defers deletion if quiescence cannot be established; corrected ADR references.
  - Resolved FR14-8: Meaningful package descriptors (`package-info.java`) created for all 5 populated packages; `docs/repository/repository-package-tree.md` synchronized.
- **Test Verification:**
  - Focused test suites passing:
    - `MediaStorageIntegrationTest`: 14/14 passing (byte equality async dispatch, 409 storage conflict, 404 metadata not found, pre-commit 500 JSON, post-commit abort without JSON, executor rejection closure, missing multipart 400, uncertain storage probe/compensation, probe failure orphan retention, confirmed rollback compensation, delayed-commit regression, bounded compensation, health groups, storage-down readiness degradation).
    - `PortabilityIntegrationTest`: 5/5 passing (lossless ZIP archive with all 67 tables, JSONB precision, fractional time, 401 unauthenticated, pre-commit 500 JSON, schema allowlist guard, repeatable-read snapshot consistency).
    - `ApplicationArchitectureTests`: 35/35 passing (including strengthened Portability repository/JDBC confinement and Media-only AWS SDK isolation).
    - `FlywayV1SchemaManifestIntegrationTest`: 2/2 passing (schema allowlist matches physical tables).
    - `OpenApiRouteInventoryIntegrationTest`: 1/1 passing (validating all 214 routes and explicit binary schemas).
  - Full suite verification: `mvn -f backend/pom.xml -ntp clean verify` passed with **891 tests** (0 failures, 0 errors, 0 skips), execution time 02:20 min.
  - Formatting / whitespace check: `git diff --check` passed with 0 violations.
- **Evidence:** Documented comprehensively in [test-evidence.md](../phase-14/test-evidence.md).
- **Invariants Preserved:** Schema v1 frozen (zero Flyway migrations or DDL changes), single-user vault semantics, zero AWS SDK types outside Media infrastructure, bounded compensation (<=3 retries), pre-commit canonical JSON `ApiResponse` vs post-commit stream abort.
- **Residual Orphan Risk & Recovery:** Conservative object preservation on uncertain outcomes; complete privileged fail-closed manual recovery guidance documented in [test-evidence.md](../phase-14/test-evidence.md#3-operational-manual-recovery-instructions-for-residual-orphans).

## Codex remediation

**No open remediation.** [Final acceptance](../phase-14/reviews/2026-10-06-phase-14-final-codex-acceptance.md)
closes FR14-4 and retains closure of all eight other findings. Both checksum races now distinguish actual
PostgreSQL arbitration; active export cursor faults and registered image completion hooks close the remaining
lifecycle gap. Retain all accepted fixes and tests. Re-review 5's former test-only instructions remain recorded in
its [historical report](../phase-14/reviews/2026-10-06-phase-14-final-codex-rereview-5.md), not as a new active task.

Owner commit/push is next, then ChatGPT Phase 14 closeout/Phase 15 preparation. This acceptance authorizes no
additional implementation, frozen-baseline changes or deferred scope. Agents do not commit/push.

### Antigravity re-review 1 remediation submission (2026-10-06; claims retained)

The following is the implementer's submission, not Codex acceptance. Re-review 2 accepts FR14-1/FR14-3/FR14-9,
but rejects full closure of FR14-2/FR14-4/FR14-5. Its formal findings and the remediation above take precedence.

- **FR14-1 Resolved:** Enforced ambient transaction rejection before remote S3 I/O via `TransactionSynchronizationManager.isActualTransactionActive()`. Whitelisted `AlbumNotFoundException` as confirmed validation rollback triggering bounded compensation.
- **FR14-2 Resolved:** Chunked streaming via `StreamingResponseBody` now rethrows `IOException` directly on post-commit transfer failure to abort the transport connection. Added fallback to provider known size.
- **FR14-3 Resolved:** Configured Jackson read constraints (1M number length) and implemented direct raw token writing via `RawJsonValue` (`gen.writeRawValue`) for JSONB/JSON column exports, eliminating quoted-string coercion.
- **FR14-4 Resolved:** Real concurrency test via `exportSnapshot(Path, Runnable)` verifying repeatable-read isolation against concurrent commits. Real throwing probes for storage exists check. Primary-error preservation with max 3 compensation retries verified.
- **FR14-5 Resolved:** Documented HTTP 413 on `POST /api/v1/images/upload` in `@ApiResponse` and `OpenApiConfiguration`. Tightened route inventory check to `format == "binary" && type == "string"` with exact approved media types.
- **FR14-9 Resolved:** Replaced all raw exception message logging (`ex.getMessage()`) with safe classifications (`ex.getClass().getSimpleName()`). Logback assertions verify zero SQL, bucket, or key leakage.
- **Verification:** `mvn -f backend/pom.xml -ntp clean verify` passed with **893 tests** (0 failures, 0 errors, 0 skips), execution time 02:25 min. `git diff --check` passed clean (0 violations).

### Antigravity re-review 2 remediation submission (2026-10-06; claims retained)

Re-review 3 accepts FR14-2/FR14-5 fixes, but does not accept full FR14-4 closure. The following submission is retained
as reported; the current formal review and test-only remediation above take precedence.

- **FR14-2 Resolved (High):** `ApiExceptionHandler` now throws `IllegalStateException("Committed response transfer aborted: ...")` when `response.isCommitted()` is true, causing the container (`CoyoteAdapter` / servlet engine) to reset and drop the TCP transport connection rather than completing chunked transfer with `0\r\n\r\n`. Streaming exceptions in `ImageController` and `PortabilityController` wrap downstream exceptions into privacy-safe `IOException`. Verified by `realHttpClientDetectsTransferFailureOnUnknownLengthChunkedStream` in `MediaStorageIntegrationTest` via real standalone Tomcat with JDK `HttpClient` streaming unknown-length chunked output interrupted mid-stream, confirming client `IOException`, 2 closed streams, and zero sensitive log leakage.
- **FR14-4 Resolved (High):** Completed all missing accepted testing contract scenarios:
  - Commit-time positive reconciliation (`commitTimeFailureReconciliationPositiveSucceeds`) and mismatch reconciliation (`commitTimeFailureReconciliationMismatchRetainsObjectAndRethrows`).
  - Concurrent duplicate-checksum upload race (`duplicateChecksumUploadCompensatesLoserRetainsWinnerAndLeavesNoPartialState`).
  - Trashed vault image binary retention in storage (`vaultImageTrashDoesNotDeleteBinaryInStorage`).
  - Read-only zero-mutation export verification (`exportIsStrictlyReadOnlyAndZeroMutations`).
  - Trashed and history records preservation in export (`exportArchivePreservesTrashedAndHistoryRecords`).
  - Temporary archive spool file cleanup on success (`exportTemporaryArchiveCleanupOnSuccess`) and stream failure (`exportTemporaryArchiveCleanupOnStreamFailure`).
  - Large-number and nested JSONB export fidelity (`exportArchivePreservesLargeNumberJsonbDirectly`).
  - Disabled storage upload rejection (`disabledStorageRejectsUpload`).
  - Invalid enabled storage configuration validation (`invalidEnabledStorageConfigurationFailsValidation`).
  - Real HTTP Actuator health outage degradation (`storageOutageDegradesReadinessEndpointWhileLivenessRemainsHealthy`, verifying HTTP 503 readiness DOWN vs HTTP 200 liveness UP).
  - Multipart parser and size limits (`maxUploadSizeExceededReturnsCanonical413` and `malformedMultipartRequestReturnsCanonical400`).
- **FR14-5 Resolved (Medium):** Corrected OpenAPI schema mapping for upload 400 and 413 to `application/json` `$ref: '#/components/schemas/ErrorResponse'` in `OpenApiConfiguration` and `ImageController`. Verified by `OpenApiRouteInventoryIntegrationTest` and contract probe.
- **Verification:** `mvn -f backend/pom.xml -ntp clean verify` passed with **908 tests** (0 failures, 0 errors, 0 skips), execution time 02:41 min. `git diff --check` passed clean (0 violations).

### Antigravity test-slice submission (2026-10-06; claims retained)

- Test/evidence-only workflow reports 916 full tests, zero failures/errors/skips, 02:50 elapsed,
  finished 2026-10-06T16:09:52+07:00; focused command reports 45 tests (33 Media + 11 Portability + 1 OpenAPI), 51.31 s.
- Details are retained in [test evidence section 9](../phase-14/test-evidence.md#9-antigravity-test-slice-remediation-submission-fr14-4-closure).
- Re-review 4 accepts actual read-only/audit, exact JSONB, committed-export abort, concurrent uploads, generated size
  limit and enabled-invalid startup improvements. It rejects full FR14-4 closure; the formal review/remediation above
  takes precedence over unsupported claims.

### Antigravity test-slice submission 2 (2026-10-06; claims retained)

- Reports full command green: 917 tests, zero failures/errors/skips, 02:49 elapsed,
  finished 2026-10-06T17:20:03+07:00; focused command 46 tests, 52.27 s, finished 2026-10-06T17:17:09+07:00.
- Details in [test evidence section 11](../phase-14/test-evidence.md#11-antigravity-test-slice-remediation-submission-2-fr14-4-full-remediation).
- Re-review 5 accepts commit-boundary, real parser/error-contract, configuration/spool, race aftermath and actual
  image-hook/early snapshot failure improvements. Remaining arbitration/active-consumption lifecycle proof is not
  accepted; the formal report and narrowed remediation above take precedence.

### Antigravity test-slice submission 3 (2026-10-06; accepted with evidence qualifications)

- Clean verify passed: **920 tests**, zero failures/errors/skips, 03:23 elapsed, finished 2026-10-06T18:24:44+07:00.
- Focused command passed: **49 tests** (33 Media + 15 Portability + 1 OpenAPI), 0 failures/errors/skips, 01:06 min, finished 2026-10-06T18:21:07+07:00.
- Remediated FR14-4 arbitration gap: dynamic proxy holding `checksumPrecheckBarrier` on `ImageRepository.findByChecksumSha256` guarantees both concurrent threads observe empty precheck before either insert runs; verified `loserErr.getCause()` is `DataIntegrityViolationException` containing `images_checksum_sha256_key`. Zero orphan Vault rows.
- Remediated FR14-4 active-consumption lifecycle gap:
  - Added controlled query-cancellation (`SQLException` 57014) and database connection-loss (`SQLException` 08006) faults at active `ResultSet.next()` calls, asserting nonzero cursor work (`rowsRead >= 1`), tracked ResultSet/PreparedStatement/Statement close calls, transaction rollback, connection release and temp ZIP deletion. These are injected cursor faults, not physical network cancellation; retained HTTP tests separately prove client-visible transfer aborts.
  - Added full successful stream release verifying 100% cursor closure, commit, connection release, and archive retention.
  - Expanded `imageDownloadStreamLifecycleClosesResourcesOnSuccessFailureTimeoutAndDisconnect` to 8 paths covering actual registered `CallableProcessingInterceptor.afterCompletion` and captured `AsyncWebRequest` completion, timeout and error handlers. These directly invoked callbacks prove application cleanup branches, not real container timing.
- Full details in [test evidence section 13](../phase-14/test-evidence.md#13-test-only-remediation-pass-3--fr14-4-deterministic-arbitration--active-row-consumption-lifecycle).

## Final review

- Date: 2026-10-06
- Verdict: **READY FOR OWNER COMMIT** — FR14-1–FR14-9 closed; no blocking findings.
- Report: [Phase 14 final Codex acceptance](../phase-14/reviews/2026-10-06-phase-14-final-codex-acceptance.md).
- Independent verification: `mvn -f backend/pom.xml -ntp clean verify` passed 920 tests, zero failures/errors/skips;
  02:50 elapsed, finished 2026-10-06T18:33:48+07:00. Focused Media/Portability/OpenAPI command passed 49 tests,
  54.884 s, finished 2026-10-06T18:36:49+07:00.
- Next: owner commits/pushes using the formal acceptance's commit message, then gives the latest package to
  ChatGPT for Phase 14 closeout/Phase 15 preparation. Not yet committed/frozen; no milestone review due at Phase 14.
