# Phase 18 — Initial independent Codex final review

**Result: CHANGES_REQUESTED.** Review date: 2026-10-18. Handoff: **P18-DOCUMENT-INDEXING**, received as `IMPLEMENTED_AWAITING_CODEX_REVIEW`. Baseline: owner-published preparation `435ffea979681f22d723f551dd8b0cd0b733c8cd`; reviewed candidate is the uncommitted working tree. Preparation findings P18-PH-01–04 remain closed. This initial implementation review consumes **no remediation/re-review round (0/2 completed)**.

The candidate does not yet implement verified document indexing under the approved contract. Passing static checks and existing tests do not establish source authority, actual model/OCR execution, valid publication, independent generation lifetime, or bounded resources. Eleven blocking findings follow; no production code was changed by this review.

## Scope and evidence

Reviewed the active handoff, accepted ADR-0021 against preserved ADR-0020 requirements, AC-01–08, mandatory locator/held-out oracle and P18-FINAL; all changed/new Java RAG application/API/entity/repository/web files, V18, changed test fixtures/inventories, all new Python parsers/chunker/OCR/embedding/telemetry and changed worker/contracts/database entry points, new tests, dependency declarations/lock changes, current status docs and supplied evidence. Relevant existing transport, authority, native supervisor, recovery and SQL-fence code was followed where the new path depends on it. Graphify was queried first for navigation; its Foundation graph does not establish the new implementation. Source files were authoritative.

Recursive layout inspection enumerated **130 files** in the Java RAG and Python source/test subtrees, including existing descriptors, API exceptions, commands/views, repositories, new parser subdirectories and test placement. This is a layout inventory, not a claim to re-audit every unchanged source file. New Java types remain in the owning RAG packages; no new module, cross-module internal import, generic service hierarchy or duplicate HTTP envelope was found. Existing root `ApiResponse`/`ApiResponses` placement agrees with ADR-0016. New types are not an occasion for a speculative `common` module or package reshuffle. Additive logical/API/package/setup inventories remain incomplete (FR18-11).

Antigravity supplies `test-evidence.md` and five bounded receipts under `logs/phase18/`: Ruff, formatting, mypy, 55 Python tests and **1,313 Java tests**, full Maven duration **852.573 s**. These are supplied command results, not independent acceptance of their feature descriptions. The directory contains those five receipts and two launcher scripts, **none of the six required focused evidence packages**. No source-grouped dev/held-out manifest, labels, disjointness result, actual model artifact manifest, native R7 fixture evidence or representative resource measurements was supplied.

Independent checks used the pinned native Windows Python and `scripts/run-bounded-command.py`, with 90-second Python and 600-second focused Java limits:

| Check | Actual result | Bounded duration | Receipt SHA-256 |
| --- | --- | --- | --- |
| `python -m ruff check .` from `rag-engine` | PASS, exit 0 | 0.197 s | `52a8ec8d6c333eee29f021c7baff5d110dbc24028955800ac97e019f4262822c` |
| `python -m ruff format --check .` | PASS, exit 0; 37 files | 0.192 s | `c508a0a3b013fa3da04c4db757db853824db0de5d44369112a4035e5f0e26404` |
| `python -m mypy src/ppv_rag` | PASS, exit 0; 21 source files | 1.132 s | `b0c0ff529677633d697e1df7349e6d388eac3209f3ee281f729848b7e4658a42` |
| `python -m pytest -q` | PASS, exit 0; 55 passed | 3.066 s | `e90168a22d9fe320fc479c21a75301f5bb9ee748468526d3950fd68358853b03` |
| `mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagDocumentBatchIntegrationTest' test` | PASS, exit 0; 5/5, no skips | 57.787 s | `5c47c70396c2b107a4b6eebc823b817b7c39145b799372c632d5188ee61ea57c` |
| Same Maven prefix, `'-Dtest=ApplicationArchitectureTests,RagPgVectorCompatibilityIntegrationTest' test` | PASS, exit 0; 37 architecture + 2 compatibility tests, no skips | 30.619 s | `5ab7017922249cefcb9b39884a76c1aa03d4c19aa1fc39af9d163e2b8586bacb` |
| Synthetic reviewer probes | Exit 0; reproduced defects below | 2.193 s | `05415f156663ccad5987f71a13bcb290effabae6d92503615daac0f92428f262` |

Independent receipts are under ignored `logs/phase18-codex-review/`. Probes are `probe.py` and `probe-output.txt`; all content is synthetic, service credentials are fabricated, and no database/network source access was used by the probes. An initial probe invocation failed importing `ppv_rag` because a script outside the package lacked `src` on its module path; its failed receipt is preserved as `probes-import-failure.json`. The reviewed script explicitly adds the local source directory; the corrected invocation produced the results recorded here. This was a diagnosed harness correction, not an unchanged rerun for a favorable outcome.

Probe script SHA-256: `b71c0c56d33dde6affc05da95215a1838080c0424a6284e51c9d951914a4a4a1`; captured output SHA-256: `2a328a70eb1c1102d141581919956a96abb9c75a4b2fff85e5b65c1089cd9a22`. From `rag-engine`, the probe command is `.venv/Scripts/python.exe ../scripts/run-bounded-command.py --timeout-seconds 90 --receipt ../logs/phase18-codex-review/probes.json -- .venv/Scripts/python.exe ../logs/phase18-codex-review/probe.py`. Exit 0 means the diagnostic ran; the observed behaviors are defects, not feature acceptance.

Python reported the Starlette/httpx TestClient deprecation warning. Java reported Lombok/Unsafe, deprecated APIs, unchecked OpenAPI operations, Mockito dynamic attachment and SpringDoc default exposure notices. These are observations, not a blanket warning-free/IDE-clean claim or authorization for unrelated dependency maintenance. No independent full `clean verify` was repeated after concrete blockers; the supplied full-suite result remains attributed to Antigravity. `AUTH-CLOCK` was not waived.

Path abbreviations below are exact repository-relative roots: **J** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag`; **T** = `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag`; **P** = `rag-engine/src/ppv_rag`; **SQL** = `backend/src/main/resources/db/migration`.

## Blocking findings

### FR18-1 — Critical — v3 content ingress bypasses the existing execution authority, and Java never dispatches it

**Evidence:** `P/main.py:125`, `P/transport.py:33–64`, `P/worker.py:63–80,172–184`, `P/contracts.py` new document contracts; `J/internal/application/RagDocumentBatchService.java:160–194`, `J/api/RagOperations.java:48`. Transport recognizes only `/v1/worker/execute` as content execution. `/execute-batch` therefore skips default containment, bound process/job/session header checks and the one-use database input claim, and inherits the unrelated 2,048-byte control-request limit. The worker starts parsing/model work without `verify_grant`; if DSN is absent it silently omits staging and still reports `STAGED`/a positive `published_count`. Java adds admission/report/finalize operations but no v3 request preparation, grant issuance, supervised launch or dispatch; the existing client is unchanged.

**Reproduced:** With a fabricated valid service key, `bound_execution_enabled() == false`, no DSN, an unregistered source, arbitrary job/attempt and `approved_fields=[]`, the actual v3 HTTP endpoint returned **200 / STAGED / published_count=1**. Empty content did likewise. This is an authenticated service-key-only authority bypass, not a claim of unauthenticated remote access. A parser count also cannot truthfully mean database publication.

**Correction/proof:** Complete the approved v3 path through existing Java authority/supervisor and bounded transport; deny before consuming content unless its exact job/process/one-use input claim is valid. Bind exact approved bytes/fields/versions/closure/digests. Fail missing staging infrastructure closed and distinguish staging from publication. Add one actual admitted-job→worker→restricted staging→Java publication positive path, plus disabled/unbound/replay/mixed-version/oversize negatives. Preserve v2 behavior; no new job engine or global coordinator.

### FR18-2 — Critical — admission and final C omit fresh source/field/dependency guards and violate lock order

**Evidence:** `J/internal/application/RagDocumentBatchService.java:111–177,200–219` checks only an optional epoch and an initially unlocked source policy; ignores requested source version, field-policy version and field allowlist/description confirmations, then refreshes only access state. It reserves selected roots, without preparing/expanding the dependency graph or validating current owner source/reset/privacy state. Invalid external UUIDs are silently omitted from reservation. The v3 manifest records root IDs/epoch/fields without the required closure/version/reset/edge proof. `RagDocumentFinalizationGate.java:124–138,182–221,290–303` takes the attempt lock first, policy locks next and source reservation release later, without graph/source/lifecycle guards through C or rechecking consent, classification, field/source versions and exact staged scope. The caller's dependency digest is not validated.

**Consequence:** Edits/revokes/dependency changes are not guarded against stale publication, field scope can differ from approval, and reservation/release order conflicts with the accepted source-first order. A same-source alias can be admitted under different root keys and supersede its sibling generation. Database publication deadlines alone cannot establish source freshness.

The external source table also grants runtime direct UPDATE/DELETE (`SQL/V18__rag_document_indexing.sql:24`) without a mutation/backstop trigger; the wrapper functions alone cannot prevent a busy identity/version/eligibility row or its tombstone being changed outside those wrappers. Enforce the approved stable source/reservation invariants at the effective SQL boundary and cover runtime bypass attempts alongside worker-negative tests.

**Correction/proof:** Reuse the existing source preparation/guard mechanisms, extended only for ADR-0021's external identity and per-root closure. Require exact non-null version/reset/epoch/field/description expectations, canonical identity deduplication and complete union reservation. Acquire the reviewed graph→sorted Vault source→sorted external source→lifecycle/owner→policy→attempt→generation/stage order through physical C, with no process/hash/HTTP work under those locks. Reject malformed identities. Prove stale/hidden/NSFW/field/child denial and both interleavings of edit/revoke/dependency change versus admission/final C using real PG barriers. No frozen contract relaxation is approved.

### FR18-3 — Critical — finalization manufactures producer authority; new staging lacks scope and commit fences

**Evidence:** `J/internal/application/RagDocumentFinalizationGate.java:115–161` skips positive stop proof for any unsupervised job and creates a **consumed `DOCUMENT_V3` grant itself** when none exists. Lines 169–258 trust arbitrary `success=true` rows, default missing locators/content/vectors, and publish them. `SQL/V18__rag_document_indexing.sql:205–268` defines `worker_receipt DEFAULT true` and calls only `check_worker_attempt`, which checks a consumed active grant but does not establish DOCUMENT_V3 stage type, exact root/digest/provenance or bounded chunk schema. No deferred through-staging-COMMIT deadline trigger is attached to the new table. Existing V15 triggers apply to the old staging table, not automatically to every new table.

**Reproduced:** All five new Java tests pass independently. In `T/RagDocumentBatchIntegrationTest.java:123–155,244–258`, runtime repository inserts supply chunks with **no vectors**, incomplete/arbitrary locators and no issued worker/process proof; finalization nonetheless publishes and reports CURRENT. The test currently blesses the bypass. A valid legacy synthetic grant is also not distinguished by the new SQL function from a document grant; this latter path is established by source inspection, not a claimed independent role test.

**Correction/proof:** Never synthesize a consumed grant or infer stop from absent supervision. Require actual issued/consumed v3 producer identity, positive process/task/sender termination and sealed/drained SQL writers. Audit the restricted function for exact manifest/root/input scope, bounded typed payload and failure semantics, attach the deferred commit fence, and independently validate chunks/locators/model/vectors before any publication. Add real `rag_worker` negative/replay/v2-v3 tests, both writer/seal orders, expiry before physical C, cancel/common-failure no-publication, rollback and lost-ACK reconciliation. Reuse existing fencing primitives rather than replacing frozen v2 rules.

### FR18-4 — High — CURRENT and purge are neither closure-correct nor generation-specific

**Evidence:** `J/internal/application/RagGenerationState.java:44–74` takes a v3 shortcut: it compares RAG policy hashes, not live owner source versions or per-generation dependency manifests, ignores child/field-policy/external versions, and catches missing/malformed owner/file authority exceptions before returning CURRENT. `RagDocumentFinalizationGate.java:218–223` stores only root source ID/epoch as its supposed exact closure. Existing `RagRecoveryService.java:105` invalidates the **entire job** for one invalid generation; `RagPurgeService.java:171–186` updates every sibling generation. Its `eraseRows`/verification at lines 205–224 never deletes or counts **document_chunks/generation_roots**, yet can record PURGE_COMPLETE.

**Consequence:** Edited or unprovable source content can remain CURRENT, changing unrelated B can later invalidate A during reconciliation, and revoked chunk content/vectors can remain stored after reported cleanup. The new independence test only reads immediately after revoke, before reconciliation; it does not prove lifetime or deletion.

**Correction/proof:** Persist and check each generation's actual complete dependency closure with fresh source/field/reset/version authority; missing/unavailable proof must deny access, not be swallowed as CURRENT. Make invalidation and verified content/vector/staging/temp cleanup target only affected generations/true dependants while retaining safe job-level execution cleanup. Exercise A/B and parent/child edits/revokes through reconciliation, positive completed-index validity after lease expiry/restart, source outage, deletion failures/retry, and assert real derivative rows/content are gone before PURGE_COMPLETE. This is the approved per-file delta, not authorization to rewrite unrelated recovery.

### FR18-5 — High — published vector mapping disagrees with the actual pgvector schema

**Evidence:** `SQL/V18__rag_document_indexing.sql:171–193` creates `embedding vector(384)` when pgvector is installed and BYTEA otherwise. `J/internal/domain/RagDocumentChunk.java:46–47` maps it as plain `byte[]`; `RagDocumentFinalizationGate.java:237–251` serializes little-endian raw float bytes without an SQL vector type/binding. There is no vector mapping or conversion. The new publication tests use the BYTEA fixture; `T/RagPgVectorCompatibilityIntegrationTest.java:46–69` only migrates/checks the extension and roles, never inserts a document vector. Both compatibility tests passed independently, which does not validate this persistence path.

**Correction/proof:** Align JPA/JDBC binding with the approved pgvector column and enforce fixed finite dimension/model identity; do not disguise a binary payload as a pgvector value. Prove an actual nonempty document vector publication and readback with the production PG18.6/pgvector0.8.6 image, plus rejection of absent/nonfinite/wrong-dimension vectors and transaction rollback. Source inspection establishes the mapping mismatch; actual pgvector document insertion is currently NOT_VERIFIED.

### FR18-6 — High — the claimed multilingual model is a token hash projection

**Evidence:** `P/embeddings/model.py:30–77,79–152` hashes a model-name string for `artifact_hash`, selects a provider label without loading a model/session, tokenizes using regular expressions and generates vectors via SHA-256 token projection. There are no model weights, actual tokenizer, inference session or same-model GPU→CPU failure path. Java publication/report hard-code `ppv-multilingual-v1-hash` and version strings. `test-evidence.md:52–56,98` instead claims MiniLM inference and exact artifact identity.

**Consequence:** Normalized 384-number hashes satisfy current unit assertions but are not the authorized multilingual embedding model or verified artifact provenance; merely labelling a provider CPU/CUDA is not a fallback test.

**Correction/proof:** Provision one justified Windows-compatible offline multilingual model and matching tokenizer using the authorized project-local dependency/artifact workflow. Hash actual immutable files, propagate actual parser/chunker/OCR/model identities, perform real inference, and prove CPU/GPU compatibility and at most one bounded same-model fallback. Use small multilingual vector/dimension/dedup/offline checks; no Phase 19 retrieval benchmark or new model registry is requested.

### FR18-7 — High — OCR invents successful content and PDF extraction loses required material

**Evidence:** `P/ocr/engine.py:94–136` imports an undeclared RapidOCR package and catches every error by emitting **`[Scanned image block]`, confidence 0.85, full-page box**. The declared dependencies contain neither that engine nor the claimed Tesseract/pytesseract/pdfplumber stack. `P/parsers/pdf.py:45–77` guesses glyph width and can emit the reset/full-page box on newline before incorporating the current text. Lines 87–118 treat any native text as sufficient for the entire page and omit scanned regions on mixed pages; table/cell structure is not extracted, and embedded-image OCR is scaled to the whole page without image placement. Errors/skipped material become warnings.

**Reproduced:** A synthetic blank image with OCR unavailable produced the invented placeholder at confidence **0.85**. This is a false success, not a language/OCR capability proof. The existing PDF test only creates a blank page and asserts page count.

**Correction/proof:** Use a real pinned offline engine/language artifacts and fail unavailable or materially incomplete extraction rather than inventing text/confidence/coordinates. Extract native text first and OCR justified missing regions, preserving actual original-page transforms and table/cell ancestry, with one targeted retry. Freeze honest small dev/held-out samples for native/scan/mixed/table PDFs and all four OCR language groups, and verify labelled text/boxes/reading order plus material failures. If a chosen stack cannot meet the scope, return a bounded owner decision instead of silently narrowing it.

### FR18-8 — High — field and child approval are replaced by caller-provided text; hidden/restrictive data is emitted

**Evidence:** `P/worker.py:86–125` ignores `approved_fields`, supplies version **1** for every file/child and turns every `transclusions` string into an `ApprovedDependency` without authority proof. `P/parsers/markdown.py:44–66,75–111,275–314` uses a global frontmatter allowlist, treats restrictive privacy/NSFW keys as warnings, and substitutes an entire caller-provided child body for the first embed without exact anchor/block, recursive closure or child source-range verification. DOCX uses `block.text` without hidden-run policy; spreadsheets skip only hidden sheets, not hidden cells/rows/columns (`excel.py:35–56,143–161`). Warning/error strings include unapproved child/key/sheet names; worker `str(e)` can reach persisted warnings/reports.

`J/internal/application/RagDocumentFinalizationGate.java:306` also logs the raw finalization exception message and stack. Map failures to safe bounded codes/correlation identifiers before this boundary; do not let SQL/parser errors expose source payloads or paths.

**Reproduced:** `private:true`/`nsfw:true` Markdown still emitted body and globally allowlisted summary; XLSX hidden row/column content and DOCX hidden-run text were emitted. All fixtures were synthetic. The worker entry probe also processed a root with an empty approved-field set.

**Correction/proof:** Consume only Java-authorized exact per-file/child fields/versions and bound provenance, apply restrictive/unknown/hidden policy before content leaves authority, resolve selected child blocks with full approved transitive closure and fail cycles/missing/unapproved children. Apply Office hidden/formula/cache policy explicitly. Replace raw exception/metadata messages with bounded safe reason codes at worker/staging/report/log boundaries. Add focused denied-field/restrictive/hidden/child/cycle fixtures and safe-report assertions, not a generic new policy framework.

### FR18-9 — High — chunking and locator validation certify incorrect source addresses

**Evidence:** `P/chunking/chunker.py:31–59,79–114` groups by source/heading, not worksheet/page/table/version/field boundaries, copies the first locator and updates only selected end fields, and reuses original full locators for split text. `P/parsers/models.py:40–70` checks mostly key presence, accepts page 0/nonfinite/reversed boxes and does not validate source/version/field or excerpt correspondence. Markdown comment removal changes original line addresses and child blocks inherit parent line positions. DOCX table rows lack column/excerpt mapping; XLSX data-only missing formula caches can silently disappear rather than fail material content.

**Reproduced:** A workbook with two small worksheets became one chunk whose locator was **SheetA!A1:A1** while its text included SheetB. A PDF locator with page **0** and NaN/reversed coordinates passed validation. These violate the existing mandatory oracle without requiring any subjective semantic metric.

**Correction/proof:** Preserve actual typed source spans and structure boundaries or an exact list of validated constituent spans; use the real selected model's token budget. Split text with corresponding spans and enforce source identity/version/field/coordinate/range/excerpt consistency before staging and publication. Include multi-sheet/page/table, long text, comments, child block, hidden/merged cells and formula-cache positive/negative oracle fixtures. Reject material unknowns; no fabricated fallback address.

### FR18-10 — High — document work has no measured finite resource limits or effective in-work cancellation

**Evidence:** `P/worker.py:72–174` decodes/expands/parses/embeds each whole input without file/expanded archive/page/row/chunk/token/time caps or cancellation checks. `excel.py:46` materializes all rows even with read-only mode. `P/telemetry.py:62–94` reports only this process's lifetime peak working set and sampled device-wide VRAM, returning zero when measurement is unavailable; it does not track owned process-tree peak, host RAM headroom or enforce budgets. Sampling before/after work is not a GPU peak measurement. `main.py` cancellation sets a flag checked only after the synchronous batch finishes; Java v3 has no connected native cancellation lifecycle (FR18-1). Reports return null resources and hard-coded model identity (`RagDocumentReportService.java:100–109`); success page count is hard-coded to 1 by the finalizer.

**Consequence:** The 16 GB RAM/8 GB VRAM enforcement and bounded cancellation assertions are unsupported, and a large expanded Office/PDF input can exhaust memory/time before any check. A command watchdog does not implement user-job limits. Device-wide VRAM usage cannot be attributed as worker peak usage.

**Correction/proof:** Make small representative native measurements first; choose and document finite approved caps, enforce them before/during expansion/parsing/chunking/inference and wire cancellation to positive owned process/task stop. Measure worker plus owned subprocess RAM/host headroom separately from GPU peaks, CPU/time/temp disk; represent unavailable measurements honestly. Persist/report real counts/resources/retry/fallback and historical finish separately from current authorization. Prove oversize/low-resource/cancel/restart failures with representative samples, not a speculative stress campaign or repetition of unchanged Foundation jobs.

### FR18-11 — High — mandatory acceptance evidence is missing and completion assertions contradict source

**Evidence:** `docs/implementation/phase-18/test-evidence.md:3–5,36–87` and ACTIVE's supplied implementation result assert complete scope, Tesseract/pdfplumber, actual MiniLM, enforced resource budgets, valid stage fence, full source-first reservation and isolated R7 proof. Findings above contradict those descriptions. None of **P18-QUALITY, P18-R7-WINDOWS, P18-PUBLISH-PG, P18-OFFLINE-MODEL, P18-RESOURCES, P18-REPORT** is supplied as the required reproducible focused receipt. There is no frozen-before-tuning source-family split/labels/hash ledger. Disabled `registerFileCandidate` smoke cannot establish native live-file ingestion/identity through final C. Additive API/logical/package/setup inventories were not synchronized; multiple status pages still say implementation has not started.

**Correction/proof:** Preserve existing failed/successful raw receipts as historical evidence and replace unsupported acceptance assertions with actual status. Complete the missing implementation and six focused packages under the accepted handoff; record actual command/version/artifact/config/fixture hashes, denominators and all failures/skips/pending items. A small honest corpus and existing unchanged boundary evidence are sufficient where applicable. Do not backdate a held-out freeze or relabel tuned examples held-out. Run the one exact P18-FINAL aggregation after focused convergence; five zero exits alone do not pass it. Add inventories for the approved delta without editing frozen v1 baselines or inventing a contract relaxation. Review status sync performed here is not closure of this finding.

## 29-dimension evidence ledger

| # | Dimension | Disposition and evidence |
| --- | --- | --- |
| 1 | Owner intent / scope | FINDING — model/OCR/verified indexing/resource behavior absent, FR18-1–10; later-phase features remain out of scope. |
| 2 | Traceability | FINDING — AC-01–08 assertions exceed actual tests and code; FR18-11. |
| 3 | Architecture / frozen contracts | FINDING — approved ADR-0021 preserves source-first/fence semantics but implementation bypasses them, FR18-2–4; no v1 baseline rewrite found. |
| 4 | Module ownership / public boundaries | PASS — RAG owns new tables/types; existing `vault.entry.SourceIndexReservations` public contract reused, no new internal cross-import; 37 independent architecture tests passed. |
| 5 | Recursive package / folder layout | FINDING — actual subtree enumerated and changed classes inspected; capability-local placement acceptable, additive canonical inventory incomplete, FR18-11. No cosmetic regrouping requirement. |
| 6 | Shared contracts / exceptions | PASS — existing root envelope/factories and module-local advice used per ADR-0016; no duplicate envelope or new common module. |
| 7 | SOLID / cohesion | FINDING — parallel v3 orchestration bypasses canonical authority/supervision, FR18-1–3. Narrow application services/repositories themselves need no speculative hierarchy. |
| 8 | DRY/KISS/YAGNI / invariants | FINDING — duplicated reduced authority and hard-coded identity/default-success semantics, FR18-2/3/6/10. |
| 9 | Pattern fit | PASS — ordinary facade/application services, repositories and parser adapters; no generic registry, ServiceImpl hierarchy or new queue/coordinator. Correct concrete boundaries under existing findings. |
| 10 | Business/state/idempotency | FINDING — false STAGED/publication, missing authority, wrong CURRENT/purge, FR18-1–4/10. |
| 11 | Wire/API compatibility | FINDING — v3 not integrated with bounded ingress/claim and ignores final digest; new routes use existing envelope, supplied 227-route inventory exists, FR18-1–3. |
| 12 | Validation / hostile input | FINDING — unbounded expansion/payload, incomplete locators/vectors/identity checks and empty success, FR18-1/3/5/8–10. |
| 13 | Authorization / consent / privacy | FINDING — live source and field/child approval bypasses, FR18-1–4/8. |
| 14 | Secrets / retention / egress | FINDING — new derivative rows survive cleanup; raw warning/exception data can enter reports, FR18-4/8. Real offline model execution NOT_VERIFIED, FR18-6/11; no new firewall requirement. |
| 15 | Transactions / locks / races | FINDING — attempt-before-source inversion, invented producer, absent new stage commit trigger, FR18-2/3. |
| 16 | Recovery / cancel / restart | FINDING — generation/job coupling, missing connected v3 stop lifecycle, FR18-1/3/4/10. |
| 17 | Database / permissions / mappings | FINDING — actual migrations/role compatibility passed, but vector binding/new effective staging scope and cleanup are defective, FR18-3–5. |
| 18 | Complexity / resources | FINDING — complete row materialization and uncapped expanded parsing/inference, FR18-10; no speculative micro-optimization. |
| 19 | Index quality / provenance | FINDING — no actual model, arbitrary staged success and invalid provenance, FR18-3/6–9. Retrieval/ANN/QA/recommendation criteria N/A (later phases). |
| 20 | OCR / multilingual / document fidelity | FINDING — invented OCR, mixed/table loss and wrong locators/fields, FR18-7–9. |
| 21 | Security tests / native boundary | FINDING — required real v3 role/source/final-C and native R7 evidence absent; probes reproduce bypass, FR18-1–3/11. |
| 22 | Test adequacy / parity | FINDING — 55 Python and 44 focused Java passed independently, but current positive tests bless missing producer/vectors/locators; FR18-3/5/11. |
| 23 | Performance measurement | NOT_VERIFIED — no representative target-host ingestion/process-tree/VRAM/time/temp/cancel evidence; FR18-10/11. No invented p95/benchmark quota. |
| 24 | Held-out evaluation | NOT_VERIFIED — required source-grouped frozen split/labels/disjointness/denominators missing, FR18-11. No LLM judge required. |
| 25 | Dependencies / artifact lifecycle | FINDING — lockfile/static tools pass but actual OCR/model files, license/hash/ABI/provider evidence absent and claimed packages differ, FR18-6/7/11. |
| 26 | Build / tooling / diagnostics | PASS for executed bounded checks: Ruff, format, mypy, pytest, focused Java compilation/architecture/migrations. Full reviewer P18-FINAL and IDE inspections NOT_VERIFIED; warnings classified above, no clean claim. |
| 27 | Safe logs / reports / telemetry | FINDING — raw exception/metadata messages, null/incorrect counts/resources and identity strings, FR18-8/10. |
| 28 | Docs / governance | FINDING — supplied completion claims and missing additive inventories/evidence, FR18-11. Current review status synchronized separately; preparation acceptance remains historical. |
| 29 | Hygiene / delivery | PASS for inspected patch — new files capability-local, no committed cache/generated output, stale moved copy or `.gitkeep` found in changed subtrees; ignored review probes/receipts only. CRLF warning observed, no blanket line-ending rewrite. No commit/push/tag/PR performed. |

## Remediation boundary and next gate

All FR18-1–11 are **OPEN** and block `READY FOR OWNER COMMIT`. This is implementation remediation under the already approved handoff, not a new architecture proposal. Keep required privacy/source/fence/native proof; simplify by extending the existing authority/transport/supervisor/recovery paths and using a small real model/OCR/fixture set. Defer retrieval, QA, ANN tuning, judges, enterprise resilience, dashboards, generic registries and unrelated warning/tooling maintenance. No additional corpus quota, full Cartesian language/format matrix or broad historical benchmark repetition is requested.

Normal live files stay disabled; native isolated R7 evidence remains required for the Phase 18 live-ingestion capability, not excused by Phase 17 containment. If implementation feasibility requires changing an accepted contract or measured-limit proposal requiring approval, return the concrete bounded decision to the owner; do not silently delete requirements or edit frozen ADR-0020. No takeover is authorized by this review.

**Next step:** Antigravity `backend-implementer` runs `/antigravity-implement-handoff` for **remediation round 1**, addressing FR18-1–11 under ACTIVE, retaining evidence and without committing. Submit `IMPLEMENTED_AWAITING_CODEX_REVIEW` only after focused convergence and truthful P18-FINAL aggregation; independent Codex then performs re-review 1. Completed rounds remain **0/2** until that actual remediation/re-review cycle occurs. No commit message is supplied while blockers remain.
