# BF26-DP — test-harness readiness and finite security verification plan

## Review-driven harness clarification — PH-01–04 (Owner D1–D6 ratified; no new toolchain)

- **A:** extend existing `AuthWebIntegrationTest`, `SecurityFilterChainIntegrationTest`, `AbstractWebIntegrationTest` and real PostgreSQL/Flyway integration fixtures to mint **signed, persisted** PUBLIC/PRIVATE tokens. No `@WithMockUser`-only or unprofiled JWT shortcut for security claims. Exercise Public from both login/downgrade, unknown paths, async/ERROR redispatch, DB unavailable, lost reply and refresh/switch lock-order barriers. Positive Private control and negative Public no-bytes/side-effects required.
- **A + Portability:** same Flyway version as new security tables, `PortabilitySnapshotAdapter.EXCLUDED_SECURITY_TABLES` co-edit plus owning integration/strict schema-inventory tests; validate ZIP complete and new security table rows/secrets absent, restricted full DB backup separately documented (not simulated as ZIP). Real PostgreSQL, no H2.
- **B1:** owner-local non-NSFW Fiction/Film safe projection tests (parent/known ID, hidden/absent indistinguishable, denied nested credits/person IDs, explicit Reference GET allowlist, no Public write before B2).
- **B2:** guarded writer/reference tests and Search/Vault tag/credit candidate filtering **pre-ranking/pre-offset/hasMore**, with synthetic mixed NSFW/private dataset. Do not check only post-limit output. No reverse repository imports.
- **Google:** existing fake-provider/one-use ticket tests with feature flag ON must prove denied unprofiled/PRIVATE token issuance before profile mapping; preserve immutable identity and actual default OFF. Do not require real Google/Drive connections.
- **Browser:** pause H1-F code; later BFF SID/CSRF/cookie/cross-tab caching proof separate and not satisfied by Java MockMvc. Future browser cancellation must honor the D3 explicitly accepted pre-admission/in-flight limit.
- **Old fixtures:** update exact token issuance helpers with persisted lineage and profile; do not accept green suites via weakened filter, test-only permitAll, arbitrary accepted profile claim or H2 DB. No new agent/skill/global harness; bounded command runner remains required for implementation tests only.
- **G-AUTH — exact matcher compatibility:** parameterize [selected DP-A request table](auth-compatibility-and-session-lifecycle-2026-11-11.md) with ANON/valid-PUBLIC/valid-PRIVATE, method/path, authenticated proof, successful DTO/envelope, and 400/401/403/409/422/503. Cover legacy `/login` with mandatory Owner `identifier` as PRIVATE, and **PUBLIC `/profiles/login` with only `{profile:"PUBLIC",password}` and no identifier**; correct configured Public password -> 200 PUBLIC lineage, wrong/unconfigured -> same 401, `profile:"PRIVATE"`/extra `identifier` -> 400. Also cover anonymous JSON-body `/refresh` and `/revoke`, protected PIN, Public credential setup **and reset**, no wildcard `/auth/**`, health/docs/conditional Drive callback, and Google flag OFF vs ON/unmapped `start/link/start/callback/handoff`. Validate blocked LOGIN start/callback fixed failed 303/cookies, successful PRIVATE fresh-password LINK start/callback immutable binding and 303 ticket, and unmapped handoff **no ticket consumed/no token issued**; do not replace Google OIDC source proof with mock Identity.
- **G-SWITCH — lifetime/lineage/reset:** with signed JWT+persisted PostgreSQL session, verify `access.exp=t+A`, `currentRefresh.expiresAt=session.expiresAt=t+R` on login, successful rotate and switch; ordinary GET does not extend expiry; no fixed first-login cap/idle timer. Barrier test **rotate→revoke** (revoke old consumed predecessor = no-op, successor remains) and **revoke→rotate** (rotation fails). Test PUBLIC password reset vs refresh/elevation both serializations: epoch fence invalidates any ever-Public lineage, including PRIVATE-after-elevation, while PRIVATE-only independent sessions remain usable; invalid proof leaves state unchanged. Test current-generation revoke, Public-unconfigured downgrade 409/no mutation, same-profile request denial, lost switch reply and old-session 401. Session/DB failures fail closed; already-admitted vs post-commit read/write/async match D3. No arbitrary sleeps or tests/runtime in preparation.



**Status:** `EXISTING_HARNESS_REUSE / OPTIONAL_TEST_ONLY_HELPERS_PROPOSED`. The Owner asked for harness where necessary. Current evidence does **not** justify creating a new integration harness, backend agent, plugin, hook, tracing service, Docker Compose suite or frontend preview launcher during **preparation**. Real login/profile implementation is unapproved, so a preemptive executable harness would be speculative. This document specifies exactly which existing tools suffice and when a narrow helper should be added to an authorized source slice.

## Existing tools and evidence (observed, not rerun)

| Tool/test source | What it can establish when authorized | Limit / adjustment |
| --- | --- | --- |
| JUnit 5 + Spring Boot/MockMvc + AssertJ | Direct `/api/v1/auth`, route 401/403/404, JWT/DTO/error envelope and known-ID negative calls | Need explicit two-profile authentication/session fixtures; no claim current tests exercise profiles |
| Testcontainers PostgreSQL + Flyway/JPA | V28→proposed additive migration; real row locks, refresh replacement, race/rollback and query filtering | **No H2 substitute**; no migration run during planning |
| Spring Modulith architecture tests | No illegal cross-module Java import/entity/repository access | Architecture checks cannot prove HTTP/business authorization themselves |
| Existing Vault/Search/Fiction/Film/Media/Portability integration suites | NSFW/bytes/export/metadata/filter-before-pagination across real owner-source and SQL boundaries | Need Public and Private fixtures and more negative assertions, not a global test infrastructure rewrite |
| Existing Auth/Google E2E Java tests | One-Owner OIDC binding, one-use handoff and token issuance regression with fake provider | Do not activate real Google; new profiles must not silently change H1-AUTH accepted source scope |
| Existing `scripts/run-bounded-command.py` + evidence receipts | Owner-approved bounded execution, actual stdout/stderr/exit/source hashes and cleanup | Ordinary profile <=300 s; <=2100 s only for separately approved eligible full suite; no new runner bypass |
| H1-F Next/Playwright fixture | Later full browser cookie/multi-tab/CSRF and UI switch behavior | **PAUSED**; not usable to claim Backend acceptance or justification to resume H1-F |

Existing guidance: `.agents/skills/authentication-security`, `database-migration-review`, `backend-testing`, `rest-api-http-contracts`, `architecture-change`, `bounded-command-execution`, `codex-review-coverage`, `codex-create-handoff`, `antigravity-implement-handoff`, `antigravity-self-review`, `codex-final-review`. No new skill/agent/hook is justified now. Historical closed tests can be source-qualified reuse, not reported as freshly run.

## Minimal test-only helpers if later proven necessary

Only as part of an **Owner-authorized DP-A/DP-B implementation slice** and under a precise test allowlist:

1. **`ProfileAuthFixture`** (test source only): generate distinct Public/Private credentials from in-memory test inputs, bootstrap singleton `app_users.id=1`, explicitly enable Public profile in test scope and request login/session token pairs; never persist raw passwords into fixture dumps/logs.
2. **`ProfileSessionRaceFixture`** (test source only): deterministic latch/barrier and test clock for simultaneous refresh/elevate/downgrade/logout; no arbitrary sleeps, no new network service or global process hook. Use transactional PostgreSQL and bounded timeouts.
3. **`VisibilityDatasetFixture`** (test source only): Public and NSFW Fiction/Film, Media/Personal Private records, indirect links/tags, mixed search rank/offset and trash status, and identity-guessing test IDs; no actual personal/media provider data.
4. **`AuthzRouteMatrixTest`** (test source only): parameterized route/method/profile expected outcomes. Start from the BF26-S5 static 254 declarations; include conditionally registered Google/Drive/RAG endpoints only when authorized flags and fixtures apply; do not assume all 254 are live simultaneously.

**Trigger rule:** Do not add a helper solely to meet a target line/test count. If existing integration tests can be extended cleanly, reuse them. Any helper that requires production hooks, relaxed Trust/CSRF/Host rules, a new backend module, a broad test-only security bypass or change to frozen deployment config is **REJECTED**; STOP for an explicit new Owner decision.

## Proposed execution matrix (future, not performed)

| Gate | Positive path | Negative/adversarial path | Evidence boundary |
| --- | --- | --- | --- |
| G-AUTH | distinct Public/Private login (**PUBLIC no identifier; PRIVATE legacy identifier required**), scoped refresh, separate sessions | wrong/unconfigured Public password both 401, PRIVATE branch on PUBLIC route 400, extra identifier 400, malformed data, token tamper, missing auth, login/Google bypass; correct PUBLIC with no identifier 200 positive | Auth/MockMvc + actual JWT decoded claims and persisted database session state |
| G-SWITCH | Private→Public no password, Public→Private with freshly verified password | stale Private JWT after downgrade, stale refresh, refresh-vs-switch races, logout resurrection | Two in-flight HTTP clients, test clock/barrier and real Postgres session locks |
| G-MIGRATION | V28→next Flyway version, data/FK/unique integrity, Public setup | Public default auto-enabled, accidental Private hash duplication, unprofiled legacy token/refresh privilege, failed migration | Real PostgreSQL, schema catalog and deterministic diff; no live user data |
| G-NSFW | non-NSFW Fiction/Film reads and permitted writes by Public; Private read all | ID leaks; nested Film credit/link; Public tries create/update NSFW; concurrent hidden-to-public mutation | Owning service+controller tests, real rows and rollback state |
| G-MEDIA/PERSONAL | Private media bytes/Personal views | Public raw bytes/Content-Length, thumbnails, count, read/write and predictable IDs | Actual HTTP media/Personal fixtures, zero disclosed body |
| G-SEARCH | Public safe results in stable order across pages | Private top-K/snippet/tag/cursor `hasMore` leak, filter-after-limit | PostgreSQL search candidate/qualification proof, no unbounded query |
| G-EXPORT/IMPORT | Private retained export/import semantics where approved | Public ZIP bytes/manifest counts, import previews and error/oracle leakage | Portability query tests and streaming failure gates |
| G-GOOGLE/DRIVE | normal feature flags OFF; accepted owner binding | Direct OAuth handoff to Private without password; Drive slot cross-use; profile-as-Drive-account confusion | Existing fake provider tests only as separately approved; no live Cloud |
| G-RAG | RAG inactive and accepted source consent invariants | Private profile used to bypass RAG NSFW exclusion or activate suspended QA | Static contract + focused existing tests where source touched; no QA/inference |
| G-ARCH/OpenAPI | API inventory, error envelope, Modulith dependency closure | accidental public route exposure, unregistered auth matchers, entity leak | `OpenApiRouteInventoryIntegrationTest`, `ApplicationArchitectureTests`; exact enabled profile |

## Resource/security discipline

- Every test scenario uses non-production secrets/synthetic data; no real Google, Drive, private user content or Internet service. Logs must not contain raw bearer/refresh/passwords, PINs, private snippets or uploaded bytes.
- Never weaken auth filters, cookie/Host/Origin/CSRF, native client trust, allowlists or accepted browser restraints just to make a fixture pass. Browser/BFF source stays untouched until separate Owner approval.
- Use bounded commands with unique receipts for implementer/reviewer; qualify reused results by source hashes. No whole-suite run is automatically mandatory for first code slice; run the focused relevant suites and separate selected cross-slice gate.
- Any advanced cross-request generation or request cancellation check should use the real application boundary under test, not mock away the session revocation decision. Prove that expired/unknown sessions fail closed on server errors.

**Harness verdict for preparation:** `NO_NEW_EXECUTABLE_HARNESS_REQUIRED`. Existing harness can be extended with bounded test-only fixtures **when** specific implementation slices are authorized. This verdict itself is a planning inference pending Codex independent confirmation, not a fresh test PASS.
