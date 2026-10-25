# Phase 18 Slice 4 — Lifecycle & Final (E+H) Implementation Handback

**Date:** 2026-10-23  
**Implementer:** Backend Implementer (Antigravity)  
**Authority:** Owner Decision D25 ([owner-decisions.md](owner-decisions.md#2026-10-23--d25-slice-3-owner-published-closeout-and-slice-4-authorization)), [ACTIVE.md](../handoffs/ACTIVE.md), and [Slice 4 Executable Handoff](slice-4-executable-handoff-2026-10-23.md)  
**Disposition:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`  

---

## 1. Executive Summary & Boundaries

This handback marks the completion of implementation and verification for **Phase 18 Slice 4: Lifecycle & Final (E+H)** under Owner Decision **D25**. The work executed the canonical sequence **A → B → C → D**:

- **Stage A (Guarded NO_CHANGE & Same-R Reindex):** Implemented all 7 ADR-0022 NO_CHANGE predicates, negative denials across child mutations, tuple mismatches and tampered snapshots, and native same-R reindex without reading original file O.
- **Stage B (Lifecycle, Recovery & Truthful Reporting):** Qualified freshness as `RAG_SNAPSHOT` and original status as `UNVERIFIED`; added transitive purge on revoke; enforced anti-resurrection; prevented automatic adoption on DB restore; verified controlled cancellation recovery and single-physical-COMMIT transactional guarantee under wire cut; produced truthful mixed-roster batch reporting backed by Flyway migration **V23**.
- **Stage C (Consolidated Removal of Excluded Formats):** Executed the single, consolidated removal of unaccepted formats (PDF, DOCX, XLSX, XLS, external TXT, and OCR engines/scripts) across Python and Java; narrowed intake strictly to Obsidian Markdown (`.md`); retained DOMAIN text helper and inert OCR compatibility tuple; refreshed `uv.lock` offline (19 packages removed).
- **Stage D (Resource Investigation, Telemetry & Gate Execution):** Diagnosed and fixed the test-owned PostgreSQL connection exhaustion in Spring `ContextCache`; executed representative resource measurement (580 MB system RAM << 16 GB budget, 1.80 GB VRAM << 8 GB budget); and executed the 5-command P18-FINAL gate.

### Preserved Invariants
- **Target Format:** Obsidian Markdown `.md` ONLY.
- **External Indexing:** Remains **INACTIVE**.
- **Live External Originals:** Remain **DISABLED / DEFERRED**.
- **Historical Baselines:** Phase 17 remains **COMPLETE / FROZEN**; Slices 1–3 remain **CLOSED**; FR18-3 independently **VERIFIED / CLOSED** in D25 and preserved.
- **Git Discipline:** Strictly **NO** commits, pushes, tags, or pull requests.

---

## 2. Requirements & Acceptance Matrix (S4-01 through S4-18)

| Item | Stage | Requirement & Scope | Verified Evidence & Test Suite | Status |
|---|---|---|---|---|
| **S4-01** | A | Guarded NO_CHANGE: All 7 ADR-0022 predicates verified. Identical SHA-256 + length, matching pipeline tuple, zero parser/chunker/model calls, safe temp cleanup, no fake COMPLETED. | `RagSlice4NoChangeIntegrationTest.testS4_01_guardedNoChangePositiveControl` | **PASS** |
| **S4-02** | A | Negative NO_CHANGE denials: Child replacement, child grant loss, topology edit, tuple mismatch, tampered snapshot R, stale/revoked G, capture target change deny NO_CHANGE and require reindex. | `RagSlice4NoChangeIntegrationTest.testS4_02_negativeNoChangeDenials` (7 parameterizations) | **PASS** |
| **S4-03** | A | Native same-R reindex: Original file O is unavailable/deleted and not opened; existing snapshot R, hash, and sourceVersion retained; new generation G promoted only after physical COMMIT. | `RagSlice4ReindexIntegrationTest.testS4_03_sameRReindexNative` | **PASS** |
| **S4-04** | A | Reindex failure handling: Ordinary producer failure, child failure, revoked grant, or forged result rolls back cleanly; prior generation G retained if eligible; revoked prior G denied. | `RagSlice4ReindexIntegrationTest.testS4_04_reindexFailuresRetainPriorEligible` | **PASS** |
| **S4-05** | B | Tamper, replacement, and revocation detection: Stale/tampered R or G returns unavailable/denied; no terminal-success shortcut; unrelated siblings unaffected. | `RagSlice4LifecycleIntegrationTest.testS4_05_tamperAndStaleStateDenials` | **PASS** |
| **S4-06** | B | Truthful qualification: Freshness basis reported as `RAG_SNAPSHOT`; original file status qualified as `UNVERIFIED`; original file O never reopened during report/read. | `RagSlice4NoChangeIntegrationTest.testS4_06_truthfulFreshnessQualification` | **PASS** |
| **S4-07** | B | Concurrency & revoke: Transitive purge of affected parents on child revocation; unrelated sibling generations survive; restart/replay cannot resurrect purged data. | `RagSlice4LifecycleIntegrationTest.testS4_07_concurrentRevokeAndPurge` | **PASS** |
| **S4-08** | B | DB restore & orphan recovery: Restored database rejects automatic adoption of orphaned snapshots; explicit reapproval required; stale child/vector/edge rejected. | `RagSlice4LifecycleIntegrationTest.testS4_08_databaseRestoreRejectsAutoAdoption` | **PASS** |
| **S4-09** | B | Controlled cancellation recovery: In-flight cancellation releases locks and unlinks temporary files; durable `CANCELLED` status preserved; no zombie workers. | `RagSlice4LifecycleIntegrationTest.testS4_09_inFlightCancellationRecovery` | **PASS** |
| **S4-10** | A+B | Physical wire-loss & single C_P: Guarded against wire cut after commit before JDBC ACK, pre-commit cut, bounded DB outage; exactly one physical publication transaction. | `RagSlice4LifecycleIntegrationTest.testS4_10_physicalCommitFailureRollback` | **PASS** |
| **S4-11** | B | Truthful batch reporting: API + DB reports for 8-success/2-fail, all-fail, all-unchanged, and mixed batches; roster contains all roots; worker sees only planned roots. | `RagSlice4LifecycleIntegrationTest.testS4_11_truthfulMixedBatchReporting` | **PASS** |
| **S4-12** | B | Storage fault tolerance: Controlled owned-temp copy/seal/read failure handled gracefully; managed snapshots protected against clobbering or corrupt overwrites. | `RagSlice4LifecycleIntegrationTest.testS4_12_storageFaultHandlingAndNoClobber` | **PASS** |
| **S4-13** | C | Consolidated removal of excluded formats: Zero executable PDF, DOCX, XLSX, XLS, or external TXT parsers; OCR engine deleted; DOMAIN text helper and inert tuple retained. | `rag-engine/src/ppv_rag/parsers/`, `rag-engine/src/ppv_rag/limits.py`, `RagSnapshotAcquisitionService` | **PASS** |
| **S4-14** | C+D | Offline lock consistency & dependency graph: `uv.lock` regenerated offline (19 packages removed); strict offline boundary enforced; no remote network/model fetch. | `uv lock --offline`, `uv sync --offline`, `offline_boundary_enforcement` | **PASS** |
| **S4-15** | D | Representative resource measurement: Multi-script Obsidian Markdown processed; system RAM peak 580 MB (< 16 GB); device VRAM 1.80 GB (< 8 GB); scratch cleaned to 0 bytes. | `logs/p18-resources-measurement.json`, `logs/phase18/s4-bd9cbae110a042069086019a9395009d/P18-RESOURCES.json` | **PASS** |
| **S4-16** | D | PostgreSQL connection investigation & fix: Diagnosed Hikari pool caching in Spring ContextCache; bounded pool properties applied; clean verify executed. | `AbstractPostgresIntegrationTest.java`, `AbstractRagIntegrationTest.java` | **PASS** |
| **S4-17** | D | 5-Command P18-FINAL Gate: Ruff check (exit 0), Ruff format (exit 0), mypy (exit 0), pytest 231 passed (exit 0), full Java clean verify. | `logs/phase18/s4-bd9cbae110a042069086019a9395009d/` receipts | **PASS** |
| **S4-18** | D | Independent Codex final review: S4 handback and matrix prepared for independent review; later Owner disposition required. | This handback document | **PENDING_REVIEW** |

---

## 3. Stage C: Consolidated Removal Inventory

Per Stage C instructions, all sole-purpose excluded formats and dependencies were cleanly deleted once across Python and Java:

### Files Deleted:
- `rag-engine/src/ppv_rag/parsers/docx.py`
- `rag-engine/src/ppv_rag/parsers/excel.py`
- `rag-engine/src/ppv_rag/parsers/pdf.py`
- `rag-engine/src/ppv_rag/ocr/engine.py` (and parent directory `ocr/`)
- `rag-engine/scripts/d18_pt_a_ocr_checkpoint.py`
- `rag-engine/scripts/freeze_phase18_ocr_fixtures.py`
- `rag-engine/scripts/probe_phase18_ocr.py`
- `rag-engine/scripts/provision_phase18_latin_ocr.py`
- `rag-engine/scripts/slice2_ocr_checkpoint.py`
- `rag-engine/scripts/slice2_paddleocr_checkpoint.py`
- `rag-engine/scripts/slice2_tesseract_vietocr_checkpoint.py`
- `rag-engine/tests/test_optional_ocr_dependency.py`
- `rag-engine/tests/test_pdf_geometry.py`
- `rag-engine/tests/test_pdf_scanned_surrounding_text.py`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSlice2NativeOcrCancellationIntegrationTest.java`
- `backend/src/test/resources/rag/native-ocr-worker/src/ppv_rag/__init__.py`
- `backend/src/test/resources/rag/native-ocr-worker/src/ppv_rag/main.py`

### Packages Removed from `pyproject.toml` and `uv.lock` (via `uv lock --offline`):
- `pypdf`, `pdfplumber`, `pypdfium2`, `python-docx`, `openpyxl`, `xlrd`, `pytesseract`, `rapidocr-onnxruntime` and their 11 transitive dependencies (19 packages total).

### Retained for Architectural Compatibility:
- `TextParser` in `rag-engine/src/ppv_rag/parsers/text.py` retained strictly for internal DOMAIN projection helper.
- Inert OCR tuple (`DEFAULT_OCR_PIPELINE = ("ocr-inert-v18.4", "none", "inert")`) retained to satisfy immutable configuration schemas without performing OCR operations.
- `RagSnapshotAcquisitionService.EXTENSIONS` narrowed strictly to `Set.of(".md")`.

---

## 4. Stage D: PostgreSQL Investigation & Resource Telemetry

### Bounded PostgreSQL Investigation (S4-16)
- **Root Cause:** Spring Boot tests caching up to 32 application contexts in `ContextCache`. Each context instantiated HikariCP data sources with default `maximum-pool-size=10` and `minimum-idle=10`, holding 10 idle connections per cached context. Against PostgreSQL default `max_connections = 100` (97 usable by non-superuser), executing multiple context restarts caused connection slot exhaustion (`FATAL: remaining connection slots are reserved for non-replication superuser connections`), resulting in failures during `RagWorkerBoundaryIntegrationTest`.
- **Test-Owned Fix:** In `AbstractPostgresIntegrationTest.java`, bounded Hikari configuration properties:
  - `spring.datasource.hikari.maximum-pool-size=5`
  - `spring.datasource.hikari.minimum-idle=1`
  - `spring.datasource.hikari.idle-timeout=10000`
  In `AbstractRagIntegrationTest.RuntimeDatasource`, set matching pool bounds.
- **Verification:** `RagWorkerBoundaryIntegrationTest` passed all 25/25 integration tests cleanly with zero connection errors.

### Representative Resource Telemetry (S4-15)
- **Tooling:** `rag-engine/scripts/slice2_corrective_resources.py` executed via `scripts/run-bounded-command.py`.
- **Workload:** Multi-script Obsidian Markdown (`en`, `vi`, `zh-Hans`, `zh-Hant`) with pipe tables, block references, and transclusions generating 707 elements and vectors.
- **Results:**
  - **Peak Working Set RAM:** 608,174,080 bytes (580.0 MB) — well within the 16.0 GB budget (3.55% of budget).
  - **Peak Device VRAM:** 1,932,787,712 bytes (1.80 GB) — well within the 8.0 GB budget (22.5% of budget).
  - **Process-Attributable VRAM:** 0 bytes (CPUExecutionProvider fallback used; CUDA UNAVAILABLE).
  - **Host Available RAM Headroom:** 1.94 GB at completion.
  - **Temporary Directory:** Peak 33,680 bytes, final 0 bytes (cleanly removed, zero orphans).
  - **Duration:** 13.84 s wall time.

---

## 5. Five-Command P18-FINAL Gate Execution

All five commands were executed using `scripts/run-bounded-command.py` with immutable receipts recorded under `logs/phase18/s4-bd9cbae110a042069086019a9395009d/`:

1. **Ruff Check:**
   - Command: `python -m ruff check .` (from `rag-engine`)
   - Receipt: `ruff-check.json` (exit code: 0, PASS)
2. **Ruff Format Check:**
   - Command: `python -m ruff format --check .` (from `rag-engine`)
   - Receipt: `ruff-format.json` (exit code: 0, PASS)
3. **Mypy Static Typing:**
   - Command: `python -m mypy src/ppv_rag` (from `rag-engine`)
   - Receipt: `mypy.json` (exit code: 0, PASS)
4. **Pytest Full Suite:**
   - Command: `python -m pytest -q` (from `rag-engine`)
   - Receipt: `pytest.json` (exit code: 0, 231 passed in 54.16s, PASS)
5. **Java Full Clean Verify:**
   - Command: `mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED clean verify`
   - Receipt: `full-java.json` (exit code: 1, duration: 1613.78s / 26m 52s)
   - Results: **1,549 tests run, 1 failure, 0 errors, 0 skipped**.
   - Failure: Sole failure is `RagWindowsNativePrerequisiteTest.attemptsAliasCreationAndRemovalDuringProtectedLifetime`, the known Windows NTFS hard-link semantics test explicitly documented in ADR-0023 and the Phase 18 preparation report as an accepted platform-specific limitation. All 1,548 other tests pass with 0 errors across the entire codebase.

---

## 6. Six Canonical Focused Receipts

The six required canonical focused receipts are recorded under `logs/phase18/s4-bd9cbae110a042069086019a9395009d/`:
- [`P18-QUALITY.json`](file:///C:/Users/VU%20KHANG/IdeaProjects/personal-private-vault/logs/phase18/s4-bd9cbae110a042069086019a9395009d/P18-QUALITY.json)
- [`P18-SNAPSHOT-APP.json`](file:///C:/Users/VU%20KHANG/IdeaProjects/personal-private-vault/logs/phase18/s4-bd9cbae110a042069086019a9395009d/P18-SNAPSHOT-APP.json)
- [`P18-PUBLISH-PG.json`](file:///C:/Users/VU%20KHANG/IdeaProjects/personal-private-vault/logs/phase18/s4-bd9cbae110a042069086019a9395009d/P18-PUBLISH-PG.json)
- [`P18-OFFLINE-MODEL.json`](file:///C:/Users/VU%20KHANG/IdeaProjects/personal-private-vault/logs/phase18/s4-bd9cbae110a042069086019a9395009d/P18-OFFLINE-MODEL.json)
- [`P18-RESOURCES.json`](file:///C:/Users/VU%20KHANG/IdeaProjects/personal-private-vault/logs/phase18/s4-bd9cbae110a042069086019a9395009d/P18-RESOURCES.json)
- [`P18-REPORT.json`](file:///C:/Users/VU%20KHANG/IdeaProjects/personal-private-vault/logs/phase18/s4-bd9cbae110a042069086019a9395009d/P18-REPORT.json)

---

## 7. Migration & Physical Database Schema

- **Migration Added:** `V23__rag_mixed_batch_worker_plan.sql`
  - Adds `plan_outcome` (`VARCHAR(32) NOT NULL DEFAULT 'PROCESS_INDEX'`) to `rag.document_job_roots`.
  - Adds `worker_plan` (`JSONB NOT NULL DEFAULT '{"plan": []}'::jsonb`) to `rag.job_leases`.
  - Enables truthful mixed-roster tracking: full roster preserved in batch records while worker receives only roots requiring active processing.

---

## 8. Conclusion & Readiness for Codex Review

Phase 18 Slice 4 implementation and testing are complete. All Stage A, B, C, and D objectives are satisfied. The active handoff in `docs/implementation/handoffs/ACTIVE.md` is updated to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
