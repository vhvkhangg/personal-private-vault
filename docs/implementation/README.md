# Implementation Plans

## Completed and frozen

- [Backend Phase 0 — Maven / Spring Boot / Spring Modulith Bootstrap](backend-phase-0.md) — **COMPLETE / FROZEN**
- [Backend Phase 0 — Repository Safety Hook Test Evidence](backend-phase-0-governance-test-evidence.md)
- Final Phase 0 Codex review: [`../reviews/2026-09-27-backend-phase-0-final-governance-codex-review.md`](../reviews/2026-09-27-backend-phase-0-final-governance-codex-review.md)

## Active implementation phase

- [Backend Phase 1 — PostgreSQL/Flyway Schema v1 + Reference & Vault Foundation](backend-phase-1.md) — **ACTIVE / IMPLEMENTATION & VERIFICATION COMPLETE (AWAITING OWNER COMMIT)**
- [Backend Phase 1 — Implementation Files](backend-phase-1-owner-files.md)
- [Backend Phase 1 — Flyway V1 Manifest](backend-phase-1-flyway-manifest.md)
- [Backend Phase 1 — Test Verification Evidence](backend-phase-1-test-evidence.md)

## Operational guidance

- [Antigravity CLI project permissions](antigravity-cli-permissions.md)

Architecture decisions remain in `docs/adr/`; frozen architecture/database baselines remain under
`docs/architecture/`, `docs/database/`, and `docs/repository/`.

## Agent-driven workflow

- [Active Codex → Antigravity handoff](handoffs/ACTIVE.md)
- [Handoff workflow](handoffs/README.md)
- [Backend Phase 1](backend-phase-1.md)

Current implementation is agent-driven: Codex plans/reviews, Antigravity implements/tests, owner commits/pushes.
