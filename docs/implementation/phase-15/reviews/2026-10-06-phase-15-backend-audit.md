# Phase 15 Backend Audit — 2026-10-06, initial audit

Status: **OWNER_DECISION_REQUIRED**

The initial comprehensive non-RAG backend audit is complete: **17 open findings (one High, 15 Medium, one Low)**.
Passing tests do not close these findings. No remediation or implementation handoff has been authorized or started.

## Baseline

- Commit: `6a89a998c512dda27c3e494a3525bf6118ee981e` (`docs(phase-15): prepare comprehensive backend audit`).
  HEAD/local `origin/main` matched and the tracked worktree was initially clean. Publication was verified from the
  local remote-tracking reference, not a fresh remote fetch.
- Phase 14 implementation `3bb3f2e78a38eb66bec955219ced634d3cfddd9d` and closeout
  `0a8f3d101f39dc8cf8f5e1d6dcc46d29e6182606` are present in history; Phase 14 remains complete/frozen.
- Preparation is `READY FOR AUDIT`, P15-1 closed. ACTIVE.md remains **NO_ACTIVE_HANDOFF**, unchanged.
- Java `25.0.2`, executable `C:/Users/VU KHANG/.jdks/openjdk-25.0.2/bin/java.exe`; Maven `3.9.15`.
- Resolved Spring Boot `4.1.1`, Framework `7.0.9`, Modulith `2.1.1`, Security crypto `7.1.1`, Hibernate
  `7.4.5.Final`, Flyway `12.4.0`, PostgreSQL JDBC `42.7.13`, PostgreSQL `18.6`, Testcontainers `2.0.5`.
- Docker Desktop client/server `29.8.1`; suite storage image `minio/minio:RELEASE.2024-11-07T00-52-28Z`.
- Inventory: 1,270 production Java files, 365 production packages, 85 test Java files, 328 tracked Markdown files.
- Three read-only workers covered foundation/domain, authentication/HTTP and later workflows/storage. Completed
  reviews/findings were preserved; unavailable follow-up worker turns did not restart their completed work. The main
  audit completed diagnostics, disposable HTTP probes and report. Source reviews are not represented as test runs.

## Resume and evidence preservation

The last successful executable step before interruption was clean verification with coverage: **920 tests, zero
failures/errors/skips**. The next static command failed before any goal ran because PowerShell split an unquoted
dotted Maven property. Only incomplete diagnostics and the incomplete probe were resumed. Successful verification
and completed source reviews were not restarted.

Ignored root `phase-15-audit-*.log` files and `backend/target/` retain local audit evidence. These are not tracked
release artifacts; a future clean removes target evidence. Important results and exact commands are preserved below.
The Java probe used fresh disposable PostgreSQL and synthetic storage, not development-domain data. Earlier literal
SQL checks used the local development container read-only and queried no user records.

## Evidence executed

Commands ran from `backend/` unless marked root. Versions are resolved actual tools, not proposed upgrades.

| Evidence | Exact command / tool version | Result / notes |
| --- | --- | --- |
| Full build/tests + coverage | `mvn -ntp -l ../phase-15-audit-verify.log clean org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.15:report` | Exit 0, BUILD SUCCESS, **920/0/0/0**, 03:05, finished 22:13:23 +07; preserved, not rerun. |
| Compiler/runtime diagnostics | Same verification output/log | Lombok/Unsafe, Byte Buddy dynamic-agent and JVM class-sharing warnings reviewed; not compiler-defect findings. No IDE evidence supplied. |
| Interrupted static attempt | Failure output retained in `phase-15-audit-static.log` | Exit 1 before goals: unquoted dotted property became lifecycle phase `.outputFile=...`; corrected command quoting, not production code. |
| Dependencies/classpath | `mvn -ntp -l ../phase-15-audit-dependencies.log org.apache.maven.plugins:maven-dependency-plugin:analyze "-DfailOnWarning=false" org.apache.maven.plugins:maven-dependency-plugin:build-classpath "-Dmdep.outputFile=target/phase15-classpath.txt"` | Exit 0; resolved plugin **3.10.0**. Bytecode warnings triaged, not automatic dependency deletions. |
| SpotBugs | `mvn -ntp -l ../phase-15-audit-spotbugs.log com.github.spotbugs:spotbugs-maven-plugin:4.10.3.0:spotbugs` | Exit 0; **238 reports** in target/spotbugsXml.xml. Report generation is not a clean check. |
| PMD/CPD | `mvn -ntp -l ../phase-15-audit-pmd.log org.apache.maven.plugins:maven-pmd-plugin:cpd org.apache.maven.plugins:maven-pmd-plugin:pmd "-DfailOnViolation=false"` | Exit 0; plugin **3.28.0**, PMD **7.17.0**; **47 PMD diagnostics, 68 clone blocks**. |
| Spelling/terminology | `Get-Command codespell,cspell,python,python3 -ErrorAction SilentlyContinue`; `python -m codespell --version` | No codespell/cspell executable or Python codespell module. Manual terminology/current-status review substituted; no automated spelling pass claimed. |
| Coverage navigation | JaCoCo **0.8.15**, same verification | 808 classes analyzed. Covered/missed: lines **10,901/3,422**, branches **3,545/3,113**, instructions **50,163/16,855**. Navigation, not acceptance thresholds. |
| Modulith/schema/storage/HTTP | Included in full verification | Architecture, frozen schema manifest, PostgreSQL domain/concurrency, real MinIO/storage/export and 214-operation inventory tests passed; not rerun separately. |
| Disposable startup/HTTP/spec | `$auditProbeClasspath = 'target/classes;' + (Get-Content -LiteralPath 'target/phase15-classpath.txt' -Raw -Encoding UTF8).Trim(); & 'C:/Users/VU KHANG/.jdks/openjdk-25.0.2/bin/java.exe' --class-path $auditProbeClasspath target/phase15-audit/Phase15AuditProbe.java` | Initial run failed on fresh-database startup (BA15-1). Scratch-only explicit Flyway migration then allowed the previously incomplete probes to finish, exit 0. Not proof normal startup succeeds. |
| OpenAPI | Disposable embedded-server GET `/v3/api-docs`, PowerShell JSON inspection | 200; **155 paths, 214 operations, 254 schemas**, all operations have summaries; semantic gaps BA15-15. Captured target/phase15-audit/openapi.json. |
| Numeric boundaries | Root: `docker exec postgres psql -U ppv -d personal_private_vault -X -c "SELECT version(), CAST(0.00001 AS numeric(19,4)) AS rounded_price;"`; separately `docker exec postgres psql -U ppv -d personal_private_vault -X -c "SELECT CAST(1000000000000000 AS numeric(19,4));"` | Read-only literal checks: **0.0000**, and numeric overflow. Preserved, not rerun. |
| Repository/docs/hooks | Root: `& './backend/target/phase15-audit/AuditRepositoryChecks.ps1'; git diff --check; python .agents/hooks/test_repository_safety.py` | **328 Markdown files, zero unresolved file targets**, whitespace pass, **13 hook tests pass**. Helper under ignored target only. Links checked file existence, not anchors/external URLs. |
| Diagrams | PowerShell XML inspection of Draw.io/SVG; DOT/DSL/current matrix source review | Module diagram agrees on **19 nodes, 38 edges**; portability leaf present with no application dependencies. No diagram edits. |

Helper environment: Python **3.11.7**, Windows PowerShell **5.1.26100.9549**. No permanent tooling configuration,
suppression or dependency was added. Graphify navigation used vocabulary-expanded
`security openapi persistence transaction validation module`, BFS depth 2/budget 700; material conclusions were
verified in current source, never inferred from graph edges alone.

### Captured HTTP/runtime probes

Actual runtime Spring **7.0.9**, Security crypto **7.1.1**, PostgreSQL **18.6**:

| Probe | Observed result |
| --- | --- |
| Password encoder, 72 ASCII chars/bytes | Accepted; verification true |
| 73 ASCII, 128 ASCII, 40 `é` chars / 80 UTF-8 bytes | Each rejected with IllegalArgumentException |
| POST import execution with `{"itemDecisions":[null]}` | **500 INTERNAL_ERROR** |
| POST diary with `Content-Type: text/plain` | **500 INTERNAL_ERROR**, HttpMediaTypeNotSupportedException |
| POST diary with domain-valid 300-character title | **400 VALIDATION_ERROR** |
| Bootstrap with 73-character password | **400 INVALID_ARGUMENT**; app_users stayed empty |
| Binary first-read failure, object length 1 | **500**, Content-Length **1**, body **1 byte** |
| Binary first-read failure, object length 8192 | **500**, Content-Length **8192**, body **117 bytes** |
| Upload 8192 bytes, PUT sizeBytes=1, GET content | PUT **200**; GET **200**, Content-Length **1**, body **1 byte** |

The scratch `canonicalJson` flag mistakenly tested nonexistent top-level timestamp instead of meta, so its value is
**not used** as envelope evidence. The captured raw lengths independently prove framing/truncation defects.

## Audit coverage

PASS means no additional actionable issue identified in the inspected dimension, not exhaustive absence proof.
All 17 mandatory dimensions were covered; limitations are explicit.

| Dimension | Result | Coverage |
| --- | --- | --- |
| 1. Build/compiler/dependencies/static | FINDINGS | Clean verification/available tools complete; runtime integration BA15-1. Spelling/IDE limitations below. |
| 2. Business/domain correctness | FINDINGS | Reference/Vault/People/Fiction/Film/Media/Location/Account, four Knowledge/three Collection capabilities, Feed/Import/Finance/Journal/Personal/Search/Portability reviewed; BA15-2/9/10/12/13/14/17. |
| 3. SOLID/cohesion/coupling | PASS | Public capability/owning persistence boundaries coherent; no justified layer-collapse/refactor finding. |
| 4. Pattern fitness | PASS | Facades, orchestration, provider port/adapter, parsers, repositories and transaction boundaries serve current needs. |
| 5. Overengineering/YAGNI | PASS | No harmful speculative hierarchy established; one implementation does not make a real provider/module/test boundary redundant. |
| 6. Duplication/competing policy | FINDINGS | CPD mostly module-owned mechanics; concrete divergent DTO/domain policy BA15-2. |
| 7. Validation/errors | FINDINGS | Null/default/blank/length/numeric/reference/error paths; BA15-2/4/5/6/11/12. |
| 8. Logging/exception/privacy | FINDINGS | Constraint/error redaction and cleanup inspected; no secret leakage identified. Client failures logged as unexpected server failures, BA15-4/5/6/11. |
| 9. REST/OpenAPI | FINDINGS | Route/status/envelope/security inventory plus source adapters/generated schemas; BA15-2/4/5/6/7/8/14/15. |
| 10. Authentication/security | FINDINGS | JWT/bootstrap/refresh/PIN/settings/filter/actuator/upload/export reviewed; BA15-3. No reliable CVE scan/penetration test executed. |
| 11. Persistence/schema/query | FINDINGS | DBML/Flyway/JPA manifest, enums/UTC/constraints/indexes/query ownership and read-only snapshot exception; BA15-1/9/10/11/12/13/17. |
| 12. Transactions/concurrency/I/O | FINDINGS | Upload commit/compensation, streaming/disconnect, repeatable-read export and transitions; BA15-7/8/9/10/13/17. |
| 13. Performance/resources | FINDINGS | Bounded search/snapshot cleanup checked; full import materialization/first-100 workflow gap BA15-14. No speculative EXPLAIN/index allegation. |
| 14. Test sufficiency/reliability | FINDINGS | Suite green but manual setup masks startup failure; specific boundary/wire/mixed-writer gaps attached to findings. |
| 15. Package/file hygiene | FINDINGS | No tracked build/cache/private-key/real-env artifacts identified; missing descriptors/stale placeholders BA15-16. |
| 16. Docs/spelling/diagrams | FINDINGS | Canonical status/ADR/schema/matrix/diagram review and 328 file-link checks; BA15-15/16. Historical evidence preserved; manual spelling substitute. |
| 17. Configuration/operations | FINDINGS | YAML/env examples/Compose, readiness versus liveness/storage validation inspected; BA15-1. Production topology deferred. |

## Findings

Java shorthand **J/** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; **T/** = corresponding test root.
All findings are **OPEN** and **OWNER_DECISION_REQUIRED**. The owner's instruction gates any frozen-baseline touch,
including narrow repairs intended to restore existing documented behavior. Corrections below are proposals, not
authorizations; none implies new dependency edges, schema changes or cross-module repository access.

### BA15-1 — Normal startup does not apply Flyway migrations

- Severity: **High**. Category: runtime/configuration/test isolation.
- Evidence: backend/pom.xml declares Flyway core/PostgreSQL but the resolved classpath lacks Spring Boot Flyway
  integration. application.yml enables Flyway and Hibernate validate. Unmodified app on empty PostgreSQL failed
  **missing table [addresses]**. T/support/AbstractPostgresIntegrationTest.java:22 explicitly calls migrate before
  Spring starts, masking the missing runtime integration in all integration tests.
- Consequence: fresh-database startup fails; configured application startup cannot apply pending migrations.
- Required correction: smallest compatible Boot-managed Flyway integration repair; no Hibernate auto-DDL or SQL edits.
- Verification/regression: production-config empty PostgreSQL startup without manual migrator; V1/V2 history,
  Hibernate validation and safe restart.
- Frozen baseline impact: **owner approval required** — Phase 0 frozen build/runtime baseline.

### BA15-2 — HTTP validation competes with owning-domain contracts

- Severity: **Medium**. Category: validation/compatibility/duplication.
- Evidence: Phase 13 requires preservation of existing rules. Concrete examples under J/:

  | HTTP request DTO | Accepted owning contract |
  | --- | --- |
  | account/internal/web/dto/CreateExternalAccountRequest.java:13; UpdateExternalAccountRequest | Mandatory username versus ExternalAccountService's username OR externalId OR URL. |
  | media/internal/web/dto/CreateImageRequest.java; UpdateImageRequest | Required metadata/checksum, title/type limits versus frozen nullable metadata-only image contract/ImageService. |
  | location/internal/web/dto/CreateAddressRequest.java; UpdateAddressRequest | Required nullable locality/street; type 50 versus 100, postal 20 versus 32. |
  | journal/internal/web/dto/CreateDiaryEntryRequest.java; UpdateDiaryEntryRequest | Title 255 versus DiaryService 500; NotBlank versus preserved non-null blank Markdown. Real title=300 HTTP rejection. |
  | knowledge/internal/web/dto/ note/study/information/vocabulary create/update DTOs | 500-character title/source/progress/word fields capped at 255; blank Markdown rejected; part-of-speech 100 capped at 50. |
  | collection/internal/web/dto/ music/shopping/software create/update DTOs | 500-character names/titles capped at 255; required Music version versus null→ORIGINAL default. |

- Related drift: album titles; film/fiction genre/location-category names; Account display/owner metadata; Personal
  phone/email; Feed resource title/metadata; Import filename; Finance name/optional description; Settings upper limits.
  These are one bounded DTO-to-owning-validator comparison, not unrelated refactors.
- Consequence: valid domain/imported records cannot be created or round-tripped through HTTP; schema describes
  competing policy.
- Required correction: align affected null/default/blank/length/range DTO rules to accepted owning validators, or
  explicitly retain narrower HTTP contracts with rationale. No generic validation framework or weaker domain rules.
- Verification/regression: HTTP/schema tests at real maxima/max+1, null defaults/optional identity combinations and
  exact Markdown preservation; retain genuine domain rejection semantics.
- Frozen baseline impact: **owner approval required** — accepted Phase 13 compatibility versus older frozen rules.

### BA15-3 — Documented password range exceeds encoder input range

- Severity: **Medium**. Category: authentication/credential compatibility.
- Evidence: J/authentication/internal/application/bootstrap/BootstrapService.java:93 accepts 12–128 characters;
  authentication/internal/infrastructure/security/SecurityConfiguration.java:40 uses default delegating bcrypt.
  Actual crypto 7.1.1 rejected 73/128 ASCII chars and 40 `é` chars (80 UTF-8 bytes); bootstrap returned 400.
- Consequence: documented valid passwords fail, with surprising character-versus-byte behavior.
- Required correction: owner chooses full-range encoder with old bcrypt verification compatibility, or explicitly
  narrows the password contract to UTF-8 byte limits. Never truncate silently.
- Verification/regression: 12/72/73/128 characters, multibyte boundaries, old hash login, bootstrap/login and privacy.
- Frozen baseline impact: **owner approval required** — Phase 2 credentials/hash compatibility.

### BA15-4 — Null collection members pass validation and crash mappers

- Severity: **Medium**. Category: structural validation/errors.
- Evidence: J/importdata/internal/web/dto/ExecuteImportJobRequest.java:9 lacks element non-null validation;
  ImportDataWebMapper dereferences each member. Real `[null]` produced 500 before job lookup. Same shape in
  CreateFollowerSnapshotRequest, Finance create/update transaction/rule entry lists and ReplaceBusinessHoursScheduleRequest.
- Consequence: malformed client structure becomes noisy 500.
- Required correction: element-level non-null validation/safe mapping, preserving supported null/empty list semantics.
- Verification/regression: null-element requests in each family return canonical 400 without writes; distinguish
  list null/empty from null members, particularly unknown schedules.
- Frozen baseline impact: **owner approval required** — frozen Phase 13 validation/error behavior.

### BA15-5 — Unsupported content types become generic 500

- Severity: **Medium**. Category: framework HTTP translation.
- Evidence: J/ApiExceptionHandler.java:167 generic catch; real diary POST text/plain returned
  500 INTERNAL_ERROR for HttpMediaTypeNotSupportedException.
- Consequence: client negotiation mistakes lose HTTP semantics and generate server-failure logs.
- Required correction: canonical 415 translation; inspect analogous unsupported Accept/406 handling, not yet runtime-probed.
- Verification/regression: unsupported Content-Type/Accept, supported JSON and accurate error schemas;
  preserve committed binary abort behavior.
- Frozen baseline impact: **owner approval required** — shared HTTP status compatibility.

### BA15-6 — Feed/Import omit public Knowledge exception translation

- Severity: **Medium**. Category: cross-module errors/transactions.
- Evidence: J/feed/internal/application/SavedResourceConversionService.java and importdata/internal/application/ImportJobService.java
  delegate to KnowledgeOperations. KnowledgeFacadeService converts nested failures to public InvalidKnowledgeItemException,
  KnowledgeConflictException and KnowledgeNotFoundException. KnowledgeExceptionAdvice is scoped to Knowledge web;
  feed/internal/web/advice/FeedExceptionAdvice.java and importdata/internal/web/advice/ImportDataExceptionAdvice.java
  omit these exceptions. Invalid dual-author Study conversion/duplicate imported Note hash therefore reach generic 500.
  This is source-path evidence, not an extra runtime-probe result.
- Consequence: ordinary invalid/conflict/missing inputs lose 422/409/404 semantics despite rollback.
- Required correction: owning advice maps public exceptions safely; no Knowledge-internal import/catch-and-commit.
- Verification/regression: invalid author, duplicate hash/missing reference, safe errors and complete rollback of
  target/conversion/provenance/job writes.
- Frozen baseline impact: **owner approval required** — frozen workflow HTTP failure behavior.

### BA15-7 — Binary failure preserves stale Content-Length on JSON error

- Severity: **Medium**. Category: transport/error framing.
- Evidence: J/media/internal/web/controller/ImageController.java:157 sets binary length before streaming;
  uncommitted failure/advice does not clear it. Real first-read errors: 500 length=1/body=1 and
  500 length=8192/body=117. Phase 14/api-architecture requires canonical JSON before commitment.
- Consequence: truncated/misframed errors cannot be reliably decoded/completed by real clients.
- Required correction: clear binary headers/buffer only when uncommitted before JSON translation; retain post-commit
  abort/no-JSON-append and idempotent cleanup.
- Verification/regression: embedded HTTP lengths smaller/larger than error JSON, exact framing/envelope, first-read
  versus committed failure/disconnect/timeout. MockMvc body assertions alone are insufficient.
- Frozen baseline impact: **owner approval required** — Phase 14 frozen binary transfer implementation.

### BA15-8 — Mutable metadata overrides actual object length

- Severity: **Medium**. Category: binary integrity/source of truth.
- Evidence: J/media/internal/web/controller/ImageController.java:157 prefers metadata size to known provider size;
  ImageService permits editing metadata. Disposable upload 8192 bytes→PUT sizeBytes=1 (200)→GET returns 200/one byte.
- Consequence: ordinary metadata edits silently truncate content.
- Required correction: known provider length first, metadata fallback only when provider length unknown; preserve
  metadata-edit/content-type contracts. A new mismatch/409 rejection is a separate owner choice, not implied.
- Verification/regression: original bytes and exact wire length after smaller/larger/null size edits, fallback/closure.
- Frozen baseline impact: **owner approval required** — Phase 14 download compatibility.

### BA15-9 — Location scalar writes bypass schedule serialization

- Severity: **Medium**. Category: mixed-writer concurrency/coherence.
- Evidence: J/location/internal/application/LocationService.java:102 ordinary entity read/write versus
  BusinessHoursService parent FOR UPDATE/FOR SHARE. Location lacks version/selective-write safeguard.
  Source-derived interleaving: T1 loads known=false; T2 commits known=true+intervals; T1 scalar edit flushes stale
  known=false. A previously managed entity also needs fresh state after later locking. Not runtime-executed here.
- Consequence: known-state/schedule invariant lost despite schedule-only locking tests.
- Required correction: owner-local coherent mutation serialization/fresh locked state; prefer schema-preserving
  solution, not a version column or repository-wide lock framework.
- Verification/regression: deterministic PostgreSQL scalar/schedule races both directions and prior same-transaction
  read; assert all scalars, known flag and intervals.
- Frozen baseline impact: **owner approval required** — Phase 6 locking/mutation behavior.

### BA15-10 — Concurrent first rating sets race on absent row

- Severity: **Medium**. Category: set semantics/concurrency.
- Evidence: J/vault/internal/application/metadata/VaultMetadataService.java:140 reads optional Rating then saves
  assigned-PK new Rating without upsert/serialization. Two readers observe absent; second insert waits then violates
  PK after first commits. Source-derived deterministic scenario; existing rating tests are sequential.
- Consequence: concurrent valid set requests can produce a database/server failure.
- Required correction: narrow atomic upsert/parent serialization preserving createdAt/update-grade semantics;
  no retry inside an already rollback-only transaction.
- Verification/regression: barrier-controlled PostgreSQL first-set race, both completion results/one row;
  update/removal/capability/trash behavior preserved.
- Frozen baseline impact: **owner approval required** — Phase 1 metadata mutation behavior.

### BA15-11 — Category assignment exposes missing-reference FK errors

- Severity: **Medium**. Category: reference validation/errors.
- Evidence: J/location/internal/application/LocationCategoryService.java:116 validates null IDs only before
  LocationCategoryAssignmentRepository.insertIfAbsent. Missing parent/category hits native FK; owning advice lacks
  safe translation and generic handler returns 500. Source/SQL/advice evidence, not separately runtime-probed.
- Consequence: ordinary invalid references appear as backend failures.
- Required correction: owning validation/specific race-safe referential translation retaining ON CONFLICT behavior;
  no blanket integrity mapping or unrelated absent-parent empty-list read change.
- Verification/regression: missing references/removal race, duplicate/concurrent set, safe errors/no partial rows.
- Frozen baseline impact: **owner approval required** — Phase 6/13 reference rejection behavior.

### BA15-12 — Price validators ignore numeric(19,4) representability

- Severity: **Medium**. Category: precision/validation/persistence.
- Evidence: J/location/internal/application/BrandService.java and LocationService,
  knowledge/study/internal/application/StudyItemService.java, collection/shopping/internal/application/ShoppingService.java,
  collection/software/internal/application/SoftwareService.java check sign/currency but not width/scale.
  Frozen DBML/JPA/Flyway numeric(19,4); literal SQL proves 1000000000000000 overflow and 0.00001→0.0000.
  Study broad integrity translation may mislabel overflow as conflict.
- Consequence: late failure or different response versus reloaded amount.
- Required correction: width validation/safe error; owner chooses reject excessive fractional precision versus
  documented normalization/rounding before response/persistence. No column widening/currency-specific new rule.
- Verification/regression: every affected largest representable/overflow/five-decimal amount, response/reload equality
  and accurate errors.
- Frozen baseline impact: **owner approval required** — accepted amount behavior, especially reject versus round.

### BA15-13 — Account edits can invalidate linked YouTube Study

- Severity: **Medium**. Category: cross-module invariant/ownership.
- Evidence: frozen DBML:1924–1926 requires YOUTUBE_CHANNEL type/YouTube platform.
  J/knowledge/study/internal/application/StudyItemService.java:300 checks assignment only;
  account/internal/application/ExternalAccountService.java:133 freely edits platform/type.
  Source-derived sequence: valid linked Study→account becomes another type/platform→invalid persisted Study link;
  unchanged Study update now fails. Not runtime-probed here.
- Consequence: previously valid cross-module invariant is invalidated by referenced-owner mutation.
- Required correction: owner chooses guard/coordination preserving Knowledge→Account direction, or explicitly accepts
  assignment-time-only policy. No Account→Knowledge edge/cross-module JPA/export-exception misuse.
- Verification/regression: sequential/concurrent mutation versus assignment, unchanged account updates, public errors
  and Modulith cycle/boundary checks.
- Frozen baseline impact: **owner approval required** — cross-module behavior/possible architecture decision.

### BA15-14 — Imports above 100 items cannot be fully reviewed through HTTP

- Severity: **Medium**. Category: workflow completeness/resources/API policy.
- Evidence: J/importdata/internal/application/ImportJobService.java:116 materializes all parsed items without
  a 100 cap; execute requires decisions for all. ImportJobController.findItems allows limit≤100; service always
  PageRequest.of(0, limit); no next-page/per-item route. A valid 101-item job cannot expose its final item for review.
- Consequence: accepted job size exceeds HTTP review capability; complete jobs are also materialized in memory.
- Required correction: owner chooses bounded complete inspection, explicit ingestion cap, or accepted first-N debt;
  no silent new API/product feature or rejection of previously valid imports.
- Verification/regression: 100/101 jobs/chosen boundary, complete decision coverage/order, atomic execution and
  realistic resource bound for the selected policy.
- Frozen baseline impact: **owner approval required** — intentional Phase 13 first-N versus Phase 10 import behavior.

### BA15-15 — OpenAPI omits non-obvious request/domain semantics

- Severity: **Medium**. Category: semantic API documentation/tests.
- Evidence: generated 214 summaries but no operation descriptions. Absence alone is not a defect:
  J/search/internal/web/controller/GlobalSearchController.java q/tagId lacks literal matching/limits/all-tags semantics;
  Finance schemas omit amountDelta sign/cardinality, transfer/category and frequency-specific fields;
  Collection update schemas omit full-replacement/default/purchase-state clearing semantics implemented by validators.
- Consequence: clients must reverse-engineer code/phase docs to construct valid requests or interpret filters.
- Required correction: targeted existing-rule descriptions; synchronize BA15-2/5 decisions, not redundant prose for
  every self-evident ID or a changed contract under documentation work.
- Verification/regression: generated-spec assertions for named semantic/error families against actual validators.
- Frozen baseline impact: **owner approval required** — documentation additions in frozen HTTP annotations only.

### BA15-16 — Missing ownership descriptors, stale placeholders and architecture entry prose

- Severity: **Low**. Category: repository hygiene/current canonical docs.
- Evidence: ten meaningful Collection packages lack package-info: collection/internal/application and each
  music/shopping/software internal/application, internal/domain, internal/infrastructure/persistence.
  Repository tree/rules require these descriptors. Two stale tracked placeholders coexist with real content:
  backend/src/main/resources/db/migration/.gitkeep and backend/src/test/java/com/vhvkhangg/personalprivatevault/.gitkeep.
  docs/architecture/README.md:9–10 still defers migrations/ApiResponse shape despite implemented canonical contracts.
- Consequence: incomplete ownership documentation and contradictory current architecture entry guidance; explicit
  repository-rule violations, not stylistic preference.
- Required correction: descriptors for existing ownership without new module/named-interface annotations, remove only
  those placeholders, update two superseded statements/links. Preserve historical records.
- Verification/regression: inventory/links/whitespace and unchanged Modulith boundaries.
- Frozen baseline impact: **owner approval required** — frozen package/architecture documentation; hygiene only.

### BA15-17 — Stale lifecycle writers overwrite independently committed state

- Severity: **Medium**. Category: lifecycle/mixed-writer lost updates.
- Evidence: J/journal/internal/application/DiaryService.java:54/105/117 and
  personal/internal/application/PersonalProfileService.java:111/181/193 ordinary reads for scalar update/delete/restore,
  without version/serialization/selective-write safeguard. An active pre-delete entity can flush stale deletedAt=null;
  reverse ordering can overwrite new content with stale deleting-transaction content. FeedItemService.recordFetch
  versus FeedSourceService.update has the analogous timestamp/config writer risk. Source-derived, not runtime-executed.
- Consequence: concurrent normal operations undo deletion or lose edits/configuration without intent.
- Required correction: narrow schema-preserving owning serialization/selective updates for these identified pairs;
  no new version column or generic locking framework without separate approval.
- Verification/regression: deterministic PostgreSQL update/delete/restore and fetch/config races, both commit orders;
  exact Markdown, deletion timestamps, self-profile uniqueness and feed configuration preserved.
- Frozen baseline impact: **owner approval required** — Phase 10/11 lifecycle/transaction behavior.

## Observations / tool limitations

- SpotBugs: constructor-throw **98**, exposed representation **65+53**, possible null **19**, broad catch **2**,
  redundant nullcheck **1**. Reviewed null diagnostics predominantly repeat validated accessors/Optional/exception
  metadata; constructors deliberately enforce invariants. Representation warnings include request containers and
  injected collaborators; canonical copy/snapshot boundaries reviewed, no concrete shared-state corruption proven
  by those flags. Parsing/export broad catches implement failure/cleanup boundaries. No blanket suppression/rewrite.
- PMD diagnostics mostly imports/conditionals/ternaries/qualifiers. Unused FinancialTransactionService.loadWithEntries,
  Markdown local title and snapshot columnType parameter are small dead-code observations, not independently material
  defects. No style-cleanup handoff.
- Largest CPD blocks (135–140 lines) are module-owned search mechanics. Text similarity alone does not justify
  frozen-edge changes/new common module/base SQL framework. Concrete competing validation is BA15-2.
- Dependency-analyze cannot infer all starters/runtime/service-loader needs; PostgreSQL/Flyway/SpringDoc/AWS HTTP/
  test-starter warnings are not deletion evidence. No reliable CVE scanner ran; no vulnerability-free attestation or
  broad dependency-upgrade recommendation.
- Maven summary is **920**; retained **81 Surefire XML files sum to 907**, discrepancy not reconciled. This report
  quotes the captured zero-failure Maven summary, not fabricated independent XML accounting.
- No IDE inspection, production load test, planner benchmark, external-link/heading-anchor check or automated spelling
  pass. Source/manual terminology, integration/schema/static/wire/file-target checks provide bounded substitute evidence.
- Historical parse rollback-to-CREATED/retry, metadata-controlled content type, uncertain provider/DB reconciliation,
  and public health versus private readiness remain accepted tradeoffs, not new findings.
- Earlier Vault repeat-trash timestamp concurrency concern remains an unestablished independent contract violation:
  retained source observation, not authority for blanket locks.
- Scratch Testcontainers deprecation and intentionally induced committed-transfer abort warnings are not production
  defect evidence. SpringDoc enabled warnings reflect the currently accepted documentation surface.
- DOT/Draw.io/export agree on 19 modules/38 edges including portability. High-level C4/Structurizr and planned
  frontend/feed-network/backup functions are not implemented-feature inventories. No diagram/schema/index change
  justified merely by taste.

## Frozen-baseline decisions

No repair decision is recorded. **All BA15 findings are OWNER_DECISION_REQUIRED** under the owner's conservative
instruction. Prior Phase 14 concept/ADR-0017 preparation approval is not Phase 15 remediation authority.

Record approval/rejection and scope, or named accepted-debt rationale, for these groups:

1. **BA15-1:** minimal runtime Flyway integration repair retaining SQL/validate, or explicitly external migration prerequisite.
2. **BA15-2:** align HTTP to owning contracts, or retain narrower rules individually with rationale.
3. **BA15-3:** full accepted character range with compatible hashing, or explicit UTF-8 byte-limited credentials.
4. **BA15-12:** reject versus documented normalize/round plus representable-width validation.
5. **BA15-13:** approved invariant guard/coordination without reverse edge, or assignment-only accepted debt;
   architecture alternatives require a scoped ADR decision.
6. **BA15-14:** complete bounded inspection, ingestion cap, or accepted >100 review limitation.
7. **BA15-4/5/6/7/8/9/10/11/17:** approve narrow error/transport/concurrency integrity repairs without schema,
   ownership/dependency or unrelated behavior changes, or disposition individually.
8. **BA15-15/16:** documentation/hygiene-only correction, retaining semantics/module annotations.

Expanding a proposed correction returns to the owner. This report does not approve any option.

## Final disposition

**OWNER_DECISION_REQUIRED**. No Critical issue established; fresh-database startup is High. This is not
BACKEND_AUDIT_READY or a Phase 15 closeout. Only report/audit-status/current-phase governance summaries change.
Production code/tests/POM/config, ADRs, migrations/DBML, diagrams and ACTIVE.md remain unchanged. Scratch target/log
artifacts remain ignored. No commit/push/tag/PR; Phase 16/17 preparation remains deferred.

Final verification: `git status --short; git diff --check; git diff --stat; git diff --name-only -- backend/src backend/pom.xml backend/compose.dev.yml docs/adr docs/database docs/architecture docs/implementation/handoffs/ACTIVE.md; git rev-parse HEAD origin/main`
confirmed only nine governance summaries plus this new report changed, whitespace clean, no protected-path edits,
and unchanged HEAD/local origin/main. Targeted PowerShell link/count checks on the ten changed/new documents found
**79 resolvable links**, 17 finding headings and the stated severity totals; no stale unstarted audit summary remained
in the checked current entry documents. No full/static test run was repeated for these documentation-only edits.

Next step: owner records bounded approval/rejection or accepted-debt rationale for the eight groups, then reruns
`$codex-backend-audit` to consolidate authorized findings into the single permitted remediation workflow.
