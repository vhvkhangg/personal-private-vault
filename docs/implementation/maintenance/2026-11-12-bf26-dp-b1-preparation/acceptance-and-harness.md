# DP-B1 — Finite acceptance B1-01–B1-24 and existing harness plan

**Status:** [Owner-ratified D7–D11 Option A](owner-ratification-d7-d11-2026-11-12.md) prospective acceptance only, **NOT_RUN**. No test suite, PostgreSQL instance, provider or browser was executed for this document. These approved-for-preparation routes/fields still require independent pre-handoff review, separate exact source/frozen authorization and implementation. This is not a request for a new test framework or source implementation.

| ID | Mandatory condition and future concrete proof |
| --- | --- |
| **B1-01** | Real signed persisted `PROFILE_PUBLIC` created via configured credential and through PRIVATE→PUBLIC downgrade can invoke both selected direct GETs, without `MockUser` bypass. |
| **B1-02** | Qualifying non-NSFW active FICTION returns `200` and **exact PublicFictionResponse key set**; never return `FictionResponse` extra fields even as `null`. |
| **B1-03** | Qualifying non-NSFW active FILM returns `200` and exact PublicFilmResponse key set, no `FilmResponse` extras. |
| **B1-04** | NSFW=true Fiction and Film IDs each return the same 404 code/body/headers as absent IDs; no content/ID leak. |
| **B1-05** | Trashed/missing/wrong-type backing `vault_entries` for feature ID returns same 404 for PUBLIC; restore makes it eligible again iff non-NSFW. |
| **B1-06** | PRIVATE direct GET for qualifying, NSFW, trashed and otherwise existing rows preserves the **current existing** semantics and full existing JSON schema (use source-qualified baselines, do not guess). |
| **B1-07** | Preexisting `POST/PUT` Fiction/Film, genre, classification, links, credit mutations are denied 403 for PUBLIC before validation/DB effects, including invalid bodies and guessed IDs. |
| **B1-08** | PUBLIC nested Fiction GETs (`/story-archetypes`, `/world-settings`, `/classifications`, `/links`, `/links/{linkId}`) all remain 403; no parent existence oracle. |
| **B1-09** | PUBLIC nested Film GETs (`/genres`, `/story-archetypes`, `/world-settings`, `/classifications`, `/credits`, `/links`, `/links/{linkId}`) all remain 403. |
| **B1-10** | PUBLIC `GET /api/v1/films/credits/{creditId}` directly returns 403 for a real and absent credit, even with non-NSFW parent. |
| **B1-11** | PUBLIC FictionGenre/FilmGenre catalogs, Search, Vault, People, Media, Personal, Portability export and all other modules remain 403 as in DP-A; no bytes/counts. |
| **B1-12** | Anonymous requests to all eight new B1 routes are 401; invalid/expired/stale-generation JWT denied, no `sub=1` fallback. |
| **B1-13** | Exactly six proposed Reference GETs return their **unchanged** DTOs for PUBLIC and PRIVATE; direct code lookup and list shape/ordering agree. |
| **B1-14** | PUBLIC Reference platforms/story-archetypes/world-settings list and detail remain 403 with real populated data; no free-text/url leaks. |
| **B1-15** | Hidden Fiction and Film GETs do not cause People/Media/Link/Credit service access or serialize forbidden values; use controlling mocks/spies only if they exercise real security and persistence. |
| **B1-16** | Forced read/NSFW toggle lock/commit orderings prove requests admitted after toggle commit produce 404; an earlier snapshot is consistent and emits only safe DTO. |
| **B1-17** | Forced read/Vault trash/restore ordering proves no split-state false Public grants after a committed trash operation. Tests use PostgreSQL controlled barriers, not time sleeps alone. |
| **B1-18** | Concurrent session downgrade/refresh/revocation during GET obeys existing DP-A admission/redispatch contract; no token authority from a cached controller field. |
| **B1-19** | HTTP status/error envelope identical hidden-vs-absent for approved detail GET; cache-control `no-store` on PUBLIC success and hidden/absent 404; no PRIVATE DTO/cache cross-contamination. |
| **B1-20** | Exact method/path matching: HEAD/OPTIONS/POST variants, suffixes, path normalization, unknown segment, encoded paths, ASYNC and ERROR dispatch cannot reach a Public business handler outside selected eight GETs. |
| **B1-21** | OpenAPI accurately documents authenticated-only Reference GETs, profile-conditional safe/full Fiction/Film DTOs, 401/403/404, correct unchanged operation IDs or expressly approved replacements. |
| **B1-22** | Structural module verification: Fiction/Film use permitted Vault named capability, no reverse Vault→Fiction/Film import, no cross-module `internal`/repo/entity access, no new dependency/DDL. |
| **B1-23** | Focused regression: existing `FictionWebIntegrationTest`, `FilmWebIntegrationTest`, `ReferenceWebIntegrationTest`, feature integration and applicable `SecurityFilterChainIntegrationTest` remain valid for PRIVATE/ANON. |
| **B1-24** | Docs Sync and evidence: exact approved file map, frozen contracts/API impact, source-qualified test commands/receipts (exits, skips, run times), source hashes and independent review; no B2/RAG/Frontend activation or historical counter reset. |

## Existing test assets (confirmed on HEAD)

- `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/fixture/ProfileAuthFixture.java` — real DP-A profile-bound test fixtures; extend **only if needed** and never bypass persisted authority.
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/ProfileRouteAuthorizationIntegrationTest.java` and `SecurityFilterChainIntegrationTest.java` — existing route/security negative regression assets.
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/fiction/FictionWebIntegrationTest.java`, `FictionIntegrationTest.java`; corresponding Film web/domain tests; `reference/ReferenceWebIntegrationTest.java`, `ReferenceModuleIntegrationTest.java`.
- Existing PostgreSQL/Testcontainers, MockMvc, Spring Modulith/OpenAPI, Clock and barrier patterns from DP-A review. New small B1 owner-local integration tests are acceptable under a **later exact handoff**; no new generic harness/agent/plugin/dependency.

## Future gate design — not commands executed now

- **G-B1-SEC:** source-owned PUBLIC detail positives, negatives, nested/credit denials and retained DP-A Auth-on-business deny sweep.
- **G-B1-DB:** real PostgreSQL eligibility and controlled NSFW/soft-delete races, no side effects/partial reads.
- **G-B1-REF:** six exact Reference list/detail GETs and six denied counterparts; PRIVATE/ANON unchanged.
- **G-B1-CONTRACT:** serialized DTO exact property sets, `ApiResponse`, error/cache headers, OpenAPI shape and Modulith ownership.
- **G-B1-REG:** focused existing Fiction/Film/Reference PRIVATE tests and source-qualified safety tests as identified by pre-handoff; no arbitrary giant full suite or invented test-count quota.

Every command must use the repository's bounded runner, unique receipts and explicit timeout/cleanup semantics. A failed/timeout command stays recorded as failed or timeout; a later rerun does not erase it. Publish neither implementation PASS nor a numeric total before actual source-applicable test evidence exists.

## Non-goals and stop conditions

Do not add lists/search/pagination, Public taxonomy/mutations, cross-module DTO enrichment, media proxying, session redesign, a visibility column, backup tool, frontend work, or new schema without an exact additional Owner decision. If a required Public projection cannot be produced safely within current module or transaction limits, **STOP** and report the minimal architectural exception instead of weakening D1 or claiming READY. Closed DP-A findings/counters and DPA-L01 remain unchanged.
