# Independent preparation review — challenge contract (NOT an executed review)

**Role:** an independently launched Codex review session, or Claude as external challenge reviewer; **scope:** only this docs-only preparation package and the latest current repository authority. A reviewer must not treat this document as evidence the review was performed. Output a separately dated, actual review report with exact findings and line references; no self-repair in review session.

## Inputs, provenance and prerequisites

- Confirm `main` HEAD, `origin/main`, remote `main` and local `git status` before reviewing, **without** modifying Git state. The drafting baseline was `4e4e2cb50ac84c86b0df93ad67f770648c7d32db`; if the actual HEAD moved after Owner placed these files, identify old/new SHA and whether material requirements changed. Do not falsely demand exact old HEAD after an authorized docs publication.
- Read root `AGENTS.md`, `docs/workflow/current-owner-delivery-policy.md`, `docs/implementation/handoffs/ACTIVE.md`, 2026-11-01 final audit Owner intent, 2026-11-11 Backend-first decision, 2026-11-15 V1 closeout, audit skill and review coverage skill. Select the **maintenance docs-only** review route, not historical Phase 15 execution.
- Inspect all fourteen preparation files, the actual file tree and changed-files-only correction scope, links and cross-references. For NRP-PH-02, the author supplies a **separate external historical ZIP provenance bundle** documented in `acceptance-and-reconciliation.md` §A.1. Validate original/correction archive hashes and before/after file bytes **independently**, then compare the new overlay against the real local applied files; if the evidence bundle is absent or cannot be reproduced, classify A20/A21 `NOT_VERIFIED`. Verify there are no references to nonexistent generated logs, reviews or test results as fact.
- Validate baseline claims against git tree and authoritative docs, not model memory. Verify external claims against primary sources and exact versions; avoid importing external repo conventions as requirements.

## Independent challenge questions

| ID | Adversarial question | Finding threshold |
|---|---|---|
| PR-C01 | Is current user authority truly **preparation-only**, or did any file silently allow audit/implementation? | Any unintended execution permission = BLOCKER |
| PR-C02 | Do docs create a circular bootstrap: skill cannot run current mode until its own approval? | Missing H0 safe bypass/Owner gate = BLOCKER |
| PR-C03 | Does the 2026-11-15 Owner decision **replace legacy Phase 15 runnable skill logic** while preserving every historical report/record and keeping RAG suspended? | Historical rewrite/execution or retaining old callable Phase 15 contrary to Owner direction = BLOCKER |
| PR-C04 | Is Codex-only session separation explicit and enforceable without Antigravity? | Same author independent-accepting own change = BLOCKER |
| PR-C05 | Does the H0 scenario matrix exercise V1 handoff discovery failure and negative cases? | Missing scope/ambiguity/supersession tests = finding |
| PR-C06 | Is use of GPT-6.1 Sol or safety hooks **observed** or simply asserted? | Unsupported “tested on Sol/secure” claim = BLOCKER |
| PR-C07 | Does full docs coverage truly include **all** docs and immutable history differentiation? | Silent excluded current files = finding |
| PR-C08 | Is code scope truly all current non-RAG modules, tests, migrations and security/shared boundaries? | Missing owner-visible module or RAG boundary confusion = finding |
| PR-C09 | Could PUBLIC leak People/Personal/NSFW via errors, Search, DTO, counts or linked data? | Missing negative channel review = finding; actual confirmed leak needs future H/B investigation |
| PR-C10 | Does `displayName` proposal accidentally authorize an API/schema change or break V1? | Unauthorized contract mutation = BLOCKER |
| PR-C11 | Are design recommendations measured/evidenced rather than complexity preference? | Mandatory refactor without demonstrated reason = finding |
| PR-C12 | Does review fabricate code/audit test evidence, local CLEAN, provider access or full suite acceptance? | Any unqualified fabrication = BLOCKER |
| PR-C13 | Does all-severity closure handle real LOW/INFO without forcing false positive edits? | Auto waiver or unjustified mandatory tweak = finding |
| PR-C14 | Are remediation budgets finite and prior counters immutable? | Unbounded retries or reset = finding |
| PR-C15 | Are exact future Owner stop gates and file allowlists defined? | Implicit authority from empty ACTIVE/published proposal = BLOCKER |
| PR-C16 | Is every preparation acceptance item provable by a source, path, or explicit future check? | Non-falsifiable gate = finding |
| PR-C17 | Does the correction ZIP modify **only existing preparation draft Markdown**, with no source/harness/history edits or fake independent reports? | Scope breach = BLOCKER |
| PR-C18 | Does final reconciliation revisit Docs hypotheses after code rather than canonize unverified design assumptions? | Missing provisional stage = finding |
| PR-C19 | Can a fresh reviewer understand execution order, input/output and safe STOP without this chat? | Missing critical authority or handoff instruction = finding |
| PR-C20 | Are the proposed checks feasible on Windows 11, PowerShell, Docker/Testcontainers and real Codex CLI? | Unsupported universal command/version assertion = finding |
| PR-C21 | Is the existing audit skill clearly rewritten (not duplicated), and are its dependent existing skills/rules/docs/scripts correctly identified for an H1-only patch? | Implicit skill-creation, stale routing or unconditional edit permission = finding/blocker by impact |
| PR-C22 | Is `delivery/` auto ZIP behavior treated as unverified until tested, with safe, scoped packaging changes rather than fabricated workflow? | Claimed automatic review ZIP without evidence or self-acceptance by ZIP = finding |

## Required independent output (in a future, permitted review session)

1. Verdict exactly `READY_FOR_OWNER_COMMIT` **or** `CHANGES_REQUESTED` **or** `BLOCKED_REVIEW`, clearly restricted to `DOCS_ONLY_PREPARATION`.
2. Scope/baseline: actual local & remote SHA, tracked/untracked/diff list and whether corrected paths match the existing applied draft and touch nothing else.
3. Prioritized findings by stable ID `NRP-PH-##`, severity, exact path/line, conflicting authority, reproducible impact, minimal correction and expected evidence. Do not invent findings to hit a quota.
4. A `NRP-A01..A23` matrix of PASS/FINDING/NOT_VERIFIED/N/A plus evidence path for all material criteria. Empty cells are not PASS.
5. Explicit checks for unauthorized source edit, dirty-worktree preservation, historical integrity, current GPT-6.1 Sol uncertainty, phase/mode routing and all LOW/INFO disposition.
6. Factual test report: which static link/whitespace/Markdown/Git checks were actually executed, exit codes, and what was **not** executed. No JVM/DB/runtime acceptance from this docs-only task.
7. A finite next step: on READY Owner publishes reviewed docs; on CHANGES_REQUESTED author corrects only bounded preparation files, then **a separate independent re-review**; on BLOCKED exact missing fact/authority. Never start H0 or open executable from review verdict alone.

## Reviewer independence warning

Codex sessions using the same model are not statistically independent auditors in the strong sense. Fresh contexts/diff inspection reduce self-review conflict but do not prevent correlated errors. A Claude challenge is welcome, but it must present source-grounded counterexamples; brand comparison does not replace reproducible tests or Owner approval.
