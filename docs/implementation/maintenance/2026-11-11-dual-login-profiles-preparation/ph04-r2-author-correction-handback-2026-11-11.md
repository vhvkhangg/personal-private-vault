# BF26-DP — PH-04 re-review 2 bounded author correction handback

- **Date:** 2026-11-11.
- **Published `main` baseline verified for authoring:** `9c7e123d01237e057675d348ddb93700a4850e51`. Future local HEAD should be reverified before applying this package.
- **Owner authority:** D1–D6 already ratified; no repeated ratification. **This handback is not an independent readiness verdict.**
- **Finding history:** PH-01/02/03 **CLOSED at preparation** by [re-review 1](reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-1.md); PH-04 MEDIUM **OPEN** per [re-review 2](reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-2.md), pending independent verification of the two fixes.

## Correction A — exact Public-login input

- [Auth matcher and lifecycle contract](auth-compatibility-and-session-lifecycle-2026-11-11.md): `POST /api/v1/auth/profiles/login` takes exactly `{"profile":"PUBLIC","password":"..."}`, **no `identifier`**. Singleton Owner resolution is server-side. Correct configured PUBLIC credential -> 200 PUBLIC token pair/session; wrong and not-yet-configured -> indistinguishable 401; attempted PRIVATE profile/extra identifier -> 400; no silent alternate Owner lookup. Existing PRIVATE legacy `/api/v1/auth/login` keeps required `{"identifier":"...","password":"..."}` and PRIVATE-only issuance.
- Aligned [Authentication design](authentication-session-and-migration.md), [DP-A acceptance](implementation-slices-and-acceptance.md) A03/A12/A21 and [harness](harness-readiness.md) G-AUTH. Existing PIN verify well-formed wrong PIN -> 200 `verified:false` clarified; not new PIN policy.

## Correction B — current notices **must be applied locally**

The companion `apply-bf26-dp-ph04-r2-docsync-once.py` safely updates exactly six files in the Owner checkout: five current notices (`AGENTS.md`, current Owner policy, roadmap, maintenance index, ACTIVE current block) and the BF26-DP README current-status addendum. It preserves the pre-existing historical remainder and does not stage/publish. A **dry run alone is insufficient**; apply and check actual `git diff --` before the independent Codex re-review.

- All five notices link [re-review 1](reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-1.md) and [re-review 2](reviews/2026-11-11-bf26-dp-independent-pre-handoff-re-review-2.md). Record **PH-01–03 CLOSED at preparation / PH-04 MEDIUM OPEN / D1–D6 ratified / NO_ACTIVE_HANDOFF**. Do **not** claim PH-04 CLOSED before Codex verifies it.
- BF26-S5 remains `OWNER_PUBLISHED / DOCS_ONLY_API_READINESS` at `9c7e123d`; Frontend H1-F paused/unaccepted, FR-13 open, RAG suspended, normal flags OFF, historical findings/evidence/counters unchanged.
- Do not edit old independent review markdown/CSV/JSON, historical statuses, application source/tests/DBML/ADR/Flyway or any harness executable code.

## Delivery sequence

1. Copy only the five amended preparation Markdown documents and this new handback from the ZIP into the Owner checkout; **do not replace the reviewer-updated README** with a prior version.
2. From the extracted ZIP location, run the one-off Python script with `--repo <checkout>`; inspect its dry-run diff. Run again with `--apply` to **actually write** the five notices + README addendum.
3. Check `git diff --check`, changed-file list, and `git diff` for **all five notices** plus the exact auth/acceptance/harness changes. The independent review must observe these actual changes, not a tool that was merely uploaded.
4. Open a **separate independent Codex session** using `$codex-pre-handoff-review` focused on PH-04 and this re-review 2. `CHANGES_REQUESTED` remains until independent adjudication. No implementation or executable handoff, no stage/commit/push by agent.

**Next step:** Owner checkout docs sync (actual `--apply`) -> separate Codex `$codex-pre-handoff-review` -> only after independent ready result may Owner publish preparation, then separately authorize and create DP-A handoff. No implementation/remediation/takeover counters consumed.
