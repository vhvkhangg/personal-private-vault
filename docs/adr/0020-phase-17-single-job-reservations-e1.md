# ADR-0020 — Phase 17 single-job source reservations and E1 execution expiry

> **Prospective scoped applicability, 2026-10-18:** [ADR-0022](0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) supersedes only the explicitly listed external-source content-consent, live freshness/lifetime and reserved deny-incident applicability for new Phase 18 RAG-owned whole-file snapshots, adding guarded nonpublication C_N. This ADR remains accepted/frozen for Vault and all unaffected guarantees; historical rationale/status/evidence below is unchanged. No implementation or live capability activation follows.

- **Status:** Accepted by the owner, 2026-10-15; CONTRACT APPROVAL ONLY.
- **Decision owner:** Repository owner, explicit Alternative B approval (seven decisions).
- **Amends:** ADR-0019 clauses 2–5 for the bounded Phase 17 foundation. Does not reopen unrelated frozen phases.
- **Authority:** [Owner decision record](../implementation/phase-17/owner-decisions.md#2026-10-15--alternative-b-owner-design-approval) following [independent design review §8](../implementation/phase-17/reviews/2026-10-15-phase-17-architecture-simplification-independent-design-review.md#8-exact-owner-approvals-and-canonical-amendments).
- **Implementation:** NOT AUTHORIZED by this synchronization. Existing handoff P17-RAG-FOUNDATION remains CHANGES_REQUESTED; containment stays; FR17-5/8/9/10 OPEN.

## Context

The finalized deployment is permanently single-user/single-account, one local
Windows desktop and one active indexing job. The owner permits relevant edits to
conflict while that job holds its fixed source set. Durable reservations can span
connection/process failure without the global F+H+V+E protocol. A JVM mutex or
SQL locks held across worker execution cannot provide that lifetime: Java/DB loss
can release locks while Python still consumes buffered input.

## Decision

1. Select Alternative B: Vault-owned retained source/version/reset/reservation
   metadata; short ordered transactions; RAG-owned single-active-attempt constraint,
   one job-scoped Python/FastAPI process, one-use input grant and sealed staging.
   No F/LockFileEx source fence, global V, Vault DRAIN/process supervisor or v3 G/B
   ticket/control ledgers. Existing rag -> vault/fiction/film public edges remain;
   Vault never calls RAG. Worker receives no business/Vault SQL privileges.
2. Source reservations never expire on work TTL/disconnect/health failure. All
   relevant mutation paths and mandatory owner-local SQL backstops reject busy
   sources atomically with 409. Cancel-first includes NSFW/Private/revoke/field/
   dependency changes: no denied mutation is committed or automatically queued.
   Unrelated items remain editable. Release requires terminal launch/send tasks,
   verified actual worker/tree termination and attempt-writer drain/seal.
3. Explicit per-item consent (1B) survives eligible ordinary edits, but indexes
   become OUTDATED. Eligibility loss/reset permanently invalidates prior consent;
   returning eligible never restores it. Approve/renew compares owner-observed
   reset/version/authorization-epoch/field-policy values under guards. Approval
   cannot change Private configuration. Description uses the exact-hash rule in
   P17-2, optionally confirmed in the manual Index/Reindex action.
4. Approve **E1(a)**: timely read/send/work gates may precede post-deadline transfer/
   consumption under suspension or buffering, with unbounded elapsed residual.
   Approve **E1(b)**: sensitive raw-free untrusted S may durably commit late;
   a new verification gate observing expiry still denies. Approve **E1(c)**:
   timely last guarded validation may precede late trusted VERIFIED/P physical
   COMMIT. This is real publication, not quarantine or a strict-E2 proof.
   Retain post-wait/default-deferred expiry checks. No newly observed expired gate
   passes; no implicit renewal or promotion of an expired candidate.
5. An authorized, CURRENT published index, including valid late P, is usable
   independently of the completed worker execution lease, service session and
   boot identity. Current consent, live eligible source versions, exact fields,
   full dependency state and its own legitimate retention/selection scope remain
   authoritative. Ad-hoc selection-session expiry is a separate artifact scope,
   not the completed worker credential. Historical SUCCEEDED is not CURRENT.
6. Manual reindex only (2B); no automatic resume/resend/retry/reindex (3B).
   Startup reconciles actual DB outcomes and aborts/discards unfinished work only
   after positive process/task and SQL proof. Unknown outcomes keep reservations
   and job exclusivity. Normal restart preserves fresh consent/completed indexes;
   declared restore/uncertain history requires suspension/reconciliation/renewal.
7. Atomic finish physically commits publication or abort, staging seal, terminal
   job state and reservation release together. Purge completion separately requires
   verified erasure. No source/NSFW/Private/consent/provenance/freshness/offline
   requirement is relaxed by E1. Full bounded Foundation functionality remains;
   no parser/model or Phase 18–22 implementation is authorized.

## Canonical consequences and alternatives

[P17-1](../implementation/phase-17/integration-ownership-and-permissions.md),
[P17-2](../implementation/phase-17/source-eligibility-and-allowlists.md) and
[P17-3](../implementation/phase-17/revocation-and-derived-data.md) specify the
binding API/SQL/process contract. [Additive logical schema](../database/phase-17-alternative-b.dbml)
records the target without modifying Schema v1 or executing migrations. Future
implementation must append after V13; current code is not certified by this ADR.

Alternative A (F+H+V+E) adds unnecessary global/native coordination for a permitted
edit-exclusion interval. C (framed stdio) is viable but replaces maintained typed
HTTP without removing reservations/history/process proof. A persistent daemon's
idle ACK cannot prove buffers/threads are terminal. Strict E2 C-before-expiry is
unprovable under arbitrary pauses; the owner explicitly replaces that time rule
with E1 rather than pretending another fence makes physical COMMIT timely.

Costs: selected items can remain busy after failed termination; cold process
startup and tombstone storage need measured budgets. Actual Windows handles,
endpoint binding, SQL triggers/roles/COMMITs, full tests, enabled offline proof
and useful successful benchmarks remain NOT_VERIFIED. Separate IMPLEMENTATION
authorization and later independent acceptance are mandatory. No containment
removal, commit/push, phase completion or owner commit readiness is implied.

## Addendum: 2026-10-16 Practical Local Offline Operation (Narrow Amendment)

On 2026-10-16, the repository owner explicitly approved a narrow contract amendment superseding the previous strict OS-level Windows Filtering Platform (WFP) / elevated firewall enforcement requirement with Practical Local Offline Operation.

The owner's personal vault is permanently single-user, local Windows desktop, with Film/Fiction as the primary indexed domains. Indexing is performed entirely locally without external or cloud AI services.

### 1. Requirements removed by explicit owner approval:
- Windows Firewall outbound-denial rules and elevated WFP 5157 event collection.
- Administrator elevation requirement for network enforcement tests.
- System DNS/Dnscache blocking or global IPv4/IPv6/proxy denial enforcement.
- Windows firewall or DNS audit-policy modifications.
- Mandatory OS-level proof that every possible RAG outbound connection is blocked by host firewall rules.

### 2. Remaining practical-offline requirements:
- Java -> local Python -> PostgreSQL indexing pipeline execution entirely on the local machine.
- No required Internet connection for indexing operations.
- Zero outbound external endpoints configured, required, or accessed by the indexing pipeline.
- No intentional external transmission of source content by the indexing pipeline.
- All Python dependencies pinned, installed locally, and self-contained (`uv.lock`).
- Existing RAG consent, source eligibility, strict NSFW/Private exclusion, and provenance controls remain fully intact.
- Durable reservations, PostgreSQL transaction consistency, native Win32 process supervision, and crash recovery remain valid.
- The approved E1 contract and independent persistent index lifetime remain preserved.

### 3. Tests that verify remaining requirements:
- Python worker bound ingress and socket containment tests (`tests/test_bound_ingress.py`, `tests/test_consumption_containment.py`).
- Java RAG integration tests verifying local loopback communication (`http://127.0.0.1:{port}`), lease token opaque capability verification, and database staging/publication.
- Configuration and code inspection verifying no external API clients, cloud endpoints, or unpinned dependencies exist.

### 4. Guarantees that are no longer claimed:
- Operating-system-wide egress denial enforcement via Windows Filtering Platform or elevated packet-filtering rules.

