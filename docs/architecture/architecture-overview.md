# Architecture Overview

## 1. Purpose

Personal Private Vault is a permanent single-user system for organizing personal knowledge, media metadata, fiction/film tracking, external accounts, locations, study material, finance, journal entries, and structured personal/family information.

The project is intentionally **backend-first**. Frontend implementation begins only after the non-RAG backend is complete enough to support it.

## 2. Architectural style

The backend is a **modular monolith** implemented with Spring Boot and Spring Modulith.

The application is deployed as one backend process and uses one PostgreSQL database, but the codebase is divided into business-capability modules with explicit ownership and dependency rules. A module may use another module only through its public contract; another module's JPA entities, repositories, and internal implementation packages are not shared infrastructure.

This architecture is chosen because the system is permanently single-user, has many business capabilities but no current requirement for independent service deployment, and is expected to evolve frequently while learning and applying Spring architecture practices.

## 3. System context

The owner uses the vault through a browser-capable client. The planned system interacts with:

- PostgreSQL for structured application data and metadata;
- an S3-compatible object store for media binaries;
- public feed sources such as GitHub Trending, Hacker News, Reddit, RSS, and configured technology sites;
- external social/account platforms as metadata/link sources where supported.

See [`diagrams/exported/system-context.svg`](diagrams/exported/system-context.svg).

## 4. Container view

The planned runtime containers are:

### Web Application

Future Next.js + TypeScript + shadcn/ui client. It is represented in C4 for completeness but is **not initialized yet**.

### Backend API

Single Java/Spring process containing the modular monolith. It owns:

- REST endpoints;
- authentication and authorization;
- module business logic;
- persistence orchestration;
- import workflows;
- scheduled jobs;
- feed retrieval;
- global search orchestration;
- object-storage integration.

### PostgreSQL Database

Stores structured business data, reference data, authentication data, search-supporting relational data, and media metadata. Binary media is not stored as PostgreSQL blobs.

### S3-Compatible Object Storage

Stores image/media binaries. The provider is not frozen. Development initially uses only small test files through the same storage abstraction.

See [`diagrams/exported/container.svg`](diagrams/exported/container.svg).

## 5. Application structure

Top-level modules (all 19 are implemented and frozen through Phase 14 owner commit `3bb3f2e`):

```text
authentication
settings
reference
vault
people
fiction
film
media
location
knowledge
collection
account
feed
importdata
finance
journal
personal
search
portability
```

Nested modules:

```text
knowledge
├── study
├── information
├── vocabulary
└── note

collection
├── music
├── shopping
└── software
```

See [`module-boundaries.md`](module-boundaries.md) for ownership and dependency rules.

## 6. Foundation modules

### `vault`

Shared content identity and cross-cutting content capabilities:

- `vault_entries` identity;
- favorites;
- ratings;
- tags;
- recycle-bin lifecycle.

`vault` does not depend on feature modules.

### `reference`

Stable cross-domain reference data:

- countries;
- languages;
- currencies;
- external/software platforms;
- globally shared narrative taxonomies needed by both fiction and film (`story_archetypes`, `world_settings`).

Feature modules depend on `reference`; `reference` does not depend on feature modules.

## 7. Orchestration modules

### `search`

Global search is intentionally separated from `vault` to avoid a reverse dependency from a foundation module back into feature modules. It is a leaf/orchestration module that calls public search contracts of searchable modules.

### `portability`

`portability` is the implemented/frozen Phase 14 portable-export leaf at owner commit `3bb3f2e`. It owns no business
tables and, under ADR-0017, may perform only a read-only repeatable-read JDBC snapshot over an explicit allowlist of
application tables. It never mutates another module's data or imports another module's entities/repositories.

### `importdata`

Owns import workflow state and parsing/orchestration. It does not write directly to repositories belonging to Study, Information, Vocabulary, or Note; it calls the public `knowledge` import contract.

## 8. Communication rule

Use synchronous module APIs when the caller requires an immediate result to complete the current operation. Use application/domain events for side effects that do not need to be completed through a direct return value.

Do not introduce events merely to avoid normal method calls.

## 9. Persistence rule

A database foreign key does not grant Java-level repository access. The table owner remains the owning module even when another table references it.

Cross-module behavior goes through a public application contract or an accepted event.

## 10. Current non-goals

The v1 backend architecture deliberately does not freeze:

- frontend component architecture;
- RAG architecture;
- cloud deployment provider/topology;
- production media provider;
- production backup destination;
- 2FA/passkeys;
- microservices;
- GraphQL.

These are revisited only when their implementation phase begins or a concrete requirement appears.
