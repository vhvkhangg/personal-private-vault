# BF26-NR-H0 — Harness coverage and reproducible evidence contract

**State:** future H0 audit checklist only; no row is PASS by creation of this document. Source: [parent H01–H13](../2026-11-15-bf26-nr-audit-preparation/audit-coverage-matrix.md), [harness plan](../2026-11-15-bf26-nr-audit-preparation/harness-audit-plan.md).

## A. Manifest contract

Use a complete file manifest at actual entry HEAD and a separate local-only tier. Minimal fields:

`path, git_blob_sha, provenance(tracked/untracked/ignored/external), role, current_or_historical, read_depth, read_depth_reason, references, scope_decision, evidence_id, finding_ids, reviewer_status`.

Inventoried does **not** mean deeply read; `STATIC_METADATA` is valid for low-risk historical files with stated rationale. All execution-control skills, hooks, policies and scripts with write rights require `FULL` or `RISK_TARGETED` source inspection. Count both tracked/untracked correctly; compare absolute path and case on Windows. Do not copy token-bearing local config, secrets, ignored logs or private data into the manifest. If a field/path is unknown, explicitly say `NOT_VERIFIED`.

## B. Mandatory H0 coverage rows

| ID | What to inspect | Minimum evidence/probe | Handoff acceptance rule |
|---|---|---|---|
| H01 | All `.agents/`, `.codex/`, exact AGENTS, relevant scripts | Recomputed path/blob inventory + separate local/ignored tier | Complete accountable inventory; no fabricated count |
| H02 | Root and nested AGENTS discovery and precedence | Representative cwd startup, actual loaded instruction provenance; quantify duplicates/size | Effective behavior verified where possible; otherwise NOT_VERIFIED |
| H03 | All 43 starting SKILL.md, asset/reference graph | Metadata, trigger collisions, orphaned refs, current usage, KEEP/SIMPLIFY/REMOVE rationale | Each skill classified; avoid automatic deletion |
| H04 | Existing Phase 15-only audit skill | Compare current Owner supersession to old routes; propose H1 rewrite + legacy STOP | No legacy execution; H1 path identified, not edited |
| H05 | `codex-create-handoff` scope discovery | V1 regression + missing/ambiguous/revoked authority variants (H-T01–06) | Evidence for both allowed and denied paths, or explicit gap |
| H06 | Codex-only H0/H1 and independent reviews | Roles/permissions from current policy/skills; H-T10/11/18 | No self-independent acceptance or accidental Antigravity dispatch |
| H07 | Project/global effective Codex config/trust/MCP/model | CLI version, actual effective sandbox/approval/hook loading; redact secrets | Config source and observed enforcement separated |
| H08 | Git/gh rules + PreToolUse safety hook | Fixture-based direct/wrapped/alias/missing-hook positives and negatives | Covered/uncaptured bypass classes explicitly distinguished |
| H09 | Hook/watchdog/command-runner behavior | Source and existing tests; bounded interrupt/timeout fixture when safe | No assumption that file text equals execution proof |
| H10 | Agent rules, roles, docs/workflow and resume prompts | Cross-reference map and stale conflicts; failing/successful task samples | Proposal for minimal required synchronized fixes |
| H11 | GPT-6.1 Sol compatibility/availability | Actual model selected and CLI support, representative safe sessions | Unsupported/unselected models NOT_VERIFIED, not guess |
| H12 | H1 plan and permission boundaries | Dry-run attempts to self-authorize, change protected files, issue grant | Auditing cannot become implementation permission |
| H13 | `delivery/` and ZIP packaging | Detect actual local existence/producer/trigger/member list; fixture reproduction if safe | PRESENT/ABSENT/NOT_VERIFIED with proof; ZIP is not accepted review |

All rows must be `PASS / FINDING / NOT_VERIFIED / N/A` with evidence. An untestable runtime probe does not invalidate a faithful **read-only audit report** if its limitation is explicit and independently adjudicated, but it **does not qualify a runtime protection as working**.

## C. Cross-reference ledger

Required relation keys in `h0-audit-report.md` or `h0-file-manifest.csv`:

- `AGENTS/current policy -> invoking skill -> referenced current Owner decision -> permitted output path`;
- `skill -> script/hook/execpolicy -> CLI command and environment assumptions -> fixture/result`;
- `H0 observed finding -> exact root cause -> smallest H1 option -> required test -> Owner scope gate`;
- `delivery producer -> trigger -> output -> manifest/hash -> independent review provenance` (only when observed).

Version and portability conditions: Windows 11, PowerShell, current Codex CLI/IDE, actual selected model, root vs scoped cwd, paths/quoting and hook trust/loading. No arbitrary `GPT-6.1 Sol PASS` because an OpenAI page lists the model.

## D. Evidence index format

For each check record `H0 evidence ID`, timestamp/timezone, operator/session role (if available), baseline SHA, command or manual inspection, exact cwd, CLI/runtime version, expected, observed, exit code, artifact path/blob/SHA, applicability, limitations, risk from performing the probe. Preserve negative attempts and errors. Do not count a failed/denied check as a successful behavior unless **the denial was the expected result**.

Deliverable artifacts are authorized **only after exact H0 activation** in [Owner authorization](owner-authorization-h0.md): audit report, inventory CSV, scenario results, H1 proposal and evidence index. An H0 independent review can report gaps or demand more evidence **within the same read-only scope**, but cannot authorize new files or remediation outside its role.
