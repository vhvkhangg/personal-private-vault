# Slice 4 Java-gate fixture correction - 2026-10-25

**BLOCKED. GPU_EVIDENCE_BLOCKED unchanged.** Codex is the implementer; no independent
review, Slice 4/P18-FINAL acceptance or closeout occurred. This is the one explicit
Owner-authorized narrow fixture correction following the immutable October 25
triage handback/evidence. It does not reset historical remediation allowances.

## Exact correction

Only code file changed relative to entry: `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractRagIntegrationTest.java`.
The inherited JUnit InvocationInterceptor matches the exact frozen class and
method, then holds the existing Spring recovery bean's monitor across the complete
original invocation. Intrinsic release propagates assertions/exceptions/timeouts;
invocation.proceed is called once. Other methods/classes immediately proceed with
no recovery lookup/lock. No generic synchronization framework, scheduler disable,
global serialization, SQL filtering, counter manipulation or failure suppression.

Exact entry-relative diff: `logs/phase18/s4-java-fixture-20261025/fixture-correction.patch`.
Preexisting Hikari fixture changes remain intact and are absent from this patch.
Entry fixture SHA-256 `d209b329a17b2e23165057400370d5468928a7cf48fd7516eff8c50eea9c7137`;
final fixture SHA-256 `9291ce7b3b0c4ae753873cbd3108ebf85b802cef554317f393953f9cbb97bb47`.
The entire frozen Phase 17 test file remains byte-identical:
`d3676b2f95041cd2343088e0ad1b0ea6244e1e70b6861d91db318bba182b6fa1`; all original 5/6, 1/8-ID and byte/selection-budget assertions
remain unchanged. All production, recovery scheduling, source services, dependency/
plugin/model/Python source hashes match checkpoint entry. Only ACTIVE and these
new handback/evidence documents are synchronized prospectively.

## Verification on final source

Exact argv/cwd, watchdog bounds, exits/log/source hashes are in command-index.json
and commands.jsonl under the isolated evidence directory above. Focused archives
may contain unselected stale reports; only selected suite reports are counted.
The full gate starts with clean, so its archived reports are fresh.

| Check | Actual result |
| --- | --- |
| Original frozen method, default 30,000ms recovery | original-normal: exit 0, 1 PASS |
| Original frozen method, diagnostic 50ms recovery | original-50ms: exit 0, 1 PASS |
| Observation-only agent + installed fixture, 50ms | installed-50ms-observer: exit 0, 1 PASS; 5,6,5,6, all measured SQL on main; no external guard/counter change |
| Recovery/concurrency selection | recovery-concurrency: exit 0, 19 PASS, 0 failures/errors/skips |
| Exception/monitor/bypass diagnostic | invocation-propagation-probe: exit 0; identical assertion/error propagation, competing thread blocked during target invocation, release after failure, unrelated method/class bypass, actual recovery resumes |
| Genuine unchanged full clean verify | exit 124; actual testcase elements {'tests': 1265, 'failures': 0, 'errors': 0, 'skipped': 0}; suite attributes {'tests': 1252, 'failures': 0, 'errors': 0, 'skipped': 0} |

Full reporting scope: **COMPLETED_SUITE_REPORTS_ONLY_PARTIAL_AFTER_TIMEOUT**. The 2,100-second watchdog
timed out the command (exit 124) during Slice 4 resource testing. There is no final
aggregate stdout summary, no complete full-suite result and no completed package/
verify proof. Counts above cover completed reports only; unreported/in-flight and
remaining tests are NOT_VERIFIED. No failure/error was present in the completed
suite XML, which does not establish full PASS. The command was not rerun unchanged
for a favorable outcome and its deadline was not extended.

The isolated propagation probe supplies a controlled invocation callback; it is
not a replacement frozen-method execution or a full-gate proof. Its first compile
failed because two Void lambdas lacked return null; failed source/log/receipt are
preserved, followed by successful compile/run. No product/test assertion was changed.
The SQL observer is the existing diagnostic agent/identity inspector; no original
method or counter value is rewritten. Neither observer nor diagnostic 50ms property
is present in the full command.

Full command (repository root, 2,100-second executable watchdog):
`mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED clean verify`.
Source manifests for normal, fast, observer, concurrency and full commands match.
The original historical failure's exact extra SQL remains unknown; the new triage
mechanism and correction do not retroactively invent its missing trace.

Counts use both actual testcase elements and suite attributes. The established
Surefire grouping collision of equal method names across nested classes explains
the difference without missing testcase execution; full-java-counts.json records
actual suite/case details. The sole expected historical alias applicability skip
remains governed by the earlier narrow ADR-0023 Owner authorization; no new test
exclusion was added. Every prior failed gate/triage receipt remains unchanged.

## Preservation, static checks and stop state

Entry HEAD `ebe125f7529124b62761e8d01c692cbbd28e4aa7` and Git index unchanged. 81,449 historical assets
checked unchanged; original backend target output archived before compilation.
Entry/final per-file hashes, exact patch, selected/full case results and evidence
hashes are machine-readable in this directory and the accompanying new evidence.
Final git diff --check is recorded after documentation synchronization. No Python
static rerun was needed for this Java-fixture-only change; production/dependency
identity is unchanged. The full Maven attempt completed compilation and a subset
of tests, but its remaining regressions/package/verify are NOT_VERIFIED after the
timeout. No dependency/plugin upgrade or GPU experiment.

**BLOCKED** applies only to this Java-gate correction. GPU_EVIDENCE_BLOCKED is
unresolved; no CUDA installation, CPU-only amendment or GPU waiver. Slices 1-3
CLOSED, D24 Markdown-only, D22 NOT_RECOVERED; external indexing INACTIVE. Slice 4
and P18-FINAL remain NOT ACCEPTED. No staging/commit/push/tag/PR, activation,
independent review, new corrective cycle or closeout. Stop after handback.

Outstanding Java gate issue: {"required_java_gate": "FAILED", "failed": [], "unexpected_skips": [], "receipt": {"profile": "full-suite", "timeout_seconds": 2100.0, "executable": "mvn.cmd", "argv_sha256": "2a033f8dd643becad8a69842abc7a09977b3f9000a254035df3ddd38b4ca8f9b", "started_utc": "2026-10-25T01:34:59.263+00:00", "pid": 15684, "finished_utc": "2026-10-25T02:09:59.840+00:00", "duration_seconds": 2100.577, "result": "TIMEOUT", "exit_code": 124, "cleanup": "windows_tree_termination_requested"}}
