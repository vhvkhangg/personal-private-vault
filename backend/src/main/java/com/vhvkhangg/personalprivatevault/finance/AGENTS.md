# Finance Module Agent Instructions

Applies recursively to `com.vhvkhangg.personalprivatevault.finance`.

## Ownership

Finance owns exactly:

- `wallets`
- `transaction_categories`
- `financial_transactions`
- `financial_transaction_entries`
- `recurring_transaction_rules`
- `recurring_rule_weekdays`
- `recurring_rule_entries`
- `subscriptions`

## Boundary

Allowed application-module dependency: public `reference` only.

Finance is not Vault-backed.

## Core invariants

- BigDecimal money only; no silent numeric rounding.
- Current balance = opening balance + POSTED/non-deleted ledger deltas.
- INCOME: one positive entry.
- EXPENSE: one negative entry.
- TRANSFER: two distinct wallets, one negative + one positive.
- Category kind must match Income/Expense or BOTH; Transfer category is null.
- Transaction entry replacement is atomic and same-transaction mutation is row-lock/fresh-state safe.
- Wallet currency change is rejected while any currently retained transaction/recurring entry row references it;
  after a committed full replacement removes the last retained reference, currency may change again.
- Wallet currency mutation and every entry-assignment path share wallet write guards; lock multiple wallets in
  ascending wallet ID order.
- Do not persist or infer an ever-referenced history marker absent from Schema v1.
- Category kind changes must remain compatible with every historical transaction/rule reference and share a
  category guard with category assignment.
- Recurring update/full child replacement/soft-delete/restore share one recurring-rule write guard and refresh
  parent/child state under that guard; deleted recurring rules reject update until restored.
- Required contention tests prove actual PostgreSQL lock waiting/index competition and coherent persisted
  scalar + child state; timing-only sleeps are insufficient.
- Recurring entries use the same shape rules.
- Frequency-specific schedule fields follow the canonical Phase 11 README.
- Phase 11 does not auto-post recurring rules or subscriptions.
- Subscription recurring-rule uniqueness is PostgreSQL-arbitrated.
- Soft delete is Finance-owned, not Vault recycle behavior.
- Reads are bounded and deterministic.

## Phase gate

Production changes require Phase 11 `READY FOR HANDOFF` plus an active Phase 11 handoff.

## Phase 13 HTTP adapter exception

An accepted active Phase 13 handoff may modify this otherwise-frozen module only to add the REST/JSON adapter work
authorized by `docs/implementation/phase-13/README.md`.

Allowed:

- owner `internal/web` controller/request/response DTO/mapper/advice packages;
- mapping to existing owner operations/facades;
- HTTP Bean Validation and OpenAPI annotations;
- module exception-to-HTTP translation;
- tests needed for this HTTP surface.

Not allowed:

- changing existing domain/application invariants or persistence behavior;
- importing another module's internal/repository/entity;
- exposing application entities or internal Search contracts;
- inventing new business operations solely for HTTP convenience;
- schema/Flyway changes;
- Phase 14+ work.

The active Phase 13 handoff, when present, is the temporary authority for this narrow adapter exception. Otherwise
the frozen-module rules remain in force.
