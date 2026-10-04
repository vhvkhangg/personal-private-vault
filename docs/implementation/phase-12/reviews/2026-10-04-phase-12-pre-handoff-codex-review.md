# Phase 12 Codex pre-handoff review

- Date: 2026-10-04
- Outcome: **CHANGES_REQUESTED**
- Scope: owner-submitted Phase 12 PostgreSQL-first Search preparation and Phase 11 closeout synchronization.
- Baseline: `338e3067ace07b6f7dcf99491e00a6257ebd87af`; matches local `origin/main` (no remote fetch performed).
- Review-only: no production implementation, migration, implementation handoff, commit or push created.

## Preconditions and supplied evidence

Phase 11 implementation is owner committed at the baseline above; local tracking state and closeout documents
record owner push/freeze. Its archived handoff and final acceptance retain independent 787-test verification.
`ACTIVE.md` is `NO_ACTIVE_HANDOFF`. Phase 12 README and preparation-review exist. Phase 11 is not a milestone
trigger, so no milestone prerequisite blocks this review; Phase 12 completion will trigger the Phase 10–12 review.

The working tree contains preparation/closeout documentation, one domain skill/rule, module instructions and
existing agent skill-routing additions only. There is no new Java/test implementation, Flyway V2, hook or custom
agent. No Phase 12 implementation-test evidence is supplied or expected at this preparation gate.

## Finding requiring preparation remediation

### P12-1 — High: tag-source top-K cannot be selected before feature-owned global tie keys are known

Concrete contract references in `docs/implementation/phase-12/README.md`:

- "Tag-name search": Vault returns bounded deterministic tag-origin entry candidates; the orchestrator obtains their
  display documents afterward through feature-owned bulk lookup.
- "Cross-module relevance ordering": the global comparator uses rank, similarity, then case-insensitive `primaryText`, type and ID.
- "Bounded fan-out / pagination behavior": only top `K = offset + limit` tag-origin Vault candidates are requested before materialization;
  the top-K correctness proof assumes each source already uses the global order.

Vault owns tags/identity/lifecycle but not feature titles/names and may not depend on feature modules. Consequently
it cannot select its tag-origin top K using the required `primaryText` tie key. Deterministic Vault-ID or tag-name
ordering is not the global ordering. Sorting the limited set after document lookup cannot recover omitted entries.

Counterexample: query `topic`, offset 0, limit 1; two active entries have the same matching tag and equal tag rank/
similarity, with no text-origin matches. ID 1 has title `Zulu`, ID 2 has title `Alpha`. A Vault-ID-ordered candidate
limit returns ID 1, while the specified global first result is ID 2. Reversing feature titles without changing
Vault data reverses the correct answer but cannot change a Vault-only top-K selection. This is a contract-level
counterexample, not a claim that Phase 12 code already exists.

The required-tag AND filter, supported type/domain filters, active state and per-entry best-tag deduplication must
also be resolved before any final source limit; post-filtering a fixed tag prefix can underfill or miss later hits.

Required correction:

1. Specify an achievable ownership-preserving tag candidate/materialization/ranking strategy. Separate bounded
   transport pages from final top-K selection. For example, paged Vault candidates plus batched owner document
   materialization and bounded owner/orchestrator top-K retention may work, provided the stopping rule cannot skip
   a better equal-rank candidate. A fixed oversampling factor does not prove correctness.
2. Require source ordering/selection to agree with the final comparator, including tie normalization. Do not solve
   this by making Vault call/query feature internals, introducing a projection, or loading all candidates in memory.
3. Make tag-origin required-tag qualification, duplicate-tag collapse and limits explicit. Revise the top-K proof,
   public/module/Vault contract descriptions and `global-search-domain-modeling` fan-out guidance consistently.
4. Add required PostgreSQL regressions: more than K equal-rank tag matches with reverse ID/title ordering; offset
   pages and cross-domain ties; multiple matching tags on one entry; required-tag AND filtering with rejected early
   candidates and qualifying later candidates; text/tag overlap; bounded batch query/materialization evidence.

Until this is resolved, the preparation does not support a precise correct implementation handoff.

## Other review dimensions

- Scope/non-goals: 17 types agree with `VaultEntryType`; Finance/Journal/Personal and non-Vault standalone results,
  runtime indexing/projections, HTTP/frontend/RAG and mutation changes are excluded explicitly.
- Frozen architecture/data: source ownership and the ten allowed Search dependencies match the matrix/ADR-0010.
  Search remains a no-table leaf. Field inventory matches frozen V1. Proposed V2 is append-only pg_trgm/index-only;
  no logical-schema rewrite or ADR change is required by the currently proposed scope.
- Public API/tree: semantic query/view/enums and owner search contracts are proportionate; Knowledge/Collection
  stay parent-facing with nested-owned persistence. Descriptor refinement, package-info, placeholder removal and
  narrow search-extension clauses are covered. Current Search contains only its descriptor and instructions.
- Integrity/privacy: literal bound parameters, trash exclusion, tag capability/AND semantics and value-free
  diagnostics are appropriate. No history/write side effects or new authentication surface is proposed.
- Tooling/SOLID/reuse: one focused domain skill/rule and routing changes are justified; existing agents and safety
  hook suffice. No generic framework, thread pool, new agent or hook modification is needed.
- Tests/evidence: PostgreSQL, baseline preservation, architecture, V2/index and query-count requirements are
  appropriate, but P12-1 adversarial pagination proof must be added. Full Maven verification was not rerun for this
  docs-only gate; the 787 baseline is historical independently verified evidence, not a fresh run.
- Diagnostics: no IDE inspection was run or owner IDE warning supplied; no IDE-clean/warning-free claim is made.
  Existing build notices remain covered by Phase 11 acceptance; no warning suppression or SQL rewrite is requested.
- Docs/status: closeout/archive, milestone cadence and skill routing agree. Current gate/status now records this
  preparation finding; `ACTIVE.md` remains `NO_ACTIVE_HANDOFF`.

Implementation watchpoint, not an additional blocking finding: an explicit `similarity(...) >= 0.30` semantic
check alone does not provide a trigram index operator predicate. Pair semantics with a safe index-supported query
strategy, verify query-plan compatibility, and prevent connection/session threshold settings from dropping valid
matches. PostgreSQL documents supported operators, index behavior and short-pattern limitations in its
[pg_trgm index-support documentation](https://www.postgresql.org/docs/18/pgtrgm.html#PGTRGM-INDEX).

## Checks and next gate

- `git diff --check`: passed.
- `python .agents/hooks/test_repository_safety.py`: 13 tests, exit 0, OK; existing hook unchanged.
- Optional `scripts/validate_repository.py` check unavailable (file absent); no repository-validator success claimed.
- Graphify query used for limited navigation; frozen sources/source files were authoritative.
- Applied Global Search, Java/Spring, architecture, persistence, migration-review, backend-testing, pragmatic SOLID,
  reuse and pattern-selection skills. They guided ownership/ranking checks without adding speculative tooling.

Next: give P12-1 and the latest package to ChatGPT for preparation remediation, then rerun
`$codex-pre-handoff-review`. Do not create a handoff or commit an accepted preparation slice yet.
