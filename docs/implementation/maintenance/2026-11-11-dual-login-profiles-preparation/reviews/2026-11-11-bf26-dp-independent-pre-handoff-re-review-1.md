# Independent pre-handoff re-review 1 — BF26-DP

- **Date / role:** 2026-11-11 / Codex independent preparation reviewer in this invocation; review of the previously supplied correction, not implementation or author self-acceptance.
- **Mode / result:** Owner-approved maintenance preparation / **CHANGES_REQUESTED**. PH-01, PH-02 and PH-03 are **CLOSED at preparation level for the proposed DP-A boundary**; PH-04 remains **MEDIUM / OPEN** for the finite contract gaps below. No runtime acceptance or B1/B2 readiness follows.
- **Verified baseline:** local `main` HEAD, `origin/main` and live remote `refs/heads/main` all `9c7e123d01237e057675d348ddb93700a4850e51`. BF26-S5 is published; this BF26-DP package remains untracked/unpublished.
- **Authority:** `$codex-pre-handoff-review`, [D1–D6 ratification](../owner-ratification-d1-d6-2026-11-11.md), [corrected scope](../README.md) and [author synchronization handback](../documentation-synchronization-handback-2026-11-11.md). D1–D6 are settled prospective requirements. Exact source/frozen-baseline authorization remains a later gate, not a reason to ask for those six decisions again.
- **Execution:** `NO_ACTIVE_HANDOFF`; no application tests, build, migration, provider/browser/IDE execution, source change, staging, publishing or new implementation/remediation/takeover round. H1-F/FR-13, historical counters, approved v0.3, dependencies and paused RAG remain preserved.

## Scope, evidence and limits

Reviewed all eleven top-level BF26-DP Markdown inputs, the original independent review and route ledger, synchronization receipts/manifests, the five changed current-navigation files, and the earlier deferred Owner decision. The original review and author check are evidence inputs, not fresh runtime results. Source/guidance inspection included:

- Authentication `AppUser`, `RefreshToken`, `SessionService`, `JwtTokenService`, `JwtProperties`, `SecurityConfiguration`, Auth controller/advice/DTO contracts, Google controller and ticket-redemption seam; relevant Auth/PostgreSQL test sources and existing token fixtures.
- Portability security exclusions/schema preflight; singleton/FK/PIN constraints in V1 and Google identity/attempt/ticket constraints in V28. Inventoried and hashed V1–V28; did not re-audit every historical RAG SQL function or connect to a database.
- All 34 controller declaration files across 21 modules, module descriptors/dependency matrix, the eight directly relevant module directory trees (Authentication, Portability, Vault, Fiction, Film, Media, Personal, Search), mixed Fiction/Film DTO fields, Search materialization boundaries and conditional Drive registration. The remaining modules' route and structure inventories were statically qualified; this is not a new whole-Backend implementation audit.
- Root/backend/docs/Authentication instructions; current Owner policy, Docs Sync, package-tree reference and applicable ADR-0006/0008/0016/0017/0028; pre-handoff/coverage/documentation/authentication/testing/HTTP/migration/architecture skill guidance; existing backend implementer, security/schema/auth/workflow rules and hook/config declarations. No new agent, rule, hook or dependency is necessary. Existing unrelated historical routing notices are deferred below. Graphify executable exists but `graphify-out/graph.json` is absent.

Fresh [static evidence](2026-11-11-bf26-dp-re-review-1-static-evidence.json) independently reconciles **254 method/path/source-line declarations, 34 controllers and 21 modules**, with matching controller source hashes and no extracted/ledger differences. This is **not a live registered-route count**, OpenAPI execution or permission proof. Conditional/provider/framework registrations remain future runtime gates. All **3,344 tracked files** and all 23 pre-existing package/delivery files matched the [review entry manifest](2026-11-11-bf26-dp-re-review-1-entry.json) before reviewer writes. The author synchronization's recorded documentation hashes also matched. Static validation checked 254 local targets/anchors across sixteen preparation/navigation Markdown files without an issue. Final preservation/whitespace/link results are recorded separately in the final validation receipt.

Read-only inspection errors were corrected: a missing guessed advice path was resolved to `AuthExceptionAdvice.java`; Windows glob/path guesses were replaced with discovered paths; Python console encoding was set explicitly to UTF-8 after one read-only output error. An initial annotation substring scan included `RestControllerAdvice`; the final exact annotation scan and declaration comparison above exclude those advice classes. None of these inspections executed application code or changed prior evidence.

## Adjudication of the original findings

| Finding | Current disposition | Evidence and exact limit |
| --- | --- | --- |
| **BF26DP-PH-01 / HIGH** | **CLOSED — preparation sequencing/invariant** | Corrected slices:9–16 and auth design:7–13,45 require the deny-Public business boundary in A simultaneously with both Public login and downgrade issuance; A17/A18/A20 require signed persisted tokens, unknown/async/ERROR denial, no bytes/side effects and a Private positive control. This fixes the unsafe A-before-B plan. The remaining exact authentication compatibility table is retained under PH-04 rather than counted twice. Runtime enforcement remains NOT_VERIFIED. |
| **BF26DP-PH-02 / MEDIUM** | **CLOSED — selected DP-A preparation boundary** | Ratified D1 and the corrected policy deny all business data in A, including generic Vault/Search. Later B1 limits candidates to explicitly qualified read projections; credits/People references, full DTOs, writes and taxonomy mutations are denied/deferred. This avoids a reverse Vault dependency and removes undecided mixed grants from the first deployable slice. No B1/B2 route/field/write contract is independently accepted here; each still requires its stated future preparation/grant gate. |
| **BF26DP-PH-03 / MEDIUM** | **CLOSED — preparation integration/backup contract** | Ratified D5; auth design:8,37,47 and corrected A15/A19 co-deliver migration, `EXCLUDED_SECURITY_TABLES` and owning integration tests. Portable ZIP exclusions are distinct from restricted full PostgreSQL recovery, with restored authority invalidated/key rotated before exposure. Current adapter:120–130,245–268 confirms the strict schema dependency. Actual DDL, migration/export/restore proof remains an implementation gate. |
| **BF26DP-PH-04 / MEDIUM** | **OPEN — residual preparation contract gap** | The major product choices are resolved. The package still explicitly requires, but does not supply, the exact Auth compatibility/matcher table and an unambiguous persisted-session lifetime/reset contract. See below. No new product approval or alleged current-source vulnerability is inferred. |

The [initial review](2026-11-11-bf26-dp-independent-pre-handoff-review.md), original ledger and ratified decisions remain immutable. This dated adjudication supersedes their finding status for current review routing only.

## BF26DP-PH-04 — remaining finite correction

**MEDIUM / OPEN / blocking readiness of this submitted implementation preparation.** This is a missing preparation contract, not a demonstrated exploit in the current singleton implementation.

**1. Complete the promised authentication compatibility/allowlist table.** `authentication-session-and-migration.md:62–71` still calls the API a sketch/candidates and explicitly says existing login, refresh, revoke, bootstrap and Google need a precise compatibility table. A12/A21 require exact approved matchers without providing that table. The state-machine logout row at line 57 delegates authentication to an unspecified “selected compatibility contract.”

The current boundary has materially different entry points: `SecurityConfiguration.java:110–129` permits anonymous bootstrap/login/refresh/revoke, health/docs and conditional callbacks; PIN is protected. `AuthController.java:64–75` receives refresh/revoke material in explicit JSON, whereas Google has authenticated password-verified link/start and anonymous callback/handoff. `GoogleSignInStore.java:257–289` currently consumes a ticket and issues tokens transactionally. D4 selects temporary issuance denial while preserving linking, but the exact callback/link/handoff/error treatment is still missing. A blanket `/auth/**` grant or a bearer requirement accidentally applied to refresh is not an equivalent implementation of these contracts.

**Smallest correction:** one compact table for each existing/new A operation, identifying HTTP method/path, anonymous/PUBLIC/PRIVATE reachability, proof required, success/error envelope, authority mutation and old-client behavior. Cover bootstrap/status, both logins, refresh/revoke, elevation/downgrade, Public credential setup/reset, PIN, and Google OFF/ON-unmapped link/start/callback/handoff. State disposition of existing health/docs/callback exemptions separately from the business deny; an Authentication prefix is not an allowlist. Fix the selected request fields/validation and error outcomes needed to review those distinctions. Class names, internal helpers, exact DDL column spelling and generated OpenAPI are implementation/handoff details; this finding does not require code, a full SQL migration or a new endpoint.

**2. Define lifetime and credential-reset outcomes without inventing a new timeout.** Auth design:35,60 preserves existing token TTLs while requiring a bounded session expiry, without saying what advances that expiry. Current `SessionService.java:106` computes each successor refresh expiry as `now + refreshTokenLifetime`; `JwtProperties.java:21–22` defaults to 15-minute access and 30-day refresh lifetimes. An absolute session cap from initial login and an expiry advanced by valid rotation are different behaviors even with those same token TTL values. The package does not specify which rule the new persisted authority follows through refresh and switches. D3 ratification selects no new server idle timer; it does not supply that missing rule.

Additionally, A10 (`implementation-slices-and-acceptance.md:48`) still qualifies Public-credential reset as “if selected,” while ratified D2 selects Private-password-proved setup/reset and invalidation of affected sessions. Specify which lineages/generations reset invalidates, including a lineage that has switched profile, and whether an old rotated/consumed refresh may revoke current lineage authority or receives an idempotent no-effect result. Existing `SessionService.java:122–137` revokes only the presented row and ignores an already-revoked predecessor; D3's lineage logout is an intentional prospective semantic change. “Revoke affected sessions” alone does not define the late-revoke/reset acceptance outcome.

**Smallest correction:** add a short operation/state table with the session expiry formula and update rule, generation/profile effect, active/expired/consumed-token outcomes, reset scope and lock participation. Preserve accepted rotation/lifetime behavior unless a genuine policy amendment is explicitly submitted to the Owner. No Private recovery, global revoke service, idle scheduler, stronger in-flight cancellation or extra product decision is requested. D1–D6 need not be ratified again; only a newly proposed deviation from them needs a consolidated Owner decision.

**Preparation closure proof:** cross-reference the completed tables from A09–A15/A20/A21 and harness G-AUTH/G-SWITCH. Give deterministic expected results for rotate→logout, logout→rotate, reset versus refresh/elevate and session expiry across refresh/switch. Confirm no anonymous matcher bypass, no optional omission of D2 reset and no implicit lifetime tightening. Independent source/table review suffices for this preparation correction; runtime tests remain prohibited here and mandatory later. Do not write or activate an implementation handoff to resolve these missing semantics.

## Required capabilities, necessity and effort

Estimates are qualitative planning judgments, not measured time or a new implementation authorization.

| Owner-visible capability / mechanism | Disposition / simplest adequate mechanism | Implementation / verification effort and approval |
| --- | --- | --- |
| One Owner/shared dataset | KEEP singleton/FKs and existing bootstrap | Low preservation effort; synthetic PostgreSQL invariants; already selected |
| Distinct Public login and Private password | REUSE hashing/legacy login; add only Public hash/configured state | Medium; exact PH-04 contract, negative credentials/reset; later bounded source/schema approval |
| Immediate lineage downgrade/password elevation/logout | KEEP persisted generation authority; SIMPLIFY to local DB transactions | Medium/high; PH-04 lifecycle tables plus signed-token/lock/admission evidence; D3 in-flight trade-off retained |
| Safe first release | KEEP central A deny on all Public business routes | Medium; representative bytes/writes plus unknown/redispatch/positive controls; no per-module rewrite in A |
| Non-NSFW catalog view/reference lookups | DEFER actual grants to B1 with owner-qualified projections | Medium later; selected route/field proof before any opening; no Public full DTO inference |
| Public writes/credits/tags/Vault/Search | DEFER to selected B2 scopes; deny meanwhile | High later if selected; safe owner API and pre-ranking qualification; no reverse dependencies |
| Portable export and disaster recovery | REUSE adapter/format; same-A exclusions and manual recovery contract | Medium; PostgreSQL/export/security negatives; no backup platform |
| Google/Drive and retained PIN | KEEP immutable identity/PIN, OFF flags; D4 blocks unmapped issuance | Medium focused fake-provider/Private-only PIN proof; exact PH-04 compatibility; no live provider activation |
| New agent/harness/dependency/performance platform | REMOVE from current need; REUSE JUnit/MockMvc/PostgreSQL/Modulith/OpenAPI and bounded runner | Existing backend implementer/review roles suffice; no speculative library or numerical test quota |
| Frontend/BFF, Recycle Bin, RAG, final audit, second Owner/arbitrary visibility | DEFER separately gated work; REJECT second Owner/general visibility framework | No current implementation/verification burden; no hidden expansion |

## Finite future verification and delivery gates

The corrected plan's **A → separately selected B1 → separately selected B2 → C** is proportionate. A includes Authentication/security, one additive migration after actual V28, Portability exclusions/tests, narrowly affected real-session fixtures and approved ADR/DBML/API addenda. It grants no business data to Public. B1/B2 do not need fully designed future APIs to close A preparation; they cannot inherit READY from it.

After PH-04 docs correction and independent readiness: Owner publishes preparation, separately approves the exact source/frozen-baseline delta, then Codex `$codex-create-handoff`. Antigravity implements and self-reviews; separate Codex reviews each selected slice before Owner publication. C qualifies actual accepted source/receipts and only necessary changed-source integration; no publication-before-review circular prerequisite. Three future remediation/re-review rounds per active slice remain policy; none is consumed/reset by this preparation review. Takeover requires its separate authorization.

| Future gate | Finite positive/negative proof | Evidence boundary |
| --- | --- | --- |
| Auth/matchers/legacy | Real Public login and downgrade tokens denied on private data, unknown route, PIN and applicable async/ERROR dispatch; legitimate Private/anonymous auth operations retain selected semantics; legacy/unbound/missing claims fail closed | Real filter chain, persisted sessions, signed JWTs and explicit JSON/envelope assertions; mock principal alone is insufficient |
| Switch/refresh/reset/expiry | Both transaction orderings, lost reply, stale generation, DB failure, exact PH-04 reset/expiry outcomes; other lineages unchanged | PostgreSQL barriers/controlled clock; old JWT remains cryptographically valid but is rejected at new admission |
| In-flight D3 | Already-admitted read/write/stream may finish; new admission/redispatch after switch commit rejects old generation | Deterministic admission/commit controls and actual HTTP streaming only where needed; no retroactive erasure claim |
| Migration/export/recovery | V28 synthetic data forward-migrates; singleton/FKs retained; Private ZIP succeeds with security contents absent; restored authority invalid/key rotated before exposure | Real PostgreSQL, Portability owning integration/schema inventory and a bounded operator recovery contract; no H2 or production data |
| Google/PIN | OFF defaults, ON-unmapped issuance denial with no unusable ticket consumption; approved link/one-use/identity rules, Private-only retained PIN | Existing fake-provider/owning tests; no real Cloud/browser acceptance |
| B1/B2 later | Hidden=absent, safe fields, denied relations/writes until selected, parent/mutation races, text/tag/credit filtering before rank/limit/materialization | Only selected owner scopes; no blanket cross-module grant or arbitrary volume benchmark |
| Architecture/API | No new module/reverse/internal edge; selected errors/security/OpenAPI agree | Existing Modulith and route-inventory integration tests, exact selected configurations |

Reuse existing fixtures where adequate; narrowly extend `AbstractWebIntegrationTest` and `SecurityFilterChainIntegrationTest` rather than weakening authorization. Future long commands use the implemented `scripts/run-bounded-command.py`: ordinary 300 seconds, up to 2,100 only for separately approved eligible full suites, fresh receipts and owned cleanup. This review authorizes no suite. Browser SID/CSRF/cookie/cache/multi-tab proof remains deferred; Java evidence will not certify it. Performance and IDE warnings are NOT_VERIFIED; there is no IDE-clean or latency claim.

## Coverage ledger — all 29 considerations

PASS means preparation/static inspection only; planned tests are not executed results.

| # / consideration | Status / evidence or explicit limit |
| --- | --- |
| 1 Intent/non-goals | PASS ratified D1–D6, singleton, A deny and explicit deferrals |
| 2 Traceability | FINDING PH-04 exact Auth/lifetime/reset contract; remaining A/B test IDs are proposed gates |
| 3 Architecture | PASS prospective ADR/schema/API deltas identified; frozen sources unchanged; exact authorized addendum is a later gate |
| 4 Ownership/dependencies | PASS Authentication authority, owning resource checks, no reverse Vault/internal imports proposed |
| 5 Folder/package structure | PASS recursive relevant inventories, existing capability/public/internal grouping and descriptors; no new module/package or `.gitkeep` in eight directly inspected module trees; no general cleanup |
| 6 Shared contracts/exceptions | PASS root ApiResponse and owner-local advice retained under ADR-0016; new Auth errors need PH-04 table |
| 7 SOLID/cohesion | PASS minimal authority in Authentication; implementation class design N/A at preparation |
| 8 DRY/KISS/YAGNI | PASS reuse/necessity matrix; no generic policy/harness framework |
| 9 Patterns | PASS existing services/repositories/owner APIs suffice; new pattern layer N/A |
| 10 Domain/state | FINDING PH-04 expiry/reset/revoke; D3 switch/device/admission choices otherwise explicit |
| 11 API/wire | FINDING PH-04 method/proof/error compatibility table absent |
| 12 Input/validation | PASS bounded/redacted credential inputs required; exact new DTO validation remains PH-04; runtime NOT_VERIFIED |
| 13 Auth/privacy | FINDING PH-04 exact Auth exemptions; PH-01/02 A boundary repaired; enforcement NOT_VERIFIED until implementation |
| 14 Secrets/backup/retention | PASS hash-only/no seed, same-A ZIP exclusion and full-recovery separation; no new cleanup service required |
| 15 Transactions/concurrency | FINDING PH-04 operation outcomes/reset scope; lineage-before-refresh lock and D3 in-flight model accepted in plan |
| 16 Failure/recovery | PASS fail-closed lookup, cutover, lost response and pre-exposure recovery obligations; PH-04 late revoke/expiry unresolved |
| 17 Database | PASS source tip V28, additive/FK/history constraints; new DDL and live DB NOT_VERIFIED, not needed to execute this docs gate |
| 18 Complexity/resources | PASS bounded local session lookup; avoid DB locks across network/stream; no unsupported performance threshold |
| 19 Retrieval/index quality | N/A RAG/recommendation unchanged; ordinary Search grants explicitly deferred to B2 qualification |
| 20 OCR/document fidelity | N/A no parser/OCR/document capability |
| 21 Security testing | PASS finite risk-based future negatives; PH-04 expectations need completion; runtime NOT_VERIFIED |
| 22 Test quality | PASS existing signed-token/PostgreSQL/barrier infrastructure and anti-bypass fixture boundary; no tests run |
| 23 Performance benchmarks | N/A new benchmark obligation; runtime overhead NOT_VERIFIED and no performance claim |
| 24 Evaluation science | N/A no model/evaluation/corpus selection change |
| 25 Dependencies/lifecycle | PASS no dependency/pin/lock/install delta; latest-release/security audit N/A to this docs scope; FR-13 preserved |
| 26 Build/tooling/static diagnostics | NOT_VERIFIED Maven/IDE analyzers; static declaration/link/hash/whitespace checks separately evidenced; no IDE-clean claim |
| 27 Logging/observability | PASS no secret/body logging and synthetic/redacted receipts required; actual new logging N/A, no observability service |
| 28 Docs/governance | FINDING PH-04 remaining in-scope contract; scoped Docs Sync below |
| 29 Hygiene/delivery | PASS fresh reviewer artifacts, preserved input/index/source hashes and limited README status update; no publication |

## Docs Sync Check and handback

- **UPDATED:** this new dated review, fresh reviewer evidence/validation and only BF26-DP README review-status/next-action fields. No author protocol, ratification, prior review, ledger or receipt is rewritten.
- **ALREADY_CONSISTENT:** five navigation files match the prior synchronization hashes at entry; S5 publication verified live, `NO_ACTIVE_HANDOFF`, Frontend pause/FR-13, counters, RAG restrictions and flags preserved. Their pending-review/all-four-open statements describe the entry checkpoint; this review is the later adjudication.
- **REQUIRED WITH PH-04 CORRECTION:** ChatGPT supplies the two bounded tables and aligns A acceptance/harness. Update current PH status/next-action references in the five already approved navigation targets to this review, without changing ACTIVE execution authority or rewriting immutable ratification/history. These review-following status edits are routine documentation ownership, not a new implementation gate.
- **DEFERRED_OUT_OF_SCOPE:** older backend/docs/scoped instruction notices and package-tree/matrix historical status, backend implementer's old “B1 pending” wording, broad repository cleanup, B1/B2 exact data grants, Frontend redesign/FR-13 remediation, Recycle Bin, RAG and final audit. No proposed new capability is a blocker here.
- **NOT_VERIFIED:** application/build/HTTP/DB/migration/race/provider/browser/performance/IDE behavior. Future gates above are mandatory only in their authorized slices. No Antigravity implementation/test handback exists for DP-A.
- **OWNER DECISIONS:** D1–D6 remain ratified. No repeated approval request. Only a newly proposed material deviation or the later exact source/frozen-baseline delta needs its own Owner consent.

**Result: CHANGES_REQUESTED — one remaining MEDIUM finding, BF26DP-PH-04.** No ready commit message, docs-only acceptance, source authority or executable handoff is issued. This submitted implementation preparation is not silently converted into a docs-only deferral to bypass its missing contract.

**Next step:** ChatGPT | bounded PH-04 documentation correction, then separate Codex `$codex-pre-handoff-review` | BF26-DP auth compatibility/matcher and lifetime/reset tables, aligned A acceptance/harness and current notices; this review | PH-04 MEDIUM OPEN; preparation publication, exact source/frozen consent and handoff remain subsequent gates | implementation/remediation/takeover not started; historical counters unchanged.
