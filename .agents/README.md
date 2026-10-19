# Agent Customization

## Phase 3+ workflow

1. ChatGPT prepares next-phase docs/tooling.
2. Codex `$codex-pre-handoff-review`.
3. Owner commits/pushes preparation after `READY FOR HANDOFF`.
4. Codex `$codex-create-handoff`.
5. Antigravity `/agents` → `backend-implementer`.
6. Antigravity `/antigravity-implement-handoff`.
7. Codex `$codex-final-review`.
8. Owner commit/push.
9. ChatGPT closeout + next preparation.
10. Historical milestone reviews applied after Phase 3/6/9/12. **Closed Phase 15 audit workflow (history only; not for current RAG phases):**
    `$codex-pre-handoff-review` returned `READY FOR AUDIT`, the owner committed preparation, then `$codex-backend-audit` ran.
    It supersedes the former post-15 milestone.

**Current gate (2026-10-18):** [ADR-0022](../docs/adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) is OWNER_APPROVED / CANONICALLY_SYNCHRONIZED for external snapshots/whole-file/NO_CHANGE. [ACTIVE](../docs/implementation/handoffs/ACTIVE.md) remains CHANGES_REQUESTED / STOPPED / INCOMPLETE / NOT_VERIFIED, documentation-only. Live ingestion DISABLED / DEFERRED; snapshot ingestion inactive. G1–G4 and FR18-1–11 OPEN; 2/2 Antigravity rounds exhausted, no automatic implementation loop. Next: independent documentation synchronization review. Frozen Phase 17 and unrelated skill/workflow rules remain unchanged.

See `docs/workflow/owner-phase-workflow.md`.

## Agents

- `backend-implementer` (shared Java + Python RAG implementation; no extra agent per language unless a real role boundary appears)
- `architecture-auditor`

Reuse them by default. Add another agent only for a materially different role/tool/permission boundary.

## Engineering skills

- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `backend-testing`
- `authentication-security`
- `people-domain-modeling`
- `fiction-domain-modeling`
- `film-domain-modeling`
- `media-domain-modeling`
- `location-domain-modeling`
- `account-domain-modeling`
- `knowledge-domain-modeling`
- `collection-domain-modeling`
- `feed-import-workflow-modeling`
- `finance-journal-personal-domain-modeling`
- `global-search-domain-modeling`
- `rest-api-http-contracts`
- `backend-integration-portability-storage`

- `local-rag-engineering` — approved Phase 17–22 scope-aware local RAG constraints
- `python-rag-coding` — Python/FastAPI/OCR/parser/ML glue and compatible locked tooling
- `bounded-command-execution` — executable 20-minute default / 35-minute future full-suite watchdog

## Workflow/review skills

- `codex-review-coverage` — shared 29-dimension evidence checklist (folder structure, SOLID/patterns, API/common placement, concurrency, security, performance, AI quality)
- `codex-implementation-takeover` — after two failed Antigravity remediation/re-review cycles, separate from independent reviewer
- `codex-pre-handoff-review`
- `codex-create-handoff`
- `antigravity-implement-handoff`
- `codex-final-review`
- `codex-milestone-review`
- `codex-backend-audit`
- `graphify-context`
- `architecture-change`
- `database-migration-review`

## Hooks

Reuse the repository safety hook. Add phase-specific hook logic only for a genuinely new command/security need.

## Graphify

Use `scripts/setup-graphify.ps1` and `scripts/refresh-graphify.ps1`.
Generated `graphify-out/` is navigation context only.
