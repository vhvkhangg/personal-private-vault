# Daily Trends Radar — preparation (DTR26)

**Date:** 2026-11-02  
**Baseline:** published GitHub `main` at [`6da26d4a457f3465e8b43489b95202d886787036`](https://github.com/vhvkhangg/personal-private-vault/commit/6da26d4a457f3465e8b43489b95202d886787036) — `docs(feed): close published universal inbox browsing slice`.  
**Disposition:** `READY_FOR_HANDOFF / OWNER_APPROVED_ARTICLE_URL / MAINTENANCE_FEATURE_PREPARATION / NO_IMPLEMENTATION_HANDOFF`.  
**Canonical ACTIVE:** [`NO_ACTIVE_HANDOFF`](../../handoffs/ACTIVE.md) at this baseline.

The [2026-11-02 independent pre-handoff re-review 1](reviews/2026-11-02-independent-pre-handoff-re-review-1.md) closes **DTR26-PH-01/02 for preparation** and returns `READY_FOR_HANDOFF`. The Owner explicitly selected **`ARTICLE_URL`**: one FeedItem per exact trimmed article URL, `externalId=null`, deterministic HN representative provenance, and Save to Inbox stores the article URL. The finalizer design protects source-type/enabled authority through the shared ingest commit while preserving existing lock order. The [initial CHANGES_REQUESTED review](reviews/2026-11-02-independent-pre-handoff-review.md) remains historical; the [correction response](ph01-ph02-correction-response-2026-11-02.md) records the Owner decision and bounded repairs. All runtime acceptance cases remain `NOT_RUN`; no implementation remediation round was consumed.

## Intent and delivery order

Develop a genuinely useful **single-user, source-attributed daily technology/news radar** in the existing `feed` owner module. It must ingest only permitted public link/metadata, show when and from where an item was obtained, and let the Owner use the already published Universal Inbox save flow. No AI agent, scraper, content mirror, automated social-media downloader, copyright-unclear fiction copy, Hermes/OpenClaw, RAG, or Ideaverse.

**Recommended bounded slices** (neither is implemented by this package):

1. **DTR26-S1 — independently reviewed preparation, pending Owner publication and a separate executable handoff:** official Hacker News API adapter, explicitly triggered **manual** refresh for a configured `HACKER_NEWS` source, a bounded last-24-hours Radar read endpoint, and reuse of the existing Feed Item → SavedResource API. DTR26-S1 presents only live HN story records with a valid HTTP(S) external article URL; text-only/Ask HN entries or entries without such an external article URL are skipped, and Save for Later saves the external article URL. No background scheduling.
2. **DTR26-S2 — prospective, separately Owner-approved after S1 publication:** secure RSS/Atom ingestion and optional, bounded scheduled refresh by reusing existing `feed_sources` scheduling fields. This is not silently authorized or part of the S1 acceptance gate.
3. **Optional later provider decisions:** GitHub repository discovery using official REST search (must not be called literal GitHub Trending), Reddit only after access/policy review, other sources only through individual documented permissions. No X/Facebook/TikTok scraping.

This sequence yields one useful deliverable without prematurely building a generic provider framework or adding a queue/microservice. Calendar-day "today" is not implied: S1 **explicitly uses a rolling 24-hour HN submission-time window**, and distinguishes source-provided ranking from mere recency or linked article publication time.

## Read in order

- [Owner scope and decisions](owner-scope-and-decisions.md)
- [Source reuse and proposed API](source-reuse-and-api-design.md)
- [Provider, security and data-policy constraints](provider-security-and-ranking.md)
- [Acceptance and review plan](acceptance-and-review-plan.md)
- [Phase 10 frozen Feed contracts](../../phase-10/README.md)
- [Published UI26 closeout](../2026-11-01-universal-inbox-preparation/owner-publication-closeout-2026-11-02.md)
- [Feed AGENTS](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/AGENTS.md), [current owner policy](../../../workflow/current-owner-delivery-policy.md), [ACTIVE](../../handoffs/ACTIVE.md)

## What the repository already has

The following are **code-confirmed** at the baseline, not proposed features:

| Existing owner/API | Reuse |
| --- | --- |
| `feed_sources`, `FeedSourceService`, `FeedSourceOperations` | create/update sources, `type`, enabled flag, existing refresh settings, due-source read |
| `FeedItemOperations.ingestFetch(sourceId, fetchedAt, normalizedItems)` | normalized per-source upsert and atomic fetch-time updates; no HTTP provider implementation yet |
| `FeedItemRepository.findRecentBySource` and `GET /api/v1/feed/sources/{sourceId}/items` | existing source-scoped item access |
| `feed_items`, `SavedResourceOperations.saveFeedItem` | stable source metadata and Save to Inbox |
| `GET /api/v1/saved-resources/inbox` | published UI26 active-only keyset Inbox browsing |
| `ApiResponse`, module-local controller/DTO/exception rules | existing authenticated REST pattern |

The frozen `FeedSourceType` enum already contains `HACKER_NEWS`, `RSS`, `GITHUB_TRENDING`, `REDDIT`, `WEBSITE`; an enum constant does **not** mean its network adapter exists or is approved. Existing `feed` allows dependencies only on the public `vault` and `knowledge` contracts. No new top-level module is justified.

## Gated workflow

1. Apply **this documentation-only package** and review the scope against the actual checked-out HEAD; if the remote HEAD has moved, Codex checks source applicability rather than pretending the baseline is still current.
2. Run **`$codex-pre-handoff-review`** in the existing **explicit Owner-approved maintenance/feature preparation** mode. It may change only these prospective design documents and issue a separate review, not production code or ACTIVE. It must decide whether DTR26-S1 is `READY_FOR_HANDOFF`, `CHANGES_REQUESTED` or `OWNER_DECISION_REQUIRED`. Read the exact technical conditions and security gates first.
3. On `READY_FOR_HANDOFF`, Owner reviews and publishes the preparation (only Owner commits/pushes); then `$codex-create-handoff` can issue **one** detailed S1 handoff, provided reviewed scope and Owner authority remain current. Antigravity implements and self-reviews, then an independent Codex session reviews; Owner alone publishes after acceptance.
4. Record S1 publication closeout before asking for/authorizing S2. A source reader or an accepted S1 does not independently authorize a scheduler, RSS, GitHub, Reddit, Drive or frontend.

**Docs Sync Check:** This package introduces only proposed maintenance preparation. Existing ACTIVE, current code, migrations, DBML, accepted diagrams and historical reviews remain unchanged. The obsolete status headings in broader historical roadmap/policy are future documentation cleanup matters, not grounds to rewrite frozen evidence here. All previously open LOW/INFO remain open unless separately adjudicated.

**Next step:** Owner, then Codex | Owner reviews/stages/commits/pushes this preparation; then `$codex-create-handoff` for DTR26-S1 only | this README and [independent re-review 1](reviews/2026-11-02-independent-pre-handoff-re-review-1.md) | reviewed preparation published and baseline/authority still applicable; `NO_ACTIVE_HANDOFF` until separately issued | preparation rounds not consumed; implementation `0/3 / NOT_STARTED`, takeover `NOT_AUTHORIZED`.
