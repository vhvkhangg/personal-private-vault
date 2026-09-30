# Phase 7–9 milestone Codex re-review

Date: 2026-09-30

Result: **MILESTONE_READY**

Reviewed commit: `785dd7dced9fbf939b7e9451f48c86d225a0f157` (`785dd7d`). HEAD and the local `origin/main`
reference agree; worktree was clean before this review. No remote fetch was performed.
Window: frozen Phase 7 Account, Phase 8 Knowledge, Phase 9 Collection, plus their approved maintenance.
Canonical scope/status: [`../milestone-review.md`](../milestone-review.md).

## Closure of original blockers

The owner committed the maintenance accepted in
[`../../maintenance/milestone-7-9-integrity-and-query-shape/reviews/2026-09-30-final-codex-acceptance-review.md`](../../maintenance/milestone-7-9-integrity-and-query-shape/reviews/2026-09-30-final-codex-acceptance-review.md).

1. **Note managed-state isolation — closed.** Canonical Note-owned snapshot copying protects commands, entity state,
   nested views and parent views. Mutable atomic leaves normalize to immutable values; exact numeric class checks
   reject mutable BigDecimal/BigInteger subclasses. Nulls, nested objects/lists and ordinary numeric fidelity remain
   covered. Cycles/unsupported leaves/invalid keys use stable, payload-free validation. PostgreSQL isolation and
   no-write rejection regressions pass.
2. **Preloaded Vocabulary stale history — closed.** A targeted refresh follows the pessimistic item lock in the
   existing transaction. The parent API regression explicitly awaits B's committed 10/2.70 state before A records
   its transition to 20/2.90, and verifies the history chain. Existing contention/rollback coverage remains green.
3. **Per-entry snapshot merge probes — closed.** New composite-ID entries use focused Persistable lifecycle state.
   Batch sizes 1/3/5 prove two Account lookup SELECTs, zero entry-table SELECTs on creation, persisted historical
   copies and two grouped-read statements. Duplicate/conflict/atomicity semantics remain unchanged.

## Cross-phase assessment

- **Domain/integrity:** Vault-backed identities, atomic create/rollback, Reference/People/Account validation,
  uniqueness arbitration, full scalar replacement/defaults and Shopping purchase/price rules remain coherent.
  Vocabulary history and snapshot history retain their owner-local immutable/atomic semantics.
- **Architecture/contracts:** frozen dependency matrix and named-interface descriptors remain unchanged.
  Knowledge/Collection parents are thin synchronous mappings with parent-owned signatures; no nested repositories
  or entities leak across modules. The Note helper is exposed only through the existing legal Note named interface.
  Global and nested architecture tests pass; no cycle or new top-level dependency was introduced.
- **Performance/concurrency:** bounded scoped reads and deterministic ordering remain intact; snapshot counts are
  grouped and merge probes eliminated. Collection assignments retain atomic `ON CONFLICT DO NOTHING`, positive
  limits and stable ordering. Vocabulary lock scope remains targeted and caller transaction atomicity is preserved.
  No additional concrete N+1/repeated-I/O/unbounded-read or algorithmic blocker found in the reviewed paths.
- **Design/reuse:** canonical snapshot policy replaces divergent copies. Facades do not duplicate business rules;
  no generic persistence/JSON framework, speculative pattern, event hierarchy or global-session workaround added.
  The Collection domain skill reinforces retaining nested-owned purchase/set rules rather than broadening this maintenance.
- **Security/privacy:** no HTTP/authentication exposure or secret handling expansion in these phases; expected
  conflict privacy regressions and authentication tests pass. Synthetic test data and statement-shape assertions
  do not justify enabling production bind/payload logging. No new private-payload logging found.
- **Tests/reliability:** committed regressions exercise parent/nested snapshot isolation, preloaded-state freshness,
  real PostgreSQL contention and batch query shape. Existing schema, authentication/privacy and rollback tests pass.
- **Docs/tree/tooling:** maintenance is owner committed; this review synchronizes current milestone/status pointers.
  The helper occupies an existing named-interface package, and production/schema/module baselines have not drifted.
  Graphify supplied navigation, with facts verified in canonical source. No next-phase handoff or implementation created.

No blocking findings remain, and no new maintenance implementation is required by this review.

## Non-blocking observations retained

- **Low:** archived Phase 8 handoff summary still says YouTube Study requires TRACKED ownership and ease >=1.30;
  canonical implementation instead validates type/platform and positive two-decimal-scaled ease. Correct summary
  wording during documentation synchronization, not production behavior.
- **Low:** populated Collection internal packages still lack package-info documentation. This does not violate
  Modulith encapsulation or block Phase 10. Any production-package hygiene follow-up needs its own narrow authority.

These are the original non-blocking observations, not additions to the approved maintenance scope.

## Independent verification

- `mvn -f backend/pom.xml -ntp clean verify` on committed `785dd7d`: exit 0, BUILD SUCCESS;
  **594 tests, 0 failures, 0 errors, 0 skipped**, elapsed **01:16 min**;
  finished **2026-09-30 16:22:30 +07:00**.
- Java 25.0.2 / Maven 3.9.15; PostgreSQL 18.6 Testcontainers; Flyway/Hibernate schema fidelity and global/nested
  architecture tests pass. Frozen DBML/Flyway/dependency/ADR paths unchanged by the maintenance commit.
- `git diff --check`: exit 0 after milestone review/status documentation updates.
- Observed Lombok Unsafe terminal-deprecation and deprecated test-support API notices remain non-blocking.
  No IDE inspection or warning-free claim; no blanket suppression requested.
- Review changed documentation/status only; no production fixes, commits, pushes, tags or PR operations performed.

## Gate

**MILESTONE_READY** does not itself approve Phase 10 preparation or implementation. The owner must commit/push
these milestone review/status documents, then give the latest package to ChatGPT for post-milestone
synchronization/reset and Phase 10 preparation before `$codex-pre-handoff-review`.

**Next step:** Owner commits/pushes milestone docs, then gives the latest package to ChatGPT for that reset/preparation.
