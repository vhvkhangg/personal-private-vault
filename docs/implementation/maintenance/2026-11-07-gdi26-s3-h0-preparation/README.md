# GDI26-S3-H0 — Frontend harness foundation preparation (2026-11-07)

**Gate:** `READY_FOR_HANDOFF` — H0 harness preparation independently reviewed on 2026-11-07; see the [Codex review](reviews/2026-11-07-gdi26-s3-h0-independent-pre-handoff-review.md). No blocking findings; one INFO clarification for future H1 accessibility verification. **NOT** `READY_FOR_IMPLEMENTATION`, **NOT** `DESIGN_APPROVED`. H0 runtime acceptance remains prospective; Owner publication and a separate executable H0 handoff are still required.

**Verified GitHub main baseline:** [`d8da1677f6700c308aec750dcc714fc8c3983185`](https://github.com/vhvkhangg/personal-private-vault/commit/d8da1677f6700c308aec750dcc714fc8c3983185) (`docs(drive): close published GDI26-S2 folder and file operations`), checked 2026-11-07. At that baseline, [`ACTIVE.md`](../../handoffs/ACTIVE.md) is `NO_ACTIVE_HANDOFF`; S1 and S2 are Owner-published, Drive/files/Feed default OFF; repository has **no `frontend/` tree or `package.json`**. This package does not change ACTIVE. The dated S2 closeout is [authoritative publication history](../2026-11-06-gdi26-s2-preparation/owner-publication-closeout-2026-11-07.md).

**Owner preparation authority:** This conversation approves D11–D20 option A, a UI-only Google Drive placement under **Personal → Storage → Google Drive**, and, explicitly, **Owner visual approval before H1 implementation**. Earlier [D00–D07](../2026-11-05-google-drive-integration-preparation/owner-decision-record-2026-11-05.md) and [D08–D10](../2026-11-06-gdi26-s2-preparation/owner-decision-record-s2-2026-11-06.md) remain binding. Product/frontend implementation, Google activation and new frontend files are **not** authorized by this preparation.

## Intended slice sequence and gates

1. **H0 preparation — NOW:** docs only; produce smallest reviewable frontend harness contract. Independent Codex `$codex-pre-handoff-review` must assess and may request corrections. No source/tooling/hooks/agent/skill edits in this package.
2. **H0 harness implementation — LATER ONLY:** after `READY_FOR_HANDOFF`, Owner commit/push of reviewed preparation and separate Codex `$codex-create-handoff` resulting in `READY_FOR_IMPLEMENTATION`. The H0 implementer may add/adjust **approved** frontend-targeted agent/rules/skills/documentation and narrowly proven safety tests/hooks, without creating `frontend/` or writing product UI. Mandatory self-review and independent `$codex-final-review`, then Owner commit/push and docs-only closeout.
3. **Visual design rounds — SEPARATE OWNER GATE:** produce actual versioned desktop/mobile UI designs and state coverage for feedback. Owner may request unlimited revisions. Record `DESIGN_APPROVED` **only after explicit Owner acceptance of a concrete identified version**, not from D11–D21, approval of the harness, silence or an agent opinion.
4. **H1 Website Explorer implementation — BLOCKED:** requires published/accepted H0, accepted H1 preparation plus its own executable handoff, and a **separate, version-bound Owner `DESIGN_APPROVED` record** before any implementation of `frontend/`. Initial Phase 16 remains deferred, not implicitly opened by S3.

No H1 implementation, Next.js scaffolding, design freeze, source mutation, test run, Google consent or deployment is included here. An independent reviewer cannot grant Owner visual approval.

## Preparation files

- [Owner decisions and explicit visual gate](owner-decisions-and-visual-gate.md)
- [Bounded scope, authority and sequence](h0-scope-and-sequencing.md)
- [Repository harness reuse/gaps and candidate artifacts](harness-inventory-and-targets.md)
- [External skill evaluation and supply-chain conditions](skill-selection-and-provenance.md)
- [H1 frontend/BFF boundary that H0 must encode](frontend-bff-and-ux-contract.md)
- [Cross-project visual brief and design-approval protocol](design-review-and-approval.md)
- [Reviewable H0 acceptance matrix](acceptance-and-verification-plan.md)
- [Risk ledger and operational exclusions](risk-and-operations.md)
- [Independent review launcher and evidence rules](pre-handoff-review-instructions.md)

## Non-negotiable boundaries

- H0 is a **frontend harness**, not a new web app. Reuse existing `.agents`, `.codex`, `AGENTS.md`, `docs/workflow`, `scripts/run-bounded-command.py` and protected Git workflow; do not casually replace or bypass native hook rules.
- Design intent is dark-first blue/violet gradient, controlled glassmorphism, double sidebar, balanced density, list/table-first desktop Explorer, responsive mobile, Vietnamese-first with i18n-ready structure, subtle 150–250 ms motion with reduced-motion support. **These are directions, not a signed-off mockup.**
- Drive is **secondary navigation under Personal** as a UI grouping; backend `drive` remains its independent owner/module and does **not** become a `personal` dependency.
- S1/S2 backend is reused unchanged: PPV-authenticated 2-slot S1 connection API, direct-child S2 list/ensure/upload/download, required stable upload `Idempotency-Key`, 50 MiB limit, no Google refresh token to browser. Media S3 is distinct.
- `PPV_DRIVE_ENABLED=false`, `PPV_DRIVE_FILES_ENABLED=false`, `PPV_FEED_SCHEDULER_ENABLED=false` stay OFF. Remote Google/Cloud/live browser/reverse proxy/HTTPS/key/spool ACL proof remains independently gated.
- H0 and H1 do not authorize Google Picker, arbitrary Google Drive scanning, native Workspace export, nested directories, file mutation (delete/rename/move/share), background sync, RAG indexing or activation of deferred Phase 16.

**Next step:** Owner | inspect/stage/commit/push the independently reviewed **docs-only H0 preparation**, then Codex `$codex-create-handoff` for `GDI26-S3-H0` ONLY | this README and the [independent review](reviews/2026-11-07-gdi26-s3-h0-independent-pre-handoff-review.md) | actual preparation publication and `ACTIVE=NO_ACTIVE_HANDOFF`; no H0 implementation before a separate executable handoff, no H1 before its own gates and explicit version-bound Owner visual approval | H0 Antigravity 0/3, takeover NOT_AUTHORIZED.
