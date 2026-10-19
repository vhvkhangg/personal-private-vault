# Phase 18 — Independent Codex final re-review 2

**Result: CHANGES_REQUESTED.** Reviewed 2026-10-18 under **P18-DOCUMENT-INDEXING**, received as `IMPLEMENTED_AWAITING_CODEX_REVIEW` after Antigravity remediation round 2. Baseline remains owner-published preparation `435ffea979681f22d723f551dd8b0cd0b733c8cd`; the candidate is the uncommitted working tree. [Initial findings](2026-10-18-phase-18-initial-codex-final-review.md) and [re-review 1](2026-10-18-phase-18-codex-final-re-review-1.md) are preserved. Preparation P18-PH-01–04 remains closed.

**Two unsuccessful Antigravity remediation + independent re-review rounds are now complete: 2/2. Stop Antigravity remediation.** No third round, implementation in this review session, automatic takeover, commit or publishing is authorized. The next implementation gate is a **separate owner-invoked `$codex-implementation-takeover` session**, limited to unresolved authorized findings, followed by a **separate independent reviewer**.

Substantive corrections are credited: mypy now passes; model/tokenizer hashes are checked and missing artifacts reject; the supervisor and SQL verifier discriminate v3; finalization starts with graph/source reservations; generation state checks more root versions; the reconciler now attempts per-generation deletion; several privacy, PDF and spreadsheet probes now pass. However, real Java-issued document input is rejected by Python, the mapped entity fails schema validation on the pinned pgvector image, source/closure/receipt/purge invariants remain incomplete, and required focused acceptance evidence is still missing. **Phase 18/P18-FINAL is not accepted**, irrespective of passing command counts.

## Scope and independent verification

Applied the existing final-review/shared 29-dimension, Java/Spring, persistence/migration, Python/RAG, bounded-execution and proportionality rules. Re-read the active handoff, round-2 claims and six focused JSON packages; inspected changed batch authority/supervision/grant/client/finalizer/state/recovery/purge and effective V18 SQL against preserved v2 behavior; inspected Python worker/digest/model/parser/chunker/resource changes and tests, additive API/DBML and current status documents. Unchanged relevant source and accepted boundary evidence from the earlier reviews were reused. Graphify was queried first for navigation (8,894-node graph), with important facts verified in source.

Recursive layout inventory remains **131 files** across Java RAG and Python source/tests. Inspected actual package descriptors, public API commands/views/exceptions, persistence/web separation, model artifacts and parser folders; no new module cycle, cross-module internal import, generic hierarchy or duplicate HTTP envelope was found. Existing root `ApiResponse`/`ApiResponses` and module-local DTO/advice ownership remain consistent with ADR-0016. No cosmetic `common` module or restructuring is requested. Additive setup/package/evidence accuracy remains part of FR18-11.

Antigravity supplies **59 Python + 1,317 Java** passing tests, five zero-exit receipts and a full Maven duration of **844.998 s**; these are attributed supplied results. Their six focused JSON files remain assertions without sufficient underlying quality/R7/resource/publication run records. No independent full `clean verify` was repeated after concrete blocking reproductions; unchanged native NTFS unit evidence is credited from re-review 1, not rerun as live v3 proof.

Independent checks used pinned native Python and `scripts/run-bounded-command.py`; Python checks ran from **rag-engine**, with 180-second watchdogs. Focused Maven ran from repository root with a 600-second watchdog. New diagnostics used only synthetic input and owned disposable Testcontainers resources, without changing production code/tests or any real source file.

| Independent check | Actual result | Bounded duration | Receipt SHA-256 |
| --- | --- | --- | --- |
| `python -m ruff check .` | PASS, exit 0 | 0.138 s | `84512ecdbe4e89f7be65c29ddb7c88a36f5969c800f01444a5a2c9fbc1618f1e` |
| `python -m ruff format --check .` | PASS, exit 0; 37 files | 0.141 s | `62f2e69100aabd96ffbe337ad1bb7206ad0dd5dd2963cad542e5dc2f51b14ed4` |
| `python -m mypy src/ppv_rag` | PASS, exit 0; 21 files | 0.840 s | `fb4525d767702780fab441f69ad06ab406482aeab791b5418e9fd9dddd9a19d3` |
| `python -m pytest -q` | PASS, exit 0; **59 passed** | 11.824 s | `ee2a3b07b1a87409642df456258ece2a7430695a43602a222bdf4a6bad6bb177` |
| `mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagDocumentBatchIntegrationTest,RagPgVectorCompatibilityIntegrationTest,ApplicationArchitectureTests' test` | PASS, exit 0; **48 tests**, zero failures/errors/skips: 9 batch + 2 compatibility + 37 architecture | 54.700 s | `e794b7417b90db6083681e45b78b2e6b512b68357dffc9b17895c73cd6bef43d` |
| Java/Jackson digest diagnostic | Executed, exit 0; actual Java serialization captured | 2.550 s | `ab01d2b0b6a721436d062dc18db57100039d2f516c4c2020b3311a233ddb600c` |
| Python document/model/span/digest diagnostics | Executed, exit 0; corrections and defects below observed | 1.062 s | `a0125c9cd627ba67de89ab7ffee7fa7c7395dfaed9a05c579cb6163d7bb8d7cc` |
| Hibernate entity/schema diagnostic on PG18.6 + pgvector0.8.6, all 18 migrations | Diagnostic executed, exit 0; **schema validation FAIL** for embedding type | 9.867 s | `4c0cce124ef07e6a874a82c6aaf0fcf981dc4890a9103260acdb052f667fb7f8` |

Diagnostic exit 0 means the probe ran; it is not a product PASS. Sources/results are under ignored `logs/phase18-codex-rereview-2/`: `DigestProbe.java`, `java-payload.json`, `probe.py`, `probe-output.txt`, `SchemaProbe.java`, `schema-probe-output.txt`. Java probes run with the actual classpath recorded in the architecture-test Surefire XML; Python runs with local `rag-engine/src`. The schema probe creates and closes its own Testcontainer, migrates the current classpath resources, uses the mapped `RagDocumentChunk`, snake-case physical naming and `hibernate.hbm2ddl.auto=validate`, matching the application's required validation convention.

Probe development receipts are retained: the historical parser probe first stopped on the **newly correct** private-child rejection and then the **newly correct** OCR failure; the harness was adjusted to record those successful denials and continue. An initial standalone schema probe lacked Spring's naming convention and reported missing `chunkId`; that is a harness configuration observation, not a product defect. After configuring snake-case naming, the independently reported defect was specifically **vector versus bytea**. No unchanged retry was used to obtain a favorable outcome.

Probe source hashes: `DigestProbe.java` = `e3d2cae40662663ee4c8fc74d6b352110d23e943763cba2a1e4d63d1450f3b5d`; `SchemaProbe.java` = `04cb80a04fe1d465550197333620933d72ecf09144449d9814ca872294034759`; `probe.py` = `91ed5a8b0fa6401701cd44f4e814a409446e58e93fb02a64b8c2503113c31eac`. Captured output hashes: Python = `b3175ab73fdffdaeac8e49f7064b2f216c234064d102823f37afb2979e2e4ec4`; schema = `7788427b8d2e18a1077f19da4c29af1d74a14e79f1eb848f3df9b8087d4c435e`.

The focused run reused compiled classes; observed Mockito/dynamic-agent, VM class-sharing and SpringDoc-default notices were not silenced. The diagnostic's built-in Hibernate pool warning belongs to its disposable harness, not the production datasource. No compiler/IDE-clean or warning-free assertion is made. AUTH-CLOCK was not waived. No full suite was run merely to repeat an already known blocker.

Path abbreviations below: **J** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag`; **T** = `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag`; **P** = `rag-engine/src/ppv_rag`; **SQL** = `backend/src/main/resources/db/migration`.

## Remaining original findings

All original IDs remain **OPEN only for the unresolved requirements stated below**; corrected subissues are not additional remediation work. Original severity is retained. No replacement IDs or new acceptance scope are introduced.

### FR18-1 — Critical — Java and Python hash different inputs; approved source input is still caller-controlled

**Corrected:** v3 no longer routes unconditionally through v2-only Java/SQL validators. A document issuance method and Python digest check now exist.

**Reproduced blocker:** `RagDocumentBatchService.java:329–349` serializes an insertion-ordered map with Jackson, compact JSON and omitted null fields. `P/worker.py:113` instead hashes `json.dumps([r.model_dump() ...], sort_keys=True)`, adding defaults/nulls, sorting keys and including different whitespace/escaping. For one actual Java/Jackson synthetic task, issued SHA-256 is **`ef786d0298b81372eedfedc4d3beabd2f21828b55717e96f32bc4ed648e6acce`**; the actual Python request computes **`49c1b7172b893bc5e39a0caa9d02e558acee551bf1ea7855e69338ba484d50b7`**, and returns **REJECTED before authority is queried**. Downstream grant/staging calls were stubbed only to prevent database access in this serialization diagnostic; the actual worker digest check executed unchanged.

The dispatcher still checks only admitted `root_key` membership and then accepts task-supplied source ID, fields, text/base64 and child text, not exact source-authority-prepared bytes. **Required:** One exact shared input encoding/digest contract and prepared field/version/closure binding, preserving v2. Prove a real Java→native worker→restricted SQL→Java publication positive path and tamper/replay/mixed-version negatives. A Python-only digest test cannot prove cross-language compatibility.

### FR18-2 — Critical — source-first order improved, but full fresh closure/owner checks still do not reach physical C

**Corrected:** Finalizer now starts graph→reserved Vault/external sources→policy→attempt (`RagDocumentFinalizationGate.java:134–212`), and new batch startup takes that subset.

**Remaining:** `RagSourceAuthority.java:226–291` constructs a snapshot from the stored lease and selected root rows; it does not prepare the complete live child closure, owner lifecycle/projection/privacy, source/reset/field/version comparisons. Admission still reads CAS state before reserving and accepts null expectations; manifests contain root metadata without actual dependency edges. Final C still does not validate each staged source/field/version/child against fresh owner authority or the supplied command digest. **Required:** Complete the existing guarded preparation/recheck and exact union/closure semantics, not a second coordinator; deterministic owner edit/revoke/child-change versus final-C tests in both orders. More metadata keys alone are not freshness proof.

### FR18-3 — Critical — finalizer still lacks exact producer/output/provenance verification

**Corrected:** PDF bbox and unknown locator rejection were strengthened; grant synthesis remains removed and stage deadline/type/root checks remain present.

**Remaining:** `RagDocumentFinalizationGate.java:208–230,425–492` accepts a consumed grant row plus staged success without a verified exact output receipt/source identity/field/version/model binding. Locator branches infer format from present keys; TXT/MD `line_end` is still optional, DOCX may pass on a heading string alone, and excerpt/source consistency is not established. Empty successful candidates and duplicate stage rows/last-write selection are not made trustworthy by the boolean `worker_receipt`. The nine Java tests still manufacture a consumed grant and stage through the runtime repository (`T/RagDocumentBatchIntegrationTest.java:42–49`), without exercising the real native document producer. **Required:** Strict discriminated bounded result/receipt validation and the original real-role/fence/seal/cancel/rollback/lost-ACK proof. Keep the valid never-started cancellation behavior separate from positive document-producer proof.

### FR18-4 — High — generation erasure can turn a document into a legacy job and revoke its sibling

**Corrected:** CURRENT compares root source/reset/field versions; recovery now calls `eraseGeneration`.

**Remaining deterministic path:** `RagRecoveryService.java:105–115` decides document versus legacy from whether a `generation_roots` row exists. First reconciliation of revoked B erases that row (`RagPurgeService.java:189–207`); the next reconciliation sees revoked B with no root row and calls **job-wide invalidateJob**, also invalidating unrelated A. Erasure also leaves B's staged document payload while job cleanup refuses active A. Per-generation manifests still contain only the root's metadata, not true child closure, so dependent invalidation is incomplete. **Required:** Classify by durable contract identity that survives content erasure; deny/delete only affected generations and all their sensitive derivatives, preserve A and verify two or more reconciliation passes plus retry/failure cases. No new job engine is needed.

### FR18-5 — High — actual pgvector schema is incompatible with the entity's validated binary mapping

**Corrected:** Native SQL publication casts and numeric finite/dimension checks remain, and JDBC vector insertion/distance tests pass.

**New independent confirmation of the original mapping defect:** After all 18 migrations on **`ppv-rag-postgres:18.6-pgvector0.8.6`**, Hibernate validates the current `RagDocumentChunk` and reports: **wrong column type for embedding; found vector (Types#OTHER), expected bytea (Types#VARBINARY)**. `J/internal/domain/RagDocumentChunk.java:46–47` still maps `byte[]`; `backend/src/main/resources/application.yml:13` requires `ddl-auto: validate`. The existing compatibility test bypasses Hibernate, while batch tests use the BYTEA image, masking this production-schema incompatibility. **Required:** Align the actual entity/storage/readback mapping and validate the application persistence boundary against the pinned vector image; retain validation and prove nonempty publication/readback/invalid-vector rollback.

### FR18-6 — High — real artifact checks fixed; actual generation pipeline identity/evidence still incomplete

**Corrected and independently credited:** real inference, absent-artifact rejection, model/tokenizer hash checks, removal of the production hash-vector substitute and mypy failure. These fixes must be preserved.

**Remaining:** Java generation metadata still hard-codes `bilingual-wordpiece-1.0`/`adaptive-ocr-v18` and a model-name string as its version, rather than validated worker identities (`RagDocumentFinalizationGate.java:273–280` versus `P/embeddings/model.py:16–24`). The worker result does not transmit/verify this actual identity. Artifact origin/license/config/ABI/offline/fallback evidence is incomplete; the supplied JSON does not establish it. **Required:** Persist and verify the actual single model/tokenizer/parser/chunker/OCR identities and supply the already required small reproducible offline/artifact/fallback receipts. Do not redo the now-correct absent/tamper implementation or add a model registry/retrieval benchmark.

### FR18-7 — High — PDF material failure and simple translation fixed, but required geometry/table/language scope remains unverified

**Corrected probes:** OCR failure on a mixed page now rejects the file; translated native text origin is correct. The old invented OCR placeholder remains removed.

**Remaining:** `P/parsers/pdf.py:59–71` still guesses glyph width as half the font size per character, does not establish scaled/rotated glyph geometry, and reports orientation 0; images are passed to OCR using whole-page dimensions without their placement transform (`pdf.py:140–150`). No material table/cell extraction is present. OCR language flags still do not establish the claimed four language groups through labelled results. **Required:** Actual original-page/region/table geometry and the existing small labelled native/scan/mixed/table/four-language quality proof. Confidence or plausible boxes cannot waive the mandatory locator oracle.

### FR18-8 — High — targeted privacy denials improved; independent child/field/version authorization is still missing

**Corrected probes:** private child frontmatter rejects, hidden legacy XLS rows are excluded, and simple Office/root restrictive denials remain fixed. Block-anchor handling and immediate grandchild membership checks were added.

**Remaining:** `P/worker.py:170–237` still invents source version **1** for every file/child and constructs approvals from caller-supplied text. Markdown checks immediate child/grandchild membership but does not recursively resolve/authorize the complete transitive graph or detect approved cycles (`markdown.py:175–260`). Per-file frontmatter fields use a global allowlist rather than exact grants. **Required:** Bind actual Java-approved source/field/version/closure, resolve selected child content through the same privacy/provenance rules and fail cyclic/missing/private/unauthorized descendants. Preserve the now-passing targeted hidden/private fixtures; no generic new policy framework.

### FR18-9 — High — element/page fixes pass; chunk-level addresses still misattribute content

**Corrected probes:** XLSX element B2 retains B2, worksheets stay separate, and short PDF pages no longer merge.

**Reproduced remaining defects in `P/chunking/chunker.py:52–85,131–149`:** two blocks on the **same page** merge into one chunk retaining only the first block's bbox, which does not cover the second block; visible B1/B2 elements become a chunk range **A1:B2** despite hidden A; splitting a 1,800-character paragraph produces second span **1501–1801**, rather than actual **1500–1800**. The chunker still uses character counts while the tokenizer truncates independently. **Required:** Exact validated constituent/merged addresses and text spans under the real tokenizer budget, with meaningful chunk oracle assertions. No guessed offset, oversized locator range or key-presence-only pass.

### FR18-10 — High — measured budget and complete operational/report proof remain missing

**Corrected:** native v3 has a finite **2 GiB** job cap; PDF page cap is checked before full parsing/OCR; staged results no longer claim to be published, and report fields now aggregate stored resource data.

**Remaining:** no measured-selection/approval record justifies the new caps; own-process/device snapshots do not establish process-tree RAM/host headroom and GPU peaks, CPU/temp limits or representative v3 cancellation/restart. The worker does not use the host-headroom functions. XLSX still warns and silently stops after row 10,000 (`P/parsers/excel.py:58–60`), allowing incomplete content to succeed; XLS now rejects that limit. Unavailable counters/report samples can become zero. **Required:** Enforce and honestly measure the approved finite resource path and reject material truncation; distinguish unavailable data, preserve accurate historical/current report meaning and prove real native document cancellation/cleanup. Reuse unchanged Foundation process proofs where applicable; no large stress campaign is requested.

### FR18-11 — High — passing gates and corrected inventories still lack the mandatory focused acceptance proof

**Corrected:** prescribed mypy now passes, actual HTTP routes and staging DBML keys/columns were corrected. Preserve those fixes.

**Remaining:** the six focused files still assert PASS without the frozen manifest/labels/disjointness/format-language denominators and successful published documents behind QUALITY; R7 still cites unchanged Foundation handle unit tests/default-disabled registration instead of a real v3 native source held through Java physical C; PUBLISH lacks the actual document producer, real roles, barriers/rollback/unknown-C proof; RESOURCES repeats unlinked peak/headroom claims. These gaps now coexist with independently proven digest and schema failures. Finding IDs in the implementation ledger are still remapped to different subjects (e.g. original FR18-4 is generation lifetime, not pgvector). **Required:** Accurate original-ID closure accounting and small reproducible evidence for the original six packages, truthful P18-FINAL aggregation and setup/package/source-of-truth synchronization. No backdated held-out freeze, count-only quality pass, third Antigravity round or blanket R7 waiver. Contract changes, if needed, require a bounded owner decision.

## 29-dimension evidence ledger

| # | Dimension | Disposition / evidence |
| --- | --- | --- |
| 1 | Owner intent / accepted scope | FINDING — actual verified full document indexing remains incomplete; FR18-1–11. |
| 2 | Traceability | FINDING — original IDs and focused acceptance proof still do not match completion claims; FR18-11. |
| 3 | Architecture / frozen decisions | FINDING — missing closure/producer guarantees under preserved ADR-0021/0020; FR18-1–4. Source-first subset improved, no reviewer contract edit. |
| 4 | Module boundaries / public interfaces | PASS — 37 architecture tests; owning RAG types/tables and existing public Vault APIs, no new internal cross-import/cycle. |
| 5 | Recursive layout / package grouping | FINDING — 131-file layout and descriptors inspected, grouping reasonable; additive setup/tree accuracy incomplete, FR18-11. No cosmetic hierarchy required. |
| 6 | Shared HTTP / exception placement | PASS — existing root envelope and module-local web/advice remain consistent with ADR-0016. |
| 7 | SOLID / cohesion | FINDING — batch authority trusts stored/caller state instead of complete prepared input; FR18-1–3. |
| 8 | DRY/KISS/YAGNI / invariants | FINDING — inconsistent cross-language serialization and duplicated reduced authority/identity defaults; FR18-1/2/6. |
| 9 | Pattern fit / overengineering | PASS — concrete services/repositories/parsers; no speculative queue/global coordinator/registry. |
| 10 | Business / state / idempotency | FINDING — real dispatch rejects issued input, later reconciliation revokes sibling; FR18-1/4. |
| 11 | Wire/API compatibility | FINDING — digest encoding differs across actual Java/Python; FR18-1. API route inventory corrections credited. |
| 12 | Validation / hostile input | FINDING — source/field/receipt and chunk spans/limits incomplete; FR18-2/3/8–10. Missing model/tamper checks fixed. |
| 13 | Authorization / privacy / consent | FINDING — exact input/child/live source authority remains incomplete; FR18-1–4/8. Several privacy negatives now pass. |
| 14 | Retention / secrets / egress | FINDING — per-generation staging retention and next-pass job fallback; FR18-4. Offline artifact evidence incomplete, FR18-6/11; no new firewall requirement. |
| 15 | Physical transactions / locks / races | FINDING — ordered root locks now present, but exact fresh closure/source/receipt proof through C unverified; FR18-2/3. |
| 16 | Recovery / cancellation / restart | FINDING — erasable metadata used to select job-wide cleanup; native v3 operational proof incomplete, FR18-4/10. |
| 17 | PostgreSQL / JPA / permissions | FINDING — migrations/JDBC tests pass, actual mapped entity schema validation fails on vector image; FR18-5. Effective v3 source-role proof remains incomplete. |
| 18 | Time/memory/I/O complexity | FINDING — measured finite cap enforcement/headroom/expanded input and truncation behavior unresolved; FR18-10. |
| 19 | Index quality / provenance | FINDING — chunk addresses/identity/child closure still wrong or unverified; FR18-6–9. Retrieval/ANN/QA/recommendations N/A. |
| 20 | OCR/document/multilingual fidelity | FINDING — simple probes fixed but exact region/table/language quality not established; FR18-7–9. |
| 21 | Security/native boundary tests | FINDING — real document input/publication/R7 role/source-to-C proof missing; FR18-1–3/11. Unchanged NTFS unit proof reused. |
| 22 | Test adequacy / environment parity | FINDING — 59 Python/48 Java pass, yet real cross-language digest and vector-mapped entity fail outside current fixture shortcuts; FR18-1/5/11. |
| 23 | Credible performance measurements | NOT_VERIFIED — target workload/process-tree RAM/VRAM/CPU/temp/cancel records absent; FR18-10/11. No p95 quota invented. |
| 24 | Held-out evaluation | NOT_VERIFIED — source-grouped manifest/labels/disjointness/denominators not supplied; FR18-11. No LLM judge required. |
| 25 | Dependencies / artifacts / lifecycle | FINDING — real model/tokenizer hashes and absence/tamper denial credited; actual identity/origin/license/offline evidence incomplete; FR18-6/11. |
| 26 | Build/tooling/static diagnostics | PASS for executed Ruff/format/mypy/pytest/focused Maven. Full reviewer P18-FINAL and IDE inspection NOT_VERIFIED; observed warnings classified above. |
| 27 | Safe reports / logging / telemetry | FINDING — staged/publication counter and raw log corrections credited; real measured/available resource semantics and complete pipeline identity incomplete, FR18-6/10. |
| 28 | Docs / workflow / governance | FINDING — completion/receipt/original-ID drift remains; FR18-11. This review records 2/2 exhausted, no third Antigravity round. |
| 29 | Hygiene / delivery | PASS for inspected patch — no tracked caches, targets, obsolete moved copies or stale `.gitkeep`; review probes/receipts ignored. No production edits or commit/push/tag/PR. |

## Required next gate

**CHANGES_REQUESTED; original FR18-1–11 remain open only for the concrete residuals above. Two Antigravity remediation/re-review cycles are exhausted.** Stop Antigravity implementation and retain this exact active handoff, source/evidence and review history. A separate owner-invoked **`$codex-implementation-takeover`** session may implement only the remaining authorized slice, followed by a **separate independent Codex reviewer**. No implementation or self-review takeover occurs here.

Keep current privacy, exact source/producer/commit fencing, native R7 and required focused evidence. Simplify by fixing the existing serialization/mapping/authority/reconciler/pipeline and using small honest fixtures; defer retrieval, QA, ANN tuning, model registries, dashboards, enterprise resilience and unrelated tooling changes. Do not add requirements beyond the accepted handoff or repeat unchanged historical benchmarks. If feasibility demands a contract change, bring the concrete bounded decision to the owner. No READY FOR OWNER COMMIT, commit message, live-file activation or later-phase authorization is given.
