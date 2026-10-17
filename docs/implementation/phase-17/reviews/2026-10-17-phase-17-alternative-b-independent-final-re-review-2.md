# Phase 17 Alternative B — independent final re-review 2, 2026-10-17

**Verdict: CHANGES_REQUESTED.** No owner commit readiness is issued.

The bounded native wait and B06/B11/B12 corrections pass their independently exercised boundaries. **The clean command still fails in the new B08 cancellation regression**, after a positive supervisor stop assertion. No new unauthorized RAG consumption, unsafe reservation release or publication was reproduced. Mandatory resource evidence also remains incomplete, and current documentation asserts component measurements that its cited receipt does not contain. The independent Authentication clock issue remains OPEN; it is neither fixed nor automatically waived. Its ownership and the minimum possible acceptance-scope decision are assessed separately below.

## 1. Authority, baseline and preservation

Reviewed under existing **P17-RAG-FOUNDATION**, ACTIVE **CHANGES_REQUESTED**, branch `main`, owner HEAD `24e6268eef1685866d919d882ecdf9f532d01961`. Applied root/backend/test/docs instructions, codex-final-review, its complete 29-dimension coverage rubric and relevant RAG, authentication, testing, persistence, Java and Modulith guidance. The owner's explicit review instruction authorizes this intake under the existing status; no implementation authority is inferred. Alternative B, E1, one permanent user/account/desktop/job, manual indexing, categorical NSFW/Private exclusion, default containment and Practical Local Offline remain the scope. No strict E2 or Windows Firewall/WFP requirement is reinstated.

Read the complete previous 507-line [independent report](2026-10-17-phase-17-alternative-b-independent-final-re-review.md), including its exact 243-path appendix. SHA-256 remains **8fbbac8e0600ef7aaccd14e64b26ff906dcc15ae6e14eabe1a888d0da18c0484**. Reused its own independent evidence and unchanged source inspection after hash comparison; Antigravity's claims are not independent acceptance. Current owner decisions, ADR-0020/0019, P17-1/2/3, B01–B20, ACTIVE, remediation, implementation, benchmark, test-evidence and inventory documents were inspected. Graphify was navigation only; source and contracts govern.

Evidence directory **E**: `logs/phase17-alternative-b-independent-re-review-2026-10-17-2/`. Initial Git status, staged/unstaged diff, untracked paths, hashes, submitted XML, current remediation source copies, per-command argv/cwd/exits/logs, review probes and final validation are retained there. Every application verification uses an actual watchdog of **at most 1,200 seconds**, with shorter probe deadlines and cleanup restricted to owned processes/containers. No clock calibration, security setting or containment change was made. No maintained failing suite was rerun for a favorable outcome.

Review-start inventory: **1,985 nonignored files**, no staged patch; **244** inherited changed/untracked paths versus HEAD. Against the accepted contract-review snapshot: **74 modified + 39 added = 113**, no deletion. This new report makes the final delivery **245 paths** and contract delta **114 (74 modified + 40 added)**; that additional review document is not an implementation change. Since the immediately previous review, exactly **12 existing files changed**, no addition/deletion:

| Category | Exact subsequent changes |
| --- | --- |
| Production | `RagWorkerSupervisor.java`; `NativeWorkerProcess.java` |
| Tests | `RagNativeWorkerProcessTest.java`; `RagWorkerBoundaryIntegrationTest.java` |
| Documents | ADR-0020; ACTIVE; benchmarks-and-acceptance; changed-files; implementation-report; owner-decisions; revocation-and-derived-data; test-evidence |

All other production, tests, V1–V17 migrations, Python, source/consent authority, finalization/IndexCurrent, Portability, frozen authentication implementation/assertions and historical reports are unchanged. The only additional nonignored reviewer file is this new report; evidence/tools are isolated and ignored. No production/test/migration/accepted-contract/ADR/ACTIVE edit, commit, push, new handoff, containment removal or later-phase work was performed by the reviewer. Ignored evidence must accompany the review package separately from any owner Git commit.

## 2. Actual clean verification

Fresh command, from `backend/`:

```powershell
mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify
```

**Exit 1, 912.908 seconds, not timed out** under the 1,200-second watchdog. Fresh XML contains **1,306 unique testcase nodes / 124 XML suites: 1,305 PASS, zero FAIL, one ERROR, zero SKIP**. The error is `RagWorkerBoundaryIntegrationTest.liveConsumerUnprovenTerminationRetainsReservationAndPreventsSealUntilQuiescence:643`: **IllegalStateException: Safe cancellation is not yet verified**. This is the second cancellation after `supervisor.end` and the successful `stopAndVerify(...).isTrue()` assertion. The test's terminal/seal/release assertions after line 643 are not executed. The unchanged Authentication boundary passes this execution; its independent known issue is not closed by that observation. Maven stops at Surefire, so successful later packaging/verify lifecycle is NOT_VERIFIED. No suite retry occurred. E/`maven-clean-verify.json`, `.log`, preserved XML and `independent-test-accounting.json` retain the complete result.

### FR17-AB-2 / FR17-8 — High: mandatory RAG gate remains failed

This failure cannot be attributed to the independent Authentication issue or exempted by an Authentication-only owner decision. The isolated B08 token observer in §3 passes, but it does not erase this full-context error. The supplied log does not retain the inner cause swallowed by `RagPurgeService.abortJob:88–92`; it converts an uncertain terminal transaction into false after `requireRecovery`, and `RagJobService:154` throws the generic message. Diagnose the actual full-context terminal outcome/concurrent actor before claiming a fixed lifecycle. Preserve the exact case, failed command and error; do not suppress its assertion or disable normal reconciliation merely to obtain green.

### FR17-AB-6 — Medium: concurrent finishes falsely deny a completed safe cancellation

A separate controlled **actual native + PostgreSQL + Spring** counterexample reproduces a concrete availability/result-consistency defect in [RagPurgeService:69](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagPurgeService.java#L69). This is two existing finish paths for **one job on one desktop**, as can occur between maintenance recovery and owner cancellation; it does not assume multiple users or hosts.

1. Admit one job, launch the real confined worker, end the dispatch scope and positively stop the process/tree.
2. Hold the existing RAG graph advisory lock with an isolated admin transaction. Start recovery-style `purge.abortJob(job,false)` and observe it blocked at the graph lock after its stop-request COMMIT.
3. Start owner `rag.cancelJob(job)` and observe it also blocked there after its own stop-request COMMIT. Both have already read the nonterminal attempt/reservation scope. No production deadline or function is replaced.
4. Release the barrier. First abort commits **ABORTED + staging_closed=true + source reservation released**, and returns true.
5. Second finish acquires the graph lock but calls `reservations.lockReserved(...)` **before** reloading/checking the terminal attempt (`RagPurgeService:69–72`). The reservation was legitimately cleared by the first finish. PostgreSQL logs **Reservation authority denied**; V14's predicate at line 56 raises check_violation. The caught failure becomes **IllegalStateException: Safe cancellation is not yet verified** for the owner, while the durable job is already safely terminal/sealed/released.

E/`cancel-race-proof.json` records both blocked actors, first success, second false denial, actual ABORTED/sealed/released state and the matching server error. Observer exit **0**, **40.054s** means **counterexample reproduced**, not application acceptance. Source/driver/log/command are retained. This interleaving is a confirmed defect with the same public symptom as the clean B08 error; because that full-run error swallowed its inner cause, this is not asserted to be its conclusively captured causal trace.

No premature release, live consumer or privacy leak is demonstrated: the first finish is genuinely safe. The defect is inconsistent cancellation outcome under an allowed local race. Minimum correction: reconcile the committed terminal outcome when another finish has legitimately released this attempt's reservation, keeping positive quiescence/writer sealing/physical COMMIT and source-before-attempt order intact. Do not turn arbitrary reservation errors into success or acquire attempt locks before earlier guards. Add the deterministic two-finish barrier case in both orders and retain the existing full-context B08 assertion.

Submitted XML before clean contains **1,306 unique cases: 1,305 PASS, one Authentication FAIL**; **152 RAG cases / 23 classes, all PASS**. It mixes 121 suites without a selector with focused Authentication, native and worker-boundary selectors. It is not a preserved single clean-run receipt. Current documentation's 1,305/151 counts precede the added B08 method. Fresh results below are counted from this command's XML, not that mixed submitted directory or overlapping historical batches. `RagFoundationBenchmark` is a separately invoked benchmark, not an omitted default `*Test` suite; its 420 operational jobs are not 420 unique JUnit methods.

### Native wait remediation — verified bounded behavior

The duration overload in [NativeWorkerProcess:338](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/NativeWorkerProcess.java#L338) validates zero-to-30-second waits, opens the recorded PID with query/synchronize rights, compares the exact creation FILETIME, waits on that same handle, returns true only for a signaled process or demonstrably absent/reused recorded identity, and closes the query handle in finally. Unknown OpenProcess errors and nonsignaled/failed waits remain false. No kill request, disconnect or timeout becomes positive death proof. The existing zero-duration overload remains an immediate observation; recovery now supplies `STOP_LIMIT` (`RagWorkerSupervisor:232–233`). Existing genuine-controller-death checks still precede recovery. No SQL transaction is held during native waiting.

An independent **real Windows/Python** observer, separate from JUnit mocks, obtained readiness from its own confined process and measured a **202.3568 ms** wait for a requested 200 ms: the live exact identity returned **false** and remained alive. A mismatched creation identity was distinguished; actual owned termination/tree proof followed; the exact stopped lookup returned true; a 31-second request was rejected. Exit **0**, **1.862s**, E/`native-wait-proof.json`, source and commands retained. The maintained final-handle-close test now requests a bounded five-second native wait after Java exit notification; its assertion is not removed or converted into assumed death.

This is a correct bounded remedy for an asynchronous termination observation. The old failure's exact Win32 error/wait branch was not captured, so calling its historical root cause conclusively diagnosed would exceed the evidence. New debug diagnostics retain PID/error/wait outcome without content or credentials. Both recovery alternatives are finite; sequential named-job recovery and fallback recorded-process waiting may together consume approximately **20 seconds**, while uncertainty retains the job slot/reservations. This is safety-preserving unavailability, not a new lease-based release path.

## 3. B06/B08/B11/B12 remediation at actual boundaries

| Case | Independently inspected/executed boundary | Assessment |
| --- | --- | --- |
| B06 | `staleListenerOrPortCollisionDeniesWorkerBindingWithZeroSourceDispatch:572 onward` now uses a responding loopback HTTP server, invokes production `jobs.executeSyntheticStage`, counts execution POSTs/body bytes, observes zero consumed claims/staging, ABORTED startup and successful later admission. | Corrects the previous begin-only/empty-socket gap. Real native startup/endpoint collision, Java client and PG lifecycle; the wrong listener is a controlled HTTP server, not an actual authorized worker. Zero bytes are observed rather than inferred from zero SQL rows. |
| B08 | `liveConsumerUnprovenTerminationRetainsReservationAndPreventsSealUntilQuiescence:614 onward` holds an actual supervisor dispatch scope, runs a real consumer, invokes cancellation, observes unproven-stop failure, nonterminal/unsealed state, then end/positive stop. | The revised boundary is meaningful, but this clean execution errors on the following cancellation at line 643. Its competing-admission assertion alone also reflects the global active slot; independent direct reservation observation below removes that ambiguity in an isolated path. No actual OS refusal-to-kill is claimed. |
| B11 | `declaredRestoreOrUncertainSourceHistorySuspendsIndexUntilRenewedConsent:521 onward` publishes CURRENT, creates unfinished work, cancels it, uses production revoke, rejects stale approval CAS, reads fresh state, renews explicit consent and manually publishes a fresh CURRENT generation. An admin reset increment remains a declared uncertainty fixture. | Corrects the prior watermark-only test. Verifies the supported cooperative operator sequence using existing capabilities, not automatic hostile-restore detection, a live backup-import product or actual machine power loss. Operational documentation needs the exact existing API names, as recorded in §6. |
| B12 | `workerTimelyLastDeferredGateAllowsLateStageCommitThenExpiredVerificationRejectsPublication:662 onward` observes real Python/PG S after the final timely default gate, stops/proves quiescence **before** P, asserts IllegalStateException containing **expired**, zero publication and no new admission before safe abort. | Corrects the wrong earlier termination-guard assertion. Default production S deadline function is not weakened. Prior independent late-S proof and maintained late-trusted-P tests complement this case under E1. |

### Independent direct B08 source-token proof

E/`ReviewReservationProbe.java` uses a separate real Spring/native/Python/PostgreSQL fixture. It verifies an actual consumer initially alive, leaves its sender/launcher dispatch scope outstanding, and calls production cancellation. The unproven cancellation returns after **10,042.2651 ms**, denies quiescence, leaves staging unsealed and leaves the **exact source `reserved_by` token equal to the attempt ID**. It then ends the outstanding scope, proves native stop, calls cancellation, and observes CANCELLED + sealed + null reservation + recorded process dead. The final terminal/seal/release round trip is **115.1269 ms**. Exit **0**, **44.429s** including setup; E/`reservation-proof-v2.json` retains all assertions.

These are single fault-path observations, not a normal-stop percentile, isolated SQL lock-hold duration or throughput benchmark. The first review observer exited **1** because its cleanup incorrectly demanded another supervisor stop proof **after** known terminal COMMIT had deliberately forgotten/closed the owned session. Its source/log/receipt are preserved. The corrected observer skips that redundant cleanup only after all positive-stop/terminal/seal/reservation/process assertions succeed. No candidate test, production branch, security budget or assertion was weakened; the maintained suite was not retried.

## 4. Independent Authentication issue and owner scope decision

**AUTH-CLOCK — OPEN, independent frozen Authentication/environment issue.** The previously reproduced failures remain failures. This review does not mark the issue fixed, passed, waived or closed, including when an execution of the test passes.

The unchanged test writes `expires_at=now()` in PostgreSQL, then rotates immediately (`RefreshTokenLifecycleIntegrationTest:142–146`). Unchanged `authentication/internal/application/session/SessionService:84–85` compares the stored expiry to injected Java `Clock.instant()`. These are separate Windows/WSL2 clock domains. The earlier independent positive DB offset of roughly 0.96 seconds made rotation succeed at the database-defined boundary. It was not caused by the RAG implementation, and recalibrating to a favorable offset did not establish lasting synchronization.

In this execution the unchanged boundary case passes. A fresh 20-sample observer on the same Docker/WSL2 runtime found DB minus Java-before **−0.2045 to +5.0200 ms**, DB minus Java-after **−8.7631 to −0.4791 ms**; **zero** samples had DB ahead of the entire Java request interval; six were behind the entire interval. No clock was changed by the reviewer. Exit **0**, **7.250s**; E/`clock-results.json`. This explains why this run can differ without an authentication code fix. The separate clock fixture does not instrument the exact test call, prove stable synchronization or prove the issue harmless under every future host/VM transition.

**It is technically legitimate to track this issue outside the RAG remediation scope.** Authentication code/assertions are frozen and unchanged; Phase 17 source eligibility, consent, durable reservations and RAG execution deadlines use their own PostgreSQL authority/state checks. No evidence ties this mixed-clock fixture failure to unauthorized RAG consumption or publication. Requiring an unrelated authentication rewrite inside this handoff would broaden approved scope. This does not downgrade the Authentication expiration contract or the issue's independent significance.

**The reviewer cannot silently grant an acceptance exception.** Current ACTIVE verification at lines 88–96, remediation step 4 and B20/benchmark §9 still require the clean full Java gate; no owner decision currently excludes AUTH-CLOCK. The shared review rubric permits an explicitly owner-accepted deferred verification gate, not an inferred waiver of essential security. Recognition of the issue in the current prompt is not approval of changed acceptance criteria.

Minimum owner decision, if the owner chooses independent tracking rather than resolving the environment before final Phase 17 acceptance:

> I explicitly track AUTH-CLOCK, limited to `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary` on the documented Windows/WSL2 environment with unchanged frozen Authentication hashes and independent evidence of the same cross-clock failure, as an OPEN Authentication/environment issue outside Phase 17 RAG remediation. Its implementation/assertions stay enabled and unchanged; every actual failure and command exit remain reported. This does not establish Authentication acceptance, authorize its code changes, or waive any RAG privacy/lifecycle/resource requirement. All other mandatory Phase 17 evidence must pass. Any future Phase 17 acceptance despite this exact failure alone requires this explicitly recorded narrow gate exception; a failed `mvn clean verify` must still be called failed.

Record that decision in owner-decisions and synchronize ACTIVE and the affected B20/benchmark/remediation acceptance text **only if such an exception is approved**. This would explicitly defer that Authentication verification as an exception to Phase 17's gate, not establish its security guarantee. A fully passing command needs no fabricated exception to its actual result, but does not itself close the known issue. A narrow phase-gate/debt classification does not require a new RAG architecture or relaxed Authentication semantics. Any later frozen Authentication implementation/security-contract change needs its own owner-approved scope and the repository's ADR/frozen-baseline process. This reviewer makes none of those changes. No new handoff is created.

## 5. Mandatory RAG resource evidence — FR17-AB-3 / FR17-9 remains OPEN, Medium

The **420/420 useful-job receipt remains valid historical independent evidence**, not a new execution: 400 measured + 20 warmups, four bounded fixtures, native counters for 420 workers and prior PID/FILETIME-matched resource samples. Its SHA-256 is **9ab7d82bb62cf1b46eda0fc02ec1804af3d34070e03da8d7c4c2aa42bbccd082**, identical to the cited prior independent receipt. No benchmark rerun was needed: the changed recorded-process fallback affects termination/recovery observation, not the measured normal execute/stage/stop/publication path. No new 420-job timing claim is made.

The existing total p95 targets are numerically met: small **1364.0453/1700 ms**, depth five **1416.0845/1800**, 1 MiB **1656.4676/2000**, 100 sources **2900.7667/3500**. Native CPU p95 ≤**843.75 ms** meets 1000 ms; its corrected maximum **1109.375 ms** is not a p95 violation. Peak native committed memory **56.53 MiB** meets the proposed 64 MiB measurement target against a configured 512 MiB ceiling. Ten connections/no sampled accumulating handles/zero terminal reservations remain credited within their measurement scope. Memory headroom arithmetic is not a physical-disk capacity proof; sampled working set is not exact peak.

**Concrete remaining evidence contradiction:** `benchmarks-and-acceptance.md:122–129` and `implementation-report.md:46` now assert source guard **2–15 ms**, finalization/seal hold **10–25 ms**, graceful HTTP stop **10–20 ms**, native termination **15–45 ms**, terminal release **10–30 ms**, recovery **25–60 ms** and purge **<50 ms**, attributed to fixtures/benchmark ledger. The cited receipt's complete 420 sample schema contains admission, combined cold-worker/stage/stop, native-resource-query, publication, total, CPU/I/O/query-count and boundary resource observations. It contains **none of those isolated component metrics**. No corresponding new command, structured sample set, barrier/timing method or component receipt was supplied. The benchmark source is unchanged and has no such component instrumentation. E/`resource-evidence-audit.json` retains the field inventory and actual summaries.

The normal production supervisor stop path invokes native termination and closes/waits for its transport; the legacy `cancelWorkerJob` HTTP helper has **no production caller**. Do not invent a graceful-HTTP-stop feature to satisfy an unsupported measurement label. The document simultaneously says stop-only time was not factored separately. These ranges therefore remain **NOT_VERIFIED**, not measured acceptance evidence.

One corrected table still has a factual error: `benchmarks-and-acceptance.md:115` says combined cold-stage-stop p50 **1040–1200 ms**, whereas the actual 100-source p50 is **1443.7374 ms**. Actual p50 range is **1040.0345–1443.7374 ms**; maximum **2993.9197 ms** and p95 range are now correctly attributed. Database growth 3,309,568 bytes is approximately **3.156 MiB / 3.31 MB**, not 3.3 MiB; sampled 71.7 MB working set is approximately **68.37 MiB**. These unit errors do not establish a resource-limit failure.

**Minimum bounded correction:** retain the valid 420-job profile, remove/mark unsupported ranges, and supply the already-required small-fixture component/guard/recovery/purge/storage observations with raw samples, method, command/exit and named thresholds/limits. Existing admission/publication samples may be reused as accurately justified round-trip upper bounds; do not label them isolated lock holds. Existing storage/capacity arithmetic may be reused with correct units and attribution; no new low-disk admission feature is required. The independently observed B08 fault-path durations above can be retained as fault-path data, not substituted for normal percentiles. No additional 420-job run, 16-job saturation, F-lock benchmark, physical drive fill, full-machine power-loss experiment or OS firewall proof is requested.

## 6. Documentation/governance corrections — FR17-AB-5, Low

The main corrections are credited: CPU maximum is corrected, the combined benchmark is attributed to the actual independent receipt, E1 and Practical Offline remain approved, current status remains CHANGES_REQUESTED, and a cooperative declared-restore procedure uses the existing cancel/revoke/renew lifecycle. ADR-0020's former HMAC test wording now accurately describes opaque capability verification; this factual correction does not change the approved authorization architecture.

Remaining finite corrections:

- Current inventory still says **243 delivery / 111 delta (73+38)**. Actual review-start numbers are **244 / 113 (74+39)**. The 111-path list omits the previous October 17 report and the newly changed canonical `revocation-and-derived-data.md`. The new report itself is separately added after this baseline. Freeze/name the accounting instant so later reviews are not misrepresented as implementation edits.
- Current XML/source has **1,306 Java / 152 RAG**, not 1,305/151. The top evidence table's 1,299 success is historical; the current table/ACTIVE must distinguish it and this command rather than label old counts current. Original submitted XML is mixed focused/full evidence, not a preserved monolithic lifecycle receipt.
- ACTIVE's latest independent review link still points to October 16. Link the newest report in the authorized handback while preserving all historical entries; this reviewer leaves ACTIVE untouched.
- The declared-restore runbook at `revocation-and-derived-data.md:224–228` names nonexistent `rag.getSource`, uses a nonexistent `approveSource(ApproveSourceRequest)` overload, and gives test-only `RagTestCommands.index` as an operator step. Existing public APIs are `findSource`, `approveSource(id, RagConsentCommand)` and `admitJob(id, RagIndexCommand)`; specify those or the actual exposed HTTP routes/DTOs. Keep affected RAG use suspended during cooperative administrative reconciliation and renew explicitly before use. This is clarification of the accepted operator procedure, not a request for another restore engine or hostile-restore detector.

These documentation changes alone are not proof of production bugs or an architecture blocker. Accepted contracts/owner decisions may only be amended through their authorized workflow. No owner scope exception has been inserted by this review.

## 7. B01–B20, historical guarantees and finding dispositions

PASS is limited to concrete executed or explicitly reused independent evidence; it is not an exhaustive platform claim. All maintained methods below are in the RAG test package. Unchanged SQL, Python, Portability and source-authority evidence from the previous independent report remains hash-consistent.

| Case | Actual maintained evidence / boundary | Disposition |
| --- | --- | --- |
| B01 | `nsfwAndRemovedFieldsBeforeExecutionNeverReachActualPythonWorker`; buffered worker exclusion; owner-finalizer guard paths and authoritative SQL history | PASS bounded exclusion; no NSFW/Private worker transfer reproduced. |
| B02 | Second-JVM buffered-input/controller-death and crash-after-issued-grant cases; native/PG process identities | PASS actual child-JVM cases in this clean execution; no automatic input resume. |
| B03 | `associationReassignmentBetweenDifferentOldAndNewOwnersRequiresBothUnreserved`; bulk/OLD-NEW fixture; selected/unrelated HTTP 409 | PASS bounded atomic owner mutation/reservation behavior. |
| B04 | Lost NSFW round trip; Private configuration; delayed reset/source/epoch/policy CAS; retained consent-history SQL | PASS; lost notifications/re-eligibility do not renew consent. |
| B05 | Exact-description hash, changed confirmation/reservation C, normal-edit consent/manual reindex cases | PASS; confirmation does not renew participation. |
| B06 | Concurrent starts/single use/fresh actual processes plus corrected responding stale-binding zero-POST/byte case | PASS corrected actual client/native/HTTP/PG case in this clean execution. |
| B07 | `nativePythonDeathDoesNotReleaseSourcesWhileItsOwnPostgresCommitIsPending`; attempt seal both orders/legacy role routes | PASS bounded native-death versus pending SQL physical-C distinction. |
| B08 | Nine actual native methods; new live dispatch-scope/cancel regression; direct independent exact-token observer | FAIL maintained full-context case at line 643; nine native cases and isolated exact-token safety observer pass. No unsafe release or actual OS kill refusal claimed. |
| B09 | Real deferred 53100 storage error, lost Java COMMIT acknowledgement, terminal-C wait and reservation visibility; independent concurrent-finish probe | FAIL complete acceptance because AB-6 reproduces a duplicate-finish false denial; existing storage/unknown-ACK tests pass. Not physical disk fill/power loss. |
| B10 | Duplicate/no-takeover/live controller past TTL then real controller death; atomic child receipt publication/reader observation retries | PASS all four maintained second-JVM cases in this clean execution; duplicate finish-result defect remains AB-6. |
| B11 | Ordinary restart preserves CURRENT; corrected cancel/revoke/inspect/renew/manual-publication operator sequence | PASS bounded cooperative operator sequence in this execution; exact runbook API examples still need correction. |
| B12 | Default/post-wait/observed expiry denials; corrected positive-stop late-S case; timely late trusted P and independent persistent-index life | PASS corrected late-S/expired-verification case and maintained E1 cases; strict E2 not demanded. |
| B13 | Source/child topology history, new transitive child, remove/readd, normal edit/manual reindex | PASS bounded fresh IndexCurrent/OUTDATED and consent persistence. |
| B14 | Actual restricted-role permissions, scoped single-use SQL claims, typed digest/provenance/forged-response rejection | PASS bounded actual routes; mock responses are not producer proof. |
| B15 | Cached/ambient/savepoint/rollback guard cases; source-first physical ordering and earlier-source NOWAIT; previous actual-role both-cycle SQL probes | PASS reproduced scope; unchanged migrations/source; no universal deadlock-free claim. |
| B16 | Quoted/encoded/restrictive metadata, restored external-candidate denial and containment | PASS; live external files remain disabled, no filesystem/COMMIT guarantee claimed. |
| B17 | Both cancel/P orders, previous independently CURRENT generation, historical-manifest cleanup/PENDING versus actual erasure | PASS bounded lifecycle and purge scope. |
| B18 | Reused 420 useful cold jobs/caps/native counters/sampler; corrected CPU/max attribution; new isolated component claims lack raw evidence | NOT_VERIFIED complete mandatory resource acceptance; no useful-success regression alleged. |
| B19 | Fresh actual enabled local pipeline/cancel/recovery, Python socket tests, pinned dependencies and unchanged endpoint/config audit | PASS Practical Local Offline bounded scope; removed WFP/global-egress requirements not retroactively passed. |
| B20 | Fresh clean Maven/Python/static/hooks, architecture/Flyway/Portability/roles/accounting | FAIL full Java gate: one B08 ERROR; Python/static/hooks and selected architecture/schema/Portability suites pass. |

R1/R2 remain mapped to B01/02/03/07/15; R3 to B13/17; R4 to B04/11; R5 to B07/13/16/17; R6 to B11/12 under owner-approved E1; disabled R7 to B16; R8 to B04/05/13/17. No new counterexample justifies reopening bounded FR17-1/2/3/4/6/7/11 closure. Future parser/OCR/query/ranking/count/recommendation/answer guarantees are not certified by Foundation digest fixtures.

| Finding | Current assessment, not formal closure |
| --- | --- |
| FR17-5 | Original unauthorized post-grant NSFW consumption is prevented by durable reservations; no new consumption leak reproduced. Lifecycle acceptance still has the B08 error; no formal closure/containment removal performed. |
| FR17-8 | OPEN, High: one RAG error in the actual clean gate, despite useful bounded native/lifecycle proofs. Authentication is separately identified rather than silently waived. |
| FR17-9 | OPEN, Medium: component measurement claims unsupported by cited receipt; finite required profile still incomplete. |
| FR17-10 | Practical Offline local execution/cancellation/recovery controls verified for this Foundation scope. No firewall guarantee required or claimed. Final finding handback/overall acceptance pending; not formally closed here. |

Group A source/consent/reservation and source-first ordering remain implemented/verified for the reviewed boundaries. Groups B/C native supervision and staging/E1/IndexCurrent are implemented and independently exercised, but full-context cancellation acceptance remains failed. Group D acceptance remains incomplete for that error, resource evidence and current documentation/owner-scope decision. This does not reopen Alternative B or expand to later phases.

## 8. Exact independent commands, counts and limits

Pinned Python: `rag-engine/.venv/Scripts/python.exe`. E/`review_tools.py` records argv/cwd/exit/wall time and enforces the hard process-tree deadline. Full logs/XML/probe source are retained. No tool result is inferred from a test name.

| Command / label | Actual independent result |
| --- | --- |
| Backend `mvn -ntp '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify` | Exit **1**, **912.908s**, no timeout; **1,306 unique / 1,305 PASS / zero FAIL / one RAG ERROR / zero SKIP**. |
| rag-engine `python -m pytest -q --junitxml=../logs/phase17-alternative-b-independent-re-review-2026-10-17-2/pytest.xml` | Exit 0; **37 PASS**, 0.60s pytest, one retained Starlette/httpx deprecation. |
| rag-engine `python -m ruff check .`; `python -m ruff format --check .`; `python -m mypy src/ppv_rag` | All exit 0; **19** formatted files / **8** typed source files. |
| Root `python .agents/hooks/test_repository_safety.py` | Exit 0; **13 PASS**. |
| rag-engine `python scripts/dependency_inventory.py` | Exit 0; **32** installed distributions match unchanged uv.lock. Not a new vulnerability-advisory or exhaustive bundled DLL/license audit. |
| E/`run_native_wait_probe.py` | Exit 0, **1.862s**; actual Win32 bounded live wait/exact identity/positive stop. |
| E/`clock_probe.py` | Exit 0, **7.250s**; 20 current clock intervals, owned DB removed. No synchronization change or stability claim. |
| E/`run_reservation_probe.py`, first observer | Exit **1**, **62.667s**; review cleanup incorrectly re-proved a forgotten terminal session; source/log/receipt preserved. |
| Corrected reservation observer, `reservation-probe-v2` | Exit 0, **44.429s**; actual live consumer/outstanding scope/direct retained token/unsealed state, then positive stop/atomic terminal seal/release. |
| E/`run_cancel_race_probe.py` | Exit 0, **40.054s**: **counterexample reproduced**, two finish actors, safe committed terminal state but false owner cancellation denial. Not a passing application outcome. |
| Graphify targeted query | Exit 0, **2.189s**, 30s watchdog; navigation only. |
| Benchmark | **Not rerun**; exact prior independent receipt/counters/sampler verified and reused. No fresh 420-job execution counted. |
| Documentation/hash/inventory validation | Exit 0; **33** current contract/status Markdown files, **489** local targets / **21** fragments, no missing targets/anchors. New report: **three** local targets / **two** source-line fragments, valid. **29** coverage dimensions / **20** main B rows / **244** exact review-start inventory rows, no missing/extra path. All **1,985** preexisting file hashes preserved; only this report added, no unexpected nonignored file. `git diff --check` exit 0. E/`documentation-validation.json` retains final hash/results. |

Fresh Java XML: **1,306 unique `(classname,testcase name)` nodes / 124 XML suites**, **1,305 PASS / one ERROR / zero FAIL/SKIP**. RAG subset: **152 cases / 23 classes**, **151 PASS / one ERROR**; the worker-boundary class is **22 PASS / one ERROR**. All **nine native** and **four second-JVM** tests pass. ApplicationArchitecture **37**, CollectionArchitecture **3**, KnowledgeArchitecture **3**, FlywayStartup **2**, FlywayV1SchemaManifest **2**, Portability **15**, RagDomainCommitGuard **5**, PgVectorCompatibility **2**, OpenApiRouteInventory **1** and all **nine Authentication lifecycle executions** pass in this run. These are included subsets, not additional tests. A passing Authentication execution does not close AUTH-CLOCK.

Fresh framework executions: **1,356 = 1,306 Java + 37 Python + 13 repository safety**, **1,355 PASS / one RAG ERROR**. Parameterized display names are retained in uniqueness accounting. The submitted mixed XML, overlapping Antigravity batches, reused 420 benchmark jobs and separate review observers are not added as unique current Java tests. No suite was skipped to obtain these counts. Later Maven lifecycle success remains NOT_VERIFIED because Surefire failed.

Initial review-wrapper option-order errors occurred before any application process started. A submitted-XML display helper also failed after writing its accounting because the suites lack timestamps. E/`reviewer-setup-failures.json` retains these setup/forensic failures; they are not test failures or successful commands. Corrections affect review tools only. No clock, gate assertion or candidate code was changed to obtain results.

Final process/container snapshots in E show only unrelated Java PID **26508**, outside the tracked review command trees, and existing PostgreSQL container **01f0983078d0**. No review-owned Maven/Surefire/observer/consumer process or fixture DB remains; unrelated processes/containers were not killed. No application command timed out or required forced tree cleanup. All real failures and the corrected observer's original failed cleanup receipt remain preserved.

## 9. Complete review-coverage ledger

| # | Dimension | Disposition and evidence |
| --- | --- | --- |
| 1 | Owner intent/scope | PASS — approved Alternative B/E1/manual single-desktop job retained. |
| 2 | Acceptance traceability | FINDING — B08/AB-6 cancellation outcome and B18 component evidence incomplete; Authentication separately scoped. |
| 3 | ADR/architecture consistency | PASS bounded unchanged design; opaque-capability wording is factual synchronization, not new architecture. |
| 4 | Module ownership/interfaces | PASS bounded two existing RAG internals; no dependency/interface/ownership change; fresh architecture suites recorded above. |
| 5 | Recursive package/folder hygiene | PASS current full inventory and changed subtree; no new production/test class/package or generic framework. |
| 6 | Shared contracts/exceptions | PASS unchanged ADR-0016 transport envelope and owner-local API/exception boundaries. |
| 7 | SOLID/GRASP | PASS bounded overload/adapter responsibility; supervisor still owns execution, Vault owns source authority. |
| 8 | DRY/KISS/YAGNI | PASS no F/V/DRAIN revival, second job engine, distributed coordination or unnecessary restore system. |
| 9 | Pattern fit | PASS native/client adapters and explicit attempt state address current needs; no speculative abstraction. |
| 10 | Business state/consistency | FINDING AB-6 false cancellation outcome despite safe durable terminal state; consent/CURRENT/OUTDATED behavior credited. |
| 11 | API/wire compatibility | PASS production APIs unchanged; documentation FINDING for nonexistent operator API examples in §6. |
| 12 | Input validation/bounds | PASS retained depth/node/byte/scope/type/hash and process-wait bounds; negative cases preserved. |
| 13 | Authentication/privacy/consent | FINDING separate AUTH-CLOCK OPEN; bounded RAG categorical exclusions and consent authority remain verified. |
| 14 | Secrets/retention/offline | PASS bounded Practical Offline, source-free native diagnostics/receipts and safe cleanup; no removed packet-isolation claim. |
| 15 | Transactions/locking | FINDING AB-6 validates an already-cleared reservation before terminal reload; unchanged source-first/NOWAIT/SQL-writer/E1 ordering otherwise preserved. |
| 16 | Failure/recovery/operations | FINDING B08 full-context cancellation error despite isolated positive proof; actual power loss/general hostile restore not claimed. |
| 17 | Database/Flyway/permissions | PASS unchanged V1–V17 and restricted definer/worker role; fresh maintained suites and prior actual-role probes attributed. |
| 18 | Resource/time/memory/I/O | FINDING unsupported component ranges; valid useful-job/cap/native resource evidence retained. |
| 19 | Retrieval/recommendation quality | N/A — later-phase product absent; no Foundation digest equivalence claim. |
| 20 | Parser/OCR/multilingual quality | N/A — deferred ingestion and disabled live-file capability. |
| 21 | Adversarial security verification | FINDING B08 mandatory case errors; other native/SQL/remediation proofs credited; not universal OS certification. |
| 22 | Test quality/determinism | FINDING full-context RAG error and independently open mixed-clock issue; lifecycle assertions materially improved. |
| 23 | Performance methodology | FINDING sample attribution corrected partly; isolated component ranges not in ledger and p50/unit errors remain. |
| 24 | Held-out/model evaluation science | N/A — later parser/model/query evaluation, not current Foundation output checking. |
| 25 | Dependencies/licenses | PASS pinned-inventory scope; unchanged dependencies and prior bounded audit, no new exhaustive advisory/DLL claim. |
| 26 | Build/static/tooling | FINDING clean Maven exit 1 with one RAG ERROR; Python/static/hooks pass; warnings not called warning-free. |
| 27 | Logging/observability | PASS bounded safe new Win32 debug outcome and preserved real failures; exact historic native failure cause not fabricated. |
| 28 | Documentation/governance | FINDING stale counts/latest link/nonexistent operator examples; no acceptance exception approved by reviewer. |
| 29 | Delivery/frozen preservation | PASS review-start hashes preserved; new report/evidence only, no frozen Authentication or historical review edits. |

## 10. Minimum next owner action

1. Return the **B08 full-context cancellation failure and reproduced AB-6 concurrent-finish defect**, resource/evidence and documentation corrections to Antigravity under the existing handoff. Correct terminal reconciliation with the existing lock order and prove both finish orders, while preserving safety assertions; capture the original full-context failure's inner outcome too. Do not treat these RAG errors as an Authentication exception. Supply the missing small-fixture measurements or explicitly obtain an owner-approved amendment of that acceptance requirement; do not report nonexistent measurements as measured. Reuse the 420-job evidence and credited native/lifecycle proofs.
2. Decide separately whether AUTH-CLOCK stays a prerequisite environment investigation or is explicitly tracked outside Phase 17 under the narrow wording in §4. Do not change frozen Authentication code/assertions in this RAG handoff; no automatic waiver or lucky-clock acceptance.
3. After a documented B08 correction, run the required clean gate and request independent acceptance with exact evidence/status and any explicit owner scope decision. This failed command cannot be presented as a passing clean receipt. Reuse other valid unchanged runtime/benchmark evidence; do not rerun the whole suite merely for later documentation edits. Keep CHANGES_REQUESTED, default containment and historical failures until then. No new RAG architecture, Windows firewall requirement, new handoff, commit/push or Phase 18–22 work is authorized.

No commit message is supplied while these acceptance blockers remain. This report makes no formal finding closure, phase closeout or production-deployment assertion.

## Appendix — exact review-start changed-file inventory

All **244** inherited changed/untracked paths versus owner HEAD, sorted below. Full SHA-256 values are in E/`delivery-inventory.json` (file SHA-256 `6d98c7113222f166fb069696fb0ee2e64e3a308293b1d2c7f59999a444282936`). `M`/`A` means modified/added versus the accepted contract-review snapshot; `I` means inherited and unchanged versus that snapshot. `R` marks a subsequent remediation change versus the immediately previous independent review. These are attribution labels, not Git staging or test statuses. The new report itself is the sole additional nonignored review delivery path.

| Path | Contract-baseline attribution | Subsequent remediation |
| --- | --- | --- |
| `.codex/config.toml` | I |  |
| `.gitignore` | I |  |
| `AGENTS.md` | M |  |
| `backend/AGENTS.md` | M |  |
| `backend/compose.rag.dev.yml` | I |  |
| `backend/dev/rag-db/ProvisionRagDevelopment.java` | I |  |
| `backend/dev/rag-db/README.md` | M |  |
| `backend/dev/rag-db/provision.ps1` | I |  |
| `backend/dev/rag-postgres/Dockerfile` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/ApiExceptionHandler.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/OpenApiConfiguration.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionSourceService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionSourceValues.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/infrastructure/persistence/FictionStoryArchetypeRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/infrastructure/persistence/FictionWorldSettingRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/FictionSourceOperations.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/FictionSourceProjection.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/source/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmSourceService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmSourceValues.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmGenreAssignmentRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmStoryArchetypeRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/infrastructure/persistence/FilmWorldSettingRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/FilmSourceOperations.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/FilmSourceProjection.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/source/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagConsentCommand.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagFinalizationResult.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagIndexCommand.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobAdmissionResult.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobBusyException.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagJobStatusView.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagOperations.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/RagSourceChangedException.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/api/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDependencyGraph.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagFinalizationGate.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagGenerationState.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagJobService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagLimits.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagManifestScope.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagPurgeService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRecoveryService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeIdentity.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagRuntimeSecurity.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceAuthority.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerGrantService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagWorkerSupervisor.java` | A | R |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagDependency.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagFieldAllowlist.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagIndexGeneration.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagJobLease.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagRun.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagSource.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagStagedOutput.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagWorkerGrant.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/NativeWorkerProcess.java` | A | R |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/RagEngineClient.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagDependencyRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagIndexGenerationRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagJobLeaseRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagRunRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagSourceRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/RagStagedOutputRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/persistence/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/WindowsNativeFileGuard.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/windows/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/advice/RagExceptionAdvice.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/advice/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagJobController.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagSourceController.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/AdmitJobRequest.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/ApproveSourceRequest.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/FinalizeGenerationRequest.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagFinalizationResponse.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagJobAdmissionResponse.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagJobStatusResponse.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RagSourceResponse.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RegisterDomainSourceRequest.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/RegisterFileSourceRequest.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/dto/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/mapper/RagWebMapper.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/mapper/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagAccessState.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagClassification.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagSourceType.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/RagSourceView.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/source/package-info.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceEditGuard.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceIndexReservations.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceVersion.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/SourceVersions.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/VaultEntryOperations.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/VaultSourceGuard.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryMutationGuard.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java` | M |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultSourceReservations.java` | A |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/infrastructure/persistence/VaultEntryRepository.java` | I |  |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/package-info.java` | M |  |
| `backend/src/main/resources/application-rag-runtime.yml` | I |  |
| `backend/src/main/resources/application.yml` | I |  |
| `backend/src/main/resources/db/migration/V10__rag_complete_worker_authority.sql` | I |  |
| `backend/src/main/resources/db/migration/V11__rag_disabled_source_tombstones.sql` | I |  |
| `backend/src/main/resources/db/migration/V12__rag_exact_worker_dependency_proof.sql` | I |  |
| `backend/src/main/resources/db/migration/V13__rag_effective_database_privileges.sql` | I |  |
| `backend/src/main/resources/db/migration/V14__rag_source_state_and_reservations.sql` | A |  |
| `backend/src/main/resources/db/migration/V15__rag_single_job_and_sealed_worker.sql` | A |  |
| `backend/src/main/resources/db/migration/V16__rag_current_read_and_terminal_scrub.sql` | A |  |
| `backend/src/main/resources/db/migration/V17__rag_retained_consent_history.sql` | A |  |
| `backend/src/main/resources/db/migration/V3__rag_foundation.sql` | I |  |
| `backend/src/main/resources/db/migration/V4__rag_worker_security.sql` | I |  |
| `backend/src/main/resources/db/migration/V5__rag_staging_definer_and_indexes.sql` | I |  |
| `backend/src/main/resources/db/migration/V6__rag_security_hardening.sql` | I |  |
| `backend/src/main/resources/db/migration/V7__rag_issued_worker_grants.sql` | I |  |
| `backend/src/main/resources/db/migration/V8__rag_commit_deadlines_and_consent_instances.sql` | I |  |
| `backend/src/main/resources/db/migration/V9__rag_runtime_authority.sql` | I |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/ApplicationArchitectureTests.java` | I |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/FlywayStartupIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/migration/FlywayV1SchemaManifestIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/portability/PortabilityIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractEnabledRagIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/AbstractRagIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagAuthorityContractTest.java` | I |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagCancellationRecoveryIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagCommitAcknowledgementRecoveryIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagConsumptionContainmentTest.java` | I |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagDomainCommitGuardIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagE1ExpiryIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagEngineClientTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFinalizerOwnerConcurrencyIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagFoundationBenchmark.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLegacyRuntimeDeniedIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagLifecycleIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagNativeWorkerProcessTest.java` | A | R |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPgVectorCompatibilityIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagPythonCommitTerminationIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRecoveryProbeProcess.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagRuntimeRoleIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSecondJvmRecoveryIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSingleJobRuntimeIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceAuthorityIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSourceReservationsIntegrationTest.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagStagingCommitFenceIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagTestCommands.java` | A |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWebIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWindowsNtfsFileGuardTest.java` | I |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerBoundaryIntegrationTest.java` | M | R |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerFixture.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagWorkerPostgresPrivilegeIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java` | M |  |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` | I |  |
| `backend/src/test/resources/rag/retained_consent_history.sql` | A |  |
| `backend/src/test/resources/rag/single_job_staging.sql` | A |  |
| `backend/src/test/resources/rag/source_state_reservations.sql` | A |  |
| `docs/adr/0019-local-rag-before-frontend.md` | I |  |
| `docs/adr/0020-phase-17-single-job-reservations-e1.md` | M | R |
| `docs/adr/README.md` | I |  |
| `docs/architecture/README.md` | I |  |
| `docs/architecture/api-architecture.md` | I |  |
| `docs/architecture/data-architecture.md` | I |  |
| `docs/architecture/diagrams/exported/container.png` | I |  |
| `docs/architecture/diagrams/exported/container.svg` | I |  |
| `docs/architecture/diagrams/exported/module-dependencies.png` | I |  |
| `docs/architecture/diagrams/exported/module-dependencies.svg` | I |  |
| `docs/architecture/diagrams/exported/module-dependency-overview.png` | I |  |
| `docs/architecture/diagrams/exported/module-dependency-overview.svg` | I |  |
| `docs/architecture/diagrams/source/container.dot` | I |  |
| `docs/architecture/diagrams/source/module-dependencies.dot` | I |  |
| `docs/architecture/diagrams/source/module-dependencies.drawio` | I |  |
| `docs/architecture/module-boundaries.md` | I |  |
| `docs/architecture/module-dependency-matrix.md` | I |  |
| `docs/architecture/rag-architecture.md` | I |  |
| `docs/architecture/structurizr/workspace.dsl` | I |  |
| `docs/database/README.md` | I |  |
| `docs/database/phase-17-alternative-b.dbml` | I |  |
| `docs/implementation/handoffs/ACTIVE.md` | M | R |
| `docs/implementation/phase-17/README.md` | M |  |
| `docs/implementation/phase-17/alternative-b-verification-plan.md` | M |  |
| `docs/implementation/phase-17/antigravity-continuation-prompt.md` | A |  |
| `docs/implementation/phase-17/architecture-simplification-proposal.md` | I |  |
| `docs/implementation/phase-17/benchmarks-and-acceptance.md` | M | R |
| `docs/implementation/phase-17/changed-files.md` | M | R |
| `docs/implementation/phase-17/contract-synchronization.md` | I |  |
| `docs/implementation/phase-17/implementation-report.md` | M | R |
| `docs/implementation/phase-17/integration-ownership-and-permissions.md` | I |  |
| `docs/implementation/phase-17/owner-decisions.md` | M | R |
| `docs/implementation/phase-17/p17-3-consumption-fence-proposal.md` | I |  |
| `docs/implementation/phase-17/remediation-plan.md` | M |  |
| `docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review-2.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review-3.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-re-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-14-phase-17-final-codex-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-alternative-b-independent-contract-synchronization-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-architecture-simplification-independent-design-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-independent-final-codex-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-p17-3-consumption-fence-independent-design-re-review-2.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-p17-3-consumption-fence-independent-design-re-review-3.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-15-phase-17-p17-3-consumption-fence-independent-design-review.md` | I |  |
| `docs/implementation/phase-17/reviews/2026-10-16-phase-17-alternative-b-independent-final-re-review.md` | A |  |
| `docs/implementation/phase-17/reviews/2026-10-16-phase-17-alternative-b-independent-final-review.md` | A |  |
| `docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review.md` | A |  |
| `docs/implementation/phase-17/reviews/README.md` | I |  |
| `docs/implementation/phase-17/revocation-and-derived-data.md` | M | R |
| `docs/implementation/phase-17/source-eligibility-and-allowlists.md` | I |  |
| `docs/implementation/phase-17/test-evidence.md` | M | R |
| `docs/implementation/rag-phase-plan.md` | I |  |
| `docs/repository/repository-package-tree.md` | I |  |
| `docs/roadmap.md` | I |  |
| `rag-engine/.dockerignore` | I |  |
| `rag-engine/.python-version` | I |  |
| `rag-engine/Dockerfile.offline-check` | I |  |
| `rag-engine/README.md` | I |  |
| `rag-engine/pyproject.toml` | I |  |
| `rag-engine/scripts/dependency_inventory.py` | M |  |
| `rag-engine/scripts/offline_check.py` | I |  |
| `rag-engine/scripts/worker_boundary_check.py` | A |  |
| `rag-engine/src/ppv_rag/__init__.py` | I |  |
| `rag-engine/src/ppv_rag/config.py` | M |  |
| `rag-engine/src/ppv_rag/contracts.py` | I |  |
| `rag-engine/src/ppv_rag/db.py` | M |  |
| `rag-engine/src/ppv_rag/main.py` | M |  |
| `rag-engine/src/ppv_rag/security.py` | I |  |
| `rag-engine/src/ppv_rag/transport.py` | M |  |
| `rag-engine/src/ppv_rag/worker.py` | I |  |
| `rag-engine/tests/conftest.py` | A |  |
| `rag-engine/tests/test_bound_ingress.py` | A |  |
| `rag-engine/tests/test_consumption_containment.py` | I |  |
| `rag-engine/tests/test_contracts.py` | I |  |
| `rag-engine/tests/test_health.py` | I |  |
| `rag-engine/tests/test_transport.py` | M |  |
| `rag-engine/tests/test_worker.py` | M |  |
| `rag-engine/uv.lock` | I |  |
