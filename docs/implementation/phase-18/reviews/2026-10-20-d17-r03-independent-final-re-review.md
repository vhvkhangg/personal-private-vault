# P18-S2-D17 — independent final R03 re-review

**Date: 2026-10-20. Decision: READY_FOR_OWNER_DECISION.**

**D17-FR-01 and D17-FR-02 are independently CLOSED; D17-R03 is CLOSED.** All twelve frozen D17 requirements are verified for their bounded executable machine scope. No outstanding machine-verification failure or blocking implementation finding was found in this review. Genuine human adjudication remains pending. This does **not** grant READY_FOR_OWNER_COMMIT, original broader Slice 2 acceptance, Java publication or Phase 18 acceptance.

## Authority, candidate and evidence integrity

Applied `codex-final-review`, its 29-dimension coverage ledger, Python/RAG, reuse/design and bounded-command rules. The owner's explicit R03-only independent review request is the approved review scope despite the implementer's `READY_FOR_INDEPENDENT_RE_REVIEW` status; no implementation authority is inferred. Read [ACTIVE](../../handoffs/ACTIVE.md), the complete [frozen D17 contract](../obsidian-markdown-d17-contract.md), [owner decisions](../owner-decisions.md), the [preceding independent final re-review](2026-10-20-d17-obsidian-markdown-independent-final-re-review.md), [R03 handback](../slice-2-obsidian-markdown-d17-r03-only-handback-2026-10-20.md), [inventory](../slice-2-obsidian-markdown-d17-r03-only-evidence-2026-10-20.json), [interpretation explanation](../d17-r03-html-interpretation-resolution-2026-10-20.md), [new overlay](../d17-r03-container-interpretation-2026-10-20.json), original frozen sources/gold and actual source/tests/fixtures/diffs.

Working tree: **main / c3b46351507803053850e3c2711602ee9c9ec419**, with inherited uncommitted work. Verified the six changed and four new submitted files, entry-manifest digest, **17 implementer command receipt/log pairs**, all **81 distinct submitted file/artifact hashes**, **2,228 other implementer-entry files**, **165 historical log/artifact hashes**, **29 referenced independent artifacts** and **10 official snapshots**. No mismatch. Independently preserved **2,239 current entry files** before review-status synchronization. Original gold remains SHA-256 `7a0d67bb23e6a3ec4eec59cf65eaa74e48355c6394ea561be582c4fc9cbcb4fe`; previous reviews, overlays and failed receipts are unchanged.

The [independent evidence ledger](2026-10-20-d17-r03-independent-final-re-review-evidence.json), SHA-256 **`048f79f8498254832315df7fcd585681a99bbcc64fe499e9d415f0ae47818eef`**, records exact commands/cwd/budgets/exits, receipts, artifact hashes and a recursive **112-file** source/test/script/configuration manifest. Graphify's older 8,894-node cache was navigation only. Reviewed current files directly, including worker/parser dispatch, chunker/metadata consumers, exceptions, limits, transport, test fixtures and Java admission. The inherited Git diff includes deferred Slice 2 work; it is not misrepresented as the R03-only delta. Untracked parser files were compared against hash-verified accepted-entry snapshots as well as inspected directly.

| Candidate production file | SHA-256 |
| --- | --- |
| `rag-engine/src/ppv_rag/parsers/obsidian.py` | `6dfa91355524da18c18ecfabb7cedc17cedcb2735f70e52feaf5daabae3ee33d` |
| `rag-engine/src/ppv_rag/parsers/obsidian_html.py` | `cd2f2f1c024dd7dd6674c53e4759c0447d5e11edbf442af2ccdd76590cd01172` |
| `rag-engine/src/ppv_rag/embeddings/model.py` | `0761be34112e541ae758e9b4816e27dd46c0d6abb82024e5f8690db0e372389a` |

The new implementation regression file hashes to `463a745ae32f292dc619c08f58e8e3e6f0f7081274fae571c501a30d8d2f4bbb`. The R03 overlay hashes to `1e15ef98cf95ce21e102c078e0481d93381ae34f851cef74d8ebc7b3b9b8476c`.

## Independent dispositions

### D17-FR-01 — CLOSED

`obsidian_html.py:169–172` delegates solidus start tags to the existing normal start-tag path. `_VOID` still decides genuine void termination; non-void tags retain their native context. The same path retains textarea/title RCDATA. No independent solidus-only grammar or new execution capability was introduced.

The implementation's original div/span regressions pass through parser/chunker and real CPU producer JSON. Fresh reviewer sources additionally cover uppercase non-void tags with quoted `>` attributes, inline `i` and `span`, entity-escaped closing text, matching ancestor/end tags, `wbr`, optional `p`/`li` endings, title text, CRLF textarea, root blank blocks and EOF. Literal `[[Ghost]]`, `![[Missing]]` and supplied child markers inside HTML remain authored text with no child edge or material. Genuine void/closed boundaries allow independently approved outside embeds and emphasis to resume.

Fresh active-content roots containing encoded JavaScript URLs, uppercase event attributes, remote/file image references, a solidus script start or CSS URL remain complete failures with zero chunks. Unapproved embeds after an actual closing boundary also fail whole roots. Active-content denial is retained without falsely rejecting script-looking literal text in RCDATA. No filesystem/network child lookup was enabled.

### D17-FR-02 — CLOSED

`obsidian.py:214–241` uses Markdown's adjusted indentation/quote state and passed line limit. It permits blank HTML continuation but stops at a nonempty outdent/sibling or quote sentinel; root-level HTML can still extend to EOF. Exact AST comparison shows `_html_across_blocks` is the **only changed parser function**. HTML module AST comparison, excluding `handle_startendtag`, is identical to its accepted entry snapshot.

Fresh positive cases cover ordered `9)`/`10)` siblings, changed unordered markers, a correctly separated nested ordered list, quote/ordered-list, ordered-list/quote, nested blockquotes, list/quote/list, tabs, CRLF, blank continuation, outdented root headings and matching end tags. The second item and its approved child remain outside the preceding HTML. Missing outside bindings reject rather than produce false SUCCESS; literal inside bindings remain inert.

Verified actual root revision **3** and child revision **19**, complete original source reconstruction, original character/line slices, and dependency edges pointing to the actual parent embed node/source block. A separate check of captured JSON verifies exact expected ancestry for five mixed-container cases, sibling parent equality and the distinction between inner native HTML and outer quoted headings. It checks more than node counts or source-character presence. Parser/chunker probes use budgets **29 and 1,500**; producer output also exercises compact metadata reconstruction.

Deterministic cancellation inside the new `_html_across_blocks` continuation scan propagates `ParsingCancelled` on the sixteenth control check. Scope cleanup permits a following parse. Existing producer/HTTP interruption and deterministic deadline-at-scope-exit regressions pass in the complete group. No successful partial root or changed independent-root batch semantics was observed.

### R01/R02/R04/R05 — earlier closures remain valid

| Finding | Disposition and applicability |
| --- | --- |
| D17-R01 | **CLOSED, bounded CPU scope retained.** Restoring only `ppv-parser-v18.6-d17-html-container-boundaries` to its preceding fingerprint reproduces the accepted embedding-module hash exactly. Model/tokenizer/cache/session/thread/vector/cancellation logic and chunker are unchanged. Reuse the independently verified inference equivalence, list ownership and resource receipts. |
| D17-R02 | **CLOSED.** Intersection functions and chunker are unchanged; current split producer/source regressions pass. Prior independent first/middle/final/disjoint and nested-table proofs remain applicable. |
| D17-R04 | **CLOSED.** Worker/outcome/limit code is unchanged; actual deterministic scope-exit deadline, digest and independent-root tests pass again. Fresh producer batches agree on staged/failed counts and zero chunks for failed roots. |
| D17-R05 | **CLOSED for producer-local representation.** Compaction, expansion, source structures and consumers are unchanged. Reuse the independent decoder's lossless reconstruction and measured output/allocation/cleanup receipts; new boundary JSON retains source/ancestry/revision/edge fidelity. |

The prior million-character repeated source has **689 chunks but only TWO normalized texts**. Its three submitted passes and independent replay remain valid for that workload. Prior independently authored unique-content evidence is **725 distinct chunks / 52.900 seconds**, not universal throughput. No maximum benchmark was rerun: the relevant measured workload paths, cache, chunker, model and limits are unchanged. The new HTML scan is finite and cancellable, and does not execute on those prose workloads. The **60-second deadline**, million-character admission and pinned model identity are unchanged. Prior table amplification, scoped temporary-disk and allocation caveats remain as recorded; no global-zero-OS-write claim is required or made.

## Interpretation adjudication

Independently extracted and hashed the original CommonMark JSON and GFM HTML examples from the frozen snapshots, then read their actual source and official expectations. See the [new seven-record adjudication](2026-10-20-d17-r03-independent-interpretation-adjudication.json), which retains source text, source digest, original official HTML, overlay HTML and reviewer reasoning.

**CommonMark 175 / GFM 144** have exactly:

```markdown
- <div>
- foo
```

Their correct source tree has **two sibling Markdown list items**. The new expectations exactly restore the official two-item HTML; the producer corpus and direct structural tests agree with that independently established boundary. Both previous disputes are resolved.

The other five records are unchanged and independently remain supported: **CM171** is mandatory inert textarea RCDATA; **CM155/GFM125** retain native root HTML across blank blocks/EOF; **CM184/GFM153** retain native root-level indented descendants. The last four are explicit frozen Obsidian-profile interpretations of the official sources, not a claim that their overlay HTML is the unmodified CommonMark/GFM expectation. Source gold and the previously rejected overlay remain immutable.

The earlier **87-label independent audit** remains historical. Its CM171 disagreement was resolved through the explicit corrective record; no new disputed machine interpretation remains here. **Seven supported Codex interpretations are not seven human signatures.** All original human reviewer/signature fields remain null; no held-out or human acceptance was manufactured.

## Complete D17 machine acceptance matrix

All rows below are **VERIFIED for bounded executable requirements**, using the frozen ALL-OR-NOTHING profile, previous independently applicable proofs and this regression/review evidence together. This is not universal grammar proof or completed human quality acceptance.

| ID | Machine disposition | Independent basis |
| --- | --- | --- |
| MD17-01 | VERIFIED | Official CommonMark/GFM producer corpus, mixed Obsidian notes and restored outer HTML/list composition; seven explicit interpretations adjudicated. |
| MD17-02 | VERIFIED | Complete GFM/table tests, irregular/escaped/nested cells, malformed-intent denial; retained independent exact split/cell/intersection and compaction proof. |
| MD17-03 | VERIFIED | Safe source-backed properties/scalars/lists/nesting, aliases/cssclasses and empty maps; malformed/duplicate/unsafe/expanding YAML whole-root rejection retained. |
| MD17-04 | VERIFIED | Ordered/unordered/task/quote nesting plus fresh exact sibling/mixed-container ancestry and outdented headings, CRLF/tab boundaries. |
| MD17-05 | VERIFIED | Wikilinks/display aliases, heading/block selectors and inert ordinary references; missing/ambiguous selection denials; literal HTML links do not become child authority. |
| MD17-06 | VERIFIED | Immutable transclusion manifests/conflicts/cycles/depth/selection tests; fresh inside/outside HTML controls, actual parent nodes/edges and revisions 3/19. |
| MD17-07 | VERIFIED | Current callout/collapsed-body/nested-container fixtures and producer matrix; retained versioned child context. |
| MD17-08 | VERIFIED | Current footnote/comment/tag/highlight tests and original source/metadata reconstruction proofs; no execution or discovery introduced. |
| MD17-09 | VERIFIED | Inert code/fences/Mermaid/math and whole-root dynamic-directive denials; original delimiters/source preserved. |
| MD17-10 | VERIFIED | FR-01/02 closed: non-void/void, open/closed/optional tags, RCDATA, native hierarchy, blank/EOF continuation, owning Markdown boundaries and active denials. |
| MD17-11 | VERIFIED, measured bounds | R01/R02/R05 retained with resource caveats; fresh exact source/line/character/ancestry/revision checks and deterministic new-scan cancellation. |
| MD17-12 | VERIFIED, machine scope | Complete 170-test group, 25 legacy passes, 25 fresh tests; complete-root validation/zero-chunk failures, deterministic outcomes/cancellation and Python 3.14.4/static/dependency checks. |

## Independent commands and receipts

All commands ran from the repository root with the existing watchdog. **Complete exact argv, cwd, timeout, exit, command hash and receipt/log hashes** are in the [evidence ledger's `commands`](2026-10-20-d17-r03-independent-final-re-review-evidence.json). Receipts and reviewer scripts are under `logs/phase18/d17-r03-independent-2026-10-20/`. Each gate uses `python scripts/run-bounded-command.py --profile normal --timeout-seconds N --receipt <receipt> -- <argv>`; Python argv use `rag-engine/.venv-d17/Scripts/python.exe`.

| Receipt | Operation / result | Budget |
| --- | --- | --- |
| `d17-full-gate-final.json` | Exact 15-file D17/R03/worker/transport/domain/chunker/embedding/package group, `-m pytest … -q`: **170 passed in 41.29 s**, no failures/errors/skips | 180 s |
| `compatibility.json` | `test_parsers.py test_slice2_corrections.py` with unchanged explicit deferred-format `-k` selection: **25 passed / 30 deselected in 13.21 s** | 180 s |
| `fresh-final.json` | Reviewer `test_independent_r03.py -q -o pythonpath=rag-engine/src`: **25 passed in 5.83 s**; 22 positive sources, 44 bound/unbound producer attempts plus eight explicit denied roots | 120 s |
| `ancestry.json` | Reviewer `ancestry.py`: exact five mixed-container ancestries and every captured active child edge/parent source locator PASS | 30 s |
| `cancel.json` | Reviewer `cancel.py`: cancellation inside new scan and subsequent context reset/parse PASS | 30 s |
| `producer-corpus.json` | `d17_structural_conformance.py --producer --official … --profile-gold … --resolution … --output …`: **1,267 supported + 62 required whole-root rejections**, all 1,329 attempted | 180 s |
| `ruff-final.json` | `-m ruff check rag-engine`: PASS | 180 s |
| `format-final.json` | `-m ruff format --check rag-engine`: **73 files** already formatted | 180 s |
| `mypy.json` | `-m mypy --config-file rag-engine/pyproject.toml rag-engine/src`: **24 files** clean | 180 s |
| `pip.json` | `uv pip check --python rag-engine/.venv-d17/Scripts/python.exe`: **67 compatible distributions**, Python **3.14.4** | 180 s |
| `lock.json` | `uv lock --directory rag-engine --check --offline`: **73 packages**, PASS | 180 s |
| `dependencies.json` | `dependency_inventory.py --output …/dependencies-results.json`: all 67 installed versions match lock, PASS | 180 s |
| `diff-check.json` | `git diff --check`: PASS; existing CRLF-normalization notices retained | 180 s |

Fresh producer checks run actual parsing → chunking → pinned CPU inference → metadata compaction → **JSON encoding/decoding**. They observe **28 successful roots and 24 whole-root rejections**, with exact source/locator/provenance checks and 384-dimensional output. Grant validation and staging SQL are explicit local capture boundaries; no Java/PG publication is claimed. The official corpus script captures Python producer structures before SQL; it is not incorrectly described as independent JSON serialization proof. Obsidian static syntax/vault producer matrices are exercised by the complete D17 group, rather than relabeling the 1,329 CommonMark/GFM examples as Obsidian examples. The corpus uses shared maintained parsing/HTML libraries; fresh source-authored structural checks and manual source adjudication address that oracle limitation.

The initial reviewer `fresh.json` receipt is preserved as **COMMAND_FAILED / 3 failed, 22 passed**. Its supposed nested ordered list started at `9)` directly inside an existing paragraph; CommonMark permits only start `1` to interrupt that paragraph. The ordinary CommonMark token diagnostic confirms one item, so that expectation was a reviewer error, not a production defect. The original script snapshot and diagnostic are hashed; a required blank line was added only to the reviewer fixture to exercise the intended nested list, followed by the passing final receipt. No production/test/gold fix or failing requirement was weakened. The implementer's earlier **15 failed / 10 passed** reproduction remains historical FAILED.

## Shared 29-dimension coverage ledger

| # | Dimension | Disposition / evidence |
| --- | --- | --- |
| 1 | Intent/scope | PASS — explicit independent R03-only review; no implementation authority. |
| 2 | Traceability | PASS — both residuals closed, twelve machine rows mapped; human/deferred gates distinguished. |
| 3 | Architecture | PASS scoped — frozen protected hashes and D17 boundaries retained; no new authority. |
| 4 | Module ownership | PASS — existing Python parser/worker path; no cross-module implementation change. |
| 5 | Recursive tree | PASS — 112-file manifest/current changed subtree inspection; existing capability layout retained. |
| 6 | Shared contracts | PASS scoped — root `ApiResponse` remains consistent with ADR-0016; Python typed errors/transport unchanged; no new common module. |
| 7 | SOLID/cohesion | PASS — two existing semantic owners corrected; no framework or new interface. |
| 8 | DRY/KISS | PASS — normal start-tag and Markdown container state reused. |
| 9 | Patterns | PASS — no speculative pattern or lifecycle abstraction introduced. |
| 10 | Logic/outcomes | PASS — source hierarchy, zero-chunk denials, staged/failed counts and independence verified. |
| 11 | API compatibility | PASS producer-local — fingerprint honest; legacy tests pass; Java admission deferred. |
| 12 | Validation | PASS — adversarial HTML, missing outside grants, finite limits and correct literal contexts. |
| 13 | Privacy/consent | PASS scoped — only supplied approved children resolve, exact revisions/edges, no discovery. |
| 14 | Secrets/egress | PASS scoped — local pinned CPU model, inert content, sanitized failures; no new fetch/execution. |
| 15 | SQL/concurrency | N/A new SQL scope — no changed DB transaction/publication path; frozen guard proofs retained. |
| 16 | Failure/recovery/cancel | PASS producer-local — cancellation in new scan, context cleanup and current deterministic scope-exit/root isolation tests. |
| 17 | Migrations/JPA | N/A — no D17 migration/entity change; historical aggregate liabilities remain OPEN. |
| 18 | Complexity/resources | PASS bounded — finite/cancellable scan, unchanged admitted limits; measured prior receipts reused with caveats. |
| 19 | Source/locator quality | PASS — exact source, ancestry, source revisions and serialized parent/child edges. Retrieval/QA N/A. |
| 20 | Document fidelity | PASS D17 machine scope — native and Markdown containers agree; other formats remain deferred. |
| 21 | Security tests | PASS scoped — fresh active HTML/unauthorized dependency denials and interruption tests; no OS containment proof demanded. |
| 22 | Test quality | PASS — fresh independently authored positive/negative sources, actual CPU JSON, exact ancestry; reviewer oracle error disclosed. |
| 23 | Performance evidence | PASS retained — no invalidated performance path or unnecessary maximum rerun; no universal 1M throughput claim. |
| 24 | Evaluation independence | PASS machine review; NOT_VERIFIED human adjudication — original gold preserved, seven independently reasoned records, human fields unset. |
| 25 | Dependencies | PASS selected Markdown — Python 3.14.4, installed inventory/lock compatible; model pins unchanged, CUDA unavailable. |
| 26 | Build/static | PASS executed Python/static/lock checks; Java full gate remains historical FAILED. |
| 27 | Observability | PASS — exact commands/receipts/digests and honest capture/resource limits; no invented global-write evidence. |
| 28 | Documentation | PASS scoped — source-hashed interpretation correction, immutable history, dated review/status synchronization. |
| 29 | Hygiene/delivery | PASS scoped — inherited work preserved; only review/status artifacts written, no publishing or implementation fixes. |

## Remaining human/deferred gates and next Owner action

**READY_FOR_OWNER_DECISION**, for D17 Markdown machine verification only. Human source/Obsidian quality adjudication and any required genuine reviewer/signature record remain pending. This report is independent Codex evidence, not a human signature, held-out acceptance, contract waiver or substitute for the owner's bounded decision. No unresolved machine interpretation is being hidden in that human gate.

**Next step:** Owner reviews this evidence and records the bounded D17 decision and genuine human adjudication as required by the frozen package. Do not infer permission to commit/push, resume OCR, implement Java publication, begin another correction cycle or start later slices. No commit message or further implementation authorization is supplied.

Phase 17 **FROZEN**; Slice 1/D14 **CLOSED**; FR18-3 **OPEN**; external indexing **INACTIVE**; live originals **DISABLED/DEFERRED**; original broader Slice 2 **NOT ACCEPTED**; OCR/Office/PDF acceptance **DEFERRED**; Slices 3/4 **NOT AUTHORIZED**. Java still rejects this parser/chunker identity in `RagDocumentFinalizationGate`; future compact-metadata validation/publication belongs to FR18-3 and separately authorized work. Historical Java full gate remains **FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips, exit 1**. No Maven rerun/waiver, dependency upgrade, OCR experiment, implementation fix, commit, push, tag or PR occurred.
