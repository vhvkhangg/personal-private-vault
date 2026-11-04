# DTR26-S2 — Owner scope and bounded design choices

**Owner request (2026-11-04):** Begin Daily Trends Radar S2: **RSS/Atom + Scheduled Refresh**, after the successful S1 publication/closeout. This is Owner approval for **preparation and independent pre-handoff review** of this scope, **not** approval to implement, execute arbitrary RSS requests, start a scheduler or change frozen Schema v1. Exact implementation authority is issued only after reviewed preparation is Owner-published.

## S2 goals

1. A configured **RSS** `feed_source` may manually fetch one explicitly supplied RSS 2.0 or Atom 1.0 feed URL, normalize a bounded set of entries into already owned `feed_items`, and display metadata/link-only results.
2. A cached, bounded **combined** Radar view includes valid, recent HN and RSS entries, while preserving the **existing HN-only** `/api/v1/feed/radar` contract. A FeedItem remains eligible for the accepted Save-to-Inbox operation; no duplicate inbox implementation.
3. **After S2A acceptance**, a separately gated scheduled-refresh slice can periodically process opt-in due `HACKER_NEWS` or `RSS` sources via their existing accepted refresh paths, without DB locks during HTTP.
4. Keep accurate source attribution, publication timestamps, fetch timestamps and provider provenance. This is **not** an external ranking claim for RSS.

## Owner-directed boundaries and preparation decisions

| Subject | Preparation disposition | Why / review gate |
| --- | --- | --- |
| Ownership | **KEEP** `feed` / existing `feed_sources`, `feed_items`, Vault-backed `saved_resources` | Same schema and Spring Modulith boundary; no new module. |
| URL identity | **RECOMMENDED S2A DEFAULT: `ARTICLE_URL`**, consistent with S1: one FeedItem per **exact trimmed item link URL per source**, `externalId=null`; RSS guid / Atom id retained only as provenance | Avoid the generic ingestion's `(sourceId, externalId)` versus `(sourceId, urlHash)` conflict. Codex must verify representative and legacy/upsert cases. The earlier explicit HN `ARTICLE_URL` Owner decision applies to HN, not automatic approval of RSS specifics. |
| RSS/Atom | **IN S2A:** RSS 2.0 and Atom 1.0 only, bounded metadata/link-only; no content/enclosure fetch | The `RSS` enum exists; parser/transport do not. More dialects and extensions need separate approval. |
| Arbitrary feed URL | **IN S2A:** only *configured* `feed_url` is a fetch destination; validate strictly at fetch time; require HTTPS, public DNS, default port 443, real-connection destination enforcement and no redirects/proxies | Existing source create/update accepts arbitrary metadata URLs by frozen contract; do **not** silently tighten it or fetch `source_url`. RSS arbitrary hosts have a broader SSRF threat than S1's fixed HN host. |
| Item URLs | **IN S2A:** accept absolute HTTP(S) article links as stored data only; do not follow, scrape or retrieve them. Relative links and nonstandard XML base handling **explicitly deferred** for first slice | Honest limited compatibility is safer than implementing an unverified IRI/base resolver or silently assuming links absolute by standard. Reviewer may request bounded coverage for common feeds if justified. |
| Item identity within feed | **IN S2A:** group duplicate exact stored links before calling generic ingestion; choose latest valid publication timestamp then deterministic GUID/Atom ID tie-break, otherwise earliest document order | Prevent repeat of S1 pre-handoff PH-01. Across refreshes, a changed article URL creates a new FeedItem; old entry retained. Do not change uniqueness. |
| RSS/Atom dates | Use RSS `<pubDate>` or Atom `<published>` when valid; otherwise `publishedAt=null` and entry stays accessible by per-source reads but not the 24h published-time Radar | Atom `<updated>` is **not** necessarily original publication time. No guessed timestamps or treating fetch time as publication. |
| API compatibility | **Proposed additive routes** `POST /api/v1/feed/sources/{id}/refresh-rss` and `GET /api/v1/feed/radar/combined` | Preserve HN's existing `/refresh` response fields and HN-only `/radar` semantics without making HN-specific counts masquerade as RSS counts. Review exact routes and OpenAPI before handoff. |
| Scheduler | **ONLY S2B:** explicit global opt-in OFF by default **and** existing per-source `scheduled_refresh_enabled`; due-only HN+RSS, single process, bounded work/cooldown | User asked to plan it, not to enable background outbound traffic now. Do not reinterpret existing scheduling flags as an executing scheduler. |
| Frozen SQL/Schema v1 | **NO ADDITIONAL SCHEMA/MIGRATION by default**; preserve `next_fetch_at` only after successful generic ingestion | If durable retry tracking, multi-process leases or stronger semantics genuinely require DB changes, `OWNER_DECISION_REQUIRED` before authorization. |
| Libraries/tooling | **Prefer existing** Java XML, HTTP, Spring scheduling and accepted bounded runner, verify exact available APIs in execution environment | No speculative new POM dependency, native runtime download, generic framework or new agent. |

## Mandatory decision gates before an executable handoff

- **RSS `ARTICLE_URL` identity** is the recommended and fully specified preparation default, **not** a misquoted extension of the HN-only Owner authorization. Independent reviewer may accept it as proportionate within this Owner-requested design, but if the distinction between article versus feed-entry identity materially changes intended Save-for-Later behavior, return `OWNER_DECISION_REQUIRED` with both exact alternatives before issue of S2A handoff. Never invent consent from the HN choice.
- **Public-host RSS egress controls** and parsing hardening must have a feasible client/parser on the actual resolved dependency graph, including negative tests for real peer pinning and XML external references. If not feasible with reviewed local tooling, `OWNER_DECISION_REQUIRED`; no fallback to unsafe generic URL fetch.
- **S2B activation** is independently owner-gated after S2A. The single-process/explicit opt-in assumption must be checked against deployment configuration. If multiple app instances or mandatory durable retry semantics are needed, stop for separate design rather than claiming a local single-flight guard is cross-process.
- **Existing status:** ACTIVE remains `NO_ACTIVE_HANDOFF`. HN `ARTICLE_URL` stays accepted, S1 exhausted remediation 3/3 and one accepted takeover stay historical, no RAG Phase 19 or S19-2 action.

## Exclusions

Never fetch entry links, enclosures, Atom `content src`, XSLT, external entity/DTD, XInclude, XML schema references, link previews, HTML media or source favicons. No request to fetch metadata from arbitrary article hosts, no auth credentials or cookies for feeds, no content mirroring. No rate/score synthesis, external GitHub Trending/Reddit/social scraping, broad redesign of `ApiResponse`, OAuth, frontend/UI, RAG, Drive, Ideaverse or historical ADR/source/schema reinterpretation.
