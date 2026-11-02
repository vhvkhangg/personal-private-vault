# Provider, security, freshness and data-use policy — DTR26

**Status:** corrected S1 security proposal responding to `DTR26-PH-01/02`; no provider HTTP execution or credentials have been authorized by this documentation. Source facts verified on 2026-11-02; provider terms/API may change and must be rechecked at implementation.

## Provider decisions

| Provider | Available official surface | S1 disposition | Truthful product label |
| --- | --- | --- | --- |
| Hacker News | Official Firebase/HN API `topstories`, `newstories`, individual `/item/{id}.json` | **SELECTED** (read-only metadata, explicit manual refresh) | `Hacker News Top Stories` or `HN: top list at fetched time`; current list position is a snapshot |
| RSS/Atom | Publicly provided feed endpoint per publisher; no general permission to scrape surrounding sites | **S2 planned**, separately authorize | `Latest from <source>`; publication recency is not popularity |
| GitHub | Official REST repository search with creation/updated qualifiers; separate GitHub web Trending list has **no equivalent official REST Trending endpoint** | **DEFER** to a later provider scope | `Recently created/popular repositories` if using Search; not `GitHub Trending today` |
| Reddit | API access requires approval under current Responsible Builder policy; restrictions on scraping/retention | **DEFER** until actual permission/credential review | Only the scope actually approved by Reddit |
| X/Facebook/TikTok | No source authorization in this project | **EXCLUDED** | No automatic fetch/scrape/downloader |

**References (read-only design sources):**

- Official HN API: https://github.com/HackerNews/API (v0 documented `topstories`, story fields, deleted/dead flags).
- GitHub REST Search: https://docs.github.com/en/rest/search ; Search caveats/rate limits: https://github.com/github/docs/blob/main/content/rest/search/search.md
- GitHub Trending availability distinction: https://www.searchapi.org/docs/github-trending-api (third party explicitly notes absence of official GitHub Trending API; do not use that third party without new approval).
- Reddit: https://mods.reddithelp.com/hc/en-us/articles/16693988535309-Moderation-Bots-Tooling and https://support.reddithelp.com/hc/en-us/articles/360043512931-Don-t-break-the-site (2026 access policy; no scraping fallback).
- SSRF: https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html ; https://developer.mozilla.org/en-US/docs/Web/Security/Attacks/SSRF

## S1 fixed endpoint vs untrusted content

S1's HTTP client should contact **only** known official HN API endpoints on the official HTTPS host. Its API root and allowed paths are fixed in an adapter/verified configuration, never copied directly from arbitrary `feed_sources.source_url`, `feed_url` or untrusted story `url`. Reject redirects for S1 unless a later approved policy demands them; no content retrieval from article URLs, HN story links, item authors, assets or other arbitrary domains.

- Enforce HTTPS host identity, expected port, resolved-address egress safety (no loopback/private/link-local/multicast/reserved/metadata destinations), DNS rebinding defense as supported by the actual HTTP client, bounded redirect count (prefer zero), no credentials in query strings, and no proxy bypass. A hard-coded hostname **alone** is not a complete guarantee against poisoned DNS/resolution.
- Explicit connect/read/request deadline and total operation deadline. Proposed starting caps to review: at most 30 (absolute max 50) item GETs for one manual refresh, response body size per request, JSON depth, titles/metadata and fetched bytes bounded; no automatically following comments/kids/pagination to thousands of requests.
- Bounded response ingestion/decompression, validate `Content-Type` where meaningful, reject malformed payloads, treat HTML/text fields as untrusted. Never render HTML unsanitized or execute embedded script. User-facing titles and source links are data only.
- Avoid dumping provider response bodies, credentials, tokens, raw URL query details, fetched metadata or sensitive payloads into application errors/logs; log safe source IDs and categorical status codes. Existing request-level bearer authorization stays mandatory.
- At the test boundary use a deterministic fake adapter or in-memory transport; do **not** weaken production outbound URL checks merely to allow localhost. No live provider requests are necessary for unit/integration acceptance. Optional one-off public connectivity smoke must have a separate bounded authorization if needed.

## Source freshness and ranking honesty

- HN `topstories` is a list curated/ranked by its own provider at **fetch time**; the numerical list position is not a PPV-measured daily velocity, world-wide news importance or guaranteed post created today. A `score` field is an observed HN score only; never call it a cross-network normalized score.
- The S1 Radar **last-24h** view uses `publishedAt` for inclusion. A top story older than 24 hours can be missing from that view even though HN still ranks it. This is correct by the stated 24h contract; document this tradeoff in API examples.
- RSS (S2) exposes recent published links only. If a feed lacks timestamps, its items cannot truthfully be called "published today"; use a separately labeled "recently fetched" view only if actually implemented and tested.
- A failed refresh must preserve previous items and timestamps. The read route may show historical cached data with `lastFetchedAt` and a stale indicator; it must not report it as freshly refreshed.
- No retaining/republishing full article bodies, comments or media. Metadata (title, author, link, publication time, source-specific public rank/score with capture time) is a minimal design ceiling, subject to publisher/API terms.

## Future S2 SSRF/XML/scheduler controls

- Only Owner-approved RSS/Atom feeds. Parse XML with DTD, external entities, XInclude and external schema resolution disabled. Never fetch enclosures/media or URLs embedded inside feed content. Impose limits on decompressed bytes, nesting, item count, XML text size and parse time.
- Validate the **requested** feed URL and every redirect/resolved destination with HTTPS and public-IP policy. Explicitly consider DNS rebinding, IP literals, IPv6 representations, credentials-in-authority, malicious redirects, and cloud metadata endpoints. A raw `feed_url` string from the database is not implicitly trusted.
- Scheduler should be separately opted in; sources currently store scheduling fields but **no live scheduler** is implemented. Avoid long DB transactions while calling the network, parallel overlapping refresh of the same source, auto-retry storms or unattended crawling of disallowed platforms.

## Provider errors / operational semantics

Define explicit classes: `UNSUPPORTED_SOURCE_TYPE`, `SOURCE_DISABLED`, `REFRESH_ALREADY_RUNNING`, `UPSTREAM_UNAVAILABLE`, `UPSTREAM_RATE_LIMITED`, `UPSTREAM_INVALID_RESPONSE`, `FETCH_LIMIT_EXCEEDED`; exact public HTTP codes must follow existing owner advice/root `ApiResponse` conventions. Do not claim these identifiers are existing codes. HTTP response must not include provider secrets, stack traces or raw third-party HTML. Distinguish upstream partial failure from a valid empty list, and reject updates to fetch timestamps on failed ingest.

**Next step:** Owner | apply recorded `ARTICLE_URL` decision and corrected proposal; Codex independently re-reviews PH-01/PH-02 and egress limits without provider I/O | DTR26-S1 documentation only, `NO_ACTIVE_HANDOFF` | future implementation `0/3`, takeover unauthorized.

## PH-01/02 bounded source and authority amendments (2026-11-02)

- **Identity (Owner-approved `ARTICLE_URL`, 2026-11-02):** S1 is **article-centric**. For one exact trimmed external article URL, keep one FeedItem with `externalId=null`; group duplicates from different HN submissions before ingest and select greatest valid submission time, then greatest HN ID. Save to Inbox saves the **article URL**. HN discussion URL/ID/rank/score/capture timestamp are provenance only. Skip story records without valid HTTP(S) article URLs instead of mixing discussion identity into the same contract. Titles remain untrusted HTML-shaped text from provider; normalize safely and always treat as data.
- **Refresh authority:** a type/enabled preflight before HTTP is not a commit-time permission check. A Feed-owned transactional finalizer must cause generic ingestion and an **authoritative locked** source-type/enabled validation to happen in the **same short DB transaction**, before its commit. It joins the existing `ingestFetch` REQUIRED transaction, retains its item-then-source lock order, and rejects/rolls back on any HN→RSS or disable race. No database lock over outbound HTTP, no change to generic ingress semantics, and no new connection to an arbitrary source URL. Inspect Spring proxy boundaries and the exact transaction/lock scope in implementation.
- **Provider response handling:** topstories may contain non-story/dead/deleted/null IDs. Valid skipped candidates do not create fake success counts. A failed selected-item fetch, malformed required provider JSON or invalid root list is **not** a successful empty snapshot. The HN Unix `time` describes the submission, not the linked article's publication. Use a fixed rolling 24h UTC submission-time cutoff, exclude null/future times, restrict GET to enabled HN sources, and distinguish retained cached rows from present-day HN top-list membership.
- **Resource bounds to fix in executable handoff:** choose explicit total request cap (1 top-list + at most 50 item GETs; default 30 selected IDs), max compressed/decompressed bytes per response and in aggregate, parser depth, string/metadata lengths, timeout/deadline and response-retention limits. Test the **actual** connector peer resolution, pinned DNS/connection, redirect/unsafe IP denial and proxy policy, not just a separate DNS precheck. The existing client provides feasible hooks, but effectiveness is still NOT_VERIFIED until runtime tests.
- **Errors/counters:** `SOURCE_CHANGED_DURING_REFRESH`, `SOURCE_DISABLED`, `UNSUPPORTED_SOURCE_TYPE`, `REFRESH_ALREADY_RUNNING`, `UPSTREAM_UNAVAILABLE` etc. are *proposed owner-local categorical mappings*, not already deployed codes. Do not claim inserted/updated counts from a generic upsert that returns only item views. Prefer `processedItems` meaning returned item count plus separately derived selection/deduplication counts; no fabricated new-row count.
