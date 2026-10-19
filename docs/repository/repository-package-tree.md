# Repository and Java Package Tree v1.1.1 — Frozen baseline + Phase 14 and Phase 17 candidate deltas

## 1. Repository principles

- Backend-first. Frontend remains deferred. The Phase 17 RAG foundation candidate is implemented under its active handoff and awaits independent review.
- One repository, one `main` branch, no pull-request workflow.
- Package by business capability/domain, never a repository-wide `controller/service/repository/entity` layout.
- Spring Modulith application modules map to direct subpackages of the root Java package.
- Module internals are hidden under `internal` packages.
- Cross-module access goes through explicitly exposed module APIs (`@NamedInterface` subpackages or intentional base-package contracts); direct access to another module's repositories/entities/internal packages is forbidden.
- `knowledge` and `collection` are parent application modules containing explicitly declared nested application modules.
- Avoid generic root packages such as `common`, `shared`, `util`, or `helpers`. Reusable concepts must have an explicit owner.
- No implementation or dependency scaffold is committed in this repository-initialization baseline.

Spring Modulith treats each direct subpackage below the Spring Boot application package as an application module. The module base package owns the module descriptor. Public contract subpackages are exposed deliberately with `@NamedInterface`; other subpackages remain internal. Nested modules are declared explicitly with `@ApplicationModule`.

**Phase 14 delta:** ADR-0017 adds the implemented `portability` leaf module and ADR-0009/Phase 14 closure introduces
Media-internal S3-compatible storage packages. The delta is frozen at owner commit `3bb3f2e` and comprises `portability`
(`internal/application`, `internal/infrastructure/snapshot`, `internal/web/controller`) and `media`
(`internal/application/storage`, `internal/infrastructure/storage`). All prior module boundaries and ownership
invariants remain strictly preserved.

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
│       │   │       ├── search/
│       │   │       ├── portability/           # Phase 14 implemented/frozen at 3bb3f2e
│       │   │       └── rag/                   # Phase 17 RAG foundation leaf module
│       │   └── resources/
│       │       └── db/migration/
│       └── test/
│           └── java/
│               └── com/vhvkhangg/personalprivatevault/
│
├── rag-engine/                                # Phase 17 Python worker service
│   ├── pyproject.toml
│   ├── README.md
│   ├── src/
│   │   └── ppv_rag/
│   └── tests/
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

`frontend/` and deployment/infra directories are intentionally not created yet. They will be introduced only when their phases begin. Phase 17 introduces the backend `rag` leaf module and local isolated `rag-engine` worker.



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
├── PersonalPrivateVaultApplication.java
├── package-info.java
├── ApiResponse.java                  # Canonical unified API response envelope
├── ApiError.java                     # Unified error payload
├── ApiFieldError.java                # Field-level validation detail
├── ApiMeta.java                      # Unified response metadata
├── ApiPageMeta.java                  # Truthful pagination metadata
├── ApiResponses.java                 # Static response builder utilities
├── ApiExceptionHandler.java          # Framework-level exception advice
└── OpenApiConfiguration.java         # OpenAPI/Swagger configuration
```

The Spring Boot bootstrap class lives directly in this root package so Spring Modulith can detect the direct child packages as top-level application modules. Per ADR-0016, this root package additionally contains only the narrow, explicitly bounded shared REST/HTTP wire contract. No business logic, repositories, entities, or module-specific DTOs live in the root package.

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
├── search
├── portability    # Phase 14 leaf module; no business-module dependency
└── rag            # Phase 17 foundation candidate; vault/fiction/film public contracts only
```

`portability` is the sole approved Phase 14 top-level addition. Its implemented shape is module-local:
`package-info.java` plus:
- `internal/application/`: `PortabilityExportService` (archive orchestration and cleanup);
- `internal/infrastructure/snapshot/`: `PortabilitySnapshotAdapter` (sole approved read-only JDBC allowlist adapter);
- `internal/web/controller/`: `PortabilityController` (`POST /api/v1/portability/exports`).

`media` internal storage packages:
- `internal/application/storage/`: `MediaStoragePort`, `StorageDownloadResult`, `ImageUploadService`, `ImageDownloadService`, storage exceptions;
- `internal/infrastructure/storage/`: `S3MediaStorageAdapter`, `DisabledMediaStorageAdapter`, `MediaStorageProperties`, `MediaStorageConfiguration`, `MediaStorageHealthIndicator`.

Module responsibilities and allowed dependencies are frozen except for this owner-approved ADR-0017 delta in
`docs/architecture/module-dependency-matrix.md`.

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
    └── web/                          # Phase 13 HTTP adapters
        ├── advice/                   # module-specific @RestControllerAdvice
        ├── controller/               # REST controllers under /api/v1
        ├── dto/                      # request/response records
        └── mapper/                   # static DTO mappers
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
    ├── infrastructure/persistence/   # Spring Data repositories
    └── web/                          # Phase 13 HTTP adapters (advice, controller, dto, mapper)
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
    ├── infrastructure/persistence/   # Spring Data repositories
    └── web/                          # Phase 13 HTTP adapters (advice, controller, dto, mapper)
```

### Search

```text
search/
├── package-info.java                 # @ApplicationModule(allowedDependencies = {"vault::search", "vault::enums", "people::search", "fiction::search", "film::search", "media::search", "location::search", "knowledge::search", "collection::search", "account::search", "feed::search"})
├── query/                            # @NamedInterface("query") - GlobalSearchOperations, GlobalSearchQuery
├── view/                             # @NamedInterface("view") - GlobalSearchResult, GlobalSearchPage
├── enums/                            # @NamedInterface("enums") - SearchDomain, SearchMatchKind
└── internal/
    ├── application/                  # orchestration service: GlobalSearchService (leaf/orchestration, no persistence tables)
    └── web/                          # Phase 13 HTTP adapters (controller, dto, mapper)
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

ADR-0017 adds one narrow exception only for the implemented/frozen Phase 14 `portability` module: its internal snapshot adapter
may issue **read-only JDBC** against an explicit allowlist of application tables inside one PostgreSQL read-only
`REPEATABLE READ` export snapshot. This does not grant repository/entity/internal-package imports, writes, generic
reporting SQL, or any Spring Modulith dependency edge to the owning business modules.

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
rag-engine/     # Phase 17 foundation candidate; original phase numbering retained
infra/          # deployment decisions are explicitly deferred
```

Local Docker support may be introduced during backend implementation when PostgreSQL/object-storage development environments are actually needed; it does not require freezing a deployment topology now.

## Related architecture decisions

- [`ADR-0001`](../adr/0001-modular-monolith-with-spring-modulith.md) — modular monolith.
- [`ADR-0002`](../adr/0002-package-by-business-capability.md) — package by business capability.
- [`ADR-0015`](../adr/0015-semantic-public-api-subpackages.md) — semantic public API subpackages and named interfaces.
- [`ADR-0016`](../adr/0016-root-http-contract-module-local-adapters.md) — root HTTP contract and module-local web adapters.
- [`ADR-0017`](../adr/0017-portability-readonly-snapshot-module.md) — implemented portability leaf and narrow read-only JDBC snapshot exception.

## Phase 17 candidate package and tooling inventory

The additive `rag` leaf exposes `api` and `source`; its internals contain application authority/graph/grants/finalization/purge/recovery/runtime budgets, domain state, persistence, bounded loopback client and module-local HTTP adapters. All 12 RAG package descriptors remain present (nine internal packages plus root, `api` and `source`; FR17-11 preserved). Fiction/Film `source` named interfaces supply fresh immutable projections; Vault `entry` supplies transaction guards and fresh views. No owner imports RAG internals.

`rag-engine/src/ppv_rag` contains strict versioned contracts, service authentication, raw transport bounds, restricted psycopg staging and one controlled synthetic worker. Tests and verification scripts live beneath `rag-engine/tests` and `rag-engine/scripts`. `uv.lock` and `.python-version` enforce the measured environment.

`backend/dev/rag-db` supplies a separate provisioning process; `backend/dev/rag-postgres` builds a digest/source-pinned PostgreSQL 18.6 / pgvector 0.8.6 development image. `backend/compose.rag.dev.yml` is local database tooling, not deployment authorization. `rag-engine/Dockerfile.offline-check` is a runtime isolation verification fixture. Generated logs, targets, environments, graphs and caches are excluded from delivery. Phase 16 and Phases 18–22 retain their separate gates.
- [`ADR-0019`](../adr/0019-local-rag-before-frontend.md) — accepted RAG-first sequence and bounded Phase 17 owner integration.


## Approved Alternative B contract inventory (2026-10-15; pending implementation)

[ADR-0020](../adr/0020-phase-17-single-job-reservations-e1.md) retains the existing
rag leaf/api/source/internal boundaries and all 12 package descriptors (FR17-11).
Required additions stay in existing owners: vault.entry immutable source versions/
edit-reservation API, Vault internal source-state persistence, Fiction/Film guard/
projection integration, RAG policy/job/grant/finalization/recovery/purge changes
and one internal Windows process-tree adapter. Python retains typed loopback
FastAPI contracts and restricted stage path, hosted once per job. No new shared,
Vault supervisor/DRAIN/global fence/ticket engine or speculative events package.
SQL backstops and additive fields are catalogued in the separate Phase 17 DBML;
future migrations append after V13. Listed additions are contracts, not files
created by synchronization. Current runtime remains contained and unaccepted.

### Phase 18 additive candidate inventory — Codex takeover 2026-10-18

The existing RAG application package owns `RagDocumentBatchService`,
`RagDocumentInput` (canonical issued input), `RagDocumentFinalizationGate`,
`RagDocumentReportService` and `RagExternalFileAuthority`. Existing authority,
CURRENT, supervisor, recovery and purge components are reused. Batch/outcome,
generation-root, chunk and staged-document entities/repositories remain internal.
V18 adds RAG-owned document records; V19 requires pgvector and extends terminal
scrubbing only for independently manifested v3 generations. Module-local web
DTO/controller/mapper ownership and the dependency graph are unchanged.

Python `parsers`, `chunking`, `embeddings`, `ocr` and `telemetry` packages hold the
contained v3 pipeline. PDF geometry/native cells use pdfplumber 0.11.10; detected
raster tables reject pending a verified cell pipeline. New synthetic PDF geometry
tests and a hashed two-family OCR diagnostic fixture set live under worker tests.
This is an inventory of partial implementation, not complete format/language,
resource, external-file/R7 or P18-FINAL acceptance.
