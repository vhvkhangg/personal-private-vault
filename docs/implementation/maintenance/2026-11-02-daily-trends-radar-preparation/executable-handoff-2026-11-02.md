# Executable handoff — DTR26-S1: manual HN refresh and cached Radar

- **Handoff ID:** `DTR26-S1`
- **Created by / date:** Codex / 2026-11-02
- **Status:** `READY_FOR_OWNER_COMMIT`
- **Type:** One Owner-approved Feed maintenance/feature slice extending frozen Phase 10/13; no numbered phase reopened.
- **Authority:** Owner's current `$codex-create-handoff` invocation after [reviewed preparation](README.md) publication at `974c8c26ae9efcd236f3e590a536527c5200b2bf`; [binding ARTICLE_URL decision](owner-scope-and-decisions.md) and [independent READY_FOR_HANDOFF re-review 1](reviews/2026-11-02-independent-pre-handoff-re-review-1.md).
- **Entry baseline:** Local `main`, `origin/main` and live remote `refs/heads/main` verified at `974c8c26ae9efcd236f3e590a536527c5200b2bf`; initially clean tree. Reviewed source remains applicable: no source/tooling/frozen-contract diff from preparation's `6da26d4` baseline.
- **Implementer:** Codex under the Owner's one bounded 2026-11-04 `$codex-implementation-takeover` authorization for the remaining FR-05 test/evidence defect only; Antigravity stopped. Codex is the implementer, not the independent reviewer.
- **Independent reviewer:** Codex, separate from the Codex takeover implementation session; [independent takeover review 1](reviews/2026-11-04-dtr26-s1-independent-takeover-review-1.md), 2026-11-04. Codex takeover authorship is disclosed; this review authored no implementation correction.
- **Implementation / acceptance:** **READY_FOR_OWNER_COMMIT** under independent takeover review 1. **FR-01–07 CLOSED; DTR-01–12 verified** within the existing scope/limits. Actual ingest/source-update/finalizer share one physical transaction; the same assertion detects a separately committed test-only REQUIRES_NEW ingest. Fresh independent 21 PostgreSQL tests pass with zero failures/errors/skips; unchanged independent compatibility/architecture/provider/Radar/web evidence retained. Implementer final 105 results separately reconciled. Production unchanged by takeover. [Handback](implementation-handback.md) remains the dated implementer submission; prior reviews preserved. L01 LOW remains OPEN/NONBLOCKING. Owner publication pending.
- **Remediation:** **Antigravity `3/3` consumed / EXHAUSTED / STOPPED; one authorized Codex takeover independently accepted; loop STOPPED.** No fourth round or further implementation authorized. The takeover changed one integration test and three handoff/handback documents; this independent review adds its report and synchronizes ACTIVE/contract only. Production, contracts, immutable reviews and historical counters preserved. Owner alone publishes accepted S1; no next feature follows automatically.

## Outcome and authority

The authenticated Owner manually refreshes one configured, enabled Hacker News source, browses a bounded cached preview of links submitted within the last 24 hours, and explicitly saves an article through the existing Feed Item → Universal Inbox API. Store only link/descriptive metadata and captured HN provenance. No external article fetching, background refresh or AI is part of S1.

Read the [Owner decisions](owner-scope-and-decisions.md), [current preparation re-review](reviews/2026-11-02-independent-pre-handoff-re-review-1.md), [corrected design](source-reuse-and-api-design.md), [provider policy](provider-security-and-ranking.md), [acceptance plan](acceptance-and-review-plan.md) and [PH-01/02 response](ph01-ph02-correction-response-2026-11-02.md). The re-review closes PH-01/02 **for preparation**, with mandatory runtime proof below; the [initial review](reviews/2026-11-02-independent-pre-handoff-review.md) remains immutable. Earlier pending-review/preparation-only wording describes submission history; this issued handoff controls S1 execution.

Applicable references: [Owner policy](../../../workflow/current-owner-delivery-policy.md), [Docs Sync](../../../workflow/documentation-synchronization.md), [ACTIVE](../../handoffs/ACTIVE.md), [root instructions](../../../../AGENTS.md), [backend instructions](../../../../backend/AGENTS.md), [Feed instructions](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/AGENTS.md), [test instructions](../../../../backend/src/test/AGENTS.md), [docs instructions](../../../AGENTS.md), [Phase 10](../../phase-10/README.md), [Phase 13](../../phase-13/README.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [module matrix](../../../architecture/module-dependency-matrix.md), [API architecture](../../../architecture/api-architecture.md), [package tree](../../../repository/repository-package-tree.md), [DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml) and [published UI26 closeout](../2026-11-01-universal-inbox-preparation/owner-publication-closeout-2026-11-02.md).

This is the narrow maintenance exception to historical Feed phase gates, covering only the change map below. Schema, module graph, root HTTP conventions and accepted mutation contracts stay frozen; no migration or new architecture decision is approved.

## Simplicity and reuse

| Decision | Selected mechanism / burden |
| --- | --- |
| KEEP / REUSE source management | Existing `FeedSourceOperations`, type/enabled/scheduling metadata and source lock. Do not tighten existing URL/config acceptance or auto-create sources. |
| KEEP manual provider refresh | One fixed-host HN adapter and owner-local orchestration/finalizer. Moderate work lies in bounded HTTP, normalization and transaction tests; no measured duration is claimed. |
| REUSE persistence and saving | Unchanged `FeedItemOperations.ingestFetch`, URL-hash identity, JSON snapshots and `SavedResourceOperations.saveFeedItem`; no parallel job, bookmark or ingest engine. |
| KEEP / SIMPLIFY read | One SQL-bounded cached 24h preview, existing source attribution and submission-time/id ordering. No cursor, `hours` knob, total or popularity model. |
| REUSE client/build/tooling | Installed Apache HttpClient 4.5.13 via AWS SDK 2.29.50; no POM/install/upgrade. Existing watchdog, backend implementer and independent reviewer. |
| DEFER / REJECT | RSS/Atom/scheduler S2, other providers, bodies/comments/media, frontend, RAG, global scores, new module/schema/queue/provider registry and benchmark project. |

## User-visible acceptance — MUST

| ID | Scenario and observable result | Minimal evidence / existing reuse |
| --- | --- | --- |
| DTR-01 | Authenticated manual refresh accepts only an existing enabled HN source. Missing/invalid JWT, missing source, disabled source or other type fail safely before network. Type/disable changes during HTTP prevent a later HN commit. Existing source creation/edit acceptance remains intact. | New web/finalization tests; existing source operations. Preflight is not commit authority. |
| DTR-02 | Only fixed official HN top-list/item endpoints are requested. Configured URLs, article/discussion links, comments and media are never fetched; zero redirects and unsafe peers/proxies denied. | Production client wiring with injected recording resolver/socket/transport; no real unsafe-destination or provider request. |
| DTR-03 | The concrete operation/body/parser/text/metadata/deadline limits below are enforced. Timeout, 429, 5xx, invalid JSON and limit failures return safe errors, with no ingest or timestamp change. | Client/parser/admission boundary tests, including absent Content-Length, slow/drip input and unsupported compression. Limits are acceptance policy, not measured PASS today. |
| DTR-04 | ARTICLE_URL normalization: one candidate per exact trimmed external article URL; latest valid submission time, then greatest HN ID wins. `externalId=null`; representative ID/discussion/rank/score/capture stay bounded provenance. Explicit skips differ from malformed/failing responses. | Normalizer fixtures: same URL/different IDs, tie, missing/invalid article link, jobs/dead/deleted/null, malformed required fields, literal HTML-shaped title and exact trim-only identity. |
| DTR-05 | The unchanged generic ingest runs inside the short HN finalizer transaction. Same URL updates one FeedItem, including a legacy nonnull HN external ID; representative replacement preserves item ID; revised article URL creates a distinct row. Repeat refresh preserves identity. | Real PostgreSQL unique-key/upsert tests: `hn:101` / `hn:102` one URL, legacy `hn:old`, subsequent winner and changed URL; no migration. |
| DTR-06 | Changed type/disable, partial upstream/parse failure and ingest/finalization failure preserve committed items and fetch state. Prove rollback of staged inserts **and updates**, not just an early rejection. Valid empty/all-skipped complete selections can succeed with truthful zero processed count. | Deterministic HTTP/staging barriers and before/after PostgreSQL snapshots; source edits in their separate transaction remain committed. |
| DTR-07 | Same-source refresh is single-flight, admission is bounded/released on success/failure/interruption, and another source can progress. Source edits can commit during blocked HTTP. Final source validation/lock lasts through the same commit; existing item-then-source lock order is preserved. | Real PostgreSQL lock/transaction identity and contention proof plus admission tests. No SQL lock/ambient transaction over HTTP, detached late ingest, distributed lease or queue. |
| DTR-08 | Authenticated GET reads only cached eligible-source data in fixed UTC 24h bounds, default 50/max 100, deterministic submission-time/id order. Null/old/future timestamps and disabled/other-type sources are excluded. Invalid source filters fail safely. GET performs no network. | Fixed-clock SQL/web boundary, tie, filter/limit/empty and no-client-call tests. Legacy per-source reads unchanged. |
| DTR-09 | Rank is representative position **at that capture**, not current rank/global popularity. `publishedAt` is HN submission creation time. Cached rows omitted from newer captures are not presented as the current top list. Missing legacy provenance yields no invented rank/capture. | Multi-refresh/read fixtures, provenance fields and truthful OpenAPI/API note. |
| DTR-10 | Existing save API stores the exact trimmed **article URL**, not the HN discussion URL. Duplicate SavedResource still conflicts; existing saved snapshots do not auto-update. Existing Vault/Inbox actions remain compatible. | New end-to-end fake-refresh → Radar → existing save → Inbox assertion and duplicate/unchanged-snapshot proof; source-applicable Feed/Inbox web regressions. |
| DTR-11 | Exactly two new routes use existing envelope, safe errors, authentication and explicit DTOs. Generated OpenAPI preserves all 231 preexisting operations and becomes exactly 233 with unique IDs and accurate schemas/parameters/security. | New web assertions plus current exact OpenAPI inventory; no RAG/archived QA selector. |
| DTR-12 | Only approved Feed/tests/current API inventory/docs and handback targets change. All required evidence is retained; Antigravity self-reviews before independent Codex review. Owner alone publishes. | Actual tree/diff, architecture checks, receipts, DTR-01–12 mapping, Docs Sync and counter. No assumed runtime acceptance. |

## Exact HTTP/read contract

### Manual refresh

`POST /api/v1/feed/sources/{sourceId}/refresh?maxItems=30`, operation ID **`refreshFeedSource`**. Positive `sourceId`; optional integer `maxItems`, default **30**, range **1–50**; no request body or new source/config mutation. Select the first `min(maxItems, topListSize)` positions without backfilling skipped items. This action alone can contact HN; bean construction, startup, health and all GET/source-edit routes cannot. Evaluate source/proxy eligibility on refresh, not as a bean-creation failure that would disable cached reads or existing source management.

Success: **200**, existing `ApiResponse<FeedRefreshResponse>` with null error/meta. Data fields: `sourceId`, `fetchedAt`, `selectedTopIds`, `eligibleStories`, `distinctArticleUrls`, `processedItems`. `selectedTopIds` counts the selected prefix; `eligibleStories` counts live, valid article-bearing submissions before grouping; `distinctArticleUrls` counts groups passed to ingest; `processedItems` counts returned ingest views, **not inserted rows**. Required inequality: `0 <= processedItems = distinctArticleUrls <= eligibleStories <= selectedTopIds <= maxItems`. Capture `fetchedAt` in UTC after complete fetch/normalization and pass it unchanged to ingest/provenance. Return success only after finalizer commit. No fabricated inserted/updated/skipped-reason counts or automatic save.

### Cached Radar

`GET /api/v1/feed/radar?limit=50&sourceId=<optional>`, operation ID **`getFeedRadar`**. Positive optional source ID; default limit **50**, range **1–100**. No cursor/offset/total, adjustable window or remote I/O. Do not read or advertise `hours`; ordinary unrecognized-query behavior stays unchanged and cannot alter the fixed window.

Success: **200**, `ApiResponse<FeedRadarResponse>` with null error/meta. Data: `items`, `windowStart`, `windowEnd`, `retrievedAt`. Read the existing injected UTC `Clock` once: `retrievedAt = windowEnd = now`, `windowStart = now - 24h`. Inclusive submission-time bounds. Query Feed-owned `feed_items` joined to `feed_sources`, enforcing `enabled=true`, `type=HACKER_NEWS`, nonnull `publishedAt` in bounds, optional source ID, `publishedAt DESC, id DESC` and database-side limit. No count/load-all/N+1. This is a bounded preview, not a complete feed or current HN membership snapshot.

Item fields: `feedItemId`, `sourceId`, `sourceName`, `sourceType`, `title`, `url`, `publishedAt`, `fetchedAt`, `signalType`, nullable `sourceRank`, `signalCapturedAt`, `hnStoryId`, `hnDiscussionUrl`. `url` is the stored article URL. Validate legacy URL/provenance data safely: use the same article-URL data check; invalid legacy URLs may reduce the already SQL-bounded preview, without unbounded backfill. Valid captured HN provenance gives `signalType=HN_TOP_STORIES_AT_CAPTURE` and its recorded rank/capture/story/discussion. Missing/malformed legacy provenance gives `signalType=SOURCE_CACHE` and null provenance fields, not a 500 or invented HN claim. No raw metadata/body/score aggregation in this response. Include an empty list with the same UTC bounds when there are no matches.

Validate a supplied source filter as existing/enabled/HN and also keep source eligibility in the SQL query; an earlier source read alone is insufficient. Source attribution is current source metadata, while item capture/rank belongs to that row's saved representative. Failed refreshes and later omissions retain prior cached rows/times. No stale boolean or freshness threshold is invented. Existing item/detail/source reads and UI26 behavior stay unchanged.

### Stable errors and privacy

Use current root `ApiResponse`/error schemas and module-local `FeedExceptionAdvice`; preserve existing advice mappings. One small Feed-owned categorical refresh exception/reason is sufficient; no shared exception framework or root handler edits. Match typed causes/reasons, never exception-message text.

| Condition | HTTP / code |
| --- | --- |
| Invalid numeric type/overflow; invalid positive/range constraint | 400 / existing `MALFORMED_REQUEST` or `VALIDATION_ERROR`, respectively |
| Missing/invalid Bearer | 401 / current authentication envelope; no provider call |
| Source absent at preflight/filter validation | 404 / existing `FEED_SOURCE_NOT_FOUND` |
| Existing disabled source; existing non-HN source | 422 / `SOURCE_DISABLED`; `UNSUPPORTED_SOURCE_TYPE` (check enabled before type) |
| Same-source in flight; authority changed after preflight | 409 / `REFRESH_ALREADY_RUNNING`; `SOURCE_CHANGED_DURING_REFRESH` |
| Existing ingest uniqueness conflict | 409 / existing `FEED_ITEM_CONFLICT` |
| Transport/DNS/time/interruption failure, or unsupported configured proxy | 503 / `UPSTREAM_UNAVAILABLE` |
| Upstream 429; global admission capacity full | 503 / `UPSTREAM_RATE_LIMITED`; `REFRESH_CAPACITY_REACHED` |
| Upstream 5xx | 503 / `UPSTREAM_UNAVAILABLE` |
| Non-200 other status, redirect, bad media/encoding/JSON/required shape | 502 / `UPSTREAM_INVALID_RESPONSE` |
| Blocked actual destination; body/parser/metadata/request-count overflow | 502 / `UPSTREAM_DESTINATION_REJECTED`; `FETCH_LIMIT_EXCEEDED` |
| Finalization lock/transaction timeout | 503 / `REFRESH_FINALIZATION_UNAVAILABLE`; rollback |
| Unexpected internal failure | Existing safe 500; rollback, no raw detail |

Errors have null data and safe fixed messages. Do not echo upstream bodies, URL queries, configured targets, private address values, bearer headers, stack traces or provider text in error/log output. Log only safe source ID, category and numeric counters where useful. Do not forward PPV authentication/cookies or add credentials to HN requests. No automatic retry, Retry-After scheduling or failure-as-empty conversion.

Keep POST success at 200; the existing OpenAPI customizer recognizes creation operations by its fixed ID allowlist, and these IDs are not creations. Document Radar's additional 422 and refresh's 502/503 errors using controller-local annotations and the existing error-envelope schema. No root OpenAPI/configuration change is needed or authorized.

## Provider identity, normalization and finite budgets

The [official HN API](https://github.com/HackerNews/API), checked as documentation on 2026-11-02, documents the v0 item/topstories surface and submission creation time; title content can be HTML-shaped. Only documentation was accessed, not a provider endpoint or article. Preserve these source semantics; additional unknown JSON fields are not a reason to store them.

- Fixed outbound authority: **`https://hacker-news.firebaseio.com:443`**, paths **`/v0/topstories.json`** and **`/v0/item/{positiveLongId}.json`**, no query, userinfo, fragment, alternate host/port/scheme or configurable root. Discussion URLs `https://news.ycombinator.com/item?id=<id>` are provenance only. Never derive destinations from source/config/story URLs.
- Root response must be a valid JSON array of at most **1,000 distinct positive long IDs**; malformed/noninteger/duplicate IDs, null root or extra JSON roots fail. Validate the bounded root; fetch only the selected prefix, sequentially within one refresh. Record each original **1-based top-list position**, before skips/grouping. No kids/comments/newstories/pagination requests.
- Selected item JSON `null`, true `dead`/`deleted`, a well-formed non-story type, or absent/null/blank/invalid external article URL is an explicit skip. Wrong JSON shape/field type, malformed required eligible-story ID/title/time, mismatching requested/returned ID or invalid numeric provenance fails the request. Boolean flags must be booleans when present. A failed selected-item HTTP request always fails the whole fetch. Apply the skip policy before requiring fields irrelevant to a skipped record; no synthetic discussion fallback.
- External URL is data, never a new fetch target: exact Java `trim()` string, maximum **2,048 UTF-16 code units**, syntactically valid HTTP(S) URI with host, no userinfo/control characters. No URL canonicalization, query stripping, lowercasing or second hash. Overlength fields are limit failures; malformed link syntax/scheme is skipped. Do not resolve article hosts. Same-URL groups select greatest valid HN Unix submission time, then greatest positive HN ID; normalize `externalId=null` and order representatives deterministically by their captured rank for ingestion.
- Title: required nonblank, whitespace normalized, maximum **1,000 UTF-16 code units**; HTML-shaped characters remain literal untrusted JSON text, never HTML-rendered/executed. Do not fetch/parse article bodies or add an HTML framework. Optional author is a string up to **500** units or null; summary is null. Time is an exact nonnegative integer Unix second convertible to a PostgreSQL-safe UTC Instant (years 1970–9999). Future times may be stored but are excluded by Radar's window. No numeric/string coercion.
- Persist only `hnStoryId`, fixed `hnDiscussionUrl`, `hnRank`, nullable `hnScore`, nullable `hnCommentCount`, and ISO UTC `hnCapturedAt` in defensive `rawMetadata`. Rank is 1–50; optional score/comment count are exact integers 0–2,147,483,647 or null. Serialized metadata **<=2 KiB UTF-8 per representative**, at most 50 representatives. Ignore provider body/text/kids/unknown fields; do not retain their trees after processing the current response.

| Resource | Fixed S1 limit / enforcement |
| --- | --- |
| Requests | One top-list + at most 50 selected item GETs; default selection 30. Zero retries/redirects; no skipped-item backfill. |
| Bodies | Top-list **64 KiB**, item **256 KiB**, aggregate **2 MiB** per refresh. Enforce streaming actual bytes even without Content-Length; do not use an unbounded entity-to-string call. |
| Compression/media | Request `Accept-Encoding: identity`; disable automatic decompression. Accept absent/identity encoding only, reject gzip/other encodings before parsing. Compressed expansion budget is **zero**; accepted wire and decoded body budgets are identical. Require JSON media type, with absent/UTF-8 charset. |
| JSON | Depth **16**, any string **8,192** units, numeric token **20** characters, strict duplicate-field detection, one root. These parser limits apply to ignored content too; never retain raw response maps as metadata. |
| Time | Monotonic overall refresh budget **70s**, fetch/normalization sub-budget **60s**, individual exchange **5s** including resolution/TLS/body, DNS **2s**, connection lease **1s**, connect **2s**, socket inactivity **2s**. Deadline caps the remaining work; inactivity timeouts alone do not stop a slow-drip response. |
| Finalization | Short transaction timeout **5s**, within remaining overall budget, with effective JDBC/PostgreSQL lock/statement timeout bounded accordingly. No network within that transaction and no detached DB work after a timeout response. |
| Admission/retention | At most **two active refreshes**, distinct sources; one per source. Fail fast, no queued requests. At most two owned blocking exchange/resolver workers of each kind if required, no unbounded queue/thread growth; cleanup budget **2s** within the overall deadline. Retain only current bounded response, selected IDs and <=50 normalized representatives. |

These are selected technical limits within the reviewed finite scope, not measured performance claims or permission to raise limits. Test-only injection may use stricter budgets/fake monotonic time, without production bypasses. Check remaining budget before new requests/finalization; cancellation must close/abort owned HTTP resources, release source admission, restore interruption where applicable and prevent late publication. Native DNS cancellation may be limited by the host: contain blocked resolution in the bounded owned capacity, discard late results and report the actual limit; do not claim universal kernel cancellation or create replacement threads without a cap. If required enforcement is not feasible with the existing client, stop with evidence for a bounded amendment.

### Actual destination/TLS/proxy enforcement

Use the installed Apache **4.5.13** hooks, inspected locally with `javap`: resolver, route planner, SSL hostname verifier and redirect/compression/retry controls. The [Apache builder documentation](https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/client/HttpClientBuilder.html) was also checked; its current page labels 4.5.14, so installed classes remain the version authority. A custom connection manager can override builder resolver settings; wire validation into the manager/connection operator actually used.

Resolve only the fixed HN host, cap returned addresses at **16**, validate every supplied IPv4/IPv6 candidate and pass only that validated resolution to actual socket selection. Deny loopback, unspecified, private/site-local/ULA, link-local/cloud metadata, CGNAT, multicast, documentation/benchmark/reserved and unsafe mapped/transition forms; reject mixed public/unsafe answers rather than falling back. No second independent resolution after validation. Keep original HN hostname for SNI/certificate identity, normal certificate-chain verification and hostname verification; no trust-all/no-op TLS. A hostname allowlist or separate DNS precheck is insufficient.

S1 is **direct-only**: fixed direct route, no ambient ProxySelector/system-property/environment proxy routing, credentials or authentication retries. Detect configured HTTP(S)/SOCKS proxy requirements and fail safely rather than silently bypassing a required proxy; proxy support needs a later amendment. Keep this client private to Feed; do not change S3/global JVM networking. Reject every redirect status without following Location, regardless of its destination.

Tests must exercise the production builder/resolver/connection-operator path using an injected recording socket/transport boundary. Prove the validated peer is the peer offered to the connector, unsafe/mixed/rebinding answers cannot reach a socket, redirect targets are never requested, original TLS host verification remains configured, and proxy settings cannot route around policy. Stub the lowest I/O boundary; do not replace the entire client/policy and call that enforcement proof. No actual localhost/private/public provider socket or production allowlist exception is required.

## Atomic finalization, authority and admission

1. The manual-refresh application entry must reject an ambient database transaction before any provider I/O (a separate Spring-proxied `Propagation.NEVER` orchestration boundary is sufficient). Preflight source state is a short read, not a lock spanning HTTP. Acquire bounded per-source/global admission and release it in `finally`, including validation, timeout, failure and interruption. Source create/update remains independent.
2. Fetch and normalize the complete selected list outside a transaction. Do not ingest partial successes. An entirely valid empty top list, or complete selected list whose records all meet skip rules, can finalize an empty batch. Failure cannot masquerade as either case.
3. Call a **distinct Spring-proxied HN finalizer** with one short outer REQUIRED transaction, same thread/transaction manager. Invoke **unchanged `FeedItemOperations.ingestFetch`** inside it; no self-invocation, `REQUIRES_NEW`, source pre-lock or generic ingest rewrite. Preserve its item-upsert → locked source read/refresh → flush order.
4. After ingest returns, re-read/refresh the authoritative source under its retained `findByIdForUpdate` lock and require `enabled && type == HACKER_NEWS` before outer commit. Flush is not commit. A changed source throws a rollback-causing Feed exception; all staged inserts/updates and fetch timestamps roll back. If generic ingest first rejects a now-disabled/missing source, translate that typed failure in this HN-only call path to `SOURCE_CHANGED_DURING_REFRESH`; never classify by message or recover to success from rollback-only state. Generic callers retain their existing exceptions/behavior.
5. Source lock stays held through outer completion. Do not release it between validation and commit, reverse lock order or hold it over network. Existing scheduled metadata/`nextFetchAt` calculation is reused but starts no scheduler. Return counters only after commit; late/cancelled HTTP workers can never invoke finalization.

Required real-DB proof: same physical transaction/backend connection participates in ingest and finalization; a source update attempted **after final validation** waits until outer completion; generic concurrent-ingest regression stays valid. Use test-local spies/AOP/transaction synchronizations and PostgreSQL lock observations, not production latches or timing-only sleeps.

Race tests must separately prove: (a) HN→enabled RSS and HN→disabled source edits **commit while fake HTTP is blocked**, then refresh fails safely; (b) failure after item inserts **and a preexisting-row update have been staged/flushed** rolls those values and fetch state back. Include the disable-after-staging path so an early disabled-source check is not mistaken for rollback proof. Compare against authoritative state after the independently committed Owner edit; do not undo that edit. Use bounded waits/joins and failure-safe barrier release. Another source must progress while one refresh is blocked; admission must be reusable after every terminal path.

## Precise change map and ownership

Production paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`. Minor local helper names may vary; ownership, allowed targets, existing contracts and exact test-selector names may not silently change.

| Allowed target | Bounded outcome |
| --- | --- |
| `feed/source/FeedSourceOperations.java`, `feed/internal/application/FeedSourceService.java` | Add one manual-refresh operation/delegation returning the new result view. Preserve create/update/read/due methods and accepted validation/locking bodies. No callback cycle through source operations. |
| `feed/internal/application/HackerNewsRefreshService.java`, `HackerNewsRefreshFinalizer.java`, `HackerNewsRefreshAdmission.java` — new | Transaction-free orchestration/admission and separate proxied short finalizer. Same-owner repositories/public generic ingest only; no job engine. |
| `feed/internal/infrastructure/hackernews/` — new concrete internal adapter package | `HackerNewsClient`, `HackerNewsNormalizer`, `HackerNewsDestinationPolicy`, small immutable limits/result helpers as needed, and `package-info.java`. Fixed transport/parsing/mapping, actual connection controls and test seams; no generic provider registry or framework. |
| `feed/item/FeedItemOperations.java`, `feed/internal/application/FeedItemService.java`, `feed/internal/infrastructure/persistence/FeedItemRepository.java` | Add only Radar read/query/mapping. Prefer bounded scalar projection for source/provenance fields. **Do not change generic `ingestFetch`, its validation/upsert/lock/transaction semantics or existing recent reads.** |
| Existing `feed/source/exception/` | One small refresh exception with finite typed reasons, keeping public/internal placement consistent. No duplicate base hierarchy. |
| Existing `feed/view/` | Immutable refresh result/Radar/item views with defensive list copying. Reuse `FeedJsonSnapshot` for normalized provenance. |
| `feed/internal/web/controller/FeedController.java`, existing `feed/internal/web/dto/`, `feed/internal/web/mapper/FeedWebMapper.java`, `feed/internal/web/advice/FeedExceptionAdvice.java` | Exactly two routes, explicit refresh/Radar/item response DTOs, mapping/validation and local categorical error translation/OpenAPI. Preserve every existing mapping. |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/` and matching internal test subpackages (repository-relative) | New named provider/admission/refresh/concurrency/Radar/web tests below. Adjust existing Feed/Inbox test setup only where a new dependency/behavior assertion requires it; no shared fixture or RAG changes. |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` (repository-relative) | Add refresh/Radar manifest rows, exact **231 → 233** counts, unique IDs, DTOs, bounds/errors/bearer semantics. Preserve every old route/schema/assertion; no permissive inventory replacement. |
| Current documentation and handback below | Only expressly authorized API/inventory documentation and result/status artifacts. |

This permits one concrete internal provider package and additive current Feed inventory, not a new top-level module, new public named interface, dependency direction, architecture baseline revision or schema. Read-only references include source repository/domain, existing SavedResource/Vault/Knowledge operations, root Clock/HTTP/security and test support. Do not modify those implementation contracts to make the new path pass. No deletion is authorized.

## Focused tests and final verification

New tests must prove behavior rather than mirror helpers: `HackerNewsClientTest`, `HackerNewsDestinationPolicyTest`, `HackerNewsNormalizerTest`, `HackerNewsRefreshAdmissionTest`, `HackerNewsRefreshIntegrationTest`, `HackerNewsRefreshConcurrencyIntegrationTest`, `FeedRadarIntegrationTest`, `DailyTrendsWebIntegrationTest`. Use the existing PostgreSQL/Testcontainers fixtures and test-local transport/resolver/clock seams. No live HN smoke, unsafe-destination request, H2, production test bypass or inference.

Cover each DTR row plus exact boundaries: selection 1/default 30/max 50/invalid 0/51, root 1,000/1,001, streaming body and aggregate caps, depth/string/metadata limits, duplicate JSON fields/root IDs, gzip rejection and absolute deadline despite trickle input. Same-URL winners, tie ID, trim-only URL variants, legacy external ID, changed URL and unchanged saved snapshot need assertions against real stored data. For Radar test UTC cutoff equality, one instant outside each bound, future/null, ties, empty/default/max/invalid limit, source filtering and safe legacy provenance; assert GET/source edit/startup does not call transport. Verify untrusted title characters remain JSON text and no marker-bearing upstream payload/URL/token leaks through errors/logs.

Use fixed clocks and short deterministic test budgets where appropriate; do not wait 60/70 seconds merely to mirror constants. Exercise production limit configuration and connector wiring separately so smaller test budgets do not hide missing production enforcement. Retain SQL/query-shape and row-cap observations from the synthetic Radar fixture; no performance SLO or benchmark subsystem is introduced. Missing mandatory real-DB/transport evidence blocks acceptance.

Run this final gate from repository root, PowerShell. Each focused command uses the accepted executable watchdog, normal profile **300s maximum**, fresh ignored receipts. All named classes are explicit; no RAG selector.

```powershell
$dtrPython = (Get-Command python -ErrorAction Stop).Source
$dtrRunDir = Join-Path 'logs/daily-trends-radar' ('dtr26-s1-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $dtrRunDir -ErrorAction Stop | Out-Null
$dtrChecks = @(
    @{ Name = 'provider-unit'; Tests = 'HackerNewsClientTest,HackerNewsDestinationPolicyTest,HackerNewsNormalizerTest,HackerNewsRefreshAdmissionTest' }
    @{ Name = 'refresh-postgres'; Tests = 'HackerNewsRefreshIntegrationTest,HackerNewsRefreshConcurrencyIntegrationTest' }
    @{ Name = 'radar-postgres'; Tests = 'FeedRadarIntegrationTest' }
    @{ Name = 'radar-http'; Tests = 'DailyTrendsWebIntegrationTest' }
    @{ Name = 'feed-compatibility'; Tests = 'FeedIntegrationTest,FeedValidationTest,FeedIngestionConcurrencyTest' }
    @{ Name = 'web-inbox-openapi'; Tests = 'FeedWebIntegrationTest,SavedResourceInboxWebIntegrationTest,OpenApiRouteInventoryIntegrationTest' }
    @{ Name = 'architecture'; Tests = 'ApplicationArchitectureTests' }
)
foreach ($dtrCheck in $dtrChecks) {
    $dtrReceipt = Join-Path $dtrRunDir ($dtrCheck.Name + '.json')
    $dtrTestArgument = '-Dtest=' + $dtrCheck.Tests
    & $dtrPython scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt $dtrReceipt -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' $dtrTestArgument test
    if ($LASTEXITCODE -ne 0) { throw ('DTR26-S1 verification failed: ' + $dtrCheck.Name) }
}
```

Split groups into their exact named classes if needed for the cap, preserving the same evidence set and unique receipts. Run focused checks while implementing, then retain final source-applicable receipts; repeat only for fixes/new drift. Existing unchanged independent receipts may support untouched adjacent behavior when exact source applicability is established; they cannot substitute new provider/finalizer/read acceptance.

B1/B2 watchdog and scoped Windows native hook enforcement are Owner-published: ordinary cap **300s**, separately preapproved full-suite ceiling **2,100s**, inactivity diagnostics and owned-tree cleanup. **This handoff approves no full suite/extended profile, `mvn clean verify`, live provider smoke, RAG/QA selection, `ppv.test.archived-s19-2=true`, model/worker startup or runtime download.** Native trust/root/client limits still apply; no startup/configuration/trust/hook repair or bypass. Missing local images/build/runtime prerequisites require a factual stop, not installation or unbounded retry. Save full output to new ignored logs; never overwrite historical receipts. No IDE-clean/dependency-warning-free or runtime success claim without actual evidence.

## Documentation impact and owner

| Affected current/derived document | Authorized editor / observable sync evidence |
| --- | --- |
| `docs/architecture/daily-trends-radar-api-extension.md` — new; narrow link in `docs/architecture/api-architecture.md` | Antigravity with implementation: delivered routes/examples/errors, fixed budgets/identity, live versus cached capture semantics, source authority/failure behavior and exclusions. Codex independently compares source/OpenAPI/tests. Do not rewrite historical API sections. |
| `docs/repository/repository-package-tree.md` | Antigravity may add only the actual new internal HN adapter package/current Feed capability inventory. No relocation/version/baseline-policy rewrite or diagram regeneration. Verify package-info for every new package and the actual changed tree. |
| This contract / ACTIVE / future `implementation-handback.md` in this scope directory | Antigravity records results, receipts/self-review/Docs Sync and status `IMPLEMENTED_AWAITING_CODEX_REVIEW`; preserve issued requirements and never self-accept or claim publication. Codex adds independent review/remediation. |
| Preparation README, maintenance navigation, broader notices and post-publication closeout | ChatGPT for separately requested governance/closeout, grounded in the actual published SHA. Owner alone publishes. No unrelated cleanup in implementation. |

**Docs Sync Check — creation:** `UPDATED` — this executable contract and ACTIVE only. `ALREADY_CONSISTENT` — reviewed ARTICLE_URL preparation published at `974c8c2`, no prior live handoff, UI26 closeout and unchanged reviewed source/client/ownership baseline. `DEFERRED_OUT_OF_SCOPE` — stale CSA26/phase notices in root AGENTS, current policy and handoff README, plus old implementer pending-tooling wording; current ACTIVE/review/Owner decisions govern, with ChatGPT cleanup only under separate authority. The preparation documents' pending-review/publication statements retain their dated submission meaning. `NOT_VERIFIED` — all DTR-01–12 implementation, actual transport controls, resource enforcement, SQL/transaction/lock proof and future Owner publication.

At handback/review add concise changed-path, API/source/test/link and remaining-drift evidence in the existing artifact. Keep `UI26-FR-L02`, `CSA26-FR-L01`, `SAD-FR-L01`, AUTH-CLOCK and prior backlog open under their own records. DTR26-PH-01/02 preparation closure is not runtime closure. Preserve all accepted/failed historical reviews/ADRs/decisions/receipts and prior counters.

**Creation validation (2026-11-02):** All 36 relative file links across ACTIVE and this contract resolve; UTF-8/whitespace/final-newline checks pass, including the new untracked file. DTR-01–12 each appear once in the acceptance table. The seven-selection PowerShell gate parses without execution. Implementation/tooling/frozen-contract diff against HEAD is empty; only the two handoff documents changed. These checks do not establish runtime acceptance; no provider endpoint, test suite, model or worker was executed.

## Engineering guidance, exclusions and completion

Use existing `backend-implementer`, `feed-import-workflow-modeling`, `java-spring-coding-standards`, `modular-monolith-architecture`, `jpa-postgresql-persistence`, `rest-api-http-contracts`, `backend-testing`, `bounded-command-execution`, `pragmatic-solid-design` and `reuse-and-consistency`. Consult `design-pattern-selection` only for a concrete need, `graphify-context` only for source-verified navigation, and `antigravity-self-review` before submission. Codex's independent path is `codex-final-review` with `codex-review-coverage`. No new agent/skill/rule/hook; Python/RAG/authentication implementation skills are N/A because those implementations are excluded.

Coverage trace: identity/domain/compatibility DTR-04/05/09/10; actual egress/privacy/resource bounds DTR-01–03; transactions/concurrency/recovery DTR-05–07; read/API validation and SQL bounds DTR-08/11; folder/shared-contract/tooling/docs/delivery DTR-11/12. Runtime transport/time/transaction evidence is mandatory NOT_VERIFIED until produced. OCR, GPU/model evaluation, recommendation/retrieval quality, new credentials, deployment and broad benchmarks are N/A. The rubric cannot add features or speculative infrastructure.

MUST NOT add RSS/Atom/scheduler/S2, GitHub/Reddit/X/Facebook/TikTok, Drive/Dashboard/frontend, bodies/comments/media/profile scraping, URL previews, personalization/AI/global scoring, provider framework/queue/lease, new module/schema/index/migration/POM/dependency, root HTTP/auth/config changes, RAG/Ideaverse/indexing/live-original capability or unrelated frozen repairs. Existing schedule flags remain configuration only. No deletion, agent staging/commit/push/tag/PR, destructive repository commands or historical log mutation.

Done requires DTR-01–12 behavior/negatives, real mandatory evidence, passing focused gate, consistent current docs and completed Antigravity self-review. Handback supplies per-case test/receipt pointers, actual commands/exits/counts/skips/durations, changed files, client/deadline/peer enforcement observations, DB transaction/lock evidence, precise limitations, findings/counter and Docs Sync. No test-count quota or fabricated clean/acceptance claim.

Stop with the exact missing gate if transaction/actual-peer/deadline proof cannot be delivered, a required prerequisite is missing, unsafe test selection is requested, existing generic/frozen contracts conflict, or a broader source/schema/dependency/architecture change appears necessary. Request the smallest bounded amendment; no check-then-ingest workaround, assumed evidence, hook bypass, unapproved repair or raised limits.

One independent Codex S1 review follows self-review. No extra numbered-phase final/milestone or repeated full-suite gate is invented. Mandatory NOT_VERIFIED or open CRITICAL/HIGH/MEDIUM blocks `READY_FOR_OWNER_COMMIT`; LOW/INFO follow current feature policy. Three correction + independent re-review rounds maximum, then stop for separately authorized takeover with independent subsequent review. Initial counter is 0/3. Owner publishes accepted S1, then ChatGPT records its actual closeout; S2 requires a separate approval/preparation/handoff. The future comprehensive non-RAG audit's all-actionable-severity rule is not an endless S1 correction loop.

## Implementation result

Codex's bounded takeover changes one integration-test file and these three handoff/handback documents only. The final PostgreSQL observation is REQUIRED ingest/source-update/finalizer PID/transaction `81/883` throughout; the passing negative control observes independently committed ingest `82/878` versus finalizer `81/879` and proves the shared-identity assertion rejects it. Final-source refresh/concurrency 21, Feed compatibility 47 and architecture 37 tests pass (105 total, zero failures/errors/skips); real normal-profile 300s receipts, XML, preliminary runs and input/final boundary hashes are preserved under `logs/daily-trends-radar/codex-takeover1-20261104-31df241065e44d26b122a07de5756ef6/`. Production is unchanged from takeover entry. The prior independently reviewed 239 supplied/104 fresh cases, credited corrections and false-positive diagnostic remain historical evidence in re-review 3 and its original receipt directory; they are not overwritten or relabeled as this takeover's acceptance.

Independent takeover review 1 closes FR-05 and DTR-05/07 with its own fresh 21-case PostgreSQL execution (exit 0, zero failures/errors/skips, 33.594s) and inspection of correctly placed observations/negative control. Current source matches takeover final hashes; all 2,881 input files were compared, with only the authorized four-file takeover delta. Reviewer evidence: `logs/daily-trends-radar/codex-takeover-review1-20261104-1d4381ee792242ea8dad0c147d5c0afa/`. No source/test/history change or publishing by the reviewer.

## Codex remediation and final review

**`READY_FOR_OWNER_COMMIT`** under [independent takeover review 1](reviews/2026-11-04-dtr26-s1-independent-takeover-review-1.md). FR-01–07 CLOSED; DTR-01–12 verified within existing limits. L01 LOW remains OPEN/NONBLOCKING. Antigravity **`3/3`** remains consumed/exhausted; the one authorized Codex takeover is independently accepted and the correction loop is stopped. No agent publishes; no additional staging-review, phase-final or automatic next-slice gate for this accepted S1.

**Docs Sync Check — independent takeover review 1:** `UPDATED` — independent report and ACTIVE/contract acceptance, FR-05 closure, accepted takeover, unchanged counter and publication next step. `ALREADY_CONSISTENT` — current handback evidence, unchanged production/API/schema/dependency inputs, immutable reviews and package inventory; 21 fresh independent cases and 105 supplied final cases reconciled. The dated implementer handback's independent-review-pending statements are superseded by the independent decision. `NOT_VERIFIED` — eventual Owner publication only; no mandatory implementation gate remains unverified. `LOW_BACKLOG` — L01 remains OPEN/NONBLOCKING. `DEFERRED_OUT_OF_SCOPE` — historical routing/tooling notices, other LOW backlog and preexisting archive; no broad cleanup. Historical creation Docs Sync above retains its dated meaning.

**Next step:** Owner | commit/push the accepted DTR26-S1 delivery, then ChatGPT records publication closeout | [ACTIVE](../../handoffs/ACTIVE.md), this executable handoff and independent takeover review 1 | READY_FOR_OWNER_COMMIT; preserve unrelated preexisting work and record actual publication SHA afterward | Antigravity `3/3` consumed; one bounded Codex takeover independently accepted; loop STOPPED.
