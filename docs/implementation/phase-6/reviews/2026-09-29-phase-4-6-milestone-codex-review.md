# Phase 4–6 milestone Codex review — 2026-09-29

Status: **CHANGES_REQUESTED**. Review window: frozen Phase 4 Fiction, Phase 5 Film, and Phase 6 Media + Location. Phase 7 pre-handoff review remains blocked.

## Blocking finding

1. **Medium — Hibernate logs private values from expected uniqueness conflicts.** The independent PostgreSQL Testcontainers run records `org.hibernate.orm.jdbc.error` warning lines with PostgreSQL `Detail: Key ...` values, including an Image `object_key` and `checksum_sha256` in `backend/target/surefire-reports/TEST-com.vhvkhangg.personalprivatevault.media.MediaIntegrationTest.xml` (lines 83–91). The same pattern logs Fiction/Film genre and Location category names, and earlier modules' names. `backend/src/main/resources/application.yml` (lines 44–47) leaves Hibernate's error logger at its default warning level, so the existing exception-to-domain translation does not prevent framework logging of user data. This conflicts with the repository's no-sensitive-payload-logging rule and the milestone's explicit object-key/privacy check. **Classification: owner-approved maintenance implementation slice**, because fixing logging behavior touches frozen backend configuration and possibly frozen conflict paths. Define a narrow privacy-safe diagnostic policy for expected constraint errors, preserve actionable non-sensitive failure telemetry, and add a regression check that a duplicate Image object key containing a distinctive private marker does not appear in captured application logs. Audit other expected uniqueness paths for the same leak. Do not change Flyway/schema or suppress all diagnostics without a justified replacement.

## Checks and non-blocking observations

- Independent `mvn -f backend/pom.xml -ntp clean verify` on Java 25 passed: 458 tests, 0 failures/errors/skips. The 12 Spring Modulith architecture tests, Flyway V1, and Hibernate validation passed against PostgreSQL Testcontainers. `git diff --check` passed.
- Source and the frozen dependency matrix show Fiction and Film using public People/Reference/Vault contracts, Media using Vault, and Location using Vault/Reference; no cross-feature internal import or module cycle was found. Vault-backed creation and rollback tests, set-assignment contention tests, and Phase 6 schedule locks remain green.
- Fiction and Film genre services have parallel, module-owned rules; this is not a reason to introduce a cross-domain generic base service. Parent-scoped Film credits/links and Fiction links are not paginated, but no concrete data-volume bottleneck was established for this single-user phase; revisit bounded HTTP presentation when that scope begins.
- Build diagnostics include Lombok's JDK `Unsafe` notice, the deprecated test-support API note, and expected test-induced constraint warnings. No IDE inspection was run, so this review does not claim the repository is warning-free. The constraint warnings are blocking only because their detail contains private values.
- Phase 7 preparation files are present and uncommitted; this review did not edit them or create a next implementation handoff. Frozen Phase 4–6 source/schema was not modified.

## Gate

The owner must approve a narrow maintenance scope before frozen-code changes. Give this review to ChatGPT to prepare that scope and its tests; after implementation, run the maintenance final-review workflow, commit/push, then rerun this milestone review. Do not advance Phase 7 pre-handoff review yet.
