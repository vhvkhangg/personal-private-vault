# Current PPV system read-only audit — 2026-11-01

**Disposition: `AUDIT_REPORTED / OWNER_REVIEW_REQUIRED`.** This is the [one-off authorized audit](owner-authorization.md), not phase acceptance, remediation, a frontend approval, or a handoff. One new MEDIUM finding concerns default test discovery reaching suspended QA. Three new LOW findings concern current documentation, diagrams and lint. No new CRITICAL/HIGH defect in accepted application behavior was established by this audit. That statement is bounded by the verification limits below; it is not a clean bill of health.

## Baseline, authority and preservation

| Item | Observed state |
| --- | --- |
| Branch and HEAD | `main`, `51240841c91b0a2b94f665d7d66c432d36c063c9` |
| Remote | `git ls-remote --heads origin main` returned the same exact SHA at entry. Rechecked at completion; see the final diagnostic receipt. |
| Entry index / tracked tree | Empty index; no tracked modifications. Untracked: `.agents/skills/codex-current-system-readonly-audit/` and this audit directory containing the Owner authorization. These pre-existing inputs were not changed. The tree was therefore **tracked-clean, not completely clean**. |
| Writes | Only this report, the [diagram inventory](architecture-diagram-gap-inventory.md), and new ignored diagnostics under the receipt root below. No source/configuration/test/lock/migration/diagram/ACTIVE edits, staging or publication. |
| Current execution state | [ACTIVE](../../handoffs/ACTIVE.md): `NO_ACTIVE_HANDOFF`; S19-2 suspended, not accepted, with the narrow source-archival exception. |
| Accepted history | P17 frozen; [P18 closed under D26/D27](../../phase-18/phase-18-final-owner-closeout-2026-10-25.md); [S19-1 accepted/published](../../phase-19/s19-1-owner-publication-closeout-2026-10-28.md). Phase 19 remains open/on hold. |
| Source archive | `106a0d2` is the default-off source-archive commit, followed by the skill-only baseline commit. `git diff --name-only 106a0d2 HEAD -- backend/ rag-engine/` returned no paths. Publication of this source is **archival**, not acceptance. |

The [suspension](../../phase-19/owner-decision-s19-2-suspension-2026-10-30.md), [archival decision](../../phase-19/owner-decision-s19-2-source-archival-only-2026-10-31.md), current Owner policy and this audit's decision control over stale introductory notices. No evidence was found requiring the baseline stop rule.

## Method and evidence boundaries

All **2,787 tracked paths** were inventoried and hashed. Structural diagnostics inspected all **1,462 production Java files**, **382 module package directories**, **83 JPA entity table mappings**, **24 Flyway migrations**, and **73 tracked Python files** for syntax. There are **20 top-level Java modules**, **179 Java test files**, and **25 Python source files** under `rag-engine/src/`. Counts are files/declarations, not assertions of exhaustive behavioral review.

Every relevant subtree was inventoried; detailed reading concentrated on authentication, module boundaries, source authority, snapshot/reader/process lifetimes, HTTP/error contracts, import transactions, finance locks, media compensation, portability, parser input isolation, dependency declarations and agent controls. The module ledger records the actual depth. This is **not a line-by-line review of every method** or a fresh independent rerun of every historical acceptance case.

Graphify navigation read the existing local graph in memory: 8,894 nodes and 32,909 edges. Vocabulary selected from actual labels: authentication, security, rag, retrieval, snapshot, indexing, process, portability, storage, privacy. Its cache predates this checkout. No graph regeneration, graph writes, model query, external service upload or diagram generation occurred. Canonical source, not graph edges, supports conclusions.

Receipt root, abbreviated **R** below:

`logs/current-system-readonly-audit/csa26-20261101T034945-77703231e27f4e5a8811ccf91e68814e/`

Local ignored receipts are review evidence; they will not resolve in a public GitHub checkout. `source-inventory.json` records tracked hashes/package ownership, `structure-checks.json` records graph/table-name checks, and `navigation-links.json` records the bounded link scan. These custom diagnostics are lexical/inventory checks, not Java compilation, SQL execution or a vulnerability scanner.

Source abbreviations used below:

- **J:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/`
- **T:** `backend/src/test/java/com/vhvkhangg/personalprivatevault/`
- **P:** `rag-engine/src/ppv_rag/`

### Executed diagnostics

Environment observed: Windows 11 amd64; OpenJDK **25.0.2**; Maven **3.9.15**; repository Python `.venv-d17` **3.14.4**; pytest **9.1.1**, Ruff **0.17.0**, Mypy **2.4.0**, FastAPI **0.143.0**, Pydantic **2.14.1**, NumPy **2.5.4**, ONNX Runtime **1.31.0**, tokenizers **0.23.3**. Package versions were read from installed metadata, without importing/loading model runtimes. The shell's default Python is **3.11.7**; it ran the standard-library watchdog/inventory only, not the application tests requiring Python >=3.12.

| Check / receipt under R | Actual result and limit |
| --- | --- |
| `python-focused.json` | **PASS, 42 tests**, zero failures, 0.26s pytest time. Contract validation, health, pre-body authority denial/claim ordering, raw-byte limits, duplicate JSON, Markdown authorization/recursive transclusion, chunk attribution and exact spans. All synthetic; database claim mocked; no model/session construction, native QA, real DB or owner corpus. |
| `safety-hook-units.json` | **PASS, 21 tests**, 0.777s unittest time. Hook decision/adaptor subprocesses only, not execution of the Git command strings they classify. Does not certify the current native client loads/enforces the hook. |
| `ruff-check.json` | **FAIL, exit 1:** four I001 test import-order diagnostics, CSA26-004. No fixes applied. |
| `ruff-format.json` | **PASS, exit 0:** 120 files already formatted. |
| `mypy.json` | **NOT VERIFIED / command exit 1:** checked 25 source files; two `import-not-found` diagnostics for optional archived `llama_cpp` modules at `P/qa_generation.py:230,280`. The inspected `.venv-d17` lacks that optional runtime. No package installation or broad ignore was added. This is not evidence of a core type defect or a passing whole-source type gate. |
| Python syntax | **PASS:** AST parsing of all 73 tracked `.py` files without bytecode creation. |
| Module/import graph | **PASS within lexical scope:** 41 inter-module import edges, zero matrix violations, zero cycles, zero cross-module `.internal` imports. No `@Data` found on the 83 entity classes. Not a fresh Spring Modulith/ArchUnit execution. |
| Persistence inventory | V1 SQL and frozen DBML both name **69 tables**, no name differences. All 83 explicit JPA entity table names occur in migration declarations. This does **not** verify every column/default/index/constraint, live privileges, query plans, or migration application. |
| Navigation links | 411 inline local Markdown path-link occurrences in 50 current/navigation/architecture/ADR files: **0 missing targets**. Anchors, external URLs, code-block examples and all historical reports were outside this check. |
| Delivery | `git diff --check` passes for tracked changes; final preservation check verifies all inventoried tracked hashes and unchanged index/HEAD. New reports receive their own link/structure checks. |

Exact bounded invocations, from repository root, with `$R` meaning the receipt root and `$P = 'rag-engine/.venv-d17/Scripts/python.exe'`:

```powershell
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt "$R/python-focused.json" -- $P -B -m pytest -c rag-engine/pyproject.toml -p no:cacheprovider --basetemp "$R/pytest-temp" rag-engine/tests/test_contracts.py rag-engine/tests/test_health.py rag-engine/tests/test_bound_ingress.py rag-engine/tests/test_transport.py rag-engine/tests/test_chunker.py rag-engine/tests/test_parsers.py -q
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt "$R/safety-hook-units.json" -- $P -B -m unittest discover -s .agents/hooks -p test_repository_safety.py -v
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt "$R/ruff-check.json" -- $P -B -m ruff check --no-cache rag-engine/src rag-engine/tests rag-engine/scripts
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 120 --receipt "$R/ruff-format.json" -- $P -B -m ruff format --check --no-cache rag-engine/src rag-engine/tests rag-engine/scripts
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 180 --receipt "$R/mypy.json" -- $P -B -m mypy --config-file rag-engine/pyproject.toml --cache-dir "$R/mypy-cache" rag-engine/src
```

Tests inherited `PYTHONDONTWRITEBYTECODE=1`; pytest plugin autoload was disabled and default worker execution set false. Hook tests used a disposable TEMP/TMP within R. Static tools used no cache or a fresh cache within R. The watchdog used the shell Python 3.11.7. The existing runner produced separate exclusive stdout/stderr logs and receipts; all five completed without timeout or cleanup failure. These commands are an audit record, not permission to run QA or a blanket suite.

Baseline commands: `git status --short`, `git branch --show-current`, `git rev-parse HEAD`, `git ls-remote --heads origin main`, `git diff --stat`, `git diff --cached --stat`, and `git diff --check`. Source-applicability comparisons used `git diff --name-only <ref> HEAD -- backend/ rag-engine/` for `3158f402c7dba9c29a5276d5ffb3a9349e022f55`, `5e84e7096137b15528bb09114d7358f719c7bdc7`, and `106a0d2`. Read-only Python stdin diagnostics used `python -B -`, `ast`, `pathlib`, `hashlib`, XML/JSON parsing and `git ls-files -z`; no dependency installation or generated source was involved.

A few ad hoc reads failed on guessed non-existent paths, Windows console encoding, or inline native-argument quoting. Corrected exact-path/UTF-8/stdin reads completed. Three inline diagnostics were rejected **before launch** by the repository quoting hook; safe simplified reads completed without altering/bypassing controls. These are diagnostic setup limitations, not passing product tests or product defects.

### Historical receipts inspected, not rerun

| Evidence | What was independently checked now / applicability |
| --- | --- |
| `logs/phase18/s4-java-instrumented-20261025-201e93c2/full-java.json` and [D26 review](../../phase-18/reviews/2026-10-25-slice-4-d26-independent-final-acceptance-review.md) | Raw receipt exists: PASS/exit 0, 1,937.915s/2,100s. The accepted historical report distinguishes 1,580 testcase elements, 1,567 suite attributes and one historical skip. This audit did not recount its archived XML. No non-RAG production Java file differs between P18 publication `3158f402` and current HEAD. Shared configuration, RAG, tests and V24 did change, so this is **historical applicable evidence**, not a full current green gate. |
| `logs/phase-19/s19-1/codex-independent-attempt-4-review-1793168874689/retrieval-sender-native.json` and [S19-1 final review](../../phase-19/reviews/2026-10-28-s19-1-independent-codex-corrective-attempt-4-final-review.md) | Raw receipt PASS/exit 0, complete capture/cleanup; historical review records 84 fresh plus 134 reused Java cases and 14 reused Python cases. Shared retrieval/recovery/ingress files have subsequent archival deltas. No blanket unchanged-source reuse or new retrieval/native PASS is claimed. |
| `logs/phase-19/s19-2-source-archive-84adfabc5cf6400d95b8b4d0051b3431/guard.json`, `integrity-v3.stdout.log`, and [archive manifest](../../phase-19/s19-2-source-archive-manifest-2026-10-31.md) | Existing guard receipt PASS/exit 0; manifest records three focused default-off MVC tests. Source inspection confirms all five conditions and unchanged runtime/test trees since archive commit `106a0d2`. This supports the narrow default-registration boundary, not full application, native, DB or S19-2 acceptance. The prior 29,999-log preservation assertion remains that task's evidence; this audit did not rehash that whole archive. |

**NOT_RUN:** Java build/test/clean/full suite, live PostgreSQL/Testcontainers/MinIO, production migrations, real retrieval embeddings, native process/crash/race probes, C1/C2, CUDA, M10/24-slot evaluation, provisioning/download, new performance benchmark, full vulnerability/license/SBOM scan, PMD/SpotBugs/CPD/JaCoCo, IDE diagnostics, or diagram rendering/export. The current permission and CSA26-001 preclude a blanket suite. No full test result, production/deployment readiness or fresh model quality is inferred.

## Findings and priorities

Counts of **new CSA findings only**: CRITICAL **0**, HIGH **0**, MEDIUM **1**, LOW **3**, INFO **0**. Preserved S19-2 and earlier backlog IDs are separate; they are neither reclassified nor counted as new defects.

### CSA26-001 — Default test discovery reaches suspended QA/evaluation

- **Severity / disposition / priority:** MEDIUM / OPEN / `FIX_BEFORE_FRONTEND`.
- **Category:** build/test isolation and evidence preservation; archived-code integration, not a new acceptance review of QA.
- **Evidence:** [RagQaEvaluationNativeIntegrationTest.java](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagQaEvaluationNativeIntegrationTest.java):27–38 registers a Spring test and enables QA/retrieval/test-scoped execution without an opt-in execution condition. Lines 119–145 default to C1/M01 and can prepare the corpus; lines 230–231 and 291–292 use `Files.writeString` at fixed historical evaluation filenames. [RagQaNativeIntegrationTest.java](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagQaNativeIntegrationTest.java):33–44 also enables native QA unconditionally in its test context. Their base `T/rag/AbstractEnabledRagIntegrationTest.java:31–42` enables native worker execution. No inherited test-disable condition was found in that chain.
- **Selection mechanism:** `backend/pom.xml:176–223` adds no archival test exclusions/profile. The locally installed Boot BOM resolves Surefire **3.5.6**; its plugin descriptor documents default `**/*Test.java` discovery, which includes these classes. The prospective newer evaluation class has an explicit `EnabledIfSystemProperty` at line 32; that does not gate the older native/evaluation classes. Python's [pyproject.toml](../../../../rag-engine/pyproject.toml):54–56 selects all tests, including `tests/test_grounded_qa.py:26,73–83`, which initializes the archived service and exercises its embedding path. QA tests have comments, not default exclusion.
- **Consequence:** ordinary unqualified test commands can enter suspended native/model paths, fail for missing optional runtime/resources, and, if execution reaches the old evaluation writers, replace retained historical filenames. No overwrite or model execution was performed in this audit. The runtime Spring default-off guard still works; this finding concerns a different entry point: the build/test runner.
- **Reproduction status:** **static mechanism VERIFIED; execution intentionally NOT_RUN**. Do not reproduce with `mvn clean verify` or blanket pytest under this permission. The old non-clobber writer issue was already identified under S19-2-F04; this finding adds its current default-build reachability, without reopening/resolving F04.
- **Smallest recommendation:** separately authorize a bounded build/test-isolation change: make suspended native QA/evaluation and QA-dependent Python tests explicitly opt-in and absent from ordinary accepted-system verification. Keep `RagQaArchiveGuardTest` in ordinary coverage. Define an accepted-only check that does not require installing the optional QA runtime. Preserve archived bodies and old evidence; no QA repair, evaluation or activation follows.
- **Proof to close:** inspect default discovery without inference; focused non-generative exclusion/guard checks must show archived native/evaluation tests cannot run by default and accepted tests/guard still run. Only after that boundary is separately authorized and verified should the Owner authorize a broader accepted-code regression selection.
- **Confidence / frozen impact:** HIGH for selection/call/writer paths; downstream execution/resource outcome not measured. No production/schema change proposed; build/test policy needs explicit Owner-selected remediation.

### CSA26-002 — Current navigation contradicts later accepted/suspended status

- **Severity / disposition / priority:** LOW / OPEN / `DEFER` to one scoped current-document cleanup.
- **Evidence:** root `AGENTS.md:1` and `docs/workflow/current-owner-delivery-policy.md:3` still route to S19-2 handoff creation/NOT_STARTED/0-of-3; root `README.md:1`, `docs/README.md:1` and `docs/roadmap.md:1` say Phase 19 not authorized. `docs/architecture/README.md:15` describes contract synchronization and a CHANGES_REQUESTED candidate; `docs/repository/repository-package-tree.md:5` still awaits P17 review. ADR-0025:3 still says candidate awaits independent acceptance. `rag-engine/README.md:1,103–128` presents old Slice 3/blocked parser/OCR setup, names removed tooling and old dependencies. Its lines 67–73 prescribe blanket unbounded checks, which also intersect CSA26-001.
- **Mechanism / consequence:** ordinary entry documents retain multiple incompatible CURRENT notices. A reader must recover the latest accepted P17/P18/S19-1 and suspended S19-2 facts from other records. This can misroute preparation or validation. The authoritative ACTIVE/suspension/audit decision resolves the conflict, so no present grant or application failure is inferred and severity remains LOW.
- **Recommendation / proof:** add one accurate current pointer and clearly label dated historical sections; link actual closeouts and suspension. Correct current setup to accepted Markdown/default-off boundaries and bounded checks; preserve immutable reviews/decisions/failures. Verify direct source/status links and command selection. Do not rewrite old ADR decisions to invent past acceptance.
- **Confidence / frozen impact:** HIGH; documentation-only repair requires selected Owner scope; no current cleanup was performed. Overlaps older documentation backlogs without closing them.

### CSA26-003 — Current architecture views still depict synthetic-only candidate topology

- **Severity / disposition / priority:** LOW / OPEN / `DEFER` to an approved derived-document update.
- **Evidence:** [workspace.dsl](../../../architecture/structurizr/workspace.dsl):26,53–54 calls the worker non-root/synthetic/digest-only at fixed loopback :8001. [container.dot](../../../architecture/diagrams/source/container.dot):7 and its SVG retain “Phase 17 candidate / synthetic digest only.” The module DOT/draw.io/SVG family retains “rag (Phase 17 candidate).” Current `P/worker.py:123+` and `P/db.py:103+` contain actual Markdown chunk/vector result staging; `P/query_embedding.py` and `J/rag/internal/application/retrieval/RagQueryEmbeddingSupervisor.java` define a separate request-bound query process. Snapshot storage, reader reservations and incident fences are absent from those runtime views.
- **Consequence:** drawings hide the accepted data/process boundaries and may suggest OS confinement or a single synthetic daemon that current Windows application-level snapshot trust does not promise. There is no demonstrated module-edge violation: the source import graph agrees with the matrix. Frozen v1 functional/context diagrams remain historical baseline views, not failed runtime tests.
- **Recommendation / proof:** use the [diagram gap inventory](architecture-diagram-gap-inventory.md) to select updates; retain baseline meaning, annotate accepted/default-off/archive/future layers, verify each edge against source and ADR, then regenerate only after authorization. Proposed missing diagrams are optional review aids, not new acceptance criteria.
- **Confidence / frozen impact:** HIGH for text/topology drift. Frozen canonical diagram edits require Owner authorization and synchronized sources/exports; none were changed.

### CSA26-004 — Four Python test import-order diagnostics

- **Severity / disposition / priority:** LOW / OPEN / `DEFER`.
- **Evidence:** Ruff 0.17.0, `R/ruff-check.json` and stdout: I001 at `rag-engine/tests/test_grounded_qa.py:7`, `test_qa_model_paths.py:3`, `test_qa_runtime_profile.py:3`, and accepted `test_query_embedding.py:3`. Ruff format passes independently.
- **Consequence / recommendation:** the current declared lint gate exits 1. Later reorder only the indicated import groups within an approved hygiene scope; no functional refactor or runtime provisioning is needed. Three files are archived QA and remain suspended.
- **Proof / confidence / frozen impact:** rerun the same non-mutating Ruff command; HIGH confidence; no behavior/schema impact, no LOW-only remediation loop required. This audit did not run `--fix`.

## Source observations and retained design choices

**KEEP:** the capability-based modular monolith, two runtime directories, named public interfaces, ADR-0016 root HTTP envelope, module-local web adapters, PostgreSQL/Flyway ownership, read-only portability exception and media storage port have concrete roles. Source scans found no competing `ApiResponse`, reverse foundation/RAG/search dependency, cross-module internal import, tracked `.gitkeep`/bytecode/cache artifact, or path casing collision. This does not prove absence of every semantic duplicate or hidden fully qualified dependency.

Authentication source uses singleton bootstrap, PBKDF2 with legacy BCrypt verification, HS256 issuer/time validation and required >=256-bit injected signing material. Session rotation/revoke uses a locked hashed-token row; PIN changes refresh the locked singleton. Login/bootstrap/token records redact secrets in `toString`. `SecurityConfiguration.java:119+` permits only explicit auth/health/docs routes, with other application routes authenticated. Stateless bearer CSRF/CORS/token-storage decisions match the existing security contract. Internet exposure and browser token transport remain Owner-gated; no cookie/security policy change is inferred.

Media upload spools bounded input, uploads outside the metadata transaction, compensates confirmed rollback, and retains an object for ambiguous commit outcomes (`J/media/internal/application/storage/ImageUploadService.java:53–177`). Portability uses a read-only REPEATABLE READ allowlist and excludes auth/security/system tables (`J/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java:120–128,184–259`), with incremental JSONL export. Existing residual orphan/backup/provider behavior is an accepted operational tradeoff, not a newly proved defect. No live MinIO, restore or checksum drill was run.

Finance orders/refreshes pessimistic wallet locks (`FinanceLockManager.java:43–51`) and uses ledger-entry transactions. Vocabulary review locks/refreshes an item and persists its review plus new state atomically (`J/knowledge/vocabulary/internal/application/VocabularyService.java:178–236`). Import create/parse/validate/execute retain job guards and whole-job target rollback. The attempted FAILED assignment followed by a runtime exception in `ImportJobService.java:135–142` rolls back; this is already explicitly documented/tested as retained CREATED/no writes, including `T/importdata/ImportDataIntegrationTest.java:795+` and `:889+`. It is **not** reported as a new failure-state bug.

Python production parsing accepts prepared DOMAIN fields or MD payloads, resolves only supplied approved dependencies, and performs no child filesystem resolution (`P/document_source.py:34–138`). `P/transport.py` claims/commits before body consumption, limits bytes/deadline and rejects duplicate authority fields; the focused tests verify isolated negative cases. `P/db.py:17–50` restricts loopback/role identity and statement/lock/transaction durations; no live DB privilege certification is claimed. Model/tokenizer hashes and vector(384) mappings are explicit. External snapshots follow ADR-0023's application-level trust; this is not an OS sandbox or native live-original guarantee.

Complexity hotspots merit **future measured assessment**, not immediate refactoring: `RagRetrievalService` 1,289 lines, `WindowsNativeFileGuard` 1,087, `RagOriginalBytesStore` 982, archived `RagQaSupervisor` 896, and `P/parsers/obsidian.py` 1,194. Callback/process/currentness responsibilities explain much of that complexity. This audit did not establish a safe equivalent simplification. Similar owner-specific search DTOs and finance entry-shape validation are not sufficient reason to introduce a generic framework or new shared module. No performance or duplication percentage is claimed.

### Module and repository inspection ledger

All rows include the recursive path/import/mapping inventory. Listed service reads were targeted, not exhaustive branch proofs. Java counts exclude module AGENTS files; root application/HTTP Java files add ten to these module totals.

| Module | Java files | Additional source read / actual depth |
| --- | ---: | --- |
| authentication | 59 | SecurityConfiguration, JwtProperties, SessionService, BootstrapService, PrivatePinService, token repository and redacted command/view DTOs; source review, no new Java execution. |
| settings | 25 | AppSettingsService singleton lock/refresh and defaults/validation entry points. |
| reference | 41 | ReferenceCatalogService ordered read-only catalogs and explicit public interface. |
| vault | 58 | VaultEntryService identity, edit guard/lock/source selection bounds; foundational public dependency ownership. |
| people | 64 | PersonService validated profile/shared Vault identity; source entry points and roles, not every membership race. |
| fiction | 84 | FictionService create/validation and public-source projection structure; detailed author/classification race behavior not rerun. |
| film | 102 | FilmCreditService reference/Vault creation; FilmSourceService projection/import relationships. |
| media | 61 | Upload/download/S3 configuration and adapter; HTTP streaming close/no-store behavior. |
| location | 99 | BusinessHoursService replacement lock/refresh and interval validation entry points. |
| knowledge | 159 | Parent facade/package boundary inventory and VocabularyService atomic SRS transition. Other nested business branches partial. |
| collection | 127 | Parent/nested module inventory; ShoppingService validated Vault-backed creation and state/price paths. Music/software internals primarily structural coverage. |
| account | 71 | FollowerSnapshotService validated historical snapshot creation and duplicate-collapse paths. Relationship concurrency primarily existing tests/source inventory. |
| feed | 84 | FeedItemService normalized supplied-batch ingestion; source/due service API inventory. No provider network fetch implementation assumed. |
| importdata | 52 | ImportJobService parse/decision/execute/rollback and strict JsonImportParser. |
| finance | 118 | FinancialTransactionService and recurring validation/lock paths, FinanceLockManager. No balance/FX/clock runtime claim. |
| journal | 28 | DiaryService verbatim Markdown, soft-delete filters and lock/refresh updates. |
| personal | 31 | PersonalProfileService unique self-profile conflict handling and lock/refresh update/delete/restore. |
| search | 19 | GlobalSearchService public fan-out/deterministic ordering and GlobalSearchQuery's 200-character/500-offset/100-limit bounds. |
| portability | 7 | Snapshot allowlist, read-only isolation and stream/export boundary. |
| rag | 163 | Default-off archive conditions, worker transport/process/currentness/snapshot/retrieval/incident/SQL and recovery boundaries; no native runtime or QA evaluation. |

`scripts/`, `.agents/` and `.codex/` were inventoried and the runner, hook/adapters, rules and current policies inspected. The runner's accepted 300/2,100-second caps, unique receipts, bounded stream capture and owned-tree termination remain intact in source. Historical native hook evidence applies only to its tested client/root/trust conditions. Current tools originate from a different installed client path (0.166.0); the audit did not rerun native interception and makes no claim that the current `functions.exec` surface is covered by the older Bash-hook evidence. No CI workflow directory is tracked; adding CI is an optional Owner decision, not a new architectural requirement.

## Preserved unfinished work and LOW/INFO backlog

### Suspended S19-2 — not remediation targets in this audit

The [last independent S19-2 review](../../phase-19/reviews/2026-10-29-s19-2-independent-final-re-review-3.md):15–27 and [forensic disposition](../../phase-19/s19-2-m10-read-only-forensic-report-2026-10-30.md):3–16 retain **F01–F05/F08/F09 OPEN**: three HIGH, four MEDIUM at that review. They cover changed-lifetime integration, strict protocol/runtime pins, no qualified candidate, evaluation provenance/preservation, unanswered-part safety, finite profile/resource proof and truthful traceability. F06/F07/F10 remain CLOSED. Later takeover changes are not independently accepted, so this audit does not assert each original reproduction still fails or silently close any finding from source inspection.

C1 **REJECTED**; C2 **NOT_QUALIFIED**; mandatory M10 **FAIL**, precise root cause **NOT_VERIFIED**. Antigravity **3/3**, historical resource corrections **3/3**, C2 starts **2/2**, infrastructure restart **1/1** remain consumed. Takeover 1 remains incomplete/stopped; independent corrective passes **0/3**. The archive includes new QA APIs/DTOs/services, Python `grounded_qa.py`/`qa_generation.py`, optional runtime locks/dependency extra, provisioning script, native QA/evaluation tests and synthetic fixtures. None is runnable under this audit permission. Shared retrieval/recovery/ingress archive deltas are acknowledged; their absence of a QA bean is not independent acceptance of the deltas' complete behavior.

### Existing backlog — retained IDs, no automatic closure

| ID / retained status | Evidence and scoped recommendation |
| --- | --- |
| AUTH-CLOCK — OPEN, separate Authentication/environment issue | [Owner exception](../../phase-17/owner-decisions.md):3–7, exact `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary` Windows/WSL2 mixed-clock case. Not reproduced/repaired; a historical passing run does not close it or waive other failures. Owner decides any separate diagnosis before a future broad gate. |
| MA-A03 — LOW/OPEN | [Maintenance A final re-review](../2026-10-25-post-phase18-simplification/reviews/2026-10-26-maintenance-a-status-contract-final-re-review.md), preserved backlog: Git/ZIP inventory and attribution of five tracked helper deletions. Correct attribution in a new note only if selected. |
| MA-A04 — LOW/OPEN | Same backlog: current/legacy phase-final result vocabulary. Normalize prospective templates if selected; preserve old result truth. |
| MA-A05 — LOW/OPEN | Same backlog: “both” report paths in three-round takeover wording. No reset or new round inferred. |
| MA-A06 — LOW/OPEN | Same backlog: harness summary overstates rule propagation. Correct only from actual changed-file evidence. |
| B1-L01 — LOW/OPEN | [B1 takeover review](../2026-10-26-watchdog-tooling/reviews/2026-10-26-b1-independent-takeover-final-review.md):21; stale status/attribution and swapped historical severities. The current runner skill's accepted-status banner is already corrected; that alone does not close the full finding. |
| B2-L01 — LOW/OPEN | [B2 re-review 3](../2026-10-26-watchdog-tooling/reviews/2026-10-26-b2-independent-re-review-3.md):46 and final aggregation: genuine native event/client correlation versus four-row component trace. No stronger force-ask guarantee. |
| DOCB-L01 — LOW/OPEN | [Cleanup B acceptance](../2026-10-27-repository-doc-sync/reviews/2026-10-27-cleanup-b-independent-final-review.md):19; historical manifest self-row NEW/MODIFIED classification. Publication does not rewrite what the intake operation was. |
| DOCB-L02 — LOW/OPEN | [Publication-closeout review](../2026-10-27-repository-doc-sync/reviews/2026-10-27-cleanup-b-publication-closeout-review.md):19; distinguish published B/B3 closeouts from the subsequent cleanup closeout. |
| S19-1 L02 — LOW/OPEN | [Accepted S19-1 review](../../phase-19/reviews/2026-10-28-s19-1-independent-codex-corrective-attempt-4-final-review.md):27. Inventory reconfirms `J/rag/internal/web/` is the sole module Java package directory without `package-info.java`. Nonblocking descriptor hygiene; no new module needed. |
| S19-2-L01 — LOW/OPEN, archived | [S19-2 review](../../phase-19/reviews/2026-10-29-s19-2-independent-final-re-review-3.md):27; additive package/optional QA setup inventory. Any future note must say archival/not accepted. No installation or QA resumption follows. |

These are ten retained LOW IDs plus AUTH-CLOCK, not ten newly proven code faults. B3-L01 remains CLOSED under the cleanup acceptance. No separate current INFO finding was identified in the inspected controlling backlog records. This is not a re-adjudication of every OPEN word in immutable historical reviews.

## Coverage ledger — all 29 dimensions considered

`VERIFIED` applies only to the stated finite question. `PARTIAL` means source/limited tests or historical evidence covers part of the dimension. It does not mean all mandatory production behavior passed. Totals: **5 VERIFIED, 22 PARTIAL, 1 NOT_VERIFIED, 1 NOT_APPLICABLE**.

| # | Dimension | Status | Evidence / practical limit |
| --- | --- | --- | --- |
| 1 | Owner intent / non-goals | VERIFIED | Exact local/remote baseline, full audit decision, ACTIVE/suspension/archive boundaries; report-only action. |
| 2 | Requirement traceability | PARTIAL | Accepted P17/P18/S19-1 versus archived S19-2 separated; no every-method acceptance remap across all backend phases. |
| 3 | Architecture / ADR / diagrams | PARTIAL | Matrix/ADRs/source checked; CSA26-002/003 drift; diagrams not regenerated or accepted as proof. |
| 4 | Module ownership / dependencies | VERIFIED | Entire lexical import graph: 41 allowed edges, acyclic, no cross-internal imports. Runtime Modulith verification not rerun. |
| 5 | Repository / package structure | VERIFIED | Recursive module/package/path inventory; canonical root types and existing L02 gap. Behavior of every class not certified. |
| 6 | Shared HTTP / exception ownership | VERIFIED | Single ADR-0016 root envelope/error family and module-local adapters; no competing root/common module detected. Wire execution partial under #11. |
| 7 | SOLID / cohesion / coupling | PARTIAL | Targeted orchestrator/port/facade/service review; hotspots identified without speculative refactor. |
| 8 | DRY / KISS / invariants | PARTIAL | Canonical parser/source/HTTP reuse and state guards; no exhaustive semantic-duplication analyzer. |
| 9 | Pattern fitness | PARTIAL | Real storage/process/module boundaries retained; no blanket single-implementation-interface finding. |
| 10 | Domain correctness | PARTIAL | Module ledger, finance/SRS/import/soft-delete source paths; no fresh Java business suite. |
| 11 | REST / OpenAPI / pagination | PARTIAL | DTO/root error/bounds review; accepted 230-route test manifest inspected, archive MVC receipt read; no fresh generated OpenAPI. |
| 12 | Input validation | PARTIAL | 42 synthetic Python cases, source byte/schema/path/span limits; no full fuzz/adversarial Java suite. |
| 13 | Auth / privacy / consent | PARTIAL | Security/secret DTOs, scopes, role/default-off source checks; no fresh JWT/SQL/reader race execution. |
| 14 | Secrets / retention / egress | PARTIAL | Safe logging/loopback/receipt/ignore/lock source review; no full secret scanner, firewall or backup audit. |
| 15 | Transactions / concurrency | PARTIAL | Finance/SRS/import/source reservation lock/finalization paths and old receipts; no fresh DB race tests. |
| 16 | Failure / recovery / operations | PARTIAL | Media uncertainty, native owned-tree and retrieval recovery source; no crash/restore/native probe. |
| 17 | DB / JPA / Flyway / queries | PARTIAL | 69-table-name match, 83 mapping declarations, 24 migrations and V24 guards; columns/privileges/plans/live schema not freshly verified. |
| 18 | Complexity / resources | PARTIAL | Finite parser/ingress/search/process limits and source hotspots; no current host/production load measurements. |
| 19 | Retrieval / index / answer quality | PARTIAL | Accepted S19-1 source/provenance and historical receipt; no new retrieval execution; no accepted answer generator. |
| 20 | Document / multilingual fidelity | PARTIAL | Current prepared-MD/parser/chunker tests and source; VI/Han full producer measurement not rerun. OCR/PDF/Office are excluded, not failed current requirements. |
| 21 | Security tests | PARTIAL | Synthetic ingress/authority/duplicate/payload negatives and 21 hook units pass; native/SQL/privacy races unrun. |
| 22 | Test sufficiency / determinism | PARTIAL | 63 fresh focused tests total; existing targeted concurrency/HTTP tests inspected; CSA26-001 prevents safe default full verification. |
| 23 | Performance benchmarks | NOT_VERIFIED | No fresh p50/p95, cold/warm startup, throughput, RAM/VRAM or production-volume test; historical numbers remain historical. |
| 24 | New semantic evaluation science | NOT_APPLICABLE | No newly authorized QA/catalog/OCR qualification; preserved C1/C2/provenance failures inspected, no retuning or requalification. This does not mark archived evaluation PASS. |
| 25 | Dependencies / supply chain | PARTIAL | POM/BOM, uv lock/runtime metadata and optional-extra separation inspected; no current CVE/license/SBOM/native-DLL attestation. |
| 26 | Build / static tools / CI | PARTIAL | AST/format pass; lint fails; Mypy optional-runtime limitation; no Java compile/static/full-suite pass or CI claim. |
| 27 | Logging / observability | PARTIAL | Category-only errors, redacted DTOs, safe receipts and stream cleanup source; no end-to-end private-payload log capture. |
| 28 | Docs / governance | PARTIAL | 411 path links resolve; CSA26-002/003 and retained backlog; external URLs/anchors/history not exhaustively validated. |
| 29 | Hygiene / preservation | VERIFIED | Tracked-clean baseline/hash preservation, unique ignored diagnostics, no cached/binary build artifacts tracked, only two report writes. No exhaustive old ignored-log rehash asserted. |

## Owner disposition proposals and Docs Sync Check

| Priority | Proposed bounded action; no execution authority granted |
| --- | --- |
| FIX_BEFORE_FRONTEND | Select CSA26-001 for a small test/build-isolation remedy and non-generative proof. A later accepted-only regression selection can then be reviewed/authorized without resuming S19-2. |
| KEEP | Accepted backend/API/domain boundaries, inactive indexing, disabled live originals, default-off QA and all failed/unknown historical evidence/counters. Keep current directory names; see the companion inventory. |
| DEFER | Consolidate CSA26-002/003/004 and selected existing LOW items once; do not require a LOW-only correction loop or blanket framework rewrite. New diagrams are proposals. |
| OWNER_DECISION_REQUIRED | Decide selected remediation and whether/when Phase 16 preparation may start. Browser token transport, private-mode UI lifetime, origin/CORS and rendering/download policy remain frontend/deployment design decisions under existing contracts. No implementation choice or deployment permission is made here. |

Automated social-media image/video downloading, fiction copying/backup, and Hermes/OpenClaw integration are **dropped**, not pending audit requirements. Google Drive and Ideaverse remain product-design candidates only. No source synchronization, live Ideaverse reads, OAuth integration, new module or frontend scaffold was created.

**Docs Sync Check:** `UPDATED` — only these two new audit artifacts. `ALREADY_CONSISTENT` — canonical ACTIVE, suspension/archive decision, accepted historical closeouts and current source guards. `DEFERRED_OUT_OF_SCOPE` — CSA26-002/003 current-navigation/diagram drift and listed LOW backlog. `NOT_VERIFIED` — current full runtime gates, performance/security-scan coverage, M10 root cause and operational readiness. `BLOCKED_OWNER_DECISION` — any remediation/frozen-artifact change or frontend authorization. No canonical document was silently corrected and no acceptance/counter changed.

**Final result: `AUDIT_REPORTED / OWNER_REVIEW_REQUIRED`.** Both required reports are complete; selected remediation and frontend authorization remain solely with the Owner.

Next step: Owner | review docs/implementation/maintenance/2026-11-01-current-system-readonly-audit/{audit-report.md,architecture-diagram-gap-inventory.md} and authorize only selected remediation | audit-only; no implementation | no counters reset
