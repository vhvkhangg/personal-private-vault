# Phase 18 Slice 4 — independent final corrective re-review

**2026-10-25 — OWNER_DECISION_REQUIRED / GPU_EVIDENCE_BLOCKED.**

The reviewed corrections close **S4-F01–F05, S4-F07 and S4-N01**. S4-F06's CPU/native measurement and attribution defects are resolved; its mandatory actual GPU inference/fallback evidence remains **NOT VERIFIED**. No remaining non-GPU source or mandatory evidence defect was independently demonstrated. The five executable P18-FINAL commands genuinely pass on the applicable candidate. **That does not accept P18-FINAL, Slice 4 or Phase 18 while the separate mandatory GPU gate remains unmet.**

No implementation, test/assertion/contract change, waiver, dependency installation, staging, commit, push, tag, PR or indexing activation occurred. This is a separate independent review of the Owner-authorized corrective candidate, explicitly requested despite ACTIVE's truthful `BLOCKED / GPU_EVIDENCE_BLOCKED` handback status. The skill's ordinary implementation-status prerequisite does not negate this explicit Owner review scope. No new corrective checkpoint is authorized.

## Authority, candidate and evidence method

HEAD remains **`ebe125f7529124b62761e8d01c692cbbd28e4aa7`**, D25 documentation following accepted Slice 3 source **`e2e00534fb35534a298e8fb15d174f18cb63b5a0`**. Reconciled ACTIVE, AGENTS, D24/D25 and retained D14/D17/D20/D22/D23, S4 executable handoff/acceptance/execution plan, original October 24 independent review, corrective handback/matrix/evidence, October 25 Java triage, fixture correction, timeout investigation and instrumented full-gate handbacks/evidence. ADR-0021–24 and retained snapshot/final acceptance rules apply as prospectively amended; no OS-level malicious-same-user proof is reinstated.

Inspected actual current diffs, source consumers, V22/V23, tests and raw receipts. Used `codex-final-review`, its 29-dimension coverage ledger, Java/Spring, SOLID/reuse/pattern, module/persistence/backend-testing, REST, Python/local-RAG, migration and bounded-execution skills. Graphify was navigation only; its stale references to deleted adapters were checked against actual files.

New exclusive evidence root: **`logs/phase18/s4-rereview-20261025/`** (`O`). Original generated reports were archived before fresh Maven testing. The [machine evidence](2026-10-25-slice-4-independent-corrective-evidence.json) records source/receipt/log hashes, exact commands, full candidate inventory, selected case rosters and evidence qualifications. `O/candidate-audit.json` retains all 142 archived full-gate XML suite/case records and the entry's **105 dirty paths / 17 deletions**. These are a review inventory, not an approved staging manifest.

Independently hashed **all 1,732 entries** in the instrumented gate's `final-source.json`: **zero drift**. The Python trees and production sources of retained successful Python/static/native resource receipts also match. Later fixture corrections are accounted for as test-source changes; old whole-tree equality is not asserted. No 32-minute gate, unchanged maximum workload or accepted D17 adjudication was rerun merely for repetition.

## Finding-by-finding disposition

| Finding | Independent disposition | Source-backed evidence |
| --- | --- | --- |
| **S4-F01 — standalone false NO_CHANGE / durable C_N** | **CLOSED** | New module-local `RagNoChangeService` prepares full source/child authority and independently CURRENT retained G, then revalidates under source-first guards. Selected revisions cannot have pending replacements. Existing nullable-job `RagRun` plus JSON stores operation/roster/controller/reservation identities and disposition; no indexing job is fabricated. Actual changed-child counterexample now rejects. Real C_N rollback, both wire-loss directions, database unavailability and new-JVM reconciliation pass. |
| **S4-F02 — caller omission / mixed false NO_CHANGE** | **CLOSED** | Explicit `noChangeRoots` is bound into the immutable admission manifest with exact retained G. Absent intent preserves full execution. `RagDocumentInput.workerPlan`, grant issuance, actual-result roster and trusted replay use that authoritative plan, not returned candidate keys. Original omitted pending A/B-only plan rejects before dispatch; reordered tasks reject. Unchanged proofs revalidate after replay and inside C_P; invalid proof publishes/promotes nothing. All-unchanged creates only a C_N operation, without indexing lease or worker. |
| **S4-F03 — HTTP fields / incomplete reports** | **CLOSED** | Existing DTO/mapper/controller transports now carry all six qualification/revision fields and operation identity. Authenticated MockMvc → production services → native/PG cases assert mixed outcomes and full CANCELLED/ABORTED/RECOVERY_REQUIRED rosters, including after sensitive scope scrub. Failed reindex reports the independently eligible retained G. Currentness is computed once per root. No new route or root envelope. |
| **S4-F04 — selected runtime / residual cleanup** | **CLOSED** | 17 deletions remain; cleanup manifest names HEAD hashes and retained consumers. Residual excluded locator adapters and obsolete OCR execution were removed. Selected Python now has 52 distributions at exact applicable locked versions, zero outside-lock packages; independent import/locator/DOMAIN checks pass. Historical evidence, required DOMAIN helper and exact inert OCR tuple remain. |
| **S4-F05 — lifecycle / misleading evidence mapping** | **CLOSED for executable non-GPU scope** | Actual tests now cover the previously absent C_N/restore/revoke/retry/roster cases; no nonexistent test is credited. Corrected prospective matrix separates row-image restore from PITR, injected I/O from real host exhaustion, and native SQL from producer-local corpus mocks. Genuine successful current Java gate replaces only the current gate outcome; prior failures/timeout remain unchanged. GPU belongs to F06, not an inferred PASS here. |
| **S4-F06 — resource evidence / GPU** | **CPU/native evidence VERIFIED; GPU remains OPEN / OWNER_DECISION_REQUIRED** | Actual owned-tree producer/verifier/JVM/PG measurements and workload/source identities replace the original unbound sample. Device-wide VRAM is explicitly distinct from process attribution; missing GPU attribution is UNKNOWN. CPU/Azure ORT has no CUDA provider, so actual CUDA inference and same-model GPU-to-CPU fallback remain NOT VERIFIED. |
| **S4-F07 — NTFS applicability / failed Java command** | **CLOSED** | Owner-authorized property condition gates only the superseded hostile-alias lifetime method. Original body/assertion is preserved; explicit opt-in failure is retained. Eight other Windows prerequisites remain active. Current full gate genuinely exits zero with exactly that one diagnostic skip. No D14 global waiver or assertion relaxation. |
| **S4-N01 — diff EOF** | **CLOSED** | Fresh `git diff --check` exits zero. No fixture whitespace normalization by reviewer. |

### Seven NO_CHANGE predicates and physical outcomes

| Predicate | Actual reviewed mechanism / proof |
| --- | --- |
| Explicit intent and exact expectations | Capture command still checks canonical path intent and exact source/reset/epoch/policy/selected/pending expectation. Batch unchanged intent is explicit, limited to its immutable roster; arbitrary omission is rejected. |
| Coherent freshly acquired original bytes | Existing bounded copy/change checks, SHA-256 and byte length compare remain in manual Update. All-unchanged batch mode qualifies already selected snapshots; it does not claim new original freshness. |
| Verified selected ORIGINAL_BYTES | Selected descriptor/integrity and captured `.md` identity are checked outside row-lock windows; no silent original reread on reindex or repair. |
| Current authority, history and exclusion | Exact current authority plus source-first guards, existing source reservations and durable nullable-job C_N row. Live foreign controller prevents ownership takeover; process creation identity resolves PID reuse. |
| Exact child topology/revision vector | Authority prepared manifest includes exact S/R/hash/length/selection/reset/epochs/edges. Guarded CAS and retained G manifest equality deny child version/selection/epoch/edge changes. |
| Independently eligible G / pipeline | Exactly one active CURRENT G with published status, legitimate completed closed-staging origin, exact dependency digest and pinned parser/chunker/tokenizer/model/vector compatibility. Changed-child OUTDATED and deliberately stale G/pipeline controls reject. |
| Guarded physical finish / unknown C_N | Existing graph/owner/external/source ordering through physical transaction; capture cancellation/deadline checked before commit. Deferred SQL failure rolls back; actual pre-COMMIT and committed-ACK cuts reconcile durable state, retaining exclusion while DB unavailable. No blind resend, new R/G/version or worker on NO_CHANGE. |

The original counterexamples are no longer accepted. Fresh native tests exercise normal APIs and actual PostgreSQL; Mockito spies only introduce controlled faults/barriers and call real behavior, rather than replacing the producer or trusted verifier with successful mocks. C_N row status `COMPLETED` is existing operational history, not a zero-publication COMPLETED indexing job: HTTP `jobId/status/leaseToken` are absent for that mode, with separate operation identity/disposition.

Mixed publication keeps the full admitted union reserved through C_P. Java validates every unchanged proof before and after trusted replay and under final guards. Actual-result seals, immutable expected output order, vector recomputation, stop proof and attempt fencing remain enforced. Same-R reindex uses stored root/child R bytes with deleted originals and publishes only a newly verified G; an eligible prior G remains on common failure. No sourceVersion advance is credited for pure reindex.

## Full S4-01–18 assessment

VERIFIED below means the stated executable requirement and source applicability, not Owner closeout. Required GPU evidence is not hidden within a CPU PASS.

| Row | Independent disposition | Decisive evidence and limits |
| --- | --- | --- |
| **S4-01** | **VERIFIED** | Equal capture records durable C_N and preserves source/R/G/version, no job/worker; all-unchanged HTTP mode likewise. Fresh corrective tests plus applicable original positive case. |
| **S4-02** | **VERIFIED** | Changed child, four child/edge CAS faults, two post-replay retained G/pipeline faults, pending omission and order negatives. Root consent/reset/pipeline/integrity controls retained in complete applicable gate. |
| **S4-03** | **VERIFIED** | Actual same-R reindex and nested children after originals removed; resource workload and complete Java XML/source match. |
| **S4-04** | **VERIFIED** | Failed reindex retains independently CURRENT prior G; physical multi-root rollback at five locations, revocation and vector/provenance denials remain in full gate. Fresh three native norm-preserving vector-forgery denials. |
| **S4-05** | **VERIFIED** | Selected bytes, exact graph/consent/reset/epochs/history/pipeline qualification; tamper/child/restart tests and fresh guarded negatives. No terminal-job or TTL shortcut. |
| **S4-06** | **VERIFIED** | Deleted originals do not reopen; authenticated report carries RAG_SNAPSHOT / SNAPSHOT_CURRENT / original UNVERIFIED and exact selected/pending R. |
| **S4-07** | **VERIFIED** | Both C_N/revoke orders, parent transitive denial/erasure, failed erasure retry and sibling retention. Erasure is bounded recovery, not claimed instantaneous or backup/WAL erasure. |
| **S4-08** | **VERIFIED bounded restore contract** | Both actual controlled DB row-image versus missing managed bytes, and restored files versus revoked DB. Old G denied; stale approval cannot adopt it; fresh explicit new S/R approval works. Not physical PITR/server restore or malicious coordinated DB+filesystem rollback proof. |
| **S4-09** | **VERIFIED** | Existing real CPU/transfer/staging/verifier cancellation and durable ordering remain in complete gate. Fresh C_N finish/precommit cancellation and new-JVM live-owner refusal/dead-owner recovery pass. Positive stop precedes unsafe release. |
| **S4-10** | **VERIFIED** | Real C_P wire cuts retained; fresh C_N actual committed/precommit wire cuts, DB unavailability and deferred physical rollback. Durable result decides one/zero commit; no callback-only substitute or blind retry. |
| **S4-11** | **VERIFIED** | Fresh authenticated SUCCESS/FAILURE/NO_CHANGE, all-unchanged operation, full cancel/abort/unknown rosters and retained G. Complete gate includes established 8-success/2-failure and all-failed cases. Counts exclude retained G. |
| **S4-12** | **VERIFIED bounded fault/cleanup scope** | Snapshot/recovery protections retained; C_N cancellation temp cleanup, controlled erasure retry, managed verifier I/O fault and process-stop checks. Injected failures are not physical host disk exhaustion; global OS/owner copies remain outside guarantee. |
| **S4-13** | **VERIFIED** | Exact 17 deletions and consumer manifest; strict `.md`, removed adapters/imports/installation groups and locator branches; 7 fresh cleanup checks plus full 238-test Python receipt. |
| **S4-14** | **VERIFIED CPU/offline runtime** | Python 3.14.4 / 52 applicable locked distributions, pins/artifacts, no downloads or unwanted formats. Actual typed MD/DOMAIN and cross-type denial preserved. GPU requirement is separately unmet below. |
| **S4-15** | **CPU/native VERIFIED; GPU NOT VERIFIED / BLOCKED** | Source-bound representative near-byte-bound, child/cold/warm/reindex/mixed/cancel/error evidence and honest OS attribution. Actual CUDA inference, fallback and attributable worker VRAM remain absent. |
| **S4-16** | **VERIFIED tested capacity scope** | Prior independent 95→12 connection mechanism, test-owned bounded Hikari settings, genuine successful aggregate and live PG observations. No production setting/test parallelism/discovery weakening. Earlier slowdown cause remains UNKNOWN, not an unresolved demonstrated current leak. |
| **S4-17** | **VERIFIED five-command execution** | Four source-applicable Python/static exits zero plus genuine current Maven clean verify exit zero within 2,100 seconds. This row does not waive S4-15 or accept overall P18-FINAL. |
| **S4-18** | **INDEPENDENT REVIEW COMPLETE; OWNER DECISION PENDING** | This report supplies independent adjudication and exact GPU alternatives. No commit readiness, activation or Slice 4/Phase 18 closure. |

AC/FCNC/IS applicability is the unchanged D24/D25 executable plan: native multilingual Markdown/provenance/locators, whole-file authority, guarded C_N/C_P, SQL, lifecycle/offline/finite resources remain mandatory. Excluded-format/OCR positive subcases are N/A_BY_D24, not PASS. Only the named historical hostile-alias lifetime diagnostic is N/A_BY_ADR0023. **AC-05/S4-15 GPU portions remain mandatory and unmet.** D17/D20 accepted quality is reused; no new human signature, independent held-out labels or fabricated adjudication is claimed.

## PostgreSQL, cleanup and architecture

**V1–V22 remain unchanged; V23 is the only forward migration.** V22 authority/result functions retain protected actual-result admission. V23 replaces `rag.seal_document_result` with existing `runs.metrics.worker_plan`/`batch_roots` support, adds no columns/tables, keeps SECURITY DEFINER with `pg_catalog` search path, exact ordered result membership and worker-attempt checks, and revokes PUBLIC/runtime execution while granting the worker function. It does not allow the worker to assign NO_CHANGE. Fresh four-role privilege tests and actual native fresh migration startup pass. Finalizer still validates real source-derived text/locators/compact references/vectors and publication eligibility in one physical C_P, with guarded source promotion and union release. FR18-3 remains CLOSED without a demonstrated regression.

The canonical no-change service centralizes the prior divergent eligibility rule, using existing authority, repository, runtime, reservation and transaction mechanisms. No generic transaction/job engine, new module/state/schema or speculative abstraction was introduced. HTTP stays module-local under the accepted root ApiResponse, with compatible constructors and optional intent fields. No cross-module internals or repositories were added. Full-gate architecture/OpenAPI/legacy tests are source-applicable.

Cleanup preserves 17 original deletions: four DOCX/Excel/PDF/OCR adapters; seven sole-purpose OCR scripts; three Python format tests; one Java OCR test and its two worker fixture files. Exact paths/HEAD hashes and retained consumers are in `cleanup-plan.json` and the independent inventory. Obsolete OCR launch is retired, not restored. The live R7 diagnostic is explicit opt-in. `parse_text` remains necessary for internal DOMAIN, not external TXT. Historical mixed fixtures/reviews/failure receipts remain immutable evidence. The exact **`rapidocr-1.4.4-en-hans-script-admission-v18.2`** identifier remains inert compatibility metadata, with no RapidOCR installation/execution.

Lock reduction remains 73→54, 19 removed, no added/upgraded retained packages. The actual install now has **52** matching distributions; the other lock entries are the virtual project and an Emscripten-only dependency, not missing Windows requirements. Fresh `uv pip check` and offline lock checks pass, with no outside-lock distribution or version mismatch. This fixes the earlier distinction between a cleaned lock and an unpruned install.

## Genuine full Java gate and fixture applicability

Reused, independently audited exact command from repository root:

```text
mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED clean verify
```

Raw root: **`logs/phase18/s4-java-instrumented-20261025-201e93c2/`**. The unchanged executable wrapper used full-suite profile and **2,100 seconds**. Its actual receipt is **PASS / exit 0 / cleanup=not_needed**, **1,937.915 seconds** (02:53:18.621–03:25:36.535 UTC), leaving **162.085 seconds**. Raw stdout says **BUILD SUCCESS** and shows clean, compilation/test lifecycle, JAR and Boot repackaging; the requested verify endpoint completed. No extra plugin step is invented. Archived repackaged JAR independently hashes **`ad6a380e7d313b7429ff4a69ca60d182a2a664cccb35528848bfc7a8c8cd3bde`**.

| Independent archived XML count | Tests | Failures | Errors | Skipped |
| --- | ---: | ---: | ---: | ---: |
| Actual testcase elements, 142 fresh XML files | **1,580** | **0** | **0** | **1** |
| Sum of suite attributes | **1,567** | **0** | **0** | **1** |

The 13 extra testcase elements are concretely explained by equal method names across nested classes: **Collection 2, Fiction 3, Knowledge 1, Location 6, Media 1**. The machine audit retains the exact classname/name collision groups, not a deduplicated execution total. The one skip is exactly the historical NTFS opt-in diagnostic, not an excluded mandatory current test. Current full-gate success does not retrospectively pass the earlier failed commands or the partial timeout, or recover the historical D22 captures.

The fixture correction's `QueryCountInvocation` matches **only** the exact frozen `RagSourceAuthorityIntegrationTest.genuinelyFreshBatchProjectionsHaveConstantQueryCountsAndEnforceByteBudget`. It holds the existing recovery bean monitor during that original invocation, calls proceed once and releases intrinsically on assertion/exception. Unrelated methods immediately bypass without recovery lookup/lock. No counter filtering, scheduling disable, broad serialization or assertion/time-budget change. Frozen query test content remains equal to accepted Phase 17 (Git LF versus current checkout line endings accounted for); current raw SHA is **`d3676b2f95041cd2343088e0ad1b0ea6244e1e70b6861d91db318bba182b6fa1`**. Fixture SHA is **`9291ce7b3b0c4ae753873cbd3108ebf85b802cef554317f393953f9cbb97bb47`**.

The 50ms diagnostic proves shared-statistics interference and a successful exact-method coordination control; it does not establish the exact SQL in the historical failed aggregate. Retained propagation/concurrency diagnostics test release and bypass; current full gate and fresh original-method execution pass. Production recovery still runs, with relevant concurrency/recovery tests active. Rollback observation coordination retains its assertions and explicitly reconciles afterward; the CPU cancellation fixture supplies distinct content to sustain observed work, retaining the original CPU threshold/CANCELLED assertions. Neither changes production cache/inference/deadlines.

The earlier timeout remains **TIMEOUT / exit 124**, completed-suite-only evidence. Focused near-bound progress and the later instrumented success do not retrospectively identify its missing live stack or slowdown cause. Current successful evidence establishes feasibility within the unchanged cap; no deadline increase or speculative context refactor is justified. Read-only observation did perturb execution; no uninstrumented timing claim is made.

## Five-command P18-FINAL and independent fresh checks

All retained Python command manifests have **zero current Python drift and zero production drift**. Java fixture/test changes explain whole-tree differences; retained Python receipts are therefore applicable without rerunning full pytest. Original 238-case JUnit XML was independently counted, not accepted from its summary alone.

| Mandatory command | Audited current result |
| --- | --- |
| Ruff check | Retained final-ruff exit 0; fresh independent check also exit 0. |
| Ruff format check | Retained final-format exit 0; fresh check exit 0, **67 files**. |
| Mypy | Retained fresh-cache final-mypy exit 0; fresh independent empty-cache check exit 0, **22 source files**. |
| Full Python pytest | Retained final-pytest-v2 exit 0, **238 actual testcase elements / 0 failures/errors/skips**. Entire current Python tree matches. Fresh cleanup selection independently **7 PASS**. |
| Full Maven clean verify | Above genuine **exit 0 / 1,580 testcase elements / 0 failures/errors / 1 explicit historical diagnostic skip / 1,937.915s**. Not rerun in this review. |

Fresh isolated native command (ordinary 720-second bound):

```text
mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=RagSlice4CorrectiveIntegrationTest,RagWorkerPostgresPrivilegeIntegrationTest,RagSourceAuthorityIntegrationTest#genuinelyFreshBatchProjectionsHaveConstantQueryCountsAndEnforceByteBudget,RagSlice3CorrectiveIntegrationTest#trustedInferenceRejectsNormPreservingNativeVectorForgery test
```

Actual **36/0/0/0**, exit 0, **317.228 seconds**: corrective **28**, privilege **4**, frozen query **1**, native norm-preserving vector-forgery **3**. Selected suite XML was counted separately; stale unselected reports in a focused Maven output are not added. Actual new native/C_N/HTTP/recovery bindings are under O. These tests exercise real selected Python/ONNX, trusted Java and PostgreSQL, not DTO-only publication.

Fresh Ruff/format/mypy, `pytest tests/test_slice4_cleanup.py -q` with isolated output paths, `uv pip check --python <selected Python>`, `uv lock --directory rag-engine --check --offline`, and `git diff --check` all exit zero. Exact absolute argv/cwd/wrappers and logs are in the machine evidence and `O/commands.jsonl`. No dependency sync/install was performed by reviewer.

Both applicable corpus results contain independently recounted **1,267 PASS + 62 required rejections = 1,329**. The isolated corpus adapter mocks grant/staging/seal storage, not parsing/CPU embeddings; it is producer-local quality proof, **not SQL security proof**. An old failed output-quoting artifact and a Java test fixture explain recorded non-production manifest differences. D17 core/gold/interpretation identity is preserved; no corpus/large-workload rerun was necessary.

## Resource and GPU assessment

The prior **580 MB / 1.80 GB** unsupported sample is not reclassified PASS. Its corrected replacement has real Java → producer → verifier → PG workload identities, exact S/R/grant/result bindings and an inspected OS observer retaining handles and `(PID, creation FILETIME)` through process lifetime. Cold/warm means two roots in one native model session, not cross-job caching. Near-bound input is **507,380 original bytes / 504,059 characters / 1,020,964 prepared bytes**, **97.3667%** of the unchanged 1 MiB admission ceiling, with vi/en/Hans/Hant, tables/callouts/code and approved child. It is not a universal million-unique-character benchmark.

| Bound observation | Independently audited interpretation |
| --- | --- |
| Corrective native window: 669.663s wall / 958.641 tree CPU seconds | Actual aggregate selected workload, not per-file latency or a universal worst case. |
| Simultaneous sampled tree RAM 2,139,361,280 bytes working set / 2,309,246,976 private | Java/orchestration + observed producer/verifier descendants. Individual lifetime peaks and their sum are not simultaneous totals. 324 identified process lifetimes: 67 producers, 55 verifiers, 202 orchestration/unclassified children. |
| Host minimum available RAM 1,682,743,296 bytes | Host-wide availability, not worker RAM. Docker/WSL and unrelated activity require separate accounting. |
| Owned temp 12,283,399; managed temp 10,896,701; revision storage 512,193 bytes | Sampled peak categories. No remaining managed capture temp or observed owned survivor. Three retained fixture files / 3,693 bytes are disclosed, not called zero global writes/storage. |
| Device VRAM peak 1,544,941,568 bytes | **Device-wide**, not attributable worker VRAM. Process-attributed GPU bytes remain **UNKNOWN**, not measured zero. |
| JVM | Original OS Java working set is not heap. Separate three real heap points are not peaks. Later instrumented full gate has 15 valid heap snapshots, maximum sampled Surefire heap used 894,609 KiB. Do not add heap and working set. |
| Successful instrumented full gate | Sampled owned working set 3,236,352,000 bytes / private 3,740,360,704; minimum host free 268,680 KiB; sampled CPU reached 100%. Tight headroom is disclosed, not proof of a leak or cause of earlier slowdown. |

The 50ms target resource sampler averaged about 73ms; maximum gap was not recorded, and short processes/peaks may be missed. Final aggregate sampling is coarser (~15 seconds) with diagnostic attachment overhead. Temp headroom was an end sample; full-gate volume headroom is separately sampled, not a temp high-water benchmark. Disk-full-equivalent faults are controlled injected I/O failures. These limits are sufficient for bounded representative CPU acceptance, not guarantees against every possible resource workload.

The instrumented full gate observed maximum **21 main-PG client sessions excluding observer**, main database max_connections=100. Two short lock-blocking samples progressed; no persistent deadlock was established. Background recovery Hikari waits/connection warnings against the **different, already unavailable clean-startup fixture database** remain in raw logs. They are not relabeled absent, nor treated as proof of main-DB pool exhaustion. Test-owned max5/min1/idle10s plus independent capacity diagnosis and actual aggregate success satisfy the required capacity correction for this tested configuration. No production pool configuration or test concurrency was weakened.

Selected runtime is Python **3.14.4**, ORT **1.31.0**, providers **AzureExecutionProvider / CPUExecutionProvider**. Actual pinned CPU model inference and offline local artifact loading are verified. No GPU provider is available despite an RTX 2070/8 GiB device. **Starting with CPU is not GPU-to-CPU fallback. Device NVML usage is not CUDA inference.** Actual same-model CUDA inference, GPU failure/OOM fallback and attributable worker VRAM remain NOT VERIFIED. Existing S4-15/AC-05/final-resource rules explicitly retain this gate; D14, D24 format narrowing and earlier CPU-only limitations do not waive it.

## Exact remaining Owner decision

**GPU applicability/fulfillment is the sole remaining mandatory Owner-controlled gate.** No new non-GPU correction is requested or authorized. Owner must explicitly choose one prospective route:

**A — fulfill the existing requirement.** Authorize a bounded compatible actual CUDA environment and its exact dependency/provider setup separately. Preserve the pinned model/tokenizer and accepted processing/authority/SQL contracts. Produce actual CUDA inference and forced GPU resource-failure → bounded same-model CPU fallback evidence, with output correctness, process attribution, finite limits, cancellation/cleanup and source/runtime hashes. Independently review the changed runtime and affected verification. No GPU installation, upgrade or trial follows automatically from this report.

**B — prospectively amend final Phase 18 acceptance to CPU-only.** Suggested decision text for Owner approval, not an adopted decision:

> Phase 18 final accepted execution scope is pinned offline CPU inference only. Actual GPU inference, GPU OOM-to-CPU fallback and process-attributed GPU VRAM remain NOT VERIFIED; those capability-specific acceptance subcases become NOT APPLICABLE UNDER THIS NEW CPU-ONLY SCOPE, never historical PASS. Preserve all CPU model/vector/provenance/SQL/security, Markdown fidelity, authority/lifecycle, deadline/resource/cancellation/cleanup and unchanged five-command verification obligations. Future GPU enablement requires separate authorization and verification. This amendment does not itself accept P18-FINAL, close Slice 4/Phase 18, activate indexing or authorize publishing.

If B is chosen, explicitly synchronize the named GPU portions of S4-15/AC-05, executable handoff/execution-resource plan, acceptance-and-evidence, final-verification-gate and applicable ADR-0021–24/D24 applicability records. Preserve original required/unavailable/failed evidence and mark supersession prospectively. Do not silently edit D24/D25 history or change the five commands. A documentation-only applicability decision does not invalidate these unchanged source-bound successful commands; any subsequent source/runtime/configuration change requires affected revalidation. Independent governance-consistency verification and explicit Owner closeout still follow. **The reviewer selects neither route and grants no waiver.**

## Coverage, hashes, preservation and delivery

Recursive inventory: **110 RAG Java production files, 50 RAG Java test files, 22 Python production files, 13 scripts, 67 Python test/fixture files** (excluding caches). Shared transport stays at the application root under ADR-0016; the new service is module-local. Exact changed paths, hashes and CRLF counts are machine-recorded; no whole-directory staging scope is inferred.

| Coverage dimension | Disposition / evidence |
| --- | --- |
| 1 Scope | PASS D24/D25 .md and bounded correction; GPU decision outstanding. |
| 2 Traceability | PASS executable corrected matrix, exact cases and independently audited gate; GPU explicitly unmet. |
| 3 Architecture/contracts | PASS existing-row nonpublication and atomic publication; no new engine/schema. |
| 4 Module ownership | PASS source inspection and applicable full-gate architecture suite. |
| 5 Recursive tree | PASS inventory/consumer review; no deleted parser assumed from stale graph. |
| 6 Shared contracts | PASS root ApiResponse and corrected module DTO/mapper. |
| 7 SOLID | PASS cohesive canonical no-change proof rather than divergent policies. |
| 8 DRY/KISS | PASS shared authority/plan/currentness rules, no speculative hierarchy. |
| 9 Patterns | PASS existing service/repository/adapter/guard reuse. |
| 10 Domain/status | PASS actual NO_CHANGE/reindex/publication/operation/outcome distinctions. |
| 11 Wire | PASS authenticated full roster and nullable nonpublication transport. |
| 12 Validation | PASS exact intent/plan, actual-result and source/vector negatives. |
| 13 Authorization | PASS fresh source-first guards, child CAS, revoke, SQL denials. |
| 14 Privacy/offline | PASS application-local pinned loading; no anti-same-user OS sandbox claim or payload logging added. |
| 15 Transactions | PASS actual C_N/C_P loss/rollback/fencing; no blind retry. |
| 16 Recovery | PASS bounded row-image restore/restart/positive-stop/cleanup; not PITR/forensic proof. |
| 17 Database | PASS V22 preserved / V23 narrow function+ACL; fresh PG privileges. |
| 18 Efficiency/resources | PASS bounded CPU scope; GPU NOT_VERIFIED. |
| 19 Index quality | PASS eligible exact source/G bindings; retrieval/reranking N/A to Phase 18. |
| 20 Document fidelity | PASS accepted unchanged D17/D20 plus source-applicable corpus/native evidence; OCR N/A_BY_D24. |
| 21 Security tests | PASS native vector/closure/plan/role/authority denials; no superseded hard-link demand. |
| 22 Tests | PASS critical actual boundaries inspected and freshly executed; no count-only acceptance. |
| 23 Benchmarks | PASS bounded representative CPU interpretation; GPU NOT_VERIFIED, no universal maximum claim. |
| 24 Adjudication | PASS D17/D20 accepted evidence reuse; no fabricated new human label/signature. |
| 25 Dependencies | PASS exact applicable lock/install/artifacts and offline checks; no reviewer installation. |
| 26 Build/static | PASS all five source-applicable commands and fresh static/diff checks; no warning-free claim. |
| 27 Observability | PASS corrected attribution, raw logs/identities, sampling gaps and warning limitations explicit. |
| 28 Governance | OWNER_DECISION_REQUIRED solely retained GPU acceptance; prospective review controls earlier dated submissions. |
| 29 Hygiene/delivery | PASS source/history/index preservation and diff; commit readiness NOT GRANTED. |

Key independent hashes (complete file/receipt inventory in machine evidence):

| Artifact | SHA-256 |
| --- | --- |
| Instrumented final-source.json | `015cc6d027b15c3b0743fcf434a9371edb60fff6e29b4304bafe12a2ef0bf478` |
| RagNoChangeService.java | `1cb0e5fcba8800522f9c0c26b2fff8dac8d6ad59688bbb68dd5b305f0e54ffa8` |
| V22 authority migration | `443802cfe7a4b6a0d41939e61b93ef7190210ff0f2a13bdb0cfb7c270ee488e6` |
| V23 worker-plan migration | `072de27b3ab22ca748100419e6d50ad1c8ca71f118fd9ce3b8e84343a95c28fe` |
| model.onnx | `98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f` |
| tokenizer.json | `2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8` |
| Cleaned uv.lock | `5d5ab94a66875c51c35840e37aec76a2efa5e193a1fa1210347691c96c3517cc` |

Entry preservation covers **2,520 nonignored files** and **83,542 existing ignored evidence assets**, with original generated reports archived before tests. Original reviews, failed/timeout captures and source/history remain unchanged; only new review records and ACTIVE's additive review disposition are written. HEAD and semantic Git index are unchanged. D22 originals remain **NOT_RECOVERED / ORIGINAL_PRESERVATION_FAILED**; fresh preservation does not recover or retrospectively pass them.

No eligible commit message or staging boundary is issued while GPU remains mandatory/unmet. After an approved route and any required independent follow-up, delivery must still use the reviewed exact inventory and Owner verification; this report performs no staging. Phase 17 COMPLETE/FROZEN; Slices 1/D14, 2/D23 and 3/D25 plus FR18-3 CLOSED; D24 `.md` only; external indexing INACTIVE; live originals DISABLED/DEFERRED; Slice 4, P18-FINAL and overall Phase 18 **NOT ACCEPTED / NOT CLOSED**. Earlier failures retain their historical FAILED/TIMEOUT status despite the new current Java PASS.

**Next action: Owner explicitly chooses A or B. Stop after independent handback; no new corrective checkpoint, CUDA work, publication or closeout is authorized.**
