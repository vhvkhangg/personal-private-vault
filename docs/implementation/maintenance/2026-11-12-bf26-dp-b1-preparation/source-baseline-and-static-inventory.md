# DP-B1 — Source-qualified static inventory (2026-11-12)

**Verified remote `main` HEAD:** `6374537074afc32ed7e939cf94ed2f21d77c5f42`. Source was inspected via the connected GitHub repository on this specific commit; no local checkout/runtime inspection was performed. Blob hashes below are Git blob identifiers (not SHA-256).

## Existing HTTP handlers and core DTOs

| Source and Git blob | What exists on HEAD | Consequence for proposed B1 |
| --- | --- | --- |
| `fiction/internal/web/controller/FictionController.java` — `71d6ed396d0a798885b36ddda5dcf7118f2a9205` | `/api/v1/fictions`: GET `/{id}`, classifications, story-archetypes, world-settings, links and mutation handlers | Only exact direct `GET /{id}` candidate; nested GETs and writes stay PRIVATE |
| `film/internal/web/controller/FilmController.java` — `87dff841b4c00c3162aaf94ccba1906d2c9914b8` | `/api/v1/films`: GET `/{id}`, genres, classifications, credits, direct `/credits/{creditId}`, links and mutation handlers | Only exact direct `GET /{id}` candidate; direct credit ID is a high-risk escape |
| `fiction/internal/web/controller/FictionGenreController.java` — `d6f5d5cd1e159cc0d70d03f7394b8a58f95d1535` | GET `/api/v1/fiction-genres/{id}`, `/by-name`; writes | Not a B1 Public catalog |
| `film/internal/web/controller/FilmGenreController.java` — `58eb2f3f0ea3549798203f90603c4083f070a9be` | GET `/api/v1/film-genres/{id}`, `/by-name`; writes | Not a B1 Public catalog |
| `reference/internal/web/controller/ReferenceController.java` — `f12c273e5e971624af03054473028132afafbd4b` | Twelve GET mappings: list/detail for countries, languages, currencies, platforms, story-archetypes and world-settings | Propose **six exact GETs** for first three catalogs; no generic `/reference/**` grant |
| `fiction/internal/web/dto/FictionResponse.java` — `243ba8a4abcc50b375374883d49f5f2686927770` | Contains `posterUrl`, `authorPersonId`, `authorGroupId`, `description`, `review`, `consumptionStatus`, `currentProgressText`, genre and classification IDs | Not safe to return unchanged to PUBLIC |
| `film/internal/web/dto/FilmResponse.java` — `f58266180768c5f8ae8973f7c9efdcf3b1246cfd` | Contains `posterUrl`, `directorPersonId`, `description`, `review`, `consumptionStatus`, `currentProgressText`, genre/classification IDs | Not safe to return unchanged to PUBLIC |
| `film/internal/web/dto/FilmCreditResponse.java` — `6588903387059ab25e9f889318bf5c01ab5db250` | Contains `personId`, `characterName`, `note`, `filmId` | All Public credit access deferred |
| `reference/internal/web/dto/CountryResponse.java` — `6c25e7102dc13d55c3338c6bf54f903ef0a835b6` | `code`, `nameEn`, `nameVi` | Owner-ratified Option A B1 candidate; no source implementation authority |
| `reference/internal/web/dto/LanguageResponse.java` — `e6bdd48f23c5e2076298fe6e359118235fe6faab` | `code`, `nameEn`, `nameVi` | Safe static-style candidate |
| `reference/internal/web/dto/CurrencyResponse.java` — `e2f6a00d953dad73bbfa0e3674c583abc12656bb` | `code`, `name`, `symbol`, `decimalPlaces` | Safe static-style candidate |
| `reference/internal/web/dto/PlatformResponse.java` — `d1a6e15090f31ade22c79d6c8fa0cd59a96c1ff7` | Contains user-relevant `url`, `createdAt` | Exclude from B1; static-safe origin not established |
| `reference/internal/web/dto/StoryArchetypeResponse.java` — `b621ad282296a739b7fb124170c4e07a33dc1cb8` | `id`, `name`, free-text `description` | Exclude pending taxonomy/privacy policy |
| `reference/internal/web/dto/WorldSettingResponse.java` — `84f742ae70b016e61e57cd7e9546f9b10c4b5401` | `id`, `name`, free-text `description` | Exclude pending taxonomy/privacy policy |

Paths above are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/` unless marked otherwise. The word `public` in an existing Java `view` is a **module API visibility**, not an authorization for the new PUBLIC login profile.

## Source eligibility and ownership

- `fiction/internal/application/FictionService.java` (`bc993c6bf2e78815198b1f2b2e00c33be77c5464`) and `film/internal/application/FilmService.java` (`e4afaa4e6983ed646a8bff8cc2ff96744537663a`) currently implement their `find` by `findById` and map a full feature view. They are not current PUBLIC-safe gates.
- `fiction/internal/infrastructure/persistence/FictionRepository.java` (`400780a55fcdf72a2de6d4e4b4b4d0589e2b5527`) and `film/internal/infrastructure/persistence/FilmRepository.java` (`5fac19330be50dac62853a97bf416fe560742654`) currently expose standard `JpaRepository`, not an independently reviewed visibility predicate.
- `fiction/internal/domain/Fiction.java` (`0dc8c82c864cbb002c88c4ee153735dfbfa7e205`) and `film/internal/domain/Film.java` (`e58adc9aa1658c7f98a57ac05179a73c09292deb`) both persist `is_nsfw`. Their IDs equal backing Vault Entry IDs.
- `vault/internal/domain/VaultEntry.java` (`6951009010e00da324f81de0de53ab216c16131d`) has `entry_type` and nullable `deleted_at`. `VaultEntryOperations.find()` returns `VaultEntryView` with `entryType` and `deletedAt`; it is an existing **named public module capability**, not a PUBLIC-profile grant. Feature modules already depend on this Vault capability. Vault itself must not import Fiction/Film internals.
- `reference/internal/application/catalog/ReferenceCatalogService.java` (`48da7af7c1b312891dc9386e156ddaf1bf9f3509`) maps Reference repository results to the six existing DTO families; no Public-profile filtering is currently implemented there.
- `authentication/internal/infrastructure/security/SecurityConfiguration.java` (`0b582d218ab94f455379dfbeca43716a875cfb92`) currently has explicit Auth/conditional-provider matchers and `anyRequest().hasAuthority("PROFILE_PRIVATE")`. A change to allow exactly B1's GET methods **must not** loosen the fallback to authenticated or create broad wildcard grants.
- `ApiResponse.java` and `ApiResponses.java` retain the canonical `{data,error,meta}` envelope; the new safe DTO should not change that structure or PRIVATE responses.

## Source-identified design blockers to resolve in independent preparation review

1. **No safe DTO exists today.** The public projection needs a separate defined schema and mapping path; using the full feature `View`/DTO is insufficient.
2. **No active/NSFW qualifying find exists today.** A Public read must require the appropriate `VaultEntryType`, `deletedAt == null`, and `isNsfw == false` within a consistent DB snapshot. The existing basic `find` is not enough. Do not dereference People or link/credit relations for Public.
3. **Cross-module ownership matters.** The feature modules may use `VaultEntryOperations` named interfaces; no access to `vault.internal` classes/repositories or direct cross-owned SQL unless explicitly justified and authorized under the frozen module matrix. A consistent-read strategy must be demonstrated by PostgreSQL evidence; it is not yet verified.
4. **Current HTTP route semantics are Private-only at the security filter.** Adding a Public decision to an existing detail GET must keep PRIVATE's complete `FictionResponse`/`FilmResponse` behavior and produce accurate OpenAPI profile-sensitive schemas without inventing a global polymorphic transport type.
5. **Reference catalog provenance:** Source shows read-only HTTP handlers but does not prove every populated row is globally curated. D9's initial three catalog choice is conservative, not proof that every name always meets organizational publishing criteria.

## Next review evidence to request

Inspect the actual source-listed classes, module dependency descriptors, DP-A `ProfileAuthFixture` and `SecurityFilterChainIntegrationTest`, Fiction/Film web and feature tests, `ReferenceWebIntegrationTest`, and frozen API/ADR/DBML contracts. Confirm physical SQL/test owner data and runtime registered mappings; do not infer a runtime route inventory from this static inventory. Runtime/behavior tests are postponed to the **later authorized implementation**, not this preparation authoring session.
