# Phase 18 RAG API and worker contract — additive *proposed* inventory

**Status:** Owner-approved design **D18-A**, not implemented or an authorized handoff. [ADR-0021](../adr/0021-phase-18-bounded-multi-file-verified-indexing.md) controls scope; [ADR-0020](../adr/0020-phase-17-single-job-reservations-e1.md) controls frozen Phase 17 production. This is the additive canonical Phase 18 API/worker inventory for Codex pre-handoff; names/shapes below are **proposed**, not claims of existing Java/Python types.

## Existing interfaces preserved

- `RagOperations` single-root admission and current single-root version-2 manifest/worker digest receipt remain compatible and gated by existing limits, grants, source guard and endpoint shape. Do not break existing callers/tests or rewrite version 2.
- Existing `rag/api`, `rag/source`, module-local `rag/internal/web` adapter and `ppv_rag` are extension points; no new cross-module Java edge, shared HTTP envelope, controller outside owner module or Python business-SQL permission.
- Existing root `ApiResponse` JSON success/error handling and module-local DTO validation remain; no arbitrary raw file paths in public job/report DTOs.

## Mixed-source lock compatibility (binding)

All batch admission, consent/revocation and trusted finalization paths take only the applicable subset of the **Phase 17-compatible source-first order** in [ADR-0021 clause 2](../adr/0021-phase-18-bounded-multi-file-verified-indexing.md): short graph barrier (where needed) -> sorted Vault source-state guards -> sorted RAG-owned external-file guards -> Vault lifecycle -> owner projection/assignment -> sorted RAG policy -> job/attempt -> generation/staging/run. Never lock policy/job/attempt before a required source guard. Workers use only attempt -> staging; the attempt-only stop-request transaction ends before source/process actions. Later-row-trigger paths retain the existing NOWAIT backstop; no hash/OCR/model/HTTP/process wait under SQL row locks. No changed Phase 17 lock ordering is authorized.

## Proposed additive typed operations (names provisional for source code, semantics binding)

| Boundary | Typed shape / operation | Required invariants |
| --- | --- | --- |
| Java RAG application | `admitDocumentBatch(DocumentBatchCommand) -> JobReceipt` | Explicit ordered non-empty finite list of individually approved typed root references, expected source/version/consent/field-policy proofs; CAS and complete independently approved dependency closures; atomic union reservation and one job-scoped worker. Reject stale/duplicate/mismatched roots. No raw/path-only authority. |
| Java RAG application | `documentBatchReport(jobId) -> DocumentBatchReport` | Job `COMPLETED`/`ABORTED`/`CANCELLED`/`RECOVERY_REQUIRED` and stable sanitized per-root outcomes with source identity alias, warning/error codes, chunk/page counts, model/engine hash, resource peaks, generation IDs; compute current index validity **at query time** instead of trusting historical success. |
| HTTP (RAG module-local) | Add typed batch admission and report adapters under existing `/api/v1` RAG module convention | Existing standard envelope, bearer checks, strict DTO validation, bounded list lengths, sanitized conflict/denial errors; exact route names must be checked against existing RAG controller to avoid collision before implementation. No external URL, directory-wide autoapproval or new auth bypass. |
| Java -> Python worker | `DocumentWorkerEnvelope(v3, jobId, attemptId, issuedGrant, selectedApprovedInputs, format/language/options, policy/model/version hashes)` | v3 is **new** and discriminated from unchanged v2 digest-only payload. Transfer only Java-verified bounded bytes + independently approved child attribution; no uncontrolled worker file refetch; preserve single-use grant/worker process/ingress bounds. |
| Python -> restricted SQL staging | `PerFileStagedResult(rootKey, success|failed, reasonCode, contentBlocks, chunks, embeddings, typedLocators, dependencyManifest, resourceReport)` | Typed length/size/cardinality/vector dimension/finite value validation, worker provenance and COMMIT fence; non-success has no candidate chunks/vectors. Stage via restricted audited functions only. SQL stored payload sensitive. |
| Trusted Java finalizer | `finalizeVerifiedBatch(jobId, attemptProof)` | After positive native stop, writer seal/drain and exact eligibility verification, one physical COMMIT for outcomes, **all verified successful root generations or none**, job terminal and exact union release. Shared failure -> zero publication; unknown -> retained reservations/reconciliation. |

## Core document/result concepts

- `TypedSourceKey = (sourceKind, stableId)` with explicit variant `VAULT` vs `EXTERNAL_FILE`; external file never occupies an arbitrary Vault ID slot. `SourceApproval` binds exact source version/reset, authorization epoch, field policy, approved body/metadata and dependency identity. Reservation union is independent of per-generation closure.
- `StructuredElement` carries `sourceKey`, `sourceVersion`, structural kind, text/table cell relationships, heading context, and **typed mandatory provenance**. `Chunk` is a bounded group of elements under one structure-first tokenizer policy; transcluded child source remains attributed to **child**, not fabricated as parent text.
- `DocumentGeneration` binds one root plus exact actual approved dependency closure, content/vector rows, model artifact and dimension, tokenizer/parser/OCR/chunker versions. `CURRENT` is independently rechecked; changing unrelated B never revokes A.
- Report-only `PARTIAL_SUCCESS = job.status == COMPLETED AND >=1 root success AND >=1 root failure`; all-success also `COMPLETED`, zero valid publish `ABORTED`, cancellation `CANCELLED`, unknown process/SQL `RECOVERY_REQUIRED` (**nonterminal**). No DB `PARTIAL_SUCCESS` status and no assumed `SUCCESS`/`FAILED` DB states.
- Historical job outcomes never substitute for a live `CURRENT` predicate. File errors may be isolated; shared worker/trust/final-C failures cannot.

## Versioning/migration and compatibility proof

- Version v3 input/output/staging is a **proposed protocol label**, not an existing schema or transport implementation. Exactly preserve v2 single-root Foundation behavior and evidence on untouched paths; version/receipt dispatch must reject unknown or downgraded mixed structures. No raw path substitution for tagged source IDs.
- Actual tables/SQL functions/role grants must be compared to installed V3–V17 before adding **V18+** migrations. [Phase 18 DBML](../database/phase-18-document-indexing.dbml) describes logical responsibilities, not final column names or permission-validated SQL. No edits to frozen Schema v1 / V1–V17.
- Focused compatibility tests: old single-root v2 grant, new multi-root v3 attempt, unknown/replayed grant, denied worker SELECT/DML, mixed source guard ordering, unrelated/current vs dependent/revoked generation, all-success/mixed/all-fail/cancel/unknown-COMMIT and strict HTTP envelope.
