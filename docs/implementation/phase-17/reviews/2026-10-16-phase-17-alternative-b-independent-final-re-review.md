# Phase 17 Alternative B — independent final implementation re-review

**Verdict: CHANGES_REQUESTED. Not ready for owner commit.**

The source-before-owner correction resolves the previously reproduced ordering defect. The owner-approved timeout and Practical Local Offline amendments are valid and applied. Nevertheless, the independent clean Maven command fails, mandatory adversarial subcases remain unverified, and resource acceptance and current documentation remain incomplete. Alternative B remains the approved architecture; no return to F/H/V/E or strict E2 is requested.

## 1. Authority, baseline and independence

Reviewed on 2026-10-16, branch `main`, HEAD `24e6268eef1685866d919d882ecdf9f532d01961`, including staged, unstaged and untracked files. ACTIVE is **CHANGES_REQUESTED**. Read current repository/backend/docs/test instructions, accepted ADR-0019 as amended by ADR-0020, owner decisions, P17-1/2/3, B01–B20, resource acceptance, implementation/remediation/evidence/inventory documents and the previous independent final review. Reused previously inspected source and historical evidence only after hash comparison; implementation claims are not independent acceptance evidence. Applied codex-final-review, the 29-dimension rubric and relevant RAG, Java, persistence, migration, authentication, testing and Modulith guidance. The explicit owner review request authorizes this review despite the generic skill's usual intake status.

Review evidence directory: `logs/phase17-alternative-b-independent-re-review-2026-10-16/` (**E** below). It contains the initial complete hashes/diffs, original submitted Surefire reports, submitted native benchmark copies, fresh per-command logs/JSON/XML, independent SQL/clock/offline probes, accounting and documentation validation. Ignored evidence must accompany the review when the owner transfers the package; it will not automatically be included in a Git commit.

Only this new report and isolated review tools/evidence were created. No implementation, tests, migrations, accepted contracts, ADRs, owner decisions, ACTIVE or historical reviews were edited. No containment change, new handoff, commit, push or later-phase work occurred. Test-generated ignored receipts are distinguished from immutable review reports. All review application commands had a real watchdog of at most **1,200 seconds**; helper Docker/Java/query operations had shorter deadlines. No application command timed out. Cleanup targeted only owned subprocesses/containers. The initial snapshot and final hash comparison establish delivery preservation.

### Owner amendments

| Amendment | Authoritative verification | Review treatment |
| --- | --- | --- |
| Maximum 1,200 seconds per external command | Owner's current instruction; `owner-decisions.md:7–11`; continuation prompt decision 1 | Supersedes 600 seconds. Independent clean Maven and benchmark use 1,200-second outer watchdogs. A previous 600-second timeout remains a historical timeout, not a retroactive pass. |
| Practical Local Offline | `owner-decisions.md:13–41`; ADR-0020 addendum at line 86; B19; benchmark-plan amendment | Removes required WFP/firewall rules, elevation, system DNS/global IPv4/IPv6/proxy packet denial and associated OS audit receipts. Retains local execution, no Internet/remote-AI dependency, no external pipeline endpoints, pinned installed dependencies and all privacy/consent/native/SQL controls. No removed guarantee is reported tested or passed. |

Physical network disconnection and OS packet isolation were **not** enabled during this review. That limitation is explicitly allowed by the amended owner decision. Current remediation-plan step 6 still states the older network-enforcement requirement; it needs synchronization, not reimposition.

## 2. Actual inventory and governance

Review-start snapshot: **1,983 nonignored files**; no staged patch. Complete inherited delivery versus HEAD: **242 paths**. Versus the accepted contract-review snapshot, current delta is **72 modified + 37 added = 109 paths**, no deletion. The 109 include the previous independent review and a continuation prompt; they are not 109 newly changed implementation files. Full path/hash inventory is in E/`delivery-inventory.json`; E/`implementation-delta.json` and the appendix distinguish attribution baselines.

| Category versus contract-review baseline | Modified | Added |
| --- | ---: | ---: |
| Production Java | 35 | 13 |
| Production resources/migrations | 0 | 4 |
| Backend tests/resources | 18 | 15 |
| Python source/tests/scripts | 7 | 3 |
| Root/backend instructions | 2 | 0 |
| Documentation/review/prompt | 10 | 2 |

Since the previous final review, exactly **12 existing files changed and one prompt was added**: two production classes (`VaultEntryMutationGuard`, `VaultEntryService`), two test files (`RagDomainCommitGuardIntegrationTest`, `RagFoundationBenchmark`), ADR-0020 and seven status/contract/evidence/inventory documents. No deletion. The rest of the previously reviewed implementation is byte-identical, including native supervision, consent/authority/finalization/recovery, Python, SQL V1–V17 and Portability changes. This allows reuse of prior code inspection without attributing old evidence as new execution.

All historical reviews, including the immediately previous report (SHA-256 `756da8e8623bab506361a4c5e66d4231bf9978521212557ee5b53417717dd746`), remain unchanged. Frozen authentication code/tests, Schema v1 DBML and V1–V13 are unchanged. Actual append-only migration sequence is **V1–V17**, with V14 source state/reservations, V15 single-job/sealed worker, V16 current-read/terminal scrub and V17 retained consent history.

The current inventory is still inaccurate: it says **104 (69 modified/35 added)**, contains **105 unique listed paths**, and omits four paths from the 109-path delta: ADR-0020, `benchmarks-and-acceptance.md`, the continuation prompt and the previous independent final review. Its asserted HEAD baseline also does not describe the full 242-path inherited delivery. These baselines must be named separately.

ACTIVE's CHANGES_REQUESTED status is appropriate. Its “VERIFIED” labels are implementer-attributed submissions and do not override this review. Its latest-review source link still targets an older report. The earlier IMPLEMENTED_AWAITING_CODEX_REVIEW transition was an intake claim, not acceptance; it is no longer the current status. ACTIVE must stay CHANGES_REQUESTED and link this report during the authorized remediation handback. This reviewer leaves it untouched under the review-only restriction.

## 3. Remaining findings, ordered by severity

### FR17-AB-2 — High, OPEN: the mandatory clean gate fails and recovery evidence is not stable

Independent command from `backend/`:

```powershell
mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify
```

The command completed in **709.608 seconds**, process exit **1**, BUILD FAILURE, without hitting the 1,200-second watchdog. Fresh XML has **1,299 unique testcase nodes: 1,297 PASS, one FAIL, one ERROR, zero skipped**. This is a completed failed command, not a timeout or a successful `verify` lifecycle. Maven stopped at test failure; later lifecycle phases are not claimed executed.

1. **FAIL:** `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary`, [line 145](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/RefreshTokenLifecycleIntegrationTest.java#L145): “Expecting code to raise a throwable.” The DB expiry was set to `now()` at line 143, but rotation succeeded.
2. **ERROR:** `RagSecondJvmRecoveryIntegrationTest.liveControllerPastLeaseExpiryPreventsSecondJvmTakeoverThenGenuineDeathAllowsRecovery`, [line 60](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSecondJvmRecoveryIntegrationTest.java#L60), through `Probe.receipt()` [line 229](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSecondJvmRecoveryIntegrationTest.java#L229): Java `FileNotFoundException` reporting that recovery receipt `15353d1f-d362-4bd7-86ab-c631634379ed.json` was being used by another process. This occurred before the case completed its live-controller/expiry/recovery assertions.

The second error is an evidence-harness failure, **not proof of an unsafe controller takeover**. The child already writes a temporary JSON and atomically moves it (`RagRecoveryProbeProcess:138–141`); claiming non-atomic publication is its cause would be incorrect. The reader waits for existence, then opens once (`RagSecondJvmRecoveryIntegrationTest:225–230`). Existence alone did not guarantee readable Windows sharing state. The actual conflicting handle/process is **NOT_IDENTIFIED**; antivirus/indexer involvement is a hypothesis, not a finding of cause. Preserve the receipt, child log and error; diagnose the supported Windows receipt handoff and make its bounded observation reliable without replaying business operations or skipping assertions. The other three second-JVM cases passed.

Antigravity's preserved pre-review XML contains **1,299 unique PASS nodes across 124 suites**, no `-Dtest` selectors, including nine authentication passes. That supports a prior successful test execution, but XML alone does not prove the claimed original command exit/800-second lifecycle. This review did not find a matching new complete command log/exit receipt in the supplied implementation evidence directory. More importantly, the new independent command reproduces failures. No failed suite was retried to manufacture green.

**Clock assessment:** frozen `SessionService.rotate:84–85` compares database-persisted expiry to `Clock.systemUTC()`, while the test writes PostgreSQL transaction time. Fresh separate Java/PG probing found PostgreSQL behind the entire Java request interval in 20/20 samples: DB-minus-Java-before **−357.0123 to −350.2538 ms**, DB-minus-Java-after **−363.7994 to −356.9556 ms**. Older independent receipts observed other relative timings and accepted rotations. These measurements establish noncoherent clocks and an environment-dependent boundary; they do not bracket the exact failing rotation or prove one constant skew direction. A passing saved run does not establish correction. The current claim that connection-pool warmup “aligns timestamps” is unsupported and contradicted as an acceptance argument by this warmed full run.

Required action: diagnose/stabilize the host/VM time environment and record evidence before another clean gate. Preserve frozen authentication code and assertions; any proposed authentication change requires separate owner scope. Correct the Windows test receipt observation and complete the mandatory gate. RAG's authorization deadlines use PostgreSQL time and transactional state; do not justify their correctness by assuming “a 300-ms offset is harmless because leases are long.”

### FR17-AB-2 / FR17-8 — High, OPEN: unchanged mandatory B subcases are still not established

The only newly added maintained regression is the two-source lock test. The previous review's remaining adversarial subcases were not added or supplied as new runtime receipts. The B matrix below records exact methods and boundaries; passing neighboring tests does not prove these assertions:

- B01/B03: complete exclusion-route versus actual input coverage, differing OLD/NEW association owners, indirect/cascade affected sets, selected/unrelated HTTP and complete rollback. The current association UPDATE fixture uses the same owner ID, not an OLD-A/NEW-B reassignment.
- B06: actual stale listener/port collision/wrong-process binding with zero source dispatch/body, beyond mocked claims and ordinary successful native binding.
- B08: live native consumer plus failed/unknown stop and in-progress spawn/bind/late sender boundaries; no release on unproven termination. Existing native confinement and unknown-binding cases are useful but do not cover every listed transition.
- B09: bounded isolated storage/COMMIT failure, preserving atomic P-or-abort/seal/terminal/release. Existing injected lost ACK and deferred-C waits do not establish low-disk behavior.
- B11: declared restore/uncertain source-history suspension and renewed consent. The misleadingly named runtime-restart test now proves normal restart preservation and stale execution denial, not declared restore. No hostile undeclared administrator-restore detection is demanded.
- B12: a distinct positive **S physical COMMIT after its last timely default deferred gate**, followed by rejection of new expired verification. The maintained positive E1 test pauses P, and the maintained worker test pauses **before** the default staging expiry gate and expects rollback. Neither proves that S-positive case.

These are finite accepted obligations, not a request for distributed architecture or Phase 18–22 features. Use controlled barriers and real consumers/transactions where relevant; inject failures at named boundaries without claiming a mocked success is native proof. An actual unsafe result is not alleged for these missing cases. Their status remains NOT_VERIFIED, which cannot close the corresponding mandatory acceptance invariant.

### FR17-AB-3 / FR17-9 — Medium, OPEN: useful resource evidence is stronger, acceptance is incomplete

The two submitted native-resource JSON files independently parse as **210 + 210 successful jobs**, all 420 with CPU, committed-memory and I/O counters. Four fixtures each have five warmups and 100 measured jobs, and below/at/above input/source/depth checks. This resolves the previous **native counter completeness** gap. Submitted peak committed memory maxima are about 49.9 MiB for three fixtures and 56.3 MiB at 1 MiB input, below the configured 512 MiB cap. The test positively checks actual process termination, staging seal, CURRENT publication and zero active reservations/attempts at completion.

However, `RagFoundationBenchmark:100–107` and the JSON limitations explicitly leave worker peak resident set, process-handle leakage and Docker disk I/O unverified. Its startup/stage/stop metric combines three phases; it does not independently measure stop latency, source/attempt guard hold/wait, SQL-writer recovery or purge cost. Database/disk boundary snapshots are not a low-disk/headroom or long-lived tombstone-growth test. A handle close in source is not measured absence of handle growth. Current evidence's assertion that all handle metrics/threshold dispositions are complete exceeds these receipts.

`benchmarks-and-acceptance.md` §9 and remediation-plan step 5 still require supported-desktop cold-start/job/stop/CPU/memory thresholds selected from measurements **before acceptance**. None was approved or concretely disposed of in the new owner decisions; those decisions amend timeout and offline only. Do not invent an SLA, reintroduce the obsolete 16-job saturation target, or free uncertain reservations to satisfy performance. Present measured thresholds and honest remaining resource coverage, plus bounded cleanup/contention/storage evidence.

Independent benchmark and sampler results are recorded in §7 after completion. Even a 420/420 independent success does not fill missing stop/guard/storage measurements or approve thresholds.

### FR17-AB-5 — Medium, OPEN: current documentation still contains contradictory behavior and overclaims

Concrete references:

- `changed-files.md:7–11`: HEAD/104/69+35 inventory mismatch and four omissions described above.
- `implementation-report.md:17`: claims WAITING/RUNNING/CANCELLING are the unique-index states. Actual V15 lines 17–23 cover **ADMITTED/STARTING/ACTIVE/STOPPING/RECOVERY_REQUIRED/FINALIZING**. All six matter to no-eviction safety.
- `backend/dev/rag-db/README.md:36`: says every restart invalidates consent, contradicting ADR-0020 and actual persistent CURRENT/consent preservation.
- `test-evidence.md:69–70`: unsupported fixed skew/pool-warmup alignment explanation; fresh full run failed the boundary again.
- Owner/ADR practical-offline test descriptions refer to “lease token HMAC verification”; the current implementation uses an opaque issued single-use capability and durable SQL checks, with no RAG HMAC operation. Correct the test/evidence description without inventing a new cryptographic requirement.
- ACTIVE/test-evidence resource “complete handle” and “threshold disposition” claims exceed benchmark limitations. Current remediation-plan step 6 retains the superseded OS isolation requirement; its implementation inventory ends at V16 although V17 exists.
- The benchmark's generated `reproduce` string still advertises an 1,800-second outer runner and its JUnit annotation is 1,500 seconds. This review actually enforces 1,200 seconds externally; the stale reproduction guidance does not authorize a longer command.
- ACTIVE's latest independent review link and current/historical sections in implementation-report/README/roadmap need an unambiguous newest verdict. The implementation-report handback still describes the older post-issuance leak as the newest review result.

Preserve historical claims as attributed history and append the new failed receipts; do not rewrite old reviews or convert amended-away requirements into past passes. Synchronize current inventory, approved scope and remaining findings. This review does not edit those files.

## 4. Resolved ordering finding and SQL assessment

**FR17-AB-1: independently verified remediation for the reproduced cycle.** `VaultEntryService:133–134` obtains the source guard before the single Vault row; lines 150–164 prevalidate IDs without row locks, acquire the **whole sorted source-state set**, then call `lockAndRefreshAll`. `VaultEntryMutationGuard` now owns only the existing physical row lock/refresh responsibility. The old A-owner/A-source/B-owner/B-source sequence is no longer the admission helper's sequence. Ordinary unrelated favorite operations retain their frozen idempotent behavior without unnecessary duplicate source acquisitions in the physical-row guard.

Fresh maintained tests: all five `RagDomainCommitGuardIntegrationTest` cases and all 11 `VaultMetadataIntegrationTest` cases pass, including the previously failing `serializesConcurrentWritersOnVaultEntry` and new `twoSourceConcurrentInterleavingsAvoidDeadlockAndFailBusy`.

Fresh independent SQL probe uses PostgreSQL **18.6**, actual `rag_runtime` connections and production V1–V17 SQL. It deliberately observes database waits:

1. B owns physical Vault A before reaching its trigger. Admission A acquires source A+B, then blocks on Vault A. B updates Vault B; the earlier source NOWAIT guard returns **P1701**, B rolls back, and admission locks both owner rows and **COMMITs**. No 40P01 cycle.
2. B first updates Vault B and owns source B. Admission requests the sorted full source set and gets **P1701 before owning any Vault row**. After admission rollback, B updates Vault A and commits. No reverse owner wait/cycle.
3. While the source guard is held, an AFTER favorite INSERT returns P1701 in **11.223 ms** and leaves **zero favorite rows**. This is an actual statement rollback, not a partial successful mutation.

E/`sql-lock-results.json` records the interleavings and migration hashes. This probe executes migration SQL with a minimal Flyway-history placeholder; it is **not** represented as Flyway execution. Fresh maintained Flyway tests separately run Flyway. This proves elimination of the original cycle under the corrected sequence, not universal impossibility of every PostgreSQL deadlock.

V14's AFTER association triggers are unchanged since the previous independent review. They gather OLD/NEW affected IDs and obtain earlier source guards with NOWAIT; uniqueness/FK checks can happen first, so blanket claims that no wait precedes a trigger are wrong. The main admission no longer holds an earlier Vault row while waiting for the next source guard. Additional reassignment/cascade interleavings remain in B03/B15 coverage, rather than being inferred from an ordinary INSERT. Runtime cannot disable triggers/TRUNCATE/DDL. Previously verified BEFORE DELETE `RETURN OLD` behavior for non-RAG types is hash-preserved; fresh broad Vault/domain tests remain green.

Worker least privilege: fresh actual `rag_worker` probes return **42501** for business SELECT, source-history SELECT, policy UPDATE, release function, TEMP, CREATE SCHEMA and SET ROLE. Fresh role/staging suites test allowed scoped writes plus legacy/effective privilege denials. V15 stage routes reload the attempt under FOR UPDATE after waits, retaining it through physical COMMIT; seal/release uses the same fence. Definers have fixed `pg_catalog` search_path and qualified objects, revoked PUBLIC/helper rights and separate owner authority. No new unfenced worker write route was found.

## 5. Source, native lifecycle, publication and E1 assessment

| Group | Implemented | Independently exercised | Acceptance |
| --- | --- | --- | --- |
| A: consent, source history, reservations, owner conflicts | Yes; durable nonexpiring token, exact closure, monotonic source/reset history, CAS and exact description revision, mandatory SQL backstops | Real role/SQL and owner/consent tests; corrected ordering verified | Partial; B01/B03 missing subcases |
| B: one job/native worker/input claim | Yes; static nonterminal uniqueness, known admission C, suspended Job Object process, durable identity before resume/input, one-use claim | Native tests, real Java/Python/PG, buffered controller death and three successful second-JVM cases | Partial; B06/B08 gaps and B10 receipt error |
| C: drain/seal/finish/recovery/freshness | Yes; positive actual stop, attempt fence, atomic P-or-abort/terminal/release, no automatic resume, retained CURRENT checks | Actual pending SQL writer/native death, lost ACK, both cancellation orders, normal restart, exact historical cleanup | Partial; B09/B11 coverage incomplete |
| D: E1 and resources/offline verification | E1 and persistent index semantics implemented; practical offline path real | Observed-expiry/default-deferred denials, late trusted P, CURRENT after TTL, complete Python/static gates | Partial; E1 S-positive/resources/full gate incomplete |

NSFW/Private: ordinary owner changes serialize through stable source rows; a committed reservation prevents a relevant mutation COMMIT until safe finish. Owner-first changes alter version/reset and invalidate admission. Lost allowed→NSFW→allowed notifications cannot erase the retained reset watermark; Private/revoke/field-policy epoch history cannot be approved away by stale CAS. Ordinary eligible edits outdate dependent manifests while retaining participation consent. Description confirmation is exact-byte hash scoped and cannot renew revoked/reset consent. Edge removal/readdition advances ancestor source history, so equal restored topology/hash does not resurrect an old generation. These implementation arguments and passing cases are credited; the missing comprehensive actual-body cases are not claimed complete.

Native supervision is real Windows x64 FFM/Job Object code, not a daemon mock: suspended launch, noninheritable owned handles, kill-on-close, process-count one, 512 MiB committed-memory ceiling, actual process/creation identity, no breakaway, known endpoint nonce/job/attempt/PID binding and actual wait/tree-empty proof. Java completes launcher/sender scope and HTTP transport termination before declaring stopped. No lease, disconnect, HTTP ACK or kill request releases a reservation. Unknown control/process/SQL state stays reserved and occupies the nonterminal slot. A pending PostgreSQL writer can survive Python death; the maintained real native/SQL case proves publication/release waits for its physical outcome and later writers reload a closed seal.

`RagFinalizationGate:63–117` rejects ambient entry, requires stopped proof, acquires current source/policy/attempt/generation/run authority, rechecks durable producer/provenance and observed PostgreSQL expiry after waits, and atomically commits publication/seal/terminal/exact release. Handle forgetting follows a known successful C. Unknown outcomes are reconciled by fresh durable reads, not replaying a publication callback. Cancel-first blocks relevant edits with 409/no successful mutation; P-first returns completed, and cancelling a failed new reindex preserves an independently CURRENT previous index. Verified derivative erasure is separate from safe source release; erasure failure remains inaccessible/PENDING, not PURGE_COMPLETE.

**Approved E1 is honored:** (a) timely gate can precede late transfer/consumption; (b) timely staging gate can precede late untrusted raw-free S COMMIT; (c) timely Java verification/publication gate can precede late trusted VERIFIED/P COMMIT. No finite timeout/deferred trigger is claimed to establish strict physical-instant E2. All newly observed expired input/S/new verification/P gates, post-wait checks and default deferred denials remain required. Fresh late-P tests pass and completed CURRENT indexes survive execution expiry and normal restart. Persistent lifetime applies to the implemented consented Fiction/Film index; no ad-hoc/session-scoped private content is made permanent. There is no implemented retrieval/ranking/count/answer product to certify. Future exposure must use fresh IndexCurrent; current status authorization already denies OUTDATED/restricted results.

Live external files remain explicitly disabled; R7 namespace/COMMIT proof is not waived. Full machine power-loss durability and exhaustive bundled OS/DLL auditing remain disclosed unverified guarantees, not newly demanded enterprise scope. Declared restore/uncertain source-history handling is a separate current B11 obligation and is not proven by a normal Spring restart.

## 6. B01–B20 and original security traceability

Method names below refer to maintained tests under `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/`, unless qualified otherwise. **PASS** means the bounded case was independently exercised; **NOT_VERIFIED** means at least one mandatory subcase remains missing even where listed tests pass. The new full command provides current results; older results are attributed separately.

| Case | Exact maintained method(s) / real versus mock boundary | Result and remaining subcase |
| --- | --- | --- |
| B01 | `RagSourceReservationsIntegrationTest.nsfwOwnerFirstAndResetRoundTripDenyAdmissionWithoutWorkerGrant`; `RagSourceAuthorityIntegrationTest.nsfwAfterAdmissionNeverReachesWorker`; `RagFinalizerOwnerConcurrencyIntegrationTest.writerFirstForEveryOwnerPathForcesFreshDenialAfterActualCommit` | **NOT_VERIFIED** complete case. Real PG/owner denials pass; the “never reaches worker” test uses contained entry after cancellation. No complete all-route actual native body-transfer matrix. |
| B02 | `RagSecondJvmRecoveryIntegrationTest.controllerDeathWithActualBufferedHttpInputRetainsExclusionUntilVerifiedRecovery`, `crashAfterIssuedGrantCommitNeverDispatchesOrAutomaticallyResumesInput` | **PASS**, actual second JVM, real worker/buffered input, durable reservations and recovery; distinct from failed B10 case. |
| B03 | `RagSourceReservationsIntegrationTest.realOwnerSqlBackstopAndPrivileges`, `reservationSurvivesPhysicalCommitAndBlocksSelectedButNotUnrelatedEdits` | **NOT_VERIFIED** complete case. Real bulk/selected/unrelated/savepoint fixture and triggers pass; differing OLD-A/NEW-B reassignment and complete cascade/HTTP route audit not demonstrated. |
| B04 | `lostNsfwRoundTripCannotRestoreConsentOrAcceptOldOwnerIntent`, `delayedApprovalCannotUndoRevokeOrFieldReduction`, `privateConfigurationNeverChangesImplicitlyDuringApproval` in reservation suite; retained-history SQL | **PASS** bounded consent/reset/CAS/type/history assertions, real PG; no notification authority. |
| B05 | `descriptionConfirmationUsesExactBytesAndCannotRenewItemConsent`, `changedDescriptionConfirmationAndReservationCommitTogether`, `manualReindexOfNormalEditRetainsConsentWithoutAnotherApproval` | **PASS**, real exact hashes and atomic confirmation/reservation. |
| B06 | `RagSingleJobRuntimeIntegrationTest.twoActualConcurrentManualStartsCommitOneReservationAndOneActiveAttempt`, `twoExplicitJobsUseFreshActualProcessesAndPublishVerifiedSingleUseReceipts`; Python `test_wrong_binding_denies_without_any_body_receive` | **NOT_VERIFIED** full binding-fault case. Actual uniqueness/native/claim positives pass; Python binding tests mock claim authority; actual stale listener/port collision/wrong-process zero-body case missing. |
| B07 | `actualWorkerIsDeadButPriorSqlWriterMustCommitBeforePublicationCanReleaseReservation`, `queuedActualRoleWriterReloadsTheSealAfterPublicationPhysicalCommit`; `RagPythonCommitTerminationIntegrationTest.nativePythonDeathDoesNotReleaseSourcesWhileItsOwnPostgresCommitIsPending` | **PASS**, real Windows process and pending PG COMMIT/attempt sealing in both orders. |
| B08 | `RagNativeWorkerProcessTest.suspendedChildCannotConsumeBeforeBindingAndResume`, `singleProcessLimitDeniesNormalAndBreakawayChildBodies`, `finalHandleCloseActuallyStopsRecordedProcessButMissingJobIsNotRecoveryProof`; cancellation `unknownWorkerBindingRetainsReservationAndSingleJobSlot` | **NOT_VERIFIED** full fault case. All eight native tests pass; failed stop and late launch/send/bind transitions with a live consumer remain missing. |
| B09 | `RagCommitAcknowledgementRecoveryIntegrationTest.committedPublicationWithInjectedLostJavaAcknowledgementRetainsCurrentIndexAndReconcilesHandles`, `committedCancellationWithInjectedLostJavaAcknowledgementIsNotResumedAndReconcilesHandles`; cancellation `sourceReservationRemainsVisibleWhileTerminalPhysicalCommitWaits` | **NOT_VERIFIED** full storage-failure case. Real C with injected Java ACK loss and actual deferred C wait pass; low-disk/failed storage C not proven. |
| B10 | `RagSecondJvmRecoveryIntegrationTest.liveControllerPastLeaseExpiryPreventsSecondJvmTakeoverThenGenuineDeathAllowsRecovery`; cancellation `previouslyRegisteredControllerNeverReclaimsAnotherRuntimeOnReconciliation` | **FAIL current gate / ERROR** in receipt observation before full assertions. The second method passes. Prior successful B10 execution is preserved, not substituted for the new error. |
| B11 | `RagWorkerBoundaryIntegrationTest.actualSpringRuntimeRestartDeniesRestoredConsentAndStaleWorkerGrants`; `RagCancellationRecoveryIntegrationTest.startupReconciliationDiscardsUndispatchedWorkWithoutIndexing` | **NOT_VERIFIED** complete restore case. Actual normal restart preserves CURRENT and denies stale work; named method does not execute declared restore. No physical reboot/power-loss receipt claimed. |
| B12 | `RagE1ExpiryIntegrationTest.expiryAfterTimelyLastDeferredGateAllowsRealLateCommitAndPersistentCurrentIndex`, `expiryBeforeRetainedDefaultDeferredGateRejectsPhysicalCommit`, `observedExpiryAfterActualGenerationRowLockWaitStillDeniesPublication`; worker `workerExpiryAfterLastLeaseCheckRejectsPhysicalCommit` | **NOT_VERIFIED** full case. Actual late P and observed-expiry negatives pass; distinct E1(b) late-S-positive missing. |
| B13 | `RagSingleJobRuntimeIntegrationTest.normalEditPreservesConsentButMakesIndexOutdatedUntilManualReindex`; ACK suite `ordinaryDependencyRemovalAndReadditionPreserveConsentButRequireManualReindex`; authority `newTransitiveChildInvalidatesOldGrantAndPublication` | **PASS**, real version/scope history and no resurrection/manual reindex. |
| B14 | `RagWorkerBoundaryIntegrationTest.malformedStageDigestAndMissingWorkerProvenanceCannotPublish`; `RagEngineClientTest.wrongStageDigestScopeJobAndUncommittedResponseAreRejected`; reservation `realSingleJobSqlAndRestrictedWorkerStagingRoutes`; role/privilege suites | **PASS** bounded raw-free Foundation provenance and actual role denials. HTTP fake responses and actual PG/native production receipts are distinguished. |
| B15 | `RagDomainCommitGuardIntegrationTest.serializesConcurrentWritersOnVaultEntry`, `twoSourceConcurrentInterleavingsAvoidDeadlockAndFailBusy`; finalizer `cachedSourceAndOwnerContextCannotReuseRemovedDescriptionConsent`, `rollbackAfterRealFinalizationDoesNotExposeActiveGeneration`; E/SQL probe | **PASS** previously reproduced ordering/cache/ambient/rollback regression scope; reassignment path completeness remains B03. No universal deadlock freedom claim. |
| B16 | `RagSourceAuthorityIntegrationTest.externalCapabilityCannotBeApprovedOrDispatchedEvenFromRestoredCatalog`, `newTransitiveChildInvalidatesOldGrantAndPublication`; `RagAuthorityContractTest.rejectsQuotedUnknownAndEncodedMetadataWhileAcceptingProse`; lifecycle `rejectsApprovalForExternalFileWithRestrictiveMetadataWhitespaceAndQuotes` | **PASS** unchanged bounded exclusions and live-file containment. No live external namespace proof claimed. |
| B17 | `RagCommitAcknowledgementRecoveryIntegrationTest.oldDependentGenerationCleanupCannotCancelNewReservedScopeThatExcludesTheRevokedItem`; cancellation `failedErasureRetainsPendingCleanupButSafeTerminalReleaseIsAllowed`; single-job P/cancel both-order tests | **PASS**, actual historical manifest, previous CURRENT preservation and PENDING versus verified cleanup. |
| B18 | `RagFoundationBenchmark.measureFourAuthorizedSingleJobWorkloadsAndRetainEveryOutcome`; worker `actualWorkflowBenchmarkRecordsOperationLatencyAndProcessResources` | **NOT_VERIFIED** complete acceptance. Useful workload/native counters verified; stop/guard/storage/leak thresholds and measurements remain incomplete. See §7. |
| B19 | Python `test_bound_ingress.py` / `test_consumption_containment.py`; maintained `scripts/worker_boundary_check.py`; `RagWorkerBoundaryIntegrationTest.javaPythonRestrictedPostgresRoundTripPublishesVerifiedReceipt` and native cancel/recovery cases | **PASS under amended Practical Local Offline**, bounded app/configuration/actual local execution evidence. No physical network cut, OS packet isolation, all-library/native-libpq egress interception claimed. |
| B20 | Fresh full clean command, complete pytest/Ruff/format/mypy, architecture/Flyway/role/Portability/hook/doc checks | **FAIL** overall gate: one failure, one error, mandatory subcases and resource/doc acceptance incomplete; zero skips. |

Original R1/R2 → B01/02/03/07/15; R3 → synthetic publication/current/purge B13/17 (later LLM artifacts absent); R4 → B04/11; R5 → B07/13/16/17; R6 → B11/12 with approved E1; R7 → B16 disabled; R8 → B04/05/13/17. No original exclusion/provenance/consent requirement was relaxed by the offline amendment.

| Finding | Current disposition |
| --- | --- |
| FR17-5 High | **OPEN / partially independently verified.** Durable reservation closes the original grant-then-NSFW ordering argument and corrected source order passes; mandatory complete exclusion-path/native coverage remains. No original unauthorized consumption was reproduced against the corrected reservation implementation. |
| FR17-8 High | **OPEN / FAILED acceptance.** Fresh gate failure/error and missing mandatory B subcases. |
| FR17-9 Medium | **OPEN / partially verified.** Complete native counters and useful successes; resource thresholds and remaining cleanup/storage/guard evidence missing. |
| FR17-10 Medium | **Amended Practical Local Offline core verified; final closure withheld with current evidence synchronization.** Original OS-enforcement criterion is superseded, not failed or passed. No further WFP/firewall proof is requested. |
| FR17-1/2/3/4/6/7/11 | Historical bounded closures preserved; unchanged implementation and retained negative regressions exercised. No global recertification of deferred products. |

Previous-review remediation IDs: **FR17-AB-1 resolved** for the reproduced cycle; **FR17-AB-2 OPEN**; **FR17-AB-3 OPEN** with full native counters and bounded handle/process/connection evidence now credited; **FR17-AB-4's original OS criterion superseded**, amended practical checks independently pass; **FR17-AB-5 OPEN**. Supersession is an owner requirement change, not a retrospective firewall test result.

## 7. Independent execution, performance and practical offline evidence

Exact argv, working directory, start, hard limit, exit and retained output are in E/`<label>.json` / `.log`. Commands below use the pinned `rag-engine/.venv/Scripts/python.exe` for Python.

| Command / label | Independent result |
| --- | --- |
| Backend: `mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify` | Exit **1**, 709.608s, 1,299 unique nodes, 1,297 PASS / 1 FAIL / 1 ERROR / 0 skips; 124 XML suites. |
| rag-engine: `python -m pytest -q --junitxml=../logs/phase17-alternative-b-independent-re-review-2026-10-16/pytest.xml` | Exit 0, **37 passed**, 0.63s pytest time; one Starlette/httpx deprecation warning. |
| `python -m ruff check .` | Exit 0, all checks passed. |
| `python -m ruff format --check .` | Exit 0, **19 files** formatted. |
| `python -m mypy src/ppv_rag` | Exit 0, **8 source files** checked. |
| `python scripts/dependency_inventory.py` | Exit 0, **32 installed distributions**, actual installed versions match `uv.lock`. Not a fresh vulnerability-advisory or exhaustive native-bundle audit. |
| Root: `python .agents/hooks/test_repository_safety.py` | Exit 0, **13 passed**. |
| E/`sql_lock_probe.py`, via 1,200s runner | Exit 0, 4.633s; actual runtime ordering/no-cycle/AFTER rollback and seven worker privilege denials; owned DB removed. |
| E/`clock_probe.py`, via 1,200s runner | Exit 0, 7.119s; 20 Java/PG offset samples; owned DB removed. |
| E/`practical_offline_probe.py` initial setup | Exit **1**, 5.053s. Maintained script failed import before app entry because the review harness omitted PYTHONPATH. Original harness/receipt/log retained; no application assertion was weakened. |
| Same isolated harness with the production launcher's explicit `PYTHONPATH=rag-engine/src` | Exit 0, 5.764s. Invokes maintained `scripts/worker_boundary_check.py --isolated-test-database --deny-external` with real migrated PG/restricted role; owned DB removed. Corrected harness only, not a candidate fix or retry of failed security assertions. |
| Graphify targeted query | Exit 0, 30s limit; navigation only, verified in canonical source. No graph refresh or generated graph used as correctness evidence. |
| E/`validate_docs.py`; `git diff --check` | 33 current changed Markdown documents, **489 local targets / 21 fragments**, no missing target/anchor; whitespace check exit 0. Final report validation recorded separately. |
| Explicit native benchmark: backend `mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagFoundationBenchmark' test` | Exit **0**, **707.851s**, one fresh JUnit case PASS; **420/420 useful jobs succeeded**, 400 measured + 20 warmups, all four workloads complete; full native counters and matched process sampling for all 420 workers. |

Fresh clean test details: **145 RAG cases across 23 classes: 144 PASS, one ERROR**. Root architecture 37 PASS, Collection/Knowledge architecture 3+3 PASS, Flyway startup 2 PASS, schema manifest 2 PASS, Portability 15 PASS, OpenAPI route inventory 1 PASS, pgvector compatibility 2 PASS, native worker 8 PASS, source guard 5 PASS, Vault metadata 11 PASS. Authentication lifecycle: **8 PASS / 1 FAIL**. These are subsets of 1,299, not extra test executions. No maintained suite was excluded by `-Dtest` in the clean run. Benchmark class intentionally does not match default Surefire test-name conventions and is separately invoked; its 420 job outcomes are not 420 JUnit cases.

The watchdog saved the actual Maven exit/log/XML before a console-only UnicodeEncodeError while printing the log tail; that reviewer-wrapper printing fault is separately disclosed and is not the cause of Maven's failure. Subsequent reviewer wrapper output encoding was corrected without rerunning the gate. Non-clean benchmark directories include old XML: final accounting filters by the benchmark's actual `test=RagFoundationBenchmark` property and never counts stale authentication/recovery reports as newly rerun.

### Independent 420-job result and measured limits

E/`independent-native-benchmark-results.json` is the complete fresh receipt; E/`benchmark-analysis.json` matches actual worker PID **and creation FILETIME** to the separate Windows samples. All **420/420 useful jobs passed**, with **400 measured jobs plus 20 warmups**; **420/420 native resource records**, **420/420 worker identities sampled**, all nine boundary checks true, zero failed jobs and all requested work completed. Every job asserts actual worker death, CURRENT publication and staging closure; final assertions require zero active attempts and zero held source reservations. This independently confirms useful real Java/native Python/restricted PG execution, not only an implementer claim.

Windows 11 x64, Java 25.0.2, native pinned Python 3.14.4, PostgreSQL 18.6, six CPU processors and 17,112,092,672 bytes physical RAM. Each fixture has five warmups then 100 measured sequential cold worker processes; JVM/DB/filesystem caches remain warm. Percentiles use nearest rank among measured successful jobs; all outcomes are retained. Sampling was active during measurement and can affect timing. No model/GPU/parser performance is inferred.

| Fixture | Nodes / depth nodes / input bytes | Total job p50 / p95 / max ms | Cold worker + S + stop p95 ms | Worker CPU p95 ms | Max committed job MiB |
| --- | --- | ---: | ---: | ---: | ---: |
| small | 1 / 1 / 146 | 1195.7 / 1364.0 / 2213.5 | 1168.9 | 796.9 | 49.75 |
| maximum_selected_sources | 100 / 2 / 10827 | 2129.5 / 2900.8 / 5029.1 | 1762.1 | 843.8 | 49.87 |
| maximum_dependency_depth | 5 / 5 / 558 | 1212.9 / 1416.1 / 1739.7 | 1194.3 | 843.8 | 49.97 |
| maximum_selected_input_bytes | 1 / 1 / 1048576 | 1287.0 / 1656.5 / 1753.1 | 1333.6 | 843.8 | 56.53 |

Maximum native committed job memory is **59,277,312 bytes (56.53 MiB)**, below the configured **512 MiB** ceiling. SQL client connections are **10 before/after and 10 at every job boundary**. Database size grows from **12,859,071 to 16,168,639 bytes**, consistent with retained fixture/jobs/generations but not a lifetime tombstone-growth/headroom proof. Workspace usable space falls from **22,821,158,912 to 22,088,323,072 bytes**; that whole-drive delta includes unrelated host activity and is not attributed as per-job physical disk I/O or a proven leak.

The independent sampler produced **10,170 rows**, including **6,046 JVM observations**. JVM handles: first **706**, last **688**, sampled range **688–755**. Live JVM threads at receipt boundaries: **25→24**. Sampled JVM maximum working set: **765,812,736 bytes**; sampled private bytes maximum: **808,288,256 bytes**. Largest sampled worker working set: **71,688,192 bytes**; largest sampled worker handle count: **184**. All 420 workers are matched to completed receipts. Thus bounded repeated-run process/connection/handle observations show no accumulating handle count; this is credited new evidence. Sampled maxima remain lower bounds, not exact resident-memory peaks or an unbounded leak guarantee. The sampler ended when this specific JVM exited; its one terminal process-query exception is retained. Its own embedded benchmark-path coverage field was unavailable; the separate analysis performs the correct PID/FILETIME join against this fresh native receipt.

After commands completed, final process inspection found only the pre-existing owner Java process, no owned Maven/test/Python/worker process, and the same three unrelated PostgreSQL containers. Probe containers were removed by their own IDs; unrelated applications/services were not killed. The maintained benchmark source/receipt still correctly disclaims missing distinct stop/guard/SQL-server/disk metrics and unapproved thresholds. New complete process sampling narrows the prior resource gap; **it does not supply low-disk/fault recovery measurements or select the required acceptance thresholds**.

Unique-test accounting: clean run **1,299 Java**, explicit benchmark **one additional distinct Java case** = **1,300 Java test executions/identities: 1,298 PASS, one FAIL, one ERROR, zero skipped**. Complete pytest adds **37**, safety hooks add **13**, giving **1,350 distinct framework-scoped cases: 1,348 PASS, one FAIL, one ERROR**. The **420 jobs are product outcomes inside one JUnit case**, not 420 extra unit tests. Architecture/Portability/RAG subtotals overlap the clean run and are not added again. No failed Java case was rerun.


Practical Offline: configuration/source inspection finds exact loopback Java endpoints, explicit NO_PROXY/no redirects, a sanitized native environment without inherited proxy configuration, Python exact loopback binding and libpq numeric hostaddr with external service/DNS host denial. No remote model/API/download/telemetry path was found in this Foundation pipeline. Installed dependencies are local and pinned. Fresh real Java→native Python→restricted PostgreSQL→publication cases pass alongside actual cancellation and buffered/crashed-controller cases.

The standalone ASGI/PG probe independently verifies known admission C, restricted role, claim C visible before the first receive, cross-job denial with zero receives, one typed receipt physical C/provenance, duplicate claim denial with zero additional receives and five additional forbidden SQL routes. Its process-local Python audit hook blocks two deliberate external connect attempts and two DNS/proxy resolution attempts. It is **not** OS enforcement, native-libpq interception, real Java HTTP or proof against every bundled library. The complete native Java HTTP path is established by separate maintained integration/benchmark cases, not fabricated by the in-process probe. Those boundaries satisfy the owner's narrower practical contract; no whole-host network disruption is performed.

## 8. Architecture, migration, Portability and 29-dimension ledger

The actual recursively inspected relevant tree remains module-local `rag.api/source/internal(application/domain/infrastructure/web)` with capability-specific Vault `entry` and Fiction/Film `source` named interfaces. Source DTOs expose no entities/repositories. No Vault→RAG dependency or cross-module internal imports were introduced; application architecture and owner architecture tests pass. Shared HTTP envelope/error types remain at the accepted ADR-0016 root boundary, with module-local commands/DTOs/advice. No speculative generic coordinator, multi-host support, automatic indexing or future model/parser/query scope was introduced.

Portability's exact operational-table exclusion preserves its **67 business-table export allowlist**; `vault_rag_source_state` is internal authority/history, not silently exported data or authority to restore old consent. The schema-manifest test separately excludes it from the original public-schema baseline and keeps native enum expectations. Fresh Flyway V1–V17, manifest and all 15 Portability tests pass. Those results do not certify a full restored backup or missing B11 restore workflow.

| # | Dimension | Disposition and concrete evidence |
| --- | --- | --- |
| 1 | Owner intent/scope | PASS — Alternative B, one user/job, explicit consent/manual reindex; no later feature or architecture reopening. |
| 2 | Requirement traceability | FINDING — finite missing B subcases and resource conditions; §3/§6. |
| 3 | ADR/architecture consistency | FINDING — runtime follows ADR-0020; current report/runtime-guide/state-inventory contradictions remain. |
| 4 | Modulith ownership/public boundaries | PASS — actual root 37 and owner 6 architecture tests; narrow named interfaces, no reverse Vault edge. |
| 5 | Recursive package/folder organization | PASS — reviewed actual API/source/internal subtrees and complete path inventory; no new generic framework or misplaced cross-owner persistence. |
| 6 | Shared transport/contracts/exceptions | PASS — ADR-0016 root ApiResponse retained; owner-local DTO/exception/advice/CAS contracts. |
| 7 | SOLID/GRASP | PASS — physical-row refresh separated from source reservation responsibility; capability interfaces and cohesive native/authority/finalization owners. |
| 8 | DRY/KISS/YAGNI/invariants | PASS — no F/V/DRAIN rebuild; canonical SQL versions, immutable proof snapshots and bounded single job. |
| 9 | Patterns/anti-patterns | PASS — repository, native/client adapter and explicit lifecycle serve actual boundaries; no generic multi-host orchestration. |
| 10 | Business/state correctness | NOT_VERIFIED — useful CURRENT/OUTDATED/consent/manual behavior passes; complete restore/failure subcases incomplete. |
| 11 | API/wire compatibility | PASS bounded surface — OpenAPI inventory, explicit CAS DTOs, HTTP busy mapper and safe error contracts; full selected/unrelated mutation HTTP coverage remains B03. |
| 12 | Hostile/input bounds | PASS — exact typed digests/schema, malformed/duplicate header/body tests, real caps below/at/above and no arbitrary file fetch. |
| 13 | Authentication/authorization/privacy | FINDING — frozen authentication boundary fails; RAG reservation/consent/NSFW/Private/role positives and negatives credited, all-path native coverage incomplete. |
| 14 | Secrets/retention/offline | PASS bounded Practical contract — local-only source/configuration/actual app proofs, raw-free provenance, PENDING cleanup; no WFP or exhaustive native audit claim. |
| 15 | Transactions/locks/consistency | PASS reproduced regression scope — corrected sorted all-source order, actual SQL waits/P1701, attempt fencing/atomic C and approved E1; broader subcases separately NOT_VERIFIED. |
| 16 | Recovery/cancel/operations | FINDING — real process/writer/cancel/ACK tests pass; B10 receipt error and B08/09/11 gaps. |
| 17 | Database/migrations/permissions | PASS bounded scope — append-only V14–17, fresh Flyway/schema/privilege/Portability and independent actual-role SQL. |
| 18 | Resource/time/memory/I/O complexity | FINDING — hard caps and useful native measurements; stop/guard/storage/handle/threshold coverage incomplete. |
| 19 | Retrieval/recommendation/index quality | N/A — Phase 17 contains synthetic receipt lifecycle, no retrieval/ranking/recommendation/model product. |
| 20 | Document/OCR/multilingual fidelity | N/A — later ingestion capability; live external files disabled. |
| 21 | Adversarial security tests | NOT_VERIFIED complete matrix — actual native/SQL adversarial proofs plus explicit missing interleavings in §6. |
| 22 | Test assertion quality/determinism | FINDING — maintained assertions preserved; auth environment boundary and Windows receipt observation fail independently. |
| 23 | Performance benchmark credibility | FINDING — measured useful cold jobs and full native counters; acceptance thresholds and missing distinct metrics not approved by receipt generation. |
| 24 | Held-out evaluation science | N/A — separately gated parser/retrieval/recommendation evaluation, not Foundation inference quality claims. |
| 25 | Dependencies/licenses/security lifecycle | PASS pinned inventory scope — 32 installed packages match unchanged lock, exact PG/vector tests; prior advisory/native-license evidence attributed, no fresh/exhaustive audit claim. |
| 26 | Build/static/tooling/CI | FINDING — Python/hooks/architecture pass; clean command exit 1. Compiler/FFM/Mockito/Testcontainers/deprecation diagnostics not called warning-free. |
| 27 | Logging/observability | PASS bounded lifecycle — typed safe errors, source-free benchmark identities/counters, retained exact failure/command receipts; expected closed-context scheduler connection errors distinguished from JUnit errors. |
| 28 | Documentation/governance | FINDING — hash-preserved history/status appropriate; current evidence overclaims, inventory omissions and stale runtime/offline instructions. |
| 29 | Hygiene/delivery/frozen preservation | PASS reviewer preservation — initial/final hashes, no staged patch, contiguous migration history, ignored logs only plus new report; inventory documentation defect tracked separately. |

Final documentation validation: **29 ledger rows, 20 B-case rows, 242 exact inventory rows**, no missing/extra inventory entry, no pending placeholders, all report links/line anchors resolved and `git diff --check` exit 0. All **1,983 pre-existing nonignored hashes remain unchanged**; the sole new nonignored file is this report (**1,984 total**). E/`documentation-validation.json` records its final SHA-256. Current contracts/status documents were inspected and their required corrections reported, not silently edited. Historical failed command receipts and the new failed recovery receipt/child log are retained.

## 9. Exact remaining action

Return the candidate to Antigravity under **the existing P17-RAG-FOUNDATION handoff**, retaining CHANGES_REQUESTED and containment:

1. Diagnose the fresh auth environment failure and Windows recovery-receipt sharing error; preserve all failed receipts and frozen auth code/assertions. Record an actual environment/harness correction before a subsequent full command; do not use a lucky retry as acceptance.
2. Complete the finite B01/B03/B06/B08/B09/B11/B12 proof gaps listed in §3/§6 using controlled, bounded actual-boundary cases. Separate absent product scope/power-loss proof from currently mandatory consent/reservation/restore/COMMIT invariants.
3. Retain the new bounded process/connection/handle credit. Finish resource acceptance: distinct stop/guard/recovery/purge and isolated storage/headroom evidence, plus supported-desktop threshold dispositions based on measurements, without restoring obsolete 16-job/F-fence tests.
4. Synchronize current inventory, states/runtime guidance, amended offline plan, evidence attribution and newest review links. Keep all historical reviews/failures unchanged. Preserve the credited lock-order fix and Practical Offline amendment.
5. After those corrections, submit another independent final review. **No owner commit message or commit readiness is issued.** No new handoff, containment removal, commit/push, Phase 17 closeout or Phase 18–22 work is authorized by this report.

## Appendix — exact review-start changed-file inventory

All **242** inherited changed/untracked paths versus owner HEAD, sorted below. Full SHA-256 values are in E/`delivery-inventory.json` (file SHA-256 `6433fdb88f1c206df5c63019074babef143d423be2e3e0b74c0503cb9afa3354`). `M`/`A` means modified/added versus the accepted contract-review snapshot; `I` means inherited and unchanged versus that snapshot. `R` marks a subsequent remediation change versus the immediately previous independent review. These are attribution labels, not Git staging or test statuses. The new report itself is the sole additional nonignored review delivery path.

| Path | Contract-baseline attribution | Subsequent remediation |
| --- | --- | --- |
| `.codex/config.toml` | I |  |
| `.gitignore` | I |  |
| `AGENTS.md` | M |  |
| `backend/AGENTS.md` | M |  |
| `backend/compose.rag.dev.yml` | I |  |
| `backend/dev/rag-db/ProvisionRagDevelopment.java` | I |  |
| `backend/dev/rag-db/README.md` | I |  |
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
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryMutationGuard.java` | M | R |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java` | M | R |
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
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagDomainCommitGuardIntegrationTest.java` | M | R |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagE1ExpiryIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagEngineClientTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFinalizerOwnerConcurrencyIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFoundationBenchmark.java` | A | R |
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
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerBoundaryIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerFixture.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerPostgresPrivilegeIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` | I |  |
| `backend/src/test/resources/rag/retained_consent_history.sql` | A |  |
| `backend/src/test/resources/rag/single_job_staging.sql` | A |  |
| `backend/src/test/resources/rag/source_state_reservations.sql` | A |  |
| `docs/adr/0019-local-rag-before-frontend.md` | I |  |
| `docs/adr/0020-phase-17-single-job-reservations-e1.md` | M | R |
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
| `docs/implementation/phase-17/antigravity-continuation-prompt.md` | A | R |
| `docs/implementation/phase-17/architecture-simplification-proposal.md` | I |  |
| `docs/implementation/phase-17/benchmarks-and-acceptance.md` | M | R |
| `docs/implementation/phase-17/changed-files.md` | M | R |
| `docs/implementation/phase-17/contract-synchronization.md` | I |  |
| `docs/implementation/phase-17/implementation-report.md` | M | R |
| `docs/implementation/phase-17/integration-ownership-and-permissions.md` | I |  |
| `docs/implementation/phase-17/owner-decisions.md` | M | R |
| `docs/implementation/phase-17/p17-3-consumption-fence-proposal.md` | I |  |
| `docs/implementation/phase-17/remediation-plan.md` | M |  |
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
