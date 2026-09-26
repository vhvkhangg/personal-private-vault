# Code Review Records

This directory stores formal pre-commit Codex review records.

## Workflow

1. The repository owner writes the implementation.
2. Antigravity writes the tests and runs the relevant test command once.
3. The owner fixes confirmed issues.
4. Codex performs the final review.
5. Codex records the review here.
6. The owner commits and pushes if satisfied.

## Naming

Use:

`YYYY-MM-DD-<short-scope>-codex-review.md`

Example:

`2026-10-03-authentication-bootstrap-codex-review.md`

## Content

Each review should include:

- review scope;
- commit/working-tree reference;
- Antigravity test evidence;
- findings grouped by severity;
- architecture/database/security conformance;
- residual risks/questions;
- final workflow status.

Do not use this directory as a substitute for ADRs. Architectural decisions belong in `docs/adr/`.
