# BF26-DP-B2 — Source-qualified static inventory and risk boundaries

**Source:** connected GitHub `main` at `9007f212adc91f7c1b5e1a5c466c8a102c13fff3`, inspected 2026-11-12. The SHA values below are Git blob IDs, not test receipt hashes. **No local runtime, registered Spring mappings, PostgreSQL transactions, OpenAPI generation or security tests were executed here.**

Source paths in the table are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.

## Observed HTTP entrypoints

| Path / Git blob | Relevant declared routes | Implication |
| --- | --- | --- |
| `fiction/internal/web/controller/FictionController.java` (`20a706c8c36004899689b8eb8551f1f13ce7d457`) | `POST /api/v1/fictions`, `PUT /api/v1/fictions/{id}`, nested classification and link operations | B1 only opened its `GET /{id}`; Public writes and nested paths remain denied |
| `film/internal/web/controller/FilmController.java` (`89200df76ccfbe09d4d2d3da30f2867efea4c17b`) | `POST /api/v1/films`, `PUT /api/v1/films/{id}`, genre/classification, credit, link operations | B1 only opened `GET /{id}`; direct `/credits/{creditId}` must not be unlocked by generic film permissions |
| `search/internal/web/controller/GlobalSearchController.java` (`d35dc1e6ba724d6cae038bf0dc046d7bddfdfa91`) | `GET /api/v1/search`, `q`, `domain`, `entryType`, `tagId`, `offset`, `limit` | Existing global entrypoint is NOT a Public-qualified Fiction/Film-only search |
| `vault/internal/web/controller/VaultController.java` (`de14f47df09f3ac1726d3c8655663eaf5c8c42e5`) | entry detail/trash/restore, metadata, favorites, ratings, tag create/attach/detach | Generic Vault ID and tag operations cross sensitive domains; keep denied until exact qualifications |
| `fiction/internal/web/controller/FictionGenreController.java` (`d6f5d5cd1e159cc0d70d03f7394b8a58f95d1535`), `film/internal/web/controller/FilmGenreController.java` (`58eb2f3f0ea3549798203f90603c4083f070a9be`) | genre ID/name reads and writes | Owner-managed taxonomy is not automatically Public even if a Film/Fiction is non-NSFW |

These are **static controller declarations**, not a complete live route inventory. Actual B2 handoff must reconcile registered methods, dispatcher types and conditional handlers with the accepted DP-A/B1 deny sweep.

## Request/domain constraints that change the engineering plan

- `fiction/internal/web/dto/CreateFictionRequest.java` (`8089134b906f561f08700449c8058784dd9fe761`) and `UpdateFictionRequest.java` (`f0c9fae36c6abe5179a3399f4a478c93d11d0d1f`) require `genreId`; both include `authorPersonId` and `authorGroupId`. `FictionService.java` (`61827a8280d8126f5fd000aaa5e90707c25463b4`) **requires exactly one author source (XOR)** and calls People/Creator Group capabilities. Therefore a supposedly simple Public Fiction create/update would require a separately approved way to choose/qualify these references. Reusing full requests may leak reference existence or accept unqualified private links.
- `film/internal/web/dto/CreateFilmRequest.java` (`ccd698858b7e64894642ad739e958efd417f7445`) requires `title`, `format`, `productionStyle` and permits optional `directorPersonId`, genres, archetypes, world settings, poster, description, review and personal progress fields. `FilmService.java` (`6d9f8de7b30238a911ab137e2d2d8fd65e3c9245`) validates director via People only when supplied. **Inference:** a strictly restricted Film create with none of these optional links is a smaller first write boundary. This is a design candidate, **not** evidence that the present controller is safely Public-writable.
- `film/internal/web/dto/UpdateFilmRequest.java` (`b87292ee0cfdebb99ce8989ca0fbcf24831c4ef7`) is a broad full-object update. **Risk:** a narrowly named Public update cannot safely call it with null/implicit values without proving no Private fields are erased or changed. Prefer deferring updates until an explicitly patch/merge-safe owner operation is independently qualified.
- Current accepted B1 Public DTOs are only read projections (six Fiction keys / seven Film keys) and must not become full mutation input or automatically authorize mutation responses. New request parsing must reject unsupported fields rather than silently accept mass assignment.

## Global Search and Vault: why result-level filtering is insufficient

- `search/internal/web/controller/GlobalSearchController.java` exposes a single global Search method. `search/internal/application/GlobalSearchService.java` (`62d732c08b8e9802a7842f1eb6795218f8812615`) fans out across **People, Fiction, Film, Media, Location, Knowledge, Collection, Account and Feed**; it additionally obtains Vault tag-origin candidates, deduplicates/ranks and then slices results with `offset`, `limit`, `hasMore`.
- `search/internal/web/dto/GlobalSearchResultResponse.java` (`773283339f24f29d6304aec0aad267626de1dac1`) includes `vaultEntryId`, `entryType`, `domain`, `primaryText`, `secondaryText`, `snippet`, `matchKind`, `rankBucket`, `similarity`.
- The current `GlobalSearchQuery.java` (`bb0635ca906af6305c43b27fb142260b3cc80cee`) allows domain/type/tag filters and offset up to 500, limit up to 100. A Public caller cannot be trusted to select only safe domains by self-supplied filters; `tagId` and tag-source snippets can themselves reveal private ownership. A post-`LIMIT` Public filter can change ranks, page sizes, offsets or `hasMore` and leak hidden candidates or produce false pages.
- Generic Vault endpoints share IDs across feature domains, with delete/trash, ratings, tags and favorites. Neither known-ID access nor `GET` alone implies Public safety. A Public Vault operation needs proof that its parent feature is eligible, the returned fields are safe, and private ID existence cannot be probed. Reverse Vault→Fiction/Film imports and direct cross-owner SQL remain forbidden without new Owner authorization.

**Required future analysis:** map each source's candidate generation and tag-origin ranking BEFORE any public `top-K`/pagination, demonstrate no hidden candidates enter rank/`hasMore`, constrain materialization, decide permitted search terms/fields, and test against populated NSFW, trashed, Film Credit, tag and mixed-domain records. This is broader than adding a Security matcher.

## Baseline boundary and unverified items

`ACTIVE.md` currently records `NO_ACTIVE_HANDOFF`; D1–D11 do not open B2. `README.md` in the B1 preparation area and its final review are completed historical evidence, not a B2 source exception. The Owner has now ratified D12–D17 Option A for **B2-W1 preparation** (Film create first); exact source/frozen permission and independent pre-handoff review remain pending. Public tag semantics, safe Fiction references, Search reuse and all later B2-W2/S/V grants still require separate decisions and evidence. No new `visibility` field, user, tenant, schema/migration, global Auth rewrite, Frontend/BFF, Google/Drive activation, or Public media bytes are approved.
