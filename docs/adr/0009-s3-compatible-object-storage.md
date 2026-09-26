# ADR-0009 — Store Media Binaries in S3-Compatible Object Storage

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

The media library may reach tens of gigabytes. Storing binaries in PostgreSQL or on the application machine as the permanent design would create unnecessary database growth or local-storage coupling.

## Decision

Store media metadata in PostgreSQL and media binaries through an S3-compatible object-storage abstraction. The production provider is intentionally deferred. Development uses only small test objects locally.

## Rationale

This keeps relational data manageable, supports provider portability, and avoids tying the domain model to a specific cloud vendor.

## Consequences

- Object keys/checksums become important metadata.
- Backup strategy must cover both database metadata and object storage.
- Final provider/cost/retention decisions remain deployment concerns.

## Alternatives considered

- **PostgreSQL blobs:** rejected for the expected binary volume and separation-of-concerns.
- **Permanent laptop filesystem:** rejected because the owner explicitly does not want the primary media library stored on local devices.
