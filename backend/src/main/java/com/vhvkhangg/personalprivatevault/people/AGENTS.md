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

## Phase 12 read-only search extension

An accepted Phase 12 handoff may modify this otherwise-frozen module **only** to add the read-only global-search
contract/query support defined by `docs/implementation/phase-12/README.md`.

Allowed:

- semantic public `search` named interface;
- owner-local search query/application/repository methods;
- search-only package descriptors/tests;
- Vault batch qualification/tag calls where this module already legally depends on Vault.

Not allowed:

- changing existing mutation/validation/lifecycle semantics;
- exposing entities/repositories/internals;
- importing the top-level `search` module;
- reading another module's repository/table directly;
- adding unbounded lists or per-hit cross-module calls.

The Phase 12 active handoff, when present, is the authority for this narrow exception to the original phase gate.

## Phase 13 HTTP adapter exception

An accepted active Phase 13 handoff may modify this otherwise-frozen module only to add the REST/JSON adapter work
authorized by `docs/implementation/phase-13/README.md`.

Allowed:

- owner `internal/web` controller/request/response DTO/mapper/advice packages;
- mapping to existing owner operations/facades;
- HTTP Bean Validation and OpenAPI annotations;
- module exception-to-HTTP translation;
- tests needed for this HTTP surface.

Not allowed:

- changing existing domain/application invariants or persistence behavior;
- importing another module's internal/repository/entity;
- exposing application entities or internal Search contracts;
- inventing new business operations solely for HTTP convenience;
- schema/Flyway changes;
- Phase 14+ work.

The active Phase 13 handoff, when present, is the temporary authority for this narrow adapter exception. Otherwise
the frozen-module rules remain in force.
