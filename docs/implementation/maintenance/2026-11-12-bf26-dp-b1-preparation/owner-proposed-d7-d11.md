# DP-B1 — Owner-ratified D7–D11 (Option A; original proposal and alternatives retained)

**Source HEAD:** `6374537074afc32ed7e939cf94ed2f21d77c5f42`. The Owner previously ratified **D1–D6** for DP-A and the future conservative Public surface. The Owner has now **explicitly ratified D7–D11 Option A without amendments**; see the [dated Owner ratification](owner-ratification-d7-d11-2026-11-12.md). The table retains the original alternative analysis, but Option A is the binding **preparation design only**. Independent Codex preparation review, frozen source/API authorization and implementation remain pending. `NO_ACTIVE_HANDOFF`.

| Decision | Owner-ratified Option A — smallest safe B1 boundary | Main trade-off / unselected alternatives needing new approval |
| --- | --- | --- |
| **D7 — Exact Fiction/Film entrypoints** | Public may use only existing `GET /api/v1/fictions/{id}` and `GET /api/v1/films/{id}` after authoritative eligibility and Public projection; **no list endpoint, nested GET, read-by-credit, search or mutation** | No Public discovery/browse UI until later work. Option B (new separate Public paths or list/search) has additional API/navigation costs, not selected |
| **D8 — Public DTO fields** | Fiction: `id,title,originalTitle,nationalityCode,format,isNsfw`; Film: `id,title,originalTitle,nationalityCode,format,productionStyle,isNsfw`. Dedicated response DTOs; full DTO remains PRIVATE-only | Excludes posters, plot text, progress, personal review, creator/credit, genre/classification IDs and total count—even if some are innocent. Wider fields require exact source/privacy assessment |
| **D9 — Reference GET allowlist** | Six exact methods: countries, countries/{code}, languages, languages/{code}, currencies, currencies/{code}. Preserve existing response DTOs/error semantics; authentication still required | No Public `platforms`/`story-archetypes`/`world-settings` or genres initially because URLs/free text and taxonomy provenance may be owner-local |
| **D10 — Visibility consistency** | Non-NSFW feature row **AND** backing active correct-type Vault Entry at one consistent logical read snapshot; hidden/absent/trashed/wrong type indistinguishable 404; no Public access if uncertain | May require narrowly scoped owner-local read transaction/query/capability changes. No generic schema visibility column, Vault reverse dependency or broad cross-owned query authority |
| **D11 — Frozen HTTP compatibility and cache** | Keep same direct GET paths; profile-aware **separate response DTO**, unchanged PRIVATE wire response; canonical `ApiResponse`; conditional accurate OpenAPI; PUBLIC `Cache-Control: no-store` (and no entity-derived validators/redirect/Location headers) | Same path returning two profile-specific payloads needs explicit API documentation and regression tests; new endpoint approach not selected |

## Rationale and contract boundaries

**Facts from the HEAD:** The current direct GETs return the full feature DTO, including user and People-related fields; `VaultEntry` carries `deletedAt`; `ReferenceController` has 12 GET routes but different DTO privacy profiles. Authentication presently permits only PRIVATE on business paths. These facts support the need for a constrained projection, **not** the conclusion that every non-NSFW record is automatically safely publishable.

**Owner-ratified conservative first-release premise:** The existing Owner-controlled `isNsfw=false` classification is the first release visibility condition under already-ratified D1; there is no independent per-record Public opt-in. If the Owner expects confidential but non-NSFW titles, then D1's model is insufficient and **B1 must stop for a new explicit visibility design decision** rather than silently leak them. There is no proof in source that titles of all non-NSFW rows are non-confidential.

**A critical non-option:** Do not reuse `FictionResponse`/`FilmResponse` and serialize with nullable/redacted fields; that preserves schema keys and risks null-vs-present leakage, accidental new fields, and inconsistent OpenAPI. Prefer an explicit allowlist-shaped DTO with no forbidden keys. Do not include those fields as `null` by default.

**Owner disposition (2026-11-12):** All D7–D11 = **Option A approved without modification**, recorded in the [dated ratification](owner-ratification-d7-d11-2026-11-12.md). This ratifies *preparation design only*, **not** frozen source/API edits, independent review acceptance or an executable handoff. Independent Codex `$codex-pre-handoff-review` must confirm adequacy and authorize a subsequent publication gate; implementation requires further exact source/frozen consent.

## Findings to keep open until reviewed

- Ability to preserve a consistent Vault+Feature read under module boundaries.
- Exact API/OpenAPI representation of profile-specific DTOs on the same path.
- Source-backed classification of list/detail Reference safe catalogs.
- Security matcher precision (HEAD, OPTIONS, unknown, nested routes and redispatch).
- Mandatory test evidence for hidden IDs, revocation, concurrent reclassification/deletion and legacy PRIVATE compatibility.

These are **questions for preparation review**, not findings already closed or test failures observed. No schema/DDL, global taxonomy/publication policy, new authorization engine or Frontend redesign is approved here.
