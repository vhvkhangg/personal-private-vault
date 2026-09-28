---
name: fiction-domain-modeling
description: Guide Fiction-module Vault identity, author-source invariants, Fiction-owned genres, shared narrative references, classification sets, links, state, and cross-module contracts without persistence leakage.
---

# Fiction Domain Modeling

Use for Phase 4 Fiction planning, implementation, testing, or review.

## Ownership

`fiction` owns:

- `fictions`
- `fiction_genres`
- `fiction_story_archetypes`
- `fiction_world_settings`
- `fiction_links`

Shared `story_archetypes` and `world_settings` remain owned by `reference`.

## Vault identity

A Fiction is backed by a Vault Entry with the same ID. Create identity through public Vault operations and keep
Fiction/Vault creation transactionally consistent.

Favorite/rating/tag/recycle behavior remains in `vault`; never duplicate it in Fiction.

## Author source

A Fiction has exactly one author source:

```text
author_person_id XOR author_group_id
```

Validate it through public People capabilities. Never import People entities/repositories/internal packages.

## Classification ownership

- one Fiction genre, owned by Fiction;
- many story archetypes, sourced from Reference;
- many world settings, sourced from Reference.

Repeated story-archetype/world-setting assignment is set behavior and should be idempotent. PostgreSQL uniqueness is
the final race arbiter; raw persistence uniqueness errors should not leak.

## Links

Multiple links with the same language/type are valid per Schema v1. Do not invent a uniqueness constraint.

Validate optional language through public Reference operations when present.

Do not infer a stronger `is_primary` uniqueness rule unless the active handoff explicitly defines it from an
owner-approved requirement.

## Reads

Prefer bounded reads by Fiction ID and parent-scoped classifications/links. Global search/list behavior belongs to
later scope.

## Public API

Use semantic capability packages and immutable views. Do not expose JPA entities/repositories or create
`Service` / `ServiceImpl` pairs merely for convention.

## Testing

Use PostgreSQL Testcontainers. Cover Vault/Fiction rollback, author XOR, missing references, genre/reference
assignments, idempotent set associations, chapter-count check, links/language validation, module boundaries, and
unchanged Flyway/Hibernate validation.
