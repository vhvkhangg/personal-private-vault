# P18-S2 D16 official PaddleOCR feasibility handback — 2026-10-20

**BLOCKED / OWNER_DECISION_REQUIRED.** D16 is recorded; mandatory Slice 2 OCR is **vi/en**.
Three official-runtime configurations completed offline but none demonstrated capable Vietnamese
recognition. No production configuration is selected or promoted. This is implementer evidence,
not independent review, human adjudication, Slice 2 acceptance or another remediation round.

## Authority and preservation

This continues the [preceding finite checkpoint](slice-2-blocker-resolution-checkpoint-2026-10-20.md),
reusing its development sources, RapidOCR comparisons, native cancellation proof and unsigned
held-out package. [D16](owner-decisions.md#2026-10-20--d16-slice-2-vietnameseenglish-ocr-and-official-runtime-feasibility)
prospectively removes Chinese **OCR** from mandatory acceptance; native Chinese text/dependency
coverage and all other quality/resource gates remain. ADR-0023 and current acceptance/oracle/gate
documents are synchronized. Database, module, package, privacy and Phase 17 baselines do not change.

Entry HEAD/main: `c3b46351507803053850e3c2711602ee9c9ec419`. The new ignored receipt root is
`logs/phase18/s2-d16-paddleocr-2026-10-20/`; `entry.json` snapshots the inherited dirty tree, existing
checkpoint receipts and model hashes before edits. Earlier reviews, handbacks, four-script failures,
production source/tests/pins/artifacts and the frozen v3 dataset remain unchanged by D16 work.
The [D16 evidence inventory](slice-2-d16-paddleocr-evidence-2026-10-20.json) records exact commands,
receipt/output hashes, configurations, artifacts, metrics and preservation checks.

## S2-06 — official runtime comparison: FAILED

Isolated **Windows 11 x64 / CPython 3.12.13 / PaddleOCR 3.7.0 / PaddleX 3.7.2 /
PaddlePaddle CPU 3.3.1 / NumPy 2.3.5 / pdfplumber 0.11.10 / psutil 7.2.2**.
The real PaddlePaddle CPU self-check passes. Inspected PyPI 3.3.1 metadata supplies Windows wheels
for CPython 3.9–3.13, **none for the pinned production Python 3.14.4**. Compatibility here is an
actual isolated 3.12 test, not approval to replace the production interpreter or dependency lock.
The official [installation guidance](https://www.paddleocr.ai/main/en/version3.x/installation.html)
and [pipeline API](https://www.paddleocr.ai/main/en/version3.x/pipeline_usage/OCR.html) were consulted;
local package source and execution establish the exact tested behavior.

All candidates use official local **PP-OCRv6 small detection**, one of the recognizers below,
CPU/2 threads, MKL-DNN and HPI off, detection max-side 960, recognition batch 1, score filter 0,
and optional orientation/unwarping off. The score filter exposes all outputs for comparison; it
does not authorize them for production. `effective-{small2,latin,latin3}.yaml` retains complete
resolved configurations. No runtime ONNX/RapidOCR inference is substituted for Paddle inference.

| Official Paddle recognizer | Raw material-exact documents en / vi | Existing PDF path en / vi | Raw vi character errors | Wall / process CPU seconds | OS peak working set bytes |
| --- | --- | --- | ---: | ---: | ---: |
| PP-OCRv6 small | 3/3 / 0/3 | 2/3 / 0/3 | 7/123 | 18.919 / 30.063 | 589,897,728 |
| PP-OCRv5 Latin mobile | 3/3 / 0/3 | 2/3 / 0/3 | 8/123 | 20.511 / 35.031 | 560,836,608 |
| PP-OCRv3 Latin mobile | 3/3 / 0/3 | 2/3 / 0/3 | 15/123 | 22.182 / 35.734 | 555,851,776 |

Each run attempts **6 exposed development PDFs / 6 pages / 16 gold blocks / 2 scanned tables /
10 logical cells**: per script 3 files/pages, 8 gold blocks, 1 table and 5 cells. All 18 candidate-file
attempts completed; zero unreported files or skips. Historical `held-out-invoice-*` filenames are
already exposed **development**, not fresh held-out. Sources are real sanitized synthetic PDF files,
not private owner documents or a representative field corpus. No broader real-world accuracy claim.

`Kệ sách có 45 quyển.` becomes `K sách có 45 quyn` with v6 small (confidence **0.988393**);
v5 Latin likewise drops required characters. The invoice and warehouse Vietnamese headings also
lose/substitute material. V3 Latin substitutes accents and letters. English raw text, digits and
whitespace match these labels, including the heading before the table. Raw digit sequences match
all six sources in each run. The existing PDF path drops **both warehouse headings** while retaining
the 10 table cells; its digit sequence also loses heading-only numbers. This reproduces the earlier
S2-05/S2-06 coverage defect rather than repairing or accepting it.

All returned raw boxes are finite and within original page bounds. Exact English gold-block order
is preserved in raw output. All 10 table texts and measured merged spans match the source labels;
cell interior versus source border outlines differ by **up to 1.5 PDF units**. Missing/misread
Vietnamese blocks and dropped headings prevent full order/coverage acceptance. Human box convention
and ambiguity adjudication remain pending; no invented tolerance or locator PASS. `quality-metrics.json`
records original-coordinate boxes, per-cell differences/spans/order, and unnormalized CER numerators
and denominators. English raw CER is 0/139; parsed CER is 51/139 due to omitted heading content.

An initial interactive alphabet check was invalid because shell encoding replaced the probe letters.
It is **not evidence of character support**. The Unicode-safe experiment and actual live decoder
inspection show missing `ệ`, `ể`, `ề`, `ố` in v6 small and v5 Latin. Artifact and effective decoder
hashes are retained separately. Confidence cannot cure absent characters or establish completeness.
This corrects the preliminary commentary; no historical result is rewritten.

**Comparison with retained RapidOCR:** the preceding RapidOCR 3.10.0/ONNX v6 small and v5 Latin
experiments also failed the same exposed Vietnamese shelf/invoice texts and lost scanned-table
headings. Production RapidOCR-ONNXRuntime 1.4.4 still explicitly denies unverified Vietnamese OCR.
Changing runtime alone did not demonstrate a vi-capable pipeline. Prior medium timeout/four-script
failures remain unchanged. The older eight-document, four-script timing is a different workload;
these numbers are not a controlled speed ranking against RapidOCR.

## Artifact integrity, license, offline execution and resources

Only explicit development provisioning used network access. Official PaddlePaddle Hugging Face
repositories were downloaded at fixed revisions; LFS weight hashes match upstream metadata.
Local SHA-256 is rechecked before and after inference. Full model JSON/YAML/weight/README hashes
and immutable URLs are in the evidence inventory. No old artifact is overwritten or binary added
to the patch. Tested weight identities:

| Artifact (`inference.pdiparams`) | Bytes | SHA-256 |
| --- | ---: | --- |
| PP-OCRv6 small det | 9,817,888 | `5043d4ccc8d63402ccea8feefcee4db57077431a873e78d2191836a178a492da` |
| PP-OCRv6 small rec | 21,074,618 | `406e1e689c9a7fbb04178007a3fe10cf852afe7bf8bb3bc6dbb9d532b13bd907` |
| PP-OCRv5 Latin rec | 7,965,915 | `53cdc8b481a7394bb108f96d0fb3432b0a8f392e22c7d18f06dbb2d42b8b25f9` |
| PP-OCRv3 Latin rec | 8,940,218 | `fa6f22a8b2a669c7d5b4dfd2607491abd4cd2467adf7077c9cc70ce8c47b61d9` |

Official [v6 small recognition](https://huggingface.co/PaddlePaddle/PP-OCRv6_small_rec),
[v6 small detection](https://huggingface.co/PaddlePaddle/PP-OCRv6_small_det),
[v5 Latin](https://huggingface.co/PaddlePaddle/latin_PP-OCRv5_mobile_rec) and
[v3 Latin](https://huggingface.co/PaddlePaddle/latin_PP-OCRv3_mobile_rec) metadata declare Apache-2.0.
Installed PaddleOCR/PaddlePaddle/PaddleX license files and their hashes are retained. This is
license identification, not blanket approval of every bundled native dependency for redistribution.

The comparison command denies Python socket connect/connect_ex/create_connection/getaddrinfo **before
Paddle imports**, disables model-host checks, uses local paths and offline HF flags. **Zero attempted
connects** were recorded. Models remained unchanged; no surviving child processes were observed.
This demonstrates the tested offline path, not OS egress confinement or a production download policy change.

Resources include Paddle imports/model initialization, raw page OCR and existing PDF parsing/OCR;
they exclude interpreter launch and the earlier psutil import. Process CPU
can exceed wall time with two threads. Host has 6 logical CPUs and **17,112,092,672 bytes RAM**.
Paddle is CPU-only; no GPU inference/VRAM proof is claimed. Per-run dedicated TEMP/TMP/TMPDIR and
PaddleX cache were sampled at a requested **20 ms** interval: **0 bytes observed peak/final** in each.
Sampling can miss short-lived allocations and covers those namespaces, not all OS/native caches.
Working-set peaks are OS process counters; sampled RSS is also retained. No worst-case memory or
selected-production cancellation/resource certification follows. The isolated venv occupies
**784,770,915 logical file bytes**; all four downloaded model directories total **48,850,816 bytes**.
These are logical sizes after the experiment, not physical allocation or provisioning peaks.

## Genuine human review package — PENDING, NOT_RUN

The frozen [v3 review package](../../../rag-engine/tests/fixtures/phase18-s2-checkpoint-v3/REVIEW.md)
and manifest SHA `406e3aeedc78e943cd494e30452819d18e3ceba4e1bc2e4612845d5eea225e99`
are unchanged. The new [D16 applicability/adjudication ledger](slice-2-d16-adjudication-applicability-2026-10-20.json)
excludes only its two pure Chinese OCR scans from **mandatory** OCR acceptance, retaining them as
optional historical fixtures. Native Chinese Markdown/child/XLS/XLSX coverage stays mandatory.
All corrupt/locator-negative cases remain counted. Current denominator: **13 documents/negative
cases, 5 connected family/dependency units, 5 PDF pages, 43 gold blocks, 6 tables, 25 cells**.
Per-format and overlapping script denominators are explicit in that ledger; do not sum script rows.

No actual human reviewer/signature is supplied and **all 13 quality results remain NOT_RUN**.
Review originals beside source-authored gold; independently check family/near-duplicate and full
closure disjointness, diacritics/digits/whitespace, original ink/cell conventions, hidden/metadata
content and mandatory locators. Sign the exact manifest/applicability hashes and each ambiguity;
second-check acceptance-critical ambiguity. Corrections require a **new** dataset revision. Freeze
a sufficiently verified capable pipeline before independent held-out inference and human error
adjudication. No fresh held-out document was used to tune or compare these candidates.

## S2-07 and S2-08 — conditional next steps NOT SATISFIED

- **S2-07:** reuse the preceding actual Java → Python → native ONNX cancellation/deadline proof
  for its stated current-backend scope. D16 orders revalidation **after** a configuration is selected
  and verified; none qualifies. No fresh integrated Paddle cancellation/deadline/representative
  resource proof is claimed, and the old result does not certify Paddle or full-suite stability.
- **S2-08:** no Java identity allowlist change. Parser/chunker/OCR identities, exact rejection of
  unknown identities, legacy compatibility and actual worker provenance validation remain unchanged.
  Minimal tuple-specific alignment and mismatch tests remain conditional on a verified frozen
  producer. FR18-3/publication work is not included.

## Commands and verification

All long commands used `python scripts/run-bounded-command.py --profile normal --timeout-seconds N
--receipt <root>/<name>.json -- <command>`; PowerShell redirected stdout/stderr to the corresponding
`.log` and propagated the exit. Exact argv and verified receipt argv hashes are in the inventory.

| Command after watchdog `--` (paths relative to repo root) | Limit | Result |
| --- | ---: | --- |
| `uv venv --python 3.12.13 <root>/.venv` | 300 s | PASS |
| `uv pip install --python <root>/.venv/Scripts/python.exe paddleocr==3.7.0 paddlepaddle==3.3.1 pdfplumber==0.11.10 psutil==7.2.2` | 900 s | PASS |
| `uv pip install --python <root>/.venv/Scripts/python.exe python-docx==1.2.0 openpyxl==3.1.5 xlrd==2.0.2` | 180 s | PASS |
| `rag-engine/.venv/Scripts/python.exe <root>/provision.py` / `provision-latin3.py` | 600 / 180 s | PASS; official local artifacts only |
| `<root>/.venv/Scripts/python.exe <root>/runtime-check-corrected.py` | 120 s | Paddle CPU self-check PASS |
| `<root>/.venv/Scripts/python.exe rag-engine/scripts/slice2_paddleocr_checkpoint.py --output <root> --recognizer small --run-id small2` | 300 s | **FAILED / exit 1**, completed comparison |
| Same command with `--recognizer latin --run-id latin` | 300 s | **FAILED / exit 1**, completed comparison |
| Same command with `--recognizer latin3 --run-id latin3` | 300 s | **FAILED / exit 1**, completed comparison |
| `<root>/.venv/Scripts/python.exe <root>/inspect-decoder.py` | 120 s | PASS diagnostic, not OCR quality |
| `rag-engine/.venv/Scripts/python.exe -m pytest rag-engine/tests/test_slice2_checkpoint_package.py rag-engine/tests/test_slice2_corrections.py -k "pdf or scanned or overlapping or mixed or package" -q --junitxml=<root>/pytest-focused.xml` | 180 s | **12 passed / 29 deselected**, no skipped/failed/error |
| `rag-engine/.venv/Scripts/python.exe -m mypy --config-file rag-engine/pyproject.toml rag-engine/src` | 120 s | PASS, 22 production source files |
| `rag-engine/.venv/Scripts/python.exe -m ruff check rag-engine/src rag-engine/scripts rag-engine/tests` | 60 s | PASS |
| `rag-engine/.venv/Scripts/python.exe -m ruff format --check rag-engine/src rag-engine/scripts rag-engine/tests` | 60 s | PASS, 54 files |
| `rag-engine/.venv/Scripts/python.exe rag-engine/scripts/dependency_inventory.py --output <root>/locked-inventory-results.json` | 120 s | PASS, original 66 packages match lock |
| `git diff --check` | 60 s | PASS |

`<root>` is `logs/phase18/s2-d16-paddleocr-2026-10-20`. Failed initial self-check quoting and
the missing-parser-dependency OCR attempt remain retained under `runtime-check.*` and `small.*`.
Their corrected commands have distinct receipts; no failure was overwritten. Initial Ruff/format
diagnostics were corrected only in the new development script. No production or test implementation
changed. The earlier 117-test/11-Java-test checkpoint is retained, not repeated or added to these counts.

The required aggregate Java clean gate is still **FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips**.
Its receipt/log and historical evidence hashes remain preserved. There was no Java change and no
verified configuration to align, so no new Java suite/full gate was run. D14 applies only to Slice 1;
it is not a Slice 2 waiver. No new aggregate PostgreSQL stability claim.

## Changed files and remaining decision

D16 modifies only current documentation: ADR-0023; ACTIVE; Phase 18 owner decisions, README,
Slice 2 handoff, simplified plan, acceptance, held-out oracle and final-verification gate.
New files: this handback, D16 evidence inventory, D16 adjudication applicability JSON and the
[development-only experiment script](../../../rag-engine/scripts/slice2_paddleocr_checkpoint.py).
The isolated venv, models and raw receipts remain ignored. Existing dirty changes are preserved.

Remaining blockers: a demonstrated vi-capable offline recognizer/configuration; the existing
scanned-table material-heading omission; human source/locator/closure and held-out adjudication;
then selected-configuration native interruption/resource proof and conditional exact Java identity
alignment. A further bounded investigation or parser repair needs explicit follow-up scope; this
finite pass stops here. **Phase 17 COMPLETE/FROZEN; Slice 1 COMPLETE/CLOSED/D14; FR18-3 OPEN;
external indexing INACTIVE; live originals DISABLED/DEFERRED; Slices 3–4 NOT AUTHORIZED.**
No commit/push, automatic takeover, P18-FINAL/Phase 18 closure or readiness declaration.
