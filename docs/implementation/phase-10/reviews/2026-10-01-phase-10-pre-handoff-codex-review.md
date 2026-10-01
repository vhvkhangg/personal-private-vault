# Phase 10 Codex pre-handoff review

Date: 2026-10-01

Result: **CHANGES_REQUESTED**

Review-only preparation gate. No implementation handoff or production code created.

## Preconditions and scope

Phase 9 and maintenance are committed/frozen; milestone docs are committed as `17b3f6e` and the Phase 7–9
milestone is `MILESTONE_READY`. Prepared reset leaves ACTIVE.md at `NO_ACTIVE_HANDOFF` and archives the accepted
maintenance handoff. Phase 10 Feed/ImportData README, review gate, owner-local module instructions and workflow
skill/rule are present. This review concerns that proposed preparation only, not prior frozen implementation.

## Blocking finding

### 1. High — ImportData transition concurrency contract and tests are missing

**Location:** `docs/implementation/phase-10/README.md`, Job/item state model and User decision + transactional import;
corresponding testing contract, preparation checklist, module instructions and workflow skill.

The preparation requires execute from VALIDATED, irreversible terminal states, whole-job target rollback and
cancellation without target writes. It does not specify a shared job-level guard for parse/validate/execute/cancel,
fresh state checks after acquiring that guard, or same-job contention regressions. A normal transaction alone
does not serialize the read/check/write sequence.

Two executions can both read VALIDATED and create separate Study/Information/Vocabulary targets (these intentionally
have no natural-key uniqueness), then each mark the same job IMPORTED. Execute and cancel can similarly observe
VALIDATED concurrently; cancellation can overwrite a completed job or execution can write targets after cancellation.
The per-job imported counts can conceal duplicated targets. Frozen schema checks do not arbitrate these transitions.
This is a preparation ambiguity and credible future implementation defect, not a claim of existing Phase 10 code.

**Required preparation correction:**

- Specify that all mutating transitions for one job share an owner-local serialization/conditional-state guard;
  a pessimistic lock on the existing import_jobs row is a proportionate schema-compatible option, not a required
  new framework. Read/recheck authoritative current status under that guard, including already-managed entities.
- Define loser/repeat behavior: only one execution can create/update targets; callers observing terminal or
  incompatible state must not perform target writes or regress status. Choose stable invalid-transition rejection
  or an explicit idempotent return for a completed repeat; do not silently run it twice.
- Define execute/cancel ordering: if cancellation wins, no execution writes; if execution wins, cancellation cannot
  overwrite IMPORTED. Hold the guard through the existing whole-job transaction and retain failure rollback to
  VALIDATED; no schema change, global clear, retry framework or per-item independently committing transaction.
- Add deterministic PostgreSQL tests for execute-vs-execute, execute-vs-cancel and competing pre-import transitions,
  including a target without natural uniqueness. Observe actual contention/explicit completion rather than relying
  on sleeps; assert target/Vault counts, job/item states, classification/imported counts and rollback outcomes.
- Synchronize the README, review checklist and relevant rule/skill/module guidance so the implementation handoff
  has one canonical transition requirement instead of competing state policies.

**Classification:** docs/tooling preparation remediation by ChatGPT; no production maintenance slice needed.

## Accepted preparation dimensions

- Four Feed and two ImportData owned tables, enums, fields and count checks align with Flyway V1/DBML.
  SavedResource identity, unique URL hash and conversion provenance match frozen schema intent.
- Dependencies remain public Vault plus parent Knowledge; conversion and imports use parent-owned commands.
  No nested Knowledge access, public query expansion or persistence leakage is proposed.
- Normalized adapter-ready ingestion, dual-key conflict rules, privacy-safe unique conflicts, JSON isolation,
  scheduling predicate/defaults and bounded/deterministic reads are explicit.
- Import target/format matrix, staged parsing without target writes, safe CSV/JSON/YAML direction, exact Markdown,
  Note-only existing database duplicate lookup and whole-job rollback are explicit.
- Owner-local parsing/snapshot helpers and a minimal mature parser dependency are proportionate; no generic base,
  custom agent, hook, event hierarchy, live network/scheduler, HTTP/frontend or deferred domain work is justified.
- Package grouping fits existing semantic named interfaces and internal ownership. Implementation must add appropriate
  package-info files and replace placeholders; no production package redesign is required at this gate.
- Prior low-risk archived Phase 8 wording was synchronized. Milestone remains ready; the finding does not reopen it.

## Verification and diagnostics

- `git diff --check`: exit 0, repeated after review/status edits.
- `git diff --name-only -- '*.java' backend/pom.xml .agents/hooks.json .agents/hooks`: no changes;
  preparation adds module AGENTS files only, not Phase 10 production/test code or parser dependencies.
- `python .agents/hooks/test_repository_safety.py`: exit 0, **13 tests passed**.
- No full Maven rebuild required for this docs/tooling-only gate; prior milestone verification recorded 594 passing
  tests on `785dd7d`. No Phase 10 implementation test evidence is claimed.
- Known Lombok Unsafe and deprecated test-support notices remain accepted baseline diagnostics. No new IDE warning
  was supplied; no IDE inspection or warning-free claim. Native SQL schema-resolution concerns would depend on IDE
  datasource configuration, not justify changing correct frozen SQL.
- Graphify used for navigation; critical statements verified in canonical schema, contracts and preparation files.
  User/ChatGPT changes preserved; no commit/push/tag/PR action performed.

The Feed/Import workflow skill reinforced owner-local state/transaction boundaries; the new guard requirement must
be documented there or routed explicitly to the canonical README during remediation.

## Next gate

Give findings/latest package to ChatGPT for narrow preparation remediation, then rerun `$codex-pre-handoff-review`.
ACTIVE.md stays `NO_ACTIVE_HANDOFF`. No commit message until preparation is accepted.
