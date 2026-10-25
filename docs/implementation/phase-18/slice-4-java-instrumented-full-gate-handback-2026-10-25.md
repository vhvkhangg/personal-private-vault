# Slice 4 instrumented Java full-gate verification - 2026-10-25

**JAVA_FULL_GATE_VERIFIED. GPU_EVIDENCE_BLOCKED unchanged.** This is the one additional
Owner-authorized verification execution, not independent acceptance or a corrective cycle.
Slice 4 and P18-FINAL remain **NOT ACCEPTED**. No production, test, fixture, dependency,
plugin, model, Maven configuration, runner or timeout change was made.

## Genuine command and complete result

Executed exactly once from the repository root:

```text
mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED clean verify
```

The existing pinned `.venv-d17/Scripts/python.exe` launched the unchanged
`scripts/run-bounded-command.py`, `--profile full-suite --timeout-seconds 2100`.
Receipt: **2026-10-25 02:53:18.621 to 03:25:36.535 UTC**, **1,937.915 seconds**, exit **0**,
`PASS / cleanup=not_needed`. The unchanged cap had **162.085 seconds** remaining.
Maven reports **BUILD SUCCESS** and **32:16 min**. Clean, toolchain enforcement,
production/test compilation, Surefire, JAR creation and Spring Boot repackaging completed.
Successful `clean verify` reached the requested lifecycle endpoint; no additional verify-bound
plugin execution is invented. Maven/Java versions: **3.9.15 / OpenJDK 25.0.2**.

| Complete fresh report measure | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| Actual testcase elements / final stdout | 1,580 | 0 | 0 | 1 |
| Sum of suite attributes, 142 XML files | 1,567 | 0 | 0 | 1 |

The **13** difference is the retained, independently explained Surefire equal-method-name
grouping across nested classes: Collection 2, Fiction 3, Knowledge 1, Location 6, Media 1.
The new XML inventory includes every classname/name and collision group. These counts are
reported separately, not deduplicated into a fabricated total.

The sole skip is `RagWindowsNativePrerequisiteTest.attemptsAliasCreationAndRemovalDuringProtectedLifetime`:
the existing absent `ppv.rag.legacy-live-alias-diagnostic` opt-in, explicitly governed by
ADR-0023's superseded hostile-alias lifetime obligation. No new exclusion, property, skip,
test selection, assertion relaxation or GPU waiver was added. No mandatory current Java
test failed or was newly skipped. Historical failed/timeout gates remain FAILED/TIMEOUT.

## Chronology and the previously outstanding tests

All observed suite starts/finishes, final case names/durations and report mtimes are retained.
Method starts are not universally emitted by the unchanged Surefire configuration; sampled
stacks and per-method native bindings supply additional observations, not invented timestamps.
The binding write is an after-each observation, not itself an independent test PASS event.

| Relevant suite | This run | Previous timed-out run | Difference |
| --- | ---: | ---: | ---: |
| RagDocumentBatchIntegrationTest | 29.888 s | 36.682 s | -6.794 s |
| RagFinalizerOwnerConcurrencyIntegrationTest | 47.003 s | 106.748 s | -59.745 s |
| RagLifecycleIntegrationTest | 26.201 s | 66.360 s | -40.159 s |
| RagSecondJvmRecoveryIntegrationTest | 210.407 s | 278.590 s | -68.183 s |
| RagSlice3CorrectiveIntegrationTest | 663.397 s | 742.238 s | -78.841 s |
| RagSlice4CorrectiveIntegrationTest | 264.933 s | 285.764 s | -20.831 s |

These six descriptive reductions total **274.553 seconds**. They are not a demonstrated
explanation of the earlier run's slowdown. The prior killed run lacked live telemetry.

The resource class start was observed at **1,680.578 seconds** (03:21:19.008 UTC), with
about **419 seconds** left, rather than the previous run's approximately 73 seconds at first
resource setup. Observed event timestamps can lag emission; they are not exact method starts.
`RagSlice4ResourceIntegrationTest` completed **2/0/0/0 in 70.896 seconds**:

- Cold/warm roots, approved children and same-revision reindex: **12.312 seconds**.
- Near-input ceiling with structured source and approved child: **58.578 seconds**.

Authentic cold/warm, selected-R reindex and near-bound workload JSON files were produced.
The previously failing `RagSourceAuthorityIntegrationTest` completed **9/0/0/0 in 8.576 seconds**,
including the unchanged frozen query-budget method with the preserved fixture correction.
Worker-boundary and remaining Vault/web regressions then completed before packaging.

## Live observations and their limits

Fresh isolated evidence root:
`logs/phase18/s4-java-instrumented-20261025-201e93c2/`.
Original backend output was archived **before clean** with its own file manifest; fresh final
backend output was archived separately. Raw stdout and stderr are separate unchanged byte logs.
Only evidence-path and no-bytecode environment overrides were supplied; exact values are recorded.

External observation used 5-second log/report checks, approximately 15-second owned-tree/system
samples, 30-second read-only PostgreSQL sampling, 60-second Docker observation and approximately
120-second `jcmd Thread.print` / `GC.heap_info`. There were **116 process/system samples**,
**15 valid Surefire thread dumps** and **15 valid heap snapshots**, including live captures
well before completion. Additional captures were armed at 1,980/2,040/2,070/2,090 seconds;
none was needed because the gate exited before the first threshold. No watchdog termination occurred.

Maven JVM **17972**, Surefire JVM **23864**, launcher/root **30876** and wrapper identities,
creation times, descendant CPU/memory/I/O and Docker/image/port identities are recorded.
The observer also identified the Surefire `cmd.exe` launcher **21436** as a candidate;
failed attachments to that non-JVM are retained and excluded from valid dump counts.

| Sampled resource observation | Actual value |
| --- | ---: |
| Maximum simultaneous gate-owned working set | 3,236,352,000 bytes (3.014 GiB) |
| Maximum simultaneous gate-owned private memory | 3,740,360,704 bytes (3.483 GiB) |
| Minimum host free physical memory | 268,680 KiB (262.383 MiB) |
| Maximum sampled host CPU | 100% |
| Minimum sampled workspace-volume free disk | 110,713,696,256 bytes |
| Maximum sampled Surefire heap used | 894,609 KiB |
| Maximum sampled main-PG client sessions, excluding observer | 21 |

These are sampled values, not guaranteed peaks, a transient-temp-disk benchmark, or proof of
general future headroom. Total visible host RAM was 16,711,028 KiB. Host memory became tight;
the evidence does not establish that this caused any historical failure. Docker/WSL memory is
not included in the Windows gate-owned process sum; system headroom and Docker stats are separate.
The observer itself is outside that owned-tree sum. Observation work overlapped test execution:
218.672 seconds cumulative observer work-wall and 53.469 seconds controller lifetime CPU
(including preparation) are **not** an estimate of added gate runtime. Child diagnostic costs,
sampling gaps and brief attach/safepoint effects prevent an observer-free performance claim.

The main isolated PostgreSQL container used **localhost:57350 / personal_private_vault**,
`max_connections=100`, reserved=0, superuser-reserved=3. Maximum sampled sessions including
background processes were **29**; maximum client sessions were **21**, excluding the observer's
short read-only connection. Two blocking samples were captured: advisory lock **213 -> 295**
at elapsed 271.328 seconds, and transaction-ID lock **2697 -> 2823** at 1,809.656 seconds.
Their query ages were approximately 1.19 and 1.40 seconds. Subsequent progress and full success
do not support a persistent database deadlock. No Java monitor deadlock was reported in valid dumps.

**Pool waits were observed.** Background `pool-6-thread-1` stacks show Hikari borrow waits in
`RagRecoveryService.reconcile` / `RagRuntimeSecurity.isIsolatedRole`. Original logs report
HikariPool-2 connection timeouts with total/active/idle/waiting all zero against the separate,
already unavailable **localhost:57377 / ppv_clean_startup** fixture. They are preserved, not
suppressed or relabeled as absent. The main test database is a different port/database.
No internal Hikari JMX counters were enabled; numerical per-pool state is limited to original logs.
The warnings coexist with this successful full gate and do not establish the earlier timeout's cause.

The short-lived clean-startup PostgreSQL container was missed by 60-second Docker discovery;
its full ID, image and port are recoverable from raw stdout. Main PG, Ryuk and MinIO were
externally inspected. No live PG snapshots of the short-lived clean-startup DB are claimed.

Main-thread captures show real bounded work: lease-expiry Awaitility polling at the unchanged
65-second bound, worker HTTP admission, trusted replay polling, PostgreSQL operations and recovery
probe waits. **227 native binding files** retain method/job/attempt identities and producer/verifier
creation identities, complementing workers missed between process samples. ACTIVE/RECOVERY_REQUIRED
and unclosed-staging values in those point-in-time failure/recovery-test records remain unchanged;
they are not relabeled COMPLETED or confused with post-run live-process observations.

Post-run checks found **no sampled gate-tree survivors**, **no matching recorded native creation
identity surviving**, and **no new test container remaining**. Two native-PID candidates were
unrelated Windows processes with different creation times; reconciliation records the PID reuse.
No unrelated process/container was killed. Unrecorded descendants and arbitrary external resources
are outside these checks; this is not a universal forensic cleanup certificate.

## Source, artifacts and historical preservation

HEAD **ebe125f7529124b62761e8d01c692cbbd28e4aa7**, raw index and semantic index are unchanged.
All **1,732** candidate-source manifest entries match before/after execution; **2,518** existing
repository files matched immediately after verification, before the prospective governance notices.
All **83,049** historical assets match. The fixture remains
`9291ce7b3b0c4ae753873cbd3108ebf85b802cef554317f393953f9cbb97bb47`;
the frozen query test remains unchanged. No runner, JUnit limit or dependency/configuration change.

| Artifact | SHA-256 |
| --- | --- |
| Final repackaged JAR | `ad6a380e7d313b7429ff4a69ca60d182a2a664cccb35528848bfc7a8c8cd3bde` |
| Existing model.onnx | `98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f` |
| Existing tokenizer.json | `2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8` |

Exact argv/cwd/environment, source/index fingerprints, historical/entry/final manifests,
all case results, chronology, raw native/PG/process/thread/heap observations and artifact hashes:
[machine evidence](slice-4-java-instrumented-full-gate-evidence-2026-10-25.json).
No historical failure, review, original raw capture or accepted baseline was rewritten.

## Disposition and stop

**JAVA_FULL_GATE_VERIFIED** applies to this complete, genuine Java command only. The existing
2,100-second budget demonstrably permits this candidate to complete; no budget change is needed
on this evidence. The prior timeout's underlying slowdown remains **UNKNOWN / INCONCLUSIVE**;
this success does not reconstruct its missing live state or retrospectively pass it.

Narrowest next action: independent review of this verification evidence alongside the existing
Slice 4 corrections and remaining GPU blocker. No new correction or further full-gate run is
proposed or automatically authorized by this checkpoint. No speculative source/context fix.

Remaining P18-FINAL obligations are separate from this Java result:

- **S4-F06 / GPU_EVIDENCE_BLOCKED:** genuine CUDA inference, same-model GPU OOM-to-CPU
  fallback and attributable GPU/VRAM evidence remain unavailable and unwaived.
- The existing corrective handback's successful Ruff, formatting, mypy and **238-test Python**
  receipts are preserved; those commands were not rerun here. Aggregate their source-bound
  evidence with this new Java receipt and the retained focused quality/resource/lifecycle evidence
  during independent review. Java success alone is not a new verification of those other gates.
- Independent Slice 4/P18-FINAL adjudication and subsequent explicit Owner disposition remain
  required. This checkpoint closes no finding, slice or phase and grants no commit readiness.

On the Owner's resume instruction, the existing successful receipt and stopped observer were
recovered. Only evidence reconciliation and governance documentation were finished; the attempt
counter remained **1**, with **0** additional executions on resume.

Phase 17 FROZEN; Slices 1-3/FR18-3 CLOSED within D24 `.md` scope; D22 originals NOT_RECOVERED;
external indexing INACTIVE; live originals DISABLED/DEFERRED. **GPU_EVIDENCE_BLOCKED unchanged**;
Slice 4/P18-FINAL **NOT ACCEPTED**. No CUDA/model change, CPU waiver, staging, commit, push,
tag, PR, publishing or activation. Stop after verification and this handback.
