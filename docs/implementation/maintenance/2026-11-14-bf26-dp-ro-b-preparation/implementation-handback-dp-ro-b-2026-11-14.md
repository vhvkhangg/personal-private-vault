# Implementation Handback — BF26-DP-RO-B: PUBLIC Full Fiction/Film Detail

- **Handoff ID:** `BF26-DP-RO-B`
- **Date:** 2026-11-14
- **Author / Implementer:** Antigravity (backend-implementer)
- **Status:** **`IMPLEMENTED_AWAITING_CODEX_REVIEW`**
- **Authority:** Owner's 2026-11-14 authorization for BF26-DP-RO-B ONLY ([executable handoff](executable-handoff-dp-ro-b-2026-11-14.md) and [ACTIVE](../../handoffs/ACTIVE.md)).
- **Remediation Counters:** Remediation **0/3 consumed** (initial implementation); Takeover **NOT_AUTHORIZED / NOT_USED**; RO-A **OWNER_PUBLISHED / CLOSED** (`0956b4962f529050dddf88dc7f2ec19ac571e0d0`).

---

## 1. Implementation Overview

This handback reports the completion of `BF26-DP-RO-B` within the exact 18-file boundary (13 production targets P01–P13, 5 test targets T01–T05):

1. **PUBLIC 19-Field Response Enrichment:**
   - Authenticated `GET /api/v1/fictions/{id}` returns exactly 19 fields: 18 original full Fiction fields plus `authorName` (string if eligible linked Person/CreatorGroup, or explicit JSON `null`).
   - Authenticated `GET /api/v1/films/{id}` returns exactly 19 fields: 18 original full Film fields plus `directorName` (string if eligible linked Person, or explicit JSON `null`).
   - Both response DTOs (`PublicFictionDetailResponse`, `PublicFilmDetailResponse`) include `@JsonInclude(JsonInclude.Include.ALWAYS)` so `null` scalar names are explicitly emitted in JSON.
2. **Eligibility & Safe Error Semantics:**
   - Work exists, is non-NSFW, and has an active same-ID `FICTION`/`FILM` Vault entry **before** any linked-name lookup occurs.
   - Hidden, missing, NSFW, trashed, or type-mismatched works return identical safe 404 (`FICTION_NOT_FOUND` / `FILM_NOT_FOUND`) with `Cache-Control: no-store` and `data: null`, with zero calls to People or CreatorGroup operations.
   - For a Person reference, the referenced Person must have its own active same-ID `PERSON` Vault entry before resolving the name. CreatorGroup is not Vault-backed and resolves name via `creatorGroupOperations.find`.
   - Missing/ineligible/blank linked names yield 200 OK with `authorName: null` / `directorName: null`.
   - Genuine query/completion failures yield safe 500 (`INTERNAL_ERROR`, "An unexpected error occurred") with `Cache-Control: no-store` and `data: null`.
3. **Physical Snapshot Isolation:**
   - Both features execute via `@Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)`.
   - Complete work values, defensively copied classification sets, Person Vault qualification, and scalar name are captured within the single owning transaction.
   - HTTP DTO mapping is pure in-memory transformation after transaction completion with zero post-return queries.
4. **Preserved Invariants:**
   - PRIVATE Fiction/Film GET/POST/PUT remain exactly 18 fields without names; PRIVATE DTOs and web controllers are unchanged.
   - PUBLIC Film POST remains revoked with 403 `ACCESS_DENIED` before handler; anonymous protected requests return 401.
   - Exactly eight PUBLIC business GET method/path pairs are permitted; all other business/data routes return 403 before handler.
   - `SecurityConfiguration.java` is untouched; OpenAPI schemas for Fiction and Film reflect exact 19-field PUBLIC and 18-field PRIVATE components using `anyOf`.

---

## 2. Eighteen-File Source & Test Manifest with Final SHA-256 Hashes

| ID | File Path | Status | Final SHA-256 |
| --- | --- | --- | --- |
| `P01` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/fiction/FictionOperations.java` | Modified | `82afa7bd49695657a3f374755b1b143a13e2e1bb667af638a4a5617a6e16d613` |
| `P02` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/view/PublicFictionDetailView.java` | **NEW** | `6af10cb075899f84b8530c6439296189b700c3da980f660623a93f53c9b9624d` |
| `P03` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionService.java` | Modified | `05b2a80c46d7129197c1683c26e1b11ede353138353a9e4ae5e059a24a0b331f` |
| `P04` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/web/controller/FictionController.java` | Modified | `c8c866d01ba41293639db1e2ed397b0c29511ef21b1bf0b296c4172de3a1d8ca` |
| `P05` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/web/mapper/FictionWebMapper.java` | Modified | `edca7befd8252a99269216ef330a63062699a9a186df6ee9128efb08cbc7c38c` |
| `P06` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/web/dto/PublicFictionDetailResponse.java` | **NEW** | `b45ac42db61a09242f79597435ceca98ceed6db11feff860f1cdbbe24f618c7a` |
| `P07` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/film/FilmOperations.java` | Modified | `43e49bcd5ee158885249ff73e3db9b383edf652cab0d4ea6aca9054125ed611f` |
| `P08` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/view/PublicFilmDetailView.java` | **NEW** | `fd5cf5e0f5205dfc39881dbbe64f034de5907d7680e2211b9711451200c29a9f` |
| `P09` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmService.java` | Modified | `f9cbb9a366a496c5fe4dcc3469db74fc137df99ff29476ba0a39a3528f82a8fd` |
| `P10` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/controller/FilmController.java` | Modified | `36dd727b155c7102fda1f038aa4e89992f36770536b9219a04c7b4690daadffd` |
| `P11` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/mapper/FilmWebMapper.java` | Modified | `85fd9cb763e69b57817d7b77a57b827c683981af3efddd73d5b2e9915ca170dc` |
| `P12` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/dto/PublicFilmDetailResponse.java` | **NEW** | `9ecd2a6ceaab920591dfaaef7b8ace6d07081dc5afe629a7667be360d9d22b99` |
| `P13` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/OpenApiConfiguration.java` | Modified | `daf7346a8c27355ef9a863e6847699e388e18812d9e87479fb1468696a6696f4` |
| `T01` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/fiction/PublicFictionReadIntegrationTest.java` | Modified | `e968059d0231dfb22655db2b7da0afd8a48297923f9181e5f34dd40f8231bfb7` |
| `T02` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmReadIntegrationTest.java` | Modified | `5bd2b47788d822d48ce0a99f9086b3333e22d436ec5c72dedd6c37925e78a3ef` |
| `T03` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/ProfileRouteAuthorizationIntegrationTest.java` | Modified | `30bdfc5e942def5cd3a28dd8e5e6326a09b6929b304a01d3b3714da49fb3c395` |
| `T04` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` | Modified | `936283d3c649d6a75ad32a079702b9265296ccf31cbe5438291f4b6b74f5ee23` |
| `T05` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmCreateIntegrationTest.java` | Modified | `34557d898c3f2db2261ff7563629f4ad6b8417ee3832b4ea2bf18e077f8a37e0` |

---

## 3. Requirement-to-Evidence Trace (ROB-01 – ROB-22)

| Criteria | Actual Test Class & Method Name | XML Testcase Identity | Result |
| --- | --- | --- | --- |
| **ROB-01** | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`, `OpenApiRouteInventoryIntegrationTest.openApiContractContainsAllRoutesAndUniqueOperationIds` | `registeredRoutesDeniedToPublicTokenFromBothOrigins`, `openApiContractContainsAllRoutesAndUniqueOperationIds` | **PASS** |
| **ROB-02** | `PublicFictionReadIntegrationTest.publicSessionsReachFictionDetail`, `PublicFilmReadIntegrationTest.publicSessionsReachFilmDetail`, `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins` | `publicSessionsReachFictionDetail`, `publicSessionsReachFilmDetail`, `registeredRoutesDeniedToPublicTokenFromBothOrigins` | **PASS** |
| **ROB-03** | `PublicFictionReadIntegrationTest.eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor` | `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor` | **PASS** |
| **ROB-04** | `PublicFilmReadIntegrationTest.eligibleFilmHasExactlyNineteenKeysAndResolvedDirector` | `eligibleFilmHasExactlyNineteenKeysAndResolvedDirector` | **PASS** |
| **ROB-05** | `PublicFictionReadIntegrationTest.privateFictionReadRetainsEighteenKeysWithoutAuthorName`, `PublicFilmReadIntegrationTest.privateFilmReadRetainsEighteenKeysWithoutDirectorName`, `FictionWebIntegrationTest.fictionLifecycleOverHttp`, `FilmWebIntegrationTest.filmLifecycleOverHttp` | `privateFictionReadRetainsEighteenKeysWithoutAuthorName`, `privateFilmReadRetainsEighteenKeysWithoutDirectorName`, `fictionLifecycleOverHttp`, `filmLifecycleOverHttp` | **PASS** |
| **ROB-06** | `PublicFictionReadIntegrationTest.eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor`, `PublicFictionReadIntegrationTest.qualifiesPersonVaultBeforeResolvingName` | `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor`, `qualifiesPersonVaultBeforeResolvingName` | **PASS** |
| **ROB-07** | `PublicFictionReadIntegrationTest.eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor`, `PublicFictionReadIntegrationTest.referenceAbsenceYieldsPresentNullName` | `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor`, `referenceAbsenceYieldsPresentNullName` | **PASS** |
| **ROB-08** | `PublicFilmReadIntegrationTest.eligibleFilmHasExactlyNineteenKeysAndResolvedDirector` | `eligibleFilmHasExactlyNineteenKeysAndResolvedDirector` | **PASS** |
| **ROB-09** | `PublicFictionReadIntegrationTest.qualifiesPersonVaultBeforeResolvingName`, `PublicFictionReadIntegrationTest.referenceAbsenceYieldsPresentNullName`, `PublicFilmReadIntegrationTest.qualifiesDirectorPersonVaultBeforeResolvingName`, `PublicFilmReadIntegrationTest.referenceAbsenceYieldsPresentNullName` | `qualifiesPersonVaultBeforeResolvingName`, `referenceAbsenceYieldsPresentNullName`, `qualifiesDirectorPersonVaultBeforeResolvingName` | **PASS** |
| **ROB-10** | `PublicFictionReadIntegrationTest.ineligibleFictionReturnsFourHundredFourAndZeroPeopleAccess`, `PublicFilmReadIntegrationTest.ineligibleFilmReturnsFourHundredFourAndZeroPeopleAccess` | `ineligibleFictionReturnsFourHundredFourAndZeroPeopleAccess`, `ineligibleFilmReturnsFourHundredFourAndZeroPeopleAccess` | **PASS** |
| **ROB-11** | `PublicFictionReadIntegrationTest.operationalAndCompletionFailuresYieldFiveHundredSafeEnvelope`, `PublicFilmReadIntegrationTest.operationalAndCompletionFailuresYieldFiveHundredSafeEnvelope` | `operationalAndCompletionFailuresYieldFiveHundredSafeEnvelope` | **PASS** |
| **ROB-12** | `PublicFictionReadIntegrationTest.s1PhysicalNewPathIsolationAndContextResumption`, `PublicFictionReadIntegrationTest.s6ImmutableMaterializationAndNoPostReturnQueries`, `PublicFilmReadIntegrationTest.s1PhysicalNewPathIsolationAndContextResumption`, `PublicFilmReadIntegrationTest.s6ImmutableMaterializationAndNoPostReturnQueries` | `s1PhysicalNewPathIsolationAndContextResumption`, `s6ImmutableMaterializationAndNoPostReturnQueries` | **PASS** |
| **ROB-13** | `PublicFictionReadIntegrationTest.s2WorkEligibilityAndFullValuesSnapshotIsolation`, `PublicFilmReadIntegrationTest.s2WorkEligibilityAndFullValuesSnapshotIsolation` | `s2WorkEligibilityAndFullValuesSnapshotIsolation` | **PASS** |
| **ROB-14** | `PublicFictionReadIntegrationTest.s3LinkedSourceSnapshotIsolation`, `PublicFilmReadIntegrationTest.s3LinkedSourceSnapshotIsolation` | `s3LinkedSourceSnapshotIsolation` | **PASS** |
| **ROB-15** | `PublicFictionReadIntegrationTest.s1PhysicalNewPathIsolationAndContextResumption`, `PublicFilmReadIntegrationTest.s1PhysicalNewPathIsolationAndContextResumption` | `s1PhysicalNewPathIsolationAndContextResumption` | **PASS** |
| **ROB-16** | `PublicFictionReadIntegrationTest.publicProfileSwitchAndNoStoreCachingControls`, `PublicFilmReadIntegrationTest.publicProfileSwitchAndNoStoreCachingControls` | `publicProfileSwitchAndNoStoreCachingControls` | **PASS** |
| **ROB-17** | `OpenApiRouteInventoryIntegrationTest.openApiContractContainsAllRoutesAndUniqueOperationIds` | `openApiContractContainsAllRoutesAndUniqueOperationIds` | **PASS** |
| **ROB-18** | `OpenApiRouteInventoryIntegrationTest.openApiContractContainsAllRoutesAndUniqueOperationIds`, `PublicFilmCreateIntegrationTest.followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys` | `openApiContractContainsAllRoutesAndUniqueOperationIds`, `followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys` | **PASS** |
| **ROB-19** | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins` | `registeredRoutesDeniedToPublicTokenFromBothOrigins` | **PASS** |
| **ROB-20** | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`, `SecurityFilterChainIntegrationTest.permitsPublicBearerTokenOnExactB1EndpointsAndDeniesUnselected`, `PublicFilmCreateIntegrationTest.publicTokenFromProfilesLoginDeniedWith403AndNoMutation` | `registeredRoutesDeniedToPublicTokenFromBothOrigins`, `permitsPublicBearerTokenOnExactB1EndpointsAndDeniesUnselected`, `publicTokenFromProfilesLoginDeniedWith403AndNoMutation` | **PASS** |
| **ROB-21** | `ApplicationArchitectureTests` (all 38 rules) | 38 architecture rule testcases | **PASS** |
| **ROB-22** | All 6 finite gates executed under bounded watchdog, receipts in `logs/bf26-dp-ro-b/impl-20261114-1/`, XMLs preserved | Surefire XML reports in `logs/bf26-dp-ro-b/impl-20261114-1/*-surefire/` | **PASS** |

---

## 4. Finite Verification Gate Execution & Receipts

All 6 finite gates were executed fresh under the bounded watchdog runner (`scripts/run-bounded-command.py`, normal profile, hard cap <= 300s):

```
Directory: logs/bf26-dp-ro-b/impl-20261114-1/
```

| Gate | Selector | Exit Code | Tests | Failures | Errors | Skipped | Time | Receipt |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| **G-ROB-FICTION** | `PublicFictionReadIntegrationTest` | 0 | 15 | 0 | 0 | 0 | 79.9s | `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-FICTION-r2.json` |
| **G-ROB-FILM** | `PublicFilmReadIntegrationTest` | 0 | 15 | 0 | 0 | 0 | 70.5s | `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-FILM-r2.json` |
| **G-ROB-SECURITY** | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | 0 | 27 | 0 | 0 | 0 | 123.0s | `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-SECURITY.json` |
| **G-ROB-PRIVATE** | `PublicFilmCreateIntegrationTest,FictionWebIntegrationTest,FilmWebIntegrationTest` | 0 | 23 | 0 | 0 | 0 | 92.1s | `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-PRIVATE-r2.json` |
| **G-ROB-OPENAPI** | `OpenApiRouteInventoryIntegrationTest` | 0 | 1 | 0 | 0 | 0 | 59.5s | `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-OPENAPI.json` |
| **G-ROB-ARCH** | `ApplicationArchitectureTests` | 0 | 38 | 0 | 0 | 0 | 44.0s | `logs/bf26-dp-ro-b/impl-20261114-1/G-ROB-ARCH.json` |
| **Total** | 9 test classes across 6 gates | **0** | **119** | **0** | **0** | **0** | **~469s** | All 6 gates passed |

---

## 5. Documentation Synchronization Check

- **UPDATED:**
  - `docs/implementation/maintenance/2026-11-14-bf26-dp-ro-b-preparation/self-review-dp-ro-b-2026-11-14.md`
  - `docs/implementation/maintenance/2026-11-14-bf26-dp-ro-b-preparation/implementation-handback-dp-ro-b-2026-11-14.md`
  - `docs/implementation/maintenance/2026-11-14-bf26-dp-ro-b-preparation/executable-handoff-dp-ro-b-2026-11-14.md` (sections 9 & 11)
  - `docs/implementation/handoffs/ACTIVE.md` (transitioned to `IMPLEMENTED_AWAITING_CODEX_REVIEW`)
- **ALREADY_CONSISTENT:**
  - Published preparation documents, predecessor publications, frozen DBML Schema v1, module architecture rules, provider defaults OFF.
- **DEFERRED_OUT_OF_SCOPE:**
  - Dated preparation notices, root roadmap, and repository-wide navigation sync (ChatGPT governance owner upon Owner publication).
- **NOT_VERIFIED:**
  - Live production deployment/cutover and real Owner-vault data suitability.

---

## 6. Implementation Result & Routing

The implementation of `BF26-DP-RO-B` is complete, cleanly tested with 119/119 passing tests, verified across all 6 finite gates under bounded execution, and self-reviewed.

Next step: Codex | `$codex-final-review` | BF26-DP-RO-B ONLY, `docs/implementation/handoffs/ACTIVE.md` and `docs/implementation/maintenance/2026-11-14-bf26-dp-ro-b-preparation/executable-handoff-dp-ro-b-2026-11-14.md` | Independent final review of 18-file boundary, ROB-01–22 criteria, S1–S6 witnesses, and receipts | RO-B remediation 0/3 consumed; takeover NOT_AUTHORIZED / NOT_USED; RO-A published.
