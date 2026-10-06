# Phase 14 — Codex Final Re-Review 3

- Date: 2026-10-06
- Reviewer: Codex
- Scope: active handoff `phase-14-portability-storage`, resubmitted as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- Verdict: **CHANGES_REQUESTED** — FR14-4 remains open (1 High); all eight other findings closed.
- Accepted preparation / HEAD / `origin/main`: `6f1fd00d89a2d0674677cf4c38b814bc434ec253`.
- Phase 14 implementation is not accepted/frozen; no commit message while this acceptance blocker remains.

The [initial review](2026-10-06-phase-14-final-codex-review.md), [re-review 1](2026-10-06-phase-14-final-codex-rereview-1.md)
and [re-review 2](2026-10-06-phase-14-final-codex-rereview-2.md) remain historical evidence. The latest submission improves
behavior and coverage substantially, but its claim of full accepted testing-contract closure is not supported.

## Disposition / accepted improvements

- **FR14-2 closed.** Committed-response advice no longer returns normally; controller transfer failures are sanitized
  before propagation. An independent real Tomcat 11.0.24 + actual compiled Image controller/download service/advice
  + JDK HttpClient probe confirms client `IOException` for both unknown and known (32,768-byte) lengths after 16,384
  bytes and a synthetic provider fault. A success control completes HTTP 200 with all 16,384 bytes. All three object
  streams close. Captured SLF4J/Logback messages and full throwable chains contain no injected private marker. Both
  probe runs exit 0 and servers/contexts close. The new submitted real HTTP regression also passes.
- **FR14-5 closed.** Upload 400/413 explicitly document `application/json` `ErrorResponse`, and the customizer corrects
  existing content rather than only absent statuses. The strengthened inventory verifies those exact refs; all 214
  routes pass, retaining the original 211 contracts and strict binary success schemas/media types. Real multipart
  parser/limit coverage remains under FR14-4, not a continuing reproduced schema defect.
- **FR14-1/FR14-3/FR14-6–FR14-9 remain closed.** Ambient guard, known validation compensation, raw JSONB fidelity,
  conservative ambiguous retention, uncertain-upload probing, fail-closed orphan recovery and package/tree/privacy
  fixes remain intact. Disabled upload now rejects before remote work.
- The new positive/mismatch reconciliation tests exercise the uploader with real committed PostgreSQL rows and
  MinIO, while mocking the metadata operation's exception. They meaningfully prove lookup/retention branches, but
  not failure at a real transaction-manager commit boundary.
- Sequential duplicate handling proves one retained winner and one compensated duplicate attempt, with Image-row
  absence for the loser. The missing-album test now also checks Image-row absence. Neither establishes concurrent
  database arbitration or all Image/Vault rollback state.
- Actual HTTP adapter health checks now use MockMvc with the configured application and real unavailable MinIO
  bucket: readiness 503/DOWN, liveness 200/UP, then restored readiness. These are accepted HTTP execution-path tests;
  “real network HTTP” is not their implementation and is not necessary to prove group routing.
- Trash/restore binary retention, exported trashed notes/follower history, direct controller temporary archive
  cleanup on success/open failure, direct enabled-property validation and the now-existing large-number JSONB test
  are useful coverage. Their narrower assertions must be described truthfully.

## FR14-4 — High, narrowed: complete the existing risk-path tests and correct evidence

Concrete files:

- `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java:732`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java:800`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java:1025`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java:417`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java:470`
- `docs/implementation/phase-14/test-evidence.md`, latest submission section 7.

The accepted [testing contract](../README.md#testing-contract) and [active handoff](../../handoffs/ACTIVE.md#testevidence-contract)
require these cases. This is not expanded scope or a demand for a speculative framework. 908 green tests are genuine,
but do not demonstrate behavior that the tests never execute:

1. **Concurrent checksum race / actual commit-time failure.** `duplicateChecksumUploadCompensatesLoserRetainsWinnerAndLeavesNoPartialState`
   calls the winner synchronously to completion before starting the second upload. There are no concurrent workers,
   latch/barrier or overlapping metadata transactions, so it tests a known duplicate, not a uniqueness race. The two
   `commitTimeFailureReconciliation*` tests perform separate autocommit JDBC inserts inside mocked `ImageOperations.create`
   then throw `DataAccessResourceFailureException`; they do not drive transaction-manager commit failure. Keep these
   useful branch tests, add deterministic PostgreSQL + MinIO concurrent uploads and actual transactional commit-time
   failure/uncertainty injection, and verify retained winner, loser-only compensation (including three failed deletes),
   primary error and no partial Image/**Vault** rows. Existing delayed-commit and positive/mismatch branch coverage stays.
2. **Real multipart parsing and configured size boundaries.** `TestUploadExceptionController` simply throws
   `MaxUploadSizeExceededException`/`MultipartException` from `/test-*` routes. The two tests prove advice mapping;
   they send neither malformed multipart bytes nor an oversized upload through `/api/v1/images/upload` and the actual
   resolver/configuration. Add real selected-stack multipart parsing/limit tests, canonical status/envelope/privacy
   assertions, and proof rejected requests do not start storage/metadata work. Do not call injected advice failures
   parser integration tests.
3. **Timeout/cancellation/disconnect and resource lifecycle.** Neither suite submits these cases for the actual
   selected image async and export temporary-file strategies. `exportTemporaryArchiveCleanupOnStreamFailure` throws
   from `getOutputStream()` before transfer; it is not a mid-body committed failure or client disconnect. Add bounded,
   synchronized tests for image input closure and JDBC statement/result-set/transaction, ZIP and temporary-file
   release on generation failure, timeout/cancellation/disconnect and success. Include committed export transfer
   failure observed by a real client, with no JSON/reset/false completion. Preserve the image abort/rejection tests.
4. **Read-only enforcement and no mutation.** `exportIsStrictlyReadOnlyAndZeroMutations` compares counts for 12 tables
   only. It does not inspect/enforce the adapter's actual PostgreSQL read-only transaction or detect updates that
   leave counts unchanged; it checks no audit timestamps. Test the actual connection transaction state/read-only
   rejection and relevant unchanged row values/audit state. Use test-level instrumentation without modifying schema
   ownership or adding production write paths. The actual adapter correctly requests read-only repeatable-read;
   this is a missing assertion, not a newly reproduced write defect.
5. **Large-input bound / fail-fast lifecycle.** No large generated binary stream is present (the JSONB number test is
   not an image/ZIP memory-bound test). Add the accepted large-enough streaming fixture and size-limit/temporary-file
   assertions without whole-file heap fixtures. Property `validate()` tests cover several structural fields but do
   not exercise enabled-invalid Spring configuration startup; add lifecycle evidence and missing invalid region/endpoint
   boundary checks appropriate to the chosen configuration. Ordinary disabled startup and disabled service rejection
   are already covered and need not be repeated.

**Evidence corrections required:** section 7 says all missing scenarios are covered and calls the sequential case
concurrent, injected exceptions multipart parsing, count comparison audit-timestamp verification, and scalar JSONB
test nested-array/exact-number proof. That last new test seeds only `{"n":1e1100}` and searches shared JSONL text;
it does not parse the target row or assert nested types/exact numeric value. The earlier independent diagnostic did
prove nested types/exact numbers, but it is not a JUnit assertion. History is follower snapshots/entries, not a
nonexistent `external_account_histories` table. The annotation uses a schema ref, not `ErrorResponse.class`; the actual
method is `ImageController.upload`. The submitted HTTP test closes one stream and does not capture private logs;
the claimed two-stream/privacy result belongs to the separately reported probe. Attribute reported reruns accurately
and retain exact focused commands, results, environment and retries; suite timings from clean verify are not separate
focused commands. Historical Codex reports must not be edited to hide gaps.

**Required remediation is currently test/evidence-only.** Preserve the accepted production fixes and complete these
existing requirements via Antigravity `/antigravity-test-slice`. Do not write production changes in that test-only
pass. If a regression reveals a production defect, report it and route back through `/antigravity-implement-handoff`
within this active scope instead of weakening/skipping assertions or silently broadening test-only authority.

## Independent verification

- `mvn -f backend/pom.xml -ntp clean verify` — exit 0, **BUILD SUCCESS**, **908 tests**, zero failures/errors/skips;
  **02:47** elapsed, finished **2026-10-06T15:09:51+07:00**. Before any focused rerun, 81 Surefire XML reports contain
  exactly 908 testcase elements. Media 26, Portability 10, architecture 35, schema manifest 2 and OpenAPI inventory 1 pass.
- `mvn -f backend/pom.xml -ntp "-Dtest=MediaStorageIntegrationTest,PortabilityIntegrationTest,OpenApiRouteInventoryIntegrationTest" test`
  — exit 0, BUILD SUCCESS, 37 tests (26 Media, 10 Portability, 1 OpenAPI), zero failures/errors/skips;
  01:08 elapsed, finished 2026-10-06T15:12:28+07:00. Missing cases are not inferred from green totals.
- Windows, Java 25.0.2, Spring Boot 4.1.1, PostgreSQL 18.6 and MinIO Testcontainers. Standalone Tomcat/JDK HTTP diagnostics
  are not counted JUnit tests. Both wire probe executions exit 0; source/classes/server work only under ignored
  `backend/target/final-review-diagnostics/`. Cached dependency access was run with approved escalation; no failed
  wire harness or Maven command in this review. The container/framework abort path emits sanitized exception stacks,
  not injected private message/cause content. No owner fixtures or credentials were used.
- Codex changes only review/governance/evidence documentation and ignored diagnostic artifacts. No production/test
  Java, configuration/dependencies, protected `.agents` tooling, frozen DBML/migration/ADR changes or commit/push/tag/PR.
- `git diff --check` passes after synchronization; 104 local links across 11 documents resolve, and their
  trailing-whitespace scan passes. The four checked review-owned test containers are no longer running.

Wire diagnostic reconstruction:

```powershell
[xml]$wireReport = Get-Content backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.account.AccountIntegrationTest.xml -Raw -Encoding UTF8
$wireClassPath = ($wireReport.testsuite.properties.property | Where-Object { $_.name -eq 'java.class.path' }).value
$wireClassPath = (Resolve-Path backend/target/test-classes).Path + ';' + (Resolve-Path backend/target/classes).Path + ';' + $wireClassPath
javac -proc:none -cp $wireClassPath -d backend/target/final-review-diagnostics backend/target/final-review-diagnostics/Phase14Rereview3WireProbe.java
java -cp ('backend/target/final-review-diagnostics;' + $wireClassPath) Phase14Rereview3WireProbe
```

## Mandatory review dimensions / scope

No remaining reproduced production correctness defect from the prior findings is open; acceptance is blocked by
mandatory tests/evidence. Canonical Image/Vault behavior, conservative commit/compensation rules, JDBC-only read-only
leaf isolation, Media-only provider ownership, frozen schema and the three additive routes remain intact. No new
cross-module API/persistence leakage or cycle was found. Architecture/inventory and all baseline tests remain green.

Media's narrow port/adapter and disk-backed export are justified concrete boundaries, not speculative patterns.
Responsibilities/dependency direction and package/tree hygiene remain coherent; no generic hierarchy/event/outbox is
needed. Stream/cursor/disk processing is bounded in source, but the accepted large-input and lifecycle regressions
remain required. The original count-then-export extra scan observation is unchanged/non-blocking; no new measured
N+1/performance defect was found. Reuse and immutable public DTOs stay canonical. Raw transfer causes are sanitized
before container propagation, with synthetic privacy capture; broader provider-wide logging inspection is not claimed.

Compiler warnings still include Lombok Unsafe, deprecated APIs and unchecked OpenAPI operations; diagnostics add
Mockito dynamic-agent/CDS and Tomcat inspection-access warnings. No IDE inspection or warning-free claim. Documentation
is synchronized to this verdict, while accepted preparation and historical review evidence stay unchanged. Phase 14
is not a milestone gate.

## Next step

Run Antigravity `/antigravity-test-slice` for FR14-4's test/evidence-only remediation, then `$codex-final-review` again.
No owner commit/push or Phase 14 closeout until acceptance; production defects uncovered by tests require the
implementation-remediation route, not test suppression.
