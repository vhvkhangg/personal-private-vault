# Owner Decision — BF26-S5 Option A: Minimal Dashboard API Readiness

- **Date / status:** 2026-11-11 / `OWNER_APPROVED / PREPARATION_CORRECTION_ONLY`.
- **Authority:** Owner's explicit “Owner Decision — BF26-S5 Option A Approval & Preparation Correction” instruction in this conversation. This dated record transcribes that decision; it is not an agent-selected proposal or an independent acceptance.
- **Affected scope / version:** [BF26-S5 preparation](README.md), `BF26-S5-A-v1`; latest published local/remote `main` verified at [`bbdcd3e6d7c1eac1d800f95883a5753070d72e9e`](https://github.com/vhvkhangg/personal-private-vault/commit/bbdcd3e6d7c1eac1d800f95883a5753070d72e9e).
- **Objective:** document the smallest first Dashboard consumer package using existing authenticated owner-module reads; correct BF26S5-PH-01–03 and submit for a later independent pre-handoff re-review.
- **Existing behavior / evidence:** the [independent review](reviews/2026-11-11-bf26-s5-independent-pre-handoff-review.md) and its [254-route inventory](reviews/2026-11-11-bf26-s5-static-route-inventory.csv) remain historical evidence. Exact source facts and acceptance are in the [selected matrix](selected-consumer-api-acceptance-matrix-2026-11-11.md). Route presence is not fresh runtime or Frontend acceptance.

## Approved selection and alternatives

| Option | Owner disposition | Code/schema/dependencies now | Tradeoff |
| --- | --- | --- | --- |
| A — Minimal Dashboard API Readiness | **APPROVED for preparation correction** | None | Reuse existing bounded independent reads; expose their actual limits and missing navigation contracts |
| New owner-module APIs or aggregate | NOT_AUTHORIZED | None | Needs a demonstrated selected source gap and separate approval; no speculative Dashboard module |
| Broader cards / dual access profiles / Recycle Bin | DEFERRED | None | Independent backlog, not S5 acceptance prerequisites |

1. **Universal Inbox:** `GET /api/v1/saved-resources/inbox?limit=5`; display title, source, kind and saved timestamp. Use existing item IDs for supported detail navigation. No total-count requirement or new read/archive state.
2. **Combined Daily Trends Radar:** `GET /api/v1/feed/radar/combined?limit=5`; display title, source, publication timestamp and article link. Preserve the rolling 24-hour cached window. `retrievedAt` means retrieval time, not last provider refresh. Never automatically invoke provider refresh.
3. **Vocabulary Due:** `GET /api/v1/knowledge/vocabulary/due?limit=5`; omit cutoff to use the server default. Display word, meaning, language, learning status and next review timestamp. No fabricated total due count, pagination or SRS behavior.
4. **Global Search navigation:** existing authenticated `GET /api/v1/search`; require a valid nonempty query, with consumer default page size **10**. Preserve existing offset/filter/`hasMore` semantics. Navigate only to supported owning-module detail routes; identify missing navigation contracts rather than implementing them silently.

**No other Dashboard card or journey is selected.** Finance, subscriptions, Drive status/quota, general Recent Activity, cross-module totals and new chart/statistics APIs are explicitly deferred.

## Composition, freshness and failure behavior

- Target the **current singleton Owner authentication model** only; reuse existing owner-module read endpoints independently. No new Dashboard aggregation endpoint or application module.
- Fetch when the Dashboard opens or the user explicitly refreshes it; Search still requires a valid query. No new automatic polling, scheduled jobs or provider activation.
- Each preview has independent success, empty, error and unavailable consumer states. One failed read must not invalidate successful independent reads.
- Preserve existing DTOs, `ApiResponse`, authentication and error semantics. Do not fabricate total counts, timestamps, stale-data status, continuation tokens or consistency guarantees. No cross-module transaction or cross-source snapshot consistency is required.

## Future Frontend-driven API evolution

This is a **versioned readiness baseline, not a permanent API freeze**. Future API additions/modifications are allowed after actual Frontend design and implementation needs become concrete, subject to these rules:

1. Preserve existing accepted contracts unless a later approved change is necessary.
2. Prefer backward-compatible additions and existing module ownership.
3. Require an impact assessment for breaking API changes, schema changes, authorization changes and affected consumers.
4. Require separate Owner authorization and appropriate tests/review for later implementation.
5. Do not prebuild speculative endpoints for an unapproved Frontend design.
6. This flexibility neither reactivates paused H1-F nor waives FR-13. Deferred dual Public/Private access profiles and Recycle Bin browsing remain independent Backend backlog items.

## Authorized correction, workflow and stop

Correct PH-01 with the finite scope above, PH-02 with the exact selected source/DTO/consumer/acceptance matrix, and PH-03 with this sequence:

1. Correct bounded preparation docs; synchronize only directly affected current documentation/navigation; run scoped static checks, link validation and Docs Sync Check. **STOP after documentation correction.**
2. A **separate independent Codex session** runs `$codex-pre-handoff-review`. The author does not self-approve PH-01–03.
3. If `READY_FOR_OWNER_COMMIT`, the Owner commits/pushes accepted preparation.
4. If no implementation gap is demonstrated, conclude with a **docs-only API readiness outcome**; do not create an executable handoff.
5. If a concrete source gap remains and the Owner separately approves it, create a new bounded executable handoff **only after preparation publication**.

## Preservation and minimum proof

**Permitted files:** corrected README, this new dated Owner decision, selected matrix, and directly affected current navigation in root AGENTS, Owner policy, roadmap, maintenance index and ACTIVE's planning block. The initial independent review and 254-route CSV must remain byte-for-byte unchanged. [Earlier deferred decision](../2026-11-11-backend-first-assessment/owner-deferred-work-access-profiles-and-recycle-bin-2026-11-11.md) and all historical decisions/reviews/counters remain preserved.

**Excluded:** Backend/Frontend source/tests; dependencies/lockfiles/schema; authentication/PIN/NSFW implementation; Google/Drive/Feed flags; frozen module contracts; H1-F design/evidence/FR-13 disposition; runtime tests/installations/migrations/provider activation; branch/worktree creation. ACTIVE remains `NO_ACTIVE_HANDOFF`. No agent staging, commit or push.

**Minimum current proof:** actual latest published `main`, selected route/operation/DTO/source references, finite acceptance coverage and supported-versus-missing navigation, historical evidence hashes, resolving local links, scoped diff/whitespace checks and the README's Docs Sync Check. Runtime verification is a conditional future plan only. This decision resolves the Owner selection docket; independent finding closure and publication are pending.

**Next step:** separate independent Codex session | `$codex-pre-handoff-review` | BF26-S5 Option A corrected preparation | documentation correction complete before re-review; Owner publication only after independent readiness | no implementation/remediation/takeover round consumed or reset.
