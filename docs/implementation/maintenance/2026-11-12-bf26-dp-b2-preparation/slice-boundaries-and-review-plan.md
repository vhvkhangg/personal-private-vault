# BF26-DP-B2 — Owner-ratified Option A staged preparation and finite acceptance map

**Status:** `OWNER_RATIFIED_D12–D17_OPTION_A_FOR_PREPARATION / NOT_RUN / NOT_INDEPENDENTLY_REVIEWED / NOT_AUTHORIZED_FOR_IMPLEMENTATION`. The Owner approved D12–D17 Option A on 2026-11-12; see [ratification](owner-ratification-d12-d17-2026-11-12.md). This remains a feasibility/coverage plan, not an executable handoff or frozen-source/API approval.

## Owner-selected risk-separated preparation order (not automatic implementation)

| Candidate slice | User-visible capability | Hard gate | Deferred |
| --- | --- | --- | --- |
| **B2-W1** | Public creates a Film with a tiny safe request and safe response; no relationships, no private field write | Owner-ratified D12–D17, independent preparation `READY_FOR_HANDOFF`, exact frozen/HTTP source authorization, accepted real DB/HTTP/security evidence | Existing-film update, Fiction writes, genre/credits/links, Search and Vault |
| **B2-W2** | Later explicitly selected Public changes to eligible Film/Fiction with qualified author/genre relations | Cross-owner reference-safe reads, atomic guards and no implicit overwrite of Private fields; independently new decisions/tests | Unapproved People/genre/metadata grants |
| **B2-S** | Later independently selected Public discovery/search | Source qualification before ranking/limit/offset/hasMore, including Vault tag origin, Film Credit and safe materialization | Whole generic Search permission, unrelated domains |
| **B2-V** | Later explicitly selected generic Vault operations, if actually needed | Feature-owner qualification, metadata safe projection and hidden/absent oracle controls | Shared tag create/rename, Recycle Bin and blanket known-ID metadata |

Separating risk boundaries does **not** itself authorize more total Public capability or automatically require all sub-slices to be implemented. B2-S/V can be deferred indefinitely if the Owner has no concrete use case.

## B2-W1 prospective acceptance — 16 finite requirements, none executed

| ID | Mandatory proof after authorization |
| --- | --- |
| W1-01 | Exact `POST /api/v1/films` selected for PUBLIC only after real persisted login and downgrade-origin tokens, without anonymous grant. |
| W1-02 | PUBLIC request accepts exactly the Owner-approved keys/validations, rejects extra fields and mass-assignment attempts; omitting links/person/genre fields cannot silently attach private relationships. |
| W1-03 | PUBLIC `isNsfw` cannot become true, even through unknown properties, alternate serialization, invalid enum, malformed JSON or duplicate property ambiguity. |
| W1-04 | Successful 201 creates exactly one Film and one correct-type active Vault Entry in one committed atomic unit; any failure rolls back both and joins; no orphan created. |
| W1-05 | Success body has exactly the seven B1 PublicFilmResponse data keys; never full FilmResponse or hidden values even as null, preserving canonical `ApiResponse`. |
| W1-06 | Invalid/missing required fields, bad enum, length overflow and unsupported options return safe errors without writes or private metadata. |
| W1-07 | Existing PRIVATE `POST /api/v1/films` request/response/validation and all PRIVATE update/credit/link/genre flows remain byte/semantic compatible. |
| W1-08 | PUBLIC Film PUT/update, NSFW mutation, all Fiction writes, credits, links, classifications, genre and Vault mutators remain 403 before handler or DB work, even for guessed IDs. |
| W1-09 | PUBLIC direct detail GET for newly created Film obeys B1 D10 non-NSFW + active-correct-type snapshot; no source type/status mismatch. |
| W1-10 | Fail-closed if persisted profile authority, database transaction or authorization context is unavailable; no cached grant/partial write. |
| W1-11 | CSRF/HTTP method/path variants, HEAD, OPTIONS, unknown, suffix and ASYNC/ERROR dispatch cannot bypass the exact route-level B2 grant or B1 restrictions. |
| W1-12 | Two real profiles/origins, revoke/downgrade/refresh race tests preserve DP-A admission and the already-admitted boundary; no silent token privilege escalation. |
| W1-13 | No error, header, Location, response cache, ETag or log leaks restricted fields or raw tokens; safe cache-control contract explicit for Public response. |
| W1-14 | Generated OpenAPI correctly models profile-specific POST request/response variants, authentication, codes, and preserves existing operation IDs and PRIVATE full schemas. |
| W1-15 | Architecture/Modulith tests show only permitted feature/Vault dependencies and no new migration, index, module edge, global transaction manager or new test framework. |
| W1-16 | Exact registered-route deny-sweep membership, focused source-qualified tests, bounded receipts/counters, independent code review and Docs Sync with original DP-A/B1 evidence preserved. |

**Potential implementation seam, not approved:** dedicated `PublicCreateFilmRequest` and owner-local create method, with exact explicit DTO->command mapping and a response projected from the committed creation path, rather than passing a restricted body through broad `CreateFilmRequest`/returning `FilmResponse`. Codex must verify the actual `FilmService.create`, `VaultEntryOperations.create` transaction, validation and event hooks. Do not presume atomicity from annotations without real PostgreSQL testing.

## Separate future B2-S / B2-V analysis gates (do not implement in W1)

1. Enumerate all Search candidates by origin, allowed Public domains/types and tag-origin provenance *before ranking*, including `FILM_CREDIT` and private taxonomy references. Document exact order/tie-break/offset/limit and `hasMore` semantics on a mixed three-page synthetic dataset; test no private candidate affects page shape. A Public-provided `domain`, `entryType` or `tagId` must never broaden server policy.
2. Validate search snippets, similarity/rank metadata and later materialization only from owner-qualified non-NSFW active Film/Fiction. Avoid N+1 cross-owner reads and arbitrary query/raw SQL exceptions.
3. For generic Vault, no grant based merely on shared Vault ID or existing `GET`. Return only separately selected metadata for an eligible owning feature; hidden/absent indistinguishable on selected paths and full 403 on unselected paths.
4. Declare all HTTP and frozen module source exceptions with code-owner paths, and re-review the DP-A/B1 request matcher/dispatcher order under new routes.

## Delivery and evidence governance

- The Owner has selected D12–D17 Option A for **preparation only**. Independent Codex **pre-handoff** review of B2-W1 follows; then Owner publication of reviewed preparation and distinct precise source/API/frozen authorization; only then `$codex-create-handoff` and an approved execution handoff; implementer self-review; separate Codex final review; Owner alone commits/pushes accepted code; publication closeout with actual SHA.
- Prove current feature/DB shape at handoff; do not rely on static source inspection as runtime PASS. Use existing bounded runner, Testcontainers/PostgreSQL, MockMvc, registered-route inventory, OpenAPI and Modulith tests; preserve failed/timeout receipts and source-qualified reuse. Avoid arbitrary full-suite quotas, new agents, dependencies, hooks and unrelated refactoring.
- No stage/commit/push by agents, no provider activation, no Frontend/BFF, no feature-flag widening, no revision of frozen migrations V1–V29, and no automatic DP-C/Recycle Bin/audit start.
- `ACTIVE=NO_ACTIVE_HANDOFF` until independently ready, Owner-published and explicitly authorized new executable handoff. B2 remediation **0 consumed**; no takeover authorized. These are proposed fresh B2 counters, not a reset of B1 3/3 or DP-A 3/3 + 1/1.
