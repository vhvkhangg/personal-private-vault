---
trigger: glob
globs: "docs/**/*.md, docs/**/*.dbml, docs/**/*.dsl, docs/**/*.drawio"
description: "Documentation synchronization and frozen-baseline rules."
---

**CURRENT DOCUMENTATION CONTRACT:** Use `docs/templates/` and [Owner policy](../../docs/workflow/current-owner-delivery-policy.md) for current vs dated-historical files, per-slice review, per-phase final review, post-P19/P22 milestone reviews, later two RAG/Backend audits and severity. Never rewrite immutable historical receipts; Owner retains `logs/`. Document closeout as changed-file package without patch scripts.



# Documentation Rule

- Documentation is written in English.
- Distinguish current implementation from planned/deferred work.
- Preserve canonical editable sources.
- Do not silently change frozen v1 artifacts.
- Architecture changes require an ADR update/new ADR and synchronized affected docs.
- Formal Codex reviews are recorded under the owning phase's `docs/implementation/phase-N/reviews/` directory.

**Forward-scope consistency:** For an approved current documentation write, apply [Documentation synchronization](../../docs/workflow/documentation-synchronization.md) at the relevant handoff/review boundary. Classify current vs immutable history; record material out-of-scope drift rather than expanding the edit. This rule does not authorize cleanup B or change frozen artifacts.
