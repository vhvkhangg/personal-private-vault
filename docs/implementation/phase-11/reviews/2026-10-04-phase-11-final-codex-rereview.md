# Phase 11 Codex final re-review

- Date: 2026-10-04
- Outcome: **CHANGES_REQUESTED**
- Scope: Phase 11 implementation remediation against the active handoff and 2026-10-02 findings; review-only.
- Baseline: `1fd0031d77a3c195c4b7f7d2af48d0e64ca75f7b`; implementation remains uncommitted.
- Prior report: `2026-10-02-phase-11-final-codex-review.md` (historical, not overwritten).

## Verification and supplied evidence

Antigravity reports 769 tests with no failures/errors/skips in `../test-evidence.md`. Codex independently ran
`mvn -f backend/pom.xml -ntp clean verify`: exit 0, **BUILD SUCCESS; 769 tests, 0 failures, 0 errors, 0 skips**,
01:14, finished 2026-10-04T09:46:57+07:00. Architecture and Flyway/Hibernate validation passed.
`git diff --check` passed. No POM, migration, DBML or hook changes were found.

The isolated diagnostic `backend/target/final-review-diagnostics/StaleWeekdayDiagnostic.java` used Java source launch
with the Surefire XML java.class.path, compiled application classes and a disposable PostgreSQL 18.6 Testcontainer.
Exit 0, but output **STALE_READD_WEEKDAY_COUNT=0**, **RELOADED_WEEKDAYS=[]**. The diagnostic is ignored, not a
production edit or implementation regression test. Its reproducible sequence is described below.

## Findings still requiring remediation

### FR11-1 — High: detaching only currently stored children misses obsolete managed weekdays

`backend/src/main/java/com/vhvkhangg/personalprivatevault/finance/internal/application/RecurringTransactionRuleService.java:
138–145` queries the currently stored weekday rows and detaches only those before bulk replacement. This fixes the
single-context same/overlap/disjoint case, but not a preloaded child removed by another committed transaction:

1. Create a WEEKLY rule with MONDAY.
2. Transaction A loads it through public `findRuleById`, managing the MONDAY composite-key child.
3. Independent transaction B replaces the rule with TUESDAY and commits.
4. A full-replaces the rule with MONDAY again and commits.
5. The current-child query sees/detaches TUESDAY, not obsolete managed MONDAY. Saving MONDAY merges into the stale
   managed instance instead of inserting it. Reload returns no weekdays, despite WEEKLY configuration.

This exact sequence reproduced with independent transactions and ordered completion, without timing assumptions.
Refreshing the parent does not reconcile separately mapped children. Existing same-context replacement tests do not
exercise removal by another context; the replacement contention test starts its waiter fresh.

Required: reconcile all relevant owner-local managed child identities, including already-removed/reintroduced keys,
or use a deliberate persistence strategy that cannot merge into obsolete managed children. Do not globally clear,
change Schema v1 or use independently committed child writes. Add the sequence above plus an observed-lock-wait
variant with preloaded parent/children, same/overlapping/disjoint sets and rollback. Assert committed scalar fields,
exact weekday/entry rows and a fresh-context reload, not just the service's requested-input DTO.

### FR11-3 — High: recurring restore test does not preload the waiting context

`backend/src/test/java/com/vhvkhangg/personalprivatevault/finance/RecurringTransactionRuleIntegrationTest.java:595–646`
is named `contentionRestoreWithPreloadedParentAndChildren`, but neither participant explicitly preloads a parent or
child entity before waiting. The second participant directly invokes restore in a fresh service transaction, while
the winner only restores unchanged children. Thus stale-child recovery is not exercised. The analogous transaction
restore test at `FinancialTransactionIntegrationTest.java:456` also does not preload despite its name.

Required: preload the waiting participant's parent AND children in the same outer persistence context that performs
the mutation after waiting; coordinate a real competing committed state change, observe PostgreSQL blocking, and
verify the authoritative scalar/child results and rollback behavior. Preserve the now-present transaction update
preload, update-vs-delete and recurring replacement-vs-delete tests in both winner orders. Do not claim preloaded
restore coverage from a fresh direct service call. The remaining weekday race in FR11-1 must be covered too.

### FR11-5 — Medium: Subscription update race proof remains missing

The production create/update guards in `SubscriptionService` now follow Subscription -> Rule -> Wallet for updates
and Rule -> Wallet for creates, refresh owners, and preserve unchanged historical links. The four new contention
tests at `SubscriptionIntegrationTest.java:308–574` all invoke **createSubscription**. The update test at `:576` is
sequential; it does not prove changed-link assignment-vs-delete under contention or preservation of other fields
when a waiting update loses.

Required: add changed-link **updateSubscription** contention for both owner types in both winner orders, with
observed waits, unchanged-owner historical behavior, and scalar/link rollback/count assertions. Verify guard order
when both links change. No new lifecycle restrictions or cascading deletion is requested.

### FR11-7 — Medium: batch population tests do not prove constant query count

The production list methods now batch children by selected parent IDs, eliminating the identified loop queries.
But `FinancialTransactionIntegrationTest.java:514` and `RecurringTransactionRuleIntegrationTest.java:650` only assert
that views contain children. They would also pass the original N+1 implementation. No query counter, Hibernate
statistics or repository invocation-count assertion is present, although evidence explicitly claims constant-query
tests.

Required: a focused measured query-count or equivalent constant-fetch regression, comparing small/larger bounded
parent selections after fixture setup. Cover transaction global/wallet history and recurring list/due paths, parent
ordering/limits and absence of unrelated child loading. Correct evidence wording until this proof exists.

## Closed findings and partial progress

- **FR11-2 closed:** normalize exactly before checking persisted precision; money/rate bounds, scientific notation,
  trailing-zero equivalence and privacy-safe messages are covered by the 15-case validation suite.
- **FR11-4 closed:** nationality is normalized consistently before validation/persistence; blank/null and invalid
  country behavior is covered for self/nonself create/update.
- **FR11-6 closed for the original false-conflict defect:** uniqueness handling now identifies the frozen named
  active-self index/inline Subscription UNIQUE instead of treating every integrity error as a duplicate. Existing
  unique-index loser tests pass. Negative tests mainly prove input/reference rejection before database flush; they
  should not be described as exhaustive database-error classification proof.
- **FR11-8 original corrections closed:** Journal fields, Personal Gender and inline Subscription constraint name
  are corrected. New evidence still overstates preload/query-count closure, as FR11-3/7 explain.
- **FR11-1/3/5/7 partially remediated but not closed**, for the concrete reasons above.

## Other mandatory dimensions and limitations

Business/default/lifecycle, decimal, query, transaction/privacy and boundary behavior were checked against the
unchanged handoff and prior review. The original child defect has a narrower remaining concurrent-context variant;
passing totals do not outweigh the confirmed invariant failure. Capability-oriented public APIs, immutable views,
owner-local guards and bounded batch reads remain appropriate, without speculative frameworks or schema expansion.
No new scheduler, REST, Vault integration, cross-module repository sharing or dependency cycle was found.
Package organization and documented owned table/dependency sets remain as reviewed previously.

Warnings observed: Lombok Unsafe, frozen CsvImportParser deprecated API, test PostgreSQLContainer deprecated API,
Mockito/ByteBuddy attachment, and SpringDoc endpoint startup notices. No IDE inspection or warning-free assertion;
no SpotBugs/Checkstyle/Spotless claim. No production code was edited by Codex; only review/status documentation and
the ignored diagnostic were written. No publishing actions. Phase 11 remains unfrozen; no milestone is due until 12.

## Next step

Antigravity `/antigravity-implement-handoff`: fix FR11-1 and complete FR11-3/5/7 proof, update accurate evidence and
implementation result, run focused tests and full clean verify/diff check, then return ACTIVE to
`IMPLEMENTED_AWAITING_CODEX_REVIEW` for `$codex-final-review`. No commit message while these blockers remain.
