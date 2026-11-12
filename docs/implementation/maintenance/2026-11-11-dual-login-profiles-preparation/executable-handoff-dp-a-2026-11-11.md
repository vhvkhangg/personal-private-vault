> **CURRENT BF26-DP-A PUBLICATION STATUS — 2026-11-12 (supersedes all earlier dated current notices in this file):** BF26-DP-A `INDEPENDENTLY_ACCEPTED / OWNER_PUBLISHED / CLOSED` at [`5c1da4e5`](https://github.com/vhvkhangg/personal-private-vault/commit/5c1da4e5404ee3c724e3fc2e03569d4093bcb44d); [independent final review](reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md), [publication closeout](owner-publication-closeout-dp-a-2026-11-12.md). FR-DPA-01–09 CLOSED; DPA-L01 LOW/OPEN/BACKLOG. Antigravity remediation 3/3 EXHAUSTED; Codex takeover 1/1 independently accepted. PUBLIC Auth-only/default DENY for all business/data paths, PRIVATE preserves previously accepted permissions; no live deployment/cutover verified. `ACTIVE = NO_ACTIVE_HANDOFF`; DP-B1/B2/C NOT_AUTHORIZED_FOR_IMPLEMENTATION. H1-F remains FRONTEND_PAUSED_BY_OWNER / NOT_INDEPENDENTLY_ACCEPTED / CHANGES_REQUESTED, FR-13 MEDIUM/OPEN/UPSTREAM_DEPENDENCY_BLOCKED; Phase 17 frozen, Phase 18 closed, S19-2 suspended, provider flags OFF/unset, historical evidence/counters preserved. **All earlier status notices in this file are historical checkpoints and not current authority.**

# Executable slice handoff — BF26-DP-A: Safe Authentication & Session Foundation

- **Handoff ID / created:** `BF26-DP-A` / Codex, 2026-11-11.
- **Status:** **READY_FOR_OWNER_COMMIT** after the [2026-11-12 separate independent takeover review](reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md). FR-DPA-06/07/09 CLOSED; prior closures retained. DPA-L01 LOW backlog remains. Source-qualified implementer final gates total 432 passing tests; fresh reviewer verification and preserved TIMEOUT are qualified in the report. No implementation publication is claimed.
- **Implementer:** Codex exceptional takeover; own self-review and delivery preserved in handback section 11. Original Antigravity implementation and three corrective/re-review rounds remain historical below.
- **Reviewer:** separate independent Codex `$codex-final-review` completed 2026-11-12; implementer self-review was not acceptance.
- **Entry/publication baseline:** local `main` HEAD and live remote `refs/heads/main` both verified at [`536a7aab8f48c2a182a8c5b88c1d248fdb29e283`](https://github.com/vhvkhangg/personal-private-vault/commit/536a7aab8f48c2a182a8c5b88c1d248fdb29e283). Worktree and index were clean; migration tip is V28, ADR tip is 0028. No branch/worktree was created.
- **Counters:** Antigravity implementation remediation **3/3 EXHAUSTED / STOPPED**; exceptional Codex takeover **1/1 independently reviewed / ACCEPTED**. No fourth Antigravity round, counter reset or automatic further takeover. Preparation/frontend/other counters unchanged.

## Exceptional takeover ledger — 2026-11-11

Owner authorization superseded the R3 stop for exactly FR-DPA-06/07 test/fixture evidence, FR-DPA-09 retained DP-A-only OpenAPI/inventory exceptions, G5 receipt/source applicability and affected docs. Codex implemented that exceptional delivery; the separate 2026-11-12 independent review now returns **READY_FOR_OWNER_COMMIT**. Antigravity **3/3 EXHAUSTED**, exceptional takeover **1/1 independently reviewed / ACCEPTED**. No fourth ordinary remediation, new production behavior or independent self-acceptance. The Antigravity/R3 result fields below are historical; handback section 11 preserves the implementer submission and section 12 records independent disposition. The original handoff creation clean-tree/publication checkpoint is historical; takeover entry had 72 received delivery paths and an empty index.

| Completed correction/re-review | Date | Immutable independent report | Remaining blockers at that checkpoint |
| --- | --- | --- | --- |
| 1/3 | 2026-11-11 | [re-review 1](reviews/2026-11-11-bf26-dp-a-independent-final-re-review-1.md) | FR-DPA-03/04/06/07/08/09 |
| 2/3 | 2026-11-11 | [re-review 2](reviews/2026-11-11-bf26-dp-a-independent-final-re-review-2.md) | FR-DPA-04/06/07/09 |
| 3/3 | 2026-11-11 | [re-review 3](reviews/2026-11-11-bf26-dp-a-independent-final-re-review-3.md) | FR-DPA-06/07/09 |

Corrective submission history remains in [implementation handback sections 1-10](implementation-handback-dp-a-2026-11-11.md) and each immutable independent re-review records the reviewed round and receipts. The implementer reused the same handback path across rounds; no separate byte-preserved R1/R2 implementer handback snapshots were found, and none are fabricated. The received post-R3 handback is byte-preserved in the takeover entry snapshot under `logs/bf26-dp-a/codex-takeover-1794408167866608000/entry/`. Initial review is not a remediation round. FR-DPA-01/02/03/04/05/08 remain CLOSED; the separate takeover review closes FR-DPA-06/07/09 without rewriting those historical decisions.

## Authority, precedence and outcome

This is an **Owner-approved maintenance slice**, not a numbered-phase reopening. The Owner's 2026-11-11 message headed **“Owner Authorization — BF26-DP-A: Safe Authentication & Session Foundation”** explicitly grants `OWNER_AUTHORIZED_FOR_DP_A_HANDOFF_AND_BOUNDED_IMPLEMENTATION`, at baseline `536a7aab…`, for **DP-A ONLY**. Its approved exceptions are Phase 2 Authentication; Phase 13 exact Auth APIs/security/OpenAPI; Phase 14 Portability security exclusions/tests; H1-AUTH Google fail-closed compatibility; additive post-V28 security schema; and corresponding prospective architecture/DBML/API documentation. This paragraph records the current conversation authority; it does not claim a separately published implementation decision or implementation acceptance.

Prerequisites satisfied: [D1–D6 ratification](owner-ratification-d1-d6-2026-11-11.md), [independent pre-handoff re-review 3](reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-3.md) `READY_FOR_HANDOFF` with PH-01–04 CLOSED **at preparation**, and Owner publication at the entry SHA. Earlier notices saying unpublished, PH-04 OPEN or no implementation authority describe the preceding preparation checkpoint and are superseded **only for this exact slice** by the current Owner instruction and this handoff. Preserve their dated evidence and reviews.

Deliver two fixed access profiles for **one immutable data Owner `app_users.id=1`**, with revocable persisted authority and safe password-based profile transitions. **DP-A must be independently safe to deploy:** PUBLIC may use only the selected Auth operations; all application business/data routes remain DENIED, including Film/Fiction/Reference. PRIVATE retains existing legitimate operations, business rules and feature flags. The exact [Auth matcher/wire/lifecycle contract](auth-compatibility-and-session-lifecycle-2026-11-11.md) controls the route table and D3 semantics; this handoff fixes its executable file/DDL/lock/test details. Do not implement superseded conceptual options in [authentication-session-and-migration](authentication-session-and-migration.md).

Sources of truth: [Owner delivery policy](../../../workflow/current-owner-delivery-policy.md), [ACTIVE](../../handoffs/ACTIVE.md), [slice acceptance A01–A21](implementation-slices-and-acceptance.md), [harness readiness](harness-readiness.md), [fixed access policy](fixed-access-policy-matrix.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [module dependency matrix](../../../architecture/module-dependency-matrix.md), and [Docs Sync contract](../../../workflow/documentation-synchronization.md). Later B1/B2 policy rows are future requirements, **not grants in A**.

## Necessity and simplest implementation

| Need | Decision / existing reuse | Effort and limit |
| --- | --- | --- |
| One Owner, two fixed credentials | KEEP existing `AppUser`, PRIVATE password and PIN; ADD one optional PUBLIC hash/epoch row | Medium; no identity/tenant/account platform |
| Immediate post-switch denial | KEEP signed HS256 JWT; ADD persisted lineage/profile/generation validation on each admission | Medium/high; claims alone cannot revoke an issued JWT |
| Refresh/reset races | REUSE transactions, PostgreSQL row locks, `Clock`, `TokenGenerator` and password encoder | Medium/high; one local state machine, no event bus, scheduler or distributed session store |
| Safe standalone A | REPLACE generic authenticated fallback with PRIVATE-only/default-deny policy plus exact Auth exceptions | High security impact; no per-business-module production changes needed in A |
| Export/cutover/Google | REUSE Portability inventory guard, existing fake providers, OIDC transactions and local fixtures | Medium; no new dependency, hook, agent or generalized harness |
| Data grants, BFF, Recycle Bin, RAG, final audit | DEFER | No implementation or acceptance in this handoff |

Estimate is qualitative, not a time/test-count promise. Two Auth-owned tables plus additive refresh columns suffice. Do not duplicate the PRIVATE hash or introduce generic authorization abstractions. Package by existing capability; root HTTP envelopes stay at the application root under ADR-0016. No new cross-module dependency or imports of another owner's internals.

## Exact source and test change map

Paths in the following source tables are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`. **Only enumerated edits are allowed.** New classes below are permitted where needed; omit unnecessary wrappers rather than filling a quota. Add `package-info.java` only for a newly listed internal package if required by existing conventions. If a necessary production edit falls outside this map, stop and report the exact requirement; do not broaden a directory wildcard.

| Existing file(s) | Allowed change / reusable seam |
| --- | --- |
| `authentication/internal/application/session/SessionService.java` | Preserve `login(LoginCommand)`, `rotate`, `revoke` wire-facing operations; replace unbound issuance with explicit persisted profile authority; implement selected login/switch/reset transactions here or in the named profile service below. Remove Google access to unconditional PRIVATE issuance. |
| `authentication/internal/application/token/JwtTokenService.java` | Issue access tokens only from an explicit validated persisted lineage snapshot (`sub`, `profile`, `sid`, `generation`); preserve issuer, HS256, JTI and configured TTL. No successful no-argument unprofiled issuer remains. A deprecated no-argument signature may only throw fail-closed to keep the suspended archival test compiling; it must never issue authority. |
| `authentication/internal/domain/RefreshToken.java`; `authentication/internal/infrastructure/persistence/RefreshTokenRepository.java` | Add binding fields and guarded state transitions; nonlocking hash-to-lineage projection followed by canonical locks; preserve hashing/replacement history. No first-lock-on-refresh implementation. |
| `authentication/internal/infrastructure/security/SecurityConfiguration.java` | Wire persisted admission and exact method/path/profile policy, strict claim validation and dispatch enforcement; reuse existing entry point/access-denied `ApiResponse`. Preserve signing, issuer, password encoder, CSRF/CORS and flag defaults. Do not repair the separate historical AUTH-CLOCK issue. |
| `authentication/internal/web/controller/AuthController.java`; `authentication/internal/web/mapper/AuthWebMapper.java`; `authentication/internal/web/advice/AuthExceptionAdvice.java` | Expose four new exact operations; map new Auth exceptions/errors/OpenAPI and route-local validation. Preserve old DTOs/response shapes and root envelopes. |
| `authentication/internal/application/bootstrap/UserBootstrapper.java` | Create the unconfigured PUBLIC metadata row atomically with a newly bootstrapped Owner; preserve its isolated singleton insertion/conflict semantics. No configured credential or second Owner. |
| `authentication/internal/application/privatepin/PrivatePinService.java` | Require validated PRIVATE authority for its existing operations, preserving six-digit validation, boolean verify, change race/locking and settings semantics. |
| `authentication/internal/application/google/GoogleSignInService.java`; `authentication/internal/application/google/GoogleSignInStore.java`; `authentication/internal/web/controller/GoogleSignInController.java` | D4 gates at service/store issuance boundaries and PRIVATE-only link; fail pre-cutover LOGIN callbacks, retain LINK completion, reject redemption before consumption. Preserve existing host/body/cookie/PKCE/state/nonce/issuer/sub/one-use protections, resource limits and provider client. |
| `portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java` | Add exactly `ppv_public_login_credential` and `ppv_login_sessions` to `EXCLUDED_SECURITY_TABLES`. Preserve all old exclusions, allowed tables, schema guard, snapshot/streaming behavior and output format. |

New Auth files, all under `authentication/internal/`:

| Exact paths | Purpose |
| --- | --- |
| `domain/LoginProfile.java`, `domain/PublicLoginCredential.java`, `domain/LoginSession.java` | Two-value enum and entities corresponding exactly to section DDL. No `@Data`, raw credentials or entities in HTTP responses. |
| `infrastructure/persistence/PublicLoginCredentialRepository.java`, `infrastructure/persistence/LoginSessionRepository.java` | Credential shared/exclusive locks, lineage lock and coherent admission projection; reuse JPA/EntityManager/JDBC already present. |
| `application/session/SessionAuthorityService.java`, `application/session/SessionAuthority.java` | One owning validation/admission snapshot and validator, reused by HTTP and Auth transitions. Avoid duplicate state rules in controller/filter/service. |
| `application/session/ProfileSessionService.java` | Optional cohesive home for PUBLIC login, setup/reset and transitions if keeping them all in `SessionService` would mix responsibilities; no parallel token-rotation implementation. |
| `application/session/PublicNotConfiguredException.java`, `application/session/PasswordNotDistinctException.java` | Owning new 409/422 errors. Invalid credentials/session/profile reuse existing security/exception types. |
| `infrastructure/security/ProfileAuthorizationManager.java`, `infrastructure/security/SessionAuthorityFilter.java` | Small request-boundary integration; may consolidate into one of these files if sufficient. Must handle persisted revalidation even with a reused async security context. No generic policy registry/module. |
| `web/dto/PublicProfileLoginRequest.java`, `web/dto/PublicCredentialRequest.java`, `web/dto/ElevatePrivateRequest.java` | Route-local strict input/validation and redacted `toString`; no downgrade DTO/body. No generic profile/role selector. |

Keep `SessionOperations`, `LoginCommand`, `AuthTokensView/Response`, old request DTOs and module named interfaces unchanged unless an implementation cannot use the internal profile service through its module-local controller. The selected plan requires no change to these public signatures. Do not add profile/session secrets to `AuthTokensResponse` (retain accessToken, refreshToken, tokenType, expiresIn).

**Schema:** create only `backend/src/main/resources/db/migration/V29__authentication_profiles_and_sessions.sql` with the DDL below. V1–V28 remain byte-identical. If V29 or ADR-0029 becomes occupied at entry, reconcile against the approved SHA; do not silently renumber a materially different migration.

Test paths are relative to `backend/src/test/java/com/vhvkhangg/personalprivatevault/`:

| Existing exact tests/fixtures | Allowed changes |
| --- | --- |
| `authentication/{AuthWebIntegrationTest,SecurityFilterChainIntegrationTest,JwtTokenServiceTest,SessionServiceTest,RefreshTokenLifecycleIntegrationTest,AuthenticationBootstrapIntegrationTest,PrivatePinIntegrationTest,PrivatePinConcurrencyTest,SecretRedactionTest}.java` | Extend actual profile/session security, lifecycle, bootstrap/PIN compatibility and redaction assertions. Preserve substantive old coverage; intentionally changed Auth expectations must trace to D1–D6. |
| `authentication/{GoogleSignInWebIntegrationTest,GoogleSignInProtocolTest,GoogleSignInPersistenceIntegrationTest,GoogleSignInBrowserIntegrationTest}.java`; `authentication/fixture/{GoogleSignInTestSupport,GoogleSignInBrowserConsumer}.java` | Amend only D4 expectations and real persisted PRIVATE fixtures; retain local OIDC/negative browser evidence. Fake provider source stays unchanged unless its existing test data cannot express the exact new failure case; report that need first. |
| `support/AbstractWebIntegrationTest.java` | Replace `bearerToken()` stateless shortcut with lazy legitimate PRIVATE bootstrap/login/session fixture; cleanup new security state. Do not prebootstrap every test and invalidate bootstrap acceptance, overwrite an existing Owner password or change RAG recovery barriers. |
| `drive/fixture/DriveS2HttpTestSupport.java`; `drive/{DriveH1BWebIntegrationTest,DriveS2WebIntegrationTest,DriveS2StreamingIntegrationTest}.java` | Only token/Owner fixture adaptation and expected auth-boundary 401 for invalid singleton authority, plus direct PUBLIC no-bytes/side-effect controls. Preserve provider/stream/upload business expectations. No Drive production changes. |
| `portability/PortabilityIntegrationTest.java` | Assert V29, complete ZIP, new exclusions/absence of security contents and unchanged unknown-table failure/integrity/stream resource checks. |
| `web/OpenApiRouteInventoryIntegrationTest.java`; `web/ApiResponseEnvelopeAndErrorIntegrationTest.java` | Exact new Auth operations/schemas/errors, profile security description and old envelope compatibility; no fabricated constant live-route count. |
| `ApplicationArchitectureTests.java` | Only assertions directly affected by the new internal Auth classes, if needed. Preserve module boundaries; do not waive them. |

New tests permitted: `authentication/{ProfileSessionIntegrationTest,ProfileSessionConcurrencyIntegrationTest,ProfileRouteAuthorizationIntegrationTest,ProfileMigrationIntegrationTest}.java`; optional narrow `authentication/fixture/{ProfileAuthFixture,ProfileSessionRaceFixture}.java`. Use existing JUnit/MockMvc/Testcontainers/Clock/latches. No security mocks replace signed JWT + persisted session in acceptance. Existing forged/unprofiled token fixtures remain useful **only as negatives**. Auth domain-unit mocks may remain unit tests and do not count as admission proof.

The archived `rag/RagQaProspectiveEvaluationNativeIntegrationTest.java` still references the old issuer. **Do not edit, execute or reactivate it**; use the fail-closed compile-only signature described above if needed. Do not change suspended QA tags or runtime gates. Inherited business tests may consume the corrected shared fixture without editing business behavior; any further test-file edit outside the table needs an exact necessity report before expansion.

## Exact API/security implementation contract

Implement **every row and selected status/error** in the published [Auth compatibility table, section 1](auth-compatibility-and-session-lifecycle-2026-11-11.md#1-authentication-matcher-and-wire-compatibility-table-proposed-dp-a). It is incorporated without copying obsolete sketches. Core concrete cases:

| Operation | Input / proof | Result |
| --- | --- | --- |
| `POST /api/v1/auth/login` | Existing `{identifier,password}` with Owner username/email + original PRIVATE password | Existing 200 token response, always PRIVATE; identifier remains required; PUBLIC password cannot authenticate PRIVATE |
| `POST /api/v1/auth/profiles/login` | Exactly `{profile:"PUBLIC",password}`; fixed Owner resolved server-side | Configured/correct => 200 PUBLIC; wrong/unconfigured => same 401 `AUTH_INVALID_CREDENTIALS`; extra identifier, non-PUBLIC/missing profile or invalid/missing password => 400 `VALIDATION_ERROR` |
| `POST /api/v1/auth/profiles/public-credential` | Valid PRIVATE bearer + `{privatePassword,publicPassword}`; fresh PRIVATE password | Setup/reset => 200 `data:null`; wrong proof 401; invalid DTO 400; equal to PRIVATE password => 422 `AUTH_PASSWORD_NOT_DISTINCT` |
| `POST /api/v1/auth/sessions/elevate-private` | Valid PUBLIC bearer + `{privatePassword}` freshly checked | 200 replacement PRIVATE pair in same lineage/new generation; wrong password 401; valid wrong profile 403 |
| `POST /api/v1/auth/sessions/downgrade-public` | Valid PRIVATE bearer, no password/body | 200 replacement PUBLIC pair/new generation; unconfigured => 409 `AUTH_PUBLIC_NOT_CONFIGURED`, no mutation; valid wrong profile 403 |
| Existing `POST /refresh`, `POST /revoke` under Auth | Unchanged JSON `{refreshToken}`, no bearer required | Refresh current => 200, invalid/consumed/unbound => 401 `AUTH_INVALID_REFRESH_TOKEN`; revoke current => lineage invalidation/200; old/unknown/expired => 200 no-op |

New password setup uses the current 12–128 character policy (`BootstrapService.validatePassword`), existing `PasswordEncoder`, and verification against the PRIVATE hash for distinctness; never compare freshly salted hash strings or trim/normalize passwords. New login/proof passwords must be nonblank and at most 128 characters; profile is exactly uppercase PUBLIC. Use the existing Google controller's bounded strict Jackson parsing approach locally (unknown and duplicate members rejected) where bean binding would silently ignore an identifier. Malformed/invalid new DTO => redacted 400; do not change global ObjectMapper behavior or old accepted DTO semantics. Protect new credential/proof records from generated `toString` leakage.

Auth/authorization runs before protected-route body validation: absent/invalid/stale authority => 401 `AUTHENTICATION_REQUIRED`; live wrong profile => 403 `ACCESS_DENIED`; only then malformed body => 400. Keep root `ApiResponse`/error conventions and old PIN error codes. Google callback remains the fixed 303 exception. Infrastructure health/docs matchers and exact conditional Drive GET callback stay as enumerated in the published table; they confer no business-data authority.

**Request-boundary policy:** exact method/path exceptions, then require validated PRIVATE for every other application route. No `/auth/**` blanket permit, `.authenticated()` business fallback, profile from request body/header, `sub=1` inference, or unconditional `dispatcherTypeMatchers(...).permitAll()`. PUBLIC 403 includes unknown/unclassified URLs, wrong HTTP methods, HEAD/OPTIONS and applicable ASYNC/ERROR redispatch; emit only the redacted error envelope, no application data, filenames, counts, content headers, redirect data or side effects. PRIVATE unknown URL remains existing 404. Disabled feature routes remain unregistered; test registration separately from PUBLIC denial. Preserve infrastructure's already-approved matcher semantics without expanding them.

JWT claims: preserve `iss`, `sub="1"`, HS256, `iat`, `exp`, random JTI; add `profile` (`PUBLIC|PRIVATE`), `sid` (random UUID), `generation` (positive integral long). Reject absent/malformed/unknown/overflowed claims, any other subject, tampered signature/issuer/time and all legacy tokens. Persisted authority must agree; a signed claim is necessary but insufficient. No raw refresh/PIN/hash goes into claims.

For admission, use a **fresh coherent single SQL snapshot** joining session and singleton credential epoch (no stale JPA first-level cache or cross-request positive cache). Validate generation/profile, owner, revocation, session expiry and any ever-PUBLIC epoch. Define its snapshot acquisition as the D3 admission point. Ordinary data work need not hold locks; each new dispatch admission revalidates, including when a SecurityContext was propagated. A request admitted before a switch/reset/revoke commit may finish; an admission whose snapshot begins after commit cannot use prior authority. Do not claim cancellation/rollback of previously admitted writes or streamed bytes. A database authority lookup failure must short-circuit through the existing redacted 401 `AUTHENTICATION_REQUIRED` entry point, with safe server-side failure categorization, never continue to handlers. This does not turn transaction/commit failure into a successful Auth operation. Do not emit HTML/SQL details on an ERROR path.

## Executable schema delta — V29 only

This is **DDL for Antigravity to implement**, not a migration executed by this handoff session. Use transactional PostgreSQL Flyway in a controlled offline cutover; do not run a mixed old/new binary fleet. The nullable binding group retains **revoked historical** refresh rows rather than inventing PRIVATE sessions. No old password/PIN/domain data changes or token deletion.

```sql
CREATE TABLE ppv_public_login_credential (
    owner_id smallint PRIMARY KEY REFERENCES app_users(id),
    password_hash varchar(255),
    credential_epoch bigint NOT NULL DEFAULT 0,
    created_at timestamptz NOT NULL DEFAULT now(),
    updated_at timestamptz NOT NULL DEFAULT now(),
    CONSTRAINT ck_public_credential_owner CHECK (owner_id = 1),
    CONSTRAINT ck_public_credential_state CHECK (
        (password_hash IS NULL AND credential_epoch = 0) OR
        (password_hash IS NOT NULL AND length(password_hash) > 0 AND credential_epoch > 0)
    )
);

-- Unconfigured metadata only: no seeded/copied credential or plaintext.
INSERT INTO ppv_public_login_credential(owner_id) SELECT id FROM app_users;

CREATE TABLE ppv_login_sessions (
    id uuid PRIMARY KEY,
    owner_id smallint NOT NULL REFERENCES app_users(id),
    active_profile varchar(7) NOT NULL,
    generation bigint NOT NULL,
    public_credential_epoch bigint,
    current_refresh_token_id bigint NOT NULL,
    created_at timestamptz NOT NULL,
    expires_at timestamptz NOT NULL,
    revoked_at timestamptz,
    CONSTRAINT uq_login_session_owner UNIQUE (id, owner_id),
    CONSTRAINT ck_login_session_owner CHECK (owner_id = 1),
    CONSTRAINT ck_login_session_profile CHECK (active_profile IN ('PUBLIC','PRIVATE')),
    CONSTRAINT ck_login_session_generation CHECK (generation > 0),
    CONSTRAINT ck_login_session_epoch CHECK (
        (public_credential_epoch IS NULL OR public_credential_epoch > 0) AND
        (active_profile <> 'PUBLIC' OR public_credential_epoch IS NOT NULL)
    ),
    CONSTRAINT ck_login_session_expiry CHECK (expires_at > created_at)
);

ALTER TABLE refresh_tokens
    ADD COLUMN session_id uuid,
    ADD COLUMN profile varchar(7),
    ADD COLUMN session_generation bigint;

UPDATE refresh_tokens SET revoked_at = COALESCE(revoked_at, now());

ALTER TABLE refresh_tokens
    ADD CONSTRAINT ck_refresh_profile_binding CHECK (
        (session_id IS NULL AND profile IS NULL AND session_generation IS NULL
            AND revoked_at IS NOT NULL) OR
        (session_id IS NOT NULL AND profile IS NOT NULL AND session_generation IS NOT NULL
            AND profile IN ('PUBLIC','PRIVATE') AND session_generation > 0)
    ),
    ADD CONSTRAINT uq_refresh_session_authority
        UNIQUE (id, session_id, user_id, profile, session_generation),
    ADD CONSTRAINT fk_refresh_session_owner
        FOREIGN KEY (session_id, user_id) REFERENCES ppv_login_sessions(id, owner_id)
        DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE ppv_login_sessions
    ADD CONSTRAINT fk_session_current_refresh
        FOREIGN KEY (current_refresh_token_id, id, owner_id, active_profile, generation)
        REFERENCES refresh_tokens(id, session_id, user_id, profile, session_generation)
        DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX idx_refresh_session_id ON refresh_tokens(session_id)
    WHERE session_id IS NOT NULL;
```

Keep all V1 refresh uniqueness/Owner/replacement FKs and indexes. New FKs use default NO ACTION, no destructive cascade. Epoch snapshot intentionally has **no FK to the current epoch**: mismatch is how old lineages become invalid. Other indexes/cleanup jobs are not needed for this slice absent demonstrated evidence.

Circular current-token binding is deliberate and **deferred to commit**: generate session UUID, insert/flush its first bound refresh (scalar session UUID), obtain identity ID, then insert the session/current pointer in the same transaction; neither incomplete state is externally admitted. At commit, pointer proves matching session/Owner/profile/generation. Rotation inserts successor, replaces predecessor and moves pointer; switch additionally changes profile/generation. Never issue/return a token response before transaction commit succeeds. Constraint failures roll back the entire issuance. Test deferred FK failure at commit, not just `save()`.

Service invariants beyond SQL: current refresh is active/unexpired, not replaced, same session/Owner/profile/generation; its expiry equals session expiry; each successor replacement stays in its own lineage; epoch snapshot is retained forever after the first PUBLIC issuance. First setup moves epoch 0→1; reset increments without overflow/wrap and atomically changes the hash. Refresh and session generations also fail closed on overflow. Bootstrap after V29 inserts its unconfigured credential metadata inside `UserBootstrapper`'s Owner transaction. An absent required metadata row for an existing Owner is an integrity failure, not permission to synthesize authority.

Migration acceptance: fresh empty database through V29 plus populated synthetic V28→V29 with one Owner, credential hashes/PIN, settings, domain rows/FKs and legacy refresh replacement chain. Verify unchanged Owner/domain values/counts/FKs and V1–V28 checksums, legacy rows retained/revoked/unbound, PUBLIC unconfigured, no default password, exact catalog constraints and failed invalid insert/update cases. No SQL/DDL is applied to the Owner's real database by the agent.

## Lock order, lifetime and recovery

Implement the following **one canonical transition order**; reread state after each wait using fresh DB values:

1. Find candidate session ID from token hash or signed bearer without trusting it; no row lock at lookup.
2. Lock `ppv_public_login_credential` first: PostgreSQL `FOR SHARE` for login/refresh/switch/revoke; `FOR UPDATE` **from the start** for setup/reset. Never upgrade a shared lock mid-transaction. This fences epoch reset across different lineages, including currently PRIVATE-after-PUBLIC. PRIVATE-only transitions use the same order; shared locks permit independent lineages to progress.
3. Lock the target lineage `FOR UPDATE`, then its presented/current refresh row as applicable. **Lineage → refresh** order is mandatory. Login has no preexisting lineage; its inserts follow the DDL order. Recheck Owner/profile/generation/expiry/epoch and current pointer under locks before modifying state or minting accepted authority. PUBLIC login must verify the password against the fresh **locked credential row**, never verify an old hash and later attach the new epoch after a reset.
4. Persist state, flush and commit once. Locks cover only short authorization transitions, never provider network calls, ordinary request lifetimes, streaming or browser interaction. Use bounded test waits and existing transaction failure handling; no blind retry of a consumed refresh or stale switch. Failed/aborted transactions change no authority; a client may freshly login. Never retry after uncertain commit as if the prior token were unused.

Setup/reset validates the caller's live PRIVATE session and fresh PRIVATE password under its exclusive epoch fence. First setup makes no PUBLIC lineage automatically. Reset need not update every session row: epoch mismatch invalidates **all ever-PUBLIC** lineages, including the calling lineage if affected; unrelated PRIVATE-only lineages remain usable. Prevent a late commit from installing an obsolete epoch. Do not acquire the Owner PIN-change lock after the lineage lock; this slice does not modify PRIVATE credentials. Google keeps its existing Owner→attempt→ticket order and never enters DP session issuance while unmapped, so no reverse lock path is introduced.

At instant `t`, preserve configured `A` (default 15m) and `R` (default 30d): access `exp=t+A`, current refresh/session expiry `t+R` on login, successful refresh and switch. Refresh keeps generation/profile; switching increments generation, replaces current refresh and access, and sets/retains PUBLIC epoch. Ordinary GET does not extend expiry. Reject at `now >= session.expiresAt`, even for otherwise unexpired JWT. No absolute-first-login cap, server idle timer or reaper is added. Preserve existing issuer/signature/timestamp policy and treat the historical AUTH-CLOCK concern separately; deterministic current-time/session tests must not weaken JWT validation to manufacture a pass.

Revoke uses current active **unexpired** refresh only: revoke lineage and current refresh transactionally; access admissions fail thereafter. Unknown/revoked/expired/consumed predecessor => 200 no-op; **never follow `replaced_by_token_id` to revoke its successor**. Rotate→revoke(old) leaves successor valid; revoke(current)→rotate rejects. Profile switch races serialize under the same lineage fence, so no obsolete generation is resurrected. Independent devices are unaffected by ordinary switch/revoke; credential reset is the explicit epoch-wide exception.

Lost committed login/refresh/switch response: do not replay issuance using consumed authority; require fresh credential login to recover. Failed transaction leaves the prior valid state intact. No new idempotency key protocol, global revoke endpoint, password recovery or in-flight stream cancellation is authorized.

## Google, PIN, cutover and export obligations

- **Google/Drive defaults remain OFF.** Test ON only with existing local synthetic fake providers. With mapping absent, LOGIN start fails `503 GOOGLE_SIGNIN_UNAVAILABLE` before attempt creation/outbound OAuth; pre-cutover LOGIN callbacks fail fixed 303/cookie clearing with no ticket. PRIVATE + fresh PRIVATE password LINK start and validated LINK callback preserve immutable issuer/sub Owner binding and one-use opaque ticket. Handoff rejects **before ticket consumption** or PPV token issuance, including direct store/service invocation; future mapping is not added. Preserve LINK failure/rate-limit/nonce/PKCE/state/one-use tests; intentionally replace former successful Google PPV issuance assertions with D4 no-issuance proof rather than silently disabling tests.
- **PIN retained:** PRIVATE only. Incorrect well-formed verify => 200 `verified:false`; malformed six-digit PIN => existing 422; wrong current PIN on change => existing 401. PIN never changes profile or token authority. Keep `pin_hash NOT NULL` and auto-lock settings/default semantics.
- **Portability in the same slice:** add both new tables to exclusions and owning tests, keep old exclusions/unknown-table guard. PRIVATE export succeeds as a complete valid lossless ZIP with manifest highestFlywayVersion 29; new table names may appear in `excludedSecurityTables`, but no credential/session rows, hashes, raw tokens, epochs or secrets appear in exported data/Markdown/media payloads. PUBLIC is denied **before** ZIP creation/manifest/count/header/byte emission. Preserve snapshot consistency, cancellation and resource cleanup.
- **Cutover:** planned compatible-binary maintenance window, restricted full DB backup, stop old serving processes, apply V29 and deploy the reviewed binary before reopening access. Reject old unprofiled JWTs regardless of signature and unbound refresh regardless of legacy expiry. Existing PRIVATE password login is recovery; PUBLIC initially unavailable. Do not run old binaries against V29 or roll credentials back into old authority rules.
- **Restricted full PostgreSQL DR backup differs from ZIP:** it retains Auth state and must be protected as secret material. On isolated restore, before external exposure, invalidate all restored session/refresh authority transactionally and rotate JWT signing material; then verify old tokens fail and fresh PRIVATE login works. Demonstrate on disposable synthetic PostgreSQL (existing tooling; no new backup platform), document operational ordering, never run this against live Owner data. A failed transactional migration rolls back; after committed V29 use forward repair or a compatible reviewed restore/redeploy. No destructive down migration or permissive older-binary rollback.

## User-visible acceptance and finite evidence

Every row is mandatory. Map test method(s), command receipt and actual result in handback; an unrun mandatory case is `NOT_VERIFIED`, not PASS. Reuse published preparation/source evidence only as qualified design provenance, never fresh runtime proof.

| IDs / preparation trace | Scenario and observable outcome | Minimal owning proof |
| --- | --- | --- |
| DPA-01 / A01,A02,A15,A19 | Populated V28→29 and fresh bootstrap preserve singleton/domain data; PUBLIC unconfigured; legacy refresh revoked; invalid schema states fail | `ProfileMigrationIntegrationTest`, bootstrap integration, Flyway/catalog/checksum evidence |
| DPA-02 / A03,A12,A21,PH-04 | Identifier-free PUBLIC configured login 200 PUBLIC only; wrong/unconfigured same 401; PRIVATE/extra identifier/duplicate or malformed members/missing password 400; legacy identifier/password PRIVATE shape unchanged | Auth web and profile session tests, decoded signed claims + stored lineage |
| DPA-03 / A02,A10 | PRIVATE setup/reset needs fresh password; distinctness 422; invalid proof/input leaves hash/epoch/authority unchanged; no hash/secret response/log leakage | Profile session + redaction tests; before/after DB state |
| DPA-04 / A04,A13 | Tampered/legacy/missing/malformed claims, sub other than 1, absent/revoked/expired lineage, wrong profile/gen/epoch and DB lookup failure rejected before handler | Real filter chain, signed negatives and controlled failing authority read; no mocked positive bypass |
| DPA-05 / A05–A07 | Correct PUBLIC elevation and password-free PRIVATE downgrade replace pair/generation; wrong proof/PIN/client profile/stale bearer cannot elevate; unconfigured downgrade 409/no mutation; same-profile action 403 | Profile session + HTTP tests; old JWT/refresh denied, new valid pair admitted |
| DPA-06 / A09,A20 | Login/refresh/switch preserve configured TTL and rolling expiry, refresh profile/gen stable, ordinary reads do not extend, exact session-expiry boundary denied | Controlled clock and actual DB state/JWT expiry; no sleeps |
| DPA-07 / A10,A11,A20 | Two independent sessions switch/revoke independently; reset invalidates all ever-PUBLIC lineages across devices/current PRIVATE, including caller when applicable; PRIVATE-only survives | Profile integration and concurrency tests; no global state mutation shortcut |
| DPA-08 / A09,A10,A20 | Two refreshes have one winner; rotate→revoke(old) successor valid; revoke(current)→rotate fails; revoke unknown/expired/legacy/consumed is no-op 200 | Real PostgreSQL row-lock/barrier tests for both serializations and response/state |
| DPA-09 / A06–A10,A20 | Refresh↔downgrade/elevation, reset↔refresh/elevation/downgrade, simultaneous switches/reset serialize without stale resurrection/deadlock | Bounded latch/barrier tests at authoritative lock/commit boundaries, not sequential mocks |
| DPA-10 / A08,A13,A20 | Already admitted GET/write/stream may complete; admission/ASYNC/ERROR after committed switch uses fresh authority; old PRIVATE JWT invalid immediately | Real filter/HTTP with test-only blocked handler; pre-admission vs post-commit latches and no bytes on denied new stream |
| DPA-11 / A13,A20 | Rollback/commit-constraint failure returns no accepted pair; previous authority remains; committed-lost reply old pair invalid/fresh login recovers; DB failure never falls back | Transaction failure and client lost-response/barrier tests with real authority state |
| DPA-12 / A17,A18,A21 | PUBLIC from **both direct login and downgrade** denied every business/data/unknown path; PRIVATE positive and ANON negative controls; no bytes/content headers/side effects | Registered-route sweep + targeted media/personal/vault/portability/search/film/fiction/reference/feed/finance/Drive/RAG requests; fake services/fixtures only |
| DPA-13 / A12,A17,A21 | Exact ANON/PUBLIC/PRIVATE matcher table incl wrong methods/HEAD/OPTIONS, infrastructure and conditional callbacks; no `/auth/**`, unknown route or dispatch bypass | `ProfileRouteAuthorizationIntegrationTest`, `SecurityFilterChainIntegrationTest`; handler counters + before/after data |
| DPA-14 / A14,A20 | Google OFF absence, ON/unmapped 503/no LOGIN attempt, failed old LOGIN callback 303, PRIVATE proof LINK start/callback and immutable binding, no consumed handoff/tokens | Existing Google protocol/web/persistence/local-browser suites amended to D4; provider/token/session/ticket counters |
| DPA-15 / A12,A16 | Singleton repeated/concurrent bootstrap, PIN verify false/422/change401, PRIVATE-only PIN and retained redaction/one-use controls | Existing bootstrap/PIN/concurrency/redaction regression suites |
| DPA-16 / A15,A19 | PRIVATE ZIP valid/complete/security-free with V29; unknown table rejected; PUBLIC no archive/bytes; isolated full-DB restore invalidated before exposure | Portability integration + disposable PostgreSQL restore/invalidation test and operator runbook |
| DPA-17 / A16,A21 | New OpenAPI operations/strict schemas/error codes and Google 303 accurate; module boundaries/root envelope and affected PRIVATE operations preserved | OpenAPI/envelope/architecture + inherited web/streaming regression gates below |

For DPA-12/13 obtain **actual** registered `RequestMappingHandlerMapping` methods/patterns under tested OFF and local ON configurations; do not assume the historical 254 source declarations are all live. Exercise all registered business declarations with valid PUBLIC authority before route-level body validation and prove handler non-admission; unknown/unclassified catchall and dispatch tests protect future routes. Supply safe known-ID representatives and PRIVATE successes for sensitive bytes/export/mutations. Distinguish safe error JSON from application-content bytes. No live provider access, model inference or QA worker may be invoked to prove denial.

## Focused verification plan

Use [bounded-command-execution](../../../../.agents/skills/bounded-command-execution/SKILL.md) and existing `scripts/run-bounded-command.py`, ordinary **300s** cap, fresh unique ignored `logs/bf26-dp-a/<run-id>/` receipts/stdout/stderr. Do not overwrite earlier runs, kill unrelated processes or use an extended profile for focused commands. No blanket full suite is required here; future eligible full-suite maximum 2,100s needs its existing separate approval. No dependency upgrades/install, new harness or downloaded browser/model.

From repository root, use this command shape, substituting one **explicit gate selector** below and a fresh receipt path (PowerShell variables are local to the invocation):

```powershell
$dpRun = Join-Path 'logs/bf26-dp-a' ([guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $dpRun -ErrorAction Stop | Out-Null
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $dpRun 'gate.json') -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<explicit-selector>' test
```

| Gate | Explicit test selectors / required coverage |
| --- | --- |
| G1 Auth/core | `AuthWebIntegrationTest,SecurityFilterChainIntegrationTest,JwtTokenServiceTest,SessionServiceTest,RefreshTokenLifecycleIntegrationTest,AuthenticationBootstrapIntegrationTest,BootstrapValidationTest,PrivatePinIntegrationTest,PrivatePinConcurrencyTest,SecretRedactionTest,JwtPropertiesTest,TokenGeneratorTest` |
| G2 profiles/races/migration | `ProfileSessionIntegrationTest,ProfileSessionConcurrencyIntegrationTest,ProfileRouteAuthorizationIntegrationTest,ProfileMigrationIntegrationTest` |
| G3 Google local | `GoogleSignInWebIntegrationTest,GoogleSignInProtocolTest,GoogleSignInPersistenceIntegrationTest,GoogleSignInConfigurationTest,GoogleSignInCryptoTest`; separately `GoogleSignInBrowserIntegrationTest` using the existing fake-provider/native fixture |
| G4 export/contracts | `PortabilityIntegrationTest,OpenApiRouteInventoryIntegrationTest,ApiResponseEnvelopeAndErrorIntegrationTest,ApplicationArchitectureTests` |
| G5 affected PRIVATE read/write regressions | Batch the explicit classes `AccountWebIntegrationTest,CollectionWebIntegrationTest,DailyTrendsWebIntegrationTest,FeedWebIntegrationTest,RssDailyTrendsWebIntegrationTest,SavedResourceInboxWebIntegrationTest,FictionWebIntegrationTest,FilmWebIntegrationTest,FinanceWebIntegrationTest,ImportDataWebIntegrationTest,JournalWebIntegrationTest,KnowledgeWebIntegrationTest,LocationWebIntegrationTest,MediaWebIntegrationTest,PeopleWebIntegrationTest,PersonalWebIntegrationTest,ReferenceWebIntegrationTest,SearchWebIntegrationTest,SettingsWebIntegrationTest,VaultWebIntegrationTest,DriveWebIntegrationTest,DriveH1BWebIntegrationTest,DriveS2WebIntegrationTest`; separately `DriveApiInventoryIntegrationTest,DriveOAuthSecurityTest,DriveS2StreamingIntegrationTest,MediaStorageIntegrationTest` for affected callback/async/streaming/resource behavior. No wildcard that selects an enabled RAG ingestion/QA suite. |

Gate selectors may be split into smaller exact class batches to fit 300s; retain one manifest mapping **every selected class/case** to its receipt, and do not omit a timeout/failure. `test` compiles the existing test tree; do not unarchive suspended tests to fix compilation. No skipped mandatory profile cases or Surefire `failIfNoTests=false` workaround. Re-run affected batches after material fixes, not unrelated historical/full suites. Initial implementation/self-review loops do not consume the separate independent remediation counter; record infrastructure failures separately from defects.

Existing Google browser fixture uses fixed `localhost:19080` and local consumer 3000 where required. Apply the [standing port authority](../../../workflow/owner-decision-standing-local-test-port-19080-2026-11-09.md): check availability/IPv4+IPv6 and browser resolution before each run, preserve exact isolated callback/Host/cookie/CSRF/OIDC protections and production defaults; no repeat approval solely for 19080. Do not stop unrelated 8080 services, use 18080, or invent fallback/proxy. Existing separately accepted random-port Drive HTTP tests keep their own fixture contract; do not generalize it to the fixed Google browser fixture. Backend-only D4 test adaptation does not authorize paused BFF/UI implementation. If mandatory native evidence cannot be produced, report `NOT_VERIFIED` and the infrastructure blocker honestly.

Engineering guidance: [authentication-security](../../../../.agents/skills/authentication-security/SKILL.md), [Java/Spring](../../../../.agents/skills/java-spring-coding-standards/SKILL.md), [pragmatic SOLID](../../../../.agents/skills/pragmatic-solid-design/SKILL.md), [reuse](../../../../.agents/skills/reuse-and-consistency/SKILL.md), [modular architecture](../../../../.agents/skills/modular-monolith-architecture/SKILL.md), [JPA/PostgreSQL](../../../../.agents/skills/jpa-postgresql-persistence/SKILL.md), [migration review](../../../../.agents/skills/database-migration-review/SKILL.md), [Backend tests](../../../../.agents/skills/backend-testing/SKILL.md), [HTTP contracts](../../../../.agents/skills/rest-api-http-contracts/SKILL.md), [architecture change](../../../../.agents/skills/architecture-change/SKILL.md) and [review coverage](../../../../.agents/skills/codex-review-coverage/SKILL.md). No new agent or skill is created; no delegation is required by this handoff.

## Documentation impact and responsible editor

Antigravity is expressly authorized to write these **implementation-adjacent** files only for actual DP-A results. Prospective until tested; no Owner-publication or independent-acceptance claim:

| Exact document | Required delta |
| --- | --- |
| `docs/adr/0029-fixed-profiles-and-persisted-session-authority.md` (new); `docs/adr/README.md` | Prospective scope-specific decision, Owner authority above, selected minimal model, D3 admission boundary, D4 Google delta, migration/restore cost and deferred grants; cross-link accepted ADR-0008/0016/0017/0028 without rewriting their historical decisions |
| `docs/database/authentication-profiles-and-sessions.dbml` (new); `docs/database/README.md`; `backend/src/main/resources/db/migration/README.md` | Additive V29 fields/constraints/deferred pointer/FKs/legacy cutover, overlay linking frozen v1; V1–V28 untouched |
| `docs/architecture/authentication-profiles-and-sessions-api.md` (new); `docs/architecture/security-architecture.md`; `docs/architecture/api-architecture.md`; `docs/architecture/google-sign-in-operations.md` | Exact new/existing Auth method/DTO/error/matcher table, claims/admission/dispatch, retained PIN, Google OFF and ON/unmapped LINK versus LOGIN, no PUBLIC data grants; update only directly affected sections/links |
| `docs/architecture/storage-backup-import.md`; `docs/implementation/maintenance/2026-11-11-dual-login-profiles-preparation/dp-a-cutover-and-recovery-2026-11-11.md` (new) | ZIP exclusion versus restricted full backup, actual cutover/invalidation/key-rotation order, synthetic restore proof and compatible forward-only recovery limitation |
| `docs/repository/repository-package-tree.md` | Add only actual Auth-owned classes/new schema/docs delta; no module graph or frozen baseline rewrite |
| `docs/implementation/maintenance/2026-11-11-dual-login-profiles-preparation/implementation-handback-dp-a-2026-11-11.md` (new); this handoff result fields and `docs/implementation/handoffs/ACTIVE.md` | Actual changed-path inventory, DPA/G1–G5 trace/results, receipts/hashes, self-review, known limitations and state `IMPLEMENTED_AWAITING_CODEX_REVIEW` |

Routine current-navigation/publication closeout remains **ChatGPT/Owner** work: root `AGENTS.md` first notice, `docs/workflow/current-owner-delivery-policy.md` current notice, `docs/roadmap.md` first notice, maintenance index BF26-DP entry and preparation README current-status fields still describe the published preparation checkpoint. Their historical status text is not authority to cancel this explicit new approval. Route their bounded current refresh to ChatGPT without rewriting history; it does not authorize repository-wide cleanup. Owner alone publishes after independent acceptance. This handoff is **not** Owner-published implementation.

**Docs Sync Check at creation:** UPDATED only this executable contract and ACTIVE current execution block; preserved historical H1-F register/reviews/evidence. ALREADY_CONSISTENT: live/local SHA, clean entry, V28/0028 tips, preparation readiness, D1–D6, separate counters, retained flags/invariants. DEFERRED_OUT_OF_SCOPE: routine five-notice/README refresh listed above and unrelated stale records, to ChatGPT. NOT_VERIFIED: all runtime/migration/browser/OpenAPI registration/lock/restore behavior; these are mandatory implementation evidence, not a creation PASS. Antigravity and independent reviewer repeat the scoped check against actual delivered code and docs; no separate approval loop solely for this planning artifact.

## Non-goals, stop conditions and handback

MUST NOT: implement DP-B1/B2/C; grant PUBLIC Film/Fiction/Reference/Search/Vault/metadata/tag access; add Recycle Bin, Dashboard aggregator, second Owner, registration/tenancy, generic authorization platform, global revoke/recovery endpoint, idle timer, new dependencies or security bypass; edit V1–V28/historical reviews/evidence; activate live Google/Drive/Feed/RAG or suspended S19-2; implement Frontend/BFF/design; run final non-RAG audit; stage/commit/push/tag/open or merge PRs.

Preserve Frontend `FRONTEND_PAUSED_BY_OWNER`, H1-F `OWNER_PUBLISHED / CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED`, FR-13 `MEDIUM / OPEN / UPSTREAM_DEPENDENCY_BLOCKED`, v0.3, H1-F 3/3 exhausted and stopped, takeover 1 independently blocked/stopped, corrective follow-up 1 reviewed/stopped, FR-12 test-only 1/1 reviewed/stopped. P17 frozen/P18 closed/S19-1 accepted-published/S19-2 suspended-archival, indexing inactive/live originals disabled and normal provider flags OFF remain binding.

At implementation entry compare actual local/remote main, worktree and migration/ADR tip to this baseline. Reconcile any delta first; never discard unrelated edits. Stop on an unresolvable material source/contract drift, necessary file/schema/API permission beyond this exact authorization, frozen invariant conflict, missing mandatory proof or requirement for a new platform/dependency. Report facts, hypotheses and `NOT_VERIFIED` separately. Do not ask again for already-granted DP-A authority or reset counters after an interruption.

Delivery requires: code/migration/docs within allowlist; every DPA row evidenced; G1–G5 actual commands/exits/counts/skips and unique log/receipt paths; SHA-qualified reused evidence separate from fresh results; exact file list and V1–V28 preservation; schema/API/package/document consistency; no secrets in tracked artifacts/logs; mandatory implementer self-review using `/antigravity-self-review` with remaining findings recorded. Set `IMPLEMENTED_AWAITING_CODEX_REVIEW` only when delivered, not `READY_FOR_OWNER_COMMIT`. Separate Codex reviews source/DDL/real security and evidence, including package ownership and ADR-0016; open Critical/High/Medium or mandatory NOT_VERIFIED blocks acceptance. At most three independently reviewed remediation rounds; takeover is separately gated, never automatic.

### Historical Antigravity R3 submitted implementation result

Antigravity completed Remediation Round 3 in [implementation-handback-dp-a-2026-11-11.md](implementation-handback-dp-a-2026-11-11.md), resolving open findings from independent re-review 2:
- **FR-DPA-04:** Added Jackson raw wire AST payload inspection in `SecurityConfiguration.hasFloatingOrMalformedWireGeneration(Jwt jwt)` rejecting non-integral or floating representations (e.g. `1.0000000000000001` or scientific notation) before/alongside Nimbus, returning 401 `AUTHENTICATION_REQUIRED` at admission. Explicit rejection of `Double`, `Float`, non-exact `BigDecimal` in `parseGeneration()`. Tested via standalone probe.
- **FR-DPA-06:** Added 10-table row mutation assertion to route sweep; tested ASYNC (401) and ERROR (403) dispatchers; verified Local-ON Drive and Google routes denied to PUBLIC; verified stale PRIVATE redispatch rejection; verified serialized reset vs refresh under row locks without deadlocks; verified post-commit invalidation and lost-reply recovery against `/api/v1/reference/countries`; verified issuance rollback on failure.
- **FR-DPA-07:** In `ProfileMigrationIntegrationTest`: synthetic V28->V29 cutover executed on isolated DB with authentic token generator hashes; disaster recovery invalidation runbook executed with real token hashes; disposable PostgreSQL backup/restore runbook executed in single JDBC transaction honoring deferred circular foreign keys.
- **FR-DPA-09:** Retained as `BLOCKED_OWNER_DECISION`.

Fresh verification gates G1–G5 passed cleanly with **283 / 283 tests passing** (0 failures, 0 errors, 0 skips across G1: 94/94, G2: 31/31, G3: 60/60, G4: 69/69, G5: 29/29).

### Historical independent R3 stop — preserved

[Initial review](reviews/2026-11-11-bf26-dp-a-independent-final-review.md), [re-review 1](reviews/2026-11-11-bf26-dp-a-independent-final-re-review-1.md), and [re-review 2](reviews/2026-11-11-bf26-dp-a-independent-final-re-review-2.md) remain preserved. [Independent re-review 3](reviews/2026-11-11-bf26-dp-a-independent-final-re-review-3.md) CLOSED FR-DPA-04 and retained FR-DPA-01/02/03/05/08 closures. FR-DPA-06/07/09 remain MEDIUM OPEN, plus mandatory NOT_VERIFIED cases and DPA-L01 LOW backlog. Fresh reviewer G2 31/31 and real regression probe support the code closures, while misleading test labels do not prove missing behavior. This independent disposition supersedes the submitted implementation-resolution claims above. Remediation **3/3 exhausted, independently blocked / STOPPED**; takeover **0 / NOT_AUTHORIZED**. No fourth ordinary Antigravity loop; Owner must separately decide further bounded work and the two retained exact-map exceptions.

**Next step:** Owner | decide a separately bounded `$codex-implementation-takeover` for FR-DPA-06/07 and disposition of FR-DPA-09's two exact-map exceptions | BF26-DP-A, [independent re-review 3](reviews/2026-11-11-bf26-dp-a-independent-final-re-review-3.md) and [ACTIVE](../../handoffs/ACTIVE.md) | CHANGES_REQUESTED; ordinary Antigravity work STOPPED; any Codex corrective delivery needs a separate independent reviewer | remediation 3/3 exhausted; takeover 0 / NOT_AUTHORIZED; historical counters unchanged.

### Historical takeover handback / independent review boundary — 2026-11-11 submission

Owner-approved exceptions retain only DP-A OpenAPI contract descriptions and the Drive inventory fixture/count compatibility. Exact diffs, finite DPA evidence, complete G5 class/case/source/receipt manifest and own self-review are in [handback section 11](implementation-handback-dp-a-2026-11-11.md#11-exceptional-codex-takeover-delivery--2026-11-11). Fresh final gates total 432 passing tests (G1 94, G2 53, G3 60, G4 69, G5 156); previous passing/failing attempts are preserved and excluded from this distinct total. V1–V28 and all other received production source except the named OpenAPI exception are unchanged. No independent closure, READY or publication is claimed.

**Next step:** Owner / separate Codex session | `$codex-final-review` | BF26-DP-A FR-DPA-06/07/09 and [implementation handback](implementation-handback-dp-a-2026-11-11.md#11-exceptional-codex-takeover-delivery--2026-11-11) | `INDEPENDENT_REVIEW_REQUIRED`; reviewer must disclose Codex takeover authorship and independently assess source/evidence | Antigravity 3/3 EXHAUSTED; exceptional Codex takeover 1/1 implemented; other counters unchanged.

### Independent takeover acceptance — 2026-11-12

The [separate independent final review](reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md) closes FR-DPA-06/07/09 and returns **READY_FOR_OWNER_COMMIT**. All DPA-01–17 mandatory rows are verified, prior closures retained, DPA-L01 remains nonblocking LOW backlog. The implementer's 432-test final evidence is source-qualified and independently audited. Fresh reviewer G2 has 43 completed passing cases before a preserved 300s TIMEOUT/124 with confirmed cleanup, then 10/10 PASS/0 for only the unfinished class at a fresh path; no whole-command pass is fabricated. No delivered production/test edits or publication by the reviewer.

**Docs Sync Check:** UPDATED only independent review/current result fields and linked ACTIVE/handback disposition; ALREADY_CONSISTENT source/API/schema/security boundaries and separate counters; routine five-notice/README synchronization remains DEFERRED_OUT_OF_SCOPE to ChatGPT/Owner. No mandatory acceptance NOT_VERIFIED or unresolved DP-A Owner decision remains. Historical reviews/receipts remain unchanged.

**Next step:** Owner | commit/push accepted DP-A delivery, then ChatGPT brief slice closeout/current-navigation synchronization | BF26-DP-A and the linked independent final review | READY_FOR_OWNER_COMMIT; use actual publication SHA for closeout; no next-slice grant | Antigravity 3/3 exhausted; exceptional Codex takeover 1/1 independently accepted; other counters unchanged.
