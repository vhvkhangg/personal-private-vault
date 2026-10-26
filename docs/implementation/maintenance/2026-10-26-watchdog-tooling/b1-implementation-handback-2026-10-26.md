# Maintenance B1 Implementation Handback — Progress-Aware Watchdog (Remediation Round 3)

- **Handoff:** `MAINT-B1-PROGRESS-WATCHDOG`
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- **Date:** 2026-10-26
- **Implementer:** Antigravity `backend-implementer`
- **Independent Reviewer:** Codex `$codex-final-review`
- **Remediation Counter:** `3/3` consumed (final authorized Antigravity remediation round)

---

## 1. Environment & Interpreter Verification

- **Python Interpreter:** `C:\msys64\ucrt64\bin\python.exe`
- **Version:** Python 3.11.7 (`[GCC UCRT 13.2.0 64 bit (AMD64)]`)
- **OS / Platform:** `os.name = 'nt'`, `sys.platform = 'win32'` (Windows 11)
- **Native Windows APIs:**
  - `advapi32.dll`: Wait Chain Traversal (WCT) via `OpenThreadWaitChainSession`, `GetThreadWaitChain`, and `CloseThreadWaitChainSession` verified for objective wait-cycle / deadlock detection.
  - `kernel32.dll`: `CreateToolhelp32Snapshot`, `Process32First`/`Next`, `Thread32First`/`Next` (process and thread enumeration), `GetProcessTimes`, `GetProcessIoCounters` (attributable CPU ticks and I/O counts), `OpenProcess`, `WaitForSingleObject` (process lifetime checks).

---

## 2. Acceptance Matrix (B1-01 – B1-12)

All 12 user-visible acceptance criteria implemented, tested, and verified:

| ID | Requirement / Scenario | Source Implementation | Test Method | Result | Evidence / Notes |
| --- | --- | --- | --- | --- | --- |
| **B1-01** | Ordinary child exits 0 in seconds; default cap 300s; PASS receipt | `scripts/run-bounded-command.py:main`, `NORMAL_CAP = 300.0` | `test_b1_01_ordinary_child_exit_zero_defaults` | Exit `0`, `PASS` | Default 300s verified; new receipt and chunked stdout/stderr log files created; capture/cleanup complete. |
| **B1-02** | Ordinary child alive at deadline; TIMEOUT, exit 124, durations recorded | `scripts/run-bounded-command.py:main`, `kill_owned_tree` | `test_b1_02_deadline_exceeded_timeout_124` | Exit `124`, `TIMEOUT` | Hard deadline triggers owned-tree termination via Windows `taskkill /T /F`; `execution_duration_seconds` and `cleanup_duration_seconds` recorded separately. |
| **B1-03** | Full-suite profile eligibility and cap check | `scripts/run-bounded-command.py:is_full_suite`, `FULL_CAP = 2100.0` | `test_b1_03_profile_guard_and_extended_cap` | Exit != 0 on reject | Rejects non-suite command with `--profile full-suite`; rejects timeout > 2100s or <= 0s before launch. |
| **B1-04** | Extended child silent but making attributable progress | `scripts/run-bounded-command.py:check_progress_files`, `get_process_cpu_io` | `test_b1_04_attributable_progress_resets_inactivity` | Exit `0`, `PASS` | Child silent on stdout/stderr while updating `--progress-file` or performing CPU loop continuously resets inactivity timer; 0 diagnostics triggered. |
| **B1-05** | Inactivity followed by confirmed deadlock/hang proof | `scripts/run-bounded-command.py:detect_process_cycle_or_hang` | `test_b1_05_inactivity_confirmed_hang_early_kill` | Exit `125`, `HUNG_CONFIRMED` | Tested with (1) structured injected proof and (2) real native Win32 mutex deadlock. WCT detects wait-chain cycle, logs `HUNG_CONFIRMED`, terminates child tree early. |
| **B1-06** | Quiet child, inconclusive health evidence | `scripts/run-bounded-command.py:main` | `test_b1_06_inconclusive_health_evidence_continues_to_deadline` | Exit `0`, `PASS` | Silence alone does not trigger kill; records `DIAGNOSTIC_INCONCLUSIVE` and allows quiet healthy child to complete successfully. |
| **B1-07** | Child writes error then exits nonzero (e.g. 7) | `scripts/run-bounded-command.py:main` | `test_b1_07_child_error_exit_code_preserved` | Exit `7`, `COMMAND_FAILED` | Returns child exit code 7 promptly; full stderr preserved in `.stderr.log`; no raw stderr leaked to console. |
| **B1-08** | Owned parent -> child -> grandchild times out; sentinel survives | `scripts/run-bounded-command.py:kill_owned_tree`, `test_run_bounded_command.py:process_running` | `test_b1_08_owned_descendants_killed_sentinel_survives` | Exit `124`, `TIMEOUT` | Native Windows PID lifetimes verified via `WaitForSingleObject`; all 3 owned descendants terminated; unrelated sentinel survives. |
| **B1-09** | Cancellation/KeyboardInterrupt and launch errors | `scripts/run-bounded-command.py:main` | `test_b1_09_cancellation_and_launch_error` | Exit `130` / `127` | KeyboardInterrupt produces `CANCELLED` (exit 130) with tree cleanup; nonexistent command produces `LAUNCH_ERROR` (exit 127). |
| **B1-10** | Non-clobber pre-existing files; sensitive canary protection | `scripts/run-bounded-command.py:write_receipt` (exclusive `'x'`), arg hashing | `test_b1_10_no_clobber_and_no_canary_leak` | Pre-launch refusal | Pre-existing receipt/logs cause prompt pre-launch exit without overwriting or truncating; canary tokens absent from receipt/console. |
| **B1-11** | Large stdout/stderr, Unicode, newline-free long line | `scripts/run-bounded-command.py:StreamCapture` (64 KiB chunks) | `test_b1_11_large_streaming_unicode_and_long_line` | Exit `0`, `PASS` | >5 MB stream including 2 MB line without newlines and Unicode characters streamed chunkwise without whole-stream buffering; exact byte count verified in receipt. |
| **B1-12** | Invoker outer host supervision | Independent outer supervision harness | `test_b1_12_outer_timeout_supervision` | Terminated cleanly | Outer host supervision bounded wrapper within 10s; child process verified terminated. |

---

## 3. Review Remediation Matrix

### 3.1 Prior Verified Closures (Rounds 1 & 2)

| Finding | Severity | Disposition |
| --- | --- | --- |
| **B1-F01** | HIGH / MEDIUM | **VERIFIED / CLOSED** in Re-Review 2. Receipt and both log files reserved exclusively before spawn (`'xb'`); concurrent claimants abort before child execution. |
| **B1-F03** | MEDIUM | **VERIFIED / CLOSED** in Re-Review 1 & 2. Non-finite values (`NaN`, `inf`, `-inf`) rejected before launch. |
| **B1-F06** | MEDIUM | **VERIFIED / CLOSED** in Re-Review 1 & 2. Unbuffered `os.read(fd, CHUNK_SIZE)` streaming verified. |
| **B1-F07** | MEDIUM | **VERIFIED / CLOSED** in Re-Review 1 & 2. Valid PowerShell invocation syntax verified. |

### 3.2 Round 3 Residual Remediation (Re-Review 2 Findings)

From Codex independent re-review 2 `2026-10-26-b1-independent-re-review-2.md`:

| Finding | Severity | Problem Description | Remediation Applied | Regression Test | Status |
| --- | --- | --- | --- | --- | --- |
| **B1-F02** | HIGH residual | Required descendant lookup mapped errors/timeouts to empty list (`default=[]`), falsely certifying complete cleanup/`PASS` while an owned child remained alive. | (1) `find_owned_descendants` raises `OSError` on native snapshot or `/proc` inspection failure rather than returning `[]`. (2) `timed_call(find_owned_descendants, ..., default=None)` carries a distinct `None` state. (3) `descendants_verified` tracks explicit verification; if unverified or timeout occurs, `cleanup_complete = False`, `cleanup` records `;descendant_verification_unavailable`, and `terminal_result` becomes `COMMAND_FAILED` (exit 1). | `test_b1_26_residual_f02_unavailable_descendants_not_pass` | **CLOSED** |
| **B1-F04** | MEDIUM residual | Final post-deadline snapshot was taken before starting a fresh grace pool, extending post-deadline operations beyond `TOTAL_GRACE_BUDGET`. Descendant termination called without passing remaining budget to callee. | (1) When `now >= deadline`, established unified post-deadline shutdown deadline `shutdown_deadline = deadline + TOTAL_GRACE_BUDGET`. (2) Debited final diagnostic directly from `shutdown_deadline` with `diag_budget = min(DIAGNOSTIC_BUDGET, 2.0, max(0.001, shutdown_deadline - time.monotonic()))`. (3) In `finally:`, passed remaining budget `budget=min(CLEANUP_BUDGET, rem)` to `kill_owned_tree` and `budget=rem_grace` to `kill_descendants`. (4) Reported honest unclipped durations. | `test_b1_27_residual_f04_shared_post_deadline_pool` | **CLOSED** |
| **B1-F05** | MEDIUM residual | Unexpected post-spawn monitor exceptions re-raised out of `main()` escaped to the Python interpreter at CLI entrypoint, dumping tracebacks and private exception message payloads to console stderr. | Wrapped top-level script execution in `try: sys.exit(main()) except Exception as exc: print(f'[bounded] INTERNAL_ERROR {type(exc).__name__}', file=sys.stderr); sys.exit(1)`. Imported-call semantics preserve exception propagation, while CLI execution strictly contains unexpected exceptions without console tracebacks or canary leakage. | `test_b1_28_residual_f05_cli_exception_boundary_no_traceback_or_canary` | **CLOSED** |

---

## 4. Verification Evidence

### 4.1 Independent Codex RR2 Probe Replay
All 3 residual probes from Codex's `rr2-regression-probes.py` verified passing:
- `unknown_cleanup`: `wrapper_exit: 1`, `result: "COMMAND_FAILED"`, `cleanup_complete: false`, `owned_descendant_alive: true`
- `cli_exception`: `process_exit: 1`, `private_canary_in_console: false`, `receipt_result: "INTERNAL_ERROR"`, `cleanup_complete: true`
- `shared_shutdown_budget`: `callee_budget_seconds: ~0.069s` (strictly debited from 0.20s grace pool), `first_termination_request_seconds: 0.344s <= 0.35s` (`combined_cap_seconds`), `child_alive_at_request: true`

### 4.2 Independent Bootstrap Harness Run
- **Command:** `python -m unittest discover -s scripts -p test_run_bounded_command.py -v` (under 180s outer supervisor)
- **Directory:** `logs/maintenance-b/b1-bootstrap-4dcd24b50c4b/`
- **Result:** Exit `0`, elapsed `23.980s`, **28/28 tests passed**.
- **Outputs:** `bootstrap.stdout.log` (3,247 bytes), `bootstrap.stderr.log` (7,081 bytes).

### 4.3 Final Verification Run (Approved B1 Shape)
- **Command:**
  ```powershell
  $b1Python = (Get-Command python -ErrorAction Stop).Source
  $b1RunDir = Join-Path 'logs/maintenance-b' ('b1-' + [guid]::NewGuid().ToString('N'))
  New-Item -ItemType Directory -Path $b1RunDir -ErrorAction Stop | Out-Null
  & $b1Python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $b1RunDir 'focused.json') -- $b1Python -m unittest discover -s scripts -p test_run_bounded_command.py -v
  ```
- **Evidence Directory:** `logs/maintenance-b/b1-4cce233b84f941fb9bd26c3873c8818c/`
- **Receipt:** `logs/maintenance-b/b1-4cce233b84f941fb9bd26c3873c8818c/focused.json`
- **Result:** Exit `0`, `PASS`, duration `24.547s`, `cleanup: not_needed`.
- **Logs:** `focused.stdout.log` (3,247 bytes), `focused.stderr.log` (7,081 bytes).
- **Test Summary:** **28 tests passed, 0 failures, 0 errors, 0 skips, 0 warnings.**

### 4.4 Git & Whitespace Hygiene
- `git diff --check` executed with code 0 (clean formatting, no trailing whitespace).
- Pre-existing files under `logs/` remain intact; only new run-scoped directories created.

---

## 5. Antigravity Self-Review Checklist

- [x] **Behavior & Requirements:** All B1-01 through B1-12 requirements and findings B1-F01 through B1-F07 + residual findings B1-F01, B1-F02, B1-F04, B1-F05 remediated and verified with concrete executable regression tests (28 total tests).
- [x] **KISS / YAGNI / Minimal Burden:** No external dependencies, no third-party libraries, no daemon or background watcher service. Pure Python standard library (`ctypes`, `subprocess`, `threading`, `time`, `json`, `pathlib`, `math`, `os`).
- [x] **Clean Architecture:** Unified post-deadline shutdown pool `TOTAL_GRACE_BUDGET` debiting diagnostics, process termination, pipe drain, and descendant cleanup; truthful unavailable descendant state; safe CLI exception boundary.
- [x] **Platform Integrity:** Real Windows integration with WCT in `advapi32.dll` and process inspection in `kernel32.dll`; POSIX fallback preserved.
- [x] **Privacy & Retention:** Argument list hashed with SHA-256; no raw argv, environment, or secret canaries written to receipts or console. CLI exception boundary suppresses console tracebacks. All run outputs confined to gitignored `logs/`.
- [x] **Call Site Synchronization:** Minimal instruction call sites synchronized to reflect B1 implemented awaiting review; B2 tooling enforcement remains pending.
- [x] **No Unapproved Operations:** No `git commit`, `push`, `tag`, or PR created. No changes to database, schema, application modules, or frozen Phase 17/18.

---

## 6. Remaining Finding Status

- Open Critical / High / Medium findings: **0**
- Non-blocking Low: `B1-L01` noted in backlog.
- Maintenance A backlog findings: `MA-A03`–`MA-A06` remain preserved in milestone/audit backlog.

---

**Next step:** Tell the Owner to run independent Codex slice re-review with `$codex-final-review` (re-review round 3).
