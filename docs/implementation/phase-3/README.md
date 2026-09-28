# Backend Phase 3 — People Foundation

Status: **PREPARED — READY FOR HANDOFF**

Phase 3 implements `people` because fiction, film, music, and study all need stable person/creator references.

No production implementation is authorized until:

1. `preparation-review.md` is `READY FOR HANDOFF`;
2. the preparation slice is committed/pushed;
3. `$codex-create-handoff` creates the Phase 3 handoff.

## Owned Schema v1 tables

- `persons`
- `person_roles`
- `creator_groups`
- `creator_group_members`

No migration/schema change is planned.

## Exact Spring Modulith dependencies

Phase 3 must narrow `people` to these exact named interfaces:

```java
@ApplicationModule(allowedDependencies = {
    "vault::entry",
    "vault::enums",
    "vault::view",
    "reference::catalog",
    "reference::view"
})
```

They are required for:

- `vault::entry` — `VaultEntryOperations`;
- `vault::enums` — `VaultEntryType.PERSON`;
- `vault::view` — immutable Vault Entry views used by the entry contract;
- `reference::catalog` — `ReferenceCatalog`;
- `reference::view` — immutable country/reference views returned by the catalog.

The Phase 3 implementation handoff must update the People module descriptor to this exact verified set. It may
become narrower only if the concrete public signatures prove one named interface is unused. It must not retain
whole-module `vault` / `reference` allowances. Internal packages remain forbidden.

Until an active Phase 3 implementation handoff exists, `people/package-info.java` intentionally remains at the committed Phase 2 baseline with whole-module `vault` / `reference` allowances. Preparation must not change production source.

## Planned capabilities

### Person

- create/read/update person profiles;
- use the Vault Entry ID as Person ID (`persons.id = vault_entries.id`);
- create shared identity through public `VaultEntryOperations` with type `PERSON`;
- validate optional nationality through public `ReferenceCatalog`;
- manage roles: ACTOR, SINGER, DIRECTOR, AUTHOR, ARTIST;
- expose immutable public views, not entities.

### Creator groups

- create/read/update creator groups;
- manage unique person memberships;
- expose stable group/member lookup views needed later by fiction/study;
- keep join-table repositories internal.

### Duplicate and concurrent semantics

These externally visible application semantics are fixed before handoff creation.

#### Creator-group name

- Schema v1 exact `creator_groups.name` uniqueness is authoritative.
- Creating another group with the same stored name is a **conflict**, not an idempotent create.
- Surface a stable People-domain conflict: `CreatorGroupNameAlreadyExistsException` unless an already-existing
  equivalent domain exception is discovered during handoff creation.
- Under concurrent duplicate creates, exactly one create succeeds; losing callers receive the same domain conflict.
- Do not leak raw JDBC/Hibernate uniqueness exceptions.
- Do not invent case-insensitive or normalized uniqueness beyond frozen Schema v1.

#### Person-role set

- Re-adding the same `(person_id, role)` is **idempotent**.
- Sequential and concurrent duplicate additions converge to exactly one row and the same successful final state.
- PostgreSQL uniqueness is the final race arbiter; a raw persistence uniqueness exception must not escape.

#### Creator-group membership set

- Re-adding the same `(creator_group_id, person_id)` is **idempotent**.
- Sequential and concurrent duplicate additions converge to exactly one row and the same successful final state.
- PostgreSQL uniqueness is the final race arbiter; a raw persistence uniqueness exception must not escape.

For all three uniqueness boundaries, correctness must not depend only on check-then-insert. Constraint-backed races
must produce the documented stable outcome with no partial state.

### Lookup and deletion boundary

Phase 3 public reads are intentionally bounded to:

- person by person ID;
- creator group by group ID;
- roles for one person ID;
- members for one creator-group ID.

Do not add an unbounded global `findAll`/list/search API in Phase 3. Pagination/search is future scoped work.

Person deletion, creator-group deletion, cascading delete APIs, and permanent-delete semantics are explicitly
**out of scope** for this foundation.

### Shared vault behavior

`PERSON` already supports favorite/rating/tags in the frozen vault capability matrix. Do not duplicate that logic
inside `people`.

Creator groups are not Vault Entries in Schema v1.

## Proposed public package direction

```text
people/
├── person/
│   └── PersonOperations.java
├── group/
│   └── CreatorGroupOperations.java
├── view/
│   └── *View.java
├── enums/
│   └── stable public people enums as needed
└── internal/
    ├── application/
    ├── domain/
    └── infrastructure/persistence/
```

Every meaningful package gets `package-info.java`. Do not introduce `Service` / `ServiceImpl` pairs by convention.

## Required invariants for the future handoff

- nonblank person/group names;
- optional height/weight > 0 when present;
- optional nationality resolves through public reference API;
- exact creator-group-name uniqueness: duplicate create = stable conflict;
- `(person_id, role)` uniqueness: duplicate add = idempotent success;
- `(creator_group_id, person_id)` uniqueness: duplicate add = idempotent success;
- the same outcomes under concurrent races, protected by PostgreSQL constraints;
- referenced people/groups exist;
- Person and Vault Entry creation are transactionally consistent;
- public APIs never expose JPA entities/repositories.

Do not invent mandatory gender/nationality/birth date or age restrictions absent from approved requirements.

## Logging

Routine reads do not need entry/exit logs. Log only meaningful failures/state changes with minimal metadata; do not
log private profile payloads wholesale.

## Testing contract

Future handoff should require:

- focused validation/domain tests;
- PostgreSQL Testcontainers tests for all four owned tables;
- Person/Vault Entry rollback/transaction consistency;
- creator-group-name, role, and membership uniqueness;
- PostgreSQL concurrency tests for competing group create, role add, and membership add;
- country validation through public reference contract;
- Spring Modulith architecture verification;
- final `mvn -f backend/pom.xml clean verify` evidence on Java 25.

Do not use H2.

## Out of scope

- fiction/film/music/study implementation;
- REST controllers;
- global search;
- frontend;
- migrations/schema changes;
- frozen vault/reference behavior changes.

## Preparation tooling

Added:

- `.agents/skills/people-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-3-people.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/AGENTS.md`

Reused:

- `backend-implementer`
- `architecture-auditor`
- repository safety hook

No new custom agent or hook is justified for Phase 3.
