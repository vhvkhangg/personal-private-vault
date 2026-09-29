# Active Implementation Handoff

- Handoff ID: `pre-phase4-code-hygiene`
- Created by: Codex
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Scope type: owner-approved maintenance of frozen Phase 1 Vault and Phase 3 People; not Phase 4

## Goal

Reorganize the public People person/group commands and exceptions without changing their logical named
interfaces or behavior; resolve the approved Vault constructor-visibility and genuine duplication findings;
leave verified PostgreSQL native-SQL IDE false positives untouched.

## Sources of truth

- `docs/implementation/maintenance/pre-phase4-code-hygiene/README.md` — owner-approved exact scope,
  permitted moves, inspection findings, non-goals, and regression contract.
- `docs/architecture/module-dependency-matrix.md`, `docs/architecture/module-boundaries.md`, and
  `docs/repository/repository-package-tree.md` — frozen ownership and package boundaries.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` and
  `backend/src/main/resources/db/migration/V1__create_schema_v1.sql` — unchanged schema/native SQL baseline.
- `.agents/rules/package-organization.md` and the completed Phase 1/3 handoffs/evidence — package and
  behavior baselines.

## Implementation targets

- Under `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/`, keep `PersonOperations` in
  `person/` and `CreatorGroupOperations` in `group/`; move only the approved command records and exception
  classes into `person/command/`, `person/exception/`, `group/command/`, and `group/exception/`. Add each new
  `package-info.java`. Update affected imports, tests, and Javadocs atomically.
- Preserve exactly the logical `people::person` and `people::group` named interfaces across their new child
  packages. Extend `ApplicationArchitectureTests` to prove both contracts remain exposed to allowed callers,
  no extra dependency name is required, and no People internal package is exposed.
- In `vault/internal/application/metadata/`, clean up `VaultMetadataService` constructors so no public
  signature exposes package-private `TagCreator`; preserve Spring injection, test-injectable `Clock`, and
  `TagCreator`'s proxied `REQUIRES_NEW` duplicate-tag recovery. Remove only genuine local duplication.
- In `people/internal/application/PersonService.java`, consolidate the repeated create/update profile
  validation/normalization sequence with one small private helper if it improves clarity. Preserve every
  input, exception, normalization, and transaction outcome.
- Verify the named native-SQL table/column/enum identifiers in the approved scope against Flyway V1 and
  PostgreSQL tests. Do not edit valid SQL or add IDE suppression just to silence unresolved-schema hints.

## Required behavior / acceptance criteria

- Existing People public operations, command fields, exceptions, and persistence behavior remain semantically
  unchanged after the package moves; all callers compile. `modules.verify()` and explicit named-interface
  assertions pass with `people::person` / `people::group` intact and no new People exposure.
- Vault tag creation retains isolated transaction recovery for case-insensitive uniqueness races. Favorite,
  rating, tag attachment, capability, trash, and timestamp behavior remain unchanged.
- Person create/update validation and normalization, Vault shared identity, nationality lookup, and rollback
  semantics remain unchanged. No blanket or IDE-only suppression is introduced.
- If Spring Modulith 2.1.1 cannot preserve the two logical named interfaces with the approved physical split,
  stop and report the architecture conflict; do not weaken the boundary or invent extra interfaces.

## Non-goals

No Phase 4 Fiction production work, DBML/Flyway/schema change, new business rule, public API redesign beyond
the approved package moves, generic validator/base-service/exception hierarchy, SQL rewrite for IDE hints,
new custom agent/hook, or unrelated refactor.

## Test/evidence contract

- Focused tests: `ApplicationArchitectureTests`; People validation/integration tests for person and group;
  `VaultMetadataIntegrationTest`, including tag-concurrency and constructor/clock behavior; PostgreSQL tests
  verifying the native SQL identifiers. Use Testcontainers PostgreSQL, not H2.
- Final commands: `mvn -f backend/pom.xml clean verify` on Java 25, then `git diff --check`.
- Record exact commands/exits, environment, totals, focused results, Modulith verification, and
  Flyway/Hibernate validation in
  `docs/implementation/maintenance/pre-phase4-code-hygiene/test-evidence.md`.

## Constraints / risks

- Preserve all pre-existing owner preparation in the dirty worktree. Production edits are limited to the
  approved People/Vault targets and their direct callers/tests; do not alter frozen architecture or schema.
- `TagCreator` is a separate Spring bean to make `REQUIRES_NEW` effective. Constructor cleanup must not
  replace its proxied call with a self-invocation or accidentally drop test clock injection.
- Relevant engineering skills: `java-spring-coding-standards`, `pragmatic-solid-design`,
  `reuse-and-consistency`, `design-pattern-selection`, `modular-monolith-architecture`,
  `jpa-postgresql-persistence`, and `backend-testing`.
- Phase 4 `$codex-pre-handoff-review` stays blocked until this maintenance passes final review and the
  owner commits/pushes it. This handoff does not authorize a Phase 4 implementation handoff.

## Implementation result

Completed all implementation targets in strict compliance with the approved scope:
1. **People Public API Organization:**
   - Moved commands and exceptions into child packages `people.person.command`, `people.person.exception`, `people.group.command`, and `people.group.exception`.
   - Created `package-info.java` in all 4 new packages declaring `@org.springframework.modulith.NamedInterface("person")` and `@org.springframework.modulith.NamedInterface("group")` respectively.
   - Updated imports and Javadoc across `PersonOperations`, `CreatorGroupOperations`, `PersonService`, `CreatorGroupService`, and all tests.
   - Enhanced `ApplicationArchitectureTests` to verify that `people::person` and `people::group` logical named interfaces remain intact without creating extra named interfaces, and that no internal packages are exposed.
2. **`VaultMetadataService` / `TagCreator` Constructor & Duplication Cleanup:**
   - Made `@Autowired` constructor package-private to avoid exposing package-private `TagCreator` in public API.
   - Removed unused public overload `VaultMetadataService(..., TagCreator tagCreator)`.
   - Chained default constructor to test-accessible clock constructor.
   - Preserved `TagCreator` as a separate Spring component with `REQUIRES_NEW` propagation for concurrent tag insertion conflict recovery.
3. **`PersonService` Profile Normalization Consolidation:**
   - Consolidated the identical 6-line create/update profile validation and normalization sequence into a private `ValidatedProfile` record and `validateProfile(...)` helper.
   - Preserved all inputs, exceptions, validation ordering, normalization, and transaction semantics.
4. **PostgreSQL Native SQL Verification:**
   - Verified native SQL queries and identifiers in `VaultEntryTagRepository`, `FavoriteRepository`, `PersonRoleRepository`, and `CreatorGroupMemberRepository` against Flyway `V1__create_schema_v1.sql`. Confirmed they are valid PostgreSQL identifiers and IDE unresolved-schema false positives; left SQL untouched without suppression.
5. **Verification Evidence:**
   - `mvn -f backend/pom.xml clean verify` passed with 0 failures across 228 tests (build time: 40.402 s).
   - `git diff --check` passed cleanly.
   - Detailed test evidence recorded in `docs/implementation/maintenance/pre-phase4-code-hygiene/test-evidence.md`.

## Codex remediation

Codex fills this only when status becomes `CHANGES_REQUESTED`.

## Final review

Codex final review on 2026-09-29: **READY FOR OWNER COMMIT**. No blocking findings. The moved People
types preserve their prior definitions apart from package declarations; named-interface and PostgreSQL-backed
tests pass. Independent `mvn -f backend/pom.xml clean verify` passed (228 tests, zero failures/errors/skips),
and `git diff --check` passed. See
`docs/implementation/maintenance/pre-phase4-code-hygiene/reviews/2026-09-29-final-codex-review.md`.

Suggested commit message: `refactor(people): organize public APIs and tidy validation`
