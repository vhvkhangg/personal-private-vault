# Agent Customization

This directory contains repository-local agent configuration shared where possible by Antigravity and Codex.

## Why this structure

- Repository-wide invariants live in root `AGENTS.md`.
- Directory-specific invariants live in scoped `AGENTS.md` files.
- Antigravity conditional rules live in `.agents/rules/`.
- Repeatable workflows live in `.agents/skills/`.
- Antigravity lifecycle hooks are configured in `.agents/hooks.json`.

The repository intentionally does not duplicate the root instructions into `GEMINI.md`; both tools can consume `AGENTS.md`, and duplicated always-on context would increase prompt size without adding information.

Antigravity legacy Workflows are intentionally not used. Skills are the forward path for repeatable workflows.

## Role split

- Owner: production implementation and all git commits/pushes.
- Antigravity: write tests for a completed slice and run them once.
- Codex: final review before commit/push; review-only by default.

## Hooks

`hooks.json` contains a repository-safety gate implemented by `.agents/hooks/repository_safety.py`.

The hook:
- denies agent-driven `git commit`, `git push`, `git tag`, and PR merge/create commands;
- understands direct and nested PowerShell/pwsh/cmd wrappers, including absolute Windows executable paths, plus Git global options such as `git -c` / `git -C`;
- treats CR/LF command boundaries outside quotes as separate commands;
- requests confirmation for destructive shell commands, including order-independent PowerShell deletion flags;
- requests confirmation before edits to frozen baseline files.

Hook regression tests live at `.agents/hooks/test_repository_safety.py` and can be run with:

```text
python -B .agents/hooks/test_repository_safety.py
```

The hook requires a `python` executable on PATH. If Antigravity cannot launch it, disable the hook temporarily from Antigravity Customizations > Hooks until Python is available. Do not replace it with an unsafe unconditional-allow hook.


## Antigravity CLI permissions

Safe command auto-approval is configured in Antigravity's **project permission settings**, not by a
source-controlled hook. Follow `docs/implementation/antigravity-cli-permissions.md` once before the
first Antigravity CLI test run.

## Skill invocation

- Antigravity CLI exposes workspace skills as slash commands, so use `/antigravity-test-slice`.
- Codex CLI skills are invoked with a `$` prefix; use `$codex-final-review`, or open `/skills` and
  select `codex-final-review`.
