# DP-B1 — Eligibility, consistency, ownership and information-leak contract

**State:** [Owner-ratified D10/D11 Option A](owner-ratification-d7-d11-2026-11-12.md), **not independently reviewed or implemented**. DP-A remains the controlling default-deny authority until independently reviewed preparation, separate source/frozen authorization and accepted implementation.

## 1. Source eligibility predicate (PUBLIC direct details only)

For requested ID `x`, **all** conditions are mandatory for a PUBLIC `200`:

- A valid DP-A signed `PROFILE_PUBLIC` bearer resolves to a persisted current, non-revoked session/generation/epoch (existing DP-A admission, no copy of auth logic into the feature modules).
- Exactly one owning feature `Fiction` or `Film` row has `id=x` and `isNsfw=false`. **A null, missing or unknown classification must never count as public**; current source stores a non-null boolean.
- Its backing `vault_entries.id=x` exists with `entry_type=FICTION` or `FILM` respectively and `deleted_at IS NULL`.
- Read and eligibility are evaluated consistently. Do not first serialize a full feature DTO and then remove fields; select only allowed fields and avoid loading People, Media, credits, links or private taxonomy where feasible.

For `PUBLIC`: record missing, NSFW true, Vault entry missing, wrong type or trashed **all yield the exact same 404 response** (and no extra relevant headers/redirect). `PRIVATE` continues current accepted existing direct GET and domain rules; do not silently add a new soft-delete restriction to PRIVATE as a side effect of B1.

## 2. Transaction and ownership mechanism — reviewer must choose the simplest lawful implementation

**Observed:** `FictionService` and `FilmService` already depend on `vault.entry.VaultEntryOperations`, whose `find` exposes `entryType` and `deletedAt` through a named interface. A feature repository and a Vault read are two different storage reads. Merely checking `isNsfw` in a service after `findById` leaves Vault deletion/type unqualified; blindly trusting a separate Vault result across a concurrent write may give inconsistent snapshots under PostgreSQL READ COMMITTED.

**Proposed design requirement:** carry the feature and Vault eligibility through a **single consistent read snapshot**, e.g. an owner-local read transaction with a justified PostgreSQL isolation/snapshot strategy and existing Vault named capability. Independent pre-handoff review must verify transaction semantics (`@Transactional` propagation, JPA first-level cache, concurrent mutation semantics and possible service proxy invocation) and reject an unproven two-check design. Another narrow, ownership-compliant mechanism may be approved if equivalent and verified. Avoid cross-module SQL into `vault_entries` from Fiction/Film without explicitly approved architecture exception; do not import `vault.internal`, invert Vault dependencies or invent a shared authorization engine.

**Admission/commit boundary:** for a request that starts after a completed PRIVATE update toggles `isNsfw=true` or a Vault trash commit, PUBLIC must see 404. A pre-commit read legitimately started against a consistent earlier snapshot may complete with already-authorized **minimal** public fields; such a race cannot be advertised as instantaneous retroactive byte erasure. A direct Public read must not manufacture a fresh authorization snapshot after the NSFW/trash commit while using old cached feature data. Tests should force both orderings under PostgreSQL barriers, not just simultaneous-start timing.

## 3. Hidden vs absent and 403 vs 404

- **401:** no bearer, invalid/mismatched signed profile, revoked/expired session, stale generation/epoch or authority DB error. No Private fallback from `sub=1`.
- **403:** authenticated PUBLIC uses a route **not** selected in the B1 exact allowlist: nested relation, direct credit, genre, metadata, Search/Vault, Reference platform/taxonomy, writes, malformed unselected method or unexpected dispatch. Must not read existence/perform side effects first.
- **404:** authenticated PUBLIC uses **selected** Fiction/Film detail path, but matching eligible content is absent/NSFW/trashed/wrong type. Use the existing generic `FICTION_NOT_FOUND`/`FILM_NOT_FOUND` body without hidden field values, distinguishing query hints or debug messages. PRIVATE retains existing generic not-found semantics.
- **Reference missing code:** preserve existing catalog 404, unrelated to hidden Fiction/Film entry ID. No reliance on user-visible HTTP timing equality as a formal security guarantee; match method/status/error/body and observable headers instead.

**Cache rule (proposed D11):** use `Cache-Control: no-store` for PUBLIC successful Fiction/Film projections and hidden/absent 404s; no public/shared cache hit on prior PRIVATE full-body responses. Vary/end-to-end cache scope must not leak full Private DTO, ETag, Last-Modified, Content-Length or Location metadata across profiles. Validate HTTP responses, not merely service return values; do not assume a Frontend/BFF change is possible (paused).

## 4. Secondary paths that remain denied

1. Film `/credits/{creditId}` and `/{id}/credits` may expose People IDs, character/note metadata and hidden parent links. They remain 403 for PUBLIC even when the Film itself is non-NSFW.
2. Fiction/Film classifications, assigned genre IDs, story archetypes, world settings and links may expose owner-local taxonomy or private URLs. No Public B1 grant.
3. `posterUrl` may point to privately stored Media or third-party trackers. No Public B1 URL or bytes; never leak via `Location`, redirect, headers or eager DTO mapping.
4. Search/recommendations, Vault tags/ratings/favorites/trash, pagination/sort/count/`hasMore` and import/export remain B2+ or outside DP. B1 adds **no** search/list aggregation. If an implementation incidentally calls a shared search or tag path for Public, reject it.
5. Reference `PlatformResponse` contains `url` and timestamps; `StoryArchetypeResponse`/`WorldSettingResponse` contain free-text descriptions. Their GETs are excluded, even though all currently share a read-only controller.
6. PRIVATE remains subject to all existing business permissions and feature flags; an authorization profile alone does not relax invariants on update, links or ownership.

## 5. Failure behavior and operational constraints

- DB unavailable/transaction fails: no accepted Public projection from stale/partial eligibility; use existing safe 5xx handling/redaction; never respond with a previously resolved full Private DTO.
- Errors should not print titles, creator/people IDs, private URLs, SQL rows or token/session secrets. Reuse canonical `ApiResponse` and existing error advice instead of broad new advice handlers.
- No new Flyway migration is expected. If a new column/index/DDL is truly necessary, the implementer must stop before changing frozen schema and obtain explicit bounded Owner consent; explain the evidence and cheaper alternatives.
- No new Public record-level visibility flag, second Owner, Profile Session implementation rewrite, frontend cache migration or live provider activation.
- Feature routes remain private-first. Allowlist only the exact B1 routes and re-test DP-A negative matrix for all other paths including ASYNC and ERROR. Existing `SecurityConfiguration.anyRequest().hasAuthority("PROFILE_PRIVATE")` must remain an effective fallback for unselected Public access.
