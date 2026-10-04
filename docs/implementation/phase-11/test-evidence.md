# Backend Phase 11 — Test Verification Evidence

- Date: 2026-10-04
- Handoff ID: `phase-11-finance-journal-personal-foundations`
- Implementer: Antigravity
- Status: `READY FOR OWNER COMMIT` after Codex final acceptance
- Remediation of Codex Re-Review: `docs/implementation/phase-11/reviews/2026-10-04-phase-11-final-codex-second-rereview.md`

## Latest Codex review disposition & FR11-9 Remediation

Codex [final acceptance](reviews/2026-10-04-phase-11-final-codex-acceptance.md) closes FR11-1 through FR11-9.
Independent `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, 787 tests, no failures/errors/skips,
01:19, finished 2026-10-04T10:52:08+07:00. Diff check passed. Per-suite counts below were synchronized with actual
Surefire test cases, including parameterized cases. Owner commit/push and ChatGPT closeout are next; not frozen.
Antigravity's implementation/remediation evidence follows.

Codex independently verified clean verify (777 test cases) and confirmed FR11-1 through FR11-8 closed.
The second final re-review identified **FR11-9 (High)**: unconditional guard refresh in `FinanceLockManager`
discarded earlier unflushed public service mutations within the same caller transaction (`TransactionTemplate`),
reproduced as wallet update loss upon transaction creation and acceptance of an EXPENSE transaction after
updating a category to INCOME.

Antigravity remediated FR11-9 by enforcing deliberate mutation-completion flushes across all mutating operations
in `WalletService`, `TransactionCategoryService`, `FinancialTransactionService`, and `RecurringTransactionRuleService`,
preserving completed owner-local mutations before any subsequent guarded operation reloads state under pessimistic lock.
Real PostgreSQL public service composition regressions were added in `FinancePublicServiceCompositionIntegrationTest` (10 tests).

## Remediation Summary (FR11-1 through FR11-9)

1. **FR11-1 (Persistence Context Reconciliation for Composite Weekday Identities):**
   - Reconciled owner-local persistence context in `RecurringTransactionRuleService.updateRule` and `restoreRule`. All 7 possible weekday identities (`DayOfWeek.values()`) for the rule are explicitly reconciled and detached via `entityManager.find` + `entityManager.detach` prior to executing bulk JPQL deletes, preventing `SimpleJpaRepository.save` from executing `merge` into an obsolete managed instance loaded by an earlier read.
   - Converged weekdays are persisted directly with `entityManager.persist`, guaranteeing clean SQL `INSERT` statements into PostgreSQL.
   - Explicit `recurringTransactionRuleRepository.flush()` added to `softDeleteRule` and `restoreRule` (and `financialTransactionRepository.flush()` in `FinancialTransactionService`) ensuring that subsequent `lockAndRefresh` operations under pessimistic lock in the same transaction never reload stale uncommitted state.
   - **Verification Tests in `RecurringTransactionRuleIntegrationTest`:**
     - `staleManagedWeekdayReaddedAfterExternalRemovalPersistsCleanly`: exact 5-step sequential reproduction (Tx A preloads rule with MONDAY; Tx B in another context replaces with TUESDAY and commits; Tx A re-adds MONDAY and commits; direct SQL asserts count = 1 and weekday = MONDAY; fresh reload verifies MONDAY).
     - `staleManagedWeekdayContentionWithPreloadedContextReconcilesFreshState`: contention variant with preloaded context, observed PostgreSQL row lock wait, overlapping/disjoint sets, committed scalar/entry assertions, and fresh context reload.
     - `staleManagedWeekdayContentionRollbackPreservesWinnerState`: contention rollback proving no partial writes and winner's committed weekdays preserved in DB.

2. **FR11-2 (Decimal Precision & Scale Validation with Privacy — Confirmed Closed):**
   - In `FinanceValidationUtils`: exact scale conversion using `BigDecimal.setScale(..., RoundingMode.UNNECESSARY)` executed first, accepting equivalent zero-padded and scientific notation inputs.
   - Precision boundaries: `numeric(19,4)` enforces precision <= 19; `numeric(24,10)` enforces precision <= 24 and strictly positive rate. Overflows and scale mismatches throw domain exceptions without leaking private monetary values or rates into error messages.
   - Verified by dedicated unit test suite `FinanceValidationUtilsTest` (15 tests).

3. **FR11-3 (Real Preloaded-Context Contention for Restore Operations):**
   - **Recurring Rules (`contentionRestoreWithPreloadedParentAndChildren`):** Waiting participant explicitly preloads parent AND children (`recurringTransactionRuleRepository.findById`, `recurringRuleWeekdayRepository.findByIdRecurringRuleId`, `recurringRuleEntryRepository.findByRecurringRuleIdOrderByIdAsc`) in its outer transaction before waiting on the lock. Winner restores and updates rule with new state (TUESDAY, THURSDAY, `-25.0000`), PostgreSQL lock wait is observed via `awaitCompetingLock`, and waiting participant unblocks and reloads authoritative fresh state.
   - **Financial Transactions (`contentionRestoreTransactionWithPreloadedContextReconcilesFreshState`):** Waiting participant explicitly preloads parent AND entries (`financialTransactionRepository.findById`, `financialTransactionEntryRepository.findByTransactionIdOrderByIdAsc`) in its outer transaction before waiting on the lock. Winner restores and updates transaction to new state (`250.0000`), PostgreSQL lock wait is observed via `awaitCompetingLock`, and waiting participant unblocks, recovering fresh authoritative state and balance.

4. **FR11-4 (Nationality Normalization & Reference Validation — Confirmed Closed):**
   - In `PersonalProfileService`: empty and whitespace nationality strings are consistently normalized to `null`. Any retained non-null code is trimmed and validated against `ReferenceCatalog.country`.
   - Verified by `PersonalProfileIntegrationTest` (12 tests).

5. **FR11-5 (Subscription Update Race Proof & Canonical Guard Order):**
   - Added 5 changed-link `updateSubscription` contention tests in `SubscriptionIntegrationTest`:
     - `contentionUpdateAssignmentVsWalletDeleteWinnerUpdate`: Winner Order A (Update wins, wallet delete waits and completes; historical link retained).
     - `contentionUpdateAssignmentVsWalletDeleteWinnerDelete`: Winner Order B (Delete wins, update waits on wallet lock, is rejected with `InvalidSubscriptionException`, full scalar/link rollback asserted, subscription count preserved).
     - `contentionUpdateAssignmentVsRecurringRuleDeleteWinnerUpdate`: Winner Order A (Update wins, rule delete waits and completes; historical link retained).
     - `contentionUpdateAssignmentVsRecurringRuleDeleteWinnerDelete`: Winner Order B (Delete wins, update waits on rule lock, is rejected with `InvalidSubscriptionException`, full scalar/link rollback asserted, subscription count preserved).
     - `contentionUpdateAssignmentBothLinksChangeCanonicalGuardOrder`: verifies canonical lock acquisition order (`Subscription -> RecurringRule -> Wallet`) when both links change under contention.

6. **FR11-6 (Safe Uniqueness Classification — Confirmed Closed):**
   - `PersonalProfileService` specifically matches `uq_personal_profiles_one_active_self` before translating to `PersonalProfileConflictException`.
   - `SubscriptionService` specifically matches inline UNIQUE `subscriptions_recurring_rule_id_key` before translating to `SubscriptionConflictException`. Non-uniqueness errors are not misdiagnosed.

7. **FR11-7 (Constant Query-Count Proof with Hibernate Statistics):**
   - In `FinancialTransactionIntegrationTest` (`measuredQueryCountProvesConstantChildQueriesForTransactions`): measured prepared statements using Hibernate `Statistics` comparing limit 2 vs limit 10:
     - Global recent: exactly 2 prepared statements (1 for parent batch, 1 for child entries batch) regardless of limit (2 vs 10).
     - Wallet recent: exactly 3 prepared statements (1 for wallet check, 1 for parent batch, 1 for child entries batch) regardless of limit (2 vs 10).
     - Asserted deterministic ordering (`occurredAt DESC, id DESC`), bounded limit adherence, and absence of unrelated child loading.
   - In `RecurringTransactionRuleIntegrationTest` (`measuredQueryCountProvesConstantChildQueriesForRulesAndDueRules`): measured prepared statements using Hibernate `Statistics` comparing limit 2 vs limit 10:
     - Rules list: exactly 3 prepared statements (1 for parent rules, 1 for batch weekdays, 1 for batch entries) regardless of limit (2 vs 10).
     - Due rules: exactly 3 prepared statements (1 for due rules, 1 for batch weekdays, 1 for batch entries) regardless of limit (2 vs 10).
     - Asserted deterministic ordering (`name ASC, id ASC` / `nextRunAt ASC, id ASC`), bounded limit adherence, and absence of unrelated child loading.

8. **FR11-8 (Documentation & Review Corrections — Confirmed Closed):**
   - Corrected Journal description to `entry_date`, `title`, `content_markdown`.
   - Corrected Personal description to `Gender` enum.
   - Corrected Subscription recurring rule unique constraint name to inline `subscriptions_recurring_rule_id_key`.
   - Accurately recorded compiler/JVM notices.

9. **FR11-9 (Caller-Transaction Mutation Preservation & Public-Service Composition):**
   - Enforced deliberate mutation-completion flushes across all mutating application service methods in `finance`:
     - `WalletService`: `createWallet` (`saveAndFlush`), `updateWallet` (`flush`), `softDeleteWallet` (`flush`), `restoreWallet` (`flush`).
     - `TransactionCategoryService`: `createCategory` (`saveAndFlush`), `updateCategory` (`flush`).
     - `FinancialTransactionService`: `createTransaction` (child + parent `flush`), `updateTransaction` (child + parent `flush`), `softDeleteTransaction` (`flush`), `restoreTransaction` (`flush`).
     - `RecurringTransactionRuleService`: `createRule` (weekdays, entries, parent `flush`), `updateRule` (weekdays, entries, parent `flush`), `softDeleteRule` (`flush`), `restoreRule` (`flush`).
     - `SubscriptionService`: `createSubscription` (`saveAndFlush`), `updateSubscription` (`flush`), `softDeleteSubscription` (`flush`), `restoreSubscription` (`flush`).
   - Retained authoritative refresh under pessimistic lock in `FinanceLockManager` without blindly flushing stale preloaded state before lock acquisition, preserving both concurrent contention behavior and same-transaction mutation composition.
   - Added real PostgreSQL integration suite `FinancePublicServiceCompositionIntegrationTest` (10 tests) covering:
     - `walletUpdateFollowedByTransactionAndRuleAssignmentPreservesCurrencyBalanceAndMetadata`: updates wallet currency to VND and opening balance to 123.4500, then creates transaction and recurring rule referencing it; DB and balance calculation (`124.4500`) preserve all mutations without reverting.
     - `walletSoftDeleteFollowedByTransactionRejectsAndRollsBackAtomically`: soft-deletes wallet, then attempts transaction creation; rejected with `InvalidFinancialTransactionException` ("Referenced wallet is deleted"), transaction rolls back, and wallet remains active in fresh DB context.
     - `walletSoftDeleteFollowedBySubscriptionRejectsAndRollsBackAtomically`: soft-deletes wallet, attempts subscription creation; rejected with `InvalidSubscriptionException` ("Payment wallet is deleted"), rolls back cleanly.
     - `walletRestoreSequencingInSingleCallerTransactionAllowsSubsequentAssignment`: restores previously soft-deleted wallet, creates transaction and subscription in same caller transaction; all succeed and commit authoritative restored state.
     - `categoryKindUpdateFollowedByIncompatibleExpenseTransactionRejectsAtomicallyAndRollsBack`: updates category from BOTH to INCOME, attempts EXPENSE transaction; rejected with `InvalidFinancialTransactionException`, rolls back, category in DB remains BOTH.
     - `categoryKindUpdateFollowedByIncompatibleExpenseRuleRejectsAtomicallyAndRollsBack`: updates category to INCOME, attempts EXPENSE rule; rejected with `InvalidRecurringTransactionRuleException`, rolls back, category remains BOTH.
     - `categoryKindUpdateFollowedByCompatibleAssignmentPreservesNewKind`: updates category to INCOME, creates compatible INCOME transaction and rule; commits with category kind INCOME.
     - `transactionScalarUpdateFollowedBySoftDeletePreservesScalars`: updates transaction scalars (POSTED, new description/notes, amount delta 75.0000), then immediately soft-deletes; fresh DB state preserves updated scalars and child entry.
     - `recurringRuleScalarUpdateFollowedBySubscriptionAssignmentPreservesScalarsAndLinks`: updates recurring rule scalars (interval count 2, mutated name/description), then links subscription; fresh DB state preserves updated rule scalars and subscription link.
     - `recurringRuleScalarUpdateFollowedBySoftDeletePreservesScalars`: updates rule scalars (name, notes), then soft-deletes in same caller transaction; fresh DB state preserves updated scalars on soft-deleted rule.

## Final Verification Command

```powershell
mvn -f backend/pom.xml -ntp clean verify
```

- **Exit status:** `0` (`BUILD SUCCESS`)
- **Total build time:** 01:17 min
- **Finished at:** 2026-10-04T10:45:53+07:00

```powershell
git diff --check
```

- **Exit status:** `0` (clean, no whitespace warnings or errors)

```powershell
powershell -ExecutionPolicy Bypass -File scripts/refresh-graphify.ps1
```

- **Exit status:** `0` (4641 nodes, 16532 edges, 402 communities)

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

- **Total tests run:** 787
- **Failures:** 0
- **Errors:** 0
- **Skipped:** 0

### Test Count Composition

- **Approved pre-Phase-11 baseline:** 685 tests
- **Phase 11 domain tests:** 95 tests
  - `finance`: 78 tests (`FinanceValidationUtilsTest`: 15, `WalletIntegrationTest`: 11, `TransactionCategoryIntegrationTest`: 5, `FinancialTransactionIntegrationTest`: 8, `RecurringTransactionRuleIntegrationTest`: 12, `SubscriptionIntegrationTest`: 17, `FinancePublicServiceCompositionIntegrationTest`: 10)
  - `journal`: 5 tests (`DiaryIntegrationTest`: 5)
  - `personal`: 12 tests (`PersonalProfileIntegrationTest`: 12)
- **Phase 11 new architecture tests:** 7 tests added to `ApplicationArchitectureTests` (from 16 to 23 tests)
- **Total tests:** 685 + 95 + 7 = 787 tests

### Breakdown by Test Class

| Test Class | Test Count | Failures | Errors | Result |
| :--- | :---: | :---: | :---: | :---: |
| `com.vhvkhangg.personalprivatevault.ApplicationArchitectureTests` | 23 | 0 | 0 | PASS |
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
| `com.vhvkhangg.personalprivatevault.fiction.FictionValidationTest` | 40 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionGenreIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionLinkIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.fiction.FictionIntegrationTest` | 13 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmValidationTest` | 44 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmGenreIntegrationTest` | 7 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmLinkIntegrationTest` | 6 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmCreditIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.film.FilmIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.media.MediaValidationTest` | 23 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.media.MediaIntegrationTest` | 10 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.location.LocationValidationTest` | 32 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.location.LocationIntegrationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.account.AccountValidationTest` | 21 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.account.AccountIntegrationTest` | 15 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeValidationTest` | 27 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeArchitectureTests` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.knowledge.KnowledgeIntegrationTest` | 29 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionArchitectureTests` | 3 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionValidationTest` | 16 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.collection.CollectionIntegrationTest` | 17 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.feed.FeedValidationTest` | 10 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.feed.FeedIntegrationTest` | 29 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.importdata.ImportDataConcurrencyTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.importdata.ImportDataValidationTest` | 23 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.importdata.ImportDataIntegrationTest` | 20 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.finance.FinanceValidationUtilsTest` | 15 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.finance.WalletIntegrationTest` | 11 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.finance.TransactionCategoryIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.finance.FinancialTransactionIntegrationTest` | 8 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.finance.RecurringTransactionRuleIntegrationTest` | 12 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.finance.SubscriptionIntegrationTest` | 17 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.finance.FinancePublicServiceCompositionIntegrationTest` | 10 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.journal.DiaryIntegrationTest` | 5 | 0 | 0 | PASS |
| `com.vhvkhangg.personalprivatevault.personal.PersonalProfileIntegrationTest` | 12 | 0 | 0 | PASS |
| **Total** | **787** | **0** | **0** | **ALL GREEN** |

## Observed Compiler & JVM Notices

- **Lombok Unsafe Notice:** Terminally deprecated `sun.misc.Unsafe::objectFieldOffset` called by `lombok.permit.Permit` on Java 25.
- **Deprecated API Notices:**
  - `CsvImportParser` (frozen Phase 10) uses deprecated Apache Commons CSV parser methods.
  - `AbstractPostgresIntegrationTest` uses deprecated `PostgreSQLContainer` constructor.
- **ByteBuddy / Mockito Notice:** Dynamic agent attachment warning on Java 25.
- **SpringDoc Notice:** Default-enabled `/v3/api-docs` and `/swagger-ui.html` endpoint startup warnings, not schema-resolution warnings.
- **JVM Notice:** Class-data sharing is limited after the Mockito agent appends to the bootstrap classpath.

## Schema & Architecture Integrity

- Zero Flyway migrations or DDL changes introduced. Frozen Schema v1 (`V1__create_schema_v1.sql`) remains intact.
- Spring Modulith module boundaries verified by `ApplicationArchitectureTests`:
  - `finance` depends strictly on `reference::catalog` and `reference::view`.
  - `journal` has zero application-module dependencies.
  - `personal` depends strictly on `reference::catalog`, `reference::view`, `location::address`, and `location::view`.
  - No module dependency cycles, no Vault identity or recycle bin behavior for Phase 11 modules, and no internal package leakage.
