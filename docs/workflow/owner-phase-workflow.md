# Owner Phase Workflow — current (2026-10-27)

**Authority:** [Current Owner Delivery Policy](current-owner-delivery-policy.md). This process applies **only to future Owner-authorized work**, not to frozen Phase 17/closed Phase 18. **Phase 19 S19-1 ONLY is now Owner-approved** for a Codex handoff ([binding authorization](../implementation/phase-19/owner-authorization-s19-1-2026-10-27.md)); accepted preparation was Owner-published at `17f66bb2` and [ACTIVE](../implementation/handoffs/ACTIVE.md) is still `NO_ACTIVE_HANDOFF`. S19-2+ and indexing activation remain unauthorized. Earlier Phase 15 audit-first steps, Backend 3/6/9/12 milestones and two-remediation-round instructions are historical, not present-day execution permission.

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
- Only the [explicit Owner authorization for P19-S19-1](../implementation/phase-19/owner-authorization-s19-1-2026-10-27.md) grants that first-slice scope. Do not infer permission for S19-2+, Frontend or external indexing from a roadmap, historical handoff or closed slice.

## Completed maintenance and docs synchronization

- Maintenance B1/B2 and the overall B aggregate were independently reviewed and Owner-published, most recently at [`3d07c7f`](https://github.com/vhvkhangg/personal-private-vault/commit/3d07c7f15daf290f8e23d035a214716084f45ee9). Their published runner/hooks and exact native-client limitations remain as accepted; historical 3/3 remediation histories remain unchanged.
- Maintenance B3 workflow/resume/Docs Sync mechanism A was [independently accepted](../implementation/maintenance/2026-10-27-b3-workflow-resumability/reviews/2026-10-27-b3-independent-final-review.md) and Owner-published at [`3a1e5fc`](https://github.com/vhvkhangg/personal-private-vault/commit/3a1e5fc7eb14fbabb24a97b5cc27656d0f95e399). B3-L01 is independently VERIFIED/CLOSED in the [cleanup B review](../implementation/maintenance/2026-10-27-repository-doc-sync/reviews/2026-10-27-cleanup-b-independent-final-review.md); other prior LOW backlog remains open.
- The Owner has separately authorized repository-wide **documentation cleanup B**. Its [current docket](../implementation/maintenance/2026-10-27-repository-doc-sync/README.md) is **READY_FOR_OWNER_COMMIT**, with publication/brief cleanup closeout next; it does not create an implementation handoff or activate Phase 19.
- [ACTIVE](../implementation/handoffs/ACTIVE.md) remains `NO_ACTIVE_HANDOFF`. Owner alone publishes subsequent reviewed changes. Do not rerun full Java/RAG suites for purely documentary edits without a demonstrated source risk or alter retained logs/history.

See [handoff state machine](../implementation/handoffs/README.md), [documentation templates](../templates/README.md), [current maintenance status](../implementation/maintenance/2026-10-26-watchdog-tooling/README.md) and [current Owner policy](current-owner-delivery-policy.md).
