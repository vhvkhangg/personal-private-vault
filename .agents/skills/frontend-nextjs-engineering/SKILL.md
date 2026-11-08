---
name: frontend-nextjs-engineering
description: Plan or implement PPV Next.js App Router and strict TypeScript UI, server/client boundaries, private caching and frontend performance. Applies before scaffolding too; backend-only work uses existing backend skills.
---

# PPV Next.js engineering

Start with [ACTIVE](../../../docs/implementation/handoffs/ACTIVE.md) and the [frontend workflow](../../../docs/workflow/frontend-development.md). H0 writes harness guidance only. H1 app work requires all workflow gates, including version-bound Owner visual approval. D11 selects Next.js/TypeScript/shadcn direction, not a dependency version or broad Phase 16 authority.

## Future app decisions

- At H1 preparation, select compatible supported Node/Next/React/TypeScript/shadcn/test versions and one package manager against official version-matched docs. Pin the resulting toolchain/lockfile and review lifecycle/download side effects before installation. H0 chooses none and runs no initializer. Do not overwrite PPV instructions with framework-generated AGENTS files.
- Use App Router Server Components for server reads/rendering; keep interactive client islands small. `use client` creates a serialization/bundle boundary, not a security boundary. Send only validated minimal display DTOs, never tokens or server-only imports. Use strict TypeScript; validate untrusted JSON at the boundary, with explicit empty/error/unknown state types rather than unsafe casts.
- Spring remains the business/policy owner. Reuse its root `ApiResponse` under [ADR-0016](../../../docs/adr/0016-root-http-contract-module-local-adapters.md); inspect actual envelope/errors before typing adapters. The fixed S1 text callback and S2 binary attachment are deliberate exceptions. No duplicate business validator, alternate auth service, generic API framework or invented success state.
- Apply [BFF security](../frontend-bff-security/SKILL.md) to sensitive pages, Route Handlers, Server Actions and transfers. Authenticate/authorize each callable entry, including direct requests. A layout, client hidden control or routing proxy alone cannot protect it. Server Components should call the approved server adapter directly rather than create a loopback HTTP hop through their own public BFF.

## Private caching and concurrency

- Make authenticated/private reads and responses explicitly non-shared/non-stored for the selected Next version; use appropriate fetch no-store and response no-store behavior, and verify rendered/RSC/route cache behavior. Do not rely on changing framework defaults, static generation, public CDN caching, global LRU or `use cache` for private session/Drive data.
- Keep credentials and request state in the approved server session/request boundary, never mutable module globals. Request-local read deduplication is distinct from cross-request caching and must preserve expiry/revocation. A permanently single Owner still has concurrent tabs, slots and refresh requests.
- Slot switch, logout, disconnect and session expiry invalidate the affected client data and in-flight projections. Reject late results from the old slot/action; never display old account pages as the new account.
- Parallelize independent authorized reads when useful; preserve dependencies, mutation order, cancellation and backend capacity. Never parallelize duplicate uploads or refresh-token rotation for speed.

## Components and resources

- Reuse coherent components and accessible shadcn primitives where the approved app needs them; inspect generated code and semantics. Keep domain behavior at its existing owner. Avoid speculative repositories, global stores and interfaces.
- Optimize measured waterfalls, serialization and bundle size first. Lazy-load genuinely expensive optional client features; do not add analytics/CDN/fonts/animation libraries as presumed improvements. Pagination is bounded by the existing Drive API; do not create a broad prefetch/virtualization subsystem for a 1–50 page without evidence.
- File streams stay bounded and cancellable per the security skill. Do not turn binary bodies into React state, base64, `arrayBuffer()`/whole-response `blob()` buffers in the BFF, or cached RSC data. A browser-selected File handle is not permission to read it all into JS memory.
- Use [design/accessibility](../frontend-design-accessibility/SKILL.md) for visual changes and [frontend-testing](../frontend-testing/SKILL.md) for actual type/lint/format/component/BFF/browser evidence. All long commands use the existing normal-profile watchdog, no Node/browser extended exemption.

Selective advisory sources, identities and exclusions are recorded in the [H0 provenance manifest](../../../docs/implementation/maintenance/2026-11-07-gdi26-s3-h0-preparation/frontend-guidance-provenance-h0-2026-11-07.md). They cannot override PPV authority. No frontend build or performance result exists merely because this skill does.
