# Phase 14 — Codex Final Re-Review 4

- Date: 2026-10-06
- Reviewer: Codex
- Scope: active `phase-14-portability-storage` handoff, resubmitted after Antigravity's test/evidence-only pass.
- Verdict: **CHANGES_REQUESTED** — FR14-4 remains open (1 High); all eight other findings remain closed.
- Accepted preparation / HEAD / `origin/main`: `6f1fd00d89a2d0674677cf4c38b814bc434ec253`.
- No Phase 14 acceptance/freeze or commit message while the testing-contract blocker remains.

[Re-review 3](2026-10-06-phase-14-final-codex-rereview-3.md) and all earlier reports remain historical evidence.
The new 916-test submission is genuinely green, but several tests still do not execute their advertised risk paths.

## Accepted improvements / disposition

- FR14-1–FR14-3 and FR14-5–FR14-9 remain closed. No previously closed production defect is reopened.
- `exportIsStrictlyReadOnlyAndZeroMutations` now captures the actual adapter connection, asserts read-only and
  repeatable-read state, observes PostgreSQL SQLState `25006` on a forbidden write, and restores the savepoint.
  Sampled Note values and Vault created/updated timestamps remain unchanged, alongside 12 table counts. This closes
  the previously missing read-only enforcement / sampled row-and-audit assertions; it is not a whole-database diff.
- `exportArchivePreservesLargeNumberJsonbDirectly` now parses the exact target row and asserts an ObjectNode,
  exact `10^1100`, nested array/object types and exact high-precision decimal content. Its fidelity claims are supported.
- `realHttpClientDetectsTransferFailureOnCommittedExportArchiveStream` now drives the actual export service/adapter/
  controller through Tomcat and injects an output failure after flushing 8,192 bytes. A JDK client observes IOException,
  no trailing JSON error is received, and the actual generated temporary archive is deleted. This closes the missing
  committed-export transfer-failure regression, not generation timeout/cancellation/disconnect cleanup in general.
- Two uploads now actually run on concurrent workers using real PostgreSQL and MinIO; an upload barrier replaces
  the former sequential-only evidence. The new failed-compensation variant observes three delete attempts, the
  primary ImageConflictException, retained loser object and no loser Image row. Stronger database-race/Vault/key
  assertions are still required below; the concurrent calls are an accepted improvement.
- The 55 MiB generated InputStream runs through the actual uploader and triggers the configured 50 MiB spool bound
  without a whole-file byte-array fixture or `MultipartFile.getBytes()`. This closes the absence of a large streaming
  size-limit fixture. Its test does not assert stream closure or temporary-file deletion.
- ApplicationContextRunner now proves enabled Spring startup failure for blank bucket/region/access/secret and
  nonpositive size. These lifecycle cases are accepted; the claimed invalid endpoint case is not present.

## FR14-4 — High, narrowed: remaining execution-path tests and truthful evidence

The accepted [testing contract](../README.md#testing-contract) and [handoff](../../handoffs/ACTIVE.md#testevidence-contract)
remain authoritative. These are existing requirements, not new implementation scope.

### 1. Actual transaction-manager commit failure and partial-state assertions

File: `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java:813` and `:850`.

`actualTransactionalCommitTimeFailureWithPositiveReconciliationRetainsObject` calls `imageOperations.create(cmd)`
to successful completion, then throws a synthetic exception from a manually implemented ImageOperations wrapper.
It exercises real committed metadata plus positive reconciliation, but never makes commit fail.
`actualTransactionalCommitTimeRollbackRetainsObjectAndZeroPartialDbState` throws immediately without calling the
metadata operation or creating any transaction/row. It checks only an Image count, not Vault rows. Its name and
section-9 claim of actual rollback/zero Image-and-Vault state are unsupported.

Keep useful branch tests, but add faults at the actual transaction-manager/JDBC commit boundary around the real
metadata operation. Prove positive committed uncertainty and rollback/uncertainty handling, object retention or
compensation according to authoritative outcome, primary failure and no partial Image/Vault state. A method-level
throw before/after a successful call is not commit-boundary fault injection. Do not change frozen schema or production
semantics to construct the tests; test-level transaction/connection instrumentation is sufficient.

### 2. Deterministic PostgreSQL checksum arbitration / loser Vault rollback

File: `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java:887` and `:964`.

The CyclicBarrier synchronizes completion of remote upload, before `ImageService.create` and its checksum precheck.
It does not ensure both metadata transactions pass the precheck before either insert commits. The test can pass
when one create finishes and the other sees a known duplicate, without exercising PostgreSQL uniqueness arbitration.
The success variant checks the winner's Vault row, not absence of an orphan loser Vault row. The failed-delete
variant counts three attempts but does not record their keys or assert that the winner binary remains available.
Neither asserts the advertised fully downloadable winner in these new cases.

Synchronize/observe overlap at the metadata precheck/write boundary using test-only instrumentation. Verify the
database-arbitrated conflict, loser Image/Vault rollback, winner metadata and downloadable bytes, and that every
compensation attempt targets only the loser key, including all three failures. Bound worker waits and use finally/
try-with-resources cleanup; the current unbounded `Future.get()` and success-only `executor.shutdown()` can hang or
leave workers alive if the test fails before cleanup. Do not rely on scheduler timing as proof of a deterministic race.

### 3. Actual multipart parser and canonical size-limit error contract

File: `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java:1258` and `:1278`.

The malformed test puts bytes on a MockMvc MockHttpServletRequest, whose servlet Part collection is not populated
by a real container parser. Its expected “Required multipart part is missing: file” proves the missing-part path,
not malformed-byte parsing. It checks only Image counts, not storage invocations or partial Vault state.

The oversized test does use Tomcat multipart limits, but its GenericWebApplicationContext has no selected MVC
configuration/multipart resolver, and its body assertions accept a container HTML error page. An independent
diagnostic reproduces that exact shape: `413 text/html;charset=utf-8`, HTML true, canonical error false. Adding
configured MVC + StandardServletMultipartResolver with the actual controller/root advice instead yields
`413 application/json`, canonical `PAYLOAD_TOO_LARGE`, and no upload/storage calls. This identifies a test-harness
gap, not a newly reproduced production schema/advice defect; FR14-5 stays closed.

Drive malformed and oversized bytes through the selected container/resolver/MVC upload path, assert canonical
JSON status/code/envelope/privacy, and verify no upload/storage/metadata work and no partial Image/Vault rows.
Do not accept HTML 413 or an empty mock Part collection as proof of the accepted parser/error contract.

### 4. Actual image/export timeout, cancellation and resource lifecycle

File: `backend/src/test/java/com/vhvkhangg/personalprivatevault/media/MediaStorageIntegrationTest.java:1424`;
`backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java`;
large-upload test at Media `:1338`.

`imageDownloadStreamLifecycleClosesResourcesOnSuccessFailureTimeoutAndDisconnect` constructs two handwritten
StreamingResponseBody lambdas with try-with-resources. It invokes neither ImageController, ImageDownloadService nor
their async cleanup hooks, and triggers no timeout, cancellation or disconnect. It would still pass if application
cleanup were removed. Existing actual image transfer/rejection tests remain useful but do not close these missing paths.

No submitted test drives snapshot-generation failure/timeout/cancellation while observing statement/result-set/
transaction and ZIP/temp-file release. The new committed export transfer failure is accepted, but does not substitute
for those generation lifecycle cases. The large upload test asserts only the limit exception, not input closure or
spool deletion. Add bounded, synchronized execution-path tests against the chosen actual image async and export
temporary-file strategies. Observe successful and exceptional resource release, including timeout/cancellation and
simulated client disconnect; retain the now-accepted export transfer-failure and read-only tests.

### 5. Correct submission/evidence and finish the scoped configuration boundary

File: `docs/implementation/phase-14/test-evidence.md`, section 9; Media startup test at `:1377`.

Section 9 still claims actual commit failure/rollback, zero partial Image/Vault state, parser integration/canonical
413, application timeout/disconnect cleanup, large-upload spool cleanup, and invalid endpoint startup that its tests
do not prove. The rollback entry also says “positive reconciliation confirms 0 rows”; an absent lookup is not positive
reconciliation or rollback authority. Record the narrower coverage truthfully and add the missing invalid endpoint
startup boundary already requested in re-review 3. Keep exact focused/full commands, counts, environment and failures/
retries. Arithmetic `908 + 8 = 916` is not independent verification; this review now supplies an actual independent run.
Retain historical reports/submissions as reported history, without rewriting prior Codex evidence.

**Remediation remains test/evidence-only:** use Antigravity `/antigravity-test-slice`. If a test reveals a production
defect, report it and route through `/antigravity-implement-handoff` within the active scope; do not change production
in the test-only pass, weaken assertions or broaden the frozen-domain/schema exception.

## Independent verification

- `mvn -f backend/pom.xml -ntp clean verify` — exit 0, BUILD SUCCESS, **916 tests**, zero failures/errors/skips;
  **02:48** elapsed, finished **2026-10-06T16:16:44+07:00**. Before focused rerun, 81 Surefire XML reports contain
  exactly 916 testcase elements. Media 33, Portability 11, architecture 35, schema manifest 2 and OpenAPI 1 pass.
- `mvn -f backend/pom.xml -ntp "-Dtest=MediaStorageIntegrationTest,PortabilityIntegrationTest,OpenApiRouteInventoryIntegrationTest" test`
  — exit 0, BUILD SUCCESS, **45 tests** (33 Media + 11 Portability + 1 OpenAPI), zero failures/errors/skips;
  **54.030 s**, finished **2026-10-06T16:29:20+07:00**. 867 baseline + 49 Phase 14 tests is not full-coverage proof.
- Ignored `backend/target/final-review-diagnostics/Phase14Rereview4MultipartProbe.java`: javac and java exit 0.
  Synthetic real-HTTP comparison uses actual compiled ImageController/root advice and Tomcat 11.0.24; the submitted
  Generic context returns HTML 413, while configured MVC returns canonical JSON 413. Both have zero upload/storage
  mock calls; contexts/clients/servers close and the probe's temporary directories are removed in finally.
- Windows, Java 25.0.2, Spring Boot 4.1.1, PostgreSQL 18.6 and MinIO Testcontainers. Approved escalation permits cached
  dependencies/Docker access. No failed/cancelled Maven or diagnostic compile/run; two source-navigation path lookups
  were corrected. Standalone diagnostics are not counted as JUnit tests. The three checked focused-run containers stopped.
- Baseline hashes unchanged; no diff in frozen DBML/migrations/architecture/ADR or protected `.agents` paths.
  Codex edits review/governance/evidence docs and ignored diagnostics only, not production Java/tests/config/dependencies.
- `git diff --check` passes after documentation synchronization; 104 local links across 11 documents resolve,
  and their trailing-whitespace scan passes. Stale implementation-index next-step text is corrected to test-only.

Diagnostic reconstruction:

```powershell
[xml]$reviewWireReport = Get-Content backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.account.AccountIntegrationTest.xml -Raw -Encoding UTF8
$reviewWireClassPath = ($reviewWireReport.testsuite.properties.property | Where-Object { $_.name -eq 'java.class.path' }).value
$reviewWireClassPath = (Resolve-Path backend/target/test-classes).Path + ';' + (Resolve-Path backend/target/classes).Path + ';' + $reviewWireClassPath
javac -proc:none -cp $reviewWireClassPath -d backend/target/final-review-diagnostics backend/target/final-review-diagnostics/Phase14Rereview4MultipartProbe.java
java -cp ('backend/target/final-review-diagnostics;' + $reviewWireClassPath) Phase14Rereview4MultipartProbe
```

## Mandatory review dimensions / scope

The blocker remains test/evidence assurance, not a new reproduced production correctness defect. Previously reviewed
Image/Vault canonical mutations, conservative commit/compensation rules, privacy-safe transfer abort and strict
multipart/binary OpenAPI schemas remain intact. Leaf/read-only/provider ownership, frozen Schema v1, dependency
directions and original 211 HTTP contracts remain green under architecture/schema/214-route inventory and full tests.

No new speculative abstraction, duplicate business path, production coupling or measured performance defect was
found. Media's narrow port/adapter and bounded disk-backed export remain appropriate; the previous extra count scan
is unchanged/nonblocking. Generated input and read-only assertions improve concrete efficiency/persistence evidence;
their lifecycle gaps are specified above. Test workers should be bounded and cleanup unconditional.

Package descriptors/tree remain consistent with the reviewed Phase 14 delta; no tracked generated cache or new
frozen migration was found. Compiler warnings remain Lombok Unsafe, deprecated APIs and unchecked OpenAPI operations;
Mockito agent/CDS and Tomcat inspection-access warnings also occur. No IDE inspection or warning-free claim.
Current documentation is synchronized without modifying accepted preparation or historical reviews. Phase 14 is not
a milestone gate. No commit/push/tag/PR was performed.

## Next step

Run Antigravity `/antigravity-test-slice` for the narrowed FR14-4 test/evidence remediation, then `$codex-final-review`.
No owner commit/push or Phase 14 closeout yet. Production defects discovered by tests require the implementation route.
