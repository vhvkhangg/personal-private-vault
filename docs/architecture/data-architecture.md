# Data Architecture

## 1. Database role

PostgreSQL stores structured application data and metadata. It does **not** store large image/media binaries.

The frozen logical schema is:

[`../database/personal-private-vault-schema-v1-FROZEN-final.dbml`](../database/personal-private-vault-schema-v1-FROZEN-final.dbml)

## 2. Source-of-truth lifecycle

Before backend implementation, the frozen DBML is the logical schema baseline.

After Flyway is introduced:

1. Flyway migrations become the executable database source of truth.
2. Schema changes are append-only migrations; do not edit an already-applied migration to rewrite history.
3. The DBML/ERD is updated after an accepted schema change so architecture documentation remains synchronized.
4. JPA mappings follow the migrated schema; Hibernate automatic schema mutation is not the production migration mechanism.

## 3. Permanent single-user model

The system permanently supports one application account.

Consequences:

- domain tables do not carry a repeated `user_id` tenant column;
- `app_users` exists only for authentication/account security;
- there is no registration/multi-user administration domain;
- one-time bootstrap creates the initial account.

## 4. Shared vault identity

Content-capable records share a `vault_entries` identity. This allows common capabilities to remain normalized:

- favorites;
- ratings;
- tags;
- recycle-bin lifecycle;
- global identity for cross-cutting operations.

Subtype tables use the vault-entry ID as their identity where applicable.

`film_credit` is intentionally a vault entry because it can be favorited, while the application prevents unsupported capabilities such as rating/tagging that credit.

## 5. Capability enforcement

Not every `vault_entry_type` supports every cross-cutting capability.

The application/domain layer must maintain a capability matrix for:

- favoriteable types;
- rateable types;
- taggable types.

Database constraints/triggers may be added where they remain clear and maintainable, but capability semantics must not be inferred merely from the existence of a `vault_entry` row.

## 6. Recycle bin

Soft-deletable records use a deletion timestamp. Trash is retained until explicitly permanently deleted; there is no automatic 30/60/90-day purge in v1.

Restore and permanent-delete behavior must respect aggregate ownership and foreign-key dependencies.

## 7. Reference data

Reference tables use stable identifiers where appropriate:

- country code: ISO 3166-1 alpha-2;
- currency code: ISO 4217 where applicable;
- language code: application-supported language code;
- platform reference: normalized internal table.

A business entity has at most one nationality where the current schema models nationality.

## 8. Money

Monetary values use decimal numeric types plus a currency code; floating-point types must not be used for persisted money.

Price ranges store minimum/maximum values with one currency.

Finance is multi-currency. Cross-currency transfers are represented by ledger entries in wallets with their own currencies; an exchange rate may be recorded for the transaction.

## 9. Finance ledger

Wallet `currentBalance` is derived, not an independent authoritative column:

```text
opening_balance
+ sum(posted, non-deleted ledger entry deltas)
= current balance
```

Income adds a positive entry, expense adds a negative entry, and transfer normally produces source and destination entries.

This prevents mutable cached balance from becoming a second source of truth.

## 10. Derived values

Examples such as album `imageCount` are API/view-model values derived from underlying rows. They are not stored as an independent authoritative count in v1.

## 11. Time

- `created_at`, `updated_at`, event times, and transaction times are stored as UTC-capable timestamps (`timestamptz`).
- The application setting stores the user's display timezone.
- Birthdays and other date-only facts use `DATE`, not timestamps.

## 12. Flexible content

Long-form notes and imported Obsidian/AI documents retain Markdown text. Unknown YAML frontmatter is preserved as JSONB instead of discarded.

JSONB is used for metadata that is naturally source-dependent or heterogeneous, not as a replacement for relational modeling of stable business fields.

## 13. Search-supporting indexes

Search implementation may add PostgreSQL indexes/extensions such as `pg_trgm` and full-text indexes through Flyway. Indexes are driven by real query behavior and do not change module ownership.

Vietnamese accent-insensitive matching is not a requirement. Case-insensitive, partial, fuzzy, and filtered search are required.

## 14. Phase 15 implementation notes

For runtime Flyway ordering before Hibernate validation and transactional rollback guarantees across modules, see:
[`phase-15-implementation-notes.md`](phase-15-implementation-notes.md)
