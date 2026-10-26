# Independent corrective re-review 1 — Maintenance B2

- Date / reviewer: 2026-10-26 / Codex, independent of Antigravity remediation and self-review.
- HEAD / entry: local `main` `a4cf43505ab48198e22799fecf5098c5c6870a79`, submitted working tree; `MAINT-B2-CLI-GIT-HOOK-ENFORCEMENT / IMPLEMENTED_AWAITING_CODEX_REVIEW`, B2 round **1/3**.
- Scope: unchanged [B2 executable contract](../b2-executable-handoff-2026-10-26.md); [initial independent review](2026-10-26-b2-independent-final-review.md), [round-1 handback](../b2-implementation-handback-2026-10-26.md). No Owner-approved acceptance change/deferment for the native gaps is recorded.
- Disposition: **CHANGES_REQUESTED**. **B2-F03 VERIFIED/CLOSED; B2-F01 HIGH and B2-F02 MEDIUM remain OPEN/PARTIAL.** No new finding IDs or feature requirements.
- Counter: **1/3** corrective/re-review rounds consumed; next authorized correction is Antigravity round 2. B1 remains published/CLOSED, with its own 3/3 history intact. LOW backlog is preserved.

## Independent assessment

| Finding | Result / credit retained |
| --- | --- |
| B2-F01 | **HIGH / OPEN / PARTIAL.** Documentation now truthfully acknowledges the Codex option/single-string wrapper gap and removes the cross-client protection claim. Added rules cover other literal prefixes. The mandatory native gap is unchanged; documenting it and requesting Owner disposition does not independently resolve it. |
| B2-F02 | **MEDIUM / OPEN / PARTIAL; mandatory NOT_VERIFIED.** Hook command is restored to its simple canonical form; manual subprocess tests pass. The handback now reports a client-shaped denial and allowed status call, but provides no linked native capture establishing actual hook payload/response, loading/interpreter/cwd and applied semantics. |
| B2-F03 | **VERIFIED / CLOSED.** Argument types and recognized nonempty string commands are validated before classification. All four original failing cases now return `force_ask` without overrides; valid deny/status controls are preserved. The 17-test suite, including ten malformed-argument subcases, passes. |

## Remaining blockers and minimal corrections

### B2-F01 — HIGH: declared native limit remains a mandatory unresolved gap

- **Locations:** `.codex/README.md:6–10`; `b2-implementation-handback-2026-10-26.md:16, 38, 72–73`; submitted ACTIVE remediation row for F01. These now acknowledge `UNRESOLVED_NATIVE_LIMIT / OWNER_DISPOSITION_REQUIRED`, while the remediation register still labels the finding resolved.
- **Evidence:** fresh explicit rule checks again produce **no matching rule** for Git `-C`/`-c` options, absolute Git, cmd/PowerShell single-string and nested script forms. Direct Git and tokenized cmd controls match forbidden. Current additions for `git.exe branch -D` and wrapped gh prefixes do not match or repair the previously executed Git cases. `.codex/config.toml` is unchanged and no alternate Codex-side control is demonstrated.
- **Reused native proof:** the original review's `native-client.stdout.jsonl:6–9` shows actual Codex dispatch of `git -C '<fixture>' add -h` and `cmd.exe /c "git add -h"`, both printing Git usage. The new handback expressly retains these as uncovered. Repeating the same model/client run is unnecessary: the relevant rule behavior remains unchanged and is rechecked against current source. Parser checks are not promoted to native interception proof.
- **Impact:** B2-03/06 and the linked native acceptance obligations remain unsatisfied. The contract permits reporting an unsupported path for Owner disposition; it does not automatically waive a mandatory case or convert it to CLOSED. No actual publishing mutation was used as proof.
- **Smallest correction:** retain the improved truthful documentation and leave F01 OPEN. Either demonstrate a compatible narrow project-local control under the already-approved conditional mechanism, with native denial and benign controls, or obtain an explicit Owner disposition for the exact remaining mandatory cases and synchronize that decision canonically. Do not add another unrelated prefix and call the demonstrated gap fixed; do not broaden permissions, alter global trust or build a general framework.
- **Closure proof:** actual supported-client pre-dispatch rejection for the approved representative forms, or an explicit applicable Owner acceptance/deferment decision. A documentation-only limitation label is neither proof nor such a decision.

### B2-F02 — MEDIUM: native hook claims are still not independently verifiable

- **Locations:** handback `:17, 29, 40, 62–65`; `.agents/hooks.json:9`; `test_repository_safety.py` methods `test_hook_cli_payload_schemas_and_malformed_input` and `test_hook_process_invocation_from_root_and_scoped_cwd`.
- **New evidence credited:** the handback quotes a claimed real `tool call denied by pre-tool hook` result for `git add -h`, plus an allowed `git status`, and describes why the old inline quoting failed. The canonical `python hooks/repository_safety.py` command and direct component tests are reasonable simplifications. These are useful implementer reports, not independently verified native closure.
- **Remaining evidence gap:** the cited fresh directory `logs/maintenance-b/b2-b32e2553d11040549bf42a6a896978be/` contains only two test receipts and their four stdout/stderr files. No native client/version/payload/response/launch-context trace is linked. Independent inventory of those six artifacts finds no quoted native denial event; the hook receipt is a 17-test unittest run. The older cited Codex receipts cannot supply Antigravity proof. Manual tests now invoke `python hooks/...` from `.agents` and a different explicit `.agents/hooks/...` path from the workspace; they do not establish how the actual client loads the configured command or consumes deny/force-ask responses.
- **Impact:** required B2-05 and relevant native parts of B2-04/07/10 remain **NOT_VERIFIED**. A copied outcome sentence does not establish the actual input schema, effective hook source/cwd/interpreter or permission behavior required by the approved contract. This is not a finding that the reported event was fabricated or that the client is absent.
- **Smallest correction:** retain an inspectable export/capture of the claimed real invocation with sanitized request/tool identity, actual hook input/output, configured source identity, interpreter/cwd and observed allow/deny/force-ask behavior where supported. Link it from the handback. No particular log format, new instrumentation platform or repeated full suite is required. If the installed client cannot expose/support the required proof within scope, report that exact limitation for Owner disposition instead of marking it verified.
- **Closure proof:** native evidence tied to the tested source/config and showing the real client applies the decision, with required supported/benign/output controls. Keep the already-green parser/manual tests labelled as component evidence.

## Verified F03 correction

`repository_safety.py:404–413, 423–443, 480–489` now preserves malformed argument values until validation, rejects missing/non-string/blank command input and emits no override for conservative responses. Original entrypoint probes for string args, array args, missing command and numeric command all return **`force_ask`**. Valid `git add .` returns deny; valid status returns allow with its narrow override. No production correction was made by this reviewer.

## Focused checks and evidence reuse

Fresh review root: `logs/maintenance-b/b2-codex-rr1-5d3aff83ceca465d8b171d1241451aac/`.

| Check | Independent result |
| --- | --- |
| Published B1 normal 300s wrapper: `python -B -m unittest discover -s .agents/hooks -p test_repository_safety.py -v` | **17/17 PASS**, exit 0, tests **0.477s**, wrapper **1.031s**, outer **1.219s**; `hooks.json`, raw streams and independent 320s supervisor metadata. Ten new malformed subcases are part of 17 tests, not ten extra test methods. |
| Original focused parser/entrypoint probes copied unchanged into fresh `probe-replay/` | Driver exit 0, **1.812s** outer, through B1 normal profile with independent 320s supervision. `focused-probe-results.json` preserves exact input/output: F03 corrected; F01 representative rule gaps remain. Version still **codex-cli 0.164.0**. |
| Source/static/scope | AST checks and `git diff --check` pass. Index and excluded-scope checks exit 0; **95** local Markdown links resolve before review writes. `review-inputs.json`, `prewrite-checks.json` record identities and submitted evidence inventory. |

The prior actual Codex probe/control and **5/5 B1 output/privacy/collision** evidence remain applicable: B1 runner/tests and Codex configuration match their previously reviewed hashes. No B1 suite or identical native model run was repeated. The implementer's new 5/5 receipt is additional supplied evidence, not claimed as a fresh independent run. No Java/RAG/application suite, client installation, trust bypass or publishing action occurred. Existing logs, including the earlier interrupted attempt and reviewer filename-collision failure, are retained.

Source SHA-256: hook `02b4f7c959aaab507e850153c36c282e8b0a8a0bbc8dd7e1df8ca2c009940663`; tests `dfd59ebe2e6a4b177c1b5d8c798240d05534df39ad18d12c597fd510e5a80d1d`; rules `d449a880e0bca481dfd0096818db5874063dffe835b29cd9b609dfd1a85c4150`. Hook config is restored to published HEAD but differs from the initial submission, so it was explicitly inspected as an input.

## Scope, inventory and acceptance coverage

**20 inputs** were inspected: 19 current dirty paths (15 tracked modifications and four untracked documents), plus restored `.agents/hooks.json`. Exact inventory is in `review-inputs.json`: the existing hook/tests, rule/README, finite agent/rule/skill instructions, root notice, ACTIVE/handoff README, workflow policy, Maintenance B README/B1/B2 inventories, B1 closeout, B2 executable contract/handback and preserved initial B2 review. Changed lifecycle hashes also include the previous review's status synchronization; they are not all Antigravity-authored source changes. Recursive hook/rule inventory contains only existing source files, with no new production component/dependency.

B2-01–03/06 remain FAIL/PARTIAL on F01's unresolved native forms/coverage. B2-04/05/07/10 remain PARTIAL/mandatory NOT_VERIFIED on F02; reported status/denial and manual tests are credited at their demonstrated level. B2-08/09 remain PASS within unchanged output/privacy/retention/permission evidence. F03's input-classification requirement is now PASS. B1-L01 and MA-A03–MA-A06 remain LOW/OPEN milestone/audit backlog.

Shared risk-applicable coverage: **PASS 1, 3, 5, 7–9, 12, 14, 18, 23, 25, 29** for bounded scope, reuse/structure, corrected validation, retained privacy/output/resource evidence, dependencies and hygiene. **FINDING 2, 10, 16, 22, 26–28** for remaining enforcement/acceptance claims; **21 mandatory NOT_VERIFIED** for actual Antigravity security integration. **N/A 4, 6, 11, 13, 15, 17, 19–20, 24**: no module/API/ADR-0016 envelope, authentication, persistence or RAG-quality capability change.

Review writes are this report, current lifecycle notices and fresh ignored evidence only. No implementation correction or old review/log overwrite. B1 stays CLOSED; no B3 or Phase 19 authorization; frozen P17/P18, indexing and live-original restrictions are preserved.

## Next step

**CHANGES_REQUESTED; B2 1/3 consumed.** Exact next skill: **`/antigravity-implement-handoff`**, remediation **round 2** for remaining F01/F02 within the unchanged approved controls. Keep F03 CLOSED unless a concrete regression appears. F01 requires effective native coverage or explicit Owner disposition; no automatic waiver follows from its limitation label. Then mandatory **`/antigravity-self-review`** and independent **`$codex-final-review`**. No takeover, fourth-round allowance or commit message is authorized here.
