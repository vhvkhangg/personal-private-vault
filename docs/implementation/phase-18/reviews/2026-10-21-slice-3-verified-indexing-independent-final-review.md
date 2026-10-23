# Phase 18 Slice 3 — independent final review

**2026-10-21 — CHANGES_REQUESTED. FR18-3 remains OPEN.**

The actual native Markdown path now publishes useful output, but mandatory trusted admission and publication-local eligibility requirements fail. Independently reproduced runtime-authored false content/provenance publication, incomplete-output partial publication, unusable newly published external generations, rejected approved child dependencies, and acceptance of an expressly excluded `.markdown` source. Green focused tests do not close these findings.

This is review only under `codex-final-review`. No production/test implementation, migration, dependency, index, commit, push, tag, PR, activation or remediation authorization was made. Reviewer diagnostics ran exclusively against synthetic documents and fresh owned PostgreSQL containers. ACTIVE is synchronized to this decision; the implementer handback is preserved as submitted.

## Authority, candidate and evidence

Reviewed main/HEAD **`ef32d41b33dd00e5fa08cf63977e91b8936ef3d5`** plus the current uncommitted candidate. Read AGENTS, ACTIVE, D24 and its applicability/baseline record, the executable Slice 3 handoff and mandatory S3-01–16 matrix, simplified handoff, Slice 1/D14 and Slice 2/D23 closeouts, the actual `slice-3-verified-indexing-handback.md`, ADR-0016/0020–24, snapshot/worker/API/DBML/acceptance/final-gate contracts, D17 and prior independent closeout evidence. Inspected changed production/test files and their reachable authority, client, worker, storage, grant, SQL, current-state, supervisor, purge and recovery consumers.

**D24 is authoritative and sufficiently explicit:** overall Phase 18 external format is `.md` only; `.markdown` is not an alias. Its additive ADR-0021–24 applicability notices preserve earlier text as history. It does not weaken exact S/R/hash/length/consent, producer verification, restricted SQL, source-first fencing, one physical C_P, recovery, fidelity or finite resources. No additional Owner contract decision is needed to reject the defects below. ADR-0020/Vault and Phase 17 remain frozen. The former Slice 2 selective-delivery workflow is superseded by the Owner-confirmed published baseline and is not reopened.

The [machine evidence](2026-10-21-slice-3-verified-indexing-independent-evidence.json) contains exact argv/cwd/environment selection, watchdog receipts and hashes, raw source hashes, diagnostic observations, preservation checks and limits. The [complete changed-path/subtree inventory](2026-10-21-slice-3-verified-indexing-independent-inventory.json) identifies all **44 entry dirty paths**, their raw SHA-256/line endings and classifications. New evidence uses only `logs/phase18/s3-independent-20261021/`; retain that ignored directory separately. Historical fixed-output scripts were not run.

## Blocking findings

### S3-F01 — Critical: actual output/provenance is still forgeable

**Contracts:** FR18-3; handoff packages 2/3; S3-05/06/10; ADR-0022 N8/WF-8 and ADR-0023 publication.

`RagWorkerGrantService.java:87` and `RagDocumentFinalizationGate.java:133` still derive output admission from `SHA256("DOCUMENT_V3:" + inputDigest)`. This predates actual inference and binds no actual result. Python `worker.py:399` instead hashes job/attempt/success/failure counts; `RagEngineClient.java:282` validates only the response envelope, returns it, and `RagDocumentBatchService` discards it. No canonical actual-result digest is checked against the sealed stage or published rows.

The finalizer's `validateChunk` (`:387–496`) receives no prepared source/closure. It checks nonblank content, locator shape/order and vector dimensions, rather than source-backed content/coverage/revision/coordinates. Compact validation uses `Number.intValue()` (`:470–484`), so fractional `0.5` becomes index 0. Definitions, defaults, ancestry, structure/cell identities and their source bindings are not losslessly resolved and validated. A source identity is copied directly at `:257`.

**Independent actual Python → Java → PG counterexample:** after native inference and real worker SQL staging of an approved `.md`, the ordinary `rag_runtime` connection updated the first staged chunk to synthetic text absent from the source, an unauthorized source identity, revision 999, field `secret`, lines 900000–900001, character range 8000000–8000100, and a fabricated compact node with `locator_ref=0.5`. The original genuine pipeline/vector remained. Finalization returned **COMPLETED / published=1** and PostgreSQL contained **one forged published chunk**. This is not a mocked DTO or a malicious same-user OS attack. It is the explicitly required compromised-result/runtime-DML fault injection.

V18 (`:259–260`) grants runtime staging INSERT/UPDATE/DELETE, and the finalizer merely requires a consumed grant. A genuine consumed grant does not authenticate later staged content. `RagDocumentBatchIntegrationTest.java:60–78` also manufactures consumed grants using runtime DML and supplies synthetic 384-element vectors; those tests cannot prove worker-only admission.

A second native counterexample deleted one of two real staged roots using runtime DELETE. Finalization returned **COMPLETED / 1 published / 1 failed**, treating the missing result as `UNPROCESSED` (`FinalizationGate:287–294`). Missing/corrupt output is a common trust failure, not an explicit independent producer failure. Exact roster/chunk completeness/order and actual output integrity are unproved.

**Required acceptance repair/proof, not authorization:** bind canonical actual results separately from input/scope; enforce nonforgeable worker admission and complete sealed rosters; validate source-backed normalized content and all compact references against exact approved roots/children. Reproduce these native mutations plus chunk omission/duplication/reordering and unauthorized child metadata, with zero new G/promotion/outcome/release on common trust failure. Reuse the accepted source-context representation; no second general Markdown parser is required.

### S3-F02 — High: a successful external publication is immediately unavailable and has a stale version manifest

**Contracts:** handoff package 3 and its explicit publication-local eligibility requirement; S3-03/09; independent R/G binding under ADR-0021/22/23.

The legitimate native diagnostic returned **COMPLETED / published=1 / selected sourceVersion=2**, but `RagGenerationState.state(newG)` returned **UNAVAILABLE**. `RagGenerationState.java:63` unconditionally parses every `domain_id` as a Vault `long`; an external UUID throws and is caught as UNAVAILABLE. `RagDocumentReportService.java:69` uses this same production path. This is fail-closed, but successful indexing does not deliver a usable verified generation.

There is another coherence defect: the finalizer freezes the prepared manifest and expected sourceVersion **before** promotion (`:230–235`), then `promotePendingToSelected` increments external sourceVersion (`:329`). The manifest still describes pre-promotion version 1 while selection is version 2. Correcting only UUID parsing would therefore leave a stale generation proof. No claim is made that that unimplemented correction was tested.

**Required acceptance repair/proof:** correctly discriminate external versus Vault read authority and publish a coherent exact selected-R/G/version proof in C_P. A real new external G must be freshly eligible immediately after commit and remain independently qualified after restart; wrong/withdrawn authority must deny. Full Slice 4 lifecycle remains deferred; this immediate-publication defect does not.

### S3-F03 — High: approved Markdown child dependencies cannot enter the real path

**Contracts:** S3-02/07, independently approved nested Markdown transclusions and exact child revisions.

Two separately captured and approved `.md` sources, `Parent.md` containing `![[Child]]` and `Child.md`, were passed to production `addDependency(parent,child)`. PostgreSQL rejected the edge with **`Dependency owner scope unavailable`**. The unchanged V14 `rag.guard_dependency_change()` (`:265–270`) permits only FICTION/FILM ancestors and casts owner identities to bigint. No subsequent migration repairs it for external UUID authority. The native worker cannot receive this legitimate child closure through the application API.

There is a downstream transport gap as well: `RagDocumentInput.java:35–43` keys child transclusions only by external UUID, not the approved Obsidian target name/alias. Normal authored `![[Child]]` has no demonstrated approved name-to-S/R binding. This latter conclusion is source inspection; the end-to-end diagnostic stopped at the earlier SQL guard, and did not bypass it with an administrator-created edge.

The new Python `test_transclusion_closure_with_child_payload` only constructs a DTO and asserts that its supplied map contains the child. It executes neither parsing nor Java/SQL authority and cannot establish this requirement.

**Required acceptance repair/proof:** make the existing dependency guard/transport support the authorized typed external closure without weakening Vault rules or inventing a Vault owner. Any necessary function repair must append after V21. Demonstrate ordinary names, nested/anchored child bindings, exact child R/locators, duplicate-byte distinct sources, unauthorized/missing/revoked/cyclic children and source-specific isolation through the native publication path.

### S3-F04 — Medium, blocking scope defect: `.markdown` is accepted as `.md`

**Contracts:** D24 clause 2 and S3-02's explicit alias rejection.

The independent diagnostic registered and approved **`excluded.markdown`**, then dispatched an MD task with no caller body. It returned **COMPLETED / published=1**. `RagDocumentBatchService.java:254–258` checks the caller's format label, while `RagSourceAuthority.java:124–160` treats any approved external snapshot as UTF-8 Markdown without a Java-owned admitted `.md` format check. Rejecting task `format=TXT` is insufficient to prove the original source is an authorized `.md`.

**Required acceptance repair/proof:** bind supported format to trusted captured source identity and reject excluded sources before transfer. Preserve immutable-snapshot operation after the original disappears; do not solve this by reopening live originals. Test `.markdown` and excluded file types supplied with an MD task, while normal `.md`/DOMAIN cases remain authorized separately.

### S3-F05 — High: explicit snapshot protocol and immutable revision tuple were not implemented

**Contracts:** handoff package 1, ADR-0022 exact tuple/negotiation and the API contract's explicit negotiated snapshot mode.

The external path silently reuses `contract_version=3.0`, with no RAG_SNAPSHOT/WHOLE_FILE discriminator or equivalent explicit negotiation. `RagDocumentInput` supplies text/sourceVersion; `contracts.py` accepts the existing `DocumentRootTask`. `RagSourceAuthority.SourceProof` (`:56`) and its emitted node (`:152–160`) omit revision UUID, byte length and expected selected-R identity. It still emits a legacy-shaped inner `2.0` manifest (`:203–205`), newly allowing external sources in `RagManifestScope`.

The store genuinely verifies S/R/hash/length locally, which is credited. However, reading the correct R is not the required end-to-end binding: those identities are absent from the issued producer/result/G contract and cannot be compared in returned provenance. Content hashes and mutable sourceVersion are not R UUIDs. No inference of a demonstrated normal-API revision-swap exploit is necessary or claimed; the mandated negotiated authority tuple is missing in reachable production serialization.

**Required acceptance repair/proof:** carry the already-required explicit mode and exact root/child revision/selection/length/authority associations consistently across issuance, transport, result admission and G. Reject missing/legacy/forged modes and revision substitutions while preserving unchanged Foundation v2 and DOMAIN behavior. No new storage architecture or implicit schema expansion is approved by this review.

### S3-F06 — High: mandatory integration evidence is incomplete and handback mapping overstates it

**Contracts:** mandatory S3-01–16 and tracked machine-evidence manifest in the executable handoff.

The implementer directory has six top-level watchdog JSON receipts, without preserved pre-repair stdout/stderr, a source-bound actual-result manifest or the required matrix observations. `fr18-3-before.json` establishes exit 1 in 36.125s; it does **not** independently establish the stated failure cause. The named retained test is a DOMAIN test, not an external-snapshot test. The existing historical failure remains valid history; this review does not invent its exact pre-repair exception or rewrite its cause.

The four new Java tests prove transport/body denial and a simple Unicode note, `.md` 8/2 outcomes/promotions, all-empty failures, and stale version 99 at admission. They do not exercise native forged output/closure/compact provenance, changed child/consent barriers, through-C writer races, interrupted connections or measured integration resource limits. Their purported lost-ACK test is an ordinary successful finalize followed by replay, explicitly insufficient under S3-13. Existing lost-ACK/second-JVM/privilege tests mainly cover v2/synthetic receipts. The newly authored Python unapproved-field test merely asserts that the constructed field list lacks `content`; it does not test rejection.

This review adds real native rollback/Java-ACK/mixed-authority and negative identity/vector observations, but does not turn them into complete packet-loss/crash/barrier/resource evidence. Actual v3 writer/seal order interleavings, unknown uncommitted connection/controller loss with unavailable reconciliation, exact same-file/child concurrency and cancellation at transfer/validation/staging boundaries, and independently observed integration worker+child system RAM/VRAM/CPU/temp disk/expansion remain incomplete. Accepted D17 producer-local resource receipts do not measure the Java/HTTP/PG integration. The historical full clean gate is not a new Slice 3 gate and was not rerun.

**Required evidence, not an automatic corrective cycle:** preserve failing evidence and distinguish implemented behavior from missing verification; supply exact source/input/output/receipt hashes and mandatory native observations. Retract the current claim that every matrix row is covered. Owner must explicitly route any further work; D24 grants zero additional remediation rounds.

## Atomic C_P, authorization and recovery assessment

The new ordering **`externalFileAuthority.release(...)` before `ext.promotePendingToSelected(...)` is not itself a finding**. Both execute inside the finalizer's same writable `TransactionTemplate`. Source-first guarded reservation locks are acquired before attempt/stage work. V18 release first verifies the exact token/union and updates the reserved row; PostgreSQL holds its row locks until the physical commit/rollback. Concurrent supported mutation uses `FOR UPDATE NOWAIT` and cannot observe an editable intermediate state. `entityManager.refresh(ext)` prevents reusing stale reservation state. No separate release transaction was found.

The reviewer wrapped only this production finalizer's transaction manager in the disposable test process, without altering repository source. Independent JDBC observations at physical commit were:

| Observation | Job | New G | Outcomes | Selected R | Reserved |
| --- | --- | --- | --- | --- | --- |
| Before actual delegate COMMIT | ACTIVE | 0 | 0 | absent | yes |
| After actual delegate COMMIT | COMPLETED | 1 | 1 | present | no |
| After injected Java ACK exception and durable replay | COMPLETED | 1 | 1 | present | no |

Exactly **one physical publication commit** occurred in that diagnostic. The exception was injected **after real PG commit**, not after a mocked callback; it is Java acknowledgement-loss evidence, not packet-level lost-connection certification. `requireRecovery` does not overwrite an already terminal durable state; replay reads outcomes and recovery forgets proven-stopped handles. Source inspection preserves no automatic resend/reparse. Full S3-13/14 requirements remain only partly proved.

A separate fresh native run installed a test-only **deferred commit trigger** in its owned database to raise SQLSTATE 53100 at physical commit. After failure, an independent connection observed **RECOVERY_REQUIRED / 0 G / 0 outcomes / no selected R / reservation retained**. This verifies rollback of the release/promotion sequence for that candidate; subsequent explicit cancellation safely cleaned it. It does not prove every prior-generation or crash scenario.

The correctly versioned capture command and normal revoke request between native execution and finalization both returned **P1701**, left authority APPROVED and reservation live, and permitted the still-authorized original attempt to finish. This is the frozen cancel-first contract, not a committed revoke being ignored. An earlier exploratory call used the deliberately denied unversioned capture overload; its result is retained but is not used as CAS evidence. No deterministic concurrent-child or late committed-authority-change proof is claimed.

Mixed actual **DOMAIN + EXTERNAL_FILE** execution published two roots and released both typed reservation sets. UUIDs were not converted into Vault owner IDs on batch reserve/release. The remaining UUID coercion defect is in fresh generation eligibility (S3-F02), and external dependency mutation remains incompatible (S3-F03).

## Complete Slice 3 D+F acceptance matrix

`PARTIAL` and `NOT_VERIFIED` do not mean accepted. This matrix assesses the complete mandatory row, not only its happy path.

| Gate | Independent disposition | Evidence / remaining limit |
| --- | --- | --- |
| S3-01 retained FR18-3 | **PARTIAL / not closed** | Original failure receipt retained; native DOMAIN now passes. Pre-repair stdout/cause and canonical actual-result comparison absent; S3-F01 persists. |
| S3-02 snapshot transport | **FAIL** | Java-managed S/R bytes and hash/length outside row guards; deleted-original Unicode positive and body denial pass. Wrong actual format and missing negotiated R tuple: F04/F05. |
| S3-03 real `.md` 8/2 | **PARTIAL** | Real 8 G/2 failed, promotions/releases and report pass. Complete output admission, published eligibility and source/output hash proof fail or remain unverified. |
| S3-04 all failed | **VERIFIED for exercised bounded case** | Two real empty `.md` roots produce ABORTED, zero G/promotions and full release. Missing/corrupt outputs are a separate F01 failure. |
| S3-05 text/completeness | **FAIL** | Runtime changed real text and deleted a real root; invalid publication occurred. |
| S3-06 locators/compact refs | **FAIL** | Unauthorized identity/revision/field, huge coordinates and fractional compact reference were published. |
| S3-07 exact child closure | **FAIL** | Approved native external edge fails SQL guard; name/R transport unproved. |
| S3-08 pipeline/vectors | **PARTIAL** | Real pinned model and finite 384-vector path; actual altered parser/383 dimensions/float overflow reject with zero publication and retained reservations. Full fingerprint/cardinality/malformed/PG-negative matrix not demonstrated. |
| S3-09 consent/CAS | **PARTIAL** | Stale admission and P1701 capture/revoke conflicts verified. Fresh eligibility fails; complete selected-R/child/concurrent-authority matrix absent. |
| S3-10 SQL provenance/roles | **FAIL** | Existing worker v2 role denials pass; runtime mutation of actual v3 stage publishes forged data. Full v3/PUBLIC/worker-negative proof incomplete. |
| S3-11 writer/seal | **PARTIAL** | Common source-first attempt fence/seal inspected; seven existing fence tests pass. Both deterministic real v3 staging-commit/seal orders not established. |
| S3-12 physical rollback | **PARTIAL; bounded native case PASS** | Deferred physical C_P failure rolls back G/outcomes/selection/release. Full prior-G/multi-root injected-failure observations absent. |
| S3-13 unknown committed | **PARTIAL** | Real PG C_P followed by injected Java ACK failure reconciles once; ordinary replay alone was insufficient. Actual connection-loss variant absent. |
| S3-14 unknown uncommitted | **NOT_VERIFIED for full v3 requirement** | Retained generic restart/recovery tests and failed-commit probe are useful; actual native MD connection/controller loss and unavailable reconciliation are not proved. |
| S3-15 cancellation/resources | **PARTIAL / integration profile NOT_VERIFIED** | Existing process/cancel tests and unchanged producer limits retained. Required v3 boundary cancellations and independently measured integration resource/expansion evidence absent. |
| S3-16 compatibility/static/final boundary | **PARTIAL overall; executed checks PASS** | 91 focused Java + 37 architecture + 16 process/fence; 124 Python; static/dependency/lock checks pass. Mandatory evidence gaps above prevent S3-VERIFY acceptance. No full-gate claim. |

**FR18-3 disposition: OPEN / acceptance blocked.** Matching version constants fixes one compatibility obstacle. It does not authenticate actual worker output or prove source-backed Java/PG admission. The independent forged-publication counterexample directly defeats closure.

## Pipeline identity and retained producer acceptance

The Java map now matches actual `embeddings/model.py:22–24`: parser `ppv-parser-v18.6-d17-html-container-boundaries`, chunker `structure-first-v18.5-compact-source-context`, and OCR identity `rapidocr-1.4.4-en-hans-script-admission-v18.2`. **That OCR string is legitimate existing fingerprint compatibility metadata**, explicitly permitted by the handoff. `worker.py` constructs the OCR engine only in its PDF branch; Markdown does not initialize RapidOCR. The selected Python 3.14.4 environment has no `rapidocr_onnxruntime` import specification. No OCR dependency was installed or experiment run.

Independently hashed the actual ONNX artifact **`98a01d88b7de996cdea58c32ca71208c09968d143798814b2ea09d3439dc334f`** and tokenizer **`2c3387be76557bd40970cec13153b3bbf80407865484b209e655e5e4729076b8`**. Python validates these on initialization. Java's exact tuple/provider and numeric finite-float checks are useful and retained, but self-reported metadata is not actual-result provenance (F01).

All 34 entries of the inherited-source comparison still match the recorded published hashes. The 31 matches to the old selective projection and three known mixed shared-file differences remain honestly distinguished; production Python, lock/config, D17 parser/chunker/model and fixtures did not change during Slice 3. The unchanged accepted D17/D20/D23 producer-local reviews and bounded resource/corpus/quality findings are reused. **D17-R01–R05 remain CLOSED and MD17-01–12 remain VERIFIED within producer-local acceptance.** New Java transport/admission defects do not reopen those findings or certify excluded historical adapters. No repeated million-character benchmark was run.

## Executed checks and exact receipts

All commands below ran from repository root with `scripts/run-bounded-command.py`, exclusive receipt/log paths, selected `.venv-d17`, the pinned local model and ordinary watchdog limits. Full absolute argv, wrapper invocation, cwd, timeout, exit and SHA-256 are in the evidence JSON. The tables describe only these bounded selections.

| Receipt in `logs/phase18/s3-independent-20261021/` | Result |
| --- | --- |
| `java-focused.json` | **91 / 0 failures / 0 errors / 0 skips**, exit 0, 317.612s; exact implementer Java selection reproduced. |
| `architecture.json` | `ApplicationArchitectureTests`: **37/0/0/0**, exit 0; Spring Modulith/ArchUnit boundaries. |
| `worker-fence.json` | `RagNativeWorkerProcessTest` 9 + `RagStagingCommitFenceIntegrationTest` 7: **16/0/0/0**, exit 0. |
| `python-focused-corrected.json` | Exact seven-file handback selection: **124 passed**, exit 0, pytest 32.05s. |
| `mypy.json` | Fresh previously absent cache: **24 source files**, exit 0. |
| `ruff.json` / `format.json` | PASS; formatter checked **75 files**, exit 0. |
| `dependency.json` / `lock.json` | `uv pip check`: **67 installed packages compatible**, Python 3.14.4; offline lock check **73 resolved**, both exit 0. |
| `probe-native.json` | Five reviewer observations; process exit 0. Four confirmed wrong acceptance/eligibility outcomes and one legitimate child-edge SQL rejection. **Not an acceptance PASS.** |
| `boundary-native.json` | Four reviewer observations: physical C_P/Java ACK exception, deferred rollback, exploratory reservation actions, actual mixed authority. Process exit 0; limitations stated above. |
| `admission-native.json` | Four reviewer observations: wrong parser identity, wrong dimension and float overflow safely rejected; correct versioned capture/revoke busy conflicts. Process exit 0. |
| `diff-check.json` | `git diff --check`, exit 0; existing CRLF-to-LF advisories recorded, no normalization performed. |

The Java 91 selection is:

```text
mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=RagNativeDocumentBatchIntegrationTest,RagDocumentBatchIntegrationTest,RagDocumentInputCompatibilityTest,RagWorkerPostgresPrivilegeIntegrationTest,RagCommitAcknowledgementRecoveryIntegrationTest,RagSecondJvmRecoveryIntegrationTest,RagCancellationRecoveryIntegrationTest,RagSlice1SnapshotIntegrationTest,RagSnapshotCorrectiveIntegrationTest,RagSlice3*Test test
```

Additional Java commands use the same flags with `-Dtest=ApplicationArchitectureTests` and `-Dtest=RagNativeWorkerProcessTest,RagStagingCommitFenceIntegrationTest` respectively. Python/static commands (substitute the absolute selected interpreter and this new evidence directory exactly as recorded in JSON):

```text
python -B -m pytest rag-engine/tests/test_obsidian_d17.py rag-engine/tests/test_d17_completion.py rag-engine/tests/test_d17_final_corrective.py rag-engine/tests/test_d17_r03_only.py rag-engine/tests/test_optional_ocr_dependency.py rag-engine/tests/test_worker.py rag-engine/tests/test_slice3_verified_indexing.py -q -o cache_dir=<new-evidence>/pytest-cache --basetemp <new-evidence>/pytest-temp
python -B -m mypy --config-file rag-engine/pyproject.toml --cache-dir <new-evidence>/fresh-mypy rag-engine/src
python -B -m ruff check rag-engine
python -B -m ruff format --check rag-engine
uv pip check --python <selected-python>
uv lock --directory rag-engine --check --offline
git diff --check
```

Reviewer-only Java diagnostics are `S3ReviewProbe.java`, `S3BoundaryProbe.java`, `S3AdmissionProbe.java` in the new ignored evidence directory, compiled with the existing Maven test classpath and launched using the hash-recorded Java argfiles. They reuse the real Spring application, selected Python, actual pinned ONNX model, restricted native worker SQL and fresh PostgreSQL 18.6/pgvector 0.8.6 containers. The evidence records exact compilation/launch commands and sources. Their fault DML uses `rag_runtime`; only the disposable deferred-trigger setup and independent observer use test administrator connections. Owned contexts/pools/containers were closed; watchdogs recorded no required process-tree termination.

One reviewer command initially named nonexistent Python test files and exited 4 without running tests (`python-focused.json`). It is preserved. The corrected selection above is the actual 124-test run. The initial compile receipt and the final source-matching compile are both retained. No failed receipt is relabelled PASS. Existing Mockito dynamic-agent/CDS warnings are recorded, not suppressed; this is not an IDE warning-free claim.

## Architecture, maintainability and 29-dimension coverage

Recursive inspection inventories **108 RAG production Java files, 42 RAG test files, 24 Python production files and 66 Python test/fixture files**, excluding bytecode. Existing module/API/source/internal/domain/persistence/storage/client/web package ownership remains. Changed behavior reuses existing authority, repositories, grants, worker supervisor and transaction templates. No new module, interface hierarchy, job engine or dependency was introduced. Shared root `ApiResponse` and module-local web advice remain consistent with ADR-0016; no arbitrary `common` module is requested. Existing flat application helpers have concrete roles; correctness fixes, not speculative structural refactors, are required.

| # | Dimension | Disposition / evidence |
| --- | --- | --- |
| 1 | Intent/scope | **FINDING** F04; D24 authority itself valid. |
| 2 | Requirement traceability | **FINDING** F01–F06 versus mandatory matrix. |
| 3 | Architecture/contracts | **FINDING** snapshot protocol/authority gaps F05; module baseline retained. |
| 4 | Ownership/dependencies | **PASS** public Vault/Film/Fiction APIs; 37 architecture checks. |
| 5 | Recursive package structure | **PASS** actual inventories above; no new layout/visibility drift. |
| 6 | Shared contracts/exceptions | **PASS** unchanged ADR-0016 envelope and module-local adapters. |
| 7 | SOLID/cohesion | **FINDING** validator lacks its source context, F01; no speculative abstraction request. |
| 8 | DRY/KISS/invariants | **FINDING** result/selection invariants F01/F02; mechanisms reused. |
| 9 | Patterns | **PASS** existing facade/repository/adapter composition reused. |
| 10 | Domain/status correctness | **FINDING** missing-result partial success and unavailable new G. |
| 11 | Wire compatibility | **FINDING** F05; exercised v2/DOMAIN compatibility passes. |
| 12 | Input/format/locator validation | **FINDING** forged coordinates/refs and format alias, F01/F04. |
| 13 | Authorization/least privilege | **FINDING** runtime-forged output and missing child path; positive typed reserves retained. |
| 14 | Credentials/privacy/offline | **PASS within inspected paths** no new secret/payload logging or outbound service; synthetic diagnostics only. No OS sandbox claim. |
| 15 | Transactions/concurrency | **FINDING** coherent publication eligibility fails; bounded physical atomicity/rollback verified, wider matrix incomplete. |
| 16 | Recovery/cancellation | **NOT_VERIFIED completely** useful native ACK/rollback and existing cancellation/restart tests; F06 limits. |
| 17 | DB/migrations | **FINDING** V14 dependency guard/V18 staging permissions; migrations unchanged through V21. |
| 18 | Complexity/resources | **NOT_VERIFIED for integrated profile** finite checks inspected; required actual integration measurements absent. |
| 19 | Index/provenance quality | **FINDING** forged published source/text, F01; retrieval/reranking outside scope. |
| 20 | Markdown/multilingual fidelity | **FINDING in integration** approved transclusion blocked; accepted D17 producer fidelity retained. OCR/Office/PDF N/A under D24. |
| 21 | Security tests | **FINDING** new native adversarial failures; v2 privilege tests are not complete v3 proof. |
| 22 | Test quality | **FINDING** misleading DTO/replay assertions and missing risk coverage, F06. |
| 23 | Benchmarks | **NOT_VERIFIED for new integration resources** unchanged D17 bounded evidence reused, no universal performance claim. |
| 24 | Independent quality evidence | **PASS for reuse only** D20/D23 accepted D17 evidence; no fabricated human labels or broader quality acceptance. |
| 25 | Dependencies/model | **PASS within selected runtime** lock/pip integrity, pinned actual artifacts, optional OCR absent. No new vulnerability audit claimed. |
| 26 | Build/static/tooling | **PASS for executed selections** 144 Java and 124 Python tests, Ruff/format/fresh mypy; full P18-FINAL deferred. |
| 27 | Logging/observability | **FINDING** F06 receipts/measurements incomplete; generic pre-commit validation exceptions are also logged as physical COMMIT failures. No sensitive-payload log found in changed code. |
| 28 | Documentation/governance | **FINDING** handback/ACTIVE all-matrix coverage assertion disproved; original submission preserved. D24 prospective boundaries consistent. |
| 29 | Hygiene/delivery | **PASS for review preservation** index/source/history checks; no cache/model staged. Candidate remains blocked for delivery. |

## Exact inventory, preservation and Owner delivery recommendation

The full 44-path inventory is linked above; all entries have raw byte hashes and line-ending counts. Seven changed production candidates are under `rag/internal/`:

- `application/RagDocumentBatchService.java`
- `application/RagDocumentFinalizationGate.java`
- `application/RagDocumentInput.java`
- `application/RagManifestScope.java`
- `application/RagSourceAuthority.java`
- `application/RagSourceService.java`
- `domain/RagExternalFileSource.java`

The six test paths are the four modified Java fixtures/tests (`AbstractEnabledRagIntegrationTest`, `RagDocumentBatchIntegrationTest`, `RagDocumentInputCompatibilityTest`, `RagNativeWorkerProcessTest`), new `RagSlice3VerifiedIndexingIntegrationTest.java` and new `rag-engine/tests/test_slice3_verified_indexing.py`. No new production Python, fixture, dependency, lock or migration change was introduced. The other 31 paths are D24 preparation/governance and handback/ACTIVE records, individually classified in the inventory. Published excluded implementation remains unaccepted and is not certified by this review.

All **2,474 entry nonignored files** were hash-snapshotted before verification; production/tests/dependencies/fixtures and prior reviews remained byte-identical during review. Only ACTIVE receives the review-status update, with new review/evidence/inventory documents added. All **24 implementer receipt assets** remain unchanged. The **272 pre-existing Surefire files** were archived before Maven ran and individually verified against entry hashes; fresh selected reports are archived separately. The old Slice 2 ACTIVE archive also matches its published HEAD blob byte-for-byte. Existing CRLF bytes were not normalized. The actual index semantic hash and HEAD remain unchanged. Machine evidence states the precise checked set; it does not claim to recover or certify all ignored historical captures.

**Do not stage or commit this candidate on this review.** No commit message or READY_FOR_OWNER_COMMIT is issued. Retain exact candidate and diagnostic hashes for Owner routing. The proposed delivery inventory is an identification of reviewed dirty files, not authorization to publish them; no selective index was constructed or modified.

## Retained lifecycle boundaries and next Owner action

Phase 17 COMPLETE/FROZEN; Slice 1/D14 CLOSED; Slice 2/D23 Markdown producer-local CLOSED; D17/D20 accepted; D22's three original captures **NOT_RECOVERED / original preservation FAILED**. FR18-3 remains OPEN and Java identity admission/trusted publication remain NOT ACCEPTED. External indexing stays INACTIVE and live originals DISABLED/DEFERRED. TXT/DOCX/PDF/XLS/XLSX/OCR remain excluded from accepted functionality under D24, with unwanted-format cleanup required once at Slice 4 end before P18-FINAL.

The historical full Java gate remains **FAILED — 1,433 tests / 1 failure / 2 errors / 0 skips**. Focused successes do not replace it. Slice 4 must separately address final NO_CHANGE, same-R Reindex API, complete lifecycle/purge/restore/reporting, consolidated cleanup, comprehensive resources/GPU fallback, aggregate PostgreSQL connection-resource investigation and the five-command P18-FINAL. None is started or accepted here; current Slice 3 publication-local eligibility/recovery requirements cannot be deferred into that list.

**Next Owner action:** review S3-F01–F06 and explicitly route the blocked Slice 3 work. Historical Antigravity remediation remains 2/2 exhausted and D24 grants zero additional remediation rounds. This review starts or authorizes no further cycle, takeover or Slice 4. Overall Phase 18 and P18-FINAL remain NOT ACCEPTED. Stop after this independent handback.
