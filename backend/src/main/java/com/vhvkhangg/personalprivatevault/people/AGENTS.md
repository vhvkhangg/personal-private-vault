# People Module Agent Instructions

Applies to `com.vhvkhangg.personalprivatevault.people`.

## Ownership

- `persons`
- `person_roles`
- `creator_groups`
- `creator_group_members`

## Allowed dependencies

The People module may depend only on these Spring Modulith named interfaces:

- `vault::entry`
- `vault::enums`
- `vault::view`
- `reference::catalog`
- `reference::view`

Never import `vault.internal.*` or `reference.internal.*`.

Do not widen these dependencies back to whole-module `vault` / `reference` unless an owner-approved architecture change requires it.

## Guidance

Use:

- `people-domain-modeling`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `reuse-and-consistency`
- `backend-testing`

## Invariants

- Person ID is the corresponding Vault Entry ID.
- Create shared identity through public `vault::entry` operations.
- Validate optional nationality through public `reference::catalog` operations.
- Person metadata remains owned by `vault`.
- Creator groups are not Vault Entries.
- Public APIs expose capabilities/views, never JPA entities or repositories.
- Exact duplicate creator-group names are conflicts.
- Re-adding the same person role is idempotent.
- Re-adding the same creator-group membership is idempotent.
- Sequential and concurrent duplicate operations must converge to the same documented outcome.
- PostgreSQL uniqueness constraints are the final race-condition arbiter.
- Raw persistence uniqueness exceptions must not leak through the public People API.
- Public reads are bounded to person-by-ID, group-by-ID, roles-for-person, and members-for-group.
- Person/group deletion is outside Phase 3 scope.
- Avoid `Service` / `ServiceImpl`, role subclasses, generic CRUD bases, or events without a concrete need.

## Phase gate

Phase 3 production changes require:

1. `docs/implementation/phase-3/preparation-review.md` = `READY FOR HANDOFF`;
2. an active approved Phase 3 Codex handoff.
