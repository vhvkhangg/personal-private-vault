---
name: media-domain-modeling
description: Guide Media-module Vault-backed Albums/Images, image metadata uniqueness, optional album membership, derived image counts, and the deferred object-storage boundary.
---

# Media Domain Modeling

Use for Phase 6 Media planning, implementation, testing, or review.

## Ownership

`media` owns `albums` and `images` and depends only on public Vault contracts.

## Vault identity

Both Album and Image are independent Vault Entry-backed records:

- Album → `VaultEntryType.ALBUM`;
- Image → `VaultEntryType.IMAGE`.

Create each Vault identity and subtype row in one transaction. Vault owns favorite/rating/tag/recycle semantics.

## Image membership and metadata

`album_id` is optional. Validate a supplied Album inside Media; do not create cross-module associations.

Respect frozen checks:

- object key required/unique;
- optional checksum unique;
- size nonnegative;
- width/height positive when present.

Duplicate object key or non-null checksum is a conflict, not an idempotent create. Concurrent duplicates require a
stable domain result with PostgreSQL uniqueness as final arbiter.

## Object storage boundary

Phase 6 stores metadata and `object_key`; it does not implement S3/local binary I/O, upload/delete workflows,
presigned URLs, or object existence checks. Do not add a provider SDK prematurely.

## Derived image count

Never persist `image_count`. If exposed by Album reads, compute it with a bounded count/projection; do not load all
Image rows merely to count them.

## Public API / reads

Prefer semantic Album/Image operations and immutable views. Keep multi-row reads parent-bounded and paginated/bounded
where appropriate. No global unbounded list/search API.

## Testing

Use PostgreSQL Testcontainers. Cover Vault rollback, Album reference validation, image constraints, deterministic
object-key/checksum conflict races, derived count behavior, exact Modulith dependencies, and unchanged Flyway/Hibernate
validation.
