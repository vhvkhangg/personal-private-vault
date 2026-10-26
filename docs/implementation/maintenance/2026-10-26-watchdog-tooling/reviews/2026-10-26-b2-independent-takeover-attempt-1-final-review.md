# Independent final review — Maintenance B2 Codex takeover attempt 1/2

- Date / reviewer: **2026-10-26 / Codex**, independent of the prior Codex implementation and self-review session. This session made no implementation correction.
- Entry: local `main` HEAD **`a4cf43505ab48198e22799fecf5098c5c6870a79`**; `MAINT-B2-CLI-GIT-HOOK-ENFORCEMENT / IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- Authority: unchanged [B2 executable contract](../b2-executable-handoff-2026-10-26.md), [Owner scope](../README.md), [takeover handback](../b2-codex-takeover-attempt-1-handback-2026-10-26.md), and [Owner delivery policy](../../../../workflow/current-owner-delivery-policy.md).
- Disposition: **READY_FOR_OWNER_COMMIT — B2 only.** B2-F01 and B2-F04 are **VERIFIED / CLOSED**; B2-F02/F03 remain independently **VERIFIED / CLOSED**. No blocking finding or mandatory unverified case remains within the approved supported scope.
- Counter: **F01 Codex attempt 1/2**, independently accepted. Antigravity remains **3/3 exhausted**. The earlier CLI interruption/resumption did not advance either counter. No second attempt or Option B acceptance change is used.

## Evidence and review boundary

Read current HEAD, AGENTS, ACTIVE, the executable contract, both implementation handbacks and all four prior independent B2 reviews. The earlier initialization error (`failed to load workspace requirements: invalid data`) and launch-quoting failure remain historical evidence, not an implementation/review verdict. No cause or configuration repair is inferred. Subsequent strict-config Windows initialization and actual tool use succeeded, including this independent run.

Fresh independent evidence is retained under [the unique review evidence directory](../../../../../logs/maintenance-b/b2-codex-takeover-a1-independent-d919d6c36db24f5d87c7611cdb6ceadc/), abbreviated **I** below. Reused takeover evidence is `logs/maintenance-b/b2-codex-takeover-a1-e49ddaf796ac4f80ba22b1d55d19ecf5/resume-1807d262cede4909bb9526953584de20/`, abbreviated **S**. Source/config identities match the submitted takeover evidence; no repeated original native probe was needed.

Acceptance is for **Codex CLI 0.164.0 on Windows, started at the trusted repository root with native hooks effectively trusted/loaded**. The tool working directory may be the isolated fixture, as actually verified. The current relative `command_windows` does **not** establish supported startup from a scoped subdirectory. Skipped/untrusted/unavailable hooks, arbitrary scripts/aliases/encoded commands, alternate connectors/GUI, other platforms and future clients are not certified. These are explicit operating/coverage limits, not PASS labels, universal interception claims or an Option B waiver.

## Finding closure register

| Finding | Severity retained | Independent result and evidence |
| --- | --- | --- |
| **B2-F01** | HIGH original | **VERIFIED / CLOSED.** Conditional inline native hook configuration and a thin adapter close the reproduced option/string-wrapper gaps on the supported Windows launch path. Three original negative probes are verified in S; eight supplemental negative probes are independently denied before dispatch in I. Benign status, diff and a focused test execute successfully. Details below. |
| **B2-F02** | MEDIUM original | **VERIFIED / CLOSED, no regression.** Preserve the provenance-correct native allow/deny/force-ask evidence and measured ambient-policy limit established by [re-review 3](2026-10-26-b2-independent-re-review-3.md). Shared evaluator functions are unchanged, AG registration is unchanged, and all focused tests pass. No new stronger human-prompt/hard-denial claim. |
| **B2-F03** | MEDIUM original | **VERIFIED / CLOSED, no regression.** Existing malformed-input/schema cases remain green. The added Codex adapter independently denies malformed native input without echoing payloads or granting overrides. |
| **B2-F04** | MEDIUM residual; HIGH original | **VERIFIED / CLOSED.** The optional audit producer, target state, call and unused imports are removed. There is no audit sink to append to, race for, or populate with private command arguments. Removal/retention/privacy tests and source inspection substantiate closure. |

### F01: actual native enforcement, rather than parser-only proof

**Locations:** `.codex/config.toml:10–19`; `.agents/hooks/repository_safety.py:447–478, 520–524`; adapter regressions in `.agents/hooks/test_repository_safety.py:399–450`.

The native adapter accepts Codex's `Bash` command input and calls the existing `evaluate_tool_call`, rather than duplicating classification. Allowed commands return `{}` to preserve ambient permissions; deny and malformed/conservative input emit native `hookSpecificOutput.permissionDecision: deny`. Codex's documented native protocol does not support PreToolUse `ask`; using native denial here is appropriate. [Official hook protocol](https://learn.chatgpt.com/docs/hooks).

TOML parsing succeeds. Parsed non-hook configuration equals HEAD: low model verbosity, `approval_policy=never`, `danger-full-access`, existing MCP endpoint and tool approval mode are preserved. The project change meets the contract's conditional configuration route because the original prefix gaps were reproduced independently. No global trust/configuration, model or permission change, explicit rules injection or trust-bypass flag was used. Sanitized user configuration inspection found no competing user PreToolUse registration; normal hook trust state is not a second command guard.

S's actual router errors at `native-enforcement-argv.stderr.log:2–4` prove pre-dispatch denial of **`git add -h`**, **`git -C "<fixture with spaces>" add -h`**, and **`cmd.exe /c "git add -h"`**; its benign status completed. Those are raw client errors, not the model's final narration. The prior automatic prefix-rule loading evidence remains applicable; prefix rules alone still have their documented limitations. Current acceptance relies on the effective native hook in addition to those rules.

I launched the actual installed `codex exec --json --strict-config --ephemeral -C <repository-root> -` through published B1 capture, without modifying normal approval/sandbox/tool settings. Its exact prompt, argv construction, installed path/version and fixture are retained in `native.py`, `native-prompt.txt`, `native-manifest.json` and `client-version.json`. `native-client.stderr.log:1–8` records **PreToolUse rejection before dispatch** for every supplemental negative:

| Safe native probe, fixture working directory | Actual result |
| --- | --- |
| `git -c core.quotePath=false commit -h` | Denied before dispatch |
| `& 'C:\Program Files\Git\cmd\git.EXE' add -h` | Denied before dispatch; quoted absolute path with spaces |
| `powershell.exe -NoProfile -Command "git add -h"` | Denied before dispatch |
| `powershell.exe -NoProfile -Command "cmd.exe /c git commit -h"` | Denied before dispatch; supported nested form |
| `git push -h` and `git tag -l` | Both denied before dispatch |
| `gh pr create --help` and `gh pr merge --help` | Both denied before dispatch; no reliance on a missing executable |
| `git status --short`, `git diff --stat`, one named pure classifier test | All three actually executed, exit **0** |

`native-assessment.json` matches all eight literal negatives to native errors, finds **zero unexpected dispatches**, and records successful benign completions at JSONL lines **5, 7 and 9**. There is no startup failure or skipped-hook/trust warning in this run. Safe help/list probes establish the matching command families and representative required shapes, not every possible argument or shell language.

### F04: remove the producer, preserve retained evidence

**Locations:** current hook `main` at `.agents/hooks/repository_safety.py:480` and imports/entrypoint; regression `.agents/hooks/test_repository_safety.py:351–397`. The diff against the independently reviewed round-3 snapshots removes `_audit_log`, `_created_audit_targets` and the invocation completely; there is no replacement collector.

The regression sets the obsolete audit environment variable to both fresh and existing synthetic targets. No fresh audit is created, existing bytes remain identical, and synthetic private command/prompt/argv canaries are absent from hook output. In-process I/O spies also establish no audit `open`, path-existence or directory-creation work across allow/deny/force-ask cases. With no target acquisition or write path, the former concurrent claimant interleaving cannot append through this hook. Historical evidence is never used as a mutation fixture. This closes both residual defects without adding a capture pipeline or weakening enforcement.

## Focused verification and acceptance traceability

| Check | Actual result / receipt |
| --- | --- |
| Independent `python -B -m unittest discover -s .agents/hooks -p test_repository_safety.py -v`, B1 normal 300s | **21/21 PASS**, exit 0; **0.993s** tests, **1.531s** wrapper, **1.734s** outer. I: `hooks.json`, raw streams and supervisor receipt. |
| Independent native supplemental matrix, B1 normal 300s | **8/8 negative denials + 3/3 benign completions**, exit 0; **98.672s** wrapper / **98.875s** outer, no timeout. I: `native.json`, client streams/assessment and 320s outer-supervisor receipt. |
| Original takeover native matrix reused after source identity verification | Three required direct/option/cmd denials plus benign status; actual Windows strict-config initialization succeeds. S's failed quoting launch remains preserved separately. |
| Unchanged B1/output evidence reused | Published B1 30-test/native acceptance and prior independent **5/5** B1 output regressions remain valid; runner/tests are byte-identical. No B1 reopening or redundant application suite. |
| Static, scope and preservation checks | Python AST/TOML checks, unchanged shared evaluator ASTs, non-hook config equality, `git diff --check`, empty staged diff and excluded-source checks pass. I: `prewrite-checks.json`; final documentation checks in `postwrite-checks.json`. |

**B2-01/02/03/06/10:** actual Windows loading, direct/option/absolute/wrapper/nested/GitHub denials, conditional scoped native adjustment and explicit limitations above. **B2-04:** real benign commands/test work. **B2-05:** prior native AG evidence and unchanged shared behavior retained. **B2-07/08:** reviewed invocation instructions correctly separate low narration verbosity from client-rendered events, reuse B1 full saved streams and bounded summaries, and preserve error exits. **B2-09:** no audit producer, privacy regressions and retained-log/index/fixture checks. All mandatory B2 cases pass within the approved supported scope; no application/frozen capability was enabled to obtain that result.

## Inventory, coverage and retained backlog

At review entry: **24 dirty paths (16 tracked modifications, eight untracked documents), plus unchanged `.agents/hooks.json`: 25 inspected inputs**. Full path/hash inventory is I's `review-inputs.json`. Changed implementation/instructions: hook and focused tests; `.codex/config.toml`, rule file and README; `.agents/README.md`, two existing rules and implementation skill. Governance: AGENTS first current notice, ACTIVE/handoff README, Maintenance B README/B1 and B2 inventories, Owner policy, B1 closeout, B2 contract, both handbacks and four historical independent reviews. All were inspected; historical reviews/handbacks remain unchanged by this session. Recursive hook/rule inventory has only the existing components, with no new production helper/package/dependency.

Reviewed source SHA-256 identities:

- Hook: `0d5e69641ad0b9d498a64d612f76ac066bb353d5902b258683860c2d85ad8a99`.
- Tests: `767e36ea258f0e53dc40b657e8cde11670a95f9911271adabbd8960ac487a5fc`.
- Config: `b107aacfe5b3550867ba009a6c1aa966c090fec1bfbe1154446eca4fae52b643`.

Shared review coverage is risk-applicable: **PASS 1–3, 5, 7–10, 12, 14–16, 18, 21–23, 25–27, 29** for scoped requirements, structure, reuse/adapter simplicity, validation, privacy/no shared audit sink, bounded operations, actual Windows negative/benign tests, proportional performance evidence, unchanged dependencies and delivery hygiene. **FINDING 28 — LOW only:** retain existing documentation/evidence-index backlog. **N/A 4, 6, 11, 13, 17, 19–20, 24:** no application module/API/ADR-0016 transport, application authentication/consent, database, retrieval/document quality or evaluation-science change. No warning-free IDE/compiler claim or broad performance/security certification is made.

**B1-L01, B2-L01 and MA-A03–MA-A06 remain LOW / OPEN for consolidated milestone/audit remediation**, with their original evidence and minimal corrections preserved. In particular, B2-L01's four-row component trace is not substituted for the provenance-correct native F02 event; see re-review 3's shared/mirrored event and client-step correlation. No LOW finding forces another slice correction round.

Only this new review, seven current status notices and fresh ignored evidence/fixtures are written by this reviewer. Pre-existing relevant audit files retain their recorded hashes; B1 source/tests, live index/refs and fixture index/HEAD/refs/remotes remain unchanged. No claim of an exhaustive historical-log hashing audit is made. No logs were deleted, overwritten, moved or truncated. No staging, publishing, application suites or implementation corrections occurred. Phase 17 remains FROZEN, Phase 18 CLOSED, B3/Phase 19 unauthorized, indexing INACTIVE and live originals DISABLED/DEFERRED.

## Next action

**Owner commits/pushes B2**, then records its brief publication closeout. After both slices are Owner-published, the next review skill is **`$codex-final-review` for Maintenance B final aggregation**, reusing accepted B1/B2 evidence and checking only relevant drift. This is the B2 slice acceptance, not that aggregate gate. No separate staged-tree review, second takeover attempt or new phase is authorized.

Conventional Commit: **`fix(tooling): enforce owner-only Git actions with native hooks`**.
