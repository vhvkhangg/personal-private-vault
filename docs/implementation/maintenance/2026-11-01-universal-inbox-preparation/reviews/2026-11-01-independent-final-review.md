# Independent slice review — UI26-INBOX-BROWSE

- **Date / reviewer:** 2026-11-01 / Codex, independent implementation review.
- **Disposition:** `CHANGES_REQUESTED`; three MEDIUM / OPEN blocking findings and one LOW / OPEN nonblocking hygiene note. No CRITICAL/HIGH finding.
- **Mode / authority:** implementation mode, entered with [ACTIVE](../../../handoffs/ACTIVE.md) = `IMPLEMENTED_AWAITING_CODEX_REVIEW`; [executable handoff](../executable-handoff-2026-11-01.md), following Owner-published preparation `a4f89ddcfa750548e28034c4df78ca0725f85a9b`. Local HEAD and live remote main match that SHA.
- **Counter:** initial independent review; **0/3 correction rounds consumed**, next correction is round 1/3. No takeover authorization, borrowed allowance or new feature scope.
- **Write boundary:** this report and live review/status sections in ACTIVE and the executable handoff only. Production/tests and the submitted handback are unchanged by Codex. Isolated reviewer probes/receipts are retained under ignored `logs/universal-inbox/codex-final-20261101-0798bc62433743b0a1cc346a727aab13/`.

## Findings

### UI26-FR-01 — MEDIUM / OPEN / BLOCKING: OpenAPI rejects the terminal cursor value

**Evidence:** [SavedResourceInboxPageResponse.java](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/dto/SavedResourceInboxPageResponse.java), lines 12–13, describes null in prose but declares only a String schema. The actual generated **OpenAPI 3.1.0** document has:

```json
{"type":"string","description":"Opaque continuation cursor for next page, or null if exhausted"}
```

The endpoint actually returns `nextCursor: null` on an empty/terminal result. That value fails the declared JSON Schema type; an optional property permits omission, not a present null. The generated cursor query parameter also omits the implemented 512-character bound. The submitted OpenAPI change checks the envelope name/route count, not the page fields or nullable cursor, so its passing test does not prove UI-09. Reviewer probe `terminalCursorNullMustBeRepresentedInGeneratedOpenApi` failed; full document is `generated-openapi.json` in the reviewer directory.

**Consequence / requirement:** wire/schema incompatibility violates UI-09 and the handoff's explicit nullable-cursor/truthful-parameter contract. This is an actual generated-document defect, not a request for another API or envelope.

**Smallest correction:** express nullability using the annotation/schema mechanism supported by the configured OpenAPI 3.1 generator; document the existing cursor bound locally without changing its 400 mapping or root OpenAPI configuration. Extend the permitted OpenAPI test to check page/items references, null-compatible cursor, parameters/default/bounds, and existing bearer/error conventions. Preserve exactly 231 unique operations and all earlier routes.

**Close with:** generated-doc assertions plus empty and nonempty terminal-page HTTP assertions; the actual emitted schema must accept the emitted terminal value.

### UI26-FR-02 — MEDIUM / OPEN / BLOCKING: cursor timestamp can come from a cached creation entity

**Evidence:** [SavedResourceService.java](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/SavedResourceService.java), lines 266–278, discards candidate timestamps, hydrates IDs with `findAllById`, then encodes `lastReturned.savedAt()`. JPA can return an entity already managed by the caller's transaction. The public save operation keeps its original creation Instant in that context, while PostgreSQL stores a rounded microsecond value.

The independent real-PostgreSQL probe called the existing public save and Inbox operations inside one caller transaction. It observed:

```text
database: 2026-11-01T14:27:52.219346Z
cursor:   2026-11-01T14:27:52.219345500Z
view:     2026-11-01T14:27:52.219345500Z
```

`generatedCursorMustUseDatabaseTupleEvenWithManagedCreationEntity` failed equality with the database tuple. Fresh-transaction paging tests still pass. **No HTTP duplicate/skip or exploit is claimed from this observation**; the demonstrated defect is the explicit database-read timestamp requirement under a supported caller transaction.

**Consequence / requirement:** UI-03 / handoff “Use database-read timestamps for comparison/serialization; an in-memory creation timestamp is not database-read evidence” is not met. The already-read authoritative candidate tuple is available but is lost before encoding.

**Smallest correction:** retain the database projection tuple for the last returned ID and derive the continuation token from it. Keep bounded retained state, ID order and eligible lookahead behavior. Do not change frozen save timestamps, add clock infrastructure, refresh every item, or clear a caller's persistence context.

**Close with:** a deterministic managed-entity/caller-transaction regression with a controlled sub-microsecond fixture, plus the existing persisted fractional/tie/no-skip checks. Assert the decoded token timestamp equals the persisted candidate timestamp. Both fresh and caller-owned transaction paths must satisfy the same contract.

### UI26-FR-03 — MEDIUM / OPEN / BLOCKING: mandatory evidence and handback overstate completion

**Evidence:** handoff lines 43 and 105–111 specify the finite remaining cases below. The submitted [Inbox database tests](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/SavedResourceInboxIntegrationTest.java) and [web tests](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/SavedResourceInboxWebIntegrationTest.java) do not establish all of them. In particular:

| Required proof still incomplete | Actual submitted evidence / minimal follow-up |
| --- | --- |
| Missing qualification result; all five kinds as database filters | No isolated missing-result test exists; database filtering exercises ARTICLE, while codec enum round trips are not database-filter proof. Add the small permitted negative and parameterize the existing filtering path |
| Changed page size and removed anchor | All database continuations retain their page size; anchor test only trashes the anchor. Exercise changed limit and actual disposable-fixture anchor removal/absence through the application read |
| Committed-trash visibility in the production read transaction | Lines 413–427 construct `new SavedResourceService` and call it without a surrounding read transaction, bypassing its transactional proxy. Keep the real qualifier/barrier, but exercise the same read-only/default-isolation transaction as the production call; retain bounded waits and cleanup |
| Real HTTP paging/default/max, valid continuation and representative structural negatives | The nine web tests never consume a non-null continuation or populate enough rows to establish the HTTP default/max. Complete a compact paging flow and representative empty/SQL-range/overflow/edited-tuple cases using the existing classes and error mappings |
| Nested metadata and private-marker error/log proof | The new untrusted-string test supplies title/URL only; the handback names `errorMessagesDoNotLeakCredentialsOrSql`, which does not exist. Reuse unchanged JSON helper evidence where applicable, and add the specified small HTTP/captured-output negative rather than claim an absent test |

The [handback](../implementation-handback.md), lines 19/21, also cites nonexistent method names including `missingQualificationResultExcluded`. Its lines 47–54 describe colon separators and a 1970 minimum, while the actual codec uses **pipes** and **0001-01-01**. The API extension note says a terminal page has `items: []`, although the existing 101-row test correctly returns a nonempty final page with a null cursor; its lifecycle prose also needs the handoff's qualification-time/live-list limit.

**Consequence / requirement:** UI-04/06/09/10 retain mandatory NOT_VERIFIED items, and the self-review assertion that every requirement is verified is not supported. The 129 passing tests are real; they do not cover missing scenarios. These are current accepted checks, not a new benchmark or future feature.

**Smallest correction:** complete the finite evidence above in the already permitted test files; correct the handback to actual method/receipt pointers, codec format/range and remaining results, and correct the implementation-adjacent API note. Use one combined fixture/parameterized flow where appropriate; no per-case subsystem or arbitrary test-count quota. Do not rewrite failed original logs. The independent batch observations below already supply real SQL/batch/retention evidence and may be reused after source-applicability checks.

**Close with:** truthful UI-01–10 mapping, mandatory cases actually demonstrated, mandatory self-review, and the four existing focused selector groups after the changes. No broader suite, RAG opt-in, migration or new approval docket is needed for these corrections.

### UI26-FR-L01 — LOW / OPEN / NONBLOCKING: extra blank lines fail whitespace check

`git diff --check` exits 1 for new blank lines at EOF in `FeedWebMapper.java:195`, `SavedResourceOperations.java:29` and `docs/architecture/api-architecture.md:401`. Remove them during an authorized touched-file cleanup if convenient; LOW alone does not require another correction round. No line-ending/formatter/tooling overhaul is warranted.

## Correct behavior and scope retained

The single Feed-owned read is proportionate. **KEEP** the small cursor/page types and current application/repository/web roles; **REUSE** Vault's existing public batch qualifier, shared HTTP envelope/advice, legacy capture/conversion/lifecycle/search paths; **DEFER** read/archive, metadata filters/badges, indexes, external fetchers and every later feature. No new pattern/framework, module edge, Vault implementation, schema, dependency, auth change or RAG capability is justified by these findings.

The scalar candidate projection prevents accumulation of rejected entities. The real reviewer fixture had 201 newer trashed rows and three older active rows; for limit 2 it observed:

- qualification batches **100 / 100 / 4**;
- three bounded scalar candidate SQL reads, followed by one two-ID entity hydration;
- **0 → 2 managed entities** in the surrounding read-only transaction;
- two returned items and a non-null lookahead cursor.

SQL shapes and counts are retained in `query-observations.json`. This verifies bounded candidate/result retention and batch qualification for that fixture, not constant query count or a production latency SLO. Total scanning can still grow with trash distribution, as the approved handoff allows. No index/migration is requested.

Existing mutations and legacy query bodies are unchanged. The added compatibility tests cover trashed duplicate/no orphan, legacy trashed reads and Vault favorite/tag actions. Module ownership, existing `vault::search` allowance, parent-Knowledge boundaries and root ADR-0016 transport placement remain intact.

## Evidence and execution

**Submitted evidence inspected:** all four JSON receipts and raw stdout summaries under `logs/universal-inbox/ui26-190b534b3a584c078ba36b541dc3f58a/`: **36 + 42 + 11 + 40 = 129**, zero failures/errors/skips, exits 0. These are Antigravity's runs, not 129 newly executed reviewer tests. Their full logs/receipts are preserved.

**Fresh reviewer evidence:** `logs/universal-inbox/codex-final-20261101-0798bc62433743b0a1cc346a727aab13/`.

| Receipt / artifact | Actual result |
| --- | --- |
| `review-inbox-database.json` | Normal 300s watchdog; exact `SavedResourceInboxCursorTest,SavedResourceInboxIntegrationTest` selection; **36/36 passed**, no failures/errors/skips; exit 0, **40.922s** |
| `review-probe-compile.json` | Existing local JDK/test dependencies only; isolated source compiled under ignored reviewer directory; exit 0, **2.531s** |
| `review-probes.json` | Same normal cap; **3 probes, 1 passed / 2 failed**, exit 1, **41.219s**; failures reproduce FR-01/02, batching probe passed |
| `SavedResourceInboxReviewProbe.java`, argument files, stdout/stderr | Reproducible isolated probe source and launch inputs; no repository production/test rewrite |
| `generated-openapi.json` | Actual MockMvc-generated 3.1.0 schema and parameters; basis for FR-01 |
| `query-observations.json` | Real candidate SQL, qualifier batch sizes and managed-entity counts |
| `submission-sha256.json` | SHA-256 inventory of all **18 submitted changed/new files**; no submitted-file drift during source inspection or probes |

Environment from the fresh Surefire report: **Windows 11, Oracle Java 25.0.2**; existing PostgreSQL `ppv-rag-postgres:18.6-pgvector0.8.6` and Ryuk images were already local. Original test selectors/results, Maven test compilation, existing architecture/archive receipts and source inspection supply the other evidence; no full suite, archived QA, model inference, worker launch, runtime installation/download or Git publishing was performed.

All potentially long reviewer commands used the implemented watchdog, normal profile and 300s cap with new non-clobber receipt paths. One read-only inspection print hit the shell Python stdout encoding limit; it was repeated with process-local UTF-8 output. It was not a test failure or a configuration/trust repair.

**Diagnostics classification:** the original compile warning about deprecated-for-removal `MappingJackson2HttpMessageConverter` in unchanged `audit/MediaBinaryFramingWireIntegrationTest.java:82` is preexisting migration debt outside this slice. Mockito dynamic-agent/CDS warnings are existing JDK/test-harness conditions, not evidence of a new Inbox defect. No blanket suppression/configuration change was made. No IDE inspection was run or IDE-clean/warning-free claim made.

## Acceptance and shared review coverage

| Acceptance | Result / limit |
| --- | --- |
| UI-01, UI-02, UI-05 | PASS for inspected unchanged behavior and supplied focused compatibility/web evidence |
| UI-03 | FINDING — FR-02; remaining paging/HTTP proof FR-03 |
| UI-04 | PARTIAL / NOT_VERIFIED — existing trash cases and fresh batching pass; missing/transaction cases FR-03 |
| UI-06 | PARTIAL / NOT_VERIFIED — 401, ordinary string and non-HTTP URL checks pass; specified negatives FR-03 |
| UI-07, UI-08 | PASS — source/import/schema scope, fresh SQL/retention, supplied unchanged architecture and three archive-boundary checks |
| UI-09 | FINDING — generated schema FR-01 and current doc/evidence drift FR-03 |
| UI-10 | FINDING — self-review exists, but completion/evidence claims need FR-03 correction |

All 29 dimensions were considered; grouped rows identify every included number explicitly.

| Rubric dimensions | Status / evidence |
| --- | --- |
| 1 Owner intent | PASS — reuse-only goal and exclusions retained; findings add no new feature |
| 2 Traceability | FINDING — FR-03, false/incomplete mandatory evidence mapping |
| 3 Architecture | PASS — actual diff against module matrix, frozen DBML/Flyway and ADR-0016; no boundary/schema change |
| 4 Ownership / transactions | PASS for legal Feed/Vault/Knowledge direction; transaction-test limitation in FR-03 |
| 5 Folder/package tree | PASS — recursively inspected actual Feed subtree: **87 Java files / 24 existing packages**, all main packages have descriptors, no stale `.gitkeep`; new cursor test stays in matching owner test package |
| 6 Shared API / exceptions | PASS — existing root envelope and safe `IllegalArgumentException → 400`; no new exception hierarchy or shared module |
| 7 SOLID; 8 DRY/KISS; 9 Patterns | PASS — cohesive small additive roles and public batch reuse; no speculative strategies/framework |
| 10 Domain / state | FINDING — FR-02; ordinary trash, duplicates, conversion and live-list design retained |
| 11 Wire/OpenAPI | FINDING — FR-01; exact route count passes but inner schema contract does not |
| 12 Validation | PARTIAL / NOT_VERIFIED — codec negatives pass; finite integration omissions FR-03 |
| 13 Authentication / visibility | PASS for existing bearer protection and active qualification; no new privacy boundary or anonymous route |
| 14 Secrets / egress | NOT_VERIFIED only for mandatory marker/log negative FR-03; source adds no fetch, credentials, logging or retention path |
| 15 Atomicity / concurrency | FINDING for transaction proof FR-03; existing duplicate/rollback behavior remains unchanged |
| 16 Failure / recovery | PASS — no new job/mutation lifecycle; legacy rollback and restore retained |
| 17 Persistence / query | PASS for unchanged mappings, scalar queries, batch hydration and owner-local persistence; cursor authority issue separately FR-02 |
| 18 Complexity / resource bounds | PASS for measured finite fixture and bounded retained state; no general performance claim |
| 19 Retrieval/recommendation; 20 Document/OCR; 23 Standalone benchmarking; 24 Evaluation science | N/A — no changed/enabled capability, model, parser, ranking or approved performance SLO |
| 21 Security tests; 22 Test quality | FINDING — existing checks plus fresh reproductions; mandatory negative/transaction evidence FR-03 |
| 25 Dependencies / lifecycle | PASS for no dependency/build/runtime change; no fresh repository-wide supply-chain audit claimed |
| 26 Build / tools / diagnostics | PASS for executed focused compile/test/watchdog gates and warning classification; no IDE inspection |
| 27 Logging / observability | Source PASS for no new noisy/private logging; explicit negative proof NOT_VERIFIED under FR-03 |
| 28 Documentation / governance | FINDING — FR-03; scoped synchronization below, dated history preserved |
| 29 Hygiene / delivery | FINDING — nonblocking FR-L01; all submitted targets fit permitted scope, no staging/publishing |

## Scoped Docs Sync Check

- **UPDATED:** this independent review, ACTIVE and executable-handoff review/status sections only; `CHANGES_REQUESTED`, initial 0/3 and next round 1/3. Issued requirements and original passing receipts remain intact.
- **ALREADY_CONSISTENT:** published preparation baseline, existing Feed/Vault package ownership, matrix/ADR/schema/dependencies, inactive indexing and suspended/archival-only S19-2. Checked **37 local file targets** in submitted changed Markdown, zero missing targets; this is not an external-URL/anchor audit.
- **FINDING / correction owner Antigravity within existing handoff:** current API extension and handback facts in FR-01/03, plus mandatory evidence. Do not “fix” source to match the incorrect colon/1970 documentation. Original handback is preserved by the reviewer for traceability; implementation-adjacent correction remains expressly authorized.
- **DEFERRED_OUT_OF_SCOPE:** routine scope README/maintenance navigation/publication closeout and old CSA26 root/policy notices remain ChatGPT's separately authorized governance work. Existing CSA26-FR-L01, SAD-FR-L01, AUTH-CLOCK and historical RAG findings/counters are not closed or transferred.
- **NOT_VERIFIED:** listed remaining acceptance, future fixes and eventual Owner publication. No accepted/published Inbox delivery is claimed.

## Bounded correction and next gate

Correct FR-01/02 and complete FR-03 using the existing permitted Feed/application/DTO/controller/test/API-note/handback targets. Reuse the passing batch evidence if source remains applicable. Add focused regressions for the two reproduced failures, finish the finite missing cases, run the four handoff selector groups under their existing 300s caps, self-review and retain fresh receipts. Keep LOW separate from blocking acceptance. No new schema decision, root handler/configuration change, new module, RAG test or full-suite permission follows.

**Next step:** Antigravity | `/antigravity-implement-handoff`, mandatory `/antigravity-self-review`, then Codex `$codex-final-review` | `UI26-INBOX-BROWSE`, [executable handoff](../executable-handoff-2026-11-01.md) and FR-01–03 above | resolve three MEDIUM blockers and mandatory evidence before Owner commit | 0/3 consumed; next correction/re-review 1/3; takeover not authorized.
