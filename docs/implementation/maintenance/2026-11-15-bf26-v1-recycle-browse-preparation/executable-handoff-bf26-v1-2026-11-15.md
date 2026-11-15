# Executable slice handoff — BF26-V1-RECYCLE-BROWSE

- **Handoff ID:** `BF26-V1-RECYCLE-BROWSE`.
- **Created by / date:** Codex / 2026-11-15.
- **Status:** `READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED` — [independent final re-review 2](reviews/2026-11-15-bf26-v1-independent-final-re-review-2.md), FR-V1-01–05 CLOSED. 112 distinct qualified passing invocations = 49 reviewer fresh + 63 applicable reused; BFV1-L01/L02 LOW/OPEN/BACKLOG nonblocking. Owner publication pending; no deployment/cutover claim.
- **Implementer:** Antigravity, existing [backend-implementer](../../../../.agents/agents/backend-implementer/agent.md), `/antigravity-implement-handoff`, followed by mandatory `/antigravity-self-review`.
- **Final reviewer:** independent Codex in a separate session, `$codex-final-review`. This handoff is not implementation acceptance.
- **Entry branch / HEAD:** existing `main` / `58cbd58d1f3fe6be002ba97eb6599292a758f8f5`; no new branch/worktree.
- **Remediation:** `2/3 consumed / STOPPED`; round 2 independently accepted; no third round pending. Takeover `NOT_AUTHORIZED / NOT_USED`.

## 1. Owner authority, sources of truth and entry qualification

The [Owner Authorization](owner-authorization-bf26-v1-2026-11-15.md) expressly ratifies the entire product/API contract, the **exact nine production/four existing test** Phase 1/13 exception, and the ordinary existing `main` checkout. Authorization was Owner-published at [`757643e9adcafa0c54b0adb3cca93cfa8efd0731`](https://github.com/vhvkhangg/personal-private-vault/commit/757643e9adcafa0c54b0adb3cca93cfa8efd0731), following published preparation [`5e333d96a4565b7e5e40d3a15bc5db57a10dd56f`](https://github.com/vhvkhangg/personal-private-vault/commit/5e333d96a4565b7e5e40d3a15bc5db57a10dd56f). The [independent preparation review](reviews/2026-11-15-bf26-v1-independent-pre-handoff-review.md) is **READY_FOR_HANDOFF — TECHNICAL PREPARATION ONLY**, with no blocking finding. Its five carry-forward qualifications are binding here. Earlier pending/proposed wording in the reviewed preparation describes its pre-authorization checkpoint; the subsequent Owner decision controls without rewriting those inputs.

Other sources: [finite acceptance V1-01–19](finite-acceptance-v1.md), [HTTP/privacy proposal ratified by that decision](candidate-http-and-privacy-contract.md), [qualified source map](candidate-source-test-map.md), [source/risk inventory](source-baseline-and-risk-inventory.md), [Owner policy](../../../workflow/current-owner-delivery-policy.md), [Docs Sync](../../../workflow/documentation-synchronization.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [module dependency matrix](../../../architecture/module-dependency-matrix.md), [package tree](../../../repository/repository-package-tree.md), and [frozen DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml). The [ACTIVE register](../../handoffs/ACTIVE.md) selects only this executable.

**Verified entry facts, not runtime proof:** local HEAD, `origin/main` and live remote `main` all equal the entry SHA; tracked/untracked worktree and index were clean before these two handoff writes. The authorization and preparation publications are ancestors of HEAD. Changes since reviewed baseline `2d088445e6323b091741cf370e1e0c8cf13928a2` are preparation/authorization/navigation Markdown only. The complete `backend` Git tree at that baseline, preparation publication, authorization publication and entry HEAD is identically **`7e68fb0a202fc5544cbf94c342d40bef2d258c8a`**. All **17** inventory file/blob claims match current working files; all four NEW paths are absent with existing parent `package-info.java`. The Vault subtree remains 59 files / 17 descriptors. Source inspection confirms no existing equivalent Vault trash-list handler or `listVaultTrash` operation ID. Graphify was navigation only; canonical source was checked.

Before implementation, recheck actual branch/HEAD/status, these input fingerprints, source collision/authorization state and ACTIVE. A docs-only later publication is not automatically material source drift; inspect it. Preserve unrelated work. STOP for a revocation, competing handoff, material unapproved drift or unexplained source mismatch; do not reset counters or silently expand the map.

## 2. Goal and simplicity decision

Allow the PRIVATE Owner to discover existing soft-deleted Vault identities without retaining their IDs, through **one** read-only `GET /api/v1/vault/trash`. Return only identity/type/deletion time with bounded ID-descending continuation. Existing trash/restore-by-ID remains unchanged.

| Owner-visible need / mechanism | Decision / smallest adequate implementation | Relative effort and limits |
| --- | --- | --- |
| Discover deleted identities | **KEEP / REUSE** existing Vault entry API, service, repository and controller | Moderate implementation/verification; one vertical slice |
| Recognize each returned identity | **SIMPLIFY** to exactly `id,entryType,deletedAt`; map existing immutable item view to dedicated HTTP DTO | Low; Owner accepts reduced recognition without titles |
| Bounded traversal | **KEEP** one owner-local keyset query, small cursor codec and immutable page records | Moderate; no shared pagination framework or job engine |
| Ordering/consistency | **KEEP** ratified `id DESC`, weak consistency between requests | Low implementation/moderate proof; not deletion chronology |
| Permission and root envelope | **REUSE** existing PRIVATE fallback and root `ApiResponse`/factories | No security/root implementation changes; explicit regression proof |
| Query cost | **KEEP** server-side bound/seek and synthetic sparse-trash plan evidence; **DEFER** tuning | No latency SLA, index grant or benchmark platform |
| New agents/dependencies/technical modules | **REMOVE** from scope; existing backend implementer/tooling suffice | None |
| Titles, chronological order, total count, filters, UI and purge | **DEFER** to separately approved work | None in V1 |

These are relative static estimates, not measured durations. Existing five-field HTTP DTO reuse would violate the ratified three-field list contract; a titled cross-module collection or chronological cursor/index would require different approval. No material product choice remains open for this executable.

## 3. Precise change map — nine production / four existing tests

`J/` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` = `backend/src/test/java/com/vhvkhangg/personalprivatevault/`. Blob IDs identify committed contents, not SHA-256 or runtime acceptance. Exactly these source/test paths may change:

| ID | Exact path | Permitted outcome / entry Git blob |
| --- | --- | --- |
| P01 | `J/vault/entry/VaultEntryOperations.java` | Add one typed bounded trash read, retaining all old signatures; `22f1392dd16206422146ac770ec6d251f55e7975` |
| P02 | `J/vault/internal/application/entry/VaultEntryService.java` | Validate limit/cursor, call owner query, materialize immutable page/cursor; retain lifecycle/guards; `648100017d024b7bea19c9cc9e7496b1c04ebe28` |
| P03 | `J/vault/internal/infrastructure/persistence/VaultEntryRepository.java` | One parameterized deleted-only ID seek with DB-enforced limit+1; `b605742a45c95ff8a348407e724a63162d090531` |
| P04 | `J/vault/internal/application/entry/VaultTrashCursor.java` **NEW** | Package-local strict bounded versioned cursor codec; existing package |
| P05 | `J/vault/view/VaultTrashPageView.java` **NEW** | Immutable page over existing `VaultEntryView` plus nullable continuation; existing named interface |
| P06 | `J/vault/internal/web/controller/VaultController.java` | Exactly one GET handler, typed annotations, route-local validation/error/no-store; old handlers unchanged; `de14f47df09f3ac1726d3c8655663eaf5c8c42e5` |
| P07 | `J/vault/internal/web/dto/VaultTrashItemResponse.java` **NEW** | Exact three-property item record; existing package |
| P08 | `J/vault/internal/web/dto/VaultTrashPageResponse.java` **NEW** | Immutable HTTP page, always-present items/nullable nextCursor; existing package |
| P09 | `J/vault/internal/web/mapper/VaultWebMapper.java` | Pure new page/item mapping; old mappings unchanged; `4f3b3a9ae7678d703a828c163fdfa7beaed35775` |
| T01 | `T/vault/VaultEntryIntegrationTest.java` | PostgreSQL seek/bounds/plan, traversal, immutability and committed lifecycle witnesses; `6ae55cd88a3d856c50ac8cd2f311ad551b93bd51` |
| T02 | `T/vault/VaultWebIntegrationTest.java` | Actual PRIVATE JSON, input/error/cache/conditional HTTP/no-write witnesses; `63c7d7dd659559c0b590355f425a0483686acd93` |
| T03 | `T/authentication/ProfileRouteAuthorizationIntegrationTest.java` | Both persisted PUBLIC origins denied before handler, PRIVATE/401 controls, exact-eight/dispatcher lock; `d173776b8b128287efff1f3bb177cbd181d0f3af` |
| T04 | `T/web/OpenApiRouteInventoryIntegrationTest.java` | One manifest row, coupled totals and generated graph/HTTP specimen reconciliation; `e72b4142e9c362bb0d1ce1ca7acf85525930eeca` |

Read-only regression execution, **no edit permission**: `VaultMutationConcurrencyTest`, `VaultMetadataIntegrationTest`, `ApplicationArchitectureTests`, relevant existing `SecurityFilterChainIntegrationTest` controls and `GlobalSearchIntegrationTest#verifiesRecycleBinExclusion`. No new test class/file. Four NEW production classes stay in existing packages; no descriptor/module changes.

## 4. Binding HTTP, cursor and failure contract

### Admission and exact wire

`GET /api/v1/vault/trash?limit=25&cursor=...`, operation ID **`listVaultTrash`**. Valid persisted PRIVATE session gets 200 on valid input. Valid PUBLIC sessions from **both** direct login and PRIVATE→PUBLIC downgrade get **403 ACCESS_DENIED before the MVC business handler and new owner read**. Anonymous/invalid/expired credentials get canonical 401, no handler. Authentication/session authority persistence is legitimate and is not the business trash query. Do not modify security matchers/session logic; the existing PRIVATE fallback must suffice.

Success is the root envelope with `data:{items:[...],nextCursor:string-or-null}`, `error:null`, `meta:null`. Both data properties are always present; every item has **exactly** `id` (positive int64), `entryType` (existing enum), `deletedAt` (non-null ISO-8601 UTC instant). Empty/exhausted pages have `nextCursor:null`; empty pages have `items:[]`. No created/updated time or feature/content/metadata enrichment in the new wire. PRIVATE metadata browsing covers Vault types without introducing feature-level content lookups. Existing five-field `VaultEntryView` remains an internal application item; existing five-field HTTP `VaultEntryResponse` stays untouched.

Illustrative future result, **not an observed runtime specimen**:

```json
{"data":{"items":[{"id":142,"entryType":"FILM","deletedAt":"2026-11-14T08:12:01Z"}],"nextCursor":null},"error":null,"meta":null}
```

### Limit and canonical cursor

- Omitted limit defaults to **25**. Accepted inclusive **1–100**. Non-integer/decimal/overflow/out-of-range input gives canonical safe **400 before the business repository read**. Distinguish an absent parameter from malformed supplied input.
- Absent cursor starts traversal. Supplied cursor is canonical **unpadded Base64url UTF-8 of `v1|<positive-id>`**, maximum encoded length **128 characters**. Encoder emits the positive int64 in decimal. Reject blank, padding, non-URL alphabet, ambiguous/noncanonical encoding, malformed UTF-8, unsupported version, extra/missing components, invalid/nondecimal/nonpositive/overflow ID. Use strict decoding plus canonical roundtrip verification or equivalent; Java's replacement-character UTF-8 decoding alone is insufficient. Do not log/echo cursor or parser diagnostics.
- Static codec example: `v1|142` → `djF8MTQy`. This is a cursor format example, not runtime test evidence or an authorization token.
- Seek strictly **below last emitted ID**, `id DESC`; one owner-local parameterized deleted-only query, at most **limit+1 qualifying DB rows**. At most limit emitted items. No count, offset, whole-table Java filter or unbounded intermediate list. Cursor is non-null **iff** lookahead exists and encodes the last emitted item, never lookahead.
- Example with stable deleted IDs `[300,280,260,240]` and limit 2: first emits `[300,280]`, cursor `djF8Mjgw`; second emits `[260,240]`, cursor null. Exactly-limit with no lookahead yields null immediately. Active gaps never appear.

### Error, cache and completion boundary

All successful PRIVATE responses and applicable generated errors must carry **Cache-Control: no-store**. Test canonical 400/401/403/500 and conditional HTTP against the actual stack. Do not introduce a cacheable or bodyless conditional metadata success. Errors use the root envelope (`data:null`, safe canonical error, no internal/cursor payload). Syntax conversion may use existing `MALFORMED_REQUEST`; controlled range/codec validation uses existing `INVALID_ARGUMENT`. Assert the actual branch-specific code explicitly in HTTP evidence; do not claim the two branches necessarily share a code.

Genuine repository, read transaction or transaction-completion failure is **500 INTERNAL_ERROR**, never empty 200, cursor/limit 400 or lifecycle 409. Separate expected input validation from operational execution so an operational exception cannot masquerade as bad input. Use existing root factories; contain safe handling in the new P06 route/P02 operation. Global advice can reset responses, and existing Vault advice maps IllegalState/data-integrity failures to 409: setting no-store only on success or relying on that old advice is insufficient. Catch/translate the new path's operational and completion failures at the controlled boundary. Do not change old handler/advice behavior. No raw exception messages, cursor, secrets, private payload or sensitive stack traces in new error/log output.

## 5. Minimal implementation path and reusable seams

1. Add one `VaultTrashPageView`-returning read to P01 (a suitable shape is `listTrash(int limit, String cursor)`, null cursor meaning first page). Keep constructors/old operations unchanged. P04 is a small owner-local codec; do not import Feed internals or create a shared abstraction.
2. P03 adds a bounded list/projection query over `vault_entries` through its current Vault owner. Reuse the existing `VaultEntryView` projection concept in `findSourceViews`; filter `deleted_at IS NOT NULL`, optional `id < cursorId`, descending ID and database limit+1. A bounded list avoids a count query; actual generated SQL, not method naming, must prove seek/bound. Do not acquire mutation/source locks.
3. P02 validates before query, retains the existing class-level read-only service transaction/isolation, and constructs the complete immutable page before service return. Determine lookahead/last-emitted cursor inside this bounded operation. `List.copyOf` or equivalent defensive copies in application and HTTP page records; no JPA entity/lazy collection escapes. Pure later HTTP mapping is allowed. One owner query needs no RO-B multi-owner REQUIRES_NEW/REPEATABLE_READ design or controller transaction.
4. P07/P08/P09 explicitly map down to three item properties. Reuse root `ApiResponses` without offset `ApiPageMeta`, shared serializer edits or public application-view serialization.
5. P06 adds only `/trash`, with typed response metadata and PRIVATE-only operation description; preserve old routes. If parsing is kept in the controlled handler to preserve error/no-store behavior, generated query schema must still truthfully describe integer limit/default/bounds and bounded cursor. Validate before the new repository read; catch actual operational and proxy-completion failures safely.
6. T01–T04 supply the finite proof below. T04 updates **all coupled** manifest/operation/unique-ID/subset totals from actual registration/generation, including existing 239-subset assertions where applicable, without labelling that subset a global route count. Root `OpenApiConfiguration` already has a generic typed/error customizer: use annotations; no root edit permission follows from a schema mismatch.

**Selected engineering skills:** `java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`, `design-pattern-selection`, `modular-monolith-architecture`, `jpa-postgresql-persistence`, `rest-api-http-contracts`, `backend-testing`, `bounded-command-execution`; `authentication-security` for existing permission regression analysis only, without auth source permission. Use the existing backend implementer and separate independent Codex review path; no additional agent or skill/hook update is needed. Python/RAG, feature-content and frontend skills are N/A. Root and scoped backend/test/Vault instructions apply within this exact Owner exception; dated older RAG status/runner wording does not supersede current Owner policy.

## 6. User-visible acceptance — V1-01–19 (all runtime NOT_RUN)

These IDs are requirements, not a promised number of test methods or passing cases. The ratified matrix and all independent-review qualifications are retained.

| ID | Required scenario / observable result | Minimal proof and reusable seam |
| --- | --- | --- |
| V1-01 | Exactly one additional GET `/api/v1/vault/trash`; existing entry/trash/restore handlers unchanged; no new PUBLIC/mutation/search route | T03/T04 real registered handler set, source diff; existing VaultController |
| V1-02 | Persisted PRIVATE valid request 200; both persisted PUBLIC origins 403 ACCESS_DENIED before handler/read; anonymous/invalid/expired 401; eight original PUBLIC GET pairs unchanged | T03 positive-controlled MVC adapter/selected-handler witness, owner-read spy and current sweep; no mock-profile substitution |
| V1-03 | Actual HTTP data always has items/explicit nullable nextCursor, error/meta null; each item exactly three correctly typed properties | T02 exact JSON key sets/values; T04 schema/specimen reconciliation; explicit new DTOs |
| V1-04 | Empty trash and all-active Vault each return 200, items [], nextCursor null | T01/T02 real fixtures; deleted-only query |
| V1-05 | Omitted limit 25; 1/100 valid; output ≤limit and DB qualifying fetch ≤limit+1; no count/offset/unbounded paging | T01/T02 populated default/min/max witnesses plus actual SQL; bounded repository projection |
| V1-06 | 0/-1/101/non-integer/decimal/huge overflow limit yields safe canonical 400, no business repository read | T02 HTTP status/code/no-store, positive-qualified business query spy; existing factories |
| V1-07 | Absent cursor starts, canonical roundtrip works; malformed/blank/padded/ambiguous/invalid UTF-8/version/components/ID/overlong values yield safe 400 | T01/T02 exercise new operation/HTTP before query; no new codec test file |
| V1-08 | Stable membership across ≥3 pages, all supported Vault enum types and active gaps: strictly descending IDs, no duplicate/skip, active excluded | T01 real PostgreSQL traversal against expected deleted ID set; owner entry fixtures |
| V1-09 | Last-emitted cursor, non-null iff lookahead exists; one/exactly-limit/limit+1/empty/exhausted boundaries correct | T01/T02 HTTP/owner traversal, decode emitted cursor; no lookahead skip |
| V1-10 | A genuinely committed restore between pages removes later membership while old cursor bound stays strict, no earlier-ID repetition | T01 isolated committed requests/transactions, reuse restore API; class test transaction alone insufficient |
| V1-11 | Committed re-trash retains ID/lifecycle rules; changed membership does not make read write/loop/lock; weak traversal semantics documented | T01 committed re-trash/new high-ID trash sequence, bounded completion and no-write witnesses; existing lifecycle API |
| V1-12 | Actual PostgreSQL seek/filter/order/limit+1 with bound parameters, no count/offset/Java whole-table filter; representative sparse-trash plan captured | T01 generated SQL + bounded synthetic EXPLAIN; source/migration freeze; qualifying rows ≠ rows examined |
| V1-13 | Valid/invalid read makes no domain writes, provider/job/RAG/indexing/cross-module hydration; real query and transaction/completion faults → safe 500 | T01/T02 business-query/no-write witnesses and narrowly labelled fault injection, actual transaction-proxy boundary |
| V1-14 | Existing trash/restore idempotence/timestamps, mutation locks and source/reservation guards retained | T01 lifecycle + V1-REG concurrency; unchanged methods/guard sources |
| V1-15 | Existing Search trash exclusion/metadata rules retained; no title, Person detail, source content, NSFW flag, tags/favorite/rating/notes in list | T02 exact allowlist; V1-REG metadata and `GlobalSearchIntegrationTest#verifiesRecycleBinExclusion` |
| V1-16 | Actual generated docs: one new GET, unique listVaultTrash, correct refs/page/item/parameters/default/bounds/security and canonical 200/400/401/403/500 | T04 inventory/coupled totals and generated graph reconciled with T02/T03 specimens; existing root customizer unchanged |
| V1-17 | no-store on success, bad input, operational/completion errors and checked security errors; conditional requests do not leak cacheable metadata | T02/T03 actual headers/status/body including parser-before-handler path if used; preserve shared advice/filter |
| V1-18 | Module/package ownership, ADR-0016, existing dependency graph and frozen schema/security/build boundaries unchanged except exact grant | V1-ARCH plus final allowlist/frozen-input diff and package inventory; no new module/package descriptor |
| V1-19 | Final hashes/case-to-evidence map, preserved failures, truthful fresh/reused provenance, self-review and independent review/Owner-only publication | Section 9 handback; self-review is author evidence, independent acceptance remains pending until separate Codex review |

## 7. Required proof at changed boundaries

- **Security:** use actual persisted PRIVATE and both PUBLIC session origins from current fixtures. Positively control the spy with the real new PRIVATE handler, then verify zero handler and new owner-read invocation for forbidden requests. Retain the eight-route allowlist, PUBLIC Film POST denial, sensitive/nested denial and ASYNC/ERROR controls. Domain snapshot/no-write checks exclude legitimate authentication/session persistence; invalid PRIVATE query must skip the business trash repository read, not all SQL globally.
- **Transactions/materialization:** T01 currently has class-level `@Transactional`; use an isolated committed sequence (e.g. suitable test transaction propagation/transaction templates within allowed files) to prove restore/re-trash between requests. Verify no lazy acquisition after service return, defensive copied lists and unchanged source guards. Use finite synchronization only for an actual overlapping race; do not simulate commit by an uncommitted update in the same test transaction.
- **SQL/plan:** exercise first and subsequent pages of the new operation; record actual SQL/binds/fetch bound, absence of count/offset, and plan on finite synthetic data containing many active gaps/sparse trash. Record fixture cardinality/distribution, PostgreSQL version and observed rows/plan limits. `limit+1` caps returned qualifying rows at 101, not scanned rows or latency. No prescribed planner node/timing PASS on tiny fixtures. If observations require an index/order change, STOP for exact Owner decision; no migration here.
- **Failures/cache:** prove malformed numeric input, each cursor rejection category, a repository failure, and a completion failure raised after a successful bounded read but before proxy return. Test the actual service transaction boundary and HTTP response; label injected completion faults honestly, rather than presenting a mocked facade throw as physical commit proof. Include IllegalState/data-integrity operational paths to guard against existing 409 advice. Each must be safe 500/data:null/no-store; preserve old Vault conflict semantics. Invalid input and valid read remain read-only. Capture safe synthetic specimens, with no raw cursor in error/log output.
- **OpenAPI:** inspect actual `/v3/api-docs`, follow schema refs to page/item/envelope and inspect nullable/required fields, limit integer/default/range, cursor length and PRIVATE security description. Root customizer's existing extra GET 404 documentation is unchanged, not a new V1 runtime feature or cleanup obligation. Annotation insufficiency is a STOP with generated evidence, not permission to edit root config.
- **Coverage limits carried forward:** runtime security/headers/completion, generated OpenAPI/SQL, sparse-trash plan and immutability are NOT_VERIFIED at creation and mandatory in implementation. Full application suite, full IDE diagnostics, real Owner data/latency and deployment suitability are not claimed. RAG retrieval/OCR/model evaluation, dependency upgrades and frontend/native-browser verification are N/A because V1 changes none of those capabilities.

## 8. Finite verification contract

Use existing `scripts/run-bounded-command.py`, **normal 300-second hard cap per command including startup**, Testcontainers real PostgreSQL and MockMvc. Six groups are the final scoped verification contract, not six slices or a full-suite mandate:

| Group | Exact initial selector | Required scope |
| --- | --- | --- |
| V1-DB | `VaultEntryIntegrationTest` | V1-04/05/07–12/14; new query, bounded plan and committed traversal/materialization |
| V1-HTTP | `VaultWebIntegrationTest` | V1-01/03–07/09/13/14/17; real JSON, error/completion/no-store/no-write |
| V1-SEC | `ProfileRouteAuthorizationIntegrationTest,SecurityFilterChainIntegrationTest` | V1-02/13/17; persisted origins, positive handler/no-handler, eight pairs/dispatch |
| V1-API | `OpenApiRouteInventoryIntegrationTest` | V1-01/03/05–07/16; manifest and actual generated graph/specimens |
| V1-ARCH | `ApplicationArchitectureTests` | V1-18 plus separate final frozen-file/source diff |
| V1-REG | `VaultMutationConcurrencyTest,VaultMetadataIntegrationTest,GlobalSearchIntegrationTest#verifiesRecycleBinExclusion` | V1-14/15; lifecycle/guards/metadata and Search trash exclusion |

Example from repository root, **for Antigravity after implementation, not executed at handoff creation**:

```powershell
$v1Python = (Get-Command python -ErrorAction Stop).Source
$v1RunDir = Join-Path 'logs/bf26-v1' ('implementation-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $v1RunDir -ErrorAction Stop | Out-Null
& $v1Python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $v1RunDir 'V1-DB.json') -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=VaultEntryIntegrationTest' test
```

Run groups **sequentially**, substituting exact selector and a fresh receipt name per attempt. Runner creates sibling stdout/stderr logs and refuses collisions. Preserve each attempt's fresh Surefire XML before another Maven run replaces reports; distinguish stale XML when a command fails before tests. Preserve failures/timeouts/skips/disabled cases. Diagnose nonzero promptly; if 300 seconds is insufficient, split finite disjoint actual methods/classes and reconcile coverage without increasing timeout or retrying unchanged for luck. No concurrent Maven/shared fixtures, arbitrary process termination, native hook bypass or startup/trust/configuration repair. Cleanup only owned test processes/containers via existing tooling.

Maintenance B1/B2 are independently accepted/Owner-published: current normal cap 300s, broader approved full-suite cap up to 2,100s with inactivity diagnosis and scoped native Windows enforcement. **V1 grants no full-suite/extended-time run**; those broader limits do not change this contract. Supported-client/trusted-root limits remain factual, not universal containment claims.

Final applicable **fresh coverage of changed T01–T04** is mandatory, either whole classes or a documented complete applicable selector set. Run architecture on final inputs. Unchanged regression/security evidence may be reused only after verifying source/test/fixture/config/build input applicability, exact case/receipt/XML and changed-boundary impact; changing service input forbids assuming old lifecycle evidence remains sufficient. New V1 SQL/HTTP/security/plan/failure proofs require fresh evidence. Deduplicate test class/name/parameter identities; requirement IDs, assertion counts and reruns are not distinct passing cases. No predetermined test total.

## 9. Documentation impact, definition of done and handback

| Affected current artifact | Editor / permitted timing | Observable evidence |
| --- | --- | --- |
| This executable and `docs/implementation/handoffs/ACTIVE.md` | Codex creates now; Antigravity may update current implementation-result/status/counter/evidence/next-action fields after implementation/self-review | Same V1 ID, actual round/status, links to final hashes and receipts; baseline contract sections preserved |
| `implementation-handback-bf26-v1-2026-11-15.md` in this directory | Antigravity, implementation-adjacent report only | V1-01–19 → actual final test/case → command/receipt/stdout/stderr/XML → input hashes/outcome/limit; current Docs Sync Check |
| `self-review-bf26-v1-2026-11-15.md` in this directory | Antigravity, mandatory author self-review | Concise scope/architecture/privacy/tests inspection, failures/limits and pending independent adjudication |
| A later V1 independent final review under `reviews/` | Separate independent Codex `$codex-final-review` | Findings/status and fresh/reused qualification; no self-acceptance |
| Routine root/policy/maintenance-index/roadmap/package-tree navigation and actual-SHA publication closeout | ChatGPT only under separately scoped governance/closeout work; Owner publishes | Report specific drift/owner in handback; no broad cleanup or premature publication claim |

No other implementation-adjacent documentation edit is authorized. Preserve the reviewed preparation, Owner decision, independent review, predecessor histories and raw evidence. If a later correction round is authorized by independent review, use fresh round-labelled handback/self-review/evidence paths; do not overwrite earlier reports.

Handback must include actual branch/HEAD/diff list, exact final source/test SHA-256 hashes (and qualified unchanged inputs), tool/JDK/Maven/PostgreSQL/fixture versions, case/evidence mapping for every requirement, actual SQL/plan and committed-sequence observations, actual HTTP/OpenAPI/error/header specimens, fresh/reused distinct case accounting, failures/remaining NOT_VERIFIED limits, cleanup and docs disposition. Mandatory unresolved evidence or CRITICAL/HIGH/MEDIUM finding blocks final acceptance; LOW/INFO is backlog under Owner policy.

After tests and `/antigravity-self-review`, persist **IMPLEMENTED_AWAITING_CODEX_REVIEW** in the top current ACTIVE banner and exact top-level `- **Status:**` field, this executable, implementation-result section, round ledger and current Next step. Then reopen the saved ACTIVE/executable/handback and compare status, ID, counters, takeover and independent-review state using the implement skill's saved-file handback gate. Self-review does not close findings independently. Route to separate-session Codex `$codex-final-review`, not Owner commit. Only independent acceptance permits READY_FOR_OWNER_COMMIT; only Owner publishes.

### Docs Sync Check at handoff creation

- **UPDATED:** only this new executable and ACTIVE's current V1 selection/status/contract/next-action; prior ACTIVE content stays visibly historical below the new register.
- **ALREADY_CONSISTENT:** published exact Owner grant and preparation review, clean entry `main`, same backend tree/17 input blobs/four absent NEW paths, existing PRIVATE fallback/eight PUBLIC GETs, source map and predecessor counters. Current root/policy/maintenance navigation points to V1 authorization; earlier preparation pending headings are explicitly superseded by that later decision, not rewritten.
- **DEFERRED_OUT_OF_SCOPE:** broader README/roadmap/package-tree navigation, historical backend/docs instructions and old backend-implementer/REST wording identified in the preparation review; ChatGPT owns any separately requested governance correction. No source boundary or executable authority follows from those old notices.
- **NOT_VERIFIED / NOT_RUN:** all V1 runtime cases, SQL/OpenAPI/plan/headers/completion, full suite/IDE diagnostics, real Owner data and deployment. Handoff validation is documentation/static verification only.
- **STATIC HANDOFF VALIDATION:** saved ACTIVE and executable agree on READY_FOR_IMPLEMENTATION, V1 ID and 0/3 counters; their exact 13-path maps equal the Owner Authorization, with zero mismatches. All 19 unique requirement rows and six finite gate rows are present; 18 local links in the executable/current ACTIVE resolve. Scoped whitespace/diff checks pass; only the two handoff documents change, with backend/preparation/authorization/review inputs preserved. These are author documentation checks, not independent runtime acceptance.

## 10. Constraints, STOP rules and preserved state

Do not edit root `OpenApiConfiguration`, root API envelope/factories/meta/advice, `SecurityConfiguration`/session/JWT/PIN/filter/global Jackson/transaction manager, old Vault entity/item view/five-field response/advice, guards/locks/old lifecycle semantics, module/package descriptors, DBML/Flyway/schema/indexes, POM/lock/build/dependencies, other modules or harness/hooks/agents. No title/name/source hydration, additional PUBLIC route, filtering/count/search, chronological order, hard delete/purge/retention, provider calls/jobs/indexing, Frontend/BFF, RAG, audit or deployment. Study Feed cursor/page conventions read-only; no Feed internal import. Use existing `main`, no new branch/worktree. Only Owner stages/commits/pushes/tags/releases/opens or merges PRs.

STOP with exact evidence/path and seek a separate exact Owner decision for any required source/test path beyond 9+4, extra test file/module/dependency/schema/security capability, inaccurate generated schema requiring root edits, failure/no-store behavior not satisfiable route-locally, unacceptable measured plan requiring index/order change, material unauthorized drift/revocation/competing ACTIVE, unresolved new privacy choice or exhausted remediation. Do not weaken the contract to force green or reopen a completed predecessor.

**Current V1 counters:** round 2 independently accepted; **READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED**, FR-V1-01–05 CLOSED. Remediation **2/3 consumed / STOPPED**; no third round pending. BFV1-L01/L02 LOW/OPEN/BACKLOG do not trigger per-slice rework. Test retries and reviews do not create/reset correction rounds. Takeover NOT_AUTHORIZED / NOT_USED; no automatic new slice or publication.

**Preserved predecessors:** RO-B INDEPENDENTLY_ACCEPTED / OWNER_PUBLISHED / CLOSED at `0f3365de888ab03b0d1de6b32e56a3bb7ad96d5a`, 114 distinct qualified cases (25 reviewer fresh + 89 reused), DPROB-L01/L02 LOW/OPEN/BACKLOG, remediation 2/3 STOPPED; RO-A Owner-published/CLOSED at `0956b4962f529050dddf88dc7f2ec19ac571e0d0`, remediation 1/3 STOPPED, DPROA-L01 LOW backlog. W1 2/3 STOPPED; B1 3/3 STOPPED; DP-A 3/3 exhausted / exceptional takeover 1/1 accepted; prior LOW backlogs unchanged. RO-A/RO-B/W1/B1 takeover not authorized/used. H1-F paused/FR-13 upstream-blocked, P17 frozen, P18 closed, S19-2 suspended, providers OFF/unset, indexing inactive; no live cutover or real Owner-data suitability claim.

## 11. Initial implementation result — preserved Antigravity submission

**IMPLEMENTED_AWAITING_CODEX_REVIEW.** All 9 production files and 4 test files implemented within exact approved boundaries. All 6 verification groups passed under bounded watchdog (V1-DB, V1-HTTP, V1-SEC, V1-API, V1-ARCH, V1-REG; 112 test executions, 0 failures). Implemented behaviors, exact SHA-256 hashes, receipts, and specimens documented in [implementation-handback-bf26-v1-2026-11-15.md](implementation-handback-bf26-v1-2026-11-15.md) and [self-review-bf26-v1-2026-11-15.md](self-review-bf26-v1-2026-11-15.md). Remediation budget: 0/3 consumed; takeover NOT_AUTHORIZED / NOT_USED.

## 12. Codex remediation and independent final review

**Initial independent review — 2026-11-15: CHANGES_REQUESTED.** [Review and exact evidence/remediation](reviews/2026-11-15-bf26-v1-independent-final-review.md) open **FR-V1-01–05 MEDIUM / OPEN**. Correct the operational IllegalArgumentException-to-400 branch and prove real proxied completion failure; capture actual generated SQL/binds/fetch/representative plan; finish the finite missing boundary/negative/no-read/no-write/expired/header witnesses; correct generated page/item required-field semantics through allowed DTO annotations; deliver truthful round-labelled case/specimen/provenance reports. Existing nine production/four test permissions and all frozen/STOP rules remain unchanged. No extra source file or global configuration grant is implied.

Reviewer fresh **87/87 PASS** (DB 13, HTTP 7, SEC 28, API 1, ARCH 38); **25 applicable reused REG** cases qualify a **112 distinct passing invocation union**, not completed V1 acceptance. Separate controller diagnostic reproduces the wrong operational 400; actual generated OpenAPI capture confirms missing required fields. All 13 submitted hashes match; author reports and original failed/passing evidence remain preserved. Use fresh correction-round-1 receipts/XML/reports and mandatory self-review, then independent Codex re-review. **0/3 consumed; round 1 next**, takeover NOT_AUTHORIZED / NOT_USED. No commit message or publication is issued while blockers remain.

## 13. Round 1 remediation result — Antigravity submission

**IMPLEMENTED_AWAITING_CODEX_REVIEW.** All 5 findings (FR-V1-01 through FR-V1-05) resolved within the exact 9 production and 4 test files. All 6 verification groups passed under bounded watchdog (V1-DB-r2, V1-HTTP-r3, V1-SEC-r2, V1-API, V1-ARCH, V1-REG; 112 passing test invocations, 0 failures, 0 errors, 0 skipped). Observed PostgreSQL backward index scan plan captured with execution time ~0.03ms; typed exception `InvalidTrashInputException` separates input validation from operational/completion 500 errors; all 17 entry types traversed; populated boundaries and codec negatives verified; required OpenAPI fields (`nextCursor`, `id`, `entryType`, `deletedAt`) enforced; full receipt disclosure with failed attempts reported. Documented in [implementation-handback-bf26-v1-round-1-2026-11-15.md](implementation-handback-bf26-v1-round-1-2026-11-15.md) and [self-review-bf26-v1-round-1-2026-11-15.md](self-review-bf26-v1-round-1-2026-11-15.md). Remediation budget: 1/3 consumed; takeover NOT_AUTHORIZED / NOT_USED.

## 14. Independent final re-review 1 — 2026-11-15

**CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED.** [Re-review 1](reviews/2026-11-15-bf26-v1-independent-final-re-review-1.md) closes **FR-V1-04** and credits fixed operational classification, all-type traversal, terminal boundaries, ordinary headers and corrected method/receipt mapping. **FR-V1-01/02/03/05 remain MEDIUM/OPEN** only for their documented residual obligations: actual transaction completion-phase injection, generated service/repository SQL/binds/correlated plan, finite maximum/canonical/no-write/expired-session controls, and traceable truthful captured specimens. No new source permission or finding ID.

Reviewer **87/87 fresh PASS**, plus **25 applicable reused = 112 distinct passing invocations**; these do not establish unasserted mandatory cases. All 13 final hashes match; 10 source/test files differ from initial submission within the existing 9+4 map. Preserve original and R1 submissions/raw failures, FR-V1-04 closure and already-proven subcases. Use fresh round-2 evidence/reports and mandatory self-review; independent Codex re-review remains required. **1/3 consumed; round 2 next**, takeover NOT_AUTHORIZED / NOT_USED. No commit message or independent acceptance while blockers remain.

## 15. Round 2 remediation result — Antigravity submission

**IMPLEMENTED_AWAITING_CODEX_REVIEW.** All 4 residual findings (`FR-V1-01`, `FR-V1-02`, `FR-V1-03`, `FR-V1-05`; `FR-V1-04` closed in re-review 1) resolved within the exact 9 production and 4 test files:
- `FR-V1-01`: Positively verified real bounded repository acquisition and materialization, injected fault via scoped `TransactionSynchronization.beforeCommit` during physical completion phase, ensured clean rollback/connection release via `AbstractPlatformTransactionManager.setRollbackOnCommitFailure(true)`, and proved safe 500 `INTERNAL_ERROR` with `Cache-Control: no-store` and `data: null`.
- `FR-V1-02`: Intercepted actual generated Hibernate SQL via `org.hibernate.SQL` and bind parameters via `org.hibernate.orm.jdbc.bind` (`[null, null, 3]` initial, `[52, 52, 3]` continuation). Correlated `EXPLAIN (ANALYZE, COSTS OFF)` plan directly to parameter-corresponding captured statement, showing `Limit -> Index Scan Backward using vault_entries_pkey on vault_entries ve1_0` (0.021ms / 0.029ms execution time, 2 shared buffer hits).
- `FR-V1-03`: Added maximum boundary check (101 trashed `NOTE` items, limit 100); added valid canonical unpadded control `v1|1` -> `"djF8MQ"` (200 OK) and rejected unpadded ambiguous Base64url `"djF8MR"` before query; positively qualified repository observer on valid read; added full domain state row comparison (`SELECT id, entry_type, deleted_at, created_at, updated_at FROM vault_entries ORDER BY id`) before vs after reads and re-trash operations confirming zero DML writes; decoded admitted private token to extract real `sid` and `generation` claims, constructed otherwise-valid expired session token, and proved 401 `AUTHENTICATION_REQUIRED`, `Cache-Control: no-store`, and `never()` on handler/operations.
- `FR-V1-05`: Evidence provenance resolved with actual stdout captures directly from fresh test runs (`FILM` wire response, controlled handler `"Invalid argument provided"` 400 error, exact security messages for 401/403, and generated OpenAPI schemas with OAS 3.1 `["string", "null"]` nullable representation). Disclosed full receipt ledger including failed compile and timeout attempts.

All 6 verification groups passed under bounded watchdog (V1-DB-r2, V1-HTTP-r2, V1-SEC, V1-API, V1-ARCH, V1-REG; 112 passing test invocations across 9 classes, 0 failures, 0 errors, 0 skipped). Documented in [implementation-handback-bf26-v1-round-2-2026-11-15.md](implementation-handback-bf26-v1-round-2-2026-11-15.md) and [self-review-bf26-v1-round-2-2026-11-15.md](self-review-bf26-v1-round-2-2026-11-15.md).

Remediation budget: **2/3 consumed**; round 2 complete; ready for independent final re-review 2. Takeover `NOT_AUTHORIZED / NOT_USED`.

## 16. Independent final re-review 2 — 2026-11-15 acceptance

**READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED.** [Re-review 2](reviews/2026-11-15-bf26-v1-independent-final-re-review-2.md) CLOSED **FR-V1-01–05**. Fresh transaction-manager evidence locates the actual beforeCommit fault/rollback; actual SQL/binds/correlated plans, maximum/canonical/state/session controls and matching captured bodies/schemas qualify the remaining finite obligations. No mandatory finite V1 acceptance remains unverified.

**112 distinct qualified passing invocations across nine classes = 49 reviewer fresh + 63 applicable reused**, not additive with earlier runs. All 13 final hashes match; R2 changes only the four allowed test files, with nine production files unchanged from R1. **BFV1-L01/L02 LOW / OPEN / BACKLOG** are nonblocking (completion-test inactive-sync fallback and minor R2 author source-signature/class-subtotal prose). Remediation **2/3 consumed / STOPPED**; takeover **NOT_AUTHORIZED / NOT_USED**. Preserve earlier author submissions/reviews/raw successes/failures/timeouts; no third correction round or source expansion. Owner-only publication and later actual-SHA closeout remain pending; no deployment/Owner-data suitability claim.

**Next step: Owner | ordinary git add/commit/push reviewed delivery | BF26-V1 + this executable/ACTIVE and independent re-review 2, exact 9+4 plus scoped delivery records | READY_FOR_OWNER_COMMIT; then brief actual-SHA closeout, no deployment grant | remediation 2/3 consumed / STOPPED; takeover NOT_AUTHORIZED / NOT_USED.**
