# Agent Development Workflow

## Roles

| Role | Responsibility |
|---|---|
| Owner | Approves scope, manages local secrets/environment, commits and pushes |
| Codex | Creates implementation handoffs and performs final review |
| Antigravity `backend-implementer` | Implements production code + tests from the active handoff |
| Antigravity `architecture-auditor` | Optional read-only architecture/JPA/PostgreSQL audit |
| Graphify | Optional local navigation cache; never a source of truth |

## Standard slice

```text
Owner confirms scope
      ↓
Codex: $codex-create-handoff
      ↓
ACTIVE.md → READY_FOR_IMPLEMENTATION
      ↓
Antigravity: /agents → backend-implementer
      ↓
/antigravity-implement-handoff
      ↓
implementation + tests + final evidence
      ↓
IMPLEMENTED_AWAITING_CODEX_REVIEW
      ↓
Codex: $codex-final-review
      ↓
CHANGES_REQUESTED → Antigravity remediation → Codex review
or
READY_FOR_OWNER_COMMIT → Codex gives one Conventional Commit message
      ↓
Owner commit + push
```

## Skill routing

Use relevant focused skills through progressive disclosure:

- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `backend-testing`
- `graphify-context`

## Local tooling

```powershell
powershell -ExecutionPolicy Bypass -File scripts/configure-antigravity.ps1
powershell -ExecutionPolicy Bypass -File scripts/setup-graphify.ps1   # optional
```

After meaningful source changes:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/refresh-graphify.ps1
```

Graphify stays installed if the setup script is deleted; keep the script committed for reproducibility.

## Context discipline

Read the active handoff first, then exact canonical files. Use Graphify before broad exploration when available.
Do not repeatedly load unchanged DBML/migrations/logs.

## Git boundary

Agents never commit, push, tag, or create/merge PRs. The owner performs publishing.

## Phase completion

After final Codex review and owner commit/push, mark the phase `COMPLETE — FROZEN`, archive its handoff/evidence,
and clear `ACTIVE.md`. Later tooling/governance changes do not reopen the frozen business implementation unless
they modify production/test/database files.
