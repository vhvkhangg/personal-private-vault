# Owner Phase Workflow — current (2026-10-25)

**Authority:** [Current Owner Delivery Policy](current-owner-delivery-policy.md). The implementation process below applies **only to future Owner-authorized work**, not to already frozen Phase 17/18. Phase 19 **NOT AUTHORIZED**. Earlier operational workflows (including Phase 15 historical audit-first flow, Backend 3/6/9/12 milestones, and the former 2-round policy) remain available through committed Git history and their dated acceptance/review records; they are **not current instructions**.

## Before starting a phase or maintenance slice

1. **Owner decides scope and exclusions** with ChatGPT. For any ambiguity about requested functions, minimal options, privacy or accepted guarantees, ask the Owner **before** writing the plan. Prefer simple existing functions, not new infrastructure.
2. ChatGPT edits canonical docs and templates directly, gives only changed files. No shell patch/closeout scripts. Document the proposed small, independently committable slices.
3. Independent Codex runs `$codex-pre-handoff-review` once for the approved phase preparation (or the explicitly approved maintenance scope). Requirements, implementation proportionality, relevant tests and current harness must be verified. `READY FOR HANDOFF` is preparation readiness, not permission to invent missing functions.
4. Codex runs `$codex-create-handoff` for **one approved slice**. Handoff must have clear positive/negative acceptance examples, exact source boundaries, approved change targets, risks, test commands and retained invariants. No open material Owner decision and no stale phase permission.

## Each small slice

1. Antigravity runs `/antigravity-implement-handoff`, implements/tests **only approved work**, and always executes `antigravity-self-review` after implementation. Self-review does not count as a Codex independent review or as a remediation round.
2. Codex runs `$codex-final-review` **in slice-review mode** (the existing shared skill covers slice and phase-final modes). Every CRITICAL/HIGH/MEDIUM open finding or mandatory unverified case blocks `READY_FOR_OWNER_COMMIT`; LOW/INFO go to milestone/audit backlog.
3. If blocked, permit at most **three** corrective passes by Antigravity, each independently re-reviewed by Codex. Codex takeover is an exceptional last resort after three rounds and requires the Owner's separate direction; the implementer cannot self-review as independent reviewer.
4. After a clean review, **Owner** performs ordinary `git add`, inspects if desired, `git commit` and `git push`. There is **no separate staged-tree review session**. ChatGPT records a concise slice closeout with actual published commit.

## Each completed phase and later reviews

- After all its approved slices are owner-published, run **one independent Codex phase final review**, reusing unchanged source-applicable slice receipts; then ChatGPT creates the phase Owner closeout. Do not repeat full Java/Python suites for docs-only changes.
- Run Codex **three-phase RAG milestone** after **P19** for P17–19 and after **P22** for P20–22, only once the closing phase is completed and Owner-authorized. Consolidate LOW/INFO for one pragmatic corrective pass where warranted.
- Once **RAG P17–22** is complete, conduct a separately authorized **RAG-wide audit**.
- When both Backend and RAG are complete, conduct an independently authorized **RAG + Backend repository/integration audit**, reusing valid earlier evidence. The former Phase 15 Backend audit stays closed.
- Do not infer automatic permission to implement Phase 19/Frontend or enable external indexing from an archived roadmap or completed review.

## Responsibility and process safety

Codex primarily performs pre-handoff, detailed handoff, independent slice/phase review, milestones and audits; Antigravity implements and self-reviews; ChatGPT edits docs; **Owner alone stages/commits/pushes/tags/releases/PRs**. Git command rules and Antigravity hook are defense-in-depth, subject to independent validation on the actual Windows clients. Preserve original `logs/` unchanged.

Maintenance B1 watchdog is implemented (300s normal wall-time cap, up to 2,100s for pre-approved full-suite tests, diagnosis after 300s of no progress, owned-tree termination), awaiting independent Codex review; B2 tooling enforcement remains pending. Lack of stdout alone is not a hang; don't kill unrelated processes.

See [handoff state machine](../implementation/handoffs/README.md), [documentation templates](../templates/README.md) and [maintenance status](../implementation/maintenance/2026-10-25-post-phase18-simplification/README.md).
