# BF26-V1-RECYCLE-BROWSE — Owner publication closeout (2026-11-15)

- **Slice:** `BF26-V1-RECYCLE-BROWSE`, one PRIVATE-only read of soft-deleted Vault identities.
- **Verified published GitHub `main` source commit:** [`54b62627d7d937e7a95bcca7629c9f0e26786e8f`](https://github.com/vhvkhangg/personal-private-vault/commit/54b62627d7d937e7a95bcca7629c9f0e26786e8f), `feat(backend): add private vault trash browsing`, **2026-11-15 11:08:52 UTC**.
- **Source commit parent / reviewed entry:** [`58cbd58d1f3fe6be002ba97eb6599292a758f8f5`](https://github.com/vhvkhangg/personal-private-vault/commit/58cbd58d1f3fe6be002ba97eb6599292a758f8f5).
- **Owner authority:** [V1 exact approval](owner-authorization-bf26-v1-2026-11-15.md), Owner-published at [`757643e9adcafa0c54b0adb3cca93cfa8efd0731`](https://github.com/vhvkhangg/personal-private-vault/commit/757643e9adcafa0c54b0adb3cca93cfa8efd0731): nine production plus four test paths, existing `main`, additive Phase 1/13 exception only.
- **Independent acceptance:** [final re-review 2](reviews/2026-11-15-bf26-v1-independent-final-re-review-2.md), `READY_FOR_OWNER_COMMIT / INDEPENDENTLY_ACCEPTED`, **FR-V1-01–05 CLOSED**, all [V1-01–19](finite-acceptance-v1.md) finite acceptance criteria qualified.
- **Post-source-publication disposition:** **`INDEPENDENTLY_ACCEPTED / OWNER_PUBLISHED / CLOSED` for V1 only**. Source publication is verified. **This document and navigation sync are a separate subsequent docs-only delivery**, not part of the source commit above. No actual production deployment/cutover is claimed.

## Accepted API, authorization and data contract

| Surface | Accepted behavior |
| --- | --- |
| New route | Exactly one `GET /api/v1/vault/trash`, OpenAPI `listVaultTrash`; no new mutation or other business route. |
| PRIVATE | Valid persisted `PROFILE_PRIVATE` session can read; success **200** with canonical `ApiResponse` containing `data:{items:[...],nextCursor:string-or-null}`, `error:null`, `meta:null`. |
| PUBLIC / anonymous | Both persisted PUBLIC origins are **403 `ACCESS_DENIED` before handler/owner read**. Anonymous, invalid or expired bearer is **401**. Exactly eight previously accepted PUBLIC business GET pairs are retained, with no ninth route. |
| Item disclosure | Each item has **exactly `id`, `entryType`, `deletedAt`**. No title, work content, NSFW details, createdAt/updatedAt, People, tags, rating, favorite, credentials or cross-owner enrichment. Existing Vault five-field DTOs are unchanged. |
| Pagination | `id DESC` (identity order, **not** deletion-time order), deleted-only keyset; default limit **25**, allowed **1–100**, database-fetched at most `limit+1` qualifying rows; no count/offset/unbounded Java page filtering. |
| Cursor | Always-present nullable `nextCursor`; canonical unpadded Base64url UTF-8 encoding of `v1|<positive-id>` (maximum 128 encoded characters), based on the **last emitted** ID. Invalid limits/cursors produce safe **400** before the business browse query. |
| Consistency | **Weak consistency between requests**. Stable membership permits full descending traversal without duplicate/skip; concurrent restore/re-trash/new deletion can change later pages. No cross-request snapshot or exactly-once promise. |
| Failure/cache behavior | Sensitive success and applicable errors have `Cache-Control: no-store`. Genuine owner/database/transaction-completion faults return safe canonical **500** and `data:null`, not empty success, input 400 or lifecycle 409. |
| Compatibility | Trash-by-ID, restore-by-ID, their idempotency/guards/timestamps, metadata rules, active-only Search exclusion and security/profile configuration remain unchanged. No hard delete or purge. |

The owner-local, read-only service materializes immutable data for the response. No schema/index, Flyway/DBML, root API/advice/OpenAPI, auth/session, module, provider, dependency, Frontend or RAG change is authorized or claimed by this delivery.

## Verified publication boundary

The actual GitHub source commit has **24 changed files**: the exact **13 authorized Java files** (nine production, four existing tests) and **11 scoped implementation/executable/review/ACTIVE documentation artifacts**. The four new production classes live in existing Vault packages. The published code boundary matches the [Owner Authorization](owner-authorization-bf26-v1-2026-11-15.md) and [independent review](reviews/2026-11-15-bf26-v1-independent-final-re-review-2.md).

- **Production (9):** `VaultEntryOperations`, `VaultEntryService`, `VaultEntryRepository`, new `VaultTrashCursor`, new `VaultTrashPageView`, `VaultController`, new `VaultTrashItemResponse`, new `VaultTrashPageResponse`, and `VaultWebMapper` in their approved Vault paths.
- **Test (4):** `VaultEntryIntegrationTest`, `VaultWebIntegrationTest`, `ProfileRouteAuthorizationIntegrationTest`, `OpenApiRouteInventoryIntegrationTest`.
- **Frozen:** existing `SecurityConfiguration`, root `OpenApiConfiguration`, global response/advice, Vault entity/legacy DTO, all other Java tests, source reservations, indexes/schema, module/descriptor/build/dependencies.

The review qualified **13/13 final source/test hashes**, actual read/query/cache/security and generated OpenAPI evidence. The review's inspection does **not** substitute for an unrun production rollout or whole-application qualification.

## Independent verification and evidence accounting

**112 distinct accepted passing invocations across nine classes = 49 reviewer fresh + 63 source-applicable reused**; earlier overlapping initial/R1/R2 runs are *not additional cases*.

| Provenance | Count | Evidence |
| --- | ---: | --- |
| Reviewer-fresh `VaultWebIntegrationTest` | 7 | HTTP success/errors, no-store and observed `beforeCommit` completion-phase rollback/failure |
| Reviewer-fresh `VaultEntryIntegrationTest` | 13 | PostgreSQL keyset/binds, captured SQL/EXPLAIN, canonical cursor and committed lifecycle |
| Reviewer-fresh security group | 28 | `ProfileRouteAuthorizationIntegrationTest` **16** + `SecurityFilterChainIntegrationTest` **12**, both admitted and denied token paths |
| Reviewer-fresh `OpenApiRouteInventoryIntegrationTest` | 1 | Generated route and schema, exact nullable/required item/page shape |
| Reused source-qualified architecture | 38 | `ApplicationArchitectureTests` |
| Reused source-qualified regression | 25 | `VaultMutationConcurrencyTest` **13**, `VaultMetadataIntegrationTest` **11**, and `GlobalSearchIntegrationTest#verifiesRecycleBinExclusion` **1** |

Reviewer evidence: `logs/bf26-v1/reviewer-r2-20261115-309c2f1f2a/`. Producer round-2 evidence: `logs/bf26-v1/impl-20261115-r2/`. Verification used existing bounded 300-second commands, real PostgreSQL Testcontainers/MockMvc and source-applicable reused results. The historical failed compile/timeout/debug attempts and earlier handbacks/reviews remain preserved and must not be relabelled as PASS.

**NOT_VERIFIED:** full application suite, full IDE diagnostics, performance/latency on actual Owner-scale data, production DB/data suitability, live deployment, provider activation or cutover. No additional runtime test is claimed for this documentation-only closeout.

## Findings, backlog and counters (retain without reopening)

| Identifier | Disposition |
| --- | --- |
| `FR-V1-01–05` | **CLOSED** by independent final re-review 2. |
| `BFV1-L01` | **LOW / OPEN / BACKLOG**. T02's inactive-transaction-synchronization fallback could mark the completion hook as executed in the wrong phase if transaction wiring later regressed. Current observed `beforeCommit` phase is independently proven. Defer fail-closed fixture cleanup to a separately authorized test-hygiene slice; no extra remediation round here. |
| `BFV1-L02` | **LOW / OPEN / BACKLOG**. Author R2 handback SEC prose says 19+9 rather than actual **16+12=28**, and misstates three production signatures. The independent review supplies correct identities; preserve the original dated handback and correct later only under a separately scoped docs task. |
| V1 remediation/takeover | **2/3 consumed / STOPPED**; third round is not automatically invoked. **Takeover NOT_AUTHORIZED / NOT_USED**. |
| RO-B | **OWNER_PUBLISHED / CLOSED**, **114** cases (25 fresh + 89 reused), **2/3 STOPPED**, `DPROB-L01/L02 LOW/OPEN/BACKLOG`. |
| RO-A | **OWNER_PUBLISHED / CLOSED**, **1/3 STOPPED**, `DPROA-L01 LOW/OPEN/BACKLOG`. |
| Previous W1/B1/DP-A | W1 **2/3 STOPPED**, B1 **3/3 STOPPED**, DP-A **3/3 exhausted / takeover 1/1 accepted**; existing LOW backlogs retained without recalculation. |
| Other programs | H1-F **PAUSED / NOT_INDEPENDENTLY_ACCEPTED**, FR-13 still blocked; P17 FROZEN, P18 CLOSED, S19-2 SUSPENDED / NOT_ACCEPTED, indexing inactive and providers OFF/unset. |

## Docs Sync Check and next authorization boundary

- **UPDATED by this separately authorized docs-only package:** this new publication closeout; **current-first** navigation notices in root `AGENTS.md`, root `README.md`, Owner Delivery Policy, roadmap and maintenance index; and the front current status of `docs/implementation/handoffs/ACTIVE.md` to **`NO_ACTIVE_HANDOFF`** with a clear historical divider below it.
- **ALREADY_CONSISTENT:** actual accepted V1 source commit/parent; Owner-ratified nine-production/four-test allowlist; 112-case acceptance; eight PUBLIC GET permission boundary and earlier RO-B publication; counters and history.
- **PRESERVED:** original V1 preparation, Owner Authorization, executable, R0/R1/R2 handbacks and self-reviews, original independent reviews, raw evidence including failures/timeouts; RO-A/RO-B/predecessor history; prepublication ACTIVE material preserved as an explicitly **historical** checkpoint.
- **DEFERRED_OUT_OF_SCOPE:** the two V1 LOW backlog repairs, historical/repo-wide docs rewrite, UI, RAG, later maintenance/features, schema/index tuning and provider deployment.
- **NOT_VERIFIED:** any new application runtime test from the docs-only edit, production cutover, complete suite or real Owner-data suitability.

`ACTIVE = NO_ACTIVE_HANDOFF` after Owner source publication. This closeout **does not approve or select the next slice**, reinstate frontend/RAG, open a remediation budget or authorize code/tests/deployment. Subsequent work needs its own Owner selection, preparation/review, exact authorization and executable.

**Next step:** Owner | review, commit and push this **separate docs-only publication closeout** | verified V1 source SHA `54b62627d7d937e7a95bcca7629c9f0e26786e8f` + independent final re-review 2 | V1 `OWNER_PUBLISHED / CLOSED`, no new implementation authority, `ACTIVE=NO_ACTIVE_HANDOFF` | remediation **2/3 consumed / STOPPED**, takeover **NOT_AUTHORIZED / NOT_USED**.
