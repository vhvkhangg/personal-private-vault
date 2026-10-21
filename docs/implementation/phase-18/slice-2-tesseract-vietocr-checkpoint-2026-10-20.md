# P18-S2 D16 Tesseract/VietOCR and PDF-heading checkpoint — 2026-10-20

**BLOCKED / OWNER_DECISION_REQUIRED.** The owner authorized this new finite checkpoint explicitly;
it is not another automatic remediation round. The PDF surrounding-text repair is implemented and
tested, awaiting independent review. No OCR configuration meets the required development fidelity;
no model, production dependency or verified producer identity is promoted.

## Authority, baseline and disposition

Read ACTIVE, D16, the complete preceding PaddleOCR/corrective handbacks, Slice 2 handoff and current
acceptance, held-out and verification gates. Applied Python/local RAG, bounded-command and testing
guidance. Graphify is unavailable; source files establish the diagnosis. Entry HEAD/main remains
`c3b46351507803053850e3c2711602ee9c9ec419`; inherited dirty/untracked work is preserved.
`logs/phase18/s2-ocr-tv-2026-10-20/entry.json` snapshots 2,196 existing files and 218 prior
model/receipt files before changes. Exact final preservation results and commands are in the
[evidence inventory](slice-2-tesseract-vietocr-evidence-2026-10-20.json).

| Area | Decision and remaining gate |
| --- | --- |
| KEEP | D16 vi/en OCR; native Chinese parsing/embedding coverage; existing source authority, model pins/artifacts, worker supervision and exact Java allowlist/provenance checks. All historical failures remain failures. |
| CORRECT | Existing PDF raster-table path omitted all text outside detected cells. OCR the measured-cell-masked image remainder once, retaining original-coordinate paragraphs and the existing page sort, cell/merged geometry and native-text masking. Independent review is still required; no S2 finding is self-closed. |
| S2-06 | **BLOCKED.** Tesseract variants are 5/6 exact; both VietOCR architectures retain tone/content errors. Fresh human-adjudicated held-out quality is still pending. No confidence-only acceptance, punctuation repair, spell correction, label rewriting or threshold waiver. |
| S2-07 | Prior current-backend native cancellation/deadline proof is preserved. No selected Tesseract/VietOCR producer exists, so no new integrated selected-backend interruption/resource certification is claimed. |
| S2-08 | No Java alignment or unknown identity admission. Conditional producer freeze/verification is unmet. The repair is identified by source hash in these diagnostics; no new certified pipeline identity is asserted. |
| DEFER | FR18-3/publication/selected-R/G, aggregate PostgreSQL investigation and P18-FINAL; Slices 3–4 unauthorized. Phase 17 FROZEN; Slice 1 COMPLETE/CLOSED/D14; external indexing INACTIVE. |

## Same-source comparison — all configurations FAIL quality

All seven valid comparisons use **exactly the same six exposed scanned development PDFs** as the
PaddleOCR checkpoint: English/Vietnamese shelf, historically named `held-out-invoice-*` (already
exposed development), and English/Vietnamese warehouse tables. Source hashes are verified before
each attempt. **6 documents/pages, 16 authored gold blocks, 2 tables, 10 logical cells** per run;
per script 3 files/pages, 8 blocks, 1 table, 5 cells. English gold has 139 characters, Vietnamese 123.
These sanitized synthetic PDFs are development samples, not a representative field corpus.
No frozen v3 held-out document was evaluated or used to select/tune a candidate.

| Actual configuration through repaired PDF parser | Exact en / vi files | en / vi character errors | Wall s | Root CPU s + observed child CPU s | Root OS peak working set bytes |
| --- | --- | --- | ---: | ---: | ---: |
| Tesseract 5.5.3, best `vie+eng`, OEM 1, PSM 6, 144 DPI | 3/3 / 2/3 | 0/139 / 1/123 | 14.023 | 6.453 + 12.563 | 218,648,576 |
| Tesseract, declared `en→eng`, `vi→vie`, PSM 6 | 3/3 / 2/3 | 0/139 / 1/123 | 8.791 | 4.859 + 7.141 | 218,124,288 |
| Tesseract, declared languages, PSM 11 | 3/3 / 2/3 | 0/139 / 1/123 | 9.225 | 4.938 + 8.172 | 218,443,776 |
| VietOCR VGG seq2seq + existing RapidOCR detector | 1/3 / 0/3 | 3/139 / 5/123 | 49.995 | 91.297 + 0 observed | 1,101,950,976 |
| VietOCR VGG Transformer + same detector | 0/3 / 0/3 | 3/139 / 4/123 | 51.008 | 99.250 + 0 observed | 1,326,161,920 |
| RapidOCR 3.10.0 / retained PP-OCRv6 small ONNX | 3/3 / 0/3 | 0/139 / 6/123 | 82.298 | 141.578 + 0 observed | 911,540,224 |
| Official PaddleOCR 3.7.0 / retained PP-OCRv6 small | 3/3 / 0/3 | 0/139 / 7/123 | 24.762 | 38.125 + 0.141 | 694,108,160 |

All **42 valid candidate-file attempts** complete; none are skipped or unreported. Every comparison
exits **1**. The earlier six-file Tesseract wrapper run without TSV configuration also fails and
is retained separately as **invalid wrapper evidence**, not an OCR capability measurement. Constructor/
environment failures have zero attempted source files and remain in the receipt ledger.

Tesseract consistently changes **`quyển` to `quyền`** in `Kệ sách có 45 quyển.`; declared PSM 6
line confidence is **0.965676**, demonstrating that high confidence does not establish fidelity.
Both VietOCR models change **`tiền` to `tiên`** in the invoice. Seq2seq also changes `A-17` to
`A 17`; both lose punctuation, and Transformer changes invoice capitalization. These discrepancies
remain explicit failures; no invented human judgment declares them nonmaterial. Tesseract's other
five files, including both complete warehouse headings/tables, match these authored labels exactly.

VietOCR is a **line recognizer**, not a page/box detector. The experiment reuses the existing local
RapidOCR-ONNXRuntime 1.4.4 detector, measured crop coordinates and explicit CPU VietOCR recognition.
No detector gold/held-out hints or synthetic boxes are supplied to inference. Native pretrained
VGG downloads are disabled because the complete local checkpoint supplies those parameters.
Official base/model config merging, CPU/2 threads, interop 1 and greedy prediction are recorded.
Tesseract TSV word/line boxes are measured in the rendered raster and scaled into original PDF
coordinates by the development adapter. No model result is staged/published or production-admitted.

`quality-metrics.json` records full-string CER without normalization, raw/parsed differences,
digits, complete block counts, original page/source/version/box checks, reading order and each
cell's text/span/geometry. Parsed digit sequences match all sources; this alone does not repair
tone/identifier loss. All observed table boxes retain the existing source-outline versus interior
delta of up to 1.5 PDF units; conventions/ambiguous ink envelopes still need human adjudication.
The full-page raw Tesseract PSM 6 path omits table text; the actual PDF path supplies all measured
cells independently and the remainder heading. Raw and integrated results are kept separately.

## Minimal PDF correction and real regression evidence

In `parsers/pdf.py`, `for box in boxes or [whole_image]` previously selected **only cells** whenever
a raster grid existed. Native glyph handling and final reading-order sorting could not recover text
never sent to OCR. The correction preserves that cell path, masks only the measured cell rectangles
in a copy of the already native-masked image, OCRs the remainder once, and emits its actual boxes as
paragraphs. Existing coordinate offsets/scales, native-overlap checks, limits, cell spans and page
sort are reused. It adds no engine, authority, worker, staging or dependency contract.

Two real generated raster-PDF tests cover a heading, side note, footer, merged header and four data
cells at **72 and 144 source DPI**, exact output/order and original page/source/geometry. Initial
spaced-heading fixtures demonstrate **2/2 failures before repair**: all three outside strings are
missing. After repair, that first fixture exposes the pinned OCR's independent whitespace loss.
The final structural regression uses authored **single-token** headings/notes/footers to isolate
coverage without normalizing OCR output; it still asserts exact strings/coordinates and passes 2/2.
The original spaced-fixture failures are preserved; the six comparison sources/labels are unchanged,
and no general English/whitespace gate is weakened or declared satisfied by these unit fixtures.

Both RapidOCR/PaddleOCR replays through the repaired parser retain the English warehouse heading
and all cells, unlike their preserved earlier handbacks. Vietnamese misrecognition remains visible.
The **full pinned Python suite passes 119/119, zero failures/errors/skips**, including existing
native/scanned/mixed PDF, overlap, merged geometry and preallocation/cancellation tests. No production
source changed after this run. New development-script changes do not affect those production tests.

## Versions, provenance, hashes and licenses

Tesseract is the real **MSYS2 UCRT Windows x64 5.5.3 / Leptonica 1.87.0** runtime, extracted locally
without installing packages or changing system PATH. Its dependency closure is taken from the current
MSYS2 repository database, every package SHA-256 matches that database, and native binary/license
hashes are retained. MSYS2 is an upstream-documented Windows distribution, not an official upstream
binary build: [Tesseract installation](https://tesseract-ocr.github.io/tessdoc/Installation.html).
Official `tessdata_best` requires the LSTM engine used here:
[upstream traineddata guidance](https://tesseract-ocr.github.io/tessdoc/Data-Files.html).

| New local model | Bytes | SHA-256 |
| --- | ---: | --- |
| Official best `vie.traineddata` | 12,435,550 | `b6b49293d95d0b6dbd8780174627e82c75be957b6f4ed9862155540d6b00bb45` |
| Official best `eng.traineddata` | 15,400,601 | `8280aed0782fe27257a68ea10fe7ef324ca0f8d85bd2fd145d1c2b560bcb66ba` |
| Official-linked VietOCR `vgg_seq2seq.pth` | 89,575,371 | `0921503a41375a0584268e23ef3d414ea478a8fe8777865c7745d38f2d0bc5db` |
| Official-linked VietOCR `vgg_transformer.pth` | 151,815,373 | `380512193a8b6cbf6fad80deacdc9b6939d10d473d199892fc6408d13775ea59` |

Tessdata repository revision **`e12c65a915945e4c28e237a9b52bc4a8f39a0cec`**; downloaded bytes
also match the upstream Git blob IDs at that revision. Tesseract/tessdata license files identify
Apache-2.0; individual native dependency licenses are separately preserved.

VietOCR source is pinned to **`fe8c3a7fc714aec57ab81cec844eb3adf0c1636c`**, reporting version
0.3.13, rather than silently treating it as the different PyPI release. The PyPI package pins
Pillow 10.2.0, conflicting with pdfplumber 0.11.10's Pillow requirement. The official source's broader
declared requirements resolve in the isolated **Python 3.12.13 / torch 2.14.1 / torchvision 0.29.1**
environment. UTF-8 mode is required for its Windows source build; both initial failures are retained.
Production Python **3.14.4**, lock/dependencies and model artifacts are unchanged. No claim that the
3.12 experiment establishes in-process compatibility with the 3.14 production worker.

VietOCR weights are downloaded only during provisioning from HTTPS URLs in the pinned official
[configs](https://github.com/pbcquoc/vietocr/tree/fe8c3a7fc714aec57ab81cec844eb3adf0c1636c/config).
Both load with the expected architecture and strict state-dict validation under torch's default
weights-only loader; exact bytes are checked before/after inference. No publisher-signed digest is
provided by those URLs, so recorded local integrity/source provenance is not signed authenticity.
The source [LICENSE](https://github.com/pbcquoc/vietocr/blob/fe8c3a7fc714aec57ab81cec844eb3adf0c1636c/LICENSE)
is Apache-2.0 while setup classifiers say MIT; a separate explicit pretrained-weight license is not
established. Those inconsistencies remain recorded, not blanket redistribution approval or promotion.

## Offline/resource verification and its limits

Provisioning is explicit development networking. Inference denies Python socket connect/connect_ex/
create_connection/getaddrinfo before candidate imports, uses local paths/configs, disables pretrained
CNN download and records **zero attempted Python connects**. Tesseract receives PNG bytes on stdin,
not URL input. Its native libcurl is present; Python socket denial is **not native/OS egress isolation**.
No production runtime download or automatic network capability is enabled.

Tables above include model initialization, raw OCR and actual PDF parsing. They exclude interpreter/
common parser import startup; Tesseract root CPU excludes native children, whose **last observed**
sampled CPU is reported separately and can undercount before process exit. Sampling is requested
every 20 ms, not a guaranteed schedule. Sampled process-tree RSS peaks are **289,652,736–309,866,496
bytes** across the three valid Tesseract runs; OS root peaks are not total-tree peaks. All runs record
zero surviving child processes. These are process/resource observations, not integrated Java job
committed-memory or native cancellation certification. Some inference/tests overlapped on the host;
timings are actual receipts, not an isolated comparative speed ranking.

Dedicated TEMP/TMP/TMPDIR and model caches show **0 observed peak/final file bytes** in every valid
run. This covers those namespaces with sampling, not every OS cache or a universal disk peak. Final
logical file sizes: isolated venv **1,042,699,161 bytes**, native runtime **100,533,966**, model folder
**269,255,119**, downloaded native package cache **62,715,598**. No GPU inference/VRAM proof is claimed.
Representative worst-case limits and selected-backend process supervision remain future gates.

## Exact commands, receipts and failed attempts

Receipt root `<R>` is `logs/phase18/s2-ocr-tv-2026-10-20`; candidate `<V>` is
`<R>/.venv/Scripts/python.exe`. Every long command uses the existing bounded runner, normal profile,
an explicit timeout and unique receipt/log; the evidence inventory stores exact argv and verifies
its SHA-256 against each wrapper receipt. Main inference command:

```text
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <R>/<run>.json -- <V> rag-engine/scripts/slice2_tesseract_vietocr_checkpoint.py --output <R> --backend <backend> --run-id <run> [options]
```

| Receipt/run | Backend / options or action | Result |
| --- | --- | --- |
| `tesseract-psm6b` | tesseract `--psm 6` (combined languages) | FAILED quality, 5/6 exact |
| `tesseract-declared6` | tesseract `--psm 6 --languages declared` | FAILED quality, 5/6 exact |
| `tesseract-declared11` | tesseract `--psm 11 --languages declared` | FAILED quality, 5/6 exact |
| `vietocr2` | vietocr (default seq2seq) | FAILED quality, 1/6 exact |
| `vietocr-transformer` | vietocr `--viet-model transformer` | FAILED quality, 0/6 exact |
| `rapid-baseline2` | rapid, same retained small ONNX hashes | FAILED quality, 3/6 exact |
| `paddle-baseline` | paddle, prior isolated Paddle venv and exact small configuration | FAILED quality, 3/6 exact |
| `heading-before` / `heading-after` / `heading-tokens` | focused real-PDF regressions, 180 s | 2 failed before; 2 independent whitespace failures after; final 2 passed |
| `python-full` | pinned `python -m pytest rag-engine/tests -q --junitxml=<R>/python-full.xml`, 240 s | 119 passed, 88.14 s pytest |
| `ruff` / `format` | pinned Ruff check / format --check over src/scripts/tests, 60 s | PASS |
| `mypy` | pinned mypy --config-file rag-engine/pyproject.toml rag-engine/src, 120 s | PASS, 22 production source files |
| `locked-inventory` | pinned dependency_inventory.py --output <R>/locked-inventory-results.json, 120 s | PASS, original 66-package lock |
| `isolated-freeze` | uv pip freeze --python <V>, 60 s | PASS; isolated versions retained |
| `git-diff-check` | git diff --check, 60 s | PASS |

Initial failures are preserved: missing virtual-package provider in native provisioning (resolved
from repository PROVIDES); PyPI Pillow conflict; source-build Windows decoding (UTF-8 corrected);
missing psutil in the original pinned/old Rapid venvs (use the new isolated venv, do not alter them);
wrong deep config merge (match upstream shallow architecture replacement); missing native TSV config
(use explicit `tessedit_create_tsv=1` and validate its header). No failed attempt is overwritten.
TSV parsing, fixture and static diagnostics are not falsely reported as candidate-quality success.

The original full Java clean gate remains **FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips**.
Receipt/log hashes are unchanged. No selected producer or Java change exists; this bounded failed
feasibility checkpoint does not repeat the known blocked aggregate for a lucky green. Current full
cross-runtime verification is **NOT RUN / NOT SATISFIED**, not waived by D14 or Python success.

## Human review and stop

The new [development discrepancy package](slice-2-ocr-feasibility-adjudication-2026-10-20.json)
provides exact sources/hashes, authored labels, outputs and null human decisions. The unchanged
[D16 held-out applicability](slice-2-d16-adjudication-applicability-2026-10-20.json) still has
**13 NOT_RUN** mandatory cases and no human reviewer/signature. No human approval or independent
quality evaluation is claimed. Review tone distinctions, digits/identifiers, punctuation/whitespace,
original-coordinate conventions and source-family/closure independence; second-check critical
ambiguity and sign exact dataset hashes. Corrections require new revisions, never historical edits.

Changed existing files: PDF parser; current ACTIVE, owner decisions, Phase 18 README and Slice 2
handoff. New: real-PDF regression file, development comparison script, this handback, evidence JSON
and development adjudication JSON. All earlier changes, reviews, receipts, datasets and old model
files are preserved. No production dependency, Java, migration, global policy or external activation
change; no commit/push. Stop for independent review/owner disposition. A capable verified OCR
configuration, genuine human held-out adjudication, subsequent S2-07 proof and exact S2-08 alignment
remain outstanding. FR18-3 and P18-FINAL/Phase 18 remain OPEN/incomplete.
