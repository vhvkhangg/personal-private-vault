# Personal Private Vault — Agent Operating Contract

This file defines repository-wide instructions for coding agents.

## Project state

- Private, permanently single-user personal vault.
- Backend-first modular monolith using Spring Boot and Spring Modulith.
- PostgreSQL + Flyway; Maven build.
- Phase 17 **RAG Foundation (Alternative B / ADR-0020)** is **COMPLETE — FROZEN** after
  [Codex independent final acceptance](docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md)
  and owner implementation commit/push `829d294aca7d916851348ff21004c631915d572e`. Its accepted handoff is
  [archived](docs/implementation/phase-17/handoff.md) and [ACTIVE](docs/implementation/handoffs/ACTIVE.md)
  is `NO_ACTIVE_HANDOFF` in this documentation-only closeout. FR17-1–11 and AB-1–6 are closed for
  bounded Foundation scope; `AUTH-CLOCK` remains OPEN in Authentication under its narrow owner exception,
  which was **not used** by the successful clean gate. Default containment and disabled live external-file
  capability remain active. Frontend Phase 16 and RAG Phases 18–22 remain deferred/owner-gated.

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
ACTIVE to `NO_ACTIVE_HANDOFF`. Independent `mvn clean verify` passed **1,308/1,308** Java tests
(including 154 RAG), Python 37 and safety hooks 13; full reviewer receipts are linked from the
[final review](docs/implementation/phase-17/reviews/2026-10-17-phase-17-alternative-b-independent-final-re-review-3.md).
`AUTH-CLOCK` remains OPEN as a separate frozen Authentication/environment issue (exception unused).
No Phase 18–22, Frontend, Ideaverse, deployment or containment-removal authorization is implied.


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
