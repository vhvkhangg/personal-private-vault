# DP-B1 — Prospective file boundary, frozen-contract impacts and governance

**Status:** [D7–D11 Option A OWNER_RATIFIED_FOR_PREPARATION](owner-ratification-d7-d11-2026-11-12.md); prospective implementation boundary **not an executable handoff or Owner-approved frozen source allowlist**. Independent pre-handoff review pending. Source baseline `main` `6374537074afc32ed7e939cf94ed2f21d77c5f42`.

## Candidate owner-local code areas requiring separate authorization

| Owning component | Prospective authorized minimum, only after B1 gate | Forbidden without new scope |
| --- | --- | --- |
| Authentication SecurityConfiguration | Add exactly the two Fiction/Film direct detail GET matchers and six Reference GET matchers for `PROFILE_PUBLIC` **or** `PROFILE_PRIVATE`; retain the default PRIVATE fallback. No session/JWT algorithm change | Wildcard `fictions/**`, `films/**`, `reference/**`, global `authenticated()`, `permitAll`, expanding JWT privilege |
| Fiction `internal/web` | Profile-conditioned direct detail GET, new `PublicFictionResponse`, mapper and accurate OpenAPI; no full DTO exposure to Public | Genre/link/classification controller grants or new Fiction browse API |
| Fiction `internal/application` and owning persistence | Read-only eligibility/predicate and consistent snapshot using existing permitted `VaultEntryOperations` capability if supported; no domain mutation | Query another module's internals, Vault reverse dependency, arbitrary join exception or schema visibility flag |
| Film `internal/web` | Profile-conditioned direct detail GET, new `PublicFilmResponse`, mapper/OpenAPI | Credits/links/genre/tag endpoint grant, Return `FilmResponse` to Public |
| Film `internal/application` and owning persistence | Same safe per-ID eligibility and read consistency | Film Credit direct ID exposure, People/Media internals or public writes |
| Reference `internal/web` | Reuse existing six GET handlers/DTOs; no new endpoint. If no code needed, omit owning source edit | Platforms/archetypes/world-settings GETs, Reference writes or unvalidated free text |
| Existing tests/fixtures | Narrow B1 HTTP/security/consistency/contract cases, reuse real DP-A persisted tokens, existing Testcontainers/Modulith/OpenAPI | Mock auth bypass; unrelated end-to-end browser/framework/dependency |
| Architecture/API/ADR docs | Exact prospective HTTP/DTO/security exceptions only, with source-verified descriptions | Historical frozen ADR rewrite, Flyway/DBML migration without new Owner approval |

**Phase/frozen exception:** Any modification to previously accepted Phase 4 Fiction, Phase 5 Film, Phase 1 Reference, Phase 2 Authentication Security, Phase 13 HTTP DTO/OpenAPI, or related named-interface/architecture documentation requires a separate **explicit Owner source/frozen-boundary authorization** after independently ready preparation. The already accepted DP-A authorization does not automatically extend to these changes. Do not edit a frozen source file in this preparation phase.

## Minimum implementable plan (future, after all approvals)

1. Apply the [Owner-ratified D7–D11 Option A](owner-ratification-d7-d11-2026-11-12.md); reconcile any material source drift since this HEAD. Do not request a repeat ratification of unchanged choices.
2. Independent Codex `$codex-pre-handoff-review` of B1 route/field matrix, source evidence, safe query/ownership mechanism, compatibility, finite acceptance. Close mandatory findings; `READY_FOR_HANDOFF` is preparation-only.
3. Owner commits/pushes independently accepted preparation, then **separately** approves exact source/DTO/OpenAPI/security/frozen exceptions and any additional module-index/schema modifications if justified.
4. Codex `$codex-create-handoff` for **DP-B1 ONLY**, with concrete file allowlist, owned method names, pseudocode/query consistency choice, scope-appropriate verification gates and STOP conditions; `ACTIVE` changes only under that approved handoff.
5. Assigned implementer follows handoff, runs tests, self-review, submits immutable receipts and handback; a different independent reviewer adjudicates actual implementation. Owner alone stages/commits/pushes if READY.
6. After publication, perform a separate B1 slice closeout using actual new SHA. **Do not** automatically authorize B2 or Recycle Bin.

## Docs Sync Check for this preparation

- `UPDATED`: only **new** files under `docs/implementation/maintenance/2026-11-12-bf26-dp-b1-preparation/`, preserving the published DP-A navigation and existing historical files.
- `ALREADY_CONSISTENT`: verified `main` source baseline, accepted DP-A, existing `NO_ACTIVE_HANDOFF`, D1–D6 and D7–D11 Option A ratified, default provider flags OFF, Frontend pause, H1-F/FR-13, RAG suspension and all counters.
- `DEFERRED_OUT_OF_SCOPE`: current-root `AGENTS.md`, Owner policy, roadmap, maintenance index, ACTIVE notices **until** Owner/independent B1 preparation transition warrants navigation synchronization; no source edit; B2/Recycle Bin/final audit not triggered.
- `BLOCKED_OWNER_DECISION`: D7–D11 decisions are resolved; **only** any future exact frozen source exception or safe-query cross-module exception not covered by published boundaries requires a separate Owner decision.
- `NOT_VERIFIED`: no runtime/HTTP/SQL/OpenAPI/IDE tests, schema execution, live provider/browser validation, B1 implementation or its review.

**Governance:** No new active handoff, no new branch/worktree, no tests/commands in the Owner checkout, no runtime activation, no agent staging/committing/pushing, no consumed Antigravity implementation/remediation/takeover round. Publication of a preparation draft must not be mistaken for source authority. Avoid editing any accepted DP-A review or prior failing/timeout receipt.
