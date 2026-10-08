# Phase 15 — Bounded Owner Remediation Decisions

Recorded: **2026-10-07**, from the owner's explicit instruction in this conversation.
Scope: findings BA15-1 through BA15-17 in the [initial audit](reviews/2026-10-06-phase-15-backend-audit.md).
These are approvals to remediate, **not closed findings or accepted debt**. No rejected or debt-accepted finding exists.

The [authorization re-review](reviews/2026-10-07-phase-15-authorized-remediation-codex-review.md),
[ADR-0018](../../adr/0018-phase-15-bounded-backend-remediation.md) and the single
[active handoff](../handoffs/ACTIVE.md) implement the governance gate only; Codex does not implement the fixes.

## 1. BA15-1 — APPROVED

Repair normal fresh-database startup using the smallest compatible Spring Boot-managed Flyway integration.
Preserve the existing Flyway SQL migrations and Hibernate validate behavior.
Do not introduce Hibernate auto-DDL and do not make external/manual migration a runtime prerequisite.

## 2. BA15-2 — APPROVED

Align the affected HTTP DTO validation/default/null/blank/length/range rules with the already accepted owning-domain
contracts. The owning domain/application validators remain the source of truth. Do not weaken the domain rules and
do not introduce a generic validation framework solely for this remediation.
Add boundary and OpenAPI regression coverage for the corrected contracts.

## 3. BA15-3 — APPROVED

Preserve the documented 12–128 character password contract.
Use a full-input password encoder for newly encoded passwords while preserving verification compatibility with
existing bcrypt hashes through the existing delegating/migration-compatible approach.
Never silently truncate password input. Prefer the smallest standard Spring Security-compatible solution without
unnecessary new infrastructure.

## 4. BA15-12 — APPROVED

Reject monetary values that cannot be represented exactly by the existing numeric(19,4) contract.
Reject excessive fractional precision rather than silently rounding or normalizing it.
Add width/scale validation before persistence. Do not widen database columns or introduce currency-specific
precision rules.

## 5. BA15-13 — APPROVED WITH ARCHITECTURAL BOUNDARY

Preserve the ExternalAccount/YouTube Study invariant after assignment; do not accept assignment-time-only behavior
as debt. Implement the smallest coordination/guard that preserves the existing Knowledge → Account dependency
direction. Do not introduce Account → Knowledge dependency, cross-module JPA access, schema changes, or misuse the
portability exception.

If preserving the invariant requires a new dependency direction, schema change, or materially new architecture/ADR
beyond a narrow coordination mechanism, stop and return **OWNER_DECISION_REQUIRED for BA15-13 before implementation**.
Recording this bounded authorization in ADR-0018 does not approve such an expansion or select an unverified mechanism.

## 6. BA15-14 — APPROVED

Preserve support for import jobs above 100 items. Provide complete bounded HTTP inspection/review using pagination
or an equivalent bounded mechanism so every accepted item can be reviewed. Do not impose a new 100-item ingestion
cap merely to match the existing endpoint.

Preserve ordering, complete decision coverage, atomic execution, and reasonable existing resource bounds.
If an additional total ingestion limit becomes necessary beyond an already accepted resource/file-size bound,
return that separately for owner approval.

## 7. BA15-4/5/6/7/8/9/10/11/17 — APPROVED

Apply the narrow error, HTTP framing, binary integrity, validation, referential-integrity and concurrency/lost-update
repairs described in the audit report. No schema changes, new dependency edges, generic locking framework, unrelated
behavior changes or broad exception mapping are authorized.
Use targeted regression tests, including deterministic PostgreSQL concurrency tests where required by the audit.

## 8. BA15-15/16 — APPROVED

Apply documentation/OpenAPI and repository-hygiene corrections only. Document the actual existing non-obvious
semantics identified by the audit. Add missing ownership package descriptors without introducing new module/named-
interface boundaries. Remove only the confirmed stale placeholder files and update the identified stale architecture
prose. Preserve historical records.

## Authorization boundary and workflow

These decisions authorize **only** the bounded remediation above. Any expansion must stop and return
**OWNER_DECISION_REQUIRED**; no approval is inferred from a test pass, static warning, historical TODO, or this file.
Schema v1, migrations, module ownership/dependency directions and unaffected behavior stay frozen.
The earlier alternative choices in the historical audit are superseded by these explicit choices, not implemented.

Antigravity implements/tests the single `phase-15-backend-audit-remediation` handoff. After `$codex-final-review`
accepts that handoff, `$codex-backend-audit` must perform repository-wide closure verification **before owner
implementation commit/push**. Only BACKEND_AUDIT_READY permits that commit. Frontend/RAG/deployment remain deferred.
