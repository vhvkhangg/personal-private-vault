# Phase 13 — Codex Final Re-review 3

- Date: 2026-10-06
- Scope: active handoff `phase-13-rest-api`, submitted `IMPLEMENTED_AWAITING_CODEX_REVIEW`
- Preparation baseline / HEAD: `b3d91a5dead618fa1ea1fc14c7798d77e6fe143f`
- Reviewer: Codex, review-only
- Decision: **CHANGES_REQUESTED**
- Previous report: [final re-review 2](2026-10-05-phase-13-final-codex-rereview-2.md)

The remediation materially improves the previously reported issues. FR13-1/FR13-3/FR13-4/FR13-7 remain
closed. FR13-2 and FR13-6 retain narrower Medium blockers; FR13-5 is reduced to one Low documentation typo.
No production or test-source correction was made by Codex. No commit/push was made or authorized.

## Independent verification

Command from repository root: `mvn -f backend/pom.xml -ntp clean verify`.

1. First run: **BUILD FAILURE**, exit 1; **866 tests**, 1 failure, 0 errors, 0 skips; 02:31 min;
   finished `2026-10-06T07:18:09+07:00`. The failure was
   `RefreshTokenLifecycleIntegrationTest.rejectsTokenAtDatabaseCurrentTimestampBoundary`, line 145:
   expected `InvalidRefreshTokenException`, but rotation returned without throwing.
2. One complete repeat, without source changes, exclusions, automatic failure reruns or skipped tests:
   **BUILD SUCCESS**, exit 0; **866 tests**, 0 failures/errors/skips; 02:13 min;
   finished `2026-10-06T07:26:38+07:00`. The entire nine-test refresh-token lifecycle class passed.
3. `git diff --check`: passed before and after review-document synchronization.

The first failure is preserved, not reclassified as a clean pass. The unchanged baseline test writes database
`now()` as expiry, whereas unchanged `SessionService.rotate` uses the application `Clock`. A later isolated
20-sample diagnostic found the database timestamp 521–2488 microseconds behind the subsequent application-clock
read; it did not reproduce or establish the cause of the earlier failure. This is an observed intermittent
baseline boundary failure, not evidence of a Phase 13 authentication regression. Do not weaken expiry checks,
add arbitrary sleeps, skip the test, or silently change frozen authentication. Preserve this observation in
the next evidence package; if it recurs, report the failure and obtain explicit scope before any frozen-code fix.

Repeat-run reconciliation: **79 XML reports**, **853** summed suite `tests` attributes, **866** actual testcase
elements, no failures/errors/skips. The documented 13 nested-execution difference and 79-row / 866-test inventory
are correct. The run includes **45 HTTP tests** and four added architecture rules, over the 817-test baseline.
Application architecture (31 tests), Knowledge/Collection architecture and the physical-schema manifest passed.

Review-only diagnostics used an isolated Testcontainers database and ignored `backend/target` artifacts, not
owner data. The settings probe observed:

```text
settingsDocumented404=false
settingsActualStatus=404 errorCode=SETTINGS_NOT_FOUND
```

## Remaining findings

### FR13-2 — Medium — Settings Optional-to-404 response omitted from OpenAPI

Location: `backend/src/main/java/com/vhvkhangg/personalprivatevault/OpenApiConfiguration.java:203`;
`settings/internal/web/controller/SettingsController.java:34`.

The new GET error heuristic explicitly excludes paths ending `/settings` from 404 documentation. However,
`getSettings()` deliberately returns 404 `SETTINGS_NOT_FOUND` when `AppSettingsOperations.read()` is empty.
The isolated authenticated probe confirmed runtime 404 and absent `paths./api/v1/settings.get.responses.404`.
Generated clients and the published contract therefore omit a supported initialization-state outcome.

Required correction: document this real 404 using the canonical error schema and finish the deliberate
relevant-response audit against controller Optional/error behavior. Do not change correct runtime/domain
behavior to fit the documentation. Add a regression pairing uninitialized-settings HTTP 404 with its OpenAPI
response. Preserve the corrected creation statuses and Vault mutation 409 documentation.

Accepted progress: all 40 controller creation operation IDs match the explicit creation set; saved-resource
201 responses remain correct. HTTP-method mutation classification and the new Vault favorite/rating/tag
assertions close the previously identified omitted Vault 409 cases.

### FR13-6 — Medium — Exact contract manifest and repeated-filter proof remain incomplete

Locations: `backend/src/test/java/com/vhvkhangg/personalprivatevault/web/OpenApiRouteInventoryIntegrationTest.java:61,147`;
`search/SearchWebIntegrationTest.java:108`.

The OpenAPI test still checks route-family prefixes, a count of 211 unique IDs, and selected endpoint statuses.
It does not compare the exact accepted method/path manifest or each operation's expected success/request/response
DTO schema. Replacing an unasserted route with a different route in the same family could keep it green.
The real settings-response omission above also passed this test. Counting operations and checking that three
error components exist does not prove the required complete contract.

The improved Search test now uses correct singular parameter names, real seeded results, distinct pages and
exact `hasMore` values. However, it sends only **one value** for each of `domain`, `entryType`, and `tagId`.
Only the two films carry the selected tag; the Person and third film do not. Consequently the same assertions
would pass if both domain and entry-type filters were discarded, because the tag already excludes those rows.
It does not yet prove repeated binding or independent pass-through of all three filter sets.

Required correction: add an exact expected method/path/operation/status/DTO manifest assertion grounded in the
accepted exposure inventory, including the settings 404 regression and all creation success statuses. Add a
small repeated-value Search adapter test that proves all received filter sets reach `GlobalSearchQuery` unchanged
(captured arguments are acceptable), or independently discriminating result cases. Keep the existing real
Search page/tag integration checks and truthful limit-only metadata checks. Record actual coverage limitations
and both verification outcomes; do not claim complete repeated-filter coverage from singleton requests.

Accepted progress: authenticated `FilterChainProxy.doFilter` with downstream `AccessDeniedException` now proves
the security chain's canonical 403 handling. Present-null limit-only `meta` is explicitly asserted. Previously
flagged global parameter, Vault and Knowledge exception inventory mappings are corrected; XML/count evidence
is reconciled. PIN negative/public-auth method-matrix/Swagger/void-null coverage remains accepted.

### FR13-5 — Low — One remaining inventory interface-name typo

Location: `docs/implementation/phase-13/test-evidence.md:24,345`.

`GET /api/v1/personal/profiles/self` is still attributed to nonexistent `ProfileOperations.findSelfProfile()`.
The controller injects `personal.profile.PersonalProfileOperations` and calls its `findSelfProfile()`.
Correct this name in the table and remediation summary; do not create another interface.

A mechanical comparison of all **211 rows** against all **211 actual controller mappings**, dependency-method
calls, request DTO names and response generic signatures found this single discrepancy. The earlier broad
invented-contract/DTO issue and Collection PUT/idempotency issue are otherwise corrected. This isolated typo
is not a production blocker by itself.

## Other review dimensions / limits

- Corrected adapters still delegate to owner public contracts; no new domain operations, foreign repositories,
  entity signatures, nested Knowledge/Collection HTTP owners, module edges or frozen-schema changes were found.
  The reviewed tracked source changes remain the authorized root/security/configuration/architecture-test slice;
  new HTTP adapters and tests stay in handoff scope.
- Privacy-safe static errors, structural unexpected-error logging and redacted auth diagnostics remain accepted.
  No new security/privacy blocker was found in the remediation.
- Owner transaction/concurrency behavior remains unchanged. No new blocking hot-path, persistence, SOLID,
  speculative-pattern or package-hygiene issue was found in this narrowed re-review. This is not a load benchmark.
- Compiler notices remain for unchecked OpenAPI schema operations, inherited deprecated APIs, Lombok `Unsafe`
  and Mockito/ByteBuddy dynamic agents. The unchecked notice is now acknowledged in evidence; no IDE inspection
  or warning-free claim is made by this review. Graphify was navigation only; findings were verified in source
  and, for the settings mismatch, runtime.
- Historical reports/submissions are preserved. Current handoff/status/evidence links are synchronized to this
  decision. Phase 14 remains deferred; Phase 13 is not ready to commit or freeze.

## Next step

Antigravity `/antigravity-implement-handoff`: correct the remaining OpenAPI response, focused contract/filter
tests and evidence typo within the active handoff, run full clean verification without baseline exclusions,
and return `IMPLEMENTED_AWAITING_CODEX_REVIEW` for `$codex-final-review`. This is not test-only remediation
because production OpenAPI configuration still needs correction. Do not commit/push.
