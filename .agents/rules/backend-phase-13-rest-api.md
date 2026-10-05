---
trigger: model_decision
description: "Apply when preparing, implementing, testing or reviewing Backend Phase 13 REST/API and module HTTP exposure."
---

# Backend Phase 13 — REST/API

Canonical scope:

`docs/implementation/phase-13/README.md`

Implementation requires:

1. Phase 13 preparation review = `READY FOR HANDOFF`;
2. owner commit/push of accepted preparation;
3. active Phase 13 Codex implementation handoff.

Use `rest-api-http-contracts` plus authentication/security, Java/Spring, modular-monolith, backend-testing,
pragmatic-SOLID and reuse/consistency guidance.

Constraints:

- `/api/v1` only for application endpoints;
- exact shared `ApiResponse` family;
- narrow root-package HTTP support per ADR-0016; no new application module;
- controllers/DTOs/mappers/advice are owner `internal.web` adapters;
- explicit HTTP DTOs; never expose entities directly;
- no controller crosses module internals/repositories;
- public auth routes only as listed in canonical Phase 13 scope;
- every other business route Bearer-protected;
- module/domain exception mapping is explicit and privacy-safe;
- pagination metadata must be truthful;
- OpenAPI mirrors actual DTO/security behavior;
- no new domain use case, schema, scheduler, object storage, hard delete, CORS/cookie policy, frontend or Phase 14+ work.

After accepted Phase 13 implementation, owner commit/push then ChatGPT closeout/Phase 14 preparation; no milestone
review is due until Phase 15.
