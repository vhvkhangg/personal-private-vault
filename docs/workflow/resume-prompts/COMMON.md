# Common interrupted-session recovery policy

## Common resume safety contract

- **Resume only the previously approved task**, not a new phase, approval, remediation round, or feature. A model-at-capacity error, cancelled tool call, context compaction, or broken CLI startup is not PASS/FAIL and does not increment a remediation/takeover counter without an actual independently reviewed corrective attempt.
- Start Codex at the **trusted repository root with native hooks loaded**. If the CLI fails to initialize or hook/config loading fails, STOP with sanitized diagnostics; do not disable hooks, bypass trust, or change global user settings.
- Read the latest local HEAD, `AGENTS.md`, `docs/implementation/handoffs/ACTIVE.md`, the canonical Owner-approved scope, the exact handoff (if any), and latest dated review/handback. Check `git status --short` and `git diff --stat`. Identify changes made by a previous partial session before editing.
- Recheck the [Docs Sync Check](../documentation-synchronization.md) for current scope and only files changed/invalidated by the interruption: current vs historical status, partial doc edits, evidence/publication claims and unresolved scoped drift. Reuse a verified checkpoint; never convert out-of-scope cleanup B into an authorized task or a remediation-counter increment.
- Reuse only **verified** completed work and existing immutable receipts. Do not claim test success from narration or old unrelated evidence. Continue from the first unfinished step, repeat only checks invalidated by source drift, and use fresh evidence paths. Do not overwrite, truncate, move, delete or reinitialize `logs/`.
- Preserve roles: Antigravity implements and self-reviews; Codex independently reviews, except a separately Owner-authorized bounded takeover whose code requires a different reviewing session. Only the Owner stages, commits, pushes, tags and publishes PRs.
- If an Owner decision, active-state conflict, forbidden change, unsupported client guard or mandatory unverified acceptance case blocks progress, STOP with exact evidence and the smallest Owner action. No B3/Phase 19 or other work can be activated from a resume prompt.
- End with an explicit **Next step**: application/person, exact skill command, scope and input reference, gate/result, remediation counter, and whether Owner action is required. Never fabricate acceptance.

## Typical interruption categories

- Model capacity/context exhaustion: switch model/session as appropriate, do not fabricate work or increase counters.
- CLI TOML/hook initialization error: STOP and report sanitized diagnostics and the proven cause. Do not edit project/global configuration, delete state, disable hooks, or change trust/permissions as an implicit resume action. A separately and expressly Owner-authorized bounded repair requires its own safety validation before any resumed execution.
- Interrupted tests/watchdog: inspect receipt and owned child state; rerun only if incomplete, using a unique new run directory.
- Partial documentation/write: inspect file content/diff and completed review records; avoid a second conflicting ACTIVE or duplicate dated acceptance.
