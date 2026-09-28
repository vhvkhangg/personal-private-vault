# Backend Phase 4 — Fiction Foundation

Status: **PREPARED — AWAITING CODEX PRE-HANDOFF REVIEW**

Phase 4 implements the `fiction` module after the frozen `vault`, `reference`, and `people` foundations.

No Phase 4 production implementation is authorized until:

1. `docs/implementation/phase-3/milestone-review.md` is `MILESTONE_READY`;
2. Phase 4 passes `$codex-pre-handoff-review`;
3. the approved Phase 4 preparation slice is committed/pushed;
4. `$codex-create-handoff` creates an active Phase 4 handoff.

## Owned Schema v1 tables

- `fictions`
- `fiction_genres`
- `fiction_story_archetypes`
- `fiction_world_settings`
- `fiction_links`

No schema/Flyway change is planned.

## Frozen dependency direction

At whole-module architecture level, `fiction` may depend only on:

- `vault`
- `people`
- `reference`

The future handoff must narrow the Spring Modulith descriptor to only the named interfaces actually required by
the concrete implementation.

Expected public dependencies to verify during handoff creation:

- Vault identity: `vault::entry`, `vault::enums`, and any returned `vault::view` contract actually used;
- author validation: `people::person`, `people::group`, and returned `people::view` contracts actually used;
- shared reference lookup: `reference::catalog` and returned `reference::view` contracts actually used.

Do not change `fiction/package-info.java` during preparation; dependency narrowing is production work for the active
Phase 4 implementation handoff.

## Core fiction identity

A Fiction is a Vault Entry-backed entity:

```text
fictions.id == vault_entries.id
vault entry type == FICTION
```

Create shared identity through the public Vault API and preserve transaction consistency. Favorite/rating/tag/recycle
behavior remains owned by `vault`; Fiction must not duplicate it.

## Author-source invariant

Schema v1 requires exactly one author source:

- `author_person_id`, or
- `author_group_id`.

The two are mutually exclusive. The application contract must validate the selected source through public People
operations and preserve the database XOR check.

Do not expose People entities/repositories.

## Fiction genre and shared narrative classifications

- `fiction_genres` is owned by `fiction`; one Fiction has exactly one Fiction genre.
- `story_archetypes` and `world_settings` are shared reference catalogs owned by `reference`.
- A Fiction may have multiple story archetypes and multiple world settings.
- Duplicate `(fiction_id, story_archetype_id)` and `(fiction_id, world_setting_id)` assignments are set semantics:
  repeated add should be idempotent and PostgreSQL uniqueness remains the final race arbiter.

Do not move shared narrative reference tables into Fiction.

## Fiction state

Respect frozen Schema v1 fields and enums:

- format;
- NSFW flag;
- progress status;
- consumption status;
- optional total chapters (`>= 0`);
- optional current progress text;
- optional review text;
- optional nationality.

Do not invent a chapter table or percentage field in this phase.

## Fiction links

`fiction_links` supports multiple links, including repeated language/type combinations.

Planned behavior:

- create/read/update link records for a Fiction;
- optional `language_code` validates through the public Reference catalog when present;
- `link_type` remains the frozen free-form/varchar contract with documented examples (`TRANSLATION`, `CONVERT`,
  `ORIGINAL`, `OTHER`) unless the active handoff identifies an already-frozen enum elsewhere;
- do not impose uniqueness on language/type combinations that Schema v1 explicitly allows.

Any `is_primary` business rule beyond the frozen column semantics must be defined explicitly by the future handoff,
not guessed during preparation.

## Bounded public reads

Phase 4 should expose bounded lookup capabilities such as:

- Fiction by ID;
- classifications for one Fiction;
- links for one Fiction.

Do not add global unbounded list/search APIs; global search belongs to the later `search` module.

## Proposed package direction

```text
fiction/
├── fiction/
│   ├── package-info.java
│   └── FictionOperations.java
├── genre/
│   ├── package-info.java
│   └── FictionGenreOperations.java
├── link/
│   ├── package-info.java
│   └── FictionLinkOperations.java
├── view/
│   ├── package-info.java
│   └── *View.java
├── enums/
│   ├── package-info.java
│   └── stable public Fiction enums as needed
└── internal/
    ├── application/
    ├── domain/
    └── infrastructure/persistence/
```

The future Codex handoff may refine names. Do not introduce `Service` / `ServiceImpl`, generic CRUD bases, or
speculative abstractions merely to match a pattern.

## Testing contract for the future handoff

Require PostgreSQL Testcontainers coverage for:

- Fiction/Vault Entry transaction consistency and rollback;
- author-person XOR author-group invariant;
- missing author/reference failures through public contracts;
- Fiction genre ownership/association;
- idempotent story-archetype/world-setting assignments, including concurrency where the implementation accepts
  simultaneous add operations;
- `total_chapters >= 0`;
- Fiction link persistence and optional language validation;
- exact Spring Modulith named-interface dependencies;
- unchanged Flyway V1/Hibernate validation;
- final `mvn -f backend/pom.xml clean verify` evidence on Java 25.

Do not use H2.

## Out of scope

- film/media/account/knowledge implementation;
- chapters/content hosting;
- global search;
- REST/controllers/OpenAPI;
- frontend;
- migrations/schema redesign;
- deletion/permanent-delete behavior unless explicitly introduced by a later owner-approved scope;
- changes to frozen People/Vault/Reference behavior.

## Preparation tooling

Added for Phase 4:

- `.agents/skills/fiction-domain-modeling/SKILL.md`
- `.agents/rules/backend-phase-4-fiction.md`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/fiction/AGENTS.md`

Reused without new custom agents/hooks:

- `backend-implementer`
- `architecture-auditor`
- repository safety hook

No new custom agent or hook is justified for this phase.
