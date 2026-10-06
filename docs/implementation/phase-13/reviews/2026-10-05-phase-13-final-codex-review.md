# Phase 13 Final Codex Review

Date: 2026-10-05

Result: **CHANGES_REQUESTED**

Scope: implemented active handoff `phase-13-rest-api`, submitted as `IMPLEMENTED_AWAITING_CODEX_REVIEW`.
Baseline: owner preparation commit `b3d91a5`; implementation and handoff/status changes are uncommitted.
This is an implementation review, not authority to reopen frozen domain/schema/module baselines.

## Verification

- Antigravity supplied `docs/implementation/phase-13/test-evidence.md`: clean verify, 854 tests, zero
  failures/errors/skips, 02:23, finished 2026-10-05T19:20:39+07:00.
- Independent Codex `mvn -f backend/pom.xml -ntp clean verify`: **BUILD SUCCESS**, 854 tests, zero
  failures/errors/skips, 02:15, finished 2026-10-05T19:31:06+07:00.
- `git diff --check` passed. A green build does not establish the omitted contract assertions below.
- Review-only synthetic probes were compiled/run under ignored `backend/target/final-review-diagnostics/`,
  using the existing Spring test context and a separate PostgreSQL Testcontainer. No production/test source
  was changed. Probe cleanup affected only its disposable test database; no real vault data was used.
- Graphify guided navigation; source, generated OpenAPI and MockMvc results were the evidence authority.

## Blocking findings

### FR13-1 — High — Exception translation exposes private values and throwable messages

References: `backend/src/main/java/com/vhvkhangg/personalprivatevault/ApiExceptionHandler.java:74`–95;
`film/internal/web/advice/FilmExceptionAdvice.java:46`, `people/internal/web/advice/PeopleExceptionAdvice.java:41`
under the same Java root; corresponding module advice using `ex.getMessage()`.

The global handler returns arbitrary exception messages for `NoSuchElementException`, `IllegalArgumentException`
and `IllegalStateException`; unexpected failures log the full throwable/cause chain. Module advice similarly
forwards domain messages containing rejected private fields. A duplicate synthetic film genre produced 409 with
`SYNTHETIC_PRIVATE_NAME` embedded in `error.message`. A synthetic illegal-state message was returned verbatim as
409; a synthetic unexpected private message appeared in the ERROR throwable output. Thus programmer/internal
illegal-state failures are also treated as conflicts without explicit semantic context.

Required correction: use audited safe HTTP messages and explicit owner/context-specific mappings for known
application failures. Unexpected failures must remain generic 500, with privacy-safe structural logging rather
than raw exception objects/messages/causes. Do not change frozen exception/domain behavior. Add captured-log and
response regression tests using private/credential/import/SQL sentinels, covering both global and module advice.

### FR13-2 — Medium — Generated OpenAPI contradicts public security and actual statuses

References: `OpenApiConfiguration.java:33`, `authentication/internal/web/controller/AuthController.java:44`–77,
all controller response documentation, and `web/OpenApiRouteInventoryIntegrationTest.java` in the test root.

Generated OpenAPI has global `security: [{"bearerAuth": []}]`, but public login has no operation-level security
override. `@Operation(security = {})` did not emit `security: []`, so public auth operations inherit bearer security.
Generated `POST /api/v1/film-genres` documents only 200 although the actual response is 201. Login documents only
200; required error responses are absent. The OpenAPI test checks the scheme and path prefixes, not these facts.

Required correction: make all five public operations explicitly anonymous in the generated document; retain
bearer security elsewhere. Document actual 201/200 and relevant error/envelope schemas and media types, with
validation matching real DTOs. Assert concrete generated operation security/status/schema/ID coverage, not just
scheme presence or path prefixes.

### FR13-3 — Medium — Auth DTO constraints reject accepted frozen-domain inputs

References: `authentication/internal/web/dto/BootstrapRequest.java:11`–23, `LoginRequest.java:10`, and frozen
`authentication/internal/application/bootstrap/BootstrapService.java:69`–102.

HTTP bootstrap adds username length 3–50 and email maximum 255, whereas the frozen operation accepts trimmed
nonblank usernames through 100 characters and email through 320. The probe's username `ab` was rejected by HTTP
with 400, then accepted by the unchanged `BootstrapOperations` with the same remaining valid inputs. Login's
identifier bound likewise prevents accepted longer email identities from reaching the operation.

Required correction: align structural auth DTO constraints with accepted application semantics; do not add
unapproved username/email policy. Preserve password/PIN checks in their canonical owner and audit other web DTO
bounds for the same drift. Add success/boundary tests proving equivalent accepted inputs remain reachable via HTTP.

### FR13-4 — Medium — Pagination silently rewrites invalid input and Search has the wrong default

References: `knowledge/internal/web/controller/KnowledgeController.java:139`–152,
`media/internal/web/controller/AlbumController.java:76`–84,
`search/internal/web/controller/GlobalSearchController.java:48`.

Knowledge/Media clamp invalid limits, and Media clamps negative offset to zero. The probe's vocabulary-due requests
with limits -1 and 101 both succeeded with 200 instead of exposing the invalid request. Search defaults to 20;
the probe confirmed `meta.page.limit = 20`, while the accepted HTTP default is 50.

Required correction: validate structural bounds consistently and return safe 400 for invalid values without
rewriting them. Use default 50, preserve stricter accepted limits and Search's offset <= 500/limit <= 100, and
keep existing operations unchanged. Test default/min/max/out-of-range limits and offsets, actual image windows,
Search filter/page pass-through, and truthful/absent metadata.

### FR13-5 — Medium — Association verbs and the concrete capability inventory are incomplete

References: `film/internal/web/controller/FilmController.java:73`–101 and 122–136,
`fiction/internal/web/controller/FictionController.java:68`–82,
`people/internal/web/controller/CreatorGroupController.java:60`, and the Phase 13 exposure matrix.

Accepted set-assignment conventions use nested PUT, but these association assignments use POST. Film credits
have create/list routes but no route for the existing `FilmCreditOperations.find(Long)` resource lookup.
The evidence lists controller families rather than the required concrete route/operation/DTO inventory, so
it cannot demonstrate that every permitted capability is exposed or that integration-only capabilities were
deliberately excluded.

Required correction: align assignment verbs across the surface, expose the missing accepted credit lookup with
Optional->404, and record an exact route-to-operation/DTO inventory with explicit exclusion rationale. Check other
existing lookups (for example genre/category name lookups) against the approved matrix instead of declaring
complete exposure from one route per family. Do not invent domain operations or association removals.

### FR13-6 — Medium — Tests/evidence do not prove the required acceptance contract

References: `web/ApiResponseEnvelopeAndErrorIntegrationTest.java`, `authentication/AuthWebIntegrationTest.java`,
`web/OpenApiRouteInventoryIntegrationTest.java`, `ApplicationArchitectureTests.java:909`–918 in the test root,
and `docs/implementation/phase-13/test-evidence.md`.

The eight shared-envelope tests do not exercise 422, 403 or generic 500, although the evidence claims 422/403
coverage. `jsonPath(...).doesNotExist()` passes for both omitted and null fields, so it does not prove required
present null envelope fields. Auth lacks successful bootstrap/login/rotation/PIN flows and missing/invalid/expired
bearer coverage across the required protected surface. Pagination and semantic/OpenAPI cases found above are not
covered. The entity-signature architecture check erases `ResponseEntity<ApiResponse<Entity>>` to `ResponseEntity`
and therefore cannot detect a nested entity wire type. The evidence inventory rows sum to 838, not the claimed 854.

Required correction: complete the handoff's risk-based negative/boundary/security/privacy/OpenAPI/architecture
tests, including actual key presence/nulls, void-200/no-204, generic 500 and controlled 403. Inspect generic/nested
wire signatures and DTO ownership/entity leakage, not only erasures. Preserve baseline tests, rerun focused tests
then full clean verify, and produce accurate reconciled totals, route/exception coverage, diagnostic classification
and limitations. Do not claim tests or inspections that did not run.

### FR13-7 — Medium — Meaningful new web packages lack required descriptors

References: new owner `internal/web/{controller,dto,mapper,advice}` directories, for example
`account/internal/web/controller/` and `authentication/internal/web/dto/` under the Java root.

All 69 populated new web subpackages lack their own `package-info.java`; descriptors exist only at `internal.web`.
This violates the active handoff and accepted package-hygiene rule that each real package gets a descriptor.

Required correction: add concise ownership/package-purpose descriptors only for real populated packages and
synchronize the repository/package inventory to the actual surface. Do not introduce empty placeholder packages,
new named interfaces or unrelated frozen-package cleanup.

## Other review dimensions and diagnostics

- Existing owner operations/facades remain the business implementation; Knowledge/Collection use parents.
  No foreign-module internal import was found in the new web code. No entity/repository/schema/Flyway/POM/
  dependency-matrix change was introduced; the root allowlist and ADR-0016 exception remain bounded.
- Adapters/static mappers are proportionate; no speculative CRUD framework, interface hierarchy or new module
  was introduced. Mapping repetition is not itself justification for a shared business abstraction.
- Persistence/transactions/races remain delegated to frozen operations and existing regression tests.
  No new production N+1/DB-in-loop path was identified in this risk-focused review; album pagination's extra
  count query is bounded and not grounds for speculative optimization. Test truncation is disposable-test-only.
- Credential/token DTO strings are redacted and exact security matchers preserve stateless bearer behavior.
  FR13-1 concerns error/log payload safety, not a change to password/JWT/refresh cryptography.
- Build diagnostics include inherited Lombok/Unsafe, test-support deprecation, Mockito/ByteBuddy self-attachment,
  JVM sharing and Springdoc exposure notices. The compile also reports new advice deprecated-API use
  (`HttpStatus.UNPROCESSABLE_ENTITY`); use the supported equivalent where appropriate, without blanket suppression.
  No IDE inspection, Spotless, Checkstyle or SpotBugs run is claimed.
- Two inherited filled-directory `.gitkeep` files remain accepted low-priority debt, not an unrelated cleanup
  mandate. New package descriptors are in scope and separately required by FR13-7.
- REST/security/testing/package guidance produced the findings above; no production correction was implemented
  during review. This risk-focused pass is not certification of every untested route/DTO combination.

## Remediation and next gate

Antigravity must remediate FR13-1–FR13-7 within this same handoff, implement the corresponding tests, retain fresh
evidence and resubmit as `IMPLEMENTED_AWAITING_CODEX_REVIEW`. No commit message is provided while blockers remain.

Next step: run Antigravity `/antigravity-implement-handoff` with this report, then rerun `$codex-final-review`.
This is production plus test remediation, not a test-only slice. Phase 14 remains deferred; no milestone is due
after Phase 13.
