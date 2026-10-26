# Executable slice handoff — Maintenance B1 progress-aware watchdog

- **Handoff:** `MAINT-B1-PROGRESS-WATCHDOG` / `READY_FOR_IMPLEMENTATION`, 2026-10-26. Lifecycle status and remediation counter live in [ACTIVE](../../handoffs/ACTIVE.md).
- **Authority:** [Owner-approved scope](README.md), [B1 preparation requirements](slice-b1-watchdog.md), [READY_FOR_HANDOFF preparation review](reviews/2026-10-26-maintenance-b-independent-pre-handoff-review.md), [current Owner policy](../../../workflow/current-owner-delivery-policy.md), and Owner's handoff-creation invocation. No unresolved material preparation decision.
- **Entry:** published `main` `dfc0c2052ffd5272ba63e6529d0bb401f1e5c022`; accepted preparation is the only drift from Maintenance A `6fe8dbce24ae9ab43290d76172e2c39678246bbd`. Account for later drift before implementing.
- **Implementer:** Antigravity `backend-implementer`; independent reviewer: Codex `$codex-final-review`. Use `bounded-command-execution`, `pragmatic-solid-design`, `reuse-and-consistency` and mandatory `antigravity-self-review`. This is Python tooling, not a RAG pipeline; Java/JPA/domain/RAG-runtime skills and a separate Python agent are unnecessary.
- **Runtime truth:** all acceptance is `NOT_RUN / NOT_VERIFIED`. Existing runner uses fixed 1200s/2100s waits, inherited console output and replaceable receipts. This handoff does not implement or certify it.

## User-visible acceptance (MUST)

Preserve the approved B1 IDs. Every row needs source/test references and its actual result in the handback; counts alone do not prove coverage.

| ID | Scenario/input | Expected observable result | Minimal proof / reusable implementation |
| --- | --- | --- | --- |
| B1-01 | Ordinary child exits 0 in seconds; no timeout override | Default deadline 300s; return 0, terminal `PASS`, new receipt, short console/evidence paths. PASS only after exit and successful required capture/receipt. | Extend `test_success_receipt`; assert shipped defaults and CLI compatibility without waiting 300s. |
| B1-02 | Ordinary child alive at its deadline | Stop useful execution; finite diagnostics and owned-tree termination; `TIMEOUT`, exit 124. Execution and cleanup/grace durations separate. | Existing short timeout test; assert total upper bound and cleanup evidence. |
| B1-03 | Explicit approved full/large/complex verification with eligible profile | At most 2100s; shape and per-slice approval checked below. Reject arbitrary command/timeout bypass before launch. Quiet healthy work can continue within deadline. | Extend `is_full_suite`/`test_profile_guard`; invalid-input and injected-clock/profile tests. No actual application suite approved here. |
| B1-04 | Extended child silent for 300s but making attributable CPU/I/O/report progress | Bounded health/progress observations; continue, no HUNG or PASS. Progress never renews hard deadline. | Scaled timers; quiet CPU-work and controlled report-growth fixtures. |
| B1-05 | After inactivity, objective proof confirms owned-process hang/deadlock | Preserve positive proof; stop only owned tree early; `HUNG_CONFIRMED`, nonzero exit. Silence/sleep/low CPU/one unchanged sample is insufficient. | Structured positive/negative proof fixtures and bounded native-probe evidence below; distinguish injected from OS-observed proof. |
| B1-06 | Quiet child, unsupported/unavailable/ambiguous health evidence | `DIAGNOSTIC_INCONCLUSIVE` observation; continue until exit/deadline. Neither PASS nor an early-kill reason. | Denied/unsupported probes, unchanged samples and healthy sleep; later exit/cap stays effective. |
| B1-07 | Child writes error then exits, e.g. 7 | Prompt `COMMAND_FAILED`; retain child code and return it when representable. Safe error/category plus complete new log path; no inactivity delay. | Synthetic stderr/nonzero, exit normalization and launch-error tests; reuse receipt fields. |
| B1-08 | Owned parent -> child -> grandchild times out; unrelated sentinel | Stop owned descendants; sentinel survives. Verify native Windows PID lifetimes, not merely taskkill's status. Cleanup denied/unknown is explicit. | Reuse `test_timeout_terminates_owned_child_and_grandchild_only` and `process_running`; actual Windows execution mandatory. |
| B1-09 | KeyboardInterrupt/cancellation, unexpected monitor exception or capture failure | Finite owned cleanup, non-PASS receipt, no indefinite pipe/thread wait or leaked handles. Optional unavailable probe follows B1-06; unexpected monitor failure cannot silently disable deadlines. | Interrupt/exception injection, inherited-open-pipe and bounded reader-shutdown tests; sentinel survives. |
| B1-10 | Prior receipt/log exists, concurrent collision, sensitive argv/output canary | No pre-existing bytes/path overwritten, truncated, moved or deleted. Fail before launch or choose fresh auto path. No raw argv/env/private payload in receipt/console. | Exclusive reservation/collision/digest/canary tests; extend `write_receipt`, retain argv SHA-256 and safe metadata. |
| B1-11 | Large stdout/stderr, Unicode, newline-free long line, silent descendant | Complete raw streaming to new files, bounded buffers, concise console; no whole-stream accumulation or unbounded drain. Preserve saved error details. | Chunked multi-megabyte fixture, output integrity/size and bounded-buffer assertions; long-line/Unicode/quiet cases. |
| B1-12 | Invoker has earlier host/tool timeout | Report actual interpreter/Windows/host limits and polling method. Wrapper cannot outlive host by promise; scaled tests do not prove real 2100s operation. | Short isolated outer-timeout test with cleanup of its own tree; factual host/limits in handback. |

## Simplicity decision and burden

| Decision | Smallest adequate path |
| --- | --- |
| KEEP / REUSE | Existing wrapper/CLI, `is_full_suite`, `iso_now`, hashed-argv receipt, `kill_owned_tree`, native `process_running` and sentinel fixture. Extend unittest, no second runner. |
| SIMPLIFY | One monotonic deadline, chunked streaming, private monitoring/probe helpers and bounded native read-only observations. No supervisor framework or general deadlock debugger. |
| KEEP | Non-clobber evidence, conservative diagnosis, error/cancel cleanup and actual Windows integration: required retention/lifecycle fixes. |
| REMOVE from changed runner | Unbounded inherited raw console output and replacement of old receipts. No historical cleanup or replacement client. |
| DEFER | B2 client/rules/hook work, arbitrary extended profiles, application suites, new packages/services and monitoring infrastructure. |

Expected work: two existing Python files, short synthetic/native tests and minimal instruction corrections. Scaled timers keep focused verification within 300s; no real five-/35-minute sleep, Java/RAG workload or benchmark apparatus. A demonstrated platform gap needing a helper/dependency requires a narrow handoff amendment before adding it.

## Minimal implementation path and finite bounds

### Deadline, profiles and observations

1. Preserve argv-form `--profile`, `--timeout-seconds`, optional `--receipt`, and `-- executable args`. Before launch reject missing command, non-finite/nonpositive timeout and over-cap values. Defaults: normal **300s**, full-suite **2100s**. Shorter limits stay supported.
2. Reuse **full-suite as the only extended profile in B1**. Retain recognized executable families (Maven/wrapper, Python `-m pytest`, pytest) and meaningful suite-shape exclusions in `is_full_suite`. Shell/script/install/download commands are ineligible. No reason-only override. Future complex commands outside these shapes need an explicitly amended narrow guard/handoff.
3. Approval has two parts: the **invoking agent** checks an active approved handoff naming the long command/shape, cap and rationale; the **wrapper** enforces shape and cap. A profile/shape match alone is never Owner approval. **B1 approves no actual extended application command.** Test extended behavior with injected clocks/thresholds and controlled children, without a public production bypass or authorization service.
4. One monotonic execution deadline, never renewed. Check child exit/deadline at least every **0.5s**; probes/readers cannot block that loop indefinitely. Sample progress at **5s** intervals; first diagnostic due after **300s** without output or attributable progress, repeat at most once per **30s** of continued inactivity. No console-per-poll output. Override constants privately in tests only.
5. Keep output and meaningful progress separate: stdout/stderr bytes, attributable owned PID/tree CPU-time/I/O deltas, or metadata advancement of explicitly selected local report/log files. Never recursively scan the vault, arbitrary directories or historical logs. Another process's activity and the wrapper's own diagnostics/heartbeat are not child progress. If report paths are needed, one narrow repeated opt-in file option (e.g. `--progress-file PATH`), metadata only, with documented tests is permitted; no watcher service.
6. Diagnose process existence, output/file changes and available CPU/I/O/thread/wait evidence. Record availability/deltas. **Only positive evidence of an owned wait/deadlock cycle, or equally direct objective hang proof, may yield HUNG_CONFIRMED.** Repeated idle samples do not qualify. Prefer one bounded native read-only proof path, not a debugger/stack-dump tool. Test its classifier with structured proof/non-proof and the actual probe on a controlled native fixture where supported. Report injected, OS-observed and unsupported evidence separately. A mandatory B1-05 gap remains a blocker for Codex, never a fabricated PASS/N/A. Unavailable optional health data otherwise follows B1-06.
7. Diagnostic episode maximum **5s**, including probe subprocesses; before deadline also bound it by remaining execution time. At deadline take only a bounded final snapshot then terminate owned tree. Total post-execution diagnostic + termination + reap + output-drain grace is **at most 20s**: up to 5s diagnosis and remaining at most 15s cleanup. No stacked endless waits. Record execution/diagnostic/cleanup durations and survivor/unknown/tool limits separately.

### Output, evidence and lifecycle

1. Capture stdout/stderr in **chunks at most 64 KiB** to fresh files; no `readline`/`communicate` accumulating arbitrary output. Bound queues/error previews. Blocked writers, inherited pipe handles or reader/probe exceptions cannot defeat deadline/cleanup; close owned handles, bound joins and report incomplete capture.
2. Fresh run-scoped evidence under ignored `logs/`; validation uses `logs/maintenance-b/b1-<unique-id>/`. A pre-existing explicit receipt target fails before launch. Reserve/create exclusively; finalize without replacing an earlier/concurrent run's path, fixing current `os.replace` behavior. With omitted `--receipt`, auto-select a fresh evidence path and print it. Only this run's new files may grow; never mutate historical files even on failure.
3. Keep safe receipt fields: profile, cap, executable **basename**, argv SHA-256, UTC times, owned PID, exit/result and cleanup. Add bounded diagnostic availability/deltas, paths, durations and capture/cleanup completeness. No raw argv/env/command text/private payload/credentials or stack/report contents. Full raw child output stays in new ignored local logs. Console: one start, bounded diagnostic summaries, one result/evidence location. Use safe error category/code rather than blindly echoing stderr; save actionable details in the full log and point to it.
4. Terminal results: `PASS` only after zero exit and required capture/receipt; `COMMAND_FAILED` for child failure; `TIMEOUT`/124 at deadline; `HUNG_CONFIRMED`/nonzero on positive proof; `LAUNCH_ERROR`/127 for launch failure; explicit nonzero cancellation/internal-error result for those paths. Preserve native child code even if wrapper exit needs normalization. `DIAGNOSTIC_INCONCLUSIVE` is nonterminal. Evidence-write failure cannot become PASS; report failure and clean up owned children.
5. Reuse/strengthen owned-tree cleanup only as needed. No kill by process name/global Java/Docker/arbitrary PID targeting. Include descendant/sentinel checks. If root exits before descendants/pipe EOF, bound shutdown and report what was stopped/unverified; no universal OS containment claim. Probe/cleanup subprocesses are also owned resources with finite budgets.

## Precise change map and ownership

| Permitted targets | Required outcome / limits |
| --- | --- |
| `scripts/run-bounded-command.py` | Canonical timing, monitoring, capture, receipts and cleanup; private helpers in this file by default. |
| `scripts/test_run_bounded_command.py` | Extend five existing tests with short deterministic, native Windows and failure/retention/output cases; no second framework. |
| `.agents/skills/bounded-command-execution/SKILL.md` | Tested CLI/examples, caps, diagnostics and limits; fresh evidence paths; distinguish implementation from independent acceptance. |
| Minimal current instruction call sites listed below | Only stale executable watchdog statements/examples after source tests; B2 remains pending. No general governance/history rewrite. |
| This maintenance scope + ACTIVE | Acceptance evidence, concise implementation/self-review handback, lifecycle/counters; preserve dated preparation report. |
| New ignored `logs/maintenance-b/b1-*` | Full new output/receipts; no old-file mutation, staging or cleanup. |

Finite instruction list: `AGENTS.md` **first current notice only**; `.agents/README.md`; `.agents/agents/backend-implementer/agent.md`; `.agents/skills/antigravity-implement-handoff/SKILL.md`, `backend-testing/SKILL.md`, `codex-create-handoff/SKILL.md`, `codex-implementation-takeover/SKILL.md`; `.agents/rules/backend-python.md`, `tests.md`; `docs/workflow/current-owner-delivery-policy.md`, `agent-development-workflow.md`, `owner-phase-workflow.md`; **forward/current notices only** in `docs/workflow/rag-phase-delivery-guardrails.md`. Leave unrelated text and historical sections unchanged. Say B1 implemented/awaiting review until Codex accepts; do not declare all Maintenance B complete.

Java/application modules, HTTP DTO/exception/ADR-0016 envelope, schema/Flyway, package-tree and dependencies are untouched; those preparation dimensions are N/A. Applicable coverage: intent/traceability, tooling ownership/KISS, retention/privacy, recovery, resource bounds, Windows integration, tests, logging and current instructions. Runtime/process/resource risks deferred by preparation become **mandatory B1 evidence**, not inherited PASS. Actual-client interception is B2 work.

## Negative/failure tests and verification

Cover all twelve rows, reusing fixtures: invalid timeout/shape before spawn; quiet healthy/sleep/inconclusive vs positive proof; deadline dominance despite output/repeated diagnostics; early child failure; launch/probe/capture errors; interrupt/inherited pipes; native owned tree/sentinel; exclusive file collisions; synthetic private/secret canaries; Unicode/long lines/bounded storage; shorter outer timeout. Mocks prove decision logic, not native cleanup/probe behavior.

Run from repository-root PowerShell. At creation `python` resolved to `C:\msys64\ucrt64\bin\python.exe`, **3.11.7 / os.name=nt / sys.platform=win32**; `py` was unavailable. Recheck interpreter/native APIs at implementation and use the same resolved interpreter throughout. No runtime install/upgrade is authorized.

**Independent bootstrap:** develop with individual tests. After convergence run `python -m unittest discover -s scripts -p test_run_bounded_command.py -v` once under an independent session-only `Popen` harness with **180s** outer deadline, argv list, and stdout/stderr streamed to exclusively created new files. Keep its PID; on timeout/interruption/error stop only that launched tree with bounded Windows `taskkill /PID <owned PID> /T /F`, reap/close within **20s**, record failure and diagnose. Inner fixtures have shorter deadlines/finally cleanup. This harness must not depend solely on the changed runner's deadline; no new committed helper is necessary.

**Final command after bootstrap passes** (one focused suite, including native child/grandchild/sentinel case; no redundant identical native run):

```powershell
$b1Python = (Get-Command python -ErrorAction Stop).Source
$b1RunDir = Join-Path 'logs/maintenance-b' ('b1-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $b1RunDir -ErrorAction Stop | Out-Null
& $b1Python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $b1RunDir 'focused.json') -- $b1Python -m unittest discover -s scripts -p test_run_bounded_command.py -v
```

This exact shape is approved at **300s**, not extended. Use independent host supervision allowing execution plus at most 20s cleanup, polling/yielding for user updates. If the host ends earlier, record its actual limit; do not claim wrapper-only proof. Every failed attempt gets fresh evidence. No Graphify regeneration or Java/RAG suites for these two tooling files; they add no B1 acceptance evidence.

Before handback run `git diff --check`, review `git diff --stat`/changed paths, and validate changed relative Markdown links. Focused hygiene only; no staging review or publishing/enforcement probes in B1.

## Definition of done and handback

1. B1-01–12 matrix with source/test, actual command/exit/result/evidence and native-vs-injected limits; no mandatory row silently unverified. Short timing/buffer evidence replaces unapproved benchmark/full-suite demands.
2. New ignored run directory retains bootstrap/final stdout/stderr, receipts and scoped native/diagnostic evidence. Record interpreter/Windows/host, outer/inner limits, source identity/diff paths and actual totals. Collision-test before/after bytes prove preservation without scanning every historical log.
3. Write `b1-implementation-handback-2026-10-26.md` here (or actual implementation date): matrix, exact evidence/commands, self-review and remaining finding IDs. Invoke `/antigravity-self-review`; <=12 short chat lines plus links. This is not independent acceptance.
4. Set ACTIVE `IMPLEMENTED_AWAITING_CODEX_REVIEW`; initial counter **0/3**. Independent Codex B1 review follows. CRITICAL/HIGH/MEDIUM or mandatory NOT_VERIFIED blocks; LOW/INFO stays backlog. Prior `MA-A03–MA-A06` is neither imported as blockers nor closed. At most three Antigravity corrective + independent re-review rounds; no automatic takeover.
5. Owner alone stages/commits/pushes after `READY_FOR_OWNER_COMMIT`. **B2 starts only after independent B1 acceptance, actual Owner publication and its own handoff.** One Maintenance B final review follows both published slices.

## Non-goals, risks and stop conditions

No B2 `.codex` config/rules, Antigravity hook/protocol/client UI/enforcement changes or publishing probes. No agent staging/commit/push/tag/PR, new dependency/framework/agent/daemon/service/watcher, OS security change, general debugger, application/RAG code, model, schema/migration or unrelated refactor. Preserve frozen P17/P18 and historical FAILED/TIMEOUT/GPU/missing-original receipts. Indexing INACTIVE; live originals DISABLED/DEFERRED; Phase 19 NOT AUTHORIZED.

Native observations can be unavailable, cleanup is OS-limited and hosts can interrupt earlier. Report actual limits; silence is never hang proof. Return the smallest blocker to Codex for amendment if mandatory behavior cannot be implemented/proven within scope, a helper/dependency is necessary or source drift touches protected boundaries. Do not silently expand to B2, loosen retention/privacy or reopen resolved Owner choices.
