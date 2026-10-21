# P18-S2-PARSER-OCR-EMBEDDING — independent final review

Date: **2026-10-20**. Decision: **CHANGES_REQUESTED**. Scope: **Slice 2, Packages G+H only**.

This is an independent review of Antigravity's submitted working tree, not implementation or a Phase 18 acceptance. Eight blocking findings follow. Passing tests do not establish the claimed document fidelity or quality gate.

## Authority, baseline and reviewed changes

Reviewed [ACTIVE](../../handoffs/ACTIVE.md), the **complete** [Slice 2 handoff](../slice-2-parser-ocr-embedding-handoff.md), [D15 and retained D12/D14](../owner-decisions.md), [ADR-0023](../../../adr/0023-phase-18-application-level-snapshot-worker-trust.md), [acceptance obligations](../acceptance-and-evidence.md), the [held-out quality and locator oracle](../held-out-quality-and-locators.md), final-gate boundaries, Slice 1 closeout and [Antigravity's dated handback](../slice-2-parser-ocr-embedding-handback-2026-10-20.md). Applied `codex-final-review`, its 29-dimension coverage rubric, and applicable Python/RAG, reuse/design, testing and bounded-command rules. Graphify was used for navigation; current source controls conclusions.

Actual baseline: `main`, HEAD **c3b46351507803053850e3c2711602ee9c9ec419**, with uncommitted D15 preparation and implementation. Inspected the actual tracked diff and untracked provisioning script, recursively inventoried all parser/OCR/chunker/model, worker/contracts, telemetry, scripts, tests and fixtures, and verified important paths in source. The [evidence record](2026-10-20-slice-2-independent-review-evidence.json) hashes **63** files plus original and independent receipts. Its SHA-256 is `5cd116f6cc99ad44ff371b1619e3faa9ee33591084b23bb2a13120a633377b2d`.

The production diff changes `parsers/docx.py`, `parsers/excel.py`, `parsers/models.py` and `ocr/engine.py`; two diagnostic scripts and parser tests change, and a model-provisioning script is added. Markdown, PDF, chunker, embedding implementation, worker and wire contracts are inherited. Their current required behavior is explicitly within D15's reuse-first gap review; inherited defects below are not attributed as newly introduced changes. No backend Java, migration, Maven, Python dependency/lock/interpreter, Phase 17 archive or Slice 1 closeout change was found. No migration is required for this Python-only patch.

Credited repairs: hidden DOCX runs and selected properties/comments are now extracted; ordinary hidden Excel sheets/rows/columns and XLSX comments are included by default; sparse-row header lookup uses the actual column index. Existing TXT offset tests, native PDF transformed-glyph/table geometry, token-aware splitting, artifact hash checks and real CPU inference remain useful. Reusing these components is appropriate; the KEEP classification for all their behavior is not supported.

## Blocking findings

### S2-01 — High — external whole-file Markdown still vetoes or removes authorized content

**Evidence:** `rag-engine/src/ppv_rag/parsers/markdown.py:61`, `:91`, `:106`, `:157`; `worker.py:194`; `tests/test_parsers.py:40` and `:138`. These tests explicitly preserve obsolete field filtering/comment stripping for external documents. The worker supplies approved fields to this same parser; its DOMAIN branch separately uses prepared text fields.

The independent `whole_file_private` probe rejects an approved external file containing `private: true` with `RestrictiveContentError`. A second probe loses its title, custom property, Obsidian comment and a literal HTML comment inside a fenced code block; emitted texts are `BODY` and an empty code element. Comment stripping happens before code-fence parsing. This contradicts G1 and whole-file consent. The TXT/Markdown byte adapters also use UTF-8 `errors="replace"`, so malformed bytes can become plausible replacement text rather than the required encoding failure.

**Required correction/proof:** distinguish external whole-file data from frozen Vault field authorization; preserve supported properties/comments and code literals with original locators, and reject unsupported malformed encoding. Exercise these cases through the producer-local worker adapter as well as the parser. Preserve DOMAIN/Vault denial tests. No change to Java consent or Slice 1 is required.

### S2-02 — High — dependency syntax and chunking lose child/revision attribution

**Evidence:** `markdown.py:211`, `:226`, `:364`; `chunking/chunker.py:80`, `:105`, `:117`. Independent probes call the actual parser and chunker with supplied payloads only:

- `> [!NOTE]` containing an approved `![[Child]]` emits the literal embed as parent text; child material/edge is absent.
- An approved `![[Child#H|Alias]]` fails because the display alias becomes part of the heading anchor.
- Two aliases for `same-child`, versions **1 and 2**, produce one chunk containing `OLD_REVISION\n\nNEW_REVISION`, labeled **source_version 1**. Constituent locators retain both versions, which does not make the chunk's top-level version true. The chunker checks source identity, not revision or structural/visibility role.

**Required correction/proof:** resolve supported embeds consistently outside inert code, preserve heading/block/alias scope and complete child edges, and reject conflicting child bindings or keep different revisions/roles in separate valid chunks. Add direct parser→chunker regressions for these cases and missing/cyclic/changed child payloads. Existing cycle/depth checks and absence of parser-driven disk/network reads are credited. This does not require integrating Java child approval or publication from Slice 3.

### S2-03 — High — DOCX loses visible/nested material and masks real comment anchors

**Evidence:** `parsers/docx.py:19`, `:82`, `:116`, `:203`; `parsers/models.py:81`. A generated DOCX contains visible hyperlink text, an inner table and a comment anchored using `Document.add_comment` to an actual paragraph run. Parsing returns `BEFORE  AFTER`, omits `VISIBLE_HYPERLINK` and `NESTED_MATERIAL`, and gives the anchored comment only `container_part="comments", comment_id="0"`, without its real anchor. Warnings are empty. Paragraph `.runs` omits hyperlink-contained runs; walking only `cell.paragraphs` omits nested tables. Comment extraction also swallows every exception.

The new structural alternative merely checks that an identity key exists: `comment_id=None` passes validation. It is not the narrowly discriminated, nonempty original-object locator required by the oracle. Row-wide table text also lacks individual column attribution.

**Required correction/proof:** preserve supported inline and nested-table text in original order, with original paragraph/table/row/column paths; retain actual comment anchors; validate nonempty typed structural identities and fail material extraction/attribution errors. Test anchored comments rather than only the new unanchored comment fixture. Unlocated properties may retain a genuine non-page variant; no invented DOCX page numbers are requested.

### S2-04 — High — spreadsheet formulas, merges and grouped visibility are not faithfully represented

**Evidence:** `parsers/excel.py:38`, `:50`, `:81`, `:189`. Independent XLSX probes establish:

- Header A1 plus uncached `=SUM(1,2)` in A2 returns only A1, with **no warning or file failure**. Loading only `data_only=True` discards formula text and cannot distinguish an uncomputed cache from an empty cell.
- A merged **A1:C1** header becomes **A1:A1**, without merged anchor/covered-cell ancestry or rejection.
- Hidden column group **B:D**, with material at C1, returns C1 without hidden metadata. Only the dimension dictionary's starting letter enters `hidden_cols`; the dimension's min/max extent is ignored.

Source inspection also finds no merged-cell or legacy XLS comment extraction/failure handling in the XLS branch. The handback's general XLS/XLSX completeness claim exceeds the demonstrated behavior.

**Required correction/proof:** preserve formulas as inert text with honest cache state, preserve exact merged/covered cells and visibility per original cell, and handle or explicitly fail material unsupported legacy content. Add corresponding XLSX and applicable XLS fixtures. Keep the sparse-column correction. Do not evaluate formulas or hide required positive coverage behind a blanket unsupported-format claim.

### S2-05 — High — PDF selection/order duplicates text and leaves scanned tables unsupported

**Evidence:** `parsers/pdf.py:87`, `:117`, `:131`, `:148`. A generated native PDF with a paragraph above a table emits **A, B, 1, 2, ABOVE_TABLE**. All table cells are appended before all native lines. A second PDF with native text over a full-page raster emits the same text twice, once native and once OCR, at overlapping boxes. That probe uses a deterministic positioned OCR adapter to isolate the actual PDF selection/geometry path; it is not claimed as real OCR-quality evidence. The production parser OCRs every image rectangle without subtracting native coverage and appends OCR after native text.

The current raster-grid path deliberately fails with `Raster table cell structure is not verified`. That is a useful fail-closed behavior, but it does not satisfy the required positive scanned-table capability or justify claiming complete scanned/mixed PDF support.

**Required correction/proof:** selective OCR over deficient regions, no overlap duplication, original combined reading order and material table/merged geometry. Add realistic native/scanned/mixed, overlap and table fixtures with source-box labels; retain fail-closed handling when mandatory geometry cannot be established. No Java publication work is required.

### S2-06 — High — four-script OCR and the mandatory independent quality gate are incomplete

**Evidence:** `ocr/engine.py:45`, `:65`, `:68`, `:112`; `tests/fixtures/phase18-ocr-v2/manifest.json:5`; handback §§2/6/9. The adapter initializes one default RapidOCR engine and uses the requested language as an output label; the new Vietnamese warning does not add a language-capable backend or an admission policy for material errors.

The original handback resource receipt, `logs/phase18-takeover/ocr-quality-1792463760517287700.json`, explicitly says **FAIL / 3 of 8**. Independent replay, `ocr-quality-1792466431893837100.json`, again exits **1**, **3/8** exact strings: development **3/4**, historically labeled held_out **0/4**; en **1/2**, vi **0/2**, zh-Hans **1/2**, zh-Hant **1/2**. There are two source families, eight documents/pages, ten emitted blocks and no tables/cells. Vietnamese `Kệ sách có 45 quyển.` becomes `Ké sach co 45 quyen.`; `Số` becomes `S6`; Traditional `金額` becomes `金额`. Chinese block-joining whitespace and English spacing need adjudication rather than assuming every mismatch is material. The actual script substitutions/diacritic losses already disprove fully verified script fidelity.

The old manifest is unchanged and says **human adjudication PENDING**. Its family split has disjoint IDs, which is credited; these already exposed historical PDFs are not a new independent Slice 2 held-out gate. No submitted label/denominator/adjudication ledger covers the required other formats, transclusion closures, hidden material, merged tables, corrupt and locator-negative cases. `d18-pt-a` describes a separate diagnostic candidate with `production_integration=false`; it does not establish this production adapter's quality.

**Required correction/proof:** correct the OCR/runtime admission gaps using development data; freeze pipeline/config and a credible source-family/closure split; then independently evaluate with original labels, per-format/script denominators and written material-error adjudication. Keep historical failures unchanged. If credible labels or a backend cannot be supplied, record the targeted blocked evidence and seek an owner decision; do not replace it with warning-only acceptance. The current decision is CHANGES_REQUESTED because independently reproduced implementation defects also remain, not merely missing owner policy.

### S2-07 — High — compressed size/row checks do not bound parser expansion

**Evidence:** `parsers/excel.py:38`, `:63`, `:66`; `markdown.py:116`, `:217`; `worker.py:49`, `:190`, `:201`, `:222`. The workbook is loaded before the row check; there is no expanded archive/cell/workbook budget. A **4,851-byte** sparse workbook with A1 and XFD10000 passes the row check and reaches rectangular iteration of **163,840,000 cells**. The reviewer intercepted `Worksheet.iter_rows` at that boundary and stopped before allocation: this proves admission of the dangerous shape, not that the machine was deliberately exhausted.

A 384-character parent with 32 embeds of one 80,000-character approved child produces **2,560,000** characters successfully. Depth eight is not an aggregate expansion limit; the worker's one-million-character check covers parent text before expansion. Similar compressed DOCX/PDF inflation has no demonstrated preallocation bound in this submission.

**Required correction/proof:** finite preallocation/inflation, aggregate cells/expanded text and raster/work budgets appropriate to these existing libraries, plus a bounded malformed/large-input and cancellation/termination receipt. Reuse existing owner process controls rather than inventing a new job engine. The HTTP cancel route marks a flag checked after the synchronous batch returns; the passing cancellation test exercises synthetic v2, not cancellation during these real parsers. No new claim of parser termination or temporary-disk cleanup is established by that test. Java process-death/publication guarantees remain frozen and are not reopened here.

### S2-08 — Medium — handback overstates GPU, resource and receipt verification

**Evidence:** handback §§4/5/7/9; `tests/test_embeddings.py:48`, `:78`; `embeddings/model.py:24`, `:174`; `telemetry.py` and `scripts/run-bounded-command.py:67`.

The named fallback test sets `provider="CUDA"` and injects `Synthetic GPU OOM`, then runs real CPU inference. This is a valid unit test of one retry, **not an observed GPU failure/fallback**. Independent ONNX runtime lists only **AzureExecutionProvider and CPUExecutionProvider**. CUDA execution is **UNAVAILABLE**, an expressly allowed truthful disposition; GPU device memory being measurable does not establish CUDA inference. Do not upgrade dependencies solely to manufacture that evidence.

The handback's original OCR hardware numbers are real and match its retained **failed** receipt. `system_ram_peak_bytes` is this process's peak working set, not total system peak; VRAM is one device-wide sample, not this pipeline's peak. No representative parser→embedding temporary-disk peak or real parser cancellation measurement is supplied. The embedding parser/OCR pipeline version constants also remain identical despite changed extraction behavior, and there is no explicit vector-length rejection in `embed_batch`; the handoff's wrong-dimension/zero/nonfinite negative verification is not supplied by tests asserting only normal outputs.

Section 7 supplies no original per-command receipt paths or `--receipt` arguments. The existing generic `logs/phase18/pytest.json` and `ruff-check.json` are dated **2026-10-18**, so cannot establish the reported October 20 run. This review independently establishes current green checks; it does not retroactively authenticate the original run's 24.68 seconds. The handback's blanket completeness conclusion and preparatory README/status pointers must be reconciled with the actual missing/failed evidence.

**Required correction/proof:** issue a corrective handback preserving the original, distinguish unit fallback from GPU UNAVAILABLE, report process RAM/host headroom/device sample types honestly, supply bounded workload/disk/termination measurements and original command receipts, verify required vector rejection paths, and make the pipeline identity distinguish changed configurations. A small honest producer-local evidence set is enough; no aggregate Java rerun or new benchmark platform is requested.

## Independent verification and its limits

All commands ran from the repository root on Windows 11 with **rag-engine/.venv Python 3.14.4**. Tests used the existing libraries and local model files. Commands used `python scripts/run-bounded-command.py --profile normal --timeout-seconds <limit> --receipt logs/phase18/<receipt> -- rag-engine/.venv/Scripts/python.exe <arguments>`:

| Arguments / receipt basename (all dated `2026-10-20.json`) | Limit | Observed result |
| --- | --- | --- |
| `-m pytest rag-engine/tests -q` / `s2-independent-pytest-` | 1200 s | **76 passed in 19.85 s**, no failures/errors/skips; exit 0 |
| `-m ruff check rag-engine` / `s2-independent-ruff-` | 180 s | PASS, exit 0 |
| `-m ruff format --check rag-engine` / `s2-independent-format-` | 180 s | 45 files already formatted, exit 0 |
| `-m mypy rag-engine/src/ppv_rag` / `s2-independent-mypy-` | 180 s | 21 source files clean, exit 0 |
| `rag-engine/scripts/dependency_inventory.py --output logs/phase18/s2-independent-dependency-inventory-2026-10-20.json` / `s2-independent-dependencies-` | 180 s | 66 installed packages match lock, exit 0 |
| `rag-engine/scripts/probe_phase18_ocr.py` / `s2-independent-ocr-` | 180 s | **FAIL, 3/8**, exit 1; historical diagnostic replay |
| `logs/phase18/s2-independent-diagnostics.py` / `s2-independent-diagnostics-` | 180 s | Exit 0: reproduced observations in S2-01–05; **not** a fidelity PASS |
| `logs/phase18/s2-independent-bounds-probe.py` / `s2-independent-bounds-` | 60 s | Exit 0: guarded shape/expansion observations in S2-07 |
| `logs/phase18/s2-independent-model-probe.py` / `s2-independent-model-` | 180 s | Real CPU, four finite normalized 384-dimensional vectors, no attempted Python socket calls under a deny guard; exit 0 |

The model probe checks actual local artifact SHA-256: model `98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f` (118,453,870 bytes), tokenizer `2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8` (9,081,518 bytes). These match runtime/provisioning constants; model files are ignored and untracked. Runtime uses local files; the new explicit provisioning script is not imported by the worker and was not executed. The socket guard proves this tested application path, not OS confinement or comprehensive native telemetry denial.

Independent OCR replay measured **25.974 s wall / 81.641 s process CPU**, **881,754,112 bytes process peak working set**, **1,170,075,648 bytes host headroom**, **17,112,092,672 bytes physical host RAM**, **2,543,890,432 bytes instantaneous device-wide VRAM**. The tiny model probe measured **1.906 s wall / 2.000 s CPU**, **567,005,184 bytes process peak**, with CPU provider. These are diagnostics, not proof of bounded worst-case workloads. Host headroom is shared and time-dependent. No GPU equivalence, parser-bomb exhaustion, whole-pipeline temporary-disk peak or new Java cancellation test is claimed.

The [evidence JSON](2026-10-20-slice-2-independent-review-evidence.json) preserves raw result data, artifact hashes and exact bounded exits. Reviewer scripts live in ignored `logs/phase18/`; no implementation/test suite was changed. `git diff --check` passed; Git reported existing CRLF→LF normalization notices, not compiler warnings. No Maven/full clean suite was run because no Java/SQL boundary changed and repeating its unchanged failure would not prove Slice 2 quality.

## 29-dimension evidence ledger

| # | Dimension | Disposition and evidence |
| --- | --- | --- |
| 1 | Owner intent/scope | PASS for authority: D15 authorizes G+H only; no expansion performed. |
| 2 | Traceability | FINDING: G1/G2/G3/H and quality exit proofs remain unmet, S2-01–08. |
| 3 | Architecture | PASS scoped: ADR-0023 application trust retained; no frozen Java/schema/diagram change. |
| 4 | Ownership/dependencies | PASS scoped: Python-local changes; worker still depends on existing contracts, no new cross-module access. |
| 5 | Recursive package/tree inspection | PASS structure: 63-file inventory; reviewed all parser formats and adjacent producer/fixtures/scripts. Existing capability packages reused; provisioning script fits scripts owner. Frozen tree artifact unchanged. |
| 6 | Shared contracts/exceptions | FINDING: new structural locator validation is too loose, S2-03. No ApiResponse/HTTP envelope change; ADR-0016 root ownership unaffected. |
| 7 | SOLID/cohesion | PASS structure: focused parser/OCR adapters and shared element/chunk models; no speculative hierarchy needed. Behavior defects remain separately recorded. |
| 8 | DRY/KISS/invariants | FINDING: identity/role boundaries and inconsistent whole-file policy, S2-01–04. Reuse preferred over parallel implementations. |
| 9 | Patterns | PASS: existing adapters/chunker/model suffice; no new factory/event/job framework justified. |
| 10 | Business correctness | FINDING: silent material omission, wrong revision, duplicates/order, S2-01–06. |
| 11 | API/wire compatibility | PASS scoped: wire/worker code unchanged; v2, contract and default-containment Python tests pass. Not new Java publication certification. |
| 12 | Validation | FINDING: malformed encoding, loose object identity, compressed/cell/expansion bounds, S2-01/03/07. |
| 13 | Authorization/privacy | FINDING: external whole-file and child attribution gaps, S2-01/02. No observed parser original-path discovery or network resolution; Vault authority untouched. |
| 14 | Secrets/egress/artifacts | PASS scoped: explicit provisioner separated, local pinned model hashes verified, ignored artifacts, sanitized worker exception handling. OS malicious same-user proof excluded under D12. |
| 15 | Transactions/concurrency | N/A new implementation: no DB/Java/concurrency changes; accepted Slice 1 guards/CAS remain frozen. No new transaction proof or publication claim. |
| 16 | Failure/recovery/cancellation | FINDING for producer-local limits/termination evidence, S2-07/08. Frozen Java recovery not reopened; synthetic cancel test does not prove real parser interruption. |
| 17 | Database/migrations | N/A: no migration/mapping/SQL change. Existing FR18-3 and aggregate PG issue remain OPEN. |
| 18 | Complexity/resources | FINDING: measured small-input expansion and dangerous sparse shape, S2-07; limited resource sampling, S2-08. |
| 19 | Data/locator quality | FINDING: wrong revision, missing anchored/merged provenance and order, S2-02–06. Retrieval/QA metrics N/A to this slice. |
| 20 | Document/OCR fidelity | FINDING: deterministic source probes plus real OCR replay, S2-01–06. |
| 21 | Security tests | FINDING: existing grant/ingress/containment negatives pass, but current whole-file/dependency/hostile-format coverage incomplete. No superseded native security proof required. |
| 22 | Test quality | FINDING: green 76 include assertions preserving obsolete omissions; new regressions miss actual anchored comments, merges, formulas and mixed PDFs. |
| 23 | Performance measurements | FINDING: original and reviewer measurements credited only as small diagnostics; disk/large-work/cancel proof incomplete, S2-07/08. |
| 24 | Independent evaluation | FINDING: frozen family IDs credited; exposed failed diagnostic, pending adjudication and missing per-format labels, S2-06. |
| 25 | Dependencies/lifecycle | PASS scoped: unchanged lock/interpreter; 66-package inventory and local model hashes verified; no runtime download added. No new global vulnerability-free claim. |
| 26 | Build/static/tooling | PASS executed scope: pytest/ruff/format/mypy/dependency checks above; bounded receipts retained. Java full gate not rerun or relabeled. |
| 27 | Observability | FINDING: resource type/provider and original receipt attribution need correction, S2-08; generic warning codes do not adjudicate material OCR loss. |
| 28 | Documentation/governance | FINDING: handback completeness and stale READY_FOR_IMPLEMENTATION pointers conflict with evidence; S2-08. Original history/receipts preserved. Canonical review status synchronized only. |
| 29 | Delivery/hygiene | PASS scoped: no tracked models/cache, no Java/SQL/frozen archive edits, no publishing. New review/evidence files intentional; existing dirty user work preserved. |

## Preserved gates and next action

**Phase 17 remains COMPLETE/FROZEN. Slice 1 remains COMPLETE/CLOSED under D14; S1-01–07 closures and its original 210-test receipts are not reopened or reassigned.** Pending-only R, selected-R/sourceVersion/G semantics and application-level G1/G3 remain unchanged. External snapshot indexing remains INACTIVE; live originals DISABLED/DEFERRED. Slices 3–4 remain NOT AUTHORIZED; FR18-3/native producer provenance and aggregate PostgreSQL connection-slot liability remain OPEN. The original clean gate remains **FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips, exit 1**; D14 is no Slice 2 or P18-FINAL waiver.

**Next step:** owner explicitly authorizes a separate bounded `$codex-implementation-takeover` session for S2-01–08, then a separate independent re-review. Historical Antigravity remediation **2/2 exhausted** is preserved; D15 authorized this initial Slice 2, not an automatic further loop. This review does not itself authorize implementation, Slice 3, Phase 18 acceptance or owner commit. No commit message is supplied while blockers remain. No fixes, commit, push, tag or PR were performed.
