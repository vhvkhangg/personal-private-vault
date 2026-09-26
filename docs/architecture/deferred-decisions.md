# Deferred Architecture Decisions

These topics are intentionally **not frozen**. Deferral prevents speculative design from becoming accidental architecture.

## Frontend architecture

Known direction:

- Next.js + TypeScript
- shadcn/ui
- pnpm

Deferred until backend completion:

- exact Next.js version at initialization time;
- state/data-fetching strategy;
- frontend feature-folder boundaries;
- token transport implementation details in the browser;
- frontend test stack details beyond the current Playwright E2E direction.

## RAG

Python/RAG is explicitly out of scope until the non-RAG backend and frontend reach the agreed point.

No vector database, embedding model, ingestion pipeline, or RAG service boundary is frozen now.

## Deployment

Current development is expected to run locally on the owner's laptop/PC with Docker where useful.

Deferred:

- cloud provider;
- public domain/reverse proxy;
- TLS termination;
- managed vs self-hosted PostgreSQL;
- final object-storage provider;
- backup destination;
- observability stack;
- deployment CI/CD.

## Advanced authentication hardening

Current baseline is password + JWT access/refresh token + private-mode PIN.

Deferred until deployment/security hardening:

- TOTP 2FA;
- passkeys;
- device/session management UI;
- Internet-facing login throttling strategy;
- security audit-log scope.

## Exact API envelope

A consistent `ApiResponse` is required, but the exact fields and pagination/error metadata are finalized during backend bootstrap before the first controller is implemented.

## Exact stable dependency versions

The project uses the latest stable **mutually compatible** set at implementation time, preferring LTS runtimes and Spring Boot dependency management. Version numbers are not permanently frozen in architecture documentation.
