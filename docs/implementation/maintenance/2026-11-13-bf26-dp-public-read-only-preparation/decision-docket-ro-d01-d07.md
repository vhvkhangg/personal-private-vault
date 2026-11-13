# BF26-DP-RO — policy decisions RO-D01–RO-D07; RO-D04/RO-D06 Owner-ratified

**Status:** `OWNER_RATIFIED_RO_D04_RO_D06 / RO_A_PREPARATION_RE_REVIEW_PENDING / RO_B_DEFERRED / NO_SOURCE_PERMISSION`. The Owner initially approved the Public-read-only product policy, then explicitly ratified RO-D04 and RO-D06 on **2026-11-13**, after [independent review 1](reviews/2026-11-13-bf26-dp-ro-independent-pre-handoff-review.md) found `FR-RO-PH-01 MEDIUM`. See [dated, binding ratification](owner-ratification-ro-d04-d06-2026-11-13.md). This is **not** authorization to implement or to reopen `ACTIVE.md`. The earlier unapplied B2-W2 D18–D24 draft has no authority.

| ID | Selected decision / policy | Scope and boundary |
| --- | --- | --- |
| **RO-D01** Policy | PUBLIC is authenticated and **business/data read-only** for explicitly chosen routes; PRIVATE preserves accepted actions. | Owner-approved. No blanket GET; Auth/profile flows keep their exact existing exceptions. |
| **RO-D02** Revoke W1 | Remove PUBLIC admission for `POST /api/v1/films` only; PRIVATE POST stays intact. After RO-A, exactly **eight** B1 authenticated GET business pairs remain for PUBLIC. | Owner-approved intent, independently review **RO-A preparation** before exact source/API authorization. |
| **RO-D03** Full work detail | In deferred RO-B, eligible PUBLIC Fiction/Film detail returns **all 18 current top-level shared work properties** including review/progress/relationship IDs, plus only the scalar name selected by RO-D04. PRIVATE retains the original 18-property shape. | Public response becomes **19**, not 18, under D04. All materialization remains inside qualified owner snapshot. No RO-A GET shape change. |
| **RO-D04** Name fields | **Owner-ratified Option A:** PUBLIC Fiction data = 18 common fields + nullable **`authorName`** (Person or Creator Group); PUBLIC Film data = 18 common fields + nullable **`directorName`** (Person) = **19 fields each**. Names always present as string or JSON null; PRIVATE GET/POST/PUT remains original 18 exactly. | Future **RO-B** only. Missing/ineligible Person (wrong/trash/missing Vault), missing Group, absent director, absent/blank name → `null`; real lookup/DB failure → canonical safe `500` with null data/no-store. No direct People grant; see ratification for snapshot and eligibility. |
| **RO-D05** Privacy qualification | PUBLIC eligible work requires non-NSFW + active correct-type Vault in a coherent feature-owner read snapshot. Work absent/hidden/trashed/wrong type → indistinguishable 404/no-store; never enrich People before work eligibility. | Owner-approved. Existing relationship IDs may appear only on eligible work. NSFW, People, Personal, Finance remain blocked; unrelated nested APIs still PRIVATE-only. |
| **RO-D06** Delivery order | **Owner-ratified Option A:** **RO-A first as a standalone revocation slice**, independent prep/review/handoff/implementation/publication. **RO-B deferred** to a separately approved future slice. No combined A+B delivery. | **Current re-review target is RO-A ONLY**. No RO-B implementation, even though D04 wire decision is now specified. |
| **RO-D07** Further modules | Six existing Reference GETs stay unchanged. No extra Fiction/Film lists, Search, People/Group, generic Vault or other content modules admitted by inference. | Each future route/module needs separate source-aware Owner selection and authorization. |

## Binding RO-A boundary for current pre-handoff re-review

- Remove only the W1 PUBLIC `POST /api/v1/films` matcher; leave PRIVATE admission and real `FilmController` create handling operational. Do not redesign security, remove broad PRIVATE DTO, delete Film domain logic or change B1 read responses.
- Reconcile the registered-handler allowlist from **nine to eight** exact business method/path pairs. A valid persisted PUBLIC POST (both login- and downgrade-origin) must be **403 before MVC handler/DB work**, while anonymous remains 401; PRIVATE POST remains a successful ordinary create.
- Generated OpenAPI no longer advertises a PUBLIC-allowed Film create request/response. It must keep the correct PRIVATE create request/response, `createFilm` operation ID, and unchanged GET schema graph. Preserve Auth routes and response/header behavior.
- `RO-05–RO-14` and the *new full GET schema* part of `RO-19` are **DEFERRED**, not current RO-A acceptance. Use old B1 six/seven-key GETs until separately accepted RO-B.
- No source is changed merely because an Owner decision is recorded. Post-review publication and **separate exact frozen-source/API/test authorization** precede any executable handoff.

## Frozen future RO-B wire rules (not current implementation grant)

- PUBLIC Fiction/Film successful GET: **exactly 19** named properties: existing unchanged top-level 18 work fields plus `authorName`/`directorName`, respectively. Each name key is always present, nullable. PRIVATE remains exactly 18, including GET/POST/PUT and OpenAPI schemas.
- For Fiction's existing Person XOR Creator Group author, resolve the scalar via permitted named People/Group capability; for Film optional director resolve Person only. Check Person's Vault identity is active/correct-type before name disclosure; Creator Group is not Vault-backed and has no fictitious trash state. Missing/ineligible reference → present null name; real provider/DB failure → safe `500`, null data, no-store. Do not use catch-all to silently turn infrastructure failure into null.
- Qualify work NSFW/Vault **first** and perform full-field/classification/name reads under one owner-qualified `REQUIRES_NEW / REPEATABLE_READ` snapshot (or demonstrated equivalent). No People API permission, recursive People view, extra field or secondary Public list/search is approved.
- PRIVATE 18-property DTOs should remain unchanged; prefer a PUBLIC-only 19-property type so the W1 `PublicFilmResponse` can remain as historical code until a later separately qualified cleanup. Do not delete type references in RO-A.

## Status and next gate

`FR-RO-PH-01` remains **OPEN in independent review 1** until a new independent pre-handoff re-review explicitly closes it on this bounded correction. No new runtime tests have run and RO-B has no implementation readiness. Preserve historical accepted W1 facts, counters, source SHA and `ACTIVE=NO_ACTIVE_HANDOFF`.

**Next step:** Codex `$codex-pre-handoff-review` for **BF26-DP-RO-A ONLY**, source baseline `3ce4c87d`, this docket, [Owner ratification](owner-ratification-ro-d04-d06-2026-11-13.md), [acceptance map](read-only-contract-and-acceptance.md) and [independent review 1](reviews/2026-11-13-bf26-dp-ro-independent-pre-handoff-review.md). If ready, Owner commits/pushes review-corrected prep then separately authorizes RO-A source/API; **do not create a handoff yet**.
