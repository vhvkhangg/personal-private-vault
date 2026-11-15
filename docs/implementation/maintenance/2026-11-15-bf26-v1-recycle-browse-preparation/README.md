# BF26-V1-RECYCLE-BROWSE — bounded preparation (docs-only)

**Preparation date:** 2026-11-15. **Repository:** `vhvkhangg/personal-private-vault`.

**Verified published baseline:** GitHub `main` [`2d088445e6323b091741cf370e1e0c8cf13928a2`](https://github.com/vhvkhangg/personal-private-vault/commit/2d088445e6323b091741cf370e1e0c8cf13928a2), `docs(backend): close BF26-DP-RO-B publication and sync navigation` (2026-11-15 05:15:54 UTC). Published RO-B source: [`0f3365de888ab03b0d1de6b32e56a3bb7ad96d5a`](https://github.com/vhvkhangg/personal-private-vault/commit/0f3365de888ab03b0d1de6b32e56a3bb7ad96d5a).

**Owner decision in this conversation:** `BF26-V1-RECYCLE-BROWSE` is **SELECTED AS NEXT BACKEND SLICE**; the Owner has **APPROVED this preparation draft for independent pre-handoff review** on 2026-11-15. See [Owner preparation approval](owner-approval-for-pre-handoff-review-2026-11-15.md). This authorizes reviewing the prepared proposal, **not** ratifying its proposed detailed contract (metadata fields, ordering, pagination), exact frozen-source/API/test exception, worktree/branch, executable handoff, implementation, runtime tests, or publication. These must not be inferred from preparation approval.

**Current preparation state:** `READY_FOR_HANDOFF — TECHNICAL PREPARATION ONLY / OWNER_CONTRACT_AND_EXACT_SOURCE_APPROVAL_PENDING`, following the [independent pre-handoff review](reviews/2026-11-15-bf26-v1-independent-pre-handoff-review.md). No blocking preparation finding; the review qualifies **nine production / four test** candidate paths and required carry-forward precision. Product choices and source exceptions below remain unratified. [Canonical ACTIVE](../../handoffs/ACTIVE.md) remains **`NO_ACTIVE_HANDOFF`** at the verified baseline. The original draft and Owner approval record are preserved apart from current status/next-action overlays in this README and the [review workflow](review-workflow-and-status.md). No ACTIVE, source, test, harness, root navigation or historical acceptance record is modified; no agent commits/pushes. V1 runtime tests **NOT_RUN**; implementation/remediation **NOT_STARTED / 0 consumed**, takeover **NOT_AUTHORIZED**.

## Goal and smallest meaningful slice

Add **one PRIVATE-only, read-only** collection API for browsing existing soft-deleted Vault entries without knowing their IDs. Restore-by-ID and trash-by-ID are already implemented; browsing is missing. Prefer reuse of the existing Vault entry owner, repository and REST transport; **do not** introduce a global search, cross-module titles, permanent deletion, a shared pagination framework, or a new module.

**Proposed endpoint:** `GET /api/v1/vault/trash?limit=25&cursor=...`.

- `PROFILE_PRIVATE`: authenticated read with a bounded identity-only page.
- `PROFILE_PUBLIC`: **403 `ACCESS_DENIED`** before the controller, because only eight existing B1 PUBLIC business GET method/path pairs are permitted; the new Vault route is **not** a ninth permission.
- Anonymous / invalid token: canonical 401; existing Auth/profile exceptions remain unchanged.
- No source changes to `SecurityConfiguration` should be needed: its current `anyRequest().hasAuthority("PROFILE_PRIVATE")` covers the new route. Verify with real HTTP tests before acceptance.

## Source-grounded state vs candidate decisions

| Item | Verified at published HEAD | Proposed change / pending qualification |
| --- | --- | --- |
| Vault lifecycle | `VaultEntryOperations` exposes `find`, `findAll(ids)`, `moveToTrash(id)`, `restore(id)`; `VaultController` implements ID-based GET/DELETE/restore | One **new paged trash read**; retain all existing operations unchanged |
| Storage | `vault_entries` already includes `id`, `entry_type`, `created_at`, `updated_at`, nullable `deleted_at` | Query rows with `deleted_at IS NOT NULL`; no new table assumed |
| PUBLIC/PRIVATE security | Eight named PUBLIC GET pairs; other business routes PRIVATE by fallback | PRIVATE-only new route, not a blanket authenticated/Public grant |
| Existing paging | Feed inbox has an owner-local opaque cursor plus `items`/`nextCursor`; root `ApiPageMeta` models offset, not cursor | Reuse conceptual shape, but implement bounded Vault-local cursor rather than extend offset metadata or import Feed internals |
| Proposed response | Existing `VaultEntryView` has five metadata fields | Prefer **exactly three** output fields `id`, `entryType`, `deletedAt` (subject to Owner ratification and independent review) |
| Ordering | No trash collection endpoint currently exists | `id DESC` keyset (not deletion-time order); limit 25 default, 1–100 range, at most limit+1 rows |

**Owner choices still open at the next authorization gate:** accept metadata-only (without title), `id DESC` instead of deletion-time ordering, proposed `items`/`nextCursor` envelope and frozen Phase 1/13 additive exception; also settle the worktree/branch question separately if desired. The [earlier Backend-first assessment](../2026-11-11-backend-first-assessment/backend-readiness-assessment-2026-11-11.md) recommended but did not itself authorize these changes. This preparation makes the assumptions explicit and reviewable; it does **not** silently ratify them.

## Contents

| File | Purpose |
| --- | --- |
| [Binding candidate and security](candidate-http-and-privacy-contract.md) | Exact **proposed**, not yet approved, PRIVATE HTTP wire, cursor, auth and concurrency semantics |
| [Source baseline and risks](source-baseline-and-risk-inventory.md) | Git-blob-qualified current code and likely seams, no claimed runtime tests |
| [Candidate change boundary](candidate-source-test-map.md) | Minimal **proposed** production/test paths, exclusions and frozen-source decision gate |
| [Finite acceptance](finite-acceptance-v1.md) | 19 finite V1 criteria, meaningful PostgreSQL/HTTP/regression verification |
| [Review workflow](review-workflow-and-status.md) | Review/Owner gates, evidence hygiene, Docs Sync and preservation |
| [Owner preparation approval](owner-approval-for-pre-handoff-review-2026-11-15.md) | Explicit permission for preparation review only; source/API/test authority withheld |
| [Independent pre-handoff review](reviews/2026-11-15-bf26-v1-independent-pre-handoff-review.md) | Technical preparation ready; qualified map, future evidence gates, coverage and pending Owner decisions |

## Preservation and next gate

BF26-DP-RO-B remains `INDEPENDENTLY_ACCEPTED / OWNER_PUBLISHED / CLOSED`; final reviewer **25 fresh + 89 qualified reused = 114 distinct** accepted cases, remediation **2/3 STOPPED**, takeover `NOT_AUTHORIZED/NOT_USED`; `DPROB-L01/L02 LOW/OPEN/BACKLOG` remain. RO-A is also published/closed, remediation **1/3 STOPPED**, with `DPROA-L01` open LOW. Earlier DP-A/B1/W1 status/counters/backlogs, paused Frontend H1-F/FR-13, frozen P17, closed P18, suspended S19-2 and providers OFF/unset remain unchanged. No production deployment or real Owner-data readiness is claimed.

**Next step: Owner | ratify the reviewed V1 contract and exact nine-production/four-test exception, then commit/push preparation | BF26-V1 only + independent review/candidate map | product/source decisions and publication before Codex `$codex-create-handoff`; ACTIVE remains NO_ACTIVE_HANDOFF | V1 remediation NOT_STARTED / 0 consumed; takeover NOT_AUTHORIZED.**
