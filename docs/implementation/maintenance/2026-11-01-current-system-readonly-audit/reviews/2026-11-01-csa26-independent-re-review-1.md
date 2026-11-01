# Independent re-review 1 — CSA26-AUDIT-REMEDIATION

- **Date / reviewer:** 2026-11-01 / independent Codex; SLICE maintenance implementation re-review.
- **Baseline:** main, HEAD 04528f76c32380f07377f470e1920a31583d690d; uncommitted working tree, empty index. Entry has 35 modified tracked paths and four untracked handoff/review records.
- **Authority:** [Owner selection](../owner-decision-csa26-selected-remediation-2026-11-01.md), [scope](../remediation-scope.md), [executable contract](../executable-handoff-2026-11-01.md), [ACTIVE](../../../handoffs/ACTIVE.md), and [initial review](2026-11-01-csa26-independent-final-review.md).
- **Status:** **CHANGES_REQUESTED**. **CSA26-FR-02 CLOSED; CSA26-FR-01 remains OPEN / MEDIUM / blocking.**
- **Remediation:** **1/3 consumed, 2/3 remaining**. This reviews submitted correction round 1; the next submitted correction is round 2. Takeover NOT_AUTHORIZED. Historical S19-2 budgets are unchanged.
- **Fresh evidence:** logs/csa26-maintenance/codex-rereview-1-20261101-140821-e2e13b0c/. Prior accepted isolation/test/lint evidence is reused after content and inventory checks, rather than rerun.

## Requirement and correction assessment

Round 1 restores the missing container topology, separates suspended QA from accepted capabilities, supplies a new container PNG, and fixes the module PNG graph. The same existing views and local rendering tools are sufficient. No new production behavior, schema, dependency, runtime or test framework is needed.

The container export now introduces incorrect implementation facts and still crosses a status label. These are unresolved parts of the selected source-truth/legibility requirement A07, retained under the original finding ID.

## Finding disposition

| ID | Severity / blocking | Independent result and exact evidence | Minimum remaining correction |
| --- | --- | --- | --- |
| **CSA26-FR-01** | **MEDIUM / YES / OPEN** | **Partially corrected.** Required nodes and the suspended-QA distinction are now visible. Remaining source/export defects are detailed below; A07 still fails. | Correct only the container representation and affected handback evidence, synchronize SVG/PNG with DOT/DSL, and inspect both actual renders. |
| **CSA26-FR-02** | **CLOSED** | DOT, draw.io XML and SVG have identical sets of **20 nodes / 41 edges**, including rag → vault, fiction and film. Both module PNGs now show those three arrows and legible labels. SVG aliases are byte-identical; PNG aliases are byte-identical. Delivered PNGs and freshly rendered SVG were visually checked. The handback identifies retained Graphviz vector art and Edge-derived PNGs, rather than claiming draw.io produced the delivered SVG. | None. Preserve these accepted module views; no further module-layout work or test rerun is required. |

Remaining **CSA26-FR-01** evidence:

1. **Incorrect embedding dimension:** container.svg:107 and its PNG say **Vector embeddings (1024-dim)**. Accepted Python embeds **384** dimensions (rag-engine/src/ppv_rag/embeddings/model.py:20); Java's publication contract also fixes 384 (RagDocumentFinalizationGate.java:36,486–489), and V24__rag_markdown_retrieval_readers.sql:198 requires 384. This label falsely describes the accepted storage/model contract. Use 384, or omit the unnecessary dimension label. **Do not change code, models or schema to fit the diagram.**
2. **Invented storage path:** container.svg:61 and container.dot:20 identify **data/snapshots/** as the snapshot location without marking it as an example. RagOriginalBytesStore.java:148,190–195 uses ppv.rag.storage.snapshot-root when configured, otherwise user.home/.ppv-vault/rag-store. No inspected current application configuration selects the depicted path. Label this as the configured Java-owned snapshot root, or accurately describe the configuration/default; no filesystem change is needed.
3. **Reader-release wording still loses sender completion:** container.svg:177 and workspace.dsl:68 say **positive child stop**. The DOT specifies **positive sender/child stop**, and ADR-0025:35–41 requires exclusion through actual output/container completion and verified positive sender **and** child quiescence, outside SQL transactions. Synchronize a compact note across the existing views to preserve that full boundary. No new sequence diagram or runtime proof is required.
4. **Status badge is still crossed by an unrelated connector:** container.svg:191 draws the feed path horizontally at y=615 through the Query Embedding Worker badge at global x=1185–1415, y=605–627 (badge text baseline y=620). The delivered PNG and independent SVG render visibly strike through **ACCEPTED S19-1 / DEFAULT_OFF**, suggesting an unrelated feed connection passes through the worker. Route this connector outside the worker/badge bounds and verify the final PNG/SVG. This contradicts the submitted zero-crossed-label claim.

Evidence: delivered container.png, fresh container-svg-render.png, XML text/geometry, and unchanged authoritative source. These are representation corrections within approved CSA26-003; there is no new architecture or acceptance criterion.

## Acceptance and original finding register

| Requirement | Disposition / evidence |
| --- | --- |
| A01–A04 | **PASS / reused.** All 14 changed Java/Python test hashes match the initial independent review; recursive inventories still contain the same 74 Java-test-subtree files and 119 Python-test-subtree files. All 131 finite fixture/control/writer paths remain unchanged. Prior absent/false/legacy Java initialization proofs, all four Python import-tripwire modes, 6 Java tests and 42 synthetic Python tests remain source-applicable. |
| A05–A06 | **PASS / retained.** Nine phase/setup entry points remain within scope. Review/status synchronization corrects the current maintenance counter and disposition; accepted/suspended phase boundaries and supported safe commands are preserved. |
| A07 | **FAIL** under the remaining CSA26-FR-01 items above. Restored topology does not make inaccurate labels or crossed status text acceptable. |
| A08 | **PASS.** Source graph set equality, all three visible RAG arrows, complete alias equality and actual module render inspection; CSA26-FR-02 CLOSED. |
| A09 | **PASS / reused.** The four-file/full Ruff and format receipts remain valid against identical test source and unchanged production/configuration. |
| A10 | **FAIL pending A07 correction.** Self-review and matching submission status existed, but submitted container source-truth/zero-overlap PASS claims are not independently verified. Correct these claims with the next actual delivery. Current review status/counter is synchronized at this boundary. |

**CSA26-001, CSA26-002 and CSA26-004 remain CLOSED. CSA26-003 remains OPEN.** Initial review and historical decisions/evidence remain unchanged.

## Verification and scope

- Fresh recursive inspection of docs/architecture/diagrams/source, exported and structurizr records every file hash in inspection.json. Parsed draw.io/SVG XML passes; DOT/draw.io/SVG module node/edge **sets**, not just counts, agree. Aliases compare byte-for-byte. Module ownership and portability's isolated/read-only JDBC exception are preserved.
- Fresh images: container PNG 2360×1180; both module PNGs 2400×800. Local SVGs were embedded in run-local HTML and rendered with installed Edge headless, using isolated run-local profiles. Both bounded commands used the existing runner, normal profile, 120s maximum, fresh receipts render-container.json and render-module-dependencies.json; exit 0, complete capture/cleanup, no termination needed. Every changed view/alias is covered by direct inspection or proven byte-identical alias.
- Reproduction: the run contains inspect_rereview.py, the two render HTML files and captured PNGs. Renderer invocation was the existing scripts/run-bounded-command.py wrapper followed by installed msedge.exe with headless/disable-gpu/disable-background-networking/no-first-run/hide-scrollbars, a unique user-data-dir, screenshot destination, window sizes 2360,1180 and 2400,800, and the corresponding local HTML URI. No network/runtime/model provision was executed.
- Reuse verified in inspection.json: twelve successful initial-review bounded receipts retain exit 0 and complete capture/cleanup; all 14 reviewed test hashes match. preservation-reuse.json confirms 131 preserved paths and both full recursive test inventories unchanged. No runtime or test was rerun merely for this review label.
- Git diff shows **no change** to backend/src/main, rag-engine/src, Maven/Python dependencies/configuration, uv.lock or database artifacts. Existing tests retain only the originally reviewed guards/import changes.
- Formal Graphviz/Structurizr semantic validation remains **NOT_VERIFIED under the explicitly permitted tooling limitation**. XML parsing and successful rasterization are not represented as formal DSL validation.
- The initial review's compiler/JDK/Mockito warning assessment remains applicable; no compiler/IDE inspection was rerun and no warning-free claim is made. No QA execution, inference, GPU probe, DB integration, model load, download, full suite or publishing is authorized by this review.
- Current local links, saved statuses, test hashes, whitespace and empty-index checks are recorded in final-check.json after review/status writes.

## Risk-applicable coverage

The [initial full coverage ledger](2026-11-01-csa26-independent-final-review.md) remains applicable after drift checks, with the following current dispositions covering all 29 dimensions.

| Dimensions | Result / rationale |
| --- | --- |
| 1–3, 10 | **FINDING:** incomplete selected container correctness/source-truth deliverable, CSA26-FR-01. Module export discrepancy is independently CLOSED. |
| 4–9 | **PASS:** unchanged production ownership/abstractions and package grouping, unchanged full test inventories, actual recursive diagram tree and graph comparison. Initial ADR-0016/root ApiResponse review remains applicable; no transport/package move or common module is introduced. |
| 12–14, 16, 18, 21–22, 27 | **PASS for preserved test boundary:** hashes and finite preservation support reused isolation/security/resource/test evidence. No new runtime, performance path, credentials, logging or recovery behavior. |
| 11, 15, 17, 19–20, 23–25 | **N/A to this correction:** no wire/API, transaction, persistence, ingestion/OCR, answer/retrieval quality, benchmark/evaluation or dependency change. The diagram dimension error is a documentation defect, not authority to alter a frozen model/schema. |
| 26 | **PASS** for applicable XML/render/reused static checks; formal DOT/DSL semantic validation **NOT_VERIFIED**, explicitly permitted as above. |
| 28 | **FINDING:** remaining container facts/lifetime/legibility and false handback PASS claims. Current review/status fields are synchronized; no historical cleanup. |
| 29 | **PASS:** inspected changed-path/tree inventory, no new production/cache/model artifact, no staged changes, preserved fixtures/history and scoped whitespace checks. Required aliases are intentional. |

## Scoped Docs Sync Check

- **UPDATED — review/status only:** this re-review, ACTIVE, executable handoff current final-review disposition, maintenance-scope current state, implementation-handback reviewer disposition, and five current entry-point maintenance counters. No diagram or test is repaired by Codex.
- **ALREADY_CONSISTENT:** accepted P17/P18/S19-1 versus suspended S19-2; module sources/exports and closed CSA26-001/002/004. No accepted delivery has been published in this review.
- **FAIL:** container facts and reader/output annotation versus actual source/ADR, crossed worker badge, and corresponding submitted PASS claims. Address only under CSA26-FR-01.
- **DEFERRED_OUT_OF_SCOPE:** nested/historical agent/rule/navigation drift, ten older LOW backlog IDs and AUTH-CLOCK; no old record is rewritten and no historical S19-2 counter is transferred or reset.
- **Publication:** no readiness or commit message. Owner-only normal staging/commit/push follows a future accepted review; no separate staging-review session.

## Required next correction

Use existing maintenance authority for **CSA26-FR-01 only**: correct the four concrete container representation issues, synchronize its existing sources/SVG/PNG, and supply truthful scoped source/export/render and handback evidence. Preserve the closed module finding and existing passed test/lint evidence. Self-review, save matching review-ready status, and request independent re-review. Reuse unchanged test receipts; no wider suite, runtime, installation or architecture change.

Next step: Antigravity | /antigravity-implement-handoff, then /antigravity-self-review and independent $codex-final-review | CSA26-AUDIT-REMEDIATION; [ACTIVE](../../../handoffs/ACTIVE.md), [executable handoff](../executable-handoff-2026-11-01.md), this re-review | close remaining CSA26-FR-01 container defects and correct handback evidence; no commit yet | maintenance remediation 1/3 consumed, 2/3 remaining; next correction 2/3; takeover NOT_AUTHORIZED.
