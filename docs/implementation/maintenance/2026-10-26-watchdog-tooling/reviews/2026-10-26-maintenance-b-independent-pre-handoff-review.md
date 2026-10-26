# Maintenance B — independent preparation review

- Date / reviewer: 2026-10-26 / Codex.
- Mode / authority: explicit Owner-approved maintenance preparation, using `codex-pre-handoff-review` and proportional `codex-review-coverage`.
- Baseline: local `main` HEAD **`6fe8dbce24ae9ab43290d76172e2c39678246bbd`**; `git ls-remote origin refs/heads/main` independently returned the same commit. Maintenance A is published. Before this review, tracked files and index were unchanged; only the four Maintenance B preparation files were untracked.
- Reviewed scope: [README](../README.md), [preparation checklist](../preparation-review.md), [B1](../slice-b1-watchdog.md), [B2](../slice-b2-tooling.md).
- Disposition: **READY_FOR_HANDOFF**. No CRITICAL/HIGH/MEDIUM preparation findings or unresolved material Owner decisions. No new LOW/INFO findings.
- Meaning: the preparation is consistent for a **separate B1-only handoff**. No B1/B2 implementation, live handoff, runtime acceptance or Phase 19 authorization follows automatically.

## Mandatory checks

| Requirement | Preparation result / evidence |
| --- | --- |
| 1. Minimal, independent, separately committable slices | PASS — README two-slice table and controlled sequence; B1 allowed changes target the existing wrapper/tests and necessary instructions, excluding B2 guards. B1 is usable without B2. B2 consumes the published B1 output contract and modifies existing client controls; that explicit sequential dependency does not merge the commits. |
| 2. 300-second ordinary cap; approved complex tests at most 2100 seconds | PASS — README acceptance 1–2 and B1-01/02/03. Longer eligibility requires approved command/shape/rationale, not an unrestricted timeout flag. Finite diagnostic/cleanup grace is reported separately, not treated as an unlimited extension of the execution budget. |
| 3. Five minutes of silence means diagnosis, not automatic hang | PASS — README acceptance 3, B1-04/05/06 and minimal implementation preference distinguish output, attributable progress and inconclusive evidence. Quiet healthy extended tests continue within their hard cap. `HUNG_CONFIRMED` requires objective evidence; silence, sleep or one unchanged sample is insufficient. |
| 4. Owned process-tree cleanup and unrelated-process safety | PASS — B1-02/08/09 require finite shutdown, child/grandchild checks, surviving unrelated sentinel and truthful OS limitations. Existing native-tree testing provides a reusable fixture; no broad Java/Docker process-name kill is authorized. |
| 5. Existing logs preserved | PASS — README current truth/acceptance and B1-10/11 require unique new paths, collision checks, full saved output and bounded memory. B2-08/09 reuse that contract. Existing paths/bytes may not be overwritten, truncated, moved or removed. |
| 6. Real Windows-client enforcement tests | PASS — B2-01/02/05 and test plan require actual trusted-client rule loading and Antigravity hook invocation. Explicit policy checks and unit tests are insufficient. B2-03/06/10 require honest gaps or Owner disposition where supported scoped enforcement is unavailable. |
| 7. Antigravity B1, self-review, independent Codex review, Owner publication before B2 | PASS — README controlled steps 2–4, B1 roles and B2 dependency. B2 requires actual B1 publication and its own executable handoff, not only a green implementation report. One maintenance final review follows both published slices. |
| 8. No unconditional suites, new infrastructure or frozen-phase/Phase 19 changes | PASS — both slice exclusions and focused verification plans preserve P17/18, inactive indexing and the Phase 19 gate. Scaled synthetic timers/process fixtures replace long waits in unit tests. No application suite is demanded merely because tooling changes. |
| 9. Severity and backlog | PASS — README acceptance 6: open CRITICAL/HIGH/MEDIUM or mandatory unverified acceptance block implementation delivery; LOW/INFO consolidate at milestone/audit. Mandatory self-review and three corrective/re-review rounds per active slice remain. |

## Actual source cross-check

| Existing source | Observed fact / effect on preparation |
| --- | --- |
| `scripts/run-bounded-command.py:17–28, 66–76, 90–103` | Current caps are 1200/2100 seconds; extended eligibility is full-suite-shaped; execution waits with `proc.wait`. There is no progress monitor. B1 accurately treats the new ordinary cap, guarded complex eligibility and diagnostic behavior as unimplemented. |
| `scripts/run-bounded-command.py:33–41` | Receipts currently use a temporary file followed by `os.replace`, which can replace an existing receipt. B1's explicit no-overwrite/collision acceptance is necessary implementation scope, not existing behavior being credited as PASS. |
| `scripts/run-bounded-command.py:44–62, 83–98` and `scripts/test_run_bounded_command.py` | Existing owned cleanup is best effort: native Windows `taskkill /T /F`, POSIX process group, parent fallback and finite waits. Five existing tests include parent/child/grandchild timeout and unrelated sentinel. B1 retains those invariants and adds interruption/output/health/collision coverage. No universal cleanup guarantee is inferred. |
| `.codex/config.toml`, `.codex/rules/owner-git.rules`, `.codex/README.md` | Current verbosity/full-access settings and limited direct-token deny rules match the preparation. Project trust/autoload, wrapped invocations and actual UI behavior remain runtime evidence obligations. B2 does not presume that low model verbosity suppresses tool events or that rules guarantee interception. |
| `.agents/hooks.json`, `.agents/hooks/repository_safety.py`, `.agents/hooks/test_repository_safety.py` | Hook invocation uses `python hooks/repository_safety.py`, so actual working-directory/interpreter resolution needs B2 evidence. The current deny set is commit/push/tag, not `git add`; destructive operations generally return `force_ask`. Thirteen parser/protocol tests cannot prove invocation by the actual Antigravity client. B2 explicitly includes direct staging denial, real hook protocol tests and safe isolated probes. |
| Existing agents, implementation/self-review/test-slice skills and shared rules | Existing `backend-implementer`, optional read-only `architecture-auditor`, mandatory self-review, bounded-command skill and Codex review path are sufficient. Current instructions still correctly describe old runner limits until B1 is verified. B1's allowed minimal call-site synchronization must update current executable instructions while B2 enforcement remains pending; frozen receipts stay unchanged. |
| Owner policy and handoff-creation skill | Preparation scope, severity, three-round rule, publication ownership and review cadence agree with the current policy. Both shared skills emit/consume `READY_FOR_HANDOFF`; explicit Owner scope approval remains separately required. No additional staged-tree or approval layer is inserted. |

## Proportionality and implementation burden

| Mechanism | Decision / smallest adequate path and verification burden |
| --- | --- |
| Existing Python wrapper, CLI and receipts | REUSE — change the existing implementation and its focused tests. Preserve argv-form invocation and supported options where possible; no competing wrapper. |
| Timeout/profile validation | KEEP / SIMPLIFY — one monotonic hard deadline and explicitly guarded extended eligibility. The B1 handoff specifies the exact finite timing/cleanup behavior and approved command shapes. No new authorization service is needed. |
| Silence diagnostics and confirmed-hang classification | SIMPLIFY — bounded, attributable output/progress/health observations; unsupported or ambiguous evidence remains inconclusive. Do not turn this into a generic deadlock debugger or infer hang from absent stdout/low CPU. Only evidence-supported verdicts are mandatory. |
| Owned cleanup and log preservation | KEEP — reuse the owned process-tree/sentinel fixtures, add meaningful interrupt/collision/output cases and a narrow real Windows integration. Unique new evidence paths, not historical log migration or rotation. |
| Codex rules and Antigravity hook | REUSE — existing native controls and protocol, representative safe isolated client tests, scoped correction only where justified. B2 reuses B1 capture; no second capture pipeline. |
| New packages/helpers/agents | DEFER — existing stdlib/native process APIs and agents are the default. An unavoidable narrow platform gap needs a demonstrated reason before handoff expansion, as the approved scope already requires. |
| General UI suppression, monitoring service, new RAG features | REMOVE from implementation scope — client-rendered UI limitations are reported, not solved through a replacement client/daemon. Phase 19 and frozen P17/18 are outside both slices. |
| Application-wide suites and benchmark apparatus | DEFER — no unconditional Java/RAG gate. Focused synthetic/process/client evidence covers these changed tooling boundaries. Reuse valid prior receipts rather than increasing test counts. |

No additional material Owner choice is needed for preparation. Detailed algorithm constants, exact commands and the safe probe matrix belong in the separately created executable slice handoff; this report does not create that handoff or promise future Windows results.

## Verification performed and limits

- Inspected all four preparation files recursively; checked **11 relative Markdown link occurrences**, **0 missing targets**, and **0 trailing-whitespace lines** before status synchronization.
- Inspected the current wrapper, focused tests, Codex config/rules, Antigravity hook/protocol and relevant existing agents/skills/rules named above. Source—not the graph or historical acceptance—supports the implementation observations.
- `git rev-parse HEAD`, branch/log inspection and `git ls-remote origin refs/heads/main` confirmed the supplied baseline and `main` branch. `git diff --quiet HEAD` and `git diff --cached --quiet` both returned **0** before review writes.
- A source comparison between the earlier independently inspected `217cb5d24bf7a62d7348d31838a7a79b01a524bd` and current baseline returned **0** for changes to the runner/tests, hooks/tests and Codex config. Historical 5/5 runner and 13/13 hook evidence remains baseline context only, not acceptance of new behavior.
- One budget-limited Graphify query was navigation only; it included stale/unrelated nodes and was not treated as current source authority. No graph regeneration.
- **No tests, application suites, worker jobs, publishing probes or actual client interception experiments were run.** All B1/B2 runtime acceptance, including native Windows semantics, remains **NOT_VERIFIED / REQUIRED DURING IMPLEMENTATION**. This is not a preparation blocker because the obligations and honest stop/Owner-disposition paths are explicitly specified.
- No existing `logs/` content was written, scanned for cleanup, moved, overwritten or deleted. No current log byte-equality claim is made.

Input identities captured before review-status synchronization:

| Input file | Raw SHA-256 |
| --- | --- |
| `README.md` | `cb23b2c23c720d3d087bbdf27fb28f20a47fcd126f36b168c3db113eb3c2505e` |
| `preparation-review.md` | `2615f57e2aa10eb6699aa92ddc4c829c52e6b4e6e4fe8243e721c1b0f514885b` |
| `slice-b1-watchdog.md` | `e27c9b5f86260e3ccdae6b2eeab4cd650c550c7e0e4fc3a41cd015f0ca97b3b3` |
| `slice-b2-tooling.md` | `3990fb892e9eb7852149f242a0b981695f498eee2a5e14d024d28be5e247ef0b` |

## Shared coverage

| Dimensions | Classification / rationale |
| --- | --- |
| 1 intent; 2 traceability; 3 architecture; 5 folders; 8 KISS/YAGNI; 14 retention/privacy; 16 failure/operations; 18 resource bounds; 22 test plan; 25 dependencies; 26 tooling; 27 logging; 28 governance; 29 delivery | PASS for preparation — finite existing-tool changes, documented invariants, proportional tests and separate slice ownership/publication. No new dependency or frozen-package changes are approved. |
| 21 actual client/security integration; 23 measured performance/output-resource behavior | NOT_VERIFIED/runtime — mandatory scoped B1/B2 evidence, not a prose or unit-test PASS. |
| 4 application module ownership; 6 HTTP/exception conventions including ADR-0016; 7 code SOLID; 9 code patterns; 10 business logic; 11 APIs; 12 application input validation; 13 application auth/consent; 15 database transactions; 17 persistence; 19 retrieval quality; 20 parsing/OCR; 24 model evaluation | N/A — no changed application code, database, API, model or ingestion capability. Process argv/log bounds and publishing safety are addressed in the applicable tooling dimensions. |

The Maintenance A LOW findings **MA-A03–MA-A06 remain recorded in the existing milestone/audit backlog**. They are not closed, promoted or imported as B1/B2 blockers. Historical P17/18 decisions and failed/missing evidence are preserved.

## Review writes and next action

Review writes are limited to this report, the maintenance README's readiness, preparation-checklist outcomes and B1's preparation-status line. B1/B2 requirements, executable files, configuration, hooks, tests, dependencies, ACTIVE and historical evidence were not changed. This is preparation readiness only; B2 remains blocked until reviewed and Owner-published B1.

**Conventional Commit:** `docs(maintenance): prepare watchdog and tooling slices`

**Next step:** Owner commits/pushes the accepted preparation, then invokes `$codex-create-handoff` for **B1 only**. Antigravity implements and self-reviews B1; independent Codex slice review and actual Owner publication must precede B2. No separate staged-tree review, immediate implementation or Phase 19 activation is authorized by this preparation review.
