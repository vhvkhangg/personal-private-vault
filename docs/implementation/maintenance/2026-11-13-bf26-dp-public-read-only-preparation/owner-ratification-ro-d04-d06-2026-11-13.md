# BF26-DP-RO — Owner ratification of RO-D04 and RO-D06 (2026-11-13)

**Decision date:** 2026-11-13. **Basis:** Owner explicitly approved both RO-D04 and RO-D06 as presented after [independent pre-handoff review 1](reviews/2026-11-13-bf26-dp-ro-independent-pre-handoff-review.md) reported `CHANGES_REQUESTED / FR-RO-PH-01 MEDIUM / OWNER_DECISION_REQUIRED`. The earlier [product-policy approval](owner-policy-ratification.md) remains in force.

**Verified source baseline (not changed by this ratification):** GitHub `main` [`3ce4c87d6808c039ffd35ebd7ed67d73659b092c`](https://github.com/vhvkhangg/personal-private-vault/commit/3ce4c87d6808c039ffd35ebd7ed67d73659b092c), docs closeout of accepted W1. The six-document original RO preparation and independent review are local preparation history, not committed source. No new implementation/runtime result is claimed.

## RO-D06 — binding slice order: A first

**Selected Option A:** Prepare/review/authorize/implement **RO-A independently first**. RO-A only revokes PUBLIC business mutation `POST /api/v1/films`, restores the exact B1 eight-PUBLIC-business-GET allowlist, adjusts the affected registered-handler deny sweep and OpenAPI POST contract, and preserves PRIVATE Film POST and all existing eligible Fiction/Film GET six/seven-field payloads *unchanged*.

**RO-B is deferred** to its own separately qualified preparation, independent pre-handoff review, exact frozen-source/API authorization, implementation handoff and acceptance, **after** RO-A publication. No combined RO-A/RO-B handoff is selected. B2-W2 Public writes, B2-S/V, DP-C and other modules remain unauthorized. Approval of product-level RO-B field choices below does not move RO-B into the current implementation scope.

## RO-D04 — binding future RO-B PUBLIC/PRIVATE wire contract

For an eligible PUBLIC Fiction/Film direct-detail GET in **future RO-B**:

| Profile + endpoint | Exact successful `ApiResponse.data` object | Wire preservation |
| --- | --- | --- |
| PUBLIC `GET /api/v1/fictions/{id}` | All **18 existing** `FictionResponse` fields, **plus exactly one `authorName`** field = **19 properties** | `authorName` is always a present, nullable scalar string; no recursive Person/Group object |
| PUBLIC `GET /api/v1/films/{id}` | All **18 existing** `FilmResponse` fields, **plus exactly one `directorName`** field = **19 properties** | `directorName` is always a present, nullable scalar string; no recursive Person object |
| PRIVATE Fiction/Film GET, POST and PUT | Existing **18-property** `FictionResponse`/`FilmResponse`, without either new name field | Preserve current field set, values, validation, headers, errors, semantics and generated PRIVATE OpenAPI schemas |

- **Author source:** exactly one existing Fiction reference, `authorPersonId` or `authorGroupId`; use only that referenced source through legal named capabilities, never a PUBLIC People API. If Person, require its Vault identity to exist, be correct type `PERSON`, and be active/not trashed before disclosing `.name`. Creator Group is **not** Vault-backed; resolve via its named capability, without inventing a group Vault/trash requirement.
- **Director source:** optional `directorPersonId`, similarly require a correct, active Person Vault identity before returning only `.name`.
- **Null semantics:** absent optional director; absent, missing, trashed, wrong-Vault-type or otherwise ineligible referenced Person; missing Creator Group; or absent/blank referenced name → **the name property is present with JSON `null`**. No fallback to unrelated names, no entire People/Group DTO and no availability/status oracle through HTTP. Fiction's domain-required author relation is not made optional by this read-failure policy.
- **Execution errors:** an actual database/lookup/infrastructure failure (as distinct from a normal absent/ineligible reference) produces canonical **safe 500**, `ApiResponse.data = null` and `Cache-Control: no-store`. Do not convert infrastructure failures into a `null` name, expose IDs/internal errors, or retry via unqualified PRIVATE reads.
- **Ordering/snapshot:** confirm Fiction/Film itself is eligible (`isNsfw=false`, existing correct-type active/nontrashed Vault identity) **before** any Person/Group name access. Materialize all 18 work fields, classifications, eligible name and qualification in the same feature-owner `REQUIRES_NEW / REPEATABLE_READ` (or source-proven equivalent) snapshot. Hidden/missing/trashed/wrong-type work remains identical 404/no-store and must not trigger name access. The accepted D10 coherent earlier-snapshot completion rule remains; no retroactive cancellation is promised.
- **Implementation shape:** preserve shared existing PRIVATE 18-field DTOs. A dedicated PUBLIC-only 19-field response/projection (or an equivalently isolated serializer contract) must not change PRIVATE wire/OpenAPI. Exact Java class naming and read implementation remain subject to later scoped preparation/review, not granted here.
- **Access policy:** showing a scalar name in an eligible work does not grant `GET /api/v1/people/**`, other module details, group members or recursive follow-up reads. Owner-approved `review`, progress and relation-ID values remain part of the 18 common fields; NSFW/People/Personal/Finance remain protected as previously decided.

## Closure and authorization boundary

These decisions resolve the **Owner decision part** of `FR-RO-PH-01`. The finding is **not independently CLOSED** until Codex separately re-reviews corrected RO-A preparation and records its disposition. Re-review can declare **RO-A preparation READY_FOR_HANDOFF** while RO-B remains deferred and **not independently ready**. No RO source changes, handoff creation, `ACTIVE` edit, PostgreSQL/HTTP tests or publication are authorized by this dated ratification.

Preserve all predecessor independent reviews/evidence and counters: RO remediation **0 consumed**, takeover `NOT_AUTHORIZED`; W1 remediation **2/3 consumed / STOPPED**, takeover `NOT_AUTHORIZED`; B1 **3/3 / STOPPED**; DP-A **3/3 exhausted** plus accepted exceptional takeover **1/1**. DPA-L01, DPB1-L02, DPB2W1-L01 LOW backlogs remain open. Providers, Frontend pause and RAG suspension unchanged.

**Next step:** Owner → independent Codex | re-run `$codex-pre-handoff-review` for **BF26-DP-RO-A ONLY** against this ratification, original review 1 and the bounded corrected documents | `FR-RO-PH-01` closure decision / preparation `CHANGES_REQUESTED` until re-review | after independently ready, Owner publishes preparation, then grants exact RO-A frozen-source/API permission before `$codex-create-handoff`.
