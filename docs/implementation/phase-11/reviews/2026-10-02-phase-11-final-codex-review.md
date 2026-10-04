# Phase 11 Codex final review

- Date: 2026-10-02
- Outcome: **CHANGES_REQUESTED**
- Scope: implemented `phase-11-finance-journal-personal-foundations` handoff; review-only.
- Baseline: `1fd0031d77a3c195c4b7f7d2af48d0e64ca75f7b`, uncommitted Phase 11 implementation plus handoff/status docs.
- Preparation acceptance remains valid; this report concerns implementation, not a reopened preparation gate.

## Verification

Antigravity supplied `docs/implementation/phase-11/test-evidence.md`: clean verify with 737 tests, no failures/errors/
skips. Codex independently ran `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, **737 tests,
0 failures, 0 errors, 0 skips**, 01:20, finished 2026-10-02T20:39:11+07:00. Schema validation, the Flyway manifest and
23 architecture tests passed. No POM, migration, DBML or hook change was found. `git diff --check` passed.

Codex also ran an untracked diagnostic in `backend/target/final-review-diagnostics/Phase11ReviewDiagnostic.java`,
using the Surefire XML `java.class.path`, Java source launch, existing compiled classes and a disposable PostgreSQL
18.6 Testcontainer. Final run exit 0. An initial harness datasource-configuration failure was corrected before the
successful reproduction; it is not an implementation finding. The diagnostic is not a committed implementation test.

- Money `1000000000000000` and `1E+15` were accepted, then normalized to precision **20** (numeric(19,4) overflow).
- Representable `1.00000000000000000000` was rejected solely for input precision.
- Within one outer transaction: load a WEEKLY rule with MONDAY via public find, then full-replace it with WEEKLY/
  MONDAY again and commit. The persisted weekday count was **0**, not 1.

These green tests therefore do not establish handoff acceptance.

## Blocking findings

Paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/` unless stated otherwise.

### FR11-1 — High: preloaded recurring weekdays disappear on full replacement

`finance/internal/application/RecurringTransactionRuleService.java:134–138` bulk-deletes weekdays then calls Spring
Data save/merge with the same composite keys. `RecurringRuleWeekdayRepository.java:24` does not reconcile the
persistence context. When public reads have already managed a weekday, the bulk delete leaves that managed object
present and merge does not reinsert the database row. The diagnostic above confirms a committed WEEKLY rule with
zero weekdays. Refreshing only the parent in `FinanceLockManager` cannot repair independently mapped children.

Required: make replacement and post-guard child reads safe for preloaded managed state using owner-local lifecycle/
refresh/detach or authoritative projections as appropriate, without global context clear or schema changes. Cover
same/overlapping/disjoint weekday replacement after a public preload, repeated updates within one caller transaction,
commit/reload, and rollback. Verify recurring entries and restore child validation also use authoritative state.

### FR11-2 — High: decimal bounds are checked before conversion to persisted scale

`finance/internal/application/FinanceValidationUtils.java:43–51,81–89` compares input precision, not precision after
exact scale conversion. Sixteen integer digits pass money validation but exceed the 15 integer digits in numeric(19,4).
Likewise `100000000000000` passes rate input precision but exceeds numeric(24,10)'s 14 integer digits. Conversely,
extra equivalent trailing zeros can make valid numbers fail. Errors are then deferred to PostgreSQL or misclassified
by broad integrity catches; monetary values are also interpolated into validation exception text.

Required: exact conversion with UNNECESSARY, then validate normalized precision/range; accept equivalent zero-padded
representations and reject overflow with the owning domain exception before persistence. Keep failure messages free
of private monetary values. Add money/rate max boundary, just-over-boundary, negative/zero where applicable,
scientific/negative-scale and trailing-zero tests across Wallet, transaction/rule entries and Subscription.

### FR11-3 — High: mandatory contention/transition evidence is incomplete

Under `backend/src/test/java/com/vhvkhangg/personalprivatevault/`, the recurring suite has only four tests and only
replacement-vs-replacement contention (`finance/RecurringTransactionRuleIntegrationTest.java:239`). It lacks
replacement-vs-delete in both winner orders and restore contention with preloaded parent/children. Transaction
contention (`finance/FinancialTransactionIntegrationTest.java:262`) starts the waiter directly in a fresh service
call; it never preloads its context or proves rejection/rollback leaves no partial children. Personal has concurrent
create-to-self but no concurrent update-to-self proof (`personal/PersonalProfileIntegrationTest.java:254`).

Required: implement these explicitly mandated cases with independent transactions, observed PostgreSQL blocking,
fresh persisted scalar/child/balance/count assertions and rejected/rolled-back loser checks. Add missing relevant
boundary/default/retained-reference states from the README Testing contract, including recurring assignment guards.
Do not substitute sequential lifecycle tests or timing-only overlap for contention. Rerun focused suites and full
clean verify; record exact new test names, coordination and outcomes rather than claiming blanket coverage.

### FR11-4 — Medium: blank nationality bypasses validation but is still persisted

`personal/internal/application/PersonalProfileService.java:244` skips public Reference validation for blank nationality
yet create/update persist the original string (`:70,:126`). Empty/whitespace values are not valid countries and fail
the frozen foreign key (or length constraint) instead of normalizing to null or returning a stable domain error.
For isSelf=true the same failure can be falsely reported as a second-self conflict.

Required: resolve the optional code once, consistently normalize blank to null (or reject with a stable domain
validation error), validate every retained non-null code via public Reference and persist that resolved value.
Test null/empty/whitespace, unknown and valid country on create/update, self and nonself, and rollback preservation.

### FR11-5 — Medium: Subscription assignments race with wallet/rule deletion

`finance/internal/application/SubscriptionService.java:250–264` reads referenced owners without their shared write
guards. A create/update can see an active owner, allow concurrent soft-delete to commit, then persist a new link to
the now-deleted wallet/rule. Foreign keys cannot detect soft deletion. This bypasses the accepted no-new-assignment-
to-deleted-owner rule, unlike transaction/rule entry assignments that already hold the canonical guards.

Required: validate new/changed Subscription links under the same Finance-owned rule/wallet guards as lifecycle
mutations, refresh after waiting, hold until commit and maintain a compatible lock order. Retain historical links
without cascading or inventing restore restrictions. Prove assignment-vs-delete in both winner orders for each
optional owner, including update, rejection with no partial Subscription changes, and no guard-order inversion.

### FR11-6 — Medium: all integrity failures are mislabeled as uniqueness conflicts

`finance/internal/application/SubscriptionService.java:86,148` translates any DataIntegrityViolationException when
recurringRuleId is non-null into duplicate-rule conflict. `personal/internal/application/PersonalProfileService.java:
81,137,206` similarly treats every integrity violation on self as active-self conflict. Decimal overflow, invalid
nationality or other FK/check failures therefore produce false diagnoses, not the promised stable unique-race result.

Required: recognize only the actual frozen uniqueness violation using existing privacy-safe exception inspection
conventions; do not invent names (V1 uses inline recurring_rule_id UNIQUE). Translate other expected validation/
reference failures deliberately and never expose raw SQL, sensitive parameters or raw causes. Tests must distinguish
unique losers from other integrity failures on commands that also carry the relevant link/self flag.

### FR11-7 — Medium: bounded Finance lists still perform N+1 child queries

`finance/internal/application/FinancialTransactionService.java:168,179,215` loads entries once per returned parent;
`RecurringTransactionRuleService.java:194,210,252` loads weekdays and entries once per parent. Listing N transactions
costs 1+N queries (plus wallet existence for wallet history), and listing N rules costs 1+2N. Positive limits have no
small maximum, so ordinary history/due reads can generate hundreds of round trips.

Required: retain bounded parent selection/order, batch child loads by selected parent IDs, group into views without
changing ordering or loading unselected history. Add a focused query-count or equivalent constant-query regression;
no global cache/eager graph/generic framework is needed.

## Non-blocking documentation correction

**FR11-8 — Low:** ACTIVE implementation result describes Journal `content`/`mood` and Personal `PersonalProfileKind`;
the actual contracts contain content_markdown/title and Gender, not those features. Evidence names
`uq_subscriptions_recurring_rule_id`, whereas frozen V1 defines inline UNIQUE. Correct these descriptions, actual
constraint names and the observed compiler notices during resubmission. Synchronize implementation statuses without
marking Phase 11 frozen or altering historical preparation acceptance.

## Other review dimensions and limitations

- Capability-oriented commands/exceptions/views/enums and package-info are present; filled internal placeholders
  removed. Narrow named interfaces and immutable copied collections are appropriate; no speculative framework,
  new agents, hooks, scheduler, REST or Vault persistence leakage was found.
- Shared Finance ledger shape/category validation is duplicated between transaction and recurring services. Consider
  a small owner-local policy if fixing these paths, but no generic hierarchy is required; this is advisory.
- New entities use owned scalar IDs, PostgreSQL enums, BigDecimal and UTC instants; schema validation passed.
  The explicit guard protocol is appropriate in principle, but FR11-1/3/5 prevent accepting its completeness.
- No new payload logging or custom cryptography was found. Existing privacy-safe constraint logging assertions pass;
  safe exception classification still requires FR11-6. DTO string redaction was not independently exhaustively tested.
- Observed notices: Lombok sun.misc.Unsafe, frozen CsvImportParser deprecated API, test PostgreSQLContainer deprecated
  API, Mockito/ByteBuddy attachment and SpringDoc startup notices. No IDE inspection or warning-free claim. No
  SpotBugs/Checkstyle/Spotless claim: the supplied POM's clean verify does not execute those tools.
- No production implementation was changed by Codex. Only review/status documentation and ignored diagnostic output
  were created/updated. No commit/push/tag/PR. This is not a milestone review; next milestone is after Phase 12.

## Next step

Return to Antigravity `/antigravity-implement-handoff` for FR11-1 through FR11-7 and FR11-8 documentation correction.
Preserve frozen scope, write regressions, update evidence and implementation result, then set ACTIVE back to
`IMPLEMENTED_AWAITING_CODEX_REVIEW` and run `$codex-final-review`. No commit message while blockers remain.
