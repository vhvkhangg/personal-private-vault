# Phase 15 Backend Audit — 2026-10-07, owner-authorization re-review

Status: **REMEDIATION_REQUIRED**

All 17 original BA15 findings are authorized within the owner's recorded bounds and consolidated into the single
`phase-15-backend-audit-remediation` [handoff](../../handoffs/ACTIVE.md), status **READY_FOR_IMPLEMENTATION**.
They remain **OPEN** (one High, 15 Medium, one Low); approval is not remediation, acceptance or accepted debt.
Codex has not implemented production fixes.

## Baseline

- HEAD/local origin/main: `6a89a998c512dda27c3e494a3525bf6118ee981e`, unchanged since the initial audit.
- Phase 14 implementation `3bb3f2e78a38eb66bec955219ced634d3cfddd9d` and closeout
  `0a8f3d101f39dc8cf8f5e1d6dcc46d29e6182606` ancestry verified; Phase 14 remains frozen.
- Phase 15 preparation-review remains READY FOR AUDIT. Its committed preparation baseline is unchanged.
- Before this turn's edits: nine pending audit/governance summaries plus the initial audit report from the previous
  completed audit. No production/test/POM/config/schema/architecture changes and no conflicting handoff.
  ACTIVE.md was **NO_ACTIVE_HANDOFF** before authorized replacement; its stale NOT_STARTED context was not used
  instead of the newer audit-status/initial report.
- Java 25.0.2/Maven 3.9.15; Spring Boot 4.1.1/Framework 7.0.9/Modulith 2.1.1; PostgreSQL 18.6/
  Testcontainers 2.0.5 and MinIO baseline are inherited from the unchanged
  [initial audit](2026-10-06-phase-15-backend-audit.md), not represented as newly sampled versions.

## Evidence executed / preserved

This is the skill's **post-owner-decision consolidation**, not a post-implementation closure audit.
All prior evidence/notes/findings are preserved. No build/static/HTTP/concurrency check was needlessly repeated
against identical implementation; none is newly claimed green here.

| Evidence | Command / source | Result |
| --- | --- | --- |
| Baseline/status/ancestry | Root: `git status --short; git diff --stat; git rev-parse HEAD origin/main; git diff --name-only -- backend/src backend/pom.xml backend/compose.dev.yml docs/adr docs/database docs/architecture; git merge-base --is-ancestor 3bb3f2e78a38eb66bec955219ced634d3cfddd9d HEAD; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }; git merge-base --is-ancestor 0a8f3d101f39dc8cf8f5e1d6dcc46d29e6182606 HEAD` | Exit 0; unchanged baseline/implementation; only prior audit documents pending; no live handoff. Publication checked via local origin/main, not remote fetch. |
| Preserved complete evidence | Initial audit's exact commands/tool versions/results and retained phase-15-audit logs/target reports | Full verify **920 tests, zero failures/errors/skips**, dependency/SpotBugs/PMD/CPD/coverage/Modulith/PostgreSQL/MinIO/OpenAPI/wire/repository results unchanged. Report-generation success is not finding-free static acceptance. |
| Evidence presence | `Get-Item -LiteralPath 'phase-15-audit-verify.log','phase-15-audit-dependencies.log','phase-15-audit-spotbugs.log','phase-15-audit-pmd.log','backend/target/phase15-audit/openapi.json'`; `rg -n 'Tests run: 920\|BUILD SUCCESS' phase-15-audit-verify.log` | Logs/spec retained; verify summary and BUILD SUCCESS captured. Target remains disposable/ignored. |
| Decision/source review | Owner's current explicit request, owner-decisions.md, initial finding entries, preparation/ACTIVE/template, matrix, Account/Study/Knowledge descriptors/public contract, ADR-0007/0008 | All eight approval groups resolve the previously pending choice set without new schema/edge permission. |
| Graphify navigation | `graphify reflect --if-stale`; `graphify query 'account study mutation lock guard invariants' --budget 900` | Existing 6,566-node graph, BFS depth 2; output truncated/partly irrelevant. Navigation only, not proof a coordination mechanism is implemented or safe. Canonical sources verified separately. |
| Documentation validation | Root: `& './backend/target/phase15-audit/ValidateAuthorizationDocs.ps1'; git diff --check; if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }; git diff --name-only -- backend/src backend/pom.xml backend/compose.dev.yml docs/database docs/architecture; git status --short; git --version; $PSVersionTable.PSVersion.ToString(); & 'C:/Users/VU KHANG/.local/bin/graphify.exe' --version` | Exit 0; **14 documents, 140 file-target links, zero missing/trailing-whitespace**. Handoff/report each contain exactly 17 unique finding rows; eight approval groups. No production/schema/diagram edits, no Phase 15 handoff archive. |

Tool versions for the preserved evidence and unavoidable spelling/IDE/CVE/anchor/load-test limitations remain as
recorded in the initial report. The 920 Maven versus 907 retained-XML accounting discrepancy is still disclosed,
not silently resolved. Source-derived concurrency examples still need deterministic implementation regressions.
This turn's diagnostic versions: Git **2.45.1.windows.1**, PowerShell **5.1.26100.9549**, Graphify **0.9.69**.
Validation helpers/cache stay ignored, outside production/test source. Initial historical report SHA-256:
`5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63`.

## Audit coverage

All 17 dimensions from the complete initial audit remain applicable to the **unchanged implementation**.
The table below carries forward those results, not new test execution. No dimension is omitted or narrowed.

| Dimension | Result | Retained coverage / finding IDs |
| --- | --- | --- |
| 1. Build/compiler/dependency/static | FINDINGS | Full/static evidence; BA15-1. |
| 2. Domain correctness | FINDINGS | All domain families; BA15-2/9/10/12/13/14/17. |
| 3. SOLID/cohesion/coupling | PASS | Owner/public boundary review; no speculative refactor finding. |
| 4. Pattern fitness | PASS | Real facade/provider/repository/transaction boundaries. |
| 5. Overengineering/YAGNI | PASS | No independently material speculative hierarchy finding. |
| 6. Duplication/competing policy | FINDINGS | CPD triage; divergent validation BA15-2. |
| 7. Validation/errors | FINDINGS | BA15-2/4/5/6/11/12. |
| 8. Logging/exception/privacy | FINDINGS | Redaction/error review; BA15-4/5/6/11. |
| 9. REST/OpenAPI | FINDINGS | Full route inventory/generated schema/wire evidence; BA15-2/4/5/6/7/8/14/15. |
| 10. Authentication/security | FINDINGS | Credential and broader auth/privacy review; BA15-3. |
| 11. Persistence/schema/query | FINDINGS | Frozen manifest/JPA/PostgreSQL/query/ownership; BA15-1/9/10/11/12/13/17. |
| 12. Transactions/concurrency/I/O | FINDINGS | Upload/export/stream/race boundaries; BA15-7/8/9/10/13/17. |
| 13. Performance/resources | FINDINGS | Bounded reads/export, complete import materialization/review gap BA15-14. |
| 14. Tests/reliability | FINDINGS | Full suite passed; per-finding regression gaps remain. |
| 15. Package/file hygiene | FINDINGS | Confirmed descriptor/placeholder gaps BA15-16. |
| 16. Docs/spelling/diagrams | FINDINGS | Canonical/current/file-link/diagram review; BA15-15/16; manual spelling substitute. |
| 17. Configuration/operations | FINDINGS | Runtime defaults/storage/health consistency; BA15-1. |

## Findings and authorization disposition

Each ID incorporates its **unchanged exact evidence/path/symbol/contract, consequence and regression requirements**
from the corresponding initial audit entry. The handoff makes the selected correction/test targets executable;
the [owner record](../owner-decisions.md) supersedes only the earlier unselected alternatives.
Frozen baseline impact for each remains **owner approval required**, now **satisfied within the recorded bounds**;
this does not unfreeze the surrounding baseline. Status of each is **OPEN — APPROVED FOR BOUNDED REMEDIATION**.

| ID | Severity / category | Selected correction and approval |
| --- | --- | --- |
| BA15-1 | High — runtime integration | Group 1: Boot-managed Flyway, preserve SQL/validate; no manual runtime prerequisite. |
| BA15-2 | Medium — validation/compatibility | Group 2: owning-domain rules win; boundary/schema coverage, no weakened domain/generic framework. |
| BA15-3 | Medium — credentials | Group 3: full 12–128-character inputs, standard encoding and legacy bcrypt verification; no truncation/byte-limit substitution. |
| BA15-4 | Medium — structural validation | Group 7: member non-null validation preserving legitimate list null/empty behavior. |
| BA15-5 | Medium — HTTP translation | Group 7: specific negotiation/error status repair; no broad exception map. |
| BA15-6 | Medium — cross-module errors | Group 7: safe public Knowledge exception translation and rollback. |
| BA15-7 | Medium — transport framing | Group 7: clear only uncommitted binary framing; real-wire regression/no JSON after commitment. |
| BA15-8 | Medium — binary integrity | Group 7: provider-known length first, retain metadata fallback/edit contract; no new mismatch policy. |
| BA15-9 | Medium — schedule concurrency | Group 7: targeted owner-local coherent writers/fresh locked state, no schema/framework. |
| BA15-10 | Medium — first-set race | Group 7: narrow atomic rating set preserving timestamps/capabilities. |
| BA15-11 | Medium — reference errors | Group 7: specific missing-reference/race handling retaining conflict-safe assignments. |
| BA15-12 | Medium — monetary precision | Group 4: reject unrepresentable width/excess fractional precision before persistence; no rounding/widening/currency rule. |
| BA15-13 | Medium — cross-module invariant | Group 5: preserve invariant after assignment within Knowledge → Account; mandatory pre-implementation expansion stop. |
| BA15-14 | Medium — complete bounded review | Group 6: inspect every accepted >100-item job with bounded pagination/equivalent; no ingestion-cap substitution. |
| BA15-15 | Medium — semantic OpenAPI | Group 8: document existing non-obvious semantics; no unrelated behavior change. |
| BA15-16 | Low — hygiene/current docs | Group 8: existing-package descriptors, two confirmed placeholders and identified prose only; historical records unchanged. |
| BA15-17 | Medium — lifecycle lost updates | Group 7: targeted Journal/Personal/Feed writer pairs, deterministic PostgreSQL regressions, no generic locks/schema. |

### BA15-13 boundary assessment

The current top-level matrix permits Knowledge → Account, not the reverse; Study already consumes account::account,
account::enums and account::view. Account remains dependent only on Vault/Reference public interfaces. Narrow
Account-owned public synchronization/guard coordination consumed by Study is a candidate implementation family,
not proof of a working design or permission for new module edges/named-interface boundaries.

The owner approved this bounded mechanism class, so no unresolved choice presently prevents handoff creation.
Antigravity must record/prove the selected design's transaction/lock/dependency behavior before implementing this
finding and **stop for OWNER_DECISION_REQUIRED** if the viable solution needs new direction/schema or materially
new architecture/ADR beyond narrow coordination. Assignment-only or asynchronous eventual repair is not approved.
No concrete mechanism was implemented, new reverse edge approved, or architecture verification rerun here.

### BA15-14 boundary assessment

Support for >100 accepted items is explicitly retained; bounded complete inspection is authorized. A per-response
bound is not a total ingestion limit. Any newly necessary total limit outside existing accepted file/resource bounds
returns separately to the owner rather than being silently introduced.

## Observations / limitations

- Initial static/tool/environment/style observations stay observations and **do not enter the handoff**.
- No new finding, debt acceptance, production change or new executable-remediation proof is claimed in this turn.
- ADR-0018 documents the already supplied bounded approvals. It supplements, not replaces, existing foundational
  ADRs; it does not select a broader BA15-13 architecture. Canonical behavior docs must be synchronized with actual
  implementation later, not rewritten now to imply fixes already exist.
- Approval of source-derived concurrency repairs does not remove the need to reproduce them before adding narrow
  safeguards. Failure to reproduce must return to Codex for evidence-based disposition, not speculative lock code.

## Frozen-baseline decisions

The owner's **2026-10-07** eight decisions are recorded completely in owner-decisions.md and grounded by ADR-0018.
All 17 findings are approved, none rejected or accepted as debt. Approval covers the enumerated corrections only;
scope expansion reopens OWNER_DECISION_REQUIRED. Schema v1, migrations, module ownership/directions/diagrams and
unaffected runtime/security/business/API semantics remain frozen.

## Final disposition

**REMEDIATION_REQUIRED**. Preconditions and full approval coverage are satisfied. Exactly one active handoff is now
READY_FOR_IMPLEMENTATION; no other Phase 15 handoff was created or archived. No production/test/POM/config/schema
or diagram implementation was changed by Codex. Initial report/logs/notes remain preserved.

Only approval/ADR/audit/handoff and current-phase governance documentation is written. No commit/push/tag/PR.
After Antigravity implementation and handoff-specific final acceptance, a **closure `$codex-backend-audit`** must
rerun necessary repository-wide full/static/architecture evidence before owner implementation commit/push.
Neither this verdict nor READY_FOR_OWNER_COMMIT from handoff review is BACKEND_AUDIT_READY.

Next step: run Antigravity `/antigravity-implement-handoff` against the single active handoff.
