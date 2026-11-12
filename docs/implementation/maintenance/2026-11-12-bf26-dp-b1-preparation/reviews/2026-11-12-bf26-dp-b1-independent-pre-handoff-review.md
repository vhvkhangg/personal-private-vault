# Independent pre-handoff review — BF26-DP-B1

- **Date / reviewer / mode:** 2026-11-12 / Codex, independent of the supplied preparation author / Owner-approved maintenance preparation, review only.
- **Baseline:** local `main`, `HEAD`, `origin/main` and fresh `git ls-remote origin refs/heads/main` all resolve to `6374537074afc32ed7e939cf94ed2f21d77c5f42`. At entry, the only worktree delivery was the nine untracked B1 Markdown files; tracked diff and index were empty.
- **Authority:** [D7–D11 Option A ratification](../owner-ratification-d7-d11-2026-11-12.md), retained [D1–D6](../../2026-11-11-dual-login-profiles-preparation/owner-ratification-d1-d6-2026-11-11.md), [DP-A publication closeout](../../2026-11-11-dual-login-profiles-preparation/owner-publication-closeout-dp-a-2026-11-12.md), [independent DP-A acceptance](../../2026-11-11-dual-login-profiles-preparation/reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md), current Owner policy and `ACTIVE=NO_ACTIVE_HANDOFF`.
- **Final status: `READY_FOR_HANDOFF / B1_PREPARATION_ONLY / NOT_AUTHORIZED_FOR_IMPLEMENTATION / UNPUBLISHED`.** No blocking preparation finding was found. D10 is qualified at design level by the concrete ownership-compliant mechanism below; its implementation and mandatory runtime evidence remain future gates. This review creates no executable handoff or source/frozen exception.

## Requirement and simplest-mechanism check

The approved maintenance candidate is one bounded vertical slice: two authenticated direct detail GETs, each with a dedicated minimal PUBLIC DTO, and six authenticated Reference GETs with unchanged DTOs. PRIVATE keeps its accepted behavior. All unselected PUBLIC business methods and paths remain denied before handler invocation. The Owner explicitly selected non-NSFW plus active/correct-type Vault eligibility; this is an approved visibility premise, not a claim that source inspection proves every existing title non-confidential.

| Owner-visible behavior / mechanism | Decision | Simplest adequate mechanism and evidence | Implementation / verification effort; authority |
| --- | --- | --- | --- |
| Two direct Fiction/Film reads, D7 | KEEP / REUSE | Existing detail paths and operation IDs; route-local branch on the validated request authorities produced by `SessionAuthorityFilter`, not a client profile parameter | Small adapter/security delta; positive cases from both real PUBLIC origins and PRIVATE compatibility; exact source exception still required |
| Fixed safe fields, D8 | KEEP | Separate owner HTTP DTOs and small immutable application projections, six Fiction keys / seven Film keys; canonical `ApiResponse` | Small per-owner mapping/query addition; exact serialized-key assertions, including null allowed fields; no new generic serializer |
| Six Reference GETs, D9 | REUSE | Existing `ReferenceController`, catalog operations, ordering and DTOs; only exact method permissions change | Small matcher delta; all six list/detail controls and the other six Reference GET denials; no Reference service rewrite needed |
| Consistent feature/Vault eligibility, D10 | KEEP / SIMPLIFY | One owner-local read-only PostgreSQL REPEATABLE READ transaction, existing `vault::entry` call, no cross-owner SQL | Moderate, concentrated in two read operations and deterministic PostgreSQL barriers; design below; no schema or dependency exception indicated |
| Same paths, full PRIVATE schemas, D11 | KEEP / REUSE | Preserve existing PRIVATE `find`/mappers; explicitly describe two profile-conditioned response schemas and the PUBLIC cache/error behavior | Moderate contract checks; include root OpenAPI configuration and affected inventory assertions in the later exact source map |
| Revocable profile/session admission | REUSE | Accepted DP-A filter/authority lifecycle; no duplicate session lookup or auth algorithm in feature modules | Reuse unchanged, source-applicable acceptance; run affected route/redispatch regressions, not the full DP-A restore/browser suite |
| Framework, agents, hooks and dependencies | REUSE / REMOVE from proposed new work | Existing backend implementer, JUnit/MockMvc/PostgreSQL/Modulith, bounded runner and Git protections suffice | No new agent, rule, hook, dependency or benchmark platform justified |
| Browse/search, taxonomy/credits/links/posters, writes and visibility column | DEFER | Not in D7–D11; deny by existing PRIVATE fallback | No B1 implementation/test project for future functionality; negative denial coverage only |
| Frontend/BFF, providers, RAG, Recycle Bin, final non-RAG audit | DEFER | Separate existing Owner gates | No activation, runtime work or approval inferred |

No D7–D11 revote is necessary. Effort descriptions are relative engineering estimates, not measured duration or performance claims. A new per-record confidentiality policy would require a new Owner decision; it is not silently introduced here.

## D10 — exact design qualification and limits

**Observed source facts:** `FictionService.find` at line 140 and `FilmService.find` at line 154 currently read and map full feature views, including classification queries. Those PRIVATE operations must not become the PUBLIC mapper input. Both owning services already legally inject `VaultEntryOperations`. `VaultEntryService` has class-level `@Transactional(readOnly=true)` at line 21; `find` at line 74 has no different propagation/isolation override and exposes `entryType/deletedAt` through `VaultEntryView`. Both feature module descriptors permit `vault::entry`, `vault::view` and `vault::enums`. `application.yml:11` disables Open EntityManager in View. No feature/Vault second-level or query-cache configuration was found in the inspected module/resource scope.

**Qualified prospective mechanism (source-supported inference, not runtime PASS):**

1. Add a distinct minimal read operation in each owning feature's existing application capability and a minimal immutable view; keep the existing PRIVATE operation unchanged. Invoke the new operation through the injected Spring proxy from its own controller.
2. That operation owns a **new read-only REPEATABLE READ transaction**, for example method-local `REQUIRES_NEW` with `Isolation.REPEATABLE_READ`. This prevents a caller's weaker ambient transaction from silently determining isolation. Do not place the annotation on a self-invoked helper or merely on a repository read that finishes before the Vault call.
3. Within that same physical transaction and fresh persistence context, read the owner-local allowed scalar projection, requiring `isNsfw=false`, and call the existing injected `VaultEntryOperations.find(id)`. Its default REQUIRED propagation joins the outer snapshot. Check exact expected Vault type and `deletedAt==null`; return empty for every ineligible case. Do not reuse an entity/view resolved before this transaction, enable a result cache, or serialize feature entities/full views. No People, Media, genre, link or credit enrichment is needed.
4. End the transaction before returning the immutable allowed projection to the HTTP mapper. Missing feature/Vault, wrong type, trash and NSFW resolve to the same existing per-feature 404. Transaction/query failure produces safe failure, never a cached grant.

The inspected default single-datasource arrangement supports this approach without a new Vault API, reverse dependency, cross-owned SQL, row lock protocol, Flyway migration or visibility column. The later exact source approval must cover the new feature capability/view and owning query/service/adapter changes; this review is not that approval. No global transaction-manager configuration change is required or approved.

PostgreSQL REPEATABLE READ keeps successive reads on the snapshot established by the first non-transaction-control statement. Spring REQUIRED participates in the enclosing transaction, while REQUIRES_NEW creates an independent transaction with its own isolation. These are the design basis, not evidence that a not-yet-written B1 method actually does so. See [PostgreSQL isolation](https://www.postgresql.org/docs/18/transaction-iso.html#XACT-REPEATABLE-READ) and [Spring propagation](https://docs.spring.io/spring-framework/reference/data-access/transaction/declarative/tx-propagation.html). Ordinary separate READ COMMITTED reads would not establish D10.

**Mandatory carry-forward into B1-16/17/22:** assert effective isolation, the proxy/transaction boundary and joined Vault read on real PostgreSQL; use fresh request threads/contexts and committed synthetic fixtures. Pause between the feature and Vault reads, commit a controlled change, then release. Include an atomic state transition from `(non-NSFW, trashed)` to `(NSFW, active)` so no instant has both eligible predicates: a split-snapshot false 200 must be impossible. Also prove the accepted earlier-snapshot case and a new request after NSFW/trash commit. Avoid sleeping as the ordering proof. The existing five-connection test pool requires bounded concurrent actors and no accumulated suspended outer transactions; do not introduce a pool/lock framework.

A request whose data snapshot predates a change may complete with the previously eligible minimal DTO under ratified D10. A later request must see committed ineligibility. This is separate from the accepted DP-A authentication-admission boundary. If the implemented proxies, transaction manager or persistence/cache behavior invalidate these assumptions, STOP and return for bounded correction; do not replace the contract with two unqualified checks.

## HTTP, security and API handoff carry-forward

These resolve implementation planning details within the already approved design; they are not defects in existing DP-A, which correctly denies this entire surface today.

- Add only the eight exact **GET** permissions for validated `PROFILE_PUBLIC` or `PROFILE_PRIVATE`. Keep the ASYNC/ERROR guard **before** the new route grants, or otherwise qualify the grants by REQUEST dispatch, so same-URI redispatch cannot match a business grant first. Keep the PRIVATE fallback and all accepted infrastructure/Auth/provider exceptions. HEAD, OPTIONS, nested paths, direct Film Credit, taxonomy and unselected methods must not reach handlers; existing firewall/infrastructure rejections remain distinct from handler authorization.
- `ProfileRouteAuthorizationIntegrationTest.registeredRoutesDeniedToPublicTokenFromBothOrigins` currently asserts every business mapping is 403 and that the handler adapter is never invoked (`:38–124`). B1 necessarily changes those eight expectations. The future handoff must explicitly authorize a method+declared-pattern exception set, positive checks for its exact membership, and unchanged denial/no-handler/no-domain-mutation assertions for the remaining registered routes. Do not skip all Fiction/Film/Reference mappings or weaken the aggregate assertions globally. Real token helpers are in `support/AbstractWebIntegrationTest`, not `ProfileAuthFixture`; the latter supplies synthetic rows/snapshots/clock support.
- Root `OpenApiConfiguration.java:98` explicitly says PUBLIC is Auth-only and business operations require PRIVATE. The later exact file map must include this narrow description update as well as both controller response schemas and `web/OpenApiRouteInventoryIntegrationTest` expectations for the two existing detail operations. Retain bearer security and operation IDs. Represent both complete envelopes, with explicit profile conditions, rather than documenting the old full DTO for PUBLIC or introducing a new global polymorphic transport. A schema union must actually accept both serialized forms and preserve the strict PUBLIC property set; do not assume generated schema names or union validation without checking `/v3/api-docs`.
- Reuse existing Reference DTOs/lookup errors and ordering. No Reference owning-source edit is needed unless the final source-qualified implementation demonstrates one.
- Preserve existing PRIVATE full responses and behavior for NSFW/trashed records; do not accidentally apply PUBLIC visibility restrictions to PRIVATE. Test PUBLIC success and all hidden/absent 404s for `no-store`, canonical error bodies and no entity-derived validators, redirects or prior PRIVATE response contamination. No Frontend/BFF change is available in this slice.

## Reviewed preparation, source and harness inventory

All nine supplied B1 Markdown files were read. All **23 explicit source-path/blob pairs** in the static inventory match local HEAD. All **38 local Markdown file links** in the supplied package resolve. Fresh remote branch SHA verification supplements the connected GitHub read of the published DP-A closeout; author claims of remote inspection are not treated as new runtime evidence.

Reviewed root `AGENTS.md` current notice, `docs/AGENTS.md`, current Owner policy, Docs Sync contract, ACTIVE, B1 review instructions/ratification, D1–D6 and DP-A acceptance/closeout. Existing historical current-looking notices are subordinate to the latest dated publication notice, not a reason to reopen DP-A.

Relevant code inspected: Fiction/Film services, basic repositories, entities, current public operations, full response DTOs and enums; all Fiction/Film/genre and Reference controller GET declarations; Reference catalog service; Vault entry operations/view/service; feature module descriptors and dependency matrix; `SecurityConfiguration`, `SessionAuthorityFilter`, root envelope and OpenAPI configuration, main JPA configuration. Source GET declarations reconcile to **6 Fiction + 9 Film + 2 FictionGenre + 2 FilmGenre + 12 Reference**, not a live registration count. Only the two direct and six Reference GETs are selected.

Recursive relevant production structure: Fiction **84 Java files / 23 packages**, Film **102/26**, Reference **41/12**, Vault **58/17**, Authentication **91/21**. Every actual Java-containing package in these subtrees has `package-info.java`; no `.gitkeep` was found there. This is structural inventory, not a semantic audit of every unchanged class. Capability and owner-internal web placements fit the module matrix and ADR-0016. The frozen package-tree document contains an older illustrative Fiction `query` sketch; actual capability names/module descriptors control implementation, with the stale sketch deferred below.

Harness inspected: `AbstractPostgresIntegrationTest` (disposable PostgreSQL image, five-connection pool), `AbstractWebIntegrationTest` (real persisted PUBLIC login/downgrade and PRIVATE helpers), `ProfileAuthFixture`, route sweep, existing feature web test pattern and OpenAPI inventory expectations; presence of Fiction/Film/Reference web/domain suites was verified. Earlier DP-A source-qualified evidence is reused by reference for unchanged admission behavior only. No Antigravity B1 implementation/self-review/test delivery exists yet.

Applied review-coverage, documentation-writing, authentication-security, modular-monolith-architecture, Fiction/Film modeling, persistence, reuse and HTTP/backend-testing guidance; checked the existing bounded runner guidance, backend implementer and relevant phase 4/5/13 rules, workflow routing, `.codex/config.toml` and `.agents/hooks.json`. No agent, skill, hook or rule change is needed. Graphify output is absent; targeted source navigation was used. No IDE inspection was run and no warning-free assertion is made.

## Finite implementation and verification plan — proposed, NOT_RUN

Keep **one B1 vertical slice** and one independent slice review after implementer self-review; do not split source permissions from the corresponding DTO/eligibility enforcement. Exact filenames/selectors belong in the separately approved executable handoff, using the carry-forward map above. No arbitrary full-suite or benchmark quota is required.

| Gate | Existing B1 requirements / bounded future proof |
| --- | --- |
| G-B1-SEC | B1-01/07–12/14/18/20: both real PUBLIC origins, unchanged PRIVATE/ANON and DP-A admission controls, exact new method membership, denial/no-handler sweep for all remaining mappings, REQUEST/ASYNC/ERROR and path variants |
| G-B1-DB | B1-04–06/15–17/22: owner-local minimal reads, actual PostgreSQL Vault/type/NSFW/trash/restore, effective isolation and controlled split-state/commit races; no new dependency/DDL |
| G-B1-REF | B1-13/14: six selected unchanged catalog responses/order/missing-code errors; six unselected catalog GETs denied |
| G-B1-CONTRACT | B1-02/03/06/19/21/22: exact JSON keys, retained PRIVATE full DTO, indistinguishable 404 and cache headers, both OpenAPI variants/operation IDs/bearer security, Modulith boundaries |
| G-B1-REG | B1-23/24 and changed source: existing Fiction/Film/Reference web/domain and security/contract tests, plus source-qualified evidence/docs synchronization; review every selected class rather than assuming names establish coverage |

Use `scripts/run-bounded-command.py`, ordinary profile **300 seconds**, unique receipt/stdout/stderr paths and owned-tree cleanup. A gate may be partitioned by explicit classes to fit that bound. A 2,100-second full-suite profile requires its separate policy eligibility/approval; it is not granted here. Preserve failures/timeouts and source hashes; no totals are predicted. All B1-01–24 runtime cases are **NOT_RUN / NOT_VERIFIED at implementation level**, with sufficient preparation obligations specified. New implementation/remediation allowances do not arise from this review: initial B1 implementation is not started, remediation consumed **0**, no takeover authorized; all DP-A and other historical counters remain unchanged.

## Finding and coverage ledger

**No open CRITICAL/HIGH/MEDIUM preparation findings; no new LOW finding.** The concrete planning resolutions above must travel with the reviewed package into the later exact authorization and handoff. They do not certify runtime behavior or reopen DP-A's closed findings; DPA-L01 remains LOW/OPEN/BACKLOG.

`PASS` below means preparation adequacy/static inspection only. Runtime/security/transaction/OpenAPI results remain NOT_VERIFIED until authorized implementation evidence exists.

| Review dimension | Disposition / evidence and limit |
| --- | --- |
| 1 Intent and scope | PASS — D7–D11, eight routes and explicit non-goals |
| 2 Traceability | PASS — B1-01–24 mapped to five finite gates and carry-forward details |
| 3 Architecture/frozen contracts | PASS — legal existing module edges; no schema delta indicated; exact future frozen-source/HTTP approval remains mandatory |
| 4 Ownership and transactions | PASS design — owner projection plus existing Vault named API; D10 mechanism above; runtime NOT_VERIFIED |
| 5 Recursive folder/package structure | PASS static — five complete relevant subtree inventories and descriptors above; no new package implementation yet |
| 6 Shared APIs/exceptions | PASS — ADR-0016 root envelope, existing owner errors; no shared business/auth engine |
| 7 SOLID | PASS design — owner query/mapping responsibilities; no auth copied into domain services |
| 8 DRY/KISS/YAGNI | PASS — existing catalog/session/transport mechanisms reused |
| 9 Patterns | PASS — owner capabilities/repositories and explicit DTOs suffice; no speculative framework |
| 10 Domain correctness | PASS design — non-NSFW/active/type predicate, PRIVATE semantics retained |
| 11 HTTP/API compatibility | PASS design — same paths/IDs, explicit two schemas, root/inventory carry-forward; runtime NOT_VERIFIED |
| 12 Validation | PASS plan — exact methods/segments, malformed/unselected requests and safe error contract; actual firewall results NOT_VERIFIED |
| 13 Authorization/privacy | PASS design — persisted admission, fixed fields and default DENY; real HTTP proof NOT_VERIFIED |
| 14 Secrets/cache/egress | PASS plan — no new credentials/providers/URLs, no-store/leakage cases; runtime NOT_VERIFIED |
| 15 Isolation/concurrency | PASS design — D10 transaction and controlled split-state gates; runtime NOT_VERIFIED |
| 16 Failure/recovery | PASS plan — fail closed on DB/transaction failure; new backup/migration/restart subsystem N/A for these reads |
| 17 Database | PASS static/design — existing keys/NSFW/deletion state, owner-only projection, no proposed migration; SQL execution NOT_VERIFIED |
| 18 Complexity/resources | PASS design — bounded per-ID reads and unchanged catalogs; no measured latency claim |
| 19 Retrieval/recommendation quality | N/A — Search/RAG/recommendations denied and unchanged |
| 20 Document/OCR fidelity | N/A — no document/parser work |
| 21 Security testing | PASS plan — both real token origins, populated denials, forced orderings and redispatch; NOT_RUN |
| 22 Test quality | PASS plan — existing PostgreSQL/HTTP harness, exact assertions and controlled actors; B1 tests not yet written/run |
| 23 Performance benchmarks | N/A — no approved benchmark obligation or performance claim; bounded-read design reviewed |
| 24 Evaluation science | N/A — deterministic HTTP/DB acceptance; no model/evaluation work |
| 25 Dependencies | PASS static — unchanged POM/tooling; no upgrade/download proposed; new vulnerability scan N/A |
| 26 Build/tooling/diagnostics | PASS plan — existing runner/hook/harness; build and IDE inspection NOT_RUN; no IDE-clean claim |
| 27 Logging | PASS plan — existing safe errors, no content/token logs; future negative assertions required |
| 28 Docs/governance | PASS within review scope — authority/links/status and Docs Sync below; historical drift deferred explicitly |
| 29 Hygiene/delivery | PASS static — entry tracked/index diffs empty; only this review and B1 README status edited by reviewer; no publishing |

## Verification and scope-bounded Docs Sync Check

- **Fresh read-only checks:** exact Git/remote SHA, source blob reconciliation **23/23**, supplied local file links **38/38** and final package links **44/44**, controller source declarations, recursive package/descriptor/placeholder inventory, tracked/index preservation. Final SHA-256 comparison preserves all eight supplied non-README documents byte-for-byte. The new review passes an untracked-file `git diff --no-index --check`; the README scan flags only its three intentional two-space Markdown hard breaks (date/baseline/status), retaining the author's formatting. Tracked `git diff --check` is empty. These checks are not application tests.
- **Tool limitation preserved:** one combined read-only Python here-string command was rejected before execution by the repository PreToolUse hook for ambiguous/unclosed quoting. It produced no verification result. Subsequent plain Git/PowerShell read-only commands completed the checks; no hook, permission, trust or startup configuration was changed or bypassed.
- **UPDATED:** this dated independent review and the B1 README's current review disposition/index/next action only. Eight other supplied preparation files, including the Owner ratification, remain author snapshots unchanged; their pending-review language is superseded by the current README and this review.
- **ALREADY_CONSISTENT:** current root AGENTS/Owner policy/ACTIVE publication notice; DP-A accepted/published/closed, PUBLIC business/data default DENY today, no B1 source authority; Frontend/H1-F/FR-13 pause/blocked status, provider defaults, RAG suspension and historical counters. No new current-navigation contradiction is introduced by preparation readiness without implementation authority.
- **DEFERRED_OUT_OF_SCOPE:** routine root/index/roadmap/ACTIVE navigation synchronization, if requested for later Owner publication, belongs to ChatGPT; no readiness transition may make ACTIVE executable. Existing historical notice stacks, `docs/AGENTS.md` and workflow rule S19-1 wording, backend-implementer's old watchdog-pending bullet, and the frozen package-tree illustrative Fiction sketch remain for separately scoped documentation cleanup. The latest controlling policy/source is explicit; these older records do not authorize B1 or block this bounded preparation.
- **BLOCKED_OWNER_DECISION:** none for the ratified B1 design. Future exact source/frozen/API authorization is an explicit next prerequisite, not supplied by this review; any discovered need for schema, new module edges or changed confidentiality policy requires a separate decision.
- **NOT_VERIFIED:** B1 runtime/security/DB/OpenAPI/build/IDE behavior, live mapping registration, Owner data suitability, deployment/cutover and future publication. No tests, migrations, provider/browser process or production code were executed/changed. Existing DP-A reviewed receipts are preserved and not counted as fresh B1 tests.

Suggested preparation commit: `docs(backend): prepare bounded public reads for BF26-DP-B1`.

**Next step:** Owner, then Codex | Owner commit/push reviewed preparation and separately approve exact B1 source/frozen/API exceptions; only then `$codex-create-handoff` | BF26-DP-B1 README, D7–D11 and this review including D10/HTTP carry-forward | READY_FOR_HANDOFF is preparation-only; published preparation plus exact implementation authority required; ACTIVE remains NO_ACTIVE_HANDOFF | B1 implementation not started, remediation 0 consumed, no takeover authorized; DP-A 3/3 exhausted and takeover 1/1 accepted preserved.
