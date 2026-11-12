# BF26-DP-A — Owner publication closeout (2026-11-12)

- **Slice:** BF26-DP-A — Safe Authentication & Session Foundation (first slice of Dual Public/Private Profiles).
- **Source publication verified:** GitHub `main` commit [`5c1da4e5404ee3c724e3fc2e03569d4093bcb44d`](https://github.com/vhvkhangg/personal-private-vault/commit/5c1da4e5404ee3c724e3fc2e03569d4093bcb44d), `feat(auth): add fixed profiles and persisted session authority`, dated 2026-11-12 UTC.
- **Predecessor:** [published DP-A preparation commit `536a7aab`](https://github.com/vhvkhangg/personal-private-vault/commit/536a7aab8f48c2a182a8c5b88c1d248fdb29e283); D1–D6 remain [Owner-ratified](owner-ratification-d1-d6-2026-11-11.md).
- **Independent acceptance:** [Codex exceptional-takeover independent final review](reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md) returned `READY_FOR_OWNER_COMMIT`, with **FR-DPA-01–09 CLOSED** and **DPA-L01 LOW / OPEN / BACKLOG** (retained Google cancellation fixture wording). This dated review preceded publication; **implementation is now OWNER_PUBLISHED**.
- **Result:** `INDEPENDENTLY_ACCEPTED / OWNER_PUBLISHED / DP-A CLOSED / NO_ACTIVE_HANDOFF`; this new documentation-only closeout itself remains **unpublished until Owner commits/pushes the present docs synchronization**. No independent runtime/deployment review is asserted for the closeout.

## Published scope and exact first-slice boundary

- One singleton Owner (`app_users.id=1`) with separate fixed PUBLIC and PRIVATE password login profiles and persisted server-revocable session authority; preserve identifier/password legacy PRIVATE login and identifier-free PUBLIC login.
- PUBLIC remains **authentication/profile-only**. All business/data APIs, generic Vault/Search, Media/Personal, Film/Fiction, Reference, Portability export and unknown/unclassified/ASYNC/ERROR paths remain **default DENY** for PUBLIC. **No Public Film/Fiction/Reference grant is implemented or authorized** by DP-A. PRIVATE retains accepted business capabilities, subject to existing policy/feature flags.
- Profile-bound session generation, password-gated PUBLIC→PRIVATE elevation, Private→Public downgrade, token/refresh/epoch fencing, retained PIN contract, Google Sign-In ON-unmapped fail-closed behavior and default-OFF provider flags follow the reviewed [executable DP-A handoff](executable-handoff-dp-a-2026-11-11.md).
- Additive Flyway V29 security data and same-slice Portability secret-table exclusions, together with accepted migration/export/restore verification. Canonical V1–V28 contents remain unchanged; no arbitrary record visibility or second user/tenant.
- Two bounded FR-DPA-09 source-map exceptions (`OpenApiConfiguration.java`, `DriveApiInventoryIntegrationTest.java`) were expressly Owner-authorized and independently closed; they do not activate Drive or change unrelated production behavior.

## Evidence characterization — no fresh tests in this docs closeout

| Source of evidence | Qualification |
| --- | --- |
| Implementer final G1–G5 receipts | **432 passing tests** with source-qualified manifests: G1 94, G2 53, G3 60 (including 10 native Edge), G4 69, G5 156 across 27 classes; zero reported failures/errors/skips in these final batches. They are implementer evidence independently audited, **not** 432 newly rerun reviewer tests. |
| Independent reviewer | Separate focused G2 verification completed **43 passing cases before `TIMEOUT/124`**; timeout/cleanup receipt preserved as failed command status, **not** an overall PASS. Outstanding class separately executed **10/10 PASS, exit 0** at a fresh receipt path. This and audited source-affinity evidence supported the independent acceptance. |
| Preservation | Review records matching hashes for **1,898 final backend/source/resource/build/watchdog inputs**; unchanged V1–V28; historical failed/timeout receipts retained. |
| Not verified by publication/this docs task | Live production deployment/cutover, real Owner-data backup/restore, live Google/Drive activation, browser/Frontend integration, IDE diagnostics; no new runtime tests have been executed during publication sync. The accepted disposable PostgreSQL recovery verification is not evidence of a live deployment. |

Detailed commands, receipts and reviewer qualifications remain in the [final independent review](reviews/2026-11-12-bf26-dp-a-codex-takeover-independent-final-review.md) and [implementation handback §12](implementation-handback-dp-a-2026-11-11.md#12-independent-takeover-final-disposition--2026-11-12). This closeout **reuses**, but does not rewrite, those records.

## Findings, counters and preservation

| Register | Final publication disposition |
| --- | --- |
| BF26DP-PH-01–04 | CLOSED at preparation, unchanged. |
| FR-DPA-01–09 | CLOSED by independent implementation review. |
| DPA-L01 | LOW / OPEN / BACKLOG; no waiver or silent closure. |
| DP-A Antigravity remediation | 3/3 EXHAUSTED / STOPPED. |
| DP-A Codex exceptional takeover | 1/1 independently reviewed / ACCEPTED / STOPPED. |
| All other implementation/frontend counts | Preserved unchanged. |

Preserve H1-F `OWNER_PUBLISHED / NOT_INDEPENDENTLY_ACCEPTED / CHANGES_REQUESTED / FRONTEND_PAUSED_BY_OWNER`; FR-13 `MEDIUM / OPEN / UPSTREAM_DEPENDENCY_BLOCKED`; desktop v0.3; P17 FROZEN, P18 CLOSED, S19-2 SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; indexing INACTIVE and live originals DISABLED/DEFERRED. Normal Google Sign-In/Drive/Drive Files/Feed flags remain OFF/unset. No new provider activation, RAG work, Recycle Bin, final non-RAG audit or Frontend implementation is authorized by this closeout.

## Docs Sync Check and next gate

**Requested docs-only synchronization (not part of source commit `5c1da4e`):** update only current navigation blocks in root `AGENTS.md`, `docs/workflow/current-owner-delivery-policy.md`, `docs/roadmap.md`, `docs/implementation/maintenance/README.md`, `docs/implementation/handoffs/ACTIVE.md`, and add controlling publication notices to this preparation `README.md`, executable handoff and implementation handback. Preserve all prior checkpoint text and historical independent reviews; no source/test/SQL changes. The adjacent one-time apply helper is a delivery convenience and **must not be committed**. Confirm actual local `git diff --check`, content, links, tracked-file scope and HEAD before Owner publication of the docs.

**Next step:** Owner | verify/apply this bounded docs-only sync and commit/push only the reviewed Markdown delta | BF26-DP-A publication closeout at the **actual** published SHA above | `NO_ACTIVE_HANDOFF`, no implementation permission granted | Antigravity 3/3 exhausted, Codex takeover 1/1 accepted. Thereafter, independently prepare and seek separate Owner authorization for **BF26-DP-B1** (safe non-NSFW Fiction/Film projections and qualified static Reference GETs), without assuming these Public grants already exist.
