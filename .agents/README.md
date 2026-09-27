# Agent Customization

Repository-local configuration shared across Codex and Antigravity.

## Primary workflow

1. Codex: `$codex-create-handoff`
2. Antigravity: `/antigravity-implement-handoff`
3. Codex: `$codex-final-review`
4. Owner: commit + push using the commit message supplied by Codex

`/antigravity-test-slice` remains available for focused regression-only work.

## Instruction layout

- Root/scoped `AGENTS.md`: always-on repository invariants.
- `.agents/rules/`: Antigravity conditional rules.
- `.agents/skills/`: repeatable Codex/Antigravity workflows.
- `.agents/hooks.json`: repository safety hook.
- `docs/implementation/handoffs/ACTIVE.md`: current implementation contract.

## Repository safety hook

The existing hook denies agent-driven commit/push/tag/PR publishing, asks before destructive commands, and protects frozen baselines.

Regression test:

```text
python -B .agents/hooks/test_repository_safety.py
```

## Graphify integration

Graphify is optional and used for token-efficient navigation.

Setup once from the repository root:

```powershell
powershell -ExecutionPolicy Bypass -File .agents/setup-graphify.ps1
```

Refresh after meaningful code changes:

```powershell
powershell -ExecutionPolicy Bypass -File .agents/refresh-graphify.ps1
```

The integration uses **code-only local AST extraction**. Generated `graphify-out/` data is local/ignored and is
not a source of truth. If Graphify is unavailable, agents fall back to targeted file reads.

## Skills

Codex CLI:

```text
$codex-create-handoff
$codex-final-review
$graphify-context
```

Antigravity CLI:

```text
/antigravity-implement-handoff
/antigravity-test-slice
/graphify-context
```
