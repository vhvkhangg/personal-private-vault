# Phase 13 Final Codex Re-review — 2026-10-05

Result: **CHANGES_REQUESTED**

Scope: active handoff `phase-13-rest-api`, returned as `IMPLEMENTED_AWAITING_CODEX_REVIEW` after Antigravity
remediation of the [initial final review](2026-10-05-phase-13-final-codex-review.md). Preparation baseline remains
owner commit `b3d91a5`. No frozen domain, schema, dependency graph or production implementation was changed by Codex.

## Independent verification

- `mvn -f backend/pom.xml -ntp clean verify`: **BUILD SUCCESS**, exit 0; **861 tests**, 0 failures, 0 errors,
  0 skipped; 02:12 min; finished `2026-10-05T20:38:04+07:00`.
- `git -c safe.directory='C:/Users/VU KHANG/IdeaProjects/personal-private-vault' diff --check`: exit 0.
  The one-command ownership exception avoids changing global Git configuration.
- A review-only Java diagnostic in ignored `backend/target/final-review-diagnostics/` used the existing
  Spring/MockMvc test infrastructure and a separate disposable PostgreSQL 18.6 container. It confirmed:

```text
/api/v1/saved-resources/manual documented POST responses: 200, 400, 401, 403, 500
/api/v1/saved-resources/feed-item documented POST responses: 200, 400, 401, 403, 500
saved-resource manual creation actual HTTP: 201
collection music credits documented methods: GET, POST
PUT /api/v1/collection/music/999/credits actual HTTP: 405
```

The diagnostic prints no credentials, tokens or private response payloads; its application context and isolated
container were stopped. It is not a substitute for committed implementation regression tests.

## Finding disposition

| Finding | Re-review disposition |
| --- | --- |
| FR13-1 — High | Closed: static domain error messages and structural unexpected-failure logging remove the reported payload leaks; captured sentinel test passes. |
| FR13-2 — Medium | Open: generated OpenAPI still disagrees with real creation responses. |
| FR13-3 — Medium | Closed: auth identity bounds now match the frozen contracts; short username and long email success are tested. |
| FR13-4 — Medium | Closed: pagination clamping is removed; invalid Knowledge/Media/Search bounds and Search default 50 are tested. |
| FR13-5 — Medium | Open: one set-assignment verb remains incorrect and the capability inventory is inaccurate/incomplete. |
| FR13-6 — Medium | Open, substantially improved: required route/security/pass-through evidence and reconciled inventory remain missing. |
| FR13-7 — Medium | Closed: all 70 populated web leaf packages have descriptors; no new named interface is introduced. |

## Remaining blocking findings

### FR13-2 — Medium — Creation/error documentation still uses an incomplete naming heuristic

References under `backend/src/main/java/com/vhvkhangg/personalprivatevault/`:
`OpenApiConfiguration.java:108`, `feed/internal/web/controller/SavedResourceController.java:46–59`,
`knowledge/internal/web/controller/KnowledgeController.java:128`, and the generated `/v3/api-docs`.

The customizer recognizes creation only by `create*`, `bootstrapVault` or `convertSavedResource*` operation IDs.
Actual `saveManualResource` and `saveFeedItemResource` return `ApiResponses.created`, but their published success
response remains 200 with no 201. The independent authenticated manual creation returned 201. Relevant 409/422
responses are likewise inferred from method-name prefixes rather than owner semantics; for example vocabulary
review can reach `KnowledgeExceptionAdvice`'s 422 mapping but its `reviewVocabularyItem` ID misses that rule.

Consequence: consumers/generated clients receive an inaccurate contract even while the OpenAPI test is green.
Public `security: []` and shared error component registration are corrected; those parts do not need rework.

Required correction: document each real operation's success and relevant owner errors explicitly or through a
complete, deliberate mapping that does not guess semantics from names. Keep reusable common security/error
schema setup. Verify generated responses for every creation route, including both saved-resource routes, and
representative non-CRUD actions with domain errors. Do not rename frozen domain operations or change correct
runtime 201 responses merely to fit the customizer.

### FR13-5 — Medium — Collection assignment remains POST; exposure inventory invents routes/contracts

References: `collection/internal/web/controller/CollectionController.java:79–86`, frozen
`collection/music/internal/application/MusicService.java:102–119`, Phase 13 README association conventions,
`docs/architecture/api-architecture.md:296`, and `docs/implementation/phase-13/test-evidence.md:170–260`.

Music-person credits delegate to the existing idempotent `insertIfAbsent` set assignment but still expose POST
`/api/v1/collection/music/{id}/credits`. PUT returns 405. The accepted contract requires PUT on association
assignments; fixing Film/Fiction/People/Location alone did not finish the surface review. Film credit creation
is a different existing resource-creation operation and need not be converted to PUT.

The submitted route table cannot prove accepted capability coverage. Concrete mismatches include:

- `/auth/pin` instead of actual `/auth/private-pin`, and nonexistent `AuthenticationOperations`/
  `RefreshTokenOperations` instead of the controller's `SessionOperations`.
- `/creator-groups` instead of `/people/creator-groups`; nonexistent Fiction genre assignment.
- `/media/albums` and `/media/images` instead of `/albums` and `/images`.
- `/knowledge/studies` and `/knowledge/vocabularies` instead of `/knowledge/study` and `/knowledge/vocabulary`.
- `/feed/saved-resources` instead of `/saved-resources`; nonexistent vocabulary conversion.
- `/import-jobs` instead of `/imports/jobs`; `/journal/diaries` instead of `/journal/diary-entries`.
- Invented `VaultOperations`, `ReferenceCatalogOperations` and `FinanceOperations` targets, with omitted real
  routes such as role/credit assignments, reference lookups, vocabulary reviews and finance capabilities.

The table also lacks the required explicit request/response DTO mapping. These documentation defects are not
authority to add nonexistent operations or to rename correctly implemented canonical routes.

Required correction: finish the association-verb audit, fix only the Collection HTTP adapter as needed, and test
repeated assignment with one stored association and 200/null-data responses. Rebuild a concise but complete
method/path → actual public owner operation → request/response DTO inventory from controllers and canonical
contracts. Reconcile every accepted capability/exclusion; remove fictional rows rather than implementing them.

### FR13-6 — Medium — Acceptance tests/evidence still permit the remaining contract drift

References under `backend/src/test/java/com/vhvkhangg/personalprivatevault/`:
`web/OpenApiRouteInventoryIntegrationTest.java`, `web/ApiResponseEnvelopeAndErrorIntegrationTest.java`,
`authentication/AuthWebIntegrationTest.java`, `search/SearchWebIntegrationTest.java`, and Phase 13 test evidence.

The new exact-null/void-200, 422, generic-500/safe-log, auth lifecycle and recursive generic signature checks are meaningful
improvements and pass. Remaining gaps are specific:

- OpenAPI coverage checks family prefixes and two creation examples, not the actual complete method/path/DTO
  inventory. It passes despite both saved-resource statuses and the Collection assignment mismatch above.
- The 403 test throws `AccessDeniedException` in a controller and exercises MVC advice, not the separately
  configured security-filter `AccessDeniedHandler`. Missing/invalid/expired bearer tests cover settings; PIN
  routes only test missing bearer. No complete anonymous-route matrix proves only the five exact method/path
  pairs are public, and Swagger access is not covered alongside existing health/OpenAPI checks.
- Search tests assert metadata types/defaults and invalid bounds against empty results, not repeated
  domain/entryType/tagId filters, a nonzero page, or result/hasMore pass-through. Add a direct limit-only read
  assertion proving no fabricated page metadata; the representative People void-action null/no-204 test passes.
- The evidence has 79 class rows summing to **848**, while its total row says **78 classes / 861 tests**.
  Independent Maven reports 861 successful executions; current 79 Surefire XML reports also sum to 848.
  This discrepancy is not a failed-test claim, but must be explained/reconciled instead of presenting the table
  as exhaustive. The 20 new HTTP suites contain 40 tests; four new architecture tests belong separately in
  the 44-test phase delta, not in a claim of 44 HTTP tests. The claimed exception-mapping inventory is absent.

Required correction: add focused regression checks for the remaining gaps using the existing test stack,
including a controlled filter-level access-denied path without new production authorization policy. Validate
the exact accepted exposure manifest and statuses rather than reproducing the implementation's prefix rules.
Record real exception mappings, test limitations and reconciled execution/suite totals; retain baseline tests,
then rerun focused tests, full clean verify and whitespace checks. No frozen-domain correction is requested.

## Other review dimensions and diagnostics

- Existing operations/facades retain business rules, transaction and race ownership. No new schema, repository,
  entity, Maven dependency, module edge or nested external controller was introduced. Modulith/architecture
  tests remain green; parent Knowledge/Collection delegation and bounded ADR-0016 root types are preserved.
- DTOs/mappers/adapters remain proportionate. No speculative framework/pattern hierarchy or new production
  DB-in-loop/N+1 issue was identified in this remediation review. Test cleanup affects disposable databases only.
- Exact security matchers, stateless JWT/session behavior and redacted auth DTO strings remain intact. The
  outstanding security item concerns required filter/matcher regression evidence, not new cryptography/policy.
- Build still reports inherited Lombok/Unsafe, deprecated parser/test-support, Mockito/ByteBuddy and JVM notices.
  The new `OpenApiConfiguration` also emits unchecked-operation compiler warnings; use properly parameterized
  schemas while correcting FR13-2 where practical, not blanket suppression. Deprecated HTTP status constants
  were corrected. No IDE inspection, Spotless, Checkstyle, SpotBugs or warning-free result is claimed.
- REST/security/testing/package skills shaped these checks. Graphify was navigation only; source and live
  generated HTTP/OpenAPI behavior established the findings. Historical reviews remain unchanged.

## Next step

Antigravity `/antigravity-implement-handoff` remediates **FR13-2, FR13-5 and FR13-6 only** within the active handoff,
then returns `IMPLEMENTED_AWAITING_CODEX_REVIEW` for `$codex-final-review`. This is not a test-only slice because
HTTP/OpenAPI production adapters still require correction. Do not commit/push; Phase 14 remains deferred.
