# Codex Final Review — Backend Phase 4 Fiction

- Date: 2026-09-29
- Handoff: `backend-phase-4-fiction`
- Initial result: **CHANGES_REQUESTED**; final result: **READY_FOR_OWNER_COMMIT**
- Mode: review-only; Codex changed review/status documentation, not production code or tests

## Blocking findings

### Medium — Unbounded public genre list exceeds the approved bounded-read scope

`FictionGenreOperations.findAll()`, `FictionGenreService.findAll()`, and
`FictionGenreRepository.findAllByOrderByNameAsc()` expose/load every Fiction genre with no bound. The Phase 4
scope explicitly prohibits global unbounded list/search APIs; the active handoff authorized genre
create/update/find, not a global list. This also adds a public contract that future consumers may depend on
before the read strategy is approved.

Required correction: remove this path and its `findAllOrderedByName` test. In `FictionLinkIntegrationTest`, retain
the ID of the genre created by the fixture instead of retrieving it through `findAll().get(0)`. Do not introduce
pagination or another global list as a replacement without owner approval.

### Medium — Test evidence names columns absent from frozen Schema v1

`docs/implementation/phase-4/test-evidence.md` lists `cover_image_url`, `is_adult`, `original_work_id`,
`published_at`, `notes`, `created_at`, and `updated_at` as `fictions` columns, and `title`/`updated_at` as
`fiction_links` columns. Flyway V1 actually defines `poster_url`, `is_nsfw`, `description`,
`current_progress_text`, and `review` on `fictions`, and `label` (with no `updated_at`) on `fiction_links`.
The report's claim that those listed columns were validated is therefore false and obscures the frozen-schema
contract, even though the JPA integration tests pass.

Required correction: replace both inventories with the exact Flyway V1 columns or omit the inventories and
reference the migration path. Audit adjacent evidence claims for similar overstatement. Do not change the
frozen migration, DBML, or correct entity mappings merely to match the report.

### Medium — Start-latch tests do not prove database duplicate contention

`FictionGenreIntegrationTest.concurrentDuplicateGenreCreatesHaveOneWinner` and
`FictionIntegrationTest.storyArchetypeAssignmentIdempotencyAndConcurrency` /
`worldSettingAssignmentIdempotencyAndConcurrency` release multiple threads together, but never observe that a
second transaction reaches a conflicting PostgreSQL insert while the first is pending. All calls can complete
sequentially; the tests still pass without exercising the database race and transaction-recovery claims in the
handoff/evidence. The `readyLatch.await(...)` result is also not asserted.

Required correction: use deterministic database coordination (or an equivalently observable conflict-path
mechanism) for genre creation and both classification tables. Verify no raw persistence error escapes and the
committed database has exactly one target row; describe only the race actually established by the tests.

## Verified areas

- Codex independently ran `mvn -f backend/pom.xml clean verify` on Java 25 with Testcontainers PostgreSQL:
  exit 0, `BUILD SUCCESS`, 298 tests, 0 failures/errors/skips. Spring Modulith verification and Flyway-backed
  Hibernate validation passed. `git diff --check` passed.
- Fiction uses public Vault, People, and Reference contracts; named dependencies are narrowed and module tests
  pass. No DBML/Flyway migration or frozen-module source was changed. Vault identity, author XOR, chapter bound,
  parent-scoped link operations, and atomic classification inserts have relevant integration coverage.
- Existing Lombok/JDK deprecation, Mockito dynamic-agent, and SpringDoc warnings remain toolchain/runtime
  diagnostics; this review did not run an IDE inspection and does not claim warning-free status.

The owner should not commit yet. Antigravity remediates only these findings and returns the handoff for Codex
re-review. No commit message is provided while blockers remain.

## Remediation re-review — 2026-09-29

- **Unbounded genre list resolved:** `findAll()` was removed from the public genre contract, service, repository,
  and test. The link fixture retains its created genre ID; no replacement global list was introduced.
- **Schema evidence resolved:** the `fictions` and `fiction_links` column inventories now match Flyway V1.
- **Duplicate-contention coverage remains open (Medium):** the new tests hold the first insert uncommitted,
  but each then sleeps 200 ms before releasing it. None observes that the second transaction reached the
  conflicting PostgreSQL insert or is waiting on an ungranted lock. A delayed second thread can begin only after
  the first commits: the genre service can then reject via its precheck, and the classification inserts can
  succeed sequentially through `ON CONFLICT DO NOTHING`. Thus the tests still pass without exercising the
  promised database conflict path. The updated evidence overstates this as deterministic contention.

Required test-only correction: before releasing the holder transaction, poll an observable PostgreSQL wait for
the competing query (for example the established `RefreshTokenLifecycleIntegrationTest.awaitCompetingLock`
pattern, adapted to each Fiction table) or use an equivalent deterministic mechanism. Assert the wait occurred,
then assert the domain/no-raw-error result and the single committed row. Remove timing-only sleeps and align
`test-evidence.md` with what is actually proven.

Codex independently reran `mvn -f backend/pom.xml clean verify`: exit 0, 297 tests, 0 failures/errors/skips;
`git diff --check` passed. Green tests do not close the remaining coverage gap. Final disposition remains
**CHANGES_REQUESTED**; no owner commit message is provided.

## Final remediation re-review — 2026-09-29

The remaining Medium finding is resolved. The genre-name and both classification duplicate-race tests now
hold the first insert uncommitted, poll PostgreSQL `pg_locks` joined to `pg_stat_activity` for an ungranted
competing lock associated with the target table, and release the holder only after observing that wait. They
then assert the public domain/no-raw-error outcome and exactly one committed target row. The 200 ms
timing-only sleeps were removed, and `test-evidence.md` describes the observable contention. The prior
unbounded-list and incorrect-schema-evidence findings remain resolved. No production code was changed in
this remediation.

Codex independently ran `mvn -ntp clean verify` from `backend/` on Java 25 with Testcontainers PostgreSQL:
exit 0, `BUILD SUCCESS`, 297 tests, 0 failures/errors/skips. Flyway/Hibernate validation and Spring Modulith
verification passed. `git diff --check` passed. Existing toolchain/runtime warnings remain; no IDE inspection
was run. No blocking findings remain.

Final disposition: **READY_FOR_OWNER_COMMIT**. The owner may commit/push the Phase 4 implementation and
review documents with this one suggested Conventional Commit message:

`feat(fiction): implement fiction foundation`
