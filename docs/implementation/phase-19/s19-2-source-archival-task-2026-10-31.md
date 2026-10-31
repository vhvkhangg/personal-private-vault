# Codex one-off task — label and safely archive unaccepted S19-2 source (2026-10-31)

**Status:** `OWNER_APPROVED / ARCHIVAL_ONLY`; **not** an active implementation handoff. This bounded task does not resume Codex Takeover Attempt 1. Authorization: [Owner archival-only decision](owner-decision-s19-2-source-archival-only-2026-10-31.md). The [2026-10-30 S19-2 suspension](owner-decision-s19-2-suspension-2026-10-30.md) remains the governing product status.

## Operator instructions (single Codex CLI session)

1. Read `AGENTS.md`, `docs/AGENTS.md`, the two decisions above, `docs/implementation/handoffs/ACTIVE.md`, the M10 forensic report and current working-tree/index state. Do not infer source completeness from a docs-only ZIP. Verify S19-1 accepted baseline and list actual S19-2 changes, including untracked source/tests/config/migrations; identify potential sensitive/generated material not for publication.
2. Make a short path-by-path inventory before editing. Distinguish source that merely exists from runtime entry points that register at startup, change existing endpoints or initialize GPU/model/worker. Inspect any unaccepted change to shared accepted files. If the inventory cannot isolate S19-2 from accepted S19-1 or includes unsafe migration/security behavior, STOP.
3. Add **small, explicit archival-only comments/docstrings** to actual S19-2 Java/Python entry points, relevant module/package documentation and QA-only tests/config as needed; do not flood every file. The label is `P19-S19-2 — SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY`, with reference to the Owner decision. Do not mislabel all RAG as suspended: S19-1 was independently accepted.
4. Prove S19-2 cannot be used in **default** PPV startup (routes unavailable, generator/worker not started, external indexing remains inactive), without degrading accepted retrieval/citation. Inspect existing feature gating first. If strictly necessary, add a small S19-2-local default-off fail-closed gate (no production enablement approval), without widening unrelated architecture/contracts. A comment alone is not a safety gate. If proof of separation is missing or default app behavior changes unsafely, STOP; do not call the result ready for `main`.
5. Run only bounded archive checks: `git diff --check`, static inventory/guard verification, and an appropriate narrow startup/route-mapping check or focused test if it can run without model inference. Do not invoke CUDA, use C2/C1, rerun 24 slots, retry M10 or run resource-heavy suites. Do not falsely label omitted checks PASS.
6. Write `docs/implementation/phase-19/s19-2-source-archive-manifest-2026-10-31.md` with: scope and baseline/HEAD; modified/untracked paths to include/exclude; which entry points carry annotations; default-off evidence and check results (including gaps); historical unresolved M10/C1/C2/findings; publication risks and what remains local; suggested exact two-commit Owner-only plan. Keep prior logs, evidence and frozen metadata unchanged. Existing suspension docs may be linked but should not be rewritten historically.
7. End with precisely one of `READY_FOR_OWNER_ARCHIVE_COMMIT` or `BLOCKED_ARCHIVE_ON_MAIN`. Only the former authorizes recommending an **Owner-initiated archival commit**, not an independent acceptance or deployment. If blocked, describe the minimum blocker and a clearly labeled non-main archive option requiring separate Owner approval. Do not stage/commit/push.

## Hard stop and protected invariants

- `NO_ACTIVE_HANDOFF`, `S19-2_SUSPENDED_BY_OWNER / NOT_ACCEPTED`, S19-1 `ACCEPTED / PUBLISHED`; no Phase 19 closeout.
- C1 rejected, C2 not qualified (M10 fail, raw cause unknown), all accepted historical hash/fixture/ledger/counter/evidence records unchanged, no third C2 attempt or resumption of takeover.
- The Owner is archiving unfinished source **as source**, not authorizing its release or activation. No auto-adjustments to `AGENTS.md`, single-branch policy, IAM/security or QA acceptance thresholds.

**Next step after READY:** Owner verifies the manifest and exact `git status`, commits/pushes; Codex never performs publication. After BLOCKED, stop; do not continue an implementation/review loop.
