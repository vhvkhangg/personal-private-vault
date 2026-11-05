# Executable slice handoff — DTR26-S2A: manual RSS/Atom refresh and combined Radar

- **Handoff ID:** `DTR26-S2A`.
- **Created:** 2026-11-04 by Codex.
- **Status:** `READY_FOR_OWNER_COMMIT` (independent Codex takeover review 1, 2026-11-05). All DTR26-S2A-FR-01–06 CLOSED; Owner publication pending.
- **Original handoff authority:** the Owner's `$codex-create-handoff` invocation after publication of the [reviewed preparation](README.md) and its [independent READY_FOR_HANDOFF review](reviews/2026-11-04-dtr26-s2a-independent-pre-handoff-review.md). The review's seven explicit proposal resolutions govern this slice; older preparation-only wording records the preceding gate. Current bounded takeover authority is recorded below and in its handback.
- **Entry commit:** `43ca772054cfc9c2546cae87b949bcc065a1066a`, `docs(feed): prepare DTR26-S2A RSS refresh`. Local HEAD and remote `refs/heads/main` matched at creation (`git ls-remote origin refs/heads/main`); entry worktree/diff were clean. Its only changes from preparation's source baseline `8502a30b38c48fb812397b3a5de216be804fd958` are seven preparation/review Markdown files.
- **Implementer:** Codex for the Owner's explicit 2026-11-05 **ONE bounded takeover**, limited to `DTR26-S2A-FR-01`; [takeover handback and authority ledger](codex-takeover-handback-2026-11-05.md). Previous Antigravity submissions remain historical.
- **Reviewer:** a separate Codex `$codex-final-review` session; implementer self-review never substitutes for independent review.
- **Remediation:** S2A `3/3 consumed; 0/3 remaining; Antigravity EXHAUSTED / STOPPED`; ONE Owner-authorized Codex takeover **1/1 independently accepted, STOP**. No fourth Antigravity round or automatic additional takeover.

## Goal and sources of truth

An authenticated Owner can manually refresh a configured RSS source containing RSS 2.0 or Atom 1.0, read a bounded cached HN+RSS Radar, and save an article through the existing Inbox operation. Fetch one feed document only. Preserve accepted HN-only routes, metadata-only source CRUD/scheduling semantics, generic ingestion and saved snapshots. S2B scheduling remains a separate future gate.

Read the [Owner scope](owner-scope-and-decisions.md), [source/API design](source-reuse-and-api-design.md), [fetch/parser policy](rss-atom-fetch-security.md), [acceptance matrix](acceptance-and-review-plan.md), and especially the [preparation review resolutions and evidence limits](reviews/2026-11-04-dtr26-s2a-independent-pre-handoff-review.md). Where the draft says “200 elements,” the reviewed unit is **200 item/entry records**, with separate event/text limits below. A parsed future publication date excludes the entry; it is not converted to an undated item.

Applicable references: [Owner policy](../../../workflow/current-owner-delivery-policy.md), [Docs Sync](../../../workflow/documentation-synchronization.md), [ACTIVE](../../handoffs/ACTIVE.md), [root](../../../../AGENTS.md), [backend](../../../../backend/AGENTS.md), [Feed](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/AGENTS.md), [test](../../../../backend/src/test/AGENTS.md) and [docs](../../../AGENTS.md) instructions; [Phase 10](../../phase-10/README.md), [Phase 13](../../phase-13/README.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [module matrix](../../../architecture/module-dependency-matrix.md), [package tree](../../../repository/repository-package-tree.md), [frozen DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml), and [published S1 closeout](../2026-11-02-daily-trends-radar-preparation/owner-publication-closeout-2026-11-04.md). Historical Phase-10 provider deferrals do not veto this specific approved extension; unrelated frozen behavior stays unchanged.

## Simplicity decision

| Disposition | Selected implementation and burden |
| --- | --- |
| REUSE | Existing RSS enum/source CRUD, FeedItemOperations.ingestFetch, URL hash/upsert, FeedJsonSnapshot, SavedResource/Inbox, root envelopes and local advice. Low implementation burden plus focused compatibility proof. |
| KEEP | One Feed-local RSS adapter, secure JDK streaming parser/normalizer, manual orchestration and short finalizer. Medium implementation; substantial finite transport/XML and transaction negatives are required for arbitrary configured hosts. |
| REUSE / SIMPLIFY | The same singleton admission used by HN, maximum two refreshes across HN+RSS. Reuse its existing API; an internal rename is optional only with compatible HN wiring/tests. No second RSS capacity budget. |
| KEEP | Separate bounded combined query and new wire DTOs, retaining HN-only DTO/schema descriptions. Medium read/web verification; no external popularity synthesis. |
| DEFER | [S2B](scheduled-refresh-design.md), relative IRI/xml:base resolution, extra feed dialects, authenticated feeds, durable retries and distributed scheduling. No scheduler scaffolding in S2A. |
| REJECT | Generic provider/job/plugin framework, new module/agent/schema/dependency, duplicate Inbox and speculative benchmarking. Existing Apache HTTP/JDK XML and bounded runner suffice under the reviewed local baseline. |

## User-visible acceptance — MUST

Each row maps to the preparation matrix ID. Definitions and negative evidence follow; these are twelve acceptance cases, not twelve implementation projects.

| ID | Scenario/input | Expected observable result | Minimal proof / reuse |
| --- | --- | --- | --- |
| S2A-01 | Configure RSS, then POST refresh; missing/disabled/wrong-type source, invalid ID, absent bearer | Existing CRUD/defaults/positive schedule intervals unchanged. Only enabled RSS sources refresh; exact errors below, no provider call on preflight/auth rejection. | Real Spring web + source integration; FeedSourceOperations, FeedExceptionAdvice. |
| S2A-02 | Configured HTTPS public host versus literal/local/mixed DNS, rebind, redirect, proxy or TLS mismatch | Only configured feedUrl fetched; actual connector uses validated public addresses and original TLS/Host identity. No unsafe fallback/second uncontrolled DNS or article fetch. | Production manager/socket/resolver/route tests with local fakes; not regex-only proof. |
| S2A-03 | At/over a cap, slow trickle, blocked DNS, cancellation or invalid encoding | One GET with all nested deadlines/limits below; failure releases ownership/admission and changes no rows/timestamps. No unbounded tasks/allocations. | Measured transport/parser limits and lifecycle tests; production constants/wiring assertions. |
| S2A-04 | RSS2/Atom1, valid empty feed, DTD/XXE/XInclude, spoofed namespace, malformed tail | Supported complete documents normalize; valid empty commits zero items. Unsafe/malformed XML fails atomically, zero entity/file/auxiliary network access. | Local hostile XML fixtures and effective parser-property assertions; JDK StAX. |
| S2A-05 | Same link/different GUIDs, repeated Atom IDs, ties, legacy externalId, changed link | One representative per exact trimmed article URL per source, externalId=null; deterministic rule below. Same URL updates, changed URL adds; generic uniqueness unchanged. | Real PostgreSQL state/upsert and representative/provenance fixtures. |
| S2A-06 | Valid/missing/invalid/future dates, Atom updated-only, untrusted titles, UTC boundaries | Only pubDate/published supplies publication time. Missing/invalid stays null and readable per source; future entry skipped. Inclusive combined [now−24h, now]; strings serialized safely. | Fixed-clock parser + PostgreSQL boundary fixtures; no guessed dates. |
| S2A-07 | Disable, RSS→HN, change feedUrl mid-HTTP; edit after staging; finalizer timeout | Edits proceed during HTTP. Same physical transaction contains ingest/timestamps/final authority; source change rolls staged changes back. Source lock lasts through commit. | PostgreSQL barriers, PID+XID at actual ingest/source update/beforeCommit; same identity assertion rejects test-only REQUIRES_NEW control. |
| S2A-08 | HN/RSS concurrency, same source after type edit, third refresh, interruption/failure/shutdown | Shared max-two budget, same-source single-flight, finite DNS/abort work and cleanup. No DB transaction/lock over DNS/HTTP/XML or connection/ingest after deadline. | Deterministic admission/concurrency/lifecycle tests and architecture checks. |
| S2A-09 | Combined GET with HN/RSS/other/disabled/null/legacy rows; default/max/invalid limit | Eligible HN+RSS cache only, deterministic SQL bound/order, provider-specific provenance, zero provider calls. HN-only GET unchanged. | Real DB+web, SQL/row-cap observation, exact OpenAPI/security/schema/error assertions. |
| S2A-10 | HN refresh/Radar, feed reads, RSS Save-to-Inbox, cross-source duplicate, conversion/search | Accepted HN behavior and exact links compatible. Existing save captures immutable metadata/timestamps; later refresh cannot mutate saved snapshot. Global URL conflict/paging/Knowledge conversion/search preserved. | Focused Feed/Inbox/HN/search regressions; SavedResourceService, public Knowledge/Vault APIs. |
| S2A-11 | Changed tree, ownership, hostile markers/errors/logs, startup/source edit/GET | Only allowed targets; package-info/docs match source. No schema/dependency/root HTTP/auth change, payload/secret logging, auxiliary fetch or automatic polling. | Diff/tree and architecture/HTTP checks, privacy markers and call counts. |
| S2A-12 | Implementation/evidence handback | Mandatory proof and focused gate pass; Antigravity self-reviews, then separate Codex reviews. Only independent acceptance permits Owner publication. | Dated handback/self-review, per-case receipts/limits, independent review and exact counter. |

## Exact HTTP and cached read contract

### RSS manual refresh

Add `POST /api/v1/feed/sources/{sourceId}/refresh-rss`, operationId `refreshRssFeedSource`. Positive sourceId, no request body, request-time URL/host override or selection parameter. Use stored exact feedUrl; sourceUrl is metadata only. Fixed maximum selection 50. Expose a new result view/DTO (`RssFeedRefreshResultView` / `RssFeedRefreshResponse`) through the existing Feed source facade; do not repurpose HN counters.

Return HTTP 200 in the existing ApiResponse family with exactly these payload fields:

| Field | Meaning |
| --- | --- |
| sourceId | Refreshed source ID. |
| fetchedAt | UTC completion instant after entire fetch/parse/normalization succeeds; persisted with committed batch. |
| parsedEntries | All item/entry records scanned in accepted complete document, including ineligible records. |
| eligibleEntries | Individually eligible records before URL grouping. |
| distinctArticleUrls | All eligible URL groups before the 50-group selection cap. |
| processedItems | Representatives returned by committed generic ingestion, not newly inserted rows. |

Example: three eligible records with links A/A/B yield parsedEntries=3, eligibleEntries=3, distinctArticleUrls=2, processedItems=2. Repeating the document updates those two rows and still reports processedItems=2. Valid empty reports four zero counters and advances successful timestamps. Malformed XML never becomes successful-empty. With 60 distinct eligible links, report distinctArticleUrls=60 and processedItems=50 after complete validation.

### Combined cached Radar

Add `GET /api/v1/feed/radar/combined?limit=50`, operationId `getCombinedFeedRadar`. Default 50, range 1–100; no sourceId filter in S2A. Capture one UTC now: windowEnd=retrievedAt=now; windowStart=now−24h. Query enabled sources of current type HACKER_NEWS or RSS, non-null publishedAt within both inclusive bounds, ordered publishedAt DESC then item ID DESC. Apply the SQL row limit using the existing projection/Pageable pattern, then reject invalid stored article links before serialization. A post-filter short page is honest; no totals or unbounded fill query. GET performs zero provider requests.

Use new `CombinedFeedRadarResponse` / `CombinedFeedRadarItemResponse` DTOs: page fields items/windowStart/windowEnd/retrievedAt; item fields feedItemId, sourceId, sourceName, sourceType, title, url, publishedAt, fetchedAt, signalType, sourceRank, signalCapturedAt, hnStoryId, hnDiscussionUrl. Existing immutable read views may be reused. New date/window descriptions cover RSS publication and HN submission; do not change old HN schemas/descriptions to achieve reuse.

- Current source type **RSS**: signalType=RSS_SOURCE_CACHE; sourceRank/hnStoryId/hnDiscussionUrl=null, even with plausible legacy hn-prefixed metadata. signalCapturedAt is persisted item fetchedAt, not arbitrary metadata or a ranking claim.
- Current source type **HN**: reuse validated provenance mapping (exact positive story ID/rank, matching discussion URL, valid capture timestamp); otherwise retain SOURCE_CACHE and current null fallback fields.
- Existing POST `/sources/{sourceId}/refresh` and GET `/radar`, parameters, HN-only filtering, response fields, codes and semantics stay unchanged. Neither dispatches to RSS. Combined reads do not merge separate sources' FeedItems.

### Stable errors and privacy

Reuse FeedRefreshException/FeedRefreshFailureReason and actual FeedExceptionAdvice. Check enabled before type. Match typed causes/reasons, never message text. Use existing bearer security/root ApiResponse/error schemas; errors have null data and safe fixed messages.

| Condition | HTTP / code |
| --- | --- |
| Invalid numeric type/overflow; positive/range violation | 400 / MALFORMED_REQUEST; VALIDATION_ERROR, respectively. |
| Missing/invalid bearer | 401 / AUTHENTICATION_REQUIRED; no provider call. |
| Source absent at preflight | 404 / FEED_SOURCE_NOT_FOUND. |
| Disabled; enabled non-RSS source | 422 / SOURCE_DISABLED; UNSUPPORTED_SOURCE_TYPE. |
| Same-source running; final source authority changed/deleted | 409 / REFRESH_ALREADY_RUNNING; SOURCE_CHANGED_DURING_REFRESH. |
| Generic ingest uniqueness conflict | 409 / FEED_ITEM_CONFLICT; atomic rollback. |
| Unsafe URI/resolved destination | 502 / UPSTREAM_DESTINATION_REJECTED; no unsafe connection. |
| Non-200 except 429/5xx, redirect, bad media/encoding/XML/root, DTD/external-reference construct | 502 / UPSTREAM_INVALID_RESPONSE. |
| Body/record/event/text/depth cap exceeded | 502 / FETCH_LIMIT_EXCEEDED. |
| Network/DNS/TLS/time/interruption or unsupported configured proxy | 503 / UPSTREAM_UNAVAILABLE. |
| Upstream 429; shared capacity full | 503 / UPSTREAM_RATE_LIMITED; REFRESH_CAPACITY_REACHED. |
| Upstream 5xx; finalizer lock/transaction failure | 503 / UPSTREAM_UNAVAILABLE; REFRESH_FINALIZATION_UNAVAILABLE. |
| Unexpected internal failure | Existing safe 500; rollback without raw detail. |

Invalid/overlong entry title/link is skipped locally; optional-field policy below prevents invalid metadata reaching ingest. Log safe source IDs, fixed categories and numeric counters only. Never log/echo full configured/item URLs, queries, XML, body/headers, authors, tokens, private addresses, stack/cause payloads or content fields. Never forward PPV bearer/cookies, enable feed credentials or automatically retry. Preserve the old S1 documentation error mismatch as PH-L01; actual mappings here govern S2A.

OpenAPI must show exact defaults/bounds, no RSS body, bearer security, 200 envelopes and nullable fields. RSS errors: 400/401/404/409/422/502/503; combined: 400/401 plus existing safe 500 behavior. Use local annotations/existing ErrorResponse. Extend the exact generated manifest from 233 to **235** with the two operation IDs/DTOs; all previous rows unchanged. A changed count alone is insufficient. No root OpenApiConfiguration/security edits.

## Fetch, parser, normalization and resource contract

### Connection-bound destination policy

1. Only configured feedUrl is an outbound target: strict absolute HTTPS URI with hostname, no credentials/user-info, fragment, IP literal, unusual authority, controls or port other than omitted/443. Deny HTTP/file/jar/ftp/gopher and opaque/ambiguous destinations at fetch time; metadata-only CRUD validation stays unchanged.
2. Bound A/AAAA resolution; reject the **entire** answer if any address is non-public/local/private/link-local/multicast/unspecified/reserved, including mapped IPv4-in-IPv6/non-public IPv6. Only isSiteLocalAddress or first-answer checking is insufficient. Bind the actual manager/operator to checked InetAddress values; all fallback addresses checked, no second unrestricted lookup.
3. Preserve original Host, TLS SNI and default certificate hostname verification. Direct-route without environment/OS/JVM/explicit proxies; detected unsupported configured proxy fails closed. Disable redirects, automatic retries, cookies, authentication and decompression. Request identity, reject non-identity Content-Encoding and invalid XML encoding. Accept successful XML feed responses (RSS/Atom/XML media types), not HTML/error bodies.
4. One feed-document GET only. Never fetch sourceUrl, feed self/alternate links, entry links, enclosures, content src, XML base/schema/XSLT/XInclude, media or favicons. Article links are stored data and require no DNS.
5. Use reviewed installed Apache 4.5.13, no new POM/framework. Its [manager API](https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/conn/PoolingHttpClientConnectionManager.html) documents resolver-aware construction; the public current page labels 4.5.14, so reviewed installed 4.5.13 classes remain local API authority. A custom manager can bypass builder resolver/TLS settings: prove actual connector wiring. HN's fixed-host policy is not an RSS policy.

Actual-manager negatives use injected fake DNS/socket/route/TLS collaborators through production wiring, capturing peers, hostname/Host and fallback. A mocked high-level client or isolated policy test cannot establish connection enforcement. Hostile destinations stay simulated; no real public feed or unsafe local/metadata endpoint is contacted.

### Fixed budgets and ownership

| Bound | Exact contract |
| --- | --- |
| Requests/body | One GET; ≤1,048,576 response entity bytes after transfer framing, identity encoding. Detect advertised oversize and streaming one-byte overrun, including unknown/chunked length. |
| Records | ≤200 RSS item / Atom entry records, eligible or not; not child XML elements. |
| Events | ≤20,000 emitted XML events across complete document, including start/end document, comments and ignored sections. |
| Text/depth | ≤1,048,576 accumulated UTF-16 text code units across character/CDATA/space content, including ignored sections; root depth=1, max 32. Count before retaining/concatenating. Attribute/other token allocations remain bounded by raw bytes. |
| Selection | ≤50 distinct eligible URL groups; still validate to END_DOCUMENT. |
| Entry values | Trimmed title 1–1000 Java characters; URL ≤2048. Optional-field/metadata limits below. |
| Refresh/finalizer | Monotonic overall 25s; reserve 5s for finalizer, so fetch/parse/normalization completes by 20s with ≥5s remaining. Finalizer transaction timeout 5s. No work starts after its remaining budget expires. |
| Exchange/DNS | Exchange ≤10s including body reads, clamped to remaining fetch budget; DNS caller wait ≤min(2s, remaining budget). Nested, not additive; abort slow trickle too. |
| DNS work | ≤2 DNS workers/4 queued tasks; fast saturation failure, owned cancellation/shutdown, no replacement pool per request/unbounded executor/retries. Shared HN+RSS admission remains two total. |

Test exact-cap/one-over bytes, records, events, text, depth and fields. Isolate parser budgets with local fixtures when enclosing raw bytes would fire first. Short injected test deadlines are acceptable alongside assertions of production constants/wiring. Retain elapsed/operation/worker observations; no benchmark/SLO claim.

Native lookup is not universally interruptible. On timeout cancel/remove owned pending work, prevent late connection/ingest, keep finite workers/queue and close owned resources on shutdown. No unbounded abandoned application work; no guarantee of OS-level native DNS termination. Preserve S1's host limitation. Abort scheduling/connection resources must be finite and owned; S2B scheduler is unrelated/excluded.

### XML and article normalization

Use namespace-aware JDK StAX: DTD/external entities disabled, external DTD access denied, rejecting XMLResolver, explicit DOCTYPE/DTD-event rejection. Verify effective critical settings against [Java 25 XMLInputFactory](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/javax/xml/stream/XMLInputFactory.html); unsupported critical settings fail closed. No XInclude/validation/schema imports/XSLT/external content resolution; reject XInclude constructs. No unbounded DOM/getElementText/CDATA accumulation. Predefined/numeric references may become bounded text; custom declarations never become accepted input.

- Accept actual unnamespaced `rss version="2.0"` with one channel/items, or Atom feed/entry in exactly `http://www.w3.org/2005/Atom`. Refuse RDF/RSS1/OPML/HTML, spoofed namespaces, malformed prolog/tail or global overrun atomically. Ignore unsupported extension/content payloads but count their structural/text cost; never dereference them. Reach document end after 50 selected groups too.
- RSS: nonblank bounded title/absolute HTTP(S) link; guid optional provenance, never URL fallback/externalId. Atom: title plus valid absolute HTTP(S) alternate link (absent rel means alternate); first eligible alternate link in document order. Relative links/xml:base resolution unsupported. Title/author text is untrusted string data, never rendered/interpreted as HTML. No description/content/enclosure article text; normalized summary=null.
- Reuse accepted article-link predicate: HTTP/HTTPS, URI host, no user-info/controls, ≤2048. A narrow Feed-local extraction is allowed for real reuse with unchanged HN tests. No outbound public-DNS validation on stored links. Trim only; preserve case/path/query/fragment, no tracking stripping/canonicalization/equivalence.
- RSS pubDate must support specified two-/four-digit years and timezone forms, with fixtures; Atom published uses its specified timestamp. See [RSS dates](https://www.rssboard.org/rss-specification) and [Atom published](https://www.rfc-editor.org/rfc/rfc4287#section-4.2.9). Missing/invalid→null; never fetch-time/updated fallback. A parsed instant after the batch's one captured UTC now excludes the record. Same clock reference across eligibility; fetchedAt captured later after complete successful processing.
- Group by exact trimmed link, preserving groups' first-occurrence document order. Representative: latest valid publication first, null after valid; ascending lexical trimmed original GUID/Atom ID, missing/blank after present; earliest document order last. Compare IDs before output truncation. Retain one winner's title/author/date/provenance, select first 50 groups and pass only representatives with externalId=null to generic ingest.
- Optional policy: blank author/ID→null; omit author >500 characters. Keep at most first 500 UTF-16 units of provenance ID without splitting a surrogate pair. Invalid Atom updated omitted; valid value only feedEntryUpdatedAt provenance. Metadata allowlist: feedFormat=RSS_2_0 or ATOM_1_0, optional feedEntryId/feedEntryUpdatedAt, rssCapturedAt=fetchedAt. Serialized JSON ≤2048 UTF-8 bytes; if needed omit feedEntryId first, then feedEntryUpdatedAt. Required format/capture remain. No arbitrary raw entry/body/HTML; existing FeedJsonSnapshot copy/immutability.
- Across refreshes same URL updates source hash row, including legacy non-null externalId (becomes null); same provenance ID/new link adds a row, old article retained. Include legacy external-ID/URL collision fixtures without changing generic dual-key policy. Separate sources retain separate FeedItems; SavedResource global URL hash governs duplicate saves.

## Atomic finalization and concurrency

1. Preflight existence/enabled/type=RSS, capture **exact configured feedUrl**, no row lock over network. Existing NEVER orchestration shape and shared admission; URI/DNS/HTTP/XML outside DB transaction. Manual RSS works with scheduledRefreshEnabled=false.
2. After complete fetch/parse/normalization capture fetchedAt, check remaining budget, call Spring-proxied RSS REQUIRED finalizer with 5s timeout. Reuse ingestFetch without changing validation/dedup/uniqueness/timestamp/propagation rules.
3. Preserve item-then-source lock ordering. After ingest, findByIdForUpdate plus entityManager.refresh rechecks authoritative enabled/type/feedUrl in the **same physical transaction**: enabled=true, type=RSS, exact fetched-URL equality. Deletion/mismatch→SOURCE_CHANGED_DURING_REFRESH and rollback staged inserts/updates/timestamps. Pre-ingest check or independently committed REQUIRES_NEW ingest is insufficient.
4. Hold source lock through commit. Generic ingest uses fresh schedule/interval to compute nextFetchAt on success; every existing positive interval (including 1 minute) remains accepted. Failures leave lastFetchedAt/nextFetchAt unchanged; valid empty complete feeds may advance them. No scheduled-invocation flags/authority for S2B.
5. Translate lock/transaction failure to REFRESH_FINALIZATION_UNAVAILABLE. Finally release admission and close/cancel owned work on every outcome. Source-type flip cannot obtain a separate provider budget for the same ID.

Reuse [S1 transaction observation/negative control](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/HackerNewsRefreshIntegrationTest.java) and [lock/rollback barriers](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/HackerNewsRefreshConcurrencyIntegrationTest.java). Observe `pg_backend_pid()`+`pg_current_xact_id()` **after Spring advice** at actual ingest, source timestamp update and finalizer beforeCommit; equality/no early commit. Test-only REQUIRES_NEW must establish independent commit and fail the **same shared-identity assertion**; active-status/annotation checking is insufficient. No production test callback. Include URL/type/enabled edits mid-HTTP, staged insert+update rollback, retained NOWAIT lock, actual PostgreSQL lock timeout and HN/RSS cross-capacity barriers.

## Precise change map and ownership

Paths relative to repository root. Production prefix below is backend/src/main/java/com/vhvkhangg/personalprivatevault/. Outside targets require concrete approved-scope necessity or Owner decision; no broad refactoring.

| Allowed target | Bounded change |
| --- | --- |
| feed/internal/infrastructure/rss/ under production prefix | New RssFeedDestinationPolicy, RssFeedClient, RssAtomParser, RssFeedNormalizer (or cohesive equivalent), package-info.java. Distinguish transport/XML/article roles where needed; no generic provider stack. |
| feed/internal/application/ | RssFeedRefreshService/RssFeedRefreshFinalizer; narrow FeedSourceService delegation and FeedItemService combined read/source-aware mapping. Reuse HackerNewsRefreshAdmission singleton; optional internal rename/references/tests only. HN service/finalizer/client behavior unchanged. |
| feed/source/FeedSourceOperations.java; feed/item/FeedItemOperations.java; feed/view/ | Add refreshRssSource(sourceId), getCombinedRadar(limit), immutable RSS result; reuse compatible read views. Existing signatures/named interfaces unchanged, no new module edge. |
| feed/internal/infrastructure/persistence/FeedItemRepository.java and FeedRadarProjection.java | Separate combined query/projection; HN query unchanged. Reuse FeedSourceRepository locking. No new entity/index/migration/native cross-owner SQL/DBML. |
| feed/internal/web/controller/FeedController.java; feed/internal/web/dto/; feed/internal/web/mapper/FeedWebMapper.java | Two routes, RSS/combined DTOs, mapping/validation/OpenAPI. Existing exceptions/advice/root envelopes reused; no duplicate catalog/root API type. |
| Feed-local article-link helper, if needed | Narrow extraction of existing shared predicate with unchanged HN behavior/tests; no broad utilities or weakened fetch policy. |
| backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/ and internal subpackages | New RSS/parser/transport/PostgreSQL/concurrency/combined/web tests; focused existing HN/Feed/Inbox compatibility adjustments. Local XML fixtures under backend/src/test/resources/feed/ allowed; no production bypass/live provider test. |
| backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java | Exact two operations/235 inventory and DTO/security/error assertions. Existing architecture/search/envelope tests are verification targets, not permission to weaken checks. |
| Scoped documents below | Delivered API/inventory/handback only; no unrelated governance repair. |

No ImportData/RAG/Knowledge/Vault production change needed. New packages need package-info.java; recursively inspect changed folders, visibility/named interfaces and test placement. ADR-0016 root HTTP exception and public Vault/parent Knowledge boundaries remain unchanged.

## Negative/failure tests and finite verification

Stable new selectors: RssFeedDestinationPolicyTest, RssFeedClientTest, RssAtomParserTest, RssFeedNormalizerTest, RssFeedRefreshIntegrationTest, RssFeedRefreshConcurrencyIntegrationTest, CombinedFeedRadarIntegrationTest, RssDailyTrendsWebIntegrationTest. Use existing PostgreSQL/Testcontainers/Spring/MockMvc and test-local resolver/socket/clock seams, never H2. Prove production manager/parser wiring and actual DB state; preserve S1 controls.

Required negatives: private/mixed/mapped/reserved DNS, rebinding/fallback, proxy/TLS mismatch, redirects/retries/decompression, zero article/enclosure/content/base calls; DNS saturation/cancellation/shutdown; slow trickle/body overrun/encoding; DOCTYPE/XXE/entity bomb/XInclude/spoofed namespace/deep XML/ignored CDATA; malformed tail after 50 valid groups; optional JSON escaping/Unicode byte cap; representative/date/legacy cases; zero network on GET/CRUD/due read/startup; hostile URL/XML/token markers absent from logs/errors. Synthetic local fixtures only.

Run focused checks while implementing, fix failures, retain final source-applicable receipts. This **seven-selection focused gate** is the final command sequence from repository root, PowerShell; not the full application suite:

```powershell
$s2aPython = (Get-Command python -ErrorAction Stop).Source
$s2aRunDir = Join-Path 'logs/daily-trends-radar' ('dtr26-s2a-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $s2aRunDir -ErrorAction Stop | Out-Null
$s2aChecks = @(
    @{ Name = 'rss-provider-unit'; Tests = 'RssFeedDestinationPolicyTest,RssFeedClientTest,RssAtomParserTest,RssFeedNormalizerTest' }
    @{ Name = 'rss-refresh-postgres'; Tests = 'RssFeedRefreshIntegrationTest,RssFeedRefreshConcurrencyIntegrationTest' }
    @{ Name = 'combined-radar-postgres'; Tests = 'CombinedFeedRadarIntegrationTest,FeedRadarIntegrationTest' }
    @{ Name = 'hn-compatibility'; Tests = 'HackerNewsClientTest,HackerNewsDestinationPolicyTest,HackerNewsNormalizerTest,HackerNewsRefreshAdmissionTest,HackerNewsRefreshIntegrationTest,HackerNewsRefreshConcurrencyIntegrationTest' }
    @{ Name = 'feed-inbox-compatibility'; Tests = 'FeedIntegrationTest,FeedValidationTest,FeedIngestionConcurrencyTest,SavedResourceInboxIntegrationTest,SavedResourceInboxCursorTest' }
    @{ Name = 'web-openapi'; Tests = 'RssDailyTrendsWebIntegrationTest,DailyTrendsWebIntegrationTest,FeedWebIntegrationTest,SavedResourceInboxWebIntegrationTest,OpenApiRouteInventoryIntegrationTest,ApiResponseEnvelopeAndErrorIntegrationTest' }
    @{ Name = 'architecture-search'; Tests = 'ApplicationArchitectureTests,GlobalSearchIntegrationTest,SearchWebIntegrationTest' }
)
foreach ($s2aCheck in $s2aChecks) {
    $s2aReceipt = Join-Path $s2aRunDir ($s2aCheck.Name + '.json')
    $s2aTestArgument = '-Dtest=' + $s2aCheck.Tests
    & $s2aPython scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt $s2aReceipt -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' $s2aTestArgument test
    if ($LASTEXITCODE -ne 0) { throw ('DTR26-S2A verification failed: ' + $s2aCheck.Name) }
}
```

Split groups into exact classes if necessary for 300s, same selection/evidence, unique receipts. If admission renamed, update its test selector and document replacement; never silently omit a class/allow no-tests success. No count quota. Reuse independent evidence only with exact unchanged-source applicability; never replace new RSS/transaction/read proof. Separate implementer, fresh independent and reused results.

Use [bounded-command-execution](../../../../.agents/skills/bounded-command-execution/SKILL.md)/scripts/run-bounded-command.py: normal ≤**300s**, fresh ignored logs/receipts, bounded diagnostics/owned-tree cleanup. B1/B2 accepted/Owner-published; separate full-suite ceiling 2,100s is **not authorized here**. No full suite/clean verify/extended profile, real feed smoke, RAG/QA/archived-S19-2 override, inference/model/worker startup, runtime/dependency/image download, hook/trust/startup repair or bypass. Missing prerequisites require factual stop. Never delete/overwrite/relocate/truncate historic logs. Native-hook root/trust/client and native-DNS limits remain explicit.

## Documentation impact and ownership

| Current/derived path | Authorized editor, timing and sync evidence |
| --- | --- |
| This contract; docs/implementation/handoffs/ACTIVE.md | Codex creates authority. Antigravity fills results/counter, links dated handback/self-review, marks IMPLEMENTED_AWAITING_CODEX_REVIEW. Independent Codex records review/remediation/disposition; never Owner publication. |
| docs/architecture/daily-trends-radar-rss-api-extension.md — new | Antigravity: delivered routes/fields/counts/errors, formats/caps/relative-link limits, ARTICLE_URL, cache provenance, rollback/manual-only scope. Compare source/generated OpenAPI/receipts at self/independent review. |
| docs/architecture/api-architecture.md | Antigravity: one navigation link to RSS extension; preserve current contract/history sections. |
| docs/repository/repository-package-tree.md | Antigravity: additive actual Feed RSS package/capability inventory only. No relocation/module edge/version/baseline-policy rewrite/diagram regeneration. Verify actual tree/package-info. |
| This directory's implementation-handback-2026-11-04.md (uniquely dated if later) | Antigravity: S2A-01–12/commands/receipts, self-review, limits, paths, Docs Sync/counter; later correction evidence additive/dated, not replaced history. |
| Preparation README/acceptance-plan; root/backend/docs/Feed instructions; Owner policy/handoff README; future closeout | ChatGPT: routine governance/status sync when separately requested. Preparation records submission baseline; ACTIVE/this contract controls execution. Owner publishes acceptance, then ChatGPT records real SHA/brief closeout and authorized ACTIVE reset. No automatic S2B. |

**Docs Sync Check — creation:** UPDATED — this contract/ACTIVE only. ALREADY_CONSISTENT — published READY_FOR_HANDOFF at 43ca772, no prior live handoff, source unchanged since 8502a30, accepted S1/UI26 and Feed ownership. DEFERRED_OUT_OF_SCOPE — DTR26-S2-PH-L01 in old S1 API errors, stale dated AGENTS/Owner-policy/handoff-README/backend-implementer notices; ChatGPT/future approved cleanup owns repairs. Existing DTR26-FR-L01, UI26-FR-L02, SAD-FR-L01, CSA26-FR-L01, AUTH-CLOCK/older backlog remain in their registers. NOT_VERIFIED — all new runtime/security/resource/DB/API cases and future delivery publication. No historical finding closes here.

**Creation validation (2026-11-04):** All 40 relative file links across ACTIVE/this contract resolve; strict UTF-8, whitespace and final-newline checks pass, including the new untracked contract. S2A-01–12 each appear once in the acceptance table. The seven-group/28-selector gate (eight prospective RSS classes) parses as PowerShell without execution; existing selectors resolve in the current test tree. Only ACTIVE and this contract differ from entry. No production/test/tooling/frozen-contract edit, application test, provider fetch, model or worker execution occurred. These are documentation checks, not runtime acceptance.

## Engineering guidance, exclusions and handback

Relevant implementation skills: java-spring-coding-standards, pragmatic-solid-design, reuse-and-consistency, modular-monolith-architecture, jpa-postgresql-persistence, backend-testing, feed-import-workflow-modeling, rest-api-http-contracts, bounded-command-execution. Existing backend-implementer plus mandatory antigravity-self-review; no new/delegated Python agent. No auth implementation change. Python/RAG/OCR/GPU/model/retrieval/recommendation evaluation, broad benchmarks, deployment/security-wide audit and new credentials are N/A.

**Non-goals / MUST NOT:** S2B scheduler/tick/queue/cooldown/automatic polling or scaffolding; generic provider/jobs; schema/DBML/migration/index/POM/library/runtime changes; extra dialects/relative links/xml:base/feed auth; article/description/content/enclosure/media/preview fetch or mirroring; rank synthesis; other providers; frontend/Drive/OAuth/Ideaverse/RAG/reopen S19-2; unrelated refactors/LOW cleanup; historical S1 reviews/counters/evidence edits. Root personal-private-vault.zip is outside scope: do not open/modify/remove/certify. Only Owner stages/commits/pushes/tags/opens or merges PRs.

Coverage: intent/identity/data S2A-01/05/06/09/10; egress/XML/privacy/resources S2A-02–04/08/11; transactions/recovery S2A-07/08; API/shared contracts/tree/tooling/docs S2A-01/09/11/12. Every material deferred-runtime risk has mandatory evidence above; source feasibility/public API docs are not runtime PASS. No 29-dimension feature burden or new phase-final/milestone/audit for this maintenance slice.

Done: every case/negative proven, final focused gate passing, scoped docs consistent, Antigravity self-review complete. Handback: exact commands/exits/counts/skips/durations/unique receipts, case mapping, actual manager/peer/DNS/XML/bound measurements, PostgreSQL identity/lock/rollback/negative-control observations, changed files/packages, limitations/findings/Docs Sync/counter. No unsupported IDE-clean/warning-free/universal containment/real-feed availability/acceptance claim.

**Blockers / Owner decisions:** no material S2A choice open at creation. Stop with evidence if reviewed installed transport/parser cannot meet controls, frozen/schema/dependency change needed, prerequisites unavailable, source drift invalidates preparation, or entry-ID identity/relative links/feed auth/real calls/stronger native DNS termination requested. No weakening/feature expansion/download. S2B requires S2A independent acceptance/publication, current Owner confirmation, separate preparation applicability/decision check and handoff.

One independent Codex slice review follows self-review. Mandatory NOT_VERIFIED or open CRITICAL/HIGH/MEDIUM blocks READY_FOR_OWNER_COMMIT; LOW/INFO follow backlog policy. Initial counter 0/3. At most three correction+independent re-review rounds; stop on acceptance. After three independently blocked rounds, stop for Owner takeover decision; no automatic takeover/transferred S1 allowance.

## Implementation result

### Codex takeover — implementation submission and independent result

Owner explicitly authorized one bounded FR-01-only takeover on 2026-11-05. [Handback](codex-takeover-handback-2026-11-05.md) records the authorization, three exhausted correction/re-review pairs, /18 mask correction, actual-manager boundary/mixed-DNS regressions and 40 authoritative IANA width checks. Fresh implementer tests: **178 RSS + 76 HN transport = 254 passing**, zero failures/errors/skips. Pre-fix reproduction: 8 tests, 4 expected connector-call failures. Receipts, full logs, copied XML, entry hashes and source applicability are retained in `logs/daily-trends-radar/codex-s2a-takeover1-20261105-7f072fd413a64b8991263179531be8a0/`.

This takeover uses focused offline normal-profile tests within the Owner's authorization. The existing seven-selection whole-slice gate retains its prior receipts. Subsequent [independent takeover review 1](reviews/2026-11-05-dtr26-s2a-independent-takeover-review-1.md) CLOSED FR-01 after **178 fresh independent RSS tests**, zero failures/errors/skips, zero reserved connector calls and 131,072 width samples with zero mismatches; **361 unchanged independent tests** are reused with source applicability. This is separate review acceptance, not implementer self-acceptance.

### Prior Antigravity submission — history

Implementer submission snapshot: `IMPLEMENTED_AWAITING_CODEX_REVIEW` (2026-11-04, Remediation Round 3). Implemented by Antigravity under authorized maintenance slice `DTR26-S2A`.
- Handback: [implementation-handback-remediation-round3-2026-11-04.md](implementation-handback-remediation-round3-2026-11-04.md) (preserved Round 2 handback: [implementation-handback-remediation-round2-2026-11-04.md](implementation-handback-remediation-round2-2026-11-04.md) and Round 1 handback: [implementation-handback-2026-11-04.md](implementation-handback-2026-11-04.md)).
- Self-review: reported complete by implementer (zero POM/schema edits; FR-01 IANA public prefix allowlist with zero connector calls; FR-06 TLS hostname verifier path, slow exchange/in-flight interruption/pool shutdown, exact boundaries, absence of credential/hostile markers across exceptions, logs, and HTTP responses, accurate types and dynamic PG assertions).
- Verification: full 7-selection gate passed (524 tests, 0 failures, 0 errors, 0 skipped, all exit 0). Receipts preserved in `logs/daily-trends-radar/dtr26-s2a-80228397e1b24e20a1e6810cc23f1f53/` (prior receipts preserved in `dtr26-s2a-2754e33588f24e9c92c188bfd0035ee0/`, `dtr26-s2a-f0ccb04819e94a9f99c305d58151addd/`, and `dtr26-s2a-379d97fe1983494ab9a651d9aea4547f/`).
- Remediation counter at submission: `3/3 consumed; 0/3 remaining; takeover NOT_AUTHORIZED`.
- Independent disposition (2026-11-05): [re-review 3](reviews/2026-11-05-dtr26-s2a-independent-re-review-3.md) supersedes submission acceptance/proof claims; FR-06 CLOSED with independent supplement, FR-01 HIGH OPEN. Fresh independent 229 tests pass; 295 unchanged independent tests reused. Antigravity STOPPED; Owner disposition required.

## Codex remediation

The [initial review](reviews/2026-11-04-dtr26-s2a-independent-final-review.md), [re-review 1](reviews/2026-11-04-dtr26-s2a-independent-re-review-1.md) and [re-review 2](reviews/2026-11-04-dtr26-s2a-independent-re-review-2.md) remain immutable. Historical pre-takeover disposition: [independent re-review 3](reviews/2026-11-05-dtr26-s2a-independent-re-review-3.md) retained `FR-02–05` CLOSED and closed `FR-06` with the new marker/bound tests and independent configured TLS, measured lifecycle/cleanup and serialized-envelope proof. It corrected submission overstatements and retained full-ID/identity repairs. `FR-01` HIGH remained OPEN at that review: 2003::/16 was admitted although only 2003::/18 was allocated; reserved 2003:4000::1 reached the recording connector once without a real connection. The subsequent independent closure is recorded below.

Current closure: [independent takeover review 1](reviews/2026-11-05-dtr26-s2a-independent-takeover-review-1.md) CLOSED FR-01 and retained FR-02–06 CLOSED. Stable IDs and all historical records are preserved. Antigravity **3/3 consumed; 0/3 remaining; EXHAUSTED / STOPPED**; the Owner-authorized FR-01-only Codex takeover **1/1 is independently accepted, STOP**. [Handback](codex-takeover-handback-2026-11-05.md) retains implementer authority/identity and the exhausted correction/re-review ledger. No fourth Antigravity round, automatic additional takeover, new capability/dependency or frozen-boundary authority.

## Final review

**READY_FOR_OWNER_COMMIT** — [independent takeover review 1](reviews/2026-11-05-dtr26-s2a-independent-takeover-review-1.md), 2026-11-05. All DTR26-S2A-FR-01–06 CLOSED; no blocking finding or mandatory unverified acceptance remains in S2A. Owner publication and brief slice closeout remain pending. S2B needs separate Owner approval and handoff.

**Docs Sync Check — independent acceptance (2026-11-05):** UPDATED — independent takeover review, this contract/ACTIVE acceptance and RSS API status header only. ALREADY_CONSISTENT — API/package/module/schema boundaries and source-applicable evidence. DEFERRED_OUT_OF_SCOPE — existing PH-L01/backlog and stale governance notices. NOT_VERIFIED — future Owner publication and unrelated coverage. All historical reviews, handbacks and receipts remain unchanged; no mandatory S2A proof remains open.

**Suggested commit:** `feat(feed): add manual RSS refresh and combined radar`.

**Next step:** Owner | stage/commit/push accepted S2A delivery, then request ChatGPT's brief publication closeout | DTR26-S2A and independent takeover review 1 | READY_FOR_OWNER_COMMIT; closeout uses actual published SHA; S2B remains separately gated | Antigravity 3/3 exhausted; Codex takeover 1/1 independently accepted, STOP.
