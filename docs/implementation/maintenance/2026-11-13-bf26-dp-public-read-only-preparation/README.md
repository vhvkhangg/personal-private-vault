# BF26-DP-RO — Public read-only policy realignment: preparation

**Date:** 2026-11-13
**Verified published baseline:** GitHub `main` [`3ce4c87d6808c039ffd35ebd7ed67d73659b092c`](https://github.com/vhvkhangg/personal-private-vault/commit/3ce4c87d6808c039ffd35ebd7ed67d73659b092c), `docs(backend): close BF26-DP-B2-W1 publication and sync navigation`.
**Decision status:** `READY_FOR_HANDOFF_RO_A_PREPARATION_ONLY / FR_RO_PH_01_CLOSED / OWNER_RATIFIED_RO_D04_RO_D06 / RO_B_DEFERRED / NOT_AUTHORIZED_FOR_IMPLEMENTATION`. [Independent RO-A re-review 1](reviews/2026-11-13-bf26-dp-ro-a-independent-pre-handoff-re-review-1.md) CLOSED FR-RO-PH-01 at preparation level, with no blocking RO-A preparation finding. [Initial review](reviews/2026-11-13-bf26-dp-ro-independent-pre-handoff-review.md) remains historical CHANGES_REQUESTED; [dated Owner ratification](owner-ratification-ro-d04-d06-2026-11-13.md) and bounded correction resolve its contract/scope ambiguity. This is not runtime acceptance, source authorization or RO-B readiness.
**Execution:** `ACTIVE = NO_ACTIVE_HANDOFF`, as published in [`ACTIVE.md`](../../handoffs/ACTIVE.md). No source/API/schema/flag permission or executable handoff is created by these documents.

## Why this replaces the un-applied B2-W2 draft

The Owner did **not** apply `personal-private-vault-bf26-dp-b2-w2-preparation-draft-20261113.zip`. After that draft, the Owner revised the product policy:

1. `PROFILE_PUBLIC` can **read** selected nonsensitive content modules; it cannot create, update, delete or otherwise mutate any business/data resource.
2. `PROFILE_PRIVATE` retains its existing authorized reads/writes; profile separation is not multi-user tenancy.
3. For an eligible non-NSFW Fiction/Film, `PROFILE_PUBLIC` may see **the complete current FictionResponse/FilmResponse**, including review, personal reading/viewing progress, ratings/status, relationship ID fields and other current DTO properties. These are NOT sensitive solely because they reflect the Owner's media consumption.
4. NSFW works, all direct People/Personal/Finance and other private-fixed modules remain unavailable to Public. The Owner accepts that **names disclosed in an authorized Fiction/Film detail** (e.g. author/director from People/Creator Group) may reveal that specific work-to-person association. That does not authorize browsing People or its unrelated fields.
5. The present source contradicts rule 1 because B2-W1 already published authenticated PUBLIC `POST /api/v1/films`; a **new corrective slice must explicitly revoke** that matcher. Historical accepted B2-W1 evidence/status must remain historically truthful.

This is an Owner-approved policy change **prospectively**, not a claim that current code already implements it, nor a blanket approval of all module reads. The new work is called **BF26-DP-RO**, not B2-W2. The prior W2 draft's proposed D18–D24 are NOT applied, ratified or reusable as authority; this docket uses namespaced **RO-D01–RO-D07**.

## Owner-ratified delivery order (RO-D06)

| Candidate | Goal | Current authority |
| --- | --- | --- |
| **RO-A — current review target** | Revoke the sole PUBLIC business write (`POST /api/v1/films`), preserve PRIVATE writes, eight existing PUBLIC GETs with **their unchanged B1 six/seven-key DTOs**, update exact deny sweep and POST OpenAPI | Owner-selected **first**; **READY_FOR_HANDOFF at preparation level** by independent re-review 1; source not authorized |
| **RO-B — deferred future slice** | On eligible Fiction/Film GETs, PUBLIC returns all 18 current shared work fields **plus exactly one nullable `authorName`/`directorName` key (19 total)**; PRIVATE GET/POST/PUT stays exactly 18 | RO-D04 wire choice ratified for a **later separate** preparation/review; no current source or handoff grant |
| **RO-C (separate future)** | Any additional Public content module/read method or search/list/browse surface beyond the eight accepted B1 GET routes; enumerate routes and field/NSFW/relations first | NOT_SELECTED / NOT_AUTHORIZED |

**RO-A must be independently reviewed, authorized, implemented, accepted and published first.** There is **no combined RO-A/RO-B handoff option selected**. The independently reviewed baseline for RO-A must keep current B1 GET wire responses unchanged. RO-B is an Owner-ratified *future contract decision*, not independently ready preparation or permission to implement. No automatic W2/S/V/DP-C handoff follows.

## Documents and scope

| File | Purpose |
| --- | --- |
| [Owner policy decision](owner-policy-ratification.md) | Accepted policy versus remaining exact contract questions and inheritance |
| [RO-D01–RO-D07 decision docket](decision-docket-ro-d01-d07.md) | Owner-ratified RO-D04/D06, exact RO-A boundary and deferred RO-B wire rules |
| [Dated RO-D04/D06 ratification](owner-ratification-ro-d04-d06-2026-11-13.md) | Controlling Owner decision following independent review 1; 19-key PUBLIC/18-key PRIVATE, missing/error/snapshot contract, RO-A first |
| [Source baseline / leakage inventory](source-baseline-and-leakage-inventory.md) | Git blob-qualified source facts, full DTO contents, People/name joins, transaction and denial surfaces |
| [Read-only contract and acceptance](read-only-contract-and-acceptance.md) | Candidate HTTP/DTO/security, 22 finite RO criteria, negative fixtures, real DB proof |
| [Review and publication workflow](review-gates-and-next-steps.md) | Docs Sync, independent review, frozen exceptions and Owner governance |

### Explicit exclusion / stop

- No production/DB/test/OpenAPI code changes by this preparation package. No new PUBLIC authorization without independent preparation readiness, its Owner publication and **separate exact Owner source/API authorization**.
- Do not open People, Personal, Finance, Media, account, feed, Drive, RAG, global Search, generic Vault, related detail/credit/link/classification routes or arbitrary content modules merely because the response DTO is now full.
- Keep `isNsfw=true`, deleted, missing and wrong-Vault-type direct IDs hidden from Public under the accepted B1 policy. No `PUBLIC` wildcard GET, no unrestricted DTO serialization from nested objects, no generic visibility schema column.
- Preserve DP-A/B1/B2-W1 independent reviews and raw evidence, remediation/takeover counters, Frontend H1-F pause, RAG suspension, feature/provider flags OFF/unset, original source acceptance and historical notices. No deployment/cutover is claimed.

**Next step:** Owner → Codex | Owner commit/push independently reviewed RO-A preparation, then give separate exact RO-A frozen-source/API/test authorization; only then `$codex-create-handoff` | BF26-DP-RO-A and [independent re-review 1](reviews/2026-11-13-bf26-dp-ro-a-independent-pre-handoff-re-review-1.md) | `READY_FOR_HANDOFF` for RO-A preparation only, unpublished; `ACTIVE=NO_ACTIVE_HANDOFF`, RO-B deferred | RO remediation 0 consumed, takeover NOT_AUTHORIZED; predecessor counters unchanged.
