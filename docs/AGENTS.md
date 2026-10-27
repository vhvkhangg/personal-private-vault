> **Canonical forward instruction:** [Current Owner Delivery Policy](workflow/current-owner-delivery-policy.md) applies to all **new** documentation and review cadences; frozen Phase 17/18 reports are historical source evidence. CRITICAL/HIGH/**MEDIUM** block commit; LOW/INFO are logged for a consolidated milestone/audit pass. Codex reviews each slice, finally each phase; RAG milestones after P19/P22 and two distinct audits after RAG. Do not implement new RAG work or modify `logs/` from docs maintenance.

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

Formal Codex reviews belong in the owning phase's `docs/implementation/phase-N/reviews/` directory.
A review log must state:

- review scope;
- baseline/commit or working-tree context;
- findings with severity and file references;
- test evidence supplied by Antigravity;
- unresolved risks;
- final review status.

A clean review should explicitly state that no blocking findings were found; do not fabricate findings.

## Current documentation templates and lifecycle (2026-10-25)

For all new docs, use `docs/templates/` (`adr.md`, `review.md`, `pre-handoff-review.md`, `handoff.md`, `owner-decision.md`, `slice-closeout.md`, `finding.md`). The new authoring guidance is `.agents/skills/documentation-writing/SKILL.md`. Phase-specific records stay under `docs/implementation/phase-N/`; repository-wide policy belongs under `docs/workflow/`; reusable generic templates under `docs/templates/`.

Finding severity: CRITICAL (data loss/security/corrupt publication), HIGH (blocking correctness/invariant), MEDIUM (material accepted requirement or verifiable coverage gap), LOW (hygiene/docs/limited risk), INFO (nonblocking observation). Severity is separate from disposition: OPEN/CLOSED/NOT_VERIFIED/OWNER_DECISION_REQUIRED, and from final gate: CHANGES_REQUESTED/READY_FOR_OWNER_COMMIT. Only approved-scope findings may be blockers; unapproved feature ideas cannot enter acceptance quietly.

Write **one compact current status**, with links to immutable historical dated records. Avoid stacking contradictory `CURRENT` headers. Historic failures, tests, evidence, source hashes, Owner decisions and actual commit links are kept accurate; do not remove or rename referenced artifacts until dependencies are inventoried and the Owner approves specific deletions.

The Owner has explicitly authorized **Phase 19 S19-1 only** ([Owner decision](implementation/phase-19/owner-authorization-s19-1-2026-10-27.md)); the preparation is already independently reviewed and Owner-published. This scope approval is not an executable handoff. Other slices, frozen-contract amendments and external-index activation remain unauthorized. The Owner commits/pushes; no agent staging/publishing. Documentation closeout uses an actual changed-files package rather than asking the Owner to execute a patch script.
