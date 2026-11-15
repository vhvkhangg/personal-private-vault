# BF26-V1 — published source baseline, seam qualification and risks

**Static inspection baseline:** GitHub `main` `2d088445e6323b091741cf370e1e0c8cf13928a2`, 2026-11-15. Git blob IDs below identify individual committed file contents; they are **not** SHA-256 hashes and do **not** prove new runtime behavior. `J/` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` = `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

## 1. Exact source observations

| Path / Git blob | Observed source | Preparation implication |
| --- | --- | --- |
| `J/vault/entry/VaultEntryOperations.java` `22f1392dd16206422146ac770ec6d251f55e7975` | Synchronous named API supports create/find/findAll(ids)/moveToTrash/restore, no list-trash | Add only a bounded read capability; do not change old signatures |
| `J/vault/internal/application/entry/VaultEntryService.java` `648100017d024b7bea19c9cc9e7496b1c04ebe28` | Class-level `@Transactional(readOnly=true)`; writes individually `@Transactional`; existing mapping to immutable `VaultEntryView` | Owner-local page/codec may reuse class-level read-only behavior; do not alter mutation locks, timestamp/idempotence, or source guards |
| `J/vault/internal/infrastructure/persistence/VaultEntryRepository.java` `b605742a45c95ff8a348407e724a63162d090531` | `JpaRepository`; existing `findSourceViews(ids)` and `findByIdForUpdate`, no trash-page query | Add a bounded `deletedAt != null`, ID-desc seek projection/query inside Vault; validate PostgreSQL actual SQL limit and plan |
| `J/vault/internal/domain/VaultEntry.java` `6951009010e00da324f81de0de53ab216c16131d` | `id` identity, `entryType`, `createdAt`, `updatedAt`, `deletedAt`; isDeleted uses non-null deletedAt; trash/restore modify timestamps | No entity/schema change needed for proposed metadata read |
| `J/vault/view/VaultEntryView.java` `fd31b574276bd818e0b36956f5ff6367a2e60738` | Immutable five-field ID/type/timestamp view | Safe as internal page item before mapping down to a three-field HTTP DTO; no new business entity projection necessary |
| `J/vault/internal/web/controller/VaultController.java` `de14f47df09f3ac1726d3c8655663eaf5c8c42e5` | Base `/api/v1/vault`; routes `/entries/{id}` GET/DELETE and `/entries/{id}/restore` POST, plus metadata/tags; no collection GET | Add distinct `GET /trash` without changing existing ID handlers; document stable operationId |
| `J/vault/internal/web/mapper/VaultWebMapper.java` `4f3b3a9ae7678d703a828c163fdfa7beaed35775` | Pure `VaultEntryView`→five-key VaultEntryResponse mapping | Add a separate three-key trash-item mapper; do not shrink existing `VaultEntryResponse` |
| `J/vault/internal/web/dto/VaultEntryResponse.java` `3cbd4fbfd88fa2e7be6580e332cab33610ac67e9` | Existing full metadata DTO has five fields | Keep it untouched; use dedicated list-item type for exact privacy surface |
| `J/authentication/internal/infrastructure/security/SecurityConfiguration.java` `ae7e487a11cfe8ab19056d40aec23e6e28f50e7a` | Exactly eight PUBLIC business GET pairs and PRIVATE `anyRequest` fallback | New `/api/v1/vault/trash` remains PRIVATE-only without security source edit; new positive 403/401 regression required |
| `J/ApiResponses.java` `ca1b5f87dc184a9c7f1c3d606b4e941b7d9484ba`; `J/ApiPageMeta.java` `fda21d2806917d562b2b8572c462cbc1a449f6df` | Canonical envelope supports `meta.page` but page meta uses offset; another feature has page `items`+`nextCursor` | Reuse envelope, not offset paging for keyset; no root API contract refactor |
| `J/ApiExceptionHandler.java` `75eb725b8559e9766f08649fbd31a5be3edb1e6d` | Type mismatch yields safe 400 MALFORMED_REQUEST; IllegalArgumentException yields 400 INVALID_ARGUMENT; unexpected errors are safe INTERNAL_ERROR 500 | Prefer owner-local parameter/cursor validation reusing root safe advice; no global handler edit |
| `T/vault/VaultEntryIntegrationTest.java` `6ae55cd88a3d856c50ac8cd2f311ad551b93bd51` | PostgreSQL lifecycle/idempotence tests exist | Potential test home for repository ordering/bounded cursor/restore between pages |
| `T/vault/VaultWebIntegrationTest.java` `63c7d7dd659559c0b590355f425a0483686acd93` | Existing HTTP trash→restore lifecycle and Vault metadata tests | Add new request and exact three-key/paging validation, retain existing lifecycle |
| `T/authentication/ProfileRouteAuthorizationIntegrationTest.java` `d173776b8b128287efff1f3bb177cbd181d0f3af` | Exact-eight-PUBLIC-business-route and deny sweep | Explicitly retain eight allowed pairs; new route PRIVATE positive/PUBLIC deny and anonymous 401 |
| `T/web/OpenApiRouteInventoryIntegrationTest.java` `e72b4142e9c362bb0d1ce1ca7acf85525930eeca` | Large exact manifest of HTTP routes, operation IDs and schema targets | Add one GET and validate actual generated response, query limits/cursor and security semantics; no fabricated schema names |

`J/vault/AGENTS.md` `d816ed775c14eb38fc33ba353e3e6640b5ef9069` states frozen Phase 1/13 behavior; the Owner must separately grant a bounded exception after review. Vault module has zero business dependencies, and only its named APIs may be used; no direct feature repository/entity access.

**Cross-feature precedent, read-only:** `J/feed/internal/application/SavedResourceInboxCursor.java` implements bounded versioned Base64url cursor; `J/feed/internal/web/dto/SavedResourceInboxPageResponse.java` models `{items,nextCursor}`. These are **patterns to study**, not permitted dependency imports or a shared cursor library. Do not reuse Feed data semantics or IDs.

## 2. Privacy and correctness hazards requiring independent scrutiny

1. **DP-A/RO-A/RO-B security precedence:** old Backend-first text said "authenticated" generically; current policy means **PRIVATE only** for new Vault route, while the eight B1 PUBLIC GET pairs are frozen. No extra PUBLIC permission or mistaken Public-suitable ID metadata.
2. **Sorting realism:** `id DESC` is not `deletedAt DESC`. A user who wants chronological trash requires another ordered index/cursor decision. Do not silently advertise "recently deleted".
3. **Query cost:** although the schema already stores ID and deletedAt, selectivity on an ID-desc bounded query can degrade when deleted rows are sparse. `limit+1` avoids response oversizing but not necessarily large index scans; inspect EXPLAIN/actual SQL on representative synthetic data. No performance claim is established here.
4. **Keyset precision:** continuation marker must be last *emitted* item, not the extra lookahead row; otherwise an item is skipped. Strict `id<cursorId` protects stable-order pagination. No negative/overflow or noncanonical cursor should reach SQL.
5. **Race/restore:** membership can change between requests. No traversal snapshot across HTTP pages; test restore between pages and explain retrash/high-ID inclusion limits.
6. **Root envelope:** `ApiPageMeta` has `offset`; using it with cursor would misstate facts. Dedicated `items/nextCursor` in `data` is the simpler documented proposal, not a global contract rewrite.
7. **Schema/route inventory:** root OpenAPI customizer and exact route test may require scoped changes beyond controller/DTO annotations; do not infer production `OpenApiConfiguration.java` must change until generated graph is verified in a later source-authorized run.
8. **HTTP privacy:** `VaultEntryResponse` exposes five harmless metadata fields now, but this slice's *proposed* list intentionally limits to three. Validate JSON key equality and no-store; no feature titles, private metadata, linked relationships or contents.
9. **Frozen phase and source ownership:** read belongs wholly in Vault; code must not call Search/Feed/People/Film/Fiction to decorate trash items, change DB migration, rewire modules, alter source edit/reservation guards or add cross-module SQL.
10. **No runtime proof:** source/blob inspection confirms seams only. PostgreSQL query performance, cursor serialization, generated OpenAPI, auth denials and consistency guarantees require future focused tests.

## 3. Other governing references

- [Backend-first assessment](../2026-11-11-backend-first-assessment/backend-readiness-assessment-2026-11-11.md), section 6, is the pre-selection **proposal**, not execution authority.
- [Backend-first Owner decision](../2026-11-11-backend-first-assessment/owner-decision-backend-first-2026-11-11.md) retains Frontend pause and source/branch preservation.
- [Current Owner policy](../../../workflow/current-owner-delivery-policy.md), [ACTIVE](../../handoffs/ACTIVE.md), [Docs Sync](../../../workflow/documentation-synchronization.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [dependency matrix](../../../architecture/module-dependency-matrix.md), [frozen schema](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml).
- [RO-B actual-SHA closeout](../2026-11-14-bf26-dp-ro-b-preparation/owner-publication-closeout-dp-ro-b-2026-11-15.md) supplies the current accepted access boundary; older current-looking status messages are history.
