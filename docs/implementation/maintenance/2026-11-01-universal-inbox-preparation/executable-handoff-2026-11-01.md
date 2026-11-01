# Executable slice handoff — Universal Inbox browsing

- **Handoff ID:** `UI26-INBOX-BROWSE`
- **Created:** 2026-11-01 by Codex
- **Status:** `READY_FOR_OWNER_COMMIT`
- **Scope:** One additive, reuse-only Feed browsing slice extending frozen Phase 10/13 capabilities; no numbered phase is reopened.
- **Authority:** Owner's current `$codex-create-handoff` invocation, following publication of the [reviewed preparation](README.md) at `a4f89ddcfa750548e28034c4df78ca0725f85a9b`. This selects the reviewed `KEEP_REUSE_ONLY` default; it grants no read/archive or schema amendment.
- **Entry baseline:** Local `main`, `origin/main` and live remote `refs/heads/main` verified at `a4f89ddcfa750548e28034c4df78ca0725f85a9b`; working tree clean before handoff creation.
- **Implementer:** Antigravity `/antigravity-implement-handoff`, using existing `backend-implementer`; mandatory self-review.
- **Independent reviewer:** Codex `$codex-final-review` after implementation/self-review.
- **Execution:** `INDEPENDENTLY_ACCEPTED / PENDING_OWNER_PUBLICATION` in [independent re-review 2](reviews/2026-11-01-independent-re-review-2.md). All MEDIUM findings UI26-FR-01–04 CLOSED; UI26-FR-L02 remains LOW / OPEN / NONBLOCKING. Supplied 139-test evidence verified; 18 fresh selected reviewer tests and one strict schema probe passed.
- **Remediation:** `2/3` correction rounds consumed; accepted and stopped. Unused third round is not transferable; takeover not authorized.

## Goal and sources of truth

The authenticated Owner can browse active saved links in descending saved-time order, optionally filter by an existing kind, and continue through pages without losing eligible links behind trashed candidates. Existing saving, detail/hash/recent reads, metadata/lifecycle actions, search and explicit Knowledge conversions retain their accepted behavior.

Read the [Owner scope](owner-scope-and-decisions.md), [independent READY_FOR_HANDOFF review](reviews/2026-11-01-independent-pre-handoff-review.md), [design inventory](source-reuse-and-design.md) and [UI-01–10 matrix](acceptance-and-review-plan.md). The independent review resolves their prospective questions: `KEEP_REUSE_ONLY`, existing Vault batch qualification, structural cursor validation and unchanged legacy trash semantics. Earlier preparation-only labels remain dated preparation facts; this issued handoff now controls execution.

Applicable contracts:

- [Owner policy](../../../workflow/current-owner-delivery-policy.md), [Docs Sync contract](../../../workflow/documentation-synchronization.md), [ACTIVE](../../handoffs/ACTIVE.md) and [handoff template](../../../templates/handoff.md).
- [Root instructions](../../../../AGENTS.md), [backend instructions](../../../../backend/AGENTS.md), [Feed instructions](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/AGENTS.md), [test instructions](../../../../backend/src/test/AGENTS.md) and [docs instructions](../../../AGENTS.md).
- [Phase 10 contract](../../phase-10/README.md), [Phase 13 contract](../../phase-13/README.md), [ADR-0016](../../../adr/0016-root-http-contract-module-local-adapters.md), [API architecture](../../../architecture/api-architecture.md), [module matrix](../../../architecture/module-dependency-matrix.md), [package tree](../../../repository/repository-package-tree.md) and [frozen DBML](../../../database/personal-private-vault-schema-v1-FROZEN-final.dbml).

The scoped Feed instructions' historical phase gates are satisfied only by this separately approved additive maintenance exception for the targets below. Other frozen behavior, database/module/diagram baselines and dependency directions remain unchanged. CSA26 and S19-2 are not reopened.

## Simplicity decision and burden

**KEEP / REUSE:** `SavedResourceOperations` capture/read methods, `SavedResourceService.toView`, `FeedWebMapper.toResponse`, Vault identity/metadata/lifecycle, parent-Knowledge conversions and global Feed search. Reuse `VaultSearchOperations.filterQualifyingActiveEntries(candidateIds, Set.of())`; Feed already permits `vault::search`. No Vault implementation change is needed.

**ADD:** One owner-local read, bounded keyset candidate query, small cursor codec, immutable page view/HTTP DTO, one route and focused tests. Existing application/repository/web roles suffice. Work is concentrated in these Java extensions and database/web verification; no measured duration is claimed.

**SIMPLIFY / DEFER:** No count, shared pagination engine, signing key, cursor session, metadata enrichment, new module, index or migration. Read/unread, archive, title/notes editing, metadata badges and favorite/tag filters are deferred. A scan/sort on the finite fixture alone does not justify a migration or benchmark subsystem.

## User-visible acceptance — MUST

| ID | Scenario / expected observable result | Minimal proof and reuse |
| --- | --- | --- |
| UI-01 | Manual/Feed-item saves create one SavedResource sharing its Vault ID. The trimmed stored URL's SHA-256 UTF-8 hash remains unique. Sequential/concurrent duplicates retain stable conflict and losing-transaction rollback with no orphan. A trashed duplicate still conflicts; no resurrection/merge. | Existing `FeedIntegrationTest` saves/duplicate/concurrency cases plus a trashed-duplicate compatibility case; reuse `saveManual` / `saveFeedItem`. |
| UI-02 | Existing recent-list, ID/hash and conversion/history APIs retain shapes, status codes and semantics. ID/hash/recent reads can still return trashed resources. Study/Information/Note conversions remain explicit and atomic. | Existing ID/hash/recent/conversion tests, including `conversionFailureRollback`; `FeedWebIntegrationTest` plus targeted legacy trashed-read assertions. No Vocabulary conversion. |
| UI-03 | `GET /api/v1/saved-resources/inbox` returns a bounded page, default 50 / maximum 100, with optional existing kind, strict `(saved_at DESC, id DESC)` order and truthful continuation. A stable dataset has no duplicate/skip, including timestamp ties. | New PostgreSQL paging and HTTP proof below; reuse current views/mapping. Empty page: `items: []`, `nextCursor: null`. |
| UI-04 | Only Vault-qualified active candidates enter the Inbox. Multiple full batches of leading/interleaved trash cannot cause false exhaustion or hide a later eligible lookahead. Restore re-enables the original saved-time position; a missing qualification result is excluded. | Real PostgreSQL trash/restore and committed-trash-before-qualification interleaving. Use a small isolated missing-result negative where FKs prohibit a missing Vault row. Reuse the public batch qualifier. |
| UI-05 | Existing Vault favorite/tag actions work on saved-resource IDs without duplicate state or changed Inbox order. Conversion/provenance rollback stays intact. | Exercise one favorite and tag attach/detach path via existing public operations/HTTP; reuse the existing conversion failure proof. |
| UI-06 | Bearer authentication is required. URLs/titles/metadata remain untrusted JSON data; capture/listing never fetches, redirects or executes content. Cursor/validation errors never echo source data, cursor contents, credentials or SQL. Existing manual URL acceptance stays intact. | Missing/invalid Bearer 401, representative untrusted-string round trip, safe errors/logs and source inspection. Include an existing-compatible non-HTTP-scheme save; no link opening. |
| UI-07 | Feed owns the read/persistence; cross-owner access uses existing public named interfaces. Explicit HTTP DTOs and shared envelope/advice remain. No schema, migration, module allowlist, dependency or auth change. | `ApplicationArchitectureTests`, changed subtree/import review, PostgreSQL query/batching observations and unchanged frozen-target diff. |
| UI-08 | Exact safe Java selectors preserve CSA26 archival guards. No archived QA, inference, worker launch or runtime download. | Bounded receipts and source-applicable `RagQaArchiveGuardTest` registration/MVC checks only; no archived opt-in. |
| UI-09 | OpenAPI/current API docs describe only delivered behavior; all existing operation contracts stay exact and package ownership stays accurate. | Exact route inventory, page/item/cursor/schema/security assertions, local links, changed-file inventory and Docs Sync Check. Immutable Phase 10/13 history untouched. |
| UI-10 | Antigravity self-reviews and records actual evidence/limits before independent Codex review. Owner alone stages/publishes after acceptance. | Handback maps UI-01–10 to tests/receipts, findings, counter and next action; no fabricated PASS/publication. |

### HTTP and cursor contract

- Add only `GET /api/v1/saved-resources/inbox`, operation ID `findSavedResourceInbox`. Success: **200**, `ApiResponse<SavedResourceInboxPageResponse>`, `data.items: SavedResourceResponse[]`, `data.nextCursor: string|null`, `error: null`, `meta: null`. Retain current item fields/defensive JSON snapshots; no totals, offset, synthetic `hasMore` or per-item metadata enrichment.
- `limit`: existing default 50, range 1–100. Zero/negative/101: **400 / VALIDATION_ERROR**. Nonnumeric/overflow or invalid enum binding: **400 / MALFORMED_REQUEST**. Reuse current Bean Validation/binding advice. The new application read also enforces 1–100 for non-HTTP callers.
- Optional `kind`: omission/empty normalizes to all kinds; ARTICLE, REPOSITORY, SOCIAL_POST, WEB_PAGE, OTHER use ordinary existing enum trimming/case semantics. No global converter. Apply the kind predicate in the Feed query before qualification.
- Omitted `cursor` means first page; supplied empty/blank is invalid. Cap encoded length at **512 characters before decoding**. Use a versioned Base64url token containing database-read UTC saved-time, positive signed-64-bit ID and normalized kind/all-kinds marker. Use JDK parsing. Private field layout is an implementation detail recorded in the handback and covered by round-trip tests. Preserve existing PostgreSQL timestamp precision. Validate a supported SQL timestamp range before binding; Java-valid but SQL-out-of-range values must not accidentally become 500.
- Invalid encoding, missing/extra/invalid fields, unsupported version, nonpositive/overflow ID, invalid/out-of-range timestamp and filter mismatch: **400 / INVALID_ARGUMENT**, null data, existing safe message. Normalize codec parsing failures to the existing `IllegalArgumentException` path. Do not change root advice, use Feed's business-rule 422 exception, add an advice/exception hierarchy or misclassify unrelated database failures as cursor errors.
- Cursor is an owner-local seek position: a structurally valid edited tuple remains subject to the same authenticated query/qualification; no cryptographic tamper-detection claim. Limit may change; normalized kind must match. Continuation must not require the anchor row to exist or remain active.
- Example: 101 eligible rows, limit 100 → 100 items/non-null cursor, then one item/null cursor. Exactly 100 eligible rows → null cursor. Empty result: `{"data":{"items":[],"nextCursor":null},"error":null,"meta":null}`. ARTICLE cursor reused for OTHER or all kinds → safe 400.
- This is a **live list**. Trash committed before batch qualification is excluded; later trash/restore/insertion can affect subsequent reads. Newer saves above a cursor require restarting at page one. No cross-request snapshot, reservation, write lock or network-send-time visibility guarantee.

### Correct, bounded query path

1. Read Feed-owned keyset candidates in strict saved-time/ID descent using an older-than tuple predicate. Each candidate batch/qualification call has at most **100 IDs**. Use bounded first-page `PageRequest`/LIMIT plus seek predicate; no growing offset, full count or unbounded query.
2. Call `VaultSearchOperations.filterQualifyingActiveEntries(ids, Set.of())` for each batch; preserve Feed order when selecting the returned subset. No Vault table/entity/repository access, per-row metadata calls, copied lifecycle or top-level Search dependency.
3. Continue from the last examined candidate until `limit + 1` eligible rows or genuine Feed exhaustion. All-rejected batch is not exhaustion. Retain only response/lookahead and current batch, without accumulating rejected rows/the dataset. Account for JPA persistence-context retention when choosing the query/projection: bounded result lists alone do not prove bounded retained rows, and clearing a caller's entire persistence context is not an acceptable shortcut.
4. Return at most `limit`; emit a public cursor **from the last returned row** only after an additional eligible row is found. Never place it beyond lookahead. Internal scanning and public continuation positions have distinct purposes.
5. Use database-read timestamps for comparison/serialization. Prove PostgreSQL round trips with ties/fractional seconds; an in-memory creation timestamp is not database-read evidence. Keep current read-only transaction/isolation conventions and observe committed trash at qualification.

Query-plan/runtime suitability is **NOT_VERIFIED**. Retain the actual candidate SQL shape or focused EXPLAIN on synthetic paging data, observed candidate/qualifier batch sizes/counts and no-N+1/count/load-all inspection. Work can grow with trashed candidates; no constant query-count, latency SLO or large benchmark. A demonstrated need for an index/frozen change requires the smallest explicit Owner amendment before editing that target.

## Precise change map

Production paths are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/`. Use existing packages; no new module/package is needed.

| Permitted target | Bounded change |
| --- | --- |
| `feed/resource/SavedResourceOperations.java` | One additive read, e.g. `findInboxSavedResources(SavedResourceKind kind, int limit, String cursor)`, returning the new page view; preserve old signatures. |
| `feed/view/SavedResourceInboxPageView.java` — new | Immutable existing `SavedResourceView` list plus nullable next cursor; defensive list copy, no persistence/transport types. |
| `feed/internal/application/SavedResourceService.java` | Read implementation; inject existing `VaultSearchOperations`, reuse `toView`, validate and scan. Preserve all mutations/legacy reads. |
| `feed/internal/application/SavedResourceInboxCursor.java` — new | Small owner-local codec/value helper, package-private where possible; no framework, external dependency, signing/state store. |
| `feed/internal/infrastructure/persistence/SavedResourceRepository.java` | Bounded Feed-table keyset/kind query or projection needed by this read; retain `findRecent`. |
| `feed/internal/web/controller/SavedResourceController.java` | Literal `/inbox`, validation/OpenAPI, delegation to owner operations; preserve `/{id}` and other routes. |
| `feed/internal/web/dto/SavedResourceInboxPageResponse.java` — new; `feed/internal/web/mapper/FeedWebMapper.java` | Explicit page DTO/mapping reusing `SavedResourceResponse`; never serialize entities or substitute application views as wire DTOs. |
| Repository-relative `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/` | New `SavedResourceInboxIntegrationTest`, `SavedResourceInboxWebIntegrationTest`; extend `FeedIntegrationTest`, `FeedValidationTest` or `FeedWebIntegrationTest` only for specified compatibility proof. |
| Repository-relative `backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/application/SavedResourceInboxCursorTest.java` — new | Isolated behavioral cursor/bounds/round-trip negatives without mirroring codec implementation. |
| Repository-relative `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java` | Exact protected GET manifest row, operation ID/page schema, no item-404 requirement. Applicable exact operation counts **230 → 231**; page/item/cursor/security assertions. Preserve every prior route and exact-inventory/unique-ID checks. |
| Documentation/handback targets below | Only expressly authorized implementation-adjacent documentation and result/status updates. |

These filenames/operation identify the selected finite path. Local helper/method details may vary while preserving packages, public behavior and exact test-selector names. Resolve any broader target first. No deletion authorized.

## Non-goals — MUST NOT

- No read/unread/archive state/API, title/notes editing, workflow tags/raw-metadata status, metadata badges/filters, full count, new search/ranking or automatic conversion.
- No new Inbox module/table, Vault API/implementation, cross-owner SQL/entity/repository, module edge, root HTTP type, generic orchestrator/pagination or exception hierarchy.
- No DBML, migration/index, POM/dependency, configuration, auth/JWT/PIN, harness/hook/runner or unrelated frozen-source edits. Preserve applied migrations.
- No provider/crawler HTTP, preview, redirect, webpage mirroring, scheduler, queue, Drive/OAuth, Daily Trends, dashboard, diagram regeneration, frontend/deployment or multi-user work.
- No RAG changes, QA execution, inference/evaluation, worker launch, runtime/model download, indexing activation or live-original access. P17 FROZEN, P18 CLOSED; S19-2 `SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY`.
- No agent staging, commit, push, tag, PR or destructive repository operations. No historical log/receipt deletion, overwrite, relocation or truncation.

## Focused verification and negatives

Use real PostgreSQL/Testcontainers through existing fixtures, without H2, mock database paging proof or production test hooks. Fixture cleanup/recovery is infrastructure reuse, not QA authorization. Missing prepared local services/images do not authorize installation/download or unrelated repair.

**Database:** 0/1/100/101 eligible rows; default/max and representative invalid limits; each kind and filtered continuation; equal/fractional database timestamps; first/middle/final pages and changed page size; stable-set no-duplicate/no-skip collection. More than 100 leading trash rows; interleaved trash across multiple full batches; all-trash exhaustion; exactly-limit eligible rows followed only by trash; eligible lookahead after trash. Trash/restore and trashed/removed cursor anchor continue without anchor lookup. Use deterministic disposable fixtures and bounded batch assertions, without timing-only sleeps.

**Committed-trash interleaving:** Coordinate immediately before real Vault qualification; commit trash in another transaction, then let the actual qualifier SQL run. Assert exclusion/truthful continuation. Test-local spy/barrier may coordinate; production latches/locks and mocked qualification results cannot replace real DB proof. Bound waits/joins, release on failure and respect existing fixture transaction/barrier rules.

**HTTP/codec:** Correct page envelope/null terminal cursor and current item fields; literal versus ID routing; malformed/empty/oversized cursor, version/missing/extra fields, timestamp parsing/SQL-range, zero/negative/overflow ID, invalid kind/filter mismatch; valid edited seek tuple and changed limit. Safe 400 codes above; absent/invalid Bearer 401. Representative URL/title/nested metadata remain ordinary JSON/defensive snapshots, with no supplied private marker in errors/logs. Existing-compatible non-HTTP URL capture works without fetch/rendering.

**Compatibility/structure:** Existing save/duplicate/rollback/detail/hash/recent/conversion/web checks plus new legacy-trash assertions. Inspect actual changed Feed/test subtree, descriptors/imports against matrix/tree. Existing architecture tests remain unmodified. OpenAPI exposes exactly the one new route, page/item schema, nullable cursor, truthful parameters/errors, bearer protection and unique operation ID.

### Exact final selectors and bounded commands

Run from repository root in PowerShell after implementing the named tests. Four focused selections, each with a separate **300-second normal-profile** watchdog, are the final slice gate. No full application suite.

```powershell
$inboxPython = (Get-Command python -ErrorAction Stop).Source
$inboxRunDir = Join-Path 'logs/universal-inbox' ('ui26-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $inboxRunDir -ErrorAction Stop | Out-Null
$inboxChecks = @(
    @{ Name = 'inbox-database'; Tests = 'SavedResourceInboxCursorTest,SavedResourceInboxIntegrationTest' }
    @{ Name = 'feed-compatibility'; Tests = 'FeedIntegrationTest,FeedValidationTest' }
    @{ Name = 'inbox-http-openapi'; Tests = 'SavedResourceInboxWebIntegrationTest,FeedWebIntegrationTest,OpenApiRouteInventoryIntegrationTest' }
    @{ Name = 'architecture-archive-boundary'; Tests = 'ApplicationArchitectureTests,RagQaArchiveGuardTest' }
)
foreach ($inboxCheck in $inboxChecks) {
    $inboxReceipt = Join-Path $inboxRunDir ($inboxCheck.Name + '.json')
    $inboxTestArgument = '-Dtest=' + $inboxCheck.Tests
    & $inboxPython scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt $inboxReceipt -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' $inboxTestArgument test
    if ($LASTEXITCODE -ne 0) { throw ('Inbox verification failed: ' + $inboxCheck.Name) }
}
```

`RagQaArchiveGuardTest` checks registration/MVC exclusion with mocks, without QA/model/worker execution. No `ppv.test.archived-s19-2=true`, ambient QA/test-scoped enablement, broad `*Rag*` selector or unselected RAG tests. A group may split into its exact named classes for the cap; retain all receipts and complete the same acceptance set. Failure/timeout is not PASS or authority to raise caps/change tooling/hide skips.

Accepted B1/B2 tooling supports 300 seconds ordinarily, and an explicitly preapproved full-suite ceiling up to 2,100 seconds with inactivity diagnostics/owned-tree cleanup. **No full suite/extended profile is approved here.** Preserve native-hook trust/root/client limits; startup/trust failures do not authorize bypass. Save output to unique ignored `logs/`; summarize counts/exits safely and report nonzero exits promptly. Repeat checks only after a concrete fix/source drift; no redundant full-suite loop.

## Documentation impact and ownership

| Affected current/derived target | Authorized editor, timing and evidence |
| --- | --- |
| `docs/architecture/universal-inbox-api-extension.md` — new; narrow link in `docs/architecture/api-architecture.md` | Antigravity with implementation: delivered route, parameter/page/error examples, active-only/live-list/cursor behavior, unchanged legacy reads/Vault actions, exclusions. Preserve historical sections. Independent Codex compares note/source/OpenAPI/tests. |
| `docs/repository/repository-package-tree.md` | Inspect actual changed subtree; report `ALREADY_CONSISTENT` if existing package inventory still fits. No package relocation/tree-baseline edit authorized; escalate a real structural mismatch. |
| This contract, canonical ACTIVE and future `implementation-handback.md` in this scope directory | Antigravity records results/self-review and status `IMPLEMENTED_AWAITING_CODEX_REVIEW`, links receipts, preserves issued requirements and never self-accepts/claims publication. Codex owns independent review/remediation. |
| Scope README, maintenance navigation, root/policy notices, publication closeout | ChatGPT for separately requested routine governance/closeout after the gate; Owner publishes. Preparation labels are not delivered runtime facts. No unrelated notice cleanup here. |

**Docs Sync Check — creation:** `UPDATED` — executable contract and ACTIVE only. `ALREADY_CONSISTENT` — published reviewed preparation at `a4f89dd`, no prior live handoff, existing Feed/Vault ownership and unchanged architecture/schema. `DEFERRED_OUT_OF_SCOPE` — root `AGENTS.md:1` and `docs/workflow/current-owner-delivery-policy.md:3` still call CSA26 active; inspected ACTIVE/publication records and preparation review establish it is closed/published. Also defer backend-implementer's old pending-B1/B2 phrase; current policy/bounded-command skill govern. ChatGPT owns separately approved notice cleanup. `NOT_VERIFIED` — implementation/runtime/query evidence and future publication.

Antigravity handback and independent review each include a concise Docs Sync Check: changed paths, behavior/evidence, link validation, responsible editor and exact remaining drift. `CSA26-FR-L01`, `SAD-FR-L01`, AUTH-CLOCK and earlier backlog keep their real statuses; no Inbox closure or transferred counter. Historical accepted reviews/ADRs/decisions/logs remain immutable.

**Creation validation (2026-11-01):** All 30 relative file links across the two handoff documents resolve; whitespace/final-newline checks pass, including the new untracked contract. The PowerShell gate parses without execution; UI-01–10 each appear once in the acceptance table. Source/tooling/frozen-contract diffs against HEAD are empty. These are document checks, not implementation or runtime acceptance.

## Guidance, definition of done and blockers

Use existing `backend-implementer` with `java-spring-coding-standards`, `feed-import-workflow-modeling`, `modular-monolith-architecture`, `jpa-postgresql-persistence`, `rest-api-http-contracts`, `backend-testing`, `pragmatic-solid-design`, `reuse-and-consistency` and `bounded-command-execution`. Consult `design-pattern-selection` only for a concrete choice and `graphify-context` for navigation, verifying source facts. Invoke `antigravity-self-review` before submission. Python/RAG/auth implementation, new agents/skills/rules/hooks are N/A. Independent Codex uses `codex-final-review` and `codex-review-coverage`.

Traceable risks: behavior/compatibility UI-01–05; validation/auth/untrusted-data/egress UI-06; ownership/persistence/batching UI-04/07; safe tooling UI-08; API/docs/tree UI-09; evidence/remediation UI-10. Runtime/query/concurrency proof remains mandatory implementation evidence, not PASS. OCR/retrieval/recommendation/GPU/new credentials/deployment/broad benchmarks are N/A; the coverage rubric cannot add features.

Done: approved behavior/negatives implemented; final focused checks pass; mandatory evidence complete; docs agree; Antigravity self-review addresses its blocking findings. Handback records UI-01–10 status/test/receipt pointers, actual exits/counts/skips/durations, changed files, private cursor layout, query/batch observations, Docs Sync Check, limitations/findings. Initial counter stays 0/3 until a blocking independent review starts corrections.

Stop for missing real DB proof, unsafe/archived selector, missing runtime prerequisite, requested read/archive/schema/index/cross-owner change, conflicting frozen behavior or out-of-map source drift. Preserve evidence and request the smallest appropriate amendment; no unrelated repair, assumed acceptance, waived proof or borrowed counter.

One independent slice review follows self-review. This one-slice maintenance feature adds no numbered-phase final/milestone gate. CRITICAL/HIGH/MEDIUM or mandatory NOT_VERIFIED blocks `READY_FOR_OWNER_COMMIT`; LOW/INFO follow current policy. Three correction plus independent re-review rounds maximum; if all remain blocked, stop for separately authorized takeover and independent subsequent review. No automatic takeover.

## Implementation result

**Submitted result (Round 2/3):** Antigravity submitted corrections for UI26-FR-04 and LOW follow-ups, passed all four focused verification stages (139 tests, 0 failures, 0 errors, 0 skipped, all under 300s limit), and completed self-review. The [implementation handback](implementation-handback.md) remains the submitted record; independent adjudication below retains residual LOW aliases accurately.
- Stage 1 `inbox-database`: 40 tests PASS (exit 0, 70.7s) — `logs/universal-inbox/ui26-e96eb144e396424cbde41cc4e91af45f/inbox-database.json`
- Stage 2 `feed-compatibility`: 42 tests PASS (exit 0, 34.7s) — `logs/universal-inbox/ui26-e96eb144e396424cbde41cc4e91af45f/feed-compatibility.json`
- Stage 3 `inbox-http-openapi`: 17 tests PASS (exit 0, 39.5s) — `logs/universal-inbox/ui26-e96eb144e396424cbde41cc4e91af45f/inbox-http-openapi.json`
- Stage 4 `architecture-archive-boundary`: 40 tests PASS (exit 0, 31.0s) — `logs/universal-inbox/ui26-e96eb144e396424cbde41cc4e91af45f/architecture-archive-boundary.json`

## Codex remediation and final review

`READY_FOR_OWNER_COMMIT` in [independent re-review 2](reviews/2026-11-01-independent-re-review-2.md). UI26-FR-01–04 and UI26-FR-L01/L03 are independently VERIFIED / CLOSED. UI26-FR-L02 remains LOW / OPEN / NONBLOCKING for four stale UI-04 handback aliases; do not claim all aliases fixed. No CRITICAL/HIGH/MEDIUM or mandatory unverified acceptance remains for this approved slice.

Supplied four-stage evidence passed 139 tests. Fresh reviewer selection passed 18/18, the strict rendered-schema probe passed 1/1, and the saved-document exact type check passed; evidence: `logs/universal-inbox/codex-rereview-2-20261101-42a64c5028a84f9fb9d63dda9eeac421/`. Actual limit is integer/int32 with numeric default 50; cursor nullability/bounds and controlled precision/privacy tests remain green. Original reviews/failed receipts and issued requirements above remain unchanged.

**Docs Sync Check — acceptance:** `UPDATED` — current acceptance/status sections, ACTIVE and new dated review only. `ALREADY_CONSISTENT` — actual API schema/runtime/current API note, ownership/schema/packages and retained history. `DEFERRED_OUT_OF_SCOPE` — residual LOW L02, routine governance/closeout and earlier unrelated notices/backlog. `NOT_VERIFIED` — eventual Owner publication SHA and unapproved capabilities, not mandatory Inbox acceptance. Counter `2/3` consumed, accepted loop stopped; unused third round not transferable; takeover not authorized.

**Next step:** Owner | commit/push accepted delivery, then ChatGPT records a brief publication closeout with the actual SHA | `UI26-INBOX-BROWSE`, this handoff and independent re-review 2 linked above | READY_FOR_OWNER_COMMIT; UI26-FR-L02 remains LOW/nonblocking; no extra staged-tree review | 2/3 consumed; accepted loop stopped; unused round not transferable; takeover not authorized.
