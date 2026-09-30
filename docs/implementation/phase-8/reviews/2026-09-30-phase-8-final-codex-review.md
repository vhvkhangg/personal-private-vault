# Phase 8 Knowledge final Codex review — 2026-09-30

Status: **CHANGES_REQUESTED**. Handoff: `backend-phase-8-knowledge`. This is an implementation review; Codex made no production-code changes.

## Blocking findings

1. **Medium — Website domain accepts URL/path values as hostnames.** `StudyItemService.validateStudyItem` (around line 275) only trims, lowercases, and length-checks `siteDomain`. It accepts values such as `https://example.com/course` or `example.com/path` in the hostname-only field, contrary to the approved Phase 8 Website contract. Reject non-hostname values on create and update, retain locale-independent normalization, and test accepted hostnames plus scheme/path/invalid-host boundaries.
2. **Medium — Vocabulary ease can become zero after validation.** `VocabularyService` checks that the submitted ease is positive, then rounds it to two decimal places (around lines 177–194 and 336–339). For example, `0.001` passes validation but rounds to `0.00`, violating Schema v1's `ease_factor > 0` check. Validate the persisted/scaled value and `numeric(5,2)` range before writing for both item create/update and review transition; return the stable Vocabulary validation error and test these boundaries without a PostgreSQL constraint exception.
3. **Medium — required PostgreSQL contention and due-query behavior are not proved by tests.** `KnowledgeIntegrationTest` (around lines 360–411, 547–592, and 678–734) releases two workers together but does not observe a blocked PostgreSQL transaction or assert that the losing conflict reached the unique constraint. Its review test checks only that two rows exist and that the final repetition count is either value, not that the second history row's `previous_*` equals the first committed `new_*`. The due test (around lines 524–530) checks one scheduled item, omitting inclusive cutoff, null-time `NEW`, other null-time states, `MASTERED`, scheduled-first/tie ordering, and limit. Make contention observable with transaction/lock coordination or database lock evidence, assert the serial history chain and the full due predicate/order/boundary matrix, and capture both worker-thread output streams for private markers and raw vendor detail. Update the evidence to describe only what the tests actually prove.

## Checks and non-blocking observations

- Independent `mvn -f backend/pom.xml -ntp clean verify` on Java 25 passed: **543 tests, 0 failures, 0 errors, 0 skipped**. PostgreSQL Testcontainers, Flyway/Hibernate validation, and architecture verification passed. `git diff --check` passed.
- The code retains Vault-backed identity, nested-module ownership, a closed parent API, bounded collection reads, and database uniqueness/row locking. No frozen DBML/Flyway change was observed. The gaps above concern behavior at domain boundaries and insufficient proof of the concurrency contract despite a green suite.
- The compiler reported existing Lombok/JDK `Unsafe` and deprecated test-support warnings. No IDE inspection was run; this is not an IDE-warning-free claim.

## Gate

Antigravity should remediate these findings within the active Phase 8 handoff, update tests/evidence, and rerun the required verification. Do not commit/push or advance phase closeout while `CHANGES_REQUESTED` remains.
