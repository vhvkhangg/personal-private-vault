**D24 CURRENT - 2026-10-21:** Overall Phase 18 external document target is **Obsidian Markdown `.md` ONLY**. Owner authorizes the new initial **Slice 3 Verified Indexing D+F**; see [D24](../docs/implementation/phase-18/owner-decisions.md#2026-10-21--d24-overall-markdown-only-phase-18-and-slice-3-verified-indexing), [executable handoff](../docs/implementation/phase-18/slice-3-verified-indexing-handoff.md), [baseline/applicability/cleanup](../docs/implementation/phase-18/slice-3-markdown-scope-and-baseline-2026-10-21.md). Slice 2/D23 producer-local scope is CLOSED and prior work is **published** at verified main `ef32d41b33dd00e5fa08cf63977e91b8936ef3d5`, including unaccepted mixed changes; publication does not accept them. The former selective staging/commit workflow is superseded and must not be reopened. TXT/Office/PDF/OCR are excluded from Slice 3 and final Markdown acceptance; temporarily retain without use/certification, then remove unnecessary implementation/dependencies/tests/fixtures once at Slice 4 end before P18-FINAL. No Slice 4 implementation now. Phase 17 FROZEN; Slice 1/D14 CLOSED; D22 originals NOT_RECOVERED / original preservation FAILED; FR18-3 OPEN; Java publication NOT ACCEPTED; historical full Java gate FAILED until genuine successful rerun; indexing INACTIVE; live originals DISABLED/DEFERRED; P18-FINAL NOT ACCEPTED. Historical Antigravity remediation 2/2 remains exhausted; this initial slice grants no extra remediation rounds. Preparation only in this session; no activation or agent publishing.

## Preserved pre-D24 status and contract history

# Backend Scope Instructions

These instructions apply to `backend/` and refine root `AGENTS.md`.

## Implementation authority

Antigravity may write substantial production implementation **only** when following the active Codex handoff at:

`docs/implementation/handoffs/ACTIVE.md`

If the handoff is missing, not marked `READY_FOR_IMPLEMENTATION`/`CHANGES_REQUESTED`, or does not cover the
requested production change, stop and request a Codex handoff instead of improvising scope.

Codex remains planning/review-only for production implementation unless the owner explicitly overrides the workflow.

Phase 17 Alternative B/E1 **Foundation is COMPLETE — FROZEN** after independent final re-review 3,
owner implementation commit/push `829d294aca7d916851348ff21004c631915d572e` and
[formal closeout](../docs/implementation/phase-17/closeout.md). Its `P17-RAG-FOUNDATION` handoff
is archived; [ACTIVE](../docs/implementation/handoffs/ACTIVE.md) was reset at that closeout and now contains the separate Phase 18 `P18-DOCUMENT-INDEXING` handoff.
FR17-1–11 and AB-1–6 are closed for bounded Foundation scope. `AUTH-CLOCK` remains separately OPEN
in Authentication; no blanket test waiver. Default containment and **disabled live external-file
indexing** remain until independent Windows/NTFS R7 proof and an accepted new handoff.

## Current RAG authorization — 2026-10-18

[ADR-0022](../docs/adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) is the **OWNER_APPROVED / CANONICALLY_SYNCHRONIZED** external snapshot target: whole-file consent, exact ORIGINAL_BYTES, guarded SHA-256/length NO_CHANGE and C_N. Its exact external-only applicability replaces listed ADR-0021/P17 clauses; frozen Vault behavior and unaffected Phase 17 guarantees remain.

[ACTIVE](../docs/implementation/handoffs/ACTIVE.md) is **P18-DOCUMENT-INDEXING / CHANGES_REQUESTED / STOPPED / INCOMPLETE / NOT_VERIFIED**. Present authorization is documentation-only synchronization, not implementation, tests, migrations, experiments, worker launch or ingestion activation. Live originals remain DISABLED / DEFERRED pending separate future authorization/R7 acceptance; snapshot ingestion inactive. G1–G4 and FR18-1–11 remain OPEN; preserve credited repairs and mandatory producer/quality/resource/P18-FINAL obligations. **2/2 Antigravity rounds exhausted; no third round/automatic takeover loop. Next gate: independent documentation synchronization review.** Owner-only commits/pushes; later implementation requires separate finite authorization/handoff.

## Package structure

Keep code inside the owning business module.

Do not create global root technical packages such as `controller`, `service`, `repository`, `entity`, or `dto`.
Technical layers belong under each business capability/module.

## Module access

- Do not access another module's JPA repository/entity directly.
- Do not import another module's `internal` package.
- Cross-module access goes through exposed APIs/named interfaces.
- Validate dependency directions against the frozen matrix.
- Prefer synchronous APIs for immediate results and events for decoupled side effects.

## Persistence

- PostgreSQL is the production database.
- Flyway migrations define the physical schema.
- Frozen DBML is the logical v1 baseline.
- Do not use H2 assumptions.
- Keep transactional boundaries in application services/use cases.
- Avoid persistence/lazy-loading leakage into public API serialization.

## Lombok

Lombok is compile-time boilerplate support, not a design shortcut.

Allowed examples:

- `@Getter` on JPA entities where appropriate;
- protected `@NoArgsConstructor` for JPA;
- `@RequiredArgsConstructor` for Spring services with final dependencies;
- focused builders/value-object helpers when justified.

Avoid:

- `@Data` on JPA entities;
- automatic `toString`, `equals`, or `hashCode` across lazy associations;
- mutable public setters merely because Lombok can generate them.

## Build/test

- Maven is the backend build tool.
- Prefer Spring Boot-managed dependency versions.
- Antigravity may compile/test/fix iteratively within an active handoff.
- Use Testcontainers PostgreSQL for persistence integration tests.

## Phase 13 shared HTTP contract exception

During an accepted active Phase 13 handoff, ADR-0016 permits a small allowlisted set of shared HTTP envelope/error/
OpenAPI types directly in `com.vhvkhangg.personalprivatevault`.

Do not turn this into a global technical subpackage or generic utility layer. Business controllers/DTOs/mappers/advice
still belong under their owning module's `internal/web` package.

## Phase 14 portability/object-storage exception

Phase 14 is complete/frozen. Its accepted implementation contains:

- the `portability` leaf module and ADR-0017 read-only JDBC snapshot adapter;
- Media-owned S3-compatible binary storage infrastructure;
- the approved binary HTTP routes/configuration/health integration;
- directly required Maven/test dependencies.

The portability JDBC exception remains read-only and export-only. It does not authorize generic cross-module SQL,
repository/entity sharing, schema changes, provider-specific domain types, schedulers, hard delete, deployment,
frontend, or RAG work. Future changes require an explicit approved scope.

## Phase 15 backend audit

Phase 15 is audit-first. Codex may inspect the entire non-RAG backend and run audit-only tooling, but production
changes require the Phase 15 remediation handoff created from proven findings. Antigravity must not remediate audit
observations or speculative cleanup outside that handoff.
