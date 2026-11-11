# BF26-S5 — Option A: Minimal Dashboard API Readiness

- **Date / baseline version:** 2026-11-11 / `BF26-S5-A-v1`.
- **Authority:** [dated Owner Option A decision](owner-decision-option-a-2026-11-11.md), following the [BF26 Backend-first decision](../2026-11-11-backend-first-assessment/owner-decision-backend-first-2026-11-11.md).
- **Published source baseline:** local `main` HEAD, `origin/main` and live remote `main` all verified at [`bbdcd3e6d7c1eac1d800f95883a5753070d72e9e`](https://github.com/vhvkhangg/personal-private-vault/commit/bbdcd3e6d7c1eac1d800f95883a5753070d72e9e). This corrected package is uncommitted; that SHA publishes the earlier assessment, not these documents.
- **Status:** `READY_FOR_OWNER_COMMIT / DOCS_ONLY_API_READINESS / NOT_AUTHORIZED_FOR_IMPLEMENTATION`. The [independent re-review 1](reviews/2026-11-11-bf26-s5-independent-pre-handoff-re-review-1.md) VERIFIED/CLOSED BF26S5-PH-01–03 with no remaining blocking findings in this docs-only scope. The [initial independent review](reviews/2026-11-11-bf26-s5-independent-pre-handoff-review.md) remains historically `CHANGES_REQUESTED`; no runtime/Frontend acceptance or Owner publication is implied.
- **Evidence:** preserve the independent review and its [254-route static inventory](reviews/2026-11-11-bf26-s5-static-route-inventory.csv). No implementation/remediation/takeover round consumed or granted; all historical counters remain unchanged.

## Selected consumer package

The first package targets the **current authenticated singleton Owner only**. The exact source contracts, owning operations, DTO fields, errors, navigation targets and finite acceptance cases are in the [selected API/consumer/acceptance matrix](selected-consumer-api-acceptance-matrix-2026-11-11.md). These are future consumer requirements and current Backend source facts; no Dashboard UI is implemented or certified here.

| Selected consumer | Existing request | Selected display / journey | Limits and freshness |
| --- | --- | --- | --- |
| A. Universal Inbox preview | `GET /api/v1/saved-resources/inbox?limit=5` | `title`, `sourceName`, `kind`, `savedAt`; existing `id` for supported saved-resource detail navigation | First capped preview, no total requirement or new read/archive state; preserve existing opaque cursor without requiring continuation UI |
| B. Combined Daily Trends Radar preview | `GET /api/v1/feed/radar/combined?limit=5` | `title`, `sourceName`, `publishedAt`, article `url` | Existing rolling 24-hour cached window; `retrievedAt` is retrieval time, not last provider refresh; no refresh POST |
| C. Vocabulary Due preview | `GET /api/v1/knowledge/vocabulary/due?limit=5` | `word`, `meaning`, `languageCode`, `learningStatus`, `nextReviewAt` | Omit `cutoff` to use server now; preserve null review times; no total due count, pagination or SRS change |
| D. Global Search navigation | `GET /api/v1/search?q=<valid-nonempty-query>&limit=10` | Existing results and owning-module detail navigation keyed by `entryType` and `vaultEntryId` | Consumer default 10, existing HTTP default remains 50; preserve offset, filters and `hasMore`; never use empty-query catalog browsing |

No other Dashboard card or journey is selected. Finance, subscriptions, Drive status/quota, general Recent Activity, cross-module totals and new chart/statistics APIs are deferred. Dual Public/Private access profiles and Recycle Bin browsing remain [independent Backend backlog](../2026-11-11-backend-first-assessment/owner-deferred-work-access-profiles-and-recycle-bin-2026-11-11.md), not prerequisites for this singleton-Owner package.

## Composition, state and reuse

Fetch previews independently when the Dashboard opens or the user explicitly refreshes it. Search additionally requires a valid query. There is no new aggregation endpoint/application module, automatic polling, scheduled job or provider activation. Each preview has independent **success, empty, error and unavailable** consumer states; one failed read must preserve successful independent reads. These states do not introduce Backend DTO status fields or new HTTP error codes. Keep existing `ApiResponse`, authentication and error semantics. Never convert a failed read into an empty success, label cached data stale without evidence, or fabricate counts, timestamps, continuation tokens or cross-source consistency.

| Feature / mechanism | Disposition | Implementation / verification effort (estimate) |
| --- | --- | --- |
| Inbox, cached Radar, due vocabulary and search | REUSE existing owner-module reads | No demonstrated Backend source change; bounded static route/DTO/acceptance qualification |
| Detail navigation | REUSE existing Backend detail GETs; DOCUMENT remaining Frontend contract | 17 searchable types have detail API declarations; Frontend destination pages/routing are missing and remain future separately approved work |
| Dashboard aggregate, new envelope/count/state APIs | REMOVE from this package | No selected requirement justifies them; no source or cross-module transaction |
| Broader cards, profiles, Recycle Bin, Frontend/BFF, RAG, performance platform | DEFER | Separate Owner scope and applicable preparation/review before implementation |

**Source-grounded conclusion submitted for review:** no Backend endpoint/DTO gap is demonstrated for the selected reads. The matrix identifies missing Frontend destination/navigation contracts without inventing URLs or implementing pages. Runtime, browser integration and deployed readiness remain `NOT_VERIFIED`; this is a versioned documentation readiness baseline, not end-to-end acceptance.

## Versioned baseline and future API evolution

`BF26-S5-A-v1` is **not a permanent API freeze**. The Owner allows later additions or modifications when actual Frontend design/implementation needs become concrete. Preserve accepted contracts unless a later approved change is necessary; prefer backward-compatible additions within existing module ownership. Breaking API, schema, authorization and affected-consumer changes require an impact assessment, separate Owner authorization and appropriate tests/review before implementation. Do not prebuild speculative endpoints. This decision neither resumes H1-F nor waives FR-13.

## Correction trace and delivery workflow

| Historical finding | Submitted correction | Disposition now |
| --- | --- | --- |
| BF26S5-PH-01 | Owner decision and the four selected rows above fix audience, fields, caps, journeys, freshness, composition and exclusions | VERIFIED/CLOSED in independent re-review 1 |
| BF26S5-PH-02 | Source-grounded selected matrix supplies operation/DTO/error/navigation traceability, supported contracts versus gaps, and finite static/conditional-runtime cases | VERIFIED/CLOSED in independent re-review 1 |
| BF26S5-PH-03 | Conditional publication/docs-only route below replaces automatic implementation routing | VERIFIED/CLOSED in independent re-review 1 |

1. Complete bounded documentation correction and static/link/Docs Sync checks; **STOP this authoring session here**.
2. A **separate independent Codex session** runs `$codex-pre-handoff-review` on this corrected maintenance package and adjudicates PH-01–03. This session does not perform that re-review or close the findings.
3. Only if the independent result is `READY_FOR_OWNER_COMMIT`, the Owner stages, commits and pushes the accepted preparation.
4. If no implementation gap is demonstrated, conclude with **docs-only API readiness**; create no executable handoff or implementation/self-review cycle.
5. If a concrete source gap remains, the Owner must separately approve its exact bounded scope. Only after accepted preparation publication may Codex create a new executable handoff for that approved gap. No approval, counter reset or source work follows automatically from this package.

## Scope-bounded Docs Sync Check

- **Scope / classification:** README is current preparation; the new dated decision records this Owner authorization; the matrix is a versioned derived source/consumer contract. The existing independent review, CSV, earlier decisions, evidence and counters are immutable history. Entry tracked worktree/index were clean; the README, review, CSV and deferred-backlog record were pre-existing untracked inputs.
- **UPDATED:** this README, [Option A decision](owner-decision-option-a-2026-11-11.md), [selected matrix](selected-consumer-api-acceptance-matrix-2026-11-11.md), and only directly affected current navigation: [root AGENTS](../../../../AGENTS.md), [Owner policy](../../../workflow/current-owner-delivery-policy.md), [roadmap](../../../roadmap.md), [maintenance index](../README.md), [ACTIVE current planning block](../../handoffs/ACTIVE.md). ACTIVE remains `NO_ACTIVE_HANDOFF`; historical registers are preserved. The maintenance index now distinguishes published BF26 assessment `bbdcd3e` from unpublished S5 preparation.
- **ALREADY_CONSISTENT:** singleton Owner scope; frozen module/API/schema boundaries unchanged; H1-F `OWNER_PUBLISHED / NOT_INDEPENDENTLY_ACCEPTED / CHANGES_REQUESTED / FRONTEND_PAUSED_BY_OWNER`; FR-13 `MEDIUM / OPEN / UPSTREAM_DEPENDENCY_BLOCKED`; H1F-L01 deferred; v0.3/evidence/pins/lock and all counters preserved; P17 frozen/P18 closed/S19-2 suspended-archival-only; indexing inactive/live originals disabled/deferred; normal Google/Drive/Files/Feed flags unchanged OFF/unset.
- **Static verification:** results and integrity hashes are recorded in the matrix's verification section. These are author checks, not independent acceptance. No runtime tests, installations, migrations, provider calls, source/test/dependency changes, branch/worktree, staging, commit or push.
- **Independent review outcome:** PH-01–03 VERIFIED/CLOSED; READY_FOR_OWNER_COMMIT under independent re-review 1. **NOT_VERIFIED:** fresh HTTP/OpenAPI/DB/browser/operational/IDE/performance evidence. Missing Frontend destination contracts remain explicit; no permanent Backend gap is inferred.
- **DEFERRED_OUT_OF_SCOPE:** older dated notices in root/scoped AGENTS, policy/roadmap and unrelated maintenance records; broad repository/package-tree cleanup and final non-RAG audit. Preserve history for separately authorized cleanup; no global rewrite.

**Next step:** Owner | stage, commit and push the reviewed documentation package | BF26-S5 Option A README, decision, selected matrix and independent re-review 1 | READY_FOR_OWNER_COMMIT; docs-only API readiness, no executable handoff or implementation | BF26-S5 no implementation/remediation/takeover round consumed; historical counters unchanged.
