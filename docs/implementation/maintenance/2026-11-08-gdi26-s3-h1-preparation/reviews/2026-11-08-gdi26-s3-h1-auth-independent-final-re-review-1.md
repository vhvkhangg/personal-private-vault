# GDI26-S3-H1-AUTH — independent final re-review 1

- **Date / reviewer:** 2026-11-08 / Codex; review-only, independent of Antigravity implementation and self-review.
- **Disposition:** **CHANGES_REQUESTED / NOT_ACCEPTED**. Four findings and both LOW items are VERIFIED/CLOSED; **eight findings remain OPEN: three HIGH, five MEDIUM**, including mandatory NOT_VERIFIED evidence.
- **Counter:** H1-AUTH remediation + independent re-review **1/3 consumed**; next correction/re-review **2/3**. Takeover **NOT_AUTHORIZED**. H1-B remains 3/3 accepted/published/stopped; no transferred authority or budget.
- **Sources:** [initial review](2026-11-08-gdi26-s3-h1-auth-independent-final-review.md), [executable contract](../executable-handoff-h1-auth-2026-11-08.md), [selected v0.6 protocol](../h1-google-sign-in-owner-identity-contract-v0.5.md), [Round 1 implementer handback](../implementation-handback-h1-auth-2026-11-08.md), [Owner policy](../../../../workflow/current-owner-delivery-policy.md), [Docs Sync](../../../../workflow/documentation-synchronization.md).
- **Baseline:** local HEAD and fresh live GitHub main both `ec5d1c3dcd65e9f409069e243d46a28ab960d421`. H1-AUTH remains uncommitted. This review does not reopen H1-B, Phase 16/H1-F, frozen Authentication behavior or suspended RAG work.

## Fresh evidence

**L** = `logs/gdi26/h1-auth/codex-rereview-1-e7f0cd3760f645ef9f6fd4ed963f044b/` (ignored, new evidence only). `entry-source-manifest.json` records all **49** entry changes; `relevant-package-tree.txt` inventories all **100** Authentication source/test paths recursively. Source/fixture/DTO/repository/package inspection was compared against the initial review; unchanged accepted boundaries and prior source-qualified evidence were reused. No production or repository test was edited by the reviewer.

All five exact executable-handoff selectors ran freshly with the normal **300s** watchdog, installed Maven 3.9.16/JDK 25, `-ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED ... test`.

| Receipt under L | JUnit tests | Failures/errors/skips | Result / elapsed seconds |
| --- | ---: | --- | --- |
| `new-core.json` | 46 | 0/0/0 | PASS / 82.656 |
| `browser.json` | 2 | 0/0/0 | PASS / 55.641; random Spring port |
| `legacy-auth.json` | 89 | 0/0/0 | PASS / 90.187 |
| `shared-integration.json` | 53 | 0/0/0 | PASS / 87.875; architecture 38 + Portability 15 |
| `drive-isolation.json` | 36 | 0/0/0 | PASS / 47.094 |
| **Total** | **226** | **0/0/0** | All exit 0, capture/cleanup complete |

Separate ignored reviewer probes are observations, **not additional JUnit passes**:

- `ProviderR1Probe.java`, `provider-probe.json/.stdout.log` (12.547s): real JDK HTTP client against an owned loopback token/JWKS server, synthetic RS256 key/token, real UTC clock and controlled body latch. Positive signature validation passes; wrong single-audience azp, missing iat and extra Drive scope now reject. Cold malformed-JWKS pressure now performs **one fetch for three calls**. Extra profile/non-string scope still accepts; token body still blocks beyond 11s. The latch is released and owned server/executor/clients close.
- `SpringR1Probe.java`, `spring-probe.json/.stdout.log` (52.125s): actual disposable PostgreSQL and Spring MVC/store transactions. A counting request stream, OpenAPI inspection, SQL constraint insert, controlled clock and concurrency probes produce the observations below. The clock advances **immediately at the real owner-lock call boundary through a repository decorator**; this simulates time elapsed acquiring the lock, not a claim of a measured two-second physical DB wait. Actual transactional store/SQL/token issuance are retained.
- `FkDetailProbe.java`, `fk-detail.json/.stdout.log` (19.531s): narrows the retention failure and confirms PostgreSQL **SQLSTATE 23503**. No previous helper or receipt was overwritten. Synthetic rows, contexts, containers and fake servers are disposed.
- `publication-baseline.json/.stdout.log` verifies current remote main. Native Edge metadata remains **157.0.4341.42**. Read-only inspection still finds :8080 held by `AgentService` PID 4840 and `vmnat` PID 23680. No unrelated service was killed/reconfigured; no browser installation, trust change, real Google traffic/key/Cloud provisioning or normal-runtime activation.

## Finding adjudication

IDs/severities retain their initial review identity. CLOSED applies to the stated defect, not blanket A-01–10 acceptance.

| ID | Severity | Independent result |
| --- | --- | --- |
| AUTH-FR-01 | HIGH | **VERIFIED/CLOSED** — production token/JWKS URI properties removed; production factory uses fixed constants; fake endpoints use a test bean/controlled constructor. Enabled blank credentials fail configuration tests. Remaining host boundary is FR-09. |
| AUTH-FR-02 | HIGH | **PARTIALLY ADDRESSED / OPEN** — streams now close on exit and a deadline is passed, but body reads still outlive it. |
| AUTH-FR-03 | HIGH | **PARTIALLY ADDRESSED / OPEN** — azp/iat/Drive rejection fixed; selected managed client and fixed scope semantics remain unresolved. |
| AUTH-FR-04 | MEDIUM | **VERIFIED/CLOSED** — cold failure cooldown applies; three independent cold calls cause one fetch, and network work leaves the cache monitor. No blanket claim of full transport-bound verification. |
| AUTH-FR-05 | HIGH | **PARTIALLY ADDRESSED / OPEN** — fresh time before store fixes the network-gap case but not expiry crossing lock acquisition. |
| AUTH-FR-06 | HIGH | **VERIFIED/CLOSED** — LOGIN and LINK serialize admission under the owner row lock; callbacks acquire endpoint slots; resource rate clock is injected. Fresh concurrent starts from three active admit one and leave four. Detailed acceptance/rate-window test gaps remain FR-11. |
| AUTH-FR-07 | MEDIUM | **PARTIALLY ADDRESSED / OPEN** — expiry methods and failure-event capacity gate added; FK-safe recovery remains broken. |
| AUTH-FR-08 | MEDIUM | **PARTIALLY ADDRESSED / OPEN** — active OIDC PKCE/owner nonnull checks fixed; failure-event owner still admits NULL. |
| AUTH-FR-09 | MEDIUM | **PARTIALLY ADDRESSED / OPEN** — duplicate/lowercase/oversize-result checks improved; host/port, pre-read bound and OpenAPI contract still fail. |
| AUTH-FR-10 | MEDIUM | **PARTIALLY ADDRESSED / OPEN** — password/ticket/verifier/binding/private identity and raw callback error redaction improved; authorization URL proof still appears in record strings. |
| AUTH-FR-11 | MEDIUM | **PARTIALLY ADDRESSED / OPEN / mandatory NOT_VERIFIED** — new race/rollback/HTTP/revocation tests earn partial credit; exact-port/native negative, genuine upgrade and other claimed evidence remain missing. |
| AUTH-FR-12 | MEDIUM | **VERIFIED/CLOSED** — shared AbstractWebIntegrationTest restored to HEAD; new Google support/web fixtures clean their own rows at teardown. Fresh affected selectors pass. |
| AUTH-L01 / AUTH-L02 | LOW | **VERIFIED/CLOSED** — whitespace check succeeds; scoped Authentication AGENTS links now resolve. Existing CRLF normalization notices are not new blockers. |

Source abbreviations below: **A** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication`; **T** = `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication`.

## Remaining bounded remediation

### AUTH-FR-02 — HIGH — complete-body deadline is still unenforced

**Location:** `A/internal/infrastructure/oauth/GoogleSignInProviderClient.java:107–110, 135–148, 296–318`.

The token/JWKS request timeout only surrounds the HTTP request stage; after `BodyHandlers.ofInputStream()` returns, `readNBytes` has no timer/cancellation owner. A server sending headers plus one byte then pausing still leaves the provider call pending after **11 seconds**. Try-with-resources closes only after the blocked read returns. The new wall-clock deadline is not checked during body consumption, and cache-hit validation can also return without enforcing elapsed total work.

**Smallest correction/proof:** enforce the selected single overall <=10s budget through complete bounded token/JWKS bodies and validation, cancel/close on expiry and release slots. Use monotonic elapsed execution time for I/O budgeting while retaining injected Clock for protocol timestamps. Add controlled headers-then-stall, slow-chunk, non-200/oversize and combined-token/JWKS budget tests with owned cleanup. No retry/redirect or new provider platform.

### AUTH-FR-03 — HIGH — scope allowlist was widened and the selected managed client remains unused

**Location:** provider `:40–45, 127–133, 155–164`; unchanged handoff managed-client requirement at lines 34/51/98.

Code exchange still manually constructs a form with JDK HttpClient and validates with direct Nimbus calls; there is no Spring OAuth2 Client exchange API usage despite the mandatory selected managed seam. The new allowlist includes **profile and userinfo.profile**, beyond the fixed openid/email request and approved email alias. An actual signed exchange with `scope="openid email profile"` succeeds; a numeric scope also succeeds because validation only runs for textual nodes. The fixed-profile scope change has no Owner authorization. The independent negatives confirm azp/iat and Drive-scope rejection are fixed; no Google-token persistence defect is asserted.

**Smallest correction/proof:** use the already selected BOM-managed Client/JOSE seam, preserving private transaction-specific validation, explicit confidential-client S256, durable state and PPV HS256 separation. Qualify returned scope against the stored fixed request/approved email alias or documented omission; reject malformed/additional scope. Exercise scope omission/alias/extra/type and signed claim/skew negatives. No dependency upgrade, default OAuth login/session route or generic provider framework.

### AUTH-FR-05 — HIGH — current time is still captured before DB locks

**Location:** `A/internal/application/google/GoogleSignInService.java:235–242, 263–265`; `GoogleSignInStore.java:207–216, 255–278`.

The service now reads time after provider validation, which fixes the earlier network-gap example. Store finalization and redemption still receive that timestamp before acquiring owner/attempt/ticket locks and never read current time after them. The PostgreSQL probe advances the service clock from attempt age 299s to 301s at owner-lock acquisition: **finalization still creates a ticket**. A second probe starts handoff redemption at ticket age 59s, advances to 61s at that boundary and **still issues PPV tokens**. This demonstrates a remaining stale authorization time; actual row-lock delay can create the same ordering.

**Smallest correction/proof:** make the authoritative expiry/claim checks use injected current time after the blocking locks, including ticket consume and actual issuance timestamps. Keep owner→attempt→ticket order and atomic canonical issuance. Add deterministic lock-boundary TTL crossing/equality and stale PROCESSING/fence tests. Do not change global JWT clock, claims or legacy session lifetimes.

### AUTH-FR-07 — MEDIUM — independent LIMIT selections can delete referenced parents

**Location:** `GoogleSignInStore.java:147–156`; `A/internal/infrastructure/persistence/GoogleLoginAttemptRepository.java:47–52` and ticket pruning query.

The two LIMIT 32 selections are independent; parent selection does not exclude attempts still referenced by retained tickets. The isolated database contains 33 eligible old completed attempt/ticket pairs (tickets inserted in a different order) and 991 recent terminal attempts, reaching 1,024 rows. A new start deletes a ticket subset, then chooses a parent subset that includes a remaining referenced attempt. PostgreSQL raises **23503** and rolls back cleanup, although safely recoverable pairs exist. New expiry updates and the password-failure capacity check receive credit; they do not repair this FK failure.

**Smallest correction/proof:** prune only parents whose tickets are already safely removed, respecting <=32/24h/proof retention and transaction/lock rules. Exercise differently ordered related records, expired pending/PROCESSING/tickets, capacity and failure-event windows. Apply bounded expiry/recovery consistently where capacity checks run. No cascading FK, scheduler or extra table.

### AUTH-FR-08 — MEDIUM — failure-event conditional owner requirement still passes SQL NULL

**Location:** `backend/src/main/resources/db/migration/V28__google_signin_owner_transactions.sql:62–75`.

The failure branch still uses `owner_id = 1` without `IS NOT NULL`. A real insert with record_kind=LINK_PASSWORD_FAILURE, purpose=LINK, owner_id=NULL and otherwise valid failure timestamps/proof-null fields **succeeds**, because CHECK accepts UNKNOWN. This remains the selected three-valued-logic invariant failure. The OIDC nonnull fixes are independently credited. DDL/DBML also now describe both issuer aliases as stored canonical values, whereas the accepted contract canonicalizes stored identity to HTTPS.

**Smallest correction/proof:** complete required-member checks for every conditional record variant and preserve the selected canonical stored issuer. Add failure-event NULL owner/field/status SQL negatives and synchronize additive DBML with the approved shape. Keep V1–V27 and existing table shapes unchanged; correct only the new uncommitted V28, with the genuine upgrade proof below.

### AUTH-FR-09 — MEDIUM — input remediation leaves boundary and OpenAPI regressions

**Location:** `A/internal/web/controller/GoogleSignInController.java:59–65, 87–98, 227–232, 243–282`.

The new host check explicitly permits 127.0.0.1/::1 and ignores port. Real MVC requests using **127.0.0.1:8080** and **localhost:9999** both start a flow with 200; the selected production pilot permits literal localhost:8080 only. Test substitutions must stay in test constructors/beans, not relax production trust.

The 4KiB check runs after Spring has converted the entire body to a String. A counting servlet stream records **65,623 bytes consumed before the 400 response**. Thus the rejection result is improved but the selected boundary is not a bounded read. Generated OpenAPI now declares `/start` application/json request schema **`{"type":"string"}`**, rather than the handoffChallenge DTO object; the other raw-String handlers need the same inspection. These are regressions introduced by the manual parsing approach. JSON null did return 400 in the independent probe and is not a finding.

**Smallest correction/proof:** enforce exact approved production host/port and metadata policy, consume at most the bounded body before allocating/parsing, retain strict duplicate/unknown/trailing/canonical checks, and document the real input DTOs in OpenAPI. Prove alias/wrong-port/forwarding denial, bounded stream consumption, duplicate callback no-dispatch and generated schema vs actual HTTP. Preserve existing global APIs, CORS/security matcher behavior and synthetic test seams; no general filter framework.

### AUTH-FR-10 — MEDIUM — authorization URL proof remains in record strings

**Location:** `GoogleSignInService.java:44–57`; `A/internal/web/dto/GoogleSignInDtos.java:47–60`.

StartResult still prints authorizationUrl, containing raw state/nonce, and StartGoogleSignInResponse retains a generated toString with the same URL. The probe confirms a sentinel state appears in the response string. The new SecretRedactionTest uses an authorization URL without proof parameters, so it misses this case. Password/ticket/verifier/binding/identity carrier redaction and bounded callback error categories are otherwise credited. **No actual production log leak is alleged.**

**Smallest correction/proof:** redact authorization URL/proofs in both new output carriers and add realistic synthetic state/nonce sentinels to redaction assertions. Preserve the necessary authorizationUrl wire response to the consumer; do not remove protocol data or change the shared transport contract.

### AUTH-FR-11 — MEDIUM — mandatory evidence labels still exceed what the tests execute

**Locations:** `T/GoogleSignInBrowserIntegrationTest.java:31, 55–63, 98–177`; `GoogleSignInProtocolTest.java:380–404, 501–571`; `GoogleSignInPersistenceIntegrationTest.java:314–326`; Round 1 handback/ACTIVE/executable claims.

Credited additions: real simultaneous same/different-sub linking; MVC concurrent redemption returning one 200/one 401; early issuance-seam failure rolling back ticket consumption; cookie-name filtering; revoke=200 followed by actual refresh=401. These support narrower conclusions than the full matrix:

- **A-07 mandatory NOT_VERIFIED / ENVIRONMENT_BLOCKED:** Spring is still always RANDOM_PORT. No required native missing/replaced/wrong/expired cookie-proof negatives or direct host-only/path/HttpOnly assertions were added. Exact :8080 remains occupied. A random-port success is partial evidence, even if its attributes resemble the selected cookie policy.
- **A-08:** the test named expired PROCESSING completion ages a **PENDING** attempt before callback; it stops at CLAIM_FAILED, exercising neither in-flight finalization nor PROCESSING/restart. Cancellation is still selected before starting the flow; it is not a controlled generation/logout change during an in-flight result. Production Next cases remain DEFERRED_H1_F, not borrowed PASS.
- **A-09:** `seededV27ToV28UpgradeCompatibility` runs against already migrated V28 and checks owner count/version. It never creates a V27 database, seeds its legacy state, applies V28 or verifies preserved credentials/tokens/data. A real seeded upgrade remains NOT_VERIFIED.
- **A-05/A-06:** the strict-token test starts a “missing iat” flow but never submits/asserts it, and contains no scope rejection call. The forced rollback stub throws at entry to issueForVerifiedOwner, before refresh persistence; it asserts pending ticket but no refresh-row rollback. The required failure after refresh persistence and row-count proof remain NOT_VERIFIED, despite sound-looking shared transaction annotations. Reviewer probes independently establish the fixed iat negative but do not complete all claimed JUnit coverage.
- **A-10/docs:** the PIN route/authentication wording is corrected. Operations guide line 66 now asserts recovery from an old offline backup when both password/Google access are lost. A backup does not itself supply a lost password or valid Google identity; that recovery claim needs explicit credential/key prerequisites or removal, consistent with the selected “not recoverable by H1” limit. Activation remains separately Owner-gated, not merely possession of client credentials/key.

**Smallest correction/proof:** complete the already required focused cases, then qualify each A-row honestly. Replace “all twelve resolved” and aggregate PASS with independently supported subcases; retain the fixed-port blocker until genuinely verified, without killing/reconfiguring unrelated services, installing drivers or silently substituting ports. Prove rollback after the real refresh write, seeded upgrade, in-flight/restart boundaries and controlled consumer/native negatives. Preserve early-rollback/concurrency/revocation evidence as partial credit. No full-suite expansion, production Next scaffold, recovery implementation or new test platform.

## Shared coverage and Docs Sync Check

All 29 shared dimensions were considered proportionately; this ledger updates the initial review rather than creating 29 new projects.

| Dimensions | Independent result / evidence |
| --- | --- |
| 1–2 scope/requirements, 10 state/business | FINDING — eight remaining IDs above; four closures earn bounded credit. Approved implementation review mode and unchanged non-goals verified. |
| 3–6 architecture/ownership/tree/shared contracts | PASS for internal ownership, unchanged public named interfaces, root ApiResponse/ApiResponses under ADR-0016, recursive 100-path package inspection/package-info and 38 Modulith tests. FINDING for selected client/DDL/OpenAPI drift (03/08/09); no arbitrary common module or interface hierarchy. |
| 7–9 SOLID/principles/pattern fit | PASS for service/store/provider roles, canonical issuance reuse and no speculative generic framework; FINDING for manual client duplication and time/cleanup semantics (02/03/05/07). No new pattern is demanded. |
| 11–14 API/input/auth/privacy/key handling | FINDING — 02/03/05/08–10. Fixed provider/credential gate and protected LINK remain; callbacks use the approved 303 exception. Synthetic keys only; real ACL/provisioning remains a later gate. |
| 15–18 transactions/recovery/database/resources | FINDING — actual PG lock-time/FK/NULL probes (05/07/08), body/transport bounds (02/09). PASS for repaired serialized admission, no new module repository leak/N+1 loop and 15 Portability checks. |
| 19–20, 24 RAG/data quality/document fidelity/evaluation | N/A — no RAG/model/source ingestion/index/worker capability changes. Do not demand unrelated external-file identity/OCR/evaluation proof. |
| 21–22 security/tests | FINDING / NOT_VERIFIED — 226 fresh green JUnit, targeted independent positive and negative probes; mandatory evidence gaps remain 11. No H2, runtime/test/browser installation or evidence waiver. |
| 23 benchmarks | N/A for a new benchmark project; required finite resource bounds reviewed under 02/05/07/09, without unsupported latency claims. |
| 25 dependency lifecycle | PASS for one BOM-managed starter/no version or package expansion; FINDING for selected exchange seam unused (03). No fresh vulnerability/SBOM audit claim. |
| 26 tooling/diagnostics | PASS for bounded fresh five-batch verification and git diff --check. IDE-wide static analysis NOT_VERIFIED, not asserted warning-free. Nonfatal JVM/Flyway/CRLF notices retained; no blanket suppression. |
| 27 logging/observability | FINDING — authorization URL strings (10); safe bounded callback categories improved. Probes print only synthetic-safe booleans/counts/status/schema/SQLSTATE, no raw credentials. |
| 28 docs/governance | FINDING — source-qualified matrix and scoped operations/client/schema assertions (03/08/09/11). Initial review/history retained; H1-B and earlier exceptional counters separate. |
| 29 hygiene/delivery | PASS — shared base restored, own teardown, LOW fixes verified, ignored fresh evidence and empty index. Reviewer changes only review/status artifacts. No staging/publishing or unrelated cleanup. |

**Docs Sync Check:** UPDATED — this re-review plus ACTIVE/executable review/status/result/counter/next action; implementer Round 1 handback is preserved as reviewed claims, not independent closure. ALREADY_CONSISTENT — local/live published prerequisites, H1-B accepted/stopped, default-OFF normal settings, unaffected JWT/refresh/PIN/bootstrap/Drive contracts, P17 frozen/P18 closed/S19-1 published/S19-2 suspended. FINDING/NOT_VERIFIED — remaining IDs and A-matrix above; scoped operational/schema/API assertions must match the contract. DEFERRED_OUT_OF_SCOPE — old historical routing notices, unrelated LOW registers, actual H1-F UI/session/CSRF/generation/Node-restart and activation/deployment. No broad doc cleanup, acceptance, commit message, takeover or publishing.

**Next step:** Antigravity | `/antigravity-implement-handoff`, then `/antigravity-self-review` | GDI26-S3-H1-AUTH; remaining AUTH-FR-02/03/05/07/08/09/10/11 in this re-review and executable handoff | bounded correction and mandatory evidence, then separate Codex `$codex-final-review`; keep unavailable native proof NOT_VERIFIED | 1/3 consumed; next remediation/re-review 2/3; takeover NOT_AUTHORIZED.
