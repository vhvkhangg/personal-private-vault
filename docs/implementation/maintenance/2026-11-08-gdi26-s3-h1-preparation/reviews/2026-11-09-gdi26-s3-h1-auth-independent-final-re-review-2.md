# GDI26-S3-H1-AUTH — independent final re-review 2

- **Date / reviewer:** 2026-11-09 / Codex, independent of Antigravity implementation/self-review; review-only.
- **Disposition:** **CHANGES_REQUESTED / NOT_ACCEPTED**. Six more findings are VERIFIED/CLOSED. **AUTH-FR-09 and AUTH-FR-11 remain OPEN, both MEDIUM**, including mandatory NOT_VERIFIED. All prior HIGH findings are closed; no new finding IDs or architecture choices are introduced.
- **Counter:** H1-AUTH correction + independent re-review **2/3 consumed**; next correction/re-review **3/3**, the final Antigravity round. Takeover **NOT_AUTHORIZED**. A separately authorized Owner decision is required if that third correction/re-review remains blocked; no automatic takeover or extra round.
- **Authority:** [executable contract](../executable-handoff-h1-auth-2026-11-08.md), [selected v0.6 protocol](../h1-google-sign-in-owner-identity-contract-v0.5.md), [re-review 1](2026-11-08-gdi26-s3-h1-auth-independent-final-re-review-1.md), preserved [initial review](2026-11-08-gdi26-s3-h1-auth-independent-final-review.md), [Round 2 handback](../implementation-handback-h1-auth-2026-11-08.md), [Owner policy](../../../../workflow/current-owner-delivery-policy.md) and [Docs Sync](../../../../workflow/documentation-synchronization.md).
- **Baseline:** local HEAD and fresh live GitHub main both `ec5d1c3dcd65e9f409069e243d46a28ab960d421`. H1-AUTH is uncommitted. H1-B remains accepted/published/stopped at its own 3/3; H1-F and activation remain inactive/unauthorized. No publishing or commit message.

## Fresh verification and applicability

**L** = `logs/gdi26/h1-auth/codex-rereview-2-534b1dc98fc543f9aa6ca7b1ad6694b5/` (ignored, unique). Entry manifest covers **50** changed paths, including the two prior reviews; recursive inventory covers **100** Authentication source/test paths. Changed service/store/provider/controller/DTO/repository/fixture/DDL/docs were inspected, with unchanged accepted boundaries and prior source-qualified evidence reused. No Graphify graph exists. No production/test implementation was edited by this reviewer.

The **five exact executable-handoff selectors** were freshly run using installed Maven 3.9.16/JDK 25 and `scripts/run-bounded-command.py --profile normal --timeout-seconds 300`; each used `-ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED ... test`.

| Fresh receipt under L | Tests | Failures/errors/skips | Watchdog / seconds |
| --- | ---: | --- | --- |
| `new-core.json` | 50 | 0/0/0 | PASS / 70.188 |
| `browser.json` | 2 | 0/0/0 | PASS / 30.078; random Spring port only |
| `legacy-auth.json` | **89** | 0/0/0 | PASS / 42.578 |
| `shared-integration.json` | 53 | 0/0/0 | PASS / 42.578; architecture 38 + Portability 15 |
| `drive-isolation.json` | **36** | 0/0/0 | PASS / 47.078 |
| **Total fresh JUnit** | **230** | **0/0/0** | All exit 0, capture/cleanup complete |

Round 2's **195** total describes **different selectors**, not these five contract gates. Its Authentication selector omitted JwtPropertiesTest, JwtTokenServiceTest, RefreshTokenLifecycleIntegrationTest, PrivatePinIntegrationTest and PrivatePinConcurrencyTest; the listed replacement repository/PIN test names do not exist in the current test tree. Its Drive selector omitted four required classes: DriveApiInventoryIntegrationTest, DriveConfigurationIntegrationTest, DriveWebIntegrationTest and DriveH1BWebIntegrationTest. Surefire passing matching tests does not prove unmatched selector names ran. The independent 230-test receipts now establish the correct current-source cohorts; the handback/matrix must cite these accurately rather than calling the altered selection the agreed final gate.

Separate reviewer probes are **not additional JUnit passes**; a harness exit 0 means its observations completed, not blanket application acceptance:

- `ProviderR2Probe.java`, `provider-probe.json/.stdout.log` (**31.063s**): owned loopback token/JWKS server, synthetic RS256 key, real HTTP client and controlled latches. Valid managed exchange and code/client/secret/redirect/PKCE wire fields pass. Omitted scope and approved email alias pass; profile/numeric scope reject. An expired overall budget rejects even with cached keys. Token-body stall, JWKS-body stall and **6s token delay + stalled JWKS** each reject around **10.0s total** (10,007/10,001/10,002ms including scheduling overhead), rather than waiting indefinitely or allocating another 10s. Owned futures/server/executor/clients are canceled/closed.
- `managed-converter-bytecode.txt`: read-only inspection of the actual installed Spring Security **7.1.1** converter confirms grant-specific conversion dispatch. Source now uses managed ClientRegistration/grant/request-parameter/response converters with bounded JDK transport and Nimbus JOSE. This earns the selected managed-client correction; no additional framework login/session design is demanded.
- `SpringR2Probe.java`, `spring-probe.json/.stdout.log` (**24.047s**): actual isolated Spring/PostgreSQL, native owned random-port HTTP listener, counting request stream, OpenAPI and SQL checks. No real credentials or persistent runtime flags. Current Store and Service clocks are both injected in the expiry probe; the original global JWT clock is untouched. Time advances at the real owner-lock call boundary through a decorator, not a claim of a measured physical lock wait.
- Current observations: alias/wrong explicit port reject 400; unknown-length oversized body stops at **4,097 bytes** and returns 400; `/start` schema is its DTO `$ref`; authorization URL strings redact; NULL-owner failure event rejects; lock-time expired finalization/redemption both reject; concurrent starts leave four active flows; the previous reversed-ticket-order/1,024-row FK-capacity fixture now recovers safely.
- The same native probe observes **HTTP 200 for bare `Host: localhost`**, with production allowedPort left 8080 (FR-09 below). This is an explicit **negative authority test**, not substituted A-07 browser proof.
- `publication-baseline.json` confirms the published prerequisite. Installed Edge remains **157.0.4341.42**; :8080 still belongs to `AgentService` PID 4840 and `vmnat` PID 23680. No unrelated process was killed or reconfigured; no browser/driver, trust, hook, real key/Google client or deployment change.

## Adjudication of stable findings

| ID | Severity | Result and evidence |
| --- | --- | --- |
| AUTH-FR-01 | HIGH | **CLOSED, retained** — fixed production provider constants, no generic URI properties, fail-fast credentials; controlled test bean remains isolated. |
| AUTH-FR-02 | HIGH | **VERIFIED/CLOSED now** — limiting complete-body subscribers, cancelable futures, one monotonic budget including validation. Separate token/JWKS/combined-stall probes reject at the shared deadline; expired cache-hit budget rejects. |
| AUTH-FR-03 | HIGH | **VERIFIED/CLOSED now** — managed grant/request/response conversion has actual wire proof; fixed openid/email allowlist and omission/email alias qualification, extra profile/numeric/Drive rejection, explicit confidential code/PKCE transaction. No PPV decoder substitution or Google-token persistence. |
| AUTH-FR-04 | MEDIUM | **CLOSED, retained** — bounded cache/cold-failure cooldown logic unchanged in substance, one in-flight fetch admission; network outside cache monitor. Prior source-qualified cooldown proof remains applicable. |
| AUTH-FR-05 | HIGH | **VERIFIED/CLOSED now** — claim/finalize/redemption read injected current time after blocking locks. Fresh PG lock-boundary probes no longer publish expired ticket/token. Source and core tests preserve processing/replay fences and canonical atomic issuance. |
| AUTH-FR-06 | HIGH | **CLOSED, retained** — serialized owner admission, injected resource clock and callback slot retained; fresh probe admits one of two starts from three active, leaving four. |
| AUTH-FR-07 | MEDIUM | **VERIFIED/CLOSED now** — expiry runs consistently before capacity work; parent pruning excludes remaining child references. Previous reversed-order FK recovery fixture now succeeds; core repository regression verifies referenced parent preservation. |
| AUTH-FR-08 | MEDIUM | **VERIFIED/CLOSED now** — failure owner has explicit nonnull, canonical stored issuer is HTTPS only, active PKCE conditional checks retained; actual SQL NULL denial, fresh Flyway/JPA and new upgrade/constraint tests pass. |
| AUTH-FR-09 | MEDIUM | **PARTIALLY ADDRESSED / OPEN** — bounded streaming, strict JSON and DTO OpenAPI repaired; explicit alias/wrong-port denial repaired; absent port still bypasses selected authority. |
| AUTH-FR-10 | MEDIUM | **VERIFIED/CLOSED now** — both StartResult and StartGoogleSignInResponse redact authorizationUrl; real state/nonce sentinel assertions plus prior password/ticket/binding/identity redactions pass. Wire authorization URL remains available as required. |
| AUTH-FR-11 | MEDIUM | **PARTIALLY ADDRESSED / OPEN / mandatory NOT_VERIFIED** — genuine upgrade, post-refresh-write rollback, strict-token cases and recovery/activation docs improved; native/current-consumer proof and evidence qualification still incomplete. |
| AUTH-FR-12 | MEDIUM | **CLOSED, retained** — shared base remains restored, own fixture teardown preserved; affected exact selectors pass. |
| AUTH-L01/L02 | LOW | **CLOSED, retained** — scoped links and whitespace checked; CRLF normalization notices do not reopen them. |

Ten implementation findings and both LOW items are therefore closed; **only FR-09/11 remain**. A closure of a concrete defect does not aggregate all source-qualified A-01–10 subcases into PASS.

## AUTH-FR-09 — MEDIUM — missing authority port skips validation

**Location:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/web/controller/GoogleSignInController.java:288–325`, especially `if (port != -1 && port != allowedPort)` at 318–320.

**Fact:** when Host is simply `localhost`, parsing leaves port=-1 and deliberately skips the port comparison. A real origin-form POST with `Host: localhost` to the owned Spring test listener returns **200** and starts the flow. The production port policy is still 8080; no `setAllowedPort` seam was used in this probe. The authority without a port implies HTTP port 80 and does not satisfy the selected literal localhost:8080 authority. Explicit localhost:9999 and 127.0.0.1 now reject, which receives credit.

**Consequence:** the new boundary still accepts an unapproved authority and creates protocol state/cookies instead of rejecting before processing. This is a reproduced contract violation, **not a claim of forged Google identity or unauthenticated PPV token issuance**. Random-port testing of this negative case does not certify the mandatory fixed-port browser leg.

**Required bounded correction:** require the exact approved authority/port, including absent-port and fallback cases, before body/state/cookie/provider work. Keep test-only dynamic-port substitution isolated and explicit. Add a real HTTP or equivalent boundary regression for missing port plus existing alias/wrong-port/forwarding cases. Preserve repaired <=4KiB reads, exact DTO/OpenAPI, JSON/content-type and global security/CORS behavior. No generic host/proxy framework or broader deployment changes.

## AUTH-FR-11 — MEDIUM — mandatory native/consumer evidence remains incomplete

**Locations:** `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/GoogleSignInBrowserIntegrationTest.java` (`RANDOM_PORT`, setAllowedPort and two existing tests); `fixture/GoogleSignInBrowserConsumer.java` cancellation path; Round 2 handback acceptance/receipt/next-action sections.

**Credited current evidence:**

- `seededV27ToV28UpgradeCompatibility` now creates a disposable database, migrates to **target 27**, seeds old user/refresh data, migrates to **28**, verifies preservation and inserts into the new identity table. This closes the previously false upgrade label for the demonstrated storage scope; it is not live Google or production-backup certification.
- Forced rollback now stubs JWT signing **after canonical refresh save**, asserts pending ticket/no consumed timestamp and unchanged refresh count, then successful retry adds exactly one refresh row. MVC concurrent redemption and same/different-sub first-link tests remain useful.
- PROCESSING replay/expiry checks now operate on persisted PROCESSING state, and independent current-clock boundary probes establish the post-lock correction. Do not describe manually pre-aged test rows as physical waiting-on-lock execution; cite each source accurately.
- Missing iat, wrong azp, disallowed scopes/types and email alias are now actually submitted/asserted. The PIN/emergency limitation and explicit Owner activation gate wording are corrected. Existing consumer revoke=200 followed by refresh=401 still supports actual revocation.

**Mandatory remaining proof:**

1. **A-07 NOT_VERIFIED / ENVIRONMENT_BLOCKED:** Spring browser fixture remains RANDOM_PORT in every run, even if :8080 becomes available. The required native missing/replaced/wrong/expired proof cases and direct path/host-only/HttpOnly behavior assertions are still absent; two positive/preset-cancellation cases are not those negatives. Literal :8080 remains occupied by unrelated services. Keep this exact gate blocked; no port substitution, service kill, trust change or claim of Next proof.
2. **A-08 controlled consumer partial:** `simulateCancellation=true` is still set before starting the flow. It proves the selected canceled outcome/revocation path, not a prelogin/generation/logout change while callback/handoff result is in flight. The already approved controlled consumer ordering proof remains missing. Actual production Next session/CSRF/generation/singleflight/Node restart remains **DEFERRED_H1_F**, not requested implementation here.
3. **Evidence/Docs Sync:** replace the all-eight-resolved/full-browser/all-gates claims with the precise current-source adjudication and source-qualified cases above. The 195 count comes from altered selectors; refer to the independent **230** exact-selector receipts and keep the original implementer receipts preserved. Some listed replacement auth test names do not exist; naming a test is not evidence it executed. Maintain the skill's five-field Next step and the actual 2/3 counter.

**Required bounded correction/proof:** complete only the approved current-slice native/controlled-consumer cases and accurate evidence/docs. Make the fixture capable of the selected :8080 run when the environment permits, rather than always changing the port. Preserve unavailable mandatory proof as NOT_VERIFIED and acceptance-blocking until actually obtained. Retain independent upgrade/rollback/provider/lock-boundary and regression receipts; no redundant new test platform, full-suite run, real Google activation, production frontend, recovery feature or wider Auth/RAG changes. After the third failed correction/re-review, stop and seek the Owner's separate bounded disposition; this review grants no takeover or test-environment exception.

## Source-qualified coverage and Docs Sync Check

The 29 shared dimensions are considered as grouped risk coverage, reusing applicable prior inspection rather than imposing extra projects.

| Dimensions | Current result / evidence |
| --- | --- |
| 1–2 requirements/traceability, 10 business/state | FINDING — FR-09/11 only; ten defect closures are bounded. No feature/architecture expansion or blanket A-matrix acceptance. |
| 3–6 architecture/ownership/tree/shared contracts | PASS — recursive 100-path inventory, capability grouping/package-info, unchanged named interfaces and root ApiResponse/ApiResponses under ADR-0016; architecture 38 tests. FINDING — remaining authority qualification/evidence; no arbitrary common module or interface hierarchy. |
| 7–9 SOLID/principles/pattern fit | PASS — service/store/provider boundaries, canonical issuance reuse, managed protocol conversion and finite transport; no speculative generic framework or new pattern demanded. |
| 11–14 API/input/auth/privacy/keys | PASS for repaired streaming/DTO/redaction/provider/scopes/identity and fixed crypto; FINDING for absent-port authority (09), mandatory native proof (11). Existing PPV HS256/Owner fresh-password protection retained; fixture keys only. |
| 15–18 transactions/recovery/database/resources | PASS for post-lock expiry, replay/atomic rollback, FK-safe bounded pruning, V28 constraint/upgrade, admission and complete-body budget probes. No new cross-module persistence access or N+1 growth. Remaining missing consumer ordering is FR-11. |
| 19–20, 24 RAG/data quality/document fidelity/evaluation | N/A — no corpus/model/index/worker/source-ingestion change or activation. No unrelated OCR/native-file/judge gate. |
| 21–22 security/tests | FINDING / NOT_VERIFIED — 230 exact-selector JUnit pass and separate targeted probes; remaining native/consumer cases in 11. No H2, driver/runtime install or evidence waiver. |
| 23 benchmarks | N/A for a new benchmark project; required finite execution/I/O/capacity bounds were tested. No p95 or hardware scalability claim. |
| 25 dependencies/lifecycle | PASS — one existing BOM-managed starter; inspected actual 7.1.1 API/wire use; no upgrade/new test package. No new vulnerability/SBOM audit claimed. |
| 26 tooling/diagnostics | PASS — bounded five-batch/source/whitespace checks. IDE-wide static inspection NOT_VERIFIED; no warning-free claim or blanket suppressions. Existing JVM/Flyway/CRLF notices retained. |
| 27 logging/observability | PASS for new secret carrier redaction and safe callback categories; probes output booleans/counts/status/schema only. No actual production log leak alleged. |
| 28 docs/governance | FINDING — source-qualified A-07/A-08 and changed selectors/all-resolved claims (11). Current ops limitations are corrected; old historical routing remains out of scope. |
| 29 hygiene/delivery | PASS — own teardown, shared fixture baseline, ignored unique evidence, preserved previous reviews/logs and empty index. Reviewer edits only review/status artifacts. |

**Docs Sync Check:** UPDATED — this independent re-review and ACTIVE/executable status/result/review/counter/next action. ALREADY_CONSISTENT — published H1 preparation/H1-B/closeout, normal default-OFF flags, canonical shared issuance and unaffected JWT/PIN/bootstrap/Drive contracts, V28 only in disposable tests; P17 frozen/P18 closed/S19-1 accepted/S19-2 suspended. FINDING/NOT_VERIFIED — FR-09/11 above; implementer Round 2 handback remains preserved as reviewed claims, not independent closure. DEFERRED_OUT_OF_SCOPE — older routing/LOW registers, actual H1-F and activation/deployment. No source/test repair, staging, publishing, acceptance, commit message, extra round or automatic takeover.

**Next step:** Antigravity | `/antigravity-implement-handoff`, then `/antigravity-self-review` | GDI26-S3-H1-AUTH; AUTH-FR-09/11 in this re-review and executable contract | bounded final correction and mandatory evidence, then separate Codex `$codex-final-review`; unavailable exact-port/native proof stays NOT_VERIFIED | 2/3 consumed; next correction/re-review 3/3 (final); takeover NOT_AUTHORIZED.
