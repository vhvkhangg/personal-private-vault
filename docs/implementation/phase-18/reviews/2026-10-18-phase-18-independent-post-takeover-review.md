# Phase 18 — Independent post-takeover review, 2026-10-18

**Verdict: CHANGES_REQUESTED — implementation INCOMPLETE. P18-FINAL NOT ACCEPTED. Not READY FOR OWNER COMMIT.**

The takeover made substantive, independently verifiable repairs. It did not complete the approved live-file indexing, producer verification, multilingual/table quality or resource acceptance contract. Its explicit INCOMPLETE assessment is accurate. Passing 1,321 Java and 72 Python tests does not replace those mandatory acceptance gates.

## Scope, independence and authority

Reviewed the actual uncommitted working tree on `main`, HEAD `435ffea979681f22d723f551dd8b0cd0b733c8cd`, including untracked implementation files, not merely the tracked `git diff`. Compared the [active P18-DOCUMENT-INDEXING handoff](../../handoffs/ACTIVE.md), [takeover record](../codex-takeover-2026-10-18.md), [original independent findings](2026-10-18-phase-18-initial-codex-final-review.md), [re-review 2](2026-10-18-phase-18-codex-final-re-review-2.md), implementation/test changes, migrations, raw receipts and unresolved acceptance obligations. The archived [Phase 17 handoff](../../phase-17/handoff.md) remains historical authority, not a replacement for the active Phase 18 scope.

This is a separate review of the Codex-authored takeover. This reviewer did not author its production changes and changed no production code or implementation tests. No original finding is closed in this review; verified subrepairs below must nevertheless be preserved rather than reimplemented. Two Antigravity remediation/re-review rounds remain exhausted; this review does not create a third round or an automatic further takeover loop. No commit, push, tag, PR, live-file activation or contract amendment was performed.

Applied `codex-final-review`, its 29-dimension `codex-review-coverage` rubric, relevant Java/Spring, persistence/migration, backend-testing, architecture, REST, pragmatic design/reuse, `local-rag-engineering`, `python-rag-coding`, Graphify navigation and `bounded-command-execution` instructions. Canonical requirements are [ADR-0021 clauses 1–8](../../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md), [owner decisions](../owner-decisions.md), [AC-01–08](../acceptance-and-evidence.md), the [locator/held-out oracle](../held-out-quality-and-locators.md), and [P18-FINAL](../final-verification-gate.md). Frozen ADR-0020 and V1–V17 were not amended.

## Evidence independently checked

The [review evidence manifest](2026-10-18-post-takeover-review-evidence.json) records exact receipt/artifact hashes, results and limitations. Raw reviewer artifacts are under ignored `logs/phase18-post-takeover-review/`.

| Check | Independent result and limits |
| --- | --- |
| Supplied evidence integrity | All **52** recorded receipt/artifact SHA-256 values match actual files. Current **126-file runtime snapshot has zero drift** from the takeover's final snapshot. Model and tokenizer bytes independently match the pinned hashes. |
| Reused full Java verification | Bounded runner and Maven record exit **0**, BUILD SUCCESS, 816.352 seconds, native access and full `clean verify`. Before the focused rerun, independently counted **1,321 actual XML testcase elements**, zero failures/errors/skips. Suite `tests` attributes sum to 1,308 because nested cases are undercounted by 13. Archived original XML before overwriting focused reports. Historical outer PowerShell tool exit 1 remains disclosed; it is not the Maven/watchdog exit. No new full suite was necessary against the unchanged runtime snapshot. |
| Fresh focused Java | **13/13**, zero failures/errors/skips, watchdog exit 0, 49.898 seconds: `RagDocumentBatchIntegrationTest` 10, `RagDocumentInputCompatibilityTest` 1, `RagNativeDocumentBatchIntegrationTest` 2. |
| Fresh Python | **72/72**, exit 0; pytest 20.67 seconds, bounded command 22.933 seconds. |
| Fresh static checks | Exact worker-directory Ruff check, Ruff format check and mypy commands pass: 41 formatted files, 21 typed source files. No IDE-wide or warning-free claim. |
| Fresh selected OCR diagnostic | **FAIL / exit 1**, one development Vietnamese scan, unchanged source/hash, no publication. Reference `Kệ sách có 45 quyển.` becomes `Ké sach co 45 quyen.` at confidence **0.9691**. Independently rendered and visually spot-checked the source accents. This is a regression diagnostic, not fresh held-out acceptance or complete human adjudication. |
| Existing OCR evidence | Hashed selected-runtime report retains **3/8 exact string matches**. Spacing/punctuation mismatches are not automatically material; Vietnamese lost diacritics and `Số` → `S6` are concrete quality failures. The exposed two-family corpus cannot now be relabelled a fresh held-out acceptance set. |
| Runtime | Native Windows 11, CPython **3.14.4**, ONNX Runtime **1.31.0**. Available providers are `AzureExecutionProvider`, `CPUExecutionProvider`; **no CUDA provider** is installed in this environment. A provider name alone is not evidence of egress or of native GPU inference. |
| Structure/hygiene | Recursively inventoried **175** relevant Java/Python source, test, script and metadata files; compared package descriptors and additive package-tree inventory. `git diff --check` passes; Git emits CRLF normalization notices. No production/test file was edited by this reviewer. |

Reviewer commands used the repository watchdog: normal profile, 600 seconds for focused Maven, 180 seconds for pytest, 120 seconds for the one-file OCR probe, 60 seconds for each static check. Focused Maven used `-DargLine=--enable-native-access=ALL-UNNAMED` and `-Dtest=RagDocumentBatchIntegrationTest,RagDocumentInputCompatibilityTest,RagNativeDocumentBatchIntegrationTest`. Python used the pinned venv. The OCR probe's nonzero result is deliberately retained. These are review checks, not a claim to have newly executed/accepted the complete P18-FINAL sequence.

## Repairs verified, with original-ID traceability

Paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/` (`J`) or `rag-engine/src/ppv_rag/` (`P`). Backend test paths use the corresponding `backend/src/test/java/.../rag/` subtree (`T`). This table credits evidence without changing original finding status.

| Original finding | Independently supported repair | Remaining acceptance boundary |
| --- | --- | --- |
| FR18-1 | `J/internal/application/RagDocumentInput.java`, grant service and Python canonical encoding agree on real Unicode/typed defaults. Actual Java-prepared DOMAIN projections reach the native worker; caller replacement bodies are denied. | External file/child input is not wired. Exact staged-output authority remains FR18-3. |
| FR18-2 | Batch preparation now uses actual root closures; `RagSourceAuthority.java:159–214,252–294` checks graph edges, owner versions/reset, fields, epochs and reservations using one source-first pass. Admission rejects null/stale expectations and reserves the tested complete child union. Hash/input preparation is outside row guards. | Deterministic mixed/external mutation versus final-C proof is absent. Do not infer it from DOMAIN admission or metadata keys. |
| FR18-3 | Nonempty success, duplicate root rejection, consumed input/scope grant checks, numeric DOCX and TXT line-end checks, and the real native producer positive path are improvements. | Exact output/typed source/field/version/excerpt verification and changed-boundary negative/race proof remain Critical below. |
| FR18-4 | Durable `batch_roots` distinguishes document jobs after content erasure. Per-generation `manifest_text`, targeted chunk/root/stage erasure and repeated reconciliation preserve unrelated A after B revocation; the focused test checks three reconciliation passes. | Native external/true-dependent, failed replacement and interrupted-cleanup cases remain in focused acceptance. No current repetition of the old sibling-revocation defect was found. |
| FR18-5 | Hibernate VECTOR `float[]` mapping, BOM-managed `hibernate-vector`, vector-backed application startup, real JPA publication/readback and pinned pgvector test image repair the prior BYTEA/schema mismatch. V19 rejects an old BYTEA candidate without dropping data. | **No remaining mapping defect identified in this review.** Preserve the fix; do not retain a separate BYTEA implementation or rerun unrelated baseline suites to address it. Publication rollback/security proof belongs to FR18-3. |
| FR18-6 | Exact model/tokenizer hashes, real inference, actual pipeline identity transport/persistence and controlled same-model CPU fallback code are supported by source/tests. Publisher-origin metadata and failed diagnostics are retained. | Identity is still self-reported staged data without exact producer binding; full changed-pipeline offline/native GPU/resource evidence is incomplete. |
| FR18-7 | PDFMiner/pdfplumber glyph/matrix geometry, rotated native text, positioned raster OCR and native table cells have meaningful geometry tests. Unsupported detected raster tables fail closed. | Actual Vietnamese and required scanned/mixed table capability fail/remain incomplete; four-language quality/oracle acceptance is absent. |
| FR18-8 | DOMAIN fields/versions come from fresh Java projections. Parser-level typed child versions, recursive cycle/private/missing-child rejection and exact frontmatter grants improve the former caller/default-version behavior. | Those parser tests do not establish Java-authorized external transclusion/closure through physical C. |
| FR18-9 | Separate PDF boxes/sparse sheet cells, constituent locators/metadata, exact split offsets, tokenizer-aware budgets and rejection instead of silent tokenizer truncation repair the reproduced examples. | Complete published excerpt/source/locator oracle is unverified; finalizer does not enforce it. No request to undo these repairs or add a second chunker. |
| FR18-10 | XLSX overflow now rejects rather than silently truncates. Win64 memory structure layout and unavailable-reading semantics are corrected; historical outcome and live CURRENT are distinct. | Process-tree/VRAM peaks, measured limits, temp/expanded-input bounds, native v3 operational proof and required report details remain incomplete. |
| FR18-11 | Current takeover/evidence explicitly say INCOMPLETE/NOT_ACCEPTED, retain failed probes and original IDs, disclose corpus exposure/overwritten early diagnostic, and update additive inventories. Inspected hashes/counts are consistent. | Six focused packages are not accepted. Historical Antigravity PASS claims remain attributed history, not current truth. This review synchronizes stale review/status pointers only. |

## Remaining blocking findings

### FR18-3 — Critical — exact producer and published-content verification is still absent

**Implementation defect, not an environment limitation.** `J/internal/application/RagDocumentFinalizationGate.java:131,171–181` compares an expected output hash derived only from `DOCUMENT_V3:` plus the **input** hash. Python `P/worker.py:362–383` returns an output digest of job/attempt and result **counts**. Neither binds the actual staged outcomes, chunk text, locators, vectors or identities, and Java does not verify that returned receipt against the sealed staged result.

`backend/src/main/resources/db/migration/V18__rag_document_indexing.sql:259–260` grants runtime direct staging DML; `:275–335` records `worker_receipt=true` after checking the worker session/attempt/type/root but does not attest each exact result. Java then accepts arbitrary `source_identity` (or defaults it) at `RagDocumentFinalizationGate.java:255`; `:358–433` checks self-reported pipeline strings and locator shape rather than a discriminated approved format/source/field/version/excerpt. A correct grant and 384 finite numbers do not establish those claims. Runtime-authored synthetic stage tests remain useful internal transaction tests, not producer-provenance proof.

**Consequence:** the finalizer lacks the required assurance that the immutable generation contains exactly the authorized producer's verified result for that root/closure. The new real 8/2 test proves a positive path, not rejection of forged/mutated output.

**Smallest correction:** complete the existing restricted v3 staging/seal and Java validator. Bind a canonical exact result receipt to job/attempt, admitted input/scope, actual per-root outcome/chunks/vectors/locators and pipeline identity; validate every attributed source/version/field and mandatory excerpt/locator against prepared authority. Enforce effective producer-only staging provenance and immutable sealed results. Reuse existing attempt/fence/physical transaction infrastructure; no new coordinator, signing platform or job engine is required.

**Required focused proof:** actual worker/runtime roles deny wrong/absent/unconsumed/mixed-v2 grants and runtime-forged result provenance; substituted chunk/source/field/version/format/locator/vector/receipt is denied; stale attempt and both writer-versus-seal orders; cancellation/common trust failure publishes none; injected finalization rollback leaves no replacement/outcomes/release; unknown finish response reconciles durable committed or uncommitted state without blind publication replay. The native test named `...ReconcilesLostAck` merely calls finish again after a successful returned finish; credit **durable replay**, not injected unknown-C/rollback proof. Existing unchanged Foundation fence tests may be reused only for unchanged behavior.

### FR18-1 / FR18-2 / FR18-8 — Critical acceptance gap — live R7 and external authority are unimplemented/unverified

`J/internal/application/RagSourceService.java:56–57` denies live candidate registration; `RagSourceAuthority.java:72` denies external preparation. The external authority component provides rows/reservations, not the complete approved namespace/identity/consent/read-handle lifecycle. The demonstrated native 8/2 batch uses **DOMAIN** inputs, not the required externally approved document files.

Keeping these denials is correct containment. It does not satisfy ADR-0021 clause 3 or AC-01/03/07. The existing Windows guard/unit tests and mapping diagnostic do not hold a v3 source through Java physical final C. The takeover mapping diagnostic **safely denied** its pre-existing writable mapping case; its nonzero exit is neither a demonstrated bypass nor proof that all required writer/mapping cases are feasible or impossible.

**Classification:** missing integration and mandatory native proof. This host already runs native Windows tests; “no Windows environment” is not the blocker. Particular unsupported NTFS/writer/mapping cases may be genuine platform constraints and must be measured/denied, not inferred or waived.

**Smallest correction under the existing full contract:** one test-scoped native external-source path using the existing authority/reservations/supervisor/finalizer, with an exact approved root/path/ancestors/reparse/hard-link/file object/version/fields/closure; guarded authorized read outside SQL guards with continuous lifetime through physical C. Keep ordinary live capability disabled until independent acceptance. Do not turn arbitrary base64/caller text into approved live-file input.

**Required proof:** safe ordinary source held through actual final C; deterministic attempted path/ancestor/identity/writer/pre-existing mapping changes and revoke/change in both finalization orders; denial of unsupported cases; rollback and unknown-C reconciliation with correct handle/reservation release. Immutable copies require **different source identities** and never establish the original live file's CURRENT state. The owner decision below determines whether to continue this contract or first propose an explicit amendment.

### FR18-7 / FR18-9 — High — required OCR/table quality remains materially incomplete

**Implementation/capability defect plus missing quality evidence.** `P/ocr/engine.py` labels four supported languages but sends the same selected RapidOCR engine through each language label. The independently reproduced Vietnamese misread at high confidence is real, not the earlier PowerShell Unicode fixture error. `P/parsers/pdf.py:148–149` deliberately rejects detected raster tables with `Raster table cell structure is not verified`. This protects against fabricated cells but does not deliver scanned/mixed table support. Genuine native geometry/cell repairs must be preserved.

**Smallest correction:** a bounded development feasibility test of **one** concretely justified Vietnamese/table-capable candidate or configuration, followed by one selected offline pipeline with actual cell/reading-order/merged-cell and original-page geometry. A targeted model/runtime change can be justified by the demonstrated failures; a registry, competing production pipelines, universal document AI platform or repeated indiscriminate vendor comparison cannot.

**Required proof:** freeze the selected configuration and a small source-grouped development/fresh independent held-out set before acceptance; record real reference text/cells/locators, ambiguous-label adjudication, disjointness and complete relevant format/language denominators. Reuse existing unit/negative cases; do not demand a meaningless full Cartesian matrix. Include Vietnamese/English/Hans/Hant, native/scan/mixed PDFs and tables, approved Obsidian dependencies, DOCX and spreadsheets with mandatory publication locators. All unsupported/materially wrong files must reject replacement; confidence is not the oracle. The existing exposed eight PDFs remain useful regression data, not a new held-out pass.

### FR18-10 — High — resource enforcement, operational proof and report completeness remain incomplete

**Implementation and evidence gaps.** `P/telemetry.py` now measures this Python process's Windows peak working set, but not the full owned tree. NVML reads device-wide current usage. `P/worker.py:143–144,332–347` samples VRAM outside/after work, so `peak_gpu_vram_bytes` is not a demonstrated worker/job peak and can include unrelated device users. Own-process CPU time and available host RAM are reported in staging but do not establish finite CPU/temp/expanded-content guards or complete lifecycle measurement.

The supervisor's existing v3 **2 GiB** process-job cap, worker 10 MiB/500-page/1M-character limits and SQL/parser chunk/row limits are finite safety measures. No representative measured-selection/review record establishes that these limits support the retained scope on 16 GB RAM/8 GB VRAM. The command watchdog is not that record or the user job's cancellation guarantee. `J/api/RagDocumentBatchReport.java` and its service do not yet expose the required OCR work/retry, CPU fallback and CPU/temp/resource detail as the approved lightweight report specifies. Generic `WARN_n` codes do not explain actual nonmaterial warnings.

**Smallest correction/proof:** measure one representative successful bounded workload and targeted over-limit/cancel/restart cases with the existing native job/supervisor. Record peak worker plus owned-child RAM, host headroom, separately attributable/qualified peak VRAM, CPU/wall time, temporary/expanded disk and actual cleanup. Derive finite admitted limits from those observations and obtain the contract-required review/approval before activation. Use existing caps and counters where valid; add only missing bounds/measurements/report fields. Prove no partial replacement on resource/common process failure and distinguish unavailable counters from measured zero. No dashboard, broad stress campaign or repetition of the historical 420-job benchmark is required.

### FR18-6 / FR18-11 — High mandatory evidence gap — changed offline/GPU and final focused gates are not accepted

Real CPU inference, artifact checks and same-model fallback tests are credible. Current native ONNX providers do not include CUDA. Native GPU success/OOM-to-same-model CPU behavior, changed parser/OCR artifact/ABI/offline setup and representative bounded execution are therefore **not verified**, not automatically defective and not waived by CPU-only test success. Classify an incompatible pinned provider/ABI as a concrete environment/setup limitation if reproduced; do not claim that the RTX 2070 itself is absent or incapable. Preserve Practical Local Offline; no new Firewall/WFP requirement is imposed.

Finish the already required small `P18-OFFLINE-MODEL` receipt for the **selected** pipeline and its actual CPU/GPU/fallback/offline behavior, linked to resource evidence. `P18-QUALITY`, `P18-R7-WINDOWS`, `P18-PUBLISH-PG`, `P18-RESOURCES` and full `P18-REPORT` remain incomplete as above. All five green command receipts alone cannot make P18-FINAL accepted. No new evidence-integrity defect was found in the inspected takeover bundle; it correctly discloses its failed/unverified gates.

**Delivery observation — Low, FR18-11:** local untracked `rag-engine/models/.../model.onnx` (118,453,870 bytes) and tokenizer artifacts are eligible for Git addition; `.gitignore` does not exclude them. Make provisioned-artifact delivery explicit and exclude local weights from the eventual source commit unless deliberate vendoring is already reviewed. Keep reproducible hashes/provisioning metadata. No Git LFS/platform project or unrelated cleanup is requested, and no source delivery decision is made here.

## Contract and remaining acceptance reconciliation

| Contract | Current supported conclusion |
| --- | --- |
| AC-01 parsers | Parser/unit behavior improved; end-to-end individually approved external files for the entire retained format set are not demonstrated. Scanned tables are explicitly unsupported. |
| AC-02 OCR/layout | Native geometry/cell tests pass. Selected Vietnamese fails; scan/mixed/table/four-language acceptance absent. |
| AC-03 Obsidian | Parser-level recursive denial/version improvements supported; actual Java external child grants/closure and final-C proof absent. |
| AC-04 chunks/locators | Reproduced chunk-address defects repaired in focused tests; final publication oracle/source binding incomplete. |
| AC-05 model/offline | Real pinned CPU model/vector mapping supported; producer binding and full selected runtime/native GPU/offline/fallback acceptance incomplete. |
| AC-06 publication | Native DOMAIN 8/2, union admission, all-fail synthetic path, sibling purge/reconciliation and durable replay pass. Changed v3 rollback/unknown-C/seal-role/cancel/true-dependent proof incomplete. |
| AC-07 authority/R7 | Default denial preserved; actual approved external-file/child authority and native R7 through physical C missing. |
| AC-08 operations/resources/report | No silent XLSX truncation; unavailable measurements and CURRENT/history distinction improved. Tree RAM/VRAM/CPU/temp/cap/cancel and complete report acceptance incomplete. |

## Smallest next scope and exact finite owner decision

**Recommend A below.** Preserve the full approved contract and first settle feasibility, rather than launching another broad remediation round. Exact producer verification is ordinary unfinished authorized implementation, not a scope decision; it remains required under either direction. Preserve the verified mapping/input/purge/chunker repairs. Work after this review belongs in a separate explicitly scoped implementation session, followed by a separate independent reviewer.

**Decision D18-PT-A — preserve full scope, one finite feasibility checkpoint (recommended).** The owner may authorize this exact instruction:

> Keep ADR-0021/D18-A and AC-01–08 unchanged. Authorize one bounded checkpoint in a separate Codex implementation session: one test-scoped native R7 integration candidate through Java physical C, and one selected Vietnamese/scanned-table OCR candidate justified by the failed cases. Use small frozen development fixtures, fresh independently reserved acceptance fixtures, and separate RAM/VRAM/CPU/temp measurements. Keep normal external-file ingestion disabled. Record PASS, FAIL or NOT_VERIFIED for each lane and stop after that checkpoint; no third Antigravity round, automatic candidate loop, scope reduction or acceptance. Use the existing per-command watchdog ceilings. Return the concrete result and the smallest remaining implementation/evidence slice for independent review.

The R7 lane must test an ordinary approved file through actual physical C and adversarial writer/mapping/identity cases. The OCR lane must establish Vietnamese diacritics and real scanned/mixed table cells/locators; text-only fixture success is insufficient. A failed checkpoint leads to an explicit owner decision, not another automatic round. A passed checkpoint does not waive producer, offline, resource, report or held-out acceptance. No arbitrary new MB/CER/time quota is invented by this review; the separately reviewed user-indexing limits remain mandatory before activation.

**Decision D18-PT-B — request an explicit narrower-contract proposal instead.** If the owner does not want to fund the R7 checkpoint, the exact alternative is:

> Authorize a documentation-only proposed ADR-0021/Phase 18 amendment that defers live external-file ingestion and defines individually approved immutable copies as distinct RAG-owned sources. The original live files must never be reported CURRENT from those copies. Show the exact removed/deferred requirements, identity/provenance/consent/revoke/purge consequences and revised acceptance criteria for owner approval and independent review before implementation. Keep the current full contract in force until that exact amendment is approved.

B is **not** permission to silently declare existing disabled live ingestion complete, weaken privacy/producer/commit fences or drop Vietnamese, scanned/mixed tables, formats or resource proof. Any proposed change to those additional requirements must identify each exact delta and obtain a **separate explicit approval**. Neither D18-A's historical eight-clause approval nor the authorized takeover selected B or approved any reduced Phase 18. No such amendment was found in the current owner record.

After the chosen checkpoint or reviewed amendment, the remaining focused implementation is: exact v3 producer/result verification; supported authorized source/child path; selected OCR/table correction; resource bounds/measurement/report; targeted changed-boundary tests and honest six-package acceptance. Reuse valid unchanged evidence, then aggregate P18-FINAL only when its requirements are met. No new general infrastructure or unrelated full-verification repetition is requested.

## 29-dimension evidence ledger

These are review dispositions, not 29 new implementation tasks. Inherited unchanged architecture/security evidence is reused; critical changed behavior remains blocked as identified above.

| # | Dimension | Disposition / evidence |
| --- | --- | --- |
| 1 | Owner intent/scope | FINDING — full D18-A remains binding; live files/OCR/resources incomplete. |
| 2 | Requirement traceability | FINDING — original-ID repairs credited above; AC table and six-package gaps remain. No IDs self-closed. |
| 3 | Architecture/frozen decisions | FINDING — required ADR-0021 producer/R7 guarantees missing; no frozen ADR-0020 or V1–V17 rewrite found. |
| 4 | Modules/ownership | PASS — RAG-owned additions and existing public source APIs; unchanged module graph and retained full architecture verification. |
| 5 | Recursive layout/grouping | PASS — 175-file recursive inventory, 12 Java package groups/descriptors, existing local API/source/internal/web grouping and additive tree inventory; no hierarchy demanded for style. |
| 6 | Shared HTTP/exceptions | PASS — ADR-0016 root `ApiResponse` and module-local DTO/mapper/advice preserved; no new shared transport module. |
| 7 | SOLID/cohesion | FINDING — exact authority/producer responsibility incomplete in existing finalizer; finish that boundary, no speculative interface split. |
| 8 | DRY/KISS/YAGNI | PASS for reviewed repairs — canonical issued input and existing authority/purge reused; no second chunker/coordinator. Validation defects are recorded separately. |
| 9 | Pattern fit | PASS — concrete services/repositories/parsers; no model registry, scheduler or generic plugin framework. |
| 10 | Business/state/idempotency | FINDING — native DOMAIN 8/2/replay and sibling repair pass; true-dependent/external/unknown-C completion unverified. |
| 11 | Wire/API compatibility | FINDING — canonical v3 encoding repair verified; typed exact staged-result contract incomplete. Existing v2 suite evidence reused. |
| 12 | Input/hostile validation | FINDING — forged source/field/locator output and external authority gaps; bounds and parser denials partly repaired. |
| 13 | Authorization/privacy | FINDING — full live/child/producer acceptance missing; default deny and fresh DOMAIN projection repairs preserved. |
| 14 | Secrets/retention/egress | FINDING — targeted per-generation erasure repaired; changed pipeline offline and remaining true-dependent/external purge proof incomplete. No new firewall gate. |
| 15 | Transactions/locks/races | FINDING — source-first preparation/union and real publication supported; changed v3 seal/rollback/unknown-C/R7 races unverified. |
| 16 | Recovery/cancel/restart | FINDING — durable document identity and repeated sibling reconciliation repaired; native v3 lifecycle/unknown-C proof pending. |
| 17 | PostgreSQL/JPA/permissions | FINDING — actual VECTOR mapping/application startup repaired; producer-only staging and exact-result verification incomplete. |
| 18 | Resource complexity/bounds | FINDING — finite existing caps and corrected counters credited; measured tree/VRAM/temp/expanded limits and supported workload missing. |
| 19 | Index/provenance quality | FINDING — real vectors/chunk repairs supported; complete source/excerpt/locator acceptance absent. Retrieval/ANN/QA N/A for Phase 18. |
| 20 | OCR/multilingual fidelity | FINDING — independent Vietnamese failure, raster-table rejection, incomplete format/language oracle. |
| 21 | Security/native tests | FINDING — native producer positive tests pass; exact changed roles/fences and live R7 through C still missing. |
| 22 | Test adequacy | FINDING — 13 Java/72 Python independently green and 1,321 Java evidence validated; omitted mandatory negatives/quality/operations not covered by counts. |
| 23 | Performance measurement | NOT_VERIFIED — representative selected pipeline/target process-tree RAM, peak VRAM, CPU/temp/cap acceptance absent. |
| 24 | Held-out evaluation | NOT_VERIFIED — exposed diagnostic is not fresh independent acceptance; full labels/denominators/adjudication pending. |
| 25 | Dependencies/artifacts | FINDING — exact local hashes/origin records and CPU inference credited; native CUDA/changed OCR/offline ABI evidence pending. No renewed vulnerability-free claim. |
| 26 | Build/tooling/diagnostics | PASS for executed Ruff/format/mypy/pytest/focused Maven and validated unchanged-source full receipt. IDE inspection N/A to claimed results; aggregate P18-FINAL remains NOT_ACCEPTED. |
| 27 | Reports/logging/telemetry | FINDING — safe history/CURRENT/unavailable readings improved; complete OCR/fallback/resource semantics and measurements missing. |
| 28 | Documentation/governance | FINDING — partial takeover is truthfully documented; this report/status sync replaces stale “await takeover/unreviewed” pointers without rewriting historical reports. Finite owner decision outstanding. |
| 29 | Hygiene/delivery | FINDING (Low) — large local model artifacts are untracked and not ignored; intended source/provisioning delivery needs explicit handling. Review probes ignored; diff check passes; no publishing. |

**Next step:** owner selects D18-PT-A or D18-PT-B explicitly. Preserve the current full contract and containment meanwhile. Continue only the resulting bounded scope in a separate implementation session and obtain independent acceptance afterward. Phase 18 remains INCOMPLETE; no commit message or READY FOR OWNER COMMIT is issued.
