# Phase 14 Final Codex Review

- Date: 2026-10-06
- Handoff: `phase-14-portability-storage`
- Review mode: implementation; entered at `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- Owner baseline: `6f1fd00d89a2d0674677cf4c38b814bc434ec253` (HEAD and `origin/main`)
- Verdict: **CHANGES_REQUESTED**
- Open findings: **4 High, 4 Medium**

The implementation remains inside the approved feature area, and independent clean verification passed. However,
passing the current suite does not establish the handoff's required failure/concurrency guarantees. The probes below
demonstrate data loss, binary/JSON mixing, resource leakage and export precision loss. Phase 14 is not accepted/frozen;
owner implementation commit/push must wait for remediation and Codex re-review.

Production Java paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`;
test paths are relative to `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

## Findings

### FR14-1 — High — Negative reconciliation still authorizes unsafe object deletion

**Location:** `media/internal/application/storage/ImageUploadService.java:138` (catch-all branch through line 146).

An exception outside `TransactionException` causes a fresh lookup, followed by compensation when the row is absent.
The comment calls this a confirmed non-commit outcome, but neither the exception category nor a negative lookup
proves rollback. This explicitly violates the accepted uncertain-commit contract. The positive branch also omits the
expected size check. Reconciliation failures can replace the original failure rather than preserving its classification.

**Observed:** the actual upload service was invoked with a synthetic `DataAccessResourceFailureException` while
Image/Vault writes remained uncommitted in real PostgreSQL. A separate connection saw zero rows; the service called
delete once. Committing the original writer then left one committed Image row with its binary already deleted.
The diagnostic used a recording storage port and real PostgreSQL, not a production network-fault simulation.

**Required correction:** classify the original metadata transaction's outcome explicitly. Default ambiguous/unclassified
failures to retention; automatic deletion requires authoritative rollback/abort evidence from that transaction boundary,
never absence, timeout or connection loss. Positive reconciliation must verify the exact attempt/key/checksum/size in
a fresh independent read; failed/inconclusive reconciliation retains the object and preserves a privacy-safe primary
failure. Add an actual upload-path delayed-commit regression plus confirmed commit/rollback and commit-time failure cases.

### FR14-2 — High — Committed download errors append JSON; rejected async work leaks the open stream

**Locations:** `media/internal/web/controller/ImageController.java:64` and `:83`;
`media/internal/application/storage/ImageDownloadService.java:26`; root `ApiExceptionHandler.java:94`.

The S3 stream is opened on the request thread and closed only inside the later `StreamingResponseBody` callback.
There is no cleanup when that callback is rejected/never runs. Read failures propagate to the generic JSON exception
handler without a response-commit guard.

**Observed:** a real MockMvc async dispatch of the compiled controller wrote 8,192 binary bytes, committed the response,
then failed its next read. Exception handling appended the canonical error envelope: body size became 8,309 bytes and
contained `INTERNAL_ERROR`. A separate executor-rejection probe completed the request but the already-open input
stream remained unclosed. Ordinary pre-commit download/export failures returned canonical JSON correctly.

**Required correction:** distinguish servlet pre-/post-commit failure handling explicitly. After commitment, abort the
transfer without JSON/error-body replacement; retain canonical JSON before commitment. Give opened object streams a
reliable lifecycle through async admission failure, timeout, cancellation and disconnect, not only callback execution.
Add actual async/committed-response regressions and resource-release assertions; preserve existing JSON routes.

### FR14-3 — High — Portable export is not loss-preserving for JSONB numbers and fractional time

**Location:** `portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java:400`, especially `:412` and `:433`.

The private default Jackson mapper parses JSONB fractional numbers as floating-point tree values. `java.sql.Time`
conversion through `toLocalTime()` drops PostgreSQL fractional seconds. Both affect approved retained data, including
JSONB frontmatter/provenance/configuration and `time` columns in the frozen schema.

**Observed with real PostgreSQL values and the adapter's conversion method:**

| Stored value | Exported value |
| --- | --- |
| `{"n":1234567890.1234567890123456789}` | `{"n":1.2345678901234567E9}` |
| `12:34:56.123456` | `12:34:56` |

**Required correction:** use precision-preserving JSON handling and type-aware temporal reads without truncation.
Keep the existing portable format, explicit nulls and UTC instant semantics. Test exact high-precision/nested JSONB,
decimal and fractional time fidelity through a generated archive, alongside enums, dates, timestamps and Markdown.

### FR14-4 — High — Required regression evidence is missing or does not exercise the claimed behavior

**Locations:** `media/MediaStorageIntegrationTest.java:161`, `:200`, `:250`;
`portability/PortabilityIntegrationTest.java:173`; architecture tests; `docs/implementation/phase-14/test-evidence.md`.

- The named snapshot concurrency test only creates an archive and checks file existence/size. It performs no concurrent
  mutation, synchronization at snapshot establishment, or comparison of before/after content.
- The delayed-commit test writes SQL and checks object existence, but never invokes upload outcome classification or
  reconciliation. It would pass with FR14-1's unsafe implementation unchanged.
- The confirmed-rollback test asserts HTTP 404, not deletion or absence of Image/Vault partial state. The failed-cleanup
  test calls `compensateObject` directly and proves only three method calls, not primary-error preservation/privacy.
- Byte equality reads an async MockMvc response without awaiting/dispatching completion. Streaming failure/timeout/
  cancellation/disconnect, large-stream resource bounds, checksum races, uncertain storage, invalid enabled config,
  unavailable readiness vs healthy liveness and retained trash/history/fidelity cases are not established by this suite.
- Architecture guards allow SDK types in any infrastructure package, inspect only selected signatures, and detect
  repositories only by annotation. They do not fully enforce Media-only SDK isolation, Spring Data repository absence
  and the sole approved cross-table JDBC adapter boundary.
- Evidence claims tests/guarantees not present in source (including disabled upload and full concurrency proof), omits
  exact focused commands, and misstates the wire DTO, error code and export operation ID. It also cites nonexistent
  ADR 0021 instead of ADR-0017. The verified total of 882 is genuine; the claimed comprehensive coverage is not.

**Required correction:** implement the entire accepted Phase 14 testing contract with meaningful assertions against
the real execution paths, using PostgreSQL and S3-compatible integration storage where required. Strengthen architecture
guards without weakening original baseline assertions. Correct evidence to exact executed commands, actual test names,
assertions, contracts, environment and results; distinguish untested risks from proven guarantees. Preserve Phase 13
historical evidence. Rerun focused suites and the handoff-required clean verification after all implementation fixes.

### FR14-5 — Medium — Multipart client errors become 500; binary OpenAPI checks accept absent schemas

**Locations:** root `ApiExceptionHandler.java:63` and `:94`; `media/internal/web/controller/ImageController.java:36`;
`portability/internal/web/controller/PortabilityController.java:40`; `web/OpenApiRouteInventoryIntegrationTest.java:537`.

A request missing the required multipart file raises `MissingServletRequestPartException`, which falls through to
the unexpected-error handler. The probe received HTTP 500 `INTERNAL_ERROR`, not a structural client-error response.
Known multipart limit/parsing failures also need deliberate mapping/tests rather than the generic fallback.
The binary inventory assertion explicitly allows absent/empty response content, and the ZIP annotation supplies no
explicit binary schema; a green inventory test therefore does not prove the required multipart/binary contract.

**Required correction:** narrowly map known multipart structural/size/parsing failures to truthful canonical JSON 4xx
contracts, document them consistently and test runtime behavior. Document/verify explicit file and binary response
schemas, approved content types/statuses/security and transfer-abort semantics. Do not accept missing/empty schemas
as binary-contract success or weaken the original 211 route entries.

### FR14-6 — Medium — Uncertain S3 uploads skip the required resolution/probe path

**Location:** `media/internal/application/storage/ImageUploadService.java:93` (storage upload catch).

Every upload exception is immediately rethrown. The attempt is never probed even when storage remains available;
the existing `MediaStoragePort.exists` is unused by this workflow. Metadata is correctly not created, but the promised
resolution/known-object compensation step is absent.

**Observed:** a recording port stored the object and then simulated a lost upload acknowledgement while its probe
remained available. The service performed zero probes and zero deletes, leaving the object unnecessarily unresolved.

**Required correction:** represent confirmed/uncertain storage outcomes and perform bounded, privacy-safe resolution
when possible before metadata creation. Compensate only this attempt's known object when metadata was never committed;
unavailable/inconclusive outcomes return failure with the explicitly accepted residual-orphan risk. Test successful PUT
with lost acknowledgement, unavailable probing and partial-state absence. Do not add jobs/outbox/schedulers.

### FR14-7 — Medium — Manual cleanup's activity filter cannot prove writer quiescence

**Location:** `docs/implementation/phase-14/test-evidence.md:108` (settle-active-transactions procedure).

The only supplied activity check filters `pg_stat_activity.query` by `%images%`. That is the current/last statement,
not the transaction's complete write history. An open Image/Vault writer can be idle in transaction after another
statement, or executing COMMIT/another statement, and disappear from this check while still able to commit.

**Observed:** the probe left Image/Vault writes uncommitted, executed `SELECT 1` on the original connection, and the
documented query filter no longer matched that still-pending writer. A fresh inventory/row lookup still saw no row.

**Required correction:** document a fail-closed privileged procedure that establishes quiescence of all original
managed-upload requests and metadata transactions, not a query-text heuristic. Settle/terminate and verify every
possible writer before fresh inventory plus immediate unreferenced-key recheck. If quiescence/outcomes cannot be
established, retain objects and defer deletion. Keep this manual, with no new cleanup platform or hard-delete API.

### FR14-8 — Medium — New populated packages lack descriptors; package-tree documentation is preparation-only

**Locations:** new `media/internal/application/storage`, `media/internal/infrastructure/storage`,
`portability/internal/application`, `portability/internal/infrastructure/snapshot`,
`portability/internal/web/controller`; `docs/repository/repository-package-tree.md:17`.

Only the top-level Portability descriptor was added. The new populated implementation packages lack the meaningful
`package-info.java` files required by repository instructions. The canonical tree still describes only a planned
future delta and omits the actual Media storage package organization.

**Required correction:** add focused package descriptors for actual meaningful packages and synchronize the canonical
tree with the implemented, not-yet-accepted Phase 14 working structure. Preserve historical baseline statements,
ownership and dependency directions; do not create speculative public interfaces or new modules.

## Verification and review coverage

- Independent command: `mvn -f backend/pom.xml -ntp clean verify` — **BUILD SUCCESS**, 882 tests, zero failures/errors/
  skips; 02:22 elapsed, finished 2026-10-06T12:30:14+07:00. The 81 Surefire XML files contain 882 testcase elements.
- `git diff --check` passed after review/status updates. All 92 local Markdown links across the 10 reviewed governance/
  evidence files resolve; HEAD and `origin/main` remain the accepted preparation baseline `6f1fd00`.
- No production Java, test sources, schema, migrations or dependencies were changed by Codex. The probe source/classes
  live only under ignored `backend/target/final-review-diagnostics/` and use synthetic data/temporary containers.
- Probe command reconstruction (classpath comes from the independent Surefire report):

```powershell
[xml]$reviewReport = Get-Content backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.media.MediaStorageIntegrationTest.xml -Raw -Encoding UTF8
$reviewClassPath = ($reviewReport.testsuite.properties.property | Where-Object { $_.name -eq 'java.class.path' }).value
$reviewClassPath = (Resolve-Path backend/target/test-classes).Path + ';' + (Resolve-Path backend/target/classes).Path + ';' + $reviewClassPath
javac -proc:none -cp $reviewClassPath -d backend/target/final-review-diagnostics backend/target/final-review-diagnostics/Phase14ReviewProbe.java
java -cp ('backend/target/final-review-diagnostics;' + $reviewClassPath) Phase14ReviewProbe
```

- The final probe run exited 0. The first compile attempt failed because of sandbox dependency access/relative
  classpath; an escalation was canceled during interruption, then rerun with corrected classpath. An intermediate
  probe exited 1 when its harness awaited a rejected task's nonexistent async result; the final harness explicitly
  captures that condition and checks cleanup after completion. These were diagnostic retries, not failed Maven suites.
- Business/transaction/failure correctness: blocked by FR14-1/2/3/6. Tests/evidence: blocked by FR14-4/5.
- Security/ownership: bearer protection, no raw provider types outside Media infrastructure in current production code,
  read-only leaf ownership and unchanged frozen schema are retained; unsafe cleanup still threatens data integrity.
- Maintainability/SOLID/patterns: the Media storage port/adapter is a justified provider boundary, not a speculative
  framework. The correctness defects need narrow fixes, not new global services/events/reconciliation infrastructure.
- Performance: database rows use fetch-size streaming and binaries disk/stream I/O, not whole-vault heap lists.
  Each table currently receives a full count scan and a second export scan, a concrete extra traversal for large
  retained datasets; consider deriving counts during export if convenient, without broadening this remediation.
- Compiler diagnostics observed: Lombok `sun.misc.Unsafe`, existing deprecated parser/test APIs and unchecked OpenAPI
  operations; probe Mockito dynamic-agent/CDS warnings. No IDE inspection was run and no warning-free claim is made.
- Scope/package/docs: approved three additive API operations and no schema mutation; descriptor/tree/evidence gaps and
  implementation status are addressed by the findings and the synchronized handoff/status docs.

## Next step

Owner runs Antigravity `/antigravity-implement-handoff` for FR14-1–FR14-8 implementation/test/documentation remediation,
then invokes `$codex-final-review` again. This is not a test-only slice. No commit message is provided while findings
remain; agents do not commit/push. Accepted preparation and its historical reviews remain unchanged.
