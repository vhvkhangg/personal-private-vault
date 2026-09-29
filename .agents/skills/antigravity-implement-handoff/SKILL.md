---
name: antigravity-implement-handoff
description: Implement the active Codex handoff, write tests, iterate to green, preserve evidence, and return the slice to Codex without committing.
---

# Antigravity Implement Handoff

Use `/antigravity-implement-handoff`.

For substantial backend work select `backend-implementer` from `/agents`.

## Preconditions

Read root/scoped `AGENTS.md` and `docs/implementation/handoffs/ACTIVE.md`.

Proceed only with `READY_FOR_IMPLEMENTATION` or `CHANGES_REQUESTED`.

If `ACTIVE.md` says `NO_ACTIVE_HANDOFF`, references frozen/completed work as executable scope, or does not cover
the requested production change, **STOP**. Never infer work from frozen phase docs or historical TODOs.

## Engineering skills

Apply relevant skills progressively:

- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `backend-testing`
- `authentication-security` when authentication/JWT/PIN/refresh-token work is in scope
- `graphify-context` when broad navigation is needed

## Loop

1. Read exact handoff references.
2. Use Graphify before broad exploration when available; verify source facts.
3. Implement only approved targets.
4. Add/update tests.
5. Iterate focused tests.
6. Limit repeated full-suite failures to three for the same unresolved cause.
7. Run final verification after convergence.
8. Record exact command/result/test counts.
9. Refresh Graphify with `scripts/refresh-graphify.ps1` after meaningful source changes when installed.
10. Mark `IMPLEMENTED_AWAITING_CODEX_REVIEW` and summarize changes/evidence/risks.

Do not commit, push, tag, or create/merge a PR.

## Required final response — next step

Every invocation must end with a concise **Next step:** statement.

- implementation/tests complete and handoff marked `IMPLEMENTED_AWAITING_CODEX_REVIEW` → tell the owner to run
  `$codex-final-review`;
- blocked implementation → state the exact blocker and whether the owner/Codex must clarify or amend the handoff;
- remediation complete after a Codex `CHANGES_REQUESTED` review → tell the owner to rerun `$codex-final-review`.

Never leave the owner to infer the next workflow action.
