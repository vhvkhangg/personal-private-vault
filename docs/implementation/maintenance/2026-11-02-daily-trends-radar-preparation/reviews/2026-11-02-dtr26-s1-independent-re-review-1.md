# Independent re-review 1 — DTR26-S1

- **Date / reviewer / mode:** 2026-11-02 / Codex / first independent correction re-review.
- **Authority:** [ACTIVE](../../../handoffs/ACTIVE.md) entered as `IMPLEMENTED_AWAITING_CODEX_REVIEW`; [issued handoff](../executable-handoff-2026-11-02.md), ARTICLE_URL decision and [initial finding register](2026-11-02-dtr26-s1-independent-final-review.md) govern.
- **Baseline:** local and live remote `main` verified at `974c8c26ae9efcd236f3e590a536527c5200b2bf`. Delivery remains uncommitted.
- **Disposition:** **`CHANGES_REQUESTED`**. **FR-03 and FR-06 CLOSED. FR-01 HIGH and FR-02/04/05/07 MEDIUM remain OPEN/BLOCKING.** No new finding IDs or product requirements.
- **Counter:** `1/3` correction rounds consumed; next correction `2/3`; takeover `NOT_AUTHORIZED`. No earlier UI26/CSA26/RAG counter changes.

## Reviewed correction and closures

The basic design remains appropriate: one Feed adapter, article identity, bounded cached preview, existing ingestion/save contracts and a short finalizer. Six production files changed since the initial submission, alongside relevant tests/docs. Unchanged source bodies and prior findings/evidence remain applicable; no framework, module, migration, dependency or new product scope is required.

| Finding | Independent disposition | Verified progress |
| --- | --- | --- |
| FR-01 HIGH | **OPEN, partially corrected** | DNS caller timeout, application/normalizer 50-item cap, remaining-budget checks and body-read deadline checks added. Absolute blocking-exchange cancellation and bounded resolver admission are still missing. |
| FR-02 MEDIUM | **OPEN, partially corrected** | Full-body byte accounting, single JSON root, fractional-ID rejection and wrong URL-type rejection fixed. Null boolean flags and malformed upstream charset handling still violate the issued validation/error contract. |
| FR-03 MEDIUM | **CLOSED** | Transition-address samples fixed; environment proxy checks added. Independent production-manager diagnostics prove validated peer selection and unsafe/mixed/rebinding/redirect denial; default TLS hostname verifier inspected, proxy environment denial exercised in an isolated child. |
| FR-04 MEDIUM | **OPEN, partially corrected** | Article URL policy is now reused for Radar; invalid links excluded after SQL cap. Negative/range/discussion checks added, but floating-point equality still promotes non-integral decimal provenance. |
| FR-05 MEDIUM | **OPEN / mandatory proof NOT_VERIFIED** | Ambient transaction rejection test and HN-local finalization error translation added. Required real PostgreSQL staging/connection/lock/timeout and identity/save sequence evidence remains incomplete. |
| FR-06 MEDIUM | **CLOSED** | `fetchStories` performs HTTP, parsing and representative selection before the injected clock is captured. The same captured instant reaches normalization, ingest and response. The new clock-advancement PostgreSQL test verifies post-fetch item/provenance/response values; unchanged ingest records the supplied instant on the source. |
| FR-07 MEDIUM | **OPEN, partially corrected** | Required status codes and accurate DTO examples/parameters added. Explicit error annotations use wildcard/raw ApiResponse schemas; current handback metrics/claims and some file/test descriptions remain inaccurate. |

### FR-03 closure evidence

`Round1Probe.java`, retained with its bounded receipt, uses the actual production builder/connection manager with numeric resolver fixtures and fake sockets; it never connects a real socket. Observations:

- Safe result: exactly one resolution/connection, original HN hostname, port 443 and the validated address offered to the connector.
- Unsafe and mixed results: `UPSTREAM_DESTINATION_REJECTED`, zero connector calls.
- Rebinding between separate closed connections: first request succeeds; the changed unsafe answer is denied before a second connection. There is one resolution for each attempted connection.
- Redirect response: rejected with `UPSTREAM_INVALID_RESPONSE`; only the original HN host was resolved/connected.
- The normal, unmodified client registry contains `SSLConnectionSocketFactory` with `DefaultHostnameVerifier`.
- A separate child JVM with a synthetic `HTTPS_PROXY` requirement rejects it as `UPSTREAM_UNAVAILABLE` before I/O. No proxy value/credential was exposed or actual environment changed.

Together with the updated policy tests, source inspection and retained initial security fixtures, this closes the original destination/connector requirement. It does not claim a live TLS handshake or provider smoke, neither of which is required.

## Remaining bounded corrections

### DTR26-FR-01 — HIGH / OPEN: blocking HTTP and resolver queue remain outside the bound

**Locations:** [HackerNewsClient](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/hackernews/HackerNewsClient.java), lines 174–238, 259–282 and 352–383; [refresh service](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshService.java), budget/finalizer path.

`exchangeDeadlineNanos` is calculated, but `httpClient.execute(request)` remains a synchronous blocking call without an absolute-deadline abort. Body checks run only after reads; the header/TLS/cleanup periods do not get an independent abort. The production-manager fake-socket diagnostic supplied response bytes every 5 ms, below inactivity timeout: a **200 ms** remaining deadline returned only after **1,467 ms**, once the body check finally detected expiry. The test is in-memory and demonstrates the existing slow-header case without any network connection. Restoring a body check does not establish the five-second whole-exchange or 70-second caller guarantee.

The DNS caller timeout is real, but its default `Executors.newFixedThreadPool(2, ...)` uses an unbounded queue, confirmed both by the [JDK 25 documentation](https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/util/concurrent/Executors.html#newFixedThreadPool(int,java.util.concurrent.ThreadFactory)) and actual `LinkedBlockingQueue` remaining capacity `2147483647`. Two native resolutions that ignore interruption can occupy both workers while subsequent timed-out/cancelled tasks accumulate. The interrupted-wait branch restores interruption but does not cancel its future. There is still no explicit interrupted/expired-work guard preventing finalization after cancellation.

**Required:** finish the already-issued absolute exchange/overall abort and bounded owned DNS/exchange admission/cleanup design. Reject capacity without growing queues/replacement workers; discard late results; preserve interrupt state and prevent late ingest. Check expiry on EOF/completion as well as positive reads. Add deterministic short-budget slow-header/blocked-I/O, interrupted wait, exhausted native-resolver capacity and no-late-publication cases using the existing low-I/O seam. Preserve native cancellation limitations and fixed budgets; no live/unsafe socket, generic job system or raised limit.

### DTR26-FR-02 — MEDIUM / OPEN: remaining typed-response validation failures

**Locations:** [HackerNewsNormalizer](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/hackernews/HackerNewsNormalizer.java), lines 169–192; `HackerNewsClient.validateHeaders`, lines 305–320.

New null exemptions accept `dead:null` and `deleted:null` as live eligible stories. Both were reproduced. The issued handoff explicitly requires boolean flags to be booleans when present; an entire null item remains a legitimate skip, but a null-valued flag is a different case. This exemption was not approved.

The malformed upstream header `Content-Type: application/json; charset=unsupported-charset-fixture` also escapes as `UnsupportedCharsetException`. An isolated client/handler diagnostic confirms it reaches the root illegal-argument mapping as **400**, rather than **502 / UPSTREAM_INVALID_RESPONSE**. The upstream response is being reported as a caller argument error.

**Required:** reject present nonboolean flags, including null, and translate malformed/unsupported upstream media/charset parsing into the existing safe upstream category. Add exact negative fixtures and assert failed refresh leaves committed items/source times unchanged. Keep the corrected full-body/single-root/integer/URL handling. These are remaining cases of the original typed-validation requirement, not new features.

### DTR26-FR-04 — MEDIUM / OPEN: decimal provenance still truncates into valid capture claims

**Location:** [FeedItemService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java), lines 244–257.

The new “integral” checks compare `Number.doubleValue()` with the already-truncated long/int value. Precision loss makes a non-integral value such as `BigDecimal("1.0000000000000000001")` compare equal to 1. The retained diagnostic supplies that value for story ID and rank with a matching discussion URL; Radar returns `HN_TOP_STORIES_AT_CAPTURE`, ID 1/rank 1, instead of falling back to `SOURCE_CACHE`.

**Required:** use exact, range-checked numeric validation/conversion, or conservatively reject unsupported numeric representations. Do not use double equality to establish integrality. Add near-integer decimal and overflow/range fixtures; malformed provenance must produce `SOURCE_CACHE` and all-null provenance. Keep the new shared article URL check and bounded filtering; no new API or normalization rule.

### DTR26-FR-05 — MEDIUM / OPEN / NOT_VERIFIED: real finalization evidence is still missing

**Locations:** [refresh concurrency tests](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/HackerNewsRefreshConcurrencyIntegrationTest.java) and [refresh integration tests](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/HackerNewsRefreshIntegrationTest.java).

The concurrency test file is byte-identical to the initial submission. Its disabled-source case still commits the disable before ingest begins, so the initial enabled check rejects before staging. Its description is not proof of disable-after-staged update/insert rollback. No new observation shows the same physical transaction/backend connection or an updater blocked after final validation until outer completion. Revised-URL/representative replacement and saved-snapshot-after-refresh sequences requested in the original finding are still absent from the new tests.

The new ambient-transaction test is credited. The new timeout translation test constructs a service with a mocked finalizer throwing `CannotAcquireLockException`; it proves classification, not a real effective PostgreSQL lock/statement timeout, complete rollback or commit-time proxy behavior. The added outer catch is in the correct owner-local place, including errors emitted by the proxied finalizer call.

**Required:** supply the specifically issued real-DB tests: disable after staging, shared backend/transaction identity, retained source lock through outer commit, bounded actual lock/statement failure with rollback and HTTP category, and the remaining new identity/save sequences. Use test-local hooks/barriers and finite waits. Preserve generic ingest, existing lock order, independent Owner edit and the new no-ambient-transaction guard. Do not replace these cases with another before-fetch disable or mock-only failure.

### DTR26-FR-07 — MEDIUM / OPEN: error schema and current evidence remain inaccurate

**Locations:** [FeedController](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/controller/FeedController.java), lines 107–142; [handback](../implementation-handback.md); current handoff status/result text.

Fresh generated OpenAPI now includes refresh 502/503 and Radar 422, but their content is `*/*` with `#/components/schemas/ApiResponse`. The issued contract requires the existing JSON error-envelope schema. The annotations select the raw generic class instead of `application/json` / `#/components/schemas/ErrorResponse`; new tests assert status presence without checking these schema references. Success response DTO references remain intact and the exact 233-route inventory passes.

Both the supplied receipt stdout and independent verification total **227** tests: 100 + 14 + 7 + 5 + 47 + 17 + 37. The handback/current acceptance claims say **145** and list incorrect per-stage counts. Its references still include a nonexistent `HackerNewsItemRecord.java`, a semaphore implementation absent from the unchanged admission class, and nonexistent method names. The executable handoff also entered with a round-1 header but stale initial-review/0-of-3 footer. Routine current status synchronization is performed here; the submitted handback remains unchanged as evidence.

**Required:** use controller-local annotations for the existing JSON ErrorResponse schema and assert the generated content/schema for new errors, preserving old routes/root configuration. Correct actual case names/files/counts and current acceptance statements in the authorized handback/API note after real fixes; do not claim unresolved gates verified. The API examples, parameters and camel-case metadata corrections are credited. Preserve historical reports/receipts and approved preparation history.

## Acceptance and evidence

| Case | Result | Limit / credited proof |
| --- | --- | --- |
| DTR-01 | PASS | Auth/preflight and type/disable-during-HTTP tests remain green; new ambient transaction rejection credited. |
| DTR-02 | PASS | Policy fixes plus actual manager peer/negative/redirect/TLS-configuration and proxy-environment diagnostics close FR-03. |
| DTR-03 | FAIL | Complete-body accounting improved; absolute deadline/queue and malformed response cases remain FR-01/02. |
| DTR-04 | FAIL | Grouping, caps, strict IDs and URL types pass; present null flags are still accepted, FR-02. |
| DTR-05 | NOT_VERIFIED in full | Repeat/legacy upsert and ambient rejection pass; required transaction identity and new identity sequences missing, FR-05. |
| DTR-06 | NOT_VERIFIED in full | Enabled-RSS rollback passes; disable-after-staging and actual timeout failure proof incomplete, FR-05. |
| DTR-07 | NOT_VERIFIED in full | Admission and independent-source progress pass; final-lock/timeout/interrupt proofs incomplete, FR-01/05. |
| DTR-08 | PASS for read window/query/link eligibility | SQL cap/source/time/order behavior and shared legacy URL filter tests pass. Provenance and error schema defects are DTR-09/11. |
| DTR-09 | FAIL | Post-fetch capture fixed, FR-06 closed; precision-based legacy provenance false claim remains, FR-04. |
| DTR-10 | NOT_VERIFIED in full | Save article/Inbox happy path and adjacent compatibility pass; requested refresh/save/refresh snapshot proof missing, FR-05. |
| DTR-11 | FAIL | 233 operations and new status presence pass; error media/schema contract still wrong, FR-07. |
| DTR-12 | FAIL | Scope/architecture pass; current evidence and mandatory acceptance claims require FR-07 correction. |

All seven focused commands independently ran once for the corrected source, through the normal **300s** watchdog using offline Maven, native-access flag and the handoff's explicit safe selectors. No full suite, provider smoke, unsafe destination, QA/model/worker or runtime download. Existing PostgreSQL image was verified local.

| Stage | Exit | Tests | Failures/errors/skips | Wall seconds |
| --- | --- | --- | --- | --- |
| provider-unit | 0 | 100 | 0/0/0 | 11.641 |
| refresh-postgres | 0 | 14 | 0/0/0 | 35.625 |
| radar-postgres | 0 | 7 | 0/0/0 | 26.579 |
| radar-http | 0 | 5 | 0/0/0 | 30.610 |
| feed-compatibility | 0 | 47 | 0/0/0 | 31.094 |
| web-inbox-openapi | 0 | 17 | 0/0/0 | 34.093 |
| architecture | 0 | 37 | 0/0/0 | 24.922 |
| **Total** | **all 0** | **227** | **0/0/0** | **194.564** |

Exact command shape: `python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <fresh path> -- mvn.cmd -o -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=<same seven explicit selector groups in the issued handoff> test`. Class XML totals were cross-checked against each final Maven stdout summary; short-name copies retained.

Reviewer evidence directory: ignored `logs/daily-trends-radar/codex-rereview1-20261102-12bc569f078a4a3cbed16d9fc41567fd/`. It contains seven receipts, `test-summary.json`, `xml/`, source hashes, `Round1Probe.java`/`probe-results.json`, proxy/charset diagnostics, and fresh `generated-openapi.json`. Diagnostic commands used 30/60/120-second normal watchdog limits, fake I/O or the existing isolated MockMvc/PostgreSQL test context. They establish observations, not new production/test implementation. No submitted source/test file was edited by the reviewer.

## Coverage, scope and Docs Sync Check

The initial full review remains source-applicable for unchanged paths. Changed source/tests/docs were re-inspected, actual Feed subtree recursively inventoried and generic ingestion/deduplication/source-edit bodies compared against HEAD. Feed remains 103 Java files / 25 packages, all source package-info present, no .gitkeep or new module/schema/POM/root configuration change. Relevant Java/Feed/HTTP/persistence/reuse/SOLID/pattern/testing/bounded-execution/coverage/documentation instructions remain applicable. Existing implementer/hook/skills suffice; no new agent or takeover.

| Dimensions | Status | Current evidence / limit |
| --- | --- | --- |
| 1 intent; 2 traceability | FINDING | Same ARTICLE_URL scope; five existing findings remain and handback overstates completion. |
| 3 architecture; 4 ownership; 5 package tree | PASS | 37 architecture cases; same Feed owner and complete tree inspection. Transaction proof remains separately blocked. |
| 6 shared contracts | FINDING | Root envelope is reused, but new error annotations select raw schema, FR-07; no common module needed. |
| 7 SOLID; 8 principles; 9 patterns | PASS | Concrete adapter/orchestrator/finalizer remain proportionate. Bounded resolver correction must not introduce a generic framework. |
| 10 domain; 11 API; 12 validation | FINDING | FR-02/04/07; strict roots/IDs and URL eligibility improved, error and exact-number cases remain. |
| 13 auth/privacy; 14 egress/secrets | PASS | Auth tests and actual connector/proxy diagnostics pass; metadata/link only, no article fetch or credential forwarding. |
| 15 transactions; 16 recovery | FINDING | FR-01/05. Basic source race/admission passes; real final-lock/timeout/cancellation proof incomplete. |
| 17 persistence | PASS for mappings/constraints/query | PostgreSQL cases, no schema change, unchanged generic ingestion; required transaction sequences remain FR-05. |
| 18 resource bounds | FINDING | FR-01: blocking phase and unbounded queue; byte/selection caps improved. |
| 19 data quality | FINDING | Capture time fixed; near-integer provenance still falsely promoted, FR-04. |
| 20 document/OCR; 24 model evaluation | N/A | Neither capability changed/enabled. No additional fidelity/evaluation gate. |
| 21 negatives; 22 test quality | FINDING | 227 passing cases and new diagnostics credited; required remaining cases are explicitly identified. |
| 23 benchmarks | NOT_VERIFIED | No benchmark/SLO requested. Concrete deadline/resource failure is FR-01, not a new benchmark project. |
| 25 dependencies; 26 build/tooling | PASS with limits | No install/upgrade; seven bounded offline stages pass. Existing JDK/Lombok/Byte Buddy diagnostics retained, no suppression or IDE-clean claim; IDE NOT_RUN. |
| 27 logging | PASS for inspected paths | Fixed safe errors, no body/credential logging added. Diagnostic fixtures are synthetic. |
| 28 docs/governance | FINDING | FR-07; supplied counts/claims/schema still conflict. This review/current status correct the disposition. |
| 29 hygiene/delivery | PASS | Only new review and two review-status files changed by Codex; implementation inputs/history preserved, no staging/publication. |

Graphify navigation reused the existing cache with a 500-token bounded query; its truncation and missing new symbols were not treated as source evidence. Canonical code and fresh inventory were used.

**Docs Sync — UPDATED:** this dated review and current ACTIVE/executable-handoff disposition, counters, result and next action. **ALREADY_CONSISTENT:** approved article identity, owner/module/schema boundaries, new DTO examples and scoped package inventory. **REQUIRES CORRECTION:** FR-07 handback/API error schema and claims, plus code/evidence for remaining findings. The reviewer leaves implementer submission/receipts and historical preparation/review reports untouched.

**DEFERRED_OUT_OF_SCOPE:** stale broad AGENTS/policy/phase notices and old implementer wording; ChatGPT owns separately authorized cleanup. UI26-FR-L02, CSA26-FR-L01, SAD-FR-L01, AUTH-CLOCK and prior backlog retain their statuses. P17 FROZEN, P18 CLOSED, S19-2 SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; indexing INACTIVE and live originals DISABLED/DEFERRED. No S2, scheduler, other provider, frontend, Drive or RAG permission follows.

**Next step:** Antigravity | `/antigravity-implement-handoff`, mandatory self-review, then independent Codex `$codex-final-review` | DTR26-S1 and this remaining finding register | correct FR-01/02/04/05/07 within issued scope/budgets with actual focused evidence; no publication | `1/3` consumed; next correction `2/3`; takeover `NOT_AUTHORIZED`.
