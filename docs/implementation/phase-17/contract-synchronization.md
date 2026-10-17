# Phase 17 — Alternative B contract synchronization record

**2026-10-15: CONTRACT SYNCHRONIZATION ONLY.** The owner explicitly approved the
four-part package in [independent design review §8](reviews/2026-10-15-phase-17-architecture-simplification-independent-design-review.md#8-exact-owner-approvals-and-canonical-amendments),
recorded as seven decisions in [owner-decisions](owner-decisions.md#2026-10-15--alternative-b-owner-design-approval).
Accepted [ADR-0020](../../adr/0020-phase-17-single-job-reservations-e1.md) amends
ADR-0019 clauses 2–5. Approval is of the contract, not implementation.

Existing P17-RAG-FOUNDATION/ACTIVE remains **CHANGES_REQUESTED**;
FR17-5/8/9/10 **OPEN**, prior closed findings preserved. Containment remains.
No application/test/migration execution/change, new handoff, later-phase work,
commit/push, self-approval or completion/freeze.

## Independent Section 8 checklist reconciliation

| Checklist | Synchronized authoritative record | Disposition |
| --- | --- | --- |
| P17-1 §1–3 and owner decisions | [P17-1](integration-ownership-and-permissions.md), owner record and ADR-0020 | Vault stable state/public operations/owner-local SQL backstop; RAG native worker lifecycle and restricted worker privileges; no reverse Java/worker Vault edge |
| P17-2 §1–2 and source matrix | [P17-2](source-eligibility-and-allowlists.md) and [API inventory](../../architecture/api-architecture.md#16-phase-17-alternative-b-contract-amendment) | Persistent item consent, independent Private veto, expected source/reset/version/epoch/policy CAS, exact description hash combined with manual start; original fields unchanged |
| P17-3 §1/§1A.1–4 | [P17-3](revocation-and-derived-data.md) and [logical DBML](../../database/phase-17-alternative-b.dbml) | Durable whole-closure reservation, all-path busy/NOWAIT guard/backstop, short ordered physical TX, one static nonterminal unique constraint |
| P17-3 §1A.5–7/§2/§3.1–6 | P17-3, ADR-0020 and API inventory | E1(a)/(b)/(c), valid late CURRENT publication, independent index lifetime, verified actual process/task/writer stop, atomic P-or-abort/seal/terminal/release, manual reindex/no resume |
| ADR-0019 clauses 2–5 | [ADR-0019](../../adr/0019-local-rag-before-frontend.md), ADR-0020 and ADR index | Explicit replacement of strict deadline/startup/session coupling, not a claim to satisfy strict E2 |
| Existing handoff and canonical inventories | [ACTIVE](../handoffs/ACTIVE.md), architecture/data/module/API/package/DB/phase inventories | Selected bounded target versus current contained candidate distinguished; topology unchanged, no rendered export changed |
| Tests/evidence | [B01–B20 expectation map](alternative-b-verification-plan.md), [benchmark plan](benchmarks-and-acceptance.md) and [remediation plan](remediation-plan.md) | Exact kept/changed assertions and enabled native/offline/resource/full verification obligations recorded; historical tests/evidence/reviews not edited |

## Approved requirement changes and retained security

E1(a) permits post-check post-deadline transfer/consumption under pause/buffering
with unbounded elapsed residual. E1(b) permits sensitive raw-free untrusted stage
COMMIT after expiry; a new verification gate observing expiry denies. E1(c)
permits actual trusted verification/P physical COMMIT after a valid timely last
gate. A valid late published index may remain CURRENT and usable independently
of completed worker lease, service session and boot. This replaces strict E2;
transaction/deferred/WAL/physical-C/response timing is not conflated. Retain all
fresh observed-expiry and post-wait/default-deferred rejection. Later denial does
not retroactively make a durable transition timely.

No NSFW/Private/reset/provenance/full dependency/freshness/offline guarantee is
waived. Reservation-first relevant edits, including revoke/classification, return
409/no change; owner cancels, awaits verified process/task/SQL safety and retries.
Owner-first exclusion denies input. Never release on TTL/ACK/disconnect, never
take over a live controller, never queue the denied edit. Unknown stays busy;
unrelated items editable. Real endpoint binding prevents stale-listener dispatch.

Ordinary eligible edits retain participation/configuration but permanently
OUTDATE prior source/edge versions. Lost events and hash/edge round trips cannot
revive consent/indexes. Explicit CAS renewal after reset is required; Private is
not changed by approval. Exact unchanged description hash needs no repeated
confirmation; changed bytes can be confirmed with manual Index/Reindex. That
confirmation cannot renew item consent or expand fields/children.

Historical SUCCEEDED jobs remain historical; effective CURRENT/OUTDATED/revoked
index state uses fresh owner consent/version/scope, never work credentials.
Normal restart preserves fresh completed indexes/standing consent. Unfinished
work is aborted/discarded after actual process/task/writer proof, never resumed
or auto-finalized. Safe edit release is distinct from verified physical erasure;
failed erasure stays PENDING/inaccessible. Original live external files stay
disabled pending R7; no later parser/model/query consumer is certified here.

## Exact changed-file inventory for this synchronization

This delta is relative to the 1,941-file pre-session SHA-256 snapshot, not the
large inherited uncommitted candidate. Ignored local writer/validation scripts
and receipts are tooling, not application/test delivery.

| Change | Path |
| --- | --- |
| Modified | [AGENTS.md](../../../AGENTS.md) |
| Modified | [backend/AGENTS.md](../../../backend/AGENTS.md) |
| Modified | [docs/adr/0019-local-rag-before-frontend.md](../../adr/0019-local-rag-before-frontend.md) |
| Modified | [docs/adr/README.md](../../adr/README.md) |
| Modified | [docs/architecture/README.md](../../architecture/README.md) |
| Modified | [docs/architecture/api-architecture.md](../../architecture/api-architecture.md) |
| Modified | [docs/architecture/data-architecture.md](../../architecture/data-architecture.md) |
| Modified | [docs/architecture/module-boundaries.md](../../architecture/module-boundaries.md) |
| Modified | [docs/architecture/module-dependency-matrix.md](../../architecture/module-dependency-matrix.md) |
| Modified | [docs/architecture/rag-architecture.md](../../architecture/rag-architecture.md) |
| Modified | [docs/architecture/structurizr/workspace.dsl](../../architecture/structurizr/workspace.dsl) |
| Modified | [docs/database/README.md](../../database/README.md) |
| Modified | [docs/implementation/handoffs/ACTIVE.md](../handoffs/ACTIVE.md) |
| Modified | [docs/implementation/phase-17/README.md](README.md) |
| Modified | [docs/implementation/phase-17/architecture-simplification-proposal.md](architecture-simplification-proposal.md) |
| Modified | [docs/implementation/phase-17/benchmarks-and-acceptance.md](benchmarks-and-acceptance.md) |
| Modified | [docs/implementation/phase-17/changed-files.md](changed-files.md) |
| Modified | [docs/implementation/phase-17/integration-ownership-and-permissions.md](integration-ownership-and-permissions.md) |
| Modified | [docs/implementation/phase-17/owner-decisions.md](owner-decisions.md) |
| Modified | [docs/implementation/phase-17/p17-3-consumption-fence-proposal.md](p17-3-consumption-fence-proposal.md) |
| Modified | [docs/implementation/phase-17/remediation-plan.md](remediation-plan.md) |
| Modified | [docs/implementation/phase-17/revocation-and-derived-data.md](revocation-and-derived-data.md) |
| Modified | [docs/implementation/phase-17/source-eligibility-and-allowlists.md](source-eligibility-and-allowlists.md) |
| Modified | [docs/implementation/rag-phase-plan.md](../rag-phase-plan.md) |
| Modified | [docs/repository/repository-package-tree.md](../../repository/repository-package-tree.md) |
| Modified | [docs/roadmap.md](../../roadmap.md) |
| Added | [docs/adr/0020-phase-17-single-job-reservations-e1.md](../../adr/0020-phase-17-single-job-reservations-e1.md) |
| Added | [docs/database/phase-17-alternative-b.dbml](../../database/phase-17-alternative-b.dbml) |
| Added | [docs/implementation/phase-17/alternative-b-verification-plan.md](alternative-b-verification-plan.md) |
| Added | [docs/implementation/phase-17/contract-synchronization.md](contract-synchronization.md) |

## Documentation consistency and preservation evidence

Run from repository root:

~~~powershell
.\rag-engine\.venv\Scripts\python.exe logs/phase17-alternative-b-contract-sync-2026-10-15/validate_sync.py
git diff --check
~~~

Final check result: **PASS**, validator exit 0; 26 modified and four added files,
1,941 baseline / 1,945 current nonignored files, 1,915 baseline files unchanged,
all 1,696 protected files unchanged. All **470 local links and 19 heading anchors**
resolved; 20 acceptance rows; `git diff --check` exit 0. Local ignored receipt:
`logs/phase17-alternative-b-contract-sync-2026-10-15/validation.json`.
Validator checks authorized
file-set delta, all pre-existing protected code/test/migration/review/evidence/
frozen-DBML hashes, local Markdown file/heading targets, B01–B20 coverage and
current ACTIVE authorization/status. Manual comparison reconciles each Section 8
row, consent/E1/lifetime distinctions, unchanged dependency direction and no
speculative global control plane. These checks are documentation evidence only;
not runtime/native/PG/offline/benchmark/Modulith acceptance. DBML is a reviewed
logical inventory, not executed SQL or a formal runtime schema proof.

The first document-check invocation failed on a checker-only literal
`never renews`; the contract says `never restores consent`. The checker wording
was corrected after inspection; no contract/security assertion was relaxed.
An initial shell scan used unsupported PowerShell brace syntax and did not run;
the final finite checks and direct file inspection replace that failed scan.
The inventory whitespace check also found a pre-existing trailing space after
an empty `Count:` label; that space was removed without changing inventory data.

## Blockers and next authorization

**No unresolved contract-level approval blocker identified.** The owner has
approved the finite package and these records synchronize it. This is ready for
separate IMPLEMENTATION authorization under the existing handoff, not execution
enablement or acceptance. Native process/SQL writer/trigger coverage, physical
COMMIT, stale approval/description/fresh reads, exact role tests, full suites,
enabled offline and successful desktop resource benchmarks remain NOT_VERIFIED.
Concrete measured cold-start/job/stop/CPU/memory thresholds must be chosen before
implementation acceptance; no invented benchmark values. Unknown recovery remains
fail closed even if that reduces availability. No FR closure or owner commit gate.
