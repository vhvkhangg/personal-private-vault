# P19-S19-1 — Owner authorization for bounded Codex implementation takeover

- **Decision date:** 2026-10-27.
- **Status:** `OWNER_APPROVED — ONE_BOUNDED_CODEX_IMPLEMENTATION_TAKEOVER / NOT_STARTED`.
- **Owner decision:** The Owner explicitly approves **one separate Codex Implementation Takeover**, confined to the unresolved blockers of **P19-S19-1**, following the independently blocked third Antigravity remediation/re-review. This approval authorizes the takeover skill to make scoped implementation corrections and produce required evidence; it **does not** mean the candidate has passed review, is ready for commit, or may be deployed.
- **Controlling review:** [S19-1 independent final re-review 3](reviews/2026-10-27-s19-1-independent-final-re-review-3.md), `CHANGES_REQUESTED / OWNER_DECISION_REQUIRED` at the approval boundary.
- **Active execution register:** [ACTIVE.md](../handoffs/ACTIVE.md); [S19-1 executable handoff](s19-1-executable-handoff-2026-10-27.md).
- **Previous Owner scope:** [S19-1-only implementation authorization](owner-authorization-s19-1-2026-10-27.md) and [P19-OD-01 Option A](owner-decision-p19-reader-exclusion.md) remain unchanged.
- **Actual counter:** Antigravity remediation + independent re-review `3/3 EXHAUSTED`. Takeover independently authorized **once**, **NOT_STARTED** when this decision is recorded. No fourth Antigravity round and no reset of any counter.
- **Repo/reference baseline:** re-review 3 records local HEAD `ead5291bdffee09e53d0b41957ea751231015db2` with **58 unstaged candidate paths** at entry. Verify actual working-tree state before editing; this document does not certify an unchanged tree or newly run tests.

## Exact, finite takeover scope

Codex may implement **only remaining** S19-1 findings **F02/F04/F05/F06/F08 (HIGH)** and **F09/F11 (MEDIUM)** and their indispensable tests/evidence under the already authorized S19-1 acceptance and race matrix. The [re-review 3 residual register](reviews/2026-10-27-s19-1-independent-final-re-review-3.md) is the definitive list. Specifically:

1. **F02 — Canonical guarded read authority.** Bind full source/child closure, snapshot/revision, consent-reset, epochs, approved field set, generation/manifest/pipeline and runtime identity to authoritative `C_A` and `C_E` short guarded transactions, including no stale preflight-only authorization or unsafe reuse after a concurrent mutation.
2. **F04 — Exact-scope incident/reader finish.** Prevent ordinary failure/cancel from releasing unresolved inspection; restrict mandatory integrity denial to matching admitted source–revision–token/runtime tuples; verify affected rows, unknown COMMIT and denial/release/admission atomic ordering. Unrelated sources must never be marked denied.
3. **F05 — Real HTTP/native sender quiescence.** Own the complete Java/container/context/output/transport/native lifecycle through actual safe emission completion or positively proven stop. Do not release exclusion before sender quiescence; cover disconnect, timeout, cancellation, failed/unknown stop and blocked write/completion. Do not hold SQL locks during I/O waits.
4. **F06 — Safe recovery, reconciliation and purge.** Preserve live/unknown foreign-controller readers and pending inspections; require positively proven old-owner/controller/process/sender stop and guarded read-back before release or takeover. No PID-only or missing-PID inference, arbitrary reader release, or bypass of source reservation/denial guards.
5. **F08 — Phase 18 locator fidelity.** Validate real parser/chunker end-exclusive spans and line/heading/contributor/revision provenance, including trailing newline, CRLF, supplementary Unicode, mixed parent/child and duplicate-name fixtures. No unverifiable optional citation metadata.
6. **F09 — API/input/eligible discovery.** Restore the canonical root HTTP error envelope, consistent Unicode code-point limits across ingress/facade/wire, bounded payload behavior and `cap+1` discovery over **actually eligible CURRENT** sources/closures rather than raw approved rows. No expansion of the authorized scope/cap.
7. **F11 — Mandatory missing S19-1 proof.** Run/report the original targeted Java/real PostgreSQL/native/HTTP/race acceptance gates, actual P18-producer-to-query retrieval path, VI/EN/Han expected passages, WHOLE_FILE consent, rosters/transclusions, real sender/foreign controller/second-JVM/unknown-commit races, offline identity and resource bounds. Report actual receipts and `FAIL`/`NOT_VERIFIED` honestly; update only authorized current implementation-adjacent documentation.

The review's closed **F01/F03/F07/F10/F12/L01** implementation findings are preserved and **must not be reopened without a demonstrated regression**. L02 and unrelated LOW backlog remain deferred; no LOW-only work is authorized by this decision.

## Binding restrictions

- Preserve Phase 17 `FROZEN`, Phase 18 `CLOSED`, exact P19-OD-01 durable reader semantics and previously approved S19-1 API, scope, security, runtime and DB boundaries. Any required change to a frozen security/consent/authority contract, expanded privileges, different phase/slice or new architecture is **STOP / seek separate Owner decision**, not implied takeover permission.
- No local LLM generation/S19-2, Fiction/Film recommendations, frontend, GitHub sync, OCR/PDF/Office/GPU work, extra indexing activation, external production data import, broader source policies, new generic framework/agent or unrelated cleanup.
- Retain all prior raw evidence, reviews, historical claims and 3/3 counters without overwrite. No auto-publish, commit, push, tag, deployment or unapproved changes to unrelated files.
- Implement the **minimal root-cause changes plus focused regression tests**, using the existing approved RAG/Java/Python conventions and bounded execution/watchdog profile. An already-green component test does not waive remaining acceptance or independent verification.
- **Authorship separation is mandatory:** Codex performing `$codex-implementation-takeover` is the **implementer**, not the independent reviewer. When complete, mark the existing handoff `IMPLEMENTED_AWAITING_CODEX_REVIEW` with Codex takeover attribution, verified scope, test receipts and Docs Sync Check. Have the Owner open a **separate independent Codex session** and run `$codex-final-review`. The implementation session may not close its own findings or mark `READY_FOR_OWNER_COMMIT`.
- If the bounded takeover remains blocked, stop and return the precise residual evidence and **another Owner decision**. Do not silently begin an additional round.

## Docs Sync Check — Owner decision only

- **UPDATED:** this dated authorization; the current [ACTIVE](../handoffs/ACTIVE.md) execution/status and [S19-1 handoff](s19-1-executable-handoff-2026-10-27.md) remediation ledger.
- **UNCHANGED:** the original and all three independent reviews, raw receipts/logs, implementation code/test/migrations/skills/hooks, historical Owner approvals, P17/P18 and publication state.
- **NOT_VERIFIED:** seven remaining defects, all outstanding S19-1 runtime acceptance, real sender/resource/DB proof and future independent takeover review.
- **DEFERRED_OUT_OF_SCOPE:** S19-2+, unrelated governance notices and LOW milestone/audit backlog.

**Next step:** Owner | place the approved docs into the existing S19-1 repository tree, then invoke `$codex-implementation-takeover` **without a supplementary prompt** | [ACTIVE.md](../handoffs/ACTIVE.md) + [re-review 3](reviews/2026-10-27-s19-1-independent-final-re-review-3.md) + this exact authorization | one bounded takeover approved, existing 3/3 exhausted, no commit of unaccepted implementation | implementer Codex / independent later review required.
