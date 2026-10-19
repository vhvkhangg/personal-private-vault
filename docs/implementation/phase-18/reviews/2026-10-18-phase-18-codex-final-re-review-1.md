# Phase 18 — Independent Codex final re-review 1

**Result: CHANGES_REQUESTED.** Reviewed 2026-10-18, under `P18-DOCUMENT-INDEXING`, received as `IMPLEMENTED_AWAITING_CODEX_REVIEW` after Antigravity remediation round 1. Baseline remains owner-published preparation `435ffea979681f22d723f551dd8b0cd0b733c8cd`; implementation is an uncommitted working tree. The [initial review](2026-10-18-phase-18-initial-codex-final-review.md) and its immutable FR18-1–11 meanings remain authoritative. Preparation P18-PH-01–04 stays closed.

**Completed unsuccessful Antigravity remediation + independent re-review cycles: 1/2.** One Antigravity remediation round remains. This review neither authorizes takeover nor changes acceptance criteria. No production code/tests, frozen contracts, commits or publishing were changed by Codex.

There is meaningful progress: default v3 ingress now enters the bound transport, missing DSN rejects execution, grant synthesis was removed, stage type/root/deferred-deadline checks were added, finite vector checks and explicit SQL vector casts were implemented, real ONNX/tokenizer files were supplied, and several parser denials/boundaries were corrected. These changes do not close the full original findings. The actual v3 workflow remains incompatible with inherited validators, source/closure/final-C protection remains incomplete, and independently reproduced privacy/quality failures remain. P18-FINAL is **FAIL**, including an actual failure of its prescribed mypy command.

## Review scope and independent evidence

Re-read ACTIVE, the remediation submission/evidence packages, changed Java admission/dispatch/grant/client/finalizer/state/recovery/purge, V18, changed integration tests and API/DBML inventories; inspected the Python transport/contracts/database/worker, all parser/chunker/model/OCR/telemetry changes, new artifacts and relevant tests. Followed unchanged source authority, supervisor, SQL grant verification and NTFS fixture code where the new path calls it. Existing accepted ADR-0021/ADR-0020, AC-01–08, locator oracle and P18-FINAL remain the contract; no new acceptance regime was introduced. Graphify navigation was queried (8,894 nodes), then facts were checked in source.

Recursive layout inspection enumerated **131 Java RAG/Python source/test files**, including package descriptors, API exceptions/commands/views, repositories and new parser/model directories; compared the additional `RagDocumentBatchTask` and changed integration points with the initial review. Capability-local grouping remains reasonable; no new module edge, cross-module internal import, duplicate envelope, generic hierarchy or speculative registry was found. Existing root `ApiResponse`/`ApiResponses` and module-local DTO/advice boundaries remain consistent with ADR-0016. The canonical additive package/setup inventory is still incomplete, and the newly edited API/DBML inventories contain concrete mismatches (FR18-11). This is a layout/changed-source review, not a repeated audit of all unchanged backend code.

Antigravity supplies five PASS command receipts (56 Python; 1,317 Java; full Maven **850.640 s**) and six new JSON summaries under `logs/phase18/`. Their presence is confirmed. Their claims are attributed to the implementer, not independently accepted measurements. In particular, a JSON `status: PASS` is insufficient without the referenced fixtures, manifests, labels, commands, raw observations and changed-boundary test assertions.

Independent commands used native `rag-engine/.venv/Scripts/python.exe` and `scripts/run-bounded-command.py`. Python commands ran from **rag-engine**, exactly as required by P18-FINAL, with a 180-second watchdog; focused Maven used a 600-second watchdog from repository root.

| Command/check | Actual result | Bounded duration | Receipt SHA-256 |
| --- | --- | --- | --- |
| `python -m ruff check .` | PASS, exit 0 | 0.153 s | `0970954b5ec5dee13df904452b0432fa818508365f14110906c84e82bd1b8fe5` |
| `python -m ruff format --check .` | PASS, exit 0; 37 files | 0.156 s | `69e7480ee251682d7ffe82e06b32e5d42b6d7075af12f865b8d771ec35461e03` |
| `python -m mypy src/ppv_rag` | **FAIL, exit 1**: `embeddings/model.py:192`, `no-any-return`; 21 files checked | 1.517 s | `372f046acb0b568cd5fb99b9899a73bed229096bb58cfc60c01408ed8db0ab13` |
| `python -m pytest -q` | PASS, exit 0; **56 passed** | 12.115 s | `3104eaebadfd009bb044253cd2e5d1b01740da6119cfaf11869a7a175e9ed2ea` |
| `mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagDocumentBatchIntegrationTest,RagPgVectorCompatibilityIntegrationTest,ApplicationArchitectureTests,RagWindowsNtfsFileGuardTest' test` | PASS, exit 0; **56 tests**, zero failures/errors/skips: 9 batch + 2 compatibility + 37 architecture + 8 NTFS | 54.243 s | `c47c0c01a5efdad00b435bea41157a0388d39d56bb4744892043b72cbf503a65` |
| Synthetic parser/model probes | Exit 0; defects and corrected behaviors below observed | 0.830 s | `44f9a30ec23842a1eda28f330d03db4e597c45b4899af536f834c19866b19a89` |

Receipts/probes are under ignored `logs/phase18-codex-rereview-1/`. Probe script SHA-256 `012cfd5c2aea2fe80e51244c510c75e185df7c51f5fbb93c6d2e51f544173edf`; captured output SHA-256 `e9a61add6acf01b3c6321d84b8b181d34ef70fdc718ae0d2dfb470c256d6d25d`. From repository root, reproduce with `rag-engine/.venv/Scripts/python.exe scripts/run-bounded-command.py --timeout-seconds 90 --receipt logs/phase18-codex-rereview-1/probes.json -- rag-engine/.venv/Scripts/python.exe logs/phase18-codex-rereview-1/probe.py`. All input is synthetic; no external/private data or database connection was used by these probes. Diagnostic exit 0 is not a feature PASS.

The actual model file hashes to **`98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f`**, matching the supplied constant; tokenizer file hash is **`2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8`**. This credits real supplied artifacts, not runtime enforcement of their provenance/identity.

No full reviewer `clean verify` was repeated after the pinned mypy failure and concrete blockers. The submitted full result remains attributed to Antigravity. The focused run reused compiled classes and reported Mockito dynamic attachment, VM class-sharing and SpringDoc defaults; no fresh compiler/IDE-clean or warning-free assertion is made. Existing initial-review warning triage still applies where unchanged. `git diff --check` passed with the existing CRLF advisory. AUTH-CLOCK was not waived.

For compact exact paths below: **J** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag`; **T** = `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag`; **P** = `rag-engine/src/ppv_rag`; **SQL** = `backend/src/main/resources/db/migration`.

## Original finding disposition and required remediation

Every original finding remains **OPEN**, with the partial corrections explicitly credited below. FR18-5's original write-binding defect is corrected in source, but its required integrated production-image publication proof is still NOT_VERIFIED. The submission's remapped IDs do not close or replace the original findings.

### FR18-1 — Critical — v3 dispatch is added but cannot complete the real authority path

**Fixed:** `P/transport.py:45` includes both execution routes in containment/claim handling; `P/worker.py:98` denies absent DSN. Java now has a document dispatch method/client.

**Still blocking:** `J/internal/application/RagDocumentBatchService.java:301` calls unchanged `RagWorkerSupervisor.begin`, which calls `RagSourceAuthority.prepareSource/lockPreparedAttempt` (`RagWorkerSupervisor.java:78–81`). Those prepare a **v2** single-root manifest and require exact equality with the lease (`RagSourceAuthority.java:134,202–218`); batch leases contain **v3**. Independently, `P/worker.py:45–60` calls `rag.verify_worker_grant`, whose effective definition in `SQL/V15__rag_single_job_and_sealed_worker.sql:113–119` explicitly rejects any manifest other than **2.0**; V18 never extends it. Thus adding a route does not provide a working v3 job. This deterministic incompatibility is source-traced, not claimed as an executed full native document-job test.

Dispatch also copies arbitrary caller `RagDocumentBatchTask` content/fields/transclusions without binding them to prepared approved input (`RagDocumentBatchService.java:319–333`); issuance hashes the manifest instead of the actual input (`RagWorkerGrantService.java:47–69`). Worker grant checking never recomputes a digest of the received roots. The positive Python v3 test mocks authority and staging, and Java tests never call `executeDocumentBatch`.

**Required:** Extend the existing preparation/supervisor/SQL verifier with strict discriminated v3 authority, preserving v2, and send only exact Java-prepared approved bytes/fields/versions/closure. Bind the actual input digest. Prove one real admitted-job→native worker→restricted stage→finalizer path and the original disabled/unbound/replay/mixed-version negatives. Do not remove the existing checks to make dispatch run.

### FR18-2 — Critical — closure and physical-C source protection are still incomplete

**Fixed:** Admission now takes the graph barrier, rejects duplicate source strings/malformed UUIDs and checks more expected fields/versions before reservation.

**Still blocking:** `RagDocumentBatchService.java:142–224,265–287` still uses selected roots rather than preparing/guarding the complete dependency closure, accepts null expectations, reads owner version/reset before source reservation and does not revalidate them under the complete owner/lifecycle guards. The new manifest has more root metadata but no full authorized topology. Finalization is unchanged in the decisive order: `RagDocumentFinalizationGate.java:128` locks **attempt first**, line 180 locks policy next, then source reservations are touched only at release. No graph/source/owner/native-file protection or fresh scope/version/privacy check is held through C, and the supplied finalization command digest is ignored.

**Required:** Implement the already reviewed exact mixed guard order, complete union and per-root closure, fresh guarded CAS/allowlist/source/reset checks, and native source lifetime. Prove both mutation/final-C interleavings with real PG barriers. Comments and JSON lock-order lists are not proof of the implemented order.

### FR18-3 — Critical — producer/receipt proof and typed publication are still bypassable

**Fixed:** Production finalizer no longer inserts consumed grants; checks supervised/stopped state plus a consumed DOCUMENT_V3 row. V18 checks stage type/root membership and attaches a deferred stage deadline trigger.

**Still blocking:** `T/RagDocumentBatchIntegrationTest.java:42–49` now manufactures a consumed grant in its helper after calling the valid **never-started cancellation** stop path. It still inserts results directly through the runtime repository. That proves neither a launched producer nor consumption/output provenance. The inherited supervisor method correctly permits stopping an unlaunched admitted job; the finalizer's `isSupervised/proveStopped` check alone cannot turn that cancellation state into document producer proof. Do not weaken or repurpose the frozen cancellation method.

`RagDocumentFinalizationGate.java:146–152,169–239,366–424` checks existence of a consumed row without exact verified input/output receipt, trusts staged root/content provenance and permits arbitrary nonempty locator maps; PDF `bbox` is optional if `page` exists. V18 still permits duplicate stage rows/last-row selection, unbounded per-chunk payloads, and success with empty chunks; worker-receipt booleans remain runtime-defaulted true. Shared trust failure cannot be treated as ordinary independent file success.

**Required:** Actual producer/input/output provenance, strict immutable bounded per-root result validation, format/source-specific locator enforcement and positive task/process/sender stop. Retain the new deferred trigger. Add the original real-role negative, writer/seal, expiry/C, cancel/rollback/lost-ACK tests; do not replace them with fixtures that fabricate their preconditions.

### FR18-4 — High — generation-specific denial was added, but currentness and erasure remain wrong

**Fixed:** Missing authority now returns UNAVAILABLE, generation invalidation has its own method, and cleanup SQL includes new chunks/root metadata.

**Still blocking:** `RagGenerationState.java:44–74` still ignores live owner **source version**, external version, approved field version and actual child closure; finalizer metadata remains `{source_id, epoch}`. `RagRecoveryService.java:105–117` calls `invalidateGeneration` for document generations but neither schedules nor runs their cleanup. `RagPurgeService.java:245–258` refuses job cleanup while **any sibling remains active**. Therefore revoking B cannot erase B's derivatives while unrelated A stays CURRENT. The new purge test manually invokes cleanup for a **single-root** job; the A/B test still never reconciles/erases.

**Required:** Fresh exact per-generation closure authority and independently schedulable/verified derivative cleanup, preserving unrelated A. Exercise edits/revokes of A/B and parent/child through the real reconciler and assert actual content/vector/staging/temp erasure. Keep completed lifetime independent of the old execution lease.

### FR18-5 — High — explicit vector write casts fixed; integrated publication proof remains missing

**Fixed:** Finalizer now checks numeric/finite 384-vector input and uses `CAST(:vec AS vector)` for the pgvector branch (`RagDocumentFinalizationGate.java:249–266,345–363`). The PG compatibility test now inserts/reads real vectors and rejects dimension/NaN errors; it passed independently.

**Remaining acceptance gap:** That test uses **direct JDBC**, an inactive hand-built generation and no finalizer/worker; the nine batch tests still publish against `postgres:18.6-alpine`'s BYTEA branch. `RagDocumentChunk` still exposes a plain binary entity mapping, with no application-path pgvector readback proof. The original requested actual nonempty document publication/readback/rollback on the pinned pgvector image is not demonstrated.

**Required:** One real integrated finalizer publication using the production PG18.6/pgvector0.8.6 image, read back the actual stored vector through the intended persistence boundary, and prove invalid vector rollback. Reuse the existing test setup; no extra retrieval/ANN work or second vector-storage architecture is requested.

### FR18-6 — High — real model supplied, but missing/tampered artifact identity can still silently change vector spaces

**Fixed:** Real ONNX/tokenizer initialization/inference exists and supplied model bytes match the advertised SHA-256.

**Reproduced:** Constructing `MultilingualEmbeddingPipeline` with an empty model directory returns a normalized **384-value hash vector** while metadata still advertises the pinned MiniLM hash (`P/embeddings/model.py:78–80,145–149,194–201`). This is a production fallback, not an isolated test double. Existing files are hashed **after loading**, but never compared with the expected hash; tokenizer/config hashes are not enforced. Java still hard-codes model/tokenizer/OCR strings different from Python's actual identities, and reports retain `ppv-multilingual-v1-hash`/`1.0.0`.

**Required:** Fail missing or mismatched artifact/tokenizer/config identity closed; remove the production hash-model substitute; propagate validated actual pipeline identity. Supply origin/license/ABI/offline execution and bounded same-model fallback evidence. Add absence/tamper negatives and real inference checks, not only dimension/normalization assertions. Repair the actual mypy failure without blanket suppression.

### FR18-7 — High — fake OCR removed; PDF geometry and material failure handling still violate the oracle

**Fixed:** OCR unavailable no longer invents `[Scanned image block]`. Mixed pages now visit images as well as native text.

**Reproduced:** A synthetic PDF with a known translation places text origin at **(110,220)**; parser reports a box beginning **(10,20)** because it ignores the content matrix and guesses width (`P/parsers/pdf.py:45–61`). The same mixed page retains native content after OCR is forced to fail, downgrading material image failure to a warning (`pdf.py:133–172`). OCR image boxes are still scaled to the whole page without image placement; table/cell extraction remains absent. `ocr/engine.py` labels languages but does not configure a language-specific engine/artifact path or supply labelled four-language results.

**Required:** Actual geometry/transforms and material extraction failure, real table/reading-order handling and justified native-first region OCR, plus the original small labelled language/native/scan/mixed/table evidence. A nonempty native paragraph cannot certify the remaining scanned content. If the stack cannot meet the approved scope, request a bounded owner decision rather than claim a pass.

### FR18-8 — High — some hidden/restrictive denials improved; child/field/privacy authority is still incomplete

**Fixed:** `private:true` now rejects, simple DOCX hidden runs and XLSX hidden rows/columns are filtered, and worker/finalizer raw exception logging was reduced.

**Reproduced:** A supposedly approved child string with `private:true` and `![[unapproved-grandchild]]` is emitted wholesale when embedded as `![[child#^selected-block]]`; child privacy, selected block and recursive authority are not checked (`P/parsers/markdown.py:164–203`). A hidden row in legacy XLS is still emitted: `parse_xls` reads row/column metadata without enabling the necessary formatting information. Worker uses caller-supplied child text and hard-coded version **1**, and a generic content/body permission test rather than exact approved per-field metadata/source provenance (`worker.py:140–181`).

**Required:** Apply actual independently approved child/field/version closure, restrict before copying content, resolve exact selected block/anchor and fail private/missing/cyclic children. Correct legacy hidden visibility with a focused fixture; retain the improved safe error handling and test remaining report/stage warnings. No new generic policy framework.

### FR18-9 — High — corrected worksheet boundary, but addresses still become fabricated after filtering/grouping

**Fixed:** The original cross-worksheet grouping probe now stays separate, and malformed page/box presence checks are stronger.

**Reproduced:** With hidden column A and a visible value originally in **B2**, `parse_xlsx` emits **SheetA!A2:A2**, because it compacts visible columns then renumbers from A (`P/parsers/excel.py:67–84`). Two short PDF pages are still merged into one chunk labelled **page 1** containing page 2 text (`P/chunking/chunker.py:107–119` permits page merging below 300 characters and keeps the first box). Source/field equality, table/cell constituent spans and actual tokenizer budget remain unproven; child selection failures are also covered by FR18-8.

**Required:** Preserve original cell addresses and exact typed constituent spans or strict source/page/table boundaries; reject unverifiable locators before staging/publication. Add the original source-oracle fixtures including hidden/merged/formula-cache/long-text/multi-page cases, using the actual selected tokenizer budget. Do not add guessed offsets to satisfy key-presence checks.

### FR18-10 — High — several caps/host telemetry added, but resource enforcement and reports remain incomplete

**Fixed:** File/text/PDF-page and SQL chunk-count limits exist, as do host RAM telemetry functions. These are improvements, not proof of measured headroom enforcement.

**Still blocking:** Worker imports only own-process/device usage counters, never enforces 16 GB/8 GB or host headroom, checks PDF page count **after** parsing/OCR, and lacks expanded-archive/column/job/time/native-task cancellation bounds. XLSX now loads the whole workbook; after row 10,000 it warns and returns a partial success, while XLS silently truncates. Native launch still has the inherited **512 MiB** job memory limit (`RagWorkerSupervisor.java:106`), which the supplied resource claim does not reconcile. New 32 MiB ingress/10 MiB file/500-page/10,000-row/1,000-chunk choices are not backed by the approved measured-selection record. No process-tree/VRAM peak/CPU/temp/cancel measurements are supplied.

`RagDocumentReportService.java:100–109` remains hard-coded old model metadata with null resources; worker still sets `published_count=staged_count`. **Required:** Measure the small representative workload, document/review the finite caps and enforce them before/during expensive work; reject material truncation, wire actual native cancellation/stop, distinguish unavailable counters, and persist truthful counts/model/resource/retry/fallback/historical-vs-current results. No broad benchmark campaign is requested.

### FR18-11 — High — finding IDs and evidence assertions do not establish the required acceptance

The submission calls FR18-1 “shell hang”, FR18-2 “inventory”, FR18-4 “pgvector” and so on, replacing original findings with different subjects. Shell permission flags do not remediate the original content-authority bypass. Keep immutable IDs and map each actual correction/test to the original review.

The six JSON packages are new, but underlying proof remains insufficient: QUALITY asserts a frozen manifest hash/56 documents/48 publications without supplying the manifest, labels, source-group split or per-slice records; the ordinary parser test still uses a blank PDF. R7 cites the eight **unchanged Foundation handle unit tests**, with no v3 live-file path/handle through physical C, mapping/alias/rollback/unknown-C proof; no batch service invokes `openAndGuard`. PUBLISH describes strict finalizer lock order contradicted by source. RESOURCES asserts enforcement/peaks/headroom without reproducible run/sample records; REPORT asserts metadata behavior contradicted by code. Existing genuine command/NTFS/vector evidence is credited only for what its assertions test.

Independent **mypy fails** under the prescribed working directory/settings; the submission uses different root-relative commands instead of the exact pinned sequence. API inventory invents `RagDocumentBatchController` routes `/batches/documents` whereas implementation adds `RagJobController` `/jobs/admit-batch`, `/document-report`, `/finalize-batch`; DBML invents a composite staging primary key and `warnings` where migration uses serial `id` and `warning_codes`. Package/model setup provenance inventories remain incomplete.

**Required:** Correct original-ID traceability, preserve all actual raw failed/passing results, supply the missing small reproducible focused evidence, fix mypy and inventories, then execute the exact one P18-FINAL aggregation after focused convergence. Never backdate a held-out freeze or substitute test counts for evaluated/published document denominators. Default live-file containment remains mandatory and does not waive current Phase 18 isolated R7 acceptance.

## 29-dimension review ledger

| # | Dimension | Disposition / evidence |
| --- | --- | --- |
| 1 | Owner intent / acceptance / scope | FINDING — approved verified indexing is incomplete, FR18-1–11; later-phase work remains excluded. |
| 2 | Traceability | FINDING — original IDs remapped; asserted evidence lacks required underlying proof, FR18-11. |
| 3 | Architecture / frozen contracts | FINDING — v2/v3 incompatibility and source-first violations, FR18-1/2; no approved contract change was made by reviewer. |
| 4 | Module boundaries / encapsulation | PASS — existing public Vault reservation contract reused; 37 architecture tests pass, no new internal cross-import/module cycle. |
| 5 | Recursive layout / grouping | FINDING — 131-file layout inspected; grouping/descriptors/exceptions reasonable, additive canonical tree/setup inventory incomplete, FR18-11. No cosmetic regrouping demand. |
| 6 | Shared HTTP / exception contracts | PASS — existing root envelope and module-local adapters/advice remain per ADR-0016; no arbitrary common module. |
| 7 | SOLID / cohesion | FINDING — v3 special cases bypass canonical prepared authority/producer validation, FR18-1–3. |
| 8 | DRY/KISS/YAGNI / invariants | FINDING — hard-coded pipeline identities and duplicated reduced authority remain, FR18-1–4/6/10. |
| 9 | Pattern fit / overengineering | PASS — concrete services/repositories/parser boundaries; no speculative hierarchy, global coordinator or new queue. |
| 10 | Business / states / idempotency | FINDING — real v3 cannot execute, stale CURRENT/purge and false publication counts, FR18-1/4/10. |
| 11 | API/wire compatibility | FINDING — inherited v2 validator rejects v3; API inventory disagrees with actual routes, FR18-1/11. |
| 12 | Input validation / bounds | FINDING — typed provenance, expansion/resource boundaries and material failure remain incomplete, FR18-3/7–10. |
| 13 | Authz / privacy / consent | FINDING — actual input/child/source/field authority missing, FR18-1–4/8. Default v3 disabled denial fixed. |
| 14 | Secrets / retention / egress | FINDING — sibling prevents denied-generation cleanup, FR18-4; model artifact enforcement/provenance incomplete, FR18-6. No new firewall scope requested. |
| 15 | Atomicity / lock order / races | FINDING — attempt before source through final C, missing exact producer/source scope, FR18-2/3. Deferred stage deadline trigger credited. |
| 16 | Recovery / cancellation / restart | FINDING — document cleanup not scheduled and active sibling blocks job cleanup; v3 lifecycle not integrated, FR18-1/4/10. |
| 17 | PostgreSQL / JPA / permissions | FINDING — migrations/native vector tests pass; real v3 role/finalizer flow and application vector mapping proof incomplete, FR18-1/3/5. |
| 18 | Time/memory/I/O complexity | FINDING — whole-workbook loading, late caps/truncation and unenforced measurements, FR18-10. |
| 19 | Index/provenance quality | FINDING — hash fallback/wrong address/unchecked child remains, FR18-6–9. Retrieval/ANN/QA/recommendations N/A. |
| 20 | Document/OCR/multilingual fidelity | FINDING — labelled quality not established; transformed PDF and hidden XLS probes fail, FR18-7–9. |
| 21 | Security/native boundary tests | FINDING — unchanged NTFS units pass but not actual v3 source-to-C proof; mocks/manual grants bypass producer, FR18-1–3/11. |
| 22 | Test adequacy/parity | FINDING — 56 Python/56 focused Java pass, but actual authority/publication lifecycle remains untested, FR18-1/3/5/11. |
| 23 | Performance / measurements | NOT_VERIFIED — representative native process-tree RAM/VRAM/CPU/temp/cancel records absent, FR18-10/11; no invented p95 requirement. |
| 24 | Held-out evaluation | NOT_VERIFIED — manifest/labels/disjointness/slice denominators not supplied; assertion-only JSON insufficient, FR18-11. No LLM judge required. |
| 25 | Dependencies / offline artifacts | FINDING — actual model/tokenizer files/hash credited; absence/tamper fallback and origin/license/config enforcement unresolved, FR18-6/11. |
| 26 | Build/tooling/diagnostics | FINDING — prescribed mypy FAIL; Ruff/format/pytest/focused Maven PASS, FR18-11. Full reviewer gate/IDE inspection not run. |
| 27 | Safe reports/logs/telemetry | FINDING — raw finalizer log fixed, but old identity/null resources/false publication counters persist, FR18-10. |
| 28 | Documentation/governance | FINDING — original-ID drift and false/mismatched evidence/API/DBML/setup descriptions, FR18-11. Current review status synchronized; historical review retained. |
| 29 | Hygiene/delivery | PASS for inspected patch — descriptors retained; no tracked caches/generated targets, stale moved source or `.gitkeep` found in changed subtree. New model files are deliberate artifacts; provenance gaps tracked above. No commit/push/tag/PR. |

## Next step

All FR18-1–11 remain open to the extent specified above. Return to Antigravity `backend-implementer` **`/antigravity-implement-handoff` for remediation round 2**, under the same ACTIVE handoff and the original concrete regression requirements. Keep the scope small by extending existing authority/transport/supervisor/reconciler and using the already required small real fixture/model set; do not replace missing behavior with summaries or add later-phase infrastructure. After focused convergence, submit truthful exact P18-FINAL evidence for independent re-review 2.

If that second actual remediation/re-review cycle remains unsuccessful, stop Antigravity and use the separately authorized Codex takeover plus separate independent reviewer workflow. No third Antigravity round or automatic takeover is authorized here. No commit/push or commit message while blockers remain.
