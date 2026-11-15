# Antigravity Self-Review — BF26-DP-RO-B Remediation Round 2

- **Date:** 2026-11-15
- **Handoff ID:** `BF26-DP-RO-B`
- **Implementer:** Antigravity (backend-implementer)
- **Remediation Counters:** Remediation **2/3 consumed** (Round 2 correction complete); Takeover **NOT_AUTHORIZED / NOT_USED**
- **Review Scope:** Exactly 18 authorized targets (13 production P01–P13, 5 test T01–T05) under [executable handoff](executable-handoff-dp-ro-b-2026-11-14.md) and [ACTIVE](../../handoffs/ACTIVE.md)
- **Target Findings Remediated in Round 2:** `FR-ROB-02`, `FR-ROB-03`, `FR-ROB-04`, `FR-ROB-05` remainders from [independent final re-review 1](reviews/2026-11-15-bf26-dp-ro-b-independent-final-re-review-1.md) (`FR-ROB-01` was independently **CLOSED** in re-review 1)

---

## 1. Scope & Minimal Delta Verification

1. **Exact 18-Target Boundary (Production Unchanged from Round 1, Test-Only Delta):**
   - **Production (13 files - 0 edits in Round 2):**
     - `P01`: `FictionOperations.java` (qualified full-detail read signature)
     - `P02`: `PublicFictionDetailView.java` (immutable 19-property view)
     - `P03`: `FictionService.java` (`findPublicDetail` and `resolveAuthorName` with `REQUIRES_NEW / REPEATABLE_READ`)
     - `P04`: `FictionController.java` (PUBLIC GET branch with safe 500 error catch)
     - `P05`: `FictionWebMapper.java` (pure in-memory 19-field mapping)
     - `P06`: `PublicFictionDetailResponse.java` (exact 19 fields, `@JsonInclude(JsonInclude.Include.ALWAYS)`)
     - `P07`: `FilmOperations.java` (qualified full-detail read signature)
     - `P08`: `PublicFilmDetailView.java` (immutable 19-property view)
     - `P09`: `FilmService.java` (`findPublicDetail` and `resolveDirectorName` with `REQUIRES_NEW / REPEATABLE_READ`)
     - `P10`: `FilmController.java` (PUBLIC GET branch with safe 500 error catch)
     - `P11`: `FilmWebMapper.java` (pure in-memory 19-field mapping)
     - `P12`: `PublicFilmDetailResponse.java` (exact 19 fields, `@JsonInclude(JsonInclude.Include.ALWAYS)`)
     - `P13`: `OpenApiConfiguration.java` (canonical status enums, unchanged from accepted R1 fix)
   - **Tests (5 files - exactly T01 and T02 updated in Round 2):**
     - `T01`: `PublicFictionReadIntegrationTest.java` (**REMEDIATED FR-ROB-02, 03, 04, 05**: S1 Spring Data repository AOP query advice with `pg_current_snapshot()::text` and outer managed entity suspension/resumption for both Person and Group paths; S2 full 19-property and exact classification set assertions during concurrent writer commit destroying eligibility and preserving eligibility; S4 missing work Vault vs mismatched work Vault ID, missing Person Vault vs mismatched Person Vault ID, injected null Person and CreatorGroup names; S6 positive query acquisition counters, exact sets, DTO and JSON serialization outside transaction, zero post-return queries; populated Group fixture PRIVATE GET 18-key contract and full 18 common-value parity)
     - `T02`: `PublicFilmReadIntegrationTest.java` (**REMEDIATED FR-ROB-02, 03, 05**: S1 Spring Data repository AOP query advice with `pg_current_snapshot()::text` across all 7 data query points; S2 full 19-property and exact classification set assertions during concurrent writer commit; S4 missing work Vault vs mismatched work Vault ID, missing director Person Vault vs mismatched director Person Vault ID, injected null director name; S6 positive query acquisition counters, exact sets, DTO and JSON serialization outside transaction, zero post-return queries)
     - `T03`: `ProfileRouteAuthorizationIntegrationTest.java` (canonical ID T03: 15 tests, registered route sweep, 8 business GET pairs)
     - `T04`: `OpenApiRouteInventoryIntegrationTest.java` (canonical ID T04: 1 test, canonical status enum membership, specimen validations)
     - `T05`: `PublicFilmCreateIntegrationTest.java` (canonical ID T05: 21 tests, follow-up GET asserts exact 19 keys, PRIVATE creation/validation/rollback/RO-A denial tests preserved)

2. **Preservation Invariants:**
   - **Zero Unauthorized Edits:** `SecurityConfiguration.java` untouched; no repository edits; no People/Vault production edits; no database migrations or DBML changes; no dependency/pom.xml changes.
   - **PRIVATE Preservation:** PRIVATE Fiction/Film GET/POST/PUT remain exact 18 fields without `authorName` / `directorName`.
   - **PUBLIC POST Denial:** PUBLIC Film POST remains revoked with exact 403 `ACCESS_DENIED` before handler; anonymous remains 401.
   - **8 Permitted GET Pairs:** Authenticated PUBLIC token permits only the 8 accepted business GET pairs; all other business routes return 403 before handler.

---

## 2. Remediation Verification of Re-Review 1 Remainders (FR-ROB-02 – FR-ROB-05)

| Finding | Reviewer Re-Review 1 Assessment | Round 2 Implementation & Observable Proof | Status |
| --- | --- | --- | --- |
| **FR-ROB-01** | **CLOSED** by reviewer in re-review 1 | Maintained exact accepted domain enum values in P13 and T04. | **CLOSED** (re-review 1 accepted) |
| **FR-ROB-02** | **MEDIUM / OPEN** — finish S1 actual query/snapshot and S2 full-state witnesses | In T01 & T02:<br>• **S1 Query Instrumentation**: Advised Spring Data JPA repositories directly (`fictionRepository`, `filmRepository`, classification repos, `vaultEntryRepository`, `personRepository`, `creatorGroupRepository`) using Spring AOP advice to capture `pg_backend_pid()`, `pg_current_snapshot()::text`, `SHOW transaction_isolation`, `SHOW transaction_read_only` at real query boundaries. Asserted single snapshot identity across all reads, connection separation from outer transaction (`isNotEqualTo(outerPid)`), outer managed entity suspension (`innerContainsOuterEntity == false`), and clean outer transaction resumption with original PID and intact managed entity. Fiction Group branch fully instrumented with classification repos, snapshot witness, and outer entity suspension.<br>• **S2 Full-State & Classification Races**: Concurrent writers now commit classification changes (insert new archetype/genre, delete world setting), work Vault deletion, and full work row mutations. Held readers assert all 19 properties match initial state including exact initial classification sets (`Set.of(id + 3_000_000L)`). Fresh requests after eligibility loss return 404 with 0 calls to People/Group. Eligibility-preserving mutations assert all 19 updated properties and updated classification sets. | **SUBMITTED FOR RE-REVIEW** (G-ROB-FICTION: 12/12 PASS, G-ROB-FILM: 13/13 PASS) |
| **FR-ROB-03** | **MEDIUM / OPEN** — distinguish missing identity from mismatch and complete captured-set safeguards | In T01 & T02:<br>• **S4 Identity Separation**: Distinct test cases for missing work Vault (`Optional.empty()`) vs mismatched work Vault ID (`new VaultEntryView(id + 999_999L, ...)`). In Person Vault qualification, added missing Person Vault (`Optional.empty()`) and mismatched Person Vault ID (`new VaultEntryView(personId + 999_999L, ...)`). In reference absence, added injected schema-disallowed null Person name (`new PersonView(id, null, ...)`), null CreatorGroup name (`new CreatorGroupView(id, null, ...)`), and null Film director name.<br>• **S6 Captured-Set Safeguards**: Installed AOP advice counters on all repositories; positively asserted all query counters > 0 during `findPublicDetail`. Asserted exact expected non-empty sets on view and immutability (`UnsupportedOperationException`). Mapped to DTO outside transaction and serialized to JSON; asserted exact expected sets in DTO and JSON. Guard advice on repositories confirmed 0 queries post-return, paired with Mockito `verify(..., never()).find(...)`. | **SUBMITTED FOR RE-REVIEW** (G-ROB-FICTION: 12/12 PASS, G-ROB-FILM: 13/13 PASS) |
| **FR-ROB-04** | **MEDIUM / OPEN** — Group full-value parity is still missing | In `PublicFictionReadIntegrationTest.java` lines 280–298:<br>• Populated Group work fixture requested via PRIVATE GET.<br>• Asserted exact 18 keys (verifying absence of `authorName`).<br>• Asserted all 18 common values and classification sets match the PUBLIC response field-for-field (`for (String commonField : privGroupFields) assertThat(groupData.path(commonField)).isEqualTo(privGroupData.path(commonField));`). | **SUBMITTED FOR RE-REVIEW** (G-ROB-FICTION: 12/12 PASS) |
| **FR-ROB-05** | **MEDIUM / OPEN** — Acceptance assertion trace overstates source | • Corrected canonical file IDs: T03 = `ProfileRouteAuthorizationIntegrationTest.java`, T05 = `PublicFilmCreateIntegrationTest.java`.<br>• Accurately attributed PRIVATE POST/PUT key assertions to `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor` and `eligibleFilmHasExactlyNineteenKeysAndResolvedDirector`.<br>• Accurately described S6 repository advice counters, JSON array assertions, and `verify(..., never()).find(...)` guards without claiming `verifyNoMoreInteractions`.<br>• All cited methods, test counts, and hashes verified directly against source and Surefire XML. | **SUBMITTED FOR RE-REVIEW** (Reconciled trace in Handback & Self-Review) |

---

## 3. Exact Traceability Matrix: ROB-01 – ROB-22 to Actual Test Methods

| Criteria | Actual Test Method(s) Executed | Gate & Class | Status |
| --- | --- | --- | --- |
| **ROB-01** | `registeredRoutesDeniedToPublicTokenFromBothOrigins`, `openApiContractContainsAllRoutesAndUniqueOperationIds` | `G-ROB-SECURITY` (`ProfileRouteAuthorizationIntegrationTest`), `G-ROB-OPENAPI` (`OpenApiRouteInventoryIntegrationTest`) | **PASS** |
| **ROB-02** | `publicSessionsReachFictionDetail`, `publicSessionsReachFilmDetail`, `registeredRoutesDeniedToPublicTokenFromBothOrigins` | `G-ROB-FICTION`, `G-ROB-FILM`, `G-ROB-SECURITY` | **PASS** |
| **ROB-03** | `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor` (covers Person & Group authors, populated classifications, PRIVATE 18-key parity) | `G-ROB-FICTION` | **PASS** |
| **ROB-04** | `eligibleFilmHasExactlyNineteenKeysAndResolvedDirector` (covers director & absent director, populated classifications, PRIVATE 18-key parity) | `G-ROB-FILM` | **PASS** |
| **ROB-05** | `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor` (PRIVATE GET/POST/PUT 18 keys), `eligibleFilmHasExactlyNineteenKeysAndResolvedDirector` (PRIVATE GET/POST/PUT 18 keys), `privateProfileCreateRetainsFullEighteenKeyBehavior` (Film POST 18 keys) | `G-ROB-FICTION`, `G-ROB-FILM`, `G-ROB-PRIVATE` (`PublicFilmCreateIntegrationTest`) | **PASS** |
| **ROB-06** | `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor`, `qualifiesPersonVaultBeforeResolvingName` | `G-ROB-FICTION` | **PASS** |
| **ROB-07** | `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor`, `referenceAbsenceYieldsPresentNullName` | `G-ROB-FICTION` | **PASS** |
| **ROB-08** | `eligibleFilmHasExactlyNineteenKeysAndResolvedDirector`, `qualifiesDirectorPersonVaultBeforeResolvingName` | `G-ROB-FILM` | **PASS** |
| **ROB-09** | `qualifiesPersonVaultBeforeResolvingName`, `referenceAbsenceYieldsPresentNullName`, `qualifiesDirectorPersonVaultBeforeResolvingName` | `G-ROB-FICTION`, `G-ROB-FILM` | **PASS** |
| **ROB-10** | `ineligibleFictionReturnsFourHundredFourAndZeroPeopleAccess`, `ineligibleFilmReturnsFourHundredFourAndZeroPeopleAccess` | `G-ROB-FICTION`, `G-ROB-FILM` | **PASS** |
| **ROB-11** | `s5OperationalAndCompletionFailuresYieldSafe500` (injected work query, classification queries, Group read, Work Vault read, Person Vault read, and `beforeCommit` transaction completion failure) | `G-ROB-FICTION`, `G-ROB-FILM` | **PASS** |
| **ROB-12** | `s1PhysicalNewPathIsolationAndContextResumption`, `s6CompleteImmutableResultMaterializedBeforeReturn` | `G-ROB-FICTION`, `G-ROB-FILM` | **PASS** |
| **ROB-13** | `s2WorkEligibilityAndFullValuesSnapshotIsolation` | `G-ROB-FICTION`, `G-ROB-FILM` | **PASS** |
| **ROB-14** | `s3LinkedSourceSnapshotIsolation` | `G-ROB-FICTION`, `G-ROB-FILM` | **PASS** |
| **ROB-15** | `s1PhysicalNewPathIsolationAndContextResumption` | `G-ROB-FICTION`, `G-ROB-FILM` | **PASS** |
| **ROB-16** | `publicProfileSwitchAndNoStoreCachingControls` (200, 404, 500 conditional header checks) | `G-ROB-FICTION`, `G-ROB-FILM` | **PASS** |
| **ROB-17** | `openApiContractContainsAllRoutesAndUniqueOperationIds` (asserts exact 19-key PUBLIC and 18-key PRIVATE schemas) | `G-ROB-OPENAPI` | **PASS** |
| **ROB-18** | `openApiContractContainsAllRoutesAndUniqueOperationIds` (schema specimen validation, enum membership), `followUpB1GetOnCreatedFilmSucceedsWithIdenticalSevenKeys` (updated to 19 keys) | `G-ROB-OPENAPI`, `G-ROB-PRIVATE` (`PublicFilmCreateIntegrationTest`) | **PASS** |
| **ROB-19** | `registeredRoutesDeniedToPublicTokenFromBothOrigins` | `G-ROB-SECURITY` (`ProfileRouteAuthorizationIntegrationTest`) | **PASS** |
| **ROB-20** | `publicTokenDeniedToPostFilmWith403AndNoHandler`, `publicSessionsCannotCreateFilms`, `anonymousRequestsRejected` | `G-ROB-FILM`, `G-ROB-SECURITY` (`SecurityFilterChainIntegrationTest`) | **PASS** |
| **ROB-21** | `ApplicationArchitectureTests` (all 38 rules pass) | `G-ROB-ARCH` | **PASS** |
| **ROB-22** | Finite test execution receipts in `logs/bf26-dp-ro-b/remediation-round2/`, Surefire XML preserved, Docs Sync Check recorded | Full gate execution | **PASS** |

---

## 4. Finite Verification Receipts Summary

| Gate | Target / Test Selector | Fresh Tests Run | Result | Duration | Receipt Path |
| --- | --- | --- | --- | --- | --- |
| **G-ROB-FICTION** | `PublicFictionReadIntegrationTest` | 12 | **PASS (0 fail, 0 err)** | 65.61s | `logs/bf26-dp-ro-b/remediation-round2/G-ROB-FICTION.json` |
| **G-ROB-FILM** | `PublicFilmReadIntegrationTest` | 13 | **PASS (0 fail, 0 err)** | 83.13s | `logs/bf26-dp-ro-b/remediation-round2/G-ROB-FILM.json` |
| **G-ROB-SECURITY** | `ProfileRouteAuthorizationIntegrationTest`, `SecurityFilterChainIntegrationTest` | 27 (15 + 12) | **PASS (0 fail, 0 err)** | 86.63s | `logs/bf26-dp-ro-b/remediation-round2/G-ROB-SECURITY.json` |
| **G-ROB-PRIVATE** | `FictionWebIntegrationTest`, `FilmWebIntegrationTest`, `PublicFilmCreateIntegrationTest` | 23 (1 + 1 + 21) | **PASS (0 fail, 0 err)** | 64.11s | `logs/bf26-dp-ro-b/remediation-round2/G-ROB-PRIVATE.json` |
| **G-ROB-OPENAPI** | `OpenApiRouteInventoryIntegrationTest` | 1 | **PASS (0 fail, 0 err)** | 44.59s | `logs/bf26-dp-ro-b/remediation-round2/G-ROB-OPENAPI.json` |
| **G-ROB-ARCH** | `ApplicationArchitectureTests` | 38 | **PASS (0 fail, 0 err)** | 37.09s | `logs/bf26-dp-ro-b/remediation-round2/G-ROB-ARCH.json` |
| **TOTAL** | **6 finite gates** | **114** | **114 PASS (0 fail, 0 err)** | **381.16s** (cumulative wall time) | All Surefire XML reports preserved in subdirectories |

---

## 5. Residual Risk & Non-Goals

1. **Non-Goals Preserved:**
   - Zero production edits to Security, session, JPA repositories, People, or Vault.
   - Zero changes to database schema, Flyway migrations, or DBML.
   - No Frontend/BFF, RAG, or external service activation.
2. **Remediation Budget:**
   - Round 2 complete: remediation **2/3 consumed**.
   - Exactly one corrective round remains if needed. Takeover: **NOT_AUTHORIZED / NOT_USED**.

---

## 6. Pre-Handback State Consistency & Docs Sync Check

- **ACTIVE.md Status:** Transitioning to `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
- **Docs Sync Check:**
  - `UPDATED`: `ACTIVE.md`, `executable-handoff-dp-ro-b-2026-11-14.md` (section 9 hashes, section 14 for Round 2 submission), `self-review-dp-ro-b-round-2-2026-11-15.md`, `implementation-handback-dp-ro-b-round-2-2026-11-15.md`.
  - `ALREADY_CONSISTENT`: 18-path boundary, previous review records, frozen codebase, provider defaults OFF.
  - `DEFERRED_OUT_OF_SCOPE`: Root docs sync and closeout navigation (ChatGPT governance owner).
  - `NOT_VERIFIED`: Live production cutover, Owner-data suitability.

Next step: Codex | `$codex-final-review` | BF26-DP-RO-B Remediation Round 2, canonical handback docs/implementation/maintenance/2026-11-14-bf26-dp-ro-b-preparation/implementation-handback-dp-ro-b-round-2-2026-11-15.md | independent final re-review of FR-ROB-02–05 remainders | remediation 2/3 consumed; takeover NOT_AUTHORIZED / NOT_USED.
