# Independent re-review 2 — DTR26-S1

- **Date / reviewer / mode:** 2026-11-02 / Codex / second correction re-review.
- **Authority:** [ACTIVE](../../../handoffs/ACTIVE.md) entered as `IMPLEMENTED_AWAITING_CODEX_REVIEW`; [issued handoff](../executable-handoff-2026-11-02.md) and [re-review 1](2026-11-02-dtr26-s1-independent-re-review-1.md) govern.
- **Baseline:** local and live remote `main` verified at `974c8c26ae9efcd236f3e590a536527c5200b2bf`; implementation remains uncommitted.
- **Disposition:** **`CHANGES_REQUESTED`**. FR-01/02/04/07 now CLOSED; FR-03/06 remain CLOSED. **FR-05 MEDIUM is the sole remaining blocking finding.** DTR26-FR-L01 is LOW/OPEN/NONBLOCKING.
- **Counter:** `2/3` corrections consumed; next correction is the third/final `3/3`; takeover `NOT_AUTHORIZED`. If that correction remains independently blocked, stop for explicit Owner disposition; no automatic takeover or extra round.

## Finding disposition

| Finding | Status | Verified result / limit |
| --- | --- | --- |
| FR-01 HIGH | **CLOSED** | Scheduled request abort, bounded two-worker/four-slot DNS queue, cancellation, EOF and interrupted-service checks added. Actual-manager slow-header diagnostic returns in 201 ms for a 200 ms budget. Native limit recorded below. |
| FR-02 MEDIUM | **CLOSED** | Present null/nonboolean flags rejected; unsupported charset maps to safe upstream failure. Full-body/single-root/integer/URL checks retained; 105 provider cases pass. |
| FR-03 MEDIUM | **CLOSED, retained** | Actual-manager peer/unsafe/mixed/rebinding/redirect/TLS-configuration checks repeated. Prior proxy-environment evidence remains applicable. |
| FR-04 MEDIUM | **CLOSED** | Exact BigInteger/BigDecimal conversion and Float/Double rejection replace double equality. Prior near-integer fixture now gives SOURCE_CACHE/all-null provenance; eight Radar PostgreSQL cases pass. |
| FR-05 MEDIUM | **OPEN / BLOCKING** | New identity/save sequences pass. Production test callbacks violate the change map, and transaction probes still observe the wrong phases. Remaining correction below. |
| FR-06 MEDIUM | **CLOSED, retained** | Post-fetch capture/clock behavior remains green. |
| FR-07 MEDIUM | **CLOSED for mandatory API/evidence gate** | JSON/ErrorResponse references now have generated-schema assertions; exact 233-route inventory passes. Supplied totals match supplied logs. Minor handback labels remain LOW L01. |

The selected mechanism remains appropriate: one Feed provider, existing article-URL ingest/save contracts, short finalizer and cached bounded read. No new module, migration, dependency, framework, provider or future capability is needed.

### FR-01 supported boundary and native limitation

`Round2Probe.java` exercises the production builder/manager with numeric resolver fixtures and fake sockets whose close operation wakes pending reads. The slow-header case now returns `UPSTREAM_UNAVAILABLE` after **201 ms** for **200 ms** remaining budget. The actual resolver has two workers and a four-slot ArrayBlockingQueue; exhaustion/interruption tests pass, and service interruption checks prevent finalization of interrupted fetches. Selection, complete-body and EOF guards are retained.

Native DNS is not universally interruptible: a synthetic one-second resolution with a 200 ms remaining budget returns after **1,002 ms**, with **zero connections after resolution expired**. That wait fits the issued two-second caller/cleanup allowance within the overall 70-second envelope. Aborted work does not connect or ingest. This is the existing documented host limitation, not exact kernel preemption or permission to raise limits. No real socket/provider request was made.

## FR-05 — remaining MEDIUM blocker

**Source:** [FeedItemService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java), `postStagingTestHook` declaration/setter/invocation; [HackerNewsRefreshFinalizer](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/HackerNewsRefreshFinalizer.java), `postStagingHook`/`postLockHook` declarations/setters/invocations. Tests: [refresh integration](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/HackerNewsRefreshIntegrationTest.java) and [refresh concurrency](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/HackerNewsRefreshConcurrencyIntegrationTest.java).

### Production test controls violate the approved boundary

The handoff permits only additive Radar work in FeedItemService and explicitly keeps generic `ingestFetch` unchanged. It requires **test-local spies/AOP/transaction synchronizations**, without production latches. Round 2 instead adds a public static mutable callback inside every generic ingestion transaction and two public callback setters on the production finalizer. Null defaults do not make these changes test-local; the global callback adds cross-request mutable control outside the approved read extension.

**Required:** remove all three production callback fields, setters and calls; restore the generic ingest body to HEAD. Move coordination into `src/test` with the already-approved tools. Another production setter or a test annotation on these callbacks is not the authorized alternative. No architecture amendment or new test-control API is needed.

### Transaction identity is sampled after ingest returns

`ingestFetchAndSourceUpdateSharePhysicalTransactionIdentity` records the first PID/transaction ID in `finalizer.postStagingHook`, **after `feedItemOperations.ingestFetch` has returned**. Its second sample is also in the outer finalizer. This proves two finalizer callbacks share a transaction, not which physical connection executed ingest; it would miss an independently committed inner ingestion transaction.

**Required:** record one observation inside the real generic ingest invocation through test-local instrumentation, and compare it with authoritative outer finalization/before-commit. Keep production propagation and item-then-source lock order unchanged.

### Retained-lock and timeout probes need the intended phases

`retainedSourceLockHeldUntilOuterCommit` probes from `postLockHook` **before the final enabled/type guard**, and treats any exception as lock denial. It does not observe an updater waiting after successful final validation until outer completion. Use test-local before-commit synchronization or an equivalent correctly ordered mechanism and assert the expected lock outcome/identity, rather than any exception.

The timeout test installs `SET LOCAL lock_timeout = '300ms'` in `postStagingHook`, after generic ingest has attempted/acquired its source lock. With a background source lock held, the hook cannot configure the earlier blocked operation. The real failure/503 result is credited (fresh execution about 4.1 seconds), but is not proof of the claimed 300 ms configuration or its placement. Configure any shorter test timeout before the contended statement and assert relevant rollback/timestamps plus the safe 503.

Retain the now-passing revised-URL/representative and saved-snapshot cases. Use finite, failure-safe test-local barriers. These changes finish the original FR-05 requirement; they add no feature, benchmark or production abstraction.

## DTR26-FR-L01 — LOW / OPEN / NONBLOCKING

The [handback](../implementation-handback.md) still describes a semaphore where admission uses ConcurrentHashMap/AtomicInteger, lists nonexistent `HackerNewsItemRecord.java`, and cites several nonexistent method names. Its **242** supplied tests are real, but the feed-compatibility selector label is inaccurate: its log runs FeedIntegrationTest plus SavedResourceInboxIntegrationTest (32 + 18 = 50); the handoff names FeedIntegrationTest, FeedValidationTest and FeedIngestionConcurrencyTest (32 + 10 + 5 = 47).

Independent review executed the exact required selectors, so this labeling difference leaves no coverage gap. Correct the labels during an in-scope documentation update or consolidated non-RAG audit; they do not force another S1 correction or block commit alone. Preserve historical logs. Current review metadata distinguishes the two runs accurately.

## Acceptance matrix

| Case | Result | Evidence / limit |
| --- | --- | --- |
| DTR-01 | PASS | Auth/preflight, source changes during HTTP and ambient-transaction rejection green. |
| DTR-02 | PASS | Actual-manager peer/negative/TLS/proxy evidence retained/repeated. |
| DTR-03 | PASS within documented host/cleanup limits | Abort, queue, EOF/cap changes and provider negatives; no universal native DNS cancellation claim. |
| DTR-04 | PASS | Exact ID/time/flag/URL validation and article grouping pass. |
| DTR-05 | NOT_VERIFIED in full | New representative/revised-URL cases pass; physical transaction probe placement remains FR-05. |
| DTR-06 | NOT_VERIFIED in full | Staged rollback and real timeout outcomes credited; production callbacks must be replaced with valid test-local proof. |
| DTR-07 | NOT_VERIFIED in full | Admission/source progress pass; after-validation lock-to-commit proof remains FR-05. |
| DTR-08 | PASS | UTC window, SQL cap/order/source filter and legacy URL checks. |
| DTR-09 | PASS | Exact provenance fallback and capture behavior verified. |
| DTR-10 | PASS | Article/Inbox path and refresh/save/refresh snapshot plus replacement cases pass. |
| DTR-11 | PASS | Exact 233 operations and JSON/ErrorResponse media/schema assertions pass; old routes retained. |
| DTR-12 | FAIL | Generic ingest changed for prohibited production test instrumentation; FR-05. Other tree/build/doc boundaries respected. |

## Independent verification

Reviewer directory: ignored `logs/daily-trends-radar/codex-rereview2-20261102-6a8ec9a299e146e88065e74967dfa797/`. Existing PostgreSQL image verified local. Seven exact handoff selector groups ran once with offline Maven and native-access flag through the normal **300-second per-command watchdog**. No full suite, download, provider smoke, unsafe destination, QA, inference or native worker execution.

| Stage | Exit | Tests | Failures/errors/skips | Wall seconds |
| --- | --- | --- | --- | --- |
| provider-unit | 0 | 105 | 0/0/0 | 10.188 |
| refresh-postgres | 0 | 20 | 0/0/0 | 36.578 |
| radar-postgres | 0 | 8 | 0/0/0 | 27.109 |
| radar-http | 0 | 5 | 0/0/0 | 26.062 |
| feed-compatibility | 0 | 47 | 0/0/0 | 27.110 |
| web-inbox-openapi | 0 | 17 | 0/0/0 | 33.593 |
| architecture | 0 | 37 | 0/0/0 | 23.172 |
| **Total** | **all 0** | **239** | **0/0/0** | **183.812** |

Command shape: `python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <new path> -- mvn.cmd -o -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=<each exact selector group in the handoff> test`. XML totals checked against final stdout summaries; short-name copies retained. Supplied receipts also exit 0 and total 242 with the distinct selection explained in L01; they are supplied results, not the reviewer count.

Evidence includes `Round2Probe.java`, `probe-results.json`, `round2-probe.json`, `test-summary.json`, XML, source hashes and supplied-count reconciliation. The actual-manager probe uses numeric fixtures and close-aware fake sockets, repeats peer/negative/redirect/TLS checks, verifies bounded DNS capacity, null-flag rejection and exact numeric fallback. Charset and generated-schema regression tests pass. No submitted source/test file was edited by Codex; historical evidence remains preserved.

## Coverage and Docs Sync

The initial full review and re-review 1 remain applicable to unchanged paths. Six changed production files, tests and current docs were re-inspected. Actual Feed tree remains **103 Java files / 25 packages**, all source package-info present, no .gitkeep. Root contracts, module graph, schema, POM, skills/hooks, source management and existing saving remain unchanged. The generic ingest body is reported as changed under FR-05, not falsely marked frozen.

| Dimensions | Status | Evidence / limit |
| --- | --- | --- |
| 1 intent; 2 traceability | FINDING | Same scope; FR-05 proof/boundary remains, other fixes credited. |
| 3 architecture; 4 ownership | PASS for module graph | 37 architecture tests; no new dependency/schema/root configuration. Transaction proof separately blocked. |
| 5 tree; 6 shared contracts | PASS | Actual grouping checked; canonical error schema restored; no common module/entity exposure. |
| 7 SOLID; 8 principles; 9 patterns | FINDING | Core mechanism proportionate; production callbacks/global mutable control violate the boundary. |
| 10 domain; 11 API; 12 validation | PASS for functional cases | Flag/charset/exact-number/identity/save and wire tests green; transaction completeness below. |
| 13 auth/privacy; 14 egress/secrets | PASS | Bearer, metadata-only, actual connector negatives; no article fetch/credential surface. |
| 15 transactions; 16 recovery | FINDING | Outcomes credited; actual phase sampling, lock and test mechanism remain FR-05. |
| 17 persistence | FINDING limited to FR-05 | PostgreSQL and bounded projection pass; generic-ingest alteration outside approved scope. |
| 18 resource bounds | PASS with native limit | Abort/finite DNS/body caps and short-budget diagnostics; no universal preemption claim. |
| 19 provenance | PASS | Exact numeric fallback and completion/capture verified. |
| 20 document/OCR; 24 ML evaluation | N/A | Neither capability changed/enabled. |
| 21 negatives; 22 test quality | FINDING | 239 cases pass; FR-05 must instrument actual phases using test-only tools. |
| 23 benchmarks | N/A | No SLO/project requested; resource checks performed without a benchmark subsystem. |
| 25 dependencies; 26 tooling | PASS with limits | No upgrade/download, bounded offline checks pass. Existing JDK/Mockito/Lombok warnings retained; IDE NOT_RUN, no warning-free claim. |
| 27 logging | PASS for inspected paths | Fixed categorical errors, no body/token logging added. |
| 28 governance/docs | FINDING | Current status corrected here; L01 captures minor handback labels. |
| 29 delivery/hygiene | PASS for review boundary | Only review/status edits by Codex; no source repair, staging, publication or historical log edits. |

Graphify navigation used the existing cache with a bounded 450-token query; canonical source is authoritative. Relevant review/coverage, Java, Feed, HTTP, persistence, reuse/SOLID/pattern, testing/watchdog and documentation rules remain applicable. No new agent/skill/hook or takeover.

**Docs Sync — UPDATED:** this report and current ACTIVE/handoff disposition/counters/result/next action. The entry handoff header still carried re-review-1 status while ACTIVE/footer carried round 2; current review synchronization resolves that status drift without resetting the counter. **ALREADY_CONSISTENT:** article identity, supported transport fixes, provenance, JSON errors and scope inventory. **REQUIRES CORRECTION:** FR-05 callbacks/proof. **LOW BACKLOG:** L01 handback labels; supplied and reviewer runs remain accurately distinguished.

**DEFERRED_OUT_OF_SCOPE:** broad stale CSA26/phase notices and prior UI26-FR-L02/CSA26-FR-L01/SAD-FR-L01/AUTH-CLOCK/backlog. P17 FROZEN, P18 CLOSED, S19-2 SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; indexing INACTIVE, live originals DISABLED/DEFERRED. No S2/scheduler/provider/frontend/Drive/RAG permission.

**Next step:** Antigravity | `/antigravity-implement-handoff` for FR-05, mandatory self-review, then independent Codex `$codex-final-review` | DTR26-S1 and this remaining finding | remove production callbacks, restore generic ingest and provide correctly placed test-local transaction/lock proof; no publication | `2/3` consumed; next/final correction `3/3`; takeover `NOT_AUTHORIZED`.
