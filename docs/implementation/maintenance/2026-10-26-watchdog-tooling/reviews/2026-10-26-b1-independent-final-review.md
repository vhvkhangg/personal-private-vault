# Maintenance B1 — independent final implementation review

- Date / reviewer: 2026-10-26 / Codex, independent of Antigravity implementation and self-review.
- Handoff: `MAINT-B1-PROGRESS-WATCHDOG`; entry/local HEAD `dfc0c2052ffd5272ba63e6529d0bb401f1e5c022`, branch `main`, unstaged implementation and handoff documents. Index unchanged.
- Contract: [executable handoff](../b1-executable-handoff-2026-10-26.md), [approved B1 requirements](../slice-b1-watchdog.md), [implementation handback](../b1-implementation-handback-2026-10-26.md).
- Disposition: **CHANGES_REQUESTED** — **4 HIGH + 3 MEDIUM blockers**; **1 LOW** finding is non-blocking. No CRITICAL finding. Initial review consumes no remediation round: **0/3** remains.
- Scope: all 22 submitted paths inspected, including the two Python files, handoff/handback, lifecycle documents and finite instruction changes. No production correction was made by the reviewer.

## Outcome and independent evidence

The submitted focused suite passes **12/12** under independent supervision, including its native Windows WCT cycle and parent/child/grandchild/sentinel cases. The required final focused run through the changed wrapper also passes **12/12**. Those positive paths do not prove the complete handoff: eight additional contract probes reproduce retention, lifecycle, timing, privacy and live-output failures. A PowerShell parser check finds an invalid documented invocation.

All fresh reviewer evidence is under `logs/maintenance-b/b1-codex-review-d03ec3c3845a433c9a1724dac0fedc5f/`:

- `bootstrap.json`, `bootstrap.stdout.log`, `bootstrap.stderr.log`: independent 180-second outer supervisor; exit 0, **13.500s**; 12 tests in 13.256s.
- `final-supervisor.json`, `review-final.json`, `review-final.stdout.log`, `review-final.stderr.log`: approved 300-second inner command under an independent 320-second outer supervisor; exit 0, outer **14.156s**; 12 tests in 13.328s. No extended application command ran.
- `adversarial-probes.py`, `adversarial-results.json`, `adversarial.stdout.log`, `adversarial.stderr.log`: saved deterministic short fixtures under a 90-second independent outer supervisor; exit 0 means the probes completed, **not that the implementation met their contracts**. Individual receipts/raw logs remain alongside them.
- `powershell-example-check.json`: native PowerShell parser outcomes; no example command was executed.
- `review-inputs.json`: complete pre-review-write path/SHA-256 inventory. Runner SHA-256 `3c364690ee629acc2ec3cc439d1cb8ae2f24e41f805d32686ce21dff47c42010`; test SHA-256 `0627b40c330649e6dac30aca63915f4c3951ca1fb2d212d684fd8efce3ac5476`.

The probe script uses controlled synthetic data and faults, real short subprocesses and explicit owned-process cleanup. Copy it to a **fresh** evidence directory before replaying from the repository root; do not reuse/overwrite the original receipts. No real private content or historical log was used as a test target.

## Blocking findings

### B1-F01 — HIGH / OPEN: concurrent output-log creation is overwritten

- **Location:** `scripts/run-bounded-command.py:65–77, 363–378, 403–430`; test gap at `scripts/test_run_bounded_command.py:413–441`.
- **Evidence:** paths are checked with `exists()` before launch, but `StreamCapture` later opens them with **`wb`**. Probe `concurrent_log_collision` creates a synthetic prior log between that check and open. The wrapper returns 0; original sentinel bytes are replaced by `NEW_CHILD_OUTPUT`. Before SHA-256 `6f7d51f3db682d28e29edd707b8e625f2a4137854f81aec255f3381a512a599b`; after `c776b4d438e47ca59dfa1595f7d4c8a0f7cbe2202818c1224434061f83fe2906`.
- **Requirement / impact:** B1-10 explicitly requires exclusive reservation and concurrent-collision preservation. An existing evidence file can be truncated even though ordinary pre-existing-path tests pass. Exclusive receipt creation alone does not protect stream files or reserve all destinations before launch.
- **Minimal correction:** claim all required receipt/output destinations exclusively before spawning, retain and use those owned handles, and fail safely on any collision. Finalization must not replace someone else's path. Preserve the existing CLI and historical logs.
- **Proof to close:** a deterministic check/open interleaving or concurrent-creator fixture must preserve the original bytes and prevent an unowned-path write/unsafe launch. Cover receipt, stdout and stderr collisions rather than only a pre-existing receipt.

### B1-F02 — HIGH / OPEN: post-spawn failures and incomplete capture can be reported as PASS or leak children

- **Location:** `scripts/run-bounded-command.py:79–87, 309–311, 424–443, 449–453, 526–561`; test gaps at `scripts/test_run_bounded_command.py:374–411, 443–469`.
- **Evidence:** `capture_failure` injects a stream sink error: exit **0**, receipt **PASS**, `capture_complete=false`. `initial_probe_exception` raises in the first health call at line 434, before the monitor's `try`: no receipt and the real child is still alive after `main` raises. `root_exit_descendant` lets the root exit while its child holds inherited pipe handles: exit **0**, **PASS**, `capture_complete=false`, `cleanup_complete=true`, `cleanup=not_needed`, while the owned descendant is still alive. The reviewer then terminated only that known owned fixture process.
- **Requirement / impact:** B1-01/08/09/11 prohibit PASS without required capture and demand bounded lifecycle cleanup. Root exit alone is neither complete capture nor complete owned-tree cleanup. Reader errors are stored but never enforced, and initial monitoring/setup is outside cleanup protection.
- **Minimal correction:** guard the entire post-spawn lifecycle with guaranteed bounded cleanup; observe reader errors, handle initialization/monitor faults, settle inherited handles/owned descendants safely and gate PASS on the required completed evidence/capture. Report surviving/unknown descendants truthfully. Keep unrelated sentinel safety; no process-name killing or new supervisor framework.
- **Proof to close:** failed sink, initial probe exception and root-exits-before-descendant cases must finish within the approved budget, avoid PASS on incomplete required capture, preserve evidence and prove owned cleanup or explicitly report inability without claiming success. Retain the native Windows sentinel regression.

### B1-F03 — MEDIUM / OPEN: NaN bypasses finite timeout validation

- **Location:** `scripts/run-bounded-command.py:359–361, 385–386, 456–461`; existing guard test `scripts/test_run_bounded_command.py:129–164` omits non-finite values.
- **Evidence:** `nan_timeout` invokes `--timeout-seconds nan` with a harmless immediate child. The child launches, wrapper returns **0/PASS**, and the receipt accepts a NaN timeout. The validation's comparisons do not reject NaN; subsequent `now >= deadline` comparisons cannot enforce that deadline.
- **Requirement / impact:** the executable handoff explicitly requires rejecting non-finite values **before launch**. This provides a cap-bypass path for a long-lived child; the review used an immediately exiting child, not an uncontrolled hang.
- **Minimal correction:** require a finite, positive, in-cap timeout before any spawn/evidence side effects.
- **Proof to close:** NaN, positive/negative infinity, nonpositive and over-cap inputs fail before a child launch marker can be created; valid shorter deadlines still work.

### B1-F04 — HIGH / OPEN: diagnostic and shutdown budgets are declared but not enforced

- **Location:** `scripts/run-bounded-command.py:26–28, 160–280, 433–434, 464–499, 526–539`.
- **Evidence:** `diag_budget` is calculated at line 486 but not used to bound the synchronous probe at line 489. In `slow_diagnostic`, a controlled inconclusive probe took **0.656s** with a private scaled **0.040s diagnostic budget** and **0.150s execution cap**; `execution_duration_seconds` reached **0.687s** before cleanup. These are injected slow-probe results, not claims about every native WCT call. Source also serially stacks up to 5s taskkill, 15s reap and two 2s joins without applying the declared 20s total grace deadline. Initial health/file probes also run inline.
- **Requirement / impact:** the handoff requires execution checks at least every 0.5s, diagnostic episodes at most 5s/remaining execution time, and **one** post-execution grace of at most 20s. A blocking probe can hold the deadline loop; independent host supervision, not this wrapper, is then the effective bound. Ordinary timeout currently also breaks directly to cleanup without the specified bounded final diagnostic snapshot.
- **Minimal correction:** enforce remaining-time budgets for potentially blocking observations and a single shared shutdown deadline. Unavailable/over-budget optional observations become inconclusive rather than suspending execution control. Preserve conservative native positive proof; no new dependency/service is required.
- **Proof to close:** stalled/slow probe and cleanup/drain fixtures must not exceed the execution/diagnostic/shared-grace bounds, and repeated output/progress must never renew the hard deadline. A constant or unused local variable is not runtime enforcement.

### B1-F05 — HIGH / OPEN: failed child output is copied into the console

- **Location:** `scripts/run-bounded-command.py:413, 522, 570–581`; contrary test expectation at `scripts/test_run_bounded_command.py:299–317`.
- **Evidence:** `private_stderr` writes a synthetic private-output canary to child stderr and exits 7. The wrapper returns 7, preserves the full saved stderr **and prints the canary in its console summary**. Lines 578–581 deliberately echo the last raw stderr line. Other error paths stringify exceptions into console/receipt fields without a safe category boundary.
- **Requirement / impact:** B1-07/10 and the executable contract explicitly keep private payload/raw stderr in fresh ignored logs, with safe category/code and evidence location in summaries. Successful-stdout argv-canary coverage does not protect failure paths.
- **Minimal correction:** use safe categories/codes in console/receipt summaries; retain complete raw child error details only in this run's saved evidence. Do not weaken capture or hide the evidence path. Update the existing test that currently requires raw failure-text echo.
- **Proof to close:** synthetic stderr and exception canaries must be absent from console and receipt metadata while remaining in the appropriate saved raw evidence; exit 7 must still propagate.

### B1-F06 — MEDIUM / OPEN: small flushed output is buffered until EOF rather than observed live

- **Location:** `scripts/run-bounded-command.py:69–77, 462–469`; live-output coverage missing from `scripts/test_run_bounded_command.py:443–469`.
- **Evidence:** `small_live_output` starts the real reader on a child that flushes 11 bytes and stays alive. At 0.3s, child alive, captured bytes **0**, disk bytes **0**; after child exit, final bytes **11**. Buffered `read(CHUNK_SIZE)` waits for the requested quantity or EOF instead of delivering the available small write.
- **Requirement / impact:** stdout/stderr byte progress is an explicit B1 progress source. Small heartbeats cannot advance the runner's progress state or saved log promptly; a large-output-at-exit test misses this behavior.
- **Minimal correction:** read available bytes in bounded chunks without waiting for 64 KiB/EOF, retaining bounded memory and full output integrity.
- **Proof to close:** while a low-CPU child stays alive, repeated short flushed writes become visible in the new log and progress state before EOF; retain Unicode/long-line and complete-capture checks.

### B1-F07 — MEDIUM / OPEN: documented PowerShell example does not parse

- **Location:** `.agents/skills/bounded-command-execution/SKILL.md:26–27`.
- **Evidence:** native `System.Management.Automation.Language.Parser.ParseInput` reports **1 parse error** for line 26: the second `&` after `--` is not allowed. Line 27 parses. Neither example was executed. The examples also reuse fixed receipt filenames rather than the fresh run-scoped pattern required by the handoff.
- **Requirement / impact:** the approved B1 instruction target calls for tested CLI/examples and fresh evidence paths. The primary focused-verification example cannot be copied into the stated shell and run correctly.
- **Minimal correction:** pass the executable as an argv value after `--` (no nested PowerShell call operator) and reuse the approved unique-run-directory pattern. Keep full-suite examples conditional on actual approved command scope.
- **Proof to close:** parse both examples without errors; run only the approved focused normal-profile shape with fresh paths. Do not run a Java/RAG full suite to verify this documentation fix.

## Non-blocking finding

### B1-L01 — LOW / OPEN: duplicated current-status prose and independence wording drift

`docs/implementation/handoffs/README.md:3` still says READY_FOR_IMPLEMENTATION/not implemented, while the submitted ACTIVE was awaiting independent review. The submitted handback at `b1-implementation-handback-2026-10-26.md:25` calls all criteria “independently verified” before any independent implementation review. Some current scope status lines likewise still described implementation as not started. The canonical ACTIVE and scope status are synchronized by this review; the remaining duplicate prose/handback attribution needs a small later cleanup. Record the supplied 12/12 results as implementer evidence, preserving their raw receipts and dates; do not rewrite history to imply independent acceptance. This is non-blocking because canonical gate authority is explicit and the substantive runtime failures have their own blocking IDs.

MA-A03–MA-A06 remain LOW/OPEN in the existing milestone/audit backlog. No new requirement, B2 guard task, architectural redesign or frozen-phase reopening is imposed by these findings.

## Acceptance coverage and limits

| B1 cases | Independent state |
| --- | --- |
| 01, 02, 03 | Ordinary success/deadline and ordinary shape examples pass; complete acceptance FAILS on capture/lifecycle, non-finite input and enforced diagnostic bounds (F02–F04). |
| 04, 05, 06 | Native CPU/file progress, real WCT positive cycle and quiet/inconclusive cases pass in the submitted suite. Live pipe progress and time-bounded observation remain deficient (F04/F06); do not discard the credited native proof. |
| 07, 10, 11 | Ordinary nonzero exit and large-output capture pass; full contracts FAIL on raw error leakage, concurrent clobber and incomplete/live capture (F01/F02/F05/F06). |
| 08, 09 | Native timeout tree/sentinel, cancellation and launch-error tests pass. Initial monitoring failure and root-before-descendant/inherited-pipe behavior FAIL (F02). |
| 12 | Short outer-supervisor case ran and bounds wrapper lifetime. Its source asserts the wrapper PID only, not the child PID lifetime claimed in the handback. Preserve that limit; no claim of actual 2100s host operation is supported or required by these short tests. |

Test names and aggregate counts cannot replace missing fault-path assertions. Remediation should extend the same focused suite with the concrete regression cases above; no application suite, arbitrary test quota or separate test framework is needed.

## Scope, hygiene and shared coverage

The 22 reviewed input paths are fully inventoried with raw hashes in `review-inputs.json`: two Python files; `.agents/skills/bounded-command-execution/SKILL.md`; finite call-site edits in `.agents/README.md`, backend agent, backend-python/tests rules, implement-handoff/backend-testing/create-handoff/takeover skills, `AGENTS.md`, and the four workflow files; ACTIVE/handoff README, maintenance README/B1 status, executable handoff and implementation handback. Recursive `scripts/` inventory contains ten files and no added production helper; both changed Python files were read completely. Changed Markdown content/hunks and both new handoff artifacts were inspected. Ninety-six local Markdown links resolved before review writes; `git diff --check` exited 0. The PowerShell syntax failure is a separate finding.

No B2 `.codex`/Antigravity-hook changes, dependency/module/database changes, application/RAG tests, Phase 19 activation or agent Git publishing occurred. Prior implementation bootstrap/final evidence is retained. New reviewer logs and synthetic fixture files were created exclusively in the fresh run; only reviewer-owned fixture processes were terminated. No historical log was a probe target, and no repository-wide log hash scan/PASS is asserted.

| Shared dimensions | Result / rationale |
| --- | --- |
| 1 intent; 3 architecture/scope; 5 folders; 7 cohesion/SOLID; 8 reuse/KISS; 9 pattern fit; 25 dependencies | PASS for inspected scope: existing wrapper/private native helpers and reader threads, no speculative framework, module/frozen-baseline changes or new dependencies. Source responsibilities are identifiable; no taste-based class/file quota. |
| 2 traceability; 10 execution logic; 12 timeout/path input; 14 privacy/retention; 16 operations/recovery; 18 I/O/resource bounds; 21 security negatives; 22 tests; 26 tooling; 27 diagnostics/logging; 28 docs; 29 delivery | FINDING — F01–F07; L01 remains non-blocking. Actual Windows positive cases and all observed limitations are distinguished from fault injections. |
| 23 resource/performance verification | NOT_VERIFIED for whole-contract claims: fixed chunk/tail sizes and the large stream test are useful evidence, but reader shutdown/live-progress and enforced timing have reproduced gaps. No microbenchmark dashboard requested. |
| 4 application-module dependencies; 6 HTTP/ApiResponse/ADR-0016 placement; 11 application APIs; 13 application auth/consent; 15 database transactions; 17 persistence; 19 retrieval quality; 20 parser/OCR; 24 model evaluation | N/A — no changed/enabled application, schema, ingestion or model capabilities. B2 client interception remains excluded rather than falsely certified. |

## Review writes and next step

Review-only writes: this dated report, ACTIVE remediation/final-review state, canonical maintenance README and B1 lifecycle status, plus fresh ignored reviewer evidence. Runner, tests, instruction fixes and historical reports were **not** changed. The implementer must correct the code and current handback/evidence mapping within the existing B1 contract.

**Next step:** return B1-F01–F07 to Antigravity `/antigravity-implement-handoff` for the **first** bounded remediation and mandatory self-review, then independent `$codex-final-review`. Initial counter remains 0/3; no takeover or B2 authorization. No commit message is supplied while blockers remain. Owner alone publishes after independent acceptance; B2 remains gated on that publication and its own handoff.
