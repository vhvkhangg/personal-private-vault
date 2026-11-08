# H1 desktop 14-inch + delegated state-design gate v0.5

**Current Owner decisions:** D23 approves the exact [populated Explorer v0.3](owner-visual-approval-record-v0.3.md) SHA-256 `1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e`; D27 limits initial H1 visual implementation to **desktop/laptop 14-inch**; D28 delegates subordinate state screens and component visual choices to Codex under the existing frontend harness. **No new Owner visual approval of the v0.4 state sheets is needed.** This delegation is sourced from the Owner's 2026-11-08 message and must be checked by independent Codex, not assumed by ChatGPT to close PH-01.

## Viewport meaning and visual scope

- **14-inch denotes physical screen diagonal, not a fixed CSS width.** Use two practical browser-viewport fixtures: baseline **1366×768 CSS px** and a secondary **1536×864 CSS px** (representative, adjustable to actual browser chrome/scaling). The acceptance target is usable content without clipped controls or horizontal scrolling in the 1366×768 fixture; vertical scroll/infinite load is expected.
- **Only the Explorer content area** was visually Owner-reviewed in v0.3; surrounding PPV header/sidebar are established dark double-sidebar directions but not a newly approved full-dashboard implementation. Do not build extra modules to fill the primary rail.
- Cards/grid with safe thumbnail preview or honest fallback, two easy account selectors, **real** per-account Drive quota (no synthetic value), “Tìm trong Drive hiện tại...”, Loại/Từ ngày/Đến ngày, one “Mới” menu for **Tải tệp lên** and **Tạo thư mục con** (not local directory-tree upload), and cursor-backed infinite scroll. Preserve scope: PPV-owned root and directly owned child folders, not entire arbitrary Google Drive.
- **No mobile/tablet screen delivery, mobile drawer variants, breakpoint fine-tuning or mobile-specific Playwright acceptance in initial H1.** Prior `v0.4` responsive state-sheet artifact is **historical only, NOT_APPROVED and NOT_REQUIRED**, not another implementation gate. D18's earlier general responsive direction remains a future project-level intent rather than an H1 initial mobile deliverable.
- Browser zoom, semantic controls, keyboard, contrast, focus, reduced-motion and basic accessible **reflow** still need truthful measurement appropriate to a desktop app. SC 1.4.10 at 320 CSS px under extreme zoom is an accessibility **reflow test**, not a requirement to design a separate mobile app. The H0 `PH-I01` remains open until actual H1 browser evidence or an independently reviewed scoped exception; do not claim WCAG 2.2 AA from static design.

## Codex delegated implementation states and quality rules

| State family | Minimum required behavior (Codex designs exact visuals) |
| --- | --- |
| Login / identity | Google Sign-In start/loading/success/error/unbound/expired; secure first-time owner linking and session expiry; no username/password as ordinary website login |
| Google Drive slots | Connected/disconnected/reconnect-required, per-slot quota loading/absent/error, rapid slot switch with no stale cross-account tiles |
| List and folders | Loading/skeleton, empty permitted root/child, no matches, next cursor/partial scan, unknown provider fault, one-level parent/breadcrumb, long Vietnamese filename |
| Mutations | Root ensure pending, folder creation pending/unknown/reconciliation, upload progress/conflict/unknown (same key+same file), safe cancel and explicit retry |
| Preview / download | Safe thumbnails or type-icon fallback, rejection of untrusted formats, authorization failure/expired login and download abort/timeout/truncation without false completion |

Codex may choose subtle motion, actual color shades, spacing, loading placeholders, accessible focus and wording. **Must not** remove required states, turn unknown into success, expose credentials, substitute fake provider content as live quota/preview, add delete/move/share/Picker/whole-Drive, or materially change approved v0.3 core structure without new Owner approval. Reviewer checks harness-based accessibility, finite state mapping, source-qualified test evidence and screenshot comparison to approved v0.3. Codex self-review never substitutes for independent acceptance.

## Approval logic and PH-01

`MAIN_VISUAL_APPROVED` = exact v0.3 Owner statement + SHA match (**true**). `SECONDARY_STATES_DELEGATED` = exact later Owner instruction (**true**). `H1_DESIGN_SCOPE_READY_FOR_REVIEW` = those two facts + a complete state/desktop acceptance contract (**candidate here, independent verification pending**). Full implementation remains **blocked** until H1 preparation is independently accepted and Owner-published and a separate executable handoff is authorized. The original D22 Owner-only gate remains effective for the material main visual; delegated small states require **Codex quality + independent review**, not a fabricated additional Owner approval.

Visual artifacts v0.4 remain evidence of design exploration only; do not label them Owner-approved or require the Owner to retroactively sign them. No new image-generation requirement, no mobile design work and no `frontend/` are authorized by this document.
