# Executable slice handoff — BF26-DP-RO-A: Public write revocation

- **Handoff ID / date / author:** `BF26-DP-RO-A` / 2026-11-13 / Codex.
- **Status: READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED.** [Independent final re-review 1](reviews/2026-11-14-bf26-dp-ro-a-independent-final-re-review-1.md) CLOSED FR-ROA-01/02/03 on round-1 final inputs. Producer 117 distinct cases / nine classes qualified; fresh reviewer 36/36 PASS corroborates the same union, no double count. DPROA-L01 LOW/OPEN/BACKLOG remains nonblocking. Exact eight-file scope retained; Owner publication/actual-SHA closeout pending, no RO-B/deployment grant.
- **Classification / authority:** explicit maintenance slice; Owner's current **APPROVED — BF26-DP-RO-A ONLY / EXACT_FROZEN_SOURCE_API_TEST_EXCEPTION**, dated 2026-11-13 and recorded in section 1. Bounded allowlist amendment (6 → 8 files) explicitly approved by the Owner on 2026-11-13 to update obsolete B2-W1 test assertions in `SecurityFilterChainIntegrationTest.java` and `PublicFilmReadIntegrationTest.java`. This is a bounded exception to frozen Phase 2 authentication, Phase 5 Film and Phase 13 HTTP/API/test baselines, not a general unfreeze.
- **Published preparation / entry commit:** [`7700222673712abc59eb6f1de621abc86c7172cc`](https://github.com/vhvkhangg/personal-private-vault/commit/7700222673712abc59eb6f1de621abc86c7172cc).
- **Independent preparation gate:** [RO-A pre-handoff re-review 1](reviews/2026-11-13-bf26-dp-ro-a-independent-pre-handoff-re-review-1.md), **READY_FOR_HANDOFF — RO-A preparation only**, `FR-RO-PH-01 MEDIUM / CLOSED`. The [initial review](reviews/2026-11-13-bf26-dp-ro-independent-pre-handoff-review.md) remains immutable history.
- **Execution register:** [ACTIVE](../../handoffs/ACTIVE.md). Implementer: Antigravity, existing [backend-implementer role](../../../../.agents/agents/backend-implementer/agent.md), `/antigravity-implement-handoff` then mandatory `/antigravity-self-review`. Reviewer: a separate independent Codex `$codex-final-review`; Owner alone stages/commits/pushes after acceptance.
- **Counters:** RO-A remediation **1/3 consumed / STOPPED** after independently accepted round 1; takeover **NOT_AUTHORIZED / NOT_USED**. Unused allowance does not authorize another round or slice. RO-B **DEFERRED / NOT_AUTHORIZED_FOR_IMPLEMENTATION**.

## 1. Binding Owner authorization and sources of truth

The Owner's authorization in the current 2026-11-13 request explicitly permits **one executable handoff for BF26-DP-RO-A only**, based on the published preparation above and independent re-review 1. Its exact operative scope is:

> Remove PUBLIC permission for `POST /api/v1/films` before MVC handler execution. Authenticated PROFILE_PUBLIC returns canonical `403 ACCESS_DENIED` without business handler execution or database mutation; anonymous returns canonical 401; PROFILE_PRIVATE keeps existing Film creation. Test both persisted PUBLIC origins, preserve exactly the eight B1 business GET pairs, and keep all other PUBLIC business routes denied. Only the six source/API/test files in section 3 are authorized. Do not implement RO-B, change the request reader/domain transaction, delete PUBLIC DTOs, change schema/dependencies/providers, or publish through agents. Additional necessary source/capability requires STOP and a separate exact Owner decision.

This record makes the current chat authorization durable for the implementer; the earlier preparation documents' `NOT_SOURCE_AUTHORIZED`, unpublished and pending-review statements describe their dated submission checkpoints. They are superseded **only for this exact RO-A handoff** by the current Owner authorization and verified preparation publication. They are not rewritten.

Read these bounded inputs with the current Owner decision taking precedence:

- [Preparation README](README.md), [RO-D04/D06 ratification](owner-ratification-ro-d04-d06-2026-11-13.md), [slice-specific acceptance interpretation](read-only-contract-and-acceptance.md), [source inventory](source-baseline-and-leakage-inventory.md), and independent re-review 1 above. Full-field/name portions concern deferred RO-B only.
- Root/scoped `AGENTS.md`, [current Owner delivery policy](../../../workflow/current-owner-delivery-policy.md), [Docs Sync](../../../workflow/documentation-synchronization.md), [handoff workflow](../../handoffs/README.md) and [handoff template](../../../templates/handoff.md).
- [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [module dependency matrix](../../../architecture/module-dependency-matrix.md), [frozen DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml), [package tree](../../../repository/repository-package-tree.md). No architecture/schema/tree change is authorized.
- Accepted [W1 independent re-review 2](../2026-11-12-bf26-dp-b2-preparation/reviews/2026-11-13-bf26-dp-b2-w1-independent-final-re-review-2.md) and [actual-SHA W1 closeout](../2026-11-12-bf26-dp-b2-preparation/owner-publication-closeout-dp-b2-w1-2026-11-13.md). Those historical Public-write successes are not current RO-A acceptance evidence.

**Verified handoff entry facts:** local branch `main`; HEAD, `origin/main` and live `git ls-remote origin refs/heads/main` all equal `7700222673712abc59eb6f1de621abc86c7172cc`; `git status --short` and `git diff --stat` were empty before handoff creation. Preparation publication changed only its nine Markdown documents from the reviewed `3ce4c87d6808c039ffd35ebd7ed67d73659b092c` baseline. Both commits' complete `backend` tree is `f85539956e4672d09ffd267e427618060cc3854b`. Graphify supplied navigation; exact source/test files were inspected. No Java build, tests, PostgreSQL experiment or deployment ran in this handoff session.

Before implementing, inspect saved state, ACTIVE, diff and the six entry hashes in section 9. Expected new handoff/ACTIVE documentation is not material source drift. Investigate other changes; do not reset the worktree, overwrite work, restart counters or treat unrelated drift as approval to expand scope.

## 2. Goal, simplicity decision and exact retained surface

Deliver PUBLIC_READ_ONLY at the business-route authorization boundary by **removing the one PUBLIC/PRIVATE POST matcher for `/api/v1/films`**. Existing `anyRequest().hasAuthority("PROFILE_PRIVATE")` retains PRIVATE POST. Preserve matcher order: existing Auth exceptions, PRIVATE ASYNC/ERROR guard, eight B1 GET exceptions, then PRIVATE fallback. Do not introduce a global method-denial mechanism, new filter/advice, controller rejection rule or transaction wrapper.

| Decision | Implementation consequence |
| --- | --- |
| **REMOVE** sole PUBLIC business-write exception | One matcher removal in the existing security chain; no alternate permission route |
| **KEEP / REUSE** PRIVATE creation | Unchanged runtime `FilmController.create`, `FilmCreateRequestReader`, configured MVC conversion, domain validation and Film/Vault transaction |
| **KEEP / REUSE** B1 reads and Auth | Unchanged minimal DTOs, owner-qualified read snapshots, exact matchers and session authority validation |
| **CORRECT** POST API documentation and affected tests | Existing root customizer/module annotations and real PostgreSQL/MockMvc fixtures; no new framework |
| **DEFER** RO-B and broader work | Future PUBLIC 19 fields / PRIVATE 18 remains ratified but grants no field/name/projection work here |

Relative effort: small production delta; most work is replacing obsolete current PUBLIC-create tests and qualifying denial/PRIVATE/API regressions. This is an estimate, not a promised duration or test count.

The **only eight PUBLIC business method/path pairs after RO-A** are:

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

These are method/path templates, not wildcard access or a count of all registered application routes. Required Auth/profile operations and existing infrastructure exceptions are separate; “no PUBLIC business writes” must not disable login, refresh, revoke or permitted profile transitions. No new GET/list/search/module permission follows.

## 3. Precise source/API/test change map (Amended 6 → 8 files by Owner on 2026-11-13)

`J/` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` = `backend/src/test/java/com/vhvkhangg/personalprivatevault/`. **These are the entire code edit allowlist.** No new production source file is authorized. The Owner explicitly approved expanding the allowlist by two test files on 2026-11-13 to remediate obsolete B2-W1 test expectations (`T4` and `T5`).

| ID | Exact authorized path | Permitted change and preservation boundary |
| --- | --- | --- |
| P1 | `J/authentication/internal/infrastructure/security/SecurityConfiguration.java` | Remove exact PUBLIC authorization for `POST /api/v1/films`, preferably the entire selected POST matcher. Preserve all eight GET matchers, accepted Auth exceptions, conditional provider handling/defaults, session validation/filter wiring, PRIVATE fallback and ASYNC/ERROR ordering. No other security behavior change. |
| P2 | `J/film/internal/web/controller/FilmController.java` | **POST OpenAPI annotations only:** describe PRIVATE-only `CreateFilmRequest`; retain `createFilm`. Do not change runtime creation body, signatures, injected dependencies, request reader, PUBLIC branch, GET/PUT or other handlers. |
| P3 | `J/OpenApiConfiguration.java` | Correct bearer description and Film POST request/201 response documentation to PRIVATE-only. Remove obsolete POST-only PUBLIC-create schema advertisement/registration/references as needed within this file. Keep `createFilm`, all Auth documentation and existing B1 GET schema contracts, including `PublicFilmResponse` used by GET. No global serializer/envelope/cache redesign. |
| T1 | `T/authentication/ProfileRouteAuthorizationIntegrationTest.java` | Registered business allowlist nine→eight; replace PUBLIC POST positive controls with denial for both real origins; qualify no-handler and unchanged domain state. Preserve PRIVATE positive, anonymous, Auth, method/path, sensitive-module, unknown and dispatcher controls. |
| T2 | `T/film/PublicFilmCreateIntegrationTest.java` | Replace obsolete current PUBLIC-create success/parser/media/race expectations with revocation assertions; preserve relevant PRIVATE broad-request/media/charset/validation/committed-transaction and failure regressions. Existing filename may remain. No edits to fixtures/reader/domain classes. |
| T3 | `T/web/OpenApiRouteInventoryIntegrationTest.java` | Generated PRIVATE-only POST request/201 response/security; unchanged `createFilm`; no obsolete PUBLIC-create advertisement or dangling refs. Preserve B1 GET schema/specimen validation and current route/operation inventory checks. |
| T4 | `T/authentication/SecurityFilterChainIntegrationTest.java` | **Owner amendment 2026-11-13:** replace obsolete W1 PUBLIC Film POST `201 Created` expectation with 403 `ACCESS_DENIED`. Preserve all other security assertions. |
| T5 | `T/film/PublicFilmReadIntegrationTest.java` | **Owner amendment 2026-11-13:** replace obsolete W1 PUBLIC Film POST `{}` expectation of `400 VALIDATION_ERROR` with 403 `ACCESS_DENIED` at the authorization boundary. Preserve all read/credit/nested/NSFW assertions. |

Root HTTP support remains at the application root under ADR-0016; Film annotations/runtime adapters remain Film-local and security remains Authentication-owned. No new package, module edge, named capability or shared utility is needed. Existing unaffected tests are **read-only execution/reuse inputs**, including `FilmWebIntegrationTest`, Reference HTTP and `ApplicationArchitectureTests`. If a required correction reaches another source/test file, STOP under section 8.

The existing PUBLIC create branch, reader, request/response DTOs and methods remain in place behind denial. Removing a POST-only generated schema in P3 is not authority to delete Java DTOs or modify B1 GET schema components.

**Relevant engineering skills:** Antigravity uses existing `authentication-security`, `rest-api-http-contracts`, `backend-testing`, `bounded-command-execution`, `java-spring-coding-standards`, `reuse-and-consistency` and `modular-monolith-architecture` guidance, with `film-domain-modeling`/`jpa-postgresql-persistence` only as needed to preserve existing Film/DB test invariants. No new agent, skill, rule, hook or dependency is needed; no design-pattern or persistence redesign is selected.

## 4. User-visible acceptance (all MUST; runtime NOT_RUN at issuance)

| ID / preparation trace | Scenario/input | Required observable result | Minimal proof / reusable implementation |
| --- | --- | --- | --- |
| ROA-01 / RO-02,20 | Persisted PUBLIC from direct login posts a formerly valid five-field body | **403 ACCESS_DENIED**, canonical safe JSON; zero MVC business handler execution and zero Film/Vault/domain mutation | T1/T2; actual persisted session, qualified handler observation and row-value snapshot |
| ROA-02 / RO-01,02,20 | Persisted PUBLIC obtained by real PRIVATE→PUBLIC downgrade repeats POST | Same exact 403/no-handler/no-mutation, while the token is otherwise live and can read an allowed GET | T1/T2; existing two-origin fixtures, distinct actual sessions rather than mock authorities |
| ROA-03 / RO-02,04 | PUBLIC sends broad PRIVATE fields, malformed/duplicate JSON, missing Content-Type or `text/plain` | Same authorization 403 before parsing/validation/business execution; no 400/415/201 and no write | Bounded representative payload/media negatives in T2, both-origin denial coverage; reuse canonical security denial |
| ROA-04 / RO-01,20 | Anonymous POST with formerly valid JSON; invalid/expired/revoked session controls where applicable | Canonical **401 AUTHENTICATION_REQUIRED**, data null, no business handler/write | T1/T2 plus unchanged session/security tests qualified by inputs; invalid token is not a substitute for live PUBLIC 403 |
| ROA-05 / RO-03,20 | PRIVATE posts a broad valid Film request | 201; committed Film and active correct-type FILM Vault row share response ID; original values and exact 18-key response; headers unchanged, **no Location header** | T2 and unchanged `FilmWebIntegrationTest`; independent SQL connection/transaction sees committed rows after HTTP response |
| ROA-06 / RO-03 | PRIVATE JSON, vendor `application/vnd.ppv+json`, ISO-8859-1 bytes; validation and unsupported media failures | Original configured conversion/charset, broad fields, validation/error statuses, no-write failures and standard headers retained | T2's existing PRIVATE media/charset/unknown-key/415 tests, relevant FilmWeb/domain controls; reader/domain unchanged |
| ROA-07 / RO-01,04,15,16 | Actual registered business-route sweep under both PUBLIC origins | Exact discovered allowed set equals eight pairs above, with positive registered owner handlers; POST films and every unselected registered business method/path denied 403 before handler; anonymous protected requests 401; complete seeded domain snapshot unchanged by denials | T1 `registeredRoutesDeniedToPublicTokenFromBothOrigins`; retain unrelated/sensitive People/Personal/Finance/Vault/Media/account/Drive/RAG denials and PRIVATE controls, not just Film |
| ROA-08 / RO-04,17,18 | Method/path variants, HEAD/OPTIONS, unknown/suffix/subpaths, ASYNC/ERROR and Auth exceptions | No PUBLIC write or unselected read grant; preserved accepted Auth/infrastructure rules, dispatcher priority and persisted-session revalidation; PRIVATE behavior unchanged | T1 existing named controls and `SecurityFilterChainIntegrationTest`; preserve established path-normalization rejection statuses rather than inventing universal 403 |
| ROA-09 / RO-19 POST portion | Actual generated `/v3/api-docs` | `POST /api/v1/films` explicitly PRIVATE-only with bearer requirement; operation ID `createFilm`; broad `CreateFilmRequest`, 201 full `ApiResponseFilmResponse`, canonical 401/403 and existing errors. No PUBLIC POST permission, PublicCreateFilmRequest variant/advertisement or dangling refs | T3 actual graph/specimens plus T1 profile API controls; POST may retain existing wrapper names only if they resolve solely to PRIVATE schemas |
| ROA-10 / RO-01,19 existing GET portion | Both selected work GETs and six Reference GETs | Existing payloads/status/headers unchanged: Fiction exactly six PUBLIC keys, Film exactly seven; PRIVATE shapes unchanged; Reference list/detail DTOs unchanged. Existing NSFW/trash/type privacy and B1 read snapshot semantics retained | T1 positives, T3 unchanged B1 specimen/schema controls and source-applicable Public read/Reference evidence; no RO-B fields/names |
| ROA-11 / RO-03,20–22 | PRIVATE transaction failure/visibility controls and frozen compatibility | No partial Film/Vault commits on relevant insertion/deferred-commit failures; existing application transaction intact; architecture/Modulith compatibility and frozen schema/module graph unchanged | Preserve/adapt applicable T2 transaction controls, valid historical unchanged domain evidence, architecture execution plus exact diff/frozen-file checks |
| ROA-12 / RO-22 | Completed implementation handback | Actual final source hashes, command receipts/XML/case identities, fresh vs reused qualification, limitations, self-review and Docs Sync; no acceptance/publication/deployment claims | Section 6 and handback artifacts in section 7; independent Codex decides acceptance |

Canonical denied POST JSON is the existing envelope, not a new transport contract:

```json
{"data":null,"error":{"code":"ACCESS_DENIED","message":"Access is denied","fieldErrors":[]},"meta":null}
```

Anonymous POST uses code `AUTHENTICATION_REQUIRED` and the existing safe message `Full authentication is required to access this resource`, with the same null data/meta and empty field errors. Assert semantic JSON rather than property ordering, JSON UTF-8, no payload/private diagnostic disclosure, existing security cache prevention including a `no-store` directive, and no resource Location/Content-Disposition. Do not require a newly formatted Cache-Control string or copy PUBLIC GET's exact header onto PRIVATE responses.

PRIVATE Film response keys remain exactly: `id,title,originalTitle,nationalityCode,posterUrl,format,productionStyle,isNsfw,directorPersonId,description,totalEpisodes,progressStatus,consumptionStatus,currentProgressText,review,genreIds,storyArchetypeIds,worldSettingIds`. PUBLIC Fiction GET remains `id,title,originalTitle,nationalityCode,format,isNsfw`; PUBLIC Film GET adds only `productionStyle` to those six. No new name key is accepted here.

## 5. Negative/failure evidence and test adaptation

1. **Real authority and qualified observation:** reuse `AbstractWebIntegrationTest.publicBearerToken()` and `publicBearerTokenViaDowngrade()` or their actual HTTP flows. Confirm persisted profile/session/generation and live allowed-GET controls. Use the existing actual MVC `RequestMappingHandlerAdapter.handle` spy in T1, first positively observed on a successful PRIVATE Film POST (qualify that the Film create handler was observed), then clear invocation history immediately before denied requests and assert no handle call. A service/Vault spy alone is insufficient. Avoid clearing the positive evidence before recording it or treating a mocked/invalid token as a legitimate PUBLIC origin.
2. **No mutation:** reuse `ProfileAuthFixture.seedDomain` and `domainSnapshot` row-value snapshots (not only counts). Create/configure tokens and positive controls **before** taking the denial baseline. Compare at least Film/Vault and the fixture's covered business tables, retaining sensitive seeded values and empty-table checks; authentication/session fixture writes are not business mutation caused by the denied POST. HTTP denial must not open the Film create transaction. Do not claim a snapshot covers the fixture's excluded auth/RAG tables.
3. **Replace obsolete PUBLIC behavior, keep PRIVATE regressions:** audit every T2 method expecting PUBLIC 201/400/415, Public-created follow-up GET, or a barrier reached after PUBLIC admission. Convert those HTTP assertions to 403/no-handler/no-write, consolidate redundant parser-only cases where useful, and remove obsolete PUBLIC-success barriers so tests cannot wait for an unreachable handler. Preserve relevant PRIVATE/media/validation and transaction rollback controls, retargeting existing SQL observer/fault fixtures to legitimate PRIVATE creation where needed. Do not preserve a PUBLIC 201 expectation or use direct controller invocation to bypass security. Keep the historical W1 source/reviews/receipts intact; document method dispositions without claiming those original runtime outcomes were false.
4. **Session and dispatcher preservation:** no changes to JWT/session authority, admission or cancellation guarantees. Reuse source-applicable DP-A/B1 evidence for unchanged invalidation races; new requests with invalidated authority stay denied. Already-admitted-request limitations remain as accepted. T1's `errorAndAsyncDispatcherRequiresPrivate`, `actualAsyncRedispatchRevalidatesPropagatedContextWithoutBearer`, `sameUriAsyncAndErrorDispatcherDeniesPublicSession`, `methodAndPathVariantsOnPublicRoutesDenyNonGetAndSubpaths`, Auth and unknown-route controls remain applicable; include POST in the revoked Film collection's method controls. Retain genuine async/context propagation proof, not only a dispatcher label.
5. **API specimens:** T3's existing `openApiContractContainsAllRoutesAndUniqueOperationIds` includes obsolete PUBLIC POST success/schema specimens. Replace that part; preserve the validator's unresolved-reference failures and existing six/seven-key GET/type/enum/extra-field negatives. Check the entire generated reference graph and bearer description, not just a string in the annotation. A PRIVATE body containing only the formerly public five fields may still be valid PRIVATE input; schema validation does not establish PUBLIC permission.
6. **PRIVATE transaction evidence:** keep committed HTTP/DB proof fresh for the changed tests. Qualify retained SQL observer/insertion/deferred-commit rollback evidence against unchanged production and applicable PRIVATE test inputs; rerun controls changed to PRIVATE. Do not claim former PUBLIC-201 receipts prove the new denial or blindly relabel a PUBLIC-specific failure as a fresh PRIVATE test.

## 6. Finite implementation verification gates

**Preparation coverage transferred:** authorization/privacy and safe errors → ROA-01–04/07–08; HTTP/validation/OpenAPI → ROA-05–06/09–10; transactions/database and module ownership → ROA-05/11; evidence/docs/hygiene → ROA-12 and sections 7–9. All runtime outcomes remain NOT_RUN/NOT_VERIFIED until implementation evidence exists. New retrieval/OCR/model evaluation, performance targets and dependency changes are N/A because this slice adds none; this is not a full-project audit or a claim of zero diagnostics.

Use existing MockMvc, Testcontainers PostgreSQL and `scripts/run-bounded-command.py`. No H2 substitute, real Owner database, native browser, provider activation, new benchmark, full-suite or extended-time run is required/authorized here. The normal hard cap is **300 seconds per command**, including Maven startup. Existing Maintenance B1/B2 runner and native hook limits remain; do not bypass hooks or repair unrelated startup/trust/configuration failures.

| Gate | Exact existing Maven `-Dtest` selector | Acceptance coverage |
| --- | --- | --- |
| G-ROA-SECURITY | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | ROA-01–04/07–08; registered eight-pair sweep, live sessions, qualified denial, Auth/dispatch variants |
| G-ROA-PRIVATE | `PublicFilmCreateIntegrationTest,FilmWebIntegrationTest` | ROA-01–06/11; converted revocation cases, exact PRIVATE HTTP/media/validation and committed/rollback controls |
| G-ROA-API | `OpenApiRouteInventoryIntegrationTest` | ROA-09–10; real generated POST contract, retained GET graphs/specimens, reference integrity |
| G-ROA-READ | `PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest,ReferenceWebIntegrationTest` | ROA-10; existing B1 privacy/read responses and Reference compatibility |
| G-ROA-ARCH | `ApplicationArchitectureTests` | ROA-11; current Modulith/ownership checks; diff/schema qualification remains separately required |

From repository root, PowerShell example **for implementation, not executed at issuance**:

```powershell
$roaPython = (Get-Command python -ErrorAction Stop).Source
$roaRunDir = Join-Path 'logs/bf26-dp-ro-a' ('implementation-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $roaRunDir -ErrorAction Stop | Out-Null
& $roaPython scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $roaRunDir 'G-ROA-SECURITY.json') -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest' test
```

Execute selectors sequentially with the matching gate receipt name. Do not run Maven groups concurrently against shared `target`/Surefire or mutable fixtures. If a group exceeds the cap, preserve its failed/timeout attempt and split into bounded disjoint exact class/method selectors; reconcile the complete case union without raising the limit. Use fresh receipt names for retries; the runner cleans only its owned process tree. Nonzero exit needs prompt inspection, not an unbounded retry loop.

**Final gate:** every changed test class must be executed on final applicable source (full class or documented complete disjoint selectors). Run affected security/PRIVATE/API and architecture checks; unchanged READ/domain/session evidence may be reused **only after source applicability is verified**, including the changed security boundary's effect. The fresh T1/T3 controls must establish retained GET admission/schema, even when unchanged payload/snapshot evidence is reused. Existing Film domain/credit/link/genre tests may be executed or source-qualified without modifying them; do not add a new general full-suite obligation.

Preserve each run's Surefire XML before another run overwrites it. The handback must map ROA-01–12 to exact final test methods/cases, commands, receipt/log/XML paths, actual exits/outcomes and final input hashes. Distinguish fresh execution from source-qualified reuse; for reuse name original case/receipt/XML, production/test/fixture/POM/resource/config inputs, changed-boundary impact and remaining environment limits. Report failures/timeouts/skips/disabled/not-run cases explicitly. Deduplicate testcase class/name/parameters; do not invent counts, count assertions as cases, add corroborating W1 runs to distinct totals or report old XML as fresh. Missing mandatory acceptance remains **NOT_VERIFIED** and blocks implementation acceptance.

## 7. Documentation impact, handback and definition of done

This handoff creation edits **only this new executable and ACTIVE**. The six-file boundary is the complete code boundary; workflow result artifacts below do not authorize other code or governance cleanup.

| Artifact / owner | Authorized timing and observable evidence |
| --- | --- |
| This executable and [ACTIVE](../../handoffs/ACTIVE.md) / Codex now; Antigravity result fields later; independent Codex disposition later | Current scope/status/counters/next action only; preserve original contract and all historical checkpoints. Antigravity fills result links and changes status to IMPLEMENTED_AWAITING_CODEX_REVIEW after actual tests/self-review. |
| `implementation-handback-dp-ro-a-2026-11-13.md` / Antigravity, NEW in this directory | Exact six-file diff, criterion→method/case/receipt trace, final hashes, fresh/reused evidence, obsolete test dispositions, limitations and Docs Sync Check. No acceptance/publication claim. |
| `self-review-dp-ro-a-2026-11-13.md` / Antigravity, NEW in this directory | Mandatory `/antigravity-self-review` result: scope, minimal matcher change, preserved PRIVATE/B1 behavior, API reference/DTO safety, meaningful evidence and remaining issues. Self-review is not independent acceptance. |
| New `reviews/` final-review artifact / independent Codex | Actual source/test/evidence assessment, findings, counters and acceptance disposition; never rewrite prior reviews. |
| Preparation README/current navigation, root `AGENTS.md`, `README.md`, `docs/workflow/current-owner-delivery-policy.md`, `docs/roadmap.md` and `docs/implementation/maintenance/README.md` where affected / ChatGPT under separate scoped governance/closeout work | These dated current-looking notices may still describe accepted W1 or unpublished preparation. Report drift; do not edit them in implementation. After independent acceptance and Owner push, verify actual RO-A SHA and perform brief publication closeout/current-navigation sync. |

**Docs Sync Check at handoff issuance:** **UPDATED** this executable/ACTIVE with current exact Owner authority, published preparation, RO-A-only route and READY_FOR_IMPLEMENTATION; **ALREADY_CONSISTENT** six source/test entry hashes, unchanged backend tree, independent FR-RO-PH-01 closure and deferred RO-B boundary; **DEFERRED_OUT_OF_SCOPE** dated preparation/root/policy/roadmap/index navigation above, old harness notices and LOW backlog corrections (ChatGPT governance owner); **NOT_VERIFIED** all RO-A runtime results, live-data suitability and deployment. Dated preparation author/reviewer statements remain historical. No material unresolved Owner choice remains for this handoff.

At handback and independent review, repeat the scoped Docs Sync Check with actual HEAD/diff, edited docs, current behavior versus planned behavior, linked evidence, remaining drift and responsible editor. Frozen schema/tree/ADRs, original reviews and raw receipts remain untouched. No repository-wide cleanup is part of RO-A.

**Definition of done for Antigravity:** only authorized changes; ROA-01–12 evidence produced, final finite gates passed/source-qualified with mandatory gaps resolved; PRIVATE/B1/source ownership preserved; safe failure/transaction and generated API proof; failed attempts retained; mandatory self-review completed; result artifacts linked. Set **IMPLEMENTED_AWAITING_CODEX_REVIEW**. Do not set independently accepted, READY_FOR_OWNER_COMMIT, OWNER_PUBLISHED or CLOSED. Independent Codex must resolve any CRITICAL/HIGH/MEDIUM or mandatory NOT_VERIFIED before Owner publication; LOW/INFO remains backlog.

## 8. Exclusions, STOP rules and preserved state

**MUST NOT:** implement RO-B's PUBLIC 19-field Fiction/Film or author/director names; add PUBLIC GET/list/search/module access; allow PUBLIC Fiction/Film update/delete/credit/genre/other mutation; modify Fiction, People, Vault, Reference or other production modules; modify/delete PUBLIC Java DTOs/read projections or `FilmCreateRequestReader`; change create transaction/domain validation; add schema/Flyway/DBML/index/module descriptor/dependency/build/provider changes; refactor unreachable code; implement Frontend/BFF, RAG, DP-C, B2-W2/S/V, Recycle Bin or final audit. No deployment/cutover or live-data suitability claim.

**STOP and report the exact missing source/capability, evidence, impact and smallest separate Owner decision** if any required change exceeds the six files, modifies P2 runtime instead of POST annotations, changes Auth/session/private/B1 contracts, needs a global method-denial/filter/serializer, requires a new dependency/schema/module edge, or cannot obtain mandatory proof within existing tools and scope. Also stop for conflicting live handoff, material unapproved source/authority drift or exhausted remediation. Do not use an easier contract relaxation, edit an unaffected test to hide failure or auto-expand the handoff. An infrastructure failure is not automatically a product defect or permission to repair it.

Preserve separate predecessor state: W1 independently accepted/Owner-published at `40fb8a61b6529585927e0a7dd524e9a363c69b6e`, remediation **2/3 consumed / STOPPED**, takeover NOT_AUTHORIZED/NOT_USED; B1 at `277522ee5b89ce5d2d8200cb955654b031b9f97a`, remediation **3/3 consumed / STOPPED**, takeover NOT_AUTHORIZED/NOT_USED; DP-A at `5c1da4e5404ee3c724e3fc2e03569d4093bcb44d`, remediation **3/3 exhausted**, exceptional takeover **1/1 accepted**. `DPA-L01`, `DPB1-L02`, `DPB2W1-L01` remain LOW/OPEN/BACKLOG. Preserve historical test successes/failures/receipts and all counters; revoked future permission does not invalidate accepted W1 history.

H1-F remains paused/not independently accepted, FR-13 upstream-blocked; P17 FROZEN, P18 CLOSED, S19-2 SUSPENDED, indexing INACTIVE, live external originals DISABLED/DEFERRED, provider flags OFF/unset. Local-ON disposable regression fixtures already present in accepted tests do not authorize production provider activation. No agent stages/commits/pushes/tags or creates/merges a PR.

RO-A starts at **0/3**. After an initial blocking independent review, at most three Antigravity correction + independent re-review rounds apply to this same slice; no reset per finding/session. After a third still-blocked round, stop for an exact Owner decision. Takeover remains **NOT_AUTHORIZED**, never automatic and never self-accepted.

## 9. Eight-file entry and final fingerprint

IDs resolve to the exact complete paths in section 3.

| ID | Entry SHA-256 | Published Git blob | Final SHA-256 |
| --- | --- | --- | --- |
| P1 | `390f73b282100a6667494fecba54e160478878b2164d9a2817b5a2ef40acd6f6` | `30f4542f72762e82941af24d54a3f515b75f8e0a` | `c5753d4998b2bc60014e821d08dd42c079086becf12db467d7bcbea58e15e617` |
| P2 | `7b8cdafa2b89a4e5bdb5bf7ec1e5dff22e43c2498213b2324c2ded548359967f` | `c047a3aef9b8d0d8181bf1935679a12ce8001eb7` | `f87d39e3871410081ee624efc5ffbb9a5c5df74e5a2746c7de51200be08cf1ea` |
| P3 | `0a41b699327ddc08afb59fbb56dacd5377ac28f9ce8b22b74b4bbee4e35a3237` | `7c06843c8c765d43e0197f9587aae159bd93df01` | `faaad86f9122ea6653054c3117fddf1fe4886c2275a0fa9d97af88554f295289` |
| T1 | `244a36e005355f7e4b9c6bdb3f02c60c0ffb60e8bc9be9980f46c5b1dc87169b` | `54be95a52feca4efb004c6a3ca0b30876f5d1caf` | `66c3c623ffff8a3e5ec03ca1a9b7136155c77a572d4534b976e7444bc2d85be9` |
| T2 | `fa83914e8902c8b1d001fbf59a2df03453db67280169a0f4cdd6e9aeb571f19e` | `a18e2250c7a6d54dca19e6c88fe2d4723500157f` | `be3094bda9640ff499f2ac3a7dd071f8e94cd4c6b56aae232783ce58be7e69f3` |
| T3 | `f1370d4be77b28fa1dab6831a718f165bd6a7c09fbf42b5efb3374ac73daa0f0` | `3185484841aae61f5c0e4959a55c9b91901ed284` | `91adc3f0d91c82cdff9ada13cb5c52616a66328e29b02d82f87b107bfabda857` |
| T4 | `3ecf02f901968846174a7b45889ff30ccb8720875c7b398691515bbcf533b63d` | — | `4c444d5690755ef7ebad17c00845c5bd7b9fac054162b32bf22ca60bd7dce9aa` |
| T5 | `bb6937107db8fa240751e18d6e355799335a9647dc93675f92270d4bbf5744cb` | — | `26f95458c3ba97fcf35199f6c673032495d6f7793a485443e1c942c9259f4d65` |

## 10. Implementation result — Antigravity fills after execution

**IMPLEMENTED_AWAITING_CODEX_REVIEW (Remediation Round 1).** Remediation Round 1 completed addressing FR-ROA-01, FR-ROA-02, FR-ROA-03, and DPROA-L01. All 5 finite verification gates executed fresh under bounded watchdog (`scripts/run-bounded-command.py`, normal profile, <= 300s):
- `G-ROA-SECURITY`: 27/27 PASS (`ProfileRouteAuthorizationIntegrationTest` 15, `SecurityFilterChainIntegrationTest` 12)
- `G-ROA-PRIVATE`: 22/22 PASS (`PublicFilmCreateIntegrationTest` 21, `FilmWebIntegrationTest` 1)
- `G-ROA-API`: 1/1 PASS (`OpenApiRouteInventoryIntegrationTest` 1)
- `G-ROA-READ`: 29/29 PASS (`PublicFictionReadIntegrationTest` 13, `PublicFilmReadIntegrationTest` 13, `ReferenceWebIntegrationTest` 3)
- `G-ROA-ARCH`: 38/38 PASS (`ApplicationArchitectureTests` 38)
- Total: 117 tests PASS across 9 test classes, 0 failures, 0 errors, 0 skipped.
- Receipts preserved in `logs/bf26-dp-ro-a/remediation-round1/`.
- Handback: [implementation-handback-dp-ro-a-2026-11-13.md](implementation-handback-dp-ro-a-2026-11-13.md).
- Self-review: [self-review-dp-ro-a-2026-11-13.md](self-review-dp-ro-a-2026-11-13.md).

## 11. Codex remediation / independent final review

**READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED — Round 1.** [Independent final re-review 1](reviews/2026-11-14-bf26-dp-ro-a-independent-final-re-review-1.md) CLOSED **FR-ROA-01/02/03** after exact FilmController.create observer qualification, representative PRIVATE validation/domain no-write negatives and actual criterion/case/receipt/XML reconciliation. Producer 117 distinct cases / nine classes qualify; fresh reviewer changed classes passed 36/36, exit 0, 147.375s, with no failures/errors/skips; these are corroborating cases, not additions to 117. Evidence: `logs/bf26-dp-ro-a/codex-rereview1-20261114-b7d2e4/`. DPROA-L01 LOW/OPEN/BACKLOG remains for limited residual wording, not another correction round. Initial review, original failures and producer submission remain preserved. Remediation **1/3 consumed / STOPPED**, takeover **NOT_AUTHORIZED / NOT_USED**; no unused-allowance transfer or automatic further work. Owner publication and brief actual-SHA closeout remain pending.

**Next step: Owner → ChatGPT | Owner normal git add/commit/push of accepted RO-A delivery, then brief actual-SHA publication closeout/current-navigation sync | BF26-DP-RO-A ONLY, this executable, ACTIVE and independent final re-review 1 | READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED, unpublished; no RO-B/deployment grant | RO-A remediation 1/3 consumed / STOPPED; takeover NOT_AUTHORIZED / NOT_USED.**
