# B2 — CLI output and Git/hook enforcement (second Maintenance B slice)

- **Status:** `OWNER_APPROVED / BLOCKED_UNTIL_B1_OWNER_PUBLISHED_AND_B2_HANDOFF`.
- **Dependency:** B1 independently `READY_FOR_OWNER_COMMIT` **and actually Owner-published**; separate Codex B2 executable handoff. B1 verification and accepted output contract become inputs, not an invitation to rewrite B1.
- **Implementer:** Antigravity + mandatory self-review. **Independent reviewer:** Codex. **Publication:** Owner.

## Allowed modifications

- Existing `.codex/rules/owner-git.rules` and `.codex/README.md`; `.codex/config.toml` only if the approved requirement demonstrably cannot be met without a narrowly justified compatible config change. Do not silently change model selection, approved approvals/sandbox or tool permissions.
- Existing `.agents/hooks.json`, `.agents/hooks/repository_safety.py` and `.agents/hooks/test_repository_safety.py`; minimal existing rules/skills docs to match *verified* behavior. Avoid new global agents, external privilege hooks or a system-wide policy service.
- Reuse B1's new output/receipt/log capabilities. Do not build a second competing command-capture pipeline.

## Acceptance examples

| ID | Scenario | Mandatory observable outcome |
| --- | --- | --- |
| B2-01 | Codex in actual trusted Windows project | Verify actual loaded execpolicy, **not merely** `codex execpolicy check --rules`. Record tested CLI/version/config and true interception outcomes. |
| B2-02 | Direct Owner-only Git publishing attempts | Reject agent `git add/commit/push/tag` and `gh pr create/merge` in the actual supported CLI interception paths. Do not execute unsafe publishing actions in the real project. |
| B2-03 | Git wrapper/options (e.g. `git -C`, `git -c`, `cmd /c`, `powershell -Command`) | Test representative wrapped forms in a **disposable local repository** or non-mutating policy probes. Where a client cannot intercept a form, classify `NOT_VERIFIED`/remaining gap; do not claim complete prevention. |
| B2-04 | Benign work | `git status`, `git diff --stat`, ordinary relevant build/test commands continue to work without repeated unnecessary permission prompts. |
| B2-05 | Antigravity Windows client invokes its PreToolUse hook | Prove hook is invoked with real client payload, that intended deny/force-ask/allow responses take effect, and that correct path/interpreter working-directory resolution is used. Unit tests alone are insufficient. |
| B2-06 | Any uncovered Git publish path | Prefer the smallest supported native guard/config adjustment and tests. If enforcement is impossible without a high-risk broader redesign or privileges, stop and return a concrete limitation/Owner decision, not invented airtight security. |
| B2-07 | Codex normal progress and diff/report output | Ask for short narration/status and file names or `git diff --stat` rather than automatic large patches. Confirm `model_verbosity=low` cannot control client-rendered `Ran` events or tool previews; do not overpromise hidden UI output. |
| B2-08 | Long command output/error | Reuse B1 full saved output in new ignored `logs/` paths plus bounded console/receipt summaries; no repeated massive full-output paste, no test results lost. |
| B2-09 | Privacy and preservation | No original `logs/` deletion, rewrite, relocation, or truncation; no credentials/secrets/raw argv in receipts; do not broaden shell permissions or let agent stage/commit/push. |
| B2-10 | CLI hook unavailable/version-incompatible | Record actual limitation and only supported scoped mitigation, without altering the Owner's local global Codex/Antigravity settings without separate approval. |

## Minimal test and review plan

1. On Windows, use `codex --version`, rule syntax checking and a **real trusted-client** invocation that cannot alter the live repository; distinguish parser test from actual hook/rule auto-loading. Capture exact outcomes in uniquely named `logs/maintenance-b/b2-*` evidence.
2. Test Antigravity hook with its real working-directory and the existing JSON protocol (read-only and blocked dry/sandbox cases). Avoid actual Git commits/pushes/tags/PR or destructive operations in the Owner repo. For mutation-deny tests, use a disposable test repository if necessary and positively prove isolation.
3. Run `.agents/hooks/test_repository_safety.py` plus relevant B1 focused regressions. The previous **13/13** and **5/5** are historical baseline receipts, not acceptance of new Windows behavior.
4. Validate concise agent narration and diff-stat; keep complete new logs accessible. Do not promise ability to suppress all UI-generated events from a third-party terminal/CLI client.
5. Antigravity mandatory self-review, Codex independent B2 review, Owner commit/push. Then one Maintenance B final review consolidates accepted B1/B2 findings and evidence. No redundant full application-suite gate absent demonstrable relevant drift.

## Explicit exclusions

No new RAG feature, Phase 19, live indexing, CI/CD redesign, general purpose CLI replacement, global OS security hardening, local system-wide permission changes, new agent/daemon, blanket output suppression that loses errors, historical evidence deletion, or changes to completed Phase 17–18 contracts.
