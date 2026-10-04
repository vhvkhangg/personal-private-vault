# Phase 12 Codex pre-handoff re-review

- Date: 2026-10-04
- Outcome: **CHANGES_REQUESTED**
- Scope: remediated Phase 12 preparation, especially source/global ordering and P12-1 closure; review-only.
- Baseline: `338e3067ace07b6f7dcf99491e00a6257ebd87af`; HEAD equals local `origin/main`, no remote fetch performed.
- Previous report: [initial pre-handoff review](2026-10-04-phase-12-pre-handoff-codex-review.md), retained historically.

## Preconditions and P12-1 disposition

Phase 11 remains committed/frozen with retained independent 787-test evidence; `ACTIVE.md` remains
`NO_ACTIVE_HANDOFF`. The owner-submitted Phase 12 preparation is the reviewed scope. No milestone prerequisite is
due before Phase 12, and no production/test Java implementation or V2 migration is present in the preparation patch.

**P12-1 is closed.** The revised ranking removes `primaryText` from membership/order, keeps it as display data,
and requires every source to rank by the same rank/similarity/type/ID tuple. Vault now qualifies active state,
domain/types and all required tags and collapses duplicate matching tags before limiting. Text qualification
continues across bounded pages; selected tag IDs are batch-materialized afterward. Reverse ID/title, offset,
cross-domain, duplicate-tag, late qualifier and overlap regressions are now required. The skill/rule and Vault/Search
instructions were synchronized. This resolves the feature-title ownership conflict without a projection/framework.

## New finding requiring preparation remediation

### P12-2 — Medium: native enum source ordering is not the required type-name ordering

Concrete references:

- `docs/implementation/phase-12/README.md`, "Cross-module relevance ordering": global tie key is
  **`VaultEntryType` name ascending**.
- The same README's feature source tuple, tag-source tuple, bounded-qualification tuple and PostgreSQL test
  requirement instead show **`vault_entry_type ASC`**, including
  `ORDER BY best_similarity DESC, vault_entry_type ASC, vault_entry_id ASC` before LIMIT.
- `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/AGENTS.md`, "Phase 12 tag-origin source contract",
  repeats the bare enum ordering. Domain skill/rule and Search instructions say only "type ASC".
- `backend/src/main/resources/db/migration/V1__create_schema_v1.sql:7` defines the frozen native enum with
  `IMAGE` before `ALBUM`; Java `VaultEntryType` also declares IMAGE before ALBUM.

The source tuples do not define a textual alias/cast or distinguish label order from native enum/Java ordinal
order. PostgreSQL enum comparisons follow declaration order, not alphabetic label order, as documented in
[PostgreSQL 18 enum ordering](https://www.postgresql.org/docs/18/datatype-enum.html#DATATYPE-ENUM-ORDERING).
Thus following the SQL-shaped source requirement directly gives the wrong top-K under the specified global
comparator. Sorting limited candidates afterward cannot recover the omitted entry.

Counterexample: one active IMAGE and one active ALBUM have the same matching tag score, offset 0, limit 1, and
no text-origin hits. Native enum ordering selects IMAGE, but name ordering requires ALBUM. The same problem can
occur within the multi-type Media source. This is a frozen-definition/documentation counterexample, not an executed
Phase 12 implementation or database diagnostic.

Required correction:

1. Make the type tie key explicit and identical in SQL source selection and the Java/global comparator. Preserve
   the canonical name-order requirement unless an explicit preparation decision changes it consistently.
2. For name order, specify a textual type-label key with deliberate collation, such as
   `CAST(ve.entry_type AS text) COLLATE "C"`, and Java comparison by `Enum.name()` rather than enum natural/ordinal
   order. Define `type_name` aliases if used so generic tuples cannot be interpreted as native enum comparison.
3. Synchronize all source tuples, the proof/test contract, domain skill/rule and Vault/Search instructions. No V1,
   enum declaration, logical schema or dependency change is needed or authorized.
4. Require real PostgreSQL limit/offset regressions comparing source output and global order for equal-score
   ALBUM/IMAGE candidates and cross-domain type ties, including tag-only and multi-type feature text sources.
   Exercise more than K candidates so post-LIMIT reordering cannot conceal the defect.

P12-2 is the only open blocking preparation finding. Do not reopen P12-1 or create an implementation handoff yet.

## Remaining review dimensions and evidence

The previously reviewed scope/non-goals, 17-type field inventory, no-table leaf boundary, exact allowed dependency
matrix, parent Knowledge/Collection contracts, append-only index-only V2, narrow frozen-module extension,
literal matching/privacy, batched qualification/materialization and positive bounds remain appropriate. No new
logical schema, mutation change, cross-module persistence access, custom agent, hook, executor or speculative
framework is introduced. Meaningful package-info/placeholder hygiene remains part of implementation acceptance.

Prepared tests cover the original adversarial ranking issue and preserve the 787 baseline; they now need the
P12-2 native-vs-label-order cases. The prior trigram query/index compatibility watchpoint remains an implementation
verification requirement, not a new blocker. Missing materialization is explicitly treated as integrity failure.
Documentation status has been synchronized to this re-review; the initial report is historical.

Checks performed:

- `git diff --check`: passed before final documentation synchronization.
- `python .agents/hooks/test_repository_safety.py`: 13 tests, exit 0, OK; hook unchanged.
- Source/DDL comparison and limited Graphify navigation; source remains authoritative.
- No new Maven run or PostgreSQL integration test at this docs-only gate; 787 tests are retained Phase 11 evidence.
- No IDE inspection or owner IDE warning supplied; no IDE-clean/warning-free claim or blanket suppression.

Global Search, Java/Spring, architecture, JPA/PostgreSQL, backend-testing, pragmatic SOLID, reuse/consistency and
pattern-selection skills guided the review. The persistence skill exposed the native-enum ordering mismatch.
No production implementation, migration, handoff, commit or push was created.

Next: give P12-2 and the latest package to ChatGPT for preparation remediation, then rerun
`$codex-pre-handoff-review`.
