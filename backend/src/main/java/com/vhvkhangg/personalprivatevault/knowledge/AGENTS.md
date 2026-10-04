# Knowledge Module Agent Instructions

Applies recursively to `com.vhvkhangg.personalprivatevault.knowledge`.

## Topology

Parent:

- `knowledge` — stable facade/orchestration only.

Nested application modules:

- `knowledge.study` — `study_items`
- `knowledge.information` — `information_items`
- `knowledge.vocabulary` — `vocabulary_items`, `vocabulary_reviews`
- `knowledge.note` — `notes`

Do not let the parent facade become a second persistence implementation.

Externally consumable parent facade methods must use parent-owned Knowledge API types in their public signatures.
Top-level callers must not need imports from `knowledge.study`, `knowledge.information`, `knowledge.vocabulary`, or
`knowledge.note`. The parent may map parent DTOs to nested DTOs, but validation/persistence stays nested-owned.

## Whole-module dependencies

Frozen top-level direction allows:

- `vault`
- `people`
- `reference`
- `account`

Each nested module must declare/use only the public named interfaces it genuinely needs. Never import another
module's or nested module's `internal` package/repository.

Expected needs:

- Study → Vault + People + Reference + Account public contracts;
- Information → Vault public contracts;
- Vocabulary → Vault + Reference public contracts;
- Note → Vault public contracts.

## Guidance

Use:

- `knowledge-domain-modeling`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `reuse-and-consistency`
- `backend-testing`

## Invariants

- Study/Information/Vocabulary/Note IDs equal their corresponding Vault Entry IDs.
- Vault metadata behavior stays in Vault.
- Study author is zero-or-one of Person/Creator Group, never both.
- Study Website and YouTube fields follow frozen type-specific checks.
- YouTube Study references an Account of type `YOUTUBE_CHANNEL` on the canonical YouTube platform.
- Vocabulary does not gain word/language uniqueness.
- Vocabulary review transition updates current state + history atomically and serializes per item.
- Due Vocabulary reads exclude `MASTERED`, use an inclusive cutoff, include null-time `NEW` only, put scheduled due
  rows before unscheduled NEW rows, use ID tie-breaks, require a bounded limit, and never mutate state.
- Note Markdown/Obsidian content and arbitrary frontmatter survive round-trip.
- Note imported-hash and Study YouTube-account unique conflicts remain privacy-safe in logs.
- Public contracts/views are immutable and contain no JPA entities/repositories.
- A representative external top-level consumer test must compile/verify using only parent Knowledge API imports and
  no nested Knowledge package imports.
- Collection reads are explicitly bounded.
- Remove nested `.gitkeep` files when real implementation files replace them.

## Phase gate

Phase 8 production changes require:

1. Phase 8 preparation review = `READY FOR HANDOFF`;
2. an active approved Phase 8 handoff.

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
