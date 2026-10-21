# Phase 18 Slice 2 — D22 independent Markdown-only closeout re-review

**2026-10-21 — CHANGES_REQUESTED.**

**D22 validly resolves S2-D21-E01 for the bounded D21 closeout, and accepted ADR-0024 resolves D21-OD-01.** Original raw preservation remains FAILED / NOT_RECOVERED; neither finding needs another recovery demand or another approval of the same qualification. D17/D20 acceptance and R01–R05 independent closures remain recorded.

One new mandatory verification blocker prevents closeout: **S2-D22-01 — fresh-cache mypy fails on the actual selected Markdown environment**, not just the proposed delivery projection. Runtime Markdown tests pass, but the required static gate is not reproducible. No production fix, dependency upgrade or further implementation cycle was performed or authorized.

## Scope and independently verified authority

Applied `codex-final-review` in the explicitly approved governance/review mode, its shared 29-dimension rubric and applicable Python/RAG, reuse/design and bounded-command rules. Read [ACTIVE](../../handoffs/ACTIVE.md), [Owner decisions D17/D20/D21/D22](../owner-decisions.md), the complete [frozen D17 contract](../obsidian-markdown-d17-contract.md), [D21 matrix](../slice-2-markdown-only-d21-scope-and-acceptance.md), [traceability](../slice-2-markdown-only-d21-governance-traceability-2026-10-20.md), [proposed closeout](../slice-2-markdown-only-d21-proposed-closeout-2026-10-20.md), [preceding independent review](2026-10-21-slice-2-markdown-only-independent-closeout-review.md), [D22 disposition](../slice-2-markdown-only-d22-evidence-disposition-2026-10-21.md), [D22 handoff](../slice-2-markdown-only-d22-closeout-rereview-handoff-2026-10-21.md), [ADR-0024](../../../adr/0024-phase-18-slice-profile-acceptance-boundaries.md), relevant ADR-0020–23/0016 and retained P18-FINAL requirements. Examined actual source, tests, fixtures, dependencies, shared consumers and Git changes; the older Graphify cache supplied navigation only.

Baseline: **main / c3b46351507803053850e3c2711602ee9c9ec419**, with mixed inherited work. At entry, inventoried **2,450** nonignored files and protected **239 existing runtime/evidence artifacts**. Verified all **111** reviewed source/test/script/configuration hashes against the preceding review, both actual model/tokenizer hashes, the **193 unchanged D19 files**, **14** R03 receipt records, **seven** preceding closeout receipt/log pairs, **165** older independent log/artifact hashes and **10** original frozen documentation snapshots. The three known original-byte mismatches are exactly those covered by D22; no additional mismatch was accepted as an exception.

The [new evidence ledger](2026-10-21-slice-2-d22-independent-closeout-evidence.json), SHA-256 **`e8a318c734897bf2e11aa2016247c6dd7c83813a504c01671b61db866e8e5e04`**, records exact argv/cwd/budgets/exits, hashes, preserved records, isolated projection and raw verification results. All new receipts are under `logs/phase18/s2-d22-independent-closeout-2026-10-21/`, created as a new directory. Historical writer/test scripts were not rerun. No original capture, receipt, failure verdict or old review was rewritten.

## Prior findings: independent dispositions

### S2-D21-E01 — RESOLVED FOR BOUNDED CLOSEOUT BY D22 OWNER EXCEPTION

D22 explicitly accepts only the loss of the following three original raw captures under `logs/phase18/d17-r03-independent-2026-10-20/`:

| File | Preserved original SHA-256 |
| --- | --- |
| `fresh-producer-bound-True.json` | `48b1383e46bbf508bd25faa1075db857d1daac018efc8ccd4bef708a16bcc0b0` |
| `fresh-producer-bound-False.json` | `2909e393b18648b3b4b4ad3bf77b35e6e2ca1d7693505aabf022af99c26f72e1` |
| `fresh-negatives.json` | `3727b91410965e67df87074d0384e12a86e2f4e3d013cf056051845ae1ed91e4` |

Independently verified that current replacements still have the incident's recorded different hashes and match their separately labelled rerun copies. The original ledgers, original hashes, preceding CHANGES_REQUESTED report and incident record are intact. **Original bytes are NOT_RECOVERED; original preservation is not PASS.** D22 does not reconstruct or backdate telemetry.

Recomputed the D19 comparison independently for **all 52 cases**: original source, status/reason, chunks, locator/structure metadata and embedding-value digests agree with the explicitly labelled rerun copies. D19's original capture references/hashes remain references to the lost originals, not relabelled replacement receipts. The D19 projection intentionally omits host/resource measurements and cannot recover original JSON bytes. Separate surviving resource/inference/intersection/accounting receipts remain available and hash-verified.

This is a sufficient, narrow substitute **for the identified D21 producer-local evidence gap**, under the explicit Owner risk acceptance. It does not waive source fidelity, consent, resource controls, quality checks, other corruption, Java/PG publication or P18-FINAL. It is not a generic evidence-integrity exception. No impossible byte recovery or extra human signature is required by this review.

### D21-OD-01 — RESOLVED BY D22 / ADR-0024

ADR-0024 now expressly states **ACCEPTED prospectively under Owner D22, for D21 Slice 2 applicability only**. Its authority is explicit in the Owner ledger and current canonical notices. The adopted qualification preserves all MD17-01–12 and D20, excludes TXT/DOCX/PDF/XLS/XLSX/OCR from **Slice 2 acceptance only**, and preserves overall Phase 18 obligations and future explicit-format decisions.

Independently recovered the original substantive bodies of ADR-0021/22/23 and P18-FINAL beneath their applicability notices and matched their pre-D21 hashes. The pre-D22 D21-and-earlier Owner-decision body also matches its original hash. Current traceability/index notices correctly report the prospective acceptance; dated decisions and independent reviews retain their historical outcomes. No security/provenance/publication requirement was silently reassigned to Slice 3/4 or deleted. No further Owner approval of this same scoped qualification is needed. A future overall Phase 18 format-target decision remains separate and does not create a new D21 closeout blocker.

## New finding

### S2-D22-01 — Medium — selected Markdown runtime fails required fresh-cache mypy

**Location:** `rag-engine/src/ppv_rag/ocr/engine.py:50`; selected environment/configuration in `rag-engine/pyproject.toml` and `.venv-d17`.

An isolated delivery projection first failed mypy on the optional RapidOCR import. To distinguish a projection error from the real candidate, executed the actual current source with an explicitly fresh cache:

```text
rag-engine/.venv-d17/Scripts/python.exe -m mypy
  --cache-dir logs/phase18/s2-d22-independent-closeout-2026-10-21/mypy-current-fresh-cache
  --config-file rag-engine/pyproject.toml rag-engine/src
```

Receipt `current-mypy-fresh.json`: **COMMAND_FAILED / exit 1**:

```text
rag-engine/src/ppv_rag/ocr/engine.py:50: error: Cannot find implementation or library stub
for module named "rapidocr_onnxruntime" [import-not-found]
Error code "import-not-found" not covered by "type: ignore[import-untyped]" comment
Found 1 error in 1 file (checked 24 source files)
```

Direct environment inspection confirms **Python 3.14.4 / mypy 2.4.0**, with no installed RapidOCR distribution and no import spec. The lock deliberately defers this backend to a Python `<3.13` dependency group. Its absence is expected for the selected Markdown runtime, but the existing optional-import/type-check boundary does not make the mandated static command pass. The worker/PDF import graph keeps that module within the actual checked source tree even though Markdown does not instantiate OCR. The existing configuration provides a narrow `cv2` override and explicitly claims no missing-import waiver for other modules.

**Consequence:** older green static receipts cannot establish current reproducible closeout/delivery readiness. Source hashes are unchanged and no Markdown extraction regression is demonstrated; nevertheless, the current required gate genuinely fails. D22's three-capture exception does not waive this independent static failure. The same error in the projected HEAD OCR module is supporting evidence, not the sole basis of the finding.

**Required result:** a separately authorized bounded correction must reconcile the optional excluded-backend import/type-check boundary with the selected Markdown environment and make the required command reproducibly pass, retaining the frozen runtime/model and all safety/fidelity criteria. Do not install an incompatible OCR runtime, resume OCR development, blanket-suppress missing dependencies or claim that D22 waived the gate. No correction is implemented here. The reason prior mypy receipts were green is **not established**; cache/environment differences are hypotheses, not a proven diagnosis or permission to relabel old results.

## Complete D21 acceptance matrix

Historical D17/D20 acceptance and the five finding closures are preserved. This matrix distinguishes those accepted runtime behaviors from the currently failing closeout verification gate.

| ID | Independent current disposition | Basis |
| --- | --- | --- |
| MD17-01 | VERIFIED within accepted bounds | Unchanged CommonMark/GFM/Obsidian parser; accepted 1,267 support cases + 62 required rejections, seven interpretations; projected mixed-note tests pass. |
| MD17-02 | VERIFIED | Tables, cells/alignment/escapes/overflow/nesting; unchanged independent source-cell/intersection and lossless representation proofs; current tests pass. |
| MD17-03 | VERIFIED | Source-backed safe properties/scalars/lists/nesting and malformed/duplicate/unsafe YAML whole-root denial. |
| MD17-04 | VERIFIED | Tasks, lists/quotes/headings, correct sibling/outdent/mixed-container ancestry and CRLF/tab attribution. |
| MD17-05 | VERIFIED | Wikilinks/aliases/heading/block references, inert ordinary links and missing/ambiguous selector denial. |
| MD17-06 | VERIFIED | Immutable supplied Markdown bindings, child revisions, actual parent edges, selection/conflicts/cycles/depth and literal-versus-active HTML embeds. |
| MD17-07 | VERIFIED | Arbitrary/nested/collapsible callouts retain hidden bodies, titles and source-backed child context. |
| MD17-08 | VERIFIED | Footnotes/comments/tags/highlights and original source/metadata fidelity. |
| MD17-09 | VERIFIED | Inert code/fences/Mermaid/math; executable directive denial without fetch/execution. |
| MD17-10 | VERIFIED | Non-void/void solidus, native/RCDATA/optional tags, EOF/blank blocks, Markdown container boundaries; FR-01/02 remain independently closed. |
| MD17-11 | VERIFIED within measured bounds | Source/revision/locators, exact splits, model/vector/caching/compaction, finite limits/cancellation and surviving resource receipts; D22 addresses only specified lost telemetry. |
| MD17-12 | RUNTIME/COMPATIBILITY VERIFIED; CLOSEOUT STATIC GATE BLOCKED | Complete-root failure, zero chunks, outcome/cancellation behavior and relevant tests pass; required fresh-cache mypy fails S2-D22-01. No current all-gates PASS. |

**R01/R02/R04/R05 remain independently CLOSED**: model/cache/vector/thread math, intersection/compaction functions, worker accounting and original limits are unchanged. R03/FR-01/02 likewise remain CLOSED. The earlier repeated million-character fixture has **689 chunks / two normalized texts**; the independent unique-content workload has **725 distinct chunks / 52.900 seconds** under the unchanged 60-second deadline. No maximum workload was rerun, no universal throughput claim added and no original failure converted into success.

D20 still makes Codex independent adjudication sufficient for D17: D19's 213 case + 12 profile forms remain unsigned and NOT_REQUIRED_UNDER_D20. D21 exclusions are acceptance assignments, not claims that dormant runtime dispatch was removed. No new human, held-out or non-Markdown acceptance was invented.

## Exact delivery boundary and reproducibility

The [current delivery inventory](2026-10-21-slice-2-d22-independent-delivery-scope.json) enumerates **336 current dirty paths**, with SHA-256 values and exact zero-context hunk headers/digests. This preserves the earlier 329-path inventory as history and accounts for seven added governance/review paths. Current categories: **22 Markdown paths, 12 shared/mixed paths, 35 excluded implementation paths, 265 governance/history paths, one inherited AGENTS policy path and one necessary `.venv-d17/` ignore hunk**.

To substantiate selective staging rather than merely list files, constructed an **isolated review projection** from HEAD and only existing candidate lines/hunks. No repository/index file was changed. It selects **31 package/test/fixture/dependency/support files**, plus the existing one-line `.gitignore` addition. The exact proposed patch, selected-file hashes, original/retained AST spans and resulting hashes are in the ledger and `proposed-markdown-only.patch` under this review's log directory. No new production function body was authored. Original functions retained in the shortened `limits.py` have identical ASTs.

| Shared path | Exact proposed selection / risk |
| --- | --- |
| `chunking/chunker.py` | Existing shared source/revision/role, cancellation and D17 split/intersection hunks; generic address boundaries are shared, not Office extraction certification. |
| `embeddings/model.py` | Reviewed full model/cache/vector/thread/fingerprint hunks. The inactive OCR component tag is not proof of OCR support or Java admission. |
| `main.py` | Single cancellation callback/forwarding hunk. |
| `parsers/models.py` | Only common enum additions and source/locator identity-equality hunks. **Exclude DOCX-specific validator hunk**, current new lines 78–111; keep its HEAD behavior. |
| `parsers/text.py` | Three common size/checkpoint hunks for retained DOMAIN helpers; no external TXT acceptance. |
| `worker.py` | Existing shared complete-root scope, bounds, lazy OCR initialization, Markdown dispatch, metadata compaction, cancellation, counts and resource-report hunks. Existing format plumbing does not certify other adapters; no new excluded adapter implementation is selected. |
| `limits.py` | New-file statement selection: existing control/constants/imports and `ParsingCancelled`, `checkpoint`, `parsing_scope`, `check_text`. **Exclude `input_bytes`, `checked_zip`, `checked_pdf` and their unused imports**. Exact retained/omitted spans are recorded. |
| `pyproject.toml` | Existing Markdown pins, deferred OCR group split and matching selected runtime configuration; no upgrade/new suppression. Static import boundary still blocks. |
| `uv.lock` | Matching existing project/group/marker/transitive lock hunks; retain pins, no regeneration/upgrades. |
| `tests/test_parsers.py` | **Exclude all dirty hunks**, which are Office/PDF/OCR changes. Retain HEAD baseline; its seven relevant legacy cases pass in the projection. |
| `tests/test_slice2_corrections.py` | **Exclude whole new mixed-format file** from this proposed clean source commit; preserve it in the working tree/history. Pure D17 tests supply the selected regression package. |
| `tests/test_slice2_checkpoint_package.py` | **Exclude whole new OCR package test/fixture addition** from the Markdown source commit; retain historical evidence. |

The 22 Markdown paths are individually enumerated, including three Markdown/HTML parsers, four D17 test files, two fixture families, evidence scripts, dependency inventory and development-only model provisioning. The 35 excluded paths remain untouched; projected excluded adapters use their committed HEAD versions. No new Office/PDF/OCR source/fixture or native-OCR Java test delta is selected. Governance/history files are enumerated separately: preserving old review/failure documents does not accept their excluded implementation. **Exclude inherited AGENTS changes** from this staging proposal. Venvs, models, caches, logs and this entire projection remain ignored runtime/review artifacts, not proposed source content.

**A functional existing-change boundary is constructible, but a clean check-complete commit is not currently verified.** The projection passes **168** relevant tests and **seven** legacy cases, Ruff, formatting and lock checks. It fails mypy, as does actual current source. Selective staging alone cannot cure that existing missing-dependency boundary. No commit scope is eligible or authorized now; after an approved correction, the final selected tree must be checked again and its patch/hashes frozen before any owner-commit gate. No wholesale directory staging, cleanup/deletion of excluded work or new implementation is requested by the patch artifact.

## New checks and reused evidence

All exact commands and receipts are in the [ledger](2026-10-21-slice-2-d22-independent-closeout-evidence.json). Watchdogs used normal profile and 30–180-second budgets. Historical scripts with fixed capture destinations were not executed. Repository D17 tests were copied into the isolated projection after inspection for fixed-output writers; pytest cache/temp and model read locations were explicitly controlled. The decisive actual-source mypy command uses a new explicit cache. No global zero-write claim is made for ordinary tooling caches.

| New receipt | Result / purpose |
| --- | --- |
| `verify.json` | PASS: source/model/history identity, 193 D19 files, 52-case substitute comparison, receipts and exact accepted three-file gap. |
| `governance.json` | PASS: higher-ADR/P18-FINAL bodies and old Owner decisions retained, 10 snapshots and 13 D22 links verified. |
| `project.json`, `delivery.json` | PASS: existing-line projection and exact path/hunk plan constructed; these are diagnostic completion, **not commit acceptance**. |
| `projection-pytest.json` | **168 passed in 39.42 s**, no failures/errors/skips; 14 focused files from the isolated tree. |
| `projection-legacy.json` | **7 passed / 5 excluded in 0.22 s**; HEAD parser tests with the explicit non-Office/PDF/OCR selection. |
| `projection-ruff.json`, `projection-format.json` | PASS; **62 files** already formatted. |
| `projection-mypy.json` | **FAILED / exit 1**: missing RapidOCR module in projected HEAD OCR adapter. |
| `current-mypy-fresh.json` | **FAILED / exit 1**: same missing-module error at actual current `ocr/engine.py:50`, 24 source files. |
| `projection-lock.json`, `pip.json` | PASS: offline lock **73 packages**, selected Python **3.14.4 / 67 compatible distributions**. |
| `environment.json` | PASS inspection: mypy 2.4.0, RapidOCR distribution/import absent. No OCR engine initialized. |
| `diff-check.json` | PASS with existing CRLF-normalization notices. |

**Twelve commands passed; two failed.** Neither failed static receipt is hidden by passing test counts. Original accepted 170-test, 25-compatibility, 25-independent-case and 1,329-corpus receipts remain historical and are reused where applicable; groups are not claimed disjoint. The initial projection mypy result motivated the independent fresh-cache actual-source check. The older static PASS records remain unchanged but do not establish the present gate. No full clean Java suite, million-character benchmark, OCR experiment, dependency installation or publication test was run.

## Frozen lifecycle and shared coverage

`RagSourceAuthority` still rejects EXTERNAL_FILE indexing. Java finalization still expects parser/chunker v18.1 rather than the reviewed v18.6/v18.5 tuple and has not admitted compact metadata. Trusted actual producer-output/closure/vector verification, pgvector publication, lifecycle/recovery and FR18-3 remain OPEN. Original G+H already excluded Java publication; D22/ADR-0024 do not waive it at the future gate. No new sourceVersion, selected revision or generation was promoted. The historical aggregate Java gate remains **FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips**; P18-FINAL and its aggregate PostgreSQL investigation/repeat verification remain unaccepted.

| # | Dimension | Disposition / evidence |
| --- | --- | --- |
| 1 | Intent/scope | PASS — explicit D22 independent review, no implementation authority. |
| 2 | Traceability | FINDING S2-D22-01; prior two blockers explicitly resolved, current static gate retained. |
| 3 | Architecture | PASS scoped — accepted prospective ADR-0024, original higher clauses preserved. |
| 4 | Ownership/boundaries | PASS — existing parser/model/worker roles; Java authority remains gated. |
| 5 | Recursive tree | PASS — 2,450-file entry, 336-path classification, actual shared-hunk and dependency closure inspection. |
| 6 | Shared contracts | PASS scoped — root ApiResponse/ADR-0016 and existing Python DTO/exception contracts retained, no new common module. |
| 7 | SOLID/cohesion | PASS retained — narrow existing roles, no new runtime abstraction. |
| 8 | DRY/KISS | PASS — existing functions/hunks reused; no duplicate production implementation created. |
| 9 | Patterns | PASS — no speculative framework, interface or lifecycle engine introduced. |
| 10 | Logic/outcomes | PASS runtime — whole-root failures, independent roots, exact counts and no publication. |
| 11 | API/compatibility | PASS bounded runtime; Java tuple admission remains intentionally unaccepted. |
| 12 | Validation | PASS D17 runtime — source/selector/HTML/limit/negative tests, no fabricated locators. |
| 13 | Privacy/consent | PASS scoped — supplied approved dependencies/revisions, original DOMAIN and Slice 1 boundaries unchanged. |
| 14 | Secrets/egress/artifacts | PASS scoped — local pinned model, no fetch/script/plugin execution or secret-bearing diagnostics. |
| 15 | Transactions/concurrency | N/A new SQL change — no publication capability enabled; frozen guards remain, FR18-3 open. |
| 16 | Failure/recovery/cancel | PASS producer-local runtime and surviving receipts; no new end-to-end acceptance. |
| 17 | Database/migrations | N/A — no schema/migration change; retained aggregate database issue not waived. |
| 18 | Resources/complexity | PASS measured bounds — unchanged resource paths/guards and separate surviving receipts; D22 missing telemetry disclosed. |
| 19 | Source/index quality | PASS bounded source/locator/edge fidelity; retrieval/trusted publication outside scope. |
| 20 | Document fidelity | PASS D17 runtime — full frozen profile evidence credited; excluded formats not certified. |
| 21 | Security tests | PASS applicable runtime negatives/cancellation; no superseded OS-security proof required. |
| 22 | Evidence quality | PASS scoped D22 substitute; FINDING current static failure honestly retained. Raw original preservation remains FAILED. |
| 23 | Performance evidence | PASS reused applicability — unique/repeated workloads distinguished, no needless maximum rerun. |
| 24 | Adjudication | PASS under D20/D22 — independent comparisons, no invented human/held-out acceptance. |
| 25 | Dependencies | FINDING — selected compatible package set lacks excluded backend, required static import boundary unresolved. |
| 26 | Build/static/tooling | FINDING S2-D22-01 — actual fresh-cache mypy fails; other executed checks credited separately. |
| 27 | Observability | PASS — original gaps, new failures, exact commands/hashes and measurement limits disclosed. |
| 28 | Governance | PASS D22/ADR authority and retained history; no new contract approval needed for prior blockers. |
| 29 | Hygiene/delivery | FINDING build reproducibility — exact selective proposal supplied, no staging or whole-tree certification; protected historical assets unchanged. |

## Decision and next Owner action

**CHANGES_REQUESTED — S2-D22-01 remains the sole current mandatory closeout blocker.** D22's evidence-gap disposition is sufficiently bounded, ADR-0024 is accepted correctly, and the prior two findings need not be reopened. A new specific Owner decision is not needed to reapprove them.

**Next step:** Owner determines a separately authorized bounded resolution of the selected Markdown static dependency boundary, then obtains verification of that gate and the final selective delivery tree. This review initiates or authorizes no corrective cycle, OCR work or implementation. Do not close Slice 2, stage the proposal or grant owner-commit readiness while the required static gate fails.

Phase 17 **COMPLETE/FROZEN**; Slice 1/D14 **COMPLETE/CLOSED**; D17/D20 **ACCEPTED**; D21 **Markdown-only**; D22 **bounded evidence exception valid**; ADR-0024 **ACCEPTED for Slice 2**; FR18-3 **OPEN**; indexing **INACTIVE**; live originals **DISABLED/DEFERRED**; P18-FINAL **NOT ACCEPTED**; historical Java full gate **FAILED**; Slices 3/4 **NOT AUTHORIZED**; Antigravity **2/2 exhausted**. No production code/test/dependency fix, evidence rewrite, commit, push, tag or PR occurred.
