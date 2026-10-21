# D17-R03-only corrective implementation handback — 2026-10-20

**READY_FOR_INDEPENDENT_RE_REVIEW — Codex corrective implementation authorship.** Scope is exactly
D17-FR-01/02 from the [independent final re-review](reviews/2026-10-20-d17-obsidian-markdown-independent-final-re-review.md).
Both have passing implementation and actual CPU producer-local evidence. This is not independent
closure, human adjudication, broader Slice 2 acceptance or owner-commit readiness. Stop here for
separate independent re-review; no automatic additional correction is authorized.

## Authority, baseline and preservation

The owner explicitly authorized this one finite R03-only correction. **D17-R01/R02/R04/R05 retain
their independently CLOSED dispositions**; their accepted performance, intersection, accounting and
serialization evidence is reused. HEAD/branch remain `c3b46351507803053850e3c2711602ee9c9ec419` / `main`.
The inherited dirty tree was inventoried before editing; historical reviews, gold, adjudications,
handbacks, receipts, backend/Phase 17/Slice 1 files and other uncommitted work remain preserved.

Historical Antigravity allowance remains **2/2 exhausted**, without a reset: [2026-10-18 round 1](reviews/2026-10-18-phase-18-codex-final-re-review-1.md)
and [2026-10-18 round 2](reviews/2026-10-18-phase-18-codex-final-re-review-2.md) each returned
CHANGES_REQUESTED with their stated FR18-1–11 residuals. Initial implementation was not a counted
remediation cycle. This owner-authorized R03 exception creates no general remediation round.
Codex is the implementer, not the independent reviewer.

See the [new evidence inventory](slice-2-obsidian-markdown-d17-r03-only-evidence-2026-10-20.json)
for exact changed/new paths and SHA-256 values, command argv/cwd/budgets, receipts, preservation checks
and limitations. New command logs are under `logs/phase18/s2-d17-r03-only-2026-10-20/`.

Final inventory: **six inherited files changed**, four new corrective files plus the evidence
inventory, **17 bounded command receipts** (including the preserved failed reproduction), **2,228
other entry files unchanged**, **165 historical log/artifact hashes**, **29 referenced independent
artifacts** and **10 official snapshot hashes** verified. Historical human fields remain unset.

## Reproduction and implementation

Before production edits, deterministic parser/chunker and actual CPU/JSON producer tests reproduced
both findings: **15 failed / 10 passed**, exit 1 (`reproduce`). That failure receipt remains unchanged.

| Finding | Minimal correction | Passing proof |
| --- | --- | --- |
| D17-FR-01 | The existing lexical scanner sends all solidus start tags through normal HTML start-tag handling. Existing void membership still determines termination; non-void div/span keep native context. Existing RCDATA and active-content checking are reused. | Block/inline solidus, matching ancestor/end tags, void br/hr, textarea/title, optional native end tags; both supplied and absent child bindings. Literal child material never appears inside native HTML. |
| D17-FR-02 | HTML continuation uses the owning Markdown parser's adjusted indentation/quote state and passed end limit. Blank lines may continue HTML; the first nonempty outdent/sibling or negative quote sentinel ends that continuation. | Sibling unordered/ordered items, outdented headings, nested lists, list/quote and quote/list containers, blank blocks, CRLF and separately bound child notes. Root-level HTML still continues through blank lines/EOF. |

Only `_html_across_blocks` changes in `obsidian.py` (AST comparison against the entry snapshot);
only `handle_startendtag` changes in `obsidian_html.py`. The parser fingerprint is now
`ppv-parser-v18.6-d17-html-container-boundaries`. Restoring that one string in `embeddings/model.py`
reproduces its accepted entry SHA-256 exactly: model/tokenizer, embedding/cache/thread/fallback and
validation logic are unchanged. Chunker identity/code, compaction/intersection functions, worker
accounting, limits and Java identity admission are unchanged.

Tests check exact original source characters and CRLF, root/child identity and revision 3/7,
character and line locators, dependency edges, structural parents, list/heading/embed/strong nodes,
and real 384-dimensional CPU embeddings after producer JSON encoding/decoding. Parser/chunker
checks use budgets 37 and 1,500. Missing bindings outside HTML and active content reject complete
roots with zero successful chunks; supplied child material inside HTML stays inert. The complete
root is parsed/validated through the existing producer path. Grant/SQL boundaries are captured;
this proves no Java/PG publication behavior.

## Interpretation disposition

The new [interpretation document](d17-r03-html-interpretation-resolution-2026-10-20.md) and
[source-hashed overlay](d17-r03-container-interpretation-2026-10-20.json) restore the official
two-sibling-item projections for **CommonMark 175 / GFM 144**. The first item's open div does not
own the second list marker. The other **five independently supported** interpretations are retained.
The original source gold, prior seven-label overlay and independent adjudication are unchanged.
This is a reviewable corrective interpretation, with independent review **PENDING** and human
reviewer/signature fields **unset**. No syntax is excluded or acceptance threshold weakened.

## Executed verification

All long commands used the existing watchdog, normal profile, **120–240-second** budgets.
The inventory records full exact argv, logs, command hashes, exits and receipt hashes.

| Gate / receipt stem | Result |
| --- | --- |
| Pre-fix `reproduce` | **15 failed / 10 passed**, preserved |
| R03-only `focused-r1` | **25 passed** |
| R03 + HTML5/RCDATA/Obsidian module group `focused-r2` | **95 passed**, no failures/errors/skips |
| Complete applicable D17/worker/domain/transport/chunker/embedding/package `d17-full-gate-final` | **170 passed / 0 failures / 0 errors / 0 skips**, 37.33 seconds; final sibling/nested-parent assertions included (preceding full run: 170 passed in 38.69 seconds) |
| Additional unchanged legacy compatibility selection `compatibility` | **25 passed / 30 deselected**, no failures/errors/skips; explicit deferred Office/PDF/OCR exclusions unchanged |
| Official parser→chunker `parser-corpus` | **1,267 supported + 62 required whole-root rejections**, all 1,329 examples attempted |
| Official real CPU parser→chunker→embedding→producer output `producer-corpus` | **1,267 supported + 62 required whole-root rejections** |
| Ruff / formatting / mypy | PASS; **73** files formatted, **24** source files typed |
| Offline lock / installed dependencies | PASS; Python **3.14.4**, **67** installed locked distributions, **73** resolved lock packages; no dependency/model change or download |
| `git diff --check` / evidence preservation | PASS; exact hashes and entry/final paths in inventory |

The new regression file contributes **26 tests**, covering 23 source cases, producer batches with
and without bindings, and four whole-root rejection cases. CPU provider is asserted on successful
producer outputs. CUDA remains **UNAVAILABLE** in this selected runtime; no synthetic GPU claim.
The accepted R01 maximum-source/unique-content performance, R02 intersections, R04 accounting and
R05 resource/serialization receipts are reused, not reopened or rerun for a favorable result.
No global Windows-write proof or additional application-temp investigation was introduced.

## Remaining disposition and boundaries

D17-FR-01/02 have passing implementation evidence and await fresh independent adjudication.
No residual executable failure is known in the required checks. Human evidence remains unsigned;
the implementer cannot grant independent closure or full D17 acceptance. The frozen profile and
ALL-OR-NOTHING gate remain mandatory.

Phase 17 **FROZEN**; Slice 1/D14 **CLOSED**; FR18-3 **OPEN**; external indexing **INACTIVE**;
live originals **DISABLED/DEFERRED**; OCR/Office/PDF acceptance deferred; Slices 3/4 unauthorized.
Historical aggregate Java gate remains **FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips**,
not rerun unchanged or waived. No Java/Flyway/SQL/public-wire change, OCR research, unrelated
performance/architecture work, commit/push/tag/PR or independent final review. Stop after handback.
