# Codex CLI Repository Notes

The repository skill `codex-final-review` lives at:

`.agents/skills/codex-final-review/SKILL.md`

Codex CLI skills are invoked with a `$` prefix, not as a same-name slash command:

```text
$codex-final-review
```

You can also run `/skills` and select `codex-final-review`.

Therefore `/codex-final-review` not appearing in Codex CLI inside IntelliJ IDEA is expected behavior
and is not a repository configuration error.

`config.toml` remains reserved for Codex/IDE configuration such as the IntelliJ IDEA MCP endpoint.
