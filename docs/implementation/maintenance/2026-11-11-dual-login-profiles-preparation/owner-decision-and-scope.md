# BF26-DP — Owner decision, alternatives and frozen-boundary proposal

## 2026-11-11 PH-01–04 correction and subsequent D1–D6 Owner ratification

The independent preparation [review](reviews/2026-11-11-bf26-dp-independent-pre-handoff-review.md) identified **PH-01 HIGH and PH-02–04 MEDIUM, all OPEN**. The document [D1–D6 proposed decision docket](owner-proposed-d1-d6-decision-docket-2026-11-11.md) is the single source for the proposed exact Public allowlist, field/reference policy, credential compatibility, per-lineage atomic switching, Google enabled-mode restriction, Portability/full-backup distinction and PIN retention. All six decisions in that docket were **ratified by the Owner** in the [dated decision record](owner-ratification-d1-d6-2026-11-11.md), including D1 Public module denials and D4 temporary Google enabled-mode behavior. This is a preparation-level future policy, **not** a modification of frozen source or already accepted schemas; PH-01–04 remain OPEN until re-review. A source/DBML/API/ADR change requires separate Owner approval after the preparation is independently ready.


**Owner's already selected product contract (2026-11-11):** proceed with Dual Profiles, then Recycle Bin browsing, then Final Non-RAG Backend Audit. This is **preparation authorization only**. It supersedes the earlier *priority* that held profile work behind BF26-S5, because BF26-S5 Option A was published at `9c7e123d01237e057675d348ddb93700a4850e51`; the earlier deferred document remains a true historical decision.

## Owner-confirmed behavior (binding for preparation)

- **Exactly one human Owner and one shared logical dataset/DB**. No separate user-owned business records, tenants, workspaces, registration, new public user CRUD or duplicate Data/Drive identities.
- Two **fixed login profiles/credentials** `PUBLIC` and `PRIVATE`, shared business features subject to access rights. `PRIVATE` may access permitted Public and Private data; `PUBLIC` may access only safe Public content and perform permitted Public operations.
- **Private classification is fixed by domain/resource and by existing Fiction/Film `isNsfw`**; not an arbitrary per-record Public/Private checkbox on every module. **All Media and Personal** are Owner-confirmed Private. NSFW Fiction/Film are Private. D1 now ratifies the conservative first-release module classifications; a **route/field-level independent review** remains required before any Public business API is opened, with **fail-closed Public access** until individually qualified.
- `PRIVATE -> PUBLIC`: immediate downgrade without password, **old Private tokens/sessions must be rejected server-side** (in the affected session lineage). `PUBLIC -> PRIVATE`: fresh **Private password** checked by authoritative Spring Authentication; no remembered token, UI toggle or PIN-only elevation. Public and Private credentials must be distinct.
- Preserve any already-accepted additional sensitive-operation controls unless a separately demonstrated equivalent is authorized. The legacy six-digit PIN is **not** automatically removed: V1 has a non-null `pin_hash` and settings has private-mode auto-lock.
- Authentication/authorization may change prospectively after independently approved frozen-boundary maintenance. The published BF26-S5 consumer baseline is not a permanent API freeze; any future actual Frontend-driven change needs its own authorization and compatibility review.

## Architecture alternatives (selected direction; no executable authorization)

| Option | Benefit | Risk | Recommendation |
| --- | --- | --- | --- |
| Two `app_users`/Owner IDs | Familiar account abstraction | Contradicts singleton/FKs, Google identity, global data ownership and frozen DBML; large migration | **REJECT** |
| One login + UI `mode` toggle | Small UI change | No Backend security boundary; old tokens can still fetch Private data | **REJECT** |
| One Owner + Public credential + Private Owner password + server-revocable session | Preserves `app_users.id=1` and data ownership, two real login profiles | Requires explicit profile authorization, schema/session/token evolution and broad negative tests | **RECOMMENDED** |
| Add `visibility` column to every record | Extensible | Against Owner's fixed-classification requirement; schema bloat | **REJECT** |
| Keep 6-digit PIN as sole elevation | Backwards-compatible | Owner expressly selected password-based Private elevation | **REJECT as future elevation**; preserve legacy table/API until safe retirement |

## Explicit non-goals

No custom users, multiple Owners, per-record arbitrary visibility, independent profile data, migration of all tables to owner IDs, organization roles, public anonymous access, passwordless Private escalation, new OAuth provider, Drive slot remapping, live Google/Feed/Drive/RAG activation, frontend/BFF code/UI, Recycle Bin read implementation, or Final Audit execution. No new agent, global harness framework, dependency/hook/trust/pin changes without demonstrated need.

## Existing frozen boundaries affected

- Phase 2 Authentication singleton/bootstrapping, password and PIN API/security contracts, JWT/refresh lifecycle.
- Phase 1 Vault metadata and Phase 12 Search qualification/pagination; Phase 13 HTTP/OpenAPI and API envelope/security; accepted Fiction/Film/Media/Personal/other module reads and mutations.
- Frozen Schema v1 and post-V28 additive migration chain; security exclusions and Portability export allowlist/backup policy; H1-AUTH immutable Google Owner binding and token-issuance semantics.
- Accepted BF26-S5 Dashboard API readiness stays correct for the **historical singleton**. Do not falsely retrofit profile acceptance onto that delivery.

These are **material architectural/security amendments** requiring an explicit addendum/ADR plus Owner approval **before** source changes; do not silently rewrite frozen historical artifacts. Additive authoritative migration comes only after agreement on the schema strategy and rollback/compatibility.

## Ratified consolidated decisions — independent feasibility/review still required

The Owner has now approved **D1–D6 as prospective design choices**. The following six historical questions have been answered by the ratified docket; the reviewer must independently verify the exact operational/ownership/test contract rather than reopen them without a concrete evidenced conflict:

1. **Exact fixed module/endpoint policy** for ambiguous modules (Journal, Finance, Knowledge/Notes, Collection, People, Location, Account, Feed/Inbox/Radar, Import, Settings, Drive, Export, reference data, Search/RAG). The conservative matrix is now approved **as a prospective fixed initial-release policy**, not as deployed or as permission to bypass per-route authorization proof.
2. **Password/session protocol**: proposed existing Owner password = Private; Public distinct credential configured by Private; per-session-lineage downgrade, other device sessions unaffected except security reset. D2/D3 are ratified for preparation; reviewer must verify source feasibility and privacy equivalence.
3. **Google Sign-In**: existing one-owner Google login cannot silently grant Private without the Owner-selected Private-password step. D4 ratifies keeping Google default OFF, denying enabled-but-unmapped token issuance during DP-A, and at-most-PUBLIC future mapping before password elevation; verify accepted H1-AUTH regression impact.
4. **Migration/rollback and legacy compatibility**: Public disabled until configured, Private legacy password emergency path, no credentials in SQL; deny legacy unprofiled JWTs once the new enforcement is on. Exact DDL and wire-contract delta remain subject to independent review and separate frozen-baseline implementation authorization.
5. **PIN retirement**: D6 retains PIN API and columns during DP-A; any later behavior retirement or schema cleanup needs separate authorization. Do not delete PIN features now.
6. **Private expiry and concurrent clients**: immediate per-session downgrade and accepted concurrent in-flight limitation; complete server-backed revocation independent of BFF. D3 selects per-lineage authority only; global-revoke-all is expressly deferred and must not be silently built.

D1–D6 are resolved by Owner. Do **not** mark PH-01–04 CLOSED, preparation READY_FOR_HANDOFF, or implement source until a separate independent re-review determines the contract sufficient. Any newly evidenced gaps must be reported with specific proof.

## Risks / expected benefit

The benefit is a durable server-enforced Public view of the same dataset. The cost is **cross-module security work**, higher than adding a second login screen. Server checks must cover direct IDs, writes, collections, search, counts, descriptions, files/bytes, raw exports, background/derived views, caching and stale tokens; the absence of a generic `NSFW` flag on every module is intentional. This is a high-impact maintenance change rather than a small authentication UI feature.
