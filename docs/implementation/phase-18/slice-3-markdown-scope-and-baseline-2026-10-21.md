## D26 prospective CPU-only applicability - 2026-10-25

**OWNER_APPROVED scope amendment; final acceptance NOT GRANTED.** [D26](owner-decisions.md#d26-cpu-only)
selects pinned offline CPU inference ONLY. Actual CUDA inference, GPU OOM-to-CPU fallback
and process-attributed GPU VRAM remain **NOT VERIFIED** and are
**NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE**, never PASS. See the
[exact execution profile and reconciliation](cpu-only-d26-applicability-2026-10-25.md).

**Affected rows: AC-05/06/07/08 retained column and Slice 4 resource boundary.** D24 remains the overall .md-only format decision. The later D26 changes only GPU/same-model GPU fallback and process-VRAM acceptance portions: retain the same model, 384-vector space, CPU resource/cancellation/reporting, authority, actual publication and lifecycle/final gates. This additive note does not rewrite D24 or the accepted Slice 3 baseline.

This is prospective acceptance applicability only. Unaffected Markdown/authority/publication,
CPU/RAM/disk/deadline/cancellation/security and five-command requirements remain mandatory.
Future GPU enablement requires separate Owner authorization and independent verification.
No source/runtime/test/model/lock/runner change, activation or closeout follows. Original
GPU_EVIDENCE_BLOCKED and failed/timeout evidence remains unchanged historical truth.
Slice 4/P18-FINAL/Phase 18 remain NOT ACCEPTED / NOT CLOSED; indexing INACTIVE.

## Preserved pre-D26 text - unaffected rules remain effective

# Phase 18 Slice 3 — published baseline and prospective Markdown scope

**Authority:** Owner D24, 2026-10-21. Preparation only; no runtime acceptance or activation.

## Verified repository baseline

Read-only checks on 2026-10-21: `git status --short` and `git diff --stat` empty;
branch `main`; `git rev-parse HEAD`, `git rev-parse origin/main` and live
`git ls-remote origin refs/heads/main` all returned
**`ef32d41b33dd00e5fa08cf63977e91b8936ef3d5`**.
The two published Slice 2 commits are `5b4df3862a78f5a62111cf8e22eb24fb2d023edd`
and `ef32d41b33dd00e5fa08cf63977e91b8936ef3d5`, both titled
`feat(rag): complete Phase 18 Slice 2 Obsidian Markdown ingestion`.
These Git titles are publication facts, not acceptance findings. This preparation's
documentation edits are subsequent uncommitted changes. Recheck the baseline at implementation entry.

The Owner confirms the published tree includes historically unaccepted changes.
D23's selective staging instructions are superseded as future delivery actions;
do not reconstruct, stage or reopen that workflow. The
[last independent delivery review](reviews/2026-10-21-slice-2-d22-01-independent-final-closeout-re-review.md)
certified an isolated 88-file/34-result candidate against `c3b4635`, not this entire
published tree. Its historical findings, hashes and limits remain unchanged.

A read-only [34-result hash comparison](slice-3-inherited-source-comparison-2026-10-21.json)
found **31 exact matches and 3 differing mixed files**: `ocr/engine.py`,
`parsers/models.py`, `limits.py`. No candidate was reconstructed or staged. Their
additional published hunks remain unaccepted; shared changes need inspection rather
than assuming the selective review covers the complete file. This comparison does not
rerun or extend the original acceptance evidence.

## Inherited source classification

Paths below are relative to the repository; Java application names are under
`backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/`.

| Classification | Actual source / evidence | Slice 3 treatment |
| --- | --- | --- |
| Frozen accepted Foundation | Phase 17 closeout; legacy v2, reservations, E1, worker supervision and SQL fences | Reuse; preserve behavior and module/public API boundaries. No new Foundation features. |
| Closed bounded Slice 1/D14 | `RagSnapshotAcquisitionService`, `infrastructure/storage/RagOriginalBytesStore`, `RagExternalFileSource`, snapshot integration/corrective tests; V20/V21 | Reuse pending custody, approval and integrity. Existing `promotePendingToSelected` is available behavior, not proof of trusted publication. D14 does not waive new gates. |
| Closed producer-local Slice 2/D23 | Python Markdown/Obsidian/HTML parser, chunker, compact metadata, pinned model; D17/D20 and independent final review | Reuse the reviewed D17 profile. Inspect published shared-file differences against the review before crediting identity/evidence; publishing all mixed hunks did not broaden acceptance. |
| Unaccepted integration candidate | `RagDocumentInput`, `RagSourceAuthority`, `RagDocumentBatchService`, `RagDocumentFinalizationGate`, client, grants/staging, reports/recovery and native batch tests | Inspect and repair only Slice 3 D+F. Existing code is reusable input, not certified output validation/publication. FR18-3 remains OPEN. |
| Excluded, temporarily retained | TXT/Office/PDF/OCR dispatch/adapters, related mixed tests/fixtures, dependencies and OCR identity fields | No external input or acceptance credit. Deny at the new snapshot path; do not delete now or require their positive quality gates. One cleanup at Slice 4 end. |
| Historical evidence | D22 missing raw captures, failed Java full gate and earlier reviews | Preserve; not recovered/passed by publication or this preparation. |

Observed integration gaps, not independently executed diagnoses:

- `RagDocumentInput.approvedRoot` emits DOMAIN; `RagDocumentBatchService.executeDocumentBatch`
  requires DOMAIN and denies caller bodies. Preserve that denial and add Java-owned snapshot transport.
  Batch reservation currently collects owner IDs into the Vault reservation path; external UUIDs
  need the existing RAG-owned external reservation path, never synthetic Vault IDs.
- Java finalizer pins parser `ppv-parser-v18.1-pdfplumber-0.11.10` and chunker
  `structure-first-v18.1`; Python pins `ppv-parser-v18.6-d17-html-container-boundaries`
  and `structure-first-v18.5-compact-source-context`. Python's metadata contains the
  legacy OCR string `rapidocr-1.4.4-en-hans-script-admission-v18.2`; carrying it as an
  exact compatibility field is not OCR admission. No new model or OCR runtime is needed.
- Python compacts Obsidian metadata with `block-local-references-v1`, locator defaults,
  locator definitions, source-structure definitions and cell references. Java must verify
  their resolution and source/revision scope, not merely nonempty maps/line numbers.
- Finalizer still derives an expected output from `DOCUMENT_V3:` plus input digest and
  validates locator shape. Exact result authentication, completeness and source fidelity
  remain substantive. Inspect both client receipt and worker digest paths before choosing a repair.
  Specifically, the Java client checks top-level `contract_version`, while the Python worker
  places it in the nested receipt in its batch response. This is a static contract mismatch
  to reproduce, not a claimed runtime diagnosis of every retained failure.
- Actual migration tip is **V21**, not historical V19. V18 grants runtime direct staging DML
  and worker-only function execution. Producer provenance must survive runtime-authored/altered
  rows. No rewriting V1–V21 or adding a schema/framework is authorized.

## Prospective applicability reconciliation

D24 extends the overall Phase 18 format target to **Obsidian Markdown `.md` only**.
This table governs future applicability; the original clauses remain historical text.

| Contract | Prospective amendment | Retained / evidence owner |
| --- | --- | --- |
| ADR-0020 | No format or Vault/Foundation change | Frozen v2, E1, process/SQL fences, source-first checks and recovery. |
| ADR-0021 D18-A.4/.7/.8; ADR-0022 G2 and format/locator clauses | Parser/quality/geometry requirements apply to D17 `.md` syntax, source spans, blocks, properties, tables/cells and approved Markdown children. No TXT/Office/PDF/OCR positive acceptance. | Exact producer tuple, actual result, Java/PG single C_P, consent, generations, limits and compatibility retained. |
| ADR-0023 feature set | Replace overall format list prospectively; same-user application threat model unchanged | Whole-file approved Markdown, safe inert semantics, no arbitrary reads, trusted result verification and actual process/SQL proof. R7 live-original gate remains for deferred live capability, not a snapshot prerequisite. |
| ADR-0024 | D22 Slice 2 approval remains historical; D24 supplies the previously missing overall-format decision | Slice 2 closure does not close Slice 3, Slice 4, FR18-3 or final verification. |
| AC-01/02/03/04 | AC-01 `.md` only; AC-02 OCR/PDF layout N/A by D24, retain Markdown structural/table order under AC-03/04; AC-04 exact D17 original locators | Native vi/en/zh-Hans/zh-Hant text remains; material loss/forged locator fails, no warning-only acceptance. D20 quality adjudication remains bounded to D17. |
| AC-05/06/07/08 | Format-specific input restricted; no other relaxation | Same identified offline model, finite 384-vector space, GPU/same-model CPU fallback, snapshot authority, actual publication, lifecycle/resource/report gates. |
| FCNC-08/09/10; IS-23/24/25 | Non-MD/OCR geometry subcases N/A by D24, not PASS | Markdown hidden/property/inert HTML/table fidelity, exact source/revision and approved child closure stay mandatory. Other FCNC/IS authority/recovery/lifecycle cases retain their slice assignment. |
| P18-FINAL QUALITY / OFFLINE-MODEL / RESOURCES | Markdown-only quality/locator inputs; model/tokenizer artifacts without OCR artifacts; resource scenarios for Markdown pipeline | Five final commands and all applicable checks remain; one final cleanup precedes gate. No broad test-ignore or historical failures relabelled PASS. |

## Slice 3 / Slice 4 boundary and cleanup

| Responsibility | Slice 3 D+F | Future Slice 4 E+H |
| --- | --- | --- |
| R transport/authority | Verify approved pending/selected exact R and child closure; Java supplies bounded immutable bytes | Complete update/NO_CHANGE eligibility and same-R Reindex API |
| Trusted publication | Validate actual D17 output, fence/seal, CAS, promote pending R only with successful G in single C_P; outcome/release/reconciliation | Complete retained/currentness lifecycle, purge/restore and report validation |
| Recovery | Publication rollback, unknown-C, crash/lost ACK, no resurrection of incomplete candidates | Full lifecycle/cleanup/restore combinations and aggregate resource investigation before final gate |
| Resources | Preserve limits; measure integration RAM separately from VRAM, cancellation and bounded transport/output expansion | Final comprehensive Windows resource profile and GPU/fallback evidence |
| Excluded formats | Keep unreachable from approved snapshot ingestion; reject wrong format and unapproved child payload | Remove unnecessary unaccepted implementation/dependencies/tests/fixtures once, before P18-FINAL |

Cleanup is required work for Slice 4, not permission to implement Slice 4 now.
At its end: inventory all excluded adapters/dispatch/native helpers/OCR artifacts and dependency
groups, mixed tests/fixtures/docs/config references; distinguish shared Markdown/Foundation code;
remove unwanted format implementations and solely dependent packages/tests/fixtures, including
obsolete OCR identity plumbing where compatibility permits. Update lock/config/imports and relevant
API/docs together. Preserve historical decisions/reviews/failure receipts and migration history;
do not drop persisted data/schema or change legacy v2 without separate authority. Git history
preserves removed source; no future-reuse adapter stubs or default optional OCR installation.
Verify the resulting full tree has no reachable non-MD external ingestion, missing shared dependency,
or ignored mandatory Markdown tests. Then run focused compatibility/static checks, resolve the
aggregate PostgreSQL resource limitation, and run the single complete P18-FINAL five-command gate.
No final acceptance before independent review of cleanup and real successful verification.

## Preparation gate and limits

Numbered Phase 18 `preparation-review.md` remains **READY FOR HANDOFF** under independent
[re-review 2](reviews/2026-10-17-phase-18-pre-handoff-codex-re-review-2.md).
It is reused as the accepted phase preparation gate, with D24 explicitly narrowing format
applicability and authorizing the new initial Slice 3. This document is handoff preparation,
not a new independent pre-handoff review or runtime acceptance. P18-03/04's exact approved
file outcomes plus **one final Java physical publication COMMIT** remain entry preconditions.
ADR-0023 already superseded snapshot OS R7/confinement proof; this preparation does not resurrect it.
Historical 2/2 remediation remains exhausted; this new initial slice does not reset it.

Graphify was queried for navigation (8,894-node cache; 1,200-token bounded query, truncated);
all material facts above were checked in source/contracts. No code, tests, worker, indexing,
model provisioning, database or deployment ran. Runtime assertions are NOT_VERIFIED and
become mandatory acceptance cases in the [Slice 3 matrix](slice-3-verified-indexing-test-matrix.md).
