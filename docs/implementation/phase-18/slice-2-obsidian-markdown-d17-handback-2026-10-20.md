# D17 — Obsidian Markdown implementation handback, 2026-10-20

**BLOCKED / OWNER_DECISION_REQUIRED.** This finite owner-authorized checkpoint records D17,
implements the source-extraction candidate and preserves failed acceptance. It does **not** establish
the entire frozen Obsidian Markdown profile, the original broader Slice 2, P18-FINAL or Phase 18.
Stop for independent review/owner disposition; no automatic remediation round, commit or push.

## Controlling scope and exact gaps

[D17](owner-decisions.md#2026-10-20--d17-obsidian-markdown-priority-bounded-rebaseline-and-implementation)
supersedes the active broader-format delivery priority, under the unchanged
[ADR-0023](../../adr/0023-phase-18-application-level-snapshot-worker-trust.md) authority contract.
The [frozen syntax contract/matrix](obsidian-markdown-d17-contract.md) remains mandatory; requirements
were not removed to make this implementation appear complete.

1. **MD17-10 FAILED:** `<div>\n\n**literal HTML body**\n\n</div>\n` is rejected because the HTML
   element spans blank Markdown blocks. `<p>First paragraph\n<p>Second paragraph\n` is rejected
   because optional HTML closing tags are not reconstructed. Both return
   `MD_UNSUPPORTED_HTML_STRUCTURE` with **zero staged chunks**. That is safe whole-document failure,
   not support for required inert HTML. These are real producer-local failures, not merely parser
   unit assertions. Other legal raw-HTML examples also remain rejected; see official example receipts.
2. **Installed dependency compatibility FAILED:** `uv pip check` reports that existing
   `rapidocr-onnxruntime` requires Python `>=3.6,<3.13` while the frozen interpreter is **3.14.4**.
   The Markdown route does not initialize OCR, but that does not waive dependency compatibility.
   No old version, frozen interpreter or OCR artifact was changed to hide this conflict.
3. **Remaining mandatory evidence:** development source checks do not establish exhaustive
   CommonMark/GFM/Obsidian structural conformance, malformed intended-table behavior or independently
   reviewed quality. The synthetic fixture gold has no human signature/independent acceptance.
   Temporary retained bytes are measured; transient peak disk usage is **not measured**. The entire
   profile and representative resource coverage therefore remain unverified.

## Implementation delivered

The existing `parse_markdown` entry point delegates **whole-file** Markdown to the new structural
extractor; its legacy field-filtered privacy path remains intact. The extractor reuses the existing
elements, chunker, immutable supplied-dependency bindings, producer and cancellation/deadline controls.
No additional worker engine, file discovery, network resource resolution or Java publication path
was introduced.

- Pinned CommonMark/GFM grammar plus Obsidian source extensions: raw authored text, properties,
  headings, nested list/task/quote structure, aliases/anchors, note embeds, callouts, footnotes,
  comments, tags/highlights, code/Mermaid/math and a balanced inert HTML source tree.
- Exact original source/revision/field and Unicode character/line spans. Table cells retain raw
  spans, row/column/header/alignment; ragged implicit cells explicitly use row context without
  invented cell characters. Overflow source cells are retained. Inline nodes identify their
  containing block/cell instead of inventing narrower original offsets.
- Whole-root supplied-source binding conflicts, unapproved/missing/cyclic/deep embeds, malformed
  properties, invalid mapping, active HTML, binary/remote embeds and known executable query/template
  directives fail explicitly. Comments, ordinary code and raw HTML do not grant dependency authority.
  Full source is parsed/validated before root embedding/staging; no successful prefix of a failed
  root is staged. Existing batch semantics for other independent roots are unchanged.
- The parent source retains its embed marker. Child source units carry complete dependency edges,
  aliases, anchor selections, revisions and parent callout context. Flattening lists a complete
  parent block then child units; it does not claim an inline rendered DOM or computed plugin output.
- Large chunks keep contiguous exact spans. Their original source structure is defined once and
  referenced by following slices, preventing repeated full-tree allocation. Split line ends use the
  last actual source character. Properties avoid YAML 1.1 on/yes/octal/sexagesimal substitutions;
  aliases, duplicates, unsafe/nonfinite values and excessive nesting reject without global loader changes.
- Markdown initializes no OCR engine. Sanitized typed Markdown reason codes reach failure staging.
  Python parser/chunker identities are updated truthfully; **Java exact allowlists are unchanged**.
  Unknown configurations remain rejected. This does not close S2-08/FR18-3 or authorize publication.

## Candidate configuration and source freeze

| Item | Recorded candidate |
| --- | --- |
| Profile | `ppv-obsidian-source-v1`, incomplete implementation |
| Parser identity | `ppv-parser-v18.3-obsidian-source-v1` |
| Chunker identity | `structure-first-v18.3-source-tree-spans` |
| New packages | markdown-it-py **4.2.0**, mdit-py-plugins **0.6.1**, mdurl **0.1.2**; MIT licenses and installed license-file hashes recorded |
| Runtime | Existing Windows 11 / CPython **3.14.4**; no interpreter/model/OCR version change |
| Existing packages | All pre-checkpoint package versions retained; **69 installed** distributions match the lock (71 cross-platform resolved packages) |
| Embedding | Existing CPU multilingual MiniLM model; SHA-256 `98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f` |
| Tokenizer | Existing artifact SHA-256 `2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8` |
| Official syntax | Eight immutable Obsidian help blobs, CommonMark 0.31.2 and GFM 0.29 snapshots, pinned in the [source inventory](obsidian-markdown-d17-official-sources-2026-10-20.json) |

This is a reproducible **candidate fingerprint**, not promotion or acceptance of a verified complete
configuration. Source/config/dependency/test hashes, changed-file inventory, exact verification argv,
receipt hashes and preservation checks are in the
[D17 evidence inventory](slice-2-obsidian-markdown-d17-evidence-2026-10-20.json).

## Verification and actual limitations

All receipt/log names below are under `logs/phase18/s2-d17-markdown-2026-10-20/` and paired by stem.
Commands ran from the repository root through `scripts/run-bounded-command.py --profile normal`.
The evidence inventory records exact child argv and watchdog limits. No required failure is relabeled PASS.

| Check | Result / receipt |
| --- | --- |
| Final Python suite, after property resolver correction | **168 passed / 0 failures / 0 errors / 0 skips**, 71.20 seconds pytest; `pytest-full-properties-final.{json,log}` |
| Earlier focused Markdown/corrective compatibility | **87 passed**, 48.14 seconds; `pytest-focused-r3.{json,log}`; predates final added property regression |
| Ruff | PASS, `ruff-closed.{json,log}` |
| Formatting | PASS, 63 files, `format-closed.{json,log}` |
| mypy | PASS, 24 source files, `mypy-closed.{json,log}` |
| Lock offline check | PASS, `uv-check.{json,log}` |
| Installed version/license inventory | PASS, 69 packages match lock; `dependency-inventory.{json,log}`, `installed-dependencies.json` |
| Installed dependency compatibility | **FAILED**, existing RapidOCR/Python constraint; `uv-pip-check.{json,log}` |
| Final actual CPU producer + mandatory HTML gate | **FAILED**, exit 1: 2 supported roots succeed, 2 required HTML roots fail with zero chunks; `producer-resources-final.{json,log}` and `resources-final/producer-resources-and-mandatory-html.json` |
| CommonMark official smoke | 652 examples: 581 exact-source extractions, 71 typed rejections. Core dependency rendering matches 649/652 exactly; this is **not full profile conformance**. `official-examples-final.{json,log}`, `official/official-example-results-v18.3.json` |
| GFM official smoke | 677 examples: 606 exact-source extractions, 71 typed rejections. No rendered/structural conformance claim. `gfm-examples-final.{json,log}`, `official/gfm-example-results-v18.3.json` |
| Diff whitespace | `git diff --check`, recorded in the evidence inventory |
| Java | No Java code/allowlist change or fresh Java gate. Historical full gate remains **FAILED**, 1,433 tests / 1 failure / 2 errors / 0 skips. |

The direct producer runs use the real local embedding/tokenizer/worker code. Java grant verification
and DB staging are explicit capture stubs. They prove producer-local outputs, not restricted-role DB
COMMIT, Java trust validation or publication. Actual HTTP cancellation interrupts the running Markdown
parser, returns CANCELLED and stages no partial output; a short actual parsing deadline produces a
whole-root resource failure. Java's existing process supervision remains unchanged. This is not new
native OCR interruption proof; OCR acceptance is deferred.

Final resource probe: **10.71 s wall / 45.92 s process CPU**, **618,057,728 bytes (589.4 MiB) process
peak working set**. The 152,001-character large root produces 103 chunks; the mixed vault root produces
23. The isolated process denies Python `socket.connect`; provider reports CPU and local artifact
integrity is checked. It is not a fresh OS firewall/container proof. **0 retained temporary bytes**,
temporary peak NOT_MEASURED. NVML values are device-wide boundary samples, not worker-attributable
VRAM. The first oversized metadata run was observed at approximately 7.46 GB working set; its failed
test receipt is preserved. Later bounded receipts verify the tree-reuse correction on the exposed
resource workload, not worst-case resource acceptance.

Original failures and intermediate attempts remain: first two focused attempts each had 2 failures;
first full run had 3 failures/156 passes; later full run had 167 passes; initial mypy/Ruff issues,
the failed MSYS TLS source fetch and both mandatory HTML gate failures are retained. Do not cite
only the final green unit suite as acceptance. No source/metadata hashes from a preceding candidate
are represented as final-candidate measurements.

## Fixtures, preservation and next gate

The new exposed vault is `rag-engine/tests/fixtures/phase18-d17-obsidian-v1/`: three authored notes,
immutable revision/hash manifest and unsigned `REVIEW.txt`. Parametrized regression fixtures add
mixed syntax, escaped/nested tables, raw HTML, malformed/unsafe cases, conflict/cycle/depth limits,
large inputs and cancellation. Labels were authored by the implementer; **human/independent review
is pending**, not claimed. The previous frozen OCR held-out/adjudication package is untouched and
its pending human adjudication is not waived or repurposed as D17 tuning material.

The machine-readable evidence distinguishes D17 changes from the pre-existing dirty tree and verifies
the original review/handback files, all 331 inventoried prior artifacts, deferred parser/OCR code,
backend sources and frozen policies against entry hashes. Only two pre-existing Markdown regression
functions were updated to require the new stronger verbatim-source semantics; unrelated/deferred
test functions are preserved. Original file bytes are saved under the ignored receipt directory.

The existing failed Java gate is preserved at
`logs/phase18/s2-takeover-full-java-2026-10-20.{json,log}`; its meaning is unchanged. Native alias
history, native document provenance/FR18-3 and aggregate PostgreSQL slot exhaustion remain unresolved
as previously governed. D14 is not a D17/full-gate waiver.

**Next gate:** independent review of this incomplete D17 implementation and owner disposition of
the remaining syntax/compatibility/evidence gaps. No automatic continuation or OCR experiment.
Phase 17 FROZEN; Slice 1/D14 CLOSED; FR18-3 OPEN; external indexing INACTIVE; Slices 3–4 unauthorized.
No production migration, broader slice acceptance, P18-FINAL/Phase 18 closure, commit, push or tag.
