---
name: codex-final-review
description: Final-review either an implemented active handoff or an explicitly requested owner-approved maintenance/governance slice; request remediation or return READY FOR OWNER COMMIT with one commit message.
---

# Codex Final Review

Invoke with `$codex-final-review` or select it from `/skills`. Codex is review-only.

## Review-mode gate

### Implementation handoff mode

Use only when `docs/implementation/handoffs/ACTIVE.md` is `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

### Owner-approved maintenance/governance mode

Use only when the owner explicitly requests such a review and a canonical scope document identifies the slice
(for example `docs/implementation/agent-tooling-change-summary.md` or
`docs/implementation/phase-1/package-layout-refactor.md`). This mode may run while `ACTIVE.md` is
`NO_ACTIVE_HANDOFF`. A frozen phase may be touched only where the owner-approved maintenance scope explicitly says so.

### No valid mode

If neither condition is met, **STOP and ask the owner what approved slice should be reviewed**. Never default to a
frozen phase or archived handoff.

## Review

Use targeted Graphify navigation when useful and only relevant engineering skills:
`java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`,
`design-pattern-selection`, `modular-monolith-architecture`, `jpa-postgresql-persistence`, `backend-testing`.

Review correctness, scope, architecture/ownership, persistence where applicable, security, evidence quality,
tooling safety, and stale/competing documentation. Record findings under `docs/reviews/`.

## Changes required

Implementation mode: set the handoff to `CHANGES_REQUESTED` and add remediation.

Maintenance/governance mode: set the scope doc to `CHANGES REQUESTED` and update its remediation checklist;
do not create a fake production handoff.

Do not provide a commit message while blocking findings remain.

## Ready

If no blocking finding remains, record `READY FOR OWNER COMMIT`, synchronize the reviewed scope status, provide
exactly one Conventional Commit message, and do not commit/push.

Frozen phases remain frozen unless their production/test/database/build artifacts are explicitly owner-approved
review scope.


- Use `authentication-security` when authentication/JWT/PIN/refresh-token work is in scope.
