# ADR-0011 — Use Ledger Entries as Finance Source of Truth

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

Finance supports income, expense, wallet-to-wallet transfer, recurring transactions, subscriptions, and multiple currencies. A mutable wallet balance alone would be hard to audit/reconcile and could diverge from transaction history.

## Decision

Persist wallet opening balance plus transaction ledger entries. Derive current balance from posted, non-deleted entries. Cross-currency transfers use source/destination wallet entries and may record an exchange rate.

## Rationale

The ledger preserves an explainable financial history and prevents a cached balance from becoming a second authoritative truth.

## Consequences

- Transaction invariants must be validated.
- Pending/cancelled/deleted transactions do not contribute to current balance.
- Balance may be cached later only as a derived optimization with reconciliation.

## Alternatives considered

- **Store and directly mutate `current_balance`:** rejected as the primary truth because it can drift from transaction records.
