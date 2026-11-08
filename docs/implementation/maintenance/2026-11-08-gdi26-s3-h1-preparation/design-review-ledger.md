# Owner visual review ledger — GDI26-S3-H1

**CURRENT:** `DESKTOP_EXPLORER_V0.3_OWNER_APPROVED / D27_DESKTOP_ONLY / D28_SECONDARY_STATES_DELEGATED / BACKEND_GOOGLE_LOGIN_REVIEW_PENDING` on 2026-11-08. Explicit positive Owner approval applies **only to the attached populated desktop Explorer reference**, not H1 implementation. No executable H1 authority.

| Version | Artifact/hash | Coverage | Owner feedback | Disposition |
| --- | --- | --- | --- | --- |
| **v0.3, 2026-11-08** | [Owner-approved desktop Explorer](designs/gdi26-s3-h1-owner-approved-desktop-explorer-v0.3.png), SHA-256 `1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e` | Populated desktop Explorer only; grid+preview, two slots, quota, name search, type/date filters, one “Mới” menu, automatic load-more. Other states still outstanding. | **Explicit Owner:** “Tôi chốt design này nha.” Permits non-pixel-perfect optimization provided required functions remain. | **`DESIGN_APPROVED_DESKTOP_EXPLORER_V0.3`**; H1 `BLOCKED` pending bounded backend API independent review and state coverage; D25=A now chosen |
| **v0.4 DERIVATIVE**, 2026-11-08 | [Nine-state desktop](designs/gdi26-s3-h1-derivative-state-sheet-v0.4-desktop.png) SHA-256 `01b0128ca4f19117727fbf9f1cf320f479d3058b529f4cd3f52edecd7a1e8c16`; [three-state responsive](designs/gdi26-s3-h1-derivative-responsive-v0.4.png) SHA-256 `706bd15a2917e574a1fa9759e163426ececc085cf2d199500ae2108f1e4f6de0` | Login/connection/empty/error/unknown/child/preview + responsive representative, supplement *not replacement* for approved v0.3 | **NOT_RECEIVED**; Owner D28 later **delegated these states to Codex**; no v0.4 approval needed; [decision](owner-decision-addendum-v0.5.md) | `HISTORICAL_UNAPPROVED / OWNER_SIGNOFF_NO_LONGER_REQUIRED` for derivative states |
| v0.2, 2026-11-08 | [Retained prior candidate](designs/gdi26-s3-h1-design-candidate-v0.2-desktop-no-sidebar-no-header.png), SHA-256 `90e880abeeed48fd00c55cdf64b41686f905f5c0ab6f7b671a43985863ec6ecf` | Earlier card-grid desktop draft; historical superseded by v0.3 | Owner requested later changes | `HISTORICAL / NOT_DESIGN_APPROVED` |
| v0.1, 2026-11-08 | [Desktop/mobile presentation board](designs/gdi26-s3-h1-design-candidate-v0.1-desktop-mobile.png), SHA-256 `71a02e391bb7a6bd21429422fdcd7bcfebfc9c0d49498c3f176a8310d1bd1d0b` | Populated list example only, illustrative data. No actual UI states implemented. | **NOT_RECEIVED** | `DRAFT_FOR_REVIEW / NOT_DESIGN_APPROVED` |

## Procedure

1. Present immutable images/prototypes with explicit version/date/hash and a design-state index; Owner may request unlimited iterations. Keep prior rejected versions and wording as dated history; do not overwrite prior files silently.
2. For each revision write Owner feedback **only as actually received**; classify `REVISIONS_REQUESTED`, `PARTIAL_PREFERENCE_ONLY`, or an explicit version-bound positive approval. Approval of a palette or direction alone does **not** equal overall approval.
3. **Current D27/D28 override for H1:** Codex designs representative login/disconnected/reconnect, loading/empty/error, upload unknown/conflict/retry and download states under the H0 harness. Independent review verifies state coverage, desktop 14-inch fit, accessible contrast/keyboard/zoom and exclusions. **No separate mobile or derivative-image Owner sign-off** is required. Real WCAG conformance remains a runtime evidence matter.
4. Only the **Owner** approves/materially changes the main desktop visual, already approved as v0.3. D28 explicitly grants Codex discretion for minor states; these are **not** another Owner approval. Record actual user wording and independent reviewer applicability, never invent derivative-image assent.
5. If a material visual revision occurs after approval, obtain fresh Owner acceptance of the changed artifact version. Do not reuse old approval across materially changed screens.

## Remaining full-H1 approval fields — keep unresolved until actual evidence

| Field | Grounded value |
| --- | --- |
| H1 visual status | `DESKTOP_V0.3_APPROVED / H1_NOT_READY_FOR_IMPLEMENTATION` |
| Approved populated desktop artifact | `v0.3` / `1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e`; [signed reference](owner-visual-approval-record-v0.3.md) |
| Owner-delegated secondary states | D28 Owner message (2026-11-08), no separate image/version approval required; Codex must provide implementation coverage/evidence |
| Semantic colors/typography/spacing/glass/motion token sheet version | — |
| Owner's exact positive approval wording, date and source | `Tôi chốt design này nha` (ChatGPT conversation, 2026-11-08); desktop v0.3 only; other states pending |
| Requested revisions resolved and exclusions | D27 mobile deferred; D28 secondary-state visual discretion; main v0.3 still binding |
| Independent Codex verification, date and applicability | — |
| H0 published + H1 reviewed/published + distinct handoff evidence | — |

**Negative controls:** no reply, “chưa hài lòng”, palette-only praise, agent/self-review screenshot claim, wrong version, missing required states or **material changes to the approved main Explorer** without Owner reapproval => `H1_BLOCKED` for the affected scope. The v0.3 desktop approval remains genuine but cannot authorize backend extensions or unrelated visual surfaces. Do not write an invented Owner approval quote.

## Additional decision event, not a new visual version

2026-11-08: Owner explicitly selected **D25=A**, app-owned direct first-level child folders. No new visual screenshot/version is implied; the exact v0.3 image and immutable hash above remain approved for the populated desktop. [Scope binding](owner-decisions-and-open-questions.md); [unseen state boundary](design-state-coverage-and-approval-boundary.md). H1 overall remains `NOT_READY_FOR_IMPLEMENTATION` until all required independent preparation and version-bound visual evidence is complete.


## Later Owner decision — D27/D28 (2026-11-08)

Owner's latest message explicitly limits H1 to 14-inch desktop and delegates all minor/auxiliary states to Codex under the harness. This changes the **approval model**, not the v0.3 artifact or its SHA. The earlier v0.4 row is retained as historical; it is no longer an Owner pending approval. [Current D26–D30 record](owner-decision-addendum-v0.5.md). The main Explorer remains version-bound Owner-approved; independent H1 implementation review still must test delegated states.
