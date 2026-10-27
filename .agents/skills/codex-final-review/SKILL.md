---
name: codex-final-review
description: Final-review an implemented active handoff or explicit owner-approved maintenance/governance slice; request remediation or return READY FOR OWNER COMMIT with one commit message.
---

# Codex Final Review

**CURRENT REVIEW CADENCE:** [Owner policy](../../../docs/workflow/current-owner-delivery-policy.md) governs. For **each completed slice**, do one independent Codex slice review; after all slices in a phase, do **one phase final review** consolidating prior accepted evidence and only re-running relevant checks for drift. Also review at milestones after P19/P22 and separate RAG-wide / RAG+Backend audits when authorized. An implementer self-review is **not** an independent review. CRITICAL/HIGH/MEDIUM OPEN/mandatory NOT_VERIFIED block; LOW/INFO are tracked for milestone/audit and do not force per-slice rework. No new feature/pattern can be imposed as a finding without Owner need or reproducible defect. Max **three** Antigravity remediation + independent re-review rounds on active slice before possible Codex takeover. Never require a separate staging-review session; Owner alone performs normal Git add/commit/push.


Invoke with `$codex-final-review`. Codex is review-only.

## Review-mode gate

### Implementation mode

Use only when `docs/implementation/handoffs/ACTIVE.md` is `IMPLEMENTED_AWAITING_CODEX_REVIEW`.

### Maintenance/governance, phase-final and audit mode

Use only for explicit Owner-approved maintenance scope, phase-final aggregation of previously independently reviewed and published slices, or explicitly authorized audit. A frozen phase may be touched only where that scope says so. Phase-final is not a second final review of the same uncommitted slice.

### No valid mode

If neither mode applies, stop and ask for the approved review scope. Never infer/reopen a frozen phase.

## Relevant skills

Apply the relevant review skills:

- `java-spring-coding-standards`
- `pragmatic-solid-design`
- `reuse-and-consistency`
- `design-pattern-selection`
- `modular-monolith-architecture`
- `jpa-postgresql-persistence`
- `backend-testing`
- `authentication-security` when relevant
- any phase/domain-specific skill named by the active handoff

## Mandatory shared coverage gate

Use [`codex-review-coverage`](../codex-review-coverage/SKILL.md) as a **risk-applicable checklist** (record irrelevant categories as a compact N/A group; no artificial 29 independent gates), in addition to the targeted rules below. In particular perform an actual recursive changed-package/folder inspection, compare `ApiResponse` and other shared transport abstractions against accepted ADR-0016 (do not demand an arbitrary `common` module), and inspect exception/interface/enum/command/view grouping and coupling. Every dimension must be PASS/FINDING/N/A/NOT_VERIFIED with rationale and code or test evidence where applicable. For RAG, require live source-commit guards and local-only worker/security integration tests **only for changed/enabled capabilities**. Require external-file handle/identity proof when the active phase enables actual file ingestion; do not demand proofs for Phase 17-disabled future capabilities.

## Mandatory final-review dimensions

### Business / logical correctness

- acceptance criteria and business rules are actually implemented;
- domain invariants hold for success, failure, boundary, repeated, and state-transition cases;
- no alternate path bypasses canonical behavior;
- null/empty/time/identity semantics are deliberate.

### Clean code / maintainability

- naming expresses domain intent;
- methods/classes are cohesive;
- control flow is understandable;
- duplicated business rules are centralized in the correct owner;
- helpers/abstractions reduce real complexity rather than hiding simple logic;
- logging is useful, non-noisy, and non-sensitive.

### Extensibility without speculation

- dependencies point toward stable narrow contracts;
- persistence/internal details do not leak;
- approved likely evolution is not blocked by avoidable coupling;
- no abstraction exists only for hypothetical future variants;
- YAGNI remains the default.

### SOLID / patterns / overengineering

- responsibilities, interface size, dependency direction, and substitutability are sensible;
- Strategy/Factory/Adapter/Facade/State/Specification/Event/Builder/etc. appear only for a concrete problem;
- flag both under-designed coupling and pattern-driven overengineering;
- generic bases, `ServiceImpl`, unnecessary interfaces/events/hierarchies are not introduced for convention alone.

### Performance / efficiency

Check concrete risks:

- N+1 or unnecessary eager loading;
- repeated DB/network/file work in loops;
- unbounded growing reads;
- avoidable scans when appropriate indexed lookup exists;
- excessive lock scope/contention;
- unnecessary repeated serialization/hash/computation on hot paths;
- realistic algorithmic or memory growth.

Do not demand speculative micro-optimization. Every performance finding must identify a plausible/measured
inefficient path and a proportionate correction.

### Persistence / transactions / concurrency

- JPA matches Flyway/PostgreSQL;
- transaction boundaries fit the use case;
- uniqueness/locking/race behavior is safe;
- failures cannot leave partial state;
- no cross-module entity/repository access;
- no silent frozen-schema rewrite.

### Security / privacy

- authn/authz boundaries are correct;
- secrets/passwords/PINs/tokens/private payloads do not leak through logs, DTO strings, exceptions, tests, or diagnostics;
- security-sensitive validation fails closed;
- no custom cryptography where platform/framework primitives exist.

### Tests / evidence

- tests prove behavior instead of mirroring implementation;
- negative/boundary/concurrency cases exist where risk warrants;
- PostgreSQL behavior uses PostgreSQL/Testcontainers, not H2;
- architecture verification remains green;
- final evidence records the handoff-required command/result/environment;
- tests do not leak secrets or rely on brittle timing/order.

### Repository tree / package hygiene

- package layout is coherent and capability-oriented;
- commands/exceptions/views/enums are grouped consistently when the API size justifies it;
- meaningful packages have `package-info.java`;
- no stale `.gitkeep`, duplicate source, obsolete moved copy, generated cache, or unexpected tracked artifact remains;
- repository/package-tree docs match the actual structure when the reviewed change affects the tree.

### Static diagnostics / warnings

- review compiler/build warnings and any IDE warnings explicitly reported by the owner;
- distinguish real code problems from IDE configuration/schema-resolution false positives;
- do not rewrite correct native SQL merely because an IDE has no database datasource attached;
- do not silence warnings with blanket suppression unless the reason is concrete and documented;
- do not state that the repository is “warning-free” unless the relevant inspection tool was actually executed.

### Scope / architecture / docs

- implementation stays inside handoff scope;
- module dependencies/named interfaces are valid;
- frozen baseline changes have explicit approval/ADR synchronization;
- docs, roadmap, status, handoff, reviews, and evidence match reality;
- no stale TODO or duplicate source of truth remains.

## Findings

For a numbered implementation phase, record formal findings in that phase's `reviews/` folder.

For maintenance/governance scopes, record the formal review in the canonical maintenance scope's `reviews/`
folder rather than attaching it to an unrelated feature phase.

Use Critical / High / Medium / Low severity.
Each finding identifies the concrete file/behavior, consequence, and required correction.

## CHANGES_REQUESTED

For implementation: set the active handoff to `CHANGES_REQUESTED` and add precise remediation.

For maintenance/governance: update the approved scope to `CHANGES_REQUESTED`.

Do not provide a commit message while blocking findings remain.

## READY FOR OWNER COMMIT

If no blocking finding remains:

1. record `READY FOR OWNER COMMIT`;
2. synchronize status;
3. provide exactly one Conventional Commit message;
4. do not commit/push.

## Required final response — next step

Every invocation must end with a concise **Next step:** statement derived from the reviewed scope.

- `CHANGES_REQUESTED` → **route by review mode**, never automatically through `ACTIVE.md`:
  - **Documentation/governance-only maintenance with `NO_ACTIVE_HANDOFF`:** return precise blocking findings to **ChatGPT**, the approved documentation owner, for a bounded file correction. Keep the review/status under the canonical maintenance scope, then rerun independent `$codex-final-review` there. Do **not** invoke Antigravity implementation/test-slice or create/activate a handoff.
  - **Authorized implementation slice (including maintenance tooling with a valid active implementation handoff):** return to Antigravity `/antigravity-implement-handoff`, or `/antigravity-test-slice` only for expressly test-only scope; require self-review and independent re-review. After all **three** correction/re-review rounds are independently blocked, escalate to Owner for a separately bounded `$codex-implementation-takeover` decision. No automatic takeover.
  - **Phase-final or audit without an active implementation handoff:** record findings at that approved review scope and request an explicit Owner remediation disposition. Do not infer a new slice or feature-phase authorization.
- `READY FOR OWNER COMMIT` → state the scope-specific next gate:
  - Phase 15 audit remediation → **do not commit yet**; rerun `$codex-backend-audit` for repository-wide closure and
    commit only after `BACKEND_AUDIT_READY`;
  - other completed numbered phase → tell the Owner to commit/push the accepted delivery; ChatGPT handles closeout docs as needed. Do not require the Owner to relay routine skill results through ChatGPT for another prompt;
  - milestone-triggered maintenance → tell the owner to commit/push, then rerun `$codex-milestone-review`;
  - other maintenance → follow its canonical scope/workflow.

Never leave the owner to infer the next workflow action.

## Documentation synchronization checkpoint — Maintenance B3 amendment

Follow the shared [Documentation synchronization contract](../../../docs/workflow/documentation-synchronization.md) **at this skill's handoff/review boundary**, within Owner-approved scope and without a repository-wide cleanup. Independently check the scoped Docs Sync Check: current status/decisions vs actual reviewed changes, applicable links/references, evidence and publication claims. In B3 docs-only review, check this mechanism and B3-touched documents—not all historical `docs/`. An in-scope material conflict or mandatory NOT_VERIFIED is blocking; out-of-scope drift is recorded, not silently repaired. Record the concise **Docs Sync Check** in the existing handback/review artifact (or identify why no docs are affected). Keep the **Next step** routed to the exact remaining documentation action when necessary.

## Forward next action and interruption recovery — Maintenance B3

At the end of **every completed, blocked or interrupted invocation**, output one concrete line with all fields:

`Next step: <application/person> | <exact skill command or Owner action> | <scope + canonical artifact> | <gate/prerequisite> | <remediation or takeover counter, if applicable>`

**Route for this skill:** Implementation slice CHANGES_REQUESTED with rounds <3 -> Antigravity `/antigravity-implement-handoff` (or `/antigravity-test-slice` for test-only scope); 3/3 -> Owner decides bounded takeover, no automatic implementation. Codex takeover code -> separate independent reviewer. Documentation-only CHANGES_REQUESTED -> ChatGPT. READY_FOR_OWNER_COMMIT -> Owner commit/push, then brief slice closeout; after all published slices -> phase/maintenance final `$codex-final-review` in aggregation mode; phase-final/audit blocker -> Owner disposition.

**Interrupted session:** the Owner may paste the English [resume prompt](../../../docs/workflow/resume-prompts/codex-final-review.md). Resume only from inspected files/evidence, with the current approved scope and existing counter; capacity/tool errors do not authorize a new round or reset evidence. Do not overwrite logs or bypass native safety hooks. A standalone skill command (without a special prompt) remains sufficient for an uninterrupted run.
