# D18-PT-A feasibility checkpoint — 2026-10-18

**STOPPED: R7 FAIL; Vietnamese/scanned-table OCR FAIL; resources NOT_VERIFIED.**
Implementer-authored checkpoint for independent review, not independent acceptance.
Phase 18 remains CHANGES_REQUESTED / INCOMPLETE. No FR18 finding is closed.

## Authority and preserved entry state

The owner explicitly selected D18-PT-A in a new, separate implementation session:

> Keep ADR-0021/D18-A and AC-01–AC-08 unchanged. Authorize ONE bounded feasibility
> checkpoint: one test-scoped native Windows R7 external-file integration candidate,
> one justified Vietnamese/scanned-table OCR candidate with source-grouped development
> and freshly reserved held-out fixtures, and separate target-host resource measurements.
> Keep ordinary live external-file ingestion disabled. Record PASS, FAIL or NOT_VERIFIED
> per lane, with evidence. STOP after this checkpoint.

This is not a third Antigravity round, new handoff, automatic takeover continuation,
contract amendment, Phase 18 acceptance or permission for a second candidate.
The [independent post-takeover review](reviews/2026-10-18-phase-18-independent-post-takeover-review.md)
and its finding IDs remain unchanged. ADR-0020, ADR-0021/D18-A and AC-01–08 are unchanged.

Starting and ending HEAD: `435ffea979681f22d723f551dd8b0cd0b733c8cd`, branch `main`.
The initial porcelain inventory has **108 entries**, including untracked implementation,
models and previous evidence, preserved at `logs/phase18-d18-pt-a/starting-status.txt`.
No reset, clean, checkout, commit, push, tag, PR or user-process termination was used.
The inherited 126-file runtime snapshot was rechecked: **zero drift** against
`logs/phase18-takeover/final-source-snapshot.json`. See the new
`inherited-snapshot-check.json`. Historical 1,321 Java / 13 focused Java / 72 Python
results remain historical; the full suites were not repeated or relabelled current.

Read the actual root/backend/test AGENTS, worker README, ACTIVE handoff, ADRs 0020/0021,
owner decisions, AC ledger, held-out/locator oracle, P18-FINAL, takeover record and
independent review/evidence. Applied local-rag-engineering, python-rag-coding,
backend-testing, persistence/migration, bounded-command and review-traceability guidance.
No migration or production file was changed. The Graphify executable's recorded Python
failed initialization because of the inherited PYTHONHOME/interpreter mismatch; direct
graph lookup located the guard/authority/finalizer, then actual sources were inspected.

## Lane A — one existing-handle integration candidate: FAIL

Selected approach: retain the **existing** `WindowsNativeFileGuard` handle around a
real Java JDBC physical PostgreSQL COMMIT, first checking its native safety prerequisites
before connecting it to external authority, supervised worker input or publication.
Only the explicitly source-launched diagnostic can run this path. There is no new
production flag, Spring bean override, caller-content input or forged worker receipt.

The diagnostic reused the existing native Java classpath and pinned Testcontainers
PostgreSQL 18.6 / pgvector 0.8.6 infrastructure. It created only disposable sanitized
NTFS fixtures and a disposable database marker table. It held a real guard while the
driver committed a marker, and checked marker visibility from another connection.
**That marker is not an index generation or a publication receipt.**

| Native prerequisite | Actual observation |
| --- | --- |
| Ordinary source handle across physical C | Write attempt denied immediately before and after `Connection.commit()` while the same handle remained open; a separate connection saw one committed marker row. |
| Release after known physical C | File became writable after lexical handle closure; fixture deletion succeeded. |
| Existing hard-link alias | `Files.createLink(alias, file)` succeeded, then `openAndGuard(file)` **accepted** the two-link file. Required unsupported/unproved alias denial was absent. |
| Ancestor rename | Attempted `Files.move(parent, moved)` while the child guard remained open was denied on this host. No claim that the hypothetical namespace-replacement branch ran. |
| Pre-existing writable mapping | Java FFM READ_WRITE mapping retained after its channel closed; guard acquisition was denied. No mapping mutation or bypass observed. |
| Cleanup | Owned fixture directory removed; temporary PostgreSQL container stopped in `finally`; source-launch JVM exited. |

**Concrete failure, not Windows impossibility:** this guard calls `CreateFileW` with
`GENERIC_READ`, `FILE_SHARE_READ`, `OPEN_EXISTING`; it queries the 52-byte
`BY_HANDLE_FILE_INFORMATION`, but never reads/enforces `nNumberOfLinks` at offset 40.
The candidate therefore accepted the intentionally unsupported/unproved hard-link case.
This does **not** show that a write through the alias bypassed sharing, or that NTFS
cannot support a corrected candidate. It demonstrates a missing fail-closed prerequisite
in this selected candidate. No hypothetical writable-mapping bypass is asserted.

Receipt `r7-command.json`: **exit 1, 7.867 s**. Results and native console:
`r7-result.json`, `r7-console.log`. The failure is retained, not converted to a passing
JUnit result. No second R7 design or post-failure guard repair was attempted.

**Full R7 integration is NOT_VERIFIED:** no actual individually approved external-source
authority -> complete reservation closure -> supervisor -> restricted worker staging ->
index publication was exercised. No unapproved source/child was supplied to the worker.
Identity/version/root policy changes versus finalization, new writer/mapping races,
reparse/symlink integration, revocation in both orders, rollback, unknown-C recovery and
reservation/handle release on those paths remain unrun. Marker-C proof must never be
cited as satisfying these cases. A failed prerequisite prevented safe continuation;
FR18-3 was neither bypassed nor expanded to produce a misleading positive result.

An existing real application containment test independently passed: registration still
throws `Live external-file capability is disabled`. **Ordinary live ingestion stays disabled.**

## Lane B — one native Vietnamese / ruled-table candidate: FAIL

Selected **one configuration**: the Windows x64 native DLL shipped in NuGet
`Tesseract` **package 5.2.0**, publisher `tessdata_best` Vietnamese LSTM data, OEM 1,
single-line PSM 7 per detected cell/region, zero retry, existing OpenCV ruled-cell
segmentation, original PDF placement from existing pdfplumber/PDFium at 144 DPI.
This remains an opt-in diagnostic. RapidOCR production code and dependencies are unchanged.

Rationale: language-specific Vietnamese data directly addresses the demonstrated accent
loss; explicit image-grid cell detection addresses structure before recognition and
supports a merged header. Tesseract itself is **not** a table-structure guarantee:
[upstream quality guidance](https://tesseract-ocr.github.io/tessdoc/ImproveQuality.html)
requires custom segmentation for tables. The [language-data documentation](https://github.com/tesseract-ocr/tessdoc/blob/main/Data-Files.md)
lists Vietnamese and LSTM-only use of tessdata_best. The
[native wrapper publisher](https://github.com/charlesw/tesseract) documents Windows
native binaries and the Visual C++ runtime requirement. The diagnostic calls the
[upstream C API](https://github.com/tesseract-ocr/tesseract/blob/5.2.0/include/tesseract/capi.h)
using stdlib ctypes; no Python wheel ABI downgrade or .NET installation was needed.

**Actual compatibility evidence:** CPython **3.14.4**, Windows 11 x64, native DLL
loaded/initialized/inferred successfully. The package's DLL reports **TessVersion()
`5.0.0`**, despite its package being 5.2.0. Preserve that discrepancy; the frozen config's
`Tesseract 5.2.0` string identifies the package, not a verified native engine version.
No DLL/engine switch was made. This is an additional reason not to select it for production.
Native execution was CPU-only. ONNX Runtime 1.31.0 still exposes Azure/CPU providers,
not CUDA; no inference on the RTX 2070 or GPU-to-CPU fallback was demonstrated.

Deliberate provisioning fetched only local candidate artifacts before evaluation.
The runtime evaluation performs no downloads, remote OCR or telemetry. No Python,
uv.lock, dependency, driver or CUDA installation changed. Runtime egress interception
was not performed, so this is not full P18-OFFLINE-MODEL acceptance.

| Artifact | SHA-256 |
| --- | --- |
| NuGet Tesseract 5.2.0 archive | `202d82fc7c7d8384df7da57206d5e1f456ccdabd648c46e67cdfaa3a911d4795` |
| x64 tesseract50.dll | `de4d04ec75095374d98f5dd7a60d14d7e2e0f76589db693eccf7ae658be8cb2b` |
| x64 leptonica-1.82.0.dll | `dfcb3e6ed0b16bc55bfdbcf53543cfe42a354b87c3e35bd3a95eebf005d73e76` |
| vie.traineddata | `b6b49293d95d0b6dbd8780174627e82c75be957b6f4ed9862155540d6b00bb45` |

Data is pinned to publisher commit `e12c65a915945e4c28e237a9b52bc4a8f39a0cec`.
`ocr-artifacts.json` records exact URLs, sizes, hashes and notices; artifacts stay
under ignored logs, not eligible source weights. Tesseract/traineddata use Apache-2.0;
Leptonica uses BSD-2-Clause. The upstream data license is retained locally. Full bundled
DLL notice/security/redistribution review is not claimed by this private-use checkpoint.

### Frozen groups, outputs and denominators

Before provisioning or inference, froze four sanitized synthetic groups in
`rag-engine/tests/fixtures/d18-pt-a/manifest.json`, SHA-256
`9c1d7b16e0c5d9d4b4f18db95e37680c556b8b6bb466ab3e0f02e8ff50897801`;
configuration SHA-256 `c153d3e83d84e1713e603ddfd479fd3a1ae308e924d4ab22e50ffd5e328cfbda`.
Each file hash, source key/version/field, original page, text/cell label and box is
recorded. Development groups: shelf regression / office stock. Reserved held-out
groups: medical appointment / transport timetable. No translations, duplicates,
dependency edges or transcluded children cross splits; all four PDF hashes are distinct.
The old exposed eight-PDF set was not reused as held-out data.

This is source-group separation, **not independent human adjudication**. Both tables
use the same synthetic generator/template, limiting structural generalization; do not
interpret this as independent real-scan table acceptance or proof against all near-duplicate
layout leakage. No candidate parameter tuning occurred. Human review of reference labels
and any ambiguous scans is PENDING; fixtures have now been exposed and cannot be reused
as a fresh held-out acceptance set. Chinese and broader-format acceptance was not run.

| Split | Groups/docs/pages | Tables/cells | Exact OCR blocks | Exact cells | Material failed files | Literal CER |
| --- | --- | --- | --- | --- | --- | --- |
| Development | 2 / 2 / 2 | 1 / 7 | 7 / 8 | 7 / 7 | 1 / 2 | 1 edit / 52 reference codepoints = 1.923% |
| Reserved held-out, once | 2 / 2 / 2 | 1 / 7 | 6 / 8 | 5 / 7 | 1 / 2 | 2 edits / 79 reference codepoints = 2.532% |

Zero missing results/skips. Each table has four rows/two base columns, a merged
two-column first row, six ordinary cells and one merged cell. Both detected **7/7**
cell boxes and correct row/column/spans; all **8/8** region/cell boxes per split matched
the deterministic original-page oracle within the frozen 2-point tolerance. These are
region/cell boxes, not human-adjudicated exact character boxes. Page/source/version/field
attribution is diagnostic fixture metadata, not proof of Java-approved publication.

Actual material errors:

- Development: `Kệ sách có 45 quyển.` -> `Kệ sách có 45 quyền.`
- Held-out timetable: `Chuyến sáng` -> `Chuyển sáng`; `Chuyến tối` -> `Chuyến tồi`.
- Held-out appointment: `Bác sĩ hẹn khám vào thứ Sáu.` matched exactly.

The mixed PDF's native `Transit schedule` line was extracted once separately from
the raster cells; cell order was top-to-bottom / left-to-right. This is a constrained
upright, ruled synthetic case, not general mixed/rotated/multicolumn PDF support.
All four source files remained hash-identical. No content was published. Label-aware
diagnostics rejected files containing material errors with exit 1; production semantic
error detection, arbitrary scanned/unruled/row-merged tables and full parser integration
remain NOT_VERIFIED. High confidence was not used as the acceptance oracle.

Both actual OCR commands returned **exit 1**. Retained raw outputs:
`ocr-development.json`, `ocr-held_out.json`, both `ocr-*-console.log`, and `quality-ledger.json`.
The initial development TSV reader used an assumed header and recorded empty optional
word arrays; before the held-out run the harness was corrected to use the C API's
headerless field names. No recognition/segmentation setting or fixture changed and no
development inference was repeated. Held-out word arrays contain 18 word records.
The original failed development output remains intact; it is not word-box evidence.
There was no second candidate, retry/tuning loop or production integration of this failed candidate.

## Lane C — resources/operations: NOT_VERIFIED

Measured both selected OCR runs using Win32 process snapshots, retained process
handles, GetProcessMemoryInfo, GetProcessTimes and GetProcessHandleCount. Each tree
contained the launched venv process and its owned base-interpreter child; **two PIDs**
sampled, none unreadable. Sampling target was 20 ms (39 development / 37 held-out samples).
The monitor itself and unrelated user processes are excluded. No user processes were stopped.

| Metric | Development | Held-out | Interpretation |
| --- | --- | --- | --- |
| Sampled tree working-set maximum | 117,870,592 B = 112.410 MiB | 117,891,072 B = 112.430 MiB | Sampled lower bound, includes both owned PIDs; not an exact continuously observed peak. |
| Sum of per-process OS lifetime peak working sets | 123,441,152 B = 117.723 MiB | 127,098,880 B = 121.211 MiB | Conservative upper bound; individual peaks need not coincide. |
| Sampled tree private memory maximum | 258,887,680 B | 259,649,536 B | Separate from resident working set. |
| Minimum observed host available physical RAM | 2,734,170,112 B | 2,682,531,840 B | Other running apps affect headroom; no attempt to evict them. |
| Tree CPU time | 0.875 s | 0.828125 s | Sum of observed retained-process kernel+user times. |
| Average one-core CPU use | 76.55% | 77.05% | Not whole-machine utilization or an enforced CPU cap. |
| Child-tree wall time | 1.143103 s | 1.074729 s | Includes interpreter/startup; watchdog durations below include monitor too. |
| OCR evaluation inner duration | 0.562907 s | 0.607381 s | Recorded separately from process startup. |
| Observed peak files in owned TMP/TEMP | 0 B | 0 B | Measured exclusive directories; may miss files shorter than sampling interval or library writes elsewhere. |
| Source PDF input bytes | 114,675 B | 126,533 B | Two single-page PDFs per run. |
| Expanded image buffers | 5,760,000 B full RGB page + 1,749,600 B region | Same | Logical dimensions only; not a complete native-allocation peak or disk expansion guard. |
| Observed peak tree handle count | 410 | 410 | Not a per-library leak audit. |
| Completion cleanup | No owned PID survivors; owned temp directory removed | Same | OS process exit releases native handles; no cancellation/restart proof. |

Host: Windows 11 build 26300; i5-9600K, six cores/logical CPUs; physical RAM API reports
**17,112,092,672 B** (16 GB class). NVIDIA inventory confirms **RTX 2070, 8,192 MiB**, driver
610.88. Initial device-wide usage **1,927 MiB** is merely a system snapshot, **not job VRAM**.
**Attributable worker peak VRAM is UNAVAILABLE / NOT_VERIFIED, not zero.** No CUDA provider
was installed and no GPU inference/fallback result is invented. See `resources-*.json`,
`environment.json`, hardware/GPU receipts and the GPU console.

The failed small CPU workload does not justify production RAM/VRAM/CPU/temp/time caps.
No new production thresholds are proposed for activation. Existing v3 supervisor 2 GiB
job cap, Python 10 MiB input / 500-page / 1M-character limits and unchanged old-path
100-source / 1 MiB / 15-minute ceilings remain as before; they have not been accepted
for all required Phase 18 documents. Diagnostic guard budget was 4,096 B; measurement
inner deadline 150 s; command ceilings were shorter than the owner maxima. Those are
diagnostic bounds, not a changed user-job contract. No incremental-processing claim is
made: fixtures were read into memory and each raster page rendered in full.

R7 tree peaks, actual native v3 indexing resource bounds, reliable instantaneous tree
peak, attributable GPU VRAM, GPU/OOM fallback, expanded-input guard, over-limit/cancel/
restart/unknown-process cases and native handle cleanup after cancellation are
**NOT_VERIFIED**. Measurement limitations are retained rather than reported as zero/PASS.

## Files and commands

Only these session changes were made (prior changes remain preserved):

| File(s) | Reason |
| --- | --- |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagR7FeasibilityCheckpoint.java` | Opt-in source-launch native/PostgreSQL prerequisite diagnostic. No JUnit discovery or production activation. |
| `rag-engine/scripts/run_d18_pt_a_native.py` | Reuse actual verified Java test classpath to launch that diagnostic. |
| `rag-engine/scripts/d18_pt_a_ocr_checkpoint.py` | Freeze fixtures, deliberate local provisioning, one-candidate OCR/cell comparison. Runtime worker never imports it. |
| `rag-engine/scripts/measure_d18_pt_a.py` | Narrow Windows measurements of the selected OCR process tree. |
| `rag-engine/tests/fixtures/d18-pt-a/{manifest.json,manifest.sha256,shelf-regression.pdf,office-stock.pdf,medical-appointment.pdf,transport-timetable.pdf}` | Sanitized immutable source groups, labels and hashes. |
| This report and `d18-pt-a-evidence-2026-10-18.json` | Reviewable results, receipt/artifact hashes and exact limitations. |
| `docs/implementation/handoffs/ACTIVE.md` | Truthful checkpoint/status pointer only. |

Raw logs, local weights/DLLs, command receipts, sample reports, helper summary and
artifact manifest are under ignored `logs/phase18-d18-pt-a/`. No global installs or new
agent, skill or delivery workflow was created. Existing model delivery hygiene remains
the prior review's open observation, not an unrelated cleanup performed here.

All blocking commands used `scripts/run-bounded-command.py`, normal profile.
From repository root, with `P=rag-engine/.venv/Scripts/python.exe`, the common prefix is
`P scripts/run-bounded-command.py --timeout-seconds N --receipt logs/phase18-d18-pt-a/NAME --`.
No shell-generated Bash/PowerShell command nesting was used.

| Child command after prefix | Budget | Exit / duration | Receipt |
| --- | --- | --- | --- |
| `P rag-engine/scripts/run_d18_pt_a_native.py` | 300 s | **1 / 7.867 s** | `r7-command.json` |
| `P rag-engine/scripts/d18_pt_a_ocr_checkpoint.py freeze` | 30 s | 0 / 0.424 s | `freeze.json` |
| Same script, `provision` | 180 s | 0 / 4.142 s | `provision.json` |
| `P rag-engine/scripts/measure_d18_pt_a.py development` | 180 s | **1 / 1.343 s** | `ocr-development-command.json` |
| Same script, `held_out` | 180 s | **1 / 1.297 s** | `ocr-held-out-command.json` |
| `mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=RagDocumentBatchIntegrationTest#testExternalFileDefaultContainmentDisabled test` | 180 s | **1 test passes**, 0 failures/errors/skips; 0 / 41.857 s | `containment-java.json` |
| `P -m pytest rag-engine/tests/test_pdf_geometry.py rag-engine/tests/test_telemetry.py -q` | 45 s | **6 pass**, pytest 2.84 s; 0 / 5.777 s | `python-geometry.json` |
| `P -m ruff check` with the three new Python scripts above | 30 s | 0 / 0.145 s | `ruff-final.json` |
| `P -m ruff format --check` on the same three scripts | 30 s | 0 / 0.138 s | `format-check-final.json` |

Provisioning/inference/Java stdout and stderr were retained in their named console
logs; focused Python/static/hardware stdout also exists in the execution tool transcript.
Maven receipt and child both exit 0; no claim based solely on the enclosing PowerShell
exit. Hardware/GPU, summary and formatting receipts are included in the evidence JSON.
Initial lint failures (13 findings, then three remaining) and failed Graphify launch
remain preserved as nonzero receipts. Lint fixes affected diagnostic formatting/imports,
not OCR selection or frozen labels. Two convenience result-readback commands failed
on Windows console/default codepage; UTF-8 file reads/ASCII JSON presentation corrected
readback without rerunning inference or changing raw results. No timeout occurred.

The six focused Python tests verify retained native geometry/telemetry behavior;
the one Java test verifies default containment. They do not validate all new diagnostic
code or Phase 18. No full backend suite, full Python suite, mypy-on-unchanged-production,
full P18-FINAL or independent acceptance was claimed. `git diff --check` passes with
existing CRLF normalization notices. The new Java diagnostic also compiled during the
focused Maven run; it is not an automatically executed JUnit test.

## Remaining findings and finite next decision

All original **FR18-1–11 remain OPEN** for independent adjudication. Preserve credited
subrepairs, including the reviewed VECTOR mapping (no new mapping defect asserted).
Critical: **FR18-3** exact producer/output/source/field/version/locator/vector binding;
**FR18-1/2/8** live external source/child authority and complete physical-C/R7 proof.
High: **FR18-7/9** multilingual/scanned/mixed table quality and publication locator oracle;
**FR18-10** measured resource enforcement/lifecycle/reporting; **FR18-6/11** changed
offline/native GPU/fallback evidence and remaining focused/final acceptance packages.
This report changes none of the independent review's severities or conclusions.

**Next, without executing it:** independent review of this checkpoint. The smallest
recommended subsequent implementation decision is a separately bounded R7 repair/proof
slice: explicit fail-closed hard-link/namespace validation in the existing candidate,
then real approved external authority/reservation/input/supervisor/finalization through
physical C, only after the minimal FR18-3 dependency needed for a truthful publication
proof is resolved. Require rollback/unknown-C and relevant opposing mutation orders;
no default live enablement. This is a recommendation, not authorization or an assertion
that the current missing check makes Windows infeasible.

OCR needs a **separate explicit owner decision** whether to fund a further justified
configuration/candidate and fresh independently adjudicated groups; this session selects
none. If the owner instead wants any requirement amended, that requires a separate
documentation-only exact delta and explicit approval. Resource acceptance must follow
a viable supported workload; these measurements approve no new production limits.

**Implementation is STOPPED.** No automatic second candidate, remediation round,
takeover loop, P18-FINAL, scope reduction, finding closure, commit/push or live-file
activation follows this record. Full Phase 18 acceptance remains outstanding.
