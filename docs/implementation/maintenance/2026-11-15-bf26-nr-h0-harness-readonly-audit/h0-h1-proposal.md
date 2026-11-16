# BF26-NR-H0 proposed H1 scope

**Proposal only — H1_NOT_AUTHORIZED.**  
H0 baseline: `7c521fb9998bc1ad0a86e7225aa605f0de17e261`.  
Status: H0_REPORT_SUBMITTED / INDEPENDENT_REVIEW_PENDING.

## Desired state and smallest behavior change

After independent H0 review and an exact Owner grant, a **different Codex author session** rewrites the existing `.agents/skills/codex-backend-audit/SKILL.md`. Keep its name/trigger. Support only explicitly authorized **current-non-rag**, with H/D/B/X phase, session role, baseline and exact output permissions. Explicit legacy `phase-15` input must STOP and link immutable historical evidence. The skill must not resume old Phase 15 tooling, mutate its status/ACTIVE, issue Antigravity audit remediation, or treat a report as the next phase's authorization.

Preserve ordinary non-audit Antigravity workflows, frozen backend/business/API/schema contracts, Phase 15 historical evidence and all counters. Reuse the existing hook/evaluator, watchdog, evidence and proportional review helper. No new audit skill, agent, packaging framework, library, provider or backend code is justified by H0.

Fix the demonstrated branch-deletion protection gap in the existing evaluator/test file. Correct only implicated operational status/routing clauses. Large-scale domain-skill or AGENTS deletion is not justified.

## Candidate change graph, not an executable allowlist

Every path below is an **existing file**. “Direct” identifies an observed dependency; “conditional” identifies an exact candidate only if the chosen H1 design uses that shared consumer. Owner must decide these conditions and freeze the actual finite list before authoring. A changed consumer requires its linked resume/template to remain consistent. This table grants no write.

| Candidate path | Evidence / smallest proposed change | Necessity |
|---|---|---|
| .agents/skills/codex-backend-audit/SKILL.md | F01: replace runnable Phase 15 mode; exact current phase/role/authority/output gates and legacy STOP | Mandatory Owner-directed H1 core |
| .agents/skills/codex-backend-audit/assets/backend-audit-report-template.md | F01: Phase 15/BA15/outcomes no longer match sole active mode; use current audit ledger and role/status fields | Direct asset dependency |
| docs/workflow/resume-prompts/codex-backend-audit.md | F01: replace old Phase 15 resume route; retain saved-state/counter recovery and no implicit grant | Direct resume dependency |
| .agents/skills/codex-create-handoff/SKILL.md | F01: old Phase 15 launcher redirect must fail closed; add exact audit exception if it routes approved audit batches; preserve empty-ACTIVE discovery | Direct old-launcher caller |
| .agents/skills/codex-final-review/SKILL.md | F01: remove old closure-rerun instruction as executable behavior; audit role/output/next step must follow exact approved scope | Direct old-launcher caller |
| .agents/skills/codex-pre-handoff-review/SKILL.md | F01: historical evidence remains, but no direction to a now-replaced Phase 15 launcher; qualify audit maintenance route | Direct historical launcher guidance |
| .agents/README.md | F01/F03: current audit entry plus current policy/ACTIVE pointers; stop advertising obsolete execution/publication checkpoints | Direct discovery surface |
| .agents/rules/agent-handoff-workflow.md | F01/F03: label/remove runnable Phase 15 steps from current dispatch; conditional Codex-only audit route; preserve ordinary loop | Direct instruction route |
| .codex/README.md | F01: current audit exception and version-qualified safety statements; do not globally replace ordinary implementation routing | Direct client entry |
| AGENTS.md | F01/F03: a concise current audit pointer and precedence; ordinary workflows/frozen protections retained | Direct root discovery; compression optional below |
| backend/AGENTS.md | F03: stale active Phase 18 state replaced by current policy/ACTIVE link; no domain/invariant rewrite | Direct scoped stale state |
| .agents/agents/backend-implementer/agent.md | F03: correct B1-awaiting/B2-pending sentence to actual published, client-qualified state | Direct single-line status defect |
| docs/workflow/owner-phase-workflow.md | F01/F03: conditional audit exception; replace current S19-1/publication-pending assertions with current pointer; history retained | Direct current route/status |
| docs/workflow/current-owner-delivery-policy.md | F01: narrow Owner-ratified audit exception and all-actionable finding disposition; ordinary LOW/backlog/counters unchanged | Conditional shared authority surface; Owner decides exact amendment |
| docs/workflow/agent-development-workflow.md | F01: current audit Codex author/reviewer branch; no permanent Codex-only policy | Conditional shared lifecycle consumer |
| docs/workflow/documentation-synchronization.md | F01: named Codex audit-document ownership where explicitly granted, saved-state evidence unchanged | Conditional shared ownership consumer |
| docs/workflow/resume-prompts/COMMON.md | F01: scope audit-specific Codex authorship while retaining ordinary Antigravity route and independence | Conditional if current audit recovery uses COMMON |
| docs/workflow/resume-prompts/README.md | F01: audit prompt is current-only, legacy refusal; no Phase 15 live resume link | Direct resume index |
| docs/workflow/resume-prompts/codex-create-handoff.md | F01: mirror conditional audit/ordinary routing if create-handoff is changed | Conditional paired consumer |
| docs/workflow/resume-prompts/codex-final-review.md | F01: mirror scope/role-based audit route if final-review is changed | Conditional paired consumer |
| .agents/skills/codex-create-handoff/assets/handoff-template.md | F01: hardcoded Antigravity implementer/result ownership conflicts with audit batches if this asset is used | Conditional shared template |
| docs/templates/handoff.md | F01: conditional implementer/self-review fields, never self-independent acceptance | Conditional shared template |
| docs/templates/audit.md | F01: add current Non-RAG audit phase/role options if reused; preserve separately authorized RAG/combined modes | Conditional generic template; alternative is the existing skill asset |
| docs/templates/review.md | F01: explicit audit review role/mode if reused; current contract governs LOW/INFO disposition | Conditional generic review consumer |
| docs/templates/README.md | F01: label current Non-RAG audit template route if generic templates are amended | Conditional paired index |
| docs/AGENTS.md | F01: scope-specific maintenance audit review/output ownership if its generic phase/Antigravity assumptions are relied on | Conditional docs instruction consumer |
| backend/src/test/AGENTS.md | F01: exact audit Codex author exception if later B1 uses shared test workflow; no test behavior change | Conditional future B1 consumer, not needed for hook-only H1 tests |
| .agents/rules/documentation.md | F01: maintenance/audit output routing if selected generic docs rules require it | Conditional thin selector |
| .agents/rules/tests.md | F01: audit exception if later Java test ownership is routed through this rule | Conditional future B1 consumer |
| .agents/skills/documentation-writing/SKILL.md | F01: exact audit author exception; automatic/manual packaging must be subordinate to active output grant | Conditional authoring/packaging consumer |
| .agents/hooks/repository_safety.py | F02: reject branch-deletion semantics through supported options/wrappers; never grant that mutation via overrides | Direct defect repair; security approval required |
| .agents/hooks/test_repository_safety.py | F02: branch-deletion denial matrix + benign branch/status regressions; adapter has no allow override | Direct regression dependency |

**Minimality rule:** settle template choice and dispatch ownership first. Do not edit both generic and skill-local templates merely to duplicate instructions. If all audit batches use the existing audit asset and exact scoped contracts, generic template edits may be omitted only after proving no reachable caller emits stale routing. The direct old-launcher callers must be reconciled; a skill-only patch leaves demonstrable contradictory guidance. This is a proposed dependency graph, not a demand to change all 32 candidates.

The H0 producer does not create an H1 folder, executable handoff, ACTIVE row or correction budget. The later Owner grant must name H1's own author handback, permitted tests/receipts, reviewer outputs and publication baseline. Generic skill permissions cannot supply them.

## KEEP / SIMPLIFY / MERGE / DEPRECATE / REMOVE decisions

The [270-row manifest](h0-file-manifest.csv) records each of the **43 skills and 29 rules individually**, its observed callers/references, reason, benefit, compatibility risk and suggested regression.

| Set | Decision | Rationale / risk |
|---|---|---|
| Current audit skill and demonstrated workflow callers | SIMPLIFY / rewrite existing skill | Required replacement, not another skill; conditional ordinary workflow remains |
| Domain skills, Java/JPA/architecture/security/reuse/pattern/testing guards | KEEP | Different ownership/invariant purposes; similar wording is not proof of redundant business behavior |
| codex-review-coverage | KEEP | Already a proportional helper; current parent contract controls scope. No proof a second coverage engine or edit is needed |
| codex-implementation-takeover | KEEP | Exceptional takeover is not ordinary audit authorship; no counter reset/repurposing |
| codex-current-system-readonly-audit | KEEP as historical-contract-bound | One-off 2026-11-01 grant and exact baseline, not a fallback current launcher; optional retirement from discovery can be separately considered |
| Three Antigravity skills and frontend role/skills/rules | KEEP for non-audit tasks | No observed need to rewrite them. Stop audit misdispatch at current audit authorization/router, retain H-T18 |
| bounded-command-execution and scripts/run-bounded-command.py | KEEP | Fresh focused evidence supports intended boundaries; no runner rewrite or new test framework justified |
| Existing execpolicy file | KEEP by default | Direct rules work in parser. Fix F02 in semantic evaluator; add prefixes only if a specific additional test justifies them |
| .codex/config.toml | KEEP pending Owner security choice | Full-access/never is an observed capability and standing preference, not an authority grant; do not silently lower/raise or repair trust |
| Phase-specific domain rules and scoped module AGENTS | KEEP | Retain frozen guards; do not globally reopen phases or erase history |
| Repeated resume paragraphs | Optional MERGE into existing COMMON only after routing tests | Potential maintenance reduction; not an observed runtime failure. Preserve named per-skill next action and interruption counters |
| Root AGENTS dated banner stack | Optional SIMPLIFY | 43,645 bytes is measured; actual context truncation is not. Preserve current Owner scope, Git, privacy, frozen and failure-recovery shields |
| One-off apply-package/apply-workflow scripts and CP18 native diagnostic | DEPRECATE operational reuse; retain source/history | Write/delete/ACL/fixed-output effects make them inappropriate H0 launchers. No deletion approved |
| delivery/ and packaging utilities | KEEP observed empty directory; no new utility | No validated producer/ZIP exists. No auto-ZIP or “review success -> export” feature justified |
| REMOVE | None currently recommended | H0 has not proved that deleting a skill/rule preserves intended task selection and safeguards |

Any actual simplification must show concrete before/after byte counts and routing outcomes. No claimed token, latency, cost or Sol-performance saving follows from H0.

## Selected verification for a future approved H1

1. **Entry:** independently requalify published SHA/dirty/local state, exact Owner H1 contract, finite path/output list and author/reviewer identity. Do not use the H0 grant for source edits.
2. **Current skill:** execute H-T07 legacy refusal and H-T09 exact phase/role/grant/output checks in fresh separately authorized sessions. Negative cases: no grant, conflicting approvals, stale/revoked approval, CHANGES_REQUESTED preparation, wrong baseline, wrong role/output and attempt to self-authorize next phase. H-T08 is never a route into legacy behavior.
3. **Discovery/roles:** H-T01–06 and H-T10/11/16–18. Preserve V1 regression without reopening it. A same-session self-check must never yield independent acceptance; ordinary non-audit task still uses its actual approved workflow.
4. **Hook:** retain E06's 21 method regressions; add F02 direct/-C/-c/absolute/supported-wrapper branch-delete denials and benign branch/status/diff. Probe semantic output and parser separately. Native pre-dispatch proof must use harmless, never-dispatched text/help controls only inside an authorized fixture, no actual publication/deletion.
5. **Runtime:** actual selected model/binary, hook trust fingerprint/loading, root and scoped cwd, tool-name schema, instruction precedence/cap. Source or `execpolicy check` alone cannot close effective enforcement. No H0 permission for model/provider network carries forward.
6. **Runner:** rerun the six relevant methods only if runner/caller integration changed; broader tests need changed-source risk. Short normal profile, unique receipts, exact copied-source hashes and owned cleanup.
7. **Docs/evidence:** all changed instruction/caller/resume links and saved handback state agree. Preserve Phase 15 source/history, existing counters/receipts and all excluded paths. Independent H1 reviewer inspects actual diff, declared vs actual changed paths and qualified fresh/reused results before Owner publication.

Default proposed correction ceiling: at most three **author correction + separate reviewer** pairs for a future audit batch, only if Owner ratifies it. Initial author/self-check is not a corrective round. No current historical allowance is consumed, transferred, reset or extended.

## Owner choices and exclusions

- Choose the exact direct/conditional candidate paths and template/dispatcher design; resolve any accepted H0 reviewer finding first.
- Decide whether optional AGENTS/resume compression belongs in the same bounded H1 or a later housekeeping slice.
- Decide the target client/model and separately permitted live qualification, including any network-dependent model checks. Until then model identity/entitlement and missing-hook behavior remain unverified.
- Decide whether sandbox/approval policy should change; no change is required merely to write the audit skill. Any approved config/hook change requires actual target-host proof.
- Do not authorize ZIP automation merely because an empty delivery directory exists. If later packaging is requested, name producer/trigger/member allowlist, exclusions, no-clobber/hash/manifest and retention tests separately.

Frozen Phase 15 documents/accepted reviews, business source/POM/application tests, DB/ADRs, RAG, frontend, providers, current ACTIVE and Git history remain excluded unless a **future** exact grant explicitly says otherwise. Only the named harness/instruction/test candidates may enter an eventual H1 grant. No H1 source change, D0/B0 launch, independent acceptance or publication has been performed.

Next step: separate Codex H0 independent review -> Owner exact H1 decision -> distinct Codex H1 author -> separate H1 reviewer -> Owner publication. Each arrow remains gated.

