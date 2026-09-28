# Module Boundaries v1 — Frozen

## 1. Boundary rule

Each business capability is owned by one Spring Modulith application module. Only the owning module may directly access its internal repositories and entities.

The base package owns the module descriptor. Non-trivial public contracts are grouped into semantic subpackages and explicitly exposed with Spring Modulith `@NamedInterface`; implementation stays under `internal/`.

## 2. Top-level modules

| Module | Responsibility | Primary table ownership | Allowed dependencies |
|---|---|---|---|
| `authentication` | One-time bootstrap, login, password, JWT access/refresh lifecycle | `app_users`, `refresh_tokens` | — |
| `settings` | Application-level settings | `app_settings` | `reference` |
| `reference` | Stable cross-domain reference/lookup data | `countries`, `languages`, `currencies`, `platforms`, `story_archetypes`, `world_settings` | — |
| `vault` | Shared content identity, favorites, ratings, tags, recycle bin | `vault_entries`, `favorites`, `ratings`, `tags`, `vault_entry_tags` | — |
| `people` | People, creator roles/groups | `persons`, `person_roles`, `creator_groups`, `creator_group_members` | `vault`, `reference` |
| `fiction` | Fiction library and fiction-specific classification/linking | `fictions`, `fiction_genres`, `fiction_story_archetypes`, `fiction_world_settings`, `fiction_links` | `vault`, `people`, `reference` |
| `film` | Film library, film classification, film credits | `films`, `film_genres`, `film_genre_assignments`, `film_story_archetypes`, `film_world_settings`, `film_links`, `film_credits` | `vault`, `people`, `reference` |
| `media` | Albums and media metadata | `albums`, `images` | `vault` |
| `location` | Brands, physical locations, addresses, categories, business hours | `brands`, `location_categories`, `addresses`, `locations`, `location_category_assignments`, `location_dining_service_styles`, `location_business_hours` | `vault`, `reference` |
| `knowledge` | Parent facade for study/information/vocabulary/note | nested-module tables | `vault`, `people`, `reference`, `account` |
| `collection` | Parent facade for music/shopping/software | nested-module tables | `vault`, `people`, `reference` |
| `account` | External/social/game/YouTube accounts, follow relationships, follower history | `external_accounts`, `external_account_relationships`, `follower_snapshots`, `follower_snapshot_entries` | `vault`, `reference` |
| `feed` | Public feeds, feed items, saved resources and conversions | `feed_sources`, `feed_items`, `saved_resources`, `saved_resource_conversions` | `vault`, `knowledge` |
| `importdata` | Import job state and import orchestration | `import_jobs`, `import_job_items` | `vault`, `knowledge` |
| `finance` | Wallets, ledger transactions, recurring rules, subscriptions | finance tables | `reference` |
| `journal` | Diary entries | `diary_entries` | — |
| `personal` | Structured personal/family profiles | `personal_profiles` | `reference`, `location` |
| `search` | Cross-module global search orchestration | no v1 tables | `vault`, `people`, `fiction`, `film`, `media`, `location`, `knowledge`, `collection`, `account`, `feed` |

The frozen dependency matrix remains canonical at [`module-dependency-matrix.md`](module-dependency-matrix.md).

## 3. Nested modules

### `knowledge`

```text
knowledge
├── study
├── information
├── vocabulary
└── note
```

| Nested module | Tables |
|---|---|
| `study` | `study_items` |
| `information` | `information_items` |
| `vocabulary` | `vocabulary_items`, `vocabulary_reviews` |
| `note` | `notes` |

External top-level modules depend on the public `knowledge` facade rather than directly accessing nested repositories.

### `collection`

```text
collection
├── music
├── shopping
└── software
```

| Nested module | Tables |
|---|---|
| `music` | `music_tracks`, `music_track_people` |
| `shopping` | `shopping_items` |
| `software` | `software_items`, `software_item_platforms` |

## 4. Public contract rule

Example module shape:

```text
reference/
├── package-info.java                 # @ApplicationModule
├── catalog/                          # @NamedInterface("catalog")
│   ├── package-info.java
│   └── ReferenceCatalog.java
├── view/                             # @NamedInterface("view")
│   ├── package-info.java
│   └── *View.java
├── enums/                            # @NamedInterface("enums")
│   ├── package-info.java
│   └── PlatformKind.java
└── internal/
    ├── application/
    │   └── catalog/
    ├── domain/
    └── infrastructure/persistence/
```

Allowed cross-module access targets only exposed named interfaces or base-package types intentionally retained as
public API. Internal packages remain forbidden.

Public capability interfaces use domain-oriented names. Do not introduce `Service` / `ServiceImpl` pairs merely
for layering convention.

## 5. Named interfaces

Use Spring Modulith named interfaces for semantic public API subpackages that genuinely need cross-module exposure (capabilities, read models, stable enums, or events). Keep the exposure narrow; do not make a module open for convenience.

## 6. Foundation-module direction

`vault` and `reference` are dependency foundations. They do not call feature modules.

Global search is therefore not part of `vault`; `search` is a separate orchestration module with outgoing dependencies to searchable features.

## 7. Cross-domain examples

- `film` validates a person through the public `people` contract, not `PersonRepository`.
- `study` may reference an external YouTube channel through the public `account` contract.
- `personal` may reference a physical address owned by `location` through a public contract.
- `feed` converts a saved resource through public `knowledge`/`vault` contracts.
- `journal` and `finance` remain independent even if a future calendar view combines them.

## 8. Verification

When implementation begins, Spring Modulith verification should enforce:

- no cyclic application-module dependencies;
- no access to another module's internals;
- declared allowed-dependency rules;
- nested module encapsulation.

An architecture verification test belongs in the backend test suite and is written/run under the agreed Antigravity testing workflow.
