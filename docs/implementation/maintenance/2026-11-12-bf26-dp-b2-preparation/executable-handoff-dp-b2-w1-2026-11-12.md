# Executable slice handoff — BF26-DP-B2-W1: bounded Public Film creation

- **Handoff ID:** `BF26-DP-B2-W1`.
- **Created by / date:** Codex / 2026-11-12.
- **Status: READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED.** [Independent re-review 2](reviews/2026-11-13-bf26-dp-b2-w1-independent-final-re-review-2.md) CLOSED FR-W1-01/03/05 and retained FR-W1-02/04 closures. No blocking finding or mandatory acceptance NOT_VERIFIED remains; DPB2W1-L01 LOW/OPEN/BACKLOG is nonblocking.
- **Implementation:** round 2 independently accepted; producer 199 distinct passing cases, fresh reviewer CREATE/API 31/31 PASS and independent media/handler-boundary diagnostic qualified. Owner publication and actual-SHA closeout pending; no deployment or next-slice authority.
- **Implementer:** Antigravity, existing [backend-implementer](../../../../.agents/agents/backend-implementer/agent.md) role; `/antigravity-implement-handoff`, then mandatory `/antigravity-self-review`.
- **Final reviewer:** separate independent Codex session using `$codex-final-review`; implementer checks are not independent acceptance.
- **Entry / published preparation commit:** [`e8f4dde0d696d8dc8e9cd51d6658aeb9634ba6b0`](https://github.com/vhvkhangg/personal-private-vault/commit/e8f4dde0d696d8dc8e9cd51d6658aeb9634ba6b0).
- **Counters:** W1 remediation **2/3 consumed**, ordinary remediation stopped after accepted round 2; takeover **NOT_AUTHORIZED / NOT_USED**. No counter reset, extra round or future-slice authorization.
- **Delivery:** only Owner stages/commits/pushes; no agent publishing. W1 is independently accepted but unpublished; no live deployment/cutover is claimed.

## 1. Authority, entry checks and sources of truth

**Current Owner authorization, recorded from the 2026-11-12 request:** `OWNER_AUTHORIZED_FOR_W1_HANDOFF_AND_BOUNDED_IMPLEMENTATION`, final decision **APPROVED — BF26-DP-B2-W1 ONLY**. The Owner explicitly permits the narrowly named frozen Backend Phase 2/5/13 source, HTTP/API/DTO/Security/OpenAPI/test exceptions below, conditional on this ready executable handoff. The current request is separate from D12–D17's earlier preparation-only approval; no new product choice remains open. This record does not purport to be a new independent review.

**Verified at creation:** local branch `main`, HEAD, `origin/main` and live `git ls-remote origin refs/heads/main` all equal the full entry SHA above. Entry worktree/index were clean. All **15 preparation Git blob IDs** match current source. Graphify executable is available but `graphify-out/graph.json` is absent; source was inspected directly. Section 10 records **27 actual file-byte SHA-256** inputs, not runtime results.

Read the following without reopening accepted predecessor decisions:

- [Independent W1 pre-handoff review](reviews/2026-11-12-bf26-dp-b2-w1-independent-pre-handoff-review.md): **READY_FOR_HANDOFF**, no blocking preparation findings; its eight carry-forward points are mandatory below.
- [D12–D17 Option A ratification](owner-ratification-d12-d17-2026-11-12.md), [decision docket](owner-proposed-d12-d17.md), [original W1-01–W1-16](slice-boundaries-and-review-plan.md), [source/risk inventory](source-baseline-and-risk-inventory.md). Their earlier pending publication/authorization notices describe their creation checkpoint; this later handoff records verified publication and the new exact Owner authority.
- [D1–D6 ratification](../2026-11-11-dual-login-profiles-preparation/owner-ratification-d1-d6-2026-11-11.md) and [D7–D11 ratification](../2026-11-12-bf26-dp-b1-preparation/owner-ratification-d7-d11-2026-11-12.md); accepted [DP-A closeout](../2026-11-11-dual-login-profiles-preparation/owner-publication-closeout-dp-a-2026-11-12.md) and [B1 closeout](../2026-11-12-bf26-dp-b1-preparation/owner-publication-closeout-dp-b1-2026-11-12.md).
- [Owner delivery policy](../../../workflow/current-owner-delivery-policy.md), [Docs Sync](../../../workflow/documentation-synchronization.md), [handoff template](../../../templates/handoff.md), [ACTIVE](../../handoffs/ACTIVE.md).
- Root/backend/test/Film/Auth scoped `AGENTS.md`; [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [module dependency matrix](../../../architecture/module-dependency-matrix.md), [frozen DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml), [package tree](../../../repository/repository-package-tree.md). No architecture, module edge or schema change is selected.

Before implementation, recheck HEAD, diff/index and named input hashes. Reconcile intervening docs-only publication by exact SHA and source equality; stop on material source/authority drift or a conflicting active handoff. Do not reset counters or treat historical current-looking notices as new authority.

**Inherited state:** DP-A independently accepted / Owner-published / CLOSED at `5c1da4e5404ee3c724e3fc2e03569d4093bcb44d`, remediation 3/3 exhausted, exceptional takeover 1/1 accepted, DPA-L01 LOW/OPEN/BACKLOG. B1 independently accepted / Owner-published / CLOSED at `277522ee5b89ce5d2d8200cb955654b031b9f97a`, remediation 3/3 consumed/STOPPED, takeover NOT_AUTHORIZED/NOT_USED, DPB1-L02 LOW/OPEN/BACKLOG. B1's 127 source-qualified cases are **26 fresh R3 + 101 reused**, not W1 results. H1-F remains paused/not independently accepted; FR-13 upstream-blocked. P17 FROZEN, P18 CLOSED, S19-2 SUSPENDED, indexing inactive, live originals disabled/deferred, provider flags OFF/unset.

## 2. Goal, reuse and simplicity decision

Add exactly one authenticated business operation for persisted PROFILE_PUBLIC: **POST /api/v1/films**, creating a new non-NSFW Film with no relationships or private inputs. PROFILE_PRIVATE keeps its full existing creation behavior. Retain all eight B1 GET permissions.

| Need / mechanism | Decision and rationale |
| --- | --- |
| One authenticated Film creation | KEEP exact route; REUSE accepted DP-A admission and Film/Vault ownership |
| Five-key request | KEEP dedicated Film DTO and one local profile-aware reader; no global mapper change |
| Command, normalization, validation, defaults, persistence | REUSE `FilmOperations.create(CreateFilmCommand)` / proxied `FilmService.create`; no second persistence path |
| Safe seven-key receipt | REUSE `PublicFilmResponse`; explicitly project the returned immutable creation `FilmView` after the service proxy commits |
| New application capability | OMIT by default: existing create returns everything required, transaction joins Vault creation. No demonstrated boundary need exists at entry |
| Error/cache handling | REUSE canonical envelopes/codes and existing handlers/advice where applicable; scope any controller-local final header handling to PUBLIC POST |
| Other writes, Search, Vault, infrastructure | DEFER; no new strategy hierarchy, generic parser framework, transaction manager, benchmark, agent or dependency |

**Planning estimate, not delivery evidence:** a small HTTP/security/schema delta plus substantial focused integration assertions; six finite verification groups below. This is one vertical slice. Runtime proof remains mandatory; no full application suite is imposed by a review label.

## 3. Precise authorized change map

All `J/` paths mean `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` means the corresponding `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

| Allowed production file | Exact responsibility / limit |
| --- | --- |
| `J/authentication/internal/infrastructure/security/SecurityConfiguration.java` | Add only exact POST /api/v1/films with `hasAnyAuthority("PROFILE_PUBLIC", "PROFILE_PRIVATE")` **after** ASYNC/ERROR PRIVATE guard; preserve eight B1 GET grants, Auth/provider exceptions and PRIVATE fallback |
| `J/film/internal/web/controller/FilmController.java` | Change only create method/wiring, profile-aware body interpretation, response selection, PUBLIC create errors/headers and createFilm annotations. Preserve GET and all PRIVATE/nested handlers |
| `J/film/internal/web/dto/PublicCreateFilmRequest.java` **NEW** | Exactly title/originalTitle/nationalityCode/format/productionStyle; same relevant Bean Validation bounds/required constraints as existing create DTO |
| `J/film/internal/web/controller/FilmCreateRequestReader.java` **NEW, Film-local helper** | Read unconsumed request body after authenticated profile selection; PUBLIC strict syntax/key/type checks then DTO binding/validation; PRIVATE delegate to the existing configured MVC JSON conversion and Bean Validation behavior. No domain rules, persistence, Auth-internal imports, global converter registration or unrelated parser API |
| `J/film/internal/web/mapper/FilmWebMapper.java` | Add explicit `toCommand(PublicCreateFilmRequest)` and a distinctly named `toPublicCreateResponse(FilmView)` (or equivalently explicit mapping). Keep existing overloads and B1 mapping unchanged |
| `J/OpenApiConfiguration.java` | Minimum exact POST request/201 variants and Bearer-description delta in existing customizer; preserve root transport and B1 schema graph |
| `J/film/film/FilmOperations.java`, `J/film/internal/application/FilmService.java` | Conditional Owner-permitted targets only if an actual safe-command/transaction/projection need is demonstrated. Default is **no edit**. Retain existing create validation/persistence and PRIVATE semantics. If needed, stop and return the source-qualified reason/exact proposed method delta to Codex for a bounded executable design addendum before editing; Antigravity cannot self-expand the change map. A broader unlisted exception requires Owner disposition |

The named reader is the sole selected new parsing helper; tests must exercise it through real HTTP, including profile selection before parsing and PRIVATE conversion parity. Keep it in the existing controller package (existing package descriptor); do not add a global parser or a new package hierarchy. Controller-private methods for finalizing PUBLIC POST headers/errors are allowed; **no new filter/advice/global exception handler is selected or authorized**. Existing Film/root advice may be reused without editing their production files.

**Unchanged production contracts:** `CreateFilmRequest`, `FilmResponse`, `PublicFilmResponse`, `CreateFilmCommand`, entities/repositories, Vault APIs/implementation and session authority/JWT/refresh/switch/revocation code. Film application authorization does not extend to repositories or entities. No production edits in Reference, People, Fiction, Search, Film Credit/link/genre services, Feed, Drive or other modules.

**Allowed tests:** `T/authentication/ProfileRouteAuthorizationIntegrationTest.java`, `T/authentication/SecurityFilterChainIntegrationTest.java`, `T/film/FilmWebIntegrationTest.java`, `T/film/FilmIntegrationTest.java`, `T/web/OpenApiRouteInventoryIntegrationTest.java`; new **`T/film/PublicFilmCreateIntegrationTest.java`** owns W1 HTTP/DB/admission cases. `T/authentication/fixture/ProfileAuthFixture.java` may change only if directly necessary for W1 fixture/snapshot reuse; prefer no edit. Existing architecture/Modulith and Film regression classes in section 8 may receive only directly affected assertions, never weakened contracts. No modification to shared `AbstractWebIntegrationTest` or predecessor session concurrency tests is selected. Put test-only barriers/fault injection inside the new Film test class or scoped configurations nested there.

## 4. Profile selection, strict body contract and expected errors

Security must admit and validate the persisted session before the handler. Controller selects from **server-authenticated authorities**, never a JSON/query/header profile, JWT claim alone or client-supplied visibility. Do not turn unknown/missing authority into PRIVATE by default. No broader authority policy is introduced.

**Selected parsing path:** take the original servlet request/stream (or lossless bytes) into the profile-aware reader; do not first bind a `JsonNode`, `Map`, broad `CreateFilmRequest`, or any other representation that discards duplicate/unknown properties. PUBLIC gets a Film-local strict reader using the installed Jackson version; enable duplicate detection and trailing-token rejection before tree/DTO construction, enforce a single JSON object and exact property names, and reject scalar coercions/numeric enum ordinals. PRIVATE must still use the configured MVC JSON converter/mapper and full `CreateFilmRequest` Bean Validation, including its current optional relationships, unknown-key/coercion/duplicate behavior and media/charset negotiation. Do not route PRIVATE through the strict reader or five-key DTO. No mutation of a shared ObjectMapper; any local copy/settings are PUBLIC-only. Auth's strict-parser pattern may be read, never imported from Auth internals.

PUBLIC accepts these **five case-sensitive names only**:

| Input | Required behavior and status |
| --- | --- |
| `title` | Required JSON string; missing, null, empty or blank → **400 VALIDATION_ERROR**; raw length >500 → same 400. Exactly 500 accepted if otherwise valid; existing service trims the accepted value |
| `originalTitle` | Optional string/null; omitted/null/empty/blank → stored/returned null. Nonblank text trimmed by existing service. Raw length 500 accepted, 501 rejected **400 VALIDATION_ERROR**, including padding; do not move length validation after normalization |
| `nationalityCode` | Optional string/null, raw max 2. Omitted/null/empty/blank → null. Existing trim + Locale.ROOT uppercase/catalog validation retained: fixture `"vn"` → `"VN"`; `"ZZ"` absent from fixture catalog, or nonblank `"V"` → **422 FILM_INVALID**. Raw length 3, e.g. `" VN"`, → **400 VALIDATION_ERROR**, even if trimming would yield a valid code |
| `format` | Required string `MOVIE` or `SERIES`. Missing/null → **400 VALIDATION_ERROR**; unknown/lowercase/blank enum, numbers (including valid ordinals), bool/array/object → **400 MALFORMED_REQUEST** |
| `productionStyle` | Required string `LIVE_ACTION` or `ANIMATION`; same missing/null, enum and type outcomes as format |
| Any string field supplied as number/bool/array/object | **400 MALFORMED_REQUEST**, no coercion, before domain mutation |
| Any property outside the five decoded names | **400 MALFORMED_REQUEST**, including `isNsfw` true/false/null, directorPersonId, genreIds, storyArchetypeIds, worldSettingIds, posterUrl, description, review, totalEpisodes, progressStatus, consumptionStatus, currentProgressText, links/credits, profile and unknown/differently cased keys. Unicode escaping cannot hide a forbidden decoded name; an escaped allowed name is not itself forbidden, but duplicate decoded names still fail |
| Duplicate property | **400 MALFORMED_REQUEST** even when values agree, are null, or use escaped equivalent names; cover duplicate allowed and forbidden keys. No last-value-wins |
| Missing/empty body, JSON null/array/scalar root, malformed/truncated JSON, concatenated objects or trailing nonwhitespace tokens | **400 MALFORMED_REQUEST**. Trailing whitespace is allowed; enforce syntax over original bytes before DTO interpretation |
| Unsupported media / unacceptable representation | Preserve existing canonical **415 / 406** behavior where applicable; no new consumes restriction that changes PRIVATE routing |
| Existing domain-validation failure | Existing safe **422 FILM_INVALID** / "Invalid film data", no rejected value or reference details echoed |
| Persistence/commit failure | Safe existing **500 INTERNAL_ERROR** for injected ordinary DB faults; preserve applicable existing classified errors rather than inventing new mappings. No 201/data receipt and no committed partial rows |

Reuse Bean Validation for request constraints and existing Film service normalization/country/domain rules; **do not duplicate domain validation** in the reader/controller. Freeze these expectations before implementation. Test each required field missing/null separately, blank/empty strings, exact bounds and boundary+1, optional omission/null, both enum alternatives and type negatives. Maintain legacy PRIVATE outcomes through the same configured conversion and validation path; a PRIVATE input accepted at baseline cannot silently become strict PUBLIC input.

Minimal PUBLIC specimen (valid for both token origins):

```json
{"title":"New film","format":"MOVIE","productionStyle":"LIVE_ACTION"}
```

All-five specimen: `{"title":"  New film  ","originalTitle":"  Original  ","nationalityCode":"vn","format":"SERIES","productionStyle":"ANIMATION"}` → normalized title/originalTitle/country. Optional nulls must still produce seven response data properties.

## 5. Server-controlled command, commit boundary and response privacy

Explicit PUBLIC mapping to the existing `CreateFilmCommand`:

- Copy only the five approved validated DTO fields.
- Set **`isNsfw = false`**, **`directorPersonId = null`** server-side.
- `posterUrl,description,totalEpisodes,progressStatus,consumptionStatus,currentProgressText,review = null`; `genreIds,storyArchetypeIds,worldSettingIds = Set.of()`.
- Reuse accepted internal defaults **ONGOING / UNCONSUMED** in `FilmService.validateFilm`. No People/Creator Group lookup, reference selection other than country validation, genre/classification/link/credit assignment or private/external resource creation.

**Commit sequence:** nontransactional HTTP adapter → proxied existing `FilmOperations.create` → existing `@Transactional FilmService.create` → existing REQUIRED `VaultEntryOperations.create(FILM)` plus Film save → transaction flush/commit succeeds → immutable returned `FilmView` → explicit seven-field PUBLIC mapping → canonical `ApiResponses.created` → HTTP **201**. Do not add a controller transaction or invoke create via self-invocation that bypasses the proxy. Service-body return is not itself commit evidence. Use the existing transaction manager, one database transaction and legal named APIs.

Do **not** call B1 `findPublic()` inside the uncommitted create transaction: its REQUIRES_NEW / REPEATABLE_READ cannot see the new row. No follow-up read is needed for the POST receipt; project the owned creation result. B1 GET remains an independently qualified later operation; a subsequent concurrent PRIVATE edit/trash may hide it without changing the committed creation receipt.

Success uses existing `ApiResponse` with **exactly** these data keys, including nullable fields:

```json
{"data":{"id":123,"title":"New film","originalTitle":null,"nationalityCode":null,"format":"MOVIE","productionStyle":"LIVE_ACTION","isNsfw":false},"error":null,"meta":null}
```

`id` is the newly committed identity, not a hardcoded fixture. No full FilmResponse serialization or hidden extra keys even as null; exact seven-key property comparison is required.

PUBLIC success and applicable create-handler errors require **Cache-Control: no-store**; no Location/redirect, ETag, Last-Modified, private-derived validators, rejected values, relationship IDs, SQL/stack details or tokens in error metadata/logs. Keep PRIVATE headers/response behavior unchanged. Reuse the existing no-Location overload `ApiResponses.created(data)`.

Handle PUBLIC parse/validation/domain/persistence failures within the selected controller branch or reuse the existing root/Film error methods, then finalize headers on the **final** response. Root `ApiExceptionHandler.abortIfCommitted` calls `response.reset()`: merely setting no-store before delegating is insufficient. Reapply no-store after error translation; do not alter global advice or impose PUBLIC headers on PRIVATE/other handlers. Keep canonical malformed 400, field-validation 400, Film 422 and unexpected 500 envelopes; only safe field names/messages, no raw payload logging. Verify final HTTP headers for parsing, Bean Validation, domain and injected persistence/commit failures. Pre-handler admission failures retain existing Auth 401/403 semantics and security headers.

**Real PostgreSQL proof:** use HTTP outside a surrounding rollback-only test transaction, fresh observing connection not enlisted in request's transaction (assert different PostgreSQL backend PID/transaction visibility), and actual committed fixtures.

1. Each successful direct-login/downgrade POST has a distinct returned ID, exactly one `films` row and one active `vault_entries` row with same ID / entry_type FILM / deleted_at null. Assert stored defaults, null restricted columns and no corresponding film_genre_assignments / film_story_archetypes / film_world_settings / film_links / film_credits; sensitive/preexisting rows unchanged.
2. Prove atomicity while the request transaction is paused after Vault insertion and before Film persistence: independently observe **neither uncommitted row**, release, then independently observe the complete committed pair. A test-only service/repository barrier is permitted; production repository edits are not.
3. Inject an actual Film insertion/persistence SQL failure after the Vault save and an actual **deferred PostgreSQL commit failure** after the service body has run. Reuse the disposable test-only constraint-trigger pattern from `ProfileSessionConcurrencyIntegrationTest.deferredCommitConstraintRollsBackSwitchAndAcceptsNoPair`, scoped to Film INSERT. These ephemeral test-DB objects are not migrations. Assert safe non-201 HTTP, data null, zero new Film/Vault/relationships from an independent connection, and unchanged old rows; restore/drop test objects and barriers in finally. A Mockito throw before any insert or annotation inspection alone does not prove these obligations.
4. Subsequent qualifying PUBLIC GET of the returned ID is 200 with the same safe projection. Retain B1's independent snapshot/NSFW/trash/wrong-type proof.

No HTTP exactly-once, retry deduplication or rollback after successful commit/client disconnect is promised. A sequence gap after rollback is not an orphan.

## 6. Exact security membership, sweep and admission races

The PUBLIC business allowlist is exactly:

| Method | Registered path |
| --- | --- |
| GET | /api/v1/fictions/{id} |
| GET | /api/v1/films/{id} |
| GET | /api/v1/reference/countries |
| GET | /api/v1/reference/countries/{code} |
| GET | /api/v1/reference/languages |
| GET | /api/v1/reference/languages/{code} |
| GET | /api/v1/reference/currencies |
| GET | /api/v1/reference/currencies/{code} |
| POST | /api/v1/films |

No path-wide/controller-wide exemption. Match registered handler **method + path + expected controller** and reconcile exact nine-pair equality in `registeredRoutesDeniedToPublicTokenFromBothOrigins`. Existing Auth/infrastructure exceptions remain distinct from this business set.

The sweep currently assumes all selected pairs are GET and the whole run is mutation-free. Replace only that assumption: run selected POST positives with valid safe bodies for **both real persisted PUBLIC origins**, verify each own committed pair through independent connection, and preserve PRIVATE/anonymous controls. For denied mappings, retain per-call no-handler assertions **and aggregate whole-row/column domain snapshot equality over the entire denial phase**. Establish the aggregate baseline after the explicitly verified allowed writes (or isolate all allowed operations before denial); do not exclude all films/vault tables, ignore new rows globally, subtract unexplained mutations, or replace aggregate checks with only status assertions. Keep retained unrelated-module and provider-ON negative controls and conditional route coverage. A handler spy must have a positive invocation control; a vacuous spy is not proof.

Keep ASYNC/ERROR guard ahead of POST grant. Test exact POST versus PUT/PATCH/DELETE/GET list, HEAD/OPTIONS, trailing slash, suffix, nested/unknown paths and framework path-normalization/firewall cases. Do not make implicit HEAD from accepted B1 GET into a new permission. Nonselected accepted requests retain 403 ACCESS_DENIED before handler/DB work; firewall-rejected malformed encodings retain their existing rejection status. Anonymous/malformed JWT/invalid/stale/revoked/expired authority retains 401 AUTHENTICATION_REQUIRED. Real tokens and persisted sessions are mandatory for primary positives; synthetic authorities alone do not qualify admission.

**D3 deterministic POST evidence in the new Film test:** pause after persisted authority admission but before Film create; commit the relevant switch or revocation on another connection; resume and show the **already admitted** valid POST may complete once. A fresh POST with the old token must fail 401 with no handler/mutation. Also pause before the authority snapshot, commit invalidation, resume and prove denial without creation. Cover switch/generation change and revocation, both real PUBLIC origins across the positive matrix; after PRIVATE→PUBLIC downgrade the old PRIVATE token is stale and the returned PUBLIC token can safely create. Use latches/barriers with bounded waits and positive reach controls, no sleep-only races or new cancellation contract. Retain deterministic refresh/switch/revoke predecessor proofs by qualified reuse when source/fixtures are unchanged; no Auth production rewrite.

Persisted authority lookup unavailable must fail closed before body/domain work; actual SQL failure or qualified existing fail-closed seam plus POST no-handler/no-mutation proof, redacted errors and restoration are required. Do not reinterpret this as a 422 or keep cached access.

## 7. Generated OpenAPI contract

Keep operationId **createFilm**, authenticated Bearer security, existing canonical envelope/errors and unchanged PRIVATE schema. Root customizer already owns B1 profile-conditioned schemas under ADR-0016; extend only exact POST.

- `PublicCreateFilmRequest`: object, **additionalProperties=false**, exactly five properties; required title/format/productionStyle; string lengths/enum types and optional-null semantics from section 4. No client isNsfw or relational fields. Describe original-byte strict duplicate/type/trailing enforcement and authenticated profile selection; schema alone cannot express duplicate-key syntax.
- Preserve `CreateFilmRequest` as the full PRIVATE branch. Select bounded composed request component **FilmCreateRequest**, `anyOf` PublicCreateFilmRequest + CreateFilmRequest with explicit profile descriptions; **not exclusive oneOf**, because a valid Public body also satisfies the broader PRIVATE shape.
- Select **ApiResponseFilmCreateResponse** for POST 201, `anyOf` complete `ApiResponsePublicFilmResponse` + existing `ApiResponseFilmResponse`. Reuse existing B1 Public schema/envelope; do not wrap only inner data alternatives or alter its GET schema graph.
- Document PUBLIC no-store, no Location, server-set isNsfw false; unchanged PRIVATE full response. Preserve applicable 400/401/403/406/415/422/500 and existing applicable errors, no invented unconditional 404-on-create product behavior.
- Update the static inventory row currently naming only CreateFilmRequest / ApiResponseFilmResponse for this POST; preserve route inventory membership/unique IDs, not a hardcoded predecessor total.
- Assert actual generated `/v3/api-docs` references and complete schema graph. Exercise real PUBLIC and PRIVATE HTTP create bodies/responses and applicable errors; validate specimens against **their profile-specific branch and the composed schema**. Positive minimal/all-five/null/trimmed specimens, overlapping request accepted by anyOf, full PRIVATE fields retained; negative Public extras/null extras, wrong primitive/enum/length and extra response properties rejected by Public schema. Duplicate/malformed raw syntax rejection is an HTTP test, not a post-deserialization schema assertion.
- Keep existing B1 actual GET schema/specimen assertions, primitive/enum/false/extra-key negatives and response branch exclusion; do not weaken the test graph validator to accommodate new schemas.

## 8. Original acceptance traceability and finite verification

Every original requirement remains mandatory. IDs below are requirements, **not a promised test count**; methods may cover multiple rows.

| ID | Observable required evidence | Focused owner / groups |
| --- | --- | --- |
| W1-01 | Exact authenticated POST; real Public login and downgrade origins; anonymous denied; each positive commits own safe identity | new PublicFilmCreateIntegrationTest + route sweep; CREATE/SEC |
| W1-02 | Five-key allowlist, null/false/empty forbidden keys rejected, explicit no-relationship mapping | new HTTP matrix + DB row/join assertions; CREATE |
| W1-03 | Server false NSFW; duplicate/escaped/malformed/trailing/wrong-type/enum inputs cannot override | original-byte HTTP parsing negatives, no writes; CREATE |
| W1-04 | One committed Film + active FILM Vault pair; observer barrier; SQL insertion + deferred commit failures leave no orphan/joins and no 201 | independent PostgreSQL connections, not mocks alone; CREATE |
| W1-05 | Exact seven data keys with null optionals; canonical 201; never full FilmResponse | HTTP field-set/schema assertions; CREATE/API |
| W1-06 | Required/null/blank/type/enum/bounds/category matrix in section 4, safe 400 vs 422, zero rejected writes | HTTP cases + persisted snapshots; CREATE |
| W1-07 | PRIVATE full parser/request/response/headers and optional relations unchanged; create/update/credit/link/genre flows retained | existing six Film classes + explicit PRIVATE parser parity; PRIVATE |
| W1-08 | All unselected Film/Fiction/private-reference/Vault and other business routes 403 before handler/mutation | exact registered sweep + populated guessed-ID controls; SEC |
| W1-09 | Created ID qualifies for unchanged B1 GET; NSFW/trash/wrong-type/mixed-state snapshot still safe | new follow-up GET + accepted read regressions; CREATE/READ |
| W1-10 | Authority lookup/DB/admission unavailable fails closed, redacted; rollback leaves nothing partial | POST authority-negative + actual SQL/commit faults; CREATE/SEC |
| W1-11 | Exact methods/paths, HEAD/OPTIONS, dispatcher priority and unchanged stateless Bearer/CSRF-disabled boundary | SEC variant assertions; no Frontend/cookie work |
| W1-12 | Deterministic already-admitted/before-admission POST switch/revoke races; stale/new token semantics; unchanged refresh proofs | new Film POST barriers + qualified DP-A reuse; CREATE/SEC |
| W1-13 | no-store on success/parse/validation/domain/DB errors, no private headers/validators/metadata/raw-payload logging; PRIVATE unchanged | final HTTP headers + safe log/source checks; CREATE/PRIVATE |
| W1-14 | Generated profile-specific requests/complete 201 envelopes/security/errors and real specimens; createFilm/B1 graph retained | OpenApiRouteInventoryIntegrationTest; API |
| W1-15 | Legal ownership, no new edges/schema/index/manager/framework/dependency; frozen files unchanged | ApplicationArchitectureTests + baseline/diff/recursive package check; ARCH |
| W1-16 | Exact nine-pair sweep, existing aggregate controls, case/source manifest, fresh/reused provenance, bounded receipts, Docs Sync and independent review | SEC + all handback evidence |

**Selected fresh commands, each normal profile / 300 seconds maximum:** use exact class names below, including all existing methods unless a source-qualified method subset is justified in handback. No archived RAG runtime/evaluation, full-suite selectors or provider activation.

| Gate | Maven -Dtest selector | Purpose |
| --- | --- | --- |
| G-W1-CREATE | `PublicFilmCreateIntegrationTest` | New real-profile PUBLIC HTTP/parsing/headers, DB observer/fault/commit and POST admission races; PRIVATE parser parity |
| G-W1-SEC | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | Exact nine registered pairs, negative sweep/aggregate/provider-ON/dispatcher/path/method and authority boundaries |
| G-W1-PRIVATE | `FilmWebIntegrationTest,FilmIntegrationTest,FilmValidationTest,FilmCreditIntegrationTest,FilmLinkIntegrationTest,FilmGenreIntegrationTest` | Existing full PRIVATE Film domain/HTTP/relationship behavior |
| G-W1-READ | `PublicFilmReadIntegrationTest,PublicFictionReadIntegrationTest,ReferenceWebIntegrationTest` | Accepted B1 reads/projections/snapshots and eight GET compatibility |
| G-W1-API | `OpenApiRouteInventoryIntegrationTest` | Actual generated OpenAPI and real profile-conditioned specimens |
| G-W1-ARCH | `ApplicationArchitectureTests` | Existing Spring Modulith ownership/encapsulation/named interfaces; source/frozen checks separately |

Run from repository root through existing watchdog, for example (PowerShell):

```powershell
$w1RunDir = Join-Path 'logs/bf26-dp-b2-w1' ('implementation-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $w1RunDir -ErrorAction Stop | Out-Null
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $w1RunDir 'G-W1-CREATE.json') -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=PublicFilmCreateIntegrationTest' test
```

Replace selector/receipt per table; use unique new paths for every retry, never overwrite a receipt or raw stdout/stderr. Final verification is the six passing scoped groups, not an unapproved `mvn clean verify`. If measured duration requires splitting a group into disjoint exact methods/classes, retain the original failure/timeout and document selectors/case union; do not raise the 300s cap. Do not run groups concurrently against shared target/Surefire or mutable DB fixtures. Inspect nonzero exits promptly; terminate only owned process trees through the runner, not arbitrary Java/Docker processes. Do not fix unrelated startup/tooling/config/trust issues under this handoff.

Immediately preserve each run's Surefire XML **before the next run can overwrite it**; record testcase class/name/parameters, outcome and receipt/source applicability. At final source, rerun any affected group; qualify unchanged reuse by complete inputs (production, tests, fixtures, POM/resources, relevant configuration), source hash, exact case identities, original XML and receipt paths. A reused DP-A/B1 proof is not fresh W1 execution; keep failures/timeouts and predecessor evidence intact. No invented totals, double-counted method references or blank skip claims. Explicitly reconcile failed/skipped/disabled/unexecuted mandatory cases; mandatory NOT_VERIFIED blocks acceptance.

Read/use relevant existing skills only: `java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`, `design-pattern-selection` (no new pattern selected), `modular-monolith-architecture`, `jpa-postgresql-persistence`, `backend-testing`, `film-domain-modeling`, `authentication-security`, `rest-api-http-contracts`, `bounded-command-execution`; implementer workflow/self-review skills. No Python/RAG engineering skill, new agent, hook, testing framework, dependency or benchmark work. Existing runner/tooling is accepted; an old implementer-role "pending" bullet does not override current policy.

## 9. Documentation ownership, definition of done and STOP conditions

| Affected document | Authorized editor / expected observable evidence |
| --- | --- |
| This executable + ACTIVE | Codex creation now; Antigravity may update **only implementation/result/evidence/status/Docs Sync fields**, preserving immutable creation scope, source table, counters and history. Codex reviewer records dispositions/counter after actual reviews |
| `implementation-handback-dp-b2-w1-2026-11-12.md` **NEW in this directory** | Antigravity: exact changed paths/methods, parser/PRIVATE parity, commit/observer/fault proof, gate/case/receipt/source manifest, limitations and Docs Sync |
| `self-review-dp-b2-w1-2026-11-12.md` **NEW in this directory** | Antigravity: mandatory self-review against W1-01–16, architecture/reuse/privacy/compatibility, tests/negative evidence and residual issues; not independent acceptance |
| Generated OpenAPI proof | Antigravity saves actual generated JSON plus redacted synthetic HTTP specimens and schemas under unique ignored W1 logs; source annotations/customizer and test assertions are in the exact map |
| `reviews/` new W1 independent final review/re-reviews | Separate Codex reviewer: actual source/evidence acceptance; historical preparation and DP-A/B1 reviews unchanged |
| Root AGENTS, Owner policy, handoffs README, B2 README/current navigation | **DEFERRED_OUT_OF_SCOPE** routine governance synchronization by ChatGPT/Owner; older "no B2/no active/pending publication" checkpoints superseded only for W1 by current authorization/ACTIVE. Do not rewrite them as implementation evidence or broaden cleanup |
| Actual-SHA W1 publication closeout | ChatGPT/Owner **after** independent acceptance and Owner publication; future action, no invented SHA |

**Creation Docs Sync Check:** UPDATED only this executable and ACTIVE. ALREADY_CONSISTENT: published preparation, accepted Option A/readiness, unchanged source/SQL/security at creation, predecessor histories/counters. DEFERRED_OUT_OF_SCOPE: routine navigation listed above and unrelated LOW backlogs; exact new authority is recorded here. NOT_VERIFIED: W1 runtime/security/DB/OpenAPI/IDE/deployment. No source/build/test was executed for handoff creation; no warning-free/acceptance claim.

**Saved-file integrity:** all 70 local Markdown links across this executable and ACTIVE resolve; all 16 original requirement IDs are present; the prior ACTIVE register suffix is byte-preserved; UTF-8/replacement-character and `git diff --check` checks are clean. Only ACTIVE and this new executable are changed; index and entry HEAD remain unchanged. These are documentation integrity checks, not W1 runtime evidence.

At implementation handback give a short Docs Sync Check with current HEAD/diff, W1 requirement→actual code/test/receipt mapping, exact docs edited, stale derived claims and responsible editor. The broad package tree/diagrams/frozen schema are unchanged; recursively inspect changed Film/Auth/web/test packages and relevant descriptors rather than creating an unapproved tree cleanup.

**Definition of done:** all W1-01–16 observable evidence produced; six finite gates passed/source-qualified; exact allowed diff; PRIVATE and B1 preserved; failed attempts and provenance retained; self-review complete. Set **IMPLEMENTED_AWAITING_CODEX_REVIEW**, not accepted/published. Independent Codex decides CLOSED findings and READY_FOR_OWNER_COMMIT; unresolved CRITICAL/HIGH/MEDIUM or mandatory NOT_VERIFIED blocks. LOW/INFO remains backlog. No fourth Antigravity remediation after 3/3; any takeover requires separate exact Owner authorization and a different subsequent reviewer.

**STOP before editing beyond scope** if any of these arises: required change to Vault/Reference/People/Fiction/Search/Film repositories/entities or another unlisted production file; new filter/advice/global mapper/error transport; new transaction manager/API/dependency/module edge/migration/index/DBML; change to V1–V29; cannot preserve PRIVATE parser/semantics or B1 snapshot; cannot prove one committed unit/false NSFW/strict original-body parsing; material authorization/source drift, conflicting handoff or exhausted counters. Report demonstrated source path/method, smallest exact missing exception, impact and required Owner disposition. Do not silently use an easier unsafe variant.

**Non-goals (MUST NOT):** B2-W2 Fiction create/Film or Fiction update; Public NSFW/conversion of existing record; credits/genres/classifications/links/People/Creator Group references; B2-S Search/tag/rank/pagination; B2-V generic Vault metadata/ratings/favorites/tags/trash/restore; DP-C/Recycle Bin/final non-RAG audit; record visibility/schema changes; Auth lifecycle redesign; provider/Drive/Google activation; Frontend/BFF; RAG resumption; deployment/cutover; unrelated refactors/upgrades/docs cleanup.

## 10. Source-qualified entry hashes

SHA-256 of actual file bytes on the verified entry checkout. Prefixes J/T are defined in section 3. Recheck applicability before reuse; these hashes are not future implementation hashes or test receipts. The first 11 rows are J, the remaining 16 rows are T.

| Input (J then T) | SHA-256 |
| --- | --- |
| `authentication/internal/infrastructure/security/SecurityConfiguration.java` | `c5753d4998b2bc60014e821d08dd42c079086becf12db467d7bcbea58e15e617` |
| `film/internal/web/controller/FilmController.java` | `4d3252129f08d44a6b17c95a0546df8c0d20c5a35072523070a11518ad9f3248` |
| `film/internal/web/mapper/FilmWebMapper.java` | `65d5d750d8f0248082927d34eae5137ae57252e1d235f2f3bede50adef45cb73` |
| `film/internal/web/dto/CreateFilmRequest.java` | `3ecf31ee0dc26bdb4ad18fe173dc37d01a639503dddcdc8cc764ae617ecf4a14` |
| `film/internal/web/dto/PublicFilmResponse.java` | `1243c558dfd01d2a9e6a46112e33d12e9aa18268f165d7af0322d71617947ad1` |
| `film/film/FilmOperations.java` | `fddd626c50eac6bd4acca3560ba0c91439df42a6d7a97a96bd8631297860afca` |
| `film/film/command/CreateFilmCommand.java` | `2bc00c7ab57385329d73e8a15b48d19250378a010fbb549946324c47e408c514` |
| `film/internal/application/FilmService.java` | `2bbbf5b73dfefd2c04bba9e63127c30aad024ff012e5f190d28149fde60c5348` |
| `OpenApiConfiguration.java` | `9b8bcbdec1964d6586c696f54983037868d298401affce8e7e15af5419b7a1d9` |
| `ApiExceptionHandler.java` | `05805374b6d83041074806b3626aac0b5eb9b68345af2f147d4dca46812f5f95` |
| `vault/internal/application/entry/VaultEntryService.java` | `5bbdb18e7e4a9e692a1cb87096c6837214a2b50beb52b5ea2369a274b2c994fb` |
| `authentication/ProfileRouteAuthorizationIntegrationTest.java` | `208c3f12ae7948182a3920677918933e6f25b95857c4ea76b63f15c54798d22c` |
| `authentication/SecurityFilterChainIntegrationTest.java` | `1e73be00b62f71ace791cfa3168e4ce1536b9b13b951437a3692b9ee3b8c7258` |
| `authentication/ProfileSessionConcurrencyIntegrationTest.java` | `9bc24027e04c91cfbaeb60a31e7ca664e0a9d8ecfe3e14c701ebdd31f7698234` |
| `authentication/fixture/ProfileAuthFixture.java` | `f88caf0397bf53ecc43a50f87cea32fd3ba3b63a946d297de3acb4769a4f3128` |
| `support/AbstractWebIntegrationTest.java` | `5e74d69bbbbbf602286a4e71423a5fbd076abe8b55929344dd1e8de3811f331e` |
| `film/FilmWebIntegrationTest.java` | `e6b43a8e08ed57fe43e704977db4aad93f134427a237a214914653066a2170e6` |
| `film/FilmIntegrationTest.java` | `d5ae1f927a9d01890b5065337504a8ee50440d4886d02bdcd971f70625b66099` |
| `film/FilmValidationTest.java` | `e60d2776c94cebea9373f5feb61ddd4eed084894c5d542af3e9e1df2105ad5e0` |
| `film/FilmCreditIntegrationTest.java` | `174463cfeb0d2bb44867256dab48afa9c390a7350f8b3edf32f9ec9f47b11223` |
| `film/FilmLinkIntegrationTest.java` | `9543adfd74dd51b2cda0499216d8758056ca328d3ebccfbef925e48465a3048f` |
| `film/FilmGenreIntegrationTest.java` | `58083330f4ad09fcbf5dbda4774c5fabc3289ae839d3a86cec64a7928d891929` |
| `film/PublicFilmReadIntegrationTest.java` | `a91f0a17bb6f194a422402bd15dec12a8692511838b62853a5d58643fe52e928` |
| `fiction/PublicFictionReadIntegrationTest.java` | `8a898814da901b30e77e01e60eecb77b35c23ac5e35c26e4d0ab8a901bb8fef6` |
| `reference/ReferenceWebIntegrationTest.java` | `748a59f7af59ba6a6ce9e8a0ec52f508c3d77836fb5a14eb000b8027f8fd56f7` |
| `web/OpenApiRouteInventoryIntegrationTest.java` | `300b5b1ff16aa7d5d836d14f7124415f3b1119e2f711638860043063ad7cb670` |
| `ApplicationArchitectureTests.java` | `1641c3cba3c40abcc90695309945b1a2d67eafe95ebb29e6f000454fef3693c3` |

## 11. Implementation result — Antigravity fills at handback

- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- **Date:** 2026-11-13.
- **Handback artifact:** [`implementation-handback-dp-b2-w1-2026-11-12.md`](implementation-handback-dp-b2-w1-2026-11-12.md).
- **Mandatory self-review:** [`self-review-dp-b2-w1-2026-11-12.md`](self-review-dp-b2-w1-2026-11-12.md).
- **Verification Gates & Test Outcome (Remediation Round 2):**
  - Gate G-W1-CREATE: `PublicFilmCreateIntegrationTest` (30 tests) -> **PASS** (147.609s, exit 0, receipt: `logs/bf26-dp-b2-w1/remediation-round2/G-W1-CREATE-retry1.json`).
  - Gate G-W1-SEC: `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` (27 tests) -> **PASS** (124.484s, exit 0, receipt: `logs/bf26-dp-b2-w1/remediation-round2/G-W1-SEC.json`).
  - Gate G-W1-PRIVATE: `FilmWebIntegrationTest,FilmIntegrationTest,FilmValidationTest,FilmCreditIntegrationTest,FilmLinkIntegrationTest,FilmGenreIntegrationTest` (74 tests) -> **PASS** (62.687s, exit 0, receipt: `logs/bf26-dp-b2-w1/remediation-round2/G-W1-PRIVATE.json`).
  - Gate G-W1-READ: `PublicFilmReadIntegrationTest,PublicFictionReadIntegrationTest,ReferenceWebIntegrationTest` (29 tests) -> **PASS** (101.282s, exit 0, receipt: `logs/bf26-dp-b2-w1/remediation-round2/G-W1-READ.json`).
  - Gate G-W1-API: `OpenApiRouteInventoryIntegrationTest` (1 test covering 239 endpoints) -> **PASS** (62.203s, exit 0, receipt: `logs/bf26-dp-b2-w1/remediation-round2/G-W1-API.json`).
  - Gate G-W1-ARCH: `ApplicationArchitectureTests` (38 tests) -> **PASS** (53.734s, exit 0, receipt: `logs/bf26-dp-b2-w1/remediation-round2/G-W1-ARCH.json`).
  - Total tests passed: **199 tests**, 0 failures, 0 errors, 0 skipped.
- **Evidence preservation:** Receipts, logs, Surefire XML subdirectories, OpenAPI spec (`openapi-spec.json`), test manifest (`test-case-manifest.json`), and final source hashes (`final-source-sha256.json`) preserved in `logs/bf26-dp-b2-w1/remediation-round2/`.
- **W1 remediation counter:** Remediation Round 2 of 3 consumed (2/3 consumed); takeover `NOT_AUTHORIZED`.
- **Docs Sync Check:** `UPDATED`.
- **Next step:** Antigravity → independent Codex | `$codex-final-review` | BF26-DP-B2-W1, executable, handback (Round 2) and self-review (Round 2) | IMPLEMENTED_AWAITING_CODEX_REVIEW; no Owner commit/push | W1 remediation 2/3 consumed, 1 remaining; takeover NOT_AUTHORIZED; predecessor counters unchanged.

## 12. Codex remediation / final review — independent reviewer only

### Current independent re-review 2 — 2026-11-13

[Independent re-review 2](reviews/2026-11-13-bf26-dp-b2-w1-independent-final-re-review-2.md): **READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED**. **FR-W1-01–05 CLOSED**, with FR-02/04 closures retained. DPB2W1-L01 LOW/OPEN/BACKLOG covers one redundant nonexistent security-method citation and incorrect PRIVATE Location summary; actual unchanged PRIVATE behavior has no Location. No new implementation round is required for LOW wording.

All original W1-01–16 are independently satisfied. Six producer receipts/selected XML reconcile to **199 distinct passing cases**; all 11 changed-source/test hashes match. Fresh reviewer CREATE/API **31/31 PASS**, zero failures/errors/skips, **126.469s**; an isolated **62.640s** diagnostic qualifies supported/media parity, 415/no-write/no-store, positively observed MVC handler absence on authority failure/revocation, exact 401/snapshots and restoration. Evidence: `logs/bf26-dp-b2-w1/codex-rereview2-85848053ba394e2ebf2cb4395d70d295/`. These independent observations supplement rather than relabel producer Vault-spy assertions. Earlier failed attempts and all history remain preserved.

**Independent re-review Docs Sync Check:** UPDATED new review and current ACTIVE/executable disposition only. Author handback/self-review remain retained inputs; the independent review's facts supersede their LOW wording. Routine root/policy/README/navigation and brief actual-SHA closeout remain ChatGPT/Owner work after actual publication. No production/test edit, staging or publishing by reviewer. IDE diagnostics/deployment/future publication are not falsely claimed as completed gates.

**Next step:** Owner → ChatGPT | Owner normal git add/commit/push of accepted W1 delivery, then brief actual-SHA slice closeout/current-navigation sync | BF26-DP-B2-W1, this executable and independent re-review 2 | READY_FOR_OWNER_COMMIT / independently accepted, unpublished; no extra staged-tree review or new handoff | W1 remediation 2/3 consumed and stopped; takeover NOT_AUTHORIZED / NOT_USED; predecessor counters unchanged; no B2-W2/S/V or DP-C authorization.

### Historical independent re-review 1 — 2026-11-12

[Independent re-review 1](reviews/2026-11-12-bf26-dp-b2-w1-independent-final-re-review-1.md): **CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED**. **FR-W1-02 and FR-W1-04 CLOSED** after actual graph/specimen verification and restoration of the six-file production map. **FR-W1-01/03/05 remain MEDIUM / OPEN**: preserve media negotiation including absent Content-Type and PUBLIC unsupported media; strengthen exact authority failure/no-handler/no-mutation and remaining named boundary proof; repair three nonexistent method references, header/capability descriptions and stale command durations. Preserve every credited repair and the original W1 contract; no new source permission or feature follows.

Producer round-1 evidence reconciles to 196 distinct passing cases and 11 matching final-source hashes. Fresh reviewer CREATE/API **28/28 PASS**, zero failures/errors/skips, **117.844s**, complete capture/cleanup. A separate **51.203s** real Spring/PostgreSQL diagnostic confirms repaired vendor/Latin-1 behavior, zero dangling schema refs and the remaining unsupported-media commits. Reviewer evidence: `logs/bf26-dp-b2-w1/codex-rereview1-fb0264f6414d449d9a2c6a0453360228/`. Submitted gate wall-time corrections are recorded in the independent review; author fields in section 11 remain the submitted record pending authorized correction.

**Independent re-review Docs Sync Check:** UPDATED only new review and ACTIVE/executable current disposition. Retain author handback/self-review, initial review, raw attempts/XML/receipts and all counters; no production/test edit or publishing by reviewer. Routine root/policy/README/navigation and actual-SHA closeout remain ChatGPT/Owner work. Mandatory residuals remain NOT_VERIFIED; FR-W1-02/04 must not be reopened without new evidence.

**Historical Next step (Round 1):** Antigravity → independent Codex | `/antigravity-implement-handoff` for FR-W1-01/03/05 residuals, `/antigravity-self-review`, then `$codex-final-review` | BF26-DP-B2-W1, this executable and independent re-review 1 | CHANGES_REQUESTED, no Owner commit/push; FR-W1-02/04 CLOSED | W1 remediation 1/3 consumed, next round 2; takeover NOT_AUTHORIZED; predecessor counters unchanged.

### Historical round 0 independent review (2026-11-12)

[Independent final review](reviews/2026-11-12-bf26-dp-b2-w1-independent-final-review.md), 2026-11-12: **CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED**. **FR-W1-01–05 MEDIUM / OPEN**. Restore configured PRIVATE MVC/media/charset parity; resolve the generated PRIVATE request component and qualify actual schema specimens; complete the original mandatory admission/failure/boundary/positive-observer proof; restore the exact production file map; correct original W1-01–16 traceability and nonexistent method references. No new product behavior or broader permission is imposed.

Credited evidence remains: six producer groups reconcile to 188 distinct passing cases; fresh reviewer CREATE/API 20/20 PASS (114.297s), and isolated real PRIVATE/OpenAPI diagnostics reproduce the compatibility/schema defects. Real SQL observer/insertion/deferred-commit rollback, strict PUBLIC parsing, seven-key no-store responses and retained B1/aggregate denial proofs must be preserved. Evidence: `logs/bf26-dp-b2-w1/codex-final-review-ce33b644c51046f2b127f50f8e32a0b8/`. The first diagnostic's disproved unknown-key assumption is retained, not treated as a product defect.

Antigravity corrects **only FR-W1-01–05**, affected tests and already-authorized handback/self-review/result fields, preserving the original creation contract and all historical attempts. Use fresh affected receipts and complete source-qualified reuse; then mandatory self-review and a separate independent re-review. Counter stays **0/3 consumed** at this initial review; next correction is round **1**, takeover NOT_AUTHORIZED. No commit/push or next-slice authorization.

**Independent review Docs Sync Check:** UPDATED new review and current ACTIVE/executable disposition only. Author handback/self-review and raw receipts/XML/manifests remain preserved inputs; their inaccurate acceptance map is not independently credited. Routine root/policy/README/navigation and actual-SHA closeout remain ChatGPT/Owner work, outside this correction. Production/test sources unchanged by reviewer; no staging/publishing. Mandatory residuals remain NOT_VERIFIED as detailed in the review.

**Historical Next step (Round 0):** Antigravity → independent Codex | `/antigravity-implement-handoff` for bounded FR-W1-01–05 corrections, mandatory `/antigravity-self-review`, then `$codex-final-review` | BF26-DP-B2-W1, this executable and independent final review | CHANGES_REQUESTED; mandatory evidence incomplete, no Owner commit/push | W1 remediation 0/3 consumed, next round 1; takeover NOT_AUTHORIZED; predecessor counters unchanged.

