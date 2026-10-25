# Codex CLI — current project rules

- Read [Owner Delivery Policy](../docs/workflow/current-owner-delivery-policy.md) and [ACTIVE](../docs/implementation/handoffs/ACTIVE.md) first. **Phase 19 NOT AUTHORIZED**. Main work is pre-handoff review, detailed handoff, per-slice independent review, per-phase final review, post-P19/P22 milestone review and separate future RAG / RAG+Backend audits. Antigravity implements and self-reviews; it has up to three independently reviewed correction rounds per slice before exceptional Codex takeover.
- CRITICAL/HIGH/MEDIUM open defects block the reviewed delivery; LOW/INFO backlog belongs to milestone/audit. No independent review of the implementer's own code in the same session. No separate staged-tree review session; only Owner runs `git add`, `git commit`, `git push`, tag or PR publication.
- `model_verbosity = "low"` does **not** bound CLI tool stdout or line-by-line diffs. Keep narration short (`git diff --stat`, changed file list, key errors and receipt paths), retain full raw evidence under the existing ignored `logs/` (do not delete/move/truncate).
- `.codex/rules/owner-git.rules` contains **preview `prefix_rule(... decision="forbidden")` direct command restrictions**. `danger-full-access`/`approval_policy=never` remain unchanged but do not constitute publishing permission. The rules are **not proven active** until verified using the Owner's real Windows Codex CLI in a *trusted* repo and tests of wrapped `cmd`, `powershell`, `git -C`, connector actions and obvious bypasses. A prefix rule does not cover arbitrary invocations by itself. Prefer denied operation and a human-run command rather than claiming absolute protection.

Example **read-only policy test** on the Owner's CLI (not executed in this docs-only patch):

```powershell
codex execpolicy check --rules .codex/rules/owner-git.rules git commit -m test
codex execpolicy check --rules .codex/rules/owner-git.rules git status
```

If the installed Codex version cannot load this format or a bypass is found, mark protection `NOT_VERIFIED` and correct the tooling under Maintenance B; **do not report the hook as guaranteed**. No automated `git commit/push/tag` is ever authorized by this policy.
