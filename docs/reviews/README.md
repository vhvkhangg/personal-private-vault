# Code Review Records

Formal pre-commit Codex review history.

## Workflow

1. Codex creates an implementation handoff.
2. Antigravity implements/tests it and retains evidence.
3. Codex performs final review.
4. If changes are required, Codex updates the active handoff with remediation and Antigravity implements it.
5. If ready, Codex records `READY FOR OWNER COMMIT` and supplies one Conventional Commit message.
6. Owner commits/pushes.

## Naming

`YYYY-MM-DD-<short-scope>-codex-review.md`

## Required content

- review scope/handoff ID;
- baseline/working-tree reference;
- Antigravity test evidence;
- findings by severity;
- architecture/database/security conformance;
- residual risks;
- final status;
- recommended commit message only when ready.

Architectural decisions still belong in `docs/adr/`.
