# Backend Phase 7 — External Account & Relationship History Foundation

Status: **PREPARED — READY FOR HANDOFF** after the [Codex re-review](reviews/2026-09-29-phase-7-pre-handoff-codex-rereview.md).

Implementation final acceptance re-review: **READY FOR OWNER COMMIT**. See the [acceptance review](reviews/2026-09-29-phase-7-final-codex-acceptance-review.md).
Phase 7 is not complete/frozen.

Phase 7 implements the `account` module after the frozen Vault/Reference foundations and before Phase 8 Knowledge,
because Study may later reference a stored YouTube channel account.

The Phase 4–6 milestone is `MILESTONE_READY`, its review/status documents are committed/pushed, and the required
post-milestone synchronization/reset is complete.

No Phase 7 production implementation is authorized until:

1. Phase 7 passes `$codex-pre-handoff-review`;
2. the approved Phase 7 preparation slice is committed/pushed;
3. `$codex-create-handoff` creates an active Phase 7 implementation handoff.

## Current gate

The preparation slice was owner committed/pushed and the implementation handoff was created. Antigravity's
implementation and test-only remediation passed Codex final acceptance re-review. Owner commit/push is pending.

## Owned Schema v1 tables

- `external_accounts`
- `external_account_relationships`
- `follower_snapshots`
- `follower_snapshot_entries`

No schema/Flyway change is planned.

## Frozen dependency direction

At whole-module level, `account` may depend only on:

- `vault`
- `reference`

Expected named-interface dependencies to verify during implementation:

- `vault::entry`
- `vault::enums`
- `vault::view`
- `reference::catalog`
- `reference::view`

Do not change `account/package-info.java` during preparation. Exact dependency narrowing belongs to the active Phase 7
implementation handoff.

## External-account Vault identity

Every `external_accounts` row is Vault Entry-backed:

```text
external_accounts.id == vault_entries.id
vault entry type == EXTERNAL_ACCOUNT
```

Create the Vault Entry and External Account row in one transaction. Failure must not leave an orphan Vault Entry.

Vault continues to own favorite/rating/tag/recycle behavior for `EXTERNAL_ACCOUNT`; Account must not duplicate it.

## External account contract

Respect frozen Schema v1:

- required `platform_id`, validated through public `ReferenceCatalog.platform(id)`;
- required ownership: `OWNED` or `TRACKED`;
- required account type: `SOCIAL`, `GAME`, `YOUTUBE_CHANNEL`, or `OTHER`;
- at least one identifier is present: `username`, `external_id`, or `url`;
- optional display/profile/artwork/owner/notes metadata.

YouTube channels use `account_type = YOUTUBE_CHANNEL`; username may hold `@handle`, `external_id` may hold the channel
ID, and URL may hold the canonical channel URL.

Manual entry/import remains valid where a platform API is unavailable.

### External-ID uniqueness

Frozen Schema v1 uniquely constrains `(platform_id, external_id)` when `external_id` is non-null.

- duplicate create for the same non-null platform/external-ID pair is a stable Account-domain conflict, not
  idempotent reuse;
- concurrent duplicates have one winner and domain-conflict losers;
- PostgreSQL uniqueness is the final race arbiter;
- do not invent username or URL uniqueness because Schema v1 does not define it.

### Privacy-safe external-ID conflict diagnostics

The new `(platform_id, external_id)` conflict path must preserve the privacy-safe constraint-logging baseline from the
Phase 4–6 maintenance.

The Phase 7 handoff must require a PostgreSQL-backed concurrent duplicate regression that:

1. uses the same valid Platform with a distinctive private External ID marker;
2. deterministically forces both create attempts past any application pre-check so the loser reaches the PostgreSQL
   unique constraint;
3. captures application logs across the competing worker thread;
4. preserves the stable Account-domain conflict for the loser;
5. asserts the private External ID marker does **not** appear anywhere in captured logs;
6. asserts raw PostgreSQL vendor detail such as `Detail: Key (platform_id, external_id)=(` does **not** appear;
7. preserves only non-sensitive diagnostics if any are emitted, such as SQLState, constraint name, or stable event
   code, without logging rejected values or raw throwable/root-cause messages.

This is a regression requirement for the existing logging policy, not authorization for another logging redesign.
Do not weaken the privacy-safe Hibernate logger configuration merely to make the new conflict path observable.

## Relationship state

`external_account_relationships` owns one current relationship row per `(owner_account_id, target_account_id)`.

Both accounts must exist and owner must differ from target.

The row tracks:

- `follower_status`: whether target follows owner — `CURRENT_FOLLOWER`, `NO_LONGER_FOLLOWING`, `NOT_FOLLOWING`,
  `UNKNOWN`, or null where the frozen schema permits;
- `follow_status`: whether owner follows target — `FOLLOWED`, `NOT_FOLLOWED`, `UNKNOWN`;
- `source`: `MANUAL`, `SNAPSHOT`, `IMPORT`, or `API`;
- `has_liked_post`;
- optional note.

Use one canonical set/create-or-update capability per owner/target pair rather than competing duplicate rows.
Repeating the same requested state should be idempotent.

Concurrent writes to the same pair must preserve the unique-pair invariant and must not leak raw persistence errors.
If competing commands request different states, do not invent hidden merge precedence; serialize or use an explicit
single-row update strategy whose final committed state corresponds to one complete command, not a torn mix.

Do not infer that `has_liked_post` applies only to Douyin at the persistence/API contract level; Schema v1 stores the
flag generically even though Douyin is a primary use case.

## Follower snapshots

`follower_snapshots` is historical capture metadata for one owner External Account.

A snapshot stores owner, capture time, source (`MANUAL`, `IMPORT`, `API`), optional reported total count, optional
imported file name, and created time.

`follower_snapshot_entries` stores the captured target set plus snapshot copies of username/display name/external ID/
profile URL so historical display data is not lost when the current External Account later changes.

Snapshot creation should be transactional across the snapshot header and its submitted entries. A failure must not
leave a partial snapshot.

Phase 7 uses **batch snapshot creation** as the only public write contract for snapshot entries. A completed snapshot
is immutable in this phase; do not expose a separate public `addEntry`/append operation after snapshot creation.

Within one submitted snapshot command, group entries by `target_account_id` before persistence:

- if repeated entries for the same target have identical normalized historical copies
  (`username_snapshot`, `display_name_snapshot`, `external_id_snapshot`, `profile_url_snapshot`), collapse them to one
  entry;
- if any of those historical copies differ for the same target, reject the **entire snapshot command** with a stable
  Account-domain validation error (for example `InvalidFollowerSnapshotException`) before commit;
- never choose first-wins/last-wins arbitrarily and never merge fields from conflicting copies;
- the rejected command must commit neither the snapshot header nor any snapshot entry.

This makes historical capture deterministic and prevents arbitrary/torn copies. No independent concurrent public
entry-add operation is approved in Phase 7, so no concurrent-add winner policy is needed. A future append API would
require a separate owner-approved contract.

Do not invent uniqueness across different snapshots.

Do not require `reported_total_count == entry count`; frozen Schema v1 stores reported count separately.

## Snapshot versus current relationship state

A snapshot is historical evidence; it is not itself the canonical mutable relationship row.

Phase 7 may create/update relationship rows from an explicitly requested snapshot/import workflow, but storing a
snapshot alone must not silently mutate relationship status. Do not create placeholder events without a concrete
consumer/side effect.

## Bounded public reads

Expose only bounded/account-scoped reads, such as:

- External Account by ID;
- Relationship for one owner/target pair;
- recent relationships for one owner with an explicit bound;
- Snapshot by ID;
- recent snapshots for one owner with an explicit bound;
- entries for one snapshot with an explicit bound.

Do not add global unbounded account/relationship/snapshot lists or global search. Avoid a repository-wide pagination
framework solely for this phase; a narrow bounded contract is sufficient.

## Proposed public package direction

```text
account/
├── account/
│   ├── package-info.java
│   └── ExternalAccountOperations.java
├── relationship/
│   ├── package-info.java
│   └── ExternalAccountRelationshipOperations.java
├── snapshot/
│   ├── package-info.java
│   └── FollowerSnapshotOperations.java
├── enums/
│   ├── package-info.java
│   └── stable Account enums
├── view/
│   ├── package-info.java
│   └── *View.java
└── internal/
    ├── application/
    ├── domain/
    └── infrastructure/persistence/
```

Commands/exceptions may live in child packages while remaining part of the parent logical named interface, following
the verified People/Fiction/Film organization.

Do not introduce generic CRUD bases, `Service` / `ServiceImpl`, adapters for hypothetical external APIs, scraping
automation, or event classes without a concrete consumer.

## Required invariants for the future handoff

- External Account/Vault Entry identity is transactionally consistent;
- platform validation uses public Reference contracts;
- at least one account identifier is present;
- non-null `(platform_id, external_id)` duplicate creation has stable race-safe conflict semantics;
- relationship owner and target both exist and are different;
- one canonical relationship row exists per owner/target pair;
- relationship updates are whole-command coherent under concurrent writes;
- snapshot header + submitted entries commit atomically;
- identical duplicate snapshot entries for one target collapse idempotently, while conflicting historical copies
  for the same target reject the entire snapshot command;
- historical snapshot text values remain historical snapshots rather than live joins;
- public APIs return immutable views and never expose JPA entities/repositories;
- cross-module internals remain inaccessible.

## Testing contract for the future handoff

Require PostgreSQL Testcontainers coverage for:

- all four Account-owned tables against unchanged Flyway V1/Hibernate validation;
- External Account/Vault rollback;
- identifier check and platform validation;
- duplicate non-null `(platform_id, external_id)` sequential/concurrent conflict with observable PostgreSQL contention;
- privacy regression for that concurrent external-ID conflict using a distinctive private marker, captured worker-thread
  logs, absence of the marker and raw PostgreSQL `Detail: Key (platform_id, external_id)=(` output, and preservation
  of the stable Account-domain conflict;
- username/URL duplicates remaining permitted when the frozen unique key does not apply;
- relationship self-reference rejection;
- relationship set/update semantics and concurrent same-pair writes without raw persistence leakage;
- transactional batch snapshot creation and rollback;
- identical duplicate snapshot entries for one target collapse to one committed entry;
- conflicting duplicate historical copies for one target reject the whole snapshot and commit neither header nor
  entries;
- no public post-creation snapshot-entry append API in Phase 7;
- bounded reads and deterministic ordering;
- exact Spring Modulith named-interface dependencies;
- final `mvn -f backend/pom.xml clean verify` on Java 25;
- `git diff --check`.

Race tests must prove the competing operation reached PostgreSQL contention/serialization rather than relying on
timing-only sleeps.

Do not use H2.

## Out of scope

- live platform API clients, OAuth, scraping, browser automation, schedulers, or background sync;
- feed/importdata/knowledge implementation;
- global search;
- REST/controllers/OpenAPI;
- frontend;
- schema migrations/redesign;
- automatic relationship-status mutation from merely storing a snapshot;
- account deletion/permanent-delete behavior unless separately approved;
- changes to frozen Vault/Reference production behavior.

## Preparation tooling

Added for Phase 7:

- `.agents/skills/account-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-7-account.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/account/AGENTS.md`

Reused without new custom agents/hooks:

- `backend-implementer`
- `architecture-auditor`
- repository safety hook

No new custom agent or hook is justified for Phase 7.
