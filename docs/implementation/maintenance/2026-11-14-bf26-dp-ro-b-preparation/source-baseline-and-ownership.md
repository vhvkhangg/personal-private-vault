# BF26-DP-RO-B — source-qualified baseline and ownership feasibility (static only)

**Verified source ref:** published `main` commit [`83f87ca72a2571ce08e3ee99e78a147e556775ff`](https://github.com/vhvkhangg/personal-private-vault/commit/83f87ca72a2571ce08e3ee99e78a147e556775ff). Source identifiers below are **Git blob SHA-1 object IDs**, not file SHA-256 or execution results. Requalify against the handoff entry HEAD. Prefix `J/` denotes `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` denotes corresponding `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

## Actual current code, not a proposal

| Inspected file / blob SHA-1 | Current observed behavior | RO-B implication |
| --- | --- | --- |
| `J/authentication/internal/infrastructure/security/SecurityConfiguration.java` — `ae7e487a11cfe8ab19056d40aec23e6e28f50e7a` | Exactly eight PUBLIC/PRIVATE business GET method matchers; `anyRequest().hasAuthority("PROFILE_PRIVATE")`; ASYNC/ERROR PRIVATE before GET grants; **no** PUBLIC Film POST exception | No security matcher change indicated for RO-B; preserve RO-A deny |
| `J/fiction/internal/web/controller/FictionController.java` — `20a706c8c36004899689b8eb8551f1f13ce7d457` | PUBLIC GET uses `fictionOperations.findPublic` and minimal six-key response; PRIVATE GET uses full mapper | Replace only PUBLIC GET branch after owner-qualified full read; preserve PRIVATE and other methods |
| `J/film/internal/web/controller/FilmController.java` — `a55ee5a9d2da87a69fd80d83115a3f95e7d19e68` | PUBLIC GET uses seven-key minimal response, PRIVATE GET uses full DTO; POST's old PUBLIC branch remains in code but is blocked before handler under RO-A | No POST cleanup or renewed grant; change only eligible PUBLIC GET |
| `J/fiction/internal/application/FictionService.java` — `61827a8280d8126f5fd000aaa5e90707c25463b4` | `findPublic` is `@Transactional(readOnly=true, REQUIRES_NEW, REPEATABLE_READ)`, first non-NSFW projection, then Vault identity/type/trash; `find` loads all 18 via toView without PUBLIC qualification; service already holds Person/CreatorGroup named APIs | Build the full qualified read **in the feature owner**, not `controller.findPublic → find` |
| `J/film/internal/application/FilmService.java` — `6d9f8de7b30238a911ab137e2d2d8fd65e3c9245` | Same qualified minimal-read pattern; `find` is unqualified full view; existing Person named capability | Same transaction/safety; no changed Film mutation path |
| `J/fiction/internal/infrastructure/persistence/FictionRepository.java` — `d34e6c94e571890e22ec10ef12bc312d16b902f4` | Existing `findPublicProjectionById` selects six safe columns with `f.isNsfw=false`; inherited `findById` can obtain the full entity within a single owner-local transaction | Verify source path checks NSFW **before** full serialization; prefer reuse before repository expansion |
| `J/film/internal/infrastructure/persistence/FilmRepository.java` — `3ee3b15dfa81660f08cb0fc9d9daaf03d0b14671` | Existing seven-column non-NSFW projection, inherited `findById` available | Same choice; do not claim unmodified repository path is automatically safe without tests |
| `J/fiction/internal/web/mapper/FictionWebMapper.java` — `867a95e92704630622ee2b58f57ebe347a41262a` | `toResponse(FictionView)` emits existing 18; `toPublicResponse(PublicFictionView)` emits six | Add separate explicit mapping for exactly 19, keep existing PRIVATE mapping unchanged |
| `J/film/internal/web/mapper/FilmWebMapper.java` — `a6c08ec225d49d879b5858d3da60290efd1ce86c` | `toResponse(FilmView)` emits 18; current `toPublicResponse` emits seven; `toPublicCreateResponse` still referenced by blocked Film POST branch | Keep shared DTO and historical parser/mapper unless later separately authorized |
| `J/fiction/fiction/FictionOperations.java` — `95649bdd4e910d6bd2d89d138f8dc6e73d4db099` | Public named interface exposes `findPublic` returning minimal `PublicFictionView` | Candidate added owner API for eligible full-detail-with-name |
| `J/film/film/FilmOperations.java` — `adfb4a88885c1c3f16292131cea3cc58db36dbc8` | Same minimal `PublicFilmView` API | Candidate full-detail API without destroying old B1 service evidence |
| `J/people/internal/application/PersonService.java` — `5afecfbcc37946494ff93e9d5d004e8aa8d87a39` | `find(id)` returns full `PersonView` and roles but **does not qualify Person Vault trash/type** | Must separately qualify via Vault named capability before taking only name; no People source change by default |
| `J/people/internal/application/CreatorGroupService.java` — `dba374a9c4fad601a1318e340f7191a6816c68f5` | `find(id)` returns `CreatorGroupView`; Creator Group is **not** Vault-backed | No fake group Vault status; absent normal → null |
| `J/vault/entry/VaultEntryOperations.java` — `22f1392dd16206422146ac770ec6d251f55e7975` | Existing `find(id)` returns `Optional<VaultEntryView>`; no new query API needed by design | Validate work and Person Vault ID/type/deletedAt via named interface |
| `J/OpenApiConfiguration.java` — `31d6e7fc0c9cde97cbd8c385042a73b4d186be61` | Current selected GET schema graph is **minimal-public / full-private** with `anyOf` envelopes and `PublicFictionResponse` / `PublicFilmResponse`; POST now PRIVATE-only | Amend only two GET contract graphs; avoid global endpoint/schema drift |
| `J/fiction/internal/web/dto/FictionResponse.java` — `243ba8a4abcc50b375374883d49f5f2686927770` | Exactly 18 fields without `authorName` | Preserve for PRIVATE unchanged |
| `J/film/internal/web/dto/FilmResponse.java` — `f58266180768c5f8ae8973f7c9efdcf3b1246cfd` | Exactly 18 fields without `directorName` | Preserve for PRIVATE unchanged |
| `J/people/view/PersonView.java` — `bd49e1c3f94210b6b5973912de0fe452188ff3d0` | Name and additional private attributes (notes, birthday, roles, etc.) | HTTP output must expose **only** selected name scalar |
| `J/people/view/CreatorGroupView.java` — `289877194e22fab42c773c3d5bf9f75c6fb9db8d` | ID/name/description/timestamps | No group DTO, membership or recursive output |

**Architecture checked:** `fiction/package-info.java` (`2cc32d482f0a0f3e86b53ad2d4706e36dff566f2`) permits `people :: person`, `people :: group`, `people :: view`, `vault :: entry/enums/view`; `film/package-info.java` (`f900a031bfead076f6a87342759f2338dd605c84`) permits `people :: person/view`, `vault :: entry/enums/view`. Existing API-only reuse adds no new module edge; direct People/Vault internal imports remain forbidden. See [module dependency matrix](../../../architecture/module-dependency-matrix.md).

## Specific hidden implementation difficulties

1. **Name eligibility:** `PersonOperations.find(id)` on its own can return a trashed Person, so test missing/wrong-type/trashed Vault before returning a name; also verify consistent physical snapshot with the work. Creator Group has no Vault entry and must be treated differently.
2. **B1 tests currently forbid full enrichment:** `PublicFictionReadIntegrationTest` and `PublicFilmReadIntegrationTest` assert exactly six/seven properties for eligible GETs and that hidden paths never call full `find`/enrichment. RO-B deliberately supersedes the **eligible-response** assertions, while preserving the **hidden-path no-enrichment** guard. Do not delete negative tests wholesale.
3. **Separate transactions are unsafe:** public minimal projection + later PRIVATE `find` or HTTP controller People read could straddle NSFW/trash/name commits. Construct an owner-qualified full result with classification IDs and name inside **one physical snapshot**.
4. **Nullable name serialization:** if mapper inclusion drops null fields, `authorName`/`directorName` would be absent, violating exact 19 properties. Verify actual JSON and generated OpenAPI; prefer local inclusion rather than global serializer mutation.
5. **Historical POST path:** RO-A's security blocks PUBLIC Film POST even though code retains an unreachable branch and old `PublicFilmResponse` references. Do not remove or reactivate this in RO-B; deleting old minimal types is not needed to meet the new detail response.
6. **Unknown technical proof until runtime:** Spring transaction annotations and named APIs suggest a feasible implementation, but actual Testcontainers/PostgreSQL joins/isolation, 404/500 envelopes and concurrent snapshots are **NOT_VERIFIED**. No new runtime count or PASS is asserted.

## Existing test sources (current baseline)

- `T/fiction/PublicFictionReadIntegrationTest.java` (`6088c536117c2ee2d61217589e15313cd7f74284`): two origins; 6-key minimal shape; NSFW/trash/missing/wrong-type 404; no-classification-on-hidden; real snapshot/races; safe 200/404/cache.
- `T/film/PublicFilmReadIntegrationTest.java` (`0b99bff9a244c5e54ac5c3b0d76c56c0d25493f4`): analogous 7-key shape and denied Public POST after RO-A.
- `T/authentication/ProfileRouteAuthorizationIntegrationTest.java` (`f003d0b5ea035d90915042d52848996b91f7f2ab`): exact eight-pair registered-handler sweep, both session origins and no-handler/no-mutation controls.
- `T/web/OpenApiRouteInventoryIntegrationTest.java` (`938303209b93b5cbcec6db3f316d9d2274cb9c83`): complete generated operation/schema inventory, error and profile-specific GET shapes.
- `T/fiction/FictionWebIntegrationTest.java` (`7b6164d089cfba3dde3833e7c192072085a807e0`), `T/film/FilmWebIntegrationTest.java` (`ec60b43e8148752e775061a41e837c97d74acdc4`): PRIVATE responses/mutations regression inputs; remain unchanged by default.

These are source reconnaissance findings; test names, assertions and fresh runtime evidence must be revalidated by the independent reviewer at the active source HEAD.
