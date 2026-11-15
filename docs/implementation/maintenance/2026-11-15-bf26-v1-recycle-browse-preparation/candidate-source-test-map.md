# BF26-V1 — candidate frozen-source/API/test file map (not permission)

**Status: PROPOSAL / OWNER_EXACT_SOURCE_AUTHORIZATION_REQUIRED AFTER INDEPENDENT REVIEW.** No file listed here is editable merely because the Owner selected docs-only preparation.

`J/` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` = `backend/src/test/java/com/vhvkhangg/personalprivatevault/`. Candidate map aims to reuse existing Vault named interface, immutable view and web mapper without expanding dependency edges.

## Proposed production candidate map

| ID | Path | Minimal intended change | Qualification |
| --- | --- | --- | --- |
| P01 | `J/vault/entry/VaultEntryOperations.java` | Add one bounded trash-page read method | Existing named `entry` interface |
| P02 | `J/vault/internal/application/entry/VaultEntryService.java` | Validate page size/cursor, invoke one read-only bounded repository query, assemble immutable result/nextCursor | Preserve all trash/restore/guards; avoid transaction/isolation change |
| P03 | `J/vault/internal/infrastructure/persistence/VaultEntryRepository.java` | Owner-local `deletedAt IS NOT NULL`, `id DESC` keyset with DB-bound `limit+1` | Must verify generated SQL, active filtering, count/offset avoidance |
| P04 | `J/vault/internal/application/entry/VaultTrashCursor.java` **NEW** | Owner-local versioned strict bounded cursor codec | Parent package exists; avoid shared abstraction or raw details in HTTP error |
| P05 | `J/vault/view/VaultTrashPageView.java` **NEW** | Immutable page of existing `VaultEntryView` plus nullable nextCursor | Parent named `view` package exists; no entity/lazy values |
| P06 | `J/vault/internal/web/controller/VaultController.java` | Add exactly one PRIVATE-only GET `/trash`, canonical envelope, safe header/validation | No existing ID route/restore mutation change |
| P07 | `J/vault/internal/web/dto/VaultTrashItemResponse.java` **NEW** | Explicit three-property `id,entryType,deletedAt` response | Prevent five-field existing DTO or feature content leakage |
| P08 | `J/vault/internal/web/dto/VaultTrashPageResponse.java` **NEW** | `{items,nextCursor}` immutable typed envelope data | Preserve `ApiResponse`/`ApiMeta` rather than altering shared types |
| P09 | `J/vault/internal/web/mapper/VaultWebMapper.java` | Pure page/item mapping only | Existing `VaultEntryResponse` unchanged |

**Potential CONDITIONAL additional path, not yet granted:** `J/OpenApiConfiguration.java` (root) **only if** actual generated OpenAPI fails to produce an accurate schema/security response for the single new route with controller/DTO annotations. The pre-handoff reviewer must inspect current customizer and declare whether it is truly necessary; do **not** silently include or modify it. Source change beyond exact future allowlist requires STOP and another Owner decision.

Possible simplification for reviewer: keep cursor encode/decode entirely as a small private helper inside P02 if creating P04 is unjustified; retain identical validation and test behavior. Alternatively a single page response record with nested immutable item may be acceptable, but prevent accidental private/metadata disclosure and preserve package conventions. **The final qualified map belongs to independent pre-handoff review**, not this draft's count.

## Proposed test candidate map

| ID | Path | Mandatory new-path evidence |
| --- | --- | --- |
| T01 | `T/vault/VaultEntryIntegrationTest.java` | Real PostgreSQL keyset repository, 3+ page stable order, all types, active exclusion, limit+1, restore/retrash inter-page and no writes |
| T02 | `T/vault/VaultWebIntegrationTest.java` | PRIVATE HTTP 200, exact three-key data, cursor/limit 400s, cache/no-store, empty/exhaustion, existing lifecycle unchanged |
| T03 | `T/authentication/ProfileRouteAuthorizationIntegrationTest.java` | Exact original eight PUBLIC business GET pairs, both persisted PUBLIC origins 403/no-handler for new route, anonymous 401, PRIVATE positive, ASYNC/ERROR/security sweep regression |
| T04 | `T/web/OpenApiRouteInventoryIntegrationTest.java` | Exactly one new GET and unique op id, true generated schema/envelope/params/errors, existing manifest still correct |

Read-only regression executables (not write permission): `T/vault/VaultMutationConcurrencyTest.java`, `T/vault/VaultMetadataIntegrationTest.java`, `T/ApplicationArchitectureTests.java`, appropriate `SecurityFilterChainIntegrationTest` and feed cursor tests only if relevant. The reviewer must decide if additional/fewer test edits are needed based on actual Java assertions; no guessed fixed testcase count.

## Explicitly frozen / out of scope

- `SecurityConfiguration.java`, session issuance, Public allowlist and profile authority; no ninth PUBLIC business GET.
- `VaultEntry.java`, existing `VaultEntryView` and five-property `VaultEntryResponse` unless a later, exact approved exception becomes demonstrably necessary; no schema migration or DBML change.
- Global `ApiResponse`, `ApiResponses`, `ApiMeta`, `ApiPageMeta`, `ApiExceptionHandler`, global Jackson/filters/transaction manager.
- Existing Vault mutation/source edit/reservation/lock/restore semantics; no permanent deletion or purge.
- People/Fiction/Film/Reference/Search/Feed/Drive/Finance/RAG/Frontend, feature repositories, title/name enrichment, providers, workers.
- New dependencies, shared cursor service, generic pagination framework, new module/package descriptors, hooks/agents, source-branch/worktree creation.
- No direct feature or Vault internal cross-module repository calls outside Vault owner.

## Stop rules and approval sequence

1. Before source authorization, independent reviewer verifies current Git SHA, exact blobs, collision-free NEW paths and existing package-info parents, module boundaries, fixture/test mapping and root OpenAPI needs; reports any material product ambiguity (ID ordering, three-field wire, auth, branch).
2. Owner reviews the qualified preparation and approves **exact paths and frozen Phase 1/13 exception**, plus any pending product contract choice, **separately**. A pre-handoff `READY_FOR_HANDOFF` verdict is not an implementation grant.
3. Only after applicable publication/Owner approval may Codex create an executable handoff. No `/antigravity-implement-handoff`, self-review, remediation, runtime tests or agent commits now.
4. If implementation later discovers another required path, STOP for exact additional Owner permission; do not infer permission from this candidate map.
