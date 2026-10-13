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

**Current gate (2026-10-13):** owner-authorized Phase 17 RAG foundation preparation, then separately gated planned phases 18–22, precedes deferred Phase 16 Frontend, preserving the original numbers; see accepted ADR-0019 and `docs/implementation/phase-17/owner-decisions.md`. No RAG implementation handoff exists yet. See `docs/implementation/rag-phase-plan.md`.

See `docs/workflow/owner-phase-workflow.md`.

## Agents

- `backend-implementer`
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

- `local-rag-engineering` — proposed Phase 17 offline Python/RAG, source-privacy and evaluation conventions

## Workflow/review skills

- `codex-review-coverage` — shared 29-dimension evidence checklist (folder structure, SOLID/patterns, API/common placement, concurrency, security, performance, AI quality)
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
