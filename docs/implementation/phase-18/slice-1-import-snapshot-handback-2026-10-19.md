# Phase 18 Slice 1: Import & Snapshot (A+B+C) — Handback Report

**Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`  
**Date:** 2026-10-19  
**Implementer:** Antigravity (`backend-implementer`)  
**Base Commit / Head:** `edac5f466935fdd29698acbe89d4f129efce1701`  
**Governing Documents:**  
- [ADR-0023: Phase 18 Application-Level Snapshot & Worker Trust](../../adr/0023-phase-18-application-level-snapshot-worker-trust.md)  
- [Owner Decisions D12, D13](owner-decisions.md)  
- [Slice 1 Implementation Handoff](slice-1-import-snapshot-handoff.md)  
- [ACTIVE Handoff](../../implementation/handoffs/ACTIVE.md)  
- [Agent Launcher Prompt](../agent-launcher-prompt.md)  

---

## 1. Executive Summary

Antigravity has implemented owner-authorized **Phase 18 Slice 1: Import & Snapshot (A+B+C)** strictly within the bounds defined by ADR-0023, D12, and D13.

### Core Architectural Invariants Maintained:
1. **Application-Level Acquisition (G1):** Switched from OS-level Restricted Token / native NTFS adversarial proofs to application-level safe acquisition in `RagSnapshotAcquisitionService` (regular file verification, extension filtering, reparse/link checks, streaming SHA-256 + length counting, before/after size/mtime/key checks, and automatic temporary file cleanup).
2. **Pending Revision Sealing without Premature Promotion (G3):** Ingestion captures bytes into a uniquely named, no-overwrite store with SHA-256 and byte length verified against the manifest. Persisted as **pending revision** metadata on `RagExternalFileSource`. Crucially, **neither `sourceVersion`, `selectedRevisionId`, nor any active generation `G` is advanced or promoted during Slice 1**. Promotion remains strictly deferred to future trusted physical commit $C_P$ in Slice 3.
3. **Phase 17 Frozen Contract Compatibility:** Live external-file candidate registration via `ragOperations.registerFileCandidate` remains **DISABLED** (throws `IllegalStateException("Live external-file capability is disabled")`), ensuring backward compatibility with Phase 17 frozen lifecycle integration tests. Dedicated snapshot candidate registration is provided via `ragOperations.registerSnapshotCandidate`.
4. **Fail-Closed Indexing:** Indexing external file sources remains disabled and inactive in Slice 1 (`"External file indexing capability is inactive in Slice 1 (disabled)"`), leaving Critical finding **FR18-3 OPEN**.
5. **No Commit / No Push:** Working tree changes remain uncommitted and unpushed for independent Codex review.

---

## 2. Component Disposition (KEEP / REUSE / CHANGE)

| Component | Disposition | Details |
|---|---|---|
| `RagOriginalBytesStore.java` | **REUSE / EXTEND** | Preserved native Windows `MoveFileExW` with non-Windows atomic fallback; reused sealing, verification, and manifest integrity checks. |
| `WindowsNativeFileGuard.java` | **KEEP / UNTOUCHED** | Kept intact; native adversarial proof obligations are out of scope per ADR-0023. |
| `RagSnapshotAcquisitionService.java` | **NEW** | Application-level acquisition engine enforcing G1 safety checks, streaming hash/copy, before/after attribute comparison, and automatic failure cleanup. |
| `V20__rag_snapshot_revisions.sql` | **NEW MIGRATION** | Forward-only migration adding `pending_revision_id`, `pending_revision_hash`, `pending_byte_length`, `pending_sealed_at`, `expected_selected_revision_id`, `expected_source_version` to `rag.external_file_sources`, with sequence grants for `rag_runtime`. |
| `RagExternalFileSource.java` | **EXTEND** | Added pending revision fields, CAS expectation getters/setters, and promotion methods (`setPendingRevision`, `clearPendingRevision`, `promotePendingToSelected`). |
| `RagExternalFileSourceRepository.java` | **EXTEND** | Added queries `findAllSelectedRevisionIds()` and `findAllPendingRevisionIds()`. |
| `RagExternalFileAuthority.java` | **EXTEND** | Added `findByCanonicalDigest()`, `findAllLegitimateRevisionIds()`, and save helpers. |
| `RagSourceService.java` | **CHANGE / EXTEND** | - `registerFileCandidate`: preserved disabled state for Phase 17 compliance.<br>- `registerSnapshotCandidate`: new entry point for snapshot acquisition and pending R sealing in DENIED state.<br>- `capturePendingRevision`: captures new pending R without promoting version.<br>- `approveSource`: handles EXTERNAL_FILE with whole-file consent check, pending R integrity verification, and CAS checks.<br>- `revokeSource`: invalidates source and deletes unselected pending/selected files.<br>- `guardOwners`: handles both domain IDs and external UUID locks. |
| `RagSourceAuthority.java` | **EXTEND** | Fails closed on EXTERNAL_FILE during Slice 1: `"External file indexing capability is inactive in Slice 1 (disabled)"`. |
| `RagOperations.java` & `RagService.java` | **EXTEND** | Added `registerSnapshotCandidate` and `capturePendingRevision` to RAG public API. |
| `RagSourceController.java` | **EXTEND** | `/register-file` delegates to `registerSnapshotCandidate`; added `@PostMapping("/{id}/capture-revision")`. |
| `OpenApiRouteInventoryIntegrationTest.java` | **EXTEND** | Added `POST /api/v1/rag/sources/{id}/capture-revision` (`capturePendingFileRevision`) to manifest and updated endpoint count from 227 to 228. |

---

## 3. Test Verification & Evidence

All test runs were executed via `scripts/run-bounded-command.py` with bounded timeouts.

### 3.1 Focused Test Suite (92 tests)
Command:
```powershell
python scripts/run-bounded-command.py -- cmd.exe /c "mvn -f backend/pom.xml test `"-Dtest=RagOriginalBytesStoreTest,RagWindowsAcquisitionTest,RagSnapshotAcquisitionServiceTest,RagSlice1SnapshotIntegrationTest,RagSourceReservationsIntegrationTest,RagLifecycleIntegrationTest,RagSourceAuthorityIntegrationTest`""
```
**Results:** `Tests run: 92, Failures: 0, Errors: 0, Skipped: 0` (PASS, exit=0, duration: 1m 46s)

Breakdown:
- `RagOriginalBytesStoreTest`: 14 tests, 0 failures (sealing, manifests, collision rejection, tamper detection)
- `RagWindowsAcquisitionTest`: 14 tests, 0 failures (Windows path/file handling)
- `RagSnapshotAcquisitionServiceTest`: 9 tests, 0 failures (non-existent, directories, extensions, size limits, cancelled streams, modified-during-copy)
- `RagSlice1SnapshotIntegrationTest`: 11 tests, 0 failures (snapshot candidate registration, pending R sealing, non-promotion of sourceVersion/selectedRevisionId, duplicate byte separation, whole-file consent, revoke cleanup, CAS concurrency, inactive indexing)
- `RagSourceReservationsIntegrationTest`: 16 tests, 0 failures (domain reservation authority)
- `RagLifecycleIntegrationTest`: 19 tests, 0 failures (retained Phase 17 lifecycle, external file disabled assertions)
- `RagSourceAuthorityIntegrationTest`: 9 tests, 0 failures (authority boundaries)

### 3.2 OpenAPI Route Inventory (1 test / 228 endpoints)
Command:
```powershell
python scripts/run-bounded-command.py -- cmd.exe /c "mvn -f backend/pom.xml test -Dtest=OpenApiRouteInventoryIntegrationTest"
```
**Results:** `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0` (PASS, exit=0, duration: 43.9s)  
Verified that all 228 endpoints, operations, security schemes, and error codes match the OpenAPI specification.

### 3.3 Architecture & Module Boundaries (37 tests)
Command:
```powershell
python scripts/run-bounded-command.py -- cmd.exe /c "mvn -f backend/pom.xml test -Dtest=ApplicationArchitectureTests"
```
**Results:** `Tests run: 37, Failures: 0, Errors: 0, Skipped: 0` (PASS, exit=0, duration: 21.2s)  
Verified that Spring Modulith module boundaries, package isolation, and persistence encapsulation remain strictly intact.

---

## 4. Checklist Signoff (Handoff Requirements)

- [x] **Owner-selected TXT and binary files:** Captured bytes equal original sampled on completion, persisted SHA-256+length and manifest agree with verified R; original unchanged.
- [x] **Safety validations:** File absent, directory, unsafe symlink/reparse, unwritable store, oversize input, altered-during-copy, canceled read, corrupt/temp/seal mismatch all fail closed with no selected invalid R.
- [x] **Distinct source identity:** Two different sources with identical bytes have distinct source/revision authorization and no cross-source physical deduplication; repeated exact input does not spuriously promote R without a guarded decision.
- [x] **Whole-file consent & CAS:** Whole-file consent (`content`) and source-version/reset/CAS are enforced and tested; revoked/stale requests do not select or reactivate R.
- [x] **Atomicity & Recovery:** Target collision, partial manifest, crash/orphan cleanup maintain deny-by-default; no orphan auto-adoption or unintended sourceVersion/selected-R/G change.
- [x] **No premature promotion:** Competing updates or cancel/revoke races cannot promote any pending R in Slice 1; existing domain and Phase 17 tests continue to pass.
- [x] **API & State Invariants:** API produces no changed selected-R pointer/sourceVersion, no false RAG `CURRENT`, no finalized `NO_CHANGE` and no published G; external indexing remains inactive; historical FR18-3 stays OPEN.
- [x] **Bounded execution & logging:** All commands executed via `run-bounded-command.py` with zero exits; no raw source paths or secrets leaked in logs.

---

## 5. Scope Deferred to Future Slices

- **Slice 2 (Parser & Chunker Integration):** Python document parsers (PDF, DOCX, XLSX, Obsidian Markdown), OCR adapters, and local chunking algorithms.
- **Slice 3 (Snapshot Transport & Physical Commit):** Snapshot-to-worker transport, worker validation, producer provenance validation (closing Critical FR18-3), promotion of pending R to selected R, and publication in one trusted physical commit $C_P$.
- **Slice 4 (NO_CHANGE & Verification):** Full same-hash NO_CHANGE candidate bypass, Reindex API, resource profiling (16 GB RAM / 8 GB VRAM), and full P18-FINAL verification suite.

---

## 6. Git Status & No-Commit Confirmation

- **Git Working Tree:** Dirty with Slice 1 changes (10 modified files, 4 untracked files).
- **Git Commit / Push:** NOT performed. No tags created, no PR opened.
- **Controlling Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
