# BF26-NR-AUDIT — Owner direction and preparation authority contract

**Status:** `OWNER_DIRECTED_FOR_PREPARATION_ONLY / EXECUTION_NOT_AUTHORIZED`. This is a record of the Owner's explicit current-conversation direction, **not** a signed review, independent acceptance, Owner publication or permission to edit the repository. Source baseline: `4e4e2cb50ac84c86b0df93ad67f770648c7d32db` (GitHub `main`, 2026-11-15).

## O-01 — Requested outcome

Evaluate the **entire current Non-RAG system**, not code alone: harness, instructions/governance, docs, architecture/product design, Spring Backend, tests and operational boundaries. Determine whether existing features meet the Owner's real needs **using the simplest sufficient mechanisms**. Distinguish a proven defect from an optional enhancement and an unverifiable claim. Make all conclusions falsifiable by another independent reviewer (Codex or Claude).

## O-02 — Ordered assessment

1. **Harness and workflow first:** `.agents/**`, `.codex/**`, all `AGENTS.md`, related repository safety hooks, scripts, governance/resume rules and configuration; verify current Codex compatibility including GPT-6.1 Sol where actually available, identify redundancy and failure modes.
2. **Docs, product and architecture second:** **all** tracked `docs/**`, especially `adr/`, `architecture/`, `database/`, `implementation/`, `repository/`, `workflow/`; assess requirements, competing designs, KISS/YAGNI trade-offs, source truth, status and provenance.
3. **Non-RAG Backend third:** all in-scope Backend source, config, tests, dependencies, migrations and real integration boundaries; trace back to Owner-visible behavior and database/schema.
4. **Cross-layer reconciliation last:** revisit provisional docs conclusions against observed code, test and integration evidence; independently validate final findings/dispositions on actual published SHA.

This order is not a permission to modify an area before its assessment is approved and completed.

## O-03 — Agent staffing and independence

- Use **Codex only for this audit**. No Antigravity implementation, review or remediation session.
- Use **distinct Codex sessions** for read-only inspection, authorized implementation and independent review. No session may independently accept its own implementation. Author self-checks are supplementary, not independent acceptance.
- Owner alone commits/pushes, changes approval boundaries, grants API/schema/frozen-contract exceptions and decides material product behavior. The existing Codex takeover skill **does not** automatically become ordinary implementation authority.
- For the initial harness audit, use a **small independently readable bootstrap contract** (this package) because the existing audit skill itself is being reviewed. Do not invoke the legacy Phase 15 skill to launch the current audit. An H1 rewrite of the existing skill is a future, separately Owner-approved and independently reviewed harness implementation, **not** permission granted by this preparation.

## O-04 — Skills and quality policy; explicit Owner supersession (2026-11-15)

- **Newer binding Owner decision:** The Owner now directs **replacement/rewrite of the existing** `.agents/skills/codex-backend-audit/SKILL.md` for the **current comprehensive Non-RAG audit** covering Harness → Docs/Design → Non-RAG Backend → cross-layer reconciliation. **Do not create another backend-audit skill.** The old skill's Phase 15 preconditions, execution routing, remediation handoff and `BACKEND_AUDIT_READY` behavior **need not be preserved as callable behavior**. An explicit legacy `phase-15` invocation should fail closed with a reference to archival evidence; it must not restart historical audit or accidentally select current mode.
- **This expressly supersedes the conflicting 2026-11-01 Owner intent's requirement** to retain Phase 15 *skill mode behavior* and add a parallel `current-non-rag` mode. That earlier intent remains valid for comprehensive non-RAG coverage, Owner authority, all-actionable findings including LOW/INFO and excluded RAG; **only the keep-Phase-15-skill-mode requirement is replaced**.
- **Historical integrity is unchanged:** preserve, do not rewrite or re-run, original Phase 15 `docs/implementation/phase-15/**`, prior skill versions in Git history, accepted review/evidence, commit SHAs, findings and counters. Historical material is read-only evidence; removing old runnable codepaths from the current skill is **not** retroactively invalidating Phase 15.
- **H1 mandatory integration work (after H0 + exact Owner approval):** modify the existing audit skill **and every demonstrably affected** `codex-create-handoff`, review/implementation/routing skill, `AGENTS.md`, `.codex/`, hooks, scripts, resume prompts and current workflow/documentation. Deliver a consistent Codex-only *audit-specific* path; do not leave stale unconditional Antigravity routes or stale Phase 15 launcher references for this audit. Other approved non-audit workflows must not be silently changed.
- A validated `delivery/` packaging workflow, if one exists locally or in the harness, also belongs in H0 inventory and may require scoped changes to the responsible skills/scripts/docs in H1. The supplied repository snapshot contains **no `delivery/` entry**; its local existence, auto-ZIP behavior and ownership remain `NOT_VERIFIED`. Do not claim a review was accepted merely because a ZIP was created, or assume the folder is Git-tracked.
- Skill/AGENTS brevity is a *testable design goal*, **not** a reason to discard hard-earned failure shields. Remove/merge/rewrite instructions only with verified redundancy/contradiction, explicit scope, regression scenarios and a measured benefit.
- No requirement to adopt external GitHub templates or new agents/frameworks; external repositories are comparative references, not PPV authority. Record model/version/runtime evidence, not assumptions.

## O-05 — Simplest sufficient product behavior

- Single Owner; fixed PUBLIC/PRIVATE profiles. PUBLIC must not expose NSFW, Personal, People and other data expressly PRIVATE-only through UI, API, errors, lists, search, caches, previews or indirect relationships. Use **default-deny and explicit allowlist**, not a generic enterprise permission platform.
- PRIVATE retains approved access. Avoid unrelated security theatre; preserve credential secrecy, correct privilege enforcement and data integrity even if elaborate privacy controls are not wanted.
- For UI-facing list/search/preview responses, consider directly returning a safe `name/title/displayName` when it avoids unnecessary detail calls **and** can be supplied cheaply and correctly. Never mandate this for every DTO. `GET /api/v1/vault/trash` currently has an accepted *exact* `{id,entryType,deletedAt}` item contract; its extension needs distinct authorization and technical justification.
- Compare KEEP/REUSE/SIMPLIFY/REPLACE/REMOVE/DEFER with concrete conditions, regression risks and implementation cost. “More sophisticated” is not sufficient justification. Nor is “shorter code” sufficient justification to remove a real transaction/auth/module boundary.

## O-06 — Existing authorities preserved

- `ACTIVE = NO_ACTIVE_HANDOFF`, V1 published/CLOSED; older accepted DP-A, B1, W1, RO-A, RO-B remain historical, including their backlogs/counters. Only eight existing PUBLIC business GET method/path pairs; PUBLIC Film POST denied. V1 trash is PRIVATE-only and identity-only.
- H1-F paused, FR-13 open; P17 frozen, P18 closed, S19-2 suspended/not accepted/archival-only, other RAG work not reopened; providers OFF/unset absent separate grant.
- No live Google/Drive/S3 claims, real Owner dataset suitability, production cutover or full-suite assertion without fresh proof.
- Use the 2026-11-01 final audit intent's **all-actionable-findings** rule across CRITICAL/HIGH/MEDIUM/LOW/INFO. Distinguish real defects from taste/optional improvements, duplicates and false positives.

## O-07 — Exact preparation write boundary

**Allowed by this request:** author and correct the **draft Markdown preparation documents already applied by the Owner** under `docs/implementation/maintenance/2026-11-15-bf26-nr-audit-preparation/`, supplying a changed-files-only correction ZIP. No changes outside that directory, including historical policy/skill files; no writing findings as if an audit has run.

**Not allowed yet:** edit any existing `.agents/`, `.codex/`, `AGENTS.md`, scripts, `docs/` historical artifacts, `ACTIVE.md`, `pom.xml`, API/DTO, Java/Kotlin, tests, migrations, DBML, RAG, or Frontend; run repository audit/tests; create branches/worktrees; stage/commit/push/tag/PR; install/update dependencies; start containers/providers; modify real data.

**Required future gates:** independent *preparation* review → Owner publication of accepted docs → separate Owner authorization of H0 read-only harness audit → independently challenged H0 findings + exact H1 change approval → Codex rewrites the **existing** audit skill and approved related harness/workflow/delivery files → independent H1 reviewer → Owner publication → D/B phases under their own gates. No implicit transition.

## O-08 — Acceptance of this contract

Preparation review must prove that O-01–O-07 are represented **without expanding permission**. A new unresolved product question or need for frozen exception is **not** consent; record `OWNER_DECISION_REQUIRED` at the appropriate future gate. Until independent review and actual publication, this document is only the submitted preparation contract.
