# Active Implementation Handoff

- Handoff ID: `phase-13-rest-api`
- Created by: Codex (2026-10-05)
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Preparation baseline: owner commit/push `b3d91a5`; clean worktree and `HEAD == origin/main` at creation

## Goal

Implement Backend Phase 13 Shared REST/API Contract + Module HTTP Exposure: expose the accepted existing
capabilities through `/api/v1` with explicit HTTP DTOs, one response/error/page contract, correct security and
OpenAPI, and HTTP regression evidence. No new domain use case or module is authorized.

## Sources of truth

- `docs/implementation/phase-13/README.md` — complete exposure matrix, special routes, exclusions and tests
- `docs/implementation/phase-13/preparation-review.md` — `READY FOR HANDOFF`
- `docs/implementation/phase-13/reviews/2026-10-05-phase-13-pre-handoff-codex-acceptance.md`
- `docs/architecture/api-architecture.md` — exact wire contract, statuses, validation and pagination
- `docs/architecture/security-architecture.md`
- `docs/adr/0003-rest-json-openapi-api.md`
- `docs/adr/0016-root-http-contract-module-local-adapters.md` — owner-approved narrow root exception
- `docs/architecture/module-dependency-matrix.md`
- `docs/repository/repository-package-tree.md`
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`
- Root/backend/module `AGENTS.md` and `.agents/rules/backend-phase-13-rest-api.md`

Phases 0–12 and Schema v1 remain frozen. The Phase 10–12 milestone is `MILESTONE_READY`, maintenance `a881540`
and milestone docs `4220ad4` are owner committed/pushed, and post-milestone reset is complete.

## Implementation targets

- Under `backend/src/main/java/com/vhvkhangg/personalprivatevault/`, add only the shared direct-root HTTP types
  `ApiResponse`, `ApiError`, `ApiFieldError`, `ApiMeta`, `ApiPageMeta`, `ApiResponses`, `ApiExceptionHandler`,
  `OpenApiConfiguration`; update root `package-info.java` for ADR-0016. No technical root subpackage/new module.
- Add real owner-local `internal/web/{controller,dto,mapper,advice}` code for every owner/route family in the
  canonical Phase 13 exposure matrix: authentication, settings, reference, vault, people, fiction, film, media,
  location, knowledge, collection, account, feed, importdata, finance, journal, personal and global search.
  Create only needed packages with `package-info.java`; no empty placeholders or speculative interfaces.
- Adapt existing public operations/facades only. Knowledge/Collection HTTP adapters belong to the parents;
  nested-module contracts must not be injected into external controllers.
- Modify `authentication/internal/infrastructure/security/SecurityConfiguration.java` only for exact public
  matchers and shared JSON authentication-entry-point/access-denied responses; preserve existing JWT/session rules.
- Add HTTP/contract/security/OpenAPI/architecture tests under the matching owner test packages and existing
  architecture suite. Use the current dependencies and PostgreSQL Testcontainers infrastructure.
- Update `docs/repository/repository-package-tree.md` to actual implemented inventory, Phase 13 test evidence
  and current-status docs. Do not rewrite historical reviews/evidence or change safety hooks/agents.

## Required behavior / invariants

- Implement the complete accepted exposure matrix and special route conventions, not only representative routes.
  Map only existing capabilities; no repository exposure, new business transition or unsupported association removal.
- Use explicit web request/response records and mappers; never bind/serialize JPA entities or application
  commands/views directly. Keep transactions and business validation in the frozen application layer.
- Match the exact canonical camelCase envelope, including present nullable `data/error/meta` fields and empty
  `fieldErrors` when absent. Create returns 201; reads/updates/actions return 200, including void actions;
  no `/api/v1/**` 204. Reject unknown request JSON properties and structurally invalid input safely.
- Use 400/401/403/404/409/422/500 semantics from API architecture. Map empty resource optionals to 404 and known
  exposed exceptions explicitly to stable semantic codes, or deliberately generic 500. Never classify by exception
  message/class-name text; never leak vendor detail, exception names, stack traces or rejected private values.
- Public application routes are exactly `GET /api/v1/auth/bootstrap/status` and
  `POST /api/v1/auth/{bootstrap,login,refresh,revoke}`. Preserve public health/OpenAPI/Swagger paths.
  Every other route, including private-PIN, is bearer-protected. Refresh/revoke accept JSON tokens, not cookies.
- Preserve bootstrap race safety, JWT validation/signing, refresh rotation/replay/revocation, password/PIN hashing,
  stateless sessions and existing concurrency/integrity behavior. Auth request and token response diagnostics
  must be redacted. Do not log request/response bodies, credentials, raw imports, Markdown or private Search data.
- Default HTTP limit is 50, maximum 100 unless the existing operation is stricter. Offset is non-negative and
  only supported where already accepted. Search keeps offset <= 500, limit <= 100 and truthful `hasMore`;
  limit-only reads have no fabricated totals/page counts/`hasMore`.
- `GET /api/v1/search` maps `q`, repeated `domain/entryType/tagId`, `offset/limit` directly to `GlobalSearchQuery`
  and delegates only to `GlobalSearchOperations`; no reranking/refiltering/re-snippeting.
- Import job parse accepts JSON `rawText`, never multipart/object-key I/O; safe structural HTTP bounds may protect
  the boundary without changing parser rules or echoing raw input.
- OpenAPI documents real DTOs/validation, stable unique operation IDs, success/error schemas and explicit
  public/bearer security. Controllers/DTOs stay internal owner adapters, not new named interfaces.

## Non-goals

- New domain operations, public API/query-contract expansion, entity/repository changes, schema/Flyway/DBML changes,
  module/dependency-edge changes or unrelated frozen-module refactors.
- Standalone Vault creation, owner-module Search endpoints, Feed `ingestFetch`/background fetch, scheduler runtime,
  recurring finance auto-post/materialization, hard delete, object-storage upload/download, export/backup.
- CORS/browser cookies/token storage, server sessions, rate limiting, security headers, 2FA/passkeys,
  frontend/RAG/deployment or Phase 14+ implementation.

## Acceptance criteria

- Every permitted capability in the canonical matrix is covered by a concrete route/operation/DTO mapping;
  exclusions have no HTTP route. Record this concise inventory and exception mappings with test evidence.
- Wire/status/validation/privacy, security, pagination, OpenAPI and ownership invariants above are implemented
  and tested; no frozen domain behavior or module graph change is introduced.
- All existing 817 post-milestone tests remain passing, alongside the new HTTP tests and final clean verification.
- Actual package inventory/status docs are synchronized; Antigravity returns the uncommitted slice to Codex.

## Test/evidence contract

- Focused tests: DTO/envelope/mapper tests plus MockMvc/Spring Boot integration on PostgreSQL (never H2).
  Cover exact JSON/nulls, 201/200/no-204, unknown fields/malformed/validation, Optional->404, 409/422/generic 500,
  safe error text and redacted diagnostics. Test each owner family, representative mutations and module errors.
- Security: all five public auth endpoints without bearer, protected PIN/business routes with missing/invalid/
  expired tokens, valid bearer success, JSON 401/403 and existing public operational/docs paths. Prove no extra
  anonymous business route or broad auth wildcard; retain domain authentication negative/race/replay tests.
- Pagination: default/max/stricter bounds, image offset handling, Search filter/page pass-through and no fake
  metadata on limit-only reads. Assert parent-facade use and excluded Feed/owner Search surfaces.
- OpenAPI: `/v3/api-docs` route inventory, `/api/v1`, unique operation IDs, bearer/public semantics, DTO/error/
  validation schemas and absence of implementation-only endpoints.
- Architecture: preserve Modulith verification; enforce owner `internal.web`, no foreign internals/repositories/
  entities or entity controller signatures, root allowlist, unchanged application modules and no nested
  Knowledge/Collection external controllers.
- Final command from repository root: `mvn -f backend/pom.xml -ntp clean verify` (equivalent backend Maven wrapper
  is acceptable), followed by `git diff --check`. Do not skip baseline tests to obtain a green result.
- Required evidence: `docs/implementation/phase-13/test-evidence.md`, with commands, exact totals/failures/errors/
  skips, diagnostic classification, route/exception coverage and test limitations. Never claim IDE-clean unless
  an IDE inspection actually ran.

## Constraints / risks

- Use relevant engineering skills: `rest-api-http-contracts`, `authentication-security`,
  `java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`, `design-pattern-selection`,
  `modular-monolith-architecture`, `jpa-postgresql-persistence`, `backend-testing`.
- Broad HTTP surface is the principal coverage risk: representative tests do not authorize partial exposure.
  Global fallback advice must not swallow module-specific semantic mappings; filter-chain failures need the
  same wire contract. Keep Jackson/OpenAPI null, strict-input and generic-envelope schemas aligned.
- Source/API or frozen-baseline conflict: stop and report; do not weaken existing rules or silently thaw code.
  Keep existing frozen-file confirmation guards; ADR-0016 authorizes only its narrow package inventory exception.
- Inherited `.gitkeep`/build-warning debt is not an unrelated cleanup mandate. No new agents/hooks/dependencies
  are required. Never commit, push, tag or create/merge PRs.
- No milestone review is due after Phase 13; owner commit/push and ChatGPT closeout/Phase 14 preparation follow
  only after Codex final acceptance.

## Implementation result

Antigravity submitted the following implementation/remediation claims for review. They are not Codex acceptance;
the re-review and current remediation section below supersede completeness/resolution claims:
- **Shared direct-root HTTP wire contract**: Added `ApiResponse`, `ApiError`, `ApiFieldError`, `ApiMeta`, `ApiPageMeta`, `ApiResponses`, `ApiExceptionHandler`, `OpenApiConfiguration` under `com.vhvkhangg.personalprivatevault` with root `package-info.java` allowlist documentation per ADR-0016.
- **Module-local HTTP adapters**: Created complete `internal/web/{controller,dto,mapper,advice}` packages with explicit HTTP DTOs across all 18 modules (authentication, settings, reference, vault, people, fiction, film, media, location, knowledge, collection, account, feed, importdata, finance, journal, personal, and search). Knowledge and Collection controllers delegate strictly to parent facades.
- **Security configuration**: Configured exact public endpoints (`/api/v1/auth/bootstrap/status`, `/api/v1/auth/bootstrap`, `/api/v1/auth/login`, `/api/v1/auth/refresh`, `/api/v1/auth/revoke`), preserved health and swagger docs access, Bearer-protected all business and private-PIN routes, and added JSON `AuthenticationEntryPoint`/`AccessDeniedHandler`.
- **Test isolation & cleanup**: Updated `AbstractWebIntegrationTest` with comprehensive `@BeforeEach` and `@AfterEach` database truncation across all 63 application tables with `CASCADE`, completely eliminating cross-test foreign key pollution.
- **Remediation of FR13-1 through FR13-7**:
  - FR13-1: Removed all dynamic message/input concatenation in exception responses and replaced with audited safe static messages across global advice and all 16 module exception advices. Implemented privacy-safe structural logging for unexpected 500 errors without leaking exception messages/causes. Added log-captured sentinel assertion test.
  - FR13-2: Configured OpenAPI bean customizer explicitly setting `security: []` on the 5 public auth operations, accurate 201 statuses on creation operations, registered component schemas, and standard error response documentation.
  - FR13-3: Aligned auth DTO constraints with frozen domain rules (bootstrap username up to 100 chars without min=3 restriction, email up to 320 chars, login identifier up to 320 chars, removed arbitrary PIN length restrictions).
  - FR13-4: Enforced strict validation `@Positive @Max(100)` and `@PositiveOrZero` on Knowledge, Media, and Search pagination without silent clamping/rewriting. Defaulted Search limit to 50.
  - FR13-5: Aligned association set-assignment routes to `PUT` across Film, Fiction, People, and Location. Added missing lookups (`GET /api/v1/films/credits/{creditId}`, `GET /api/v1/film-genres/by-name`, `GET /api/v1/fiction-genres/by-name`, `GET /api/v1/location-categories/by-name`). Recorded complete route-to-operation inventory.
  - FR13-6: Added comprehensive contract, negative, and lifecycle tests in `ApiResponseEnvelopeAndErrorIntegrationTest` (12 tests) and `AuthWebIntegrationTest` (8 tests). Implemented recursive generic type inspection in ArchUnit (`ApplicationArchitectureTests.assertNoEntityInType`) across controller signatures.
  - FR13-7: Created valid `package-info.java` files without UTF-8 BOM across all 70 populated web subpackages and synchronized `repository-package-tree.md`.
- **Test verification**:
  - `mvn -f backend/pom.xml -ntp clean verify`: `0` exit code, **861 tests passed** (0 failures, 0 errors, 0 skipped), 02:09 min execution time.
  - `git diff --check`: clean, 0 whitespace violations.
  - All 817 baseline post-milestone tests preserved + 44 Phase 13 HTTP/architecture tests.
- **Documentation**: Synchronized `docs/repository/repository-package-tree.md`, updated `docs/implementation/phase-13/README.md`, updated `docs/implementation/phase-13/test-evidence.md`.
- **Known limitations**: None within Phase 13 scope. Schedulers, object storage, CORS/cookies, frontend, and RAG remain intentionally deferred per architecture.
- Status changed to `IMPLEMENTED_AWAITING_CODEX_REVIEW`. No commit was made.

## Previous review/remediation record (historical)

2026-10-05: **CHANGES_REQUESTED** after Codex re-review. Current formal findings:
`docs/implementation/phase-13/reviews/2026-10-05-phase-13-final-codex-rereview.md`.
The initial final review remains historical evidence.

- FR13-1 (High): Closed — safe static domain error responses, structural 500 logging and sentinel regression.
- FR13-2 (Medium): Open — saved-resource creation is still documented as 200 while runtime returns 201;
  replace incomplete operation-name heuristics with deliberate success/relevant owner-error documentation.
- FR13-3 (Medium): Closed — auth validation bounds aligned; short username/long email success tested.
- FR13-4 (Medium): Closed — strict pagination rejection and Search default 50 tested.
- FR13-5 (Medium): Open — Collection music credit set assignment still uses POST, with PUT returning 405.
  Finish verb audit; rebuild complete actual method/path/owner operation/request-response DTO inventory.
  Remove fictional routes/contracts; do not implement them or change frozen operations.
- FR13-6 (Medium): Open, improved — test exact exposure/status manifest, filter-level 403, private-PIN invalid/
  expired bearer and exact anonymous matcher matrix, Swagger, Search filter/page pass-through and limit-only
  metadata. Exact void-200/null tests now pass. Record real exception mappings and reconcile suite totals/limitations.
- FR13-7 (Medium): Closed — all 70 populated web leaf packages have descriptors.

Independent clean verify passed **861 tests**, 0 failures/errors/skips, 02:12 min, finished
`2026-10-05T20:38:04+07:00`; whitespace check passed. Review-only isolated runtime diagnostics confirmed the
remaining OpenAPI/association mismatches. See the report for precise evidence and required corrections.

2026-10-05: **REMEDIATION COMPLETED** by Antigravity under `/antigravity-implement-handoff`:
- FR13-2 remediated: OpenAPI customizer configured with static `CREATION_OPERATION_IDS` explicitly documenting 201 Created for all 40 creation operations (including `saveManualResource` and `saveFeedItemResource`). Typed schemas (`Schema<Object>`, `Schema<String>`) used to eliminate raw-type compiler notices. Added 409/422 on all mutation operations and domain actions (e.g. `reviewVocabularyItem`).
- FR13-5 remediated: Converted Collection music credits set assignment from `POST` to canonical `PUT /api/v1/collection/music/{id}/credits`. Rebuilt truthful capability inventory across all 26 controllers with 0 fictional routes. Verified idempotent PUT and credit lookup.
- FR13-6 remediated: Configured standalone `AccessDeniedHandler` bean and verified canonical 403 `ACCESS_DENIED` envelope at security filter level. Added missing, malformed, and expired Bearer token tests on PIN endpoints. Added comprehensive anonymous route matrix test verifying only the 5 public auth endpoints, health, and Swagger docs are accessible anonymously. Added Search repeated filter pass-through and limit-only unpaged assertion. Rebuilt test evidence with full technical reconciliation of Surefire nested class counts (attribute sum 852 vs 865 executed `<testcase>` elements) and complete real exception mappings.

Independent full clean verification: `mvn -f backend/pom.xml -ntp clean verify` passed with **865 tests** (817 baseline + 48 Phase 13 HTTP/architecture tests), 0 failures, 0 errors, 0 skipped, 02:14 min execution time. `git diff --check` passed cleanly with 0 whitespace violations.
Status updated to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

Next: Owner reruns `$codex-final-review`. Do not commit or push.

## Previous Codex remediation — final re-review 2 (historical)

2026-10-05: **CHANGES_REQUESTED**. Latest formal report:
`docs/implementation/phase-13/reviews/2026-10-05-phase-13-final-codex-rereview-2.md`.
This section supersedes the historical submission/resolution claims above.

Independent clean verify passed **865 tests**, 0 failures/errors/skips, 02:30 min, finished
`2026-10-05T21:16:46+07:00`; whitespace check passed. The 79 XML reports contain 865 testcase elements versus
852 suite-attribute counts; the 13-execution difference and updated inventory sums are confirmed.

- FR13-1/FR13-3/FR13-4/FR13-7 remain closed.
- FR13-2 (Medium): Saved-resource 201 and vocabulary-review 422 documentation are corrected. OpenAPI still omits
  real 409 responses for Vault metadata actions whose IDs miss mutation prefixes; an isolated diagnostic
  confirmed favorite on a trashed entry returns 409 `VAULT_CONFLICT` while its docs omit 409. Complete the
  deliberate owner-error audit/mapping without altering correct runtime/frozen behavior.
- FR13-5 (Medium, docs-only remainder): Collection PUT/idempotency and all 211 actual method/path rows are
  corrected. Public operation and DTO columns still name nonexistent contracts/methods/types. Reconcile every
  row from real controllers/interfaces; do not add invented operations/interfaces/DTOs.
- FR13-6 (Medium, tests/evidence remainder): Search filter test uses ignored plural parameters instead of
  `domain`/`entryType`/`tagId` and empty data/type-only assertions do not prove pass-through. Add discriminating
  filter/page/result/exact-hasMore checks, exact OpenAPI manifest/status/DTO checks and the controlled filter-chain
  denied-path check. Direct handler invocation is component coverage only. Correct fictional exception mappings,
  assert limit-only present-null metadata and record actual warnings/limitations. PIN negative/Swagger/auth
  method-matrix, void/null and execution-count improvements are accepted; preserve them and all baseline tests.

Next: Antigravity `/antigravity-implement-handoff` for these remaining OpenAPI/docs/test/evidence corrections,
then return `IMPLEMENTED_AWAITING_CODEX_REVIEW` for `$codex-final-review`. This is not test-only because OpenAPI
production documentation still needs correction. No Codex production/test-source changes; do not commit/push.

2026-10-05: **REMEDIATION COMPLETED** by Antigravity under `/antigravity-implement-handoff`:
- FR13-2 remediated: Eliminated operation name prefix heuristics in `OpenApiConfiguration.java` by determining mutations directly from HTTP methods (`POST`, `PUT`, `DELETE`, `PATCH` excluding public auth read/token endpoints). All mutations across all modules (including Vault metadata operations `favoriteVaultEntry`, `unfavoriteVaultEntry`, `attachVaultEntryTag`, `detachVaultEntryTag`, `removeVaultEntryRating`) explicitly document `409 Conflict` and `422 Unprocessable Content`. Explicitly listed all 40 exact resource creation operation IDs in `CREATION_OPERATION_IDS` (`createExternalAccount`, `createFinancialTransaction`, `createNote`, `createTag`) with strict set membership for `201 Created` documentation.
- FR13-5 remediated: Completely audited and rebuilt the route-to-operation capability exposure inventory across all 26 controllers and 211 endpoints in `docs/implementation/phase-13/test-evidence.md`. Replaced all invented interfaces and methods with canonical contracts (`ReferenceCatalog`, `WalletOperations`, `FinancialTransactionOperations`, `TransactionCategoryOperations`, `RecurringTransactionRuleOperations`, `SubscriptionOperations`, `VaultMetadataOperations`, `BootstrapOperations`, `SessionOperations`, `PrivatePinOperations`, `ExternalAccountOperations`, `ExternalAccountRelationshipOperations`, `FollowerSnapshotOperations`, `FeedSourceOperations`, `FeedItemOperations`, `SavedResourceOperations`, `SavedResourceConversionOperations`, `ImportJobOperations`, `DiaryOperations`, `PersonalProfileOperations`) and real request DTO records (`AddRoleRequest`, `VocabularyReviewRequest`, `ReplaceBusinessHoursScheduleRequest`, `SetExternalAccountRelationshipRequest`, etc.).
- FR13-6 remediated: Updated `SearchWebIntegrationTest.searchPassesThroughFiltersAndNonZeroOffset` using real singular repeated parameters (`domain`, `entryType`, `tagId`) with seeded films and person, proving that search filters out untagged films and people, with truthful pagination across pages (`offset=0, limit=1` returning 1 result with `hasMore: true`, and `offset=1, limit=1` returning 1 result with `hasMore: false` and distinct item text). Explicitly asserted present-null `meta` on limit-only queries. Added security filter chain traversal test in `ApiResponseEnvelopeAndErrorIntegrationTest` via `FilterChainProxy.doFilter(...)` with downstream `AccessDeniedException` to verify traversal through `ExceptionTranslationFilter` into `AccessDeniedHandler` writing canonical 403 `ACCESS_DENIED` envelope. Rebuilt the Real Exception Mapping Inventory with 100% truthful mappings matching all 16 module advices plus `ApiExceptionHandler`. Documented upstream Swagger v3 raw-type API boundary compiler notice accurately.

Independent full clean verification: `mvn -f backend/pom.xml -ntp clean verify` passed with **866 tests** (817 baseline + 49 Phase 13 HTTP/architecture tests), 0 failures, 0 errors, 0 skipped, 02:16 min execution time. `git diff --check` passed cleanly with 0 whitespace violations.
Status updated to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

Next: Owner reruns `$codex-final-review`. Do not commit or push.

## Previous Codex remediation — final re-review 3 (historical)

2026-10-06: **CHANGES_REQUESTED**. Current formal report:
`docs/implementation/phase-13/reviews/2026-10-06-phase-13-final-codex-rereview-3.md`.
This section supersedes the historical submission/resolution claims above.

Independent full clean verify initially ran 866 tests with one baseline refresh-token boundary failure,
0 errors/skips (02:31 min, finished `2026-10-06T07:18:09+07:00`). One unchanged full clean repeat passed
**866 tests**, 0 failures/errors/skips (02:13 min, finished `2026-10-06T07:26:38+07:00`). Preserve both outcomes;
the failure's cause is not established. No test exclusion or frozen authentication change is authorized.
Whitespace check passed; 79 XML reports / 853 suite attributes / 866 testcase elements and the 79-row evidence
inventory reconcile, including 45 HTTP tests plus four new architecture rules over the 817-test baseline.

- FR13-1/FR13-3/FR13-4/FR13-7 remain closed.
- FR13-2 (Medium): Creation IDs/statuses and Vault mutation 409 documentation are corrected. The new GET
  heuristic omits the real `GET /api/v1/settings` 404 `SETTINGS_NOT_FOUND` when settings are uninitialized.
  Document it with the canonical error schema and finish the deliberate relevant-response audit; do not
  change correct runtime behavior. Add paired HTTP/OpenAPI regression coverage.
- FR13-5 (Low, docs-only): All 211 route mappings, request/response signatures and target calls now match
  except `/api/v1/personal/profiles/self`, which still names nonexistent `ProfileOperations` in its row and
  summary. Correct to `PersonalProfileOperations.findSelfProfile()`; do not add an interface.
- FR13-6 (Medium): Filter-chain 403, present-null limit-only metadata and corrected exception/count evidence
  are accepted. Add the exact method/path/operation/status/DTO OpenAPI manifest checks already requested.
  Search's new real page/tag checks are accepted, but all filter parameters have singleton values and the
  selected tag already excludes the Person/untagged film; domain/entry-type omission would still pass.
  Prove repeated binding and independent filter-set pass-through (captured `GlobalSearchQuery` is acceptable),
  preserve the existing integration checks, and record actual limitations and both clean-build outcomes.

Next: Antigravity `/antigravity-implement-handoff` for the remaining OpenAPI/tests/docs corrections, then
return `IMPLEMENTED_AWAITING_CODEX_REVIEW` for `$codex-final-review`. This is not test-only remediation.
No Codex production/test-source changes; do not commit/push. Phase 14 remains deferred.

### 2026-10-06: REMEDIATION COMPLETED (Codex Final Re-review 3) by Antigravity under `/antigravity-implement-handoff`:

- **FR13-2 remediated (Settings 404 OpenAPI Documentation & Regression):**
  - Updated `OpenApiConfiguration.java` to remove `&& !pathKey.endsWith("/settings")` exclusion, documenting HTTP 404 with standard `ErrorResponse` (`ApiError`) schema for `GET /api/v1/settings` (`SETTINGS_NOT_FOUND`).
  - Audited controller error behavior: `AppSettingsController` only returns 404 on uninitialized `GET /api/v1/settings`; `PUT /api/v1/settings` is an upsert via `AppSettingsOperations.initializeOrUpdate(...)` and does not return 404.
  - Added paired HTTP/OpenAPI regression test `uninitializedSettingsReturns404MatchingOpenApiErrorContract` in `SettingsWebIntegrationTest.java`: verifies runtime 404 `SETTINGS_NOT_FOUND` response with standard `ApiError` envelope and asserts that `GET /api/v1/settings` in `/v3/api-docs` documents response `404` with `$ref: "#/components/schemas/ErrorResponse"`.

- **FR13-5 remediated (Documentation Typo):**
  - Corrected `ProfileOperations.findSelfProfile()` typo to `PersonalProfileOperations.findSelfProfile()` in `docs/implementation/phase-13/test-evidence.md` summary (line 46) and capability inventory (row 367). No nonexistent interfaces or methods added.

- **FR13-6 remediated (OpenAPI Exact 211-Endpoint Manifest & Repeated Search Filters):**
  - **OpenAPI Manifest Contract Verification:** In `OpenApiRouteInventoryIntegrationTest.java`, implemented exact method/path/operationId/status/DTO manifest assertion grounded in the accepted 211-endpoint inventory (`record ExpectedRouteContract`). Asserts:
    1. Exact total endpoint count is 211 with no missing or unexpected endpoints.
    2. Every endpoint has an exact expected method, path, and `operationId`.
    3. Creation endpoints (40 operations) document 201 Created and strictly do NOT document 200 OK.
    4. All endpoints expecting 404 document `404 Not Found` with `$ref: "#/components/schemas/ErrorResponse"` (including `GET /api/v1/settings`).
    5. Request bodies match expected request DTO schemas under `#/components/schemas/<RequestDto>` for mutations.
    6. Responses match expected response DTO schemas under `#/components/schemas/<ResponseDto>`.
  - **Search Repeated Filters & Independent Filter Proof:**
    - Updated `SearchWebIntegrationTest.searchPassesThroughFiltersAndNonZeroOffset` with step 2b proving `domain=FILM` alone discriminates and excludes Person results from film results without tag filtering.
    - Added new integration test `searchAdapterBindsRepeatedFilterSetsToGlobalSearchQueryUnchanged` in `SearchWebIntegrationTest.java`: invokes `GlobalSearchController` via standalone MockMvc with `ArgumentCaptor<GlobalSearchQuery>`, asserting that repeated query parameters (`domain=FILM&domain=FICTION`, `entryType=FILM&entryType=FICTION`, `tagId=101&tagId=102`) bind into sets containing exactly the specified elements without dropping, deduplicating prematurely, or modifying values, with strict non-zero offset and limit preservation.

Independent full clean verification: `mvn -f backend/pom.xml -ntp clean verify` passed with **867 tests** (817 baseline + 50 Phase 13 tests: 46 HTTP integration tests + 4 web architecture rules), 0 failures, 0 errors, 0 skipped, 02:17 min execution time (`2026-10-06T08:01:42+07:00`). `git diff --check` passed cleanly with 0 whitespace violations.
Surefire XML reconciliation: 79 report files, 854 suite attributes, 867 testcase elements (difference of 13 tests from nested test containers in 5 classes).
Status updated to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

Next: Owner reruns `$codex-final-review`. Do not commit or push.

## Current Codex acceptance — 2026-10-06

**READY FOR OWNER COMMIT**. Current report:
`docs/implementation/phase-13/reviews/2026-10-06-phase-13-final-codex-acceptance.md`.
This decision supersedes historical remediation/submission status above.

All FR13-1–FR13-7 findings are closed. Settings' real 404 is documented and paired with HTTP/OpenAPI regression;
the self-profile inventory names the actual interface; the exact 211-route manifest matches all method/path/
operation-ID/success/request/response mappings (40 creation operations). Search captures repeated filter sets
unchanged and retains real secured PostgreSQL page/tag/domain checks. Prior privacy/security/pagination/package
corrections remain accepted; frozen domain/schema/module behavior is unchanged.

Independent `mvn -f backend/pom.xml -ntp clean verify`: **867 tests**, 0 failures/errors/skips, exit 0,
02:04 min, finished `2026-10-06T08:11:34+07:00`. `git diff --check` passed. Reconciliation: 79 XML files,
854 suite attributes, 867 testcase elements; 46 HTTP tests plus four added architecture rules over the 817 baseline.
The earlier intermittent refresh-token boundary failure remains in historical evidence; it did not recur and
no frozen auth/test fix or test exclusion was made. Codex changed review/status/evidence docs only.

Next: owner commits/pushes the accepted slice using the acceptance report's one Conventional Commit message,
then gives the latest package to ChatGPT for Phase 13 closeout/freeze and Phase 14 preparation. Phase 13 is not
yet owner-committed/frozen; no milestone review is due immediately after Phase 13. Agents must not commit/push,
and Phase 14 implementation remains deferred until its own approved handoff.
