# Independent pre-handoff review — BF26-DP-B2-W1

- **Date / reviewer / mode:** 2026-11-12 / independent Codex / Owner-approved maintenance preparation only.
- **Baseline:** local `main`, HEAD, `origin/main` and live remote `main` all `9007f212adc91f7c1b5e1a5c466c8a102c13fff3`. Entry index and tracked diff were empty; only this five-file B2 preparation package was untracked. B1 source publication `277522ee5` and separate closeout publication `9007f212` are distinct.
- **Authority:** [dated D12–D17 ratification](../owner-ratification-d12-d17-2026-11-12.md), all Option A without amendments, for **B2-W1 preparation only**. [Decision docket](../owner-proposed-d12-d17.md), [source inventory](../source-baseline-and-risk-inventory.md) and [W1 acceptance plan](../slice-boundaries-and-review-plan.md) define this review. No numbered-phase or milestone gate is invented.
- **Outcome: READY_FOR_HANDOFF for B2-W1 preparation only. No open CRITICAL/HIGH/MEDIUM finding or unresolved material product decision was identified.** This does not authorize source changes, grant PUBLIC a POST, create an executable handoff or constitute runtime acceptance.
- **Execution / counters:** `ACTIVE = NO_ACTIVE_HANDOFF`, unchanged. B2 implementation NOT_STARTED; remediation **0 consumed**, takeover NOT_AUTHORIZED. DP-A/B1, Frontend and RAG findings, evidence and counters are preserved.

## Required capabilities and proportionality

| Owner-visible function / mechanism | Disposition / simplest reusable mechanism | Effort and approval boundary |
| --- | --- | --- |
| Authenticated PUBLIC creates one new Film | KEEP only `POST /api/v1/films`; reuse persisted profile admission and existing Film/Vault create capability | Small HTTP/security delta with real DB integration proof; exact frozen-source/API permission remains required |
| Five-key PUBLIC body | KEEP a dedicated strict request and explicit mapping: `title,originalTitle,nationalityCode,format,productionStyle`; server fixes `isNsfw=false`, no supplied relationship/private values | Moderate parsing/compatibility verification on the shared PRIVATE/PUBLIC route; no global mapper change |
| Seven-key 201 response | REUSE B1 `PublicFilmResponse`, canonical `ApiResponse` and existing safe errors; project only the new creation result | Small mapping/header delta; exact POST OpenAPI response contract must accompany implementation |
| Existing PRIVATE create/update/relations | REUSE unchanged behavior, input semantics and full response; regression is mandatory | Existing Film tests plus focused profile-branch comparison; no new PRIVATE product requirement |
| New transaction framework or duplicate Film validation | SIMPLIFY to existing owner transaction, command validation and Vault named API; a narrow adapter is sufficient unless a concrete limitation is demonstrated | No new transaction manager, repository sharing, global interface hierarchy or framework |
| Fiction writes, existing Film edits and private reference choices | DEFER to separately selected B2-W2 | No W1 authorization or reference-discovery endpoints |
| Search and generic Vault | DEFER to B2-S/V only if later needed; qualification must precede candidate ranking/pagination | No W1 query/ranking/metadata changes or three-page Search benchmark |
| New skills, agent, hooks, dependencies or benchmarks | REMOVE from W1 needs; existing backend implementer, tests and bounded runner suffice | No tooling/dependency approval requested |
| Frontend, RAG, providers, deployment and future audits | DEFER under existing separate gates | No activation or new acceptance claim |

This is one useful, separately committable vertical slice. Splitting W2/S/V is a scope limit, not a requirement to build them. No additional Owner vote on D12–D17 is needed. Publication and the already-required exact implementation authorization are the remaining workflow gates.

## Source qualification and feasible boundary

All **15 Git blob IDs** in the static inventory match the current published source after resolving abbreviated filenames. Important facts were checked directly rather than inferred from that inventory. Paths below use **J** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/` and **T** = `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

| Source fact | Preparation implication |
| --- | --- |
| `J/film/internal/web/controller/FilmController.java:51–55` currently accepts broad `CreateFilmRequest`, calls `FilmOperations.create` and returns full `FilmResponse`. `CreateFilmRequest.java:14–38` contains private/relationship inputs. | Adding only a PUBLIC matcher would violate D14. Profile-specific parsing, explicit safe mapping and response selection belong in the same slice. |
| `J/film/internal/application/FilmService.java:73–153` already validates, creates a `FILM` Vault identity, saves the Film and optional assignments in an owner transaction. `J/vault/internal/application/entry/VaultEntryService.java:61–70` uses normal transactional creation and no event publication in that method. | Reusing this transaction is feasible without changing Vault ownership. Null director and empty relationship sets skip the corresponding reference validation/assignment loops. There is no need to invent a cross-module create coordinator. |
| `FilmService.java:429–517` trims/validates titles, qualifies optional country through Reference, requires the two enums and provides existing private-state defaults. No director or genre is mandatory for Film. | Preserve existing validation/defaults; PUBLIC cannot select hidden fields. An internal default such as ONGOING/UNCONSUMED is not permission to accept those keys from the client. |
| `J/authentication/internal/infrastructure/security/SecurityConfiguration.java:225–262` is stateless Bearer security, CSRF disabled, ASYNC/ERROR PRIVATE guard before eight B1 GET grants, then PRIVATE default deny. `SessionAuthorityFilter.java:81–103` obtains persisted session authority and replaces authorities before dispatch. | Add only the selected authenticated POST after the dispatcher guard. Reuse admission; do not introduce a cookie/CSRF system, anonymous grant or global Auth rewrite. W1-11 means preserving this boundary against variants, not opening Frontend work. |
| `J/film/internal/application/FilmService.java:171–191` implements B1 PUBLIC detail with a new read-only REPEATABLE_READ transaction. | Preserve the GET snapshot. Do not invoke that REQUIRES_NEW read from inside an uncommitted create and expect it to see the new row. |
| `J/ApiResponses.java:40–49`, `ApiExceptionHandler.java:67–111`, and `film/internal/web/advice/FilmExceptionAdvice.java:24–31` supply canonical 201, safe validation/malformed 400 and domain 422 behavior. | Reuse root transport and module advice. No new common module or new error hierarchy is justified. |
| `T/film/FilmIntegrationTest.java:201–231` has a real rollback/no-orphan test; `FilmWebIntegrationTest.java:35–57` covers PRIVATE HTTP create. `T/authentication/ProfileRouteAuthorizationIntegrationTest.java` contains exact B1 exception membership, both PUBLIC origins, dispatch and no-mutation controls. | Reuse these seams and qualifying evidence, then add the new POST-specific proof. Existing tests alone do not qualify the not-yet-implemented PUBLIC path. |

### Carry-forward precision for the later executable handoff

These points make the existing W1-01–16 obligations concrete; they add no new user capability or implementation permission:

1. Choose PUBLIC versus PRIVATE from the validated server authentication, before interpreting the body as a mutation request. Keep PUBLIC unknown/duplicate-key and malformed/type handling local to its branch. A global strict mapper or forcing PRIVATE through the five-key DTO would break W1-07. Existing Auth strict-parser code is a pattern reference, not permission for Film to import Auth internals.
2. Reuse the existing title/original-title bounds, optional original title/country behavior, country validation and enum values. The handoff must spell out missing/null/blank/type and normalization examples and expected safe 400 versus 422 categories. Reject every nonselected PUBLIC key even when its value is null or false. Freeze these cases before implementation rather than choosing expectations after tests run.
3. The shortest candidate is explicit five-field-to-existing-command mapping with false NSFW, no director/private inputs and empty relation sets, then a safe projection of the newly created result. A dedicated application method is optional if it solves an actual boundary need; do not duplicate validation merely to name another create method. No full PRIVATE DTO may reach the PUBLIC wire.
4. Successful 201 must follow a committed owner transaction; prove insertion and rollback with an independent connection and an injected persistence failure. Keep response projection within the creation result or after commit, without splitting Film and Vault into independent commits. W1-04 is atomic database creation: it does not promise HTTP exactly-once delivery or undo a committed record after a client disconnect. No retry/deduplication product is approved.
5. Make W1-13 explicit in the handoff: PUBLIC creation success and its handler errors use no-store, with no entity-derived validators, private Location or redirect. Existing PRIVATE headers remain unchanged; do not redesign global caching. Subsequent detail uses unchanged B1 qualification. A concurrent PRIVATE edit can make that later GET hidden; the POST receipt need not reveal the edit.
6. Generated POST schemas must describe actual profile selection and complete request/response envelopes. A PUBLIC five-key request can also satisfy the broader PRIVATE request schema; avoid an exclusive `oneOf` that incorrectly rejects an overlapping valid body. Use an accurate alternative representation with profile descriptions and test real specimens against it. Preserve `createFilm`, existing PRIVATE schemas and the B1 GET schema graph.
7. Update the registered-route sweep to **exactly nine business method/path pairs**: the eight accepted B1 GETs plus this one POST. Give the selected POST valid safe content for each real PUBLIC origin, assert one committed creation per request, and preserve all other 403/no-handler/no-mutation and provider-ON denials. A blanket path exemption is insufficient.
8. Preserve D3: an admitted request may complete; stale/new requests after switch/revoke must fail authority. Cover the new POST at that boundary using deterministic barriers. Reuse unchanged refresh/session proofs by source qualification; do not require new cancellation semantics or rerun all authentication tests solely for a review label.

The future exact Owner authorization should identify the Film HTTP request/controller/mapper and any necessary owner capability changes, the single Auth matcher change, root OpenAPI delta and focused tests/docs. Vault/People/Reference production edits, schema changes and token-policy rewrites are not implied. If those become necessary, stop for a separate exact decision.

## Finite future verification plan — proposed, not executed

| Focused group | Original requirements / minimum useful evidence |
| --- | --- |
| PUBLIC HTTP create and validation | W1-01/02/03/05/06/13: real persisted login and downgrade-origin tokens; minimal/all-five valid bodies, optional null/omission, field/type/enum/length boundaries, extras including null/false, duplicate/malformed/trailing input; exact seven keys and safe errors/headers; no writes on rejected input |
| PostgreSQL creation/rollback and detail | W1-04/09/10: committed one-to-one active FILM/Vault identity, no relationships, injected write/commit failure with no orphan, safe failure response, then qualified B1 GET; no fake transaction proof or H2 |
| Admission and route denial | W1-01/08/10/11/12/16: exact nine-pair registered sweep for both origins, 401 stale/anonymous versus 403 live unselected, path/method/dispatcher controls, authority lookup failure and deterministic POST admission race; unchanged session evidence may be reused |
| PRIVATE/B1 compatibility, OpenAPI and architecture | W1-07/09/14/15/16: existing Film create/update/credit/link/genre regressions as affected, B1 field/snapshot behavior, generated request/201/error schemas and actual HTTP specimens, existing architecture/Modulith checks, frozen source/dependency/migration checks |

Use existing Testcontainers PostgreSQL, MockMvc, real session fixtures, source/case manifests and `scripts/run-bounded-command.py`. Each ordinary focused command has the existing **300s** hard cap, fresh receipt/log paths and owned-tree cleanup. The executable handoff must name selectors and source applicability; groups may share a command if they fit. No arbitrary test-count quota, new benchmark or full suite is imposed. Broaden only for concrete source impact or failures. Preserve failed attempts and distinguish fresh from reused evidence.

Mandatory implementer self-review precedes separate independent Codex final review of W1. Ordinary remediation follows the existing three-round policy; none is consumed here, and no automatic takeover is authorized. Future W2/S/V and any final aggregation require their own actual scope approval.

## Harness, package tree and shared coverage

Reviewed the five new preparation files, root/current ACTIVE and Owner policy, Docs Sync contract, `docs/AGENTS.md`, backend/test and Film/Vault/Auth scoped instructions; `codex-pre-handoff-review`, shared review coverage, documentation-writing, Film/Auth guidance and bounded-command skill; existing backend-implementer role, relevant Film/REST/workflow/package/testing rules, `.agents/hooks.json` and `.codex/config.toml`. Existing tooling is sufficient. No new agent, skill, hook, permission mechanism, parser framework or dependency is needed. Python/RAG skills and a takeover path are not needed for this preparation.

Checked the dependency matrix, ADR-0016, package-tree inventory, V1 Film/Vault schema, current module descriptors and relevant owner/controller/validation/test source. Recursive Java/package counts are Film **104/26**, Authentication **91/21**, Vault **58/17**, Reference **41/12**, Fiction **86/23**, Search **19/9**; every Java-containing package has `package-info.java`, with no `.gitkeep`. Film owns DTOs/application/persistence; Vault retains identity ownership; shared envelope/OpenAPI remain at the existing root boundary. The proposed slice needs no new module edge, migration, index or package hierarchy. Graphify output is absent; direct scoped navigation was used.

Statuses below concern **preparation coverage**, not completed future runtime behavior.

| Dimension | Status / evidence or limit |
| --- | --- |
| 1 Owner intent | PASS — D12–D17 ratified, one create-only route and explicit deferrals |
| 2 Traceability | PASS — W1-01–16 mapped to four finite groups above |
| 3 Architecture | PASS — source-qualified legal Film/Vault path; frozen exceptions await exact authorization |
| 4 Ownership/transactions | PASS preparation — existing owner transaction and named API; runtime atomicity NOT_VERIFIED until implementation gate |
| 5 Recursive structure | PASS — six relevant module trees/descriptors inspected |
| 6 Shared contracts/exceptions | PASS — ADR-0016 root envelope/OpenAPI and Film advice reused |
| 7 SOLID | PASS preparation — one scoped HTTP branch; no duplicated domain responsibility |
| 8 DRY/KISS/YAGNI | PASS — existing create/validation/defaults reused where feasible |
| 9 Patterns | PASS — no new framework/abstraction needed |
| 10 Domain correctness | PASS preparation — new non-NSFW identity, no existing-record mutation or relationships; DB proof deferred explicitly |
| 11 HTTP/OpenAPI | PASS preparation — five input/seven output properties, unchanged PRIVATE, overlap/complete-envelope tests specified |
| 12 Validation | PASS preparation — local strict parsing and existing constraints; concrete cases required in handoff |
| 13 Authorization/privacy | PASS preparation — persisted admission, exact route grant and field policy; runtime NOT_VERIFIED |
| 14 Secrets/cache/egress | PASS preparation — safe error/no-store/headers proof; no new secret or network capability |
| 15 Atomicity/concurrency | PASS preparation — commit/rollback and request-admission tests; D3 preserved |
| 16 Failure/operations | PASS preparation — bounded DB/auth failures and cleanup; new restore/deployment system N/A |
| 17 Database | PASS preparation — existing schema/API only; real PostgreSQL acceptance mandatory later |
| 18 Complexity/resources | PASS preparation — bounded single-record create/validation, no search fan-out or new performance claim |
| 19 Search/recommendation quality | N/A — no PUBLIC Search/ranking/recommendation implementation in W1 |
| 20 Document/OCR fidelity | N/A — no document or parser work |
| 21 Security tests | PASS plan — positive origins, strict-body negatives, deny sweep and deterministic admission controls |
| 22 Test quality | PASS plan — observable committed rows, zero-write failures, real HTTP/schema checks and qualified reuse |
| 23 Benchmarks | N/A — no new performance requirement or claim |
| 24 Evaluation science | N/A — no ML or quality evaluation scope |
| 25 Dependencies | PASS preparation — existing build stack, no upgrades/lockfile changes |
| 26 Build/tooling/diagnostics | PASS plan for focused runner/build; runtime build and IDE diagnostics NOT_VERIFIED and not claimed |
| 27 Logging | PASS preparation — safe exception translation, token/private payload negative checks; no new telemetry |
| 28 Docs/governance | PASS — ratification, workflow and future/source distinctions consistent; scoped Docs Sync below |
| 29 Hygiene/delivery | PASS preparation — only scoped docs changed, no publishing or ACTIVE edit; final integrity check recorded below |

No runtime command, PostgreSQL experiment, model invocation or IDE inspection was executed during this preparation review. Prior DP-A/B1 successes are predecessor evidence, not B2 test results. There is no IDE-clean or warning-free claim.

## Finding disposition, stop conditions and Docs Sync Check

**No blocking preparation findings.** All D12–D17 product choices are resolved at their stated preparation level. The precision above belongs in the later executable handoff and its original W1 acceptance, not another product vote or an extra pre-handoff review for each implementation detail.

- **UPDATED:** this new independent review and canonical B2 README readiness/next action only. The dated ratification, decision docket, source inventory and acceptance plan retain their original pre-review statements as the reviewed inputs.
- **ALREADY_CONSISTENT:** actual published baseline, B1 closure, `ACTIVE=NO_ACTIVE_HANDOFF`, five-field/seven-field contract, deferred W2/S/V and implementation prohibition. Current root/policy notices deny B2 implementation and therefore do not conflict with this preparation-only readiness.
- **DEFERRED_OUT_OF_SCOPE:** routine root/policy/roadmap/maintenance-index navigation after preparation publication belongs to ChatGPT/Owner. Existing old S19/P18 routing in docs/backend instructions, legacy Phase 5/13 gate prose, backend-implementer's outdated watchdog-pending bullet and illustrative package-tree drift are historical guidance cleanup; current policy and this exact scope control. No cleanup or new authority is inferred here. DPA-L01 and DPB1-L02 remain their separate LOW backlogs.
- **NOT_VERIFIED:** all W1 runtime/security/DB/OpenAPI cases, live data/deployment and future publication. These are explicitly future implementation/release gates, not fabricated PASS results or blockers to a documentation preparation review.
- **Final saved-file integrity:** all 21 local links across six preparation/review Markdown files resolve; whitespace and UTF-8 replacement-character checks are clean. The four original decision/inventory/acceptance inputs and ACTIVE retain their entry SHA-256 hashes; only README status/next action and the new review were edited by this reviewer. Tracked diff/index remain empty, HEAD is unchanged and no production/test/flag or historical evidence was edited.
- **STOP:** no implementation or handoff creation before publication and exact Owner source/API authorization; stop if a new dependency/schema/Auth policy/field/grant is required, PRIVATE compatibility cannot be preserved, or the atomic safe create path cannot be proved. Do not widen W1 or silently weaken the approved policy.

**One suggested preparation commit:** `docs(backend): prepare bounded public film creation`

**Next step:** Owner → Codex | Owner commit/push reviewed B2-W1 preparation, then give exact B2-W1 frozen-source/API implementation authorization; only then invoke `$codex-create-handoff` | BF26-DP-B2-W1 and this review | READY_FOR_HANDOFF at preparation level, unpublished; ACTIVE remains NO_ACTIVE_HANDOFF until the separately authorized executable handoff | B2 remediation 0 consumed, takeover NOT_AUTHORIZED; predecessor counters unchanged.
