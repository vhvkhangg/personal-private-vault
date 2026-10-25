# Current Owner Delivery Policy — effective 2026-10-25

**Status:** Owner-approved forward workflow. **Phase 17 FROZEN; Phase 18 COMPLETE/CLOSED under D26/D27; Phase 19 NOT AUTHORIZED / NOT STARTED.** This policy controls future work and maintenance only. It does not retroactively rewrite historical reviews, tests, evidence, Owner decisions, or accepted contracts. `docs/implementation/handoffs/ACTIVE.md` is the sole current execution-handoff status.

## User need is the product contract

- This is a permanent **single-account** private web app with local/offline RAG; remote access by the Owner remains supported, not enterprise/multi-user RAG. Prefer existing simple mechanisms (KISS, DRY, YAGNI); do not invent multi-tenant infrastructure, distributed jobs, extra formats, live-file surveillance, automatic agents, or framework abstractions.
- Before any handoff, make the Owner-visible requirement inventory explicit, show `KEEP / REUSE / SIMPLIFY / REMOVE / DEFER` against **proven** current needs, estimate implementation/test burden, compare the simplest adequate alternative, and ask the Owner to resolve material choices **together before** implementation. A speculative threat or optional idea cannot become a blocking requirement.
- Preserve **necessary and already-accepted** privacy, permission, consent, correct results, atomicity and data-integrity guarantees. Simplification of a frozen contract/code path requires a separately approved, demonstrated-equivalent maintenance change; do not silently relax security or erase records.
- Owner's desired **future** RAG capabilities (not Phase 19 authorization): (a) manually select/import `.md` (ordinary Markdown or Obsidian), immutable snapshot with explicit approved dependencies; search and offline grounded Q&A with correct citations; (b) prompt or selected existing **Fiction** -> similar Fiction, **Film** -> similar Film; free-form prompt can target Fiction, Film, or both; only real permitted catalog IDs and grounded reasons. No Markdown-note recommendations and no named Ideaverse integration. Q&A may use owner-approved Markdown and approved Fiction/Film fields selected by source scope. Private/NSFW default excluded; explicitly selected whole-file approval for relevant Markdown allowed under existing policy. Unsupported/unapproved embeds fail that file atomically while independent files can proceed. Keep indexing INACTIVE until independently authorized; do not infer activation from closed Phase 18.
- The **future** quality gate is proportionate: small deterministic multilingual retrieval/citation, grounded answer/abstention, catalog-valid recommendation and privacy negative cases. A Codex reviewer may examine actual sample answers. No mandatory LLM-as-judge, human adjudication, elaborate benchmark dashboard, OCR/PDF/Office/TXT, GPU proof, large-scale rerank/hybrid infrastructure or live-file R7 without a separate Owner request. Historical D17/D20 and D26 acceptance remains intact.

## Review cadence — one clear layer per event

1. Before a new approved phase/slice: ChatGPT prepares requirements, decisions and docs; **Codex pre-handoff review** checks true user needs, reuse/simplification, implementation/test feasibility; **Codex creates a detailed executable handoff** with positive/negative observable acceptance examples. These are planning/review tasks, not additional implementation approvals or a fresh pre-handoff review for every small fix.
2. **Every slice:** Antigravity implements, tests and **always self-reviews** (not counted as an independent review). **Codex independently reviews the completed slice**. After blocking findings are fixed and reviewed, it returns `READY_FOR_OWNER_COMMIT`; Owner performs local `git add`/`commit`/`push` and the slice gets a *brief* closeout referencing the actual commit. No extra agent staged-tree review.
3. **After all slices of each phase:** Codex runs **one phase final review** aggregating reviewed slice evidence and only executing necessary changed-source verification; phase Owner closeout after publication. Do not repeat every prior source/test gate without a concrete source-drift or review finding.
4. **Every three completed RAG phases:** Codex **milestone review**; next boundaries are after **Phase 19** (17–19) and **Phase 22** (20–22). Earlier Backend milestones 3/6/9/12 remain historical. The milestone gate never starts Phase 19 or unfreezes Phase 17/18.
5. **After completing all RAG phases:** independent **RAG-wide audit** (Phase 17–22). **After RAG and Backend are complete:** independent **RAG + Backend integration/repository-wide audit**, reusing valid RAG audit receipts rather than duplicating work. Both are separate explicitly authorized future gates, not feature-implementation phases; no Phase 19 work is authorized by scheduling them.
6. **LOW/INFO backlog:** record with ID/evidence at slice review; consolidate a single remediation pass at the next milestone review/audit where justified. Do not loop on LOW/INFO alone.

## Finding severity and acceptance

| Severity | Definition (evidence required) | Blocks `READY_FOR_OWNER_COMMIT`? |
| --- | --- | --- |
| **CRITICAL** | Confirmed catastrophic data loss, privilege bypass, severe active exploit | **YES** |
| **HIGH** | Confirmed core functional/integrity/security defect | **YES** |
| **MEDIUM** | Reproducible incorrect approved requirement, important coverage/integration/maintenance defect | **YES** |
| **LOW** | Minor hygienic or limited-impact defect | **NO**: backlog for milestone/audit |
| **INFO** | Observation/recommendation, no required behavior broken | **NO**: backlog and optionally consolidate |

An unresolved `NOT_VERIFIED` **mandatory** acceptance case blocks regardless of severity. Every finding needs exact location, reproducible impact, minimal remedy and applicability. `NOT_APPLICABLE` is only legitimate when explicitly within Owner-approved scope; it is not PASS. New desirable features are suggestions, not findings.

## Remediation, ownership and Git safety

- Initial Antigravity implementation and self-review **do not consume** remediation rounds. After the **initial Codex slice review** identifies blocking findings, allow up to **three Antigravity correction + independent Codex re-review rounds on that active slice/handoff**. Do not reset by opening a new session or counting each finding separately. Codex `$codex-implementation-takeover` is **last resort**, only after all three independently remain blocked or the Owner expressly authorizes an exceptional bounded takeover; Codex implementer must never self-review its own implementation.
- Antigravity implements production/tooling; Codex mainly reviews and writes handoffs; ChatGPT handles docs. Agents may read code, run tests and edit within approved scope. **Only Owner** stages, commits, pushes, tags/releases, opens/merges PRs; reject destructive repository commands. Project Codex execpolicy and Antigravity hook are **defense-in-depth only**, must be tested on the actual Windows CLI; do not claim guaranteed enforcement from Markdown rules, token-prefix filters or unit tests.
- No separate staging **review session**. This does **not** remove Git's normal staging step (`git add`) performed by the Owner before `git commit`.

## Tests, watchdog and short output

- **Maintenance B pending:** executable `scripts/run-bounded-command.py` currently has an old fixed normal 1,200s/full-suite 2,100s timeout and **does not** diagnose inactivity. Do **not** claim the new watchdog is active until Antigravity implements/tests and Codex independently reviews it.
- Future intended behavior: normal command wall-time cap **300s**, explicitly approved large/complex verification wall-time cap **up to 2,100s**; for *every command* after **300s without progress/output** inspect log, child process health, CPU/I/O/thread and test reports; verify deadlock/hang before terminating **only its owned process tree**. Lack of stdout alone is not proof of a hang; a genuinely long test can be quiet. Exit nonzero: report failure promptly. Host/tool limits may still bound execution.
- Short console: command summary, bounded progress, first actionable error and receipt/log path. Save full stdout/stderr/diff securely to ignored `logs/` **without deletion, relocation or truncation of existing historical logs**. Codex `model_verbosity = "low"` cannot suppress all CLI tool events or printed diffs; suppress unsolicited large diff excerpts in agent narration and use `git diff --stat`/named paths, not giant pasted patches.
- Full verification and independent re-review are **risk-driven**, not repeated for mere docs changes; do not compromise phase-specific frozen evidence or reinterpret failed historical runs.

## Documentation responsibilities

- `docs/templates/` and `.agents/skills/documentation-writing/SKILL.md` govern new ADR, finding, review, handoff, Owner decision and closeout records. Keep a **single current status** and link dated history. Phase-scoped artifacts under `docs/implementation/phase-N/`, maintenance artifacts under `docs/implementation/maintenance/`, reusable policy under `docs/workflow/`. Historic material requiring hash/reference preservation is archived, not rewritten to look green.
- No human signature/adjudication gate for new work. Preserve material archived evidence; eliminate obsolete user-facing adjudication UI/forms only after an exact link/reference inventory and explicit deletion mechanism. No patch scripts for normal docs closeout: deliver only new/changed files and an explicit deletion list, if needed.
