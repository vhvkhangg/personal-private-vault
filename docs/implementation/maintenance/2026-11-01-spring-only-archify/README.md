# Spring-only Architecture Interactive Diagrams — preparation

**Status: `READY_FOR_OWNER_COMMIT / PREPARATION_ONLY / NOT_IMPLEMENTED`.** The [independent preparation review](reviews/2026-11-01-independent-preparation-review.md) found no blocking preparation findings. The Owner authorizes this tightly scoped docs/diagram deliverable; execution is gated on Owner-published reviewed preparation and a vetted Archify toolchain. No diagrams, third-party execution, or installation is authorized **before that gate**. No active implementation handoff; diagram artifact acceptance remains NOT_RUN.

**Controlling documents:**

- [Owner scope and authorization](owner-scope-and-authorization.md)
- [Diagram plan and acceptance contract](diagram-plan.md)
- [CSA26 publication closeout](../2026-11-01-current-system-readonly-audit/owner-publication-closeout-2026-11-01.md)
- [Canonical active-handoff register](../../handoffs/ACTIVE.md)

## Workflow

1. Owner applies this **docs-only** package to current `main` baseline `4cf62041796d0b510255ee7014261221cafe3d5d`; inspect `git diff` and confirm only intended paths changed. Do not commit preparation yet.
2. In a separate Codex session run `$codex-pre-handoff-review` using the approved maintenance-only preparation mode. Codex reviews *only* this scope against source, repo policy, Archify upstream license/security/package files, and current diagram gaps. It must not draw, install tools or create a live handoff during preparation review.
3. This is a **documentation/diagram-only** maintenance scope; per the existing review skill, a successful preparation review should return `READY_FOR_OWNER_COMMIT` (and not create a live implementation handoff). If `CHANGES_REQUESTED`, correct the scoped docs and obtain independent re-review. Owner publishes accepted preparation.
4. Then in a **new Codex session** invoke the [diagram authoring task](diagram-authoring-task.md) with the short launcher written there. This Owner authorization already covers the four diagrams **after** the preparation gate, subject to vetted third-party tooling and actual repository state. An independent **different** Codex session reviews completed artifacts and evidence before Owner publication.
5. Keep `ACTIVE.md = NO_ACTIVE_HANDOFF` for this bounded documentation-only work. Do not invoke `$codex-create-handoff` or `$codex-final-review` as if this were a software implementation handoff. Do not invoke `$codex-backend-audit` or reopen S19-2.

No new skill is required for this preparation. External Archify is a tool candidate, not a repository-accepted dependency at issuance.

**Next step:** Owner | commit/push reviewed preparation, then start a new Codex session with the launcher in [diagram-authoring-task.md](diagram-authoring-task.md) | Spring-only maintenance scope and [independent preparation review](reviews/2026-11-01-independent-preparation-review.md) | READY_FOR_OWNER_COMMIT for preparation only; vetted local execution and separate artifact review remain required | no Spring remediation loop; CSA26 2/3 consumed and stopped; S19-2 counters unchanged.
