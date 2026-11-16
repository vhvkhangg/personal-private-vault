# BF26-NR-H1 — Proposed Codex-only executable harness handoff (not yet active)

**Status:** `DRAFT / H1_EXECUTION_NOT_AUTHORIZED`.

## 0. Entry gates — all mandatory

1. H0 independent assessment acceptance is preserved with original reviewer provenance; five H0 reports and evidence are **Owner-published** on `main`, actual SHA verified.
2. This H1 preparation has an independent docs-only preflight verdict `READY_FOR_OWNER_RATIFICATION` with all required criteria evidenced, then an **explicit dated Owner ratification** selecting the 17-path list and all execution boundaries; executable approval published to `main` with actual commit SHA.
3. New **Codex H1 implementer** session is distinct from H0 producer, H0 reviewer and future H1 independent reviewer. No Antigravity involvement in this audit.
4. Read current `HEAD`, `origin/main`, permitted exact remote metadata under separately granted network rule, `ACTIVE`, tracked/index/untracked/ignored inputs, complete Owner contract and current delivery policy. **Do not infer that H0's remote network exception automatically carries into H1.** If final H1 authorization does not permit remote check, record `NOT_VERIFIED` and STOP whenever remote qualification is a mandatory entry gate; do not silently fetch/pull. Codex model-service calls for test scenarios also require their **own express H1 permission** and shall never be inferred from routine use of Codex.
5. Require clean/qualified working tree except explicitly planned H1 artifacts; verify no conflicting active handoff; inspect every allowed path and any existing output file before write.
6. If any authority, baseline, tool/sandbox/trust, path, output, fixture or missing receipt is ambiguous, `STOP / OWNER_DECISION_REQUIRED` without editing.

## 1. Implementation task — one bounded slice, no unrelated cleanup

### F01 — Current-only audit skill and reachable routing

- Rewrite the **existing** `$codex-backend-audit` skill (keep its identity/name) for current Non-RAG phases **H/D/B/X**, with explicit phase, Owner grant, source SHA, session role, output and read/write boundary checks.
- Make `phase-15` explicitly `STOP / HISTORICAL_ONLY`, referencing immutable historical records; do **not** execute any Phase 15 audit or create a parallel skill.
- Adapt the skill-local report asset, old launcher consumers, current audit resume and navigation. Preserve ordinary Antigravity workflows outside the audit; Codex-only author/reviewer roles are an **audit-specific exception**, not an ecosystem rewrite.
- Keep `ACTIVE=NO_ACTIVE_HANDOFF` valid for H0/D0/B0 read-only audit. No generic handoff creation may silently activate execution from this register; accepted old V1 scope cannot reopen.
- If an unallowlisted shared COMMON/template rule is actually blocking, STOP and request a minimal scope expansion rather than edit it.

### F02 — Branch-deletion protection gap

- Make the existing semantic evaluator identify actual Git **branch deletion** forms (`branch -d`, `-D`, `--delete`, supported safe parsing of `git -C` / `git -c`, absolute Git executable paths, `cmd /c` or PowerShell string wrappers, including allowed argument-boundary arrangements). Rejection must happen **before** broad permission overrides; branch list/show/status must remain valid.
- Add deterministic existing-hook-unit tests for denied and benign cases, malformed/ambiguous inputs, adapter outputs, and non-Git text. Do not promise coverage for unsupported arbitrary aliases/encoded scripts/MCP/GUI without proof.
- Keep `.codex/rules/owner-git.rules` unchanged initially; no claiming prefix parser alone provides full protection. Never run destructive Git operations in the Owner checkout; command texts are test data, not actions.

### F03 — Narrow guidance corrections

- Replace stale **active/current** execution notices and incorrect B1/B2 published tooling status with current canonical links and a compact audit exception.
- Preserve dates and text of explicitly labeled historical checkpoints, frozen contracts and previous counters. No global rewrite of all `AGENTS.md`, no stylistic simplification quota.

## 2. Verification contract

- H-T07: legacy Phase 15 invocation must STOP without report/status/ACTIVE mutation. A static read is not a native fail-closed model test.
- H-T09: authorized and negative current-only H/D/B/X route combinations (no grant, wrong phase, conflicting approvals, revoked/stale decision, wrong baseline, wrong role, wrong output, self-acceptance) must be qualified on the actual selected runtime **within an expressly Owner-approved test and Codex-service network boundary**, using only disposable fixtures. Without this separate grant, static reasoning may be recorded but **H-T09 native acceptance remains NOT_VERIFIED and H1 cannot claim full G2 readiness**. No model-traffic authorization is inferred from this draft.
- H-T01–06, 10–11, 16–18: preserve no-false-stop scope discovery, ordinary non-audit routing, reviewer independence, interrupted recovery and saved counters.
- F02: full relevant existing hook suite plus newly added deletion deny/control cases and `codex execpolicy check` as **parser evidence only**; native pre-dispatch loading/trust remains separately qualified or `NOT_VERIFIED`.
- Link/caller graph and exact changed-source hashes; no historical status/Phase 15 mutation; no unapproved path. Re-run focused runner tests only if a runner or its integration truly changes. No Maven full suite or live providers.
- Record actual commands, versions, exit codes, input strings, receipts, safe environment and outcome for every test; do not report static analysis as live model success. A real unsafe runtime gap prevents certification of protection; STOP for Owner if mandatory.

## 3. Write/output and environment boundary

Edit **only** the 17 exact existing files after final Owner grant in [allowlist](h1-exact-path-allowlist.md). New handback, self-review and evidence-index documents are limited to the three proposed names in §D of that file, once included in the ratification. Any new tests are added **within** the existing `.agents/hooks/test_repository_safety.py`, not in additional files without approval. Fresh diagnostic receipts only under separately approved ignored path after checking ignore/no-clobber.

Keep `.codex/config.toml`, global settings, current execpolicy source, `delivery/`, phases, schema, product source, ignored existing logs and all unrelated current/history documents untouched. Never stage/commit/push/tag, create PR, create branches/worktrees, delete/clean/stash/reset/checkout, access live providers or install software. No auto-ZIP/review packaging.

## 4. Author handback → different independent reviewer

H1 author returns `IMPLEMENTED_AWAITING_INDEPENDENT_REVIEW` with exact 17-path-or-subset change manifest, no-clobber new outputs, evidence ledger and unresolved checks. Self-review is necessary but **not** independent acceptance.

In a **new** Codex session, an independent H1 reviewer checks actual diff, granted allowlist, scenario/fixture proofs and runtime unknowns, with findings `H1-FR##`; returns `H1_ACCEPTED / READY_FOR_OWNER_COMMIT`, `H1_CHANGES_REQUESTED` or `BLOCKED_H1_REVIEW`. Reviewer may write a report file only on its own explicitly approved path; otherwise chat only. If changes are requested, each correction and its re-review consumes one authorized H1 round; after the ratified ceiling or an unsafe/non-fixable defect, STOP for Owner. Owner alone commits/pushes accepted changes and verifies actual SHA. H1 Owner publication **does not auto-authorize D0/B0**.
