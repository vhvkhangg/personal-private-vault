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
