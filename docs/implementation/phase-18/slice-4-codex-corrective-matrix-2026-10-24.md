# Slice 4 corrective implementation evidence matrix - 2026-10-24

Codex is the implementer. This is prospective evidence, not independent acceptance.
The October 24 independent CHANGES_REQUESTED review and historical S4 matrix remain
unchanged. Source and command hashes are under `logs/phase18/s4-corrective-20261024/`.
`native-final-resource` executed 104 cases with zero failures/errors/skips. Counts
from overlapping selections are never added. Full clean gate completed as recorded below.

| Row | Implementation evidence on current candidate | Disposition / limits |
| --- | --- | --- |
| S4-01 | `RagSlice4NoChangeIntegrationTest.testS4_01_ValidNoChange_UnchangedBytes_PreservesAllState_ZeroWorkerCalls`; corrective `equalCaptureRecordsDurableNonpublicationWithoutJobRevisionOrGeneration`, `allUnchangedHttpAdmissionCreatesOperationWithNoIndexingLeaseOrWorker` | PASS implementation evidence: no worker/job/new R/G/version; separate durable C_N operation. |
| S4-02 | Corrective `changedChildCannotQualifyEqualParentAsNoChange`, `preparedNoChangeCannotOutliveExactChildAuthority` (4 faults), `mixedPublicationRechecksEveryUnchangedProofAfterTrustedReplay` (2), `omittedPendingRootIsRejectedBeforeAnyWorkerDispatch`, `reorderedTasksCannotReplaceImmutableWorkerPlan`; original NoChange negative controls | PASS executed denials; current source/child/G/pipeline and selected/pending proof required, no arbitrary omission. |
| S4-03 | `RagSlice4ReindexIntegrationTest.testS4_03_NativeExplicitSameRReindex_WithoutTouchingOriginal`; resource `actualColdWarmRootsApprovedNestedChildrenAndSameRevisionReindex` | PASS real native reindex with approved nested children after originals deleted. |
| S4-04 | Reindex failure test; corrective `failedReindexHttpReportNamesTheIndependentlyEligibleRetainedGeneration`; S3 `multiRootPhysicalRollbackPreservesPreviouslySelectedRevisionAndIndependentGeneration` (5 faults), norm-preserving vector forgeries (3) | PASS zero common-failure publication/promotion; eligible prior G retained. |
| S4-05 | Lifecycle eligibility/tamper test, corrective child/G/pipeline negatives, accepted S3 generation/restart/typed-identity tests in full default selection | PASS focused and final full default selection. No TTL/historical success shortcut. |
| S4-06 | NoChange original-edit/deletion test; native resource reindex after deleting all originals; authenticated mixed report field checks | PASS RAG_SNAPSHOT / UNVERIFIED, six qualification fields reach production HTTP boundary. |
| S4-07 | Corrective `noChangeAndRevokeRespectBothDurableOrders` (2), `transitiveRevokePurgesParentContentAndPreservesIndependentSibling`, `failedTransitiveErasureRemainsDeniedAndRetriesWithoutPurgingSibling`; existing cancellation/revoke/recovery selection | Focused PASS for guarded ordering, immediate denial and bounded safe erasure retry; no assertion of instantaneous erasure. |
| S4-08 | Corrective `ordinaryRestoreDirectionsNeverReapproveOrAdoptOldRevision` (2); snapshot recovery and exact-child CAS negatives | PASS controlled DB row-image restore versus missing store, and restored files versus revoked DB; fresh explicit source/revision approval required. Not physical server/PITR evidence. Administrative row-image changes are controlled fault fixtures, not authorized concurrent business operations. |
| S4-09 | S3 actual transfer/CPU/staging/validator cancellation and original ordered CANCELLED tests; corrective C_N cancellation at finish/commit (2), new-JVM C_N live-owner refusal and dead-owner recovery | Focused PASS; PID + creation identity, writer/validator stop and reservation safety. Broader restart/default selection included in final full gate. |
| S4-10 | `RagSlice3ConnectionLossIntegrationTest` (2 actual C_P cuts); corrective `actualNoChangeWireLossReconcilesCommittedAndUncommittedOutcomes` (2), deferred physical C_N rollback, fresh controller halt/recovery | PASS real connection cuts before commit/after DB commit and DB unavailable retention. No callback-only substitute. |
| S4-11 | Corrective authenticated mixed SUCCESS/FAILURE/NO_CHANGE, all-unchanged operation report, CANCELLED/ABORTED/RECOVERY_REQUIRED roster (3), failed-reindex retained G; original 8/2 and all-failed batch cases in default suite | Focused PASS; default 8/2/all-failed cases included in final full selection. |
| S4-12 | Snapshot corrective 42 cases, C_N temp cancellation, selective erasure failure/retry, S3 managed verifier disk failure; resource observer no owned survivors/capture temps | PASS controlled I/O/cleanup/recovery evidence. Disk-full-equivalent injected I/O is not physical host disk exhaustion. |
| S4-13 | Preserved 17 deletions; `cleanup-plan.json`; residual locator/obsolete CLI removal; new cleanup tests; final dependency/import inventory | PASS 28 focused Python invocations and full pytest 238. DOMAIN TextParser and exact inert OCR tuple retained. |
| S4-14 | Offline uv sync removed 15 outside-lock packages, zero installs/upgrades; 52 installed distributions match cleaned lock; final artifact integrity and lock checks; typed MD/DOMAIN/cross-type native cases | PASS selected Python 3.14.4, pinned MiniLM/tokenizer. No external model/package fetch or excluded-format restoration. |
| S4-15 | `native-final-resource-resources.json`, source-bound resource-workloads/native/C_N/HTTP bindings. Cold/warm shared session, mixed roots, nested children, same-R reindex, cancellation/failure, 97.37% prepared-byte ceiling | CPU/native MEASURED. GPU_EVIDENCE_BLOCKED / OWNER_DECISION_REQUIRED: CPU/Azure providers only; no genuine CUDA inference/fallback or attributable worker VRAM. OS Java working set is not JVM heap. Sampling gaps remain explicit. |
| S4-16 | Preserved independent pool-capacity observations; unchanged reviewed test-owned max5/min1/idle10s settings; full `clean verify` | BLOCKED - actual aggregate exit 1: {'tests': 1567, 'failures': 1, 'errors': 0, 'skipped': 1}. Original 1433/1/2/0 and reported1549 vs observed1536 discrepancy remain historical FAILED/unknown cause. |
| S4-17 | Final Ruff, format, fresh-cache mypy PASS; full pytest 238 PASS; exact Maven clean verify | FAILED Maven exit 1, {'tests': 1567, 'failures': 1, 'errors': 0, 'skipped': 1}; first pytest output-path launch failed before collection, preserved, corrected quoting only. |
| S4-18 | New handback/matrix/machine inventory, exact source/deletion/receipt/preservation hashes and six focused summaries | INDEPENDENT_REVIEW_REQUIRED / OWNER_DECISION_REQUIRED. Implementer cannot accept Slice 4 or Phase 18. |

AC/FCNC/IS applicability follows the unchanged executable test plan: OCR/PDF/Office
positive geometry subcases N/A_BY_D24, not PASS; native four-script Markdown,
source/child/locator fidelity, typed DOMAIN/v2, SQL, snapshot/physical transactions,
recovery, resources and reporting remain mandatory. Hostile-alias lifetime method
alone is N/A_BY_ADR0023 by explicit Owner authorization; original assertion/failed
opt-in diagnostic retained. GPU remains required and unmet. Historical D22 originals
NOT_RECOVERED. External indexing INACTIVE, originals DISABLED/DEFERRED; Slices 1-3
and FR18-3 CLOSED in their accepted scope; Slice 4/P18-FINAL NOT ACCEPTED.

Final mandatory gate blocker: unchanged frozen Phase 17 query-budget case expected 5 prepared statements, observed 6 (final-java-v3). Exact extra-statement origin NOT_ISOLATED. No waiver or fourth unchanged rerun. S4-16/17 and full-gate portions of F05/F07 remain BLOCKED; GPU separately OWNER_DECISION_REQUIRED.

Current aggregate counts: Maven stdout1580/1/0/1; fresh archived XML1567/1/0/1. Difference13 cause UNKNOWN. Final CPU-fixture profile1/0/0/0 passes on exact final source; see final-cpu-resource. Full Java gate remains FAILED, not waived.
