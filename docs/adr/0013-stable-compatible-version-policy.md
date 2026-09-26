# ADR-0013 — Use Latest Stable Mutually Compatible Versions

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

The owner wants modern stable technology, but independently forcing every library to the numerically latest version can break framework compatibility.

## Decision

At each implementation phase, choose the latest stable mutually compatible release set. Prefer LTS runtimes where appropriate. Let Spring Boot dependency management/BOM control managed libraries unless a documented reason requires an override.

## Rationale

Compatibility and maintainability are more important than maximizing version numbers. This also prevents architecture documents from becoming stale version catalogs.

## Consequences

- Exact versions are recorded in build files once implementation begins.
- Upgrades are deliberate and test-backed.
- Beta/RC/milestone releases are not chosen for production baseline solely because they are newer.

## Alternatives considered

- **Pin every library manually to its newest release:** rejected because it can create unsupported combinations.
- **Freeze old versions indefinitely:** rejected because the project intentionally targets modern stable releases.
