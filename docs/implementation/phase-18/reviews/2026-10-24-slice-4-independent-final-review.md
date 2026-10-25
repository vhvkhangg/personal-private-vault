# Phase 18 Slice 4 — independent final review

**2026-10-24 — CHANGES_REQUESTED. Slice 4 and P18-FINAL are NOT ACCEPTED.**

There are reproducible NO_CHANGE and reporting defects, incomplete cleanup, and unmet mandatory lifecycle/resource evidence. The failed Java gate cannot be accepted. The NTFS assertion tests a superseded snapshot obligation; correcting its applicability would address that particular failure, not the independent defects below.

This is independent review only. No production/test/dependency/migration correction, format restoration, staging, commit, push, tag, PR or activation occurred. ACTIVE records this review disposition; implementation handback, prior decisions/reviews and failed artifacts remain preserved. Historical Antigravity 2/2 is not reset and no corrective cycle is authorized.

## Baseline, authority and evidence

Reviewed `main` / HEAD **`ebe125f7529124b62761e8d01c692cbbd28e4aa7`**, the published D25 documentation successor to accepted Slice 3 source **`e2e00534fb35534a298e8fb15d174f18cb63b5a0`**, plus the actual dirty implementation. ACTIVE was `IMPLEMENTED_AWAITING_CODEX_REVIEW`, a valid implementation-review gate. Read/reconciled AGENTS, ACTIVE, D25/D24 and historical D14/D17/D20/D22/D23, Slice 3 closeout/final independent review, S4 executable handoff/execution plan/acceptance matrix/handback, ADR-0021–24 and applicable acceptance/final-gate contracts. ADR-0016 and frozen module/Phase 17 rules remain applicable. D25 authorizes A→B→C→D, not acceptance, publication or extra remediation.

Applied `codex-final-review`, its 29-dimension ledger, Java/Spring, SOLID/reuse/pattern, modular/persistence/backend-testing, Python/local-RAG, migration, REST and bounded-execution skills. Graphify supplied navigation, not evidence. Actual changed packages, source consumers, SQL and tests were inspected recursively.

Fresh outputs use only **`logs/phase18/s4-independent-20261024/`** (`O` below). Entry preservation covers **2,502 nonignored files**, **70,228 existing ignored evidence assets**, and a pre-test archive of generated Surefire reports. The [independent machine evidence](2026-10-24-slice-4-independent-evidence.json) contains exact commands, exits/counts, source/receipt/log hashes, observations and qualifications. The [candidate and cleanup inventory](2026-10-24-slice-4-independent-inventory.json) enumerates all **67 entry dirty paths**, including **17 deletions**, their HEAD/working hashes and remaining trees. Ignored raw evidence must be retained separately.

## Stage A findings

### S4-F01 — High — standalone equality accepts an ineligible retained generation

**Contracts:** S4-01/02, ADR-0022 NO_CHANGE predicates 4–7 and C_N; executable handoff A.1–3.

`RagSourceService.capturePendingRevision:144–223`, especially `:177–209`, checks root classification/reset/selected-R/version and whether an **active** generation's pipeline is current. It does not establish that the retained G is currently eligible for the exact authorized child vector/topology. The existing `captures` map is in-memory only; this path creates no durable nonpublication operation/history mapping for unknown C_N/restart, despite that explicit handoff requirement. `snapshotPlan` guards root/affected-source metadata; it does not supply the missing child/G equality proof.

**Independent actual API/native/PG counterexample:** publish an approved Markdown parent embedding its approved child; capture/approve/publish a new immutable child revision. The parent's G now returns **OUTDATED**. Capture the parent's unchanged original bytes with current expectations: **accepted**, same selected R and sourceVersion, no pending R, **zero new jobs/G/runs**, and the retained G remains **OUTDATED**. This is not a same-user attack or administrative tamper injection. The returned equal-byte no-op should instead require authorized vector resolution/reindex; it does not meet NO_CHANGE.

**Required acceptance correction, not authorization:** prepare and guard the complete existing authority/G/child proof through C_N, centralize its consistent use, and implement the approved durable existing-row nonpublication mapping and unknown-outcome reconciliation. Keep hashing/process work outside SQL guard windows. Do not repair this by launching a worker, fabricating a zero-publication COMPLETED job or creating an equal-byte R.

| ADR-0022 predicate | Independent assessment |
| --- | --- |
| 1. Explicit intent and exact expectations | Root path/version expectations present; decisive ordinary/stale controls pass. |
| 2. Coherent acquired bytes and SHA-256/length | Existing bounded capture/change detection/equality preserved. |
| 3. Independently verified selected ORIGINAL_BYTES | Selected descriptor/integrity checks present; normal positive controls pass. |
| 4. Current authority/history/retention/no conflicting operation | Root checks exist; durable nonpublication history/exclusion and restore uncertainty not established. |
| 5. Exact authorized child topology and selected S/R vector | **FAIL:** actual changed-child counterexample. |
| 6. Legitimate CURRENT retained G and exact pipeline/producer state | **FAIL:** active/pipeline-current is accepted while G is OUTDATED; mixed path also accepts REVOKED G. |
| 7. Source-first guarded finish and unknown-outcome safety | **Incomplete:** no durable C_N operation mapping or C_N loss/restart evidence; missing full child/G validation cannot be serialized into correctness. |

### S4-F02 — High — caller omission becomes false NO_CHANGE in a mixed publication

**Contracts:** S4-01/02/10/11, full immutable roster/Java-owned unchanged proof, ADR-0022 mixed C_P.

`RagDocumentBatchService:242–266` accepts a caller-selected subset; `RagWorkerGrantService:79–136` derives `worker_plan` from that subset and writes `runs.metrics`. The finalizer derives replay inputs from the **actual candidate keys** and, at `:365–378`, treats every omitted admitted root as NO_CHANGE. `validateNoChangeForUnexecutedRoot:612–638` checks active G/pipeline and some root fields, not current G eligibility, selected-versus-approved pending R, or a Java-proven captured-equality disposition. V23's roster subset check cannot make that missing business proof valid.

**Independent actual API/native/PG counterexample:** publish A, then capture and approve different A bytes as pending R2. Its retained old G qualifies **REVOKED**. Admit A and new B; execute only B. Finalization returns **COMPLETED / 1 new G / 0 failures** and commits outcomes **A=NO_CHANGE, B=SUCCESS**. A still has its different pending R2, retains old selected R1, and its reported snapshot/G state is **REVOKED**. The stored worker plan is `["b"]`. No privileged fault or forged DTO is needed. B's content is genuinely authorized and verified; the defect is the false unchanged disposition and acceptance of a batch whose mandatory unchanged proof is invalid.

The pure all-unchanged path is also unproved: `executeDocumentBatch` calls `supervisor.begin` before rejecting an empty task list. That cannot substantiate the canonical receipt's claim that an all-unchanged indexing batch safely processes an empty worker plan. The handoff requires nonpublication operation handling without an indexing lease/worker.

**Required acceptance correction:** bind the full immutable roster, intended action and Java-prepared unchanged proof to the smaller worker plan; reject arbitrary omission and revalidate every unchanged root under final guards. An invalid unchanged/common-trust proof must roll back the mixed publication. Do not weaken actual-result seals or allow the worker to assert NO_CHANGE.

## Stage B findings

### S4-F03 — Medium, blocking — qualification and complete file outcomes do not reach the user

**Contracts:** S4-06/11, executable B.4/5, ADR-0016 transport consistency.

`RagFileOutcomeView` gained six qualification/disposition/revision fields, but production `RagWebMapper:89–101` and `internal/web/dto/FileOutcomeDto` still serialize only the old nine fields. **freshnessBasis, snapshotIndexState, originalFreshness, publishedNewGeneration, selectedRevisionId and pendingRevisionId disappear at the HTTP boundary.** The deleted-original positive test examines the internal service record, not this mapper. An independent mapper run using the recorded actual native mixed report confirms those fields are absent. This is native-report-to-production-mapper evidence plus controller/source inspection, not a claimed fresh authenticated HTTP request.

The report service iterates only committed `file_outcomes` (`:73`), not the full admitted roster. A fresh staged native job cancelled through the actual API has **one admitted root, CANCELLED, zero G**, but `getDocumentBatchReport` returns **outcomes=[]**. Common abort/unknown states therefore lack required per-file reporting. Failed-file retained-G lookup can affect state while the returned candidate ID stays null; its retained identity/disposition needs explicit truthful treatment. State and `snapshotState` also call the expensive currentness path twice per root, duplicating store/authority work and permitting inconsistent labels across intervening mutations.

**Required acceptance correction:** carry the approved typed fields through existing module-local DTOs/mappers/OpenAPI; derive complete outcomes from the immutable roster for cancellation/failure/unknown/retention; qualify effective G consistently without inventing publication counts or measurements. No new root envelope/module/route is required.

### S4-F05 — High — required lifecycle evidence and the submitted PASS mapping are incomplete

**Contracts:** S4-01/02/04/07–12/18; executable test/evidence protocol.

The actual new S4 classes contain **10 tests: 3 NoChange, 2 Reindex, 5 Lifecycle**. The handback's named S4-08 restore and S4-09 in-flight cancellation tests do not exist. Its S4-10 test is actually mixed execution plus repeated finalization, not a wire cut. Its claimed seven parameterized NO_CHANGE negatives are a single method with six controls and no child replacement/grant/topology case. The “concurrent revoke” test is sequential single-source revoke; the storage test checks happy-path capture temp absence. Counts do not prove missing behavior.

Fresh inherited Slice 3 wire-loss, staging cancellation, ordered intent, SQL security and typed-closure tests provide useful **C_P/current-path** evidence. They do not prove a new durable C_N lifecycle that has no recorded operation mapping. Actual C_N acknowledgement loss/rollback/new JVM, both restore directions with reapproval/stale child/edge/grant fencing, and the required new NO_CHANGE/revoke orders are not supplied. Do not manufacture restore uncertainty or a new forensic framework: implement/verify only the already approved ordinary recovery contract.

The six `P18-*.json` files are descriptive summaries, not self-proving executable receipts. Concrete inconsistencies include:

- `P18-PUBLISH-PG` and handback §7 claim new `plan_outcome`/`worker_plan` columns and `rag.document_job_roots`. **V23 adds no columns** and uses `rag.runs.metrics` plus existing `rag.batch_roots`.
- `P18-OFFLINE-MODEL` names **BAAI/bge-small-en-v1.5** while the actual pinned producer/model bytes are **sentence-transformers/paraphrase-multilingual-MiniLM-L12-v2**. The production model is not wrong; the receipt description is.
- The original S4 receipt directory supplies wrapper JSON files but no matching retained stdout/stderr logs or final-source hash manifest. Current entry-generated XML totals **1,536/1/0/0**, whereas the handback reports **1,549/1/0/0**. The 13-test discrepancy is unresolved; do not invent its cause or substitute the XML sum as an independently reconstructed historical full-run total.
- S4-17 is labelled PASS despite its own `full-java.json` recording **exit 1 / COMMAND_FAILED**.

**Required acceptance correction:** provide an additive truthful executed matrix with exact actual methods, source/fixture/model hashes, logs and original counts/limits; preserve the original submissions. Supply the missing current-scope proof rather than retroactively assigning old passing tests to untested new behavior. Do not change the actual model to match an incorrect receipt.

## Stage C finding and exact cleanup inventory

### S4-F04 — Medium, blocking — cleaned lock is not the selected installed runtime

The source removal and lock reduction are substantial: **73→54 locked packages**, **19 removed**, no added packages or retained-package version changes. Actual external intake is `.md` only; worker dispatch now accepts MD and internal DOMAIN, with no TXT/`.markdown`/Office/PDF/OCR fallback. Required DOMAIN `parse_text` remains used by `worker.py` and `document_source.py`. The accepted **`rapidocr-1.4.4-en-hans-script-admission-v18.2`** tuple remains inert compatibility metadata, as required; the handback's alternative “ocr-inert-v18.4” description is not the actual tuple and must not trigger an identity change.

However the **selected `.venv-d17` still has 67 distributions and 15 not in the cleaned lock**: `cffi`, `charset-normalizer`, `cryptography`, `et-xmlfile`, `lxml`, `openpyxl`, `pdfminer.six`, `pdfplumber`, `pillow`, `pycparser`, `pypdf`, `pypdfium2`, `python-docx`, `xlrd`, `xlwt`. `uv pip check` verifies dependency compatibility, not removal or exact lock synchronization. Both that check and offline lock validation pass while these packages remain. The cleanup cannot be declared complete. No runtime package was removed/installed/upgraded by this review.

The executable `measure_d18_pt_a.py:85` still launches the deleted OCR checkpoint and writes fixed historical evidence paths. Do not run it or restore the unwanted checkpoint. Retain required imported observer primitives while removing/retiring the obsolete OCR execution path. Residual Office/PDF/XLS locator-only branches in shared `parsers/models.py` need a documented required consumer/compatibility justification or approved cleanup; they do not by themselves prove a reachable external parser. `run_d18_pt_a_native.py` is a live/R7 diagnostic, distinct from an OCR parser; its historical/offline applicability should be explicit.

The review reconstructed this exact deletion set from HEAD; the supplied handback does not supply the required before/deleted/edited/retained-consumer manifest:

| Group | Actual deleted paths |
| --- | --- |
| Adapters/backend | `rag-engine/src/ppv_rag/parsers/docx.py`, `excel.py`, `pdf.py`; `rag-engine/src/ppv_rag/ocr/engine.py` |
| Sole-purpose scripts | `rag-engine/scripts/d18_pt_a_ocr_checkpoint.py`, `freeze_phase18_ocr_fixtures.py`, `probe_phase18_ocr.py`, `provision_phase18_latin_ocr.py`, `slice2_ocr_checkpoint.py`, `slice2_paddleocr_checkpoint.py`, `slice2_tesseract_vietocr_checkpoint.py` |
| Python tests | `rag-engine/tests/test_optional_ocr_dependency.py`, `test_pdf_geometry.py`, `test_pdf_scanned_surrounding_text.py` |
| Java test/fixture | `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSlice2NativeOcrCancellationIntegrationTest.java`; `backend/src/test/resources/rag/native-ocr-worker/src/ppv_rag/__init__.py`, `main.py` |

Total **17 deletions**. The inventory JSON records exact full paths and HEAD hashes, edited paths and remaining consumers/trees. D17 Markdown/gold/fixtures remain; the retained `test_slice2_checkpoint_package` verifies historical manifest/source integrity rather than executing excluded parsers. Historical mixed datasets may be retained as immutable evidence where hash tests/ledgers require them; `.txt` notes and native guard fixtures are not external TXT intake. Ignored owner/model caches are not authorized data-purge targets.

**Required acceptance correction:** synchronize/prune the exact selected runtime offline against the cleaned lock without upgrades; complete the authorized consumer-based cleanup/manifest and clean import/format-denial verification. Do not restore excluded implementations for convenience.

## Stage D resources, PostgreSQL and final gate

### S4-F06 — High — final integration/GPU resource evidence is not established

The reported **580 MB** is described in the canonical summary as a *single process lifetime peak working set*, not simultaneous producer/verifier/Java tree or host RAM. **1.80 GB** is device-wide NVML usage, not worker-attributable VRAM. “Process-attributable VRAM = 0 bytes” is unsupported by a process-attribution measurement; it must be UNKNOWN/unmeasured, not inferred from force-CPU execution.

`logs/p18-resources-measurement.json` is only a watchdog receipt: **3.057s, exit 0**. It contains no 608,174,080-byte/1,932,787,712-byte measurements, no claimed 13.84s/707-element profile, no source hash or producer/verifier lifetime observations. The current `slice2_corrective_resources.py` constructs **two repeated English-text documents**, runs parser/chunker/CPU embeddings inside a **120-second development scope**, and labels itself development-only. It does not exercise native Java→producer→verifier→PG, approved child/mixed/reindex/near-bound paths, or the claimed multilingual/table/embed workload. Its actual stdout was not supplied at the cited path. The numbers may not be treated as independently verified current evidence.

The actual runtime reports Azure/CPU providers and **CUDA UNAVAILABLE**. CPU output/model identity is genuinely verified by native tests; starting on CPU is not actual native GPU→same-model CPU fallback evidence. Unlike bounded Slice 3's accepted limitation, the S4 execution plan **explicitly leaves final GPU evidence unmet** (S4-15/AC-05 and resource protocol); no D24/D25 waiver exists. Earlier 175-identity S3 resources remain historical, not a new final cleaned-tree resource profile.

**Required acceptance evidence:** representative final native cold/warm/near-admitted-bound, child/mixed/reindex, cancellation/error workloads with exact source/model/workload binding, OS PID+creation identities, separate simultaneous tree/per-process/JVM/host RAM, CPU/wall, temp/expansion/cleanup, separate device/process VRAM and unknown sampling gaps. No universal maximum, zero-global-OS-write or unavailable-CUDA PASS claim. Owner must separately route fulfillment of the existing GPU requirement or explicitly amend its applicability; this review authorizes neither installation nor a waiver.

### PostgreSQL capacity correction: supported, with bounded attribution

The Hikari edits are confined to test-owned `AbstractPostgresIntegrationTest` and `AbstractRagIntegrationTest.RuntimeDatasource`: max **5**, minimum idle **1**, idle timeout **10,000ms**. No production pool settings, Maven parallelism/discovery/exclusions, `max_connections` or assertions were weakened.

A fresh test-owned PostgreSQL experiment independently establishes the capacity mechanism:

| Configuration / 12 pools | Observed result |
| --- | --- |
| Historical Hikari defaults, max/min-idle 10 | **95 owned connections**, additional restricted runtime connection rejected with **SQLSTATE 53300**. Server max_connections=100, superuser_reserved=3, reserved=0. |
| Corrected max=5/min-idle=1 | **12 owned connections**, additional runtime connection accepted. |
| Close each set | **0 owned connections remaining** for both configurations. |

This demonstrates idle pool multiplication can exhaust capacity without a connection leak and that the change reduces that pressure. Fresh **25/25 WorkerBoundary** tests and **4/4 privilege** tests pass with these settings. The report's “32 contexts” is a cache bound, not an observed exact historical context count. The probe does not reconstruct the historic sequence, rule out every unrelated leak or certify arbitrary parallel saturation: 32 simultaneously full pools can still exceed 100. Actual current retained aggregate output has no recorded SQL errors, but the full gate remains failed. S4-16's complete aggregate acceptance therefore remains qualified by S4-17 and historical evidence limits.

### Five-command P18-FINAL disposition

| Original operation | Independent disposition |
| --- | --- |
| Ruff check | Original wrapper exit 0; fresh final-tree check also PASS. |
| Ruff format check | Original wrapper exit 0; fresh check PASS, **66 files** formatted. |
| Mypy | Original wrapper exit 0; fresh empty-cache check PASS, **22 source files**. |
| Full pytest | Original wrapper exit 0, reported 231; fresh **231 passed**, no failures/skips. |
| Full Maven `clean verify` | **FAILED.** Original `full-java.json`: full-suite profile, 2100s bound, **1613.776s / exit 1 / COMMAND_FAILED**. Reported 1549/1/0/0; available entry XML 1536/1/0/0, unresolved discrepancy. Known failing assertion independently reproduced below. Later clean-verification stages cannot be assumed passed after Surefire failure. |

**P18-FINAL is FAILED/NOT ACCEPTED, not “1548 tests passed.”** No unchanged 27-minute aggregate was rerun merely to obtain green. D14's narrow Slice 1 exception is not a Phase 18 final waiver.

### S4-F07 — Medium, blocking — superseded NTFS assertion still fails the required command

Fresh bounded execution of `RagWindowsNativePrerequisiteTest.attemptsAliasCreationAndRemovalDuringProtectedLifetime` reports **created=true, removed=true**, then fails at line **167** because it requires alias creation to be blocked throughout the held lifetime. This is real Windows behavior, not a parser/embedding or new snapshot-copy failure.

| Question | Independent determination |
| --- | --- |
| **A — superseded requirement?** | **Yes for active immutable snapshots.** ADR-0023 scope/priority and explicit out-of-scope list remove malicious hard-link/mapping/writer lifetime proof for G1/G3. Practical path/copy/change/hash/no-clobber/process/SQL/recovery protections remain mandatory. |
| **B — mandatory now?** | The hostile-alias guarantee is **not a functional snapshot acceptance requirement**. The unchanged current default Maven discovery nevertheless executes this test, and P18-FINAL requires genuine exit zero. The failed command cannot be ignored or declared passed because its underlying guarantee is superseded. |
| **C — defect or prerequisite?** | Execution of a historically superseded stronger native prerequisite; not evidence that current ADR-0023 snapshot processing is defective. Live originals remain disabled. Git history places this test in pre-D12 Phase 18 commit `afeeb82`; it is **absent from frozen Phase 17 commit `829d294...`**. No frozen Phase 17 prerequisite is silently removed here. |
| **D — narrowest resolution?** | A separately authorized **exact test-applicability correction** retaining the original assertion/failure as historical or opt-in future-live diagnostic, with explicit snapshot/nonapplicable classification and retained current application-security tests. Not flipping the assertion, deleting because it fails, blanket exclusions or repairing OS anti-tamper behavior that is no longer required. Review performs no such correction. |
| **E — new Owner contract decision?** | Applying the already accepted ADR-0023 supersession does **not itself require a new security waiver/ADR**. An Owner-routed, independently reviewed narrow applicability correction can legitimately allow the same five commands to succeed, with truthful applicable/nonapplicable/skipped counts. Accepting an unchanged failed gate instead would require a **new explicit final-gate exception**; D14 supplies none. This review grants neither an implementation checkpoint nor a final-gate waiver. |

This correction alone cannot resolve F01–F06 or unmet GPU evidence. Ordinary snapshot/read-only native path checks must not be removed under an overbroad reading of supersession.

### S4-N01 — Low — working diff hygiene fails

Independent `git diff --check` exits **2** for a new blank line at EOF in `RagGenerationState.java:152`. Existing CRLF conversion advisories are separate. No whitespace or fixture bytes were normalized by the reviewer. This is a minor cleanup item, not the reason the lifecycle contract fails.

## Full S4-01–18 acceptance matrix

PARTIAL/NOT_VERIFIED are not PASS. Existing accepted Slices 1–3 and FR18-3 remain CLOSED at their published scope; current S4 changes must satisfy their newly enabled plan/lifecycle behavior rather than inherit a blanket acceptance stamp.

| Row | Independent result | Evidence / remaining requirement |
| --- | --- | --- |
| **S4-01** | **FAIL** | Normal equal bytes preserve R/G/version and launch no indexing job; child/G eligibility fails and durable C_N operation/unknown finish mapping is missing (F01). |
| **S4-02** | **FAIL** | Some root expectation/pipeline/consent/path negatives pass; real changed child and omitted pending root falsely qualify unchanged (F01/F02). |
| **S4-03** | **VERIFIED bounded native case** | Real same-R reindex with deleted original creates a new trusted G, keeps R/sourceVersion and supersedes prior G at publication. |
| **S4-04** | **PARTIAL** | Native tampered-result denial retains eligible prior G; complete reindex-specific child/revoke/failure/physical-boundary mapping not supplied. Existing S3 guards/negative proof credited where unchanged. |
| **S4-05** | **PARTIAL** | New selected-R integrity and pipeline checks plus source revoke work for exercised cases; complete restored-history/current-authority variants not proven. Missing G validity in NO_CHANGE remains F01/F02. |
| **S4-06** | **FAIL at user-facing boundary** | Internal deleted-original qualification passes; actual web mapper drops the required fields (F03). |
| **S4-07** | **PARTIAL** | Sequential revoke/anti-reapproval passes; complete NO_CHANGE/revoke orders, transitive purge/restart and sibling proof for final lifecycle is not mapped/executed as claimed (F05). |
| **S4-08** | **NOT_VERIFIED** | The cited restore test does not exist; both restore directions and explicit reapproval/stale dependency evidence are missing. No new claim of automatic orphan adoption is made without a reproduction. |
| **S4-09** | **PARTIAL** | Inherited actual staging cancellation and three durable-intent orderings freshly pass; new nonpublication recovery/resource ownership scenarios remain unverified. |
| **S4-10** | **PARTIAL** | Real committed/precommit wire-cut/DB-outage C_P cases freshly pass. S4's named test proves idempotence only; durable unknown C_N/restart proof is absent. |
| **S4-11** | **FAIL** | Valid mixed positive passes, but pending/REVOKED root becomes NO_CHANGE and cancellation loses admitted outcome; full all-unchanged and HTTP/retention reporting incomplete (F02/F03). |
| **S4-12** | **PARTIAL** | Happy capture-temp cleanup and retained no-clobber/storage mechanisms credited; complete current C_N/storage/retry/crash-fault evidence not supplied. |
| **S4-13** | **FAIL / substantial partial removal** | 17 real deletions and strict MD dispatch; selected runtime still has 15 removed packages and stale OCR execution path; consumer manifest incomplete (F04). |
| **S4-14** | **FAIL cleaned-runtime acceptance** | Lock checks/pins/CPU identity pass; selected installed environment is not the cleaned lock set; final GPU evidence remains unmet. |
| **S4-15** | **NOT_VERIFIED / GPU BLOCKED** | Reported sample does not prove final native integration/tree/near-limit/cancellation resources; raw numbers unbound; GPU absent (F06). |
| **S4-16** | **PARTIAL complete row; capacity correction supported** | Independent 95→12 connection experiment and actual worker/security regressions support test-only bounds. Historic exact context sequence is not reconstructed; final aggregate still FAILED. |
| **S4-17** | **FAIL** | Fifth operation exits 1; four positive Python/static operations do not waive it. Count discrepancy and diff hygiene remain explicit. |
| **S4-18** | **REVIEW COMPLETE / NOT READY** | This independent review identifies blockers. Later Owner routing/acceptance remains required; no self-closeout. |

## V23, transaction/security and compatibility assessment

V23 is a forward **CREATE OR REPLACE function / ACL** change to `rag.seal_document_result`, not a column/table migration. V1–V22 have no working changes. It preserves `SECURITY DEFINER`, `search_path=pg_catalog`, worker attempt checking, exact expected-result ordering, complete subset membership and failure shape checks; PUBLIC/runtime execution is revoked, worker execution granted. Native fresh PostgreSQL migration startup and four explicit privilege tests pass. The schema/function change fits the allowed existing-row/JSON contract; the handback schema description does not.

Its `expected_roots` comes from `runs.metrics.worker_plan`, defaulting to the full admitted manifest. This metadata's allowed selection does not establish a Java-approved unchanged proof. The Java grant/finalizer changes must bind the whole roster/dispositions to the actual subset and avoid deriving expected completeness merely from returned candidate keys. F02 is a new S4 admission/disposition defect, not evidence that pinned embeddings or protected worker seals themselves became forgeable.

Actual source-first locks, attempt fences, producer/verifier stop, rollback and one C_P remain in the existing finalizer. Fresh wire-loss tests and forged-vector/typed-closure/cancellation negatives pass; no unauthorized original reading or cross-type Markdown processing was reproduced. Same-R deleted-original processing works. The published Slice 3/FR18-3 acceptance remains historical CLOSED; no speculative reopening of D17 or prior findings is made. Current S4 mixed plan correctness is not independently accepted until F02 is fixed.

## Independent commands, hashes and preservation

Use `P = rag-engine/.venv-d17/Scripts/python.exe`, root cwd unless noted. Every executable test/diagnostic ran through `scripts/run-bounded-command.py` with a separate exclusive JSON receipt/log; literal absolute argv/cwd, watchdog bounds, XML rosters and all hashes are in the machine evidence and `O/commands.jsonl`.

| Fresh check / receipt | Actual result |
| --- | --- |
| Maven `-ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=<three S4 classes, S3 ConnectionLoss, ten selected S3 corrective parameters> test` / `java-native`, 900s bound | **22/0/0/0**, exit 0, **217.419s**. Exact selector in machine evidence. |
| Same Maven options, `-Dtest=ApplicationArchitectureTests,RagDocumentInputCompatibilityTest,RagWorkerPostgresPrivilegeIntegrationTest,RagWorkerBoundaryIntegrationTest` / `java-compatibility-security`, 420s bound | **67/0/0/0**, exit 0, **182.009s**: architecture 37, input 1, privileges 4, worker boundary 25. |
| Same Maven options, only `RagWindowsNativePrerequisiteTest#attemptsAliasCreationAndRemovalDuringProtectedLifetime` / `ntfs-prerequisite` | **1/1/0/0**, exit 1; expected false assertion receives true. This diagnostic remains FAILED. |
| `P -B -m pytest -q -o cache_dir=O/pytest-cache --basetemp O/pytest-temp`, cwd `rag-engine` / `pytest` | **231 passed**, pytest **96.39s**, exit 0. Full default collection, no test edits/exclusions. |
| `P -B -m ruff check .`; `P -B -m ruff format --check .`, cwd `rag-engine` | PASS; format 66 files. |
| `P -B -m mypy --cache-dir O/fresh-mypy src/ppv_rag`, cwd `rag-engine` | PASS, fresh empty cache, 22 source files. |
| `uv pip check --python P`; `uv lock --directory rag-engine --check --offline` | Both exit 0; **not** proof of removed installed packages. Independent distribution/lock comparison finds 15 extras. |
| `git diff --check` | **exit 2**, new EOF blank line; recorded, not repaired. |
| `javac @O/probe-v2-compile.args`; `java @O/probe-v2-native.args` / separate receipts | Real API/native/PG observations in `native-observations.json` establish F01/F02/cancelled report. Exit 0 means observations captured, not acceptance PASS. |
| `javac @O/capacity-compile.args`; `java @O/capacity-native.args` | Bounded actual Hikari/PG capacity observations; no production change. |
| `javac @O/mapper-compile.args`; `java @O/mapper-native.args` | Production mapper output from the recorded native report; wire fields missing, no fresh HTTP/auth claim. |

Thus **89 focused Java tests pass**, plus a separately failed NTFS reproducer; these are not a new full-suite PASS. Additional diagnostics are not added to JUnit totals. A first reviewer probe mistakenly shared its new observation filename with its watchdog receipt; the receipt replaced those fresh observations. That source/log/receipt and the incident record are preserved; the isolated V2 probe uses distinct outputs and supplies the actual retained observations. **No historical evidence path was affected.**

Actual selected runtime is Python **3.14.4**, ONNX **1.31.0**, tokenizers **0.23.3**, NumPy **2.5.4**, Ruff **0.17.0**, mypy **2.4.0**, pytest **9.1.1**. Model SHA **`98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f`**, tokenizer SHA **`2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8`** independently match pinned artifacts. CPU identity remains accepted; no GPU PASS. Core D17 parser/chunker/model and trusted verifier/source-context bytes remain unchanged; worker cleanup/finalizer/plan changes prevent treating all old whole-file S3 receipts as identical current evidence. No unchanged million-character workload or official corpus was rerun merely for repetition; accepted D17/D20 evidence is retained for its unchanged scope.

All production/test/dependency/fixture/history entry hashes remain unchanged by review, apart from ACTIVE's review-only disposition. Historical ignored-asset hashes remain intact, original generated reports are archived, HEAD and semantic Git index remain unchanged. D22's original missing captures are still **NOT_RECOVERED / original preservation FAILED**; entry preservation is not recovery or a retrospective PASS.

## 29-dimension coverage ledger

Recursive actual trees contain **109 RAG Java production files, 48 RAG Java test files, 22 Python production files, 13 active Python scripts and 66 Python test/fixture files**. Names/API/internal/application/domain/infrastructure/web grouping and accepted root ApiResponse were inspected, not inferred from the handback.

| # | Dimension | Disposition / evidence |
| --- | --- | --- |
| 1 | Owner scope | **FINDING** mandated NO_CHANGE/lifecycle/cleanup/final gates incomplete; D24/D25 authority itself valid. |
| 2 | Traceability | **FINDING** F05 nonexistent/misdescribed cases and unsupported PASS labels. |
| 3 | Architecture/contracts | **FINDING** missing approved existing-row C_N mapping; no unauthorized schema columns actually added. |
| 4 | Module ownership | **PASS** same leaf/public APIs; fresh architecture 37. |
| 5 | Recursive package structure | **PASS scoped layout** inventories above; no new module/framework; cleanup remnants are F04. |
| 6 | Shared transports | **FINDING** app-view/HTTP DTO drift F03; root ApiResponse remains correct under ADR-0016. |
| 7 | SOLID/cohesion | **FINDING** partial equality policies in source/finalizer do not reuse a full authoritative proof. |
| 8 | DRY/invariants | **FINDING** active/pipeline checks diverge from actual eligible G/closure; F01/F02. |
| 9 | Patterns | **PASS scope** existing services/repositories/guards reused; no need for a new generic engine/policy hierarchy. |
| 10 | Business/status semantics | **FINDING** false NO_CHANGE and completed mixed acknowledgement, incomplete roster reports. |
| 11 | Wire compatibility | **FINDING** missing qualification/retention fields, internal-only positive assertions. |
| 12 | Validation | **FINDING** omitted-root proof missing; existing vector/typed-source rejection freshly passes. |
| 13 | Authorization/privacy | **FINDING** NO_CHANGE accepts ineligible retained G; no actual unauthorized live read or forged vector publication observed. |
| 14 | Credentials/offline | **PASS inspected application path** credential-free verifier, local artifacts; no new egress/secrets. Clean runtime remains F04; no OS anti-user guarantee demanded. |
| 15 | Transactions/concurrency | **FINDING** C_N durability and mixed disposition proof; existing physical C_P/wire tests pass. |
| 16 | Recovery | **NOT_VERIFIED for complete S4** C_N/restore/reapprove cases missing; prior and fresh native stop/intent proof credited. |
| 17 | PostgreSQL/migration | **PASS narrow V23 syntax/ACL/forward scope**, fresh PG/privileges; business plan proof remains F02. |
| 18 | Efficiency/resources | **FINDING** repeated report eligibility work and incomplete final resource evidence F03/F06. |
| 19 | Index quality | **FINDING** stale/revoked retained disposition; retrieval/reranking/recommendations **N/A**. |
| 20 | Document fidelity | **PASS unchanged MD core scope** accepted D17 preserved plus fresh producer checks; excluded-format cleanup incomplete, OCR quality **N/A_BY_D24**. |
| 21 | Security tests | **FINDING** new native counterexamples; actual old vector/closure/role denials remain green. |
| 22 | Test quality | **FINDING** claimed tests do not prove the claimed cases; counts alone rejected. |
| 23 | Benchmarks | **NOT_VERIFIED final integration** no identity-bound current native resource profile or GPU proof. |
| 24 | Evaluation/adjudication | **PASS accepted D17/D20 reuse scope** no fabricated human labels/new adjudication; unsupported canonical summaries remain F05/F06. |
| 25 | Dependencies | **FINDING** lock pruned without matching selected install; versions/artifacts unchanged. |
| 26 | Build/static | **FINDING** full Java and diff gate fail; Python/static and focused suites genuinely pass. |
| 27 | Observability/evidence | **FINDING** missing raw measurement/source binding, misleading RAM/VRAM/model/count claims. |
| 28 | Documentation/governance | **FINDING** handback/ACTIVE PASS overstatement; this dated review controls without rewriting history. |
| 29 | Hygiene/delivery | **FINDING** EOF and incomplete removal; original source/history/index preservation verified. No commit eligible. |

## Owner next action and preserved boundaries

**No commit message or ready commit scope is provided while blockers remain.** The inventory identifies review scope, not staging authorization. Owner should route one explicitly bounded correction/evidence task addressing F01–F07 and N01, preserving the accepted existing architecture and historical records; do not infer a new remediation allowance from this review. Required proof includes the exact counterexamples, durable C_N/restore/report cases, cleaned selected runtime, final native resource/GPU disposition, and a genuine successful unchanged five-command final gate after authorized applicability correction. No speculative refactor, new engine, new format or model change is requested.

For NTFS, authorize only the narrow applicability correction if desired; a new global failure waiver is not implied. For unavailable final GPU proof, fulfill the existing contract in a separately authorized environment or explicitly decide a prospective applicability amendment; the reviewer cannot choose one or fabricate results. The overall decision remains **CHANGES_REQUESTED** because actual source and verification defects exist independently of these Owner choices.

Phase 17 **COMPLETE/FROZEN**; Slices 1/D14, 2/D23 and 3/D25 **CLOSED**; FR18-3 **CLOSED at published Slice 3 scope**; D24 `.md` only; D22 originals **NOT_RECOVERED**; historical Java failures and current failed full gate retained. External indexing **INACTIVE**, live originals **DISABLED/DEFERRED**, P18-FINAL and overall Phase 18 **NOT ACCEPTED**. Slice 4/Phase 18 is not declared CLOSED. Stop for explicit Owner routing; no implementation or publishing follows.
