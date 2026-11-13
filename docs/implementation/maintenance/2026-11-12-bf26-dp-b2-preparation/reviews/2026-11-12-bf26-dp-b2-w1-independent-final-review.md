# Independent final review — BF26-DP-B2-W1

- **Date / reviewer / mode:** 2026-11-12 / independent Codex / initial implementation review.
- **Authority:** current [ACTIVE](../../../handoffs/ACTIVE.md) and [executable handoff](../executable-handoff-dp-b2-w1-2026-11-12.md), Owner authorization **APPROVED — BF26-DP-B2-W1 ONLY**. The accepted preparation and D12–D17 are not reopened.
- **Baseline:** `main`, HEAD `e8f4dde0d696d8dc8e9cd51d6658aeb9634ba6b0`; empty index. Submitted implementation is uncommitted. Seven production files and five test files differ from the baseline, including new files; one production file is outside the exact map as detailed below.
- **Outcome: CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED. FR-W1-01–05 are MEDIUM / OPEN.** Supplied passing tests are real, but do not establish all mandatory original requirements. No commit message is issued while these blockers remain.
- **Counters:** initial implementation review, remediation **0/3 consumed**; next bounded Antigravity correction is round **1**. Review diagnostic attempts do not consume implementation remediation. Takeover NOT_AUTHORIZED. Predecessor, Frontend and RAG counters unchanged.

## Evidence and credited behavior

**Reviewer evidence R:** `logs/bf26-dp-b2-w1/codex-final-review-ce33b644c51046f2b127f50f8e32a0b8/`. Entry snapshots preserve **1,910 source/resource/script/POM inputs**, ten scoped documentation inputs, **1,028 producer-evidence files** and the pre-review Surefire XML. Reviewer probes live only under this ignored directory; no production/test source was edited.

Fresh independent execution:

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R/review-create-api.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=PublicFilmCreateIntegrationTest,OpenApiRouteInventoryIntegrationTest' test
```

`R` denotes the actual unique directory above. **PASS, exit 0, 20 tests (19 create + 1 API), zero failures/errors/skips, 114.297s**, complete capture/cleanup. These passing tests coexist with the defects and omitted mandatory cases below. Fresh XML was copied before any later work.

Producer evidence: `logs/bf26-dp-b2-w1/implementation-51e680cfd78440898371a936c43be8fa/`.

| Submitted gate | Distinct selected cases | Receipt / duration |
| --- | --- | --- |
| CREATE | 19 | `G-W1-CREATE-retry5.json`, PASS / 123.328s |
| SEC | 27 | `G-W1-SEC.json`, PASS / 128.312s |
| PRIVATE | 74 | `G-W1-PRIVATE.json`, PASS / 63.734s |
| READ | 29 | `G-W1-READ-retry1.json`, PASS / 156.453s |
| API | 1 | `G-W1-API-final.json`, PASS / 111.250s |
| ARCH | 38 | `G-W1-ARCH.json`, PASS / 54.969s |

The six receipts are exit 0 with complete capture/cleanup; recorded stdout/stderr lengths match. The selected XML and case manifest reconcile to **188 distinct class/method cases**, with no failures/errors/skips. Copied Surefire folders also contain unrelated older reports: only the explicitly selected manifest cases are credited. All **12 submitted final-source hashes** match current bytes. This is not a complete per-run input manifest; final source/resource/fixture/POM applicability must be maintained when refreshing affected gates. Do not add the fresh reviewer rerun as another 20 unique acceptance cases or relabel older evidence as fresh.

Credited source and tests: exact additional authenticated POST after dispatcher guards; five-key original-byte PUBLIC parsing, duplicate/escaped-key rejection; explicit false NSFW/null private inputs/empty relationship mapping; existing proxied Film/Vault create without a new transaction manager or precommit `findPublic`; seven-key no-store success and safe parse/validation/domain/SQL failure envelopes; real PostgreSQL uncommitted observer, insertion fault and deferred-commit rollback; subsequent B1 GET; retained aggregate denial snapshot and predecessor read/architecture regressions. These are preserved during remediation.

## Findings

Paths use **J** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/` and **T** = the corresponding test directory.

### FR-W1-01 — MEDIUM / OPEN: PRIVATE no longer uses its configured MVC conversion/media path

**Evidence:** `J/film/internal/web/controller/FilmController.java:60–124` adds `consumes=application/json`, reads raw bytes itself and sends PRIVATE through `readPrivate`. `FilmCreateRequestReader.java:66` creates a separate Jackson 2 `new ObjectMapper().findAndRegisterModules()`; its PRIVATE method at lines 180–213 uses byte-based `readValue`, rather than the configured MVC converter and charset behavior required by handoff sections 3–4 and W1-07.

Fresh isolated Spring/Testcontainers diagnostics compare the **actual configured MVC converter** (`org.springframework.http.converter.json.JacksonJsonHttpMessageConverter`) with actual PRIVATE POST handling:

| Synthetic PRIVATE request | Configured converter used by the original handler | Current HTTP |
| --- | --- | --- |
| Valid Film JSON, `application/vnd.ppv+json` | ACCEPT | **415**, no row |
| Valid Film JSON with title Café, `application/json;charset=ISO-8859-1`, correctly encoded bytes | ACCEPT with correct title | **400**, no row |
| Valid JSON plus unknown `legacyExtra` | REJECT 400 | 400, no row — **control, not a defect** |

**Consequence:** accepted PRIVATE request representations become unusable, contrary to the explicit no-new-consumes restriction and converter/charset parity contract. Merely naming the new mapper `mvcObjectMapper` does not preserve configured behavior.

**Smallest correction:** remove the new media restriction and delegate PRIVATE to the configured MVC conversion/validation path with the real request headers and charset. Keep strict parsing local to PUBLIC and preserve existing PRIVATE errors, full DTO, optional relations and headers. Do not change a global mapper or introduce a new framework.

**Required proof:** real PRIVATE HTTP regressions for vendor JSON, explicit non-UTF charset, ordinary JSON, malformed/unknown/duplicate/coercion behavior as applicable to the configured baseline, required/optional fields and unchanged headers. Assert accepted input stores the expected title and rejected input makes no changes. Keep the unknown-key control as a matching 400 rather than inventing a regression.

### FR-W1-02 — MEDIUM / OPEN: POST OpenAPI has a dangling PRIVATE request schema

**Evidence:** `J/OpenApiConfiguration.java:208–213` references `#/components/schemas/CreateFilmRequest` in `FilmCreateRequest.anyOf`, but the component is absent. The changed handler annotation exposes only PublicCreateFilmRequest, so the former PRIVATE request type is no longer discovered. Both the producer's `openapi-spec.json` and the fresh actual `/v3/api-docs` saved as `R/review-openapi-spec.json` contain this dangling reference.

`T/web/OpenApiRouteInventoryIntegrationTest.java:1080–1202` checks the reference string and PUBLIC request specimens, and sends a PRIVATE request, but validates only its **response**, not the PRIVATE request against its branch and union. `resolveSchema` at lines 1251–1266 returns an unresolved reference unchanged; `validateSpecimen` can then fall through as if it were unconstrained. The added request-length negative obligations are also not exercised by this helper.

**Consequence:** the advertised full PRIVATE POST contract is unresolved, and passing API tests do not prove the complete request graph required by W1-14/section 7.

**Smallest correction:** register the actual existing PRIVATE request schema and its referenced dependencies while preserving its accepted semantics. Keep the approved request `anyOf` and full response envelopes. Make unresolved references fail validation rather than act as an unconstrained success.

**Required proof:** assert complete reference resolution; validate real minimal/all-five/null PUBLIC and full PRIVATE request bodies against their profile branches and composition; preserve exact property/type/enum/null/length negatives and actual response/error specimen checks. Include a failing unresolved-reference control and length boundary+1 controls. Do not weaken B1's graph checks or add another schema framework merely for this fix.

### FR-W1-03 — MEDIUM / OPEN: mandatory POST admission and boundary evidence is incomplete

**Evidence:** `T/film/PublicFilmCreateIntegrationTest.java:664–715` is the sole POST admission race: an already-admitted direct-PUBLIC request completes after elevation, then a fresh old-token request gets 401. This is useful and retained. The class contains no before-authority-snapshot barrier, POST revocation race, POST authority-lookup-unavailable case, or corresponding no-handler/no-mutation proof required by handoff lines 156–158 and original W1-10/12. Accepted DP-A tests are not a substitute for these explicitly selected new POST controls.

The HTTP parsing matrix at lines 180–411 covers representative negatives, but omits required-field missing/null cases separately, exact 500 bounds, padded raw-length boundaries, normalization/optional blank cases, one-character country 422, forbidden null/empty options and several required type/duplicate variants from section 4. It generally asserts status/headers without a persisted before/after snapshot for rejected inputs.

`T/authentication/ProfileRouteAuthorizationIntegrationTest.java:95–135` executes selected POST positives before the aggregate baseline but does not capture their IDs and independently qualify each committed Film/Vault pair. Its handler spy is cleared before denials without a positive invocation assertion; the original explicit positive-control obligation at handoff line 152 remains unqualified. Keep the correctly retained aggregate row/column equality for the entire denial phase.

**Consequence:** original W1-06/10/12/16 have mandatory **NOT_VERIFIED** components despite the “all 16 verified” submission. No new security vulnerability is inferred solely from absent tests; acceptance is blocked by the explicit evidence contract.

**Smallest correction / proof:** in the already-allowlisted new Film and route tests, add finite deterministic before-admission and admitted switch/revoke cases, authority failure/restoration with POST no-handler/no-write controls, both real origins across positives, and the frozen request boundary matrix. Reuse a single aggregate persisted snapshot around negative loops where adequate; no artificial test-count target. Positively qualify the handler spy before reset and both selected POST committed pairs. Retain qualified unchanged refresh/session evidence; do not rewrite Auth production or impose instant cancellation.

### FR-W1-04 — MEDIUM / OPEN: an unlisted production exception file exceeds the exact change map

**Evidence:** new `J/film/internal/web/controller/MalformedPayloadException.java` is not one of section 3's selected production files. The handoff selects the reader as the sole new parsing helper and explicitly stops before another unlisted production file (section 9, line 240). No subsequent design addendum or exact Owner exception for this file was supplied.

**Consequence:** a narrow frozen-source allowlist was broadened without its required disposition. This is a scope finding, not a claim that the four-line exception itself creates a runtime vulnerability.

**Smallest correction:** keep this small implementation detail within an already authorized source file, such as a nested reader exception, and remove the unlisted production file while preserving public error behavior. If a separate file must remain, seek the exact missing exception through the existing workflow before editing beyond scope. No new package, hierarchy or global handler is justified.

**Required proof:** final production diff is within the approved map, strict parse errors still yield canonical safe 400/no-store, and affected architecture/compile checks pass. No broader source permission is granted by this review.

### FR-W1-05 — MEDIUM / OPEN: handback/self-review redefine requirement IDs and cite nonexistent methods

**Evidence:** [handback](../implementation-handback-dp-b2-w1-2026-11-12.md), section 2, and [self-review](../self-review-dp-b2-w1-2026-11-12.md), section 2, replace original W1 meanings with a sequential list of parser/test scenarios. For example original **W1-04 is atomic creation/rollback**, whereas the handback calls it escaped duplicate keys; original **W1-07 is PRIVATE compatibility**, whereas the handback calls it unknown-field rejection. This hides the actual coverage gaps.

Each document has **16 class-qualified method references, all 16 nonexistent in current source**. Examples include claimed `publicTokenFromLoginCreatesFilmWithExactSevenKeysAndNoStore` versus actual `publicTokenFromProfilesLoginCreatesFilmWithExactSevenKeysAndNoStore`, and claimed `databaseInsertionFailureRollsBackAtomically` versus actual `sqlInsertionFailureRollsBackCompletely`. The documents also claim a PRIVATE Location header although current code and the real PRIVATE test require no Location, and describe an observer PUBLIC GET/404 although the actual observer uses independent SQL counts.

**Consequence:** W1-16's requirement-to-code/test/receipt contract is not reviewable as submitted. A genuine 188-case manifest does not make an inaccurate requirement map true.

**Smallest correction / proof:** preserve the original W1-01–16 definitions verbatim in meaning; map each to existing methods, actual assertions and fresh/reused receipt/source evidence. Correct the observer/PRIVATE/header descriptions and keep missing cases NOT_VERIFIED until delivered. Refresh affected gates after fixes; qualify unchanged groups by complete relevant inputs (including fixtures/resources/POM/config) rather than copying a final changed-file hash list as if it recorded every execution. Retain all original failed attempts, raw XML/receipts, this review and predecessor history. Correct only authorized handback/self-review/current result fields; no root navigation cleanup.

## Original acceptance disposition

| Original criterion | Independent disposition |
| --- | --- |
| W1-01 | Credited real login/downgrade PUBLIC positives and anonymous denial; selected sweep pair proof residual in FR-03 |
| W1-02 | Five-key source mapping and representative rejection credited; complete negative matrix remains FR-03 |
| W1-03 | False NSFW/original-byte duplicate/type controls credited; remaining explicit variants FR-03 |
| W1-04 | Real observer, insertion failure and deferred-commit rollback credited; preserve them during correction |
| W1-05 | Seven-key 201 and nullable projection credited |
| W1-06 | Partial; mandatory boundary/no-write matrix NOT_VERIFIED, FR-03 |
| W1-07 | **FINDING**, reproduced PRIVATE media/charset regressions, FR-01 |
| W1-08 | Exact nine-pair deny membership/aggregate snapshot credited; spy qualification residual FR-03 |
| W1-09 | Follow-up B1 GET and retained read gates credited |
| W1-10 | DB insertion/commit faults credited; POST authority-unavailable proof NOT_VERIFIED, FR-03 |
| W1-11 | Exact POST/dispatcher priority and method variant tests credited; keep unchanged Bearer boundary |
| W1-12 | Partial admitted elevation case; before-admission/revoke/no-write obligations NOT_VERIFIED, FR-03 |
| W1-13 | Safe final success/parser/validation/domain/SQL error headers credited for tested cases; PRIVATE compatibility remains FR-01 |
| W1-14 | **FINDING**, dangling request schema/incomplete specimens, FR-02 |
| W1-15 | Existing 38-case architecture gate credited; exact production-map violation FR-04 |
| W1-16 | **FINDING**, incomplete proof and inaccurate traceability/source qualification, FR-03/05 |

## Diagnostic provenance and limits

`R/review-private-compatibility-probe2.json`: **PASS/exit 0, 52.672s**, ordinary 300s, complete capture/cleanup. This diagnostic reproduces the two actual HTTP compatibility regressions and fresh dangling OpenAPI reference; PASS means successful reproduction, not product acceptance. It uses the existing test base/disposable PostgreSQL, configured MVC converter and real PRIVATE session. Java probe source, argument files and class output are retained under R; `compile-probe2.json` records compilation. No backend test source or POM was changed.

The first diagnostic attempt (`review-private-compatibility-probe.json`) stopped when its assumption that PRIVATE ignored unknown keys was disproved by the configured Jackson 3 converter. Its original source/classes/logs/receipt and `compile-probe.json` remain intact. Probe 2 records unknown-key matching 400 as a control, not a finding. There is no retry/reset of an implementation remediation counter.

Fresh Maven warnings report HTTP 401 metadata access to existing Flyway/Redgate repositories; the scoped build/tests still passed. These are configuration/repository-access warnings, not a reason to repair credentials or change dependencies here. No IDE inspection was run, no IDE-clean/warning-free claim is made, and no native UI gate applies to this backend slice.

## Architecture, structure and 29-dimension coverage

Recursively inspected current Film **107 Java files/26 packages**, Authentication **91/21**, Vault **58/17**, Reference **41/12**; every Java-containing package has `package-info.java`, with no `.gitkeep`. Relevant Fiction/read tests and root web tests were inspected as affected. New types stay within existing Film DTO/controller packages; no module edge, common module, repository/entity leakage or dependency was introduced. ADR-0016 root transport placement is preserved. The unnecessary new file's exact permission is addressed in FR-04, without inventing an architectural redesign.

Existing Film/Vault application create/validation and B1 snapshots are unchanged. Diff checks confirm no edits to resources/migrations V1–V29, POM, ADR/DBML, agents/hooks/config or unrelated modules. Reviewed scoped instructions, current Owner policy/Docs Sync and the applicable Java, SOLID, reuse/pattern, Modulith, persistence/testing, Film/Auth, REST and bounded-command guidance. No new agent or tooling is needed.

| Dimension | Status / evidence and limit |
| --- | --- |
| 1 Owner intent | FINDING — PRIVATE preservation broken, FR-01; exact W1 scope otherwise retained |
| 2 Traceability | FINDING — original IDs/method references invalid, FR-05 |
| 3 Frozen compatibility | FINDING — unlisted production file, FR-04; schema/module baseline unchanged |
| 4 Ownership/transactions | PASS inspected canonical Film/Vault create and real rollback/observer tests |
| 5 Recursive tree | PASS package organization/descriptors; exact file permission tracked separately |
| 6 Shared transport/exceptions | PASS existing canonical wire envelopes/root placement; PRIVATE conversion exception FR-01 |
| 7 SOLID | PASS focused HTTP adaptation; no speculative interface/framework |
| 8 DRY/KISS/YAGNI | FINDING — PRIVATE duplicates configured conversion, FR-01; domain create reused |
| 9 Patterns | PASS no unnecessary pattern hierarchy or new transaction manager |
| 10 Domain correctness | PASS inspected new identity/false NSFW/no-reference mapping and tested failures; remaining evidence FR-03 |
| 11 HTTP/OpenAPI | FINDING — PRIVATE representation and schema graph, FR-01/02 |
| 12 Validation | FINDING mandatory boundary proof incomplete, FR-03; representative strict parser controls pass |
| 13 Authorization/privacy | NOT_VERIFIED for mandatory new POST failure/admission variants, FR-03; no bypass inferred solely from this gap |
| 14 Secrets/cache/egress | PASS tested safe PUBLIC wire/errors/headers; no new credential/provider/network capability |
| 15 Atomicity/concurrency | PASS DB observer/fault/commit proof; NOT_VERIFIED remaining admission races, FR-03 |
| 16 Failure/recovery | FINDING remaining authority failure proof, FR-03; DB cleanup/fault cases credited |
| 17 PostgreSQL/JPA | PASS real PostgreSQL and unchanged mappings/migrations; no H2 |
| 18 Complexity/resources | PASS bounded per-record domain operation; no new scans/fan-out/lock framework |
| 19 Search/recommendation quality | N/A — unchanged and outside W1 |
| 20 Document/OCR fidelity | N/A — no document/parser capability |
| 21 Security tests | FINDING mandatory cases/positive observers missing, FR-03 |
| 22 Test adequacy | FINDING — passing suite misses PRIVATE/schema defects, FR-01/02/03 |
| 23 Benchmarks | N/A — no performance obligation/claim |
| 24 Evaluation science | N/A — no model/evaluation work |
| 25 Dependencies | PASS unchanged POM/resources/stack; vulnerability audit N/A |
| 26 Build/tooling/diagnostics | PASS focused build/watchdog; metadata warnings classified; IDE diagnostics NOT_VERIFIED |
| 27 Logging | PASS no raw tokens/payload emitted by selected HTTP errors; complete runtime log audit NOT_VERIFIED |
| 28 Docs/governance | FINDING — author acceptance/traceability claims inaccurate, FR-05; current disposition synchronized below |
| 29 Hygiene/delivery | FINDING unlisted file, FR-04; no staging/publishing, preserved raw evidence |

## Docs Sync Check and remediation boundary

- **UPDATED:** this independent review, ACTIVE and executable current review/status/remediation/next-action fields only. The handback/self-review are retained as author submissions and superseded by this independent disposition; their correction remains Antigravity's explicitly authorized task.
- **ALREADY_CONSISTENT:** exact W1 product choice, predecessor acceptances/counters, no provider/Frontend/RAG activation, frozen DB/ADR/module contracts and Owner-only publication.
- **DEFERRED_OUT_OF_SCOPE:** routine root/current-policy/README/roadmap/navigation updates and future actual-SHA publication closeout remain ChatGPT/Owner work under section 9. Existing unrelated historical notice drift and separate LOW backlogs do not create W1 remediation scope.
- **NOT_VERIFIED:** mandatory residuals in FR-01/02/03/05, IDE diagnostics and future deployment/publication. Do not mark all W1 requirements VERIFIED merely because six submitted commands returned zero.
- **Final saved-file integrity:** all 79 local Markdown links across this review, ACTIVE and executable resolve; UTF-8 checks and `git diff --check` pass (only the existing OpenAPI-test CRLF conversion notice). All 1,910 entry source inputs and 1,028 original producer-evidence files retain their hashes. The prior ACTIVE historical suffix and executable sections 1–10 are byte-preserved. Only the new review and current ACTIVE/executable fields were edited; author handback/self-review remain unchanged, the index is empty and HEAD remains `e8f4dde0d696d8dc8e9cd51d6658aeb9634ba6b0`.

Remediate **only FR-W1-01–05**, affected tests and authorized handback/self-review/result fields. Preserve all credited parser/transaction/rollback/B1/denial proofs, original W1 IDs, raw attempts and creation contract. Refresh source-affected gates and truthfully qualify unchanged reuse; no new feature, Auth lifecycle change, schema, provider, framework, timeout increase or unrelated cleanup. Then mandatory self-review and separate independent `$codex-final-review`.

**Next step:** Antigravity → independent Codex | `/antigravity-implement-handoff` for bounded FR-W1-01–05 corrections, mandatory `/antigravity-self-review`, then `$codex-final-review` | BF26-DP-B2-W1, executable and this review | CHANGES_REQUESTED; mandatory evidence incomplete, no Owner commit/push | remediation 0/3 consumed, next round 1; takeover NOT_AUTHORIZED; all predecessor counters unchanged.
