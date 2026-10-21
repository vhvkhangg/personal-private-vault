# P18-S2 bounded technical checkpoint — S2-06 / S2-07 / S2-08

**2026-10-20 — BLOCKED / OWNER_DECISION_REQUIRED.** Codex-authored technical checkpoint,
not independent review or finding closure. The owner authorized this one finite checkpoint and
subsequently instructed continuation after a usage quota reset. Completed provisioning, comparisons
and receipts were reused; the checkpoint was not restarted. Historical Antigravity **2/2 exhausted**
remains unchanged. No automatic further takeover or implementation follows.

## Authority, baseline and preservation

Read ACTIVE, the Slice 2 handoff, complete corrective handback/independent review, held-out oracle,
owner decisions and ADR-0023; inspected actual installed dependencies, model files, native supervisor,
HTTP client, Python worker/parser/OCR and Java finalizer identity gate. The resumed read confirmed
that no checkpoint handback had yet been written; the preceding corrective handback was the latest
historical handback. New evidence is recorded here without modifying it.

Entry/final branch **main**, HEAD `c3b46351507803053850e3c2711602ee9c9ec419`.
`logs/phase18/s2-blocker-checkpoint-2026-10-20/entry.json` hashes 2,164 pre-existing files and records
the inherited dirty tree. Production Java/Python, migrations, lockfile/interpreter, original model
artifacts, historical reviews/handbacks and earlier failed gates are unchanged by this checkpoint.
Only new development scripts, fixtures/tests and current governance/evidence are added/updated.

Applied Python RAG, local RAG, backend-testing, Graphify navigation and bounded-command guidance.
The owner's explicit finite authorization controls the previously stopped checkpoint and conditional
Java scope; it does not change global skills. No production worker network/download path was enabled.

## Technical decisions

| Blocker | Evidence / decision | Remaining gate |
| --- | --- | --- |
| **S2-06** | **BLOCKED.** RapidOCR 3.10.0 / PP-OCRv6 small and medium were provisioned with verified hashes in an isolated environment. Small completed 8 exposed development documents: 4 exact, 2 Vietnamese material losses, 2 Chinese block-joining whitespace differences awaiting human adjudication. Medium produced 3 results before a 300-second timeout and also lost Vietnamese characters at confidence 0.9999. A targeted PP-OCRv5 Latin recognizer experiment was 0/2 exact Vietnamese and also lost/substituted material. Neither v6 recognizer contains several required composed Vietnamese characters in its embedded alphabet. No capable four-script configuration was demonstrated. | Capable artifact/configuration; correct OCR-to-table material coverage; genuinely human-reviewed source/locator labels and independent held-out evaluation. No warning-only or blanket-rejection acceptance. |
| **S2-07** | **Native cancellation/request-deadline evidence supplied for the current pinned backend.** Real Spring admission → real bound/authenticated Python endpoint and PostgreSQL grant → real PDF parser/ONNX inference → unchanged Java supervisor termination. Final 2/2 integration tests pass; an earlier combined native-process group passed 11/11. Both final traces end with a native call entered without returning. Actual terminal states CANCELLED / ABORTED, empty owned job, zero staged output/chunks, sealed staging, scrubbed grants and released reservations are asserted. Native resource counters are retained. | Independent adjudication; resource/termination evidence for any future selected OCR configuration and representative admitted workloads is still required. These sample workloads are not worst-case/whole-Phase-18 certification. |
| **S2-08** | **BLOCKED; conditional Java alignment not performed.** Producer remains parser `ppv-parser-v18.2-whole-file-structural-bounded`, chunker `structure-first-v18.2-revision-role`, OCR `rapidocr-1.4.4-en-hans-script-admission-v18.2`. Java still admits its exact legacy v18.1/1.4.4 tuple. Since no sufficiently verified new OCR configuration exists, admitting an experimental identity would violate the owner's condition. Existing compatibility/unknown-identity rejection is unchanged. | Freeze a capable verified producer, then minimal tuple-specific Java alignment and positive/negative parser/chunker/OCR mismatch tests, preserving legitimate compatibility. No FR18-3/result-validator/publication changes are made. |

The four-script scanned-table development supplement adds **4 real generated PDF files / 4 pages /
24 gold blocks / 4 tables / 20 logical cells**. The actual candidate→production PDF path emitted
the 20 table cells but **omitted all four material headings above the tables**. It returned parsed
documents rather than rejecting the loss. This is a blocking OCR/PDF coverage observation related
to the still-unaccepted S2-05/S2-06 quality path, not a new authorization to broaden parser remediation.
Source cell outline versus emitted interior boxes differ by approximately 0.5–1.5 PDF units; that
geometry convention still requires human adjudication, not an invented acceptance tolerance.
One real public RapidAI image produced 18 OCR blocks without networking during inference; it has
no independent labels and is **not scored or accepted**. The supplement exits **1**.

## OCR candidates, artifacts and offline behavior

Reuse-first candidate runtime: Windows x64, Python **3.14.4**, RapidOCR **3.10.0**, ONNX Runtime
**1.31.0 CPU**, NumPy **2.5.4**, OpenCV-headless **5.0.0.93**, Pillow **13.0.0**. The candidate environment
is under ignored `logs/phase18/s2-blocker-checkpoint-2026-10-20/.venv`; its read-only access to existing
parser/ORT dependencies does not install into the pinned worker. Effective imported module paths/versions
are in `effective-environment-and-original-models.json`; the initial broad distribution listing can
contain duplicate inherited distributions and is not the effective-module inventory.

| Exact candidate artifact | Bytes | SHA-256 |
| --- | ---: | --- |
| PP-OCRv6 det small | 9,929,594 | `090f04abcd9d9a7498bc4ebf677e4cb9bdce1fe4197ddb7e529f1ef44e1ff94f` |
| PP-OCRv6 rec small | 21,234,383 | `6f327246b50388f3c176ae304bd95767ea6dc0c9ae92153ef8cbe210b3c14884` |
| PP-OCRv6 det medium | 62,119,454 | `92078b7355007ccfffcd4c8cd441a3afd4538904d06881b29a155e1e679907c2` |
| PP-OCRv6 rec medium | 76,629,984 | `eef444829dbbe18d7fea59a3f6eb75647518d2b3a9568d27c92e42940204894b` |
| PP-OCRv5 Latin mobile rec | 7,904,513 | `b20bd37c168a570f583afbc8cd7925603890efbcdc000a59e22c269d160b5f5a` |

Registry/source verification: [RapidOCR v3.10.0 model registry](https://github.com/RapidAI/RapidOCR/blob/v3.10.0/python/rapidocr/default_models.yaml),
[release Python requirements](https://github.com/RapidAI/RapidOCR/blob/v3.10.0/python/pyproject.toml),
[official PP-OCRv6 description](https://github.com/PaddlePaddle/PaddleOCR/blob/main/docs/version3.x/algorithm/PP-OCRv6/PP-OCRv6.en.md).
The RapidOCR source/model notice and official [medium recognition](https://huggingface.co/PaddlePaddle/PP-OCRv6_medium_rec_onnx),
[medium detection](https://huggingface.co/PaddlePaddle/PP-OCRv6_medium_det_onnx) and
[Latin ONNX](https://huggingface.co/PaddlePaddle/latin_PP-OCRv5_mobile_rec_onnx) cards identify Apache-2.0;
local notice copies/hashes are preserved. Model availability/advertised language support is not
demonstrated fidelity. Converted weights are identified by their RapidAI registry hashes, not
assumed byte-equivalent to another upstream representation. The public sample's dataset/image terms
are separate; it is retained only as an ignored local diagnostic, not redistributed with this patch.

Before offline inference, every candidate file is hash-checked; Python socket creation is denied
through initialization and inference. Explicit development provisioning happens earlier. This proves
the tested offline application paths, not OS egress confinement. CUDA is **UNAVAILABLE** in the installed
ORT providers; CPU inference is real, not a simulated GPU result. No production pin, model or identity
is promoted. The embedded-alphabet inspection records missing `ề`, `ể`, `ệ`, `ố` in both v6 artifacts;
space is separately added by the decoder and is not treated as an unsupported language character.

Small: **76.033 s wall / 110.625 s CPU / 911,548,416 bytes process peak working set** for the eight-document
double-pass diagnostic. Medium: **TIMEOUT 300.156 s / exit 124**, three completed results, five unreported;
an external sample observed **1,498,959,872 bytes peak working set / 297.875 s cumulative CPU**.
Both tracked medium launcher/worker PIDs were subsequently absent. Concurrent verification affected
host load; these are actual diagnostics, not isolated latency benchmarks. Supplement: **47.210 s wall /
88.531 s CPU / 718,303,232 bytes process peak working set**. Device-wide VRAM samples are not OCR GPU
allocation or peak inference evidence. No automatic threshold adjustment follows from high confidence.

## Native lifecycle and resource proof limits

The test-only import overlay exposes the **real worker app**, preserving its header/session/nonce,
grant verification and supervisor. It supplies public generated PDF bytes to the existing parser
after real grant validation; it neither enables external-source dispatch nor stages/publishes a result.
Instrumentation writes an entry immediately before `InferenceSession.run` and a return immediately
after it. Final events show entry without return when the owned process is killed. This is an
instrumented native-call boundary, not a kernel instruction-pointer trace or power-loss experiment.

The existing Java HTTP deadline is configured to **10 seconds in this disposable test context**.
The deadline case proves that request deadline and subsequent native termination; it is not an
immutable SQL E1 expiry or a claim that Python cooperative checkpoints interrupt every C call.
The existing supervisor/process implementation and Phase 17 contracts are unchanged. A disposable
test context binds the workload directory explicitly because inherited dynamic properties otherwise
selected the ordinary worker. No second engine/queue or production hook is introduced.

Final per-boundary receipts (`native-final-cancel.json`, `native-final-deadline.json`):

- Cancellation: **CANCELLED**, **0.690 s** action→terminal; peak native job committed memory
  **594,997,248 bytes**, CPU **4.171875 s** combined user/kernel.
- Request deadline: **ABORTED**, **12.044 s** launch→terminal; peak native job committed memory
  **841,502,720 bytes**, CPU **38.421875 s** combined user/kernel.
- Both: native job/process memory below existing **2 GiB** v3 job cap; stopped job counters include
  terminated processes. Memory is **committed**, not RSS. Windows job transfer counters include I/O
  beyond physical disk; they are not temporary-disk peak. The dedicated test TEMP/TMP namespace is
  empty at finish and removed; zero final bytes is not an invented disk-peak measurement. Earlier
  measured producer-local scratch sampling remains attributed to the corrective handback.

Earlier fixture failures are retained: missing overlay version, inherited directory selection, then
a 20-page fixture correctly rejected by the unchanged aggregate embedded-raster budget. The corrected
fixture repeatedly processes one valid bounded PDF. No production limit was weakened or failed test
skipped. The final passing run follows those concrete fixture fixes, not an unchanged lucky rerun.

## Fresh held-out/adjudication package

[Review package](../../../rag-engine/tests/fixtures/phase18-s2-checkpoint-v3/REVIEW.md),
[immutable manifest](../../../rag-engine/tests/fixtures/phase18-s2-checkpoint-v3/manifest.json),
[unsigned adjudication](../../../rag-engine/tests/fixtures/phase18-s2-checkpoint-v3/adjudication.json).
Manifest SHA-256: `406e3aeedc78e943cd494e30452819d18e3ceba4e1bc2e4612845d5eea225e99`.

**15 synthetic source documents/negative fixtures, 7 family IDs (5 connected family/dependency
units), 7 PDF pages, 59 gold blocks,
8 table instances, 35 logical cells; all 15 NOT_RUN.** Per-format/script denominators and original
source/child hashes are explicit; multi-script rows overlap. Labels derive from source authoring and
original structures, not parser/OCR output. Format coverage includes TXT, Markdown/child closure,
hidden/anchored DOCX, native/scan/mixed PDF, hidden/merged/formula XLSX, XLS, corrupt input and missing
locator negatives. Translation/variant/child/mixed closures remain wholly held-out. Historical
shelf/invoice, new warehouse documents and public samples are development only.

The package closure audit supplies connected-group denominators per format/script; 7 declared
family IDs must not be treated as 7 independent groups. Hash/group/closure checks pass; semantic
near-duplicate independence, label correctness and material
error adjudication still need a **real human** and independent check. There is **no human signature**.
Do not claim genuinely human-adjudicated evidence. Before any evaluation, an initial source-generation
draft was archived under ignored `preflight-dataset-v1`; v2 corrected source newline/line-range authoring labels; it is also archived under
`preflight-dataset-v2`. V3 canonicalizes fixture JSON/manifest to LF so Git normalization cannot
invalidate their hashes. No material reference content or OCR result was tuned in that correction.
No held-out result was observed or used for tuning in any version. The source/manifest integrity
tests do not run the held-out parser/OCR or turn unsigned labels into acceptance.

## Verification and exact commands

Full raw receipts/logs and source/config hashes are catalogued in the adjacent
[checkpoint evidence inventory](slice-2-blocker-checkpoint-evidence-2026-10-20.json).
All potentially long commands used `scripts/run-bounded-command.py`, ordinary limits ≤600 seconds
in this checkpoint. Commands run from repository root. Let **P** be `rag-engine/.venv/Scripts/python.exe`,
**C** be `logs/phase18/s2-blocker-checkpoint-2026-10-20/.venv/Scripts/python.exe`, and **L** be
`logs/phase18/s2-blocker-checkpoint-2026-10-20/`:

`python scripts/run-bounded-command.py --profile normal --timeout-seconds LIMIT --receipt L/RECEIPT -- EXECUTABLE ARGUMENTS`

| Executable / arguments | LIMIT / receipt | Result |
| --- | --- | --- |
| P `-m pytest rag-engine/tests -q --junitxml=L/python-final.xml` | 600 / `python-final.json` | **117 passed / 0 failures / 0 errors / 0 skips**; XML time 78.886 s, wrapper 87.860 s, before the package-only canonical revision. Includes actual producer-local OCR/parser/embedding and 2 new package-integrity tests, not held-out quality acceptance. |
| P `-m pytest rag-engine/tests/test_slice2_checkpoint_package.py -q` | 180 / `canonical-package-tests.json` | **2/0/0/0**, current v3 canonical hash/source/closure checks; the same 2 cases, not 2 additional unique tests. No held-out inference. |
| P `-m ruff check rag-engine backend/src/test/resources/rag/native-ocr-worker/src/ppv_rag` | 180 / `ruff-canonical-final.json` | PASS |
| P `-m ruff format --check rag-engine backend/src/test/resources/rag/native-ocr-worker/src/ppv_rag` | 180 / `format-canonical-final.json` | PASS, 56 files |
| P `-m mypy rag-engine/src/ppv_rag` | 180 / `mypy-final.json` | PASS, 22 production source files |
| P `rag-engine/scripts/dependency_inventory.py --output L/dependency-inventory.json` | 180 / `dependencies-final.json` | PASS, original 66 packages match lock |
| `mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=RagSlice2NativeOcrCancellationIntegrationTest,RagNativeWorkerProcessTest test` | 600 / `native-integration-resources.json` | **11/0/0/0**, exit 0 |
| Same Maven flags; `-Dtest=RagSlice2NativeOcrCancellationIntegrationTest` | 600 / `native-final.json` | **2/0/0/0**, exit 0; final overlay import/format/resources, 86.875 s wrapper. These 2 are a rerun of the same class, not 2 additional unique cases. |
| C `rag-engine/scripts/slice2_ocr_checkpoint.py compare --size small --output L/candidates` | 300 / `compare-small-2.json` | Diagnostic completed exit 0; **quality FAILED**, 4/8 exact and material Vietnamese loss. Command completion is not quality PASS. |
| Same with `--size medium` | 300 / `compare-medium.json` | **TIMEOUT / 124**, incomplete quality comparison |
| Same with `--size latin` | 180 / `compare-latin.json` | Diagnostic completed exit 0; **quality FAILED**, 0/2 Vietnamese exact |
| C `L/development-supplement.py` | 240 / `development-supplement.json` | **FAILED / 1**, four omitted material headings; real public document unlabelled |
| C `L/inspect-alphabets.py` | 120 / `inspect-alphabets.json` | Metadata diagnostic completed; reports missing Vietnamese codepoints |
| P `rag-engine/scripts/prepare_slice2_adjudication.py --output rag-engine/tests/fixtures/phase18-s2-checkpoint-v2` | 180 / `prepare-adjudication-v2.json` | Initial v2 package preparation PASS; subsequently preserved in ignored evidence. V3 canonicalizes metadata without running OCR; `canonical-package-tests.json` verifies it. Human review/evaluation NOT_RUN. |

Provisioning argv, all pre-fix failed attempts, exact receipts and logs are in the evidence inventory;
no earlier failure is erased. Final `git diff --check` and preservation checks are recorded there.

**Original required Java full gate remains FAILED: 1,433 tests / 1 failure / 2 errors / 0 skips,
exit 1**, retained at `logs/phase18/s2-takeover-full-java-2026-10-20.{json,log}` with its summary and
the earlier Slice 1 evidence. It was not rerun unchanged solely to seek a green result: this checkpoint
changes no production Java/SQL/Python or pins. New focused tests do not establish aggregate stability
or waive that gate. D14 is not a Slice 2 waiver. Native batch identity/provenance and PostgreSQL
connection-slot limitations remain open; no P18-FINAL claim is made.

## Changed files and stop

New development scripts: `rag-engine/scripts/slice2_ocr_checkpoint.py`,
`rag-engine/scripts/prepare_slice2_adjudication.py`. New tests:
`rag-engine/tests/test_slice2_checkpoint_package.py`,
`backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSlice2NativeOcrCancellationIntegrationTest.java`,
and its two-file test-only Python overlay under `backend/src/test/resources/rag/native-ocr-worker/src/ppv_rag/`.
New source/label/review package: `rag-engine/tests/fixtures/phase18-s2-checkpoint-v3/`.
Current ACTIVE/handoff/owner decision and this handback/evidence inventory record the outcome.
No large models/binaries are tracked or staged; only small sanitized document fixtures are added.

**Stop: BLOCKED / OWNER_DECISION_REQUIRED.** A bounded next owner decision must address the failed
Vietnamese artifacts and OCR/table material coverage, obtain actual human label/error review, and
authorize any further work after this checkpoint. Conditional identity alignment and independent
held-out/runtime verification cannot be claimed complete. No findings are independently closed by
this implementer. Phase 17 COMPLETE/FROZEN, Slice 1 COMPLETE/CLOSED under D14, FR18-3 OPEN, external
indexing INACTIVE and live originals DISABLED/DEFERRED remain. Slices 3–4 are not authorized.
No commit/push/tag/PR or automatic next takeover.
