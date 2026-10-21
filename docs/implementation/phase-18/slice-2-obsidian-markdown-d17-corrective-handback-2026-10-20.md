# D17 Markdown completion corrective handback — 2026-10-20

**BLOCKED / OWNER_DECISION_REQUIRED.** This resumes the separately owner-authorized finite D17
completion checkpoint. It does not restart historical remediation rounds or perform independent
review. The [frozen profile/matrix](obsidian-markdown-d17-contract.md) is unchanged in scope.
Stop here; no automatic further cycle, commit, push or broader Slice 2 acceptance.

## Result and exact remaining gaps

MD17-10 now has passing parser → chunker → real producer-local development evidence for inert
HTML across blank blocks and optional closing tags. The selected Markdown environment is a
consistent **Windows CPython 3.14.4 / 67-distribution** installation, without RapidOCR/OpenCV.
Structural corpus verification passes **1,266 supported examples plus 63 required whole-document
rejections**, not 1,329 successful documents. Human/independent adjudication remains **PENDING**.

**MD17-11 remains FAILED:** the representative admitted **1,000,000-character** prose workload
completed once in **58.735 seconds**, then failed the unchanged **60-second** processing deadline
in two later resource attempts: **61.786 seconds** and **64.494 seconds** wall time. Both failures
return `RESOURCE_LIMIT_EXCEEDED` with zero chunks. The earlier single pass does not prove stable
maximum-workload completion. The failure is preserved rather than increasing the limit, lowering
the admitted size or retrying unchanged for a lucky pass. Final small source/format/type corrections
do not re-establish the maximum-workload gate; it was not rerun after them. Resource readiness
therefore remains unverified for the final candidate, and the entire frozen profile is not ready.

There is no newly demonstrated unsupported static syntax in the exercised development corpus.
This is not a completeness or independently reviewed acceptance claim: all 87 profile composition
labels, the realistic vault gold, native HTML source-context locators and the complete matrix
still require genuine review. Reviewers can reject the implementer's interpretation.

## Reused implementation and bounded corrections

- Reuse the existing whole-file Markdown entry point, markdown-it-py/GFM rules, source tree,
  dependency bindings, chunker, worker, deadline and cancellation controls. No additional worker.
- Use pinned **html5lib 1.1** (MIT) for inert HTML5 hierarchy and optional end-tag repair, with
  instance-local checked tokenization. Preserve authored lexical tokens and raw source separately.
  HTML5-implied nodes explicitly have `DERIVED_HTML5_STRUCTURE` and containing-source locators;
  no invented narrow spans for implied nodes. Cross-blank HTML remains literal native HTML.
  Active tags, handlers and unsafe resource-bearing styles/URLs reject the entire document.
- Preserve block/inline hierarchy metadata, semantic text and source-cell definitions once for
  split structures; later slices reference the definition. Correct GFM single/double strike,
  email local-part handling, empty properties and short CommonMark HTML comments. Unsupported
  YAML tags fail explicitly. Guard table expansion before allocation while allowing nested
  list tables. Final regressions caught and corrected an overbroad table guard.
- Avoid repeated D17 line-prefix scans when splitting large blocks. Embedding model, tokenizer,
  inference implementation and Phase 17 wire/authority contracts are unchanged. Parser/chunker
  candidate identities are `ppv-parser-v18.4-d17-html5-structural` and
  `structure-first-v18.4-source-tree-definition`. Java's exact allowlist is unchanged; these
  identities are **not** evidence of approved Java publication or FR18-3 closure.

## Dependency compatibility

RapidOCR 1.4.4 is retained in the lock and in a nondefault `deferred-ocr` dependency group requiring
Python `>=3.12,<3.13`, matching its obsolete runtime constraint. It is not installed in the new
`.venv-d17` environment. The old `.venv`, OCR code/models and failed receipts are preserved.
The selected base plus dev environment adds html5lib 1.1/webencodings 0.6.1, makes existing
PyYAML 6.0.3 explicit, and preserves existing locked package versions. Python stays **3.14.4**.

Locked synchronization, offline lock consistency, 67-package installed/lock inventory and
`uv pip check` pass. Deliberately requesting `deferred-ocr` on Python 3.14.4 fails resolution
(expected negative receipt); no claim that OCR works in this environment. The targeted mypy
configuration permits only missing `cv2` typing in the deferred PDF helper; production imports
remain lazy and unchanged. No broad missing-import waiver or dependency bypass.

## Structural and quality evidence

The frozen official snapshots and all previous fixtures are reused. The new
[development review package](../../../rag-engine/tests/fixtures/phase18-d17-completion-v1/REVIEW.md)
contains an authored mixed HTML vault note and **87 unsigned, source-hashed** composition records.
Official corpus denominators are **652 CommonMark + 677 GFM = 1,329**. The structural oracle
reconstructs the extracted AST (tags, parents, attributes, semantic text, native HTML hierarchy),
not merely verbatim source. Producer mode uses actual local CPU vectors; existing grant validation
and DB staging are explicitly captured test boundaries. No Java/DB publication proof is claimed.

The strict unadapted official corpus has preserved failures. Profile-aware evaluation explicitly
applies frozen Obsidian literal-HTML/empty-properties/wikilink priority and combined GFM/newer
CommonMark rules, and requires typed rejection for unsafe/unsupported mandatory failures.
These labels need independent review; they are not a waiver or a human signature. HTML semantic
normalization reuses html5lib, so it is not an independent HTML parser oracle. No frozen OCR
held-out data was used for selection or tuning. OCR adjudication remains deferred, not waived.

## Verification and resource receipts

All new receipts are under `logs/phase18/s2-d17-completion-2026-10-20/`. The
[machine-readable inventory](slice-2-obsidian-markdown-d17-corrective-evidence-2026-10-20.json)
records exact commands, bounded runner receipts, hashes, preservation checks and changed files.

| Check | Actual result |
| --- | --- |
| D17 plus worker/transport/domain/chunker/embedding/package integrity group | **128 passed**, 0 failed/errors/skips (`pytest-final-r2`) |
| Additional Markdown/legacy/domain compatibility selection | **25 passed, 30 deselected**, 0 failed (`pytest-compatibility-r2`); deferred-format cases explicitly outside selected runtime |
| Official parser/chunker profile corpus | **1,266 PASS + 63 required rejections** (`structural-profile-r2`) |
| Final actual producer-local CPU corpus | **1,266 PASS + 63 required rejections** (`producer-conformance-final`) |
| Ruff / formatting | PASS; final static receipts linked in inventory |
| mypy with project configuration | PASS, 24 source files (`mypy-config-final-r2`) |
| Locked installation / offline lock check / installed inventory / pip check | PASS; 67 distributions; expected incompatible OCR-group resolution preserved |
| `git diff --check` / artifact consistency | PASS; all 122 historical D17 artifacts unchanged, source/receipt hashes and local handback links checked; implementer consistency only |
| Representative maximum source resource gate | **FAILED** as described above; not superseded by smaller-workload success |
| Targeted near-node-cap table/HTML, expansion rejection, unsafe HTML cleanup, actual parser cancellation/deadline | **6/6 PASS** (`resources-targeted-final-r2`); maximum-workload failure remains blocking |
| Original Java full clean gate | **FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips**, historical receipt preserved, not rerun or waived |

Disk measurement redirects TEMP/TMP and Python temporary paths into a controlled directory,
samples at nominal 2 ms, and uses Windows native directory-change notifications to detect changes
during Python GIL pauses. A **4,194,304-byte** calibration file proves both monitors detect activity.
Measured workload scratch peak/retained bytes are zero with no native scratch change detected.
Sampling gaps are reported, not concealed. This covers the controlled scratch tree, not every
host cache/temp path. Native process IO counters record **260 bytes / 24 writes** at first model
initialization outside that tree; their destination is not established, so global zero temporary
disk use is **not proven**. Peak process working set, CPU/wall times, source/output bytes and
cleanup are retained per workload. Device-wide VRAM sampling is not model VRAM attribution.

The final near-cap table has **61,236 characters**, completes in **9.794 seconds** (40.297 process
CPU seconds), and produces **47,701,900 serialized output bytes**. The near-cap HTML has **126,016
characters**, completes in **12.723 seconds** (57.094 CPU seconds), and produces **8,592,142 output
bytes**. Reported process peak working set reaches **753,414,144 bytes** across this six-case run;
the peak is cumulative rather than per-document incremental allocation. Cancellation/deadline
cases each receive 1,000,000 characters and complete with zero chunks in 1.543/1.473 seconds.

Cancellation executes during actual Markdown parsing; direct worker cancellation returns REJECTED
with no stage, while the existing HTTP compatibility test verifies cancellation status. Deadline
and unsafe-content failures have no successful chunks. These producer-local tests reuse boundary
captures and are not native OCR supervision proof or an independent Java authority review.

Failed intermediate receipts remain: outdated HTML assertions, strict corpus differences,
incomplete-source/comment fixes, nested-table regression, static diagnostics, a deferred PDF test
accidentally included in the compatibility selection, and a resource command argument error.
Later passing checks do not delete or relabel those attempts.

## Governance and stop

Phase 17 COMPLETE/FROZEN; Slice 1 COMPLETE/CLOSED under D14; FR18-3 OPEN; external indexing INACTIVE;
live originals DISABLED/DEFERRED. OCR/PDF/DOCX/XLS/XLSX acceptance remains deferred with code/tests
and evidence retained. Slices 3/4 unauthorized. No Java allowlist, publication, migration, model,
global skill policy, commit or push change. Historical Antigravity rounds remain **2/2**.

Owner disposition is required for any additional finite work on maximum-workload stability and
unattributed temporary IO evidence. Genuine human/independent quality review is also pending.
No quality or resource criterion is narrowed here. Stop with **BLOCKED / OWNER_DECISION_REQUIRED**.
