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


## 15. Phase 17 Alternative B contract and current candidate

Accepted [ADR-0020](../adr/0020-phase-17-single-job-reservations-e1.md) amends
ADR-0019/P17-1/2/3. [Additive DBML](../database/phase-17-alternative-b.dbml) is
the canonical logical target. V3–V13 remain historical unaccepted RAG migrations;
no rewrite/execution here. Approved future changes append after V13 and add
Vault-owned stable source/version/reset/reservation metadata with mandatory
owner-local SQL guard/version backstops, preserving original business ownership.

RAG reuses jobs/grants/staging/dependencies/generations/runs, adds one static
nonterminal unique constraint, one-use grant marker, process identity and staging
seal, and atomically finishes P-or-abort/terminal/release. No global V/F/DRAIN
tables, worker Vault/business SQL or reverse Java dependency. Runtime roles remain
non-owner; fixed audited definer scope and actual effective-role tests required.

Current code's blanket startup consent revocation/work-TTL cleanup and deferred
deadline triggers are not Alternative B compliance. Target preserves standing
consent/completed CURRENT indexes across ordinary restart and completed work
expiry; E1 explicitly permits valid late trusted P while keeping fresh gates.
Historical job success is separate from effective CURRENT/OUTDATED/revoked state.
Monotonic versions/reset/scope epochs prevent event/hash/edge replay; verified
cleanup is separate from safe process/writer release. No new runtime proof here;
current containment remains and all implementation acceptance gates stay open.

## 16. Google Drive connection authority schema

GDI26-S1 implementation is submitted for separate independent review; default OFF. See the [handback](../implementation/maintenance/2026-11-05-google-drive-integration-preparation/implementation-handback-s1-2026-11-06.md).

Flyway migration `V25__drive_connections.sql` introduces the additive `public.drive_connections` table, documented in canonical DBML at:
[`../database/google-drive-connections.dbml`](../database/google-drive-connections.dbml)

The table stores encrypted token custody and connection metadata for up to two connection slots:
- `slot SMALLINT PRIMARY KEY CHECK (slot IN (1, 2))`
- `google_sub VARCHAR(255) NOT NULL UNIQUE` (enforces single slot assignment per verified Google account)
- `display_email VARCHAR(320)` nullable, display only
- `status VARCHAR(32) NOT NULL CHECK (status IN ('CONNECTED', 'RECONNECT_REQUIRED'))`
- `encrypted_refresh_token BYTEA` (version 0x01 + 12-byte IV + AES-256-GCM ciphertext + 16-byte tag; NULL when RECONNECT_REQUIRED)
- `key_id VARCHAR(64)` (external key reference; NULL when RECONNECT_REQUIRED)
- `credential_epoch UUID NOT NULL UNIQUE` (fresh random UUID generation for equality CAS, not monotonic)
- Table-level check constraint `chk_drive_connections_credentials` requires nonempty ciphertext/nonblank key_id for CONNECTED and both NULL for RECONNECT_REQUIRED; `chk_drive_connections_status` restricts the two status strings.

As security-sensitive credentials, `drive_connections` is classified as an excluded security table (`PortabilitySnapshotAdapter.EXCLUDED_SECURITY_TABLES`) and is excluded from portability export archives. The table does not participate in the frozen `vault_entries` shared identity.

## 17. GDI26-S2 root and upload action metadata

The exceptional S2 implementation adds only V26, with two Drive-owned relations described in [google-drive-files.dbml](../database/google-drive-files.dbml) and [ADR-0027](../adr/0027-google-drive-root-file-authority.md). The [independent remaining-verification review](../implementation/maintenance/2026-11-06-gdi26-s2-preparation/reviews/2026-11-07-gdi26-s2-independent-media-verification-review.md) records READY_FOR_OWNER_COMMIT.

`drive_folder_roots` retains a unique reserved provider ID, root-binding UUID and binding epoch per verified Google subject. `drive_upload_intents` retains each globally unique timestamped action key, fingerprint, metadata/digest, reserved provider file ID, phase and equality-CAS epochs under its subject/root foreign key. Uncertain outcomes remain retained; only terminal rows older than 30 days may be pruned (at most 32 per admission). No bytes, access tokens or resumable session URLs are stored. Both relations are excluded from Portability along with S1 credentials; neither participates in Vault identity. V1–V25 and module dependencies are unchanged.
