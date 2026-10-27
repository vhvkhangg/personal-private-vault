# Owner Phase Workflow — current (2026-10-27)

**Authority:** [Current Owner Delivery Policy](current-owner-delivery-policy.md). This process applies **only to future Owner-authorized work**, not to frozen Phase 17/closed Phase 18. Phase 19 **NOT AUTHORIZED**. Earlier Phase 15 audit-first steps, Backend 3/6/9/12 milestones and two-remediation-round instructions are historical, not present-day execution permission.

## Before starting a phase or maintenance slice

1. **Owner decides scope and exclusions** with ChatGPT; resolve material feature, privacy, existing-guarantee and simplest viable approach decisions before implementation.
2. ChatGPT edits canonical docs/templates directly and supplies only new/modified files with clear attribution. No closeout shell patch scripts; divide work into small separately committable slices.
3. Independent Codex `$codex-pre-handoff-review` for the approved phase preparation or maintenance scope checks need, reuse, risk and proportional tests; status `READY_FOR_HANDOFF` means preparation readiness, not automatic authorization to invent functionality.
4. Codex `$codex-create-handoff` for **one approved implementation slice**, with positive/negative acceptance examples, exact targets/non-goals, retained invariants and focused verification. No unresolved material Owner decision or stale phase permission.

## Each small slice

1. Antigravity runs `/antigravity-implement-handoff` and its mandatory self-review; self-review does not count as independent Codex review or as a remediation round.
2. Codex `$codex-final-review` in slice mode. Unresolved CRITICAL/HIGH/MEDIUM findings and mandatory unverified acceptance cases block `READY_FOR_OWNER_COMMIT`; LOW/INFO stay in a consolidated milestone/audit backlog.
3. If blocked, allow at most **three Antigravity correction + Codex independent re-review pairs per slice** (initial review excluded). Owner-authorized bounded Codex takeover is exceptional and needs a **separate independent review**. Do not reset the exhausted counter or have the implementer self-accept.
4. After acceptance, **Owner** does ordinary `git add`, `git commit` and `git push`; no separate staged-tree review session. ChatGPT records a brief slice closeout citing the actual published commit and reviewer acceptance.

## Completed phase and later review gates

- After all approved phase slices are independently reviewed and Owner-published, run one **Codex phase final review**, reusing unchanged applicable evidence. ChatGPT then records phase Owner closeout.
- Run the **three-phase RAG milestone review** after Phase 19 (P17–19) and after Phase 22 (P20–22), only when the closing phase is completed/authorized. Consolidate justified LOW/INFO cleanups.
- After RAG P17–22, conduct the separately authorized RAG-wide audit. After RAG and Backend are both complete, conduct the separately authorized combined RAG+Backend integration/repository-wide audit. Reuse valid earlier evidence instead of duplicating gates. Historical Phase 15 Backend audit remains closed.
- Do not infer authorization for Phase 19/Frontend or external indexing from a roadmap, historical handoff or closed slice.

## Maintenance B publication-to-final-review state

- [B1](../implementation/maintenance/2026-10-26-watchdog-tooling/b1-owner-closeout-2026-10-26.md) and [B2](../implementation/maintenance/2026-10-26-watchdog-tooling/b2-owner-closeout-2026-10-26.md) have each been independently accepted and **Owner-published**. The 300s normal / approved 2100s extended B1 watchdog and the supported Windows B2 client-hook behavior are published, subject to the exact reviewer limitations.
- [ACTIVE](../implementation/handoffs/ACTIVE.md) is `NO_ACTIVE_HANDOFF` after B2 publication. **Maintenance B final aggregation is READY_FOR_OWNER_COMMIT**, [independent review, 2026-10-27](../implementation/maintenance/2026-10-26-watchdog-tooling/reviews/2026-10-27-maintenance-b-independent-final-review.md), reusing accepted B1/B2 evidence and verifying source/integration consistency. Next: Owner publishes the remaining reviewed documentation, then records the brief overall maintenance closeout. B3 and Phase 19 are not authorized by this step.
- Only Owner publishes any subsequent closeout/review docs. Preserve all existing logs and dated historical findings. Do not run a full Java/RAG suite for docs-only closeout without a demonstrated relevant source risk.

See [handoff state machine](../implementation/handoffs/README.md), [documentation templates](../templates/README.md), [current maintenance status](../implementation/maintenance/2026-10-26-watchdog-tooling/README.md) and [current Owner policy](current-owner-delivery-policy.md).
