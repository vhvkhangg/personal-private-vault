# Codex final acceptance review

Date: 2026-09-30

Result: **READY FOR OWNER COMMIT**

Handoff: `maintenance-milestone-7-9-integrity-and-query-shape`.
Scope: approved frozen Phase 7 Account / Phase 8 Knowledge maintenance only.

## Findings and acceptance

No blocking findings remain. The previous review records are retained as history; this is the current gate.

- Numeric validation now accepts exact trusted immutable classes, not mutable BigDecimal/BigInteger subclasses.
  The added PostgreSQL regression rejects both subclass types through direct snapshots, nested and parent
  create/update command boundaries, and enclosing write-transaction create attempts; SQL confirms no notes written.
  Ordinary BigDecimal/BigInteger create/update and reload fidelity remains covered.
- Atomic numeric normalization, recursive map/list isolation, stable cycle/key/unsupported-leaf validation,
  JSON null support and existing Markdown/hash/privacy regressions remain green. The canonical policy is
  Note-owned in its legal named interface; DTOs expose no persistence/helper type in their signatures.
- Targeted Vocabulary refresh obtains database state under the held row lock in the existing caller transaction.
  Parent API A-preload/B-commit/A-review history, contention and rollback regressions pass.
- Snapshot entries use focused Persistable lifecycle handling. Sizes 1/3/5 retain two Account validation SELECTs,
  zero entry-table SELECTs on insertion, historical data and two grouped-count read statements.
- No schema/migration, module ownership, public operation signature, deferred feature or unrelated production change.
  Architecture/schema tests remain green. Helper occupies an existing documented named-interface package.
- Responsibilities and reuse are proportionate; no generic JSON/persistence framework, new transaction boundary,
  blanket suppression, or speculative pattern was introduced. No additional concrete performance blocker found.

## Verification

- Independent `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS;
  **594 tests, 0 failures, 0 errors, 0 skipped**, elapsed **01:04 min**;
  finished **2026-09-30 16:15:16 +07:00**.
- `git diff --check`: exit 0 before and after final review/status documentation updates.
- Implementer evidence records focused 71 passing tests and final 594 passing tests, versions and query shape.
  Executed testcase aggregation is the relevant nested-suite counting convention, as clarified in the prior review.
- Observed diagnostics: Lombok Unsafe terminal-deprecation warning and test-support deprecated-API notice.
  No IDE inspection or warning-free assertion; no warning suppression requested in this scope.
- No production edits, commits, pushes, tags or PR actions performed by Codex. Existing owner/implementer work preserved.

Reuse-and-consistency review guided centralizing the previously divergent snapshot rules within the owner;
no repository-wide abstraction was required.

## Owner gate

Suggested commit message:

`fix: isolate note snapshots and preserve locked review integrity`

Owner commit/push is pending. Maintenance is not yet frozen/closed. The Phase 7–9 milestone remains
`CHANGES_REQUESTED`, and Phase 10 remains blocked until its required milestone and synchronization gates.

**Next step:** Owner commits/pushes, then runs `$codex-milestone-review` for Phases 7–9.
