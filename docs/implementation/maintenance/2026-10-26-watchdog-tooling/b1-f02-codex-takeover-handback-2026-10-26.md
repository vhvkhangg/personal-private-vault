# Maintenance B1-F02 — Codex takeover handback

- Date / authorship: 2026-10-26 / **Codex implementer**, including implementation self-review; not an independent acceptance review.
- Status: **IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED**. **B1-F02 HIGH remains OPEN** until a separate reviewer verifies the correction. No `READY_FOR_OWNER_COMMIT` or commit message.
- Authority: Owner explicitly authorized takeover limited strictly to **B1-F02**, after three independently blocked Antigravity correction rounds; unchanged [B1 contract](b1-executable-handoff-2026-10-26.md), [ACTIVE](../../handoffs/ACTIVE.md) and [Owner policy](../../../workflow/current-owner-delivery-policy.md).
- Entry: `main` HEAD `dfc0c2052ffd5272ba63e6529d0bb401f1e5c022`; pre-existing submitted B1 working tree retained. Entry inventory/hashes and index content are saved in `entry-inputs.json` below.

## Remediation ledger and transition

The [initial blocking review](reviews/2026-10-26-b1-independent-final-review.md) is not a corrective round. Each entry below is an actual submitted Antigravity correction followed by independent Codex `CHANGES_REQUESTED`, all dated **2026-10-26**. Immutable input inventories preserve the submitted correction identities; earlier implementer reports are not reconstructed or rewritten.

| Round | Preserved correction evidence | Independent report / exact remaining blockers |
| --- | --- | --- |
| 1/3 | `logs/maintenance-b/b1-codex-rr1-c31610ff6a114549b0c44278f51ec635/review-inputs.json` | [Re-review 1](reviews/2026-10-26-b1-independent-re-review-1.md): F01/F02 HIGH; F04/F05 MEDIUM residuals |
| 2/3 | `logs/maintenance-b/b1-codex-rr2-64c639ad2d7241ad8d90cc76f8a66047/review-inputs.json` | [Re-review 2](reviews/2026-10-26-b1-independent-re-review-2.md): F02 HIGH; F04/F05 MEDIUM residuals |
| 3/3 | [Antigravity round-3 handback](b1-implementation-handback-2026-10-26.md), preserved unchanged; `logs/maintenance-b/b1-codex-rr3-68702d5b10024d8aa16980ff50cb20b0/review-inputs.json` | [Re-review 3](reviews/2026-10-26-b1-independent-re-review-3.md): **F02 HIGH only**, native First/Next enumeration failures certify empty/partial snapshots |

Owner subsequently authorized this bounded correction in the current conversation. Codex became the implementer for F02 only; the Antigravity counter remains **3/3 exhausted**. F01/F03/F04/F05/F06/F07 retain their independent CLOSED status. B1-L01 and MA-A03–MA-A06 remain unchanged LOW backlog. Separate review is required; no fourth Antigravity round is granted.

## Change and observable acceptance

Only eight production lines were added to [the existing native helper](../../../../scripts/run-bounded-command.py). A false `Process32First` or `Process32Next` return now reads the native error immediately. Only `ERROR_NO_MORE_FILES` (18) permits normal empty/end-of-enumeration completion; any other error raises `OSError` with the original code before the existing `finally` closes the snapshot. This follows the documented [First](https://learn.microsoft.com/en-us/windows/win32/api/tlhelp32/nf-tlhelp32-process32first) and [Next](https://learn.microsoft.com/en-us/windows/win32/api/tlhelp32/nf-tlhelp32-process32next) boundaries.

The existing `timed_call(..., default=None)` and `descendants_verified` path handles that exception: cleanup becomes unavailable/incomplete, and an otherwise successful command returns **1 / COMMAND_FAILED**, rather than certifying no descendants from an incomplete snapshot. No new state, framework, dependency, cleanup strategy or timeout was introduced.

Two regressions in [the existing tests](../../../../scripts/test_run_bounded_command.py), `test_b1_29_f02_process32first_failure_not_pass` and `test_b1_30_f02_process32next_failure_not_pass`, exercise the actual helper using a narrow `ctypes.WinDLL` proxy:

- Native snapshot allocation and handle closure remain real; Next failure occurs after real First success, rejecting a partial map. Closure success and preservation of the error through closure are asserted.
- Error codes **5** and **0** must raise and route a real zero-exit parent plus sleeping owned child to non-PASS, `capture_complete=true`, `cleanup_complete=false`, and `descendant_verification_unavailable`. The child is confirmed alive before fixture-owned finally cleanup, then confirmed stopped.
- Code **18** remains a valid empty/end result at either boundary. Existing ordinary enumeration, unavailable-helper, lookup-timeout, owned-tree/sentinel and error-path behavior is retained.

## Verification and exact evidence

Fresh evidence root: [`logs/maintenance-b/b1-codex-takeover-f02-5148c3df1c4b48468a0d5e07162b6916/`](../../../../logs/maintenance-b/b1-codex-takeover-f02-5148c3df1c4b48468a0d5e07162b6916/). No existing evidence path was overwritten. `supervisor.py` was copied unchanged from round 3 into this new directory; it streams to exclusive files and supervises owned PIDs independently of the changed runner, with bounded tree cleanup on failure/timeout.

| Check | Actual result / evidence relative to root |
| --- | --- |
| Red: two new tests against entry runner | Exit **1**, four expected subtest failures (First/Next × codes 5/0), 0.406s outer; `red-supervisor.json`, `red.stderr.log`, `red.py` |
| Initial green / initial bootstrap / initial final | Passed; raw `green*`, `bootstrap*`, `final*`, `review-final*` retained. Superseded as final test identity by the subsequent explicit native-close assertion; production code did not change. |
| Final bootstrap: `python -B -m unittest discover -s scripts -p test_run_bounded_command.py -v` | Exit **0**, **30/30**, zero failures/errors/skips; 26.492s tests / 26.734s outer, independent **180s** cap; `verified/bootstrap-supervisor.json`, stdout/stderr |
| Final approved shape: `python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <root>/verified/review-final.json -- <resolved python> -B -m unittest discover -s scripts -p test_run_bounded_command.py -v` | Exit **0 / PASS**, **30/30**, zero failures/errors/skips; 26.545s tests / 27.063s wrapper / 27.234s outer; capture/cleanup complete. Independent **320s** cap; `verified/final-supervisor.json`, `verified/review-final.json`, raw streams |
| Original round-3 native failure probes, copied unchanged into fresh `native-failures/` | Driver exit **0**, 3.250s / **90s** outer cap. Both First/Next: **1 / COMMAND_FAILED**, cleanup incomplete/unavailable, capture complete, child alive before fixture cleanup. Lookup-timeout still rejects PASS. `native-failures/native-failure-results.json` and individual receipts/console logs retained. Driver exit alone is not contract acceptance. |
| Hygiene / scope / links | `git diff --check` and local Markdown-link validation passed; `scope-check.json` records only runner, tests and ACTIVE changed from entry plus this new handback, unchanged index and no missing entry files. Entry-relative source deltas retained as `*.takeover.diff`. |

Interpreter: `C:\msys64\ucrt64\bin\python.exe`, **3.11.7**, `os.name=nt`, `sys.platform=win32`; platform reports `Windows-10-10.0.26300-SP0`. No outer timeout occurred. Commands ran via polled host sessions; configured harness limits were 90/180/320s, with at most 20s owned cleanup. Short fixtures do not establish real 2100s host operation or universal OS containment. No Java/RAG/application suite was run.

Final SHA-256 identities:

- Runner: `914d84a9633caf1c238caa25118358a205561a447cee7cb14ef22be66b316d23`.
- Tests: `7a74793e2e55a08a9bc1c5b2f0d30c6f11832fd2bcc5e4d847b34f56b8789c5c`.

## Implementation self-review and independent handoff

Self-review checked the entry-relative delta, immediate error capture, both failure boundaries, rejection of partial enumeration, normal completion, real handle closure and fixture cleanup. Existing unavailable-verification, deadlines, diagnostics, streaming, capture/receipt privacy and unrelated-process safety mechanisms are reused. Applicable Python/bounded-command and documentation guidance was read; RAG runtime, format, model and Java guidance is outside this tooling-only correction. The complete focused suite includes the previously accepted native owned-child/grandchild/sentinel behavior. Historical logs/reviews and unrelated submitted files were preserved; no staging, commit, push, B2 or frozen-baseline change occurred.

This is an implementation self-check, **not** independent closure of B1-F02. Owner invokes the following in a **fresh/separate Codex session** under the takeover skill's independence rule:

> `$codex-final-review` — Independently review the Maintenance B1-F02 Codex takeover. Codex authored the fix and self-review in the prior session; you are the separate reviewer. Read ACTIVE, this handback, the unchanged executable contract and round-3 review. Verify the First/Next failure branches and focused/native evidence, reusing accepted unchanged B1 evidence. F02 remains HIGH/OPEN until your independent disposition; the three Antigravity rounds stay exhausted. Preserve historical evidence and all unrelated work; no B2, staging, commit or push.
