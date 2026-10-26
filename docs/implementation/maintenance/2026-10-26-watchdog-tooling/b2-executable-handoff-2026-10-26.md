# Executable slice handoff — Maintenance B2 CLI Output & Git/Hook Enforcement

- **Handoff:** `MAINT-B2-CLI-GIT-HOOK-ENFORCEMENT` / `READY_FOR_IMPLEMENTATION`, 2026-10-26. [ACTIVE](../../handoffs/ACTIVE.md) owns lifecycle and the B2 remediation counter.
- **Authority:** Owner's explicit resumed B2 handoff request, [approved Maintenance B scope](README.md), [B2 requirement inventory](slice-b2-tooling.md), [READY_FOR_HANDOFF preparation](preparation-review.md) and its [independent review](reviews/2026-10-26-maintenance-b-independent-pre-handoff-review.md). Material scope decisions are already approved.
- **Entry:** clean local/remote `main` `a4cf43505ab48198e22799fecf5098c5c6870a79`. [B1 closeout](b1-owner-closeout-2026-10-26.md) resolves its prior live handoff after independent acceptance and actual Owner publication; no B1 reopening.
- **Implementer:** Antigravity `backend-implementer`, `/antigravity-implement-handoff` and mandatory `/antigravity-self-review`. **Independent reviewer:** Codex `$codex-final-review`, B2 slice review. Initial B2 remediation **0/3**; B1's historical **3/3 exhausted** remains unchanged.
- **Skills:** `bounded-command-execution`, `reuse-and-consistency`, `pragmatic-solid-design`; `openai-docs` for installed Codex behavior. Java/JPA/RAG/domain skills, new agents and delegation are unnecessary for these existing tooling files.
- **Execution:** accepted B1 supplies default 300s ordinary cap, at most 2100s only for separately approved eligible full suites, diagnostics and protected output/receipts. **Every B2 verification command here uses normal profile at most 300s.** No actual extended application run is approved.

## Entry facts and reuse

Source inspection found existing Codex direct `forbidden` rules for staging/publishing, Git destructive operations and GitHub publication; do not duplicate those rules merely to increase their number. The Antigravity hook's `_DENIED_GIT_SUBCOMMANDS` currently omits `add`; it recognizes supported Windows wrappers/options and returns its existing `decision`/`permissionOverrides` protocol through `build_hook_output`. Its configured command is `python hooks/repository_safety.py`, with working-directory/interpreter resolution still requiring native-client proof. `main` currently expects `toolCall.name/args`; that is a source fact, not proof of the installed client's actual payload.

Planning observed **codex-cli 0.164.0** via `--version` and inspected its command help. `codex` resolves to the installed OpenAI binary; no `antigravity` command was found on PATH. That does not prove the Antigravity app is absent or unsupported. No actual policy/hook interception, trust loading or B2 runtime experiment was performed during handoff creation. Existing hook parser tests are baseline context, not B2 acceptance.

Official [Codex rules guidance](https://learn.chatgpt.com/docs/agent-configuration/rules) describes trusted project-layer loading and prefix matching; it does not establish this Windows installation's effective interception. The documented shell splitting examples concern Unix shell forms; do not infer equivalent PowerShell/cmd coverage. [Configuration precedence](https://learn.chatgpt.com/docs/config-file/config-basic) also makes trust and invocation overrides relevant. Record actual effective, non-sensitive settings; never dump user configuration/authentication or assume a file was loaded because it exists.

## User-visible acceptance (MUST)

| ID | Scenario/input | Expected observable result | Minimal test / reusable implementation |
| --- | --- | --- | --- |
| B2-01 | Actual trusted Windows Codex project session | Identify CLI/version, relevant effective config/trust, loaded rule identity and tool entry path. Observe real tool-policy denial before dispatch; an explicit `execpolicy check --rules` pass alone is insufficient. | Existing `.codex/rules/owner-git.rules` + safe runtime matrix below; preserve actual rejection/tool trace. Model refusal or executable-not-found is not interception proof. |
| B2-02 | Direct staging/publishing families: git add/commit/push/tag, gh pr create/merge | Owner-only operations denied in the supported client paths; zero actual staging, commit, push, tag or PR effects in any repository. Read-only simulation/help probes establish interception safely. | Extend hook denial set/tests; Codex native direct rules; source/policy literals plus help/inert runtime probes, never real publishing. |
| B2-03 | Git global options, absolute paths, cmd/PowerShell wrappers and one nested wrapper | Representative supported forms receive intended deny; preserve benign quoted Windows paths. A missed/unsupported form is an explicit gap, never a universal protection claim. | Reuse `_command_segments`, `_wrapper_payload`, `_git_subcommand`, existing quoting/CRLF/depth tests; actual-client probes, separately classified. |
| B2-04 | git status, git diff --stat and focused tests | Benign work runs with the intended existing approval/sandbox policy and without new unnecessary prompts. No blanket denial of shells/Git/Python to make negatives pass. | Both actual clients run status/diff and a short focused test command; evaluator allow regressions. |
| B2-05 | Actual Antigravity PreToolUse invocation | Prove the real client invokes this hook with its real payload, resolves Python/script correctly even with spaces in paths, and applies deny/allow/force-ask semantics where supported. Denied or ask-required operations receive no permission override. | Existing hooks.json, main/build_hook_output; native trace plus matching parser/protocol tests, including malformed input and root/scoped working directory. Unit invocation alone is insufficient. |
| B2-06 | Confirmed uncovered publishing path | Apply the smallest supported project-local correction and retest. If it needs forbidden global settings, broader permissions or a replacement framework, report exact failure/mitigation and stop for Owner disposition. | Reuse native rules/hook and common evaluator; no shadow policy engine or unsupported claimed guarantee. Mandatory gaps remain blocking until resolved/disposed. |
| B2-07 | Progress narration/diff output and client-generated UI | Short status, named paths/diff-stat, first safe actionable error and evidence link. Distinguish agent-controlled output from client-generated Ran/tool/diff previews; no promise to suppress all UI. | One short real-client sample each; existing low verbosity and narrative instructions. No huge pasted patches, replacement client or broad output suppression. |
| B2-08 | Large stdout/stderr or early nonzero test exit | B1 capture retains complete new output/receipt with bounded console and truthful nonzero result; no lost test result or duplicate capture pipeline. | Published runner, existing selected output/receipt regressions and a small synthetic failure, not a new runner. |
| B2-09 | Prior evidence, sensitive canaries and current permissions | Keep prior logs byte/path intact; fresh ignored evidence only. No raw argv/environment/credentials in receipts or private payload in summaries; no broadened permissions or actual Owner-only actions. | New-file collision/canary evidence, scoped diff and fixture state checks; reuse B1 guarantees/hashes. No historical-log cleanup/scan is required. |
| B2-10 | Missing/version-incompatible client or unsupported hook/trust mechanism | Exact native limitation, evidence and narrow supported mitigation. Mark required runtime proof NOT_VERIFIED/BLOCKED rather than pretending parser success certifies it. No global client settings or trust bypass. | Actual version/help/runtime evidence; explicit stop path. Do not install/upgrade clients or change Owner-global configuration by inference. |

## Simplicity decision and burden

| Decision | Selected minimum / excluded alternative |
| --- | --- |
| KEEP / REUSE | Existing Codex rule file/config, Antigravity hook/evaluator/tests, B1 capture and existing instruction entry points. |
| SIMPLIFY | Add missing direct add denial and small actual-protocol/path corrections; retain one command classifier. Use a finite probe table, not a shell interpreter/security product. |
| KEEP | Real Windows-client loading/decision proof, benign regression and retained full logs: these are mandatory preparation runtime risks, not extra application requirements. |
| REMOVE from claims | Parser-only or model-refusal proof of enforcement; assertions that verbosity hides client UI; claims of airtight protection under full access. |
| DEFER | Arbitrary scripts/aliases/encoded/reflected commands, connector/GUI-wide interception, system security hardening and all application suites. Record such coverage limits without inventing a broader stack. |

Expected work is a small existing rule/hook/test/instruction delta and a finite native probe ledger. Native availability/trust is the principal uncertainty, not a reason for a daemon, new agent, package, general shell parser or repeated full suites. Try one supported scoped correction for a reproduced gap; if it cannot work within the approved controls, return the concrete blocker instead of redesigning.

## Precise change map and implementation path

| Allowed target | Outcome / boundary |
| --- | --- |
| `.codex/rules/owner-git.rules`, `.codex/README.md` | Reuse valid direct restrictions; add only demonstrated supported gaps/examples and truthful Windows loading/coverage instructions. No blanket shell prohibition. |
| `.codex/config.toml` **conditional only** | Change only after proving existing rules cannot satisfy an approved requirement and documenting the compatible project-local fix. Preserve model selection, approval_policy=never, danger-full-access and existing tool/MCP permissions. No global changes. |
| `.agents/hooks.json`, `.agents/hooks/repository_safety.py`, `.agents/hooks/test_repository_safety.py` | Missing add denial; actual client schema/response/path resolution; finite supported wrapper tests; keep shared evaluator/build_hook_output rather than a second classifier. |
| `.agents/README.md`, `.agents/rules/tests.md`, `.agents/rules/backend-python.md`, `.agents/skills/antigravity-implement-handoff/SKILL.md`, `.codex/README.md` | Only necessary invocation/output/verified-coverage guidance. No unrelated policy, model, sandbox or skill rewrite. |
| Maintenance B scope/status/handback + ACTIVE; root AGENTS first current notice; handoff workflow README | Current B2 lifecycle/evidence/limitations; preserve dated reviews/B1 contract and ledger. |
| New ignored `logs/maintenance-b/b2-*` | Full new client/probe/test output, isolated fixtures, receipts and concise evidence ledger. Keep all existing logs and prior attempts. |

**B1 runner/tests are read-only inputs**: no B1 repair or timing/capture redesign in this handoff. No new committed helper, Codex hook file, dependency or test framework is authorized; session-only probe helpers/fixtures may live in fresh ignored evidence paths. If a new persistent file is unavoidable, justify it for an amendment before adding it.

1. Recheck entry/source drift, approved scope and actual clients. Keep the validated B1 hashes from its closeout. Identify real config/trust/tool paths using supported non-mutating introspection; do not edit global trust/auth settings or use ignore-rules/skip-hook-trust/permission-bypass flags for acceptance proof.
2. Reuse rule checks as **parser evidence only**, then observe auto-loading in a real trusted Codex session. Run probe commands with an explicitly verified disposable fixture working directory while preserving the trusted project layer; do not silently assume a newly created fixture repo is trusted. Record which layer/rule/decision was actually used. An explicit rules-file check cannot substitute for this.
3. Reuse Antigravity's command evaluator; add add denial, retain relevant destructive/frozen-path safeguards and benign quoting behavior. Deny/ask-required results cannot carry broad permissionOverrides. Unknown/malformed real payload must not fall through as successfully protected allow. Match the installed client's documented schema; do not infer it from the current test harness or another product's hook API.
4. Fix hook command resolution using the actual client's supported project/script-root mechanism and resolved Python, correctly quoting spaces. Verify through the real client from its actual cwd; don't merely run the file manually and declare hook loading PASS.
5. If Codex native prefix rules demonstrably miss an in-scope form, first use their supported narrow mechanisms. A conditional project-local native hook via inline `.codex/config.toml` may reuse the **same** existing Python evaluator only if this proves necessary, is supported by the installed client and preserves permissions. Add a thin native input/output adapter in that existing file, with tests; do not return Antigravity response fields to Codex and call them effective. Official [Codex hook guidance](https://learn.chatgpt.com/docs/hooks) documents a distinct native PreToolUse denial schema and separate hook trust. Runtime invocation/denial and trust still need proof. No new hook file, new framework, auto-trust/global edit or trust-bypass flag is allowed. If native trust/coverage cannot be demonstrated in scope, follow B2-06/10's blocker route.
6. Keep console/narration concise using B1 output paths and bounded summaries. Preserve native exit/errors in raw evidence. Do not swallow stderr, suppress test failures or change the client to remove rendered tool events. Map exact observed supported/unsupported paths in the handback, not an unbounded threat checklist.

## Safe finite probe matrix

**Zero actual staging/publishing/destructive operations are authorized, including fixture commits.** Unit/policy tests may contain mutating command strings as data. Native runtime probes use **help-only/read-only argv** or inert stand-ins that cannot call real Git/GitHub or access the network. A trusted client's actual rejection and tool trace establish enforcement; refusal by its model, missing executable, fixture permissions or lack of credentials do not.

Create a fresh isolated fixture under the new B2 evidence root. Verify its resolved absolute path is inside that root, outside the live `.git`, with no live-repo gitdir/worktree link and no remotes. `git init --initial-branch=main` is allowed for fixture metadata only: no initial add/commit/tag. Never copy credentials, remote URLs or the live .git. Retain fixtures/evidence; no recursive historical-log deletion. Check fixture index/HEAD/refs and the live repository source/index before/after to prove no Owner-only effects.

| Case | Policy/unit input | Actual-client safe probe / expected observation |
| --- | --- | --- |
| Direct Git families | git add ., git commit -m test, git push origin main, git tag v-test | `git add -h`, `git commit -h`, `git push -h`, `git tag -l` **in fixture only**: same restricted families, no staging/commit/network/tag creation. Actual guard rejection before dispatch is required for a runtime denial PASS. |
| GitHub PR families | gh pr create, gh pr merge 1 | `gh pr create --help`, `gh pr merge --help`: no PR/network mutation. A missing gh command is not proof of rejection; use an explicitly inert test stand-in if necessary and label its coverage. |
| Options | git -C <fixture> add ., git -c <safe synthetic key=value> commit -m test | Same options with `add -h` / `commit -h`; keep fixture path explicit. No global Git config changes. |
| Absolute executable | quoted resolved git.exe path + add/commit | Same resolved executable with help-only arguments. Paths containing spaces must survive parsing; do not trust basename-only unit tests as native proof. |
| Windows wrappers | cmd.exe /c git push; powershell.exe -NoProfile -Command git tag v-test | Help/list variants above through actual cmd/PowerShell, in fixture. Record literal dispatched shape and client rejection or gap. |
| One supported nested form | powershell -> cmd -> git commit | Same help-only variant; test current bounded recursion without building a generic shell language. |
| Compound/quoting | existing CR/LF, separator and ambiguous-quote fixtures | Parser/protocol data only for mutating forms. Native sample can combine safe status/help forms. Malformed input must produce the actual supported conservative response, no unintended permission override. |
| Benign/control | git status, git diff --stat; quoted benign Windows path; short test | Real client allows the intended read-only/focused work. Prove the policy source was active and would reject a matching negative; instruction compliance alone is not a guard test. |

Use an inert stand-in only when its dispatch/marker cannot perform a real operation; record its resolved executable and that limitation. Help-only samples prove interception for their matching command family, not every possible argument. Record untested dynamic scripts, aliases, alternate connectors/GUI and bypass flags as coverage limits; do not test them against the live repository or claim global prevention. A failed mandatory supported form stays unresolved under B2-06.

## Focused verification and exact commands

Use the accepted B1 runner from repository-root PowerShell. Resolve Python once; no client/runtime install or version upgrade is approved. Each command gets a fresh receipt path, normal profile <=300s and host supervision allowing at most 20s additional cleanup. Poll/yield for updates; no unbounded client wait. Client startup/auth/trust failure is a factual blocker, not a cue for repeated unchanged retries.

```powershell
$b2Python = (Get-Command python -ErrorAction Stop).Source
$b2RunDir = Join-Path 'logs/maintenance-b' ('b2-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $b2RunDir -ErrorAction Stop | Out-Null
& $b2Python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $b2RunDir 'hook-tests.json') -- $b2Python -B -m unittest discover -s .agents/hooks -p test_repository_safety.py -v
& $b2Python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $b2RunDir 'b1-output-regressions.json') -- $b2Python -B scripts/test_run_bounded_command.py BoundedCommandTests.test_b1_07_child_error_exit_code_preserved BoundedCommandTests.test_b1_10_no_clobber_and_no_canary_leak BoundedCommandTests.test_b1_11_large_streaming_unicode_and_long_line BoundedCommandTests.test_b1_19_remedy_f06_small_live_output_chunkwise_observed BoundedCommandTests.test_b1_20_remedy_f05_no_canary_leak_in_console -v
```

Read-only CLI identification/rule syntax inputs (use installed help, retain complete outputs through B1 capture; these are not real interception proof):

```powershell
codex --version
codex execpolicy check --pretty --rules .codex/rules/owner-git.rules -- git add .
codex execpolicy check --pretty --rules .codex/rules/owner-git.rules -- git status
```

Apply the rest of the finite matrix as check inputs and actual-client safe probes. Use supported native client invocation with the Owner's existing model/approval/sandbox settings and a narrow probe-only prompt; no delegated implementation. For Codex, installed `exec --json` can provide structured traces; its session must demonstrably load the trusted project rules without an explicit --rules injection. Antigravity native evidence must show its actual tool payload/decision and hook execution context. Do not invent an antigravity CLI/flag merely because it was absent from PATH during planning. Save exact benign probe commands and relevant sanitized context in the new evidence ledger, not raw argv/env in runner receipts. No secrets/auth/config dump or external publishing tools.

After convergence, run the two focused commands once, preserve parser and native client evidence, `git diff --check`, JSON/TOML/rule syntax checks for changed files, named-path diff inspection and Markdown-link checks. Reuse B1's unchanged independent native/30-test evidence instead of redoing B1 acceptance. No Java/RAG suite, full application gate, Graphify rebuild or extra staged-tree review.

## Definition of done and handback

1. Write `b2-implementation-handback-2026-10-26.md` here (or actual execution date): B2-01–10 -> source/test -> actual client/version/tool path -> observed decision/side effects -> evidence -> PASS/FINDING/NOT_VERIFIED/limit. Separate unit/parser, manual script invocation, real hook loading and real runtime interception.
2. Keep full new output/receipts/client traces/fixture state under `logs/maintenance-b/b2-*`; concise chat references only. Trace sensitive-input tests with synthetic canaries; no historical evidence alteration. Record unchanged B1 source hashes and scope exclusions. If any B1 defect is discovered, report it separately rather than changing B1 here.
3. Mandatory `/antigravity-self-review`: requirements, reuse/KISS, exact supported paths, benign behavior, no broadened permissions, safe probe isolation, privacy/log retention, focused results and honest limits. <=12 short chat lines plus links. Self-review is not independent acceptance.
4. Update ACTIVE to `IMPLEMENTED_AWAITING_CODEX_REVIEW` with evidence and initial B2 **0/3** counter. Codex `$codex-final-review` independently reviews B2. CRITICAL/HIGH/MEDIUM or mandatory NOT_VERIFIED blocks; LOW/INFO joins the existing milestone/audit backlog. B1-L01 and MA-A03–MA-A06 remain open, not promoted or closed by B2.
5. At most three B2 Antigravity correction + independent re-review rounds, separate from B1's exhausted ledger. No automatic takeover/fourth round. Owner alone stages/commits/pushes after acceptance. Then brief B2 publication closeout and **one Maintenance B final review**, reusing valid B1/B2 evidence; this handoff does not perform that final review.

## Non-goals, applicable risks and stop conditions

No B1 code/test changes, permission/sandbox/model change, global settings/trust edit, client reinstall, new agent/daemon/service/dependency, replacement CLI, general parser, application/RAG/model/schema/migration or frozen P17/P18 edit. No actual stage/commit/push/tag/PR or destructive probe anywhere. Phase 19 remains NOT AUTHORIZED; indexing INACTIVE; live originals DISABLED/DEFERRED.

Applicable preparation coverage: ownership/intent/traceability, simple tooling reuse, privacy/retention, error handling, bounded output/resources, actual Windows-client security integration, tests, dependencies, logging and current docs. Native interception/resource/UI claims were deferred runtime evidence and remain mandatory here. Java module/HTTP/ADR-0016 envelope, database/transactions, authentication and RAG quality are N/A because none changes. This rubric creates no extra features.

No known material preparation choice is open. If actual installed clients cannot enforce an approved mandatory path without a forbidden global/trust/permission change or new persistent component, stop with exact reproduction, supported minimal mitigation, remaining B2 IDs and the concrete Owner decision needed. Do not waive the case, hide failed evidence, broaden scope or reopen B1 to force READY. All B2 runtime acceptance is NOT_STARTED/NOT_VERIFIED at handoff creation.
