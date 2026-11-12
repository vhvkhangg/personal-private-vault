# BF26-DP-B2 — Owner ratification of D12–D17, Option A (2026-11-12)

- **Owner decision:** The Owner explicitly approved **all six D12–D17 decisions, Option A, without amendments** in the conversation after delivery of [BF26-DP-B2 preparation draft](README.md). No further vote is needed to establish those product/scope choices **for preparation**.
- **Source baseline:** GitHub `main` [`9007f212adc91f7c1b5e1a5c466c8a102c13fff3`](https://github.com/vhvkhangg/personal-private-vault/commit/9007f212adc91f7c1b5e1a5c466c8a102c13fff3), after the independently accepted/published [B1 closeout](../2026-11-12-bf26-dp-b1-preparation/owner-publication-closeout-dp-b1-2026-11-12.md) and navigation sync.
- **Authority level:** `OWNER_RATIFIED_D12–D17_OPTION_A_FOR_PREPARATION / INDEPENDENT_PRE_HANDOFF_REVIEW_PENDING / NOT_AUTHORIZED_FOR_IMPLEMENTATION / NO_ACTIVE_HANDOFF`.
- **Controlling design:** [D12–D17 docket](owner-proposed-d12-d17.md), [slice plan and 16 prospective acceptance criteria](slice-boundaries-and-review-plan.md), and [static source/risk inventory](source-baseline-and-risk-inventory.md). Alternatives are nonselected historical options.

## Binding decisions for the first B2 sub-slice

| ID | Approved Option A (preparation design) | Not approved by this decision |
| --- | --- | --- |
| D12 — decomposition | Split B2 into **B2-W1** Film create, later B2-W2 Fiction/Film updates, B2-S Search, and B2-V generic Vault. Prepare/review each independently. | One combined B2 implementation, automatic activation of W2/S/V. |
| D13 — first write | **B2-W1 only:** an authenticated PUBLIC `POST /api/v1/films`, **create-only**, for a new non-NSFW Film. Preserve existing PRIVATE behavior. | PUBLIC Film update, Fiction write, links, credits, genres, classifications or Vault writes. |
| D14 — HTTP fields | Dedicated strict Public create request: **`title,originalTitle,nationalityCode,format,productionStyle`** only. Server sets `isNsfw=false`; extra keys, including client `isNsfw` and all relational/private fields, are rejected. Successful `201` responds using the existing B1 seven-property `PublicFilmResponse` and canonical `ApiResponse`. | Passing the PRIVATE full request/DTO through Public or mass assignment; adding any relation, note, poster or personal field. |
| D15 — references | Defer Fiction create/update and all People/Creator Group/genre reference selection to B2-W2 preparation with separate qualification. | Public probe of author/genre/private IDs or new People/genre endpoints. |
| D16 — Search/Vault | Keep Global Search, generic Vault, tag-origin, credits and metadata **PRIVATE-only** in W1. Prepare Search/Vault, if needed, under their own decisions; safe qualification must precede ranking/top-K/offset/`hasMore`/materialization. | Generic Public search/Vault grants, post-pagination filtering or tag leakage. |
| D17 — approval/test boundary | Independent pre-handoff readiness and later precise Owner frozen-source/API authorization before handoff. Mandatory real persisted PUBLIC login/downgrade tokens, strict request validation, DB atomicity/rollback, secure responses, 401/403/404, ASYNC/ERROR and deny-sweep, preserved PRIVATE, OpenAPI and architecture tests. | Code edits, new active handoff, source/SQL/flag/Frontend changes or a claim of runtime PASS from the static preparation. |

## Nonnegotiable preservation and limits

- **Source and security today:** no new PUBLIC write, Search or Vault permission. Existing B1's eight authenticated GET grants and exact Public DTO privacy/REPEATABLE_READ read boundary remain unchanged. PRIVATE retains its accepted mutations. D1–D11 remain binding.
- **Preparation only:** `ACTIVE=NO_ACTIVE_HANDOFF`. This approval is **not** source/frozen/API authorization, a final independent preparation review, an executable handoff, implementation acceptance, Owner publication of implementation or production deployment. Do not run `$codex-create-handoff` until the separately required gates are satisfied.
- **Future B2 sub-slices:** B2-W2/S/V require distinct, bounded Owner source decisions and independent readiness. No DP-C, Recycle Bin, Frontend/H1-F, RAG, provider activation or final non-RAG audit follows automatically.
- **Counters and historical evidence:** B2 initial work **not started; remediation 0 consumed; no takeover authorized**. DP-A FR-DPA-01–09 CLOSED, DPA-L01 LOW/OPEN/BACKLOG, remediation 3/3 exhausted and exceptional takeover 1/1 accepted. B1 FR-DPB1-01–04 CLOSED, DPB1-L01 CLOSED, DPB1-L02 LOW/OPEN/BACKLOG, remediation 3/3 STOPPED and takeover NOT_AUTHORIZED/NOT_USED. Historical independent reviews, receipts and Frontend/Phase 19 states remain untouched.
- **Known uncertainty:** The existing Film `create` path and Owner data have **not** been runtime-qualified for PUBLIC. Real PostgreSQL transactional insertion/rollback, strict unknown/duplicate-key parsing, correct `201` response and OpenAPI variants are **NOT_VERIFIED** until separately authorized tests. If required behavior cannot be delivered without a broader module/DDL/policy exception, STOP and seek exact Owner permission; do not silently broaden Option A.

## Next gate

**Owner → independent Codex:** After applying these preparation-only Markdown documents in the worktree (before commit/push), request **`$codex-pre-handoff-review`** scoped to **BF26-DP-B2-W1 only**, with this ratification, D12–D17, static source inventory and the W1-01–W1-16 acceptance map. The reviewer must independently qualify source/frozen/API boundaries and the simplest safe create/transaction path, record findings/readiness, and preserve `NO_ACTIVE_HANDOFF`. Only after independently `READY_FOR_HANDOFF` and Owner publication may a **separate exact Owner implementation authorization** and `$codex-create-handoff` follow.
