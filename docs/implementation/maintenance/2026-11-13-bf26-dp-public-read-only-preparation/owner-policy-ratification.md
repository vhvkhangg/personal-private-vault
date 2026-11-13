# BF26-DP-RO — Owner-approved policy, binding meaning and limits

**Owner conversation decision, 2026-11-13:** The Owner explicitly selected policy A and then clarified that Public should see **all existing Fiction/Film response fields**, including Owner-written `review`, progress, consumption state, status and relationship fields. The Owner stated that such media-consumption metadata is **not** the sensitive category being protected. Direct NSFW, People, Personal, money/Finance and similarly sensitive areas are the real privacy boundary. The Owner approved replacing the un-applied W2 preparation with a new read-only policy preparation.

## Ratified product intent (does not authorize implementation)

| Topic | Binding policy |
| --- | --- |
| PUBLIC business writes | **None.** Deny POST/PUT/PATCH/DELETE and other mutating operations regardless of method naming. The B2-W1 POST is an already-published exception that must be removed by a new source-authorized maintenance slice. |
| PRIVATE business actions | Preserve currently accepted Owner-controlled functionality, including Film create/modify, auth, provider flags and business validations. |
| PUBLIC eligible Fiction/Film | Return the **complete existing FictionResponse / FilmResponse shape**, not a six-/seven-property substitute. `review`, consumption/progress information and reference IDs are intentionally disclosed. |
| PUBLIC NSFW | Hidden by content attribute and active, correct-type Vault qualification in a consistent read snapshot. Hidden known ID is observationally indistinguishable from absent. |
| PUBLIC People | **No direct People API**. Inside an authorized Fiction/Film response, limited display name of linked author/director/group is permitted. No People full DTO, notes, birthdays, private profile or unconstrained People lookup. |
| PUBLIC Personal / Finance | Whole modules denied. No balances, spending/income plans, journal/personal profile or other personally sensitive surfaces by inference. |
| PUBLIC other content modules | **Selected routes only**, not read all 21 modules by default. Current selected baseline is two eligible Fiction/Film direct GETs plus six country/language/currency Reference GETs. Additional modules and list/search endpoints require independent selection. |
| Authorization/session | PUBLIC remains authenticated (not anonymous); PRIVATE and PUBLIC are fixed profiles of one Owner, not a second tenant or new public internet/anonymous visitor role. |

## Clarification: full DTO does not mean unrestricted data graph

The approved FULL DTO requirement applies to the **existing top-level FictionResponse/FilmResponse fields** (18 fields each at this source baseline), not recursively exposing full `PersonView`, `CreatorGroupView`, Vault internals, tags, Media bytes, linked NSFW objects, or enabling follow-up routes. IDs already present in the full DTO are permitted to remain visible, but arbitrary lookup of those IDs remains denied. A displayed author/director name is a specifically selected cross-module scalar and not a generic People permission.

Current `FictionResponse` contains `authorPersonId`/`authorGroupId`, **not `authorName`**. Current `FilmResponse` contains `directorPersonId`, **not `directorName`**. **Subsequent Owner decision (2026-11-13):** [RO-D04 and RO-D06 ratified](owner-ratification-ro-d04-d06-2026-11-13.md). For *deferred RO-B*, PUBLIC receives **19** properties (all original 18 plus a present nullable scalar `authorName`/`directorName`); PRIVATE GET/POST/PUT keeps exactly **18** without names. Missing/ineligible referenced People, absent Creator Group or director → null; actual DB/lookup failures → safe 500 with null data/no-store. RO-A is delivered first and **preserves current B1 GET projections** until RO-B separately passes preparation and is implemented. Do not say that simply replacing PublicFictionResponse automatically supplies an author name.

## Historical evidence preserved

- DP-A implementation accepted/published at `5c1da4e5404ee3c724e3fc2e03569d4093bcb44d`; B1 source accepted/published at `277522ee5b89ce5d2d8200cb955654b031b9f97a`.
- B2-W1 accepted/published at `40fb8a61b6529585927e0a7dd524e9a363c69b6e`; closeout current navigation published at `3ce4c87d6808c039ffd35ebd7ed67d73659b092c`. Its source grants exact PUBLIC `POST /api/v1/films`, and the new Owner policy **supersedes this grant prospectively**, not retroactively.
- W1 reviewer fresh 31/31 PASS; 199 distinct accepted cases, not 199+31; FR-W1-01–05 CLOSED; DPB2W1-L01 LOW/OPEN/BACKLOG; remediation 2/3 consumed/STOPPED, takeover NOT_AUTHORIZED/NOT_USED.
- DP-A/B1 LOW backlogs and distinct historical counters unchanged. H1-F remains paused/FR-13 blocked, S19-2 suspended, providers OFF/unset, indexing inactive. Existing tests and deployment evidence do not prove new RO behavior.

**Authority now:** `OWNER_RATIFIED_PRODUCT_INTENT_AND_RO_D04_D06 / FR_RO_PH_01_INDEPENDENT_REREVIEW_PENDING / RO_A_FIRST / RO_B_DEFERRED / RO_SOURCE_AND_HTTP_NOT_AUTHORIZED`. [Independent review 1](reviews/2026-11-13-bf26-dp-ro-independent-pre-handoff-review.md) was `CHANGES_REQUESTED`; new decisions resolve its Owner questions, **not** its independent closure. Exact frozen-source/API authorization remains a separate Owner gate after RO-A pre-handoff readiness and publication.
