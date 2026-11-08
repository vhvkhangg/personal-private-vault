# H1 decisions: binding history vs design proposals

## Binding existing Owner choices

- **D11=A:** Next.js + strict TypeScript + shadcn/ui, minimum common foundations for S3 only; not all Phase 16.
- **D12=A:** PPV website login (**D26 now specifies Google Sign-In**), Google slots 1/2 connect/status/reconnect/disconnect, root ensure, list, upload and download.
- **D13=A:** BFF with server-managed Spring access JWT and **opaque** refresh token; browser holds only an HttpOnly protected session identifier. No tokens in browser JavaScript/storage/props/URLs.
- **D14–D20=A:** slim primary rail + contextual secondary sidebar; dark-first, restrained blue/violet gradient glass, balanced density, desktop grid/card Explorer with previews (later Owner v0.3 overrides earlier desktop-table direction), mobile deferred for H1, Vietnamese-first/i18n-ready, subtle ~150–250 ms reduced-motion-aware interactions.
- **D21:** delegated **provisional UI placement** Personal → Storage → Google Drive, no backend `personal → drive` edge. Owner may revise during visual review.
- **D22:** Owner-only version-bound visual approval before H1 implementation, unlimited design revisions; no silent approval.
- **D00–D10:** preserve two Google account subjects, `drive.file`+OIDC, single PPV-owned root per subject, original binary direct children, <=50 MiB upload, no Picker/whole Drive/exports/sync/delete/share/rename/move/copy/RAG.

## New dated Owner instruction, 2026-11-08

- **D23 — `OWNER_APPROVED_DESKTOP_VISUAL`**: exact [v0.3 screenshot](owner-visual-approval-record-v0.3.md), SHA-256 `1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e`. Codex may make justified small color/layout/accessibility/performance adjustments; not pixel-exact.
- **D24 — `OWNER_REQUIRED_UI_CAPABILITIES`**: easy slot 1/2 switching, real account storage usage, current Drive-subset filename search, type/date from/to filters, cards with previews, “Mới” upload/new folder menu, infinite scroll without visible pages or count footer. Date filter defaults to `modifiedAt` as an explicit engineering *assumption* pending independent review; do not claim creation-date semantics. D24 is not evidence existing backend supports the requirements.
- **D25=A — `OWNER_SELECTED / 2026-11-08`**: “Mới → Thư mục mới” creates one or more **PPV-app-created direct child folders under the single verified PPV root** of the selected Google subject. Users can enter a child folder, list its permitted original files, upload original files into that child and download those files. Maximum nesting **one child-folder level** for this bounded H1 proposal; no grandchild creation, arbitrary Google Drive traversal, delete/rename/move/share/copy or local-folder bulk upload. This is a **new, separately reviewed Drive backend capability**, not a claim that S2 already supports it. Source: Owner explicit “GDI26-S3-H1: Tôi chọn D25=A” message on 2026-11-08. See [bounded API proposal](backend-capability-extension-contract.md).

**Source:** Owner's explicit PPV ChatGPT message with attached v0.3 image, 2026-11-08. No automated agent approval. D23 supersedes older v0.1 aesthetic/table recommendations for the populated desktop Drive view, not D18 responsive implementation or D22 for other relevant visual states.

## Proposal register — not Owner-approved product changes

| Proposal | Current status | Reason to revisit |
| --- | --- | --- |
| P-H1-01 dark glass and blue/violet token candidates | DESIGN_DRAFT | Actual hex/contrast/blur/elevation/typography not yet chosen by Owner |
| P-H1-02 64–76px primary rail and ~210–250px secondary panel desktop | DESIGN_DRAFT | Real screen-space balance and large-text reflow must be checked |
| P-H1-03 full authorized-subset name/type/date filters | OWNER_REQUIRED / API_EXTENSION_REVIEW | Existing S2 only has `limit,cursor`; UI must not silently pretend a single page covers all files |
| P-H1-04 genuine per-slot Google account storage usage | OWNER_REQUIRED / API_EXTENSION_REVIEW | New bounded Drive-owned read needed; global PPV search remains excluded |
| P-H1-05 single self-hosted Node BFF with bounded volatile server-side sessions | SECURITY_PROPOSAL | Must prove auth rotation/CSRF/expiry/concurrency, single-process topology, restart logout |
| P-H1-06 mobile nav via drawer + list cards and compact file actions | DEFERRED_BY_OWNER_D27 | Not an initial H1 deliverable; desktop browser accessibility remains |
| P-H1-07 local font stack; no remote CDN/assets/Google logo requirement | SECURITY/DESIGN_PROPOSAL | Limit external dependencies, avoid font download/runtime telemetry |

D25=A is now selected, and the previous `OWNER_DECISION_REQUIRED` question is historical. Distinguish **create a child folder within PPV** from **upload an entire existing local directory**: the latter remains excluded absent new Owner approval. Exact folder caps/idempotency, preview transport, full-subtree search implementation and session persistence are separately reviewable technical contracts, not implementation permission. If a reviewer finds a material unsatisfied product/security condition, return an exact blocker or Owner decision rather than silently changing it. This request authorizes *preparation and design review only*.

## D25 acceptance interpretation / no accidental scope expansion

- **KEEP:** one PPV-created root per distinct Google subject (S2), two connection slots, `drive.file` + OIDC, 50 MiB original-file limit.
- **NEW SUBJECT TO GATE:** app-created direct child folders with creation identity/provenance and safe lost-response recovery; folder-level list/enter and file upload/download scoped to that specific verified parent.
- **NEW UI REQUIRED:** `Mới` menu has **Tải tệp lên** and **Thư mục mới**; child folder tiles are real where backed by verified provider state. The initial root-ensure action appears contextually when absent, not as generic folder creation.
- **NOT CHOSEN:** recursively nested folders, arbitrary external-folder adoption, Picker, upload a local directory tree, deleting/renaming/moving/sharing, Workspace export, Media S3 replacement or external RAG indexing.
- **DESIGN:** the exact populated desktop v0.3 remains Owner approved; the later Owner instruction D28 **delegates auxiliary login/error/empty/unknown-state visuals to Codex** under the accepted design language; mobile is deferred under D27. Refer to [state applicability](design-state-coverage-and-approval-boundary.md).

## Historical D26 docket note (superseded by actual D26 Google Sign-In choice)

The earlier v0.4 docket was written **before** the Owner's later D26–D30 instruction. Its pending-approval claims are historical; see [actual Owner decision addendum](owner-decision-addendum-v0.5.md). D26 now denotes Google Sign-In, D27 desktop 14-inch, D28 Codex state delegation, D29 accepted backend/BFF scope and D30 delta-only package delivery. No executable handoff follows.


### Current controlling decisions

[Addendum D26–D30](owner-decision-addendum-v0.5.md) is the binding later Owner message. The historical D26 placeholder above is not a veto. **Do not equate the two Google Drive slots with the one Google login identity**; verify the new [Authentication contract](h1-google-sign-in-owner-identity-contract-v0.5.md) separately.
