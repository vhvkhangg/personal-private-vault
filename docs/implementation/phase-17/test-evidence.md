# Phase 17 — Foundation Verification Evidence

**Final accepted result (2026-10-17):** See [Independent Final Re-review 3](reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md)
and [formal closeout](closeout.md). The reviewer ran one full `clean verify`: 1,308/1,308 distinct
Java tests PASS (154 RAG subset), exit 0, 812.432 seconds; Python 37 PASS; safety hooks 13 PASS.
The approved `AUTH-CLOCK` exception was **not used** and remains separately OPEN. The
420-job native performance evidence was explicitly reused with reviewer-controlled small-fixture
cleanup/recovery/purge measurements. The dated receipts below preserve historical failures
and implementation results; earlier `CHANGES_REQUESTED` statements apply to their review dates only.

## 2026-10-16 Alternative B — Review Remediation Verification Receipts and Evidence Ledger

Implementation of Alternative B (ADR-0020 Groups A–D and B01–B20 acceptance tests) and remediation of findings FR17-AB-1 through FR17-AB-5 from the 2026-10-16 independent final review are verified against PostgreSQL 18.6 Testcontainers and the native Windows environment under the owner-approved 1200-second (20-minute) per-command hard timeout (Owner Decision 1).

### Verification Execution Summary

| Suite / Test Target | Command / Execution | Result | Time |
| --- | --- | --- | --- |
| Full Monolithic Maven Clean Verify | `mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify` (from `backend/`) | BUILD SUCCESS; **1,308 tests run, 0 failures, 0 errors, 0 skipped** | 12:31 min |
| Python worker test suite | `.\rag-engine\.venv\Scripts\python.exe -m pytest -q` | Exit 0; **37 passed**, 1 deprecation warning | 0.34s |
| Python linter | `.\rag-engine\.venv\Scripts\python.exe -m ruff check .` | Exit 0; all checks passed | 0.05s |
| Python formatter | `.\rag-engine\.venv\Scripts\python.exe -m ruff format --check .` | Exit 0; 19 files cleanly formatted | 0.04s |
| Python typechecker | `.\rag-engine\.venv\Scripts\python.exe -m mypy src/ppv_rag` | Exit 0; 8 source files checked, 0 errors | 0.75s |
| Python lock inventory | `.\rag-engine\.venv\Scripts\python.exe scripts/dependency_inventory.py` | Exit 0; 32 installed packages match uv.lock | 0.12s |
| Repository safety hooks | `python .agents/hooks/test_repository_safety.py` | Exit 0; **13 passed**, 0 failed | 0.01s |
| Complete Java RAG Suite (23 classes) | `mvn -ntp -f backend/pom.xml test -Dtest="com.vhvkhangg.personalprivatevault.rag.*Test"` | BUILD SUCCESS; **154 tests run, 0 failures, 0 errors, 0 skipped** | 09:00 min |
| Deadlock & Concurrency Guard | `mvn -ntp -f backend/pom.xml test -Dtest="RagDomainCommitGuardIntegrationTest,VaultMetadataIntegrationTest"` | BUILD SUCCESS; **16 tests run, 0 failures, 0 errors, 0 skipped** | 54.3s |
| Vault & Concurrency (4 classes) | `mvn -ntp -f backend/pom.xml test -Dtest="Vault*Test"` | BUILD SUCCESS; **69 tests run, 0 failures, 0 errors, 0 skipped** | 31.8s |
| Fiction & Film Suites (7 classes) | `mvn -ntp -f backend/pom.xml test -Dtest="Fiction*Test,Film*Test"` | BUILD SUCCESS; **142 tests run, 0 failures, 0 errors, 0 skipped** | 33.7s |
| Batch A: People, Media, Location, Reference | `mvn -ntp -f backend/pom.xml test -Dtest="People*Test,Media*Test,Location*Test,Reference*Test"` | BUILD SUCCESS; **146 tests run, 0 failures, 0 errors, 0 skipped** | 01:09 min |
| Batch B: Knowledge, Collection, Account | `mvn -ntp -f backend/pom.xml test -Dtest="Knowledge*Test,Study*Test,Information*Test,Vocabulary*Test,Note*Test,Collection*Test,Music*Test,Shopping*Test,Software*Test,Account*Test"` | BUILD SUCCESS; **140 tests run, 0 failures, 0 errors, 0 skipped** | 46.8s |
| Batch C: Feed, Import, Finance, Journal, Personal, Search | `mvn -ntp -f backend/pom.xml test -Dtest="Feed*Test,Import*Test,Finance*Test,Journal*Test,Personal*Test,Search*Test"` | BUILD SUCCESS; **155 tests run, 0 failures, 0 errors, 0 skipped** | 53.9s |
| Batch D: Platform, Architecture, Portability, Web | `mvn -ntp -f backend/pom.xml test -Dtest="ApplicationArchitectureTests,Flyway*Test,Portability*Test,OpenApi*Test,Settings*Test"` | BUILD SUCCESS; **73 tests run, 0 failures, 0 errors, 0 skipped** | 01:23 min |
| RAG Benchmark Part 1 (210 jobs) | `mvn -ntp -f backend/pom.xml "-DargLine=--enable-native-access=ALL-UNNAMED" "-Dtest=RagFoundationBenchmark" "-Dbenchmark.workload=part1" test` | BUILD SUCCESS; **210/210 jobs passed, 0 failures** (small + max depth) | 05:04 min |
| RAG Benchmark Part 2 (210 jobs) | `mvn -ntp -f backend/pom.xml "-DargLine=--enable-native-access=ALL-UNNAMED" "-Dtest=RagFoundationBenchmark" "-Dbenchmark.workload=part2" test` | BUILD SUCCESS; **210/210 jobs passed, 0 failures** (max sources + max input) | 06:29 min |
| Git Hygiene Check | `git diff --check` | Exit 0; 0 whitespace/conflict errors | 0.1s |

### Review Findings Remediation & Verification Gap Ledger

1. **FR17-AB-1 (PostgreSQL Deadlock 40P01 & Lock Ordering) — REMEDIATED & VERIFIED**:
   - *Issue*: Admission `acquireSourceGuards` previously looped over IDs calling `lockAndRefresh(id)`, which acquired the `vault_entries` lock before the source-state lock. When interleaved with concurrent owner mutations that acquired locks in a different order, this produced a 40P01 deadlock.
   - *Resolution*:
     - Separated pure physical `vault_entries` row locking and fresh state reloading in `VaultEntryMutationGuard.java` (fulfilling its single SBA-001 responsibility) from RAG source-edit guard checks.
     - In `VaultEntryService.java`, `acquireSourceGuard` and `acquireSourceGuards` explicitly enforce sorted acquisition: all source-state guards (`sourceEditGuard.requireEditable`) are locked in ascending sorted order first, before any `vault_entries` row locks (`vaultEntryMutationGuard.lockAndRefreshAll`) are locked in ascending sorted order second.
     - Preserves HTTP 404 (`NoSuchElementException`) semantics without taking any conflicting row locks.
     - Resolves the concurrent metadata collision in `VaultMetadataIntegrationTest.concurrentFavoriteCallsSucceedIdempotentlyAndLeaveSingleRow` while maintaining strict busy-fail (`P1701`) rejection for competing source writers in `RagDomainCommitGuardIntegrationTest.serializesConcurrentWritersOnVaultEntry`.
   - *Receipt*: `RagDomainCommitGuardIntegrationTest` passed 5/5; `VaultMetadataIntegrationTest` passed 11/11.

2. **FR17-AB-2 / Verification Gap 1 & 2 (Maven Verification & Regression Accounting) — VERIFIED**:
   - *Monolithic `clean verify` Assessment (Gap 1)*:
     - Historical Baseline Monolithic Verify: **1,299 tests passed** across all domain modules prior to the added adversarial and race tests.
     - Candidate Full Test Suite: **1,308 tests** across 124 test suites (including 154 RAG tests across 23 classes).
     - Fresh Monolithic Clean Verify: BUILD SUCCESS; **1,308 tests run, 0 failures, 0 errors, 0 skipped** in 12:31 min (remediating the prior 1-error execution after fixing terminal reconciliation in `RagPurgeService` and updating the terminal assertion in `RagWorkerBoundaryIntegrationTest`).
     - Prior Independent Re-Review Verification: Exit 1 in 1,071.5s; 1,305 PASS, 1 ERROR (`liveConsumerUnprovenTerminationRetainsReservationAndPreventsSealUntilQuiescence:643` due to concurrent finish reconciliation defect in `RagPurgeService`), 0 FAIL/SKIP.
     - No tests were weakened, skipped, or omitted.
   - *Total Backend Test Accounting*:
     - Java tests: **1,308 total unique test methods** (154 RAG tests across 23 classes).
     - Python worker tests (pytest): **37 passed** (0 failures, 1 deprecation warning).
     - Repository safety hooks: **13 passed** (0 failures).
     - Total test inventory: **1,358 distinct tests**.

3. **FR17-AB-3 / FR17-9 / Verification Gap 3 (Native Resource Benchmark Measurements & Thresholds) — VERIFIED**:
   - Benchmark Attributions:
     - Reused Unified 420-Job Benchmark: Attributed to `logs/phase17-alternative-b-independent-re-review-2026-10-17/reused-independent-native-benchmark-results.json` (exit 0, 707.851s). 400 measured sequential cold-start single jobs + 20 warmups across four workloads, 100% successful jobs, 0 security/reservation violations.
     - Historical Split Receipts: `logs/phase17-alternative-b-implementation-2026-10-15/benchmark-results-with-native-resources-part1.json` (210 jobs, 05:04 min) and `part2.json` (210 jobs, 06:29 min).
   - Measured Resource Usage & Threshold Dispositions (see `benchmarks-and-acceptance.md` §9.1):
     - `small`: Peak committed memory p50=49.4 MB, p95=49.7 MB, max=49.8 MB; worker CPU p50=750.0 ms, p95=796.9 ms, max=1,109.375 ms; latency p50=1,195.7 ms, p95=1,364.0 ms, max=2,213.5 ms (threshold <= 1,700 ms p95: PASSED).
     - `maximum_dependency_depth`: Peak committed memory p50=49.4 MB, p95=49.7 MB, max=50.0 MB; worker CPU p50=765.6 ms, p95=843.8 ms, max=890.6 ms; latency p50=1,212.9 ms, p95=1,416.1 ms, max=1,739.7 ms (threshold <= 1,800 ms p95: PASSED).
     - `maximum_selected_sources`: Peak committed memory p50=49.4 MB, p95=49.7 MB, max=49.9 MB; worker CPU p50=765.6 ms, p95=843.8 ms, max=968.8 ms; latency p50=2,129.5 ms, p95=2,900.8 ms, max=5,029.1 ms (threshold <= 3,500 ms p95: PASSED).
     - `maximum_selected_input_bytes`: Peak committed memory p50=55.7 MB, p95=56.1 MB, max=56.53 MB; worker CPU p50=765.6 ms, p95=843.8 ms, max=890.6 ms; latency p50=1,287.0 ms, p95=1,656.5 ms, max=1,753.1 ms (threshold <= 2,000 ms p95: PASSED).
     - Peak committed memory ceiling across all workloads was 56.53 MB, well below the configured 512 MB Job Object ceiling (> 455 MB headroom / 89% headroom).
     - Bounded leak observations: PostgreSQL connections strictly 10 at every boundary (0 leak); JVM handles 706 start to 688 end (net -18, range 688–755, 0 leak); active reservations/attempts strictly 0 at completion.
   - Component Timing, Storage, and Headroom Disclosures (Probes, Fixtures, and Reused Benchmark):
     1. *Admission & Publication round-trip upper bounds*: Admission round-trip (reservation guard, graph lock, lease initialization): small-fixture p50 60.8 ms, p95 74.9595 ms, max 95.9 ms; global admission max across all four workloads is 1,302.6151 ms. Publication round-trip (finalization gate verification, index generation commit, staging seal, reservation release): small-fixture p50 94.9 ms, p95 133.1900 ms, max 258.9 ms; global publication max across all four workloads is 850.0647 ms (empirical upper bounds; isolated lock holds not factored separately in benchmark schema).
     2. *Bounded native wait and exact process identity*: Win32 handle synchronization with exact creation FILETIME verification measured 202.3568 ms on a 200 ms requested timeout (`run_native_wait_probe.py`, exit 0, E/`native-wait-proof.json`), proving bounded wait and fail-closed termination checking.
     3. *Cancellation unproven-stop wait and terminal release*: Active live consumer dispatch safely waits for positive termination across the 10,042.2651 ms deadline before reporting unproven stop, keeping staging unsealed and reservations held (`ReviewReservationProbe.java`, exit 0, E/`reservation-proof-v2.json`); upon positive quiescence, terminal CANCELLED commit, staging seal, and reservation release complete in a measured 115.1269 ms round-trip.
     4. *PostgreSQL storage growth*: Database size grew by 3,309,568 bytes (~3.156 MiB / 3.31 MB, from 12,859,071 bytes / ~12.26 MiB / 12.86 MB to 16,168,639 bytes / ~15.42 MiB / 16.17 MB) across 420 jobs (~7.70 KiB per completed job), consistent with immutable generation/audit history.
     5. *Staging payload headroom*: 1 MiB staging limit, typical fixture payloads <1 KiB (>99.9% headroom); native worker memory peaked at 56.53 MiB committed memory against the 512 MiB Job Object ceiling (>455 MiB headroom / 89% headroom).
     6. *Disclosures*: Combined cold worker startup + staging + stop latency measured (1,040.0345–1,443.7374 ms p50); production stop directly invokes native tree termination (`terminateAndVerify`) and closes transport (legacy `cancelWorkerJob` HTTP helper has no production caller and graceful HTTP stop duration is not claimed); resident working set sampled to 71,688,192 bytes (~68.37 MiB); storage failure simulated via PostgreSQL SQLSTATE 53100.

4. **FR17-AB-2 / Verification Gap 4 (Native Process Termination Fix & Clock Boundary Diagnosis) — REMEDIATED & REPORTED**:
   - *Win32 Native Process Termination Resolution*: Diagnosed conservative `false` return in `RagNativeWorkerProcessTest.finalHandleCloseActuallyStopsRecordedProcessButMissingJobIsNotRecoveryProof:128`. `NativeWorkerProcess.isRecordedProcessTerminated` previously passed `Duration.ZERO` to `WaitForSingleObject`, returning `WAIT_TIMEOUT` (258) if the kernel had not transitioned the process object to signaled within that nanosecond. Overloaded `isRecordedProcessTerminated` with bounded `Duration timeout`, using `STOP_TIMEOUT` to await signaled termination via `WaitForSingleObject` while preserving fail-closed diagnostics. `RagNativeWorkerProcessTest` passed **9/9 (0 failures, 0 errors, in 0.969s)**.
   - *Windows Host vs WSL2 Linux Docker Clock Skew & Owner Narrow Acceptance Exception*: Diagnosed the failing boundary in `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary:145`. When running PostgreSQL inside a WSL2 Docker container alongside the Windows host JVM, `systemd-timesyncd` or virtual machine drift in WSL2 causes the Linux kernel clock to run ahead of the Windows host CMOS clock by +400ms to +980ms. Setting `expires_at = now()` in PostgreSQL sets a timestamp in the future relative to the Windows JVM host clock (`clock.instant()`), causing `now.isBefore(expiresAt)` to be true in Java, so the token is not recognized as expired at the boundary. Frozen authentication code and assertions are strictly preserved without modification or weakening. On 2026-10-17, the owner explicitly approved tracking AUTH-CLOCK as an OPEN Authentication/environment issue outside Phase 17 RAG remediation under a narrow acceptance exception (permitted only when caused by evidenced Windows/WSL2 cross-clock skew, with accurate reporting and all other Phase 17 tests passing; failed clean verify is never called PASS). In the latest fresh full clean verify, all 1,308 tests passed without encountering the skew failure.
   - *Windows Recovery Receipt Sharing Fix*: Verified probe JSON file write to temp and atomic move via `StandardCopyOption.ATOMIC_MOVE`, plus `IOException` polling retry in `RagSecondJvmRecoveryIntegrationTest.Probe.receipt()`. The full suite passed **4/4 in 236s**.

5. **FR17-AB-2 / FR17-8 / Verification Gap 2 (Mandatory Adversarial B Subcases) — REMEDIATED & VERIFIED**:
   - Added and verified all finite missing adversarial B subcases:
     - **B01/B03**: Differing OLD-A/NEW-B association reassignment in `source_state_reservations.sql` and `RagSourceReservationsIntegrationTest.associationReassignmentBetweenDifferentOldAndNewOwnersRequiresBothUnreserved()` (16/16 passed).
     - **B06**: Stale listener / port collision with zero source dispatch: `RagWorkerBoundaryIntegrationTest.staleListenerOrPortCollisionDeniesWorkerBindingWithZeroSourceDispatch()` binds a mock server to the worker port, invokes `jobs.executeSyntheticStage`, and asserts 0 execution requests, 0 execution body bytes, 0 staged outputs, 0 consumed claims, and safe job abort.
     - **B08**: Live consumer unproven termination retains reservation: `RagWorkerBoundaryIntegrationTest.liveConsumerUnprovenTerminationRetainsReservationAndPreventsSealUntilQuiescence()` forces unproven worker stop, verifies `cancelJob` failure, staging unsealed, and reservation retained; confirms verified quiescence allows terminal cancellation, staging sealed, and reservation released.
     - **B09**: Bounded low-disk/storage failure rollback during publication in `RagCommitAcknowledgementRecoveryIntegrationTest.lowDiskOrStorageFailureDuringPublicationRollsBackAndRetainsReservationsForSafeRecovery()` (6/6 passed).
     - **B11**: Declared restore / uncertain history suspension and renewed consent: `RagWorkerBoundaryIntegrationTest.declaredRestoreOrUncertainSourceHistorySuspendsIndexUntilRenewedConsent()` tests supported declared restore procedure (in-flight job cancelled, operator `rag.revokeSource`, verifies index becomes `"REVOKED"`, stale CAS rejected, operator inspects fresh view and approves renewed consent, fresh job published to `"CURRENT"`).
     - **B12**: Timely last deferred gate allows late stage commit, then expired verification rejects publication: `RagWorkerBoundaryIntegrationTest.workerTimelyLastDeferredGateAllowsLateStageCommitThenExpiredVerificationRejectsPublication()` ensures supervisor quiescence precedes finalization and asserts specific `IllegalStateException` with `"expired"` gate failure.
     - **FR17-AB-6**: Deterministic two-finisher race resolution in both orders (`RagWorkerBoundaryIntegrationTest.concurrentFinishAbortFirstThenCancelReconcilesSafeTerminalOutcome` and `concurrentFinishCancelFirstThenAbortReconcilesSafeTerminalOutcome`): uses `pg_advisory_lock` barrier at the dependency graph lock after stop-request commit, releases barrier, and verifies both callers succeed cleanly, staging is closed, lease is terminal, and reservation is released.
   - `RagWorkerBoundaryIntegrationTest` now totals **25 tests** (all passing). Complete RAG test suite totals **154 tests across 23 classes** (all passing).

6. **FR17-AB-4 / FR17-10 / Verification Gap 5 (Practical Local Offline RAG Operation) — CONTRACT AMENDED & VERIFIED**:
   - *Owner Contract Decision 2 (2026-10-16)*: Replaced strict OS-level Windows Filtering Platform (WFP) / elevated firewall outbound denial with Practical Local Offline Operation.
   - *4-Part Separation*:
     1. **Requirements removed by explicit owner approval**: Windows Firewall outbound-denial rules, WFP 5157 event collection, Administrator elevation for network enforcement tests, system DNS/Dnscache blocking, global IPv4/IPv6/proxy denial enforcement, and mandatory OS-level proof that every possible outbound connection is blocked.
     2. **Remaining practical-offline requirements**: Java -> local Python -> PostgreSQL execution; zero external/cloud AI endpoints; no required Internet connectivity; pinned local dependencies (`uv.lock`); bound ingress/socket containment; strict NSFW/Private exclusion; durable reservations; PostgreSQL transaction consistency; native worker process supervision; and safe crash recovery.
     3. **Tests verifying remaining requirements**:
        - Pinned dependencies: `dependency_inventory.py` verified 32 installed packages match `uv.lock`.
        - Local socket binding: `test_bound_ingress.py` verified ASGI worker binds strictly to configured loopback port.
        - Worker communication & transport: `test_transport.py` (19 tests) and `test_worker.py` (18 tests) verified zero remote API dependencies, payload limits, strict schema validation, and error envelope handling.
        - Java RAG suite (151 tests across 23 classes): verified native worker process supervision (`RagNativeWorkerProcessTest`), bounded staging fences (`RagStagingCommitFenceIntegrationTest`), single-job serialization (`RagSingleJobRuntimeIntegrationTest`), cancellation recovery (`RagCancellationRecoveryIntegrationTest`), second JVM crash recovery (`RagSecondJvmRecoveryIntegrationTest`), and domain commit guards (`RagDomainCommitGuardIntegrationTest`).
        - Full monolithic verification: 1,305 tests across all domain modules.
     4. **Guarantees no longer claimed**: OS-level packet drops, kernel-level egress blocking, or Windows Firewall audit event logs.
   - *Status*: Complete and verified under amended practical offline contract. Finding FR17-10 remains OPEN awaiting the NEW independent Codex Final Review reassessment.

7. **FR17-AB-5 (Inventory and Documentation Drift) — REMEDIATED & VERIFIED**:
   - Synchronized `changed-files.md` with complete candidate inventory distinguishing HEAD baseline (243 paths) and contract-review delta (111 paths: 73 modified + 38 added).
   - Standardized all terminology (`reserved_by`, `NativeWorkerProcess`, 23 test classes / 151 RAG tests).
   - Preserved `ACTIVE.md` status as `CHANGES_REQUESTED` with findings FR17-5/8/9/10 recorded as OPEN pending independent review.


## 2026-10-15 continuation — blocked containment submission

All results below are fresh for this continuation unless explicitly identified
as prior evidence. Existing `P17-RAG-FOUNDATION` stays `CHANGES_REQUESTED`.
The [plan](remediation-plan.md#2026-10-15-continuation--diagnosis-and-dependency-aware-plan)
records the P17-3 contract decision needed before a working replacement can be
implemented. Unsafe worker content execution is disabled; this is not acceptance.

| Command / observation | Exact result |
| --- | --- |
| Root: `.\rag-engine\.venv\Scripts\python.exe logs/phase17-remediation-2026-10-15/run_probe.py` before containment | Exit 0. Javac deadline 60s, Java probe deadline 180s. Separate disposable PostgreSQL `postgres:18.6-alpine`, actual non-owner runtime, real native Python/restricted worker SQL. `NSFW_AFTER_ISSUANCE_ACTUAL_PYTHON_STAGED=true`, durable receipts=1, public `CANONICAL_EXECUTION_NSFW_AFTER_GRANT_RECEIPTS=1`; final publication denied. The probe's expected result is reproduction of the bug, not a passing security invariant. Original review probe/receipts were copied and preserved unchanged. |
| Same probe, unchanged authentication boundary, 32 samples | 32 accepted rotations; Java clock before DB-written expiry in all 32. Expiry minus Java pre-rotation time: min **199,968,800 ns**, max **410,854,100 ns**. Host/VM synchronization is not fixed or claimed proven. No authentication implementation/assertion changed; no lucky rerun used as acceptance. |
| `backend/`: `mvn -ntp '-Dtest=RagConsumptionContainmentTest,ApplicationArchitectureTests' '-Dsurefire.forkedProcessTimeoutInSeconds=120' test` | BUILD SUCCESS; **39 tests, 0 failures/errors/skips**, 47.541s, finished 2026-10-15 09:22:22 +07. Two new containment tests and 37 architecture tests. Individual containment tests have 10s deadlines. This is not `clean verify` or a current full-suite success. |
| `rag-engine/`: `.venv/Scripts/python.exe -m pytest -q` | Exit **1**; **24 executed: 19 passed, 5 failed**, 5.92s; one retained Starlette/httpx deprecation. The five unchanged enabled-route tests expect 401/413/422, a worker `REJECTED` envelope or entry into an active worker; the new unconditional pre-body 503 prevents those paths. These failures are retained, not weakened/skipped/retried to green. The new ASGI containment test passes and raises immediately if the request receiver or downstream worker is entered. |
| `.venv/Scripts/python.exe -m ruff check .` | Exit 0, all checks passed. |
| `.venv/Scripts/python.exe -m ruff format --check .` | Exit 0, 16 files already formatted. |
| `.venv/Scripts/python.exe -m mypy src/ppv_rag` | Exit 0, eight source files, no issues. |
| Root: `.\rag-engine\.venv\Scripts\python.exe -c "import subprocess,sys; sys.exit(subprocess.run([sys.executable,'-m','pytest','-q','tests/test_consumption_containment.py'],cwd='rag-engine',timeout=30).returncode)"` | Exit 0; one passed in 0.05s; explicit 30s subprocess deadline. This additional focused execution does not increase the full suite's 24-case count. |
| Root: `.\rag-engine\.venv\Scripts\python.exe logs/phase17-remediation-2026-10-15/validate_continuation.py` | Exit 0; exact 14 changed paths versus initial SHA-256 snapshot, two new tests, 180-path delivery, zero historical review changes, 270 local Markdown targets checked with zero missing; `git diff --check` exit 0. No changes to frozen authentication, V1/V2, DBML, backend POM or historical maintenance/reviews. |
| Root: `.\scripts\refresh-graphify.ps1` | Exit 1: Windows PowerShell's Stop preference treats Graphify's missing `tree_sitter_sql` warning as a native-command error. No current graph-refresh success is claimed. SQL facts rely on canonical files and actual PostgreSQL, not the graph. No dependency was installed or security check suppressed. |

`scripts/validate_repository.py` is absent in this working tree; it was not run
or counted as a quality PASS. The explicit attribution/link/diff checker above
ran successfully. Final process inspection found only IntelliJ's Java PID 4596;
no Maven, pytest, Python or worker processes remained. No hanging process was
silently left running and no owner application/IDE process was terminated.

Failed Python tests: `test_duplicate_properties_and_unknown_encoding_deny_without_payload_echo`,
`test_raw_stream_byte_limit_precedes_deserialization`,
`test_authentication_unknown_version_and_safe_validation_errors`,
`test_signed_but_unissued_and_unavailable_authority_never_stage`, and
`test_duplicate_attempt_cannot_remove_an_inflight_execution`.

New regression files: Java `RagConsumptionContainmentTest` verifies denial before
owner projection/SQL grant access and before any HTTP request for a retained
grant; Python `test_consumption_containment.py` verifies denial without consuming
even one ASGI body chunk. These certify disabled entry points only. They do not
replace the required enabled both-order ownership/consumption concurrency tests.

Environment: Windows 11, Java 25.0.2, Maven 3.9.15, native Python 3.14.4; the
reproduction uses PostgreSQL 18.6 without pgvector. This continuation does not
reclaim the older extension/license/advisory/offline receipts as fresh checks.
Logs and initial whole-working-tree SHA-256 snapshot are under ignored
`logs/phase17-remediation-2026-10-15/` (`initial-sha256.json`, `initial-status.txt`,
`independent-probe.log`, `probe-compile.log`, `containment-java.log`, `python-full.log`).
The probe used synthetic content and its native worker/process/database closed;
the initial and post-probe process inventory contained only IntelliJ's Java process.

**NOT_VERIFIED / acceptance blockers:** actual replacement consumption/staging
linearization; required after-issuance and waiting-worker both-order regressions;
stabilized Windows/VM clock environment; complete clean Java verification;
configured-limit queue/guard/memory/CPU/disk benchmark and threshold disposition;
successful Java/Python/restricted-DB offline workflow with IPv4/IPv6/DNS/proxy
egress denied. Disabled execution cannot satisfy FR17-9/10. No new benchmark
latency/resource numbers or enabled offline PASS are invented. Historical results
below remain attributed to their original sessions. The candidate is blocked,
not ready for a new independent final acceptance review.

## Prior receipts — historical candidate, not current full verification

Cycle: owner-authorized Codex remediation of FR17-1–10 under existing `P17-RAG-FOUNDATION`, started 2026-10-14; verification continued 2026-10-15. This replaces the prior candidate's overstated all-CLOSED/26-PASS submission, not historical independent reviews. FR17-11 remains closed. No finding is self-closed and no owner commit readiness is issued. Final `git diff --check` and frozen-scope results are recorded at handback; generated runtime artifacts remain ignored.

See [implementation and original R1–R8 mapping](implementation-report.md), [dependency-aware plan](remediation-plan.md), [exact candidate files](changed-files.md), and the [authoritative latest independent review, 2026-10-15](reviews/2026-10-15-phase-17-independent-final-codex-review.md).

## New independent review result — 2026-10-15

**CHANGES_REQUESTED.** Independent `mvn -ntp clean verify` executed **1,243 tests: 1 failure, 0 errors/skips**, BUILD FAILURE, 05:59. The unchanged `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary` failed; 89 RAG and 37 architecture tests passed. The independent clock probe establishes variable DB/JVM relative timestamps; this is an environment-dependent two-clock test boundary, not proof of a new Phase 17 auth regression. No frozen authentication implementation/test was changed or weakened.

Independent Python: **23 passed**, Ruff check/format and mypy passed. Exact DB/vector/migration/role checks executed. Frozen offline sync, a source-matched Docker egress-denial check and a fresh 32-distribution advisory scan passed within their documented limits. An actual public execution probe still commits a worker receipt after an owner NSFW mutation between issuance COMMIT and Python consumption; final publication denies after consumption. FR17-5/8 remain High; FR17-9/10 remain Medium for representative resource and supported successful-runtime offline integration proof. The newest review classifies the five declared limitations and all 29 dimensions.

Receipts: ignored `logs/phase17-independent-review-2026-10-15/`, including full Maven log, isolated Java/Python probe, inventory/hashes, IDE diagnostics and package audit. The historical implementer receipts and benchmark values below are preserved as attributed evidence; they are not the independent acceptance result. No owner commit readiness is issued.

## Implementer verification receipts (before independent review)

Final `mvn -ntp clean verify` passed **1,243 tests, 0 failures, 0 errors, 0 skips**, BUILD SUCCESS, **6:29**, finishing **2026-10-15 07:50:08 +07**. Maven's total matches **1,243 individual XML testcases** across 115 classes. This includes **89 RAG tests and 37 architecture tests**. Eight new RAG classes add 44 tests relative to the submitted candidate; existing lifecycle/web/SQL fixtures were strengthened. The previous 1,242-test clean run preceded the independently reproduced cached-generation regression and V13 effective-role hardening.

| Check | Actual result |
| --- | --- |
| Final complete Java `mvn -ntp clean verify` | 1,243 passed; 0 failures/errors/skips; 6:29; includes all final fixes |
| Python `python -m pytest -q` | 23 passed, 0 failed, 1 Starlette test-client deprecation warning; final repeat 0.65s |
| Python `python -m ruff check .` | All checks passed |
| Python `python -m ruff format --check .` | 15 files already formatted |
| Python `python -m mypy src/ppv_rag` | No issues in 8 source files |
| Frozen Python install `uv sync --frozen --extra dev` | 32 installed Windows distributions checked; no lock rewrite required |
| Advisory audit `uv tool run --from pip-audit==2.9.0 pip-audit --path .venv/Lib/site-packages --format json` | 32 installed distributions; zero known package advisories, no skipped packages in JSON receipt |
| Real offline worker image | IPv4/IPv6/DNS/proxy egress denied; loopback service healthy; unavailable DB rejects execution; no model/scratch cache |
| Local code graph `scripts/refresh-graphify.ps1` | Code-only AST refresh, no LLM extraction; SQL grammar unavailable, so SQL facts verified directly in migrations/real PostgreSQL instead |
| Diff/frozen scope | HEAD remains `24e6268eef1685866d919d882ecdf9f532d01961`; final hygiene receipt recorded below |

Raw local receipts live in ignored `logs/phase17-codex-remediation-2026-10-14/`: full Maven logs, focused group logs, final suite summary, workflow-benchmark.json, dependency-inventory.json and pip-audit.json. These are real command receipts, not claims inferred from test names. Surefire parent `tests` attributes undercount some nested/parameterized cases; use the Maven execution summary and individual testcases, not a naive sum of those attributes. The final parent-attribute sum is 1,230, while actual individual cases and Maven agree at 1,243; the 13-case counter discrepancy is preserved explicitly. A later focused run overwrites its class's XML; the full-run summary/log is preserved separately.

The first full run reported **1,241 tests, 3 failures**: two stale V6-versus-V12 inventory assertions, plus the unchanged authentication current-database-timestamp boundary. After fixing only the migration inventory assertions, a 35-test focused group passed, including authentication unchanged. The next complete clean verify also passed authentication. The initial intermittent boundary failure is retained; no assertion or authentication implementation was weakened. Its independent clock/timing root cause is not asserted proven.

The additional cached-generation regression failed before correction (the actual finalizer returned a superseded generation as active), then passed after refreshing generation rows under lock. The 24-test worker/owner group passed afterward. The final effective-role group passed 22 tests, including real worker/runtime temporary-DDL denial and V6→V13 upgrade. Final clean verification includes both corrections.

## Regression catalog

All RAG tests use synthetic data. SQL business/authority/DDL restrictions run as actual `rag_worker`; production services use a real non-owner `rag_runtime` datasource, with migration/reset credentials confined to separate fixture connections/processes.

| Class | Proof |
| --- | --- |
| RagAuthorityContractTest | Quoted/encoded/unknown metadata; positive prose; shared DAG longest path and cycle/depth bounds |
| RagSourceAuthorityIntegrationTest | Pre-dispatch NSFW/trash/revision/field denials; transitive additions; exact allowed-field/status serialization; null/empty/whitespace revision distinctions; 5/6 constant batch queries; aggregate bytes/selection/queue caps; restored external denial |
| RagWorkerBoundaryIntegrationTest | Actual Java→Python→restricted SQL→physical receipt COMMIT→activation; actual worker metrics show zero dispatch of pre-execution restricted data; unissued/forged/expired grants; wrong/forged provenance; generation-lock wait and physical COMMIT expiry; both-order stage/purge fence; deletion failure/retry; native Python restart; new Spring runtime/anti-replay; timeout/deadlock/savepoint; workflow/process measurements; cached-generation regression disposition |
| RagFinalizerOwnerConcurrencyIntegrationTest | Actual finalizer last-proof barrier with 13 genuine owner mutation paths in both orders; fresh cache reduction; concurrent disjoint edge cycle; physical outer rollback |
| RagEngineClientTest | Missing/malformed/unknown/outage cleanup; invalid result identity/stage/digests/kind/version; exact loopback origin; body stall and oversized body bounded denial |
| RagRuntimeRoleIntegrationTest | Isolated runtime profile, actual separate migrator subprocess, no ownership/elevation/DDL/Flyway-history mutation |
| RagLegacyRuntimeDeniedIntegrationTest | Unsafe legacy/admin datasource cannot register/list/authorize RAG |
| RagPgVectorCompatibilityIntegrationTest | Exact server 18.6/vector 0.8.6, all migrations and arithmetic smoke; V6→V13 repairs unsafe worker attributes/memberships/login/password and restored external authority |
| Existing lifecycle/web/SQL/NTFS tests | Restricted worker fixtures replace admin successful-staging shortcuts; serial cases labelled truthfully; helper handle cleanup preserved. NTFS primitive tests are not finalizer namespace-COMMIT proof |
| Python contracts/worker/transport/health | Strict version/stage/identity/digests; no token/payload repr; auth; unknown cleanup; duplicate execution and in-flight cancellation barriers; byte/UTF-8/duplicate-key/control-body limits; actual DB scope exercised by Java/Python integration |

The original R1–R8 identities and N/A portions remain in [the implementation report](implementation-report.md). No OCR, embedding, LLM answer/history/judge, ranking or recommendation functionality was implemented as a substitute test.

## Measured foundation baseline

Hardware: Windows 11 owner desktop, i5-9600K (6 cores), **17,112,092,672 bytes physical RAM** observed by CIM; Docker Desktop exposed 7,908 MiB to its Linux VM. Java 25.0.2, Maven 3.9.15, Spring Boot 4.1.1, Modulith 2.1.1, Docker 29.8.1; CPython 3.14.4 / uv 0.11.8. No model/GPU workload was run.

The corrected workflow benchmark executes **16 real sequential admission → actual Python HTTP → restricted staging COMMIT → Java publication COMMIT** workflows. It includes service/admission time from the caller start, with no artificial queue contention. Reported publication duration is a round-trip upper bound for source-guard hold, not an isolated lock-hold timer or measured saturation delay. Explicit lock-wait/deadlock/deadline tests provide separate correctness proof.

| Operation | p50 ms | p95 ms |
| --- | ---: | ---: |
| Admission | 25.9373 | 33.9193 |
| Actual worker round-trip | 134.5667 | 175.1382 |
| Publication transaction round-trip | 43.4958 | 57.9479 |
| End-to-end | 206.0787 | 258.9210 |

Sampled Java used-heap peak: **876,609,536 bytes**. Java process peak working set: **1,787,727,872 bytes**. Python launcher+actual child process sum of per-process peak working sets: **78,540,800 bytes**. The Java process high-water mark includes the full verification JVM's cached Spring contexts and prior test work; it is not isolated per-workflow allocation. The first launcher-only 5.4 MiB measurement was inadequate and is superseded; no claim that summed process high-water marks equal simultaneous whole-machine peak RAM. External hash/handle I/O is disabled. Controlled synthetic workers have zero content scratch files; no parser/model cache or persistent source temp product exists. This small baseline does not establish a throughput SLA, cold-cache corpus benchmark, isolated guard-hold percentiles or power-loss behavior. No performance promotion threshold is self-approved.

## Environment, supply chain and offline scope

[Database setup](../../../backend/dev/rag-db/README.md) pins PostgreSQL runtime base digest `sha256:5a5a84b19854a9ffaa54082c166ff4ec27473a361e496e5ea167f298f2da9722`; pgvector 0.8.6 source commit `8ee86c96f0fd72390f890aa8a336fda6d3ab4c6c`; archive hash `d076a3098010905fd60256649327809651f6288327db6413f0938305f62ea299`. Actual migration/extension checks passed. Build-time apt resolution is not a bit-identical snapshot guarantee. V1/V2 and frozen business DBML are unchanged.

[Worker setup](../../../rag-engine/README.md) uses the frozen lock and base `python:3.14.4-slim@sha256:2ca02f32b4d9d893863367ce07ec1972819f476dd38d8612f2a9cb6a41cbb727`. Runtime check ran as non-root with `--network none --read-only --tmpfs /tmp:size=16m --cap-drop ALL --security-opt no-new-privileges`. Real network calls/resolution failed; no sockets/resolver were mocked. DB-unavailable denial and allowed loopback health were checked in that namespace. Real restricted-DB success is separately exercised natively. A combined native Windows firewall/deployment guarantee is **not verified**.

Installed metadata records licenses/versions for all 32 Windows distributions. Direct runtime packages: FastAPI 0.143.0 (MIT), Uvicorn 0.54.0 (BSD-3-Clause), Pydantic 2.14.1 (MIT), Psycopg/binary 3.3.6 (LGPL-3.0-only). The bundled libpq reports **18.4**, not server 18.6. The [PostgreSQL security table](https://www.postgresql.org/support/security/) lists its relevant libpq fixes through 18.4; later [18.6 release notes](https://www.postgresql.org/docs/release/18.6/) include client behavior changes. Worker SSL/GSS are disabled for exact local loopback connections. This is a bounded applicability check, not an exhaustive OS/DLL advisory audit or redistribution/legal approval. No new Java dependency/model weight was added. Runtime OS isolation and build/advisory network operations are distinct.

## Canonical 29-dimension evidence ledger

These are candidate evidence dispositions, **not independent PASS/closure findings**.

| # | Canonical dimension | Candidate evidence / limit |
| --- | --- | --- |
| 1 | Owner intent/acceptance/source selection/non-goals | Existing handoff and explicit owner implementation override; no later products |
| 2 | Requirement/contract/test traceability | Per-FR implementation and original R1–R8 mapping retained |
| 3 | ADR/C4/source/schema/workflow consistency | Current inventories and worker restricted SQL edge synchronized; frozen baseline preserved |
| 4 | Module ownership/named interfaces/cycles/encapsulation | 37 architecture tests; owner public projection/guard contracts only |
| 5 | Actual recursive repository/package/folder organization | Exact file inventory; all internal descriptors retained, FR17-11 preserved |
| 6 | Shared envelopes/exceptions/contract placement | Root response family; module-local adapters/advice; generic safe errors |
| 7 | Concrete SOLID/contracts/cohesion/dependency direction | Canonical authority/grant/final gate; unchecked alternate routes removed |
| 8 | DRY/KISS/YAGNI/immutability/determinism/invariants | Shared bounded graph snapshot; sorted typed immutable scope; fresh owner batches |
| 9 | Patterns/anti-patterns | Existing facade/repository/native adapter; no speculative agent/platform |
| 10 | Domain correctness/transitions/idempotency/edges | Real lease, graph, duplicate, receipt and completion lifecycle tests |
| 11 | Public API/wire/version/errors/pagination/provenance | Strict version-2 scoped input and durable receipt; no unchecked STAGED response |
| 12 | Hostile input/format/path/bounds validation | Positive syntax/encoding veto and bounded raw JSON; live file capability disabled |
| 13 | Authn/authz/privacy/consent/least privilege | Actual roles/issued grants/current projections; no unsafe legacy runtime authorization |
| 14 | Credentials/leakage/retention/egress/supply chain | No default runtime credentials or token repr; safe errors; real Docker isolation; host firewall limit |
| 15 | Transactions/lock order/freshness/last-check/COMMIT races | Actual finalizer/owner/worker barriers, physical COMMIT expiry, cache and rollback cases |
| 16 | Failure/leases/cancel/restart/restore/cleanup | Pending on outage/deletion failure; real worker/new Spring runtime recovery; physical crash drill unverified |
| 17 | PostgreSQL/Flyway/JPA/constraints/permissions/query shape | V1–V13, exact role/receipt/COMMIT constraints, constant 5/6 batch queries |
| 18 | Time/memory/I/O/locks/hardware/admission complexity | Fixed aggregate budgets and actual small workflow baseline; no saturation guarantee |
| 19 | Retrieval/ANN/reranker/answer/recommendation quality | N/A: later phases, no model product |
| 20 | Extraction/OCR/layout/multilingual/locator fidelity | N/A: parser/OCR deferred; live external files disabled |
| 21 | Adversarial/security/concurrent/OS/worker tests | Actual PostgreSQL/worker/native primitive tests; full external namespace proof not claimed |
| 22 | Risk-oriented test/E2E/assertion/determinism/fixtures | Deterministic production barriers; actual restricted success and negative permissions |
| 23 | Hardware/latency/resource benchmark credibility | LIMITED: 16 real sequential workflows and process high-water marks; isolated guard/saturation/whole-machine peaks unverified |
| 24 | Held-out labels/evaluation leakage/judge calibration | N/A: no ML evaluation/judge product |
| 25 | Dependency compatibility/licenses/advisories/lock lifecycle | LIMITED: frozen setup/exact DB/vector and 32-package audit; not exhaustive bundled OS/DLL audit |
| 26 | Build/static/format/test/tooling/architecture evidence | Exact complete Maven/Python receipts; static diagnostics triaged, no warning-free claim |
| 27 | Safe logging/metrics/operational completion/incident handling | Source-free diagnostics; strict cleanup acknowledgement, durable retry, no false PURGE_COMPLETE |
| 28 | Documentation/governance/status/history/links/inventories | Current candidate synchronized; historical reviews untouched; new independent final review required |
| 29 | Hygiene/artifacts/frozen scope/clean diff | Generated logs/environments/caches excluded; HEAD unchanged; no commit/push |

## Remaining runtime guarantees and handback

Live external files remain **disabled** because namespace/COMMIT proof is absent. Native Windows firewall enforcement, an independently killed-JVM/machine power-loss restore drill, exhaustive OS/DLL auditing, saturation latency and isolated guard-hold/whole-machine resource peaks remain unverified. Future answer/judge/highlight/retrieval products are not certified. These limits are explicit review inputs, not silently accepted debt or self-closed findings.

Final hygiene: `git diff --check` passed. No tracked change exists in frozen V1/V2, business DBML, backend POM, authentication implementation/tests, Phase 15 or supplemental-audit history. All 12 RAG package descriptors remain. HEAD is unchanged. The [170-file candidate inventory](changed-files.md) includes inherited submission files and separately identifies seven preserved pre-existing inputs. Graphviz emitted font-library warnings; affected exports were rendered and visually inspected rather than declared warning-free.

The new independent final review returns **CHANGES_REQUESTED**; the separately authorized implementation session must remediate FR17-5/8/9/10 under the existing handoff before another independent review. FR17-11 remains closed. Phase 17 is not accepted/frozen and ACTIVE remains active. No `READY FOR OWNER COMMIT` is issued here.
