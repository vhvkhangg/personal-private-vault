# Vault Module Instructions

Backend Phase 1 behavior is frozen; owner-approved structural changes must preserve that behavior.

- Owned tables: `vault_entries`, `favorites`, `ratings`, `tags`, `vault_entry_tags`.
- No application-module dependencies.
- Public capabilities use named subpackages: `entry`, `metadata`, `view`, and `enums`.
- Public interfaces are capability-oriented; internal implementations mirror the capability under
  `vault.internal.application`.
- Entities/repositories/services stay under `vault.internal`.
- Permanent deletion remains outside the frozen Phase 1 baseline.
- Capability checks remain mandatory before metadata writes.
- `FILM_CREDIT` is favorite-only (no rating/tags). `BRAND` supports global rating.

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


### Phase 12 tag-origin source contract

For tag-name search, Vault owns the complete pre-limit candidate relation:

- matching tag names;
- active/trash qualification;
- optional type/domain filters;
- required-tag AND qualification;
- exclusion of `FILM_CREDIT`;
- per-Vault-entry collapse to best tag similarity.

Only after those operations may Vault apply the tag-source top-K, ordered by:

```text
best_tag_similarity DESC
type_name ASC
vault_entry_id ASC
```

For native PostgreSQL ordering, define `CAST(ve.entry_type AS text) COLLATE "C" AS type_name` and order by
`type_name ASC`; do not use the native enum order.

`primaryText` is feature-owned display data and is not a ranking key. After Vault chooses the exact source top-K,
Search may batch-materialize those IDs through owning feature search contracts.

Do not use fixed oversampling followed by post-filtering, do not query feature tables, and do not issue per-ID feature
calls.

## Phase 13 HTTP adapter exception

An accepted active Phase 13 handoff may modify this otherwise-frozen module only to add the REST/JSON adapter work
authorized by `docs/implementation/phase-13/README.md`.

Allowed:

- owner `internal/web` controller/request/response DTO/mapper/advice packages;
- mapping to existing owner operations/facades;
- HTTP Bean Validation and OpenAPI annotations;
- module exception-to-HTTP translation;
- tests needed for this HTTP surface.

Not allowed:

- changing existing domain/application invariants or persistence behavior;
- importing another module's internal/repository/entity;
- exposing application entities or internal Search contracts;
- inventing new business operations solely for HTTP convenience;
- schema/Flyway changes;
- Phase 14+ work.

The active Phase 13 handoff, when present, is the temporary authority for this narrow adapter exception. Otherwise
the frozen-module rules remain in force.
