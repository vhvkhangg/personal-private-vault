# ADR-0014 — Defer Frontend, RAG, and Deployment-Specific Architecture

- **Status:** Accepted
- **Date:** 2026-09-26

## Context

The project sequence is backend first, then frontend, then later RAG. Deployment design is explicitly postponed until the application is functionally complete enough to make deployment requirements concrete.

## Decision

Do not initialize or freeze frontend, RAG, or production deployment architecture during backend bootstrap. Represent the future web client in C4 for system completeness, but create implementation directories only when their phase begins.

## Rationale

Deferral reduces speculative architecture and keeps current work focused on decisions that affect backend code now.

## Consequences

- `frontend/`, `rag/`, and production `infra/` are not baseline implementation directories yet.
- Local Docker support may still be added for PostgreSQL/object-storage development because that is a development environment need, not a production topology decision.

## Alternatives considered

- **Design every future phase now:** rejected because requirements/provider/tool choices may change before implementation begins.
