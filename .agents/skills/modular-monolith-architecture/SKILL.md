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
