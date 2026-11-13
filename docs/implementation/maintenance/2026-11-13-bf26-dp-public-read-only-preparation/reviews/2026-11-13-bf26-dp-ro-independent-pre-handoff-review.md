# Independent pre-handoff review — BF26-DP-RO

- **Date / reviewer / mode:** 2026-11-13 / independent Codex / Owner-approved maintenance preparation; review only.
- **Baseline:** branch `main`; local HEAD, `origin/main` and live `git ls-remote origin refs/heads/main` all `3ce4c87d6808c039ffd35ebd7ed67d73659b092c`. Entry index and tracked worktree were clean; only the six submitted RO preparation documents were untracked.
- **Authority:** [Owner policy ratification](../owner-policy-ratification.md) approves prospective read-only Public policy and disclosure of the existing full Fiction/Film response fields. It does not authorize production/API edits. The abandoned W2 archive is not authority. `ACTIVE = NO_ACTIVE_HANDOFF`.
- **Final status: CHANGES_REQUESTED.** **FR-RO-PH-01 MEDIUM / OPEN / OWNER_DECISION_REQUIRED** blocks readiness for the submitted RO-A/RO-B preparation. RO-D04 remains an unresolved observable API contract. RO-D06 needs an explicit delivery selection; reviewer recommendation is RO-A first, with RO-B separately prepared. No implementation handoff, production/test edit or publication occurred.
- **Limits:** this is preparation assessment, not new RO runtime acceptance or a re-adjudication of accepted DP-A/B1/W1. Lack of future source authorization is the expected later gate, not an additional finding.

## Source qualification and current behavior

Evidence: `logs/bf26-dp-ro/pre-handoff-20261113-5b7cbe/static-review.json` records the six entry document SHA-256 values, **15/15** inventory blobs matching both HEAD and working files, **7/7** local Markdown link targets resolving, and complete file inventories of Authentication, Fiction, Film and People. Graphify was queried once for navigation; the following conclusions were verified in source.

The eight B1 business pairs are exactly:

| Method | Path |
| --- | --- |
| GET | `/api/v1/fictions/{id}` |
| GET | `/api/v1/films/{id}` |
| GET | `/api/v1/reference/countries` |
| GET | `/api/v1/reference/countries/{code}` |
| GET | `/api/v1/reference/languages` |
| GET | `/api/v1/reference/languages/{code}` |
| GET | `/api/v1/reference/currencies` |
| GET | `/api/v1/reference/currencies/{code}` |

`J/` below means `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` means the analogous `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

- `J/authentication/internal/infrastructure/security/SecurityConfiguration.java:217–263` orders infrastructure and exact Auth exceptions, conditional Google/Drive exceptions, profile transition/PIN rules, then PRIVATE-only ASYNC/ERROR, eight B1 GET pairs, W1 `POST /api/v1/films`, then PRIVATE fallback. PUBLIC is authenticated. Removing the POST exception lets the existing PRIVATE fallback retain PRIVATE create; a new blanket method rule or security framework is unnecessary. Preserve Auth POSTs and dispatcher order.
- FictionController GET and FilmController GET currently serialize six/seven-property public projections, respectively. FilmController POST still admits the W1 Public create branch after security admission. The read-only policy is therefore **not currently implemented**; this is a documented, intentional prospective change, not a newly proven defect in historical W1 acceptance.
- `FictionResponse.java:9–28` and `FilmResponse.java:10–29` each define exactly **18** existing properties, including review/progress and relationship IDs, but no author/director name. WebMapper `toResponse` is also used by PRIVATE create/update/read responses. No Vault rating/tag metadata is an implicit nineteenth property.
- `FictionService.findPublic:153–176` and `FilmService.findPublic:172–195` use owning-service `REQUIRES_NEW / REPEATABLE_READ`, non-NSFW repository projections and Vault ID/type/deleted-at qualification. Ordinary `find` is not an eligible-Public read. Full fields, classifications and any approved name must be materialized inside the qualified snapshot; do not qualify in one transaction and enrich in a later controller call.
- `PersonService.find:85–92` reads a full PersonView and roles without checking Person Vault trash/type. `CreatorGroupService.find:81–85` reads the group row; Creator Groups are not Vault-backed and have no analogous trash lifecycle. Existing named People capabilities make scalar extraction feasible, but do **not** themselves implement the proposed missing/deleted/mismatched-reference policy. Their default REQUIRED read transactions can participate in the feature owner's snapshot; the later PostgreSQL gate must prove this for any new enrichment.
- Fiction and Film descriptors already allow the necessary named People/View interfaces, including `people::group` for Fiction, plus Vault and Reference. No new module edge, reverse Vault dependency, repository sharing or cross-owner SQL is needed for the proposed boundary.
- Full DTO means the approved fixed property set. It does not authorize recursive PersonView/CreatorGroupView, arbitrary follow-up IDs, generic Vault metadata, credits, nested routes or additional module reads. Field-value disclosure of review/progress/IDs is Owner-approved; this review does not reopen that choice as a privacy finding.
- Public DTO/view references include feature Operations, Service, Repository, WebMapper, tests and root OpenApiConfiguration. PublicFilmResponse is additionally used by W1 POST mapping. **No safe deletion is approved**: revocation can retain unreachable Public parser/DTO code initially. Enumerate all references again before any later authorized retirement.

The registered-handler sweep source in `T/authentication/ProfileRouteAuthorizationIntegrationTest.java:44–248` currently reconciles nine allowed pairs, exercises both persisted Public token origins, positively qualifies the handler spy and checks zero handler execution plus a complete domain snapshot on denied routes. It is reusable after changing the current contract to eight pairs. This is a source inspection; no fresh runtime registration count or HTTP pass is claimed.

## Finding and finite decisions

### FR-RO-PH-01 — name enrichment is optional in the decision docket but mandatory and underspecified in acceptance

- **Severity / blocking / disposition:** MEDIUM / YES for submitted RO-B or combined readiness / OPEN, OWNER_DECISION_REQUIRED.
- **Provenance:** [decision docket](../decision-docket-ro-d01-d07.md), RO-D03/D04 and unresolved-decision paragraphs (lines 9–19); [contract](../read-only-contract-and-acceptance.md), response rules 1/4/5 and RO-05–07 / RO-11–14 (lines 20–24, 35–44); [ratification](../owner-policy-ratification.md), full existing shape versus proposed enrichment. Current shared DTO and WebMapper consumers are identified above.
- **Observed preparation defect:** RO-D04 explicitly leaves names, missing-link semantics and addition to the **shared** full DTOs proposed. RO-05/06 condition the extra key on a future decision, but RO-11–14 unconditionally require name display and failure behavior. Meanwhile PRIVATE must stay unchanged. Neither the exact selected 18/19-key Public contract nor the effect on PRIVATE GET/POST/PUT, required-null versus absent key, invalid Person Vault identity/trash, group absence or database failure is frozen. Existing People `find` does not resolve those choices automatically.
- **Consequence:** an implementer cannot produce one unambiguous exact-field/OpenAPI/private-compatibility oracle. Adding a nullable property to the shared DTO can change PRIVATE serialization and schemas even if its value is null; swallowing a failed DB transaction as a null name is also not an established recovery contract. This is a preparation contract gap, not a claim that an unimplemented RO endpoint has leaked data.
- **Smallest correction:** select the delivery target explicitly. For **RO-A first**, mark RO-B and RO-05–14/new GET-schema obligations DEFERRED and preserve current B1 six/seven-key GETs; no name vote is needed to implement revocation later. For **RO-B readiness**, record the Owner's chosen wire contract: original 18 keys with names deferred, or exactly 18 + one approved name key; state PRIVATE wire/schema compatibility, null/key presence, Person/Group source eligibility and safe missing-versus-infrastructure-failure behavior. Make RO-11–14 conditional on the selected enrichment. No new People HTTP/API capability is necessary by default.
- **Proposed enriched option, not ratified:** PUBLIC only gets nullable `authorName`/`directorName`; existing PRIVATE responses remain exactly 18 keys. Missing optional director or unavailable/ineligible relation returns an explicit null name without revealing other fields; a genuine DB/lookup execution failure returns the canonical safe 500 with no data/no-store. Decide Person Vault qualification and group-specific absence explicitly; do not invent group soft-delete. The lower-effort alternative is full existing 18 keys with no name enrichment.
- **Closure proof:** a bounded ChatGPT docs correction states one chosen slice and exact response/error rules, maps each applicable RO criterion without optional/mandatory conflict, identifies finite source/frozen exceptions, and receives independent preparation re-review. Runtime proofs remain later implementation acceptance. Reviewer must not silently choose a new Owner-visible wire contract.

**RO-D06 recommendation:** deliver **RO-A alone first**. It removes the current write-policy mismatch without waiting for the full-read/name expansion. Keep RO-B as a distinct, not-ready proposal; no combined implementation is approved. This is a recommendation, not a second demonstrated code defect or a new remediation round. If the Owner instead selects combined preparation, FR-RO-PH-01 must be resolved for the whole combined contract before readiness.

## Owner-visible requirements, necessity and effort

Effort is a relative static estimate of implementation/verification scope, not measured duration or a test-count promise.

| Requirement / mechanism | Disposition | Simplest adequate reuse and effort | Approval / non-goal |
| --- | --- | --- | --- |
| No PUBLIC business mutation, including Film create | **KEEP / REMOVE** Public POST exception | Existing PRIVATE fallback; small production delta, moderate verification because current W1 positive tests must change | Approved product policy; exact Phase 2/5/13 source/API/test authorization later |
| PRIVATE writes and auth transitions | **KEEP / REUSE** | Preserve converter, validation, transaction and full DTO; reuse source-qualified controls, rerun affected HTTP/DB cases | No token/session/PIN redesign or Auth POST ban |
| Existing eight selected GETs and sensitive-module denial | **KEEP / REUSE** | Existing matchers, Reference DTOs, handler-spy sweep, domain snapshot | No wildcard GET, new module/list/search/People grant |
| Full 18-field eligible work detail | **KEEP / REUSE**, RO-B | Feature-owned qualified read and full mapping; moderate implementation and PostgreSQL/HTTP proof | Approved disclosure; later exact Phase 4/5/13 exception; no recursive graph |
| Author/director name addition | **OWNER DECISION REQUIRED / DEFER** until fixed | Zero enrichment is simplest original-DTO option; scalar via existing named capability is feasible but adds API, null/error and snapshot tests | Permission to disclose names does not by itself select a new shared DTO contract |
| Snapshot eligibility and failure/cache behavior | **KEEP / REUSE** | Existing read boundary and deterministic barriers; extend proofs to full data/classifications/names only when changed | No retroactive cancellation: a coherent earlier eligible snapshot may complete; fresh post-change read denies |
| Separate RO-A then RO-B | **SIMPLIFY**, recommended | Keeps revocation review independent from expanded disclosure; separate accepted slice publication | Owner selects target; no umbrella B2-W2/S/V/C grant |
| More Public modules, metadata resolution, search/lists | **DEFER** | No implementation/test subsystem now | Separate exact product selection and preparation |
| New schema, dependency, serializer framework, agent/hook or provider | **REMOVE from proposed scope / REUSE existing tools** | Existing Spring/Modulith/MockMvc/PostgreSQL/watchdog sufficient | No migrations, downloads, Frontend, RAG, Recycle Bin, audit or deployment |

## Candidate source/frozen map — review guidance, not authorization or executable handoff

| Candidate | Exact existing paths to consider | Bound |
| --- | --- | --- |
| RO-A production | `J/authentication/internal/infrastructure/security/SecurityConfiguration.java`; `J/OpenApiConfiguration.java`; `J/film/internal/web/controller/FilmController.java` **OpenAPI annotations only** | Revoke Public POST admission, document PRIVATE-only create; retain Private body/converter/domain behavior. Removing the Public matcher is sufficient at the admission layer. No parser/domain cleanup required. |
| RO-A tests | `T/authentication/ProfileRouteAuthorizationIntegrationTest.java`; `T/film/PublicFilmCreateIntegrationTest.java`; `T/web/OpenApiRouteInventoryIntegrationTest.java` | Replace contradictory current Public-create expectations, preserve Private/media/DB controls and historic evidence; eight-pair deny/no-handler/no-write proof. No blanket deletion of historical receipts or test files. |
| RO-B production per feature `X=fiction/film`, `N=Fiction/Film` | `J/X/X/NOperations.java`; `J/X/view/PublicNView.java`; `J/X/internal/application/NService.java`; `J/X/internal/infrastructure/persistence/NRepository.java`; `J/X/internal/web/controller/NController.java`; `J/X/internal/web/mapper/NWebMapper.java`; `J/OpenApiConfiguration.java` | Expand only qualified owner-local detail. The formula denotes the existing exact Fiction/Film equivalents; freeze a concrete list in corrected preparation. No unguarded controller `find`, cross-owner internals or new transaction per scalar. |
| RO-B DTO and schema alternatives | Existing `J/fiction/internal/web/dto/FictionResponse.java`, `J/film/internal/web/dto/FilmResponse.java` and `PublicFictionResponse.java` / `PublicFilmResponse.java` in those DTO directories | Original full DTOs can remain unchanged for 18-key reuse. Any 19-key public-only DTO/view edit and exact path must follow RO-D04 selection. Do not authorize changing shared PRIVATE DTOs or deleting legacy types implicitly. |
| RO-B tests / unchanged regression inputs | `T/fiction/PublicFictionReadIntegrationTest.java`; `T/film/PublicFilmReadIntegrationTest.java`; `T/authentication/ProfileRouteAuthorizationIntegrationTest.java`; `T/web/OpenApiRouteInventoryIntegrationTest.java`; existing Fiction/Film WebIntegrationTest and `T/ApplicationArchitectureTests.java` | Change only affected assertions/fixtures; reuse unchanged tests/evidence by source applicability. Architecture runtime execution is a later gate, not already passed. |

RO-A needs only the bounded Authentication/Film/HTTP frozen exceptions above. RO-B additionally needs Fiction/Film operations/view/persistence-read/HTTP exceptions and the selected DTO/schema contract. Existing named People/Group and Vault entry APIs are read inputs, **not an allowlist to modify People/Vault**. No DBML/Flyway/index/module descriptor/build/lockfile/hook edit is justified. A future new path/capability or broader change must be identified before authorization; this candidate table is not permission to improvise.

## Finite acceptance and later verification — NOT_RUN

| Candidate gate | RO criteria and existing test source | Mandatory observable proof |
| --- | --- | --- |
| RO-A admission/denial | RO-01–04, 15–18, 20, 22; ProfileRouteAuthorizationIntegrationTest and revised PublicFilmCreateIntegrationTest | Both real persisted Public origins get exact 403 for POST before qualified MVC handler/service and unchanged domain rows; anonymous 401; exact eight allowed registered business pairs; Private positive handler/control; Auth exceptions and unknown/method/dispatcher denial. Reuse unchanged session proofs; do not rebuild all DP-A tests. |
| RO-A PRIVATE/API/architecture | RO-03, 19–22; retained Private create cases, FilmWebIntegrationTest, OpenApiRouteInventoryIntegrationTest, ApplicationArchitectureTests | Original media/charset/validation, committed full Private 201 and rollback/error controls as affected; GETs remain B1 minimal; no Public POST advertisement or dangling schema reference; no new module edge. Old Public-positive W1 scenarios cannot remain contradictory current expectations, though original evidence stays immutable. |
| RO-B Fiction/Film field correctness | RO-01, 05–09, 15–16, 20, 22; PublicFictionReadIntegrationTest / PublicFilmReadIntegrationTest | Exact selected 18/19 keys and sentinel values; equal approved common values under stable Private fixtures; original PRIVATE GET/create/update shapes retained; hidden/missing/trash/wrong-type 404 no-store, no unrelated data/route grant. |
| RO-B consistency/failure/names | RO-10 and **RO-11–14 only if names selected**, plus 17–18, 20–22 | Extend real PostgreSQL barriers to all materialized fields/classifications and selected scalar source. Prove outer weaker transaction suspension/physical snapshot and no mixed-state grant; coherent earlier-snapshot completion remains allowed. Test Person/Group/null/DB failure exactly as ratified; hidden work must not trigger People enrichment. |
| RO-B API/architecture | RO-19–22; OpenApiRouteInventoryIntegrationTest / ApplicationArchitectureTests | Generated actual wire/profile schema, nullable/required fields, eight business pairs, no transitive People response or unresolved references; legal named-interface reuse. No assumed fixed total test/schema count. |

RO-A alone defers RO-05–14 and RO-19's **new full GET schema** portion; it retains and source-qualifies existing B1 reads. RO-B defers RO-11–14 if names are not selected. RO-01, 15–22 are shared considerations with scoped proofs, not duplicated mandatory full suites. The original RO-10 wording must be applied with the explicit D10 earlier-snapshot limitation; it must not be read as a new instant-cancellation requirement.

Use the implemented `scripts/run-bounded-command.py --profile normal --timeout-seconds 300` for finite Maven groups, PostgreSQL Testcontainers and MockMvc. Select exact changed tests at handoff; no full-suite, extended-time, benchmark, native browser or provider execution is automatically needed. Retain fresh run paths/hashes/receipts and unchanged source-applicable evidence separately. Antigravity implementation plus mandatory self-review, independent Codex slice review and Owner publication remain the workflow; no extra RAG phase/milestone gate is introduced for this maintenance.

## Harness and shared coverage

Reviewed instructions: root, docs, backend, backend test and Authentication/Fiction/Film/People AGENTS; current Owner policy and Docs Sync; D1–D6, D7–D11 and D12–D17 ratifications; W1 independent final re-review 2 and actual publication closeout. Architecture inputs: module dependency matrix, repository/package tree, API architecture, ADR-0016 and ADR-0029. Historical D8/D13 field/write grants are prospectively superseded only by the selected new RO contract; their accepted evidence is retained.

Harness reviewed: pre-handoff, coverage, documentation-writing, graphify-context, authentication-security, rest-api-http-contracts, modular-monolith-architecture and bounded-command skills; backend-implementer role; handoff/security/tests/package/Fiction/Film rules; `.agents/hooks.json`, `.codex/config.toml`, `.codex/rules/owner-git.rules`. Existing role/tools are sufficient; no new skill, agent, hook or permission mechanism. Python/RAG and takeover implementation are N/A. Historical RAG notices and the role's old watchdog-pending wording are deferred documentary drift; current Owner policy and ACTIVE control this review. No hook was changed or bypassed.

Complete relevant production subtrees were inventoried: Authentication **91 Java files / 21 Java directories**, Fiction **86/23**, Film **106/26**, People **64/19**; all those directories have package-info, with no `.gitkeep`. Source ownership and proposed placement match the existing module-local pattern; this is not a line-by-line audit of every unchanged class.

| Coverage dimension | Preparation disposition / evidence and limit |
| --- | --- |
| 1 Owner intent | PASS: prospective read-only/full-field intent and explicit exclusions; remaining name/delivery decisions identified |
| 2 Traceability | **FINDING FR-RO-PH-01**: optional name choice versus unconditional RO-11–14; slice map above is review guidance |
| 3 Architecture | PASS at design level: existing module matrix, ADR-0016/0029; no schema/edge redesign |
| 4 Ownership | PASS at design level: legal feature→People/Vault calls; no internals/shared repositories |
| 5 Folder/package tree | PASS for recursive relevant inventory and proposed placement; counts above |
| 6 Shared contracts/errors | PASS: reuse root ApiResponse/OpenAPI and module-local adapters; no new common module |
| 7 SOLID | PASS at proposal level: owner-local qualifier/projection, no general service refactor |
| 8 DRY/KISS/YAGNI | PASS with RO-A-first recommendation and existing maps/tools; no unnecessary subsystem |
| 9 Patterns | PASS at proposal level: existing facade/repository/mapper sufficient |
| 10 Business correctness | **FINDING FR-RO-PH-01** for selected name semantics; write revocation and Private rules otherwise explicit |
| 11 Wire/API | **FINDING FR-RO-PH-01**: 18/19-key and PRIVATE schema contract not fixed |
| 12 Validation | PASS for plan: denial before body parsing; unchanged PRIVATE media/validation; finite field assertions |
| 13 Auth/privacy | PASS for specified exact routes/eligibility; selected scalar semantics require finding closure; runtime NOT_RUN |
| 14 Secrets/cache/egress | PASS for plan: no-store/safe errors/no payload logs; no provider/network expansion |
| 15 Transactions/races | PASS for planned existing snapshot/admission contract; new full-data/name runtime proof NOT_RUN |
| 16 Failure/recovery | **FINDING FR-RO-PH-01** for absent/ineligible relation versus execution failure; no new workers/recovery system |
| 17 Database | PASS for design: read-only owner repositories and existing constraints; schema/migration changes N/A; PostgreSQL proof later |
| 18 Complexity | PASS for bounded one-detail reads/classification sets and constant number of referenced names; no list batching/performance claim |
| 19 Retrieval/recommendation quality | N/A: no RAG/search/indexing |
| 20 Document/OCR fidelity | N/A: no document ingestion/OCR |
| 21 Security tests | PASS for specified future both-origin/no-handler/no-write, denial and snapshot cases; NOT_RUN |
| 22 Test quality | PASS at plan level with conditional name cases still needing correction; real DB/HTTP required later |
| 23 Performance benchmarks | N/A: no performance target/claim or new benchmark subsystem justified |
| 24 Evaluation science | N/A: deterministic HTTP/data boundary, no model/quality evaluation |
| 25 Dependencies | N/A for changes: none proposed; no fresh vulnerability/license audit or upgrade claim |
| 26 Build/static/tooling | Six submitted Markdown files inspected by IntelliJ with errorsOnly=false: empty problem arrays; no timeout reported. Java build/full IDE inspection NOT_RUN |
| 27 Logging | PASS at plan level: preserve safe canonical errors and no PersonView/payload emission; runtime logs NOT_VERIFIED |
| 28 Docs/governance | **FINDING FR-RO-PH-01** and Docs Sync ledger below; seven local input link targets valid |
| 29 Hygiene/delivery | PASS for entry source/status and scoped review output; no source/test/schema/history publication; final integrity receipt below |

IDE evidence: `logs/bf26-dp-ro/pre-handoff-20261113-5b7cbe/ide-preparation-inspections.json`. Empty Markdown problem arrays are a bounded inspection result, **not** a semantic approval or project-wide IDE-clean claim. No actionable/configuration-dependent/intentionally accepted diagnostic was returned for those six inputs. No Java/SQL build, runtime test, model call, deployment or Owner-data suitability check was run. Historical W1 **199 distinct accepted cases** and fresh historical **31-case corroboration** remain distinct and are not RO results.

## Docs Sync Check, counters and next action

- **UPDATED:** this independent review and only the maintenance README review status/next action. Routine correction of the proposed contract stays with ChatGPT after Owner decision; the original five decision/inventory/acceptance/workflow input files remain unchanged.
- **ALREADY_CONSISTENT:** live baseline `3ce4c87d`, `ACTIVE=NO_ACTIVE_HANDOFF`, source W1 admission, exact B1 allowlist, accepted W1 publication and immutable reviews/evidence; runtime versus proposed distinctions in the inputs.
- **BLOCKED_OWNER_DECISION:** RO-D04 exact name/PRIVATE contract or explicit RO-B deferral; RO-D06 selected RO-A-first versus combined preparation. Do not turn recommendations into ratifications.
- **DEFERRED_OUT_OF_SCOPE:** root AGENTS/current Owner policy/roadmap/maintenance-index navigation until accepted RO publication; historical root/docs/backend notices, original W1 handback/self-review and DPB2W1-L01, unrelated LOW backlogs and Frontend/RAG docs. ChatGPT owns later authorized documentation sync; no repository-wide cleanup here.
- **NOT_VERIFIED:** all prospective RO runtime, HTTP/DB/race/OpenAPI acceptance, Java/full IDE diagnostics, real Owner data and deployment/cutover. Expected implementation gates remain pending, not failed or fabricated PASS.

Counters unchanged: RO implementation remediation **0 consumed**, takeover **NOT_AUTHORIZED / NOT_USED**; this preparation review consumes no implementation remediation. W1 **2/3 / STOPPED**, B1 **3/3 / STOPPED**, both takeover NOT_AUTHORIZED/NOT_USED. DP-A **3/3 exhausted**, exceptional takeover **1/1 accepted**. All DPA-L01/DPB1-L02/DPB2W1-L01 backlogs retain their previous dispositions. H1-F paused/FR-13 upstream-blocked, P17 frozen, P18 closed, S19-2 suspended, providers OFF/unset and indexing inactive remain unchanged.

Final saved-file/source validation is recorded separately at `logs/bf26-dp-ro/pre-handoff-20261113-5b7cbe/final-integrity-r1.json`; the earlier integrity receipt remains preserved before a source-line citation correction. Neither constitutes runtime acceptance. Stop for uncertain name disclosure, Private compatibility change, extra route/module/schema/capability, source drift or an attempt to open ACTIVE without exact later authority.

**Next step:** Owner → ChatGPT → independent Codex | select RO-A first with RO-B deferred, or ratify the exact combined RO-D04/D06 contract; ChatGPT bounded docs correction, then `$codex-pre-handoff-review` | BF26-DP-RO README, docket, acceptance/workflow and FR-RO-PH-01 in this review | CHANGES_REQUESTED; no handoff/source authorization; after readiness Owner publishes preparation and separately authorizes exact source/API exceptions | RO remediation 0 consumed; takeover NOT_AUTHORIZED; predecessor counters unchanged.
