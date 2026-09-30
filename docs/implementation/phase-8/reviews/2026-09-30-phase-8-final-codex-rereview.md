# Phase 8 Knowledge final Codex re-review — 2026-09-30

Status: **READY FOR OWNER COMMIT**. Handoff: `backend-phase-8-knowledge`. Codex made no production-code changes.

## Prior findings

1. **Website hostname validation — closed.** `StudyItemService` now validates the normalized `site_domain` as a hostname on create/update. Unit and PostgreSQL integration tests cover valid hostnames and reject schemes, paths, ports, and invalid labels.
2. **Vocabulary ease scaling — closed.** `VocabularyService` validates the two-decimal persisted value against the positive `numeric(5,2)` range before item writes and review transitions. Boundary tests cover rounding to zero and overflow as domain validation errors.
3. **Contention and due-query evidence — closed.** PostgreSQL integration tests now observe competing ungranted locks for both unique-conflict paths and the Vocabulary row lock, assert the committed review-history state chain, and cover the due predicate, inclusive cutoff, ordering, and limit. Captured output includes worker-thread stdout/stderr and is checked for private markers and raw vendor detail.

## Independent verification and review

- `mvn -f backend/pom.xml -ntp clean verify` passed on Java 25: **547 tests, 0 failures, 0 errors, 0 skipped**. PostgreSQL Testcontainers, Flyway/Hibernate schema validation, and Spring Modulith architecture checks passed.
- `git diff --check` passed. Generated Surefire reports contained neither the private test markers nor `Detail: Key`.
- Reviewed the Knowledge facade and nested-module ownership, Vault-backed transactions, race translation, bounded reads, code/package hygiene, scope, and status/evidence. No frozen schema or migration change was observed; no new blocking architecture, security, maintainability, or performance finding remains.
- Existing Lombok/JDK `Unsafe` and deprecated test-support compiler warnings remain non-blocking. No IDE inspection was run, so this review does not claim an IDE-warning-free state.

## Gate

**READY FOR OWNER COMMIT.** Suggested commit message: `feat(knowledge): establish phase 8 knowledge foundation`

Codex did not commit or push. After the owner commits/pushes, hand the latest package to ChatGPT for Phase 8 closeout and Phase 9 preparation.
