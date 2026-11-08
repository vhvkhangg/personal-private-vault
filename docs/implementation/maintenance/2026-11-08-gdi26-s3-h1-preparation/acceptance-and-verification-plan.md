# H1 finite acceptance: design gate vs future implementation tests

**Current result:** every H1 runtime case `NOT_RUN`; no `frontend/`. Desktop Explorer v0.3 is explicitly Owner-approved; the Owner **delegates auxiliary states** and **defers mobile** (D27/D28), while accepting bounded H1-B/BFF scope (D29). The new Google Sign-In identity/authentication design needs independent review. Each future case requires actual receipts/screenshots/source-qualified tests, never model narration in place of evidence. `PASS` below means a *future criterion*, not today's result.

## D-01 through D-08 — pre-implementation visual/preparation criteria

| ID | Scenario | Required proof |
| --- | --- | --- |
| D-01 | Preserved Owner decisions and H0 baseline | D00–D22 traced; H0 actual publication; ACTIVE no handoff before H1 |
| D-02 | Desktop design | Approved v0.3 content-area grid/cards and two account selectors; previous D14/D21 sidebar direction preserved separately; no dead primary Drive module |
| D-03 | **14-inch desktop** and browser accessibility | Primary 1366×768 CSS px + secondary 1536×864 CSS px; no mobile visual deliverable. Keyboard/focus/200% zoom and 320 CSS-pixel zoom reflow require truthful tests/disposition for H0 PH-I01. |
| D-04 | **Codex-delegated** state matrix | D28 Owner grants Codex design discretion for Google login, connection/loading/empty/unknown/error/transfer/preview states under v0.3 and H0 harness. Previous v0.4 sheets are **historical, not Owner approval gates**; require state checklist and independent quality review. |
| D-05 | Token/typography/accessibility plan | [Selected candidate semantic tokens and calculated opaque-pair ratios](h1-ph01-visual-derivative-v0.4.md); actual rendered glass contrast, reduced motion, focus/touch and 320px reflow are H1-F runtime checks |
| D-06 | Feature/API truth | Approved quota, search/type/date, safe thumbnails, scroll and folder menu must be real or explicitly marked unavailable; D25=A recorded; obtain independent bounded Drive API extension review. No whole-Drive claims |
| D-07 | Owner version-bound main visual approval | **Owner has approved v0.3** with SHA `1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e`; **D28 explicitly delegates minor states**; no additional owner sign-off required for v0.4. Material changed primary visuals still need new Owner approval. |
| D-08 | Independent preparation review | Verify actual D26–D29 Owner instruction, scope-approved v0.4 backend/BFF, delegated states, desktop-only test boundaries, and **new Google Sign-In backend owner mapping**. Codex must independently adjudicate PH-01–05 and any new identity/auth blocker; `READY_FOR_HANDOFF` only if resolved. |

## H1-01 through H1-20 — future runtime acceptance, only after executable handoff

| ID | Acceptance behavior / required negative |
| --- | --- |
| H1-01 | **H1-F frontend slice only:** only reviewed frontend paths/versions/lockfile; no backend/module/schema/Drive flags/Phase 16 expansion. H1-B has separately enumerated Drive-only/V27/Portability scope; H1-AUTH has separately reviewed Spring Google identity/migration scope |
| H1-02 | TypeScript strict, formatting/lint, pinned package/security audit and production Next build with recorded actual versions |
| H1-03 | **Google Sign-In**: login state/nonce/PKCE/callback, singleton Owner `sub` binding, server-side Spring JWT/opaque refresh; unbound or wrong-sub Google account denied. No Google token browser exposure/cache. |
| H1-04 | BFF direct route/action unauthorized access denied, including private RSC/serialized data/cache |
| H1-05 | CSRF/malformed Origin/Fetch Metadata/fixation/refresh race/replay/expiry/logout tests |
| H1-06 | Real-browser exact S1 same-host/path/300s cookie positive and absent/wrong/expired/replaced binding negative |
| H1-07 | Slot 1/2 connection/status/reconnect/disconnect without stale account mixing; no actual Cloud required for deterministic gate |
| H1-08 | Existing root ensure remains singular, with pending/unknown/not-ready; D25=A new direct-child folder creation is independently gated, not a generic folder CRUD or grandchild permission |
| H1-09 | Authorized scoped list/search/type/modified-date API semantics, Unicode/escaping, date-boundary and cursor-filter binding; no broad My Drive results; no data omitted just because it lies on another page |
| H1-10 | Upload 201/200, zero-byte, exact 50 MiB and over-cap/request limits, invalid name/MIME/Workspace rejection |
| H1-11 | Missing/invalid/stale/same-key-different-body/in-progress upload action with exact status mapping |
| H1-12 | Lost response/unknown action: retain same key+content for explicit retry, never automatic new remote mutation |
| H1-13 | Streaming bounded memory/backpressure/cancel/timeouts through BFF; not whole-file JS buffering |
| H1-14 | Download expected length/no-store/nosniff/name; short/excess/truncated/canceled paths do not report saved success |
| H1-15 | Approved desktop grid/cards, lazy authorized previews/fallback, account quota/unknown, all loading/empty/connection/error/unknown UI tested with user events and keyboard focus |
| H1-16 | **Desktop 14-inch only:** 1366×768/1536×864 real viewport, no clipped controls/horizontal scroll; 200% desktop zoom and 320 CSS-pixel reflow as accessibility evidence (not a mobile visual) with explicit PH-I01 disposition. |
| H1-17 | Automated axe + manual keyboard/focus/contrast/reduced-motion checks targeting WCAG 2.2 AA, precise limitations |
| H1-18 | Main Explorer compared with Owner-approved v0.3 exact hash; justified minor differences recorded. Codex-delegated secondary states independently reviewed against harness (not new Owner-approved images); no material main-view drift. |
| H1-19 | Deterministic Playwright E2E with fake provider/backend: Google sign-in/auth/slots/list/transfer/error; no credential or live Google claims |
| H1-20 | Watchdog receipts/cleanup, implementation self-review, separate Codex final review, approved design still applicable, Owner-only Git publication |

## Additional new acceptance cases required by D24 (not yet supported by S2)

| ID | Requirement / negative proof |
| --- | --- |
| H1-21 | Slot 1/2 account storage-quota values are queried with current authority, accurately labeled (total account vs Drive usage), switch cleanly; absent/unlimited/failure never show invented values |
| H1-22 | Name-search matches eligible files across page boundaries with Unicode/case behavior defined; type and modified-date inclusive endpoints work with timezone/DST limits |
| H1-23 | New filter/search/slot invalidates old cursor and in-flight results; malformed criteria and swapped cursors fail closed; no broad My Drive search |
| H1-24 | Card thumbnail is real and authorized **when safe provider thumbnail exists**; missing/untrusted/unsupported returns type-icon fallback; browser never receives provider credentials/thumbnail URLs; no local PDF/DOCX parsing or universal-preview promise |
| H1-25 | Scroll appends finite cursor pages with no page numbers/count footer, stops at `hasMore=false`, handles empty page with cursor, repeated trigger and cancellation without unbounded requests |
| H1-26 | `Mới` offers upload file and **D25=A new direct child folder**; root/child parent identity, one-level maximum and unknown outcome enforced; no unauthorized local-folder bulk upload/delete/rename/move/share/sync |
| H1-27 | Approved screenshot's core interaction hierarchy survives accessibility/performance improvements and visual diff is documented; test actual screenshot against its SHA-bound reference |

All future tests remain `NOT_RUN` until a distinct accepted implementation handoff. A visual approval is not a backend test.

## Test technique and budget

Use existing `.agents/skills/frontend-testing/SKILL.md`, `scripts/run-bounded-command.py` normal profile <=300s per command, fresh unique ignored `logs/` receipts, no Node/Playwright extended-profile waiver. Prefer Vitest + React Testing Library for components and actual browser E2E (Playwright) for async Server Components/S1 cookies. Add axe and manual accessible reflow/keyboard/contrast tests; screenshot success is *not* Owner approval. Test private data without real tokens; redaction and safe fixtures. No dependency install/test run authorized by this preparation.

Official references: [Next.js Vitest](https://nextjs.org/docs/app/guides/testing/vitest), [Next.js Playwright](https://nextjs.org/docs/app/guides/testing/playwright), [W3C Reflow SC 1.4.10](https://www.w3.org/WAI/WCAG22/Understanding/reflow.html).

## H1-28–35 — additional first-level folder and backend-extensibility gates (NOT_RUN)

| ID | Concrete acceptance / negative case |
| --- | --- |
| H1-28 | Newly-created child folder has immutable ID, exact parent=root, folder MIME, root/subject/provenance markers and durable creation action; spoofed name, external folder, wrong root, slot or epoch fails closed |
| H1-29 | Two concurrent same-key folder requests or provider create success with lost response converge on **at most one** reserved child-ID; retry reconciles exact ID, never creates again; uncertain remains uncertain after 404/timeouts |
| H1-30 | One-level `root → child` navigation works; grandchild creation/traversal, manually added folder, moved child, path traversal and cross-account parent are rejected; disconnect/reauthorize fences old state |
| H1-31 | Authorized original file upload/list/download inside a verified child work with S2 limits; mutation fingerprint includes parent; same key with changed parent conflicts; root-only S2 regression suite remains applicable |
| H1-32 | Full account-subtree filename search has verified **complete and bounded** scope; test name prefix-vs-substring, Vietnamese Unicode and matches in unloaded child pages; never claim client-page-only search exhaustive |
| H1-33 | 1–50 caller-limit folder/file results (**effective search provider pages ≤5**, with 2 root proofs + 1 list + ≤5 child GETs = **≤8** actual provider calls per request), and opaque cursor isolation across slot/parent/query/type/from/to/timezone; empty page with continuation, stale token and nonterminal resource budget tested |
| H1-34 | Per-slot `about.storageQuota`: `usage` vs `usageInDrive`, absent/unlimited/organizational limit and null/error presentations are accurate; never show fake quota or count |
| H1-35 | Genuine authorized and lazy previews where available with strict server-origin/proof/bytes/MIME/timeout rules; unavailable provider thumbnail => clearly marked fallback, no raw Google URL/tokens or XSS/SSRF; no claim that all formats always have images |

The independent pre-handoff reviewer should source-qualify acceptance for changed backend files (if any later H1-B handoff is approved) and not collapse H1 backend tests into frontend screenshot counts. Current H1 source/browser tests **NOT_RUN**.

### H1-AUTH source-specific criteria — PH-06/07 (all NOT_RUN)

The [Spring-owned selected contract](h1-google-sign-in-owner-identity-contract-v0.5.md) is binding for prospective A-01–A-10; do not combine Google-signature validation with one-use browser transaction proof into one assertion. In particular: two simultaneous/duplicate direct `POST /api/v1/auth/google/handoff` requests with the same ticket+verifier yield **one** or zero PPV token issuance, never two; direct raw Google ID-token POST cannot login. The BFF completion reads a **cookie**, not a credential-bearing URL; Spring owns code exchange and one-use stored nonce/state, BFF owns independent raw verifier and generation fence. Fresh legacy password and PPV bearer are both required at LINK start; JWT age alone is not proof. V28 identity/attempt/ticket security exclusions and original password/refresh/PIN/Drive S1 regressions are mandatory; existing login/Drive flags remain OFF by default.

The source-qualified search regression requires at least six distinct child proofs across sequential <=5-candidate pages; each HTTP operation actually dispatches two root checks, one list and <=5 child GETs: **max 8**. Stable eligible late-page substring results must be returned with valid continuation. No buffered candidate row is carried over an operation boundary.

## Selected H1-B independent acceptance ownership — PH-02–04 (all NOT_RUN)

The H1-B backend executable handoff must name exact affected source files and test paths, including prospective V27 and Portability-only security exclusion. It does **not** inherit H1-01 (which applies only to H1-F). Tests/receipts must independently establish:

| Gate | Mandatory future evidence |
| --- | --- |
| B-01 | V27 PostgreSQL FK/unique/CHECK/status/nullable-parent migration and rollback-safe startup; applied V26/frozen v1 unchanged |
| B-02 | Concurrent same-key child folder create, reservation before dispatch, 404/timeout/restart unknown preserved, no second create or unreviewed cleanup, limits 256 total/8 nonterminal |
| B-03 | Root-child exact parent/provenance, foreign/moved/trashed/shortcut/grandchild denial, disconnect/sub/epoch fences |
| B-04 | Child file upload target parent in immutable fingerprint/DB/provider; key replay different parent conflicts; existing S2 root 50/55 MiB and root-only regression still pass |
| B-05 | Complete scoped `files.list` app-private predicate, server Unicode substring and type/date filters, unloaded pages, empty continuation, incompleteSearch, poison/expired cursor, **≤8 calls/request counting TWO root authority requests**, <=5 provider entries/proofs per operation, no buffered candidates |
| B-06 | Exact thumbnail hostname/port/no-redirect rule and zero bearer dispatch to other hosts; MIME/signature/1 MiB/timeout/abort, safe fallback, root+child+slot isolation |
| B-07 | Per-account quota `usage`, `usageInDrive` and absent/pooled `limit` with no fabricated percentage |
| B-08 | Portability inventory includes new Drive ledger as EXCLUDED_SECURITY_TABLES; Modulith/package boundaries, exact HTTP route inventory, flags default OFF |

These require fake provider + Testcontainers PostgreSQL + focused HTTP/security/architecture regression after separately authorized H1-B implementation. A source document alone is not a PASS.

## Selected H1-F BFF and visual acceptance ownership — PH-01/05 (all NOT_RUN)

| Gate | Mandatory future evidence |
| --- | --- |
| F-01 | Anonymous prelogin and authenticated CSRF token with wrong Origin/Host/Fetch Metadata denials; no private mutation under unauthenticated direct Handler/Action |
| F-02 | Rotation/fixation and two-tab singleflight refresh, logout vs in-flight refresh/expiry/restart generation fence; backend JWT + opaque token never serialized/stored in browser |
| F-03 | Exact localhost:3000 → Spring localhost:8080 binding-cookie delivery in actual browser with absent/replaced/wrong/expired negative cases; original text callback unchanged |
| F-04 | **Desktop 14-inch** 1366×768 / 1536×864 keyboard/focus, text resize and practical 320 CSS-px reflow/actual glass contrast against approved main v0.3; no mobile UX design or derivative sheet approval; PH-I01 measured, not assumed |
| F-05 | Screenshot diffs preserve Owner-approved populated desktop v0.3; Codex-designed auxiliary states are delegated by D28 and independently checked against H0 harness, not deemed Owner-approved v0.4 image; document justified minor differences |
| F-06 | Slot-switch late-result isolation, no fake quota/preview, known/unknown upload and download failure truth, infinite scroll stop/cancel without extra pages footer |
| F-07 | Exact pinned compatible toolchain/lockfile, strict type/lint/format/build, component/integration/Playwright/axe, bounded watchdog raw receipts with NOT_RUN/FAIL truth |

**Current owner decisions:** [D26–D30](owner-decision-addendum-v0.5.md) approve v0.4 backend/BFF scope and delegate minor states; **Google Sign-In** is new material authentication scope requiring an independent review before readiness. Historical [v0.4 docket](owner-approval-docket-v0.4.md) is superseded.


## H1-36–42 — Google Sign-In and approved desktop scope (FUTURE TESTS, NOT_RUN)

| ID | Mandatory acceptance / negative |
| --- | --- |
| H1-36 | Google Login OIDC **separate from Drive S1**: `openid email` only, Google login callback has separate host/path; no implicit `drive.file`, no auto-login by Drive slot status. |
| H1-37 | Single bootstrapped Owner binds exactly **one verified Google `sub`** after fresh owner reauthentication; email match, arbitrary nonlinked Google subject and slot-2 subject never grant PPV access. No user auto-creation. |
| H1-38 | Server verifies Google ID-token signed `iss`/`aud`/`exp`/`sub` and browser-bound one-use `state`/`nonce`/PKCE; forged/expired/wrong client/nonce, replay and direct login route fail closed. |
| H1-39 | On success **Spring issues canonical PPV JWT/opaque refresh**; BFF stores them server-only and rotates cookie+CSRF. Normal website login is Google; existing password API remains for controlled bootstrap/recovery but is not ordinary UI login. |
| H1-40 | Multiple tabs/callback races, Google error/provider timeout, logout-in-flight, refresh vs logout, OAuth session fixation and reconnect correctly denied/fenced; no identity substitution on Google Drive slot switch. |
| H1-41 | 14-inch desktop 1366×768 and 1536×864 usable with account switch, real quota, name search, type/From/To, one Mới menu, card previews/fallback and infinite scroll; no separately designed mobile UI in scope. |
| H1-42 | Three **separately scoped** backend/frontend reviews/handbacks (H1-B Drive, H1-AUTH Google login, H1-F Explorer), versioned Portability/Flyway checks, no default-ON flags, and deferred live Google/deployment proof. |

Google OIDC server/token trust and one-time Owner binding are **new high-impact design obligations**, not retroactively implied by accepted v0.4 BFF scope. Review before producing executable handoffs; no CURRENT PASS claims.
