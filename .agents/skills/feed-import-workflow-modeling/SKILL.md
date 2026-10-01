---
name: feed-import-workflow-modeling
description: Guide Phase 10 Feed and ImportData workflows with Vault-backed saved resources, normalized feed ingestion, parent-Knowledge conversions/imports, isolated JSON snapshots, and transactional job state.
---

# Feed + Import Workflow Modeling

Use for Phase 10 planning, implementation, testing, or review.

## Boundaries

Both `feed` and `importdata` may depend only on public `vault` and parent `knowledge` contracts.

Never import nested Knowledge module packages/internals/repositories.

## JSONB isolation

Feed config/raw metadata and ImportData parsed payloads must be deeply isolated from managed state and public views.
Protect nested maps/lists and JSON nulls; reject unsupported/cyclic graphs safely without logging payloads.

Do not reuse a forbidden nested Knowledge helper across top-level module boundaries.

## Feed

- FeedSource scheduling follows frozen checks.
- Due = enabled + scheduled + (`next_fetch_at` null or <= cutoff), with explicit positive bound.
- Network adapters/scheduler triggers are deferred; normalized fetch ingestion is the Phase 10 application boundary.
- Feed item URL hash = SHA-256 of trimmed stored URL UTF-8 bytes.
- Resolve feed-item refreshes by source-scoped external ID / URL hash; reject split-key ambiguity.
- SavedResource is Vault-backed `SAVED_RESOURCE`.
- SavedResource URL-hash duplicates are stable conflicts with losing Vault rollback.
- SavedResource conversions synchronously call parent Knowledge API for Study, Information, or Note and persist
  provenance atomically.
- No Vocabulary conversion.

## ImportData

- Targets: Study, Information, Vocabulary, Note.
- Formats: CSV/JSON for all; Markdown only for Note.
- Parse never creates final Knowledge records.
- Parsed items use provisional VALID/INVALID because Schema v1 has no PARSED item status.
- Validate may classify Note imported-hash duplicates through parent Knowledge only; do not invent nested/repository
  lookups for other targets.
- Execution uses explicit IMPORT/SKIP/UPDATE decisions and one transaction for all selected target writes.
- Target business validation remains Knowledge-owned.
- Any target failure rolls back the whole execution attempt and leaves the committed job in VALIDATED.
- All same-job mutations (parse/validate/execute/cancel) share one owner-local job-row serialization guard.
- Acquire the guard first, refresh/re-read authoritative current state under it, then validate the transition;
  stale pre-lock state is never authoritative.
- Hold the guard through the whole mutation transaction. Repeated/incompatible callers reject with a stable
  ImportData transition error; they never repeat target writes or overwrite terminal status.
- Execute-vs-execute, execute-vs-cancel, parse-vs-cancel, and validate-vs-cancel require deterministic real
  PostgreSQL lock-contention regressions rather than timing-only sleeps.
- Markdown raw text is preserved exactly; YAML parsing must be safe and keep unknown frontmatter fields.

## Privacy/security

Never log raw import payloads, metadata, URLs, hashes, frontmatter, or vendor detail. Stored item error messages are
safe summaries.

Do not enable arbitrary polymorphic JSON/YAML object construction.

## Testing

Use PostgreSQL Testcontainers. Verify state machines, races, rollback, query bounds/order, JSON isolation, privacy,
Flyway/Hibernate fidelity, and exact Spring Modulith dependency boundaries.
