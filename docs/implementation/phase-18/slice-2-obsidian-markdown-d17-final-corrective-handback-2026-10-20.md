# D17 final bounded corrective implementation handback — 2026-10-20

**Codex implementation authorship; READY_FOR_INDEPENDENT_RE_REVIEW, D17-R01–05 only.**
This is implementation evidence, not independent acceptance, human adjudication, broader Slice 2
completion or owner-commit readiness. The owner explicitly authorized this one finite corrective
scope against the [independent blocker review](reviews/2026-10-20-d17-obsidian-markdown-independent-blocker-review.md).
No further remediation loop is authorized. Stop after this handback.

## Authority and preservation

Entry/exit HEAD: `c3b46351507803053850e3c2711602ee9c9ec419`, branch `main`, inherited dirty tree.
The [evidence inventory](slice-2-obsidian-markdown-d17-final-corrective-evidence-2026-10-20.json)
contains entry/final hashes, exact argv/cwd/timeouts, all passing and failed receipts and preservation
checks. Historical reviews, handbacks, failed receipts, gold, model/tokenizer and dependency pins
are unchanged. The [previous corrective handback](slice-2-obsidian-markdown-d17-corrective-handback-2026-10-20.md)
and the [87-label independent audit](reviews/2026-10-20-d17-independent-profile-label-audit.json)
remain historical evidence.

Final preservation checks verify **98 historical log/receipt hashes**, all **10 official snapshots**,
**29 directly referenced independent artifacts**, and **1,687 protected backend/Phase 17/Slice 1/review
files** unchanged. Only the ten enumerated inherited implementation/status files changed in this
scope; five new corrective files plus the evidence inventory were added. The inventory records
**43 exact bounded commands**, including all five failed command receipts from this corrective run.

Historical Antigravity remediation remains **2/2 exhausted**: [round 1, 2026-10-18](reviews/2026-10-18-phase-18-codex-final-re-review-1.md)
and [round 2, 2026-10-18](reviews/2026-10-18-phase-18-codex-final-re-review-2.md) each returned
CHANGES_REQUESTED, retaining their FR18-1–11 residuals. The initial review was not counted as a
remediation cycle. This separately owner-authorized D17 correction does not reset that ledger.
Codex is the implementer; a separate session must independently review these changes.

## Corrections and regression evidence

Before production edits, ten new tests reproduced HTML EOF/RCDATA rejection or false child
transclusion, reversed intersections in real serialized producer output, and scope-exit count
errors (**10 failed**, retained `reproduce` receipt). A smaller real CPU profile reproduced the
serial inference bottleneck and large metadata output without rerunning the failed million-character
gate unchanged. No historical failed evidence was removed or relabeled.

| Finding | Implemented correction and evidence |
| --- | --- |
| D17-R01 | Same model/tokenizer, singleton vector semantics and validations; one session reused within the existing worker batch, four ORT intra-op threads/one inter-op thread, sequential execution. Exact normalized-text results reused only within each embed_batch call; returned vectors are separate lists. Real tensor batching/thread experiments retained; batching was rejected because quantization rounding changed vectors and offered no measured advantage at the chosen threading. Real cancellation during cached reuse tested. Unchanged 1,000,000-character workload passes in three fresh processes plus the complete resource run, with the unchanged 60-second root deadline. |
| D17-R02 | Source context stays complete. Only strictly nonempty intersections are emitted; disjoint context has explicit null intersection. First/middle/final split chunks and plain/quoted/nested-list tables are checked after actual producer JSON serialization and reference expansion, with original source/revision/cell locations. |
| D17-R03 | Open inert HTML continues across blank blocks through EOF or its enclosing source scope; optional closures still use native HTML repair. Literal embeds remain inert even with a supplied child binding. textarea/title use RCDATA lexical context, including EOF/self-closing textarea; real active tags/attributes outside RCDATA remain denied. See the explicit source-hashed [corrective interpretation record](d17-commonmark-171-corrective-resolution-2026-10-20.json). Original gold and independent audit remain unchanged. |
| D17-R04 | Success and staged_count change only after parsing_scope exits successfully. Deterministically advance the real scope clock at exit: failed sole roots stage zero successful chunks; a second independent root still succeeds. Counts, reason, status and output digest are asserted. |
| D17-R05 | Existing block definition/reference semantics now intern exact locators, inherit equal provenance fields and node defaults, and reference once-defined source cells. Parser/chunker dictionaries remain available; producer metadata declares block-local-references-v1. Lossless reconstruction is asserted against every original dictionary for split/nested and unsplit tables. Source characters, ancestry, revision and all original cell locations survive. No arbitrary byte threshold was added. |

Parser identity: `ppv-parser-v18.5-d17-html-context`;
chunker identity: `structure-first-v18.5-compact-source-context`.
Model name/version/artifact, tokenizer/version/artifact, 384 dimensions, normalization and OCR identity
are unchanged. No Java identity allowlist was changed or publication authorized.

## Executed gates

All command receipts are in `logs/phase18/s2-d17-final-corrective-2026-10-20/`; exact commands and
hashes are in the evidence inventory. Ordinary commands use the existing executable watchdog,
with finite 180–300-second budgets; no benchmark uses the full-suite timeout exception.

| Gate | Result |
| --- | --- |
| Complete authorized D17 + worker/transport/domain/chunker/embedding/package group | **144 passed / 0 failures / 0 errors / 0 skips**; final 37.58 seconds |
| Additional legacy compatibility selection | **25 passed / 30 deselected**, 0 failures/errors/skips; the unchanged selection excludes explicitly deferred Office/PDF/OCR acceptance |
| Official CommonMark/GFM, parser → chunker | **1,267 PASS + 62 required whole-root rejections**, all 1,329 attempted |
| Official corpus, actual parser → chunker → real CPU embedding → producer-local output | **1,267 PASS + 62 required rejections**; grant/DB boundary captured, no Java/PG publication claim |
| Maximum-source repeatability | Three fresh-process passes at **4.216 / 3.786 / 3.692 seconds**; unchanged input and 60-second parsing scope; 689 successful chunks, exact original source |
| Complete resource suite | **7/7 PASS**: maximum source, near-cap table/HTML, table-expansion rejection, active-HTML cleanup, actual parsing cancellation and deadline; unsuccessful roots have zero successful chunks |
| Ruff / formatting / mypy | PASS; **72** formatted files; **24** source files typed |
| Offline lock / installed dependency integrity | PASS; Python **3.14.4**, **67** installed locked distributions; model/tokenizer SHA-256 verified during real inference |
| git diff --check / preservation | PASS; see final inventory for entry/final hashes and protected paths |
| Historical aggregate Java clean gate | **FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips**; preserved, not rerun unchanged or waived |

The interpretation overlay changes CommonMark 171 from rejection to required inert RCDATA support.
Six previously unlabelled examples (CM155/175/184, GFM125/144/153) also expose the same EOF/container
literal-HTML rule. Their source hashes and explicit expected projections are recorded separately;
all original examples and expectations remain preserved. The native-tree oracle compensates for
HTML5's initial-textarea-newline consumption when reserializing an already-parsed tree. These are
reviewable implementer interpretations, not fabricated independent or human approvals.

## Measurements and limits

The pinned graph contains DynamicQuantizeLinear/MatMulInteger. A real 35-chunk tensor experiment
with thread counts 1/2/4/8 and batch sizes 1/4/8 found batch coordinate drift up to **0.01009**
(minimum cosine **0.99831**), while singleton vectors across thread settings match. Production
therefore retains singleton tensors. Model initialization/hash checking remains per existing
worker request; no process-global content/session cache or new lifecycle was introduced.

The 50,000-character repeated-prose profile's embedding stage fell from **3.432 to 0.100 seconds**.
This gain depends on exact repeated text in the unchanged acceptance source; it is not a promise
that every million-character unique document finishes that quickly. Arbitrary unique documents
remain bounded by the unchanged fail-closed deadline. No size/deadline reduction or vector-space
change was used. CUDA is **UNAVAILABLE** in the selected runtime; synthetic fallback tests do not
prove real CUDA OOM recovery.

The unchanged workload hashes to `749df549cb2aa8f9aaa90800e1fc1064bde1bcaded850a2edfef4ac1cf134cfd`:
689 chunks contain **two distinct normalized texts** (maximum 354 tokenizer tokens). This explains
the large reuse gain explicitly; input characters, all emitted chunks and exact locators are retained.

For the same 100/600-row synthetic sources, serialized output changed from **2,554,347 / 15,398,302**
bytes to **1,106,127 / 6,642,647** bytes. The 600-row serialization/compaction traced peak allocation
fell from **30,796,678 to 22,929,252** bytes. Traced live allocation counts after serialization grew
from 2 to 79,962 because compact dictionaries are constructed during the measured region; these
are live counts, not a cumulative allocation-event count. With tracing enabled, serialization plus
compaction took **1.713 seconds**, versus **1.288** for original serialization. This is an explicit
CPU/allocation tradeoff for less redundant staged output. After owned payload release/collection,
only **484 traced bytes** remain in that measured region; native allocator release is not implied.
The final profile's cumulative process peak working set is **642,703,360 bytes**.

The complete worker resource suite reports near-cap table output **20,763,337 bytes** versus the
historical **47,701,900**; its cumulative process peak working set reaches **776,290,304 bytes**.
Controlled application scratch is calibrated with a 4-MiB write, sampled and monitored using native
directory notifications: zero workload scratch changes or retained bytes observed. Sampling gaps
are retained. This is scoped application-temp evidence, not zero global Windows writes. Process
peak working set, host headroom, device-wide VRAM samples and controlled scratch are distinct metrics.

Failed intermediate receipts remain: initial defect reproduction, the rejected tensor/vector
experiment, first corpus interpretation mismatches, initial typing/static/format issues and their
subsequent successful corrections. No failing test was skipped to produce the current passes.

## Outstanding disposition and stop

All five findings have passing implementation evidence; none is independently closed by this session.
The original human package remains **unsigned**, including no invented reviewer/signature. Separate
independent review must adjudicate source semantics, the seven explicit label interpretations,
reference encoding, vector reuse and resource evidence. D17 ALL-OR-NOTHING remains mandatory.

Phase 17 **FROZEN**; Slice 1/D14 **CLOSED**; FR18-3 **OPEN**; external indexing **INACTIVE**;
live originals **DISABLED/DEFERRED**; OCR and other deferred formats remain deferred. Historical
Java gate remains **FAILED**. No Java/Flyway/SQL/public-wire change, Slice 3/4, commit/push/tag/PR,
independent final acceptance or automatic next corrective round. Await separate independent re-review.
