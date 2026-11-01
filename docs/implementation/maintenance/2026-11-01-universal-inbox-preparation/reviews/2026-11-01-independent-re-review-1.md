# Independent re-review 1 — UI26-INBOX-BROWSE

- **Date / reviewer:** 2026-11-01 / Codex, independent slice re-review.
- **Disposition:** `CHANGES_REQUESTED`. Original UI26-FR-01–03 and UI26-FR-L01 are independently CLOSED; one newly confirmed MEDIUM regression, **UI26-FR-04**, blocks acceptance. Two LOW follow-ups do not block.
- **Authority / baseline:** entered with [ACTIVE](../../../handoffs/ACTIVE.md) = `IMPLEMENTED_AWAITING_CODEX_REVIEW`; same [executable handoff](../executable-handoff-2026-11-01.md), Owner-published preparation and matching local/live remote main `a4f89ddcfa750548e28034c4df78ca0725f85a9b`. No new phase or feature scope.
- **History / counter:** [initial independent review](2026-11-01-independent-final-review.md) remains unchanged. Antigravity correction round **1/3** is now independently reviewed; next correction/re-review is **2/3**. No takeover or transferred allowance.
- **Reviewer write boundary:** this dated report and current review/status sections of ACTIVE and the executable contract only. Production, tests, handback and issued prior reviews remain unchanged by Codex.

## Finding register and original-finding closure

| Finding | Independent disposition / evidence |
| --- | --- |
| UI26-FR-01 — nullable cursor and cursor bound | **CLOSED.** Actual generated OpenAPI 3.1.0 has `nextCursor.type: ["string","null"]` and cursor `maxLength: 512`. Fresh schema/HTTP checks and independent probe pass |
| UI26-FR-02 — database timestamp authority | **CLOSED.** Service retains timestamps only for eligible IDs, bounded by `limit + 1`, and encodes the database projection value. Independent controlled caller-transaction fixture: database/cursor both `2026-11-01T14:00:00.123457Z`, while the managed entity remains `2026-11-01T14:00:00.123456789Z` |
| UI26-FR-03 — mandatory evidence and material handback facts | **CLOSED for required behavior/evidence.** New real database tests cover missing qualification, five kinds, changed limit/removed anchor and committed trash inside a read-only transaction. New HTTP flow/metadata/privacy checks plus independent supplemental checks below establish the previously missing observable cases. Cursor format/range and nonempty terminal-page documentation are corrected. Residual aliases/fixture hardening are separately LOW, not unverified runtime behavior |
| UI26-FR-L01 — whitespace | **CLOSED.** `git diff --check` now exits 0; original three extra EOF blank lines are removed |
| **UI26-FR-04 — limit schema type regression** | **MEDIUM / OPEN / BLOCKING.** Detailed below |
| UI26-FR-L02 — handback method aliases | LOW / OPEN / NONBLOCKING |
| UI26-FR-L03 — regression fixture strength | LOW / OPEN / NONBLOCKING |

### UI26-FR-04 — MEDIUM / OPEN / BLOCKING: OpenAPI now declares the integer limit as a string

**Evidence:** [SavedResourceController.java](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/controller/SavedResourceController.java), line 69, adds `@Parameter(schema = @Schema(minimum = "1", maximum = "100", defaultValue = "50"))` without preserving the inferred integer type. The actual generated Inbox parameter is:

```json
{
  "type": "string",
  "default": "50",
  "exclusiveMinimum": 0,
  "maximum": 100,
  "minimum": 1
}
```

Before this correction, the same parameter generated as `integer / int32` with numeric default `50`. Runtime binding still uses `int`; the current API note also correctly calls it an integer. Thus the annotation introduced a new documentation/client-contract regression while fixing the cursor schema.

[OpenApiRouteInventoryIntegrationTest.java](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java), lines 636–638, calls `.asInt()` without checking JSON node types. Jackson coerces the default string `"50"` to 50, so all 139 submitted tests and the fresh OpenAPI selection pass despite the defect.

**Consequence / violated requirement:** UI-09 and the issued truthful-parameter contract are not satisfied. Schema consumers see a string parameter and string default, and numeric minimum/maximum do not constrain string instances. This is confirmed output, not a request for another API or optional generator style.

**Smallest correction:** preserve integer typing (and the appropriate integer format/numeric default) in this owner-local schema annotation, or let the generator infer it correctly. Keep the existing runtime signature/default/validation/error behavior and cursor metadata. Add strict generated-document assertions for integer type and numeric default 50; do not use coercion as proof of their types. No root OpenAPI configuration, dependency, schema, auth or database change is needed.

**Reproduction / close with:** `generated-openapi.json` and `strict-openapi-parameters.json` in the reviewer directory below. The latter explicitly checks the saved generated document and exits **1 / FAIL** for both integer type and numeric default; cursor bound passes. After correction, run the existing HTTP/OpenAPI selector and require the strict snapshot check to pass. Retain the other handoff gates with explicit source-applicability evidence; rerun affected groups if further behavior/source changes. No full suite or new capability is authorized.

### LOW follow-ups — no extra correction loop on these alone

**UI26-FR-L02:** [handback](../implementation-handback.md), lines 18 and 27–30, still uses aliases such as `trashedSavedResourceDuplicateStillConflicts`, `saveManualSavedResourceUrlHashIsUnique`, `legacyReadsReturnTrashedSavedResource` and `committedTrashInterleavingExcludesResourceAtQualification`. Actual equivalent tests exist under names including `trashedDuplicateUrlConflictsWithoutResurrection`, `duplicateUrlHashRollback`, `legacyReadsStillReturnTrashedSavedResources` and `committedTrashBeforeQualificationIsExcluded`. Correct aliases in a future authorized documentation pass or while the handback is already touched. Class-level receipts and actual behavior have been independently verified; this is now lookup hygiene, not missing acceptance evidence.

**UI26-FR-L03:** some newly committed fixtures prove less than their labels:
- [Web test](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/SavedResourceInboxWebIntegrationTest.java), lines 138–181 and 324–335, encodes ARTICLE cursors but omits the request's kind. Filter mismatch therefore happens before timestamp/ID validation. The private-marker assertion checks decoded plaintext, not the encoded token. Use matching filters and check encoded plus decoded forms when hardening those tests.
- The caller-transaction test at [database test](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/SavedResourceInboxIntegrationTest.java), lines 542–566, tries wall-clock samples instead of forcing a known sub-microsecond fixture. Prefer a controlled value and a demonstrable mismatch with persisted precision.

These are LOW after **independent matching-filter HTTP and controlled-precision probes actually passed the required cases**. No approved behavior remains unknown because of those fixture weaknesses. Preserve that supplemental evidence; do not describe the weak assertions as the proof. No human waiver, new benchmark or additional product feature is introduced.

## Fresh independent verification and reused evidence

Reviewer artifacts: `logs/universal-inbox/codex-rereview-1-20261101-7a2c7286533d40c9825b3e378b15098b/`. All long commands used the implemented normal-profile 300s watchdog and fresh non-clobber paths.

| Evidence | Actual result |
| --- | --- |
| `inbox-database.json` | Exact `SavedResourceInboxCursorTest,SavedResourceInboxIntegrationTest`: **40/40 PASS**, no failures/errors/skips, exit 0, **40.625s** |
| `inbox-http-openapi.json` | Exact `SavedResourceInboxWebIntegrationTest,FeedWebIntegrationTest,OpenApiRouteInventoryIntegrationTest`: **17/17 PASS**, no failures/errors/skips, exit 0, **41.031s** |
| `review-probe-compile.json` | Isolated reviewer source compiled with existing local JDK/test dependencies; exit 0, **2.531s** |
| `review-probes.json` | **5/5 PASS**, exit 0, **47.688s**. Nullable cursor, controlled database timestamp, real batching/retention, populated HTTP paging/default/max and matching-filter private/error cases |
| `strict-openapi-parameters.json` | **FAIL**, explicit JSON type checks against the generated document: integer type false, numeric default false, cursor bound true. Short Python check exit 1; basis for FR-04 |
| `submission-sha256.json` | Inventory of **19 submitted changed/new files**; no drift while reviewing/testing |
| Probe source/argument files and generated/observation JSON | Retained under this new ignored directory; prior raw receipts and failed-probe history untouched |

The supplemental passing probes provide actual evidence omitted or masked by the submitted fixtures:

- Fixed managed timestamps `.123456100Z` and `.123456789Z`, explicitly flushed within one caller transaction; encoded anchor equals the persisted rounded value. No production clock or caller persistence-context clearing.
- 201 newer trash rows and three older active rows: batches **100 / 100 / 4**, three scalar candidate queries plus one two-ID hydration, managed entities **0 → 2**, two returned items/non-null cursor. The new timestamp map retains only eligible tuples; no rejected-row accumulation.
- 101 HTTP-visible resources: default **50**, changed-limit continuation **51**, maximum **100**, final limit-one tail **1**, exact combined ID order and truthful null terminal cursor. Empty/trimmed kind handling also passed.
- **Nine matching-filter decoded negative cases** (SQL range, overflow/invalid ID, malformed timestamp/layout/version/private marker), plus empty/blank/oversized cursors and integer binding overflow. Actual 400 codes/messages pass; neither encoded cursor nor decoded private/SQL marker appears in response/log capture.

Antigravity's new four receipts/raw summaries under `logs/universal-inbox/ui26-29ec5be89a324b8ea9294f76d12f6fb7/` were inspected: **40 + 42 + 17 + 40 = 139**, zero failures/errors/skips, exits 0. The 42 compatibility and 40 architecture/archive results are reused where source remains applicable, not presented as fresh reviewer executions. The fresh reviewer total is **57 selected tests plus five probes**; their green result does not override the separate strict schema failure.

Existing Oracle Java 25.0.2 / Windows 11 and local PostgreSQL fixture/runtime/dependencies were reused. No full suite, archived QA, inference, worker/model execution, runtime installation/download or publishing. Existing JDK/Mockito/CDS and unchanged audit-test deprecation warnings remain classified as before; no new IDE inspection or warning-free claim.

## Scope, structure and review coverage

Inspected the complete changed subtree/diff and remediation delta against the previous manifest. Production changes remain in the permitted Feed service/DTO/controller/mapper/resource/repository paths, with tests/API notes/status alongside them. Actual main Feed tree remains **87 Java files / 24 packages**, all with descriptors, no stale `.gitkeep`. No migration, POM/dependency, module allowlist, root HTTP/security, harness or unrelated frozen-target delta. Existing package-tree inventory still fits.

**KEEP / REUSE:** current scalar keyset reads, bounded eligible timestamp map, public Vault batch qualification, immutable page view/DTO, existing envelope/advice and capture/lifecycle/conversion/search paths. **SIMPLIFY:** make only the local schema/type assertion correction. **DEFER:** read/archive, metadata filters/badges, index changes, provider fetching, frontend and all RAG work. No new owner decision or agent/hook/framework is justified.

| Rubric dimensions | Status / evidence |
| --- | --- |
| 1 Intent; 3 Architecture; 4 Ownership | PASS — same approved reuse-only slice and legal existing public contracts |
| 2 Traceability | PASS for actual independent requirement mapping; LOW aliases L02 |
| 5 Package/folder structure; 6 Shared contracts | PASS — full tree inventory and unchanged ADR-0016 root envelope/advice placement |
| 7 SOLID; 8 DRY/KISS; 9 Patterns | PASS — local roles/reuse and bounded map; no speculative abstraction |
| 10 Domain/state; 15 Transactions/concurrency; 16 Failure/recovery | PASS — fresh paging/precision/read-transaction proof; supplied mutation rollback/restore compatibility |
| 11 API/wire | FINDING — FR-04; prior nullable cursor/bound fixed |
| 12 Input validation; 13 Authentication; 14 Privacy/egress; 21 Security tests | PASS for actual matching-filter/marker/401/source evidence; fixture hardening remains LOW |
| 17 Persistence; 18 Complexity/resources | PASS — real PostgreSQL scalar/batch/hydration observations and bounded eligible state; no general SLO claim |
| 19 Retrieval; 20 Document/OCR; 23 Standalone benchmarking; 24 Evaluation | N/A — no changed/enabled capability or approved benchmark requirement |
| 22 Test quality | FINDING — schema coercion misses FR-04; LOW fixture follow-up L03; supplemental evidence explicit |
| 25 Dependencies; 26 Build/tools/diagnostics | PASS for unchanged dependency scope, actual bounded gates and warning classification; no IDE inspection |
| 27 Logging/observability | PASS — no new payload logging and independent encoded/decoded marker capture |
| 28 Documentation/governance | FINDING — schema contradicts runtime/API note; LOW aliases, truthful dated history and counter retained |
| 29 Hygiene/delivery | PASS — whitespace check 0, scoped files only, Owner-only Git |

UI-01–08 have source-applicable or fresh positive evidence as detailed above. UI-09 remains **FAIL / FR-04**. UI-10 self-review/receipts/counter exist; current acceptance is blocked by that confirmed contract defect, not by a requirement to rerun an unrelated phase.

## Scoped Docs Sync Check and next correction

- **UPDATED:** this dated re-review and current ACTIVE/executable review/status only: `CHANGES_REQUESTED`, original findings closed, new FR-04 OPEN, LOW L02/L03 tracked, **1/3 consumed / next 2/3**.
- **ALREADY_CONSISTENT:** original review history, published preparation baseline, corrected terminal-page/cursor facts, existing Feed/Vault ownership/schema/package layout and frozen/inactive RAG state.
- **FINDING:** generated `limit` schema contradicts the integer runtime/API note; Antigravity owns the narrow annotation/test correction under the existing handoff. Remaining method aliases are LOW.
- **DEFERRED_OUT_OF_SCOPE:** routine scope README/index/publication closeout and old CSA26/root/policy notices remain ChatGPT's separately authorized governance work. Earlier CSA26/SAD/AUTH-CLOCK/RAG findings and counters are unchanged.
- **NOT_VERIFIED:** future FR-04 fix and Owner publication; no feature acceptance/publication claimed.

**Next step:** Antigravity | `/antigravity-implement-handoff`, mandatory `/antigravity-self-review`, then Codex `$codex-final-review` | `UI26-INBOX-BROWSE`, [executable handoff](../executable-handoff-2026-11-01.md) and UI26-FR-04 above | preserve integer limit schema/numeric default and prove exact generated types before Owner commit | 1/3 consumed; next correction/re-review 2/3; takeover not authorized.
