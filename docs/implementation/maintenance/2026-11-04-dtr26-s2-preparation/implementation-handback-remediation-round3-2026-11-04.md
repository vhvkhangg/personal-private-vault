# Implementation handback — Daily Trends Radar S2A (DTR26-S2A) — Remediation Round 3

- **Handoff ID:** `DTR26-S2A`
- **Current implementer:** Antigravity (backend-implementer)
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- **Date:** 2026-11-04 (Remediation Round 3)
- **Remediation counter:** `3/3 consumed; 0/3 remaining; takeover NOT_AUTHORIZED`
- **Independent acceptance:** `INDEPENDENT_REVIEW_REQUIRED` (Antigravity does not self-accept)
- **Target review:** Owner opens a fresh Codex session using `$codex-final-review`
- **Verified evidence directory (Round 3):** `logs/daily-trends-radar/dtr26-s2a-80228397e1b24e20a1e6810cc23f1f53/`
- **Preserved historical evidence (Round 2):** `logs/daily-trends-radar/dtr26-s2a-2754e33588f24e9c92c188bfd0035ee0/`
- **Preserved historical evidence (Round 1):** `logs/daily-trends-radar/dtr26-s2a-f0ccb04819e94a9f99c305d58151addd/`
- **Preserved historical evidence (Initial):** `logs/daily-trends-radar/dtr26-s2a-379d97fe1983494ab9a651d9aea4547f/`
- **Entry baseline:** verified main at `43ca772054cfc9c2546cae87b949bcc065a1066a`

---

## 1. Remediation Round 3 Resolution Ledger (FR-01, FR-06)

This section documents the specific remediations implemented to address all findings from [independent re-review 2](reviews/2026-11-04-dtr26-s2a-independent-re-review-2.md):

| Finding ID | Severity | Root Cause & Remediation Summary | Verification Evidence | Status |
| --- | --- | --- | --- | --- |
| **DTR26-S2A-FR-01** | **HIGH** | The destination predicate `isPublicIpv6Address` previously used a broad mask `(b0 & 0xF0) == 0x20` which admitted unallocated IANA spaces `2d00::/8`, `2e00::/7`, and did not check for the unallocated gap `2001:1000::/23`. Consequently, the recording connector was called once on `2e00::1` instead of denying the answer before connection.<br><br>**Remedy:** Replaced the mask with a pinned IANA-allocated public prefix allowlist strictly admitting only assigned Regional Internet Registry blocks: `2001::/16` (checked via `isAllocated2001Suballocation` which rejects protocol gap `2001:1000::/23`, documentation `2001:db8::/32`, and all non-AS112 protocol ranges), `2002::/16` (6to4 embedded IPv4), `2003::/16` (RIPE), `2400::/12` (APNIC), `2600::/12` (ARIN), `2610::/23` (ARIN), `2620::/23` (ARIN), `2800::/12` (LACNIC), `2a00::/12` (RIPE), `2c00::/12` (AFRINIC). All other ranges (including `2000::/16`, `2d00::/8`, `2e00::/7`, `3000::/4`, `4000::/3`, `fe80::/10`, `fc00::/7`, `ff00::/8`, `100::/64`, `::/128`) fail closed before connection. In `RssFeedClientTest`, added negative proofs verifying isolated and mixed DNS answers containing `2d00::1`, `2e00::1`, and `2001:1000::1` produce exactly zero connector calls. | Verified in `RssFeedDestinationPolicyTest` (79 tests) and `RssFeedClientTest` (20 tests). | **REMEDIATED (Implementer Verified)** |
| **DTR26-S2A-FR-02** | **MEDIUM** | Cookie caching, HTTP authentication, and DNS resolver budget propagation. | Closed in independent re-review 1. Preserved without regression. | **CLOSED (Independent Review)** |
| **DTR26-S2A-FR-03** | **MEDIUM** | RSS 2.0 single `<channel>` element requirement. | Closed in independent re-review 2. Preserved without regression. | **CLOSED (Independent Review)** |
| **DTR26-S2A-FR-04** | **MEDIUM** | Timestamp capture post-normalization completion. | Closed in independent re-review 2. Preserved without regression. | **CLOSED (Independent Review)** |
| **DTR26-S2A-FR-05** | **MEDIUM** | Strict calendar validation and 2-digit year base 1970. | Closed in independent re-review 1. Preserved without regression. | **CLOSED (Independent Review)** |
| **DTR26-S2A-FR-06** | **MEDIUM** | Mandatory evidence docket remainder (5 items):<br>1. **Actual TLS verification:** Tested Apache `SSLConnectionSocketFactory` invoking production `DefaultHostnameVerifier` on mock `SSLSocket` with certificate sessions. Mismatch denial (`CN=mismatch.com` against request host `example.com`) throws `SSLPeerUnverifiedException` and maps to `FeedRefreshException(UPSTREAM_UNAVAILABLE)`. Valid-host control (`CN=example.com`) passes verification without exception.<br>2. **Slow exchange & in-flight lifecycle:** Tested slow trickle abort (`SocketTimeoutException` during stream reading) translating to `UPSTREAM_UNAVAILABLE`. Tested in-flight thread interruption in `RssFeedClient.readEntityToEof` halting stream read immediately and throwing `UPSTREAM_UNAVAILABLE`. Tested `client.close()` shutting down owned `PolicyEnforcingDnsResolver` worker pool (`dnsResolver.isShutdown() == true`).<br>3. **Remaining exact bounds:** Added exact body size boundary: 1,048,576 bytes succeeds, 1,048,577 bytes throws `FETCH_LIMIT_EXCEEDED`. Added exact and one-over field bounds: title (1000 passes, 1001 ineligible), URL (2048 passes, 2049 ineligible), author (500 preserved, 501 omitted as null), originalId (500 preserved, 501 truncated to 500 in metadata). Added isolated text boundary: 1,048,576 UTF-16 code units passes, 1,048,577 code units fails with `FETCH_LIMIT_EXCEEDED`. Verified serialized Unicode/escaping envelope.<br>4. **Actual error/log markers:** Verified absence of synthetic credential tokens (`secret-token-xyz123`) and hostile XML markers (`EVIL_XXE_MARKER_999`) across all 3 channels: exception message (`ex.getMessage()`), captured logs (`OutputCaptureExtension`), and public HTTP error responses (`RssDailyTrendsWebIntegrationTest`).<br>5. **Accurate evidence mapping:** Removed all inaccurate type/method references; verified canonical exception types (`FeedRefreshException`, `FeedRefreshFailureReason`) and truthful dynamic PG connection identity (`pg_backend_pid()`, `pg_current_xact_id()`) descriptions. | Verified in `RssFeedClientTest`, `RssAtomParserTest`, `RssFeedNormalizerTest`, `RssFeedRefreshIntegrationTest`, and `RssDailyTrendsWebIntegrationTest`. | **REMEDIATED (Implementer Verified)** |

---

## 2. User-Visible Acceptance & Requirements Matrix (S2A-01 – S2A-12)

| Requirement ID | Specification / Scenario | Implementation & Verification Evidence | Status |
| --- | --- | --- | --- |
| **S2A-01** | Configure RSS source, then POST refresh; reject missing/disabled/wrong-type source, non-positive ID, absent/invalid bearer. | `FeedController.refreshRssFeedSource` enforces `@Positive`, Bearer security. `RssFeedRefreshService` validates preflight: `FeedSourceNotFoundException` (404), `SOURCE_DISABLED` (422), `UNSUPPORTED_SOURCE_TYPE` (422), `UPSTREAM_DESTINATION_REJECTED` (502 for missing/malformed feedUrl). Zero provider calls on preflight/auth failure. Verified in `RssDailyTrendsWebIntegrationTest`. | **PASS (Implementer Verified)** |
| **S2A-02** | Destination enforcement: public HTTPS host only, reject IP literals, non-public/private/link-local/multicast/loopback/reserved IP, mixed DNS answers, DNS rebinding, proxy, TLS mismatch. | `RssFeedDestinationPolicy` validates URI scheme (`https`), hostname, port (443/omitted), user-info, IP literals, and resolves addresses via `InetAddress.getAllByName`. All addresses must be public routable global unicast within pinned IANA allocations (excluding `2000::/16`, `2d00::/8`, `2e00::/7`, and unallocated gap `2001:1000::/23`). Explicitly denies `2d00::1`, `2e00::1`, and `2001:1000::1` before connection with zero connector calls. `RssFeedClient` binds `PolicyEnforcingDnsResolver` into Apache HttpClient 4.5.13 `PoolingHttpClientConnectionManager`. Production `DefaultHostnameVerifier` verified with mock TLS peer. Verified in `RssFeedDestinationPolicyTest` (79 tests) and `RssFeedClientTest` (20 tests). | **PASS (Implementer Verified)** |
| **S2A-03** | Fixed resource budgets: 1 GET, $\le$ 1 MiB body stream overrun, $\le$ 200 records, $\le$ 20k events, $\le$ 32 depth, $\le$ 1,048,576 UTF-16 text code units, $\le$ 50 selected URL groups. 25s overall refresh budget, 5s finalizer reservation, 10s exchange budget, 2s DNS wait. | `RssFeedClient` enforces streaming body count with immediate overrun abort (1,048,576 bytes passes, 1,048,577 bytes throws `FETCH_LIMIT_EXCEEDED`); `RssAtomParser` enforces exact text boundary (1,048,576 passes, 1,048,577 throws `FETCH_LIMIT_EXCEEDED`), depth (32 vs 33), records (200 vs 201), and event counters (20,000 vs 20,001); `RssFeedNormalizer` caps distinct article URL groups at 50 while parser continues to `END_DOCUMENT`. Bounded executor aborts on saturation. Verified in `RssFeedClientTest`, `RssAtomParserTest`, `RssFeedNormalizerTest`. | **PASS (Implementer Verified)** |
| **S2A-04** | Streaming XML parsing: JDK StAX, DTD/external entity disabled, DOCTYPE rejected, XInclude rejected, `rss version="2.0"` (with exactly 1 channel) and Atom `http://www.w3.org/2005/Atom` only; valid empty feed commits zero items. | `RssAtomParser` sets `SUPPORT_DTD=false`, `IS_SUPPORTING_EXTERNAL_ENTITIES=false`, custom `XMLResolver` failing closed, and explicit DOCTYPE rejection. Bare `<rss version="2.0"/>` without channel rejected with `UPSTREAM_INVALID_RESPONSE`. Valid empty channel produces 0 entries and updates source timestamps. Malformed tail after 50 valid groups rejected. Verified in `RssAtomParserTest` (31 tests). | **PASS (Implementer Verified)** |
| **S2A-05** | Canonical deduplication and dual-key item identity: `externalId = null` for all RSS items; deduplication by exact trimmed article URL per source. Ties broken deterministically (publishedAt DESC, title ASC). Same URL updates, new URL inserts. | `RssFeedNormalizer` normalizes entries, discards relative links, groups by exact trimmed URL, picks representative via comparator on full untruncated ID, and emits items with `externalId = null`. `FeedItemOperations.ingestFetch` performs upsert on `(source_id, url)`. Matched existing row clears legacy `externalId`. Same provenance ID with changed URL inserts a new row and retains old row. Verified in `RssFeedNormalizerTest` and `RssFeedRefreshIntegrationTest`. | **PASS (Implementer Verified)** |
| **S2A-06** | Date parsing & boundaries: RFC 822 / RFC 1123 / ISO 8601 with 2-to-4 digit year support and DOW leniency; missing/invalid dates stay null; future dates excluded; Radar filters inclusive `[now - 24h, now]`. | `RssFeedNormalizer` parses pubDate/published against multiple formatters with `ResolverStyle.STRICT`, base 1970 for 2-digit years, and day-of-week fallback; future timestamps skipped against single captured UTC batch clock. Combined Radar query filters `publishedAt >= :windowStart AND publishedAt <= :windowEnd`. Verified in `RssFeedNormalizerTest` and `CombinedFeedRadarIntegrationTest`. | **PASS (Implementer Verified)** |
| **S2A-07** | Authoritative finalization & transactional atomicity: staging items, locking source `findByIdForUpdate`, rechecking enabled/type/feedUrl in the same physical transaction. Staging rolls back on source change or lock timeout. | `RssFeedRefreshFinalizer` is annotated `@Transactional(REQUIRED, timeout = 5)`. Ingest and source update share dynamic PostgreSQL connection PID (`pg_backend_pid()`) and transaction ID (`pg_current_xact_id()`). Verified with `TransactionSynchronization.beforeCommit` in `RssFeedRefreshIntegrationTest`, and dynamic proxy negative control `separatelyCommittedRequiresNewIngestFailsSharedIdentityAssertion` proves identity assertion rejects unshared transactions. Lock contention & rollback verified in `RssFeedRefreshConcurrencyIntegrationTest`. | **PASS (Implementer Verified)** |
| **S2A-08** | Concurrency admission: shared singleton `HackerNewsRefreshAdmission` enforces max 2 concurrent refreshes across HN+RSS and same-source single-flight. Interruption/rejection releases permit. | `RssFeedRefreshService` acquires `admission.acquire(sourceId)` and releases in `finally`. Tested with concurrent threads in `RssFeedRefreshConcurrencyIntegrationTest` (9 tests). | **PASS (Implementer Verified)** |
| **S2A-09** | Combined cached Radar: `GET /api/v1/feed/radar/combined?limit=50` (1..100) queries enabled HN + RSS sources within rolling 24h window ordered `publishedAt DESC, id DESC`. Zero external network calls. RSS items map `signalType = "RSS_SOURCE_CACHE"`, `sourceRank/hnStoryId/hnDiscussionUrl = null`. | `FeedItemService.getCombinedRadar(limit)` delegates to `FeedItemRepository.findCombinedRadarCandidates`, rejects limit $\le$ 0 or $>$ 100 with `InvalidFeedItemException` (400), filters invalid URLs post-fetch, and maps DTOs via `FeedWebMapper`. Verified in `CombinedFeedRadarIntegrationTest` (5 tests) and `RssDailyTrendsWebIntegrationTest`. | **PASS (Implementer Verified)** |
| **S2A-10** | Compatibility & non-regression: existing HN manual refresh and HN-only Radar untouched. Save-to-Inbox, global search, and feed ingestion concurrency preserved without regression. | Existing HN classes, queries, and behaviors unmodified. Verified in `hn-compatibility` (126 tests), `feed-inbox-compatibility` (87 tests), and `architecture-search` (69 tests). | **PASS (Implementer Verified)** |
| **S2A-11** | Shared contracts, OpenAPI inventory, and zero architectural leakage: manifest extended from 233 to 235 routes; no POM edits, no Flyway migrations, no relative link resolution, no feed auth, no scheduler scaffolding. | OpenAPI route inventory asserts 235 routes in `OpenApiRouteInventoryIntegrationTest`. Feed package inventory documented in `docs/repository/repository-package-tree.md`. Architecture rules pass in `ApplicationArchitectureTests` (37 tests). | **PASS (Implementer Verified)** |
| **S2A-12** | Bounded command execution & receipts: all test stages executed via `scripts/run-bounded-command.py` with normal profile ($\le$ 300s timeout) producing clean JSON receipts. | Verified receipts stored in `logs/daily-trends-radar/dtr26-s2a-80228397e1b24e20a1e6810cc23f1f53/`. Total 524 tests executed across all 7 stages with 0 failures, 0 errors, 0 skipped. | **PASS (Implementer Verified)** |

---

## 3. Seven-Selection Gate Verification Receipts (Remediation Round 3)

All tests executed via `scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <path> -- mvn.cmd -ntp -f backend/pom.xml "-DargLine=--enable-native-access=ALL-UNNAMED" -Dtest=<selector> test`.

- **Run directory:** `logs/daily-trends-radar/dtr26-s2a-80228397e1b24e20a1e6810cc23f1f53/`
- **Total test count:** **524 tests run**, **0 failures, 0 errors, 0 skipped** across all 7 stages.
- **Exit codes:** All 7 stages exited with code `0`.

| Stage | Name / Selector | Exit | Duration (s) | Tests Run | Failures / Errors / Skipped | Receipt File |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | `rss-provider-unit`<br>`RssFeedDestinationPolicyTest,RssFeedClientTest,RssAtomParserTest,RssFeedNormalizerTest` | `0` | 8.047s | 163 | 0 / 0 / 0 | `rss-provider-unit.json` |
| 2 | `rss-refresh-postgres`<br>`RssFeedRefreshIntegrationTest,RssFeedRefreshConcurrencyIntegrationTest` | `0` | 30.094s | 24 | 0 / 0 / 0 | `rss-refresh-postgres.json` |
| 3 | `combined-radar-postgres`<br>`CombinedFeedRadarIntegrationTest,FeedRadarIntegrationTest` | `0` | 30.078s | 13 | 0 / 0 / 0 | `combined-radar-postgres.json` |
| 4 | `hn-compatibility`<br>`HackerNewsClientTest,HackerNewsDestinationPolicyTest,HackerNewsNormalizerTest,HackerNewsRefreshAdmissionTest,HackerNewsRefreshIntegrationTest,HackerNewsRefreshConcurrencyIntegrationTest` | `0` | 33.578s | 126 | 0 / 0 / 0 | `hn-compatibility.json` |
| 5 | `feed-inbox-compatibility`<br>`FeedIntegrationTest,FeedValidationTest,FeedIngestionConcurrencyTest,SavedResourceInboxIntegrationTest,SavedResourceInboxCursorTest` | `0` | 36.078s | 87 | 0 / 0 / 0 | `feed-inbox-compatibility.json` |
| 6 | `web-openapi`<br>`RssDailyTrendsWebIntegrationTest,DailyTrendsWebIntegrationTest,FeedWebIntegrationTest,SavedResourceInboxWebIntegrationTest,OpenApiRouteInventoryIntegrationTest,ApiResponseEnvelopeAndErrorIntegrationTest` | `0` | 46.093s | 42 | 0 / 0 / 0 | `web-openapi.json` |
| 7 | `architecture-search`<br>`ApplicationArchitectureTests,GlobalSearchIntegrationTest,SearchWebIntegrationTest` | `0` | 50.656s | 69 | 0 / 0 / 0 | `architecture-search.json` |
| **TOTAL** | **Full 7-Selection Gate** | **0** | **234.624s** | **524** | **0 / 0 / 0** | **All receipts present** |

---

## 4. Documentation Synchronization Check (Docs Sync Check)

Per `docs/workflow/documentation-synchronization.md`:

| Category | Disposition & Scope |
| --- | --- |
| **UPDATED** | `docs/implementation/handoffs/ACTIVE.md` top-level status, banner, result block, remediation counter (`3/3 consumed; 0/3 remaining; takeover NOT_AUTHORIZED`), and Next step routed to `$codex-final-review`. `docs/implementation/maintenance/2026-11-04-dtr26-s2-preparation/executable-handoff-2026-11-04.md` status, banner, and counter synchronized. Additive handback `docs/implementation/maintenance/2026-11-04-dtr26-s2-preparation/implementation-handback-remediation-round3-2026-11-04.md` created. |
| **ALREADY_CONSISTENT** | `AGENTS.md` owner policies, `docs/repository/repository-package-tree.md` feed tree, OpenAPI 235 routes manifest, Flyway migrations 1..24, and module boundaries. |
| **DEFERRED_OUT_OF_SCOPE** | DTR26-S2B (background scheduler / workers), UI26-FR-L02, SAD-FR-L01, CSA26-FR-L01, AUTH-CLOCK/older backlog items. |
| **BLOCKED_OWNER_DECISION** | None. |
| **NOT_VERIFIED** | Independent review acceptance (reserved for independent Codex `$codex-final-review`). |

---

## 5. Modified & Created Artifacts Summary

### Production Code Modified
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedDestinationPolicy.java` (pinned IANA public prefix allowlist, explicit suballocation check for `2001::/16` rejecting `2001:1000::/23`, explicit rejection of `2d00::/8`, `2e00::/7`)
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedClient.java` (thread interruption during stream reading, `getDnsResolver()` accessor, `isShutdown()` on resolver pool)

### Test Code Modified / Created
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedDestinationPolicyTest.java` (added tests for `2d00::1`, `2e00::1`, `2001:1000::1`, and public allocation controls)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedClientTest.java` (exact 1,048,576 vs 1,048,577 body boundary, slow trickle abort, thread interruption during stream read, client close and resolver pool shutdown, zero connector calls for `2d00::1`, `2e00::1`, `2001:1000::1`, actual production `DefaultHostnameVerifier` inside `SSLConnectionSocketFactory` mismatch vs valid-host control)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssAtomParserTest.java` (isolated text boundary 1,048,576 vs 1,048,577 code units)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedNormalizerTest.java` (exact and one-over bounds: title 1000 vs 1001, url 2048 vs 2049, author 500 vs 501, origId 500 vs 501; serialized Unicode/escaping envelope)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/RssFeedRefreshIntegrationTest.java` (`OutputCaptureExtension` verifying absence of synthetic credential tokens and hostile XML markers from exception messages and captured logs)
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/RssDailyTrendsWebIntegrationTest.java` (verifying public HTTP error responses do not leak credential tokens or hostile XML markers)

---

**Next step:** Codex | $codex-final-review | DTR26-S2A handoff and round 3 handback | independent review of remediation round 3 | 3/3 consumed; 0/3 remaining; takeover NOT_AUTHORIZED.
