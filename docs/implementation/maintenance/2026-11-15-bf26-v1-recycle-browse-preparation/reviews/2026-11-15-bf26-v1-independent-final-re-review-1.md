# Independent final re-review 1 — BF26-V1-RECYCLE-BROWSE

- **Date / reviewer / mode:** 2026-11-15 / independent Codex / `SLICE`, review of Antigravity correction round 1.
- **Disposition: `CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED`.** **FR-V1-04 CLOSED. FR-V1-01/02/03/05 remain MEDIUM / OPEN**, narrowed to the residual obligations below. No new finding ID, new feature, CRITICAL/HIGH defect or privilege bypass is asserted.
- **Scope/authority:** unchanged [Owner authorization](../owner-authorization-bf26-v1-2026-11-15.md) and [executable](../executable-handoff-bf26-v1-2026-11-15.md), exact **nine production/four existing test** paths. Reuse the [initial review](2026-11-15-bf26-v1-independent-final-review.md)'s unaffected conclusions; its history is preserved. [Round-1 handback](../implementation-handback-bf26-v1-round-1-2026-11-15.md) and [self-review](../self-review-bf26-v1-round-1-2026-11-15.md) are author evidence, not independent closure.
- **Verified checkout:** `main`; HEAD, `origin/main` and live remote `main` all **`58cbd58d1f3fe6be002ba97eb6599292a758f8f5`**. Entry ACTIVE was IMPLEMENTED_AWAITING_CODEX_REVIEW, **1/3 remediation consumed**. No staged delta or publication occurred.
- **Counters:** **1/3 consumed; round 2 next**. Test retries and this re-review do not create/reset rounds. Takeover **NOT_AUTHORIZED / NOT_USED**.
- **Evidence root:** `logs/bf26-v1/reviewer-r1-20261115-378da9d02a/`; producer round-1 evidence remains under `logs/bf26-v1/impl-20261115-r1/`. Reviewer did not edit production/tests or author reports.

## Reviewed delta and retained mechanism

Ten of the thirteen allowed source/test files changed since initial review: P01/P02/P04/P06/P07/P08 and T01–T04. P03 repository, P05 page view and P09 mapper match the initial review hashes. All **13/13** round-1 handback hashes match current files; no source/test drift occurred during this re-review. Existing mutation methods, guards, security/session filters, root HTTP/OpenAPI/advice, entity/old DTO, schema/migrations, build/dependencies, module descriptors and harness remain unchanged.

The nested `VaultEntryOperations.InvalidTrashInputException` is a proportionate distinction within the allowed files. Service/codec use it for known input rejection; controller catches it for 400 and lets ordinary operational exceptions reach the safe 500 branch. Existing owner-local query, copied views and three-field mapping remain suitable. No alternative architecture or extra module/framework is needed.

## Finding adjudication — accepted subcases stay accepted

| ID | Current disposition | Accepted round-1 improvement | Residual correction required |
| --- | --- | --- | --- |
| **FR-V1-01** | **MEDIUM / OPEN / blocking** | Broad IllegalArgumentException-to-400 defect is corrected; ordinary operational IllegalArgumentException now returns 500. | Supply a real service transaction **completion-phase** witness after a successful bounded read; throwing TransactionSystemException in a Mockito method answer still does not establish that phase. |
| **FR-V1-02** | **MEDIUM / OPEN / blocking** | Actual repository initial/continuation calls each return three rows; active gaps are now distributed; two handwritten-query plans are genuinely retained in stdout. | Capture actual service/repository generated SQL and binds/fetch bound, and correlate the retained EXPLAIN to that captured statement. |
| **FR-V1-03** | **MEDIUM / OPEN / blocking** | All 17 types, populated default/minimum, exact-limit terminal/lookahead, malformed UTF-8, long overflow/MAX boundary, ordinary negative query checks and 401/403 headers are improved/proven. | Complete maximum-page boundary, unpadded ambiguous encoding, positively controlled repository observer, meaningful no-write proof and otherwise-valid persisted expired-token control, detailed below. |
| **FR-V1-04** | **CLOSED** | Actual generated page/item required lists, nullable required nextCursor, missing-field negative validation and populated/empty HTTP specimens all pass fresh T04. | None. Preserve this closure unless later source drift/new evidence affects it. |
| **FR-V1-05** | **MEDIUM / OPEN / blocking** | Canonical V1 IDs and five-old/eight-new T01 accounting corrected; **16/16** explicit class-qualified method references resolve; hashes/receipt ledger and raw failed attempts qualify. | Replace unqualified/contradictory “captured” wire/schema specimens with traceable actual captures in a fresh report; retain honest limits for the still-unverified cases. |

### FR-V1-01: classification fixed; completion witness still absent

**Fact:** current controller catches only the input-specific subtype at `J/vault/internal/web/controller/VaultController.java:114`; ordinary IllegalArgumentException reaches INTERNAL_ERROR/500. A read-only diagnostic against the compiled controller (`controller-probe.json`, child exit 0) records:

```text
operationalIllegalArgument.status=500
invalidLimit.status=400
invalidLimit.message=Invalid argument provided
```

This closes the observed classification subcase; it does not prove physical transaction completion.

**Fact:** `T/vault/VaultWebIntegrationTest.java:403–407` calls `invocation.callRealMethod()` and then throws `new TransactionSystemException(...)` **inside the Mockito answer for listTrash**. No transaction-manager/synchronization completion hook is instrumented. A real method followed by an exception with a transaction-related name is not a completion-phase witness. Depending on spy/proxy placement it can execute inside the invocation or after another boundary; neither the test nor a timing/order observation establishes the required phase.

**Minimal remaining proof:** use the actual proxied service read, positively observe successful bounded repository acquisition/materialization, then inject a labelled fault through a transaction completion mechanism (for example a scoped `TransactionSynchronization.beforeCommit` or controlled transaction-manager completion interception). Assert that the fault occurs in that phase before proxy return and that HTTP is safe 500/data:null/no-store. Ordinary invalid input remains 400. Keep this in T02/allowed files; no transaction-policy/global-manager rewrite or new test file is authorized. The existing mocked operational controls remain useful, but cannot be relabelled completion proof.

### FR-V1-02: measured plan is still for a separate statement

**Fact:** `T/vault/VaultEntryIntegrationTest.java:394–406` now checks actual `findTrashPage` results for initial and continuation reads. Credit those result-size/order witnesses and the 40-active/six-deleted distributed fixture.

**Fact:** lines 409–432 still build `String seekSql = "SELECT ... LIMIT 3"` manually and pass that string to JdbcTemplate EXPLAIN. The retained `ACTUAL_SPARSE_TRASH_PLAN_*` output is real evidence of **that handwritten statement**, not captured Hibernate SQL/parameters. No generated SQL/bind observation was added. Thus the missing portion of V1-12 remains NOT_VERIFIED despite the now-useful repository result bound.

**Minimal remaining proof:** capture actual first/continuation SQL, bound cursor/fetch parameters and the service's limit+1 acquisition, then EXPLAIN a parameter-corresponding captured statement. State exactly what a JDBC/JPA result limit proves. Retain current successful fixture/plan observations; do not repeat unrelated regression or grow the fixture into a benchmark project. No conclusion that production SQL is slow, offset-based or needs an index is made; no index/migration/order change is authorized.

### FR-V1-03: exact remaining finite gaps

1. **Maximum page/fetch boundary:** T01 `limitBoundsValidation` and T02 `limitParsingAndRangeValidationHttp` have 30 deleted items and request 100, returning 30 with no continuation. This proves acceptance of 100, but not truncation/lookahead at the maximum. Add a finite maximum-boundary witness with enough eligible entries (e.g. 101) or equivalent direct positive fetch observation; preserve the now-passing default/minimum/exactly-limit tests.
2. **Unpadded ambiguous Base64url:** tests added malformed UTF-8 and overflow/MAX cases, which are credited. `djF8MTQy==` is a padding rejection already covered before correction; it does not exercise alternate unused-bit encodings of a valid **unpadded** payload and the canonical roundtrip check. Add a valid cursor control and a different unpadded token decoding to the same bytes (e.g. canonical `djF8MQ` versus `djF8MR` for `v1|1`) and prove the latter is rejected before the business read. The codec source retains the correct roundtrip check; no accepted hostile cursor is alleged.
3. **No-write/observer fidelity:** repository `never()` assertions now exist, but the positive controls at `T02:208–235` verify the operation spy, not the same repository observer used for the negative claim. Positively qualify that observer once with a real valid query, then preserve its negative checks. `T02:281–312,335–357` and `T01:499–508` compare only `count(*)`. Equal row counts cannot detect UPDATE or delete/reinsert writes; in T02 the final count snapshot also starts **after** the valid paging calls and fixture changes. Use a scoped DML observation or meaningful before/after domain-state comparison around valid and invalid reads, excluding legitimate auth/session persistence. The initial `countBefore` local does not become evidence merely by being assigned.
4. **Otherwise-valid expired persisted session:** `T03/ProfileRouteAuthorizationIntegrationTest.java:1114–1122` adds profile=PRIVATE and scope=PRIVATE but **no sid or generation**. Unchanged SecurityConfiguration requires both (`J/authentication/internal/infrastructure/security/SecurityConfiguration.java:87–102`). That token would be rejected even without expiry. Preserve a real admitted session/token's required claims, expire it, and positively control the otherwise-equivalent unexpired request. Keep new-route 401, no-handler/new-read and no-store assertions. No auth source/configuration change is needed.

All-type traversal, exact-limit/no-lookahead, committed restore, ordinary security headers and newly added strict UTF-8/long-ID cases are credited; they are not reopened or ordered to be redesigned. No new finding ID or unlimited fuzz/performance obligation is introduced.

### FR-V1-04: CLOSED on fresh generated-contract evidence

P07 now marks id/entryType/deletedAt REQUIRED; P08 marks nextCursor **REQUIRED and nullable**. Fresh `OpenApiRouteInventoryIntegrationTest#openApiContractContainsAllRoutesAndUniqueOperationIds` requests actual generated `/v3/api-docs`, asserts the exact required lists, rejects each missing-key specimen, and validates populated and empty actual HTTP responses. The whole changed T04 method passes. Root OpenApiConfiguration is unchanged. This establishes the requested schema fix without relying on the handback's prose schema excerpt.

### FR-V1-05: method mapping fixed; “captured” specimens remain inaccurate

Credit canonical V1-01–19 IDs, corrected actual method identities, **13/13 hashes**, **16/16 explicit method references**, case totals and failed/passing receipt disclosure. The corresponding machine checks are retained in the evidence root.

Two concrete contradictions remain in [round-1 handback](../implementation-handback-bf26-v1-round-1-2026-11-15.md):

- Section 8A (lines 191–211) says “Captured from trashHttpSuccessAndEnvelopeWire” but shows **FICTION**. That case seeds a **FILM** (`T02:132`) in its isolated cleaned fixture; mapper/repository preserve the enum. The unchanged prior id/timestamp in prose has no raw response capture pointer.
- Section 8D (lines 249–264) claims the observed INVALID_ARGUMENT message is **“Limit must be between 1 and 100”**. The controlled HTTP handler emits **“Invalid argument provided”** (`P06:105/117`); the compiled-controller diagnostic above corroborates it. The service exception's text is not serialized as that message.

The artifact listing contains receipts/logs/XML, but no supplied raw successful wire capture corresponding to those paragraphs. Tests retain response strings in memory and assert them; that does not establish the displayed invented field values as captured output. The schema excerpt also uses a schematic nullable representation, whereas the actual OAS 3.1 graph should be reported in its generated form if claimed as a capture.

**Minimal repair:** a fresh round-labelled handback/self-review must point to actual synthetic body/header/schema artifacts with run/case/source applicability and state remaining NOT_VERIFIED cases honestly. Clearly distinguish illustrative examples; replacing “captured” with “illustrative” alone does not supply the executable's required actual specimens. Preserve both initial and round-1 reports and raw evidence rather than rewriting history. This is an outstanding V1-19 evidence obligation, not a request for real Owner data or extra product behavior.

## Acceptance disposition after round 1

Unchanged PASS conclusions from the initial review remain accepted where current source/evidence still applies. No earlier execution count is added to the current union.

| V1 requirement(s) | Current status | Evidence / limit |
| --- | --- | --- |
| 01,03,04 | PASS | Existing single-route/three-field/empty behavior retained; changed T02/T03/T04 pass fresh |
| 02 | NOT_VERIFIED | Both PUBLIC origins, PRIVATE, anonymous/invalid, no-handler and headers pass; expiry fixture is not otherwise valid, FR-V1-03 |
| 05 | NOT_VERIFIED | Default/minimum populated witnesses now pass; maximum/fetch proof incomplete, FR-V1-02/03 |
| 06 | NOT_VERIFIED | Canonical negative responses and `never()` controls pass; qualify the same business-query observer positively, FR-V1-03 |
| 07 | NOT_VERIFIED | New UTF-8/MAX/overflow cases pass; unpadded ambiguity witness missing, FR-V1-03 |
| 08,09,10 | PASS | Fresh all-17-type traversal, exact terminal/lookahead/exhaustion and committed restore |
| 11 | NOT_VERIFIED | Committed re-trash/retained identity passes; no-write witness is count-only, FR-V1-03 |
| 12 | NOT_VERIFIED | Real repository batches improved; generated SQL/binds/correlated plan missing, FR-V1-02 |
| 13 | NOT_VERIFIED | Operational classification fixed; transaction completion/no-write proof outstanding, FR-V1-01/03 |
| 14,15 | PASS | Old lifecycle/guard behavior retained; current applicable metadata/concurrency/Search regression |
| 16 | PASS | FR-V1-04 CLOSED by fresh generated schema/HTTP positive and negative checks |
| 17 | NOT_VERIFIED | Ordinary success/input/operational/security/conditional headers pass; genuine completion witness outstanding |
| 18 | PASS | Exact 9+4 boundary, unchanged frozen inputs and fresh architecture 38/38 |
| 19 | FAIL | Case/hash/receipt corrections credited; displayed captured specimens remain contradictory, FR-V1-05 |

## Verification and applicability

All Maven groups ran sequentially with existing watchdog **normal 300s**, exact existing `mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<selector>' test` command shape. Fresh per-group XML was saved before the next Maven run; stale target reports were excluded by run start time.

| Fresh selector | Receipt in this review root | Result / invocations / seconds |
| --- | --- | --- |
| `VaultEntryIntegrationTest` | `V1-DB.json` | PASS / 13 / 38.094 |
| `VaultWebIntegrationTest` | `V1-HTTP.json` | PASS / 7 / 36.594 |
| `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | `V1-SEC.json` | PASS / 28 / 63.109 |
| `OpenApiRouteInventoryIntegrationTest` | `V1-API.json` | PASS / 1 / 33.562 |
| `ApplicationArchitectureTests` | `V1-ARCH.json` | PASS / 38 / 25.063 |

**87/87 reviewer-fresh PASS**, zero failures/errors/skips. The separate 60-second-budget controller diagnostic is PASS, not an extra JUnit case. Existing suites still do not assert the residual mandatory cases, so their green results do not close those gates.

**25 reused invocations** from current producer round-1 `V1-REG.json`/its XMLs: VaultMutationConcurrencyTest 13, VaultMetadataIntegrationTest 11, GlobalSearchIntegrationTest#verifiesRecycleBinExclusion 1. Those source files, fixtures, mutation/guard behavior, schema and build inputs are unchanged/applicable; no fresh REG rerun is claimed. `case-ledger.json` records **112 unique current invocations across nine classes = 87 fresh + 25 reused**, their XML origins and test-source hashes. Producer's overlapping 112 and the initial review's earlier 112 are not additional cases or proof of completed V1 acceptance.

Round-1 producer failures are preserved and disclosed: V1-DB compile failure, V1-HTTP assertion failure, V1-HTTP-r2 compile failure and V1-SEC header assertion failure; six final passing groups have distinct receipts. Test retries do not consume additional remediation rounds. Environment remains Java 25/Maven 3.10.0/Testcontainers PostgreSQL 18.6 fixture. Configured Maven-repository metadata/authentication warnings and Byte Buddy dynamic-agent warnings are nonblocking for these successful builds, not silently suppressed. No IDE-clean/full-suite/performance SLA/Owner-data/deployment claim is made.

## Shared coverage and Docs Sync Check

The initial review's full rubric remains the baseline; the following accounts for every dimension without adding subsystems:

| Dimensions | Status / current evidence |
| --- | --- |
| 1,3,4 | PASS; same ratified need, frozen exception, owner-local dependencies/source boundary |
| 2 | FINDING; residual V1 traceability/proof and FR-V1-05 specimen accuracy |
| 5 | PASS; recursive current Vault inventory **63 files / 17 descriptors / zero new package**, no stale gitkeep; `vault-tree.txt` |
| 6–9 | PASS at source level; narrow typed input exception, root ADR-0016 envelope, immutable copied records and no speculative framework |
| 10 | PASS for observed domain/lifecycle behavior; missing runtime proof is separately identified |
| 11 | PASS for generated HTTP contract after FR-V1-04 closure |
| 12–14 | NOT_VERIFIED for complete validation/expired-session/no-write/cache evidence; observed ordinary controls pass |
| 15–16 | FINDING; unproven actual transaction-completion phase, FR-V1-01 |
| 17 | FINDING in generated SQL/plan evidence, FR-V1-02; no proven database/schema defect |
| 18,23 | NOT_VERIFIED for the required actual query observation; bounded result/source controls pass; no index or benchmark mandate |
| 19,20,24 | N/A; no retrieval/model/document/OCR/evaluation capability changed |
| 21–22 | FINDING; finite remaining controls, FR-V1-01/03; fresh suite results recorded above |
| 25 | N/A to dependency changes; none permitted/found |
| 26 | PASS for bounded compile/test/architecture; full IDE diagnostics NOT_VERIFIED, warnings classified |
| 27 | PASS for inspected response privacy/source logging; no raw cursor or provider/data hydration added |
| 28 | FINDING; false captured-specimen claims remain, FR-V1-05; current execution status synchronized |
| 29 | PASS; exact permitted source delta, all 13 hashes qualified, no agent staging/publication |

- **UPDATED:** this dated independent re-review, ACTIVE/current counter/disposition/next action and executable current status/counter plus a separate re-review entry. FR-V1-04 closure is recorded here/current register, not retroactively in the initial review. Final checks resolve **114/114 local links** across these three artifacts, find zero trailing-whitespace files, and confirm **zero source/test drift and zero drift in five preserved review/author-report inputs**; the index remains unstaged.
- **ALREADY_CONSISTENT:** published authority/base, exact 9+4 boundary, 13 final hashes, PRIVATE fallback/eight PUBLIC business GETs, RO-A Film POST denial, source/module/schema/build/harness freezes and predecessor counters. Original owner decisions, both author submissions and initial independent review are preserved.
- **DEFERRED_OUT_OF_SCOPE:** root/policy/README/roadmap/maintenance-index/package-tree/historical harness cleanup and actual-SHA publication closeout; separately authorized ChatGPT governance owns those artifacts. No unrelated LOW backlog is reopened.
- **NOT_VERIFIED:** residual mandatory cases described above, full suite/IDE, actual Owner-data scale, deployment/cutover. No independent V1 acceptance or source publication is issued.

**Next step: Antigravity | /antigravity-implement-handoff, then /antigravity-self-review | BF26-V1 + executable/ACTIVE + residual FR-V1-01/02/03/05 in this re-review | bounded round 2 within existing 9+4; preserve FR-V1-04 closure; independent Codex $codex-final-review before Owner commit | remediation 1/3 consumed; round 2 next; takeover NOT_AUTHORIZED / NOT_USED.**
