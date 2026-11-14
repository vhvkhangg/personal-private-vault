# Independent pre-handoff review — BF26-DP-RO-B

- **Date / reviewer / mode:** 2026-11-14 / independent Codex / explicit maintenance preparation, RO-B only.
- **Verified baseline:** branch `main`; HEAD, `origin/main` and live remote `main` all `83f87ca72a2571ce08e3ee99e78a147e556775ff`. Entry tracked worktree/index clean; only this six-document preparation directory was untracked. RO-A source `0956b4962f529050dddf88dc7f2ec19ac571e0d0` and subsequent closeout publication are verified predecessors; ACTIVE is NO_ACTIVE_HANDOFF.
- **Authority:** Owner-selected RO-B preparation is recorded in README/review-workflow, following published RO-A; [RO-D04/D06](../../2026-11-13-bf26-dp-public-read-only-preparation/owner-ratification-ro-d04-d06-2026-11-13.md) fixes 19 PUBLIC fields, unchanged 18 PRIVATE, name/null/error/snapshot semantics. This review does not reopen those product choices or infer source authority from them.
- **Outcome: READY_FOR_HANDOFF — RO-B PREPARATION ONLY. No blocking preparation findings or unresolved material Owner product decisions.** The qualified source/test map and materialization precision below are the review's required carry-forward into later exact Owner authorization and executable handoff. No implementation, runtime acceptance, publication or active handoff is created.
- **Counters:** RO-B implementation remediation **0 consumed**; takeover **NOT_AUTHORIZED / NOT_USED**. RO-A remains 1/3 consumed/STOPPED; predecessor counters/history/backlogs unchanged.

## Required user-visible functions and simplest mechanism

Relative effort below is a static estimate of implementation and verification scope, not measured runtime or a case-count quota.

| Owner-visible requirement / mechanism | Decision and simplest adequate reuse | Effort / approval / non-goal |
| --- | --- | --- |
| Full eligible Fiction/Film fields | **KEEP / REUSE** existing full feature views, owning repositories and classification loaders; explicit PUBLIC-only 19-property DTOs | Moderate implementation, focused field/value tests; existing PRIVATE DTOs/mapping unchanged; later exact Phase 4/5/13 source/API exceptions |
| Linked author/director scalar | **KEEP / REUSE** existing Person/CreatorGroup named find APIs, with Person's own active correct-type Vault qualification first | Moderate null/error/snapshot verification; no People/Vault source edit or direct People HTTP grant |
| Coherent eligibility and content | **KEEP / REUSE** feature-owned REQUIRES_NEW/REPEATABLE_READ boundary, materialize immutable full result before return | Controlled PostgreSQL proof required on the new full-detail path; no controller transaction, cross-owner repository or retroactive cancellation |
| PRIVATE GET/POST/PUT compatibility | **KEEP** original 18-property DTOs and mutation paths; add exact cross-profile assertions in selected read tests | Verification effort; no shared DTO name addition or global Jackson change |
| Exact eight business GET pairs / denied mutations | **KEEP / REUSE** unchanged SecurityConfiguration and actual registered-handler sweep; retain RO-A POST 403 | Regression/source qualification; no new endpoint, wildcard GET, Public write or session redesign |
| Accurate 19/18 OpenAPI and null presence | **KEEP / REUSE** root customizer, module DTO metadata and HTTP specimen validator | Moderate schema/test edits; truthful anyOf where shapes overlap; no new schema/serialization framework |
| Old minimal APIs/DTOs and blocked Public POST branch | **DEFER retirement / SIMPLIFY scope** by retaining them | No deletion or unrelated cleanup; retain callers/tests and historical evidence |
| New dependencies, agents, hooks, global policy, search/list/modules, Frontend/RAG/deployment | **REJECT from current scope / DEFER** separately selected capabilities | Existing backend-implementer and tools suffice; no new infrastructure, benchmark, model or provider work |

Two selected GET representations form one finite maintenance slice. Shared read/name/schema concerns justify reviewing them together; no extra phase/milestone or automatic future slice is introduced. Antigravity self-review and an independent completed-slice review remain required. Any later broader aggregation/audit needs its own actual approved scope.

## Source and ownership qualification

Evidence: `logs/bf26-dp-ro-b/pre-handoff-20261114-c39e71/entry-audit.json`. **27/27** declared production/test/module-descriptor Git blobs match both HEAD and worktree; **12/12** local input links resolve. Four proposed new class paths are absent and their existing parent packages are present. No source change was made by this review.

- FictionService/FilmService already have full `find`/`toView`/classification loaders and minimal `findPublic` with owning REQUIRES_NEW/REPEATABLE_READ qualification. The inherited repository `findById` is a feasible full-row seam inside a new qualified service transaction; a repository production edit is not currently necessary. Plain `find` remains unsuitable as a Public authorization operation.
- PersonService.find and CreatorGroupService.find are read-only with default REQUIRED propagation; VaultEntryService is class-level read-only/default REQUIRED. Their existing APIs can join the owning feature transaction. This is source-supported feasibility, **not fresh proof** of the future full-detail path's physical transaction/isolation. Person.find does not itself check Vault type/trash; a separate existing Vault named read is required before disclosing the Person name. CreatorGroup has no Vault/trash lifecycle.
- Existing Fiction/Film descriptors already permit the necessary exact People/Group/View and Vault named interfaces. New application views live in existing feature view packages; no module edge or descriptor change is needed. A search for concrete FictionOperations/FilmOperations implementations found the owning services; no additional handwritten implementation requiring a new method was identified.
- Shared PRIVATE FictionResponse/FilmResponse are the unchanged 18-property records. PUBLIC response property lists in the binding contract add only authorName/directorName. Existing full views contain scalar/enum/ID-set values; materialize/copy sets before returning, rather than carrying entities, lazy collections or in-process Person/Group views into serialization.
- Root ApiExceptionHandler's unexpected-error branch already returns safe INTERNAL_ERROR/500 and logs only exception class. The selected Public branch must still ensure **all genuine operational/transaction failures** become canonical data-null/no-store 500, including exceptions at service return/transaction completion. Do not swallow them as normal Optional absence or let an unrelated domain/not-found advice disclose child existence. Existing owning controller paths suffice for a bounded profile-specific adapter; no global advice redesign is indicated.
- SecurityConfiguration's eight B1 GET method/path pairs, PRIVATE fallback, Auth exceptions and PRIVATE ASYNC/ERROR ordering are unchanged by design. Public POST remains denied. Root OpenAPI edits concern two GET graphs only; retain RO-A's PRIVATE-only createFilm contract and all unselected routes.

Graphify was used for navigation, then claims were checked in canonical source. It is not transaction evidence. Reviewed source inputs include services/repositories/operations, full and minimal DTOs/views/mappers/controllers, People/Vault readers, root error handling, module descriptors and affected tests. Applicable root/docs/backend/test/Fiction/Film/People instructions, current Owner policy, Docs Sync, ADR-0016, module dependency matrix, repository/package tree and previous RO-A independent review remain the governing baseline.

## Review refinements required in the later executable contract

### 1. Distinguish snapshot materialization from pure HTTP mapping

The controlling RO-D04 ratification requires all 18 work values, classification sets, qualified name and eligibility to be **materialized in the same owner snapshot**. The draft binding contract's line 23 broadly says no DTO assembly after commit, while the candidate correctly returns a feature-owned immutable result and maps a separate HTTP DTO.

**Review interpretation and precision:** the immutable application result must be completely assembled inside the transaction. A controller/mapper may then perform **pure in-memory mapping and JSON serialization of those captured values** after the service transaction completes. It must not issue a repository/capability call, resolve a name, traverse a lazy entity/collection or obtain any additional value then. This preserves the ratified snapshot/security guarantee and normal service/web ownership; it does not require a service→web dependency or a controller transaction. The later handoff must state this explicitly instead of reproducing an ambiguous blanket ban on DTO construction. This is implementation-boundary precision under the existing Owner decision, not a request to weaken or re-vote the contract.

### 2. Include the existing create-test follow-up GET in the exact candidate map

`T/film/PublicFilmCreateIntegrationTest.java:488`, `followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys`, creates a PRIVATE Film and asserts exactly seven keys from the following PUBLIC Film GET. Its expected representation becomes obsolete under approved RO-B. It was omitted from the initial four-test candidate table and cannot be treated as unchanged passing evidence for the new GET shape.

**Required refinement:** include this existing file in the later exact Owner test exception, changing only the follow-up PUBLIC GET expectation/name/specimen to 19 properties with present nullable directorName and full approved values. Preserve its PRIVATE 18-key creation/media/validation/observer/rollback/race controls and RO-A Public POST denials. Run its final affected class or a documented complete applicable selector set. No new test file or production behavior follows from this refinement. The draft expressly asks the reviewer to determine the exact necessary set; this review supplies that qualification before any source authorization.

### 3. Make future proof target the new operation and legal fixtures

- Existing B1 minimal `findPublic` isolation tests are foundation/reuse inputs, **not proof of a new `findPublicDetail`** or new controller branch. New full-path witnesses must cover feature fields/classifications, Vault and Person/Group lookups in the same physical owner transaction, including a weaker outer transaction/first-level-context case where applicable. Preserve coherent earlier-snapshot completion and fresh-request denial; no instantaneous cancellation claim.
- After a work fails eligibility, assert zero linked People/Group name access and no emitted full result. Eligible review/progress/IDs are approved output; remove them from old positive-case forbidden sentinels, while still rejecting actual People notes/birthdays/roles, Group description/members, hidden-work data and recursive objects.
- Use legal PostgreSQL updates/renames for committed races. Impossible under current FK/NOT-NULL/domain constraints is not permission to drop constraints or invent CreatorGroup trash. Missing/legacy/inconsistent-return cases can use explicitly identified named-capability fault injection alongside real DB snapshot tests. A Group rename/removal scenario must state which stored/name-return state is changed; do not demand an impossible committed broken FK.
- For normal absent/ineligible/blank referenced names, return the always-present name key with null. For genuine work/Vault/People/Group/transaction failures, return the whole safe 500/data:null/no-store; do not return a partially filled 200, retry a PRIVATE read, or classify exception-message text.
- HTTP specimens must assert exact 19 keys including null-name presence and original 18 values. PRIVATE GET/POST/PUT must assert exact original 18 in affected fixture paths; unchanged web lifecycle tests that assert only title/ID are supplementary, not proof of the exact-key invariant. These checks can stay in the already selected read/create tests.

## Qualified candidate file map — not source permission

`J/` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` = the analogous test root. This is the preferred **13 production / 5 test** candidate set for the later exact Owner grant; it is not an executable handoff or authority to create these files now. NEW means a class in an existing package.

| Production candidate | Purpose |
| --- | --- |
| `J/fiction/fiction/FictionOperations.java` | New qualified full-detail capability, retaining existing contracts |
| `J/fiction/view/PublicFictionDetailView.java` **NEW** | Immutable materialized full Fiction view and nullable authorName |
| `J/fiction/internal/application/FictionService.java` | Owner-local full qualification/name/snapshot; mutation paths unchanged |
| `J/fiction/internal/web/controller/FictionController.java` | PUBLIC selected GET branch and safe errors only |
| `J/fiction/internal/web/mapper/FictionWebMapper.java` | Pure explicit 19-field Public mapping, PRIVATE mapping unchanged |
| `J/fiction/internal/web/dto/PublicFictionDetailResponse.java` **NEW** | PUBLIC-only exact 19-property wire shape |
| `J/film/film/FilmOperations.java` | Equivalent Film full-detail capability |
| `J/film/view/PublicFilmDetailView.java` **NEW** | Immutable materialized full Film view and nullable directorName |
| `J/film/internal/application/FilmService.java` | Owner-local full qualification/name/snapshot; mutations unchanged |
| `J/film/internal/web/controller/FilmController.java` | PUBLIC selected GET branch only, no POST/runtime cleanup |
| `J/film/internal/web/mapper/FilmWebMapper.java` | Public 19-field mapping, PRIVATE mapping unchanged |
| `J/film/internal/web/dto/PublicFilmDetailResponse.java` **NEW** | PUBLIC-only exact 19-property wire shape |
| `J/OpenApiConfiguration.java` | Exactly two GET response graphs/components; preserve all other operations |

| Test candidate | Bound |
| --- | --- |
| `T/fiction/PublicFictionReadIntegrationTest.java` | Both authors, 19/18 wire, normal null versus real 500, work/Person qualification and new full snapshot proofs |
| `T/film/PublicFilmReadIntegrationTest.java` | Director/no director, same 19/18/404/500/snapshot and retained denied-POST controls |
| `T/authentication/ProfileRouteAuthorizationIntegrationTest.java` | Exact eight-pair and sensitive/mutation/dispatcher/handler preservation; only necessary changed assertions |
| `T/web/OpenApiRouteInventoryIntegrationTest.java` | Exact actual 19/18 schemas/specimens, null presence, required name, reference validity and operation IDs |
| `T/film/PublicFilmCreateIntegrationTest.java` **review-added existing path** | Obsolete seven-key follow-up GET only; preserve PRIVATE create and revocation regressions |

Read-only regression inputs include FictionWebIntegrationTest, FilmWebIntegrationTest, SecurityFilterChainIntegrationTest, Reference HTTP, existing People/Vault and ApplicationArchitectureTests. Keep SecurityConfiguration, shared PRIVATE DTOs, People/Vault production, repositories, global advice/mapper/filter/transaction manager, module descriptors, schema/Flyway/DBML, POM/locks, providers and agents/hooks unchanged. No old Public type deletion is required. Any necessary additional source/capability requires an exact later Owner decision before implementation; neither an inherited repository call nor this review authorizes repository edits.

No new architecture/schema/edge change is indicated. The later grant must expressly cover the bounded frozen Fiction/Film operations/views/read services/web/API/test exceptions and root OpenAPI; existing named People/Vault reads are read inputs, not permission to modify those modules.

## Finite later verification — NOT_RUN

| Gate / existing selector | ROB coverage and required proof |
| --- | --- |
| FICTION — `PublicFictionReadIntegrationTest` | ROB-02/03/05–07/09–16/18: exact fields/values, Person and Group, null/error/hidden no-enrichment, legal controlled races and physical new full-path snapshot, PRIVATE 18 |
| FILM — `PublicFilmReadIntegrationTest` | ROB-02/04/05/08–16/18/20: director and absent director, 19/18/404/500, full snapshot and retained denial |
| SECURITY — `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | ROB-01/02/19/20: actual eight-pair handlers, both live Public origins, sensitive/nested/mutation denials, session/dispatcher rules, retained RO-A before-handler POST denial |
| PRIVATE — `PublicFilmCreateIntegrationTest,FictionWebIntegrationTest,FilmWebIntegrationTest` | ROB-05/18/20 plus retained mutation/media/validation/commit/rollback controls; updated Public follow-up GET and no Private DTO expansion |
| OPENAPI — `OpenApiRouteInventoryIntegrationTest` | ROB-17/18 plus ROB-01/05/20: generated real graph, exact 19/18 and required nullable name, specimens, all refs, getFiction/getFilm and PRIVATE-only createFilm retained |
| ARCH — `ApplicationArchitectureTests` | ROB-21: named-interface/module/ADR-0016/frozen compatibility, no unauthorized file/dependency/schema/edge |
| Source/receipts/self-review/docs — all groups | ROB-22: final input/fixture/POM/config hashes, selected XML identities, old versus new assertions, fresh versus reused evidence and preserved failures |

ROB-12–15 apply to new full-detail path, not an unchanged minimal helper alone. Their witnesses can share bounded deterministic fixtures; 22 criteria are not 22 subsystems or benchmarks. Use existing normal 300s watchdog groups sequentially; preserve XML before the next run. If a group times out, preserve the attempt and split finite disjoint selectors without raising the cap. Choose actual cases/counts from final changed source, not a guessed fixed total. Unchanged valid evidence is reusable with input and changed-boundary qualification, including RO-A revocation/session controls; no unrelated full-suite or native browser/model/provider run is justified.

## Harness, package tree and shared review coverage

Reviewed six new Markdown inputs plus current publication/authority, relevant source and tests. Existing pre-handoff/coverage/documentation-writing, Graphify, Java/Spring, reuse/SOLID/pattern, module/JPA, backend-testing, authentication and REST/Film/Fiction guidance and the existing backend-implementer role are sufficient. No new skill, custom agent, hook or permission mechanism is justified. Python/RAG/takeover implementation is N/A. Existing trusted-root/native hook limits and actual bounded runner semantics remain; old unrelated harness status prose is deferred under current Owner policy/ACTIVE precedence.

Recursive relevant production inventory: Fiction **86 Java files / 23 Java directories**, Film **106/26**, People **64/19**, Vault **58/17**; all Java directories have package-info and no `.gitkeep`. All four proposed new classes fit existing packages/named interfaces, with no filename collision. This is preparation inventory/placement review, not a line-by-line audit of every unchanged class.

| Shared dimension | Preparation status / evidence and limit |
| --- | --- |
| 1–2 Owner intent/traceability | **PASS:** ratified full 19/18 contract, ROB-01–22 and qualified map/gates above; no new product vote |
| 3–4 Architecture/ownership | **PASS at design level:** existing legal named APIs and owner read transaction; no new module edge/schema |
| 5 Folder/package tree | **PASS:** recursive inventory and four existing package placements verified |
| 6 Shared contracts/exceptions | **PASS:** root ApiResponse/error/OpenAPI and module-local HTTP DTO reuse under ADR-0016; no global shared module |
| 7–9 SOLID/principles/patterns | **PASS at proposal level:** immutable result/pure mapping, bounded reuse and deliberate retention; no framework, generic CRUD or speculative pattern |
| 10–12 Business/wire/validation | **PASS for specified plan:** exact 19/18, Person/Group distinction and null/failure semantics; runtime NOT_RUN |
| 13–14 Auth/privacy/secrets/cache | **PASS for specified plan:** eight routes, work-first/name-only, safe 404/500/no-store, no logs/recursive output/provider expansion; runtime NOT_RUN |
| 15–17 Transactions/failure/database | **PASS for source-supported feasibility and explicit future proof:** existing REQUIRED readers can join RR owner; actual new physical snapshot and safe operational failures NOT_VERIFIED until implementation |
| 18 Complexity | **PASS for bounded detail proposal:** fixed number of reference lookups/classification loaders; no N+1 list, new loop/search/lock or performance claim; reuse existing Person find despite its bounded role read |
| 19–20 Retrieval/OCR | **N/A:** no indexing/search/model/document processing |
| 21–22 Security/test quality | **PASS for finite future gates:** actual DB/HTTP/new-path barriers and no-handler/no-mutation/strict wire cases specified; mandatory runtime NOT_RUN |
| 23–24 Benchmarks/evaluation | **N/A:** no new latency/model-quality target or benchmark subsystem |
| 25 Dependencies | **N/A for changes:** none; no current vulnerability/license audit or upgrade claim |
| 26 Build/static/tooling | **PASS for bounded Markdown inspection:** all six input files returned empty IntelliJ problem arrays with errorsOnly=false and no timeout reported; Java build/full IDE scan NOT_RUN |
| 27 Logging | **PASS for source/plan:** existing safe unexpected-error handler logs class only; new branch must avoid raw payload/Person/log/exception leakage; runtime NOT_VERIFIED |
| 28 Docs/governance | **PASS with explicit review refinements:** current versus prospective, source map and mapping boundary clarified; Docs Sync below |
| 29 Hygiene/delivery | **PASS for scoped artifact/source checks:** 27 blobs and 12 input links valid, no code/ACTIVE/publishing change, final guard below |

IDE evidence: `logs/bf26-dp-ro-b/pre-handoff-20261114-c39e71/ide-preparation-inspections.json`. Empty Markdown problem arrays are not a semantic or project-wide IDE-clean claim; no actionable/configuration-dependent/accepted warning was returned in that selection. **No RO-B runtime tests, Java compilation, PostgreSQL experiment or actual OpenAPI generation was run.** RO-A's historical 117-case/36-case results are predecessor evidence only. New physical-snapshot/wire/failure proofs are future mandatory acceptance, not fabricated passes or a preparation blocker.

## Docs Sync Check, stop and next action

- **UPDATED:** this independent review and only the maintenance README current readiness/qualified-map/next-action fields. Original five binding/inventory/boundary/acceptance/workflow inputs remain the reviewed proposal; this report supplies the final preparation refinements without pretending source is authorized.
- **ALREADY_CONSISTENT:** actual published RO-A and docs closeout, current eight GET/minimal shapes and denied Public POST, RO-D04/D06 choices, source/module fingerprints, ACTIVE NO_ACTIVE_HANDOFF, predecessor counters/history.
- **DEFERRED_OUT_OF_SCOPE:** root/current policy/roadmap/maintenance-index/ACTIVE navigation until appropriate accepted RO-B publication; original RO-A handback/self-review/executable and DPROA-L01; other LOW/harness/Frontend/RAG docs. ChatGPT owns later authorized docs sync; no repository-wide repair here.
- **NOT_VERIFIED:** all RO-B runtime, generated schema, physical snapshot, fault handling and Private compatibility proof; full IDE diagnostics, live deployment and Owner-data suitability. These remain specified later gates.

Stop for any attempted source edit/activation now, unapproved People/Vault/repository/global policy/schema/module/dependency change, new Public route/write, changed PRIVATE semantics, unsafe post-transaction lookup/lazy value or material source drift. No implementation counter is consumed here. RO-B remediation **0 consumed**, takeover NOT_AUTHORIZED/NOT_USED; RO-A **1/3 consumed/STOPPED**, W1 **2/3 STOPPED**, B1 **3/3 STOPPED**, DP-A **3/3 exhausted / accepted exceptional takeover 1/1**. DPA-L01/DPB1-L02/DPB2W1-L01/DPROA-L01 LOW backlogs remain unchanged. H1-F paused/FR-13 upstream-blocked, P17 frozen, P18 closed, S19-2 suspended, providers OFF/unset, indexing inactive; no deployment/cutover claim.

Final saved-file/source/ACTIVE integrity and scoped links: `logs/bf26-dp-ro-b/pre-handoff-20261114-c39e71/final-integrity.json`.

**Suggested Owner preparation commit:** `docs(backend): prepare public full-detail reads`

**Next step:** Owner → Codex | Owner commit/push reviewed RO-B preparation, then separately authorize the exact qualified 13-production/5-test frozen-source/API map; only then `$codex-create-handoff` | BF26-DP-RO-B, README and this review's refinements | READY_FOR_HANDOFF at preparation level, unpublished; ACTIVE remains NO_ACTIVE_HANDOFF | RO-B remediation 0 consumed; takeover NOT_AUTHORIZED; predecessor counters unchanged.
