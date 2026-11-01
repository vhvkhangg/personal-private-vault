# Source-backed reuse inventory and proposed Inbox API design

**Baseline:** published `main` `84f11854de20cde4dcdcd7b082b4c40c1410885b`, inspected through the GitHub repository. This is a **design**, not generated Java code, accepted API or executed test.

## Verified existing backend capabilities

| Current code / contract | Established fact | Reuse in Inbox |
| --- | --- | --- |
| [`SavedResourceController.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/controller/SavedResourceController.java) | `POST /api/v1/saved-resources/manual`, `POST /feed-item`, `GET /{id}`, `GET /?urlHash=&limit=`, plus three Knowledge conversion POSTs and conversion history GET | No duplicate capture or conversion endpoints |
| [`SavedResourceService.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/SavedResourceService.java) | A saved resource is created in the same transaction as a Vault Entry; URL is trimmed and hashed, duplicates conflict, persisted IDs share Vault identity | Keep transactional identity, conflict and rollback semantics |
| [`SavedResourceRepository.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/persistence/SavedResourceRepository.java) | `findRecent` orders by `savedAt DESC, id DESC` but has no cursor/kind/status filter; current controller defaults limit 50, max 100 | Extend via a separate stable paged Inbox read, do not replace legacy query contract |
| [`SavedResource.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/domain/SavedResource.java) | Fields include kind, source metadata, URL/hash, title, summary, savedAt, rawMetadata; no read/archive lifecycle columns | State is a prospective schema decision, not something existing |
| [`SavedResourceKind.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/enums/SavedResourceKind.java) | ARTICLE, REPOSITORY, SOCIAL_POST, WEB_PAGE, OTHER | Existing kind-based organization; not invented categories |
| [`FeedController.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/controller/FeedController.java), [`FeedItemService.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java) | Manage sources and read items; service ingests a **supplied normalized batch**, not a provider HTTP crawler | Trends later connects to same Feed Items; Inbox must not implement any provider fetch |
| [`VaultEntryOperations.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/entry/VaultEntryOperations.java), [`VaultController.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/web/controller/VaultController.java) | Vault Entry owns soft-delete/trash and restore | Avoid duplicate deletion/archive; default Inbox must respect `vault_entries.deleted_at` |
| [`VaultMetadataOperations.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/metadata/VaultMetadataOperations.java) | Owns favorite/unfavorite, ratings, tags and attach/detach | Reuse existing APIs, do not copy favorite/tag columns into Feed |
| [`FeedSearchService.java`](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/search/FeedSearchService.java) | Feed has scoped saved-resource search over title/source/author/summary and Vault qualification | Reuse existing search; no second search subsystem in v1 |
| [`phase-10/README.md`](../../phase-10/README.md) and [`module-dependency-matrix.md`](../../../architecture/module-dependency-matrix.md) | Feed owns `saved_resources`, `saved_resource_conversions`; allowed deps `vault`, `knowledge` through public contracts | Extend Feed under separately approved bounded maintenance; do not reopen frozen Phase 10 history |
| [`Schema v1 DBML`](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml) | `saved_resources.id` references `vault_entries.id`; `resource_url_hash` unique; `vault_entries.deleted_at` drives trash; no read/archive columns | Never rewrite frozen V1; plan only additive migration after approval |

## Proposed first-slice behavior — reuse-only default

**User flow:** Save an external URL manually (title, kind, URL), or save an already-ingested Feed Item; browse all non-trashed saved resources through a predictable, bounded Inbox; filter by existing kind, retrieve details, open external link in a future frontend, and use existing favorite/tags/trash/restore/conversion actions. Missing/deleted resources should not leak through the Inbox list. This can work entirely without automated content downloads or new provider integrations.

**Gap:** existing `GET /api/v1/saved-resources` returns a **list** of recent items, not a cursor page. Do **not** silently change that route's return envelope or the `urlHash` semantics; other consumers may depend on them.

**Prospective additive API (not yet implemented):**

```text
GET /api/v1/saved-resources/inbox?kind=ARTICLE&limit=50&cursor=<opaque>
  -> ApiResponse<InboxPageResponse>
  -> { items: SavedResourceResponse[], nextCursor: string|null }

Existing endpoints remain unchanged:
POST /api/v1/saved-resources/manual
POST /api/v1/saved-resources/feed-item
GET  /api/v1/saved-resources/{id}
GET  /api/v1/saved-resources?urlHash=&limit=
POST /api/v1/saved-resources/{id}/convert/{study|information|note}
GET  /api/v1/saved-resources/{id}/conversions

Vault remains owner of favorite/tag/trash/restore operations:
PUT    /api/v1/vault/entries/{id}/favorite
DELETE /api/v1/vault/entries/{id}/favorite
PUT    /api/v1/vault/entries/{id}/tags/{tagId}
DELETE /api/v1/vault/entries/{id}/tags/{tagId}
DELETE /api/v1/vault/entries/{id}
POST   /api/v1/vault/entries/{id}/restore
```

The new Inbox route is the minimum suggested extension, **not** an accepted API name or new public contract. Codex pre-handoff review may propose a smaller backward-compatible owner-local design.

### Query and safety rules

- Deterministic keyset: `(saved_at DESC, id DESC)` with cursor referring to the last returned tuple, stable tie-breaking and at most an approved finite limit (suggest retain existing max 100). Never return unbounded rows, calculate N+1 Vault metadata per result or change the existing global-search contract.
- Inbox default excludes Vault trashed entries. Source of truth is `vault_entries.deleted_at`; determine an allowed read path through Vault's public API or a justified owner-approved read projection. **Do not directly inject Vault repositories/entities into Feed**. If a new cross-owner read API is unavoidable, justify and test its minimal batch contract in the reviewed handoff.
- Optional kind filter uses existing SavedResourceKind. Cursor must be opaque/validated, bound to chosen filters, and return a stable 400 for malformed/tampered/incompatible cursor without surfacing sensitive URL/title. Count/hasNext should be scoped and not require an expensive full count by default.
- Exact duplicate-save semantics remain existing conflict, even if the same link is already trashed; changing this is a *different* owner decision. Never silently resurrect or merge an old Vault entry.
- Raw metadata is provenance and may contain external text; preserve deep snapshots, bounded input policy and safe JSON response behavior; never render it as trusted HTML or use it as an Inbox-status storage hack.
- External URLs are user data, not automatically permission to fetch/download; future browser uses safe link handling and scheme checks. Do not silently tighten frozen manual-save acceptance without an approved compatibility decision.
- Reuse authenticated owner-only HTTP envelope, owner-specific validation/error advice and OpenAPI conventions. No unauthenticated or public crawler endpoints.

### Optional second capability: read/archive state (NOT APPROVED)

Only after a distinct Owner/schema decision, consider adding nullable `read_at` and `archived_at` to `saved_resources` with an additive migration and owner-local mutation APIs. Never change frozen DBML v1 or existing Flyway files; derive the next migration version from actual tree. Read-state and archive-state are **not** Vault trash, favorite, or tag state. Avoid materializing that feature merely because it appears in this design.

## Technical risk questions for Codex review

1. Is the list truly missing a safe, bounded page for the intended Inbox, or is an existing Vault/search projection enough? Compare concrete API consumers before selecting the new route.
2. What is the smallest correct read path to exclude `vault_entries.deleted_at` without illegal cross-module repository access, security leakage, or per-row N+1 calls?
3. Which existing endpoint returns a trashed SavedResource by ID? Does the Inbox need only list filtering, or a separately approved change to get/search semantics? Do not silently revise accepted behavior.
4. Does proposed cursor keyset need an index on `saved_at,id`, and what is the measurable size/expected query shape? Avoid an unjustified migration or generic pagination engine.
5. Is read/archive state essential in v1? If yes, return a concrete Owner decision request; if not, deliver reuse-only browsing first.
