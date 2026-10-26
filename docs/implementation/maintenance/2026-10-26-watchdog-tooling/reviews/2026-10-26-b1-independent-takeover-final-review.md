# Independent final review — Maintenance B1-F02 Codex takeover

- Date / reviewer: 2026-10-26 / Codex, reviewing independently of the prior Codex implementation session and self-review. This reviewing session made no source/test correction.
- HEAD / entry: local `main` `dfc0c2052ffd5272ba63e6529d0bb401f1e5c022`; submitted working tree; ACTIVE `IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED`.
- Scope / authority: Owner-authorized **B1-F02 only** takeover, [takeover handback](../b1-f02-codex-takeover-handback-2026-10-26.md), unchanged [B1 contract](../b1-executable-handoff-2026-10-26.md), [Owner policy](../../../../workflow/current-owner-delivery-policy.md) and [round-3 independent review](2026-10-26-b1-independent-re-review-3.md).
- Disposition: **READY_FOR_OWNER_COMMIT — B1 only**. **B1-F02 VERIFIED/CLOSED; no blocking findings remain.** F01/F03/F04/F05/F06/F07 retain their independent closures. B1-L01 and MA-A03–MA-A06 remain LOW/OPEN backlog.
- Counter: **3/3 Antigravity remediation rounds remain exhausted**. This is the separate review of the Owner-authorized Codex takeover, not a fourth Antigravity round or counter reset. Maintenance B overall is not complete; B2 remains separately gated.

## User requirement and simplest-mechanism check

The correction adds eight lines to the existing native enumeration helper, plus two focused regressions and their shared test helper. Immediate error capture distinguishes normal enumeration end from failure, rejects incomplete snapshots and reuses the existing `timed_call(..., default=None)` / `descendants_verified` gate. No new production helper, state framework, dependency, timeout, cleanup strategy, application capability or B2 configuration is introduced.

Independent entry-source hashes match the round-3 reviewed source identities. Reconstructed entry-relative diffs show only the submitted native error handling and tests. Accepted unchanged B1 evidence is reused from the [initial review](2026-10-26-b1-independent-final-review.md), [re-review 1](2026-10-26-b1-independent-re-review-1.md), [re-review 2](2026-10-26-b1-independent-re-review-2.md) and [re-review 3](2026-10-26-b1-independent-re-review-3.md); their historical findings and failed receipts remain preserved.

## Finding register

| ID | Severity | Blocking? | Independent evidence / disposition | Status |
| --- | --- | --- | --- | --- |
| B1-F02 | HIGH original | No — resolved | `scripts/run-bounded-command.py:112, 145–161` captures First/Next errors before snapshot closure. Failed enumeration propagates through the existing unavailable-cleanup/PASS gate. Fresh full focused runs and the unchanged original native-error probes now reject false success. | VERIFIED / CLOSED |
| B1-F01/F03/F04/F05/F06/F07 | Prior HIGH/MEDIUM, as recorded in dated reviews | No — resolved | Previous closures retained; only the eight-line native helper delta and added tests differ from the accepted round-3 source. All corresponding focused regressions pass again. Prior timing/host limits remain in force. | VERIFIED / CLOSED |
| B1-L01 | LOW | No | Existing duplicated status/attribution debt remains backlog. Additionally, takeover handback line 14 swaps the round-1 F01/F04 severities: the original review records F01 MEDIUM and F04 HIGH, not the reverse. Correct that summary from the immutable review during consolidated cleanup; aggregate counts/escalation eligibility are unaffected. | OPEN / BACKLOG |
| MA-A03–MA-A06 | LOW | No | Existing Maintenance A milestone/audit backlog is preserved, without promotion or implied closure. | OPEN / BACKLOG |

### B1-F02 closure proof

First/Next false returns now read the native last error immediately. Code 18 permits normal enumeration end; all other codes, including 0, raise before an empty/partial result can be certified. The snapshot still closes in `finally`, and the captured error survives closure. This matches the documented return/error distinction for [Process32First](https://learn.microsoft.com/en-us/windows/win32/api/tlhelp32/nf-tlhelp32-process32first) and [Process32Next](https://learn.microsoft.com/en-us/windows/win32/api/tlhelp32/nf-tlhelp32-process32next).

New tests `test_b1_29_f02_process32first_failure_not_pass` and `test_b1_30_f02_process32next_failure_not_pass` at `scripts/test_run_bounded_command.py:870–968` exercise both error boundaries with codes 5/0, normal completion with 18, real snapshot allocation/closure, error preservation through closure and a real zero-exit parent with an owned child. Next fails after real First success, so a partial snapshot is rejected. Fixture cleanup stops only its owned child and verifies it stopped.

Independent replay of the **unchanged** round-3 native probes produces these results for both First and Next errors: **exit 1 / COMMAND_FAILED / capture_complete=true / cleanup_complete=false / descendant_verification_unavailable**. The deliberately undiscoverable child is alive before reviewer finally cleanup; the wrapper reports inability truthfully rather than certifying containment. The slow-lookup case also rejects PASS. No new universal OS-containment requirement is imposed.

## Acceptance matrix

| Requirement | Result / evidence |
| --- | --- |
| B1-01 | PASS: ordinary zero exit, default 300s, complete evidence; native-unavailable false PASS repaired. |
| B1-02–03 | PASS: short deadline/TIMEOUT, profile/finite/cap guards; normal 300s and approved full-suite at most 2100s remain unchanged. No actual extended application command is authorized here. |
| B1-04–06 | PASS: attributable CPU/file/output progress, native WCT positive cycle and quiet/inconclusive behavior; silence alone remains insufficient hang proof. Prior bounded-diagnostic evidence retained. |
| B1-07 | PASS: child exit code retained, complete private stderr in fresh evidence, concise safe summaries. |
| B1-08–09 | PASS within recorded OS limits: native owned tree/sentinel, cancellation, read/setup/capture faults, unknown/timeout and now First/Next native errors. Unavailable cleanup fails safely. |
| B1-10–11 | PASS: exclusive evidence reservation/collisions, safe canaries, bounded chunked Unicode/large/live output; prior closures and fresh focused regressions agree. |
| B1-12 | PASS for approved short host-supervision proof; retain prior limits. No actual 2100s host-duration or universal child-containment claim. |

## Verification

Fresh reviewer evidence root: `logs/maintenance-b/b1-codex-takeover-independent-4dcc1ace72514103b2575bb48bb178e8/`. Supervisors and original probes were copied unchanged into new exclusive paths. Historical logs were not used as writable targets.

| Check | Independent result / evidence |
| --- | --- |
| Bootstrap: `python -B -m unittest discover -s scripts -p test_run_bounded_command.py -v`, independent 180s supervisor | **30/30 PASS**, exit 0, no skips; **26.564s** tests / **26.812s** outer. `bootstrap-supervisor.json`, raw stdout/stderr. |
| Approved final shape: `python -B scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <fresh>/review-final.json -- <resolved python> -B -m unittest discover -s scripts -p test_run_bounded_command.py -v`, independent 320s supervisor | **30/30 PASS**, exit 0, no skips; **26.589s** tests / **27.063s** wrapper / **27.250s** outer. `final-supervisor.json`, `review-final.json`, raw streams; capture/cleanup complete. |
| Original native-error/lookup-timeout probe replay, independent 90s supervisor | Driver exit 0, **3.219s** outer; all three cases reject false PASS. First/Next approximately 0.516/0.515s; lookup timeout 1.531s. `native-failures/native-failure-results.json`, individual receipts/console logs and unchanged script retained. Results, not driver exit alone, establish closure. |
| Source/scope/static checks | Independent reconstructed source/test diffs, AST parsing, `git diff --check`, index and excluded-scope checks pass. **125** local Markdown link occurrences resolve before review writes. Evidence directory is ignored. `review-inputs.json`, `prewrite-checks.json`, `*.independent.diff`. |

Runtime: `C:\msys64\ucrt64\bin\python.exe`, Python **3.11.7**, native Windows / `win32`. No outer timeout occurred. Runner SHA-256 `914d84a9633caf1c238caa25118358a205561a447cee7cb14ef22be66b316d23`; tests `7a74793e2e55a08a9bc1c5b2f0d30c6f11832fd2bcc5e4d847b34f56b8789c5c`.

Native allocation/handle closure and owned fixture lifetimes were actually exercised; error returns themselves are controlled fault injection. Existing native healthy/deadlock and descendant/sentinel cases also executed in the focused suite. This does not certify B2 Windows-client interception. Prior scaled timing, scheduling/native overhead and earlier-host-interruption limits remain explicit; no real 35-minute run, Java/RAG/application suite, warning-free IDE/compiler claim or full historical-log equality scan was performed.

## Scope and hygiene

The **27 submitted input paths** comprise 20 tracked modifications and seven untracked handoff/handback/prior-review documents; exact paths/hashes are in `review-inputs.json`. They include the two Python files; finite AGENTS/agent/rule/skill/workflow call sites listed in the B1 contract; ACTIVE/handoff README; maintenance README/B1 requirements; executable handoff, Antigravity handback, Codex takeover handback and four prior independent implementation reviews. This new report is a subsequent output.

Independent comparison against the takeover entry confirms submitted changes only to runner, tests and ACTIVE, plus the new takeover handback; earlier submitted instruction files/reviews are unchanged. Recursive `scripts/` inventory shows no added source package/helper/dependency. No application, `.codex`, hook, database, frozen Phase 17/18 or B2 change exists. Owner authorization, authorship and all three historical remediation rounds remain recorded.

Shared risk-applicable coverage: **PASS 1–3, 5, 7–10, 12, 14, 16, 18, 21–23, 25–27, 29**, substantiated by the scoped delta, native/focused regressions and reused accepted evidence; timing evidence is limited as stated above. **28 FINDING / LOW only** for B1-L01 and retained backlog. **N/A 4, 6, 11, 13, 15, 17, 19–20, 24**: no module/API/ADR-0016 envelope, authentication, persistence or RAG-quality change. No mandatory in-scope case remains unverified or blocking.

Review writes are limited to this report, canonical ACTIVE/maintenance README/B1 status and fresh ignored evidence. No implementation correction, staging or publishing. Phase 17 remains FROZEN, Phase 18 CLOSED, Phase 19 NOT AUTHORIZED, indexing INACTIVE and live originals DISABLED/DEFERRED.

## Owner next action

**READY_FOR_OWNER_COMMIT — B1 only.** Owner performs normal commit/push of B1. No additional staged-tree review is required. B2 still needs actual Owner publication of accepted B1 and its own Codex handoff; overall Maintenance B final review follows both published slices.

Suggested Conventional Commit: `fix(tooling): enforce progress-aware command bounds and safe cleanup`
