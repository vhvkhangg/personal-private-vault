# Phase 7–9 Milestone Integrity & Query-Shape Maintenance

Status: **READY FOR OWNER COMMIT**

Owner approval: **2026-09-30**

Implementation handoff: [`../../handoffs/ACTIVE.md`](../../handoffs/ACTIVE.md) — `READY_FOR_OWNER_COMMIT`;
handoff ID `maintenance-milestone-7-9-integrity-and-query-shape`. Owner commit/push is next, then milestone re-review.

Latest final review: [`reviews/2026-09-30-final-codex-acceptance-review.md`](reviews/2026-09-30-final-codex-acceptance-review.md).
Independent clean verification passed 594 tests with no failures/errors/skips. All blockers are resolved.
The milestone remains `CHANGES_REQUESTED` pending owner commit/push and `$codex-milestone-review`.

Source finding:
[`../../phase-9/reviews/2026-09-30-phase-7-9-milestone-codex-review.md`](../../phase-9/reviews/2026-09-30-phase-7-9-milestone-codex-review.md)

This is one narrow maintenance implementation slice for the three blocking findings from the Phase 7–9 milestone
review. It temporarily permits only the frozen Phase 7 Account and Phase 8 Knowledge changes required below.

Phase 10 remains blocked.

## Goal

Resolve exactly these blockers:

1. isolate Note frontmatter so caller-owned/public JSON structures cannot mutate managed JPA state;
2. make Vocabulary review history read fresh state under the row lock even when the item was already loaded in the
   enclosing persistence context;
3. persist newly allocated follower-snapshot entries as known-new rows so snapshot creation does not issue one
   entry-table existence `SELECT` per entry.

No other Phase 7–9 production behavior is reopened.

## Finding A — Note frontmatter managed-state isolation

### Current defect

`Note` currently retains the supplied `Map<String, Object>` reference. `NoteView` and `KnowledgeNoteView` also expose
the same mutable graph.

Because record fields containing maps/lists are not deeply immutable, a caller can mutate a returned frontmatter map
or nested collection inside a normal write transaction and Hibernate dirty checking can persist the change without
calling the Note update operation. Mutating a caller-owned create/update map can also remain aliased to the managed
entity/view.

### Required behavior

Treat Note frontmatter as JSON **snapshot data** at every command/entity/view boundary.

- caller-owned create/update input must never remain aliased to managed persistence state;
- the entity must own an isolated deep JSON graph;
- nested Note API views and parent Knowledge API views must expose a deep immutable snapshot, or an equivalently
  isolated snapshot whose mutation cannot affect entity state;
- nested maps/lists must receive the same protection as the root map;
- preserve arbitrary unknown JSON object keys and JSON-compatible values:
  - JSON null;
  - nested objects/maps;
  - arrays/lists;
  - strings;
  - booleans;
  - integral/decimal numbers;
- preserve existing Markdown content exactly; this maintenance does not normalize/rewrite Markdown or frontmatter
  values;
- do not use shallow `Map.copyOf`/`List.copyOf` as the sole solution because JSON nulls must remain supported;
- unsupported non-JSON object graphs, if encountered, should fail through the existing Note validation boundary
  rather than becoming a generic serialization framework.

The defensive-copy policy belongs to the Note/Knowledge owner. Do not create a repository-wide generic mutable-JSON
base abstraction.

### Preferred implementation direction

A focused Note-owned JSON snapshot helper is acceptable, for example with separate concepts for:

- a deep detached copy owned by persistence; and
- a recursively unmodifiable/deep-isolated public snapshot.

The exact implementation may use a small recursive copier or an existing JSON facility already in the application,
provided JSON null/nested-array/numeric fidelity is covered by tests.

Do not solve the defect by detaching the whole entity/session, disabling dirty checking globally, or changing the
public Note feature semantics.

### Likely implementation targets

Expected narrow targets include:

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/note/internal/domain/Note.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/note/internal/application/NoteService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/note/view/NoteView.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/api/KnowledgeNoteView.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/internal/application/KnowledgeFacadeService.java`
  only if parent mapping needs a separate defensive boundary;
- one small Note-owned helper if needed;
- focused Knowledge PostgreSQL tests.

Do not change Note schema, JSONB column shape, public operation names, Vault identity, imported-hash semantics, or
Knowledge module ownership.

### Mandatory PostgreSQL regressions

Use the real parent and nested APIs with PostgreSQL Testcontainers.

At minimum prove:

1. **Caller-input isolation**
   - create/update using a mutable root map containing nested map/list data;
   - mutate the original root and nested structures after the operation;
   - assert the returned view does not change through aliasing;
   - reload with SQL/API and assert persisted JSON did not change.

2. **Nested API read isolation**
   - open an enclosing write transaction;
   - read a Note through `NoteOperations`;
   - attempt mutation of root and nested frontmatter data;
   - commit the transaction;
   - fresh SQL/API reload shows no unintended persisted change.

3. **Parent API read isolation**
   - repeat the same write-transaction proof through `KnowledgeOperations.findNoteById`;
   - no frontmatter mutation may bypass `updateNote`.

4. **JSON fidelity**
   - round-trip unknown keys, JSON null, nested object/list, booleans, strings, integral and decimal numbers;
   - existing Markdown/Obsidian preservation tests remain green.

If the chosen public snapshot is recursively unmodifiable, mutation may throw an appropriate runtime exception; the
essential assertion is that managed/persisted state cannot be changed through the view.

## Finding B — Vocabulary fresh state under review lock

### Current defect

`findByIdForUpdate` acquires a PostgreSQL pessimistic row lock, but Hibernate may return an already-managed
`VocabularyItem` from the enclosing persistence context without refreshing its fields.

The review transition then records `previousIntervalDays` and `previousEaseFactor` from stale state even though the
row is now locked.

### Required behavior

The review transition must:

1. remain inside the caller's existing transaction;
2. acquire/hold the pessimistic write lock for the Vocabulary item;
3. obtain **fresh database state under that lock**, even if the entity was loaded earlier in the same persistence
   context;
4. use that refreshed state for the history row's `previous_*` fields;
5. append the history row and apply the requested new item state atomically;
6. preserve rollback behavior and the existing real-contention serialization contract.

Do not introduce `REQUIRES_NEW`, an independently committing transition, or a second transaction merely to bypass
the caller's stale persistence context.

### Preferred implementation direction

After the row lock is acquired, explicitly refresh the managed entity from PostgreSQL under the same transaction,
for example through `EntityManager.refresh(...)` while the pessimistic lock is held, or an equivalently correct
owner-local repository implementation.

Codex may select the smallest correct JPA approach, but the test must prove first-level-cache freshness rather than
only fresh-session contention.

### Likely implementation targets

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/vocabulary/internal/application/VocabularyService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/knowledge/vocabulary/internal/infrastructure/persistence/VocabularyItemRepository.java`
  only if needed;
- focused `KnowledgeIntegrationTest` coverage.

Do not change the review-transition public command/result, SRS formula policy, Vocabulary schema, due-query semantics,
or history table shape.

### Mandatory deterministic PostgreSQL regression

Reproduce the exact stale-context sequence through the parent Knowledge API:

1. create a Vocabulary item with interval `0`, ease `2.50`;
2. transaction A starts and preloads the item through `KnowledgeOperations.findVocabularyItemById`;
3. independent worker transaction B completes a review transition to interval `10`, ease `2.70` and commits;
4. explicitly await B's completed commit;
5. transaction A then invokes the parent review transition to interval `20`, ease `2.90`;
6. transaction A's appended history row must record previous interval `10` and previous ease `2.70`, not its stale
   preload;
7. assert the returned state and final committed item are interval `20`, ease `2.90`;
8. assert the committed review-history chain reflects B then A correctly.

Retain the existing PostgreSQL lock-contention test and rollback coverage. The new regression complements them; it
does not replace them.

## Finding C — Follower snapshot known-new entry persistence

### Current defect

Follower snapshot creation already performs bulk target-account validation and grouped count reads. However every new
`FollowerSnapshotEntry` has an assigned composite ID and no new-entity marker, so `JpaRepository.save(...)` uses the
merge path and performs an existence `SELECT` against `follower_snapshot_entries` before each insert.

A fresh snapshot header guarantees every submitted entry for that new snapshot is new. The per-entry probes are
therefore unnecessary and grow linearly with snapshot size.

### Required behavior

Persist freshly constructed snapshot entries through a focused **known-new** path:

- snapshot creation remains one all-or-nothing transaction;
- preserve current duplicate collapse and conflicting-historical-copy rejection;
- preserve the existing one bulk target validation query;
- preserve snapshot historical copies exactly;
- preserve grouped-count recent-snapshot reads;
- zero `SELECT` existence probes against `follower_snapshot_entries` are allowed during insertion of a fresh
  snapshot's entries;
- no generic bulk-persistence framework or JDBC-batching project is required.

### Preferred implementation direction

An owner-local solution such as Spring Data `Persistable<FollowerSnapshotEntryId>` with a transient new-state marker
and `@PostPersist`/`@PostLoad` transition is acceptable and consistent with existing repository patterns.

A focused custom repository `persistNew` path using the current `EntityManager` is also acceptable.

Do not replace the normal transaction boundary, bypass JPA validation broadly, or add a generic base entity solely
for this finding.

### Likely implementation targets

- `backend/src/main/java/com/vhvkhangg/personalprivatevault/account/internal/domain/FollowerSnapshotEntry.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/account/internal/infrastructure/persistence/FollowerSnapshotEntryRepository.java`
  only if the selected owner-local persistence path requires it;
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/account/internal/application/FollowerSnapshotService.java`
  only as needed to use the known-new path;
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/account/AccountIntegrationTest.java`.

Do not change public Account APIs, snapshot immutability, duplicate policy, relationship behavior, Account schema, or
snapshot read limits.

### Mandatory query-shape regression

Extend the existing SQL-log query-shape regression.

For fresh snapshots with multiple distinct target entries:

- continue asserting exactly the expected owner + bulk-target `external_accounts` existence lookups;
- count SQL statements that both:
  - are `SELECT`s; and
  - reference `follower_snapshot_entries`;
- assert **zero** entry-table existence `SELECT`s during snapshot creation;
- verify this for more than one non-zero batch size (for example 1, 3, and a larger batch) so the assertion proves
  query count does not grow with N;
- still assert all expected entry rows are inserted and historical values round-trip;
- retain grouped-count read assertions.

Do not satisfy the regression merely by hiding Hibernate SQL logs; the persisted rows and query-shape observation
must both remain verifiable.

## Required final verification

After focused regressions pass, run:

```text
mvn -f backend/pom.xml -ntp clean verify
```

and:

```text
git diff --check
```

Record:

- exact commands and exit status;
- Java/Maven versions;
- PostgreSQL/Testcontainers version;
- total tests/failures/errors/skips;
- focused Note alias/isolation regression names/results;
- focused preloaded Vocabulary stale-state regression names/results;
- follower-snapshot query-shape counts for each tested batch size;
- retained existing contention/rollback regressions;
- Spring Modulith architecture verification;
- Flyway/Hibernate schema validation;
- warnings/limitations without claiming IDE inspection if none occurred.

Evidence file:

`docs/implementation/maintenance/milestone-7-9-integrity-and-query-shape/test-evidence.md`

## Frozen scope and non-goals

Do **not**:

- implement or pre-review Phase 10;
- change DBML, Flyway V1, database constraints, or table ownership;
- change public Account/Knowledge API shapes unless Codex proves a minimal signature change is strictly required by
  one of these three fixes;
- change Note Markdown/imported-hash behavior or add a generic JSON framework;
- add an SRS algorithm, alter due semantics, or change Vocabulary review-transition business rules;
- change follower-snapshot duplicate/conflict semantics, relationship behavior, or read-limit policy;
- introduce `REQUIRES_NEW`, retry frameworks, generic persistence bases, bulk frameworks, events, or AOP;
- refactor Collection or unrelated Phase 7–9 code;
- add custom agents/hooks.

The milestone review's two non-blocking observations are **not** part of this implementation slice:

- archived Phase 8 handoff wording cleanup;
- Collection internal-package `package-info.java` hygiene.

They must not expand this maintenance handoff.

## Required engineering skills

Use existing skills only:

- `knowledge-domain-modeling`
- `account-domain-modeling`
- `jpa-postgresql-persistence`
- `backend-testing`
- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`
- `modular-monolith-architecture`

No new domain skill, custom agent, or hook is required.

## Workflow

1. Codex: `$codex-create-handoff` from this approved maintenance scope.
2. Antigravity: `/antigravity-implement-handoff`.
3. Codex: `$codex-final-review`.
4. Owner: commit/push only after `READY FOR OWNER COMMIT`.
5. Codex: rerun `$codex-milestone-review` for Phases 7–9.
6. Phase 10 remains blocked until the milestone is `MILESTONE_READY`, the owner commits/pushes milestone status docs,
   and ChatGPT completes post-milestone synchronization/reset and Phase 10 preparation.
