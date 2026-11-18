# BF26-NR-B0 rev. 1 — G5-A HTTP evidence reuse

**Proposed scoped credit for B04/B05, pending independent review.** Audit SHA is `dde8fc86e820b8f9a329a65cfe590191e027ef4c`. The assertions below map 29 historical DP-A G5-A testcases to 25 groups in the [published HTTP matrix](b0-http-contract-matrix.csv). Inbox and combined radar already have some credit in the [B0 report](b0-audit-report.md). The additional cohort corroborates those assertions within its fixture.

Source locators use the following tracked tests:

- [Account](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/account/AccountWebIntegrationTest.java) and [Collection](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/collection/CollectionWebIntegrationTest.java)
- [Feed](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/FeedWebIntegrationTest.java), [HN Daily Trends](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/DailyTrendsWebIntegrationTest.java), [RSS Daily Trends](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/RssDailyTrendsWebIntegrationTest.java) and [Inbox](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/SavedResourceInboxWebIntegrationTest.java)

| Matrix group ID | Executed assertion proposed for reuse | Source locator |
| --- | --- | --- |
| `HTTP-post-accounts-private` | 201, numeric data.id and submitted username. | Account:33 |
| `HTTP-get-accounts-id-private` | 200 and submitted username on the created account. | Account:47 |
| `HTTP-put-accounts-id-private` | 200 and updated displayName. | Account:63 |
| `HTTP-post-accounts-owneraccountid-snapshots-private` | 201 and reportedTotalCount=1500 in the response. | Account:79 |
| `HTTP-get-accounts-owneraccountid-snapshots-private` | 200 and data array. | Account:87 |
| `HTTP-post-collection-music-private` | 201, numeric data.id and submitted title. | Collection:28 |
| `HTTP-get-collection-music-id-private` | 200 and submitted title. | Collection:41 |
| `HTTP-post-collection-shopping-private` | 201, numeric data.id and submitted name. | Collection:55 |
| `HTTP-get-collection-shopping-id-private` | 200 and submitted name. | Collection:68 |
| `HTTP-post-collection-software-private` | 201, numeric data.id and submitted name. | Collection:82 |
| `HTTP-get-collection-software-id-private` | 200 and submitted name. | Collection:95 |
| `HTTP-put-collection-music-id-credits-private` | 200 on both identical PUTs, each with JsonPath data.doesNotExist() (null or absent). | Collection:117,125 |
| `HTTP-get-collection-music-id-credits-private` | 200, single-element array, expected personId and SINGER role after those PUTs. | Collection:133 |
| `HTTP-post-feed-sources-private` | 201, numeric data.id and submitted name. | Feed:30 |
| `HTTP-get-feed-sources-sourceid-items-private` | 200 and data array for the created source. | Feed:44 |
| `HTTP-post-saved-resources-manual-private` | 201, numeric data.id and submitted title in the lifecycle. Inbox cases assert 201 and selected string round trips as described below. | Feed:60, Inbox:210,283,313,360 |
| `HTTP-get-saved-resources-id-private` | 200 and submitted title. | Feed:74 |
| `HTTP-post-saved-resources-id-convert-note-private` | 201, matching savedResourceId and numeric targetVaultEntryId. | Feed:87 |
| `HTTP-get-saved-resources-id-conversions-private` | 200 and data array. | Feed:96 |
| `HTTP-post-feed-sources-sourceid-refresh-private` | Mocked HN success: 200, matching sourceId, selectedTopIds/eligibleStories/distinctArticleUrls/processedItems all 1. Anonymous 401. maxItems 0/51/-1: 400 VALIDATION_ERROR. Disabled and RSS sources: 422 with SOURCE_DISABLED and UNSUPPORTED_SOURCE_TYPE respectively, both with null data, and exact disabled message. Missing source: 404 FEED_SOURCE_NOT_FOUND. | HN:100,114–132,160–178,209 |
| `HTTP-get-feed-radar-private` | 200, nonempty items, first item has the expected title, URL, HN signal type, story ID and discussion URL. Anonymous 401. Limits 0/101: 400 VALIDATION_ERROR. Disabled and RSS sources: 422 with SOURCE_DISABLED and UNSUPPORTED_SOURCE_TYPE respectively. | HN:104,139–150,181–192,220 |
| `HTTP-post-saved-resources-feed-item-private` | 201, numeric data.id, exact resource URL and title, matching feedItemId. | HN:245 |
| `HTTP-post-feed-sources-sourceid-refresh-rss-private` | Mocked RSS success: 200, matching sourceId, nonempty fetchedAt, parsedEntries/eligibleEntries/distinctArticleUrls/processedItems all 1, null error. Anonymous 401. Missing source: 404 FEED_SOURCE_NOT_FOUND. Disabled or non-RSS source: 422 with SOURCE_DISABLED or UNSUPPORTED_SOURCE_TYPE. Missing URL: 502 UPSTREAM_DESTINATION_REJECTED. Injected upstream failures: 503 UPSTREAM_UNAVAILABLE or 502 UPSTREAM_INVALID_RESPONSE. Selected synthetic markers are absent from the two sampled HTTP error bodies. | RSS:122,138,167–245 |
| `HTTP-get-feed-radar-combined-private` | 200, two items in the asserted order, titles and source/signal types match the seeded RSS/HN pair. Anonymous 401. Limits 0/101: 400 VALIDATION_ERROR. | RSS:126,252–263,293 |
| `HTTP-get-saved-resources-inbox-private` | See the Inbox assertions below. The HN lifecycle also asserts 200 and that the item filtered by the saved feedItemId returns the expected resource URL. | Inbox:34–381, HN:256 |

**Inbox assertions.** The test observes 401 for anonymous and invalid-token requests. The authenticated 200 response has a map at data, an array at data.items, and null error/meta. Limits 1/50/100 return 200 and limits 0/-5/101 return 400 VALIDATION_ERROR. A nonnumeric limit and unknown kind return 400 MALFORMED_REQUEST. The selected invalid cursors return 400 INVALID_ARGUMENT, with an exact generic message in the blank-cursor case. A syntactically valid edited tuple returns 200 with an array. The seeded pages return sizes 2 and 1 with a nonblank cursor followed by a null cursor. The empty REPOSITORY page has empty items and a null cursor. Literal inbox routing yields 200 with an array and null cursor. For synthetic strings, the tests assert the POST/GET title and URL round trip, the nested marker in the returned rawMetadata, and the supplied non-HTTP URL on POST and GET. The sampled bad-cursor response has an exact generic message and excludes the synthetic token/cursor. Captured application output also excludes those markers and the literal `SELECT ` prefix. Locators: Inbox:34–198,219–250,255–381.

**Limits.** Array-only checks allow empty results. This batch does not establish exact DTOs or envelope keysets, item identity or absence of duplication across the Inbox pages, a full persisted snapshot, universal redaction, credential transitions, browser rendering safety, or live upstream behavior. The HN/RSS clients are mocked where refresh is exercised and the sampled error assertions do not establish real destination enforcement or network egress. OS-level egress enforcement remains `NOT_VERIFIED`. Collection's Person create is setup only. Fiction/Film lifecycle testcases are outside this proposal because their exercised application/adapter paths have changed since DP-A and need a separate applicability check. The established B09 credit stands on its own witnesses.

## Applicability and provenance

The [DP-A independent final review](../2026-11-11-dual-login-profiles-preparation/reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md) qualified the cohort at source commit `5c1da4e5404ee3c724e3fc2e03569d4093bcb44d`. All six selected test sources match the historical raw hashes. Of 669 selected historical manifest entries for Account, Collection, Feed, Knowledge, People, Reference, shared support, resources and POM, 668 match. The exception is an out-of-batch Reference test class. Git comparison found no production changes in those modules or the common response classes. Changes in security add eight PUBLIC GET matchers outside the selected paths. Vault changes add trash browsing while leaving the entry creation and lookup operations used by these cases intact. The historical [shared Web fixture](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java) applies Spring Security and obtains PRIVATE bearer tokens from real persisted sessions. It uses PostgreSQL/pgvector and RAG recovery/reset dependencies. This is historical fixture metadata with no RAG execution or isolation credit from this follow-up. Tracked Backend content at preparation HEAD `2f7b2627bbbf560d1c3a30f31037417348a5da0d` matches the audit SHA.

Reconstructed argv matches receipt hash `36f47cef879b1b9f5ffd93a615ae90dbd2215fdce958d22f8ac3dfe6f76a30a9`. The gate exited 0 in 46.078 seconds with complete capture and cleanup. Stdout/stderr sizes match the receipt (66,247/0 bytes). The six XML files record 29 PASS testcases with zero failures/errors/skips from the 31-testcase gate. Count these as historical identities once, including where other addenda or cohorts cover the same assertions. The common source/evidence-manifest hashes are recorded in the [prior G5-B addendum](b0-g5-b-http-reused-evidence-addendum.md).

| Artifact under `logs/bf26-dp-a/codex-takeover-1794408167866608000/` | Raw SHA-256 |
| --- | --- |
| `g5-a.json` | `5706022f74f08add64e9b4e7c6e3866437cae14ed2c6768d0a688522e2050626` |
| `g5-a-reports-v2/TEST-com.vhvkhangg.personalprivatevault.account.AccountWebIntegrationTest.xml` | `c909d4936084bd66ea2f8f37bbc063eb55d9cd43e04fd7dc3e25bc188722042c` |
| `g5-a-reports-v2/TEST-com.vhvkhangg.personalprivatevault.collection.CollectionWebIntegrationTest.xml` | `07b4eb37fac6bfabbcbb4dad5f8c265b3b2d5b559ca972f05bd44b050b041172` |
| `g5-a-reports-v2/TEST-com.vhvkhangg.personalprivatevault.feed.FeedWebIntegrationTest.xml` | `d646a6a82c46b31d7efe5fc8cf1ee9fc548f155d8f46f20862fc4004b48f81f6` |
| `g5-a-reports-v2/TEST-com.vhvkhangg.personalprivatevault.feed.DailyTrendsWebIntegrationTest.xml` | `aec4820eb857806feba2161f46a6d3059a1fd45c0badd1576f44225705b51757` |
| `g5-a-reports-v2/TEST-com.vhvkhangg.personalprivatevault.feed.RssDailyTrendsWebIntegrationTest.xml` | `1961ff6fe11737ca95f9dccfed2355a7fa84c724050aa2a1589ebb0bd71703eb` |
| `g5-a-reports-v2/TEST-com.vhvkhangg.personalprivatevault.feed.SavedResourceInboxWebIntegrationTest.xml` | `08643d89d453814fb423748200fb3bec9b5e8b92b58d328927ccbe14a99c51f2` |

Docs Sync records one proposed addendum for independent evidence and document review. No new test invocation was used. Existing counters, published reports and earlier evidence dispositions stand. B04/B05 and B11 remain open. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 is unmet and B0-U1 remains `NOT_RUN`. A1 is deferred. A2/B1/X are outside the batch.
