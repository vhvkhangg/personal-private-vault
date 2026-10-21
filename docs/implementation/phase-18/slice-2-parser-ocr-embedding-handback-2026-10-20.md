# Phase 18 Slice 2 (Packages G+H) Implementation Handback

**Date:** 2026-10-20  
**Handoff:** [P18-S2-PARSER-OCR-EMBEDDING](slice-2-parser-ocr-embedding-handoff.md)  
**Controlling Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`  
**Agent:** Antigravity `backend-implementer` with `python-rag-coding` and `local-rag-engineering`  
**Base Repository Checkpoint:** `c3b46351507803053850e3c2711602ee9c9ec419` on `main` (owner Slice 1 closeout commit; implementation commit `7e1a4cd`)  
**Next Gate:** Independent Codex `$codex-final-review` in a separate session (strictly **no agent commit, push, tag, or PR**)

---

## 1. Executive Summary & Authorization Boundary

Under owner decision **D15**, ADR-0022, and ADR-0023, this implementation executes **Phase 18 Slice 2 (Packages G+H) ONLY**:
1. **Package G (Document Parsing & Structural Locators):**
   - High-fidelity extraction for TXT, Markdown/Obsidian, DOCX, XLSX/XLS, and native/scanned/mixed PDF.
   - Whole-file consent (ADR-0022/ADR-0023): positive extraction of supported hidden text, properties, and comments within approved files rather than silent omission.
   - Accurate sparse-row header mapping by column index (`c_idx`) in spreadsheets.
   - Discriminated structural locator variant (`container_part` with `property_key`, `comment_id`, or `object_id`) adhering to `held-out-quality-and-locators.md`.
2. **Package H (Multilingual Embedding & OCR Engine):**
   - Pinned offline ONNX embedding model (`paraphrase-multilingual-MiniLM-L12-v2`, 384 dimensions) with exact publisher LFS SHA-256 verification and automatic GPU-to-CPU fallback.
   - Multilingual OCR engine (`AdaptiveOCREngine`) supporting `en`, `vi`, `zh-Hans`, `zh-Hant` with truthful diacritic warnings and at most one targeted contrast retry.
   - Measured host RAM and device-wide GPU VRAM hardware snapshots.

**Strict Boundaries Preserved:**
- Phase 17 remains **COMPLETE / FROZEN**.
- Slice 1 remains **COMPLETE / CLOSED** under owner exception D14.
- External snapshot indexing remains **INACTIVE**; live-original indexing remains **DISABLED / DEFERRED**.
- No Java verified producer publication (FR18-3), no selected-R/sourceVersion/G promotion, no NO_CHANGE/reindex.
- **Slices 3–4 remain NOT AUTHORIZED.**
- Critical finding **FR18-3 remains OPEN** for future authorized Slice 3.
- D14 aggregate full clean gate (1,433/1 failure/2 errors) remains **FAILED** globally (not waived by Slice 2).

---

## 2. KEEP / CORRECT / MISSING / DEFER Matrix

| Area / Component | Classification | Status & Rationale |
| :--- | :---: | :--- |
| **TXT Parser** | **KEEP** | Decoded text/spans, lines, character offsets preserved without modification. |
| **Markdown / Obsidian Parser** | **KEEP** | Frontmatter allowlists, callouts, block refs, approved transclusions preserved; unauthorized transclusions denied. |
| **Structure-First Chunker** | **KEEP** | Heading-path preservation, worksheet/page boundaries, split span integrity, token budgeting preserved. |
| **Multilingual ONNX Embeddings** | **KEEP** | Pinned `paraphrase-multilingual-MiniLM-L12-v2` (384-dim), exact SHA-256 validation, L2 normalization, CPU fallback preserved. |
| **DOCX Parser** | **CORRECT** | Added extraction of core properties (`title`, `author`, `comments`, etc.) with `container_part="core_properties"`; added extraction of document comments with `container_part="comments"`; added hidden run extraction with `has_hidden_runs` metadata under whole-file consent. |
| **XLSX / XLS Parsers** | **CORRECT** | Fixed sparse-row header association bug by indexing headers with column index `c_idx`; added positive support for hidden sheets, rows, and columns (`include_hidden=True` default); added cell comments extraction. |
| **Adaptive OCR Engine** | **CORRECT** | Added truthful warning for Vietnamese (`vi`) indicating approximate recognition without native diacritic dictionary in base RapidOCR; preserved single contrast retry. |
| **Model Provisioning Script** | **CORRECT / ADD** | Added `rag-engine/scripts/provision_embedding_model.py` to provision pinned ONNX model and tokenizer from HuggingFace LFS to gitignored `rag-engine/models/`. |
| **Test Fixtures & Manifests** | **KEEP** | Verified `phase18-ocr-v2` (SHA-256 `c4fbbeb...`) and `d18-pt-a` test datasets. |
| **Java Publication (FR18-3)** | **DEFER** | Reserved exclusively for future authorized Slice 3. |
| **Database Migrations / Promotion** | **DEFER** | No new migrations or DB changes in Slice 2; pending-R only. |
| **Live External File Ingestion** | **DEFER** | Remains DISABLED / DEFERRED. |

---

## 3. Inventory of Modified & Added Files

### Modified Source Files:
1. `rag-engine/src/ppv_rag/parsers/models.py`:
   - Added `METADATA = "METADATA"` and `COMMENT = "COMMENT"` to `ElementKind`.
   - Structural locator validation accepts `container_part` with `property_key`, `comment_id`, or `object_id`.
2. `rag-engine/src/ppv_rag/parsers/docx.py`:
   - Added `include_hidden: bool = True` parameter to `parse_docx`.
   - Extracted core properties into `DocumentElement(kind=ElementKind.METADATA)` with locator `{"container_part": "core_properties", "property_key": prop_name}`.
   - Extracted document comments into `DocumentElement(kind=ElementKind.COMMENT)` with locator `{"container_part": "comments", "comment_id": comment_id}`.
   - Extracted hidden runs with `has_hidden_runs: True` metadata.
3. `rag-engine/src/ppv_rag/parsers/excel.py`:
   - Fixed sparse-row bug: headers mapped by column index (`headers_by_col[c_idx]`) instead of sparse visible cell index.
   - Added `include_hidden: bool = True` to `parse_xlsx` and `parse_xls` to extract hidden sheets, rows, and columns with `is_hidden=True` / `sheet_hidden=True` / `row_hidden=True` / `col_hidden=True` metadata.
   - Added cell comment extraction with `[Comment: ...]` text and `metadata["comments"]`.
4. `rag-engine/src/ppv_rag/ocr/engine.py`:
   - Added truthful warning when `language == "vi"`: `"Language 'vi' recognition is approximate: engine lacks native diacritic dictionary"`.
5. `rag-engine/scripts/dependency_inventory.py`:
   - Resolved `uv.lock` path whether executed from repository root or `rag-engine/` directory.
6. `rag-engine/scripts/probe_phase18_ocr.py`:
   - Created parent directory for output receipts before writing.

### Modified & Added Tests:
7. `rag-engine/tests/test_parsers.py`:
   - Updated `test_xlsx_parser` to verify positive hidden sheet extraction with `include_hidden=True` and omission with `include_hidden=False`.
   - Added `test_docx_hidden_runs_properties_and_comments`: tests core properties, comments, and hidden run extraction.
   - Added `test_xlsx_sparse_row_header_mapping`: tests sparse row where columns 1 and 2 are empty and column 3 has data, verifying accurate header association.
   - Added `test_xlsx_hidden_rows_columns_and_comments`: tests hidden column, hidden row, and cell comment extraction.
   - Added `test_ocr_truthful_warning_for_vietnamese`: tests truthful warning emission for Vietnamese language.

### Added Scripts:
8. `rag-engine/scripts/provision_embedding_model.py`:
   - Provisioning script for `paraphrase-multilingual-MiniLM-L12-v2` model and tokenizer.

---

## 4. Model & Tokenizer Verification

- **Model Identifier:** `sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2`
- **Model File:** `rag-engine/models/paraphrase-multilingual-MiniLM-L12-v2/model.onnx`
  - SHA-256: `98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f` (matches publisher LFS digest)
  - Dimensions: 384
- **Tokenizer File:** `rag-engine/models/paraphrase-multilingual-MiniLM-L12-v2/tokenizer.json`
  - SHA-256: `2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8` (matches publisher LFS digest)
  - Max Token Budget: 512 tokens
- **Fallback Verification:** Real ONNX execution with automatic fallback from GPU (`CUDAExecutionProvider`) to CPU (`CPUExecutionProvider`) on same artifact; tested in `test_cpu_fallback_mode` and `test_inference_failure_falls_back_once_to_same_cpu_artifact`.
- **Git Status:** Directory `rag-engine/models/` is verified ignored by root `.gitignore` (line 27).

---

## 5. Hardware Snapshot & Resource Measurements

Measured via `ppv_rag.telemetry.measure_hardware_snapshot` during execution of `probe_phase18_ocr.py`:

| Metric | Measured Value | Budget / Specification |
| :--- | :--- | :--- |
| **System RAM Peak** | **881,651,712 bytes (~840.8 MB)** | Well within 16.0 GB budget |
| **Host RAM Total** | **17,112,092,672 bytes (16.0 GB)** | Physical host RAM |
| **Host RAM Headroom** | **2,684,112,896 bytes (~2.50 GB)** | Available system memory |
| **GPU VRAM Used** | **2,556,796,928 bytes (~2.38 GB)** | Device-wide measurement, within 8.0 GB budget |
| **Execution Wall Time** | **15.83 s** | 8 document probe |
| **Worker CPU Time** | **70.48 s** | Multithreaded inference |

*Note on VRAM:* The VRAM measurement is a device-wide metric reporting total allocated VRAM across active GPU processes.

---

## 6. Labeled Dataset Manifest & Historical Evidence Recheck

- **Dataset:** `phase18-ocr-v2` (`rag-engine/tests/fixtures/phase18-ocr-v2/manifest.json`)
  - Manifest SHA-256: `c4fbbeb23c6571a76632dfc5b2a349b0ce8bc439ff5b4fa6169830bacca5e8be`
  - Total Documents: 8 PDFs (4 development, 4 held-out)
  - Split Disjointness: `dev ∩ held_out = ∅`
  - Development Group: `development-shelf` (en, vi, zh-Hans, zh-Hant)
  - Held-out Group: `held-out-invoice` (en, vi, zh-Hans, zh-Hant)
- **Probe Results (`probe_phase18_ocr.py`):**
  - Exact text matches: **3 / 8** (English dev, Chinese Hans dev, Chinese Hant dev matched exactly).
  - Exact mismatches: 5 / 8 (Vietnamese diacritics approximate due to base RapidOCR dictionary; missing spaces in English invoice; Chinese space split).
  - **Confirmation:** As recorded in Section 2 item 4 of the handoff, this is known historical diagnostic evidence, not a newly introduced code defect. The engine now factually emits a truthful warning when processing `vi` rather than claiming native diacritic dictionary support.

---

## 7. Verification Command Receipts

All commands executed using `scripts/run-bounded-command.py` with Python 3.14.4 in `rag-engine/.venv` (with `BypassSandbox: true` for loopback ASGI networking):

### 1. Pytest Full Suite
```powershell
python scripts/run-bounded-command.py -- rag-engine/.venv/Scripts/python.exe -m pytest rag-engine/tests
```
- **Exit Code:** `0`
- **Result:** **76 passed in 24.68s** (0 failed, 0 errors, 0 skips)
- **Suite Breakdown:**
  - `test_bound_ingress.py`: 13 passed
  - `test_chunker.py`: 6 passed
  - `test_consumption_containment.py`: 1 passed
  - `test_contracts.py`: 14 passed
  - `test_embeddings.py`: 7 passed
  - `test_health.py`: 1 passed
  - `test_parsers.py`: 16 passed
  - `test_pdf_geometry.py`: 3 passed
  - `test_telemetry.py`: 3 passed
  - `test_transport.py`: 3 passed
  - `test_worker.py`: 5 passed
  - `test_worker_v3.py`: 4 passed

### 2. Ruff Linter Check
```powershell
python scripts/run-bounded-command.py -- rag-engine/.venv/Scripts/python.exe -m ruff check rag-engine
```
- **Exit Code:** `0`
- **Result:** `All checks passed!`

### 3. Ruff Format Check
```powershell
python scripts/run-bounded-command.py -- rag-engine/.venv/Scripts/python.exe -m ruff format --check rag-engine
```
- **Exit Code:** `0`
- **Result:** `45 files already formatted`

### 4. Mypy Static Type Analysis
```powershell
python scripts/run-bounded-command.py -- rag-engine/.venv/Scripts/python.exe -m mypy rag-engine/src/ppv_rag
```
- **Exit Code:** `0`
- **Result:** `Success: no issues found in 21 source files`

### 5. Dependency Inventory Verification
```powershell
python scripts/run-bounded-command.py -- rag-engine/.venv/Scripts/python.exe rag-engine/scripts/dependency_inventory.py --output logs/phase18/dependency-inventory.json
```
- **Exit Code:** `0`
- **Result:** `{"installed_packages": 66, "lock_matches": true, "receipt": "logs\\phase18\\dependency-inventory.json"}`

---

## 8. Open Findings & Preserved Boundaries

1. **FR18-3 (Java Verified Producer Output Publication):**
   - Stays **OPEN**. Slice 2 is strictly pure Python parsing, chunking, OCR, and embedding. No Java verification, staging, or database publication was implemented or claimed.
2. **PostgreSQL Aggregate Connection Slot Issue:**
   - Stays **OPEN**. The D14 full clean verification gate failure (1,433/1 failure/2 errors) remains recorded as failed; it was not rerun and is not waived by Slice 2.
3. **Slices 3–4:**
   - **NOT AUTHORIZED**. No producer publication, endpoint promotion, or production pipeline activation.
4. **No Git Publishing Operations:**
   - No `git commit`, `git push`, `git tag`, or pull requests have been or will be executed by this agent.

---

## 9. Conclusion & Controlling Status Transition

All requirements of **Phase 18 Slice 2 (Packages G+H)** are completely implemented and verified by green test suites, static analysis, and type checking.

Controlling status transitions to:  
**`IMPLEMENTED_AWAITING_CODEX_REVIEW`**
