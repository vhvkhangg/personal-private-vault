# Module Dependency Matrix v1 — Frozen baseline + implemented Phase 14 delta

An arrow `A → B` means module **A is allowed to depend on the public API of B**. Direct access to another module's internal entities/repositories remains forbidden.

At owner commit `3bb3f2e`, all 19 top-level modules shown below are implemented. `portability` is the frozen Phase 14 leaf; it adds no Java module dependency edge and retains only ADR-0017's narrow read-only JDBC snapshot exception.

| Module           | Allowed dependencies                                                                                    |
| ---------------- | ------------------------------------------------------------------------------------------------------- |
| `authentication` | —                                                                                                       |
| `settings`       | `reference`                                                                                             |
| `reference`      | —                                                                                                       |
| `vault`          | —                                                                                                       |
| `people`         | `vault`, `reference`                                                                                    |
| `fiction`        | `vault`, `people`, `reference`                                                                          |
| `film`           | `vault`, `people`, `reference`                                                                          |
| `media`          | `vault`                                                                                                 |
| `location`       | `vault`, `reference`                                                                                    |
| `knowledge`      | `vault`, `people`, `reference`, `account`                                                               |
| `collection`     | `vault`, `people`, `reference`                                                                          |
| `account`        | `vault`, `reference`                                                                                    |
| `feed`           | `vault`, `knowledge`                                                                                    |
| `importdata`     | `vault`, `knowledge`                                                                                    |
| `finance`        | `reference`                                                                                             |
| `journal`        | —                                                                                                       |
| `personal`       | `reference`, `location`                                                                                 |
| `search`         | `vault`, `people`, `fiction`, `film`, `media`, `location`, `knowledge`, `collection`, `account`, `feed` |
| `portability`    | —                                                                                                       |

## Cross-domain rules

- `search` is an orchestration/leaf module. No business module depends on `search`.
- `portability` is a read-only leaf module with no business-module dependencies. ADR-0017 grants only its narrowly
  scoped JDBC snapshot exception; it owns no business table and performs no cross-module writes.
- `vault` and `reference` are foundation modules and do not depend on feature modules.
- `reference` owns globally shared reference/lookup data, including country/language/currency/platform data and narrative taxonomies shared by both fiction and film.
- `journal` and `finance` remain independent even if a future calendar UI aggregates both.
- `feed` and `importdata` may use the public `knowledge` API; they do not access nested repositories directly.
- Synchronous public APIs are used when an immediate result is required; events are used for cross-module side effects.
