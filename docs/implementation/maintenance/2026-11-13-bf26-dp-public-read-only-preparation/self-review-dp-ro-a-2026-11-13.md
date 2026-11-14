# Antigravity Self-Review — BF26-DP-RO-A: Public Write Revocation (Remediation Round 1)

**Date:** 2026-11-14  
**Handoff ID:** `BF26-DP-RO-A`  
**Implementer:** Antigravity (backend-implementer)  
**Remediation Round:** 1 of 3 consumed  
**Review Target:** Remediation of Codex findings FR-ROA-01, FR-ROA-02, FR-ROA-03, and DPROA-L01 from [independent final review](reviews/2026-11-14-bf26-dp-ro-a-independent-final-review.md).

---

### 1. Remediation Verification & Requirement Trace (ROA-01 – ROA-12)

Every test method cited below is verified to exist in source and executed in surefire XML reports under `logs/bf26-dp-ro-a/remediation-round1/surefire-xml/`:

- **ROA-01 (Direct PUBLIC POST denied 403 / no-handler / no-mutation):**
  - Verified in `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`: handlerAdapter invocation history cleared prior to PRIVATE POST `/api/v1/films`; observed `HandlerMethod` specifically qualified as `FilmController.create`; cleared before testing formerly valid 5-field POST under direct PUBLIC token; verified 0 `handlerAdapter.handle` calls and identical domain row snapshot.
  - Verified in `PublicFilmCreateIntegrationTest.publicTokenFromProfilesLoginDeniedWith403AndNoMutation`: formerly valid 5-field body returns 403 `ACCESS_DENIED`, verifies `handlerAdapter.handle` never called, `vaultEntryOperations.create` never called, and domain row snapshot unchanged.
  - Verified in `SecurityFilterChainIntegrationTest.permitsPublicBearerTokenOnExactB1EndpointsAndDeniesUnselected`: revoked POST `/api/v1/films` returns 403 `ACCESS_DENIED`.
  - *Status:* **PASS / VERIFIED** (FR-ROA-01 addressed).
- **ROA-02 (Downgraded PUBLIC POST denied 403 / no-handler / no-mutation):**
  - Verified in `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`: downgraded PUBLIC token tested with formerly valid 5-field body; returns 403 `ACCESS_DENIED`, 0 `handlerAdapter.handle` calls, and identical domain snapshot.
  - Verified in `PublicFilmCreateIntegrationTest.publicTokenFromDowngradeDeniedWith403AndNoMutation`: live allowed GET `/api/v1/reference/countries` returns 200; Film POST returns 403 `ACCESS_DENIED`, zero handlerAdapter calls, zero vaultEntryOperations calls, and unchanged domain snapshot.
  - *Status:* **PASS / VERIFIED** (FR-ROA-01 addressed).
- **ROA-03 (Broad fields, malformed JSON, media/Content-Type variations denied 403 before parsing):**
  - Verified in `PublicFilmCreateIntegrationTest.publicPayloadVariantsDeniedWith403BeforeParsingOrValidation` and `publicMissingOrTextPlainContentTypeDeniedWith403`. All return 403 before parsing/business logic, with unchanged domain snapshots.
  - *Status:* **PASS / VERIFIED**.
- **ROA-04 (Anonymous POST returns 401 AUTHENTICATION_REQUIRED):**
  - Verified in `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`: anonymous formerly valid 5-field POST returns 401 `AUTHENTICATION_REQUIRED`, zero handlerAdapter calls, and unchanged domain snapshot.
  - Verified in `PublicFilmCreateIntegrationTest.anonymousRequestToCreateFilmReturns401`: returns 401, zero handlerAdapter calls, zero vaultEntryOperations calls, and unchanged domain snapshot.
  - Verified in `SecurityFilterChainIntegrationTest.rejectsMissingBearerToken`.
  - *Status:* **PASS / VERIFIED** (FR-ROA-01 addressed).
- **ROA-05 (PRIVATE valid POST succeeds with 201, 18 keys, no Location header, committed rows):**
  - Verified in `PublicFilmCreateIntegrationTest.privateProfileCreateRetainsFullEighteenKeyBehavior`: 201 Created, exact 18 keys, standard headers (no Location, not no-store), committed rows in `films` and `vault_entries`, and `handlerAdapter.handle` specifically verified to invoke `FilmController.create`.
  - Verified in `FilmWebIntegrationTest.filmLifecycleOverHttp`.
  - *Status:* **PASS / VERIFIED**.
- **ROA-06 (PRIVATE media/charset/validation and domain rejection regressions):**
  - Verified in `PublicFilmCreateIntegrationTest.privateVendorJsonContentTypeAccepted` (application/vnd.ppv+json returns 201).
  - Verified in `PublicFilmCreateIntegrationTest.privateIso88591CharsetDecodedCorrectly` (ISO-8859-1 returns 201).
  - Verified in `PublicFilmCreateIntegrationTest.privateUnknownKeyRejectedWith400` (returns 400 `MALFORMED_REQUEST`).
  - Verified in `PublicFilmCreateIntegrationTest.privateMissingContentTypeReturns415` (returns 415 `UNSUPPORTED_MEDIA_TYPE`).
  - Verified in `PublicFilmCreateIntegrationTest.privateUnsupportedMediaTypeReturns415` (text/plain returns 415).
  - **NEW (FR-ROA-02):** `PublicFilmCreateIntegrationTest.privateStructurallyInvalidRequestFailsBeanValidationWith400`: blank title under PRIVATE token returns 400 `VALIDATION_ERROR`, safe message "Request validation failed", empty data, and unchanged domain snapshot.
  - **NEW (FR-ROA-02):** `PublicFilmCreateIntegrationTest.privateDomainValidationFailureReturns422FilmInvalid`: nonexistent nationality code "ZZ" under PRIVATE token returns 422 `FILM_INVALID`, safe message "Invalid film data", empty data, and unchanged domain snapshot.
  - *Status:* **PASS / VERIFIED** (FR-ROA-02 addressed).
- **ROA-07 (Registered business-route sweep under both PUBLIC origins):**
  - Verified in `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`: exact 8 allowed GET pairs discovered; all other registered business endpoints denied 403 before handler; complete domain snapshot unchanged. Assertion message updated from 9-pair to 8-pair (DPROA-L01 addressed).
  - *Status:* **PASS / VERIFIED**.
- **ROA-08 (Method/path variants, ASYNC/ERROR dispatchers, Auth preservation):**
  - Verified in `ProfileRouteAuthorizationIntegrationTest.methodAndPathVariantsOnPublicRoutesDenyNonGetAndSubpaths`, `sameUriAsyncAndErrorDispatcherDeniesPublicSession`, `actualAsyncRedispatchRevalidatesPropagatedContextWithoutBearer`, `errorAndAsyncDispatcherRequiresPrivate`.
  - Verified in `PublicFilmCreateIntegrationTest.d3AdmissionRaceInFlightCreateDuringRevocation`, `d3AdmissionRacePauseBeforeAuthoritySnapshot`, `d3AuthorityLookupUnavailableFailsClosed`.
  - *Status:* **PASS / VERIFIED**.
- **ROA-09 (Generated OpenAPI PRIVATE-only POST):**
  - Verified in `OpenApiRouteInventoryIntegrationTest.openApiContractContainsAllRoutesAndUniqueOperationIds`: POST `/api/v1/films` references `CreateFilmRequest` and `ApiResponseFilmResponse`; bearer description omits POST films; no obsolete Public create schemas exist.
  - *Status:* **PASS / VERIFIED**.
- **ROA-10 (B1 GETs unchanged):**
  - Verified in `PublicFictionReadIntegrationTest` (13 tests), `PublicFilmReadIntegrationTest` (13 tests), `ReferenceWebIntegrationTest` (3 tests).
  - Verified in `PublicFilmCreateIntegrationTest.followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys`.
  - *Status:* **PASS / VERIFIED**.
- **ROA-11 (PRIVATE transaction rollback/atomicity controls and Modulith architecture):**
  - Verified in `PublicFilmCreateIntegrationTest.sqlInsertionFailureRollsBackCompletely`, `deferredCommitFailureRollsBackCompletely`, `filmCreationProvesAtomicityAndDatabaseIntegrity`.
  - Verified in `ApplicationArchitectureTests` (38/38 rules PASS).
  - *Status:* **PASS / VERIFIED**.
- **ROA-12 (Artifacts, manifests, accurate mapping):**
  - Handback and self-review updated with actual method names, surefire XML inventory, fresh SHA-256 hashes, and truthful verification status (FR-ROA-03 addressed).
  - *Status:* **PASS / VERIFIED**.

---

### 2. Scope & Minimal Delta Check

- Exactly 8 files in working tree diff (3 production, 5 tests). No unapproved files modified.
- Production delta: 0 lines added or changed in remediation round 1 (production code was already accepted by Codex).
- Test delta in remediation round 1:
  - `ProfileRouteAuthorizationIntegrationTest.java`: added explicit `FilmController.create` observation capture via `ArgumentCaptor`, added formerly valid 5-field body POST assertions (direct PUBLIC, downgraded PUBLIC, anonymous), and corrected assertion message to "8-pair".
  - `PublicFilmCreateIntegrationTest.java`: added `handlerAdapter` spy and assertions to verify `FilmController.create` on PRIVATE success and verify `handlerAdapter` is never called on direct PUBLIC, downgraded PUBLIC, or anonymous requests. Added two representative PRIVATE negative tests (`privateStructurallyInvalidRequestFailsBeanValidationWith400` and `privateDomainValidationFailureReturns422FilmInvalid`).

---

### 3. Test Verification Summary (Finite Gates)

All 5 finite gates executed with bounded runner (`scripts/run-bounded-command.py`, normal profile <= 300s):

| Gate | Selector | Tests Run | Result | Duration | Receipt |
| --- | --- | --- | --- | --- | --- |
| `G-ROA-SECURITY` | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | 27 (15 + 12) | **PASS (exit 0)** | 197.45s | `logs/bf26-dp-ro-a/remediation-round1/G-ROA-SECURITY.json` |
| `G-ROA-PRIVATE` | `PublicFilmCreateIntegrationTest,FilmWebIntegrationTest` | 22 (21 + 1) | **PASS (exit 0)** | 92.70s | `logs/bf26-dp-ro-a/remediation-round1/G-ROA-PRIVATE.json` |
| `G-ROA-API` | `OpenApiRouteInventoryIntegrationTest` | 1 | **PASS (exit 0)** | 66.19s | `logs/bf26-dp-ro-a/remediation-round1/G-ROA-API.json` |
| `G-ROA-READ` | `PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest,ReferenceWebIntegrationTest` | 29 (13 + 13 + 3) | **PASS (exit 0)** | 108.27s | `logs/bf26-dp-ro-a/remediation-round1/G-ROA-READ.json` |
| `G-ROA-ARCH` | `ApplicationArchitectureTests` | 38 | **PASS (exit 0)** | 56.72s | `logs/bf26-dp-ro-a/remediation-round1/G-ROA-ARCH.json` |
| **Total** | **9 test classes across 5 gates** | **117 / 117** | **PASS (exit 0)** | **521.33s** | **0 failures, 0 errors, 0 skipped** |

---

### 4. Remediation Counters & State Alignment

- Slice ID: `BF26-DP-RO-A`
- Remediation Round: **1/3 consumed** (0 remaining before round 1; 2 remaining after round 1)
- Takeover: **NOT_AUTHORIZED / NOT_USED**
- RO-B: **DEFERRED / NOT_AUTHORIZED**
- Predecessors preserved: W1 (2/3 STOPPED), B1 (3/3 STOPPED), DP-A (3/3 exhausted / takeover 1/1 accepted).
- Status transition: `CHANGES_REQUESTED` -> `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

---

### 5. Docs Sync Check

- **UPDATED:** `docs/implementation/handoffs/ACTIVE.md`, `executable-handoff-dp-ro-a-2026-11-13.md`, `self-review-dp-ro-a-2026-11-13.md`, `implementation-handback-dp-ro-a-2026-11-13.md`.
- **ALREADY_CONSISTENT:** All 29 Flyway migrations, frozen DBML, ADRs, Spring Modulith descriptors, provider defaults.
- **DEFERRED_OUT_OF_SCOPE:** Root navigation, `README.md`, `docs/roadmap.md`, `AGENTS.md` historical headers (governed by ChatGPT/Owner post-acceptance).
- **NOT_VERIFIED:** Live production deployment or cutover.

---

### 6. Self-Review Summary to Codex

1. **FR-ROA-01 resolved:** `ProfileRouteAuthorizationIntegrationTest` and `PublicFilmCreateIntegrationTest` specifically isolate and verify `FilmController.create` HandlerMethod invocation on PRIVATE creation via `ArgumentCaptor`, and assert 0 `handlerAdapter.handle` invocations on direct PUBLIC, downgraded PUBLIC, and anonymous formerly valid body requests.
2. **FR-ROA-02 resolved:** `PublicFilmCreateIntegrationTest` includes representative PRIVATE negative cases for Bean Validation (`privateStructurallyInvalidRequestFailsBeanValidationWith400` -> 400 `VALIDATION_ERROR`) and Film domain validation (`privateDomainValidationFailureReturns422FilmInvalid` -> 422 `FILM_INVALID`), both verifying safe error envelopes and unchanged domain snapshots.
3. **FR-ROA-03 resolved:** Handback and self-review use exact class, method, and XML testcase names. All 117 testcases across 9 classes reconcile cleanly with zero failures/errors/skips.
4. **DPROA-L01 addressed:** Accurately reported as 9 test classes across 5 execution gates; updated T1 assertion message from 9-pair to 8-pair; 8-file wording maintained.
5. All 5 finite gates passed (117/117 PASS, exit code 0, duration well under 300s each).
6. Ready for independent Codex `$codex-final-review` (Remediation Round 1).
