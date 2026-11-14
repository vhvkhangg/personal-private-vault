# BF26-DP-RO-B — candidate edit boundary and frozen-source exceptions (NOT APPROVED)

**Status:** candidate source map for independent pre-handoff review; **not** an Owner authorization, handoff allowlist, or permission to edit source. Existing phase-specific frozen contracts remain binding. The technical route below is the simplest presently source-supported design, not a claim of completed implementation.

## Preferred minimal implementation seam

1. Keep SecurityConfiguration's eight exact PUBLIC GET matchers and PRIVATE fallback **byte-unchanged**. Do not create new People/Search/Vault routes.
2. Add an **owner-local named read capability** per Fiction/Film, e.g. `findPublicDetail(id)`, returning a feature-owned immutable full-work view plus a nullable scalar `authorName`/`directorName`. Keep existing minimal `findPublic` if other code/tests still refer to it. Avoid changing PRIVATE `find` semantics.
3. Within the owning service's single `REQUIRES_NEW/REPEATABLE_READ, readOnly=true` physical transaction, fetch the work record and verify `!isNsfw` and same-ID active correct-type work Vault entry **before** People/Group lookups. Reuse the owning feature's existing `toView` and classification loaders; resolve only its linked Person or CreatorGroup via approved named interfaces. Qualify Person's own Vault entry first. Assemble one immutable result containing all approved data before return.
4. Map the result to a separate **19-field PUBLIC-only response class** per feature; leave the shared 18-field PRIVATE DTOs and their mappers unchanged. Ensure nullable name JSON key is present. Only the two selected GET controller branches switch, with existing safe 404/no-store behavior; 500 must use safe canonical data-null/no-store and must not treat DB failure as absent reference.
5. Modify only the two selected GET OpenAPI envelopes/schemas and targeted tests needed to distinguish the new 19-field Public variant from the unchanged 18-field Private one; preserve `getFiction`/`getFilm` and RO-A `createFilm` PRIVATE-only contract. Retain original tests/evidence as historical records.

## Precise candidate production edit paths

Prefix `J/` means `backend/src/main/java/com/vhvkhangg/personalprivatevault/`. Paths marked **NEW** are suggested additions within existing packages, not an instruction to create new packages. **All are proposed only**; Codex pre-handoff review must determine the truly necessary exact set.

| Candidate | Permitted prospective purpose after separate authorization |
| --- | --- |
| `J/fiction/fiction/FictionOperations.java` | Add owner-local qualified detail read signature; keep existing public named capabilities |
| `J/fiction/view/PublicFictionDetailView.java` **NEW** | Bundle approved full immutable FictionView + nullable authorName; never Person/Group object |
| `J/fiction/internal/application/FictionService.java` | Build a single-snapshot, eligibility-guarded full detail and name; no create/update change |
| `J/fiction/internal/web/controller/FictionController.java` | Change only PUBLIC selected `GET /{id}` branch to 19-field result |
| `J/fiction/internal/web/mapper/FictionWebMapper.java` | Explicit 19-key Public mapping; PRIVATE 18-key mapper unchanged |
| `J/fiction/internal/web/dto/PublicFictionDetailResponse.java` **NEW** | Exactly 19 properties; always-present nullable authorName |
| `J/film/film/FilmOperations.java` | Add corresponding qualified Film detail signature |
| `J/film/view/PublicFilmDetailView.java` **NEW** | Full FilmView + nullable directorName; no Person/Group object |
| `J/film/internal/application/FilmService.java` | Single-snapshot full Film detail and qualified director name |
| `J/film/internal/web/controller/FilmController.java` | Change only PUBLIC selected `GET /{id}` branch; **not POST** |
| `J/film/internal/web/mapper/FilmWebMapper.java` | Public 19-key detail mapper; PRIVATE create/read/update remains 18 |
| `J/film/internal/web/dto/PublicFilmDetailResponse.java` **NEW** | Exactly 19 properties; always-present nullable directorName |
| `J/OpenApiConfiguration.java` | Change exactly the two selected GET response/schema graphs; retain all other operations, including PRIVATE-only Film POST |

**Potential refinement subject to review:** the existing feature repositories' inherited `findById` can read a full row under the owning service's snapshot; repository production edits are **not proposed by default**. If a conditional full-row query is required by evidence, the reviewer must identify the exact `FictionRepository.java` / `FilmRepository.java` paths and seek *explicit* Owner authorization before source changes. Do not assume this table pre-authorizes optional edits. Do not delete `PublicFictionResponse`, `PublicFilmResponse`, old `Public*View`, or blocked W1 parser/POST references as cleanup.

## Targeted candidate test edit paths

Prefix `T/` is `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

| Candidate | Test boundary |
| --- | --- |
| `T/fiction/PublicFictionReadIntegrationTest.java` | New exact 19-field positive, Person/Group/missing/null/500/404/snapshot, preserve hidden-path no-enrichment and old negative controls |
| `T/film/PublicFilmReadIntegrationTest.java` | New 19-field and director/null/500/concurrency, retain POST 403 and other RO-A/B1 controls |
| `T/web/OpenApiRouteInventoryIntegrationTest.java` | Generated Public GET 19, Private 18 and unchanged POST/Reference/errors/refs/operation IDs |
| `T/authentication/ProfileRouteAuthorizationIntegrationTest.java` | Exact eight-pair sweep and protected People/mutation routes with full content; only change test assertions actually made obsolete by RO-B |

**Unmodified execution/reuse inputs by default:** `T/fiction/FictionWebIntegrationTest.java`, `T/film/FilmWebIntegrationTest.java`, relevant People/Vault integration tests, architecture/Modulith tests and `SecurityFilterChainIntegrationTest.java`. Those may be run for regression but no edit is implied. New targeted test classes or additional files require an exact Owner decision if the approved executable map later does not list them.

## Frozen, architectural and privacy STOP rules

- **NO CHANGE by default:** `SecurityConfiguration.java`; direct People or CreatorGroup/Vault production source; repositories; shared `FictionResponse`/`FilmResponse`; database migrations V1–V29, DBML, schema/index, POM/dependencies, module descriptors, auth/JWT/session, global serializer/advice/filter/transaction manager, provider/flag setup, Frontend/BFF, Search and RAG.
- No import from another module's `internal`, no Vault→Fiction/Film reverse edge, no People lookup from a controller outside the owner snapshot, no recursive view leakage, no arbitrary association ID probing, no new PUBLIC endpoint.
- Do not change PRIVATE GET/POST/PUT, including their exact 18-field body, headers and error behavior. Don't soften NSFW/active-Vault/404 gates or make Person absence cause a 404 for otherwise eligible work.
- If a chosen design needs a People/Vault source edit, new package, global JSON inclusion setting, an extra route, an additional repo query, an added test file, or a changed contract/exception beyond this proposed finite map, **STOP for an explicit Owner source/API/test decision** after independent review. Do not silently enlarge the plan.
- Do not modify `ACTIVE.md`, create an executable handoff, stage, commit, push or interpret preparation as source permission. The Owner's separate exact approval comes only **after** independent `READY_FOR_HANDOFF` and publication of reviewed preparation.
