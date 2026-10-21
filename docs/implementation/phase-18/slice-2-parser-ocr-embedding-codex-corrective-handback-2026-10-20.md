# P18-S2-PARSER-OCR-EMBEDDING - Codex corrective handback

**Date:** 2026-10-20. **Implementer:** Codex, owner-authorized bounded takeover, S2-01 through S2-08 only. **Exit: BLOCKED / OWNER_DECISION_REQUIRED.** This is not an independent review, finding closure, Slice 2 acceptance or readiness for owner commit. Stop after this handback.

**Unresolved:** S2-06 has no demonstrated four-script production OCR backend or fresh independent human-adjudicated held-out corpus. S2-07 has useful preflight/cooperative cancellation and native process termination evidence, but no integrated production cancellation/deadline proof through an in-flight native OCR/library call. S2-08 also hits the unchanged Java pipeline-identity allowlist after the required version bump; aligning Java is outside this Python-only authority. No threshold, required format or language is waived. These gaps prevent `IMPLEMENTED_AWAITING_INDEPENDENT_RE_REVIEW`.

## Authority, baseline and preserved work

Read ACTIVE, the complete [Slice 2 handoff](slice-2-parser-ocr-embedding-handoff.md), all eight findings and required proofs in the [independent review](reviews/2026-10-20-slice-2-parser-ocr-embedding-independent-review.md), current source/tests, D12/D14/D15, ADR-0023, acceptance and held-out oracle before implementation changes. Applied `codex-implementation-takeover`, Python/RAG/testing, Graphify navigation and bounded-command skills.

Actual entry: `main`, HEAD `c3b46351507803053850e3c2711602ee9c9ec419`; **26 dirty/untracked files**. Submitted Antigravity repairs, preparation and independent review files were preserved and evolved in place. Entry hashes: `logs/phase18/s2-takeover-entry-2026-10-20.json`. The [original Antigravity handback](slice-2-parser-ocr-embedding-handback-2026-10-20.md), dated independent review and evidence JSON remain byte-identical to entry. They are historical evidence, including unsupported completeness/GPU assertions; this new document supplies the correction.

Historical phase remediation ledger remains **2/2 exhausted**:

- Round 1: [2026-10-18 independent re-review 1](reviews/2026-10-18-phase-18-codex-final-re-review-1.md), `CHANGES_REQUESTED`; original FR18-1 through FR18-11 OPEN for its stated residuals, including not-verified integrated publication proof.
- Round 2: [2026-10-18 independent re-review 2](reviews/2026-10-18-phase-18-codex-final-re-review-2.md), `CHANGES_REQUESTED`; original FR18-1 through FR18-11 OPEN for its stated residuals. Initial implementation/review was not counted as a remediation cycle.
- The owner expressly authorized **one separate bounded corrective Slice 2 takeover** in this session. This is neither another Antigravity round nor a reset. Codex now authors implementation; any independent adjudication must occur in another session.

Phase 17 stays COMPLETE/FROZEN; Slice 1 COMPLETE/CLOSED under D14. No Java, Flyway, SQL privilege, PostgreSQL, dependency/lock/interpreter, HTTP DTO or public v2/v3 wire definition was changed. D14 is not a Slice 2 waiver. External indexing remains INACTIVE; live original DISABLED/DEFERRED; no selected-R/sourceVersion/G promotion, completed NO_CHANGE or Slice 3/4 implementation. FR18-3 and the aggregate PostgreSQL resource obligation remain OPEN. No commit, push, tag or PR.

## KEEP / CORRECT / MISSING / DEFER

| Classification | Components and disposition |
| --- | --- |
| KEEP | Existing exact TXT offsets; pinned multilingual ONNX/tokenizer; token-aware splitting; native PDF glyph/table geometry; source-only supplied child map; DOMAIN prepared projections; sparse column header mapping; useful hidden/property/comment extraction. |
| CORRECT | External Markdown policy, comment/code handling, child anchors/edges and chunk revision/role boundaries; DOCX inline/nested/anchor extraction; XLSX formulas/merges/groups; PDF OCR selection/order and measured ruled cells; validation, preflight/cancellation checks and vector rejection. |
| MISSING | Demonstrated vi/zh-Hant backend; fresh source-family/closure-disjoint independently human-labeled quality gate across required formats/scripts; integrated native-call cancellation/deadline/resource proof; Java admission alignment for the truthfully versioned pipeline. |
| DEFER | Java actual-producer verification/publication (FR18-3), SQL/publication authority, Slice 3, Slice 4, external activation, P18-FINAL acceptance. No second engine, model switch/download or architecture framework added. |

## Findings mapped to corrections and development proofs

Tests below are in [test_slice2_corrections.py](../../../rag-engine/tests/test_slice2_corrections.py), with existing parser/chunker/worker tests retained. **39 new deterministic cases** supplement the original 76; they are exposed development evidence, not independent held-out acceptance.

| Finding | Minimal correction and proof | Disposition |
| --- | --- | --- |
| S2-01 | `parse_markdown(..., whole_file=True)` is explicit in the external worker path. Preserve literal frontmatter, authorized private/custom metadata, comments and code; reject duplicate/malformed/alias-expanded YAML and malformed UTF-8. Field-filtered default and DOMAIN prepared-field denial remain. Actual worker test uses real chunking/CPU embedding with DB authority/staging isolated; invalid MD/TXT bytes produce failed empty output. Tilde fences and multiline inert comments do not resolve child embeds. | Candidate correction tested; independent closure pending. |
| S2-02 | One existing supplied-map resolver handles callout embeds, display aliases, heading/nested-heading/block anchors and nested edge chains. Original child identities, versions, original roles/locators retained. Conflicting same-revision payloads reject; different revisions and structural/visibility roles cannot blend into a falsely labeled chunk. Tests cover missing/cyclic/changed children, callout aliases, nested edges and parser-to-chunker old/new revisions. No child filesystem/network resolver added. | Candidate correction tested; independent closure pending. |
| S2-03 | Walk original inline runs including hyperlinks, then physical table cells and nested content in order. Retain original body/table/row/column/paragraph nesting paths, merge information, hidden-run metadata and actual comment-range start/end offsets. No broad exception suppression. Nonempty typed property/comment identities required; cell addresses validated; no DOCX page invented. Real anchored-comment/hyperlink/nested-table and null/empty identity regressions. | Candidate correction tested; independent closure pending. |
| S2-04 | XLSX loads formula text and cached values separately, executes neither, labels uncomputed/cached state, retains original per-cell coordinates, merged anchor/covered cells, full hidden column-group extents, row/sheet visibility and comments. Sparse iteration replaces dangerous rectangular expansion after archive/dimension/cell preflight. XLS retains valid BIFF8 values/merges/visibility; exact legacy formula text and complete NOTE/TXO comment graphs are unsupported and explicitly fail the entire file before xlrd can discard them. Tests include real XLS/XLSX, cached/uncached formula XML, merged/hidden group ancestry, sparse mapping, and declared unsupported legacy material. | Candidate correction tested; unsupported legacy material is a rejection, never completeness PASS. |
| S2-05 | Combine native paragraphs, native tables and OCR in original page order. Mask native-covered regions before OCR; reject ambiguous overlap and suppress matching overlapping duplicates. Extract closed ruled raster cells/merged spans from measured image boundaries, convert to source PDF coordinates; incomplete/ambiguous grids fail. Real CPU OCR positives cover scanned English table, merged table, mixed native/scanned table and plain scan; deterministic positioned OCR separately isolates overlap selection. Existing native transformed-glyph tests retained. | Candidate correction tested for these supported cases, not general layout/four-script quality acceptance. |
| S2-06 | Remove warning-only acceptance for known unsupported script fidelity: vi/zh-Hant now fail admission (`OCR_BACKEND_SCRIPT_UNVERIFIED`). Validate OCR confidence/geometry, at most one existing contrast retry, reject persistent low confidence. No relabeling or unapproved model provisioning. Existing Chinese/English engine is not a demonstrated four-script backend. | **BLOCKED / OWNER_DECISION_REQUIRED**, details below. |
| S2-07 | Shared small `limits.py` supplies input/inflation/XML-node/depth, spreadsheet dimension/cell/merge/SST, text/transclusion, PDF stream/page/raster and finite cooperative time bounds. Checks precede dangerous library workbook/merge/raster allocations. Actual HTTP cancel flag reaches real parsing checkpoints, with no staged output after cancellation. Native parsing workload is physically stopped by existing bounded runner, with owned process absence checked. Native calls and initialization themselves remain synchronous and depend on existing Java process supervision. | **PARTIAL / BLOCKED** for integrated in-flight native cancellation/deadline and complete representative resource proof. No new process/job engine or Java changes. |
| S2-08 | Reject wrong shape/dimension, zero and nonfinite vectors through `embed_batch`; validate normalized output. Bump parser/chunker/OCR pipeline revisions; freeze source hashes. Real same-model CPU inference and artifact verification run offline under a Python socket deny guard. CUDA UNAVAILABLE; synthetic OOM remains only a unit retry test. New measured resource and command receipts below replace unsupported assertions. | **BLOCKED for compatibility:** producer-local vector/CPU/evidence corrections pass, but changed pipeline versions fail the unchanged Java identity allowlist. No Java alignment is authorized. |

Touched production paths are existing `parsers/{text,markdown,docx,excel,pdf,models}.py`, `chunking/chunker.py`, `embeddings/model.py`, `ocr/engine.py`, `worker.py`, `main.py`, plus small shared `limits.py`. Worker cancellation callback is an optional internal Python argument; HTTP request/response schema is unchanged. Additions: regression file and `scripts/slice2_corrective_resources.py`. Submitted provisioning/dependency/probe script changes are preserved; no provisioner executed.

The previous Vietnamese warning test now asserts a **stronger admission failure**, because warning-only acceptance is the reported defect. Original field-filtered frontmatter/comment tests remain; the actual external worker explicitly uses the separate whole-file mode. No failing case was removed or skipped to manufacture PASS.

## Frozen configuration and quality blocker

Final source/test/fixture/config freeze: `logs/phase18/s2-takeover-source-freeze-final-2026-10-20.json`, **66 files**, no subsequent source mismatch. The first freeze is retained separately. A final inspection corrected image dimension checks in both key orders and moved the fallback ahead of rendering; 42 focused cases passed afterward. Resource/OCR observations above that final admission-only change retain their original provenance, not a new held-out claim. Parser `ppv-parser-v18.2-whole-file-structural-bounded`; chunker `structure-first-v18.2-revision-role`; OCR `rapidocr-1.4.4-en-hans-script-admission-v18.2`. This is configuration identity, **not** a newly certified held-out split. Final source hashes and receipt hashes are retained in the adjacent [corrective evidence inventory](slice-2-corrective-evidence-2026-10-20.json).

Historical `phase18-ocr-v2` manifest remains `c4fbbeb23c6571a76632dfc5b2a349b0ce8bc439ff5b4fa6169830bacca5e8be`. Two families, eight documents/pages, no tables/cells. Original failed **3/8** receipts are unchanged, including `logs/phase18-takeover/ocr-quality-1792463760517287700.json` and independent replay `ocr-quality-1792466431893837100.json`. Their human adjudication remains PENDING. Neither these exposed PDFs nor the already exposed `d18-pt-a` candidate become a fresh independent held-out set.

Current diagnostic `logs/phase18-takeover/ocr-quality-1792468389091806400.json`: **FAIL, 2/8 exact**, five emitted blocks, zero published files. This reduction reflects explicit rejection of unverified scripts, not improved OCR quality. Historical split names remain historical labels: development 2/4 exact; historically named held_out 0/4 exact.

| Script | Attempted documents/pages | Exact matches | Admission rejected | Remaining comparison |
| --- | --- | --- | --- | --- |
| en | 2/2 | 1 | 0 | Invoice spacing differs; independent material adjudication absent. |
| vi | 2/2 | 0 | 2 | No demonstrated capable backend; known historical diacritic/number loss is material. |
| zh-Hans | 2/2 | 1 | 0 | Block-joining whitespace differs; adjudication absent. |
| zh-Hant | 2/2 | 0 | 2 | No verified Traditional-script backend; historical Simplified substitution is material. |

No fresh held-out groups, human-approved labels, closure/near-duplicate audit, per-format table/cell/locator gold denominators or human material-error decisions were supplied in this bounded scope. The new generated regression fixtures are development fixtures only. **No independent quality PASS, invented human decision or weakened CER/locator standard is claimed.** Required owner action is a bounded decision on obtaining/evaluating a capable offline four-script backend and a fresh independently adjudicated corpus under the existing oracle. Model download/dependency changes are outside this authorization; a warning, all-file rejection or threshold waiver cannot satisfy S2-06.

## Runtime, resource and cancellation evidence

Windows host: i5-9600K, physical RAM **17,112,092,672 bytes**, RTX 2070 **8192 MiB**. Locked worker Python **3.14.4**; inventory **66 packages match uv.lock**. ONNX lists AzureExecutionProvider and CPUExecutionProvider; **CUDA inference UNAVAILABLE**, not PASS. No dependency/model upgrade or download.

Pinned model SHA-256 `98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f` (118,453,870 bytes); tokenizer SHA-256 `2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8` (9,081,518 bytes). Constructors verify the actual local files. Fresh-process import regression prevents the discovered OCR/helper import-order cycle from recurring.

`s2-takeover-resources-2-2026-10-20.log`: **four development documents, 707 elements/chunks/vectors**, real CPU inference, **12.6473 s wall / 53.8906 s process CPU**. Process lifetime peak working set **673,763,328 bytes**; host headroom at finish **2,090,831,872 bytes**; maximum sampled **device-wide** VRAM **2,534,649,856 bytes**. VRAM is not pipeline allocation or GPU inference evidence; host headroom is not host peak usage.

Dedicated TEMP/TMP/TMPDIR sampled every **20 ms**, **379 samples**: observed overall scratch peak **70,810 bytes**, including XLSX fixture generation; **pipeline-only observed scratch peak 0**, final scratch **0**, directory removed. These are actual sampled directory sizes, not kernel accounting or a proof no shorter transient write occurred. First resource receipt (511 chunks, before stricter structural boundaries) is preserved and superseded by this 707-vector workload. It preceded the last PDF admission-only correction; the unchanged positive extraction path is covered again by final tests.

Real HTTP worker cancellation test pauses at the fifteenth actual text-parser checkpoint, invokes the existing cancel endpoint, then observes CANCELLED with **no stage call**. Actual parsing deadline and cumulative transclusion/inflation/cell/raster negatives are tested. The 100-page real PDF/OCR termination diagnostic enters the native recognition pass, then the **15-second watchdog intentionally exits 124/TIMEOUT** and requests Windows owned-tree termination. Python launcher PID 20520 and actual OCR PID 28736 both subsequently fail `OpenProcess` with error 87 (absent); survivor receipt retained. This negative receipt is a successful observation of termination, **not** a test-suite PASS or proof of Java finalization/quiescence.

Production uses cooperative checkpoints with a **60-second per-root scope**, allowed maximum 120 seconds in controlled scopes; native calls can exceed a checkpoint interval. No new hard heap/VRAM quota, application temp-disk enforcement platform or interruptible native runtime is claimed. Conservative admission caps are 10 MiB input, 32 MiB expanded package/PDF streams, 8 MiB member, 200x ZIP ratio, 4096 members/streams, 250,000 XML elements/depth 128, 100,000 spreadsheet cells/shared strings plus bounded dimensions/merge area, 1,000,000 text/transclusion characters, 100 PDF pages and 16,000,000 raster pixels. These are conservative rejection bounds, not measured maximum-safe workload certification. Complex/ambiguous PDF filter chains/streams, unverified grids and unsupported legacy material fail closed. Representative maximum-bound and integrated native cancellation/resource evidence remains incomplete (**S2-07**); no frozen Java lifecycle guarantee is changed or claimed newly proved.

## Exact verification commands and receipts

All commands run from repository root. Let `P = rag-engine/.venv/Scripts/python.exe`, `R = scripts/run-bounded-command.py`, `L = logs/phase18/`. Each row is the exact argv expansion of the template below; the evidence inventory also stores literal wrapper/child argv arrays and verifies their hashes against the receipts:

```powershell
python R --profile normal --timeout-seconds LIMIT --receipt L/RECEIPT -- P ARGUMENTS
```

Actual invocations use the literal paths represented by P/R/L; stdout/stderr are retained in matching `.log` files where applicable. These variables are notation, not an alternative runner. All counts below have **zero skipped** cases unless explicitly stated.

| ARGUMENTS | LIMIT | RECEIPT | Actual result |
| --- | --- | --- | --- |
| `-m pytest rag-engine/tests/test_slice2_corrections.py rag-engine/tests/test_pdf_geometry.py -q` | 600 | `s2-takeover-pdf-bounds-final-2026-10-20.json` | **42 passed**, 0 failures/errors/skips, 47.53 s; after final PDF guard correction. |
| `-m pytest rag-engine/tests/test_slice2_corrections.py rag-engine/tests/test_parsers.py rag-engine/tests/test_pdf_geometry.py rag-engine/tests/test_chunker.py -q` | 600 | `s2-takeover-focused-post-admission-2026-10-20.json` | **62 passed**, 0 failures/errors/skips, 37.28 s; before final PDF admission-only guard. |
| `-m pytest rag-engine/tests -q --junitxml=logs/phase18/s2-takeover-python-module-pdf-final-2026-10-20.xml` | 1200 | `s2-takeover-python-module-pdf-final-2026-10-20.json` | **115 passed**, 0 failures/errors/skips, 50.44 s pytest time. |
| `-m ruff check rag-engine` | 180 | `s2-takeover-ruff-final-2-2026-10-20.json` | PASS, exit 0. |
| `-m ruff format --check rag-engine` | 180 | `s2-takeover-format-final-2-2026-10-20.json` | PASS, 48 files formatted, exit 0. |
| `-m mypy rag-engine/src/ppv_rag` | 180 | `s2-takeover-mypy-final-3-2026-10-20.json` | PASS, 22 source files, exit 0. |
| `rag-engine/scripts/dependency_inventory.py --output logs/phase18/s2-takeover-dependency-inventory-2026-10-20.json` | 180 | `s2-takeover-dependencies-2026-10-20.json` | PASS, 66 packages match lock, exit 0. |
| `rag-engine/scripts/probe_phase18_ocr.py` | 180 | `s2-takeover-ocr-2-2026-10-20.json` | **FAIL 2/8**, exit 1; exposed diagnostic, no held-out acceptance. |
| `rag-engine/scripts/slice2_corrective_resources.py` | 180 | `s2-takeover-resources-2-2026-10-20.json` | PASS, real 707 CPU vectors and resource observations above, exit 0. |
| `rag-engine/scripts/slice2_corrective_resources.py --termination-workload` | 15 | `s2-takeover-native-termination-2026-10-20.json` | Intentional **TIMEOUT**, exit 124; real native recognition entered, owned processes absent afterward. |

The one required full clean gate uses this exact command (2100 s full-suite cap):

```powershell
python scripts/run-bounded-command.py --profile full-suite --timeout-seconds 2100 --receipt logs/phase18/s2-takeover-full-java-2026-10-20.json -- mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED clean verify
```

**Full clean gate: FAILED**, exit **1**, watchdog **885.297 s**, Maven reported **14:43 min**. Surefire console: **1,433 tests, 1,430 passing, 1 failure, 2 errors, 0 skips**. Build stopped at test; later verify/package stages did not execute. The 37 application architecture cases, one OpenAPI route-inventory case, nine native v2 worker cases and two pgvector compatibility cases passed within this run.

Exact non-green cases:

- `RagWindowsNativePrerequisiteTest.attemptsAliasCreationAndRemovalDuringProtectedLifetime`: unsupported alias creation assertion, expected false but true. Existing historical native test remains unchanged; D14 only excludes it from Slice 1 acceptance.
- `RagNativeDocumentBatchIntegrationTest.realNativeEightSuccessTwoFailurePublishesAtomicallyAndReconcilesLostAck`: **`IllegalStateException: Unverified document pipeline identity`**. This is the current failure, not the earlier `Invalid worker batch result provenance` message. `RagDocumentFinalizationGate.java:30-36,358-363` still allowlists parser/chunker v18.1 and original OCR version; Python now truthfully identifies the changed v18.2 extraction. Updating that Java admission policy requires explicit escalation under this scope. Original producer provenance/FR18-3 remains OPEN behind this earlier gate; no end-to-end v3 compatibility PASS is claimed.
- `RagWorkerBoundaryIntegrationTest.actualSpringRuntimeRestartDeniesRestoredConsentAndStaleWorkerGrants`: **`PSQLException: FATAL: remaining connection slots are reserved for roles with the SUPERUSER attribute`**. No root-cause repair or bounded aggregate-resource investigation is claimed; that retained liability remains OPEN.

Receipt/log: `logs/phase18/s2-takeover-full-java-2026-10-20.{json,log}`; extracted class/case counts and exact messages: `s2-takeover-java-summary-2026-10-20.json`. Console totals are authoritative; summing nested Surefire XML suite roots is not substituted for the console count. The original Slice 1 failed gate and independent reviews remain unchanged. Neither D14 nor focused Python success waives a non-green full gate. P18-FINAL quality/publication/resource acceptance is not implied by running this command.

The Java command began against the first Python freeze and continued while the final PDF preallocation-only guard was corrected. No Java/SQL/wire code changed; the final Python suite separately verifies that correction. The failed Java receipt is not represented as an exact-final-source clean PASS. No unchanged failed aggregate rerun was used to seek lucky green.

Development failures were retained, not hidden: focused iteration 1 **50 passed / 1 failed** (nested heading/child scope, corrected); iteration 2 **44 passed / 7 failed** (new PDF preflight import, corrected); iteration 3 **51 passed**; iteration 4 **54 passed / 1 failed** (test OCR import, corrected); pre-final focused **60 passed**; first whole Python module **106 passed**; next full Python module **113 passed** before the final two image-budget cases; final **115 passed** afterward. Static iteration receipts retain the temporary syntax/import/type/format failures and their repairs. The first standalone OCR diagnostic failed on an import-order cycle; shared limits were moved out of parser package initialization, a fresh-process regression was added, and the second diagnostic actually ran and FAILED 2/8. No unchanged failed command was retried to seek a lucky result. All `s2-takeover-*` receipts/logs and argv hashes remain under `logs/phase18/`; the adjacent evidence inventory records hashes. `git diff --check` is recorded separately after documentation completion.

## Remaining disposition

**S2-06: BLOCKED / OWNER_DECISION_REQUIRED.** Missing capable verified vi/zh-Hant production OCR, credible fresh source-family/closure-disjoint all-format labels/denominators, independent human adjudication and configuration-frozen independent evaluation. Historical exposed failures remain failures. No acceptance-standard change requested.

**S2-07: BLOCKED / partial proof.** Preallocation and cooperative cancel checks, native watchdog death and sampled small-workload disk/RAM evidence are useful; they do not establish production bounded interruption during native library execution or all required representative upper-bound resource cases. Existing Java supervision stays frozen; no new Java boundary work is authorized here.

**S2-08: BLOCKED for Java pipeline identity compatibility.** Producer-local dimensions/finiteness/normalization, truthful CPU/GPU disposition and receipts are supplied. The required changed extraction identity is rejected by the existing Java v18.1 allowlist. A separately authorized bounded Java alignment or explicit disposition is needed; no version relabeling or admission weakening was used to manufacture PASS. This does not authorize Slice 3 or close FR18-3.

`git diff --check` **PASS, exit 0**; final receipt is `logs/phase18/s2-takeover-diff-check-handback-final-2026-10-20.json`, with matching log. Git's CRLF normalization notices are not test failures or a warning-free compiler claim. Final HEAD is unchanged at `c3b46351507803053850e3c2711602ee9c9ec419`; final source freeze matches all 66 files, historical handback/review hashes still match entry, and no backend/SQL/lock/wire definition diff exists.

Other corrections require separate independent adjudication; none are self-closed. Stop this finite takeover after handback. Do not launch another remediation loop, commit/push, accept Slice 2, start Slice 3/4 or enable external indexing.
