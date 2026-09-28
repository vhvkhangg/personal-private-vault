---
name: people-domain-modeling
description: Guide People-module person shared-vault identity, roles, creator groups/memberships, reference validation, and public contracts without persistence leakage.
---

# People Domain Modeling

## Ownership

`people` owns `persons`, `person_roles`, `creator_groups`, and `creator_group_members`.

Its module descriptor must allow only:

```text
vault::entry
vault::enums
vault::view
reference::catalog
reference::view
```

Do not retain whole-module `vault` / `reference` allowances and never import their `internal` packages.

## Person identity

A Person is backed by a Vault Entry:

```text
vault_entries.id == persons.id
vault_entries.entry_type == PERSON
```

Create identity through public `VaultEntryOperations`. Never use vault repositories/entities directly.
Person persistence and Vault Entry creation must be transactionally consistent.

## Vault metadata

PERSON favorite/rating/tag/recycle-bin behavior remains owned by `vault`. Never duplicate it in People.

## Reference validation

Validate optional nationality through `ReferenceCatalog`, not reference repositories.

## Roles

Roles form a unique set by `(person_id, role)`. Duplicate add is idempotent, including under races: final state
is one row and callers do not receive a raw uniqueness exception. PostgreSQL uniqueness remains the final arbiter.

Do not create role subclasses/hierarchies without real role-specific behavior.

## Creator groups

Creator groups are not Vault Entries.

- Exact duplicate creator-group name create is a conflict. Concurrent duplicates have one winner; losers receive
  `CreatorGroupNameAlreadyExistsException` (or an already-existing equivalent discovered by the handoff).
- Membership duplicate add is idempotent, including under races; final state is one row with no raw uniqueness error.

Keep membership repositories internal and expose public views/capabilities.

## Lookup and deletion boundary

Public reads are bounded to person-by-ID, group-by-ID, roles-for-person, and members-for-group.
No unbounded global list/search API is part of Phase 3. Person/group deletion is out of scope.

## API

Prefer semantic capabilities such as `PersonOperations` / `CreatorGroupOperations` and immutable views.
Do not expose entities/repositories or add `Service` / `ServiceImpl` pairs merely for layering convention.

## Validation

Respect approved optionality. Do not make gender, birth date, nationality, height, or weight mandatory when Schema v1
allows null.

## Testing

Use PostgreSQL Testcontainers and cover Person/Vault Entry transaction consistency, exact group-name uniqueness,
concurrent duplicate group creates, sequential/concurrent idempotent role adds, sequential/concurrent idempotent
membership adds, missing references, country validation, and exact named-interface Modulith boundaries.
