---
name: finance-journal-personal-domain-modeling
description: Guide Phase 11 Finance ledger, Journal Markdown entries, and Personal profile modeling over frozen Schema v1.
---

# Finance + Journal + Personal Domain Modeling

Use for Phase 11 planning, implementation, tests, and review.

## Module boundaries

- Finance -> public Reference only.
- Journal -> no application-module dependency.
- Personal -> public Reference + Location Address only.
- None of these three modules is Vault-backed.

## Finance

- BigDecimal only for persisted money; never silently round to fit numeric columns.
- Wallet balance is derived from opening balance + POSTED/non-deleted ledger deltas.
- PENDING/CANCELLED/deleted transactions contribute zero.
- INCOME = exactly one positive entry.
- EXPENSE = exactly one negative entry.
- TRANSFER = exactly two distinct wallets, one negative + one positive.
- Transfer category is null; Income/Expense categories must match kind or BOTH.
- Transaction updates replace scalar state + complete entry set atomically.
- Same-transaction mutations use an owner-local write guard and fresh state under that guard.
- Wallet currency change is rejected only while a currently retained transaction/recurring entry row references it;
  after full replacement removes the last retained reference, currency may change again. Currency update and entry
  assignment share the wallet guard, with multi-wallet locks acquired by ascending wallet ID.
- Do not infer permanent ever-referenced history that frozen Schema v1 does not retain.
- Category kind changes must remain compatible with all historical transaction/rule references and share a
  category guard with assignment.
- Existing recurring-rule update/delete/restore uses one rule-row guard and fresh parent/child state; deleted
  rules reject update until restored.
- Concurrency regressions prove actual PostgreSQL waiting, not timing-only overlap; Subscription uniqueness
  races must reach the database unique index after application pre-checks.
- Recurring rules validate schedule-field compatibility and use the same ledger shape rules.
- Phase 11 stores recurring/subscription schedule metadata but does not run a scheduler or auto-post charges.
- Subscription recurring-rule uniqueness is PostgreSQL-arbitrated and translated to a stable privacy-safe conflict.
- Soft delete is module-owned, not Vault-owned.

## Journal

- Multiple entries per date are allowed.
- Preserve non-null Markdown exactly.
- Date/range reads are positive-bounded and deterministic.
- Soft delete/restore is module-owned.
- No global search in Phase 11.

## Personal

- `relationship` is a free-form string, not an enum.
- Country validation uses public Reference; address validation uses public Location Address.
- At most one non-deleted `is_self = true` profile.
- The partial unique index is the final race arbiter; translate conflicts safely.
- Soft-deleting self frees the slot; conflicting restore must fail stably.
- Preserve non-null notes Markdown exactly.

## Testing

Use real PostgreSQL Testcontainers for ledger balance, row-lock contention, recurring-rule/subscription uniqueness,
Personal active-self races, soft-delete/restore, schema fidelity, and Spring Modulith boundary verification.
