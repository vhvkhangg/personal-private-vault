> **HISTORICAL DESIGN CANDIDATE / SUPERSEDED OWNER SIGN-OFF REQUIREMENT.** The Owner's later **D27/D28** instruction defers mobile and delegates derivative states to Codex; the two v0.4 sheets below are not approved and **do not need a separate Owner approval before H1**. VS-01 will be **Google Sign-In**, not routine password login. Refer to [current desktop/state contract](h1-desktop-and-state-delegation-v0.5.md) and [Google identity contract](h1-google-sign-in-owner-identity-contract-v0.5.md). Preserve past proposal rows for history, not as current gate.

# PH-01 — Concrete derivative-state visual set v0.4 (Owner review requested)

**State: PROPOSED / OWNER_APPROVAL_PENDING.** This is **not** a replacement for [desktop Explorer v0.3, already Owner-approved](owner-visual-approval-record-v0.3.md). Review the new two sheets only. Their approval must be a later explicit Owner message citing `v0.4` or their exact hashes; the assistant/reviewer may not infer it from this file, the screenshot or D23.

| Artifact | Nature and binding |
| --- | --- |
| [Desktop v0.3](designs/gdi26-s3-h1-owner-approved-desktop-explorer-v0.3.png) | **APPROVED**, SHA-256 `1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e` (unchanged). This is the populated desktop Explorer visual target, with proportionate coding tolerance. |
| [v0.4 desktop states](designs/gdi26-s3-h1-derivative-state-sheet-v0.4-desktop.png) | **NOT_APPROVED**; SHA-256 `01b0128ca4f19117727fbf9f1cf320f479d3058b529f4cd3f52edecd7a1e8c16`, 1920×1510. Nine labelled representative states. |
| [v0.4 responsive states](designs/gdi26-s3-h1-derivative-responsive-v0.4.png) | **NOT_APPROVED**; SHA-256 `706bd15a2917e574a1fa9759e163426ececc085cf2d199500ae2108f1e4f6de0`, 1550×950. Three illustrative narrow-screen compositions, **not** a final mobile theme or WCAG validation. |

The two v0.4 sheets are **design aids and behavioral review records**, not runnable source code, test screenshots, backend output, quota proof or a new globally approved PPV design system. Any sample date/file/folder/account/quota text is fabricated demo content. No direct Google API data is shown. No local-folder bulk upload, nested grandchildren or global My Drive search is authorized by decorative icons.

## State-to-Owner approval mapping

| ID | v0.4 visual | Required implementation meaning |
| --- | --- | --- |
| VS-01 | PPV login | Username/email + password, loading, invalid input and expired session; login failure must not disclose credential details. |
| VS-02 | Not connected | Distinct DISCONNECTED slot, explicit connect; don't display another slot's quota/files under it. |
| VS-03 | Reconnect required | Reauthorize required; no silent CONNECTED status or new backend authority. |
| VS-04 | Root pending/uncertain | Pending/reconciliation/error are different from verified READY; no false folder-create success. |
| VS-05 | Empty or no matching results | Empty folder vs filter returned no matches; “Xóa bộ lọc” when applicable. A page that is empty with `hasMore` **is not** terminal empty state. |
| VS-06 | Open child | `PPV → Child`; no grandchild navigation, adoption or cross-slot path. |
| VS-07 | Upload unknown | Retain SAME key/file bytes/name/MIME/parent for explicit reconciliation; no new action/key. Avoid false “Upload thành công”. |
| VS-08 | Download failed | Explicit transport failure or safe retry; do not promise the browser saved a file merely because it started downloading. |
| VS-09 | Thumbnail missing/loading | Safely display a file-type fallback when Google returns no safe thumbnail; no raw provider URL/token. |
| VS-10 | Responsive drawer | Primary/secondary sidebar collapses to accessible navigation, focus return after close, keyboard/escape. |
| VS-11 | Responsive file list | One-column cards/list and usable slot switch, type/date filters and “Mới”; no horizontal overflow in surrounding UI. |
| VS-12 | Responsive unknown | Upload outcome ambiguity remains visible on small screens; session/slot switch cancels stale projections. |

Supplemental **written-only acceptance** (not visually separate screens, because adding nine more decorative screens offers little benefit): connection callback fixed-text success/failure, quota absent/unlimited/pooled, folder create unknown and retry, invalid date range, cursor expiry/incomplete search, authorization 401/403, download abort, keyboard/focus/high-contrast/reduced-motion and 320 CSS-pixel reflow. Each must have tests in H1-F; Owner may ask for another visual sheet for any of them before approval.

## Proposed semantic token palette (not final approval)

| Token / candidate | Value | Intended usage |
| --- | --- | --- |
| Background `--ppv-bg` | `#0A1128` | Dark-first canvas |
| Solid fallback `--ppv-surface` | `#172447` | Opaque accessible fallback for blurred glass |
| Elevated fallback `--ppv-surface-raised` | `#1B2B4A` | Cards, overlays and menus |
| Primary text | `#F7F9FF` | Main text |
| Secondary text | `#C7D4EF` | Metadata and secondary labels |
| Primary solid action | `#3558C5` | Text on action must satisfy contrast |
| Secondary violet action | `#4F3AA5` | Decorative/secondary state |
| Focus stroke | `#8FD3FF` | Strong visible focus on dark surface |
| Motion | 150–250 ms | Favor opacity/transform; honor `prefers-reduced-motion` |
| Glass | selective, with solid fallback | No mandatory full-viewport blur, no low-contrast text on raw gradient |

Contrast **token-pair calculations**, not measured glass surfaces: primary `#F7F9FF` on `#172447` ≈ **14.46:1**; secondary `#C7D4EF` on `#172447` ≈ **10.21:1**; primary on solid action `#3558C5` ≈ **5.95:1**; focus `#8FD3FF` on `#172447` ≈ **9.37:1**. Formula: WCAG 2.x sRGB relative luminance, `(Llighter+0.05)/(Ldarker+0.05)`; rounded values. These calculations **do not prove WCAG 2.2 AA** on translucent, composited, hover, disabled, error or live browser states. Actual contrast, focus and 320 CSS-pixel reflow remain H1-F tests; `GDI26-S3-H0-PH-I01` remains OPEN until evidenced.

## Owner visual-review protocol

1. Retain D23 desktop v0.3 as already accepted. Owner sees and comments on **only v0.4 derivative states**; may request any number of revisions.
2. Owner reply must identify *both* state sheets or specific accepted subset and their version/hash, plus any exact exclusions. An explicit “Tôi duyệt toàn bộ v0.4” after viewing both suffices if both immutable hashes are in the preceding docket. If Owner wants to defer mobile, record a precise exception with separate H1-F approval gate, **do not** silently mark an unseen mobile screen approved.
3. Record dated Owner wording/source and independent reviewer applicability. Design review outcome until then is `REVISIONS_REQUESTED_OR_APPROVAL_PENDING`; never prefill `DESIGN_APPROVED`.
4. Material changes to any approved artwork/state semantics require a new derivative version and renewed Owner approval of those changes. Minor accessible implementation adjustments can differ from pixels under D23 when documented and tested, not become functionality reductions.

**Next step:** Owner explicitly reviews these two v0.4 sheets; ChatGPT records real answer; Codex re-reviews once PH-02 authority docket and PH-01 approval are satisfied. `ACTIVE=NO_ACTIVE_HANDOFF`; no `frontend/` or implementation handoff.
