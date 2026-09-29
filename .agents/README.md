# Agent Customization

## Phase 3+ workflow

1. ChatGPT prepares next-phase docs/tooling.
2. Codex `$codex-pre-handoff-review`.
3. Owner commits/pushes preparation after `READY FOR HANDOFF`.
4. Codex `$codex-create-handoff`.
5. Antigravity `/agents` → `backend-implementer`.
6. Antigravity `/antigravity-implement-handoff`.
7. Codex `$codex-final-review`.
8. Owner commit/push.
9. ChatGPT closeout + next preparation.
10. After Phase 3/6/9/12/15: `$codex-milestone-review`.

See `docs/workflow/owner-phase-workflow.md`.

## Agents

- `backend-implementer`
- `architecture-auditor`

Reuse them by default. Add another agent only for a materially different role/tool/permission boundary.

## Engineering skills

- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `backend-testing`
- `authentication-security`
- `people-domain-modeling`
- `fiction-domain-modeling`
- `film-domain-modeling`
- `media-domain-modeling`
- `location-domain-modeling`

## Workflow/review skills

- `codex-pre-handoff-review`
- `codex-create-handoff`
- `antigravity-implement-handoff`
- `codex-final-review`
- `codex-milestone-review`
- `graphify-context`
- `architecture-change`
- `database-migration-review`

## Hooks

Reuse the repository safety hook. Add phase-specific hook logic only for a genuinely new command/security need.

## Graphify

Use `scripts/setup-graphify.ps1` and `scripts/refresh-graphify.ps1`.
Generated `graphify-out/` is navigation context only.
