# Maintenance B1 — independent corrective re-review 1

- Date / reviewer: 2026-10-26 / Codex, independent of Antigravity remediation/self-review.
- Handoff / entry: `MAINT-B1-PROGRESS-WATCHDOG`, local `main` HEAD `dfc0c2052ffd5272ba63e6529d0bb401f1e5c022`; submitted working tree, index unchanged.
- Contract: unchanged [executable handoff](../b1-executable-handoff-2026-10-26.md); prior [initial independent review](2026-10-26-b1-independent-final-review.md).
- Disposition: **CHANGES_REQUESTED**. B1-F03/F06/F07 VERIFIED/CLOSED; B1-F01/F02/F04/F05 partially OPEN. **Two HIGH + two MEDIUM residual blockers**. No new finding IDs, features or approval layers.
- Round: **1/3** corrective/re-review rounds consumed. Next correction is Antigravity round 2; no automatic takeover or B2 authorization.

## Independent closure assessment

| Finding | Result / credit retained |
| --- | --- |
| B1-F01 | PARTIAL / OPEN, **MEDIUM residual**. Exclusive pre-opened stdout/stderr handles repair the original HIGH log-clobber path. Receipt allocation still occurs only after child execution; its concurrent pre-launch claim is not protected. |
| B1-F02 | PARTIAL / OPEN, **HIGH**. Explicit sink-failure rejection, optional health-exception tolerance and native orphan-descendant cleanup now pass their new tests. Real read errors still masquerade as EOF/PASS, and capture initialization/start is still outside lifecycle protection. |
| B1-F03 | VERIFIED / CLOSED. Finite validation and five non-finite-value cases reject before the child marker or receipt is created; positive/default and shorter timeout behavior remains tested. |
| B1-F04 | PARTIAL / OPEN, **HIGH**. WCT calls now have caller-side timed joins, and the original slow-WCT regression passes. Other inline health/file work and descendant termination do not respect the execution/shared-grace bounds. |
| B1-F05 | PARTIAL / OPEN, **MEDIUM residual**. The original HIGH raw-child-stderr console leak is repaired and its canary regression passes. The narrower receipt-write exception echo remains. |
| B1-F06 | VERIFIED / CLOSED. `os.read` makes the small flushed heartbeat visible while its child is alive; bounded chunking and the large-stream case pass. Read-error classification is accounted for separately under F02. |
| B1-F07 | VERIFIED / CLOSED. PowerShell examples now use a unique run directory and executable argv after `--`. Native AST checks report **0 errors** at skill lines 28 and 29; the approved focused command shape ran successfully. No Java/RAG command was run. |

F01/F05 severities are reduced because the original damaging stream-overwrite and direct child-stderr paths are fixed. The residual pre-launch/evidence-error defects still violate the already-approved contract and block commit. This re-review does not reopen the three independently closed findings.

## Residual findings and minimal corrections

### B1-F01 — MEDIUM / OPEN: receipt is not reserved before launch

- **Location:** `scripts/run-bounded-command.py:568–591, 613–621, 822–831`; `write_receipt` at 486–493.
- **Reproduction:** `unreserved_receipt` deterministically creates another writer's receipt after the preflight `exists()` check but at stdout allocation, before spawn. The concurrent receipt is preserved, yet the child execution marker is created. The wrapper fails only afterward with exit **1** when late `open(..., 'x')` cannot write the receipt.
- **Impact / existing requirement:** actual clobbering is fixed, but B1-F01 and the handoff explicitly require claiming **all** required evidence destinations before spawn. An approved command can execute without the required owned receipt destination.
- **Smallest correction:** claim the receipt exclusively alongside both logs before launch and finalize through that owned reservation without replacing a concurrent writer's path. Keep Owner approval and CLI semantics unchanged.
- **Closure proof:** a receipt claimant in the check/allocation interval must be preserved **and prevent the child marker**, or cause selection of a fresh owned run path before launch. Retain stream collision regressions.

### B1-F02 — HIGH / OPEN: real read failure is reported as complete capture, and setup failure escapes cleanup

- **Locations:** `scripts/run-bounded-command.py:223–242, 643–653, 781–801`; new tests `scripts/test_run_bounded_command.py` methods 14–16 do not cover these remaining paths.
- **Reproduction A:** `read_error_as_eof` queues a harmless child's known output, then raises a controlled `OSError` in the real `os.read` call. Lines 229–230 break, then line 240 sets complete. Result: exit **0**, **PASS**, `capture_complete=true`, **0 stdout bytes**, and the expected child output is absent. This is not the test's injected `.error` flag; it exercises the actual error handler.
- **Reproduction B:** `capture_setup_exception` fails `StreamCapture.start`. Capture construction/start precede the lifecycle `try` at line 653. `RuntimeError` escapes, no receipt exists, and the real owned child remains alive. The reviewer then stopped only that fixture process and closed its handles.
- **Impact:** the original false-success/lifecycle contract remains broken despite the added sink-error and orphan tests. Missing required output can be certified as complete, and a post-spawn setup error can leave work running without supervision.
- **Smallest correction:** distinguish EOF from read failure; mark failed/incomplete capture non-PASS. Put every post-spawn initialization/start operation inside guaranteed bounded cleanup, including partial setup. Preserve safe descendant handling and unrelated sentinel tests.
- **Closure proof:** exercise the real read-exception branch with queued bytes and initialization/start failure after a real spawn. Require truthful non-PASS capture/evidence and bounded owned cleanup; do not substitute another mocked `.error` assignment.

### B1-F04 — HIGH / OPEN: timing bounds do not cover all observations or descendant cleanup

- **Locations:** `scripts/run-bounded-command.py:658–664, 700–713, 496–535, 751–800`, including calls to `kill_descendants` at line 789. Its loop at 162–201 takes up to five seconds per native taskkill without the shared remaining budget.
- **Reproduction A:** `unbounded_health` injects a **0.650s** initial optional health observation with a **0.150s** execution cap. Execution reaches **0.656s** before cleanup, exit 124. Moving that call inside `try` handles exceptions but does not time-bound the call. Explicit progress-file stat calls likewise remain inline.
- **Reproduction B:** `unbounded_cleanup` uses a private scaled **0.060s** total-grace budget and a controlled 0.400s descendant-cleanup callee. Cleanup takes **0.453s**, yet returns `cleanup_complete=true` and exit 0. The fixture patches termination and uses a synthetic PID value; no unrelated process is targeted.
- **Impact:** the WCT-specific repair is real but is not a bound on all monitor/cleanup work. Calculating `shutdown_deadline` does not constrain calls that never receive/use its remaining time. A slow optional observation or several termination attempts can still defeat the promised timing limits.
- **Smallest correction:** apply remaining-time budgets to every optional observation and all descendant/handle/reap/drain work. Use one common shutdown deadline; over-budget or unavailable optional data is inconclusive, not permission to suspend supervision. No new service or dependency is required.
- **Closure proof:** include slow initial/periodic health/file observations and descendant cleanup in the timing regressions, in addition to the already passing WCT case. Record execution versus grace separately and respect the existing five-/20-second production limits with scaled private test thresholds.

### B1-F05 — MEDIUM / OPEN: receipt-write exception text still leaks to console

- **Location:** `scripts/run-bounded-command.py:822–831`, especially line 826.
- **Reproduction:** `receipt_exception_console` makes the receipt writer raise an `OSError` containing a synthetic private exception canary. Exit is correctly nonzero (**1**), but the canary is still in console output because `{exc}` is interpolated. The repaired child-stderr path remains credited.
- **Impact / existing requirement:** the initial F05 correction/proof explicitly included exception canaries and safe category/code summaries. A remaining error-reporting path can echo arbitrary exception payload instead of the required safe category. No real user secret was used.
- **Smallest correction:** replace the raw exception echo with a safe error category/code and appropriate evidence location; retain nonzero failure and avoid raw exception payload in receipt metadata.
- **Closure proof:** both child-stderr and evidence-writer exception canaries must stay absent from summaries/metadata; full child stderr remains available in the new raw log. Preserve exit-code behavior.

## Fresh verification and evidence

Directory: `logs/maintenance-b/b1-codex-rr1-c31610ff6a114549b0c44278f51ec635/`.

- Independent bootstrap: `python -B -m unittest discover -s scripts -p test_run_bounded_command.py -v`, under an independent **180s** outer supervisor. Exit **0**, **21.828s**, **20/20** tests passed (test time 21.577s). `bootstrap.json` and raw streams retained.
- Required final focused shape: changed runner with `--profile normal --timeout-seconds 300 --receipt <fresh>/review-final.json -- <resolved python> -B -m unittest discover -s scripts -p test_run_bounded_command.py -v`, under independent **320s** supervision. Exit **0**, **22.203s**, **20/20** tests passed (test time 21.673s). `final-supervisor.json`, `review-final.json` and raw streams retained.
- `rr1-probes.py`, `rr1-probe-results.json`, raw probe stdout/stderr: six short deterministic contract probes under an independent **90s** outer supervisor, completed exit 0. Completion is not an implementation PASS: their observed failures are above. Copy the probe file to a fresh directory before replay; never overwrite these receipts.
- Native PowerShell `Parser.ParseInput`: both current examples parse, zero errors. No full-suite example was executed.
- `git diff --check`: exit **0**. Index diff and excluded-scope diff (`backend`, `rag-engine`, `.codex`, Antigravity hooks and P17/P18 records) also exit **0**. No warning-free IDE/compiler claim.
- **101** local Markdown link occurrences resolved before review writes. Source identity/inventory is in new `review-inputs.json`: runner **`61f856f15e527bd7e4e3057ce3e94ee3d29a280932a78f957dd0352c4f7c35ab`**, tests **`0a00520aaee540111bb51bfc8e2e7ed822d9c97189832bfa96e093a13c693ca9`**.

The input inventory has 23 paths, including the preserved initial independent review. Compared with its original input hashes, only runner, tests, bounded-command skill and implementation/lifecycle handback/status files differ; remaining instruction-call-site review evidence is reused. Changes in the lifecycle files also include the previous review's status synchronization, not solely Antigravity code changes. Both changed Python files and the new native-helper/test paths were inspected; no new source file, package, dependency or B2 change exists.

Native WCT positive proof, healthy/inconclusive behavior, normal timeout, native descendant/sentinel and repaired privacy/live-output tests remain useful evidence. They do not cover the six failed residual probes. No Java/RAG suite, actual extended application command, B2 client-interception test, historical-log cleanup or publishing operation ran. Evidence and collision/read-failure fixtures used only new paths in this reviewer run. No full historical-log scan/equality claim is made.

## Backlog, coverage and outcome

**B1-L01 and MA-A03–MA-A06 remain LOW/OPEN backlog**, without promotion to commit blockers or compelled per-slice cleanup. No new finding ID is introduced. Preserve original independent review/raw receipts and the credited native results; make the current handback mapping accurate after remediation.

Shared coverage carries forward from the initial review: applicable scope/tree/cohesion/reuse/dependency dimensions **1, 3, 5, 7–9, 25 PASS**; execution/validation/privacy/operations/resource/testing/tooling/docs dimensions **2, 10, 12, 14, 16, 18, 21, 22, 26–29 FINDING** for the four residuals, with LOW-only docs backlog retained; **23 NOT_VERIFIED** for whole-contract bounds, despite useful positive measurements. Application-only dimensions **4, 6, 11, 13, 15, 17, 19, 20, 24 N/A**. B2 client enforcement remains excluded, not certified.

Review writes: this dated report, canonical ACTIVE/maintenance README/B1 status, and fresh ignored evidence only. No correction was implemented; prior review, runner, tests and executable contract are unchanged by the reviewer. B1 is not ready for Owner commit; B2, Phase 19 and indexing activation remain gated.

**Next step:** Antigravity `/antigravity-implement-handoff` performs **round 2** limited to F01/F02/F04/F05 residuals, adds meaningful focused regressions, self-reviews and returns for independent `$codex-final-review`. Counter remains **1/3** consumed. Preserve the three independently closed findings unless a concrete regression appears. No commit message while blocking findings remain; no automatic takeover.
