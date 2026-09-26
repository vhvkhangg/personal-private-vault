# ADR-0002 — Package by Business Capability

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

A repository-wide `controller/service/repository/entity` structure does not align with the frozen domain/module boundaries and encourages implementation classes to be shared across domains.

## Decision

Use package-by-business-capability. Direct child packages of the root Java package represent top-level Spring Modulith modules. `knowledge` and `collection` contain explicitly declared nested application modules. Module implementation is kept under `internal/`; only intentional contracts are public.

## Rationale

Code that changes together stays together. Ownership remains visible in the package tree, and Spring Modulith can verify cycles/internal access.

## Consequences

- New packages are created only when a real responsibility exists.
- Public module contracts must stay small.
- Generic root `common/shared/util` dumping grounds are prohibited.

## Alternatives considered

- **Package by layer:** rejected because domain boundaries become implicit.
- **Full separate Maven module per domain:** deferred; current boundaries do not require separate compilation/deployment artifacts.
