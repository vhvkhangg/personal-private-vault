# GDI26-S3-H1-AUTH — independent final review of D31 test-only takeover

- **Date / reviewer:** 2026-11-09 / Codex, independent of the Codex test-only implementer; review-only.
- **Disposition:** **READY FOR OWNER COMMIT** (`READY_FOR_OWNER_COMMIT`). **AUTH-FR-11 VERIFIED/CLOSED** against the Owner's D31-amended acceptance. AUTH-FR-01–12 and AUTH-L01/L02 are now CLOSED; no blocking finding or mandatory H1-AUTH NOT_VERIFIED remains.
- **Counters:** Antigravity **3/3 exhausted / accepted / STOPPED**; SAME Codex **test-only takeover 1 independently accepted / STOPPED**. No new round, counter reset or further implementation authority.
- **Baseline:** local HEAD, origin/main and fresh live GitHub main all `ec5d1c3dcd65e9f409069e243d46a28ab960d421`. The reviewed H1-AUTH delivery remains uncommitted and unpublished.
- **Authority / submission:** [original test-only authorization](../owner-authorization-auth-fr-11-test-only-codex-takeover-2026-11-09.md), [Owner D31](../owner-decision-auth-fr11-alternate-native-test-port-2026-11-09.md), [submitted D31 handback](../codex-test-only-takeover-auth-fr-11-d31-handback-2026-11-09.md), [executable contract](../executable-handoff-h1-auth-2026-11-08.md), [prior independent re-review 3](2026-11-09-gdi26-s3-h1-auth-independent-final-re-review-3.md), [Owner policy](../../../../workflow/current-owner-delivery-policy.md) and [Docs Sync](../../../../workflow/documentation-synchronization.md).

## Scope, provenance and independent verification

This review accepts the bounded H1-AUTH slice by closing its remaining FR-11 evidence gap and consolidating unchanged prior independent evidence. D31 replaces only the literal native-test Spring port 8080 with the selected fixed **19080**; the consumer remains **localhost:3000**. This report does **not** mark literal :8080 browser execution PASS. Historical blocked and random-port evidence remains intact.

**L** = [unique independent evidence directory](../../../../../logs/gdi26/h1-auth/codex-independent-d31-e86e7ad5cde0b6bc5633f162b42b8835/), ignored and separate from the implementer's receipts. Entry manifest covers **56 pre-existing changed paths**. Comparing every path in the prior re-review-3 entry manifest identifies only two changed sources: `GoogleSignInBrowserIntegrationTest.java` and `fixture/GoogleSignInBrowserConsumer.java`; other differences are authorized current status and new authority/handback/history documents. All production, migration, dependency and other test hashes in that manifest match. Unchanged HEAD and the full changed-path inventory also qualify the previously reviewed tracked baseline.

Both changed test files were inspected completely, including their existing provider/controller/service seams, superclass cleanup, passive callback observer, consumer routes, JavaScript, proof mutations, live-ticket sequence and child-process lifecycle. The recursive Authentication source/test inventory is **100 paths**, identical to the prior package tree; capability grouping, visibility, exception/DTO placement and package descriptors remain coherent. No additional module, shared transport hierarchy, test framework or production abstraction is introduced.

`entry-source-manifest.json`, `r3-source-comparison.json`, `authentication-tree.txt` and `independent-results.json` retain source identities and observations. Every backend source hash remained unchanged during the independent run. Final review/status-only preservation checks are recorded in `final-review-check.json`.

## Fresh bounded execution

Preflight `preflight.json` / `preflight-observations.json` independently proves simultaneous owned bind/listen availability at **127.0.0.1 and ::1**, for **19080 and 3000**. localhost resolves to both families; all probe sockets close. Installed Edge **157.0.4341.42**, its executable SHA-256, Windows 11 build 26200, Oracle JDK **25**, Maven **3.10.0** and Python **3.12.3** are recorded. No unrelated service, port reservation, firewall, trust or installation is changed.

Fresh command: existing `scripts/run-bounded-command.py --profile normal --timeout-seconds 300`, installed `mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED`, with the five exact new-core classes plus `GoogleSignInBrowserIntegrationTest`, and absolute `-Dppv.auth.browserEvidenceDir=L/native-independent`. The combined run also checks that the expanded browser fixture coexists with the core tests. It uses real Spring, disposable PostgreSQL and controlled fake Google/JWKS.

| Fresh selector | Tests | Failures / errors / skips |
| --- | ---: | --- |
| GoogleSignInConfigurationTest | 4 | 0 / 0 / 0 |
| GoogleSignInCryptoTest | 5 | 0 / 0 / 0 |
| GoogleSignInProtocolTest | 16 | 0 / 0 / 0 |
| GoogleSignInPersistenceIntegrationTest | 13 | 0 / 0 / 0 |
| GoogleSignInWebIntegrationTest | 12 | 0 / 0 / 0 |
| GoogleSignInBrowserIntegrationTest | 10 | 0 / 0 / 0 |
| **Fresh total** | **60** | **0 / 0 / 0** |

`native-core.json`: **PASS**, exit 0, **161.594 seconds**, capture and cleanup complete, no timeout or hang termination. Copied XML in `junit/` and ten separate `native-independent/<scenario>-<uuid>/` records preserve DOM, redacted browser stderr and safe native observations. `independent-results.json` correlates XML, case names, source hashes and evidence hashes. The receipt's argv fingerprint and raw streams preserve execution provenance.

All **10/10** isolated owned Edge profiles are removed. After test JVM exit, both ports again pass actual IPv4/IPv6 bind/listen probes. Native Edge uses the approved headless flags and fresh profiles, a 30-second per-child deadline and the outer normal watchdog. No personal browser profile, driver/download, TLS/sandbox bypass, proxy, random Spring fallback, :8080 or forbidden :18080 is used.

## AUTH-FR-11 — VERIFIED/CLOSED

The actual Spring listener is fixed at **19080**. Existing test-only `setAllowedPort(19080)` and callback/auth endpoint seams retain the shipping strict authority check and real filter/controller chain. The passive observer invokes that chain once without request/response rewriting. Native callback observations confirm exact **Host: localhost:19080**, navigation metadata and the specifically expected browser-delivered proof; authorization, callback and managed token-exchange redirect agree. Completion stays the exact clean **303** to `http://localhost:3000/api/bff/auth/google/complete`, no-store/no-referrer, without query/fragment credentials.

The consumer binds IPv4 127.0.0.1 while native URLs and Host remain localhost; successful native requests establish that usable local leg. This is not a claim that the browser exercised both IP families. Alias requests below are negative cookie-scope controls on the controlled consumer, not alternate accepted Spring authorities.

| Independently rerun native case | Actual evidence / adjudication |
| --- | --- |
| Positive | One provider exchange, one canonical refresh row, one successful redemption and installed controlled-consumer session; actual browser delivers binding and handoff cookies. Exact no-Domain, HttpOnly, Lax, Path and Max-Age 300/60 attributes pass; selected HTTP-local Secure exception is explicit. |
| Missing / replaced / wrong binding | Edge's jar is changed by controlled Set-Cookie or a second real start. The callback observes absence or the specifically replaced/wrong value. Each rejects before provider exchange, ticket creation, refresh persistence or consumer session installation. |
| Expired cookie / expired attempt | An expired Set-Cookie removes the native jar entry. Separately, disposable attempt timestamps shift 301s while the valid native cookie remains. Both reject before provider/session. These are controlled expiry simulations, not a physical 300-second wait or production clock change. |
| Path / host-only / HttpOnly / SameSite | Native covered-path request contains the binding while document.cookie hides it; outside Path and proof.localhost exclude it. Cross-site resource excludes it and safe top-level navigation delivers it, both confirmed by native Sec-Fetch-Site. Completion JavaScript hides the handoff cookie; an outside-ticket-Path native request excludes it. |
| Live ticket: wrong verifier, correct verifier, replay | Native completion supplies a newly issued browser-delivered ticket to the consumer's server leg. **401 → 200 → 401**: first denial retains one PENDING ticket, unchanged refresh count and no session; correct proof consumes it and creates exactly one pair; replay creates no additional refresh row/session. |
| Preset and in-flight cancellation | The in-flight case waits for native authorization before setting cancellation and releasing the latch. Both cases receive late tokens, install no consumer session, revoke refresh and assert later refresh **401**. No immediate access-JWT revocation claim. |

The missing/replaced/wrong/expired cases now manipulate or observe **Edge's native cookie jar**; they are not after-browser JDK-only negatives. The Java HTTP start/redemption calls are the controlled consumer's server-side legs, with native callback/completion in between. Wrong-verifier proof is isolated on a live ticket rather than masked by prior consumption.

Cookies are host/path scoped, **not port-isolated**; the two localhost ports deliberately share that host. SameSite observations apply to this Edge version and these navigation types; ambient third-party-cookie policy may also restrict the resource request. No universal browser/CSRF certification follows.

Final DOM/proof exclusion and redaction assertions pass; retained native records contain booleans, counts, statuses and attributes, not raw cookie/ticket/token/verifier values. The transient code query at the registered callback remains expected protocol behavior, covered by redaction and no-referrer handling.

**A-07: H1_AUTH_PASS under D31. A-08: H1_AUTH_PASS for backend and controlled-consumer cancellation/replay obligations.** Actual Next prelogin/session persistence, CSRF, refresh singleflight, generation/logout fencing, Node restart, UI/a11y/visual and production integration remain **DEFERRED_H1_F**, with their separate executable/Owner gates. The test consumer does not certify those deferred capabilities.

## Retained acceptance and evidence limits

AUTH-FR-01–10/12 and AUTH-L01/L02 remain CLOSED. A-01–06/A-09/A-10 retain prior acceptance, supported by current core **50** and unchanged source-qualified evidence. Re-review 3's legacy Authentication **89**, shared architecture/Portability **53** (38 + 15), and Drive isolation **36** are **178 reused passes**, not fresh execution in this review. The old three-case random-port browser selector is superseded by the current ten-case native proof. The prior **231** remains a historical run; neither it nor the combined accepted coverage is presented as a fresh aggregate.

V28-only schema authority, singleton immutable issuer/sub ownership, fresh-password LINK, post-lock TTL/fencing, atomic ticket/refresh issuance, fixed provider validation/bounded transport, strict production localhost:8080 authority, root ApiResponse under ADR-0016, Portability exclusions and default-OFF normal configuration retain their reviewed source identities. Normal Google Sign-In/Drive/Drive Files/Feed environment flags are all **unset** at independent entry. No production source is changed by this review or the two-file takeover.

Build output is reviewed rather than claimed warning-free. Non-blocking observations include Maven metadata HTTP 401 from configured Flyway repositories, existing Flyway duplicate-index notice, SpringDoc test-context notices, future Spring inherited-test-configuration behavior and Mockito/JVM dynamic-agent warnings. Expected missing-client configuration failures are negative-test outcomes. The build and all assertions pass; no repository credential/configuration change or blanket suppression is introduced. No fresh IDE-wide analysis, CVE/SBOM audit, full backend suite, benchmark, real Google/Cloud/HTTPS/ACL or deployment proof is claimed.

## Proportionate shared coverage and Docs Sync

All 29 dimensions were considered; unchanged independent evidence is reused where source-applicable.

| Dimensions | Result / evidence and limits |
| --- | --- |
| 1–2, 10 intent / traceability / business-state | **PASS** — exact FR-11 scope, D31 precedence, fresh native/core state transitions and complete H1-AUTH acceptance allocation. |
| 3–6 architecture / ownership / tree / shared contracts | **PASS** — recursive 100-path comparison, complete two-file inspection, existing seams and module encapsulation; ADR-0016 transport and prior architecture 38 retained. |
| 7–9 SOLID / principles / pattern fit | **PASS** — cohesive controlled test consumer and passive observer reuse existing runtime ownership; no speculative interfaces, framework or production abstraction. |
| 11–14 wire / input / auth / credentials | **PASS** — real strict Host/callback/303/cookie proof; fresh negative admission/live-ticket tests and unchanged validation/crypto/provider/redaction evidence. |
| 15–18 atomicity / recovery / DB / resources | **PASS** — real PG core 50, bounded browser/consumer lifecycle, late-token revoke and released ports; unchanged V28/locking/rollback/transport/resource proofs. |
| 19–20, 24 RAG / document fidelity / evaluation | **N/A** — no model, retrieval, indexing, ingestion, OCR or scientific evaluation capability changed. |
| 21–22 security / test quality | **PASS** — native Edge jar manipulation, no-provider/no-session denials, exact live-ticket sequence, deterministic cancellation barrier, 60 fresh nonempty JUnit cases and real PG; deferred Next proof explicitly excluded. |
| 23 benchmarking | **N/A** for new benchmarking — no new hot path or performance claim; existing finite resource proofs remain source-qualified. |
| 25 dependency lifecycle | **PASS** for this delta — no new dependency/runtime/install; prior managed client starter unchanged. No fresh vulnerability/license certification. |
| 26 build / diagnostics | **PASS** for executed bounded build/tests and retained architecture verification. IDE-wide diagnostics **NOT_VERIFIED**, outside the mandatory slice gate; warnings triaged above. |
| 27 logging / observability | **PASS** — safe boolean/status/hash receipts, redacted DOM/stderr and no-proof DOM assertions; existing secret-redaction behavior unchanged. |
| 28 docs / governance | **PASS** — current independent closure/status/counters/next action synchronized; prior decisions, blocked records and implementer self-review retained as history. |
| 29 hygiene / delivery | **PASS** — reviewer source guard, ignored unique evidence, owned cleanup, empty index, scoped links/whitespace checks and review/status-only writes. No publishing. |

**Docs Sync Check:** **UPDATED** — this independent report and ACTIVE/executable current review/result/status/counter/Next step only. **ALREADY_CONSISTENT** — D31/initial authority, submitted source hashes, prior closures, package/API/ADR/DB inventory, H1-B publication and normal defaults. **HISTORICAL** — original :8080 blocker, earlier random-port results, dated decisions/reviews and implementer handback's pending-review state; they are preserved, not rewritten as successes. **DEFERRED_OUT_OF_SCOPE** — H1-F/actual Next/UI/CSRF/session/visual/a11y, live Google/Cloud/deployment and unrelated stale routing/LOW cleanup. Final saved-file/link/hash/whitespace/index checks are retained under L; document integrity is not substituted for runtime evidence.

**Next step:** Owner | normal Git add/commit/push of the accepted delivery, then brief slice publication closeout | GDI26-S3-H1-AUTH; this review and ACTIVE | READY_FOR_OWNER_COMMIT; verify actual publication before closeout, separate H1-F handoff remains required | Antigravity 3/3 exhausted / accepted / STOPPED; SAME Codex test-only takeover 1 independently accepted / STOPPED.

