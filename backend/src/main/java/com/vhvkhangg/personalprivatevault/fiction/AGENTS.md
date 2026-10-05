# Fiction Module Agent Instructions

Applies to `com.vhvkhangg.personalprivatevault.fiction`.

## Ownership

- `fictions`
- `fiction_genres`
- `fiction_story_archetypes`
- `fiction_world_settings`
- `fiction_links`

Shared `story_archetypes` / `world_settings` remain owned by `reference`.

## Dependencies

Whole-module architecture allows `vault`, `people`, and `reference`, but Phase 4 implementation must narrow the
Spring Modulith descriptor to the named interfaces actually used by the public signatures and application logic.

Never import another module's `internal` packages or repositories.

## Guidance

Use:

- `fiction-domain-modeling`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `reuse-and-consistency`
- `backend-testing`

## Invariants

- Fiction ID is its corresponding Vault Entry ID.
- Exactly one author source is present: Person XOR Creator Group.
- Fiction genre is required and Fiction-owned.
- Shared story archetypes/world settings are validated through Reference.
- Repeated classification assignment is idempotent set behavior.
- `total_chapters` is null or nonnegative.
- Fiction links permit repeated language/type combinations.
- Public APIs expose capabilities/views, never JPA entities or repositories.
- Global search/listing and deletion are outside Phase 4 unless an approved handoff says otherwise.

Phase 4 production changes require milestone success, `READY FOR HANDOFF`, and an active approved Phase 4 handoff.

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
