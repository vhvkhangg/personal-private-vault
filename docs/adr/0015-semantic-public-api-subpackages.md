# ADR-0015 — Organize Public Module APIs into Semantic Named-Interface Subpackages

- **Status:** Accepted
- **Date:** 2026-09-28

## Context

The Phase 1 `reference` and `vault` module base packages accumulated capability interfaces, read models, and enums
side by side. This made related concepts harder to scan and does not scale well as later modules add more public
contracts.

A flat package also encourages technical naming such as `Service` / `ServiceImpl`, which hides domain intent.

## Decision

Keep package-by-business-capability from ADR-0002, and refine public API organization as follows:

- the application-module base package owns the module descriptor;
- non-trivial public APIs are grouped into semantic subpackages such as `catalog`, `entry`, `metadata`, `session`,
  or `configuration`;
- public read models are grouped under `view/` and stable public enums under `enums/` when useful;
- public subpackages are explicitly exposed with Spring Modulith `@NamedInterface`;
- internal implementations may mirror the capability under `internal/application/<capability>/`;
- capability interfaces use domain/capability names (`ReferenceCatalog`), while implementations use descriptive
  implementation names (`ReferenceCatalogService`); generic `Service` / `ServiceImpl` pairs are avoided;
- meaningful new packages include `package-info.java`;
- `.gitkeep` is removed when real tracked content makes it unnecessary.

## Rationale

The structure keeps related concepts discoverable without sacrificing Spring Modulith encapsulation. Named
interfaces make exposure deliberate, while mirrored internal capability packages keep implementations easy to
locate. Capability-oriented names communicate intent better than technical suffix conventions.

## Consequences

- Cross-module callers import only explicitly exposed named-interface packages.
- Package moves are architecture changes and must keep architecture/package documentation synchronized.
- Do not create subpackages for single trivial types merely to satisfy a folder template.
- Phase 1 behavior/schema remains unchanged by the package-layout refactor.

## Relationship to earlier ADRs

This ADR refines, but does not supersede, ADR-0002 (package by business capability).
