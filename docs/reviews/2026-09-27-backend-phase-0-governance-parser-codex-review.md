# Codex Final Review — Backend Phase 0 Governance Parser

- Date: 2026-09-27
- Reviewer: Codex
- Scope: Re-review of the repository-safety hook after absolute-path, multiline, and nested-wrapper hardening
- Baseline / working tree: `main` at `45a89f3b6601798db5fadaa09aa6dcf5904c183a` plus the current uncommitted working tree; this review follows the two earlier Backend Phase 0 review records dated 2026-09-27
- Test evidence: `docs/implementation/backend-phase-0-governance-test-evidence.md` records `python -B .agents/hooks/test_repository_safety.py`, exit status 0, with 7 tests passing. The retained Maven Surefire report records 1 architecture test, 0 failures, 0 errors, and 0 skipped in 3.278 seconds. Codex did not rerun either suite. Codex performed one read-only hook probe and one benign PowerShell parsing probe for H-1.

## Findings

### Critical

None.

### High

#### H-1 — PowerShell backslash-before-quote input hides a publishing command from the hook

- Files: `.agents/hooks/repository_safety.py:57`, `.agents/hooks/repository_safety.py:70`, `.agents/hooks/test_repository_safety.py:45`, `docs/implementation/antigravity-cli-permissions.md:34`
- Observed problem: `_split_shell_segments` treats a backslash inside a double-quoted value as an escape for the next character. PowerShell does not use backslash as its quote escape character. As a result, the hook evaluates `git status "C:\temp\"; git push` as a single quoted segment and returns `allow`, while direct PowerShell parsing treats the quote after the final backslash as the end of the path string and executes the semicolon-separated second command. The benign equivalent `Write-Output "C:\temp\"; Write-Output SECOND` produced both output lines in PowerShell. This case is absent from the seven-test regression suite.
- Consequence: The documented narrow `command(git status)` permission is prefix-compatible with a command line that subsequently runs `git push`, but the defense-in-depth hook does not detect it. An agent could therefore publish through a command form that both layers are intended to prevent.
- Recommended correction: Do not apply C/POSIX backslash-escape semantics to PowerShell command segmentation. Use PowerShell-compatible quote handling (including backtick escaping), or fail closed with `force_ask` whenever segmentation ends in an ambiguous/unclosed quote state. Add a regression case for `git status "C:\temp\"; git push` and matching benign quoted-path cases, then run and retain the hook test result before re-review.

### Medium

None.

### Low

None.

## Architecture / Database Conformance

- Module boundaries: Unchanged and conformant with the frozen module inventory and dependency matrix.
- Database/Flyway: No schema, Flyway, or frozen DBML change was introduced.
- API: No endpoint or API-contract implementation was introduced.
- Security/logging: Application scaffolding remains free of identified secret/logging defects. The repository publishing guard remains blocked by H-1.
- Documentation: The earlier status and explicit bypass documentation issues are corrected. The current description still overstates the hook's effective command-boundary handling because of H-1.

## Residual Risks / Questions

- Shell command parsing is security-sensitive and shell-specific. The hook should prefer conservative `force_ask` behavior for ambiguous syntax rather than attempting permissive recovery.
- The architecture test does not explicitly lock the complete frozen module identifier inventory; this remains a non-blocking future hardening opportunity.
- Runtime application-context startup, Docker Compose startup, and database connectivity remain outside the supplied Phase 0 test evidence.

## Final Review Status

`CHANGES REQUIRED`

The previously reported absolute-path, multiline, and nested-wrapper cases are fixed and covered by passing evidence, but H-1 is a remaining publishing-policy bypass and is blocking.

> This status is a code-review workflow result, not an automated commit/push action.
