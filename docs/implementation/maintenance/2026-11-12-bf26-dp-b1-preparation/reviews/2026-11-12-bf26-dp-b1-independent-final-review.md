# Independent final review — BF26-DP-B1

- **Date / reviewer / mode:** 2026-11-12 / Codex, independent of Antigravity implementation / implementation maintenance slice, initial independent review.
- **Authority:** [executable handoff](../executable-handoff-dp-b1-2026-11-12.md), sections 1–10; Owner-approved B1 ONLY; entry ACTIVE was `IMPLEMENTED_AWAITING_CODEX_REVIEW`. Reviewed preparation remains independently accepted; this review does not reopen it or DP-A.
- **Baseline / worktree:** `main` at `066e361ac9938dfdc102168969fe8a498cf4ee2f`; empty index. Received 17 tracked modifications and ten untracked files: four production records, two owner tests and four implementation documents. All are within the exact B1 map. No implementation code/test was edited by this reviewer.
- **Status: CHANGES_REQUESTED.** **FR-DPB1-01–04 MEDIUM / OPEN**. Passing submitted tests do not establish all original B1-01–24 obligations. The schema mismatch below is confirmed; the other proof gaps are not claims of a demonstrated authorization bypass or broken transaction implementation.
- **Counters:** B1 remediation **0/3 consumed**; this initial review does not consume a correction round. Next is Antigravity correction round 1, self-review, then independent Codex re-review. No takeover authorized. DP-A remediation 3/3 exhausted and takeover 1/1 accepted; DPA-L01 and all Frontend/RAG/historical dispositions remain unchanged.

## Owner requirement, implementation and scope assessment

The delivered design is proportionate: eight exact authenticated GET grants, owner-local scalar projections and minimal HTTP records, existing Vault named API, and unchanged PRIVATE operations. It introduces no dependency, schema/index, new module edge, generalized policy engine, provider activation or Frontend work. Security grants are correctly placed after the ASYNC/ERROR guard and before the PRIVATE fallback. Controllers use request authorities set by the accepted DP-A admission filter, not a client profile selector. Feature services use new read-only REQUIRES_NEW/REPEATABLE_READ methods and check Vault ID/type/deletion through the existing capability. Existing PRIVATE finds/full mappers remain unchanged.

The direct projections do not load full feature entities or related private classifications. Both actual minimal DTO key sets and the atomic never-eligible state transition have useful passing PostgreSQL/HTTP tests. These positive observations are retained; no architecture rewrite, new framework or full application suite is requested. Remaining work is the finite contract/proof correction below.

## Finding register — all four are blocking MEDIUM / OPEN

### FR-DPB1-01 — Required physical transaction/context and lifecycle evidence is incomplete

**Evidence:** `fiction/PublicFictionReadIntegrationTest.java:398` and `film/PublicFilmReadIntegrationTest.java:394` inspect Spring's read-only flag and `SHOW transaction_isolation` at the Vault spy, then assert a successful result. Neither creates the specified outer READ COMMITTED transaction, warms its persistence context, observes the owning projection's physical transaction/connection identity, compares it with the real joined Vault read, or verifies outer resumption. The two files contain no transaction/connection identity or EntityManager comparison. Their forced NSFW and atomic restore/reclassification cases are credited, but there is no forced eligible-reader/concurrent-trash case with the required before/after boundary. Hidden/error paths also lack the requested no-full-find/enrichment and data-failure assertions.

**Contract / consequence:** executable section 6 explicitly makes these proofs mandatory for both owners (B1-15/16/17/22). The current tests can pass without proving REQUIRES_NEW suspension/resumption or the complete freshness/context boundary. This is **NOT_VERIFIED mandatory acceptance**, not a demonstrated production race. Source annotation and the passing mixed-state case do not substitute for those named checks.

**Minimum correction / regression:** extend the two already allowed owner tests; observe actual physical transaction/connection and persistence-context boundaries at projection and real Vault read, suspend a deliberately weaker warmed outer context, and assert its unchanged resumption. Add the missing controlled trash/read orderings and safe data-failure/no-full-find/enrichment observations. Continue delegating real query/Vault calls for D10 proof; use committed fixtures and bounded actors, release barriers in finally and clean owned actors. Retain the existing passing atomic `(non-NSFW, trashed) → (NSFW, active)` case. No global transaction-manager, Vault, schema or dependency edit is required by this finding.

**Wrong-type limit:** the current wrong-type and missing-Vault cases are explicitly injected capability results. V14's real triggers forbid changing Vault type and reject mismatched feature identity on insertion; FK rules also prevent a committed orphan. Preserve those invariants and label these defensive branch tests honestly. Do not disable constraints/triggers to manufacture the handoff's real wrong-type state; reconcile the frozen-invariant limitation explicitly in the corrected evidence map rather than calling the injection a real persisted wrong-type row.

### FR-DPB1-02 — New business grants lack their required session, method/path and same-URI redispatch proof

**Evidence:** the changed `ProfileRouteAuthorizationIntegrationTest.java:38` sweep discovers the exact eight declared pairs and checks positive statuses and remaining per-denial no-handler assertions. However, selected positives do not assert the actual registered handler identity. Existing `errorAndAsyncDispatcherRequiresPrivate` exercises `/error` and `/api/v1/personal/profile`, and the retained actual async probe uses its separate test route. The new owner tests cover anonymous/malformed tokens and selected writes/nested GETs; they do not exercise same-selected-URI ASYNC/ERROR, propagated context without bearer, selected HEAD/OPTIONS/path variants, or controlled session mutation on newly accessible details. `SecurityFilterChainIntegrationTest` remains unchanged and targets the earlier protected probe, not the eight new grants. Direct credit coverage uses `/films/credits/1`, without a populated safe-parent credit positive control.

**Contract / consequence:** executable sections 4/8 and B1-10/12/18/20 explicitly require these cases. Old PRIVATE-only URI denial cannot prove dispatch precedence at a URI that now matches a PUBLIC GET grant. The matcher order is correct in source; this finding does **not** assert a proven bypass. Mandatory route/session acceptance is incomplete.

**Minimum correction / regression:** within the already allowed security/owner tests, assert exact owning handler identity for the eight discovered positives; exercise the new selected URIs with method/path variants and same-URI REQUEST/ASYNC/ERROR contexts, including no-bearer propagation, stale/revoked/generation/epoch/unknown-session controls and controlled downgrade/refresh/revoke orderings required by B1-18. Reuse accepted DP-A authority logic and genuine signed persisted tokens. Add a real and absent direct Film Credit denial with an eligible parent. Keep the complete remaining-route/provider-ON denial/no-handler/no-mutation checks; do not skip whole controllers or duplicate the auth algorithm.

### FR-DPB1-03 — Generated PUBLIC schemas weaken the approved wire contract and the test codifies that mismatch

**Evidence:** `OpenApiConfiguration.java:146` requires only `id,title,format,isNsfw`; line 174 requires those plus `productionStyle`. Both omit **required emitted** `originalTitle` and `nationalityCode`. `OpenApiRouteInventoryIntegrationTest.java:870` and `:893` expressly assert those four/five-key required lists. The owner DTO annotations likewise treat these two fields as nullable without declaring their required presence. Executable sections 5/7 require six/seven emitted keys, including allowed nullable keys; nullability does not mean omission is permitted.

The added OpenAPI test checks union reference strings and property-name sets but does not resolve/validate the complete PUBLIC envelope, PRIVATE branch and actual HTTP specimens for types/nulls/enums or show an extra PUBLIC property rejected by its strict branch. The two owner HTTP key-set cases seed non-null optional values only. Consequently the current generated-schema test passes while accepting a public-data object missing both required wire keys; no new test detects that weakening. Fresh reviewer CONTRACT execution reproduces this passing-but-inadequate check.

**Minimum correction / regression:** require all six/seven emitted PUBLIC data keys while keeping the two nullable values valid in the actual generated OpenAPI dialect. Align DTO-local metadata and narrow root customizer; keep PRIVATE schemas unchanged. In the existing allowed contract/owner tests, resolve the generated schema graph and check complete real PUBLIC/PRIVATE response envelopes, including null-valued PUBLIC specimens, allowed enum/false-only NSFW semantics and strict PUBLIC-extra-field rejection. Also finish the required whole hidden/absent error/header comparisons and conditional profile-order checks; comparing body plus one cache header alone does not establish the full specified HTTP contract. No new validator dependency/framework or global envelope/cache rewrite is requested. Distinguish valid PRIVATE union membership from rejection by the PUBLIC-specific branch.

Schema terminology was cross-checked against the [OpenAPI Schema Object specification](https://spec.openapis.org/oas/v3.1.0.html#schema-object); the binding six/seven-key requirement comes from the approved handoff, not a new reviewer preference.

### FR-DPB1-04 — Handback cannot trace the approved acceptance, and current API documentation contradicts source

**Evidence:** `implementation-handback-dp-b1-2026-11-12.md:77–94` changes the meanings of the stable B1 IDs: B1-07 becomes caching, B1-18 becomes no migrations rather than session races, B1-21 becomes no Reference edits rather than OpenAPI, and B1-22 claims constructor validation instead of the original ownership/transaction proof. Across its acceptance table, **16 unique referenced methods do not exist** in the delivered classes, e.g. both `findPublicSnapshotIsolation`, both `cacheControlNoStoreOnPublic200And404`, and `OpenApiRouteInventoryIntegrationTest.publicDetailSchemasAndUnionResponsesConform`. The actual views have no validating compact constructors; adding them is not requested merely to match that assertion.

The five PASS receipts and captured 117-test console/XML totals reconcile, but no producer final-input source-hash/case manifest is present under the supplied B1 evidence tree. The reviewer's new entry snapshot cannot retrospectively certify the producer's earlier inputs. Required B1-24 provenance is missing.

`api-contract-dp-b1-2026-11-12.md:25` incorrectly groups Auth lifecycle with all other denied routes and lists nonexistent timezones/refresh catalogs instead of the actual six unselected Reference routes. Line 70 omits Currency `decimalPlaces`. Its error section appends `id` to the message, whereas both controllers emit fixed `Fiction not found` / `Film not found`; its `anyOf` full-branch schema names do not match source. Root bearer text (`OpenApiConfiguration.java:98`) incorrectly says PROFILE_PUBLIC can downgrade; downgrade remains PRIVATE-only.

**Contract / consequence:** stable requirement traceability, truthful current API/docs and source-qualified receipts are mandatory in B1-24 and executable sections 7/9/10. These records cannot support the blanket `24/24 VERIFIED` assertion or reliable remediation/review. This is separate from the genuine passing test executions.

**Minimum correction / regression:** restore the original B1-01–24 meanings and map them to actual existing methods, gates, source hashes, receipts and preserved XML; label unproved cases NOT_VERIFIED until completed. Correct only the authorized API/handback/self-review documents and narrow allowed bearer description. Record actual fixed errors, schema refs, Reference fields/routes and retained Auth exceptions. Produce final-source/case evidence for affected reruns, preserving all prior PASS/failure/timeout logs and distinguishing fresh from reused proof. Do not invent past source hashes or silently rewrite failed evidence. No routine root/preparation/history cleanup is required.

## Original acceptance disposition

`PASS` credits inspected source and observed tests within their limits. `NOT_VERIFIED` identifies a mandatory residual, not a failing test or assumed defect. IDs below retain the executable handoff's meanings.

| Original criterion | Independent disposition |
| --- | --- |
| B1-01 | PASS basic real PUBLIC origins/200; required exact handler identity pending FR-02 |
| B1-02/03 | PASS non-null exact-key specimens; nullable-key/generated-branch proof NOT_VERIFIED, FR-03 |
| B1-04 | PASS NSFW-versus-absent body/cache controls; full observable header parity NOT_VERIFIED, FR-03 |
| B1-05 | PASS trash/restore and defensive injected branches; real invalid-type persistence is constrained as described in FR-01 |
| B1-06 | PASS inspected unchanged PRIVATE path and retained normal/NSFW/trash controls; paired schema specimens pending FR-03 |
| B1-07/08/09 | PASS basic write/nested and registered-denial sweep; additional path/real-ID controls pending FR-02 |
| B1-10 | NOT_VERIFIED populated direct-credit plus absent control, FR-02 |
| B1-11 | PASS source deny fallback and submitted complete sweep/provider-ON evidence; earlier accepted authority code retained |
| B1-12 | PASS ANON/malformed controls; full specified new-grant persisted-token matrix NOT_VERIFIED, FR-02 |
| B1-13/14 | PASS selected Reference status/order/field checks for both origins/PRIVATE, unselected denials; production unchanged |
| B1-15 | PASS minimal query/source mapping; explicit no-full-find/enrichment/data-failure proof NOT_VERIFIED, FR-01 |
| B1-16/17 | PASS existing NSFW and decisive atomic mixed-state cases; remaining physical/context/trash proof NOT_VERIFIED, FR-01 |
| B1-18 | NOT_VERIFIED newly accessible detail/session ordering, FR-02; old DP-A proof is not relabelled new B1 proof |
| B1-19 | PASS basic no-store/conditional controls; full hidden header/profile-order proof NOT_VERIFIED, FR-03 |
| B1-20 | PASS inspected exact matcher order; selected-URI dispatch/method/path proof NOT_VERIFIED, FR-02 |
| B1-21 | FINDING — weaker required keys plus incomplete generated-specimen validation, FR-03 |
| B1-22 | PASS legal ownership/fresh architecture checks; physical/context proof NOT_VERIFIED, FR-01 |
| B1-23 | Submitted focused regression totals reconcile; no fresh SEC/REF/REG run claimed by reviewer |
| B1-24 | FINDING — traceability/source-evidence/current-doc gaps, FR-04 |

## Verification and evidence provenance

Reviewer evidence **R**: `logs/bf26-dp-b1/codex-review-fe2cab453a1d4df19c762b22991ed35e/`. `entry-source-sha256.json` covers **1,916** backend source/config/test/build and script inputs; post-test comparison found **zero changes**. Existing target XML was copied to `R/implementer-reports-at-entry/` before any reviewer Maven command; four freshly executed class XML files were then copied separately beside reviewer receipts. No old report is represented as fresh.

Both reviewer commands used `python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R/<name>.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<selector>' test` from repository root:

| Fresh reviewer command / receipt | Actual result |
| --- | --- |
| `review-db.json`, selector `PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest` | PASS / exit 0; **22 tests**, zero failures/errors/skips; **91.375s**, complete capture/cleanup, cleanup not needed |
| `review-contract.json`, selector `OpenApiRouteInventoryIntegrationTest,ApplicationArchitectureTests` | PASS / exit 0; **39 tests**, zero failures/errors/skips; **110.844s**, complete capture/cleanup, cleanup not needed |

Fresh total: **61 disjoint test executions**. These are executions of existing submitted assertions, not proof that the missing assertions ran. An initial shell invocation lacked PowerShell quoting around the comma-separated selector and failed parsing before Maven/watchdog execution; the corrected invocation above ran once. It is not an application-test failure or a correction round.

Supplied producer receipts under `logs/bf26-dp-b1/final/` were inspected: DB 22 / 129.938s; SEC 21 / 162.079s; REF 9 / 59.703s; CONTRACT 39 / 108.282s; REG 26 / 63.172s. All report PASS/exit 0, ordinary 300s; all five argv hashes match their declared explicit commands, captured byte lengths match, and preserved selected XML totals **117** with zero failures/errors/skips. Their missing final-input source/case manifest is FR-04; the reviewer does not combine 117 producer plus 61 reviewer executions into a unique-case coverage total. Earlier producer attempts/logs remain untouched.

Maven private metadata HTTP 401 warnings and dynamic-agent warnings occurred while the builds succeeded; these are environment/tooling warnings, not proof of a B1 defect. IDE batch inspection was attempted on seven changed files with 15s budget but returned only a timed-out FictionController entry and `more=true`; `R/ide-lint.json` preserves it. IDE diagnostics are **NOT_VERIFIED**, not warning-free. No safety hook/startup/configuration failure occurred during these runs, and none was bypassed.

## Structure, architecture and 29-dimension coverage

Recursive production inventory: Fiction **86 Java files/23 packages**, Film **104/26**, Authentication **91/21**, Reference **41/12**, Vault **58/17**; all Java-containing packages have `package-info.java`, with no `.gitkeep`. Four added records use existing owner view/DTO packages, two added tests use existing test packages. No new package/module/exception/envelope abstraction appears. ADR-0016 root transport and module-local adapters remain intact; fresh 38 architecture checks pass. The older illustrative package-tree sketch remains the already deferred documentation item, not a new blocker.

| Dimension | Result / evidence and limit |
| --- | --- |
| 1 Owner intent | PASS — exact B1 maintenance grants; no B2/Frontend/RAG expansion |
| 2 Traceability | FINDING FR-04 — altered IDs/nonexistent methods |
| 3 Architecture/frozen baseline | PASS — allowed source map, unchanged resources/ADRs/DBML/module edges |
| 4 Ownership/boundaries | PASS source/architecture; transaction proof NOT_VERIFIED FR-01 |
| 5 Recursive structure | PASS — actual five-subtree inventory above |
| 6 Shared contracts/exceptions | PASS — canonical root ApiResponse/ApiResponses; owner errors, no common module |
| 7 SOLID | PASS — narrow added capability/query/map; no speculative abstraction |
| 8 DRY/KISS | PASS — accepted auth/Vault/Reference reused; no parallel authorization engine |
| 9 Patterns | PASS — simple projections/capabilities suffice; no pattern-driven machinery |
| 10 Domain correctness | PASS observed basic predicates/PRIVATE path; full lifecycle proof FR-01 |
| 11 HTTP/OpenAPI compatibility | FINDING FR-03/04 |
| 12 Validation/path semantics | NOT_VERIFIED required selected method/path variants, FR-02 |
| 13 Authorization/privacy | PASS source boundaries/basic tests; required new-route session/dispatch evidence FR-02 |
| 14 Secrets/cache/egress | PASS no new secrets/network/URLs; complete cache/header assertions FR-03 |
| 15 Atomicity/isolation/concurrency | NOT_VERIFIED required remaining physical/context/lifecycle proof, FR-01 |
| 16 Failure/recovery | Source fail-closed path retained; required data-failure observations FR-01; new migration/restore machinery N/A |
| 17 PostgreSQL/JPA | PASS scalar owner queries/no DDL/FK-trigger weakening; no H2 |
| 18 Complexity/resources | PASS bounded ID lookups/two reads, no query loop/enrichment; no measured performance claim |
| 19 Retrieval/recommendation quality | N/A — capabilities unchanged/denied |
| 20 Parsing/OCR/multilingual fidelity | N/A — no parser/document work |
| 21 Adversarial security proof | NOT_VERIFIED specific original acceptance residuals, FR-01/02/03 |
| 22 Test adequacy | FINDING — 61 fresh passing assertions leave named gaps; no quota demanded |
| 23 Performance benchmarks | N/A — no benchmark obligation or optimization claim |
| 24 Evaluation science | N/A — no model/evaluation work |
| 25 Dependencies | PASS unchanged build/dependencies; new vulnerability audit N/A |
| 26 Build/tooling/diagnostics | PASS focused builds/watchdog; IDE NOT_VERIFIED due timeout; no warning-free claim |
| 27 Logging | PASS no new production logs/content serialization; required safe failure observation FR-01 |
| 28 Documentation/governance | FINDING FR-04; independent statuses synchronized below |
| 29 Hygiene/delivery | PASS allowed file map/empty index; tracked whitespace check passes with CRLF conversion notice; no publishing |

## Scope-bounded Docs Sync Check and remediation boundary

- **UPDATED:** this independent review and current review/status/next-action fields in ACTIVE and executable handoff. The original implementation handback/self-review/PASS claims are preserved as submitted evidence and superseded by this independent disposition, not rewritten to look green.
- **ALREADY_CONSISTENT:** B1 exact Owner implementation authority and published preparation; DP-A accepted/closed; frozen migrations/source boundaries, PRIVATE design, provider defaults, Frontend/H1-F/FR-13 and RAG suspension; separate historical counters preserved.
- **IN-SCOPE CORRECTION REQUIRED:** FR-04's named implementation-adjacent API/handback/self-review and source/case provenance; Antigravity already has explicit edit authority for these files under executable section 10. This is implementation mode, so correction is routed through its active handoff, not a new docs-only maintenance gate.
- **DEFERRED_OUT_OF_SCOPE:** routine root AGENTS/Owner policy/preparation README/roadmap/index synchronization and prior historical notice/package-tree cleanup remain ChatGPT/Owner work. The current ACTIVE/executable authority explicitly supersedes old no-B1 checkpoints; do not repair unrelated history in this correction.
- **NOT_VERIFIED:** the mandatory residuals above, complete IDE diagnostics, deployment/cutover, live Owner data and future publication. Runtime pass counts are not slice acceptance. No browser/provider activation, RAG process, full suite, new dependency/schema or additional source exception was used or authorized.

Correction is bounded to FR-DPB1-01–04 within the existing exact production/test/doc map. Prefer tests and truthful evidence fixes; change allowed production code only where the confirmed schema/documentation mismatch or a demonstrated failing requirement needs it. Reuse unchanged qualifying results, run focused changed-source checks through ordinary 300s receipts, self-review, and return for independent re-review. No commit message while blockers remain.

**Next step:** Antigravity → Codex | `/antigravity-implement-handoff`, mandatory `/antigravity-self-review`, then independent `$codex-final-review` | BF26-DP-B1 executable and FR-DPB1-01–04 in this review | CHANGES_REQUESTED; close original mandatory evidence/schema/docs gaps within the existing map | initial review complete; B1 remediation 0/3 consumed, next correction round 1; takeover NOT_AUTHORIZED; historical counters unchanged.
