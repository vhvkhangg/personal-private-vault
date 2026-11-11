# Owner authorization — H1-F bounded Codex corrective follow-up 1

**Received 2026-11-10.** The Owner explicitly authorizes one new bounded corrective follow-up after [takeover 1's independent CHANGES_REQUESTED review](reviews/2026-11-10-gdi26-s3-h1-f-codex-takeover-1-independent-final-review.md). This is a separate authorization/counter, not a continuation of takeover 1, Antigravity round 4, or a reset. Codex is the implementer; subsequent acceptance requires another independent session.

## Exact authorized scope

- **FR-08:** correct append-admission ownership/replacement in `frontend/src/components/drive/explorer.tsx`. Explicit restart must permit later paging; obsolete completion must not release a newer owner's admission. Preserve one in-flight append, view/generation fencing, cancellation, cursor semantics and no automatic 409 replay. Reproduce before/after with focused component and real native tests showing all seven files after restart during pending append; preserve existing regressions.
- **FR-09:** correct definitive-outcome transition in `frontend/src/components/drive/folder-dialog.tsx`. Keep original name/key for unknown/pending/conflicted outcomes. Resolve only canonically definitive rejection, including the verified absent-intent `FOLDER_KEY_EXPIRED` sequence, then permit an explicitly initiated new name/key. No automatic mutation/new-key retry or all-409-is-definitive assumption. Reproduce the interrupted-before-dispatch / expired-key sequence before/after in component and native tests; update directly affected current README/handback claims.
- **FR-12 / PH-I01:** complete actual native 200% and effective 320-CSS-px viewport/dialog usability evidence, controls/keyboard/focus/errors/reflow, uncropped captures, exact source/zoom/viewport metadata and unique bounded receipts. Prefer no app changes. Diagnose URL-confidence only through permitted client behavior. No trust/security/hooks/permissions/policy change, bypass or alternate-mechanism evasion; report NOT_VERIFIED if compliant observation remains blocked.
- **FR-13:** preserve OPEN / UPSTREAM_DEPENDENCY_BLOCKED and the existing Owner wait decision. No dependency/pin/lock/install/override/force/waiver or reopened investigation without new compatible evidence or Owner instruction.

Writes are limited to these two affected frontend components, focused existing component/native/a11y tests and necessary allowlisted test support, `frontend/README.md`, current ACTIVE and new dated authorization/implementer handback/self-review. Preserve the [original executable](executable-handoff-h1-f-2026-11-09.md), authorizations, takeover 1 handback/self-review, independent reviews and all old evidence as history. Do not rewrite closed findings or approved desktop v0.3.

No backend Authentication/Drive, V27/V28/schema, existing Spring security authority, unrelated source/config/harness, live Google, deployment, mobile, architecture or cleanup changes. Standing isolated Spring localhost:19080 / Next localhost:3000 applies, with free-port preflight. No Git staging/commit/push/tag/PR.

## Counters and delivery gate

Antigravity **3/3 exhausted / STOPPED**; Codex takeover **1 independently blocked / STOPPED**; **Codex bounded corrective follow-up 1 authorized by this instruction / acceptance pending**. H1-B/H1-AUTH unchanged. Tool interruptions/test iterations do not advance these counters.

Reproduce both counterexamples before and after correction, run affected regressions and mandatory compliant native zoom verification, reuse unchanged accepted evidence only with source qualification, retain raw failures/warnings/cleanup, complete Codex implementer self-review/handback and STOP. Owner opens a **separate `$codex-final-review` session**. FR-13 keeps the slice CHANGES_REQUESTED even if all three authorized corrections pass; no self-approval or READY_FOR_OWNER_COMMIT.

Entry HEAD `8e8b6c56d7cdc5895634a0fdcba2d2ae6aac7b84`; dirty work preserved, index empty. Entry manifest/backup and unique evidence root: `logs/gdi26-s3-h1-f/cf1-mwan14ae-b48f65/`. All 116 frontend and 2,281 compiled backend inputs match the independent review's entry; 3,197 tracked and 1,198 historical-evidence hashes captured.
