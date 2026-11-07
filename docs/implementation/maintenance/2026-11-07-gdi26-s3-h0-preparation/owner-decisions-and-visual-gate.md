# GDI26-S3-H0 — Owner decision record and nondelegable visual approval

**Date:** 2026-11-07. **Source:** explicit Owner choices in the current ChatGPT conversation; do not rewrite historical D00–D10 records. **Authority:** H0 documentation preparation and future review only, not H0 implementation/H1 design approval/Drive activation.

| ID | Recorded Owner choice | Binding preparation interpretation |
|---|---|---|
| D11 | **A** | Next.js + TypeScript + shadcn/ui under future `frontend/`, limited S3 and minimum common foundations. Does **not** authorize building all Phase 16. |
| D12 | **A** | Future S3 screen set: PPV login, Google slot 1/2 connection/status/reconnect/disconnect, ensure PPV root, file list, upload, download. No CRUD or unrelated module screens. |
| D13 | **A** | Future Next.js Backend-for-Frontend; keep access/refresh tokens server-side and browser session protected by HttpOnly cookie; no browser localStorage/JS-readable token cache. This is a high-level requirement, not a selected session library/cookie schema. |
| D14 | **A** | Double sidebar: narrow primary sidebar for main PPV sections and contextual secondary sidebar for the selected section. |
| D15 | **A** | Dark-first with restrained glassmorphism, blue/violet gradients and readable surfaces, not broad blur saturation. |
| D16 | **A** | Balanced information density: compact enough for file management, without IDE-density or oversized empty space. |
| D17 | **A** | File list/table is desktop default; small-screen responsive cards/list permitted. No broad Drive Grid/Picker. |
| D18 | **A** | Desktop-first but tablet/mobile functional; small-screen sidebar drawers, no inaccessible hover-only interactions. |
| D19 | **A** | Vietnamese-first UI; i18n-ready, with English translation deferred unless separately approved. |
| D20 | **A** | Subtle motion approximately 150–250ms where appropriate; honor `prefers-reduced-motion` and do not animate critical status/content in confusing ways. |
| D21 | **Owner delegated module placement; accepted as a preparation proposal** | Primary UI: `Cá nhân/Personal`, secondary UI: `Lưu trữ/Storage → Google Drive`. No standalone Drive primary icon. Owner may revise during visual review. This is not backend module ownership. |
| D22 | **Explicit Owner requirement** | Actual interface is **not final** until the Owner has inspected an identified visual version and explicitly stated satisfaction. No bounded automatic iteration count. `DESIGN_APPROVED` is an Owner-only decision and is an unconditional H1 implementation prerequisite. |

**Status distinction:** D11–D20 are approved product/design *directions*; D21 is delegated provisional placement; D22 is an approved *gate*. **No actual UI mockup, palette hex values, typography family, sizes, breakpoints, visual state matrix or animation prototype is accepted yet.** Neither a rendering by an agent nor a successful automated screenshot can substitute for Owner acceptance.

## Visual approval record required before H1

A future `design-approval-record.md` under the separate **H1 design/preparation** scope must capture all of:

1. Specific design artifact reference(s), each version or immutable hash/date; desktop and at least one small-screen presentation, including Personal double-sidebar and Drive Explorer.
2. The Owner's **explicit positive approval** of that specific version (date, wording/source), with any exclusions or requested changes resolved; absence of feedback is never approval.
3. UI states for disconnected/reconnect-required, loading, empty list, upload progress, success/error, 409 duplicate-key/unknown outcome, unauthorized/expired session, download failure and responsive navigation, with accessibility review evidence. Exact backend errors must be reconciled to the actual API before H1 handoff.
4. Approval scope: S3 initial UI only; global PPV design direction is not approval for unrelated future module implementation.
5. Independent Codex check that the record exists and **identifies Owner consent**, then later separate H1 preparation/handoff; Codex may verify the recorded approval but may not sign for Owner.

**Negative controls:** A UI screenshot exists but Owner says “chưa hài lòng” => BLOCKED. Owner approves only the palette but not layout => BLOCKED. H0 passes => H1 still BLOCKED. Frontend design agent declares completion without Owner message => BLOCKED. Any material visual changes after approval => request re-approval of changed version before shipping them.

## Matters reserved for later reviews, not silently chosen in H0

Precise colors, fonts and assets; spacing/breakpoint measurements; exact icon mapping; login screen detailed behavior; session storage implementation and CSRF mechanism; accessibility/animation details; error copy; mockup tool/Figma ownership; whether additional backend changes are demonstrably needed. These are reviewable engineering/design refinements within the approved scope unless they change product/security authority; then return to Owner.

**Next step:** retain this decision record as part of docs-only H0 preparation; do not claim `DESIGN_APPROVED` or edit `ACTIVE.md`.
