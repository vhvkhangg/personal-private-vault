# AUTH-FR-11 — D31 Codex test-only takeover handback

- **Date / implementer:** 2026-11-09 / Codex; continuation of the **same authorized test-only takeover 1**, not a new correction or independent review.
- **Handoff ID / status:** GDI26-S3-H1-AUTH / **IMPLEMENTED_AWAITING_CODEX_REVIEW**.
- **Authority:** [original bounded takeover](owner-authorization-auth-fr-11-test-only-codex-takeover-2026-11-09.md) and [Owner D31 alternate fixed native-test port](owner-decision-auth-fr11-alternate-native-test-port-2026-11-09.md). D31 supersedes the literal :8080 native-test condition only; shipping defaults/contracts are unchanged.
- **Finding:** AUTH-FR-11 **OPEN / ADDRESSED_PENDING_INDEPENDENT_REVIEW**. Implementer-observed amended native cases pass; Codex does not independently close its own finding or grant acceptance.
- **Counters:** Antigravity **3/3 exhausted**; existing Codex **test-only takeover 1 authorized / implemented / submitted**; independent acceptance pending. Test retries and the earlier environment block consume no new round/attempt.
- **Next gate:** a **SEPARATE independent Codex `$codex-final-review`** session. No READY_FOR_OWNER_COMMIT, commit message, Owner publication, H1-F/frontend or live Google claim.

## Baseline and exact delta

Local HEAD, origin/main and live GitHub main remain **`ec5d1c3dcd65e9f409069e243d46a28ab960d421`**. Resume entry contained **55 pre-existing changed paths**, including H1-AUTH implementation and historical reviews, prior blocked handback and D31. Entry hashes/status/index and before-copies of the two browser files are preserved. The earlier :8080 blocker and its reviewed evidence are not rewritten as successes; D31's pre-execution NOT_YET_PROBED wording is historical decision context.

Evidence root **E**: [unique D31 run directory](../../../../logs/gdi26/h1-auth/codex-fr11-d31-074f7abd3dcb4bf8a83f52e990b73aea/). All filenames below are relative to E. Raw outputs, receipts, copied JUnit XML, native DOM/boolean observations and failed-tool notes retain their actual provenance.

Only two test sources change:

- `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/GoogleSignInBrowserIntegrationTest.java`: fixed real Spring :19080; passive test-only callback observation; ten native cases; strict safe evidence/owned cleanup. Existing controller/service test seams are reused.
- `backend/src/test/java/com/vhvkhangg/personalprivatevault/authentication/fixture/GoogleSignInBrowserConsumer.java`: controlled cookie mutations/scope navigation and live-ticket redemption/cancellation observations; raw proofs/tokens stay server-side, retained records contain booleans/status/counts.

The other three session writes are this handback and current execution fields of ACTIVE/executable handoff. **No production source, migration, dependency, security rule/configuration, hook/trust/runner or shared test-support source is changed by this takeover.** All prior review/decision/handback history and the other entry files retain their hashes. Final path/hash/preservation evidence: `delivery-verification.json` and `session-delta-hashes.json` (generated after final saved documents, including this file).

## D31 port feasibility and real wiring

`preflight.json` / `preflight.stdout.log` record actual IPv4 **127.0.0.1** and IPv6 **::1** bind success for the first candidate **19080** and consumer **3000**. localhost resolves to ::1 and 127.0.0.1. Both owned probe listeners are closed. No second candidate, random Spring fallback, :8080 or forbidden 18080 is used by these native runs.

The submitted fixture binds actual Spring at **http://localhost:19080**, keeps consumer **http://localhost:3000**, sets the existing controller `setAllowedPort(19080)` and service `setCustomCallbackUri(http://localhost:19080/api/v1/auth/google/callback)` / fake auth endpoint test seams. The shipping strict Host parser still runs. The native callback observer asserts exact `Host: localhost:19080`, actual navigation metadata and the browser-delivered binding proof; it passes the real filter/controller chain exactly once without rewriting requests/responses. Authorization `redirect_uri`, native callback target and the fake provider's captured token-exchange redirect URI all match :19080.

The provider client already takes that service callback URI through its managed exchange; **no production change was required**. Callback success/failure remains fixed clean **303** to `http://localhost:3000/api/bff/auth/google/complete`, no-store/no-referrer, with no query/fragment or credential body. Normal application default :8080 is unchanged. Cookie probe aliases on consumer :3000 below are negative browser-scope controls, not accepted aliases for Spring or a port proxy.

Installed native Edge executable: `C:/Program Files (x86)/Microsoft/Edge/Application/msedge.exe`, **157.0.4341.42** at preflight; JDK **25**, Maven **3.10.0**, Python **3.12.3**. Each launch uses a fresh isolated owned profile, `--headless=new`, no-first-run/no-default-browser-check, disable-gpu/background-networking/component-update, `--virtual-time-budget=10000`, `--dump-dom`, and the fixed local `/start-flow` URL. No TLS/sandbox/trust bypass, driver/browser download, personal profile or real Google endpoint. [Chromium headless documentation](https://developer.chrome.com/docs/automation-and-testing/headless) is advisory; actual Edge results below are the proof.

## Submitted-source native case results

**10/10 JUnit PASS**, zero failures/errors/skips; exact submitted-source XML in `submitted-browser-junit/`, per-case actual evidence in **`native-submitted/<scenario>-<uuid>/`**. `submitted-tests.json` correlates scenario, source hashes, receipt and native evidence hashes. These are native Edge cookie/JavaScript observations through real Spring/PostgreSQL and fake Google/JWKS, not JDK CookieManager substitutes.

| Native case(s) | Observed evidence and boundary |
| --- | --- |
| Positive | Real :19080 callback and :3000 completion; provider exchange delta 1, canonical refresh row delta 1, one successful consumer redemption, clean final DOM. Both cookies have exact no-Domain/HttpOnly/Lax/Path/Max-Age attributes (binding 300s, ticket 60s); selected HTTP-local Secure exception only |
| Missing binding | Controlled Max-Age=0 deletes the native jar entry before callback. Callback sees no proof; no provider exchange, ticket, refresh row or consumer session |
| Replaced binding | A second real start overwrites the browser binding cookie; original state's callback carries the specifically observed second proof and rejects before provider/session |
| Wrong binding | Controlled well-formed random binding replaces the native cookie; callback observer confirms that exact wrong proof is delivered; provider/refresh deltas 0 and no ticket/session |
| Expired cookie | Controlled expired Set-Cookie (1970 Expires) removes the native jar entry; real callback sees no proof and rejects. This is simulated expiry, **not a physical 300s wait** |
| Expired attempt with retained native cookie | Test-only disposable row timestamps are shifted 301s, preserving the existing 300s DDL invariant. Real native callback carries its valid jar proof but expired server attempt rejects before provider/session; no global/production clock change |
| Scope / HttpOnly / SameSite | Native Cookie header present at a covered callback path; `document.cookie` on that path hides HttpOnly binding. Outside Path and `proof.localhost:3000` exclude it. Native cross-site fetch from 127.0.0.1:3000 excludes Lax binding, then cross-site top-level safe navigation to localhost delivers it; actual Sec-Fetch-Site confirms both cross-site legs. Completion JavaScript hides HttpOnly ticket and an outside-ticket-Path native report excludes it |
| Live-ticket wrong verifier → valid → replay | Browser delivers a newly minted ticket to controlled consumer. Server-only wrong verifier returns **401 while ticket is still PENDING**, unchanged refresh count and session false. Correct verifier then returns **200**, consumes ticket and creates exactly one refresh row/session; replay returns **401**, no second row. This is not the old wrong-verifier-on-consumed-ticket ambiguity |
| Preset cancellation | Native completion obtains late tokens, controlled consumer installs no session, revokes refresh successfully and proves subsequent refresh **401** |
| In-flight cancellation | A real native authorization request reaches a latch before cancellation is set; release continues callback/completion. Session remains uninstalled; late refresh revoke plus refresh **401** is verified. No claim of immediate access-JWT revocation |

Raw values remain server-side transient test state. A passive test observer reduces incoming cookie/proof comparisons and actual response attributes to safe booleans; it changes no authority. Native final DOM must have the completed outcome marker and contain no registered code/state/nonce/binding/ticket/verifier/token value. Stored DOM/stderr are redacted; evidence JSON contains no raw Cookie headers or token/credential payloads. The consumer's server-side Java HTTP redemption is its BFF-like leg **initiated by native completion with the actual browser-delivered cookie**, not a separate after-Edge API test replacing jar proof.

SameSite observations are qualified to this Edge version/current ambient policy and the tested sites/navigation types; a cross-site subresource may also be restricted by browser third-party-cookie policy. No universal browser-spec/CSRF certification is inferred. Cookies are host/path scoped, **not port-isolated**. This controlled consumer does not certify actual Next/BFF session persistence, CSRF, refresh singleflight, generation/logout fencing or Node restart. Those remain **DEFERRED_H1_F**.

## Commands, receipts and source applicability

All Maven runs use existing `scripts/run-bounded-command.py --profile normal --timeout-seconds 300`, `mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED ... test`; full safe commands/argv fingerprints and raw streams are retained. Browser evidence property is the absolute E subdirectory, never a personal profile.

Submitted-source final browser selector/command:

```text
python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt E/browser-final.json -- mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=GoogleSignInBrowserIntegrationTest -Dppv.auth.browserEvidenceDir=E/native-submitted test
```

| Receipt | Actual tests / exit / wrapper duration | Applicability |
| --- | --- | --- |
| `preflight.json` | Bind/metadata checks, exit 0 | Verified :19080/:3000; not a browser PASS by itself |
| `positive.json` | 1 PASS, exit 0, **93.719s** | Initial actual fixed-port feasibility; earlier test revision, XML/source snapshot preserved |
| `browser-all.json` | 10 PASS, exit 0, **146.922s** | Full native cases before exact-attribute assertion refinement; preserved, not counted again as distinct cases |
| `final-focused.json` | **60 PASS**, exit 0, **174.172s** | Exact browser selector + five new-core selectors; zero source drift during run. 10 native + **50 core**; XML in `final-focused-junit/` |
| `browser-final.json` | **10 PASS**, exit 0, **125.453s** | Submitted source after self-review evidence-root guard; XML in `submitted-browser-junit/`, native records in `native-submitted/` |

The 50 core cases are `GoogleSignInConfigurationTest,GoogleSignInCryptoTest,GoogleSignInProtocolTest,GoogleSignInPersistenceIntegrationTest,GoogleSignInWebIntegrationTest`. Their test/support/production source is unchanged between focused and submitted-source runs; the only later code edit confines the browser artifact output root. **60 distinct current cases** are evidenced, not 81 distinct tests from summing repeated runs. The earlier independently reviewed **231** total remains historical/source-qualified; legacy/shared/Drive selectors were not freshly rerun here. No fresh 238/full-backend/RAG or warning-free IDE result is asserted.

All receipts report capture/cleanup complete, normal exit, no timeout or hang termination. `profile-cleanup.json` verifies **31/31 owned profiles removed** across repeated launches. `post-listeners.json` finds no listener at :19080 or :3000 after test JVM exit; :8080 is still occupied by AgentService/vmnat (later observed PIDs differ from the earlier blocked preflight; no service changes were performed by this invocation). No unrelated process was killed, reconfigured, elevated or proxied.

Build warnings about existing deprecated/unchecked APIs, Lombok Unsafe, Mockito dynamic-agent attachment and SpringDoc remain in raw output; no blanket suppression or unrelated repair. No full-suite profile, runtime/browser installation, provider activation or Git publishing.

## Codex implementer self-review

This is **implementer self-review only**, not Antigravity self-review or independent acceptance:

1. Rechecked D31, the exact remaining re-review-3 FR-11 scope, prior three-round ledger and original authority. Fixed P=19080 is consistent across actual listener, callback, strict Host observation and managed token exchange; shipping :8080/parser/security remain untouched.
2. Inspected both complete changed test files and their only caller/fixture links recursively. Reused existing Spring/PG/fake provider, controlled callback seams, JDK HTTP server/client and installed Edge; no new dependency, driver, CDP/framework, shared platform or production abstraction. No other test support/legacy behavior changed.
3. Verified success/failure/state transitions using real browser jar manipulation, callback observations, actual SQL row counts, pending-ticket identity, no-provider admission and cancellation ordering. Attribute comparisons now use exact parsed attributes; output location is constrained to repository ignored scope logs even when Maven starts from backend.
4. Privacy review: proofs stay server-side, observer does not persist raw cookie values, retained records/DOM are safe; no token in JS/URLs/ordinary logs. Browser script observes names/booleans only. Controlled expiry and SameSite/consumer limitations are explicitly qualified above.
5. Reviewed bounded browser/consumer lifecycle: 30s child deadline, only owned descendants/profile cleanup, request deadlines and outer 300s cap; all owned profile removals and released test ports verified. No real Google/normal-runtime flag activation or service/network configuration change.
6. Preserved intermediate successful evidence and copied XML before subsequent Maven runs. Initial here-string source edit was denied **before dispatch** by the existing hook's ambiguous-quote guard; a duplicate-operation patch was rejected without application. Structured single-file Update patches succeeded; no hook/trust/configuration change or fabricated command receipt. No test failure is hidden or converted into a PASS.
7. Compared entry/current hashes and exact session write allowlist. Production/source/history outside the two test files is unchanged except authorized current ACTIVE/executable fields; all old reviews, decisions and blocked handback remain byte-identical. No new migration/module/schema/policy or scope beyond AUTH-FR-11.
8. Ran scoped Docs Sync and reopened saved ACTIVE/executable/this handback to check current ID/status/counter/results/Next step and links/whitespace. Final hash/path evidence is recorded externally to avoid self-referential handback hashes. No finding is independently CLOSED here.

## Docs Sync Check and submission

**UPDATED:** these two test files, this new D31 implementation handback and only current execution/result/status/counter/Next step in ACTIVE/executable. **ALREADY_CONSISTENT:** D31/previous authority, re-review-3 closures, H1-B publication/counters, source-qualified core proof, frozen production/Drive/auth semantics and normal flags OFF/unset. **HISTORICAL:** :8080 blocked preflight, earlier native random-port evidence, prior handback and D31 pre-execution state. **DEFERRED_OUT_OF_SCOPE:** H1-F/actual Next/UI/session/CSRF/visual/a11y, live Google/Cloud/HTTPS/ACL/deployment, unrelated notices/LOW cleanup and full-suite/IDE inspection. **PENDING_INDEPENDENT_VERIFICATION:** amended AUTH-FR-11 case applicability and finding closure; no self-acceptance.

Saved state is **IMPLEMENTED_AWAITING_CODEX_REVIEW** with AUTH-FR-11 **OPEN / ADDRESSED_PENDING_INDEPENDENT_REVIEW**, Antigravity 3/3 exhausted, existing Codex test-only takeover 1 submitted. Reopen/hash/link results are in E/`delivery-verification.json`; source and documentation hashes in E/`session-delta-hashes.json`. Separate independent review must assess native-submitted evidence against **D31-amended fixed-19080 acceptance**, not report literal :8080 PASS.

**Next step:** Owner opens a SEPARATE Codex session | `$codex-final-review` | GDI26-S3-H1-AUTH / AUTH-FR-11; ACTIVE, executable handoff, Owner D31 and this submitted handback | INDEPENDENT_REVIEW_REQUIRED; verify fixed :19080/:3000 native evidence and source hashes, no literal :8080 PASS/H1-F/activation | Antigravity 3/3 exhausted; SAME Codex test-only takeover 1 authorized / submitted; independent acceptance pending.
