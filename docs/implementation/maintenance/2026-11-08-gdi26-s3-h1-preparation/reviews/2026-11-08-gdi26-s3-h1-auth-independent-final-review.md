# GDI26-S3-H1-AUTH — independent initial final review

- **Date / reviewer:** 2026-11-08 / Codex, separate from Antigravity implementation and self-review.
- **Disposition:** **CHANGES_REQUESTED**. Five HIGH and seven MEDIUM findings remain OPEN; mandatory native-browser and other source-qualified acceptance cases remain NOT_VERIFIED. No implementation acceptance or commit message.
- **Authority:** [executable H1-AUTH handoff](../executable-handoff-h1-auth-2026-11-08.md), [selected v0.6 protocol](../h1-google-sign-in-owner-identity-contract-v0.5.md), [Owner policy](../../../../workflow/current-owner-delivery-policy.md) and [Docs Sync](../../../../workflow/documentation-synchronization.md).
- **Reviewed baseline:** local HEAD and live `origin refs/heads/main` both `ec5d1c3dcd65e9f409069e243d46a28ab960d421`; uncommitted H1-AUTH delivery. H1-B implementation `775564ff08e47640efcfe828560f40b12d54c7f3` and its published closeout remain accepted/stopped.
- **Counter:** initial H1-AUTH review; Antigravity **0/3 consumed**, next correction/re-review **1/3**. Takeover **NOT_AUTHORIZED**. H1-B 3/3 and other exceptional authority do not transfer.

## Evidence and limits

Reviewer evidence root **L** is `logs/gdi26/h1-auth/codex-final-8db46da907d54f3a9d7bf1a45728f616/` (ignored). `entry-source-manifest.json` records SHA-256 for all **48** entry changes. `relevant-package-tree.txt` inventories all **100** Authentication source/test paths recursively, including unchanged public contracts, internal capability packages, repositories, web DTO/advice and fixtures. `sealed-review-evidence.json` confirms zero entry-source drift before reviewer status writes and seals probe source hashes. Reviewer writes are this review and H1-AUTH status/result/next-action sections only; no production/test repair, staging, publishing or runtime activation.

All five exact handoff Maven selectors were freshly executed through `scripts/run-bounded-command.py --profile normal --timeout-seconds 300`, using installed Maven 3.9.16/JDK 25, PostgreSQL Testcontainers and installed Edge 157.0.4341.42. The full commands are the executable handoff's five selectors; each used `-ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED ... test`.

| Fresh receipt under L | Tests | Failures/errors/skips | Watchdog result / seconds |
| --- | ---: | --- | --- |
| `new-core.json` | 29 | 0/0/0 | PASS / 39.594 |
| `browser.json` | 2 | 0/0/0 | PASS / 28.078; random Spring port only |
| `legacy-auth.json` | **88** | 0/0/0 | PASS / 41.578 |
| `shared-integration.json` | 53 | 0/0/0 | PASS / 43.594; 38 architecture + 15 Portability |
| `drive-isolation.json` | 36 | 0/0/0 | PASS / 46.078 |
| **Total JUnit** | **208** | **0/0/0** | All exit 0, capture/cleanup complete |

The handback's 55 legacy / 175 aggregate count is incorrect: its original batch-3 stdout also reports **88**. Standalone probes below are **separate observations, not additional JUnit passes**. Their harnesses exited 0 after observing the defects; that exit is not an application acceptance PASS.

- `ProviderProbe.java`, `provider-probe.json/.stdout.log`: isolated synthetic signing key and loopback token/JWKS server, controlled stream latch, no real credentials/provider traffic. It releases the latch, closes the owned server and executor. Runtime: 14.547s.
- `SpringProbe.java`, `spring-probe-2.json/.stdout.log`: isolated Spring context and disposable PostgreSQL, actual MockMvc boundary/SQL and real store transactions. A controlled provider advances an injected clock; a repository decorator holds two real PostgreSQL transactions immediately after their admission counts. Neither replaces the transaction/commit implementation. Context, fake server, executor and container are closed. Runtime: 22.547s.
- Initial helper compilation and the first SQL-helper observation are preserved. The initial helper's unadapted JDBC `Instant` binding was not a constraint test; **only the corrected Timestamp-backed second probe is used for the V28 conclusion**. No old receipts were overwritten.
- Read-only listener/process inspection found :8080 held by `AgentService` PID 4840 and `vmnat` PID 23680. No unrelated process was killed or reconfigured. No fixed-port waiver, browser/driver installation, trust change, live Google/key provisioning or production Next evidence is inferred.

## Blocking findings

Source abbreviations: **A** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication`; **T** = `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication`. Line references describe the inspected, sealed entry version.

### AUTH-FR-01 — HIGH — production provider destinations are configurable; enabled credentials are not validated

**Location:** `A/internal/infrastructure/security/GoogleSignInProperties.java:16–17`, `GoogleSignInConfiguration.java:34–40`, provider constructor at `A/internal/infrastructure/oauth/GoogleSignInProviderClient.java:65–80`.

The production configuration binds `token-uri` and `jwks-uri` and passes them directly to the provider, including arbitrary HTTP origins. The independent probe invoked that production factory and observed its **synthetic client secret sent to an overridden HTTP token endpoint**. The same configuration context starts with enabled=true, a valid synthetic key and **empty client ID/secret**. This violates the selected fixed Google trust boundary and fail-fast ON validation; it is not a claim that an unauthenticated request can change operator configuration.

**Required correction/proof:** remove generic production endpoint binding, keep fixed production HTTPS metadata and use controlled constructors/test beans for fixtures. Validate nonblank separate login credentials and selected host/port configuration before provider I/O. Prove invalid ON configuration fails and fake substitution cannot be enabled through production properties. No new provider framework or normal-runtime activation.

### AUTH-FR-02 — HIGH — the provider body read can exceed the overall deadline indefinitely

**Location:** `GoogleSignInProviderClient.java:108–120, 242–253`.

Both exchanges use `BodyHandlers.ofInputStream()` followed by `readNBytes`, with no body deadline/cancellation owner or try-with-resources. The synthetic server sent headers and one body byte, then gated completion: **the actual provider call was still blocked after 11 seconds**. Non-200 bodies are also left unclosed. Token and JWKS each receive a fresh 10-second request timeout rather than sharing the required overall <=10s budget. Two blocked calls can occupy both provider slots; the synchronized JWKS path can also hold its cache monitor during unbounded I/O.

**Required correction/proof:** enforce one monotonic deadline across token, JWKS and complete bounded body consumption; close/cancel response resources on every path and release slots. Test token/JWKS headers-then-stall, slow chunks, non-200/oversize and combined elapsed budget with controlled latches and owned cleanup. Preserve no retry/redirect and <=2s connect limits.

### AUTH-FR-03 — HIGH — hand-written exchange/validation accepts prohibited token semantics

**Location:** `GoogleSignInProviderClient.java:99–130, 175–200`; POM OAuth2-client addition.

The class uses a manually built JDK HTTP form and Nimbus signature/claims checks; it does not use the selected managed Spring OAuth2 Client exchange APIs. The probe proves acceptance of a validly signed token with **wrong azp and a single expected audience**, and a token **missing iat**. It also proves a successful exchange response containing an **additional Drive scope** is accepted because only id_token is examined. That response's unexpected refresh credential was ignored; **no Google credential persistence was observed or claimed**. The signature/issuer/nonce checks that exist receive credit; the claims/scope acceptance holes remain. OIDC requires iat; the stricter azp/scope rules here derive from the approved local contract. [OpenID Connect Core, ID Token](https://openid.net/specs/openid-connect-core-1_0.html#IDToken).

**Required correction/proof:** use the already selected BOM-managed Client/JOSE seam with explicit confidential-client S256 and application validators; retain the custom durable transaction and PPV resource-server separation. Enforce required time claims, consistent azp whenever present (required on multiple audiences) and fixed granted scope including documented omission/email alias; preserve exclusion of Google credentials from persistence/session state. Add signed negative fixtures and exact skew boundaries. The [Spring reference](https://docs.spring.io/spring-security/reference/servlet/oauth2/client/authorization-grants.html) currently renders 7.1.2; it is guidance, not authority to upgrade local 7.1.1 or introduce default framework routes/sessions.

### AUTH-FR-04 — MEDIUM — cold JWKS failures bypass refresh cooldown

**Location:** `GoogleSignInProviderClient.java:224–240`.

The 60-second condition applies only when `cachedJwks != null`. With a cold cache and malformed JWKS, **three sequential unknown-key validations performed three fetches within one second**. Synchronization serializes the failures but does not impose the selected cooldown, so an outage/unknown-kid stream repeatedly reaches the provider.

**Required correction/proof:** apply singleflight and refresh-attempt cooldown to cold/failed cache states as well as populated caches, using the injected clock. Test cold failure/missing kid pressure, cached key reuse, expiry, cooldown and recovery; no stale or unbounded cache.

### AUTH-FR-05 — HIGH — callback finalization uses pre-network time

**Location:** `A/internal/application/google/GoogleSignInService.java:159–161, 222–228`; `GoogleSignInStore.java:205–208`.

The callback captures `now` before provider I/O and supplies it again to finalization. The PostgreSQL probe started at t=0, claimed at t=299s, advanced the injected clock during provider validation, then **published a ticket at t=301s** for an expired 300-second attempt. The claim epoch check exists but does not fix stale expiry evaluation. Ticket issuance timestamps also inherit the stale time.

**Required correction/proof:** evaluate current time at the final transactional fence after locks, reject expiry/invalidated claim, and base ticket TTL on actual issuance. Use current-clock checks after blocking admission/row-lock waits as applicable. Prove late/expired/restarted PROCESSING cannot publish identity/ticket or redispatch code; include TTL equality boundaries and stale claim/failure transitions.

### AUTH-FR-06 — HIGH — LOGIN admission is not serialized under the singleton lock

**Location:** `GoogleSignInStore.java:51–83, 129–148`; `GoogleSignInResources.java`; `GoogleSignInService.java:159–160`.

LOGIN uses `findById`, whereas LINK uses `findByIdForUpdate`. Two real store transactions, held after observing three active attempts, **both committed**, leaving **five active attempts**. The same count-then-insert race affects the rolling starts/retained capacity checks. The four-slot JVM semaphore is not a substitute for the required PostgreSQL owner lock. Separately, callbacks do not acquire an endpoint slot, and route-rate time uses `System.currentTimeMillis()` rather than the required injected clock.

**Required correction/proof:** serialize all start admission/count/insert paths under the existing owner row lock, count attempts/tickets once, and apply the approved endpoint/provider resources consistently. Keep network work outside locks. Add deterministic PostgreSQL barriers for 4-flow/20-start limits, concurrent LINK-failure boundary and callback endpoint exhaustion; inject Clock throughout. No fourth table or per-IP map.

### AUTH-FR-07 — MEDIUM — retention can exceed its cap or become permanently unavailable

**Location:** `GoogleSignInStore.java:100–111, 140–148`; attempt/ticket repository prune queries; entity expiry transitions.

Wrong-password insertion bypasses retained capacity: with 1,024 old failure rows the PostgreSQL probe returned the intended credential denial but left **1,025 rows**. Expired PENDING/PROCESSING attempts and PENDING tickets are never transitioned to EXPIRED by the operational path; prune queries accept terminal statuses only. The probe filled 1,024 PENDING attempts expired for >24h and a new LOGIN remained unavailable with every row still PENDING. Pruning an old attempt with a remaining ticket can also encounter its non-cascading FK; ticket/attempt selection must preserve unresolved proof.

**Required correction/proof:** bounded on-operation expiry and <=32 terminal >24h cleanup, tickets before attempts, applying capacity to failure events as well as OIDC starts. Preserve active/unresolved proof and rolling failure events; fail closed only when safe capacity truly cannot be recovered. Test abandoned/restarted flows, expired pending tickets, FK-safe cleanup and both 1,024-row boundaries without a scheduler/schema expansion.

### AUTH-FR-08 — MEDIUM — V28 conditional CHECKs admit missing required proof

**Location:** `backend/src/main/resources/db/migration/V28__google_signin_owner_transactions.sql:55–79`.

Active PKCE checks compare `octet_length` on nullable columns without explicitly requiring nonnull. **An actual PostgreSQL insert of an otherwise valid PENDING OIDC row with both PKCE columns NULL succeeded.** LINK owner equality has the same SQL NULL hazard. This is precisely the three-valued-logic failure excluded by the handoff. The identity issuer CHECK also accepts arbitrary HTTPS hosts, and the subject check accepts whitespace-only subjects instead of the selected canonical/nonblank DB shape.

**Required correction/proof:** correct the uncommitted V28 constraints with explicit required nonnull members and the selected issuer/subject constraints; synchronize additive DBML. Exercise every nullable proof/owner/status variant plus exact 12/59 lengths on PostgreSQL, and the mandatory seeded V27→V28 upgrade. Preserve V1–V27 and existing table shapes; do not edit an applied historical migration to repair this new one.

### AUTH-FR-09 — MEDIUM — the new HTTP boundary does not enforce its selected input/host limits

**Location:** `A/internal/web/controller/GoogleSignInController.java:49–116`; `A/internal/web/dto/GoogleSignInDtos.java:19–20, 31–32`; service challenge validation at 281–284.

Real MVC probes returned **200** for a duplicated handoffChallenge field, valid JSON exceeding 4KiB, and an unexpected Host/server name. Unknown fields and trailing JSON did return 400 and receive credit. A duplicated callback code caused provider dispatch (ultimately no ticket), rather than rejection before I/O. Controller checking only raw query length does not supply the specified single-value query/cookie/header checks. Challenge validation accepts uppercase although the selected wire encoding is lowercase and V28 rejects it later.

**Required correction/proof:** enforce the exact new-boundary host/port, 4KiB JSON, duplicate fields, canonical proof syntax, single-valued callback proof/cookie and <=8KiB aggregate metadata bounds before hashing/lookup/provider work. Keep existing global APIs, security matchers and CORS unchanged. Test both accepted requests and malformed/duplicate/oversize/host/forwarded-host cases with zero provider dispatch on rejection.

### AUTH-FR-10 — MEDIUM — new secret carriers expose credentials in generated strings

**Location:** `GoogleSignInDtos.java:24–38, 57–71`; `GoogleSignInService.java:44–56, 233`; provider `ValidatedIdToken` at line 49; fixture `FakeGoogleSignInServer.TokenRequest`.

Generated record toString includes fresh LINK password and raw handoff ticket/verifier; the probe confirms disclosure of synthetic values without printing them. StartResult/CallbackOutcome similarly include raw binding/ticket, and TokenRequest includes code/PKCE/client secret. Finalization errors concatenate raw exception messages into CallbackOutcome. These violate the explicit no-unsafe-toString/no-raw-provider-or-SQL-diagnostics requirement. **No actual production log disclosure is claimed**; the unsafe serialization/diagnostic path itself is confirmed. Existing AuthTokensResponse already demonstrates the local redaction convention.

**Required correction/proof:** redact all new credential/private identity carriers and keep callback errors in bounded categories, with no raw cause/message reflection. Extend focused SecretRedactionTest assertions using synthetic sentinels for request/service/provider/fixture carriers and safe failure paths. Do not redesign the frozen exception framework.

### AUTH-FR-11 — MEDIUM — mandatory evidence remains unverified and handback overstates acceptance

**Location:** `T/GoogleSignInBrowserIntegrationTest.java:27, 49–57, 85–155`; `T/fixture/GoogleSignInBrowserConsumer.java:113–117, 159–185`; [implementation handback](../implementation-handback-h1-auth-2026-11-08.md), acceptance matrix and current claims.

The browser fixture **always** uses RANDOM_PORT and rewrites the callback; even if :8080 becomes free it never exercises the selected fixed port. Two native positive/cancellation tests do not cover required missing/replaced/wrong/expired browser proofs or directly assert path exclusion/host-only/HttpOnly behavior. The consumer copies all Set-Cookie headers instead of the named verified cookie. Its cancellation flag is set before execution, and it marks refresh as revoked without checking the revoke response or stored revocation; this is not the claimed controlled late-arrival/generation race.

Further mandatory cases are absent from the listed tests: simultaneous same/different first LINK, committed 5th/6th fresh-password race, forced ticket+refresh rollback and two-thread **HTTP** redemption evidence, PROCESSING/restart/late-finalization, and seeded V27→V28 data upgrade. Existing sequential LINK and concurrent service redemption are useful partial evidence, not these cases. New-provider negative/boundary tests must accompany AUTH-FR-01–10. A-10 operations also points to nonexistent `/api/v1/auth/pin/verify`; actual controller uses `/api/v1/auth/private-pin/verify`, requiring existing PPV authentication. The selected lost-password-and-Google recovery limitation/activation gates need explicit operational wording.

**Required correction/proof:** implement only the handoff's missing current-slice evidence, fix the isolated consumer and accurate source-qualified matrix, and correct the named docs. Use fixed localhost :3000/:8080 for mandatory native positives/negatives when available; otherwise keep **A-07 NOT_VERIFIED / ENVIRONMENT_BLOCKED**, with acceptance blocked and no unrelated service kill/configuration change. Record A-08 backend/consumer evidence separately from DEFERRED_H1_F. Remove “all A-01–10 fully verified” and correct legacy count to 88. Normal runtime/client/key/Cloud activation remains separately gated. No production Next work or new testing platform.

### AUTH-FR-12 — MEDIUM — shared fixture modification violates the explicit isolation boundary

**Location:** `backend/src/test/java/com/vhvkhangg/personalprivatevault/support/AbstractWebIntegrationTest.java:158–160`; `T/fixture/GoogleSignInTestSupport.java:95–130`; new Google web test lifecycle.

Three tables were added to the frozen shared TRUNCATE despite the explicit “No shared cleanup/RAG/Media harness rewrite” instruction. Google support cleans before its tests, without its own after-test cleanup; Google web tests instead rely on the modified shared base. Green regressions therefore include an unauthorized harness change and do not establish the required fixture isolation.

**Required correction/proof:** restore the shared base to its entry baseline and make only the new Google fixtures/web tests clean their own disposable security rows before owner deletion, in FK order and on teardown/failure. Close owned fake servers/resources. Rerun the affected Google→legacy/Portability/Drive selectors against that restored base; do not repair unrelated shared/RAG/Media tests.

All twelve corrections fit the existing approved H1-AUTH contract. No new product, identity, frozen JWT behavior, recovery/rebind authority, provider activation or architecture choice is requested.

## Acceptance allocation and shared coverage

| Area | Independent disposition |
| --- | --- |
| A-01 | Partial evidence: conditional exact matchers, default-OFF wiring and 88 legacy regressions pass; ON config/negative source-specific proof incomplete (01/09/11). |
| A-02–03 | Fresh password event commit, sequential immutable linking and singleton ownership supported; admission/failure races and concurrent first-link proof not accepted (06/07/11). |
| A-04–05 | AES-GCM/tamper/signature/issuer/nonce positives and selected negatives pass; fixed transport/claims/cache/boundary findings OPEN (01–04/09). |
| A-06 | Shared MANDATORY issuance transaction and atomic conditional consume are coherent; positive/replay/concurrent service tests pass. Forced rollback/two-thread HTTP cases NOT_VERIFIED (11). |
| A-07 | Native Edge random-port partial credit; exact :8080 and required native negatives **NOT_VERIFIED / ENVIRONMENT_BLOCKED** (11). |
| A-08 | Late-finalization defect reproduced (05); PROCESSING/restart/consumer race evidence incomplete (11). Actual Next session/CSRF/generation/singleflight/restart **DEFERRED_H1_F**, no borrowed PASS. |
| A-09 | Fresh V28 Flyway/JPA, 38 Modulith and 15 Portability tests pass; SQL NULL/capacity/upgrade/isolation findings remain (06–08/11/12). |
| A-10 | Legacy APIs pass; source/operations correction and combined Google-failure emergency evidence remain scoped to 11. No PIN-only authentication/rebind inferred. |

The 29 shared dimensions were considered as the following grouped ledger; no extra project-wide gates are imposed.

| Dimensions | Result / evidence |
| --- | --- |
| 1–2 scope/traceability; 10 business/state | FINDING — 01–12 and source-qualified A matrix; current authorized implementation mode is valid. |
| 3–6 architecture/ownership/tree/shared contracts | PASS for internal ownership, unchanged named interfaces, root ApiResponse/ApiResponses under ADR-0016, cohesive capability packages/package-info and Modulith 38. FINDING for managed-client drift, V28 constraints and shared fixture boundary (03/08/12). No arbitrary common module/interface hierarchy is requested. |
| 7–9 SOLID/principles/patterns | PASS for service/store/provider separation and reuse of owner lock/PasswordEncoder/canonical issuance; FINDING for duplicated hand-written OAuth behavior, mutable production test seams and unsafe state/time semantics (01/03/05/10). No Strategy/Factory/event/general framework is needed. |
| 11–14 API/input/auth/privacy/credentials | FINDING — exact method matchers and HS256 separation preserved, but 01–05/09/10 remain. DTOs and callback 303 use accepted shared transport conventions. |
| 15–18 transactions/recovery/database/resources | FINDING — 02/04–08/12; actual PostgreSQL probes, FK/index/query/lock inspection. No cross-module persistence access or N+1 loop was introduced; boundedness failures are concrete. |
| 19–20, 24 RAG quality/document fidelity/evaluation | N/A — no RAG, ingestion, corpus/model/index/worker or scientific evaluation changes/enabled capabilities. No native source-file-ingestion proof required for this login slice. |
| 21–22 security/tests | FINDING / NOT_VERIFIED — 208 green JUnit are partial risk coverage, contradicted by sealed independent probes; mandatory gaps in 11. No H2 or test/runtime installer. |
| 23 performance benchmarks | N/A for new benchmark project; approved finite transport/cache/admission bounds reviewed under 02/04/06/07, not unsupported p95 claims. |
| 25 dependency/lifecycle | PASS for one BOM-managed starter, no version override/new test/browser package; FINDING for unused selected client exchange seam (03). No claim of fresh CVE/SBOM/license audit. |
| 26 tooling/static diagnostics | PASS for five bounded fresh batches and architecture. NOT_VERIFIED for IDE-wide static analysis; no warning-free claim. Existing logs include nonfatal Flyway/JVM notices. `git diff --check` flags three added blank EOF lines (LOW below). |
| 27 logging/observability | FINDING — 10; no actual production secret log incident asserted. Probes output booleans/counts/statuses only; synthetic keys/tokens stay out of receipts. |
| 28 docs/governance | FINDING — 11; API architecture/additive schema/package inventory inspected, current verification claims disagree with executable evidence. Historical frozen statuses remain out of scope. |
| 29 hygiene/delivery | PASS for ignored evidence/no generated source or publishing; FINDING for out-of-scope shared fixture (12). LOW newline hygiene below. |

**AUTH-L01 — LOW, nonblocking:** `git diff --check` reports added blank EOF lines in Authentication/AGENTS.md:56, docs/database/README.md:80 and repository-package-tree.md:552. CRLF normalization warnings also occur for the latter and PortabilityIntegrationTest. Record/fix proportionately during an authorized touched-file pass; LOW alone does not require another remediation cycle. No unrelated formatting cleanup.

**AUTH-L02 — LOW, nonblocking:** the two new local links at Authentication/AGENTS.md:41 use six parent traversals and resolve outside the intended repository docs directory. Correct them during an authorized touched-file pass. Scoped relative-link inspection otherwise found the review/ACTIVE/executable/doc links resolve; see `L/final-review-check.json`. No new implementation authority follows from repairing a link.

## Docs Sync Check and disposition

- **UPDATED:** this independent review, ACTIVE and executable handoff review/status/result/next action. Initial implementer handback remains preserved as the reviewed evidence claim, explicitly superseded by this review.
- **ALREADY_CONSISTENT:** published prerequisite identities, H1-B acceptance/counters; P17 frozen/P18 closed/S19-1 published/S19-2 suspended; default-OFF normal configuration and unchanged Drive/legacy auth contracts. Portability business inventory remains 67; only the three security exclusions are added.
- **FINDING / NOT_VERIFIED:** handback/ADR/DBML/operations/package-tree assertions must reflect 01–12 and actual counts, behavior and source-qualified acceptance. No document or successful test-port run waives fixed-port proof.
- **DEFERRED_OUT_OF_SCOPE:** older root historical routing notices, unrelated LOW registers and future H1-F implementation/visual/Next tests; no broad documentation cleanup.
- **Publication:** nothing staged, committed, pushed or accepted. No real secrets, activation, full-suite waiver, trust/native-hook repair or takeover.
- **Final source guard:** `L/final-review-check.json` confirms 49 changed paths = the 48 entry paths plus this review; only ACTIVE/executable entry hashes changed, all production/test hashes are preserved and the index is empty. Its two broken local links are AUTH-L02.

**Next step:** Antigravity | `/antigravity-implement-handoff`, then `/antigravity-self-review` | GDI26-S3-H1-AUTH; AUTH-FR-01–12 in this review and executable handoff | fix bounded findings, preserve mandatory NOT_VERIFIED honestly, then separate Codex `$codex-final-review` | H1-AUTH 0/3 consumed; next remediation 1/3; takeover NOT_AUTHORIZED.
