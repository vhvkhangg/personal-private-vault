# Owner decision — remediate all four new CSA26 findings — 2026-11-01

**Status:** `OWNER_APPROVED / MAINTENANCE_PREPARATION_REVIEW_PENDING / NO_ACTIVE_HANDOFF`.
**Baseline:** `vhvkhangg/personal-private-vault`, `main` at audit HEAD `51240841c91b0a2b94f665d7d66c432d36c063c9`. Verify actual local HEAD, working tree and remote rather than assuming this SHA remains current after later Owner publication.
**Canonical implementation scope:** [remediation-scope.md](remediation-scope.md).
**Evidence:** [current-system audit](audit-report.md) and [architecture diagram gap inventory](architecture-diagram-gap-inventory.md).

## Owner selection

The Owner explicitly selects **all four newly reported findings** from the 2026-11-01 read-only audit for one **bounded combined maintenance correction**:

- `CSA26-001` — MEDIUM: ordinary Java/Python test discovery can enter suspended S19-2 native/evaluation code, including historical fixed-name output writers.
- `CSA26-002` — LOW: current entry-point documentation/navigation has stale contradictory status and obsolete QA/testing instructions.
- `CSA26-003` — LOW: some current architecture sources/exports display historical synthetic-only worker/container/candidate labels instead of actual accepted P18/S19-1 topology.
- `CSA26-004` — LOW: four reproducible Ruff I001 import-order violations in specifically identified test files.

This is **selection and authorization**, not a finding closure, implementation completion or a declaration that existing tests are green. Fixing LOW findings together with the MEDIUM is a one-time choice, not a new rule that every historical LOW must block future work.

## Absolute scope and acceptance boundary

The maintenance implementation may alter only the minimum test-discovery / non-generative test guards, current documentation/navigation, canonical current-state architecture views plus synchronized exports, and import ordering necessary to close `CSA26-001..004` with evidence. Any source edits directly related to test isolation must be **default-off** and must not turn archived QA into an accepted or executable product. Changes to accepted Java production behavior, authentication, data schemas/migrations, frozen architecture semantics, external connectors or RAG model/runtime are **not approved**.

**S19-2 remains `SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY`; `NO_ACTIVE_HANDOFF` is unchanged until Codex creates a separately reviewed maintenance handoff.** Its historical F01–F05/F08/F09 blockers, C1 `REJECTED`, C2 `NOT_QUALIFIED`, mandatory M10 FAIL, unverified root cause, exhausted evaluation/restart/remediation counters and preserved receipts remain unaffected. Do not execute native QA, load C1/C2 or start a 24-slot evaluation. This decision does not reauthorize Phase 19 S19-2, later RAG phases, general indexing, or frontend implementation.

The **ten older LOW backlog IDs and `AUTH-CLOCK`** listed in audit-report.md are distinct from the four new CSA findings. This decision does not automatically remediate, reclassify or close them; handle separately only if the Owner explicitly expands scope. Diagrams for *future* Ideaverse, Google Drive, agent features or frontend are not part of this maintenance. Automated social-media downloading, fiction copying/backup and Hermes/OpenClaw are explicitly dropped from the product roadmap per Owner decision.

### Historic/frozen content

Preserve P17 FROZEN, P18 CLOSED, S19-1 ACCEPTED/PUBLISHED, all dated reviews, incident logs, evidence hashes and frozen v1 design meanings. The Owner authorizes correcting **current runtime representations** in `structurizr/workspace.dsl`, current DOT/draw.io views and their derived SVG/PNG exports only as required by CSA26-003; do not rewrite frozen v1 baselines as if their original dated statements were false. Source/ADR and existing DBML/Flyway remain authoritative. Architecture-change impact must be reviewed before modifications; any actual frozen contract/schema/module-boundary change requires a *new explicit Owner decision*.

## Required workflow / current gate

1. Run **`$codex-pre-handoff-review`** in **maintenance mode**, reviewing this Owner decision, [remediation-scope.md](remediation-scope.md), the read-only audit, diagram inventory, applicable existing skills and real source. Do not invoke historical `$codex-backend-audit` or reopen Phase 15. The review may refine the scope and record a review, but **may not repair production code or activate a handoff**. Record `READY_FOR_HANDOFF` only if this finite scope is implementable without unresolved frozen-contract issues; otherwise state specific `CHANGES_REQUESTED` or `OWNER_DECISION_REQUIRED`.
2. After `READY_FOR_HANDOFF`, the Owner commits/pushes the independently reviewed preparation documents; only then run **`$codex-create-handoff`** for the approved **maintenance** slice. `ACTIVE.md` stays `NO_ACTIVE_HANDOFF` until the handoff is created.
3. Run Antigravity **`/antigravity-implement-handoff`** for this maintenance handoff, then independent Codex **`$codex-final-review`** in a separate review session. Owner-only staging, commit and push after independent `READY_FOR_OWNER_COMMIT`.

No agent may commit/push. Do not rewrite historical evidence, fabricate unavailable runtime receipts, treat a default-off annotation as QA acceptance or run an unqualified blanket test before default test selection is proved safe. Use bounded runner and unique receipts. Any scope expansion or material frozen-contract change returns to Owner.

**Next step:** Codex | `$codex-pre-handoff-review` | this maintenance-only preparation scope | `NO_ACTIVE_HANDOFF`, all four findings selected, reviewed readiness not yet established | S19-2 historic budgets unchanged.
