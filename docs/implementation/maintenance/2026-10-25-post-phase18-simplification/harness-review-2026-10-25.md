# Harness current-tree inspection and coverage — 2026-10-25

**Source:** latest Owner ZIP `personal-private-vault(20261025-120122).zip` (full `.agents`, `.codex`, `docs`, `scripts`, root `AGENTS.md`). Selected backend/RAG sources separately checked on GitHub main; ignored `logs/` intentionally unchanged and not retrievable on main. **This is a governance/text-file inventory and direct test review, not a claim of Antigravity/Codex Windows integration passing.**

- **Skills:** 38 `SKILL.md` entrypoints plus assets; reviewed names, role, rule propagation, stale two-round/time-cap/R7/OCR/GPU references. Only active shared entrypoints modified; phase-specific historical modeling skills retained.
- **Rules:** 26; 11 current shared rules receive forward policy, 15 older numbered-phase/milestone rules retained as historical domain guards.
- **Agents:** 2; both re-anchored to current policy. Optional architecture-auditor is not automatically invoked for every slice.
- **Hooks:** `.agents/hooks.json` + `.agents/hooks/repository_safety.py` and existing test file; runtime code **untouched**. Existing wrapper behavior needs actual Windows client test; code-path tests alone cannot guarantee interception. Existing `git commit/push/tag` denial is not enough to prove all staging, shell-wrapped publication, destructive Git or connector actions are blocked.
- **Codex:** `.codex/config.toml` retains `model_verbosity=low`, `danger-full-access`, `approval_policy=never`; source code and CLI may still display tools stdout/diffs. New `.codex/rules/owner-git.rules` is **preview prefix-filter defense**, not proof of enforcement; check with `codex execpolicy check` on the Owner machine and test direct/wrapped/`git -C` negative cases. Do not claim permission isolation until observed.
- **Watchdog:** `scripts/run-bounded-command.py` + `scripts/test_run_bounded_command.py` unchanged in Maintenance A; executable old caps normal 1200s / full-suite 2100s, no progress detection. Maintenance B must implement/validate approved 300s default + optional 2100s complex + 300s inactivity diagnosis; avoid false positives from silent Maven/Surefire/native work.
- **No Phase 19 authorization, no build/runtime model changes, no UI/data deletion.**

## Skill inventory

- `.agents/skills/account-domain-modeling/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/antigravity-implement-handoff/SKILL.md` — shared/active
- `.agents/skills/antigravity-self-review/SKILL.md` — shared/active
- `.agents/skills/antigravity-test-slice/SKILL.md` — shared/active
- `.agents/skills/architecture-change/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/authentication-security/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/backend-integration-portability-storage/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/backend-testing/SKILL.md` — shared/active
- `.agents/skills/bounded-command-execution/SKILL.md` — shared/active
- `.agents/skills/codex-backend-audit/SKILL.md` — shared/active
- `.agents/skills/codex-create-handoff/SKILL.md` — shared/active
- `.agents/skills/codex-final-review/SKILL.md` — shared/active
- `.agents/skills/codex-implementation-takeover/SKILL.md` — shared/active
- `.agents/skills/codex-milestone-review/SKILL.md` — shared/active
- `.agents/skills/codex-pre-handoff-review/SKILL.md` — shared/active
- `.agents/skills/codex-review-coverage/SKILL.md` — shared/active
- `.agents/skills/collection-domain-modeling/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/database-migration-review/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/design-pattern-selection/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/documentation-writing/SKILL.md` — shared/active
- `.agents/skills/feed-import-workflow-modeling/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/fiction-domain-modeling/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/film-domain-modeling/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/finance-journal-personal-domain-modeling/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/global-search-domain-modeling/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/graphify-context/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/java-spring-coding-standards/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/jpa-postgresql-persistence/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/knowledge-domain-modeling/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/local-rag-engineering/SKILL.md` — shared/active
- `.agents/skills/location-domain-modeling/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/media-domain-modeling/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/modular-monolith-architecture/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/people-domain-modeling/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/pragmatic-solid-design/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/python-rag-coding/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/rest-api-http-contracts/SKILL.md` — domain/conditional (unchanged)
- `.agents/skills/reuse-and-consistency/SKILL.md` — domain/conditional (unchanged)

## Rule inventory

- `.agents/rules/agent-handoff-workflow.md` — shared/active
- `.agents/rules/architecture-change.md` — shared/active
- `.agents/rules/backend-java.md` — shared/active
- `.agents/rules/backend-python.md` — shared/active
- `.agents/rules/code-reuse-and-patterns.md` — shared/active
- `.agents/rules/context-efficiency.md` — shared/active
- `.agents/rules/database-migrations.md` — shared/active
- `.agents/rules/documentation.md` — shared/active
- `.agents/rules/package-organization.md` — shared/active
- `.agents/rules/security.md` — shared/active
- `.agents/rules/tests.md` — shared/active
- `.agents/rules/backend-phase-1-foundation.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-10-feed-importdata.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-11-finance-journal-personal.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-12-global-search.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-13-rest-api.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-14-integration-closure.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-2-auth-settings.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-3-people.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-4-fiction.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-5-film.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-6-media-location.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-7-account.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-8-knowledge.md` — legacy phase-specific (retained)
- `.agents/rules/backend-phase-9-collection.md` — legacy phase-specific (retained)
- `.agents/rules/maintenance-milestone-10-12-search-case-normalization.md` — legacy phase-specific (retained)

## Agents and controls

- `.agents/agents/architecture-auditor/agent.md`
- `.agents/agents/backend-implementer/agent.md`
- `.agents/hooks.json`
- `.agents/hooks/repository_safety.py`
- `.agents/hooks/test_repository_safety.py`
- `.codex/config.toml`
- `scripts/run-bounded-command.py`
- `scripts/test_run_bounded_command.py`
- `.codex/rules/owner-git.rules`

## Scope and unresolved technical checks

No remaining Owner functional decision for Maintenance A. The following **technical verification** stays open for separately authorized Maintenance B, not a reason to invent new product features:

1. Actual Windows Antigravity PreToolUse hook invocation and denial matrix, including `git add`, repository-destructive and wrapped commands.
2. Actual Codex execpolicy loading under trusted project config; forbidden direct `git`/`gh`; `git -C`, shell quoting, PowerShell, Python subprocess and connector bypass risks. If prefix rules cannot enforce the policy, block publishing at a stronger hook/invocation boundary or keep full access disabled rather than falsely claim safety.
3. Bounded watchdog correctness for one quiet-but-healthy test, a genuine hang, a failing subprocess, controlled multi-child cancellation, resource diagnosis with false-positive limits, Windows cleanup, host tool timeout and concise console output while retaining raw logs.
4. Actual selected installed Codex version's verbosity/tool-rendering behavior; do not claim `.codex/config.toml` alone hides added-file diffs.
5. Full code simplification audit of Backend/RAG after explicit narrow maintenance scope; don't rewrite already-accepted RAG for aesthetic reasons.
