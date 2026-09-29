# Active Implementation Handoff

- Handoff ID: `milestone-4-6-privacy-safe-constraint-logging`
- Created by: Codex
- Status: `READY_FOR_OWNER_COMMIT`
- Implementer: Antigravity
- Final reviewer: Codex
- Scope: owner-approved maintenance of private-value logging on expected PostgreSQL constraint conflicts

## Goal

Prevent expected uniqueness conflicts from writing private business values (especially Image object keys and checksums) to application/test logs, while preserving existing domain results, actionable non-sensitive diagnostics, and PostgreSQL transaction/concurrency behavior. This is a narrow frozen-phase maintenance slice, not Phase 7 implementation.

## Sources of truth

- `docs/implementation/maintenance/milestone-4-6-privacy-safe-constraint-logging/README.md` — owner-approved scope, permitted targets, privacy policy, and full test contract.
- `docs/implementation/phase-6/reviews/2026-09-29-phase-4-6-milestone-codex-review.md` — blocking finding and evidence.
- `docs/implementation/phase-6/milestone-review.md` — `CHANGES_REQUESTED` milestone gate.
- Root `AGENTS.md` and frozen Schema v1/module-boundary references named by the approved scope.

## Implementation targets

- Evaluate `backend/src/main/resources/application.yml` for narrowly restricting `org.hibernate.orm.jdbc.error` output that includes raw PostgreSQL `Detail: Key ...` values. Preserve a useful non-sensitive failure signal; do not silence root/all Hibernate diagnostics.
- Add focused Spring Boot/PostgreSQL Testcontainers logging regressions under existing test support and Media integration tests, or a dedicated persistence-logging integration test if cleaner. Capture application logs across competing worker threads.
- Audit the existing expected uniqueness paths listed in the approved scope (Authentication bootstrap, Vault tags, People creator groups, Fiction/Film genres, Location categories, Image object key/checksum). Modify frozen service code only if needed for narrowly scoped sanitized diagnostics; do not refactor behavior.
- Record results in `docs/implementation/maintenance/milestone-4-6-privacy-safe-constraint-logging/test-evidence.md`.

## Required behavior / invariants

- Force actual PostgreSQL unique-index contention for Image object key and checksum with distinctive private markers. The existing `ImageConflictException`, losing Vault-entry rollback, and deterministic lock-observation behavior remain intact.
- Captured logs must contain neither marker nor raw `Detail: Key (object_key)=(` / `Detail: Key (checksum_sha256)=(` text. Also prove one name-based expected conflict does not log its private marker while preserving its domain result.
- For handled conflicts, diagnostics may include stable operation/event code, SQLState, known constraint name, or exception class, but never rejected values, raw vendor detail, raw SQL with values, or throwable/root-cause messages containing them. Unexpected persistence failures must remain observable and fail visibly.
- If service-level logging is introduced, keep it local and sanitized. Do not change public exception contracts unless captured logs prove an existing exception is itself emitted.

## Non-goals

No Phase 7 work; DBML/Flyway/schema/SQL changes; uniqueness or transaction redesign; public API/entity/repository changes; broad logging rewrite; root/all-Hibernate suppression; generic logging framework, AOP, hook, or custom agent; unrelated frozen-phase refactors.

## Test/evidence contract

- Focused regressions: real Spring Boot logging configuration plus PostgreSQL Testcontainers; object-key and checksum conflict races with captured worker-thread logs, rollback assertions, one additional name-based path, and safe diagnostic preservation. Test every modified conflict path if the solution changes paths individually.
- Final commands: `mvn -f backend/pom.xml clean verify` on Java 25, then `git diff --check`.
- Evidence: exact commands/exits, environment and PostgreSQL/Testcontainers version, test totals, focused names/results, effective Hibernate logger level, marker absence, Spring Modulith and Flyway/Hibernate validation, and known diagnostics in the maintenance `test-evidence.md`.

## Constraints / risks

- Expected SQL exceptions are logged by Hibernate before domain translation; changing only domain messages cannot fix that upstream log. A broad logger suppression without a proportionate safe diagnostic replacement does not meet the approved scope.
- Use `java-spring-coding-standards`, `jpa-postgresql-persistence`, `backend-testing`, `pragmatic-solid-design`, `reuse-and-consistency`, and `design-pattern-selection`; apply root privacy/logging rules.
- Preserve unrelated dirty-worktree Phase 7 preparation. Keep Phase 7 pre-handoff review blocked until maintenance final review, owner commit/push, Phase 4–6 milestone re-review to `MILESTONE_READY`, milestone-doc commit/push, and post-milestone synchronization.
- Agents do not commit, push, tag, or create/merge PRs.

## Implementation result

1. **Targeted Logging Configuration:**
   - In `backend/src/main/resources/application.yml`, configured `org.hibernate.orm.jdbc.error: ERROR` under `logging.level`.
   - Narrowly suppresses Hibernate's `SqlExceptionHelper` WARN logging that emitted raw PostgreSQL `Detail: Key (...) already exists.` containing private business values on expected unique constraint conflicts.
   - Preserves `root: INFO` and leaves all other Hibernate categories untouched.
2. **Dedicated Persistence-Logging Regression Suite:**
   - Created `com.vhvkhangg.personalprivatevault.support.PrivacySafeConstraintLoggingIntegrationTest` (5 tests):
     - `effectiveHibernateJdbcErrorLoggerLevelIsError`: asserts `org.hibernate.orm.jdbc.error` is `ERROR` and root is `INFO`.
     - `imageObjectKeyConflictDoesNotLogPrivateMarkerAcrossWorkerThreads`: forces real PostgreSQL lock contention on `images.object_key` with distinctive marker `PPV_PRIVATE_OBJECT_KEY_MARKER`, asserts `ImageConflictException`, verifies Vault entry rollback, and asserts absence of marker and `Detail: Key (object_key)=(`.
     - `imageChecksumConflictDoesNotLogPrivateChecksumAcrossWorkerThreads`: forces PostgreSQL lock contention on `images.checksum_sha256` with distinctive checksum `e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855`, asserts `ImageConflictException`, verifies Vault entry rollback, and asserts absence of checksum and `Detail: Key (checksum_sha256)=(`.
      - `locationCategoryNameConflictDoesNotLogPrivateCategoryName`: forces PostgreSQL lock contention on `location_categories.name` with marker `PPV_PRIVATE_CATEGORY_NAME_MARKER`, asserts `LocationCategoryNameAlreadyExistsException`, and asserts absence of marker and `Detail: Key`.
      - `unexpectedHibernatePersistenceFailureRemainsObservableAndPrivacySafe`: exercises an unexpected persistence failure through Hibernate/JPA via `EntityManager` violating foreign key `fk_images_id_vault_entries` with marker `PPV_UNEXPECTED_FAILURE_PRIVATE_MARKER`; asserts `ConstraintViolationException` (retaining constraint name `fk_images_id_vault_entries` and SQLState `23503`) propagates visibly and logs contain neither marker nor raw `Detail: Key (id)=(999999)`.
3. **Integration Test Suite Enhancements:**
   - Updated `MediaIntegrationTest` with `@ExtendWith(OutputCaptureExtension.class)` and distinctive markers/checksums in `concurrentDuplicateObjectKeyThrowsDomainConflict` and `concurrentDuplicateChecksumThrowsDomainConflict`, verifying that captured worker-thread logs do not contain private values or vendor detail lines.
   - Updated `LocationIntegrationTest` with `@ExtendWith(OutputCaptureExtension.class)` in `concurrentDuplicateLocationCategoryThrowsConflict`, verifying absence of `PPV_PRIVATE_CATEGORY_NAME_MARKER` and vendor detail lines.
4. **Audit Scope Verification:**
   - Audited expected uniqueness paths (Authentication bootstrap, Vault tags, People creator groups, Fiction genres, Film genres, Location categories, Media images). All cleanly catch and translate persistence errors without logging private values. Narrow `org.hibernate.orm.jdbc.error: ERROR` covers all of them without touching frozen service logic.
5. **Full Verification:**
   - `mvn -f backend/pom.xml clean verify` passed with 463 tests run, 0 failures, 0 errors, 0 skipped (`BUILD SUCCESS`).
   - `git diff --check` passed cleanly with 0 whitespace warnings/errors.
   - Refreshed Graphify AST with `scripts/refresh-graphify.ps1`.
   - Evidence recorded in `docs/implementation/maintenance/milestone-4-6-privacy-safe-constraint-logging/test-evidence.md`.

## Codex remediation

Test/evidence remediation complete and independently accepted in the [final re-review](../maintenance/milestone-4-6-privacy-safe-constraint-logging/reviews/2026-09-29-final-codex-rereview.md):
- Updated `PrivacySafeConstraintLoggingIntegrationTest` to replace the direct `JdbcTemplate` insert with `unexpectedHibernatePersistenceFailureRemainsObservableAndPrivacySafe`, exercising Hibernate/JPA via `EntityManager` under the configured `org.hibernate.orm.jdbc.error: ERROR` logger.
- The test violates foreign key `fk_images_id_vault_entries` using `PPV_UNEXPECTED_FAILURE_PRIVATE_MARKER`, confirms the failure remains observable (`ConstraintViolationException` with constraint name `fk_images_id_vault_entries` and SQLState `23503`), and asserts captured logs contain neither marker nor raw PostgreSQL vendor detail (`Detail: Key (id)=(999999)` / `Detail: Key `).
- Corrected test comments and `test-evidence.md`.
- Ran full test verification `mvn -f backend/pom.xml clean verify` (463 tests, 0 failures) and `git diff --check`.
- Ready for Codex re-review.

## Final review

`READY_FOR_OWNER_COMMIT` on 2026-09-29. Suggested commit message: `fix(backend): prevent private values in constraint logs`. Owner commits/pushes; agents do not. After commit/push, rerun `$codex-milestone-review` for Phases 4–6.
