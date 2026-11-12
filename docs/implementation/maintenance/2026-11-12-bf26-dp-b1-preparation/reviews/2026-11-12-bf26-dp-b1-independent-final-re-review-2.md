# Independent final re-review 2 — BF26-DP-B1

- **Date / reviewer / mode:** 2026-11-12 / independent Codex / implementation maintenance, Antigravity remediation round 2.
- **Authority / history:** [executable handoff](../executable-handoff-dp-b1-2026-11-12.md), [initial review](2026-11-12-bf26-dp-b1-independent-final-review.md) and [re-review 1](2026-11-12-bf26-dp-b1-independent-final-re-review-1.md). Entry ACTIVE was `IMPLEMENTED_AWAITING_CODEX_REVIEW`; preparation and DP-A acceptance remain retained.
- **Baseline / delta:** `main`, HEAD `066e361ac9938dfdc102168969fe8a498cf4ee2f`, empty index. Exactly **five test inputs** changed against the re-review-1 snapshot: both owner PublicRead tests, ProfileRouteAuthorizationIntegrationTest, SecurityFilterChainIntegrationTest and OpenApiRouteInventoryIntegrationTest. No further production, fixture, dependency, schema or module change.
- **Outcome: CHANGES_REQUESTED. FR-DPB1-02/03/04 CLOSED; FR-DPB1-01 MEDIUM / OPEN only for the remaining no-enrichment observer qualification below.** The physical JPA/transaction/context, lifecycle race, session, schema and traceability corrections are credited. No production privilege bypass or data leak is established.
- **Counters:** B1 remediation **2/3 consumed**; next permitted Antigravity correction is round **3**. Takeover NOT_AUTHORIZED. No fourth ordinary round or automatic takeover; historical DP-A/Frontend/RAG counters remain unchanged.

## Finding disposition

### FR-DPB1-01 — Context/race/failure corrections accepted; appended repository observers still lack a trustworthy negative observation

**Accepted and retained:** `findPublicDemonstratesRepeatableReadTransaction` in both owners now loads a genuinely managed Fiction/Film entity in an outer READ COMMITTED transaction. Advice at index 0 on the projection records the same inner PostgreSQL PID as the Vault read, distinct from outer PID, with REPEATABLE READ and `transaction_read_only=on`. The outer managed instance is absent from the inner context and remains managed with unchanged ID/title after the outer PID/isolation resumes. Both real delegates still run. Fresh DB execution passes those checks and the already accepted NSFW/soft-delete/atomic mixed-state cases.

The new no-enrichment cases also perform real success/hidden HTTP requests and injected data-failure HTTP 500/INTERNAL_ERROR controls, with People/Group `never()` verification and safe bodies. These improvements are credited; they are not requested again.

**Remaining evidence defect:** `PublicFictionReadIntegrationTest.java:615–617` and `PublicFilmReadIntegrationTest.java:616–619` install the repository counter with `advised*.addAdvice(forbiddenInterceptor)`, without a position. This differs from the projection instrumentation's working `addAdvice(0, repoAdvice)`. The installed Spring AOP **7.0.9** source (`AdvisedSupport.java:426–428`) appends the no-position advice at `advisors.size()`.

The installed Spring Data Commons **4.1.1** factory adds query and implementation execution interceptors before a subsequently appended advice. Its query interceptor directly returns the query result instead of proceeding to later advice when a repository query is recognized; the implementation interceptor similarly returns its composition invocation for base operations. This was checked in the source jars matching the captured test classpath, and corroborated with the primary [QueryExecutorMethodInterceptor source](https://raw.githubusercontent.com/spring-projects/spring-data-commons/4.1.1/src/main/java/org/springframework/data/repository/core/support/QueryExecutorMethodInterceptor.java) and [RepositoryFactorySupport source](https://raw.githubusercontent.com/spring-projects/spring-data-commons/4.1.1/src/main/java/org/springframework/data/repository/core/support/RepositoryFactorySupport.java).

**Calibration:** advice placement and terminal execution behavior are source facts. The resulting concern about a bypassed observer on the modified repository proxy is source-supported analysis, not a separately executed runtime counterexample. Neither test exercises a positive control proving this appended counter actually observes `findById` and the classification-query calls it claims to exclude. Therefore its zero values cannot independently qualify the required repository no-enrichment proof. Passing the current tests does not establish that their zero counter means no prohibited call occurred. The unchanged production query remains minimal; this finding is about B1-15's required regression observation, not a proven production enrichment call.

**Smallest remaining correction, same two allowed test files:** put the observer before the terminal repository executors (e.g. `addAdvice(0, forbiddenInterceptor)`), and make a controlled positive invocation demonstrate that each relevant observed repository family increments the guard. Reset the guard, then retain the existing zero observations for actual PUBLIC success/hidden/failure paths and remove advice in finally. Keep the already accepted context/race/failure/People controls unchanged. No new helper package/framework, production edit or architecture exception is requested.

The next correction need only rerun these two affected owner classes through the ordinary watchdog and refresh their source/case/XML mapping; qualify unchanged SEC/REF/CONTRACT/REG evidence by hashes rather than replaying every gate solely for a test-observer adjustment. Update only the already permitted handback/self-review/result fields to reflect the actual observer proof. This is the sole blocking residual of the existing FR-DPB1-01, not a new feature or a reset finding.

### FR-DPB1-02 — CLOSED

Live PUBLIC/PRIVATE contexts are now exercised without bearer headers on the selected Fiction/Film/Reference URIs; PUBLIC ASYNC/ERROR is 403 and anonymous dispatch 401. Path tests add suffix/trailing/traversal cases and signed expired/unknown-session plus malformed token denials, with existing firewall/conversion distinctions preserved. The populated real/absent credit controls and exact method/membership checks remain valid.

`sessionLifecycleAndMutationOnNewlyAccessibleDetailRoutes` now holds a real HTTP PUBLIC Fiction read at its Vault call using bounded latches, commits elevation on the competing actor, then releases the admitted read. The held read completes 200; fresh and propagated stale-context admission without bearer fail 401. This complements the retained actual asynchronous DP-A redispatch controls and selected-URI live ASYNC/ERROR checks. The closure credits that combined source-applicable evidence; it does not mislabel the final stale-context GET as an actual asynchronous redispatch. No additional cross-product of unchanged auth races is demanded.

### FR-DPB1-03 — CLOSED

The contract test now obtains actual PUBLIC/PRIVATE Fiction and Film HTTP envelopes, resolves their schema references and union branches, validates the PUBLIC and PRIVATE positive envelopes against their corresponding branches/unions, and rejects PRIVATE data/envelopes under the strict PUBLIC branch. Narrow recursive helpers check declared primitive types, required/property sets, nulls, enums, arrays and `anyOf`/`oneOf` as used by this generated graph. Wrong-type ID/title/boolean/nationality, invalid enum/NSFW and extra-field negative specimens demonstrate the previously absent checks. Existing actual null-key HTTP cases and the corrected six/seven required keys are retained.

This accepts the helper's exercised B1-specific graph/constraints; it does not claim a complete general-purpose JSON Schema validator or reopen legacy PRIVATE schema conventions. No validator dependency or global envelope change was introduced. Fresh CONTRACT execution passes on the final inputs.

### FR-DPB1-04 — CLOSED

All **52 method references** in the original B1-01–24 handback table resolve to actual current source methods; the previously missing fourteen names were reconciled without changing criterion meanings. Combined scenarios are mapped to their real containing methods. The corrected API Reference/error/schema/Auth text is retained. All **1,916** producer source hashes and **127** case/XML keys validate against the submitted final input; command/capture checks below reconcile. The remaining observer limitation is reported under FR-01 rather than reopening the fixed method/provenance finding.

### DPB1-L01 — LOW / OPEN / BACKLOG, nonblocking summary wording

The round-2 self-review's FR-02 paragraph says live/stale propagated PUBLIC contexts are denied with 403. The actual stale authority control correctly expects 401, while live PUBLIC dispatch is 403. The API contract and test behavior remain correct. Align this summary opportunistically during the already required handback update, or retain it for the next applicable maintenance/audit; do not create a correction round solely for this wording.

## Verification and provenance

Reviewer evidence **R2**: `logs/bf26-dp-b1/codex-rereview2-97a4c2c1fff94559b429b50b5a7f8314/`. Source/docs/raw-producer hashes and entry XML copies were captured before reviewer edits/reruns. Post-test comparison verifies **1,916/1,916** entry inputs unchanged. `framework-advice-audit.json` records the library versions, local source locations and source-analysis limit; no runtime positive observer probe is claimed. The reviewer changed no production/test source.

Both fresh commands used `python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R2/<receipt> -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<selector>' test` from repository root.

| Fresh receipt / selector | Actual result |
| --- | --- |
| `review-db.json` / `PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest` | PASS / exit 0; **26 tests**, zero failures/errors/skips, **53.093s**, capture/cleanup complete, cleanup not needed |
| `review-security-contract.json` / `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest,OpenApiRouteInventoryIntegrationTest,ApplicationArchitectureTests` | PASS / exit 0; **66 tests**, zero failures/errors/skips, **77.109s**, capture/cleanup complete, cleanup not needed |

Fresh XML copies confirm **six classes / 92 disjoint executions**, zero failures/errors/skips. These passing assertions are credited, with the remaining observer limitation distinguished from a failing application test. No repeated full suite, timeout extension or additional production diagnostic process was used.

Source-qualified producer final gates in `logs/bf26-dp-b1/remediation-round2/`: DB 26 / 50.078s; SEC 27 / 80.625s; REF 9 / 31.094s; CONTRACT 39 / 49.578s; REG 26 / 31.063s. All five report PASS/exit 0 with ordinary 300s bounds; argv hashes and capture byte lengths match. The 127-entry manifest matches all twelve preserved XML files with zero key differences/failures/errors/skips. SEC attempts 1/2 and REG attempt 1 remain COMMAND_FAILED/exit 1 and preserved. Producer 127 and reviewer 92 are not summed as unique acceptance cases.

Earlier static/architecture/source conclusions are reused after the five-file delta check. New/fresh build warnings remain distinct from defects; no complete IDE inspection was claimed after the earlier recorded timeout. The source-library inspection is not an IDE warning-free assertion.

## Scope, structure and complete coverage

Recursive relevant structure remains Fiction **86 Java files/23 packages**, Film **104/26**, Authentication **91/21**, Reference **41/12**, Vault **58/17**: zero missing `package-info.java` and zero `.gitkeep`. Five changed tests remain in the existing owning packages, with nested instrumentation/helpers. No new package, module edge, source/data abstraction, schema/migration, dependency, hook or agent change. Source admission/DTO/query/PRIVATE behavior and ADR-0016 root envelope remain unchanged from the accepted design; fresh architecture tests pass **38** checks.

Original B1-01–24 meanings remain controlling. Reuse previously credited cases; all original mandatory areas are independently qualified except the repository no-enrichment observation in **B1-15**, carried by FR-01. Accepted context/race evidence is not reopened merely because that parent finding remains partially open. DPB1-L01 is nonblocking wording only.

| Review dimension | Result / evidence |
| --- | --- |
| 1 Owner intent | PASS — unchanged exact eight-route scope |
| 2 Traceability | PASS — 52 real method references, original meanings and manifests |
| 3 Frozen architecture | PASS — production/schema/module/dependency unchanged |
| 4 Ownership/transactions | PASS — managed context separation, physical projection/Vault PID and outer resumption |
| 5 Recursive structure | PASS — complete five-subtree inventory above |
| 6 Shared transport/exceptions | PASS — ADR-0016 and existing owner errors retained |
| 7 SOLID | PASS — no added production abstraction/coupling |
| 8 DRY/KISS/YAGNI | PASS — scope-limited test corrections, existing mechanisms reused |
| 9 Patterns | PASS — no production pattern/framework expansion |
| 10 Domain correctness | PASS — accepted eligibility/lifecycle/PRIVATE cases retained |
| 11 HTTP/OpenAPI | PASS — actual envelope/branch/union and negative type proof; narrow validator limit stated |
| 12 Validation/path variants | PASS — added categories and infrastructure distinctions |
| 13 Authorization/privacy | PASS source/session/dispatch/wire controls; B1-15 observer residual FR-01 |
| 14 Secrets/cache/egress | PASS — no new credentials/egress, accepted safe headers/errors |
| 15 Transactions/concurrency | PASS — context/PID/races and held admission boundary |
| 16 Failure/recovery | PASS focused safe failure; new migration/backup machinery N/A |
| 17 PostgreSQL/JPA | PASS — real PG/managed entities; no H2/DDL weakening |
| 18 Complexity/resources | PASS — unchanged bounded production reads, bounded test actors |
| 19 Retrieval/recommendations | N/A — unchanged/denied |
| 20 Document/OCR fidelity | N/A — no parser scope |
| 21 Adversarial testing | PASS credited negatives; repository observer qualification NOT_VERIFIED, FR-01 |
| 22 Test adequacy | FINDING FR-01 — appended counter lacks effective positive observation control |
| 23 Performance benchmarks | N/A — none authorized or claimed |
| 24 Evaluation science | N/A — no model/evaluation scope |
| 25 Dependencies | PASS unchanged; unrelated vulnerability audit N/A |
| 26 Build/tooling/diagnostics | PASS focused runner/builds; complete IDE diagnostics NOT_VERIFIED |
| 27 Logging/observability | PASS unchanged production logging, safe injected HTTP failure |
| 28 Documentation/governance | PASS blocking traceability/provenance corrected; DPB1-L01 LOW wording backlog |
| 29 Hygiene/delivery | PASS exact map/empty index/whitespace checks; no publishing |

## Scope-bounded Docs Sync Check

- **UPDATED:** new re-review and current independent disposition/counters/next action in ACTIVE and executable. FR-02/03/04 CLOSED, FR-01 narrowed, LOW wording recorded. Original reviews, author handback/self-review and raw historical evidence remain preserved.
- **ALREADY_CONSISTENT:** exact B1 authority/route-field contract, 52-method map, source/case manifests, corrected API, accepted DP-A/Frontend/RAG/security defaults and separate counters.
- **IN-SCOPE REQUIRED:** only the two allowed owner-test observers and their positive control, plus truthful authorized handback/self-review/evidence/result update. Correct DPB1-L01 opportunistically; it is not a blocker.
- **DEFERRED_OUT_OF_SCOPE:** routine root/Owner policy/preparation README/roadmap/index and historical/package-tree cleanup stay with ChatGPT/Owner; no new source/frozen exception or feature follows.
- **NOT_VERIFIED:** the residual observer's effective runtime qualification, complete IDE diagnostics, live data/deployment/cutover and future publication. The remainder is not relabelled a production failure.

**Next step:** Antigravity → Codex | `/antigravity-implement-handoff`, mandatory `/antigravity-self-review`, then independent `$codex-final-review` | BF26-DP-B1, remaining FR-DPB1-01 observer proof in this re-review | CHANGES_REQUESTED; retain FR-02/03/04 closures and accepted proof; two owner tests plus affected evidence/docs only | remediation 2/3 consumed, next correction round 3; takeover NOT_AUTHORIZED; historical counters unchanged.
