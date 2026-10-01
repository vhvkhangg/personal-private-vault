# Phase 11 Codex pre-handoff review

- Date: 2026-10-01
- Outcome: **CHANGES_REQUESTED**
- Scope: Finance + Journal + Personal preparation; review-only, no production implementation or handoff created.

## Prerequisites

- Phase 10 owner commit is `0c440919d47e8d1c221350d5041ae73f9baa7e51`; HEAD equals local origin/main.
  No remote fetch was performed; push state is supported by the local tracking ref and supplied closeout docs.
- Phase 10 is closed/frozen in current docs with 685 passing tests; completed handoff is archived.
- ACTIVE is `NO_ACTIVE_HANDOFF`; Phase 11 README and preparation-review exist.
- Phase 10 is not a milestone phase; no additional milestone prerequisite applies.
- Reviewed the prepared concept as submitted for this gate; review does not itself authorize implementation.

## Blocking findings

### P11-1 — High — Wallet currency changes can reinterpret existing ledger amounts

`docs/implementation/phase-11/README.md`, Wallet contract, exposes `currency_code` in create/update scalars,
but does not define when changing it is allowed. The ledger tables have no entry-owned currency snapshot;
the frozen DBML explicitly says wallet currency determines each entry currency. After a USD wallet receives
posted USD deltas, changing the wallet to EUR relabels the same historical amounts and derived balance without
any exchange/conversion transaction. An existing recurring rule entry is affected in the same way.

Required preparation correction:

- Define the owner-approved currency-mutation policy before handoff; do not leave the implementer to invent it.
  A proportionate schema-preserving policy is to prohibit changing currency once any historical ledger or
  recurring entry references the wallet, including entries of pending/cancelled/deleted owners. Another policy
  requires explicit owner justification compatible with the frozen ledger representation.
- Specify race safety between currency mutation and first entry assignment. A pre-check alone cannot ensure
  the policy: use a coherent Finance-owned wallet guard/recheck protocol on both paths, with a deliberate lock
  order for multi-wallet transactions; avoid deadlocks or independently committing reference checks.
- Add sequential and deterministic PostgreSQL currency-change-vs-entry-assignment regressions. Verify unchanged
  history/currency/balance on rejection and rollback across all entry writes.
- Clarify reference mutation semantics for category-kind changes too: either compatibility is a write-time
  selection rule only (explicitly allowing historical reinterpretation), or reject changes that invalidate
  referenced transactions/rules. Do not silently assume a global invariant while permitting unguarded parent edits.

This is a business/integrity decision within Finance preparation, not permission to change DBML/Flyway or add
currency snapshots/conversion machinery.

### P11-2 — High — Recurring full replacement lacks aggregate serialization and fresh-state proof

The recurring-rule contract requires coherent frequency fields plus full weekday/entry replacement, but the
explicit row-guard requirement covers FinancialTransaction only. Two same-rule updates can validate against
the same old state, independently replace child sets, and leave a final scalar frequency inconsistent with a
mixed/stale weekday or ledger-entry collection. Recurring update vs soft-delete/restore has the same stale-state risk.
Unique weekday/entry constraints do not enforce frequency-field or sign/shape coherence.

Required preparation correction:

- Define one owner-local recurring-rule row guard for update/full child replacement/soft-delete/restore (and any
  other same-rule mutation); re-read/refresh authoritative parent and relevant already-managed child state under
  the guard before validating, and hold it through one complete transaction.
- State what update of a deleted rule permits/rejects, and how restore handles retained historical references.
  Keep scheduler/materialization deferred; no global clear, schema version column or REQUIRES_NEW is required.
- Add deterministic real PostgreSQL same-rule replacement-vs-replacement and replacement-vs-delete/restore
  regressions, including preloaded contexts, incompatible frequency/weekday sets, different entry sets and rollback.
  Assert one coherent winning scalar/child configuration, not only a returned status.
- Make the existing FinancialTransaction contention test contract equally explicit about observed lock waiting,
  preloaded contexts, committed balance/child state and rollback. Subscription unique-rule tests must drive callers
  past application pre-checks into database competition rather than relying only on simultaneous starts.

## Other reviewed dimensions

- Ownership/dependencies: eight Finance tables, diary_entries, personal_profiles match DBML/Flyway; matrix allows
  Finance -> Reference, Journal -> none, Personal -> Reference + Location. Public catalog/address contracts exist.
  No Vault-backed identity or recycle behavior is proposed.
- Schema/domain: money scales/precision, exchange rate positivity, sign/shape, partial self uniqueness, Subscription
  unique rule link/custom cycle and Markdown/date rules are compatible. Include recurring description's varchar(1000)
  limit explicitly during remediation; the source schema remains authoritative.
- API/package: semantic Finance capability packages and internal persistence direction are appropriate; Journal/
  Personal package names may be refined in the later handoff. Existing descriptor/placeholder state is appropriate
  before implementation; package-info and placeholder removal are required at implementation, not prematurely.
- Tooling: new phase-specific domain skill/rule/scoped AGENTS route distinct ledger/lifecycle risks; canonical README
  is retained. Existing implementer/auditor routing and safety hook suffice. No new agent/hook/POM/schema change.
- Security/testing: privacy-safe conflicts and module boundaries are required; decimal/rollback/self-profile tests
  and all 685 regressions are retained. Remaining concurrency clarity is P11-1/P11-2. No full Maven run required for
  this docs/tooling-only gate; no new production/test code is present in the preparation diff.
- Scope/non-goals: live recurrence scheduler, auto-charging, Search/REST/frontend/calendar aggregation/hard delete
  and generic frameworks are explicitly deferred. No speculative extension point is needed.
- Status/links: Phase 10 archive and Phase 11 docs/tooling links inspected; review/status docs synchronized to this
  outcome. ACTIVE remains NO_ACTIVE_HANDOFF.
- Static diagnostics: no new compiler/IDE inspection executed. Prior Phase 10 accepted notices are not grounds
  to reopen frozen code. No IDE-clean/warning-free claim is made.
- Graphify queried for navigation; its existing graph did not identify the unimplemented Finance schema concepts
  reliably, so findings were verified directly in canonical DBML/Flyway/ADR-0011 and preparation files.
- `git diff --check`: rerun after review/status documentation updates; no production edits by Codex.

## Next step

Give these findings/latest package to ChatGPT for narrow Phase 11 preparation remediation and explicit policy
decisions, then rerun `$codex-pre-handoff-review`. Do not create an implementation handoff or commit the preparation
as READY FOR HANDOFF yet. No commit message is issued while these blocking findings remain.
