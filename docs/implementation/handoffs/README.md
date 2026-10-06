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

Handoffs should link to canonical documents rather than duplicating large schema/design content.

## Phase 15 audit exception

Phase 15 starts with no handoff. `$codex-backend-audit` creates at most one remediation handoff when actionable
findings exist. That handoff may temporarily reach `READY_FOR_OWNER_COMMIT` after handoff-specific final review, but
the owner does **not** commit yet: `$codex-backend-audit` performs the repository-wide closure re-audit. New/remnant
findings return the same handoff to `CHANGES_REQUESTED`; a clean closure audit archives it under
`docs/implementation/phase-15/handoff.md`, resets `ACTIVE.md`, and returns `BACKEND_AUDIT_READY`.
