# GDI26-S3-H1 — Owner visual approval record: desktop Google Drive Explorer v0.3

**Date:** 2026-11-08. **Approval:** `DESIGN_APPROVED_DESKTOP_EXPLORER_V0.3`, **not** blanket approval of unseen login/error/mobile screens, backend capability extensions, or H1 implementation. This record captures the Owner's explicit message after seeing the attached visual. It is a docs-only approval evidence record; independent preparation review must verify applicability before implementation handoff.

- **Owner-inspected exact artifact:** [designs/gdi26-s3-h1-owner-approved-desktop-explorer-v0.3.png](designs/gdi26-s3-h1-owner-approved-desktop-explorer-v0.3.png), PNG; SHA-256 `1c5175d40f42a64418d8127459dc7c08595e79916980308c6d9934b85d9f442e`.
- **Source of authorization:** 2026-11-08 PPV ChatGPT conversation, Owner message beginning *“Tôi chốt design này nha.”*, accompanied by this exact screenshot. The Owner expressly permits reasonable implementation variation instead of demanding pixel-perfect recreation.
- **Visual status for this exact screen:** `DESIGN_APPROVED_DESKTOP_EXPLORER_V0.3`.
- **Overall H1 execution gate:** `BLOCKED / PREPARATION_NOT_REVIEWED`, and outstanding screen/state coverage and newly required backend capability gaps. `ACTIVE` remains `NO_ACTIVE_HANDOFF`; approval is not a permission to scaffold.
- **Scope:** Populated desktop **Google Drive Explorer content area** only. Earlier Owner direction intentionally excludes header, primary/secondary sidebar and mobile visuals from **this specific visual review**, without cancelling prior D14/D18 responsive and navigational implementation obligations.

## Accepted design characteristics / required user-visible behavior

1. Dark-first navy/glass surfaces, controlled blue-to-violet accent, legible text, balanced 4-column-style responsive desktop grid; no fixed 100%-pixel-exact color, spacing, font or glow requirement.
2. Two clearly separate account cards/selector for PPV Google slots 1 and 2; one selected slot at a time; no stale cross-account content.
3. Account-specific Google Drive **storage usage/capacity indicator**. Real data required; invented values must never appear as live data. Show unavailable/unknown if a provider field is absent.
4. Exact visible search label **“Tìm trong Drive hiện tại...”**, name-based search within the **current PPV-authorized Drive subset**, not arbitrary Google Drive. Respect case/Unicode and explicit search scope.
5. Filter controls **Loại**, **Từ ngày**, **Đến ngày**. Default interpretation for dates is *file modification date* (`modifiedAt`) unless Owner changes it. Inclusive local-date boundaries converted consistently to UTC instants; reject invalid ranges.
6. **Mới** drop-down combines uploading an original file and **creating a PPV-app-owned direct child folder** under the one verified PPV root. **D25=A was explicitly chosen on 2026-11-08.** S2 cannot yet create child folders; the new endpoint/provenance must be independently approved. No nested grandchild, delete/move/rename/share or bulk local-directory upload.
7. Card/grid file and folder presentation with actual safe preview where available, plus a clearly identified fallback when unavailable. Real content previews are not guaranteed by the original S2 backend.
8. Infinite scroll/automatic **load more** without visible pagination controls or “Hiển thị x tệp”; retain bounded server cursor requests and progress/error/stop behavior internally. Do not imply one unlimited response.
9. Desktop populated-state screenshot is the visual benchmark; meaningful changes to layout/information hierarchy or removal of required actions require Owner re-approval. Reasoned minor variations improving contrast, performance, accessibility or implementation feasibility are pre-authorized, provided functionality and visual character remain consistent and Codex documents the differences.

## Material scope gap: approval does not certify availability

Existing S2 exposes `POST /root-folder`, `GET /files?limit&cursor`, `POST /files`, `GET /files/{fileId}/download`; the returned `File` DTO has `id,name,mimeType,sizeBytes,modifiedAt`, while the page has `nextCursor,hasMore`. It does **not** return quota, thumbnails, nested folders, name/type/date query parameters or new-folder creation. See [capability gap and decision gate](capability-gap-and-decision-gate.md).

Therefore **do not** treat the visual approval as authorization to silently widen backend OAuth scope, create folders, query arbitrary My Drive, show synthetic capacity/preview data, or ship nonfunctional filters. Independent Codex preparation review must verify the D25=A decision and an approved bounded API/source plan; if a capability is impossible within the selected scope, report the exact conflict to Owner rather than remove it unilaterally.

## Still to cover before a complete H1 executable handoff

The Owner approved the populated desktop Explorer screen. The H0 visual-approval template separately requires applicable session/connection, loading/empty/unknown/error, upload/download and responsive behaviors. Design these as faithful derivatives of the approved visual, document testable state/layout rules and obtain specific Owner confirmation **where the H0 version-bound review contract requires it**. Do not invent Owner approval of unseen screens. Keep H1 `NOT_READY_FOR_IMPLEMENTATION` until accepted/published H1 preparation, Owner applicability, and a distinct READY executable handoff are verified.

**Next step:** finish H1 API gap/Owner decision and representative-state design contracts; independent `$codex-pre-handoff-review` when ready. No `frontend/`, deployment, real Google call or Git publication by agents.

## D25=A subsequent Owner decision (2026-11-08)

**Verbatim:** “GDI26-S3-H1: Tôi chọn D25=A. Hãy cập nhật Owner decision record và hoàn thiện preparation, xử lý các backend capability gap của thiết kế desktop v0.3 tôi đã duyệt. Không tạo frontend/ hoặc mở implementation handoff khi chưa qua independent pre-handoff review.” This explicitly **resolves first-level child-folder product scope**; it neither changes the screenshot's approved pixels nor approves the unseen representative states. The image's shown file/folder names, counts, quota value and previews are illustrative, **not provider facts**. See [backend contract](backend-capability-extension-contract.md), [state boundary](design-state-coverage-and-approval-boundary.md).
