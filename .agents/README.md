# Agent Customization

Primary workflow:

1. Codex `$codex-create-handoff`
2. Antigravity `/agents` → `backend-implementer`
3. Antigravity `/antigravity-implement-handoff`
4. Codex `$codex-final-review`
5. Owner commit + push

Canonical workflow: `docs/agent-development-workflow.md`.

## Custom agents

- `backend-implementer`
- `architecture-auditor`

## Engineering skills

- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `backend-testing`

Workflow/tool skills remain under `.agents/skills/`. Implementation/review skills explicitly route to relevant
engineering skills so they are used through progressive disclosure.

## Windows permissions

```powershell
powershell -ExecutionPolicy Bypass -File scripts/configure-antigravity.ps1
```

Windows currently has an upstream issue where specific `command(...)` allow rules may still prompt. The safety
hook therefore also emits narrow per-call `permissionOverrides` for safe Maven/Java/Docker/read-oriented commands.

## Graphify

```powershell
powershell -ExecutionPolicy Bypass -File scripts/setup-graphify.ps1
powershell -ExecutionPolicy Bypass -File scripts/refresh-graphify.ps1
```

Deleting the setup script does not uninstall Graphify, but keeping it committed preserves reproducibility.
