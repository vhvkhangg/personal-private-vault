# BF26-DP-B1 API Contract Specification

- **Date:** 2026-11-12
- **Scope:** BF26-DP-B1 Read-Only Surface (8 GET Endpoints)
- **Status:** IMPLEMENTED — VERIFIED AGAINST PRODUCTION AND ARCHITECTURE GATES
- **Authority:** Validated persisted `PROFILE_PUBLIC` or `PROFILE_PRIVATE` bearer JWT token. Anonymous access is strictly denied (401).

---

## 1. Route Inventory and Authorization Matrix

The exact 8 endpoints accessible to `PROFILE_PUBLIC` (and retaining access for `PROFILE_PRIVATE`):

| Method | Route | Public Access | Private Access | Anonymous Access | Public Response DTO |
|---|---|---|---|---|---|
| `GET` | `/api/v1/fictions/{id}` | `200` / `404` | `200` / `404` | `401` | `PublicFictionResponse` (6 fields) |
| `GET` | `/api/v1/films/{id}` | `200` / `404` | `200` / `404` | `401` | `PublicFilmResponse` (7 fields) |
| `GET` | `/api/v1/reference/countries` | `200` | `200` | `401` | `List<CountryResponse>` |
| `GET` | `/api/v1/reference/countries/{code}` | `200` / `404` | `200` / `404` | `401` | `CountryResponse` |
| `GET` | `/api/v1/reference/languages` | `200` | `200` | `401` | `List<LanguageResponse>` |
| `GET` | `/api/v1/reference/languages/{code}` | `200` / `404` | `200` / `404` | `401` | `LanguageResponse` |
| `GET` | `/api/v1/reference/currencies` | `200` | `200` | `401` | `List<CurrencyResponse>` |
| `GET` | `/api/v1/reference/currencies/{code}` | `200` / `404` | `200` / `404` | `401` | `CurrencyResponse` |

All other registered business and data routes (mutations, collections, notes, accounts, search, feeds, personal, finance, journal, and the six unselected Reference routes: `/api/v1/reference/platforms`, `/api/v1/reference/platforms/{id}`, `/api/v1/reference/story-archetypes`, `/api/v1/reference/story-archetypes/{id}`, `/api/v1/reference/world-settings`, `/api/v1/reference/world-settings/{id}`) return `403 FORBIDDEN` to `PROFILE_PUBLIC`. Auth lifecycle routes retain their separate DP-A security rules (e.g., `/api/v1/auth/sessions/elevate-private` permits elevation from `PROFILE_PUBLIC` to `PROFILE_PRIVATE` with private password; `/api/v1/auth/sessions/downgrade-public` remains `PROFILE_PRIVATE`-only; unauthenticated entrypoints permit initial authentication).

---

## 2. Public Response Schemas

### 2.1 `PublicFictionResponse`
Bounded to exactly 6 scalar fields, strictly matching `PublicFictionView` and `additionalProperties = false`:

```json
{
  "id": 123456789,
  "title": "Clean Fiction Title",
  "originalTitle": "Original Title or null",
  "nationalityCode": "VN",
  "format": "NOVEL",
  "isNsfw": false
}
```

Forbidden fields in Public Fiction response:
- `posterUrl`, `description`, `genreId`, `authorPersonId`, `authorGroupId`, `totalChapters`, `progressStatus`, `consumptionStatus`, `currentProgressText`, `review`, `genres`, `authors`, `tags`, `links`, `rating`.

### 2.2 `PublicFilmResponse`
Bounded to exactly 7 scalar fields, strictly matching `PublicFilmView` and `additionalProperties = false`:

```json
{
  "id": 123456789,
  "title": "Clean Film Title",
  "originalTitle": "Original Film Title or null",
  "nationalityCode": "VN",
  "format": "MOVIE",
  "productionStyle": "LIVE_ACTION",
  "isNsfw": false
}
```

Forbidden fields in Public Film response:
- `posterUrl`, `description`, `directorPersonId`, `totalEpisodes`, `progressStatus`, `consumptionStatus`, `currentProgressText`, `review`, `genres`, `directors`, `credits`, `tags`, `links`, `rating`.

### 2.3 Reference Endpoints
Reference endpoints return the existing, unmodified DTOs:
- `CountryResponse`: `code` (ISO 3166-1 alpha-2), `nameEn`, `nameVi`
- `LanguageResponse`: `code` (ISO 639-1), `nameEn`, `nameVi`
- `CurrencyResponse`: `code` (ISO 4217 alpha-3), `name`, `symbol`, `decimalPlaces`

---

## 3. Caching and Security Headers

### Public GET `/api/v1/fictions/{id}` and `/api/v1/films/{id}`
- **Cache-Control:** Explicit `Cache-Control: no-store` header applied on both `200 OK` and `404 NOT_FOUND` responses.
- **Leakage Prevention:** Prevents browser, intermediate proxy, and CDN caching of public data or negative 404 responses.

### Private GET `/api/v1/fictions/{id}` and `/api/v1/films/{id}`
- Retains default Spring Security caching headers (`Cache-Control: no-cache, no-store, max-age=0, must-revalidate`). The controller does not inject duplicate or conflicting headers.

### Reference GET Endpoints
- Retains default caching behavior.

---

## 4. Error Handling and Negative Invariants

For public profile requests to `/api/v1/fictions/{id}` and `/api/v1/films/{id}`:
- Any non-qualifying entity returns **indistinguishable `404 NOT_FOUND`** with body:
  - Fiction: `ApiResponse.error("FICTION_NOT_FOUND", "Fiction not found")`
  - Film: `ApiResponse.error("FILM_NOT_FOUND", "Film not found")`
- Non-qualifying conditions that trigger this identical 404:
  1. Record does not exist in `fictions` / `films` table.
  2. `is_nsfw == true` in database.
  3. `deleted_at IS NOT NULL` in `vault_entries` (trashed entity).
  4. Corresponding `vault_entries` record does not exist.
  5. Corresponding `vault_entries` has mismatching `entry_type` (e.g. `PERSON` or opposite entity type).
- No sensitive metadata, NSFW flags, or deletion markers are leaked via error messages, status codes, or response headers.

---

## 5. OpenAPI Specification Contracts

- **Security Schemes:** `bearerAuth` OpenAPI description documents the exact 8 allowed endpoints for `PROFILE_PUBLIC`, explicitly stating that all mutations, private business routes, unselected reference routes, and anonymous requests are denied. Elevation to `PROFILE_PRIVATE` is permitted via `/api/v1/auth/sessions/elevate-private`, while session downgrade remains `PROFILE_PRIVATE`-only.
- **Component Schemas:**
  - `PublicFictionResponse`
  - `PublicFilmResponse`
  - `ApiResponsePublicFictionResponse`
  - `ApiResponsePublicFilmResponse`
  - `ApiResponseFictionDetailResponse` (`anyOf: [ApiResponsePublicFictionResponse, ApiResponseFictionResponse]`)
  - `ApiResponseFilmDetailResponse` (`anyOf: [ApiResponsePublicFilmResponse, ApiResponseFilmResponse]`)
- **Route Customizer:**
  - `GET /api/v1/fictions/{id}` response 200 references `ApiResponseFictionDetailResponse`.
  - `GET /api/v1/films/{id}` response 200 references `ApiResponseFilmDetailResponse`.
- **Accepted Manifest:**
  - Verified by `OpenApiRouteInventoryIntegrationTest` covering route counts, manifest alignment, schema properties, and required keys.
