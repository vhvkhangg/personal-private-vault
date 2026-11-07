# GDI26-S3 — Design-system brief and mandatory Owner visual review

**THIS FILE IS NOT A DESIGN APPROVAL.** It records directions and a future acceptance procedure. Owner approval must be a separate dated, version-bound decision after the Owner has seen a *real* design.

## Stable design directions (D14–D20=A)

- **Visual identity:** dark-first, deep navy/charcoal backgrounds, restrained blue-to-violet gradients, controlled semitransparent glass panels. Surfaces/text/buttons must remain clearly distinct. No exact hex palette, glass opacity, blur radius or fonts fixed yet.
- **Composition:** double sidebar. Primary slim icon rail for main PPV sections, secondary contextual panel for selected module. Drive is Personal → Storage → Google Drive in secondary navigation; this is an information-architecture proposal subject to visual feedback, not a backend ownership change.
- **Density:** balanced, optimized for a practical file list; desktop table/list first, mobile card/list. Metadata, useful actions and empty/error states must be legible at glance.
- **Response:** desktop-first but functional tablet/mobile. At smaller widths rails should collapse to accessible drawers instead of clipping content or requiring hover-only navigation.
- **Locale:** default Vietnamese, internationalization-ready text keys and locale-safe dates/file sizes; English contents deferred. Don't hardcode English UI as a fallback requirement when strings are known.
- **Motion:** subtle ~150–250 ms where it improves affordance, no gratuitous heavy 3D/scroll animations; obey `prefers-reduced-motion` and pause/disable decorative transitions where required.
- **Access and performance:** target WCAG 2.2 AA, keyboard navigation, visible focus, semantic labels, contrast sufficient even over glass, browser zoom, reduced transparency/high contrast fallbacks, conservative backdrop-filter use, performance budgets measured at H1 rather than asserted for a nonexistent app.

## Iterative design deliverables (future work, NOT H0)

1. Information architecture/wireflow for login; Personal primary/secondary navigation; Drive account switcher; connect/reconnect/disconnect/root ensure; list and upload/download.
2. Minimum **two actual UI variants** *if the Owner wants alternatives*; do not force unnecessary design options. Each version has version string, timestamp, source artifact pointer(s) and screenshots for desktop and small screen, plus list of outstanding concerns. Screen images alone do not confer implementation authority.
3. State sheet: unauthenticated; disconnected/connected/reconnect-required; loading/empty/list populated; list cursor/error; upload ready/in-progress/success/conflict/unknown/size-rejected; download success/failure; browser callback success/failure; sidebar responsive states.
4. Token sheet: actual colors, text/background/overlay/gradient contrast, typography scale, spacing, radius, blur/elevation, component button/focus/disabled/error, animation duration/curve, responsive widths. The visual master is the Owner-approved artifact, not defaults in a generated theme.
5. Accessibility/performance rationale: light-on-dark contrast and nontext contrast checks on actual rendered surfaces; keyboard path; reduced-motion mode; blur containment; responsive overflow and focus restoration.
6. Iterate with Owner feedback without an arbitrary fixed limit; require explicit per-version conclusion: `REVISIONS_REQUESTED` or `DESIGN_APPROVED`. A promise to improve later is not approval.

## Nondelegable visual-approval gate

For the actual H1 implementation handoff, the following logical condition is mandatory:

`H1_IMPLEMENTATION_ALLOWED = H0_OWNER_PUBLISHED && H1_PREPARATION_INDEPENDENTLY_ACCEPTED_AND_OWNER_PUBLISHED && H1_EXECUTABLE_HANDOFF_READY && OWNER_DESIGN_APPROVAL_FOR_EXACT_VERSION`

`OWNER_DESIGN_APPROVAL_FOR_EXACT_VERSION` is true **only** if a dated, source-grounded Owner message clearly accepts *the identified design version* (or accepted version with bounded explicit carve-outs) after review. It is false if the Owner has only selected D14–D20, asks for more changes, has not replied, or an agent thinks the design is attractive. Store the final source/pointer and version/hash in the future H1 design approval record. Design changes that materially affect approved visuals require re-approval.

No model, screenshot-diff tool, `codex-final-review` or test suite may set `DESIGN_APPROVED` on the Owner's behalf. Codex may **verify existence and applicability** of an actual Owner record and stop when absent.

## Measurable verification after H1 exists (not current claims)

- Accessible contrast: ordinary text min 4.5:1, large text min 3:1, essential nontext UI contrast min 3:1 where WCAG applies; test rendered blurred gradients, not token RGB values alone.
- Browser/responsive checks on actual screenshots at approved breakpoints and at 200% zoom; no overlapping rails, truncated actionable buttons or non-scrollable dialogs.
- Use axe automated checks, semantic/keyboard manual checks and screenshot comparison to Owner-approved baseline. Pass of automated checks does **not** imply Owner visual satisfaction.
- Establish and record baseline performance budgets appropriate to tested hardware. For motion, prefer compositor-friendly properties, reduced-motion test, and avoid large full-viewport animated blur; do not claim Core Web Vitals without measurements.

**Next step:** H0 preparation review first. Only later produce concrete designs and solicit Owner's visual approval, then H1 handoff. No `DESIGN_APPROVED` record is created in this package.
