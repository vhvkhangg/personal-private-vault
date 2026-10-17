# Phase 18 — Independent Codex pre-handoff review

- **Date:** 2026-10-17
- **Verdict:** **CHANGES_REQUESTED**
- **Baseline:** `main` / HEAD / `origin/main` = `86b08a1a44d7672b111af1977098ef6c4c3e800a`. Read-only `git ls-remote origin refs/heads/main` independently returned the same SHA.
- **Candidate:** 11 modified tracked files plus the five new Phase 18 preparation files listed below, before this review's status/report edits. No Phase 18 production implementation or handoff exists.
- **Authority:** `$codex-pre-handoff-review`, shared `codex-review-coverage`, P18-01–P18-10 preparation intent and the proportional RAG delivery policy.
- **Independence:** Codex reviewed ChatGPT's preparation. No Antigravity implementation/evidence was supplied. This is preparation review zero, **not** an implementation remediation round.

The predecessor and authorization prerequisites pass: Phase 17 was accepted, owner committed/pushed and closed/frozen; ACTIVE is `NO_ACTIVE_HANDOFF`; Phase 18 preparation is owner-authorized; Phase 16 remains deferred. A historical 3/6/9/12 milestone is not the immediately preceding RAG gate. Phase 15 is not reopened.

The requested formats/languages and one-pipeline approach are reasonable as sequential slices inside one phase. The blockers concern the contract needed to implement that scope, carried-forward quality acceptance, executable verification and applicable operating instructions. Missing not-yet-implemented runtime results are **not themselves preparation failures**.

## 1. Findings

### P18-PH-01 — High / blocking: the required frozen-contract amendment is still a question, not a reviewable contract

**Evidence:** `owner-decisions.md:10–11,30–32`, `acceptance-and-evidence.md:12,24–29`, `preparation-review.md:20`. These correctly acknowledge that an exact amendment is required, but delegate its substance to the reviewer/eventual handoff.

The actual baseline is narrower than “multi-file publication in one transaction” suggests:

- `RagOperations.java:36–46` and `RagJobService.java:92–134` admit **one root**; descendants are its dependency closure, not independent selected roots. `RagSourceAuthority.java:59–70,133` rejects external files and constructs a version-2 single-root manifest.
- `V15__rag_single_job_and_sealed_worker.sql:114–160` validates that exact root/closure and only Fiction/Film source proofs. `RagManifestScope` interprets manifest identities as Vault IDs. File consent/history/reservations cannot safely be added by casting a path/alias to a Vault ID.
- `rag-engine/src/ppv_rag/contracts.py:23–55`, V7's `stage_worker_receipt` and V15's staging functions permit bounded **synthetic digest receipts**, not document chunks or embeddings. `V3__rag_foundation.sql:72–86` defines generation metadata, not a chunk/vector store.
- `RagFinalizationGate.java:49–56,72–99` expects one receipt and returns/publishes one generation. `RagGenerationState.java:42–45` derives generation freshness from the **job-wide** lease manifest. `RagRecoveryService.java:101–106` and `RagPurgeService.java:171–198` invalidate/clean by job. Simply adding multiple generations would couple unrelated files' freshness and cleanup.
- `RagLimits` caps selected inputs at 100 sources / 1 MiB; admission caps the work lease at 15 minutes. These are reviewed Foundation ceilings, not measured document-ingestion limits or permission to raise them incidentally.

**Consequence:** the implementer would have to invent the multi-root scope, external authority, content-storage/worker contract, generation-specific dependency/cleanup semantics and possibly new limits while modifying a frozen baseline. The current documents explicitly forbid doing that.

**Smallest correction:** prepare a short proposed Phase 18 ADR and additive logical/API delta using the finite proposal in section 2. Obtain the owner's explicit approval of that exact reviewed delta, then synchronize affected canonical inventories. Preserve historical ADR-0020/P17 evidence; append migrations **after V17**, rather than editing V1–V17 or treating old “after V13” planning language as the current migration tip. No Java module edge or Schema v1 change is necessary merely to add RAG-owned file/index state.

**Required future proof:** mixed independent successes/failures; failed-child propagation; all-failed/cancelled job; injected rollback and lost finish acknowledgement; stage writer versus seal in both orders; native stop; exact whole-set release in the same physical COMMIT; unrelated file A stays CURRENT after unrelated B changes/is revoked, while a true dependent generation denies and purges. Live-file enablement additionally requires actual R7 proof through physical COMMIT. These are focused changes to existing boundary tests, not a replacement job engine or a replay of all 420 Foundation benchmark jobs.

**Owner impact:** contract decision required; no amendment is approved or applied by this report.

### P18-PH-02 — Medium / blocking: carried-forward independent parser/OCR evaluation and a decisive fidelity oracle are missing

**Evidence:** frozen `phase-17/benchmarks-and-acceptance.md:64–72` makes P17-N1 blocking when parser/OCR/ingestion quality first appears. Phase 18 `acceptance-and-evidence.md:7–11` requests representative fixtures/ground truth, but never requires source-grouped tuning versus held-out separation, disjointness, dataset/config hashes or complete denominators. Its AC-04 permits an unknown locator to be “quality-qualified”, while `README.md:13` and AC-02 require missing **mandatory** locators to fail the file.

**Consequence:** the same documents can be used to tune and certify OCR/chunking, and “material” failure versus a publishable warning lacks a sufficiently precise acceptance oracle. A warning could be interpreted as permitting a mandatory missing locator.

**Smallest correction:** add a compact P17-N1 ingestion-only evidence contract. Before comparative tuning, version a small sanitized source-grouped development/held-out manifest, keep transcluded/near-duplicate sources in the same group, record reference text/table/locator labels and adjudication instructions, and require disjointness plus coverage/denominators by introduced format/language. Define mandatory versus optional locators per format and an initial quality/admission policy; freeze the measured numeric defaults before the final acceptance run. Unknown/fabricated mandatory provenance fails; an optional unavailable locator may carry an explicit warning. State how runtime heuristics flag uncertainty rather than claiming to detect every possible OCR error.

**Required future proof:** the small held-out run with actual errors/failures/skips/pending cases, mandatory-locator rejection, and a source-group disjointness check. Calibration of a local LLM judge is **N/A for Phase 18 because no judge is introduced**; deterministic comparisons and human labels suffice here. Retrieval/no-answer/answer/recommendation scoring remains Phase 19/20 scope. No fixed corpus quota or broad ML evaluation platform is requested.

**Owner impact:** restores an existing accepted obligation; changing/deferring P17-N1 instead requires an explicit owner-approved amendment.

### P18-PH-03 — Medium / blocking: the one final verification gate is not named or executable

**Evidence:** `acceptance-and-evidence.md:18` leaves the complete command to “the accepted handoff”; `README.md` promises one final integration gate but does not identify it. The active skill requires one named gate at preparation, with bounded focused slices.

**Smallest correction:** name **P18-FINAL** and record its finite command set, working directories, receipts and failure policy. Section 5 supplies a concrete minimal proposal. Link the focused R7/subset-publication/offline/quality/resource receipts into this gate; do not pretend Maven alone runs Python/model acceptance. Use actual `COMPLETED`, `ABORTED`, `CANCELLED`, `RECOVERY_REQUIRED` semantics, rather than suggesting `SUCCESS`/`FAILED` are existing DB states (`acceptance-and-evidence.md:21`; V15:18–23).

**Required future proof:** each named component exits zero with factual test/skip counts and the required changed-boundary receipts. An unrun/failed component is not a passing overall gate. The old Phase 17 AUTH-CLOCK exception is not automatically a Phase 18 waiver.

**Owner impact:** ordinary verification synchronization within the approved scope; no extra final suite per internal slice.

### P18-PH-04 — Medium / blocking: applicable current operating documents contradict the frozen predecessor and later-phase route

**Evidence:** `backend/AGENTS.md:16–28` says Phase 17 awaits owner commit/freezing, calls its current status `READY_FOR_OWNER_COMMIT`, and forbids later phases/new handoffs. `rag-engine/README.md:1–9` presents unresolved October 15 ownership races as current. The root instructions/preparation correctly say Phase 17 is frozen at `829d294` and closeout `d02ed14`, with Phase 18 preparation authorized. These scoped/current files were omitted from synchronization targets, although `preparation-review.md:15` explicitly requires applicable existing instructions.

**Smallest correction:** synchronize only the current-status/authority paragraphs in those two operational files: accepted frozen Foundation; default containment/live-file denial retained; only a separately accepted active handoff may authorize Phase 18 changes; separate Codex takeover follows its existing policy. Label old runtime descriptions as historical where retained. Preserve historical reviews/receipts and do not bulk rewrite all old ADR status prose. The scoped test instruction's full-suite retry count and the phase-wide two remediation rounds measure different events; no new test-retry mechanism is required.

**Required proof:** compare root/scoped AGENTS, worker README, phase status and selected implementer/skills against the same accepted authority and `NO_ACTIVE_HANDOFF`. Documentation/link review is sufficient; no Maven rerun is needed for this repair.

**Owner impact:** no new implementation authorization or baseline behavior change.

## 2. Finite proposed owner decision — D18-A (not approved)

Recommended intent: **retain all P18 formats/languages and approve one bounded Phase 18 extension with the following exact behavioral delta**, after preparation records its additive logical representation and compatibility details. This is a review proposal, not an implementation handoff or an edit to frozen contracts.

1. **Scope/admission:** one manual job may select a finite explicit set of individually approved roots. Capture immutable selected roots and the union of their independently approved dependency closures; reserve the whole union atomically before one-use input. Keep one active job and one supervised worker. Never model unrelated files as artificial transclusion dependencies.
2. **Source authority:** existing Vault-backed sources keep Vault-owned history/reservation APIs. External file identity/version/consent and reservation metadata stay RAG-owned; they are not fake Vault entries. RAG-owned file policy mutations participate in the same ordered reservation/finish protocol. No new cross-module dependency or worker business/Vault read permission. Approve a documented deterministic order for mixed source guards before implementation.
3. **Live file lifetime:** live external files remain disabled until independent R7 acceptance. The permitted path is local NTFS only, exact granted file/version/body/metadata, protected identity/root/ancestor/reparse/hard-link and pre-existing writer/mapping checks, guarded consumed bytes and final validation through actual Java physical COMMIT. No reopen-by-arbitrary-worker-path or rehash-and-close substitute. Unsupported/unprovable paths deny; immutable copies are separately identified sources, not proof of live-file freshness. Record bounded reads/hashes outside SQL lock intervals and handle retention/release on rollback and unknown outcomes.
4. **Worker/storage extension:** version the existing job-scoped envelope to carry only approved bounded document input and typed per-file results/chunks/vectors/locators. Add only RAG-owned logical storage needed for immutable per-file outcomes, generation-specific manifests and content/vector rows with fixed selected dimension/model/tokenizer/parser identities. Keep restricted audited stage functions, actual worker provenance, same attempt-row fence through staging COMMIT, no direct unrestricted DML/publish/grant/release rights. Source bodies/chunks/vectors and private hashes are sensitive; extend deny/purge/temp cleanup to the new content-bearing storage. A digest-only Foundation receipt is not a complete document index.
5. **Per-file generation lifetime:** each successful file generation binds its own actual root/dependency/field/version closure, separately from the job's whole reservation union. Fresh CURRENT and post-completion purge follow actual generation dependencies. Failure/revocation of unrelated B must not revoke A; failure of a true transcluded child invalidates its dependent candidate. Failed file replacement never overwrites its previous independently CURRENT generation.
6. **One atomic finish:** after positive task/process-tree stop and writer drain, one trusted Java physical transaction seals the attempt, records outcomes, publishes **all validated successful generations or none**, terminalizes the job and releases the exact entire reservation set. Shared trust/fencing/cancel failures publish none. Proposal: known finish with at least one success maps to `COMPLETED`; zero successes to `ABORTED`; cancellation to `CANCELLED`; unknown process/SQL outcome remains nonterminal `RECOVERY_REQUIRED`. `PARTIAL_SUCCESS` is report-only. No early per-file publication, auto-resume, auto-finalization or new DB job enum.
7. **Compatibility and limits:** preserve the existing single-root Foundation API behavior; describe an additive typed batch/report API and versioned worker/manifest compatibility, within existing `rag/api`, `rag/source`, module-local web and `ppv_rag` ownership. Append migrations after V17 and synchronize a separate additive Phase 18 DBML/API inventory. Maintain existing ceilings unless a listed, measured Phase 18 limit change is explicitly approved; do not confuse command watchdogs with execution/admission limits. Do not add a global slot, new scheduler, queue, cross-module repository or generic plugin framework.
8. **Unchanged guarantees:** per-item/field consent, Private/NSFW/unknown exclusions, exact child attribution, no source write-back, manual reindex only, positive native stop, SQL COMMIT fences, E1, independent completed-index lifetime and Practical Local Offline remain binding. No Phase 19–22, frontend, Ideaverse, deployment or blanket containment-removal permission.

**Finite choice:** approve the reviewed D18-A contract once represented in proposed canonical docs, or request a specific correction while keeping implementation blocked. Do not silently fall back to per-file early commits or drop authorized formats/languages. If R7/ABI/resource feasibility fails later, return the concrete failed condition for a bounded owner decision; do not invent another infrastructure program.

## 3. Proportionality and dependency disposition

| Feature/API/dependency/evidence | Disposition | Reason and smallest proof |
| --- | --- | --- |
| All requested TXT/MD/Obsidian/DOCX/native+scan PDF/XLSX/XLS formats and VI/EN/Simplified+Traditional Han | **KEEP** | P18 owner scope. Sequential format slices, sanitized positive/unsafe fixtures; no separate phase/review per format. |
| One normalization representation/structure-first chunker | **KEEP / SIMPLIFY** | Stable source spans/table grouping; tokenizer-sized chunks. Parser-specific functions only where needed; no universal semantic chunking service. |
| Native-text-first PDF, targeted OCR, at most one justified region retry | **KEEP** | P18-06/07. Text/table/box labels; native text must not be duplicated with OCR. |
| DOCX, openpyxl/xlrd, Markdown AST candidates | **KEEP as candidates** | Check archive expansion, comments/hidden/unapproved content, formula cached-value semantics, encoding/offsets and no external relationships fetch. No installed dependency is implied. |
| Docling-like layout versus Tesseract/Paddle shortlist | **SIMPLIFY** | Establish one viable offline baseline; compare a second candidate only to close a measured language/table defect. No obligatory installation of every OCR stack. |
| One multilingual embedding model, at most one same-model CPU fallback | **KEEP** | P18-01/08. Two candidates at most where useful; artifact hash/dimension/tokenizer/finite-vector checks and bounded local sanity cases. No retrieval platform. |
| Multi-root admission, per-file outcomes and one final Java publication | **OWNER DECISION REQUIRED** | P18-PH-01 / D18-A; reuse job/attempt/worker and one physical finish. |
| R7 external-file authority/OS guard | **KEEP / OWNER DECISION REQUIRED on exact delta** | New live capability cannot use disabled-path evidence. Focused Windows/NTFS physical-C tests; no WFP/firewall project. |
| RAM/VRAM/CPU/time/temp limits | **KEEP / SIMPLIFY** | Owner's 16 GB RAM / 8 GB VRAM; sequential OCR/embedding loads and small realistic local workload, separate process-tree RAM/host headroom and VRAM peaks. Freeze finite measured caps before acceptance. No invented latency SLA or arbitrary p95 from tiny samples. |
| Mixed file success sample, fault/recovery and worker-role tests | **KEEP** | AC-06/07; existing tests extended only where touched. The proposed 8-success/2-fail example is one fixture, not a workload benchmark. |
| Held-out parser/OCR labels and per-format/language results | **KEEP / SIMPLIFY** | Existing P17-N1; small source-grouped dataset and deterministic/human evaluation, no LLM judge in Phase 18. |
| Small structured report / generation versus CURRENT distinction | **KEEP** | P18-10; sanitized reasons, outcomes, version/provenance/resource measurements. No raw paths/text in operational logs. |
| Retrieval/hybrid/ANN/reranker, QA/no-answer/judge, catalog-ID ranking | **DEFER** | Phase 19/20; locator preservation and vector validity are the Phase 18 responsibilities. |
| 420-job repeat, 29 benchmark builds, enterprise resilience, global WFP/DNS controls | **REJECT** | No changed current owner requirement; retain real changed-boundary safety proof. |
| Extra Python agent, new hooks, distributed workers/queue/model registry/dashboard | **REJECT** | Existing implementer, Python/RAG skills and watchdog are sufficient. No material new role/permission boundary. |

**Primary-source compatibility check, 2026-10-17:** package publisher metadata was read, without installs, downloads of model artifacts or lock changes. Observed release candidates: [Docling 2.140.0](https://pypi.org/project/docling/2.140.0/), [PaddleOCR 3.7.0](https://pypi.org/project/paddleocr/3.7.0/), [PaddlePaddle 3.3.1](https://pypi.org/project/paddlepaddle/3.3.1/), [torch 2.14.1](https://pypi.org/project/torch/2.14.1/). The inspected PaddlePaddle release lists Windows wheels but no CPython 3.14 Windows wheel; the inspected torch release does list a `cp314-cp314-win_amd64` wheel. This is **not** proof of transitive OCR/CUDA/RTX 2070 compatibility or license completeness. Do not force Paddle or downgrade the pinned interpreter merely to run a second comparison. Pure Python candidate metadata was also checked for python-docx 1.2.0, openpyxl 3.1.5, xlrd 2.0.2, markdown-it-py 4.2.0 and sentence-transformers 6.1.0. None is selected/pinned by this report.

**Measured versus proposed:** the accepted Python baseline is 3.14.4, FastAPI 0.143.0/Uvicorn 0.54.0/Pydantic 2.14.1/Psycopg 3.3.6. No OCR/embedding dependency is presently in its runtime list. ABI metadata is preliminary selection evidence only. Small Windows model/OCR load, language/table fidelity and CPU/GPU resource measurements remain mandatory future selection gates before promotion; exact versions/hashes/limits are not inferred from package advertising. ADR-0013 does not authorize incidental Java/Python/DB upgrades.

## 4. Review inventory and 29-dimension coverage

**All candidate changes inspected:** `.agents/README.md`, `.agents/agents/backend-implementer/agent.md`, `.agents/skills/antigravity-implement-handoff/SKILL.md`, `.agents/skills/codex-create-handoff/SKILL.md`, root `AGENTS.md`, `README.md`, `docs/README.md`, `docs/implementation/README.md`, `docs/implementation/rag-phase-plan.md`, `docs/roadmap.md`, `scripts/test_run_bounded_command.py`; new `phase-18/{README.md,owner-decisions.md,acceptance-and-evidence.md,preparation-review.md,reviews/README.md}`.

**Applicable existing governance read:** `docs/AGENTS.md`, `backend/AGENTS.md`, `backend/src/test/AGENTS.md`; Vault/Fiction/Film scoped instructions; both `.agents/agents/` roles; `.agents/skills/{codex-pre-handoff-review,codex-review-coverage,codex-create-handoff,codex-final-review,codex-implementation-takeover,antigravity-implement-handoff,python-rag-coding,local-rag-engineering,bounded-command-execution,graphify-context,architecture-change,modular-monolith-architecture,backend-testing,database-migration-review,java-spring-coding-standards,pragmatic-solid-design,reuse-and-consistency,design-pattern-selection,jpa-postgresql-persistence,rest-api-http-contracts}/SKILL.md`, handoff templates; `.agents/rules/{agent-handoff-workflow,architecture-change,backend-python,backend-java,package-organization,tests,security,context-efficiency,code-reuse-and-patterns,database-migrations,documentation}.md`; `.agents/hooks.json`, safety hook and tests; both `.codex/` files; bounded runner and scripts README. The takeover path exists and requires separate implementation/reviewer sessions after the two actual remediation rounds. No agent was spawned for this review.

**Canonical/historical authority inspected:** ACTIVE; Phase 17 closeout/final independent acceptance, P17-1/P17-2/P17-3 and P17-N1 benchmark clauses; ADR-0013/0016/0019/0020; RAG architecture, module matrix, package tree, API inventory, Phase 17 additive DBML and relevant Structurizr relationships; RAG plan, scope simplification review, proportional-delivery policy, owner-phase workflow and prompt placeholder. Schema v1 is unaffected and is not a remediation target. Irrelevant historic feature-phase internals and their domain skills/rules are **N/A**; no authorized change touches those domains. Phase 15 audit/milestone tools, authentication remediation, frontend, artifacts/design skills and deployment are **N/A**. The architecture-auditor remains available; no new custom role is necessary. A dormant prompt placeholder is not executable work.

**Source/build/test inspection:** recursively inventoried all 69 Java RAG files, 29 Java RAG test/support files, eight Python source files and seven Python test files, excluding caches. Inspected actual authority/admission/finalization/current-state/manifest/purge/recovery/grant/supervisor/native-file/process boundaries and their SQL constraints; source/job public DTOs and module-local HTTP envelope/error mappings; synthetic worker/contracts/transport/config; selected reservation/staging/owner-concurrency/lost-ack/R7/ingress/contract test scenarios. Inspected V3's storage plus relevant V4–V17 authority additions, `backend/pom.xml`, runtime/compose configuration, `rag-engine/{README.md,pyproject.toml,.python-version,uv.lock}`. This is not a full independent production re-audit. Graphify navigation used observed vocabulary `rag source commit guard index job publication transaction`; broad results were only navigation, verified against source.

All 12 Java RAG package descriptors are present; no `.gitkeep` exists in those reviewed source/test trees. Imports retain only Vault/Fiction/Film edges plus existing root HTTP types. Existing flat `rag/api` is a frozen small contract; adding a speculative package hierarchy/shared transport module is unwarranted. Future Python parser/chunker/embedding responsibilities should stay inside `ppv_rag`, grouped only as actual code warrants.

Statuses below assess **preparation coverage**, not unimplemented runtime success. `NOT_VERIFIED` runtime obligations are explicitly retained in section 5.

| # | Consideration | Status / evidence |
| --- | --- | --- |
| 1 | Owner intent/non-goals | **PASS** — P18 decisions/README retain formats/languages; later product scope excluded. |
| 2 | Requirement traceability | **FINDING** — PH-01/02/03 must bind decisions to contracts and decisive evidence. |
| 3 | Architecture/baselines | **FINDING** — PH-01 exact additive contract; existing topology preserved. |
| 4 | Ownership/public boundaries | **FINDING** — external versus Vault authority and batch scope need PH-01; no new edge required. |
| 5 | Folder/package structure | **PASS** — recursive inventories/descriptors; existing capability boundaries reusable. |
| 6 | Shared API/exception placement | **PASS** — ADR-0016 root envelope and RAG-local advice; no duplicate envelope proposed. |
| 7 | SOLID | **PASS** — reuse authority/lifecycle, separate actual parsing/chunking/embedding responsibilities; new implementation quality remains unverified. |
| 8 | DRY/KISS/YAGNI/invariants | **PASS** — one pipeline/chunker/model, no duplicated orchestrator. |
| 9 | Patterns/anti-patterns | **PASS** — minimal real format adapters; plugin/queue/registry rejected. |
| 10 | Domain/status/lifetime | **FINDING** — PH-01 terminal/report mapping and independent per-file CURRENT/cleanup. |
| 11 | API/wire compatibility | **FINDING** — PH-01 additive batch/report and versioned content worker contract required. |
| 12 | Input validation | **PASS** — AC-01/03/07/08 specify unsafe format/path/archive/field/dependency bounds; new native implementation not certified. |
| 13 | Consent/privacy/eligibility | **FINDING** — PH-01 must concretize live-file authority without weakening inherited exclusions. |
| 14 | Secrets/egress/cache/purge | **PASS** — offline/no-download/redacted evidence required; new content storage must join PH-01 cleanup. |
| 15 | Atomicity/locks/COMMIT | **FINDING** — PH-01; one physical finish retained, multi-file proof not implemented. |
| 16 | Recovery/cancel/operations | **FINDING** — PH-01 addresses job-level baseline versus per-generation purge/lost-C outcomes. |
| 17 | Database/schema/privileges | **FINDING** — PH-01 additive schema/functions after V17; worker scope remains least privilege. |
| 18 | Complexity/resource bounds | **PASS** — separate 16 GB RAM/8 GB VRAM, incremental-where-supported and measured caps in AC-08; runtime peaks NOT_VERIFIED. |
| 19 | Index/retrieval quality | **PASS** — AC-05 finite vectors/model identity; retrieval/ANN/recommendation metrics **N/A in Phase 18**. |
| 20 | OCR/document fidelity | **FINDING** — PH-02 held-out labels and mandatory/optional locator distinction. |
| 21 | Security tests | **PASS** — AC-03/06/07 require real changed DB/NTFS/process boundaries, negative grants and deterministic races. |
| 22 | Test quality | **FINDING** — PH-02 independent oracle and PH-03 complete gate; no test-count target. |
| 23 | Benchmarks | **PASS** — small target-hardware selection/resource tests, truthful sample sizes, no repeated Foundation scale obligation. |
| 24 | Evaluation science | **FINDING** — PH-02; LLM judge/calibration is **N/A here** unless later introduced under a new approved scope. |
| 25 | Dependencies/lifecycle | **PASS** — exact baseline/pins inspected, primary candidate metadata checked; lock/artifact/license/ABI/driver proof required before use. |
| 26 | Build/tooling/diagnostics | **FINDING** — PH-03; native watchdog 5/5 and safety 13/13 pass. IDE warnings **NOT_VERIFIED**, no IDE-clean claim. |
| 27 | Logging/reporting | **PASS** — AC-08/P18-10 sanitized outcome/resource report, no private paths/raw text. |
| 28 | Documentation/governance | **FINDING** — PH-04 current operating drift; PH-01 proposed canonical synchronization. Changed Markdown local file targets resolve. |
| 29 | Hygiene/delivery | **PASS** — candidate diff whitespace check passes; no production/dependency/frozen artifact changed by this review. Generated receipts remain ignored. |

## 5. Executed verification and required later gates

**Executed on native Windows, CPython 3.14.4** using the existing `rag-engine/.venv/Scripts/python.exe` and the `bounded-command-execution` wrapper:

| Check | Actual result |
| --- | --- |
| `scripts/run-bounded-command.py --timeout-seconds 45 --receipt logs/phase18-pre-handoff/watchdog-windows.json -- rag-engine/.venv/Scripts/python.exe scripts/test_run_bounded_command.py -v` (wrapper itself launched by the same Python) | **5/5 PASS**, exit 0, 6.262 s test time; native parent/child/grandchild timeout termination and unrelated sentinel survival. |
| Same wrapper, 30 s cap, `--receipt logs/phase18-pre-handoff/safety-hooks.json -- rag-engine/.venv/Scripts/python.exe .agents/hooks/test_repository_safety.py -v` | **13/13 PASS**, exit 0. |
| `git diff --check` | PASS. |
| Changed/new Markdown local file-target existence; `.codex/config.toml` parse | PASS. Anchor/remote-link crawling was not claimed. |
| `git ls-remote origin refs/heads/main` | Matches baseline SHA; no fetch/commit/push. |

`py -3` is unavailable on this host's PATH; the existing native project interpreter was used successfully. No Python reinstall/config override is required. The preparation's Linux 5/5 claim is supplied historical evidence, not relabeled as this Windows run.

Integrity references (SHA-256): watchdog test `5e867559a99f1c58384080b5a97c9a609585263985f07af5cdf49548c59ef586`; bounded runner `e9643978b8dc708b86b4014bc83805e44336fe16fee796c901c564d69cdf058d`; unchanged worker lock `6902a8569ec299bd26f6fd10d030055124cdcc00c6682412f379c02b95b241ae`. Receipts under ignored `logs/` are local supporting evidence; this report records the durable outcomes and limits.

**Proposed P18-FINAL definition for preparation remediation:** one final gate after focused convergence, comprising the following checks in the pinned environment, each with its own bounded receipt and aggregate gate status. This does not authorize execution/implementation now:

- In `rag-engine`, run the pinned Python's `-m ruff check .`, `-m ruff format --check .`, `-m mypy src/ppv_rag`, `-m pytest -q`; rerun watchdog/safety checks if changed. Normal cap 1,200 s; a genuinely full pytest suite may use the explicitly approved full-suite profile.
- From repository root: `scripts/run-bounded-command.py --profile full-suite --receipt logs/phase18/full-java.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify`, launched with the pinned native Python. Maximum 2,100 s only when the outer host permits it. Preserve the full Java/Modulith/PostgreSQL gate; no skipped security tests or inherited AUTH-CLOCK waiver.
- Require linked, already produced focused receipts for the selected offline parser/OCR/model, held-out fidelity, resources, actual Windows R7 and PostgreSQL multi-file finish/recovery. No duplicate benchmark/full-suite run merely to aggregate receipts.

Focused implementation order remains text/Obsidian → Office → PDF/OCR → verified embedding/publication. Each slice runs narrow fixtures; changed authority/finalization runs real PostgreSQL/native boundary tests. One complete P18-FINAL after convergence. Initial implementation is not remediation round 1; allow two Antigravity remediation + independent re-review rounds total, then a separately authorized Codex takeover and separate independent review. Ordinary commands use the real 20-minute watchdog, not prose; full suites alone may use 35 minutes. Watchdog tree termination is best-effort, and this successful owned-tree fixture is not universal OS cleanup certification.

**NOT_VERIFIED / mandatory future implementation acceptance:** live external-file R7/last-check-to-COMMIT; batch schema and role/fence correctness; new parser/OCR/embedding/chunk behavior; actual selected native ABI/GPU/driver/offline artifact compatibility; process-tree peak RAM/host headroom and peak VRAM; held-out quality and thresholds; new content/temp purge; final full Java/Python/static gate. Existing Phase 17 evidence remains usable only for unchanged behavior and must not certify these new boundaries. No IDE inspection, full Maven suite, ML benchmark, dependency installation or live-file activation ran during this review.

## 6. Result and next action

**CHANGES_REQUESTED:** resolve P18-PH-01–04 in preparation. No production changes, baseline amendment, new hook/agent, handoff, commit or push was performed. ACTIVE remains `NO_ACTIVE_HANDOFF`, and all containment restrictions remain.

**Next step:** give this report and the latest package to ChatGPT for bounded preparation remediation, including proposed D18-A canonical contract text for explicit owner approval; then rerun `$codex-pre-handoff-review`. Only a later `READY FOR HANDOFF`, owner preparation commit/push and separate `$codex-create-handoff` can authorize implementation.
