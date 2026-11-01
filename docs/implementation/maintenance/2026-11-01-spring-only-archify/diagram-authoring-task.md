# One-off Codex task — Spring-only Archify diagram authoring

**Gate:** NOT EXECUTABLE UNTIL the independent maintenance preparation review returns `READY_FOR_OWNER_COMMIT` and the Owner has committed/pushed those reviewed preparation docs. This task is part of the bounded Owner authorization, not an additional active software handoff. A separate Owner decision is only required for materially changed scope or unvetted execution risk.

**Codex CLI launcher (after gate):**

```text
Read and execute docs/implementation/maintenance/2026-11-01-spring-only-archify/diagram-authoring-task.md using the reviewed Owner scope and diagram-plan.md. Create only the four Spring-only non-RAG Archify views; no application code changes or publication.
```

## Required author actions

1. Inspect current `main`, branch/index/working tree, the actual approved preparatory review, this scope, source/ADR evidence and Archify upstream. If preparation has not passed/published or scope collides with another active handoff, **STOP**.
2. Vet the selected Archify version including its installation code and license/network behavior; record commit/package hash. No unreviewed auto-updating installs, `curl | bash`, remote upload or exposure of private data. If unsatisfied, STOP and request an Owner decision.
3. Generate only `SAD-01`–`SAD-04` using real repo evidence and the vetted local Archify toolchain. Save editable typed JSON, portable validated HTML and static SVG (or report a concrete verified export limitation). Respect the output tree in `diagram-plan.md` or a reviewed equivalent.
4. Validate schema, `finalize`/check/browser interactions as supported by vetted version, source anchors and provenance, module counts, static fallback legibility and all A1–A8 acceptance checks. Review the actual rendered diagrams, not only schema checks. No RAG node, edge, runtime, figure or frozen RAG data. Keep generated HTML free of remote executable dependencies where possible.
5. Write a factual implementation handback at `docs/implementation/maintenance/2026-11-01-spring-only-archify/diagram-handback.md`: exact authored artifact inventory; Archify version/hash; controls; tool/CLI commands, actual exits, test limits, source links, changes versus accepted baseline, and outstanding issues. Keep all logs unique under ignored `logs/`.
6. Stop **without staging/commit/push**. Have a **separate independent Codex session** review using the following brief request before Owner commits:

```text
Independently review the four Spring-only Archify artifacts against docs/implementation/maintenance/2026-11-01-spring-only-archify/{owner-scope-and-authorization.md,diagram-plan.md,diagram-handback.md}. Verify code provenance, browser/HTML/SVG behavior, safety, non-RAG exclusion and prior preservation; write a dated review and return READY_FOR_OWNER_COMMIT only if all required A1-A8 gates pass. Do not alter implementation or publish.
```

**Stop conditions:** preparation not published; unvetted tool; material source drift; missing real source provenance; RAG overlap; unsafe network behavior; failed diagram/browser/render validation; unapproved tracked changes. Do not replace FAILED with synthetic PASS or silently abandon a required view.
