# BF26-NR-H0 harness audit report

**Status: H0_REPORT_SUBMITTED / INDEPENDENT_REVIEW_PENDING**  
**Role:** Codex H0 auditor / report producer; not H0 independent reviewer or H1 implementer.  
**Audit baseline:** `7c521fb9998bc1ad0a86e7225aa605f0de17e261`, ordinary `main` checkout.  
**Date:** 2026-11-16, Asia/Bangkok (UTC+07); receipts use UTC. Session identifier is not exposed by the task interface.

## Entry decision and publication reconciliation

Entry gates passed. The Owner's current request explicitly activates the bounded H0 audit. This is combined with the **published** [Owner authorization](owner-authorization-h0.md), especially G.1 and G.2, rather than inferred from an empty ACTIVE register.

| Gate | Observed evidence | Disposition |
|---|---|---|
| Approved origin | Local remote URL matched the approved GitHub repository `vhvkhangg/personal-private-vault`; no URL credentials printed | PASS |
| Published ref | The single permitted network command, `git ls-remote --heads origin refs/heads/main`, returned `7c521fb9998bc1ad0a86e7225aa605f0de17e261`; exit 0 | PASS |
| Local baseline | HEAD and cached origin/main equal that SHA; branch main | PASS |
| Published package | HEAD is `docs(audit): ratify bounded H0 harness audit`; exactly nine new H0 Markdown files, including G.1 ratification and the handoff | PASS |
| Source/index/untracked at entry | `git status --porcelain=v1 --untracked-files=all --ignored`: no tracked changes or untracked files; 68,709 ignored files | PASS, qualified below |
| ACTIVE | Lines 1–5 say NO_ACTIVE_HANDOFF after V1 publication; all lower live-looking handoffs are historical checkpoints | PASS |
| Outputs | All five authorized report paths were absent; new receipt directory was absent, non-reparse ancestors checked, exact receipt paths passed git check-ignore | PASS |
| Capability vs permission | Ambient tools expose danger-full-access, approval never, network enabled. These capabilities are not an H0 grant. Exact output allowlisting, reviewed commands, no-clobber receipts and final hashes preserve the requested boundary | PASS for conduct; no claim of mechanical read-only sandboxing |
| Current authority | No competing newer executable scope identified in the canonical current register/navigation. H0 G.1 is newer and specific; ordinary Antigravity routes do not govern this H0 | PASS |

**Material label reconciliation:** owner-authorization-h0.md line 4 still says pending publication / execution not authorized; handoff line 4 says draft / waiting grant; README also retains draft STOP wording. These are real stale checkpoints, not silently ignored. G.1 (2026-11-15) records OWNER_APPROVED / OWNER_RATIFIED for this exact H0. G.2 expressly makes execution conditional on publication and executor gates, and says STOP **until** steps 3–4 are satisfied. The actual nine-file published commit, matching three refs, and this fresh execution request satisfy those conditions. The starting preparation SHA `b5a51d4...` is the parent, not an immutable execution SHA. No evidence of a substantive permission amendment beyond the ratified contract was found. The documented preflight result is **Owner-attested in G.2**, not a preflight newly performed or independently reconstructed here. This resolves activation; it does not activate H1, D0 or B0. All existing files remain unchanged.

## Inventory, depth and provenance

[File manifest](h0-file-manifest.csv) accounts for **254 tracked paths**: **137 mandatory harness files** plus **117 directly relevant or referenced context paths**. Metadata-only reference targets are not claimed as deeply audited documentation.

Mandatory counts regenerated at the exact audit SHA:

| Set | Files |
|---|---:|
| .agents, including 29 rules, 43 skills, 3 skill assets, 3 roles, hook/registration/tests and README | 82 |
| .codex | 3 |
| Exact-basename tracked AGENTS.md | 22 |
| scripts | 10 |
| docs/workflow | 20 |
| Relevant .github files | 0 (no tracked .github tree) |
| Mandatory union | **137** |

All 43 skill names match their directory names; no duplicate declared names. All skills were read as source; only bounded-command-execution and documentation-writing were applied as working guidance. The historical backend audit skill was **read, never invoked**.

The separate local tier contains **12 ignored files**: eight existing Python bytecode caches, one dependency AGENTS at `frontend/node_modules/next/AGENTS.md`, and three AGENTS copies inside retained historical logs (including another Next copy). The live dependency instruction was read; its identical historical copy and two historical entry-source AGENTS received hashes/metadata only. They are descendants outside this audit's root/docs execution path, not ancestor overrides. No additional untracked harness file or ignored .codex override was identified. Caches were not imported: probe sources were copied into a fresh fixture and bytecode writing disabled. No environment-secret file or private log content was opened.

External config/instructions are separate provenance: safe settings and hashes only in the manifest/receipt. Global AGENTS contains the Owner's communication preferences, not new H0 permissions. Checked filesystem ancestor AGENTS paths above the repository were absent. Disposition/depth/reason/callers/benefit/risk/regression are recorded per skill and rule. FULL means source inspected, not runtime conformance; RISK_TARGETED names the inspected boundary; STATIC_METADATA is not substantive acceptance. Every row remains pending independent review.

## Instruction and command graph

The mechanically extracted graph contains **324 local Markdown links from the 137-file corpus**, all resolving to existing tracked files. It does not validate section anchors, external URLs, every bare code path, or effective client loading. Literal skill-name callers are separately recorded in E12. Apparent reference cycles, such as frontend security/testing and review/helper references, are progressive-disclosure relationships; no mandatory recursive launcher was found.

| Relation | Authority / actual downstream behavior | Result |
|---|---|---|
| Global communication instructions -> root/scoped AGENTS -> current Owner policy | Scope-specific current Owner decisions override dated notices; nested files refine domain ownership but cannot revive revoked work | Source mapped; effective startup merge/cap NOT_VERIFIED |
| Current user H0 request -> published G.1/G.2 -> H0 handoff -> five named reports | Explicit H0 output boundary; ACTIVE remains empty | Observed execution route |
| codex-create-handoff -> newest navigation -> exact Owner Authorization + preparation review -> ACTIVE | Its discovery section explicitly distinguishes empty ACTIVE from missing approval, rejects competing/revoked/behind-baseline cases | Static PASS; no handoff created |
| Existing codex-backend-audit -> Phase 15 README/status -> Phase 15 tooling/ACTIVE -> Antigravity -> BACKEND_AUDIT_READY | Existing executable historical branch remains in source; current-non-rag is not implemented | NRH0-F01; do not invoke |
| Project config PreToolUse -> repository_safety.py --codex -> evaluator -> deny or empty JSON | Matcher is Bash; Windows command is relative to startup cwd; empty JSON preserves ambient policy | Fresh evaluator tests; native coverage remains conditional |
| owner-git.rules -> codex execpolicy check | Prefix rules deny direct listed Git/gh operations and selected token-split wrappers | Fresh CLI parser proof; no automatic-load claim |
| bounded-command-execution -> run-bounded-command.py -> owned child -> receipt/stdout/stderr | normal 300s default, guarded full-suite 2,100s; six focused tests used a shorter 60s outer bound | Fresh fixture proof |
| review/self-review/takeover -> separate-session acceptance -> Owner publication | Existing source explicitly disallows self-independent acceptance; generic implementation route remains Antigravity | Preserve for ordinary tasks; audit integration proposed |
| delivery producer -> trigger -> ZIP -> manifest/reviewer | Directory exists but is empty; no producer found in inspected harness | No ZIP or automation acceptance inferred |

The two Antigravity-role files and architecture-auditor role use that client's frontmatter/tool conventions. They are not native Codex registrations. Likewise .agents/rules are instructions, not Codex execpolicy. Optional Graphify is installed; no extraction/query/install was run, and its cache was not used as authority.

## H01–H13 coverage

| ID | Status | Evidence and precise limit |
|---|---|---|
| H01 | PASS | E01/E03/E08, manifest: full mandatory union, local/external tier, relevant dependencies; no .github CI coupling found |
| H02 | FINDING | NRH0-F03; root 43,645 bytes / 293 content lines, backend 7,180 bytes, ACTIVE 99,101 bytes. Actual instruction truncation/loading is NOT_VERIFIED |
| H03 | FINDING | All 43 skills and 29 rules individually classified; names/links pass. NRH0-F01/F03 concern executable routing and stale current guidance, not a reason to delete domain guards |
| H04 | FINDING | NRH0-F01; old source/templates/resume remain Phase 15-oriented; required H1 rewrite is proposed only |
| H05 | PASS | STATIC H-T01–06, current discovery rules, historical V1 publication at 757643e9 and current closed V1; no fresh model-routing test |
| H06 | FINDING | Static independence guards stand; NRH0-F01 identifies missing integrated audit-specific Codex author/reviewer route |
| H07 | NOT_VERIFIED | E11: trusted project and gpt-6.1-sol preference in user config; project full-access/never settings and active capability observed. Exact client layer merge, hook trust fingerprint and all tool/MCP coverage not proven |
| H08 | FINDING | NRH0-F02; E04/E05/E06. Direct/wrapped publication evaluator cases covered; aliases and other untested transports are limitations |
| H09 | PASS | E06 21 hook tests and E07 six selected watchdog methods, all fresh fixture PASS. No full runner suite, 300/2,100s endurance, WSL, native console Ctrl+C or real deadlock proof claimed |
| H10 | FINDING | NRH0-F01/F03; actual workflow/resume/template graph and retained historical routes reviewed. No historical records rewritten |
| H11 | NOT_VERIFIED | codex-cli 0.169.0 and configured gpt-6.1-sol observed. Actual model identity, entitlement and fresh model/session behavior not independently observed; no provider call or model switch |
| H12 | PASS | STATIC authority negatives + actual output boundary/final integrity; reports cannot issue an H1 grant; post-H1 denial tests remain future requirements |
| H13 | PASS | E12: OBSERVED_EMPTY_DIRECTORY; not tracked, not ignored, zero recursive members; no validated ZIP producer/trigger |

These statuses qualify **assessment coverage**, not runtime safety or final system readiness. The [scenario report](h0-scenario-results.md) retains all H-T01–20 including H1-only and unverified cases.

## Findings

### NRH0-F01 — Current audit launcher and dependent routes need the ratified H1 transition

- **Type / severity / confidence:** DESIGN_PROPOSAL (mandatory Owner-directed transition), MEDIUM, HIGH. Disposition: PROPOSED_H1 / NOT_IMPLEMENTED; not a claim that old Phase 15 was defective when accepted.
- **Authority:** parent owner-contract O-03/O-04, especially explicit 2026-11-15 supersession; H0 handoff sections 2 and 4.
- **Source facts:** existing skill lines 10–46 and 140–217 still bind execution to Phase 15 prerequisites, ACTIVE mutation and Antigravity remediation; its asset is headed Phase 15 and uses BA15 outcomes. codex-create-handoff lines 64–71 can still redirect a Phase 15 request to this launcher; final-review lines 177–179 can request a closure rerun. The audit resume prompt line 20 explicitly resumes historical Phase 15 behavior. Templates hardcode Antigravity and omit current Non-RAG audit roles/modes.
- **Expected future behavior:** same skill name, only current-non-rag active mode; explicit legacy phase-15 STOP; exact H/D/B/X phase, role, baseline, Owner grant and output allowlist; separate Codex authors/reviewers for this audit.
- **Mechanism / consequence:** replacing only the main skill leaves callers, template outputs or recovery routing able to select the former workflow. H0 bootstrap avoids this today; D0/B0 must remain unavailable until H1 is independently accepted and published.
- **Smallest options:** rewrite the existing skill/asset/resume and the actual caller graph together; retain ordinary Antigravity routes conditionally. No new audit skill, generic takeover authority or global role conversion. Exact candidate paths and alternatives are in the proposal.
- **Verification:** H-T01–11/16–18, with H-T07/H-T09 only after H1; stale-mode and no-grant negatives; saved output/role/counter readback; no Phase 15 status/ACTIVE writes.
- **Owner gate / frozen impact:** separately ratify exact H1 paths and behavior; no business/schema/frozen-phase amendment proposed. No H1 batch or correction allowance exists yet. Independent verification pending.

### NRH0-F02 — Branch deletion escapes both protections in an ordinary option/wrapper form

- **Type / severity / confidence:** DEFECT, MEDIUM, HIGH for the two parser/evaluator results; native unauthorized execution is **not** demonstrated. Disposition: VERIFIED_ACTIONABLE / OPEN / OWNER_H1_APPROVAL_REQUIRED.
- **Authority:** Owner-only destructive Git policy; .codex/rules/owner-git.rules explicitly forbids branch -D. H08 requires examining direct, option and wrapper coverage.
- **Exact source:** repository_safety.py line 32 denies add/commit/push/tag; evaluate_command lines 293–326 checks several destructive commands but no branch deletion. permission_overrides_for_command line 363 also broadly recognizes branch. Prefix rules match direct branch -D only.
- **Reproduction:** E04 sends command **text**, not commands to a shell: `git -C H0_ONLY_TEXT branch -D H0_ONLY_TEXT` and `powershell -Command git branch -D H0_ONLY_TEXT` return allow; Codex adapter returns empty JSON. E05 uses only `codex execpolicy check`: direct branch -D is forbidden, global-option and string-wrapper forms have no matched rule. All checks exit 0; no branch was created or deleted.
- **Expected / root cause:** destructive branch deletion should not become ambiently permitted merely by a supported global option/wrapper. The shared semantic evaluator omits this subcommand, while prefix-only protection cannot compensate.
- **Risk:** under broad ambient permissions an instruction mistake using these forms lacks either tested deny. This is a specific defense gap, not proof of malicious exploitation or universal bypass. Aliases are a separately documented unsupported class.
- **Minimal fix / alternative:** add semantic branch-deletion rejection and negative regression cases to the existing evaluator/tests; ensure permission overrides cannot grant that mutation. Retain benign branch/status reads. Merely adding more prefixes does not address arbitrary supported option placement.
- **Verification:** direct, -C/-c, absolute Git and supported wrapper deletion texts; benign branch list/status; malformed inputs; fixture-only native pre-dispatch proof on the actual approved client, never real repository mutation. Re-run the hook suite and relevant execpolicy checks.
- **Owner gate / frozen impact:** exact hook and hook-test approval needed; no schema/API change, no new security framework. Current prefix file can remain unchanged unless a specific further test warrants it. No historical B2 finding/counter is reopened or reset. Independent verification pending.

### NRH0-F03 — Active harness guidance retains obsolete execution/tooling state

- **Type / severity / confidence:** DEFECT, LOW, HIGH. Disposition: VERIFIED_ACTIONABLE / OPEN / OWNER_H1_APPROVAL_REQUIRED. Not waived because LOW.
- **Source facts:** .agents/agents/backend-implementer/agent.md line 28 calls B1 awaiting independent review and B2 pending, while current Owner policy and the actual published runner/hook describe accepted/published tooling. .agents/README.md line 3 and owner-phase-workflow's current opening still advertise S19-1 handoff/publication-pending checkpoints; backend/AGENTS.md lines 20–32 report an active stopped Phase 18 handoff. Actual current ACTIVE lines 1–5 says V1 closed / NO_ACTIVE_HANDOFF, P18 closed, S19-2 suspended.
- **Mechanism / consequence:** historical state is embedded in automatically/explicitly loaded operational guidance without consistent local historical labeling. Correct precedence resolves it here, but it adds false-stop/reopening risk for scoped startup/recovery. No such incorrect model action is claimed as observed.
- **Minimal fix / alternative:** replace obsolete operational state assertions with a short link to current policy/ACTIVE, label retained dated history explicitly, correct the one tooling-status sentence. Keep domain constraints, historical decisions and counters. A wholesale rewrite of 22 AGENTS or all domain skills is not justified.
- **Verification:** H-T01/04/06/16/18 and startup/source graph checks; preserve frozen permissions and existing non-audit routes; compare before/after AGENTS byte sizes without inventing token savings.
- **Owner gate / frozen impact:** exact current-guidance paths in the H1 proposal only. No edit to original dated reviews/Owner decisions or accepted evidence. Independent verification pending.

## Observations and unresolved runtime evidence

1. **Context cost, not proven truncation:** root AGENTS has 43,645 bytes; root + backend + Film instructions total 54,458 bytes. No project_doc_max_bytes override was present in inspected safe config. The active client's default cap, actual loaded subset and precedence at representative startup cwds were not measured. Do not claim a 32 KiB default or missing instruction from these sizes alone. Optional compression must preserve failure shields and be regression-tested.
2. **Observed conservative hook rejection:** a valid-looking large PowerShell here-string collector was rejected before dispatch for ambiguous/unclosed quoting. No script was created or run. E10 preserves the rejection; its exact cause beyond this classifier decision is not proven. The rejected collector was not retried, encoded or launched through another tool. Subsequent smaller, separately hook-approved Git/PowerShell reads produced inventory/link receipts; no protection was disabled. This is a tooling limitation/possible false positive, not evidence authorizing parser relaxation.
3. **Safety is conditional:** existing tests cover common direct/absolute/wrapped publication text. E04 also observes an alias-based text returning allow. Arbitrary shell code, encoded commands, unsupported wrapper depth, missing/untrusted hooks, native MCP/IDE/GUI actions and other clients are not certified. Policy source itself documents these limits.
4. **Model and client evidence:** Windows build 10.0.26200, PowerShell 7.7.0, Python 3.12.3, Git 2.45.1.windows.1, ripgrep 15.2.0; installed CLI 0.169.0. Process metadata shows multiple Codex binaries and an IntelliJ process (263.7307.200.0-IU); it does not identify this session's serving binary/model or prove IDE integration enforcement. No model/provider/catalog request was made.
5. **delivery:** existing ordinary directory, zero children, no tracked members and no ignore rule. Filesystem creation timestamps are metadata, not producer attribution. No packaging script matched the inspected harness search. Historical ZIP production/manual actions and any outside-repo pipeline remain NOT_VERIFIED; no ZIP was generated, inspected as acceptance, or exported.
6. Historical CP18 diagnostic and migration/setup scripts have write/install/security effects. They were source-inspected only, not run. Keep archival interpretation; do not reuse them as an H0-safe launcher.

## Assessment exit and Docs Sync Check

HA-01/02/03/05/06/07/08/09/10 are satisfied for a submitted assessment with explicit findings/limits. HA-04 is **qualified / runtime NOT_VERIFIED**, as permitted by the H0 acceptance contract; no Sol/hook safety acceptance follows. The five reports form a proposal/evidence package, not an approved H1 handoff.

**Docs Sync:** ALREADY_CONSISTENT — published G.1 authority, H0-only output boundary, empty ACTIVE and frozen product/RAG/frontend barriers. DEFERRED_OUT_OF_SCOPE — F01/F03 current harness integrations and stale status notices; no existing document corrected. NOT_VERIFIED — effective startup/model/hook loading and historical delivery automation. Historical reviews, Phase 15 evidence, DP/RO/V1 backlogs/counters, H1-F pause/FR-13, P17 frozen/P18 closed/S19-2 suspended and provider OFF/unset authority are preserved. No application, provider or full-suite readiness is asserted.

**Next step:** Owner / separate Codex H0 independent reviewer | review these five H0 reports against the published handoff; explicit separate review-write grant needed for a new review file | H0 only; then Owner chooses exact H1 paths/tests | no H1 implementation or counters authorized.

