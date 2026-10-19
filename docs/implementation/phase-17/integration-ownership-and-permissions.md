# P17-1 — Alternative B ownership, integration and database permissions

> **Prospective applicability pointer, 2026-10-18:** [ADR-0022](../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) alone controls the explicitly named external-only exceptions for new Phase 18 RAG-owned whole-file snapshots: content scope, protected capture then immutable-store lifetime, SNAPSHOT_CURRENT/original UNVERIFIED, guarded NO_CHANGE/C_N and snapshot-only deny incident C_D. This is not a retrospective rewrite of accepted Phase 17 or a change to Vault/domain privacy, field, description, reset or unaffected reservation/process/SQL/E1 guarantees. The original contract/evidence below is preserved; broad external/live/field clauses apply to S only as replaced in ADR-0022. Live capability stays DISABLED / DEFERRED; snapshots inactive; implementation STOPPED.

**Status:** Accepted contract, amended by explicit owner approval on 2026-10-15.
[ADR-0020](../../adr/0020-phase-17-single-job-reservations-e1.md) amends ADR-0019;
[independent review §8](reviews/2026-10-15-phase-17-architecture-simplification-independent-design-review.md#8-exact-owner-approvals-and-canonical-amendments)
is the synchronization checklist. This records required behavior, not implemented
guarantees. Current code stays contained; separate implementation authorization
is required under the existing CHANGES_REQUESTED handoff. FR17-5/8/9/10 OPEN.

## 1. Bounded public integration

One Java `rag` leaf depends only on public `vault`, `fiction`, `film` contracts.
No reverse owner -> RAG feature dependency, internal entity/repository access,
generic JDBC export or reused portability exception. Existing `rag/api` and
`rag/source` are the candidate named interfaces; do not add speculative packages.
Owning Fiction/Film projections contain only P17-2 fields and fresh type/NSFW/
trash/version proof. All consumption statuses are eligible subject to consent.

Vault owns a stable retained source_state row per source ID, independent of
vault_entries deletion. It holds immutable source type, monotonic source_version,
consent_reset_version, deleted state and nullable reserved_by token. Source
creation seeds it; missing/overflow/reused identity denies. No cascade removes
history. The canonical owner enforcement path advances versions atomically with
the business write; reservation-only changes cannot overwrite version history.

Extend the existing vault.entry public boundary with narrow immutable DTOs:

~~~java
void SourceEditGuard.requireEditable(SortedSet<Long> ids);
void SourceIndexReservations.reserve(SortedSet<Long> ids, UUID reservation);
SourceVersions SourceIndexReservations.lockReserved(SortedSet<Long> ids, UUID reservation);
void SourceIndexReservations.release(SortedSet<Long> ids, UUID reservation);
~~~

These are contract-level operations, not claims that classes already exist.
reserve/lockReserved/release require MANDATORY participation in the caller's
same writable physical PostgreSQL transaction/connection; no asynchronous,
autocommit or REQUIRES_NEW release. RAG alone is the maintained reserve/release
caller. Vault validates the exact token/set; RAG proves process/writer safety.
Vault does not accept a worker-supplied quiescent flag or control Python.

Every relevant owner mutation uses this common source guard before managed
mutation/flush, then existing Vault lifecycle/owner row guards. Mandatory
owner-local SQL trigger backstops cover indirect/bulk/import/cascade writes,
RAG policy/dependency paths and OLD/NEW affected source IDs. A trigger already
holding a target row acquires an earlier guard with NOWAIT, never backwards
blocking or SKIP LOCKED. Runtime roles cannot disable triggers or use unsupported
TRUNCATE/DDL. Unprovable affected sets fail closed. See P17-3 for complete order.

Initial enabled-content target remains approved Fiction/Film only. Individually
approved external documents remain in the functional plan, but live-file
capability stays disabled until separate R7 proof. DB reservations cannot fence
external editors. No expanded module/field allowlist or later parsing is granted.

## 2. One logical owner per durable responsibility

| Concern | Logical owner | Worker rights |
| --- | --- | --- |
| Business content/type/NSFW/trash/deletion | Existing owning module | None; safe Java projections only |
| Stable source/version/reset/reservation metadata | Vault, via common public guard and owner-local SQL backstop | None, including no Vault definer-chain access |
| RAG classification, standing consent, epoch, field policy, description confirmation, dependencies | Java RAG, checked against owner truth | No policy rights |
| Single job admission, controller/worker identity, stop/recovery, one-use issued grant | Java RAG | Only own audited grant-claim marker, not canonical job mutation |
| Source transfer and input scope | Java RAG under committed whole-closure reservation | Only exact job-bound input; no arbitrary SQL/path refetch |
| Raw-free stage/receipt computation | Python, disposable RAG staging | Only audited scoped stage route, attempt-row fence and provenance |
| Verification/publication, terminal state and atomic reservation release | Java RAG | No publish, release or active-pointer rights |
| OUTDATED/revoked read denial, scoped purge and verified cleanup | Java RAG using fresh owner state | No self-certification of cleanup |
| Later QA/history/judging/evaluation | Allocated to later handoffs; synthetic lifecycle fixtures only here | No later product authorization |

RAG owns one job-scoped loopback FastAPI process/tree adapter, including launch,
job-bound readiness/endpoint identity, stop/wait and crash recovery. No generic
daemon fallback, Vault DRAIN, global F/V coordinator or second job engine.

## 3. PostgreSQL least privilege and attempt enforcement

Migration authority remains separate Spring/Flyway migrator; Java runtime is
non-owner, Python rag_worker non-superuser/non-schema-owner/NOBYPASSRLS without
inherited app/migrator authority. No runtime DDL, role management, business/auth
SQL or public CREATE/TEMP escalation. Use qualified names, fixed search_path,
audited SECURITY DEFINER ownership, restricted views/functions and default-deny
PUBLIC/effective privileges. Separate DSNs; never log credentials/owner JWTs.

Owner-local triggers may call the narrow Vault metadata guard; this is an
explicit owner-to-Vault SQL dependency, not worker access or a Java module cycle.
Worker can atomically consume only its own issued grant marker and stage bounded
typed raw-free receipts. Every supported/legacy stage route locks and freshly
checks the Java-owned attempt after waits, including scope, staging_closed,
stop/runtime execution identity and observed expiry. It holds the attempt through
S physical COMMIT. No direct unfenced table DML or arbitrary-job access.

Finish obtains the same attempt fence after source guards, drains old writers,
seals staging and physically commits terminal/P-or-abort/release atomically.
Execution expiry uses approved E1, not a claimed physical-instant constraint.
Worker expiry/service boot is never the authority for a completed CURRENT index.

Actual restricted-role tests must prove allowed staging and all forbidden owner,
policy, job, pointer, reservation, DDL, role, function/legacy and inherited PUBLIC
routes. ApplicationModules.verify must verify actual public edges. Abstract
design or admin-credential tests do not establish runtime privilege correctness.

## 4. Canonical synchronization and implementation gate

The approved additive schema contract is [Phase 17 DBML](../../database/phase-17-alternative-b.dbml);
Schema v1 and V1–V13 remain immutable. No migration executes in this synchronization.
Append-only future migrations after V13, public DTOs, module/data/API/package
inventories and affected diagram semantics must match this contract. Existing
topology edges are unchanged. Preserve frozen phases, prior reviews and evidence.
Unknown source/process/SQL outcomes remain denied and reserved, not timer-released.
Actual Windows/PG/offline/resource/full-suite proof remains an acceptance blocker.
