# Active Implementation Handoff

- Handoff ID: `backend-phase-3-people-foundation`
- Created by: Codex
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex

## Goal

Implement the Phase 3 `people` foundation: person profiles and roles, creator groups and memberships, with narrow public capabilities, PostgreSQL persistence, and verified Vault/Reference integration. Work only within the approved Phase 3 scope.

## Sources of truth

- `docs/implementation/phase-3/README.md` and `preparation-review.md`
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` and `backend/src/main/resources/db/migration/V1__create_schema_v1.sql`
- `docs/architecture/module-dependency-matrix.md` and `docs/architecture/module-boundaries.md`
- `docs/repository/repository-package-tree.md` and `docs/adr/0015-semantic-public-api-subpackages.md`
- Existing `vault/entry/VaultEntryOperations.java` and `reference/catalog/ReferenceCatalog.java`

## Required engineering skills

- `people-domain-modeling`
- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `backend-testing`

## Implementation targets

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/package-info.java`: narrow `allowedDependencies` to `vault::entry`, `vault::enums`, `vault::view`, `reference::catalog`, and `reference::view`. Remove whole-module allowances; use fewer only if public signatures prove they are unused.
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/`: provide named-interface public person/group capabilities, immutable views, and stable public enums or exceptions as needed. Keep entities, repositories, application logic, and persistence adapters under `internal/`. Add `package-info.java` for meaningful packages.
- Map and operate only the existing `persons`, `person_roles`, `creator_groups`, and `creator_group_members` tables. Use frozen PostgreSQL enum, foreign-key, key, uniqueness, check, and timestamp semantics. Do not alter DBML or Flyway V1.
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/people/`: add focused behavior and PostgreSQL integration tests. Extend `ApplicationArchitectureTests` only where needed to prove the exact module boundary.
- `docs/implementation/phase-3/test-evidence.md`: record final verification evidence.

## Required behavior / invariants

- Create/read/update person profiles. Person ID equals its `vault_entries.id`; create the entry with `VaultEntryOperations.create(VaultEntryType.PERSON)` and persist the person in one transaction. A failed person create leaves no orphan Vault Entry. Keep favorite, rating, tag, and recycle behavior in Vault.
- Respect Schema v1 optional fields. Require nonblank person names; height/weight must be positive when present. Validate optional nationality through public `ReferenceCatalog.country(code)`; use no Reference repository. Preserve database FK/check protection.
- Manage the five frozen roles (`ACTOR`, `SINGER`, `DIRECTOR`, `AUTHOR`, `ARTIST`). Re-adding an existing `(person_id, role)` succeeds idempotently, including simultaneous calls; exactly one row remains and no raw uniqueness error escapes.
- Create/read/update creator groups, which are not Vault Entries. Exact duplicate stored names conflict with a stable People-domain exception such as `CreatorGroupNameAlreadyExistsException`; concurrent creates have one winner and domain-conflict losers. Apply the same stable conflict when an update would violate name uniqueness. Do not introduce case-insensitive/normalized uniqueness.
- Add unique `(creator_group_id, person_id)` memberships idempotently, including simultaneous calls; exactly one row remains and no raw uniqueness error escapes. Validate referenced person/group existence through People-owned state.
- Expose bounded reads for person by ID, group by ID, roles for one person, and members for one group. Public results are immutable views; no entity or repository escapes. Do not add global list/search or deletion APIs.
- Let PostgreSQL constraints arbitrate duplicate races; pre-checks alone do not satisfy the contract. Use transaction handling that leaves no partial state or failed transaction leaking as a raw persistence exception.

## Acceptance criteria

- JPA validation succeeds against unchanged Flyway V1 on PostgreSQL for all four People-owned tables.
- Spring Modulith verification accepts only the five listed Vault/Reference named interfaces; no cross-module `internal` or repository access and no dependency cycle.
- Tests prove transactional shared identity, validation and missing-reference behavior, bounded public reads, exact group-name uniqueness, and sequential/concurrent role and membership idempotence.
- Existing backend tests remain green. No frozen architecture/schema or Phase 0–2 production behavior changes.

## Non-goals

- Database migration, seed data, REST controllers, OpenAPI operations, frontend, search, fiction/film/music/study implementation, or unrelated refactors.
- Person/group deletion, cascading or permanent-delete APIs, unbounded listing, case-insensitive group-name uniqueness, and new role hierarchies.

## Test/evidence contract

- Focused tests: validation/domain cases plus PostgreSQL Testcontainers integration for all four tables, country lookup, Vault/Person rollback, duplicate group creates, duplicate roles and memberships under sequential and concurrent calls, and `ApplicationArchitectureTests`.
- Final command: `mvn -f backend/pom.xml clean verify` on Java 25. Do not use H2.
- Required evidence file: `docs/implementation/phase-3/test-evidence.md` with the exact command/exit status, Java/Maven/PostgreSQL versions, test totals, Modulith/Flyway/JPA results, and any material warnings.

## Constraints / risks

- V1 has no country seed data; test nationality against an explicit test fixture and do not invent production seeding.
- Constraint violations can mark a PostgreSQL transaction failed. Prove race outcomes with real independent transactions rather than relying on a same-transaction catch of a uniqueness exception.
- Keep profile payloads and other private data out of logs. Avoid speculative abstractions or new cross-module dependencies.

## Implementation result

Implemented by Antigravity on 2026-09-28:

- **Module Descriptor:** Narrowed `allowedDependencies` in `people/package-info.java` to exactly the five required named interfaces: `vault :: entry`, `vault :: enums`, `vault :: view`, `reference :: catalog`, and `reference :: view`.
- **Public API Subpackages:**
  - `people.enums` (`@NamedInterface("enums")`): `Gender` and `PersonRole` mapping PostgreSQL native enums.
  - `people.view` (`@NamedInterface("view")`): immutable record views `PersonView`, `CreatorGroupView`, and `CreatorGroupMemberView`.
  - `people.person` (`@NamedInterface("person")`): `PersonOperations` contract, `CreatePersonCommand`, `UpdatePersonCommand`, `PersonNotFoundException`, and `InvalidPersonException`.
  - `people.group` (`@NamedInterface("group")`): `CreatorGroupOperations` contract, `CreateCreatorGroupCommand`, `UpdateCreatorGroupCommand`, `CreatorGroupNotFoundException`, `CreatorGroupNameAlreadyExistsException`, and `InvalidCreatorGroupException`.
- **Internal Domain & Persistence:**
  - `people.internal.domain`: `Person` (sharing ID with `vault_entries.id`), `PersonRoleId` & `PersonRoleAssignment`, `CreatorGroup`, `CreatorGroupMemberId` & `CreatorGroupMember`.
  - `people.internal.infrastructure.persistence`: `PersonRepository`, `PersonRoleRepository` (with native `ON CONFLICT DO NOTHING`), `CreatorGroupRepository`, and `CreatorGroupMemberRepository` (with native `ON CONFLICT DO NOTHING`). Removed obsolete `.gitkeep`.
  - `people.internal.application`: `PersonService` and `CreatorGroupService` operating directly within caller transactions (`@Transactional`), eliminating previous `REQUIRES_NEW` helpers (`PersonRoleManager`, `CreatorGroupManager`, `CreatorGroupMembershipManager`).
- **Invariants Verified:**
  - Shared identity: `persons.id == vault_entries.id`; creation is transactionally consistent with `VaultEntryOperations.create(VaultEntryType.PERSON)`; rollback test proves zero orphan entries on failure.
  - Enclosing transaction preservation: uncommitted parent rows created earlier in an outer transaction are immediately visible when adding roles or memberships without transaction isolation conflicts.
  - Outer transaction rollback: rolling back an enclosing transaction cleanly rolls back created groups, updated groups, added roles, and added memberships.
  - Immutable collections: `CreatorGroupOperations.getMembers` returns `List.copyOf(...)`; callers attempting mutation receive `UnsupportedOperationException`.
  - Bounded reads: person by ID, group by ID, roles by person ID, members by group ID.
  - Role idempotence: sequential and 8-thread concurrent additions converge cleanly to 1 row without leaking raw DB exceptions.
  - Creator group name uniqueness: exact duplicate creates throw domain conflict `CreatorGroupNameAlreadyExistsException` sequentially and under concurrent races.
  - Membership idempotence: sequential and 8-thread concurrent additions converge cleanly to 1 row without leaking raw DB exceptions.
  - Reference validation: nationality codes validated through public `ReferenceCatalog.country(code)`.
- **Verification Evidence:**
  - Command: `mvn -f backend/pom.xml clean verify` completed with exit status `0` (`BUILD SUCCESS`).
  - Total tests: 222 run, 0 failures, 0 errors, 0 skipped (73 Phase 3 tests).
  - Spring Modulith module verification and Hibernate `ddl-auto: validate` against Flyway V1 DDL passed cleanly.
  - Evidence document: `docs/implementation/phase-3/test-evidence.md`.

## Codex remediation

Codex final review on 2026-09-28: `CHANGES_REQUESTED`. See
`docs/implementation/phase-3/reviews/2026-09-28-phase-3-final-codex-review.md`.

1. [x] Preserve the caller's transaction across group create/update, role add, and membership add. Current
   `REQUIRES_NEW` helpers commit independently and cannot see uncommitted Person/Group rows from an enclosing
   transaction. Keep PostgreSQL-backed duplicate/race outcomes without swallowing raw constraint errors. Add
   PostgreSQL tests for rollback of each operation within an enclosing transaction and for adding a role/member
   to a newly created Person/Group before that transaction commits; retain the existing concurrency coverage.
2. [x] Return an immutable collection from `CreatorGroupOperations.getMembers` as its public contract promises,
   and test that callers cannot mutate the returned list.
3. [x] Rerun `mvn -f backend/pom.xml clean verify` on Java 25, update
   `docs/implementation/phase-3/test-evidence.md` with the exact result, and return this handoff as
   `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

## Final review

Codex remediation re-review on 2026-09-28: `READY_FOR_OWNER_COMMIT`.

- The two findings in the Phase 3 final review are resolved; the completed checklist above is verified against
  the People source and PostgreSQL integration tests.
- Codex independently ran `mvn -f backend/pom.xml clean verify`: exit 0, 222 tests, 0 failures/errors/skips.
- Spring Modulith verification and Flyway-backed JPA validation passed. No frozen schema or Phase 0–2 production
  source changed. `git diff --check` passed.
- Review record: `docs/implementation/phase-3/reviews/2026-09-28-phase-3-final-codex-review.md`.
