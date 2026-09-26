# ADR-0007 — Use Synchronous Module APIs Plus Events

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

Some cross-module operations require an immediate result, while other reactions are side effects that should not tightly couple feature implementation.

## Decision

Use synchronous public module APIs when the caller needs a return value/validation to complete the use case. Use application/domain events for decoupled side effects. Do not introduce events solely to avoid direct application API calls.

## Rationale

This keeps control flow understandable while preserving decoupling where it has real value.

## Consequences

- Module APIs must not expose internal JPA repositories/entities.
- Event contracts are created only when a real producer/consumer relationship exists.
- Transaction boundaries remain explicit.

## Alternatives considered

- **Everything synchronous:** rejected because side effects can create unnecessary direct coupling.
- **Everything event-driven:** rejected because it would obscure simple in-process flows and add accidental complexity.
