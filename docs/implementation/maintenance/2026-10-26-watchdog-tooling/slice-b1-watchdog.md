# B1 — Progress-aware watchdog (first Maintenance B slice)

- **Status:** `OWNER_APPROVED / PREPARATION_REVIEWED`; B1 is [`READY_FOR_OWNER_COMMIT`](../../handoffs/ACTIVE.md) after the [separate independent takeover final review](reviews/2026-10-26-b1-independent-takeover-final-review.md). All seven findings are independently CLOSED under the unchanged [executable handoff](b1-executable-handoff-2026-10-26.md); LOW backlog remains. Antigravity counter stays **`3/3` exhausted**, without a fourth round. Owner-authorized Codex F02 takeover is independently accepted; Owner publication is next. This document remains the approved preparation requirement inventory; B2 remains separately gated.
- **Dependency:** approved [Maintenance B scope](README.md), successful independent maintenance pre-handoff, and explicit B1 active handoff created by Codex. Never execute this preparation document directly as a handoff.
- **Implementer:** Antigravity, with mandatory self-review; Owner-authorized Codex takeover for F02 with its own self-check and separate independent review. **Independent reviewer:** Codex reviewing session, separate from takeover implementation. **Publication:** Owner only, after accepted slice review.

## Allowed modifications

- `scripts/run-bounded-command.py` (prefer modifying existing code rather than replacing it).
- `scripts/test_run_bounded_command.py` (focused unit/integration/synthetic process tests).
- `.agents/skills/bounded-command-execution/SKILL.md` and *minimal* call-site instructions necessary to match real executable behavior after verification. Do not update historical Phase 17/18 evidence, command receipts, schemas or RAG code.
- New helper file **only if unavoidable** for a narrowly demonstrated limitation; justify to Codex before expanding the handoff.

## Acceptance examples

| ID | Scenario | Mandatory observable outcome |
| --- | --- | --- |
| B1-01 | Ordinary child exits successfully in seconds | Wrapper returns child exit 0, writes `PASS` receipt and short console result; default hard cap is 300s. |
| B1-02 | Ordinary command remains alive at 300s | Reaches hard wall-time cap, captures finite diagnostic data where possible, terminates **owned** process tree, records `TIMEOUT` and nonzero exit. Cleanup grace must not create an unbounded wait. |
| B1-03 | Explicit approved large/full/complex test, eligible profile | Uses **at most 2100s**, not an arbitrary larger cap; command approval and shape must be checked. A quiet-but-healthy command is not auto-killed after 300s. |
| B1-04 | A large test emits nothing for 300s but makes demonstrable CPU/I/O/test-report progress | Emits a bounded diagnostic/progress receipt, continues before its hard cap, reports neither `HUNG` nor success without child exit. |
| B1-05 | A large test reaches 300s without output and objective evidence confirms deadlock/hang | Captures concise evidence, stops only launched process tree early, reports nonzero `HUNG_CONFIRMED`; do not label mere quiet/sleep as confirmed hang. |
| B1-06 | No stdout and no reliable process-health or test-progress evidence | Mark diagnosis inconclusive; **do not infer a hang**, continue until an applicable hard cap or exit. |
| B1-07 | Process exits with nonzero code | Report `COMMAND_FAILED` promptly, preserve exit status where meaningful, keep one actionable error and full output in a *new* ignored log file. |
| B1-08 | Child and grandchild time out; unrelated sentinel process exists | Kill only wrapper-launched descendants; prove sentinel survives in actual Windows test. Report best-effort limitations if OS does not permit reliable termination. |
| B1-09 | Cancellation/KeyboardInterrupt or diagnostic exception | Bounded cleanup, truthful non-PASS receipt, no leaked resource handles or hung wrapper. Never kill unrelated `java.exe`/Docker instances. |
| B1-10 | Existing `logs/` contains original historical receipts | Never overwrite, truncate, move or delete any existing path. Use unique run-scoped paths; ensure no raw arguments/env/credentials in receipt and avoid emitting source/private data in console summaries. |
| B1-11 | Large stdout/stderr output and a silent subprocess | Stream output to new local files without holding the entire stream in memory; show concise terminal status and evidence location. Do not hide error details from the saved evidence. |
| B1-12 | Invoker (Codex/Antigravity) has an earlier host/tool timeout | Detect/document that the wrapper cannot override an outer host time limit; no claim of complete 2100s coverage absent a real test. |

## Minimal implementation preference

- Prefer the existing Python standard library, `subprocess`, monotonic time, bounded output streaming, and platform-native read-only probes. Avoid new broad process supervision libraries, services, instrumentation agents, plugins or dependencies unless a real Windows gap is demonstrated and reviewed.
- Define **observable output** separately from **meaningful progress** (e.g., report/log file advancement, process CPU/I/O when reliably attributable). stdout inactivity alone cannot justify a hang verdict. A single unchanged sample must not falsely imply deadlock.
- For a normal 300s cap, diagnose and terminate at the cap; for approved extended tasks, 300s of inactivity triggers diagnostics rather than automatic termination. Repeat inspection with a sensible bounded interval, without a busy loop or endless grace period.
- Preserve backward compatibility for the ordinary `--profile`, `--timeout-seconds`, `--receipt` and executable-argv usage whenever possible. If the approved complex profile needs a new option, keep it narrowly guarded and update the skill/examples. Do not silently reinterpret previous historical receipts.

## Focused verification

- Extend the existing 5-test watchdog suite with short synthetic tests by overriding time thresholds, not actually waiting 300s or 2100s in unit tests. Exercise stdout, stderr, progress/silence, inconclusive health, early nonzero exit, log collision, invalid extended eligibility, interrupt, owned Windows tree kill, unrelated sentinel survival, Unicode/long lines and bounded output-memory behavior where applicable.
- On the Owner's Windows host run focused tests and one narrow actual subprocess integration, recording actual commands/exits/limits in unique `logs/maintenance-b/b1-*` paths; never overwrite old logs. A POSIX pass does not prove Windows semantics. Do not run entire Java/RAG suites just because this is a maintenance slice.
- Self-review must check KISS, owner-only Git, no new package/framework, clean code, meaningful tests, data privacy, and exact acceptance. Codex performs the independent B1 slice review and blocks unresolved MEDIUM+ or mandatory NOT_VERIFIED.

## Explicit exclusions

No application/RAG runtime changes; no deployment, model, shell framework rewrite, general service monitor, background resident daemon, filesystem watcher, historical log cleanup, Phase 19, or B2 CLI rules/hook implementation.
