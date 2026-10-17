# Phase 17 Alternative B — independent final implementation review

**Verdict: CHANGES_REQUESTED. The candidate is not ready for owner commit.**

Alternative B remains the accepted architecture. Much of its implementation is real and independently exercised, including Windows job-scoped workers, restricted PostgreSQL staging, atomic publication/release, consent history, normal restart preservation and E1 late publication. Acceptance is blocked by a reproduced source-guard ordering regression, failed/incomplete verification, incomplete resource acceptance and missing supported OS-enforced offline evidence. No F/H/V/E redesign is requested.

## 1. Authority, baseline and review limits

Date: 2026-10-16. Branch `main`; owner HEAD `24e6268eef1685866d919d882ecdf9f532d01961`. Reviewed the actual staged/unstaged/untracked worktree, not just the Antigravity summary. No staged patch was present. This report and isolated ignored evidence are the reviewer's only delivery changes. Production, tests, migrations, ADRs, accepted contracts, owner decisions and ACTIVE were not edited.

Applied root/backend/docs/test and relevant owner-module instructions, codex-final-review, the 29-dimension coverage rubric, RAG/Modulith, Java/persistence/testing, migration and pragmatic design guidance. Read current handoff/owner authorization, implementation report/evidence/remediation/inventory and accepted P17-1/2/3/ADR-0020. Compared the previously reviewed simplification, synchronization, ADR-0019, security/API/module contracts and historical findings. Unchanged previously inspected contract/review bodies were reused after SHA-256 comparison; implementation claims were not substituted for source inspection or execution.

The original generated Surefire directory was copied before running Maven. Fresh review evidence is under ignored `logs/phase17-alternative-b-independent-final-review-2026-10-16/`. Important receipts are `baseline-sha256.json`, `delivery-inventory.json`, `implementation-delta.json`, `submitted-surefire/`, `submitted-command-accounting.json`, `final-execution-accounting.json`, per-command JSON/logs and both SQL probe results. Test-generated receipts also occur in the maintained tests' existing ignored output locations; those generated files are not production delivery or immutable review reports.

Sandboxed Git initially refused repository ownership. Git was subsequently read under the repository owner's execution context; no global safe-directory configuration was changed. Every application/probe command used an outer hard 600-second timeout; probe subprocesses also had finite limits. Timed-out tracked trees were stopped with their specific root PID, and their logs/reports retained. Unrelated processes/containers were not terminated.

## 2. Worktree and complete inventory assessment

The review-start snapshot contains **1,981 nonignored files**. Against the accepted contract-review delivery state of 1,946 files, the implementation changes **68 existing files and adds 35**, with no deletion. Against owner HEAD the complete inherited delivery contains **240 modified/untracked paths**. These are different baselines; 240 is not the implementation-session attribution count.

| Implementation delta category | Modified | Added | Inspection / consequence |
| --- | ---: | ---: | --- |
| Backend production Java | 34 | 13 | Owner guards/projections, RAG authority/consent/jobs/native supervision/finish/recovery/API, shared error translation and Portability exclusion. |
| Backend production resources | 0 | 4 | V14–V17; actual sequence is contiguous V1–V17. |
| Backend tests/resources | 18 | 15 | Maintained regressions, actual native/SQL/recovery/expiry tests, benchmark and SQL fixtures. |
| Python worker/tests/scripts | 7 | 3 | Bound ingress/claim, execution configuration, restricted SQL, fixtures and inventory/boundary scripts. |
| Documentation | 7 | 0 | Handoff, phase status, owner authorization, plan and implementation/evidence claims. |
| Root/backend instructions | 2 | 0 | Explicit implementation authorization; reviewer remains review-only. |

The machine-readable full 240-path inventory includes exact SHA-256 values. `implementation-delta.json` identifies every one of the 103 implementation paths; its exact path list is reproduced in the appendix. Previously dirty `.codex/config.toml`, `.gitignore`, diagrams and earlier candidate files are inherited inputs, not newly attributed implementation edits.

**Inventory defect:** [changed-files.md](../changed-files.md) was not synchronized for this implementation. **42 of the 103 implementation paths are absent from its links**, including the new reservation APIs, native supervisor/process adapter, V14–V17, new actual-boundary tests/resources and Portability/schema changes. Its old per-pass counts cannot serve as the current implementation inventory.

All V1–V13 migration hashes, frozen authentication implementation/tests and prior Phase 17 review files match the pre-implementation snapshot. Schema v1 DBML and accepted ADR/contract bodies remain preserved. V14–V17 are additive candidate migrations, not already frozen historical migrations. Fresh Flyway/schema tests and independent disposable PostgreSQL execution exercised all 17 migrations; the latter used an explicit minimal Flyway-history placeholder for SQL execution, not a claim to have run Flyway itself.

Review-delivery validation passed: all **1,981 pre-existing hashes unchanged**, only this new report added, 103 implementation inventory rows, B01–B20 exactly once, 29 ledger rows, ten local Markdown targets/four fragments resolved and zero whitespace errors. `final-validation.json` records the exact report hash. Final process/container inspection found no owned Maven/test/probe/worker process remaining; the pre-existing Python processes and three unrelated PostgreSQL containers were left untouched.

### Handoff governance

`IMPLEMENTED_AWAITING_CODEX_REVIEW` is normally the workflow's pre-acceptance intake state; it is not inherently a self-issued final approval. However, ACTIVE lines 5–13 claim complete implementation and B01–B20 verification despite outstanding mandatory gates. The specific owner authorization also says to keep CHANGES_REQUESTED pending proof. [Remediation-plan](../remediation-plan.md) still records verification in progress. The evidence does not justify an all-complete declaration. This review's verdict is CHANGES_REQUESTED; ACTIVE is left as found because the owner expressly permits only the new report and isolated evidence in this review. The implementation agent must synchronize it during remediation.

## 3. Findings, ordered by severity

### FR17-AB-1 — High: owner-before-source acquisition regresses the approved order and reproduces a deadlock

Evidence: [VaultEntryMutationGuard.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryMutationGuard.java#L42), lines 42–44, locks `vault_entries` before `SourceEditGuard.requireEditable`. [VaultEntryService.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java#L116), lines 116–129, repeats that pair per ID. [RagSourceAuthority.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceAuthority.java#L155), line 155, uses this operation during admission. It therefore acquires Vault A -> source A -> Vault B -> source B, rather than all sorted stable source guards before owner rows.

**Maintained regression fails independently twice:** `RagDomainCommitGuardIntegrationTest.serializesConcurrentWritersOnVaultEntry`, lines 32–75, expects the second writer to fail with P1701 before waiting on the later Vault row. It instead times out at `t2Future.get(5, ...)`. Both the focused run and clean run record this failure. Its comment explicitly describes why waiting on the Vault row is forbidden.

**Deterministic real PostgreSQL counterexample**, with the actual V1–V17 functions and actual `rag_runtime` connections:

1. Admission-order transaction A locks Vault A, then source A, as the production helper does.
2. Owner transaction B updates Vault B; its trigger obtains source B because A has not reached it.
3. A requests Vault B and blocks. The observer confirms `pg_blocking_pids`.
4. B next updates Vault A and blocks before its trigger can request source A.
5. PostgreSQL detects **40P01 deadlock** and aborts A. B can proceed, then the probe rolls it back.

The same cycle reproduced first with administrative connections and then with actual runtime credentials. The RAG graph barrier would not remove this cycle because the owning Vault mutations do not take it. The probe uses the exact relevant SQL ordering, not an instrumented Java production hook. No unauthorized Python consumption was established by this counterexample; it proves the lock-order/availability regression and invalidates the claimed complete concurrency acceptance.

V14's earlier-source NOWAIT is useful but cannot prevent a wait that happens **before the trigger is reached**. The demonstrated cycle concerns admission; no equivalent cycle is asserted here for finish/abort/release, which use source -> attempt without this Vault walk.

Required remediation: restore approved stable-source-before-owner ordering across admission and maintained owner paths, acquiring complete affected sets in the correct order. Preserve 404 semantics without taking the conflicting owner lock first. Retain bounded forward waits/conflicts, exact reservations, fresh state and complete rollback. Reassess removed Fiction/Film association guards together with their SQL backstops. Do not blindly revert safe AFTER triggers or weaken frozen domain business assertions to obtain green results. Regress the maintained failing test and the two-source cycle at actual Java/PG boundaries.

### FR17-AB-2 — High: mandatory verification is failed/incomplete; all-B-complete claims are unsupported

Evidence: [test-evidence.md](../test-evidence.md#2026-10-16-alternative-b--verification-receipts-and-evidence-ledger), lines 3–28; [implementation-report.md](../implementation-report.md), lines 3–7; ACTIVE lines 5–13; independent command receipts.

- Focused RAG/platform command: hard timeout, **177 fresh completed testcase nodes: 176 PASS, one FAIL**.
- Full clean Maven command, with required Windows native access and no test exclusions: hard timeout, **1,059 fresh nodes: 1,057 PASS, two FAIL**. It did not complete `verify`.
- A separate bounded group completed the remaining suites: **183 PASS**, exit 0. The already failing tests were not selected for a lucky retry.
- Across these commands: **1,419 Java test executions, 1,298 distinct test identities; 1,296 distinct PASS and two distinct FAIL**, zero recorded errors/skips. All **144 distinct RAG tests** were executed: 143 passed and the guard regression failed.

The second failure is unchanged `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary`. No frozen auth source/assertion changed. A fresh independent 20-sample actual Java/PG clock probe showed the database behind the complete Java measurement interval in all samples, with DB-minus-Java-after approximately **-313.4 to -300.2 ms**. This establishes noncoherent clocks at probe time; it does not establish the clock direction at the earlier failing rotation or prove the exact failure cause. The historical two-clock diagnosis remains plausible and environment-dependent, not a new Phase 17 authentication-code regression. It is **not fixed or waived** by this review.

Mandatory native ingress/failure, low-disk/restore and expiry subcases also have limits recorded in §7. Passing named ordinary tests is not complete B01–B20 proof. Restore precise implementation/tested/verified/NOT_VERIFIED distinctions; fix the guard failure, diagnose/stabilize the clock environment without auth edits, and complete a genuinely successful full verification gate with finite process cleanup. Complete mandatory missing regressions; retain every failed/timed-out receipt. A union of bounded runs is useful coverage evidence, not a successful full clean Maven lifecycle.

### FR17-AB-3 — Medium / FR17-9 remains open: useful benchmarks exist, resource acceptance is unfinished

Evidence: `RagFoundationBenchmark.java:100–108` describes resource/evidence limitations; `windows-resource-samples-20261016T054907580Z.json:14–26` records partial coverage. The prior ignored `benchmark-results.json` and `single-job-benchmarks.json` are real supplied evidence: **420 attempts, 420 successes**, four workloads, five warmups plus 100 measured cold jobs each, actual native Java/Python/PG publication, source/depth/input boundary checks. The command exited 0 after **822.025 seconds** under its original 1,800-second bound. This review did not rerun that command under the owner's 600-second review limit.

The resource sampler explicitly covers **245/420 worker identities**, leaving 175 unsampled. Sampled working sets/handles are not full peaks. The benchmark limitations identify missing worker/resource attribution and Docker disk I/O; the newer native-counter implementation and layout note describe a resource-complete rerun that is not supplied as completed evidence. No accepted measured startup/job/stop/CPU/RAM thresholds, complete lifetime-storage/low-disk/leak disposition or final source-matched resource acceptance is demonstrated. The retained 16-job workflow is a useful additional baseline, not the four-workload gate.

Required remediation: run the current implementation's resource-complete successful profile, record failures and sampling coverage, measure contention/cancellation/recovery and leakage/headroom, and record threshold dispositions before acceptance. Workload selection can bound commands while preserving all four workloads and required success/sample counts. Do not reuse obsolete F/16-job saturation targets, or force uncertain reservation release to meet an SLA.

### FR17-AB-4 — Medium / FR17-10 remains open: OS-enforced enabled offline proof is not supplied

Evidence: current `test-evidence.md:3–28` claims complete verification, while ignored `windows-offline-reviewed-procedure.md:1–6` explicitly describes a **not-executed** control candidate requiring elevation, active rules, WFP corroboration and independently checked PostgreSQL isolation. The existing `resumed-python-app-offline` receipt is an application-level boundary check, not an armed-control/workflow/restoration receipt. No supplied actual native Java -> Python -> PG -> publication/cancel/recovery run is correlated with effective IPv4/IPv6/DNS/proxy enforcement and attempted-connection evidence for the supported stack.

Required remediation: obtain authorized supported OS controls, prove permitted local communication and blocked external attempts, run actual successful and negative/cancel/recovery flows inside the enforced window, and verify restoration. Account for the database server, not just Java/Python host executables. Socket monkeypatches, reserved-target timeouts, Docker health or contained execution are insufficient. No firewall or audit policy was changed by this reviewer. The architecture/offline requirement is unchanged.

### FR17-AB-5 — Medium: delivery inventory and current documentation are inaccurate

Evidence: [changed-files.md](../changed-files.md); [implementation-report.md](../implementation-report.md), lines 12/19/20; current ACTIVE/plan; inventory receipts.

The current implementation inventory omits 42 affected paths. The implementation report names `edit_reservation_token`, statuses WAITING/RUNNING/CANCELLING and `RagNativeWorkerProcess`; actual artifacts use `reserved_by`, the V15 ADMITTED/STARTING/ACTIVE/STOPPING/RECOVERY_REQUIRED/FINALIZING predicate and `NativeWorkerProcess`. It claims 144 RAG tests across 18 classes; the three supplied RAG batch sets contain **23 classes**. Current status/evidence overstates all-B completion and lacks a complete clean gate and mandatory offline/resource disposition.

Required remediation: synchronize the exact current inventory, real identifiers, commands, unique-versus-execution counts, failures, source-matched receipts and open acceptance gaps. Preserve all historical accounts as attributed history. Restore CHANGES_REQUESTED/current remaining findings in ACTIVE under the existing handoff. No new handoff or contract redesign is needed.

## 4. Group A–D implementation versus independent verification

| Group, using the submission's grouping | Implemented | Tested / independently verified | Disposition |
| --- | --- | --- | --- |
| A — reservations/consent/source protection | Retained Vault rows, source/reset history, exact reservation API, owner/policy/dependency triggers, consent/description CAS, atomic admission. | Real SQL reservation conflicts/reset history, Java consent/reservation suites and actual whole-closure/single-job tests pass. Guard-order regression and incomplete exhaustive mutation-path/fault inventory remain. | PARTIAL; FR17-AB-1 blocks acceptance. |
| B — native job-scoped worker | Suspended Job Object child, binding before resume/input, actual readiness/nonce/PID, one-use SQL claim, controlled transport and positive stop. | Eight real native tests, actual restricted-role workflows and four actual second-JVM crash/recovery tests pass in completed focused results. Python ingress units deliberately mock SQL claims in designated fixtures. | Substantially verified; outstanding B06/B08/B19 proof is not inherited from mocks. |
| C — staging/finish/recovery/freshness | Attempt lock/seal, exact producer/receipt validation, one physical P-or-abort/terminal/release, manifest-specific purge, independent IndexCurrent, abort-only recovery. | Actual SQL writer/termination ordering, both P/cancel orders, lost acknowledgement, cleanup failure, dependency round trip and restart tests pass. No power-loss/low-disk/unknown-history guarantee inferred. | Bounded positives verified; mandatory limits remain. |
| D — E1 and acceptance | Fresh observed-expiry rejection plus owner-approved late physical completion; completed persistent lifetime separated from work credentials. | Real retained deferred gate before/after barriers prove rejection versus permitted late trusted P and CURRENT after expiry. Full acceptance/performance/offline is unfinished. | E1 direction preserved; not full A–D acceptance. |

## 5. Source, SQL, native and lifecycle assessment

### Owner mutations and trigger timing

V14 covers `vault_entries`, Fiction/Film entity rows, Fiction story/world associations, Film genre/story/world associations, links/credits, favorites/ratings/tags, RAG source policy and dependency mutations. OLD/NEW parent IDs and affected ancestors are included. Owner business data remains owner-local. Shared reference names excluded from the approved projection do not require new reservation machinery; projected assignment IDs do.

AFTER association triggers are **not intrinsically too late to roll back SQL**: a trigger exception rolls back the statement's row/index/FK effects and its source metadata changes. The independent reserved favorite INSERT returned P1701 and left zero rows; earlier source-lock contention returned P1701 in about 11 ms. A reserved NSFW write likewise left `is_nsfw=false`. This does not certify full command rollback if an application swallows an error or escapes a savepoint, nor prevent unique/FK waits before the AFTER trigger. Required business transaction/cascade/savepoint coverage remains tied to the corrected ordering.

The BEFORE DELETE fix is correct: the Vault lifecycle trigger now returns OLD for every DELETE. Independent runtime deletes of PERSON, BRAND, IMAGE and FILM_CREDIT all returned/deleted the actual row; eligible-source history remains retained. No silent deletion suppression was reproduced. V14's NOWAIT helper is real and qualified; it is the ordering of calls before that helper that fails.

`FOR SHARE` in V16's current-read function protects source metadata against updates; it is not the weaker FOR KEY SHARE. Worker routes lock the attempt FOR UPDATE and reload state after waits. Source reservations join the caller's writable physical connection; Java tests compare JDBC/JPA backend PID and reject missing/read-only transactions. Hashing/serialization and native/network waits are kept outside source SQL guard intervals. The global contract remains graph -> all sorted source-state -> Vault/owner -> policy -> attempt -> generation/staging/run; FR17-AB-1 is a concrete exception in the implementation, not an approved amendment.

### Roles and migrations

V14–V17 append retained source history, reservation operations, complete static nonterminal uniqueness, immutable attempt authority, single-use consumed marker, consolidated worker fences, current-read permission and retained consent history. V15 refuses an unresolved legacy upgrade rather than inventing quiescence. Initial old unbound approvals are denied; V17 prevents source-catalog deletion/recreation from resetting intent history. No worker business/Vault definer chain or new global F/V/DRAIN framework was introduced.

Actual `rag_worker` connections independently received **42501** for schema-qualified business/source-state reads, direct staging insertion, source-release function, policy mutation and CREATE TEMP. The initial unqualified probes produced undefined-relation errors because of search_path and were not credited as privilege proof; the qualified follow-up is preserved separately. Maintained effective-role, legacy-route, malformed-provenance and actual staging tests also passed. Fixed `pg_catalog` search_path and qualified definer references were inspected; runtime and worker remain nonowners without inherited migrator authority. This is bounded effective-role evidence, not an exhaustive native-library audit.

### Native worker and recovery

`NativeWorkerProcess` creates a named noninheritable Job Object, configures kill-on-close, process-count/memory bounds, creates a suspended hidden child, assigns/confirms membership and records creation identity. The supervisor commits STARTING/binding before resume or body dispatch. Readiness checks exact job/attempt/nonce/PID; an old generic health response is insufficient. Python burns the actual scoped input claim in a committed restricted-role transaction before ASGI body receive. Uvicorn/kernel buffering is distinct from body parsing; the committed source reservation, actual process identity and positive stop-before-release are the source-use boundary.

Success also joins dispatch, stops the actual process/tree and terminates transport before finish. ACK, kill request, disconnect, lease expiry and a new runtime UUID are not accepted as death proof. Recovery refuses live/unknown controllers and discards unfinished work after actual process/writer resolution. Real child/second-JVM/buffered-input tests support these claims. Complete stale-listener/port-collision/no-early-wire, failed-kill/late-launch and actual OS restart/failure proofs are not all established by the supplied named tests; see §7. No unsupported automatic takeover/resume is approved.

### Publication, cancellation and cleanup

The finalizer rejects ambient transactions, requires stopped ownership, locks source/policy/attempt, drains writers, verifies exact one committed typed producer receipt, rechecks expiry after waits, and commits publication/seal/terminal/release atomically. Cancellation's attempt-only stop request finishes before source/native work. Publication-winning cancellation reports completed; stop-winning publication denies. `RagGenerationState` separates historical COMPLETED from fresh CURRENT/OUTDATED/REVOKED/UNAVAILABLE and completed execution credentials.

Cleanup uses actual historical manifests, sealed terminal attempts and verification of erased rows/capabilities/digests. Failed erasure remains PENDING/inaccessible; a savepoint does not falsely mark PURGE_COMPLETE. Safe edit release can precede inaccessible physical erasure only after consumers/writers cannot continue. Actual old-generation/new-scope and deletion-failure regressions passed. Unknown outcomes retain state or reconcile a durable terminal marker; no completion callback is replayed. No power-loss or unavailable-storage erasure bound is claimed.

## 6. E1, persistent lifetime and retained security

**Strict E2 is not required and is not used to reject this candidate.**

- E1(a): timely gates may precede post-deadline consumption under suspension/buffering. The reservation remains; time passing cannot authorize an exclusion C or release.
- E1(b): a post-final-check late S can remain isolated/untrusted. Fresh expired verification must deny. Source/producer/attempt checks are unchanged.
- E1(c): a timely final trusted gate can precede late real VERIFIED/P physical C. The real deferred-trigger barrier test distinguishes before-gate rejection from after-gate permitted publication, without replacing the security function.
- A valid late CURRENT persistent index can outlive completed worker lease/session/boot. Normal restart preserves consent/current completed indexes; ordinary edits/reset/policy/scope mismatches deny before current exposure. No later retrieval/ranking product is enabled in this Foundation, so later consumer lifetimes are not certified here.

Source versions and captured reset/policy epochs prevent allowed -> NSFW -> allowed consent resurrection, including lost notifications. Reapproval cannot revive old generation bindings. Exact description confirmation can accompany manual reindex but cannot restore consent, expand fields or approve children. Dependency remove/readd advances affected history; cleanup cannot silently index. Private configuration is separate, including explicit re-registration CAS; approval cannot make it NON_PRIVATE. All these paths have real Java/PG positives/negatives, while complete failure/interleaving coverage remains bounded below.

Ad-hoc selection retention is separate from completed work credentials; current Foundation domain indexes are persistent and live external execution stays disabled. No session-scoped/private content is authorized to become permanent by E1. No NSFW/Private, field, provenance, source-integrity, transitive-freshness or offline waiver is accepted.

## 7. Complete B01–B20 audit and original finding traceability

PASS means the indicated bounded case is independently exercised; NOT_VERIFIED means a mandatory part of that row remains unproved, even where substantial subcases pass. Test names below refer to actual maintained bodies, not label-based acceptance. Java RAG tests use real PG/native services unless identified as units or administrative fault fixtures. Python unit fixtures that replace `claim_input` are not SQL/native evidence.

| Case | Exact maintained tests / boundary | Independent result and missing subcases | Disposition |
| --- | --- | --- | --- |
| B01 | `RagSourceReservationsIntegrationTest.nsfwOwnerFirstAndResetRoundTripDenyAdmissionWithoutWorkerGrant`; `RagSourceAuthorityIntegrationTest.nsfwAfterAdmissionNeverReachesWorker`; `RagFinalizerOwnerConcurrencyIntegrationTest.writerFirstForEveryOwnerPathForcesFreshDenialAfterActualCommit` | Real owner-C/reservation denials pass; actual source SQL probe confirms no NSFW write commits while reserved. Complete every-maintained-path/HTTP/worker-after-issuance inventory is not established while B03/order fails; a contained dispatch is not real Python proof. | NOT_VERIFIED |
| B02 | `RagSecondJvmRecoveryIntegrationTest.controllerDeathWithActualBufferedHttpInputRetainsExclusionUntilVerifiedRecovery`, `crashAfterIssuedGrantCommitNeverDispatchesOrAutomaticallyResumesInput` | Fresh focused class completed four tests; actual second JVM, Windows child, committed claim/partial wire body and recovery. Reservations persist through genuine controller death and SQL resolution. | PASS |
| B03 | `RagSourceReservationsIntegrationTest.realOwnerSqlBackstopAndPrivileges`, `reservationSurvivesPhysicalCommitAndBlocksSelectedButNotUnrelatedEdits`; independent multi-source cycle | SQL bulk/association rollback and unrelated edits pass, but current Java guard order reproduces deadlock. Complete OLD/NEW/cascade/unique/FK ordering must be regressed with corrected paths. | FAIL |
| B04 | `lostNsfwRoundTripCannotRestoreConsentOrAcceptOldOwnerIntent`, `delayedApprovalCannotUndoRevokeOrFieldReduction`, `privateConfigurationNeverChangesImplicitlyDuringApproval` in reservation suite; retained-history SQL fixture | Real DB/CAS tests pass; separate probe observed source/reset history (2,1) -> (4,4) across lost-event round trip. Retention/overflow/missing identity denials specified and exercised in SQL fixtures. | PASS |
| B05 | `descriptionConfirmationUsesExactBytesAndCannotRenewItemConsent`, `changedDescriptionConfirmationAndReservationCommitTogether`, `manualReindexOfNormalEditRetainsConsentWithoutAnotherApproval` | Real current hash, ordinary-edit and atomic confirmation/reservation tests pass; no automatic renewal/indexing. | PASS |
| B06 | `RagSingleJobRuntimeIntegrationTest.twoActualConcurrentManualStartsCommitOneReservationAndOneActiveAttempt`, `twoExplicitJobsUseFreshActualProcessesAndPublishVerifiedSingleUseReceipts`; Python `test_wrong_binding_denies_without_any_body_receive` | Actual uniqueness/fresh process/one-use positives pass. Python binding cases are units. Complete actual stale-listener/port collision/wrong-process/no-early-wire fault proof is not demonstrated. | NOT_VERIFIED |
| B07 | `actualWorkerIsDeadButPriorSqlWriterMustCommitBeforePublicationCanReleaseReservation`, `queuedActualRoleWriterReloadsTheSealAfterPublicationPhysicalCommit` in single-job suite; `RagPythonCommitTerminationIntegrationTest.nativePythonDeathDoesNotReleaseSourcesWhileItsOwnPostgresCommitIsPending` | Actual native death, backend/attempt locks and both seal orders pass. Restricted/legacy staging routes are covered by SQL and role fixtures. | PASS |
| B08 | `RagNativeWorkerProcessTest.suspendedChildCannotConsumeBeforeBindingAndResume`, `singleProcessLimitDeniesNormalAndBreakawayChildBodies`, `finalHandleCloseActuallyStopsRecordedProcessButMissingJobIsNotRecoveryProof`; cancellation `unknownWorkerBindingRetainsReservationAndSingleJobSlot` | Real native confinement/identity tests pass; unknown binding stays reserved. Full failed termination, late launcher/sender and every spawn-to-bind fault has not been established as an actual supported native failure trace. | NOT_VERIFIED |
| B09 | `RagCommitAcknowledgementRecoveryIntegrationTest.committedPublicationWithInjectedLostJavaAcknowledgementRetainsCurrentIndexAndReconcilesHandles`, `committedCancellationWithInjectedLostJavaAcknowledgementIsNotResumedAndReconcilesHandles`; cancellation physical-C wait test | Real C plus injected lost Java acknowledgement and rollback/visible reservation tests pass. Actual low-disk/ENOSPC outcome is not supplied. | NOT_VERIFIED |
| B10 | `RagSecondJvmRecoveryIntegrationTest.liveControllerPastLeaseExpiryPreventsSecondJvmTakeoverThenGenuineDeathAllowsRecovery`; cancellation `previouslyRegisteredControllerNeverReclaimsAnotherRuntimeOnReconciliation` | Actual second JVM/real minimum-lease wait and controller death/recovery passed in completed focused result; no timeout-based takeover. | PASS |
| B11 | `RagCancellationRecoveryIntegrationTest.startupReconciliationDiscardsUndispatchedWorkWithoutIndexing`; `RagWorkerBoundaryIntegrationTest.actualSpringRuntimeRestartDeniesRestoredConsentAndStaleWorkerGrants`; second-JVM crash tests | Current index/consent survives ordinary restart; unfinished work aborts and stale execution denies. The renamed historical restart test no longer proves uncertain restore; actual declared restore/full OS restart/history reconciliation evidence remains incomplete. | NOT_VERIFIED |
| B12 | Three `RagE1ExpiryIntegrationTest` methods, especially `expiryAfterTimelyLastDeferredGateAllowsRealLateCommitAndPersistentCurrentIndex`; worker `workerExpiryAfterLastLeaseCheckRejectsPhysicalCommit` | Actual late trusted P and observed/post-wait/default-deferred negatives pass. The separate positive after-final-stage-gate late untrusted S interleaving is not established by those P-only/negative tests. No E2 requirement is reinstated. | NOT_VERIFIED |
| B13 | `ordinaryDependencyRemovalAndReadditionPreserveConsentButRequireManualReindex`; single-job `normalEditPreservesConsentButMakesIndexOutdatedUntilManualReindex`; source-authority `newTransitiveChildInvalidatesOldGrantAndPublication` | Real version/scope/field/dependency, child and manual freshness tests pass; monotonic SQL state prevents hash/edge resurrection and unrelated scope remains separate. | PASS |
| B14 | `RagWorkerPostgresPrivilegeIntegrationTest.workerCannotReadDomainVaultTables`, `workerCannotExecuteDdl`, `stageWorkerOutputFunctionEnforcement`; worker `malformedStageDigestAndMissingWorkerProvenanceCannotPublish`, `workerSqlRejectsMalformedAndSameCountSubstitutedDependencyManifests` | Real role/typed producer/scope/legacy-route tests pass; independent qualified worker denials return 42501. Source/slot/policy/release rights denied. | PASS |
| B15 | `RagDomainCommitGuardIntegrationTest.serializesConcurrentWritersOnVaultEntry`; finalizer `cachedSourceAndOwnerContextCannotReuseRemovedDescriptionConsent`; worker `actualAuthorityLockTimeoutAndPublicationSavepointRollbackDenyActivation` | Cache/savepoint/timeout positives/negatives pass, but the actual guard fails before-source busy ordering, twice. New PG two-source cycle confirms the regression. | FAIL |
| B16 | `RagAuthorityContractTest.rejectsQuotedUnknownAndEncodedMetadataWhileAcceptingProse`; source-authority `externalCapabilityCannotBeApprovedOrDispatchedEvenFromRestoredCatalog`; lifecycle restrictive-metadata/external denial cases | Conservative foundation text contract and disabled external capability pass. No enabled external editor/OCR/parser proof is claimed or needed for the disabled capability. | PASS |
| B17 | `oldDependentGenerationCleanupCannotCancelNewReservedScopeThatExcludesTheRevokedItem`; `cancellingNewAttemptPreservesAnIndependentlyCurrentPreviousGeneration`; cancellation `failedErasureRetainsPendingCleanupButSafeTerminalReleaseIsAllowed`; both P/cancel ordering tests | Actual native/SQL cancellation, independent previous generation, exact historical scope and failed-erasure/PENDING cases pass. | PASS |
| B18 | `RagFoundationBenchmark.measureFourAuthorizedSingleJobWorkloadsAndRetainEveryOutcome`; worker `actualWorkflowBenchmarkRecordsOperationLatencyAndProcessResources` | Supplied 420/420 workload and fresh 16-workflow baseline are useful. Full current resource counters/coverage/thresholds/storage/low-disk/leak acceptance remains incomplete. | NOT_VERIFIED |
| B19 | `scripts/worker_boundary_check.py`; application-offline receipt and unexecuted Windows-control plan | No actual enforced native success/cancel/recovery window with required OS/SQL egress evidence. Unit/app checks do not satisfy it. | NOT_VERIFIED |
| B20 | Actual three Java commands, full pytest, Ruff, mypy, architecture/schema/role/Portability, hook tests and inventory audit | Two distinct failures, two aggregate timeouts, missing resource/offline proof and inaccurate inventory. No skips were used to manufacture green. | FAIL |

Original R1–R8 remain mapped: R1/R2 -> B01/02/03/15; R3 synthetic later-artifact denial -> B13/17; R4 -> B04/11; R5 -> B07/13/17; R6 independent selection/work lifetime -> B11/12; R7 -> B16 disabled capability; R8 -> B04/05/13/17. No later product or live-file capability is certified.

| Existing final-review finding | Current disposition |
| --- | --- |
| FR17-5 High | OPEN. The proposed replacement is implemented and substantially verified; no original unauthorized Python counterexample was reproduced against the reservation path. Correct ordering/all-path acceptance and remaining mandatory proofs prevent closure. |
| FR17-8 High | OPEN / FAILED acceptance. New guard regression, auth environment failure, incomplete full gate and missing B-case evidence. |
| FR17-9 Medium | OPEN / PARTIALLY VERIFIED. Meaningful successful workload receipt exists, full current resource/threshold acceptance does not. |
| FR17-10 Medium | OPEN / NOT_VERIFIED for supported enabled OS-enforced offline workflow. Locked package inventory is independently credited. |

FR17-1/2/3/4/6/7/11 retain their historical bounded dispositions; unmodified fixes and retained negatives were exercised. FR17-AB-1 tracks this **new Alternative B ordering regression** separately; old post-wait expiry rejection remains verified. No prior finding is silently waived or globally recertified outside its scope.

## 8. Exact verification and unique-test accounting

The claimed batches total **1,066 Java executions plus 37 pytest = 1,103 executions**. Their surviving XML command properties identify **1,065 distinct Java cases** because the OpenAPI case appears in both platform and web-domain selections; it survives in the latter receipt. Python adds 37 distinct cases. The preserved directory as a whole contains **1,299 distinct testcase records**, including older/default-run records, one manual benchmark case and an already failed nine-case authentication run. It is not one clean successful run. The reported 144 RAG cases comprise **7 + 5 + 11 = 23 classes**, not 18. Exact domain wildcard spellings in XML differ from the summarized commands; the original properties are retained in the accounting receipt.

Independent fresh results are filtered by their actual `-Dtest` property/logged completed classes; stale XML is excluded. The timed-out wrapper's raw directory totals include stale files in non-clean runs and are **not execution counts**.

| Independent command | Fresh result | Exit / limit |
| --- | --- | --- |
| Focused RAG/platform selection below | 177 completed: 176 pass, one failure, no errors/skips | Wrapper 124; tracked cmd tree exit 1 after stop; 600.293s. |
| Full `clean verify`, native access, no test exclusions | 1,059 completed: 1,057 pass, two failures, no errors/skips | Wrapper 124; tracked tree exit 1 after stop; 600.294s; full lifecycle incomplete. |
| Remaining suite selection below | 183 pass, no failures/errors/skips | Exit 0; 170.029s; not a replacement full gate. |
| Full pytest with JUnit XML | 37 pass | Exit 0; two warnings: Starlette/httpx deprecation and sandbox cache write warning. |
| Ruff check / format check | Passed / 19 files formatted already | Both exit 0; no formatting applied. |
| mypy src/ppv_rag | Eight source files, no issues | Exit 0. |
| Repository safety hook tests | 13 pass | Exit 0; separate from worker pytest count. |
| Installed dependency inventory | 32 distributions match uv.lock, license metadata/hashes recorded | Exit 0; not a fresh advisory or exhaustive bundled-library audit. |
| Actual SQL adversarial probes | V1–V17 applied; reserved writes rollback, retained reset/deletion/role assertions; reproduced 40P01 | Both probe commands exit 0 means the observations completed, not that the deadlock passed acceptance. Owned containers removed. |
| Actual Java/PG clock intervals | 20 samples, database behind the complete host interval in all | Exit 0; diagnostic, not auth acceptance. Owned container removed. |
| Git diff whitespace | Passed, ordinary CRLF notices | Exit 0; no warning-free claim. |

Unique independent Java results: **1,298 identities, 1,296 passed, two failed** across **1,419 executions** (1,416 passing executions and three failing executions). The guard failed twice because focused and mandatory full runs both included it; it was not retried after diagnosis to seek green. All 144 RAG identities were seen; 143 passed. The 37 architecture tests, 15 Portability tests, two schema-manifest tests and OpenAPI case passed. The standalone manual 420-job benchmark is not in the default Surefire gate or these Java counts. No Failsafe result is claimed. `scripts/validate_repository.py` is absent; available hook, inventory, hash, link and diff checks were used explicitly instead.

Exact inner Maven commands, from `backend/`:

```powershell
mvn -ntp '-Dtest=Rag*Test,ApplicationArchitectureTests,OpenApiRouteInventoryIntegrationTest,FlywayStartupIntegrationTest,PortabilityIntegrationTest,FlywayV1SchemaManifestIntegrationTest' '-DargLine=--enable-native-access=ALL-UNNAMED' test
mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify
mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=ApiResponseEnvelopeAndErrorIntegrationTest,AppSettingsConcurrencyTest,AppSettingsIntegrationTest,AppSettingsValidationTest,GlobalSearchIntegrationTest,OpenApiRouteInventoryIntegrationTest,PrivacySafeConstraintLoggingIntegrationTest,RagWorkerBoundaryIntegrationTest,RagWorkerPostgresPrivilegeIntegrationTest,ReferenceModuleIntegrationTest,ReferenceWebIntegrationTest,SearchWebIntegrationTest,SettingsWebIntegrationTest,VaultCapabilityMatrixTest,VaultEntryIntegrationTest,VaultMetadataIntegrationTest,VaultMutationConcurrencyTest,VaultWebIntegrationTest' test
```

Exact Python worker commands from `rag-engine/`, using its pinned `.venv/Scripts/python.exe`:

```powershell
python -m pytest -q '--junitxml=C:/Users/VU KHANG/IdeaProjects/personal-private-vault/logs/phase17-alternative-b-independent-final-review-2026-10-16/pytest.xml'
python -m ruff check .
python -m ruff format --check .
python -m mypy src/ppv_rag
python scripts/dependency_inventory.py --output 'C:/Users/VU KHANG/IdeaProjects/personal-private-vault/logs/phase17-alternative-b-independent-final-review-2026-10-16/dependency-inventory.json'
```

The Python paths containing spaces were passed as **single argv elements**, not unquoted shell arguments. Full exact executable/argv/cwd/start/elapsed/process/timeout/exit values are preserved in each receipt. Root hook command was `python .agents/hooks/test_repository_safety.py` with the pinned interpreter. The reusable isolated runner was `review_tools.py --label <receipt> --cwd <directory> run -- <argv>`, default hard timeout 600; it captures stdout/stderr, copies generated reports and stops only the tracked process tree on timeout.

## 9. Portability, module structure, API and remaining limitations

[PortabilitySnapshotAdapter.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java) adds one exact operational-table exclusion, `vault_rag_source_state`. It does not broaden business exports or serialize internal reservations/consent history. The 67-table non-auth allowlist stays exact; the schema test excludes the same exact post-v1 operational table while retaining the 69-table/41-enum V1 checks. Updating manifest highestFlywayVersion to 17 is correct for the migrated test database. Fifteen actual export tests and two manifest tests pass. This does not certify an implemented restore/import path or automatic restoration of RAG consent; declared history loss still requires reconciliation/renewal.

Spring Modulith's 37 architecture checks passed. Vault owns metadata/operations; Fiction/Film retain content/NSFW; RAG owns policy/jobs/native runtime/generations/purge. Public immutable records/named interfaces remain narrow and no entity/repository crosses ownership. ADR-0016 shared envelopes and module-local adapters remain. No new distributed control plane, generic shared module or later-phase implementation was introduced. Existing external-file native code/tests remain for the explicitly disabled capability, not as active F metadata fencing.

Resource, power-loss, native-firewall, exhaustive bundled-library and later product guarantees cannot be inferred from ordinary shutdown, package scans or unit mocks. Live external files stay disabled pending R7 proof. Storage outage may leave purge pending. These limitations are not fresh approvals or silent waivers of mandatory enabled Foundation acceptance.

## 10. Shared 29-dimension evidence ledger

| # | Dimension | Status | Evidence / limit |
| --- | --- | --- | --- |
| 1 | Owner intent/scope | PASS | Permanent single-user Windows, manual one-job Foundation and approved B/E1 retained. |
| 2 | Requirement traceability | FINDING | Full B01–B20 claim exceeds actual proof; §7 gives exact gaps. |
| 3 | Architecture/ADR consistency | FINDING | Approved global lock order contradicted by admission/owner helper; no architecture redesign required. |
| 4 | Module/public ownership | PASS | 37 architecture tests; Vault state, owner projections, RAG lifecycle and worker restrictions. |
| 5 | Package/folder structure | PASS | New files stay in existing capability/api/internal packages; descriptors retained, no generic module. |
| 6 | Shared contracts/errors | PASS | Narrow command/view/exception records and root ApiResponse; safe P1701 translation, no common module. |
| 7 | SOLID/cohesion | PASS | Authority, policy, supervisor/native adapter, finish/current/purge responsibilities separated. |
| 8 | DRY/KISS/YAGNI | PASS | Canonical owner history/SQL backstop, reused job/grant engine, no speculative global coordinator. |
| 9 | Pattern fit | PASS | Reservation/CAS/native Adapter/attempt fence have concrete purposes. |
| 10 | Domain/lifecycle semantics | PASS | Real consent/reset/description/manual freshness and independent historical outcome tests; limitations in §7. |
| 11 | API/wire/error behavior | FINDING | Guard waits/deadlock defeat intended earlier busy ordering; identifiers/status documentation also drift. |
| 12 | Validation/hostile input | PASS | Typed exact field/digest/producer/metadata and disabled-path negative regressions passed. |
| 13 | Auth/privacy/consent acceptance | NOT_VERIFIED | Strong bounded positives, but complete all-path/failure proof and full auth/environment gate not established. |
| 14 | Credentials/egress/retention | NOT_VERIFIED | No observed raw leakage; actual supported OS-enforced offline proof missing. |
| 15 | Transactions/concurrency | FINDING | Maintained guard fails and actual 40P01 cycle reproduced; atomic finish positives credited. |
| 16 | Failure/recovery/operations | NOT_VERIFIED | Real second-JVM/native/writer recovery passes; complete failed-kill/restore/low-disk proofs incomplete. |
| 17 | Database/migrations/roles | PASS | V1–V17 SQL/Flyway/schema checks, immutable history and actual effective worker denials; ordering defect separately tracked. |
| 18 | Actual resource bounds | NOT_VERIFIED | 420-job receipt useful; complete current counter/threshold/leak/headroom acceptance missing. |
| 19 | Retrieval/recommendation quality | N/A | No later retrieval/model/recommendation product enabled. |
| 20 | OCR/document/multilingual fidelity | N/A | No parser capability; live files disabled and later gates retained. |
| 21 | Adversarial security coverage | FINDING | B03/B15 fail; mandatory B06/08/09/11/12/18/19 subcases not fully proved. |
| 22 | Test quality/coverage/accounting | FINDING | 1,298 unique cases exercised but two fail; stale report and overlapping execution counts require reconciliation. |
| 23 | Benchmarks/thresholds | NOT_VERIFIED | Meaningful supplied successful measurements; no complete measured acceptance disposition. |
| 24 | Held-out evaluation science | N/A | No model/parser/judge selected; P17-N1 remains a later first-capability gate. |
| 25 | Dependencies/licenses/lifecycle | NOT_VERIFIED | 32 installed locked distributions verified; no fresh advisory/exhaustive bundled-library or offline runtime certification. |
| 26 | Build/static/CI | FINDING | Python/static/hooks pass; full clean Maven timed out with failures, lifecycle incomplete. |
| 27 | Observability/error safety | PASS | Typed non-content diagnostics; pending versus terminal/erased and historical versus current states distinguished. |
| 28 | Documentation/governance | FINDING | 42 unlisted paths, wrong identifiers/class count and premature complete status; §3 remediation required. |
| 29 | Hygiene/preservation | PASS | Exact snapshots/diffs, no implementation mutation by reviewer, protected history retained. |

## 11. Required remediation and exact next owner action

1. Return the candidate to **Antigravity under the existing handoff** for FR17-AB-1 and its actual Java/PG regressions. Preserve valid source privacy, native supervision, writer fences, E1 and lifecycle work; do not weaken unrelated tests.
2. Resolve current verification failures, clock/environment diagnosis and remaining B-case proof. Run finite, source-matched full/static/architecture/role/Portability checks; document a genuinely complete gate rather than summing incomplete commands.
3. Complete FR17-9's current successful resource/threshold evidence and FR17-10's authorized enabled OS-enforced offline workflow. Keep default containment; no global enablement based on partial tests.
4. Correct implementation inventory/evidence/status, including ACTIVE CHANGES_REQUESTED and open findings, then request a **NEW independent final implementation review**. The reviewer did not edit ACTIVE because this task forbids it.

**Owner's next action:** give this report to Antigravity with authorization to remediate these findings within P17-RAG-FOUNDATION and obtain the missing permitted runtime evidence. Separate owner assistance may be needed for reviewed Windows elevation/network controls. No new handoff, contract reapproval, Phase 18–22 work, commit or push is indicated. **No commit message is supplied because the verdict is CHANGES_REQUESTED.**

## Appendix — exact implementation-session path delta

The following 68 modified and 35 added paths are relative to the accepted contract-review state. The full 240-path owner-HEAD inventory and exact hashes remain in the isolated evidence described in §2.

| Change | Repository path |
| --- | --- |
| Modified | `AGENTS.md` |
| Modified | `backend/AGENTS.md` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/ApiExceptionHandler.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionService.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionSourceService.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmService.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmSourceService.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobStatusView.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagOperations.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDependencyGraph.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagFinalizationGate.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagJobService.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagLimits.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagPurgeService.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRecoveryService.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeIdentity.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagService.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceAuthority.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceService.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerGrantService.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagJobLease.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagSource.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/RagEngineClient.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/advice/RagExceptionAdvice.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagJobController.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagSourceController.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/AdmitJobRequest.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/ApproveSourceRequest.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagJobStatusResponse.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagSourceResponse.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RegisterDomainSourceRequest.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/mapper/RagWebMapper.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagSourceView.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryMutationGuard.java` |
| Modified | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/package-info.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/FlywayStartupIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/migration/FlywayV1SchemaManifestIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractRagIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagDomainCommitGuardIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagEngineClientTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFinalizerOwnerConcurrencyIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLegacyRuntimeDeniedIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLifecycleIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPgVectorCompatibilityIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRuntimeRoleIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceAuthorityIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagStagingCommitFenceIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWebIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerBoundaryIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerFixture.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerPostgresPrivilegeIntegrationTest.java` |
| Modified | `backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java` |
| Modified | `docs/implementation/handoffs/ACTIVE.md` |
| Modified | `docs/implementation/phase-17/README.md` |
| Modified | `docs/implementation/phase-17/alternative-b-verification-plan.md` |
| Modified | `docs/implementation/phase-17/implementation-report.md` |
| Modified | `docs/implementation/phase-17/owner-decisions.md` |
| Modified | `docs/implementation/phase-17/remediation-plan.md` |
| Modified | `docs/implementation/phase-17/test-evidence.md` |
| Modified | `rag-engine/scripts/dependency_inventory.py` |
| Modified | `rag-engine/src/ppv_rag/config.py` |
| Modified | `rag-engine/src/ppv_rag/db.py` |
| Modified | `rag-engine/src/ppv_rag/main.py` |
| Modified | `rag-engine/src/ppv_rag/transport.py` |
| Modified | `rag-engine/tests/test_transport.py` |
| Modified | `rag-engine/tests/test_worker.py` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagConsentCommand.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagIndexCommand.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobBusyException.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagSourceChangedException.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagGenerationState.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagManifestScope.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerSupervisor.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/NativeWorkerProcess.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceEditGuard.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceIndexReservations.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceVersion.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceVersions.java` |
| Added | `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultSourceReservations.java` |
| Added | `backend/src/main/resources/db/migration/V14__rag_source_state_and_reservations.sql` |
| Added | `backend/src/main/resources/db/migration/V15__rag_single_job_and_sealed_worker.sql` |
| Added | `backend/src/main/resources/db/migration/V16__rag_current_read_and_terminal_scrub.sql` |
| Added | `backend/src/main/resources/db/migration/V17__rag_retained_consent_history.sql` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractEnabledRagIntegrationTest.java` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagCancellationRecoveryIntegrationTest.java` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagCommitAcknowledgementRecoveryIntegrationTest.java` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagE1ExpiryIntegrationTest.java` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFoundationBenchmark.java` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagNativeWorkerProcessTest.java` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPythonCommitTerminationIntegrationTest.java` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRecoveryProbeProcess.java` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSecondJvmRecoveryIntegrationTest.java` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSingleJobRuntimeIntegrationTest.java` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceReservationsIntegrationTest.java` |
| Added | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagTestCommands.java` |
| Added | `backend/src/test/resources/rag/retained_consent_history.sql` |
| Added | `backend/src/test/resources/rag/single_job_staging.sql` |
| Added | `backend/src/test/resources/rag/source_state_reservations.sql` |
| Added | `rag-engine/scripts/worker_boundary_check.py` |
| Added | `rag-engine/tests/conftest.py` |
| Added | `rag-engine/tests/test_bound_ingress.py` |
