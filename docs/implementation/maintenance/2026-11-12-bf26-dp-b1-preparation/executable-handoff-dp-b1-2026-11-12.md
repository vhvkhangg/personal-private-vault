# Executable slice handoff — BF26-DP-B1

- **Handoff ID / date / author:** BF26-DP-B1 / 2026-11-12 / Codex.
- **Status: READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED.** [Independent final re-review 3](reviews/2026-11-12-bf26-dp-b1-independent-final-re-review-3.md) CLOSED **FR-DPB1-01** and retained FR-DPB1-02/03/04 closures; no open CRITICAL/HIGH/MEDIUM or mandatory acceptance NOT_VERIFIED remains. Fresh reviewer 26/26 PASS; final 127 cases source-qualified (26 fresh R3, 101 explicitly reused), all 1,916 input hashes and 52 method references validate. DPB1-L01 CLOSED; DPB1-L02 LOW/OPEN/BACKLOG wording is nonblocking. Remediation 3/3 consumed and stopped; takeover NOT_AUTHORIZED. Owner publication and actual-SHA closeout are pending; no deployment/cutover or B2/C authorization.
- **Owner authority:** the Owner's 2026-11-12 conversation decision, **APPROVED — BF26-DP-B1 ONLY / OWNER_AUTHORIZED_FOR_B1_HANDOFF_AND_BOUNDED_IMPLEMENTATION**, recorded in section 1. This handoff does not authorize Codex production implementation in the handoff-creation session.
- **Entry commit:** published `main` `066e361ac9938dfdc102168969fe8a498cf4ee2f`.
- **Implementer:** Antigravity, existing [backend-implementer](../../../../.agents/agents/backend-implementer/agent.md) role; `/antigravity-implement-handoff`, then mandatory `/antigravity-self-review`.
- **Reviewer:** a separate independent Codex `$codex-final-review` session; implementer checks are not independent acceptance.
- **Counters:** B1 remediation **3/3 consumed**, takeover `NOT_AUTHORIZED`.
- **Execution register:** [ACTIVE](../../handoffs/ACTIVE.md). Owner alone stages, commits and pushes an independently accepted delivery.

## 1. Authority, baseline and sources of truth

**Owner-approved decision, distinct from observed source facts:** the current authorization opens exactly eight authenticated GET operations, two dedicated PUBLIC DTOs, owner-local D10 projection reads, narrowly affected Security/HTTP/OpenAPI source and tests listed below. It expressly qualifies the frozen Phase 2 Authentication, Phase 4 Fiction, Phase 5 Film and Phase 13 HTTP/API boundaries. Phase 1 Reference is exposed only through Security matchers; **no Reference production-source edit is authorized**. There is no migration, index, DBML, dependency, module-edge or global transaction-manager exception. A necessary change beyond this map requires the smallest additional exact Owner decision before editing.

This decision supersedes the preparation-only/no-B1-authority wording in the published preparation and older current-looking notices **only for B1**. It does not amend D1–D11, independently accept B1, publish this handoff or reopen accepted DP-A. Do not repeat the D7–D11 vote.

**Fresh handoff-entry facts:** local branch `main`, HEAD and `origin/main` equal the entry SHA; live `git ls-remote origin refs/heads/main` also returned that SHA. Entry worktree and index were clean. That commit publishes the ten-file reviewed B1 preparation; its source baseline was `6374537074afc32ed7e939cf94ed2f21d77c5f42`. Graphify executable is available but `graphify-out/graph.json` is absent; targeted canonical source reads were used. No application test, PostgreSQL process, provider or browser was launched during handoff creation.

Binding references, read with the current Owner authorization:

- [Independent B1 pre-handoff review](reviews/2026-11-12-bf26-dp-b1-independent-pre-handoff-review.md): **READY_FOR_HANDOFF**, no blocking preparation findings; its D10, dispatch, cache, deny-sweep and generated-schema qualifications are mandatory below.
- [D7–D11 Option A ratification](owner-ratification-d7-d11-2026-11-12.md), [original decision docket](owner-proposed-d7-d11.md), [exact field/route policy](exact-route-and-field-policy.md), [visibility contract](visibility-consistency-and-security.md), [B1-01–24](acceptance-and-harness.md), [source inventory](source-baseline-and-static-inventory.md). Their pending-review/source-authority sentences are historical preparation checkpoints.
- [D1–D6 ratification](../2026-11-11-dual-login-profiles-preparation/owner-ratification-d1-d6-2026-11-11.md), [accepted DP-A final review](../2026-11-11-dual-login-profiles-preparation/reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md), [DP-A publication closeout](../2026-11-11-dual-login-profiles-preparation/owner-publication-closeout-dp-a-2026-11-12.md). DP-A source `5c1da4e5404ee3c724e3fc2e03569d4093bcb44d` is independently accepted/published/closed; FR-DPA-01–09 CLOSED, DPA-L01 LOW/OPEN/BACKLOG.
- [Owner policy](../../../workflow/current-owner-delivery-policy.md), [Docs Sync](../../../workflow/documentation-synchronization.md), [handoff workflow](../../handoffs/README.md), [handoff template](../../../templates/handoff.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [dependency matrix](../../../architecture/module-dependency-matrix.md), [frozen package tree](../../../repository/repository-package-tree.md), frozen [DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml).

At implementation entry recheck branch, HEAD, index, worktree, live remote and ACTIVE. Preserve the handoff files as expected local changes; do not reset/clean them. Reconcile material source drift before implementing, without discarding unrelated Owner work. No branch/worktree, agent, hook or dependency change is needed.

## 2. Goal, simplicity and explicit exclusions

Deliver one reviewable Backend slice: PUBLIC may read eligible Fiction/Film direct details through a fixed minimal response, and six existing Reference catalog GETs. PRIVATE retains accepted behavior. Security grants, eligibility, DTOs, cache controls and tests must be delivered together; opening routes before their data restrictions is not an intermediate accepted result.

| Decision | Smallest adequate mechanism / burden estimate |
| --- | --- |
| KEEP / REUSE | Eight exact method/path matchers and existing DP-A persisted session admission; small Security adapter change, no duplicated authority algorithm. |
| KEEP | Two immutable PUBLIC views and two owner HTTP DTOs with explicit scalar mapping; small owning query/adapter changes. |
| KEEP / SIMPLIFY | Each existing owning service adds one proxied REQUIRES_NEW, read-only REPEATABLE_READ operation calling existing Vault capability; concentrated transaction/race verification is the largest B1 effort. |
| REUSE | Reference handlers, DTOs, catalog ordering/errors, canonical root envelope, existing HTTP/PG/Modulith harness and bounded runner. |
| DEFER | Lists, Search, taxonomy, credits, links, writes, generic visibility/authorization/serialization engines and cache frameworks; no demonstrated B1 need. |

These are relative engineering estimates, not runtime/duration promises. A predicate in two READ COMMITTED reads or redaction of the existing full DTO is insufficient. No speculative pattern layer is requested.

**MUST NOT:** PUBLIC browse/list/pagination/Search; Fiction/Film genres/classifications/story-archetypes/world-settings/links/credits, including `/api/v1/films/credits/{creditId}`; Reference platforms or unselected catalogs; any PUBLIC write; Vault/People/Media/Personal/Feed/Finance/Drive/Portability/RAG grants; new visibility fields; session/JWT/credential/Google algorithm changes; Frontend/BFF; provider activation; DP-B2/DP-C, Recycle Bin or final non-RAG audit; unrelated cleanup/refactoring/upgrades. No Flyway edits/additions (V1–V29 retained), DDL/index/schema/DBML or module-dependency changes. No global HTTP serializer/envelope redesign or new Vault API.

H1-F remains FRONTEND_PAUSED_BY_OWNER / NOT_INDEPENDENTLY_ACCEPTED / CHANGES_REQUESTED, FR-13 MEDIUM/OPEN/UPSTREAM_DEPENDENCY_BLOCKED. P17 frozen, P18 closed, S19-2 suspended/NOT_ACCEPTED/ARCHIVAL_ONLY, indexing inactive, live originals disabled/deferred and provider defaults OFF/unset. DP-A remediation 3/3 exhausted and takeover 1/1 accepted, Frontend and all other counters remain unchanged.

## 3. Exact executable change map

Production paths below are relative to **`backend/src/main/java/com/vhvkhangg/personalprivatevault/`**. Only the named files are editable; a row is permission for its described B1 delta, not a whole-file refactor. No production deletes.

| Exact existing/new file | Permitted change and named operation |
| --- | --- |
| `authentication/internal/infrastructure/security/SecurityConfiguration.java` | Existing `securityFilterChain`: only eight exact GET grants, after the ASYNC/ERROR guard and before PRIVATE fallback. No decoder, encoder, entry-point, session-authority, credentials or provider behavior changes. |
| `fiction/fiction/FictionOperations.java` | Add `Optional<PublicFictionView> findPublic(Long fictionId)`; retain all current methods. |
| `fiction/internal/application/FictionService.java` | Implement `findPublic` with section 6 transaction and eligibility; leave existing `find`, writes and classifications behavior unchanged. |
| `fiction/internal/infrastructure/persistence/FictionRepository.java` | Add `Optional<PublicFictionView> findPublicProjectionById(Long fictionId)`, owner-only scalar query with `isNsfw=false`. |
| `fiction/view/PublicFictionView.java` — NEW | Immutable record of exactly the six allowed scalar fields in section 5; existing `view` named interface/package is reused. |
| `fiction/internal/web/dto/PublicFictionResponse.java` — NEW | Exactly the six PUBLIC JSON properties; DTO-local schema annotations if needed. |
| `fiction/internal/web/mapper/FictionWebMapper.java` | Add `toPublicResponse(PublicFictionView)`; retain current full-view `toResponse` and command/relation mappings. |
| `fiction/internal/web/controller/FictionController.java` | Only existing `get` / `getFiction` branches on validated authority, invokes `findPublic`, maps minimal DTO and applies PUBLIC no-store on 200/404; accurate operation annotations. Other handlers unchanged. |
| `film/film/FilmOperations.java` | Add `Optional<PublicFilmView> findPublic(Long filmId)`; retain current methods. |
| `film/internal/application/FilmService.java` | Implement `findPublic` as section 6; preserve PRIVATE `find`, mutation/credit/genre/link/classification behavior. |
| `film/internal/infrastructure/persistence/FilmRepository.java` | Add `Optional<PublicFilmView> findPublicProjectionById(Long filmId)`, owner-only scalar query with `isNsfw=false`. |
| `film/view/PublicFilmView.java` — NEW | Immutable record of exactly the seven allowed scalar fields; reuse existing `view` named interface/package. |
| `film/internal/web/dto/PublicFilmResponse.java` — NEW | Exactly the seven PUBLIC JSON properties; DTO-local schema annotations if needed. |
| `film/internal/web/mapper/FilmWebMapper.java` | Add `toPublicResponse(PublicFilmView)`; preserve full DTO and other mappings. |
| `film/internal/web/controller/FilmController.java` | Only existing `get` / `getFilm` branch, PUBLIC cache/errors and operation documentation. Direct/nested credit handlers remain PRIVATE-only. |
| `OpenApiConfiguration.java` | Narrow bearer-description update, exact eight-operation descriptions if needed, and schema customization only for the two detail GET response alternatives. Reuse existing root OpenAPI infrastructure; section 7 limits apply. |

Test paths are relative to **`backend/src/test/java/com/vhvkhangg/personalprivatevault/`**. Existing files were confirmed on entry. New tests stay in existing owning packages, with any test-only helpers/configuration nested in these files.

| Exact existing/new test file | Bounded responsibility |
| --- | --- |
| `authentication/ProfileRouteAuthorizationIntegrationTest.java` | Exact eight-pair sweep exception set, correct handler positives, remaining denials/no mutation, real sessions from both origins, dispatcher precedence and existing provider-ON controls. |
| `authentication/SecurityFilterChainIntegrationTest.java` | Applicable exact-method/security/ANON/PRIVATE regression and invalid-session controls. |
| `authentication/fixture/ProfileAuthFixture.java` — only if needed | Synthetic seed/snapshot support; preserve complete row/column comparisons and real session authority. |
| `support/AbstractWebIntegrationTest.java` — only if needed | Minimal real token/fixture compatibility; no global security or production bypass. |
| `fiction/FictionWebIntegrationTest.java` | PRIVATE schema/behavior, PUBLIC HTTP allowlist/error/cache checks where appropriate. |
| `fiction/FictionIntegrationTest.java` | Changed owning read/domain regression; no mutation contract redesign. |
| `film/FilmWebIntegrationTest.java` | Corresponding HTTP compatibility and denied credit/nested routes. |
| `film/FilmIntegrationTest.java` | Changed owning read/domain regression. |
| `reference/ReferenceWebIntegrationTest.java` | Six allowed and six denied GETs, ordering/DTO/errors, ANON/PRIVATE controls. |
| `reference/ReferenceModuleIntegrationTest.java` | Retained catalogs/ordering/lookups; edit only if actually necessary. |
| `web/OpenApiRouteInventoryIntegrationTest.java` | Two response alternatives, exact DTO properties/nullability, generated graph/actual HTTP specimens, unchanged inventory/IDs/security and other schemas. |
| `fiction/PublicFictionReadIntegrationTest.java` — NEW | Focused real PG/HTTP D10 eligibility, propagation/isolation and forced-race proof, safe projection and HTTP contract. |
| `film/PublicFilmReadIntegrationTest.java` — NEW | Equivalent Film proof; no generic concurrency framework. |

Run existing `ApplicationArchitectureTests.java` unchanged for module/ADR-0016 ownership proof; it is **not** an editable file. Read-only dependencies include Vault entry capability/view/service, feature entities/full DTOs/package descriptors, Reference production, DP-A filter/session services, `ApiResponse`, `ApiResponses`, shared error advice and `AbstractPostgresIntegrationTest`. None receives an implicit edit exception. If new test infrastructure/files outside the two new tests are necessary, stop with the exact proposed delta.

Documentation writes: this executable handoff's result fields and `docs/implementation/handoffs/ACTIVE.md` status fields; new `implementation-handback-dp-b1-2026-11-12.md`, `implementation-self-review-dp-b1-2026-11-12.md`, and `api-contract-dp-b1-2026-11-12.md` **in this B1 folder**. The API addendum is the directly affected current contract document: eight routes, exact schemas, cache/error semantics and generated-doc evidence. No rewrite of preparation ratification/review/history or unrelated root notices. A separate reviewer writes a new dated independent review under this folder's `reviews/`.

## 4. Matcher, dispatcher and session contract

The entire new business allowlist is the following **method + declared path pattern** set; both validated profiles may invoke it. Preserve operation IDs.

| Method | Exact pattern | Existing handler operation ID |
| --- | --- | --- |
| GET | `/api/v1/fictions/{id}` | `getFiction` |
| GET | `/api/v1/films/{id}` | `getFilm` |
| GET | `/api/v1/reference/countries` | `listCountries` |
| GET | `/api/v1/reference/countries/{code}` | `getCountry` |
| GET | `/api/v1/reference/languages` | `listLanguages` |
| GET | `/api/v1/reference/languages/{code}` | `getLanguage` |
| GET | `/api/v1/reference/currencies` | `listCurrencies` |
| GET | `/api/v1/reference/currencies/{code}` | `getCurrency` |

1. Keep existing health/docs/Auth and conditional Google/Drive exceptions and their order/semantics. Keep `SessionAuthorityFilter` after bearer authentication; no auth lookup/claim parsing is copied into feature modules.
2. Existing ASYNC/ERROR `.hasAuthority("PROFILE_PRIVATE")` must precede **all new B1 grants**. Then add precisely the eight GET patterns with `.hasAnyAuthority("PROFILE_PUBLIC", "PROFILE_PRIVATE")`; retain `.anyRequest().hasAuthority("PROFILE_PRIVATE")`. No broad `/**`, `authenticated()` business grant or `permitAll` for B1.
3. A controller chooses PUBLIC only from request-scoped authorities established by the real DP-A filter, e.g. injected `Authentication`; it must never accept profile in query/body/header, parse raw JWT claims itself, or cache authority in an instance field. PRIVATE follows its existing `find`/full mapper. Filter admission still rejects missing, invalid, expired, revoked, unknown-session, stale-generation/epoch tokens with 401. No legacy `sub=1` fallback.
4. PUBLIC unselected business methods/paths return 403 before handler invocation/validation/existence lookup. HEAD/OPTIONS and POST/PUT/PATCH/DELETE do not inherit GET permission. Test nested paths, direct credit, unknown segments, suffixes, trailing/normalized/encoded variants and redispatch to the **same selected URI**. Existing firewall/infrastructure 4xx rejections are recorded distinctly; no workaround or newly admitted business handler follows from a rejected path.
5. ASYNC/ERROR: valid propagated PUBLIC context is 403; anonymous/invalid/stale authority is 401; PRIVATE retains its accepted dispatch behavior. Include revalidation with propagated context/no bearer and stale context after session change. Existing DP-A already-admitted-request semantics remain: revocation is enforced at the next admission/redispatch, not promised retroactive byte erasure.

**Deny sweep MUST change by exact membership, not controller exclusions.** Update `registeredRoutesDeniedToPublicTokenFromBothOrigins` to enumerate section 4's eight declared pairs and reconcile them against actual `RequestMappingHandlerMapping` registrations. Assert equality of discovered exception membership with the eight-pair set; no whole-controller skip, concrete-URI-only identity, mock principal or weakened global assertion. Use valid synthetic eligible IDs/catalog codes for positive cases; prove each reaches the expected registered owning controller/method and correct response through the real filter chain using password-login and downgrade-issued persisted signed PUBLIC tokens. Retain ANON 401 and PRIVATE positive/negative controls.

For **every other registered business mapping**, retain 403/ACCESS_DENIED, null data, no content/disposition/redirect leak, per-denial no handler-adapter invocation and no-domain-mutation comparisons. Reset/scope spy observations around each denial so legitimate eight-route calls cannot dilute the no-handler assertion. Preserve `ProfileAuthFixture.domainSnapshot`'s complete row values, not just counts, including populated feature/Vault/Reference fixtures. Existing provider-ON app/probes must still exercise actually registered conditional handlers, verify no handler/provider call, no domain mutation/attempt row/spool output and PRIVATE positive controls. Local test-only flags do not change normal OFF defaults. Do not replace registration proof with static source counts.

## 5. DTO and observable HTTP contract

Use exactly two new PUBLIC HTTP DTO records and matching immutable application views. No inheritance from full DTOs, entity return, map redaction, `JsonView`, global serializer or extra envelope.

| Field | PublicFictionResponse | PublicFilmResponse |
| --- | --- | --- |
| `id` | Long, non-null | Long, non-null |
| `title` | String, non-null | String, non-null |
| `originalTitle` | String, nullable | String, nullable |
| `nationalityCode` | String, nullable | String, nullable |
| `format` | FictionFormat: NOVEL/COMIC | FilmFormat: MOVIE/SERIES |
| `productionStyle` | ABSENT | FilmProductionStyle: LIVE_ACTION/ANIMATION, non-null |
| `isNsfw` | boolean, always false on success | boolean, always false on success |

Assert exact serialized key sets, including allowed nullable keys when null. Every other key is **absent even when its full-DTO value is null**: poster/author/director/People IDs, descriptions/reviews, progress/consumption and totals, genres/classifications, credits, links, tags, nested objects and future inherited fields. Full `FictionResponse`, `FilmResponse` and PRIVATE serialization stay unchanged. Populate forbidden fields with recognizable synthetic sentinels to prove omission.

Canonical success uses `ApiResponse<T>` with exactly `data,error,meta`; success error/meta are null. Example for an eligible synthetic Fiction: `{"data":{"id":41,"title":"Synthetic work","originalTitle":null,"nationalityCode":"VN","format":"NOVEL","isNsfw":false},"error":null,"meta":null}`. Film adds only `productionStyle`, using verified source enum spellings above.

PUBLIC selected eligible details: 200. Missing/NSFW/trashed/missing-Vault/wrong-type: indistinguishable 404 via existing `ApiResponses.of`, per owner:

- Fiction: `FICTION_NOT_FOUND`, `Fiction not found`.
- Film: `FILM_NOT_FOUND`, `Film not found`.

Match status, complete canonical error body (`data/meta=null`, empty fieldErrors), content type and observable headers hidden-versus-absent, without private ID/reason/error metadata. Do not impose timing equality as an acceptance claim. Use the same PUBLIC `Cache-Control: no-store` on success **and each hidden/absent 404**, set at the owning controller response path. Reuse root factories/body; no shared-advice or global cache rewrite. No ETag, Last-Modified, Location, redirects or content-derived private header; any framing length must derive only from the emitted safe body. Test PRIVATE→PUBLIC and PUBLIC→PRIVATE reads of the same URI with conditional request headers; no 304/cached PRIVATE body or cross-profile contamination.

PRIVATE existing `find` currently loads the full feature view without the new Vault/trash/NSFW predicate. Preserve and assert its source-qualified semantics, including existing NSFW/trashed rows; do not silently add PUBLIC restrictions. Reference production remains unchanged: countries/languages `code,nameEn,nameVi`; currencies `code,name,symbol,decimalPlaces`; lists ordered by code ascending. Preserve `COUNTRY_NOT_FOUND`, `LANGUAGE_NOT_FOUND`, `CURRENCY_NOT_FOUND`, current nullable values, validation and PRIVATE behavior. Six unselected Reference list/detail GETs stay 403 for PUBLIC even when populated.

## 6. D10 — mandatory owner-local snapshot mechanism and proof

Each controller must invoke its new `findPublic` **through the injected Spring capability proxy**. Annotate that public service operation with `@Transactional(readOnly=true, propagation=REQUIRES_NEW, isolation=REPEATABLE_READ)`. Do not self-invoke an annotated helper or scope the transaction to a repository method that ends before the Vault read.

Inside that new physical transaction/fresh persistence context:

1. Query only the owning feature's allowed scalars by ID with `isNsfw=false`, preferably a JPQL constructor projection directly into its `Public*View`. No feature entity/full view may be fetched before this authoritative snapshot or used as PUBLIC input. No private classifications/genre/People/Media/Credit/Link enrichment.
2. If no projection, return empty. Otherwise call existing injected `VaultEntryOperations.find(id)` within the same transaction. Its current class-level read-only default REQUIRED joins the feature transaction. Require matching ID, expected type FICTION/FILM, and `deletedAt==null`; otherwise empty. Do not introduce cross-owner SQL, direct Vault internals, reverse dependencies or a new Vault capability.
3. Return only the immutable minimal view; finish the transaction before HTTP mapping. No query/result/second-level/entity cache may bypass eligibility. Null/unknown NSFW classification cannot qualify. Transaction/query failure fails closed through existing safe error handling, never stale/full data.

**Runtime proof is mandatory for both owners**, not inferred from annotation presence. Use existing PostgreSQL Testcontainers with committed synthetic fixtures, fresh request threads/contexts and bounded latches/barriers. SQL fixtures may prepare controlled state; production reads must follow the real services/capability. Test-only spies may pause then delegate to the **real** operations; no mocked query/Vault result may establish the D10 transaction/race proof.

- Observe transaction active/read-only and actual JDBC/PostgreSQL `transaction_isolation=repeatable read`; record transaction/connection identity at owning projection and joined Vault read. An outer READ COMMITTED transaction, with previously loaded state, must be suspended: `findPublic` uses a separate physical transaction/context, Vault joins that new one, and the caller resumes unchanged. No ambient test rollback transaction hiding uncommitted fixture rows.
- Pause after the first feature projection read and before the real Vault read. Commit concurrent NSFW reclassification, trash and restore in separate actors, release the reader and assert the contract in both orderings. A request beginning after a completed NSFW/trash commit denies; a prior consistent eligible snapshot may finish with only minimal fields. Subsequent fresh requests see the committed state. Restored content qualifies only while non-NSFW.
- Force an **atomic committed** transition `(non-NSFW, trashed) → (NSFW, active)` between the two reads. Neither instant qualifies: both the held read and subsequent requests must deny, never combine old non-NSFW with new active. Exercise both Fiction and Film. This is the decisive mixed-snapshot counterexample.
- Prove hidden paths do not invoke full find/mappers or People/Media/link/credit/classification enrichment. Separate transaction/data failures from authority-database failures (the latter retain DP-A 401); no title/private value/token in errors or new logs.
- Respect the existing five-connection test pool: bound actors/futures/timeouts, release all barriers in finally, and clean only owned test resources. No sleeps-only race proof, global pool change or production synchronization hook.

Physical V1 foreign keys prevent committing an orphan feature row. Do not disable/drop constraints or rewrite migrations to manufacture one. Cover ordinary absent IDs plus real PostgreSQL wrong-type/trash states; verify the defensive missing-Vault branch using a narrowly scoped capability fault injection with real session admission and feature persistence, explicitly labelled injected rather than a reachable committed orphan. Keep this defensive test separate from unmocked snapshot/isolation proof. No acceptance case is silently omitted or reported as real orphan persistence.

If the real proxy, propagation, persistence context, cache or isolation assumptions fail, **STOP for bounded correction within this map**. If correction requires an unapproved file/schema/transaction-manager/dependency/architecture change, return the minimal exact exception to Owner. Never fall back to weaker isolation or weaken D10 to make tests green.

## 7. OpenAPI implementation contract

Keep `getFiction`, `getFilm`, all six Reference operation IDs and bearer security. Root security text must distinguish anonymous Auth/infrastructure from authenticated PUBLIC and describe exactly the eight B1 grants plus PRIVATE-only fallback. The six Reference methods remain protected; **do not** add them to `PUBLIC_AUTH_PATHS` (that set means anonymous, not PROFILE_PUBLIC).

At each existing detail GET document PUBLIC minimal versus PRIVATE full response, 401/403/404 and no-store. The generated 200 schema must accept **two complete canonical envelopes**, each with the correct `data` schema; a bare DTO union at response root is wrong. Use the existing `OpenApiConfiguration` customizer narrowly for these two GETs plus owning annotations; no new Java envelope DTO or global polymorphic framework. A sufficient approach is two operation-specific composed schemas referencing a PUBLIC envelope and existing PRIVATE envelope. Use `anyOf` for potentially overlapping schemas, or prove disjointness if selecting `oneOf`; optional fields in the existing full schema can otherwise make `oneOf` reject actual PUBLIC responses. The validated server profile selects the branch; no new wire discriminator/profile field is authorized.

Register/resolve PUBLIC schemas within the permitted owner DTO/controller and existing root OpenAPI files. Root configuration uses schema metadata/references and must not import module `internal` DTOs or add business logic. PUBLIC data schema must have exactly the six/seven properties, required emitted keys, correct types/enums/nullability, false-only isNsfw success semantics and `additionalProperties=false`. Do not tighten/redefine existing full PRIVATE schema globally. Retain the canonical envelope's data/error/meta shape; profile conditions belong in descriptions/examples, not a broadened PUBLIC field set.

Fetch actual generated `/v3/api-docs` through MockMvc and resolve referenced schema graph, rather than asserting only an annotation or guessed schema name. Verify both complete serialized PUBLIC/PRIVATE specimens against their represented branch shape/types/nulls and union, and show extra PUBLIC properties fail its strict branch. Use narrow assertions/helpers in the affected contract test with existing dependencies; no new generic validator framework/dependency. Update only the **two** detail-operation inventory expectations needed for the union; preserve every other method/path/ID, response/security assertion and actual registration inventory. New response variants add no endpoint; do not inflate route counts.

## 8. B1-01–B1-24 executable acceptance map

All rows are MUST and **NOT_RUN** at creation. Test names below denote required scenarios to implement in the named allowed classes; they are not claims that tests already exist. Both real PUBLIC origins are required where indicated. Every applicable mandatory NOT_VERIFIED case blocks acceptance.

| ID | Scenario / observable result | Minimal owning proof / gate; reusable path |
| --- | --- | --- |
| B1-01 | Password-login and downgrade persisted PUBLIC sessions reach both correct detail handlers | New PublicFiction/PublicFilmRead tests plus exact registered sweep; SEC; real token helpers in AbstractWebIntegrationTest. |
| B1-02 | Eligible Fiction 200 has exactly six keys, nullable permitted fields, no forbidden sentinels | PublicFictionRead / FictionWeb; CONTRACT; owner mapper and ApiResponse. |
| B1-03 | Eligible Film 200 has exactly seven keys and no forbidden sentinels | PublicFilmRead / FilmWeb; CONTRACT. |
| B1-04 | Each NSFW detail is indistinguishable from absent 404 | Both new owner tests, real PG + HTTP full body/headers; DB/CONTRACT. |
| B1-05 | Trash/missing/wrong-type Vault denies; restore qualifies iff non-NSFW | Both new tests; DB; section 6 explicitly distinguishes defensive missing-Vault injection from physical FK proof. |
| B1-06 | PRIVATE full schemas/behavior retained for qualifying, NSFW, trashed and other existing rows | Existing owner web/integration tests + paired PUBLIC/PRIVATE specimen checks; DB/CONTRACT/REG; existing find/full mapper unchanged. |
| B1-07 | Feature/genre/classification/link/credit writes deny PUBLIC before body validation/effects | Registered sweep with invalid bodies/real IDs, no handler and complete row snapshot; SEC. |
| B1-08 | All nested Fiction reads deny with no parent oracle | Sweep + explicit real/absent IDs in FictionWeb or new owner test; SEC. |
| B1-09 | All nested Film reads deny | Corresponding Film cases, including genres/credits/links; SEC. |
| B1-10 | Direct Film Credit real/absent ID denies with safe/non-NSFW parent | FilmWeb/new Film test, real signed tokens; SEC. |
| B1-11 | Genres/Search/Vault/People/Media/Personal/export/all remaining mappings deny without data/count/bytes | Whole registered sweep and retained provider-ON actual handlers; SEC; domainSnapshot. |
| B1-12 | Eight routes ANON/invalid/expired/revoked/stale generation/epoch/unknown session are 401 | Security/ProfileRoute and new owner tests, real signed persisted controls; SEC; reuse accepted DP-A admission algorithm. |
| B1-13 | Six selected Reference GETs preserve DTO/order/code success/missing 404 for both profiles | ReferenceWeb + ReferenceModule; REF; existing catalog and mapper unchanged. |
| B1-14 | Six unselected Reference GETs deny even with populated data | ReferenceWeb + registered sweep; REF/SEC. |
| B1-15 | Hidden read never invokes full view or forbidden enrichment, serialized forbidden data absent | New owner tests with real PG/security and delegate spies; DB/CONTRACT; no mocked admission. |
| B1-16 | Forced before/after NSFW commits obey snapshot boundary and fresh-request denial | Both new owner tests; DB; actual proxy/isolation/transaction IDs, barriers. |
| B1-17 | Forced trash/restore commits never cause split-state grant | Both new tests, including atomic never-eligible transition; DB. |
| B1-18 | Concurrent downgrade/refresh/revocation follows existing admission and redispatch rules | ProfileRoute/new owner tests, controlled session mutation order; SEC; no cached authority. |
| B1-19 | PUBLIC success and every hidden/absent 404 use no-store, same safe errors and no PRIVATE cache/header bleed | Real MockMvc bodies/headers/conditional reads, both owners; CONTRACT. |
| B1-20 | Exact methods/segments/path variants and same-URI ASYNC/ERROR cannot bypass denial | ProfileRoute/Security + new owner tests; SEC; preserve accepted firewall/infrastructure distinctions. |
| B1-21 | Generated docs contain both complete envelopes, exact PUBLIC schema, same IDs/bearer and accurate Reference/errors | OpenApiRouteInventory plus actual HTTP specimens; CONTRACT; section 7. |
| B1-22 | Legal ownership/no reverse/internal/repo/SQL/dependency/DDL delta; new tx and joined Vault demonstrated | Unchanged ApplicationArchitectureTests + scoped source/diff inventory + both new PG tests; CONTRACT/DB. |
| B1-23 | Existing Fiction/Film/Reference domain/web and applicable Authentication tests remain valid | Exact selectors below; REG, source-qualified reuse of SEC/REF receipts. |
| B1-24 | File map, source hashes/receipts/results/limits and Docs Sync truthful; independent review still separate | Implementer handback/self-review, full five-gate manifest, final diff/index check; REG. |

## 9. Finite verification, evidence and handback

Use existing JUnit/AssertJ, MockMvc with real Spring security, Testcontainers PostgreSQL and Modulith. Relevant existing guidance: `java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`, `modular-monolith-architecture`, `jpa-postgresql-persistence`, `backend-testing`, `fiction-domain-modeling`, `film-domain-modeling`, `authentication-security`, `rest-api-http-contracts`, `bounded-command-execution`; `design-pattern-selection` only if a concrete design choice requires it. No Python/RAG agent or new rule/skill/hook/dependency. Read root/scoped AGENTS before source edits.

Five logical groups, with exact class selectors (test filenames from section 3, without `.java`). These are a finite verification plan, not a numeric quota:

| Gate | Exact selectors / result required |
| --- | --- |
| G-B1-SEC | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` plus route/session/negative cases in `PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest`; registered route membership, both origins, provider-ON and dispatcher proof. |
| G-B1-DB | `PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest`; both unmocked PostgreSQL transaction/race proofs and eligibility cases. |
| G-B1-REF | `ReferenceWebIntegrationTest,ReferenceModuleIntegrationTest`; all six allowed/six denied, retained DTO/order/errors. |
| G-B1-CONTRACT | `OpenApiRouteInventoryIntegrationTest,ApplicationArchitectureTests` plus serialized/error/cache cases in both new owner tests; existing architecture class unchanged. |
| G-B1-REG | `FictionWebIntegrationTest,FictionIntegrationTest,FilmWebIntegrationTest,FilmIntegrationTest` plus same-final-source SEC/REF evidence for existing Authentication/Reference regression. |

The same final-source class run can satisfy several groups; record case membership and do not sum overlapping runs as distinct tests. Run all listed applicable existing classes, not only new tests. Partition groups by individual class or explicit existing/new method selectors when necessary to fit 300s; do not widen the timeout, omit the provider-ON case or mark a partially finished class green. No full application suite or 2,100-second profile is granted by B1; broader regression needs demonstrated changed-source risk/appropriate authority, not a quota.

From repository-root PowerShell, for each concrete selector batch, use the accepted watchdog; the following is an implementation instruction, **not executed during handoff creation**:

```powershell
$b1RunDir = Join-Path 'logs/bf26-dp-b1' ('initial-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $b1RunDir -ErrorAction Stop | Out-Null
$b1Selectors = 'PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest'
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $b1RunDir 'g-b1-db.json') -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' "-Dtest=$b1Selectors" test
```

Choose a **fresh** receipt filename/run directory for every other batch/retry; retain failed/timeout stdout/stderr/receipts. Never overwrite prior logs, disable hooks or kill unrelated Java/Docker/processes. Use ordinary runner cap 300s, owned-tree cleanup and factual failure categories. The accepted Maintenance B1/B2 watchdog/native Git enforcement remains in force within its supported client/startup limits; no universal containment claim. Do not run suspended QA/model inference/runtime downloads or provider/live activation. Test-only provider-ON contexts are limited to the retained controlled DP-A proof and must clean up owned resources.

Handback MUST include:

- Exact changed/new files and their authorized purpose; final baseline/HEAD/index/worktree; source/config/test hashes identifying evidence applicability, including relevant unchanged admission, Vault/Reference and transport inputs. No speculative PASS or invented totals.
- B1-01–24 → named test methods → five gate memberships → command selector/receipt/XML paths. Record starts/durations/exits, executed/skipped/failing cases, effective PG isolation/propagation/controlled ordering evidence, registration/handler membership and actual `/v3/api-docs`/HTTP specimen checks.
- Preserve initial failures/timeouts separately from reruns; reconcile JUnit XML and command results, reused versus fresh evidence, and outstanding NOT_VERIFIED/infrastructure limits. Passing a mapper/unit mock alone cannot close an HTTP/session/snapshot obligation.
- Mandatory Antigravity self-review against actual diff and Owner contract using `/antigravity-self-review`; report correctness, security, ownership, reuse, schema/PRIVATE compatibility and Docs Sync. Save the two named handback/self-review artifacts and API addendum; then set current implementation result/ACTIVE to IMPLEMENTED_AWAITING_CODEX_REVIEW and STOP for separate Codex review.

Definition of done for delivery: all B1-01–24 evidenced, five finite groups pass on qualifying final inputs, no unauthorized changes or mandatory NOT_VERIFIED case, self-review complete and current docs/evidence coherent. This means ready for **independent review**, not accepted/published. Independent review alone can return READY_FOR_OWNER_COMMIT. After three independently blocked correction/re-review rounds, stop Antigravity; no fourth round or automatic takeover. LOW/INFO backlog does not create extra remediation. Owner publication and a later brief actual-SHA closeout do not start B2/C.

## 10. Documentation impact, ownership and Docs Sync Check

| Affected current/derived artifact | Responsible editor / observable evidence |
| --- | --- |
| This executable and ACTIVE | Codex creates the contract/register; Antigravity updates only implementation result/status/counter/evidence fields at handback; independent Codex records later review disposition. Keep creation authority and prior history intact. |
| New B1 API addendum, implementation handback and self-review named in section 3 | Antigravity, expressly authorized implementation-adjacent documents. Describe actual delivered eight-route/profile/schema/cache behavior and link concrete HTTP/OpenAPI/PG receipts; do not claim publication. |
| New dated independent review | Separate Codex; adjudicate the submitted proof, mandatory gaps and acceptance, with current source-qualified Docs Sync. |
| Root AGENTS/Owner policy, B1 preparation README, roadmap/maintenance navigation and post-publication closeout | ChatGPT/Owner routine governance synchronization when requested. Their no-B1-authority checkpoint wording is superseded for execution by this new Owner-authorized handoff and ACTIVE. Not a production/source or historical-review edit permission. |

**Creation Docs Sync Check — 2026-11-12:** UPDATED this executable and ACTIVE only, with exact B1 scope/current next action, published preparation SHA and separate counters. ALREADY_CONSISTENT: independent preparation readiness/D1–D11, actual source/module/HTTP ownership and accepted DP-A publication; no live handoff conflict under the controlling DP-A publication notice. Older READY_FOR_OWNER_COMMIT/NO_ACTIVE_HANDOFF notices describe their own checkpoints and are preserved as history below ACTIVE's new register. The root policy/current notice and preparation README are inspected but not rewritten by handoff creation; exact B1 authority is recorded here. DEFERRED_OUT_OF_SCOPE: routine governance/navigation updates above, historical notice stacks, `docs/AGENTS.md` old S19-1 routing, backend-implementer's old watchdog-pending bullet and package-tree illustrative Fiction sketch; ChatGPT owns separately requested cleanup. No material unresolved D7–D11 decision is identified. NOT_VERIFIED: every B1 runtime/security/PG/OpenAPI/build result, live data/deployment/cutover and future implementation acceptance/publication. Historical reviews/receipts/failures and all separate counters are preserved.

**Creation integrity evidence:** tracked `git diff --check` exits 0. The new file's `git diff --no-index --check -- NUL <handoff>` exits 1 for the added-file difference, with no whitespace diagnostics; this is not reported as a zero-exit test. All 23 local Markdown links in the new executable/current ACTIVE register resolve. The acceptance table contains 24 unique rows, B1-01–24. The allowlist resolves to 16 production files (12 existing, four planned new) and 13 test files (11 existing, two planned new). The historical ACTIVE suffix matches HEAD after line-ending normalization; no historical text changed. Final local HEAD/origin/live remote remain the approved SHA, index is empty, and the only delivery changes are this new executable and the ACTIVE prepend. These are document-integrity checks, not runtime acceptance.

Before handback and independent review, repeat only the affected Docs Sync/link/file-map/source/evidence checks. Record remaining exact drift and editor; no repository-wide cleanup. The final saved-file integrity check belongs in this section or the implementation handback, not a new report per command.

## 11. Stop conditions and result fields

STOP rather than improvise if: baseline materially differs; another live handoff conflicts; PUBLIC fields/routes expand; Reference source needs editing; new Vault API/cross-owner SQL/module edge/DDL/index/global cache or transaction-manager change becomes necessary; the eight-route deny sweep/provider-ON or real D10/OpenAPI evidence cannot be completed; PRIVATE behavior must change; or a material contract choice exceeds authorization. Report concrete evidence and the minimum proposed exception/remaining test. An infrastructure blocker is not a confirmed product defect or a test PASS; preserve both kinds accurately.

### Implementation result — Antigravity fills at delivery

Remediation Round 3 (3/3 consumed): IMPLEMENTED_AWAITING_CODEX_REVIEW. All 24 criteria B1-01–24 VERIFIED. All 5 verification gates PASSED cleanly (G-B1-DB fresh 26 tests, G-B1-SEC 27 tests, G-B1-REF 9 tests, G-B1-CONTRACT 39 tests, G-B1-REG 26 tests; total 127 tests executed, 0 failures, 0 errors, 0 skips; receipts in `logs/bf26-dp-b1/remediation-round3/`). FR-DPB1-01 residual resolved (index 0 advice on repository proxies + positive invocation control demonstrating `findById`, `findStoryArchetypeIdsByFictionId`, `findWorldSettingIdsByFictionId` in Fiction, and `findById`, `findGenreIdsByFilmId`, `findStoryArchetypeIdsByFilmId`, `findWorldSettingIdsByFilmId` in Film increment the counter, followed by reset and clean 0 counts on public success, hidden, and failure paths). FR-DPB1-02/03/04 retained CLOSED. Remediation 3/3 consumed; takeover NOT_AUTHORIZED. Submitted for independent Codex re-review 3.

### Codex remediation — independent reviewer only

[Independent final re-review 3](reviews/2026-11-12-bf26-dp-b1-independent-final-re-review-3.md) CLOSED **FR-DPB1-01** after verifying index-0 observers, positive invocation counts and retained negative controls on fresh final-source tests. FR-DPB1-02/03/04 remain CLOSED. No blocking finding or mandatory acceptance NOT_VERIFIED remains. DPB1-L01 CLOSED; DPB1-L02 is LOW/OPEN/BACKLOG author-summary wording only. Ordinary remediation is stopped after accepted **3/3 consumed**; takeover NOT_AUTHORIZED and not needed. Preserve all original acceptance, source/field/route limits, historical reviews and other counters; no follow-on slice or new implementation authority follows.

### Final review — separate independent Codex only

**READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED**, independent final re-review **3**, 2026-11-12. FR-DPB1-01–04 CLOSED; DPB1-L01 CLOSED, DPB1-L02 LOW/OPEN/BACKLOG. Fresh reviewer owner tests 26/26 PASS, zero failures/errors/skips, 55.125s with complete bounded capture/cleanup. Final evidence covers 127 disjoint cases: fresh R3 26 plus 101 source-qualified reused cases, with precise producer-versus-independent-R2 XML provenance resolved in the linked review. All 1,916 input hashes and all 52 method references validate; no blocking finding or mandatory acceptance NOT_VERIFIED remains. Evidence: `logs/bf26-dp-b1/codex-rereview3-6d1f23dc64db42639bd0891ab12dd832/`. Earlier reports/receipts and author submissions remain preserved; the independent review controls current acceptance. Implementation remains unpublished; no deployment/cutover or next-slice claim.

**Independent re-review 1 Docs Sync Check:** UPDATED new re-review and ACTIVE/executable disposition only; the residuals identify exact authorized implementation-adjacent test/evidence/doc corrections. Corrected API text/manifests are credited. Routine root/preparation/navigation and historical cleanup remain deferred to ChatGPT/Owner. No production/test edit by reviewer; prior raw receipts/reviews and all separate counters retained.

**Independent re-review 2 Docs Sync Check:** UPDATED new re-review and current ACTIVE/executable disposition only. Retain FR-02/03/04 closures and accepted FR-01 proof; remaining two-test observer/evidence correction stays within the exact map. Original history, raw receipts/manifests and all separate counters retained; routine root/preparation/navigation remains deferred to ChatGPT/Owner. No production/test edit by reviewer.

**Independent re-review 3 Docs Sync Check:** UPDATED independent review and ACTIVE/executable disposition only. Retain all FR closures and final evidence/source qualification, preserve raw history/counters, and keep author-summary LOW wording separate from acceptance. Routine root/preparation/navigation and brief actual-SHA closeout remain ChatGPT/Owner work after actual publication. No production/test edit or staging/publishing by reviewer.

**Next step:** Owner → ChatGPT | normal git add/commit/push of the accepted B1 delivery, then brief actual-SHA slice closeout/current-navigation sync | BF26-DP-B1 and independent final re-review 3 | READY_FOR_OWNER_COMMIT / independently accepted, unpublished; no extra staged-tree review or new handoff | remediation 3/3 consumed and stopped, takeover NOT_AUTHORIZED; historical counters unchanged; no B2/C authorization.
