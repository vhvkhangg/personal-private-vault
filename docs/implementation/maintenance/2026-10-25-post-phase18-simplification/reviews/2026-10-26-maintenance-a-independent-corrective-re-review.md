# Maintenance A — bounded independent corrective re-review

- Date / reviewer: 2026-10-26 / Codex, independent reviewer of the supplied corrections.
- Authority: Owner-requested maintenance/governance corrective re-review using `codex-final-review` and the proportional shared coverage checklist.
- Baseline: local HEAD `217cb5d24bf7a62d7348d31838a7a79b01a524bd`, corrected working-tree files; index unchanged.
- Prior evidence: [2026-10-25 independent review](2026-10-25-maintenance-a-independent-final-review.md), preserved unchanged; raw SHA-256 `180360ffa8b827fe9cafb05d8f1ed5131c167a9ca8c44ac13c67575bd0a01bb5`.
- Disposition: **CHANGES_REQUESTED** — one remaining MEDIUM blocker, MA-A02. No new finding ID or implementation requirement is introduced.

## Finding closure

| Finding | Current result | Independent evidence |
| --- | --- | --- |
| MA-A01 / MEDIUM | VERIFIED / CLOSED | `.agents/skills/codex-create-handoff/SKILL.md:77` now selects the approved slice-specific risk-based verification gate. Full application suites require an explicit approved obligation or demonstrated integration/source-drift risk; focused checks otherwise suffice. Frozen and explicitly required phase gates are preserved. |
| MA-A02 / MEDIUM | PARTIALLY VERIFIED / OPEN / blocking | Maintenance entry, local status/report writes and documentation-owner remediation routing now exist. The implementation-maintenance readiness output still fails the existing handoff-entry status requirement; precise residual below. |

## MA-A02 residual — maintenance readiness does not satisfy handoff creation

- **Severity / disposition:** MEDIUM / OPEN / blocking.
- **Exact locations:** `.agents/skills/codex-pre-handoff-review/SKILL.md:77` and `.agents/skills/codex-create-handoff/SKILL.md:43–48`, specifically line 46. Approved maintenance workflow: `docs/workflow/owner-phase-workflow.md:9–10`.
- **Reproducible evidence:** for an explicitly Owner-approved maintenance scope whose next step is an implementation handoff, pre-handoff review instructs Codex to set the scope status to `READY_FOR_HANDOFF`. The handoff-creation skill requires that scope's status to explicitly say `APPROVED FOR HANDOFF`. Walking the two skills in sequence, with the other Owner-approval/scope/no-conflicting-handoff conditions satisfied, therefore stops at the status gate. A focused extraction/comparison returned `emitted=READY_FOR_HANDOFF`, `required=APPROVED FOR HANDOFF`, `equal=false`.
- **Impact:** the maintenance branch added to repair MA-A02 still needs an undocumented status change before its named next step can run. This affects a mandatory entry condition, rather than only the presentation of a review result. Documentation-only maintenance now routes correctly; that partial success does not resolve the whole maintenance route.
- **Simplest correction:** use one accepted readiness status consistently between producer and consumer, or explicitly recognize the emitted status in the existing creation gate. Retain the separate requirement for actual Owner scope approval. No extra approval session, new workflow artifact, phase activation or production change is needed.
- **Verification to close:** repeat the text-only status comparison and maintenance walkthrough. A separately approved implementation-maintenance preparation must reach the existing handoff-creation gate without an invented intermediate status transition; docs-only maintenance must still finish without making ACTIVE live. This walkthrough does not authorize Maintenance B.

The other MA-A02 corrections are verified: pre-handoff lines 29–33 exempt maintenance-only review from numbered-phase prerequisites; lines 69–87 place its records/status under the maintenance scope and route document corrections to ChatGPT. Final-review lines 173–176 distinguish documentation-only, active implementation, and phase-final/audit remediation; no empty ACTIVE or automatic takeover is required.

## Policy regression check

| Requirement | Result |
| --- | --- |
| Slice review; phase final; milestones after P19/P22; separate RAG and combined audits | PASS: current cadence retained in all three skill entrypoints and consistent with the [Owner policy](../../../../workflow/current-owner-delivery-policy.md). No phase is activated. |
| Antigravity self-review; three corrective rounds with independent re-review | PASS: preserved; final-review implementation routing still requires self-review, independent re-review and an Owner takeover decision after three blocked rounds. |
| CRITICAL/HIGH/MEDIUM block; LOW/INFO backlog | PASS: preserved. The four original LOW findings remain non-blocking. |
| Detailed handoffs, reuse/KISS/YAGNI, proportionate tests | PASS for MA-A01: no unconditional suite requirement; no weakening of mandatory frozen gates. |
| Maintenance entry/status/remediation routing | FINDING: MA-A02 residual above; documentation-only entry and correction routing PASS. |
| Owner-only Git; no staged-tree review; frozen phases and runtime boundaries | PASS: no publishing permission, new staging gate or reopened P17/18 contract. Current versus future watchdog limits remain explicit. Windows client interception remains NOT_VERIFIED; no runtime enforcement claim is upgraded. |

## Preserved milestone/audit backlog

The original evidence, severity and minimal remedies remain in the prior review. None is corrected, closed or promoted by this bounded re-review.

| ID | Severity / status | Retained subject |
| --- | --- | --- |
| MA-A03 | LOW / OPEN / non-blocking | Git versus ZIP delivery inventory and attribution of five tracked helper deletions. |
| MA-A04 | LOW / OPEN / non-blocking | Phase-final and legacy review-template result vocabulary. |
| MA-A05 | LOW / OPEN / non-blocking | Three-round takeover instruction still says “both” report paths. |
| MA-A06 | LOW / OPEN / non-blocking | Harness report overstates changed-rule propagation. |

## Focused verification and reused evidence

- Read all three corrected skills and compared their contracts with the current Owner policy, Owner phase workflow and existing handoff/review templates. Recursively inventoried their three folders: three `SKILL.md` files plus the two existing template assets; no added subtree is required by the correction.
- Checked all **9** relative Markdown link occurrences in the three corrected skills: **0 missing targets**. The maintenance status producer/consumer check found the mismatch recorded above.
- `git -c core.whitespace=cr-at-eol diff --check -- <three corrected skill paths>`: exit **0**. No application compiler or IDE diagnostics claim is made.
- `git diff --quiet HEAD -- <tracked hooks/config, watchdog/tests, backend, rag-engine, ACTIVE, P17/P18 records and D20 manifest>`: exit **0**. The unchanged tracked controls remain on the same baseline as the original independent review. `git diff --cached --quiet`: exit **0**.
- Reused the prior **13/13 hook** and **5/5 watchdog smoke** results for those unchanged sources. **No tests or application suites were rerun.** Prior policy-check results retain their original limits; automatic Windows-client interception and the new watchdog remain unverified/deferred.
- `logs/` was not written, cleaned, moved or truncated by this re-review. The prior full log digest is historical evidence from 2026-10-25; no fresh full-log hash scan or current byte-equality claim is made.
- The untracked `personal-private-vault.zip` is a pre-existing archive outside this bounded three-file correction review; it was neither opened nor modified. The earlier complete inventory remains historical, not an inventory of this later archive.

| Reviewed correction | Raw SHA-256 |
| --- | --- |
| `.agents/skills/codex-create-handoff/SKILL.md` | `23621d57f2eeef5906fce867c58359b237a69da55124732ac39822669cecc7d0` |
| `.agents/skills/codex-pre-handoff-review/SKILL.md` | `492ba899d61e53f20b8cf247cfba16088218d53a6fa2cf323825ea5e1ed06593` |
| `.agents/skills/codex-final-review/SKILL.md` | `b1e0e4c06cfe036916d5e640766a4b861fb00227fc939fc5a6734ec2ce03ca44` |

Shared coverage is inherited proportionately from the prior ledger: dimensions **1, 3, 5, 8, 14, 16, 22, 26, 27 PASS** for the applicable governance checks; **2 and 28 FINDING** for the remaining route inconsistency; **29 FINDING/non-blocking** for the preserved delivery backlog; **21 NOT_VERIFIED/deferred** for client-integrated enforcement. Dimensions **4, 6, 7, 9–13, 15, 17–20, 23–25 N/A** because no application/module/API/schema/model/dependency behavior changes. No frozen-phase verification is reopened.

## Review writes and next step

Only this dated re-review and the maintenance README's current status/link were written. The three corrected skills and the original review remain unchanged by the reviewer. ACTIVE remains `NO_ACTIVE_HANDOFF`; no staging, publishing, implementation, Maintenance B or Phase 19 work was performed or authorized.

**Next step:** ChatGPT should make the bounded MA-A02 status-contract correction, then return it for independent `$codex-final-review`. Keep MA-A03–MA-A06 in the milestone/audit backlog. No commit message is supplied while the MEDIUM blocker remains.
