# Implementation Handback — BF26-DP-RO-B Remediation Round 2

- **Handoff ID:** `BF26-DP-RO-B`
- **Date:** 2026-11-15
- **Author / Implementer:** Antigravity (backend-implementer)
- **Status:** **`IMPLEMENTED_AWAITING_CODEX_REVIEW`**
- **Authority:** Owner's 2026-11-14 authorization for BF26-DP-RO-B ONLY ([executable handoff](executable-handoff-dp-ro-b-2026-11-14.md) and [ACTIVE](../../handoffs/ACTIVE.md)).
- **Remediation Counters:** Remediation **2/3 consumed** (Round 2 correction complete); Takeover **NOT_AUTHORIZED / NOT_USED**; RO-A **OWNER_PUBLISHED / CLOSED** (`0956b4962f529050dddf88dc7f2ec19ac571e0d0`).

---

## 1. Remediation Summary (FR-ROB-02 through FR-ROB-05)

This handback reports the completion of Remediation Round 2 resolving the specific remainders identified in [independent final re-review 1](reviews/2026-11-15-bf26-dp-ro-b-independent-final-re-review-1.md) (`FR-ROB-01` was independently **CLOSED** in re-review 1):

1. **FR-ROB-02 (S1 Actual Query Points & S2 Full-State Snapshot Proof):**
   - **S1 (Actual Spring Data Repository Instrumentation & Snapshot Identity):**
     - In `PublicFictionReadIntegrationTest.java` (T01) and `PublicFilmReadIntegrationTest.java` (T02), instrumented actual Spring Data JPA repository query execution via Spring AOP advice rather than outer spy entry callbacks.
     - Advised repositories:
       - Fiction: `fictionRepository` (`findById`), `storyArchetypeRepository`, `worldSettingRepository`, `vaultEntryRepository` (`findById`), `personRepository` (`findById`), and `creatorGroupRepository` (`findById`).
       - Film: `filmRepository` (`findById`), `filmGenreAssignmentRepository`, `storyArchetypeRepository`, `worldSettingRepository`, `vaultEntryRepository` (`findById`), and `personRepository` (`findById`).
     - At every real data query point across both paths (Fiction Person author, Fiction Group author, and Film director), captured `pg_backend_pid()`, `pg_current_snapshot()::text`, `SHOW transaction_isolation`, and `SHOW transaction_read_only`.
     - Positively asserted:
       - Single identical `pg_current_snapshot()::text` string across all queries in the transaction.
       - Dedicated inner connection distinct from outer connection (`pid.isNotEqualTo(outerPid)`).
       - Consistent `transaction_isolation = 'repeatable read'` and `transaction_read_only = 'on'`.
       - Outer managed entity held in outer transaction is detached in inner persistence context (`innerContainsOuterEntity == false`).
       - Outer transaction cleanly resumes with original connection PID, `read committed` isolation, and intact managed entity.
   - **S2 (Full-State & Classification Snapshot Race Witness):**
     - In both T01 and T02, concurrent committed writers now commit classification changes (inserting a new archetype/genre and deleting a world setting), work Vault deletion/trash, and full work row mutations.
     - **Case A (Eligibility Destroyed):** Held reader asserts that the snapshot preserves all 19 properties matching the earlier eligible state, including exact initial classification set membership (`Set.of(id + 3_000_000L)` and `Set.of(id + 4_000_000L)`). Subsequent fresh HTTP request after commit returns 404 Not Found with `Cache-Control: no-store` and exactly 0 calls to People/Group.
     - **Case B (Eligibility-Preserving Mutation):** Concurrent writer commits mutations across full work values (title, original title, progress status, chapters/episodes, review, progress text) and classification assignments. Held reader observes the exact earlier 19 values and earlier classification sets. Subsequent fresh HTTP request returns 200 OK and asserts all 19 updated properties and updated classification sets.

2. **FR-ROB-03 (Separating Missing Identity from Mismatch & S6 Captured-Set Safeguards):**
   - **S4 (Defensive Identity Separation):**
     - In both T01 and T02:
       - Separated missing work Vault (`Optional.empty()`) from mismatched work Vault ID (`new VaultEntryView(id + 999_999L, ...)`). Both yield 404, `no-store`, and 0 People calls.
       - In Person Vault qualification, added distinct test cases for missing Person Vault (`Optional.empty()`) and mismatched Person Vault ID (`new VaultEntryView(personId + 999_999L, ...)`). Both yield 200 with `authorName: null` / `directorName: null`.
       - In reference absence, added injected schema-disallowed null Person name (`new PersonView(id, null, ...)`), null CreatorGroup name (`new CreatorGroupView(id, null, ...)`), and null Film director name. All yield 200 with explicit `null` name.
   - **S6 (Complete Immutable Result Materialized Before Return):**
     - Installed AOP advice counters on all underlying repositories (`workRepoCalls`, `archRepoCalls`, `settingRepoCalls`, `vaultRepoCalls`, `personRepoCalls`, `genreRepoCalls`).
     - Positively verified that every repository was queried during `findPublicDetail` acquisition (all counters > 0).
     - Asserted exact expected non-empty sets on the view (`expectedArchetypes`, `expectedSettings`, `expectedGenres`), and verified collection immutability via `UnsupportedOperationException`.
     - Reset counters and Mockito capability spies.
     - Mapped view to response DTO (`PublicFictionDetailResponse` / `PublicFilmDetailResponse`) outside the transaction; serialized DTO through configured `ObjectMapper` to JSON.
     - Asserted exact expected sets and scalar values in serialized JSON.
     - Confirmed via repository advice counters that exactly 0 queries occurred post-return during mapping or serialization, accompanied by Mockito `verify(..., never()).find(...)`.

3. **FR-ROB-04 (Group Full-Value Parity):**
   - In `PublicFictionReadIntegrationTest.java` lines 280–298:
     - Populated Group work fixture requested via PRIVATE GET with valid bearer token.
     - Asserted exact 18 keys on the PRIVATE response (verifying absence of `authorName`).
     - Verified all 18 common values and classification sets match the PUBLIC response field-for-field (`for (String commonField : privGroupFields) assertThat(groupData.path(commonField)).isEqualTo(privGroupData.path(commonField));`).

4. **FR-ROB-05 (Acceptance Assertion Traceability):**
   - Standardized canonical file IDs across all handback and self-review tables:
     - `T03` = `ProfileRouteAuthorizationIntegrationTest.java` (`30bdfc5e942def5cd3a28dd8e5e6326a09b6929b304a01d3b3714da49fb3c395`)
     - `T05` = `PublicFilmCreateIntegrationTest.java` (`34557d898c3f2db2261ff7563629f4ad6b8417ee3832b4ea2bf18e077f8a37e0`)
   - Correctly attributed PRIVATE POST/PUT key assertions to `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor` (Fiction) and `eligibleFilmHasExactlyNineteenKeysAndResolvedDirector` (Film).
   - Accurately cited actual S6 test assertions: repository advice counters, JSON array assertions, and `verify(..., never()).find(...)` without claiming `verifyNoMoreInteractions`.
   - Explicitly distinguished remediated items submitted for re-review from independent closure.

---

## 2. Eighteen-File Source & Test Manifest with Final SHA-256 Hashes

| ID | File Path | Status | Final SHA-256 |
| --- | --- | --- | --- |
| `P01` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/fiction/FictionOperations.java` | Modified (Unchanged in R2) | `82afa7bd49695657a3f374755b1b143a13e2e1bb667af638a4a5617a6e16d613` |
| `P02` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/view/PublicFictionDetailView.java` | **NEW** (Unchanged in R2) | `6af10cb075899f84b8530c6439296189b700c3da980f660623a93f53c9b9624d` |
| `P03` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/application/FictionService.java` | Modified (Unchanged in R2) | `05b2a80c46d7129197c1683c26e1b11ede353138353a9e4ae5e059a24a0b331f` |
| `P04` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/web/controller/FictionController.java` | Modified (Unchanged in R2) | `c8c866d01ba41293639db1e2ed397b0c29511ef21b1bf0b296c4172de3a1d8ca` |
| `P05` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/web/mapper/FictionWebMapper.java` | Modified (Unchanged in R2) | `edca7befd8252a99269216ef330a63062699a9a186df6ee9128efb08cbc7c38c` |
| `P06` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/internal/web/dto/PublicFictionDetailResponse.java` | **NEW** (Unchanged in R2) | `b45ac42db61a09242f79597435ceca98ceed6db11feff860f1cdbbe24f618c7a` |
| `P07` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/film/FilmOperations.java` | Modified (Unchanged in R2) | `43e49bcd5ee158885249ff73e3db9b383edf652cab0d4ea6aca9054125ed611f` |
| `P08` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/view/PublicFilmDetailView.java` | **NEW** (Unchanged in R2) | `fd5cf5e0f5205dfc39881dbbe64f034de5907d7680e2211b9711451200c29a9f` |
| `P09` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/application/FilmService.java` | Modified (Unchanged in R2) | `f9cbb9a366a496c5fe4dcc3469db74fc137df99ff29476ba0a39a3528f82a8fd` |
| `P10` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/controller/FilmController.java` | Modified (Unchanged in R2) | `36dd727b155c7102fda1f038aa4e89992f36770536b9219a04c7b4690daadffd` |
| `P11` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/mapper/FilmWebMapper.java` | Modified (Unchanged in R2) | `85fd9cb763e69b57817d7b77a57b827c683981af3efddd73d5b2e9915ca170dc` |
| `P12` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/film/internal/web/dto/PublicFilmDetailResponse.java` | **NEW** (Unchanged in R2) | `9ecd2a6ceaab920591dfaaef7b8ace6d07081dc5afe629a7667be360d9d22b99` |
| `P13` | `backend/src/main/java/com/vhvkhangg/personalprivatevault/OpenApiConfiguration.java` | Modified (Unchanged in R2) | `10018e73e8854fa08a6ce326d0313c1b765cb10a61503e08f1c6169b123ee516` |
| `T01` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/fiction/PublicFictionReadIntegrationTest.java` | Modified (Round 2) | `880ca84e2f441930009a5258d46f356160a8e9fa242e2e1bbbd3fb9510154db9` |
| `T02` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmReadIntegrationTest.java` | Modified (Round 2) | `03fa58646f44adb8ea71ab2ee84ffd353e5717d5dda4ed96d8ac71024bce9b53` |
| `T03` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/ProfileRouteAuthorizationIntegrationTest.java` | Modified (Unchanged in R2) | `30bdfc5e942def5cd3a28dd8e5e6326a09b6929b304a01d3b3714da49fb3c395` |
| `T04` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` | Modified (Unchanged in R2) | `b9d30ce0eecc4664e79c48257817b6a5fbb4acc798bb335ad01698b7551e251c` |
| `T05` | `backend/src/test/java/com/vhvkhangg/personalprivatevault/film/PublicFilmCreateIntegrationTest.java` | Modified (Unchanged in R2) | `34557d898c3f2db2261ff7563629f4ad6b8417ee3832b4ea2bf18e077f8a37e0` |

---

## 3. Exact Traceability Matrix: ROB-01 – ROB-22 to Actual Test Methods

| Criteria | Actual Test Class & Method Name | XML Testcase Identity | Result |
| --- | --- | --- | --- |
| **ROB-01** | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`, `OpenApiRouteInventoryIntegrationTest.openApiContractContainsAllRoutesAndUniqueOperationIds` | `registeredRoutesDeniedToPublicTokenFromBothOrigins`, `openApiContractContainsAllRoutesAndUniqueOperationIds` | **PASS** |
| **ROB-02** | `PublicFictionReadIntegrationTest.publicSessionsReachFictionDetail`, `PublicFilmReadIntegrationTest.publicSessionsReachFilmDetail`, `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins` | `publicSessionsReachFictionDetail`, `publicSessionsReachFilmDetail`, `registeredRoutesDeniedToPublicTokenFromBothOrigins` | **PASS** |
| **ROB-03** | `PublicFictionReadIntegrationTest.eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor` | `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor` | **PASS** |
| **ROB-04** | `PublicFilmReadIntegrationTest.eligibleFilmHasExactlyNineteenKeysAndResolvedDirector` | `eligibleFilmHasExactlyNineteenKeysAndResolvedDirector` | **PASS** |
| **ROB-05** | `PublicFictionReadIntegrationTest.eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor` (PRIVATE GET/POST/PUT 18 keys), `PublicFilmReadIntegrationTest.eligibleFilmHasExactlyNineteenKeysAndResolvedDirector` (PRIVATE GET/POST/PUT 18 keys), `PublicFilmCreateIntegrationTest.privateProfileCreateRetainsFullEighteenKeyBehavior` (Film POST 18 keys) | `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor`, `eligibleFilmHasExactlyNineteenKeysAndResolvedDirector`, `privateProfileCreateRetainsFullEighteenKeyBehavior` | **PASS** |
| **ROB-06** | `PublicFictionReadIntegrationTest.eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor`, `PublicFictionReadIntegrationTest.qualifiesPersonVaultBeforeResolvingName` | `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor`, `qualifiesPersonVaultBeforeResolvingName` | **PASS** |
| **ROB-07** | `PublicFictionReadIntegrationTest.eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor`, `PublicFictionReadIntegrationTest.referenceAbsenceYieldsPresentNullName` | `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor`, `referenceAbsenceYieldsPresentNullName` | **PASS** |
| **ROB-08** | `PublicFilmReadIntegrationTest.eligibleFilmHasExactlyNineteenKeysAndResolvedDirector`, `PublicFilmReadIntegrationTest.qualifiesDirectorPersonVaultBeforeResolvingName` | `eligibleFilmHasExactlyNineteenKeysAndResolvedDirector`, `qualifiesDirectorPersonVaultBeforeResolvingName` | **PASS** |
| **ROB-09** | `PublicFictionReadIntegrationTest.qualifiesPersonVaultBeforeResolvingName`, `PublicFictionReadIntegrationTest.referenceAbsenceYieldsPresentNullName`, `PublicFilmReadIntegrationTest.qualifiesDirectorPersonVaultBeforeResolvingName`, `PublicFilmReadIntegrationTest.referenceAbsenceYieldsPresentNullName` | `qualifiesPersonVaultBeforeResolvingName`, `referenceAbsenceYieldsPresentNullName`, `qualifiesDirectorPersonVaultBeforeResolvingName` | **PASS** |
| **ROB-10** | `PublicFictionReadIntegrationTest.ineligibleFictionReturnsFourHundredFourAndZeroPeopleAccess`, `PublicFilmReadIntegrationTest.ineligibleFilmReturnsFourHundredFourAndZeroPeopleAccess` | `ineligibleFictionReturnsFourHundredFourAndZeroPeopleAccess`, `ineligibleFilmReturnsFourHundredFourAndZeroPeopleAccess` | **PASS** |
| **ROB-11** | `PublicFictionReadIntegrationTest.s5OperationalAndCompletionFailuresYieldSafe500`, `PublicFilmReadIntegrationTest.s5OperationalAndCompletionFailuresYieldSafe500` | `s5OperationalAndCompletionFailuresYieldSafe500` | **PASS** |
| **ROB-12** | `PublicFictionReadIntegrationTest.s1PhysicalNewPathIsolationAndContextResumption`, `PublicFictionReadIntegrationTest.s6CompleteImmutableResultMaterializedBeforeReturn`, `PublicFilmReadIntegrationTest.s1PhysicalNewPathIsolationAndContextResumption`, `PublicFilmReadIntegrationTest.s6CompleteImmutableResultMaterializedBeforeReturn` | `s1PhysicalNewPathIsolationAndContextResumption`, `s6CompleteImmutableResultMaterializedBeforeReturn` | **PASS** |
| **ROB-13** | `PublicFictionReadIntegrationTest.s2WorkEligibilityAndFullValuesSnapshotIsolation`, `PublicFilmReadIntegrationTest.s2WorkEligibilityAndFullValuesSnapshotIsolation` | `s2WorkEligibilityAndFullValuesSnapshotIsolation` | **PASS** |
| **ROB-14** | `PublicFictionReadIntegrationTest.s3LinkedSourceSnapshotIsolation`, `PublicFilmReadIntegrationTest.s3LinkedSourceSnapshotIsolation` | `s3LinkedSourceSnapshotIsolation` | **PASS** |
| **ROB-15** | `PublicFictionReadIntegrationTest.s1PhysicalNewPathIsolationAndContextResumption`, `PublicFilmReadIntegrationTest.s1PhysicalNewPathIsolationAndContextResumption` | `s1PhysicalNewPathIsolationAndContextResumption` | **PASS** |
| **ROB-16** | `PublicFictionReadIntegrationTest.publicProfileSwitchAndNoStoreCachingControls`, `PublicFilmReadIntegrationTest.publicProfileSwitchAndNoStoreCachingControls` | `publicProfileSwitchAndNoStoreCachingControls` | **PASS** |
| **ROB-17** | `OpenApiRouteInventoryIntegrationTest.openApiContractContainsAllRoutesAndUniqueOperationIds` | `openApiContractContainsAllRoutesAndUniqueOperationIds` | **PASS** |
| **ROB-18** | `OpenApiRouteInventoryIntegrationTest.openApiContractContainsAllRoutesAndUniqueOperationIds`, `PublicFilmCreateIntegrationTest.followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys` | `openApiContractContainsAllRoutesAndUniqueOperationIds`, `followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys` | **PASS** |
| **ROB-19** | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins` | `registeredRoutesDeniedToPublicTokenFromBothOrigins` | **PASS** |
| **ROB-20** | `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins`, `SecurityFilterChainIntegrationTest.permitsPublicBearerTokenOnExactB1EndpointsAndDeniesUnselected`, `PublicFilmReadIntegrationTest.publicTokenDeniedToPostFilmWith403AndNoHandler` | `registeredRoutesDeniedToPublicTokenFromBothOrigins`, `permitsPublicBearerTokenOnExactB1EndpointsAndDeniesUnselected`, `publicTokenDeniedToPostFilmWith403AndNoHandler` | **PASS** |
| **ROB-21** | `ApplicationArchitectureTests` | 38 architecture rule testcases | **PASS** |
| **ROB-22** | All 6 finite gates executed under bounded watchdog, receipts in `logs/bf26-dp-ro-b/remediation-round2/`, XMLs preserved | Surefire XML reports in `logs/bf26-dp-ro-b/remediation-round2/*-surefire/` | **PASS** |

---

## 4. Finite Verification Gate Execution & Receipts

All 6 finite gates were executed fresh under the bounded watchdog runner (`scripts/run-bounded-command.py`, normal profile, hard cap <= 300s):

```
Directory: logs/bf26-dp-ro-b/remediation-round2/
```

| Gate | Command / Test Selector | Fresh Tests | Fail | Err | Wall Time | Receipt Path | Surefire XML Preservation Directory |
| --- | --- | --- | --- | --- | --- | --- | --- |
| **G-ROB-FICTION** | `mvn ... -Dtest=PublicFictionReadIntegrationTest` | 12 | 0 | 0 | 65.61s | `G-ROB-FICTION.json` | `G-ROB-FICTION-surefire/` |
| **G-ROB-FILM** | `mvn ... -Dtest=PublicFilmReadIntegrationTest` | 13 | 0 | 0 | 83.13s | `G-ROB-FILM.json` | `G-ROB-FILM-surefire/` |
| **G-ROB-SECURITY** | `mvn ... -Dtest=ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | 27 | 0 | 0 | 86.63s | `G-ROB-SECURITY.json` | `G-ROB-SECURITY-surefire/` |
| **G-ROB-PRIVATE** | `mvn ... -Dtest=PublicFilmCreateIntegrationTest,FictionWebIntegrationTest,FilmWebIntegrationTest` | 23 | 0 | 0 | 64.11s | `G-ROB-PRIVATE.json` | `G-ROB-PRIVATE-surefire/` |
| **G-ROB-OPENAPI** | `mvn ... -Dtest=OpenApiRouteInventoryIntegrationTest` | 1 | 0 | 0 | 44.59s | `G-ROB-OPENAPI.json` | `G-ROB-OPENAPI-surefire/` |
| **G-ROB-ARCH** | `mvn ... -Dtest=ApplicationArchitectureTests` | 38 | 0 | 0 | 37.09s | `G-ROB-ARCH.json` | `G-ROB-ARCH-surefire/` |
| **TOTAL** | **6 sequential gates / 9 test classes** | **114** | **0** | **0** | **381.16s** (cumulative) | All 6 receipts exit 0 | All XML reports verified |

---

## 5. Documentation Synchronization Check

- **Scope:** BF26-DP-RO-B Remediation Round 2
- **Updated:**
  - `docs/implementation/handoffs/ACTIVE.md`: Top banner, status `IMPLEMENTED_AWAITING_CODEX_REVIEW`, remediation counter 2/3 consumed.
  - `docs/implementation/maintenance/2026-11-14-bf26-dp-ro-b-preparation/executable-handoff-dp-ro-b-2026-11-14.md`: Section 9 final SHA-256 hashes, Section 14 Round 2 results.
  - `docs/implementation/maintenance/2026-11-14-bf26-dp-ro-b-preparation/self-review-dp-ro-b-round-2-2026-11-15.md`: Round 2 self-review report.
  - `docs/implementation/maintenance/2026-11-14-bf26-dp-ro-b-preparation/implementation-handback-dp-ro-b-round-2-2026-11-15.md`: This document.
- **Already Consistent:**
  - Exact 18-path boundary (13 production, 5 tests).
  - All predecessor counters and historical checkpoints intact.
  - Providers OFF, RAG suspended, database migrations untouched.
- **Deferred Out of Scope:**
  - Repository-wide documentation synchronization and Owner publication closeout (owned by ChatGPT under separate governance).
- **Not Verified:**
  - Live production cutover and Owner personal data suitability.

---

## 6. Next Step

Next step: Codex | `$codex-final-review` | BF26-DP-RO-B Remediation Round 2, canonical handback docs/implementation/maintenance/2026-11-14-bf26-dp-ro-b-preparation/implementation-handback-dp-ro-b-round-2-2026-11-15.md | independent final re-review of FR-ROB-02–05 remainders | remediation 2/3 consumed; takeover NOT_AUTHORIZED / NOT_USED.
