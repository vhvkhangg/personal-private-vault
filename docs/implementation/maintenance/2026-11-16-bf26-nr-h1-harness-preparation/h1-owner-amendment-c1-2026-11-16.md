# BF26-NR-H1 — Owner Amendment C1: current-baseline qualification

**Date:** 2026-11-16 (UTC+07).  
**Amendment ID:** BF26-NR-H1-C1.  
**Parent authorization:** BF26-NR-H1-HARNESS-REMEDIATION.  
**Document state:** OWNER_RATIFIED_2026-11-16 / PENDING_PUBLICATION.  
**Documentation-recorder handback:** READY_FOR_OWNER_COMMIT / H1_C1_AMENDMENT_ONLY.  
**Owner-approved choice:** Option B — Current-baseline qualification.  
**Independent amendment preflight:** READY_FOR_OWNER_RATIFICATION / H1_C1_DOCS_ONLY.  
**Preflight criteria:** C1P-01–C1P-12 PASS; no actionable C1-PH## findings.  
**Owner acceptance:** EXPLICITLY_APPROVED_2026-11-16; C1-D01 through C1-D06 exactly as preflighted.  
**Amendment publication / actual published SHA:** PENDING_OWNER_PUBLICATION / NOT_ASSIGNED.  
**Execution rights:** NOT_EFFECTIVE until Owner publication and actual-SHA verification; Author Resume qualification remains mandatory.

**Status precedence:** The Owner Ratification Record in §12 records the current approval. Earlier DRAFT/PROPOSED/PENDING statements below describe the preparation checkpoint and are superseded only as specified there; the preflighted substantive requirements remain unchanged.

This is a separately dated proposal requested by the Owner, not an effective Owner decision, H1 execution grant, independent C1 re-review or H1 acceptance. Its preparation does not authorize report appends, native calls or configuration changes. The current H1 result remains H1_CHANGES_REQUESTED; Correction 1 remains H1_CORRECTION_1_PARTIAL / OWNER_DECISION_REQUIRED.

## 1. Authority, purpose and proposed precedence

The parent [Owner ratification](h1-owner-ratification-2026-11-16.md) was Owner-published at [802c31e715566272ef640c487942558717a54d34](https://github.com/vhvkhangg/personal-private-vault/commit/802c31e715566272ef640c487942558717a54d34). Its [exact allowlist](h1-exact-path-allowlist.md), [author handoff](h1-codex-author-handoff-draft.md) and [acceptance contract](h1-preflight-and-acceptance.md) remain controlling, subject only to the explicit proposed C1 decisions below if subsequently reviewed, accepted and published.

The proposal addresses two remaining matters:

- H1-FR01: independently identified PowerShell-wrapper literal false denial; a bounded source correction has been produced but has not received independent re-review.
- H1-FR02: missing historical global-config provenance; the proposed disposition is a new qualification of the current baseline, with historical uncertainty preserved.
- The parent ratification §3 and allowlist §D permit initially absent author reports only. All three now exist. Their no-clobber restriction currently prevents the proposed Correction 1 addenda.

If made effective, C1-D02–03 would define the new current-baseline evidence route for FR02 under the handoff's ambiguous-safety STOP gate; C1-D05 would create only the named append exception to ratification §3 / allowlist §D. The ban on configuration edits in H1-D08, the 17-path source ceiling, diagnostic limits, independence, all-actionable-findings policy and later-phase gates would remain in force. No historical document is rewritten, and no unmentioned restriction is relaxed.

The [current Owner policy](../../../workflow/current-owner-delivery-policy.md), [audit-only role contract](../2026-11-15-bf26-nr-audit-preparation/codex-only-workflow.md) and [Docs Sync contract](../../../workflow/documentation-synchronization.md) apply within this exact scope. The direct Owner request authorizes this one draft; it does not assign this documentation author the H1 implementation or independent-review role.

## 2. Evidence and actual state checked for this draft

### 2.1 Local Git and source qualification — observed fact

Read-only observation at **2026-11-16T07:35:41Z**:

| Item | Observation |
| --- | --- |
| Branch / HEAD | main / 802c31e715566272ef640c487942558717a54d34 |
| Cached origin/main | 802c31e715566272ef640c487942558717a54d34 |
| Origin | Approved vhvkhangg/personal-private-vault GitHub repository |
| Index / working tree | Zero staged paths; 17 modified tracked paths, exactly allowlist §A; three original author reports untracked |
| Full tracked comparison | 3,551 current file hashes match the recorded C1 entry plus its two explicit hook/test corrections; zero unexpected path/hash delta |
| Delta since initial H1 author delivery | Only repository_safety.py and test_repository_safety.py changed; the other 15 H1 changed-source hashes still match that delivery |
| ACTIVE | [NO_ACTIVE_HANDOFF](../../handoffs/ACTIVE.md), unchanged; no D0/B0/X activation |
| New amendment destination | Absent before creation; create-new/no-overwrite required |
| Publication qualification | Owner-supplied published SHA confirmed in local Git, with parent documents present in that commit; no live remote request in this draft task |

Cached origin/main is not live-remote proof. No network request, native Codex invocation, test execution, Git mutation, stage, commit or push was performed for this draft. Raw test results described below were inspected as existing evidence.

Current byte hashes, measured again for the draft rather than copied without checking:

| Existing H1 path | Current SHA-256 |
| --- | --- |
| .agents/skills/codex-backend-audit/SKILL.md | AC6F5355AD9D92E6B1328F69F0D29EBD41DB0C3D323BEDF19AE4C4E6235A165C |
| .agents/skills/codex-backend-audit/assets/backend-audit-report-template.md | 4D9325E4E68DF2275F5E308392343F88BF47184919F1F9993A3D468A09F3D450 |
| docs/workflow/resume-prompts/codex-backend-audit.md | 1D35D2473A21E39285B52AE7EFDF9E2DD8E241A09558246D6C3DB8F164106F2C |
| .agents/skills/codex-create-handoff/SKILL.md | F7AE2698F1CFCC7B5D16701F49D1780A554522F40EEC72CEA45AED3A26770DC5 |
| .agents/skills/codex-final-review/SKILL.md | D587D56700DD5F099D2771ED8A4D4A24B98A916FBF0031F170C7CFCE82ACEB9E |
| .agents/skills/codex-pre-handoff-review/SKILL.md | C4B6B3BE70719EBEFA333594F9B202EA7DE926261FD238B29CE487668349F7B3 |
| .agents/README.md | 695121A2517AEB9786569DBC811CFC535A985831A5366749B4F88C11221E8E71 |
| .agents/rules/agent-handoff-workflow.md | 891AF072AE31BF5C89E7100B14D44210B89FA259EDBC3183CF2CB22EF90EAF7C |
| .codex/README.md | 6BF9BC9B6157FEFE8D900BBD274A74840F3BE85F2CD2562187FD5B83D39E27F7 |
| AGENTS.md | 45FC9E23E45103D4AE6F97596CC7CAB4E08F70B306AA27F33F09A964DA2A8B74 |
| backend/AGENTS.md | 969E3ADC88572D27D139359790EA89DC28228B5FD88A04A8E08BCA270F839250 |
| .agents/agents/backend-implementer/agent.md | 035B56B5C79CB5C7954A5E7043523496FD5B9F33C2DCF6747C1CBB7CAF7A35BF |
| docs/workflow/owner-phase-workflow.md | DA3144D244F682C7A879F03AEEBD07880155A780C6D23B2ABD491EC205447057 |
| docs/workflow/current-owner-delivery-policy.md | 62761C4FB98655142C886CBC2DA7CBFC72D7AF0EC98B2AFA98377565B46274B9 |
| docs/workflow/resume-prompts/README.md | F3AC2892D87452276D78EA11D6A569E4A108C41CEC13B6496939F0F7299D54C8 |
| .agents/hooks/repository_safety.py | 904F11C9E645C4AC3BDFAC63C885C5678ACC920BA5350A73A7BE9B93D43433F8 |
| .agents/hooks/test_repository_safety.py | CC8E1FE8ACFA46B7E8148A8DF0AD9DC985924087A8478AA117F3612AD105E8B9 |

The original evidence index's two hook hashes and 27-method result describe the initial author snapshot. They are not the current C1 hook hashes or a fresh 30-method result. The proposed addenda must make that distinction without rewriting the original index.

### 2.2 Independent review and C1 provenance

The independent H1 review is **chat-only**, not a new repository review file. Its original assistant output was read directly from the locally available session transcript and matched the provenance recorded in [C1 correction-integrity.json](../../../../logs/bf26-nr/h1/20261116-correction-1-mwiwye70/correction-integrity.json):

| Record | Exact provenance |
| --- | --- |
| Independent H1 review | Session 01a1e308-66a4-78d2-9439-fc1f7c287016; assistant event 2026-11-16T06:44:12.103Z; 14,115 characters |
| Review text SHA-256 | 5CB8FB256007FF5F6FCEE33078B01C8C3A00074F899D332ABE672F34D939097B |
| Review outcome | H1_CHANGES_REQUESTED; H1-FR01 LOW / DEFECT / OPEN; H1-FR02 MEDIUM / MANDATORY_EVIDENCE_GAP / OPEN |
| Original H1/C1 author | Session 01a1e26c-1cad-7160-9da9-4e50d443941b |
| C1 author handback | Original assistant event 2026-11-16T07:21:45.768Z; 4,430 characters; H1_CORRECTION_1_PARTIAL / OWNER_DECISION_REQUIRED |
| C1 handback text SHA-256 | E8A497B30F65F21C58E109C259C155BD4303953892A23C51458638362EB12094 |

Local transcript locators, with the installation prefix redacted:

~~~text
<CODEX_HOME>/sessions/2026/11/16/rollout-2026-11-16T13-25-41-01a1e308-66a4-78d2-9439-fc1f7c287016.jsonl
<CODEX_HOME>/sessions/2026/11/16/rollout-2026-11-16T10-34-58-01a1e26c-1cad-7160-9da9-4e50d443941b.jsonl
~~~

This document summarizes those records; it neither recreates them as an independently signed review nor changes their findings. The initial reviewer accepted NRH0-F01/F03 within their stated source/sampled-routing and narrowed-guidance limits, left NRH0-F02 open through FR01, and did not accept historical configuration equivalence. Reuse those accepted observations only while their dependencies remain valid.

| Existing evidence inspected | Result and limit |
| --- | --- |
| [Initial author handback](h1-implementation-handback.md), [self-review](h1-self-review.md), [source/test index](h1-source-test-evidence-index.md) | Read in full; original author claims distinguished from independent dispositions |
| [C1 integrity and actual correction diffs](../../../../logs/bf26-nr/h1/20261116-correction-1-mwiwye70/correction-integrity.json) | Both recorded diffs independently regenerated from preserved original-author fixture bytes and current source; exact diff strings and diff hashes match |
| [Final C1 unittest receipt](../../../../logs/bf26-nr/h1/20261116-correction-1-mwiwye70/tests-final.json) and [raw stderr](../../../../logs/bf26-nr/h1/20261116-correction-1-mwiwye70/tests-final.stderr.log) | Exit 0; 30 distinct named methods marked ok; names match current test source; unittest 0.880s, bounded elapsed 1.516s; capture/cleanup complete. Existing author-run evidence, not tests freshly run or independently accepted here |
| [AST receipt](../../../../logs/bf26-nr/h1/20261116-correction-1-mwiwye70/powershell-ast.json) and [six raw cases](../../../../logs/bf26-nr/h1/20261116-correction-1-mwiwye70/powershell-ast.stdout.log) | Six parse-only cases; three literals have zero executable commands; deletion/mixed/read controls each contain a Git CommandAst. No Git command was executed by these parse checks |
| [Quote-behavior comparison](../../../../logs/bf26-nr/h1/20261116-correction-1-mwiwye70/quote-behavior.stdout.log) | Recorded evaluator/Antigravity/Codex adapter distinctions for literal, executable deletion and read controls; not native hook enforcement |
| [Qualified provenance investigation](../../../../logs/bf26-nr/h1/20261116-correction-1-mwiwye70/config-provenance-qualified.json) | No complete historical snapshot recovered in its bounded search; selected historical fields exist, but cannot establish equivalence |
| Earlier provenance scans | config-provenance.json and config-provenance-complete.json remain preserved; the qualified receipt corrects their handling of transcript input_text blocks. Their earlier empty scan results are not evidence of absence |
| [Initial execution summary](../../../../logs/bf26-nr/h1/20261116-h1-mwiptdea/execution-summary.json), [adjudication](../../../../logs/bf26-nr/h1/20261116-h1-mwiptdea/execution-adjudication.json), [argv reconciliation](../../../../logs/bf26-nr/h1/20261116-h1-mwiptdea/argv-reconciliation.json) | 18 CLI invocations = 10 model attempts + six parser checks + two version/help calls; nine model exits 0 and one usage-limit exit 1; 36 routing decisions are a separate counting unit |
| Receipt preservation | All 76 receipt/stream files referenced by the original/C1 preservation manifests rehashed successfully; 77 existing files inventoried including the C1 integrity manifest itself. The original 54 files are preserved |
| Red/intermediate C1 runs | tests-before retains six failures and tests-nested-before retains one failure. Final/intermediate successes are not added together as distinct test counts |

The source-diff comparison and receipt inspection above support documentation preparation. They do not constitute the required independent FR01/FR02 re-review.

### 2.3 Current configuration observation and evidence cohorts

The global config was read without modification or secret-value output. Its current SHA equals the recorded resume/C1 SHA in C1-D02. Selected configured observations: model preference gpt-6.1-sol / effort max; Windows sandbox setting elevated; PPV trust entry trusted; no top-level global approval_policy or sandbox_mode override; hooks state metadata present; one node_repl MCP entry; 11 plugin entries. These are configuration observations, not proof of effective runtime enforcement or a complete safety qualification.

The exact standalone CLI path recorded in the historical argv was rehashed, rather than assuming an app-embedded executable or another PATH candidate was the same tool:

~~~text
Recorded executable suffix: OpenAI/Codex/bin/codex.exe
Current executable SHA-256: CE1945EB440B5481ED1233EBAF32B7056EE7E4E452FA62FFE1CBFB26103CDEE0
Recorded version: codex-cli 0.169.0
~~~

The binary hash matches the author/reviewer record. No version/help invocation was repeated. The recorded version is qualified by that matching binary, not newly observed CLI output. The current documentation session's permissions do not establish the separate native CLI's effective permissions.

The unchanged repository .codex/config.toml contains its own approval/sandbox settings and hook definition. Those settings must not be assumed to apply to an external fixture without that project configuration; conversely, global settings alone do not prove the fixture's full effective configuration.

| Cohort | Existing native evidence | Permissible interpretation |
| --- | --- | --- |
| Before observed config drift | Seven model attempts ending 2026-11-16T04:03:00.022Z; includes smoke, failed workflow attempt and 27 successful routing decisions | Historical responses under the recorded original-config observation; no retrospective current-baseline safety certification |
| After observed drift, before this amendment | Three resumed model calls beginning 2026-11-16T05:56:42.353Z; workflow/recovery/named-skill, nine routing decisions | Separate post-drift observations; not already the complete C1-D03 qualification process |
| Future amended current-baseline qualification | None yet | Requires all C1-D06 gates and fresh, stable-baseline receipts; never relabel older native calls as fresh |

Observed config mtime is 2026-11-16T05:47:30.635086Z. The timing separates the recorded cohorts; mtime does not identify the actor, exact delta or every instant at which config bytes may have changed.

## 3. Owner choice proposed, approval still pending

| Option | Evidence requirement / effect | Present limitation |
| --- | --- | --- |
| A — Recover historical provenance | Obtain authentic complete before-content and independently examine the safety-relevant delta | Not currently available from the bounded investigation; no substitute content may be invented |
| **B — Current-baseline qualification (proposed)** | Preserve historical uncertainty and qualify the current configuration, source and representative routing under C1-D03 | Prospective scoped acceptance only; subject to observability, the remaining two CLI invocations and independent rejection |
| C — Remain blocked | Preserve all material and defer H1 acceptance | No qualification or source publication |

**Decision requested after independent docs-only preflight:** explicitly accept or reject Option B and C1-D01–C1-D06 as one bounded amendment. Preparation of Option B is the Owner's direction; approval of its eventual execution remains pending.

**Approved decision:** PENDING. Do not fill an approval date, independent acceptance or published SHA by inference.

## 4. C1-D01 — FR01 correction remains pending independent review

**Proposed decision:** record H1-FR01 as **FIXED_PENDING_INDEPENDENT_REVIEW**. This is an author-correction status, not independent closure.

No additional FR01 source expansion is permitted. This amendment grants no further source edit, new hook, new test file, protection/config change or expansion beyond the existing H1 allowlist. The completed correction is confined to the two hook/test paths whose current hashes are recorded in §2.1.

A different independent reviewer must examine:

1. The actual C1 delta against the initial H1 author bytes, including raw quote retention through intermediate wrappers and removal of only the grouping layer.
2. The original literal counterexample, reverse quoting, cmd-to-PowerShell nesting and mixed literal/executable forms; literal text must remain data and actual supported deletion must still deny.
3. All **30 distinct final unittest results**, their tested source/Python/fixture dependency chain, both adapter outputs and executable adapter-entrypoint coverage. The three new methods are test_h1_fr01_powershell_payload_literals_remain_data, test_h1_fr01_executable_payloads_still_deny_without_overrides and test_h1_fr01_cli_adapters_preserve_literal_boundary.
4. All **six AST cases** and their parse-only character, preserved red/intermediate failures, benign branch-read controls and absence of deletion permission overrides.
5. The difference between evaluator/adapter evidence and actual native hook loading or pre-dispatch enforcement, which remains NOT_VERIFIED without direct evidence.

The earlier LOW classification does not waive an actionable finding. If the correction is rejected, report the exact residual defect and STOP for a separately bounded corrective direction within the unchanged H1 correction ceiling.

## 5. C1-D02 — FR02 historical provenance is not established

**Proposed decision:** explicitly preserve the following historical facts and limits:

| Field | Required record |
| --- | --- |
| Original global-config SHA-256 | 779A3228E7C0DD2B28A8AECEB4F907B2D82D87A36293BA78C8AA2AB742530535 |
| Current observed SHA-256 | 41FD8FB4B461366EA03174903ADF12AFDAEF76847EB4C4369BA3D8C2CEAAAC51 |
| Exact historical delta | **NOT_VERIFIED** |
| Cause | **ROOT_CAUSE_NOT_DETERMINED** |
| Historical safety equivalence | **NOT_ESTABLISHED** |
| Current FR02 disposition | **OWNER_DECISION_REQUIRED**, pending an effective amendment, qualification and independent disposition |

The proposed route accepts a **NEW current-baseline qualification process**, not a finding that old and current configurations were equivalent. Matching a few fields, a current semantic fingerprint, an unchanged CLI binary or a hash alone cannot prove configuration safety or reconstruct missing historical content.

This choice must not retrospectively certify earlier native executions, erase their failures, attribute the drift to the author without evidence, or close FR02 merely because the Owner approved this amendment.

## 6. C1-D03 — Observable current-baseline acceptance process

**Proposed decision:** after all execution gates, authorize only the following bounded qualification and documentation operations under the existing H1 safety envelope.

### 6.1 Read-only effective-configuration and fixture record

Before any native replay, the qualified H1 author must inspect and record, with sensitive values redacted:

1. The configuration actually applicable to the **exact selected executable, launch context and external fixture**: global/project/profile inheritance, launch arguments and relevant environment influence. Inspect safety-relevant approval/sandbox settings, trust, instruction sources, hook definitions/state, plugin/MCP/notify bindings and tool identities, provider/model selection settings and any override. Record configured values separately from directly observable effective behavior; missing visibility is NOT_VERIFIED.
2. Current global config SHA, relevant project/rules/instruction file hashes, all 17 H1 source hashes, exact CLI path/binary SHA and qualified version provenance, relevant interpreter/runner identity, fixture/helper/input hashes and actual argv. Validate source-fed packet bytes against the current sources.
3. A newly named, owned fixture outside the repository, with verified absolute boundary and no unexpected reparse escape, .git/origin, project .codex override, application credentials, live Owner data or production secrets. Inherited global configuration still requires inspection. No existing setting may be edited, disabled, bypassed or copied into a replacement policy to force success.
4. The allowed communication remains only the existing authenticated CLI's normal configured Codex model service for the fixed response-only cases below. No third-party/provider-account/application traffic, install/download, model override, tool-driven audit or destructive probe is authorized. If the current client cannot honor the fixture, communication or duration boundary safely, STOP before the call.

The author must provide reviewable sanitized field/semantic coverage, including relevant absent or unobservable fields and the reason for that classification. A file hash identifies bytes; it is not a substitute for examining those properties. Do not export auth stores, tokens, secret environment values or unredacted configuration into reports or receipts.

### 6.2 Stability and evidence reuse

- At resume, the expected global baseline is the current SHA in C1-D02. Unexpected drift before qualification is STOP / OWNER_DECISION_REQUIRED, not automatic selection of another baseline.
- Record the config hash before qualification, immediately before and after each native replay, and after qualification/report append completion. Require the same hash throughout. Unexpected drift triggers STOP, preservation of all captured evidence and renewed Owner disposition; do not restore or repair config.
- Recheck other relevant configuration inputs, CLI/source/fixture/helper hashes at those boundaries. A stable global hash alone does not establish a stable effective environment.
- Label every receipt with its actual pre-drift, post-drift/pre-amendment, or new-qualified-baseline cohort, timestamps, sources and environmental dependencies. No historical native receipt becomes a new current-baseline result.
- Reuse source/unit/parser evidence **only after** checking its exact source and relevant environmental dependencies. Original 27-method results qualify the original snapshot; the C1 30-method final receipt qualifies the corrected snapshot subject to dependency checks. The six execpolicy results remain parser-only; no-match is not allow. Reuse the unchanged-source/caller observations where justified.
- Previously recorded native responses may remain corroborating evidence with their limitations. If a required current-baseline conclusion depends on changed or unobservable configuration, those historical responses cannot fill the gap. Any further mandatory replay that cannot fit C1-D04 requires another Owner amendment.
- Preserve actual serving-model identity, automatic skill discovery/instruction loading and native hook trust/loading/enforcement as **NOT_VERIFIED unless directly evidenced**. Model preference, named-skill wording, parser results and supplied-source responses do not establish those properties.
- The independent reviewer may reject qualification if important safety properties remain unobservable, dependencies are not qualified, or the observations support a narrower conclusion than requested. Owner adoption of Option B does not compel a PASS.

### 6.3 Minimum representative H-T07/H-T09 current-baseline replay

The proposed minimum is **two native model invocations**, using fixed, finite source-fed packets and response-only routing questions. They exercise representative routing decisions, not real H/D/B/X work. Expected decisions are maintained as a separate review oracle, not supplied as the model's answers. Preserve complete prompt/input bytes and raw streams.

| Remaining invocation | Mandatory packet / observable result |
| --- | --- |
| **19 — H-T07** | Fresh-request and interrupted/resume-request Phase 15 refusal, plus an explicitly named legacy-skill request: three declared routing cases. Each must STOP / HISTORICAL_ONLY with archival reference and no historical audit, tools, report/status/ACTIVE change or phase activation. These are scenario responses, not proof that an actual CLI resume or automatic skill load occurred |
| **20 — H-T09** | Four synthetic eligible family representatives: H1 author, D0 inspector, B0 inspector and X reviewer, each with exact phase/grant/baseline/role/output facts. Also all 14 negative controls: missing grant, wrong phase, conflicting approvals, revoked grant, stale grant, wrong baseline, blocked preparation, wrong role, self-acceptance, wrong output, unaccepted H1, inferred next-phase authority, missing mandatory dependency and X mandatory evidence gap. Every negative must STOP at its actual failing gate; eligible responses confer no real authority |

This samples the four families; it does not claim every possible role combination, filesystem grant discovery, native hook enforcement or actual audit execution. The packet must use the current relevant skill/caller text, and include enough synthetic facts to test each gate separately without forcing the answer.

Before spending either invocation, assess offline whether both complete packets and their finite outputs can safely fit the ratified duration and fixture limits. Use the existing bounded-command mechanism with a 95-second execution cap and no more than 20 seconds total diagnostic/cleanup grace, within the parent 120-second maximum per invocation. Do not add a policy/model override to make them fit. Record actual start/end/exit, argv, inputs, complete output, absence/presence of tool events, timeouts and owned cleanup.

Success requires identifiable results for every declared case, correct gate decisions, no model tool events and unchanged protected state. A timeout, service refusal, partial/truncated answer, unexpected tool event, missing result or failed boundary is preserved and is not PASS. No extra retry, smoke, version/help or parser invocation is reserved. An unsafe or insufficient two-call plan must STOP for another explicit Owner amendment; mandatory cases may not be silently dropped or compressed into an unreviewable claimed PASS.

A reviewer may accept only **scoped sampled routing conformance under the newly qualified current baseline**, after examining raw evidence and every applicable H1A criterion. That disposition does not establish historical safety equivalence or any of the retained native unknowns.

## 7. C1-D04 — CLI allowance and correction accounting

**Proposed decision:** preserve these counters across all sessions and interruptions:

| Counter | Required accounting |
| --- | --- |
| Native CLI used | **18/20 consumed** |
| Existing allowance remaining | **At most two invocations**; shared H1 total, not a fresh allowance per author/reviewer |
| New allowance / reset | **NONE** |
| H1 correction-pair ceiling | **Three author correction + independent re-review pairs maximum** |
| Completed pairs now | **0/3**; initial implementation and the first independent H1 review did not form a correction pair |
| Current C1 | Author correction occurred; **independent C1 re-review has not completed** |
| When C1 counts | When both the actual author correction and its independent re-review have occurred, count **1/3**, even if that re-review still requests changes |

Every actual native CLI invocation counts, including failure, parser, version/help and any reviewer invocation. Multiple routing cases in one response are scenario counts, not extra or replacement CLI allowance. Offline document checks and an amendment's docs-only preflight do not complete an H1 correction pair.

If qualification, mandatory dependencies, safe completion or necessary independent verification cannot fit within the remaining allowance, STOP and request another explicit Owner amendment naming the missing evidence and additional allowance. Neither this draft nor later publication of it resets the budget, authorizes a third call, omits mandatory cases or manufactures PASS results.

## 8. C1-D05 — Narrow append-only exception for three existing reports

**Proposed decision:** only after this amendment becomes effective and Author Resume qualifies the state, permit a newly dated **Correction 1** section appended to exactly these existing files in this directory:

| Protected report | Original byte length | Original SHA-256 |
| --- | ---: | --- |
| [h1-implementation-handback.md](h1-implementation-handback.md) | 13973 | 7DE3D0F3A47AE99F10FD06F2FCC8409CE863B03805D3140A521A69326AA84DF2 |
| [h1-self-review.md](h1-self-review.md) | 9675 | 22A63185261773EE7A45880CA5F9C272765DCB29200C1D863AEDD180CFDFF068 |
| [h1-source-test-evidence-index.md](h1-source-test-evidence-index.md) | 18010 | 87E006C217A44AA3247962EDFF60FA78A80EC3A49659FF9E15C36A550C284AA5 |

Before appending, verify the whole original file against its hash/length above; unexpected content drift triggers STOP. After appending, verify that the complete original byte sequence remains an identical prefix. No rewrite, newline/encoding normalization, truncation, deletion, rename, replacement, altered heading or historical evidence substitution is permitted.

Each new section must identify this effective amendment and its actual published SHA, author/session provenance, date, current finding dispositions, source/evidence hashes, original versus new evidence cohorts, exact CLI/correction counters and remaining limitations:

- **h1-implementation-handback.md:** report the bounded FR01 correction, current-baseline qualification outcome or STOP, unchanged history and pending independent C1 review. No H1 acceptance/publication claim.
- **h1-self-review.md:** explicitly reassess H1A-01/07/08/10/11/12 in light of FR01/FR02, mark each author judgment as such, retain unknowns and distinguish it from independent acceptance.
- **h1-source-test-evidence-index.md:** append corrected-source/30-method and six-AST references, the actual C1 diff, dependency-qualified reuse, fresh qualification input/output receipts, failed attempts and cumulative counters. Do not replace the old 27-method manifest or add overlapping runs as distinct cases.

The original content, failures, test provenance, counters and limitations remain intact. The original reports' historical statuses are explained by the addenda, not edited in place. Current source hashes differ from the original hook manifest as recorded in §2.

Only newly named no-clobber ignored receipts under **logs/bf26-nr/h1/{new-unique-C1-run-id}/** may be created for future qualification and append integrity. Verify git check-ignore for each exact destination, file absence, owned boundary and secret redaction before create-new writes. Existing initial/C1 receipts, including both superseded provenance scans, may be read but never overwritten or replaced. No new tracked report, review file, archive, delivery output or blanket logs write authority follows.

## 9. C1-D06 — Mandatory execution, review and publication gates

The order is mandatory:

1. **Independent DOCS-ONLY amendment preflight.** A different Codex session reviews this exact new draft, parent contracts, original review/C1 findings, current source/hash evidence, safety observability, packet completeness, budget and append restriction. It returns its disposition in chat; this draft grants no review-file path, native call, source edit or implementation acceptance.
2. **Explicit Owner acceptance and publication.** After an acceptable preflight, the Owner must explicitly accept the concrete C1 decisions and publish the amendment on main. Publishing a still-unapproved draft or receiving this author's READY status is insufficient. Publish only the amendment at this stage; H1 implementation source and the three author reports are not thereby accepted for source publication.
3. **Verify actual published SHA.** Verify the real amendment content/commit and parent authorization. After approved-origin validation, use only the parent's already authorized read-only git ls-remote --heads origin refs/heads/main metadata check; compare the returned actual main SHA with local HEAD/origin/main and the Owner's amendment publication. Failure or unverifiable response is STOP, with no cached-ref substitute, fetch/pull or local-ref mutation. No such network check runs in this draft task. Amendment-only publication may advance HEAD; requalify the existing 17-path/three-report overlay against the unchanged source baseline and recorded C1 hashes rather than treating it as published implementation or resetting it.
4. **Qualified H1 Author Resume.** Resume the original H1 author session identified in §2.2, or an explicitly Owner-qualified Author Resume session that recovers the same grant, partial C1 state, sources, receipts, role boundaries and counters. A new session or role label alone is not a qualification or budget reset. This documentation-author role does not automatically become the resumed H1 author.
5. **Perform current-baseline qualification and approved appends.** Only then may that author perform C1-D03 within C1-D04 and append under C1-D05. Record actual outcome, all retained gaps and STOP conditions; self-review and stop awaiting a different independent reviewer.
6. **Different Codex independent C1 reviewer session.** The reviewer must be independent of the H1/C1 author and this amendment's authorship, examine the actual correction diff, raw 30-test/six-AST/adapter evidence, effective-configuration qualification, routing packets/receipts, reuse dependencies, report-prefix integrity and all affected H1A criteria. The initial H1 review or this docs-only preflight is not the C1 re-review. Output remains chat-only unless the Owner separately grants an exact new review path. CLI/correction totals carry forward without new allowance.
7. **H1 source publication remains gated.** No H1 source publication until both FR01 and FR02 receive acceptable independent dispositions and all other mandatory H1 acceptance requirements hold. FR02's possible acceptable disposition must expressly rest on the new current-baseline process, with historical equivalence NOT_ESTABLISHED; it cannot be closed by Owner selection alone. Only the Owner may later stage/commit/push an independently accepted H1 result and verify its actual published SHA.

H1 acceptance/publication still grants no D0/B0/X execution. Every later phase needs its own exact published Owner authority. No automatic takeover or routine Antigravity audit dispatch follows.

## 10. Strict boundaries and unresolved risks

**This draft task writes exactly one new file:** this amendment. It does not edit H1 implementation, any existing author report, ratification, allowlist, H0 document, ACTIVE, global/project config, rules or historical record. No native CLI model call, network request, configuration change, test run, Git mutation, stage, commit, push or D0/B0/X execution is authorized here.

The proposed future exception is confined to current-baseline qualification and the three append-only addenda after all gates. It grants no additional source paths, config/trust/sandbox/execpolicy modification, install/download, real audit, product/Backend/RAG/Frontend/provider work, deployment, ZIP or historical cleanup.

Unresolved risks requiring explicit review:

- Historical config content/delta and the actor/cause remain unknown; Option B cannot recover that history or certify earlier native safety.
- Selected config fields and a stable hash do not alone establish the full effective safety boundary. Important unobservable properties can still require rejection/STOP.
- Two batched native calls may not safely produce complete evidence within the fixed cap. Feasibility is not proven by this draft; incomplete results require STOP and another Owner amendment.
- Earlier native evidence has cohort/environment limitations; unchanged source alone does not make it newly qualified runtime evidence.
- FR01 has supporting corrected-source receipts but no independent C1 disposition yet. Both adapters' unit/entrypoint results remain distinct from actual native hook enforcement.
- Global config can drift again without a source change. No repair or automatic new baseline is permitted.
- The three reports are still initial historical author deliveries; their append permission is proposed and not effective.

## 11. Docs Sync Check and handback

**Classification:** proposal / separately dated draft; no change to current execution authority or immutable history.

| Disposition | Exact paths / assessment |
| --- | --- |
| UPDATED | Only docs/implementation/maintenance/2026-11-16-bf26-nr-h1-harness-preparation/h1-owner-amendment-c1-2026-11-16.md, created fresh |
| ALREADY_CONSISTENT | Parent ratification, allowlist, handoff and acceptance contract inspected and preserved; their superseded preparation headers remain historical under the published ratification, not a new grant |
| ALREADY_CONSISTENT | ACTIVE remains NO_ACTIVE_HANDOFF; the existing 17-path overlay, three original report byte sequences and existing ignored receipts are preserved during this draft task |
| BLOCKED_OWNER_DECISION | Option B/C1-D01–06 approval, amendment-only publication/actual SHA, qualified Author Resume and append exception remain pending after independent docs-only preflight |
| NOT_VERIFIED | Exact historical config delta, historical safety equivalence, cause, serving-model identity, automatic skill discovery, native hook enforcement and future qualification feasibility/results |
| DEFERRED_OUT_OF_SCOPE | Edits to README/navigation/current policy, existing reports, source/config/history and any review-file creation; no D0/B0/X, product, provider or deployment activation |

Relative links identify existing controlling documents and locally available ignored receipts; receipt links are local evidence, not a claim those files are Git-published. Transcript provenance is explicitly attributed. No new review report, receipt or package is produced by this documentation task.

**Handback:** READY_FOR_INDEPENDENT_AMENDMENT_PREFLIGHT, for this draft only. H1 remains unaccepted, FR02 is not closed, historical equivalence is not proven, CLI usage remains 18/20, completed correction pairs remain 0/3, and C1 awaits independent re-review.

**Next step:** Owner assigns a different Codex session | independent DOCS-ONLY preflight of this one amendment plus its controlling evidence | BF26-NR-H1-C1 DRAFT / parent BF26-NR-H1-HARNESS-REMEDIATION | explicit Owner acceptance, amendment-only publication and actual-SHA verification must follow before Author Resume | native 18/20; C1 pending; maximum three correction pairs.

**STOP after preparing this draft.**

## 12. Owner Ratification Record — 2026-11-16

**Amendment:** BF26-NR-H1-C1.  
**Parent authorization:** BF26-NR-H1-HARNESS-REMEDIATION, published at 802c31e715566272ef640c487942558717a54d34.  
**Owner decision date:** 2026-11-16 (UTC+07).  
**Current ratification status:** OWNER_RATIFIED_2026-11-16 / PENDING_PUBLICATION.  
**Decision source:** the repository Owner's explicit current instruction to record this ratification; this recorder does not supply independent acceptance.

The repository Owner explicitly **APPROVES Option B — Current-baseline qualification** and **ratifies C1-D01 through C1-D06 exactly as specified in the independently preflighted amendment**. No substantive change to those decisions, acceptance criteria, packet requirements, STOP gates or historical evidence is made by this record.

### 12.1 Independent amendment preflight and reviewed artifact

**Actual preflight verdict:** READY_FOR_OWNER_RATIFICATION / H1_C1_DOCS_ONLY.  
**Criteria:** C1P-01–C1P-12 PASS. **Actionable C1-PH## findings:** NONE.

The Owner supplied that verdict in the ratification instruction. The original independent assistant output was also read directly from the available local transcript and matched the following provenance:

| Evidence | Verified record |
| --- | --- |
| Independent preflight session | 01a1e35e-f37f-77e0-98bd-38c1860362ed |
| Original review event | 2026-11-16T08:17:05.405Z; 12,276 characters |
| Original review text SHA-256 | 51A53B0ACEA86E2C2DD85F047FDA745B7EF9E52EB71A9350A9DCBABE2391B7FD |
| Independently reviewed draft SHA-256 | 6727EA4CD4CAE74C5D2BD99E3202F05925DB80D63882D369CC89B2A8C439CFCC |
| Independently reviewed draft size | 38,561 bytes; matched immediately before ratification editing |
| Role separation | Preflight session differs from H1/C1 author 01a1e26c-1cad-7160-9da9-4e50d443941b, original H1 reviewer 01a1e308-66a4-78d2-9439-fc1f7c287016 and amendment documentation author 01a1e342-229e-7f73-b1a2-46175e82c844 |

Local original-review locator, installation prefix redacted:

~~~text
<CODEX_HOME>/sessions/2026/11/16/rollout-2026-11-16T15-00-13-01a1e35e-f37f-77e0-98bd-38c1860362ed.jsonl
~~~

That review accepted the amendment specification for Owner ratification, including its mandatory visibility and feasibility STOP gates. It did not perform current-baseline qualification, close FR01/FR02 or independently accept H1. This record does not create a new review report or replace the original chat-only output.

### 12.2 Scoped precedence and effectiveness

The preceding DRAFT/PROPOSED/PENDING statements, the original “Approved decision: PENDING”, the earlier preparation handback and its preflight next step describe the **preparation checkpoint**. This dated record supersedes them **only** to record:

1. Completion of the independent docs-only amendment preflight with the result in §12.1.
2. The Owner's explicit acceptance of Option B and C1-D01 through C1-D06 without substantive alteration.
3. The resulting OWNER_RATIFIED_2026-11-16 / PENDING_PUBLICATION status and amendment-only publication next step.

All original substantive C1-D01–C1-D06 requirements remain identical, including configuration observability, evidence cohorts/reuse, exact two-call packet requirements, stable-hash checks, mandatory STOP gates, report-prefix preservation, budgets and reviewer independence. Earlier “proposed” wording within those preserved decisions identifies their preparation provenance; it does not undo the Owner's approval recorded here or bypass any remaining gate.

**Execution rights remain ineffective until the Owner publishes this amendment on main and its actual published SHA is verified under C1-D06.** No amendment publication or future commit SHA is asserted here. The parent SHA and cached origin/main are not evidence that this amendment has been published. Qualified Author Resume remains required before any current-baseline qualification or report append.

### 12.3 Preserved findings, permissions and budgets

| Item | Ratified limit / unchanged disposition |
| --- | --- |
| H1-FR01 | FIXED_PENDING_INDEPENDENT_REVIEW; no additional source expansion or independent closure |
| FR02 exact historical delta | NOT_VERIFIED |
| FR02 cause | ROOT_CAUSE_NOT_DETERMINED |
| Historical safety equivalence | NOT_ESTABLISHED; no retrospective certification of earlier native executions |
| FR02 / H1 acceptance | FR02 is not closed; H1 is not independently accepted by this approval or record |
| Native CLI budget | 18/20 consumed; at most two existing authorized invocations remain; no reset, increase or free retry |
| Correction budget | 0/3 completed; C1 awaits independent re-review; maximum three correction pairs, with C1 counted only after author correction and its independent re-review have both occurred |
| Configuration | No global/project Codex configuration, trust, sandbox, approval, hook policy or execpolicy modification |
| H1 source publication | Prohibited until acceptable independent FR01/FR02 dispositions and all mandatory H1 acceptance requirements hold; only the Owner publishes |

The approved append-only exception covers **exactly** [h1-implementation-handback.md](h1-implementation-handback.md), [h1-self-review.md](h1-self-review.md) and [h1-source-test-evidence-index.md](h1-source-test-evidence-index.md), preserving their complete original byte prefixes under C1-D05. It becomes usable only after amendment publication, actual-SHA verification and qualified Author Resume. No report append is performed by this ratification task. Existing receipts remain immutable; future receipts must satisfy the newly named, ignored, no-clobber H1 diagnostic-path restrictions.

The original H1 Author or an explicitly qualified Author Resume session must remain separate from the independent C1 Reviewer session. Neither this recorder's checks nor the amendment preflight constitutes the required independent C1 re-review. Approval does not activate D0/B0/X or any product/provider/deployment work.

### 12.4 Recorder qualification, Docs Sync Check and handback

Before editing, the exact reviewed draft hash was verified. Read-only qualification at 2026-11-16T08:25:29Z confirmed main with HEAD = cached origin/main = 802c31e715566272ef640c487942558717a54d34, zero staged paths, the same 17 modified H1 tracked paths and four untracked documents. The four comprise this amendment and the three original author reports. No live remote check was made in this docs-only task.

All 3,551 tracked file hashes match the recorded C1 state; all 17 H1 source changes, the three original report hashes and the 77 existing H1 receipt/stream files remain intact. The 76 files referenced by the preservation manifests rehash correctly. ACTIVE remains NO_ACTIVE_HANDOFF. Global config remains at SHA-256 41FD8FB4B461366EA03174903ADF12AFDAEF76847EB4C4369BA3D8C2CEAAAC51; project config/rules and the recorded CLI binary remain unchanged.

The ratification edit is limited to necessary top-level status/approval metadata plus this dated record. The complete original §1–§11 body is preserved byte-for-byte. No source, report, receipt, configuration, H0 document, parent ratification/allowlist, ACTIVE or other file is edited.

| Docs Sync disposition | Exact scope / evidence |
| --- | --- |
| UPDATED | Only docs/implementation/maintenance/2026-11-16-bf26-nr-h1-harness-preparation/h1-owner-amendment-c1-2026-11-16.md: top-level approval metadata and this Owner Ratification Record |
| ALREADY_CONSISTENT | Original substantive decisions, acceptance criteria, packets, STOP gates and historical evidence unchanged; earlier preparation-status statements explicitly scoped by §12.2 |
| ALREADY_CONSISTENT | Source/report/receipt/config integrity, ACTIVE, existing local references, native 18/20 and completed correction pairs 0/3; no test/native CLI/network invocation or Git publication in this task |
| NOT_VERIFIED | Actual amendment published SHA, historical delta/cause/equivalence and future runtime qualification/feasibility; native serving/discovery/enforcement unknowns retained |
| DEFERRED_OUT_OF_SCOPE | Owner publication, Author Resume/current-baseline execution, report appends, independent C1 re-review, H1 source publication and later phases; no delivery ZIP |

**Handback:** READY_FOR_OWNER_COMMIT / H1_C1_AMENDMENT_ONLY.

**Recommended commit message:** docs(audit): ratify H1 C1 current-baseline amendment

**Next step:** Owner | publish only this ratified amendment and verify its actual published SHA | BF26-NR-H1-C1 under the parent authorization | qualified H1 Author Resume follows only after those gates; H1 source publication still requires independent acceptance | CLI 18/20; completed correction pairs 0/3; C1 re-review pending.

**STOP after documentation ratification. Owner alone stages, commits and publishes.**

