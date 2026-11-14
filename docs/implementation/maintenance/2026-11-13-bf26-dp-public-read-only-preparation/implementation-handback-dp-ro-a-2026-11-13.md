# Implementation Handback — BF26-DP-RO-A: Public Write Revocation (Remediation Round 1)

- **Handoff ID:** `BF26-DP-RO-A`
- **Date:** 2026-11-14
- **Author / Implementer:** Antigravity (backend-implementer)
- **Status:** **`IMPLEMENTED_AWAITING_CODEX_REVIEW`**
- **Authority:** Owner's 2026-11-13 authorization for BF26-DP-RO-A and Owner-approved allowlist amendment (6 → 8 files).
- **Remediation Counters:** Remediation **1/3 consumed** (2 remaining); Takeover **NOT_AUTHORIZED / NOT_USED**; RO-B **DEFERRED**.

---

## 1. Remediation Round 1 Overview

This handback reports the completion of Remediation Round 1 addressing the findings from the [independent final review](reviews/2026-11-14-bf26-dp-ro-a-independent-final-review.md):

1. **FR-ROA-01 (Film-Specific MVC Observer Qualification):**
   - In `ProfileRouteAuthorizationIntegrationTest.java`: `handlerAdapter` invocation history is cleared before the PRIVATE Film POST; the observed handler is verified specifically as `FilmController.create` via `ArgumentCaptor`; invocations are cleared again before testing formerly valid 5-field body POSTs under direct PUBLIC, downgraded PUBLIC, and anonymous tokens, verifying 0 `handlerAdapter.handle` calls and unchanged domain snapshots.
   - In `PublicFilmCreateIntegrationTest.java`: `handlerAdapter` spy is injected; verified to never be called on direct PUBLIC (`publicTokenFromProfilesLoginDeniedWith403AndNoMutation`), downgraded PUBLIC (`publicTokenFromDowngradeDeniedWith403AndNoMutation`), and anonymous (`anonymousRequestToCreateFilmReturns401`); verified to specifically observe `FilmController.create` on PRIVATE creation (`privateProfileCreateRetainsFullEighteenKeyBehavior`).
2. **FR-ROA-02 (PRIVATE Validation & Domain Rejection Negatives):**
   - Added `privateStructurallyInvalidRequestFailsBeanValidationWith400` in `PublicFilmCreateIntegrationTest.java`: blank title under PRIVATE token fails Bean Validation `@NotBlank`, returns canonical 400 `VALIDATION_ERROR` ("Request validation failed"), empty data, and unchanged domain snapshot.
   - Added `privateDomainValidationFailureReturns422FilmInvalid` in `PublicFilmCreateIntegrationTest.java`: nonexistent nationality code "ZZ" under PRIVATE token fails reference catalog validation in `FilmService`, returns canonical 422 `FILM_INVALID` ("Invalid film data"), empty data, and unchanged domain snapshot.
3. **FR-ROA-03 (Accurate Evidence Mapping):**
   - Replaced all descriptive/fictional method references with the exact method names present in source and executed in surefire XML reports.
4. **DPROA-L01 (Backlog / Cleanup):**
   - Correctly reported across all documentation as 9 test classes across 5 gates (117 tests total); updated T1 assertion message from 9-pair to 8-pair; 8-file allowlist wording maintained.

---

## 2. Eight-File Modified Source Manifest & Final SHA-256 Hashes

| ID | File Path | Final SHA-256 |
| --- | --- | --- |
| `P1` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/infrastructure/security/SecurityConfiguration.java` | `c5753d4998b2bc60014e821d08dd42c079086becf12db467d7bcbea58e15e617` |
| `P2` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/controller/FilmController.java` | `f87d39e3871410081ee624efc5ffbb9a5c5df74e5a2746c7de51200be08cf1ea` |
| `P3` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/OpenApiConfiguration.java` | `faaad86f9122ea6653054c3117fddf1fe4886c2275a0fa9d97af88554f295289` |
| `T1` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/ProfileRouteAuthorizationIntegrationTest.java` | `66c3c623ffff8a3e5ec03ca1a9b7136155c77a572d4534b976e7444bc2d85be9` |
| `T2` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmCreateIntegrationTest.java` | `be3094bda9640ff499f2ac3a7dd071f8e94cd4c6b56aae232783ce58be7e69f3` |
| `T3` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` | `91adc3f0d91c82cdff9ada13cb5c52616a66328e29b02d82f87b107bfabda857` |
| `T4` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/SecurityFilterChainIntegrationTest.java` | `4c444d5690755ef7ebad17c00845c5bd7b9fac054162b32bf22ca60bd7dce9aa` |
| `T5` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmReadIntegrationTest.java` | `26f95458c3ba97fcf35199f6c673032495d6f7793a485443e1c942c9259f4d65` |

---

## 3. Requirement-to-Evidence Trace (ROA-01 – ROA-12)

| Criteria | Actual Test Class & Method Name | XML Testcase Identity | Result |
| --- | --- | --- | --- |
| **ROA-01** | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`, `PublicFilmCreateIntegrationTest.publicTokenFromProfilesLoginDeniedWith403AndNoMutation`, `SecurityFilterChainIntegrationTest.permitsPublicBearerTokenOnExactB1EndpointsAndDeniesUnselected` | `publicTokenFromProfilesLoginDeniedWith403AndNoMutation`, `registeredRoutesDeniedToPublicTokenFromBothOrigins`, `permitsPublicBearerTokenOnExactB1EndpointsAndDeniesUnselected` | **PASS** |
| **ROA-02** | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`, `PublicFilmCreateIntegrationTest.publicTokenFromDowngradeDeniedWith403AndNoMutation` | `publicTokenFromDowngradeDeniedWith403AndNoMutation`, `registeredRoutesDeniedToPublicTokenFromBothOrigins` | **PASS** |
| **ROA-03** | `PublicFilmCreateIntegrationTest.publicPayloadVariantsDeniedWith403BeforeParsingOrValidation`, `PublicFilmCreateIntegrationTest.publicMissingOrTextPlainContentTypeDeniedWith403` | `publicPayloadVariantsDeniedWith403BeforeParsingOrValidation`, `publicMissingOrTextPlainContentTypeDeniedWith403` | **PASS** |
| **ROA-04** | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`, `PublicFilmCreateIntegrationTest.anonymousRequestToCreateFilmReturns401`, `SecurityFilterChainIntegrationTest.rejectsMissingBearerToken` | `anonymousRequestToCreateFilmReturns401`, `rejectsMissingBearerToken` | **PASS** |
| **ROA-05** | `PublicFilmCreateIntegrationTest.privateProfileCreateRetainsFullEighteenKeyBehavior`, `FilmWebIntegrationTest.filmLifecycleOverHttp` | `privateProfileCreateRetainsFullEighteenKeyBehavior`, `filmLifecycleOverHttp` | **PASS** |
| **ROA-06** | `PublicFilmCreateIntegrationTest.privateVendorJsonContentTypeAccepted`, `privateIso88591CharsetDecodedCorrectly`, `privateUnknownKeyRejectedWith400`, `privateMissingContentTypeReturns415`, `privateUnsupportedMediaTypeReturns415`, `privateStructurallyInvalidRequestFailsBeanValidationWith400`, `privateDomainValidationFailureReturns422FilmInvalid` | `privateVendorJsonContentTypeAccepted`, `privateIso88591CharsetDecodedCorrectly`, `privateUnknownKeyRejectedWith400`, `privateMissingContentTypeReturns415`, `privateUnsupportedMediaTypeReturns415`, `privateStructurallyInvalidRequestFailsBeanValidationWith400`, `privateDomainValidationFailureReturns422FilmInvalid` | **PASS** |
| **ROA-07** | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins` | `registeredRoutesDeniedToPublicTokenFromBothOrigins` | **PASS** |
| **ROA-08** | `ProfileRouteAuthorizationIntegrationTest.methodAndPathVariantsOnPublicRoutesDenyNonGetAndSubpaths`, `sameUriAsyncAndErrorDispatcherDeniesPublicSession`, `actualAsyncRedispatchRevalidatesPropagatedContextWithoutBearer`, `errorAndAsyncDispatcherRequiresPrivate`, `PublicFilmCreateIntegrationTest.d3AdmissionRaceInFlightCreateDuringRevocation`, `d3AdmissionRacePauseBeforeAuthoritySnapshot`, `d3AuthorityLookupUnavailableFailsClosed` | `methodAndPathVariantsOnPublicRoutesDenyNonGetAndSubpaths`, `sameUriAsyncAndErrorDispatcherDeniesPublicSession`, `d3AdmissionRaceInFlightCreateDuringRevocation`, `d3AdmissionRacePauseBeforeAuthoritySnapshot`, `d3AuthorityLookupUnavailableFailsClosed` | **PASS** |
| **ROA-09** | `OpenApiRouteInventoryIntegrationTest.openApiContractContainsAllRoutesAndUniqueOperationIds` | `openApiContractContainsAllRoutesAndUniqueOperationIds` | **PASS** |
| **ROA-10** | `PublicFictionReadIntegrationTest` (all 13 methods), `PublicFilmReadIntegrationTest` (all 13 methods), `ReferenceWebIntegrationTest` (all 3 methods), `PublicFilmCreateIntegrationTest.followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys` | 29 XML testcases in `G-ROA-READ`, `followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys` | **PASS** |
| **ROA-11** | `PublicFilmCreateIntegrationTest.sqlInsertionFailureRollsBackCompletely`, `deferredCommitFailureRollsBackCompletely`, `filmCreationProvesAtomicityAndDatabaseIntegrity`, `ApplicationArchitectureTests` (all 38 rules) | `sqlInsertionFailureRollsBackCompletely`, `deferredCommitFailureRollsBackCompletely`, `filmCreationProvesAtomicityAndDatabaseIntegrity`, 38 architecture XML testcases | **PASS** |
| **ROA-12** | Handback artifacts, receipts, manifests, and Docs Sync | `logs/bf26-dp-ro-a/remediation-round1/`, `self-review-dp-ro-a-2026-11-13.md`, `implementation-handback-dp-ro-a-2026-11-13.md` | **PASS** |

---

## 4. Finite Implementation Gate Execution & Receipts

All 5 finite gates were executed fresh under bounded watchdog (`scripts/run-bounded-command.py`, normal profile <= 300s):

```
Directory: logs/bf26-dp-ro-a/remediation-round1/
```

| Gate | Selector | Tests Run | Result | Duration | Receipt Path |
| --- | --- | --- | --- | --- | --- |
| `G-ROA-SECURITY` | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | 27 (15 + 12) | **PASS (exit 0)** | 197.45s | `logs/bf26-dp-ro-a/remediation-round1/G-ROA-SECURITY.json` |
| `G-ROA-PRIVATE` | `PublicFilmCreateIntegrationTest,FilmWebIntegrationTest` | 22 (21 + 1) | **PASS (exit 0)** | 92.70s | `logs/bf26-dp-ro-a/remediation-round1/G-ROA-PRIVATE.json` |
| `G-ROA-API` | `OpenApiRouteInventoryIntegrationTest` | 1 | **PASS (exit 0)** | 66.19s | `logs/bf26-dp-ro-a/remediation-round1/G-ROA-API.json` |
| `G-ROA-READ` | `PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest,ReferenceWebIntegrationTest` | 29 (13 + 13 + 3) | **PASS (exit 0)** | 108.27s | `logs/bf26-dp-ro-a/remediation-round1/G-ROA-READ.json` |
| `G-ROA-ARCH` | `ApplicationArchitectureTests` | 38 | **PASS (exit 0)** | 56.72s | `logs/bf26-dp-ro-a/remediation-round1/G-ROA-ARCH.json` |
| **Total** | **9 test classes across 5 gates** | **117 / 117** | **PASS (exit 0)** | **521.33s** | **0 failures, 0 errors, 0 skipped** |

*Historical evidence note:* Initial attempts and amendment receipts remain preserved under `logs/bf26-dp-ro-a/implementation-20261113-r0/` and `logs/bf26-dp-ro-a/amendment-20261113-r0/`.

---

## 5. Documentation Synchronization Check

- **UPDATED:**
  - `docs/implementation/handoffs/ACTIVE.md`: status updated to `IMPLEMENTED_AWAITING_CODEX_REVIEW`, remediation counter updated to `1/3 consumed`, 117 tests across 9 classes recorded.
  - `docs/implementation/maintenance/2026-11-13-bf26-dp-public-read-only-preparation/executable-handoff-dp-ro-a-2026-11-13.md`: status updated to `IMPLEMENTED_AWAITING_CODEX_REVIEW`, final SHA-256 fingerprints updated, remediation round 1 results recorded.
  - `docs/implementation/maintenance/2026-11-13-bf26-dp-public-read-only-preparation/self-review-dp-ro-a-2026-11-13.md`: updated mandatory self-review for Remediation Round 1.
  - `docs/implementation/maintenance/2026-11-13-bf26-dp-public-read-only-preparation/implementation-handback-dp-ro-a-2026-11-13.md`: this handback.
- **ALREADY_CONSISTENT:**
  - Backend production sources, database schema V1-V29, frozen DBML, ADRs, Spring Modulith descriptors, provider defaults.
- **DEFERRED_OUT_OF_SCOPE:**
  - Global navigation headers in `AGENTS.md`, `README.md`, `docs/roadmap.md` (reserved for ChatGPT/Owner post-acceptance).
- **NOT_VERIFIED:**
  - Live production deployment or cutover.

---

## 6. Status & Next Step

- Status: **`IMPLEMENTED_AWAITING_CODEX_REVIEW`**
- Remediation: **1/3 consumed** (2 remaining)
- Takeover: **NOT_AUTHORIZED / NOT_USED**
- RO-B: **DEFERRED**

**Next step: Codex | `$codex-final-review` | BF26-DP-RO-A, `docs/implementation/handoffs/ACTIVE.md` and `docs/implementation/maintenance/2026-11-13-bf26-dp-public-read-only-preparation/executable-handoff-dp-ro-a-2026-11-13.md` | IMPLEMENTED_AWAITING_CODEX_REVIEW; remediation round 1 complete, 8-file boundary, 117/117 passing tests across 9 classes | RO-A remediation 1/3 consumed; takeover NOT_AUTHORIZED; RO-B deferred.**
