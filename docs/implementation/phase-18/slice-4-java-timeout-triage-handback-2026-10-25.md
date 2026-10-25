# Slice 4 full-Java timeout investigation — 2026-10-25

**INCONCLUSIVE. GPU_EVIDENCE_BLOCKED unchanged.** The aggregate watchdog cutoff is localized,
but the evidence does not establish why the full run's aggregate execution became slower or the
exact live stack at cutoff. This is the one separately Owner-authorized investigation checkpoint.
No correction, timeout increase, test/runner/source alteration, full-gate retry or acceptance occurred.

## Exact timeout location and original evidence

The genuine unchanged full command started **2026-10-25 01:34:59.263 UTC**, with a 2,100-second cap.
Its cap expired at **02:09:59.263 UTC**; the receipt finished after termination at **02:09:59.840 UTC**,
duration **2,100.577 seconds**, exit **124**, `TIMEOUT / windows_tree_termination_requested`.
Maven's launcher root was PID **15684**; the Surefire test JVM was PID **16200**. The post-timeout
observation found neither those PIDs nor direct children. It did not prove all Docker/descendant
cleanup, and contains no live pre-kill thread/CPU/memory/PostgreSQL sample.

**Last completed reported suite:** `RagSlice4ReindexIntegrationTest`, 2 tests, 0 failures/errors/skips,
21.80 seconds. **Last demonstrably completed method path:**
`RagSlice4ResourceIntegrationTest.actualColdWarmRootsApprovedNestedChildrenAndSameRevisionReindex()`.
Its after-each native binding at **02:09:01.896700 UTC** contains two COMPLETED jobs: two successful
roots followed by one successful reindex root. The resource-workload files record cold/warm and
same-R reindex. There is no completed resource-suite XML; that native record is not itself a final
Surefire PASS for an unreported method.

**Remaining/active method identified from the sequential two-method class:**
`RagSlice4ResourceIntegrationTest.actualNearInputByteCeilingIncludesStructuredSourceAndApprovedChild()`.
The next setup marker is **02:09:02.009 UTC**. Only that method remained; there is no corresponding
near-workload record, second after-each native binding or completed resource-suite report. This
localizes the outstanding test, but not its exact executable line: no original thread dump or
per-method start/finish event was retained. Whether it was in preparation, producer execution or
another native phase at the instant of termination remains **UNKNOWN**.

| Original marker | UTC | Time remaining to outer cap |
| --- | --- | ---: |
| Resource class first setup | 02:08:46.619 | 72.644 s |
| Cold/warm workload record | 02:08:51.213021 | 68.050 s |
| Selected-R reindex workload record | 02:08:58.761951 | 60.501 s |
| First method after-each native binding | 02:09:01.896700 | 57.366 s |
| Next method setup | 02:09:02.009 | **57.254 s** |
| Watchdog cap | 02:09:59.263 | 0 |

Thus the second method had **at most about 57 seconds after its setup marker**, despite its
unchanged class-level **240-second JUnit limit**. The prior complete `final-java-v3` receipt records
that near method at **56.718 seconds** and the whole resource class at **68.515 seconds**. These
are separate observations, not a guarantee the later run would finish at the same speed.

Completed report scope stays **PARTIAL AFTER TIMEOUT**: **1,265 testcase elements / 0 failures /
0 errors / 0 skips**, versus **1,252** suite-attribute tests. The already established 13-name
Surefire count collision is unchanged. There is no final aggregate summary, completed remaining
regression result, package or verify proof. No partial count is called full PASS.

## One minimum focused reproduction

Ran **only the outstanding near-input method**, with the actual final compiled candidate,
unchanged installed fixture correction, 30,000 ms recovery schedule, original workload, original
240-second JUnit limit and a **240-second external watchdog**. No observer agent, 50 ms schedule,
external recovery lock or source substitution was used. The command used the existing JUnit 6.0.3
launcher and dependencies, not Maven: existing `backend/target` reports/classes remain intact.

**Result: 1 PASS / 0 failed / 0 skipped.** Test lifecycle **65.239912 seconds**, entire command
**87.875 seconds** including isolated Spring/Testcontainers startup and shutdown, exit 0.
The lifecycle includes setup/teardown; it is not a pure inference timing. Its original publication
assertion passed. The authentic resource receipt records the near-bound Markdown/approved child
input and producer result. No GPU capability is inferred from CPU success.

An isolated read-only observer collected JUnit events, main-thread stacks, Java monitor-deadlock
checks, heap samples, owned PID/creation identities and CPU/RAM/I/O samples. It queried the disposable
fixture's PostgreSQL `pg_stat_activity`/`pg_blocking_pids` through its own read-only admin connection.
Two owned-JVM `jcmd Thread.print` and `GC.heap_info` snapshots were captured. Polling and attach
perturb execution timing; the result is diagnostic evidence, not a new final resource benchmark.

### Observed focused execution state

- Java progressed through **producer HTTP/result admission**, JSON/result work, then
  **trusted native replay**, followed by PostgreSQL publication. During replay the main thread was
  `TIMED_WAITING` in `RagDocumentResultVerifier.verify:69`, polling its owned process; it was not
  blocked on the query-budget coordination monitor.
- Owned producer Python **PID 12184** and trusted replay Python **PID 19300** accumulated sampled
  lifetime CPU of **88.921 s** and **94.953 s**, respectively. Their creation times and descendants
  are retained. These are parallel-process lifetime CPU observations, not elapsed time or a claim
  to exact Python instruction/native stack attribution. No Python stack was collected.
- No Java monitor/synchronizer deadlock was detected in sampled checks; no PostgreSQL blocking PID
  and no Hikari connection-pool waiter appeared. Sampled pool total/active/waiting values and all
  PostgreSQL states are retained. These observations apply to this focused run, not retrospectively
  to the killed full JVM.
- Simultaneous sampled owned-tree working set peaked at **1,327,763,456 bytes**, private memory at
  **1,518,739,456 bytes**. Maximum sampled Java heap used/committed was **425,536,768 / 450,887,680**
  bytes. Samples are not guaranteed peaks. Per-PID disk-transfer counters are recorded; no global
  disk/temp peak or full-run headroom is invented.
- The focused test uses its own Docker PostgreSQL container and actual loopback worker/native
  processes. It completes without disabling tests or extending any bound. Maven/Surefire launcher
  processes are absent from this direct focused run; it is not Maven/full-suite acceptance.

## Database warnings and earlier elapsed work

The original refused connection is **localhost:52314 / `ppv_clean_startup`**, the separate
`FlywayStartupIntegrationTest.CLEAN_POSTGRES` fixture. The main RAG database is
**localhost:64608 / `personal_private_vault`**. The failed port appears in earlier startup context
logs and scheduled `RagRecoveryService`/Hikari reconnection stacks well before the resource class.
Its pool reports `total=0, active=0, idle=0` while later main RAG suites complete.

The separate test's `@Container` lifetime and cached Spring scheduling account for a plausible
stale-context warning path. **The warning is not proof that the resource test was blocked on that
database**, and its contribution to host load or aggregate runtime is not measured. Do not replace
this distinction with an unsupported “PostgreSQL deadlock/pool exhaustion caused the timeout” claim.
The original live database waits, locks and connection counts were not retained.

Several already completed suites were slower than the preceding complete `final-java-v3` run:

| Completed suite | Timed-out run | Prior complete run | Difference |
| --- | ---: | ---: | ---: |
| RagSlice3CorrectiveIntegrationTest | 742.238 s | 626.630 s | +115.608 s |
| RagSecondJvmRecoveryIntegrationTest | 278.590 s | 206.248 s | +72.342 s |
| RagFinalizerOwnerConcurrencyIntegrationTest | 106.748 s | 45.880 s | +60.868 s |
| RagSlice4CorrectiveIntegrationTest | 285.764 s | 245.410 s | +40.354 s |
| RagLifecycleIntegrationTest | 66.360 s | 26.207 s | +40.153 s |
| RagDocumentBatchIntegrationTest | 36.682 s | 29.548 s | +7.134 s |

These six descriptive deltas total **336.459 seconds**. They localize substantial elapsed growth
**before** the outstanding resource test; they do not establish CPU contention, memory pressure,
connection contention or a particular Slice 4 regression as its cause. No pre-timeout host/process
telemetry was retained to make that attribution. Repeating these completed long suites would not
reconstruct the killed run and was not done in this finite checkpoint.

## Impact of the verified fixture correction

The coordination predicate matches only the exact frozen `RagSourceAuthorityIntegrationTest`
query-budget method. **That class was not reached in the timed-out full stdout.** Unrelated methods
call `invocation.proceed()` immediately, before any recovery bean lookup or lock. The focused
resource stacks follow this bypass path and the method completes.

There is therefore no evidence of the correction holding the recovery monitor over resource
testing or causing a broad lock/deadlock. No claim of zero registration overhead or proof about all
aggregate timing effects is made. Its source remains exactly
`9291ce7b3b0c4ae753873cbd3108ebf85b802cef554317f393953f9cbb97bb47`.
All **1,732** source entries match the failed full command's manifest. No other candidate source
change was introduced while diagnosing this timeout.

## Root-cause disposition, smallest action and 2,100-second feasibility

**Proven:** the outer aggregate deadline expired after earlier completed work consumed almost all
of the cap; the outstanding test was cut off before its own 240-second limit. Its unchanged focused
execution completes with progressing CPU-active native work and no observed DB/monitor blocker.
The focused 65.240-second lifecycle is longer than the original 57.254-second remaining window.

**Not established:** the original live stack at cutoff, why the earlier completed suites were
slower, whether the original native work would have completed normally, or a reproduced abnormal
stall causing the full timeout. A passing isolated run cannot exclude full-suite/environment
interference. **Return INCONCLUSIVE**, rather than declaring a reproduced production root cause.

**Smallest justified corrective action: no application, fixture, assertion, test-selection, runner
or budget change is justified yet.** Preserve the verified fixture correction. If separately
authorized, the next useful action is one genuinely instrumented full-gate attempt with per-test
start/finish, owned-tree/CPU/RAM, pre-cutoff thread dumps and current-database waits captured during
the long prefix. That is a proposal for new evidence, not permission for a full retry in this session.
Do not perform speculative performance/context-lifecycle fixes based on warning volume alone.

**Can the existing 2,100-second gate genuinely complete?** The earlier complete command finished
in **1,848.691 seconds** but FAILED a query assertion; it demonstrates historical execution
feasibility, not a successful gate for the corrected candidate. This candidate's failed attempt
did not complete. The present focused result does not certify all remaining tests/package/verify
will fit. Neither impossibility under 2,100 seconds nor a necessary contract/budget increase is
established. The limit stays unchanged; no Owner budget-change request or waiver is inferred.

## Evidence preservation and stop

Exact commands/cwd/limits/overrides, receipts, chronology, per-suite XML comparisons, process/PG
samples, thread/heap dumps, source hashes and preservation checks:
[machine-readable timeout evidence](slice-4-java-timeout-triage-evidence-2026-10-25.json).
All new raw artifacts are under `logs/phase18/s4-java-timeout-triage-20261025/`, with fresh output
paths. No historical full-gate output, backend target output or fixture source was overwritten.
The timeout receipt, partial XML and previous failures retain their original FAILED/TIMEOUT status.
HEAD/raw and semantic Git index, source/fixture/runner hashes and protected evidence are checked.

No source, test, dependency, model, CUDA, runner or timeout modification. Frozen Phase 17 and
production code remain intact. D24 `.md`-only, Slices 1–3/FR18-3 CLOSED, D22 NOT_RECOVERED,
external indexing INACTIVE, live originals DISABLED/DEFERRED; **GPU_EVIDENCE_BLOCKED unchanged**.
Slice 4/P18-FINAL NOT ACCEPTED. No staging/commit/push/tag/PR or activation. Stop after this
investigation; any further full gate or correction requires separate Owner authorization.
