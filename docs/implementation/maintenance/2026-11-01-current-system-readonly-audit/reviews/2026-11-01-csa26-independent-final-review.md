# Independent slice review — CSA26-AUDIT-REMEDIATION

- **Date / reviewer:** 2026-11-01 / independent Codex; initial review of Antigravity's implementation.
- **Mode:** SLICE, explicitly approved maintenance implementation.
- **Baseline:** main at 04528f76c32380f07377f470e1920a31583d690d; entry index empty, 33 modified tracked paths and three untracked handoff/handback records. This is an uncommitted working-tree review, not Owner publication.
- **Authority:** [Owner selection](../owner-decision-csa26-selected-remediation-2026-11-01.md), [scope](../remediation-scope.md), [executable handoff](../executable-handoff-2026-11-01.md), and [ACTIVE](../../../handoffs/ACTIVE.md).
- **Status:** **CHANGES_REQUESTED**. Two blocking defects in the mandatory diagram deliverable; no production-code or test-isolation blocker found.
- **Counter:** maintenance remediation **0/3 consumed**; initial review does not consume a corrective round. Next submission is correction round 1. Takeover NOT_AUTHORIZED; historical S19-2 counters unchanged.
- **Independent evidence:** logs/csa26-maintenance/codex-review-20261101-130152-32afc6f9/. Ignored receipts/screenshots remain local; no historical receipt was overwritten.

## User requirement and simplest-mechanism check

Standard class-level Jupiter conditions and pre-import pytest module skips satisfy the selected isolation requirement without a new runner, dependency, plugin or production change. Eight Java classes and five Python modules are guarded; the archive MVC tests remain discoverable. Import changes preserve test bodies. Current phase/status guidance is corrected in the nine approved entry points.

The diagram delivery does not yet satisfy A07/A08. Missing formal Graphviz/Structurizr tooling is an explicitly permitted limitation; it does not waive synchronized exports, visible required topology, or legibility. These findings require finishing existing views, not introducing new architecture or diagram types.

## Finding register

| ID | Severity / blocking | Concrete evidence and consequence | Minimum correction / proof | Status |
| --- | --- | --- | --- | --- |
| **CSA26-FR-01** | **MEDIUM / YES** — A07 mandatory deliverable | Container DOT has nine nodes/nine edges, including snapshots and queryWorker; current container.svg has seven nodes/seven edges and neither new node. container.png is unchanged from HEAD and visibly retains the synthetic-digest-only / Phase 17 candidate worker. The SVG merely changes labels in the old geometry: frontend/API/database text extends outside its shapes and becomes unreadable against the white background. Its database edge still describes the separate migrator rather than Java finalization. Neither current container source includes the required suspended-QA distinction or a clear output-completion/positive-stop reader-lifetime note. Thus the source, review exports and handback PASS claims disagree on accepted topology and status. Locations: docs/architecture/diagrams/source/container.dot:11–14,24–27; docs/architecture/structurizr/workspace.dsl; docs/architecture/diagrams/exported/container.svg:35–36,43–48,55–57,79–81,104–105; container.png. | Finish the existing container representation: approved snapshot artifact, issued inputs, restricted staging/sealing, Java finalization, separate request-bound query process, and a compact accurate reader/output lifetime annotation. Distinguish suspended/unaccepted QA with a label or legend; it must not look like an accepted runtime. Synchronize both container SVG and PNG with the canonical sources and lay out all labels legibly. Retain the accepted trust boundary; no new runtime, sequence view, download or architecture change. Supply an actual source-to-export mapping and inspect both renders. | **OPEN** |
| **CSA26-FR-02** | **MEDIUM / YES** — A08 mandatory deliverable | Both module PNG aliases show rag as a disconnected box. The DOT, draw.io XML and both SVG aliases contain rag → vault, rag → fiction and rag → film; the SVG graph contains all 20 nodes/41 edges. The PNG aliases omit those three arrows, with several other edges crossing labels. Alias hashes agree within each format, but the formats convey different dependency graphs. The handback also claims draw.io-generated SVGs although the delivered SVG retains the Graphviz-generated representation with only the RAG label edited. Locations: docs/architecture/diagrams/exported/module-dependencies.png and module-dependency-overview.png; source/module-dependencies.drawio cells rag-vault, rag-fiction, rag-film; SVG edge titles rag->vault, rag->fiction, rag->film. | Produce legible SVG/PNG exports from the approved module sources with the same 20-module/41-edge meaning, including all three RAG arrows; synchronize both alias pairs. Preserve allowed dependencies and portability's no-Java-edge/read-only JDBC exception. Correct the handback's actual generator/manual-edit provenance. Visually verify every format/alias; XML edge counts alone do not prove the PNG contains the arrows. | **OPEN** |

Evidence for both findings: diagram-source-export-check.json, container-svg-render.png, module-svg-render.png in the independent run; direct visual inspection of the delivered container.png and module-dependencies.png. Binary alias equality makes the same PNG observation applicable to module-dependency-overview.png. These are failures of selected acceptance requirements, even though original CSA26-003 was LOW.

## Original audit finding disposition

| Finding | Independent disposition |
| --- | --- |
| CSA26-001 | **CLOSED** for test isolation only. A01–A04 proof below; no generated-answer acceptance or QA execution permission. |
| CSA26-002 | **CLOSED** for the nine current status/setup entry points, A05/A06. Diagram content/export claims remain under CSA26-003. |
| CSA26-003 | **OPEN / PARTIALLY IMPLEMENTED**; blocked by CSA26-FR-01/02. |
| CSA26-004 | **CLOSED**; named-four and full applicable Ruff checks and formatting PASS. |

The original audit, Owner decisions and preparation review are preserved as point-in-time history, not rewritten to close findings.

## Acceptance matrix

| Case | Result | Independent evidence / limit |
| --- | --- | --- |
| A01 | **PASS** | Eight exact class selections: opt-in absent, explicitly false, and absent with nonempty legacy evaluation/CUDA flags; each selection reports 32 skipped, zero errors/failures. JVM class-initialization logs show discovery/verification but zero initialization of any of the eight archived classes. The harmless inherited QueryCountInvocation extension initializes; no archived test class, fixture runtime or Spring application initializes. |
| A02 | **PASS** | Import tripwire installed before pytest. Direct selection absent/false: all five modules skip, zero items, expected pytest exit 5; enclosing diagnostics exit 0. Ordinary collection from rag-engine absent/false: 242 accepted items collected, five QA modules skipped, pytest exit 0. All four modes have zero attempted/loaded grounded_qa, qa_generation or llama_cpp imports and zero collection errors. |
| A03 | **PASS** | Fresh RagQaArchiveGuardTest 3/3 plus RagAuthorityContractTest 3/3, no failures/errors/skips. Exactly the six authorized synthetic Python modules: 42 passed. |
| A04 | **PASS, finite scope** | Reviewer before/after manifests: 131 identified fixture/control/writer paths unchanged, including absent fixed writer targets; two QA runtime-lock files match HEAD. QA process observations before/after: zero matching native QA processes. Guard/init/import proof excludes the selected native/model/evaluation paths. No claim to rehash every ignored historical log or prove a model-wide runtime property. |
| A05 | **PASS** | Nine current entry points agree on P17 frozen, P18 closed, S19-1 accepted/published, S19-2 suspended/archival only, inactive indexing and disabled live originals. Current maintenance status is synchronized with this review; historical banners remain historical. |
| A06 | **PASS** | Supported interpreter exists, reports CPython 3.14.4; no llama-cpp-python installed. Bounded runner, config and six test paths exist and execute successfully. Superseded OCR/setup prose remains inside explicitly preserved history; no provisioning was executed. |
| A07 | **FAIL** | CSA26-FR-01. XML validity and partial source changes do not establish synchronized or legible container exports. |
| A08 | **FAIL** | CSA26-FR-02. 20-node/41-edge source/SVG inventories pass; required PNG semantics and legibility fail. |
| A09 | **PASS** | Four-file Ruff check, full src/tests/scripts Ruff check and format check all exit 0; 120 files already formatted. No new blanket suppression; E402 exceptions are line-local behind necessary module guards. |
| A10 | **FAIL / correction required** | Path scope, no publishing, self-review presence and 0/3 counter pass. Submitted A07/A08 PASS/provenance claims require correction with the actual completed diagram work. This review supersedes the implementer readiness claim. |

## Verification

All potentially long checks used scripts/run-bounded-command.py, normal profile: Maven 300s maximum, Python/static/render 120s maximum; Maven offline. All successful gate receipts report exit 0, complete capture and cleanup not_needed. No full suite, DB/native worker, QA inference, GPU probe, model load, runtime provisioning or download was requested or executed.

Environment preflight rejected inherited Java/Maven/pytest opt-ins/options and found none, found no Jupiter condition-deactivation config, and kept test-scoped execution false outside accepted fixtures. Pytest plugin autoload and bytecode/cache output were disabled; temporary test output was confined to the new run. Recursive source inventories cover all 74 files in the relevant Java RAG test subtree and 119 non-cache files in the Python test subtree; tests/conftest, inheritance, top-level initialization, runner settings, fixed-name writers, fixtures and guard ordering were inspected. The 14 changed test file hashes are retained in reviewed-test-hashes.json.

Exact gate invocations, from repository root (R = the independent run directory above; J = the eight classes listed in the executable handoff):

~~~powershell
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R/java-absent.json -- mvn.cmd -o -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED -Xlog:class+init=info:file=../R/java-absent-init.log' '-Dtest=J' test
# java-false: same eight classes, -Dppv.test.archived-s19-2=false; separate receipt/init log.
# java-legacy: archive property absent, -Dppv.rag.qa.eval.run=dummy -Dppv.rag.qa.cuda.launcher=dummy; separate receipt/init log.
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R/java-focused.json -- mvn.cmd -o -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dppv.test.archived-s19-2=false' '-Dtest=RagQaArchiveGuardTest,RagAuthorityContractTest' test
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt R/python-focused.json -- rag-engine/.venv-d17/Scripts/python.exe -B -m pytest -c rag-engine/pyproject.toml -p no:cacheprovider --basetemp R/pytest-focused-temp rag-engine/tests/test_contracts.py rag-engine/tests/test_health.py rag-engine/tests/test_bound_ingress.py rag-engine/tests/test_transport.py rag-engine/tests/test_chunker.py rag-engine/tests/test_parsers.py -q
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt R/ruff-four.json -- rag-engine/.venv-d17/Scripts/python.exe -B -m ruff check --no-cache rag-engine/tests/test_grounded_qa.py rag-engine/tests/test_qa_model_paths.py rag-engine/tests/test_qa_runtime_profile.py rag-engine/tests/test_query_embedding.py
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt R/ruff-full.json -- rag-engine/.venv-d17/Scripts/python.exe -B -m ruff check --no-cache rag-engine/src rag-engine/tests rag-engine/scripts
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt R/ruff-format.json -- rag-engine/.venv-d17/Scripts/python.exe -B -m ruff format --check --no-cache rag-engine/src rag-engine/tests rag-engine/scripts
~~~

- JVM initialization proof: java-absent-init.log, java-false-init.log and java-legacy-init.log, corresponding stdout and JSON receipts; 32 skipped each. Wall times 7.078s / 5.532s / 6.031s.
- Tripwire commands use the same 120s bounded prefix and project interpreter with run-local review_diagnostics_v2.py tripwire direct-absent/direct-false, then review_diagnostics_v3.py tripwire default-absent/default-false. Version 3 changes to the project directory for default discovery. Per-mode JSON contains all five observed skips, item count, exit and blocked-import observations. No archived opt-in was enabled.
- Focused Java receipt: 9.078s wrapper time, 6 passed. Focused Python: 1.531s wrapper time, 42 passed in 0.21s.
- Ruff 0.17.0, pytest 9.1.1, FastAPI 0.143.0, CPython 3.14.4. Supplied implementation logs also show Maven Surefire 3.5.6. No dependency changed.
- **Failed reviewer diagnostics retained:** the first direct-selection diagnostic incorrectly compared root-relative skip IDs (pytest exit 5, no QA imports); its assertion failed. The first ordinary-collection diagnostic used repository-root cwd and scanned ignored saved copies, causing collection errors (pytest exit 2, zero blocked QA imports). Both failed receipts remain intact; corrected project-scoped diagnostics provide the passing evidence. These diagnostic errors are not implementation findings. A version-metadata one-liner also had a shell-quoting error; the corrected stdin probe supplies metadata.
- Render inspection used installed Edge 156.0.4314.58 headless, isolated run-local profiles and local HTML embedding the delivered SVGs; no tracked export was changed by review. Delivered PNGs were inspected directly. Installed draw.io is 26.1.1. Graphviz/Structurizr CLIs are unavailable: formal DOT/DSL semantic validation **NOT_VERIFIED, explicitly permitted tooling limitation**. Draw.io/SVG XML parsing passes; rendering exposes the concrete failures above.
- Supplied Java logs retain existing deprecation/unchecked warnings and Mockito/JDK agent/CDS warnings. They were reviewed; no IDE inspection, warning-free claim, dependency repair or blanket suppression is made. Mypy remains NOT_RUN as allowed by the handoff.
- Scoped local Markdown path check: 301 relative paths, zero missing. Nine file-URI links in the implementer handback are local-machine references rather than repository-relative navigation; this is nonblocking portability hygiene, not a new acceptance project.
- Git whitespace check PASS. No staged paths; production, dependencies, migrations/DBML, frozen acceptance records and archived bodies/fixtures are unchanged.
- Final saved-file checkpoint: 320 relative Markdown paths resolve, all 14 reviewed test hashes remain identical, ACTIVE/contract/handback read back CHANGES_REQUESTED with Antigravity routing and 0/3 consumed, and the index remains empty. Receipt: final-review-check.json. The native command hook rejected one long shell edit as ambiguous quoting; structured file patches and a plain script invocation saved the same authorized review/status edits without changing the hook or its policy.

## Risk-applicable shared coverage ledger

Every shared dimension is accounted for below; unchanged capabilities are not subjected to new suites.

| Dimensions | Result | Evidence / rationale |
| --- | --- | --- |
| 1–2, 10 | **FINDING** | Approved acceptance trace A01–A10 above; selected diagram deliverable remains incomplete. Isolation's missing/false/direct/legacy boundaries pass. |
| 3–4 | **FINDING** for representations; **PASS** for preserved ownership | Container/module findings. Module matrix and rag/package-info.java retain vault::entry, fiction::source, film::source; source DOT/draw.io/SVG have the same 41 allowed edges, with no reverse edge or new module. |
| 5, 7–9 | **PASS** | Actual recursive changed-folder inventories, full changed-test diff, guard/import ordering and inheritance inspected. No added/moved command/view/enum/interface/exception package, new abstraction or production coupling. Reused framework guards are proportionate. |
| 6, 11, 15, 17 | **N/A to changes; baseline checked** | No HTTP/persistence/transaction edit. Root ApiResponse record and ADR-0016 explicitly agree on root shared envelopes and module-local adapters; no common module is needed. No JPA/schema/lock behavior is changed. |
| 12–14, 16, 18, 21–22, 26–27 | **PASS for changed test boundary** | Literal true-only opt-ins deny missing/false/nonmatching values; class-init/import proof, bounded checks, finite preservation, no private content/secret output, ordinary MVC route guard and synthetic tests. No new growing loop, hot-path work, custom logging or recovery behavior; permitted formal-validator limitation recorded separately. |
| 19–20, 23–25 | **N/A** | No enabled ingestion, parsing/OCR, retrieval/answer-quality, model, benchmark/evaluation or dependency change. No new live-file, source-commit, local worker/security integration proof is required for this maintenance. Prior source/consent/reader guarantees remain frozen. |
| 28 | **FINDING / otherwise PASS** | Nine current status/setup entry points and links pass; diagram truth and handback/provenance claims require correction. Scoped Docs Sync Check below. |
| 29 | **PASS** | Exact changed-path inventory, ignored receipts, no generated tracked cache/private/model files, unchanged fixtures/runtime locks, empty index and whitespace check. Alias duplication is explicitly required, not stale source duplication. |

Representation trace checked against actual unchanged authorities: RagOriginalBytesStore storage contract and ADR-0022/0023; RagWorkerSupervisor prepared-source preparation/locking and NativeWorkerProcess launch; RagDocumentFinalizationGate.finalizeDocumentBatch; Python worker.py/document_source.py and db.py restricted stage_document_result/seal_document_result; RagQueryEmbeddingSupervisor dynamic loopback query process and stop/transport completion; ADR-0025 reader exclusion through actual output and positive sender/child completion. No source/ADR authorizes Python business publication, a Windows OS sandbox, live-original activation or accepted QA.

## Scope-bounded Docs Sync Check

- **UPDATED — review/status only:** this review; ACTIVE; executable handoff; remediation-scope current disposition; implementation-handback current reviewer disposition; matching current maintenance status literals in AGENTS.md, README.md, docs/README.md, docs/workflow/current-owner-delivery-policy.md and rag-engine/README.md. Submitted implementer/self-review evidence remains identified as historical submission, not independently accepted.
- **ALREADY_CONSISTENT:** nine-document phase/setup corrections (including docs/roadmap.md, docs/architecture/README.md, docs/repository/repository-package-tree.md and ADR-0025 current note), accepted P17/P18/S19-1 versus suspended S19-2, actual baseline SHA and Owner-only publishing.
- **NOT_VERIFIED / FAIL:** A07/A08 completion and submitted diagram generator/visual PASS claims; correct under CSA26-FR-01/02 before readiness. The permitted missing formal validator is reported separately and does not itself block.
- **DEFERRED_OUT_OF_SCOPE:** historical/nested AGENTS/rule/skill/navigation drift listed in the handoff; old ten LOW backlog IDs and AUTH-CLOCK. No historical audit/decision/review/log rewrite, old backlog cleanup, resumed S19-2 or phase acceptance is authorized.
- **Publication:** no implementation publication or future SHA is claimed. Owner cannot commit this delivery as accepted until the two mandatory diagram failures are independently closed.

## Required correction and next action

Return only CSA26-FR-01/02 and affected handback/current-status evidence to Antigravity under the existing maintenance handoff. Preserve closed CSA26-001/002/004 and all receipts. Reuse these source-applicable isolation/regression/Ruff receipts if tests/config/runtime stay unchanged; rerun diagram source/export/visual, scoped docs/link and whitespace checks after correction. Run mandatory implementer self-review, save matching IMPLEMENTED_AWAITING_CODEX_REVIEW status in the current records, then obtain independent Codex re-review. No full suite, native QA, new tool installation, staging, publication, fourth round or automatic takeover.

Next step: Antigravity | /antigravity-implement-handoff, then /antigravity-self-review and independent $codex-final-review | CSA26-AUDIT-REMEDIATION; [ACTIVE](../../../handoffs/ACTIVE.md), [executable handoff](../executable-handoff-2026-11-01.md), this review | close CSA26-FR-01/02 and correct diagram/handback evidence; no commit yet | maintenance remediation 0/3 consumed, next correction 1/3; takeover NOT_AUTHORIZED.
