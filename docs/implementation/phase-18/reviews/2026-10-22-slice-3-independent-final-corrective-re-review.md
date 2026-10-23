# Phase 18 Slice 3 — independent final corrective re-review

**2026-10-22 — CHANGES_REQUESTED. FR18-3 remains OPEN.**

The candidate repairs substantial parts of the original trust boundary, including protected PostgreSQL result seals, external UUID eligibility, Markdown capture-format admission and ordinary Markdown dependency naming. It does not satisfy complete Slice 3 acceptance. Three independent observations block acceptance:

1. A norm-preserving forged embedding publishes and becomes CURRENT.
2. A DOMAIN parent routes an approved external Markdown child through plain-text processing without its typed snapshot binding, bypassing D17 rejection and source structure.
3. The fresh native selection fails cancellation during actual staging: requested cancellation finishes ABORTED instead of CANCELLED.

This review implements no corrections and authorizes no further cycle. No staging, commit, push, tag, PR, activation, excluded-format cleanup or Slice 4 work occurred. The submitted handbacks, earlier reviews, failed runs and Owner decisions are preserved.

## Candidate, authority and evidence

Reviewed **main / HEAD `ef32d41b33dd00e5fa08cf63977e91b8936ef3d5`** plus the actual uncommitted candidate. ACTIVE admitted independent review through `IMPLEMENTED_AWAITING_CODEX_REVIEW / READY_FOR_INDEPENDENT_SLICE_3_RE_REVIEW`; the Owner's current instruction authorizes this review only. Applied `codex-final-review`, the 29-dimension rubric, Java/Spring/JPA/modular/reuse/pattern/testing and Python/local-RAG skills, `database-migration-review` for V22, and bounded execution. Cached Graphify was navigation only; findings are verified in source and native execution.

Read/reconciled ACTIVE, D24 and the Owner ledger, the October 22 final corrective handback/matrix/machine inventory, October 21 corrective records and original independent review, executable Slice 3 handoff and unchanged mandatory S3-01–16 body, ADR-0016/0020–24 and applicable snapshot/worker/API/publication/final-gate contracts. D24 validly sets overall external Phase 18 scope to `.md` without waiving security, provenance, publication or resource obligations. No unresolved Owner contract decision explains the defects below.

New outputs use exclusively **`logs/phase18/s3-final-independent-20261022/`**. Exact commands, timeouts, results, source/receipt/log hashes, retained-evidence audits, diagnostic observations and preservation checks are in the [independent machine evidence](2026-10-22-slice-3-independent-final-corrective-evidence.json). The [exact candidate inventory](2026-10-22-slice-3-independent-final-corrective-inventory.json) records all **79 entry dirty paths**, classifications, raw hashes and line endings. Ignored raw logs, Java diagnostic sources/argfiles and archives must be retained separately; JSON hashes do not replace those bytes.

## Original finding dispositions

| Original finding | Independent disposition | Evidence and limit |
| --- | --- | --- |
| **S3-F01 actual-result admission** | **NOT CLOSED — substantial partial repair** | V22 prevents the original runtime staging/grant forgeries and protects complete ordered result seals. Source replay rejects the original false text, source, revision, locator, compact-reference and roster mutations. It excludes embedding values from source comparison, permitting S3-RR-01 below. |
| **S3-F02 publication eligibility** | **CLOSED for the original defect** | External UUIDs use RAG authority, current-read locks use canonical UUID ordering, and G stores post-promotion selected-R/version proof plus original input proof. Genuine native publication is immediately CURRENT; fresh JVM qualification and integrity/revoke/stale denial pass. Incorrectly admitted content remains an F01/F03 issue, not a requirement to rerun inference on every eligibility read. |
| **S3-F03 Markdown dependencies** | **NOT CLOSED — normal Markdown-root naming repaired** | Nested heading/block/display aliases, folder-qualified duplicate basenames, frontmatter aliases, distinct equal-content sources, ambiguity, cycles and changed/revoked child denial have native evidence. A DOMAIN-to-external closure still bypasses that path: S3-RR-02. |
| **S3-F04 captured `.md` scope** | **CLOSED for captured-format admission** | Trusted captured-name/target proof is checked against existing source identity; `.markdown`, TXT/PDF/DOCX/XLS/XLSX labels cannot be converted to MD by a caller. UTF-8 decoding is strict; deleted originals are unnecessary. RR-02 concerns processing/provenance of an actual `.md`, not a reopened suffix defect. |
| **S3-F05 snapshot protocol** | **NOT CLOSED — explicit Markdown-root negotiation repaired** | `RAG_SNAPSHOT / WHOLE_FILE`, S/R UUID/hash/length/selection/version/reset/epoch/policy and individually approved child bindings are present on the Markdown-root path. The DOMAIN branch strips that typed binding from an external child and reports an empty dependency binding: RR-02. |
| **S3-F06 complete integration evidence** | **NOT CLOSED** | Source-bound receipts, actual wire-loss/restart/fence/rollback/privilege cases and measured resource observations are materially stronger. Fresh execution is **94 tests / 1 failure**, and new adversarial cases expose RR-01/RR-02. Complete S3-01–16 acceptance cannot be inferred from the retained 238-test pass. |

**FR18-3: NOT VERIFIED/CLOSED; remains OPEN.** Protected transport/result integrity is necessary but insufficient when the trusted admission path accepts an incorrect vector or an external child through the wrong parser/provenance mode.

## Remaining findings

### S3-RR-01 — High — a valid-norm forged vector survives trusted replay

**Affected contracts:** S3-F01; S3-08; actual vector/model/chunk binding in the Slice 3 handoff and ADR-0022 N8/WF-8; the current review's explicit forged-vector rejection requirement.

At `rag-engine/src/ppv_rag/document_verification.py:122–130`, replay requires 384 numeric finite values and approximately unit norm, then explicitly removes `embedding` before comparison with the source-derived expected chunks. The pinned model is initialized at line 50 for tokenization/provider checks, but no expected embedding is computed or compared. A valid norm does not establish that a vector embeds that chunk with the pinned model.

**Actual native counterexample:** reviewer `S3FreshAdversarialProbe` captured real selected-runtime/model/worker SQL output, negated every component of a genuine embedding, and administratively recomputed the protected result seal using the same explicitly labelled corrupt-producer-result injection pattern as `corruptActualNativeResultsNeverPublish`. All text, structure, locators, source/revision input binding and pinned pipeline identity remained genuine. The norm was unchanged. Finalization returned **COMPLETED / published=1 / CURRENT**.

A second bounded diagnostic, `S3FreshVectorReceiptProbe`, independently confirmed the **stored PostgreSQL vector**, not merely an API declaration:

| Observation | Result |
| --- | --- |
| Original squared vector norm | `0.9999999562494295` |
| Stored dimensions | `384` |
| Stored cosine against genuine vector | `-1.0000000000000004` (floating roundoff around −1) |
| Maximum component error from exact sign reversal | `4.217262261629884E-9` |
| Durable publication | one G, COMPLETED, CURRENT |
| Genuine / forged vector SHA-256 | `ee0ab508c095ae29a913404a95b44605a9e26a7e9691d6652344198cafd6919f` / `10e5eda0caa5fbfefd3d0898048bcc90ed5ee1de936550a3613330abbbecc5c6` |

Raw observations, job/attempt/S/R IDs, grant digests, original producer-returned receipt and stored-vector digest are in `vector-publication-confirmation.json` and this review's evidence directory. The first diagnostic is retained independently in `vector-probe-results.json`.

**Trust-model distinction:** this does not show that `rag_runtime` regained staging-write or seal privileges; those denials now work. It tests an incorrect producer result beyond the protected digest, exactly as the implementer's administrative re-seal negatives do. It is not an OS confinement, malicious same-user filesystem or database-administrator-isolation requirement. Shape/norm/fingerprint labels alone do not meet the requested forged-vector rejection gate.

**Acceptance condition for Owner-routed follow-up:** bind vectors to their exact source-derived chunks and pinned inference through trusted verification or an equivalent sound origin-to-chunk/vector proof, allowing genuine provider numeric differences under a defined justified comparison. Add a real native norm-preserving corruption and vector/chunk reassignment negative. Do not weaken the requirement to dimensionality or L2 norm, introduce a new model, or treat a self-reported metadata field as proof. This is a finding, not implementation authorization.

### S3-RR-02 — High — a DOMAIN closure bypasses external Markdown semantics and typed child provenance

**Affected contracts:** S3-F03/F05; S3-02/06/07; D24 `.md` profile applicability, source-type separation and exact child revision transport.

`RagDocumentInput.java:54–71` handles a DOMAIN root by iterating **every** prepared source in its closure. It does not reject an EXTERNAL_FILE child. Instead, it emits that child's whole Markdown body as a plain `prepared_fields` entry with a numeric RAG policy key and no `snapshot`. `document_source.py:33–44` processes those fields through `parse_text`, while `document_binding` at lines 16–29 only enumerates `root.transclusions` for dependency input bindings. On this branch that map is empty. Source replay repeats the same incorrect plain-text interpretation and therefore agrees with it.

**Normal production API counterexample, no administrative mutation:**

1. Capture and individually approve `ExternalChild.md` containing a heading and `<script>unsupported-active-markdown-material</script>`.
2. Register and separately approve a FILM DOMAIN parent for `title`.
3. Call `addDependency(domainParent, externalChild)`, admit the current DOMAIN parent as a document batch and execute it with a DOMAIN task and no caller body.
4. Java transfers the external child as `prepared_fields[{source_key:"1", approved_field:"content", text:...}]`, with **no child snapshot** and `transclusions:{}`.
5. The native producer, Java verifier and PostgreSQL publication all accept it: **COMPLETED / published=1 / CURRENT**. The child's R is selected/promoted. PostgreSQL stores its Markdown heading and `<script>` material as plain paragraphs with numeric source identity `1`, rather than D17 Markdown source structure under its external UUID.

Exact issued input, external UUID/policy ID, selected child revision and stored content/locators are retained in `mixed-closure-results.json`. The script bytes were **not executed**; the demonstrated defect is unsupported Markdown being certified through the plain-text branch. The child's Java lease/G closure still retains authority metadata, and reservations were acquired: this is not evidence of unauthorized original reads or reservation release bypass. It is a reachable parser/typed-provenance bypass that should reject an unsupported closure or preserve the child's actual snapshot/Markdown contract.

The separately green MD-parent → MD-child cases and `.md` suffix gate do not cover this alternate entry path. V22 now permits the edge, so the Java boundary must enforce the applicable processing contract. Frozen DOMAIN field parsing does not grant permission to reinterpret an external source as DOMAIN text.

**Acceptance condition for Owner-routed follow-up:** fail closed on unsupported cross-type closures before dispatch, or preserve the already required external snapshot binding and D17 processing if that combination is intended within the approved contract. Keep legitimate DOMAIN-only operations and mixed **independent** DOMAIN/EXTERNAL roots working. Verify both a safe structural Markdown child and a mandatory-rejection child through ordinary APIs; require no publication, child promotion or partial common-failure finish on the bypass. Do not relax D17 or infer a new cross-format feature authorization.

### S3-RR-03 — Medium, blocking — cancellation loses its terminal disposition to automatic abort

**Affected contracts:** S3-F06; S3-15; known cancellation → CANCELLED under the existing Slice 3/ADR-0021 finish contract.

The independent existing native test `RagSlice3CorrectiveIntegrationTest.cancellationDuringRealWorkerStagingDrainsItsPhysicalWriterBeforeRelease` **failed at line 552**: expected `CANCELLED`, observed `ABORTED`. Its real worker was blocked inside the PostgreSQL staging trigger, a separate cancellation request waited on its attempt fence, and the test then released the writer barrier. Both futures completed through their asserted paths; the final status assertion failed.

The before-cleanup observation confirms **ABORTED, staging_closed=true, zero G/outcomes/staging/grants, no selected R, reservation released**. No publication or process leak is established by this failure. The defect is cancellation outcome accounting and concurrency, not evidence that a live writer was knowingly released.

Source inspection identifies a reachable race consistent with this result: `RagDocumentBatchService.java:286–288` calls `abortJob(job,false)` after cancellation-induced send/stage failure, while explicit `cancelJob` calls `abortJob(job,true)`. `RagPurgeService.java:47–54` records only STOPPING; after process shutdown, either caller can reach the terminal transaction first. Lines 89–96 preserve the first terminal winner, and lines 64–66/75–79 treat an already ABORTED job as a successfully finished cancellation. The cancellation intent is not given an effective ordering guarantee. The receipts do not identify which competing finishing caller committed first; this is code-backed root-cause analysis, not a reconstructed thread timeline or a measured failure frequency.

The retained 238-test pass remains a valid earlier pass. It does not override this fresh same-source native failure. No unchanged full selection was rerun to obtain a favorable result. The shared helper's historical Foundation acceptance is not reopened wholesale; its newly exercised v3 cancellation path must meet the Slice 3 contract.

**Acceptance condition for Owner-routed follow-up:** coordinate explicit cancellation and automatic failure teardown so a known requested cancellation has the required disposition while preserving positive stop, writer drain, one atomic finish and safe unknown-outcome handling. Add an ordering-controlled regression for both competing callers. Do not change this assertion to accept ABORTED or relax fencing.

## Complete S3-01–16 assessment

This matrix distinguishes verified mechanisms/scenarios from complete acceptance. `PARTIAL` and `FAIL` are not PASS. No complete Slice 3 acceptance follows from an individual green row.

| Gate | Independent result | Actual evidence / remaining limit |
| --- | --- | --- |
| S3-01 retained FR18-3 | **PARTIAL history; FR18-3 OPEN** | Retained D14 stdout/receipt hashes verify the actual `Invalid worker batch result provenance` exception and named DOMAIN boundary. Current named test passes in the source-bound 238 selection; previous independent before-correction counterexamples are preserved. Original Antigravity pre-repair stdout is still missing, not reconstructed or attributed an invented inner cause. RR-01/RR-02 prevent present admission closure regardless. |
| S3-02 snapshot transport | **PARTIAL / FAIL for alternate closure** | Direct MD paths verify immutable bytes/hash/length/consent, strict UTF-8, deleted originals, captured names and excluded suffix denial. A DOMAIN closure omits the external child's typed snapshot transport: RR-02. |
| S3-03 actual `.md` 8/2 | **VERIFIED for the required scenario** | Fresh real native batch publishes 8, fails exactly 2 unsupported active-HTML files, records ten outcomes, promotes only successful R and releases the union; exact reason assertions retained. Generic trust exceptions remain RR-01/RR-02. |
| S3-04 all failed | **VERIFIED** | Fresh all-empty batch ABORTED, no G/promotions, complete failures and safe release. Failed-root forged binding rejects as common trust failure. |
| S3-05 roster/text/seal | **VERIFIED for exercised roster/content integrity** | Runtime cannot manufacture/change/erase active result proofs; native omission/duplicate/reorder/extra-root/content and seal/input faults reject. A valid result seal does not prove model/vector correspondence: RR-01. |
| S3-06 source/compact locators | **PARTIAL overall** | Exact accepted-source replay rejects fractional/negative/dangling/default-shadowed/fabricated/oversized/Unicode offset mutations. The alternate DOMAIN child path lacks the applicable D17 source context/typed child role, RR-02. |
| S3-07 dependencies | **FAIL** | Normal native Markdown aliases/folders/anchors/nested revisions/cycles/ambiguity/equal-byte source separation and changed-child denials pass. Unsupported mixed closure publishes through plain text, RR-02. |
| S3-08 model/vectors | **FAIL** | Pinned tuple/artifacts, unavailable CUDA rejection, finite numeric/dimension/norm/type/overflow and PostgreSQL type negatives pass. A sign-reversed genuine vector publishes CURRENT, RR-01. |
| S3-09 consent/CAS | **VERIFIED for exercised source-first barriers** | Ten real post-verification/pre-publication root/child faults, pending-pointer identity, normal busy mutation/revoke and fresh-JVM currentness/denial cases pass. No implicit consent renewal or accepted administrative mutation is claimed. |
| S3-10 PostgreSQL provenance/roles | **VERIFIED for audited SQL boundary** | Real runtime/worker/PUBLIC-only connections deny staging/grant/pointer/publication/DDL operations; wrong token/attempt/absent/unconsumed/mixed-v2 admission denies. RR-01's administrative re-seal is explicitly beyond these working runtime permissions. |
| S3-11 writer/seal | **VERIFIED for both native SQL orders** | Separate restricted connections use real native payloads; writer-first publication blocks until staging/seal COMMIT; publication-first writer blocks then denies; post-wait expiry denies. Administrative fixture reset is disclosed. |
| S3-12 atomic rollback | **VERIFIED for tested multi-root faults** | Native generation/chunks/outcome/promotion/deferred-C_P faults leave no partial candidate/outcome/release, preserve selected old R and an independently CURRENT sibling G. No assertion that a separately renewed/stale old G is CURRENT. |
| S3-13 unknown committed | **VERIFIED** | Fresh PostgreSQL COMMIT CommandComplete dropped before JDBC receives it; independent connection observes durable single publication; reconciliation/replay does not resend/duplicate. This is real wire loss, not only an injected Java exception. |
| S3-14 unknown uncommitted/restart | **VERIFIED for tested outage/restart boundaries** | Fresh pre-COMMIT wire cut retains RECOVERY_REQUIRED/reservations during runtime proxy outage, safely aborts after recovery. Actual PRODUCING/STAGED/VALIDATING controller halt, recorded worker/verifier death and fresh JVM cleanup/current qualification pass. Outage is runtime reachability, not machine-wide PostgreSQL shutdown. |
| S3-15 cancellation/resources | **FAIL cancellation; measured profile verified within limits** | Transfer, CPU, verifier/temp-disk and expansion cases pass; actual staging cancellation returns ABORTED, RR-03. V4 raw OS measurements and all 175 bound identities independently audit correctly; CUDA inference UNAVAILABLE. |
| S3-16 regression/evidence | **FAIL current selected gate** | Retained final 238/0/0/0 source identity is valid. Fresh high-risk selection is **94/1/0/0**; Python 189, static/dependency/offline checks pass. No full clean gate or P18-FINAL was run or inferred. |

## Trust, atomicity, concurrency and recovery credit

**V22 migration:** SHA-256 **`443802cfe7a4b6a0d41939e61b93ef7190210ff0f2a13bdb0cfb7c270ee488e6`**. It appends after V21 without adding a table, column, route, status, module or dependency. Historical migration contents remain unchanged. Reviewed function search paths, existing grants retained by CREATE OR REPLACE, explicit PUBLIC revocations, role checks and trigger/retention behavior. Fresh native database startup applies/validates the migration and exercises actual restrictions.

The former input-derived digest is now an **unsealed issuance sentinel**, not accepted final output. Only the audited worker seal computes the separate PostgreSQL-canonical digest over exact ordered results, roster/status/reason/warnings/chunks/vectors/resources and job/attempt/input/scope. Runtime staging INSERT/UPDATE is revoked; nonterminal result/grant deletion is denied; pre-consumed v3 issuance is denied. Existing UPDATE denial on grants and `session_user=rag_worker` attempt checks remain. The actual HTTP receipt is compared with the protected result seal. After positive producer stop, Java reads the sealed candidate, validates exact pipeline keys and executes credential-free source replay before C_P.

That repair prevents the original ordinary-runtime corruption counterexamples. It does **not** prove every semantic result: RR-01 excludes embeddings from replay and RR-02 gives both producer and verifier the same incorrect source interpretation.

The verifier uses existing suspended native process supervision, commits its exact process/creation identity before resume and receives no worker DSN/service credential. Its managed input/output files, process cap, deadline/cancellation and cleanup are bounded. The row identity is append-only for one verifier; native restart cases exercise both identities. No OS sandbox or arbitrary same-user adversarial proof is claimed.

Source-first guards are rechecked before publication. Approved pending/selected R, actual pending-pointer identity, hash/length, selected-R/version/reset/epoch/policy, name proof and exact graph are checked under their existing guards. The publication manifest transforms promoted sourceVersion/selected-R context while retaining original input proof. All successful generations/chunks/outcomes/promotions and exact typed union release remain in one physical C_P. Preparatory drain and verifier identity binding are separate transactions, not extra publication commits.

**Release before promotion remains acceptable in this transaction:** its locked reservation row cannot become editable to another transaction before COMMIT; NOWAIT mutation backstops remain, refresh prevents stale managed reservation state, and multi-root/deferred physical rollback and real lost-COMMIT tests support atomicity. The publication callback's recorded hash is unchanged from the earlier independently observed C_P. No safety conclusion is inferred solely from the trigger allowing the sequence.

The fresh native tests prove both writer/seal orders, late writer denial, deterministic last-guard mutation rejection, wire-loss reconciliation and real controller/worker/verifier restart. These are substantive new evidence, not mocks of the critical trust boundary. The cancellation disposition race remains RR-03; it is not relabelled as a verification PASS because teardown avoided publication.

## Source, corpus, process/resource and failure audit

The final v4 source manifest has **1,740/1,740 current matches**. The v3 full manifest differs in the subsequently strengthened Java mixed-batch fixture; its claimed **120-file Python subset has 120/120 current matches**. That Java test was rerun in v4 and independently here. A preliminary reviewer audit that compared all 1,740 v3 files, instead of its Python subset, is retained; the scope correction is explicit and is not an implementation failure.

Independently checked all **285 final execution artifact hashes**, all 10,170 inventoried ignored evidence assets, every listed final receipt/log hash, raw resource-report hashes inside the native result observations and source/receipt associations. The retained Java console and source-bound report archive establish **238 tests / 0 failures / 0 errors / 0 skips**, exit 0; this is attributed implementer evidence, not a new independent full run.

Both retained frozen-corpus captures contain **1,329 unique (corpus,id) cases: 1,267 supported structural passes + 62 required whole-root rejections**, with no unexpected failures. Reviewed the isolated structural oracle/harness and unchanged gold/resolution identities. One is parser/chunker AST evidence; the other runs real pinned CPU inference with mocked SQL/seal boundaries. Neither alone proves Java/PG admission. Their Python source continuity is valid, so these complete corpora were audited/reused rather than rerunning historical scripts. No human signature, fresh held-out quality label or broad Slice 2 acceptance is fabricated. D17's historical accepted decisions/findings remain preserved; no universal maximum-size performance claim is added for this changed integration.

**175 native identities independently verified from raw records:** **96 producers + 79 verifiers**. Each `(PID, creation FILETIME)` in the final-run native bindings has an observed OS process record with memory and CPU measurements; no missing/unmeasured bound identity or parent-creation inconsistency was found. Reviewed the v4 retained-handle and parent-lifetime validation algorithm. The two unidentified short-lived candidate PIDs are not bound identities; no measurements were invented for them. The other 299 observed children are orchestration/unclassified processes, not additional certified producers.

| Valid retained v4 resource observation | Measurement / qualification |
| --- | --- |
| Sampled simultaneous owned-tree working set | `2,844,975,104` bytes |
| Sampled simultaneous private memory | `3,083,436,032` bytes |
| Cumulative tree CPU | `1,623.109375` seconds, across the selected run/processes |
| Wall time / sampling | `1,128.8576` seconds; 50 ms target, 13,623 samples |
| Host total / minimum available RAM | `17,112,092,672` / `776,765,440` bytes; host-wide, not worker usage |
| Sampled owned temp / capture-validator temp / revision bytes | `8,623,477` / `2,389,542` / `500,155` bytes |
| Device-wide VRAM sample | `1,678,184,448` bytes; **not attributable worker VRAM** |
| Actual ONNX providers | Azure + CPU; **CUDA inference UNAVAILABLE**, no GPU PASS |
| Final observed cleanup | zero recorded owned survivors, unreadable owned identities or managed capture temps |

Sequential per-process lifetime peaks summed to `130,957,000,704` bytes are **not simultaneous RAM**. V1–V3 profiles remain **INVALID for resource acceptance** because of numeric-PID reuse tracking; their raw evidence is preserved. V4 has sampling limits, can miss transient disk/short-process peaks and does not prove zero global Windows writes or the deferred Slice 4 maximum-resource profile. This review audits/reuses these valid observations; it does not claim to have rerun the full resource monitor.

The actual local ONNX/tokenizer identities and selected CPU environment agree with the pinned tuple. RapidOCR's existing version string remains inert compatibility metadata; Markdown does not initialize OCR, no OCR package was installed, and no excluded adapter is accepted by this review.

Failure preservation is explicit: the original independent CHANGES_REQUESTED review, prior BLOCKED handback, Antigravity exit-only entry receipt, intermediate corrective failures and Java **236/0/1/0** remain failures. That run's outer orchestration exit 0 is not Java success. Source/namespace/observer/assembly failures retain their original receipts and qualifications. The historical full Java **1,433/1 failure/2 errors/0 skips** remains FAILED. New `java-native.json` is also **COMMAND_FAILED**, not superseded by the older 238 pass.

## New verification and reproducibility

All new commands used `scripts/run-bounded-command.py`, fresh receipt/log paths, selected Python 3.14.4 `.venv-d17`, the actual local model, fresh controlled PostgreSQL 18.6/pgvector 0.8.6 and `PPV_S3_EVIDENCE` pointing only to the new directory. No historical fixed-output harness was executed. Exact absolute argv/cwd/environment selection, argfile/diagnostic source hashes and individual durations are in the machine evidence.

| New receipt | Exact selection / result |
| --- | --- |
| `java-native.json` | `RagSlice3CorrectiveIntegrationTest` **88/1/0/0**, `RagSlice3ConnectionLossIntegrationTest` **2/0/0/0**, `RagSlice3VerifiedIndexingIntegrationTest` **4/0/0/0**; aggregate **94/1/0/0**, exit **1**, 934.742 s. No timeout or skip. |
| `python.json` | Same 14-file final Python selection: **189 passed**, exit 0; pytest 57.18 s. |
| `mypy.json` | New empty cache: **26 source files**, exit 0. |
| `ruff.json` / `format.json` | PASS, exit 0; **79 files** already formatted. |
| `pip.json` / `lock.json` | **67 compatible installed packages**, offline lock **73 resolved**, exit 0; no install/upgrade/lock regeneration. |
| `probe-native.json` | Real native sign-flip diagnostic; process exit 0 records wrong acceptance, **not acceptance PASS**. |
| `vector-confirm-native.json` | Separate stored-pgvector confirmation with actual cosine/hash measurements; same distinction. |
| `mixed-native.json` | Normal-API mixed closure diagnostic; process exit 0 records wrong acceptance, **not acceptance PASS**. |
| `diff-check.json` | `git diff --check`, exit 0; existing CRLF advisories preserved. |

Fresh native command:

```text
mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=RagSlice3CorrectiveIntegrationTest,RagSlice3ConnectionLossIntegrationTest,RagSlice3VerifiedIndexingIntegrationTest test
```

This deliberately targets new high-risk behavior; it is not labelled a new 238-test or complete clean run. The remaining unchanged legacy/module/API selections are reused through the exact v4 source/report hashes. Independent Python selectors are the seven original D17/worker/Slice 3 files plus `test_slice3_corrective.py`, `test_slice3_dependency_names.py`, `test_contracts.py`, `test_bound_ingress.py`, `test_consumption_containment.py`, `test_chunker.py`, `test_embeddings.py`, with fresh pytest cache/basetemp.

Static commands are `python -B -m ruff check rag-engine`, `python -B -m ruff format --check rag-engine`, `python -B -m mypy --config-file rag-engine/pyproject.toml --cache-dir <new-empty-cache> rag-engine/src`, `uv pip check --python <selected-interpreter>`, and `uv lock --directory rag-engine --check --offline`. Reviewer Java probes compile against the actual Maven test classes/dependencies into an ignored probe directory and use hashed Java argfiles. Only the sign-flip negative performs administrative DML; the mixed closure uses ordinary approved application APIs. No production/test source in the candidate was changed.

The **276 entry Surefire files** were exclusively archived and hash-verified before execution; all fresh reports were separately archived after the failed run. Native failure observations were captured before fixture cleanup. No rerun was used to turn that failure green. Existing Mockito/CDS/compiler warnings are recorded, not suppressed; no IDE warning-free claim is made.

## Architecture and 29-dimension coverage

Actual recursive inventories contain **109 RAG production Java files, 46 RAG test files, 26 Python production files and 68 Python test/fixture files**, excluding bytecode. Existing API/source/internal/application/domain/persistence/storage/client/web ownership remains. The sequential verifier reuses the accepted parser/chunker, existing managed storage and native supervisor; no second parser framework, queue or generic job engine is introduced. Root `ApiResponse`, module-local adapters/advice and named-interface boundaries remain consistent with ADR-0016; no arbitrary common module or speculative refactor is requested.

| # | Dimension | Disposition / evidence |
| --- | --- | --- |
| 1 | Intent/scope | **FINDING** RR-02 external Markdown contract bypass; D24 itself valid. |
| 2 | Traceability | **FINDING** RR-01–03 prevent complete matrix acceptance. |
| 3 | Architecture/contracts | **FINDING** external-child typed contract omitted; no new module/schema architecture. |
| 4 | Module ownership/dependencies | **PASS** existing public Vault/Film/Fiction APIs; reused exact architecture receipts. |
| 5 | Recursive package structure | **PASS** actual inventories above; narrow verifier/helper placement. |
| 6 | Shared transports/exceptions | **PASS** unchanged ADR-0016 root envelope/module-local adapters. |
| 7 | SOLID/cohesion | **FINDING** verifier's vector responsibility incomplete and root-type branch drops child type. |
| 8 | DRY/KISS/invariants | **FINDING** shared replay repeats wrong mixed interpretation; competing cancellation finishers. |
| 9 | Patterns | **PASS** existing facade/repository/adapter/supervisor reuse; no speculative framework. |
| 10 | Domain/status correctness | **FINDING** wrong CURRENT publications and cancellation disposition. |
| 11 | Wire compatibility | **FINDING** typed external child missing in DOMAIN branch; direct MD/v2/DOMAIN compatibility otherwise supported. |
| 12 | Input/data validation | **FINDING** vector semantics and mixed source type; original malformed/locator/roster negatives now work. |
| 13 | Authorization/privileges | **FINDING** processing/typed scope bypass; actual runtime/worker/PUBLIC SQL denials pass. No unauthorized-original read shown. |
| 14 | Credentials/privacy/offline | **PASS within inspected paths** no new remote endpoint, raw credential CLI or sensitive application logging; verifier has no SQL credentials. |
| 15 | Transactions/concurrency | **FINDING** cancellation race; physical C_P/source guards/rollback/wire-loss mechanisms have real positive proof. |
| 16 | Recovery/cancellation | **FINDING** RR-03; fresh restart and committed/uncommitted outage cases otherwise pass. |
| 17 | DB/migrations | **PASS for V22 scope/permissions** forward existing-function/guard/grant repair, old migrations preserved; mixed processing policy remains RR-02. |
| 18 | Complexity/resources | **PASS within measured integration range** finite caps and v4 identity-bound observations; full maximum profile deferred. |
| 19 | Index/provenance quality | **FINDING** anti-vector and untyped external derivative accepted. Retrieval/reranking N/A to this slice. |
| 20 | Markdown/multilingual fidelity | **FINDING** RR-02 integration bypass; frozen corpus/native MD-root evidence supports producer parsing. OCR/Office/PDF acceptance N/A. |
| 21 | Security tests | **FINDING** new independent adversarial publications; improved native role/forgery cases credited. |
| 22 | Test quality | **FINDING** 94-case selection fails and missing adversarial cases expose remaining behavior; counts alone rejected. |
| 23 | Benchmarks | **PASS for bounded evidence reuse** v4 sampling/CPU/RAM/disk correctly distinguished; no universal 1M/GPU performance claim. |
| 24 | Quality adjudication | **PASS for D20/frozen evidence reuse** corpus/source identities audited; no fabricated human or new held-out acceptance. |
| 25 | Dependencies/model | **PASS selected runtime** actual pin/lock/package checks; CUDA unavailable and inert OCR identity qualified. No new vulnerability audit claimed. |
| 26 | Build/static/tooling | **FINDING** fresh Java gate failed; Python/static/dependency gates pass and old full gate remains failed. |
| 27 | Observability | **PASS for observed evidence** raw receipt hashes, lifetime identities, resource distinctions and preserved failures; limits disclosed. |
| 28 | Documentation/governance | **FINDING** submitted all-row readiness cannot be accepted after RR-01–03; historical records unchanged. |
| 29 | Hygiene/delivery | **PASS review preservation; delivery blocked** exact inventory/index/head continuity and raw line endings checked. |

## Preservation, exact delivery boundary and next Owner action

Entry hash snapshot covers **2,493 nonignored files**; production/tests/dependencies/fixtures and prior records remain unchanged during review. The checked historical ignored-asset set and all entry Surefire bytes are catalogued separately. Only ACTIVE is updated with this review disposition; new review/evidence/inventory documents are added. HEAD and semantic Git index remain unchanged. Existing raw CRLF/authored fixture whitespace was not normalized.

The exact reviewed candidate inventory identifies **14 Java production paths, 7 Python production/shared D17 paths, V22, 17 test paths, 6 corrective handback/evidence records and 34 preparation/governance/historical-review paths**. It contains every path/hash rather than directory-wide staging advice. Published excluded-format code is baseline, not newly certified functionality. Ignored models, environments, raw logs, caches, diagnostic classes and temporary artifacts are outside any proposed commit.

**No Owner commit scope is eligible on this review.** The 79-path inventory is the exact candidate boundary for future reconciliation, not a staging authorization. No commit message is supplied while blockers remain, and no index projection/staging was constructed. Keep the raw independent evidence and failed run with the reviewed candidate.

Preserve Phase 17 COMPLETE/FROZEN; Slice 1/D14 CLOSED; Slice 2/D23 Markdown-only CLOSED; D24 overall `.md` scope; D22 original captures **NOT_RECOVERED / original preservation FAILED**; historical aggregate Java **FAILED (1,433 / 1 failure / 2 errors / 0 skips)**; external indexing INACTIVE; live originals DISABLED/DEFERRED; P18-FINAL NOT ACCEPTED; Slice 4 NOT AUTHORIZED. Original broader-format implementations remain unaccepted. Their consolidated cleanup, complete lifecycle/NO_CHANGE/Reindex/purge/restore, aggregate PostgreSQL resource investigation, full final resource profile and five-command P18-FINAL remain future separately authorized obligations.

**Next Owner action:** review RR-01–03 and the still-open original findings, then explicitly route any further bounded work. The review does not initiate remediation, takeover or another checkpoint, reset historical Antigravity 2/2, close Slice 3 or authorize Slice 4. No Owner contract waiver is assumed or requested to turn these defects into acceptance. Stop after this independent handback.
