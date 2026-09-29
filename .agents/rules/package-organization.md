---
trigger: glob
globs: "backend/src/main/java/**/*.java"
description: "Semantic package organization for public APIs, views/enums, implementations, package-info, and .gitkeep hygiene."
---

# Backend Package Organization Rule

- Organize by business capability first, never by repository-wide technical layer.
- A module base package primarily owns the Spring Modulith module descriptor.
- When a module has multiple public contract types, group them into semantic subpackages and expose only those
  packages with `@NamedInterface`.
- Use capability names such as `catalog`, `entry`, `metadata`, `session`, or `configuration`; avoid generic public
  `service` packages.
- Group public `*View` read models under `view/` and stable public enums under `enums/` when the module has more
  than a trivial number of API types.
- For a non-trivial public capability API, keep the operations interface at the capability root and group public
  request/command records under `command/` and public domain exceptions under `exception/` when this materially
  improves scanability. Preserve the same logical Spring Modulith named interface and prove it with architecture
  verification; do not create extra logical interfaces solely for cosmetic folders.
- Mirror a public capability under `internal/application/<capability>/` for its implementation when that improves
  discoverability.
- Prefer a capability-oriented interface name (`ReferenceCatalog`) plus a descriptive internal implementation
  (`ReferenceCatalogService`). Do not create generic `Service` / `ServiceImpl` or `FooService` / `FooServiceImpl`
  pairs merely by convention.
- Add `package-info.java` to public named-interface packages and meaningful internal capability packages.
- `.gitkeep` is only for an otherwise empty directory that must be retained. Delete it once real tracked content
  makes it unnecessary.
- Before creating a new package, confirm it represents a real responsibility rather than cosmetic nesting.
