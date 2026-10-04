# Repository and Java Package Tree v1.1.1 — Frozen

## 1. Repository principles

- Backend-first. Frontend and RAG implementation are deliberately not initialized yet.
- One repository, one `main` branch, no pull-request workflow.
- Package by business capability/domain, never a repository-wide `controller/service/repository/entity` layout.
- Spring Modulith application modules map to direct subpackages of the root Java package.
- Module internals are hidden under `internal` packages.
- Cross-module access goes through explicitly exposed module APIs (`@NamedInterface` subpackages or intentional base-package contracts); direct access to another module's repositories/entities/internal packages is forbidden.
- `knowledge` and `collection` are parent application modules containing explicitly declared nested application modules.
- Avoid generic root packages such as `common`, `shared`, `util`, or `helpers`. Reusable concepts must have an explicit owner.
- No implementation or dependency scaffold is committed in this repository-initialization baseline.

Spring Modulith treats each direct subpackage below the Spring Boot application package as an application module. The module base package owns the module descriptor. Public contract subpackages are exposed deliberately with `@NamedInterface`; other subpackages remain internal. Nested modules are declared explicitly with `@ApplicationModule`.

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



## 2.1 Implementation documentation convention

Implementation history is organized per phase:

```text
docs/implementation/
├── phase-0/
├── phase-1/
├── phase-2/
├── handoffs/
└── <cross-cutting agent/tooling docs>
```

Every future backend phase gets its own `phase-N/` directory rather than adding phase files directly to
`docs/implementation/`.


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
├── search/                   # @NamedInterface("search") - KnowledgeSearchOperations facade, query, hits, documents
├── internal/                 # parent-module implementation/facade orchestration (KnowledgeSearchService)
├── study/                    # nested @ApplicationModule
│   ├── search/               # @NamedInterface("search") - StudySearchOperations
│   └── internal/             # StudySearchService, domain entities, repositories
├── information/              # nested @ApplicationModule
│   ├── search/               # @NamedInterface("search") - InformationSearchOperations
│   └── internal/             # InformationSearchService, domain entities, repositories
├── vocabulary/               # nested @ApplicationModule
│   ├── search/               # @NamedInterface("search") - VocabularySearchOperations
│   └── internal/             # VocabularySearchService, domain entities, repositories
└── note/                     # nested @ApplicationModule
    ├── search/               # @NamedInterface("search") - NoteSearchOperations
    └── internal/             # NoteSearchService, domain entities, repositories
```

External top-level modules depend on the public `knowledge` facade, not directly on nested repositories.

### Collection

```text
collection
├── search/                   # @NamedInterface("search") - CollectionSearchOperations facade, query, hits, documents
├── internal/                 # parent-module implementation/facade orchestration (CollectionSearchService)
├── music/                    # nested @ApplicationModule
│   ├── search/               # @NamedInterface("search") - MusicSearchOperations
│   └── internal/             # MusicSearchService, domain entities, repositories
├── shopping/                 # nested @ApplicationModule
│   ├── search/               # @NamedInterface("search") - ShoppingSearchOperations
│   └── internal/             # ShoppingSearchService, domain entities, repositories
└── software/                 # nested @ApplicationModule
    ├── search/               # @NamedInterface("search") - SoftwareSearchOperations
    └── internal/             # SoftwareSearchService, domain entities, repositories
```

External top-level modules depend on the public `collection` facade rather than bypassing the parent boundary.

## 6. Standard package shape inside a module

Package by business capability. The module base package owns the module descriptor. Public contracts may be split
into semantic named interfaces when the API becomes non-trivial.

```text
fiction/
├── package-info.java                 # @ApplicationModule
├── query/                            # optional @NamedInterface capability
│   └── package-info.java
├── view/                             # optional @NamedInterface for public read models
│   └── package-info.java
├── enums/                            # optional @NamedInterface for stable public enums
│   └── package-info.java
├── search/                           # @NamedInterface("search") - FictionSearchOperations, FictionSearchQuery, FictionSearchHit, FictionSearchDocument
└── internal/
    ├── application/
    │   └── <capability>/             # mirror public capability when useful, e.g. FictionSearchService
    ├── domain/
    ├── infrastructure/persistence/
    └── web/
```

### Feed

```text
feed/
├── package-info.java                 # @ApplicationModule(allowedDependencies = {"vault::entry", "vault::enums", "vault::view", "knowledge::api", "vault::search"})
├── source/                           # @NamedInterface("source") - FeedSource operations/commands/exceptions
├── item/                             # @NamedInterface("item") - FeedItem operations/commands/exceptions
├── resource/                         # @NamedInterface("resource") - SavedResource operations/commands/exceptions
├── conversion/                       # @NamedInterface("conversion") - SavedResourceConversion operations/exceptions
├── view/                             # @NamedInterface("view") - read models and JSON snapshot helpers
├── enums/                            # @NamedInterface("enums") - FeedSourceType, SavedResourceKind
├── search/                           # @NamedInterface("search") - FeedSearchOperations, FeedSearchQuery, FeedSearchHit, FeedSearchDocument
└── internal/
    ├── application/                  # capability services: FeedSourceService, FeedItemService, SavedResourceService, SavedResourceConversionService, FeedSearchService
    ├── domain/                       # JPA entities: FeedSource, FeedItem, SavedResource, SavedResourceConversion, SavedResourceConversionId
    └── infrastructure/persistence/   # Spring Data repositories
```

### ImportData

```text
importdata/
├── package-info.java                 # @ApplicationModule(allowedDependencies = {"vault::entry", "vault::enums", "vault::view", "knowledge::api"})
├── job/                              # @NamedInterface("job") - ImportJob operations/commands/exceptions
├── view/                             # @NamedInterface("view") - read models and JSON snapshot helpers
├── enums/                            # @NamedInterface("enums") - ImportTargetType, ImportFormat, ImportJobStatus, ImportItemStatus, ImportItemDecision
└── internal/
    ├── application/                  # capability service: ImportJobService
    ├── domain/                       # JPA entities: ImportJob, ImportJobItem
    ├── parsing/                      # parsers: CsvImportParser, JsonImportParser, MarkdownImportParser, TargetPayloadCanonicalizer
    └── infrastructure/persistence/   # Spring Data repositories
```

### Search

```text
search/
├── package-info.java                 # @ApplicationModule(allowedDependencies = {"vault::search", "vault::enums", "people::search", "fiction::search", "film::search", "media::search", "location::search", "knowledge::search", "collection::search", "account::search", "feed::search"})
├── query/                            # @NamedInterface("query") - GlobalSearchOperations, GlobalSearchQuery
├── view/                             # @NamedInterface("view") - GlobalSearchResult, GlobalSearchPage
├── enums/                            # @NamedInterface("enums") - SearchDomain, SearchMatchKind
└── internal/
    └── application/                  # orchestration service: GlobalSearchService (leaf/orchestration, no persistence tables)
```

### Module Search Capabilities (@NamedInterface("search"))

Phase 12 introduces read-only search capabilities across Vault and all 9 feature domains, each exposing `@NamedInterface("search")`:

- `vault`: `vault/search/` — `VaultSearchOperations`, `VaultTagSearchQuery`, `VaultTagCandidateHit`
- `people`: `people/search/` — `PeopleSearchOperations`, `PeopleSearchQuery`, `PeopleSearchHit`, `PeopleSearchDocument`
- `fiction`: `fiction/search/` — `FictionSearchOperations`, `FictionSearchQuery`, `FictionSearchHit`, `FictionSearchDocument`
- `film`: `film/search/` — `FilmSearchOperations`, `FilmSearchQuery`, `FilmSearchHit`, `FilmSearchDocument`
- `media`: `media/search/` — `MediaSearchOperations`, `MediaSearchQuery`, `MediaSearchHit`, `MediaSearchDocument`
- `location`: `location/search/` — `LocationSearchOperations`, `LocationSearchQuery`, `LocationSearchHit`, `LocationSearchDocument`
- `knowledge`: `knowledge/search/` (facade) and nested modules:
  - `knowledge/study/search/` — `StudySearchOperations`
  - `knowledge/information/search/` — `InformationSearchOperations`
  - `knowledge/vocabulary/search/` — `VocabularySearchOperations`
  - `knowledge/note/search/` — `NoteSearchOperations`
- `collection`: `collection/search/` (facade) and nested modules:
  - `collection/music/search/` — `MusicSearchOperations`
  - `collection/shopping/search/` — `ShoppingSearchOperations`
  - `collection/software/search/` — `SoftwareSearchOperations`
- `account`: `account/search/` — `AccountSearchOperations`, `AccountSearchQuery`, `AccountSearchHit`, `AccountSearchDocument`
- `feed`: `feed/search/` — `FeedSearchOperations`, `FeedSearchQuery`, `FeedSearchHit`, `FeedSearchDocument`

Rules:

- create a subpackage only for a real responsibility;
- prefer capability names over technical `service` packages;
- group `*View` records under `view/` and stable public enums under `enums/` when the API size justifies it;
- avoid generic `Service` / `ServiceImpl` naming;
- add `package-info.java` to meaningful new packages;
- remove `.gitkeep` when real tracked content exists.

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

Tests should mirror the production package being tested when practical. For an owner-approved slice, Codex creates the implementation handoff; Antigravity implements production code and tests, iteratively verifies/fixes the slice, and retains final evidence; Codex performs final review; the owner commits/pushes only after `READY FOR OWNER COMMIT`. See `docs/workflow/agent-development-workflow.md` and `docs/architecture/testing-and-review.md` for the canonical workflow.

## 10. Deferred directories

The following are intentionally deferred and must not be bootstrapped in the initial commit:

```text
frontend/       # after backend completion
rag/            # after non-RAG backend + frontend work reaches the agreed point
infra/          # deployment decisions are explicitly deferred
```

Local Docker support may be introduced during backend implementation when PostgreSQL/object-storage development environments are actually needed; it does not require freezing a deployment topology now.

## Related architecture decisions

- [`ADR-0001`](../adr/0001-modular-monolith-with-spring-modulith.md) — modular monolith.
- [`ADR-0002`](../adr/0002-package-by-business-capability.md) — package by business capability.
- [`ADR-0015`](../adr/0015-semantic-public-api-subpackages.md) — semantic public API subpackages and named interfaces.
