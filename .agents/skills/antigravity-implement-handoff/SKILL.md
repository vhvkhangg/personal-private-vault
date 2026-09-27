---
name: antigravity-implement-handoff
description: Implement the active Codex handoff, write tests, iterate to a green result, preserve evidence, and hand the slice back to Codex without committing.
---

# Antigravity Implement Handoff

Use `/antigravity-implement-handoff`.

## Preconditions

Read:

- root `AGENTS.md`;
- relevant scoped `AGENTS.md`;
- `docs/implementation/handoffs/ACTIVE.md`.

Proceed only when status is:

- `READY_FOR_IMPLEMENTATION`, or
- `CHANGES_REQUESTED`.

If there is no active Codex handoff, stop.

## Context strategy

1. Read exact handoff references first.
2. If Graphify is available, use `/graphify-context` or targeted `graphify query` calls before broad repo searches.
3. Verify graph-derived facts in source files.
4. Avoid loading whole schemas/logs unless needed.

## Implementation loop

1. Implement only the handoff targets.
2. Add/update tests for the same behavior.
3. Use focused compile/tests during development.
4. Fix defects found by those tests.
5. You may rerun focused tests as needed.
6. Limit repeated **full-suite** attempts to three for the same unresolved cause; stop and report a blocker instead of looping.
7. Run the handoff's final Maven command once implementation converges.
8. Record exact command, exit status, test counts, and material warnings/errors in the specified evidence file.
9. If Graphify is installed, refresh the code-only graph after implementation so Codex sees current structure.
10. Update the handoff:
   - status → `IMPLEMENTED_AWAITING_CODEX_REVIEW`;
   - summarize files changed;
   - summarize tests/evidence;
   - list any residual risk.

## Phase 1 implementation notes

- Implement the existing `reference` and `vault` skeletons.
- Replace the `TODO(antigravity)` placeholders with real code.
- Use Flyway V1 as the physical-schema contract.
- Use Testcontainers PostgreSQL, not H2.
- Use Lombok only where it removes safe boilerplate.
- Do not use `@Data` on JPA entities.
- Do not add controllers/auth/settings/other modules.

Do not commit, push, tag, or create/merge a PR.
