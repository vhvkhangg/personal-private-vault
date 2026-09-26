# ADR-0003 — Use REST/JSON with OpenAPI

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

The frontend and external consumers need a predictable HTTP API. Current use cases are CRUD/query/workflow-oriented and do not require client-defined graph traversal.

## Decision

Expose REST/JSON endpoints, document them with OpenAPI, and use one consistent `ApiResponse` family. Controllers are owned by their business module. The exact response-envelope fields are finalized before the first controller is implemented.

## Rationale

REST maps cleanly to the current use cases, is straightforward to test with Spring tooling, and avoids introducing a second query/schema abstraction without a demonstrated need.

## Consequences

- Endpoint design must be resource/use-case oriented rather than exposing repositories.
- Validation/error mapping must be consistent across modules.
- OpenAPI descriptions are part of the API contract.

## Alternatives considered

- **GraphQL:** not selected because no current requirement justifies its added schema/resolver complexity.
- **gRPC:** not selected for browser-facing application APIs.
