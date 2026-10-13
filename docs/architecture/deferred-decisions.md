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

## RAG — authorized preparation, implementation still gated

The owner authorized **Phase 17 RAG preparation ahead of Phase 16 Frontend** on 2026-10-13, after Phases 0–15 were frozen. Phase numbering remains unchanged; see proposed [ADR-0019](../adr/0019-local-rag-before-frontend.md) and [RAG architecture proposal](rag-architecture.md). No production RAG implementation, new Java module, schema migration or network service exists yet.

An offline Python/FastAPI engine + Spring read-only source orchestration + PostgreSQL/pgvector is a **proposal for Codex review**, not a settled implemented or frozen stack. Exact model/runtime, OCR engine and multilingual/table quality, chunk sizes, vector index parameters, service contracts, extension image compatibility, schema details and numeric acceptance thresholds require measurement/owner acceptance. Ideaverse is **not integrated** and future read-only Obsidian synchronization remains a later owner-approved extension.

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
