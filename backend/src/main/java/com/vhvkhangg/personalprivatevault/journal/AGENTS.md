# Journal Module Agent Instructions

Applies recursively to `com.vhvkhangg.personalprivatevault.journal`.

## Ownership

Journal owns only `diary_entries`.

Journal has no application-module dependency and is not Vault-backed.

## Invariants

- multiple diary entries per calendar day are allowed;
- non-null `content_markdown` is preserved exactly;
- title is optional and max 500;
- default reads exclude soft-deleted rows;
- soft delete/restore is Journal-owned;
- date/range reads require a positive bound and deterministic `entry_date DESC, id DESC`;
- no global/full-text Search implementation in Phase 11;
- no hard delete.

## Phase gate

Production changes require Phase 11 `READY FOR HANDOFF` plus an active Phase 11 handoff.
