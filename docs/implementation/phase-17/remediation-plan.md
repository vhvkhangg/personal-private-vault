# Phase 17 — approved Alternative B remediation plan

**2026-10-16 implementation continuation: Groups A–D explicitly owner authorized
under the existing handoff.** Owner approval of [independent review §8](reviews/2026-10-15-phase-17-architecture-simplification-independent-design-review.md#8-exact-owner-approvals-and-canonical-amendments)
is recorded in [owner decisions](owner-decisions.md#2026-10-15--alternative-b-owner-design-approval)
and [ADR-0020](../../adr/0020-phase-17-single-job-reservations-e1.md). Existing
P17-RAG-FOUNDATION stays CHANGES_REQUESTED; FR17-5/8/9/10 OPEN. Keep containment.
The previous synchronization-contract approval blocker is resolved at contract
level; this does not resolve implementation/runtime findings.

## Current dependency-aware implementation plan (authorized; verification in progress)

1. Inventory every source/policy/dependency write route. Add retained Vault
   source/version/reset/reservations and mandatory owner-local trigger guard,
   normal guard-before-flush/NOWAIT backstop, canonical version maintenance and
   safe seeding invalidating old unbound approvals. Add exact stale approval CAS
   and description hash command semantics. Focus B01/B03–05/B15/B16.
2. Implement static one-nonterminal-attempt constraint, atomic closure/reservation/
   job/grant admission and one-use body claim. RAG owns one Windows confined
   job-scoped worker, real endpoint binding/controller identity and launcher/sender
   lifecycle. No F/V/DRAIN/native source lock, daemon fallback, queue or live
   takeover. Focus B02/B06/B08–11, actual role and Windows barriers.
3. Implement all-route attempt fence/drain/seal, typed actual producer receipts,
   atomic P-or-abort/seal/terminal/reservation release. Separate E1 execution
   gates from completed-index lifetime; effective source-version OUTDATED/revoked
   reads and historical-manifest cleanup. Focus B07/B09/B12–17; real PG physical C,
   pending backend, unknown outcome and both-order tests, never mocks as proof.
4. Resolve focused failures before progressing. Then clean full Java/Python,
   Ruff check/format, mypy, architecture/migration/role/package/repository checks
   with bounded process cleanup and exact commands/counts/exits. Preserve frozen
   auth assertions and implementation unchanged. Owner Decision (2026-10-17) tracks
   `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary`
   as an OPEN Authentication/environment issue outside Phase 17 RAG remediation under a
   narrow acceptance exception (permitted only when caused by evidenced Windows/WSL2 cross-clock
   skew, with accurate reporting and all other Phase 17 tests passing; failed clean verify is
   never called PASS).
5. FR17-9: successful representative cold/sequential single jobs, bounded caps,
   two-start conflict, source/attempt wait/hold, termination/release/recovery/purge,
   query/CPU/RAM/disk/handle/connection/tombstone growth measurements. Five warmups
   then >=100 sequential measured runs per fixture, all outcomes recorded. Set
   measured supported-desktop thresholds before acceptance; no invented numbers.
6. FR17-10: Practical Local Offline RAG operation per owner decision 2 (2026-10-16):
   actual native enabled Java/Python/PG execution, bound loopback ingress/socket containment,
   zero external/cloud AI endpoints, no required Internet connectivity, pinned local dependencies,
   strict NSFW/Private exclusion, durable reservations, and safe crash recovery/cancellation.
   Windows Firewall / WFP OS-level packet denial requirements removed by explicit owner approval.
7. Synchronize actual implementation/evidence inventories and obtain a NEW
   independent implementation review. No self-closure, owner commit readiness or
   completion/freeze. No new handoff, Phase 18–22 or unrelated frozen changes.

Normative details: [P17-1](integration-ownership-and-permissions.md),
[P17-2](source-eligibility-and-allowlists.md), [P17-3](revocation-and-derived-data.md),
[B01–B20 and explicit expectation changes](alternative-b-verification-plan.md),
[benchmark/full/offline gates](benchmarks-and-acceptance.md),
[logical schema](../../database/phase-17-alternative-b.dbml) and
[API command inventory](../../architecture/api-architecture.md#16-phase-17-alternative-b-contract-amendment).
Append migrations after V13; preserve historical migrations/reviews/evidence.
Implementation now includes V14 source-state/reservations, V15 single-job and
sealed worker routes, V16 current-read/terminal cleanup, and V17 retained consent history.
Fresh Flyway startup and the full suite applied V1–V17.

E1(a)/(b)/(c) and valid late CURRENT P are approved, not strict-E2 proof. All
newly observed expired gates still deny. A completed worker TTL/service session/
boot never substitutes for IndexCurrent or invalidates standing consent alone.
Cancel-first conflicts commit no edit. Unknown process/task/SQL keeps reservation
and job exclusivity; no timeout release. Rollback/disable keeps containment,
positively stops consumers, seals writers, resolves actual C, discards untrusted
partial data and only then safely releases. Retain anti-replay history/tombstones.

## Historical remediation diagnosis and receipts (not current authorization)

The following earlier plan/evidence describes the prior candidate and rejected
proposals. Its old pending-approval, retry/queue or F/H/V/DRAIN directions are
superseded by the current plan above. No historical review or evidence is rewritten.


# Phase 17 owner-authorized Codex implementation remediation

2026-10-14. Existing handoff: `P17-RAG-FOUNDATION`, status `CHANGES_REQUESTED`.
The owner authorizes Codex implementation for this cycle. Independent final review remains required.
Historical reviews are immutable. FR17-11 stays closed.

**Current design-only continuation (2026-10-15):** the owner has not approved a
P17-3 exception. The [explicit contract-delta proposal](p17-3-consumption-fence-proposal.md)
withdraws the acknowledgement-only sketch as an adequate repair: loss of the
Java DB transaction can release locks before Python consumes buffered bytes.
Its stronger candidate requires additional owner/API/schema decisions and native
plus PostgreSQL proofs. Containment, production code and tests stay unchanged.

## Dependency order and verification

1. Source authority and graph consistency (FR17-1/2/3/5/7/9): explicitly disable unproved live external-file capability; conservative foundation text contract; bounded memoized graph snapshot and topology barrier; fresh owner and exact source/field/revision proof before worker input. Focused denial, DAG/depth, changed-child, field/status and input tests.
2. Worker provenance and database isolation (FR17-3/4/5): one typed input/result route, issued short-lived scoped grants, restricted Python PostgreSQL staging, separate runtime/migrator provisioning and effective privilege checks. Focused malformed/replay/provenance/actual-role tests.
3. Publication and lifecycle (FR17-6/7/9): sorted guard protocol and post-wait/COMMIT deadline checks; synchronous denial then committed notification then retryable verified cleanup; attempt idempotency, session/recovery/anti-replay and bounded resources. Focused actual lock-wait, both-order staging/purge, duplicate/outage/recovery tests.
4. Actual boundary proof (FR17-8 and FR17-1–9): deterministic production-boundary interleavings, physical COMMIT, revocation, purge and role restrictions; preserve original R1–R8 identities. No mocked/skipped OS proof presented as success; disabled capability is tested as denial.
5. Environment/evidence (FR17-10 and FR17-8/9): locked environment and exact compatible database/extension, offline/dependency diagnostics and workflow measurements; synchronize current affected inventories; run required complete Java/Python/static/integration checks. Record exact receipts and unresolved guarantees.

Every implementation group requires focused regression convergence before the next group.
Passing tests alone do not close findings. This session does not issue owner commit readiness.

## 2026-10-15 continuation — diagnosis and dependency-aware plan

The existing handoff remains `P17-RAG-FOUNDATION`, `CHANGES_REQUESTED`. The latest
[independent review](reviews/2026-10-15-phase-17-independent-final-codex-review.md)
leaves FR17-5/8/9/10 open. No other finding is reopened by this plan.

### Reproduced root causes and acceptance blocker

The disposable reviewer probe was copied, not edited in place, to ignored
`logs/phase17-remediation-2026-10-15/`. It exited 0 and again produced one actual
Python/restricted-role receipt after a committed Fiction NSFW update, both for a
previously issued grant and the public execution path. Issuance COMMIT releases
the Vault guards. The later worker function checks RAG snapshots, which owner
mutations do not synchronously invalidate. Final publication denial occurs too late.

The same probe observed 32/32 successful rotations at the database-current-time
boundary; expiry minus Java pre-rotation time ranged from 199,968,800 to
410,854,100 ns. This is fresh evidence of the reported two-clock environment
problem, not permission to change frozen authentication or weaken its test.

P17-3 §1A.4 prohibits both network/client sends under source DB guards and owner
transactions acquiring RAG locks. Owner decisions permit a shared transactional
Vault row guard and read-only owner projections, not a new durable owner mutation
invalidation protocol. These facts leave no implemented shared fence between
owning-domain COMMIT and worker consumption. A second pre-HTTP read, a callback
that commits before sending content, an asynchronous listener, a shorter TTL, or
a final publication check leaves the same interval. A JVM-only lock does not
cover independent application processes. Python business-table SQL is forbidden.

**Acceptance blocker:** the full repair requires an explicit amendment of the
existing P17-3/handoff synchronization contract. Do not silently reinterpret the
network prohibition or introduce reverse owner dependencies/triggers. Until that
decision, block grant issuance, Java content dispatch and Python HTTP execution
before reading request bodies. Keep health/cancellation/cleanup available. This
containment is not an implemented replacement, a finding closure, or an enabled
workflow acceptance result. Existing successful-execution tests stay unchanged.

### Revised proposal — acknowledgement alone is insufficient

The earlier minimal exception assumed the source transaction survives until a
consumption acknowledgement. That assumption is not preserved by DB disconnect,
transaction timeout or Java death; cancellation does not retract in-flight bytes.
An independent OS consumption fence also needs a durable staging fence when a
consumer dies but its PostgreSQL COMMIT is still in flight.

The [draft D1–D6 delta](p17-3-consumption-fence-proposal.md) specifies those two
boundaries, rejected alternatives (including staging-only and non-network IPC),
the complete protocol/order, proposed budgets, clock limits, deterministic tests
and rollback. Its candidate preserves the existing prohibition on network sends
under Java source DB locks but needs a broader Vault-owned process fence and
coordination-version API/SQL/schema delta. This is not approved under the original
owner decision or the owner's willingness to consider an exception. No runtime
implementation or weakening of functional requirements is authorized here.

### Ordered implementation and regression groups

1. **FR17-5, prerequisite decision:** amend current P17-3 and ACTIVE (no new
   handoff); then implement the reviewed versioned protocol in
   `RagWorkerGrantService.java`, `RagJobService.java`, `RagEngineClient.java`,
   `RagWorkerGrant.java`, Python `contracts.py`, `transport.py`, `main.py`,
   `worker.py`, `db.py`, and an append-only RAG migration if durable ingress state
   is required. The earlier file list is incomplete for the stronger candidate:
   review D1/D3's Vault API, durable coordination metadata, native adapter,
   one-shot consumer and SQL contract impacts before authorizing implementation.
   Preserve restricted roles and finalizer fences; no owner change is implied.
   Extend `RagWorkerBoundaryIntegrationTest` with an after-issuance-COMMIT barrier
   and before-consumption barriers for Fiction/Film NSFW, trash, revision,
   transitive child, revocation, expiry, stale cache, restart/retry, both orders,
   and caller rollback. Observe real PG blocking and zero unauthorized input/
   receipts. Re-run existing authorization/publication/purge tests before group 2.
2. **FR17-8:** map each counterexample to maintained Java/Python tests and original
   R1–R8 IDs; add ingress handshake tests and bounded process cleanup. Measure and
   stabilize Windows/Docker VM clocks without changing authentication source or
   assertions; if not possible, report the environment blocker. Run focused
   boundary/role/architecture tests, then unchanged full `mvn -ntp clean verify`,
   complete pytest, Ruff check/format, mypy and repository quality checks. Count
   actual XML testcase nodes; retain every failed command and exact exit status.
3. **FR17-9, depends on the real enabled protocol:** add a finite isolated fixture
   under `backend/src/test/.../rag/` and reproducible runner under
   `rag-engine/scripts/`. Benchmark configured 16-pending, 100-selected,
   depth-5/1-MiB limits, explicit over-limit denial, controlled source-lock
   contention, queue delay, cancellation, revoke/purge/recovery and batched
   query counts. Record warmup, sample count/concurrency, guard wait/hold and
   queue-inclusive p50/p95, isolated JVM/worker memory/CPU and scratch/disk;
   compare hard bounds and propose measured performance thresholds for review.
   Do not count disabled execution or the previous sequential sample as this proof.
4. **FR17-10, depends on groups 1–3:** add a supported source-matched local
   Java/Python/PostgreSQL fixture in an isolated network namespace permitting
   their loopback transports only. Run real success, revoked/stale denial,
   cancellation and recovery with empty caches; attempt IPv4/IPv6/DNS/proxy
   egress and record OS enforcement separately from application checks. Preserve
   package/version/license inventory and explicitly state host-firewall limits.
   For the proposed Windows-native source-use fence, a Linux namespace run
   omitting that fence is not equivalent. The enabled native path needs authorized
   Windows egress controls, or a separately reviewed supported equivalent; absent
   controls remain an acceptance blocker rather than a disabled-work PASS.
5. **Evidence/status:** update implementation report, test evidence, changed-file
   inventory, current Phase README and ACTIVE with exact new results; retain
   historical reviews and prior receipts. Request a new independent final review
   only after the four findings' mandatory evidence is available.

Current execution stops at the FR17-5 contract decision after fail-closed
containment and focused containment checks. FR17-8 full clean verification,
FR17-9 enabled saturation measurements and FR17-10 enabled offline proof are
**NOT_VERIFIED in this continuation**. No test is skipped or assertion relaxed to
manufacture an accepted disabled foundation.

The subsequent design-only pass adds no runtime evidence. Accepted P17-3,
historical reviews and every production/test file remain unchanged by that pass.
