# BF26-NR-H0 — Bounded read-only audit handoff (future executable)

**Handoff type:** `READ_ONLY_AUDIT_BOOTSTRAP`, **not** an implementation/executable source handoff.
**Present state:** `DRAFT / WAITING_EXACT_OWNER_H0_GRANT`.
**Executor:** new Codex **H0 auditor** session; do **not** use Antigravity.
**Review:** separate Codex **H0 independent reviewer** session.
**Canonical proposed authority:** [Owner authorization](owner-authorization-h0.md). If it remains draft/unpublished, STOP.

## 0. Entry gates (all mandatory)

1. Verify publication and explicit ratification of `BF26-NR-H0-HARNESS-READONLY` on actual `main`. This is not satisfied by the general `BF26-NR-AUDIT-PREP` publication or this handoff draft.
2. Inspect actual `HEAD`, `origin/main`, tracked/index/untracked state, `ACTIVE.md`, current Owner policy and last superseding decisions. For **remote `main` only**, first verify that `origin` points to the approved `vhvkhangg/personal-private-vault` repository, then run the one permitted network metadata command `git ls-remote --heads origin refs/heads/main` from the PPV checkout; compare its returned SHA to local `HEAD` and `origin/main`. It reads the remote ref without updating local refs or downloading objects. If the remote check fails, returns no verifiable SHA or requires unapproved access, **STOP / BLOCKED_H0** (do not substitute cached `origin/main`). STOP for material drift, conflicting active handoff or uncertain approval; do not reset/stash/checkout anything.
3. Read entire [parent Owner contract](../2026-11-15-bf26-nr-audit-preparation/owner-contract.md), [parent harness plan](../2026-11-15-bf26-nr-audit-preparation/harness-audit-plan.md), [H0 approval](owner-authorization-h0.md), [coverage](h0-coverage-and-evidence.md) and [scenarios](h0-scenario-protocol.md). Read existing legacy `.agents/skills/codex-backend-audit/SKILL.md` as **evidence**, **do not invoke it**. `ACTIVE` remaining empty is normal for read-only H0.
4. Confirm output files do not exist; if any does, reconcile exact content/history and request scoped edit permission rather than overwriting. For ignored receipts, check `git check-ignore` for the precise path before writing.
5. Confirm Codex runtime/tool permissions can honor read-only boundaries. Do not change approval/sandbox/hook/trust settings to make checks run; if unsafe, STOP. Recording missing runtime facts as `NOT_VERIFIED` is acceptable where the contract permits.

## 1. Inventory and source-of-truth reconstruction

- Build `git ls-tree -r` file/blob manifest for *all* tracked `.agents/**`, `.codex/**`, exact-basename `AGENTS.md`, relevant `scripts/**`, links from `docs/workflow/**`, relevant `.github/**` and current status docs. Include local untracked/ignored harness-affecting artifacts in a **separate provenance tier**. Do not open secrets or publish real credentials.
- Per file: `path`, `blob SHA`, `tracked/untracked/ignored`, `purpose`, `owner/caller`, `read-depth (FULL/RISK_TARGETED/STATIC_METADATA)`, `reason`, `upstream/downstream refs`, `status`, `finding IDs`. All in-scope tracked paths accounted for; not all must be deep-reviewed, but all high-risk policy and skill-execution files need full or risk-targeted reading.
- Trace `root/scoped AGENTS -> rules -> skills -> referenced docs -> command invocation -> scripts/hooks -> status/receipts`, including case-sensitive path/cwd behavior. Distinguish outdated dated banners from current Owner authority. Model a clear policy precedence and handle contradictions without silently changing anything.

## 2. Evidence-driven harness review

Evaluate all H01–H13 [coverage entries](h0-coverage-and-evidence.md): correctness of skill discovery/trigger and duplicate instructions, historical Phase 15 misrouting, known `codex-create-handoff` false-stop failure mode, Codex-only remediation routing, self-review/independent review separation, safety hook/execpolicy effectiveness and limits, watchdog/tests, root AGENTS load/cap, CLI configuration and supported model, documentation/resume compatibility, and locally observed `delivery/` ZIP behavior.

Classify each existing skill/rule as `KEEP / SIMPLIFY / MERGE / DEPRECATE / REMOVE / NOT_VERIFIED` with **reason**, active users/references, expected benefit, compatibility risk and suggested regression. Do not create or rewrite any skill in H0. Current `codex-backend-audit` replacement is a **mandatory future H1 scope proposal**, not a H0 edit.

## 3. Scenario execution and safety

- Evaluate H-T01–H-T20 using [protocol](h0-scenario-protocol.md). Begin with **static/logic qualification**; perform live CLI/hook probes only where feasible and safe in a disposable fixture outside Owner repository, under exact Owner-permitted diagnostic limits. Do not treat a static read as a verified runtime enforcement test.
- Record for each: input/fixture, tool/version, expected behavior, observed behavior, affected permissions, exact result and evidence path. A model/CLI/runtime not installed/available or untrusted hook is `NOT_VERIFIED`; do not invent model behavior.
- Do not run `git add/commit/push` or destructive variants against the real PPV checkout, even with `-h`; fixture tests must never publish or contact a real remote. **Only** the §0(2) read-only `git ls-remote --heads origin refs/heads/main` baseline verification may contact the approved Git remote. No other network calls, `fetch`/`pull`, provider access, installation, Docker/DB service mutation or production data access.
- Only scoped new report files and safe temporary fixtures/ignored receipts may be written, as allowed in [Owner authorization](owner-authorization-h0.md) §C. If an existing hook rejects a read-only check, preserve the rejection as evidence and do not bypass it.

## 4. Report and H1 handback

Produce exactly the permitted audit artifacts: `h0-audit-report.md`, `h0-file-manifest.csv`, `h0-scenario-results.md`, `h0-h1-proposal.md`, `h0-evidence-index.md`. State missing artifacts explicitly if blocked; no empty fake reports. Propose a **complete prospective H1 change graph**, including existing audit skill, any demonstrably affected review/handoff skills, AGENTS, `.codex`, hooks, scripts, workflow/resume and `delivery/` only if observed and justified. Show **minimal** fix, alternatives, regression tests, source paths and Owner decisions needed; do not guess all these files must change.

Finding ID pattern `NRH0-F01...`, severity and evidence as in [parent findings contract](../2026-11-15-bf26-nr-audit-preparation/findings-and-remediation-contract.md). Preserve all prior accepted evidence and historical counters. No auto-authorized H1 allowlist or skill rewrite.

H0 ends as `H0_REPORT_SUBMITTED / INDEPENDENT_REVIEW_PENDING`, `BLOCKED_H0` or `OWNER_DECISION_REQUIRED` with actual evidence; not `H1_READY`, `BACKEND_AUDIT_READY` or final audit success. Another Codex session challenges all H0 conclusions; then Owner decides an exact, finite H1 change contract.

## 5. Disallowed shortcuts

- Do not call existing `$codex-backend-audit` in Phase 15 mode or pretend future mode already exists.
- Do not call `$codex-create-handoff` to force `ACTIVE` to a new state just because H0 is called a handoff.
- Do not implement any proposed change, even an obvious root AGENTS compression or a suspected `delivery/` packaging script repair.
- Do not mark all H-T probes PASS from reading files, or claim implicit safety because a hook exists.
- Do not treat clean `git diff` as evidence that ignored/untracked inputs were examined.
- Do not claim a review ZIP implies independent acceptance or publication.

**Next step:** separate Codex **H0 independent review** of exact report/evidence. After accepted H0 and Owner decision, H1 authoring may be separately commissioned. This H0 document itself grants no H1 permissions.
