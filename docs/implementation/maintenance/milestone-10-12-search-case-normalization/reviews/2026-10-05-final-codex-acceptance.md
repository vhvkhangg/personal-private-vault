# Search Case-Normalization Maintenance — Codex Final Acceptance

Date: 2026-10-05. Outcome: **READY FOR OWNER COMMIT**.

Reviewed active handoff `maintenance-milestone-10-12-search-case-normalization`, submitted as
`IMPLEMENTED_AWAITING_CODEX_REVIEW`, against committed baseline `449eaf686f5c869e2ec49d59051d2e63b82e5f83`.
Owner approval of 2026-10-04 and the narrow frozen-Phase-12 Search exception remain the implementation authority.
No production or test source was edited by Codex; review/status/evidence documentation only.

## Findings and remediation

**FRM10-12-1 — CLOSED.** The real PostgreSQL global Search test now includes an unrelated primary name with BODY
`"\u0130".repeat(300) + " TARGET " + "x".repeat(400)`, queried with `"target"` under ROOT. It asserts BODY,
non-null snippet, original `TARGET`, prefix/suffix ellipses and <=240 characters. The original match index is 301;
the old folded-copy index is 601, selecting a tail window that omits `TARGET`. Unlike the former short U+1E9E
fixture, this test exercises the defective long-body path and would fail with its old offset calculation.
Locale restoration remains in `finally`. Existing locale/Unicode/boundary/cleanup/fallback/surrogate tests remain.

Evidence correctly distinguishes 15 SQL services from 13 snippet helpers (Music and Vault have none), names the
actual `rawQuery`/`prefixPattern`/`substringPattern` binds and uses U+0130 for genuine lowercase expansion.
U+1E9E remains a separate short-body character-preservation check, not an offset-window proof.

The approved maintenance implementation addresses **M10-12-1**. Its milestone gate is **not** declared closed here:
the owner must commit/push, then rerun the milestone review. No new blocking findings.

## Independent verification

- `mvn -f backend/pom.xml -ntp clean verify`: **BUILD SUCCESS**, 817 tests, zero failures/errors/skips,
  01:53 min, finished `2026-10-05T07:18:21+07:00`. Surefire XML independently totals 817 cases, including all
  26 Search tests. Architecture/Modulith, Flyway V1/V2, PostgreSQL persistence and Hibernate validation pass.
- `git diff --check`: exit 0, before and after documentation synchronization.
- All 15 production SQL text blocks compared with HEAD differ only by the approved PostgreSQL-side parameter
  folding and formatting. No Java query/snippet `toLowerCase` remains. No POM, migration, DBML, architecture,
  dependency or repository-tree baseline changes.
- Implementer focused/full results are separately retained in canonical `test-evidence.md`; the independent full
  run includes the focused tests and is not represented as a separate Codex focused command.

## Mandatory review dimensions

The [2026-10-04 review](2026-10-04-final-codex-review.md) retains the full production assessment. Re-review confirms:

- Business correctness: ROOT/tr-TR ASCII `id`/`ID`, ROOT identical `İD`, owner/global/Vault tag matching and original
  BODY windows pass. Ranking, 17 result types, literal escaping, qualification/deduplication and bounds remain intact.
- Clean code/SOLID/extensibility/patterns: original-text owner-local helpers; no speculative framework, hierarchy or
  cross-module dependency. No production remediation or unrelated refactor was introduced for the test-only finding.
- Efficiency: retained captured production Music/Vault plans, pinned-connection fuzzy threshold, lookahead and
  query/batch-count tests pass. Forced index/control settings are capability evidence, not a normal-planner/latency
  guarantee; BODY scans remain the disclosed accepted limitation.
- Persistence/concurrency: read-only owner boundaries and transaction-local threshold restoration retained;
  no new mutation, locking or global-locale production workaround. Test locale is restored; parallel testing is not enabled.
- Privacy/security: no added logging, private payload, SQL interpolation, HTTP surface or auth changes.
- Package hygiene/scope/docs: only approved 15 production services and the existing Search test source changed;
  no new production packages/public DTOs or generated tracked artifacts. Existing unrelated placeholders stay out of scope.
- Warnings: inherited Lombok/Unsafe, CSV/test-support deprecation, Mockito/ByteBuddy agent warnings and SpringDoc INFO
  remain disclosed, not new blockers. No IDE inspection or warning-free claim.
- Evidence/status: FRM10-12-1 corrections verified; active handoff, maintenance scope and current status pointers
  synchronized to acceptance while preserving historical reviews and the separate milestone `CHANGES_REQUESTED` gate.

## Owner action

Suggested commit message: `fix(search): align PostgreSQL casing and snippet offsets`

Next step: owner commits/pushes the accepted maintenance/review/status package, then runs `$codex-milestone-review`.
No commit/push/tag/PR performed by agents. Phase 13 remains blocked until `MILESTONE_READY`, owner-committed milestone
docs and ChatGPT post-milestone synchronization/reset.
