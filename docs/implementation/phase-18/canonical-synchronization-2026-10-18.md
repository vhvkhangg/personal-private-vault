# PHASE 18 — CANONICAL IMMUTABLE SNAPSHOT CONTRACT SYNCHRONIZATION

**Date:** 2026-10-18. **Contract:** `OWNER_APPROVED / CANONICALLY_SYNCHRONIZED`. **Implementation/runtime:** `CHANGES_REQUESTED / STOPPED / INCOMPLETE / NOT_VERIFIED`. **Next gate: independent documentation synchronization review.** This is the author's one bounded documentation-only submission, not an independent review, implementation handoff, runtime certification, finding closure or owner commit readiness.

## Exact authority and baseline

The owner approved the following immutable inputs together, with the delta taking precedence over every named replacement, and separately authorized exactly one documentation-only canonical synchronization:

| Approved input | Verified SHA-256 before and after synchronization |
| --- | --- |
| [Base Immutable Snapshot proposal](proposals/immutable-snapshot-amendment-proposal-2026-10-18.md) | `a0e9844f8b3144671fa05ec983f11b559de648b7b980bdb1d51eb3d36966a8ff` |
| [Whole-File Consent + NO_CHANGE delta](proposals/file-level-consent-no-change-delta-2026-10-18.md) | `2cec348c54601dac3df2b5a25ea4a38b132d7722ce2a31d95bdc82480554cfae` |

Read both mandatory independent reports: [ISAR focused re-review](reviews/2026-10-18-immutable-snapshot-isar-focused-re-review.md) and [delta review](reviews/2026-10-18-file-level-consent-no-change-independent-review.md). Their READY_FOR_OWNER_CONTRACT_DECISION was followed by explicit owner approval; it is not runtime/implementation readiness. Applicable root/docs rules, backend rules for targeted source inspection, ACTIVE, ADR-0020/0021, owner decisions, AC/oracle/final gate, evidence/review ledger and P17 ownership/eligibility/consent/reservation/revoke/recovery contracts were consulted. Graphify was used only for bounded navigation; canonical files/code establish facts. No repository-wide code audit was conducted.

Entry HEAD: `435ffea979681f22d723f551dd8b0cd0b733c8cd`; branch `main`. The tree was already dirty with inherited Phase 18 code/tests/migrations/build files, evidence, docs and model/fixture artifacts. The complete inventory contains **2,114 tracked/nonignored paths**, with full porcelain status, existence, byte length and SHA-256 captured before canonical edits. No clean-tree assumption, reconstruction or destructive restoration was used.

Durable local preservation directory: `.git/codex-preservation/phase18-canonical-sync-20261018/` (not a tracked artifact). It contains:

- `before.json`: complete entry HEAD/branch/status/inventory/per-file hash manifest; SHA-256 `08d0dd4b528d3f07069115232d0d72a0bf4bf794d4c359d2c0b05ab92545f6a4`.
- `inherited.diff`: complete tracked binary diff from HEAD before this session.
- `before-files/`: exact before-change documentation bytes, including inherited untracked inputs/reviews.
- `synchronization.diff`: complete unified session diff against those before bytes, including both new documents. This isolates this session from inherited code differences against HEAD.
- `after.json`: post-change inventory/hashes, exact changed/new/deleted paths, preservation/link/structure results and HEAD/branch. These local files support separate review; they are not runtime receipts.

Actual ADR inventory was 0001–0021. Existing convention uses numbered additive ADRs and scoped supersession pointers rather than replacing accepted rationale. **[ADR-0022](../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md)** is the single authoritative consolidated target for the new external snapshot class. No new module edge, frozen Schema v1/package-tree/diagram change or executable schema was needed. The additive Phase 18 DBML records target responsibility comments alongside clearly labelled current V18/V19 structures; no speculative physical tables were invented.

## Consolidation and exact boundaries

All **N1–N10**, **D1–D11** and **WF-1–WF-8** are integrated with the approved replacements applied, not concatenated as competing contracts. D2's unconditional new-R-on-equal-bytes rule and D10's raw-custody/field-safe permission ladder are retired for whole-file S. One source-bound finite authenticated action approves complete ORIGINAL_BYTES and supported private/NSFW/hidden/comment/property/frontmatter content. Independent external children retain independent grants/S/R/provenance; execution/fetch remains forbidden.

ADR-0022's exact applicability table prospectively replaces:

- ADR-0020 decision 3/Practical Offline privacy bullet and P17-2 §§1–4/P17-3 content privacy/field/reset applicability **only for new external whole-file S**; frozen Vault/Fiction/Film source/field/description/reset behavior is unchanged.
- External live-original currentness/lifetime in ADR-0020 decision 5, P17-2 external row/§3 and P17-3 IndexCurrent/live-edit semantics: protected original capture/seal, then protected selected immutable R/authorized vector through use/physical finish. Original edits/move/delete alone do not revoke/stale R. SNAPSHOT_CURRENT always carries original UNVERIFIED.
- External changed/equal-byte consent/intake rules: exact SHA-256/length and every authority/integrity/selection/child/incident/pipeline predicate; valid NO_CHANGE at C_N changes no R/ordinal/sourceVersion/G/selection/consent/closure and runs no pipeline/worker. Same-R reindex is separate real work, without O reread; equality never restores authority.
- Reserved snapshot incident applicability: narrow Java deny-only C_D despite reservations, with no grant/reset/selection/release bypass. Normal genuine cancel-first C_R, publication C_P and guarded nonpublication C_N remain distinct and physically ordered through common source guards.
- Named D18-A.1–8 external source/field/currentness/live-lifetime and NO_CHANGE/roster scope, retaining exact union, source-first/NOWAIT, actual producer, one trusted C_P, independent lifetime, v2/limits and all unaffected restrictions.
- AC-01/03/07 external content filtering and AC-04 external permission markers; narrowly adds original structural container-part/property-key/object/comment-ID locators for genuinely unlocated material, never fabricated page/box/cell coordinates or removal of actual geometry.

Live R7-through-C remains DISABLED / DEFERRED for future separately authorized live capability, with all historical failures/diagnostics unchanged. Snapshot receipt P18-SNAPSHOT-INTAKE-LIFETIME replaces its applicability to S, not its historical outcome. Approved snapshot authority/store/producer/physical finish remains unproved.

Preserve frozen Vault privacy/fields/public module ownership, durable reservations/normal busy conflicts, one operation/one-use worker, actual task/tree death, writer drain/seal, E1, independent completed-index lifetime, rollback/unknown-outcome recovery, transitive denial/purge and Practical Local Offline. Historical Phase 17 acceptance and all independent review bodies remain unchanged. Prospective pointer-only additions to the three P17 contracts do not rewrite their original content or accepted evidence.

## Acceptance, API, status and evidence synchronization

- AC-01–08 retained: every required MD/TXT/DOCX/native/scanned/mixed PDF/XLSX/XLS input, vi/en/zh-Hans/zh-Hant, tables/cells/merges/reading order, one chunker/model, exact locators, offline/native GPU/same-model fallback and separate 16 GB RAM / 8 GB VRAM plus CPU/time/temp/expansion/cancel/restart measurements.
- The locator oracle adds only the approved typed unlocated internal-object variant. Located page/cell/paragraph/child content retains mandatory geometry/structural attribution and parser-to-original excerpt correspondence. Material omissions/invalid locators reject the file.
- FCNC-01–15 exact approved rows are integrated into existing focused intake/lifetime, publication, quality, offline/model, resources and report receipts. IS-01–30 remain mapped with delta precedence; no new full-suite program, quota or numerical quality threshold.
- P18-FINAL retains its exact five executable command blocks and strict aggregate policy, with amended focused snapshot/NO_CHANGE/producer proof and no automatic AUTH-CLOCK waiver. It remains the single complete final gate and is NOT ACCEPTED.
- API documentation separates current DOMAIN/v3 inventory from unimplemented target O/S/R/G/WHOLE_FILE/NO_CHANGE/currentness/reporting. New fields/routes/storage structures are not represented as deployed. Reports distinguish retained/new G, selected/pending R, original UNVERIFIED, incident versus actual revoke and controlled active-copy purge versus DB/WAL/owner copies.
- Owner decisions D1–D11 and current indexes/operating/status pointers reflect final contract approval. ACTIVE is **P18-DOCUMENT-INDEXING / CHANGES_REQUESTED / STOPPED / INCOMPLETE / NOT_VERIFIED**. Its inherited launch/implementation submission text is explicitly historical and suspended, preserved for attribution, not an instruction to implement automatically.

## Confirmed current implementation mismatches

This is a compact factual target/current ledger, not an implementation plan or another investigation. Code paths below were read only where needed; the existing independent review supports untouched capability/evidence conclusions.

| Mismatch | Confirmed current evidence / missing target |
| --- | --- |
| 1 — live-only denied external path | [RagSourceService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceService.java) `registerFileCandidate` unconditionally throws live-disabled; `approveSource` also denies EXTERNAL_FILE. [RagSourceAuthority.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagSourceAuthority.java) `prepareSource` rejects EXTERNAL_FILE. This is containment, not a snapshot path. |
| 2 — safe snapshot acquisition absent | The [proposal's confirmed implementation inventory §16](proposals/immutable-snapshot-amendment-proposal-2026-10-18.md#16-smallest-later-implementationtest-impact--planning-only) and [independent delta review G1–G4](reviews/2026-10-18-file-level-consent-no-change-independent-review.md#9-findings-and-remaining-implementation-blockers) identify no snapshot intake/seal path. Current guard accepts a Path and is not approved root/ancestor-bound capture proof; G1 remains unresolved. No native experiment was performed here. |
| 3 — ORIGINAL_BYTES store absent | [RagExternalFileSource.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/domain/RagExternalFileSource.java) and current [V18/V19 DBML inventory](../../database/phase-18-document-indexing.dbml) expose identity/counters/eligible/reserved metadata, without immutable R/selected-R/exact-original store/seal. No protected ORIGINAL_BYTES capture/store implementation was found in the bounded documented source inventory; G3 remains unresolved. |
| 4 — whole-file source authority absent | [RagExternalFileAuthority.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagExternalFileAuthority.java) wraps external metadata/guards/reservations and creates default counters; this is not source-bound whole-file consent. Current preparation denies external input. Target WHOLE_FILE grants/revision/incident binding are unimplemented; G2 content and G4 authority proof remain. |
| 5 — NO_CHANGE / C_N absent | A bounded exact-token check of Java rag and ppv_rag source found no ORIGINAL_BYTES, WHOLE_FILE, NO_CHANGE, SNAPSHOT_CURRENT or C_N contract path. Current flow has no guarded no-change disposition/finish, same-R snapshot reindex or mixed unchanged roster semantics. This does not claim an audit of all similarly named generic snapshots. |
| 6 — snapshot reports/currentness absent | [RagGenerationState.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagGenerationState.java) uses Vault owner long IDs, NON_PRIVATE/approved fields/reset and returns CURRENT/OUTDATED/REVOKED/UNAVAILABLE. [RagDocumentReportService.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDocumentReportService.java) counts SUCCESS/other outcomes and queries that state; it has no snapshot R/basis/original-UNVERIFIED/NO_CHANGE contract. |
| 7 — G1–G4 | All OPEN / NOT_VERIFIED: approved native acquisition, safe whole-file extraction/locators, protected immutable exact-byte store, and existing operation/reservation/incident/C_N/C_D/C_R/C_P lifecycle/recovery. Owner contract approval proves none of them. |
| 8 — FR18 producer/publication/capability blockers | [RagDocumentFinalizationGate.java](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDocumentFinalizationGate.java) still computes expectedOutput from `digest("DOCUMENT_V3:" + expectedInput)`; exact canonical actual result/producer binding remains Critical FR18-3. The unchanged [post-takeover review](reviews/2026-10-18-phase-18-independent-post-takeover-review.md) records retained external/child/physical-C, four-language/scanned-table/locator, GPU/offline/fallback, resource/operations/report and final-evidence gaps. No earlier DOMAIN positive certifies S/NO_CHANGE. |

**FR18-1, FR18-2, FR18-3, FR18-4, FR18-5, FR18-6, FR18-7, FR18-8, FR18-9, FR18-10 and FR18-11 remain OPEN for independent adjudication.** Preserve credited input/native DOMAIN publication, VECTOR/JPA mapping, source-first/union, generation-specific purge and parser/geometry/chunker repairs. No original finding is closed or renumbered; deferred live sub-obligations are not repaired/PASS findings.

## Complete documentation change inventory

**26 inherited documents modified; 2 documents created (ADR-0022 and this receipt).** All other inherited files preserve their before hashes. Historical review bodies, proposal/delta, runtime evidence/JSON, tests/fixtures, migrations, production, build/dependencies/model artifacts and frozen schema/package/diagram files are unchanged. Review-index/status/pointer documents are distinct from immutable dated review reports.

- [.agents/README.md](../../../.agents/README.md)
- [AGENTS.md](../../../AGENTS.md)
- [README.md](../../../README.md)
- [backend/AGENTS.md](../../../backend/AGENTS.md)
- [docs/README.md](../../README.md)
- [docs/adr/0020-phase-17-single-job-reservations-e1.md](../../adr/0020-phase-17-single-job-reservations-e1.md)
- [docs/adr/0021-phase-18-bounded-multi-file-verified-indexing.md](../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md)
- [docs/adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md](../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md)
- [docs/adr/README.md](../../adr/README.md)
- [docs/architecture/rag-architecture.md](../../architecture/rag-architecture.md)
- [docs/architecture/rag-phase-18-api-extension.md](../../architecture/rag-phase-18-api-extension.md)
- [docs/database/phase-18-document-indexing.dbml](../../database/phase-18-document-indexing.dbml)
- [docs/implementation/README.md](../README.md)
- [docs/implementation/handoffs/ACTIVE.md](../handoffs/ACTIVE.md)
- [docs/implementation/phase-17/integration-ownership-and-permissions.md](../phase-17/integration-ownership-and-permissions.md)
- [docs/implementation/phase-17/revocation-and-derived-data.md](../phase-17/revocation-and-derived-data.md)
- [docs/implementation/phase-17/source-eligibility-and-allowlists.md](../phase-17/source-eligibility-and-allowlists.md)
- [docs/implementation/phase-18/README.md](README.md)
- [docs/implementation/phase-18/acceptance-and-evidence.md](acceptance-and-evidence.md)
- [docs/implementation/phase-18/canonical-synchronization-2026-10-18.md](canonical-synchronization-2026-10-18.md)
- [docs/implementation/phase-18/final-verification-gate.md](final-verification-gate.md)
- [docs/implementation/phase-18/held-out-quality-and-locators.md](held-out-quality-and-locators.md)
- [docs/implementation/phase-18/owner-decisions.md](owner-decisions.md)
- [docs/implementation/phase-18/preparation-review.md](preparation-review.md)
- [docs/implementation/phase-18/reviews/README.md](reviews/README.md)
- [docs/implementation/rag-phase-plan.md](../rag-phase-plan.md)
- [docs/roadmap.md](../../roadmap.md)
- [rag-engine/README.md](../../../rag-engine/README.md)

## Validation and preservation result

Documentation-only checks compare the complete before/after manifests, exact approved hashes, required D/N/WF/AC/FCNC/IS IDs, links/anchors, unchanged final command block, preserved historical text, allowed changed paths and complete session diff. The full inherited tracked/untracked inventory is retained locally rather than pasted into canonical contracts. Git diff against HEAD still contains the owner's inherited implementation; the isolated **session diff contains only the 28 authorized documentation paths above**.

- Both approved input hashes match exactly; no reconstruction or change to them.
- Every **2,088 unaffected inherited file** matches its original SHA-256; no inherited path deleted or unexpected new nonignored file added. HEAD/branch unchanged.
- All relative Markdown file targets and local anchors in this synchronization resolve. Required-ID/structure checks pass; exact five-command final code block is unchanged. Pointer-only P17/ADR additions preserve the original text and ACTIVE's historical record remains intact.
- `git diff --check` passes; existing CRLF normalization notices are informational and no code bytes were changed. Complete before/session/after manifests and session diff are in the durable local preservation directory above.
- Source-kind scoping prevents active contradictory field-safe consent or live-original-through-C requirements for S. Historical/deferred live requirements remain explicit. D1–D11 and WF-1–8 do not waive authority, child approval, integrity, producer, physical transaction or retained acceptance.

**Unresolved documentation conflict: none identified in this bounded synchronization.** G1–G4/FR18 implementation feasibility and runtime proof are outstanding gates, not reopened owner contract decisions. Independent review may assess this author's synchronization; no independent acceptance is self-claimed.

**No production Java/Python, tests/fixtures, migrations/schema implementations, build files/dependencies or runtime configuration were changed. No tests/builds/native/OCR/GPU/SQL experiments, worker or ingestion were run/enabled. No reset/clean/checkout/destructive restore, commit, push, tag or PR occurred.** Only documentation hash/inventory/link/diff/structural checks and targeted read-only source inspection occurred. Stop after this submission; no implementation, additional design search or next phase follows.

**Exact next gate: independent documentation synchronization review.**
