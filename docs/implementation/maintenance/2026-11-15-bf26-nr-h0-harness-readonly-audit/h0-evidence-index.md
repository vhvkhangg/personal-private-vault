# BF26-NR-H0 evidence index

**Status: H0_REPORT_SUBMITTED / INDEPENDENT_REVIEW_PENDING**  
**Operator:** Codex H0 auditor / producer; session ID not exposed; no independent reviewer was run.  
**Baseline:** `7c521fb9998bc1ad0a86e7225aa605f0de17e261`, main.  
**Workspace:** `C:/Users/ADMIN/IdeaProjects/personal-private-vault`.  
**Receipt root:** `logs/bf26-nr/h0/20261116-h0-7c521fb9/`.  
**Owned fixture:** `<TEMP>/ppv-bf26-nr-h0-f1fa6d1f3ca84457b695511aa9428dcd`.

## Authority and record provenance

The controlling [Owner authorization](owner-authorization-h0.md), [handoff](h0-readonly-handoff.md), [coverage](h0-coverage-and-evidence.md) and [scenario protocol](h0-scenario-protocol.md) are published in the nine-file HEAD commit. G.1 records the dated ratification; G.2's publication/entry conditions were actually checked, rather than treating draft headings or empty ACTIVE as permission.

The single permitted network check returned the same SHA as local HEAD/origin/main. The approved origin was checked first. No fetch/pull, new remote query, package/provider/model request, installation or service launch followed. Remote main is qualified **at entry**, not claimed continuously monitored.

E00 and E10 are transparent **transcriptions of tool observations**, not raw native event logs. E01/E02/E03/E04/E05/E08/E11/E12 contain actual command-produced metadata. E06/E07 contain watchdog-generated receipts and preserved raw stdout/stderr. E09 is generated after all five reports are saved and contains their final hashes and final boundary checks.

Exact time of some entry/help/manual-read calls was not captured as a standalone receipt; do not infer it from later transcription creation times. File creation timestamps are not command start timestamps. The two executed test receipts contain authoritative UTC start/end/duration fields. Local inspection date was 2026-11-16 UTC+07; raw receipt collection began at 00:27 UTC. Tool interface versions for exec_command/apply_patch are not exposed.

## Evidence register

All relative receipt names below are under the receipt root. No existing evidence path was overwritten. Exact log/receipt paths passed git check-ignore (`.gitignore:54:logs/`) before first write. Stream paths were checked explicitly before the runner launched.

| ID | Receipt / source | Check and observed result | Applicability / limits |
|---|---|---|---|
| E00 | e00-entry-gates.json | HEAD/origin-main/remote-main match; main; entry tracked/untracked clean; 68,709 ignored inputs; ACTIVE empty; five outputs initially absent | Transcribed entry metadata; remote call tool chunk 659691, exit 0; published nine-file stat 25aabd |
| E01 | e01-inventory.json | Raw `git ls-tree -r --full-tree HEAD` strings for all 3,537 tracked paths/blobs | Enumeration only; manifest selects 254 paths, not a D0/B0 audit of all 3,537 |
| E02 | e02-reference-graph.json | 324 local Markdown links extracted from 137 harness files; zero missing file targets | File existence only; no section-anchor, external URL, every inline code path or native-discovery proof |
| E03 | e03-environment.json | Despite historical filename, contains **12 local ignored file paths/hashes only**, not global configuration | Eight bytecode caches + four local/historical AGENTS copies; no private log contents copied |
| E04 | e04-hook-cases.json | Seven text-only evaluator/adapter cases; direct add denied; branch delete option/wrapper/direct and alias text allowed | Fixture copy of hook, Python 3.12.3; no Git command executed; NRH0-F02 |
| E05 | e05-execpolicy-cases.json | Seven `codex execpolicy check` calls, exit 0; three forbidden / four no-match | codex-cli 0.169.0 parser with copied owner-git.rules; does not prove automatic policy loading or execution |
| E06 | e06-hook-unittest.json + .stdout.log + .stderr.log | 21/21 named unittest methods PASS; exit 0; capture and cleanup complete | Fresh fixture evaluator/adapter subprocess tests; not fresh model/native denied-command coverage |
| E07 | e07-runner-selected-tests.json + .stdout.log + .stderr.log | 6/6 selected watchdog methods PASS; exit 0; capture and cleanup complete | Native Windows owned-tree timeout/sentinel proof; cancellation uses injected KeyboardInterrupt with real child |
| E08 | e08-structure-analysis.json | SHA-256/size baseline of all 137 mandatory harness files before tests | Compared again at final handback; exact byte hashes complement clean Git diff |
| E09 | e09-final-integrity.json | Final five-report hashes, all receipt hashes, source/local/external comparisons, Git status/HEAD/cached diff and artifact validation | Final machine receipt; its own hash is not self-embedded |
| E10 | e10-diagnostic-rejection.txt | One combined here-string collector blocked pre-dispatch: ambiguous/unclosed quoting; fixture/receipt directory still empty at follow-up | No shell exit code, no collector run/retry/encoding/alternate launch; classifier root cause beyond observed rejection not established |
| E11 | e11-environment-redacted.json | User model preference gpt-6.1-sol / max, target project trusted, safe config hash; active full-access/never capabilities | No credentials/headers/config dump. Preference is not selected/served model or complete hook trust proof |
| E12 | e12-source-usage.json | Literal callers for all 43 skills; delivery ordinary directory exists, zero children, no tracked/ignore match; no harness producer found | Textual usage is not actual invocation; producer search absence bounded to inspected harness |

Additional source inspection is recorded by path, depth and reason in [the manifest](h0-file-manifest.csv). Console-only source/tool facts have tool receipts in this session: 918119 (safe config and execpolicy help), aef41a (CLI/Python version/help), 7b9272 (PowerShell/platform and tracked paths), 903dfc (redacted config/executable hash/IDE process metadata), b2eec3 (delivery/local instructions/test streams), 21bd5d (link/name summary), 9337f4 (V1 source applicability and manifest checks). These are not invented filesystem artifacts. Early combined console reads were truncated; material gates/source clauses were subsequently re-read in bounded slices or retained machine receipts.

## Exact diagnostic commands and cwd

From the actual PPV root, read-only entry:

```text
git remote get-url origin          # compared internally against the approved URL forms; no credentials printed
git branch --show-current
git rev-parse HEAD
git rev-parse refs/remotes/origin/main
git ls-remote --heads origin refs/heads/main
git --no-optional-locks status --porcelain=v1 --untracked-files=all --ignored
git show --format=fuller --stat --no-renames HEAD
git ls-tree -r --full-tree HEAD
git ls-files --others --ignored --exclude-standard
git check-ignore -v -- <each exact new receipt/stream path>
```

Only ls-remote contacted the approved remote. GIT_TERMINAL_PROMPT=0 was process-local for that command; no Git config or refs were updated. No forbidden staging/publication/destructive command was run, even in help form.

From the owned fixture, copied-source tests (no remote Git origin). Process-local PYTHONDONTWRITEBYTECODE=1 and TEMP/TMP point to the fixture scratch directory; PYTHONPATH points to fixture scripts for E07.

```text
python -B scripts/run-bounded-command.py --timeout-seconds 60 --receipt <PPV>/logs/bf26-nr/h0/20261116-h0-7c521fb9/e06-hook-unittest.json -- python -B -m unittest discover -s .agents/hooks -p test_repository_safety.py -v

python -B scripts/run-bounded-command.py --timeout-seconds 60 --receipt <PPV>/logs/bf26-nr/h0/20261116-h0-7c521fb9/e07-runner-selected-tests.json -- python -B -m unittest -v test_run_bounded_command.BoundedCommandTests.test_b1_01_ordinary_child_exit_zero_defaults test_run_bounded_command.BoundedCommandTests.test_b1_02_deadline_exceeded_timeout_124 test_run_bounded_command.BoundedCommandTests.test_b1_03_profile_guard_and_extended_cap test_run_bounded_command.BoundedCommandTests.test_b1_08_owned_descendants_killed_sentinel_survives test_run_bounded_command.BoundedCommandTests.test_b1_09_cancellation_and_launch_error test_run_bounded_command.BoundedCommandTests.test_b1_10_no_clobber_and_no_canary_leak
```

E04 invokes `python -B -c` to import **only the fixture hook** and serialize evaluate_command / build_codex_hook_output for the seven exact command texts retained in that JSON. These texts were data, never shell-dispatched.

E05 invokes the following parser command for each token list, at the fixture root:

```text
codex execpolicy check --rules .codex/rules/owner-git.rules -- git add H0_ONLY_TEXT
codex execpolicy check --rules .codex/rules/owner-git.rules -- git branch -D H0_ONLY_TEXT
codex execpolicy check --rules .codex/rules/owner-git.rules -- git -C H0_ONLY_TEXT branch -D H0_ONLY_TEXT
codex execpolicy check --rules .codex/rules/owner-git.rules -- powershell -Command "git branch -D H0_ONLY_TEXT"
codex execpolicy check --rules .codex/rules/owner-git.rules -- git status
codex execpolicy check --rules .codex/rules/owner-git.rules -- git -C H0_ONLY_TEXT add H0_ONLY_TEXT
codex execpolicy check --rules .codex/rules/owner-git.rules -- cmd /c git add H0_ONLY_TEXT
```

The prefix checker parsed these tokens; none of the described Git actions ran. `forbidden` and `matchedRules: []` are preserved without recasting no-match as allow.

Other passive diagnostics: `codex --version`, CLI / execpolicy / debug / prompt-input **help only**, `python -B --version`, `git --version`, `rg --version`; PowerShell metadata/hash/link extraction; exact current/historical-source reads; no `codex debug models` or actual prompt-input/model invocation. No unsafe CLI flags or config overrides were used.

## Versions and copied-source qualification

| Component | Observed value |
|---|---|
| Installed CLI | codex-cli 0.169.0 |
| Local CLI binary SHA-256 | CE1945EB440B5481ED1233EBAF32B7056EE7E4E452FA62FFE1CBFB26103CDEE0 |
| PowerShell | 7.7.0 Core |
| Windows NT build | Microsoft Windows 10.0.26200 (marketing edition not independently queried) |
| Python | 3.12.3, resolved installed Python312 python.exe |
| Git | 2.45.1.windows.1 |
| ripgrep | 15.2.0, revision e89fff89ac |
| IntelliJ process metadata | 263.7307.200.0-IU; presence only, no IDE enforcement or live integration check |
| Model | gpt-6.1-sol preference / reasoning max in user config; actual serving identity, entitlement and compatibility NOT_VERIFIED |
| Global config SHA-256 | 779a3228e7c0dd2b28a8aeceb4f907b2d82d87a36293ba78c8aa2ab742530535 |
| Global instruction SHA-256 | 89C6F9DFC9361FCF2214B5A980568F36BB5591F5D2673735BC8C05DDB3612B1D |

| Copied fixture file | Baseline SHA-256, identical to workspace at copy |
|---|---|
| .agents/hooks/repository_safety.py | 0D5E69641AD0B9D498A64D612F76AC066BB353D5902B258683860C2D85AD8A99 |
| .agents/hooks/test_repository_safety.py | 767E36EA258F0E53DC40B657E8CDE11670A95F9911271ADABBD8960AC487A5FC |
| scripts/run-bounded-command.py | 914D84A9633CAF1C238CAA25118358A205561A447CEE7CB14EF22BE66B316D23 |
| scripts/test_run_bounded_command.py | 7A74793E2E55A08A9BC1C5B2F0D30C6F11832FD2BCC5E4D847B34F56B8789C5C |
| .codex/rules/owner-git.rules | D449A880E0BCA481DFD0096818DB5874063DFFE835B29CD9B609DFD1A85C4150 |

No repository bytecode cache was used by copied probes. Fixture copies and local/external baseline hashes are rechecked in E09. Prior B1/B2 reviews were source/provenance references, not reused as fresh execution evidence; no historical accepted count was added to this run.

## Test timing and evidence accounting

| Receipt | UTC start -> finish | Result |
|---|---|---|
| E06 | 2026-11-16 00:29:19.075 -> 00:29:20.112 | PASS, exit 0, 1.047s; unittest 21 methods / 0.709s |
| E07 | 2026-11-16 00:29:31.201 -> 00:29:39.736 | PASS, exit 0, 8.547s; unittest 6 methods / 8.030s |

Both report capture_complete=true and cleanup_complete=true. The six runner methods assert inner TIMEOUT 124, cancellation 130, launch failure 127, profile refusal, no-clobber and owned-tree/sentinel semantics as applicable. The cancellation branch is deterministic injection, not a native Ctrl+C claim. The outer PASS means the methods' expectations passed, not that timeout/cancelled children completed successfully.

**Fresh accounting:** 27 distinct unittest methods, 7 evaluator/adapter data cases and 7 execpolicy parser cases. These are separate units. Scenario dispositions are 14 STATIC/qualified PASS, 2 FINDING, 1 NOT_VERIFIED, 3 N/A; do not sum them into a runtime test count. No H1-only scenario, old review test or historical source count is newly executed evidence.

## Receipt integrity

SHA-256 at report finalization; E09 retains the complete machine hash ledger, including itself excluded from its own input list.

| File | SHA-256 |
|---|---|
| e00-entry-gates.json | 0945348B45DAE05FE78FE8D30B1BB0BCC3C982B9D5345A984DCE1AB1532D42D5 |
| e01-inventory.json | 7FAF6BC7485C5A483D616876453BCB1D8C12371CA7F05BF32FFD2D1360D61600 |
| e02-reference-graph.json | C142E44940F6BC0062B2FDCF99A51E4B483F8173DF721B881912DAB2173D3D99 |
| e03-environment.json | A10ECDA1AF11ECD82D88CC71B8CD159F59D27C2B6122E19898A6E526E47DA83B |
| e04-hook-cases.json | 4262B605874D49E17B6CE5D87C443BE89A3406367D91537EC1AA061E132ADFF7 |
| e05-execpolicy-cases.json | 9F345E30953A1AF510DB7F7D81BFA630855B7077F1ECA5F19C2E9599637B7DB0 |
| e06-hook-unittest.json | 46095B9B97429BDA16D5382E7B7C299309B1FFEFB76430A13BE3EE995958AFB1 |
| e06-hook-unittest.stdout.log | E3B0C44298FC1C149AFBF4C8996FB92427AE41E4649B934CA495991B7852B855 |
| e06-hook-unittest.stderr.log | 55E30CAA2BF2562AA76029E8CBCD83EDA1E93530B1954B708D0423C1EE62EA76 |
| e07-runner-selected-tests.json | 73674BD311276899F6050C7F398E3D5D5718EEF6CF6562CA9678F72E4D74BE3A |
| e07-runner-selected-tests.stdout.log | D586070F275A2655B94FD8B0B9A7F5DAD4B3698376F53BE70407F1BF1BA05B36 |
| e07-runner-selected-tests.stderr.log | 5FEB6EFA269E3C3F9647ECEC146667C09D2747FEB5021717D1C305194D7C5E33 |
| e08-structure-analysis.json | F5D502BA4464080D0DA70D79D20998825006DFB72FFBC053544145F0955E16D4 |
| e10-diagnostic-rejection.txt | 732606600032965BCF266BE98707EE572C731CAF312E8FB67BB7E3A9B6FA3466 |
| e11-environment-redacted.json | 750A63D548186A2100AD3978382426BCDDE6BC7B14757AE0CFCF36D65392DF88 |
| e12-source-usage.json | 9A39FBBCDD29FE3E9D62F7F32637C9AF82E08584A1B796FD4A5C18421F016F4C |

The four peer reports' final hashes are also in E09; this index's own hash is stored there to avoid a self-reference cycle.

## Output boundary and unverified checks

Authorized report set:

1. [Audit report](h0-audit-report.md)
2. [File manifest](h0-file-manifest.csv)
3. [Scenario results](h0-scenario-results.md)
4. [H1 proposal](h0-h1-proposal.md)
5. This evidence index

Final checks require exactly these five untracked report files, no tracked/cached diff, unchanged HEAD/origin-main, unchanged 137 harness hashes, unchanged 12 local ignored hashes and inspected external files, matching fixture copies, and unchanged empty delivery directory. E09 records actual results after these files are saved. The Git index's **staged content** is checked; no pre-entry index-file byte hash was captured, so no claim about Git's incidental stat-cache bytes is made. No agent Git stage/history/publication operation occurred.

Remaining NOT_VERIFIED: actual model/entitlement; exact serving binary; full native hook trust/load and tool schema; root/scoped startup instruction provenance/cap; missing/untrusted-hook fail-closed behavior; arbitrary aliases/encodings/MCP/GUI; full watchdog suite/endurance/real deadlock/native Ctrl+C; historical delivery producer or ZIP contents. Future H-T07/H-T09 require a rewritten skill and separate H1 authorization/reviewer. No application suite, production/provider readiness, Owner-data suitability, H1, D0 or B0 acceptance is implied.

Next step: separate Codex H0 independent reviewer inspects all five reports and selected raw receipts, reporting in chat unless a separate review-file write grant is supplied. Owner decides a bounded H1 contract afterward.

