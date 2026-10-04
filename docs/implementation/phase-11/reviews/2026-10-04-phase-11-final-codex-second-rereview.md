# Phase 11 Codex second final re-review

- Date: 2026-10-04
- Outcome: **CHANGES_REQUESTED**
- Scope: latest Phase 11 remediation against the active handoff; review-only.
- Baseline: `1fd0031d77a3c195c4b7f7d2af48d0e64ca75f7b`; implementation remains uncommitted.
- Prior report: [first final re-review](2026-10-04-phase-11-final-codex-rereview.md), retained as historical evidence.

## Verification

Codex independently ran `mvn -f backend/pom.xml -ntp clean verify`: exit 0, **BUILD SUCCESS**, 01:14,
finished 2026-10-04T10:18:44+07:00. Surefire reports contain **777 test cases**, with no failures/errors/skips.
Architecture and Flyway/Hibernate validation passed. `git diff --check` passed before review documentation edits.
No production fixes were made by Codex; a disposable diagnostic was added only under ignored `backend/target/`.

Compiler/runtime notices remain: Lombok Unsafe use, frozen CsvImportParser and test PostgreSQLContainer deprecated
APIs, Mockito/ByteBuddy dynamic agent attachment, and SpringDoc endpoint startup warnings. This is not an
IDE-clean, warning-free, Spotless, Checkstyle or SpotBugs verification claim.

## Prior findings closed

- FR11-1: all seven weekday identities reconciled/detached and replacements directly persisted; exact stale
  managed-identity reproduction, contention and rollback regressions are present.
- FR11-3: restore waiters preload parent and children before observed PostgreSQL contention; fresh scalar/child
  state and balance assertions are present.
- FR11-5: changed-link Subscription updates now cover both owner types, both winner orders, rejection rollback,
  retained historical links and canonical Subscription -> RecurringRule -> Wallet guard order.
- FR11-7: Hibernate Statistics measures constant statement counts for small/large bounded transaction and
  recurring list/due reads, alongside order, limit and complete-child assertions.
- FR11-2/4/6/8 remain closed. **All FR11-1 through FR11-8 are closed in this revision.**

## New finding requiring remediation

### FR11-9 — High: guarded refresh silently discards earlier writes in the same caller transaction

Affected sources under `backend/src/main/java/com/vhvkhangg/personalprivatevault/finance/internal/application/`:

- `FinanceLockManager.java:39,63,75,87,99`: lock then unconditional `EntityManager.refresh`.
- `WalletService.java:90-100,145-157`: update/delete/restore mutate managed state and return without flushing.
- `TransactionCategoryService.java:109-115`: category update returns without flushing its mutation.
- `FinancialTransactionService.java:136` and `RecurringTransactionRuleService.java:168`: parent scalar update
  follows child flushing, leaving the subsequent parent mutation vulnerable to another guard refresh.

Public service operations join the caller transaction. A subsequent operation reusing the same guarded owner
refreshes its managed instance from PostgreSQL, losing the earlier operation's unflushed changes. The added
Transaction/Rule lifecycle flushes fix some sequences but do not make the shared protocol safe consistently.

Codex reproduced two public-service sequences inside one `TransactionTemplate` callback against isolated,
Flyway-migrated PostgreSQL 18.6:

1. Create wallet Original/USD/opening 0; in one caller transaction update it to Updated/VND/opening 123, then
   create an INCOME transaction of 1 referencing it. Commit and fresh reload returns **Original/USD/0.0000**.
2. Create category BOTH; in one caller transaction update it to INCOME, then create an EXPENSE transaction of -1
   using that category. The incompatible expense **succeeds**, and fresh reload returns category **BOTH**.

Diagnostic artifact: `backend/target/final-review-diagnostics/CallerTransactionDiagnostic.java` (ignored, ephemeral).
Java source launch used the Surefire XML `java.class.path`; exit 0 with:
`WALLET_COMMITTED=Original,USD,0.0000` and `INCOMPATIBLE_EXPENSE_ACCEPTED;CATEGORY_COMMITTED=BOTH`.
The diagnostic container was stopped; no configured vault database was used.

This is silent data loss and bypasses the category invariant and caller-visible ordering of mutations, despite a
green suite. Wallet lifecycle and aggregate scalar/child coherence are exposed to the same refresh protocol.

### Required remediation and proof

Preserve completed owner-local service mutations before a subsequent guarded operation in the same caller
transaction. Deliberate mutation-completion flushing or an equivalent owner-local dirty-state protocol is
acceptable. Do not blindly flush externally stale preloaded state before acquiring a guard: retain authoritative
refresh after actual contention and the established lock order. Audit Wallet update/delete/restore, Category
update, and Transaction/Rule parent mutations consistently; keep changes within the existing Finance scope.

Add real PostgreSQL public-service composition tests using an outer caller transaction:

- Wallet update followed by transaction/rule assignment preserves currency, opening balance and metadata.
- Wallet soft-delete followed by a new entry/Subscription assignment rejects, rolls back atomically, and does not
  refresh the wallet back to an assignable state; cover restore sequencing as applicable.
- Category kind update followed by incompatible transaction/rule assignment rejects atomically; compatible
  assignment preserves the new category kind.
- Transaction/Rule scalar update followed by another guarded mutation preserves coherent scalar/child state,
  including relevant lifecycle or Subscription validation sequences.

Assert fresh-context committed state and rejection/rollback preservation, retain all existing contention and
query-count regressions, then rerun clean verify and diff check. No global context clear, REQUIRES_NEW, schema
change, generic retry framework or unrelated frozen-module refactor is authorized.

## Review coverage and disposition

Java/Spring, pragmatic SOLID, reuse/consistency, pattern selection, modular ownership, PostgreSQL/JPA, testing and
Finance/Journal/Personal modeling skills guided the review. Existing owner-local guards and batched reads remain
appropriate; no new framework or architecture change is needed. Graphify was navigation only, with conclusions
verified in source and PostgreSQL. Frozen schema/module boundaries are unchanged; no new security/logging or
query-shape blocker was found. The confirmed transaction correctness defect prevents acceptance.

Next: Antigravity `/antigravity-implement-handoff` remediates **FR11-9**, updates evidence and the active handoff,
then the owner invokes `$codex-final-review`. **No owner commit is authorized by this review.**
