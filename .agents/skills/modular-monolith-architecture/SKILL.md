---
name: modular-monolith-architecture
description: Enforce Personal Private Vault Spring Modulith ownership, allowed dependencies, public/internal boundaries, communication style, and architecture-change governance.
---

# Modular Monolith Architecture

- Each module exclusively owns its entities/repositories.
- Never import another module's `internal` package.
- A database foreign key does not grant Java persistence access.
- Cross-module callers use exposed public APIs/named interfaces.
- Check `docs/architecture/module-dependency-matrix.md` before adding dependencies.
- Prevent module cycles.
- Use synchronous APIs for immediate results and events for decoupled side effects.
- Package by business capability; avoid root technical/common/util dumping grounds.

If a frozen module boundary must change, stop and route through architecture-change governance.


## Public API package organization

- Keep the module base package as the module descriptor/ownership boundary.
- Group non-trivial public APIs by semantic capability and expose those subpackages explicitly with Spring
  Modulith `@NamedInterface`.
- Put public `*View` records in `view/` and stable public enums in `enums/` when that reduces base-package clutter.
- Mirror capability names under `internal/application/<capability>/` where useful.
- Avoid generic `Service` / `ServiceImpl` naming; name public interfaces for the capability they provide.
- A real package gets `package-info.java`; `.gitkeep` is removed as soon as real tracked content exists.
