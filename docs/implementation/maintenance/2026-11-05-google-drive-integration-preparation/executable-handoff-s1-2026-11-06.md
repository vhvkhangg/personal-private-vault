# Executable slice handoff — GDI26-S1 connection authority

- **Handoff ID:** GDI26-S1.
- **Status:** READY_FOR_OWNER_COMMIT. Separate [independent takeover review](reviews/2026-11-06-gdi26-s1-independent-takeover-review.md), 2026-11-06: FR-01..09 VERIFIED / CLOSED; S1-01..13 PASS within deterministic scope. Fresh independent gate: 224/224 tests, zero failures/errors/skips; actual failed fence control and identical restored-green test independently repeated. Original review and [corrective handback](implementation-handback-s1-2026-11-06.md) remain preserved history. Drive stays OFF; not yet Owner-published.
- **Classification:** Owner-approved implementation-maintenance slice; no numbered-phase/RAG authorization.
- **Owner authority:** [D00–D07](owner-decision-record-2026-11-05.md), [approved scope](owner-scope-and-decisions.md), original handoff creation, and the current exceptional Owner `$codex-implementation-takeover` invocation recorded below.
- **Preparation gate:** [independent pre-handoff re-review 2](reviews/2026-11-05-gdi26-independent-pre-handoff-re-review-2.md), READY_FOR_HANDOFF; PH-01/02/03 CLOSED **for preparation only**.
- **Entry commit:** [304708e6bb5c1a8e45e89e2bdb9026d9ae4e8002](https://github.com/vhvkhangg/personal-private-vault/commit/304708e6bb5c1a8e45e89e2bdb9026d9ae4e8002). Local HEAD and freshly queried remote main matched; entry worktree clean. Its 11-file change from f4144e89 contains preparation/review documentation only.
- **Implementer:** Codex under the exceptional Owner-authorized FR-01..09 takeover below; initial Antigravity implementation/self-review and Codex non-independent self-check preserved.
- **Independent reviewer:** separate Codex $codex-final-review invocation, [accepted review](reviews/2026-11-06-gdi26-s1-independent-takeover-review.md), 2026-11-06.
- **Remediation:** Antigravity **0/3 consumed**. One exceptional bounded Codex takeover independently **ACCEPTED / STOPPED**; no earned 3/3 escalation, transfer or exhaustion. Earlier counters unchanged; no further implementation authorized.

## Goal and sources of truth

### Exceptional Owner-authorized Codex takeover — 2026-11-06

The Owner explicitly invoked `$codex-implementation-takeover` for **GDI26-S1 FR-01 through FR-09 only**, without completing Antigravity remediation rounds, because Antigravity is unavailable after repeated infrastructure HTTP 503/model-capacity failures. The Owner requires root-cause repairs, focused regressions including the independent failing probes/control, bounded verification, implementer self-check and implementation-adjacent documentation synchronization. Preserve D00–D07, default OFF, security boundaries and all historical reviews/evidence. No Google OAuth activation or Git publishing.

This is the exceptional bounded takeover permitted by [Owner policy, Remediation, ownership and Git safety](../../../workflow/current-owner-delivery-policy.md#remediation-ownership-and-git-safety): “or the Owner expressly authorizes an exceptional bounded takeover”. The invocation expressly overrides entry condition 2 (ordinary strict 3/3 escalation) of the [takeover skill](../../../../.agents/skills/codex-implementation-takeover/SKILL.md) for this invocation only. Entry conditions 1/3/4 still apply. No completed remediation/re-review paths exist and none are invented. Infrastructure failures are not corrective rounds.

| Ledger event | Author / outcome | Counter and authority |
| --- | --- | --- |
| Initial implementation/self-review | Antigravity; preserved submission/handback | Does not consume remediation |
| Initial independent final review, 2026-11-06 | Codex; CHANGES_REQUESTED, FR-01..09 OPEN | Antigravity 0/3 consumed |
| Exceptional takeover authorization, current Owner invocation, 2026-11-06 | Owner; Codex implementer AUTHORIZED / IN_PROGRESS, FR-01..09 only | Antigravity remains **0/3**; one exceptional takeover, no exhaustion/transfer/reset |
| Takeover handback, 2026-11-06 | Codex implementer; [FR-01..09 handback](implementation-handback-s1-2026-11-06.md), fresh 224-test gate and isolated expected failed control | IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED; Antigravity still 0/3; no independent closure |
| Separate independent takeover review, 2026-11-06 | Codex reviewer; [new review](reviews/2026-11-06-gdi26-s1-independent-takeover-review.md), fresh 224-test gate, actual failed/restored control and source/docs verification | READY_FOR_OWNER_COMMIT; FR-01..09 VERIFIED / CLOSED; Antigravity 0/3 unchanged; one exceptional takeover ACCEPTED / STOPPED |

Codex authored the corrective implementation and its non-independent self-check; the required **separate Codex `$codex-final-review` invocation** has now independently accepted the bounded delivery. Older NOT_AUTHORIZED/Antigravity-next-route and pending-review statements below describe initial submission/review history and are superseded only by this recorded exception and new independent disposition. The global skill/policy and historical independent review remain unchanged. FR-L01/02 and other LOW backlog remain nonblocking; this result grants no new scope.

Allow the single PPV Owner to connect two distinct verified Google accounts, inspect status, reconnect the same subject, and disconnect locally with one bounded best-effort remote revoke. Deliver backend connection authority with encrypted refresh-token custody, disabled by default. This authorizes deterministic implementation/testing, not real Google consent, Cloud configuration changes, live provider smoke or deployment activation.

Read [Owner policy](../../../workflow/current-owner-delivery-policy.md), [handoff workflow](../../handoffs/README.md), [ACTIVE](../../handoffs/ACTIVE.md), [OAuth/security contract](oauth-security-and-data-governance.md), [HTTP/table plan](drive-api-ui-and-storage-plan.md), [source reuse](source-reuse-and-design.md), [acceptance plan](acceptance-and-review-plan.md), [risk ledger](decision-and-risk-ledger.md), and the accepted re-review. This executable contract fixes finite parameters left to handoff creation. Preparation-time NO_ACTIVE_HANDOFF/NOT_STARTED labels remain submission history; ACTIVE is current execution authority.

Preserve [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [module matrix](../../../architecture/module-dependency-matrix.md), [module boundaries](../../../architecture/module-boundaries.md), [package tree](../../../repository/repository-package-tree.md) and [frozen Schema v1](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml), except the expressly approved additive D04 amendment below. No material Owner choice remains open for S1.

## User-visible acceptance — MUST

U/V mean distinct verified Google subjects, not email addresses. Use synthetic identities/tokens only.

| ID / preparation trace | Scenario/input | Expected observable result | Minimal proof / reuse |
| --- | --- | --- | --- |
| S1-01 / GDI26-01 | Drive unset/false; Google credentials/key absent | App starts; no active Drive handler, credential/key loading, provider I/O or Drive connection-row mutation. Normal Flyway application of V25 is allowed. | G4 real OFF context and recording provider; existing conditional configuration. |
| S1-02 / GDI26-02 | Anonymous authorize/status/disconnect; callback without valid proof | Owner routes return existing JWT 401 envelope. Only exact enabled GET callback is public; invalid proof fails before exchange. Other methods/paths remain private. | G1 real security filter/controller; SecurityConfiguration/ApiResponses. |
| S1-03 / GDI26-03 | Connect U to slot 1 and V to slot 2 | Two isolated CONNECTED rows with verified identity/scopes and usable encrypted refresh tokens; status exposes two slots/display email only. | G2 signed fake OIDC + G3 PostgreSQL/crypto. |
| S1-04 / GDI26-04 | Slot 0/3/noninteger, U in both slots, V reconnect into U's slot, repeated callback | Invalid slot 400; duplicate subject/different-subject replacement conflicts; no overwrite/third row/duplicate exchange or commit, including concurrent callbacks. | G1/G3 barriers and DB constraints. |
| S1-05 / GDI26-05 | Replayed/expired/unknown state, wrong browser/owner context, replaced attempt, altered redirect context, restart | No commit; pre-exchange guard failures make zero token requests. Host/Forwarded never selects redirect. Consumed state cannot be reused. | G1 Clock/state/filter proof; G4 restart-loss. |
| S1-06 / GDI26-06 | Wrong ID-token signature/algorithm/issuer/audience/expiry/nonce/sub, reduced or broad Drive scope | Generic failed callback; no new credential; prior connected slot unchanged. Email never establishes identity. | G2 controlled signing keys/scope fixtures. |
| S1-07 / GDI26-07 | First grant lacks refresh token; same-subject regrant omits it; rotation/key failure | Verified first grant without usable token yields RECONNECT_REQUIRED, never CONNECTED. Retain only an existing token validated as still good. AEAD tamper/swap/lost key fails closed. | G2/G3 omission/rotation/key/ciphertext and exposure tests. |
| S1-08 / GDI26-08 | Disconnect U; revoke returns 200/non-success/ambiguous timeout | Local DISCONNECTED independent of remote outcome; at most one revoke of captured old token, no file call. Report REQUEST_ACCEPTED/FAILED/UNCERTAIN accurately; 200 is not propagation proof. | G2/G3 grant-wide fake provider and local-delete rollback. |
| S1-09 / GDI26-09,13 | Old refresh/callback completes after disconnect/reconnect | Slot/epoch and attempt-generation CAS reject stale writes; no resurrection/cross-slot token use. | G3 real PostgreSQL barriers/affected-row assertions. |
| S1-10 / GDI26-08,13 / PH-03 | Pause U's old revoke; U consent/callback in same or other slot before/during/after it | Fenced/stale U attempts cannot commit, even after HTTP returns; V remains independent. Fresh post-200 consent validates credential. Failure/timeout quarantines U; no old revoke retry. | G2/G3 remote project-grant effect plus rejected fence-bypass control below. |
| S1-11 / GDI26-12 | OAuth 401/403/429/5xx, malformed/oversize body, redirect, timeout | Finite calls/deadlines; sanitized outcome, zero automatic mutation retries, no provider body/secret leak. | G2 HTTP faults, budget observations and safe log capture. |
| S1-12 / GDI26-15 | Existing JWT/Account/Media/Portability/Feed/RAG/API consumers | Preserve existing behavior; add one module/table and exactly four enabled operations. Credentials excluded from exports; Feed OFF and no RAG activation. | G4/G5 changed-boundary checks and qualified unchanged independent evidence. |
| S1-13 / GDI26-16 | Read operator setup/recovery guide | Exact callback/browser-cookie/key/ACL/backup/two-slot/revocation/Testing-expiry and later live/remote gates are accurate. | G6 docs; actual Google/browser/deployment NOT_VERIFIED. |

GDI26-10/11/14 and file-operation extensions of 09/12/13 are DEFERRED_OUT_OF_SCOPE to separately authorized S2/S3, not PASS or missing S1 cases.

## Non-goals — MUST NOT

No folders, Drive file discovery/list/upload/download/export, Picker, Explorer/frontend, preview, delete/move/rename/share/copy, service accounts, broad scopes, MediaStoragePort adapter, background sync/token refresh, scheduler, generic connector platform, durable OAuth state/job engine or multi-process routing. No new PPV login/Google-issued PPV JWT, bootstrap/PIN/PPV-token change, business-module dependency edge, existing-table alteration, historical migration rewrite, RAG QA/indexing, Feed activation, live account access or remote HTTPS rollout. Normal Maven resolution for the approved managed Java dependency is permitted; no model/runtime installer or unrelated upgrade. Do not read/change the root ZIP or rewrite old reviews/logs/unrelated LOW findings.

## Simplicity decision and expected burden

| Decision | Smallest adequate implementation / burden |
| --- | --- |
| KEEP | Two fixed slots, verified subjects, guarded callback/external-key encryption; core Owner need, moderate Java work and substantive focused security/PostgreSQL proof. |
| REUSE | Existing JWT, root ApiResponse/ApiResponses, Spring HTTP/validation/OpenAPI, Nimbus JOSE, Flyway/Testcontainers and watchdog; no parallel PPV login or envelope. |
| SIMPLIFY | One Drive module/table, bounded ephemeral attempts/subject lifecycle records, synchronous finite OAuth calls and zero automatic retries/jobs; no distributed/durable coordinator. |
| SELECT | One Boot-managed Spring Security OAuth2 client mechanism; add spring-security-oauth2-client if needed and reuse existing HTTP/JOSE support. Explicit code/refresh clients, no oauth2Login/default login/authorization routes, sessions or competing Google SDK stack. Record resolved compatibility on Boot 4.1.1/Java 25/Modulith 2.1.1. |
| DEFER / REJECT | File/root operations, UI, generic adapters, multi-process coordination and benchmarks; none is required for S1 connection authority. |

Six logical verification groups may be split into shorter commands; no full-backend gate or new test framework is imposed.

## Exact HTTP and browser boundary

All enabled Owner routes retain bearer JWT and root ApiResponse data/error/meta. Success is HTTP 200 with error/meta null. Reuse module-local DTOs/advice; never serialize entities or create a global error/envelope replacement.

| Method / route / operationId | Exact response and errors |
| --- | --- |
| POST /api/v1/drive/connections/{slot}/authorize / authorizeDriveConnection | No request body; slot 1/2. data = {slot,authorizationUrl,expiresAt}. Set slot-specific proof cookie. Consent URL contains required state/challenge/nonce, never PPV JWT or Google tokens. 400 DRIVE_INVALID_SLOT; 409 DRIVE_REVOCATION_PENDING or DRIVE_REVOKE_UNCERTAIN for known slot quarantine; 503 DRIVE_CAPACITY_EXHAUSTED for bounded admission. Reconnect uses this route. |
| GET /api/v1/drive/oauth/callback / completeDriveConnection | Exact public GET only when enabled. Success: 200 text/plain, fixed “Google Drive connection completed. Return to PPV.” Failure: fixed “Google Drive connection could not be completed. Start again from PPV.”; 400 for invalid/expired/denied proof or identity, 409 for duplicate subject/replaced attempt/fence/stale CAS, 503 capacity, 502 provider refusal/malformed body, 504 provider deadline. No JSON envelope, reflected provider detail, variable redirect, script or third-party resource. |
| GET /api/v1/drive/connections / listDriveConnections | data = {connections:[{slot:1,status,displayEmail,remoteRevocation},{slot:2,status,displayEmail,remoteRevocation}]}, always two sorted entries. status DISCONNECTED/CONNECTED/RECONNECT_REQUIRED; absent row DISCONNECTED. displayEmail null when disconnected. remoteRevocation null when unknown, otherwise PENDING/REQUEST_ACCEPTED/FAILED/UNCERTAIN while retained. Successful fresh connection clears stale display. Never return sub/scopes/epoch/key ID/ciphertext/tokens; read-only, no provider call. |
| DELETE /api/v1/drive/connections/{slot} / disconnectDriveConnection | data = {slot,status:"DISCONNECTED",remoteRevocation}. Local success 200 even for remote FAILED/UNCERTAIN. Empty slot idempotent: invalidate pending attempts, no repeated revoke, return retained knowledge or null. Invalid slot 400. Local persistence failure: sanitized 500 DRIVE_PERSISTENCE_FAILURE, no revoke/false success. |

The plain-text callback is the explicit ADR-0016 browser-navigation exception; OpenAPI describes it and overrides bearer security only there. Disabled handlers are absent: authenticated Drive paths return 404, anonymous paths retain JWT 401 including disabled callback. No unconditional/wildcard permit. OFF retains the existing 235-operation manifest; a controlled ON context adds exactly these four = 239 without changing the existing 235 contracts.

Set no-store on these responses and Referrer-Policy: no-referrer on callback. Clear only the terminal attempt's cookie, never a newer attempt's cookie. No tokens/codes/state/cookies/consent URL/provider body in access/debug logs, traces or receipts. Consent URL in the authenticated initiation response is intentional feature data.

### Fixed OAuth and attempt parameters

- Fixed Google HTTPS authorization/token/JWKS/revoke endpoints; token/revoke secrets in form bodies, never query strings. No request-selected host/scope/redirect/return URL or Host/Forwarded-derived registered URI. Test endpoints/keys injectable only in controlled tests.
- Initial D07 redirect exactly http://localhost:8080/api/v1/drive/oauth/callback. Enabled S1 production configuration rejects a different redirect before provider I/O; remote HTTPS needs its separate gate.
- Request only openid, email, https://www.googleapis.com/auth/drive.file; access_type=offline, response_type=code, PKCE S256, nonce, explicit consent/account selection. Do not include previous broader grants. Verify actual scopes, recognizing Google's equivalent userinfo.email; reject missing required or broad Drive grants.
- Independent state/browser proof/nonce/PKCE verifier each use at least 32 CSPRNG bytes; unpadded base64url where appropriate. Store authenticated JWT Owner ID, slot, fixed URI, proof digest, nonce/verifier, attempt/slot generation, initiation order and expiry server-side. Callback query is never owner/slot authority.
- Max 4 pending-plus-processing attempts total, TTL 300s, one current attempt/slot. New start invalidates old slot attempt including an exchange already underway. Processing counts until finish/deadline; no eviction of live work. Excess fails 503 with no dispatch; lazy expiry, no timer.
- Cookies PPV_DRIVE_BINDING_1 / PPV_DRIVE_BINDING_2: host-only, HttpOnly, SameSite=Lax, Path=/api/v1/drive/oauth/callback, Max-Age=300. Secure under HTTPS; omission allowed only for exact D07 HTTP localhost development URI. Actual browser delivery NOT_VERIFIED until separately authorized proof; different API-client/browser cookie jars cannot complete S1.
- Consume state once and verify expiry, stored Owner/slot/URI context, latest attempt and constant-time cookie-proof match before exchange; recheck expiry/generation at commit. Max state 128/code 4096 characters; reject duplicate security parameters/code-plus-error ambiguity. Ignore unknown OAuth response parameters without letting them override stored context. Guarded provider denial consumes attempt without exchange.
- Reuse supported signed-ID-token verification: trusted Google keys/algorithms, Google issuer allowlist, client audience/authorized party where applicable, expiry, nonce, nonblank subject max 255. Never fetch a token-controlled jku/x5u. Email is display-only; no custom JWT crypto.

### Transport and resource bounds

Synchronous clients: 2s connect/5s response-read timeout, redirects disabled, TLS certificate/hostname checks on, finite pool admission, zero automatic retries for code/refresh/revoke. Token body max 64 KiB, JWKS max 256 KiB. Complete callback chain max 30s including key retrieval/refresh validation: at most one code exchange, one credential-validation refresh and two bounded JWKS fetches. Revoke max 8s total and one wire request. Each I/O uses remaining operation deadline; cancellation/cleanup cannot leave unbounded tasks.

Handle 401/403/429/5xx/Retry-After without automatic replay; sanitized failure requires fresh Owner attempt. Token-endpoint 401 is not permission for recursive refresh. S2 resource-operation retry is deferred. Failed identity/fence validation never revokes the newly exchanged token as cleanup.

## Persistence, encryption and concurrency

### One additive relation and credential custody

Create backend/src/main/resources/db/migration/V25__drive_connections.sql; V24 is latest at entry. If baseline/numbering changes, inspect/report rather than overwrite. New public.drive_connections only:

| Column | Mapping / constraint |
| --- | --- |
| slot | SMALLINT primary key, CHECK IN (1,2) |
| google_sub | VARCHAR(255) NOT NULL UNIQUE, verified stable subject |
| display_email | VARCHAR(320) nullable, display-only |
| granted_scopes | TEXT NOT NULL, canonical normalized set |
| status | VARCHAR(32) NOT NULL, CHECK CONNECTED or RECONNECT_REQUIRED |
| encrypted_refresh_token | BYTEA versioned AEAD envelope |
| key_id | VARCHAR(64), present exactly with credential |
| credential_epoch | UUID NOT NULL UNIQUE; fresh generation on credential change/invalidation |
| created_at / updated_at | TIMESTAMPTZ NOT NULL, UTC |

Explicit conditional CHECK: CONNECTED requires nonempty ciphertext/nonblank key_id; RECONNECT_REQUIRED requires both NULL. No DISCONNECTED row, owner/account FK, access-token column, file/folder table/column, durable pending/revoke table, new enum or existing-table modification. Validate migration/JPA/additive DBML; frozen v1 and V1–V24 stay unchanged.

PPV_DRIVE_TOKEN_KEY_FILE identifies an existing raw 32-byte AES key outside Git/PostgreSQL, OS owner-only permissions. Enabled startup validates key/client/redirect configuration; missing/unreadable/wrong-length key fails before provider work. OFF does not read it. Client inputs: PPV_DRIVE_OAUTH_CLIENT_ID, PPV_DRIVE_OAUTH_CLIENT_SECRET, PPV_DRIVE_OAUTH_REDIRECT_URI; default ppv.drive.enabled=false / PPV_DRIVE_ENABLED=false.

Use standard AES/GCM/NoPadding, fresh random 12-byte nonce/write, 128-bit tag, explicit version/key ID; authenticate unambiguously encoded slot+verified sub+credential_epoch as AAD. Define/document envelope encoding once; no custom crypto or PPV JWT-key reuse. Persist encrypted refresh token only; access tokens/verifiers ephemeral. Tamper/wrong key/cross-slot/subject/epoch swaps fail before dispatch. No secret-bearing toString/exception/log/response or database constraint values.

Before CONNECTED require a usable refresh token and one bounded validation refresh, retaining any legitimate rotation. Same-subject regrant without a token may preserve only a stored token validated as good. For a first grant whose identity/scopes are valid but refresh token is missing/unusable, apply the same final authority/uniqueness gates to persist verified metadata as RECONNECT_REQUIRED with NULL credential/key ID; callback returns generic 400 and authenticated status reports the recovery state. Unverified identity never creates that row. Refresh response omission preserves the validated candidate, never invalid old data; any refresh identity must remain bound to the same verified subject. invalid_grant atomically changes the matching slot/epoch to RECONNECT_REQUIRED and clears ciphertext/key ID. Transient failure does not destroy a different valid credential. No periodic refresh or automatic key rotation; key-loss recovery is explicit and documented.

Disconnect still removes local authority if its stored token cannot be decrypted or is absent. Make no fabricated token request: return local DISCONNECTED with remote FAILED, retain the conservative subject quarantine, and document explicit Google-side recovery. Never let key loss prevent local invalidation or log ciphertext/decryption detail.

### PH-03 — local authority and remote project-grant ordering

One small in-process lifecycle guard plus short PostgreSQL transactions; no guard/SQL lock/transaction spans Google I/O. Final writes, deletes and matching epoch checks linearize under that guard. Insert uniqueness or UPDATE/DELETE WHERE slot AND credential_epoch must check affected rows. Stale refresh never inserts an absent row; slot generations also stop old callbacks inserting after disconnect/new start.

1. Under the final-commit guard, reserve project/verified-subject revoke fence **before** local removal. Commit delete/invalidate pending attempts/cached credential before sending captured old token. Local DB failure restores pre-operation guard state and sends no revoke.
2. Local DISCONNECTED is immediate; parallel status can show remote PENDING. Active revoke blocks same-slot new starts and same-subject final commits in either slot. Unrelated V remains independent; U's paused HTTP holds no global guard.
3. Retain subject/project cutoff: an attempt initiated before/during its revoke cannot commit even after HTTP 200. Recheck attempt/slot/epoch/fence/cutoff inside final short transaction. Only explicit fresh post-200 consent is admitted; validate refresh token before CONNECTED.
4. One terminal 200 records REQUEST_ACCEPTED only. Definitive non-success FAILED; ambiguous timeout/transport loss UNCERTAIN. Both retain REVOKE_UNCERTAIN quarantine against same-subject commits in either slot/known-slot starts, without automatic retry/expiry/clear. Never revoke rejected fresh token.
5. At most two subject lifecycle records, counting connected-subject reservations, active/uncertain fences and retained cutoffs. Reserve at admission so each existing connection has capacity for eventual local disconnect. Full capacity rejects a new subject rather than preventing disconnect/evicting uncertainty. Already-admitted different subjects remain independent. Retire completed cutoff only when all pre-cutoff attempts expired/finished and no processing callback can commit; expiry rechecked at commit. Initialize at most two reservations from durable rows on startup. Exhaustion is explicit retryable 503, never an unbounded map.
6. Restart intentionally loses attempts/fences/transient remote knowledge; deleted credentials remain absent, old callback fails, no retry launches. Unknown remoteRevocation is null, never fabricated success. Restart alone does not prove an old remote request finished. Uncertain recovery: Owner inspects/revokes PPV access in Google security settings, verifies clean deployment/old operation ceased, then initiates fresh consent. No new recovery route or durable guarantee.
7. Delayed provider invalidation after 200 can still occur: later invalid_grant downgrades only current matching credential, without resurrection. Do not promise permanent provider validity, instantaneous propagation or crash-durable revoke coordination.

## Precise change map and reuse guard

Repository-relative paths; keep class splits minimal and record actual paths at handback.

| Permitted target | Required outcome / limit |
| --- | --- |
| backend/src/main/java/com/vhvkhangg/personalprivatevault/drive/** (new) | Drive package-info: no business dependencies; cohesive internal application/domain/persistence/OAuth/crypto/web ownership, minimal command/status contracts. No speculative public consumers, exposed entities/repositories or common/connector framework. |
| backend/src/test/java/com/vhvkhangg/personalprivatevault/drive/** and module-local fixtures (new) | Drive-prefixed focused tests/fake OAuth/JWKS/grant provider; no real secrets. |
| authentication/internal/infrastructure/security/SecurityConfiguration.java under Java root | Conditional exact GET callback permit; use configuration/framework types, no Drive-internal import. Preserve JWT/stateless/CSRF behavior otherwise. |
| backend/pom.xml; backend/src/main/resources/application.yml | Selected compatible OAuth-client support and default OFF/external inputs only; no oauth2Login defaults or unrelated upgrade. |
| backend/src/main/resources/db/migration/V25__drive_connections.sql (new) | Approved new relation/constraints only; normal migration even OFF, no OFF application credential writes. |
| portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java under Java root | **Only add drive_connections to EXCLUDED_SECURITY_TABLES.** Existing validateSchemaAllowlist rejects unknown public tables; this credential classification is needed to preserve exports. ALLOWED_TABLES/export format/business queries/dependency edges unchanged; no Drive-internal import. |
| Existing Authentication security/web tests, ApplicationArchitectureTests, web/OpenApiRouteInventoryIntegrationTest, migration/FlywayV1SchemaManifestIntegrationTest, portability/PortabilityIntegrationTest and narrow fixture support | Changed-boundary assertions/manifest reuse; preserve OFF 235 routes. Exclude new security table from physical v1/non-auth comparisons, preserving v1 69-table/67-export baselines; do not weaken assertions/clean unrelated tests. |
| Implementation-adjacent docs below; handoff/ACTIVE result fields | Actual delta/evidence/limits, no governance rewrite or premature publication/acceptance claim. |

Existing Account/Media/Feed/RAG production, root ApiResponse/ApiResponses/ApiExceptionHandler, other migrations, agents/skills/hooks/tooling are outside production edit scope. Report demonstrated necessity beyond this map before expansion.

Read root/backend/test AGENTS. Apply java-spring-coding-standards, pragmatic-solid-design, reuse-and-consistency, modular-monolith-architecture, jpa-postgresql-persistence, backend-testing, authentication-security, rest-api-http-contracts, database-migration-review, architecture-change and bounded-command-execution. Use design-pattern-selection only for concrete boundary choices. Python/RAG/model/evaluation skills/additional agents are N/A. Maintenance B1/B2 watchdog/hook acceptance and scoped Windows-root/trust limitations remain; legacy pending wording is not a tooling task.

## Negative controls and finite verification

Fake HTTP OAuth/token/revoke + signed JWKS, real Spring filter/context and Testcontainers PostgreSQL. H2/mocked repositories alone cannot prove constraints/CAS. Use injectable Clock/barriers instead of sleeps.

PH-03 fake must invalidate **all outstanding U/project grants, including R2**, when paused R1 revoke takes effect; invalidating R1 alone is insufficient. Prove same-slot/cross-slot U attempts before/during revoke reject even when completed later; V independent; local-delete failure sends zero revokes; failed/uncertain quarantine survives capacity pressure; no guard/SQL lock across paused HTTP; fresh post-200 flow validates credential; delayed invalid_grant clears matching state only. Bypass just the fence through a controlled test seam/mutation and show the remote-effect regression **fails**; restore guard for normal green gate. No committed production bypass flag; expected failed control is not passing acceptance.

| Group | Required focused responsibility / proposed classes |
| --- | --- |
| G1 web/security | DriveOAuthSecurityTest, DriveWebIntegrationTest: JWT/exact matcher/plain callback, state/cookie/PKCE/nonce, replay/expiry/concurrent attempts/wrong context, zero exchange on pre-guard failure. |
| G2 provider/identity | DriveGoogleOAuthClientTest, DriveGrantRevocationIntegrationTest: signed identity/scopes, token omission/rotation/validation, transport faults, grant-wide barriers/control/propagation/restart. |
| G3 DB/crypto | DrivePersistenceIntegrationTest, DriveCredentialCryptoTest: real migration/checks/uniqueness/CAS/delete rollback, key/AEAD, fence+epoch integration, no response/log/export secret leakage. |
| G4 contexts/resources | DriveConfigurationIntegrationTest: real OFF/ON/misconfigured startup, absent disabled handlers/key loading, fixed URI, bounded admission/expiry/cleanup, no default OAuth login/Feed/RAG/background activation. |
| G5 compatibility | DriveApiInventoryIntegrationTest plus existing SecurityFilterChainIntegrationTest/AuthWebIntegrationTest, ApplicationArchitectureTests, FlywayV1SchemaManifestIntegrationTest, PortabilityIntegrationTest, AccountWebIntegrationTest, MediaStorageIntegrationTest, FeedSchedulerLifecycleIntegrationTest. Exact ON/OFF manifest delta/module ownership. Prior independent Account/Media/Feed/RAG evidence reusable only with unchanged source/dependency inputs and receipt provenance; changed security/dependency/schema/API/Portability boundaries need fresh proof. |
| G6 docs/handback | Approved ADR/schema/module/API/operator delta and links; recursive changed-tree/dependency inspection; /antigravity-self-review, receipt ledger/counters/limits and Docs Sync Check. No invented live/IDE/static PASS. |

### Named final gate: GDI26-S1-focused-final

Run from repository root. Equivalent cohesive Drive-prefixed class names allowed if handback maps responsibilities; selector includes all new Drive tests. No new runner script.

~~~powershell
$gdiPython = (Get-Command python -ErrorAction Stop).Source
$gdiRun = Join-Path (Get-Location) ('logs/gdi26/s1-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $gdiRun -ErrorAction Stop | Out-Null
$gdiGroups = [ordered]@{
    'drive' = 'Drive*Test,Drive*Tests'
    'security-api' = 'SecurityFilterChainIntegrationTest,AuthWebIntegrationTest,OpenApiRouteInventoryIntegrationTest'
    'architecture-schema-export' = 'ApplicationArchitectureTests,FlywayV1SchemaManifestIntegrationTest,PortabilityIntegrationTest'
    'adjacent' = 'AccountWebIntegrationTest,MediaStorageIntegrationTest,FeedSchedulerLifecycleIntegrationTest'
}
foreach ($gdiGroup in $gdiGroups.GetEnumerator()) {
    $gdiReports = Join-Path $gdiRun ($gdiGroup.Key + '-surefire')
    New-Item -ItemType Directory -Path $gdiReports -ErrorAction Stop | Out-Null
    & $gdiPython scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $gdiRun ($gdiGroup.Key + '.json')) -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' "-Dtest=$($gdiGroup.Value)" test
    if ($LASTEXITCODE -ne 0) { throw ('GDI26-S1 gate failed: ' + $gdiGroup.Key) }
    $gdiSelected = $gdiGroup.Value.Split(',')
    $gdiReportFiles = @(Get-ChildItem -LiteralPath 'backend/target/surefire-reports' -Filter 'TEST-*.xml' | Where-Object {
        $gdiClassName = (($_.BaseName -split '\.')[-1] -split '\$')[0]
        @($gdiSelected | Where-Object { $gdiClassName -like $_ }).Count -gt 0
    })
    if ($gdiReportFiles.Count -eq 0) { throw ('Missing fresh Surefire XML: ' + $gdiGroup.Key) }
    $gdiReportFiles | Copy-Item -Destination $gdiReports -ErrorAction Stop
}
~~~

**Independent-review capture correction (2026-11-06 / FR-08):** the original command's report-directory property did not produce the declared XML directories in this checkout. The corrected sample snapshots only selected suites from actual Surefire output into a newly created run directory immediately after each group. Verify actual fresh suite counts/timestamps and skips; do not accept stale target reports as new evidence. Preserve the original logs and missing-report observation; no POM/tooling rewrite or repeated unchanged full gate is required merely for this instruction correction.

All mandatory S1/changed-boundary cases pass without silent skips/exclusion. Adjacent unchanged cases may instead use qualified independent receipts under G5; state reused selectors rather than claim execution. Split expensive groups into finite normal-profile calls; do not extend/bypass cap or waive failures. Normal cap 300s; policy's up-to-2100s profile needs a separately applicable approved full-suite gate, absent here. Preserve logs; unique runner receipt/stdout/stderr and Surefire paths prevent overwrites/stale counts. Report nonzero promptly with safe category. Startup/hook/trust failure does not authorize tooling repair.

Handback records actual commands/input hashes/exits/counts/receipt+stdout+stderr+Surefire paths and independent reused evidence separately. No real credential/key/Cloud artifact/raw secret URL in tracked evidence. The expected failed fence control has its own unique receipt. Mandatory tests cannot be satisfied by source-only inspection.

## Documentation impact and owner

Antigravity may edit **only these implementation-adjacent docs** for D04 after behavior is known. Follow [templates](../../../templates/README.md) and [Docs Sync contract](../../../workflow/documentation-synchronization.md); no premature independent acceptance/Owner publication.

| Affected current/additive docs | Observable sync evidence |
| --- | --- |
| docs/adr/0026-google-drive-connection-authority.md (new), docs/adr/README.md | D00–D07 authority, module/table/exact callback, security-table export exclusion, single-process/ephemeral limits/alternatives; no global relaxation. |
| docs/database/google-drive-connections.dbml (new), docs/architecture/data-architecture.md | Canonical additive relation exactly matches V25/JPA/checks; link beside frozen v1 without editing it. |
| docs/architecture/module-boundaries.md, docs/architecture/module-dependency-matrix.md, docs/repository/repository-package-tree.md | Drive-owned relation/packages, zero business edges; recursive actual source/test inventory and Modulith proof. |
| docs/architecture/google-drive-connection-authority.md (new), docs/architecture/security-architecture.md, docs/architecture/api-architecture.md, docs/architecture/architecture-overview.md | Four operations/public exception/OFF-ON behavior/security/data-flow delta; preserve existing auth/Media/Feed/RAG contracts. |
| docs/architecture/structurizr/workspace.dsl and directly corresponding current module/container/functional sources/exports under docs/architecture/diagrams | Only applicable Drive/Google delta. Inventory current-view source/derived links, edit source before affected export, record exact paths/checks. Dated SAD-01–04 remain snapshots at their stated revision; no Archify history regeneration/new framework. Unaffected views need no rewrite; state applicability. |
| docs/architecture/google-drive-connection-operations.md (new), docs/architecture/README.md | Exact localhost/same-browser flow, config/default OFF, external key/Windows ACL/backups, Testing expiry, local-vs-remote revoke/uncertainty/recovery, no file/UI ability; real Google/browser/HTTPS NOT_VERIFIED, no agent Cloud setup. |
| This directory's implementation-handback-s1-2026-11-06.md (new), self-review evidence, handoff/ACTIVE result fields | Actual ID/test/source/dependency/receipt mapping, PH-03 control, limits/counters/next action. Preparation/decision/independent review files remain immutable submission history. |

This invocation writes **only this handoff and ACTIVE**. ChatGPT owns later routine governance/closeout notices after independent acceptance/actual Owner publication. Root AGENTS/policy/legacy agent-rule cleanup, GDI26-PH-L01 historical-anchor repair, unrelated backlog and S2/S3 expansion are not authorized.

Primary premises rechecked 2026-11-06: [Google revocation](https://developers.google.com/identity/protocols/oauth2/web-server#tokenrevoke) covers project-grant invalidation/delayed effect; [Google OIDC](https://developers.google.com/identity/protocols/oauth2/openid-connect) covers stable subject/validation; [token expiration](https://developers.google.com/identity/protocols/oauth2#expiration) covers External/Testing seven-day caveat for non-basic scopes. Contract inputs, not live-provider evidence.

## Definition of done and handback

1. Implement only permitted targets/MUST cases; four APIs, additive relation, secret/ordering bounds and documentation are reviewable.
2. Complete G1–G6/named gate or qualified unchanged reuse, plus failing fence control and restored passing run. No mandatory S1 case remains NOT_VERIFIED.
3. Run /antigravity-self-review and write finite handback with source/DB/API/docs delta, evidence/limitations/finding IDs. It is not independent review.
4. Fill Implementation result; change this handoff/ACTIVE to IMPLEMENTED_AWAITING_CODEX_REVIEW. Return to Codex $codex-final-review; no staging/commit/push/tag/PR.
5. Independent Codex findings/missing mandatory evidence gate acceptance: CRITICAL/HIGH/MEDIUM block, LOW/INFO recorded. At most three correction/re-review rounds, stop on acceptance; no automatic takeover.
6. Only after READY_FOR_OWNER_COMMIT does Owner publish; ChatGPT later records actual-publication closeout. No S2/S3/activation/whole-GDI acceptance/counter reset follows.

### Blockers / operational limits

No preparation decision is open. If approved crypto/schema/fence cannot meet its contract or a change outside the map is necessary, stop with concrete counterexample/smallest decision; do not redesign silently. Missing real Google/browser/Cloud setup does not block deterministic implementation/fake acceptance; it prevents claims of live/remote operation. Multi-JVM routing, crash-durable coordination/permanent propagation guarantees remain outside S1.

P17 FROZEN, P18 CLOSED, S19-1 accepted/published, S19-2 SUSPENDED/NOT_ACCEPTED/ARCHIVAL_ONLY. External indexing INACTIVE, live originals DISABLED/DEFERRED, PPV_FEED_SCHEDULER_ENABLED=false. Historical S2B 3/3 accepted/stopped and other counters/evidence unchanged.

## Implementation result — initial Antigravity submission (preserved)

The statements below record the initial implementer submission, not current independent acceptance. FR-01..09 in the final review dispute all-case completeness, the negative-control proof and XML-preservation claims; original receipts/stdout/stderr and handback are retained.

**IMPLEMENTED_AWAITING_CODEX_REVIEW** (2026-11-06). All 13 acceptance cases (S1-01 through S1-13) implemented and verified.
- **Canonical named gate `GDI26-S1-focused-final`:** PASSED 141/141 tests with 0 failures, 0 errors, across all 4 test groups.
  - `drive`: 30/30 tests passed (Exit 0, 15.7s).
  - `security-api`: 17/17 tests passed (Exit 0, 18.2s).
  - `architecture-schema-export`: 55/55 tests passed (Exit 0, 18.6s).
  - `adjacent`: 39/39 tests passed (Exit 0, 23.4s).
  - Receipts, stdout, stderr, and Surefire XML reports preserved in `logs/gdi26/s1-3dcfcc059b3a4cffb9839b92de9a91b1/`.
- **PH-03 verification & negative control:**
  - `DriveGrantRevocationIntegrationTest`: Verified paused revoke in slot 1 blocks new starts in slot 1 and same-subject commits in either slot, while independent user V in slot 2 connects without interference. Pre-disconnect attempts cannot commit even after HTTP 200. Fresh post-200 consent validates and succeeds.
  - Negative control `negativeControlProvesProviderGrantWideInvalidationWithoutFence`: Demonstrated provider grant-wide invalidation regression and proved fence prevents it.
  - `revokeFailureOrTimeoutQuarantinesSubject`: Demonstrated remote revoke failure quarantines subject in `REVOKE_UNCERTAIN`.
- **Implementation delta:**
  - Database: `V25__drive_connections.sql` (additive relation, constraints, indexes).
  - Security & Portability: `SecurityConfiguration` (conditional exact `GET /api/v1/drive/oauth/callback` permit), `PortabilitySnapshotAdapter` (`drive_connections` excluded from export snapshots).
  - Application configuration: `application.yml` (`ppv.drive` config, default OFF).
  - Module `drive`: Cohesive internal packages (application, config, crypto, exception, oauth, persistence, web/controller, web/dto). Zero business dependencies.
  - Architecture & Ops docs: `ADR-0026`, `google-drive-connections.dbml`, `google-drive-connection-authority.md`, `google-drive-connection-operations.md`, `workspace.dsl`, `.dot` diagrams, and architecture/data/security/API overview updates.
  - Mandatory `/antigravity-self-review` completed cleanly. Handback written in `implementation-handback-s1-2026-11-06.md`.

## Preserved Codex takeover submission result

**IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED.** One exceptional Owner-authorized Codex implementation pass addresses FR-01..09 only. All nine remain OPEN_PENDING_INDEPENDENT_REVIEW; this is not finding closure or acceptance. Fresh named gate is 224 tests (Drive 113, security/API 17, architecture/schema/export 55, adjacent 39), zero failures/errors/skips. An actual isolated fence-bypass run failed one assertion after reproducing R2 grant invalidation; restored production admission passed in the final gate. Exact receipts, source hashes, fresh selected XML, per-finding/probe mapping, self-check and diagrams/docs provenance: [corrective handback](implementation-handback-s1-2026-11-06.md). Antigravity remains 0/3; one exceptional takeover executed.

**Docs Sync Check (takeover):** UPDATED — allowed current handback/status/ADR/additive schema/operator/module/API/source/export documents; scoped links and source/export inventory checked. ALREADY_CONSISTENT — D00–D07/default OFF, frozen v1/V1–V24, existing security/export boundaries, original review/evidence. DEFERRED_OUT_OF_SCOPE — FR-L01/PH-L01, SAD snapshots, legacy notices and unrelated backlog. NOT_VERIFIED — independent acceptance, real Google/Cloud/browser/HTTPS/ACL deployment, complete IDE/static inspection (four timeouts), and formal Graphviz/Structurizr semantic validation. No unresolved Owner decision for this submitted scope.

## Preserved initial Codex remediation disposition — historical



**Initial independent review: CHANGES_REQUESTED**, [2026-11-06 final review](reviews/2026-11-06-gdi26-s1-independent-final-review.md). Resolve GDI26-S1-FR-01..09 within the existing approved production/docs map; five HIGH and four MEDIUM block. FR-L01 is LOW/nonblocking; GDI26-PH-L01 and unrelated history remain preserved. Remediation **0/3 consumed**; next submitted Antigravity correction is **1/3**, not a reset. No takeover. Required route: implementation correction + focused regressions/control/evidence + mandatory self-review, then independent Codex re-review.

## Preserved initial final-review disposition

**CHANGES_REQUESTED / initial independent review completed 2026-11-06.** All four named-gate groups independently passed (141 tests, zero failures/errors/skips). Thirteen isolated diagnostic checks nevertheless reproduced contract violations; the diagnostic harness exit 0 denotes completion, not acceptance. Reviewer source hashes, receipts, selected XML snapshots and counterexamples are preserved in `logs/gdi26/codex-s1-review-1793942945569/`. See the full review for precise locations, required corrections, acceptance and all shared coverage categories. No READY_FOR_OWNER_COMMIT or commit message.

**Docs Sync Check (independent review):** UPDATED — this review/status/capture-instruction amendment and ACTIVE. FINDING — FR-08/09 missing control/report proof and canonical docs. ALREADY_CONSISTENT — bounded S1/default OFF, physical V25 and credential export exclusion; mandatory deterministic behavior remains blocked. NOT_VERIFIED — missing runtime cases plus separately gated live Google/browser/deployment and IDE/static checks. DEFERRED_OUT_OF_SCOPE — preparation/Owner decisions/old reviews/logs, SAD snapshots, legacy notices and unrelated LOW backlog. No production or submitted test source edited by reviewer.

**Docs Sync Check (creation):** UPDATED — this handoff and ACTIVE with finite future doc ownership. ALREADY_CONSISTENT — published preparation/re-review 2, clean entry/no competing handoff and D00–D07. Creation validation: 27 local links resolve, all 13 acceptance rows/six evidence groups are present, existing final-gate class selectors resolve, PowerShell gate parses without execution, and whitespace/fences pass. NOT_VERIFIED — all implementation/dependency/runtime/Cloud/browser/deployment; future paths above are targets, not delivered docs. DEFERRED_OUT_OF_SCOPE — preparation NO_ACTIVE labels, PH-L01, stale root/scoped AGENTS/policy/agent notices and unrelated LOW backlog. No production/historical evidence/counter/publication changed.

## Current independent takeover review

**READY_FOR_OWNER_COMMIT**, [independent takeover review](reviews/2026-11-06-gdi26-s1-independent-takeover-review.md), 2026-11-06. FR-01..09 independently VERIFIED / CLOSED; all 13 deterministic acceptance cases PASS. Fresh reviewer evidence: `logs/gdi26/codex-s1-independent-takeover-review-1793950183141/` — 224 tests, zero failures/errors/skips; actual fence-bypass assertion failure and the identical restored-green selector separately preserved. Source/transaction/security/transport reviews support the executable results. Initial review and all old receipts/handbacks remain unchanged.

**Docs Sync Check (independent takeover review):** UPDATED — ACTIVE, current fields/ledger in this handoff and new review. VERIFIED — material additive schema/ADR/operator/module/API/current source/export consistency, scoped links and exact reviewed backend inputs. ALREADY_CONSISTENT — D00–D07, default OFF, frozen v1/V1–V24 and existing JWT/export boundaries. LOW / OPEN / DEFERRED — FR-L01 package descriptors, new FR-L02 isolated inventory decision citation, PH-L01 and unrelated history/backlog. Submission-state diagram badges may be refreshed in the brief actual-SHA publication closeout. NOT_VERIFIED / SEPARATELY GATED — live Google/Cloud/browser/HTTPS/key-ACL deployment, complete IDE/static and formal diagram-tool validation; no activation, S2/S3 or RAG scope follows.

**Next step:** Owner | normal git add/commit/push, then ChatGPT brief publication closeout | GDI26-S1 [ACTIVE](../../handoffs/ACTIVE.md) and [independent takeover review](reviews/2026-11-06-gdi26-s1-independent-takeover-review.md) | READY_FOR_OWNER_COMMIT; publish reviewed default-OFF delivery and record actual SHA, no separate staging review or activation | Antigravity 0/3 consumed; one exceptional bounded Codex takeover ACCEPTED / STOPPED.

