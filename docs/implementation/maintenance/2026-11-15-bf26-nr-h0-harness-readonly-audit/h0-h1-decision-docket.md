# BF26-NR-H0 — Owner's future H1 decision docket (not an approved patch)

**State:** `H0_NOT_YET_EXECUTED / H1_NOT_AUTHORIZED`. The mandatory future architecture is to **rewrite** the existing `.agents/skills/codex-backend-audit/SKILL.md` for current Harness → Docs/Design → Non-RAG Backend → reconciliation. The 2026-11-15 Owner decision supersedes keeping runnable Phase 15 mode; Phase 15 historical documents/records remain read-only. No replacement skill may be created.

## Candidate decisions that require H0 evidence

| ID | Future Owner decision | Required H0 evidence | Default in H0 |
|---|---|---|---|
| HD-01 | Exact existing audit skill rewrite behavior | Old mode callgraph, target H/D/B/X prompts, fail-closed legacy invocation, acceptance tests | PROPOSE only; no edit |
| HD-02 | Which other review/handoff/implementation skills must change? | Dependency graph, current misrouting examples, safe regression | No blanket refactor |
| HD-03 | Root and scoped AGENTS simplification/removal | Effective instruction discovery, superseding banners, token/context proof, safety regression | Keep files unchanged |
| HD-04 | `.codex/config.toml`, hooks and execpolicy policy | Actual CLI/trust/sandbox/hook behavior and safer viable alternatives | Keep unchanged; assess risk |
| HD-05 | Roles, repo scripts, bounded-command runner, resume prompts | Current task failure modes, specific dependencies, tests | No tool/library rewrite without need |
| HD-06 | `delivery/` and automatic ZIP policy | Actual folder existence, producer script/skill, contents, triggering, info leak and retention | No auto packaging claim, no folder creation |
| HD-07 | Separate Codex implementation and independent review route | Role scope and evidence that Antigravity path would misroute this audit | Do not authorize generic takeover |
| HD-08 | Missing version/model/runtime qualification | Current Codex CLI/model not available or test unsafe | `NOT_VERIFIED`; Owner decision to defer/qualify |
| HD-09 | What constitutes safe H1 completion? | H-T07/09, negative authority tests, H01–H13 coverage, actual diff tests | No H1 acceptance before independent review + publication |

## H1 approval artifact to prepare after independent H0

The later Owner-ratified H1 executable must enumerate: observed finding ID(s); desired state; **exact path allowlist** with reason per path (including newly created test/reference files where needed); excluded files; approved frozen/ADR exceptions, if any; simple implementation alternatives; selected tests/scenarios, time/risk bounds; rollback/compatibility approach; separate Codex implementer and reviewer sessions; finite correction budget; current approved published baseline and immutable history guard.

An H0 report may list candidate paths under `h0-h1-proposal.md`, but this **is not an authorization**. If an anticipated skill/rule change is unsupported by evidence, keep it out of the mandatory patch; document it as optional. If mandatory enabling of current audit skill could not be completed under available environment, STOP before D0/B0.

`delivery/` finding disposition must include one of: `OBSERVED_SCRIPTED_AUTOMATIC`, `OBSERVED_MANUAL`, `OBSERVED_EMPTY_DIRECTORY`, `ABSENT_AT_AUDITED_PATH`, `NOT_VERIFIED`, with exact path and observation. Packaging quality, independent review, Owner publication and H1 readiness are four separate states; ZIP creation alone is none of the latter three.
