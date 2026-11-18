# BF26-NR-B0 rev. 1 — Google sign-in HTTP evidence reuse from G3

Proposed scoped credit for B04/B05 at audit SHA `dde8fc86e820b8f9a329a65cfe590191e027ef4c`, pending independent review. The four groups below refer to the enabled feature in the [B0 HTTP matrix](b0-http-contract-matrix.csv). The historical test used [Spring Security MockMvc with a bootstrapped Owner](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/GoogleSignInWebIntegrationTest.java#L35), a persisted PRIVATE session for link-start and [fake Google upstream](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/fixture/GoogleSignInTestSupport.java). Source locators below refer to `GoogleSignInWebIntegrationTest.java`.

| Matrix group ID | Executed assertions proposed for reuse | Source locator |
| --- | --- | --- |
| `HTTP-post-auth-google-link-start-private` | Anonymous sample returns 401. The bootstrapped Owner with a PRIVATE bearer and the correct password gets 200, a `Set-Cookie` header and a string `data.authorizationUrl`. | 138–172 |
| `HTTP-post-auth-google-start-proof` | The fixture with a seeded Owner Google identity returns 503 `GOOGLE_SIGNIN_UNAVAILABLE` and has zero rows in `ppv_google_login_attempts` after the request. Duplicate JSON field, oversized body and uppercase challenge samples return 400. The sampled untrusted host, port and forwarded-header cases return 400. The accepted host sample reaches 503. | 116–134, 338–431, 444–453 |
| `HTTP-get-auth-google-callback-proof` | A bound LINK callback returns 303 to `BFF_COMPLETE_URL`, with `Referrer-Policy: no-referrer` and `Cache-Control: no-store`. The binding cookie has `Max-Age=0`. The handoff cookie has a nonblank value, `Max-Age=60` and path `/api/bff/auth/google/complete`. The error-parameter sample returns 303 to the same location with both cookies set to `Max-Age=0`. The duplicate-code sample returns 303 to that location with handoff `Max-Age=0`. | 176–227, 244–258, 457–469 |
| `HTTP-post-auth-google-handoff-proof` | Submitting the ticket and verifier from the LINK callback returns 503 `GOOGLE_SIGNIN_UNAVAILABLE`. In the concurrent-submission sample, both requests return 503. | 229–240, 262–334 |

The same batch includes one generated OpenAPI assertion. `GET /v3/api-docs` returns 200 and the JSON request schema of `POST /api/v1/auth/google/start` references `#/components/schemas/StartGoogleSignInRequest` (lines 435–440). This credit concerns that one historical generated reference with Google sign-in enabled. Combined Drive and Google ON OpenAPI remains `NOT_VERIFIED`.

The link-start assertion covers status, header presence and the URL field type. The callback credit covers the listed response headers and cookie attributes. Browser cookie handling, a successful LOGIN, identity mutation, and independent enforcement of the duplicate-code predicate are outside these assertions. The handoff credit covers the observed HTTP failures. The test does not assert database contention, token issuance or mutation state for those requests. D4 credit in the [B0 report](b0-audit-report.md) stays limited to its separately verified assertions. The OFF groups, full envelopes, and any unlisted profile, provider or request variants receive no credit from this batch.

## Provenance and applicability

The [independent DP-A final review](../2026-11-11-dual-login-profiles-preparation/reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md) qualified G3 evidence at DP-A source commit `5c1da4e5404ee3c724e3fc2e03569d4093bcb44d`. The selected XML records **12 PASS, 0 failure/error/skip** within the 60-case G3 gate. Receipt exit was `0` after 64.625 seconds, with capture and cleanup complete. The reconstructed NUL-separated argv SHA-256 is `e8bdc46311380781a5651dd6e6ef1c5560b1e429352db4027887a84fd5a1ffe4`. Captured stdout and stderr sizes were 72,374 and 0 bytes. These are historical executions counted once.

Against the historical source manifest, 150 of 155 raw hashes match. The five differences are `SecurityConfiguration.java` and four test files outside the selected fixture (`ProfileAuthFixture`, `ProfileRouteAuthorizationIntegrationTest`, `ProfileSessionConcurrencyIntegrationTest` and `SecurityFilterChainIntegrationTest`). The security delta adds eight unrelated exact PUBLIC GETs and preserves the Google route conditions and authorities. The test, shared web support, Google fixture, relevant controller and DTO match. The separate OpenAPI source comparison preserves the Google operation customization and the start request schema annotation.

Artifacts are relative to `logs/bf26-dp-a/codex-takeover-1794408167866608000/`.

| Artifact | SHA-256 raw |
| --- | --- |
| `g3.json` | `7a8080f3f3a76f071ff512a6e71ca379b5947c7ee5a23272e3b626e6d1d117dd` |
| `g3-reports-v2/TEST-com.vhvkhangg.personalprivatevault.authentication.GoogleSignInWebIntegrationTest.xml` | `27a124a0d00daba648add72cfe3a8b62846af2f217391f61ed7a4422d18a8863` |
| `g2-attempt5-source.json` | `d1865ac1cf79a708ee8e33eba31ec44749abe2aac7e0ca0641da0d96826ab011` |
| `evidence-manifest.json` | `cd7061eae3c0e6e1c1de6ddab49f0293433068c1ce2cc02ee2940884340090f4` |

The historical fixture used PostgreSQL/pgvector with RAG recovery/reset dependencies. Runtime credit is limited to the historical fixture and synthetic upstream. OS egress remains `NOT_VERIFIED`. No new test ran. B04/B05 and B11 remain open, and the Media OFF HTTP responses still need their own evidence. B0 remains `BLOCKED_B0 / OWNER_DECISION_REQUIRED`, G5 remains unmet, and B0-U1 remains `NOT_RUN`. A1 stays deferred. A2, B1 and X remain outside this step.
