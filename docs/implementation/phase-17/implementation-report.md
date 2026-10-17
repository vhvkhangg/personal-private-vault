# Phase 17 — Accepted Foundation Implementation (historical remediation details)

**Current status:** `COMPLETE — FROZEN` after owner implementation commit `829d294` and
[formal closeout](closeout.md). Independent [final acceptance](reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md)
verifies 1,308 Java tests with exit 0; `AUTH-CLOCK` stays OPEN outside RAG. The
remediation narratives below record the submitted and independently verified candidate at their stated dates.

## 2026-10-17 Alternative B Implementation Remediation — Accepted Candidate (READY_FOR_OWNER_COMMIT)

Antigravity completed focused remediation of the remaining findings under the existing `P17-RAG-FOUNDATION` handoff (ADR-0020). The candidate has been reviewed and accepted by the independent final re-review 3 (`docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md`) with verdict `READY FOR OWNER COMMIT`.

### Scope and Implementation Groups (ADR-0020)

1. **Group A — Source Protection, Reservations, Consent CAS**:
   - `vault_rag_source_state` tracks stable retained source state, `source_version`, `consent_reset_version`, and non-expiring `reserved_by` lease identifier.
   - Atomic reservation and admission: selected sources are reserved under exclusive row locks in strictly sorted order; concurrent user mutations return HTTP 409 without committing.
   - Mandatory owner-local SQL trigger backstops on `vault_entries` and association tables (`fiction_story_archetypes`, `fiction_world_settings`, `film_genre_assignments`, `film_story_archetypes`, `film_world_settings`, `favorites`, etc.).
   - Explicit consent CAS commands (`approveSource`, `revokeSource`) with exact description hash confirmation and reset version verification.
   - Categorical exclusion of NSFW and Private sources.

2. **Group B — Execution and Supervised Worker**:
   - Single-job uniqueness enforced statically by PostgreSQL partial unique index on non-terminal job states (`ADMITTED`, `STARTING`, `ACTIVE`, `STOPPING`, `RECOVERY_REQUIRED`, `FINALIZING`).
   - Supervised native Windows Python worker process (`NativeWorkerProcess`) with Win32 Job Object confinement (`JOB_OBJECT_LIMIT_KILL_ON_JOB_CLOSE`), process handle signaling, and one-use ingress claim.
   - Overloaded `isRecordedProcessTerminated(pid, fileTime, Duration timeout)` with bounded `WaitForSingleObject` wait (`STOP_TIMEOUT`), ensuring process object termination transition is verified before state query.
   - Worker writes staging rows exclusively via `rag.stage_worker_output` SECURITY DEFINER function; direct table DML denied to `rag_worker`.

3. **Group C — Staging, Publication, Recovery, and Freshness**:
   - Staging seal and attempt fence: physical publication commits atomic transition, staging seal, and source reservation release.
   - Index freshness lifecycle: completed indexes are `CURRENT`; ordinary eligible source edits make the index `OUTDATED` without revoking consent.
   - Restart and recovery: on crash/restart, non-terminal jobs are cleaned up; completed `CURRENT` indexes survive normal restarts; reindexing is manual only.
   - Operator declared-restore procedure documented and tested: in-flight cancellation, source revocation to `REVOKED`, stale CAS rejection, operator inspection, renewed approval, and fresh publication.

4. **Group D — Approved E1 Semantics**:
   - E1(a), E1(b), E1(c) semantics implemented: post-check late worker activity is safely fenced; completed published indexes have independent lifetime beyond worker lease expiration.
   - Quiescence sequencing enforced: supervisor termination proof precedes finalization, and expired verification gate rejects publication.

### Verification and Review Findings Remediation

1. **FR17-AB-1 (PostgreSQL Deadlock 40P01 & Lock Ordering) — REMEDIATED & VERIFIED**:
   - Sorted acquisition across admission and owner mutations: source guards locked first in ascending order, entry row locks locked second in ascending order.
   - Both admission-first and owner-first concurrent interleavings tested without deadlock.

2. **FR17-AB-2 (Process Termination Resolution & Clock Boundary Diagnosis) — REMEDIATED & REPORTED**:
   - *Win32 Process Termination Resolution*: Overloaded `NativeWorkerProcess.isRecordedProcessTerminated` with `STOP_TIMEOUT` via `WaitForSingleObject`, resolving the 0.022s kernel transition race in `RagNativeWorkerProcessTest:128`. Passed 9/9 (0.969s).
   - *Host/Container Clock Skew Diagnosis*: Diagnosed failing boundary in `RefreshTokenLifecycleIntegrationTest:145` as cross-kernel clock skew between Windows host JVM and Linux kernel in WSL2 Docker Testcontainers (+400ms to +980ms). Frozen authentication code and tests strictly preserved without modification or weakening. Documented as an environmental blocker for owner decision.
   - *Windows Recovery Receipt Sharing Fix*: Probe JSON atomic move and polling retry verified (4/4 passed).

3. **FR17-AB-3 / FR17-9 (Benchmark Receipts & Component Timings) — REMEDIATED & VERIFIED**:
   - Reused 420-job unified benchmark attributed to `logs/phase17-alternative-b-independent-re-review-2026-10-17/reused-independent-native-benchmark-results.json` (exit 0, 707.851s).
   - Corrected cold worker startup + stage + stop p50 range: **1,040.0345–1,443.7374 ms** (p95: 1,168.9026–1,762.1168 ms, max: 2,993.9197 ms).
   - Corrected Max worker CPU: **1,109.375 ms** (p95 <= 843.8 ms).
   - Disclosed component timings and headroom: admission round-trip upper bound (small-fixture p50 60.8 ms, p95 74.9595 ms, max 95.9 ms; global admission max across all four workloads 1,302.6151 ms), publication round-trip upper bound (small-fixture p50 94.9 ms, p95 133.1900 ms, max 258.9 ms; global publication max across all four workloads 850.0647 ms), bounded native wait on handle (202.3568 ms for 200 ms request, `run_native_wait_probe.py`), cancellation unproven-stop wait (10,042.2651 ms, `ReviewReservationProbe.java`), terminal cancel/seal/release round-trip (115.1269 ms, `ReviewReservationProbe.java`), DB growth (3,309,568 bytes, ~3.156 MiB / 3.31 MB, from 12,859,071 bytes to 16,168,639 bytes across 420 jobs, ~7.70 KiB/job), payload headroom (>99.9%), memory headroom (>455 MiB), sampled working set (71,688,192 bytes ~68.37 MiB). Unsupported isolated component ranges removed.

4. **FR17-AB-4 / FR17-10 (Practical Local Offline RAG Operation) — CONTRACT AMENDED & VERIFIED**:
   - Owner Decision 2 adopted Practical Local Offline Operation in lieu of strict OS-level WFP/firewall elevation.
   - Verified local loopback binding, pinned local dependencies, zero external endpoints, and valid cancellation/recovery.

5. **FR17-AB-5 (Inventory and Documentation Drift) — REMEDIATED**:
   - Synchronized `changed-files.md` with 244 review-start / 245 current delivery and 113 review-start / 114 current contract delta (74 modified + 40 added).
   - Replaced "HMAC" with "opaque capability verification" in ADR-0020 line 111.
   - Corrected operator declared-restore procedure in `owner-decisions.md` and `revocation-and-derived-data.md` to use actual production API names (`findSource`, `approveSource(id, RagConsentCommand)`, `admitJob(id, RagIndexCommand)`).
   - Preserved `ACTIVE.md` status as `CHANGES_REQUESTED` with findings FR17-5/8/9/10 recorded as OPEN pending independent review.

6. **FR17-AB-6 / B08 (Concurrent Finish Reconciliation Defect) — REMEDIATED & VERIFIED**:
   - Diagnosed root cause: when another finisher (e.g. background reconciler or concurrent cancel) has committed a safe terminal state and released reservations, a second finisher's attempt to lock the released reservation fails PostgreSQL's check constraint (`Reservation authority denied`). Previously this called `requireRecovery` and returned `false`.
   - Remediated `RagPurgeService.abortJob`: safe reconciliation verifies that the exact attempt reached a terminal state (`CANCELLED`, `ABORTED`, `PURGED`), staging is sealed, worker is proven stopped/terminated, and source reservations are released, returning `true` without false denial.
   - Added deterministic two-finisher regression coverage in both orders (`RagWorkerBoundaryIntegrationTest.concurrentFinishAbortFirstThenCancelReconcilesSafeTerminalOutcome` and `concurrentFinishCancelFirstThenAbortReconcilesSafeTerminalOutcome`) with `pg_advisory_lock` barrier at the dependency graph lock. Both pass cleanly.

### Test Verification Summary
- **Python / rag-engine**: 37/37 pytest passed, Ruff check clean, Ruff format clean, Mypy clean (8 source files, 0 issues), 32-package locked inventory verified.
- **Java RAG Suite**: 154 tests across all 23 test classes passed (100%), 0 failures, 0 errors, 0 skipped (including 25/25 in `RagWorkerBoundaryIntegrationTest` and 9/9 in `RagNativeWorkerProcessTest`).
- **Architecture & Schemas**: 57 tests passed (`ApplicationArchitectureTests`, `OpenApiRouteInventoryIntegrationTest`, `FlywayStartupIntegrationTest`, `PortabilityIntegrationTest`, `FlywayV1SchemaManifestIntegrationTest`).
- **Repository Safety Hooks**: 13/13 passed.
- No production code modified outside authorized scope; no frozen authentication code modified.


## 2026-10-15 continuation — blocked, unsafe execution contained

The latest independent counterexample was reproduced unchanged against disposable
PostgreSQL and real native Python: one verified receipt after an owner NSFW
COMMIT, both through a retained grant and public execution. The root gap is the
release of owning-source guards at issuance COMMIT before HTTP content transfer;
worker SQL subsequently validates RAG snapshots, not current owner state.

The [dependency-aware plan](remediation-plan.md#2026-10-15-continuation--diagnosis-and-dependency-aware-plan)
identifies the specific acceptance blocker and proposed amendment. P17-3 §1A.4
forbids network sends under source guards and owner transactions acquiring RAG
locks; the approved owner delta supplies no synchronous alternative consumption
fence. No second read, asynchronous invalidation or final publication check is
substituted for that fence. No new frozen owner/schema dependency is introduced.

As required for an unproved capability, issuance and Java HTTP content dispatch
now fail closed without reading source content or persisting a grant. Python
HTTP execution returns 503 before its ASGI body receiver is called. There is no
configuration override. Existing grant/result validators, SQL fences, owner
guards, cancellation and cleanup code remain intact; this is containment, not
the requested working replacement. The RAG module descriptor also now lists its
already-existing `worker_grants` and `runtime_state` tables.

Two Java denial regressions and one Python no-body-read regression were added.
Focused Java containment/architecture: 39 passed; full Python: 19 passed, 5 failed
because the old enabled-route assertions remain unchanged. Ruff check/format and
mypy passed. Exact commands/results and the fresh two-clock measurements are in
[test evidence](test-evidence.md#2026-10-15-continuation--blocked-containment-submission).

FR17-5 remains unresolved beyond containment; FR17-8 has no green full result;
FR17-9 configured-limit enabled benchmarks and FR17-10 successful enabled offline
workflow are NOT_VERIFIED. Prior findings retain their independent dispositions;
their previous test receipts are historical and do not establish current enabled
behavior. ACTIVE remains `CHANGES_REQUESTED`, with a blocked implementation note.
This candidate is **not ready for a new independent acceptance review**. The next
step is an explicit decision on the existing P17-3/handoff protocol, then its
implementation and all mandatory verification. No new handoff, commit/push,
finding closure, phase freeze or owner commit readiness is issued.

## Previous implementation submission and independent review

Owner-authorized cycle started 2026-10-14, using existing `P17-RAG-FOUNDATION`. This is the implementer's report, not owner commit approval. The [new independent review, 2026-10-15](reviews/2026-10-15-phase-17-independent-final-codex-review.md) returns **CHANGES_REQUESTED**: FR17-5/8 remain High, FR17-9/10 remain Medium; FR17-1/2/3/4/6/7/11 are closed for the bounded reviewed scope. The implementation claims below remain attributed to the implementation session and do not override that verdict. Historical reviews are unchanged. No commit/push, new handoff, Phase 18–22, Frontend, Ideaverse or frozen authentication implementation change was made.

## Plan and implementation groups

The [dependency-aware plan](remediation-plan.md) was produced before implementation: canonical source/graph authority → issued worker/SQL isolation → publication/lifecycle → deterministic boundary proof → environment/evidence. Focused failures were diagnosed and corrected within those groups. [Current test evidence](test-evidence.md) distinguishes actual receipts from unsupported guarantees. [Exact changed files](changed-files.md) list the candidate relative to owner HEAD, including inherited Phase 17 submission files.

## FR17-1 through FR17-10

| Finding | Implemented correction | Deterministic evidence / limit |
| --- | --- | --- |
| FR17-1 | Live external-file registration, approval, admission, dispatch and publication are disabled. Restored external authority/aliases are scrubbed by migration and cannot enter a dependency proof. Native helper cleanup fix remains intact, unused by publication. | External negative lifecycle/source tests; real NTFS primitive tests retained. No assertion of stable ancestor/root/hard-link/mapping semantics across finalizer COMMIT. Re-enablement needs that missing proof. |
| FR17-2 | A positive plain-prose grammar vetoes quoted JSON/unknown schemas/encoded or metadata-looking text. Syntax never establishes authority; the entire unproved file capability is disabled. | Exact quoted PRIVATE and encoding negatives, genuine prose positives, restored external sources denied at approval/input/publication. No unauthorized parser product. |
| FR17-3 | One bounded transaction-scoped graph snapshot, exact transitive node identities and edges, canonical sorted version-2 manifest, graph mutation epoch/invalidation of every affected root. Typed issued receipt binds fields/revisions/session/input/scope/output/stage. Unchecked executor/staging overloads removed. | Newly added grandchild, changed-child denial, cached consent, concurrent disjoint cycle writes, memoized DAG depth, missing/extra/duplicate/malformed/same-count substituted manifests, wrong result contracts and digest/provenance tests. PostgreSQL independently checks exact closure/topology and lease-manifest digest. |
| FR17-4 | Separate migrator process; non-owner Java runtime profile and effective-role audit; unsafe legacy role cannot authorize RAG. Real Python Psycopg connection uses restricted worker functions and commits before success. Reset unsafe role attributes/memberships/password; fixed search paths; no worker-owned objects. V13 removes effective PUBLIC-inherited TEMP DDL and both runtime audits deny CREATE/TEMP privileges. Verified receipt flag has a DB writer fence. | Actual Java→Python→worker-role→durable receipt→publication, separate source-mode migrator subprocess, real role/DDL/security/business/Flyway/SET ROLE denials, V6→V13 unsafe-role upgrade and exact PostgreSQL/pgvector test. |
| FR17-5 | Fresh guarded eligibility and exact selected-field/revision proof BEFORE dispatch, normal JSON serialization for all allowed fields/statuses. Random issued per-attempt capability plus independent expiring DB session replaces service-key signatures of nonexistent jobs. Canonical durable-result verification runs again before activation. | NSFW/trash/revision/reduced fields deny input; actual Python metrics prove no dispatched request for pre-execution NSFW/reduction. All field/status combinations, escaping, expired/unissued/forged grants, wrong version/stage/digests/kind/identity, unverified raw staging and Java-forged receipt flag deny. |
| FR17-6 | Synchronous policy/lease/pointer denial; SQL erasure under the attempt fence; committed notification; external cleanup outside DB guards; strict attempt-specific quiescence; durable pending state and bounded retries. A JDBC savepoint allows deletion failure without rolling back denial. One Python execution and bounded cancellation tracking eliminate duplicate set-count loss. Runtime restart/restore invalidates prior consent/work. | Actual worker outage/restart, malformed/unknown quiescence, duplicate/in-flight cancellation, DB deletion-failure injection and repair/retry, rollback-no-notification, both-order worker staging COMMIT/purge, expired independent sessions, lost completion acknowledgement, new Spring runtime invalidation and stale-instance rejection. No model/temp-file capability exists to recreate scratch artifacts. |
| FR17-7 | Graph barrier before RAG reads/locks; sorted Vault then RAG policy/attempt/generation locks. Fresh scalar owner reads avoid managed-cache proof. Final expiry uses PostgreSQL time AFTER all publication waits; deferred SQL constraints recheck physical staging/publication COMMIT. | Actual generation-lock wait past deadline, post-gate pre-COMMIT expiry, source writer-first/guard-first across 13 real mutation paths, actual savepoint/outer rollback, real PostgreSQL deadlock and bounded lock timeout; cached context reduction and DAG cases. A newly reproduced cached completed-generation false-active response is rejected after refreshing generation rows under lock. |
| FR17-8 | Replaced misleading serial coverage claims with actual production authority/finalizer barriers, real owner APIs, actual PostgreSQL wait observation and genuine restricted staging. Synthetic digest equivalents are explicitly distinguished from deferred products. | Regression catalog and original R1–R8 ledger below; exact focused/full/Python receipts in current evidence. No mocked OS guarantee or suite-time benchmark claim. |
| FR17-9 | Configurable lower limits with fixed ceilings: 100 selected sources, 1 MiB aggregate input/owner descriptions, 1,000 catalog/retained jobs, 256 jobs/source, 16 pending jobs, 10,000 graph edges, depth ceilings, 3s locks/5s statements/10s transactions; 15m maximum lease. Existing stage aggregate limits retained. Bounded HTTP request/body bytes and total deadline, worker concurrency/ack cache, cleanup executor and round-robin reconciliation. | Constant 5 Fiction / 6 Film fresh batch queries for 1 and 8 IDs, queue cap, byte/selection denials, stalled/oversized HTTP body tests; actual workflow and process measurements. External hash/handle costs eliminated by disabling live files. No claim that a 16-sample sequential baseline proves saturation latency or peak machine RAM. |
| FR17-10 | Frozen Python lock/setup, exact server/extension build and upgrade checks; real OS-network-disabled worker run; installed version/license receipt and bounded advisory audit; synchronized current state/data/migration/package inventories and worker SQL C4/DOT/exported diagrams. | Python 3.14.4; PostgreSQL 18.6 / pgvector 0.8.6; 32 installed Windows distributions and libpq 18.4; pip-audit zero known package advisories. Docker namespace egress proof does not certify native Windows firewall or combined production deployment. |

## Original R1–R8 identities retained

| Identity | Foundation proof | Deferred or disabled portion |
| --- | --- | --- |
| R1 — Embedding vs revoke | Real restricted worker transaction paused after its last lease check, physical COMMIT versus purge in both orders; old publication/staging deny, new attempt independent. | No OCR/chunk/embedding model is enabled; receipt is a controlled synthetic digest. |
| R2 — Domain last-read race | Actual finalizer proof barrier, both COMMIT orders for Fiction NSFW/content/story/world/trash/restore and Film equivalent plus genres; fresh post-mutation deny; lock timeout/deadlock/rollback. | Out-of-band privileged database writers remain outside owner APIs and require runtime suspension/restart/renewal; no frozen owner trigger added. |
| R3 — Generate vs revoke | Synthetic stage completion/publication uses canonical current scope; revoke cancels/purges dependent attempt and stale completion cannot activate. | No LLM, answer/history/citation/highlight/count/ranking or judge product exists; their future gates are not certified here. |
| R4 — Lost deletion event + restart | Live truth rejects mutated/trash sources without events; reconciliation purges; actual Python restart acknowledges cleanup; new Spring runtime invalidates prior consent and authority. | No physical machine power-loss/disk durability experiment or independently killed JVM restore drill was performed. |
| R5 — Embedded child B | Explicit dependency graph revokes ancestors; transitive added child invalidates old grant and exact closure proof denies omissions. | Obsidian transclusion parsing/isolation and external files are disabled/deferred. |
| R6 — Session expiry + queued judge | Independent issued sessions and lease deadlines reject late stage/finalization, including post-check physical COMMIT and restart replay. | No ad-hoc parser source or judge queue/history exists. |
| R7 — External post-check replacement | Unsupported live-file capability refuses approval/dispatch/publication, including restored external dependency authority. Native primitive tests preserve helper cleanup. | Full ancestor/root/reparse/hard-link/mapping/independent Windows-process finalizer-COMMIT proof is missing; feature remains disabled. |
| R8 — Field-policy reduction | Actual cached consent reduction/epoch checks; old attempt is purged/denied before dispatch/publication; selected-only payload and typed new grant require renewed approval. | Description vectors/prompts/recommendation counts are future products, not test substitutes. |

## Runtime and static-analysis limits

DB-trigger/lease and owner-source tests observe real PostgreSQL locks and physical transactions, not sleep-only ordering. The independent SQL-role transaction fixture invokes the same production worker function as Psycopg; it is not represented as an independently paused native Python interpreter. Actual Python HTTP and physical staging integration are separately covered. Timeouts, subprocess cleanup and safe generic errors are explicit.

IDE native-SQL inspection reports unresolved `rag` symbols because no IDE datasource is configured; real migrations/integration queries provide runtime validation. Optional nullability/style suggestions are triaged rather than disabled. Java compiler/Testcontainers/FFM/Lombok/Mockito and Python Starlette test-client deprecation warnings are recorded, not described as warning-free. Frozen authentication implementation/tests are preserved; any intermittent boundary failure is reported in the exact verification record.

Native Windows firewall enforcement, full-system crash durability and saturation/resource guarantees beyond the measured foundation baseline remain unverified. No hidden content-bearing backup, persistent source scratch or model cache was added. Source data is never put in general logs. Reproducible dependency installation and advisory checks are verification-time network operations; runtime isolation is separately measured.

## Review handback

The independent final re-review 3 is complete: **READY FOR OWNER COMMIT**. All RAG foundation findings FR17-1 through FR17-11 and Alternative B findings AB-1 through AB-6 are closed for bounded Foundation scope. AUTH-CLOCK remains OPEN under its owner-approved narrow acceptance exception. Default containment and disabled live external-file capability remain active. The candidate is ready for owner commit. Phase 17 closeout and freezing are reserved for post-owner-commit synchronization.
