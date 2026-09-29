---
name: antigravity-test-slice
description: Run a focused regression/test-only pass for an already implemented handoff without expanding implementation scope.
---

# Antigravity Test Slice

This is a secondary/test-only skill. The primary development workflow is `/antigravity-implement-handoff`.

Use this skill when the active handoff is already implemented and only a targeted regression pass is requested.

1. Read `docs/implementation/handoffs/ACTIVE.md`.
2. Add tests only if the handoff/review explicitly requires missing coverage.
3. Do not expand production scope.
4. Use PostgreSQL Testcontainers for persistence.
5. Run the requested focused/final command.
6. Preserve command/result evidence.
7. If a production defect is found, report it or return to `/antigravity-implement-handoff` when the handoff status permits remediation.

Do not commit or push.

## Required final response — next step

Every invocation must end with a concise **Next step:** statement.

- requested test-only remediation/evidence completed → tell the owner to rerun `$codex-final-review`;
- a production defect is discovered → tell the owner to return to `/antigravity-implement-handoff` when the active
  handoff permits production remediation, otherwise request Codex/owner scope clarification;
- blocked test execution → state the exact prerequisite needed before retrying.

Never leave the owner to infer the next workflow action.
