# ImportData Module Agent Instructions

Applies recursively to `com.vhvkhangg.personalprivatevault.importdata`.

## Ownership

ImportData owns:

- `import_jobs`
- `import_job_items`

ImportData owns orchestration/job state, not Study/Information/Vocabulary/Note persistence.

## Dependencies

Allowed top-level dependencies:

- `vault`
- `knowledge`

Target reads/writes use the parent Knowledge API only. Never import nested Knowledge packages/internals/repositories.

## Invariants

- target/format matrix follows Phase 10 preparation;
- parse/preview/validate creates no final Knowledge record;
- structurally parseable items are provisional `VALID`; malformed items are `INVALID`;
- validation may classify Note imported-hash duplicates only through the frozen parent API;
- no invented Study/Information/Vocabulary natural-key uniqueness;
- execution decisions are explicit and whole-job transactional;
- parse/validate/execute/cancel share the same owner-local job-row serialization guard;
- after acquiring the guard, refresh/re-read authoritative job status before checking the transition, including
  already-managed entity cases;
- hold the guard through the complete transition transaction; incompatible/repeated callers reject without
  target writes or terminal-state overwrite;
- required contention regressions cover execute-vs-execute, execute-vs-cancel in both winner orders,
  parse-vs-cancel, and validate-vs-cancel using observable PostgreSQL lock waiting rather than sleeps;
- one target failure rolls back all selected target writes and execution-state changes;
- target business validation remains Knowledge-owned;
- `imported_items` counts IMPORTED + UPDATED;
- cancellation performs no target writes;
- Markdown raw text is preserved exactly; YAML parsing is safe and unknown frontmatter keys survive;
- parsed payload JSON is deeply isolated from managed/public state;
- raw payloads/hashes/frontmatter are never logged;
- job/item reads are positive-bounded and deterministic.

## Guidance

Use:

- `feed-import-workflow-modeling`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `reuse-and-consistency`
- `backend-testing`

## Phase gate

Phase 10 production changes require:

1. Phase 10 preparation review = `READY FOR HANDOFF`;
2. an active approved Phase 10 handoff.
