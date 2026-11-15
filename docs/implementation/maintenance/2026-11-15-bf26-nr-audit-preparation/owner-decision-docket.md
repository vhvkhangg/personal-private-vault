# Owner decisions — ratified direction vs future gated choices

**This docket does not solicit decisions already made.** Preparation must not stall on questions whose answers appear in the Owner direction. Execution decisions are deliberately deferred; no default value is permission.

## Already established for planning (no need to re-ask)

| ID | Owner direction | Disposition |
|---|---|---|
| OD-01 | Audit entire harness, Docs and Non-RAG Backend in that order; final cross-layer reconciliation | `OWNER_DIRECTED_FOR_PREPARATION` |
| OD-02 | Codex for review and implementation; separate sessions, no Antigravity in this audit | `OWNER_DIRECTED_FOR_PREPARATION` |
| OD-03 | **Rewrite/replace the content of the existing** `codex-backend-audit` skill for current comprehensive Non-RAG audit; no parallel Phase 15 runnable mode and **no new skill**. Explicitly supersedes only the 2026-11-01 keep-Phase-15-mode directive; historical records stay immutable | `SUPERSEDING_OWNER_DECISION / PREPARATION_DIRECTION` |
| OD-04 | Prefer simplest adequate mechanism, no enterprise complexity by default | `OWNER_DESIGN_PRINCIPLE` |
| OD-05 | PUBLIC must not leak NSFW/Personal/People; keep correct authorization | `OWNER_PRODUCT_INVARIANT` |
| OD-06 | Evaluate UI-oriented `id + name/title` when useful, without universal assumption | `AUDIT_QUESTION`, **not approval of DTO change** |
| OD-07 | Owner has published existing main, reports local equal remote | `OWNER_REPORTED_LOCAL_STATE`; independent local verification still needed |
| OD-08 | Create and correct the applied docs-only preparation package now | `AUTHORIZED_FOR_PREPARATION_ONLY` |
| OD-09 | Audit the alleged `delivery/` automatic ZIP/review packaging behavior; correct related skills/scripts/docs only after evidence and H1 approval | `OWNER_DIRECTED_FOR_AUDIT_DESIGN / IMPLEMENTATION_PENDING_GATES` |
| OD-10 | During H1, correct **all demonstrated affected** existing harness skill and related instruction/workflow files, not just `codex-backend-audit`; retain non-audit compatibility | `OWNER_DIRECTED_FOR_AUDIT_DESIGN / IMPLEMENTATION_PENDING_GATES` |

## Future decisions to obtain after evidence (NOT yet approved)

| ID | Question/gate | Evidence needed | Conservative default |
|---|---|---|---|
| FD-01 | Exact first H0 read-only audit authority: report paths, safe diagnostics, baseline | Accepted preparation, actual publication SHA, bounded H0 proposal | STOP audit execution |
| FD-02 | Are `.codex/config.toml` sandbox/approval settings to be changed? | CLI version/trust/host actual behavior + scenario tests + feasibility | Keep unchanged; report risk, no automatic bypass |
| FD-03 | Which exact audit skill and related harness/rule/config/workflow/delivery files may be modified during H1? | H0 findings, dependency/cross-reference graph, regression tests and exact allowlist | No harness modification until H1 approval; the Owner already selects **rewriting the existing audit skill** as the intended architecture |
| FD-04 | How will Codex-only remediation be routed in existing policy? | H0 workflow mismatch, proposed narrowly scoped exception | No generic use of takeover without authority |
| FD-05 | Any change to frozen ADR/schema/API/module boundaries? | Source tests, compatibility, concrete user need and smallest fix | Keep frozen; `OWNER_DECISION_REQUIRED` |
| FD-06 | New Public-visible route or data field? | Exact route/field, projection/query, negative leakage tests | Keep eight Public GETs; Film POST denied |
| FD-07 | Add display name to V1 Recycle Bin or other endpoint? | Consumer need, safe source, query plan, N+1/privacy/compatibility, regression cases | Preserve V1 exact 3-field identity DTO |
| FD-08 | Which live providers/data/deployment checks may execute? | Credential, cost, privacy, rollback and environment plan | OFF/unset, mocked/fixture-only if authorized |
| FD-09 | Finite correction-round budgets per actual batch? | Findings volume, risk, proposed batch and independent review method | Suggested max three **not an authorization** |
| FD-10 | How to adjudicate a real actionable LOW/INFO that cannot be fixed? | Root cause, minimal options, constraint, consequences | STOP, not implicit waiver |
| FD-11 | Reopen paused Frontend H1-F or deferred RAG/indexing? | New separately approved product scope | Remain paused/deferred |
| FD-12 | Does `delivery/` exist and perform automatic ZIP packaging? If yes, which outputs, paths and permissions are correct? | Actual directory/script/skill references (including ignored/untracked local files), trigger reproduction, safe artifact manifest | `NOT_VERIFIED`; no automatic packaging requirement or review acceptance claim, no unapproved creation of folder or ZIPs |

## Decision doctrine

- Historical accepted decisions remain binding until Owner ratifies a superseding *current* decision. **OD-03 is that explicit superseding decision** for the 2026-11-01 requirement to keep Phase 15 runnable skill behavior; it does not alter Phase 15 historical evidence or other approved constraints. Current source drift alone is not Owner consent.
- Failure to decide **FD-01 blocks H0**, not preparation creation/review. FD-02–FD-12 are conditional gates when their actual evidence arises. A directive to *evaluate* or *plan* a change is not an unrestricted source-edit grant.
- Keep `RECOMMENDATION` distinct from `DEFECT`, `ESTIMATE` distinct from observed benchmark, and `NOT_VERIFIED` distinct from PASS. Avoid “fix everything” commands that silently mutate frozen data.
