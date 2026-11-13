# BF26-DP-RO — candidate HTTP contract and finite acceptance matrix

**Prospective contract; RO-A is the ONLY current independent re-review target.** The Owner has ratified RO-D04 and RO-D06 on 2026-11-13 (see [dated decision](owner-ratification-ro-d04-d06-2026-11-13.md)): **RO-A first**, **RO-B deferred**. RO-A and RO-B are `NOT_IMPLEMENTED / NOT_TESTED`. RO-B PUBLIC full 19-key wire/private 18-key invariant is frozen as a *product decision* but remains outside RO-A handoff and needs separate technical pre-handoff review/source authorization. No source/API exception or executable handoff is created by this document.

## Current versus target PUBLIC business surface

| Operation | HEAD `3ce4c87d` | Target read-only policy |
| --- | --- | --- |
| GET `/api/v1/fictions/{id}` | Authenticated eligible, **six-field** PublicFictionResponse | **RO-A: unchanged six fields. Future RO-B only:** 18 existing FictionResponse properties + always-present nullable `authorName` = **19 PUBLIC fields**; PRIVATE 18 unchanged |
| GET `/api/v1/films/{id}` | Authenticated eligible, **seven-field** PublicFilmResponse | **RO-A: unchanged seven fields. Future RO-B only:** 18 existing FilmResponse properties + always-present nullable `directorName` = **19 PUBLIC fields**; PRIVATE 18 unchanged |
| Six Reference countries/languages/currencies GETs | Authenticated PUBLIC/PRIVATE, existing DTO | Unchanged |
| POST `/api/v1/films` | **Authenticated PUBLIC and PRIVATE**; PUBLIC strictly constrained | **PRIVATE ONLY**; a live PUBLIC token must be denied *before handler* |
| Any other PUBLIC business/data route | PRIVATE fallback, denied | Unchanged: denied, including all other GETs unless independently selected later |
| Auth/profile endpoints | Existing DP-A exact rules | Unchanged, including PUBLIC login and privilege-switch operations; do not ban necessary Auth POSTs when enforcing “no PUBLIC business writes” |

Owner-selected PUBLIC business allowlist after RO-A: **exactly eight method/path pairs** (B1 GETs). The policy “PUBLIC cannot write” applies to business/data changes, **not** required authentication/profile transitions. Preserve historic B2-W1 nine-pair acceptance as past evidence, not current target.

## RO-A: selected immediate revocation contract (no change to GET payloads)

1. PUBLIC authenticated `POST /api/v1/films` becomes **403 before MVC handler and any mutation**; unauthenticated remains 401. `PROFILE_PRIVATE` continues to use the unchanged POST handler, broad request, configured media/charset converter, full 18-property FilmResponse and HTTP 201 semantics. Required Auth/profile POSTs remain governed by existing exceptions.
2. Preserve B1's **eight** GET business route pairs, two direct eligible work GETs using the existing **six-field Fiction / seven-field Film PUBLIC** DTOs, and six Reference GETs with unchanged wire/security. **Do not implement RO-B GET expansion in RO-A.**
3. Adjust exact registered-route allowlist nine → eight, qualified two-origin denial/no-handler/no-write tests, PRIVATE positive HTTP/DB/media controls and generated OpenAPI so Film POST is no longer advertised as PUBLIC permitted, while preserving PRIVATE `createFilm` schema and B1 GET schema graph.
4. No parser/domain cleanup is mandatory: existing W1 PUBLIC Film creation branch may remain unreachable behind the security matcher. Do not remove source or producer tests indiscriminately; replace contradictory *current* positive-PUBLIC-POST expectations while retaining historical evidence. No unrelated code or source exceptions.

## Deferred RO-B: Owner-ratified response and same-snapshot contract (NOT RO-A acceptance)

1. **Exact successful PUBLIC data object:** eligible Fiction = all existing **18** top-level `FictionResponse` fields plus `authorName` (**19**); eligible Film = all existing **18** top-level `FilmResponse` fields plus `directorName` (**19**). No additional or missing keys. Both new name properties are always **present**, carrying a string or explicit JSON `null` (not omitted). `review`, `progressStatus`, `consumptionStatus`, `currentProgressText`, author/director IDs and classification IDs are intentionally disclosed. Canonical `ApiResponse`, no-store and safe errors remain.
2. **Exact unchanged PRIVATE contract:** Fiction and Film PRIVATE GET, POST, PUT return **18 original fields** only, not names, with accepted validation, headers, status/errors and unchanged generated PRIVATE OpenAPI. Do not add name fields to shared PRIVATE DTOs and accidentally alter serialization; a separate public-only 19-key response is permitted in later RO-B.
3. **Work eligibility first:** Before any linked-source access, feature owner qualifies a work as non-NSFW with existing matching **active / not trashed** `VaultEntryType.FICTION` or `FILM` identity. Missing/hidden/trash/wrong-type/wrong-ID work → indistinguishable `404` with `Cache-Control: no-store`, zero People lookup and zero full DTO leakage. Do not use ordinary unqualified `find` for PUBLIC.
4. **Author name:** Read exactly the referenced `authorPersonId` *or* `authorGroupId` (Fiction domain requires one). When Person-backed, qualify referenced Person's own Vault entry as existing, type `PERSON`, active/nontrashed, then return only `PersonView.name()` from a named capability. When CreatorGroup-backed, use `CreatorGroupOperations.find(id)` and return only its `name` (CreatorGroup is **not Vault-backed**: no invented group trash/type check). Do not recursively expose the returned People/Group view or members.
5. **Director name:** Only the optional referenced Film `directorPersonId`, with the same active/correct-`PERSON` Vault eligibility as author Person. No director ID → `directorName:null`.
6. **Reference null and error policy:** Any referenced Person absent, Vault missing/wrong-type/trashed, referenced Creator Group absent, or name missing/blank gives the selected name key with JSON **`null`**. Genuine database/lookup/transaction/infrastructure **failure** must instead yield canonical **safe `500`**, `ApiResponse.data=null`, `Cache-Control: no-store`, with no raw exception, private attribute, identifier or diagnostic source oracle. Do not swallow real errors into null or load an unqualified PRIVATE fallback.
7. **Single owner snapshot:** Eligibility, all 18 shared work values, classification IDs and the optional name must be materialized in the same Fiction/Film owner-qualified `REQUIRES_NEW / REPEATABLE_READ` or source-proven equivalent transaction (including legal called People/Group named API reads). Prove physical consistency and correct earlier-eligible snapshot semantics from B1 D10; no retroactive cancellation is required. A transaction that fails may not return a partial 200 response.
8. **No new public route:** Identifiers/linked names in the complete work detail do not grant People, Creator Group, related genres, credits, links, classifications, generic Vault, tags, Search or any other endpoint. Exactly eight business pairs remain PUBLIC; B2-W2 Public writes, B2-S/V, DP-C remain separately gated.
9. **OpenAPI:** For RO-B only, generated selected PUBLIC GET schemas have all **19** properties with a present/nullable name; PRIVATE schemas and their original 18-property shapes stay unmodified. Explicitly verify actual HTTP JSON specimens; do not infer that a shared schema with name null preserves PRIVATE wire shape.

## Finite RO acceptance map — RO-A mandatory now; RO-B deferred until its own preparation

| ID | Mandatory test/evidence once separately source-authorized |
| --- | --- |
| **RO-01** | Both real persisted PUBLIC token origins (Public login and downgrade from Private) can access only the eight selected business GET pairs. PRIVATE positive control and anonymous 401 are maintained. |
| **RO-02** | Live authenticated PUBLIC `POST /api/v1/films` is 403 before Film handler, service or Vault mutation; exact all-nine-to-eight registered-route inventory, no POST wildcard. |
| **RO-03** | PRIVATE `POST /api/v1/films` retains old broad request, supported media/charset, validations, committed Film/Vault transaction, headers and full 201 response; no code removal regresses it. |
| **RO-04** | All other PUBLIC business mutations (including future GET-with-side-effect abuse) remain denied; Auth/profile POSTs are not inadvertently blocked by read-only business rule. |
| **RO-05 — RO-B deferred** | Selected PUBLIC Fiction `data` has **exactly 19 fields**: all 18 existing FictionResponse keys (including review/progress/author and classification IDs) plus always-present nullable `authorName`. PRIVATE remains exactly 18. |
| **RO-06 — RO-B deferred** | Selected PUBLIC Film `data` has **exactly 19 fields**: all 18 existing FilmResponse keys (including review/progress/director and classification IDs) plus always-present nullable `directorName`. PRIVATE remains exactly 18. |
| **RO-07** | For eligible records, PUBLIC and PRIVATE shared existing fields have equal values under same stable DB fixture (except any deliberately profile-specific transport/cache and concurrent source changes). |
| **RO-08** | NSFW Fiction/Film inaccessible from PUBLIC by direct known ID, even where PRIVATE can read; no full DTO/body/relations/headers/log leakage. |
| **RO-09** | Missing, deleted/trashed, wrong Vault type and feature/Vault ID mismatch are observationally indistinguishable to PUBLIC: 404 no-store and no different existence oracle. |
| **RO-10** | Transaction barrier proves no full data or linked name can escape when source changes to NSFW/trash/type between read stages. Test the B1 accepted D10 earlier-snapshot limitation rather than promising retroactive cancellation. |
| **RO-11 — RO-B deferred** | Eligible PUBLIC Fiction `authorName` resolves only an active correct-`PERSON` Vault-backed author Person, without exposing PersonView; direct People API remains 403. Missing/trashed/wrong-type Person gives present `null`. |
| **RO-12 — RO-B deferred** | Eligible PUBLIC Fiction `authorName` resolves a referenced Creator Group via its named capability; group absent gives present `null`. Group is **not Vault-backed**; group description/members/timestamps and direct group reads stay blocked. |
| **RO-13 — RO-B deferred** | Eligible PUBLIC Film `directorName` is present: Person scalar name for eligible active correct-`PERSON` Vault link; `null` for absent/trashed/wrong-type/missing Person or absent director ID; no Person fields/API. |
| **RO-14 — RO-B deferred** | `authorName`/`directorName` always present; benign missing/ineligible reference → JSON null, genuine DB/lookup/transaction failure → safe 500 with data null/no-store; hidden work triggers zero People lookups; no ID-dependent error oracle or unqualified fallback. |
| **RO-15** | PUBLIC cannot exploit selected Fiction/Film IDs to access unselected nested credits, genres, linked resources, people, generic Vault, search or tag-origin endpoints. |
| **RO-16** | Whole Personal/Finance/People and other fixed Private modules deny reads, counts, exports and mutations for PUBLIC, including media bytes and metadata. |
| **RO-17** | JWT revoke/refresh/profile-switch races: new requests after invalidation denied, legitimate already-admitted request limitation preserved, no stale PUBLIC write grant. |
| **RO-18** | ASYNC/ERROR redispatch, HEAD/OPTIONS, path suffix/unknown, all HTTP verbs and no-handler cases preserve exact fail-closed default denial. |
| **RO-19** | **RO-A mandatory:** generated OpenAPI describes **eight** Public business GET pairs, no Public Film POST grant/create variant, preserves PRIVATE create/operation ID/DTO and existing B1 six-/seven-key GET schemas. **RO-B deferred:** replace two Public GET schema branches with exact 19-property/nullable-name contract while preserving PRIVATE 18 and eight routes. |
| **RO-20** | Real PostgreSQL and MockMvc fixtures use actual persisted sessions/authorities; selected tests assert bytes/JSON and DB state and cover failures, not only mock service interactions. |
| **RO-21** | Module/Modulith and dependency rules pass: only named People/Group capability, feature owner performs projection, no reverse Vault→feature import, no schema/DDL/index or new global serializer/framework. |
| **RO-22** | Exact source-hash-qualified receipts, finite bounded groups, independent review, accepted historic DP-A/B1/W1 evidence and counters unchanged; documented Docs Sync/next action and no false deployment or full-test claim. |

### Mandatory slice-specific interpretation (nothing executed)

- **RO-A (current review and eventual implementation):** RO-01–04, RO-15–18, RO-19 **POST revocation/existing GET** portion, RO-20–22 as applicable. Reuse existing eight GETs; **do not require RO-05–14 or new full GET schemas** as RO-A acceptance. Existing Auth races and B1 reads can be source-qualified rather than arbitrarily rerun.
- **RO-B (later separate prep/review/authority only):** RO-05–14, RO-19 **19-key GET** portion, and affected shared RO-01/08–10/15–22; verify all named Person/Group/absence/error/physical-snapshot fixtures. Not implied ready merely because D04 is ratified.

### Suggested bounded verification groups (not run)

- **RO-A SECURITY/PRIVATE:** registered-handler eight-pair sweep + revoked Public POST (no-handler/no-mutation, both origins) + retained PRIVATE creation media/DB cases.
- **RO-B FICTION:** full field-wire correctness; Person/Group-name/404/NSFW/trash/isolation; PRIVATE compatibility.
- **RO-B FILM:** full field-wire correctness; director-name/404/NSFW/trash/isolation; B1 and Private retained read controls.
- **RO-A COMMON/API/ARCH:** generated `/v3/api-docs` current eight-pair/PRIVATE-POST contract, dispatcher/route guards and architecture. **RO-B later:** full GET OpenAPI and real PostgreSQL same-snapshot/linked-name failures; no early execution or new grant.

Reuse existing scripts/run-bounded-command.py with the standing 300-second ordinary cap, Testcontainers PostgreSQL, MockMvc and owner test fixtures. Select exact tests after reviewing changed files; do not fabricate test counts, invent full suite PASS or treat static preparation as runtime proof. Failed and prior evidence must be preserved. No provider/Frontend live activation.
