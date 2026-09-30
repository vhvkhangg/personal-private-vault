# Backend Phase 8 — Test Verification Evidence

- Date: 2026-09-30
- Handoff ID: `backend-phase-8-knowledge`
- Implementer: Antigravity

## Final Verification Command

```powershell
mvn -f backend/pom.xml -ntp clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 01:02 min

```powershell
git diff --check
```

- **Exit status:** `0` (clean, no whitespace warnings/errors)

## Environment & Infrastructure

- **JDK:** OpenJDK 25.0.2 (Oracle Corporation, build 25.0.2+10-69)
- **Maven:** Apache Maven 3.9.15
- **Spring Boot:** 4.1.1
- **Spring Modulith:** 2.1.1
- **Hibernate ORM:** 7.4.5.Final
- **Testcontainers:** 2.0.5 (`testcontainers-postgresql`)
- **PostgreSQL Image:** `postgres:18.6-alpine`
- **PostgreSQL Version:** 18.6

## Test Counts and Summary

- **Total tests run:** 547
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Breakdown by Test Suite

| Test Class | Test Count | Failures | Errors | Result |
| :--- | :---: | :---: | :---: | :---: |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests` | 12 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.migration.FlywayV1SchemaManifestIntegrationTest` | 1 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.reference.ReferenceModuleIntegrationTest` | 6 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultCapabilityMatrixTest` | 36 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultEntryIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.vault.VaultMetadataIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsValidationTest` | 20 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.settings.AppSettingsIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.BootstrapValidationTest` | 18 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.JwtPropertiesTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.JwtTokenServiceTest` | 1 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SecretRedactionTest` | 4 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SessionServiceTest` | 2 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.TokenGeneratorTest` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.AuthenticationBootstrapIntegrationTest` | 4 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.RefreshTokenLifecycleIntegrationTest` | 9 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.PrivatePinIntegrationTest` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.authentication.SecurityFilterChainIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.PersonValidationTest` | 30 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupValidationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.PersonIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.people.CreatorGroupIntegrationTest` | 15 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionValidationTest` | 37 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionGenreIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionLinkIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionIntegrationTest` | 13 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmValidationTest` | 44 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmGenreIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmLinkIntegrationTest` | 6 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmCreditIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.media.MediaValidationTest` | 22 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.media.MediaIntegrationTest` | 10 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.location.LocationValidationTest` | 26 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.location.LocationIntegrationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.account.AccountValidationTest` | 21 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.account.AccountIntegrationTest` | 15 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeValidationTest` | 27 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeArchitectureTests` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeIntegrationTest` | 18 | 0 | 0 | PASS |
| **Total** | **547** | **0** | **0** | **PASS** |

## Focused Knowledge Regression & Invariant Tests

| Test Class | Test Method | Result | Verified Behavior |
| :--- | :--- | :---: | :--- |
| `KnowledgeArchitectureTests` | `verifiesModularStructure` | PASS | Spring Modulith verifies modular structure across monolith including closed parent knowledge facade and 4 nested modules (`knowledge.study`, `knowledge.information`, `knowledge.vocabulary`, `knowledge.note`). |
| `KnowledgeArchitectureTests` | `verifiesKnowledgeParentFacadeNamedInterface` | PASS | Knowledge parent facade exposes only `@NamedInterface("api")` containing `knowledge.api.*` types, with zero `.internal.` classes exposed. |
| `KnowledgeArchitectureTests` | `verifiesNestedKnowledgeModulesEncapsulation` | PASS | All 4 nested modules (`knowledge.study`, `knowledge.information`, `knowledge.vocabulary`, `knowledge.note`) conceal `.internal.` packages from external modules. |
| `KnowledgeValidationTest` | `enforcesWebsiteHostnameValidation` | PASS | Validates hostname-only validation on Study create/update rejecting schemes (`http://`, `https://`), paths (`/docs`), ports (`:8080`), fragments/queries, and invalid characters while preserving lowercase/trim normalization. |
| `KnowledgeValidationTest` | `validatesEaseFactorBoundaries` | PASS | Validates ease factor after two-decimal scaling (`HALF_UP`) and against `numeric(5,2)` range, rejecting values that round to zero (`0.001`, `0.000`) and overflow (`1000.00`, `999.995`) with `InvalidVocabularyItemException`. |
| `KnowledgeIntegrationTest` | `verifiesKnowledgeTablesExist` | PASS | Confirms `study_items`, `information_items`, `vocabulary_items`, `vocabulary_reviews`, `notes` tables exist in PostgreSQL schema. |
| `KnowledgeIntegrationTest` | `studyFailureRollsBackVaultEntry` | PASS | Failing study creation atomically rolls back transaction leaving 0 orphan `vault_entries` records. |
| `KnowledgeIntegrationTest` | `informationFailureRollsBackVaultEntry` | PASS | Failing information creation atomically rolls back transaction leaving 0 orphan `vault_entries` records. |
| `KnowledgeIntegrationTest` | `vocabularyFailureRollsBackVaultEntry` | PASS | Failing vocabulary creation atomically rolls back transaction leaving 0 orphan `vault_entries` records. |
| `KnowledgeIntegrationTest` | `noteFailureRollsBackVaultEntry` | PASS | Failing note creation atomically rolls back transaction leaving 0 orphan `vault_entries` records. |
| `KnowledgeIntegrationTest` | `createWithAuthorPersonOrGroup` | PASS | Creates study items with Person XOR Group author and verifies `vault_entries` backing (`STUDY` type). |
| `KnowledgeIntegrationTest` | `linksYoutubeChannelAccount` | PASS | Links study item to YouTube channel account, verifying account existence, TRACKED ownership, YOUTUBE_CHANNEL type, and YouTube platform. |
| `KnowledgeIntegrationTest` | `rejectsInvalidWebsiteHostnames` | PASS | Rejects invalid website domain strings on PostgreSQL integration create/update with `InvalidStudyItemException`. |
| `KnowledgeIntegrationTest` | `concurrentYoutubeAccountRaceIsSafe` | PASS | Deterministic contention on `study_items.youtube_channel_account_id` across 2 threads, with `awaitCompetingLock` observing real PostgreSQL lock wait (`NOT l.granted`); exactly one succeeds and one throws `StudyConflictException`; losing transaction rolls back with 1 vault entry remaining; captured output across both worker streams contains neither private marker nor PostgreSQL `Detail: Key`. |
| `KnowledgeIntegrationTest` | `createUpdateAndAllowDuplicates` | PASS | Information items allow duplicate titles and types without artificial uniqueness; markdown and raw text preserved intact. |
| `KnowledgeIntegrationTest` | `allowsDuplicateWordsAndValidatesLanguage` | PASS | Vocabulary items allow duplicate words and languages; validates language code via `ReferenceCatalog`. |
| `KnowledgeIntegrationTest` | `explicitAtomicReviewTransitionAndDueQuery` | PASS | Atomically applies SRS review transition, records `VocabularyReview` row, updates `VocabularyItem` status/ease/interval/repetition counts, and verifies due items query and history query. |
| `KnowledgeIntegrationTest` | `rejectsEaseFactorBoundariesAgainstPostgres` | PASS | Rejects ease factor boundaries (rounds to zero, overflow) on create and review without reaching database constraint errors. |
| `KnowledgeIntegrationTest` | `concurrentReviewContentionSerializes` | PASS | Concurrent review transitions on the same vocabulary item hold row lock (`SELECT FOR UPDATE`), observe PostgreSQL lock wait via `awaitCompetingLock`, and serialize state chaining where Review 2's `previous_*` matches Review 1's `new_*` exactly. |
| `KnowledgeIntegrationTest` | `dueVocabularyQueryMatrixAndOrdering` | PASS | Covers complete due matrix: inclusive cutoff boundary, null-time `NEW`, excluded null-time `LEARNING`/`REVIEW`, excluded `MASTERED`, scheduled-first ordering (`next_review_at` ASC then `id` ASC), and limit bounding. |
| `KnowledgeIntegrationTest` | `preservesMarkdownAndJsonbFrontmatter` | PASS | Notes preserve raw Obsidian markdown syntax (wiki-links, callouts, latex, code blocks) and arbitrary JSONB frontmatter round-trip. |
| `KnowledgeIntegrationTest` | `concurrentImportedFileHashRaceIsSafe` | PASS | Deterministic contention on `notes.imported_file_hash` across 2 threads, with `awaitCompetingLock` observing real PostgreSQL lock wait (`NOT l.granted`); exactly one succeeds and one throws `NoteConflictException`; losing transaction rolls back with 1 vault entry remaining; captured output across both worker streams contains neither private hash nor PostgreSQL `Detail: Key`. |
| `KnowledgeIntegrationTest` | `facadeDelegationWorksAcrossAllDomains` | PASS | Parent facade `KnowledgeOperations` successfully delegates and maps across Study, Information, Vocabulary, and Note domains. |
