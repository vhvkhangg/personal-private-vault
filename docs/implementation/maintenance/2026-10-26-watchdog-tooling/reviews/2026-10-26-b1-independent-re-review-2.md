# Maintenance B1 — independent corrective re-review 2

- Date / reviewer: 2026-10-26 / Codex, independent of Antigravity implementation and self-review.
- Entry: `MAINT-B1-PROGRESS-WATCHDOG / IMPLEMENTED_AWAITING_CODEX_REVIEW`, local `main` HEAD `dfc0c2052ffd5272ba63e6529d0bb401f1e5c022`; submitted working tree, index unchanged.
- Authority: unchanged [executable handoff](../b1-executable-handoff-2026-10-26.md), [current Owner policy](../../../../workflow/current-owner-delivery-policy.md), [initial review](2026-10-26-b1-independent-final-review.md) and [round-1 re-review](2026-10-26-b1-independent-re-review-1.md).
- Disposition: **CHANGES_REQUESTED**. Four findings VERIFIED/CLOSED; **B1-F02 HIGH, B1-F04 MEDIUM and B1-F05 MEDIUM remain OPEN/PARTIAL**. No new finding ID, feature, approval layer or B2 scope.
- Counter: **2/3** corrective/re-review rounds consumed. Next is Antigravity round 3, the last permitted round before an independently blocked result requires an Owner takeover decision.

## Independent closure assessment

| Finding | Independent result / credit retained |
| --- | --- |
| B1-F01 | **VERIFIED / CLOSED.** Receipt and both streams are reserved exclusively before spawn. Replaying the exact concurrent-receipt probe preserves the claimant's receipt and prevents child execution. Original stream-collision protections remain covered. |
| B1-F02 | **HIGH / OPEN / PARTIAL.** Actual read-error handling and post-spawn capture setup cleanup are repaired; both prior probes now fail safely and clean up the owned child. Mandatory descendant verification can still fail or time out and become an empty list, falsely certifying complete cleanup/PASS with an owned child alive. |
| B1-F03 | **VERIFIED / CLOSED**, unchanged finite-value/pre-launch rejection evidence reused and focused suite passed. |
| B1-F04 | **MEDIUM / OPEN / PARTIAL**, reduced from HIGH. Prior unbounded health/cleanup waits are repaired and their exact probes now finish within caller bounds. Final diagnostics still precede the shared shutdown clock; finite extra time is added outside the approved aggregate grace. |
| B1-F05 | **MEDIUM / OPEN / PARTIAL.** Raw child stderr and receipt-writer exception summaries are repaired; previous canaries remain absent. Unexpected monitor exceptions are re-raised to the actual CLI interpreter and can expose arbitrary exception text through a traceback. |
| B1-F06 | **VERIFIED / CLOSED**, chunked live-output evidence retained; focused suite passed. |
| B1-F07 | **VERIFIED / CLOSED**, corrected PowerShell command and unique-run examples unchanged; required focused command shape executed successfully again. |

All six round-1 probes now produce their intended corrected outcomes. The three residual probes below exercise the same approved lifecycle, privacy and shared-budget obligations. They do not reopen accepted features or require a new infrastructure layer.

## Residual blockers and minimal corrections

### B1-F02 — HIGH: unavailable descendant verification becomes successful cleanup

- **Exact locations:** `scripts/run-bounded-command.py:52–70` (`timed_call` returns the same default on error/timeout), `:872–882` (both mandatory descendant checks use `default=[]`, and exhausted grace can also become `[]`), `:884–887` (PASS gate). Native enumeration failures must also remain distinguishable from a verified empty result.
- **Reproducible evidence:** `unknown_cleanup` in the fresh `rr2-regression-probes.py` starts a real root which spawns its own sleeping child with detached output handles, records that child's PID and exits. Inject an `OSError` from `find_owned_descendants`. Observed wrapper exit **0**, result **PASS**, `capture_complete=true`, `cleanup_complete=true`, while native liveness checking confirms the owned descendant is **alive**. Reviewer `finally` cleanup stops only this known fixture child.
- **Impact / existing requirement:** missing cleanup evidence is certified as success and unsupervised owned work can continue after the wrapper returns. This violates handoff B1-09 and the owned-descendant/unknown-state requirement at executable-contract line 58. Optional health tolerance does not make required cleanup verification optional.
- **Smallest correction:** carry a distinct unavailable/timeout state through required descendant verification, including native enumeration failure and exhausted budgets. Mark cleanup unverified/incomplete and return non-PASS when absence cannot be verified; retain bounded owned cleanup and unrelated-process safety. No universal OS-containment promise is required.
- **Closure proof:** exercise unavailable and timed-out mandatory descendant checks with a real owned child; require truthful cleanup status and non-PASS. Retain the now-passing read-error, partial-start, native descendant and unrelated sentinel regressions.

### B1-F04 — MEDIUM: final diagnostics are outside the shared post-deadline budget

- **Exact locations:** `scripts/run-bounded-command.py:740–742` takes the final snapshot; `:818–820` then clips reported execution time and starts a fresh `TOTAL_GRACE_BUDGET`; `:823–827, 872–880` consume the later pool. The descendant termination call at `:877` limits the caller's wait without passing the remaining budget into the callee.
- **Reproducible evidence:** `shared_shutdown_budget` uses private scaled constants: execution **0.150s**, diagnostic **0.120s**, aggregate grace **0.200s**. The final diagnostic consumes approximately **0.125s** before a fresh grace is created. A cleanup fixture waits **0.160s within the approximately 0.200s budget actually supplied to it**, then invokes real owned termination with the remaining allocation. First termination request is at **0.438s**, with the child still alive, beyond the combined **0.350s** allowance. Total observed wall time **0.547s**; receipt reports execution **0.150s**, diagnostic **0.125s**, cleanup **0.265s**. The pre-termination timestamp establishes the extra allocation independently of native kill scheduling.
- **Impact / existing requirement:** contract line 50 explicitly allows **one** at-most-20-second pool for post-execution diagnosis, termination, reap and drain. Production takes up to a two-second final snapshot before granting a fresh 20-second pool; reported clipped execution does not reveal that allocation error. The former indefinite waits are repaired, so this narrower finite-overrun residual is MEDIUM.
- **Smallest correction:** establish one post-deadline/shutdown deadline before final diagnosis, debit every subsequent operation from it, and pass remaining time into cleanup callees as well as limiting caller waits. Record actual elapsed execution/diagnostic/cleanup values honestly. Preserve the existing 300/2100-second caps and five-/20-second limits.
- **Closure proof:** a scaled final-diagnostic-plus-cleanup regression must share one aggregate pool, with first termination and measured durations consistent with that pool. Preserve the passing slow-WCT, health and cleanup regressions; no 2100-second application run is needed.

### B1-F05 — MEDIUM: unexpected monitor exceptions leak through the CLI traceback

- **Exact locations:** `scripts/run-bounded-command.py:812–816` re-raises after recording `INTERNAL_ERROR`; `:948–949` invokes `main()` without a safe CLI exception boundary.
- **Reproducible evidence:** `cli_fault_probe.py` executes the real file using `runpy.run_path(..., run_name='__main__')`. Only `StreamCapture.start` is made to raise a `RuntimeError` containing a synthetic private canary; ordinary cleanup threads remain enabled. Actual process exit is **1**, the receipt correctly says **INTERNAL_ERROR** and `cleanup_complete=true`, but the canary appears in interpreter stderr. This is the executable CLI boundary, not a reviewer printing a caught exception. No real secret is used.
- **Impact / existing requirement:** the repaired receipt-writer path is safe, but an unexpected post-spawn fault bypasses the approved concise, privacy-safe console contract at lines 54–57. Arbitrary exception payload can escape to the invoking client.
- **Smallest correction:** retain guaranteed cleanup and nonzero INTERNAL_ERROR reporting while containing unexpected exceptions at the CLI boundary. Emit a safe category and evidence location; do not echo raw exception text or traceback into the console. Imported-call semantics need not change if a narrow CLI handler suffices.
- **Closure proof:** exercise the actual `__main__` path with an exception canary and owned child; require no canary/traceback payload in console, truthful non-PASS receipt and bounded cleanup. Retain child-stderr and evidence-writer canary regressions.

## Fresh checks and evidence

Fresh ignored directory: `logs/maintenance-b/b1-codex-rr2-64c639ad2d7241ad8d90cc76f8a66047/`. Evidence files were created exclusively in new paths; earlier logs and reviews were retained.

| Check | Independent result |
| --- | --- |
| Bootstrap, `python -B -m unittest discover -s scripts -p test_run_bounded_command.py -v`, independent 180s supervisor | **25/25 PASS**, exit 0; outer 22.485s, test time 22.216s. `bootstrap.json`, stdout/stderr retained. |
| Required final shape, changed runner `--profile normal --timeout-seconds 300 --receipt <fresh>/review-final.json -- <resolved python> -B -m unittest discover -s scripts -p test_run_bounded_command.py -v`, independent 320s supervisor | **25/25 PASS**, exit 0; outer 23.172s, test time 22.413s. `final-supervisor.json`, `review-final.json` and raw streams retained. |
| Exact round-1 probe replay, independent 90s supervisor | All six corrected outcomes verified; unchanged probe copied into fresh `rr1-replay/`. `rr1-probe-results.json` and raw replay output retained. |
| Three additional residual regressions, independent 90s supervisor | Probe driver completed exit 0; implementation contract failures are the three findings above. `rr2-regression-probes.py`, `rr2-regression-results.json`, `cli_fault_probe.py` and raw streams retained. Driver completion does not mean implementation PASS. |
| Static/scope checks | `git diff --check` passed; no index changes, application/frozen-phase, `.codex` or hook changes. 104 local Markdown link occurrences resolved before review writes; all 29 in the four review-written documents resolved afterward. Input hash comparison confirms only ACTIVE, maintenance README and slice status changed during report writing; runner, tests and prior reviews are unchanged. No warning-free compiler/IDE claim. |

Runtime: native Windows (`os.name=nt`, `win32`), `C:\msys64\ucrt64\bin\python.exe`, Python 3.11.7. Source SHA-256: runner `d0badac0c7e83c6d5fc4ea6de2031d47eedff9d7d1aa4c220904f9ce611e7008`; tests `7b94658a09f9215f1cce78eca00653eb9e5d0273d74e8ea1615982f2e3e039f3`. Source identities/inventory are retained in `review-inputs.json`.

Native WCT positive proof, healthy/inconclusive behavior, deadline dominance, child/sentinel cleanup, privacy, collision and streaming tests remain credited. They do not establish the three residual error/budget cases. No Java/RAG/full application suite, real extended application command, B2 interception test, publishing probe or historical-log scan/equality claim was made. Synthetic fixtures targeted only reviewer-owned processes.

## Changed-file inventory and coverage

The reviewed input has **24 paths** (20 tracked modified paths and four untracked handoff/handback/review documents):

- Tooling: `scripts/run-bounded-command.py`, `scripts/test_run_bounded_command.py`.
- Instructions: `AGENTS.md`; `.agents/README.md`; `.agents/agents/backend-implementer/agent.md`; `.agents/rules/backend-python.md`, `tests.md`; `.agents/skills/{antigravity-implement-handoff,backend-testing,bounded-command-execution,codex-create-handoff,codex-implementation-takeover}/SKILL.md`.
- Workflow: `docs/workflow/{current-owner-delivery-policy,agent-development-workflow,owner-phase-workflow,rag-phase-delivery-guardrails}.md`.
- Routing/scope: `docs/implementation/handoffs/{ACTIVE,README}.md`; this maintenance scope's `README.md`, `slice-b1-watchdog.md`, `b1-executable-handoff-2026-10-26.md`, `b1-implementation-handback-2026-10-26.md`, initial independent review and round-1 independent re-review.

Compared with round 1, runner, tests, ACTIVE, scope README, slice status and handback changed. Prior evidence for unchanged call sites/contracts is reused; lifecycle edits include the previous independent review's status synchronization. Both Python files and their new helper/test paths were inspected. No new source package, dependency or production feature was added. This report is the subsequent review output, outside that 24-path input inventory.

Shared risk-applicable coverage: scope/tree/cohesion/reuse/dependency and corrected command documentation dimensions **1, 3, 5, 7–9, 12, 25 PASS**; lifecycle/validation/privacy/operations/resources/tests/tooling/docs dimensions **2, 10, 14, 16, 18, 21, 22, 26–29 FINDING**, with LOW-only prose debt retained. **23 NOT_VERIFIED** for whole-contract bounds despite positive native measurements. Application-only dimensions **4, 6, 11, 13, 15, 17, 19, 20, 24 N/A**: no Java API/persistence/module change. B2 client enforcement remains excluded and uncertified.

**B1-L01 and MA-A03–MA-A06 remain LOW/OPEN milestone/audit backlog**, not commit blockers. Prior historical decisions, failed evidence and independent reviews remain unchanged.

Review writes are limited to this dated report, canonical ACTIVE/maintenance README/B1 status and fresh ignored evidence. No implementation correction, staging or publishing occurred. Phase 17 remains FROZEN, Phase 18 CLOSED, Phase 19 NOT AUTHORIZED and indexing INACTIVE; B2 remains gated on independent B1 acceptance, Owner publication and its own handoff.

**Next step:** Antigravity `/antigravity-implement-handoff` performs **round 3, the last round**, limited to the F02/F04/F05 residuals, adds focused regressions, completes mandatory self-review and returns for independent `$codex-final-review`. If that third re-review is still blocked, stop Antigravity and escalate to Owner for a separately bounded takeover decision; no fourth round or automatic takeover. No commit message while blocking findings remain.
