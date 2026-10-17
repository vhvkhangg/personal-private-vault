# Phase 17 Alternative B — independent final re-review 3, 2026-10-17

**Verdict: READY FOR OWNER COMMIT.**

The reviewed Alternative B candidate satisfies the current bounded Phase 17 Foundation acceptance requirements. AB-6 and the full-context B08 failure are resolved in independently executed evidence. No remaining blocking RAG defect or mandatory RAG verification gap was found. AUTH-CLOCK remains an explicitly OPEN, separately tracked Authentication/environment issue; its approved narrow exception was **not exercised**, because the exact enabled test and the entire clean command passed in this execution. Low factual documentation notes and the required owner handback are identified below. This is implementation acceptance for owner commit, not phase closeout, deployment or containment-removal authorization.

## 1. Authority, inventory and review boundaries

Existing handoff **P17-RAG-FOUNDATION**, branch `main`, unchanged owner HEAD `24e6268eef1685866d919d882ecdf9f532d01961`. ACTIVE remains CHANGES_REQUESTED at review intake. The explicit owner instruction authorizes this independent review under that status. Applied codex-final-review and its 29-dimension rubric, root/backend/test/docs instructions and relevant RAG, Java, persistence, testing, authentication and Modulith guidance. Unchanged instructions/source inspections were reused after hash comparison; no production authority is inferred.

Read the complete 480-line [previous independent report](2026-10-17-phase-17-alternative-b-independent-final-re-review-2.md), including all 244 inventory entries. Its SHA-256 remains **28ba5cff2ab304911da86a6c615ed6e4794c1d37173a9453fe182579a361d3aa**. Inspected current ACTIVE, owner decisions, ADR-0020/0019, P17-1/2/3, B01–B20, resource/full/offline acceptance, remediation, implementation, test evidence and exact inventory. Historical reviews and failed receipts remain preserved. Graphify was used for navigation only; current source/contracts govern.

Evidence directory **E**: `logs/phase17-alternative-b-independent-re-review-2026-10-17-3/`. It contains the initial Git status/diffs, submitted XML before clean, hashes, current changed-source copies, subsequent diffs, independent command receipts/logs/XML, controlled observers, resource audit and final documentation/preservation checks. Every application command has an actual watchdog of **at most 1,200 seconds**; probes have shorter limits. No clock calibration, firewall/security change, containment removal, test exclusion or assertion weakening occurred. Cleanup targets only owned processes/containers. Ignored evidence must accompany the owner review package separately from any Git commit.

Review-start snapshot: **1,986 nonignored files**, no staged patch, **245 inherited delivery paths** versus HEAD. Implementation delta versus accepted contract-review snapshot: **74 modified + 40 added = 114**, no deletion. `changed-files.md` lists exactly those 114 paths, with no omission/extra. Its earlier 244/113 counts are explicitly identified as historical review-start counts. This new report adds one review path: final delivery **246**, contract delta **115 (74 modified + 41 added)**, not another implementation file.

Exactly **12 preexisting files changed since re-review 2**:

| Category | Exact subsequent files |
| --- | --- |
| Production | `RagPurgeService.java`, `RagWorkerSupervisor.java` |
| Tests | `RagWorkerBoundaryIntegrationTest.java` |
| Documentation | ACTIVE; alternative-b-verification-plan; benchmarks-and-acceptance; changed-files; implementation-report; owner-decisions; remediation-plan; revocation-and-derived-data; test-evidence |

No other production/test/Python/migration/dependency/architecture file changed. In particular, frozen Authentication implementation/assertions, V1–V17 SQL, source/consent authority, admission/worker ingress, NativeWorkerProcess, finalization/IndexCurrent and Portability remain hash-identical to the previous independently reviewed candidate. Review-start source copies and exact available prior/current diffs substantiate the focused inspection. All 1,986 preexisting files are preserved by this reviewer; only this report and isolated ignored review evidence/tools are added. No commit/push, new handoff, implementation/test/contract/ADR/ACTIVE edit or later-phase work is performed.

## 2. Actual full verification and accounting

Independent command from `backend/`, with native access required by the real Windows tests:

```powershell
mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify
```

Actual exit **0**, **812.432 seconds**, **no timeout**, **BUILD SUCCESS** including packaging/repackage. Started `2026-10-17T07:58:36.414259Z`; Maven finished `2026-10-17T15:12:08+07:00`. E/`maven-clean-verify.json`, full log and copied fresh Surefire XML preserve this independent monolithic execution. Actual result: **1,308 PASS / zero FAIL / zero ERROR / zero SKIP**. It was not assembled from focused batches, retried until green or accepted through the Authentication exception.

The submitted XML snapshot contains **1,308 unique testcase nodes, all PASS**, across **124 XML files**, all without a focused `test` selector. This is consistent with the added two methods. It supports the submitted count but is not substituted for the fresh command's result. Current suite counts distinguish test executions from operational benchmark jobs; overlapping older batches are not summed. There is no blanket skip, test filter or test-failure-ignore flag in the independent command.

Fresh XML independently counts **1,308 unique `(classname, testcase name)` nodes in 124 suites**; all pass. The **154 RAG tests / 23 classes** are an included subset, not additional executions. Worker-boundary **25**, native-process **9**, second-JVM recovery **4**, domain commit guard **5**, pgvector compatibility **2**, ApplicationArchitecture **37**, CollectionArchitecture **3**, KnowledgeArchitecture **3**, FlywayStartup **2**, FlywayV1SchemaManifest **2**, Portability **15**, OpenAPI route inventory **1** and Authentication lifecycle **9** all pass within that command. E/`independent-test-accounting.json` preserves every exact node/status and suite count. Benchmark jobs, submitted XML and overlapping historical batches are not added to this count.

## 3. AB-6 and B08: physical terminal reconciliation

The change in [RagPurgeService](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagPurgeService.java#L73) recognizes an already committed terminal attempt under the existing graph barrier **before** demanding that its now-released reservation still exist. It checks the exact job/attempt and staging-closed state. This is a plain committed-state read, not an earlier-order attempt row lock. If not terminal, the original mutation sequence remains **graph → reserved source rows → attempt FOR UPDATE → seal/terminal/erasure/release → physical COMMIT**. The separate attempt-only stop-request transaction remains separate and acquires no later source locks. Ambient transaction entry is still rejected.

Before starting that final transaction, positive worker/dispatch/transport quiescence is required. The new read-only terminal branch recognizes the proof already committed by another trusted finisher; it does not treat a missing handle, TTL, HTTP acknowledgement or generic reservation error as proof. Only the guarded Java terminal path can produce the reviewed atomic terminal/seal/release state; the worker role cannot write those tables. If a writer or COMMIT is still pending, the first transaction retains the graph/attempt locks until its actual outcome, and the second cannot observe that terminal record prematurely. No publication/cleanup callback is replayed and no release-only transaction is added.

This distinction matters: the new branch does **not** separately reopen every process handle or re-release a source. A committed safe terminal record attests the prior process/writer proof and atomic release. Requiring the forgotten session to still exist would recreate the defect. Unknown/nonterminal state follows the original fail-closed path. The added `isSupervised` accessor is not used as a termination certificate; a session's presence alone establishes no safety.

The maintained regressions now coordinate two actual finish actors at PostgreSQL's existing graph advisory lock:

- `concurrentFinishAbortFirstThenCancelReconcilesSafeTerminalOutcome`;
- `concurrentFinishCancelFirstThenAbortReconcilesSafeTerminalOutcome`.

Both require actual confined-worker stop first, observe both actors blocked after their stop-request transactions, then require both callers succeed, terminal/sealed/PURGE_COMPLETE state, zero staged rows and fresh admission. Fresh admission also proves source reservation and static active-slot release. The original B08 test retains its unproven-cancellation failure assertion and positive stop assertion; accepting **CANCELLED or ABORTED** afterward is legitimate because the existing maintenance reconciler may finish the same job. It does not allow ACTIVE, unsealed state, premature release or presumed process death.

### Independent replay of the original counterexample

E/`ReviewFinishProof.java` independently reproduces the old controlled sequence with current classes, real native worker and PG: first recovery-style abort and second owner cancel both wait after stop-request COMMIT; the barrier is released. Result:

| Observation | Actual result |
| --- | --- |
| First abort | true |
| Second cancellation | SUCCESS |
| Durable state | ABORTED |
| Staging | sealed |
| Exact source reservation | released |
| Server reservation-authority error | absent |

Observer exit **0**, **33.127s** (E/`finish-proof-result.json`). The original false-denial counterexample is prevented. This is a distinct controlled verification of the former failing interleaving, not a retry of a failed suite. Maintained both-order/B08 execution results are recorded in §7–8. No change to source-before-owner ordering, worker privileges, leases or the approved architecture is needed.

## 4. Approved AUTH-CLOCK exception

The owner approval is now explicitly recorded in `owner-decisions.md:3–16`, ACTIVE's verification section, B20, remediation step 4 and benchmark/full-gate text. Its scope matches the owner's current instruction:

1. Only `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary`.
2. Only the independently evidenced Windows/WSL2 PostgreSQL–JVM clock boundary, with frozen Authentication code/assertions enabled and unchanged.
3. Every other mandatory Phase 17 test and acceptance condition must pass.
4. Actual failures and Maven exit status remain reported; a failed clean command is never called PASS.
5. No RAG defect, privacy, lifecycle, recovery, resource or security requirement is waived. Authentication is not declared fixed or accepted.

The initial hashes/Git delta confirm no Authentication implementation/test or Maven test-selection change. The unchanged test writes DB `now()` and compares via Java rotation using Java Clock; the previously reproduced mixed-clock failures and old failed exits remain valid historical evidence. This review makes no synchronization repair or stability claim. A passing execution of the test does not close **AUTH-CLOCK — OPEN**. Any different failing test, or a different cause, is outside the exception.

The fresh XML contains `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary` **PASS**, with all nine lifecycle methods passing, and Maven actually exits 0. Thus **no acceptance exception was used for this command**. The known issue nevertheless remains OPEN: one successful execution establishes neither lasting clock synchronization nor a frozen Authentication fix. The original failed receipts remain failed and preserved. No additional clock probe or calibration was necessary to attribute a failure, since this run had none.

No additional owner architecture/security exception is inferred. This record resolves the former governance question about independently tracking this known issue outside RAG; it is not permission for a frozen Authentication change or deployment.

## 5. Resource acceptance: actual evidence, bounds and limits

**The 420-job independent receipt is reused, not rerun.** SHA-256 remains **9ab7d82bb62cf1b46eda0fc02ec1804af3d34070e03da8d7c4c2aa42bbccd082**; 400 measured cold jobs + 20 warmups, **420/420 useful successes**, all four fixtures and boundary checks complete. Benchmark source, Python, native accounting and normal publication path are unchanged. The current changes affect abort reconciliation and add an unused session-presence query, not the measured normal publication workload. Prior actual native counters, PID/FILETIME-matched sampler coverage of all 420 workers and owned-process cleanup remain credited with their original limits.

| Fixture | Actual total p50 / p95 / max ms | Current p95 target |
| --- | ---: | ---: |
| Small | 1195.6928 / 1364.0453 / 2213.5083 | 1700 ms |
| Depth five | 1212.9050 / 1416.0845 / 1739.7331 | 1800 ms |
| 1 MiB input | 1286.9763 / 1656.4676 / 1753.1422 | 2000 ms |
| 100 sources | 2129.4832 / 2900.7667 / 5029.1344 | 3500 ms |

All measured p95 targets pass. Combined startup/stage/stop p50 is **1040.0345–1443.7374 ms**, p95 **1168.9026–1762.1168 ms**, max **2993.9197 ms**; these are combined operation times. Native CPU p95 ≤**843.75 ms** meets 1000 ms, actual maximum **1109.375 ms**; committed peak **56.53 MiB** meets the 64 MiB measurement target and configured 512 MiB job ceiling. Boundary SQL connection count stays ten; sampled JVM handles 706→688, range 688–755; no accumulating handles observed in that window. This is not a universal leak or resident-memory-peak guarantee.

### Guard/attempt bounds and actual cleanup/recovery observations

The unsupported 2–15/10–25/15–45-ms isolated claims have been removed. The prior review explicitly permitted justified operation-level upper bounds instead of inventing lock-only measurements. Actual source admission and publication calls enclose their guard/attempt transactions and physical COMMIT, so their full round trips conservatively bound those holds in the measured fixtures. Across all four workloads, admission maximum is **1302.6151 ms**, publication maximum **850.0647 ms**. The unchanged 3s/5s/10s lock/statement/transaction ceilings and actual denial/wait tests remain enforced. The prior real-owner NOWAIT conflict observation **12.994 ms** is a single contention observation, not a percentile. No isolated SQL lock-hold distribution or obsolete 16-job saturation requirement is claimed.

To independently complete the operational evidence rather than infer new component timings, this review executed isolated real Java→Python→restricted-PG fixtures with actual committed producer receipts:

- E/`cleanup-timing-result.json`: three stopped-staged cancellations, **51.8836–100.8315 ms**; three stopped RECOVERY_REQUIRED maintenance reconciliations, **59.7329–169.6731 ms**. Each verifies terminal state, PURGE_COMPLETE, zero stage/grant rows and released source token. Command exit 0, **46.594s** including fixture startup.
- E/`cleanup-timing-result-v2.json`: a separate explicit ingress/normal-native-stop observation, then the same complete cleanup boundaries. Six normal native stop calls **8.0632–15.7996 ms**; three cancellation/seal/release/erasure round trips **60.8565–220.8943 ms**; three stopped RECOVERY_REQUIRED reconciliation/erasure calls **58.1384–306.4130 ms**. All outcomes succeed; exit 0, **40.541s**. This intentionally adds stop instrumentation to the isolated observer; no maintained test is retried or edited.

E/`historical-purge-result.json` adds **three actual published CURRENT generations**, each explicitly revoked before historical maintenance cleanup. Revocation round trips are **49.7679–75.3490 ms**, historical reconcile/purge round trips **76.3818–175.4428 ms**. Each asserts REVOKED access before erasure, then zero staged/grant rows, inactive generation with source/dependency hashes scrubbed and PURGE_COMPLETE; the historical job correctly remains COMPLETED. Command exit **0**, **32.185s** including compilation/fixture startup (runtime **30.274s**). These are measured revocation/purge calls, not inferred from a source-state update.

These are disclosed **small Foundation fixture observations**, with n=3 per cancellation/recovery/purge operation and n=6 normal stops in the second observer, not invented p95s, isolated per-statement timings, normal-stop SLAs or claims about terminating an unkillable process. The already verified prior live-dispatch probe retained its exact reservation/unsealed staging for the **10042.2651-ms** failed-quiescence call, followed by safe **115.1269-ms** terminal cancellation. Safety-preserving indefinite uncertainty remains allowed; deadlines never release the source.

Storage growth in the cited 420-job receipt is **12,859,071 → 16,168,639 bytes**, increase **3,309,568 bytes (3.156 MiB / 3.31 MB)**, roughly **7.70 KiB/job**. Retained-job/catalog/edge caps bound supported operational metadata; capacity denial never deletes anti-replay history to admit work. Whole-workspace free-space changes include unrelated activity and are not per-job disk I/O or a new minimum-free-space policy. Memory headroom arithmetic and native read/write counters retain their actual meanings. The deterministic real SQL 53100 rollback test remains the supported storage-failure proof; physical drive fill is not required.

**PASS for the approved Foundation resource profile.** The credited 420-job raw samples satisfy the actual useful-success/latency/resource thresholds. Actual admission/P round trips provide accurately identified guard/attempt upper bounds; existing batch/query/capacity/storage-failure evidence remains valid; the newly executed small fixtures supply the previously missing normal-stop/recovery/cleanup/published-purge observations. They have disclosed sample sizes and safety/erasure assertions. No unsupported component range is needed for acceptance. The remaining unit/fixture attribution notes in §6 are factual synchronization, not an unmet threshold or a missing runtime guarantee. This disposition does not invent a new timing distribution, component SLA, isolated-lock benchmark, physical disk-fill test or multi-job requirement.

Power-loss recovery, exhaustive OS/DLL auditing, exact whole-machine/database-server peaks and later parser/model/GPU workloads remain explicitly unverified/deferred limits. Live external-file consumption remains disabled. These are not newly accepted silent guarantees and do not authorize deployment or later phases. No OS-enforced egress/Windows Firewall/WFP requirement is reinstated.

## 6. Documentation and governance

The current inventory reconciles exactly, latest independent-review link is current at intake, AUTH-CLOCK rules are synchronized, default containment is unchanged and no implementer self-closure occurred. The new terminal read stays within RAG; Vault ownership/public interfaces, ADR-0020, frozen v1 schema/Modulith boundaries and appended V1–V17 are preserved.

**Low, nonblocking factual documentation notes** remain; the independently verified raw figures below are authoritative review evidence, not additional acceptance criteria:

- `benchmarks-and-acceptance.md:124` and `implementation-report.md:47` describe admission/publication maxima **95.9/258.9 ms as across-fixture bounds**. Those are the **small-fixture maxima**, not global bounds. Small-fixture actual p95s are **74.9595/133.1900 ms**, not the unattributed **80.2/185.3**. Use the named fixture or global maxima **1302.6151/850.0647 ms** from the cited receipt; none breaches a stated acceptance limit. Do not mix older split-run numbers into the unified receipt.
- `benchmarks-and-acceptance.md:127` has the correct growth difference, but the cited receipt's before/after bytes are **12,859,071/16,168,639**, not **13,484,032/16,793,600**. Correct their MiB/MB labels consistently. Sampled worker working set **71,688,192 bytes ≈68.37 MiB** is correctly distinguished from committed memory.
- Operator API names are corrected, but constructor examples in `owner-decisions.md:63–64` and `revocation-and-derived-data.md:225–227` remain schematic/wrong-arity. Actual `rag/api` records are `RagConsentCommand(approvedFields, expectedSourceVersion, expectedConsentResetVersion, expectedAuthorizationEpoch, expectedFieldPolicyVersion, confirmedDescriptionHash)` and `RagIndexCommand(expectedSourceVersion, expectedAuthorizationEpoch, expectedFieldPolicyVersion, descriptionConfirmations)`. Use these exact signatures or clearly label pseudocode; actual source/CAS enforcement and the maintained operator sequence are correct.

These notes do not conceal missing raw measurements or a performance-limit/security failure: the complete corrected data and new operation-level observations are retained in E and this report. They belong in the final factual/status synchronization, not a new RAG implementation/redesign cycle. This reviewer leaves protected documents unchanged under the owner's review-only restriction.

## 7. B01–B20 and finding disposition

PASS is for bounded executed behavior, including expressly reused unchanged independent evidence. Historical Foundation digest fixtures do not certify later parser/OCR/query/ranking/count/answer/recommendation products.

| Case | Actual maintained evidence/boundary | Disposition |
| --- | --- | --- |
| B01 | `nsfwAndRemovedFieldsBeforeExecutionNeverReachActualPythonWorker`; `nsfwOwnerFirstAndResetRoundTripDenyAdmissionWithoutWorkerGrant`; reservation-first cases and SQL guards | PASS bounded categorical exclusion; no unauthorized transfer reproduced. |
| B02 | `crashAfterIssuedGrantCommitNeverDispatchesOrAutomaticallyResumesInput`; `controllerDeathWithActualBufferedHttpInputRetainsExclusionUntilVerifiedRecovery` | PASS both actual child-JVM/native/PG cases in the clean command; no disconnect/TTL-based release. |
| B03 | `associationReassignmentBetweenDifferentOldAndNewOwnersRequiresBothUnreserved`; `reservationSurvivesPhysicalCommitAndBlocksSelectedButNotUnrelatedEdits`; `writerFirstForEveryOwnerPathForcesFreshDenialAfterActualCommit` | PASS bounded real SQL/HTTP behavior including bulk/OLD-NEW guards; unchanged code and maintained cases. |
| B04 | `lostNsfwRoundTripCannotRestoreConsentOrAcceptOldOwnerIntent`; `privateConfigurationNeverChangesImplicitlyDuringApproval`; `delayedApprovalCannotUndoRevokeOrFieldReduction`; retained-history SQL | PASS no implicit renewed consent or stale source/reset/epoch/policy approval resurrection. |
| B05 | `descriptionConfirmationUsesExactBytesAndCannotRenewItemConsent`; `changedDescriptionConfirmationAndReservationCommitTogether`; `manualReindexOfNormalEditRetainsConsentWithoutAnotherApproval` | PASS bounded exact-description confirmation, independent participation consent and manual reindex semantics. |
| B06 | `twoActualConcurrentManualStartsCommitOneReservationAndOneActiveAttempt`; `twoExplicitJobsUseFreshActualProcessesAndPublishVerifiedSingleUseReceipts`; `staleListenerOrPortCollisionDeniesWorkerBindingWithZeroSourceDispatch` | PASS actual concurrent PG admission, native/client/HTTP paths; stale responding listener observes zero execution requests/body bytes. |
| B07 | `nativePythonDeathDoesNotReleaseSourcesWhileItsOwnPostgresCommitIsPending`; `actualWorkerStagingCommitAndPurgeShareAttemptFenceInBothOrders`; `queuedActualRoleWriterReloadsTheSealAfterPublicationPhysicalCommit` | PASS actual native death versus pending SQL physical C and legacy-route fencing, unchanged authority. |
| B08 | `liveConsumerUnprovenTerminationRetainsReservationAndPreventsSealUntilQuiescence`; both `concurrentFinish…ReconcilesSafeTerminalOutcome` methods in §3; all nine native methods | PASS original full-context regression and both controlled finish orders, plus actual Win32 lifecycle cases and preserved independent exact-token proof. |
| B09 | `sourceReservationRemainsVisibleWhileTerminalPhysicalCommitWaits`; `lowDiskOrStorageFailureDuringPublicationRollsBackAndRetainsReservationsForSafeRecovery`; both `committed…WithInjectedLostJavaAcknowledgement…` cases; independent AB-6 replay | PASS physical-C/rollback/unknown-ACK boundaries and reproduced former counterexample now succeeds. No physical drive-fill/power-loss claim. |
| B10 | `liveControllerPastLeaseExpiryPreventsSecondJvmTakeoverThenGenuineDeathAllowsRecovery`; `previouslyRegisteredControllerNeverReclaimsAnotherRuntimeOnReconciliation`; four second-JVM cases | PASS live-owner exclusion, genuine-controller-death recovery and atomic receipt/retry boundaries. |
| B11 | `actualSpringRuntimeRestartDeniesRestoredConsentAndStaleWorkerGrants`; `declaredRestoreOrUncertainSourceHistorySuspendsIndexUntilRenewedConsent` | PASS ordinary restart and supported cooperative cancel/revoke/inspect/renew/manual-publication sequence; not hostile-restore detection or power-loss certification. |
| B12 | `workerTimelyLastDeferredGateAllowsLateStageCommitThenExpiredVerificationRejectsPublication`; all three `RagE1ExpiryIntegrationTest` methods and retained lock-wait/default-deferred denials | PASS actual late S then expiry-specific rejection, timely late trusted P and persistent CURRENT; default expiry checks remain, E2 not demanded. |
| B13 | `newTransitiveChildInvalidatesOldGrantAndPublication`; `ordinaryDependencyRemovalAndReadditionPreserveConsentButRequireManualReindex`; `normalEditPreservesConsentButMakesIndexOutdatedUntilManualReindex` | PASS fresh full-dependency authorization and manual-only OUTDATED regeneration, unchanged source/SQL history. |
| B14 | `actualWorkerCannotReadBusinessAuthorityOrEscalate`; `workerSqlRejectsMalformedAndSameCountSubstitutedDependencyManifests`; `malformedStageDigestAndMissingWorkerProvenanceCannotPublish`; privilege suite | PASS bounded actual-role SQL and deliberately fake response negatives; mocks not treated as actual producer proof. |
| B15 | `cachedSourceAndOwnerContextCannotReuseRemovedDescriptionConsent`; `actualAuthorityLockTimeoutAndPublicationSavepointRollbackDenyActivation`; `twoSourceConcurrentInterleavingsAvoidDeadlockAndFailBusy`; prior real PG both-cycle/NOWAIT probes | PASS reproduced scope; new terminal precheck is a plain read and preserves mutation lock order. |
| B16 | `rejectsQuotedUnknownAndEncodedMetadataWhileAcceptingProse`; `externalCapabilityCannotBeApprovedOrDispatchedEvenFromRestoredCatalog`; restrictive external-metadata cases | PASS exclusions preserved; disabled live files not called filesystem-certified. |
| B17 | `cancellingNewAttemptPreservesAnIndependentlyCurrentPreviousGeneration`; `oldDependentGenerationCleanupCannotCancelNewReservedScopeThatExcludesTheRevokedItem`; `failedErasureRetainsPendingCleanupButSafeTerminalReleaseIsAllowed`; both P/cancel cases | PASS bounded lifecycle/manifest scope and truthful erasure; fresh cleanup/purge observations complement preserved tests. |
| B18 | `actualWorkflowBenchmarkRecordsOperationLatencyAndProcessResources`; existing cap/batch tests, 420-job raw receipt/counters/sampler; new review observers §5 | PASS bounded accepted thresholds and actual small-fixture stop/recovery/purge observations. No speculative benchmark requirement. |
| B19 | Actual enabled local native/PG pipeline, fail/cancel/recovery, Python socket checks, pinned local dependencies | PASS Practical Local Offline; no physical network cut/OS packet-isolation claim. |
| B20 | Fresh full Maven/Python/static/hooks, architecture/Flyway/Portability/effective-role evidence | PASS actual clean exit 0, 1308 unique Java/154 RAG subset; Python 37 and hooks 13; Authentication exception approved but unused. |

R1/R2 remain mapped to B01/02/03/07/15; R3 to B13/17; R4 to B04/11; R5 to B07/13/16/17; R6 to B11/12 under approved E1; disabled R7 to B16; R8 to B04/05/13/17. No new evidence justifies reopening FR17-1/2/3/4/6/7/11's bounded historical closure. The previous report's more detailed unchanged-method/boundary mapping remains applicable and is explicitly reused, with the actual fresh statuses above and E's complete method-node inventory. No test is credited solely from its name.

| Finding | Independent disposition, limited to the reviewed approved scope |
| --- | --- |
| FR17-AB-6 | **Resolved; closure supported.** Original actual interleaving now succeeds; both maintained finish orders and original B08 pass, source-before-attempt/positive stop/seal/C/release preserved. |
| FR17-AB-3 / FR17-9 | **Closure supported.** Useful 420-job/resource profile retained with corrected attribution; actual missing stop/recovery/purge observations now independently supplied. Residual factual documentation notes do not represent a runtime threshold failure. |
| FR17-AB-5 | **Low, nonblocking factual handback notes remain** as specified in §6; reviewed raw evidence and correct signatures are available. No implementation change is requested. |
| FR17-5 | **Closure supported for Alternative B's bounded internal-source capability.** Durable reservations precede body dispatch, prevent successful relevant exclusion mutations until safe stop, and lost/reset/version/dependency checks deny old authority. Live external sources remain disabled. |
| FR17-8 | **Closure supported.** Actual monolithic clean verification, native/SQL/lifecycle and Python/static/repository gates pass; no missing failure is waived. AUTH-CLOCK stays separately OPEN. |
| FR17-10 | **Closure supported for approved Practical Local Offline.** Actual enabled local pipeline/cancel/recovery and pinned/socket/config boundaries verified; superseded OS-enforcement requirements are removed requirements, not retrospectively passed tests. |
| AUTH-CLOCK | **OPEN, not fixed/closed.** Exact narrow owner-approved independent issue; enabled boundary test passes this execution, exception not exercised. |

These are review recommendations for the existing handoff's authorized status/finding synchronization. The reviewer does not edit the protected ledgers, remove containment or declare the phase COMPLETE/FROZEN.

| Implementation group | Implemented | Tested / independently verified |
| --- | --- | --- |
| A — source state/consent/reservations | Yes, unchanged source authority and additive SQL | PASS bounded mutation/CAS/closure/409/ordering behavior; prior real-role interleavings reused and maintained suites fresh. |
| B — confined single-use native execution | Yes, including prior bounded Windows wait/atomic receipts | PASS actual process/tree/endpoint/task/recovery cases; no unavailable-worker or TTL death assumption. |
| C — stage/finish/E1/IndexCurrent/recovery | Yes, current AB-6 reconciliation reviewed | PASS real SQL/native/physical-C/provenance/restart/cancel/purge and explicit E1 cases. |
| D — mandatory acceptance/evidence | Complete for current Foundation scope | PASS fresh clean/full/static/repository gates plus valid reused benchmark and new measured operational observations; limitations disclosed. |

## 8. Retained security, SQL, E1 and native boundaries

**Source protection and authority.** The reviewed source-before-owner correction and all mandatory V14 trigger backstops are unchanged. The earlier independently executed real-PostgreSQL two-cycle/NOWAIT counterexamples remain valid evidence against those identical files; this review does not substitute ordinary tests for those interleavings. Fresh `RagDomainCommitGuardIntegrationTest` and source reservation/authority/finalizer-owner suites also pass. Admission reserves the complete authoritative dependency closure before known admission C and dispatch. Ordinary eligible edits advance `source_version`/OUTDATE without renewing or removing participation; eligibility resets advance retained consent history. Lost Allowed→NSFW→Allowed notifications, stale approval source/reset/epoch/policy CAS and Private exclusion are independently covered by the maintained actual-SQL cases. Relevant owner mutation C cannot occur during the reservation; a denied 409 is not a committed NSFW/Private change. Unrelated items remain editable. Cleanup never generates an indexing job.

**SQL writer and physical finish.** The actual worker runtime remains a restricted nonowner role; `SECURITY DEFINER`/search-path/schema/legacy routes and actual credential denial are maintained and freshly exercised. Worker single-use execution/stage identity is not an owner policy or publication privilege. Native death is deliberately insufficient when a staging backend's C is pending. The attempt fence waits for prior writers, seals staging, and the final Java transaction commits outcome, pointer decision and exact-token release atomically. Both cancellation/P orders, storage failure, rollback, lost acknowledgements and failed erasure are tested. Unknown outcomes retain safety; PURGE_COMPLETE is asserted only after actual verified erasure. AB-6 recognizes an already committed safe result rather than introducing a release-only shortcut.

**E1 is the accepted contract, not E2.** ADR-0020 explicitly approves all three exceptions: **E1(a)** timely read/send/work checks may be followed by delayed/buffered consumption after expiry; **E1(b)** raw-free but sensitive untrusted S may physically commit late; **E1(c)** a timely last guarded validation can precede late **trusted VERIFIED/P physical COMMIT**. Newly observed expired gates, post-wait checks and default-deferred denials remain enforced. The actual late-S/expired-verification and late-trusted-P PostgreSQL cases pass. No extra check/timeout is claimed to close an unbounded last-check-to-C gap. Source reservations, current consent/eligibility/dependency/provenance checks and actual cancellation ordering still apply; E1 does not authorize NSFW/Private or old consent.

Completed CURRENT index life is separately authorized to survive its worker lease, execution session and ordinary service boot. Current source/reset/version/fields and complete dependency predicates decide access; historical COMPLETED alone does not. Normal edits/topology remove-readd make the old generation OUTDATED; renewal cannot resurrect it, and only explicit manual reindex creates a replacement. Failed reindex preserves a predecessor only if independently CURRENT. Session-scoped/ad-hoc retention is not converted into persistent permission. No future retrieval/ranking/count/answer implementation is certified beyond this Foundation's access predicates and negative contracts.

**Native Windows ownership/recovery.** Actual Win32 Job Object, suspended startup, PID+creation FILETIME, process-limit/breakaway tests and real second-JVM fixtures pass. The unchanged bounded wait fix was independently measured in the prior review and is reused, rather than reinterpreted as OS refusal-to-kill coverage. Sender/launcher/transport tasks and positive actual process/tree death remain prerequisites; timeout, disconnect, HTTP ACK or kill request is insufficient. Atomic local receipt publication and retry/read behavior are unchanged and all four actual child-JVM recovery methods pass in this clean run. A live controller past TTL cannot be taken over; genuine death permits the bounded owned recovery path. Unknown identity/outcome preserves reservation and single-job exclusivity. No unfinished work automatically resumes or publishes at startup. Cooperative declared-restore suspension/revoke/renew is verified; hostile external backup manipulation and power loss are not claimed.

**Migrations, ownership and Portability.** Actual sequence remains V1–V17, with additive `V14__rag_source_state_and_reservations.sql`, `V15__rag_single_job_and_sealed_worker.sql`, `V16__rag_current_read_and_terminal_scrub.sql`, `V17__rag_retained_consent_history.sql`. The historical V1–V13 files and all four additions are unchanged since the prior review. Fresh PostgreSQL 18.6/pgvector 0.8.6 compatibility, fresh-start Flyway/schema-manifest, effective-role and Portability tests pass. Operational source state/reservations are excluded from owner snapshot payloads without hiding unrelated business data; source history/tombstones remain durable operational authority. The previously inspected adapter/schema allowlists are not reaccepted merely because of test names: their source hashes and approved ownership are unchanged, their actual fresh suites execute, and no new bypass is introduced. Spring Modulith dependency/public-interface checks pass; Vault still has no RAG-internal dependency.

**Practical Local Offline.** Owner decisions/ADR-0020's current accepted context retain local Java→Python→restricted PostgreSQL, no remote/cloud AI endpoints or runtime downloads, pinned local environment, categorical exclusions and actual recovery/cancellation. Fresh enabled local pipeline and Python containment/config tests plus unchanged-source inspection meet that scope. The 32 installed dependency versions match the pinned inventory. Existing Python socket controls are not represented as native libpq/DLL-wide interception. No network cable disconnection, elevated firewall/WFP/global DNS drop or OS-level egress-isolation guarantee was measured or required in this review. Removed requirements were explicitly replaced, never retroactively marked tested.

## 9. Exact independent commands, results and preservation

Pinned executable: `rag-engine/.venv/Scripts/python.exe`. Commands below used E/`review_tools.py` with argv/cwd/hard watchdog and owned-tree cleanup. Monolithic Maven was executed **once**. Original receipts and tests from older reviews are reused only where expressly identified; their failures remain failures. Isolated observers are executable review evidence, not changes to maintained tests and not additional unique Surefire cases.

| Command / label | Actual result |
| --- | --- |
| Backend `mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify` | Exit **0**, **812.432s**, hard 1200s, no timeout; **1308 unique PASS / 0 failures/errors/skips**; package/repackage completes. |
| rag-engine `python -m pytest -q --junitxml=../logs/phase17-alternative-b-independent-re-review-2026-10-17-3/pytest.xml` | Exit **0**, **37 PASS**, 0.39s pytest / 2.246s wrapper; one retained Starlette/httpx deprecation. |
| rag-engine `python -m ruff check .` | Exit **0**, 0.243s. |
| rag-engine `python -m ruff format --check .` | Exit **0**, **19 files already formatted**, 0.347s. |
| rag-engine `python -m mypy src/ppv_rag` | Exit **0**, **8 source files**, 1.085s. |
| Root `python .agents/hooks/test_repository_safety.py` | Exit **0**, **13 PASS**, 0.278s. |
| rag-engine `python scripts/dependency_inventory.py` | Exit **0**, **32 versions match lock**, 1.537s; no new exhaustive vulnerability/license scan claim. |
| Root `python E/run_finish_proof.py` | Exit **0**, **33.127s**, 240s hard wrapper; actual original AB-6 interleaving now succeeds safely. Compile/Java argv and logs retained. |
| Root `python E/run_cleanup_timing_v1.py` (original source/driver preserved) | Exit **0**, **46.594s**, 240s hard wrapper; three cancellations + three maintenance recoveries, committed producer S and verified cleanup. Original invocation used `run_cleanup_timing.py` before adding separate stop instrumentation; original artifacts remain. |
| Root `python E/run_cleanup_timing.py` (v2) | Exit **0**, **40.541s**, 240s hard wrapper; explicit six actual normal-stop measurements + three cancellations + three maintenance recoveries; no failed maintained test rerun. |
| Root `python E/run_historical_purge.py` | Exit **0**, **32.185s**, 240s hard wrapper; three actual publication→revocation→denial→historical purge observations. |
| Targeted Graphify query | Exit **0**, **2.965s**, 30s watchdog; navigation only. |
| Benchmark | **Not rerun**. Exact prior independent 420-job receipt/counters/sampler verified and reused. Operational jobs are not new JUnit testcase nodes. |
| E/`account_tests.py` | Exit **0**; actual fresh XML node/status/subset accounting in `independent-test-accounting.json`. |
| E/`validate_docs.py`, `git diff --check` | Exit **0**; 33 current Markdown documents / **490 local targets / 22 fragments**, no missing local file/anchor; new report **2 local targets / 1 source-line fragment**, valid. **29** dimensions / **20** main B rows / **245** exact inherited inventory rows; zero missing/extra path. Final result/hash retained in E/`documentation-validation.json`. `git diff --check` exit **0**. |

Fresh framework executions total **1,358 = 1,308 Java + 37 Python + 13 repository-safety**, all pass. RAG 154 is included in Java; three architecture suites total 43 and are also included. Submitted reports, prior focused batches and new operation observers are not added to make a larger unique-test claim. Compiler/runtime warnings remain visible: Lombok deprecated Unsafe access, a frozen media test's deprecated Jackson converter, Mockito/ByteBuddy dynamic agent/CDS warnings, and Python's Starlette/httpx deprecation. No warning-free or exhaustive IDE/static-analyzer claim is made.

Two read-only path-navigation commands failed with exit **1** because the reviewer supplied incorrect guessed paths for tests/command records. `rg --files` located the actual files, which were then inspected. One inline review-helper maintenance expression also exited **1** with a quoting SyntaxError before execution; the surrounding command continued to the successful appendix/validator steps and returned 0, which does not make that Python child successful. E/`navigation-failures.json` preserves all three reviewer navigation/setup failures; none launched a test or changed the candidate. No application/test/observer command failed or timed out in this cycle, and no failing application command was retried to obtain green. Earlier independent failed receipts are preserved unchanged.

Final process snapshots retain only preexisting unrelated Java PIDs **9832/16740** and their command wrappers, with matching intake creation times and no review-harness/Maven/Surefire/consumer command. Their parent chains are IDE/language-server applications. The preexisting PostgreSQL container **01f0983078d0** remains healthy. No review-owned fixture container, Maven/Surefire/observer/consumer process remains; unrelated processes were not killed. No command reached its hard timeout or needed forced cleanup.

Final hash validation preserves **all 1,986 preexisting nonignored files byte-for-byte**, including historical reviews and every production/test/migration/contract/ADR/ACTIVE file. There are **1,987** current nonignored files; this new report is the **only added nonignored path**, no unexpected file or deletion, no staged patch. Actual delivery is **246** paths versus unchanged HEAD; the contract-snapshot delta becomes **115 = 74 modified + 41 added** solely because of this report. E/`documentation-validation.json` and inventory hashes retain the exact check. No reviewer implementation or status synchronization was performed.

## 10. Complete 29-dimension review ledger

| # | Dimension | Disposition and concrete evidence/limits |
| --- | --- | --- |
| 1 | Owner intent/business scope | PASS — approved Alternative B/E1/manual single-user Windows Foundation retained; no later product or new architecture. §§1,8. |
| 2 | Acceptance traceability | PASS bounded B01–B20/R1–R8 and finding matrix §7; new AB-6/B08 outcomes and previously missing resource observations independently recorded. |
| 3 | Architecture/ADR consistency | PASS current ADR-0020 amendments, source history and additive sequence; no F/V/DRAIN or strict E2 revival. §§1,8. |
| 4 | Modulith/public ownership | PASS RAG-only subsequent production changes; Vault narrow authority remains independent; fresh architecture 37+3+3. |
| 5 | Recursive package/folder structure | PASS complete changed-file inventory/unchanged subtree hashes, no new implementation/test package or generic framework; appendix and E tree/inventory. |
| 6 | Shared contracts/exceptions | PASS unchanged ADR-0016 root HTTP envelope and owner-local API/exception placement; fresh HTTP/OpenAPI suites. |
| 7 | SOLID/GRASP | PASS bounded terminal-reconciliation responsibility; Vault owns source authority, supervisor actual execution, purge atomic finish/erasure. §3. |
| 8 | DRY/KISS/YAGNI | PASS existing transaction/graph/attempt mechanism reused; no second job engine/distributed coordination/new restore abstraction. |
| 9 | Pattern suitability | PASS existing native/client adapters and explicit durable lifecycle solve current supported races; no speculative pattern layer. |
| 10 | Business state/idempotency | PASS original false-denial interleaving prevented; both finish orders/current/outdated/consent/retention cases pass. §§3,7–8. |
| 11 | API/wire compatibility | PASS production commands/409/CAS unchanged, actual HTTP suite passes; Low schematic runbook examples require factual sync (§6), not API changes. |
| 12 | Input/format/resource validation | PASS unchanged node/depth/byte/type/hash/manifest/identity bounds and malformed/quoted restrictive negatives; fresh RAG/Python suites. |
| 13 | Authn/authz/privacy/consent | PASS bounded RAG exclusions/least privilege/consent-reset/replay checks; AUTH-CLOCK remains separately OPEN under explicit exact-case owner decision (§4), not a fixed Authentication guarantee. |
| 14 | Secrets/retention/offline | PASS bounded raw-free receipts/typed scope/verified purge and Practical Offline; no exhaustive OS/DLL interception or physical disconnect claim. §§5,8. |
| 15 | Atomicity/locks/physical C | PASS current graph/plain-terminal-read/source/attempt order, prior actual SQL deadlock probes reused, real pending writer/final-C cases fresh. §§3,8. |
| 16 | Failure/recovery/operations | PASS actual native/controller/SQL/unknown-ACK/cancel/restart/cooperative restore boundaries; uncertain state remains reserved. General power-loss/hostile restore unverified and outside bounded acceptance. |
| 17 | Database/Flyway/permissions | PASS V1–V17 unchanged, fresh PostgreSQL/pgvector/Flyway/schema/effective-role suites; no owner privilege bypass; Portability 15. §8. |
| 18 | Resource/time/memory/I/O | PASS approved bounded profile with real 420-job data, round-trip guard bounds and new stop/recovery/purge observations; finite caps/headroom/storage-failure proof. §5. |
| 19 | Retrieval/recommendation quality | N/A — no later-phase search/ranking/answer/recommendation product introduced; Foundation authorization is reviewed, no quality equivalence claim. |
| 20 | Parser/OCR/multilingual fidelity | N/A — later ingestion/parsing products and disabled live external files; no file/COMMIT certification inferred. |
| 21 | Adversarial security verification | PASS bounded actual role/process/SQL barrier tests, lost notifications/dependency history/expired gates and original AB-6 observer; not universal platform certification. §§3,7–8. |
| 22 | Test quality/determinism | PASS maintained both-order barriers and original B08 security assertions; no skip/weaken/retry-until-green; all actual fresh nodes pass. Known mixed-clock issue remains OPEN, not claimed deterministic. |
| 23 | Benchmark methodology | PASS 400 measured +20 warmups, real useful native jobs/four fixtures, observed counters and disclosed sampler/operation limits; corrected authoritative values §5; Low remaining labels §6. |
| 24 | Held-out/model evaluation | N/A — later parser/model/query quality gate, not Foundation synthetic-stage/provenance correctness. |
| 25 | Dependencies/licenses/lifecycle | PASS pinned unchanged environment inventory (32), prior bounded audit reused; no new dependency/lock change, advisory refresh or exhaustive native-license/DLL scan claimed. |
| 26 | Build/static/tooling | PASS actual clean lifecycle 1308, Python 37/Ruff/mypy/hooks13; warning categories disclosed, no fake full-suite aggregate. §§2,9. |
| 27 | Logging/observability | PASS bounded safe job-correlated diagnostics/raw-free native receipts and actual failures retained; unused presence helper is not accepted as stop proof. |
| 28 | Documentation/governance | FINDING — Low nonblocking fixture/unit/constructor notes in §6 and normal review-ready handback required; exact current authority/exception/scope consistent. Reviewer preserves protected documents. |
| 29 | Hygiene/frozen/delivery | PASS exact inventory, Git/hash/preservation checks below and appendix; report/evidence only, no staging/commit/push/frozen code/history edit. |

## 11. Exact next owner action and handoff closeout

The candidate is **READY FOR OWNER COMMIT** for the approved Foundation scope. No additional RAG implementation cycle, architecture decision or acceptance waiver is required. Recommended finding closure is limited to the evidence and unchanged containment described above. AUTH-CLOCK remains OPEN under its existing narrow recorded owner decision; this acceptance makes no Authentication closure determination.

1. Have an authorized documentation-only handback synchronize the **existing** ACTIVE to `READY_FOR_OWNER_COMMIT`, link this report and its independent actual command results, and record the bounded FR17-5/8/9/10 plus AB-6/AB-3 dispositions. Preserve all historical reviews, failed receipts and AUTH-CLOCK. Update the current phase README/implementation/test-evidence/roadmap/agent status where needed so “awaiting independent review/CHANGES_REQUESTED” is not presented as the newest result. Include the finite factual corrections in §6 and this new report in the explicitly dated inventory. The reviewer has not edited those protected files, per the owner's restriction.
2. Preserve the ignored local E logs/XML/probe source plus credited prior 420-job/native/resource and failed receipts in the owner review package. Documentation-only corrections require link/inventory validation, not another expensive Maven run in the absence of code/test changes. Any implementation change invalidates the affected evidence and requires proportionate verification.
3. The **owner** reviews the final package and commits/pushes the reviewed Phase 17 implementation and synchronized handback. Proposed single commit message:

   ```text
   feat(rag): add single-job local indexing foundation
   ```

4. After owner commit/push, give ChatGPT the committed package for formal Phase 17 closeout/archive of the **same** handoff. Do not mark COMPLETE/FROZEN or reset ACTIVE to NO_ACTIVE_HANDOFF before that owner commit and formal closeout. This review does not perform closeout, commit/push or create another handoff.
5. Keep default containment and live-external-file disablement. Containment removal, frontend, production deployment and Phases 18–22 require their own explicit authorization; this acceptance authorizes none of them.

## Appendix — exact review-start changed-file inventory

All **245** inherited changed/untracked paths versus owner HEAD, sorted below. Full SHA-256 values are in E/`delivery-inventory.json` (file SHA-256 `ab3c4b3b041116e42053a33f5ee8e4f5fd6a86010c3f8ac83dd3995cbcf5175d`). `M`/`A` means modified/added versus the accepted contract-review snapshot; `I` means inherited and unchanged versus that snapshot. `R` marks a subsequent remediation change versus the immediately previous independent review. These are attribution labels, not Git staging or test statuses. The new report itself is the sole additional nonignored review delivery path.

| Path | Contract-baseline attribution | Subsequent remediation |
| --- | --- | --- |
| `.codex/config.toml` | I |  |
| `.gitignore` | I |  |
| `AGENTS.md` | M |  |
| `backend/AGENTS.md` | M |  |
| `backend/compose.rag.dev.yml` | I |  |
| `backend/dev/rag-db/ProvisionRagDevelopment.java` | I |  |
| `backend/dev/rag-db/README.md` | M |  |
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
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagPurgeService.java` | M | R |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRecoveryService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeIdentity.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeSecurity.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceAuthority.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerGrantService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerSupervisor.java` | A | R |
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
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagCommitAcknowledgementRecoveryIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagConsumptionContainmentTest.java` | I |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagDomainCommitGuardIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagE1ExpiryIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagEngineClientTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFinalizerOwnerConcurrencyIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFoundationBenchmark.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLegacyRuntimeDeniedIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLifecycleIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagNativeWorkerProcessTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPgVectorCompatibilityIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPythonCommitTerminationIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRecoveryProbeProcess.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRuntimeRoleIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSecondJvmRecoveryIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSingleJobRuntimeIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceAuthorityIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceReservationsIntegrationTest.java` | A |  |
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
| `backend/src/test/resources/rag/source_state_reservations.sql` | A |  |
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
| `docs/implementation/phase-17/alternative-b-verification-plan.md` | M | R |
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
| `docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-2.md` | A |  |
| `docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review.md` | A |  |
| `docs/implementation/phase-17/reviews/README.md` | I |  |
| `docs/implementation/phase-17/revocation-and-derived-data.md` | M | R |
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
