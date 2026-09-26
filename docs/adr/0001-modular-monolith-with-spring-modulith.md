# ADR-0001 — Use a Modular Monolith with Spring Modulith

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

The system contains many business capabilities but permanently serves one user, uses one primary relational database, and has no requirement for independently deployable services. Requirements are expected to evolve while the project is also used to learn modern Spring architecture.

## Decision

Build the backend as one Spring Boot deployable using Spring Modulith to define and verify application modules. Modules communicate through explicit public contracts/events while remaining in one process and one codebase.

## Rationale

This preserves strong business boundaries without introducing network calls, distributed transactions, service discovery, multi-service deployment, and observability complexity that would not solve a current requirement.

## Consequences

- One deployment unit is simpler to run and debug.
- Module boundaries must be actively verified or the monolith can degrade into a tightly coupled codebase.
- A future extraction is possible if a real independent deployment/scaling boundary emerges.

## Alternatives considered

- **Layered monolith by technical package:** rejected because domain ownership would be weaker and cross-domain repository access easier.
- **Microservices:** rejected for v1 because operational/distributed-system cost is not justified by a single-user workload.
