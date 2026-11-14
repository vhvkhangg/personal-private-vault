# BF26-DP-RO-B — binding public-detail contract and private compatibility

**Authority:** [Owner's RO-D04 and RO-D06 ratification (2026-11-13)](../2026-11-13-bf26-dp-public-read-only-preparation/owner-ratification-ro-d04-d06-2026-11-13.md) and the read-only policy; independently accepted [RO-A closeout](../2026-11-13-bf26-dp-public-read-only-preparation/owner-publication-closeout-dp-ro-a-2026-11-14.md). These are **binding product/wire decisions**. This file does **not** authorize their source implementation.

## Exact successful PUBLIC `ApiResponse.data` shape

The successful PUBLIC Fiction object has exactly **19** top-level fields:

`id, title, originalTitle, nationalityCode, posterUrl, format, isNsfw, genreId, authorPersonId, authorGroupId, description, totalChapters, progressStatus, consumptionStatus, currentProgressText, review, storyArchetypeIds, worldSettingIds, authorName`.

The successful PUBLIC Film object has exactly **19** top-level fields:

`id, title, originalTitle, nationalityCode, posterUrl, format, productionStyle, isNsfw, directorPersonId, description, totalEpisodes, progressStatus, consumptionStatus, currentProgressText, review, genreIds, storyArchetypeIds, worldSettingIds, directorName`.

The first **18** fields, their values and semantics match the current full `FictionResponse`/`FilmResponse` for a coherent snapshot of the *same eligible work*. No truncation of `review`, progress/status, IDs, descriptions or classification sets for PUBLIC; no Vault rating/tag, Person/Group object, credits, members or transitive resource fields are silently added. `isNsfw` is **false** on every successful PUBLIC work detail.

`authorName` and `directorName` are **always present JSON properties**, type `string | null`. If a name is unavailable under the authorized null rules, serialize **explicit `null`** (not omit the key); a feature-local field-level inclusion rule may be needed if the configured JSON inclusion policy would omit nulls. Do not change the global ObjectMapper. The canonical `ApiResponse` envelope (`data`, `error`, `meta`) remains unchanged.

**PRIVATE compatibility:** existing `FictionResponse` and `FilmResponse` each remain **exactly 18 fields** across PRIVATE GET/POST/PUT, without `authorName` or `directorName`, with unchanged values, exceptions, headers, validation, status and generated PRIVATE schema. Do not add nullable names to the shared PRIVATE DTO merely because the front end could ignore them. Prefer PUBLIC-only response classes or a proven isolated public serializer contract.

## Work qualification precedes linked names

For either PUBLIC GET, the **owning Fiction/Film service** must authorize work eligibility *before any People/Group name lookup*: (1) work exists and is `isNsfw=false`; (2) Vault identity exists, equals work ID, matches `VaultEntryType.FICTION` / `FILM`; and (3) that Vault entry is active (`deletedAt == null`). Use the preexisting feature-owned `REQUIRES_NEW / REPEATABLE_READ` read-only transaction or an **independently proven equivalent**. No controller `findPublic(id) → find(id)` or second-transaction enrichment. No late unqualified `find()` or DTO assembly after the transaction has committed.

If work is absent, NSFW, trashed, wrong type or has missing/mismatched Vault identity, PUBLIC sees the **same safe canonical 404** (`FICTION_NOT_FOUND` or `FILM_NOT_FOUND`) with `Cache-Control: no-store`; no 19-field data body and **zero linked People/Group name queries**. The 404 should not reveal which eligibility condition failed. The accepted B1 D10 model allows a request admitted to a *coherent earlier eligible snapshot* to complete; it does not promise retroactive cancellation. A new request after committed ineligibility must deny.

## Name-source and null/error rules, already approved

| Link case on an otherwise eligible work | Mandatory `authorName` / `directorName` result |
| --- | --- |
| Fiction `authorPersonId` points to an existing, active correct-type `PERSON` Vault identity and Person record with nonblank name | The scalar `PersonView.name()` only |
| Fiction `authorGroupId` points to an existing CreatorGroup row with nonblank name | The scalar `CreatorGroupView.name()` only |
| Film `directorPersonId` points to an existing, active correct-type `PERSON` Vault identity and Person record with nonblank name | The scalar `PersonView.name()` only |
| Film director ID missing | `directorName: null` |
| Person row absent, Person Vault row missing, trashed, wrong `PERSON` type, or mismatched identity | Name key **present** and value `null` |
| CreatorGroup row absent (CreatorGroup is **not** Vault-backed); optional/legacy relation unavailable | Name key **present** and value `null` |
| Name absent or blank | Name key **present** and value `null` |
| Genuine DB/lookup/transaction/infrastructure failure, rather than a normal missing/ineligible reference | **Canonical safe HTTP 500**, `ApiResponse.data = null`, `Cache-Control: no-store`; no partial work fields, no Person/Group identifiers or internal exception leak |

Fiction has exactly one domain author source (`authorPersonId` **XOR** `authorGroupId`). Null-name behavior for a broken legacy reference is **not** a relaxation of Fiction creation/update invariants. Do not invent CreatorGroup trash/type checks. Treat exception-causing repository/transaction failures as failures, not absence. Never expose the complete in-process `PersonView` or `CreatorGroupView` through the PUBLIC API or logs. The permitted scalar name in a qualifying Fiction/Film does **not** grant a direct PUBLIC People/CreatorGroup endpoint.

### Snapshot and concurrency (mandatory technical proof later)

Within one physical owner-qualified read snapshot, read eligible work, its **all 18 values**, required classification ID sets, correct-type active Person Vault eligibility (if a Person source), and only the selected author/director name. Called named People/Group/Vault capabilities must *participate* in that physical snapshot or an alternative must be demonstrably equivalent on actual PostgreSQL. Avoid mixing state across separate transactions or reading a newly NSFW/trashed Person after a stale work snapshot. Missing Person/Group returns null under the same point-in-time rules; SQL outage does not become null.

Prove competing commits that change NSFW, work Vault trash/type, the work review/progress/classification sets, Person Vault state, and name source content (including replacement/removal) cannot produce an incoherent mixed-state `200`. Do not present isolation annotations alone as evidence. Do not treat read-only transactions as immutable snapshots without an actual database witness.

## Auth, errors, OpenAPI and exclusions

The eligible endpoints are only authenticated `GET /api/v1/fictions/{id}` and `GET /api/v1/films/{id}`, counted within the existing **eight** PUBLIC business GET method/path pairs. Both real persisted PUBLIC origins and PRIVATE must retain their accepted auth semantics. Public success **200/no-store**, hidden/absent **404/no-store**, genuine failure **500/no-store/data:null**; unauthenticated remains 401, unselected PUBLIC business requests remain 403. Conditional ETag/304 must not bypass no-store or mix profile representations.

Generated `/v3/api-docs` must present exact PUBLIC 19 properties with `authorName`/`directorName` **required (present) and nullable**, PRIVATE 18 unchanged; profile-specific envelopes and both HTTP specimens must reconcile. Do not assume `oneOf` exclusivity if schemas overlap; use a truthful `anyOf` or demonstrate exclusivity. Preserve `getFiction`/`getFilm`, other operation IDs, PRIVATE POST/PUT, RO-A denied POST, Reference DTOs, and B1 grants.

**Excluded:** direct PUBLIC People/CreatorGroup reads; lists/search/discovery; generic Vault, nested classifications/links/credits; additional modules; NSFW; all Public writes; Frontend/BFF, RAG, providers, DP-C, Recycle Bin and deployment. No new auth/flag/schema/index/migration/module edge is authorized by preparation.
