# BF26-V1 — finite acceptance matrix (proposal, not executed)

**Status:** `CANDIDATE / RUNTIME_NOT_RUN / NOT_SOURCE_AUTHORIZED`. These are future required checks once exact source/test exceptions and executable handoff are independently ready. IDs `V1-01`–`V1-19` are a proposed reviewable matrix, **not** a count of JUnit test methods, gates or passing cases.

| ID | Mandatory acceptance claim / finite witness |
| --- | --- |
| **V1-01** | Only one new route `GET /api/v1/vault/trash`; no extra mutation/list/search/Public allowance; original Vault ID GET/trash/restore remain identical. |
| **V1-02** | Valid PRIVATE persisted bearer 200; two valid PUBLIC origins (direct login, downgrade) each 403 `ACCESS_DENIED` *before handler*; anonymous/invalid/expired bearer 401. Exactly eight original PUBLIC business GET method/path pairs retained. |
| **V1-03** | Real HTTP success JSON: `ApiResponse.data.items` always list, `nextCursor` present nullable, `error:null` and `meta:null`; each item **exactly** `id,entryType,deletedAt`, valid values, no private or nested fields. |
| **V1-04** | Empty Vault trash returns 200, `items:[]`, `nextCursor:null`; all-active Vault produces identical empty trash result. |
| **V1-05** | Default `limit=25`; explicit 1 and 100 valid; page never exceeds requested limit; more than limit uses at most limit+1 DB rows; no count, offsets or in-memory page overfetch. |
| **V1-06** | Invalid limit 0, -1, 101, non-integer, decimal and huge overflow yield safe canonical 400 before repository work; assert actual existing error code per branch. |
| **V1-07** | Cursor absent begins traversal; canonical versioned Base64url cursor roundtrips; blank, malformed encoding/UTF-8, unknown version, extra parts, zero/negative/nondecimal/overflow ID, excessive length and ambiguous encoding return safe 400. |
| **V1-08** | On 3+ pages of mixed active and deleted entries of multiple Vault types, returned IDs are strictly descending; no duplicates/skips while membership unchanged, and active rows never appear. |
| **V1-09** | `nextCursor` uses last **emitted** ID (not lookahead), is non-null iff another qualifying row exists; exactly-limit page and limit+1 boundary are correct; empty/exhausted pages return null. |
| **V1-10** | Restore between pages removes item from later eligible result; next page uses strict old cursor bound and does not repeat earlier IDs. Document weak cross-request consistency rather than promise snapshot. |
| **V1-11** | Re-trash of existing ID retains accepted lifecycle semantics; concurrent trash/restore cannot cause pagination to write, unbounded loop or a new lock policy; full exactly-once traversal is not asserted under membership changes. |
| **V1-12** | Real PostgreSQL actual SQL uses bounded keyset DB query (`deleted_at IS NOT NULL`, `id < :lastId`, `ORDER BY id DESC`, limit+1) without offset/count or whole-table filtering. Validate representative synthetic sparse-trash plan; if index needed, STOP for separate schema approval. |
| **V1-13** | GET and bad input produce zero writes, external provider calls, jobs, indexing/RAG actions or cross-module source hydration; DB/transaction failures produce canonical safe 500 without internal data leakage. |
| **V1-14** | Existing move-to-trash/restore idempotence, created/updated/deleted timestamps and source reservation/mutation guard behavior remain passing under relevant existing fixtures. |
| **V1-15** | Existing Search active-only exclusion and Vault metadata rules remain unchanged; no return of NSFW titles, person data, source content, tags/favorite/rating/notes. |
| **V1-16** | Generated `/v3/api-docs` includes exactly one new GET with unique operationId `listVaultTrash` (if approved), correct query/DTO/security/200/400/401/403/500 schemas and no unapproved Public security description. Exact route manifest and refs reconcile. |
| **V1-17** | `Cache-Control: no-store` on PRIVATE success and application-generated errors as designed; direct privacy and conditional HTTP tests do not leak cacheable trash metadata. Global security responses are checked without global security rewrite. |
| **V1-18** | Spring Modulith architecture, ADR-0016 root HTTP envelope, package naming, existing dependencies, frozen Phase 1/13 boundaries and zero unrelated source/schema/POM/provider changes are qualified. |
| **V1-19** | Final implementation handback, mandatory Antigravity self-review, independent Codex final review, deterministic bounded execution receipts/XML and exact final file hashes, truthful fresh-vs-reused count, preserved failures and Docs Sync Check. Owner alone publishes after acceptance. |

## Suggested future gates — not run in preparation

| Gate | Selector / scope | Focus |
| --- | --- | --- |
| V1-DB | `VaultEntryIntegrationTest` plus selected new owner tests if reviewer proves required | Keyset, DB limit/plan, id/cursor, lifecycle |
| V1-HTTP | `VaultWebIntegrationTest` | Exact payload, defaults/bounds, cache, valid PRIVATE and negative errors |
| V1-SEC | `ProfileRouteAuthorizationIntegrationTest`, plus source-qualified security controls | PRIVATE-only path, PUBLIC 403 before handler, anonymous 401, eight-route lock |
| V1-API | `OpenApiRouteInventoryIntegrationTest` | Full route inventory and actual generated schema, unique op ID |
| V1-ARCH | `ApplicationArchitectureTests` | Module boundaries/frozen code |
| V1-REG | `VaultMutationConcurrencyTest`, existing metadata lifecycle and relevant selected regressions | Preserve accepted mutation/restore/source guard semantics |

Use repo's existing `scripts/run-bounded-command.py` with **normal 300-second per-command watchdog**, Testcontainers real PostgreSQL and MockMvc, preserved XML/receipts. If a selector exceeds the budget, preserve the failed attempt and split finite disjoint selectors rather than raising timeout or discarding evidence. Runtime tests, query plans and real OpenAPI generation have **not** run in this docs-only preparation; no PASS counts are claimed. Existing approved DP-A/B1/RO-A/RO-B tests may be reused **only** after source/test input applicability is verified and without double counting.

**Special negative:** If the reviewer finds a currently implemented equivalent trash collection endpoint, contradictory authorization or a required feature-title UX contract, STOP and revise preparation; do not create duplicate routes or claim the absent capability solely on document prose.
