---
name: frontend-design-accessibility
description: Plan or review PPV UI layout, visual states, Vietnamese text, responsive behavior and accessibility, including design work before frontend exists. Requires the Owner visual gate for H1 implementation; excludes backend-only tasks.
---

# PPV frontend design and accessibility

Read [ACTIVE](../../../docs/implementation/handoffs/ACTIVE.md), [D11–D22](../../../docs/implementation/maintenance/2026-11-07-gdi26-s3-h0-preparation/owner-decisions-and-visual-gate.md) and the [shared visual protocol/template](../../../docs/workflow/frontend-development.md#owner-visual-approval-record-template). H0 creates no mockup/asset. Later design work needs its own authorized scope. Neither this skill nor a review screenshot confers DESIGN_APPROVED.

## Directions, not accepted tokens

- D14–D20: dark-first navy/charcoal, restrained blue/violet gradient and readable glass; two sidebars; balanced file-management density; desktop table/list and functional mobile/tablet drawers/list; Vietnamese-first, i18n-ready; useful approximately 150–250ms motion with reduced-motion support.
- D21 provisionally places Drive in **Cá nhân / Personal → Lưu trữ / Storage → Google Drive** in the secondary navigation, with no standalone primary Drive icon. Owner may revise placement during visual review. Backend `drive` ownership and Media S3 are independent of this UI grouping.
- Exact palette, font, opacity/blur, spacing, icons, breakpoints and motion curves remain future concrete design decisions. Use the approved version's semantic tokens for background, surface, text, border, focus, status and motion; do not invent a final theme or import an external design master.
- Maintain hierarchy at realistic filename lengths and balanced metadata density. Escape names as text; no untrusted HTML/Markdown or remote thumbnails. No telemetry, remote font/image/CDN or new animation package is implied.

## State and access review for later H1

| Surface | Required states and observable behavior |
| --- | --- |
| Login/session | Unauthenticated, submitting, invalid/expired session and logout; labelled controls, password-manager/paste support, useful inline error and focus recovery |
| Two slots/root | Disabled, disconnected, connecting, connected, reconnect/disconnect and root pending/reconciliation/error; no invented READY/success projection; switching slots invalidates old state |
| File list/navigation | Loading, empty, populated, cursor/end/error; clear row actions, semantic table headings, filenames that wrap or truncate accessibly; desktop rails and small-screen drawers with keyboard close/focus return |
| Upload | Ready, admission/size rejection, progress, verified success, key conflict/in-progress, outcome unknown and explicit same-action recovery; announced state without a false success toast |
| Download/callback | Callback success/failure remains backend text; start, abort/truncation/timeout/error without false completion; browser-managed download is not proof the file was saved |

- Use native links/buttons/forms/labels/headings before ARIA. Provide a main-content skip path, names for icon actions, visible unobscured focus, logical tab order and drawer/dialog focus containment/return. No hover-only or gesture-only function; tooltips are supplemental. Expose progress/errors without flooding live announcements; status must not rely on color alone.
- Target WCAG 2.2 AA on the actual rendered glass/gradient/fallback surfaces: normal text contrast at least 4.5:1, qualifying large text 3:1, relevant controls/focus boundaries 3:1. Check disabled/status distinguishability, high-contrast and reduced-transparency alternatives; shadcn primitives alone do not prove conformance.
- Measure text resize to 200% **and** SC 1.4.10 reflow at 320 CSS-pixel width (for example 1280 at 400% zoom). Preserve content/actions without two-dimensional page scrolling; apply the data-table exception only where essential, keeping surrounding navigation readable. This carries PH-I01 into H1; H0 does not close the independent observation.
- Test keyboard, touch, long Vietnamese strings/diacritics, focus after errors, responsive navigation and reduced motion on real viewports. Use locale-safe dates/sizes and translation keys, not hardcoded English or hydration-dependent locale guesses. English translation remains deferred.

Record actual measurements and screenshots against the exact approved artifact using [frontend-testing](../frontend-testing/SKILL.md). Axe/visual automation supports manual keyboard/contrast/reflow review; it cannot grant Owner approval or establish complete WCAG conformance. Source audit: [provenance](../../../docs/implementation/maintenance/2026-11-07-gdi26-s3-h0-preparation/frontend-guidance-provenance-h0-2026-11-07.md).
