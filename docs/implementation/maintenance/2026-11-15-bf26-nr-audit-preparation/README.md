# BF26-NR-AUDIT-PREP — Comprehensive Current Non-RAG Harness, Docs & Backend Audit Preparation

- **Date:** 2026-11-15 (Asia/Ho_Chi_Minh).
- **Package state:** `OWNER_DIRECTED / PREPARATION_DRAFT / INDEPENDENT_REVIEW_PENDING / NOT_OWNER_PUBLISHED`. A first independent review returned `CHANGES_REQUESTED` for `NRP-PH-01–03`; this author-only corrective overlay addresses those findings but **does not claim they are independently closed**.
- **Authority:** Owner instruction in PPV3 continuation dated 2026-11-15 authorizes **creating this docs-only preparation package**. It does **not** authorize execution of the audit, source/harness changes, tests, deployment, git publication, or opening a new executable handoff.
- **Source-of-truth starting point:** GitHub `main` at [`4e4e2cb50ac84c86b0df93ad67f770648c7d32db`](https://github.com/vhvkhangg/personal-private-vault/commit/4e4e2cb50ac84c86b0df93ad67f770648c7d32db), verified remotely when this package was composed. Owner reports local HEAD equals this commit; **local Git/working-tree cleanliness was not independently observed**.
- **Execution register at starting point:** `docs/implementation/handoffs/ACTIVE.md` = `NO_ACTIVE_HANDOFF`. Last published slice `BF26-V1-RECYCLE-BROWSE` = `INDEPENDENTLY_ACCEPTED / OWNER_PUBLISHED / CLOSED`.
- **Success for this preparation only:** independently reviewed, Owner-published, contradiction-free audit plan and explicit future audit authority gate. Not a finding report or a final-audit result.

## Purpose and order

Design a full **current Non-RAG audit** of (1) the harness (`.agents/`, `.codex/`, every `AGENTS.md`, adjacent scripts/workflow), (2) the complete `docs/` tree, architectural/product decisions and repository documentation, (3) every in-scope Non-RAG Backend source, test, build, configuration and integration boundary, followed by (4) evidence-driven cross-layer reconciliation. This supersedes the narrower *proposed next-slice scope* from PPV3 **only for planning**; it does not amend any historical accepted implementation or grant execution authority.

The audit will use **Codex-only sessions** with separate roles: read-only audit, bounded remediation implementation, independent review. Antigravity is **not used in this audit**. Do not create another backend-audit skill: after an independently reviewed H0 and separately authorized H1, **rewrite the existing** `.agents/skills/codex-backend-audit/SKILL.md` as the current `current-non-rag` audit. The Owner's newer 2026-11-15 decision expressly replaces the old requirement to retain runnable Phase 15 skill behavior; **Phase 15 historical records remain immutable**. Include dependent skill/rule/workflow updates in an authorized H1 patch. Inspect the alleged `delivery/` auto-ZIP pipeline first (not present in the supplied repository ZIP; local behavior `NOT_VERIFIED`).

### Documents in this package

| File | Canonical responsibility |
|---|---|
| `owner-contract.md` | Owner direction, authorized preparation boundary, exclusions, invariant decisions |
| `baseline-and-scope-inventory.md` | Remote evidence, scope/coverage units, baseline qualification and historical exclusions |
| `audit-coverage-matrix.md` | Reviewable, executable-in-the-future requirement-to-evidence-to-gate matrix |
| `codex-only-workflow.md` | Session separation, staged execution, authority and STOP routing |
| `harness-audit-plan.md` | Inventory/risk/scenario-based harness checks, legacy skill rewrite, `delivery/` investigation and skill simplification criteria |
| `docs-design-audit-plan.md` | All-docs review, architecture/product simplification and provenance policy |
| `backend-audit-plan.md` | All Non-RAG source/tests/DB/API/integrations/ops validation plan |
| `findings-and-remediation-contract.md` | Findings register, root-cause grouping, all-severity disposition and review batches |
| `acceptance-and-reconciliation.md` | Finite preparation gates, future audit completion gates, cross-layer reconciliation |
| `owner-decision-docket.md` | Explicit 2026-11-15 superseding skill decision, delivery investigation, conditional future approvals |
| `independent-preparation-review-brief.md` | Independent review challenge rubric; not a fabricated review |
| `reviewer-prompt.md` | Copy/paste independent Codex/Claude challenge prompt, preparation only |
| `external-reference-register.md` | Current public guidance and clear distinction from repository evidence |

## Read order

1. `owner-contract.md`, then `baseline-and-scope-inventory.md`.
2. `audit-coverage-matrix.md` and the three area plans.
3. `codex-only-workflow.md`, `findings-and-remediation-contract.md`, `acceptance-and-reconciliation.md`.
4. `owner-decision-docket.md`, `independent-preparation-review-brief.md`, then `reviewer-prompt.md`.

**No new executable permission is granted by an old `CURRENT` banner, this package, a proposed fix, a missing ACTIVE handoff, or an independently reviewed preparation alone.** Require later Owner publication and an exact, new audit-execution authorization. The future audit contract may not silently change historical counters, frozen contracts or accepted scope.

## Historical authorities and current boundaries

- [Owner final non-RAG audit intent (2026-11-01)](../2026-11-01-final-non-rag-audit-intent/owner-intent.md): all substantiated actionable findings, **including LOW/INFO**, must receive evidence-backed final disposition; frozen decisions require Owner approval. Audit includes governance and docs, RAG functional scope excluded.
- [Backend-first Owner decision (2026-11-11)](../2026-11-11-backend-first-assessment/owner-decision-backend-first-2026-11-11.md): H1-F remains paused, FR-13 open/upstream-blocked; no automatic frontend grant.
- [Dashboard BF26-S5 Option A](../2026-11-11-dashboard-api-readiness-preparation/README.md): docs-only API readiness; not a Dashboard implementation/E2E acceptance.
- [BF26-V1 actual-SHA closeout](../2026-11-15-bf26-v1-recycle-browse-preparation/owner-publication-closeout-bf26-v1-2026-11-15.md): PRIVATE-only trash GET identity fields and V1 backlogs preserved.
- [ACTIVE](../../handoffs/ACTIVE.md), [Owner delivery policy](../../../workflow/current-owner-delivery-policy.md), [Roadmap](../../../roadmap.md), root `AGENTS.md` and the existing `codex-backend-audit` skill.

## Next action

**Owner:** apply this **changed-files-only PH-01/02/03 corrective overlay** over the already-applied draft preparation directory (seven existing Markdown files); give the separate historical archive-evidence bundle to the re-reviewer and verify `git status` and the actual SHA. See `acceptance-and-reconciliation.md` §A.1. **The evidence bundle is not part of the repository overlay.** **Separate independent Codex preparation-review session:** challenge the entire package against the latest local source and published Owner authority, record `READY_FOR_OWNER_COMMIT` or `CHANGES_REQUESTED`. **Owner alone:** stage/commit/push after a successful review. **STOP.** Audit launch, harness edits, production implementation, tests and provider activation remain unauthorized until a *separate* Owner execution decision on the reviewed, published audit contract.
