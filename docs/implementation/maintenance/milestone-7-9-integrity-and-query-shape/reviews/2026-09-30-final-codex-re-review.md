# Codex final acceptance re-review

Date: 2026-09-30

Result: **CHANGES_REQUESTED**

Scope: `maintenance-milestone-7-9-integrity-and-query-shape` only. No production edits or publishing.

## Remaining blocker

### 1. High — numeric whitelist still retains mutable subclasses

`knowledge/note/note/NoteFrontmatterSnapshot.java`, `validateAndNormalizeNumber`, accepts
`num instanceof BigDecimal` and `num instanceof BigInteger` and returns the original object.
Those classes are not final. A mutable subclass therefore bypasses the stated immutable-type whitelist.
This is the same snapshot-aliasing defect as the prior High finding, not a new feature requirement.

A synthetic compiled-class diagnostic used a `BigDecimal` subclass with mutable `intValue()`/`toString()` behavior.
After constructing `CreateKnowledgeNoteCommand` and `NoteFrontmatterSnapshot.deepCopy`, changing the original
changed both snapshots from 1 to 2; the persistence copy retained exactly the original object identity.
This proves the Java snapshot contract is still violated; no PostgreSQL commit replay is claimed by this diagnostic.

Required correction: accept exact known immutable numeric classes (not extensible subclasses), or normalize to
trusted immutable representations without losing precision. Reject unsupported subclasses with the existing stable,
payload-free `InvalidNoteException`. Cover mutable subclasses of both BigDecimal and BigInteger, parent/nested
create/update commands and snapshots. If rejected, prove stable no-write rejection; if normalized, prove enclosing-
transaction isolation through commit/fresh PostgreSQL reload. Preserve ordinary BigDecimal/BigInteger fidelity.
Rerun focused and final checks and update actual evidence. Do not broaden the maintenance.

## Resolved / accepted

- AtomicInteger/AtomicLong normalization removes the reproduced aliases for those representations.
- Canonical policy now resides in the legal Note named interface; command/view records delegate to it.
  Cycles, null/non-string keys and unsupported ordinary leaves fail with stable Note validation.
- Vocabulary stale-context regression now uses the parent Knowledge API throughout, with B's commit explicitly
  awaited before A's transition; existing contention/rollback coverage remains.
- Snapshot known-new persistence and sizes 1/3/5 query-shape regressions remain green.
- Revised evidence table matches actual executed `<testcase>` counts, including nested suites. Clarification of
  the prior review: some Surefire `testsuite.tests` attributes undercount nested execution; the earlier requested
  counts derived from those attributes are superseded by testcase-based verification. The current focused total
  70 and full total 593 are correct. No further suite-count remediation is required.
- The active implementation summary still described the obsolete internal helper/Jackson validation; this review
  synchronized that documentation to the actual owner-local copier. No production change was made.

## Independent verification

- `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, 593 tests, failures/errors/skips 0;
  elapsed 01:01 min, finished 2026-09-30 15:32:48 +07:00.
- Executed testcase aggregation agrees with the supplied suite table. Architecture/schema checks remain green;
  no Flyway/DBML or public signature changes are present in this slice.
- `git diff --check`: exit 0; repeated after review documentation updates.
- Observed diagnostics: Lombok `sun.misc.Unsafe::objectFieldOffset` deprecation and deprecated API notice in
  `AbstractPostgresIntegrationTest`; no IDE inspection or warning-free claim. The precise deprecated test API was
  not independently isolated, so the evidence's attribution is not verified by this review.
- Diagnostic: ignored `backend/target/final-review-diagnostics/NumericSubclassDiagnostic.java`; synthetic data only,
  not a tracked artifact or accepted regression.

Milestone stays `CHANGES_REQUESTED`; Phase 10 remains blocked. No commit message while the High blocker remains.

## Next gate

Antigravity `/antigravity-implement-handoff`, then Codex `$codex-final-review`.
