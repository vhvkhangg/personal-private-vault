# Pre-handoff review — CSA26-001..004 maintenance

- **Date / reviewer:** 2026-11-01 / Codex.
- **Disposition:** `READY_FOR_HANDOFF / OWNER_PUBLICATION_PENDING`, maintenance preparation only. No blocking preparation finding or unresolved material Owner decision was found.
- **Authority:** [Owner selection](../owner-decision-csa26-selected-remediation-2026-11-01.md), [canonical scope](../remediation-scope.md), [audit](../audit-report.md), and [diagram inventory](../architecture-diagram-gap-inventory.md). The earlier audit-only authorization is issuance history; the subsequent explicit selection authorizes this preparation review.
- **Baseline:** local `main`, HEAD and remote `refs/heads/main` independently returned `51240841c91b0a2b94f665d7d66c432d36c063c9`. Entry index and tracked diff were empty. The audit skill and maintenance directory were untracked inputs, so this was tracked-clean, not a completely clean checkout.
- **Write boundary:** this new review and the canonical maintenance scope's status/next step, plus unique ignored diagnostic receipts. No implementation handoff, production/test/build/diagram repair, staging or publication.

This is the explicitly approved **maintenance preparation** mode. Numbered-phase README/preparation, preceding-phase completion and milestone prerequisites do not apply. S19-2 is still `SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY`; the maintenance authorization does not reopen it. The four original CSA26 findings remain **OPEN** pending implementation and independent final review. Preparation readiness is not remediation acceptance or frontend approval.

## Required Owner-visible outcomes and proportionality

Effort estimates below are relative implementation/verification burdens, not measured delivery times. All four selections are already approved together; no additional material product choice is pending.

| Requirement / mechanism | Decision and simplest adequate solution | Reuse / effort / approval |
| --- | --- | --- |
| CSA26-001: ordinary verification cannot enter archived native QA, model, corpus preparation or evaluation writers | **KEEP / REUSE** standard fail-closed test-runner guards evaluated before unsafe initialization. Retain ordinary archive-registration coverage. | Existing Jupiter conditions and pytest facilities; small Java/Python test edits, moderate isolation proof. Owner-approved. |
| Safe checks work without the optional QA installation | **SIMPLIFY** to discovery/isolation proof, three existing archive guard cases and a finite accepted-only regression selection. | Existing project environment, watchdog and synthetic tests; small-to-moderate verification. No QA dependency installation. |
| CSA26-002: current entry points tell the truth and offer usable setup/check instructions | **KEEP / SIMPLIFY** one compact current notice with canonical links and bounded accepted-only commands. Label old notices as dated history. | Exact current-document set in remediation-scope; small prose/link verification. Owner-approved; no rewrite of historical results. |
| CSA26-003: current container/module pictures show actual accepted data and process ownership | **KEEP / REUSE** reconcile existing DSL, DOT, applicable draw.io and their current exports. Preserve original v1 meanings and distinguish delivery status from enablement. | Existing accepted ADRs/source and installed draw.io; moderate source reconciliation/render review. Owner-approved representation corrections only. |
| Separate retrieval sequence, snapshot flow, RAG ERD, interactive Archify or new diagram platform | **DEFER**; inventory priorities 2/3/5+ are proposals, not this slice's deliverables. | Zero current implementation/benchmark burden. Additional views require a demonstrated need and applicable authority. |
| CSA26-004: four I001 diagnostics | **KEEP / SIMPLIFY** import ordering in the four named files, preserving test logic. | Existing Ruff 0.17.0; trivial fix and small static verification. Owner-approved. |
| New agents, hooks, dependencies, CI, directory renames or abstraction layers | **REJECT** for this scope: no demonstrated need. | Existing backend-implementer and skills suffice. No additional platform or architecture work. |
| Full backend/native suites, model evaluation, GPU/OCR/judge/connector work | **DEFER / REJECT AS ACCEPTANCE**: no changed production behavior justifies these gates. Suspended QA remains prohibited. | No benchmark/model-selection burden and no implied activation. Any later expansion needs separate Owner authority. |

Use **one combined, separately committable maintenance slice**, as expressly selected by the Owner. Work in order: isolation guards and non-generative proof; focused regression/static checks; current docs and diagram reconciliation/render review; self-review and one independent maintenance final review. This order is a plan, not an executable handoff.

## Source-verified test boundary

Inventory covered the complete relevant test trees: **74 Java RAG test/support files** and **29 Python test/support files**, including nested packages. All 12 Java `*Qa*Test` classes and all five QA-dependent Python modules were identified; their risky entry paths were inspected against the bases and runner configuration.

| Entry points | Observed mechanism / preparation requirement |
| --- | --- |
| `RagQaNativeIntegrationTest`, `RagQaEvaluationNativeIntegrationTest` | No archive opt-in condition. Both enable QA in Spring; their enabled base configures actual native execution. The evaluation class retains fixed-name `Files.writeString` writers at lines 231/292. Guard discovery/setup before reaching these paths; do not execute or repair the writers. |
| `RagQaCudaNativeIntegrationTest`, `RagQaNativeSafetyIntegrationTest`, `RagQaProspectiveEvaluationNativeIntegrationTest` | Existing nonempty CUDA/evaluation-property conditions do not constitute current Owner authorization. Include them in the archive isolation inventory; retain their existing restrictions. Prospective evaluation also has static path/metadata initialization at lines 37–44: verify that default selection cannot initialize it. |
| `RagQaIntegrationTest`, `RagQaLifecycleIntegrationTest`, `RagQaSenderIntegrationTest` | These register archived QA and use test generation/vector overrides. They are not accepted-system regression evidence. Include them in the default-off QA integration group rather than making normal verification depend on the archived service. |
| `RagQaArchiveGuardTest` | Preserve normal discovery and all **three** existing cases. It imports the guarded components into a minimal MVC context, mocks retrieval and checks absent QA/preserved retrieval routes; no DB/native/model is required. |
| `RagQaBehaviorTest`, `RagQaCitationTest`, nested `RagQaProtocolTest` | Pure helper/protocol tests are distinct from native integration. Preserve their assertions; running or refactoring them is not mandatory for this maintenance. Their existence does not qualify archived QA. |
| Python `test_grounded_qa.py`, `test_qa_generation.py`, `test_qa_model_paths.py`, `test_qa_runtime_profile.py`, `test_qa_cuda_profile.py` | Five modules import archived QA. `test_grounded_qa.py:26` initializes service state during import and its embed case reaches the real pipeline; runtime-profile tests inspect optional native bytes/packages. Keep these outside ordinary accepted-only execution without installing the QA extra. |

`backend/pom.xml` has no default archive discovery exclusion. `rag-engine/pyproject.toml` selects the whole tests directory; existing `conftest.py` has no archive filter. Comments are not execution guards.

Prefer existing **class-level Jupiter conditions** and **pytest pre-import/module guards or collection exclusion**. The exact patch belongs in the later handoff. A Python skip marker or `pytest_collection_modifyitems` applied after import is insufficient. A pre-import module skip is a feasible small alternative where direct path selection can bypass a collection filter. Do not add a generalized plugin framework. Missing/false opt-in must fail closed; existing CUDA/evaluation flags or explicit test-name selection must not silently defeat the chosen boundary.

Before any affected selection executes, inspect guard placement, inheritance and static/module initialization. If class conditions cannot establish safe pre-initialization behavior for an entry point, use standard runner exclusion for that entry point instead of changing archived evaluation logic. `mvn -DskipTests` alone proves no discovery exclusion; `pytest --collect-only` still imports modules and is not automatically safe before the new boundary is checked. Prove the guard without enabling the archival opt-in or invoking an archived writer. An opt-in mechanism is not permission to use it now.

The later handoff must define one finite isolation check that demonstrates absent archived imports/initialization, worker starts, corpus preparation and historical receipt writes, while ordinary accepted tests remain selected. Follow it with the three real archive guard cases and a small inspected accepted-only selection. The audit's six synthetic Python modules (`test_contracts`, `test_health`, `test_bound_ingress`, `test_transport`, `test_chunker`, `test_parsers`) provide a reusable starting selection; do not broaden blindly.

## Architecture impact and export feasibility

This is a correction of **representations of already accepted decisions**, not a new database, security, integration, repository-layout or module-boundary decision. No new ADR or frozen-baseline rewrite is needed for that limited reconciliation. If implementation discovers a real contract change, stop for explicit Owner disposition and ADR governance rather than inventing a new edge.

- `workspace.dsl:26,53–54` and `container.dot:7` retain synthetic/non-root/fixed-port descriptions. ADR-0023 explicitly accepts application-level same-Windows-user trust; diagrams must not promise an OS sandbox.
- Java `RagDocumentFinalizationGate.finalizeDocumentBatch`, Python `worker.py`, `document_source.py` and `db.py` substantiate Java-owned finalization and prepared-input/restricted staging. Depict immutable ORIGINAL_BYTES as a storage artifact, not an autonomous service or live-original read.
- `RagQueryEmbeddingSupervisor` launches `ppv_rag.query_embedding:app` on an allocated loopback port. `RagRetrievalService` and ADR-0025 establish the separate request/source-reader/output lifetime. Show these within the existing local deployment, without implying accepted answer generation.
- `rag/package-info.java` retains only `vault :: entry`, `fiction :: source`, `film :: source`. Preserve existing module edges and the portability exception. Update candidate labels; do not turn all permitted dependencies into observed runtime calls.
- Use separate status labels for accepted P17/P18/S19-1 capabilities, inactive/default-off operation, suspended/unaccepted QA, and proposed frontend/integrations. A current picture cannot grant execution authority.
- Keep the two module export aliases synchronized if changed; deletion/renaming is not selected. Frozen functional-decomposition/system-context meaning stays intact; reconcile only directly affected current labels/relationships in the approved model.

All **33 tracked architecture paths** were inventoried. The two existing draw.io and five SVG files parse as XML. This is **not** semantic DSL validation or rendered visual QA. Installed `C:/Program Files/draw.io/draw.io.exe` reports product version **26.1.1.0**. Graphviz/Structurizr were not found on PATH; no checked-in export script was found. CLI rendering and DSL validation are **NOT_VERIFIED** at preparation.

No new rendering framework or runtime download is required by this review. Implementation must identify the actual local export method, validate edited source with available tools, synchronize SVG/PNG, and inspect rendered legibility. Record manual/non-reproducible steps honestly, as the approved scope allows. Missing validators must be reported; malformed sources, absent changed exports or source/export disagreement cannot be called PASS. A necessary additional tool/contract change returns to Owner instead of silently expanding scope.

## Applicable harness and ownership

Reviewed authority and harness categories: root `AGENTS.md`; scoped `docs/AGENTS.md`, `backend/AGENTS.md`, `backend/src/test/AGENTS.md`; Owner policy and Docs Sync contract; pre-handoff, review-coverage, documentation-writing, backend-testing, architecture-change, graphify-context, Python-RAG, bounded-command, implement/self-review and takeover skills; backend-implementer agent; workflow/tests/documentation/architecture/Python/security/package/context rules; `.agents/hooks.json`, repository-safety rule/config and `.codex/config.toml`; watchdog source; POM, Python project/pin/lock inventory; selected tests, runtime edge sources, ADR-0023/0025, suspension/archive records and current-navigation targets. The new one-off audit skill was reviewed as an input, not invoked as another audit.

Graphify was queried once for navigation; the cache is not source authority. Existing `backend-implementer`, `python-rag-coding`, `backend-testing` and `bounded-command-execution` suffice. No new agent, rule or hook is justified. `codex-implementation-takeover` already provides a last-resort route after three independently blocked maintenance remediation rounds and applicable Owner authority; it is not active now. Its implementer cannot independently approve its changes.

Antigravity owns the authorized test/static/diagram implementation and documentation explicitly included in its later handoff. ChatGPT owns routine prose corrections not so included. The handoff must name the current-document write set and documentation owner, using this Owner-approved combined slice; no separate automatic documentation gate is added. Antigravity self-review and saved-state consistency checks are mandatory before separate Codex final review.

The implemented watchdog uses a **300s ordinary cap**, unique non-clobber receipts, bounded stream capture, inactivity/process diagnostics and owned-tree cleanup. Use shorter normal-profile budgets for this slice. No full-suite authorization or 2,100s command is needed here. Native hook enforcement remains limited to its reviewed Windows/client/root/trust conditions; it was not recertified in this session.

## Evidence: measured now, reused, and still proposed

New ignored receipt root: `logs/current-system-pre-handoff-review/csa26-20261101T043739-2673e5c3cc714d968caacfb1f1e8d9e9/` (**R**).

| Evidence | Actual outcome / limit |
| --- | --- |
| `R/entry-inventory.json` | 2,787 tracked hashes and entry input hashes; recursive test/architecture inventory; seven XML parses. Read-only standard-library diagnostics; no application/test import. |
| `R/ruff-check.json` | Fresh bounded static check **FAIL / exit 1**, exactly the four selected I001 diagnostics; 0.64s child-command duration. No fixes applied. |
| `R/ruff-format.json` | Fresh bounded format check **PASS / exit 0**, 120 files already formatted; 0.64s child-command duration. |
| `R/final-preservation-and-links.json` | All 2,787 tracked hashes preserved; only the authorized scope input changes and this new review; 45 local path links resolve. HEAD unchanged, index empty and whitespace/conflict checks pass. No complete historical ignored-log rehash or external URL/anchor validation is asserted. |
| Prior audit receipts | The five named receipts exist and retain exits 0/0/1/0/1 for focused Python, safety-hook units, Ruff check, Ruff format and Mypy respectively. The reported 42 Python and 21 hook cases were not rerun here; no new behavioral PASS is claimed. |
| Mandatory implementation proof | Isolation/selection, three archive guard cases, safe regressions, corrected lint, current links and rendered diagrams are **PROPOSED / NOT_RUN** under a future handoff. They are closure gates, not measured preparation results. |

Fresh commands used `python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt R/<name>.json -- rag-engine/.venv-d17/Scripts/python.exe -B -m ruff ...`: `check --no-cache rag-engine/src rag-engine/tests rag-engine/scripts` and `format --check --no-cache` over the same paths. Both completed without timeout or cleanup failure. Prior audit Mypy failure concerns missing optional archived `llama_cpp`; no runtime installation, broad ignore or type-clean claim follows.

**NOT_RUN:** Java compilation/tests, pytest collection/execution, native/DB/model/QA/evaluation, vulnerability scan, IDE inspections, diagram generation/rendering and DSL validator. No blanket suite was launched. No benchmark, hardware capacity, IDE-clean or current full-suite claim is made.

## Coverage — all 29 considerations

`PASS (plan)` means the approved preparation addresses that dimension with a finite future gate; it does not certify runtime behavior. Unchanged dimensions are explicitly N/A to the planned change, not accepted as newly tested.

| # | Dimension | Preparation status / evidence |
| --- | --- | --- |
| 1 | Owner intent and non-goals | PASS (plan): four selected findings; suspension and exclusions retained. |
| 2 | Requirement traceability | PASS (plan): scope's four-row acceptance table and gates above. |
| 3 | Architecture/ADR/diagrams | PASS (plan): representation-only impact assessment; render/DSL execution NOT_VERIFIED. |
| 4 | Module ownership/dependencies | PASS (plan): preserve descriptor/matrix; no new module or edge. |
| 5 | Folder/package structure | PASS (plan): recursive 74/29 test and 33 architecture inventories; no moves or new runtime packages. |
| 6 | Shared HTTP/exception placement | N/A: no envelope/error/API relocation; ADR-0016 placement preserved. |
| 7 | SOLID/cohesion | N/A to production refactoring; standard local test guards only. |
| 8 | DRY/KISS/invariants | PASS (plan): reuse framework guards and existing watchdog; no generalized framework. |
| 9 | Patterns | N/A: no new production pattern or interface. |
| 10 | Domain behavior | N/A: accepted business behavior is excluded from edits. |
| 11 | Public API compatibility | PASS (plan): default-off QA and preserved retrieval route checks; no wire change. |
| 12 | Input/initialization validation | PASS (plan): absent/false opt-in, pre-import/setup/static-initializer checks. |
| 13 | Authorization/privacy/consent | PASS (plan): no source authority change; five production QA conditions preserved. |
| 14 | Secrets/retention/egress | PASS (plan): no downloads/private uploads, ignored non-clobber receipts, historical writers unexecuted. |
| 15 | Transactions/concurrency | N/A to implementation changes; diagrams must retain actual Java publication/reader lifetimes. |
| 16 | Failure/recovery | PASS (plan): fail-closed guards, bounded commands; no S19-2 recovery retry. |
| 17 | Database/persistence | N/A: DBML, V1–V24, JPA and privileges unchanged. |
| 18 | Complexity/resources | PASS (plan): static/non-generative work under ordinary caps; no model resource budget. |
| 19 | Retrieval/answer quality | N/A: no quality change or candidate qualification; accepted/archived labels remain distinct. |
| 20 | Documents/OCR/multilingual | N/A: no parser/format/OCR change. |
| 21 | Negative/security verification | PASS (plan): default guard absence/false, safe discovery and no native/writer activation. Runtime proof pending. |
| 22 | Test quality | PASS (plan): narrow boundary proof plus archive guard and safe accepted tests; no test-count target. |
| 23 | Benchmarks | N/A: no new performance obligation or claim. |
| 24 | Evaluation science | N/A: preserved failed/frozen evaluation; no retuning, replay or new held-out requirement. |
| 25 | Dependencies/lifecycle | PASS (plan): existing POM/lock/environment retained; optional QA installation excluded. |
| 26 | Build/static/tooling | PASS (plan): lint reproduction and focused gates; Java/pytest/render proof NOT_RUN. |
| 27 | Logging/observability | PASS (plan): unique factual receipts, no private payloads or historical writer execution. |
| 28 | Documentation/governance | PASS (plan): exact current-document set, Docs Sync ledger below and explicit next gate. |
| 29 | Hygiene/delivery | PASS (plan): tracked-hash preservation check, review/status-only edits and Owner-only publication. |

## Docs Sync Check and disposition

- **UPDATED:** this review and `remediation-scope.md` current status/next step. Original audit, inventory, Owner decisions and new audit skill are preserved as received; no finding is closed.
- **ALREADY_CONSISTENT:** canonical execution status, S19-2 suspension/archive boundary, P17/P18/S19-1 accepted-history separation and this maintenance-only Owner selection. `ACTIVE.md` stays unchanged.
- **AUTHORIZED FUTURE CORRECTION:** the exact CSA26-002 document set and CSA26-003 source/export family in remediation-scope. These still contain the audited drift; readiness approves the finite repair plan, not their current accuracy. Link/status/command checks remain implementation acceptance.
- **DEFERRED_OUT_OF_SCOPE:** older status in `docs/AGENTS.md`, `backend/AGENTS.md`, `.agents/rules/agent-handoff-workflow.md`, `backend-implementer/agent.md`, the pre-handoff skill's historical Phase-19 ban, and broader `rag-architecture.md`/guardrail history. The selected decision and current Owner policy govern this maintenance. Do not expand into all-skills cleanup or close the ten old LOW IDs/`AUTH-CLOCK`.
- **NOT_VERIFIED:** new guard runtime behavior, actual rendered/syntax/export correctness, current full regression and published preparation SHA. Publication must be verified at handoff creation.

No preparation correction round or takeover was consumed. Future maintenance implementation starts with no remediation rounds consumed; the initial implementation/self-review is not a round. Apply at most three correction plus independent re-review rounds to that new maintenance handoff. Historical S19-2 Antigravity 3/3, resource corrections 3/3, C2 starts 2/2, restart 1/1, stopped incomplete takeover 1 and independent corrective passes 0/3 remain unchanged and unavailable for reuse.

**Suggested preparation commit:** `docs(maintenance): prepare CSA26 audit remediation`

**Next step:** Owner, then Codex | Owner commits/pushes reviewed preparation, then `$codex-create-handoff` | CSA26-001..004 maintenance, `remediation-scope.md` and this review | `READY_FOR_HANDOFF`; verify publication before creating the separate maintenance handoff | maintenance remediation not started; historical S19-2 counters unchanged.
