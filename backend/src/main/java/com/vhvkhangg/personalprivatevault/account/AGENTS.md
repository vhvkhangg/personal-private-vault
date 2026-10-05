# Account Module Agent Instructions

Applies to `com.vhvkhangg.personalprivatevault.account`.

## Ownership

- `external_accounts`
- `external_account_relationships`
- `follower_snapshots`
- `follower_snapshot_entries`

## Dependencies

Whole-module architecture allows `vault` and `reference`. Phase 7 implementation must narrow the Spring Modulith
descriptor to the named interfaces actually used.

Expected contracts:

- `vault::entry`
- `vault::enums`
- `vault::view`
- `reference::catalog`
- `reference::view`

Never import another module's `internal` packages or repositories.

## Guidance

Use:

- `account-domain-modeling`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `reuse-and-consistency`
- `backend-testing`

## Invariants

- External Account ID equals its `EXTERNAL_ACCOUNT` Vault Entry ID.
- At least one of username/external ID/URL is present.
- Non-null `(platform_id, external_id)` duplicates are stable conflicts; username/URL are not invented unique keys.
- External-ID uniqueness regressions must prove private External ID values and raw PostgreSQL vendor detail do not
  leak to captured logs.
- Relationship owner and target differ and resolve to existing External Accounts.
- One canonical current relationship row exists per owner/target pair.
- Snapshot header + entries are transactionally consistent historical data created as one batch.
- Completed snapshots are immutable in Phase 7; no public entry-append operation is approved.
- Identical duplicate target snapshots collapse; conflicting historical copies for one target reject the whole
  snapshot command rather than selecting or merging an arbitrary winner.
- Storing a snapshot alone does not silently rewrite current relationship state.
- Public APIs expose immutable capabilities/views, never JPA entities/repositories.
- Collection reads are parent-scoped and explicitly bounded.

## Phase gate

Phase 7 production changes require:

1. Phase 4–6 milestone status = `MILESTONE_READY`;
2. Phase 7 preparation review = `READY FOR HANDOFF`;
3. an active approved Phase 7 handoff.

## Phase 12 read-only search extension

An accepted Phase 12 handoff may modify this otherwise-frozen module **only** to add the read-only global-search
contract/query support defined by `docs/implementation/phase-12/README.md`.

Allowed:

- semantic public `search` named interface;
- owner-local search query/application/repository methods;
- search-only package descriptors/tests;
- Vault batch qualification/tag calls where this module already legally depends on Vault.

Not allowed:

- changing existing mutation/validation/lifecycle semantics;
- exposing entities/repositories/internals;
- importing the top-level `search` module;
- reading another module's repository/table directly;
- adding unbounded lists or per-hit cross-module calls.

The Phase 12 active handoff, when present, is the authority for this narrow exception to the original phase gate.

## M10-12-1 maintenance exception

A Codex handoff created from
`docs/implementation/maintenance/milestone-10-12-search-case-normalization/README.md` may modify this frozen module
only for the Search SQL/query-folding and snippet-offset defect described there.

Do not use the maintenance handoff to change mutation behavior, ownership, public Search contracts, ranking, source
bounds, schema/index definitions, or unrelated code. The maintenance handoff is the temporary authority; otherwise
the frozen phase rules remain in force.
