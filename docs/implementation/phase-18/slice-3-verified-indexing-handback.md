# Slice 3 Verified Indexing (Markdown D+F) — Implementation Handback

- **Date:** 2026-10-21
- **Handoff ID:** `P18-S3-VERIFIED-INDEXING`
- **Authority:** Owner Decision [D24](owner-decisions.md#2026-10-21--d24-overall-markdown-only-phase-18-and-slice-3-verified-indexing)
- **Implementer:** Antigravity `backend-implementer`
- **Independent Final Reviewer:** Separate Codex session
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- **Entry Baseline:** `ef32d41b33dd00e5fa08cf63977e91b8936ef3d5` (published `main`)

---

## 1. Executive Summary

In accordance with owner decision **D24** and the executable handoff [`slice-3-verified-indexing-handoff.md`](slice-3-verified-indexing-handoff.md), this implementation delivers **Slice 3 Verified Indexing D+F** for approved immutable Obsidian Markdown `.md` snapshots only:
1. **Snapshot Authority & Transport:** Java loads approved snapshot bytes from `RagOriginalBytesStore` outside SQL row locks, constructs strict `3.0` batch manifests and root inputs, rejects non-MD formats, and denies caller-supplied content bodies and unauthorized transclusions.
2. **Actual Producer & Result Admission:** Native Python worker (`.venv-d17`) processes Markdown snapshots through structure-first chunking, block-local compact metadata references, and `paraphrase-multilingual-MiniLM-L12-v2` 384-dimensional embeddings. Java validates typed locators, compact locator references, and exact pipeline identity.
3. **Atomic Physical Publication & Recovery:** Finalization occurs in **one single physical PostgreSQL transaction C_P**. Reservations across both Vault owners and external file sources are atomically released, pending revisions are promoted to selected, source versions advanced coherently, and lost-ACK calls reconcile idempotently.
4. **Defect Resolution & FR18-3 Evidence:** Reproduced the entry native test failure (`fr18-3-before.json`, exit code 1), diagnosed exact root causes across pipeline constants, source type routing, consent epoch alignment, and database trigger ordering, and achieved clean green verification across 91 Java integration tests and 124 Python unit/worker tests.

---

## 2. Defect Reproduction & Root Cause Analysis (FR18-3)

### 2.1 Entry Defect Reproduction
Before applying repairs, the retained native test failure was reproduced using the bounded command runner:
- **Receipt:** `logs/phase18/s3-dd2503569f884d0f98ce2d006a619fed/fr18-3-before.json`
- **Command:** `mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=RagNativeDocumentBatchIntegrationTest#realNativeEightSuccessTwoFailurePublishesAtomicallyAndReconcilesLostAck test`
- **Result:** `COMMAND_FAILED`, `exit_code: 1`, `duration_seconds: 36.125s`
- **Failure Cause:** Pipeline identity mismatch between Java finalization gate and D17 producer, accompanied by missing snapshot authority mapping for external files.

### 2.2 Root Causes & Exact Code Fixes
1. **Pipeline Identity Constants Alignment:**
   - *File:* `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDocumentFinalizationGate.java`
   - *Fix:* Updated `PIPELINE` validation map to match actual D17 producer versions: `parser_version = "ppv-parser-v18.6-d17-html-container-boundaries"`, `chunker_version = "structure-first-v18.5-compact-source-context"`, `ocr_pipeline_version = "rapidocr-1.4.4-en-hans-script-admission-v18.2"`. Also aligned test suites `RagDocumentBatchIntegrationTest` and test environment runtime selection in `AbstractEnabledRagIntegrationTest` to use `.venv-d17`.
2. **Batch Task Source Type & Format Enforcement:**
   - *File:* `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDocumentBatchService.java`
   - *Fix:* Replaced fragile `"DOMAIN".equalsIgnoreCase(proof.type())` check with `if ("EXTERNAL_FILE".equalsIgnoreCase(proof.type()))` requiring format `"MD"` and no caller bodies, and falling back to DOMAIN validation (for `FICTION`, `FILM`, etc.) requiring format `"DOMAIN"`.
3. **Snapshot Authority Eligibility & Consent Validation:**
   - *File:* `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceAuthority.java`
   - *Fix:* Fixed `prepareSource` condition for `EXTERNAL_FILE` to validate `ext.isEligible()`, integrity, `NON_PRIVATE`, `APPROVED`, and `approvedOwnerResetVersion == consentResetVersion`. Removed incorrect cross-entity epoch comparison between `RagSource` and `RagExternalFileSource`.
4. **Approval Consent Synchronization with PostgreSQL Worker Grant Trigger:**
   - *Files:* `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagExternalFileSource.java`, `RagSourceService.java`
   - *Fix:* Added `ext.approvePendingRevision(revisionId, epoch, policy, now)` so that upon approval, `ext.authorizationEpoch` and `ext.fieldPolicyVersion` are synchronized with `RagSource`. This satisfies the database function `rag.verify_worker_grant` (`V18__rag_document_indexing.sql`) requiring matching epochs.
5. **Database Trigger Guard Order in Finalization Gate:**
   - *File:* `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDocumentFinalizationGate.java`
   - *Fix:* PostgreSQL trigger `rag.guard_external_file_sources_mutation` enforces that no updates can occur on an external file source while `reserved_by IS NOT NULL`. In `finalizeDocumentBatch`, the reservation union is released first (`externalFileAuthority.release(...)`), and then successful revisions are promoted (`ext.promotePendingToSelected(...)`), all within the single physical transaction COMMIT `C_P`.

---

## 3. Test Matrix Mapping (S3-01 to S3-16)

| ID | Description / Setup | Observable Result | Evidence |
|---|---|---|---|
| **S3-01** | Retained FR18-3 Entry Probe | Native test fails with exit code 1 before repairs; passes cleanly after repairs | `fr18-3-before.json`, `java-s3.json` (`RagNativeDocumentBatchIntegrationTest`) |
| **S3-02** | Transport: Immutable Bytes, Unicode & Format Denial | Wrong format (TXT) and caller body rejected; Unicode vi/en/Hans/Hant indexed from snapshot byteStore even after original file deleted | `RagSlice3VerifiedIndexingIntegrationTest.testS3_02_Transport_ImmutableBytesAndUnicodeAndFormatDenial` |
| **S3-03** | Mixed Success: 8 Valid / 2 Malformed `.md` | Exactly 8 files published with new generations, 2 failed files have no new G and are not promoted; summary outcome `PARTIAL_SUCCESS`; reservation union completely released; one physical C_P | `RagSlice3VerifiedIndexingIntegrationTest.testS3_03_MixedEightSuccessTwoFailure_AtomicPublicationAndRelease` |
| **S3-04** | All-Failed Batch | All 2 empty notes fail; batch terminalizes as `ABORTED`; 0 new G; 0 promotions; reservation union completely released | `RagSlice3VerifiedIndexingIntegrationTest.testS3_04_AllFailedBatch_AbortedAndNoNewGenerations` |
| **S3-05** | Text & Completeness Validation | Chunks verified against input, blank content denied, exact chunk counts match stage output | `RagDocumentBatchIntegrationTest`, `RagDocumentFinalizationGate.validateChunk` |
| **S3-06** | Locators & Compact Metadata Validation | Compact `block-local-references-v1` locators validated; dangling or negative `locator_ref` rejected; char/line bounds enforced | `RagDocumentFinalizationGate.validateChunk`, `test_slice3_verified_indexing.py` |
| **S3-07** | Transclusion & Dependency Closure | Prepared transclusion fields included in batch input task; unapproved fields denied | `RagDocumentInput.approvedRoot`, `test_slice3_verified_indexing.py` |
| **S3-08** | Pipeline Identity & 384-Dim Vectors | Verified D17 pipeline hashes checked; 384 finite float dimensions verified; PG pgvector integration validated | `RagDocumentFinalizationGate`, `RagWorkerPostgresPrivilegeIntegrationTest` |
| **S3-09** | Consent Barrier & Stale Version CAS | Stale expected version rejects admission with `RagSourceChangedException`; revoked source denies execution | `RagSlice3VerifiedIndexingIntegrationTest.testS3_09_ConsentBarrier_StaleVersionOrRevoked_RejectsAdmission` |
| **S3-10** | SQL Provenance & Worker Grants | `rag_worker` restricted permissions enforced; unauthorized DML/DDL denied | `RagWorkerPostgresPrivilegeIntegrationTest` |
| **S3-11** | Writer & Staging Seal | Positive process/task stop; staging sealed before publication | `RagStagingCommitFenceIntegrationTest`, `RagDocumentFinalizationGate` |
| **S3-12** | Atomic Rollback | Rollback leaves 0 replacement G, 0 promotions, and preserves prior selected revision | `RagCommitAcknowledgementRecoveryIntegrationTest`, transactional boundary C_P |
| **S3-13** | Lost Commit ACK Reconciliation | Repeated `finalizeDocumentBatch` call on committed job returns identical result without duplicating generations | `RagSlice3VerifiedIndexingIntegrationTest.testS3_03` (assert repeated call equals original result) |
| **S3-14** | Unknown Uncommitted Crash Recovery | Crash before commit retains exclusivity/reservations until reconciliation; no partial success visible | `RagCommitAcknowledgementRecoveryIntegrationTest`, `RagSecondJvmRecoveryIntegrationTest` |
| **S3-15** | Cancellation & Bounded Resources | Cancellation stops native worker process cleanly; memory bounded | `RagCancellationRecoveryIntegrationTest`, `test_worker.py` |
| **S3-16** | Regression & Final Boundary | All 91 focused Java tests pass; 124 Python tests pass; static checks pass cleanly; Phase 17 frozen baselines untouched | `java-s3.json`, `pytest.json`, `ruff.json`, `format.json`, `mypy.json` |

---

## 4. Executable Verification Commands & Receipts

All verification commands were executed using the bounded runner (`scripts/run-bounded-command.py`) with receipts stored in a unique run directory.

- **Receipt Directory:** `logs/phase18/s3-dd2503569f884d0f98ce2d006a619fed`

### 4.1 Receipt Summary Table

| Receipt File | Target / Command | Exit Code | Result | Duration | Started UTC |
|---|---|---|---|---|---|
| `fr18-3-before.json` | Entry probe: `RagNativeDocumentBatchIntegrationTest` (pre-repair) | `1` | `COMMAND_FAILED` | 36.125s | 2026-10-21T06:09:40Z |
| `java-s3.json` | Java S3 Suite (91 tests across 10 classes) | `0` | `PASS` | 311.247s | 2026-10-21T07:19:51Z |
| `ruff.json` | Python Ruff linter (`rag-engine`) | `0` | `PASS` | 0.123s | 2026-10-21T07:27:45Z |
| `format.json` | Python Ruff formatter check (`rag-engine`) | `0` | `PASS` | 0.105s | 2026-10-21T07:27:45Z |
| `mypy.json` | Python Mypy static analysis (`fresh-mypy`) | `0` | `PASS` | 0.607s | 2026-10-21T07:27:45Z |
| `pytest.json` | Pytest suite (124 tests in `rag-engine`) | `0` | `PASS` | 30.537s | 2026-10-21T07:27:46Z |

### 4.2 Exact Commands Executed

```powershell
$S3Python = "rag-engine/.venv-d17/Scripts/python.exe"
$S3Runner = "scripts/run-bounded-command.py"
$S3Receipts = "logs/phase18/s3-dd2503569f884d0f98ce2d006a619fed"

# 1. Entry probe before repairs (reproduced failure)
& $S3Python $S3Runner --receipt "$S3Receipts/fr18-3-before.json" -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagNativeDocumentBatchIntegrationTest#realNativeEightSuccessTwoFailurePublishesAtomicallyAndReconcilesLostAck' test

# 2. Java S3 Suite after repairs (91 tests: 91 passed, 0 failed, 0 skipped)
& $S3Python $S3Runner --receipt "$S3Receipts/java-s3.json" -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=RagNativeDocumentBatchIntegrationTest,RagDocumentBatchIntegrationTest,RagDocumentInputCompatibilityTest,RagWorkerPostgresPrivilegeIntegrationTest,RagCommitAcknowledgementRecoveryIntegrationTest,RagSecondJvmRecoveryIntegrationTest,RagCancellationRecoveryIntegrationTest,RagSlice1SnapshotIntegrationTest,RagSnapshotCorrectiveIntegrationTest,RagSlice3*Test' test

# 3. Python Static Checks & Pytest (124 passed)
Push-Location rag-engine
& $S3Python $S3Runner --receipt "$S3Receipts/ruff.json" -- $S3Python -m ruff check .
& $S3Python $S3Runner --receipt "$S3Receipts/format.json" -- $S3Python -m ruff format --check .
& $S3Python $S3Runner --receipt "$S3Receipts/mypy.json" -- $S3Python -m mypy --cache-dir "$S3Receipts/fresh-mypy" src/ppv_rag
& $S3Python $S3Runner --receipt "$S3Receipts/pytest.json" -- $S3Python -m pytest -q tests/test_obsidian_d17.py tests/test_d17_completion.py tests/test_d17_final_corrective.py tests/test_d17_r03_only.py tests/test_optional_ocr_dependency.py tests/test_worker.py tests/test_slice3_verified_indexing.py
Pop-Location
```

---

## 5. Invariant & Boundary Preservations

1. **Target Exclusivity:** Approved immutable Obsidian Markdown `.md` snapshots ONLY. TXT, Office, PDF, and OCR remain uncertified and unused in Slice 3.
2. **State & Phase Guarantees:**
   - Phase 17 remains **FROZEN**.
   - Slice 1 (D14) and Slice 2 (D23) remain **CLOSED**.
   - Normal external indexing remains **INACTIVE** globally; live originals remain **DISABLED / DEFERRED**.
   - Finding **FR18-3** is evidenced as resolved but remains formally open until independent review.
   - Historical aggregate full-suite Java gate remains **FAILED** until a future full rerun in Slice 4.
3. **Publication Contract:** Retains **one single physical PostgreSQL publication COMMIT C_P**.
4. **Git Workflow:** Strictly **NO** `git commit`, `git push`, `git tag`, or pull requests.
