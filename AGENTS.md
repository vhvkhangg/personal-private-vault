**D24 CURRENT - 2026-10-21:** Overall Phase 18 external document target is **Obsidian Markdown `.md` ONLY**. Owner authorizes the new initial **Slice 3 Verified Indexing D+F**; see [D24](docs/implementation/phase-18/owner-decisions.md#2026-10-21--d24-overall-markdown-only-phase-18-and-slice-3-verified-indexing), [executable handoff](docs/implementation/phase-18/slice-3-verified-indexing-handoff.md), [baseline/applicability/cleanup](docs/implementation/phase-18/slice-3-markdown-scope-and-baseline-2026-10-21.md). Slice 2/D23 producer-local scope is CLOSED and prior work is **published** at verified main `ef32d41b33dd00e5fa08cf63977e91b8936ef3d5`, including unaccepted mixed changes; publication does not accept them. The former selective staging/commit workflow is superseded and must not be reopened. TXT/Office/PDF/OCR are excluded from Slice 3 and final Markdown acceptance; temporarily retain without use/certification, then remove unnecessary implementation/dependencies/tests/fixtures once at Slice 4 end before P18-FINAL. No Slice 4 implementation now. Phase 17 FROZEN; Slice 1/D14 CLOSED; D22 originals NOT_RECOVERED / original preservation FAILED; FR18-3 OPEN; Java publication NOT ACCEPTED; historical full Java gate FAILED until genuine successful rerun; indexing INACTIVE; live originals DISABLED/DEFERRED; P18-FINAL NOT ACCEPTED. Historical Antigravity remediation 2/2 remains exhausted; this initial slice grants no extra remediation rounds. Preparation only in this session; no activation or agent publishing.

## Preserved pre-D24 status and contract history

**CURRENT OWNER STATUS — 2026-10-21:** Phase 18 Slice 2 *Obsidian Markdown `.md` producer-local* scope is CLOSED under D17/D20/D21/D22 and the D23 bounded Owner closeout, **but the exact selective source/docs delivery is NOT YET COMMITTED OR PUSHED and is NOT READY_FOR_OWNER_COMMIT without staged-tree verification**. This note supersedes only older *current-status* assertions below; historical details are retained. D22's three overwritten raw evidence captures remain NOT_RECOVERED / original preservation FAILED under a narrow accepted exception. ADR-0024 applies only to Slice 2. FR18-3, Java identity/actual publication, original broader Phase 18 scope, Java full FAILED gate and P18-FINAL remain OPEN; external indexing INACTIVE and Slices 3/4 unauthorized. Consult [ACTIVE](docs/implementation/handoffs/ACTIVE.md), [D23](docs/implementation/phase-18/owner-decisions.md#2026-10-21--d23-bounded-phase-18-slice-2-markdown-only-owner-closeout), and [D23 closeout](docs/implementation/phase-18/slice-2-markdown-only-d23-closeout-2026-10-21.md). This is a documentation-only notice and does not authorize agents to stage/commit/publish.

# Personal Private Vault — Agent Operating Contract

**Corrective takeover status - 2026-10-20:** `P18-S2-PARSER-OCR-EMBEDDING / BLOCKED / OWNER_DECISION_REQUIRED`. One separately owner-authorized Codex corrective scope addressed S2-01 through S2-08; it does not reset historical Antigravity 2/2. Deterministic repairs are tested, but four-script OCR/fresh human-adjudicated held-out quality (S2-06), complete native parsing interruption/resource proof (S2-07), and the Java pipeline-identity compatibility gate (S2-08) remain unresolved. See [corrective handback](docs/implementation/phase-18/slice-2-parser-ocr-embedding-codex-corrective-handback-2026-10-20.md). Phase 17 remains FROZEN; Slice 1 COMPLETE/CLOSED under D14; FR18-3 OPEN; external indexing INACTIVE, live originals DISABLED/DEFERRED; Slices 3-4 unauthorized. No Slice 2 acceptance, automatic further implementation or agent publishing. Earlier dated statuses are historical where inconsistent.

This file defines repository-wide instructions for coding agents.

## Project state

**Controlling update — 2026-10-20:** Phase 17 remains **COMPLETE/FROZEN**. Phase 18 **Slice 1 Import & Snapshot (A+B+C)** is **COMPLETE/CLOSED under owner exception D14**, owner commit [`7e1a4cd`](https://github.com/vhvkhangg/personal-private-vault/commit/7e1a4cd34fbe19f8162150945534e9509093b3f5). See [`docs/implementation/phase-18/slice-1-closeout.md`](docs/implementation/phase-18/slice-1-closeout.md). [`ACTIVE`](docs/implementation/handoffs/ACTIVE.md) is **`NO_ACTIVE_HANDOFF`** after this separate docs closeout. **No new implementation is authorized:** Phase 18 remains NOT ACCEPTED; Slices 2–4, FR18-3 remediation and P18-FINAL require future explicit gates. Required full clean verification remains FAILED under D14, not waived globally. Earlier dated Phase 18 status statements below are historical and not instructions to restart old handoffs.

- Private, permanently single-user personal vault.
- Backend-first modular monolith using Spring Boot and Spring Modulith.
- PostgreSQL + Flyway; Maven build.
- Phase 17 **RAG Foundation (Alternative B / ADR-0020)** is **COMPLETE — FROZEN** after
  [Codex independent final acceptance](docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md)
  and owner implementation commit/push `829d294aca7d916851348ff21004c631915d572e`. Its accepted handoff is
  [archived](docs/implementation/phase-17/handoff.md); [ACTIVE](docs/implementation/handoffs/ACTIVE.md)
  was reset to `NO_ACTIVE_HANDOFF` at that closeout and now carries the separate Phase 18 handoff. FR17-1–11 and AB-1–6 are closed for
  bounded Foundation scope; `AUTH-CLOCK` remains OPEN in Authentication under its narrow owner exception,
  which was **not used** by the successful clean gate. Default containment and disabled live external-file
  capability remain active. Frontend Phase 16 and RAG Phases 19–22 remain deferred/owner-gated. **Phase 18 preparation** was accepted and owner committed/pushed as `435ffea`; **P18-DOCUMENT-INDEXING** is `CHANGES_REQUESTED / STOPPED / INCOMPLETE / NOT_VERIFIED`. The owner approved the prospective [ADR-0022](docs/adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) snapshot/whole-file/NO_CHANGE contract and one documentation-only synchronization; no implementation is authorized. Live ingestion is DISABLED / DEFERRED; snapshot ingestion inactive.

- Backend Phase 0 is complete/frozen.
- Backend Phase 1 is complete/frozen: executable Schema v1 + `reference` + `vault`.
- Backend Phase 2 is complete/frozen after owner commit/push.
- Backend Phase 3 (`people`) is complete/frozen after owner commit/push.
- The Phase 1–3 milestone is `MILESTONE_READY`.
- The pre-Phase-4 code-hygiene maintenance is complete/frozen after owner commit/push.
- Backend Phase 4 (`fiction`) is complete/frozen after owner commit/push.
- Backend Phase 5 (`film`) is complete/frozen after owner commit/push.
- Backend Phase 6 (`media` + `location`) is complete/frozen after owner commit/push.
- The Phase 4–6 milestone is `MILESTONE_READY` after committed privacy-safe constraint-logging maintenance and
  post-milestone synchronization/reset.
- Backend Phase 7 (`account`) is complete/frozen after owner commit/push. Its completed handoff is archived under
  `docs/implementation/phase-7/handoff.md`.
- Backend Phase 8 (`knowledge`) is complete/frozen after owner commit/push.
- Backend Phase 9 (`collection`) is complete/frozen after owner commit/push.
- The integrity/query-shape maintenance is complete/frozen after owner commit/push as `785dd7d`.
- The Phase 7–9 milestone is `MILESTONE_READY`; milestone docs are owner committed/pushed and post-milestone
  synchronization/reset is complete.
- Backend Phase 10 (`feed` + `importdata`) is complete/frozen after final acceptance, independent 685-test
  verification, and owner commit/push.
- Backend Phase 11 (`finance` + `journal` + `personal`) is complete/frozen after final acceptance, independent
  787-test verification, and owner commit/push.
- Backend Phase 12 (`search`) is complete/frozen after final acceptance, independent 815-test verification, and
  owner commit/push. Its completed handoff is archived under `docs/implementation/phase-12/handoff.md`.
- Search case-normalization maintenance is complete/frozen after owner commit/push as `a881540`; FRM10-12-1 closed.
- The Phase 10–12 milestone is `MILESTONE_READY` (2026-10-05); M10-12-1 closed and independent 817-test verification passed.
  Milestone review/status docs are owner committed/pushed and ChatGPT post-milestone synchronization/reset is complete.
- Backend Phase 13 Shared REST/API Contract + Module HTTP Exposure is complete/frozen after final acceptance,
  independent 867-test verification, and owner commit/push `ef92d94`.
- Backend Phase 14 Backend Integration Hardening + Portability/Object Storage Closure is complete/frozen after
  final acceptance, independent 920-test verification plus 49 focused tests, owner implementation commit `3bb3f2e`,
  and closeout commit `0a8f3d1`. Its completed handoff is archived under `docs/implementation/phase-14/handoff.md`.
- Backend Phase 15 **Comprehensive Backend Audit & Remediation Gate** over Phases 0–14 is **COMPLETE — FROZEN**
  after the [final Codex acceptance](docs/implementation/phase-15/reviews/2026-10-08-phase-15-final-codex-acceptance.md),
  [repository-wide closure audit](docs/implementation/phase-15/reviews/2026-10-08-phase-15-closure-backend-audit.md)
  (`BACKEND_AUDIT_READY`) and owner remediation commit/push `0b1ab1100084f8c7a4362a42874595b25b2b50ee`
  (`fix(backend): remediate phase 15 audit findings`). All 17 BA15 findings and FR15-9/10/11/12 are closed; no
  unresolved actionable finding or owner-accepted debt remains. Fresh full/coverage verification passed 1021 tests
  with zero failures/errors/skips; static diagnostics were triaged, not asserted to be warning-free. The
  [owner decisions](docs/implementation/phase-15/owner-decisions.md), [audit status](docs/implementation/phase-15/audit-status.md),
  historical reviews, test evidence and [archived handoff](docs/implementation/phase-15/handoff.md) remain preserved.
  `ACTIVE.md` is `NO_ACTIVE_HANDOFF`. The accepted Phase 15 remediation is frozen; future baseline-changing work
  requires a separately approved scope. At that historical **Phase 15 closeout**, Frontend Phase 16 and RAG Phase 17 were still
  not started; the later explicit owner authorization, accepted Phase 17 implementation and
  this closeout supersede that historical status. The owner separately authorized **Phase 17 RAG preparation** on
  2026-10-13; Phase 16 Frontend remains deferred, and RAG implementation requires the usual Codex handoff.

Completed maintenance scope:

- `docs/implementation/maintenance/milestone-1-3-concurrency/README.md`
- `docs/implementation/maintenance/pre-phase4-code-hygiene/README.md`
- `docs/implementation/maintenance/milestone-4-6-privacy-safe-constraint-logging/README.md`
- `docs/implementation/maintenance/milestone-7-9-integrity-and-query-shape/README.md`
- `docs/implementation/maintenance/milestone-10-12-search-case-normalization/README.md`
- `docs/implementation/maintenance/supplemental-backend-audit-2026-10-13/` —
  **COMPLETE / FROZEN** after all SBA-001–007 and FR-SBA-1–4 closed, independent 1,152-test
  verification and owner commit/push `d195b6260274`. See its `closeout.md`.

Architecture-sensitive work must respect:

- `docs/architecture/`
- `docs/database/personal-private-vault-schema-v1-FROZEN-final.dbml`
- `docs/repository/repository-package-tree.md`
- `docs/adr/`
- `docs/implementation/phase-1/README.md`

## Current Phase 17 status — 2026-10-17

Phase 17 is **COMPLETE — FROZEN** under the accepted bounded Alternative B / E1 / Practical Local
Offline contract. Implementation commit/push: `829d294aca7d916851348ff21004c631915d572e`; the
[formal closeout](docs/implementation/phase-17/closeout.md) archives P17-RAG-FOUNDATION and resets
ACTIVE to `NO_ACTIVE_HANDOFF` at that closeout. Independent `mvn clean verify` passed **1,308/1,308** Java tests
(including 154 RAG), Python 37 and safety hooks 13; full reviewer receipts are linked from the
[final review](docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md).
`AUTH-CLOCK` remains OPEN as a separate frozen Authentication/environment issue (exception unused).
This Phase 17 closeout implies no follow-on implementation authorization. The owner separately authorized Phase 18, published its accepted preparation as `435ffea` and invoked the separate **P18-DOCUMENT-INDEXING** [active handoff](docs/implementation/handoffs/ACTIVE.md). That handoff is currently STOPPED; the new ADR-0022 contract and documentation synchronization do not authorize implementation. Frontend, later RAG phases, Ideaverse, deployment and blanket containment-removal remain unauthorized.


## Phase 18 active handoff — 2026-10-18

**Final contract:** [ADR-0022](docs/adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) is `OWNER_APPROVED / CANONICALLY_SYNCHRONIZED`: Immutable Snapshot + Whole-File Consent + SHA-256 NO_CHANGE. It prospectively replaces only the named external-source portions of ADR-0020/P17 and ADR-0021/D18-A. Vault privacy/field rules, accepted Phase 17 history and unaffected E1/reservation/process/producer/publication/quality/resource guarantees remain frozen. D1–D11 are final for this documentation scope.

[ACTIVE](docs/implementation/handoffs/ACTIVE.md) is **P18-DOCUMENT-INDEXING / CHANGES_REQUESTED / STOPPED / INCOMPLETE / NOT_VERIFIED**. Owner authorized exactly one documentation-only canonical synchronization; no code, tests, migrations, experiments, workers, ingestion activation or finding closure. Live-original capability remains **DISABLED / DEFERRED** with historical R7 evidence preserved; new snapshot ingestion is inactive. G1–G4 and FR18-1–11 remain OPEN for independent adjudication; FR18-3, all original retained quality/resource/producer obligations and P18-FINAL remain mandatory.

Preserve credited takeover input/native DOMAIN publication, VECTOR mapping, source-union, generation-specific purge and parser/chunker/geometry repairs. Antigravity rounds remain **2/2 exhausted; no third round or automatic takeover loop**. The historical post-takeover D18-PT-A/B decision pointer is superseded by the owner's final contract decision. **Next gate: independent documentation synchronization review.** No automatic implementation handoff/resumption, commit/push or Phase 18 acceptance follows.

## Future RAG delivery efficiency — owner policy 2026-10-17

For not-yet-started Phases 18–22, follow [proportional RAG delivery guardrails](docs/workflow/rag-phase-delivery-guardrails.md). Codex pre-handoff reviews the **new and applicable existing docs/skills/agents/hooks/source**, explicitly challenges unnecessary scope and test burden, and requests owner approval before changing a contract. The owner allows **two Antigravity remediation + independent re-review rounds per phase**; if still blocked, stop Antigravity and use a separately authorized Codex `$codex-implementation-takeover` session with a separate independent review. No automatic takeover during frozen Phase 17.

Use `python-rag-coding` for Python and `bounded-command-execution` for long commands; ordinary max 20 minutes, **owner-authorized full-suite-only** max 35 minutes in future RAG phases when the invoking CLI permits it. The owner uses Codex Full Access and Antigravity `--dangerously-skip-permissions`; neither gives permission to skip handoff, review, privacy or no-commit rules. Long custom prompts go in [the agent prompt workspace](docs/implementation/agent-launcher-prompt.md) with a short launcher, never in a duplicated long chat prompt. No Phase 18 preparation/implementation is authorized by this workflow-only change.

## Development workflow

The owner no longer writes production implementation.

The default workflow is:

1. Owner approves the next phase concept.
2. ChatGPT prepares next-phase docs/tooling without production implementation.
3. Codex runs `$codex-pre-handoff-review`.
4. Owner commits/pushes preparation after `READY FOR HANDOFF`.
5. Codex runs `$codex-create-handoff`.
6. Antigravity implements/tests with `/antigravity-implement-handoff`.
7. Codex runs `$codex-final-review`.
8. Owner commits/pushes after `READY FOR OWNER COMMIT`.
9. ChatGPT closes/freezes the phase and prepares the next when authorized.
10. Historical milestone reviews run after Phase 3/6/9/12. Phase 15 instead uses the dedicated
    `$codex-backend-audit` workflow and supersedes the former post-15 milestone.

Agents never commit, push, tag, or create/merge PRs.

## Handoff rule

Production implementation must be grounded in the active Codex handoff:

`docs/implementation/handoffs/ACTIVE.md`

Agents must not broaden scope beyond the handoff. If the handoff conflicts with a frozen architecture/database
baseline, stop and report the conflict instead of silently changing the baseline.

## Context/token discipline

Use the smallest context that can safely complete the task.

- Read the active handoff first.
- Prefer exact referenced architecture/docs files over broad repository scans.
- If `graphify` is available and `graphify-out/graph.json` exists, query Graphify before broad grep/read operations.
- Graphify is navigation context, never a source of truth; verify important details in canonical files.
- Avoid re-reading unchanged large files.
- Do not paste DBML, migrations, logs, or whole files into handoffs when a path + precise requirement is sufficient.
- Keep handoffs concise and link to canonical docs instead of duplicating them.

## Git workflow

- One branch: `main`.
- No pull-request workflow.
- Owner commits/pushes.
- Agents must never run `git commit`, `git push`, `git tag`, or equivalent publishing commands.
- Suggested commit messages use Conventional Commits.

## Frozen baselines

The following remain frozen unless the owner explicitly approves an architectural change:

- Database Schema v1
- Module Boundary v1
- Architecture Diagrams v1
- Repository / Package Tree v1.1
- Backend Phase 0 bootstrap baseline
- Backend Phase 1 reference/vault foundation baseline
- Backend Phase 2 authentication/settings foundation baseline
- Backend Phase 3 people foundation baseline
- Backend Phase 4 fiction foundation baseline
- Backend Phase 5 film foundation baseline
- Backend Phase 6 media/location foundation baseline
- Backend Phase 7 account foundation baseline
- Backend Phase 8 knowledge foundation baseline
- Backend Phase 9 collection foundation baseline
- Backend Phase 10 feed/importdata foundation baseline
- Backend Phase 11 finance/journal/personal foundation baseline
- Backend Phase 12 search foundation baseline
- Backend Phase 13 shared REST/API + HTTP exposure baseline
- Backend Phase 14 portability/object-storage/operational closure baseline
- Backend Phase 15 accepted backend audit/remediation and closure baseline
- Backend Phase 17 accepted Local RAG Foundation (Alternative B, E1 and Practical Local Offline)

A frozen-baseline change requires an ADR update/new ADR and synchronized affected docs.

## Module invariants

- Each application module owns its entities, repositories, and internals.
- Other modules use only exposed public APIs/named interfaces.
- Never import another module's `internal` package.
- Never share JPA repositories across module boundaries.
- `vault` and `reference` have no application-module dependencies.
- `search` is a leaf/orchestration module.
- Prevent module dependency cycles.
- Consult `docs/architecture/module-dependency-matrix.md` before new cross-module dependencies.

## Backend conventions

- REST/JSON + OpenAPI when HTTP work begins.
- Never expose JPA entities from controllers.
- Use explicit API DTOs.
- Flyway defines the physical schema; Hibernate auto-DDL is not the schema source of truth.
- Store timestamps in UTC.
- Media binaries stay outside PostgreSQL.
- Use SLF4J; never log passwords, PINs, JWTs, refresh tokens, secrets, or sensitive payloads.
- Lombok is allowed for boilerplate reduction, but do not use `@Data` on JPA entities.
- For JPA entities prefer targeted annotations such as `@Getter` and protected no-args construction; keep equality,
  association handling, and state transitions deliberate.
- `@RequiredArgsConstructor` is acceptable for stateless Spring services with final dependencies.

## Testing

Antigravity owns implementation tests for the active handoff.

Preferred tools:

- JUnit 5
- AssertJ
- Mockito where isolation adds value
- Spring Modulith Test / `@ApplicationModuleTest`
- Testcontainers PostgreSQL
- Spring Boot `@ServiceConnection` where useful
- MockMvc when HTTP work exists

Do not use H2 as a PostgreSQL substitute.

## Scope discipline

**Owner-authorized 2026-10-13 exception:** Phase 17 RAG foundation preparation was reviewed (`READY FOR HANDOFF`) and owner committed/pushed
before Phase 16 Frontend, after the frozen Phase 15 gate. Preserve original phase numbers. The
supplemental backend maintenance was separately accepted and owner committed/pushed; the closeout
resets `ACTIVE.md` to `NO_ACTIVE_HANDOFF`. See accepted ADR-0019 and
`docs/implementation/phase-17/`. This authorizes Codex to create the **separate Phase 17 foundation
handoff after owner commit/push of closeout docs**, not production code, DB migration, Java
dependency changes, Python runtime or Ideaverse integration without that handoff.


Do not implement deferred scope without an explicit Codex handoff:

- frontend;
- RAG;
- production deployment;
- multi-user support;
- 2FA/passkeys;
- unrelated refactors.
