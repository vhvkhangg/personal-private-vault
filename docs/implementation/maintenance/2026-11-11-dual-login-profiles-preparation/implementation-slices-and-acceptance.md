# BF26-DP — proposed bounded implementation slices, dependencies and acceptance

## Corrective slice ordering and hard security invariant — PH-01/03 preparation CLOSED; PH-04 correction pending review

**Owner D1–D6 ratified for preparation; source implementation/frozen-boundary changes not yet separately authorized.**

| Slice | Deployable boundary | Publication precondition |
| --- | --- | --- |
| **A — Safe authority, session, schema and export** | Profile credentials/real sessions/refresh/JWT/cutover; **exact Spring Public Auth-only allowlist and default-deny for *all* business/data/unknown/async routes**; Portability security-table exclusions **and own integration tests** in same migration slice; Private preserves accepted functions | Independently ready preparation published; explicit Owner frozen-contract authorization; focused real Postgres/MockMvc/security/export proofs; independent implementation review |
| **B1 — Owner-local Public data reads** | Open ONLY selected non-NSFW Film/Fiction safe projections and independently vetted static Reference GETs. Explicitly deny Public nested credits/people and all writes until separately qualified. Maintain full denial for all other modules | A independently accepted/published; Owner-approved exact fields/routes; source/DTO tests and no-private-ID/reference negatives |
| **B2 — Mixed owner, writes, Search/Vault** | Only exact Owner-selected Public mutations, cross-module reference projections, generic Vault/tag/search and other approved pathways. Never let private tag/credit candidates affect rank/top-K/offset/hasMore | B1 accepted; specific ownership-safe contract and tests; no cross-owner internal imports/reverse Vault dependency |
| **C — Changed-source Backend closeout** | Qualify A/B receipts and run only necessary cross-slice focused tests/OpenAPI/Modulith/docs; publish truthful scope | All selected slices independently accepted; no implied Frontend/browser acceptance |

**A must be independently safe without B.** The older A/B/C proposal below does not authorize a more permissive first slice. Do not require a new global policy module or test framework. Recycle Bin/Final Non-RAG Audit after separately approved Backend work.

**Additional mandatory A acceptance (prospective, not run):** [Exact DP-A matcher and lifecycle contract](auth-compatibility-and-session-lifecycle-2026-11-11.md) controls A09–A15/A20/A21. A17 real signed Public login AND downgrade-issued Public JWT: 403 on any data route (representatives Media bytes/metadata, Personal detail, generic Vault, Portability export), unclassified URL, and applicable async/ERROR redispatch; zero private body/header/bytes/side effects. A18 Private legitimate positive control; unauthenticated 401; unknown/invalid/expired generation 401/deny; no `anyRequest().authenticated()` Public bypass. A19 V28→next migration + strict `PortabilitySnapshotAdapter` exclusion/test in **A**; Private ZIP success with no new auth table, hash, session, token or secret contents. A20 legacy unbound refresh/unprofiled JWT rejected; Google ON-unmapped behavior matches the exact table; persisted expiry/reset epoch, current-token revoke, stale/lost switch reply, rotate→revoke and revoke→rotate barriers independently checked. A21 exact selected Auth matcher/DTO/error/OpenAPI table, including **identifier-free PUBLIC success, wrong/unconfigured PUBLIC indistinguishable 401, rejection of PRIVATE branch, and preserved identifier-required legacy PRIVATE login**, plus `AbstractWebIntegrationTest` and `SecurityFilterChainIntegrationTest` profile-bound persisted token fixture changes; never let test-only helpers bypass authority. No new reset/recovery service or full backup automation by assumption.

**Additional future B1/B2 evidence:** bound-safe Public Film/Fiction projections (exclude or deny unqualified creator/credit/people, poster, review, notes and shared tags); 404 hidden=absent for known IDs; only approved Public writes with legal references; no Public shared taxonomy mutations until separately granted; private search/tag/credit candidates filtered **before** ranking and pagination. Whole Private-module 403; no reflected private ID/metadata in link/exception.


**Planning only.** There is **no executable handoff, source allowlist authorization or test result** in this package. All source examples are candidate edit boundaries for subsequent independent review. Cross-module restrictions are real security-critical work; they must not be collapsed into a single unreviewed batch.

## Proposed dependency order

| Proposed slice | Scope | Required prerequisite | What is *not* authorized |
| --- | --- | --- | --- |
| **DP-A: independently safe auth/session + Portability** | Singleton credentials, profile-bound revocable sessions and tokens, **Public Auth-only exact allowlist with default DENY on every business/data/unknown/async route**, add-after-V28 migration **together with Portability exclusions/integration tests**, password elevation/downgrade and legacy/Google cutover | Owner-ratified D1–D6, independently ready + published preparation and separately approved frozen schema/API/ADR delta | No Public data grants, no BFF, no Google/Drive activation |
| **DP-B1: only safe owner-local Public reads** | Owner-approved **read-only non-NSFW Film/Fiction safe projection** and explicitly safe static Reference GETs; all remaining business routes continue Public DENY | DP-A independently accepted/published, exact field/route policy ratified | No Public generic Vault/Search, credits/person/hidden references or writes |
| **DP-B2: mixed-resource grants and Public writes** | Only independently approved Film/Fiction writes, cross-module references, selected Search/Vault/tag/credit routes with pre-limit qualification; retain no-leak denials on all unselected paths | DP-B1 independently accepted/published and specific safe projections/write contracts ratified | No new arbitrary visibility, Public shared tag edits by default, Recycle Bin or user/tenant split |
| **DP-C: Cross-module regression and closeout** | End-to-end Backend-only policy matrix, migration/failure/replay/concurrency, static OpenAPI/architecture alignment, docs/freeze addenda; collect independently accepted A/B1/B2 evidence | DP-A/B1/B2 accepted on actual HEAD, source-drift qualified | No full Frontend integration or H1-F resumption, no final all-Backend audit by implication |
| **BF26-V1 Recycle Bin** | Later separately approved Vault browse with Public/Private trash filtering | DP policy accepted and Owner authorizes a distinct maintenance slice | No code in DP scope |

If DP-B cannot be split into a single reviewable finite scope, divide by *risk-bearing enforcement boundary* (private modules / Fiction+Film+relations / Search+Portability+derived) with separately accepted handbacks. Avoid introducing a reusable distributed authorization framework or cross-module entity/repository dependencies. Any core session+resource grant rollout must be **fail-closed**, not momentarily expose previously private data.

## A. Authentication & session acceptance (proposed IDs DP-A01..A16)

| ID | Scenario / test evidence that would satisfy the requirement |
| --- | --- |
| A01 | Existing V28 PostgreSQL data migrates forward; `app_users.id=1` and domain FK counts/row values remain unchanged. No second Owner/user row. |
| A02 | Public login disabled until explicitly configured with a distinct password through a Private-authorized flow; no default seed/forgotten password bypass; owner bootstrap remains one-time. |
| A03 | Existing PRIVATE Owner password login retains **required `{identifier,password}`** on legacy `/api/v1/auth/login`, issuing only PRIVATE. New PUBLIC-only `/api/v1/auth/profiles/login` accepts **`{profile:"PUBLIC",password}` without `identifier`** and resolves the fixed singleton Owner on the server; valid configured Public password issues a PUBLIC lineage. Wrong/unconfigured Public password yields identical `401`; `profile:"PRIVATE"`, missing password or extra `identifier` fails `400`. Public credentials cannot log in as PRIVATE. Password/PIN values and hashes never in responses, logs or SQL scripts. |
| A04 | Valid token has `sub="1"` plus `profile`, unguessable session identity and generation. Missing/unknown/forged/unsigned profile claim or unprofiled legacy token never gains PRIVATE. |
| A05 | PUBLIC→PRIVATE without password, with wrong password, with PIN, with stale Public refresh token, or with client-supplied `PRIVATE` role is denied; the previous session remains valid or is invalidated according to an explicit contract, never silently elevated. |
| A06 | PUBLIC→PRIVATE with correct freshly verified Owner password creates a new generation and replacement tokens; old Public token/refresh cannot race back to a privileged session. |
| A07 | PRIVATE→PUBLIC requires no password. **Immediately after commit**, previously issued Private access JWTs (still cryptographically valid/not expired) and Private refresh lineage are rejected by server-controlled authorization. |
| A08 | Concurrent downgrade, protected GET, streaming and refresh tests demonstrate no accepted *new* Private read after generation switch; accepted already-emitted bytes and in-flight race constraints are documented honestly. |
| A09 | Signed JWT and persisted lineage match current profile/generation. Successful refresh keeps profile/generation, rotates the **current** hashed refresh exactly once, sets refresh/session expiry to `now + configuredRefreshTTL` and leaves access JWT TTL at configured `A`; ordinary reads do not extend session authority. Expired/consumed/unbound/stale epoch rejected. No new fixed absolute cap or idle timer. |
| A10 | **Public-credential setup AND reset are mandatory** under D2: PRIVATE bearer plus fresh Private password, distinct Public password, epoch invalidation of **all ever-Public lineages**, including those currently PRIVATE after elevation; PRIVATE-only lineages unaffected. Active-current refresh `/revoke` invalidates its lineage; already-rotated/consumed token revoke is an idempotent no-op on its successor. No late refresh/elevation resurrection or new Private-password recovery subsystem. |
| A11 | Two backend HTTP clients/devices maintain independent correctly scoped sessions; downgrade affects **only the selected lineage** under proposed D3. No global revoke endpoint is selected; browser/BFF behavior remains deferred. |
| A12 | **Every row** of the [exact method/path/matcher/compatibility table](auth-compatibility-and-session-lifecycle-2026-11-11.md) is checked: anonymous bootstrap/legacy PRIVATE login/refresh/revoke; PUBLIC fixed-credential login **without identifier**, with configured-correct `200`, wrong/unconfigured `401` and other-profile/extra-identifier `400`; bearer-only switches/reset/PIN; health/docs and conditional Drive/Google exceptions. No `/auth/**` wildcard, Public business/unknown/async denial, truthful ApiResponse/303/status mapping and redaction. |
| A13 | Request authority fails closed on unknown/deleted/expired session, database unavailable, missing legacy claims, wrong profile/generation or obsolete Public credential epoch, rather than treating `sub=1` as full access. New admissions after commit are fenced; pre-admitted work follows ratified D3. |
| A14 | H1-AUTH immutable Google issuer/sub linkage and one-use integrity remain; Google flag OFF by default. With flag ON but unmapped: LOGIN start returns unavailable without creating an attempt, but PRIVATE password-verified LINK start and LINK callback preserve one-time Owner binding/opaque ticket (no PPV token); unmapped LOGIN callback fails fixed 303, and handoff rejects **before ticket consumption or PPV issuance**. No Google-success→PRIVATE/unprofiled bypass; Drive slots unaffected. The temporary enabled-mode change is D4-ratified, requiring scoped regression. |
| A15 | New security tables are excluded from **portable ZIP only**, via `PortabilitySnapshotAdapter.EXCLUDED_SECURITY_TABLES` and strict export tests **in DP-A**, while restricted full PostgreSQL disaster-recovery backups retain necessary authentication data. Restored tokens/sessions are invalidated and JWT key rotated before external access. Exact DBML/API/ADR delta reviewed; V1–V28 checksums unchanged. |
| A16 | Existing singleton Auth, bootstrap, refresh/PIN emergency, D31 Google and relevant concurrency suites remain green or any intentionally amended behavior has Owner-approved expectation and independent tests. No false historical PASS claims. |

## B. Data visibility and mutation acceptance (proposed IDs DP-B01..B16)

| ID | Acceptance obligation |
| --- | --- |
| B01 | Whole Media Private: Public album/image detail, album listing, counts, thumbnail/download/content bytes and mutations deny; Private retains authorized behavior. |
| B02 | Whole Personal Private: Public cannot query self/other profile, list, update, or infer any personal profile; Private allowed. |
| B03 | `isNsfw=true` Fiction/Film cannot be read/listed/searched/modified by Public; `isNsfw=false` remains available to Public and Private. |
| B04 | Public cannot flip private NSFW object into public by direct update, nor create/attach/update private object via one unguarded nested endpoint; service check is atomic with mutation, including concurrent reclassification. |
| B05 | Fiction/Film nested links/genres/credits/story classifications and known IDs are guarded against private-parent data disclosure; cross-module person/media references never leak private bytes or names. |
| B06 | Search filters hidden records **before ranking/top-K/offset/hasMore**; no Private tag-origin candidates, snippets, ID/type count or staleness signal leaks. |
| B07 | Vault entry metadata/tags/favorites/ratings, trash and restore by known ID do not permit Public operations on Private entries; Recycle Bin list remains unimplemented. |
| B08 | Public cannot get Private rows via join/relationship/query/metadata aggregates, hidden search domain and export preview; no post-limit-only security filtering. |
| B09 | Portability export is Private-only unless a separately approved Public subset is designed; deny before any ZIP or row-count/manifest emission. Keep existing security table exclusions. |
| B10 | No provider credentials/Drive filenames, private notes/diaries, financial totals, import duplicate metadata, raw feed metadata or response errors leak to Public under ratified fixed matrix. |
| B11 | Existing allowed Public operations retain validation, write consistency, concurrency protection, deleted-entry exclusion, and meaningful error semantics; reject Public attempts to create NSFW data. |
| B12 | Private retains all previously accepted legitimate operations (business errors and feature flags unchanged), without interpreting Private role as consent to activate RAG or external Google/Drive/Feed providers. |
| B13 | 401 missing/expired auth, 403 forbidden whole-module, 404 hidden known ID vs absent, malformed validation and API envelope are consistent with the approved contract; no side-channel via error body. |
| B14 | API/DTO/OpenAPI and module dependency tests pass with no unauthorized cross-module internal repository/entity injection; security decisions are enforced by owning modules plus the shared request authority. |
| B15 | Dashboard Option A existing reads are **not** incorrectly labeled Public-ready: any Private-classified card is independently forbidden when Public; no fake empty success or fake total/freshness. |
| B16 | Every ratified non-Public route in the 254-declaration snapshot is dispositioned; dynamically registered routes and conditional flags are explicitly qualified with tests. No route count is hard-coded as a runtime invariant. |

## C. Publication/final scope-specific acceptance (DP-C)

- **All mandatory security negatives** have fresh PostgreSQL/HTTP evidence, relevant scope-specific module cases, and one independently reviewed cross-module regression after DP-B. Historical H1-B/H1-AUTH records may be reused **with source qualification**, not counted as new executions.
- Frozen schema/ADR/API exception contracts receive **explicit approved addenda**, not retroactive mutation of old signed/baseline records. Documentation and OpenAPI inventories are truthful for enabled/disabled configurations.
- A separate final DP review can return `READY_FOR_OWNER_COMMIT` only after all selected implementation slices are independently reviewed, any blocking findings closed, migration/backwards-compatibility risks accepted and owner publishes actual code. This is not `NON_RAG_BACKEND_AUDIT_READY` and not completion of Frontend.
- If a design decision needs a migration/index/security policy beyond the approved scope, **STOP / OWNER_DECISION_REQUIRED**; no on-the-fly schema/harness expansion.

## Candidate source/test boundaries (not an implementation allowlist yet)

| Slice | Owning tree examples | Test evidence to extend |
| --- | --- | --- |
| A | `authentication` owner source/web/security; **`portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java` security exclusion and owning tests**; additive migration; prospective DBML/API/ADR addenda; narrow test fixture edits | AuthWeb/SecurityFilterChain/Google/PrivatePin tests, Portability integration/unknown table test, real profile-bound JWT fixture, concurrent switches and unknown/async requests |
| B1 | `fiction`, `film`, and limited safe static `reference` GETs, after Owner ratification | owner service/DTO/HTTP tests, non-NSFW and hidden-parent/direct-child negatives |
| B2 | Only needed `fiction`, `film`, `vault`, `search`, reference/linked owner operations and selected Private-module regressions | ranked/tag/credit/hasMore negative fixture, safe Public write barriers; **Portability adapter changes are already in A** |
| C | No new general module; API/source docs, route inventory/OpenAPI and focused security tests | `ApplicationArchitectureTests`, `OpenApiRouteInventoryIntegrationTest`, affected Modulith and Postgres tests; bounded commands and preserved evidence |

**Effort estimate (uncertain, no time commitment):** A = medium/high auth + deny gate + Portability migration; B1 = medium owner-local safe reads; B2 = high cross-module search/authorization and guarded writes; C = medium qualification. Do not imply this is a two-file/one-evening change or create numerical test-count quotas. If route classification causes too broad a DP-B scope, split before implementation instead of using arbitrary broad permissions.

**No runtime evidence generated in this preparation.** Review coverage and harness needs are detailed separately in [harness-readiness](harness-readiness.md).
