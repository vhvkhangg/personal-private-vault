# D17 Obsidian Markdown — independent blocker review

**Date:** 2026-10-20. **Decision: CHANGES_REQUESTED.** Scope: current uncommitted **D17 Markdown-only** implementation, MD17-01–12. No owner-commit readiness or broader Slice 2 acceptance.

The million-character deadline gate remains FAILED. Independent inspection also establishes invalid split metadata, an HTML context/embedding defect and inaccurate failure accounting. The unexplained native I/O count is **not**, by itself, a temporary-disk acceptance blocker. No acceptance criterion was changed.

## Authority, baseline and evidence integrity

Applied `codex-final-review` and its 29-dimension coverage rubric under the explicitly owner-approved D17 blocker-review scope. [ACTIVE](../../handoffs/ACTIVE.md) was `BLOCKED / OWNER_DECISION_REQUIRED`; this is the requested independent review of that blocked candidate, not permission to implement or reopen a frozen phase. Read the complete [frozen D17 contract](../obsidian-markdown-d17-contract.md), D17 authorization/completion decisions, [initial handback](../slice-2-obsidian-markdown-d17-handback-2026-10-20.md), [corrective handback](../slice-2-obsidian-markdown-d17-corrective-handback-2026-10-20.md), both complete machine-readable evidence inventories, pinned official snapshots, fixtures, tests and actual source/diff.

Actual HEAD remains **c3b46351507803053850e3c2711602ee9c9ec419**, `main`. The tree contains earlier uncommitted Slice 2/takeover/checkpoint changes as well as D17. Compared current files with the checkpoint entry manifests rather than attributing all dirty files to D17. All **18 changed + 7 new** files match the corrective inventory's final hashes before this review's status additions. Independently verified **96 original watchdog receipts** and the initial inventory's paired log hashes; **331 prior artifacts**, **122 historical D17 artifacts**, and **1,660 backend/Phase 17/Slice 1 files** match their entry hashes. All **10 official snapshots** match their freeze inventory. These are integrity checks, not acceptance of the recorded results.

Recursively inventoried **94** source/test/configuration files; inspected the D17 parser, HTML adapter, legacy delegation, element/chunk contracts, chunking, worker, model, limits, resource/conformance tooling and both fixture packages. Reused existing adapters, immutable supplied dependency bindings and supervision. No new worker engine or speculative abstraction is needed. The new `obsidian.py` is large, but the concrete blockers below concern behavior and measured cost rather than a stylistic rewrite. Backend authority, SQL and migration changes are not required by this review.

The [evidence record](2026-10-20-d17-independent-blocker-review-evidence.json) has SHA-256 `30da7f59fa6b4ccaa98aed31e37548ee4f896299f880eda304564cf5758583e3`. It preserves source hashes, independent observations, original resource failures and receipt hashes. The separate [87-label audit](2026-10-20-d17-independent-profile-label-audit.json) records each original source, hash, interpretation and independent disposition. Original labels, handbacks, receipts and frozen contract remain untouched.

## MD17-01 through MD17-12

“Verified” below means the specified source-backed behavior was independently inspected and exercised on the recorded fixtures/oracles; it is not a proof over every possible document. It does not waive the unresolved cross-cutting locator/resource gates.

| ID | Independent disposition | Verified evidence / remaining issue |
| --- | --- | --- |
| MD17-01 | VERIFIED for reviewed grammar; whole-profile acceptance OPEN | Official CommonMark/GFM AST replay, source coverage, breaks/headings/escapes/entities/emphasis/code/links/reference definitions and GFM strike/autolinks. The 87 interpretation labels were separately assessed below; HTML admission is not silently counted as general grammar success. |
| MD17-02 | PARTIAL / OPEN | Exact GFM cell raw spans, row/column/alignment, escaped pipes, ragged implicit/overflow cells, nested ordered-list and quote/list tables verified. Split-table output contains invalid declared intersections: D17-R02. |
| MD17-03 | VERIFIED for reviewed property cases | Original names/source preserved; scalar/list/nested values, empty mappings, dates, leading-zero/scientific numbers, local YAML resolver behavior and duplicate/alias/unsafe/malformed rejection. Legacy/global loader remains unchanged. |
| MD17-04 | VERIFIED for reviewed structures | Actual nested list/quote/task hierarchy, attributes, custom task markers and source ranges, including ordered-list and quote/list tables. Split context metadata remains subject to D17-R02. |
| MD17-05 | VERIFIED for reviewed references | Wiki target/display aliases, heading/subheading/block selection, ordinary inert links, escaped/code/comment inactivity and duplicate/missing selected-anchor failure. No target discovery/fetch. |
| MD17-06 | VERIFIED for reviewed bindings; HTML-context case OPEN | Supplied immutable source/version/payload conflicts, nested edges/order, heading/block selection, cycles/depth/expansion and unapproved embed failures pass. However HTML-looking embed text can resolve when an unclosed HTML container spans a blank line: D17-R03. |
| MD17-07 | VERIFIED for reviewed callouts | Arbitrary type/title, nested containers, expanded/collapsed state, retained folded bodies and independently versioned child edges. No rendering claim. |
| MD17-08 | VERIFIED for reviewed static extensions | Source-backed definitions/references/inline footnotes, comments, tags/nested tags and colored/nested highlights retained; inactive examples do not acquire child authority. Human usability adjudication is not fabricated. |
| MD17-09 | VERIFIED for reviewed inert code/math | Backtick/tilde/indented/longer fences, Mermaid and inline/display math source preserved. Explicit executable query/template directives reject the whole root; no renderer or execution added. |
| MD17-10 | OPEN / CHANGES_REQUESTED | Balanced cross-blank HTML, optional end tags, native hierarchy, entities and lexical source partitions improve and pass. Unclosed HTML context still breaks; CommonMark 171's inert-textarea rejection label is disputed. D17-R03. |
| MD17-11 | FAILED / OPEN | Exact ordinary source/revision and split text offsets, finite caps and cooperative cancellation are credited. Maximum-source deadline still fails; false intersections and output amplification remain. D17-R01/R02/R05. |
| MD17-12 | PARTIAL / OPEN | Markdown-only Python 3.14.4 compatibility, no OCR initialization, complete-root parse before embedding, zero chunks on invalid roots and actual parser/HTTP cancellation pass. Deadline at scope exit falsely increments staged count: D17-R04. No successful partial chunks were observed. |

## Independent interpretation of all 87 labels

The frozen source snapshots, not current web edits or agreement with the implementation, determine this assessment. Checked every label's original source hash and read its source, original expected structure, override/rejection and stated basis. The [per-record audit](2026-10-20-d17-independent-profile-label-audit.json) finds **86 supported interpretations and 1 disagreement**:

- **12 literal-HTML labels:** consistent with the frozen Obsidian rule that Markdown inside HTML stays literal. These particular balanced examples can use native HTML hierarchy instead of the stricter CommonMark/GFM Markdown rendering. This does not cover every unclosed/repair case.
- **6 bare-autolink labels:** consistent with globally composed GFM extensions; URLs/emails remain inert links.
- **2 empty-property labels:** consistent with leading YAML mapping admission and exact source retention.
- **2 wikilink labels:** consistent with D17's explicit lexical `[[...]]` support rather than CommonMark reference-link precedence.
- **2 raw-comment labels:** consistent with the selected newer CommonMark 0.31.2 comment grammar instead of older GFM core grammar.
- **63 required-rejection labels:** **62 supported** by the explicit invalid-property, malformed intended-table, unsupported/malformed/unapproved embed or active/resource-bearing HTML rules. **CommonMark 171 is not supported as a mandatory rejection:** `<textarea>\n\n*foo*\n\n_bar_\n\n</textarea>` is static native HTML source with no executable directive, event handler, fetched resource or binary dependency. An implementation `_ACTIVE` entry cannot itself narrow MD17-10. See D17-R03.

Independent parser/chunker replay reproduces **1,266 supported cases + 63 label-matching rejections**. That is agreement with the supplied oracle, **not 1,329 accepted documents**, nor independent approval of all labels. The disputed rejection and newly seeded cases keep the quality gate open. Most comparisons use original official expected HTML; native-HTML normalization and reconstruction still reuse html5lib/markdown-it, so correlated defects remain possible. The new HTML-context probe demonstrates a gap outside those official examples.

This is an independent **Codex source-based assessment**, not a human signature, Obsidian-renderer comparison or held-out human usability evaluation. The original gold's `human_reviewer`/`signed_at` remain unset; the public-safe exposed vaults remain development evidence. Human adjudication of ambiguous profile behavior is still pending. If the owner intends to exclude otherwise inert raw-text HTML elements, that would require an explicit contract decision; this report does not make that exclusion.

## Findings and root causes

### D17-R01 — High — admitted maximum source cannot reliably complete within the unchanged deadline

**Affected:** MD17-11; `embeddings/model.py:161–219`, `worker.py:150–316`, `limits.py:46–56`.

Original resource evidence remains **58.735 s once**, followed by **61.786 s** and **64.494 s**, with `RESOURCE_LIMIT_EXCEEDED` and zero chunks on both failures. The measured outer wall time includes model initialization; the per-root 60-second scope begins afterwards. These are application deadline failures, not the longer command watchdog expiring. Final-candidate maximum-source success has not been established. I did **not** rerun that million-character workload.

Instead, profiled the same multilingual prose at smaller sizes with the same CPU model/tokenizer and actual parser/chunker/inference code:

| Characters | Chunks | Parse (profiled) | Chunk | Real embedding | JSON encoding |
| --- | --- | --- | --- | --- | --- |
| 50,000 | 35 | 0.183 s | 0.029 s | 2.883 s | 0.021 s |
| 150,000 | 104 | 0.582 s | 0.084 s | 10.090 s | 0.067 s |

Parsing does two complete grammar passes (comment/inert-context scan, then final profile parse). Current measurements scale approximately with input; they do **not** demonstrate a remaining quadratic parser or full-tree-per-chunk defect. Line-prefix splitting already uses a line index/bisection. The dominant observed cost is serial inference: `embed_batch` loops through texts and invokes `_session.run` separately for each chunk, not an ONNX tensor batch. At 150k, embedding is over 93% of these measured stages. Linear extrapolation of that inference sample alone is roughly **67 s per million characters**; this is a diagnostic estimate, not a new maximum-workload receipt or a claim about every host run.

The worker also initializes/hash-loads the same model for each batch request, outside the per-root timer. Existing I/O receipts show roughly 255 MB of reads per workload, consistent with artifact verification/loading; these counters are not proof of physical-disk traffic. JSON serialization occurs after the parsing scope and is absent from its deadline checks, so it cannot explain the recorded pre-stage deadline failures, but it is an additional end-to-end resource boundary to account for.

**Minimal correction/proof:** retain the model, tokenizer, exact-source semantics, size and 60-second criteria. Profile bounded same-model inference batching/session threading and initialization reuse where permitted; choose the smallest measured correction. Prove vector/determinism/token/cancel behavior, then run the unchanged maximum gate on the changed candidate with explicit stage timing and a bounded repeatability check. Do not repeatedly retry this unchanged candidate, raise the deadline or lower the admitted size.

### D17-R02 — High — successful split output publishes reversed “intersection” coordinates

**Affected:** MD17-02/11; `parsers/obsidian.py:449–489`, `chunking/chunker.py:175–201`.

`slice_metadata` correctly retains a complete original source structure on the first slice, but computes an intersection for **every** node without testing overlap. For a node after the slice, `max(start, node_start) > min(end, node_end)`. The emitted interval is therefore impossible. `ORIGINAL_SOURCE_CONTEXT` makes the original node locator legitimate outside a chunk; it does not make a reversed field explicitly named `chunk_intersection` legitimate.

Independent producer-local proof: a 180-row table returns `SUCCESS`, four chunks, and **1,014 reversed intersections**, including `{char_start:371, char_end:370}`. Direct nested ordered/quote-list tables reproduce the same defect with small slice sizes. At 600 table rows, **14,474** such intervals appear in the first definition. Original cell locators/raw slices themselves remain correct in these probes.

**Minimal correction/proof:** retain the once-defined full source context and exact text, but represent disjoint intersections honestly (omit or explicitly mark disjoint, never invent/reverse a span). Validate every derived intersection through actual producer output, including first/last slices and nested cells. This is a pure Python metadata repair, not Java publication work.

### D17-R03 — High — HTML source context can end prematurely and resolve a literal embed

**Affected:** MD17-06/10; `obsidian.py:194–229`, `obsidian_html.py:224–232`, `_ACTIVE` at `:31–56`.

For `<div>\n\n**literal**\n`, the parser emits an HTML opener followed by Markdown `strong_open`, although the unclosed native HTML element extends to EOF under HTML5 repair. More seriously, `<div>\n\n![[Child]]\n` with a supplied approved Child produces **CHILD_MATERIAL** as a child source. D17 requires embed-like text inside native HTML to remain literal. No unauthorized filesystem read occurred; the defect is incorrect structural/dependency interpretation of supplied data.

`_html_across_blocks` extends the block only when `html_extent(..., require_closed=True)` finds a closing extent. Otherwise it retains CommonMark's blank-line boundary, then parses later content as Markdown. Independently parsing separate fragments with html5lib cannot restore the lost enclosing context. Existing balanced/optional-tag fixtures do not cover this path.

The `_ACTIVE` list also rejects inert `<textarea>` and `<title>` source as `MD_UNSUPPORTED_ACTIVE_HTML`; both independently return zero chunks. CommonMark 171's gold label silently makes one such rejection look like conformance. Conservative rejection is preferable to false success, but it is not implementation of the mandatory inert-HTML profile. The profile's executable/unsafe-content rules must remain intact.

**Minimal correction/proof:** preserve native HTML context across blank lines and EOF/implied closures without executing/rendering/fetching; ensure code/comment/embed-looking text remains inert within it. Exercise native hierarchy, exact lexical partition and producer-local child non-resolution. Resolve the disputed label against the frozen contract in a new corrective record; do not edit original gold or add exclusions to hide the defect.

### D17-R04 — Medium — deadline at scope exit reports one root as both staged and failed

**Affected:** MD17-12; `worker.py:314–316`, `:332–337`, `limits.py:54`.

The worker increments `staged_count` **inside** `parsing_scope` before its final checkpoint. If the deadline expires at that checkpoint, the exception path clears chunks, stages a failure and increments `failed_count`, but leaves the staged count incremented.

A deterministic injection expires the deadline immediately before the **actual context-manager exit checkpoint**, leaving worker counter/failure handling unchanged. The one-root response is **staged_count=1, failed_count=1**, while the sole captured stage is `success=false`, `RESOURCE_LIMIT_EXCEEDED`, **zero chunks**. This is controlled boundary fault injection, not a claim that a natural wall-clock race was reproduced. No partial successful chunks were published, but the outcome/report/digest accounting is false.

**Minimal correction/proof:** count a successful root only after complete successful scope exit; add this deterministic expiry boundary alongside normal cancellation/deadline tests. Preserve independent-root batch semantics and existing wire contract.

### D17-R05 — Medium — table output amplification is avoidable, but not quadratic whole-tree duplication

**Affected:** MD17-11; `obsidian.py:449–489`, `:684–710`, `:830–895` and serialization in `db.py:stage_document_result`.

The original near-cap receipt's **61,236 characters → 47,701,900 UTF-8 JSON bytes** is real: about **779 bytes per source character**, including real vectors. Smaller isolated table probes, without repeating the full expensive workload, locate the cost:

| Rows / source characters | Serialized chunk bytes (without vectors) | Nodes | Source cells | Full structure definitions |
| --- | --- | --- | --- | --- |
| 100 / 3,436 | 2,587,693 | 2,528 | 808 | 1 |
| 600 / 20,436 | 15,605,951 | 15,028 | 4,808 | 1 |

At 600 rows, node dictionaries contribute about **10.59 MB**, overlapping chunk-cell records **2.54 MB**, and complete source-cell definitions **2.42 MB**. Repeated node locator values alone contribute about **5.02 MB**; semantic node text only **0.059 MB**. Attribute/meta variants, long source-derived IDs, repeated ancestry/locator dictionaries and cell definitions plus slice copies add substantial redundancy.

The complete structure is currently defined **once**, not once per chunk. Growth here is approximately linear with a very large constant; calling it an exponential leak or unchanged earlier 7.46-GB allocation bug would be unsupported. Structural provenance necessarily expands tiny cells, but this magnitude is **not required by D17**: the contract expressly permits once-defined structure plus references. It is excessive representational amplification, largely independent of model vectors, and serialized output is outside the current parser deadline. The original six-case 753,414,144-byte peak is cumulative working set, not incremental table allocation or proof of a RAM breach.

**Minimal correction/proof:** reduce redundant source-context/locator/cell copies using the existing structure-definition/reference design while preserving all original cell/source relationships. Measure JSON bytes/peak allocation and cancellation/cleanup at the actual serialization boundary, then document finite output resource bounds based on those measurements. Do not invent an arbitrary new MB threshold or drop required provenance. This complements R01/R02; it is not evidence that serialization caused the already recorded million-character deadline failures.

## Temporary disk and native I/O disposition

The original controlled-scratch measurement has a **4,194,304-byte calibration**, sampled size/retention and persistent native directory-change notifications, with zero workload scratch bytes/events. Sampling gaps up to roughly 0.56–1.56 s are reported. Notifications address transient activity during those gaps, but a notification is not a byte-peak measurement if an actual transient event occurs. Here none was reported. No application-created file extraction/cache/scratch path was found in the D17 parser/chunker; native/model initialization is separate and must be described accurately.

The **260 bytes / 24 writes** at first initialization have no established destination. Windows process I/O counters are aggregate accounting, not path-attributed temporary-file sizes: see Microsoft's [IO_COUNTERS definition](https://learn.microsoft.com/en-us/windows/win32/api/winnt/ns-winnt-io_counters) and [standard handles/pipe documentation](https://learn.microsoft.com/en-us/windows/console/console-handles). A reviewer control writes **260 bytes to an anonymous pipe** and records `write_ops +1, write_bytes +260`, with no file involved. This proves the attribution limitation; it does **not** prove that the original 24 writes were pipe writes.

**Disposition:** retain the unknown destination as an observation limit, not a demonstrated scratch leak or a standalone criterion requiring an owner exception. D17 requires bounded relevant temporary usage/cleanup evidence, not proof of zero writes anywhere in Windows. The calibrated controlled-tree evidence is useful for those measured workloads; global OS caches, pipes, diagnostic output and unrelated processes are not a new zero-write obligation. R01 and the other actual blockers still prevent acceptance. A narrowly targeted native-file trace is justified only if a concrete application temporary-file path/event remains suspect; no broad forensic exercise is requested.

## Independent bounded checks

Commands used the existing watchdog, repository root and **`.venv-d17` Python 3.14.4**, without package installation or OCR execution. Full argv/receipt paths and outputs are in the evidence record; independent receipts are `logs/phase18/d17-independent-*-2026-10-20.json`.

| Check | Result |
| --- | --- |
| D17 completion/Obsidian + worker/transport/domain/contracts/chunker/model/package group | **128 passed in 20.90 s**, 0 failures/errors/skips; 180 s watchdog |
| Additional legacy/Markdown/domain compatibility selection | **25 passed, 30 deferred-format cases deselected in 11.94 s**; 90 s watchdog |
| Official parser/chunker AST replay using supplied labels | **1,266 PASS + 63 required rejections**, 90 s watchdog; independently disputed label remains disputed |
| Reviewer syntax/stage profiling and 100/600-row serialization diagnostics | Completed within 120 s watchdog; concrete observations above, not an acceptance PASS |
| Producer-local boundary probes | Actual CPU vectors and captured grant/stage boundaries; typed failures have zero chunks; split metadata/count defects reproduced, 60 s final watchdog |
| Ruff / formatting / mypy | PASS / 69 files formatted / 24 source files clean; each 120 s watchdog |
| `uv pip check --python rag-engine/.venv-d17/Scripts/python.exe` | PASS, **67** installed distributions compatible |
| `uv lock --directory rag-engine --check --offline` | PASS, **73** resolved cross-platform packages |

The selected runtime excludes RapidOCR/OpenCV through the explicitly authorized nondefault dependency group; earlier OCR runtime and failed evidence are preserved. Model/tokenizer identities and existing package versions remain pinned. Actual Markdown requests initialize no OCR. The lock/pip checks and running native CPU inference establish this selected environment's compatibility, not deferred OCR compatibility or universal warning-free status.

Cooperative parser cancellation, actual HTTP cancellation, deadline rejection, expansion rejection and no-partial-chunk failures are independently exercised by the selected tests. They do not certify OS process death; frozen Java supervision retains that authority. No full Java suite or million-character retry was run. The historical Java full gate remains **FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips, exit 1**; its original log/receipt hashes are retained. D14 does not waive D17 or the global gate.

## 29-dimension coverage ledger

| # | Dimension | Disposition / evidence |
| --- | --- | --- |
| 1 | Owner intent/scope | PASS: explicit D17-only blocker review; no OCR or later-slice work. |
| 2 | Requirements/traceability | FINDING: matrix above, R01–05; no silent narrowing. |
| 3 | Architecture/frozen decisions | PASS scoped: D12/D17 application trust preserved; 1,660 protected/backend files unchanged from entry. |
| 4 | Ownership/boundaries | PASS scoped: existing Python adapters/worker reused; no new Java/data authority. |
| 5 | Recursive package/tree | PASS scoped: 94-file manifest and D17 subtree inspection; parser/HTML split has concrete purpose; frozen tree not edited. |
| 6 | Shared contracts/exceptions | FINDING: derived source-span contract, R02. Typed MarkdownFailure remains local; ApiResponse/ADR-0016 root transport unaffected. |
| 7 | SOLID/cohesion | PASS structural design: existing parser/chunker/model separation; fixes need no generic framework. |
| 8 | DRY/KISS/invariants | FINDING: redundant serialization/context and invalid intersections, R02/R05. |
| 9 | Patterns/abstractions | PASS: reuse instance-local grammar/HTML adapters, immutable dependency bindings; no speculative job/plugin engine. |
| 10 | Business/outcome correctness | FINDING: incorrect HTML interpretation and staged/failed accounting, R03/R04. |
| 11 | Wire/API compatibility | PASS unchanged schema; FINDING outcome correctness R04. No Java identity allowlist promotion. |
| 12 | Validation/input bounds | FINDING R02/R03; finite text/node/depth/expansion guards and required negative cases otherwise credited. |
| 13 | Consent/privacy | FINDING: HTML context creates a false dependency, R03. No unauthorized filesystem read; immutable supplied bindings/conflicts and whole-file versus legacy policy verified. |
| 14 | Secrets/egress/artifacts | PASS scoped: no fetching/execution introduced; fixed local model, synthetic captures and sanitized errors. No global zero-I/O or OS confinement claim. |
| 15 | Transactions/concurrency | N/A new Java/SQL behavior: publication/locking/CAS deferred/frozen. Deterministic cancellation boundary exercised. |
| 16 | Failure/recovery/cancel | FINDING R04; actual parser/HTTP cancel and zero chunks pass. Serialization boundary evidence included in corrective recommendation. |
| 17 | Database/migrations | N/A D17 change: backend/schema unchanged; FR18-3 OPEN, no DB publication acceptance. |
| 18 | Complexity/resources | FINDING R01/R05: measured serial inference bottleneck and large linear metadata coefficient, not invented quadratic claims. |
| 19 | Locator/data quality | FINDING R02/R03. Exact source/cell slices and revision boundaries otherwise verified; retrieval/QA N/A. |
| 20 | Document fidelity | FINDING R03; detailed matrix credits tested static syntax, native hierarchy and dependency semantics. OCR/Office/PDF acceptance deferred. |
| 21 | Security testing | PASS scoped tested grants/conflicts/active content/ingress/cancel; FINDING inert HTML context R03. No superseded native anti-tamper proof demanded. |
| 22 | Test quality | FINDING: 128+25 pass but missed invalid intersections/EOF HTML/exit-expiry; independent seeded probes supplied. |
| 23 | Performance tests | FINDING R01/R05; failed originals retained, smaller diagnostics have explicit purpose and limits. |
| 24 | Evaluation/independence | FINDING: all 87 independently assessed; one disagreement. Development oracle shares libraries; unsigned human package remains unsigned. |
| 25 | Dependencies/lifecycle | PASS selected Markdown scope: Python 3.14.4, 67 compatible packages, offline lock, explicit deferred OCR group; no version novelty/model change. |
| 26 | Build/static/tooling | PASS executed scope: bounded pytest/static/lock/pip; no claim that unchanged Java full gate is green. |
| 27 | Observability | FINDING R04 reporting; process working set/device-wide VRAM/counter attribution and sampling limits accurately distinguished. |
| 28 | Documentation/governance | PASS preserved historical contract/receipts; current independent CHANGES_REQUESTED status recorded separately. No human signature invented. |
| 29 | Delivery/hygiene | PASS scoped: no production/test/fixture fix or publishing; intentional review/evidence artifacts only, user dirty work preserved. |

## Minimal finite corrective scope and next step

1. Correct derived intersections and final-success accounting, with small deterministic producer regressions (R02/R04).
2. Correct native HTML context through EOF/blank blocks; preserve safe raw-text HTML, frozen active-content denials and child non-resolution. Resolve CommonMark 171 explicitly; preserve original gold/history (R03).
3. Profile and correct the measured inference/output bottlenecks using the same pinned artifacts and existing structure-reference design (R01/R05). Keep all current size, deadline, source and fidelity criteria. Measure serialized-output allocation/cleanup rather than demanding global zero OS writes.
4. After actual changes, rerun the bounded focused/compatibility/corpus checks and the unchanged maximum gate, then obtain separate independent review. Continue to distinguish measured source semantics from pending human adjudication.

**Next step:** owner authorizes this finite D17-only `$codex-implementation-takeover` corrective session, followed by a separate independent re-review. Historical Antigravity 2/2 remains exhausted; this review starts no automatic cycle. No owner decision is needed merely to prove zero global OS writes, and no contract relaxation is recommended. If the owner wants a new inert-HTML exclusion or different size/deadline criteria, that would be a separate explicit decision, not an implementation workaround.

**Preserved:** Phase 17 FROZEN; Slice 1/D14 CLOSED; FR18-3 and aggregate Java/PG obligations OPEN; original Java full gate FAILED; external indexing INACTIVE; OCR development stopped and deferred-format evidence preserved. No Slice 3/4 authorization, broader Slice 2/Phase 18 acceptance, fixes, commit, push or tag. No commit message while blockers remain.
