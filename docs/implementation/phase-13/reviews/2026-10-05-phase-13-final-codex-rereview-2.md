# Phase 13 Final Codex Re-review 2 — 2026-10-05

Result: **CHANGES_REQUESTED**

Scope: active handoff `phase-13-rest-api`, returned as `IMPLEMENTED_AWAITING_CODEX_REVIEW` after remediation of
the [previous re-review](2026-10-05-phase-13-final-codex-rereview.md). Owner preparation baseline is still
`b3d91a5dead618fa1ea1fc14c7798d77e6fe143f`. Codex changed review/status documentation only, not production or
implementation test sources. Phases 0–12, Schema v1 and the module graph remain frozen.

## Independent verification and confirmed progress

- `mvn -f backend/pom.xml -ntp clean verify`: **BUILD SUCCESS**, exit 0; **865 tests**, 0 failures, 0 errors,
  0 skipped; 02:30 min; finished `2026-10-05T21:16:46+07:00`.
- `git -c safe.directory='C:/Users/VU KHANG/IdeaProjects/personal-private-vault' diff --check`: exit 0.
- All 211 controller method/path pairs across 26 controllers match the evidence's 211 method/path rows.
  This closes the route-name/omitted-route portion of FR13-5, not its operation/DTO mapping requirement.
- Surefire evidence reconciliation is confirmed: 79 XML reports, suite-attribute sum 852, actual `<testcase>`
  element count 865. The five documented per-suite differences total 13; the 79 inventory rows now sum to 865.
  There is no remaining execution-count discrepancy finding.
- Saved-resource manual/feed-item creation now documents 201, not 200; vocabulary review documents 422.
  Collection music credits now use PUT, not POST, with repeated-assignment/one-credit regression coverage.
- Both private-PIN endpoints now reject missing/malformed/expired bearer tokens in tests. The expanded auth
  matrix verifies public auth method pairs, wrong-method protection, selected business routes and Swagger access.
  The configured access-denied handler's JSON writer is now directly tested as a component.
- The four previously closed findings FR13-1/FR13-3/FR13-4/FR13-7 remain closed. Privacy-safe static messages,
  structural 500 logging, compatible auth bounds, strict pagination and all 70 populated web leaf descriptors
  remain intact; Modulith and generic entity-signature checks pass.

A review-only diagnostic under ignored `backend/target/final-review-diagnostics/` used the existing Spring/MockMvc
infrastructure and a separate disposable PostgreSQL container. Its application context/container were stopped;
it printed only structural status/code information, not credentials, tokens or private payloads. It confirmed:

```text
saved-resource manual/feed-item documented POST responses include 201, 409 and 422
PUT /api/v1/vault/entries/{id}/favorite documented responses: 200, 400, 401, 403, 404, 500
synthetic person creation: 201; move its Vault entry to trash: 200
favorite that trashed entry: actual HTTP 409, code VAULT_CONFLICT
```

## Remaining blocking findings

### FR13-2 — Medium — Mutation name heuristics still omit real Vault conflict responses

References under `backend/src/main/java/com/vhvkhangg/personalprivatevault/`:
`OpenApiConfiguration.java` (`isMutation`), `vault/internal/web/controller/VaultController.java`,
`vault/internal/web/advice/VaultExceptionAdvice.java`, and frozen
`vault/internal/application/metadata/VaultMetadataService.java` (`assertNotDeleted`).

Creation-status and vocabulary-review corrections are accepted. However, the customizer still decides relevant
errors by operation-name prefixes. `favoriteVaultEntry`, `unfavoriteVaultEntry`, `attachVaultEntryTag`,
`detachVaultEntryTag` and `removeVaultEntryRating` miss its mutation rule. For example PUT favorite on a trashed
entry reaches the existing state check and correctly returns 409 `VAULT_CONFLICT`, but its generated OpenAPI
operation has no 409 response. The independent diagnostic reproduced this exact mismatch.

Consequence: consumers still cannot rely on the published error contract for permitted state-sensitive mutations,
despite claims that all mutation errors are covered. Extending prefixes for just the last reported example has
not constituted a complete deliberate owner-error audit.

Required correction: document relevant real errors for the complete accepted surface using explicit operation
semantics/annotations or a checked complete mapping; do not infer completeness from a prefix list. Include the
Vault metadata actions above and verify generated responses against their existing advice/state checks. Preserve
correct runtime 409 behavior and frozen domain code. Retain the corrected creation/public-security behavior.

### FR13-5 — Medium — Complete route paths still map to invented public contracts and DTOs

Reference: `docs/implementation/phase-13/test-evidence.md`, route-to-operation/DTO table, and the actual owner
controllers/public interfaces under the Java source root.

The Collection verb and complete 211 method/path inventory are accepted. The target-operation/DTO columns remain
unreliable, so the handoff's required concrete capability mapping is not complete. Examples:

- Auth status/bootstrap actually use `BootstrapOperations.isBootstrapped()`/`bootstrap(...)`, not the table's
  nonexistent `SessionOperations.isBootstrapped()`/`bootstrapVault(...)`. Refresh/revoke use
  `SessionOperations.rotate(...)`/`revoke(...)`, not `rotateRefreshToken(...)`/`revokeAllUserTokens(...)`.
- The table names nonexistent `ReferenceOperations`, `FinanceOperations`, `VaultTagOperations`,
  `AccountOperations`, `RelationshipOperations`, `FollowerHistoryOperations`, `FeedOperations`,
  `ImportDataOperations` and `JournalOperations` interfaces. For example Reference uses `ReferenceCatalog`,
  Finance uses `WalletOperations` and the other capability interfaces, and Vault tags use `VaultMetadataOperations`.
- Settings uses `read()`/`initializeOrUpdate(...)`, not `getAppSettings()`/`updateAppSettings(...)`.
- Request DTOs `AddPersonRoleRequest`, `RecordVocabularyReviewRequest`, `ReplaceBusinessHoursRequest` and
  `SetRelationshipRequest` do not exist. The actual types include `AddRoleRequest`, `VocabularyReviewRequest`,
  `ReplaceBusinessHoursScheduleRequest` and `SetExternalAccountRelationshipRequest`; verify exact source names
  for every row rather than relying on these examples alone.

Consequence: an owner cannot audit whether each route delegates to a permitted existing capability or identify
the actual wire types from the accepted evidence. These are factual errors, not alternative naming preferences.

Required correction: reconcile all operation and request/response DTO columns with the actual controller
declarations and canonical public contracts. Preserve the now-correct paths/verbs; do not create invented
interfaces, DTOs or business operations to satisfy the table. This remaining portion of FR13-5 is docs-only.

### FR13-6 — Medium — Search pass-through test does not bind filters; exception evidence is fictional

References under `backend/src/test/java/com/vhvkhangg/personalprivatevault/`:
`search/SearchWebIntegrationTest.java` (`searchPassesThroughFiltersAndNonZeroOffset`),
`web/OpenApiRouteInventoryIntegrationTest.java`, `web/ApiResponseEnvelopeAndErrorIntegrationTest.java`,
and `docs/implementation/phase-13/test-evidence.md` (exception mappings and remediation claims).

The new Search test submits `domains`, `entryTypes` and `tagIds`. The accepted/controller parameter names are
`domain`, `entryType` and `tagId`; the submitted plural parameters are unbound and ignored. Empty data and an
`isBoolean()` assertion for `hasMore` cannot detect lost filters, lost results or changed page semantics. Thus
the claimed repeated-filter/result pass-through proof still does not exist, although the production adapter
continues constructing the correct `GlobalSearchQuery` from the singular parameters.

The exception inventory likewise contradicts source. For example global parameter errors both return
`MALFORMED_REQUEST`, not `PARAM_TYPE_MISMATCH`/`MISSING_PARAMETER`; there is no global `IllegalStateException`
409 mapping. Vault advice handles `IllegalStateException` and `DataIntegrityViolationException`, not the listed
`VaultNotFoundException`/`VaultValidationException`/`VaultConflictException`. Knowledge maps
`InvalidKnowledgeItemException` to 422 `KNOWLEDGE_INVALID`, not the invented validation/SRS exceptions and codes.
Auth uses `UserAlreadyBootstrappedException` and `AUTH_INVALID_PIN`, among other differences. Audit every owner,
not only these examples.

Other partial tests must be described accurately: calling `accessDeniedHandler.handle(...)` directly proves its
writer, not traversal through `ExceptionTranslationFilter`/the configured chain. The OpenAPI test still checks
family prefixes plus selected operations, not the required exact exposure/status/DTO manifest. Existing exact
nullable-envelope and void-200 tests are accepted; the limit-only addition should assert present-null `meta`
explicitly instead of only `jsonPath(...).doesNotExist()`.

Required correction: use the singular repeated parameters with discriminating seeded results or captured
`GlobalSearchQuery` assertions; prove a nonzero page's results and exact `hasMore` pass through unchanged.
Verify exact method/path, stable nonempty operation ID, DTO and relevant status coverage from the accepted
manifest, including FR13-2's Vault cases. Exercise a controlled filter-chain denied path without changing
production authorization policy, or clearly distinguish component-only coverage and satisfy the handoff's
filter-level check separately. Rebuild exception/code/status/message evidence from real advice and explicitly
record generic fallbacks/limitations. Preserve the now-reconciled 865 execution evidence and all baseline tests;
rerun focused tests, full clean verify and whitespace check after remediation.

This finding requests tests/evidence corrections, not a production Search or frozen-domain change.

## Review dimensions, diagnostics and scope

- The same owner operations/facades retain business, transaction, persistence and race ownership. No foreign
  internals/entities/repositories, new dependency/module edge, schema/Flyway/POM change or nested external
  controller was introduced. The allowed security change now exposes its existing JSON handlers as beans;
  statelessness, JWT validation and exact anonymous matchers remain unchanged.
- DTOs/static mappers and thin HTTP adapters remain proportionate. No speculative hierarchy/business-rule
  duplication, new production N+1/DB-in-loop issue or broad frozen-baseline refactor was identified. Test-only
  truncation is confined to disposable databases. Deferred frontend/RAG/deployment/worker scope stays excluded.
- Compiler still reports **unchecked/unsafe operations in `OpenApiConfiguration`** despite the implementer's
  elimination claim. Record this accurately and narrow its cause if correcting it; do not apply blanket
  suppression or claim warning-free. Inherited parser/test-support deprecations, Lombok/Unsafe,
  Mockito/ByteBuddy/JVM and Springdoc notices remain diagnostic limitations, not new blocking findings.
  No IDE inspection, Spotless, Checkstyle or SpotBugs run is claimed.
- REST/security/testing/package skills directed the checks. Graphify located relevant files only; canonical
  source, generated OpenAPI, actual HTTP behavior and fresh execution reports established the findings.
  Previous formal review reports are preserved unchanged.

## Next step

Antigravity `/antigravity-implement-handoff` remediates the remaining **FR13-2/FR13-5/FR13-6** portions:
OpenAPI error documentation, truthful inventory and focused tests/evidence. Return
`IMPLEMENTED_AWAITING_CODEX_REVIEW` for `$codex-final-review`. This is not test-only because the OpenAPI production
adapter still needs correction. Do not commit/push; Phase 14 stays deferred.
