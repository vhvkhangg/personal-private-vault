> **HISTORICAL APPROVAL REQUEST — SUPERSEDED 2026-11-08.** The Owner has since **delegated secondary state designs to Codex** (no v0.4 state-sheet approval required) and **approved the proposed bounded backend/BFF scope**, including max 256 child folders per account and preview/search limits. Google Sign-In was newly requested and needs [separate identity security review](h1-google-sign-in-owner-identity-contract-v0.5.md). See [D26–D30](owner-decision-addendum-v0.5.md). The original unapproved responses below remain dated request history; **do not prompt Owner again to approve A/B**.

# GDI26-S3-H1 — Remaining Owner approval docket (2026-11-08)

**NOT APPROVED as of creation.** The Owner has already approved [populated desktop v0.3](owner-visual-approval-record-v0.3.md) and selected **D25=A**; do **not** ask to revoke or repeat those decisions. This docket addresses precisely independent review `PH-01` and the proposed H1-B integrity/schema/API expansion (`PH-02–05`). ChatGPT has written reviewable proposals; only the Owner may approve, and only independent Codex can close the preparation findings.

## Approval A — derivative UI states, not the approved desktop

Review both:

1. [Desktop nine-state sheet v0.4](designs/gdi26-s3-h1-derivative-state-sheet-v0.4-desktop.png), SHA-256 `01b0128ca4f19117727fbf9f1cf320f479d3058b529f4cd3f52edecd7a1e8c16`.
2. [Responsive three-state sheet v0.4](designs/gdi26-s3-h1-derivative-responsive-v0.4.png), SHA-256 `706bd15a2917e574a1fa9759e163426ececc085cf2d199500ae2108f1e4f6de0`.

Read [state/approval matrix and candidate tokens](h1-ph01-visual-derivative-v0.4.md). Approve explicit **behavior/state applicability** and relative layout, not exact pixels; browser 320 CSS-pixel reflow, WCAG and mobile visual fit remain future H1-F evidence. If the Owner wishes to defer mobile *visual fine-tuning* (earlier expressed), they must explicitly say which responsive behaviors are sufficient for the H1-F initial gate and which changes require separate approval before ship. Do not silently label an uninspected mobile design approved.

**Visual status today:** `DESKTOP_V0.3_APPROVED / DERIVATIVE_V0.4_OWNER_APPROVAL_PENDING`.

## Approval B — narrowly bounded backend/security contract

The selected **candidate** is documented in:

- [Child-folder intent ledger, nullable upload parent, V27, Portability exclusion, exact REST delta](h1-b-authority-schema-api-contract-v0.4.md).
- [Full PPV-subtree bounded substring search and cursor algorithm](h1-search-and-cursor-contract-v0.4.md).
- [Strict thumbnail host policy and fallback](h1-thumbnail-security-contract-v0.4.md).
- [Local Next BFF session, CSRF, rotation/logout and fixed browser host](h1-f-bff-session-csrf-contract-v0.4.md).

**Key security/scope choices Owner must actually accept as part of B:** one new child-folder ledger; nullable upload `target_parent_id`; additive V27+Portability security classification; at most **256 total child folder identities and 8 unresolved per root** with no automatic destructive cleanup; one level only; current S2 root operations unchanged; server-side complete substring results via constrained app-marker listing with finite 8-call request budget/opaque continuation; exact thumbnail-origin allowlist `https://lh3.googleusercontent.com:443` and **fallback for untrusted/unavailable document previews** (PDF/DOCX not universally guaranteed); one Node BFF on `http://localhost:3000` talking to Spring `http://localhost:8080`, memory-only session with 8 total entries, 30-minute idle / 12-hour absolute session, authenticated + prelogin CSRF, serialize refresh and logout barrier. Real live provider/host availability remains NOT_VERIFIED and separate activation gate.

**No new application/business authorization is implied:** this approval, if granted, authorizes preparation review of a **prospective** ADR/additive schema/endpoint scope; it is not permission to execute V27, change backend, create frontend, install Node or turn on Google. H1-B and H1-F still require their own independent, executable implementation handoffs **after** reviewed preparation has been Owner-published. The original four-gate visual/handoff condition remains in force.

## Suggested explicit Owner response (not provided yet)

> “GDI26-S3-H1: Tôi duyệt **visual derivative v0.4** gồm hai ảnh với hash nêu trong approval docket; giữ nguyên desktop v0.3. Tôi cũng phê duyệt **proposed H1-B/H1-F contract v0.4** trong docket để đưa Codex pre-handoff review; vẫn giữ các giới hạn D25=A và fallback preview như đã mô tả. Tôi chưa cho phép implementation/`frontend/`.”

If the Owner disagrees with either approval, record specific revisions and mark the affected approval `REVISIONS_REQUESTED`. A decision on A does not automatically approve B and vice versa. There is no artificial visual review round limit.

**After actual Owner approval:** transcribe its source/date/wording and the SHA-bound artifacts to [visual approval ledger](design-review-ledger.md); preserve this docket and all earlier reviews. Then run a new *independent* `$codex-pre-handoff-review`. Until then **`CHANGES_REQUESTED / OWNER_APPROVAL_PENDING`**, not `READY_FOR_HANDOFF`.
