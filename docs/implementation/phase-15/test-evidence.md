# Phase 15 Backend Audit Remediation — Test Evidence

- Date: 2026-10-08
- Implementer: Antigravity
- Handoff ID: `phase-15-backend-audit-remediation`
- Scope: Findings BA15-1 through BA15-17 (Phases 0–14 comprehensive backend audit remediation; Codex review findings FR15-1 through FR15-8; Closure audit remnants BA15-2, BA15-9, BA15-14, BA15-15)
- Environment:
  - Java: OpenJDK 25.0.2 (build 25.0.2+10-53)
  - Maven: 3.9.15
  - Spring Boot: 4.1.1 / Spring Modulith: 2.1.1
  - Database: PostgreSQL 18.6 (via Testcontainers `postgres:18.6-alpine`)

---

## 1. Verification Summary

- Command: `mvn -ntp clean verify`
- Working Directory: `backend/`
- Result: **BUILD SUCCESS**
- Preserved baseline: 920 tests
- Total test count: 1021 tests (101 net new focused audit regression tests covering BA15-1 through BA15-17, review findings FR15-1 through FR15-8, and closure audit remnants BA15-2, BA15-9, BA15-14, BA15-15 across 14 audit suites)
- Failures: 0
- Errors: 0
- Skipped: 0
- Build Duration: ~03:48 min

### Historical Discovery and Pre-Fix Evidence Classification
As recorded in the initial backend audit report ([docs/implementation/phase-15/reviews/2026-10-06-phase-15-backend-audit.md](reviews/2026-10-06-phase-15-backend-audit.md)), findings were discovered via code analysis, build/schema inspection, and source-derived interleaving reasoning rather than pre-existing automated failure runs:
- **Source-Derived Interleaving / Analytical Reasoning (No Pre-Fix Runtime Failure Captured):**
  - **BA15-9 (Location Concurrency):** Derived from source inspection of `LocationService` unversioned read/write versus `BusinessHoursService` FOR UPDATE. The initial audit explicitly stated: *"Source-derived interleaving... Not runtime-executed here."* No failing runtime artifact was captured prior to remediation.
  - **BA15-13 (Study / Account Invariant):** Derived from structural analysis of `ExternalAccountService` unconstrained mutation versus `StudyItemService` assignment-time check. The initial audit explicitly stated: *"Source-derived sequence... Not runtime-probed here."* No failing runtime artifact was captured prior to remediation.
  - **BA15-10 (Rating First Set):** Analytical derivation of race on non-existent rating row during concurrent initial set. No pre-fix failing trace was captured.
  - **BA15-11 (Location Category FK Race):** Analytical derivation of concurrent category deletion race between existence check and assignment. Tested in remediation via dynamic JDK proxy interceptor.
  - **BA15-17 (Lifecycle Writers Concurrency):** Analytical derivation of concurrent writer interleavings on Diary, Personal, and Feed entities without row-level write locks. No pre-fix failing trace was captured.
- **Source Code, Schema & Build Inspection (Directly Observable in Baseline Code):**
  - **BA15-1 (Flyway Startup):** Build file inspection showed missing `spring-boot-starter-flyway` in `backend/pom.xml`, with test runners relying on explicit manual migration calls in test fixtures.
  - **BA15-2 & BA15-15 (Web DTO & OpenAPI):** Source code inspection identified missing length constraints, missing default bounds, and documentation drift between DTOs and domain validators.
  - **BA15-3 (Password Length):** Spring Security specification and source analysis identified BCrypt's 72-byte truncation/exception behavior versus vault password requirements (12–128 characters).
  - **BA15-4 (Null Collection Members):** Request DTO source inspection identified collection fields lacking element-level `@NotNull` validation.
  - **BA15-5 (Media Negotiation):** Source inspection of `ApiExceptionHandler` identified unhandled media negotiation exception paths.
  - **BA15-6 (Public Knowledge Exceptions):** Source inspection of `FeedExceptionAdvice` and `ImportDataExceptionAdvice` identified absent translation handlers for public Knowledge exceptions.
  - **BA15-7 & BA15-8 (Media Streaming Framing):** Source inspection of `ImageController.downloadContent` and `MediaExceptionAdvice` identified stale header retention and metadata length prioritization over stream length.
  - **BA15-12 (Monetary Numeric Bounds):** Database schema inspection of `numeric(19,4)` columns versus domain service methods identified absent pre-persistence width and scale validation.
  - **BA15-14 (Import Pagination):** Source code inspection of `ImportJobOperations` and `ImportJobController` identified missing `page` offset parameter, limiting inspection to the initial window.
  - **BA15-16 (Collection Descriptors & Hygiene):** Directory tree inspection identified 10 missing `package-info.java` files and 2 stale `.gitkeep` files.
- **Post-Remediation Verification:** Rather than relying on uncaptured historical pre-fix runs, remediation implemented 14 dedicated audit suites executing 101 regression tests against containerized PostgreSQL and embedded MockMvc/HTTP wire environments to deterministically prove all corrected invariants.

---

## 2. Per-Finding Remediation and Test Evidence

### BA15-1: Boot-Managed Flyway Migration Integration
- **Issue:** The application relied on manual Flyway execution in test harnesses; production startup on a fresh PostgreSQL instance could bypass schema initialization before Hibernate validation.
- **Remediation:**
  - Added `org.springframework.boot:spring-boot-starter-flyway` to `backend/pom.xml`.
  - Configured Flyway in Spring Boot runtime properties to run before Hibernate `ddl-auto: validate`.
  - Removed manual Flyway test migrator invocation helper from `support/AbstractPostgresIntegrationTest.java` while preserving all existing migration SQL scripts (V1, V2) without modifications.
- **Changed Symbols:**
  - `backend/pom.xml`
  - `backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractPostgresIntegrationTest.java`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=FlywayStartupIntegrationTest` (2 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.FlywayStartupIntegrationTest`:
    - `freshStartupAppliesFlywayMigrationsAndValidatesSchema`: Verifies fresh PostgreSQL container bootstrap runs Flyway automatically via Spring Boot lifecycle without any test harness migrator, applies V1 and V2 migrations, validates `flyway_schema_history`, and verifies Hibernate validation succeeds.
    - `restartAgainstMigratedDatabaseSucceeds`: Starts a second child Spring Application Context via `SpringApplicationBuilder.run(...)` against the already-migrated database without re-running migrations, confirming existing schema validation succeeds. (Note: the primary test context remains managed by the runner while the child context demonstrates cold start against an existing schema).

---

### BA15-2 & BA15-15: Web DTO Validation Alignment Across Modules and OpenAPI Documentation (including FR15-4, FR15-5, FR15-7, FR15-8, and Closure Audit Remnants)
- **Issue:** Web request DTOs had validation drift from owning domain contracts (missing string length limits, inconsistent nullable/blank handling, missing default bounds). Feed's Knowledge-conversion request DTOs lacked 500-character bounds and exact blank Markdown preservation. The closure audit identified remaining drift: Location name/phone bounds (500/64), snapshot filename/display bounds (500), relationship follow status default (`UNKNOWN`), nullable follower status, default source (`MANUAL`), uncapped owner note (>2048 chars), optional Feed source URL, Personal profile email max 320 free-form syntax and blank nationality normalization, Shopping/Software/Feed-conversion currency code normalization and padded name whitespace trimming, and missing OpenAPI descriptions for recurring rule ledger cardinality/signs/distinct wallets/categories and Software required `type`.
- **Remediation:**
  - Aligned Web DTO annotations with domain validators across all affected modules: Account, Media, Location, Knowledge, Collection, Journal, Personal, Feed, ImportData, Finance, Settings, and Film/Fiction.
  - Location: Added `@Size(max = 500)` to `name` and `@Size(max = 64)` to `phone` in `CreateLocationRequest` and `UpdateLocationRequest`, with compact constructor whitespace trimming for name and `phone = (phone == null || phone.isBlank()) ? null : phone.trim()`, matching `LocationService.trimIfPresent` check order (normalizing Unicode and ASCII blanks to null before length checks).
  - Account Follower Snapshots: Added `@Size(max = 500)` to `importedFileName` in `CreateFollowerSnapshotRequest` and to `displayNameSnapshot` in `CreateFollowerSnapshotEntryRequest`, with `trimOrNull` normalization matching `FollowerSnapshotService` (retaining historical values like `\u2003` and preserving conflicting duplicate checks).
  - Account Relationships: In `SetExternalAccountRelationshipRequest`, made `followerStatus` and `followStatus` optional, mapped null `followStatus` to default `UNKNOWN` in owning service and `source` to `MANUAL` while `followerStatus` remains nullable, uncapped `note` (>2048 chars accepted and stored without arbitrary validation bounds), and normalized `note` matching `ExternalAccountRelationshipService.trimOrNull` (retaining `\u2003`).
  - Feed Sources: In `CreateFeedSourceRequest` and `UpdateFeedSourceRequest`, made `feedUrl` optional; DTO `trimOrNull` normalization precedes Bean Validation `@Size(max = 2048)`, and retained Unicode reaches the owner's trimmed-length check before `isBlank`/null storage normalization.
  - Personal Profiles: In `CreatePersonalProfileRequest` and `UpdatePersonalProfileRequest`, relaxed `email` from `@Email` to free-form `@Size(max = 320)` (accepting domain-valid non-standard formats), and normalized blank `nationalityCode` to null.
  - Currency Code & Name Trimming: In `CreateShoppingItemRequest`, `UpdateShoppingItemRequest`, `CreateSoftwareItemRequest`, `UpdateSoftwareItemRequest`, and `ConvertToStudyRequest`, constrained `currencyCode` with `@Size(max = 3)` and normalized blank values using `trimOrNull` matching owning services (retaining unknown Unicode currencies to be rejected with 422 by domain validation, and requiring currency when price is present); added compact constructor whitespace trimming to Shopping and Software names and Study titles.
  - Feed Knowledge-conversion DTOs: Added `@Size(max = 500)` to `title` and `currentProgressText` in `ConvertToStudyRequest`; added `@Size(max = 500)` to `title` and `sourceName` in `ConvertToInformationRequest`; added `@Size(max = 500)` to `title`, `sourceName`, and `importedFileName` in `ConvertToNoteRequest`.
  - Blank Markdown Preservation: Allowed non-null blank `markdown` in `ConvertToNoteRequest`, `CreateDiaryEntryRequest`, `UpdateDiaryEntryRequest`, `CreateKnowledgeNoteRequest`, and `UpdateKnowledgeNoteRequest`, matching owning domain invariants.
  - OpenAPI Descriptions:
    - Recurring transaction rules: Documented ledger cardinality, sign constraints, and category rules on `transactionType`, `categoryId`, and `entries` in `CreateRecurringTransactionRuleRequest` and `UpdateRecurringTransactionRuleRequest`.
    - Recurring rule entries: Documented `walletId` (participating wallet; distinct across entries) and `amountDelta` (sign matches transaction type) in `RecurringRuleEntryRequest`.
    - Software: Added `@Schema(requiredMode = Schema.RequiredMode.REQUIRED)` on `type` in `CreateSoftwareItemRequest` and `UpdateSoftwareItemRequest`.
    - Documented transaction update matching create, description max 1000, recurring rule name 500 / description 1000, subscription name 500 / provider 500, Shopping status default WISHLIST, and `/api/v1/search` GET operation literal/trigram/tag semantics.
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.location.internal.web.dto.CreateLocationRequest`
  - `com.vhvkhangg.personalprivatevault.location.internal.web.dto.UpdateLocationRequest`
  - `com.vhvkhangg.personalprivatevault.account.internal.web.dto.CreateFollowerSnapshotRequest`
  - `com.vhvkhangg.personalprivatevault.account.internal.web.dto.CreateFollowerSnapshotEntryRequest`
  - `com.vhvkhangg.personalprivatevault.account.internal.web.dto.SetExternalAccountRelationshipRequest`
  - `com.vhvkhangg.personalprivatevault.feed.internal.web.dto.CreateFeedSourceRequest`
  - `com.vhvkhangg.personalprivatevault.feed.internal.web.dto.UpdateFeedSourceRequest`
  - `com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToStudyRequest`
  - `com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToInformationRequest`
  - `com.vhvkhangg.personalprivatevault.feed.internal.web.dto.ConvertToNoteRequest`
  - `com.vhvkhangg.personalprivatevault.personal.internal.web.dto.CreatePersonalProfileRequest`
  - `com.vhvkhangg.personalprivatevault.personal.internal.web.dto.UpdatePersonalProfileRequest`
  - `com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateShoppingItemRequest`
  - `com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateShoppingItemRequest`
  - `com.vhvkhangg.personalprivatevault.collection.internal.web.dto.CreateSoftwareItemRequest`
  - `com.vhvkhangg.personalprivatevault.collection.internal.web.dto.UpdateSoftwareItemRequest`
  - `com.vhvkhangg.personalprivatevault.finance.internal.web.dto.CreateRecurringTransactionRuleRequest`
  - `com.vhvkhangg.personalprivatevault.finance.internal.web.dto.UpdateRecurringTransactionRuleRequest`
  - `com.vhvkhangg.personalprivatevault.finance.internal.web.dto.RecurringRuleEntryRequest`
  - `com.vhvkhangg.personalprivatevault.journal.internal.web.dto.CreateDiaryEntryRequest`
  - `com.vhvkhangg.personalprivatevault.journal.internal.web.dto.UpdateDiaryEntryRequest`
  - `com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.CreateKnowledgeNoteRequest`
  - `com.vhvkhangg.personalprivatevault.knowledge.internal.web.dto.UpdateKnowledgeNoteRequest`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=WebDtoValidationAuditIntegrationTest` (25 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.WebDtoValidationAuditIntegrationTest`:
    - `locationNameAndPhoneCreateAndUpdateBoundaries`: Using raw wire JSON requests (bypassing pre-normalized DTO objects), verified Location name 500 valid / 501 rejected with 400; phone 64 valid / 65 rejected with 400 on both POST and PUT routes; verified Unicode blank phone (65 `\u2003` characters) normalized to null and accepted; verified `locations` and `vault_entries` table counts remain unchanged on rejected POSTs; verified database assertions proving prior name and phone remain unchanged on rejected PUTs.
    - `followerSnapshotFileNameAndDisplayNameBoundaries`: Using raw wire JSON requests, verified follower snapshot `importedFileName` 500 valid / 501 -> 400, entry `displayNameSnapshot` 500 valid / 501 -> 400; verified exact historical value retention (`\u2003`); verified `follower_snapshots` and `follower_snapshot_entries` table counts remain unchanged on 400 and 422 rejections; verified conflicting duplicate historical display copies rejection (422 `INVALID_SNAPSHOT`); verified matching duplicate copies collapse to 1 entry asserting exact retained display `\u2003` via fresh HTTP GET.
    - `accountRelationshipOptionalDefaultsAndUncappedNoteBoundaries`: Using raw wire JSON requests, verified relationship with explicit null `followerStatus`, `followStatus`, and `source`: `followStatus` defaults to `UNKNOWN`, `source` defaults to `MANUAL`, `followerStatus` remains null; verified uncapped `note` exceeding 2048 characters is successfully stored and retrieved; verified exact `\u2003` note retention.
    - `feedSourceOptionalUrlBoundaries`: Using raw wire JSON requests, verified Feed source creation and update with null and blank `feedUrl` succeeds and normalizes to null; verified oversized Unicode blank URL (2049 `\u2003` characters) rejected with 400 `VALIDATION_ERROR` on POST and PUT; verified `feed_sources` table count remains unchanged on rejected POST; verified existing name and `feed_url` remain unchanged in database on rejected PUT.
    - `personalProfileFreeFormEmailAndNationalityBoundaries`: Using raw wire JSON requests, verified free-form email max 320 characters without RFC format restriction is accepted on POST and PUT; blank `nationalityCode` normalized to null; verified `personal_profiles` table count remains unchanged on 400 and 422 rejections.
    - `shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity`: Using raw wire JSON requests for Shopping, Software, and Feed Study conversions; seeded non-null valid currency (`"VND"`) on existing Shopping and Software items; verified real replacement/clearing to `null` via subsequent PUTs with `null`, `" "` (1 ASCII space), and `"   "` (multiple ASCII spaces) without price, verified via fresh HTTP GET that `currencyCode` is cleared to `null`; verified negative PUTs (price without currency, unknown Unicode currency, 501-character name) fail with 422 or 400 while fresh GET and DB queries prove prior name, price, and currency remain unchanged; verified rejected Location and collection (Shopping, Software) creates leave their owning and `vault_entries` counts unchanged; verified conversion failures leave `study_items`, `vault_entries`, and `saved_resource_conversions` counts unchanged; verified raw ASCII-padded names max 500 valid / 501 rejected with 400 on create and update routes; and verified public HTTP Study reads asserting normalized title and currency.
    - `convertToStudyRequestBoundaries`: Verified true maxima (500 chars accepted) and maxima + 1 (501 chars rejected with HTTP 400 `VALIDATION_ERROR`) for `title` and `currentProgressText`.
    - `convertToInformationRequestBoundaries`: Verified 500 chars accepted and 501 chars rejected for `title` and `sourceName`.
    - `convertToNoteRequestBoundariesAndBlankMarkdown`: Verified exact blank Markdown content preservation, and 500/501 bounds for `title` and `importedFileName`.
    - `feedNoteConversionIndependentMaxPlusOneRejections`: Independently tested and verified HTTP 400 rejections for `title` 501, `sourceName` 501, and `importedFileName` 501.
    - `journalDiaryEntryValidationBoundaries`: Verified 500-char title boundary, non-null blank Markdown preservation, and null Markdown rejection on create and update routes.
    - `knowledgeNoteValidationBoundaries`: Verified 500-char title/source/file boundaries, non-null blank Markdown preservation, and null Markdown rejection on create and update routes.
    - `personalProfileValidationBoundaries`: Verified name max 255 valid, 256 rejected; relationship max 100 valid; blank `notesMarkdown` preserved.
    - `personalProfileCreateAndUpdateBoundaries`: Verified `phone` 64 valid, 65 rejected with 400; `email` 320 valid, 321 rejected with 400 on both POST and PUT routes.
    - `accountIdentityAlternativesAndBounds`: Verified identity alternatives on POST and PUT `/api/v1/accounts` (only username valid 201/200, only externalId valid 201/200, only url valid 201/200); neither provided rejected with HTTP 422 `INVALID_EXTERNAL_ACCOUNT`; `displayName` 500 valid / 501 rejected with 400 on POST and PUT; `ownerName` 500 valid / 501 rejected with 400 on POST and PUT.
    - `imageNullableMetadataAndBounds`: Verified POST and PUT `/api/v1/images` with all nullable metadata null (201/200); `title` 500 valid / 501 -> 400; `imageType` 100 valid / 101 -> 400; `locationText` 500 valid / 501 -> 400 on POST and PUT.
    - `addressNullableFieldsAndBounds`: Verified POST and PUT `/api/v1/addresses` with null locality/streetAddress (201/200); `addressType` 100 valid / 101 -> 400; `postalCode` 32 valid / 33 -> 400 on POST and PUT.
    - `knowledgeDirectDtosBoundaries`: Verified Study (`title` 500/501, `currentProgressText` 500/501 on POST and PUT), Information (`title` 500/501, `sourceName` 500/501 on POST and PUT), Vocabulary (`word` 500/501, `pronunciation` 500/501, `partOfSpeech` 100/101, `sourceName` 500/501 on POST and PUT), and independent 501 field rejections on Note create and update.
    - `collectionMusicOmittedVersionDefaultsAndBounds`: Verified POST `/api/v1/collection/music` with `version == null` defaults to `ORIGINAL`, `title` 500 valid / 501 -> 400; PUT `/api/v1/collection/music/{id}` with `version == null` defaults to `ORIGINAL`, `title` 500 valid / 501 -> 400.
    - `shoppingSoftwareAndAlbumBoundaries`: Verified Shopping create/update name 500 valid / 501 -> 400; Software create/update name 500 valid / 501 -> 400; Album create/update title 500 valid / 501 -> 400.
    - `fictionFilmGenresAndLocationCategoriesBoundaries`: Verified Fiction genre create/update name 150 valid / 151 -> 400; Film genre create/update name 150 valid / 151 -> 400; Location category create/update name 150 valid / 151 -> 400.
    - `manualSavedResourceAndImportJobCreationBoundaries`: Verified manual SavedResource creation title 1000 valid / 1001 -> 400; author, sourceName, externalId 500 valid / 501 -> 400 independently; Import job creation originalFileName 500 valid / 501 -> 400.
    - `financeRecurringAndSubscriptionBoundaries`: Verified recurring transaction rule create/update name 500 valid / 501 -> 400; description 1000 valid (asserting 200 OK and full 1000-character description retained on both POST and PUT) / 1001 -> 400 (null and blank accepted); Subscription create/update name 500 valid / 501 -> 400, provider 500 valid / 501 -> 400; Financial transaction description 1000 valid (asserting 200 OK and full 1000-character description retained on both POST and PUT) / 1001 -> 400 (null and blank accepted).
    - `settingsUpdateBoundariesAndRemovedCaps`: Verified Settings update values accepted above removed caps (paginationSize 200 > 100, autoLock 2000 > 1440, backupInterval 10000 > 8760); positive bounds enforced (0 rejected with 400); timezone valid accepted / 65 rejected with 400.
    - `openApiGeneratedSchemaSemanticsConfirmCorrectedContracts`: Verified generated OpenAPI schema semantics (`/v3/api-docs`): Shopping (full replacement/null-clearing, omitted status defaults to WISHLIST, name max 500); Software (full replacement/null-clearing, required type APPLICATION or EXTENSION, name max 500); Finance (`UpdateFinancialTransactionRequest` descriptions matching create, description max 1000, recurring rule name 500 / description 1000, recurring rule entry descriptions, subscription name 500 / provider 500); Fiction, Film, Location genres/categories name max 150; Media Album title max 500; SavedResource title 1000, author 500, sourceName 500, externalId 500; Import job originalFileName 500; Settings timezone max 64, paginationSize min 1 without max, privateModeAutoLockMinutes min 1 without max, backupIntervalHours min 1 without max; Knowledge Information title 500 / sourceName 500; Knowledge Vocabulary word 500 / pronunciation 500 / partOfSpeech 100 / sourceName 500.

---

### BA15-3: Full-Input Password Encoding with Legacy BCrypt Verification
- **Issue:** BCrypt in standard Spring Security throws an `IllegalArgumentException` on password inputs exceeding 72 bytes rather than silently truncating. The specification permits passwords up to 128 characters.
- **Remediation:**
  - Configured `DelegatingPasswordEncoder` in `SecurityConfiguration` with `pbkdf2` as the default encoder for all newly encoded passwords.
  - Retained `bcrypt` in the encoder map to maintain 100% backward compatibility for existing BCrypt hashes.
  - Enforced 12–128 character password boundary validation at the authentication contract level without truncation.
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.authentication.internal.infrastructure.security.SecurityConfiguration`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=PasswordRangeIntegrationTest` (8 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.PasswordRangeIntegrationTest`:
    - Tested ASCII passwords of lengths 12, 72, 73, and 128 characters.
    - Verified 72- and 73-character passwords produce distinct PBKDF2 hashes without truncation.
    - Verified below-minimum password (< 12 characters) via HTTP bootstrap returns HTTP 422 `AUTH_INVALID_BOOTSTRAP`. (Note: the system is permanently single-user with a one-time bootstrap; no separate user registration API exists).
    - Verified above-maximum password (> 128 characters) is rejected with HTTP 400 `VALIDATION_ERROR`.
    - Verified multi-byte UTF-8 inputs (> 72 bytes) are fully distinguished without truncation.
    - Verified legacy BCrypt hash authentication continues to work seamlessly against real user records.
    - Preserved PIN, JWT, and bootstrap privacy and credential behavior (confirmed by `AuthenticationWebIntegrationTest` and `PasswordRangeIntegrationTest`).

---

### BA15-4: Collection List Request DTOs Null Member Rejection
- **Issue:** Batch and list request DTOs accepted lists containing `null` elements, which produced unhandled 500 errors in service mappers.
- **Remediation:**
  - Added `@NotNull` collection element validation (`List<@NotNull @Valid ...>`) across Import decisions, Account snapshots, Finance transactions/rules, and Location schedules.
  - Preserved legitimate empty list and null list semantics (e.g. unknown schedule).
- **Changed Symbols:**
  - List DTOs in `importdata`, `account`, `finance`, and `location`.
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=NullCollectionMemberValidationTest` (5 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.NullCollectionMemberValidationTest`:
    - Tested with authentic, valid fixtures: valid account ID, ISO-8601 UTC `capturedAt`, valid `source`, valid `transactionType`, valid `dayOfMonth`, and valid schedule intervals.
    - Verified canonical HTTP 400 `VALIDATION_ERROR` responses with precise field targeting when a list contains a `null` item:
      - `ExecuteImportJobRequest`: targeting `itemDecisions[0]`, verified job status remains `VALIDATED`, `imported_items == 0`, and no target entries written.
      - `CreateFollowerSnapshotRequest`: targeting `entries[0]`, verified no snapshot rows written.
      - `CreateFinancialTransactionRequest`: targeting `entries[0]`, verified scalar control writes rows while null member rejected with unchanged persisted state in `financial_transactions` and `financial_transaction_entries`.
      - `CreateRecurringTransactionRuleRequest`: targeting `entries[0]`, verified rule name/description and entries in `recurring_rule_entries` remain unchanged.
      - `ReplaceBusinessHoursScheduleRequest`: targeting `intervals[0]`, verified row count in `location_business_hours` is zero.
    - Verified empty lists, null schedules, and valid non-null lists are accepted, with valid control lists writing rows.

---

### BA15-5: Narrow HTTP 415 and 406 Media Negotiation Handling (including FR15-3)
- **Issue:** Unsupported `Content-Type` and `Accept` headers returned generic 500 or empty 406 errors without structured JSON envelopes. FR15-3 required producing complete canonical JSON for unsupported Accept rather than failing exception-handler negotiation, and declaring 415/406 in OpenAPI schemas.
- **Remediation:**
  - In `ApiExceptionHandler`, implemented `handleMediaTypeNotSupported` returning HTTP 415 with structured `ApiResponse` JSON envelope.
  - In `ApiExceptionHandler`, implemented `handleMediaTypeNotAcceptable` directly writing JSON payload with UTF-8 character encoding and `application/json` Content-Type to `HttpServletResponse.getWriter()`, preventing secondary negotiation failure.
  - Added OpenAPI 415 and 406 response documentation in `OpenApiConfiguration`.
  - Preserved committed stream abort semantics (`abortIfCommitted`).
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.ApiExceptionHandler`
  - `com.vhvkhangg.personalprivatevault.OpenApiConfiguration`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=HttpMediaTypeNegotiationIntegrationTest` (4 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.HttpMediaTypeNegotiationIntegrationTest`:
    - Verified 415 Unsupported Media Type with canonical JSON envelope for invalid `Content-Type`.
    - Verified 406 Not Acceptable with canonical JSON envelope when client requests `Accept: text/xml`.
    - Verified standard `application/json` requests succeed.
    - Verified streaming error handling does not corrupt committed responses.

---

### BA15-6: Feed and Import Safe Translation of Public Knowledge Exceptions
- **Issue:** Feed and ImportData exception advices were not translating public Knowledge exceptions (`InvalidKnowledgeItemException`, `KnowledgeConflictException`), potentially causing 500 errors on cross-module validation failures.
- **Remediation:**
  - Added handlers in `FeedExceptionAdvice` and `ImportDataExceptionAdvice` for public Knowledge exceptions.
  - Mapped `InvalidKnowledgeItemException` to HTTP 422 `INVALID_KNOWLEDGE_ITEM`, and `KnowledgeConflictException` to HTTP 409 `KNOWLEDGE_CONFLICT`.
  - Ensured clean transactional rollback and prevention of partial target/provenance writes during failed import job execution.
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.feed.internal.web.advice.FeedExceptionAdvice`
  - `com.vhvkhangg.personalprivatevault.importdata.internal.web.advice.ImportDataExceptionAdvice`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=FeedImportKnowledgeExceptionIntegrationTest` (4 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.FeedImportKnowledgeExceptionIntegrationTest`:
    - `duplicateNoteHashTranslatesTo409WithFullRollback`: Feed Note conversion duplicate hash translates to HTTP 409 `KNOWLEDGE_CONFLICT`; verified `vault_entries`, `notes`, and `saved_resource_conversions` counts remain unchanged.
    - `invalidAuthorCombinationTranslatesTo422WithFullRollback`: Feed Study conversion invalid author combination translates to HTTP 422 `INVALID_KNOWLEDGE_ITEM`; verified `vault_entries`, `study_items`, and `saved_resource_conversions` counts remain unchanged.
    - `importJobExecutionKnowledgeConflictTriggers409AndRollsBack`: Intra-job duplicate hash triggers HTTP 409 `KNOWLEDGE_CONFLICT`; verified all-or-nothing rollback on target notes, `vault_entries`, and job provenance (`status == VALID`, `importedVaultEntryId == null`).
    - `importJobExecutionInvalidKnowledgeItemTriggers422AndRollsBack`: Import job execution failure with invalid author combination translates to HTTP 422 `INVALID_KNOWLEDGE_ITEM`; verified complete rollback across all items and target entities.

---

### BA15-7 & BA15-8: Media Streaming Framing and Error Boundary Guard (including FR15-6, FR15-8)
- **Issue:**
  - BA15-7: Streaming error handling left stale binary headers (`Content-Type`, `Content-Length`) before writing JSON error envelopes on early failures, or attempted to append JSON after response commitment.
  - BA15-8: Media download header framing prioritized database metadata length over actual storage provider stream length, and failed to recognize provider length of zero (`providerLength >= 0`).
- **Remediation:**
  - In `MediaExceptionAdvice`, cleared binary headers before writing canonical JSON error envelopes when the response is not yet committed; if committed, the stream aborts cleanly without appending trailing JSON error payloads.
  - In `ImageController.downloadContent`, prioritized known provider stream length (`providerLength >= 0`, including 0) over database metadata size. Metadata length is used only as fallback when provider length is negative/unknown.
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.media.internal.web.controller.ImageController`
  - `com.vhvkhangg.personalprivatevault.media.internal.web.advice.MediaExceptionAdvice`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=MediaBinaryFramingWireIntegrationTest` (8 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.MediaBinaryFramingWireIntegrationTest`:
    - Real embedded Tomcat HTTP wire test verifying zero-length binary framing sets `Content-Length: 0`.
    - Verified downloads after metadata edits (smaller, larger, or null) return all original provider bytes with exact provider length.
    - Verified unknown provider length falls back to metadata size.
    - Verified pre-commitment storage exception during first read (with binary headers already set, for buffers both smaller and larger than the error JSON payload) clears binary headers and returns complete canonical HTTP 500 `INTERNAL_ERROR` JSON envelope.
    - Verified post-commitment stream abort cleanly disconnects and releases resources without trailing JSON.
    - Verified raw client socket disconnect cleanup releases streams and resources cleanly.

---

### BA15-9: Location Scalar and Schedule Mutation Concurrency (including Closure Audit Remnant)
- **Issue:** Concurrent updates to scalar fields and business hours schedules could lead to lost updates or stale entity state overwrites. The closure audit identified that `BusinessHoursService.getSchedule` under share lock retained stale managed entity flags when Location was already loaded in the persistence context, combining stale flags with current intervals.
- **Remediation:**
  - Added pessimistic write locking (`findByIdForUpdate`) and explicit `entityManager.refresh` in `LocationService` and `BusinessHoursService` during mutation workflows.
  - In `BusinessHoursService.getSchedule`, added explicit `entityManager.refresh(location)` under the acquired share lock (`findByIdForShare`) before evaluating `location.isBusinessHoursKnown()` and returning the schedule, guaranteeing fresh state after any prior managed read in the same transaction.
  - Converted concurrency test workers to `ExecutorService` and `Future<?>`, eliminating raw thread `.join()` calls and propagating all worker exceptions.
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.location.internal.application.LocationService`
  - `com.vhvkhangg.personalprivatevault.location.internal.application.BusinessHoursService`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=LocationConcurrencyIntegrationTest` (7 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.LocationConcurrencyIntegrationTest`:
    - Tested concurrent workers mutating scalar fields and schedule intervals concurrently using Testcontainers PostgreSQL.
    - Verified deterministic database lock observation via `awaitCompetingLock`.
    - Verified prior managed read in the same transaction is refreshed under write lock.
    - Verified prior managed reader during unknown-to-known writer contention serializes coherent state (`priorManagedReaderDuringUnknownToKnownWriterContentionSerializesCoherentState`): reader acquires share lock and refreshes location, observing coherent `businessHoursKnown == true` alongside intervals.
    - Verified prior managed reader during known-to-unknown writer contention serializes coherent state (`priorManagedReaderDuringKnownToUnknownWriterContentionSerializesCoherentState`): reader acquires share lock and refreshes location, observing coherent `businessHoursKnown == false` and empty intervals.
    - Verified both commit orders serialize correctly with all scalar fields and schedule intervals preserved.
    - Verified unknown schedule transition correctly deletes interval rows and clears the `business_hours_known` flag.

---

### BA15-10: Vault Metadata Rating Concurrent First Set Atomic Upsert
- **Issue:** Concurrent first set of a vault item rating resulted in duplicate key constraint violations rather than atomic upsert.
- **Remediation:**
  - Added atomic upsert method in `RatingRepository.upsert` (package `com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence`) targeting table `ratings` with conflict key `vault_entry_id` using native PostgreSQL `INSERT ... ON CONFLICT (vault_entry_id) DO UPDATE`.
  - Updated `VaultMetadataService.setRating` to use atomic upsert, preserving `createdAt` timestamps and advancing `updatedAt`.
  - Replaced raw thread `.join()` in test suite with `ExecutorService` and `Future<?>`.
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.vault.internal.infrastructure.persistence.RatingRepository`
  - `com.vhvkhangg.personalprivatevault.vault.internal.application.metadata.VaultMetadataService`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=VaultRatingFirstSetConcurrencyIntegrationTest` (3 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.VaultRatingFirstSetConcurrencyIntegrationTest`:
    - Multithreaded barrier test executing concurrent first-set rating operations on PostgreSQL using `awaitCompetingLock`.
    - Verified exactly one row exists in `ratings` table.
    - Captured and verified original `createdAt` timestamp is preserved identically (`currentCreatedAt.equals(originalCreatedAt)`), while `updatedAt` advances (`currentUpdatedAt.isAfter(originalUpdatedAt)`).
    - Verified subsequent update and clear operations maintain timestamp invariants.

---

### BA15-11: Location Category Assignment Reference Validation and Race Handling
- **Issue:** Category assignment did not pre-validate category reference existence and could surface unhandled database constraint exceptions on concurrent duplicate assignments or category deletion races.
- **Remediation:**
  - Added explicit validation in `LocationCategoryService` ensuring both Location and Category exist before assignment.
  - Handled FK removal races: when a category is deleted between existence check and INSERT, the resulting `DataIntegrityViolationException` is caught and translated to `LocationCategoryNotFoundException` with clean message, returning canonical HTTP 404 without leaking vendor/constraint details.
  - Added race-safe handling for concurrent duplicate category assignment, returning clean idempotent success.
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.location.internal.application.LocationCategoryService`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=LocationCategoryAssignmentIntegrationTest` (5 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.LocationCategoryAssignmentIntegrationTest`:
    - Verified non-existent category yields 404 `LocationCategoryNotFoundException`.
    - Verified non-existent location yields 404 `LocationNotFoundException`.
    - Verified concurrent assignment of the same category completes idempotently without unhandled 500 errors.
    - Verified actual FK removal race (exercised via dynamic JDK proxy intercepting existence check and deleting category before repository flush within a constructed service/transaction) translates cleanly to `LocationCategoryNotFoundException` with zero private constraint leakage, tested separately from direct HTTP requests for already-missing categories.
    - Verified separate HTTP request for already-missing category returns 404.

---

### BA15-12: Monetary numeric(19,4) Width and Scale Pre-Persistence Validation
- **Issue:** Floating-point or decimal inputs with excessive scale (> 4) or width (> 15 integer digits) caused database overflow or truncation errors.
- **Remediation:**
  - Added pre-persistence monetary validation in Brand, Location, Study, Shopping, and Software domain services.
  - Rejects scale > 4 and integer digits > 15 before persistence without rounding or silent truncation.
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.location.internal.application.BrandService`
  - `com.vhvkhangg.personalprivatevault.location.internal.application.LocationService`
  - `com.vhvkhangg.personalprivatevault.knowledge.study.internal.application.StudyItemService`
  - `com.vhvkhangg.personalprivatevault.collection.shopping.internal.application.ShoppingService`
  - `com.vhvkhangg.personalprivatevault.collection.software.internal.application.SoftwareService`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=PriceNumericBoundsIntegrationTest` (6 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.PriceNumericBoundsIntegrationTest`:
    - Tested HTTP create and update variants for scale (> 4) and width (> 15 integer digits) rejections across Brand (`minPrice` and independent `maxPrice`), Location (`minPrice` and independent `maxPrice`), Study, Shopping, and Software.
    - Verified exact persistence and reload equality of MAX_VALID bounds (`999999999999999.9999`) and FOUR_DECIMALS (`10.1234`) via `operations.findById` after HTTP updates.

---

### BA15-13: YouTube Study / External Account Invariant Preservation (including FR15-1, FR15-7, FR15-8)
- **Issue:** Study items reference YouTube external accounts. Interleaved updates could cause stale managed reads, broken invariants, or writer deadlocks.
- **Architectural Decision & Implementation:**
  - Preserved unidirectional module dependency: Knowledge -> Account. Account has ZERO dependencies on Knowledge.
  - Account exposes public SPI `ExternalAccountMutationGuard` in package `com.vhvkhangg.personalprivatevault.account.account`.
  - Knowledge implements `StudyExternalAccountGuard` in `com.vhvkhangg.personalprivatevault.knowledge.study.internal.application` registered as a Spring bean.
  - In `ExternalAccountService.update`, pessimistic lock (`findByIdForUpdate`) is acquired on the Account and `entityManager.refresh(account)` is called under lock before evaluating registered mutation guards.
  - In `ExternalAccountService.findAndLock`, pessimistic lock is acquired and `entityManager.refresh(account)` is called under lock.
  - In `StudyItemService.validateStudyItem`, unlocked fallback was eliminated: `findAndLock` is strictly required.
  - Deterministic lock ordering: Both Account mutations and Study assignments acquire row-level locks on `external_accounts` before evaluating cross-module invariants, specifically serializing the exercised bidirectional contention orders between Account and Study writers so competing transactions block and either commit or safely roll back without invariant corruption.
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.account.account.ExternalAccountMutationGuard`
  - `com.vhvkhangg.personalprivatevault.account.account.ExternalAccountOperations`
  - `com.vhvkhangg.personalprivatevault.account.internal.application.ExternalAccountService`
  - `com.vhvkhangg.personalprivatevault.knowledge.study.internal.application.StudyExternalAccountGuard`
  - `com.vhvkhangg.personalprivatevault.knowledge.study.internal.application.StudyItemService`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=StudyAccountInvariantIntegrationTest` (12 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.StudyAccountInvariantIntegrationTest`:
    - Replaced all raw `Thread` and `.join()` calls with `ExecutorService` and `Future<?>`, ensuring all worker exceptions are caught and asserted.
    - Verified 4 deterministic competing writer contention tests using `awaitCompetingLock` observing ungranted row locks in `pg_locks`:
      - `competingWritersAccountTypeMutationHoldsLockVsConcurrentStudyCreateBlocksAndRollsBack`
      - `competingWritersAccountPlatformMutationHoldsLockVsConcurrentStudyUpdateBlocksAndRollsBack`
      - `competingWritersStudyCreateHoldsAccountLockVsConcurrentAccountMutationToOtherBlocksAndRollsBack`
      - `competingWritersStudyUpdateHoldsAccountLockVsConcurrentAccountMutationPlatformBlocksAndRollsBack`
    - Verified stale managed reads in transaction are refreshed under write lock.
    - Verified unreferenced accounts can be updated freely without obstruction.
    - Verified Spring Modulith module boundaries and cycle checks pass without violations.

---

### BA15-14: Bounded Pagination for Import Job Items Review (including Closure Audit Remnant)
- **Issue:** Import review endpoint originally exposed only the first bounded window (default 50, selectable up to 100 via limit) without page offset parameters, leaving items beyond index 99 unreachable for inspection before execution. The closure audit identified that extreme accepted page values (`page * limit > Integer.MAX_VALUE`) caused integer overflow and triggered JPA `InvalidDataAccessApiUsageException` returning HTTP 500 `INTERNAL_ERROR`.
- **Remediation:**
  - Added bounded pagination to `ImportJobOperations.findJobItems`, `ImportJobService`, and `ImportJobController.findItems`.
  - Added `page` parameter (default 0, min 0) with `limit` capped at 100 (`1 <= limit <= 100`, default 50).
  - In `ImportJobService.findJobItems`, added an owner-local safe offset overflow guard: if `(long) page * (long) limit > Integer.MAX_VALUE`, returns an empty list `List.of()` immediately without executing unsupported JPA queries or writing any database records.
  - Documented effective pagination query parameter bounds on `ImportJobController.findItems` using OpenAPI `@Parameter(description = ...)`.
  - Maintained standard vault envelope: response returns a list with `meta == null`.
  - Preserved stable index ordering (`ORDER BY item_index ASC`) guaranteeing complete reachability without omissions or duplicates.
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.importdata.job.ImportJobOperations`
  - `com.vhvkhangg.personalprivatevault.importdata.internal.application.ImportJobService`
  - `com.vhvkhangg.personalprivatevault.importdata.internal.web.controller.ImportJobController`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=ImportJobPaginationIntegrationTest` (5 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.ImportJobPaginationIntegrationTest`:
    - `extremePageAndOffsetThresholdBoundariesReturnBoundedEmptyWithout500OrWrites`: Tested extreme page values (`Integer.MAX_VALUE`, threshold boundary `Integer.MAX_VALUE / limit`, and `+ 1`) across multiple limits (50, 100), asserting HTTP 200 OK with empty data list and zero database modifications.
    - Proved contiguous combined ordered sequence without omissions or duplicates across multiple pages for a 100-item job (`0..99`) and a 101-item job (`0..100`).
    - Verified actual bounded-list envelope contract (`jsonPath("$.meta").doesNotExist()`).
    - Verified OpenAPI query parameters: strictly asserted property presence (`has("default")`, `has("minimum")`, `has("maximum")`) and lower bound on `limit` that strictly excludes zero (supporting OpenAPI 3.0 boolean `exclusiveMinimum: true` with `minimum: 0` and OpenAPI 3.1 numeric bounds).
    - Verified review and execution beyond first page (executing item index 100 on page 1).
    - Verified atomic all-or-nothing rollback when an item execution fails beyond page 0: all target entities rolled back, `vault_entries` count unchanged, items maintain `status == VALID`, and `importedVaultEntryId == null`.

---

### BA15-16: Collection Package Descriptors and Repository Hygiene
- **Issue:** Missing `package-info.java` descriptors in 10 Collection subpackages; presence of 2 stale `.gitkeep` files; stale prose in `docs/architecture/README.md`.
- **Remediation:**
  - Added `package-info.java` in all 10 Collection subpackages.
  - Removed 2 stale `.gitkeep` files.
  - Updated outdated architecture documentation statements in `docs/architecture/README.md`.
- **Changed Symbols:**
  - 10 `package-info.java` files under `collection/` subpackages.
  - `docs/architecture/README.md`
- **Test Evidence:**
  - Spring Modulith architectural verification passed without package warnings.

---

### BA15-17: Lifecycle Writers Concurrency (including FR15-2)
- **Issue:** Concurrent updates to Journal entries, Personal profiles, or Feed sources could overwrite updates. FR15-2 required authoritative in-place recalculation of `nextFetch` under write lock in `FeedItemService`, eliminating separate uncoordinated repository update queries.
- **Remediation:**
  - Added pessimistic locking (`findByIdForUpdate`) and refresh in `DiaryService` and `PersonalProfileService`.
  - In `FeedItemService.ingestFetch`, locked the `FeedSource` via `findByIdForUpdate`, called `entityManager.refresh(sourceToUpdate)`, recalculated `nextFetch` from authoritative source configuration, updated `lastFetchedAt`, and saved with `saveAndFlush(sourceToUpdate)`.
  - Replaced raw thread `.join()` in test suite with `ExecutorService` and `Future<?>`.
- **Changed Symbols:**
  - `com.vhvkhangg.personalprivatevault.journal.internal.application.DiaryService`
  - `com.vhvkhangg.personalprivatevault.personal.internal.application.PersonalProfileService`
  - `com.vhvkhangg.personalprivatevault.feed.internal.application.FeedItemService`
- **Test Evidence:**
  - Focused command: `mvn -ntp test -Dtest=LifecycleWritersConcurrencyIntegrationTest` (7 tests, 0 failures)
  - `com.vhvkhangg.personalprivatevault.audit.LifecycleWritersConcurrencyIntegrationTest`:
    - Verified concurrent Diary update/delete/restore ordering and preservation.
    - Verified concurrent Personal profile update and trash handling.
    - Verified Feed source `nextFetch` recalculation and timestamp coherence under write lock with microsecond precision.
    - Verified forced concurrent commit orders with thread barriers and `awaitCompetingLock`.

---

## 3. Summary of Regression Suites

| Suite Class | Finding | Test Count | Status |
| --- | --- | --- | --- |
| `FlywayStartupIntegrationTest` | BA15-1 | 2 | PASSED |
| `WebDtoValidationAuditIntegrationTest` | BA15-2, BA15-15, FR15-4, FR15-5, FR15-7, FR15-8 | 25 | PASSED |
| `PasswordRangeIntegrationTest` | BA15-3 | 8 | PASSED |
| `NullCollectionMemberValidationTest` | BA15-4, FR15-7, FR15-8 | 5 | PASSED |
| `HttpMediaTypeNegotiationIntegrationTest` | BA15-5, FR15-3 | 4 | PASSED |
| `FeedImportKnowledgeExceptionIntegrationTest` | BA15-6, FR15-7, FR15-8 | 4 | PASSED |
| `MediaBinaryFramingWireIntegrationTest` | BA15-7, BA15-8, FR15-6, FR15-8 | 8 | PASSED |
| `LocationConcurrencyIntegrationTest` | BA15-9, FR15-7, FR15-8 | 7 | PASSED |
| `VaultRatingFirstSetConcurrencyIntegrationTest` | BA15-10, FR15-7, FR15-8 | 3 | PASSED |
| `LocationCategoryAssignmentIntegrationTest` | BA15-11, FR15-8 | 5 | PASSED |
| `PriceNumericBoundsIntegrationTest` | BA15-12, FR15-7, FR15-8 | 6 | PASSED |
| `StudyAccountInvariantIntegrationTest` | BA15-13, FR15-1, FR15-7, FR15-8 | 12 | PASSED |
| `ImportJobPaginationIntegrationTest` | BA15-14, FR15-7, FR15-8 | 5 | PASSED |
| `LifecycleWritersConcurrencyIntegrationTest` | BA15-17, FR15-2, FR15-7, FR15-8 | 7 | PASSED |
| **Total Audit-Specific Regression Tests** | **All BA15 / FR15** | **101** | **PASSED** |

Overall repository verification (`mvn -ntp clean verify`) passed all **1021 tests** with 0 failures, 0 errors, 0 skipped.
Preserved baseline: 920 tests. Net new focused audit tests: 101 tests across 14 suites.
