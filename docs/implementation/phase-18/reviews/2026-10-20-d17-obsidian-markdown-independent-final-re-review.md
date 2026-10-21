# P18-S2-D17 — independent final re-review

**Date: 2026-10-20. Decision: CHANGES_REQUESTED.** Independent review only, of the current uncommitted D17 candidate.

**D17-R01, R02, R04 and R05 are independently CLOSED for their bounded producer-local requirements. D17-R03 remains OPEN.** Two reachable HTML-context defects still prevent full-profile machine acceptance. Five corrective interpretations are supported; two list-boundary interpretations are not accepted. This is not READY_FOR_OWNER_DECISION or broader Slice 2 acceptance.

## Scope, authority and integrity

Read [ACTIVE](../../handoffs/ACTIVE.md), the complete [D17 contract](../obsidian-markdown-d17-contract.md), [owner decisions](../owner-decisions.md), the [prior independent blocker review](2026-10-20-d17-obsidian-markdown-independent-blocker-review.md), [final corrective handback](../slice-2-obsidian-markdown-d17-final-corrective-handback-2026-10-20.md), its [inventory](../slice-2-obsidian-markdown-d17-final-corrective-evidence-2026-10-20.json), earlier inventories/audit, corrective interpretation overlay, current source, tests, fixtures and actual diff. Applied `codex-final-review`, the shared 29-dimension rubric and applicable Python/RAG, reuse/design and bounded-command rules. This expressly requested independent scope permits reviewing `READY_FOR_INDEPENDENT_RE_REVIEW`; it grants no implementation authority. Implementation authorship belongs to the separate Codex corrective session.

Actual baseline remains **main / c3b46351507803053850e3c2711602ee9c9ec419**, with inherited dirty Slice 2/D17 work. Verified **10 changed + 5 new** implementation/status files against their submitted final hashes, the entry-manifest hash, **43 original bounded-command receipt/log pairs**, **98 historical log/receipt hashes**, **29 referenced independent artifacts**, **10 official snapshots**, and **1,687 protected backend/Phase 17/Slice 1/review files** at review entry. No mismatch. The 247 final-file/artifact/command hash entries and historical checks were verified independently rather than accepting the inventory's counts. Recursively inventoried **111** source/test/script/configuration files; examined the changed Python paths and their actual consumers. Graphify supplied navigation only; current source controls findings.

The [independent evidence record](2026-10-20-d17-independent-final-re-review-evidence.json), SHA-256 **`888c0de9e62bbeb944bdc9664b1c0073e4c1c7eb878af0befbf8396578e02ecf`**, contains exact independent argv/cwd/timeouts/exits, raw observations and hashes. The [seven-record adjudication](2026-10-20-d17-final-interpretation-adjudication.json) preserves each original source/expectation and the separate reviewer disposition. Original gold, reviews, failed receipts and model/tokenizer/dependency pins were not edited. Only new review/evidence and canonical review-status/index entries were written by this session.

## D17-R01–R05 dispositions

### D17-R01 — CLOSED, with measured performance limits

`embeddings/model.py:147–215` keeps one ORT session per pipeline, reused by singleton calls and across roots in the existing worker batch. Four intra-op threads, one inter-op thread and sequential execution are explicit. Model initialization remains per worker request; there is no process-global content/session cache. `embed_batch` caches only exact **NFKC(text.strip())** keys within that call, validates a newly computed vector before caching, checks cancellation on misses and hits, and returns a distinct list for every occurrence. Normalization semantics predate this correction. Source/chunk identity, content and consent/dependency provenance remain separate and are not deduplicated.

Independent verification used **27 inputs / six distinct normalized texts**, including canonical/compatibility Unicode variants and multilingual strings. Six singleton inferences produced the cached batch. A **separate one-thread CPU ORT session**, with independently implemented original singleton pooling and L2 normalization, produced **bitwise-identical vectors, maximum coordinate difference 0.0**. All output lists had distinct identities; mutation of one did not alter another. A fresh `embed_batch` call inferred again, proving no cross-call result reuse. Cancellation during cache hits raised at the twentieth control check. This is substantially stronger than checking shape or a relaxed cosine threshold. Tensor batching was not promoted; the pinned graph/model/tokenizer and 384-dimensional space remain unchanged. CUDA remains UNAVAILABLE, not a fabricated GPU recovery PASS.

Reconstructed the unchanged acceptance source and checked its recorded identity: **1,000,000 characters**, SHA-256 `749df549cb2aa8f9aaa90800e1fc1064bde1bcaded850a2edfef4ac1cf134cfd`, **689 chunks / TWO distinct normalized texts**, maximum 354 tokenizer tokens. The three fresh-process submitted receipts pass at **4.216 / 3.786 / 3.692 s**. An independent fresh-process replay passes at **4.091 s**, with exact original source, 689 chunks and the unchanged 60-second root scope. These receipts demonstrate the repeated-text case, not universal million-character performance.

To test generalization, ran new deterministic English/Vietnamese notes with changing topics, vocabulary, record identifiers and numeric observations. Actual parser→chunker→real CPU inference→compaction→JSON capture ran under the unchanged worker deadline, with no injected vectors or increased admission limits:

| Characters | Chunks / distinct normalized texts | Actual singleton calls | Parse | Chunk | Inference | Outer wall |
| --- | --- | --- | --- | --- | --- | --- |
| 100,000 | 74 / 74 | 74 | 0.204 s | 0.058 s | 4.446 s | 6.240 s |
| 350,000 | 255 / 255 | 255 | 0.295 s | 0.194 s | 16.373 s | 18.637 s |
| 1,000,000 | 725 / 725 | 725 | 1.808 s | 0.591 s | 48.167 s | **52.900 s** |

All three succeed with exact source. The million-character root's reported processing duration is **51.098 s**, maximum 331 tokens/chunk; UTF-8 serialized output is **12,676,551 bytes**, encoding **0.240 s**. Instrumentation also computes distinct counts/token maxima, so these are diagnostic measurements rather than a claim of uninstrumented best-case speed. Workloads were sequential. Process peak working set across that process is **744,693,760 bytes**; host headroom at the final boundary is **576,380,928 bytes**, a shared time-dependent host metric. Device-wide VRAM is not attributed to CPU inference.

The large reuse gain does not generalize to unique content: cache hits are absent in these three notes. The unique-content run nevertheless demonstrates useful bounded behavior below 60 seconds on this host. It has much less timing margin than the repeated fixture, and is not a universal or repeatability guarantee for arbitrary document/token distributions. The fail-closed deadline remains mandatory. These limits are retained rather than reopening a measured, satisfied R01 or inventing a new universal throughput criterion. Historical failed runs remain FAILED for their original candidates.

### D17-R02 — CLOSED

`obsidian.py:452–489` now emits `chunk_intersection` only for a strictly nonempty overlap, and explicit null for disjoint original context. Complete original context remains defined once; null does not remove the original source-backed node.

Independently checked **12 combinations** of plain CRLF tables, quote/list-nested tables, ragged/escaped/overflow cells and the approved multi-revision vault, using budgets **97, 1,500 and 10,000** characters. These cover unsplit and first/middle/final slices. Every source/cell excerpt, line/character boundary and root/Child/Grand revision remained correct. Nonempty intersections equal the actual max/min overlap; disjoint intersections are null. No reversed or invented interval remained in those serialized/reconstructed cases. The actual serialized producer regressions also pass.

### D17-R03 — OPEN; corrected cases credited, two residual findings below

Ordinary unclosed root HTML now spans blank blocks through EOF. Optional end tags, textarea/title RCDATA, entities, EOF and self-closing textarea cases pass the recorded positive probes. Supplied child material remains absent inside these corrected contexts. Executable HTML outside RCDATA and unapproved embeds still fail whole roots with zero chunks. Literal `<script>` text inside RCDATA is not incorrectly treated as an executable child tag.

However generic non-void solidus handling and Markdown list-container termination still contradict the frozen source-tree semantics. Both defects reach actual successful producer output. CM171's original disagreement is resolved; the two list-boundary overlays are not independently accepted.

### D17-R04 — CLOSED

`worker.py:321–325` counts success only **after** `parsing_scope` completes its final exit check. Independently expired the real scope deadline immediately before exit, while leaving counter/failure/staging logic unchanged. The failed first root has `success=false`, `RESOURCE_LIMIT_EXCEEDED`, zero chunks; the independent second root succeeds. Response counts are **staged=1 / failed=1**, and the digest equals SHA-256 of the actual job/attempt/count tuple. Implementer regressions additionally cover a sole failed root, status and receipt consistency. This is deterministic fault injection, not a timing-race claim. Partial outcomes for independent roots remain valid; `STAGED` can describe staging a failure outcome and is not an indexing/publication success assertion.

### D17-R05 — CLOSED for producer-local encoding and consumers

`compact_metadata`/`expand_metadata` in `obsidian.py:500–610` use versioned block-local references, exact locator interning/default inheritance and existing source-cell identities. Parser/chunker dictionaries remain uncompressed. Producer metadata declares `block-local-references-v1`; unknown Java pipeline identities remain unapproved.

Wrote a **separate decoder**, not a call to the implementer's inverse, and compared every reconstructed JSON value with the original JSON representation across the same 12 split/unsplit/nested/vault combinations. All match exactly: node order/defaults, attributes, ancestry, original source/revision, cells, raw excerpts, implicit/overflow roles and locators. Definitions are supplied across slices; references resolve to the existing block/source/revision. JSON arrays are correctly compared as JSON arrays: two initial reviewer probes incorrectly compared Python tuple attribute pairs to serialized arrays. Their failed receipts are preserved and corrected, and are not reported as production defects.

Consumer inspection: current parser/chunker clients retain their legacy dictionaries; the conformance producer consumer expands the new representation before semantic comparison; the independent decoder verifies full reconstruction including later slices. SQL staging stores free-form JSON. Java `RagDocumentFinalizationGate` still rejects the new parser/chunker identity, and does not yet verify this encoding. **No enabled Java consumer is claimed to understand/admit it.** Java verification/interpretation remains a future FR18-3/Slice 3 obligation, not a reason to modify Java in this slice or assert publication readiness.

Independent output/allocation measurements on the same current chunk structures, excluding vectors:

| Rows / characters | Original JSON | Compact JSON | Traced serialization-region peak, original→compact | Serialization/compaction with tracing, original→compact |
| --- | --- | --- | --- | --- |
| 100 / 3,436 | 2,538,485 B | 1,125,620 B | 5,077,892→3,868,498 B | 0.199→0.330 s |
| 600 / 20,436 | 15,121,588 B | 6,652,173 B | 30,247,386→22,946,668 B | 1.191→1.897 s |

After release/collection, the compact 600-row measured region retains **3,348 traced bytes**; this is not a native-allocator release guarantee. Process cumulative peak before later model probes is **179,548,160 bytes**, not incremental table allocation. Compaction reduces output by about 56% and measured serialization-region peak by about 24%, with a measured CPU/dictionary-allocation tradeoff. It remains verbose linear provenance, not a new quadratic tree-copy claim. The submitted full resource receipt reports **20,763,337 bytes** for the unchanged near-cap table versus historical **47,701,900**; its hash is verified. No arbitrary MB threshold or dropped source fidelity was used.

Controlled scratch remains a relevant scoped measurement: independent repeated-source replay calibrates **4,194,304 bytes**, observes zero workload scratch events/retention, and reports a **0.627 s** maximum sample gap. Native notifications supplement sampled sizes. This does not prove zero global Windows writes or universal instantaneous native-memory cleanup; neither is required. The previous unexplained I/O observation is not reintroduced as a standalone blocker.

## Residual findings and seven-interpretation adjudication

### D17-FR-01 — High — non-void `/>` prematurely ends native HTML context

**Residual of R03; affects MD17-06/10.** `obsidian_html.py:169–175` treats a start/end tag as closed except for textarea/title. For other non-void HTML elements, the lexical extent and native HTML5 meaning disagree. HTML's trailing solidus does not close a non-void HTML element; see the [HTML parsing rule](https://html.spec.whatwg.org/multipage/parsing.html#parse-error-non-void-html-element-start-tag-with-trailing-solidus). This explains the existing native-context requirement; it does not adopt a new Markdown profile.

Actual JSON-serialized producer cases:

```text
<div/>

![[Child]]
```

returns `SUCCESS`, **three chunks**, including supplied **CHILD_MATERIAL**. `Before <span/>**literal** ![[Child]] after.` returns `SUCCESS`, parses `strong_open` and emits child material. The source should remain in its native HTML context; alternatively an unsupported malformed structure must fail the whole root under the existing failure rule, not return successful false structure/dependencies. The already repaired `<textarea/>` case shows this distinction was recognized but applied incompletely.

The child was explicitly supplied/approved; this is **not** an observed unauthorized filesystem/network read or exfiltration. It is a deterministic structural/dependency fidelity defect violating “literal embeds inside native HTML never resolve child material.”

**Smallest correction/proof:** make lexical native extent agree with native non-void/void handling, keep active-content denials, and add parser→chunker→serialized-producer regressions for block and inline non-void solidus cases with and without a child binding. No new grammar framework, worker or Java authority is necessary.

### D17-FR-02 — High — HTML lookahead consumes sibling/list-external Markdown; two overlays conceal the discrepancy

**Residual of R03; affects MD17-01/04/06/10.** `obsidian.py:214–231` extends HTML using `state.getLines(start, end, state.blkIndent, True)` through the passed end, without preserving the list item's indentation/sibling termination that bounded the original HTML block. The root-EOF repair therefore also swallows source outside the owning Markdown item.

Independently reproduced in actual successful producer output:

- `- <div>\n- foo\n` has **one** `list_item_open`; `- foo` is embedded as native HTML text rather than a sibling item.
- `- <div>\n  native\n\n# Outside heading\n` has **no heading node**; the outdented heading becomes literal HTML within the item.
- `- <div>\n- ![[Child]]\n` swallows the following sibling note embed as HTML instead of preserving its separate item/binding.

The equivalent blockquote boundary probe correctly returns to outside Markdown, demonstrating that owning source-container boundaries are meaningful and this is not a blanket requirement to parse Markdown inside HTML. Authored characters remain present, but character equality alone does not establish the required structural fidelity.

Independently inspected all seven source-hashed [corrective interpretations](../d17-commonmark-171-corrective-resolution-2026-10-20.json), preserving original gold. Detailed [adjudication](2026-10-20-d17-final-interpretation-adjudication.json):

| Cases | Disposition | Basis |
| --- | --- | --- |
| CommonMark 171 | **SUPPORTED** | Static textarea RCDATA is mandatory inert content, not an active-HTML rejection. Original disputed label is explicitly superseded by the corrective record, not overwritten. |
| CommonMark 155 / GFM 125 | **SUPPORTED** | Root-level open HTML retains literal Markdown through EOF/blank lines. |
| CommonMark 184 / GFM 153 | **SUPPORTED** | Root-level indented/native descendants remain source inside the open HTML container. |
| CommonMark 175 / GFM 144 | **DISAGREE — NOT ACCEPTED** | Official sources have two sibling list items. The overlay changes these to one item by treating the second marker as literal HTML, matching the overconsumption defect rather than preserving the owning Markdown container. |

The frozen literal-HTML rule does not by itself authorize changing outer/sibling list structure. Those two changes need correction and genuine adjudication, not automatic approval because a source-hashed expected result matches the implementation. If the owner intends a different Obsidian-specific container rule, it requires an explicit contract decision and evidence; this review does not supply it.

**Smallest correction/proof:** bound HTML continuation by the owning Markdown source container while retaining valid root-level EOF/blank-block continuation. Add sibling/outdent/heading/approved-embed cases to real producer regressions; preserve original labels and resolve the two disputed overlays in a separate reviewable record. Keep the five supported interpretations and other already verified repairs.

## MD17-01–12 acceptance matrix

These are bounded machine-verification dispositions, not universal grammar proof or human signatures. A row's verified subcases do not override the all-or-nothing profile gate.

| ID | Independent disposition | Evidence / remaining condition |
| --- | --- | --- |
| MD17-01 | PARTIAL / OPEN | Core block/inline grammar, extensions and official source corpus exercised; combined HTML/list interpretation remains disputed in FR-02. |
| MD17-02 | VERIFIED, machine scope | Plain/quoted/list-nested GFM cells, alignment, escapes, ragged/overflow rules and malformed-intent rejection; exact serialized split attribution and lossless compaction verified. |
| MD17-03 | VERIFIED, machine scope | Properties/scalars/lists/nesting/aliases/cssclasses/source lexemes; safe YAML and whole-root invalid/duplicate/alias-expansion rejection. Legacy resolver remains unchanged. |
| MD17-04 | PARTIAL / OPEN | Ordinary ordered/unordered/task/quote nesting and source ranges pass; HTML steals sibling and outdented structures, FR-02. |
| MD17-05 | VERIFIED, machine scope | Wikilinks/display aliases, heading/subheading/block selectors, inert ordinary links, missing/ambiguous-anchor failures and source spans. |
| MD17-06 | PARTIAL / OPEN | Immutable bindings/conflicts, cycles/depth/expansion, nested edges and ordinary selected children pass. HTML contexts can wrongly add or suppress dependency interpretation, FR-01/02. |
| MD17-07 | VERIFIED on reviewed fixtures | Arbitrary/collapsible/nested callouts, retained folded bodies and versioned dependency context. Container-related HTML defects remain separately open. |
| MD17-08 | VERIFIED, machine scope | Footnote definitions/references, comments, tags/nested tags, highlights/colors and inert examples survive source/metadata reconstruction. |
| MD17-09 | VERIFIED, machine scope | Inert fences/indented code/Mermaid/math, long fences and dynamic directive rejection; no rendering/execution introduced. |
| MD17-10 | OPEN / CHANGES_REQUESTED | Ordinary EOF/blank HTML, optional tags and RCDATA repairs credited; generic non-void and Markdown-container boundaries still fail. Five overlay interpretations supported, two disputed. |
| MD17-11 | Resource/intersection/encoding gates VERIFIED; profile fidelity OPEN | R01/R02/R05 close on measured workloads and exact source/revision checks. Incorrect HTML source-tree semantics still prevent overall fidelity acceptance. |
| MD17-12 | Failure machinery/compatibility VERIFIED; overall profile OPEN | Scope-exit counts/digest and independent-root outcomes pass, invalid roots have zero chunks, cancellation and Python 3.14.4 checks pass. Incorrectly admitted HTML cases remain successful, so whole-profile acceptance is not established. |

## Independent checks and receipts

All bounded commands ran from the repository root. The [evidence ledger's `commands`](2026-10-20-d17-independent-final-re-review-evidence.json) supplies **complete exact argv, cwd, command digest, timeout, exit and receipt hash** for every command. Each invocation used `python scripts/run-bounded-command.py --profile normal --timeout-seconds N --receipt <path> -- <argv>`. Python argv use `rag-engine/.venv-d17/Scripts/python.exe`.

Receipts below are under `logs/phase18/`, with prefix **`d17-final-independent-`** and suffix **`-2026-10-20.json`**:

| Receipt stem | Exact operation (full file lists/arguments in ledger) | Limit | Observed result |
| --- | --- | --- | --- |
| `pytest` | `-m pytest` D17 final/completion/Obsidian + worker/transport/domain/contracts/chunker/embedding/package group `-q` | 180 s | **144 passed in 34.20 s**, no failures/errors/skips |
| `compatibility` | `-m pytest test_parsers.py test_slice2_corrections.py -k <unchanged explicit deferred-format exclusion> -q` | 90 s | **25 passed, 30 deselected in 14.50 s** |
| `corpus` | `rag-engine/scripts/d17_structural_conformance.py --producer --official ... --profile-gold ... --resolution ... --output ...` | 120 s | **1,267 supported + 62 required rejections** under the submitted overlay; this does not approve its two disputed labels |
| `performance` | `logs/phase18/d17-final-independent-performance.py` | 210 s | Vector/cache equivalence, ownership/cancel and three real unique-content workloads complete |
| `structure-final` | `logs/phase18/d17-final-independent-structure.py` | 120 s | Separate decoder/source invariants, allocation, producer denials/HTML observations and deterministic exit-deadline checks complete |
| `html` | `logs/phase18/d17-final-independent-html.py` | 60 s | Actual CPU producer reproduces FR-01/02 and positive HTML controls |
| `frozen-max` | `rag-engine/scripts/d17_completion_resources.py --only maximum-source-1000000 --output logs/phase18/d17-final-independent-frozen-max-results-2026-10-20.json` | 45 s | PASS, exact repeated source / 689 chunks, 4.091 s; scoped scratch calibrated/clean |
| `ruff` | `-m ruff check rag-engine` | 120 s | PASS |
| `format` | `-m ruff format --check rag-engine` | 120 s | **72 files** already formatted |
| `mypy` | `-m mypy --config-file rag-engine/pyproject.toml rag-engine/src` | 120 s | **24 source files** clean |
| `pip` | `uv pip check --python rag-engine/.venv-d17/Scripts/python.exe` | 120 s | Python **3.14.4**, **67** compatible installed distributions |
| `lock` | `uv lock --directory rag-engine --check --offline` | 120 s | **73** resolved packages, PASS |

Reviewer scripts and raw result JSON remain in ignored logs and are hashed. Diagnostic exit 0 means observations completed; it does not convert the reproduced HTML failures into conformance PASS. Grant verification and staging are explicit capture boundaries; actual CPU inference and JSON serialization are exercised, not trusted Java/PG publication. No Maven clean gate, OCR experiment or package installation was run. `git diff --check` passes with existing CRLF-normalization notices; no warning-free IDE/compiler claim is made.

## Remaining adjudication and 29-dimension ledger

This report completes an independent **Codex** assessment of the submitted executable evidence and seven interpretations. It is not a human signature or held-out acceptance. Original human review fields remain unset. Ambiguous source/Obsidian usability and the two disputed container interpretations require genuine adjudication; matching an implementer's expected JSON/HTML does not substitute for it. The previous 87-label audit stays historical; CM171 is now supported through its explicit corrective overlay. No new human label or signature was invented.

| # | Dimension | Disposition / evidence |
| --- | --- | --- |
| 1 | Owner intent/scope | PASS: explicit D17-only independent review; no implementation authority inferred. |
| 2 | Traceability | FINDING: R03/FR-01/02 and affected matrix rows remain open. |
| 3 | Architecture/frozen baselines | PASS scoped: application-level D12/D17 trust preserved; protected hashes match. |
| 4 | Ownership/boundaries | PASS scoped: existing Python producer/metadata reused; no new Java/SQL authority. |
| 5 | Recursive package/tree | PASS scoped: 111-file inventory; changed parser/HTML/model/worker/consumers inspected; frozen tree unchanged. |
| 6 | Shared contracts/exceptions | PASS producer-local: typed errors and lossless encoding; ApiResponse/ADR-0016 root transport unchanged. Future Java decoding remains FR18-3. |
| 7 | SOLID/cohesion | PASS: concrete parser/chunker/model/encoding roles; no new framework needed for residual fixes. |
| 8 | DRY/KISS/invariants | PASS reuse/compaction; FINDING remaining native/Markdown boundary disagreement. |
| 9 | Patterns | PASS: narrow call-local cache and existing block references solve measured problems; no global cache/lifecycle framework. |
| 10 | Business/outcomes | PASS corrected counters/digest; FINDING successful false HTML structure/dependencies. |
| 11 | API/wire compatibility | PASS scoped legacy/domain regression and unchanged public wire; Java identity admission remains disabled. |
| 12 | Validation | FINDING HTML boundaries; existing source/node/token/expansion/deadline and negative checks credited. |
| 13 | Privacy/consent | FINDING wrong dependency interpretation in supplied approved data; no unauthorized disk read demonstrated. Legacy field policy preserved. |
| 14 | Secrets/egress/artifacts | PASS scoped: local pinned model, call-local reuse, sanitized errors, no fetch/execution added; no OS confinement claim. |
| 15 | Transactions/concurrency | N/A new SQL/publication scope. Deterministic cancellation/deadline and independent roots pass; frozen Java guards untouched. |
| 16 | Failure/recovery/cancel | PASS producer-local mechanisms, exit accounting and scoped cleanup; no process-death or new Java recovery acceptance. |
| 17 | Database/migrations | N/A D17 changes: no Java/Flyway/SQL mutation; historical aggregate PG issue remains open. |
| 18 | Complexity/resources | PASS measured bounded CPU/cache/encoding requirements; finite limits and diagnostic caveats retained. |
| 19 | Source/locator quality | PASS exact reconstruction/intersections; FINDING HTML source-tree semantics. Retrieval/QA N/A. |
| 20 | Document fidelity | FINDING MD17-04/06/10; other tested static syntax credited. Deferred OCR/Office/PDF not reopened. |
| 21 | Security tests | PASS applicable denials/ingress/cancellation; FINDING literal HTML dependencies. No superseded native anti-tamper proof required. |
| 22 | Tests/evidence quality | FINDING: 144+25 green and corpus agreement miss independently seeded HTML cases; raw observations preserved. |
| 23 | Performance tests | PASS bounded representative checks; three implementer repeats plus independent repeated and fully unique workloads distinguished. No universal throughput claim. |
| 24 | Evaluation/independence | FINDING: seven independently assessed, two not accepted; shared-library corpus oracle and unsigned human evidence explicitly limited. |
| 25 | Dependencies/lifecycle | PASS selected Markdown environment: Python 3.14.4, pinned 67 packages/model, offline lock; CUDA unavailable and OCR deferred. |
| 26 | Build/static/tooling | PASS executed Python/static/lock/pip checks; historical Java gate remains FAILED. |
| 27 | Observability | PASS corrected counts/digest, honest CPU/RAM/device/scratch/allocation measures; no global-zero-write claim. |
| 28 | Docs/governance | FINDING pending label disposition; immutable evidence/author attribution preserved, current review status synchronized separately. |
| 29 | Hygiene/delivery | PASS scoped: no production/test/gold fixes, publishing or generated model tracking; intentional reviewer artifacts only. |

## Decision and next owner action

**CHANGES_REQUESTED: D17-R03 remains OPEN**, specifically FR-01/FR-02. The full frozen D17 profile cannot be accepted while executable HTML/dependency and container-boundary requirements remain unmet. Four independently verified corrections are credited and should not be reopened merely to repeat unchanged gates.

**Next step:** the owner may explicitly authorize a **separate, finite D17-R03-only `$codex-implementation-takeover` correction**, then a separate independent re-review. Required proof is the small block/inline solidus and sibling/outdent/container producer regression set, truthful interpretation disposition, applicable corpus/static/compatibility checks and evidence preservation. Reuse the R01/R02/R04/R05 receipts unless related changes invalidate them. This review authorizes no remediation or automatic new round; historical Antigravity 2/2 remains exhausted. No commit message is supplied.

Phase 17 **FROZEN**; Slice 1/D14 **CLOSED**; FR18-3 **OPEN**; external indexing **INACTIVE**; original broader Slice 2 **NOT ACCEPTED**; OCR/Office/PDF acceptance **DEFERRED**; Slices 3/4 unauthorized. Historical Java full gate remains **FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips, exit 1**, with its failures preserved and no D14/global waiver. No fixes, frozen-requirement changes, commit, push, tag or publishing were performed.
