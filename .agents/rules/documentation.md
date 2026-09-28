---
trigger: glob
globs: "docs/**/*.md, docs/**/*.dbml, docs/**/*.dsl, docs/**/*.drawio"
description: "Documentation synchronization and frozen-baseline rules."
---

# Documentation Rule

- Documentation is written in English.
- Distinguish current implementation from planned/deferred work.
- Preserve canonical editable sources.
- Do not silently change frozen v1 artifacts.
- Architecture changes require an ADR update/new ADR and synchronized affected docs.
- Formal Codex reviews are recorded under the owning phase's `docs/implementation/phase-N/reviews/` directory.
