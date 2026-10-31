---
name: antigravity-self-review
description: Lightweight implementer self-check before independent Codex review, scoped to Owner-approved behavior, reuse, cleanliness, architecture, and focused test evidence.
---

# Antigravity Self-Review — before Codex

**Invoked automatically by** `/antigravity-implement-handoff` after implementation/testing and before independent Codex *slice* review. Not a remediation round and not independent acceptance. Owner's forward policy requires Critical/High/Medium blockers resolved, Low/Info listed once for milestone/audit; no speculative extra feature. After all slices, Codex performs one **phase final** review. See [current policy](../../../docs/workflow/current-owner-delivery-policy.md).


This is an implementer QA checklist, not independent acceptance and not permission for extra features.

1. Re-read exact Owner requirement and current executable handoff. For each requirement, point to an actual implemented behavior and executable test or mark NOT_VERIFIED.
2. Verify no unrequested features, generic platforms, extra schema/dependencies or speculative security mechanisms. Choose existing/reused code and the simplest design that preserves every approved behavior.
3. Inspect own diff for clean code/architecture: readable methods/names, cohesion, minimal interfaces, DRY, KISS, YAGNI, SOLID where useful, Spring Modulith ownership and correct Java/Python data boundaries; no unnecessary patterns.
4. Verify all changed-source paths and their callers; focused positive, negative, cancelled/error/cleanup cases at actual changed trust boundaries. Check true results/HTTP response, not only DTO or mocks where DB/native behavior matters.
5. Verify no secret/private payload logging, unsupported `.md` paths, frozen baseline modification, stale docs, broken links, obsolete files, whitespace problems.
6. Diagnose failing tests promptly; never run a failing full suite again unchanged for luck. Preserve receipt and honest status.
7. **Pre-handback state-consistency check:** inspect the actual working-tree `docs/implementation/handoffs/ACTIVE.md` **top-level** `- **Status:**`, its current banner, implementation-result section and `Next step` against the current executable handoff, handback and remediation ledger. Verify the slice ID, actual submitted/remaining remediation count, pending independent-review disposition, takeover status and truthful PASS/FAIL/NOT_VERIFIED/model-candidate results. Flag any contradictory **current** field (including a stale `CHANGES_REQUESTED` header) as a required handback correction; preserved dated independent-review history is not a conflict. Do not mark implementer-addressed findings independently CLOSED.
8. **Transition ownership:** this self-review runs **before** the parent skill persists the final `IMPLEMENTED_AWAITING_CODEX_REVIEW` transition. A still-blocked canonical status during this preflight is not itself grounds to falsify readiness: explicitly tell the parent which top-level status/banner/sections need updating. The parent `/antigravity-implement-handoff` **must save and re-read** the canonical status and compare all three files **after** self-review, before routing to Codex. Self-review must not assert that the post-write gate passed unless the saved state was actually inspected; a docs-only repair is not another remediation round.
9. Summarize to Codex in <=12 short lines plus links to concise evidence: feature matrix, changed paths, focused tests, risks/NOT_VERIFIED and any pending state correction. Do not dump raw command output into chat; preserve logs off-chat.
10. Stop for unresolved Owner decisions. No staging, commit, push, release or self-approval.

## Documentation synchronization checkpoint — Maintenance B3 amendment

Follow the shared [Documentation synchronization contract](../../../docs/workflow/documentation-synchronization.md) **at this skill's handoff/review boundary**, within Owner-approved scope and without a repository-wide cleanup. Self-review must inspect the parent implementation/test changes against affected **current** docs and summarize `UPDATED / ALREADY_CONSISTENT / DEFERRED_OUT_OF_SCOPE / BLOCKED_OWNER_DECISION / NOT_VERIFIED`. Report evidence and any out-of-scope correction to the parent; self-review cannot accept its own work. Record the concise **Docs Sync Check** in the existing handback/review artifact (or identify why no docs are affected). Keep the **Next step** routed to the exact remaining documentation action when necessary.

## Forward next action and interruption recovery — Maintenance B3

At the end of **every completed, blocked or interrupted invocation**, output one concrete line with all fields:

`Next step: <application/person> | <exact skill command or Owner action> | <scope + canonical artifact> | <gate/prerequisite> | <remediation or takeover counter, if applicable>`

**Route for this skill:** Report to the invoking Antigravity implementation/test skill. Self-review is not independent acceptance. Do not route directly to Owner commit; the parent must finish its handback before separate Codex `$codex-final-review`.

**Interrupted session:** the Owner may paste the English [resume prompt](../../../docs/workflow/resume-prompts/antigravity-self-review.md). Resume only from inspected files/evidence, with the current approved scope and existing counter; capacity/tool errors do not authorize a new round or reset evidence. Do not overwrite logs or bypass native safety hooks. A standalone skill command (without a special prompt) remains sufficient for an uninterrupted run.
