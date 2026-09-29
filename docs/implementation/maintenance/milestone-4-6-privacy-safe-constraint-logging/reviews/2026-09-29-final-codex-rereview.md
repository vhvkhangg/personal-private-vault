# Privacy-safe constraint-logging maintenance final re-review — 2026-09-29

Status: **READY FOR OWNER COMMIT**. Handoff: `milestone-4-6-privacy-safe-constraint-logging`.

The sole blocking finding in the [initial final review](2026-09-29-final-codex-review.md) is closed. `PrivacySafeConstraintLoggingIntegrationTest.unexpectedHibernatePersistenceFailureRemainsObservableAndPrivacySafe` now executes an invalid Image insert through `EntityManager`, not `JdbcTemplate`. It asserts Hibernate's `ConstraintViolationException`, the actual `fk_images_id_vault_entries` constraint, SQLState `23503`, and absence of a private marker and PostgreSQL vendor detail in captured output. The test comment and `test-evidence.md` now describe the foreign-key violation accurately; neither claims `size_bytes` is NOT NULL.

Independent verification: `mvn -f backend/pom.xml -ntp clean verify` on Java 25 succeeded with **463 tests, 0 failures, 0 errors, 0 skipped**. The five-test privacy suite, 12 Spring Modulith architecture tests, Flyway V1 migration, and Hibernate validation passed with PostgreSQL Testcontainers. Captured Surefire output contains no private markers or `Detail: Key` lines. `git diff --check` passed. The narrow `org.hibernate.orm.jdbc.error: ERROR` setting and prior conflict/rollback behavior remain intact. Build notices about Lombok/JDK internals and deprecated test support are non-blocking; no IDE inspection was run.

No blocking findings remain. Codex made review/status documentation changes only. The owner must commit/push the maintenance before rerunning the Phase 4–6 milestone review; Phase 7 remains blocked.

Suggested commit message: `fix(backend): prevent private values in constraint logs`
