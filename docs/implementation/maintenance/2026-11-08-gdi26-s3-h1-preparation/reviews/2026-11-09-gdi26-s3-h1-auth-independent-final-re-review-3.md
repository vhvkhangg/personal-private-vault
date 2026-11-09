# GDI26-S3-H1-AUTH — independent final re-review 3

- **Date / reviewer:** 2026-11-09 / Codex, independent of Antigravity implementation/self-review; review-only.
- **Disposition:** **CHANGES_REQUESTED / STOPPED / OWNER_DECISION_REQUIRED**. AUTH-FR-09 is VERIFIED/CLOSED. **AUTH-FR-11 remains OPEN (MEDIUM), with mandatory NOT_VERIFIED.** Eleven of twelve implementation-review findings and both LOW items are closed; all HIGH findings remain closed.
- **Counter:** Antigravity correction + independent re-review **3/3 consumed / exhausted**. No fourth Antigravity round, counter reset or automatic takeover. Codex takeover **NOT_AUTHORIZED**.
- **Sources:** [executable contract](../executable-handoff-h1-auth-2026-11-08.md), [selected v0.6 protocol](../h1-google-sign-in-owner-identity-contract-v0.5.md), [re-review 2](2026-11-09-gdi26-s3-h1-auth-independent-final-re-review-2.md), preserved earlier reviews and [Round 3 handback](../implementation-handback-h1-auth-2026-11-08.md), [Owner policy](../../../../workflow/current-owner-delivery-policy.md), [Docs Sync](../../../../workflow/documentation-synchronization.md).
- **Baseline:** local HEAD and fresh live GitHub main both `ec5d1c3dcd65e9f409069e243d46a28ab960d421`; H1-AUTH remains uncommitted. H1-B remains accepted/published/stopped at its separate 3/3. No H1-F, activation, publication or acceptance follows.

## Fresh evidence and inspected scope

**L** = `logs/gdi26/h1-auth/codex-rereview-3-3ba23cf1d9f949b4a8d690109c8d04a6/`, unique and ignored. Entry SHA-256 manifest covers **51** changed paths, including all three earlier reviews; the recursive Authentication inventory remains **100** source/test paths. Round 3 production change is the controller authority check. Browser/web tests and the controlled consumer were inspected in full for the changed behavior; unchanged accepted provider/store/crypto/DDL/session/security/Portability sources retain their prior source-qualified verification. This review does not rerun unrelated backend/RAG audits or implement a fix.

All five exact executable-contract selectors were freshly run with installed Maven 3.9.16/JDK 25, `scripts/run-bounded-command.py --profile normal --timeout-seconds 300`, and `-ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED ... test`.

| Fresh receipt under L | JUnit tests | Failures/errors/skips | Result / elapsed seconds |
| --- | ---: | --- | --- |
| `new-core.json` | 50 | 0/0/0 | PASS / 102.219 |
| `browser.json` | 3 | 0/0/0 | PASS / 59.656; random Spring port |
| `legacy-auth.json` | 89 | 0/0/0 | PASS / 84.703 |
| `shared-integration.json` | 53 | 0/0/0 | PASS / 98.844; architecture 38 + Portability 15 |
| `drive-isolation.json` | 36 | 0/0/0 | PASS / 112.344 |
| **Total** | **231** | **0/0/0** | All exit 0, capture/cleanup complete |

The Round 3 exact-selector/count correction is independently verified. **Zero JUnit skips does not mean every acceptance condition ran**: the fixed-port condition takes its documented fallback branch and the missing native negatives are not implemented as tests.

`AuthorityR3Probe.java`, `authority-probe.json/.stdout.log` (34.578s) is a separate ignored verification helper, not an extra JUnit pass. It uses actual Spring/PostgreSQL and owned Java sockets against a disposable random-port listener with the production authority policy unchanged. It is an HTTP boundary test, **not native-browser or fixed-port proof**. Observations:

- Bare `Host: localhost`, explicit wrong port, IP alias and HTTP/1.0 missing-Host fallback each return **400**.
- No attempts are created by those denials; provider dispatch remains zero.
- Approved `Host: localhost:8080` returns **200**, creating one attempt. The listener's random physical port is disclosed; this does not substitute for A-07.
- The isolated context, server/container and fake provider are closed; no real credentials or normal runtime flags are changed.

Fresh `publication-baseline.json` confirms remote main. Read-only OS inspection still finds :8080 occupied by **AgentService PID 4840** and **vmnat PID 23680**; installed Edge metadata remains **157.0.4341.42**. No unrelated service was stopped/reconfigured and no driver/browser, trust, hook, key/Cloud or deployment change was performed.

## AUTH-FR-09 — VERIFIED/CLOSED

**Location:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/web/controller/GoogleSignInController.java:288–325`; the scoped Google web test fixture and `untrustedHostRejected`.

An absent explicit port now becomes the scheme's default 80/443, and every path compares its port against the selected allowed port. Server-name/port fallback also undergoes exact matching. The previous bare-authority bypass is closed by the real HTTP counterexample above, with no protocol state created on denial. The new web-test default Host remains local to GoogleSignInWebIntegrationTest; the shared base stays restored. Repaired JSON/content-type/stream/OpenAPI/redaction behavior and all earlier closures are preserved.

**Current register:** AUTH-FR-01–10 and AUTH-FR-12 CLOSED; AUTH-L01/L02 CLOSED. **AUTH-FR-11 alone remains OPEN.** No new confirmed production defect is recorded in this bounded re-review; this is not repository-wide or phase-final acceptance.

## AUTH-FR-11 — MEDIUM / OPEN / mandatory NOT_VERIFIED

### Earned Round 3 credit

`GoogleSignInBrowserIntegrationTest.java:37–55` now checks whether :8080 can be bound and selects `server.port=8080` when available, otherwise `server.port=0` with an explicit NOT_VERIFIED/ENVIRONMENT_BLOCKED message. This is useful fixture capability; its free-port branch remains unexecuted in this environment.

The fresh native Edge run demonstrates:

- Positive browser cookie delivery and clean completion on the assigned test port; binding Set-Cookie assertions now cover HttpOnly, callback Path, SameSite=Lax and Max-Age=300 (`:162–172`). These are wire-attribute plus positive-delivery observations.
- A new in-flight case (`:228–270`) waits until fake authorization is reached, changes the consumer cancellation flag, receives CANCELLED_IN_FLIGHT, verifies successful revocation and a subsequent refresh **401**. This earns the current controlled-consumer cancellation/revocation subcase, in addition to the preset cancellation test. It does not certify production Next session/generation/singleflight/CSRF/restart or immediate access-JWT revocation.
- The Java HTTP handoff negatives exercise consumed-ticket replay, malformed verifier and unknown ticket responses; previously accepted core tests continue to provide backend proof checks. The exact five selectors and 231-test count are now correct.

### Remaining mandatory proof

1. **A-07 exact :8080 leg: NOT_VERIFIED / ENVIRONMENT_BLOCKED.** The actual Spring browser listener still uses an assigned random port because unrelated services own :8080. Correct logging of that limitation does not satisfy or waive the selected literal localhost:8080 → localhost:3000 browser leg. Preserve that status until the required environment and real execution exist.
2. **Native negative cookie/proof and scope behavior: NOT_VERIFIED.** The added negatives at `GoogleSignInBrowserIntegrationTest.java:174–224` use new **JDK HttpClient** calls after Edge has completed. They do not remove/replace/expire a binding proof in the native browser's cookie jar, nor exercise native path/host-only/HttpOnly behavior under those negative conditions. The header checks do not assert host-only Domain absence or native script/path exclusion. The selected handoff explicitly requires native missing/replaced/wrong/expired proof negatives, not only API status tests.
3. **Negative-case qualification:** the wrong-verifier request uses the already consumed ticket from the successful flow. Its 401 therefore does not isolate verifier mismatch from replay rejection. The earlier core test checks wrong verifier on a live ticket and retains credit, but this new call cannot be counted as additional independent native-verifier proof.
4. **Scoped evidence/status wording:** retain positive random-port, attribute, API-negative and controlled cancellation evidence as separate supported subcases. Do not label the complete A-07 or FR-11 CLOSED/H1_AUTH_PASS while its exact-port/native-negative prerequisites remain missing. The Round 3 handback is preserved as reviewed implementer claims, superseded by this adjudication where it claims both findings addressed.

These are remaining requirements of the existing approved handoff, not new features or a request for a Next application. Real Google/Cloud/production activation stays unauthorized; synthetic provider/JWKS/PostgreSQL and isolated browser profiles remain the required verification boundary.

## Required stop and Owner disposition

The review itself is complete. Further implementation is stopped because this is the third independently blocked correction/re-review. The controlling [codex-final-review skill](../../../../../.agents/skills/codex-final-review/SKILL.md) says:

> 3/3 -> Owner decides bounded takeover, no automatic implementation.

The [Owner policy](../../../../workflow/current-owner-delivery-policy.md) also makes mandatory NOT_VERIFIED acceptance-blocking. A test-port substitute, another client's proof, extra round or documentation-only acceptance cannot manufacture the missing result.

**Concrete remaining scope for the Owner to disposition:**

- Provide/authorize a compliant verification environment with literal localhost :8080 and :3000 available, installed native Edge and the same inspected source/provenance. Do not kill or alter unrelated services under the current authority.
- If fixture work is needed, separately authorize a bounded **test-only** `$codex-implementation-takeover` for FR-11: GoogleSignInBrowserIntegrationTest and the new Google consumer/support fixtures only as necessary for native missing/replaced/wrong/expired proof and cookie-scope/HttpOnly verification, plus focused evidence/affected docs. Keep all closed production code, auth semantics, dependencies, privacy/resource limits and H1-F exclusions intact.
- Preserve receipts and inspect the exact native execution/negative cases before acceptance. Any Codex-authored fixture correction requires a **separate independent reviewer/session**, not self-acceptance. A verification-only disposition does not itself grant implementation authority.

This is a proposed bounded disposition, **not an authorization or new executable handoff**. Takeover remains NOT_AUTHORIZED. Owner publication and any follow-on slice wait for genuine independent acceptance. No commit message is supplied.

## Shared coverage / Docs Sync Check

All 29 dimensions were considered proportionately, updating the source-qualified ledger in re-review 2:

| Dimensions | Current result / evidence |
| --- | --- |
| 1–2 scope/traceability, 10 business/state | PASS for reviewed bounded source and eleven defect closures; FINDING/NOT_VERIFIED for FR-11 acceptance. Counter 3/3 exhaustion is preserved. |
| 3–6 architecture/ownership/tree/shared contracts | PASS — recursive 100-path inventory, capability grouping/package-info and unchanged named/public interfaces; root ApiResponse/ApiResponses and explicit 303 exception follow ADR-0016; architecture 38. No new module/common hierarchy. |
| 7–9 SOLID/principles/pattern fit | PASS — small controller correction, coherent service/store/provider ownership, canonical issuance/managed protocol reuse; no speculative abstraction or extra pattern. |
| 11–14 API/input/auth/privacy/keys | PASS for repaired authority plus prior streaming/schema/crypto/provider/redaction checks; FINDING/NOT_VERIFIED for native cookie/proof coverage (11). No real credentials/key provisioning. |
| 15–18 transactions/recovery/database/resources | PASS — unchanged accepted post-lock TTL, replay/rollback, bounded transport/admission/retention and V28 source hashes; current core/PG and 15 Portability tests. No cross-module persistence or new N+1/growth path. |
| 19–20, 24 RAG/quality/document fidelity/evaluation | N/A — no model/index/worker/source ingestion or scientific evaluation change. No unrelated native-file/OCR/LLM gate. |
| 21–22 security/tests | FINDING / NOT_VERIFIED — 231 fresh JUnit and a separate HTTP authority probe; missing exact-port/native-negative proof remains 11. No H2, test-platform/driver install or waiver. |
| 23 benchmarks | N/A for new benchmarking; accepted finite resource proofs reused against unchanged code, no p95/scalability claim. |
| 25 dependencies/lifecycle | PASS — unchanged one BOM-managed client starter, no new dependency/version/test package. No fresh CVE/SBOM audit claim. |
| 26 diagnostics/tooling | PASS for fresh bounded selectors, compilation, source/whitespace checks. IDE-wide static analysis NOT_VERIFIED; no warning-free claim or blanket suppression. Existing JVM/Flyway/CRLF notices retained. |
| 27 logging/observability | PASS for unchanged redaction/safe errors; probe prints safe statuses/counts only. Evidence remains synthetic/ignored, without raw cookie/token logs or a claimed production incident. |
| 28 docs/governance | FINDING/NOT_VERIFIED for A-07/FR-11 qualification; UPDATED current stop/Owner route. Earlier reviews, source-qualified proofs and unrelated historical records preserved. |
| 29 hygiene/delivery | PASS — scoped fixtures/shared-base isolation, unique ignored evidence and empty index; reviewer edits only review/status. No staging/publishing or unrelated cleanup. |

**Docs Sync Check:** UPDATED — this re-review and ACTIVE/executable review/status/result/counter/next action. **BLOCKED_OWNER_DECISION** — FR-11 at exhausted 3/3; no further Antigravity or automatic Codex implementation. NOT_VERIFIED — exact-port/native-negative requirements above. ALREADY_CONSISTENT — actual 231-test counts/selectors, published H1-B/closeout/prerequisites, default-OFF normal settings, prior closures, P17 frozen/P18 closed/S19-1 accepted/S19-2 suspended. DEFERRED_OUT_OF_SCOPE — actual H1-F, activation/deployment and old unrelated routing/LOW cleanup. Round 3 handback remains preserved; no acceptance or publication claim.

**Next step:** Owner | record an explicit FR-11 verification disposition and authorize bounded test-only `$codex-implementation-takeover` only if needed | GDI26-S3-H1-AUTH; this re-review and ACTIVE | compliant exact-port/native evidence and separate independent review after any fixture correction | Antigravity 3/3 exhausted / STOPPED; takeover NOT_AUTHORIZED.
