# Backend Phase 11 — Finance + Journal + Personal Foundations

Status: **COMPLETE — FROZEN (2026-10-04)**

Implementation status: **COMPLETE — FROZEN** after Codex final acceptance, independent 787-test clean verification,
and owner commit/push. FR11-1 through FR11-9 are closed. The completed handoff is archived in
[`handoff.md`](handoff.md). Preparation/implementation details below are retained as historical contract/evidence.

Current review: [`reviews/2026-10-04-phase-11-final-codex-acceptance.md`](reviews/2026-10-04-phase-11-final-codex-acceptance.md).
FR11-1 through FR11-9 are closed. Preparation was owner committed/pushed as `1fd0031`; the accepted implementation
is now owner committed/pushed and frozen.

Phase 11 implements three independent top-level Spring Modulith modules:

- `finance`
- `journal`
- `personal`

Phase 11 implementation is complete, final-reviewed, owner committed/pushed, and frozen.

Future Finance/Journal/Personal changes require a new owner-approved feature or maintenance scope.

Phase 11 is **not** a milestone phase. The next milestone occurs after Phase 12.

## Frozen ownership and dependencies

### Finance

Owns exactly:

- `wallets`
- `transaction_categories`
- `financial_transactions`
- `financial_transaction_entries`
- `recurring_transaction_rules`
- `recurring_rule_weekdays`
- `recurring_rule_entries`
- `subscriptions`

Allowed dependency:

```text
finance -> reference
```

Use only public Reference catalog/view contracts.

### Journal

Owns exactly:

- `diary_entries`

Allowed dependencies:

```text
journal -> none
```

### Personal

Owns exactly:

- `personal_profiles`

Allowed dependencies:

```text
personal -> reference, location
```

Use only public Reference contracts and public Location Address contracts. Do not import Location internals.

No Phase 11 entity is Vault-backed. Finance/Journal/Personal `deleted_at` lifecycle is module-owned and must not call
Vault recycle-bin operations.

No DBML/Flyway/schema change is planned.

Wallet currency policy is intentionally defined only from state represented by frozen Schema v1. Phase 11 does
not infer or persist historical entry-reference facts after full child replacement.

# Shared Phase 11 persistence rules

## Decimal fidelity

Persisted money uses `BigDecimal`, never floating point.

- `numeric(19,4)` values must be exactly representable at scale 4 without rounding and fit precision 19;
- `financial_transactions.exchange_rate numeric(24,10)` must be exactly representable at scale 10 without rounding,
  fit precision 24, and be positive when present.

The implementation may normalize equivalent trailing-zero representations, but must never silently round a value to
fit the database column.

## Module-owned soft delete

The following rows have module-owned `deleted_at` state:

- Wallet;
- FinancialTransaction;
- RecurringTransactionRule;
- Subscription;
- DiaryEntry;
- PersonalProfile.

Rules:

- default business reads exclude deleted rows;
- explicit soft-delete sets `deleted_at`;
- restore clears `deleted_at`;
- soft-delete/restore is idempotent;
- hard/permanent delete is out of Phase 11;
- soft-delete does not cascade by inventing destructive behavior absent from Schema v1;
- references to a soft-deleted owner remain historical data;
- new/changed references must not target a currently deleted row.

`transaction_categories` use `active`, not `deleted_at`.

# Finance

Finance follows ADR-0011: wallet balance is derived from the ledger, never stored as a mutable second source of
truth.

## Public capability direction

Use semantic public packages/named interfaces such as:

```text
finance/
├── wallet/
├── category/
├── transaction/
├── recurring/
├── subscription/
├── view/
├── enums/
└── internal/
```

Exact package names may be refined by the handoff. JPA entities/repositories remain internal.

## Wallet contract

Frozen type values:

- `CASH`
- `BANK_ACCOUNT`
- `CARD`
- `E_WALLET`
- `OTHER`

Create/update scalar contract:

- `name`: required nonblank, max 255;
- `type`: required;
- `currency_code`: required and validated through `ReferenceCatalog.currency(code)`;
- `opening_balance`: null/omitted resolves to `0` on create and full-replacement update;
- `notes`: nullable;
- `active`: null/omitted resolves to `true` on create and full-replacement update;
- `created_at`, `updated_at`, `deleted_at`: service-managed.

`active = false` is not deletion. Historical transactions may still reference an inactive wallet. New/updated
transaction or recurring entries must reference an existing non-deleted wallet.

Required operations:

```text
createWallet
updateWallet
findWalletById
findWallets(activeFilter, limit)
currentBalance(walletId)
softDeleteWallet
restoreWallet
```

`findWallets` requires positive `limit`, excludes deleted rows, and orders `name ASC, id ASC`.

### Wallet currency mutation policy

Wallet currency is mutable only while **no currently retained ledger or recurring entry row references the wallet**.

The authoritative reference check covers current rows in:

- `financial_transaction_entries`; and
- `recurring_rule_entries`.

References still count even when their owning transaction/rule is pending, cancelled, inactive, or soft-deleted,
because the entry row itself remains retained and its amount is still interpreted using the wallet currency.

Phase 11 does **not** claim an "ever referenced" history that Schema v1 does not store.

If a full transaction/rule child-set replacement removes the wallet's **last retained** entry reference, a later
currency update is allowed again after that replacement commits. This is deliberate and schema-compatible: no
persisted entry row remains whose amount would be reinterpreted by the currency change.

Required regression sequence for **both** ledger entries and recurring-rule entries:

1. create an entry referencing wallet A;
2. full-replace the child set so only wallet B remains;
3. commit;
4. restart/reload the persistence context;
5. verify wallet A has no retained entry reference;
6. update wallet A to a different currency successfully.

A currency-change attempt while any retained reference exists must fail with a stable Finance-domain
validation/conflict exception without changing wallet currency, the retained entries, or derived balance.

Do not introduce a hidden "ever referenced" flag, audit table, notes marker, synthetic retained entry, or ID-based
history inference. Permanent ever-referenced immutability would require an explicit schema/architecture change and is
outside Phase 11.

### Wallet/reference race protocol

A pre-check alone is insufficient. Finance must use the same owner-local wallet row write guard on both sides of the
first-reference boundary:

- `updateWallet` acquires the wallet write guard before deciding whether `currency_code` may change, refreshes/re-reads
  authoritative wallet state, checks both entry tables while the guard is held, then updates or rejects;
- FinancialTransaction and RecurringRule create/update operations acquire write guards for **every wallet referenced by
  the submitted entry set before validating/persisting child entries**.

For an operation that references multiple wallets, acquire wallet locks in **ascending wallet ID order** and hold them
through the complete caller transaction. Do not lock wallets in command/input order.

Finance aggregate lock order is:

1. existing aggregate parent row first (`financial_transactions` or `recurring_transaction_rules`) when one exists;
2. referenced transaction category row when present;
3. all referenced wallet rows in ascending wallet ID order;
4. child-set replacement/persistence.

A create operation has no existing aggregate row, so it begins with category (if any), then ascending wallet IDs.

This protocol makes currency-change-vs-entry-assignment deterministic:

- if entry assignment wins, a waiting currency change observes the retained reference and rejects;
- if currency change wins while no retained reference exists, the waiting assignment refreshes/validates the wallet
  after the lock and proceeds using the new wallet currency;
- after a committed full replacement removes the last retained reference, a later currency update may succeed.

Do not use `REQUIRES_NEW`, advisory-lock frameworks, a global persistence-context clear, or schema changes.

### Current balance

Current balance is always derived:

```text
opening_balance
+ SUM(financial_transaction_entries.amount_delta)
  where transaction.status = POSTED
    and transaction.deleted_at IS NULL
= current balance
```

`PENDING`, `CANCELLED`, and soft-deleted transactions contribute zero.

Do not cache/persist a `current_balance` column.

## Transaction category contract

Frozen kinds:

- `INCOME`
- `EXPENSE`
- `BOTH`

Create/update:

- nonblank name, max 255;
- required kind;
- optional parent category;
- `active` null/omitted resolves to `true`;
- a category cannot directly parent itself;
- parent ID, when present, must exist.

Do not invent case-insensitive category-name uniqueness: Schema v1 has none.

Category compatibility with transactions/rules:

- an `INCOME` transaction/rule may use category kind `INCOME` or `BOTH`;
- an `EXPENSE` transaction/rule may use category kind `EXPENSE` or `BOTH`;
- a `TRANSFER` transaction/rule must have `category_id = null`.

Required operations include create/update/find-by-ID and a positive-bounded list ordered `name ASC, id ASC`.

### Category-kind mutation policy

Category compatibility is a persistent integrity rule, not merely a create-time selection hint.

Changing a category's `kind` is allowed only if the new kind remains compatible with **every** referencing
`financial_transactions` row and `recurring_transaction_rules` row, regardless of status/active/deleted state.
Therefore:

- changing to `INCOME` is rejected if any EXPENSE reference exists;
- changing to `EXPENSE` is rejected if any INCOME reference exists;
- changing to `BOTH` remains compatible with existing INCOME/EXPENSE references;
- TRANSFER never references a category.

Kind-changing `updateCategory` acquires the category row write guard, refreshes/re-reads authoritative state, and
checks historical references while holding the guard. Transaction/rule create/update paths that assign a category
acquire the same category guard before compatibility validation. A pre-check outside the guard is not authoritative.

This prevents a category-kind change from racing with the first incompatible transaction/rule reference. No category
history table or schema change is introduced.

## Financial transaction contract

Frozen types:

- `INCOME`
- `EXPENSE`
- `TRANSFER`

Frozen status values:

- `PENDING`
- `POSTED`
- `CANCELLED`

Create/update use full replacement semantics for transaction-owned scalar fields and the complete ledger-entry set.

- `type`: required;
- `status`: null/omitted resolves to `POSTED` on create and update;
- optional compatible category;
- `description`: nullable, max 1000;
- `notes`: nullable;
- `occurred_at`: required;
- `exchange_rate`: optional, positive when present;
- `generated_by_recurring_rule_id`: service-owned; manual create/update commands must not forge it;
- entries are replaced atomically as one set on update.

### Ledger shape/sign invariants

For a non-deleted transaction:

#### INCOME

- exactly one entry;
- amount delta is positive.

#### EXPENSE

- exactly one entry;
- amount delta is negative.

#### TRANSFER

- exactly two entries;
- two distinct wallets;
- exactly one negative source delta;
- exactly one positive destination delta.

For all entry sets:

- amount delta cannot be zero;
- one wallet appears at most once per transaction;
- every wallet exists and is non-deleted;
- wallet currency determines the entry currency;
- no same-currency zero-sum equality or cross-currency mathematical exchange formula is invented beyond the frozen
  sign/shape rules;
- cross-currency transfers may record a positive `exchange_rate`, but Schema v1 does not require it.

Category compatibility is enforced using the rule above.

### Transaction concurrency/atomicity

Create/update/status change/soft-delete/restore and ledger-entry replacement are one transaction.

Mutations of the same existing transaction must serialize through an owner-local transaction-row write guard and
recheck fresh authoritative state after the guard is acquired when an entity may already be managed. This prevents
lost full-replacement updates and balance-visible state races.

For entry-set create/update, apply the Finance lock order defined in the Wallet section: aggregate row (when present),
category row (when present), then all referenced wallet rows in ascending wallet ID order. Refresh/re-read managed
parent state under the transaction guard before validating/replacing children.

The deterministic PostgreSQL contention regression must observe a real lock wait rather than rely on sleeps, include
a preloaded persistence-context case, and assert committed scalar + child state, derived balance, and rollback/no
partial-entry effects.

Do not use `REQUIRES_NEW` or independently commit ledger entries.

Required reads:

- transaction by ID;
- recent transactions for one wallet with positive `limit`, ordered `occurred_at DESC, id DESC`;
- optional recent global Finance history with positive `limit`, same order.

No unbounded transaction list.

## Recurring transaction rule contract

Frozen values:

`recurring_posting_mode`:

- `AUTO_POST`
- `REQUIRE_CONFIRMATION`

`recurrence_frequency`:

- `DAILY`
- `WEEKLY`
- `MONTHLY`
- `YEARLY`

Create/update fields:

- nonblank name, max 500;
- transaction type + compatible category;
- posting mode and frequency required;
- `interval_count > 0`, default `1`;
- `start_date` required;
- optional `end_date >= start_date`;
- optional `posting_time`;
- optional `next_run_at`;
- optional description, max 1000, and optional notes;
- `active` null/omitted resolves to `true`;
- complete weekday set and complete recurring-entry set are full-replacement child collections.

### Schedule-field compatibility

- `DAILY`: no weekday rows, `day_of_month = null`, `month_of_year = null`;
- `WEEKLY`: one or more weekday rows, `day_of_month = null`, `month_of_year = null`;
- `MONTHLY`: no weekday rows, `day_of_month` required `1..31`, `month_of_year = null`;
- `YEARLY`: no weekday rows, both `day_of_month 1..31` and `month_of_year 1..12` are required.

Recurring entry shape/sign and category compatibility are the same as FinancialTransaction:

- INCOME = exactly one positive entry;
- EXPENSE = exactly one negative entry;
- TRANSFER = exactly two distinct wallets, one negative + one positive;
- every referenced wallet exists and is non-deleted.

Exact duplicate weekdays are set semantics and converge to one row.

### Recurring aggregate mutation serialization

Every mutation of one existing RecurringTransactionRule uses the same Finance-owned pessimistic write guard:

- scalar/full-child `updateRecurringRule`;
- `softDeleteRecurringRule`;
- `restoreRecurringRule`;
- any later Phase 11 mutation of that same rule.

After acquiring the rule row guard:

1. refresh/re-read the authoritative parent even if it was already managed;
2. reload/refresh the current weekday and recurring-entry child state needed for validation;
3. validate the requested frequency fields, weekday set, ledger-entry set, category compatibility, and wallet
   references against that fresh state;
4. perform scalar + complete child replacement in one transaction;
5. hold the guard until commit/rollback.

`updateRecurringRule` on a soft-deleted rule is rejected. Restore must be used first. Soft delete/restore are
idempotent and do not rewrite retained child/history rows. Restore revalidates the retained schedule/entry shape from
fresh state but does not reinterpret retained historical wallet references as new assignments.

New/changed wallet references during an update still require non-deleted wallets and the canonical wallet lock order.
The same category guard/reference policy applies to recurring category selection.

This is owner-local aggregate serialization only: no optimistic-version schema change, global clear, `REQUIRES_NEW`,
retry framework, scheduler, or posting engine.

### Recurring contention proof

Required PostgreSQL regressions must use explicit transaction coordination and observed real lock waiting:

- replacement-vs-replacement on the same rule with incompatible frequency/weekday sets and different entry sets;
- replacement-vs-soft-delete in both lock winner orders;
- restore contention from a deleted rule, including a preloaded parent/child persistence-context case.

After contention completes, persisted scalar fields, weekdays, and recurring entries must form one coherent
configuration from a serialized operation order; mixed child sets are forbidden. Rejected/rolled-back operations
leave the winning committed configuration unchanged.

### Phase 11 recurrence execution boundary

Phase 11 implements recurring-rule **configuration and due-state reads**, not the runtime scheduler/automatic
materialization of financial transactions.

`next_run_at` is explicit persisted schedule metadata in Phase 11. The module does not invent an application timezone
dependency, auto-advance algorithm, `@Scheduled` trigger, or automatic `generated_by_recurring_rule_id` posting.

Required due read:

```text
active = true
AND deleted_at IS NULL
AND next_run_at IS NOT NULL
AND next_run_at <= cutoff
```

Require positive `limit`; order `next_run_at ASC, id ASC`; the read is side-effect free.

Scheduler/materialization integration belongs to later backend operational closure unless separately approved.

## Subscription contract

Create/update fields:

- nonblank name, max 500;
- optional provider, max 500;
- `price_amount` required and nonnegative;
- currency required and validated through Reference;
- billing cycle required;
- `billing_interval > 0`, null/omitted resolves to `1`;
- `CUSTOM` requires `custom_cycle_days > 0`;
- non-CUSTOM requires `custom_cycle_days = null`;
- optional next billing date;
- `auto_renew` null/omitted resolves to `true`;
- optional payment wallet must exist and be non-deleted;
- optional recurring rule must exist and be non-deleted;
- optional URL max 2048 and notes;
- `active` null/omitted resolves to `true`.

The `subscriptions.recurring_rule_id` database uniqueness is the final race arbiter: one recurring rule may be linked
to at most one Subscription. Sequential/concurrent conflicts translate to a stable privacy-safe Finance-domain
conflict.

The concurrency regression must drive both callers past any application pre-check into real PostgreSQL unique-index
competition: keep the first insert uncommitted, observe the second waiting/conflicting at the database boundary,
then commit the winner and verify stable loser translation with no partial Subscription state.

Phase 11 stores renewal/link metadata only. It does not automatically charge a wallet, advance
`next_billing_date`, or invoke a scheduler.

Required reads:

- subscription by ID;
- positive-bounded active/upcoming subscriptions ordered `next_billing_date ASC NULLS LAST, id ASC`.

## Finance soft-delete interactions

- deleting a FinancialTransaction immediately removes a POSTED transaction from derived wallet balances;
- restoring it restores its balance contribution according to current status;
- deleting a Wallet does not delete historical transaction entries;
- deleted wallets cannot be newly assigned to transaction/rule entries or Subscription payment wallet;
- deleting a RecurringTransactionRule does not delete a linked Subscription;
- deleting a Subscription does not delete its rule or wallet.

# Journal

Journal has no application-module dependencies and is not Vault-backed.

## DiaryEntry contract

Create/update:

- `entry_date`: required;
- `title`: nullable, max 500;
- `content_markdown`: required and preserved exactly, including leading/trailing whitespace;
- timestamps/deleted state are service-managed.

Multiple entries per calendar day are explicitly allowed. Do not invent a uniqueness rule on `entry_date`.

Required operations:

```text
createDiaryEntry
updateDiaryEntry
findDiaryEntryById
findDiaryEntries(fromDate, toDate, limit)
softDeleteDiaryEntry
restoreDiaryEntry
```

For range reads:

- date bounds are optional;
- if both are present, `fromDate <= toDate`;
- positive `limit` required;
- exclude deleted rows;
- order `entry_date DESC, id DESC`.

Do not implement global/full-text search in Phase 11; Phase 12 owns global Search.

Journal soft delete retains Markdown for restore. Hard delete is deferred.

# Personal

Personal is not Vault-backed.

## PersonalProfile contract

Frozen gender values:

- `MALE`
- `FEMALE`

Create/update full-replacement scalar contract:

- `name`: required nonblank, max 255;
- `relationship`: required nonblank free-form string, max 100; do not replace it with an enum;
- `is_self`: null/omitted resolves to `false`;
- optional gender;
- optional birth date;
- optional nationality code validated by `ReferenceCatalog.country(code)`;
- optional phone max 64;
- optional email max 320; Phase 11 does not invent stricter email syntax than the frozen schema;
- optional address ID validated through public `AddressOperations.findById`;
- optional occupation max 255;
- optional `notes_markdown`, preserved exactly;
- timestamps/deleted state are service-managed.

Blank optional text may normalize to null except `notes_markdown`, whose non-null text is preserved exactly.

## One active self profile

Flyway V1 enforces:

```text
UNIQUE (is_self)
WHERE is_self = true AND deleted_at IS NULL
```

Application behavior:

- at most one non-deleted profile may have `is_self = true`;
- a second sequential create/update-to-self returns a stable `PersonalProfileConflictException`;
- concurrent create/update-to-self races rely on PostgreSQL partial uniqueness as the final arbiter and translate the
  loser to the same domain conflict without raw database/private detail;
- soft-deleting the self profile frees the active-self slot;
- restoring a deleted self profile while another active self exists fails with the same stable conflict;
- changing the active self profile to `is_self = false` frees the slot.

Do not auto-demote another profile to make a new one self.

## Personal reads and lifecycle

Required operations:

```text
createPersonalProfile
updatePersonalProfile
findPersonalProfileById
findSelfProfile
findPersonalProfiles(limit)
softDeletePersonalProfile
restorePersonalProfile
```

`findPersonalProfiles` requires positive `limit`, excludes deleted rows, and orders `name ASC, id ASC`.

`findSelfProfile` returns the sole non-deleted self profile when present.

Hard delete is deferred.

# Testing contract

Use PostgreSQL Testcontainers and unchanged Flyway V1.

## Finance tests

Cover:

- all eight Finance-owned tables unchanged from Schema v1;
- exact decimal scale/precision/no-silent-rounding validation;
- currency validation through public Reference only;
- Wallet defaults, update, soft delete/restore, bounded reads;
- wallet currency changes are rejected while any retained `financial_transaction_entries` or
  `recurring_rule_entries` row references the wallet, including pending/cancelled/deleted/inactive owners;
- sequential currency-change rejection preserves wallet currency, retained entries, and derived balance;
- deterministic PostgreSQL currency-change-vs-entry-assignment in both winner orders using the shared wallet guard
  and ascending multi-wallet lock order; rejected paths leave zero partial child writes;
- create-A / full-replace-with-B / restart-or-reload / currency-update-A succeeds once A has no retained references,
  proven separately for financial transaction entries and recurring rule entries;
- category-kind changes reject historical incompatibility and are race-safe against first transaction/rule
  assignment through the shared category guard;
- current balance = opening + POSTED/non-deleted entries only;
- PENDING/CANCELLED/deleted transaction balance exclusion and restoration effects;
- category create/update/parent existence/self-parent rule and type compatibility;
- INCOME/EXPENSE/TRANSFER exact entry shape/sign and distinct-wallet rules;
- full transaction + entry replacement atomicity;
- transaction mutation row-lock/fresh-state contention with observed PostgreSQL waiting, preloaded context,
  committed scalar/child/balance assertions, and rollback;
- cross-currency transfer acceptance without invented zero-sum/exchange requirement;
- recurring frequency-field compatibility;
- recurring entry shape/sign/category rules and weekday set behavior;
- recurring same-rule replacement-vs-replacement, replacement-vs-soft-delete, and restore contention with fresh
  parent/child state under one rule guard and no mixed scalar/child configuration;
- due-rule predicate/order/limit and side-effect-free read;
- Subscription custom-cycle checks, Reference currency, wallet/rule validation;
- sequential and real concurrent Subscription recurring-rule uniqueness conflict translation, with concurrent
  callers driven past pre-checks into actual PostgreSQL unique-index competition;
- Finance module may import only public Reference interfaces;
- no Finance -> Vault/Journal/Personal/Settings dependency.

## Journal tests

Cover:

- `diary_entries` schema fidelity;
- multiple entries on one day;
- exact Markdown preservation on create/update/reload;
- optional title constraints;
- bounded date-range ordering;
- soft delete hides from default reads; restore returns it;
- no Vault interaction and no application-module dependency.

## Personal tests

Cover:

- `personal_profiles` schema + partial active-self uniqueness;
- required/free-form fields and optional length checks;
- country validation through public Reference;
- address validation through public Location Address API only;
- multiple non-self duplicate profiles allowed;
- sequential second-self conflict;
- deterministic real PostgreSQL concurrent create/update-to-self conflict with one winner;
- self soft-delete frees the slot;
- restore-self conflict when another active self exists;
- changing self to non-self frees the slot;
- exact `notes_markdown` preservation;
- bounded active-profile reads;
- Personal dependencies limited to public Reference + Location Address contracts.

## Architecture / final verification

- meaningful package-info files for implemented semantic public/internal packages;
- remove Phase 11 `.gitkeep` placeholders when implementation fills them;
- Spring Modulith verifies exact module dependencies/named interfaces;
- no schema migration;
- preserve all 685 Phase 10/baseline regressions.

Final commands:

```text
mvn -f backend/pom.xml -ntp clean verify
git diff --check
```

Record exact commands, Java/Maven/PostgreSQL/Testcontainers versions, total tests/failures/errors/skips, focused
Finance/Journal/Personal test names/results, Flyway/Hibernate and Modulith results in
`docs/implementation/phase-11/test-evidence.md`.

# Out of scope

- scheduler runtime / `@Scheduled` finance posting;
- automatic recurring-rule materialization;
- automatic Subscription charging/renewal advancement;
- REST/controllers/OpenAPI;
- frontend;
- global Search (Phase 12);
- Finance/Journal calendar aggregation;
- Vault favorite/rating/tag integration for Finance/Journal/Personal;
- import/export additions;
- hard/permanent delete;
- Phase 12+ implementation;
- DBML/Flyway/schema redesign;
- generic ledger/event/scheduler/repository frameworks;
- custom agents/hooks.

## Preparation tooling

Added for Phase 11:

- `.agents/skills/finance-journal-personal-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-11-finance-journal-personal.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/finance/AGENTS.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/journal/AGENTS.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/personal/AGENTS.md`

Reused without new custom agents/hooks:

- `backend-implementer`
- `architecture-auditor`
- repository safety hook


## Completion record

- Preparation owner commit/push: `1fd0031`.
- Final Codex acceptance:
  [`reviews/2026-10-04-phase-11-final-codex-acceptance.md`](reviews/2026-10-04-phase-11-final-codex-acceptance.md).
- Owner implementation commit/push: completed 2026-10-04.
- Independent final verification: **787 tests**, 0 failures/errors/skips.
- Pre-Phase-11 baseline: 685 tests.
- Phase 11 additions: 95 domain tests + 7 architecture tests.
- PostgreSQL 18.6 Testcontainers, Flyway/Hibernate validation, Spring Modulith verification, and `git diff --check`
  passed according to retained evidence.
- Completed handoff: [`handoff.md`](handoff.md).
- Verification evidence: [`test-evidence.md`](test-evidence.md).
- Status: **COMPLETE — FROZEN**.
- Next gate: Phase 12 `$codex-pre-handoff-review`.
