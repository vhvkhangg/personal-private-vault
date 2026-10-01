# Phase 11 Codex pre-handoff re-review

- Date: 2026-10-01
- Outcome: **CHANGES_REQUESTED**
- Scope: preparation remediation only; no production changes or implementation handoff.

## Disposition

- P11-2: **closed**. The canonical contract now defines same-rule update/delete/restore serialization,
  fresh parent/child state, coherent child-set replacement, deleted-rule behavior and deterministic PostgreSQL
  contention proof. Transaction and Subscription contention expectations were strengthened appropriately.
- P11-1: **partially resolved, still blocking**. Shared wallet/category guards, lock order and category-kind
  compatibility are now explicit. The new permanent-ever-referenced currency policy is not implementable by the
  specified checks under the unchanged schema and full-replacement contract.

## P11-1 — High — Permanent currency immutability requires history that Schema v1 does not retain

`phase-11/README.md:175–184` says currency may change only if a wallet has **never** been referenced, and becomes
**permanently** immutable after any reference. The prescribed guarded check reads only current rows in
`financial_transaction_entries` and `recurring_rule_entries`.

However, transaction/rule updates replace their complete entry sets. For example:

1. Create an income transaction with an entry for wallet A.
2. Replace that transaction's complete entry set with an entry for wallet B.
3. No current transaction/recurring entry now references A. A currency update's two-table check reports no reference,
   even though the permanent-ever-referenced policy requires rejection.

This holds whether replacement deletes/inserts children or updates their wallet IDs. Schema v1 has neither an
ever-referenced marker nor an entry revision/audit table. An in-memory flag or current-row existence check cannot
provide durable permanent history. The same contradiction is repeated in Finance AGENTS, the domain skill/rule,
preparation-review and the tests requiring permanent immutability.

### Required narrow preparation correction

Choose and consistently document a policy enforceable with frozen Schema v1. Two schema-preserving options are:

- prohibit currency change **while any retained entry row references the wallet**, including references of
  pending/cancelled/deleted transactions and inactive/deleted rules; explicitly define the outcome after full
  replacement removes the last reference; or
- make wallet currency immutable from creation, which does not require a persisted history marker.

These are alternatives, not changes approved or selected by this review. If the owner requires permanent
ever-referenced tracking while allowing full replacement, stop and obtain an explicit architecture/schema scope
change; do not smuggle a flag into notes, retain extra entries that violate shape rules, or infer history from IDs.

Add a regression contract covering the create-A / replace-with-B / currency-update-A sequence for both ledger and
recurring entries, including after persistence-context restart/reload. Preserve the accepted guards/lock ordering,
category policy and P11-2 remediation. Synchronize README, preparation-review, domain skill, rule and scoped AGENTS.

The initial review's suggestion to protect historical references must not be interpreted as permission to introduce
unrepresented historical state. Retained soft-deleted references are distinguishable; replaced-away references are not.

## Prerequisites and other dimensions

- ACTIVE remains `NO_ACTIVE_HANDOFF`; Phase 10 is frozen at `0c44091`, HEAD equals local origin/main.
  Remote push was not re-fetched. No milestone is required after Phase 10.
- Full amended README and affected domain skill/rule/Finance AGENTS reviewed; no production/test/POM/schema/hook
  or new custom-agent change appears in the preparation scope.
- Earlier ownership/dependency, public API/package, Journal/Personal, decimal/sign/schedule, privacy, bounded-read,
  tooling and deferred-scope assessments remain applicable. Recurring description length is now explicit.
- Frozen V1 wallet/entry tables and ADR-0011 checked directly. No generic ledger/history framework is warranted.
- No compiler/IDE inspection or new test suite run for this documentation-only gate; no warning-free claim.
- `git diff --check` passed before review edits and is rerun afterward. Status links updated to this current report.

## Next step

Give this finding/latest package to ChatGPT for the narrow wallet policy preparation correction, then rerun
`$codex-pre-handoff-review`. Do not create a handoff. No preparation commit message is issued while P11-1 remains open.
