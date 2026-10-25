> **2026-10-25 OWNER UPDATE:** This was a **first-pass historical audit** before the current ZIP and Owner decision rounds. Current decisions are complete in [owner-decision-docket.md](owner-decision-docket.md). **Ideaverse was only an example, not a requested integration.** Approved future scope: plain/Obsidian `.md` manual immutable snapshot, cited search/Q&A (also approved Fiction/Film sources), and Fiction/Film similarity recommendations by prompt or same-type existing item. No automatic judge/human signature. Status and risks in the audit body below describe the earlier review snapshot, not fresh unresolved Owner questions.

# RAG Phase 17–18: first-pass audit of decisions and excess scope

**Review classification: SOURCE-BASED PARTIAL AUDIT / NOT A COMPLETE CURRENT-TREE SIGNOFF.** Examined the 2026-10-23 archived `docs/` tree, current GitHub-selected authoritative Phase 17/18 owner decisions/ADRs/workflow/agent skills, the D26 reviewed final outcome and D27 closeout. Historical archive inventories contained **37** files under `docs/implementation/phase-17` and **325** under `phase-18` (including evidence and historical review records), not a verified count for current 2026-10-25 main. A current exact-tree deletion/cross-reference audit is still needed.

## Owner-confirmed / owner-authored decisions (not agent invention)

| Decision or boundary | Evidence/source | Status now |
| --- | --- | --- |
| Permanently single account and local personal project | ADR-0006, ADR-0019; user reiteration | Retain; no enterprise/multi-tenant design |
| Backend-first Spring modular monolith, PostgreSQL, Python local RAG | ADR-0019, Phase 17 owner decisions | Accepted foundation; do not redesign automatically |
| Alternative B Phase 17 single active job, bounded owner approval/revocation, simpler reservations | ADR-0020, Phase 17 Owner Decisions 2026-10-15 | Closed/frozen; preserve behavior, audit complexity before new code |
| Practical Local Offline rather than global firewall/packet proof | Phase 17 owner decisions 2026-10-16 | Avoid unrequested OS security maze |
| Immutable whole-file snapshot over live-file R7 for Phase 18 | ADR-0022/0023, D12–D14 and related Owner approvals | Strong simplicity precedent; prefer it for future `.md` source handling |
| Obsidian `.md` ONLY, OCR/PDF/Office/TXT removed from Phase 18 | D21/D24, D26, Phase 18 closeout | Retain `.md` quality; ordinary `.md` usability to test explicitly |
| No human adjudication requirement for D17 signoff | Phase 18 D20 | Do not recreate human-signature gate |
| Pinned offline CPU only, GPU subcases N/A not PASS | Phase 18 D26 | Preserve truth; future GPU by separate Owner choice |
| One Owner commits/pushes, phases owner-gated | AGENTS/Owner workflow and recent requests | No agent Git publication or automatic Phase 19 |

## Technical mechanisms selected/designed largely by agents; Owner separately approved some

**Important:** Owner approval of a presented option does not imply the Owner originally requested every implementation detail. Treat this table as provenance/classification for review, not retroactive revocation of accepted functionality.

| Mechanism / decision area | Why it appeared | Minimality question (requires Owner answer or concrete source review) |
| --- | --- | --- |
| Phase 17 reservation/source-version/epoch/CAS, worker lease, staging, restricted PG role | Concurrent edits, consent and atomic publication across Java/Python/PG | Keep necessary correctness and one active job; remove duplicate states/tickets/abstractions only with equivalent tested guarantees |
| Phase 17 E1 expiry and unknown-C/restart reconciliation | Post-deadline activity and uncertain commit safety | Retain no-corrupt/no-unauthorized-publication behavior; ask whether the implementation can be simplified for single desktop, not whether evidence can be falsely waived |
| ADR-0019 hybrid retrieval, recommendation, read-only API routing, agentic deferral | Future roadmap proposed as technical paths | Reconfirm each as a separate **actual Owner-required** end-user feature before any new handoff; no speculative engine |
| Continuous local judging, held-out/adjudication, automatic async evaluation | Agent-style test/evaluation proposals | Human gate is already superseded by D20; ask whether anything beyond deterministic acceptance/tests is useful |
| Phase 18 C_N/no-change exact child/G proof; immutable R and atomic C_P | Prevent genuine false NO_CHANGE, stale children or partial publication | Keep proven correctness; prefer simple existing rows over generic durable workflow layer |
| Same-R reindex, revoke/purge/restore report distinctions | Snapshot semantics & owner-visible status | Confirm which statuses/UI interactions Owner actually needs, not automatically all enterprise recovery drills |
| GPU / OCR / Office/PDF/TXT | Early broad proposed scope, later reduced by D24/D26 | Removed or N/A; do not reintroduce under future RAG planning |
| Windows NTFS hostile hard-link lifetime proof / live-file R7 | Initial live-original security goal, superseded for snapshot by ADR-0023 | Do not resurrect for `.md` snapshot ingestion |
| Pre-handoff 29-dimension coverage, 20/35 min time cap, 2 remediation rounds | Agent harness/governance choices later approved as policy | Owner now requests compact risk-based reviews, progress-aware watchdog and 3 rounds; previous policy is historical |
| Exact 120-file delivery / independent staged-tree workflow | High-fidelity delivery governance in Phase 18 | Simplify: independent review → Owner ordinary commit/push; no separate staging review session |

## Concrete documentation/harness issues found

1. Existing `scripts/run-bounded-command.py` waits on a fixed `proc.wait(timeout)` with no observable progress/watchdog feedback, distinguishing no output vs healthy CPU work poorly. **Runtime correction not yet implemented.**
2. `.codex/config.toml` includes `model_verbosity="low"`; this controls assistant prose under supported settings, **not Maven/JUnit/python stdout**. Need distinguish verbose model messages from command logs and current Codex version/session overrides.
3. `.agents/skills/codex-create-handoff/SKILL.md` says "Keep handoff concise," and has outdated Phase 18 R7/20/35/2-round instructions. A more exact scenario-level handoff is warranted for future approved slices.
4. `.agents/skills/antigravity-implement-handoff/SKILL.md` currently has no explicit implementer self-review gate and still hardcodes 20/35/2-round guidance; add targeted self-review and update the skill after Owner choices.
5. `.agents/skills/codex-final-review/SKILL.md` already explicitly checks clean code, architecture, SOLID, DRY, KISS, YAGNI, patterns and module boundaries. **The gap is consistency and prevention of unsolicited architecture, not complete absence of clean-code review.**
6. ADR-0019's Ideaverse deferment (no implementation in RAG 17–22 without explicit approval) conflicts with a new priority to use RAG with Ideaverse files. Ask whether desired support is just manually imported `.md` folders or autonomous Obsidian integration; no automatic watcher/module follows.
7. Phase 18 has a large historical checkpoint/evidence corpus and `docs/D20-PATCH-MANIFEST.json` at docs root. The latter is a dated **patch receipt**, not an ADR. Propose archival to `docs/implementation/phase-18/archive/` only after exact path/reference verification.
8. `docs/implementation/phase-18/d17-human-adjudication-2026-10-20/` includes many unfilled human-signature/checklist/HTML/source artifacts. Some source/corpus outputs may be referenced by accepted D17/D20 hashes. Split into safe-to-delete working displays/forms and must-retain immutable approved evidence before deletion. Do not delete the entire folder blindly.
9. Current `docs/README.md`, `docs/roadmap.md`, `AGENTS.md`, `docs/implementation/handoffs/README.md` still contain prominent 2026-10-20–24 'CURRENT' statements. Effective D27 closeout resides in ACTIVE/new closeout, but navigation should be tidied after exact current-source retrieval and approved archive moves. Avoid byte-for-byte rewriting all historical docs.
10. Existing review docs under their owning `phase-17/reviews/` or `phase-18/reviews/` are structurally correct; don't flatten them into one folder merely because they are numerous. Use common `docs/templates/` to standardize **new** records; review severity must distinguish blocking from evidence-not-verified.

## Current unresolved audit boundaries

- Exact complete *current* Git tree for `docs/` and all agent skills was not available as a single materialized snapshot at this checkpoint. Therefore this is a verified **targeted** audit of selected current files plus a complete historical archive inventory, **not** full line-by-line final current-tree audit.
- Actual supported ordinary standalone `.md` intake vs Obsidian-vault semantics needs an explicit normal Markdown fixture and user flow test; do not assert supported from extension checks alone.
- RAG Phase 19 Q&A/retrieval and Ideaverse directory-selection behavior are **not authorized** here. No product implementation follows this maintenance audit without Owner decisions.
- Source/code simplification suggestions are candidates requiring proof that approved behavior and privacy are preserved. No retroactive Phase 17/18 acceptance rewrite or feature deletion is authorized by this audit.
