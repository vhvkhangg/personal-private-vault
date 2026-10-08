# ADR-0018 — Authorize Bounded Phase 15 Backend Remediation

- **Status:** Accepted — bounded remediation closed with BACKEND_AUDIT_READY; owner publication and closeout pending
- **Date:** 2026-10-07
- **Owner decision:** [Phase 15 owner decisions](../implementation/phase-15/owner-decisions.md)

## Context

The comprehensive Phases 0–14 [audit](../implementation/phase-15/reviews/2026-10-06-phase-15-backend-audit.md)
identified 17 findings. Earlier phases are frozen, including runtime, credentials and HTTP compatibility. The owner
explicitly approved conservative corrections with no schema, dependency-direction or unrelated product expansion.
This ADR records that authorization; it is not an implementation acceptance or a replacement architecture.

## Decision

Authorize only BA15-1–BA15-17 within the exact bounds in the owner decision record and the
[single accepted handoff](../implementation/phase-15/handoff.md), now archived after closure:

- Restore Spring Boot-managed Flyway startup; preserve V1/V2 SQL and Hibernate validate, with no manual prerequisite.
- Align affected HTTP boundary rules to existing owning-domain contracts, not weaker competing domain rules.
- Keep the 12–128-character password contract; encode full inputs using standard Spring Security abstractions with
  existing bcrypt verification compatibility and no truncation.
- Reject width/scale inputs outside numeric(19,4) before persistence; no rounding, column widening or currency rules.
- Preserve the YouTube Study/ExternalAccount invariant after assignment using only narrow coordination within the
  existing Knowledge → Account direction. This ADR **does not select or approve a broader coordination architecture**.
- Preserve imports above 100 and add bounded complete HTTP inspection, retaining ordering and atomic execution.
- Apply the listed narrow validation/error/transport/reference/lost-update repairs and documentation/hygiene only.

## Preserved architecture and mandatory stops

Database Schema v1, Flyway SQL, module ownership, named-interface boundaries, dependency directions and diagrams
remain unchanged. ADR-0004/0007/0008/0015/0016/0017 remain accepted; this ADR supplements their authorized behavior
and implementation corrections, rather than superseding their foundational decisions.

Account → Knowledge, cross-module repositories, cross-module writes via the portability JDBC exception, new schema,
generic locking/validation infrastructure and unrelated API changes are prohibited. Invariant enforcement must be
transactional/race-safe, not an asynchronous after-commit repair or assignment-only debt.

Before implementing BA15-13, stop for a fresh owner decision if its viable solution requires a new direction,
schema, or materially new architecture/ADR beyond a narrow coordination mechanism. Any new total ingestion limit
for BA15-14 likewise requires separate approval. Other scope expansion is equally fail-closed.

## Alternatives considered

- External/manual migration prerequisite: owner rejected in favor of normal managed startup.
- Narrower HTTP/domain or byte-limited password contracts: owner chose accepted domain rules and full character range.
- Monetary rounding/column widening: owner chose pre-persistence rejection using unchanged physical types.
- Assignment-time-only Study protection and 100-item ingestion cap: owner rejected both.
- Broad refactoring, reverse dependency, schema evolution and static-warning cleanup: not authorized.

## Consequences and synchronization

Antigravity, not Codex, implements the bounded handoff and supplies per-finding regressions. Affected canonical
runtime/API/security/import docs must be synchronized **with the actual implemented correction**; this decision
does not describe a fix as already implemented. No frozen DBML/matrix/diagram rewrite is needed or authorized.

The prior audit/evidence remains historical. Owner approvals resolve the authorization gate, not the findings.
The [2026-10-07 final review](../implementation/phase-15/reviews/2026-10-07-phase-15-final-codex-review.md)
returned CHANGES_REQUESTED on the submitted implementation; this does not broaden the authorization.
The [previous re-review](../implementation/phase-15/reviews/2026-10-07-phase-15-final-codex-rereview.md) resolves six
specific defects. The [re-review 2](../implementation/phase-15/reviews/2026-10-07-phase-15-final-codex-rereview-2.md)
accepts further regression improvements and passes 996 tests, but retains two regression/evidence/docs blockers.
The [re-review 3](../implementation/phase-15/reviews/2026-10-07-phase-15-final-codex-rereview-3.md)
accepts actual writer contention and additional monetary/rollback/pagination coverage and passes 1007 tests;
two narrowed Medium regression/schema and canonical-note/evidence blockers remain. No new production defect
or architecture expansion is asserted by that review.
The [re-review 4](../implementation/phase-15/reviews/2026-10-07-phase-15-final-codex-rereview-4.md)
accepts most remaining matrix/schema coverage and documentation corrections and passes 1012 tests; two narrowed
Medium blockers remain for successful Finance description update boundaries and canonical-note/pre-fix evidence
corrections. No new production defect or architecture expansion is asserted.
The prior [final acceptance](../implementation/phase-15/reviews/2026-10-07-phase-15-final-codex-acceptance.md)
closes the blocking final-review requirements with independent 1012-test verification: READY FOR OWNER COMMIT,
**but do not commit yet**. FR15-9 was a Low documentation-symbol closure follow-up, not permanent debt;
no new production defect or architecture expansion was asserted by that review.
The [closure audit](../implementation/phase-15/reviews/2026-10-07-phase-15-closure-backend-audit.md) closes 13 findings
and FR15-9 but retains four Medium remnants: BA15-2, BA15-9, BA15-14, BA15-15. These are remaining owning-rule,
coherent-reader, bounded-pagination and semantic-documentation corrections within the original decisions, not new
architecture/behavior approval. The same handoff is CHANGES_REQUESTED; the audit remains REMEDIATION_REQUIRED.
Authorization boundaries remain unchanged; this status synchronization adds no permission or implementation claim.
The [2026-10-08 final review](../implementation/phase-15/reviews/2026-10-08-phase-15-final-codex-review.md)
accepts the submitted reader/offset/documentation repairs but retains three Medium blockers, FR15-10/11/12,
for owner-exact DTO normalization, required regression/schema controls and truthful current evidence. Independent
clean verification passed 1021 tests, zero failures/errors/skips. These are bounded corrections under the same
decisions, not new domain blank/default policy or architectural approval. The same handoff remains CHANGES_REQUESTED;
the audit gate remains REMEDIATION_REQUIRED pending the subsequent dedicated repository-wide closure audit.
Final handoff acceptance must be followed by full/static/architecture closure `$codex-backend-audit` before owner
commit/push and Phase 15 closeout. Phase 16/17 and production deployment remain separately owner-gated.

The [previous Oct8 final re-review](../implementation/phase-15/reviews/2026-10-08-phase-15-final-codex-rereview.md)
closes FR15-10's functional normalization defect and accepts new PUT/persistence/schema assertions. FR15-11
retains a narrowed Medium raw-request/clearing/no-write proof gap; FR15-12 retains only Low current-evidence
precision remnants. Independent 1021-test clean verification passed. The same handoff is CHANGES_REQUESTED
for test/evidence only, next `/antigravity-test-slice`; no further production repair or authority expansion.
This handoff-specific review does not replace the subsequent repository-wide closure backend audit.

The [previous Oct8 final re-review 2](../implementation/phase-15/reviews/2026-10-08-phase-15-final-codex-rereview-2.md)
accepts raw-wire, currency clearing, unchanged collection update state and snapshot/conversion no-write controls.
FR15-11 Medium now requires only eight Vault no-write assertions on existing rejected Location/Shopping/Software
creates; FR15-12 Low retains current evidence precision. Independent clean verification passed 1021 tests,
zero failures/errors/skips (04:00). Same handoff CHANGES_REQUESTED, test/evidence only, next `/antigravity-test-slice`.
No production repair, architecture change, new authorization or repository-wide closure verdict is created.

The [Oct8 final acceptance](../implementation/phase-15/reviews/2026-10-08-phase-15-final-codex-acceptance.md)
closes FR15-11/12, preserves accepted repairs and passes independent 1021/0/0/0 clean verification (03:53).
No blocking final-review findings remained. At that gate the same handoff was READY FOR OWNER COMMIT,
**but do not commit yet**; the required next gate was repository-wide closure `$codex-backend-audit`.
Only BACKEND_AUDIT_READY permits owner implementation commit/push. Existing authorization/owner stops remain unchanged.

## Final repository-wide closure — 2026-10-08

The [final closure audit](../implementation/phase-15/reviews/2026-10-08-phase-15-closure-backend-audit.md)
returns **BACKEND_AUDIT_READY**: BA15-1–BA15-17 and FR15-9/10/11/12 are closed, with no unresolved actionable
finding or owner decision. Fresh full/coverage verification passed 1021 tests, zero failures/errors/skips (04:07);
refreshed static reports were triaged, not declared warning-free. The accepted handoff is archived under
`phase-15/handoff.md`; `ACTIVE.md` is `NO_ACTIVE_HANDOFF`.

This supersedes the earlier pending closure/commit gates, not the historical reports or bounded owner decisions.
The owner may commit/push with `fix(backend): remediate phase 15 audit findings`, then provide the latest repository
package to ChatGPT for Phase 15 closeout only. Phase 15 is not complete/frozen until owner publication and closeout.
No new architecture, schema, dependency edge, ingestion limit, implementation scope, or Phase 16/17 preparation
is authorized by this closure; the original owner-decision stops remain in force.
