# ADR-0005 — Use a Shared Vault-Entry Identity

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

Many content types need the same cross-cutting features: favorites, ratings, tags, common identity, and recycle-bin lifecycle. Duplicating these columns/relations across every content table would increase inconsistency and complicate global operations.

## Decision

Use `vault_entries` as the shared identity for content-capable entities. Normalize favorites, ratings, and tag assignments around that identity. Keep subtype-specific fields in subtype tables.

## Rationale

This avoids one giant sparse `item` table while still centralizing genuinely common content behavior.

## Consequences

- Capability support is type-specific; not every vault entry is rateable/taggable.
- Application rules must enforce the allowed capability matrix.
- Feature-specific fields stay in feature tables.

## Alternatives considered

- **One giant polymorphic `item` table:** rejected because it would create many nullable unrelated columns and weak integrity.
- **Duplicate favorite/rating/tag fields in each table:** rejected because global behavior becomes repetitive and harder to keep consistent.
