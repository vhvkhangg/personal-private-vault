# Active Implementation Handoff

- Handoff ID: `phase-11-finance-journal-personal-foundations`
- Created by: Codex, 2026-10-01
- Status: `COMPLETE_FROZEN`
- Owner commit/push: completed 2026-10-04
- Implementer: Antigravity
- Final reviewer: Codex
- Approved preparation baseline: `1fd0031d77a3c195c4b7f7d2af48d0e64ca75f7b`

## Goal

Implement Phase 11 Finance, Journal and Personal foundations over unchanged Schema v1, with semantic public
contracts, module-owned persistence/lifecycle, atomic ledger/configuration mutations and PostgreSQL-backed tests.
Preparation is `READY FOR HANDOFF`; the clean starting HEAD matches local `origin/main` at the baseline above
(no remote fetch performed). Phase 10 is frozen with 685 passing baseline tests. No milestone is due until Phase 12.

## Sources of truth

- `docs/implementation/phase-11/README.md`: complete field/default/validation, operation, lifecycle and query contract.
- `docs/implementation/phase-11/preparation-review.md`
- `docs/implementation/phase-11/reviews/2026-10-01-phase-11-pre-handoff-codex-acceptance.md`: P11-1/P11-2 closed.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`
- `backend/src/main/resources/db/migration/V1__create_schema_v1.sql`
- `docs/adr/0011-ledger-based-finance.md`
- `docs/architecture/module-boundaries.md`, `docs/architecture/module-dependency-matrix.md`
- `docs/repository/repository-package-tree.md`, root/backend/module `AGENTS.md`
- `.agents/rules/backend-phase-11-finance-journal-personal.md`

Read the canonical Phase 11 README in full; requirements linked here are mandatory. Stop and report any conflict
with frozen sources rather than changing them. Historical reviews do not override final acceptance.

## Implementation targets

Package paths below are under `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.

- `finance/`: own only wallets, transaction_categories, financial_transactions, financial_transaction_entries,
  recurring_transaction_rules, recurring_rule_weekdays, recurring_rule_entries and subscriptions. Expose capabilities
  in `wallet`, `category`, `transaction`, `recurring`, `subscription`, with immutable `view`/`enums` contracts.
  Implement all required README operations, including status changes, balance and bounded due/history reads.
- `journal/`: own only diary_entries; expose create/full update, ID/date-range reads and soft-delete/restore
  through a semantic `diary` capability and immutable `view` contracts.
- `personal/`: own only personal_profiles; expose full create/update, ID/self/bounded list reads and
  soft-delete/restore through `profile`, `view`, and `enums` contracts.
- Each module's `internal/`: entities, repositories, application services and persistence adapters. Add meaningful
  public/internal package-info and Spring Modulith named interfaces; remove filled `.gitkeep` placeholders only.
  Refine command/interface class names consistently with established modules without broadening capabilities.
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/`: focused service, PostgreSQL integration/workflow and
  architecture coverage; update existing architecture expectations only as required by Phase 11.
- `docs/implementation/phase-11/test-evidence.md` and this handoff's implementation-result section.

Finance depends only on public Reference catalog/view; Journal has no application-module dependencies; Personal
depends only on public Reference catalog/view and Location Address/view. `ReferenceCatalog.currency/country` return
Optional; `AddressOperations.findById` returns AddressView or throws AddressNotFoundException. No cross-module JPA
access, internal imports, cycles or Vault identity/recycle behavior. Frozen Phase 0–10 behavior remains unchanged.

## Required behavior / invariants

- Apply all README lengths, enums, defaults and full-replacement semantics. Use BigDecimal exactly representable
  at numeric(19,4); optional positive exchange rate at numeric(24,10). Never silently round.
- Balance is opening balance plus POSTED/non-deleted ledger deltas only. INCOME is one positive entry, EXPENSE one
  negative, TRANSFER two distinct wallets with negative source/positive destination. No zero entries or invented
  zero-sum/exchange-rate requirement. Transfer category is null; other categories match kind or BOTH.
- Wallet currency cannot change while either entry table retains any reference, regardless of owner
  status/active/deleted state. After committed full replacement removes its last reference, it may change again.
  Do not infer/persist ever-referenced history. Currency updates and submitted entry assignments share wallet write
  guards and fresh authoritative state. Category kind changes/assignment share a category guard; kind changes must
  remain compatible with every retained transaction/rule reference, including deleted owners.
- Existing transaction/rule mutations acquire aggregate parent guard first, category guard when applicable, then
  every submitted wallet guard in ascending ID order, then replace children. Creates start with category then
  wallets. Hold guards through caller commit/rollback; refresh managed state under the guard. A pre-check is not
  authoritative. No independently committed children, REQUIRES_NEW, global context clear or retry/lock framework.
- Transaction scalar/status/entry/lifecycle mutations are atomic and same-parent serialized. Recurring update/full
  replacement/delete/restore share one rule guard and fresh parent/weekday/entry state; reject update of deleted
  rules until restored. Validate frequency-specific fields and entry shape, converge duplicate weekdays as a set,
  and never leave mixed scalar/child configurations. Restore revalidates retained shape without treating historical
  wallet references as new assignments. Recurring description max is 1000.
- Recurrence implements configuration and side-effect-free due reads only: active, non-deleted, non-null next_run_at
  <= cutoff, positive limit, next_run_at/id ascending. No schedule advancement or transaction materialization.
- Subscriptions validate custom-cycle/decimal/public references; optional payment wallet/rule must be non-deleted.
  PostgreSQL recurring_rule_id uniqueness arbitrates races, including retained deleted subscriptions; translate
  conflicts stably and privately. No charging/renewal advancement.
- Soft-delete/restore is module-owned and idempotent, default reads hide deleted rows, and historical children/links
  remain retained. Deleting/restoring posted transactions removes/restores their balance contribution.
- Journal permits multiple entries per date and preserves required Markdown exactly, including whitespace;
  optional valid date bounds and positive limit, entry_date/id descending.
- Personal relationship stays free-form; validate country/address through public APIs. Preserve notes Markdown
  exactly. Partial uniqueness permits only one active self; no auto-demotion. Delete/change-to-nonself frees the
  slot; conflicting create/update/restore fails with stable PersonalProfileConflictException.
- All collection-valued reads are positive-bounded and deterministically ordered per README. Public APIs expose
  commands/views, not JPA entities. Avoid sensitive payload logging and raw database conflict details.

## Non-goals

Scheduler/@Scheduled runtime, auto-posting/charges, timezone/advance algorithms, REST/OpenAPI/frontend, Search or
Phase 12 implementation, calendar aggregation, Vault metadata integration, import/export additions, permanent
deletion, DBML/Flyway/schema redesign, unrelated frozen-module refactors, generic frameworks, new agents/hooks.

## Test/evidence contract

- Implement the entire README Testing contract with JUnit 5/AssertJ and real PostgreSQL Testcontainers/Flyway;
  Mockito only where isolation adds value. No H2. Preserve all 685 Phase 10/baseline regressions.
- Finance: schema/decimal/default/reference/balance/sign/category validation, bounded reads, full replacement,
  deletion/restoration and rollback. Prove currency-vs-first-assignment in both winner orders and category-kind-vs-
  first-assignment with actual guard contention. For BOTH entry tables separately: create on A, replace with B,
  commit, restart/reload, verify no retained A reference and successfully change A currency.
- Transaction contention includes a preloaded context, observed PostgreSQL waiting, committed scalar/child/balance
  assertions and zero partial writes after rejection/rollback. Recurring tests prove replacement-vs-replacement,
  replacement-vs-delete in both winner orders, and restore contention with preloaded parent/children; assert one
  coherent committed configuration, never mixed child sets. Timing-only sleeps are insufficient.
- Subscription races drive both callers past pre-checks into actual unique-index competition: keep the winner
  uncommitted, observe the loser's database wait/conflict, then verify safe loser translation and no partial row.
- Journal: exact Markdown create/update/reload, duplicate dates, range bounds/order and lifecycle.
  Personal: public country/address validation, exact notes, duplicate nonself allowed, sequential and deterministic
  real PostgreSQL concurrent create/update-to-self, self slot release and restore conflict, bounded reads.
- Verify Spring Modulith dependencies/named interfaces and Flyway/Hibernate schema fidelity. Run focused tests
  while iterating, then `mvn -f backend/pom.xml -ntp clean verify` and `git diff --check` (Maven wrapper equivalent
  permitted). Keep full final evidence; compile/test-only does not substitute for clean verify.
- Record exact commands, baseline, Java/Maven/PostgreSQL/Testcontainers versions, focused test names/results,
  total tests/failures/errors/skips, static checks, architecture/schema results and contention coordination/proof
  in `docs/implementation/phase-11/test-evidence.md`; link diagnostic artifacts instead of pasting large logs.
  Report warnings honestly; no warning-free/IDE-clean claim without inspection and no blanket suppression.

## Constraints / risks

Use `java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`, `design-pattern-selection`,
`modular-monolith-architecture`, `jpa-postgresql-persistence`, `backend-testing`, and
`finance-journal-personal-domain-modeling` skills. Reuse existing backend-implementer/architecture-auditor and safety
hook; keep abstractions owner-local and justified. Main risks: stale managed state after waiting, guard-order
inversion, incomplete replacement, silent rounding and privacy-unsafe uniqueness translation.
Do not commit, push, tag or create/merge PRs. Stop for frozen-baseline conflicts or required scope expansion.

## Implementation result

Remediated and verified by Antigravity on 2026-10-04 over baseline commit `1fd0031d77a3c195c4b7f7d2af48d0e64ca75f7b`, fully addressing findings FR11-1, FR11-3, FR11-5, and FR11-7 from the 2026-10-04 Codex final re-review.

### Targets Delivered & Remediated
1. **`finance/` Module:**
   - Capabilities exposed in `wallet`, `category`, `transaction`, `recurring`, `subscription` with immutable DTO `view/` contracts and `enums/`.
   - Domain entities: `Wallet`, `TransactionCategory`, `FinancialTransaction`, `FinancialTransactionEntry`, `RecurringTransactionRule`, `RecurringRuleWeekday`, `RecurringRuleEntry`, `Subscription`.
   - Persistence context reconciliation (FR11-1): all 7 candidate weekday identities (`DayOfWeek.values()`) for the rule are explicitly reconciled and detached via `entityManager.find` + `entityManager.detach` before bulk JPQL deletes in `RecurringTransactionRuleService.updateRule` and `restoreRule`, preventing merge into obsolete managed instances. Converged weekdays are persisted directly via `entityManager.persist`. Tested with the exact 5-step sequential reproduction (`staleManagedWeekdayReaddedAfterExternalRemovalPersistsCleanly`), contention variant with preloaded context, and rollback preservation.
   - Decimal validation & privacy (FR11-2): `FinanceValidationUtils.normalizeMoney` and `normalizeExchangeRate` convert exact scale with `UNNECESSARY` first, accept zero-padded and scientific notation, enforce precision boundaries (<= 19 for money, <= 24 for exchange rate), and never interpolate private values into error messages.
   - Real preloaded restore contention (FR11-3): waiting participant explicitly preloads parent and children via domain repositories in its outer transaction before waiting on PostgreSQL lock; winner restores and updates state; waiting participant unblocks and reconciles fresh authoritative state and balance in both `RecurringTransactionRuleIntegrationTest` and `FinancialTransactionIntegrationTest`.
   - Changed-link subscription update contention & canonical guards (FR11-5): added 5 `updateSubscription` contention tests in `SubscriptionIntegrationTest` for both owner types (Wallet and RecurringRule) across both winner orders (Assignment wins / Delete wins), with observed waits, unchanged-owner historical link preservation, scalar/link rollback assertions, subscription count preservation, and verified canonical lock order (`Subscription -> RecurringRule -> Wallet`) when both links change under contention.
   - Measured query-count regressions (FR11-7): added constant-query tests using Hibernate `Statistics` (`getPrepareStatementCount()`) comparing limit 2 vs limit 10 for financial transactions global/wallet recent history (constant 2 and 3 queries) and recurring rules list and due paths (constant 3 queries), asserting parent ordering, limits, and absence of unrelated child loading.
   - Inline UNIQUE constraint handling (FR11-6, FR11-8): specific matching of `subscriptions_recurring_rule_id_key` preventing false classification of other integrity violations.
   - Entity state flushing: added explicit `flush()` on soft-delete and restore across services (`RecurringTransactionRuleService`, `FinancialTransactionService`) ensuring subsequent pessimistic lock refreshes in the same transaction reload up-to-date state.
   - Real PostgreSQL test suites: `FinanceValidationUtilsTest` (15 tests), `WalletIntegrationTest` (11 tests), `TransactionCategoryIntegrationTest` (5 tests), `FinancialTransactionIntegrationTest` (8 tests), `RecurringTransactionRuleIntegrationTest` (12 tests), `SubscriptionIntegrationTest` (17 tests).

2. **`journal/` Module:**
   - Semantic capability `diary` with immutable `DiaryEntryView` and `DiaryOperations`.
   - Domain entity `DiaryEntry` owning `entry_date`, `title`, `content_markdown` (exact Markdown preservation with whitespace), and soft-delete lifecycle.
   - Multiple entries permitted per date, bounded queries ordered by `entry_date DESC, id DESC`.
   - Real PostgreSQL integration suite: `DiaryIntegrationTest` (5 tests).

3. **`personal/` Module:**
   - Semantic capability `profile` with immutable `PersonalProfileView`, `PersonalProfileOperations`, and `Gender` enum.
   - Domain entity `PersonalProfile` owning `name`, `relationship`, `is_self`, `gender`, `birth_date`, `nationality_code`, `phone`, `email`, `address_id`, `occupation`, `notes_markdown`, and soft-delete lifecycle.
   - Nationality normalization (FR11-4): blank and whitespace strings consistently normalized to `null`, non-null codes validated via `ReferenceCatalog.country`, and rollback preservation.
   - Partial uniqueness constraint (FR11-6): specifically matches `uq_personal_profiles_one_active_self` before translating to `PersonalProfileConflictException`; non-uniqueness errors are not misdiagnosed.
   - Real PostgreSQL integration suite: `PersonalProfileIntegrationTest` (12 tests).

4. **Architecture & Modulith:**
   - Modulith named interfaces and package-info rules verified across `finance`, `journal`, and `personal`.
   - `ApplicationArchitectureTests` (23 tests), verifying allowed dependencies (`finance -> reference::catalog, reference::view`, `journal -> none`, `personal -> reference::catalog, reference::view, location::address, location::view`), cycle absence, and zero Vault/recycle bin attachments.

### Verification & Evidence
- Detailed evidence recorded in `docs/implementation/phase-11/test-evidence.md`.
- `mvn -f backend/pom.xml -ntp clean verify`: Exit status 0 (`BUILD SUCCESS`), 787 tests run, 0 failures, 0 errors, 0 skipped, 01:17 min.
- `git diff --check`: Exit status 0 (clean, no whitespace warnings or trailing blank lines).
- `powershell -ExecutionPolicy Bypass -File scripts/refresh-graphify.ps1`: Exit status 0 (4641 nodes, 16532 edges, 402 communities).

### Observed Compiler & JVM Notices
- Lombok terminally deprecated `sun.misc.Unsafe::objectFieldOffset` called by `lombok.permit.Permit` on Java 25.
- Deprecated API notices: `CsvImportParser` (frozen Phase 10) Apache Commons CSV methods, and `AbstractPostgresIntegrationTest` `PostgreSQLContainer` constructor.
- ByteBuddy / Mockito dynamic agent attachment warning on Java 25.
- SpringDoc default-enabled API-docs and Swagger UI endpoint startup warnings.

## Codex remediation

Remediation completed by Antigravity on 2026-10-04 addressing all findings from:
- `docs/implementation/phase-11/reviews/2026-10-04-phase-11-final-codex-rereview.md`:
  - FR11-1 resolved: 7-candidate weekday reconciliation + detachment, direct `entityManager.persist`, sequential reproduction test (`staleManagedWeekdayReaddedAfterExternalRemovalPersistsCleanly`), contention variant, and rollback preservation.
  - FR11-3 resolved: true preloaded parent AND child contexts in waiting transactions before PostgreSQL lock contention, competing state changes, fresh state recovery, and wallet balance verification.
  - FR11-5 resolved: 5 changed-link `updateSubscription` contention tests for both owner types across both winner orders with observed waits, scalar/link rollback, and canonical guard order (`Subscription -> RecurringRule -> Wallet`).
  - FR11-7 resolved: measured query-count regressions via Hibernate `Statistics` proving constant query count across small vs large limits for financial transactions and recurring rules list/due paths.
  - Preserved closed findings FR11-2, FR11-4, FR11-6, and FR11-8.
- `docs/implementation/phase-11/reviews/2026-10-04-phase-11-final-codex-second-rereview.md`:
  - FR11-9 resolved: enforced deliberate mutation-completion flushes across all mutating operations in `WalletService`, `TransactionCategoryService`, `FinancialTransactionService`, and `RecurringTransactionRuleService`. Completed mutations are reliably flushed before any subsequent guarded operation reloads state under pessimistic lock.
  - Preserved authoritative refresh under pessimistic lock in `FinanceLockManager` without blindly flushing stale preloaded state before lock acquisition, protecting both concurrent contention and single-transaction compositions.
  - Added dedicated PostgreSQL integration suite `FinancePublicServiceCompositionIntegrationTest` (10 tests) verifying:
    1. Wallet update followed by transaction/rule assignment preserves currency, opening balance, notes, and balance calculation.
    2. Wallet soft-delete followed by transaction entry assignment rejects and rolls back atomically.
    3. Wallet soft-delete followed by subscription assignment rejects and rolls back atomically.
    4. Wallet restore sequencing in single caller transaction allows subsequent transaction and subscription assignment.
    5. Category kind update to INCOME followed by incompatible EXPENSE transaction rejects and rolls back atomically.
    6. Category kind update to INCOME followed by incompatible EXPENSE rule rejects and rolls back atomically.
    7. Category kind update followed by compatible assignment preserves new category kind.
    8. Transaction scalar update followed by soft-delete in same caller transaction preserves scalar updates.
    9. Recurring rule scalar update followed by Subscription assignment preserves scalar updates and links.
    10. Recurring rule scalar update followed by soft-delete in same caller transaction preserves scalar updates.
  - Full verification: 787/787 tests green, clean verify passed, diff check clean, status updated to `IMPLEMENTED_AWAITING_CODEX_REVIEW`. Ready for `$codex-final-review`.

## Final review

- Outcome: `READY FOR OWNER COMMIT` on 2026-10-04; no blocking findings remain.
- Latest report: `docs/implementation/phase-11/reviews/2026-10-04-phase-11-final-codex-acceptance.md`.
- FR11-1 through FR11-9 closed after source/test review and independent clean verify: 787 tests, no failures,
  errors or skips; exit 0, 01:19, finished 2026-10-04T10:52:08+07:00. Diff check passed.
- Commit message: `feat(backend): implement phase 11 finance journal and personal foundations`.
- Next action: owner commits/pushes, then gives the latest package to ChatGPT for Phase 11 closeout and Phase 12
  preparation. No milestone is due until Phase 12 completion; no agent commit/push performed.
