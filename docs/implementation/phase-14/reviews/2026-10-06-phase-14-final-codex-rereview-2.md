# Phase 14 — Codex Final Re-Review 2

- Date: 2026-10-06
- Reviewer: Codex
- Scope: resubmitted active handoff `phase-14-portability-storage`, received as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- Verdict: **CHANGES_REQUESTED** — FR14-2, FR14-4 and FR14-5 remain open (2 High, 1 Medium).
- Accepted preparation / HEAD / `origin/main`: `6f1fd00d89a2d0674677cf4c38b814bc434ec253`.
- Frozen Phase 13 baseline remains unchanged; Phase 14 is not accepted/frozen. No commit message while blockers remain.

The [initial review](2026-10-06-phase-14-final-codex-review.md) and
[re-review 1](2026-10-06-phase-14-final-codex-rereview-1.md) remain historical evidence. The latest Antigravity
submission's blanket closure claim is not accepted. This report and the synchronized active handoff take precedence.

## Finding disposition

| Finding | Severity | Disposition |
| --- | --- | --- |
| FR14-1 | High | Closed: actual ambient transaction rejected before remote I/O; missing-album validation compensation restored. |
| FR14-2 | High | Open, narrowed: controller propagates failure, but committed-response advice still resolves it as normal completion. |
| FR14-3 | High | Closed: raw PostgreSQL JSON emission preserves types and parser-boundary numbers. |
| FR14-4 | High | Open, narrowed: several meaningful regressions improved, but mandatory transaction/resource/configuration tests and evidence remain incomplete. |
| FR14-5 | Medium | Open, narrowed: binary success schemas and 413 status fixed; upload 400/413 schemas incorrectly describe success DTOs. |
| FR14-6–FR14-8 | Medium | Remain closed: uncertain-upload isolation, fail-closed recovery and package/tree hygiene retained. |
| FR14-9 | High | Closed: identified reconciliation/transfer/advice logs use safe classifications rather than raw diagnostics. |

## Accepted remediation

- `ImageUploadService.uploadImage` checks `TransactionSynchronizationManager.isActualTransactionActive()` before
  file/storage work. The actual Spring transaction-template regression passes. Outside an ambient transaction, the
  frozen proxied `ImageOperations.create` boundary completes commit before normal return. `AlbumNotFoundException`
  now joins known validation failures; the updated test calls actual Image/JPA services and MinIO and proves the
  uploaded attempt object is removed. Conservative ambiguous-outcome retention and delayed-commit protection remain.
  Broader transaction/race test gaps are recorded under FR14-4, not a continuing reproduced FR14-1 defect.
- Image streaming now propagates exceptions and closes its input; provider content length is used when metadata has
  no positive length. The real HTTP probe confirms the known-provider-length truncated response produces a client
  `IOException`. Pre-commit canonical JSON and async executor-rejection cleanup regressions remain green. This is
  partial FR14-2 remediation, not proof of unknown-length transport abort.
- `PortabilitySnapshotAdapter.RawJsonValue` writes database-valid JSON directly, removing the silent quoted-string
  fallback. An independent actual PostgreSQL/export ZIP probe verified `{"n":1e1100}` remains an object with an exact
  `10^1100` integer, nested array types and the full high-precision decimal text. Fractional-time handling remains.
- The snapshot-concurrency test now calls the actual adapter, commits writes after its snapshot-establishing query
  and inspects table JSONL plus Markdown; a fresh connection sees the excluded concurrent note afterward. The
  throwing storage probe and upload-path compensation/primary-error/max-three-retries tests are genuine improvements.
- The route inventory now requires both `type=string` and `format=binary` with approved success media types and
  checks upload 413 exists. All 214 route contracts pass without removing the original 211 entries.
- Raw exception-message logging at the four previously identified paths was removed. The uploader's synthetic
  Logback regression passes; the real image/advice HTTP diagnostic contains no injected private marker, while both
  opened streams close. Export warning source likewise logs classification only. Frozen constraint logging remains.

## FR14-2 — High: committed-response handler still completes failed unknown-length downloads normally

Files:

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/ApiExceptionHandler.java:191`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/media/internal/web/controller/ImageController.java:159`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/portability/internal/web/controller/PortabilityController.java:78`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java:268`

The controller now rethrows `IOException`, but `handleUnexpected` logs and returns `null` when the response is
committed. MVC treats this as a handled exception and completes the request normally. A warning saying “aborting”
does not abort the transport. Known length helps detect truncation but does not fix unknown-length delivery.

**Independent real HTTP reproduction:** temporary Tomcat 11.0.24, actual compiled Image controller/download service
and root exception advice, a synthetic faulting storage stream, and JDK HttpClient. The stream writes and flushes
16,384 bytes, then throws; both metadata and provider lengths are null. The client receives **normally completed
HTTP 200**, `Transfer-Encoding: chunked`, no Content-Length and precisely 16,384 bytes. No JSON is appended, but the
truncated download is indistinguishable from a successful complete body. With provider length 32,768, the same
failure is detectable as client `IOException`; both streams close and synthetic private diagnostics stay absent.

The replacement `downloadPostCommitErrorAbortsTransport` test invokes `StreamingResponseBody.writeTo` directly with
a mocked committed response. It proves callback propagation only: it does not dispatch through advice/container or
observe a client, and therefore passes while this defect remains.

**Required correction:** preserve committed transfer failure through the whole MVC/container path so the client
observes an aborted/incomplete transfer, including unknown-length content. Do not append JSON, reset the committed
response or leak raw diagnostics through the error path. Retain known-length fallback and cleanup. Add real HTTP
client regressions for the selected image/export strategies; do not accept only callback exceptions or absence of
JSON as proof of abort.

## FR14-4 — High: accepted testing contract remains incomplete and some evidence still overclaims

Files:

- `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java`
- `docs/implementation/phase-14/test-evidence.md`
- [mandatory testing contract](../README.md#testing-contract) and [active handoff](../../handoffs/ACTIVE.md#testevidence-contract).

The implemented suites contain 16 Media and 5 Portability tests, not comprehensive closure of the accepted contract.
The adapter-concurrency, throwing probe, actual missing-album compensation and primary-error regressions are
accepted. The following requirements remain unproved by the submitted tests:

- Actual metadata **commit-time** failure classification and positive fresh committed reconciliation with exact
  key/checksum/size; absent/mismatched/error outcomes retain the object. The delayed-commit case covers absence,
  but no positive/mismatch or real transaction-manager commit-failure case is present.
- Duplicate-checksum concurrent uploads against PostgreSQL plus MinIO, loser-only object cleanup, failed-compensation
  behavior, committed winner retention, and assertions that failed storage/metadata attempts leave no partial Image/
  Vault state. The missing-album test checks object absence but not both database rows.
- Image/export resources and temporary files after success, generation failure, timeout, cancellation and client
  disconnect through the actual selected execution strategy. Executor rejection is covered; lifecycle registrations
  alone do not prove the other paths. No client-level transport-abort test is submitted (FR14-2).
- Large-enough generated streaming input demonstrating no whole-file heap requirement, export read-only/no-mutation
  enforcement, retained trash/soft-delete/history rows, deterministic logical content and Vault trash/restore binary
  retention. Small happy-path ZIP/byte tests do not establish these boundaries.
- Invalid enabled storage configuration, disabled upload behavior, actual outage readiness/liveness HTTP responses,
  and multipart parsing/size-limit runtime canonical errors. Default-disabled contexts establish ordinary startup,
  but not these negative paths. `storageDownAffectsReadinessNotLiveness` still constructs a standalone indicator and
  asserts only `DOWN`; it does not call either health endpoint or assert liveness during storage outage.

**Evidence defects:** earlier broad coverage claims are explicitly historical but section 5 still marks everything
resolved, refers to nonexistent test `exportArchivePreservesLargeNumberJsonbDirectly`, and treats direct callback
throwing as a transport-abort test. The boundary seed was added to `exportArchiveProducesCompleteLosslessZip`, not
a separate test. Earlier “all tests execute against real PostgreSQL and MinIO” wording also does not describe the
mocked/direct-helper branches accurately. Green 893-test verification is genuine, but not proof of untested cases.

**Required correction:** finish the existing contract with focused actual-path tests, keep the accepted improved
regressions and all baseline assertions, and record exact executed commands/results/test names and any retries.
Describe mocks and integration boundaries truthfully; do not claim tests or closure that did not occur. No generic
reconciliation platform, new schema, frozen domain refactor or speculative architecture is required.

## FR14-5 — Medium: upload 400 and 413 describe successful image data rather than canonical errors

Files:

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/media/internal/web/controller/ImageController.java:40`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/OpenApiConfiguration.java:222`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java`

The new 400/413 annotations omit explicit error content. Springdoc infers the controller's successful return DTO;
the customizer fills error content only when a status is absent, so it leaves these wrong schemas intact.

**Observed from actual `/v3/api-docs`:** upload 400 and 413 each have
`content.*/*.schema.$ref = #/components/schemas/ApiResponseImageResponse`. Runtime framework size failure maps to
canonical `PAYLOAD_TOO_LARGE`/413, not an image DTO. The existing missing-file regression returns canonical
`MALFORMED_REQUEST`/400. A direct root-handler probe confirms 413 classification, not an actual multipart-parser test.
The inventory asserts that 413 exists but does not validate its error schema/media type, allowing this mismatch.

**Required correction:** document these upload errors as `application/json` canonical `ErrorResponse` envelopes
and make inventory assertions validate their content/schema, not status existence alone. Test actual parsing/size
failures and canonical runtime agreement under FR14-4. Retain the corrected binary success schemas and all 211
baseline contracts; do not rewrite unrelated Phase 13 routes.

## Independent verification / environment

- `mvn -f backend/pom.xml -ntp clean verify` — exit 0, **BUILD SUCCESS**, **893 tests**, zero failures/errors/skips;
  02:30 elapsed, finished **2026-10-06T14:06:11+07:00**. Immediately afterward, 81 Surefire XML reports contained
  exactly 893 testcase elements. Media 16, Portability 5, architecture 35, schema manifest 2 and OpenAPI 1 passed.
- `mvn -f backend/pom.xml -ntp "-Dtest=MediaStorageIntegrationTest,PortabilityIntegrationTest" test` — exit 0,
  BUILD SUCCESS, 21 tests (16 Media, 5 Portability), zero failures/errors/skips; 36.457 seconds, finished
  2026-10-06T14:13:07+07:00. These do not cover the absent mandatory cases; evidence remains qualified.
- Windows, Java 25.0.2, Spring Boot 4.1.1, real PostgreSQL 18.6 and MinIO Testcontainers for Maven integration tests;
  Tomcat 11.0.24/JDK HttpClient for wire behavior. Standalone diagnostics are not added to JUnit counts.
- Probe sources/classes/server work files are only under ignored `backend/target/final-review-diagnostics/`.
  The first wire compile failed on sandbox dependency access (including cached AspectJ); approved retry succeeded,
  exit 0. The first contract probe failed on harness-only missing MockMvc bean, then web-context-built MockMvc retry
  succeeded, exit 0. Synthetic fixtures only; contexts, servers and owned PostgreSQL containers closed/stopped.
- The wire transfer with known length waited for the server's connection close, then raised client `IOException`.
  A read-only process check found no remaining owned diagnostic JVM; no process was terminated.
- `git diff --check` passes after synchronization; all 102 local file links across 11 reviewed documentation files
  resolve, and new review/evidence trailing-whitespace checks pass. No production/test Java,
  config/dependencies or protected `.agents` instructions were edited by Codex; no commit/push/tag/PR was made.

Diagnostic reconstruction (use the wire/contract class names respectively):

```powershell
[xml]$probeReport = Get-Content backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.account.AccountIntegrationTest.xml -Raw -Encoding UTF8
$probeClassPath = ($probeReport.testsuite.properties.property | Where-Object { $_.name -eq 'java.class.path' }).value
$probeClassPath = (Resolve-Path backend/target/test-classes).Path + ';' + (Resolve-Path backend/target/classes).Path + ';' + $probeClassPath
javac -proc:none -cp $probeClassPath -d backend/target/final-review-diagnostics backend/target/final-review-diagnostics/Phase14Rereview2WireProbe.java
java -cp ('backend/target/final-review-diagnostics;' + $probeClassPath) Phase14Rereview2WireProbe
javac -proc:none -cp $probeClassPath -d backend/target/final-review-diagnostics backend/target/final-review-diagnostics/Phase14Rereview2ContractProbe.java
java -cp ('backend/target/final-review-diagnostics;' + $probeClassPath) Phase14Rereview2ContractProbe
```

## Mandatory review dimensions

Business/logical correctness remains blocked by FR14-2; contract/testing truthfulness by FR14-4/FR14-5. Transaction
guard, validation compensation and JSONB fidelity defects are closed. Modulith leaf/module/provider ownership,
approved read-only JDBC exception, physical schema/migrations and the three additive routes remain intact.

The Media port/adapter is a justified narrow boundary; no speculative hierarchy/event/outbox/framework is needed.
Existing domain behavior stays canonical. Row/cursor/ZIP and disk-spooled binary processing avoids whole-vault/file
heap materialization; large-input/resource tests are still required. The initial count-then-export extra traversal
observation remains non-blocking; no new measured performance defect or N+1 was found. The snapshot test seam and
raw JSON value are cohesive, though parser-read settings now unused by raw emission are optional cleanup, not blockers.
Package descriptors/tree and fail-closed manual recovery remain satisfactory. Identified raw diagnostic leakage is
closed, with synthetic source/log checks; no assertion of exhaustive provider-wide logging inspection is made.

Build warnings include Lombok `sun.misc.Unsafe`, deprecated APIs and unchecked OpenAPI operations. Standalone probes
add Mockito dynamic-agent/CDS and Tomcat inspection-access warnings. No IDE inspection was run or warning-free claim
made. Governance/evidence are synchronized to this verdict; accepted preparation and frozen/historical reviews remain
unchanged. Phase 14 is not a milestone gate.

## Next step

Run Antigravity `/antigravity-implement-handoff` for FR14-2/FR14-4/FR14-5, then `$codex-final-review` again. Production
remediation is required, not only a test-only pass. Owner commit/push and Phase 14 closeout remain gated on acceptance.
