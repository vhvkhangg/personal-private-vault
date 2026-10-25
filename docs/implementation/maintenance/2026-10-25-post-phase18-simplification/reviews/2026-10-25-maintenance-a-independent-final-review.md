# Independent final review — Maintenance A Documentation & Harness Governance

- Date / reviewer: 2026-10-25 / Codex, independent of package preparation.
- Mode / authority: Owner-requested maintenance/governance review; `codex-final-review` and `codex-review-coverage`. No implementation handoff is required for this review mode.
- Baseline: local `main`, HEAD `217cb5d24bf7a62d7348d31838a7a79b01a524bd`; unstaged working tree plus untracked package files. Index had no changes.
- Disposition: **CHANGES_REQUESTED**.
- Findings: **2 MEDIUM blockers; 4 LOW non-blocking findings**. No CRITICAL/HIGH finding.
- Reviewed input: 54 added/modified package files, plus 5 tracked deletions present in the supplied working tree. Exact inventory below. The ZIP named by the preparer was not independently available for a byte comparison.
- Input identity: SHA-256 `147abf7dcb3ce8e6ed7fc50f86db037db9dec41844152e0e31393bc59cac7f70` over the sorted compact JSON mapping of the 59 paths to their raw-byte SHA-256, with `null` for deleted paths, captured before review-artifact writes.

## Summary and requirement checks

The authoritative policy correctly records the requested slice, phase, three-phase milestone and two audit layers; three remediation rounds; mandatory Antigravity self-review; severity; Owner-only Git; and narrow future scope. Its distinction between current executable behavior and future Maintenance B is accurate. Two active skill contracts still conflict with that policy. Fix only the two blocking documentation defects, then independently re-review the changed material; full application suites are unnecessary.

| Owner requirement | Result and evidence |
| --- | --- |
| Independent Codex review after each slice; final review after each phase | PASS for policy/cadence: [current policy](../../../../workflow/current-owner-delivery-policy.md), lines 15–18; implementer and final-review skills. Phase-final result-token drift is LOW MA-A04. |
| Milestone after every three phases | PASS: current RAG windows P17–19 and P20–22; historical Backend 3/6/9/12 milestones remain historical. Current policy lines 19–20 and `codex-milestone-review`. |
| RAG-wide audit, then combined Backend + RAG audit | PASS: separately authorized future gates, with evidence reuse; current policy line 20 and audit template. Neither starts a new phase. |
| Antigravity self-review after implementation and remediation | PASS: `antigravity-implement-handoff` lines 8, 42–53 require self-review before handback; the same invocation covers corrections. Test-only skill also makes it mandatory. Self-review is neither independent acceptance nor a remediation round. |
| At most three corrective rounds | PASS for the rule: initial implementation/review excluded; three correction/re-review pairs per active slice, no session reset or automatic fourth attempt. Current policy line 36 and implementation/takeover skills. Wording defect MA-A05 is LOW. Historical two-round decisions are not reset. |
| CRITICAL/HIGH/MEDIUM block; LOW/INFO consolidated later | PASS: policy severity table, templates, review/implementation skills. Mandatory unverified acceptance cases also block; explicitly deferred Maintenance B verification is not fabricated as PASS. |
| Detailed, testable handoffs; reuse-first, KISS, YAGNI; no scope creep | FINDING: acceptance examples, reuse map and exclusions are specified, but MA-A01 retains an unconditional full-suite requirement. |
| Maintenance workflow is usable without inventing a feature phase | FINDING: MA-A02; numbered-phase prerequisites and implementation-only correction routing remain in shared skills. |
| Owner-only stage/commit/push; no separate staged-tree review gate | PASS as policy. Normal Owner staging remains. Direct-token policy syntax works; automatic Windows-client interception remains NOT_VERIFIED, as explicitly disclosed. |
| Codex verbosity and watchdog described truthfully | PASS for documentation: `model_verbosity=low` is not a tool-output cap; current runner is fixed 1,200/2,100 seconds, with no progress monitor. Future 300-second policy remains pending separately authorized Maintenance B. |
| Templates, navigation, historical evidence and delivery inventory | FINDING: LOW MA-A03/04/06. Checked relative targets/anchors resolve; historical Phase 17/18 records and D20 manifest have no working-tree changes. |
| `logs/` untouched | PASS during this independent review: identical before/after path-and-content digest for 95,547 regular files; details below. No historical pre-package ZIP equality is claimed. |

## Blocking findings

### MA-A01 — MEDIUM / OPEN / blocking: handoff skill still requires a full suite for every slice

- **Location:** `.agents/skills/codex-create-handoff/SKILL.md:77`, active “Minimal handoff and remediation budget” paragraph.
- **Reproducible evidence:** the paragraph directs the author to list “the single final full-suite gate” without conditioning it on the approved slice's risk or requirements. Compare `docs/workflow/current-owner-delivery-policy.md:46–50`, `.agents/skills/codex-pre-handoff-review/SKILL.md:63`, and `docs/templates/handoff.md:28`, which require proportionate, source-applicable verification. This paragraph is an active instruction, not a marked historical record.
- **Impact:** a small future slice inherits an unnecessary full-suite acceptance obligation directly from its executable handoff authoring skill. That recreates the test burden the approved governance change removes. Existing phase-specific mandatory gates still retain their own authority.
- **Simplest correction:** replace the unconditional phrase with the approved slice's risk-based verification gate; require a full suite only when the approved scope or a concrete integration/drift risk warrants it. Preserve historical and explicitly required frozen gates.
- **Verification to close:** compare that paragraph with the current policy and handoff template; a focused documentation-only slice must be expressible without adding an application full-suite requirement. No application tests needed.

### MA-A02 — MEDIUM / OPEN / blocking: shared skills do not complete the approved maintenance route

- **Locations:** `.agents/skills/codex-pre-handoff-review/SKILL.md:19–24, 67–95`; `.agents/skills/codex-final-review/SKILL.md:173–174`. Approved caller contract: `docs/workflow/owner-phase-workflow.md:9`.
- **Reproducible evidence:** the Owner workflow explicitly invokes pre-handoff review for an approved maintenance scope. The skill instead requires an approved **next phase** with `README.md` and `preparation-review.md`, and its status/report writes only target that numbered phase. It has no maintenance equivalent. Separately, final review admits governance review with no active implementation handoff, but its blocking-result branch always sends the Owner to `antigravity-implement-handoff` or `antigravity-test-slice`; both begin from `ACTIVE.md`. In this review the canonical ACTIVE is `NO_ACTIVE_HANDOFF`, and Maintenance A is a documentation scope, not a production handoff.
- **Impact:** following the approved maintenance workflow literally either stops on irrelevant phase prerequisites or routes documentation findings to a nonexistent implementation handoff. The Owner must improvise the missing route, risking accidental phase/handoff activation. No actual activation occurred in this review.
- **Simplest correction:** add a short maintenance branch that validates the explicitly approved canonical maintenance scope and records preparation status/reviews there. Make final-review correction routing respect that scope's documentation owner; retain Antigravity routing for actual implementation handoffs. Reuse existing maintenance artifacts; do not add a phase, approval layer or staged-tree gate.
- **Verification to close:** trace an approved docs-maintenance case with `NO_ACTIVE_HANDOFF` through entry, findings/status and correction handback. It must remain entirely under the maintenance scope, with no invented Phase 19 files or production permission. This is a documentation walkthrough, not authorization to start Maintenance B.

## Non-blocking findings — consolidated milestone/audit backlog

### MA-A03 — LOW / OPEN: package inventory does not reconcile the Git and ZIP baselines

- **Locations:** maintenance `PACKAGE_CONTENTS.md:3, 68–70`; `planned-doc-cleanup.md:7`.
- **Evidence/reproduction:** compare the 54 listed paths with `git diff --name-status HEAD` plus `git ls-files --others --exclude-standard`. Five additional tracked deletions exist: `PATCH-MANIFEST.json`, `PATCH_TOOLS/PATCH_MANIFEST.json`, `PATCH_TOOLS/README.md`, `PATCH_TOOLS/apply_d25_closeout.py`, `README_PATCH.txt`. The docs say the helpers were already absent from the Owner ZIP and that the package contains no deletions; they do not explicitly classify these Git deltas as pre-existing/excluded or intended cleanup.
- **Impact:** the package's “exact” inventory is not the complete local Git inventory. Attribution of those deletions to Maintenance A is **not established**. Historical D23 records already distinguish the two root transfer envelopes from canonical acceptance evidence; this is not evidence of lost protected receipts.
- **Minimal correction / proof:** distinguish package-relative changes from the five pre-existing or separately intended Git removals, with their disposition and reference check. Do not automatically restore obsolete helpers or impose a staged-tree review. Compare the two inventories again.

### MA-A04 — LOW / OPEN: review result tokens differ between current and legacy templates

- **Locations:** `docs/templates/phase-final-review.md:4`; `.agents/skills/codex-final-review/SKILL.md:160–180`; `.agents/skills/codex-final-review/assets/review-template.md:41`.
- **Evidence/reproduction:** phase-final template success is `PHASE_FINAL_VERIFIED`; the shared skill's positive branch only records `READY FOR OWNER COMMIT`. The legacy review asset also offers `CHANGES REQUIRED`, whereas the active routing uses `CHANGES_REQUESTED`.
- **Impact:** a human can infer the intended review result, but future records can use inconsistent statuses. No failing status-parser automation was demonstrated, so this is not a blocking runtime defect.
- **Minimal correction / proof:** document the result/next-action mapping by review mode and point the legacy asset at the current template/status vocabulary. A text comparison is sufficient; preserve historical completed reviews.

### MA-A05 — LOW / OPEN: three-round takeover evidence still says “both”

- **Location:** `.agents/skills/codex-implementation-takeover/SKILL.md:16`.
- **Evidence/reproduction:** the entry condition requires three failed corrective/re-review rounds but then says to record “both immutable report paths.”
- **Impact:** the evidence instruction is ambiguous; the repeated explicit three-round rule still controls, so this does not prove a two-round takeover bypass.
- **Minimal correction / proof:** require all three correction/re-review references and dates. Re-read the entry-condition list; no executable test needed.

### MA-A06 — LOW / OPEN: harness report overstates rule propagation

- **Location:** maintenance `harness-review-2026-10-25.md:6`.
- **Evidence/reproduction:** it says eleven current shared rules “receive forward policy.” `git diff --name-only HEAD -- .agents/rules/` lists six changed rules: `agent-handoff-workflow`, `backend-python`, `code-reuse-and-patterns`, `context-efficiency`, `documentation`, `tests`. The remaining five shared rules are retained unchanged. The totals of 26 rules and 38 skill entrypoints do match disk.
- **Impact:** the propagation summary overstates modifications; no missing substantive safety behavior in those five retained rules was demonstrated.
- **Minimal correction / proof:** say eleven shared rules were inspected, six updated and five retained, if that is the intended claim. Reconcile with the diff and actual tree; no new rule files are necessary.

## Independent verification and limits

| Check actually performed | Result / limit |
| --- | --- |
| `git status --short`, `git diff --stat`, `git diff --name-status HEAD`, untracked-file inventory, deleted blobs via `git show HEAD:<path>` | 33 modified + 21 new + 5 deleted paths before this review's writes. All 54 present package files inspected through complete added content or their changed hunks, with relevant surrounding contracts. Five deletions inspected/classified separately. |
| Recursive changed-folder inventory | `.agents/agents`: 2 files; `.agents/rules`: 26; `.agents/skills`: 38 entrypoints + 3 assets; `.codex`: 3 files; `docs/templates`: 11; maintenance scope: 6 input files. Relevant shared unchanged rules, Python guidance, hook/config sources and template assets cross-checked. No application-package changes to inspect. |
| Relative Markdown file-target/anchor check across the 54 present files | 347 local target occurrences and 16 heading/explicit-anchor occurrences checked; zero missing targets or anchors found. External GitHub URL reachability was not exhaustively tested; local baseline/source records were inspected. |
| `git -c core.whitespace=cr-at-eol diff --check` | Exit 0. Git emitted a CRLF-to-LF normalization advisory for `docs/implementation/handoffs/README.md`; no compiler/IDE warning-free claim. |
| `C:/msys64/ucrt64/bin/python.exe -B .agents/hooks/test_repository_safety.py` | Exit 0; **13 passed**, 0 failures/errors. Standard-library tests, not proof of invocation by the Antigravity Windows client. |
| `C:/msys64/ucrt64/bin/python.exe -B scripts/test_run_bounded_command.py` | Exit 0; **5 passed**, 0 failures/errors; about 6.2 seconds. Existing runner smoke/owned-tree tests only. Temporary receipts stay outside `logs/`; no new progress-aware watchdog is exercised. |
| `codex --version`; `codex execpolicy check --help` | Installed CLI **0.164.0**; check command available. Python used for these governance checks: UCRT **3.11.7**, `os.name=nt`; no claim of application RAG environment verification. |
| `codex execpolicy check --rules .codex/rules/owner-git.rules <tokens>` | Six checks, all exit 0: direct `git commit -m test`, `git add .`, `git push origin main` -> `forbidden`; `git status`, `git -C . commit -m test`, `powershell -Command "git commit -m test"` -> no matching rule. Tokens were inspected by the policy checker; no Git mutation/publishing command was executed. |
| Official Codex documentation comparison | [Rules](https://learn.chatgpt.com/docs/agent-configuration/rules) supports the prefix syntax, restrictive decision and trusted-project loading distinction. [Configuration reference](https://learn.chatgpt.com/docs/config-file/config-reference) describes model verbosity as a response setting. Explicit policy checks do not prove automatic rule loading or arbitrary shell/connector interception. |
| Current watchdog source inspection | `scripts/run-bounded-command.py:17–18, 91`: fixed 1,200/2,100-second caps and `proc.wait(timeout=timeout)`. Approved future inactivity/resource diagnosis remains unimplemented. |
| Historical/frozen boundaries | No dirty paths under Phase 17/18 implementation/evidence or application code/tests/dependencies; `.agents/hooks*`, `.codex/config.toml`, runner/tests and `docs/D20-PATCH-MANIFEST.json` unchanged. ACTIVE remains `NO_ACTIVE_HANDOFF`; indexing remains inactive. |
| `logs/` before/after raw-byte comparison | **95,547** regular files in both passes. SHA-256 `0e0c73b0619c296dcb9b839c983b5dd9b2553ed2a75b6dd932b9002b43efcf26` over JSON of the sorted `(relative path, content SHA-256)` pairs, identical. No writes, deletion, relocation or truncation by this review. |
| Graphify navigation | One bounded existing-graph query located safety-hook/test references; output was budget-truncated. Used only for navigation; conclusions verified in source files, with no graph regeneration. |

No Java build, application Python full suite, model/worker/indexing job, dependency installation, production change, staging or publication was performed. Automatic Windows Codex/Antigravity interception, tool-rendering control and new watchdog behavior remain **NOT_VERIFIED / DEFERRED TO SEPARATELY AUTHORIZED MAINTENANCE B**. Their absence is not a new Maintenance A runtime blocker because this package explicitly makes no enforcement claim.

## Shared coverage ledger

All 29 shared dimensions are accounted for proportionately. Final-review business correctness here means governance correctness; application semantics remain frozen.

| Dimensions | Classification / rationale |
| --- | --- |
| 1 Owner intent; 3 architecture consistency | PASS: approved maintenance scope, explicit exclusions, frozen P17/18 and inactive execution routing retained. |
| 2 traceability; 5 folder structure; 8 DRY/KISS/YAGNI; 26 tooling; 28 docs/governance; 29 delivery hygiene | FINDING: MA-A01/02 block; MA-A03–06 are recorded non-blocking discrepancies. Actual changed subtrees and relevant unchanged controls inspected. |
| 14 credentials/evidence retention; 16 operations/timeouts; 22 test evidence; 27 logging | PASS for applicable documentation and existing smoke evidence; current/future limitations explicit, no secret/config-value changes or log mutation. No new runtime capability certified. |
| 21 client-integrated security testing | NOT_VERIFIED, explicitly deferred: actual automatic Windows-client rule/hook loading and interception. Parser checks and hook unit tests are narrower evidence; Maintenance B is not authorized here. |
| 4 module ownership; 6 transport/exception placement including ADR-0016; 7 code SOLID; 9 code patterns; 10 domain logic; 11 APIs; 12 input validation; 13 application auth/consent; 15 transactions/concurrency; 17 database; 18 runtime complexity/resources; 19 retrieval/recommendation quality; 20 parser/OCR fidelity; 23 benchmarks; 24 evaluation science; 25 dependency lifecycle | N/A: no changed application code, wire/schema/dependency/model contracts, enabled ingestion, authentication or retrieval behavior. These are not reopened frozen-phase gates. |

## Exact reviewed input inventory

Statuses below are relative to Git HEAD, not assertions about when the Owner's ZIP acquired each difference. These are the 59 paths captured before review writes.

| Status | Path |
| --- | --- |
| Modified | `.agents/README.md` |
| Modified | `.agents/agents/architecture-auditor/agent.md` |
| Modified | `.agents/agents/backend-implementer/agent.md` |
| Modified | `.agents/rules/agent-handoff-workflow.md` |
| Modified | `.agents/rules/backend-python.md` |
| Modified | `.agents/rules/code-reuse-and-patterns.md` |
| Modified | `.agents/rules/context-efficiency.md` |
| Modified | `.agents/rules/documentation.md` |
| Modified | `.agents/rules/tests.md` |
| Modified | `.agents/skills/antigravity-implement-handoff/SKILL.md` |
| New | `.agents/skills/antigravity-self-review/SKILL.md` |
| Modified | `.agents/skills/antigravity-test-slice/SKILL.md` |
| Modified | `.agents/skills/backend-testing/SKILL.md` |
| Modified | `.agents/skills/bounded-command-execution/SKILL.md` |
| Modified | `.agents/skills/codex-create-handoff/SKILL.md` |
| Modified | `.agents/skills/codex-final-review/SKILL.md` |
| Modified | `.agents/skills/codex-implementation-takeover/SKILL.md` |
| Modified | `.agents/skills/codex-milestone-review/SKILL.md` |
| Modified | `.agents/skills/codex-pre-handoff-review/SKILL.md` |
| Modified | `.agents/skills/codex-review-coverage/SKILL.md` |
| New | `.agents/skills/documentation-writing/SKILL.md` |
| Modified | `.agents/skills/local-rag-engineering/SKILL.md` |
| Modified | `.codex/README.md` |
| New | `.codex/rules/owner-git.rules` |
| Modified | `AGENTS.md` |
| Deleted; attribution not established | `PATCH-MANIFEST.json` |
| Deleted; attribution not established | `PATCH_TOOLS/PATCH_MANIFEST.json` |
| Deleted; attribution not established | `PATCH_TOOLS/README.md` |
| Deleted; attribution not established | `PATCH_TOOLS/apply_d25_closeout.py` |
| Modified | `README.md` |
| Deleted; attribution not established | `README_PATCH.txt` |
| Modified | `docs/AGENTS.md` |
| Modified | `docs/README.md` |
| Modified | `docs/implementation/README.md` |
| Modified | `docs/implementation/agent-launcher-prompt.md` |
| Modified | `docs/implementation/handoffs/README.md` |
| New | `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/PACKAGE_CONTENTS.md` |
| New | `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/README.md` |
| New | `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/harness-review-2026-10-25.md` |
| New | `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/owner-decision-docket.md` |
| New | `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/planned-doc-cleanup.md` |
| New | `docs/implementation/maintenance/2026-10-25-post-phase18-simplification/rag-docs-decision-audit.md` |
| Modified | `docs/implementation/rag-phase-plan.md` |
| Modified | `docs/roadmap.md` |
| New | `docs/templates/README.md` |
| New | `docs/templates/adr.md` |
| New | `docs/templates/audit.md` |
| New | `docs/templates/finding.md` |
| New | `docs/templates/handoff.md` |
| New | `docs/templates/milestone-review.md` |
| New | `docs/templates/owner-decision.md` |
| New | `docs/templates/phase-final-review.md` |
| New | `docs/templates/pre-handoff-review.md` |
| New | `docs/templates/review.md` |
| New | `docs/templates/slice-closeout.md` |
| Modified | `docs/workflow/agent-development-workflow.md` |
| New | `docs/workflow/current-owner-delivery-policy.md` |
| Modified | `docs/workflow/owner-phase-workflow.md` |
| Modified | `docs/workflow/rag-phase-delivery-guardrails.md` |

## Review writes and Owner next action

This review added only this dated report and synchronized the maintenance README's status/link. It did not implement any finding correction or edit ACTIVE, policy, templates, runtime files or frozen evidence. The new report is an additional review artifact, outside the 59 input paths; the README was already in that input inventory.

**Next step:** return the two blocking documentation findings for bounded Maintenance A remediation, then rerun independent `$codex-final-review`. Antigravity implementation remediation requires a valid approved maintenance route; do not invoke an empty ACTIVE or start a feature phase to work around MA-A02. LOW findings stay recorded for consolidated milestone/audit cleanup. This review grants no Maintenance B or Phase 19 authorization. No commit message is supplied while blockers remain.
