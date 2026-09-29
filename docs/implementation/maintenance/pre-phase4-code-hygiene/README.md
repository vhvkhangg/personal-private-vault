# Pre-Phase-4 Code Hygiene and People API Organization

Status: **READY FOR OWNER COMMIT**

Owner approval: **2026-09-29**

Phase 4 pre-handoff review is intentionally paused for this narrow maintenance slice. The slice may touch frozen
Phase 1 Vault and Phase 3 People code only for the exact structural/inspection findings below. It must not implement
Fiction or alter frozen database behavior.

## Goal

1. Make the public People `person` / `group` API packages easier to scan without changing their logical Spring
   Modulith named interfaces or behavior.
2. Resolve the actionable Java inspection findings reported before Phase 4.
3. Preserve warnings that are verified IDE schema-resolution false positives instead of suppressing or distorting
   correct PostgreSQL native SQL.

## A. People public API package organization

Current `people/person` and `people/group` mix operations interfaces, command records, and exceptions in one folder.
Reorganize them as:

```text
people/
├── person/
│   ├── PersonOperations.java
│   ├── package-info.java
│   ├── command/
│   │   ├── CreatePersonCommand.java
│   │   ├── UpdatePersonCommand.java
│   │   └── package-info.java
│   └── exception/
│       ├── InvalidPersonException.java
│       ├── PersonNotFoundException.java
│       └── package-info.java
└── group/
    ├── CreatorGroupOperations.java
    ├── package-info.java
    ├── command/
    │   ├── CreateCreatorGroupCommand.java
    │   ├── UpdateCreatorGroupCommand.java
    │   └── package-info.java
    └── exception/
        ├── CreatorGroupNameAlreadyExistsException.java
        ├── CreatorGroupNotFoundException.java
        ├── InvalidCreatorGroupException.java
        └── package-info.java
```

Requirements:

- keep the logical named-interface names **`people::person`** and **`people::group`** unchanged;
- the new `command` / `exception` packages must remain members of their parent logical named interface, using
  Spring Modulith 2.1.1 named-interface declarations appropriate for the package split;
- add `package-info.java` to every new package;
- update imports/tests/Javadocs atomically;
- do not create new externally required named-interface dependency names merely because files were reorganized;
- `ApplicationArchitectureTests` must prove callers can still depend on `people::person` / `people::group` and that
  no People internal package becomes exposed;
- do not change method signatures, validation rules, transaction semantics, exception meanings, or database logic.

If Spring Modulith verification shows the proposed physical split cannot safely preserve the two logical named
interfaces, stop and report the architecture conflict rather than weakening module boundaries.

## B. Actionable Java inspection findings

### `VaultMetadataService` / `TagCreator`

The reported `Class 'TagCreator' is exposed outside its defined visibility scope` warning is actionable:
`VaultMetadataService` is public while public constructors currently accept package-private `TagCreator`.

Fix with the narrowest visibility/constructor cleanup that preserves Spring injection and existing tests. Prefer
keeping `TagCreator` internal/package-private rather than making an internal helper public solely to silence the IDE.
Remove unused/redundant constructor overloads where safe and avoid introducing a factory/abstraction for this.

Also inspect the reported duplicated 13-line fragments in `VaultMetadataService`; remove only genuine duplication
when a small private helper or constructor simplification improves clarity. Do not refactor unrelated Vault behavior.

### `PersonService`

Inspect the two reported duplicated 6-line validation/normalization fragments in create/update. If they are the
shared profile normalization sequence, extract one small private validation/normalization helper (a private record is
acceptable if it materially clarifies data flow). Do not add a generic validator framework or change validation
semantics.

## C. Verified IDE SQL-resolution false positives — do not change production SQL just to silence them

The following IntelliJ inspections are **not accepted as runtime/compiler defects by themselves**:

- `VaultEntryTagRepository`: unresolved `vault_entry_id`, `tag_id`, `created_at`;
- `FavoriteRepository`: unresolved `vault_entry_id`, `created_at`;
- `PersonRoleRepository`: unresolved PostgreSQL enum type `person_role` and `person_id` / `role` columns;
- `CreatorGroupMemberRepository`: unresolved `creator_group_id` / `person_id` columns.

These are native PostgreSQL SQL strings and the IDE cannot resolve them without an attached/configured database
schema/datasource. Their tables, columns, enum, and queries are backed by Flyway/PostgreSQL integration evidence,
including the current 227-test baseline. Do not add `SqlResolve` suppression, rewrite correct SQL into less clear
forms, or commit IDE datasource configuration merely to remove these inspections.

The implementation agent must still verify the exact identifiers against Flyway V1 and focused PostgreSQL tests. If
that verification reveals a real mismatch, report it before changing frozen schema behavior.

## D. Regression and inspection contract

The handoff must require:

- all moved People public API tests compile and remain behaviorally unchanged;
- `ApplicationArchitectureTests` verifies the reorganized `people::person` / `people::group` exposure;
- focused Vault metadata tests remain green after constructor/duplication cleanup;
- focused People validation/integration tests remain green after `PersonService` cleanup;
- no new warning is hidden with blanket `@SuppressWarnings` or IDE-specific suppression without a written reason;
- final `mvn -f backend/pom.xml clean verify` passes on Java 25/PostgreSQL Testcontainers;
- `git diff --check` passes;
- final evidence records exact command/result/test totals.

Evidence file:

`docs/implementation/maintenance/pre-phase4-code-hygiene/test-evidence.md`

## Non-goals

- no Phase 4 Fiction production implementation;
- no DBML/Flyway/schema changes;
- no Vault/People business behavior changes;
- no changes solely to silence valid unresolved-SQL IDE hints caused by a missing local datasource;
- no public API redesign beyond the approved package moves;
- no generic base-service/validator/exception hierarchy;
- no new custom agent or repository hook.

## Workflow

1. Codex: `$codex-create-handoff` from this approved maintenance scope.
2. Antigravity: `/antigravity-implement-handoff`.
3. Codex: `$codex-final-review`.
4. Owner: commit/push after `READY FOR OWNER COMMIT`.
5. Resume Phase 4 with `$codex-pre-handoff-review`.
