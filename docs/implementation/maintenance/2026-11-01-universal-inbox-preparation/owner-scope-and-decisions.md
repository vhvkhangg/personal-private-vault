# Owner scope and prospective decisions — Universal Inbox / Save for Later

**Date:** 2026-11-01  
**Authority:** Owner requested preparation and source-grounded design after publication of Spring-only Archify `84f11854de20cde4dcdcd7b082b4c40c1410885b`. This authorizes **planning and independent preparation review only**, not implementation, deployment, DB migration or modifying frozen historical contracts.

## Functional intent (approved for design)

- One personal place to save a public or user-supplied **link and descriptive metadata** for later reading/viewing. It must work without third-party integrations or remote content fetching.
- Reuse manual URL saving and saving from a normalized Feed item, with stable duplicate conflict semantics and Vault identity.
- Provide a usable Inbox list (not only an unfiltered last-50 view), item details, categorization via existing kinds, and safe access to existing Vault favorites/tags/trash and explicit Knowledge conversions.
- Support later Daily Trends Radar simply by calling the same accepted Feed Item → SavedResource path. The feature must not depend on Trends being implemented.

## Non-goals, invariant boundaries

1. **No new `inbox` Spring module/table by default.** `feed` owns saved-resource metadata and persistence; `vault` owns favorites, tags, trash and global entry identity. A new shared application-level orchestrator is allowed only if the pre-handoff review proves a concrete requirement; no cross-module entity/repository access.
2. No automatically downloading/copying webpages, fiction, social media images/videos, or third-party accounts. No scraping of X/Facebook/TikTok/Reddit/YouTube, browser automation, content-summary agent or background link preview fetching. A saved social link is only **metadata + URL**.
3. No invented user accounts/multi-tenancy, OAuth credentials, Google Drive, Ideaverse, RAG/embedding, personalized trending or frontend UI. No changing accepted `ApiResponse`, auth/JWT, frozen v1 schema, existing Flyway migrations, or API behavior without separately reviewed impact/Owner authorization.
4. Preserve existing duplicate semantics: `saved_resources.resource_url_hash` is globally unique over the **trimmed stored URL**, SHA-256 UTF-8; duplicate attempts return the existing stable conflict (not silent merge). Equivalent URLs with different tracking parameters are not automatically canonicalized in this slice.
5. Only authenticated Owner business APIs. All lists must be bounded, deterministic, exclude trashed items by default, and never expose another module's internals or stored `raw_metadata` as an unsafe executable/browser payload.

## Decisions: fixed versus proposed

| Subject | Disposition at preparation | Rationale / gate |
| --- | --- | --- |
| Feature owner | **KEEP `feed` + reuse `vault`** | Phase 10 accepted ownership and dependency matrix; no duplicate bookmark module |
| Existing save/convert APIs | **KEEP and reuse** | Already have manual/feed capture, get/list, Study/Information/Note conversion |
| Inbox browsing | **PROPOSED REQUIRED** | Add owner-scoped, stable paged browsing and optional kind filter without breaking existing GET response contract |
| Tags/favorites/trash | **REUSE existing Vault APIs** | Do not duplicate state in `saved_resources` or invent another tag hierarchy |
| `read` / `archived` flags | **OPEN DESIGN DECISION — preferred additive approach if selected** | Neither state exists in frozen `saved_resources` today; do not fake it using `raw_metadata` or reserved tag names. See design alternatives below |
| User-editable title/notes | **DEFER** | Existing create returns immutable metadata; changing owner fields is a separate mutation contract and not mandatory for first browse slice |
| URL lookup/search | **REUSE or narrow extension** | Existing URL-hash lookup and global Feed search; do not create a second full-text search engine |
| New schema/migration | **NOT YET AUTHORIZED** | If user chooses native read/archive state, propose the smallest additive migration, separate Owner/frozen-boundary decision and fresh tests; never alter V1 or applied migrations |
| Diagram/UI | **DEFER to feature delivery / Phase 16** | Spring Archify already published; do not expand diagrams or scaffold frontend during preparation |

### Native read/archive state — proposal, NOT permission to migrate

The user-facing concepts **Unread**, **Read** and **Archived** are not represented by the current `saved_resources` fields. The most proportionate design *if required in the first implementation slice* is to add nullable `read_at` and `archived_at` columns to **existing** `saved_resources` in a new additive Flyway migration, with owner-local transactional mutations. `archived_at` hides an item from the active Inbox while preserving its prior read state; trash remains the separate Vault `deleted_at` lifecycle. No new top-level module, redundant state table or generic workflow framework. Use the next actually available Flyway version **only after explicit schema authorization and independent preparation review**.

Alternative 1 — **reuse-only first slice (recommended default while decision unresolved):** implement Inbox browsing, filtering and reuse saved/favorite/tag/trash flows with **no read/archive state**. This enables real Save for Later but less advanced triage. Alternative 2 — use global tags such as `Unread` / `Archived`: **REJECT** as default because it mixes user tags with workflow and cannot enforce one status reliably. Alternative 3 — embed state in `raw_metadata`: **REJECT** because that is a source/provenance snapshot and lacks a stable indexed mutation contract.

**Decision gate:** The independent pre-handoff review should state `KEEP_REUSE_ONLY` or `OWNER_DECISION_REQUIRED_FOR_READ_ARCHIVE_MIGRATION`, with exact smallest schema/API change and user-visible benefit. No agent may treat this design note as an Owner migration grant. A substantive new decision must be recorded before Codex issues a schema-changing handoff.

## Scope isolation and future roadmap

After an accepted/published first Inbox slice: Daily Trends Radar (provider adapter + bounded due/manual fetch, separately scoped), then two-account Google Drive (OAuth/security separately scoped), then Dashboard/API readiness; finally full non-RAG audit before Frontend Phase 16. The future audit must adjudicate **all actionable severity levels including LOW/INFO**, or close a false positive with evidence, under [existing final audit intent](../2026-11-01-final-non-rag-audit-intent/owner-intent.md). RAG LOW/blockers are not an Inbox scope unless a demonstrated non-RAG safety boundary requires separate Owner decision.
