# Documentation Scope Instructions

These instructions apply to `docs/`.

## Language and accuracy

- Write documentation in English.
- Describe implemented behavior as implemented, not as planned.
- Clearly mark deferred or proposed work.
- Do not invent implementation details to make documentation look complete.

## Frozen artifacts

The v1 database, module boundaries, architecture diagrams, and repository/package tree are frozen baselines.

Do not change them without explicit owner approval.

When an approved architectural decision changes:

1. update or add the relevant ADR;
2. update the canonical source artifact;
3. update derived documentation and exports;
4. ensure cross-links remain valid.

## Canonical sources

Prefer these editable sources:

- DB schema: DBML.
- C4 model: Structurizr DSL.
- Visual architecture/BFD: draw.io source.
- Small flows/sequence diagrams: Markdown-embedded Mermaid where appropriate.

SVG/PNG exports are derived artifacts, not the canonical source.

## Review logs

Formal Codex pre-commit reviews belong in `docs/reviews/`.
A review log must state:

- review scope;
- baseline/commit or working-tree context;
- findings with severity and file references;
- test evidence supplied by Antigravity;
- unresolved risks;
- final review status.

A clean review should explicitly state that no blocking findings were found; do not fabricate findings.
