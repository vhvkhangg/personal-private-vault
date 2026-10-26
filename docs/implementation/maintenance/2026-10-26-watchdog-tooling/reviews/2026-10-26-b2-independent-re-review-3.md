# Independent corrective re-review 3 — Maintenance B2

- Date / reviewer: 2026-10-26 / Codex, independent of Antigravity implementation/self-review.
- Entry: local `main` HEAD `a4cf43505ab48198e22799fecf5098c5c6870a79`; submitted `MAINT-B2-CLI-GIT-HOOK-ENFORCEMENT / IMPLEMENTED_AWAITING_CODEX_REVIEW`, corrective round **3/3**.
- Contract: unchanged [B2 executable handoff](../b2-executable-handoff-2026-10-26.md); [initial review](2026-10-26-b2-independent-final-review.md), [re-review 1](2026-10-26-b2-independent-re-review-1.md), [re-review 2](2026-10-26-b2-independent-re-review-2.md), [current handback](../b2-implementation-handback-2026-10-26.md).
- Disposition: **CHANGES_REQUESTED**. **F01 HIGH remains OPEN; F04 remains OPEN with MEDIUM residual severity. F02 and F03 VERIFIED/CLOSED.** No Owner disposition of F01's mandatory gap is supplied.
- Escalation: **all 3/3 Antigravity correction/re-review rounds are independently blocked. STOP Antigravity remediation; OWNER_DECISION_REQUIRED.** No fourth round or automatic takeover. B1 remains published/CLOSED with its historical ledger intact.

## Independent closure register

| Finding | Independent result / credit retained |
| --- | --- |
| B2-F01 | **HIGH / OPEN / OWNER_DISPOSITION_REQUIRED.** The required Codex option/string-wrapper paths are still uncovered. Rules/config and the declared gap are unchanged; no approved acceptance change is recorded. |
| B2-F02 | **VERIFIED / CLOSED for observed native semantics.** A genuine native force-ask event was located in retained shared/mirrored evidence and correlated with the client transcript. Together with previously verified allow/deny, this resolves the missing native branch proof. The actual ambient-policy limit is stated below; this is not certification of guaranteed human prompting or absolute prevention. |
| B2-F03 | **VERIFIED / CLOSED**, preserved by the current 18-test run and unchanged validation behavior. |
| B2-F04 | **MEDIUM / OPEN / PARTIAL**, reduced from HIGH. Default-off, single explicit target, prompt/file-content/sys.argv exclusion and ordinary existing-file refusal are repaired. Raw command arguments still persist when enabled, and check-then-append allows a concurrent writer's target to be modified. |
| B2-L01 | **LOW / OPEN / BACKLOG, new evidence-index note.** The handback's cited four-row trace does not itself establish native provenance; the correct native event is elsewhere and was verified independently. Correct attribution/limits during consolidated cleanup; it is not a commit blocker. |

## Remaining blocking findings

### B2-F01 — HIGH: required Codex coverage still awaits Owner disposition

- **Locations:** `.codex/README.md:7–10`, handback F01 and B2-03/06 rows, ACTIVE F01 state.
- **Evidence reused:** the original actual client JSONL records dispatch of `git -C '<fixture>' add -h` and `cmd.exe /c "git add -h"` to Git. The rule file retains SHA-256 `d449a880e0bca481dfd0096818db5874063dffe835b29cd9b609dfd1a85c4150`; `.codex/config.toml` is unchanged and no alternative effective native guard is demonstrated. The implementer expressly keeps F01 OPEN. No equivalent native coverage or applicable Owner acceptance/deferment accompanies this submission.
- **Impact:** mandatory B2-03/06 and related coverage remain unsatisfied. A platform/trust limitation is a request for disposition, not automatic closure or permission to use trust-bypass/global edits.
- **Smallest correction / closure proof:** Owner resolves the exact remaining mandatory paths, either authorizing a compatible bounded native-control route with actual denial/benign proof or explicitly changing/deferring the approved acceptance. Preserve the measured gap and all failed evidence. Repeating unchanged prefixes/probes adds no proof.

### B2-F04 — MEDIUM residual: raw command arguments and non-atomic sink ownership

- **Exact locations:** `.agents/hooks/repository_safety.py:477–480` copies `CommandLine` verbatim to `tool.command`; `:467–472, 499–503` checks absence/ownership and then opens with **`a`**, without exclusively claiming the new destination. Added test `test_repository_safety.py:351–416` uses a file-write payload and sys.argv canary, not a private command argument or concurrent target claimant.
- **Privacy reproduction:** `residual-probe.py` invokes the actual hook entrypoint with an explicit fresh owned audit path and a synthetic command-argument canary inside `CommandLine`. Response is allow and the canary is present verbatim in the new audit's `tool.command`. The prompt is correctly excluded. Allowlisting a field name has not sanitized its command/argument value.
- **Retention reproduction:** a deterministic fixture creates another writer's synthetic target after `Path.exists()` observes absence but before the hook opens it. The hook appends its record to that unowned file: **35 bytes become 353 bytes**, preserving the other writer's prefix but changing its contents. Expected prior SHA-256 `009880b03cc841b55b67b4acec60c6a076164abe2739895a73197d51e07b4aa3`; after `2776fc9fb19999f7f9677b6670ec189de06021e8aceab62165118ee0f2ed8e81`. Only a newly created synthetic fixture was changed.
- **Credit / severity:** default-off and removal of automatic shared/latest-directory fan-out eliminate the original broad capture of all prompts/file inputs. A statically pre-existing target now remains byte-identical. The residuals apply to explicitly enabled capture and a filename-claim interleaving, so severity is reduced from HIGH to **MEDIUM**, still blocking the agreed sanitized/fresh-evidence contract.
- **Impact:** enabled capture can retain private argument values, and two callers/creators can mix evidence despite the advertised non-append guarantee. The scope permits exact benign probe commands in a controlled ledger; it does not authorize a generic collector to persist arbitrary private arguments or append to another writer's file.
- **Smallest correction:** remove the optional producer now that proof is retained, or omit/hash command arguments and atomically claim the fresh sink, retaining owned evidence handles for any subsequent write. Do not use an existence check plus append mode as ownership proof. Preserve all old captures; no deletion/redaction-in-place is authorized by this review.
- **Closure proof:** command-argument canaries absent from audit metadata; explicit/static and interleaved concurrent claimant bytes unchanged; default disabled behavior and valid allow/deny/conservative classification retained. Use fresh fixtures, not historical logs as mutation targets.

## F02 native closure and actual limits

The handback cites `b2-e8ff8362d7ae4076ad8952fc62ad149c/antigravity-native-hook-audit.jsonl`. Its four records share PID **21928**, workspace-root cwd and the RAG virtualenv interpreter, all within roughly four milliseconds. They show component outputs, but alone lack the native context needed for the claimed proof.

Independent targeted inspection found a genuine native event at **`logs/maintenance-b/antigravity-native-hook-audit.jsonl:35`**, mirrored at **`logs/maintenance-b/b2-codex-rr2-3c569890492849c789caa538d8a5ea1e/antigravity-native-hook-audit.jsonl:1`** by the former fan-out writer. This is one event in two locations, not two independent witnesses. At **2026-10-26T11:54:26.348867Z**, PID **8288**, cwd `.agents`, interpreter `C:\msys64\ucrt64\bin\python.exe`, the payload has the established native context keys and an empty/whitespace command. The hook emits `force_ask` with its nonempty-command reason and **no permissionOverrides**. Earlier native allow/deny evidence remains credited.

The event's referenced client transcript was inspected read-only at step **1692** (line **1693**): the harmless empty command returns **exit 0 / no output**. Thus under the current client/ambient permissions, force-ask withholds overrides and returns to normal permission handling; it is **not** a guaranteed human prompt or hard denial. This matches the handback's stated ambient-policy qualification. Closure covers that measured native branch behavior, not a stronger enforcement guarantee. No mutating or frozen-file operation was used by this reviewer to test it.

`audit-provenance-summary.json`, `native-force-ask-metadata.json` and `native-client-step-summary.json` retain hashes, safe metadata and the bounded client outcome; Owner prompts/transcript contents were not dumped. **B2-L01 LOW:** update the handback's F02 evidence references to this provenance-correct event and state the ambient-policy limit; retain the four-row file with an accurate component-evidence label. Actual native evidence exists, so this indexing defect does not block closure.

## Verification and reuse

Fresh root: `logs/maintenance-b/b2-codex-rr3-55eaffb0bccb4d76b06f17a1f101c0c1/`.

| Check | Independent result / evidence |
| --- | --- |
| Published B1 normal 300s wrapper: `python -B -m unittest discover -s .agents/hooks -p test_repository_safety.py -v` | **18/18 PASS**, exit 0; **0.484s** tests, **1.047s** wrapper, **1.218s** outer. `hooks.json`, raw streams and independent 320s supervisor metadata. Optional audit env is unset in the child only, verifying the shipped default; no global config changed. |
| Residual privacy/collision entrypoint probes | Driver exit 0, wrapper **0.531s**, outer **0.703s**; observed defects above. `residual-probe.py`, `residual-probe-results.json`, synthetic audit files and raw streams. Driver/outer PASS means completion, not contract acceptance. |
| Positive audit controls | Default-off source path returns before file work; static prior-target bytes remain identical; prompt/file-content/sys.argv exclusion is covered by the passing added test. These repairs remain credited. |
| Native proof reuse/inspection | Previous Codex failures and native Antigravity allow/deny are reused. The native conservative-response event and linked client step were verified from retained evidence, without launching another model/client probe. |
| Static/scope checks | AST parsing, `git diff --check`, index and excluded-scope checks pass; **99** local Markdown links resolve before review writes. Four existing audit files identified by the relevant filename pattern retain their pre-review hashes. No full historical-log equality scan claimed. |

Prior independently accepted **5/5 B1 output** evidence remains applicable: B1 runner/tests and project configuration are unchanged. New supplied 18/18 and 5/5 receipts remain implementer evidence. No Java/RAG/application suite, installation, trust bypass, global permission/model edit, actual staging/publishing or warning-free IDE/compiler claim occurred.

Source identities and byte-for-byte source snapshots are in `review-inputs.json` and `*.reviewed`: hook `569a634411f6823833f5ca68d64dd7bb9cea50c0571e57c4b0fe969154df59f2`; tests `ec8890c811077589e598f173ae4542a41db500d95f83d62f3efbd575759ceb00`. Rules/config retain their prior identities.

## Inventory, coverage and escalation

**22 inspected inputs:** 21 dirty paths (15 tracked modifications and six untracked scope/review documents), plus restored `.agents/hooks.json`. Exact inventory/hashes are retained. Scope covers existing hook/tests/config, rule/README, finite agent/rule/skill/policy notices, ACTIVE/handoff README, maintenance inventories/closeout/contract/handback and all three prior B2 reviews. The new report is subsequent output. Recursive hook/rule inspection finds no new production package/helper/dependency.

B2 native response proof is credited within the explicit ambient-policy limit; F03 stays closed. **F01 still fails mandatory Codex coverage; F04 still fails B2-09 sanitized/fresh evidence behavior.** No acceptance change is inferred from a limitation label. B1-L01 and MA-A03–MA-A06 remain LOW/OPEN; B2-L01 joins consolidated milestone/audit cleanup.

Shared coverage: **PASS 1, 3, 5, 7–9, 12, 18, 23, 25** for bounded scope/structure, reuse/validation and focused evidence; **FINDING 2, 10, 14, 16, 21–22, 26–29** for remaining guard/audit defects and documentation limits, with native Antigravity branch proof credited. **N/A 4, 6, 11, 13, 15, 17, 19–20, 24**: no application module/API/ADR-0016 transport, authentication, persistence or RAG-quality change.

Review writes are this report, current lifecycle notices and fresh ignored evidence/synthetic fixtures only. No production correction, historical review/log overwrite or B1 reopening. P17 remains FROZEN, P18 CLOSED, Phase 19/B3 unauthorized and indexing INACTIVE.

## Owner next action

**CHANGES_REQUESTED; B2 3/3 exhausted. Stop Antigravity remediation.** Owner must dispose of F01's exact native coverage/trust/scope gap and decide whether to authorize a separately bounded **`$codex-implementation-takeover`** for remaining approved corrections, especially F04's two residuals. Any takeover requires a separate independent subsequent review. This review authorizes no takeover, fourth round, publishing or new phase. No commit message while blockers remain.
