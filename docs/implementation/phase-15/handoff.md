# Phase 15 — Accepted Backend Audit Remediation Handoff

- Handoff ID: `phase-15-backend-audit-remediation`
- Created by: Codex, 2026-10-07
- Status: `COMPLETED — BACKEND_AUDIT_READY`; owner commit/push pending
- Implementer: Antigravity
- Final reviewer: Codex
- Audit gate: `BACKEND_AUDIT_READY` (2026-10-08)
- Implementation baseline: `6a89a998c512dda27c3e494a3525bf6118ee981e`

Archived by `$codex-backend-audit` after the [final closure audit](reviews/2026-10-08-phase-15-closure-backend-audit.md).
All BA15-1–BA15-17 findings and FR15-9/10/11/12 follow-ups are CLOSED; no unresolved actionable finding remains.
The owner may commit/push. Phase 15 is not complete/frozen until publication and ChatGPT closeout.
The retained submissions, review returns and earlier next-step/commit restrictions below are historical gate records;
this final closure disposition supersedes them. No second handoff or further implementation authority is created.

## Goal

Close only the 17 authorized BA15 findings through conservative correctness/HTTP/validation/concurrency/documentation
repairs. Preserve all non-approved Phases 0–14 behavior, schema and module boundaries. This is the **single evolving
Phase 15 remediation handoff**, not a new product phase; authorization is not finding closure.

## Sources of truth

- [Owner decisions](../phase-15/owner-decisions.md) — exact approval choices and stop conditions; precedence over
  alternative proposals in the historical audit.
- [Initial audit](../phase-15/reviews/2026-10-06-phase-15-backend-audit.md) — stable BA15 IDs, evidence, consequences
  and required regressions; observations are not implementation scope.
- [Authorization re-review](../phase-15/reviews/2026-10-07-phase-15-authorized-remediation-codex-review.md),
  [audit status](../phase-15/audit-status.md), [Phase 15 contract](../phase-15/README.md).
- [Closure audit](../phase-15/reviews/2026-10-07-phase-15-closure-backend-audit.md) — current disposition and
  precise evidence/acceptance for the four remaining authorized BA15 findings.
- [First Oct8 final review](../phase-15/reviews/2026-10-08-phase-15-final-codex-review.md) — three Medium blockers
  FR15-10/11/12 on the submitted closure-remnant repairs; no new audit closure verdict or broader authority.
- [Previous final re-review](../phase-15/reviews/2026-10-08-phase-15-final-codex-rereview.md) — FR15-10 functional
  repair accepted; FR15-11 narrowed Medium test gap and FR15-12 Low current-evidence remnants. Test/evidence only.
- [Previous final re-review 2](../phase-15/reviews/2026-10-08-phase-15-final-codex-rereview-2.md) — accepted raw-wire,
  clearing, update-state and snapshot/conversion proof; FR15-11 only eight missing Vault no-write checks,
  FR15-12 Low evidence precision. No production repair or authority expansion.
- [Current final acceptance](../phase-15/reviews/2026-10-08-phase-15-final-codex-acceptance.md) — FR15-11/12 CLOSED,
  independent 1021/0/0/0; READY FOR OWNER COMMIT, but **do not commit yet**. Next repository-wide closure audit.
- [ADR-0018](../../adr/0018-phase-15-bounded-backend-remediation.md),
  [module matrix](../../architecture/module-dependency-matrix.md),
  [integration policy](../../architecture/integration-and-eventing.md),
  [API architecture](../../architecture/api-architecture.md),
  [frozen DBML](../../database/personal-private-vault-schema-v1-FROZEN-final.dbml),
  [repository tree](../../repository/repository-package-tree.md).
- Root/backend/affected module/test `AGENTS.md`; accepted owning-domain validators and existing public capabilities.

## Implementation targets and per-finding acceptance

Java paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`.
The historical audit supplies the full evidence/path/contract for every ID; do not broaden these target families.

| Finding | Permitted target / required outcome | Required regression evidence |
| --- | --- | --- |
| BA15-1 | `backend/pom.xml` and minimal runtime integration: Boot-managed Flyway before Hibernate validate; unchanged SQL, no manual prerequisite. | Empty real PostgreSQL startup using production configuration **without** the test support's explicit migrator; V1/V2/history, validation and restart. |
| BA15-2 | Affected `*/internal/web/dto`/mapper contracts identified in the audit: Account, Media, Location, Knowledge, Collection, Journal, Personal, Feed, Import, Finance, Settings, Film/Fiction. Match existing owning null/default/blank/length/range rules; domain validators authoritative. | HTTP create/update + generated schema at true maxima/max+1, nullable/default/identity alternatives and exact blank Markdown preservation. Cover related drift, not only the diary example. |
| BA15-3 | `authentication/internal/infrastructure/security/SecurityConfiguration` and necessary credential boundary tests/docs: standard full-input encoding for new passwords; keep 12–128 characters and delegating legacy bcrypt verification. | ASCII 12/72/73/128, below/above range, multibyte >72 bytes, full-input distinction/no truncation, old bcrypt login, unchanged PIN/JWT/refresh/bootstrap privacy and behavior. |
| BA15-4 | Import decision, Account snapshot, Finance transaction/rule and Location schedule request lists/own mappers: reject null members, retain legitimate list null/empty semantics. | Canonical 400/no-write HTTP tests for each list family and supported unknown/empty schedules. |
| BA15-5 | Root `ApiExceptionHandler`/OpenAPI error declaration: narrow unsupported Content-Type/Accept translation with canonical 415/406 where applicable. | Supported JSON, unsupported Content-Type/Accept, safe envelopes/accurate schema; no changed committed-stream handling. |
| BA15-6 | Feed/Import owning advice: translate **public** Knowledge validation/conflict/not-found exceptions safely, without nested imports. | Invalid author combination, duplicate Note hash/missing reference; 422/409/404 as applicable, privacy and complete target/provenance/job rollback. |
| BA15-7 | Media `ImageController`/shared error boundary: clear stale binary headers only before commitment; retain post-commit abort/no JSON append. | **Embedded HTTP wire** tests with original lengths below/above error JSON length, complete canonical JSON/framing, committed failure/disconnect/timeout and cleanup. |
| BA15-8 | Media download framing: known provider length wins; metadata fallback only when unknown; preserve metadata-edit and content-type rules. | Real downloads after smaller/larger/null size edits return all original bytes with correct length; unknown-provider fallback/closure. No new mismatch-rejection policy. |
| BA15-9 | Location owning scalar/schedule mutations/read locks and persistence methods: serialize coherent state, including fresh state after a prior managed read. | Deterministic PostgreSQL scalar versus known/unknown schedule writers/readers, both orders/prior same-transaction read; all scalar fields/flag/intervals preserved. |
| BA15-10 | Vault metadata rating set and owner repository: narrow atomic upsert/serialization for concurrent first set, preserving timestamps/capabilities. | Barrier-controlled first-set race, both valid requests complete/one row, createdAt/update semantics, subsequent set/remove/trash cases. |
| BA15-11 | Location category assignment/owning advice/query: safe specific reference validation/race mapping; keep conflict-safe set semantics. | Missing parent/category, reference-removal race and duplicate/concurrent assignment; no partial rows/private vendor detail. Do not change absent-parent empty-list reads. |
| BA15-12 | Brand/Location, Study, Shopping and Software owning price validators plus affected HTTP errors: pre-persistence numeric(19,4) width/scale rejection, no rounding/normalization. | True representable bounds, integer overflow/five-fractional-digit values in every affected family, HTTP/command rejection and response/reload consistency; not a misleading uniqueness conflict. |
| BA15-13 | Account-owned public capability/guard coordination and Study-owned validation/persistence within existing Knowledge → Account direction only. Preserve invariant after assignment and serialize assignment versus account mutation. | Sequential/concurrent invalidating type/platform edits versus Study create/update, safe rejection/rollback; ordinary valid/unreferenced Account updates work; existing Modulith/named-interface/cycle checks pass unchanged. **Preflight/stop below.** |
| BA15-14 | `importdata` public operations, owning paged query, item-review controller/schema: bounded complete inspection of >100 items. Prefer compatible default first page and ≤100 per-request bound; every accepted item reachable. | 100/101 and multi-page jobs, stable item-index order, no omissions/duplicates/unbounded response, first-page compatibility/truthful metadata, complete decisions and atomic execution. No new total ingestion cap. |
| BA15-15 | Targeted Search/Finance/Collection OpenAPI annotations/schema docs only, plus synchronization of authorized corrected contracts. | Generated descriptions for literal/tag-all search, ledger sign/cardinality/transfer/category, frequency-specific fields and update/default/purchase clearing; match actual validators and meaningful errors. |
| BA15-16 | Ten existing Collection package descriptors; only the two listed `.gitkeep` files; identified `docs/architecture/README.md` future-tense statements. | Descriptor/placeholder/link/whitespace inventory, no new module/named-interface annotations or edge change. Preserve historical records. |
| BA15-17 | Journal/Personal update/delete/restore and Feed fetch/configuration writer pairs and owner repositories only: schema-preserving protection against stale independent writes. | Deterministic PostgreSQL barriers, both commit orders, no implicit restore/stale content/config; exact Markdown, deletion timestamps and self-profile uniqueness retained. |

## Required behavior / invariants

- The owner choices are fixed: managed migrations, existing domain rules, full 12–128-character passwords with
  legacy bcrypt compatibility, exact monetary representation with rejection, persistent Study/Account invariant,
  and complete bounded >100-item import review. Do not substitute the audit's rejected alternatives.
- Reproduce source-derived concurrency findings with targeted failing regressions before a repair. If the current
  baseline does not exhibit the alleged issue, record evidence and return for Codex disposition; do not manufacture
  locks/refactoring just to match a recommendation.
- Keep validation/error/privacy/transaction policies in their owner; use only existing public/named interfaces.
  No cross-module JPA or another module's internal package. Keep Knowledge nested persistence nested-owned.
- No rounding, truncation, partial multi-module/job writes, rollback-only retry, or success on malformed binary framing.
  Canonical JSON errors apply only before binary commitment; committed failures abort and release resources.
- Keep precise error classes/statuses, truthful pagination and redacted logs. Do not broadly translate all integrity
  or framework exceptions merely to make a test pass.
- Update relevant canonical runtime/security/API/import and current governance docs alongside each actual repair;
  ADR-0018 records the bounded authorization. Never describe pending behavior as implemented.

## Non-goals

- No Flyway/DBML/schema/index/column/version additions or rewrites; no Hibernate auto-DDL.
- No new application modules, named-interface boundaries, dependency directions/cycles, ownership changes,
  Account → Knowledge, or expansion of ADR-0017's read-only export exception.
- No generic locking/validation/common framework, broad refactoring/deduplication/static suppression/dependency
  upgrades, or permanent audit-tool plugins/configuration. Static observations are not actionable targets.
- No 100-item ingestion cap or separately unapproved total-job limit; no silent rounding or byte-limited passwords.
- No new image-size mismatch/409 business policy, unrelated reference-read semantics, new authentication features,
  frontend/RAG/deployment, Feed scheduling/providers, recurring Finance runtime or unrelated API features.
- No historical review/evidence rewrite, mass placeholder deletion, commit/push/tag/PR.

## Test/evidence contract

- Read/use `backend-testing`, `jpa-postgresql-persistence`, `modular-monolith-architecture`,
  `java-spring-coding-standards`, `pragmatic-solid-design`, `reuse-and-consistency`; use `authentication-security`,
  `rest-api-http-contracts`, `backend-integration-portability-storage`, and the affected existing domain skills.
  `architecture-change` is a stop/governance aid, not permission to expand this ADR.
- Focused tests: each row above and each initial audit regression requirement. Use JUnit/AssertJ, MockMvc where
  suitable, **real embedded HTTP for framing**, Testcontainers PostgreSQL for storage/races, existing MinIO/storage
  regressions and Spring Modulith architecture checks. No H2; no timing-only/sleep-based race proof.
- Final implementation command, from backend: `mvn -ntp clean verify`. Record actual suite count, failures/errors/
  skips, Java/Maven/runtime/container versions and exact focused commands. **920 is the preserved baseline, not an
  expected post-remediation test total.**
- Required evidence file: `docs/implementation/phase-15/test-evidence.md` — one section per BA15 ID with changed
  symbols, focused failing/passing evidence, privacy/rollback/transport/concurrency assertions, remaining risks,
  implementation decisions (especially BA15-13) and canonical docs synchronized.
- Retain existing initial audit evidence/limitations before clean; ignored scratch harnesses/reports do not become
  production/test dependencies. Do not claim generated report success means no static findings.
- Handoff-specific `$codex-final-review` follows implementation. Then the **closure `$codex-backend-audit`** must
  rerun necessary full/static/coverage/architecture/OpenAPI/repository evidence against the changed baseline,
  including dependency analysis, compatible SpotBugs/PMD/CPD and spelling substitute/limitations. Initial evidence
  alone cannot close Phase 15.

## Constraints / risks — mandatory stops

1. **BA15-13 preflight:** document the proposed narrow coordination/guard, dependency/transaction/lock order and
   why it fits existing public namespaces/direction before implementing this finding. An Account-owned public guard/
   synchronization contract consumed by Study is a possible bounded approach, **not an approved implementation
   claim**. Prove the chosen mechanism's boundary/race behavior with tests. If it requires a new dependency direction,
   schema or materially new architecture/ADR beyond narrow coordination, stop and report
   **OWNER_DECISION_REQUIRED for BA15-13 before implementing it**. No asynchronous eventual-consistency substitute.
2. **BA15-14:** any additional total ingestion limit beyond already accepted resource/file-size bounds requires
   separate owner approval. Per-page bounds must still make every accepted item's review reachable.
3. Any other expansion/frozen-baseline conflict stops for owner/Codex disposition. This handoff and the explicit
   owner decision record are the temporary authority for these enumerated repairs despite historical phase-only
   module gates; they authorize nothing beyond this slice.
4. Keep one evolving handoff. Never start a second Phase 15 handoff for re-review findings. Final-review
   READY_FOR_OWNER_COMMIT is **not permission to commit** until closure audit returns BACKEND_AUDIT_READY.

## Submitted implementation result — resubmission claims

Implemented by Antigravity on 2026-10-07.
The following is the submitted narrative, not Codex acceptance. Re-review 4 independently verified 1012 tests
and retained narrow FR15-7/FR15-8 remnants. The latest acceptance below closes those blocking remnants and
records a Low documentation-symbol follow-up for closure disposition.
Antigravity reports addressing all review findings, fully closing remaining Medium blockers **FR15-7** and **FR15-8**:
- **FR15-1–FR15-6:** Closed in prior re-review for specific defects; preserved unchanged (fresh locked Account state/no fallback, authoritative Feed scheduling, canonical 406/OpenAPI, correct Finance/Collection descriptions, Feed conversion parity, and known-zero provider framing).
- **FR15-7 (Regression Acceptance & Comprehensive Verification):**
  - **BA15-2 & BA15-15 (`WebDtoValidationAuditIntegrationTest` - 19 tests):** Completed the full related-drift matrix with 5 new test methods and PUT counterparts: Shopping, Software, and Album create/update names and titles (500 valid / 501 -> 400); Fiction genres, Film genres, and Location categories create/update names (150 valid / 151 -> 400); manual SavedResource create boundaries (title 1000/1001, author/sourceName/externalId 500/501 independently); Import job creation originalFileName (500/501); Finance recurring rules, subscriptions, and transaction descriptions (500/501 names/providers, 1000/1001 descriptions with accepted null/blank); Settings update values accepted above removed caps (paginationSize 200 > 100, autoLock 2000 > 1440, backupInterval 10000 > 8760), positive bounds enforced (0 rejected), and valid timezone accepted / 65 rejected with 400. Added successful HTTP PUT controls with 1000-character descriptions for both recurring rules (`PUT /api/v1/finance/recurring-rules/{id}`) and financial transactions (`PUT /api/v1/finance/transactions/{id}`), asserting HTTP 200 OK and that the full 1000-character description is retained in `$.data.description`. Completed PUT update counterparts for Account displayName/ownerName (500/501), Image metadata (title 500, imageType 100, locationText 500), Address fields (addressType 100, postalCode 32), Knowledge study, information, vocabulary (maxima and rejections for pronunciation 501, partOfSpeech 101, sourceName 501), and note. Expanded OpenAPI generated schema assertions covering Shopping full-replacement/clearing and omitted status WISHLIST default; Software full-replacement/clearing and required type; Finance update transaction matching create; recurring rule name/description; subscription name/provider; Fiction/Film/Location genres (150); Album (500); SavedResource (1000/500); ImportJob (500); Settings timezone (64) and min 1 without max; and Knowledge Information & Vocabulary.
  - **BA15-4 (`NullCollectionMemberValidationTest` - 5 tests):** Fixed schema queries using exact column/table names (`recurring_rule_entries.recurring_rule_id`, `location_business_hours.location_id`). Proves canonical HTTP 400 `VALIDATION_ERROR` targeting exact null element fields (`itemDecisions[0]`, `entries[0]`, `intervals[0]`) and verifies zero database writes on invalid requests alongside writing rows on valid controls across all 5 list families.
  - **BA15-6 (`FeedImportKnowledgeExceptionIntegrationTest` - 4 tests):** Fixed fixture baseline timing so `initialVaultCount` is captured after `createTestSavedResource`, accurately verifying zero leftover vault entries and clean rollback of target entities and job provenance (`status == VALID`, `importedVaultEntryId == null`).
  - **BA15-7 & BA15-8 (`MediaBinaryFramingWireIntegrationTest` - 8 tests):** Proves pre-commitment first-read failures clear binary headers and return canonical JSON error envelopes for payloads both smaller and larger than the error JSON, zero-length streams return `Content-Length: 0`, client disconnect releases resources cleanly, edits to metadata size return full original stream, and unknown provider length falls back to metadata size.
  - **BA15-9, BA15-10, BA15-17:** Converted all raw `Thread` and `.join()` calls to `ExecutorService` and `Future<?>` across `LocationConcurrencyIntegrationTest`, `VaultRatingFirstSetConcurrencyIntegrationTest`, and `LifecycleWritersConcurrencyIntegrationTest`. Zero `.join()` calls remain in `audit/`.
  - **BA15-12 (`PriceNumericBoundsIntegrationTest` - 6 tests):** Proves both HTTP create and update variants for scale (> 4) and width (> 15 integer digits overflow) rejections across Brand (`minPrice`, independent `maxPrice`), Location (`minPrice`, independent `maxPrice`), Study, Shopping, and Software. Proves exact persisted reload equality via `operations.findById` for MAX_VALID (`999999999999999.9999`) and FOUR_DECIMALS (`10.1234`).
  - **BA15-13 (`StudyAccountInvariantIntegrationTest` - 12 tests):** Converted worker threads to `ExecutorService` and `Future<?>`. Proves 4 deterministic competing writer contention tests using `awaitCompetingLock` observing PostgreSQL row lock blocking in `pg_locks` for both update orders and creation orders.
  - **BA15-14 (`ImportJobPaginationIntegrationTest` - 4 tests):** Proves contiguous combined ordered sequence without omissions or duplicates across multiple pages for 100-item and 101-item jobs, verifies bounded-list envelope contract (`jsonPath("$.meta").doesNotExist()`), strictly asserts query-schema property presence (`has("default")`, `has("minimum")`, `has("maximum")`) and a lower bound on `limit` that strictly excludes zero (supporting OpenAPI 3.0 boolean and OpenAPI 3.1 numeric bounds), and verifies multi-page atomic execution rollback beyond page 0.
- **FR15-8 (Evidence, Documentation & Symbol Truthfulness):**
  - Canonical Architecture Notes: Rewrote `docs/architecture/phase-15-implementation-notes.md` strictly matching backend reality: documented bootstrap master password length (12–128) and login max 128 with no registration/password-update API; PBKDF2 default with delegating BCrypt verification; private PIN 6-digit format hash verification in authenticated context without rate limits or server auto-lock timers; standard `ApiResponse(data, error, meta)` envelope with `fieldErrors` list on `ApiError`, noting approved successful raw binary response exceptions (`ImageController` binary content, `PortabilityController` zip streams); monetary HTTP rejections mapped to 422 (`BRAND_INVALID`, `LOCATION_INVALID`, `KNOWLEDGE_INVALID`, `INVALID_COLLECTION`); public Feed/Import Knowledge exceptions translated to 422 `INVALID_KNOWLEDGE_ITEM`, 409 `KNOWLEDGE_CONFLICT`, 404 `KNOWLEDGE_NOT_FOUND`; streaming first-read I/O mapped to 500 `INTERNAL_ERROR` while retaining specific storage error codes (409); null-meta envelope (`meta == null`); batch rollback preserving prior states; and startup class `FlywayStartupIntegrationTest` with Account-owned SPI `ExternalAccountMutationGuard` and Study-owned Spring bean `StudyExternalAccountGuard` coordinating synchronous `guard.validateMutation(...)` callback inside `ExternalAccountService.update` under pessimistic row lock, mapping `ExternalAccountConflictException` to 409 `EXTERNAL_ACCOUNT_CONFLICT` via `AccountExceptionAdvice`, and qualifying deadlock claims to the exercised bidirectional serialization.
  - Test Evidence: Fully updated and synchronized `docs/implementation/phase-15/test-evidence.md` with accurate 1012/92 test counts, exact suite table (19 tests in WebDtoValidationAuditIntegrationTest), accurate symbol `importJobExecutionInvalidKnowledgeItemTriggers422AndRollsBack`, `FOUR_DECIMALS = 10.1234`, proxy test distinction for category FK, `FeedItemService.ingestFetch` write lock/refresh/authoritative scheduling/`saveAndFlush`, original Import HTTP window reachability, honest classification of historical discovery evidence (explicitly acknowledging BA15-9 and BA15-13 were source-derived interleavings/sequences without captured pre-fix runtime failure traces), and qualified lock ordering statement under BA15-13.

Full verification command executed:
`mvn -ntp clean verify`
- Result: **BUILD SUCCESS** (Time elapsed: 03:55 min)
- Preserved baseline: 920 tests
- Total tests run: 1012 tests (92 net new focused audit regression tests across 14 suites)
- Failures: 0, Errors: 0, Skipped: 0

Full test evidence and per-finding documentation: [docs/implementation/phase-15/test-evidence.md](../phase-15/test-evidence.md).
Resubmission entered review as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

## Codex remediation

The subsequent [closure audit](../phase-15/reviews/2026-10-07-phase-15-closure-backend-audit.md) (2026-10-07)
returns **REMEDIATION_REQUIRED**: 13 original BA15 findings CLOSED and four Medium remnants OPEN below.
This same handoff is **CHANGES_REQUESTED**. FR15-9 is CLOSED; it is not one of these four remnants.
The prior Codex [final acceptance](../phase-15/reviews/2026-10-07-phase-15-final-codex-acceptance.md) remains
historical **READY FOR OWNER COMMIT — DO NOT COMMIT YET**; its specific accepted repairs/tests are preserved.
FR15-1–FR15-6 retain their specific closures; FR15-7 and FR15-8's blocking Medium requirements are satisfied.
The [first final review](../phase-15/reviews/2026-10-07-phase-15-final-codex-review.md) and
[first re-review](../phase-15/reviews/2026-10-07-phase-15-final-codex-rereview.md) and
[re-review 2](../phase-15/reviews/2026-10-07-phase-15-final-codex-rereview-2.md) and
[re-review 3](../phase-15/reviews/2026-10-07-phase-15-final-codex-rereview-3.md) and
[re-review 4](../phase-15/reviews/2026-10-07-phase-15-final-codex-rereview-4.md) remain historical.
Retain this same handoff and the original owner boundaries; do not reimplement the six resolved corrections.
Preserve previously accepted restart/binary/FK/lifecycle checks and the newly accepted DTO cases, actual competing
Account/Study writes in both winner directions, Future outcomes, persisted-state checks, monetary create/update
width/scale/min/max/reloads, and complete contiguous 100/101-item HTTP inspection.
The new related-drift matrix/update counterparts, generated Collection/Finance/Knowledge/Settings semantics
and strict Import query bounds are accepted, including both new 1000-character Finance description PUT controls.
Retain corrected auth/error/import/startup/guard notes, evidence field/count/symbol/value corrections and the honest
no-captured-pre-fix-runtime statements. Original executed probe/SQL evidence remains in the linked initial report.
Those specific final-review corrections remain accepted. The repository-wide closure audit establishes the
additional bounded remnants below; do not reimplement closed findings or rewrite historical review records.

| Review finding | Disposition / bounded remaining acceptance |
| --- | --- |
| FR15-1–FR15-6 | CLOSED for the specific defects: fresh locked Account state/no fallback, authoritative Feed scheduling, canonical 406/OpenAPI, correct Finance/Collection descriptions, Feed conversion parity and known-zero provider framing. Preserve these repairs and their useful tests. |
| FR15-7 / original BA15 acceptance | CLOSED — both Finance description PUTs now accept 1000 and assert the full response value; blank/null and 1001 controls remain. Other named matrix/update/schema, strict Import bounds and previously accepted regressions are preserved. |
| FR15-8 / evidence/docs | Blocking Medium requirements CLOSED — fieldErrors/binary exceptions and actual synchronous service/SPI/exception/error mapping now match source; pre-fix source-derived cases are honestly disclosed, original captured evidence stays linked, and deadlock claims are scoped. 1012 total / 92 audit cases / 14 suites independently confirmed. |
| FR15-9 / docs symbol | CLOSED — canonical note line 34 and current handoff identify the actual PortabilityController (existing POST /api/v1/portability/exports). Documentation only; no production rename or historical-report rewrite. |

### Historical closure audit — remaining implementation acceptance at that gate

Only BA15-2, BA15-9, BA15-14 and BA15-15 remain actionable. Exact paths, runtime/generated-schema evidence and
regression requirements are in the linked closure report. Original owner decisions and mandatory stops still apply.

| Finding / severity | Bounded remaining correction | Required acceptance |
| --- | --- | --- |
| BA15-2 / Medium | Match existing owner rules for Location name500/phone64, snapshot filename/display500, optional/default relationship statuses and uncapped owner note, optional Feed URL, Personal email max320 without new syntax policy and blank nationality, and Shopping/Software/Feed-conversion currency null/blank/trim semantics. Correct raw-length rejection of already owner-normalized Shopping/Software names/currency only; preserve raw-authoritative fields and Markdown. | Named POST/PUT true maximum/max+1, null/default/blank/padded controls, persisted normalized values and actual generated contract assertions listed in the report. Keep genuine required-price currency/reference rules and all accepted matrix cases; no generic validation framework/domain weakening/new note cap. |
| BA15-9 / Medium | `BusinessHoursService.getSchedule`: obtain fresh parent state under the existing share lock before combining the flag with intervals, including a prior managed Location read. | Deterministic PostgreSQL unknown→known and known→unknown reader contention with actual lock waits and consumed Futures; coherent flag/interval/scalar result and fresh final read. Preserve five accepted writer cases; no schema/version/generic-lock change. |
| BA15-14 / Medium | Owner-local safe long-offset guard or bounded out-of-window result before unsupported JPA pagination; impossible accepted page values must not return500. Document the actual effective request bound. | Extreme page plus supported-offset threshold/threshold+1 at multiple limits, canonical client rejection or bounded empty result/no writes. Preserve complete ordered 100/101-item traversal, all decisions and atomic execution; no total ingestion cap/broad exception mapping. |
| BA15-15 / Medium | Documentation/OpenAPI only: describe recurring rule ledger cardinality/sign/distinct-wallet/category rules on create/update/entry schemas; structurally mark Software type required in both request schemas. | Generated recurring property descriptions and Software schema.required/type enum/no default assertions. Preserve existing runtime rejection/status semantics; do not add runtime validation solely for metadata or invent Transfer sum-zero. Retain accepted Search/transaction/frequency/Collection descriptions. |

The closure full build/coverage passed **1012 tests**, zero failures/errors/skips (04:24); architecture and
storage/HTTP integration passed. Dependency/static reports were freshly generated and triaged: SpotBugs239,
PMD57, CPD70; they are not zero-warning claims or extra remediation scope. Retain this evidence and accepted
regressions; 1012 is the current verified total, not a fixed expected total after new tests.
The null-weekday source hypothesis was disproved by canonical HTTP400; no repair is requested for it.

### Antigravity 2026-10-08 submission — claims reviewed below, not acceptance

The following is the latest resubmitted narrative, not Codex acceptance. It supersedes earlier Oct8 submission
claims. The current re-review below accepts source repairs but retains narrow test/evidence corrections.
Antigravity submitted repairs for the four authorized closure audit Medium remnants (BA15-2, BA15-9, BA15-14, BA15-15) and final-review blockers (FR15-10, FR15-11, FR15-12) on 2026-10-08:
- **BA15-2 (Owning-Domain HTTP DTO Validation & Normalization — including FR15-10 & FR15-11):**
  - Location: Added `@Size(max = 500)` to `name` and `@Size(max = 64)` to `phone` in `CreateLocationRequest` and `UpdateLocationRequest`, with compact constructor whitespace trimming for name and `phone = (phone == null || phone.isBlank()) ? null : phone.trim()`, matching `LocationService.trimIfPresent` check order (normalizing Unicode and ASCII blanks to null before length checks).
  - Account Follower Snapshots: Added `@Size(max = 500)` to `importedFileName` in `CreateFollowerSnapshotRequest` and to `displayNameSnapshot` in `CreateFollowerSnapshotEntryRequest`, with `trimOrNull` normalization matching `FollowerSnapshotService` (retaining historical values like `\u2003` and preserving conflicting duplicate checks).
  - Account Relationships: In `SetExternalAccountRelationshipRequest`, made `followerStatus` and `followStatus` optional with `@Schema` descriptions, mapped null `followStatus` to default `UNKNOWN` in owning service and `source` to `MANUAL` while `followerStatus` remains nullable, uncapped `note` (>2048 chars accepted and stored without arbitrary validation bounds), and normalized `note` matching `ExternalAccountRelationshipService.trimOrNull` (retaining `\u2003`).
  - Feed Sources: In `CreateFeedSourceRequest` and `UpdateFeedSourceRequest`, made `feedUrl` optional and normalized blank URLs to null in compact constructors matching `FeedSourceService.validateCommon` check order (preserving raw length check before normalization).
  - Personal Profiles: In `CreatePersonalProfileRequest` and `UpdatePersonalProfileRequest`, relaxed `email` from `@Email` to free-form `@Size(max = 320)`, and normalized blank `nationalityCode` to null.
  - Currency Code & Name Trimming: In `CreateShoppingItemRequest`, `UpdateShoppingItemRequest`, `CreateSoftwareItemRequest`, `UpdateSoftwareItemRequest` (all in `com.vhvkhangg.personalprivatevault.collection.internal.web.dto`), and `ConvertToStudyRequest`, constrained `currencyCode` with `@Size(max = 3)` and normalized blank values using `trimOrNull` matching owning services (retaining unknown Unicode currencies to be rejected with 422 by domain validation, and requiring currency when price is present); added compact constructor whitespace trimming to Shopping and Software names and Study titles.
- **BA15-9 (Location Schedule Reader State Refresh Under Share Lock):**
  - In `BusinessHoursService.getSchedule`, added explicit `entityManager.refresh(location)` under the acquired share lock (`findByIdForShare`) before evaluating `location.isBusinessHoursKnown()` and returning the schedule, guaranteeing fresh state after any prior managed read in the same transaction.
  - Proved with 2 deterministic PostgreSQL contention tests in `LocationConcurrencyIntegrationTest`: `priorManagedReaderDuringUnknownToKnownWriterContentionSerializesCoherentState` and `priorManagedReaderDuringKnownToUnknownWriterContentionSerializesCoherentState` using actual lock waits and consumed Futures.
- **BA15-14 (Safe Offset Overflow Guard & Effective Request Bound Documentation):**
  - In `ImportJobService.findJobItems`, added an owner-local safe offset overflow guard: if `(long) page * (long) limit > Integer.MAX_VALUE`, immediately returns an empty list `List.of()` without executing unsupported JPA queries or writing data.
  - Documented effective pagination query parameter bounds on `ImportJobController.findItems` using OpenAPI `@Parameter(description = ...)`.
  - Proved with `extremePageAndOffsetThresholdBoundariesReturnBoundedEmptyWithout500OrWrites` in `ImportJobPaginationIntegrationTest` testing extreme page values (`Integer.MAX_VALUE`, and boundary `Integer.MAX_VALUE / limit`) across multiple limits returning HTTP 200 with bounded empty data list.
- **BA15-15 (Recurring Ledger & Software Required-Type OpenAPI Documentation — including FR15-11):**
  - In `CreateRecurringTransactionRuleRequest` and `UpdateRecurringTransactionRuleRequest`, added full OpenAPI `@Schema` descriptions for `transactionType` (cardinality, distinct wallets, delta sign matches type), `categoryId` (optional for INCOME/EXPENSE, null for TRANSFER), and `entries`.
  - In `RecurringRuleEntryRequest`, added OpenAPI `@Schema` descriptions for `walletId` and `amountDelta`.
  - In `CreateSoftwareItemRequest` and `UpdateSoftwareItemRequest`, added `@Schema(requiredMode = Schema.RequiredMode.REQUIRED)` on `type`, and verified schema asserts enum values (`APPLICATION`, `EXTENSION`) and has no default.

Full verification command executed:
`mvn -ntp clean verify`
- Result: **BUILD SUCCESS**
- Preserved baseline: 920 tests
- Total tests run: 1021 tests (101 net new focused audit regression tests across 14 suites; 9 net new tests added in this pass)
- Failures: 0, Errors: 0, Skipped: 0

Full test evidence and per-finding documentation updated in: [docs/implementation/phase-15/test-evidence.md](../phase-15/test-evidence.md).
Submission entered review as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
Codex remains review-only. **Do not commit, archive or reset this handoff before BACKEND_AUDIT_READY.**

## Historical first Oct8 Codex final review

**CHANGES_REQUESTED** — [formal review](../phase-15/reviews/2026-10-08-phase-15-final-codex-review.md).
Independent clean verification passed **1021 tests**, zero failures/errors/skips (03:47); 101 audit cases in
14 suites. The green build does not close the remaining owner-contract, regression and evidence gaps.
The audit gate remains REMEDIATION_REQUIRED; this review does not replace the dedicated closure backend audit.

Accepted: BA15-9's fresh schedule-reader state under `findByIdForShare` with both real PostgreSQL reader races;
BA15-14's long-offset guard and actual int-page/threshold cases; BA15-15's recurring ledger descriptions and
Software required-type metadata; useful BA15-2 length/null/default/ASCII-normalization/schema cases. Preserve
these repairs and all previously accepted tests. FR15-1–FR15-9 keep their specific closures; no second handoff.

Java paths below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; audit test paths
are relative to `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/`.

| Finding / severity | Bounded correction | Required acceptance |
| --- | --- | --- |
| FR15-10 / Medium (BA15-2) | Match existing owner normalization and check order in Account snapshot filename/display/relationship note, Collection Shopping/Software create/update currency, Feed ConvertToStudy currency and Feed source create/update URL, and Location create/update phone constructors. `isBlank` is not interchangeable with trim/empty; preserve owner-retained Unicode historical values and unknown currency rejection, Feed pre-normalization length bounds and Location blank-before-length semantics. Do not change domain rules or use a generic framework/global blank-policy rewrite. | Raw JSON create/update Unicode controls: three EM SPACEs as unknown currency without price rejected/no write; 2049 EM SPACE URL rejected; 65 EM SPACE phone accepted as null; exact historical filename/display/note reads and conflicting duplicate snapshot rollback. Preserve existing ASCII/null/padded bounds, required-price currency and reference checks. Diagnostic proof and exact 12 DTOs are in the formal review. |
| FR15-11 / Medium (BA15-2/15) | Complete explicitly required Shopping/Software **PUT** currency/null/blank/space/padded controls and raw padded name normalized maximum/max+1 on create/update. Assert converted Study title/currency and snapshot display through existing owning public/HTTP reads. Assert actual Software type enum/no default for both generated request schemas. | Update acceptance/rejection and no-write controls with persisted normalization/full replacement; preserve plain500/501 cases. Schema enum exactly APPLICATION/EXTENSION, no default, still required. No new runtime NotNull, cross-module JPA access or unrelated matrix expansion. |
| FR15-12 / Medium (current evidence/docs) | Reconcile current test-evidence and submission claims with actual code/results: nullable followerStatus, owner-defaulted followStatus UNKNOWN/source MANUAL (not mapper/plural default); actual int-page thresholds, not an unexecuted Long.MAX_VALUE200 claim; actual collection.internal.web.dto package, test names and findByIdForShare symbol; correct phone/normalization descriptions after FR15-10. | Accurate current narratives/display names/counts and commands matching source, generated schemas and executed cases. Preserve historical reports, captured original evidence and source-derived/runtime distinctions; do not alter behavior to match incorrect prose. |

Only these three final-review blockers remain actionable in this pass. All original owner decisions and stops
remain unchanged, including BA15-13 architecture expansion and BA15-14 additional total ingestion limits.
Antigravity owns production-boundary/test/evidence repairs. Codex made no production/test/POM changes.
Run focused regressions and the required full clean verification; update actual evidence and resubmit this same
handoff as IMPLEMENTED_AWAITING_CODEX_REVIEW. After final-review acceptance, rerun `$codex-backend-audit` for
repository-wide closure. **Do not commit, archive or reset before BACKEND_AUDIT_READY.**

## Historical handoff-specific final review

**READY FOR OWNER COMMIT — DO NOT COMMIT YET** at that review (2026-10-07):
[formal acceptance](../phase-15/reviews/2026-10-07-phase-15-final-codex-acceptance.md).
Independent `mvn -ntp -l ../phase-15-final-rereview-5-verify.log clean verify` passed **1012 tests**, zero
failures/errors/skips (03:53 min); 92 audit cases across 14 suites. All blocking final-review requirements are
satisfied for that review; FR15-9 was a Low documentation-only closure follow-up, now closed by the closure audit.
Earlier reports/evidence remain preserved. The current audit verdict/remnants above supersede this historical next gate.

## Previous Codex final re-review — 2026-10-08 (historical gate)

**CHANGES_REQUESTED — TEST/EVIDENCE ONLY**:
[formal re-review](../phase-15/reviews/2026-10-08-phase-15-final-codex-rereview.md).
Independent clean verification passed **1021 tests**, zero failures/errors/skips (04:27); 101 audit cases in
14 suites. This is not a new repository-wide backend-audit verdict. Audit gate remains REMEDIATION_REQUIRED.

FR15-10's **functional normalization defect is CLOSED**: the named 12 DTOs now follow exact existing owner
trim/empty versus blank-before-length and Feed validation/storage ordering. Preserve these source repairs;
no further production repair is requested. The new PUT branches, GET persistence checks, converted Study public
title/currency reads and Software required/enum/no-default schema assertions are accepted and must be retained.
FR15-1–FR15-9 and accepted BA15-9/14/15 source corrections keep their specific acceptance; no reimplementation.

| Current finding / severity | Remaining test/evidence-only correction | Acceptance |
| --- | --- | --- |
| FR15-11 / Medium | In the existing WebDtoValidationAuditIntegrationTest normalization methods, submit raw JSON/maps/JsonNode instead of serializing already-normalized production request DTOs. Seed a nonnull currency before successful null/one-space/ASCII-blank PUT clearing. Add actual no-write/atomic failure-state assertions instead of response-only claims. | Actual padded506/507 identities and Unicode phone/URL/currency/historical values reach the HTTP binding path unchanged. Shopping/Software null/space/blank without price clears an existing valid currency on a fresh read; price-required/unknown-currency rejection leaves prior name/price/currency unchanged. Rejected POST/conversion leaves owned/Vault/Study/provenance counts unchanged, with baselines after fixture preparation. Conflicting snapshot copies add no header/entries; matching duplicates retain exactly one entry and exact historical display. Preserve all accepted assertions/schema controls; no unrelated matrix or production change. |
| FR15-12 / Low | Substantial default/package/offset inaccuracies are corrected. Fix only current evidence line46 stale92 count, three nonexistent method names at113/116/117 and the Feed normalization/check-order description at76 (and matching submission prose). | Actual101 audit count, exact current method symbols, DTO trim/empty then BeanValidation/owner trimmed-length then owner Unicode-blank-to-null sequence, and claims matching FR15-11's executed state assertions. Preserve every historical formal report and original source-derived/runtime distinction; no behavior change to match prose. |

For FR15-11, modify only the named existing regression methods/fixtures and evidence; retain accepted source and
all prior tests. Focused command from backend: `mvn -ntp -Dtest=WebDtoValidationAuditIntegrationTest test`, followed
by required `mvn -ntp clean verify`. Record actual results and resubmit this same handoff as
IMPLEMENTED_AWAITING_CODEX_REVIEW. No further production work is authorized by this test-only return; if tests
expose a genuine defect, report it/return to the bounded implementation workflow, and stop for owner decision on expansion.
All original owner stops remain. Do not commit, archive or reset before the dedicated closure audit returns BACKEND_AUDIT_READY.

### Antigravity 2026-10-08 test slice resubmission — claims reviewed below, not acceptance

The following is the submitted narrative for the test/evidence slice, not Codex acceptance. Zero production code changes were made; all repairs were strictly confined to `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/WebDtoValidationAuditIntegrationTest.java` and `docs/implementation/phase-15/test-evidence.md`:
- **FR15-11 (Raw JSON Wire Testing, Real Replacement Clearing & Atomic No-Write Assertions):**
  - Converted methods 15 through 20 (`locationNameAndPhoneCreateAndUpdateBoundaries`, `followerSnapshotFileNameAndDisplayNameBoundaries`, `accountRelationshipOptionalDefaultsAndUncappedNoteBoundaries`, `feedSourceOptionalUrlBoundaries`, `personalProfileFreeFormEmailAndNationalityBoundaries`, `shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity`) to submit raw JSON maps directly via MockMvc, ensuring Jackson deserialization and compact constructor normalization receive un-trimmed inputs on the wire.
  - In `shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity`: seeded non-null valid currency (`"VND"`) on `shopId` and `softId`, then verified real non-null → null replacement clearing using subsequent PUTs with `null`, `" "` (1 ASCII space), and `"   "` (multiple ASCII spaces) without price, asserting fresh HTTP GET confirms `currencyCode` is cleared to `null`.
  - Added atomic failure-state and no-write assertions:
    - Rejected negative PUTs (price without currency, unknown Unicode currency, 501-character name) verify via fresh HTTP GET and database queries that prior name, price, and currency remain unchanged.
    - Rejected creates/conversions verify table counts (`shopping_items`, `software_items`, `study_items`, `vault_entries`, `saved_resource_conversions`, `feed_sources`, `personal_profiles`, `locations`) remain unchanged before and after.
    - Conflicting follower snapshot copies (422 `INVALID_SNAPSHOT`) verify `follower_snapshots` and `follower_snapshot_entries` table counts remain unchanged; matching duplicate snapshot copies (201 Created) assert fresh GET entries returns length 1 with exact retained display `\u2003`.
- **FR15-12 (Evidence File Precision Corrections):**
  - Updated `docs/implementation/phase-15/test-evidence.md` line 46 from "92 regression tests" to "101 regression tests".
  - Updated Feed source sequence description in line 76: DTO `trimOrNull` normalization precedes Bean Validation `@Size(max = 2048)`, and retained Unicode reaches the owner's trimmed-length check before `isBlank`/null storage normalization.
  - Updated method names at lines 113, 116, 117 to exact symbols (`locationNameAndPhoneCreateAndUpdateBoundaries`, `feedSourceOptionalUrlBoundaries`, `personalProfileFreeFormEmailAndNationalityBoundaries`).
  - Documented newly executed raw wire, nonnull-to-null clearing, and atomic no-write / table count assertions.

Full verification commands executed:
1. Focused test command:
   `mvn -ntp test -Dtest=WebDtoValidationAuditIntegrationTest`
   - Result: **BUILD SUCCESS**
   - Total tests run: 25 tests, Failures: 0, Errors: 0, Skipped: 0
2. Clean verification command:
   `mvn -ntp clean verify`
   - Result: **BUILD SUCCESS** (Time elapsed: 04:03 min)
   - Preserved baseline: 920 tests
   - Total tests run: **1021 tests** (101 net new focused audit regression tests across 14 suites)
   - Failures: 0, Errors: 0, Skipped: 0

Next step at submission: Codex `$codex-final-review`.

## Previous Codex final re-review 2 — 2026-10-08 (historical gate)

**CHANGES_REQUESTED — TEST/EVIDENCE ONLY**:
[formal re-review 2](../phase-15/reviews/2026-10-08-phase-15-final-codex-rereview-2.md).
Independent full clean verification passed **1021 tests**, zero failures/errors/skips (04:00), including
25 Web DTO cases and 101 audit cases across 14 suites. Audit gate remains REMEDIATION_REQUIRED, not a new audit verdict.

Preserve accepted production repairs and all accepted regression controls: raw normalization payloads,
independently re-seeded Shopping/Software null/one-space/three-space currency clearing, fresh GET/database
unchanged name/price/currency after rejected PUTs, snapshot conflict no-write/matching duplicate cardinality,
and conversion Study/Vault/provenance count assertions. FR15-10 functional repair remains CLOSED. Previously
accepted Software schema/reader/offset/ledger controls and FR15-1–FR15-9 closures remain accepted.

| Current finding / severity | Only remaining correction | Acceptance |
| --- | --- | --- |
| FR15-11 / Medium | Add Vault count assertions to eight existing rejected POSTs in WebDtoValidationAuditIntegrationTest: Location name501/phone65; Shopping name501/unknown Unicode currency/price with blank currency; Software same three. | Capture `vault_entries` count after fixture preparation immediately before each failed request, assert unchanged afterward alongside existing owning count. No new test matrix, PUT count requirement, production change or generic framework. |
| FR15-12 / Low | Correct current test-evidence line118's claim of table counts on all rejected operations; line115's relationship case sends explicit nulls, not omitted keys. | Describe owning+Vault counts for rejected creates, prior-field checks for collection PUTs and Study/Vault/provenance counts for conversion failures exactly as executed. Preserve historical records and accepted count/method/Feed-order corrections. |

Antigravity `/antigravity-test-slice` only for these existing assertions/evidence corrections. Run focused
`mvn -ntp -Dtest=WebDtoValidationAuditIntegrationTest test`, then required `mvn -ntp clean verify`; record actual
results and resubmit this same handoff as IMPLEMENTED_AWAITING_CODEX_REVIEW. No further production repair is
requested; genuine source defects return to the bounded workflow and expansion stops for owner decision.
Do not create another handoff, commit, archive or reset. Later acceptance still requires the dedicated closure
`$codex-backend-audit`; only BACKEND_AUDIT_READY permits owner commit/push.

### Antigravity 2026-10-08 test slice resubmission 2 — claims reviewed below, not acceptance

The following is the submitted narrative for the second test/evidence slice, not Codex acceptance. Zero production code changes were made; all repairs were strictly confined to `backend/src/test/java/com/vhvkhangg/personalprivatevault/audit/WebDtoValidationAuditIntegrationTest.java` and `docs/implementation/phase-15/test-evidence.md`:
- **FR15-11 (Vault No-Write Checks for the Eight Existing Rejected Creates):**
  - Added baseline capture and equality assertions for `vault_entries` count on the eight existing rejected POST branches in `WebDtoValidationAuditIntegrationTest.java`:
    1. Location padded name501 POST (`locationNameAndPhoneCreateAndUpdateBoundaries`)
    2. Location phone65 POST (`locationNameAndPhoneCreateAndUpdateBoundaries`)
    3. Shopping padded name501 POST (`shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity`)
    4. Shopping unknown Unicode currency POST (`shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity`)
    5. Shopping price with blank currency POST (`shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity`)
    6. Software padded name501 POST (`shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity`)
    7. Software unknown Unicode currency POST (`shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity`)
    8. Software price with blank currency POST (`shoppingSoftwareAndFeedConversionCurrencyAndPaddedIdentity`)
  - Each branch captures `vault_entries` count immediately before the rejected POST and asserts it is unchanged afterward alongside the owning table count.
- **FR15-12 (Evidence File Precision Corrections):**
  - Updated `docs/implementation/phase-15/test-evidence.md` line 115 to state explicit nulls rather than omitted keys for `followerStatus`, `followStatus`, and `source`.
  - Updated line 118 with precise per-path evidence: rejected Location and collection (Shopping, Software) creates leave their owning and `vault_entries` counts unchanged; collection PUT failures preserve the three captured fields (name, price, currency); conversion failures leave `study_items`, `vault_entries`, and `saved_resource_conversions` counts unchanged.

Full verification commands executed:
1. Focused test command:
   `mvn -ntp test -Dtest=WebDtoValidationAuditIntegrationTest`
   - Result: **BUILD SUCCESS** (Time elapsed: 43.99 s)
   - Total tests run: 25 tests, Failures: 0, Errors: 0, Skipped: 0
2. Clean verification command:
   `mvn -ntp clean verify`
   - Result: **BUILD SUCCESS** (Time elapsed: 04:11 min)
   - Preserved baseline: 920 tests
   - Total tests run: **1021 tests** (101 net new focused audit regression tests across 14 suites)
   - Failures: 0, Errors: 0, Skipped: 0

Next step at submission 2: Codex `$codex-final-review`.

## Current Codex final acceptance — 2026-10-08

**READY FOR OWNER COMMIT — DO NOT COMMIT YET**:
[formal acceptance](../phase-15/reviews/2026-10-08-phase-15-final-codex-acceptance.md).
FR15-11 Medium and FR15-12 Low are CLOSED; FR15-10 and all prior accepted repairs remain accepted.
No blocking final-review findings remain. The eight missing Vault checks are now present on the existing
Location/Shopping/Software rejection branches; current evidence accurately distinguishes create counts,
update-field state and conversion counts, and explicit-null relationship inputs.

Independent full clean verification passed **1021 tests**, zero failures/errors/skips (03:53), including
25 Web DTO cases and 101 audit cases across 14 suites. Production/resources/POM matches the prior accepted
review; only the sixteen requested test-count lines were added, with all other test content unchanged.
Codex made no source/test/POM changes. Preserve owner decisions, historical reports and all prior evidence.
Earlier return tables/submission next steps are historical, not current actionable remediation.

The single evolving handoff remains active; audit gate is still **REMEDIATION_REQUIRED** pending dedicated
repository-wide closure. This final review does not independently close the four BA15 audit dispositions or
claim a fresh static/coverage/closure audit. No second handoff, archive/reset, broader authority or commit/push.
BA15-13 expansion, BA15-14 new total limits and all original owner stops remain unchanged.

Future owner commit message, **only after BACKEND_AUDIT_READY**:

```text
fix(backend): remediate phase 15 audit findings
```

Next step: `$codex-backend-audit` for repository-wide closure. **Do not commit/push yet**; only BACKEND_AUDIT_READY
permits owner implementation commit. Phase 15 is not complete/frozen; Phase 16/17 remain deferred.
