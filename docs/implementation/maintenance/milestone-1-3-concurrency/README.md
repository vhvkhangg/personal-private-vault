# Phase 1–3 Milestone Concurrency Maintenance

Status: **COMPLETE — FROZEN (2026-09-28)**

The [2026-09-28 Codex final review](reviews/2026-09-28-concurrency-maintenance-final-codex-review.md)
accepted the two focused test corrections on re-review. The owner committed/pushed this maintenance slice
as `3a9294d`; the Phase 1–3 milestone re-review returned `MILESTONE_READY`. The completed
[maintenance handoff](handoff.md) is archived. Phase 4 now awaits its own pre-handoff review.

Owner approval: **2026-09-28**

Source finding:
[`../../phase-3/reviews/2026-09-28-phase-1-3-milestone-codex-review.md`](../../phase-3/reviews/2026-09-28-phase-1-3-milestone-codex-review.md)

This is a single, narrowly scoped maintenance implementation slice for the two blocking concurrency defects found
by the Phase 1–3 milestone review. It temporarily permits changes to frozen Phase 1 Vault metadata behavior and
frozen Phase 2 refresh-token behavior only where required below.

Phase 4 remains blocked.

## Goal

Resolve exactly these defects:

1. a concurrent refresh-token revoke can overwrite the replacement link committed by rotation;
2. concurrent idempotent favorite/tag-attachment operations can fail with primary-key conflicts.

No other Phase 1–3 production behavior is reopened.

## Finding A — Refresh rotation versus revocation

### Current defect

`SessionService.rotate(...)` locks the predecessor refresh-token row, but `SessionService.revoke(...)` currently
loads it without that lock. Because `RefreshToken` has no optimistic version and normal Hibernate updates can include
`replaced_by_token_id`, a stale revocation write can erase a replacement link written by a concurrent rotation.

### Required behavior

Rotation and revocation of the same predecessor must serialize on that predecessor row, or use an equivalently safe
versioned/conditional mechanism.

The externally observable outcomes are:

- **rotation wins first**
  - exactly one successor is committed;
  - predecessor is revoked;
  - predecessor `replaced_by_token_id` still points to that successor after concurrent revoke completes;
  - revoke remains idempotent/successful;
- **revocation wins first**
  - predecessor is revoked;
  - no successor is created;
  - rotation fails closed with the existing invalid-refresh-token contract.

Never allow a committed successor whose predecessor replacement link is later cleared.

Do not add a schema migration or `@Version` column unless Codex finds that absolutely necessary; the preferred
minimal correction should reuse the existing predecessor row-lock contract if it satisfies the behavior.

### Likely implementation targets

Keep the handoff narrow. Expected targets are:

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/application/session/SessionService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/infrastructure/persistence/RefreshTokenRepository.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/RefreshTokenLifecycleIntegrationTest.java`

Touch `RefreshToken.java` only if the selected minimal safe design genuinely requires it. Schema/Flyway changes are
out of scope.

## Finding B — Concurrent Vault favorite/tag attachment

### Current defect

`VaultMetadataService.favorite(...)` and `attachTag(...)` use check-then-insert. Two independent transactions can
both see no row and race into the same primary key, violating the public idempotent contract.

### Required behavior

For both operations:

- preserve existing Vault Entry existence, trash-state, capability, and tag-existence checks;
- duplicate sequential calls remain idempotent;
- duplicate concurrent calls from independent transactions both complete successfully;
- exactly one database row remains;
- PostgreSQL uniqueness is the final race arbiter;
- do not catch/swallow unrelated integrity violations;
- do not use `REQUIRES_NEW` or otherwise split the caller's transaction solely to recover from the duplicate race.

Prefer an atomic PostgreSQL insert-if-absent approach consistent with the already-verified Phase 3 set-write pattern
(`INSERT ... ON CONFLICT ... DO NOTHING`) if it fits the existing repositories cleanly.

### Likely implementation targets

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/metadata/VaultMetadataService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/infrastructure/persistence/FavoriteRepository.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/infrastructure/persistence/VaultEntryTagRepository.java`
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/vault/VaultMetadataIntegrationTest.java`

Do not change the public `VaultMetadataOperations` contract unless Codex identifies a concrete requirement from the
existing frozen contract.

## PostgreSQL regression contract

The active maintenance handoff must require deterministic PostgreSQL/Testcontainers coverage for all three races.

### Refresh-token mixed race

At minimum, prove the rotation-wins interleaving deterministically:

1. start from one valid predecessor;
2. rotation acquires/holds the predecessor write lock and proceeds toward replacement;
3. concurrent revoke targets the same raw predecessor token;
4. both transactions complete according to the contract;
5. reload committed database state;
6. assert predecessor is revoked;
7. assert `replaced_by_token_id` still identifies the committed successor;
8. assert exactly one successor was created and no raw persistence exception leaked.

Also retain/extend coverage proving revocation-first causes rotation to fail closed with no successor.

### Favorite race

Two independent transactions call `favorite(entryId)` concurrently for the same eligible, non-trashed entry:

- both callers complete successfully;
- exactly one `favorites` row exists afterward;
- existing capability/trash validation remains effective.

### Tag-attachment race

Two independent transactions call `attachTag(entryId, tagId)` concurrently for the same eligible entry/tag pair:

- both callers complete successfully;
- exactly one `vault_entry_tags` row exists afterward;
- missing-tag, capability, and trash validation remain effective.

Use latches/barriers or another deterministic coordination technique. Do not rely on timing-only sleeps as the sole
proof of the race.

## Required final verification

After focused regressions pass, run:

```text
mvn -f backend/pom.xml clean verify
```

Retain:

- exact command and exit status;
- Java and Maven versions;
- PostgreSQL/Testcontainers version;
- total tests/failures/errors/skips;
- focused concurrency test results;
- Spring Modulith verification;
- Flyway/Hibernate validation.

Evidence file for this maintenance slice:

`docs/implementation/maintenance/milestone-1-3-concurrency/test-evidence.md`

## Non-goals

Do **not**:

- change DBML or Flyway V1;
- add/change database schema;
- implement Phase 4 Fiction;
- alter public API shapes without a demonstrated need;
- refactor unrelated Phase 1/2/3 code;
- change authentication token format/lifetimes/cryptography;
- change Vault capability matrix, rating behavior, tag creation semantics, or trash semantics;
- add generic concurrency abstractions, retry frameworks, events, or new custom agents/hooks for these two defects.

## Required engineering skills

Use the existing skills:

- `authentication-security`
- `jpa-postgresql-persistence`
- `backend-testing`
- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`

No new domain skill, custom agent, or repository hook is required.

## Workflow (completed)

1. Codex: `$codex-create-handoff` from this approved maintenance scope.
2. Antigravity: `/antigravity-implement-handoff`.
3. Codex: `$codex-final-review`.
4. Owner: commit/push only after `READY FOR OWNER COMMIT`.
5. Codex: rerun `$codex-milestone-review`.
6. Phase 4 remains blocked until milestone status is `MILESTONE_READY`.
