# Codex CLI — current project rules

- Read [Owner Delivery Policy](../docs/workflow/current-owner-delivery-policy.md) and [ACTIVE](../docs/implementation/handoffs/ACTIVE.md) first. **Phase 19 NOT AUTHORIZED**. Main work is pre-handoff review, detailed handoff, per-slice independent review, per-phase final review, post-P19/P22 milestone review and separate future RAG / RAG+Backend audits. Antigravity implements and self-reviews; it has up to three independently reviewed correction rounds per slice before exceptional Codex takeover.
- CRITICAL/HIGH/MEDIUM open defects block the reviewed delivery; LOW/INFO backlog belongs to milestone/audit. No independent review of the implementer's own code in the same session. No separate staged-tree review session; only Owner runs `git add`, `git commit`, `git push`, tag or PR publication.
- `model_verbosity = "low"` does **not** bound CLI tool stdout or line-by-line diffs. Keep narration short (`git diff --stat`, changed file list, key errors and receipt paths), retain full raw evidence under the existing ignored `logs/` (do not delete/move/truncate).
- `.codex/rules/owner-git.rules` defines project-local `prefix_rule(... decision="forbidden")` command restrictions. In a trusted Windows repository session (`codex-cli 0.164.0`), Codex automatically loads these rules and denies direct `git` / `git.exe` (`add`, `commit`, `push`, `tag`, `reset --hard`, `clean -f`, `branch -D`), direct `gh` / `gh.exe` (`pr create/merge/close`, `release create`, `repo delete`), and token-split wrapped invocations (`cmd /c git add`, `powershell -Command git add`).
- **B2-F01 takeover attempt 1/2 — independently VERIFIED/CLOSED:** project `PreToolUse` configuration calls the existing hook with `--codex`, reusing its evaluator. Actual Windows Codex 0.164.0 denied direct help, variable `git -C`, `git -c`, quoted absolute Git, cmd/PowerShell/nested wrappers and GitHub PR help before dispatch; benign status, diff and a focused test executed. The [separate independent review](../docs/implementation/maintenance/2026-10-26-watchdog-tooling/reviews/2026-10-26-b2-independent-takeover-attempt-1-final-review.md) records exact probes/evidence and closes F04 after audit-producer removal. Required root-startup/trust and coverage limits below remain in force.
- **Native hook invocation/coverage limits:** start Codex at the repository root; the current Windows hook command is relative to that session cwd. Scoped-subdirectory startup is not verified/supported by that command. Effective hook trust/loading remains required; skipped/unavailable hooks are not PASS. The adapter grants no allow override; conservative input denies. No global trust/permission change or bypass flag was used.
- **Prefix-rule limitations remain:**
  - Prefix rules match exact initial argv tokens. They do not support middle wildcards, so variable option sequences like `git -C <fixture> add` or `git -c <key>=<val> add` do not match.
  - When wrapped commands are dispatched as single quoted strings (e.g. `cmd.exe /c "git add -h"` or `powershell -Command "git add -h"`), Codex does not recursively parse inner shell strings, so the compound token `"git add -h"` does not match token `"git"`.
  - The Antigravity PreToolUse hook operates only within Antigravity sessions and does not monitor Codex commands. Do not assume cross-client protection. Owner alone executes staging and publishing actions.

Example verified **read-only policy checks** on Codex CLI:

```powershell
codex execpolicy check --pretty --rules .codex/rules/owner-git.rules -- git add .
codex execpolicy check --pretty --rules .codex/rules/owner-git.rules -- git commit -m test
codex execpolicy check --pretty --rules .codex/rules/owner-git.rules -- gh.exe pr create
codex execpolicy check --pretty --rules .codex/rules/owner-git.rules -- cmd /c git push
codex execpolicy check --pretty --rules .codex/rules/owner-git.rules -- git status
```

No automated `git add/commit/push/tag` or PR creation is ever authorized by this policy. Owner alone performs staging and publishing actions.
