# DP-B1 — Owner-ratified Option A exact Public method, field and status policy (prospective)

**Status:** `OWNER_RATIFIED_D7–D11_OPTION_A / INDEPENDENT_REVIEW_PENDING / NO_SOURCE_PERMISSION`. See [dated Owner ratification](owner-ratification-d7-d11-2026-11-12.md); applied only after independent pre-handoff readiness, separate frozen/source authorization and accepted implementation. Current DP-A on `6374537074afc32ed7e939cf94ed2f21d77c5f42` still has **PUBLIC DENY on every business/data route**.

## Owner-ratified B1 HTTP allowlist (prospective exact methods, no anonymous access)

Legend: `PUBLIC` / `PRIVATE` = valid, non-expired DP-A signed profile with persisted active session, generation and epoch. All routes retain Spring security validation. `ANON` is not a Public profile.

| HTTP route on current controllers | ANON | PUBLIC after B1 | PRIVATE after B1 | Body/meaning |
| --- | --- | --- | --- | --- |
| `GET /api/v1/fictions/{id}` | 401 | 200 safe Fiction DTO or 404 hidden/absent | Existing `200 FictionResponse` or existing 404 | Exact one-segment detail path only |
| `GET /api/v1/films/{id}` | 401 | 200 safe Film DTO or 404 hidden/absent | Existing `200 FilmResponse` or existing 404 | No nested credits/links |
| `GET /api/v1/reference/countries` | 401 | 200 `List<CountryResponse>` | Existing 200 | No new filtering/visibility schema |
| `GET /api/v1/reference/countries/{code}` | 401 | Existing 200 or existing missing-code 404 | Existing behavior | Code-specific lookup |
| `GET /api/v1/reference/languages` | 401 | 200 `List<LanguageResponse>` | Existing 200 | Existing response fields |
| `GET /api/v1/reference/languages/{code}` | 401 | Existing 200 or existing missing-code 404 | Existing behavior | Code-specific lookup |
| `GET /api/v1/reference/currencies` | 401 | 200 `List<CurrencyResponse>` | Existing 200 | Existing response fields |
| `GET /api/v1/reference/currencies/{code}` | 401 | Existing 200 or existing missing-code 404 | Existing behavior | Code-specific lookup |

`401` for an anonymous request above is a proposed authenticated-B1 result, not a change to the previously accepted anonymous Auth/bootstrap/health routes. Header/cache requirements are in [visibility contract](visibility-consistency-and-security.md).

### Explicit PUBLIC deny groups — preexisting handlers not selected

| Path family | Public result | Why excluded |
| --- | --- | --- |
| `/api/v1/fictions/{id}/story-archetypes`, `/world-settings`, `/classifications`, `/links`, `/links/{linkId}` | 403 | Relation IDs, link labels/URLs and hidden-parent oracle; no B1 projection |
| `/api/v1/films/{id}/genres`, `/story-archetypes`, `/world-settings`, `/classifications`, `/credits`, `/links`, `/links/{linkId}` | 403 | Hidden relation/genre/People IDs, credits and link metadata |
| `/api/v1/films/credits/{creditId}` | 403 | **Direct credit ID bypass** of parent; must not be opened by generic GET |
| `/api/v1/fiction-genres/**`, `/api/v1/film-genres/**` | 403 | Owner-maintained taxonomy with free-text descriptions; no B1 grant |
| `/api/v1/reference/platforms`, `/platforms/{id}` | 403 | `url`, `createdAt`, possible Owner-local platform metadata |
| `/api/v1/reference/story-archetypes`, `/story-archetypes/{id}`, `/world-settings`, `/world-settings/{id}` | 403 | Free-text descriptions and taxonomy context not yet qualified |
| All POST, PUT, PATCH, DELETE, unselected HEAD/OPTIONS, all unknown/unclassified/async/ERROR business paths | 403 for valid PUBLIC, unless an existing more-specific infrastructure contract applies | DP-A default-deny remains in force |
| `/api/v1/search/**`, `/api/v1/vault/**`, `/api/v1/portability/**`, Media/People/Personal/Drive/Feed and all other modules | 403 | No B1 business grant; no reverse dependency, counts, export, bytes or metadata |

No Public GET wildcard may accidentally include nested paths. `PRIVATE` retains preexisting method semantics; unrecognized PRIVATE paths still use ordinary 404/405 behavior. Unexpected method handling/dispatch errors must not turn an intended Public 403 into side effects or a private payload.

## Proposed dedicated Public DTOs — property allowlist, not redaction

| Field | `PublicFictionResponse` | `PublicFilmResponse` | Source/decision |
| --- | --- | --- | --- |
| `id` | Long | Long | Feature/Vault shared ID, direct detail entrypoint |
| `title` | String | String | Minimum public catalog title under D1's non-NSFW assumption |
| `originalTitle` | String? | String? | Alternative title; optional; do not derive from notes |
| `nationalityCode` | String? | String? | ISO-like reference code only; not owner's Location metadata |
| `format` | `FictionFormat` | `FilmFormat` | Owner feature enum, no private relation |
| `productionStyle` | **ABSENT** | `FilmProductionStyle` | Film standalone enum |
| `isNsfw` | `false` | `false` | Qualified server-side; never trust client selector |

**Every other current full DTO field is ABSENT, not `null`:** `posterUrl`, `authorPersonId`, `authorGroupId`, `directorPersonId`, `genreId`, `genreIds`, `storyArchetypeIds`, `worldSettingIds`, `description`, `review`, `consumptionStatus`, `progressStatus`, `currentProgressText`, `totalChapters`, `totalEpisodes`, and any future newly added internal metadata. No credits, external links, related IDs/URLs, pre-signed media URL, thumbnail, user notes or embedded objects. PRIVATE continues using its unchanged full DTO and field names.

**Non-normative prospective JSON example — Fiction** (for a qualifying synthetic row):

```json
{"data":{"id":41,"title":"Synthetic work","originalTitle":null,"nationalityCode":"VN","format":"NOVEL","isNsfw":false},"error":null,"meta":null}
```

**Film** (for a qualifying synthetic row):

```json
{"data":{"id":52,"title":"Synthetic film","originalTitle":null,"nationalityCode":"VN","format":"MOVIE","productionStyle":"LIVE_ACTION","isNsfw":false},"error":null,"meta":null}
```

These examples are **prospective** and use illustrative enum values; future implementation tests must verify actual enum spellings on HEAD. Only nullability of the **allowed optional fields** is shown; forbidden keys must not appear at all. Use the canonical `ApiResponse<T>` envelope; do not invent alternative transport wrappers.

## Reference DTO allowlist

- Countries: `code,nameEn,nameVi`.
- Languages: `code,nameEn,nameVi`.
- Currencies: `code,name,symbol,decimalPlaces`.

These DTOs already exist; proposed B1 changes only exactly their six method-level profile permissions, not response schemas or contents. List ordering, 404 error codes, missing values and existing validation must match PRIVATE behavior. No new anonymous catalog service or import/export endpoint.

## HTTP compatibility obligations

- A single existing direct detail route may return **two deliberately distinct schema variants keyed by the validated server profile**. New B1 docs/OpenAPI must accurately express the PUBLIC DTO and preserved PRIVATE DTO; do not claim both return `FictionResponse` or silently broaden that class.
- The Spring request matcher must enumerate **exact GET method/path** and require authenticated `PROFILE_PUBLIC` OR `PROFILE_PRIVATE` (not `permitAll`). Keep all other handlers `PROFILE_PRIVATE`-only and the fallback default DENY. Reject HEAD/OPTIONS and async/ERROR unintended escalation; preserve existing infrastructure/health/Auth exceptions exactly.
- Forbidden/unclassified Public routes: 403 before invoking handlers (including request bodies invalid for mutations). For approved detail GET with a valid PUBLIC session but hidden/absent/wrong-type/trashed ID: identical 404 `FICTION_NOT_FOUND` or `FILM_NOT_FOUND` error, with no distinguishing metadata. No blanket `/api/v1/reference/**` permission.
- Existing PRIVATE DTO wire shape, semantics, error codes, retained PIN/Auth/Google behavior and other modules must remain unchanged. OpenAPI 401/403/404 examples must be profile-aware and must not incorrectly imply PUBLIC access to nested routes.
