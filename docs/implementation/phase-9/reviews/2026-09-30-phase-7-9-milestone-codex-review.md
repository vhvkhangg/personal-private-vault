# Phase 7–9 milestone Codex review — 2026-09-30

Status: **CHANGES_REQUESTED**. Reviewed implementation commit: `b0aa742` (`HEAD` and local `origin/main` agree). Phases 7 (`account`), 8 (`knowledge`), and 9 (`collection`) remain complete/frozen. Canonical scope/status: [`../milestone-review.md`](../milestone-review.md).

## Blocking findings

### 1. High — Note frontmatter exposes managed mutable state through the public facade

**Location:** `knowledge/note/internal/domain/Note.java:81,102`, `knowledge/note/internal/application/NoteService.java:226`, `knowledge/note/view/NoteView.java:17`, and `knowledge/api/KnowledgeNoteView.java:17`, under `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.

The Note entity retains the command's `Map<String, Object>` directly, and both nested and parent views return that same map. Record syntax does not make its map or nested lists/maps immutable. A top-level caller inside a normal write transaction can call `KnowledgeOperations.findNoteById`, mutate `view.frontmatter()`, and commit a Note change through Hibernate dirty checking without calling any Note update operation. Mutating a caller-owned create-command map also changes the returned view and the managed entity. This violates the approved immutable public boundary and is directly relevant to future synchronous Feed/ImportData callers.

**Observed reproduction:** an isolated PostgreSQL 18.6 diagnostic created a Note, opened a `TransactionTemplate`, read it through the parent API, called `frontmatter().put("bypassedUpdate", true)`, and committed. A fresh SQL lookup returned `true`. In a separate create transaction, changing the original command map from `state=before` to `state=after` immediately changed the returned parent view to `after`.

**Required correction:** give Note command/entity/view boundaries defensive JSON snapshot semantics, including nested maps/lists. Public snapshots must be immutable or otherwise defensively isolated on access; caller-owned input must not remain aliased to managed persistence state. Preserve arbitrary unknown JSON keys, JSON nulls, arrays, numbers, and unchanged Markdown. Keep the policy in the appropriate Note/API owner without a generic cross-module base. Add PostgreSQL regressions through the parent and nested APIs for mutation of source maps/nested collections and returned views inside an enclosing write transaction, proving no unintended persisted change.

**Classification:** owner-approved maintenance implementation slice touching frozen Phase 8, with tests/evidence.

### 2. High — a preloaded Vocabulary item produces stale review history despite the row lock

**Location:** `knowledge/vocabulary/internal/application/VocabularyService.java:201–205` and `knowledge/vocabulary/internal/infrastructure/persistence/VocabularyItemRepository.java:18–20`.

`findByIdForUpdate` acquires a database lock, but Hibernate can reuse an item already loaded into the enclosing transaction's persistence context. The service then reads `previousIntervalDays` and `previousEaseFactor` from that stale managed instance. The approved transition must record the actual current state protected by the lock. The existing contention regression starts with a fresh persistence context and therefore misses this case.

**Observed reproduction:** create an item with interval `0`, ease `2.50`; transaction A reads it through `KnowledgeOperations.findVocabularyItemById`; worker B completes and commits a parent-API review to interval `10`, ease `2.70`; A then calls the parent review operation to interval `20`, ease `2.90`. A's history row records previous interval `0` and ease `2.50`, rather than `10` and `2.70`. All calls use public parent contracts, and worker completion is explicitly awaited before A's review.

**Required correction:** acquire the item lock and obtain fresh state under that lock even when the entity is already managed, while preserving the enclosing transaction's atomicity. Do not introduce an independently committing transition to bypass the caller's transaction. Add a deterministic PostgreSQL regression for the preloaded-context sequence and assert the previous/new history chain, returned state, and final item state. Retain the existing real-contention and rollback coverage.

**Classification:** owner-approved maintenance implementation slice touching frozen Phase 8, with tests/evidence.

### 3. Medium — follower snapshot insertion still performs one unnecessary SELECT per entry

**Location:** `account/internal/application/FollowerSnapshotService.java:131–144`, `account/internal/domain/FollowerSnapshotEntry.java:20`, and `backend/src/test/java/com/vhvkhangg/personalprivatevault/account/AccountIntegrationTest.java:744–762`.

Bulk target validation and grouped header counts are correctly implemented. However, each new snapshot entry has an assigned composite ID and no new-entity marker; `JpaRepository.save` follows the merge path and probes the entry table before inserting. The freshly allocated snapshot header guarantees that these entries are new. Thus an N-entry historical capture still makes N avoidable entry-existence SELECTs in its transaction, beyond the necessary inserts. This is a concrete batch-path cost, not a speculative optimization.

**Observed evidence:** the fresh full-build Surefire report's `followerSnapshotBulkExistenceValidationAndGroupedCountReads` case logs three individual `SELECT ... FROM follower_snapshot_entries ... (snapshot_id,target_account_id) ...` statements before the three inserts. The regression counts only `external_accounts` SELECTs during creation, so it passes while these probes remain.

**Required correction:** persist newly allocated snapshot entries as known-new rows using a focused owner-local approach; avoid merge existence probes while preserving all-or-nothing creation, historical copies, duplicate collapse/conflict rejection, and the existing bulk validation/grouped count behavior. Extend SQL/query-shape evidence to include entry-table SELECTs across multiple entries and batch sizes. JDBC batching or a new bulk framework is not required.

**Classification:** owner-approved maintenance implementation slice touching frozen Phase 7, with tests/evidence.

## Independent verification

- `mvn -f backend/pom.xml -ntp clean verify` on Java 25.0.2/Maven 3.9.15: **BUILD SUCCESS; 583 tests, 0 failures, 0 errors, 0 skipped**. Surefire test-case elements independently match these counts. PostgreSQL 18.6 Testcontainers, Flyway/Hibernate fidelity, global and nested Spring Modulith verification, privacy-conflict tests, and the existing contention tests passed.
- Ran an additional diagnostic against disposable PostgreSQL 18.6 using the same explicit Flyway migration setup as repository tests. It used only parent Knowledge APIs plus an enclosing transaction and independent worker for the two Knowledge findings. The successful run emitted:

  ```text
  noteReadMutationPersisted=true
  commandInputAliased=true
  cachedReviewPreviousInterval=0 expected=10
  cachedReviewPreviousEase=2.50 expected=2.70
  ```

  Diagnostic source/classes are local ignored artifacts under `backend/target/milestone-diagnostics/`; they are not production changes or accepted regression tests. The reproduction sequences above preserve the evidence when that build directory is cleaned.
- `git diff --check` passed. No implementation, migration, module descriptor, agent, or hook was modified by this review. Existing owner closeout/status changes were preserved.
- Existing Lombok/JDK `Unsafe`, Mockito agent, and deprecated test-support diagnostics remain non-blocking. No IDE inspection was run; this is not a warning-free claim.

## Cross-phase assessment

- **Ownership/dependencies:** Account uses public Vault/Reference contracts; Study adds public Account/People validation; the Knowledge and Collection parents expose their own named `api` types and delegate through nested public capabilities. Architecture verification remains green, and no internal persistence imports, new cycles, or bypass of the parent integration boundary was found. The Note map alias is the concrete remaining encapsulation defect.
- **Domain/schema consistency:** the eight Vault-backed entity paths preserve shared identity/rollback; Account external-ID and Note/Study uniqueness stay database-arbitrated; relationship writes preserve creation provenance and coherent mutable fields; Shopping defaults and purchase/price rules, Collection scalar replacement and set semantics, and the Vocabulary due predicate/scaled numeric checks agree with their approved contracts.
- **Integrity/concurrency:** snapshots retain historical copies and are immutable after batch creation; Collection assignments use atomic `ON CONFLICT DO NOTHING`; expected conflict translation and captured-log privacy regressions pass. Vocabulary's already-managed state case above remains an integrity blocker.
- **Performance:** collection reads are scoped and bounded with deterministic ordering. Account clamps limits, while the approved Knowledge/Collection reads reject non-positive limits and apply the caller's explicit bound. Recent snapshot counts are grouped; no unnecessary eager associations or load-all public reads were found. Snapshot merge probes are the concrete remaining batch hotspot. A maximum limit or automatic SRS algorithm is not requested.
- **Design/maintainability:** both parent facades are thin mappings rather than duplicate business implementations. Domain validators and persistence remain owner-local. Repeated small normalization helpers do not justify a new common utility/base service, and no speculative Strategy/Factory/event hierarchy is needed for this maintenance.
- **Security/privacy:** this review found no new HTTP exposure or authentication dependency expansion in Phases 7–9. The authentication regressions and private-marker/raw-vendor-detail conflict assertions pass; SQL/bind payload logging is not newly enabled by these phases. Note's mutable persistence alias requires correction even though it is not an authentication bypass.
- **Tests/tooling:** existing concurrency tests observe PostgreSQL locks rather than relying solely on sleeps. The missing coverage is specifically managed-JSON aliasing, preloaded Vocabulary state, and entry merge-query growth. Graphify supplied navigation context; findings were verified in canonical source and runtime behavior.
- **Repository/docs:** frozen topology and semantic named interfaces match the repository-tree policy; implemented module placeholders are gone. Phase 9 owner commit/push is reflected in closeout documents, and no next-phase implementation handoff was created.

## Non-blocking observations

1. **Low — archived Phase 8 delivery summary misstates two rules.** `docs/implementation/phase-8/handoff.md:113,122` claims a `TRACKED` ownership requirement for YouTube Study accounts and ease factor `>= 1.30`. The approved contract/implementation requires account type + YouTube platform regardless of ownership, and Schema v1 requires positive ease after two-decimal scaling. Correct the delivery-summary wording during documentation synchronization; do not change correct production behavior to match it.
2. **Low — Collection internal packages lack package documentation.** Its ten populated `internal/application`, nested `internal/application`, `internal/domain`, and `internal/infrastructure/persistence` packages do not contain `package-info.java`, unlike the corresponding Account/Knowledge layout. This does not break Modulith encapsulation; a narrow package-hygiene follow-up can align them with repository guidance. The parent descriptor's future Search comment is also historical documentation debt, not implemented Search scope.

## Gate and remediation boundary

The milestone is **CHANGES_REQUESTED** and Phase 10 pre-handoff review remains blocked. All three blockers require an owner-approved maintenance implementation slice; none authorizes reopening the completed phase handoffs. A combined narrow scope may address Note metadata isolation, fresh locked Vocabulary state, and known-new snapshot entry persistence with targeted tests/evidence, leaving Schema v1 and module ownership unchanged.

**Next step:** owner approves that narrow maintenance scope, then gives this review/latest package to ChatGPT to prepare it. Follow the maintenance handoff → Antigravity implementation → Codex final review → owner commit/push workflow, then rerun `$codex-milestone-review`.
