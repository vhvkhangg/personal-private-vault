# Personal Private Vault — Architecture Diagrams v1

This directory is the diagram baseline after freezing **Database Schema v1** and **Module Boundary v1**.

## Canonical sources

- `diagrams/source/functional-decomposition.drawio` — BFD / functional decomposition.
- `structurizr/workspace.dsl` — C4 System Context and C4 Container model/views.
- `diagrams/source/module-dependencies.drawio` — Spring Modulith application-module dependencies.

## Review exports

SVG exports live under `diagrams/exported/` and are intended for Markdown documentation and fast visual review.

## Architectural scope

- Single-user permanently.
- Backend-first implementation.
- Java/Spring modular monolith.
- PostgreSQL for structured data and metadata.
- S3-compatible object storage abstraction for media binaries.
- REST/JSON API.
- RAG is explicitly deferred and excluded from v1 diagrams.
- Deployment topology is intentionally not frozen yet.

## Source-of-truth rule

Edit the canonical source, then regenerate/export the corresponding SVG. Do not edit exported SVGs as the architectural source of truth.
