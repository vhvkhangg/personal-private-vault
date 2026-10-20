# Slice 1 Import & Snapshot ? Codex corrective handback, 2026-10-20

**Status: IMPLEMENTED_AWAITING_INDEPENDENT_RE_REVIEW.** Codex takeover implementation authorship; independent acceptance is prohibited in this session. Owner authorization on 2026-10-19 is exactly one corrective scope for S1-01?S1-07, not a reset of exhausted Antigravity rounds. See [ACTIVE](../handoffs/ACTIVE.md), [handoff](slice-1-import-snapshot-handoff.md), [independent findings](reviews/2026-10-19-slice-1-import-snapshot-independent-review.md), D12/D13 and ADR-0023.

Baseline: `main`, HEAD `edac5f466935fdd29698acbe89d4f129efce1701`. Submitted tracked/untracked Antigravity implementation and Codex review/status changes were inspected and preserved. No reset, commit, push, tag or PR. Historical [Antigravity handback](slice-1-import-snapshot-handback-2026-10-19.md) and all independent reviews remain unchanged; their unsupported lifecycle signoffs are superseded only by the actual corrective evidence below, not retroactively accepted.

## Correction map

| Finding | Smallest correction and required evidence |
| --- | --- |
| S1-01 | Exact authenticated snapshot preview (pending R/hash/length, selection and both authority contexts) echoed by update/approval; common guarded CAS before and after integrity work; persisted pending consent association. HTTP A?B?stale-A denial, verification/replacement barrier, stale selection/reset/authorization tests. |
| S1-02 | First-import-only registration; no silent path-based update. First imports serialize final absence/catalog checks. All existing-S updates/approval use graph ? owner/source guards ? refreshed policy CAS. Register/register and capture/register/approve/revoke/competing-capture barriers, reservation denial and rollback assertions. |
| S1-03 | Existing startup/scheduled recovery now sweeps at most 25 filesystem entries and 25 metadata rows per tick; no full inventory materialization or orphan adoption. Exact post-commit retirements recheck authoritative references using a fresh transaction/graph lock. Final metadata changes and recovery deletion/denial also recheck the existing runtime owner under that same lock. Temp cleanup always releases its active marker even if cleanup itself throws. Failed deletes retain physical UUID inventory for durable retry; unknown outcomes resolve through fresh DB references. Registered integrity uncertainty persists denial. Tests inject post-seal metadata failure, replacement rollback, committed/rolled-back unknown outcome; real Windows inaccessible deletion plus fresh store/recovery startup, orphan/temp/partial manifest, missing registered pair and revoke during copy. |
| S1-04 | Independently verify registered pending and selected descriptors before equality; corruption persists denial and is never treated as changed bytes. Valid same-byte capture reuses R; valid changed bytes create pending R. Missing orig/manifest, partial JSON and same-length tampering tests. No final NO_CHANGE outcome. |
| S1-05 | Finite context spans request capture/seal/pre-commit; 60s default (1?120s), timed asynchronous regular-file reads, interruption and committed revoke cancellation. Checks mid-copy/pre-seal/pre-commit; temp cleanup and no promotion. Tests include slow/failed read, actual mid-copy revoke/mutation/cancel, post-copy cancel, pre-commit cancel/deadline, Windows junction and unsupported provider/UNC paths. |
| S1-06 | Native no-replace move retained; fallback uses no-clobber hard-link publication of a closed verified temp, then removes temp. Unsupported link fails closed. Forced-native-unavailable orig-only collision preserves exact target bytes; normal seal and manifest collision tested. |
| S1-07 | Acquisition DEBUG records only random capture ID/size; private locator and stable hashes removed from new error paths and store digest mismatch messages. Request/snapshot diagnostic strings redact sensitive fields. Restored legacy live locators retain safe tombstone revocation without UUID parse disclosures. DEBUG sensitive-filename/digest and failed-read cause tests. |

Reuse: existing Java original-byte store, external authority, ordered graph/source/policy guards, reservation SQL, TransactionTemplate, purge and recovery scheduler. Existing local-drive validation is reused without invoking the deferred R7 handle chain. Test runtime storage and owned second-JVM probes are confined to a unique temporary fixture root. No Python changes, OS confinement, new job engine, worker activation or generic security framework. V20 is preserved; forward V21 adds only pending consent association, selected authoritative descriptor, durable integrity denial and pending descriptor checks. V1?V19, frozen Schema v1 and Phase 17 are unchanged.

## Verification

Environment: Windows 11 amd64, OpenJDK 25.0.2, Maven 3.9.15, local Python watchdog, Docker Desktop and disposable PostgreSQL 18.6 / pgvector 0.8.6. No dependency/model/runtime version or production configuration was changed. All commands were launched from repository root with the executable watchdog; output and JSON receipts are under ignored `logs/phase18/`.

**Final focused result: 150 passed / 0 failures / 0 errors / 0 skips, exit 0, 152.110s.** This includes **84 snapshot tests**: corrective lifecycle 42, acquisition 15, store 16 and original Slice 1 11; plus source authority 9, PostgreSQL migration/vector compatibility 2, architecture 37, OpenAPI inventory 1 (**228 operations**), Flyway startup 2 and portability 15. This final run recompiles all final production/test changes, including runtime fencing, cleanup-exception retry and legacy locator tombstoning. Receipt: `logs/phase18/s1-takeover-final-regression.json`; matching console log preserved. Its exact command is below. These are implementer regression results, not independent acceptance.

The earlier 119-test run passed its then-current 79 snapshot cases, 37 architecture and 2 migration/vector checks, but failed the still-compiled old OpenAPI request-schema expectation. That expectation was corrected during the run and recompiled in the next command (10/10 pass). The final 150-test run supersedes both for current-code verification; all earlier failures remain preserved below.

Final snapshot/architecture/migration command (119 tests, **118 pass / 1 failure / 0 errors / 0 skips**, exit 1; 113.156s; original compiled OpenAPI expectation):

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 600 --receipt logs/phase18/s1-takeover-final-focused.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagSnapshotCorrectiveIntegrationTest,RagSnapshotAcquisitionServiceTest,RagOriginalBytesStoreTest,RagSlice1SnapshotIntegrationTest,RagPgVectorCompatibilityIntegrationTest,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest' test
```

Final compatibility correction verification (**10 pass / 0 failures / 0 errors / 0 skips**, exit 0; 70.375s):

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt logs/phase18/s1-takeover-compatibility-final.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagSourceAuthorityIntegrationTest,OpenApiRouteInventoryIntegrationTest' test
```

Broader affected-module command (intermediate compiled revision, **306 tests: 300 pass / 5 failures / 1 error / 0 skips**, exit 1; 655.953s):

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 1200 --receipt logs/phase18/s1-takeover-affected.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=Rag*Test,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest' test
```

That run exposed two stale migration-count assertions (19?21 and 13?15), the restored-live-path denial-message compatibility regression, and the changed capture request schema inventory. All were corrected and verified by the two commands above. It also reported `RagNativeDocumentBatchIntegrationTest.realNativeEightSuccessTwoFailurePublishesAtomicallyAndReconcilesLostAck` (`Invalid worker batch result provenance`) and historical `RagWindowsNativePrerequisiteTest.attemptsAliasCreationAndRemovalDuringProtectedLifetime` (alias creation/removal succeeds). Those paths remain unchanged; FR18-3 stays OPEN and ADR-0023 excludes the old adversarial alias proof from this slice. They are not relabeled PASS, skipped or repaired by changing the result validator. The clean gate below determines the final whole-suite result; no baseline-only clean run was performed, so the exact baseline failure population is not independently established here.

Final clean Java gate: **NOT GREEN ? 1,433 tests / 1 failure / 2 errors / 0 skips** (Surefire console aggregate; **1,430 passed**), exit 1, 847.672s. All 84 snapshot tests passed in this clean run. UTC interval: 2026-10-19 16:42:49.717?16:56:57.387; no timeout or cleanup required. The changed Java/SQL source manifest matched after the run (35 files, zero mismatches). Surefire stopped the Maven lifecycle; later package/verify gates are not claimed.

| Remaining full-gate result | Actual evidence / scope disposition |
| --- | --- |
| Native document batch error | `RagNativeDocumentBatchIntegrationTest.realNativeEightSuccessTwoFailurePublishesAtomicallyAndReconcilesLostAck`: `Invalid worker batch result provenance`. Producer/result code is unchanged and FR18-3 remains OPEN; no validator weakened. |
| Historical native alias failure | `RagWindowsNativePrerequisiteTest.attemptsAliasCreationAndRemovalDuringProtectedLifetime`: held-lifetime alias creation/removal succeeds. Preserved historical R7 failure, outside ADR-0023's required application-level Slice 1 proof; not relabeled PASS or skipped. |
| Full-suite worker connection error | `RagWorkerBoundaryIntegrationTest.actualSpringRuntimeRestartDeniesRestoredConsentAndStaleWorkerGrants`: PostgreSQL `FATAL: remaining connection slots are reserved for roles with the SUPERUSER attribute` while opening the worker fixture connection. The unchanged suite passed **25 / 0 / 0 / 0** in a fresh Maven/Testcontainers process afterward. This does not turn the failed full gate green or establish a baseline-only cause. |

Raw full log/receipt, a summary JSON, two available failing XML reports and a worker-error excerpt are retained under ignored `logs/phase18/s1-takeover-*`. Global totals above use the Surefire console total; the summed XML container total is different and is separately identified in the summary, never substituted for the Maven total.

Exact clean-gate command:

```powershell
python scripts/run-bounded-command.py --profile full-suite --timeout-seconds 2100 --receipt logs/phase18/s1-takeover-clean-verify.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify
```

The first clean attempt was intentionally stopped after discovering the temp-cleanup exception edge described in S1-03: cleanup now always releases its active-capture marker in `finally`, and a real lifecycle fault regression proves the existing reconciler can retry. Only the verified watchdog-owned Maven process tree (PID 20712) was stopped with `taskkill.exe /PID 20712 /T /F`; no blanket Java/Python/Docker termination. Its untouched receipt/log were copied to `s1-takeover-clean-verify-interrupted.{json,log}` before the final gate reuses the normal basename. It ended at 319.375s with exit 1 (intentional stop, not timeout); completed XML reports totaled **1,022 tests / 2 failures / 0 errors / 0 skips**, not a complete suite result. Both observed failures were stale `"19"` migration metadata assertions in `FlywayStartupIntegrationTest.restartAgainstMigratedDatabaseSucceeds` and `PortabilityIntegrationTest.exportArchiveProducesCompleteLosslessZip`; they were synchronized to V21 without changing production startup/export behavior. The final gate below verifies those corrections. A bounded cleanup-focused run rechecks all 80 snapshot cases, architecture, migrations, source authority and OpenAPI before the restart.

The second clean attempt was intentionally stopped to add the current-runtime recheck under the existing graph barrier to snapshot metadata and recovery erasure/denial. Only its verified watchdog-owned Maven tree, PID 15812, was stopped. Raw evidence is retained as `s1-takeover-clean-verify-interrupted-runtime.{json,log}`: 224.250s, exit 1 (intentional stop, not timeout), **991 completed-report tests / 0 failures / 0 errors / 0 skips**, not a full-suite result. The runtime-fence focused command (`s1-takeover-runtime-focused.json`, same selector as cleanup-focused) passed **132 / 0 / 0 / 0** in 127.797s, including late runtime replacement during registration/capture and a cleanup race after the initial runtime read. No OS isolation or second job engine was introduced. The final legacy-path tombstone regression preserves the frozen restored-catalog revoke behavior and privacy. No production or test source changed after the final focused run; the Java/SQL manifest was verified after the clean gate.

The cleanup-focused command (`s1-takeover-cleanup-focused.json`, ordinary 600s watchdog) passed **129 / 0 / 0 / 0** in 127.407s. It used the 119-test selector above plus `RagSourceAuthorityIntegrationTest` and the additional cleanup-exception regression. The runtime-focused command used that same selector and the three added runtime cases.

Final focused command (**150 / 0 / 0 / 0**, including **84 snapshot cases**):

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 600 --receipt logs/phase18/s1-takeover-final-regression.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagSnapshotCorrectiveIntegrationTest,RagSnapshotAcquisitionServiceTest,RagOriginalBytesStoreTest,RagSlice1SnapshotIntegrationTest,RagPgVectorCompatibilityIntegrationTest,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest,RagSourceAuthorityIntegrationTest,FlywayStartupIntegrationTest,PortabilityIntegrationTest' test
```

Isolated worker-boundary verification after the full-suite connection failure: **25 / 0 / 0 / 0**, exit 0, 168.031s; no source changes, timeout or cleanup.

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 600 --receipt logs/phase18/s1-takeover-worker-boundary-isolated.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagWorkerBoundaryIntegrationTest' test
```

Earlier bounded iterations are preserved, with no timeout or tree cleanup: compile (`s1-takeover-compile.json`, exit 0, no tests); initial focused (`s1-takeover-initial-focused.json`, 34 tests / 3 failures / 0 errors / 0 skips); corrective-focused-1 (test compilation failure, no tests executed); corrective-focused-2 (70 tests / 5 failures / 0 errors / 0 skips: test spy wiring and sanitized exception expectation); corrective-focused-3 (**73 / 0 / 0 / 0**, exit 0). The three `s1-takeover-corrective-focused-N.json` commands used the identical test selector `RagSnapshotCorrectiveIntegrationTest,RagSnapshotAcquisitionServiceTest,RagOriginalBytesStoreTest,RagSlice1SnapshotIntegrationTest`, ordinary 360s watchdog, the same Maven flags shown above, and `test`. Initial focused used `RagSnapshotAcquisitionServiceTest,RagSlice1SnapshotIntegrationTest,RagOriginalBytesStoreTest`, 300s; compile used 180s with `-DskipTests test-compile`. Logs use the corresponding receipt basename with `.log` (compile console output is in the session). Exact test command argv SHA-256 values and UTC intervals are in the executable receipts.

`git diff --check` passes (exit 0); Git emits CRLF normalization advisories. The final check is repeated after documentation synchronization. Compiler/runtime output includes Lombok/Unsafe, deprecation/unchecked, Mockito attachment, SpringDoc and scheduled datasource/connection error output; no warning-free or IDE-diagnostics claim. Python unit/static suites, parser/OCR/held-out quality, GPU/RAM benchmarks, P18-FINAL and production activation are **NOT RUN / NOT CLAIMED**.

Proof limits: restart uses fresh production store/recovery instances over the persisted test inventory/database and the real startup hook; it is not an NTFS power-loss or malicious-process proof. Unknown outcome tests inject an exception after a real commit or rollback and reconcile via fresh production DB reads, rather than disconnecting an actual network cable. Metadata failures are actual PostgreSQL trigger-injected statement failures in the disposable test DB. Inaccessible deletion/retry uses a real open Windows file handle. Files left by failed deletion are the durable UUID retry inventory; no new cleanup job table/queue is introduced. Inaccessible/unknown managed namespace names are retained for explicit operator disposition, never adopted as revisions. Existing V20 rows without a recorded pending-consent association are not silently granted consent by V21; missing selected descriptors or detectable integrity uncertainty deny. Explicit owner recovery beyond revoke/cleanup remains deferred.

## Retained boundaries and next gate

Pending-only R; no selected-R/sourceVersion/G promotion or full NO_CHANGE. External indexing remains INACTIVE, original-live DISABLED/DEFERRED, FR18-3 OPEN, Slices 2?4 unauthorized. No Slice 1 acceptance, Phase 18 acceptance or P18-FINAL claim. Ordinary application-level protection under ADR-0023; malicious same-user isolation and forensic backup/WAL erasure are not claimed. **Implementation handback:** all seven S1 corrections and their required focused regressions are implemented; S1-01?S1-07 remain OPEN for independent adjudication, not implementer-closed. No unresolved S1 implementation blocker was detected by the stated verification. The full gate remains failed with the three results above. **Next gate: a fresh/separate independent `$codex-final-review`, disclosing Codex takeover authorship and independently checking current source and evidence.** No READY FOR OWNER COMMIT, Slice 1 acceptance or automatic continuation is granted. This implementation session stops after handback.

## Final evidence digests

| Ignored artifact under `logs/phase18/` | SHA-256 |
| --- | --- |
| `s1-takeover-final-regression.json` | `7226dd68f74e02ef22896b4f14ebe47350e19c075c45862c9828460e0f9c2db0` |
| `s1-takeover-final-regression.log` | `ca61293406d49054ff02159b8a24309cf55bc119aec4fbc9f8ed88c45579b2d0` |
| `s1-takeover-clean-verify.json` | `763c3b3327612fc31b2d4e2ec2b2b225a86870a7250afe2780efef7ea8bc0d11` |
| `s1-takeover-clean-verify.log` | `8aebbdf10e1e4561e7ea59a4dc128b8d957a87c2b3ece7a16ed885dc80f5bc50` |
| `s1-takeover-worker-boundary-isolated.json` | `d65d479a97356366148949282c979a8fa53be21471d75e6ad1261911b8cbd409` |
| `s1-takeover-final-source-manifest.json` | `b5f64d4e7b26022665d0b6cf9432ba80e2da3ebf57d87c2b785e30a6c08dd607` |
