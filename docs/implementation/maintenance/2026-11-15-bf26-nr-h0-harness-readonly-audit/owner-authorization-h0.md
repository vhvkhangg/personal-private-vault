# BF26-NR-H0 — Owner-ratified authorization for read-only harness audit

**ID:** `BF26-NR-H0-HARNESS-READONLY`
**Current status:** `OWNER_RATIFIED_2026-11-15 / PENDING_OWNER_PUBLICATION / H0_EXECUTION_NOT_AUTHORIZED`
**Basis:** Owner requested preparation after publication of `BF26-NR-AUDIT-PREP`, received independent H0 docs-only preflight verdict `READY_FOR_OWNER_RATIFICATION` with `H0-PH01` CLOSED, and explicitly directed ratification of this bounded H0 read-only contract on **2026-11-15** (Asia/Ho_Chi_Minh). Ratification **is not execution activation**; publication and entry gates in §G remain mandatory.

## A. Ratified scope (effective only after activation)

Audit the entire current Codex/agent **harness** and produce a falsifiable, evidence-backed report sufficient for an independent Codex reviewer and a subsequent Owner decision on the **mandatory H1 skill rewrite** and any warranted related harness changes. Audit only; do not implement remedies.

The root Owner requirement is: assess harness first, then docs/design, then Non-RAG Backend, finally cross-layer reconciliation; use **Codex-only** distinct author/reviewer sessions; ultimately rewrite **the existing** `.agents/skills/codex-backend-audit/SKILL.md` for current Non-RAG without retaining runnable Phase 15 mode. Preserve Phase 15 historical evidence, all historical counters and accepted decisions. H0 is the **bootstrap** and must not rely on that still-legacy skill for authority.

## B. Read scope: complete harness with justified adjacencies

Mandatory enumerate and inspect:

- `.agents/**` including all skills/assets/agents/rules/hooks/tests; `.codex/**` including project config and execpolicy rules.
- **Every tracked** `AGENTS.md` by exact basename, including root, backend, scoped modules and docs. Inspect any additional untracked/local files separately with provenance and privacy safeguards; do not silently add them to the published manifest.
- Harness-related `scripts/**`, `docs/workflow/**`, affected templates, relevant `docs/implementation/maintenance/**` and historical governance/architecture sources **only as needed for H0 claim verification**. Do not turn this into the entire D0 documentation audit.
- Relevant `.github/**`, repository tool configuration and CI only where an actual dependency connects them to harness behavior; include external/global Codex configuration **only read-only and redacted**. Do not print/write credentials, MCP tokens, cookies, machine-specific secrets or private contents.
- Investigate whether `delivery/` exists on the actual Owner checkout, whether it is tracked/ignored/untracked, which code/skill/script produces artifacts, what it packages and whether ZIP creation is triggered by Codex review or merely a manual step. **Do not assume existence or auto-generation.**
- The actual Windows/PowerShell/IDE/Codex CLI environment where available. Model `GPT-6.1 Sol` selection, effective config/hooks, trust and launch behavior require **observed evidence**; otherwise `NOT_VERIFIED`.

Starting remote baseline is [`b5a51d40887017b50817adaf037207839d9959ec`](https://github.com/vhvkhangg/personal-private-vault/commit/b5a51d40887017b50817adaf037207839d9959ec). Counts from the **previous** baseline (82 `.agents` blobs, 3 `.codex`, 43 SKILL.md and 22 AGENTS.md) are only a **starting hypothesis**; H0 must regenerate a manifest at its exact authorized SHA. The Owner reports removed temporary PH-02 evidence; H0 checks actual local state, not the report alone.

## C. Allowed actions **only after activation**

- Read repository files, inspect Git metadata without modifying it, compute file and executable/config hashes, analyze instructions and references, read tool versions and **redacted** effective configuration.
- Run passive, bounded, **otherwise no-network**, no-install read-only checks and isolated **disposable fixture** routing tests conforming to [scenario protocol](h0-scenario-protocol.md). **Sole network exception for the mandatory baseline entry gate:** from the actual PPV checkout, after checking that `origin` resolves to the approved `vhvkhangg/personal-private-vault` repository, run only `git ls-remote --heads origin refs/heads/main` to read the published `main` ref. This command must not update local refs, download repository objects, prompt for or disclose credentials, contact an unrelated remote, or be used for any other network purpose. No `git fetch`, `git pull`, package download or provider probing is authorized. If this exact read-only remote check is unavailable, fails, or cannot be trusted, record the evidence and **STOP / BLOCKED_H0**; do not infer remote state from `origin/main` or silently skip the gate. Fixture writes only in an explicitly identified temporary test directory outside the repository, never to real Owner state or global trust. Do not run any destructive command in the actual checkout, even as a test of denial.
- Write **new H0 artifacts only** at these **exact paths under this H0 directory** after verifying they do not exist or obtaining Owner-authorized revision permission:
  1. `h0-audit-report.md` — source-grounded findings and coverage conclusion;
  2. `h0-file-manifest.csv` — exhaustive inventory + depth/disposition per applicable path;
  3. `h0-scenario-results.md` — H-T01–20 attempted inputs, actual evidence and statuses;
  4. `h0-h1-proposal.md` — proposed KEEP/SIMPLIFY/MERGE/REMOVE and bounded H1 candidate paths, not an approved H1 handoff;
  5. `h0-evidence-index.md` — receipts, SHA, command version, factual limitations.
- Optional **new uniquely named** ignored receipts under `logs/bf26-nr/h0/<run-id>/` only **after** `git check-ignore` confirms the path and log retention conventions are satisfied. Otherwise return evidence in the allowed report or chat; no new `.gitignore` edit. Do not overwrite existing logs.
- H0 independent reviewer may write a **new** review report under `reviews/<date>-h0-independent-review.md` **only with a separate explicit review write grant**; otherwise report in chat. That reviewer is not the H0 producer.

## D. Prohibited actions

No changes to `.agents/`, `.codex/`, `AGENTS.md`, hooks, scripts, `docs/workflow/`, current policy, `ACTIVE.md`, Java/POM/test/source, database, RAG, Frontend, frozen/archival docs, git index/history, global Codex config/trust or actual user data. No `git add/commit/push/tag`, PR/release publication, `reset/clean/checkout/stash`, branch/worktree creation, package installs, dependency updates, Maven full suite, production service startup, Docker changes, **network requests other than the single read-only `git ls-remote --heads origin refs/heads/main` entry-gate exception in §C**, external provider probing, unsafe shell wrappers, secrets copying, auto-ZIP generation or unapproved artifact export. Do not automatically create a `delivery/` folder or drop archives there.

H0 may **propose** (not enact) changes to sandbox/approval policy, overlong AGENTS, skill dispatch, hook denials, `delivery/` packaging, Codex-only routing, etc. Proposed H1 allowlist is evidence-based and **not executable** until the Owner separately ratifies exact paths, behavior and verification.

## E. Baseline & protection gate

Before executing, verify locally `HEAD`, `origin/main`, remote `main`, `git status` including all untracked/ignored inputs affecting execution and current `ACTIVE`. A newer SHA requires comparing scope-relevant drift; material drift or an unrelated active implementation stops H0 for Owner decision. Do not demand an exact older SHA after a valid Owner publication of this H0 document. Never pretend a clean tracked diff proves untracked state is safe.

Instruction conflicts must be resolved from **the explicit newer scoped Owner H0 authority** without silently rewriting current policies. If the existing CLI/hook blocks a safe diagnostic, record `NOT_VERIFIED` or request permission, **do not bypass** it. If the current session cannot preserve the stated write boundary because of effective tools/sandbox, STOP.

## F. Reporting and acceptance

H0 must cover H01–H13 and H-T01–H-T20 with `PASS / FINDING / NOT_VERIFIED / N/A`, justified file-level read depth, safe test receipts where executed, finding IDs `NRH0-F##`, and `H0_REPORT_SUBMITTED / INDEPENDENT_REVIEW_PENDING` or `BLOCKED_H0`. **This is not `NON_RAG_BACKEND_AUDIT_READY`.** H0 does not establish a future current audit skill mode or accept H1 changes. Independent review and Owner H1 decision follow later.

## G. Activation — Owner ratification recorded; publication and entry gates pending

### G.1. Canonical Owner ratification record — 2026-11-15 (Asia/Ho_Chi_Minh)

- **Decision:** `OWNER_APPROVED / OWNER_RATIFIED` for **`BF26-NR-H0-HARNESS-READONLY`**, recorded at the Owner's explicit direction after the independent docs-only preflight verdict `READY_FOR_OWNER_RATIFICATION` and closure of `H0-PH01` (MEDIUM). The approval is recorded in this file for Owner publication; it is **not** a Codex self-approval or a claim that an H0 audit occurred.
- **Exact authorized phase:** **H0 harness read-only audit only**, as bounded by §§A–F, the published preparation contract and the accompanying `h0-readonly-handoff.md`. Enumerate/inspect the specified harness and perform only permitted passive checks and conditional disposable-fixture scenarios; create only the five named H0 reports and optional permitted ignored receipts. The **single read-only remote-ref check** in §C is the sole network exception.
- **Exclusions explicitly reaffirmed:** No H1 remediation or skill rewrite, no D0/B0, no Backend/RAG/Frontend modification, no existing file edits, no provider/network operations beyond §C, no `delivery/` auto-ZIP, and no agent Git stage/commit/push/PR. The restrictions of §D are unchanged. H0 findings may **propose** H1 changes but cannot authorize them.
- **Evidence basis:** The Owner-provided independent re-review reported `HP-01–HP-10 = PASS`, `H0-PH01 = VERIFIED/CLOSED`, and `READY_FOR_OWNER_RATIFICATION / H0 DOCS ONLY`. This ratification records that decision; it does not re-characterize documentation review as runtime/model/hook test evidence.
- **Scope effect:** This is approval of the **already reviewed contract without broadening permissions**. Any material amendment after this record requires a separate Owner decision and independent re-review before execution.

### G.2. Mandatory activation checklist

1. **Preflight review:** `READY_FOR_OWNER_RATIFICATION` reported for the final H0 documents with no blocking finding — **SATISFIED per Owner-provided independent review result**.
2. **Dated Owner ratification:** The explicit decision above approves `BF26-NR-H0-HARNESS-READONLY` **for H0 read-only only** on **2026-11-15** — **RECORDED**. This record becomes canonical for the executor only after it appears in the published repository.
3. **Owner publication — PENDING:** The Owner must stage/commit/push the **complete reviewed nine-file H0 package**, including this ratification, to `main`; verify and provide the **actual published commit SHA**. The H0 executor must independently check that remote and local `main` contain this precise ratification and handoff. If publication modifies substantive scope or permissions, require another independent preflight review.
4. **Executor entry gate — PENDING:** In a **new, separate Codex H0 auditor session**, verify `ACTIVE = NO_ACTIVE_HANDOFF`, no competing newer authorization, clean/qualified source and output-path conditions, and the approved remote `main` using only the §C command. On uncertainty, material drift or inability to enforce read-only boundaries, **STOP / BLOCKED_H0**.

**Until steps 3–4 are satisfied: `H0_EXECUTION_NOT_AUTHORIZED / STOP`.** No review result, ratification note, ZIP or empty `ACTIVE` register may be treated as an automatic audit launch. This record does not authorize H1, D0, B0 or any remediation.
