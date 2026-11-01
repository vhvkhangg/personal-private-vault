# Executable maintenance handoff — CSA26-001..004

- **Handoff ID:** `CSA26-AUDIT-REMEDIATION`
- **Created:** 2026-11-01 / Codex
- **Status:** `READY_FOR_OWNER_COMMIT`
- **Classification:** one Owner-approved combined maintenance slice; no numbered-phase implementation.
- **Implementer:** Antigravity, existing `backend-implementer`.
- **Reviewer:** independent Codex in a separate `$codex-final-review` session.
- **Entry:** local `main` and remote `refs/heads/main` both verified at `04528f76c32380f07377f470e1920a31583d690d`; index and working tree clean before handoff creation.
- **Preparation:** [Owner selection](owner-decision-csa26-selected-remediation-2026-11-01.md), [approved scope](remediation-scope.md), and [independent READY_FOR_HANDOFF review](reviews/2026-11-01-csa26-maintenance-pre-handoff-review.md), all included in that published preparation commit. Their publication-pending/next-action text records issuance history; publication was checked for this handoff.
- **Implementation / remediation:** `READY_FOR_OWNER_COMMIT` after independent re-review 2; CSA26-001/002/003/004 and CSA26-FR-01/02 CLOSED, CSA26-FR-L01 LOW/nonblocking. Maintenance corrective rounds **2/3 consumed**; accepted and stopped, one unused; takeover **NOT_AUTHORIZED**.

## Goal and sources of truth

Make ordinary verification avoid suspended QA initialization and evidence writers, correct the nine approved current documentation entry points and existing runtime/module diagram families, and remove the four reported import-order diagnostics. Only CSA26-001..004 are selected. Independent reviews CLOSED CSA26-001/002/003/004 and CSA26-FR-01/02. CSA26-FR-L01 remains LOW / OPEN / NONBLOCKING for a later authorized milestone/audit.

Read this contract with [ACTIVE](../../handoffs/ACTIVE.md), the three preparation artifacts above, [original audit](audit-report.md), [diagram inventory](architecture-diagram-gap-inventory.md), [Owner policy](../../../workflow/current-owner-delivery-policy.md), [Docs Sync contract](../../../workflow/documentation-synchronization.md), and [proportional delivery policy](../../../workflow/rag-phase-delivery-guardrails.md). The new maintenance decision controls over older prospective bans on maintenance or expired S19-2 execution routes.

Preserve [P17 closeout](../../phase-17/closeout.md), [P18 final closeout](../../phase-18/phase-18-final-owner-closeout-2026-10-25.md), [S19-1 accepted publication](../../phase-19/s19-1-owner-publication-closeout-2026-10-28.md), [S19-2 suspension](../../phase-19/owner-decision-s19-2-suspension-2026-10-30.md), and [source archive manifest](../../phase-19/s19-2-source-archive-manifest-2026-10-31.md). Source archive publication is not QA acceptance.

## User-visible acceptance — MUST

| Case / finding | Scenario / input | Required observable result | Minimal evidence / reuse |
| --- | --- | --- | --- |
| A01 / CSA26-001 | Ordinary Java discovery, or explicit selection of any of the eight archived integration classes, with the new archive opt-in absent or `false` | All eight are disabled before unsafe class initialization, Spring setup, native launch, corpus preparation or historical output writes. Existing CUDA/evaluation properties alone do not enable them. | Source/selection inventory plus safe runner evidence; use standard class-level Jupiter conditions and existing tests. |
| A02 / CSA26-001 | Default Python collection and direct path selection of any of the five QA-dependent modules, with archive opt-in absent or `false` | Each archived module skips before importing QA application/generator/native modules or running module-level initialization. Accepted tests remain collectable. | Pre-import guard inspection, collection/selection receipts with an import tripwire, absent archived imports and no model/worker. A post-import marker is insufficient. |
| A03 / CSA26-001 | Focused accepted-only verification after A01/A02 isolation preflight | All three existing `RagQaArchiveGuardTest` cases PASS; QA beans/routes absent, accepted retrieval routes preserved. `RagAuthorityContractTest` and the six named synthetic Python modules below PASS without optional QA installation. | Existing MVC registration/mock tests, three pure authority-contract cases, and audit's inspected Python selection. Record actual counts/skips, not a count target. |
| A04 / CSA26-001 | Before/after the finite isolation and regression checks | No native QA process, C1/C2 load, corpus evaluation, historical receipt modification or new historical evaluation output. Archived bodies, fixtures and control hashes remain intact apart from the expressly allowed guard/import edits. | Finite path/hash inventory of identified historical writer targets and fixture/control files, relevant process/import observations, test reports and diff. No claim to have rehashed every ignored log. |
| A05 / CSA26-002 | Read any of the nine approved current entry points | One clear current pointer agrees with ACTIVE: maintenance only; P17 frozen, P18 closed, S19-1 accepted/published, S19-2 suspended/not accepted/archival only, Phase 19 unfinished/on hold. Earlier dated banners are clearly historical. | Changed-document status/link check against the accepted closeouts and suspension. No invented acceptance or reset counters. |
| A06 / CSA26-002 | Follow the current Python setup/check instructions | Paths, supported project interpreter and commands exist; checks use the bounded runner and safe selection. Old removed OCR/parser tooling and unqualified QA/full-inference commands are not current instructions. Optional QA provisioning is not part of supported default verification. | Verify command/path references against actual project configuration and A01–A03 receipts. Do not execute provisioning to validate prose. |
| A07 / CSA26-003 | Inspect current C4 container sources and SVG/PNG | Show immutable ORIGINAL_BYTES as a storage artifact, Java-issued prepared inputs, restricted Python staging/sealing and Java finalization, separate request-bound S19-1 embedding process, and source-reader/output lifetime. Distinguish ACCEPTED capability, INACTIVE/DEFAULT_OFF operation, SUSPENDED/NOT_ACCEPTED QA and PROPOSED UI/integrations. | Source/ADR edge trace, available syntax checks, synchronized exports and actual rendered visual inspection. Reuse the existing views; no new diagram platform. |
| A08 / CSA26-003 | Inspect module DOT/draw.io and both exported aliases | Replace stale RAG candidate delivery labels; preserve the 20-module ownership/dependency meaning and portability exception. Both module SVG/PNG alias pairs agree. Accepted retrieval must not imply accepted generated answers. | Compare source/module matrix, editable files and rendered exports. Keep allowed dependencies distinct from observed runtime calls. |
| A09 / CSA26-004 | Ruff on the four specified files, then the applicable existing source/test/script set | Four I001 violations resolved; full applicable Ruff check and format check PASS with guard edits included. Original assertions and archival labels preserved. | Existing pinned Ruff, no QA imports/inference and no broad ignore or unrelated auto-fix. |
| A10 / all | Completed implementation and handback | Only allowed paths changed; four findings addressed with receipts, limitations explicit, mandatory self-review complete, saved ACTIVE/contract/handback agree on review-ready status and 0/3 initial correction count. | Diff/preservation/link checks and saved-file read-back; independent Codex review remains pending. |

## Simplicity decision

| Decision | Selected implementation / burden |
| --- | --- |
| KEEP / REUSE | Existing Jupiter conditions, pytest module skips, archive MVC tests, inspected synthetic tests, watchdog, current DSL/DOT/draw.io and local export facilities. Small test/lint changes; moderate isolation and diagram verification. |
| SIMPLIFY | One test-only opt-in per language; local guards rather than a new plugin/agent/job framework. One compact current notice per approved entry document. No new production code, dependency or schema is needed by the selected plan. |
| DEFER | New retrieval sequence, snapshot-flow or ER diagrams, interactive Archify, full-suite/native/DB evaluation, ten older LOW IDs and AUTH-CLOCK. None becomes a closure condition here. |
| REJECT | Model/runtime repair or installation, historical writer repair, generic test framework, directory renaming, provider fetchers, additional agents/hooks/CI and new product features. |

## Precise change map and shortest implementation path

Repository-root path abbreviations below: **T** = `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/`; **P** = `rag-engine/tests/`; **D** = `docs/architecture/`. Inventory the complete relevant test subtree before editing; do not stop at the audit's original two examples.

### Test isolation and lint

1. In **T**, apply a class-level, test-only `@EnabledIfSystemProperty(named = "ppv.test.archived-s19-2", matches = "true")` to each of:
   - `RagQaNativeIntegrationTest.java`
   - `RagQaEvaluationNativeIntegrationTest.java`
   - `RagQaCudaNativeIntegrationTest.java`
   - `RagQaNativeSafetyIntegrationTest.java`
   - `RagQaProspectiveEvaluationNativeIntegrationTest.java`
   - `RagQaIntegrationTest.java`
   - `RagQaLifecycleIntegrationTest.java`
   - `RagQaSenderIntegrationTest.java`

   Preserve existing CUDA/evaluation restrictions and class inheritance. Verify the combined conditions reject before static initialization, especially prospective evaluation's `RUN`/`OWNER` fields. Do not move/repair those initializers or fixed-name writers. Missing, `false` or other nonmatching values must deny. The new property is a test isolation boundary, not current permission to run archived tests; **do not set it to `true`** in this maintenance.

2. In **P**, add a module-level `pytest.skip(..., allow_module_level=True)` unless `PPV_TEST_ARCHIVED_S19_2` is exactly `true`, **before any QA application/generator import or initializer**, in:
   - `test_grounded_qa.py`
   - `test_qa_generation.py`
   - `test_qa_model_paths.py`
   - `test_qa_runtime_profile.py`
   - `test_qa_cuda_profile.py`

   Keep future imports first and standard-library/pytest imports safe. No QA service initialization may precede the guard. Direct file selection must retain the guard. Narrow line-local E402 annotations for imports necessarily behind the guard are permitted if needed and explained; no blanket lint/type suppression. Do not enable the new environment opt-in.

3. Preserve ordinary discovery of **T** `RagQaArchiveGuardTest.java` and its three assertions. `RagQaBehaviorTest`, `RagQaCitationTest` and nested `internal/application/qa/RagQaProtocolTest` are pure helper/protocol tests, not native integration; do not blanket-disable every `Qa` name or treat them as generated-answer acceptance. Their execution/refactoring is not a mandatory gate here.

4. Fix I001 import ordering only in **P** `test_grounded_qa.py`, `test_qa_model_paths.py`, `test_qa_runtime_profile.py`, `test_query_embedding.py`. Guard-related edits in the five modules above are separately authorized. Preserve test logic, docstrings and archival labels; no fixture edits.

5. One narrow, non-generative discovery proof/test or run-local diagnostic is allowed for A01/A02. Prefer the existing facilities. If class conditions cannot establish safe pre-initialization for an entry point, standard runner exclusion in `backend/pom.xml` or Python `conftest.py`/`pyproject.toml` is allowed **solely** for this isolation boundary. Explicit test selection must still fail closed; preserve the module/class guards. No new dependency, generalized plugin or default QA execution. Explain any fallback and its actual evidence in handback.

### Current documentation — expressly assigned to Antigravity

The Owner selected a combined maintenance slice. Antigravity may perform the bounded current-status/setup corrections in exactly:

- root `AGENTS.md` and root `README.md`;
- `docs/README.md` and `docs/roadmap.md`;
- `docs/workflow/current-owner-delivery-policy.md`;
- `docs/architecture/README.md`;
- `docs/repository/repository-package-tree.md`;
- `docs/adr/0025-phase-19-markdown-reader-exclusion.md`;
- `rag-engine/README.md`.

Prefer small additive current notices and explicit historical grouping. Correct stale present-tense summaries where needed to avoid contradictory current guidance. ADR-0025 gets a dated current acceptance pointer; its issued decision/history is not rewritten. The package-tree correction describes real existing ownership/status, with no folder/schema/module redesign. Preserve historical command/evidence records as history. Keep the dropped social-media downloading, fiction copying/backup and Hermes/OpenClaw features out of the roadmap; Ideaverse/Drive/frontend remain proposals, not implementation targets.

### Current architecture representations — expressly assigned to Antigravity

Mandatory editable targets are **D** `structurizr/workspace.dsl`, `diagrams/source/container.dot`, `diagrams/source/module-dependencies.dot` and `diagrams/source/module-dependencies.drawio`. Synchronize **D** `diagrams/exported/{container,module-dependencies,module-dependency-overview}.{svg,png}`. Keep both module aliases; no rename/deletion is approved.

Trace changes using the diagram inventory and these read-only authorities:

- `RagOriginalBytesStore`, ADR-0022/0023 and the accepted P18 closeout: immutable approved snapshots; same-Windows-user/application trust, not an OS sandbox or live-original reader.
- Java `RagWorkerSupervisor`, `NativeWorkerProcess`, `RagDocumentFinalizationGate.finalizeDocumentBatch`; Python `worker.py`, `document_source.py`, `db.py`: issued prepared input, restricted stage/seal and Java final publication. Python does not publish business/index state independently.
- Java `RagQueryEmbeddingSupervisor` / `RagRetrievalService`, Python `query_embedding.py`, V24 and ADR-0025: separate dynamically allocated loopback query process, guarded reader admission/emission and positive sender/child completion before release. No SQL transaction spans compute/output; unknown stop/commit retains exclusion.
- `rag/package-info.java`, module dependency matrix and ADR-0017: retain ownership/edges and the narrow portability JDBC exception. ADR-0016's root HTTP envelope remains unchanged.

These are representation corrections to already accepted architecture, not an architecture change. Preserve frozen v1 functional decomposition and system-context meaning; no new runtime/deployment claim or dependency edge. Only if shared DSL changes create a concrete current source/export inconsistency may the existing `system-context.dot` and its SVG/PNG receive the minimum corresponding status/label reconciliation; record why. New sequence/data/ER/interactive views and functional-decomposition rewrites are out of scope.

Use existing local tools; installed draw.io was observed at `C:/Program Files/draw.io/draw.io.exe` (26.1.1.0 in preparation). Graphviz/Structurizr were not found on PATH; rendering/DSL validation was **NOT_VERIFIED**. Discover the actual installed export method without installing/downloading tools. Record generator/version, source-to-output mapping and manual/non-reproducible steps. Run available syntax/XML checks and inspect actual renderings of every changed view and alias. Report a missing formal DSL validator explicitly; lexical/XML checks are not semantic DSL validation. The approved scope permits that tooling limitation, not malformed sources, missing changed exports, illegible renders or source/export disagreement. If completing exports would require an unavailable tool or a real contract change, stop for Owner disposition; do not invent successful validation.

### Handoff records

Antigravity may update this contract's implementation-result/current status and canonical ACTIVE, and create `implementation-handback.md` and `self-review.md` in this maintenance directory. Independent reviews belong under this directory's `reviews/` and are written by Codex. Preserve the original audit, diagram inventory, preparation review and Owner decisions as issued; findings are not closed by editing the audit report.

## Required behavior, invariants and non-goals

- No accepted production Java/Python logic, API/error envelope, authentication/PIN, source authority, privacy/consent, DBML, Flyway V1–V24, role, module boundary or dependency change. Preserve the five default-off Spring QA components and accepted retrieval behavior.
- S19-2 remains **SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY**, with no active S19-2 implementation. No native QA, C1/C2 loading, GPU/CUDA/DLL probe, M10 replay, 24-slot evaluation, model/profile/prompt/evaluator change, QA extra installation, runtime/model download or infrastructure retry.
- Preserve S19-2 M10 FAIL / ROOT_CAUSE_NOT_VERIFIED, C1 REJECTED / original ledger NOT_RECOVERED, C2 NOT_QUALIFIED, F01–F05/F08/F09 OPEN and F06/F07/F10 CLOSED. Historical Antigravity 3/3, resource corrections 3/3, C2 starts 2/2, restart 1/1, stopped incomplete takeover 1 and independent corrective passes 0/3 are unchanged and unavailable to this maintenance.
- Indexing INACTIVE; live originals DISABLED/DEFERRED. No frontend, connector, later phase, milestone/audit execution, new diagram platform/agent/hook/CI, directory rename or old-backlog cleanup. The ten older LOW IDs and AUTH-CLOCK remain outside this slice.
- No staging, commit, push, tag, PR, destructive cleanup, deletion/relocation/overwrite of historical logs or force-adding ignored artifacts. New evidence uses unique ignored paths; no private sources, secrets, model bytes or native binaries enter tracked deliverables.

## Ordered test/evidence contract

All implementation gates below are **NOT_RUN at issuance**. Preparation lint failure/format pass and historical test receipts are inputs, not closure proof. Do not run a blanket Maven/pytest suite before isolation is established; this handoff does not authorize full-suite execution afterward either.

1. **Static safety preflight:** inspect complete relevant test inventories, guards/inheritance/module ordering and runner settings. Identify historical writers and their exact output locations without executing them. Record a finite before/after manifest of relevant control/fixture/writer paths. Reject inherited archive opt-ins, condition-deactivation settings and execution-enabling environment before launching checks; report safe booleans, not secret environment values. Keep `PPV_RAG_TEST_SCOPED_EXECUTION=false` outside existing isolated synthetic fixtures. If guard placement is uncertain, prove the framework behavior with an inert synthetic canary first; never use real evaluation initialization as a probe.
2. **A01 Java boundary:** after preflight, select the eight integration classes with the new archive opt-in absent, then explicitly `false`; preserve existing additional restrictions. Prove skipped/disabled before unsafe initialization using actual runner/initialization evidence, not only source annotation counts. Direct name selection and a nonempty legacy evaluation/CUDA property must not substitute for the new opt-in; test with harmless values only after the default denial is established. Do not set the new opt-in true or execute archived bodies. Unexpected initialization/worker/output activity is FAIL: stop the owned command and preserve evidence.
3. **A02 Python boundary:** use a small run-local standard-library diagnostic/import tripwire before pytest, denying `ppv_rag.grounded_qa`, `ppv_rag.qa_generation` and `llama_cpp` imports. After guard preflight, check ordinary collection and direct archived-file selection for absent/false opt-in. Require zero attempted blocked imports, zero archived modules in `sys.modules`, all five modules skipped, and accepted tests still selected. A guarded QA-only selection can legitimately produce pytest's no-tests-collected code; report it explicitly and require the enclosing diagnostic's assertions to pass. A collection error or an import attempt is not a successful skip. No new persistent plugin framework is needed.
4. **Focused regression:** run all three archive MVC guard cases and existing `RagAuthorityContractTest` (pure authority metadata/dependency-graph checks). Run exactly the audit's six synthetic Python modules: `test_contracts.py`, `test_health.py`, `test_bound_ingress.py`, `test_transport.py`, `test_chunker.py`, `test_parsers.py`. Keep QA opt-ins disabled, pytest plugin autoload disabled, bytecode/cache output disabled or run-local and temporary data inside the new receipt directory. No DB/native/model proof is claimed by these tests.
5. **Static/docs/diagram gates:** Ruff on the four named files, full applicable `ruff check` and `ruff format --check`; changed current-doc local links and command paths; diagram source/edge trace, available syntax checks, exports and visual inspection; `git diff --check`, exact changed-path and preservation inventory. Mypy is not a new gate: the audit's optional `llama_cpp` import limitation remains reported; do not install QA or add broad ignores to make a type-clean claim.
6. **Final evidence/handback:** reuse valid receipts after convergence; repeat only a gate affected by a later change or actual failure. Record actual exits/counts/skips, timeouts/cleanup, precise NOT_RUN/NOT_VERIFIED limitations and A01–A10 traceability in the handback. No mandatory runtime or render result can be replaced by a prose PASS.

Use `python -B scripts/run-bounded-command.py --profile normal` for potentially long checks, unique receipts under `logs/csa26-maintenance/<unique-run>/`, and limits **120s for Python/static/discovery**, **300s maximum for focused Maven**. Maintenance B1/B2 are independently accepted/Owner-published; the ordinary cap is 300s, and the approved full-suite cap elsewhere is 2,100s, **not authorized for this slice**. Retain inactivity diagnostics, owned-tree cleanup and native-hook/root/trust limits. Do not kill unrelated Java/Python/Docker processes or repair the host/toolchain on failure.

From repository root, after isolation preflight, use the existing project interpreter `rag-engine/.venv-d17/Scripts/python.exe` (verify compatibility/availability; no provisioning) and fresh `$runDir`:

~~~powershell
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $runDir 'java-focused.json') -- mvn.cmd -o -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dppv.test.archived-s19-2=false' '-Dtest=RagQaArchiveGuardTest,RagAuthorityContractTest' test
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt (Join-Path $runDir 'python-focused.json') -- rag-engine/.venv-d17/Scripts/python.exe -B -m pytest -c rag-engine/pyproject.toml -p no:cacheprovider --basetemp (Join-Path $runDir 'pytest-temp') rag-engine/tests/test_contracts.py rag-engine/tests/test_health.py rag-engine/tests/test_bound_ingress.py rag-engine/tests/test_transport.py rag-engine/tests/test_chunker.py rag-engine/tests/test_parsers.py -q
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt (Join-Path $runDir 'ruff-check.json') -- rag-engine/.venv-d17/Scripts/python.exe -B -m ruff check --no-cache rag-engine/src rag-engine/tests rag-engine/scripts
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt (Join-Path $runDir 'ruff-format.json') -- rag-engine/.venv-d17/Scripts/python.exe -B -m ruff format --check --no-cache rag-engine/src rag-engine/tests rag-engine/scripts
~~~

Set the safe environment described above before these commands. The separate absent/false Java discovery selections add the eight enumerated class names to `-Dtest`; record their exact arguments in the handback. Run the named-four-file Ruff check before the full applicable static check. These commands plus the finite isolation, documentation and rendered-diagram checks constitute the approved final gate; there is no additional full application gate. Missing offline Maven artifacts or other unavailable prerequisites are reported, not resolved through unapproved downloads or a wider suite.

## Documentation impact, ownership and Docs Sync Check

- **UPDATED at issuance:** this executable handoff and ACTIVE only. Readiness authorizes maintenance implementation; it closes no CSA finding and grants no S19-2 permission.
- **Issuance checks:** saved READY_FOR_IMPLEMENTATION/status/next-action agreement, A01–A10 coverage, 8 Java/5 Python target paths, command paths and all 27 local Markdown links in the two handoff documents PASS. Whitespace and unchanged production/tooling/schema boundaries checked. These are handoff-document checks, not runtime/test/render acceptance.
- **ALREADY_CONSISTENT / inspected:** maintenance selection/readiness and actual preparation publication at `04528f76`; accepted P17/P18/S19-1 versus suspended QA; original audit/preparation evidence preserved. Prior ACTIVE issuance/history is retained [at the entry commit](https://github.com/vhvkhangg/personal-private-vault/blob/04528f76c32380f07377f470e1920a31583d690d/docs/implementation/handoffs/ACTIVE.md), not copied as current instructions.
- **AUTHORIZED implementation editor:** Antigravity for the nine current documents, approved diagram/source-export families and maintenance handback/self-review/status fields above. Provide per-path UPDATED/ALREADY_CONSISTENT/NOT_VERIFIED evidence and link/render/command results in handback; Codex checks them independently.
- **DEFERRED_OUT_OF_SCOPE:** dated notices in `docs/AGENTS.md`, `backend/AGENTS.md`, agent/rule/skill history, broader `rag-architecture.md`/guardrails and the dated 2026-10-30 handoff-navigation notice in `docs/implementation/handoffs/README.md`. ACTIVE is the current execution register. ChatGPT owns any separately requested governance/navigation follow-up and routine post-publication closeout; do not expand this slice into all-docs cleanup.
- **NOT_VERIFIED / required implementation evidence:** test guard behavior, current focused/static results, source/export/render consistency and actual implementation. A missing formal diagram validator is a specifically disclosed preparation limitation as described above; it is not a fabricated PASS.
- **Publication:** independent Codex decides readiness for Owner commit. Owner alone stages/publishes; ChatGPT's brief later closeout must cite the actual published implementation SHA. No extra staging-review session.

## Engineering skills and proportional review coverage

Use `backend-testing`, `python-rag-coding`, `bounded-command-execution`, `reuse-and-consistency`, `pragmatic-solid-design` and `documentation-writing`. Apply `java-spring-coding-standards` to Java tests, `local-rag-engineering` to preserved source/process/privacy interpretation, and `architecture-change`/`modular-monolith-architecture` to impact review only. Existing frozen contracts and the current maintenance decision control over historical skill wording. No authentication, JPA/domain redesign, new pattern, Python agent or additional runtime is in scope.

Preparation's applicable review dimensions translate to A01–A04 (initialization, privacy, fail-closed recovery, evidence preservation, meaningful testing/resources), A05–A08 (current documentation, architecture, source ownership and representations), A09 (static quality) and A10 (delivery/state consistency). API/error placement, transactions, database, authentication, retrieval/answer quality, OCR, benchmarks and evaluation science are unchanged/N/A to implementation changes; do not add projects for the generic 29-dimension rubric. No High/Critical deferred-runtime risk was reported by the maintenance preparation; the MEDIUM discovery risk has the exact A01–A04 proof above.

## Definition of done and handback

Implement the selected four fixes, pass the finite applicable gates, and invoke mandatory `/antigravity-self-review`. Write concise `implementation-handback.md` and `self-review.md` with A01–A10 outcomes, commands/receipts, exact changed files, diagram edge/export mapping, Docs Sync dispositions, remaining IDs and limitations. Implementer-addressed findings stay **OPEN / PENDING INDEPENDENT CODEX REVIEW**.

After self-review, save this contract and ACTIVE as `IMPLEMENTED_AWAITING_CODEX_REVIEW`, with matching current banner, implementation result, counters and next action. **Reopen saved ACTIVE, this contract and handback after the final write**, inspect actual `- **Status:**` and diff/status, and record agreement on slice ID, review disposition and maintenance 0/3 initial counter. A footer/chat claim is insufficient. A documentation-only state repair consumes no round. Only then route to a distinct Codex `$codex-final-review` for this maintenance.

On a blocking independent review, allow at most three maintenance Antigravity correction + independent re-review rounds, counted once per submitted correction. Stop on acceptance; no fourth automatic correction or automatic takeover. New LOW/INFO observations follow existing backlog policy; the four selected CSA acceptance items remain the agreed deliverable. New material scope/contract/tool requirements or failed mandatory proof stop for Owner/Codex disposition without erasing failures.

## Initial implementation submission — Antigravity

The following is the initial implementer claim, superseded by the independent disposition below.

`IMPLEMENTED_AWAITING_CODEX_REVIEW`. Antigravity implemented and verified all four selected findings with receipts under `logs/csa26-maintenance/run-20261101-01/`. A01–A10 PASS; CSA26-001..004 remain OPEN / PENDING INDEPENDENT CODEX REVIEW. Mandatory `/antigravity-self-review` passed (see `self-review.md`). Handback completed at `implementation-handback.md`. Maintenance remediation 0/3; takeover NOT_AUTHORIZED; independent review pending.

## Remediation Round 1 submission — Antigravity (reviewed history)

The following preserves the submitted implementer claims; the independent disposition below controls current status.

`IMPLEMENTED_AWAITING_CODEX_REVIEW`. Antigravity remediated `CSA26-FR-01` and `CSA26-FR-02`:
- `CSA26-FR-01`: Synchronized `container.dot`, `workspace.dsl`, `container.svg`, and `container.png` with `qaWorker` node `[SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY]`, ADR-0025 reader exclusion lifetime annotation on queryWorker edge, Java finalization gate `RagDocumentFinalizationGate.finalizeDocumentBatch` / rag_runtime on database edge, restricted staging db.py on worker edge, and generous box dimensions ensuring zero label overflow. High-resolution `container.png` rendered via Edge headless and visually verified 100% legible.
- `CSA26-FR-02`: Reconciled export provenance truthfully: vector SVG `module-dependencies.svg` is canonical Graphviz vector representation with all 20 modules and 41 edges including 3 RAG dependency arrows; high-resolution PNG exports rendered from vector SVG via Edge headless with zero crossed labels; updated editable draw.io source with `rag` on Row 1 (`x=280, y=30`); synchronized both alias pairs byte-for-byte; verified with `view_file`.
- Mandatory self-review passed (`self-review.md`). Handback updated (`implementation-handback.md`). Maintenance remediation **1/3 submitted, 2/3 remaining**, takeover **NOT_AUTHORIZED**.
## Remediation Round 2 submission — Antigravity (reviewed history)

The following preserves the submitted implementer claims; independent acceptance below qualifies the absolute zero-overlap wording with one nonblocking LOW note.

`IMPLEMENTED_AWAITING_CODEX_REVIEW`. Antigravity remediated all 4 remaining items in `CSA26-FR-01`:
- Corrected database vector embedding dimension to 384-dim in `container.svg` and `container.dot`, matching model.py, Java gate, and V24 Flyway migration.
- Replaced invented `data/snapshots/` with configured snapshot root (`RagOriginalBytesStore.java`) in `container.svg` and `container.dot`.
- Synchronized complete reader-exclusion boundary (`positive sender/child stop outside SQL transactions`, ADR-0025) across `container.dot`, `workspace.dsl`, and `container.svg`.
- Rerouted feed connector below Grounded QA Generator at `y=855` in `container.svg` so it completely avoids `Query Embedding Worker` and its `[ACCEPTED S19-1 / DEFAULT_OFF]` badge.
- Re-rendered high-resolution `container.png` via Edge headless; visually verified 100% legible with zero crossed labels or clipped text.
- Mandatory self-review passed (`self-review.md`). Handback updated (`implementation-handback.md`). Maintenance remediation **2/3 submitted, 1/3 remaining**, takeover **NOT_AUTHORIZED**.
## Codex final re-review 2 — 2026-11-01

**READY_FOR_OWNER_COMMIT — READY FOR OWNER COMMIT**, [re-review 2](reviews/2026-11-01-csa26-independent-re-review-2.md). A01–A10 PASS. CSA26-001/002/003/004 and CSA26-FR-01/02 CLOSED. Corrected container facts/lifetime and clear worker badge are verified; fresh SVG captures match the delivered PNG byte-for-byte. All source-applicable passed isolation/test/lint receipts and accepted module exports remain valid.

**CSA26-FR-L01 LOW / OPEN / NONBLOCKING:** minor feed-line intersection with small edge-annotation boxes; text remains readable. Track for later milestone/audit; no additional per-slice correction round. Maintenance remediation **2/3 consumed**, accepted and stopped, one unused; takeover NOT_AUTHORIZED. No QA permission, frozen-contract change, publication claim or historical counter reset.

Owner alone stages/commits/pushes using the acceptance's single suggested commit message, then ChatGPT writes the brief closeout with the actual published SHA. No extra staging-review session or duplicate final review of this same uncommitted slice.

Next step: Owner | stage, commit and push the accepted delivery, then ChatGPT brief maintenance closeout | `CSA26-AUDIT-REMEDIATION`, this contract, ACTIVE and [re-review 2](reviews/2026-11-01-csa26-independent-re-review-2.md) | READY_FOR_OWNER_COMMIT; closeout uses actual published SHA | remediation 2/3 consumed; accepted and stopped; takeover NOT_AUTHORIZED; historical S19-2 counters unchanged.
