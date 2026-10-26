# Independent review — Maintenance B1 corrective re-review 3

- Date / reviewer: 2026-10-26 / Codex, independent of Antigravity implementation and self-review.
- HEAD / scope: local `main` `dfc0c2052ffd5272ba63e6529d0bb401f1e5c022`, submitted working tree; entry ACTIVE `IMPLEMENTED_AWAITING_CODEX_REVIEW`, corrective round **3/3**.
- Authority: unchanged [B1 executable handoff](../b1-executable-handoff-2026-10-26.md) and [Owner delivery policy](../../../../workflow/current-owner-delivery-policy.md). Prior [initial](2026-10-26-b1-independent-final-review.md), [round-1](2026-10-26-b1-independent-re-review-1.md) and [round-2](2026-10-26-b1-independent-re-review-2.md) evidence is retained and reused where unchanged.
- Disposition: **CHANGES_REQUESTED**. **B1-F02 remains HIGH / OPEN**; six other findings are VERIFIED/CLOSED. No new finding ID or approval layer.
- Routing: all **three** Antigravity corrective/re-review rounds are now independently blocked. **STOP Antigravity remediation; OWNER_DECISION_REQUIRED.** No fourth round, automatic takeover, B2 activation or commit message.

## User requirement and simplest-mechanism check

The two existing Python files still implement the approved single-runner mechanism: 300-second ordinary cap, approved suite-shaped commands at most 2100 seconds, attributable progress and conservative diagnosis, bounded owned-tree cleanup, chunked private output and non-clobber evidence. No dependency, helper package, service, B2 configuration/hook change or application feature was added. Third-round changes address the existing F02/F04/F05 residuals; no architecture or acceptance change is needed.

## Finding register

| ID | Severity | Blocking? | Independent evidence / minimal correction | Status |
| --- | --- | --- | --- | --- |
| B1-F01 | MEDIUM prior residual | No | Prior exclusive receipt/stream reservation closure retained; focused collision regressions pass. | VERIFIED / CLOSED |
| B1-F02 | **HIGH** | **Yes** | Native enumeration failures still become verified empty descendants and false PASS. Details below; propagate failure distinctly from normal enumeration end into the existing unavailable-state gate. | OPEN / PARTIAL; OWNER_DECISION_REQUIRED |
| B1-F03 | MEDIUM original | No | Finite/pre-launch validation unchanged; focused boundary tests pass. | VERIFIED / CLOSED |
| B1-F04 | MEDIUM prior residual | No | Shared post-deadline pool now starts before final diagnosis (`run-bounded-command.py:745–750`); remaining budgets reach cleanup (`:844–849, 903–922`). Exact prior probe supplies about 0.084s to cleanup and first requests termination at 0.344s, within its 0.350s combined allocation. See timing limits below. | VERIFIED / CLOSED for the round-2 allocation residual |
| B1-F05 | MEDIUM prior residual | No | Actual CLI boundary `run-bounded-command.py:994–999` suppresses exception payload/traceback. Exact prior probe exits 1 with INTERNAL_ERROR receipt, complete cleanup and no private canary. Writer/child-stderr privacy regressions remain green. | VERIFIED / CLOSED |
| B1-F06 | MEDIUM original | No | Chunked live-output proof retained and focused streaming tests pass. | VERIFIED / CLOSED |
| B1-F07 | MEDIUM original | No | PowerShell examples unchanged from independently parsed correction; approved focused command shape ran again with fresh paths. | VERIFIED / CLOSED |
| B1-L01; MA-A03–MA-A06 | LOW | No | Existing status/attribution and Maintenance A backlog retained for consolidated milestone/audit cleanup. | OPEN / BACKLOG |

### B1-F02 — native enumeration failures still certify complete cleanup

- **Exact location:** `scripts/run-bounded-command.py:136–163`, especially `:145–151`. `Process32First` returning false silently skips enumeration; any false `Process32Next` ends the loop without checking whether enumeration completed normally. Both return a list which `:898–901` treats as verified; `:924–933` then permits `cleanup_complete=true` and PASS. Regression coverage at `scripts/test_run_bounded_command.py:824–856` injects an exception above the native helper, bypassing these native error branches.
- **Reproduction:** saved `native-failures/native-failure-probes.py` runs a real root which creates an owned sleeping child, redirects its output handles and records its PID before exiting. For each case, a narrow `ctypes.WinDLL` proxy forwards real kernel32 calls but makes **only** `Process32First` or `Process32Next` return false with last-error **5**. Actual native snapshot allocation and closure still run. Each probe observes wrapper exit **0**, receipt **PASS**, `capture_complete=true`, `cleanup_complete=true`, while a native liveness check confirms the owned child is **alive**. Each injected API was called once. Reviewer `finally` cleanup terminates only that known fixture child.
- **Impact:** the newly added `None`/`descendants_verified` gate works for raised exceptions and timeouts, but native errors can still enter it as a successfully empty/partial list. This preserves the original HIGH false-success/owned-lifecycle defect. Handoff B1-01/08/09 and output/lifecycle requirement 5 require truthful survivor/unknown reporting; no universal OS containment promise is being imposed.
- **Smallest correction:** distinguish successful enumeration completion from native failure at both First/Next boundaries, preserve the relevant native error state and propagate failure to the existing unavailable-verification path. Do not accept a partial snapshot as proof of no descendants. Preserve bounded cleanup and unrelated-process safety; no new framework or dependency.
- **Proof required:** run the actual native helper with controlled First and Next error returns, a real owned child and finally cleanup. Both must return non-PASS with incomplete/unverified cleanup. Retain ordinary enumeration/cleanup, unavailable-helper, lookup-timeout, read-error, partial-start and unrelated sentinel tests.

**Credit retained:** the unchanged round-2 exception probe now returns exit **1 / COMMAND_FAILED / cleanup_complete=false**. An additional slow-lookup probe likewise returns truthful unavailable cleanup after approximately **1.531s**. Both leave the intentionally undiscoverable fixture child alive until reviewer cleanup, without certifying success. These fixes are real; they do not cover native First/Next failures.

## Acceptance matrix

| Requirements | Independent result / limit |
| --- | --- |
| B1-01, B1-08, B1-09 | Ordinary success, native owned tree/sentinel, cancellation, capture/read/setup faults pass. **FAIL overall** on F02's native-unavailable false PASS. |
| B1-02–07, B1-10–11 | Focused native/injected tests pass; previous retention, validation, progress/diagnosis, error privacy and streaming closures retained. Shared-budget allocation residual and CLI privacy probe now corrected. |
| B1-12 | Short outer-supervisor test and independent review supervisors executed. Prior limit remains: no claim of universal child containment or actual 2100-second host operation from short fixtures. |

## Verification

Fresh ignored evidence: `logs/maintenance-b/b1-codex-rr3-68702d5b10024d8aa16980ff50cb20b0/`. All reviewer evidence uses newly created exclusive paths; prior logs/reviews were read and retained.

| Check | Result / evidence |
| --- | --- |
| Independent bootstrap: `python -B -m unittest discover -s scripts -p test_run_bounded_command.py -v`, independent 180s supervisor | Exit **0**, **28/28 PASS**, outer **24.110s**, test time **23.824s**. `bootstrap-supervisor.json`, raw stdout/stderr. |
| Required final shape: `python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <fresh>/review-final.json -- <resolved python> -B -m unittest discover -s scripts -p test_run_bounded_command.py -v`, independent 320s supervisor | Exit **0**, **28/28 PASS**, outer **24.719s**, test time **23.821s**. `final-supervisor.json`, `review-final.json` and raw streams. Wrapper receipt: PASS, capture/cleanup complete, duration **24.563s**. |
| Exact round-2 probe replay, independent 90s supervisor | Completed exit 0 in **1.812s**. All three prior residual observations corrected. Unchanged script copied to fresh `rr2-replay/`; results and raw streams retained. |
| Native failure/timeout probes, independent 90s supervisor | Completed exit 0 in **3.219s**. First/Next cases reproduce F02; lookup-timeout correctly rejects PASS. `native-failures/native-failure-results.json`, script, individual receipts and console logs retained. Driver completion is not contract PASS. |
| Focused hygiene / links | `git diff --check`, index diff and excluded-scope diff all exit 0. **108** local Markdown link occurrences resolved before report writes. `prewrite-checks.json` and `review-inputs.json` retain identities and results. |

Runtime is native Windows, `C:\msys64\ucrt64\bin\python.exe`, Python 3.11.7 / `win32`. Runner SHA-256 `7dc1e88f903e6af922e07b149c3103bdac88ab62a4089a168f862b4156f6ae32`; tests `92237824a50f14dd2badf51eb4bb6972a2cc22865905b01fb80472950b27c719`.

Timing evidence is deliberately limited: the scaled shared-budget replay reports diagnostic **0.110s**, cleanup **0.218s**, total wall **0.484s**, including actual native launch/termination and timer overhead. Its first termination request and supplied remaining budget prove the previous extra-pool allocation was removed; those tiny scaled values do not establish an exact wall-time ceiling for every native operation or a real 2100-second run. Full actual durations remain in the receipt. No Java/RAG/full application suite, extended application command, B2 interception/publishing probe, warning-free IDE/compiler claim or historical-log equality scan was performed.

## Scope, changed-file inventory and coverage

The **25 input paths** comprise 20 tracked modifications and five untracked handoff/handback/prior-review documents:

- Runner/tests: `scripts/run-bounded-command.py`, `scripts/test_run_bounded_command.py`.
- Instructions: `AGENTS.md`, `.agents/README.md`, backend-implementer agent, backend-python/tests rules, and the five skill files for antigravity-implement-handoff, backend-testing, bounded-command-execution, codex-create-handoff and codex-implementation-takeover.
- Workflow: current-owner-delivery-policy, agent-development-workflow, owner-phase-workflow and rag-phase-delivery-guardrails.
- Routing/scope: `handoffs/ACTIVE.md`, `handoffs/README.md`; this scope's README, slice-b1-watchdog, executable handoff, implementation handback and three prior independent implementation reviews.

Exact paths/hashes are in `review-inputs.json`. Compared with round 2's input snapshot, only runner, tests, ACTIVE, maintenance README, slice status and handback differ; lifecycle differences include prior reviewer status writes. Prior evidence for unchanged instruction contracts is reused. Actual recursive `scripts/` inventory was checked; no additional changed source/package/helper/dependency appeared. Application, `.codex`, hooks and frozen Phase 17/18 paths are unchanged.

Risk-applicable shared coverage: **PASS 1, 3, 5, 7–9, 12, 14, 23, 25–26, 29** for approved scope/structure, reuse/cohesion, validation, privacy/retention, focused timing evidence within its stated limits, dependencies/tooling and hygiene. **FINDING 2, 10, 16, 18, 21–22, 27** for F02 traceability, truthfulness, lifecycle/resources, fault coverage and cleanup metadata; **28 FINDING/LOW only** for preserved B1-L01. **N/A 4, 6, 11, 13, 15, 17, 19–20, 24** because no application API/ADR-0016 envelope, authentication, persistence, module or RAG-quality capability changes. B2 enforcement remains excluded and uncertified.

Review writes: this report, canonical ACTIVE/maintenance README/B1 status, fresh ignored evidence only. No code/test correction, staging or publishing. Phase 17 remains FROZEN, Phase 18 CLOSED, Phase 19 NOT AUTHORIZED, indexing INACTIVE and B2 gated. Historical decisions, raw receipts and LOW backlog remain preserved.

## Owner next action

**CHANGES_REQUESTED; 3/3 rounds exhausted.** Stop Antigravity remediation. Owner must decide whether to authorize a separately bounded `$codex-implementation-takeover` for the remaining F02 native-verification defect, followed by a separate independent review. This report does not authorize takeover or another implementation round. B1 is not ready for Owner commit; no commit message is provided.
