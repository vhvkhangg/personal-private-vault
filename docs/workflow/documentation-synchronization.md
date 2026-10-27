# Documentation synchronization contract — prospective workflow gate

**Current publication (2026-10-27):** This mechanism A was independently accepted in the [B3 review](../implementation/maintenance/2026-10-27-b3-workflow-resumability/reviews/2026-10-27-b3-independent-final-review.md) and Owner-published at [`3a1e5fc`](https://github.com/vhvkhangg/personal-private-vault/commit/3a1e5fc7eb14fbabb24a97b5cc27656d0f95e399). The separate documentation-only **cleanup B** is [independently READY_FOR_OWNER_COMMIT](../implementation/maintenance/2026-10-27-repository-doc-sync/reviews/2026-10-27-cleanup-b-independent-final-review.md); its own publication remains pending. The original A-only release boundary below is historical and does not imply permission for code, hooks, logs, frozen contracts, indexing or Phase 19. [Current Owner policy](current-owner-delivery-policy.md) retains precedence.

## Purpose and when to run

Run the lightweight **Docs Sync Check** at an actual workflow boundary: preparation/pre-handoff, handoff creation, implementer self-review/handback, test-only handback, independent slice/final review, authorized audit/milestone review, or Owner-requested documentation closeout. On interrupted-session recovery, re-check only the state changed since the last valid checkpoint. **Do not scan or rewrite all `docs/` on every command.** For a documentation-only task, the check covers its approved files and directly affected current references; out-of-scope historical drift is reported for later disposition, not silently fixed.

## Authority and classification

1. Confirm the Owner-approved *current scope* and [Owner delivery policy](current-owner-delivery-policy.md). Read [`ACTIVE.md`](../implementation/handoffs/ACTIVE.md) for **execution-handoff state**; `NO_ACTIVE_HANDOFF` is legitimate for docs-only reviews and closeouts. Read the approved phase/maintenance decisions, applicable ADRs/frozen contracts, and the latest relevant independent acceptance. A prior approval cannot be inferred from an old heading, proposal, or resume prompt.
2. Check actual local source, tests, diff and review evidence against the published Git baseline. Source/runtime facts must be verified; a future plan is not delivered behavior. The **Owner** alone confirms publication after `git commit`/`push`; an accepted review is not itself an Owner publication.
3. Classify affected documents before editing:
   - **Current operational state:** `ACTIVE.md`, current Owner policy, active scope README/handoff/status and directly affected current indexes/roadmap/setup notes. These may be synchronized *only within approved edit permissions*.
   - **Derived navigation:** links, package tree, diagrams, guidance and references directly affected by the approved change. Update only when evidence warrants it and scope allows.
   - **Immutable history:** accepted reviews, prior Owner decisions, historical ADRs, raw evidence, receipts and `logs/`. Preserve provenance/failed and `NOT_VERIFIED` outcomes; do not rewrite them to look green. A genuinely new decision or superseding current notice is a separate, dated artifact/approved change, not an overwrite.
   - **Proposal/deferred work:** explicitly label as such; no new phase, feature, gate, contract relaxation or remediation budget follows from documentation alone.
4. If sources conflict, **identify the exact paths, dates, authority and impact**. Do not choose the latest-looking `CURRENT` heading over a controlling Owner decision or fabricate a new canonical answer. Escalate material unresolved authority/contract conflicts to the Owner.

## Minimal check and disposition

Use a short **Docs Sync Check** entry in the existing preparation record, handoff, implementation handback, self-review summary or Codex review report (not a new report for each command). At minimum capture:

- **Scope/evidence:** approved slice/maintenance ID, current HEAD/working-tree status, canonical decision/handoff/review path(s), changed paths and relevant current documentation touched or inspected.
- **Consistency:** current vs historical status, approved scope vs actual behavior, next owner/agent action, prior vs pending test/acceptance, links/references and publication/commit claims; check architecture/ADR/API/schema/package tree only when the change actually affects them.
- **Per-issue decision:** `UPDATED` (authorized and performed), `ALREADY_CONSISTENT` (checked), `DEFERRED_OUT_OF_SCOPE` (path + impact + responsible future cleanup), `BLOCKED_OWNER_DECISION`, or `NOT_VERIFIED` (what evidence is missing). A deferral or unknown is **not PASS**.
- **Review outcome:** classify concrete drift by actual impact under the current severity policy. An in-scope material inconsistency or mandatory unverified acceptance blocks the applicable `READY`; cosmetic/historical out-of-scope drift alone does not. Do not convert a speculative preference into a blocking finding. Include the specific next Owner/ChatGPT/Codex/Antigravity action if follow-up is needed.

## Role ownership at workflow boundaries

| Role / skill | Responsibility | Write boundary |
| --- | --- | --- |
| Codex pre-handoff / review-coverage | Identify relevant current docs and divergence; review the simplest adequate plan. | Review artifacts only; route routine docs repair to ChatGPT. |
| Codex create-handoff | Include a bounded *docs impact / owner / expected evidence* list alongside implementation acceptance; note any post-publication closeout. | Only approved handoff/ACTIVE changes; no feature or future-phase authorization. |
| Antigravity implement / test / self-review | Compare accepted behavior/test results against impacted docs and report drift. | Update in-scope implementation-adjacent docs **only if explicitly included in handoff**; otherwise request ChatGPT/Owner correction. Never claim Owner publication. |
| Codex takeover | Treat docs like code: only approved takeover edits; report remaining drift for separate reviewer. | Cannot independently approve its own work. |
| Codex final / milestone / approved audit | Independently validate scoped current-state consistency, URLs/references and evidence; record precise finding/deferral. | Review/status artifacts allowed by mode; not an unapproved repository-wide cleanup. |
| ChatGPT / documentation-writing | Prepare scoped doc fixes, current notices and post-publication closeout when Owner asks, using templates. | Docs-only package after appropriate independent review; Owner alone publishes. |

## Publication and interruption safeguards

- **Before Owner publication:** permitted status is e.g. `READY_FOR_OWNER_COMMIT` when independently accepted; never `OWNER-PUBLISHED` or an invented future commit SHA. Any prospective post-push closeout is a *next step*, then grounded in the actual published commit.
- **After publication:** verify the real commit, update only the approved current status/closeout in a subsequent docs change if needed, then seek the required review per existing Owner policy. No agent stages, commits, pushes, tags or opens/merges PRs.
- **On resume:** inspect modified docs, `git status --short`, `git diff --stat`, approved scope, prior Docs Sync Check and receipts; classify partial writes before continuing. Do not duplicate dated acceptance, reset counters, overwrite `logs/`, bypass hooks or auto-activate a handoff.
- **Explicit B3 boundary:** this addition implements **mechanism A only**. Existing repository-wide stale notices and older contradictions (cleanup B) are not corrected here. Inventory/route them for a separately Owner-authorized, bounded documentation cleanup **after B3 is independently accepted and published**; do not mark those entries fixed as part of B3.
