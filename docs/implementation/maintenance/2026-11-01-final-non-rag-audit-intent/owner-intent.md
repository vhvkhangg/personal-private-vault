# Owner intent — final comprehensive non-RAG backend audit before Frontend

**Recorded:** 2026-11-01  
**Status:** `FORWARD_OWNER_INTENT / NOT_YET_AUTHORIZED_FOR_EXECUTION`. Do not start audit or any implementation from this record alone. Independent preparation and current authorization are required after the planned backend features are accepted/published.

## Trigger / order

After Spring-only diagrams (separate documentation slice) and accepted new backend capabilities for (1) Universal Inbox/Save for Later, (2) Daily Trends Radar, (3) two-account Google Drive integration, (4) Dashboard and Frontend API readiness, perform the **one comprehensive final audit** on the then-current `main`, **before Phase 16 Frontend implementation**. Each feature must still have its own security/architecture/functional independent review. Ideaverse Reader is deferred. Automated media downloading, copying/backup of third-party fiction, Hermes and OpenClaw integrations are **removed** from the current product roadmap.

## Audit source and scope

Extend the **existing** `.agents/skills/codex-backend-audit/SKILL.md` prospectively with an explicitly selected `current-non-rag` mode when scheduling the audit. Keep its historical Phase 15 mode/records immutable; **do not create a new backend-audit skill**. The new mode must require a fresh Owner-approved audit contract and avoid re-opening frozen Phase 15 by default.

Audit all non-RAG Spring Boot application modules and current cross-cutting components; actual domain semantics, APIs/OpenAPI, auth/privacy, transactions, SQL/JPA/Flyway/DBML/migration identity, storage, ingestion/feeds/Drive, race/recovery, resources, tests, dependencies/supply chain, runtime/ops, CI/build, security controls and dev/governance mechanisms. Audit the entire documentation tree (especially `docs/adr/`, `docs/architecture/`, `docs/database/`) for accuracy, completeness, spelling, contradictions, stale current pointers, diagrams and migration/source fit. Preserve signed/issued historical evidence. The RAG module and Python engine are excluded as functional audit targets; shared boundary effects on non-RAG must still be checked. Do not run suspended QA/inference or alter RAG under the non-RAG audit.

## Owner decision: ALL actionable findings, including LOW / INFO

The Owner requires **resolution of every substantiated, actionable non-RAG finding across CRITICAL/HIGH/MEDIUM/LOW/INFO** before declaring the final non-RAG audit complete. No severity-only automatic defer/waiver. Consolidate proportional changes into a finite number of bounded, independently reviewed remediation batches; do not create an endless one-finding-per-round loop. Every item gets an evidence-backed final disposition, preferably `FIXED_AND_VERIFIED`. If it proves to be a false positive, duplicate or a non-defect observation, document evidence and classify `NOT_A_DEFECT`, `DUPLICATE` or `NOT_APPLICABLE` rather than making unnecessary code modifications or falsely claiming `FIXED`. If material constraints prevent fixing a real item, **STOP / OWNER_DECISION_REQUIRED**; no automatic acceptance of unresolved LOW/INFO. Do not carry unresolved non-RAG actionable findings to frontend merely because severity is LOW or INFO.

This Owner preference does not overwrite previous independent review findings, reopen S19-2, invent accepted quality for C2 or reset any historical remediation counters. Frozen source/API/schema/ADR changes still require explicit Owner approval and independent tests.

## Future readiness gate

Final result can be `NON_RAG_BACKEND_AUDIT_READY` only after the full agreed audit coverage is actually executed or limitations transparently adjudicated by Owner, all in-scope actionable findings resolved and independently verified, documentation diagrams/source accurately synchronized, and evidence is referenced on the final published HEAD. Frontend prep then requires its own authorization; audit completion alone does not auto-authorize Phase 16.
