# Phase 17 Alternative B — independent final re-review, 2026-10-17

**Verdict: CHANGES_REQUESTED.** The candidate is not ready for owner commit.

The corrected source locking remains independently verified. The six added cases improve coverage, and the real 420-job benchmark remains valid. Acceptance is blocked by independently reproduced gate failures and specific remaining lifecycle/restore/test-evidence gaps. Alternative B, E1, Practical Local Offline and the single-user Windows deployment remain the accepted scope. No redesign or future-phase work is requested.

## 1. Authority, preservation and actual changes

Existing handoff **P17-RAG-FOUNDATION**, ACTIVE **CHANGES_REQUESTED**; branch `main`, unchanged owner HEAD `24e6268eef1685866d919d882ecdf9f532d01961`. Applied repository/backend/docs/test/module instructions, codex-final-review and its 29-dimension rubric, plus relevant RAG/Modulith/Java/persistence/migration/authentication/testing guidance. The explicit owner instruction authorizes this review despite the generic skill's usual intake status. Previously read unchanged instructions/contracts were reused after hash comparison.

Read the complete previous report, `2026-10-16-phase-17-alternative-b-independent-final-re-review.md`, including its 242-path appendix; verified its SHA-256 **8283f7839c837b70ac57d7ff68f3163ab6fe0722453f12cdc0d9c25d391efc85**. Inspected current ACTIVE, ADR-0020/0019, accepted P17-1/2/3/B01–B20, owner decisions, remediation/implementation/evidence/inventory documents and subsequent changed source/test bodies. Actual Git status, staged/unstaged diffs, untracked inventory and hashes are retained. Graphify was used only for navigation, with source verification.

Evidence directory **E**: `logs/phase17-alternative-b-independent-re-review-2026-10-17/`. Original submitted Surefire XML was copied before `clean`. Every application command had an actual watchdog of at most **1,200 seconds**; probe subprocesses had shorter bounds. Logs, failures, partial reports and actual exits are preserved. No clock, firewall, containment or security setting was changed by this reviewer. No failed maintained test was rerun for a lucky pass.

Review-start snapshot: **1,984 nonignored files**, no staged changes. Complete inherited delivery against HEAD: **243 paths**. Against the accepted contract-review snapshot: **73 modified + 38 added = 111 paths**, zero deletion. The current changed-files document now lists all **111** exact delta paths, but still labels them 109/72+37 and the delivery 242. These counts omit the subsequently added previous review and modified development README; scope baselines must remain explicit.

Since the previous independent review: **15 modified files, zero added/deleted**:

- Six maintained Java test files: native process, second-JVM reader, recovery child, source reservations, worker boundary and commit-acknowledgement recovery.
- One SQL test fixture: `source_state_reservations.sql`.
- Eight documentation files: runtime README, ACTIVE, benchmarks, changed-files, implementation report, owner decisions, remediation plan and test evidence.

**No production Java/Python, migration, frozen authentication code/test or historical review changed.** All V1–V17 SQL bytes match the previous independently reviewed candidate. Native supervision, source authority, admission, finalization, recovery, current-index authorization, Python ingress/SQL and Portability production code are unchanged. Reuse of their prior inspection/runtime receipts is explicitly attributed, not presented as new execution. New inspection focuses on the actual remediation and current acceptance gate.

Only this new report and isolated review evidence/tools are reviewer changes. No production/tests/migrations/accepted contracts/ADRs/owner decisions/ACTIVE/historical reports were edited. No commit, push, new handoff, containment removal or later-phase work occurred. Ignored evidence E must be transferred with the review package; it is not automatically part of a Git commit.

## 2. Independent gate and clock result

Fresh command from `backend/`:

```powershell
mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify
```

**Exit 1, not timed out; 1,071.509 seconds** under the 1,200-second watchdog. All **1,305 testcase nodes are unique** across **124 XML suites**: **1,303 PASS, two FAIL, zero ERROR/SKIP**. Maven stopped at Surefire's failed test goal; later packaging/verify lifecycle success is **NOT_VERIFIED**. The command was not rerun. See E/`maven-clean-verify.json`, `.log`, preserved per-command XML and `independent-test-accounting.json`.

The preserved submitted directory has **1,305 unique testcase nodes, all PASS**, consistent with six added methods and 151 RAG cases. That is credited as submitted test evidence, not as proof that this independent command succeeded. The displayed top-level test-evidence command table still says 1,299; newer text says 1,305. Exact original command/lifecycle exit receipts should accompany the claimed monolithic success rather than relying on overwritten XML alone.

### FR17-AB-2 / FR17-8 — High, OPEN: current clean acceptance is failed

Two failures have been independently reproduced during the current clean command:

1. `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary`, [line 145](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/RefreshTokenLifecycleIntegrationTest.java#L145): rotation did not throw after database `expires_at=now()`. Frozen implementation and assertions are unchanged.
2. `RagNativeWorkerProcessTest.finalHandleCloseActuallyStopsRecordedProcessButMissingJobIsNotRecoveryProof`, [line 128](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagNativeWorkerProcessTest.java#L128): `isRecordedProcessTerminated(pid,fileTime)` returned **false**, after closing the owned handles and awaiting `ProcessHandle.onExit().get(5, SECONDS)`. The case failed in 0.022s. This is a newly observed failure in an existing native regression, not a failure of the added zero-timeout test.

The native result is conservative: it does **not** establish a falsely released source or a surviving uncontrolled worker. `NativeWorkerProcess:327–347` returns false for an unrecognized OpenProcess error or a nonsignaled exact process; neither the specific Win32 error nor the wait result was captured by the failing test. The underlying cause is **NOT_IDENTIFIED**. Do not infer that Java's exit notification is equivalent to the native authority check, or turn access/identity uncertainty into positive termination. Diagnose the exact handle/query/wait boundary and retain a bounded positive native proof. A retry of the whole test to get green would not establish the fix. Windows termination requests alone are insufficient proof; [Microsoft's termination documentation](https://learn.microsoft.com/en-us/windows/win32/api/processthreadsapi/nf-processthreadsapi-terminateprocess) requires a process-handle wait for confirmed termination. That reference does not diagnose this particular failure.

### WSL2 calibration is a temporary environmental workaround, not demonstrated stability

Antigravity reports manually setting the WSL2 clock with `wsl -u root date -s` to within −50ms of Windows. The earlier pool-warmup explanation is correctly discarded. The current fresh independent Java/PostgreSQL probe finds:

- **20/20** PostgreSQL timestamps ahead of the entire Java request interval.
- DB minus Java-before: **+959.7204 to +969.4667 ms**.
- DB minus Java-after: **+953.4337 to +959.9881 ms**.

Thus the reported calibration has not held into this review, and the unchanged authentication boundary fails again. The measurements were from a separate disposable PostgreSQL 18.6 instance on the same Docker/WSL2 runtime; they do not instrument the exact failing rotation or prove its instantaneous offset. Combined with the unchanged two-clock code and recurring boundary failure, they support an environment-dependent mixed-clock failure, not a newly introduced Phase 17 authentication-code regression.

The reviewer did not recalibrate clocks to obtain a passing gate. A deliberately negative offset can make this exact-boundary assertion pass temporarily; that is not evidence of reliable ongoing synchronization, resume behavior or correction of the two-clock assumption. Preserve the frozen code/tests. Diagnose host/VM time service/drift and record before/after gate measurements. If a frozen authentication change ultimately becomes necessary, it needs separately authorized scope; this review grants none. RAG deadlines are established/checked with PostgreSQL time and transactional state. Their safety must not be justified merely by saying leases are long relative to an offset.

## 3. New adversarial cases: what they actually prove

### Credited remediation

**B01/B03 reassignment:** `RagSourceReservationsIntegrationTest.associationReassignmentBetweenDifferentOldAndNewOwnersRequiresBothUnreserved:274–305` reserves OLD-A, rejects A→B with P1701, then reserves NEW-B and rejects again; after both release, it verifies one reassignment and both versions advance. The SQL fixture adds differing-owner cases too. Together with maintained bulk rollback, owner backstops, actual selected/unrelated HTTP conflicts and existing buffered-worker exclusion, this materially closes the previous OLD/NEW assignment gap. It does not itself transfer any source body to Python.

**B09 storage error:** `RagCommitAcknowledgementRecoveryIntegrationTest.lowDiskOrStorageFailureDuringPublicationRollsBackAndRetainsReservationsForSafeRecovery:150–193` injects **53100 at a real deferred publication COMMIT**, verifies zero published rows, RECOVERY_REQUIRED, staging not falsely sealed, intact reservation, then safe cancellation/release. This is meaningful transaction-boundary fault injection. It is **not a filled physical disk, failed WAL flush, power loss or measured storage headroom**. Those guarantees are not claimed. Existing unknown-ACK and pending-writer tests cover complementary outcomes.

**Windows receipt handling:** child JSON is serialized to bytes, written/closed in a temporary file, then atomically moved with replacement (`RagRecoveryProbeProcess:136–143`). The reader retries **observation IOException** within a 35-second polling deadline (`RagSecondJvmRecoveryIntegrationTest.Probe.receipt:225 onward`). It does not replay admission, launch, input, publication or cancellation. Valid JSON containing `success:false` still fails; UUID path and controller identity checks remain. This is a proportionate correction to the previous sharing failure, preserving assertions rather than retrying business work.

### FR17-AB-2 / FR17-8 — remaining bounded proof gaps

**B08: the new test does not exercise failed service-level termination or reservation retention.** `RagNativeWorkerProcessTest.liveConsumerUnprovenTerminationReturnsFalseAndDoesNotClaimTreeTerminated:234–269` launches a real native process and checks it is alive. Its negative “termination” call passes **Duration.ZERO**, which throws IllegalArgumentException **before a termination attempt**. It opens a random nonexistent job, then successfully stops the real child in finally. There is no RAG job/source reservation, supervisor stop failure, pending sender/launcher or attempted release in this test. The primitive assertions are useful; they cannot substantiate the submitted “live consumer unproven termination without release” lifecycle claim. A bounded integration case must retain an actual job/reservation with a live consumer or outstanding launch/send task, force an unsuccessful/unknown stop outcome, and prove no seal/terminal/release until actual quiescence. This is a current single-desktop cancellation race, not a distributed-system requirement.

**B11: the new case simulates a changed watermark, not declared restore handling.** `RagWorkerBoundaryIntegrationTest.declaredRestoreOrUncertainSourceHistorySuspendsIndexUntilRenewedConsent:521–552` directly uses the admin connection to increment `consent_reset_version`. It correctly verifies denial, stale CAS rejection and renewed approval. It does not declare a restore to any production boundary, restore/lose authority history, execute a documented suspension/reconciliation procedure, or test unfinished-work resolution under that declaration. All production recovery code is byte-identical and no concrete declared-restore procedure is supplied beyond saying suspension/renewal is required. Supply and test the supported operator procedure using existing stop/revoke/renew mechanisms, or the minimal approved handling required by ADR-0020. No automatic hostile-restore detector or Portability import product is requested. A trusted full-machine power-loss experiment remains an expressly unverified/deferred capability, distinct from this current declared-restore contract.

**B06: actual port collision is tested; zero source dispatch and responding stale binding are not observed.** The new `staleListenerOrPortCollisionDeniesWorkerBindingWithZeroSourceDispatch:555–565` opens a bare ServerSocket and calls `supervisor.begin`, then checks no staged rows/consumed claims and stopped proof. `begin` never sends the source body; grant issuance/body dispatch occurs later in `RagJobService.executeSyntheticStage`. The listener does not accept/count requests or return stale readiness. Credit the actual bind failure and cleanup. Complete the finite assertion by invoking the production execution entry against a responding stale/wrong binding and recording zero execution-body requests/bytes, plus no grant/stage. Do not count zero SQL receipts as proof that zero bytes were transferred.

### B12: actual security behavior verified; maintained test rejects at the wrong guard

The added `workerTimelyLastDeferredGateAllowsLateStageCommitThenExpiredVerificationRejectsPublication:568–624` installs a **zz** deferred barrier after the unchanged default S deadline gate, observes a real worker backend wait, crosses the eight-second immutable fixture deadline, and observes one late committed S receipt. This is the missing positive E1(b) boundary and is credited.

However, its publication assertion accepts any RuntimeException **before** `supervisor.end/stopAndVerify`, which occur only in finally. `RagFinalizationGate:59` first rejects an unproven consumer, before entering the new verification/expiry gate. The test therefore does not establish the reason stated in its name.

An independent isolated Java/native/Python/PG observer reproduced the exact distinction, with the production deadline function unchanged:

| Observed state | Actual result |
| --- | --- |
| Last timely S gate passed; deferred barrier released after expiry | One real S receipt committed |
| Before positive stop | **Consumer termination is not proven**; reservation remains |
| After actual sender completion and verified native stop | **Job lease is inactive or expired** |
| Publication and cleanup | Zero generations; reservation released only after safe abort |

E/`late-stage-proof-v2.json` and `late-stage-runtime-v2.log` retain the proof. This independently verifies correct current E1(b)/expired-verification behavior; no production defect is alleged. The maintained regression still needs to stop/prove quiescence before asserting the specific expired verification outcome, then retain reservation/no-publication checks. Otherwise its present passing assertion could hide removal of the intended expiry guard.

The first review observer command failed: it dispatched too early in the short fixture and held the SQL barrier beyond the configured five-second statement budget, yielding Invalid worker result provenance. Exit 1/log/source are preserved. The corrected observer uses the maintained fixture's existing finalization-window helper before dispatch; no candidate code, deadline function, assertion or budget was changed. This is a disclosed review-harness correction, not a rerun of a failed maintained suite.

## 4. Locking, roles, consent, E1 and index lifetime

**FR17-AB-1 remains resolved for the reproduced cycle.** Production hashes match the previously corrected source. `VaultEntryService:133–134` acquires source before a single owner row; `163–164` acquires the whole sorted source set before `lockAndRefreshAll`. Prevalidation reads do not lock owner rows. The physical mutation guard owns row refresh only. V14 earlier-source NOWAIT backstops remain unchanged, including AFTER association triggers and BEFORE DELETE `RETURN OLD` handling. FK/uniqueness waits can precede an AFTER trigger; blanket absence of every possible PG deadlock is not asserted.

Fresh real PostgreSQL 18.6 / V1–V17 / actual `rag_runtime` probes repeat both critical interleavings:

1. Owner B holds Vault A; admission owns source A+B and waits for Vault A. B updates Vault B and gets **P1701**, rolls back, and admission **COMMITs**.
2. Owner B already owns source/Vault B. Admission fails **P1701 before taking any Vault row**; B can update A and commit.

No 40P01 in either corrected interleaving. The AFTER association probe fails P1701 in **12.994 ms** and rolls back to zero favorite rows. Actual `rag_worker` gets **42501** for business/history SELECT, policy UPDATE, release-function execution, TEMP, CREATE SCHEMA and SET ROLE. The SQL probe executes production SQL in an isolated DB with a minimal Flyway-history placeholder; fresh maintained Flyway tests, not that placeholder, establish actual Flyway execution.

Hash-preserved implementation assessment: durable nonexpiring whole-closure reservations survive connection/process failure; static uniqueness covers all six nonterminal states; owner and RAG exclusion mutations conflict without committing; retained source/reset/epoch/policy history prevents lost-event consent resurrection and stale CAS; exact description hashes remain independently consented. Dependency remove/readd changes retained history, so equal restored topology/content cannot revive an old generation. No queue, auto-resume or auto-reindex was introduced.

Actual native lifecycle remains job-scoped, suspended/confined before binding/resume/input, noninheritable handles, process-count one, no breakaway, actual creation identity and job/attempt/nonce/PID readiness. No daemon fallback. Single-use SQL claim precedes body receive. Positive task/transport/process/tree proof precedes attempt-writer drain/seal. No ACK, disconnect, work TTL or kill request frees a reservation. Current conservative native false results must remain fail closed while the newly failed test is diagnosed.

Staging uses restricted SECURITY DEFINER routes with fixed search_path/qualified objects, scoped provenance and attempt FOR UPDATE held through physical C. Worker death is separately fenced from pending SQL COMMIT. Java finalization atomically commits publication-or-abort/seal/terminal/exact release; unknown outcomes use durable reconciliation, not callback replay. Cancel-first and publication-first behavior, prior CURRENT preservation and historical-manifest purge remain covered. PURGE_COMPLETE requires verified erasure; inaccessible PENDING is not erased merely to free capacity.

**E1(a)/(b)/(c) is the approved contract.** Timely work may continue late; timely S may commit late while untrusted; timely Java verification/P may physically commit late as trusted. Observed-expiry, post-wait and default-deferred denials remain. The new independent S proof complements prior late-P and CURRENT-after-expiry/restart proofs. No strict E2 requirement is reinstated. Completed persistent Fiction/Film index lifetime remains independent of execution credentials/boot, under fresh IndexCurrent consent/source/reset/fields/full dependency checks. OUTDATED/restricted data is denied. There is no current retrieval/ranking/answer/judge product to certify. Live external files remain disabled under R7.

## 5. Resources, Practical Offline and documentation

**Reused independent benchmark, not rerun:** the current 441,531-byte native receipt exactly matches E-previous/`independent-native-benchmark-results.json`. Production, benchmark source and Python runtime code are unchanged. Previous independent command exit **0**, **707.851 seconds**, **420/420 useful jobs**, 400 measured + 20 warmups, all four fixtures complete; native counters and PID/FILETIME-matched samples cover **420/420 workers**. The reused receipt, analysis, command and sampler metadata are copied into E with `reused-` prefixes. No new 420-job execution is claimed.

| Fixture | Independently measured total p50 / p95 / max ms | New proposed p95 target |
| --- | ---: | ---: |
| Small | 1195.7 / 1364.0 / 2213.5 | 1700 ms |
| Depth five | 1212.9 / 1416.1 / 1739.7 | 1800 ms |
| 1 MiB input | 1287.0 / 1656.5 / 1753.1 | 2000 ms |
| 100 selected sources | 2129.5 / 2900.8 / 5029.1 | 3500 ms |

These targets are numerically satisfied by the existing independent baseline and reasonable as proposed bounded Foundation regression thresholds. They are neither runtime hard guarantees nor later model benchmarks. Native committed peak **56.53 MiB** satisfies the proposed 64 MiB measurement target; configured Job Object ceiling remains **512 MiB**. Connections stayed 10; JVM sampled handles 706→688, range 688–755; no accumulating count observed in that window. All jobs assert actual process death/CURRENT/seal; terminal reservations/attempts are zero. Sampled working-set maxima are lower bounds, not exact peaks. No unbounded leak guarantee is claimed.

### FR17-AB-3 / FR17-9 — Medium, OPEN: distinguish proposed thresholds from incomplete measurements

The new table is progress, but accepted §9/remediation step 5 still require source/attempt guard wait/hold, stop response versus positive death/seal/release, recovery/purge and storage/headroom measurements. The current profile explicitly discloses that stop-only time is not separated; no new component timing/guard/recovery/headroom receipt was supplied. A combined normal-work bound is useful and must be labeled as such. Do not silently remove accepted metrics by adding PASSED to a table. Complete the remaining bounded timing/storage observations using small existing fixtures; **another 420-job rerun is not requested**.

The table also labels p95 values as p50/max in combined CPU/startup rows. Actual measured worker CPU maximum is **1109.375 ms**, while the p95 is ≤843.75 ms. This does **not** breach a 1000-ms **p95** target, but the maximum column must be correct. `test-evidence.md` mixes p50s from older submitted split receipts with p95/max values from the independent combined run without identifying that source change. Preserve both runs and attribute each table to one receipt. SQLSTATE 53100 injection verifies transactional rollback, not physical disk exhaustion or a measured minimum-free-space policy. Full-system power-loss recovery and exhaustive bundled OS/DLL audits remain disclosed unverified limits, not new enterprise gates.

**Practical Local Offline remains verified for the bounded current pipeline.** Owner decisions and ADR-0020 addendum supersede firewall/WFP/global IPv4/IPv6/DNS/proxy-enforcement requirements. No physical network cut or OS packet isolation was enabled in this review; neither is required or reported passed. Exact loopback Java/Python/libpq configuration, no proxy/redirect/external AI/download route, pinned 32 installed dependencies and actual local native/PG workflows are preserved and re-exercised in focused/full checks. The previous independent ASGI/actual-worker-role socket-control proof remains hash-consistent; it is not native-libpq interception or universal bundled-library egress proof. No WFP remediation is requested.

Current documentation corrections still required: inventory counts must be **243 delivery / 111 contract delta (73+38)** before this new report, rather than 242/109; the exact path list itself is complete. Current test summary must separate the old 1,299 run from the claimed new 1,305 and this failed independent run. ACTIVE still says the monolithic gate is “ready”; keep newest evidence and current verdict unambiguous. Runtime README now correctly preserves consent/indexes on ordinary restart; remediation-plan step 6 correctly adopts Practical Offline and its migration inventory includes V17. Owner decisions now describe opaque capabilities, while ADR-0020's explanatory test bullet still says HMAC; no actual RAG HMAC verification exists. Correct explanatory evidence, not the approved security architecture. These are finite documentation corrections, not an authority to rewrite historical reviews.

## 6. B01–B20 and finding dispositions

PASS means independently verified bounded behavior, including explicitly reused unchanged evidence. NOT_VERIFIED is not a pass. Method names below refer to maintained RAG tests; actual code/SQL/native boundaries and limitations are stated.

| Case | Evidence and boundary | Disposition |
| --- | --- | --- |
| B01 | Owner-first/reset denial; `nsfwAndRemovedFieldsBeforeExecutionNeverReachActualPythonWorker`; actual buffered-worker `assertEditBlocked`; 13-owner-path finalizer tests and mandatory common SQL backstops | PASS bounded source-exclusion behavior; new OLD/NEW case closes an indirect-write gap. No claim that the reassignment method itself observes Python input. Stop/restore gaps remain B08/11. |
| B02 | Second JVM `controllerDeathWithActualBufferedHttpInputRetainsExclusionUntilVerifiedRecovery`, `crashAfterIssuedGrantCommitNeverDispatchesOrAutomaticallyResumesInput` | PASS both actual child-JVM cases in this run; no automatic input resume. |
| B03 | `associationReassignmentBetweenDifferentOldAndNewOwnersRequiresBothUnreserved`; SQL bulk/OLD/NEW fixture; `RagWebIntegrationTest` selected revoke/trash 409 and unrelated trash | PASS actual source rows, rollback, version advancement and HTTP boundaries. |
| B04 | Reservation suite `lostNsfwRoundTripCannotRestoreConsentOrAcceptOldOwnerIntent`, `delayedApprovalCannotUndoRevokeOrFieldReduction`, `privateConfigurationNeverChangesImplicitlyDuringApproval`; retained-history SQL | PASS bounded retained consent/CAS and ID/history assertions. |
| B05 | `descriptionConfirmationUsesExactBytesAndCannotRenewItemConsent`, `changedDescriptionConfirmationAndReservationCommitTogether`, `manualReindexOfNormalEditRetainsConsentWithoutAnotherApproval` | PASS exact byte hash, independent consent and atomic reservation. |
| B06 | `twoActualConcurrentManualStartsCommitOneReservationAndOneActiveAttempt`, `twoExplicitJobsUseFreshActualProcessesAndPublishVerifiedSingleUseReceipts`; new bare-port collision case; Python binding units mock claim | NOT_VERIFIED complete responding-stale/wrong-binding zero-body case; real single-job/fresh-process/claim/port denial credited. |
| B07 | Single-job pending writer/seal both orders; `RagPythonCommitTerminationIntegrationTest.nativePythonDeathDoesNotReleaseSourcesWhileItsOwnPostgresCommitIsPending` | PASS actual native death distinct from actual SQL COMMIT; legacy/role routes tested. |
| B08 | Nine native cases including new invalid-zero-timeout primitive; cancellation `unknownWorkerBindingRetainsReservationAndSingleJobSlot` | FAIL current native close/identity case; real service-level unsuccessful stop/late-task retention remains NOT_VERIFIED. |
| B09 | New `lowDiskOrStorageFailureDuringPublicationRollsBackAndRetainsReservationsForSafeRecovery`; real C/lost Java ACK and terminal-C wait cases | PASS bounded SQL storage-error rollback/unknown ACK model; not a physical disk-fill or power-loss proof. |
| B10 | `liveControllerPastLeaseExpiryPreventsSecondJvmTakeoverThenGenuineDeathAllowsRecovery`; bounded receipt retries and registered-runtime reconciliation | PASS all four actual second-JVM cases in this run; live controller not evicted on lease expiry. Native identity failure remains separately recorded in B08. |
| B11 | Normal Spring restart preserved CURRENT; new declared-restore-named test manually increments reset | NOT_VERIFIED full declared restore/suspension/reconciliation/renewal procedure. Fresh reset mismatch primitive passes. |
| B12 | Three E1 gate/wait/P cases; new actual late S; independent review observer stop-versus-expiry proof | PASS current E1 security behavior; maintained new P assertion has a wrong-guard regression gap requiring correction. |
| B13 | `normalEditPreservesConsentButMakesIndexOutdatedUntilManualReindex`, `ordinaryDependencyRemovalAndReadditionPreserveConsentButRequireManualReindex`, `newTransitiveChildInvalidatesOldGrantAndPublication` | PASS real versions, dependency history and manual regeneration. |
| B14 | `malformedStageDigestAndMissingWorkerProvenanceCannotPublish`, `wrongStageDigestScopeJobAndUncommittedResponseAreRejected`, actual restricted-role SQL and staging routes | PASS bounded typed/provenance/role enforcement; fake HTTP response tests distinguished from actual receipts. |
| B15 | Five domain-guard tests, cached/ambient/savepoint/rollback cases, fresh actual-role both-interleaving SQL probes | PASS reproduced ordering and rollback scope; no universal deadlock-free claim. |
| B16 | `rejectsQuotedUnknownAndEncodedMetadataWhileAcceptingProse`, `externalCapabilityCannotBeApprovedOrDispatchedEvenFromRestoredCatalog`; disabled-file fixtures | PASS bounded exclusions and containment; no live-file namespace certification. |
| B17 | Exact historical-manifest cleanup, `failedErasureRetainsPendingCleanupButSafeTerminalReleaseIsAllowed`, previous CURRENT and both P/cancel orders | PASS bounded real lifecycle/PENDING versus verified erasure. |
| B18 | Hash-verified previous independent 420-job/native sampler receipt; proposed new thresholds; injected 53100 | NOT_VERIFIED complete component/resource acceptance; 420 useful successes/counters/bounded leak observations credited without rerun. |
| B19 | Unchanged local-only source/configuration, fresh Python and actual native/PG cases, reused independent ASGI/restricted-role socket probe | PASS Practical Local Offline; no removed OS enforcement guarantee claimed. |
| B20 | Fresh clean command, complete Python/static/hooks, migration/architecture/Portability/roles/docs | FAIL overall clean gate; exact counts/results in §7. |

R1/R2 map to B01/02/03/07/15; R3 synthetic publication/current/purge to B13/17; R4 to B04/11; R5 to B07/13/16/17; R6 to B11/12 under approved E1; R7 to disabled B16; R8 to B04/05/13/17. No later parser/model/query/count/answer guarantee is inferred from Foundation digests.

| Finding | Current recommendation |
| --- | --- |
| FR17-5 High | OPEN pending the bounded lifecycle/declared-restore proof gaps; original unauthorized NSFW-after-grant counterexample remains prevented by durable reservations. No new unauthorized Python transfer was reproduced. |
| FR17-8 High | OPEN; independently failed gate and incomplete/incorrectly targeted regressions. |
| FR17-9 Medium | OPEN; useful benchmark and proposed thresholds verified numerically, remaining required measurements/attribution incomplete. |
| FR17-10 Medium | Local/offline configuration and useful enabled path verified under the approved amendment. Final closure remains OPEN for the native termination/cancellation/recovery proof gaps identified in B08/B11; no external leak was reproduced and no Windows firewall proof remains required. |
| FR17-1/2/3/4/6/7/11 | Preserve historical bounded closure; no new counterexample justifying reopening. |

FR17-AB-1 remains resolved; AB-2 remains open with the fresh failures and finite test gaps; AB-3 remains open for remaining measurements; AB-4's old OS criterion remains superseded rather than retroactively passed; AB-5 is substantially remediated but counts/attribution remain to synchronize.

## 7. Exact independent verification and accounting

Every command below used the E/`review_tools.py` subprocess watchdog; default 1200s, with smaller inner limits for probes. Python is the pinned `rag-engine/.venv/Scripts/python.exe`. JSON/log labels retain complete argv/cwd/exit/time and original XML. No incomplete or stale report is treated as a new test execution.

| Command / label | Actual result |
| --- | --- |
| Backend `mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify` | Exit **1**, **1,071.509s**, no timeout; **1,305 unique: 1,303 PASS / two FAIL / zero ERROR/SKIP**. |
| rag-engine `python -m pytest -q --junitxml=../logs/phase17-alternative-b-independent-re-review-2026-10-17/pytest.xml` | Exit 0; **37 PASS**, pytest 0.59s; one retained Starlette/httpx deprecation. |
| `python -m ruff check .`; `python -m ruff format --check .`; `python -m mypy src/ppv_rag` | All exit 0; **19** formatted files, **8** typed source files. |
| Root `python .agents/hooks/test_repository_safety.py` | Exit 0; **13 PASS**. |
| rag-engine `python scripts/dependency_inventory.py` | Exit 0; **32 installed distributions match uv.lock**. Not a new vulnerability-advisory or exhaustive native-library audit. |
| E/`sql_lock_probe.py` | Exit 0, **8.794s**; actual PG roles/ordering/association rollback; owned DB removed. |
| E/`clock_probe.py` | Exit 0, **8.506s**; 20 offset samples; owned DB removed. |
| E/`run_late_stage_probe.py`, first observer | Exit **1**, **62.302s**; review barrier exceeded allowed statement budget; logs/source retained. |
| Corrected observer, `late-stage-probe-v2` | Exit 0, **68.902s**; actual late S, early unproven-stop denial, later positive-stop expired denial, no publication, safe abort/release; owned fixture DB/processes closed. |
| Graphify targeted query | Exit 0 under 30s; navigation only, no graph refresh/correctness claim. |
| Benchmark | **Not rerun**; reused previous independent exit-0 **707.851s / 420 jobs**, exact hash match and unchanged execution source. Not counted as newly executed tests. |
| Documentation/hash/whitespace validator | Exit 0; **33** current changed contract/status Markdown files, **489** local targets / **21** fragments, no broken targets/anchors; new report validated separately. **29** coverage rows, **20** B rows, **243** inventory rows, no missing/extra inventory path. All **1,984** preexisting hashes preserved; only this report added. `git diff --check` exit 0. Final machine receipt is E/`documentation-validation.json`. |

Fresh Java RAG subset: **151 unique cases / 23 classes: 150 PASS, one FAIL**. The native class is **eight PASS / one FAIL**; all **four second-JVM** cases and **all six new adversarial methods** pass. The fresh selected suites also pass: ApplicationArchitecture **37**, CollectionArchitecture **3**, KnowledgeArchitecture **3**, FlywayStartup **2**, FlywayV1SchemaManifest **2**, Portability **15**, RagDomainCommitGuard **5**, PgVectorCompatibility **2**, OpenApiRouteInventory **1**. RefreshTokenLifecycle is **eight PASS / one FAIL**. These are subsets of 1,305, not additional tests. No skipped testcase appears in the fresh XML.

Fresh framework executions total **1,355 = 1,305 Java + 37 Python + 13 repository-safety**; **1,353 PASS / two FAIL**. Java uniqueness is `(classname, testcase name)` from XML, retaining parameterized display names. Python and hook counts are separately attributed. Overlapping implementation batches and the previously submitted 1,305 are not added to this number. The 420 reused benchmark jobs are operational iterations, not 420 unique JUnit methods or new executions. No fresh benchmark/advisory/physical-network-disconnect run is claimed.

After command completion, the only Java/Python process remaining is preexisting owner Java PID **14932**, present before the review; no review-owned Maven, Surefire, recovery child or Python consumer remains. Only the three preexisting PostgreSQL containers remain. Their IDs and the final process snapshot are retained in E; no unrelated process/container was stopped. No application command timed out or required forced tree cleanup. The failed first isolated observer and both maintained gate failures remain preserved.

Spring Modulith/public interfaces remain correct: RAG leaf depends only on approved Vault/Fiction/Film public capabilities; Vault owns source history and no RAG internals. Exact V1–V17 sequence remains append-only. Operational source-state exclusion preserves the 67-business-table Portability contract and original schema/enum manifest. Actual suite results, not allowlist edits alone, support the bounded findings. No backup restore/import product is certified by those export/schema tests.

## 8. Complete review-coverage ledger

| # | Dimension | Disposition and evidence |
| --- | --- | --- |
| 1 | Owner intent/scope | PASS — permanently single user/desktop/job, explicit consent/manual indexing, Alternative B/E1 retained. |
| 2 | Acceptance traceability | FINDING — B06/08/11 gaps, B12 wrong-guard assertion, resource measurement limits. |
| 3 | ADR/architecture consistency | PASS bounded architecture — unchanged production matches ADR-0020; current doc count/metric corrections separately recorded. |
| 4 | Module ownership/interfaces | PASS bounded unchanged edges; fresh architecture verification recorded in §7. No Vault→RAG or cross-owner internals. |
| 5 | Recursive package/folder hygiene | PASS — hash/inventory of complete delivery and changed test tree; module-local API/source/internal organization retained. |
| 6 | Shared contracts/exceptions | PASS — ADR-0016 root transport boundary and owner-local DTO/advice maintained; no new envelope/framework. |
| 7 | SOLID/GRASP | PASS — narrow source/physical-lock responsibilities preserved; tests add no production coupling. |
| 8 | DRY/KISS/YAGNI | PASS — one bounded lifecycle and canonical SQL history; no distributed control plane or generic coordinator added. |
| 9 | Patterns | PASS — existing repository/native/client adapter/state roles serve actual needs; no speculative abstraction. |
| 10 | Business state/consistency | NOT_VERIFIED complete lifecycle/restore acceptance; consent/CURRENT/OUTDATED/manual behavior credited. |
| 11 | API/wire | PASS bounded HTTP/CAS/conflict tests; no new API or frozen HTTP behavior change. |
| 12 | Input bounds/validation | PASS — retained typed digest/schema/header/byte/count/depth checks and negative fixtures. |
| 13 | Authentication/privacy/consent | FINDING — frozen two-clock boundary fails; RAG categorical exclusions/provenance remain, no new leak reproduced. |
| 14 | Secrets/retention/offline | PASS bounded Practical Offline/source-free receipts; no OS packet-isolation or exhaustive DLL guarantee claimed. |
| 15 | Transactions/locking | PASS reproduced scope — source-first both orders, actual-role P1701, pending writer/atomic finish; no E2/universal deadlock claim. |
| 16 | Failure/recovery/operations | FINDING — native close-proof failure and incomplete service-stop/declared-restore proof; receipt retry correction credited. |
| 17 | Database/Flyway/permissions | PASS bounded SQL/migration/role scope; all migrations unchanged, actual PG denials and Flyway tests. |
| 18 | Resource/time/memory/I/O | FINDING — valid 420-job profile/caps; remaining component/headroom measurements and metric attribution. |
| 19 | Retrieval/recommendation quality | N/A — no such product in Phase 17 Foundation. |
| 20 | Parser/OCR/multilingual fidelity | N/A — deferred ingestion; live external-file capability disabled. |
| 21 | Adversarial security verification | NOT_VERIFIED full B matrix; real SQL/native proof credited, exact gaps in §3/§6. |
| 22 | Test quality/determinism | FINDING — full gate failures and maintained wrong-guard/invalid-timeout claims; no weakened assertions or lucky gate retry. |
| 23 | Performance methodology | FINDING — exact valid baseline reused; proposed thresholds numerically met, independent component measurements still incomplete. |
| 24 | Evaluation science/held-out data | N/A — later parser/model/query evaluation gate, not synthetic Foundation receipt validation. |
| 25 | Dependencies/licenses | PASS pinned-inventory scope — 32 current installed versions match unchanged lock; prior audits attributed, no fresh exhaustive advisory claim. |
| 26 | Build/static/tooling | FINDING — Python/hooks pass; current clean Maven fails; warnings/deprecations not called warning-free. |
| 27 | Logging/observability | PASS bounded safe receipts/errors; exact failures retained, native failure lacks required diagnostic cause rather than a fabricated explanation. |
| 28 | Documentation/governance | FINDING — handoff correctly CHANGES_REQUESTED; metric/count/current evidence corrections remain; history immutable. |
| 29 | Delivery/frozen preservation | PASS — production/auth/migrations/prior reports unchanged; new report plus isolated ignored evidence only. |

### Groups A–D status

| Group | Implemented / independently verified status |
| --- | --- |
| A — source history, consent, reservations and mutation guards | Implemented; source-before-owner correction and both reproduced SQL interleavings verified. Current reset/CAS and ordinary-edit behavior verified; declared-restore procedure remains B11. |
| B — single-job admission, confined consumer and recovery | Implemented; actual process, claim and second-JVM recovery cases executed. Native termination regression fails; B06/B08 service-boundary subcases remain incomplete. |
| C — sealed staging, atomic finalization, IndexCurrent and cleanup | Implemented; actual SQL writer fence, storage-error rollback, late S and expiry distinction verified. Maintained B12 expiry assertion still targets the earlier stop guard. |
| D — regression, benchmark, offline and governance gate | Partially independently verified. Python/static/hooks and reused useful-job profile credited; clean acceptance fails and finite measurements/documentation remain. Implementation claims do not establish final acceptance. |

## 9. Bounded next owner action

Return this candidate to Antigravity under the **existing P17-RAG-FOUNDATION handoff**, keeping CHANGES_REQUESTED and default containment:

1. Diagnose the failed authentication environment gate without changing frozen auth code/assertions or choosing a lucky offset/run. Preserve before/after drift and full failure receipts. Diagnose the native close/identity failure with actual Win32 handle/query/wait diagnostics; keep unknown termination fail closed.
2. Complete the specific B06 responding stale-binding/zero-body observation, B08 service-level unsuccessful-stop/late-task reservation test and B11 supported declared-restore procedure/test. Correct B12's maintained assertion to reach the expired gate after positive stop. Reuse the credited B03/B09 and independent late-S proof.
3. Supply only the remaining bounded resource component/storage observations and correct measured/percentile/run attribution. Do not rerun all 420 jobs merely to regenerate existing valid evidence; do not restore obsolete multi-job saturation or firewall requirements.
4. Synchronize current status/counts/newest review links while preserving every historical review/failed receipt. Run the next clean gate only after a documented correction, then return for independent review.

No commit message or owner commit readiness is issued. This review does not authorize production fixes, a new handoff, containment removal, commit/push, Phase 17 closeout or later phases.

## Appendix — exact review-start changed-file inventory

All **243** inherited changed/untracked paths versus owner HEAD, sorted below. Full SHA-256 values are in E/`delivery-inventory.json` (file SHA-256 `9a0350ae8bfaf06d48aeac276e5e3683bf92a04056e5ca7ed1c9b1561db18e75`). `M`/`A` means modified/added versus the accepted contract-review snapshot; `I` means inherited and unchanged versus that snapshot. `R` marks a subsequent remediation change versus the immediately previous independent review. These are attribution labels, not Git staging or test statuses. The new report itself is the sole additional nonignored review delivery path.

| Path | Contract-baseline attribution | Subsequent remediation |
| --- | --- | --- |
| `.codex/config.toml` | I |  |
| `.gitignore` | I |  |
| `AGENTS.md` | M |  |
| `backend/AGENTS.md` | M |  |
| `backend/compose.rag.dev.yml` | I |  |
| `backend/dev/rag-db/ProvisionRagDevelopment.java` | I |  |
| `backend/dev/rag-db/README.md` | M | R |
| `backend/dev/rag-db/provision.ps1` | I |  |
| `backend/dev/rag-postgres/Dockerfile` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/ApiExceptionHandler.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/OpenApiConfiguration.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionSourceService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionSourceValues.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/infrastructure/persistence/FictionStoryArchetypeRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/infrastructure/persistence/FictionWorldSettingRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/FictionSourceOperations.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/FictionSourceProjection.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmSourceService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmSourceValues.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmGenreAssignmentRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmStoryArchetypeRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmWorldSettingRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/FilmSourceOperations.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/FilmSourceProjection.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagConsentCommand.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagFinalizationResult.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagIndexCommand.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobAdmissionResult.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobBusyException.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobStatusView.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagOperations.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagSourceChangedException.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDependencyGraph.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagFinalizationGate.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagGenerationState.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagJobService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagLimits.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagManifestScope.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagPurgeService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRecoveryService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeIdentity.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeSecurity.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceAuthority.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerGrantService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerSupervisor.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagDependency.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagFieldAllowlist.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagIndexGeneration.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagJobLease.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagRun.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagSource.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagStagedOutput.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagWorkerGrant.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/NativeWorkerProcess.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/RagEngineClient.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagDependencyRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagIndexGenerationRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagJobLeaseRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagRunRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagSourceRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagStagedOutputRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/WindowsNativeFileGuard.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/advice/RagExceptionAdvice.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/advice/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagJobController.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagSourceController.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/AdmitJobRequest.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/ApproveSourceRequest.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/FinalizeGenerationRequest.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagFinalizationResponse.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagJobAdmissionResponse.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagJobStatusResponse.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagSourceResponse.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RegisterDomainSourceRequest.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RegisterFileSourceRequest.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/mapper/RagWebMapper.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/mapper/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagAccessState.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagClassification.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagSourceType.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagSourceView.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceEditGuard.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceIndexReservations.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceVersion.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceVersions.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/VaultEntryOperations.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/VaultSourceGuard.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryMutationGuard.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultSourceReservations.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/infrastructure/persistence/VaultEntryRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/package-info.java` | M |  |
| `backend/src/main/resources/application-rag-runtime.yml` | I |  |
| `backend/src/main/resources/application.yml` | I |  |
| `backend/src/main/resources/db/migration/V10__rag_complete_worker_authority.sql` | I |  |
| `backend/src/main/resources/db/migration/V11__rag_disabled_source_tombstones.sql` | I |  |
| `backend/src/main/resources/db/migration/V12__rag_exact_worker_dependency_proof.sql` | I |  |
| `backend/src/main/resources/db/migration/V13__rag_effective_database_privileges.sql` | I |  |
| `backend/src/main/resources/db/migration/V14__rag_source_state_and_reservations.sql` | A |  |
| `backend/src/main/resources/db/migration/V15__rag_single_job_and_sealed_worker.sql` | A |  |
| `backend/src/main/resources/db/migration/V16__rag_current_read_and_terminal_scrub.sql` | A |  |
| `backend/src/main/resources/db/migration/V17__rag_retained_consent_history.sql` | A |  |
| `backend/src/main/resources/db/migration/V3__rag_foundation.sql` | I |  |
| `backend/src/main/resources/db/migration/V4__rag_worker_security.sql` | I |  |
| `backend/src/main/resources/db/migration/V5__rag_staging_definer_and_indexes.sql` | I |  |
| `backend/src/main/resources/db/migration/V6__rag_security_hardening.sql` | I |  |
| `backend/src/main/resources/db/migration/V7__rag_issued_worker_grants.sql` | I |  |
| `backend/src/main/resources/db/migration/V8__rag_commit_deadlines_and_consent_instances.sql` | I |  |
| `backend/src/main/resources/db/migration/V9__rag_runtime_authority.sql` | I |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/ApplicationArchitectureTests.java` | I |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/FlywayStartupIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/migration/FlywayV1SchemaManifestIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractEnabledRagIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractRagIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagAuthorityContractTest.java` | I |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagCancellationRecoveryIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagCommitAcknowledgementRecoveryIntegrationTest.java` | A | R |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagConsumptionContainmentTest.java` | I |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagDomainCommitGuardIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagE1ExpiryIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagEngineClientTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFinalizerOwnerConcurrencyIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFoundationBenchmark.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLegacyRuntimeDeniedIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLifecycleIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagNativeWorkerProcessTest.java` | A | R |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPgVectorCompatibilityIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPythonCommitTerminationIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRecoveryProbeProcess.java` | A | R |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRuntimeRoleIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSecondJvmRecoveryIntegrationTest.java` | A | R |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSingleJobRuntimeIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceAuthorityIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceReservationsIntegrationTest.java` | A | R |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagStagingCommitFenceIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagTestCommands.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWebIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWindowsNtfsFileGuardTest.java` | I |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerBoundaryIntegrationTest.java` | M | R |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerFixture.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerPostgresPrivilegeIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` | I |  |
| `backend/src/test/resources/rag/retained_consent_history.sql` | A |  |
| `backend/src/test/resources/rag/single_job_staging.sql` | A |  |
| `backend/src/test/resources/rag/source_state_reservations.sql` | A | R |
| `docs/adr/0019-local-rag-before-frontend.md` | I |  |
| `docs/adr/0020-phase-17-single-job-reservations-e1.md` | M |  |
| `docs/adr/README.md` | I |  |
| `docs/architecture/README.md` | I |  |
| `docs/architecture/api-architecture.md` | I |  |
| `docs/architecture/data-architecture.md` | I |  |
| `docs/architecture/diagrams/exported/container.png` | I |  |
| `docs/architecture/diagrams/exported/container.svg` | I |  |
| `docs/architecture/diagrams/exported/module-dependencies.png` | I |  |
| `docs/architecture/diagrams/exported/module-dependencies.svg` | I |  |
| `docs/architecture/diagrams/exported/module-dependency-overview.png` | I |  |
| `docs/architecture/diagrams/exported/module-dependency-overview.svg` | I |  |
| `docs/architecture/diagrams/source/container.dot` | I |  |
| `docs/architecture/diagrams/source/module-dependencies.dot` | I |  |
| `docs/architecture/diagrams/source/module-dependencies.drawio` | I |  |
| `docs/architecture/module-boundaries.md` | I |  |
| `docs/architecture/module-dependency-matrix.md` | I |  |
| `docs/architecture/rag-architecture.md` | I |  |
| `docs/architecture/structurizr/workspace.dsl` | I |  |
| `docs/database/README.md` | I |  |
| `docs/database/phase-17-alternative-b.dbml` | I |  |
| `docs/implementation/handoffs/ACTIVE.md` | M | R |
| `docs/implementation/phase-17/README.md` | M |  |
| `docs/implementation/phase-17/alternative-b-verification-plan.md` | M |  |
| `docs/implementation/phase-17/antigravity-continuation-prompt.md` | A |  |
| `docs/implementation/phase-17/architecture-simplification-proposal.md` | I |  |
| `docs/implementation/phase-17/benchmarks-and-acceptance.md` | M | R |
| `docs/implementation/phase-17/changed-files.md` | M | R |
| `docs/implementation/phase-17/contract-synchronization.md` | I |  |
| `docs/implementation/phase-17/implementation-report.md` | M | R |
| `docs/implementation/phase-17/integration-ownership-and-permissions.md` | I |  |
| `docs/implementation/phase-17/owner-decisions.md` | M | R |
| `docs/implementation/phase-17/p17-3-consumption-fence-proposal.md` | I |  |
| `docs/implementation/phase-17/remediation-plan.md` | M | R |
| `docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review-2.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review-3.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-alternative-b-independent-contract-synchronization-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-architecture-simplification-independent-design-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-independent-final-codex-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-p17-3-consumption-fence-independent-design-re-review-2.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-p17-3-consumption-fence-independent-design-re-review-3.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-p17-3-consumption-fence-independent-design-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-16-phase-17-alternative-b-independent-final-re-review.md` | A |  |
| `docs/implementation/phase-17/reviews/2026-10-16-phase-17-alternative-b-independent-final-review.md` | A |  |
| `docs/implementation/phase-17/reviews/README.md` | I |  |
| `docs/implementation/phase-17/revocation-and-derived-data.md` | I |  |
| `docs/implementation/phase-17/source-eligibility-and-allowlists.md` | I |  |
| `docs/implementation/phase-17/test-evidence.md` | M | R |
| `docs/implementation/rag-phase-plan.md` | I |  |
| `docs/repository/repository-package-tree.md` | I |  |
| `docs/roadmap.md` | I |  |
| `rag-engine/.dockerignore` | I |  |
| `rag-engine/.python-version` | I |  |
| `rag-engine/Dockerfile.offline-check` | I |  |
| `rag-engine/README.md` | I |  |
| `rag-engine/pyproject.toml` | I |  |
| `rag-engine/scripts/dependency_inventory.py` | M |  |
| `rag-engine/scripts/offline_check.py` | I |  |
| `rag-engine/scripts/worker_boundary_check.py` | A |  |
| `rag-engine/src/ppv_rag/__init__.py` | I |  |
| `rag-engine/src/ppv_rag/config.py` | M |  |
| `rag-engine/src/ppv_rag/contracts.py` | I |  |
| `rag-engine/src/ppv_rag/db.py` | M |  |
| `rag-engine/src/ppv_rag/main.py` | M |  |
| `rag-engine/src/ppv_rag/security.py` | I |  |
| `rag-engine/src/ppv_rag/transport.py` | M |  |
| `rag-engine/src/ppv_rag/worker.py` | I |  |
| `rag-engine/tests/conftest.py` | A |  |
| `rag-engine/tests/test_bound_ingress.py` | A |  |
| `rag-engine/tests/test_consumption_containment.py` | I |  |
| `rag-engine/tests/test_contracts.py` | I |  |
| `rag-engine/tests/test_health.py` | I |  |
| `rag-engine/tests/test_transport.py` | M |  |
| `rag-engine/tests/test_worker.py` | M |  |
| `rag-engine/uv.lock` | I |  |
