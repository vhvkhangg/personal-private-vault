# Agent Implementation Handoffs

`ACTIVE.md` is the canonical implementation contract between Codex and Antigravity.

## State machine

```text
PENDING_CODEX_HANDOFF
        ↓
READY_FOR_IMPLEMENTATION
        ↓
IMPLEMENTED_AWAITING_CODEX_REVIEW
        ↓
┌───────────────────────┬────────────────────────┐
│ CHANGES_REQUESTED     │ READY_FOR_OWNER_COMMIT │
│ ↓                     │ ↓                      │
│ Antigravity remediates│ Owner commit + push    │
└───────────────────────┴────────────────────────┘
```

Codex creates/plans and reviews. Antigravity implements/tests. The owner commits/pushes.
After the owner implementation commit/push, ChatGPT archives the accepted handoff in the owning
phase folder and resets `ACTIVE.md` to `NO_ACTIVE_HANDOFF` with a separate owner-published closeout.
Completed Phase 17 is archived at [Phase 17 handoff](../phase-17/handoff.md).

Handoffs should link to canonical documents rather than duplicating large schema/design content.

## RAG Phase 18–22 remediation escalation (owner-approved)

For future RAG handoffs, the `CHANGES_REQUESTED` branch allows at most **two** Antigravity remediation + independent Codex re-review cycles after the initial implementation review. Record the remaining finding IDs, immutable report paths and the current round in the **existing** handoff. If both re-reviews remain blocked, continue the *same handoff* with Codex `$codex-implementation-takeover` in a separate implementation session, not a third Antigravity round. An independent reviewing session must verify Codex-authored fixes. This is a role transition, **not** a new handoff state, exception to security, permission to skip tests, or owner commit authorization. Use existing `CHANGES_REQUESTED` → `IMPLEMENTED_AWAITING_CODEX_REVIEW` → independent final-review transitions.

## Phase 15 audit exception

Phase 15 starts with no handoff. `$codex-backend-audit` creates at most one remediation handoff when actionable
findings exist. That handoff may temporarily reach `READY_FOR_OWNER_COMMIT` after handoff-specific final review, but
the owner does **not** commit yet: `$codex-backend-audit` performs the repository-wide closure re-audit. New/remnant
findings return the same handoff to `CHANGES_REQUESTED`; a clean closure audit archives it under
`docs/implementation/phase-15/handoff.md`, resets `ACTIVE.md`, and returns `BACKEND_AUDIT_READY`.
