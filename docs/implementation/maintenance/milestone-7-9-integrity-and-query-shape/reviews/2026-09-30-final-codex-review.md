# Codex final review — milestone integrity/query-shape maintenance

Date: 2026-09-30

Result: **CHANGES_REQUESTED**

Scope: `maintenance-milestone-7-9-integrity-and-query-shape`, owner approval 2026-09-30.
No production fixes, schema changes, commits, or publishing performed by this review.

## Blocking findings

### 1. High — numeric leaves remain aliased across Note snapshots

`NoteFrontmatterSnapshot.deepCopyValue` and `toUnmodifiableValue` return every `Number` unchanged.
The four new command/view copier implementations do the same. `Number` does not imply immutability:
`AtomicInteger`, `AtomicLong`, and custom subclasses can be mutable.

An isolated compiled-class diagnostic supplied `AtomicInteger(1)`, constructed a persistence copy and `NoteView`,
then changed the original to 2. Both copies read 2. Changing the number obtained from the view to 3 also changed
the persistence copy to 3. This directly disproves the required detached entity graph and immutable public snapshot.
Map/list wrappers do not protect these leaves. The diagnostic proves Java aliasing, not a PostgreSQL commit replay.

Required correction: define supported immutable JSON numeric representations deliberately. Normalize mutable numeric
inputs without precision loss, or reject unsupported numeric implementations through `InvalidNoteException`; never
retain a mutable leaf. Preserve supported integral/decimal fidelity. Add parent/nested API create/update and view
regressions inside enclosing write transactions, followed by commit and fresh PostgreSQL reload (or stable rejection
with no writes if that representation is unsupported).

### 2. Medium — duplicated snapshot policies diverge and invalid graphs bypass Note validation

`CreateNoteCommand`, `UpdateNoteCommand`, `NoteView`, and `KnowledgeNoteView` each contain the same recursive copier,
alongside two more traversals in `NoteFrontmatterSnapshot`. The records return unsupported objects unchanged;
the internal helper rejects them. Record nested-key casts produce `ClassCastException` for a non-string key,
and cyclic input recurses until `StackOverflowError` before reaching the existing validation boundary.
Both exception paths were reproduced with synthetic compiled-class diagnostics.

Required correction: centralize this concrete policy in one small Note/Knowledge-owned implementation available
through legal public/named-interface boundaries where needed; do not import internals into the parent API or create
a repository-wide JSON framework. Validate unsupported graphs consistently using stable `InvalidNoteException`
without payload leakage. Cover non-string/null keys, unsupported leaves and cycles, including command construction
and parent/nested operations. Preserve nulls, valid nested structures, Markdown and numeric fidelity.

### 3. Medium — required regression path and recorded evidence are incomplete/inaccurate

`KnowledgeIntegrationTest.reviewTransitionRefreshesStalePreloadedStateUnderLock` preloads and reviews through
`VocabularyOperations`, not the explicitly required `KnowledgeOperations` parent path. Its transaction sequencing
and observed history assertions are sound, but do not exercise the complete specified contract.

`test-evidence.md` has incorrect aggregated suite counts: AccountValidation 23 vs actual 21; KnowledgeValidation
27 vs 26; LocationValidation 32 vs 26; MediaValidation 23 vs 22; CollectionValidation 28 vs 16; CollectionIntegration
13 vs 15. It omits the Knowledge/Collection architecture suites (3 each). The three focused class counts total
64, not the claimed 65. The active implementation summary also claims Jackson round-trip validation, whereas
the change to `NoteService.validateNote` is a recursive copy. Required warnings/limitations are not recorded.

Required correction: exercise the exact A-preload/B-commit/A-review sequence through parent Knowledge commands/APIs;
retain contention/rollback coverage. Regenerate evidence from actual reports, aggregate nested suites correctly,
rerun focused and final commands after remediation, and record warnings/limitations and accurate implementation facts.

## Accepted aspects and verification

- Targeted `EntityManager.refresh(item)` follows the held row lock in the existing transaction; no global clear,
  new transaction, schema change or cross-module persistence leakage was introduced.
- Snapshot `Persistable` new-state lifecycle is focused; query-shape regression proves sizes 1/3/5 have two
  Account SELECTs and zero entry-table SELECTs, with persisted historical data and two grouped-read statements.
- Map/list Note isolation and supported ordinary JSON fidelity regressions pass.
- Independent `mvn -f backend/pom.xml -ntp clean verify`: exit 0, BUILD SUCCESS, 588 tests,
  failures/errors/skips 0, 59.752 seconds. Architecture/schema tests remain green.
- `git diff --check`: exit 0 before review-document updates; repeated after recording review.
- Build diagnostics observed: Lombok `sun.misc.Unsafe` terminal deprecation and deprecated API use in
  `AbstractPostgresIntegrationTest`. No IDE inspection or warning-free claim.
- Isolated diagnostic lives only in ignored `backend/target/final-review-diagnostics/NoteSnapshotDiagnostic.java`;
  not an accepted regression or tracked production artifact. No sensitive payloads used.

The reuse-and-consistency review identified concrete divergent copies, not a request for speculative abstraction.
Other milestone observations remain excluded. Phase 10 stays blocked and milestone status stays `CHANGES_REQUESTED`.

## Next gate

Antigravity `/antigravity-implement-handoff` must remediate the active handoff, then Codex `$codex-final-review`.
No commit message is provided while blockers remain.
