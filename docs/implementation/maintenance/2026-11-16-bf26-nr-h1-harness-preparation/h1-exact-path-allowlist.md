# H1 proposed exact allowlist and exclusions

**Status:** `PROPOSED / NOT_APPROVED_FOR_EDITS`. Paths were checked against the published H0 HEAD `7c521fb9998bc1ad0a86e7225aa605f0de17e261`; all **17 proposed mandatory existing files** exist there. Recheck them at the future Owner-published H0 assessment SHA before implementation. Do not treat this table as an executable grant.

## A. Proposed **17 existing-file** write allowlist

| No. | Exact file | Finding | Smallest allowed reason |
|---:|---|---|---|
| 01 | `.agents/skills/codex-backend-audit/SKILL.md` | F01 | Rewrite current-only audit with H/D/B/X, exact grant, fail-closed legacy. |
| 02 | `.agents/skills/codex-backend-audit/assets/backend-audit-report-template.md` | F01 | Replace Phase 15/BA15-only template fields with scoped current audit ledger. |
| 03 | `docs/workflow/resume-prompts/codex-backend-audit.md` | F01 | Remove executable legacy recovery; preserve evidence and no implicit grant. |
| 04 | `.agents/skills/codex-create-handoff/SKILL.md` | F01 | Remove legacy Phase 15 launcher redirect; preserve empty-ACTIVE scope discovery. |
| 05 | `.agents/skills/codex-final-review/SKILL.md` | F01 | Remove legacy closure rerun; add scope-qualified independent audit review. |
| 06 | `.agents/skills/codex-pre-handoff-review/SKILL.md` | F01 | Archival reference clarification only; current no-reopen guard stays. |
| 07 | `.agents/README.md` | F01/F03 | Current audit discovery and superseding current navigation. |
| 08 | `.agents/rules/agent-handoff-workflow.md` | F01/F03 | Audit-specific Codex route; preserve normal Antigravity route. |
| 09 | `.codex/README.md` | F01 | Document explicit audit exception and safe no-automatic-assurance limits. |
| 10 | `AGENTS.md` | F01 | Concise canonical audit pointer/precedence; no wholesale root rewrite. |
| 11 | `backend/AGENTS.md` | F03 | Add current pointer; explicitly retain preserved history without rewriting it. |
| 12 | `.agents/agents/backend-implementer/agent.md` | F03 | Correct B1/B2 operational status sentence only. |
| 13 | `docs/workflow/owner-phase-workflow.md` | F01/F03 | Current audit route and current status pointer; preserve history. |
| 14 | `docs/workflow/current-owner-delivery-policy.md` | F01 | Narrow Codex-only audit authorship and all-actionable-findings exception. |
| 15 | `docs/workflow/resume-prompts/README.md` | F01 | Current audit recovery entry and legacy refusal. |
| 16 | `.agents/hooks/repository_safety.py` | F02 | Semantic branch-deletion deny across supported options/wrappers/overrides. |
| 17 | `.agents/hooks/test_repository_safety.py` | F02 | Positive/negative regression cases including benign branch/list/status. |

No additional existing source file is authorized by these 17 candidates. All changes must be minimally tied to one of F01/F02/F03, and exact file diffs are individually reviewed. The independent H0 reviewer treated pre-handoff skill clarification as archival and recommended keeping any backend/AGENTS historical lines intact.

## B. Conditional candidates — **excluded from initial 17-path write authority**

These may be inspected read-only. They require an amended, exact Owner grant and independent re-review if a reachable caller actually requires their edit; no implicit expansion.

| Candidate | Threshold for separate permission |
|---|---|
| `docs/workflow/agent-development-workflow.md` | If shared role table is invoked as authoritative for audit remediation |
| `docs/workflow/documentation-synchronization.md` | If audit authorship cannot be reconciled under current scoped contract |
| `docs/workflow/resume-prompts/COMMON.md` | If resume graph imports a conflicting unconditional Antigravity restriction |
| `docs/workflow/resume-prompts/codex-create-handoff.md` | If selected audit remediation actually uses create-handoff skill |
| `docs/workflow/resume-prompts/codex-final-review.md` | If final-review recovery is selected for H1 reviews |
| `.agents/skills/codex-create-handoff/assets/handoff-template.md` | If audit uses generic handoff asset rather than its own contract |
| `docs/templates/handoff.md` | If referenced by selected audit handoff path |
| `docs/templates/review.md` | If selected for H1 review report output |
| `docs/templates/README.md` | If generic templates are changed and index would drift |
| `docs/AGENTS.md` | If current docs instruction prevents exact audit output ownership |
| `.agents/rules/documentation.md` | If role conflicts with the H1 permission grant |
| `.agents/skills/documentation-writing/SKILL.md` | If this skill is actually used to package or generate audit artifacts |

## C. Deferred or excluded

| Path | Reason to leave untouched now |
|---|---|
| `docs/templates/audit.md` | Use existing audit skill-local asset, avoid duplicate template. |
| `backend/src/test/AGENTS.md` | Future B1 Java test workflow, not H1 hook tests. |
| `.agents/rules/tests.md` | Future B1 test rule, not H1 code. |
| `.codex/config.toml` | No approved sandbox/trust changes or runtime proof. |
| `.codex/rules/owner-git.rules` | Semantic evaluator correction proposed; retain current rules absent new proof. |
| `delivery/` | Observed empty directory; no automatic ZIP or producer authorized. |

Other exclusions: **all** production/test Java and non-hook tests, Maven/POM/CI/dependencies, RAG/Frontend, DB/Flyway/DBML, ADRs, accepted historical Phase 15/review documents, H0 producer reports and raw receipts, any `.git` state, global configuration/trust, personal/Owner datasets, and all files not expressly named in the future final ratification.

## D. New documentation outputs versus existing-file edits

If the Owner later ratifies H1, permit creation **only** of the following fresh H1 implementation records in an H1 execution directory named exactly `docs/implementation/maintenance/2026-11-16-bf26-nr-h1-harness-preparation/`:

- `h1-implementation-handback.md`
- `h1-self-review.md`
- `h1-source-test-evidence-index.md`

These are **proposed new paths, not pre-created in this preparation ZIP and not yet authorized**. If one exists, STOP/no-clobber and obtain scoped revision approval. An independent H1 reviewer may write one new `reviews/<date>-h1-independent-review.md` only with a separate explicit review-write permission; otherwise review remains in chat. Runtime receipts are allowed only in an approved unique ignored directory after git check-ignore; no blanket ZIP or logs permissions.

## E. Escalation / proof of necessity

If a changed skill references a consumer outside this list, first determine whether the selected design actually invokes it and whether the existing contract-specific Owner exception already resolves the conflict. **Do not knowingly leave an actionable contradictory current route** merely to meet the 17-file cap. Report missing dependency + minimal additional path + its specific test, STOP for Owner expansion/re-review, then continue only if authorized. Conversely, no editing optional paths solely to achieve uniform wording or fewer lines.
