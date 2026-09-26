# Module Dependency Matrix v1 — Frozen

An arrow `A → B` means module **A is allowed to depend on the public API of B**. Direct access to another module's internal entities/repositories remains forbidden.

| Module | Allowed dependencies |
|---|---|
| `authentication` | — |
| `settings` | `reference` |
| `reference` | — |
| `vault` | — |
| `people` | `vault`, `reference` |
| `fiction` | `vault`, `people`, `reference` |
| `film` | `vault`, `people`, `reference` |
| `media` | `vault` |
| `location` | `vault`, `reference` |
| `knowledge` | `vault`, `people`, `reference`, `account` |
| `collection` | `vault`, `people`, `reference` |
| `account` | `vault`, `reference` |
| `feed` | `vault`, `knowledge` |
| `importdata` | `vault`, `knowledge` |
| `finance` | `reference` |
| `journal` | — |
| `personal` | `reference`, `location` |
| `search` | `vault`, `people`, `fiction`, `film`, `media`, `location`, `knowledge`, `collection`, `account`, `feed` |

## Cross-domain rules

- `search` is an orchestration/leaf module. No business module depends on `search`.
- `vault` and `reference` are foundation modules and do not depend on feature modules.
- `journal` and `finance` remain independent even if a future calendar UI aggregates both.
- `feed` and `importdata` may use the public `knowledge` API; they do not access nested repositories directly.
- Synchronous public APIs are used when an immediate result is required; events are used for cross-module side effects.