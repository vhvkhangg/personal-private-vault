# Independent final review — Maintenance B2 CLI Output & Git/Hook Enforcement

- Date / reviewer: 2026-10-26 / Codex, independent of Antigravity implementation/self-review.
- HEAD / scope: local `main` `a4cf43505ab48198e22799fecf5098c5c6870a79`; submitted working tree, `MAINT-B2-CLI-GIT-HOOK-ENFORCEMENT / IMPLEMENTED_AWAITING_CODEX_REVIEW` at entry. [Approved executable contract](../b2-executable-handoff-2026-10-26.md), [B2 inventory](../slice-b2-tooling.md), [implementation handback](../b2-implementation-handback-2026-10-26.md).
- Disposition: **CHANGES_REQUESTED — one HIGH and two MEDIUM blockers.** No commit message.
- Counter: **B2 0/3** corrective/re-review rounds consumed. This is the initial independent B2 review; the interruption and initial review consume no remediation round. Antigravity round 1 is next. B1 remains published/CLOSED with its separate 3/3 history preserved.
- Resume: the review was interrupted before a formal B2 report existed. On resumption, HEAD, all **19** input hashes and protected B1/config inputs matched the saved review inventory. Valid completed checks were reused, not rerun. Interruption itself is neither PASS nor FAIL.

## User requirement and simplest-mechanism check

The submitted change reuses the existing Codex prefix rules, Antigravity classifier/protocol and published B1 capture. Adding staging denial and native executable-name variants is appropriate; no new production component/dependency or B1 source change exists. Passing parser/unit tests do not satisfy the contract's mandatory native-client gates. The claimed all-PASS handback does not match observed Codex execution or the available Antigravity evidence.

## Finding register

| ID | Severity | Blocking? | Evidence / smallest correction | Status |
| --- | --- | --- | --- | --- |
| B2-F01 | **HIGH** | **Yes** | Actual Codex session dispatches required option/wrapper deny probes to Git; implement a supported narrow Codex-side guard and verify the real client, or return the exact unresolved Owner disposition. Correct overclaims. | OPEN |
| B2-F02 | **MEDIUM** | **Yes** | Actual Antigravity hook loading, payload and applied decisions remain unverified; supplied tests launch Python manually. Supply real-client evidence or report the native limitation instead of PASS. | OPEN / mandatory NOT_VERIFIED |
| B2-F03 | **MEDIUM** | **Yes** | Malformed/missing command arguments return `allow`; validate known run-command input before classification and use the existing conservative response without overrides. | OPEN |
| B1-L01; MA-A03–MA-A06 | LOW | No | Preserve existing milestone/audit backlog and historical evidence. B2 does not close or promote these findings. | OPEN / BACKLOG |

### B2-F01 — Codex option and Windows wrapper forms reach Git

- **Exact locations:** `.codex/rules/owner-git.rules:14–16` adds token-prefix wrapper rules; `.codex/README.md:6–7` claims verified wrapped interception and suggests Antigravity covers Codex gaps. `b2-implementation-handback-2026-10-26.md:27–32` and sections 3–4 mark incomplete native coverage PASS.
- **Actual native reproduction:** `native.py` invokes installed **codex-cli 0.164.0** using `exec --ephemeral --json -C <project>` with existing settings; no rule-file injection, trust bypass or permission/model override. Existing project trust is independently confirmed as `trusted`. The prompt restricts work to three help/read-only commands in a fresh isolated fixture. `native-client.stdout.jsonl:6–9` records real `command_execution` starts/completions for `git -C '<fixture>' add -h` and `cmd.exe /c "git add -h"` through the normal PowerShell tool entry. Both return **exit 1 with Git add usage output**, demonstrating execution, not guard rejection. The benign `git status --short` control executes with exit 0.
- **Supporting parser evidence, separately classified:** the direct `git add -h` prefix matches `forbidden`. Global `-C`/`-c`, absolute Git executable, cmd single-string payload, PowerShell single-string payload and nested forms have **no matching rule** in the reviewed file. Only tokenized `cmd.exe /c git add -h` matches its new prefix. These explicit `execpolicy check --rules` results are parser evidence; they do not prove auto-loading or deny enforcement.
- **Impact / approved contract:** B2-01/03/06 require representative native paths to be denied or remain explicitly unresolved. Help-only probes have no staging effect, but the required family interception fails. Antigravity's evaluator does not protect a Codex command which never invokes that hook. The handoff forbids marking an uncovered mandatory path PASS merely by documenting the gap.
- **Smallest correction:** use the installed client's supported project-local mechanism for these demonstrated forms, retaining benign work, current trust and permissions. The contract conditionally permits a thin native adapter reusing the existing evaluator only after demonstrated need and compatible trust/schema proof; no blanket shell prohibition, replacement framework or global change. If scope cannot resolve the native limitation, record the precise remaining cases for Owner disposition. Update README/handback claims to the actual supported paths.
- **Closure proof:** replay these same help-only native cases with real pre-dispatch rejection/tool evidence, plus approved representative options/absolute/PowerShell/nested and benign controls. Distinguish model refusal, parser result, execution failure and actual guard denial. An outer Codex process exiting 0 does not certify each tool-policy case.

### B2-F02 — actual Antigravity PreToolUse behavior is not established

- **Exact locations:** `.agents/hooks.json:9` changes relative script resolution; `.agents/hooks/test_repository_safety.py:239–321` constructs JSON and launches Python directly. Handback `:18, :31` asserts an Antigravity CLI version and marks native hook invocation PASS using those test names.
- **Reproducible evidence:** inspect the cited handback row and tests: `test_hook_cli_payload_schemas_and_malformed_input` mocks stdin/stdout, and `test_hook_process_invocation_from_root_and_scoped_cwd` directly calls `sys.executable -c ...` from `.` and `.agents`. Neither invokes the actual Antigravity client or proves it consumed the returned decision. The cited receipt contains a unittest run, not a native client hook trace. No supplied evidence establishes actual client payload, loaded customization, interpreter/cwd, deny/force-ask application or client benign/UI controls. No `antigravity` executable resolves on the review PATH; this alone does **not** prove the app is unavailable.
- **Impact / approved contract:** mandatory B2-05 and the related actual-client parts of B2-04/07/10 remain **NOT_VERIFIED**. Manual subprocess path/protocol tests are useful and passed, but cannot certify native loading or enforcement. The stated version/path must be grounded in an actual supported client, not inferred from another process or an invented CLI.
- **Smallest correction:** retain the passing unit/manual evidence under its correct label and capture a real supported Antigravity invocation: version/tool path, hook loading and interpreter/cwd, real payload, response and applied allow/deny/force-ask behavior with no override on denial/ask. Preserve the finite safe-probe boundary. If the installed client cannot supply the mandatory proof within existing local controls, report the exact limitation and Owner decision; no installation, trust bypass or global settings change is authorized.
- **Closure proof:** native trace showing the configured hook invoked for a safe negative and benign control, actual response consumption and required supported forms; include the short client-output/control sample. Do not substitute the same Python unittest invocation.

### B2-F03 — malformed run-command arguments fail open

- **Exact locations:** `.agents/hooks/repository_safety.py:458–469` stringifies the name and substitutes `{}` for malformed arguments; `:404–407` turns missing/non-string command input into an empty/arbitrary string which can classify `allow`. Added coverage at `test_repository_safety.py:239–269` covers malformed top-level JSON but not these argument paths.
- **Reproduction:** `probe-retry/focused-probe-results.json` retains exact input/output. For known name `run_command`, `args: "git add ."`, `args: ["git", "add", "."]`, an argument dictionary without either recognized command key, and `CommandLine: 123` each produce exit **0**, JSON **`{"decision":"allow"}`**. The valid denied control returns deny; the valid status control returns allow with its narrow override.
- **Impact / approved contract:** unknown/malformed payload shape can be classified as successfully safe, violating the explicit fail-closed input requirement. This is a demonstrated policy-classification defect; it is not a claim that the actual client accepts every malformed tool argument or has already staged files.
- **Smallest correction:** validate the actual supported tool-name/argument schema and require a usable nonempty string command for the matched run-command tool. Missing/malformed/unrecognized command payload must take the existing conservative response with no permission override, not silently normalize to an allowed empty command. Keep valid supported aliases and benign behavior.
- **Closure proof:** add focused entrypoint regressions for malformed argument type, missing/invalid command and valid native payload/control cases. Assert conservative non-allow output and absence of overrides; verify actual response semantics through F02's native proof.

## Acceptance matrix

| Case | Independent result / limit |
| --- | --- |
| B2-01–03, B2-06 | **FAIL / NOT_VERIFIED:** real Codex option/cmd execution disproves claimed coverage; remaining required direct/absolute/PowerShell/nested auto-loading/denial cases are not proven by parser checks or model narration. F01. |
| B2-04 | **PARTIAL:** actual Codex status control passes; focused tests pass directly through B1. Actual-client diff/test and Antigravity benign-path proof are incomplete. F01/F02. |
| B2-05 | **FAIL / NOT_VERIFIED:** direct hook controls/manual cwd tests pass, malformed arguments allow, actual Antigravity invocation/decision application unverified. F02/F03. |
| B2-07 | **PARTIAL:** concise Codex narration and saved command output observed; model verbosity/client UI distinction is stated accurately. Actual Antigravity sample remains unverified. F02. |
| B2-08–09 | **PASS within tested scope:** selected published B1 output/error/collision/privacy tests pass; B1/config inputs and live index/refs unchanged; fixture retains unborn HEAD, no index, refs or remotes. No historical-log equality scan claimed. |
| B2-10 | **FAIL:** mandatory missing/unsupported paths were labelled PASS rather than unresolved native limits/Owner disposition. F01/F02. |

## Verification and evidence reuse

Evidence root, created before interruption and retained: `logs/maintenance-b/b2-codex-review-3bab557478a04982b72d8c4c06f4e0d6/`. `resume-verification.json` confirms HEAD/input applicability on resumption. No completed runtime check was repeated after resumption.

| Check | Actual independent result |
| --- | --- |
| Published B1 runner, normal 300s: `python -B -m unittest discover -s .agents/hooks -p test_repository_safety.py -v` | **16/16 PASS**, exit 0; tests **0.383s**, wrapper **1.047s**, outer **1.203s**. `hooks.json`, raw streams and supervisor metadata. |
| Published B1 runner, normal 300s: selected tests B1-07/10/11/19/20 exactly as approved contract | **5/5 PASS**, exit 0; tests **3.846s**, wrapper **4.531s**, outer **4.734s**. `b1-output.json`, raw streams and supervisor metadata. |
| `focused-probes.py` retry through B1 | Completed exit 0, outer **1.718s**. Explicit rule-check and real hook-entrypoint results in `probe-retry/focused-probe-results.json` and individual parser files. Classification failures are F01/F03, not a successful acceptance result. |
| Actual Codex safe native probes through B1 | Codex/driver exit 0, wrapper **52.094s**, outer **52.281s**. `native.json`, `native-client.stdout.jsonl`, stderr, exact prompt and `native-events-summary.json`. Individual tool results establish F01 despite the outer PASS receipt. |
| Static/scope checks | AST parsing and `git diff --check` pass; index/excluded-scope checks exit 0; **85** local Markdown links resolve before report writes. `prewrite-checks.json`, input hashes and fixture-before evidence retained. |

Each verification used accepted B1 normal profile <=300s with independent 320s outer supervision; no outer timeout occurred. Python is `C:\msys64\ucrt64\bin\python.exe` 3.11.7 / native Windows; Codex is installed `0.164.0`. Approval remains `never`, sandbox `danger-full-access`, model verbosity `low`; no model/trust/permission override was passed. Existing trusted-project setting was inspected without modification or disclosure of unrelated user config.

The first reviewer parser probe stopped on a case-insensitive Windows filename collision between `-C` and `-c` labels. Its failed receipt/script/output remain intact; the corrected fresh `probe-retry/` paths use distinct names. This reviewer fixture error is not an implementation finding or remediation round.

Supplied Codex negative traces contain agent messages reporting automatic approval rejection, without corresponding negative `command_execution`/policy records in their cited JSONL. They are retained as reported outcomes, not upgraded into verified rule loading. Supplied Antigravity unittest/manual-cwd results remain credited at that level. Official [Codex rules guidance](https://learn.chatgpt.com/docs/agent-configuration/rules) describes trusted project loading, argv prefixes and specific shell splitting; it does not prove these Windows forms. [Codex hook guidance](https://learn.chatgpt.com/docs/hooks) describes a separate native denial schema/trust boundary; Antigravity response fields cannot be assumed effective in Codex.

## Changed-file inventory, coverage and boundaries

All **19 submitted paths** were inspected: 16 tracked modifications and three untracked documents. Exact input paths/hashes are in `review-inputs.json`:

- Hook/policy: `.agents/hooks.json`, `.agents/hooks/repository_safety.py`, `.agents/hooks/test_repository_safety.py`, `.codex/rules/owner-git.rules`, `.codex/README.md`.
- Instructions: `.agents/README.md`, `.agents/rules/backend-python.md`, `.agents/rules/tests.md`, `.agents/skills/antigravity-implement-handoff/SKILL.md`, `AGENTS.md`.
- Routing/workflow: `docs/implementation/handoffs/ACTIVE.md`, `handoffs/README.md`, `docs/workflow/current-owner-delivery-policy.md`.
- Maintenance scope: README, slice-b1-watchdog, slice-b2-tooling, B1 owner closeout, B2 executable handoff and B2 implementation handback.

Recursive changed hook/rule folder inspection found only the two existing Python files and existing rule file, with no new production package/helper/dependency. Source hashes: hook `67a748b1814ed546cfdc7bcb5a8e17c45d0061b8642fcd76c46d6173a61d5485`; tests `be7d81a8a41a748809af295563699ca0bb1bdc962c1f4576fa5d5fc6c9c579f4`; hook config/rule identities are in `prewrite-checks.json`.

Shared risk-applicable coverage: **PASS 1, 3, 5, 7–9, 14, 18, 23, 25, 29** for scope/ownership, narrow reuse, structure, retained output/privacy/resource measurements, unchanged dependencies and hygiene. **FINDING 2, 10, 12, 16, 21–22, 26–28** for demonstrated guard/schema failures, mandatory native proof and inaccurate acceptance claims; existing LOW backlog remains separate. **N/A 4, 6, 11, 13, 15, 17, 19–20, 24**: no application module, ADR-0016 HTTP envelope, authentication, persistence or RAG-quality capability changes. Native Antigravity enforcement remains mandatory NOT_VERIFIED, not N/A.

B1 runner/tests match their published accepted hashes and are unchanged; no B1 reopening/re-review. Live index bytes/refs and fixture state are unchanged in saved checks. No actual staging, commit, push, tag, PR, destructive action, global edit, Java/RAG suite, Graphify rebuild or warning-free IDE claim occurred. Historical logs/reviews remain preserved. Review writes consist only of this report, current lifecycle/status notices and fresh ignored evidence; implementation corrections are left to remediation. B3/Phase 19 are not authorized; indexing remains INACTIVE.

## Next action

**CHANGES_REQUESTED; B2 counter 0/3.** In Antigravity run **`/antigravity-implement-handoff`** for **round 1**, limited to B2-F01–F03 and truthful evidence/claim updates under the unchanged contract. Then mandatory **`/antigravity-self-review`**, followed by independent **`$codex-final-review`**. If a mandatory native path cannot be supported within the approved controls, return its exact Owner decision instead of fabricating PASS or expanding scope. No commit message while blockers remain.
