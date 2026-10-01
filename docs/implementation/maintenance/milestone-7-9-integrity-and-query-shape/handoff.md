# Active Implementation Handoff

- Handoff ID: `maintenance-milestone-7-9-integrity-and-query-shape`
- Created by: Codex
- Status: `COMPLETE_FROZEN`
- Owner commit/push: completed 2026-09-30
- Implementer: Antigravity
- Final reviewer: Codex
- Scope: owner-approved frozen Phase 7 Account / Phase 8 Knowledge maintenance

## Goal

Resolve exactly the three approved milestone blockers: Note JSON snapshot isolation, fresh locked Vocabulary state,
and follower-snapshot insertion without per-entry merge probes. Phase 7–9 milestone status remains
`CHANGES_REQUESTED`; Phase 10 pre-handoff review remains blocked.

## Sources of truth

- `docs/implementation/maintenance/milestone-7-9-integrity-and-query-shape/README.md` — current scope authority,
  owner approval dated 2026-09-30, permitted targets, required behavior and regression contract.
- `docs/implementation/phase-9/reviews/2026-09-30-phase-7-9-milestone-codex-review.md` and
  `docs/implementation/phase-9/milestone-review.md` — observed defects/reproductions and milestone gate.
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml` and
  `backend/src/main/resources/db/migration/V1__create_schema_v1.sql` — unchanged schema.
- `docs/architecture/module-dependency-matrix.md`, `docs/architecture/module-boundaries.md`, and
  `docs/repository/repository-package-tree.md` — frozen ownership/public boundaries.
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/account/AGENTS.md` and
  `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/AGENTS.md` — scoped conventions.

## Implementation targets

Production paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.

- Note: `knowledge/note/internal/domain/Note.java`, `knowledge/note/internal/application/NoteService.java`,
  `knowledge/note/view/NoteView.java`, and `knowledge/api/KnowledgeNoteView.java`. A small owner-local JSON snapshot
  helper is allowed; change `knowledge/internal/application/KnowledgeFacadeService.java` only for necessary
  defensive mapping. Existing Note create/update command records may gain defensive construction as needed for
  the approved boundary, with unchanged components/signatures.
- Vocabulary: `knowledge/vocabulary/internal/application/VocabularyService.java` and, only if needed,
  `knowledge/vocabulary/internal/infrastructure/persistence/VocabularyItemRepository.java`.
- Account: `account/internal/domain/FollowerSnapshotEntry.java` and, only as needed,
  `account/internal/application/FollowerSnapshotService.java` and
  `account/internal/infrastructure/persistence/FollowerSnapshotEntryRepository.java`.
- Focused tests in the existing Knowledge/Account test packages; evidence in the maintenance path below.

## Required behavior / invariants

- **Note:** deep-isolate caller input, entity state, nested Note views, and parent Knowledge views. Root/nested
  mutations must not change managed state or alias returned snapshots. Preserve unknown keys, JSON nulls, nested
  objects/arrays, strings, booleans, integral/decimal numbers, and exact Markdown. Shallow copy-only solutions are
  insufficient. Unsupported non-JSON graphs fail through the existing stable validation boundary without payload
  logging. Use a recursively immutable public snapshot or equivalent defensive isolation. Keep policy Note/Knowledge-
  owned; the parent must not import a nested `internal` helper or leak persistence/helper types.
- **Vocabulary:** acquire/hold the item write lock and obtain fresh database state even when already managed;
  record that state's previous interval/ease, append history and apply the requested state in the caller's existing
  transaction. Prefer a targeted `EntityManager.refresh` under the lock. Preserve unrelated pending work and any
  necessary flush semantics; retain atomicity, lock-contention serialization and rollback behavior.
- **Snapshots:** persist entries for the freshly allocated header as known-new rows, with zero entry-table
  existence SELECTs during insertion. Prefer the existing `Persistable` new-state pattern with `@PostPersist`/
  `@PostLoad`, or a focused owner-local persist-new path. Preserve all-or-nothing creation, historical copies,
  duplicate collapse/conflict rejection, bulk target validation, immutable snapshots and grouped count reads.
- Preserve Vault identity, privacy-safe conflicts, public API shapes/named interfaces, and all current domain rules.
  Stop/report a conflict with the approved scope or frozen baseline.

## Non-goals

No Phase 10 implementation/pre-review; DBML/Flyway/constraint/ownership or public API signature changes; SRS formula/
due-query changes; Note Markdown/hash changes; relationship/snapshot-policy/read-limit changes; `REQUIRES_NEW`,
whole-session detach/clear, retry/event/AOP/bulk frameworks, generic JSON/persistence bases, new agents/hooks, or
unrelated refactoring. Archived Phase 8 wording and Collection package-documentation observations are excluded.

## Test/evidence contract

- PostgreSQL Testcontainers only. For Note **create and update**, mutate original root/nested maps/lists after the
  operation and assert unchanged returned/persisted data. Prove read isolation through both `NoteOperations` and
  parent `KnowledgeOperations` inside enclosing write transactions through commit and fresh SQL/API reload.
  Mutation may throw or affect only isolated data; it must not persist. Cover JSON null/nested-array/numeric fidelity
  and retain Markdown/hash/privacy regressions.
- Deterministic parent-API Vocabulary regression: A preloads `0`/`2.50`; independent B reviews to `10`/`2.70` and its
  commit is explicitly awaited; A reviews to `20`/`2.90`. Assert A's previous history values are `10`/`2.70`, committed
  history forms B→A's chain, and returned/final state is `20`/`2.90`. Retain real PostgreSQL contention and rollback
  coverage; do not substitute sleeps or independently committing transitions for caller atomicity.
- Snapshot SQL-shape tests cover multiple non-zero batch sizes: retain expected owner + bulk-target Account
  lookups; assert **zero SELECTs referencing `follower_snapshot_entries` during fresh creation**; verify inserted
  rows/historical values and retain grouped-count read assertions. Do not hide SQL logging to pass counts.
- Final commands: `mvn -f backend/pom.xml -ntp clean verify` on Java 25, then `git diff --check`.
- Evidence: `docs/implementation/maintenance/milestone-7-9-integrity-and-query-shape/test-evidence.md`. Record exact
  commands/exits, versions/environment, actual test names/counts, batch sizes/query counts, retained contention/
  rollback/privacy results, Spring Modulith/Flyway/Hibernate results, and known diagnostics. Baseline: 583 passing
  tests; record actual new totals. Do not claim IDE inspection unless run.
- Acceptance: all three blockers resolved with these regressions, final checks green, unchanged schema/boundaries,
  and no scope expansion. Keep milestone `CHANGES_REQUESTED` and Phase 10 blocked pending its separate re-review.

## Constraints / risks

- Relevant skills: `knowledge-domain-modeling`, `account-domain-modeling`, `jpa-postgresql-persistence`,
  `backend-testing`, `java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`,
  `design-pattern-selection`, and `modular-monolith-architecture`.
- JSON null/numeric fidelity, permitted nested named-interface access, managed-state freshness and assigned-ID
  new detection are the concrete risks. Build durable regressions from canonical reproductions; ignored
  `backend/target/milestone-diagnostics/` artifacts are not implementation/test sources.
- Formal final reviews belong in `docs/implementation/maintenance/milestone-7-9-integrity-and-query-shape/reviews/`.
  Agents never commit/push/tag or create/merge PRs. After final acceptance, owner commit/push precedes the Phase 7–9
  `$codex-milestone-review` rerun; owner-committed `MILESTONE_READY` status and ChatGPT post-milestone synchronization
  precede Phase 10 pre-handoff review.

## Implementation result

- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- **Implementer:** Antigravity
- **Date:** 2026-09-30

### Summary of Completed Scope:
1. **Finding A (Note Frontmatter JSON Snapshot Isolation & Fidelity):**
   - Implemented `NoteFrontmatterSnapshot` in the legal `knowledge.note.note` named interface:
     - `deepCopy(source)`: produces a fully mutable, detached deep copy preserving JSON nulls and nested structures for JPA entity storage.
     - `toUnmodifiableSnapshot(source)`: produces a recursively unmodifiable view wrapping maps with `Collections.unmodifiableMap` and lists with `Collections.unmodifiableList`, safely supporting JSON nulls and preventing dirty checking modification (`UnsupportedOperationException`).
   - Updated `Note` entity constructor and `update()` method to deep-copy incoming frontmatter, and `getFrontmatter()` to expose only unmodifiable snapshots.
   - Updated `CreateNoteCommand`, `UpdateNoteCommand`, `NoteView`, and `KnowledgeNoteView` compact constructors with recursive snapshot isolation without cross-module internal package imports.
   - Updated `NoteService.validateNote` to deep-copy and validate frontmatter through the canonical recursive policy.
2. **Finding B (Vocabulary Fresh State Under Row Lock):**
   - Injected `EntityManager` into `VocabularyService`.
   - In `VocabularyService.reviewTransition`, called `entityManager.refresh(item)` immediately following `vocabularyItemRepository.findByIdForUpdate(id)`, ensuring the managed entity refreshes in-memory state from PostgreSQL under the held `PESSIMISTIC_WRITE` row lock before capturing `previousIntervalDays` and `previousEaseFactor`.
   - Updated `KnowledgeValidationTest.java` mock setup with mocked `EntityManager`.
3. **Finding C (Follower Snapshot Known-New Entry Persistence):**
   - Updated `FollowerSnapshotEntry` to implement `Persistable<FollowerSnapshotEntryId>`, adding `@Transient private boolean isNew = true;`, `isNew()`, `getId()`, and `@PostPersist`/`@PostLoad` lifecycle callbacks.
   - Updated `FollowerSnapshotService.createSnapshot` to map directly to a list of entries and invoke `followerSnapshotEntryRepository.saveAll(entries)` and `flush()`.

### Verification & Evidence:
- **Test Evidence:** Recorded in `docs/implementation/maintenance/milestone-7-9-integrity-and-query-shape/test-evidence.md`.
- **Focused Regressions:** `mvn -f backend/pom.xml -ntp test "-Dtest=AccountIntegrationTest,KnowledgeIntegrationTest,KnowledgeValidationTest"`: 71 tests run, 0 failures, 0 errors (43.715 s).
- **Full Verification:** `mvn -f backend/pom.xml -ntp clean verify`: 594 tests run (from baseline 583), 0 failures, 0 errors, 0 skipped (01:15 min).
- **Git Diff:** `git diff --check`: 0 (clean).
- **Graphify:** Synchronized via `scripts/refresh-graphify.ps1` (3361 nodes, 11522 edges, 300 communities).
- **Schema & Boundaries:** Zero changes to Flyway migrations or DBML; all 12 Spring Modulith architecture tests passed.

## Codex remediation

Latest review: `reviews/2026-09-30-final-codex-acceptance-review.md`.

Remediated findings:
1. **Finding 1 (High - Numeric leaf aliasing & mutable subclass rejection):**
   - Implemented exact class equality matching in `NoteFrontmatterSnapshot.validateAndNormalizeNumber` (`clazz == BigDecimal.class`, `clazz == BigInteger.class`, etc.) instead of open `instanceof` checks.
   - Normalized mutable numeric leaves without precision loss: `AtomicInteger` $\to$ `Integer`, `AtomicLong` $\to$ `Long`, `AtomicBoolean` $\to$ `Boolean`.
   - Mutable subclasses of `BigDecimal` and `BigInteger` (`MutableBigDecimal`, `MutableBigInteger`) are rejected with stable `InvalidNoteException("Unsupported numeric type in note frontmatter")` across:
     - `NoteFrontmatterSnapshot.deepCopy` and `NoteFrontmatterSnapshot.toUnmodifiableSnapshot`
     - Nested `CreateNoteCommand` and `UpdateNoteCommand`
     - Parent `CreateKnowledgeNoteCommand` and `UpdateKnowledgeNoteCommand`
   - Verified stable no-write rejection under active write transactions (`TransactionTemplate` with `PROPAGATION_REQUIRES_NEW`), proving 0 records written to PostgreSQL (`SELECT count(*) FROM notes` = 0).
   - Preserved full fidelity of authentic `BigDecimal` and `BigInteger` instances across creation, update, and reload on both nested `NoteOperations` and parent `KnowledgeOperations`.
2. **Finding 2 (Medium - Divergent snapshot policies & invalid graph bypasses):**
   - Centralized canonical snapshot policy in `com.vhvkhangg.personalprivatevault.knowledge.note.note.NoteFrontmatterSnapshot` in legal public named interface `note`.
   - Removed duplicated snapshot copiers from `CreateNoteCommand`, `UpdateNoteCommand`, `NoteView`, `CreateKnowledgeNoteCommand`, `UpdateKnowledgeNoteCommand`, and `KnowledgeNoteView`, delegating to `NoteFrontmatterSnapshot.toUnmodifiableSnapshot(frontmatter)`.
   - Added cycle detection using an `IdentityHashMap` visited set throwing `InvalidNoteException("Cyclic reference detected in note frontmatter")`.
   - Validated map keys (rejecting null and non-string keys) and rejected arbitrary unsupported leaf types with `InvalidNoteException("Unsupported value type in note frontmatter")`.
3. **Finding 3 (Medium - Required regression path & test evidence):**
   - Refactored `KnowledgeIntegrationTest.reviewTransitionRefreshesStalePreloadedStateUnderLock` to execute entirely through parent `KnowledgeOperations` (`createVocabularyItem`, `findVocabularyItemById`, `reviewVocabularyItem`, and `findVocabularyReviews`), retaining all concurrent contention and rollback tests.
   - Accurately regenerated `test-evidence.md` with exact counts from Surefire reports, aggregating nested test suites properly (including `KnowledgeArchitectureTests` [3] and `CollectionArchitectureTests` [3], 594 total).
   - Documented build diagnostics: Lombok `sun.misc.Unsafe` warning under JDK 25, deprecation warning in `AbstractPostgresIntegrationTest`, and explicit note that no IDE inspections were run.

## Final review

2026-09-30 final acceptance: **READY FOR OWNER COMMIT**. All prior blockers are resolved.
Independent `mvn -f backend/pom.xml -ntp clean verify` passed 594 tests, failures/errors/skips 0; `git diff --check` passed.

Commit message: `fix: isolate note snapshots and preserve locked review integrity`

Owner commit/push is next, followed by `$codex-milestone-review` for Phases 7–9. Milestone remains
`CHANGES_REQUESTED` and Phase 10 remains blocked. No agent commit/push performed.
