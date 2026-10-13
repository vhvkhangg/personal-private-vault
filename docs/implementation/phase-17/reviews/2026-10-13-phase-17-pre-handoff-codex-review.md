# Phase 17 Preparation — Codex pre-handoff review, 2026-10-13

**Status: CHANGES_REQUESTED.** Three blocking preparation findings remain. The owner's Phase 17-before-Phase 16 sequencing exception is accepted; completing Frontend is not a prerequisite for this review. No production implementation or handoff is authorized by this report.

## Scope and baseline

Reviewed [ADR-0019](../../../adr/0019-local-rag-before-frontend.md), [RAG architecture](../../../architecture/rag-architecture.md), the [phase scope](../README.md), [preparation checklist](../preparation-review.md), [benchmark/acceptance plan](../benchmarks-and-acceptance.md), workflow/status changes, and the proposed `local-rag-engineering` skill. Checked these against the frozen module matrix, package tree, security/API/import/search contracts, ADR-0014/0017, current Fiction/Film/Vault public contracts, and existing agent/rule/hook instructions. This is preparation review, not a new audit of the frozen backend.

- Branch: `main`; `HEAD` and local `origin/main` both `cbd5963086f41658ffe421695b546f5b1598a161`. No fresh remote fetch was performed.
- Phase 15 remediation `0b1ab1100084f8c7a4362a42874595b25b2b50ee` is an ancestor of HEAD (exit 0). Its final closure report is `BACKEND_AUDIT_READY`; closeout/current governance records it as `COMPLETE — FROZEN`.
- [ACTIVE](../../handoffs/ACTIVE.md) is `NO_ACTIVE_HANDOFF`. Phase 16 remains deferred. No milestone prerequisite applies after Phase 15.
- Submitted preparation contained 19 changed/new Markdown files only. Backend sources/tests/configuration, POM/wrappers, migrations/DBML, canonical module matrix/boundaries/package tree, diagram sources/exports, agents, rules and hooks have no diff from HEAD. The 19 implemented top-level module descriptors remain present.
- Graphify was used for navigation only, then relevant facts were checked in canonical files/source. No graph rebuild or delegated agent work was required.

## Blocking findings

### P17-1 — Medium — Define the proposed ownership and frozen-baseline delta before handoff

References: [ADR-0019](../../../adr/0019-local-rag-before-frontend.md), lines 17–18; [phase README](../README.md), lines 31–32; [architecture](../../../architecture/rag-architecture.md), lines 90–97 and 120; frozen [module matrix](../../../architecture/module-dependency-matrix.md) and [package tree](../../../repository/repository-package-tree.md).

The documents correctly leave the current frozen baseline untouched. However, they defer new module names/dependency edges and RAG storage approval to the implementation handoff while also requiring approval of those architectural changes before a handoff. The actual proposed delta is not bounded yet. Spring orchestrates durable jobs and Python uses PostgreSQL RAG data, but ownership of lifecycle state, generation publication and migrations, and each process's database permissions, are unspecified. A namespace alone does not restrict Python's access to business tables.

Preparation remediation must record a small proposed delta: the initial Java capability/module and semantic public interfaces it may read, package placement, initial source modules, one logical owner for each durable responsibility, migration authority, and the Java/Python database access boundary. Require a separate least-privilege worker credential or an equivalently enforced design that cannot read/write business/security tables or run schema DDL; do not share the existing broad application/migration credential. Identify the affected ADR/matrix/tree/schema/diagram sources and the owner-acceptance and synchronization gate. Exact class designs or a table per conceptual inventory item are unnecessary.

An alternative is to bound the first handoff to work that needs none of those frozen changes and explicitly retain a separate preparation/owner gate for the integration delta. Neither alternative grants implementation permission now. The eventual integration evidence must include module verification and negative database-permission tests with the actual worker role, not only an application-level SQL prohibition.

### P17-2 — High — Make source classification and field eligibility an explicit contract

References: [architecture](../../../architecture/rag-architecture.md), lines 25–37, 61–63 and 93–94; [phase scope](../README.md), lines 12 and 19; [local RAG skill](../../../../.agents/skills/local-rag-engineering/SKILL.md), lines 10–14. Current contracts: [FictionView](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/view/FictionView.java), [FilmView](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/film/view/FilmView.java), [VaultEntryView](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/view/VaultEntryView.java), and [FictionSearchDocument](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/search/FictionSearchDocument.java).

The fail-closed requirement is correct, but the preparation does not define how a source becomes known non-private or which initial fields are approved. Fiction/Film expose `isNsfw`; Vault identity/trash metadata has no generic Private classification, and existing search documents do not supply RAG eligibility/version proofs. Existing owner-authenticated reads therefore cannot be treated as automatic indexing permission. The broad future module scope and selected external formats also include sources without a stated classification authority.

Add an initial eligibility matrix identifying supported source types, authoritative classification/NSFW and deletion checks, allowed text/metadata fields, and the read-only projection needed where existing APIs are insufficient. Explicitly exclude every unreviewed module/field by default, including sensitive Finance/Personal/Journal/Account data, until its narrow contract is approved. Do not change existing Search or PIN semantics to supply RAG authorization.

For external files, define the explicit owner approval/classification record, unknown/private/NSFW handling, and precedence of source metadata or a later revocation. Selecting a directory must not silently classify every file as safe. Each embedded/transcluded target needs its own eligibility proof as well as a path/scope check; non-private parent A must not authorize private or unclassified child B. Define how secret-bearing/unknown metadata is excluded without promising an infallible content classifier. Add expected-denial fixtures for unclassified plain text/PDF, sensitive fields, and an in-scope but ineligible embed target. These are documentation/test-plan requirements now, not requests to implement classifiers or alter Schema v1.

### P17-3 — High — Complete revocation and version fencing across all derived artifacts

References: [architecture](../../../architecture/rag-architecture.md), lines 33–47, 63, 71–88 and 93–105; [benchmark plan](../benchmarks-and-acceptance.md), lines 29–47; [local RAG skill](../../../../.agents/skills/local-rag-engineering/SKILL.md), lines 11 and 14–16.

Immediate deny, rechecks, atomic generations and reconciliation are good requirements. The specified purge covers chunks/embeddings/projections, while generated answer histories, judge evidence/results, session artifacts and other retained derivatives have a separate TTL policy. The relationship between those policies is missing. Embedded-source copies can also survive inside an otherwise eligible parent's generation. A source can be revoked while embedding/generation/judging is running; short-lived selection grants and eventual event reconciliation do not by themselves prevent late publication or reuse of its derived text.

Define the authoritative source/policy revision and the fence for job admission, publication, answer/history/source resolution and queued judging. A stale worker must be unable to republish after revocation; unavailable freshness evidence must deny rather than use the last known-good version. Explain external-file version checking without relying solely on watcher delivery. Specify dependency invalidation for transclusions and the retention/denial/purge rule for every content-bearing artifact, including answers, prompts, caches, temporary snapshots, judge evidence and retired generations. Sanitized operational metrics may have a distinct retention policy.

Add deterministic acceptance scenarios for revocation during embedding/publication and answer generation; a lost deletion event plus worker restart; revoked embedded child B in parent A; and an expired/revoked ad-hoc session with a queued judge and retained answer. Check for absence of leaked text, metadata/count/ranking effects and late republishing. Preserve the existing bounded worker/reconciliation approach; a new outbox or broker is not required by this finding.

## Other review dimensions

| Dimension | Disposition |
| --- | --- |
| Sequencing / ADR-0014 | Owner exception is explicit and narrow. Preserve 17/16 numbering; Phase 15 is the frozen predecessor. ADR-0019 remains Proposed pending remediation and owner acceptance. |
| Scope and non-goals | Grounded QA, typed factual reads and catalog-only recommendations are distinguished. No Frontend, Ideaverse integration, write tools, cloud AI or production deployment is authorized. |
| Frozen compatibility | No current baseline change detected. Proposed integration delta still needs P17-1 closure; portability's JDBC exception cannot be reused. |
| SOLID and design patterns | Cohesive authorization/parsing/chunking/retrieval/evaluation roles and composition fit the problem. Format Strategies, provider Adapters and a narrow Facade have real reasons. Avoid speculative factories, service hierarchies or a State class per job enum. |
| Reuse / ownership | Preserve owning-module business rules and existing API/import/search semantics. A dedicated safe RAG projection is justified where a current read lacks the required eligibility/provenance; full view serialization is not safe reuse. |
| Service security / offline | Independent service authentication, bound source grants, loopback-only internal service, no arbitrary SQL/paths/URLs, and denied runtime downloads/telemetry are sound requirements. The handoff must make credential handling, actual local transports, permitted local connections and egress-denial checks concrete; no isolation is claimed demonstrated here. |
| Grounding / recommendation | Stable source locators, abstention, exact catalog-ID validation and all-status candidate coverage are appropriate. Live eligibility and version proof depend on P17-2/3. Local judge scores cannot prove safety or factual correctness. |
| Testing / performance | Multilingual/OCR/table/adversarial fixtures, real PostgreSQL, Java/Python contracts, recovery and memory measurements are appropriate. Model/parser/pgvector choices and numeric quality/latency thresholds may remain empirical gates; record their acceptance before implementation sign-off. |
| Agents / skills / hooks | One focused Python/offline/RAG skill adds useful domain guidance and reuses existing engineering skills. No new agent, duplicate rule set or hook change is justified by this preparation. Existing safety hooks remain unchanged. |
| Repository hygiene | Only documentation/skill artifacts are added; no new Java package or stale placeholder is introduced by this slice. Do not initialize `rag/`, a Java module, or a migration during remediation. This is not a repository-wide code-hygiene re-audit. |
| Workflow / status | The exception and normal review → owner preparation commit → separate Codex handoff loop are consistent. Current preparation status is synchronized to this rejection; archived Phase 15 evidence remains historical. |

## Verification and limitations

- Submitted package: `git diff --check` passed; 19 changed/new Markdown files had no trailing whitespace. A filesystem check resolved all 167 local inline file-target links with zero missing targets. Frozen/implementation diff checks exited 0.
- After recording the review/status changes, validation found 20 changed/new Markdown files and resolved all 194 local file-target links with zero missing targets or trailing-whitespace files. The 19-module inventory, frozen/implementation diff checks and `git diff --check` still passed.
- Targeted IntelliJ inspections actually ran on ADR-0019, RAG architecture, Phase 17 README and benchmark plan: zero errors, 27 parent-path resolution warnings and 3 table-formatting weak warnings. Native path validation establishes the parent-path warnings as IDE resolution/configuration issues; the three tables use valid GFM syntax, so column-alignment warnings are accepted style diagnostics. No repository-wide IDE-clean claim is made.
- A follow-up inspection of this review report returned the same parent-path and table-alignment diagnostic categories, with no errors; the same filesystem/GFM triage applies.
- Reviewed official candidate references, including [pgvector](https://github.com/pgvector/pgvector), [Docling](https://docling-project.github.io/docling/) and [Tesseract language data](https://tesseract-ocr.github.io/tessdoc/Data-Files.html). They support investigating local processing/vector/OCR candidates; they do not establish fit for the owner's corpus, GPU or exact repository images. Development uses `postgres:18.6`; integration tests use `postgres:18.6-alpine`. Neither was tested with pgvector in this review.
- No Antigravity Phase 17 implementation/test evidence exists yet. Maven/Python regression, models, OCR, GPU/memory benchmarks, service-authentication/database-role enforcement and runtime egress denial were not executed. The Phase 15 count of 1021 tests is historical, not a Phase 17 test result.
- The preparation author's referenced ZIP and `DxDiag.txt` were not present in the reviewed working tree. Their package/hardware observations were not independently reproduced. Hardware/runtime and storage headroom remain to be verified at the documented benchmark gate; no upload or premature installation is required for preparation remediation.

## Disposition and next step

**CHANGES_REQUESTED:** P17-1, P17-2 and P17-3 must be closed in preparation documentation before `READY FOR HANDOFF`. Only review/status documentation is changed by this invocation. ACTIVE remains `NO_ACTIVE_HANDOFF`; no production code, dependency, migration, new module, handoff, commit or push was created.

**Next step:** Give this report and the latest repository package to ChatGPT for preparation-only remediation, then rerun `$codex-pre-handoff-review`. Keep Phase 16 deferred and do not run `$codex-create-handoff` yet.
