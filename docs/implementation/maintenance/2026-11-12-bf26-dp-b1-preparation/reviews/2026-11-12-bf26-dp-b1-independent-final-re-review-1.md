# Independent final re-review 1 — BF26-DP-B1

- **Date / reviewer / mode:** 2026-11-12 / independent Codex / B1 implementation maintenance, Antigravity remediation round 1.
- **Authority and history:** [executable handoff](../executable-handoff-dp-b1-2026-11-12.md), [initial independent final review](2026-11-12-bf26-dp-b1-independent-final-review.md), and submitted round-1 handback/self-review. Entry ACTIVE was `IMPLEMENTED_AWAITING_CODEX_REVIEW`; prior accepted preparation and DP-A are not reopened.
- **Baseline:** `main`, HEAD `066e361ac9938dfdc102168969fe8a498cf4ee2f`, empty index. Against the initial review's 1,916-input snapshot, nine backend inputs changed: root OpenAPI configuration, two PUBLIC DTOs, five test classes and `ProfileAuthFixture`. All remain in the approved map. Business query/services/controllers, admission code, Vault/Reference production, dependencies and migrations are unchanged since that review.
- **Outcome: CHANGES_REQUESTED. FR-DPB1-01–04 remain MEDIUM / OPEN, narrowed to the residuals below.** Substantial corrections and passing tests are credited explicitly. The confirmed required-key schema defect is fixed; remaining blockers are the already required proof and traceability gaps. No authorization bypass or production transaction failure is established by this re-review.
- **Counters:** B1 remediation **1/3 consumed**, next Antigravity correction round **2**; takeover NOT_AUTHORIZED. Initial review and all prior receipts remain preserved. DP-A/Frontend/RAG counters and dispositions are unchanged.

## Finding dispositions — reuse the four original IDs

### FR-DPB1-01 — Partial correction; JPA context, projection/join identity and no-enrichment proof remain unverified

**Credited:** both owners now create a real outer READ COMMITTED transaction, observe a distinct inner PostgreSQL backend PID and REPEATABLE READ/read-only state at the Vault spy, then verify outer PID/isolation resumption. Both add the previously missing controlled soft-delete ordering and fresh-read denial. Existing NSFW and decisive atomic never-eligible transition cases continue to pass. Defensive wrong-type/missing-Vault injection is now described with the frozen trigger/FK limitation.

**Residual evidence:** `PublicFictionReadIntegrationTest.java:454–456` and `PublicFilmReadIntegrationTest.java:449–451` label a JDBC `SELECT count(*)` as warming the persistence context. That query does not load a managed JPA entity; neither test obtains/compares the outer and inner EntityManagers or proves retained managed state on resumption. The PID is sampled only before the Vault delegate, not at the owning projection and inside the real joined Vault read as specified. Therefore distinct outer/inner JDBC connections are demonstrated, but the complete persistence-context/projection-to-Vault boundary is not.

`PublicFictionReadIntegrationTest.java:522` and `PublicFilmReadIntegrationTest.java:517`, named `findPublicNeverInvokesFullFindOrEnrichment`, merely assert a present success and an exception from an injected Vault `QueryTimeoutException`. They contain no `verify`/`never`/interaction observation of full find or enrichment, no hidden-read assertion, and no HTTP failure/redaction assertion. A method name or minimal result does not prove that a forbidden collaborator was never called. This is the original B1-15/22 residual, not a new requirement for a subsystem.

**Minimum remaining correction:** in the two already allowed owner tests, genuinely load managed outer state, observe effective inner context and physical projection/join identities while delegating real reads, then assert the original outer context/state resumes unchanged. Add focused negative observations of the prohibited full-find/enrichment paths on success/hidden/failure and real HTTP safe failure. Keep the credited transaction/trash/race cases; no production transaction-manager/Vault/schema change or generic harness is requested.

### FR-DPB1-02 — Partial correction; sequential lifecycle is not the required concurrent admission/redispatch proof

**Credited:** the registered sweep now checks owning controller classes; exact eight-pair membership and remaining per-denial no-handler checks are retained. New cases exercise selected Fiction/Film/Reference URIs for ASYNC/ERROR with bearer, anonymous dispatch, non-GET methods and extra subpaths. A populated Film Credit has a successful PRIVATE control and PUBLIC real/absent denials. The real Fiction login→downgrade→elevation→refresh→revoke sequence proves several stale-token denials after completed transitions.

**Residual evidence:** `ProfileRouteAuthorizationIntegrationTest.java:555` always attaches the bearer when propagating a live context; its no-header cases are anonymous, without propagated authority. It never tests a live or stale **propagated context without bearer** at the newly selected URI. `sessionLifecycleAndMutationOnNewlyAccessibleDetailRoutes` (`:671`) issues each mutation synchronously and performs the detail GET only afterward. It has no barrier/held request or competing actor, so it does not establish B1-18's admitted/in-flight versus post-commit boundary. The old separate async probe remains useful DP-A evidence but is not newly selected-URI proof.

The new path test (`:614`) covers methods and `/extra` only, despite the self-review claiming trailing-slash coverage: no suffix/trailing/normalized/encoded case is present. The specified selected-route expired/unknown-session/epoch negatives also remain unobserved; new filter tests prove valid PUBLIC grants and bearer dispatch only. These are residuals of original FR-02 and executable sections 4/8, not a request to rerun or redesign all accepted DP-A races.

**Minimum remaining correction:** extend the allowed security/owner tests with a bounded deterministic newly accessible read/session transition ordering and same-selected-URI live/stale context propagation without a bearer; distinguish already admitted completion from new admission/redispatch after commit. Finish the named path/token categories using real persisted signed controls and existing helpers, preserve firewall rejection distinctions, and keep the newly credited populated-credit/method/dispatcher controls. Do not replace these scenarios with more sequential GET-after-mutation checks or relax assertions to `not 403` when an exact valid-session result is required.

### FR-DPB1-03 — Required-key/null correction accepted; complete actual envelope/union and type validation still missing

**Credited:** root schemas and DTO metadata now require all six/seven emitted keys; nullable values have explicit string/null types. Both owner HTTP key-set tests also exercise null values. Generated-schema assertions check the full required sets, nullable fields and false NSFW. NSFW/absent HTTP parity now checks the canonical body, content type/cache and absent ETag/Last-Modified/Location, and profile-order reads were added. The original four/five-key defect is no longer alleged.

**Residual evidence:** `OpenApiRouteInventoryIntegrationTest.java:878–980` creates handwritten **bare data objects**; it does not obtain actual B1 detail HTTP specimens or validate the complete `{data,error,meta}` envelopes against either envelope branch and their union. The purported valid PRIVATE examples are passed only to the PUBLIC schema expecting rejection; no positive validation against the PRIVATE schema or union follows. Union checks remain reference-string assertions.

The helper at `:989–1035` checks missing keys/additional keys/null/selected enum values but never validates primitive types, resolves `$ref`, or traverses the envelope/`anyOf`. By direct control-flow inspection, replacing a non-null `id` with a string and `title` with a number would not fail its type checks, because there are none for these fields. This is a source-derived counterexample, not a separately executed failing runtime test. Fresh CONTRACT tests pass the current assertions, which still do not establish executable section 7's mandatory generated graph/actual specimen/type/null/union proof.

**Minimum remaining correction:** use actual PUBLIC and PRIVATE HTTP response envelopes, resolve the generated operation/envelope/data schema references and the union, and assert correct primitive/enum/null handling and strict PUBLIC extras. Include wrong-type negative specimens to prove the checks reject them. Keep the accepted required-key/null fix and PRIVATE schemas unchanged; narrow assertions/helpers with existing dependencies are sufficient. No new validator framework/dependency or global envelope change is requested.

### FR-DPB1-04 — Provenance and API text corrected; acceptance-to-method traceability remains inaccurate

**Credited:** all **1,916/1,916** producer final-source hashes match the current inputs; the **127-entry** case manifest exactly reconciles with the twelve preserved XML files, zero failures/errors/skips. Five final command argv hashes and capture byte counts match. Failed intermediate SEC attempts 1–4 and CONTRACT attempt 1 remain failed and preserved separately. The original missing-manifest issue is resolved. API docs now use the correct Reference routes/decimalPlaces, fixed error messages, full-branch schema names and retained Auth rules; root bearer text no longer grants PUBLIC downgrade.

**Residual evidence:** the restored B1-01–24 table retains the right criterion meanings but still references **14 unique methods that do not exist** in the submitted source. Examples: `registeredRoutesAllowOnlySelectedEightToPublicTokenFromBothOrigins`, both `eligible*WithNullOptionalFieldsEmitsAll*Keys`, both `atomicNeverEligibleStateTransition`, both `mutationsDeniedToPublic`, and the old Reference method names. The null checks and atomic transition actually exist inside differently named methods, so their passing behavior is credited rather than inferred absent from a bad link. Nevertheless the table cannot perform its required traceability role.

The handback/self-review also states that JDBC count warmed the entity manager, that no-enrichment was asserted, that the specimen graph/PRIVATE branch was validated, and that no requirement is NOT_VERIFIED. Those claims exceed the delivered assertions described in FR-01–03. The raw case/source manifests do not repair an incorrect acceptance map automatically.

**Minimum remaining correction:** replace fabricated/old method references with actual source/XML names, indicate combined scenarios inside the real methods where appropriate, and align the handback/self-review to the independently remaining gaps until the corresponding assertions exist. Retain the corrected API text, original criterion meanings, validated manifests and all old receipts. This is a bounded in-scope implementation-document correction under executable section 10; no routine root/history cleanup is required.

## Acceptance and proportionality

Reuse the initial review's accepted source/structure/PRIVATE/default-deny conclusions; only nine inputs changed. B1-02/03's null-key and required-schema corrections are credited; B1-10's populated credit control is credited; B1-13/14 and unchanged domain regressions retain source-qualified producer evidence. The outstanding mandatory areas are **B1-15/16/17/22** for the remaining D10/context/negative observations, **B1-12/18/20** for selected admission/dispatch/path categories, **B1-21** for real generated envelope/type/union proof, and **B1-24** for truthful mapping. No additional feature, enterprise threat, numeric test quota, full suite, dependency or benchmark is introduced.

Original criterion meanings, named routes/fields, D1–D11 and frozen ownership remain unchanged. Passing test counts are credited as executions of the current assertions. They do not convert the named residuals to PASS. Finding severity remains MEDIUM because the gaps concern expressly mandatory accepted-scope evidence and its trustworthy handback, not cosmetic naming alone.

## Fresh verification and source-qualified reused evidence

Reviewer evidence **R1**: `logs/bf26-dp-b1/codex-rereview1-9bd86143aec540bb947774cefc399cbc/`. Entry source/docs/evidence snapshots and original target XML copies were saved before reviewer writes/reruns. All **1,916** entry backend/build/script hashes still match after tests. Initial review evidence remains at its original path and is not relabelled fresh.

Both fresh commands used the unchanged ordinary watchdog from repository root: `python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R1/<receipt> -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<selector>' test`.

| Fresh receipt / exact selector | Result and limits |
| --- | --- |
| `review-db.json` / `PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest` | PASS / exit 0, **26 tests**, zero failures/errors/skips, **94.204s**, complete capture/cleanup, cleanup not needed |
| `review-security-contract.json` / `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest,OpenApiRouteInventoryIntegrationTest,ApplicationArchitectureTests` | PASS / exit 0, **66 tests**, zero failures/errors/skips, **179.063s**, complete capture/cleanup, cleanup not needed |

Fresh total **92 disjoint test executions**; fresh XML copies are stored beside reviewer receipts. Neither command timed out. No further test run is needed to demonstrate that absent assertions remain absent.

Producer round-1 final receipts under `logs/bf26-dp-b1/remediation-round1/`: DB 26 / 47.109s; SEC 27 / 75.125s; REF 9 / 30.063s; CONTRACT 39 / 77.625s; REG 26 / 30.078s; all PASS/exit 0, 300s profile, complete capture/cleanup. Their argv hashes and capture byte counts validate. The manifest-to-XML class/test keys match exactly (**127**, zero mismatches); these are source-qualified producer results, not additional fresh reviewer executions. Five intermediate failed receipts remain COMMAND_FAILED/exit 1 and are not relabelled. Routine warnings remain distinct from findings; no fresh IDE inspection was attempted after the previously recorded timeout, so IDE completeness stays NOT_VERIFIED, not warning-free.

## Structure and shared review coverage

Recursive inventory is unchanged: Fiction **86 Java files/23 packages**, Film **104/26**, Authentication **91/21**, Reference **41/12**, Vault **58/17**; zero missing package descriptors or `.gitkeep`. Changed records/tests reuse existing packages; no new helper package or shared transport abstraction appears. ADR-0016 root envelopes and owner-local adapters remain intact. Fresh architecture tests pass all **38** checks. No change to Vault/Reference production, migration/resources, DBML, ADRs, dependencies, agents, skills or hooks was found. The original deferred package-tree/current-notice cleanup remains out of scope.

| Dimension | Status / round-1 evidence |
| --- | --- |
| 1 Owner intent | PASS — same bounded eight-route slice |
| 2 Requirement traceability | FINDING FR-04 — 14 nonexistent method references |
| 3 Architecture/frozen compatibility | PASS — unchanged approved ownership/schema edges |
| 4 Module and transaction boundaries | PASS source; remaining runtime boundary NOT_VERIFIED FR-01 |
| 5 Recursive tree | PASS — complete affected subtree inventory above |
| 6 Shared contracts/exceptions | PASS — accepted ApiResponse/owner errors, no new common module |
| 7 SOLID | PASS — no additional abstractions or coupling |
| 8 DRY/KISS/YAGNI | PASS — existing mechanisms retained |
| 9 Patterns | PASS — direct projections and capability calls; no framework expansion |
| 10 Domain correctness | PASS credited predicates/lifecycle cases; remaining proof FR-01 |
| 11 HTTP/OpenAPI | FINDING FR-03 — schema key defect fixed, envelope/type proof incomplete |
| 12 Input/path validation | NOT_VERIFIED specified remaining variants, FR-02 |
| 13 Authorization/privacy | PASS source/basic controls; admission/dispatch residual FR-02 |
| 14 Credentials/cache/egress | PASS unchanged secrets/egress, credited cache/error controls |
| 15 Isolation/concurrency | NOT_VERIFIED remaining context/projection identity and admission orderings, FR-01/02 |
| 16 Failure/recovery | Partial data-failure service assertion credited; HTTP/redaction/no-enrichment observations FR-01; restore/migration machinery N/A |
| 17 Database/JPA | PASS source, real PostgreSQL cases, no DDL/H2; context proof FR-01 |
| 18 Complexity/resources | PASS unchanged bounded ID reads; no performance claim |
| 19 Retrieval/recommendations | N/A — unchanged/denied |
| 20 Document/OCR fidelity | N/A — no parser work |
| 21 Adversarial security tests | NOT_VERIFIED named original residuals, FR-01/02/03 |
| 22 Test adequacy | FINDING — 92 fresh passing executions leave specified assertions unproved |
| 23 Performance benchmarks | N/A — none authorized/claimed |
| 24 Evaluation science | N/A — no model/evaluation work |
| 25 Dependencies | PASS unchanged; unrelated vulnerability audit N/A |
| 26 Build/tooling/diagnostics | PASS focused builds/runner; full IDE diagnostics NOT_VERIFIED |
| 27 Logging/observability | PASS unchanged production logging; safe HTTP failure observations FR-01 |
| 28 Docs/governance | FINDING FR-04; manifests/API corrections credited, statuses below |
| 29 Hygiene/delivery | PASS exact map/empty index/whitespace checks; no agent publication |

## Docs Sync Check and bounded next correction

- **UPDATED:** this new dated re-review and current ACTIVE/executable independent disposition, counter and next action. The initial independent review, supplied handback/self-review, raw producer attempts/manifests/XML and previous reviewer receipts are preserved.
- **ALREADY_CONSISTENT:** published preparation SHA, exact B1 authority/allowlist, original B1 criteria, corrected API text, validated source/case manifests and predecessor frozen/security/provider/Frontend/RAG boundaries.
- **IN-SCOPE REQUIRED:** only the residuals of FR-DPB1-01–04 in the already allowed tests and implementation-adjacent docs. The required-key production correction is accepted; source behavior need not change to close an evidence gap unless a new failing accepted requirement demonstrates a need within the map.
- **DEFERRED_OUT_OF_SCOPE:** routine root policy/AGENTS/preparation README/roadmap/index and older historical/package-tree notice cleanup remain ChatGPT/Owner work. No handoff scope or acceptance relaxation is inferred.
- **NOT_VERIFIED:** residual mandatory assertions described above, complete IDE diagnostics, deployment/cutover and future publication. No confirmed runtime privilege bypass, live provider/RAG activation, new production code, schema or dependency change is asserted or authorized by this review.

Keep the next correction finite: actual JPA/interaction observations, the missing selected admission/redispatch/path categories, real schema-graph envelope/type checks, and an accurate original-ID evidence map. Reuse credited cases/source-qualified evidence, retain fresh receipts for affected reruns, self-review, then submit independent re-review. No commit message while blockers remain.

**Next step:** Antigravity → Codex | `/antigravity-implement-handoff`, mandatory `/antigravity-self-review`, then independent `$codex-final-review` | BF26-DP-B1 executable and narrowed FR-DPB1-01–04 in this re-review | CHANGES_REQUESTED; finish existing mandatory proof/traceability within the exact map | remediation 1/3 consumed, next correction round 2; takeover NOT_AUTHORIZED; all historical counters unchanged.
