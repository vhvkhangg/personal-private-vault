# Phase 11 Pre-Handoff Preparation Review

Status: **READY FOR HANDOFF**

Current formal review: [`reviews/2026-10-01-phase-11-pre-handoff-codex-acceptance.md`](reviews/2026-10-01-phase-11-pre-handoff-codex-acceptance.md).
P11-1/P11-2 closed. Retained-reference currency policy and recurring concurrency accepted.
Owner commits/pushes preparation, then invokes `$codex-create-handoff`.

## Preconditions

- Phase 10 Feed + ImportData is complete/frozen after final acceptance and owner commit/push.
- Phase 10 final evidence records 685 passing tests.
- Phase 10 does not require a milestone review.
- `docs/implementation/handoffs/ACTIVE.md` is `NO_ACTIVE_HANDOFF`.

## Scope

Review Phase 11 Finance + Journal + Personal preparation only. Do not create an implementation handoff during this
review.

## Remediated Codex findings

The initial review found two Finance preparation blockers. Final acceptance closes both with this contract:

1. wallet currency changes are blocked only while a **currently retained** transaction/recurring entry row references
   the wallet; after full replacement removes the last retained reference, currency change is allowed again;
2. category kind changes must remain compatible with every historical transaction/rule reference and use a shared
   category guard;
3. recurring update/full child replacement/delete/restore share one recurring-rule row guard with fresh parent/child
   state under the lock;
4. deterministic PostgreSQL contention tests observe real lock waiting and assert coherent persisted child/scalar
   state, balances, counts, and rollback;
5. Subscription recurring-rule uniqueness races must reach actual PostgreSQL unique-index competition;
6. recurring description max length is explicitly 1000.

No production/schema/POM change is part of this remediation.

## Prepared artifacts

- `docs/implementation/phase-11/README.md`
- `docs/implementation/phase-11/preparation-review.md`
- `docs/implementation/phase-11/reviews/README.md`
- `.agents/skills/finance-journal-personal-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-11-finance-journal-personal.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/finance/AGENTS.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/journal/AGENTS.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/personal/AGENTS.md`
- implementer/auditor skill routing updates

No Phase 11 production Java/test implementation, Flyway/DBML/POM change, custom agent, or hook change is included.

## P11-1 schema-compatible policy clarification

Schema v1 does not retain "ever referenced" history after full child replacement. Phase 11 therefore uses
**current retained references only** as the authoritative currency rule:

- retained entry exists -> currency change rejected;
- last retained entry is removed by committed full replacement -> currency change may succeed afterward;
- persistence-context restart/reload must not change that result.

This requires no new column/table/history marker and preserves the accepted wallet/category guards and lock order.

## Required Codex checks

Verify at minimum:

- Finance owns exactly eight frozen tables and depends only on public Reference;
- Journal owns exactly `diary_entries` and has no application-module dependency;
- Personal owns exactly `personal_profiles` and depends only on public Reference + Location Address;
- no Phase 11 entity is Vault-backed and module-owned `deleted_at` behavior does not misuse Vault recycle operations;
- decimal precision/scale/no-rounding rules match Schema v1;
- wallet current balance matches ADR-0011 and counts only POSTED non-deleted transactions;
- wallet currency mutation is prohibited while any retained transaction/recurring entry row references the wallet,
  and uses the same wallet guard as entry assignment with ascending wallet-ID lock order;
- create-A / replace-with-B / restart-or-reload / currency-update-A must succeed once no retained A reference exists,
  for both ledger and recurring entries;
- category kind mutation cannot invalidate historical transaction/rule references and shares a category guard
  with category assignment;
- transaction category compatibility and INCOME/EXPENSE/TRANSFER shape/sign rules satisfy the frozen DBML implementation
  notes without inventing a same-currency conservation formula;
- transaction mutation serialization/fresh-state behavior explicitly covers preloaded contexts, observed lock
  waiting, complete child state, derived balance, and rollback;
- recurring frequency fields and child collections have one coherent validation contract plus a shared recurring
  row guard for update/full replacement/delete/restore with fresh parent/child state;
- recurring scheduler/materialization is intentionally deferred while `next_run_at` remains explicit metadata;
- Subscription custom-cycle, wallet/rule links, unique recurring-rule conflict behavior, actual PostgreSQL
  unique-index competition tests, and no-auto-charge boundary are explicit;
- Journal preserves Markdown exactly, allows multiple entries per day, and has bounded date reads + module soft delete;
- Personal relationship remains free-form, Reference/Address validation stays public-boundary-only, and the one active
  self partial uniqueness has sequential/concurrent/restore conflict semantics;
- bounded deterministic reads exist for every collection-valued public query;
- Phase 11 does not introduce Search/REST/frontend/scheduler/calendar aggregation/Phase 12 work;
- existing agents/hooks are sufficient;
- status/docs/links are internally consistent.

## Invoke

```text
$codex-pre-handoff-review
```

Successful result:

```text
READY FOR HANDOFF
```

If Codex returns `CHANGES_REQUESTED`, give the findings/latest package to ChatGPT for narrow preparation remediation.


## Next action

After owner preparation commit/push, invoke:

```text
$codex-create-handoff
```

No handoff was created during this review.
