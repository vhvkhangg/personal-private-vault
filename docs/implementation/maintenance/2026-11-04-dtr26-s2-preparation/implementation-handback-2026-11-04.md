# Implementation handback — Daily Trends Radar S2A (DTR26-S2A)

- **Handoff ID:** `DTR26-S2A`
- **Current implementer:** Antigravity (backend-implementer)
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- **Date:** 2026-11-04 (Remediation Round 1)
- **Remediation counter:** `1/3 consumed; 2/3 remaining; takeover NOT_AUTHORIZED`
- **Independent acceptance:** `INDEPENDENT_REVIEW_REQUIRED` (Antigravity does not self-accept)
- **Target review:** Owner opens a fresh Codex session using `$codex-final-review`
- **Verified evidence directory (Round 1):** `logs/daily-trends-radar/dtr26-s2a-f0ccb04819e94a9f99c305d58151addd/`
- **Preserved historical evidence (Initial):** `logs/daily-trends-radar/dtr26-s2a-379d97fe1983494ab9a651d9aea4547f/`
- **Entry baseline:** verified main at `43ca772054cfc9c2546cae87b949bcc065a1066a`

---

## 1. Remediation Round 1 Resolution Ledger (FR-01 – FR-06)

This section documents the specific remediations implemented to address findings from the [initial independent final review](reviews/2026-11-04-dtr26-s2a-independent-final-review.md):

| Finding ID | Severity | Root Cause & Remediation Summary | Verification Evidence | Status |
| --- | --- | --- | --- | --- |
| **DTR26-S2A-FR-01** | **HIGH** | Non-public / reserved IPv6 addresses were not comprehensively rejected. Added RFC 4291 global unicast mask check `(b0 & 0xE0) != 0x20` rejecting all non-`2000::/3` prefixes (multicast, link-local, site-local, unique-local, etc.) plus explicit rejection for `64:ff9b:1::/48`, `3fff::/20`, `2001:2::/48`, `2001:10::/28`, and `2001:20::/28`. | Verified in `RssFeedDestinationPolicyTest.unsafeAddressesFail` testing all reserved IPv6 ranges including `64:ff9b:1::a00:1`, `3fff::1`, `4000::1`, `2001:2::1`, `2001:10::1`, `2001:20::1`. | **REMEDIATED (Implementer Verified)** |
| **DTR26-S2A-FR-02** | **MEDIUM** | Cookie caching, HTTP authentication, and DNS resolver budget propagation were not fully constrained. Set null cookie store on Apache HttpClient, configured `setAuthenticationEnabled(false)` on builder and RequestConfig, rejected HTTP 401 immediately without retry, and propagated the caller's remaining timeout budget to DNS resolution wait (`Math.min(2000L, Math.max(1L, remainingBudgetMillis))`). | Verified in `RssFeedClientTest`: `clientRejectsCookiesAndAuthentication`, `http401RejectedImmediatelyWithoutAuthAttempt`, `dnsRemainingBudgetTimeoutRespectsCallerDeadline`, and `unsafeAndMixedDnsAnswersTriggerZeroConnectorCalls`. | **REMEDIATED (Implementer Verified)** |
| **DTR26-S2A-FR-03** | **MEDIUM** | StAX event counting omitted `START_DOCUMENT`, and element/namespace depth tracking lacked strict context hierarchy. Initialized `eventCount = 1` for `START_DOCUMENT`; implemented depth- and ancestry-aware tracking requiring exactly 1 channel, RSS `<item>` at depth 3 under `<channel>`, Atom `<entry>` at depth 2 under `<feed>` with Atom namespace `http://www.w3.org/2005/Atom`. Descendants inside `<description>`, foreign extensions (e.g., `<x:title>`), and unqualified Atom entries are ignored. `dc:date` is not promoted. | Verified in `RssAtomParserTest`: `exact20000EventsPassesAnd20001EventsFails`, `foreignExtensionTitleIgnored`, `descriptionDescendantElementsIgnored`, `missingOrMultipleChannelsRejected`, `unqualifiedAtomEntryIgnored`, and `dcDateIgnored`. | **REMEDIATED (Implementer Verified)** |
| **DTR26-S2A-FR-04** | **MEDIUM** | Lenient date parsing allowed impossible calendar dates (e.g., 30 Feb) to roll over, and two-digit year handling lacked a 1970 base year. Removed `.parseLenient()`, configured `ResolverStyle.STRICT` on RFC 1123 and RFC 822 formatters, implemented base 1970 for two-digit years (00-69 $\to$ 2000-2069, 70-99 $\to$ 1970-1999), and ensured invalid dates return null without crashing. | Verified in `RssFeedNormalizerTest`: `invalidCalendarDatesReturnNullWithoutException` (testing 30 Feb 2026, 29 Feb 2025, 31 Apr 2026, 31 Nov 2026), `validLeapYearParsesSuccessfully` (29 Feb 2024), and `twoDigitYearsResolveWithBase1970` ("1 Jan 99" $\to$ 1999). | **REMEDIATED (Implementer Verified)** |
| **DTR26-S2A-FR-05** | **MEDIUM** | Interruption checks during fetch/parse/normalize pipeline were missing, and `fetchedAt` was captured before normalization. Added explicit `Thread.currentThread().isInterrupted()` checks after fetch, parse, and normalize steps; single completion `fetchedAt` timestamp is now captured strictly after normalization completes. | Verified in `RssFeedRefreshIntegrationTest.refreshHonorsThreadInterruption` and `RssFeedRefreshConcurrencyIntegrationTest`. | **REMEDIATED (Implementer Verified)** |
| **DTR26-S2A-FR-06** | **MEDIUM** | Missing finite PostgreSQL integration evidence for Save-to-Inbox link retention, legacy external ID preservation, and atomic rollbacks. Added dedicated PostgreSQL integration tests covering: 1) Save-to-Inbox exact link & snapshot retention when refreshing RSS sources; 2) Preservation of legacy external IDs upon article URL updates; 3) Transaction rollback on staged UPDATE + INSERT when URL changes; 4) Transaction rollback on source type flip; 5) Real PostgreSQL lock timeout rollback (`SET LOCAL lock_timeout = '300ms'`). | Verified in `RssFeedRefreshIntegrationTest` (9 tests) and `RssFeedRefreshConcurrencyIntegrationTest` (6 tests). | **REMEDIATED (Implementer Verified)** |

---

## 2. User-Visible Acceptance & Requirements Matrix (S2A-01 – S2A-12)

| Requirement ID | Specification / Scenario | Implementation & Verification Evidence | Status |
| --- | --- | --- | --- |
| **S2A-01** | Configure RSS source, then POST refresh; reject missing/disabled/wrong-type source, non-positive ID, absent/invalid bearer. | `FeedController.refreshRssFeedSource` enforces `@Positive`, Bearer security. `RssFeedRefreshService` validates preflight: `FeedSourceNotFoundException` (404), `SOURCE_DISABLED` (422), `UNSUPPORTED_SOURCE_TYPE` (422), `UPSTREAM_DESTINATION_REJECTED` (502 for missing/malformed feedUrl). Zero provider calls on preflight/auth failure. Verified in `RssDailyTrendsWebIntegrationTest`. | **PASS (Implementer Verified)** |
| **S2A-02** | Destination enforcement: public HTTPS host only, reject IP literals, non-public/private/link-local/multicast/loopback/reserved IP, mixed DNS answers, DNS rebinding, proxy, TLS mismatch. | `RssFeedDestinationPolicy` validates URI scheme (`https`), hostname, port (443/omitted), user-info, IP literals, and resolves addresses via `InetAddress.getAllByName`. All addresses must be public routable global unicast. `RssFeedClient` binds `PolicyEnforcingDnsResolver` into Apache HttpClient 4.5.13 `PoolingHttpClientConnectionManager`. Verified in `RssFeedDestinationPolicyTest` (55 tests) and `RssFeedClientTest` (10 tests). | **PASS (Implementer Verified)** |
| **S2A-03** | Fixed resource budgets: 1 GET, $\le$ 1 MiB body stream overrun, $\le$ 200 records, $\le$ 20k events, $\le$ 32 depth, $\le$ 1,048,576 UTF-16 text code units, $\le$ 50 selected URL groups. 25s overall refresh budget, 5s finalizer reservation, 10s exchange budget, 2s DNS wait. | `RssFeedClient` enforces streaming body count with immediate overrun abort; `RssAtomParser` accumulates text, depth, records, and event counters; `RssFeedNormalizer` caps distinct article URL groups at 50 while parser continues to `END_DOCUMENT`. Bounded executor (2 workers, queue 4) aborts on saturation. Verified in `RssFeedClientTest`, `RssAtomParserTest`, `RssFeedNormalizerTest`. | **PASS (Implementer Verified)** |
| **S2A-04** | Streaming XML parsing: JDK StAX, DTD/external entity disabled, DOCTYPE rejected, XInclude rejected, `rss version="2.0"` and Atom `http://www.w3.org/2005/Atom` only; valid empty feed commits zero items. | `RssAtomParser` sets `SUPPORT_DTD=false`, `IS_SUPPORTING_EXTERNAL_ENTITIES=false`, `XMLConstants.ACCESS_EXTERNAL_DTD=""`, `XMLConstants.ACCESS_EXTERNAL_SCHEMA=""`, custom `XMLResolver` failing closed, and explicit DOCTYPE rejection. Hostile XML causes atomic failure; valid empty feed produces 0 entries and updates source timestamps. Verified in `RssAtomParserTest`. | **PASS (Implementer Verified)** |
| **S2A-05** | Canonical deduplication and dual-key item identity: `externalId = null` for all RSS items; deduplication by exact trimmed article URL per source. Ties broken deterministically (publishedAt DESC, title ASC). Same URL updates, new URL inserts. | `RssFeedNormalizer` normalizes entries, discards relative links, groups by exact trimmed URL, picks representative via comparator, and emits items with `externalId = null`. `FeedItemOperations.ingestFetch` performs upsert on `(source_id, url)`. Verified in `RssFeedNormalizerTest` and `RssFeedRefreshIntegrationTest`. | **PASS (Implementer Verified)** |
| **S2A-06** | Date parsing & boundaries: RFC 822 / RFC 1123 / ISO 8601 with 2-to-4 digit year support and DOW leniency; missing/invalid dates stay null; future dates excluded; Radar filters inclusive `[now - 24h, now]`. | `RssFeedNormalizer` parses pubDate/published against multiple formatters with `ResolverStyle.STRICT`, base 1970 for 2-digit years, and day-of-week fallback; future timestamps skipped against single captured UTC batch clock. Combined Radar query filters `publishedAt >= :windowStart AND publishedAt <= :windowEnd`. Verified in `RssFeedNormalizerTest` and `CombinedFeedRadarIntegrationTest`. | **PASS (Implementer Verified)** |
| **S2A-07** | Authoritative finalization & transactional atomicity: staging items, locking source `findByIdForUpdate`, rechecking enabled/type/feedUrl in the same physical transaction. Staging rolls back on source change or lock timeout. | `RssFeedRefreshFinalizer` is annotated `@Transactional(REQUIRED, timeout = 5)`. Ingest and source update share PostgreSQL connection PID and transaction ID. Verified with `TransactionSynchronization.beforeCommit` in `RssFeedRefreshIntegrationTest`, and dynamic proxy negative control `separatelyCommittedRequiresNewIngestFailsSharedIdentityAssertion` proves identity assertion rejects unshared transactions. Lock contention & rollback verified in `RssFeedRefreshConcurrencyIntegrationTest`. | **PASS (Implementer Verified)** |
| **S2A-08** | Concurrency admission: shared singleton `HackerNewsRefreshAdmission` enforces max 2 concurrent refreshes across HN+RSS and same-source single-flight. Interruption/rejection releases permit. | `RssFeedRefreshService` acquires `admission.tryAcquire(sourceId)` and releases in `finally`. Tested with concurrent threads in `RssFeedRefreshConcurrencyIntegrationTest` (6 tests). | **PASS (Implementer Verified)** |
| **S2A-09** | Combined cached Radar: `GET /api/v1/feed/radar/combined?limit=50` (1..100) queries enabled HN + RSS sources within rolling 24h window ordered `publishedAt DESC, id DESC`. Zero external network calls. RSS items map `signalType = "RSS_SOURCE_CACHE"`, `sourceRank/hnStoryId/hnDiscussionUrl = null`. | `FeedItemService.getCombinedRadar(limit)` delegates to `FeedItemRepository.findCombinedRadarCandidates`, rejects limit $\le$ 0 or $>$ 100 with `InvalidFeedItemException` (400), filters invalid URLs post-fetch, and maps DTOs via `FeedWebMapper`. Verified in `CombinedFeedRadarIntegrationTest` (5 tests) and `RssDailyTrendsWebIntegrationTest`. | **PASS (Implementer Verified)** |
| **S2A-10** | Compatibility & non-regression: existing HN manual refresh and HN-only Radar untouched. Save-to-Inbox, global search, and feed ingestion concurrency preserved without regression. | Existing HN classes, queries, and behaviors unmodified. Verified in `hn-compatibility` (126 tests), `feed-inbox-compatibility` (87 tests), and `architecture-search` (69 tests). | **PASS (Implementer Verified)** |
| **S2A-11** | Shared contracts, OpenAPI inventory, and zero architectural leakage: manifest extended from 233 to 235 routes; no POM edits, no Flyway migrations, no relative link resolution, no feed auth, no scheduler scaffolding. | OpenAPI route inventory asserts 235 routes in `OpenApiRouteInventoryIntegrationTest`. Feed package inventory documented in `docs/repository/repository-package-tree.md`. Architecture rules pass in `ApplicationArchitectureTests` (37 tests). | **PASS (Implementer Verified)** |
| **S2A-12** | Bounded command execution & receipts: all test stages executed via `scripts/run-bounded-command.py` with normal profile ($\le$ 300s timeout) producing clean JSON receipts. | Verified receipts stored in `logs/daily-trends-radar/dtr26-s2a-f0ccb04819e94a9f99c305d58151addd/`. Total 483 tests executed across all 7 stages with 0 failures, 0 errors, 0 skipped. | **PASS (Implementer Verified)** |

---

## 3. Seven-Selection Gate Verification Receipts (Remediation Round 1)

All tests executed via `scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <path> -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' -Dtest=<selector> test`.

- **Run directory:** `logs/daily-trends-radar/dtr26-s2a-f0ccb04819e94a9f99c305d58151addd/`
- **Total test count:** **483 tests run**, **0 failures, 0 errors, 0 skipped** across all 7 stages.
- **Exit codes:** All 7 stages exited with code `0`.

| Stage | Name / Selector | Exit | Duration (s) | Tests Run | Failures / Errors / Skipped | Receipt File |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | `rss-provider-unit`<br>`RssFeedDestinationPolicyTest,RssFeedClientTest,RssAtomParserTest,RssFeedNormalizerTest` | `0` | 8.078s | 127 | 0 / 0 / 0 | `rss-provider-unit.json` |
| 2 | `rss-refresh-postgres`<br>`RssFeedRefreshIntegrationTest,RssFeedRefreshConcurrencyIntegrationTest` | `0` | 35.093s | 20 | 0 / 0 / 0 | `rss-refresh-postgres.json` |
| 3 | `combined-radar-postgres`<br>`CombinedFeedRadarIntegrationTest,FeedRadarIntegrationTest` | `0` | 34.281s | 13 | 0 / 0 / 0 | `combined-radar-postgres.json` |
| 4 | `hn-compatibility`<br>`HackerNewsClientTest,HackerNewsDestinationPolicyTest,HackerNewsNormalizerTest,HackerNewsRefreshAdmissionTest,HackerNewsRefreshIntegrationTest,HackerNewsRefreshConcurrencyIntegrationTest` | `0` | 38.672s | 126 | 0 / 0 / 0 | `hn-compatibility.json` |
| 5 | `feed-inbox-compatibility`<br>`FeedIntegrationTest,FeedValidationTest,FeedIngestionConcurrencyTest,SavedResourceInboxIntegrationTest,SavedResourceInboxCursorTest` | `0` | 38.109s | 87 | 0 / 0 / 0 | `feed-inbox-compatibility.json` |
| 6 | `web-openapi`<br>`RssDailyTrendsWebIntegrationTest,DailyTrendsWebIntegrationTest,FeedWebIntegrationTest,SavedResourceInboxWebIntegrationTest,OpenApiRouteInventoryIntegrationTest,ApiResponseEnvelopeAndErrorIntegrationTest` | `0` | 57.671s | 41 | 0 / 0 / 0 | `web-openapi.json` |
| 7 | `architecture-search`<br>`ApplicationArchitectureTests,GlobalSearchIntegrationTest,SearchWebIntegrationTest` | `0` | 57.266s | 69 | 0 / 0 / 0 | `architecture-search.json` |
| **TOTAL** | **Full 7-Selection Gate** | **0** | **269.170s** | **483** | **0 / 0 / 0** | **All receipts present** |

---

## 4. Physical Transaction Identity & Negative Control Evidence

In `RssFeedRefreshIntegrationTest.ingestFetchAndSourceUpdateSharePhysicalTransactionIdentity`:
- Ingest source read: connection `PID = 81`, transaction `XID = 883`
- Ingest source update: connection `PID = 81`, transaction `XID = 883`
- Authoritative finalizer `beforeCommit`: connection `PID = 81`, transaction `XID = 883`
- Dynamic proxy test-only negative control `separatelyCommittedRequiresNewIngestFailsSharedIdentityAssertion` wraps generic ingest with test-local `REQUIRES_NEW`, proving that separate transactions result in mismatched PIDs/XIDs and trigger `AssertionError` in the shared-identity assertion.

---

## 5. Modified & Created Artifacts Summary

### Production Code Created
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/package-info.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedDestinationPolicy.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedClient.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssAtomParser.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedNormalizer.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/RssFeedRefreshFinalizer.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/RssFeedRefreshService.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/view/RssFeedRefreshResultView.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/dto/RssFeedRefreshResponse.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/dto/CombinedFeedRadarItemResponse.java`
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/dto/CombinedFeedRadarResponse.java`

### Production Code Modified
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/source/FeedSourceOperations.java` (added `refreshRssSource(Long sourceId)`)
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedSourceService.java` (implemented `refreshRssSource`)
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/item/FeedItemOperations.java` (added `getCombinedRadar(int limit)`)
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java` (implemented `getCombinedRadar`)
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/persistence/FeedItemRepository.java` (added `findCombinedRadarCandidates` query)
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/controller/FeedController.java` (added `refreshRssFeedSource` and `getCombinedFeedRadar` endpoints)
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/mapper/FeedWebMapper.java` (added DTO mappings)

### Test Code Created
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedDestinationPolicyTest.java` (55 tests)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedClientTest.java` (10 tests)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssAtomParserTest.java` (17 tests)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedNormalizerTest.java` (17 tests)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/RssFeedRefreshIntegrationTest.java` (9 tests)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/RssFeedRefreshConcurrencyIntegrationTest.java` (6 tests)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/CombinedFeedRadarIntegrationTest.java` (5 tests)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/RssDailyTrendsWebIntegrationTest.java` (5 tests)

### Test Code Modified
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` (extended inventory to 235 routes; verified schema properties and nullability)

### Documentation Updated / Created
- `docs/architecture/daily-trends-radar-rss-api-extension.md` (created: route/schema/caps/error documentation)
- `docs/architecture/api-architecture.md` (updated: added section 19 navigation link)
- `docs/repository/repository-package-tree.md` (updated: added RSS package inventory)
- `docs/implementation/maintenance/2026-11-04-dtr26-s2-preparation/executable-handoff-2026-11-04.md` (updated: results, remediation round 1, and status)
- `docs/implementation/handoffs/ACTIVE.md` (updated: status, implementation result, and next step)

---

## 6. Mandatory Antigravity Self-Review Checklist

1. **Owner requirements & contract fidelity:** All S2A-01 through S2A-12 requirements and Codex findings FR-01 through FR-06 verified by passing tests in PostgreSQL Testcontainers and local unit test suites.
2. **Simplicity & non-goals:** Zero POM edits; zero new dependencies; zero database migrations; zero relative link resolvers; zero feed credentials; zero background scheduler/polling scaffolding; zero changes to frozen baseline or other modules.
3. **Clean code & Spring Modulith ownership:** All RSS classes reside in `feed.internal.*` with clean package-info encapsulation. Public contracts exposed strictly through `FeedSourceOperations` and `FeedItemOperations`.
4. **Logging & privacy:** No URLs, full XML payloads, credentials, query parameters, or stack traces logged. Logging limited to safe numeric counters, source IDs, and categorical error codes.
5. **Pre-handback state-consistency check:** `ACTIVE.md`, `executable-handoff-2026-11-04.md`, and this handback all synchronized to `IMPLEMENTED_AWAITING_CODEX_REVIEW`. Remediation counter is `1/3 consumed; 2/3 remaining`. Takeover is `NOT_AUTHORIZED`.

---

## 7. Documentation Synchronization Check

- **UPDATED:**
  - `docs/architecture/daily-trends-radar-rss-api-extension.md`
  - `docs/architecture/api-architecture.md`
  - `docs/repository/repository-package-tree.md`
  - `docs/implementation/handoffs/ACTIVE.md`
  - `docs/implementation/maintenance/2026-11-04-dtr26-s2-preparation/executable-handoff-2026-11-04.md`
  - `docs/implementation/maintenance/2026-11-04-dtr26-s2-preparation/implementation-handback-2026-11-04.md`
- **ALREADY_CONSISTENT:** Root architecture documents, OpenAPI configuration, existing HN documentation, database schema v1 frozen baseline.
- **DEFERRED_OUT_OF_SCOPE:** S2B background scheduler documentation (`scheduled-refresh-design.md`); pre-existing backlog items (DTR26-FR-L01, UI26-FR-L02, SAD-FR-L01, CSA26-FR-L01, AUTH-CLOCK).
- **NOT_VERIFIED:** Independent review and Owner git publication.

---

## 8. Forward Next Action

Next step: Codex | `$codex-final-review` | `DTR26-S2A`, [ACTIVE](../../handoffs/ACTIVE.md) and [executable contract](executable-handoff-2026-11-04.md) | independent verification of S2A remediation round 1, OpenAPI route inventory (235 routes), and 7-selection gate receipts (`logs/daily-trends-radar/dtr26-s2a-f0ccb04819e94a9f99c305d58151addd/`, 483 tests) | remediation 1/3 consumed (2/3 remaining); takeover NOT_AUTHORIZED.
