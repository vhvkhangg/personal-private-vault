**RR-01-03 checkpoint, 2026-10-22:** trusted replay now independently computes
pinned-model embeddings for source-derived chunks and compares components in
chunk order. Mixed DOMAIN/external-snapshot closures deny admission before
worker dispatch; independent mixed roots remain supported. Explicit cancellation
intent uses existing internal run metrics before competing teardown. No public
wire/schema/status changes. Final verification passes; implementation disposition is READY_FOR_INDEPENDENT_SLICE_3_RE_REVIEW; see the
[RR handback](../implementation/phase-18/slice-3-rr-corrective-handback-2026-10-22.md).
FR18-3 remains OPEN and external indexing INACTIVE. Earlier candidate notices
below are preserved history.

**D24 CURRENT - 2026-10-21:** Overall Phase 18 external document target is **Obsidian Markdown `.md` ONLY**. Owner authorizes the new initial **Slice 3 Verified Indexing D+F**; see [D24](../implementation/phase-18/owner-decisions.md#2026-10-21--d24-overall-markdown-only-phase-18-and-slice-3-verified-indexing), [executable handoff](../implementation/phase-18/slice-3-verified-indexing-handoff.md), [baseline/applicability/cleanup](../implementation/phase-18/slice-3-markdown-scope-and-baseline-2026-10-21.md). Slice 2/D23 producer-local scope is CLOSED and prior work is **published** at verified main `ef32d41b33dd00e5fa08cf63977e91b8936ef3d5`, including unaccepted mixed changes; publication does not accept them. The former selective staging/commit workflow is superseded and must not be reopened. TXT/Office/PDF/OCR are excluded from Slice 3 and final Markdown acceptance; temporarily retain without use/certification, then remove unnecessary implementation/dependencies/tests/fixtures once at Slice 4 end before P18-FINAL. No Slice 4 implementation now. Phase 17 FROZEN; Slice 1/D14 CLOSED; D22 originals NOT_RECOVERED / original preservation FAILED; FR18-3 OPEN; Java publication NOT ACCEPTED; historical full Java gate FAILED until genuine successful rerun; indexing INACTIVE; live originals DISABLED/DEFERRED; P18-FINAL NOT ACCEPTED. Historical Antigravity remediation 2/2 remains exhausted; this initial slice grants no extra remediation rounds. Preparation only in this session; no activation or agent publishing.

## Preserved pre-D24 status and contract history

> **D21 Slice 2 applicability, 2026-10-20:** The Owner-approved current Slice 2 acceptance scope is Obsidian Markdown `.md` only; TXT/DOCX/PDF/XLS/XLSX/OCR are excluded from that slice and require a future explicit Owner Decision for reintroduction. See the [scope/matrix](../implementation/phase-18/slice-2-markdown-only-d21-scope-and-acceptance.md). The original broader Phase 18 clauses below remain unaccepted reference obligations; this notice does not silently delete or pass them. Precise conflicts and higher-level qualification are [ACCEPTED in ADR-0024 under Owner D22](../adr/0024-phase-18-slice-profile-acceptance-boundaries.md), strictly for D21 Slice 2 applicability, not as an overall Phase 18 rebase. Phase 17/Slice 1 and Java/FR18-3/P18-FINAL guarantees are unchanged.

# Phase 18 RAG API and worker contract — current inventory and approved target

**Current implementation, 2026-10-22:** Slice 1/D14 and Slice 2/D23 remain CLOSED. The additional finite Slice 3 corrective checkpoint is **READY_FOR_INDEPENDENT_SLICE_3_RE_REVIEW**, not accepted. See [ACTIVE](../implementation/handoffs/ACTIVE.md) and the [final handback](../implementation/phase-18/slice-3-final-corrective-handback-2026-10-22.md). External indexing remains INACTIVE, live originals DISABLED/DEFERRED; ADR-0020/Vault/v2 remains frozen and ADR-0022/23/24/D24 retain their scoped applicability.

### Slice 3 corrective candidate wire inventory — not accepted

The final additional Owner-authorized checkpoint is recorded in the
[2026-10-22 handback](../implementation/phase-18/slice-3-final-corrective-handback-2026-10-22.md).
Newly captured manifests may carry `snapshot.target_digests`: a sorted unique
list of at most 512 SHA-256 lexical suffix fingerprints, bound to the captured
name through the existing `sources.file_identity`. No original path is exposed.
An empty legacy list is omitted from serialized input so legacy DOMAIN/v3 digest
identity is retained. Basename-only historical snapshots are not silently given
unrecorded folder names. Pending updates inherit the existing naming proof.
Each individually approved child is transported once by UUID; the existing D17
source tree resolves literal aliases and authenticated relative targets, then
applies heading/block selection. Unknown/ambiguous names fail when requested;
unselected sections do not authorize or trigger additional resolution.
No route, schema version or public status changes in this checkpoint.

The existing `3.0` document envelope now explicitly negotiates external snapshots
through `root.snapshot`: `mode=RAG_SNAPSHOT`, `consent_mode=WHOLE_FILE`, external
source UUID, revision UUID, captured source name, byte length/SHA-256, expected
selected-R UUID, sourceVersion, reset, authorization epoch and field-policy version.
Every approved child field has its own matching snapshot tuple. Java constructs
the bodies and approved child bindings; caller body replacement remains denied.
No original path is sent to Python. Legacy external/v2 admission is denied;
Foundation v2 and DOMAIN v3 serialization/digests remain compatible.

Worker staged resources retain the root/child `input_binding`; the result receipt
returns the protected canonical actual-result digest. V22 seals exact ordered
root outcomes and ordered actual chunks/metadata/vectors, separately binding input,
scope and attempt. Java-owned accepted-D17 source replay precedes publication.
Generation JSON retains both `input_manifest_text` and post-selection `manifest_text`,
plus `actual_result_digest`. No new HTTP route or public job status is introduced.
The [final executed matrix](../implementation/phase-18/slice-3-final-corrective-matrix-2026-10-22.md)
records current implementation proof, retained failures and sampling limitations; independent review is pending.

## Approved external snapshot target — NOT IMPLEMENTED / NOT_VERIFIED

| Boundary | Binding target contract |
| --- | --- |
| Source selection / grant | Explicit discriminated RAG_SNAPSHOT / WHOLE_FILE logical mode, finite authenticated import/update intent, target S or first import, approved original locator and practical ordinary-file/path checks (not native NTFS ancestor/ABA identity proof), expected selection/sourceVersion/reset/authorization/policy. One complete-file action; Java binds exact ORIGINAL_BYTES SHA-256/length/R at seal. No field-safe projection/second content approval. Unknown/legacy consent mode denies. No HTTP route or wire version is selected here. |
| Snapshot / revision | O provenance, logical S, application-level no-overwrite ORIGINAL_BYTES R/ordinal with SHA-256/length verification (not same-user OS anti-tamper), selected versus pending R, grant/reset and necessary integrity-denial history remain distinct. New R selection/sourceVersion/G promotion occurs at actual C_P only. Identical imports stay distinct; no cross-source deduplication or hash-derived source identity. |
| Update / NO_CHANGE | Manual application-managed exact-byte SHA-256/length comparison against independently verified selected ORIGINAL_BYTES; every ADR-0022 source/authority/child/selection/pipeline/integrity predicate required. NO_CHANGE is an operation/per-file disposition, no R/ordinal/version/G/consent/selection change, pipeline/worker or new indexing job status. Same-R reindex uses R only, with real producer/new G at C_P. |
| Operation / worker plan | Immutable full root roster/full exact reservation union, including unchanged members, retained until known safe finish. Java alone proves NO_CHANGE; worker plan contains only changed/reindex members. All-NO_CHANGE uses C_N without worker/zero-generation COMPLETED. Mixed operation uses one C_P, no early unchanged release. |
| Input / staged result | Explicit negotiated snapshot protocol mode, whole-file scope/structural roles, S/R/epochs/incident/provenance/original digest+length/closure and exact producer tuple from ADR-0022. Input digest and canonical actual outcome/text/excerpt/locator/vector/pipeline digest remain separate. Never reinterpret old 3.0 or missing version as whole-file. FR18-3 verification of **real** worker-produced text/excerpts/locators/model/vectors, PostgreSQL role least privilege and staging/through-C writer fencing remain mandatory. Python runs under the same Windows user; no Restricted Token filesystem confinement is claimed. |
| Locators | Exact original span/line, DOCX address, PDF page/orientation/box/table-cell geometry and spreadsheet sheet/A1/merge proof remain. Narrow original container-part/property-key/object/comment-ID locator for genuinely unlocated internal material, with applicable anchor/S/R/original-byte digest; no fictional page/cell. See [oracle](../implementation/phase-18/held-out-quality-and-locators.md). |
| Reporting | Logical target concepts include `updateDisposition=NO_CHANGE`, `publishedNewGeneration=false`, selected/pending R, retained versus new G, revision ordinal, `freshnessBasis=RAG_SNAPSHOT`, `snapshotIndexState=SNAPSHOT_CURRENT` or effective denied/outdated/absent state and `originalFreshness=UNVERIFIED`. These are documentation concepts, **not existing fields**. Historical success never authorizes reads. |
| Incident / revoke / cleanup | Minimal necessary durable integrity/authority-denial state using existing Phase 17 mechanisms where possible, actual revokeOutcome (NONE/PENDING/BUSY/FAILED/COMMITTED/UNKNOWN), controlled snapshot/temp purge, databaseHistoricalCopies/externalCopyKnowledge and safe owner actions. C_D denial is UNAVAILABLE, not committed REVOKED before C_R; unknown outcomes are not confirmed NO_CHANGE/publication/erasure. Controlled active-copy completion can coexist with explicitly unknown backups. No private paths/denied names/sensitive digests/content in public reports/logs. |
| Runtime / compatibility | Existing shared ApiResponse/module-local advice, source-first order/NOWAIT, one-use process/writer fences, v2 and Foundation limits remain. G1–G4 and FR18-1–11 remain OPEN/NOT_VERIFIED under ADR-0023 practical acceptance; maintain actual guarded NO_CHANGE/revoke/publication/unknown-outcome recovery, not a new C_D/C_N transaction platform. Only the bounded Slice 1 corrective scope is authorized; no activation. |

The following inventory records inherited implemented DOMAIN/v3 shapes. Its live CURRENT/field policies describe that current path, **not** the approved external snapshot target. In particular the existing finalizer has partial checks, not accepted exact producer verification; its input-derived expected output remains FR18-3. See the [confirmed mismatch ledger](../implementation/phase-18/canonical-synchronization-2026-10-18.md#confirmed-current-implementation-mismatches).

## Exposed Slice 1 snapshot routes ? corrective implementation awaiting independent re-review

These authenticated routes use the existing module-local controller, DTOs and `ApiResponse`.
They expose pending custody/approval only; indexing remains inactive. The inventory below
separates these implemented shapes from later target contracts above.

| Route | Actual Slice 1 request and result |
| --- | --- |
| `POST /api/v1/rag/sources/register-file` | `filePath`: one absolute server-local regular document on a supported local filesystem. First import only; an existing locator denies instead of silently updating. Concurrent imports serialize their final first-import check with the existing graph barrier. Returns DENIED pending R and `snapshot` preview. No client-upload or implicit whole-directory grant. |
| `GET /api/v1/rag/sources/{id}` / list | `snapshot` contains sourceVersion/reset, policy authorizationEpoch/fieldPolicyVersion, fileAuthorizationEpoch/filePolicyVersion, selectedRevisionId, pendingRevisionId, pendingSha256 and pendingByteLength. The exact authenticated preview is echoed by update/approval; no original locator is returned. |
| `POST /api/v1/rag/sources/{id}/capture-revision` | `{filePath, expectedSnapshot}` (`RagSnapshotCaptureCommand` / `CaptureSnapshotRequest`). The entire preview must still match under graph/source/policy guards. No server-current substitution. Valid equal bytes reuse a verified existing R internally; changed bytes create pending R. No selected-R/sourceVersion/G advance and no final NO_CHANGE enum. Unversioned programmatic calls deny. |
| `POST /api/v1/rag/sources/{id}/approve` | Existing consent-version fields and `approvedFields=["content"]`, plus required external `expectedSnapshot`. Pending identity/digest/length and captured consent context must match before and after integrity verification; approval records `approved_pending_revision_id`. Domain callers keep the existing six-argument command and need no snapshot tuple. |
| `POST /api/v1/rag/sources/{id}/revoke` | Existing guarded synchronous authority denial/tombstone. Physical snapshot retirement starts after commit; failed deletion remains in managed inventory and is retried by existing startup/scheduled recovery. REVOKED is an authority result, not proof of erasure from backups or inaccessible storage. |

The existing `sourceRevisionHash` is the registered/approved policy hash; `snapshot.pendingSha256` identifies the exact pending R awaiting approval. Capture does not promote that pending hash into selected/current state.

Acquisition has a configured 60-second deadline (`ppv.rag.snapshot-capture-timeout-seconds`,
allowed 1?120), checked during timed asynchronous local reads and before seal/metadata commit.
Thread interruption cancels; committed revoke cancels active source captures. The existing ordinary local-drive check also rejects mapped/unverified drives; it does not invoke R7 acquisition. No capture job,
worker, queue or independent scheduler is added. Temp/sealed candidates are excluded while
active in this application. Recovery inspects at most 25 filesystem entries and 25 metadata
rows per tick, rechecks exact authoritative references under the existing graph lock, and
never adopts an orphan or rereads an original. Known retired revisions receive the same
reference check immediately after commit. Snapshot metadata changes and recovery deletion/denial recheck the existing current-runtime authority under that same graph lock, so a replaced Java controller cannot finish or erase a new controller's in-flight candidates. SQL uncertainty defers deletion until a fresh
ordered read succeeds. Missing/corrupt registered R persists an integrity denial.

Errors and DEBUG logs use safe status/capture IDs, never original paths or stable sensitive
digests. Candidate private digests are returned only in the authenticated exact-consent
preview; they are not emitted in routine logs/errors/reports. Slices 2?4, FR18-3 and P18-FINAL
remain open/deferred. This inventory is not independent acceptance.

## Existing interfaces preserved

- `RagOperations` single-root admission and current single-root version-2 manifest/worker digest receipt remain compatible and gated by existing limits, grants, source guard and endpoint shape. Do not break existing callers/tests or rewrite version 2.
- Existing `rag/api`, `rag/source`, module-local `rag/internal/web` adapter and `ppv_rag` are extension points; no new cross-module Java edge, shared HTTP envelope, controller outside owner module or Python business-SQL permission.
- Existing root `ApiResponse` JSON success/error handling and module-local DTO validation remain; no arbitrary raw file paths in public job/report DTOs.

## Mixed-source lock compatibility (binding)

All batch admission, consent/revocation and trusted finalization paths take only the applicable subset of the **Phase 17-compatible source-first order** in [ADR-0021 clause 2](../adr/0021-phase-18-bounded-multi-file-verified-indexing.md): short graph barrier (where needed) -> sorted Vault source-state guards -> sorted RAG-owned external-file guards -> Vault lifecycle -> owner projection/assignment -> sorted RAG policy -> job/attempt -> generation/staging/run. Never lock policy/job/attempt before a required source guard. Workers use only attempt -> staging; the attempt-only stop-request transaction ends before source/process actions. Later-row-trigger paths retain the existing NOWAIT backstop; no hash/OCR/model/HTTP/process wait under SQL row locks. No changed Phase 17 lock ordering is authorized.

## Implemented additive typed operations

| Boundary | Typed shape / operation | Implemented Java / Python Types & Invariants |
| --- | --- | --- |
| Java RAG application | `admitDocumentBatch(RagDocumentBatchCommand) -> RagJobAdmissionResult` | `rag.api.RagOperations`: exact non-null source/epoch/field CAS and approved field set; domain projections prepared before guards, complete per-root closures reserved as one union. External preparation still rejects. |
| Java RAG application | `executeDocumentBatch(jobId, attemptId, tasks)` | Programmatic execution only; no new HTTP execute route. Current implemented task selection format is `DOMAIN`: source ID, exact field set and root membership must match admission. Caller text/base64/transclusions reject. Java constructs `prepared_fields` from owner projections with actual source keys, fields and versions. This does not implement external-file extraction authority. |
| Java RAG application | `getDocumentBatchReport(jobId) -> RagDocumentBatchReport` | Returns job status (`COMPLETED`/`ABORTED`/`CANCELLED`/`RECOVERY_REQUIRED`), summary outcome (`PARTIAL_SUCCESS`/`COMPLETED`/`ABORTED`), and stable sanitized per-root `RagFileOutcomeView` with source identity alias, warning/error codes, chunk/page counts, model/engine hash, resource peaks, generation IDs; computes current index validity (`CURRENT`/`REVOKED`/`OUTDATED`/`UNINDEXED`) **at query time**. |
| HTTP (RAG module-local) | Batch admission, report, and finalization endpoints under `/api/v1/rag/jobs` | `RagJobController`: `POST /api/v1/rag/jobs/admit-batch` (batch admission), `GET /api/v1/rag/jobs/{id}/document-report` (batch report), and `POST /api/v1/rag/jobs/{id}/finalize-batch` (batch finalization). Standard `ApiResponse` envelope, bearer authentication, `@Valid` DTO validation, bounded list lengths, sanitized conflict/denial errors. No raw external file paths exposed. |
| Java -> Python worker | `DocumentWorkerEnvelope(v3, jobId, attemptId, issuedGrant, selectedApprovedInputs, format/language/options, policy/model/version hashes)` | `DOCUMENT_V3` protocol discriminated from unchanged v2 digest-only payload in `contracts.py` and `transport.py`. Worker consumes verified bytes + approved metadata over loopback HTTP with bearer grant and identity verification. |
| Python -> restricted SQL staging | `stage_document_result(jobId, attemptId, rootKey, success, reasonCode, warnings, chunks, resourceReport)` | REST-to-SQL staging via restricted function `rag.stage_document_result()`, enforced by `trg_staged_document_outputs_lease_fence` commit stage deadline. Staged chunks validate 384-dimensional finite embeddings and structured locators. Non-success roots stage 0 chunks. |
| Trusted Java finalizer | `finalizeDocumentBatch(jobId, attemptId, command) -> RagBatchFinalizationResult` | `RagDocumentFinalizationGate`: Verifies positive native worker stop (`proveStopped`), consumed `DOCUMENT_V3` grant, and lease validity. Executes in single physical transaction with strict lock ordering: creates index generations, generation roots, and document chunks with native vector embeddings, commits durable file outcomes, and atomically releases all Vault/external file source reservations. |

## Core document/result concepts

- `TypedSourceKey = (sourceKind, stableId)` with explicit variant `VAULT` vs `EXTERNAL_FILE`; external file never occupies an arbitrary Vault ID slot. `SourceApproval` binds exact source version/reset, authorization epoch, field policy, approved body/metadata and dependency identity. Reservation union is independent of per-generation closure.
- `StructuredElement` carries `sourceKey`, `sourceVersion`, structural kind, text/table cell relationships, heading context, and **typed mandatory provenance**. `Chunk` is a bounded group of elements under one structure-first tokenizer policy; transcluded child source remains attributed to **child**, not fabricated as parent text.
- `DocumentGeneration` binds one root plus exact actual approved dependency closure, content/vector rows, model artifact and dimension, tokenizer/parser/OCR/chunker versions. `CURRENT` is independently rechecked; changing unrelated B never revokes A.
- Report-only `PARTIAL_SUCCESS = job.status == COMPLETED AND >=1 root success AND >=1 root failure`; all-success also `COMPLETED`, zero valid publish `ABORTED`, cancellation `CANCELLED`, unknown process/SQL `RECOVERY_REQUIRED` (**nonterminal**). No DB `PARTIAL_SUCCESS` status and no assumed `SUCCESS`/`FAILED` DB states.
- Historical job outcomes never substitute for a live `CURRENT` predicate. File errors may be isolated; shared worker/trust/final-C failures cannot.

## Versioning/migration and compatibility proof

- V3 is implemented for the contained domain-projection experiment. Input digest encoding includes every typed field/default/null, recursively sorted object keys, original array order, compact UTF-8 JSON without ASCII escaping or BOM. `prepared_fields` entries carry `source_key`, `source_version`, `approved_field`, `text`; transclusion values on the Python boundary require the same typed grant rather than invented version 1. Native Java/Python compatibility and 8-success/2-failure publication are tested. Exact result/receipt/excerpt verification and the complete R7/quality/resource gates remain incomplete.
- V19 requires actual pgvector storage and the BOM-aligned Hibernate vector mapping. Terminal v3 attempts may scrub their union after grant deletion while remaining active generations retain their own exact manifests; v2 retains its original no-active-generation scrub rule. No edits to V1–V18 were needed by takeover.
- Actual tables/SQL functions/role grants must be compared to installed V3–V17 before adding **V18+** migrations. [Phase 18 DBML](../database/phase-18-document-indexing.dbml) describes logical responsibilities, not final column names or permission-validated SQL. No edits to frozen Schema v1 / V1–V17.
- Focused compatibility tests: old single-root v2 grant, new multi-root v3 attempt, unknown/replayed grant, denied worker SELECT/DML, mixed source guard ordering, unrelated/current vs dependent/revoked generation, all-success/mixed/all-fail/cancel/unknown-COMMIT and strict HTTP envelope.
