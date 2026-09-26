# Repository and Java Package Tree v1 — Frozen

## 1. Repository principles

- Backend-first. Frontend and RAG implementation are deliberately not initialized yet.
- One repository, one `main` branch, no pull-request workflow.
- Package by business capability/domain, never a repository-wide `controller/service/repository/entity` layout.
- Spring Modulith application modules map to direct subpackages of the root Java package.
- Module internals are hidden under `internal` packages.
- Cross-module access goes through the owning module's public base-package API; direct access to another module's repositories/entities/internal packages is forbidden.
- `knowledge` and `collection` are parent application modules containing explicitly declared nested application modules.
- Avoid generic root packages such as `common`, `shared`, `util`, or `helpers`. Reusable concepts must have an explicit owner.
- No implementation or dependency scaffold is committed in this repository-initialization baseline.

Spring Modulith's default model treats each direct subpackage below the Spring Boot application package as an application module. The module base package is its public API; subpackages are internal unless explicitly exposed. Nested modules will be declared explicitly with `@ApplicationModule` when implementation begins.

## 2. Root repository tree

```text
personal-private-vault/
├── .editorconfig
├── .gitattributes
├── .gitignore
├── README.md
│
├── backend/
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/vhvkhangg/personalprivatevault/
│       │   │       ├── authentication/
│       │   │       ├── settings/
│       │   │       ├── reference/
│       │   │       ├── vault/
│       │   │       ├── people/
│       │   │       ├── fiction/
│       │   │       ├── film/
│       │   │       ├── media/
│       │   │       ├── location/
│       │   │       ├── knowledge/
│       │   │       │   ├── study/
│       │   │       │   ├── information/
│       │   │       │   ├── vocabulary/
│       │   │       │   └── note/
│       │   │       ├── collection/
│       │   │       │   ├── music/
│       │   │       │   ├── shopping/
│       │   │       │   └── software/
│       │   │       ├── account/
│       │   │       ├── feed/
│       │   │       ├── importdata/
│       │   │       ├── finance/
│       │   │       ├── journal/
│       │   │       ├── personal/
│       │   │       └── search/
│       │   └── resources/
│       │       └── db/migration/
│       └── test/
│           └── java/
│               └── com/vhvkhangg/personalprivatevault/
│
└── docs/
    ├── README.md
    ├── architecture/
    │   ├── README.md
    │   ├── module-dependency-matrix.md
    │   ├── diagrams/
    │   │   ├── source/
    │   │   └── exported/
    │   └── structurizr/
    │       └── workspace.dsl
    ├── database/
    │   ├── README.md
    │   └── personal-private-vault-schema-v1-FROZEN-final.dbml
    └── repository/
        └── repository-package-tree.md
```

`frontend/`, `rag/`, and deployment/infra directories are intentionally not created yet. They will be introduced only when their phases begin.

## 3. Root Java package

```text
com.vhvkhangg.personalprivatevault
```

The future Spring Boot bootstrap class will live directly in this root package so Spring Modulith can detect the direct child packages as top-level application modules.

## 4. Top-level application modules

```text
com.vhvkhangg.personalprivatevault
├── authentication
├── settings
├── reference
├── vault
├── people
├── fiction
├── film
├── media
├── location
├── knowledge
├── collection
├── account
├── feed
├── importdata
├── finance
├── journal
├── personal
└── search
```

Module responsibilities and allowed dependencies are frozen in `docs/architecture/module-dependency-matrix.md`.

## 5. Nested application modules

### Knowledge

```text
knowledge
├── internal/                 # parent-module implementation/facade orchestration
├── study/                    # nested @ApplicationModule
│   └── internal/
├── information/              # nested @ApplicationModule
│   └── internal/
├── vocabulary/               # nested @ApplicationModule
│   └── internal/
└── note/                     # nested @ApplicationModule
    └── internal/
```

External top-level modules depend on the public `knowledge` facade, not directly on nested repositories.

### Collection

```text
collection
├── internal/                 # parent-module implementation/facade orchestration
├── music/                    # nested @ApplicationModule
│   └── internal/
├── shopping/                 # nested @ApplicationModule
│   └── internal/
└── software/                 # nested @ApplicationModule
    └── internal/
```

External top-level modules depend on the public `collection` facade rather than bypassing the parent boundary.

## 6. Standard package shape inside a module

The module base package is its public contract. Implementation code belongs below `internal/`.

```text
fiction/
├── package-info.java                 # @ApplicationModule + allowedDependencies (later)
├── <public API interfaces/records>   # cross-module contract (later)
└── internal/
    ├── application/                  # use cases, commands, queries, orchestration
    ├── domain/                       # aggregates, value objects, domain rules/services
    ├── infrastructure/               # outbound technical adapters
    │   └── persistence/              # JPA/Hibernate/Spring Data implementation
    └── web/                          # REST controllers and HTTP DTO/mapping
```

Not every module must contain every internal package. Create a package only when code has a real responsibility for it.

If a module later needs to expose events or another secondary contract package, expose only that package explicitly as a Spring Modulith `@NamedInterface`. Do not make a module `OPEN` merely for convenience.

## 7. Public API rule

Allowed:

```text
film.internal.application -> people (public API)
search.internal.application -> fiction (public API)
importdata.internal.application -> knowledge (public facade API)
```

Forbidden:

```text
film -> people.internal.*
search -> fiction.internal.infrastructure.persistence.*
importdata -> knowledge.vocabulary.internal.*
```

JPA entities and Spring Data repositories remain module-internal.

## 8. Persistence rule

Each module owns its persistence implementation and the tables assigned to that domain. A module never injects another module's repository.

Database foreign keys do not grant Java-level repository access. Cross-domain operations use the target module's public API or an application/domain event according to the frozen dependency model.

Flyway scripts will live under:

```text
backend/src/main/resources/db/migration/
```

## 9. Tests

Production code and tests are separated conventionally:

```text
backend/src/main/java/...
backend/src/test/java/...
```

Tests should mirror the production package being tested when practical. The repository owner writes implementation code; Antigravity writes and runs tests once for the completed slice; Codex performs the final review.

## 10. Deferred directories

The following are intentionally deferred and must not be bootstrapped in the initial commit:

```text
frontend/       # after backend completion
rag/            # after non-RAG backend + frontend work reaches the agreed point
infra/          # deployment decisions are explicitly deferred
```

Local Docker support may be introduced during backend implementation when PostgreSQL/object-storage development environments are actually needed; it does not require freezing a deployment topology now.
