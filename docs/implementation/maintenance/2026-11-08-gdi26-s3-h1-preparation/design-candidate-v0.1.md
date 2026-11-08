# H1 Design Candidate v0.1 — dark blue/violet glass, two rails

**Status:** `VISUAL_DRAFT / NOT_DESIGN_APPROVED`; date 2026-11-08. **Exact artifact:** [designs/gdi26-s3-h1-design-candidate-v0.1-desktop-mobile.png](designs/gdi26-s3-h1-design-candidate-v0.1-desktop-mobile.png) — PNG 1536 × 1024, SHA-256 `71a02e391bb7a6bd21429422fdcd7bcfebfc9c0d49498c3f176a8310d1bd1d0b`. Owner has **not** approved this version.

The image presents one illustrative desktop populated-file screen and one illustrative mobile populated-file screen. It is a **static concept**, not functional UI, source code, final design tokens, WCAG certification, accessible DOM, frontend package or Google account evidence. All names, storage usage, account statuses and files are invented example data.

## Intended aesthetic and layout to evaluate

- Primary slim icon rail selects **Cá nhân**, not a standalone Google Drive primary icon.
- Secondary contextual sidebar includes **Lưu trữ → Google Drive**, visually distinct from the content panel. On mobile, nav collapses into a labelled drawer.
- Deep-navy backdrop with blue/violet gradient accents; translucent border/elevation on navigation and file containers; limited accent glows around selection/primary action. Avoid heavy full-viewport blur or visual noise.
- Main area favors readable file list/table with file name/type/size/updated time and a clear download action; two account slot cards at top and an upload entry point. Mobile uses one-column file rows.
- Vietnamese-first labels, balanced density, accessible focus/active states and subtle ~150–250ms motion will need **actual render and keyboard evidence** later; the bitmap alone cannot prove them.

## Critical truthfulness corrections before visual finalization

1. The image's **global “Tìm trong PPV...”** bar is decorative and **NOT part of H1**; it must be removed/disabled or separately authorized. The per-folder search/filters likewise have no server API; if retained, they are **client-only filtering on currently loaded page** and must say so.
2. The **“12,4 GB đã sử dụng”** card invents a capacity/quota metric absent from accepted S2; remove from finalized visuals unless independently proven by a new approved API scope.
3. **“Tạo thư mục PPV”** means **ensure one app-owned root only**, not user-created subfolders or generic folder CRUD. It should appear only in the relevant ABSENT/PENDING/UNKNOWN root states, not the populated-ready state.
4. Both slots appear **connected as demo content**; real flags are OFF and no Google account/live verification exists. Use disconnected and reconnect-required state boards before any approval.
5. No thumbnails, server-side sorting/search, real upload progress percentage, successful browser file-save notification, general folders or native Workspace exports are implied by the image.
6. The PNG includes decorative logo/icon imagery. Before implementation decide original/local icon assets, licensing and accessible name; Google Drive branding must not imply affiliation or a connected backend.

## Visual revision backlog (candidate v0.2 goals, not automatic scope changes)

- Simplify glass accents and check actual text/essential icon contrast against the gradient; confirm touch targets and visible keyboard focus.
- Produce **full desktop and mobile frames** rather than a presentation board, plus at least login/disconnected, empty/loading, unknown upload and error state frames.
- Resolve whether an optional page-only filter survives; replace unsupported quota and global search with legitimate account/root status cues.
- Fix responsive rail collapse and overflow at 320 CSS px, 200% text enlargement and 400% zoom. Check the accessible data-table exception separately from surrounding navigation.
- Confirm actual typography/font/license and design tokens after Owner comments. No code/token change is approved merely by this list.

**Owner feedback requested:** sidebar proportions, glass intensity/gradient, typography and spacing, file row density, slot switcher and mobile ergonomics. Record concrete reactions/requests in [design-review-ledger.md](design-review-ledger.md); never set DESIGN_APPROVED from the existence of this image.
