# BF26-DP-B2-W1 Implementation Handback (Remediation Round 2)

- **Date:** 2026-11-13
- **Handoff Authority:** [`executable-handoff-dp-b2-w1-2026-11-12.md`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/docs/implementation/maintenance/2026-11-12-bf26-dp-b2-preparation/executable-handoff-dp-b2-w1-2026-11-12.md)
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- **Remediation Allowance:** Remediation Round 2 of 3 consumed (2/3 consumed). Takeover `NOT_AUTHORIZED`.
- **Verification Result:** All 6 Verification Gates Passed Cleanly (199 test executions across 6 test suites, 0 failures, 0 errors, 0 skips; receipts and logs preserved in [`logs/bf26-dp-b2-w1/remediation-round2/`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/logs/bf26-dp-b2-w1/remediation-round2/)).

---

## 1. Remediation Findings Reconciliation (FR-W1-01, FR-W1-03, FR-W1-05)

Independent final re-review 1 ([`reviews/2026-11-12-bf26-dp-b2-w1-independent-final-re-review-1.md`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/docs/implementation/maintenance/2026-11-12-bf26-dp-b2-preparation/reviews/2026-11-12-bf26-dp-b2-w1-independent-final-re-review-1.md)) CLOSED **FR-W1-02** and **FR-W1-04**. The three remaining findings (**FR-W1-01**, **FR-W1-03**, **FR-W1-05**) have been resolved and verified in Remediation Round 2:

- **FR-W1-01 (Media handling divergence & unsupported media types):**
  - Resolved `PRIVATE` missing/blank `Content-Type`: In [`FilmCreateRequestReader.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/controller/FilmCreateRequestReader.java), `readPrivate(byte[] body, HttpServletRequest servletRequest)` immediately throws `HttpMediaTypeNotSupportedException` with all supported converter media types when `Content-Type` is absent or blank, returning HTTP 415 `UNSUPPORTED_MEDIA_TYPE` instead of defaulting to `application/json`.
  - Resolved `PUBLIC` media type negotiation: Updated [`FilmController.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/controller/FilmController.java) to pass `servletRequest` into `filmCreateRequestReader.readPublic(body, request)`. In `FilmCreateRequestReader.java`, `readPublic(byte[] body, HttpServletRequest servletRequest)` inspects `Content-Type`. Missing, blank, or unsupported media types (such as `text/plain` or `application/xml`) throw `HttpMediaTypeNotSupportedException` returning HTTP 415. Retained helper `isSupportedJsonMediaType` for `application/json` and `application/*+json`.
  - Added test coverage in [`PublicFilmCreateIntegrationTest.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmCreateIntegrationTest.java):
    - `publicMissingContentTypeReturns415`: asserts HTTP 415, `UNSUPPORTED_MEDIA_TYPE`, and zero DB mutation.
    - `publicTextPlainContentTypeReturns415`: asserts HTTP 415, `UNSUPPORTED_MEDIA_TYPE`, and zero DB mutation.
    - `privateMissingContentTypeReturns415`: asserts HTTP 415, `UNSUPPORTED_MEDIA_TYPE`, and zero DB mutation.

- **FR-W1-03 (Mandatory POST admission and boundary evidence):**
  - Updated race & authority failure tests in [`PublicFilmCreateIntegrationTest.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmCreateIntegrationTest.java):
    - `d3AdmissionRaceInFlightCreateDuringRevocation`: asserts exact 401 `AUTHENTICATION_REQUIRED`, `ProfileAuthFixture.domainSnapshot(jdbcTemplate)` equality before and after the request, and `verifyNoInteractions(vaultEntryOperations)`.
    - `d3AdmissionRacePauseBeforeAuthoritySnapshot`: asserts exact 401 `AUTHENTICATION_REQUIRED`, snapshot equality before and after, and `verifyNoInteractions(vaultEntryOperations)`.
    - `d3AuthorityLookupUnavailableFailsClosed`: asserts exact 401 `AUTHENTICATION_REQUIRED`, snapshot equality before and after, and `verifyNoInteractions(vaultEntryOperations)`.
  - Expanded `publicBoundaryTestingRequiredAndOptionalFields`:
    - Added normalization to null for `originalTitle: ""` and `"   "` (returns 201, saved as null).
    - Added normalization to null for `nationalityCode: ""` and `"  "` (returns 201, saved as null).
    - Added padded raw string overflow (raw length > 500 characters after padding) -> 400 `VALIDATION_ERROR`.
    - Added explicit rejection of forbidden fields passed as null or empty: `isNsfw: false`, `isNsfw: null`, `directorPersonId: null`, `genreIds: []`, `description: null` -> all rejected with 400 `MALFORMED_REQUEST`.
    - Total successful creates in the boundary test increased from 5 to 9, verified by strict database row counts.

- **FR-W1-05 (Traceability, nonexistent method citations, gate duration labels):**
  - Corrected method names in traceability documentation:
    - OpenAPI route inventory test citation: `OpenApiRouteInventoryIntegrationTest.openApiContractContainsAllRoutesAndUniqueOperationIds`.
    - Security filter chain test citations: `SecurityFilterChainIntegrationTest.permitsPublicBearerTokenOnExactB1EndpointsAndDeniesUnselected` and `SecurityFilterChainIntegrationTest.enforcesAsyncAndErrorDispatcherPriorityOnB1Routes`.
    - Corrected application service method reference to `filmOperations.create` (under `FilmOperations` interface).
    - Corrected header documentation: `Location` header is returned on 201 for `POST /api/v1/films` only for `PROFILE_PRIVATE`, and is absent for `PROFILE_PUBLIC`.
  - Recorded exact measured wall-clock durations and verified receipt files across all verification gates.

---

## 2. File Map

### Production Code (6 Authorized Files)
1. [`backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/infrastructure/security/SecurityConfiguration.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/infrastructure/security/SecurityConfiguration.java)
   - Registered `.requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/films").hasAnyAuthority("PROFILE_PUBLIC", "PROFILE_PRIVATE")` placed immediately after the eight B1 GET routes and before `.anyRequest().hasAuthority("PROFILE_PRIVATE")`.
2. [`backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/controller/FilmController.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/controller/FilmController.java)
   - Injected `FilmCreateRequestReader`. Re-implemented `create(HttpServletRequest, Authentication)` to read raw request bytes.
   - Removed `(consumes = MediaType.APPLICATION_JSON_VALUE)` on `@PostMapping`.
   - Passes `servletRequest` into `filmCreateRequestReader.readPublic(body, request)`.
   - Branches on authenticated authority:
     - If `PROFILE_PUBLIC`: invokes `FilmCreateRequestReader.readPublic(bodyBytes, servletRequest)` -> maps to `CreateFilmCommand` via `filmWebMapper.toCommand(publicRequest)` -> invokes `filmOperations.create(command)` -> returns 201 Created with 7-key `PublicFilmResponse`, `Cache-Control: no-store`, and no `Location` header.
     - If `PROFILE_PRIVATE`: invokes `FilmCreateRequestReader.readPrivate(bodyBytes, servletRequest)` -> maps to `CreateFilmCommand` via `filmWebMapper.toCommand(privateRequest)` -> invokes `filmOperations.create(command)` -> returns 201 Created with 18-key `FilmResponse`, `Location` header, and default caching headers.
   - Exception mapping returns safe error responses with `Cache-Control: no-store` on error.
3. [`backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/controller/FilmCreateRequestReader.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/controller/FilmCreateRequestReader.java)
   - Strict low-level streaming JSON parser for `PROFILE_PUBLIC`:
     - Checks `Content-Type`: missing, blank, or unsupported media type throws `HttpMediaTypeNotSupportedException` (HTTP 415).
     - Enables `JsonParser.Feature.STRICT_DUPLICATE_DETECTION`.
     - Enforces root token is `START_OBJECT`.
     - Iterates field tokens; whitelists exact 5 fields: `title`, `originalTitle`, `nationalityCode`, `format`, `productionStyle`. Any unrecognized property throws nested `MalformedPayloadException`.
     - Enforces scalar types strictly: `title`, `originalTitle`, `nationalityCode` must be `VALUE_STRING` (or `VALUE_NULL` for optional); rejects numbers and booleans with `MalformedPayloadException`.
     - Enforces strict enum mapping: `format` and `productionStyle` must be exact string match against `FilmFormat.valueOf` and `ProductionStyle.valueOf`. Rejects numeric ordinals and lowercase values with `MalformedPayloadException`.
     - Verifies `parser.nextToken() == null` after end object.
     - Runs Bean Validation using Jakarta `Validator` on the parsed `PublicCreateFilmRequest`.
   - Dynamic MVC converter delegate for `PROFILE_PRIVATE`: `readPrivate(byte[] body, HttpServletRequest servletRequest)` verifies `Content-Type` is present and valid; missing/blank throws `HttpMediaTypeNotSupportedException` (HTTP 415). Uses `SimpleHttpInputMessage(body, headers)` and delegates to configured `HttpMessageConverter` from `handlerAdapter.getMessageConverters()`, supporting vendor media types, custom charsets (ISO-8859-1), standard Bean Validation, and 415 on unsupported media types.
   - Contains nested `MalformedPayloadException`.
4. [`backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/dto/PublicCreateFilmRequest.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/dto/PublicCreateFilmRequest.java)
   - Immutable record with exact 5 fields: `title` (@NotBlank, @Size(max=500)), `originalTitle` (@Size(max=500)), `nationalityCode` (@Size(max=2)), `format` (@NotNull FilmFormat), `productionStyle` (@NotNull ProductionStyle).
   - Annotated with `@Schema(description = "Film create request for public profile with minimal fields", additionalProperties = Schema.AdditionalPropertiesValue.FALSE)`.
5. [`backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/mapper/FilmWebMapper.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/mapper/FilmWebMapper.java)
   - Added `toCommand(PublicCreateFilmRequest request)`: sets server-enforced invariants `isNsfw = false`, `directorPersonId = null`, `posterMediaId = null`, `description = null`, `personalReview = null`, and empty sets for genres, story archetypes, and world settings.
   - Added `toPublicCreateResponse(FilmView film)`: maps the committed `FilmView` into canonical 7-key `PublicFilmResponse`.
6. [`backend/src/main/java/com/vhvkhangg/personalprivatevault/OpenApiConfiguration.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/main/java/com/vhvkhangg/personalprivatevault/OpenApiConfiguration.java)
   - Registered `PublicCreateFilmRequest` schema with exact 5 properties and `additionalProperties = false`.
   - Registered `CreateFilmRequest` via reflection (`Class.forName(...)` + `ModelConverters`), preventing cyclic package dependencies in ArchUnit.
   - Registered union schema `FilmCreateRequest` (`anyOf: [CreateFilmRequest, PublicCreateFilmRequest]`).
   - Registered response union schema `ApiResponseFilmCreateResponse` (`anyOf: [ApiResponseFilmResponse, ApiResponsePublicFilmResponse]`).
   - Updated path item for `POST /api/v1/films` with requestBody referencing `FilmCreateRequest` and 201 response referencing `ApiResponseFilmCreateResponse`.
   - Updated Bearer description noting `POST /api/v1/films` is available to both `PROFILE_PUBLIC` and `PROFILE_PRIVATE`.

### Test Code (5 Authorized Files)
1. [`backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/ProfileRouteAuthorizationIntegrationTest.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/ProfileRouteAuthorizationIntegrationTest.java)
   - Updated `declaredAllowedPairs` from 8 to 9 (added `"POST /api/v1/films"`).
   - Positive invocations assert handler execution before clearing spy.
   - Captured created film IDs (`id1`, `id2`) and queried database to independently verify committed Film/Vault pairs.
   - Verified ASYNC and ERROR dispatchers on `POST /api/v1/films` return 403 Forbidden to PUBLIC.
   - Verified non-POST HTTP methods on `/api/v1/films` (PUT, DELETE, PATCH) return 403 Forbidden to PUBLIC.
2. [`backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/SecurityFilterChainIntegrationTest.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/SecurityFilterChainIntegrationTest.java)
   - Verified `POST /api/v1/films` is permitted for `PROFILE_PUBLIC`.
   - Verified unselected mutations (`POST /api/v1/fictions`, `PUT /api/v1/films/1`) return 403 Forbidden.
   - Verified ASYNC and ERROR dispatcher priority tests on `POST /api/v1/films`.
3. [`backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmCreateIntegrationTest.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmCreateIntegrationTest.java)
   - 30 comprehensive tests covering W1-01 through W1-16:
     1. `publicTokenFromProfilesLoginCreatesFilmWithExactSevenKeysAndNoStore`
     2. `publicTokenFromDowngradeCreatesFilmWithExactSevenKeysAndNoStore`
     3. `anonymousRequestToCreateFilmReturns401`
     4. `strictParserRejectsDuplicateKeys`
     5. `strictParserRejectsUnicodeEscapeDuplicateKeys`
     6. `strictParserRejectsTrailingTokens`
     7. `strictParserRejectsNonObjectRoot`
     8. `strictParserRejectsUnknownAndForbiddenProperties`
     9. `strictParserRejectsScalarCoercion`
     10. `strictParserRejectsEnumOrdinalsAndInvalidEnums`
     11. `beanValidationRejectsBlankTitleAndLengthViolations`
     12. `domainValidationRejectsInvalidNationalityCode`
     13. `filmCreationProvesAtomicityAndDatabaseIntegrity`
     14. `concurrencyObserverBarrierDuringUncommittedState`
     15. `sqlInsertionFailureRollsBackCompletely`
     16. `deferredCommitFailureRollsBackCompletely`
     17. `followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys`
     18. `d3AdmissionRaceInFlightCreateDuringElevation`
     19. `privateProfileCreateRetainsFullEighteenKeyBehavior`
     20. `d3AdmissionRaceInFlightCreateDuringRevocation`
     21. `d3AdmissionRacePauseBeforeAuthoritySnapshot`
     22. `d3AuthorityLookupUnavailableFailsClosed`
     23. `publicBoundaryTestingRequiredAndOptionalFields`
     24. `privateVendorJsonContentTypeAccepted`
     25. `privateIso88591CharsetDecodedCorrectly`
     26. `privateUnknownKeyRejectedWith400`
     27. `privateUnsupportedMediaTypeReturns415`
     28. `publicMissingContentTypeReturns415`
     29. `publicTextPlainContentTypeReturns415`
     30. `privateMissingContentTypeReturns415`
4. [`backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmReadIntegrationTest.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmReadIntegrationTest.java)
   - Updated `writesNestedReadsAndCreditDenyPublic` to reflect `POST /api/v1/films` is authorized for PUBLIC, while unselected writes continue to return 403 ACCESS_DENIED.
5. [`backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java)
   - Manifest row for `POST /api/v1/films` verified against `FilmCreateRequest | ApiResponseFilmCreateResponse`.
   - Structural assertions verify `CreateFilmRequest`, `PublicCreateFilmRequest`, `FilmCreateRequest`, and `ApiResponseFilmCreateResponse`.
   - Positive/negative specimen validations with `maxLength` / `minLength` boundary checks.
   - Throws `AssertionError` on unresolved `$ref`.

---

## 3. Verified Acceptance Criteria Matrix (W1-01 through W1-16)

| Req ID | Canonical Description (from review plan) | Verification Method & Actual Test Method | Result |
|---|---|---|---|
| **W1-01** | Exact `POST /api/v1/films` selected for PUBLIC only after real persisted login and downgrade-origin tokens, without anonymous grant. | `PublicFilmCreateIntegrationTest.publicTokenFromProfilesLoginCreatesFilmWithExactSevenKeysAndNoStore`, `publicTokenFromDowngradeCreatesFilmWithExactSevenKeysAndNoStore`, `anonymousRequestToCreateFilmReturns401`, `ProfileRouteAuthorizationIntegrationTest.publicAuthEndpointsAccessible` | **VERIFIED** |
| **W1-02** | PUBLIC request accepts exactly the Owner-approved keys/validations, rejects extra fields and mass-assignment attempts; omitting links/person/genre fields cannot silently attach private relationships. | `PublicFilmCreateIntegrationTest.strictParserRejectsUnknownAndForbiddenProperties`, `publicBoundaryTestingRequiredAndOptionalFields`, `filmCreationProvesAtomicityAndDatabaseIntegrity` | **VERIFIED** |
| **W1-03** | PUBLIC `isNsfw` cannot become true, even through unknown properties, alternate serialization, invalid enum, malformed JSON or duplicate property ambiguity. | `PublicFilmCreateIntegrationTest.strictParserRejectsUnknownAndForbiddenProperties`, `strictParserRejectsDuplicateKeys`, `strictParserRejectsUnicodeEscapeDuplicateKeys`, `strictParserRejectsScalarCoercion`, `strictParserRejectsEnumOrdinalsAndInvalidEnums`, `filmCreationProvesAtomicityAndDatabaseIntegrity` | **VERIFIED** |
| **W1-04** | Successful 201 creates exactly one Film and one correct-type active Vault Entry in one committed atomic unit; any failure rolls back both and joins; no orphan created. | `PublicFilmCreateIntegrationTest.filmCreationProvesAtomicityAndDatabaseIntegrity`, `sqlInsertionFailureRollsBackCompletely`, `deferredCommitFailureRollsBackCompletely` | **VERIFIED** |
| **W1-05** | Success body has exactly the seven B1 PublicFilmResponse data keys; never full FilmResponse or hidden values even as null, preserving canonical `ApiResponse`. | `PublicFilmCreateIntegrationTest.publicTokenFromProfilesLoginCreatesFilmWithExactSevenKeysAndNoStore`, `publicTokenFromDowngradeCreatesFilmWithExactSevenKeysAndNoStore`, `publicBoundaryTestingRequiredAndOptionalFields` | **VERIFIED** |
| **W1-06** | Invalid/missing required fields, bad enum, length overflow and unsupported options return safe errors without writes or private metadata. | `PublicFilmCreateIntegrationTest.beanValidationRejectsBlankTitleAndLengthViolations`, `domainValidationRejectsInvalidNationalityCode`, `strictParserRejectsNonObjectRoot`, `strictParserRejectsTrailingTokens`, `publicBoundaryTestingRequiredAndOptionalFields`, `publicMissingContentTypeReturns415`, `publicTextPlainContentTypeReturns415` | **VERIFIED** |
| **W1-07** | Existing PRIVATE `POST /api/v1/films` request/response/validation and all PRIVATE update/credit/link/genre flows remain byte/semantic compatible. | `PublicFilmCreateIntegrationTest.privateProfileCreateRetainsFullEighteenKeyBehavior`, `privateVendorJsonContentTypeAccepted`, `privateIso88591CharsetDecodedCorrectly`, `privateUnknownKeyRejectedWith400`, `privateUnsupportedMediaTypeReturns415`, `privateMissingContentTypeReturns415`, Gate G-W1-PRIVATE (74 tests) | **VERIFIED** |
| **W1-08** | PUBLIC Film PUT/update, NSFW mutation, all Fiction writes, credits, links, classifications, genre and Vault mutators remain 403 before handler or DB work, even for guessed IDs. | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`, `directAndNestedFilmCreditDeniedToPublicWithPopulatedParent`, `SecurityFilterChainIntegrationTest.unselectedMutationsReturn403Forbidden`, `PublicFilmReadIntegrationTest.writesNestedReadsAndCreditDenyPublic` | **VERIFIED** |
| **W1-09** | PUBLIC direct detail GET for newly created Film obeys B1 D10 non-NSFW + active-correct-type snapshot; no source type/status mismatch. | `PublicFilmCreateIntegrationTest.followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys`, `ProfileRouteAuthorizationIntegrationTest.sessionLifecycleAndMutationOnNewlyAccessibleDetailRoutes`, Gate G-W1-READ (29 tests) | **VERIFIED** |
| **W1-10** | Fail-closed if persisted profile authority, database transaction or authorization context is unavailable; no cached grant/partial write. | `PublicFilmCreateIntegrationTest.d3AuthorityLookupUnavailableFailsClosed`, `d3AdmissionRacePauseBeforeAuthoritySnapshot`, `sqlInsertionFailureRollsBackCompletely`, `deferredCommitFailureRollsBackCompletely` | **VERIFIED** |
| **W1-11** | CSRF/HTTP method/path variants, HEAD, OPTIONS, unknown, suffix and ASYNC/ERROR dispatch cannot bypass the exact route-level B2 grant or B1 restrictions. | `ProfileRouteAuthorizationIntegrationTest.methodAndPathVariantsOnPublicRoutesDenyNonGetAndSubpaths`, `sameUriAsyncAndErrorDispatcherDeniesPublicSession`, `actualAsyncRedispatchRevalidatesPropagatedContextWithoutBearer`, `publicProfileDeniedOnUnknownRoutes`, `SecurityFilterChainIntegrationTest.enforcesAsyncAndErrorDispatcherPriorityOnB1Routes` | **VERIFIED** |
| **W1-12** | Two real profiles/origins, revoke/downgrade/refresh race tests preserve DP-A admission and the already-admitted boundary; no silent token privilege escalation. | `PublicFilmCreateIntegrationTest.d3AdmissionRaceInFlightCreateDuringElevation`, `d3AdmissionRaceInFlightCreateDuringRevocation`, `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins` | **VERIFIED** |
| **W1-13** | No error, header, Location, response cache, ETag or log leaks restricted fields or raw tokens; safe cache-control contract explicit for Public response. | `PublicFilmCreateIntegrationTest.publicTokenFromProfilesLoginCreatesFilmWithExactSevenKeysAndNoStore`, `anonymousRequestToCreateFilmReturns401`, `publicBoundaryTestingRequiredAndOptionalFields`, `privateProfileCreateRetainsFullEighteenKeyBehavior` | **VERIFIED** |
| **W1-14** | Generated OpenAPI correctly models profile-specific POST request/response variants, authentication, codes, and preserves existing operation IDs and PRIVATE full schemas. | `OpenApiRouteInventoryIntegrationTest.openApiContractContainsAllRoutesAndUniqueOperationIds`, `ProfileRouteAuthorizationIntegrationTest.dpAOpenApiSchemasAndErrorsMatchActualContract`, Gate G-W1-API (1 test, 239 endpoints) | **VERIFIED** |
| **W1-15** | Architecture/Modulith tests show only permitted feature/Vault dependencies and no new migration, index, module edge, global transaction manager or new test framework. | Package structure, zero new migrations, ArchUnit rules; Gate G-W1-ARCH (`ApplicationArchitectureTests`, 38 tests) | **VERIFIED** |
| **W1-16** | Exact registered-route deny-sweep membership, focused source-qualified tests, bounded receipts/counters, independent code review and Docs Sync with original DP-A/B1 evidence preserved. | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`, `sessionLifecycleAndMutationOnNewlyAccessibleDetailRoutes`, all 6 gates passing (199 tests total) | **VERIFIED** |

---

## 4. Verification Execution and Receipts (Remediation Round 2)

All 6 gates passed cleanly under bounded execution:

| Gate | Selector | Outcome | Duration | Exit Code | Tests | Receipt |
|---|---|---|---|---|---|---|
| **G-W1-CREATE** | `PublicFilmCreateIntegrationTest` | PASS | 147.609s | 0 | 30 | [`logs/bf26-dp-b2-w1/remediation-round2/G-W1-CREATE-retry1.json`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/logs/bf26-dp-b2-w1/remediation-round2/G-W1-CREATE-retry1.json) |
| **G-W1-SEC** | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | PASS | 124.484s | 0 | 27 | [`logs/bf26-dp-b2-w1/remediation-round2/G-W1-SEC.json`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/logs/bf26-dp-b2-w1/remediation-round2/G-W1-SEC.json) |
| **G-W1-PRIVATE** | `FilmWebIntegrationTest,FilmIntegrationTest,FilmValidationTest,FilmCreditIntegrationTest,FilmLinkIntegrationTest,FilmGenreIntegrationTest` | PASS | 62.687s | 0 | 74 | [`logs/bf26-dp-b2-w1/remediation-round2/G-W1-PRIVATE.json`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/logs/bf26-dp-b2-w1/remediation-round2/G-W1-PRIVATE.json) |
| **G-W1-READ** | `PublicFilmReadIntegrationTest,PublicFictionReadIntegrationTest,ReferenceWebIntegrationTest` | PASS | 101.282s | 0 | 29 | [`logs/bf26-dp-b2-w1/remediation-round2/G-W1-READ.json`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/logs/bf26-dp-b2-w1/remediation-round2/G-W1-READ.json) |
| **G-W1-API** | `OpenApiRouteInventoryIntegrationTest` | PASS | 62.203s | 0 | 1 | [`logs/bf26-dp-b2-w1/remediation-round2/G-W1-API.json`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/logs/bf26-dp-b2-w1/remediation-round2/G-W1-API.json) |
| **G-W1-ARCH** | `ApplicationArchitectureTests` | PASS | 53.734s | 0 | 38 | [`logs/bf26-dp-b2-w1/remediation-round2/G-W1-ARCH.json`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/logs/bf26-dp-b2-w1/remediation-round2/G-W1-ARCH.json) |

- **Total Verification Count:** **199 tests** across 6 gates, 0 failures, 0 errors, 0 skipped.
- Surefire XML artifacts preserved in subdirectories `G-W1-CREATE-surefire/`, `G-W1-SEC-surefire/`, `G-W1-PRIVATE-surefire/`, `G-W1-READ-surefire/`, `G-W1-API-surefire/`, `G-W1-ARCH-surefire/`.
- Manifests captured: [`test-case-manifest.json`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/logs/bf26-dp-b2-w1/remediation-round2/test-case-manifest.json) (199 entries) and [`final-source-sha256.json`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/logs/bf26-dp-b2-w1/remediation-round2/final-source-sha256.json) (11 source/test files).
- Exported OpenAPI specification: [`openapi-spec.json`](file:///C:/Users/ADMIN/IdeaProjects/personal-private-vault/logs/bf26-dp-b2-w1/remediation-round2/openapi-spec.json) (310 schemas, zero dangling references).

---

## 5. Next Step
The handoff is submitted for **independent Codex final re-review round 2** (`$codex-final-review`).
