# Codex CLI Repository Notes

Repository skills live under `.agents/skills/`.

Primary commands:

```text
$codex-create-handoff
$codex-final-review
$codex-backend-audit
$graphify-context
```

Codex skills use `$skill-name` (or `/skills`), not same-name slash commands.

## Role

Codex creates implementation handoffs, performs final review, and owns the dedicated Phase 15 comprehensive backend audit. It does not write production implementation
in the normal workflow and never commits/pushes.

When final review succeeds, Codex supplies exactly one Conventional Commit message for the owner.

Graphify is optional. If installed through `scripts/setup-graphify.ps1`, Codex should use it for targeted
navigation before broad source reads.

Canonical workflow: `docs/workflow/agent-development-workflow.md`.
