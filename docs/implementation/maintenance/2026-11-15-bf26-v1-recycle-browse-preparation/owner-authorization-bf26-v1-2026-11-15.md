# Owner Authorization — BF26-V1-RECYCLE-BROWSE (2026-11-15)

**Decision:** `OWNER_APPROVED / CONTRACT_RATIFIED / EXACT_FROZEN_SOURCE_API_TEST_EXCEPTION_APPROVED — BF26-V1 ONLY`.

**Owner's explicit approval:** “Tôi phê duyệt Owner Authorization BF26-V1 với hợp đồng trên, đúng 9 production / 4 test và tiếp tục sử dụng `main`.” This decision follows the published preparation and [independent pre-handoff review](reviews/2026-11-15-bf26-v1-independent-pre-handoff-review.md). It is **subsequent authority**, superseding only the earlier *pending Owner decisions/source-not-authorized* wording in dated preparation documents; it does not rewrite those reviewed historical inputs or their evidence.

- **Repository:** `vhvkhangg/personal-private-vault`.
- **Verified published preparation `main` HEAD at this authorization:** [`5e333d96a4565b7e5e40d3a15bc5db57a10dd56f`](https://github.com/vhvkhangg/personal-private-vault/commit/5e333d96a4565b7e5e40d3a15bc5db57a10dd56f), `docs(backend): prepare private vault trash browsing` (2026-11-15 06:03:09 UTC).
- **Reviewed source baseline:** `2d088445e6323b091741cf370e1e0c8cf13928a2`; its 9-production/4-test map was qualified by the independent review. Recheck actual HEAD, changed files and input fingerprints when creating the executable; a newer docs-only authorization publication is expected, not automatically source drift.
- **Pre-handoff gate:** `READY_FOR_HANDOFF — TECHNICAL PREPARATION ONLY`, no blocking finding; **V1 runtime NOT_RUN**.
- **ACTIVE now:** `NO_ACTIVE_HANDOFF`. Source permission is not an executable handoff, not implementation acceptance and not deployment authority.

## 1. Ratified product, security and HTTP contract

**Exactly one additional endpoint:** `GET /api/v1/vault/trash?limit=25&cursor=...`, on the existing Vault owner; intended operation ID `listVaultTrash`. No other new route or change to existing Vault ID-based trash/restore operations.

| Dimension | Binding Owner decision |
| --- | --- |
| Admission | Authenticated persisted `PROFILE_PRIVATE` only: 200 on valid requests; authenticated persisted `PROFILE_PUBLIC` from either direct login or PRIVATE→PUBLIC downgrade: 403 `ACCESS_DENIED` before business handler; anonymous/invalid/expired: canonical 401. The existing **eight** PUBLIC business GET pairs remain exactly eight. |
| Item wire | **Exactly** `id` (positive int64), `entryType` (existing Vault enum), `deletedAt` (non-null ISO-8601 timestamp). **No title**, created/updated time, content, NSFW, tag, favorite, rating, Person/feature details or other enrichment. Existing five-field Vault DTOs remain unchanged. |
| Envelope | Canonical `ApiResponse`: on success `data = {"items": [...], "nextCursor": string-or-null}`, `error=null`, `meta=null`. Both `items` and `nextCursor` always present; an empty/exhausted result has `items=[]` and `nextCursor=null`. |
| Ordering | `id DESC` keyset ordered by identity ID, **not** most-recently-deleted order. Seek strictly below the last emitted ID; no offset, count or whole-collection in-memory pagination. |
| Limit | Omitted => **25**; accepted inclusive **1–100**. Invalid, non-integer, overflow and out-of-range requests => canonical safe **400**. |
| Cursor | Absent => first page. Canonical **unpadded Base64url UTF-8** of `v1|<positive-id>`, maximum encoded length **128** characters. Reject padding/noncanonical encoding, invalid UTF-8, blank, extra components, unsupported version, nonpositive, malformed or overflowing IDs with safe 400. Cursor is not an authorization credential and must not be logged/echoed in errors. |
| SQL bound | One owner-local parameterized deleted-only DB query (`deleted_at IS NOT NULL`, optional `id < cursorId`, `ORDER BY id DESC`), fetching **at most limit+1 qualifying rows** from the database for has-more detection. Cursor encodes the **last emitted**, never the lookahead ID. No offset/count/unbounded Java post-filter. |
| Consistency | **Weak consistency across requests**. Stable membership implies no duplicates/skips across 3+ pages; concurrent restore, re-trash or newly deleted high-ID items may affect ongoing traversal. No exactly-once or cross-request snapshot promise. Existing trash/restore idempotence is preserved. |
| Cache/failures | `Cache-Control: no-store` on successful PRIVATE results and applicable generated errors, with safe **400/500** envelopes. Genuine DB/transaction/commit failure is **500**, never silent empty success or a lifecycle 409. Validate actual pre-handler parse/security response behavior and existing advice/response reset. No global handler/filter rewrite. |
| Query ownership | Vault only; read-only service result fully materialized before return. No cross-module title/name lookup, source hydration, mutation locks, jobs, provider requests, indexing or RAG work. |

The Owner expressly accepts that identity-only pages are less useful for recognizing deleted content, and that `id DESC` does **not** sort by deletion time. No title or timestamp-order enhancement is implied or authorized.

## 2. Exact frozen-source/API exception — 9 production paths

An **additive, bounded exception to the otherwise frozen Phase 1 Vault and Phase 13 REST/API source** is approved **only** for the following paths, under `J/ = backend/src/main/java/com/vhvkhangg/personalprivatevault/`:

| ID | Authorized exact path | Allowed purpose |
| --- | --- | --- |
| P01 | `J/vault/entry/VaultEntryOperations.java` | One typed, bounded trash collection read operation; old signatures retained |
| P02 | `J/vault/internal/application/entry/VaultEntryService.java` | Validate bounded request, use read-only owner query, materialize immutable page/cursor; preserve lifecycle and guards |
| P03 | `J/vault/internal/infrastructure/persistence/VaultEntryRepository.java` | Parameterized deleted-only strict ID keyset and database-enforced `limit+1` bound |
| P04 | `J/vault/internal/application/entry/VaultTrashCursor.java` **NEW** | Vault-local versioned canonical bounded cursor codec |
| P05 | `J/vault/view/VaultTrashPageView.java` **NEW** | Immutable page over existing VaultEntryView and nullable continuation cursor |
| P06 | `J/vault/internal/web/controller/VaultController.java` | Exactly one PRIVATE-only GET handler, typed HTTP metadata/validation, safe route-local error/cache behavior |
| P07 | `J/vault/internal/web/dto/VaultTrashItemResponse.java` **NEW** | Explicit exact three-property item serialization |
| P08 | `J/vault/internal/web/dto/VaultTrashPageResponse.java` **NEW** | Immutable `{items,nextCursor}` HTTP data representation |
| P09 | `J/vault/internal/web/mapper/VaultWebMapper.java` | Pure page/item view-to-wire mapping only; existing mappings unchanged |

All four NEW production classes belong to **existing packages** with package descriptors; no new package/module is authorized. The qualified map and source blob IDs are in the [independent review](reviews/2026-11-15-bf26-v1-independent-pre-handoff-review.md).

## 3. Exact test exception — 4 existing paths

Under `T/ = backend/src/test/java/com/vhvkhangg/personalprivatevault/`:

| ID | Authorized exact path | Mandatory proof |
| --- | --- | --- |
| T01 | `T/vault/VaultEntryIntegrationTest.java` | Real PostgreSQL keyset, SQL bound/EXPLAIN and deterministic multi-page/stable-membership and committed restore/re-trash cases |
| T02 | `T/vault/VaultWebIntegrationTest.java` | Actual PRIVATE HTTP JSON, exact three keys, safe parameter/cursor 400, 500/no-store, empty and exhausted page, no writes |
| T03 | `T/authentication/ProfileRouteAuthorizationIntegrationTest.java` | Both persisted PUBLIC origins 403 before handler, anonymous 401, PRIVATE 200, unchanged eight PUBLIC GET pairs and dispatcher guards |
| T04 | `T/web/OpenApiRouteInventoryIntegrationTest.java` | Exactly one new registered handler/manifest row, unique `listVaultTrash`, generated OpenAPI payload, refs, parameters and error schemas |

**Read-only regression selectors, not edit permissions:** `VaultMutationConcurrencyTest`, `VaultMetadataIntegrationTest`, `ApplicationArchitectureTests`, relevant existing security tests and `GlobalSearchIntegrationTest#verifiesRecycleBinExclusion`. New test files or changes to any other test require separate Owner approval.

## 4. Explicitly excluded and STOP conditions

**Do not modify** `SecurityConfiguration.java`, root `OpenApiConfiguration.java`, root `ApiResponse`/`ApiResponses`/`ApiMeta`/`ApiPageMeta`/`ApiExceptionHandler`, existing Vault entity/view/five-property response/advice, global security/session/Jackson/filter/transaction manager, module/package descriptors, DBML/Flyway/schema/indexes, POM/locks/build/dependencies, other modules, Frontend, Search, RAG, providers, hooks or agents. No additional PUBLIC business route, trash/restore behavior change, hard delete/purge, feature-title lookup, shared pagination framework or new branch/worktree.

The Owner **selects the ordinary existing `main` checkout** and grants **no branch/worktree exception**. The 2026-11-11 backend-first assessment's proposed sibling worktree/branch is not selected. Preserve unrelated uncommitted work and repository hygiene; no agent staging, committing, pushing, PR or deployment.

**STOP and seek a new exact Owner authorization** if implementation demonstrably requires any source/test file beyond this **9+4** map; generated OpenAPI cannot be truthful without root config edits; default security fallback fails; generated SQL/schema demands an index/order migration; existing canonical error/no-store handling cannot be satisfied route-locally; the current source/evidence boundary materially drifts; another ACTIVE handoff appears; or a new privacy/contract choice is needed. Do not interpret an indirect dependency, old preparation proposal or a green test as permission to expand scope.

## 5. Reviewer qualifications and finite acceptance

Carry forward **all `V1-01`–`V1-19`** from [finite acceptance](finite-acceptance-v1.md), plus the [independent review](reviews/2026-11-15-bf26-v1-independent-pre-handoff-review.md)'s mandatory precision:

1. Prove `no-store` on success and all relevant error pathways, including pre-handler type coercion, repository exception and transaction completion; do not alter global advice merely to accommodate this route.
2. Validate bounded server-side seek/fetch and realistic **sparse-trash PostgreSQL EXPLAIN**. `limit+1` bounds returned qualifying rows, **not total rows examined or latency**; no unmeasured performance PASS claims.
3. Test the **new** read operation rather than relying on a class-level test transaction: use actual committed restore/re-trash between requests, stable 3+ page traversal across Vault entry types, `limit+1` boundary, cursor canonicality and defensive immutable DTOs.
4. PUBLIC denial must precede MVC business handler/owner read; authentication/session may still legitimately touch persistence. Invalid PRIVATE query must not execute the **business** trash repository read. No secrets/raw cursor in logs/errors.
5. In T04, update the coupled real route/operation-ID inventory and validate **generated** OpenAPI and HTTP specimens; do not invent a global route count or preapprove root OpenAPI editing.

Run six finite gates described in the review (`V1-DB`, `V1-HTTP`, `V1-SEC`, `V1-API`, `V1-ARCH`, `V1-REG`) under existing **normal 300-second-per-command** bounded runner, capturing non-clobber receipts/stdout/stderr/XML and preserving failed attempts. No full-suite, production-data, browser/Front-end, RAG, provider or deployment work is implied. Antigravity implementation and **separate mandatory `/antigravity-self-review`** precede independent Codex `$codex-final-review`; only the Owner publishes independently accepted code.

## 6. Handoff authorization and preservation

**Authorized next action:** after the Owner commits/pushes **this docs-only authorization record** to the verified `main` and confirms its current SHA, Codex may run **`$codex-create-handoff`** and create an executable **for BF26-V1 ONLY** from the published preparation, this explicit decision and the independent review. Existing harness must discover the material in-repository; no supplementary executable prompt or assistant-created handoff is required. Codex may update its own scoped executable/ACTIVE records only according to the established handoff workflow; **do not edit production/tests in the handoff-creation step**. A valid reviewed executable is required before Antigravity implementation; this record alone is not an active handoff.

**Current counters:** V1 implementation/remediation `NOT_STARTED / 0 consumed` (budget is opened only in the valid later executable); takeover `NOT_AUTHORIZED / NOT_USED`. RO-B `INDEPENDENTLY_ACCEPTED / OWNER_PUBLISHED / CLOSED`, 114 distinct accepted cases (25 reviewer fresh + 89 source-qualified reused), remediation `2/3 consumed / STOPPED`, `DPROB-L01/L02 LOW / OPEN / BACKLOG`. RO-A Owner-published/CLOSED, remediation `1/3 / STOPPED`, `DPROA-L01 LOW / OPEN / BACKLOG`. Prior DP-A/B1/W1 counters/backlogs and paused H1-F/FR-13, frozen P17, closed P18, suspended S19-2, providers OFF/unset remain unchanged.

### Docs Sync Check

- **ADDED:** this Owner authorization under the existing BF26-V1 preparation directory; no previously reviewed files rewritten by this package.
- **ALREADY CONSISTENT:** independent `READY_FOR_HANDOFF` preparation, exact source/test map, eight existing PUBLIC GETs, PRIVATE fallback, actual published V1 preparation commit, RO-B actual-SHA publication/closeout, current `ACTIVE = NO_ACTIVE_HANDOFF`.
- **HISTORICAL/OVERRIDDEN:** earlier README/workflow/Owner preparation approval lines saying ratification or exact source grant is *pending*. They remain true of the earlier **pre-authorization checkpoint**; **this later approved record controls** after it is published. `PROPOSED` source/contract tables remain preserved reviewer input, not conflicting current Owner authority.
- **DEFERRED:** broader root README/roadmap/policy/index/ACTIVE navigation, later executable and source implementation, unselected LOW backlogs and unrelated features.
- **NOT_VERIFIED:** any V1 runtime implementation, generated SQL/OpenAPI, PostgreSQL plan, fresh Java tests, live deployment or real Owner-vault data suitability. No runtime PASS is claimed.

**Next step:** Owner | add and review this documentation-only authorization, commit/push it on `main` | the reviewed BF26-V1 preparation plus exact `9 production / 4 test` ratification | only then `$codex-create-handoff` | `NO_ACTIVE_HANDOFF`, V1 remediation not started, takeover NOT_AUTHORIZED.
