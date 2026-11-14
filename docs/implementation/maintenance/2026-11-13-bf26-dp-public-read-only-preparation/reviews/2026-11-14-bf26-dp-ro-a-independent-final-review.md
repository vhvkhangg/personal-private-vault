# Independent final review — BF26-DP-RO-A

- **Date / reviewer / mode:** 2026-11-14 / independent Codex / initial implementation SLICE review.
- **Baseline:** branch `main`; HEAD, `origin/main` and live remote `main` all `7700222673712abc59eb6f1de621abc86c7172cc`. The published preparation and `3ce4c87d` have the same backend tree `f85539956e4672d09ffd267e427618060cc3854b`. Entry index empty; eight modified Java files, ACTIVE and three new implementation result/contract documents.
- **Authority:** [executable](../executable-handoff-dp-ro-a-2026-11-13.md), sections 1/3, records exact RO-A authorization and Owner-approved two-test amendment from six to eight files. ACTIVE was IMPLEMENTED_AWAITING_CODEX_REVIEW. Preparation readiness is not implementation acceptance; no RO-B or broader work is reviewed.
- **Outcome: CHANGES_REQUESTED. FR-ROA-01/02/03 MEDIUM / OPEN.** These are mandatory verification/traceability gaps. The inspected production delta implements the approved matcher removal and PRIVATE-only POST documentation; this review does **not** establish a production authorization bypass or a PRIVATE validation defect.
- **Counters:** initial review; RO-A remediation **0/3 consumed**, first correction round pending; takeover NOT_AUTHORIZED / NOT_USED. Independent review and producer self-review do not consume a correction round.

## User requirement and simplest mechanism

PUBLIC must have no Film creation permission; PRIVATE creation, eight selected B1 GETs, session authority and default denial remain compatible. Production changes stay inside the approved P1/P2/P3 boundary: one security matcher removal, Film POST OpenAPI annotation changes only, and root OpenAPI POST/bearer/schema corrections. Runtime FilmController body, request reader, domain transactions, DTOs and all other production sources remain unchanged. Existing PRIVATE fallback is sufficient; no new filter, serializer, abstraction, dependency, schema or module edge was introduced. RO-B full fields/names remain deferred.

All eight submitted Java SHA-256 values match the actual files. The two amended tests have only the authorized obsolete POST expectation changes. Root transport support remains consistent with ADR-0016; no new shared module or cross-owner repository access. These source conclusions and successful tests are credited independently of the findings below.

## Finding register

| ID | Severity / blocking | Evidence / consequence | Minimum correction | Status |
| --- | --- | --- | --- | --- |
| **FR-ROA-01** | **MEDIUM / YES** | Film-specific MVC observer qualification required by executable section 5.1 is not asserted; see detailed evidence below | Qualify the actual PRIVATE Film-create handler separately, then verify denied valid PUBLIC/anonymous POSTs cannot invoke it or mutate domain state | **OPEN / mandatory boundary evidence NOT_VERIFIED** |
| **FR-ROA-02** | **MEDIUM / YES** | ROA-06 is marked PASS, but the final selected HTTP tests have no PRIVATE Bean Validation or Film-domain rejection case | Add focused PRIVATE validation/domain negative controls in existing T2 with canonical errors and unchanged row-value snapshots, or supply exact applicable pre-existing evidence | **OPEN / mandatory validation acceptance NOT_VERIFIED** |
| **FR-ROA-03** | **MEDIUM / YES** | Handback/self-review cite nonexistent methods, including a fictional aggregate PRIVATE validation test; exact criterion→case→receipt/XML mapping required by ROA-12 is not reliable | Correct result artifacts using actual source/XML case identities, selected receipts and final hashes; distinguish covered, reused and missing requirements | **OPEN** |
| **DPROA-L01** | **LOW / NO** | Author summaries repeatedly say eight test classes; five selectors/XML contain **nine**. T1 assertion message still says nine-pair while actual set has eight; executable sections 7/8 retain six-file wording after the explicit eight-file amendment | Correct adjacent current summaries/messages when already touched; preserve original historical authority/evidence. No independent LOW-only loop | **OPEN / BACKLOG** |

### FR-ROA-01 — positive observer can be satisfied by earlier GETs

**Fact:** `T/authentication/ProfileRouteAuthorizationIntegrationTest.java:64–109` performs allowed GETs, then a successful PRIVATE Film POST, then verifies `handlerAdapter.handle(any,any,any)` with `atLeast(1)`. Invocation history is first cleared **after** that generic positive assertion. Earlier successful GET handlers can satisfy it; it does not specifically assert that the actual FilmController create HandlerMethod was observed. This is weaker than executable section 5.1: positively observe the successful PRIVATE Film POST and qualify that Film create was observed before clearing for denials.

The sweep's per-origin 403/no-adapter-call assertions and domain snapshot are real and passed freshly. However, the Film POST in the sweep uses `{}`; formerly valid PUBLIC body tests at `T/film/PublicFilmCreateIntegrationTest.java:72–147` observe only `VaultEntryOperations.create`, not the MVC adapter. A Vault spy proves a later business boundary, as the handoff explicitly notes. Their display names/author descriptions overstate zero-handler evidence. Anonymous formerly valid-body 401 similarly has no Film-qualified handler observation in T2.

**Impact / calibration:** no handler bypass was reproduced. The positive assertion would remain satisfied if Film-specific observation were absent, so it does not prove the specifically approved observation contract. A fresh green test cannot close an assertion that is missing.

**Minimum repair:** in existing T1, compute credentials/setup before resetting observation, clear prior calls, issue the successful PRIVATE Film POST and assert the observed handler is FilmController's create operation (or equivalently isolate and positively qualify that exact request/handler). Then clear immediately before formerly valid-body POSTs from both live PUBLIC origins and anonymous; assert exact canonical 403/401, no MVC handle invocation and complete domain row-value equality. Existing registered sweep and payload/media negatives remain reusable; no new test framework or production change is required.

**Closure:** fresh final T1 execution with exact source/receipt/XML mapping. Existing authorization, sensitive-route and actual async controls must remain green.

### FR-ROA-02 — PRIVATE validation is not established by Public denials or malformed-key rejection

**Fact:** the final T2 class has 19 methods. PRIVATE controls at lines 261–415 cover successful 18-key creation, vendor JSON, Latin-1, unknown key → `400 MALFORMED_REQUEST`, missing Content-Type → 415 and text/plain → 415. Its invalid title, overflow and invalid-country payloads at lines 154–196 use a **PUBLIC** token and expect authorization 403. No selected T2 or unchanged FilmWebIntegrationTest HTTP case asserts PRIVATE `400 VALIDATION_ERROR` or Film-domain `422 FILM_INVALID`. The handback's claimed `privateToken_mediaCharsetAndValidationFailures_retained` method does not exist.

Unchanged source does contain the expected mechanisms: CreateFilmRequest's Bean Validation constraints; `FilmCreateRequestReader.readPrivate:291–295` calls the validator; root ApiExceptionHandler maps ConstraintViolationException to `400 VALIDATION_ERROR`; FilmExceptionAdvice maps InvalidFilmException to safe `422 FILM_INVALID`. FilmValidationTest has domain-level blank-title coverage, but it is not in the five selected execution groups and no exact source-qualified HTTP evidence is supplied. A unit domain exception or PUBLIC 403 does not establish PRIVATE conversion/validation/error translation.

**Requirement / impact:** executable ROA-06 and sections 5.3/6 require preserving PRIVATE validation/error and no-write failures. Mark this acceptance portion NOT_VERIFIED, not a proven behavior regression.

**Minimum repair:** add representative PRIVATE HTTP cases in existing T2 for a structurally invalid request (e.g. blank title → canonical 400 VALIDATION_ERROR) and a structurally valid request failing existing Film/reference validation (e.g. nonexistent country → canonical safe 422 FILM_INVALID). Assert null error data, no rejected/private value disclosure, and seeded row-value snapshots unchanged. Preserve standard existing headers; do not invent a new exact cache string. Exact applicable pre-existing HTTP evidence with identified source/fixture/receipt/XML can instead close the same requirement if genuinely available. No validation redesign or extra format matrix is requested.

**Closure:** fresh affected T2 class execution, accurate mapping and final input hashes, with retained PRIVATE success/media/charset/commit/rollback cases.

### FR-ROA-03 — supplied trace is not a reproducible case map

**Fact:** `implementation-handback-dp-ro-a-2026-11-13.md:39–49` and self-review section 1 cite methods such as:

- `directPublicLogin_postFilm_returns403AccessDenied_noHandler_noDomainMutation`;
- `downgradedPublicToken_postFilm_returns403AccessDenied_noHandler_noDomainMutation`;
- `publicToken_broadFieldsMalformedOrMissingContentType_allReturn403AccessDenied`;
- `anonymousPostFilm_returns401AuthenticationRequired`;
- `privateToken_validPost_returns201With18Keys_noLocationHeader_andCommittedRows`;
- `privateToken_mediaCharsetAndValidationFailures_retained`;
- `privateToken_sqlInsertionFailure_rollsBackTransaction`;
- `privateToken_deferredCommitFailure_rollsBackTransaction`.

None is an actual T2 method or selected XML case. Real cases have different names, including `publicTokenFromProfilesLoginDeniedWith403AndNoMutation`, `privateProfileCreateRetainsFullEighteenKeyBehavior`, separate media cases, `sqlInsertionFailureRollsBackCompletely` and `deferredCommitFailureRollsBackCompletely`. This is systematic criterion-level drift, not just the nonblocking class-count typo. In particular the fictional aggregate test conceals FR-ROA-02's missing behavior evidence.

**Credited evidence:** all selected producer XML cases resolve to actual current source methods; eight handback file hashes match; all five raw receipts/captures are available. The reviewer qualified **115 distinct passing cases across nine classes**. No allegation that those executions were fabricated is made. `entry-audit.json` records actual cases by gate, source path and receipt/XML. Its regex reference list also includes a filename-suffix `java` match, which is not counted as a test citation finding; the eight named examples above were checked directly.

**Minimum repair:** replace inaccurate handback/self-review method references with actual class/method/case names and per-criterion receipt/XML pointers, including the actual separate payload/media cases and any new validation/observer controls. Mark partial/not-run/reused evidence honestly. Record final source hashes and selected execution identities; retain original failed attempts and prior accepted history. Reviewer corroborating reruns must not inflate the distinct union. Fix current ACTIVE/executable result summaries as necessary; no new manifest framework or general audit is required.

## Acceptance assessment

`T/` is `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

| Criterion | Result | Actual evidence / limit |
| --- | --- | --- |
| ROA-01 | **PARTIAL / NOT_VERIFIED** | Fresh T2 `publicTokenFromProfilesLoginDeniedWith403AndNoMutation`: valid PUBLIC body 403, safe envelope and domain equality; Film-specific MVC qualifier remains FR-ROA-01 |
| ROA-02 | **PARTIAL / NOT_VERIFIED** | Fresh T2 `publicTokenFromDowngradeDeniedWith403AndNoMutation`: live allowed GET plus valid-body 403/domain equality; same qualifier gap |
| ROA-03 | **PASS for denial/no-write** | Fresh T2 `publicPayloadVariantsDeniedWith403BeforeParsingOrValidation` and `publicMissingOrTextPlainContentTypeDeniedWith403`; source filter order supports authorization before parser; observer precision remains FR-ROA-01 |
| ROA-04 | **PARTIAL / NOT_VERIFIED** | Fresh `anonymousRequestToCreateFilmReturns401`, existing invalid/session/authority controls; full Film-qualified handler observation still pending |
| ROA-05 | **PASS** | Fresh `privateProfileCreateRetainsFullEighteenKeyBehavior`, `filmCreationProvesAtomicityAndDatabaseIntegrity`, `concurrencyObserverBarrierDuringUncommittedState`, FilmWebIntegrationTest: 18 keys, DB pair and independent transaction visibility, no Location |
| ROA-06 | **PARTIAL / NOT_VERIFIED** | Fresh vendor/Latin-1/unknown-key/415 controls PASS; missing PRIVATE validation/domain-error evidence is FR-ROA-02 |
| ROA-07 | **PASS route/snapshot; PARTIAL observer qualification** | Fresh T1 registered eight-pair sweep, both real Public origins, sensitive/unselected routes, adapter-never assertions and complete fixture-domain equality; FR-ROA-01 precision retained |
| ROA-08 | **PASS** | Fresh T1/SecurityFilterChainIntegrationTest controls, actual async redispatch and method/HEAD/OPTIONS/error rules; T2 Private in-flight revocation, Public before-authority invalidation and authority failure/recovery |
| ROA-09 | **PASS** | Fresh OpenApiRouteInventoryIntegrationTest: PRIVATE-only POST/CreateFilmRequest/ApiResponseFilmResponse, createFilm retained, no obsolete Public create components, valid generated references and unchanged GET specimen checks |
| ROA-10 | **PASS** | Source-verified producer READ 29 cases; fresh T1 allowed GETs, fresh T2 Private-create→Public seven-key GET, fresh API six/seven-key schemas/specimens; no RO-B expansion |
| ROA-11 | **PASS** | Fresh T2 SQL insertion/deferred-commit rollback and independent visibility controls; producer ARCH 38 cases qualified against unchanged architecture/schema/module sources |
| ROA-12 | **NOT_SATISFIED** | Raw outcomes/hashes credited; criterion-level method/receipt/XML assertions need FR-ROA-03 correction. Independent acceptance remains blocked |

## Verification, provenance and limitations

Evidence directory **R**: `logs/bf26-dp-ro-a/codex-final-20261114-ae91c5/`.

- `entry-audit.json`: **1,907** backend source/resource/test/POM/watchdog inputs hashed; reviewed docs hashed; **8/8** submitted Java hashes match. Complete producer gate XML/case inventory records **115 distinct cases / nine classes**, zero failures/errors/skips; every testcase method exists in current source. All receipt stdout/stderr byte lengths match and capture/cleanup are complete. `producer-snapshot/` preserves the five selected receipts and XML before fresh Maven execution; original producer stdout/stderr remain in place and are hash-qualified.
- Producer selected receipts: SECURITY 27 in **187.390s**, PRIVATE 20 in **43.094s**, API 1 in **32.063s**, READ 29 in **106.266s**, ARCH 38 in **32.062s**. All normal 300s, PASS/exit 0. Earlier failures in `logs/bf26-dp-ro-a/implementation-20261113-r0/` remain historical, not relabeled or overwritten.
- **Fresh reviewer SECURITY:** 27/27 PASS, exit 0, **186.937s**, zero failures/errors/skips; `fresh-security.json`, `fresh-security-xml/`, `fresh-security-qualified.json`.
- **Fresh reviewer PRIVATE/API:** 21/21 PASS, exit 0, **96.265s**, zero failures/errors/skips; `fresh-private-api.json`, `fresh-private-api-xml/`, `fresh-private-api-qualified.json`.
- Fresh total **48** is independent corroboration of a subset of producer **115**, **not** 48 additional distinct acceptance cases. READ/ARCH are explicitly reused source-applicable producer results; no fresh full suite was run or required.

Commands executed sequentially from repository root (R denotes the actual unique directory above):

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R/fresh-security.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest' test
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt R/fresh-private-api.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=PublicFilmCreateIntegrationTest,FilmWebIntegrationTest,OpenApiRouteInventoryIntegrationTest' test
```

Both used real PostgreSQL Testcontainers through existing fixtures. Maven reported two remote metadata HTTP-401 warnings (Flyway-community/Redgate repositories); builds/tests completed successfully. These are repository-access/environment warnings, not evidence of a source defect or permission to upgrade/change POM. Three targeted production IDE inspections returned empty problem arrays **with timedOut=true**; results are incomplete and **NOT_VERIFIED**, not IDE-clean. No warning suppression or configuration repair was performed. No deployment/cutover, provider activation or Owner-data suitability is established.

## Scope, package inspection and shared coverage

Reviewed actual complete production diff and all changed test files, including the 19 final T2 cases and retained failure/race fixtures; published architecture/instruction baselines from preparation remain applicable. Entry source traversal covers all backend inputs; changed security and Film-controller subtrees are explicitly inventoried in `entry-audit.json`. Test Authentication/Film/Web subtrees and root HTTP placement are recorded with final integrity. No new file/package, stale `.gitkeep`, duplicate production owner, dependency or migration was introduced. Existing shared ApiResponse/error placement and module ownership follow ADR-0016; no speculative SOLID/pattern/performance refactor is justified.

| Coverage dimension | Result / rationale |
| --- | --- |
| 1 Owner intent | PASS: approved RO-A-only revocation and eight-file amendment verified |
| 2 Traceability | **FINDING FR-ROA-03**; missing mandatory portions also FR-ROA-01/02 |
| 3 Architecture | PASS: minimal exact diff, unchanged backend baseline outside eight files; no frozen schema/edge redesign |
| 4 Module ownership | PASS: Authentication admission, Film-local annotation, root OpenAPI; no new cross-owner access |
| 5 Folder/package tree | PASS for changed subtrees/root placement and recursive input inventory; no new package |
| 6 Shared contracts/exceptions | PASS: existing root envelope and advice reused; no new shared layer |
| 7 SOLID | PASS: no new responsibility/coupling in production; existing filter fallback reused |
| 8 Principles | PASS: minimal removal, preserved Private/B1 paths, no speculative machinery |
| 9 Patterns | N/A for new patterns: no abstraction or hierarchy added |
| 10 Business correctness | PASS for observed revocation/Private success/rollback/read compatibility; validation acceptance limited by FR-ROA-02 |
| 11 Wire/API | PASS: fresh generated POST and B1 schemas/specimens; actual Private 18-key response |
| 12 Validation | **FINDING FR-ROA-02**: PRIVATE validation/domain negatives not qualified; Public denials and media controls credited |
| 13 Auth/privacy | Production diff/observed 403/401/default denial PASS; mandatory Film-specific observer precision **FR-ROA-01** |
| 14 Secrets/cache/egress | PASS in scoped diff/fresh denial envelopes/cache; no new logging/provider behavior; no universal leak audit claim |
| 15 Transactions/races | PASS: untouched owning transaction, fresh Private visibility/rollback/admission controls |
| 16 Failure/recovery | PASS for authority failure/recovery and SQL/deferred-commit rollback; PRIVATE validation gap remains dimension 12 |
| 17 Database | PASS: PostgreSQL fixtures, unchanged migrations/DBML/domain persistence, snapshot and commit proofs |
| 18 Complexity | PASS: no new lookup/loop/lock or runtime cost; denied POST stops earlier |
| 19 Retrieval/recommendation | N/A: no search/RAG/indexing change |
| 20 Document/OCR fidelity | N/A: no document processing |
| 21 Security tests | **FINDING FR-ROA-01** with fresh denial/route/race results credited |
| 22 Test quality | **FINDING FR-ROA-01/02**; PostgreSQL/HTTP/architecture green does not supply missing assertions |
| 23 Benchmarks | N/A: no performance target or plausible added hotspot |
| 24 Model evaluation | N/A: no model/quality feature |
| 25 Dependencies | PASS for no dependency/build changes; no new vulnerability/license audit claim |
| 26 Build/static/tooling | Fresh bounded builds/tests PASS; metadata 401 warnings triaged; IDE checks timed out, NOT_VERIFIED |
| 27 Logging/observability | PASS for unchanged production logging and safe canonical tested errors; no new telemetry |
| 28 Docs/governance | **FINDING FR-ROA-03**; DPROA-L01 nonblocking; scoped status/deferrals below |
| 29 Hygiene/delivery | PASS for exact eight-file code boundary, empty index and reviewer source preservation; CRLF normalization warning in T3 is not a behavioral defect |

## Docs Sync Check and bounded correction route

- **UPDATED:** this new independent review and current ACTIVE/executable review status/remediation instructions only. Producer handback/self-review remain unchanged submissions for Antigravity to correct in its bounded round. Published preparation/earlier independent reviews remain preserved.
- **ALREADY_CONSISTENT:** current Owner eight-file scope, exact production matcher/API delta, observed eight GETs, deferred RO-B, source fingerprints and separate predecessor history/counters. All raw pass/failure evidence retained.
- **FINDING / NOT_VERIFIED:** FR-ROA-01/02 mandatory verification gaps and FR-ROA-03 evidence mapping; no independent acceptance/publication claim may be made until resolved. IDE completeness, deployment and real Owner-data suitability remain unverified with the limits above.
- **DEFERRED_OUT_OF_SCOPE:** root/current policy/roadmap/maintenance-index/preparation notices, original W1 summary/citation LOW backlog, old scoped harness notices, unrelated Frontend/RAG docs. ChatGPT owns later separately authorized actual-SHA closeout/navigation sync; no repository-wide cleanup now.

**Bounded first correction:** Antigravity `/antigravity-implement-handoff` within the existing eight-file authority, addressing T1/T2 evidence and truthful handback/self-review/hash/XML mapping, followed by mandatory `/antigravity-self-review` and separate Codex `$codex-final-review`. No production change is requested by these findings. Freshly run affected changed test classes with normal 300s watchdog groups; reuse source-qualified unchanged READ/API/ARCH/security inputs where valid rather than reopening a full suite. Preserve all attempts and counters. Do not call test-only workflow unless expressly selected as such; do not create another handoff or automatic takeover.

RO-A remains **0/3 consumed** at this initial review; first Antigravity correction/re-review round is pending. W1 **2/3 STOPPED**, B1 **3/3 STOPPED**, DP-A **3/3 exhausted / exceptional takeover 1/1 accepted** remain unchanged. DPA-L01/DPB1-L02/DPB2W1-L01 LOW backlogs remain open. H1-F paused/FR-13 upstream-blocked; P17 frozen, P18 closed, S19-2 suspended; provider defaults OFF/unset and indexing inactive. No commit message is issued while blockers remain.

Final review integrity/source/evidence preservation and scoped link checks: `R/final-integrity.json`.

**Next step:** Antigravity → independent Codex | `/antigravity-implement-handoff` for FR-ROA-01–03, then `/antigravity-self-review` and `$codex-final-review` | BF26-DP-RO-A, ACTIVE and this review; existing eight-file authority | CHANGES_REQUESTED; no publication or RO-B work | remediation 0/3 consumed, first correction round pending; takeover NOT_AUTHORIZED / NOT_USED.
