# BF26-DP-RO-B — PUBLIC full Fiction/Film direct-detail reads: independent preparation

**Preparation date:** 2026-11-14
**Verified GitHub `main` HEAD:** [`83f87ca72a2571ce08e3ee99e78a147e556775ff`](https://github.com/vhvkhangg/personal-private-vault/commit/83f87ca72a2571ce08e3ee99e78a147e556775ff), `docs(backend): close BF26-DP-RO-A publication and sync navigation` (2026-11-14 UTC).
**Accepted RO-A source publication:** [`0956b4962f529050dddf88dc7f2ec19ac571e0d0`](https://github.com/vhvkhangg/personal-private-vault/commit/0956b4962f529050dddf88dc7f2ec19ac571e0d0).
**Current slice state:** `BF26-DP-RO-B / READY_FOR_HANDOFF_PREPARATION_ONLY / NOT_SOURCE_AUTHORIZED / NOT_IMPLEMENTED`. [Independent pre-handoff review](reviews/2026-11-14-bf26-dp-ro-b-independent-pre-handoff-review.md) found no blocking preparation issue and qualifies the preferred **13-production/5-test** candidate map, including the existing PublicFilmCreateIntegrationTest follow-up GET. It also distinguishes required in-snapshot data materialization from pure mapping of captured immutable values after service return. The review refinements must carry into the later exact Owner source/API grant and executable; no runtime or implementation acceptance is claimed.
**Execution:** [`ACTIVE = NO_ACTIVE_HANDOFF`](../../handoffs/ACTIVE.md). Neither this draft nor the earlier RO-D04 ratification authorizes source/API/test edits, an executable handoff, a security grant, or agent git publication.

## Owner-approved product policy, accurately scoped

- `PROFILE_PUBLIC` is **read-only for business data**; `PROFILE_PRIVATE` retains prior permitted mutations. Authentication/session/profile transition endpoints have their own independent rules and are not banned merely because they use POST.
- PUBLIC may read **exactly two eligible non-NSFW active-correct-type Fiction/Film detail GETs** plus the **six existing Reference Country/Language/Currency GETs**. There are eight allowed PUBLIC business method/path pairs in total; all other business methods remain default-denied.
- The Owner **explicitly allows** publication of media consumption progress, `review`, descriptions, relationship IDs and other existing work-detail fields for eligible Fiction/Film. Direct People, Creator Group, Personal, Finance, generic Vault, Search, NSFW and all unselected business routes remain unavailable.
- The binding [RO-D04/RO-D06 Owner ratification](../2026-11-13-bf26-dp-public-read-only-preparation/owner-ratification-ro-d04-d06-2026-11-13.md) selects **19 PUBLIC response fields** (the 18 unchanged existing Fiction/Film fields plus exactly `authorName`/`directorName`, present and nullable), **18 unchanged PRIVATE fields** on GET/POST/PUT, and a separate RO-B preparation following independently published RO-A.
- Accepted RO-A is **CLOSED**. Its independent final re-review passed **36/36 freshly** within a **117-distinct-case** source-qualified union; DPROA-L01 remains LOW/OPEN/BACKLOG; RO-A remediation **1/3 consumed / STOPPED**, takeover NOT_AUTHORIZED. These are historical RO-A outcomes, **not RO-B test results**.

## Precise RO-B change in behavior

| Endpoint | Current on published HEAD | RO-B target, only after future approval/implementation |
| --- | --- | --- |
| PUBLIC `GET /api/v1/fictions/{id}` | Eligible six-key `PublicFictionResponse` | Eligible **19** keys: the exact existing 18 FictionResponse values + nullable `authorName` |
| PUBLIC `GET /api/v1/films/{id}` | Eligible seven-key `PublicFilmResponse` | Eligible **19** keys: the exact existing 18 FilmResponse values + nullable `directorName` |
| PRIVATE Fiction/Film GET, POST, PUT | 18 fields in existing full DTO | **Unchanged exactly 18**, no added name or DTO/schema drift |
| Remaining six PUBLIC Reference GETs | Existing schema, security and content | Unchanged |
| PUBLIC business writes, all other business routes | RO-A default denial | Unchanged; no restored POST Film grant |

**No new endpoint is needed.** RO-B is a deliberate increase of the **fields disclosed by two already-authorized GETs**, not a general access expansion.

## Package contents and control

| Document | Purpose |
| --- | --- |
| [Binding RO-B wire and disclosure contract](binding-contract-ro-b.md) | Exactly 19/18 fields, names, missing/failed references, same-snapshot 404/500/no-store policy |
| [Current source and ownership qualification](source-baseline-and-ownership.md) | Git blob-grounded implementation seams, constraints and current tests; static only |
| [Candidate frozen-source/test map](candidate-change-boundary.md) | Minimal proposed modification paths, no automatic permission; explicit stops |
| [Finite RO-B acceptance matrix](finite-acceptance-ro-b.md) | `ROB-01`–`ROB-22`, HTTP, PostgreSQL, auth, concurrency, OpenAPI and compatibility proof |
| [Review workflow and Docs Sync Check](review-workflow.md) | Independent pre-handoff gate, evidence reuse and next Owner authorization, no implementation yet |

## Current authority and next gate

**PREPARATION ONLY:** this package records the already-ratified product contract and independently qualified design/map. No implementation remediation counter begins or is consumed by static preparation. No Java compilation, MockMvc, PostgreSQL/Testcontainers, generated OpenAPI execution, full IDE scan or live deployment was performed for RO-B. Independent preparation is **READY_FOR_HANDOFF**; Owner publication and the required later exact frozen-source/API/test permission remain pending. This review does not open ACTIVE or authorize implementation.

**Next step:** Owner → Codex | Owner commits/pushes independently reviewed RO-B preparation, then separately approves the exact qualified **13-production/5-test** frozen-source/API map in the [review](reviews/2026-11-14-bf26-dp-ro-b-independent-pre-handoff-review.md); only then `$codex-create-handoff` | BF26-DP-RO-B and review refinements | `READY_FOR_HANDOFF` for preparation only, unpublished; `ACTIVE=NO_ACTIVE_HANDOFF` | RO-B remediation 0 consumed, takeover NOT_AUTHORIZED; predecessor counters unchanged. **Do not start implementation from these documents.**
