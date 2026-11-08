> **CURRENT D26–D30 / 2026-11-08:** Any pending request below for Owner sign-off on v0.4 derivative states or mobile is **superseded** by D27/D28. D29 accepts the bounded Drive/BFF scope. PH-03/06/07 remain technical-contract review topics, now proposed for correction in [repair record](ph03-ph06-ph07-repair-2026-11-08.md). Historical explanatory paragraphs are not new Owner questions.

# H1 capability gap / owner authority gate — updated PH-01–05 docket

**Source baseline:** published S2 and H0 on `main` at `cba57541e4f1fa35a2d799082766f9cb600bea8d`, H1 D25=A. These are **facts**: S2 supports one Google subject-bound PPV root, root direct-child binary listing, upload and download with `limit,cursor`, plus S1 connections; S2 has NO child-folder create, quota, server-side name/type/date filter, cross-subtree enumeration or thumbnail proxy. The illustrated desktop v0.3 is Owner-approved **design**, not working service evidence.

| Owner-required capability | Existing fact | Proposed smallest H1 path / review ownership |
| --- | --- | --- |
| First-level folders | No child registry or safe create | [H1-B authority/schema](h1-b-authority-schema-api-contract-v0.4.md) — one V27 child ledger; no adoption/nesting; Owner D29 accepted prospective scope; implementation remains separately gated |
| File upload/download within child | Root-only target/fingerprint | H1-B nullable immutable parent in existing intent and **separate child-upload route**; preserve legacy root compatibility |
| Search/filter/current account | Only root `limit,cursor` | [H1-B complete scoped search](h1-search-and-cursor-contract-v0.4.md) — app-private marker constrained pages; local Unicode substring; 8-call cap = two root + one list + at most five child GETs; no cross-request buffered candidates |
| Real account quota | No exposed metric | H1-B `about.get`; `usageInDrive` vs total `usage`, optional pooled `limit` |
| Preview images on cards | No safe route; link short-lived/private | [H1-B thumbnail trust](h1-thumbnail-security-contract-v0.4.md) — exact host, one-MiB safe image, **fallback**; all-format preview not guaranteed |
| BFF login/OAuth/session | No `frontend/`; Spring tokens cannot be browser-shared | [H1-F selected session/CSRF](h1-f-bff-session-csrf-contract-v0.4.md) — local host/cookies, anti-CSRF, refresh/logout CAS, actual browser proof later |
| Remaining design states | Only populated v0.3 approved | D28 **delegates** state design/review to Codex under H0 harness; v0.4 sheets are historical reference, not an Owner approval gate |

**D25=A, D27/D28 and D29 are resolved Owner decisions.** No new sign-off for historical v0.4 states/mobile is required. The bounded Drive/BFF scope is Owner-accepted as preparation; selected Spring-owned Google login transaction/immutable singleton binding and source-corrected search are **technical corrections pending independent review** ([repair](ph03-ph06-ph07-repair-2026-11-08.md)). No current H1 implementation authority follows.

**Acceptance gates:** H1-B Drive-only with focused provider/PG/Portability/architecture tests, final independent Codex review + Owner publish; then H1-AUTH Google login authentication and H1-F BFF/Explorer with type/lint/build/BFF/browser/axe/visual tests and its own independent review. H1 implementation requires published reviewed preparation, specific visual Owner approval, distinct READY executable handoff; H0 already accepted/published. `ACTIVE=NO_ACTIVE_HANDOFF`; no backend/frontend source, migration, cloud or feature flags modified here.


## Later Owner direction (D26–D29)

D25=A remains approved. Owner has now additionally **accepted** the bounded H1-B/BFF capability proposal (D29): 256 child folder identities/account, finite searching and thumbnail fallback. The new routine **Google Sign-In** requirement (D26) is **not** supplied by S1/S2 nor the existing Spring username/password endpoint; it requires separate [Authentication verification/binding](h1-google-sign-in-owner-identity-contract-v0.5.md). The browser is desktop 14-inch first; secondary states are delegated to Codex. No API or implementation exists merely by this record.
