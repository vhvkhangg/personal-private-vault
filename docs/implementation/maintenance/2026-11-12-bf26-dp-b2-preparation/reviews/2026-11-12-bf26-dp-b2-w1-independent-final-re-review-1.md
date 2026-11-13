# Independent final re-review 1 — BF26-DP-B2-W1

- **Date / reviewer / mode:** 2026-11-12 / independent Codex / remediation round 1 review.
- **Authority:** unchanged [executable handoff](../executable-handoff-dp-b2-w1-2026-11-12.md), Owner's W1-only authorization and [initial independent final review](2026-11-12-bf26-dp-b2-w1-independent-final-review.md). Entry ACTIVE was IMPLEMENTED_AWAITING_CODEX_REVIEW.
- **Baseline:** `main`, HEAD `e8f4dde0d696d8dc8e9cd51d6658aeb9634ba6b0`, empty index. Against the initial review snapshot, only OpenAPI, Film controller/reader, three affected tests changed and the unlisted exception file was removed. No new source path was added; unchanged credited production behavior is retained.
- **Outcome: CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED. FR-W1-02 and FR-W1-04 CLOSED; FR-W1-01/03/05 remain MEDIUM / OPEN with narrowed residuals below.** No commit message while blockers remain.
- **Counters:** remediation **1/3 consumed**, next bounded correction round **2**, two remaining; takeover NOT_AUTHORIZED. Initial evidence, preparation reviews, predecessor and Frontend/RAG counters remain preserved.

## Independent verification and evidence qualification

Reviewer evidence **R1**: `logs/bf26-dp-b2-w1/codex-rereview1-fb0264f6414d449d9a2c6a0453360228/`. Entry snapshots cover **1,909 source/resource/script/POM inputs**, eleven documentation inputs and **1,041 round-1 producer files**, with target XML and submitted status artifacts preserved before review. No production/test source was edited by this reviewer.

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R1/review-create-api.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=PublicFilmCreateIntegrationTest,OpenApiRouteInventoryIntegrationTest' test
```

`R1` denotes the actual unique directory above. **PASS / exit 0, 28 tests (27 create + 1 API), zero failures/errors/skips, 117.844s**, complete capture/cleanup. Fresh XML copies are retained in R1. Passing these tests does not close assertions they do not make.

Round-1 producer evidence is `logs/bf26-dp-b2-w1/remediation-round1/`. The six selected receipts, stdout summaries and testcase manifest/XML reconcile to **196 distinct cases**, zero failures/errors/skips, with complete capture/cleanup and matching output byte lengths. All **11 final-source hashes** match current bytes. Actual watchdog wall durations are:

| Gate | Cases | Receipt / actual duration |
| --- | --- | --- |
| CREATE | 27 | `G-W1-CREATE-retry3.json` / **60.094s** |
| SEC | 27 | `G-W1-SEC.json` / **54.594s** |
| PRIVATE | 74 | `G-W1-PRIVATE.json` / **30.063s** |
| READ | 29 | `G-W1-READ.json` / **50.094s** |
| API | 1 | `G-W1-API.json` / **30.579s** |
| ARCH | 38 | `G-W1-ARCH-retry1.json` / **53.141s** |

These producer results are distinct from the reviewer rerun; do not add overlapping executions to the unique case count. Earlier failed attempts are retained. The initial delivery's **1,028 producer-evidence files** still match the initial reviewer snapshot. The eleven changed-file hashes are useful final-byte evidence, not a complete per-execution input history; preserve source/resource/fixture/POM/config applicability when refreshing affected groups.

## Finding disposition

Paths use **J** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/` and **T** = the corresponding test directory.

### FR-W1-01 — MEDIUM / OPEN, partial: original regressions fixed, media handling still diverges

**Credited correction:** `FilmController` no longer imposes `consumes=application/json`; `FilmCreateRequestReader.readPrivate` now delegates to configured MVC converters with servlet headers. Fresh tests and independent diagnostics confirm PRIVATE vendor JSON and correctly declared Latin-1 now succeed, with correct decoding. Keep these fixes and the matching unknown-key 400 control.

**Residual evidence:** `J/film/internal/web/controller/FilmCreateRequestReader.java:215–219` substitutes **application/json** when Content-Type is absent, whereas the original MVC body binding treats it as unsupported application/octet-stream for this DTO. The PUBLIC branch does not check a supported media type before strict JSON interpretation. Removing consumes therefore also permits valid JSON bytes advertised as text/plain to create a PUBLIC Film.

An isolated diagnostic uses a reference handler with the original `@Valid @RequestBody CreateFilmRequest` signature and the application's actual configured MVC converters, then compares real authenticated POST handling in the current Spring/Testcontainers context:

| Input | Reference binding | Current POST | Film rows added |
| --- | --- | --- | --- |
| PRIVATE application/json | 200 binding accepted | 201 | 1 |
| PRIVATE vendor JSON | 200 binding accepted | 201 | 1 |
| PRIVATE declared Latin-1 | 200 binding accepted | 201 | 1 |
| **PRIVATE missing Content-Type** | **415** | **201** | **1** |
| PRIVATE text/plain | 415 | 415 | 0 |
| **PUBLIC text/plain with valid JSON bytes** | **415** | **201** | **1** |

The reference's 200 means binding/validation accepted, not a legacy persistence execution. The two 415→201 results are actual unsupported-media mutations, contradicting section 4's preservation of canonical 415/media behavior and W1-07. This is a contract regression, not a claimed authentication bypass.

**Smallest remaining correction / proof:** preserve original media selection, including absent Content-Type, before PRIVATE conversion; apply the approved supported-media contract to PUBLIC without reintroducing the vendor-JSON restriction. Add real HTTP no-write 415 cases for absent/unsupported media, alongside retained vendor/charset/ordinary JSON controls and safe error headers as applicable. No global converter, filter, advice or new dependency is needed.

### FR-W1-02 — MEDIUM / CLOSED: PRIVATE request graph now resolves

`J/OpenApiConfiguration.java:208–220` registers the actual CreateFilmRequest schema and dependencies and retains the approved request/response anyOf compositions. The generated PRIVATE request has **17 properties**. Both producer and fresh `R1/review-openapi-spec.json` have **zero dangling schema references**.

`T/web/OpenApiRouteInventoryIntegrationTest.java:1133–1153` now validates PRIVATE request/union specimens, rejects Public-branch misuse, checks title length overflow and explicitly rejects a broken reference. The helper at lines 1273–1323 fails unresolved references and evaluates length bounds. Fresh API execution passes with retained B1 schema/specimen checks. The original missing-component/test false-positive defect is resolved; no optional schema framework or architecture change is imposed.

### FR-W1-03 — MEDIUM / OPEN, partial: admission and boundary assertions still incomplete

**Credited correction:** added admitted-revocation, before-authority invalidation and authority-exception/restoration tests; added missing/null required fields, 500/501 bounds, country normalization, optional-null and aggregate create-count checks. `ProfileRouteAuthorizationIntegrationTest.java:97–151` now independently qualifies both selected POST Film/Vault pairs and positively asserts the handler spy before the retained aggregate denial phase. These parts of the finding are satisfied and must be preserved.

**Remaining evidence gaps, not an inference of a new live bypass:**

- `T/film/PublicFilmCreateIntegrationTest.java:879–911` allows **401 or 500** for authority lookup failure, even though the handoff preserves **401 AUTHENTICATION_REQUIRED**. It checks only Film counts; it does not assert the error envelope/redaction, no handler invocation, or an unchanged Vault/domain snapshot. A partial Vault mutation or handler execution could evade this assertion. The restoration success is credited.
- The admitted-revocation negative at lines 820–829 checks only Film counts; the before-authority case at lines 867–872 checks only the absence of one title. Neither proves the required no-handler/no-mutation boundary over the relevant existing rows/Vault state. Keep their deterministic latches and positive reach controls; strengthen the observations rather than redesigning Auth.
- The enlarged boundary method at lines 916–1068 still does not supply all explicitly frozen cases from section 4: optional empty/blank values normalized to null, padded **raw originalTitle** overflow before trimming, and forbidden null/empty options. Its padded test is JSON syntax whitespace, not the raw value-length/normalization contract. Representative scalar/duplicate/type controls remain credited; no arbitrary case-count target is added.

**Smallest remaining correction / proof:** tighten the existing negative admission cases to exact canonical status/envelope plus a positively qualified no-handler observer and relevant persisted before/after state; retain successful restoration and already-admitted behavior. Complete the named finite boundary residuals with an aggregate negative snapshot where useful. Reuse unchanged accepted session/refresh proofs; no Auth production edits, new race framework or instant-cancellation requirement.

### FR-W1-04 — MEDIUM / CLOSED: exact source map restored

The standalone MalformedPayloadException source is absent; the exception is nested in the already-authorized reader. The final diff contains **six authorized production files and five affected test files**, with no added module/package/dependency or unlisted production source. Fresh compile/create/API tests pass; the producer's 38-case architecture gate also passes. This scope defect is closed.

### FR-W1-05 — MEDIUM / OPEN, partial: original IDs restored, residual traceability remains inaccurate

The author handback/self-review now restore the original W1-01–16 meanings and most real method references, and the 196-case manifest is genuine. Credit those corrections.

However each document still cites these **three nonexistent class-qualified methods**:

- `OpenApiRouteInventoryIntegrationTest.verifyCompleteOpenApiRouteInventoryMatchesAcceptedContract`
- `SecurityFilterChainIntegrationTest.unselectedMutationsReturn403Forbidden`
- `SecurityFilterChainIntegrationTest.asyncAndErrorDispatchersDenied`

Current real security methods include `permitsPublicBearerTokenOnExactB1EndpointsAndDeniesUnselected` and `enforcesAsyncAndErrorDispatcherPriorityOnB1Routes`; use the actual API method name from source. Handback section 2 also still names `filmOperations.createFilm` and a PRIVATE Location header, while actual code calls `create` and returns no Location. The self-review/handback and executable report stale gate durations: for example SEC **127.188s** versus receipt **54.594s**, API **109.843s** versus **30.579s**, and CREATE **35.69s** is the suite time rather than watchdog wall time **60.094s**.

**Smallest remaining correction / proof:** resolve every requirement's actual class/method/assertion/receipt, correct the header/capability descriptions, label suite versus command duration correctly and align all six rows with the retained receipts. Keep residuals NOT_VERIFIED until their exact proof is delivered; the current “all findings fully resolved/all original requirements verified” claim is not independently accepted. Refresh only affected evidence and qualify unchanged inputs. No raw history or earlier independent review may be rewritten.

## Cumulative acceptance and scope

Retain initial credit for false NSFW/server-controlled mapping, strict original-body parsing, seven-key safe receipts, actual SQL insertion/deferred-commit rollback and uncommitted observer, subsequent qualified B1 GET, exact nine-pair grants, aggregate denied-route snapshots, PRIVATE domain regressions and module boundaries. FR-02/04 now close. Original **W1-07 / section 4 media contract**, **W1-06/10/12/16** residual proof and **W1-16** traceability remain blocked by FR-01/03/05. No new feature, route, source exception or architecture is imposed.

The diagnostic `R1/review-media-probe.json` is **PASS/exit 0, 51.203s**, ordinary 300s, complete capture/cleanup. PASS denotes successful reproduction/controls, not product acceptance. Source, argument files, compilation receipt and fresh OpenAPI are preserved under R1 only; the reference handler exists only in this isolated fixture. No production/test source, POM or running user application was modified.

Recursively inspected Film **106 Java files/26 packages**, Authentication **91/21**, Vault **58/17**, Reference **41/12**. All Java-containing packages retain package-info.java; no .gitkeep. Root ApiResponse/OpenAPI ownership and legal Film/Vault named APIs remain consistent with ADR-0016. No resources/migrations V1–V29, POM, ADR/DBML, agents/hooks/config or unrelated modules changed. Existing skills/role/runner remain sufficient; no new tooling or agent is required.

## Shared coverage — unchanged credit retained, current limits explicit

| Dimension | Status / evidence and limit |
| --- | --- |
| 1 Owner intent | FINDING — unsupported-media writes/PRIVATE parity, FR-01 |
| 2 Traceability | FINDING — three unresolved method references and inaccurate evidence labels, FR-05 |
| 3 Frozen compatibility | PASS exact source map restored, FR-04 closed; remaining HTTP contract FR-01 |
| 4 Ownership/transactions | PASS retained canonical create/observer/insertion/deferred-commit proof |
| 5 Recursive structure | PASS inspected four relevant trees/descriptors, no new package |
| 6 Shared transport/exceptions | PASS existing root envelope and nested Film-local parse exception |
| 7 SOLID | PASS focused adapter/reader; no domain duplication or speculative hierarchy |
| 8 DRY/KISS/YAGNI | PASS configured converters now reused; media default residual FR-01 |
| 9 Patterns | PASS no new framework/manager/strategy |
| 10 Domain correctness | FINDING unsupported-media request commits, FR-01; other credited create invariants retained |
| 11 HTTP/OpenAPI | FINDING media contract FR-01; actual schema graph PASS, FR-02 closed |
| 12 Validation | NOT_VERIFIED named original boundary residuals, FR-03 |
| 13 Authorization/privacy | NOT_VERIFIED exact failure/no-handler/no-mutation observations, FR-03; no bypass inferred solely from absent assertions |
| 14 Secrets/cache/egress | PASS retained safe wire/cache/error checks; no new credential/provider capability |
| 15 Atomicity/concurrency | PASS credited DB and deterministic barriers; admission-negative observations remain FR-03 |
| 16 Failure/recovery | FINDING weakened 401-or-500 assertion/state proof, FR-03; restoration credited |
| 17 Database | PASS real PostgreSQL, unchanged mappings/migrations and owner APIs |
| 18 Complexity/resources | PASS bounded one-record domain operation, no new scans/manager |
| 19 Search/recommendation | N/A — no W1 search/ranking capability |
| 20 Document/OCR | N/A — no document/parser capability |
| 21 Security tests | FINDING original negative evidence not fully qualified, FR-03 |
| 22 Test adequacy | FINDING fresh 28 pass while media residuals remain, FR-01/03 |
| 23 Benchmarks | N/A — no benchmark requirement/claim |
| 24 Evaluation science | N/A — no ML/evaluation scope |
| 25 Dependencies | PASS unchanged POM/resources/stack |
| 26 Build/tooling/diagnostics | PASS focused build/watchdog; IDE diagnostics NOT_VERIFIED, no warning-free claim |
| 27 Logging | PASS tested safe errors/source boundaries; full log audit NOT_VERIFIED and not a new gate |
| 28 Docs/governance | FINDING remaining inaccurate acceptance/method/duration claims, FR-05 |
| 29 Hygiene/delivery | PASS source map/package hygiene, no staging/publishing, history retained |

No IDE inspection or native UI gate was executed/imposed. The fresh Maven command reports HTTP 401 metadata access warnings for existing Flyway/Redgate repositories; these are repository/configuration warnings, not source test failures or authority to repair credentials/dependencies here. No deployment/live provider or runtime release is certified.

## Docs Sync Check and bounded next action

- **UPDATED:** this new independent review and current ACTIVE/executable review, finding, counter and next-action fields only. Round-1 author handback/self-review remain retained submissions; corrections stay with Antigravity in the exact authorized map.
- **ALREADY_CONSISTENT:** W1-only product authority, restored six-file production map, no provider/Frontend/RAG activation, predecessor acceptance/counters and frozen architecture/schema.
- **DEFERRED_OUT_OF_SCOPE:** root/policy/README/roadmap/navigation synchronization and actual-SHA closeout remain ChatGPT/Owner work. Unrelated historical notices/LOW backlogs do not create remediation scope.
- **NOT_VERIFIED:** the named mandatory residuals in FR-01/03/05, IDE diagnostics and future publication/deployment. Closed FR-02/04 and credited unchanged tests must not be reopened without new evidence.
- **Final saved-file integrity:** all 78 local Markdown links across this review, ACTIVE and executable resolve; UTF-8 and tracked whitespace checks pass, with only the existing OpenAPI-test CRLF conversion notice. All 1,909 entry source inputs and 1,041 round-1 producer files retain their hashes, as do the 1,028 initial producer files checked above. ACTIVE's historical suffix, executable creation sections 1–10 and initial-review checkpoint are byte-preserved. Only the new review and current ACTIVE/executable fields were edited; other scoped documents are unchanged, the index is empty and HEAD remains `e8f4dde0d696d8dc8e9cd51d6658aeb9634ba6b0`.

Correct only the three residual findings, affected tests and authorized handback/self-review/result fields. Keep every failed/successful attempt and prior review. Refresh source-affected groups with fresh receipts; qualify unchanged reuse by actual relevant inputs. Then mandatory implementer self-review and separate independent review; no additional approval or new handoff is required for these already-authorized bounded corrections.

**Next step:** Antigravity → independent Codex | `/antigravity-implement-handoff` for FR-W1-01/03/05 residuals, `/antigravity-self-review`, then `$codex-final-review` | BF26-DP-B2-W1, executable and this re-review | CHANGES_REQUESTED, no Owner commit/push; FR-W1-02/04 CLOSED | remediation 1/3 consumed, next round 2; takeover NOT_AUTHORIZED; predecessor counters unchanged.
