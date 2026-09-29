---
name: account-domain-modeling
description: Guide Vault-backed external accounts, current relationship state, historical follower snapshots, Reference platform validation, and race-safe account/history persistence.
---

# Account Domain Modeling

Use for Phase 7 Account planning, implementation, testing, or review.

## Ownership

`account` owns:

- `external_accounts`
- `external_account_relationships`
- `follower_snapshots`
- `follower_snapshot_entries`

It depends only on public Vault and Reference contracts.

## External Account identity

Every External Account is a Vault Entry with type `EXTERNAL_ACCOUNT` and the same ID. Create both in one transaction.

Validate `platform_id` through `ReferenceCatalog`. Require at least one of username, external ID, or URL as frozen
Schema v1 does.

Non-null `(platform_id, external_id)` is uniquely constrained. Duplicate creates are conflicts; concurrent duplicates
must translate to a stable Account-domain conflict. Do not invent username/URL uniqueness.

Treat External IDs as private values for logging. The concurrent unique-conflict regression must capture logs across
the competing worker thread and prove that a distinctive External ID marker and raw PostgreSQL vendor `Detail: Key`
output are absent while the domain conflict remains observable.

## Relationship row

There is one current relationship row per owner/target pair.

Keep follower direction and follow direction distinct. Enforce owner != target. Prefer one canonical set/update
operation rather than competing insert APIs.

Concurrent same-pair writes must preserve one row and one coherent command result; do not leak raw persistence
exceptions or construct a state by mixing fields from competing commands.

## Follower snapshots

Snapshots are historical captures, not live relationship rows.

Create the snapshot header and submitted entries atomically. Phase 7 exposes batch snapshot creation only; completed
snapshots are immutable and there is no public append-entry operation.

For repeated entries with the same target inside one submitted snapshot:

- identical normalized historical copies collapse to one entry;
- differing username/display-name/external-ID/profile-URL copies reject the entire snapshot command with a stable
  Account-domain validation error;
- never use first-wins/last-wins or merge conflicting fields.

Preserve snapshot copies of username/display name/external ID/profile URL. Do not replace them with only live current
account fields.

Do not require reported follower total to equal the number of stored entries.

## Manual/import/API provenance

`relationship_source` and `follower_snapshot_source` describe provenance independently of current follow/follower
status. Manual entry/import remains valid where platform APIs are unavailable.

Do not add API clients, OAuth, scraping/browser automation, or background sync in this phase.

## YouTube channel compatibility

`YOUTUBE_CHANNEL` is an External Account type. Keep enough stable public account/view data for later Study code to
validate a referenced channel without accessing Account internals.

Do not add a Study dependency or implement Knowledge in Phase 7.

## Reads

Keep account/history reads ID- or owner/snapshot-scoped and explicitly bounded where collections can grow.

Do not expose global unbounded account, relationship, snapshot, or snapshot-entry lists.

## Public API

Prefer semantic `ExternalAccountOperations`, `ExternalAccountRelationshipOperations`, and
`FollowerSnapshotOperations`, immutable views, and stable enums.

Do not expose JPA entities/repositories or create generic CRUD bases / `ServiceImpl` conventions.

## Testing

Use PostgreSQL Testcontainers. Cover Vault rollback, Reference platform validation, identifier checks, external-ID
unique races plus privacy-safe captured-log assertions, relationship same-pair concurrency, snapshot atomicity,
identical duplicate collapse, conflicting duplicate snapshot rejection, bounded reads, exact Modulith dependencies,
and unchanged Flyway/Hibernate validation.

Use observable PostgreSQL contention for race claims rather than timing-only sleeps.
