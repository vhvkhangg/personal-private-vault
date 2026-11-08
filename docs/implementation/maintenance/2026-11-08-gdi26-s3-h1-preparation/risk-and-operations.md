> **Current PH-03/06/07 risk decision, 2026-11-08:** Search overrun is prevented by 2-root+1-list+≤5 remote child proofs per bounded request, with no metadata spill buffer. Google Sign-In transaction integrity is Spring-owned (one-use state+cookie+nonce/PKCE) and login handoff is protected by independent BFF verifier plus atomic PostgreSQL ticket consume/token issue. Single Owner binding requires fresh legacy password and preserves existing password emergency API; **Google account replacement/rebind is NOT supported** by H1. See [repair ledger](ph03-ph06-ph07-repair-2026-11-08.md). These are prospective risk controls, NOT_RUN.

# H1 risk ledger — v0.4, all IDs unique

These are prospective risks/controls, **not evidence of existing vulnerabilities**. D25=A and desktop v0.3 remain accepted; Owner D28 **delegated minor states** and D29 **approved the v0.4 bounded backend/BFF scope**; independent review remains pending. D26 Google Sign-In requires new security analysis. Actual app/provider/browser tests NOT_RUN.

| Risk ID | Concrete risk | Required bounded control / future evidence |
| --- | --- | --- |
| H1-R01 | Approved visuals outpace root-only S2 API | H1-B prospective scope/real data first; [capability gap](capability-gap-and-decision-gate.md) |
| H1-R02 | S1 cookie lost across BFF → browser → Spring | Allowlisted exact Set-Cookie relay, real same-host/port/browser positive + negatives |
| H1-R03 | Concurrent refresh/revoke revives invalid session | One singleflight per session and atomic generation/logout barrier, backend opaque token never in browser |
| H1-R04 | Login or action CSRF/direct route bypass | Prelogin+auth CSRF, Origin/Host, direct action/route auth, negative tests |
| H1-R05 | Transfer full buffering or false download success | Streaming/caps/backpressure/expected length, abort/truncated tests |
| H1-R06 | Late slot 1 projection appears under slot 2 | Generation/slot-bound state/cursor/cancel/in-flight negative tests |
| H1-R07 | Glass/focus/reflow failures on 14-inch laptop and extreme desktop zoom | Solid fallback and contrast calculation, 1366×768/1536×864, 320 CSS-px zoom reflow/manual keyboard & axe later; mobile design deferred |
| H1-R08 | Frontend outdated security patch or external installer | Pinned Node/Next compatible set, one lockfile, dependency audit; no blind installer |
| H1-R09 | Agent materially alters Owner-approved main Explorer or invents v0.4 approval | SHA-bound Owner v0.3 for core; D28 delegated auxiliary states, independent quality review; no mobile requirement |
| H1-R10 | Testing accidentally activates live Google or Feed | Flags OFF in owner runtime; fake server tests only under distinct scope |
| H1-R11 | H0 LOW calls opaque refresh a JWT | Preserve H0 FR-L01 LOW/OPEN; H1 terms accurate; later minimal guidance correction only with scope |
| H1-R12 | Frozen module/Phase 16/RAG drift | Strict H1-B/H1-AUTH/H1-F allowlists, S1/S2/P17–19 histories, no broad phase activation |
| H1-R13 | Lost child-folder create reply causes duplicate | Durable pre-reserved ID, one dispatch, exact GET reconciliation, unknown kept forever until adjudicated |
| H1-R14 | Foreign/moved child folder accepted by name/ID | Completed root-affine ledger + current remote parent+markers, sub/epoch fence |
| H1-R15 | File upload key replays against different child | Immutable target parent fingerprint, 409 conflict, V26 root-only historical identity retained |
| H1-R16 | Prefix-only provider search omits substring hit | App-private root-binding pages + local NFC casefold and cursor completeness; no false terminal success |
| H1-R17 | Redirect/thumbnail URL leaks Google bearer or HTML/SVG to browser | Exact allowed Google origin, no redirects, 1 MiB MIME/signature constraints, fallback otherwise |
| H1-R18 | Quota conflates Google Drive usage with shared total | Distinct `usageInDrive` / `usage` / `limit` nullable and clear labels; no invented capacity ratio |
| H1-R19 | More folders than 8-call proof budget / cursor exhausted | Verify at most six distinct children/request, buffer one page and return continuation, explicit incomplete errors |
| H1-R20 | Durably stored new Drive table blocks Portability | Add table to excluded security inventory and PG regression; no unclassified table |
| H1-R21 | H1 main visual approved, auxiliary screens delegated | D28 explicit Owner delegation, Codex uses harness/tests; no additional Owner state-sheet approval, material core redesign still requires Owner sign-off |
| H1-R22 | Google Sign-In account takeover via unbound Drive slot or email-only association | Bind one verified Google OIDC `sub` to pre-existing singleton PPV Owner under fresh legacy proof; refuse other Google subjects and no auto-created PPV user; independent auth proof |
| H1-R23 | Google callback forgery/replay/redirect mix-up with S1 Drive | Separate login state/nonce/PKCE, issuer/audience/exp/token signature checks; distinct callback from S1; server-only token custody and CSRF-safe start/link |
| H1-R24 | Lost/replaced Owner Google identity makes PPV unrecoverable or attacker rebinds | Explicit Owner recovery and binding/rebinding policy with fresh proof; no anonymous or email-only recovery; stop for Owner decision if essential |
| H1-R25 | Premature deployment Google Sign-In/Drive activation | Dedicated sign-in flag default OFF and distinct real Cloud/client/callback readiness; Drive/Files/Feed OFF; no Google call to pass deterministic tests |

**Evidence semantics:** H0 unit/native evidence is historical H0 proof. All H1 backend migrations, frontend toolchain, BFF/CSRF, Google live thumbnail, storage quota, a11y/performance are NOT_RUN or NOT_VERIFIED. Do not turn an accepted preparation into implementation permission; only Owner can publish and accept visuals. Logs and previous failed review preserved. No changes to `.codex`, hook trust, defaults, provider OAuth scopes or runtime deployment.
