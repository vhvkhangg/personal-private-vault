# Phase 7 Account final Codex re-review — 2026-09-29

Status: **CHANGES_REQUESTED**. Handoff: `backend-phase-7-account`. Codex made no production-code changes.

## Prior findings

The relationship upsert now preserves creation `source` and leaves `updated_at` unchanged on an identical repeat. The unbounded public `findByIds` operation is gone. Snapshot creation validates target IDs with one bulk lookup, and the recent-snapshot read uses one grouped count query for its bounded headers. These correct the three production-code defects in the [initial review](2026-09-29-phase-7-final-codex-review.md).

## Remaining blocking finding

1. **Medium — snapshot query-shape regression is not tested.** `AccountIntegrationTest.followerSnapshotBulkExistenceValidationAndGroupedCountReads` (around line 669) verifies entry counts and missing-target rejection but observes no SQL/query count. The test would pass if `createSnapshot` resumed one existence lookup per target or `findRecentByOwner` resumed one count per header. The initial review specifically required regression evidence for this query shape, and the test evidence currently claims a single roundtrip without measuring it. Add a focused PostgreSQL-backed query-count or equivalent observable SQL-shape assertion for both paths (including multiple targets and headers); keep the existing functional/atomicity assertions. Correct the evidence wording if needed. No production-code change is requested by this finding.

## Verification and scope

- Independent `mvn -f backend/pom.xml -ntp clean verify` on Java 25: **499 tests, 0 failures, 0 errors, 0 skipped; BUILD SUCCESS**. PostgreSQL Testcontainers, Flyway/Hibernate validation, and 12 architecture tests passed.
- `git diff --check` passed. The existing Lombok/JDK `Unsafe` and deprecated test-support warnings remain non-blocking; no IDE inspection was performed.
- No frozen schema/module baseline change or scope expansion was found. Codex did not commit or push.

## Gate

Keep the active handoff at `CHANGES_REQUESTED` until Antigravity adds the query-shape regression assertion, updates test evidence, and reruns the handoff verification. This is test-only remediation suitable for `/antigravity-test-slice`.
