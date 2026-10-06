# Phase 14 Final Codex Re-Review 1

- Date: 2026-10-06
- Handoff: `phase-14-portability-storage`
- Review mode: implementation; entered at `IMPLEMENTED_AWAITING_CODEX_REVIEW` after Antigravity remediation
- Owner baseline: `6f1fd00d89a2d0674677cf4c38b814bc434ec253` (HEAD and `origin/main`)
- Verdict: **CHANGES_REQUESTED**
- Open findings: **5 High, 1 Medium**

Independent clean verification passed all 891 tests. The remediation improves negative reconciliation, stream cleanup,
ordinary numeric/time fidelity, multipart errors, architecture guards and package hygiene. However, targeted probes
still reproduce false pre-commit upload success, undetectably successful truncated HTTP delivery, JSONB type corruption
and private diagnostic logging. The green suite does not prove the submitted claim that the entire testing contract
is complete. Phase 14 is not accepted/frozen; owner implementation commit/push must wait.

The [initial final review](2026-10-06-phase-14-final-codex-review.md) remains unchanged historical evidence.
Production Java paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`;
test paths are relative to `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

## Finding disposition

| Finding | Severity | Disposition | Current scope |
| --- | --- | --- | --- |
| FR14-1 | High | Open, narrowed | Enforce committed metadata success/no ambient upload transaction; compensate known validation rollback |
| FR14-2 | High | Open, narrowed | Abort failed transfers instead of completing truncated chunked responses normally |
| FR14-3 | High | Open, narrowed | Preserve JSONB types/numbers beyond the mapper's parser limits |
| FR14-4 | High | Open | Actual export concurrency and remaining required failure/boundary tests; accurate evidence |
| FR14-5 | Medium | Open, narrowed | Document 413 and strictly verify binary schemas/contracts |
| FR14-6 | — | Closed | Uncertain PUT now probes and compensates a positively known attempt before metadata creation |
| FR14-7 | — | Closed | Manual procedure now quiesces requests and checks all pending transactions, failing closed |
| FR14-8 | — | Closed | Five meaningful package descriptors and actual package-tree delta present |
| FR14-9 | High | New | Raw SQL/provider exception messages reach application logs |

Closed aspects of FR14-1–FR14-5 are not requested again: the uploader now retains on negative ambiguous reconciliation
and checks checksum/size positively; the delayed-commit test actually invokes the uploader with PostgreSQL/MinIO;
pre-commit errors and committed JSON suppression pass; async-rejection cleanup is covered; the originally demonstrated
decimal/fractional-time values are preserved; missing multipart file now returns canonical 400; explicit binary
annotations and nonempty-content assertions are present. Architecture guards now check Spring Data assignability,
Media-only SDK dependencies and JDBC confinement inside Portability.

## Open findings

### FR14-1 — High — Upload can succeed before metadata commit; known validation rollback leaves its object

**Locations:** `media/internal/application/storage/ImageUploadService.java:128` and `:129`;
frozen `media/internal/application/ImageService.java:38` uses the existing REQUIRED transaction semantics.

The uploader neither rejects nor suspends an ambient transaction. Calling `imageOperations.create` from one joins
that transaction, so its normal return does not mean commit completed. Positive reconciliation is likewise not
guaranteed independent of an outer transaction. The narrow exception whitelist also misses `AlbumNotFoundException`,
the exact failure exercised by the claimed confirmed-rollback test.

**Observed using the actual Spring upload service, Image operations, JPA transaction manager, PostgreSQL and MinIO:**
an outer `TransactionTemplate` called upload and received a successful `ImageView` while a fresh connection saw zero
committed rows. Rolling back the outer transaction left zero rows and an uploaded object. A separate missing-album
upload raised `AlbumNotFoundException` after confirmed validation rollback, but retained its object with zero rows.
Current HTTP callers ordinarily have no outer transaction; this probe demonstrates that the accepted use-case boundary
does not enforce its explicit no-ambient/commit-before-success contract, not that normal HTTP successes all fail.

**Required correction:** enforce the supported transaction boundary before remote I/O. A narrow rejection of ambient
transactions is acceptable; do not reopen frozen Image semantics. Metadata normal return must mean completed commit;
reconciliation must be a fresh independent committed read. Classify known validation rollback correctly and compensate
only this attempt's object. Retain the now-correct conservative handling of ambiguous outcomes. Add actual Spring-path
tests for ambient invocation, confirmed success/rollback, positive reconciliation and commit-time failure classification.

### FR14-2 — High — Swallowed post-commit failure presents truncated chunked content as a completed download

**Locations:** `media/internal/web/controller/ImageController.java:145`–`:147`;
`portability/internal/web/controller/PortabilityController.java:79`–`:81`; root `ApiExceptionHandler.java:191`.

The controller catches a committed streaming exception and returns normally. Suppressing JSON is necessary, but
normal callback completion is not a transport abort. The generic committed-response handler also resolves failures
by returning null. Legacy Image metadata may omit size; the controller ignores the provider result's known length.

**Observed over real HTTP, with the compiled controller on temporary Tomcat 11.0.24 and JDK HttpClient:** metadata
size was null, the storage result supplied 32,768 bytes, and its stream failed after 16,384. The client completed
normally with HTTP 200, `Transfer-Encoding: chunked`, no `Content-Length` and 16,384 bytes. The input closed and no
JSON was appended, but the transfer was presented as a successful, complete HTTP body despite the read failure.

**Required correction:** preserve post-commit failure propagation/transport abort without JSON or response reset;
do not swallow it as normal completion, including in exception advice. Use known length appropriately, but a length
header alone does not replace correct abort behavior for unknown-length responses. Test actual HTTP client detection
of truncated/failed transfers plus resource release, timeout/cancellation/disconnect and ordinary completion.
Retain the now-correct pre-commit canonical JSON and async-rejection cleanup.

### FR14-3 — High — Valid JSONB exceeding parser limits silently changes from object to string

**Location:** `portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java:447`–`:451`.

BigDecimal configuration fixes ordinary fractional precision, but `readTree` retains mapper parser constraints. When
parsing fails, the catch-all returns the complete JSON text as a Java String; the row serializer then quotes it.
This silently changes the portable field's type instead of preserving the retained JSONB value.

**Observed through an actual generated archive using PostgreSQL:** inserting `{"n":1e1100}` into
`import_job_items.parsed_payload` succeeds. PostgreSQL renders its number with 1,101 digits. The exported JSONL field
`parsed_payload` is a textual node, not the original object. No export error is reported.

**Required correction:** preserve JSONB types and exact numeric values across PostgreSQL-valid input boundaries;
remove the silent whole-value string fallback. Use an appropriately constrained precision-preserving representation
or validated raw JSON emission without whole-vault buffering. Failures must never produce a supposedly lossless ZIP
with coerced field types. Test the actual archive with parser-boundary numbers and nested JSON, retaining the passing
ordinary decimal/fractional-time, explicit-null, UTC, enum and Markdown regressions.

### FR14-4 — High — Required tests remain absent or prove another execution path; evidence overclaims closure

**Locations:** `portability/PortabilityIntegrationTest.java:257`; `media/MediaStorageIntegrationTest.java:432`, `:540`,
`:593`; `docs/implementation/phase-14/test-evidence.md`; active handoff implementation result.

- Snapshot concurrency now proves PostgreSQL REPEATABLE READ on a manually created connection. It commits that
  connection before calling the adapter, then asserts only archive existence/size. It does not establish the actual
  export snapshot, mutate concurrently with archive consumption, or compare archive content; weakening the adapter
  to READ COMMITTED would not make this test detect the mixed-snapshot behavior it claims to cover.
- Confirmed rollback still asserts only HTTP 404. Its comment/evidence incorrectly calls the missing-album validation
  a foreign-key failure. The real probe confirms its object remains. Failed compensation still invokes the delete
  helper directly, proving three calls but not primary-error preservation/privacy through upload failure handling.
- The test named probe failure supplies `exists=false`, not a throwing/unavailable probe. Readiness/liveness outage
  coverage instantiates only a health indicator and asserts DOWN; it does not request the actual readiness/liveness
  endpoints under outage, despite the submitted claim.
- Required commit-time/positive-reconciliation, duplicate-checksum race, timeout/cancellation/disconnect, large-stream/
  size-bound, enabled-invalid-config, disabled upload, retained trash/history, read-only enforcement and resource-
  cleanup assertions remain missing. The post-commit test asserts JSON absence, not a failed/truncated client transfer.
- Exact full command/results are reported correctly, but separate claimed focused command invocations are not recorded.
  The narrative says comprehensive remediation/entire accepted testing contract when the source does not establish it.

**Required correction:** complete the existing handoff testing contract with deterministic actual-path assertions.
For export concurrency, synchronize after the adapter establishes its own snapshot, commit mutations from another
transaction while the archive is consumed, and inspect its internally consistent content. Prove rollback cleanup,
primary failure/privacy and operational/resource boundaries rather than labeling simpler checks as those guarantees.
Correct evidence to commands actually run, exact assertions/results and any still-unproven risk. Preserve the genuine
891-test result, baseline assertions and historical reviews. Run focused regressions and clean verification after fixes.

### FR14-5 — Medium — Runtime 413 remains undocumented; binary inventory accepts non-binary string schemas

**Locations:** `OpenApiConfiguration.java:173` onward; root `ApiExceptionHandler.java:153`;
`web/OpenApiRouteInventoryIntegrationTest.java:545`–`:546`.

Multipart handlers now map missing/invalid parts to 400 and oversize to 413, but the actual generated upload operation
does not document 413. The binary assertion uses `format == binary OR type == string`, so an ordinary string without
binary format passes despite its diagnostic claiming both properties are required. It also does not insist on the
specific approved binary media type.

**Observed:** fetching `/v3/api-docs` from the actual Spring test application returned no 413 response on
`POST /api/v1/images/upload`. Missing-file runtime 400 is confirmed by the green integration regression.

**Required correction:** document and exercise canonical multipart size/parsing responses, including 413, only where
appropriate. Require both binary schema properties and approved media types in the inventory; retain exact multipart
file schema/status/security checks and transfer-failure semantics. Preserve the original 211 route contracts.

### FR14-9 — High — Remediation adds raw SQL/provider exception messages to logs

**Locations:** `media/internal/application/storage/ImageUploadService.java:147`;
`media/internal/web/controller/ImageController.java:146`;
`portability/internal/web/controller/PortabilityController.java:80`; root `ApiExceptionHandler.java:192`.

These warnings include `getMessage()` from reconciliation or transfer exceptions. Such messages can contain SQL,
object keys, bucket/endpoint/provider detail or private values. The approved logging contract explicitly forbids
these; the root handler previously logged only the exception class for unexpected failures.

**Observed with synthetic sentinels only:** a Logback capture of the actual uploader's reconciliation-failure branch
contained the raw injected private-SQL/key marker. The real HTTP failure probe also logged its raw provider-detail
marker. No owner data/secrets were used or inspected.

**Required correction:** emit generic outcome/correlation identifiers or safe exception classifications only, never
raw message/cause/stack/provider/SQL diagnostics. Add failure-path log captures with synthetic sensitive markers and
assert absence while preserving meaningful operational warnings and primary-error classification. Keep the frozen
constraint-log privacy protections intact.

## Independent verification and review coverage

- `mvn -f backend/pom.xml -ntp clean verify` — exit 0, **BUILD SUCCESS**, 891 tests, zero failures/errors/skips;
  02:23 elapsed, finished 2026-10-06T13:27:02+07:00. All 81 Surefire XML reports contain 891 testcase elements.
  Media 14, Portability 5, architecture 35, schema manifest 2 and OpenAPI inventory 1 all passed independently.
- `git diff --check` passed after review/status synchronization. All 101 local Markdown links across 11 reviewed
  governance/evidence/tree files resolve; new review/evidence whitespace checks pass. No new migration, frozen schema/
  DBML edit or protected `.agents` tooling change was found; HEAD and `origin/main` remain `6f1fd00`.
- No production/test Java or dependency/configuration implementation was changed by Codex. Standalone probe
  source/classes and Tomcat work folders live only under ignored `backend/target/final-review-diagnostics/`.
- Probes used temporary PostgreSQL 18.6/MinIO containers, the existing Spring integration context and actual
  Image/JPA services; synthetic port/repository faults were used only to exercise failure branches. Real local HTTP
  used temporary Tomcat plus JDK HttpClient. Contexts, servers and owned test containers were closed/stopped.
- The initial diagnostic compile reported unresolved classes/dependency access and an `AccessDeniedException` for
  cached AspectJ; the approved rerun succeeded. The first complete probe, extended JSONB probe and final known-provider-
  length HTTP-only probe each exited 0. These are standalone diagnostics, not additional counted JUnit tests.

Diagnostic reconstruction:

```powershell
[xml]$rereviewReport = Get-Content backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.media.MediaStorageIntegrationTest.xml -Raw -Encoding UTF8
$rereviewClassPath = ($rereviewReport.testsuite.properties.property | Where-Object { $_.name -eq 'java.class.path' }).value
$rereviewClassPath = (Resolve-Path backend/target/test-classes).Path + ';' + (Resolve-Path backend/target/classes).Path + ';' + $rereviewClassPath
javac -proc:none -cp $rereviewClassPath -d backend/target/final-review-diagnostics backend/target/final-review-diagnostics/Phase14RereviewProbe.java
java -cp ('backend/target/final-review-diagnostics;' + $rereviewClassPath) Phase14RereviewProbe
java -cp ('backend/target/final-review-diagnostics;' + $rereviewClassPath) Phase14RereviewProbe wire-only
```

Business/transaction/transfer/fidelity correctness remains blocked by FR14-1–FR14-3; tests and truthful contracts by
FR14-4/FR14-5; security/privacy by FR14-9. Modulith ownership, leaf/provider isolation, frozen schema and three additive
routes remain intact. The Media port/adapter is a justified provider boundary; fixes need no new generic services,
events/outbox or inheritance hierarchy. Row/ZIP and binary disk/stream processing remains bounded rather than whole-
vault heap materialization; the initial review's extra count/export traversal observation is non-blocking, unchanged.
Package hygiene/manual recovery improved sufficiently to close FR14-7/FR14-8.

Build warnings include Lombok `sun.misc.Unsafe`, deprecated APIs (now also reported from `ApiExceptionHandler`) and
unchecked OpenAPI operations. Standalone diagnostics add Mockito dynamic-agent/CDS and Tomcat inspection-access
warnings. No IDE inspection was run; no warning-free claim is made.

## Next step

Run Antigravity `/antigravity-implement-handoff` for the six open findings, then `$codex-final-review` again. This is
not a test-only remediation. No commit message while blockers remain; agents do not commit/push. Accepted preparation,
the original final review and Phase 13 evidence remain unchanged.
