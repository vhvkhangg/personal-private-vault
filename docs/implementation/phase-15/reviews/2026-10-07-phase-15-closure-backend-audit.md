# Phase 15 Backend Audit — 2026-10-07, closure re-audit

Status: **REMEDIATION_REQUIRED**

All 17 mandatory dimensions and all 17 prior findings were reviewed against the current implementation.
**13 findings are CLOSED; four Medium remnants remain OPEN: BA15-2, BA15-9, BA15-14, BA15-15.**
They fit the existing bounded [owner decisions](../owner-decisions.md) and
[ADR-0018](../../../adr/0018-phase-15-bounded-backend-remediation.md). No new architectural authority is inferred.
The same [active handoff](../../handoffs/ACTIVE.md) returns to **CHANGES_REQUESTED**; no second handoff is created.
The earlier [final acceptance](2026-10-07-phase-15-final-codex-acceptance.md) remains a historical handoff-specific
acceptance, not comprehensive audit closure or permission to commit.

## Baseline

- HEAD and local `origin/main`: `6a89a998c512dda27c3e494a3525bf6118ee981e`; accepted preparation is committed.
  Publication is checked through the local remote-tracking reference, not a fresh fetch.
- Phase 14 implementation `3bb3f2e78a38eb66bec955219ced634d3cfddd9d` and closeout
  `0a8f3d101f39dc8cf8f5e1d6dcc46d29e6182606` are ancestors of HEAD. Phase 14 remains frozen.
  Preparation remains READY FOR AUDIT. ACTIVE entered this closure invocation READY_FOR_OWNER_COMMIT, satisfying
  the closure exception to the initial audit's NO_ACTIVE_HANDOFF prerequisite.
- Dirty worktree contains Antigravity's authorized Phase 15 implementation/tests/POM and current governance records.
  Existing changes and the unrelated untracked owner ZIP are preserved; its contents were not inspected.
- Java `25.0.2`, Maven `3.9.15`, Windows PowerShell `5.1.26100.9549`, Python `3.11.7`.
  Resolved Boot `4.1.1`, Framework `7.0.9`, Modulith `2.1.1`, Security crypto `7.1.1`, Hibernate `7.4.5.Final`,
  Flyway `12.4.0`, PostgreSQL JDBC `42.7.13`, Testcontainers `2.0.5`, SpringDoc `3.1.1`.
- PostgreSQL `18.6`; Docker client/server `29.8.1`; existing MinIO integration image
  `minio/minio:RELEASE.2024-11-07T00-52-28Z`. No development-vault data was used by the disposable probes.
- Pre-report inventory: **1,282 production Java files, 365 production packages, 99 test Java files,
  340 current Markdown files**. Three read-only workers covered foundation/domain, later workflows, and
  security/HTTP/storage; main independently executed shared tools, runtime probes, generated-spec and repository checks.

## Evidence executed

Commands below ran from `backend/` unless marked root. All tooling is audit-only, with no permanent POM/plugin,
suppression, CI or runtime-configuration change. Existing final-review evidence was retained; this is a fresh
full/static closure run against the changed implementation, not a reinterpretation of the initial 920-test run.

| Evidence | Exact command / actual tool version | Result / notes |
| --- | --- | --- |
| Git prerequisites | Root: `git status --short; git rev-parse HEAD; git rev-parse origin/main; git log -4 --format='%h %s'`; separate `git merge-base --is-ancestor <Phase-14-commit> HEAD` for both commits | HEAD/origin matched; both ancestor checks exit 0. Preparation and historical frozen commits present. |
| Full build/tests/coverage | `mvn -ntp -l ../phase-15-closure-audit-verify.log clean org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.15:report` | Exit 0, BUILD SUCCESS, **1012/0/0/0**, **04:24**, finished **18:17:06 +07**. |
| Compiler diagnostics | Same full log, compiler plugin **3.15.0** | Existing root deprecation/unchecked notes and test Jackson-2 converter removal warning inspected; no compilation failure. JVM/Lombok/Byte Buddy notices are not code-defect findings. |
| Dependencies/classpath/static | `mvn -ntp -l ../phase-15-closure-audit-static.log org.apache.maven.plugins:maven-dependency-plugin:3.10.0:analyze "-DfailOnWarning=false" org.apache.maven.plugins:maven-dependency-plugin:3.10.0:build-classpath "-Dmdep.outputFile=target/phase15-closure-classpath.txt" com.github.spotbugs:spotbugs-maven-plugin:4.10.3.0:spotbugs org.apache.maven.plugins:maven-pmd-plugin:3.28.0:cpd org.apache.maven.plugins:maven-pmd-plugin:3.28.0:pmd "-DfailOnViolation=false"` | Exit 0, **35.297 s**, finished **18:24:01 +07**. Dependency warnings triaged; SpotBugs **239 diagnostics**, PMD **57 diagnostics**, CPD **70 clone blocks**. PMD engine **7.17.0**. Successful report generation is **not** a zero-warning static check. |
| Coverage navigation | JaCoCo **0.8.15**, report from the full run | **810 classes** analyzed. Covered/missed: lines **11,455/3,010**, branches **3,662/3,088**, instructions **52,643/15,007**. Navigation only, not an invented acceptance threshold. |
| Surefire accounting | Root: PowerShell XML `SelectNodes('//testcase')` over `backend/target/surefire-reports/TEST-*.xml` | **95 XML files**, **1012 testcases**, no failure/error/skipped nodes; **92 audit cases in 14 suites**. Nested testcases are counted, not just summed testsuite attributes. |
| Modulith/schema/storage/HTTP | Included in full verification, not rerun separately | `ApplicationArchitectureTests` **35**, Collection and Knowledge architecture **3 each**; schema manifest, PostgreSQL races, actual MinIO/storage/export, wire and OpenAPI inventory passed. |
| Runtime startup/HTTP/spec | `$auditProbeClasspath = 'target/classes;' + (Get-Content -LiteralPath 'target/phase15-closure-classpath.txt' -Raw -Encoding UTF8).Trim(); & 'C:/Users/VU KHANG/.jdks/openjdk-25.0.2/bin/java.exe' --class-path $auditProbeClasspath target/phase15-closure/Phase15ClosureProbe.java` | Scratch-only synthetic PostgreSQL, production managed Flyway, real embedded HTTP. First attempt exit 1 for a missing required Address fixture. Second attempt captured BA15-2 failures, then exit 1 because actual null-weekday **400**, not hypothesized 500. Both partial results retained; these harness stops are not product findings. |
| Remaining runtime probes | Same classpath/Java command with trailing `remaining` argument | Exit 0. Skipped completed DTO probes, recreated only required disposable context/fixtures, completed PUT null-weekday, oversized page, two actual blocked-reader cases and generated OpenAPI. No manual migrations. |
| Generated OpenAPI | Embedded GET `/v3/api-docs`, then PowerShell `ConvertFrom-Json` inspection of `target/phase15-closure/openapi.json` | **155 paths, 214 operations, 254 schemas**, no missing summaries/duplicate operation IDs. Exactly five explicit anonymous auth operations; all operations declare canonical 400/406/500. Binary success schemas preserved. Semantic/schema remnants below. |
| Spelling substitute | Root: `Get-Command codespell,cspell,python,python3 -ErrorAction SilentlyContinue`; `python -m codespell --version` | No cspell/codespell executable/module. Manual terminology/current-doc review plus targeted typo regex in repository helper; no full automated spelling pass claimed. |
| Repository/docs/diagrams/hooks | Root: `& './backend/target/phase15-closure/AuditRepositoryChecks.ps1'; python .agents/hooks/test_repository_safety.py; git diff --check` | Pre-report **340 Markdown files/535 file-target links**, zero missing targets; zero missing production package descriptors or remaining `.gitkeep`; no tracked/unignored cache/class/key path candidate. **7 architecture XML files** parse; DOT/Draw.io/matrix agree on **19 nodes/38 edges**. **13 hook tests** pass; whitespace pass. File-target check excludes external URLs/anchors. |
| Actual static accessor | Read-only worker: `javap 25.0.2 -p -c org.hibernate.exception.ConstraintViolationException` against resolved Hibernate **7.4.5.Final** jar | Final exit 0 after sandbox rerun; `getConstraintName` directly returns a final field, corroborating guarded repeated-accessor triage. |
| Frozen/history preservation | Root: `git diff --name-only -- docs/database docs/architecture/diagrams docs/architecture/structurizr docs/repository backend/src/main/resources backend/compose.dev.yml .agents .codex`; `Get-FileHash` initial audit | Only the approved migration-directory placeholder deletion appears; no SQL/DBML/diagram/matrix/package-tree/config/governance-tool baseline rewrite. Initial audit SHA-256 remains **5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63**. |

Ignored root full/static logs, XML/coverage/static reports, the repository helper, generated spec and three
`target/phase15-closure/probe-evidence*.txt` results retain local evidence. A later clean removes target artifacts;
this formal report preserves the material results and exact commands. No historical failing test is fabricated.

### Resume-only final checks

The interrupted work resumed at report/current-state synchronization. Completed full/static/runtime checks were
preserved, not restarted. FR15-9's already corrected canonical note was confirmed against the existing controller;
only the current handoff symbol/disposition and current governance summaries were synchronized. Historical reports
were not rewritten. The final affected-document check used Windows PowerShell **5.1.26100.9549** at repository root:

```powershell
$auditChangedDocs = @(
    'AGENTS.md', 'README.md', 'backend/README.md', 'docs/README.md',
    'docs/implementation/README.md', 'docs/roadmap.md', 'docs/implementation/handoffs/ACTIVE.md',
    'docs/implementation/phase-15/README.md', 'docs/implementation/phase-15/audit-status.md',
    'docs/implementation/phase-15/preparation-review.md', 'docs/adr/0018-phase-15-bounded-backend-remediation.md',
    'docs/implementation/phase-15/reviews/2026-10-07-phase-15-closure-backend-audit.md',
    'docs/architecture/phase-15-implementation-notes.md'
)
& './backend/target/phase15-closure/AuditRepositoryChecks.ps1' -DocumentationOnly -DocumentPaths $auditChangedDocs
git diff --check
```

Exit0: **341 current Markdown files**, **13 affected documents/167 file-target links checked**, zero missing targets;
whitespace passed. The earlier package/diagram/hook checks were not rerun for documentation-only synchronization.
SHA-256 of the sorted **1,405 backend source/test/resource/POM path:hash lines** remains
**A81F7D226A332758462F7DEAB08148A89E091523A28841D304080437240089C0**, identical to the completed-audit digest.
Initial-audit digest, HEAD/local origin and the frozen-path diff remain as recorded above. No production/test/POM
change occurred during resume; the same active handoff remains live, with no archive/reset.

### Captured runtime results

| Probe | Actual observation |
| --- | --- |
| Fresh normal startup | V1/V2 applied automatically; two successful history rows; Hibernate **validate**; PostgreSQL **18.6**. |
| 73-character password bootstrap/login | **201/200**, canonical envelopes. Existing full-suite tests cover the other password/legacy boundaries. |
| Location public owning command | Name **500**, phone **64** accepted and read back. |
| Location HTTP POST and PUT, independent name500/phone64 | All four **400 VALIDATION_ERROR** despite valid Address fixture and accepted owning lengths. |
| Missing optional Feed feedUrl; Personal free-form email/blank nationality; Shopping/Software blank currency | Each **400 VALIDATION_ERROR**. Owning source permits these inputs; actual request/schema drift remains. |
| Shopping padded USD / padded 500-character normalized name | Each **400 VALIDATION_ERROR**; owner trims before validating. |
| Snapshot filename500 / historical display-name500; relationship omitted optional/default statuses | Each **400 VALIDATION_ERROR**. Snapshot/relationship owning contracts confirmed in source. |
| Finance weekly rule with `weekdays:[null]`, POST and PUT | **400 MALFORMED_REQUEST**, canonical envelopes; PUT leaves rule count unchanged. **Rejected 500 hypothesis**, not a remediation target. |
| Import items page0/limit100 | **200**, bounded empty list for the synthetic newly created job. |
| Same job, page2147483647/limit100 | **500 INTERNAL_ERROR**; actual `InvalidDataAccessApiUsageException` from impossible JPA offset. |
| Prior-managed Location reader: unknown→known writer | Actual PostgreSQL lock wait observed; reader returns **known=false, one interval**, fresh transaction returns **known=true, one interval**. |
| Prior-managed Location reader: known→unknown writer | Actual PostgreSQL lock wait observed; reader returns **known=true, zero intervals**, fresh transaction returns **known=false, zero intervals**. |

The schedule probes use public owner operations in independent transactions, latches, observed `pg_locks`
waiting and consumed Future outcomes. Polling merely observes a database wait; sleeps do not establish the race.
The incorrect responses are runtime-proven, not inferred solely from absent refresh calls.

## Audit coverage

PASS means no additional actionable defect established in the inspected dimension, not exhaustive absence proof.

| Mandatory dimension | Result | Current evidence / disposition |
| --- | --- | --- |
| 1. Build/compiler/dependencies/static | PASS / LIMITATION | Full/static evidence complete and triaged; normal runtime migration repaired. Spelling/IDE/CVE limits stated below. |
| 2. Business/domain correctness | FINDINGS | All 19 top-level modules and four Knowledge/three Collection capabilities reviewed; BA15-2/9 remain. Prior identity, ledger, SRS, lifecycle, defaults and invariant repairs preserved. |
| 3. SOLID/cohesion/coupling | PASS | Owning application/public API/persistence boundaries coherent, including actual narrow guard consumer. No justified layer-collapse finding. |
| 4. Pattern fitness | PASS | Facades, parser/provider adapters, orchestration and transactional guard SPI solve actual boundaries/invariant needs. |
| 5. Overengineering/YAGNI | PASS | No harmful speculative hierarchy/lock/validation framework identified; single-implementation module/provider boundaries are legitimate. |
| 6. Duplication/competing policy | FINDINGS | CPD mostly owner-local mechanics, not authority for a common SQL framework; remaining competing DTO rules BA15-2. |
| 7. Validation/errors | FINDINGS | BA15-2 and impossible page BA15-14; other list/error/public-exception/numeric/reference repairs accepted. Weekday 500 hypothesis ruled out by runtime. |
| 8. Logging/exception/privacy | FINDINGS | Redacted constraint/storage/error handling preserved. Oversized client page still classified/logged as unexpected server failure, BA15-14. |
| 9. REST/OpenAPI | FINDINGS | All operation inventory/security/binary/error checks; BA15-2/14/15 contract remnants confirmed in generated spec/runtime. |
| 10. Authentication/security/privacy | PASS / LIMITATION | PBKDF2 full input plus legacy verification, singleton/bootstrap/refresh/JWT/PIN/protected routes/storage/export reviewed/tested. No penetration/CVE attestation. |
| 11. JPA/Flyway/DBML/queries/indexes | FINDINGS | Unchanged SQL/schema/indexes and validated manifest; owner boundaries/UTC/native enums intact. Stale managed reader BA15-9 and JPA offset BA15-14. |
| 12. Transactions/concurrency/external I/O | FINDINGS | Other tested mixed writers/races, commit/rollback/uncertain upload compensation and stream/snapshot resource boundaries preserved; BA15-9 runtime counterexample remains. |
| 13. Concrete performance/resources | FINDINGS | Bounded Search batch fan-out and streaming/temp cleanup inspected; complete >100 review repaired, but page offset robustness BA15-14. No new ingestion cap or speculative index/load-test finding. |
| 14. Test sufficiency/reliability | FINDINGS | 1012 green cases with deterministic database/wire coverage; missing named DTO/reader/oversized-page/schema controls attach to four remnants. |
| 15. Package/repository/file hygiene | PASS | Ten descriptors and two stale placeholder deletions correct; no extra module/named-interface annotations. All meaningful production packages now have descriptors. |
| 16. Docs/spelling/current state/diagrams | FINDINGS / LIMITATION | BA15-15 remaining semantic schema documentation. FR15-9 canonical symbol now correct. Matrix/diagrams and file links coherent; manual terminology substitute. |
| 17. Configuration/operations | PASS | Boot-managed migrate-before-validate, UTC/OSIV, external JWT secret, enabled-only storage validation and private readiness/public-health/liveness split consistent. Deployment/providers/schedulers deferred. |

## Prior finding closure ledger

Java shorthand **J/** = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`;
**T/audit/** = corresponding test audit package. Original findings/severities/contracts remain in the immutable
[initial audit](2026-10-06-phase-15-backend-audit.md); this ledger records current evidence, not historical rewrites.
No finding is OWNER_ACCEPTED debt.

| ID / original severity | Status | Current source / regression evidence |
| --- | --- | --- |
| BA15-1 / High | CLOSED | `backend/pom.xml` starter-flyway; no support manual migrator. `FlywayStartupIntegrationTest` **2** including actual restart; disposable normal startup independently confirms V1/V2/validate. |
| BA15-2 / Medium | OPEN | Accepted 19-case DTO matrix preserved, but additional owning-rule mismatches below are source/runtime/schema-confirmed. |
| BA15-3 / Medium | CLOSED | J/authentication/.../security/`SecurityConfiguration.passwordEncoder` standard PBKDF2/delegating bcrypt including unprefixed fallback; `PasswordRangeIntegrationTest` **8**, 12/72/73/128/multibyte/full-input/legacy login. |
| BA15-4 / Medium | CLOSED | Null-member annotations for all original list families; `NullCollectionMemberValidationTest` **5** verifies indexed 400/no writes and supported unknown schedules. Extra null-weekday hypothesis produces canonical 400 at binding, not 500. |
| BA15-5 / Medium | CLOSED | Root `ApiExceptionHandler` narrow direct-JSON 415/406 and `OpenApiConfiguration`; negotiation suite **4**, including unacceptable Accept and generated errors. No committed-stream reset. |
| BA15-6 / Medium | CLOSED | Feed/Import owning advice maps public Knowledge 422/409/404, no nested access; exception integration suite **4** includes valid fixtures/control and complete target/Vault/provenance/job rollback. |
| BA15-7 / Medium | CLOSED | `ImageController`/root error boundary clears only pre-commit binary headers, aborts after commitment; embedded-wire suite **8** and retained lifecycle tests prove framing/cleanup/disconnect/timeout boundaries. |
| BA15-8 / Medium | CLOSED | `ImageController.downloadContent` provider length `>=0` wins, metadata fallback only unknown; same wire cases preserve full bytes after smaller/larger/null metadata edits, zero length and fallback. |
| BA15-9 / Medium | OPEN | Scalar/schedule mutation repairs and five concurrency cases preserved. `getSchedule` still mixes a prior managed flag with current intervals; both directions runtime-proven below. |
| BA15-10 / Medium | CLOSED | `VaultMetadataService.setRating`/`RatingRepository` narrow atomic ratings upsert, created timestamp preserved; first-set suite **3**, observed contention and both outcomes plus timestamp/update/remove behavior. |
| BA15-11 / Medium | CLOSED | `LocationCategoryService` owner reference checks/specific FK race mapping, idempotent insert retained; category suite **5** includes actual FK branch via JDK proxy and separate safe missing-category HTTP404. Absent-parent list behavior unchanged. |
| BA15-12 / Medium | CLOSED | Brand/Location/Study/Shopping/Software width/representability guards; numeric suite **6**, create/update, independent min/max, exact maximum and four-decimal reloads. No rounding/schema widening; mathematically exact redundant trailing zeros remain representable. |
| BA15-13 / Medium | CLOSED | Account-owned `ExternalAccountMutationGuard`, Study-owned `StudyExternalAccountGuard`, locked/refreshed Account update/findAndLock and Study public caller; invariant suite **12**, four actual writer pairs/both winner directions, observed waits/Futures. No reverse edge/cross-module JPA/new schema. |
| BA15-14 / Medium | OPEN | Four pagination tests prove contiguous 100/101 review/all-decision atomic execution; impossible offset still yields 500, below. No total ingestion cap introduced. |
| BA15-15 / Medium | OPEN | Accepted Search, transaction, recurrence-frequency and Collection descriptions remain; recurring ledger semantics and Software required-type metadata incomplete in actual generated schemas. |
| BA15-16 / Low | CLOSED | Ten specified ownership descriptors, no boundary annotations; only two approved placeholders removed; architecture entry prose corrected. Repository/diagram/link checks passed. |
| BA15-17 / Medium | CLOSED | Journal/Personal owner write guards/refresh and Feed source authoritative guard/scheduling/flush; lifecycle suite **7**, actual waits/Future results/both orders, content/deletion/config preserved. No generic locks/schema change. |

## Findings — bounded closure remnants

### BA15-2 — Remaining HTTP rules still compete with the owning contracts

- Severity: **Medium**. Category: validation/default/null/blank/length/schema parity.
- Evidence: current source and generated schema, with real canonical 400 probes for the named controls:

  | DTO(s), relative to J/ | Remaining HTTP restriction | Accepted owner source / rule |
  | --- | --- | --- |
  | `location/internal/web/dto/CreateLocationRequest.java:12,17`, Update counterpart | name255 / phone50 | `LocationService.java:207–212`: normalized name500 / phone64; direct owner control succeeds. |
  | `account/internal/web/dto/CreateFollowerSnapshotRequest.java:16`; `CreateFollowerSnapshotEntryRequest.java:9` | filename255 / historical display-name255 | `FollowerSnapshotService.java:59–60,84–86`: both500. |
  | `account/internal/web/dto/SetExternalAccountRelationshipRequest.java:10–14` | mandatory follower/follow statuses; note2048 | `ExternalAccountRelationshipService.java:53–64`: follower nullable, absent follow→UNKNOWN, note trimmed text without that cap. Defaults runtime-probed; note limit established in source/schema. |
  | `feed/internal/web/dto/CreateFeedSourceRequest.java:15`, Update counterpart | feedUrl nonblank mandatory | `FeedSourceService.java:128,144–148`: nullable/blank→null optional URL; Phase 10 accepted contract. |
  | `personal/internal/web/dto/CreatePersonalProfileRequest.java:16,18`, Update counterpart | exact-two nationality disallows blank; email syntax constraint | `PersonalProfileService.java:248,254,289–293`: optional blank nationality→null, email only max320. Phase 11 README:601 explicitly rejects new email syntax policy. |
  | `collection/internal/web/dto/` Shopping and Software create/update currency fields; `feed/internal/web/dto/ConvertToStudyRequest.java:24` | raw exact-three currency rejects blank/padded valid codes | Shopping:164–177, Software:205–218 and Study:256 owning normalization permits blank→null when amount absent; trimmed canonical currency when supplied. |
  | Shopping/Software create/update normalized identity fields | raw500 constraint rejects padded owner-valid500 name | Shopping:140–142 / Software:177–179 trim before checking max500. Captured Shopping padded name/code HTTP failures confirm this is normalization parity, not stylistic whitespace. |

- Consequence: valid owning commands/records remain unavailable through HTTP; generated clients receive wrong maxima,
  required/format/default rules. Current 19-case matrix does not cover these named paths.
- Required correction: align only the named owning HTTP families/normalized fields, leaving domain validators
  authoritative. Preserve genuine limits/defaults/errors and raw-authoritative fields/Markdown. Normalize only
  fields the owner already normalizes before applying equivalent bounds, or use an equally narrow owner-compatible
  boundary; no generic framework or weaker business validators. Do not invent a finite domain note cap.
- Verification/regression: Location POST/PUT name500/501 and phone64/65; snapshot POST filename/display500/501;
  relationship optional/default statuses and >2048 accepted note/reload; Feed optional null/blank URL create/update;
  Personal free-form email320/321 and blank/null/valid/unknown nationality; Shopping/Software create/update and Feed
  conversion blank/one-space currency without price, currency-required when price exists, padded valid currency,
  padded normalized maximum/max+1 names. Assert normalized persistence and actual generated maxima/required/format/
  descriptions; retain previously accepted matrix, full replacements and exact Markdown. No arbitrary whitespace
  rewrite of raw-length-authoritative fields.
- Frozen baseline impact: **none beyond recorded BA15-2 approval**; no new schema, domain policy or architecture.
- Status: **OPEN**.

### BA15-9 — Locked schedule reader retains a stale managed Location flag

- Severity: **Medium**. Category: read coherence/persistence-context/concurrency.
- Evidence: `J/location/internal/application/BusinessHoursService.java:115–123` obtains
  `LocationRepository.findByIdForShare` but does not refresh the already managed parent before combining its flag
  with freshly queried interval rows. Both mutation paths do refresh. Disposable PostgreSQL probe in both directions
  observes actual reader blocking behind the schedule writer, consumes both Futures, then gets the stale flag/new
  interval combinations recorded above. Existing five audit cases cover prior-managed writers, not this reader path.
- Consequence: valid public owner calls in an outer transaction can return internally inconsistent schedule state
  despite the intended shared-row lock.
- Required correction: smallest fresh-state safeguard under the existing owner share lock, e.g. refresh the parent
  before assembling the schedule. Preserve scalar state and current transaction/module/schema boundaries.
- Verification/regression: deterministic PostgreSQL prior managed `LocationOperations.findById` followed by
  `BusinessHoursOperations.getSchedule` while a competing replacement commits, unknown→known and known→unknown.
  Prove actual blocking, consume worker outcomes, and assert flag/interval/scalar coherence plus a fresh final read.
  Retain all five writer/coherence tests; no new version column or generic locking layer.
- Frozen baseline impact: **none beyond recorded BA15-9 coherent-state/read-lock approval**.
- Status: **OPEN**.

### BA15-14 — Accepted page values can overflow JPA's supported offset

- Severity: **Medium**. Category: bounded inspection/request robustness/error semantics.
- Evidence: `J/importdata/internal/web/controller/ImportJobController.java:102–106` permits any nonnegative int page;
  `ImportJobService.java:334–341` constructs `PageRequest.of(page, limit)` without an offset guard.
  `page=2147483647, limit=100` means long offset **214748364700**. Actual HTTP returns **500 INTERNAL_ERROR**;
  resolved Spring Data JPA **4.1.1** `PageableUtils.getOffsetAsInteger` throws `InvalidDataAccessApiUsageException`.
  Normal page0 on the same synthetic job returns200. Generated page schema has minimum0 and no description of the
  effective JPA offset limitation.
- Consequence: structurally accepted client pagination turns into an unexpected server failure. The complete
  100/101 review improvement remains accepted, but bounded inspection is not robust at its request boundary.
- Required correction: owner-local safe offset validation or bounded empty out-of-window handling before unsupported
  JPA pagination, with truthful HTTP/spec semantics. No broad DataAccess exception mapping, ingestion cap or schema change.
- Verification/regression: normal/default/100/101 traversal remains complete; extreme page and the supported-offset
  threshold/threshold+1 for multiple page sizes must never produce500 or writes. Use long arithmetic before any
  narrowing, canonical client rejection or bounded empty response, and accurate schema/description. Preserve order,
  all-decision coverage and atomic execution; no separately unapproved total ingestion limit.
- Frozen baseline impact: **none beyond recorded BA15-14 bounded HTTP inspection approval**.
- Status: **OPEN**.

### BA15-15 — Recurring ledger semantics and Software required-type metadata remain incomplete

- Severity: **Medium**. Category: generated OpenAPI semantic/schema accuracy.
- Evidence: actual generated `CreateRecurringTransactionRuleRequest` and Update counterpart omit descriptions on
  transactionType/categoryId/entries; `RecurringRuleEntryRequest.amountDelta` is also undescribed.
  `J/finance/internal/application/RecurringTransactionRuleService.java:435–485` imposes non-obvious exact-one/sign
  rules for Income/Expense and exact-two/distinct/opposite-sign/no-category for Transfer. Frequency properties are
  correctly documented; ordinary transaction descriptions do not appear in these separate recurring schemas.
  Both generated Software request schemas contain **required=["name"]**, not type, despite
  `SoftwareService.java:182–184` requiring type. Update prose says Required, but structural metadata still says optional.
- Consequence: clients cannot derive valid recurring ledger requests from their schemas; generated Software clients
  model a mandatory business input as optional. This is concrete semantic metadata, not missing prose on every endpoint.
- Required correction: targeted existing-rule descriptions for both recurring request forms and the recurring entry,
  plus required schema metadata for Software type in both request forms. Preserve existing runtime domain rejection/
  status behavior; do not add new runtime validation solely to force documentation metadata.
- Verification/regression: assert actual generated recurring cardinality/sign/distinct-wallet/category rules and
  Software type membership in schema.required/enum/no default for create and update. Retain accepted Search,
  transaction, frequency and Collection replacement/default/clearing descriptions. Never invent Transfer sum-zero.
- Frozen baseline impact: **none beyond recorded BA15-15 documentation/OpenAPI approval**.
- Status: **OPEN**.

## Observations / tool limitations

- **FR15-9 CLOSED**: canonical `docs/architecture/phase-15-implementation-notes.md:34` already identifies the actual
  `PortabilityController`, consistent with `exportSnapshotArchive` / POST `/api/v1/portability/exports`. Current
  handoff governance is synchronized to that symbol/disposition; earlier review reports remain unchanged. No production rename.
- The weekday Set-copy source hypothesis is not a product defect in the current HTTP path: real binding rejects
  `[null]` with canonical400 before the mapper. No null-weekday annotation repair is authorized from that hypothesis.
- SpotBugs **239**: 98 deliberate constructor-throw, 65+53 representation, 20 possible-null, two broad catches,
  one redundant nullcheck. New Account update ID warning is a present persisted-PK accessor; constraint accessors
  are guarded/final; credential encoder outputs and other persisted IDs are required. JSON snapshot deep isolation
  and real DI/provider boundaries inspected; parsing/export broad catches implement safe failure/rollback cleanup.
  No independently actionable defect demonstrated by these diagnostics; no blanket suppression.
- PMD **57**: 27 imports, 19 conditionals, four ternaries, two qualifications, one parentheses suggestion, four
  small unused-member/parameter/local diagnostics. Dead `FinancialTransactionService.loadWithEntries`, Markdown
  local title and snapshot columnType do not justify a separate cleanup slice. CPD **70**, largest about140 lines,
  mainly owner-local search mechanics; no divergent business path proven from text similarity alone.
- Dependency analysis cannot infer all starter/autoconfiguration/runtime/service-loader/test relationships.
  Boot-managed Flyway, PostgreSQL, SpringDoc, AWS HTTP and starter warnings are not dependency-deletion evidence.
  Existing explicitly pinned CSV/AWS/SpringDoc dependencies were not broadly upgraded.
- No automated dictionary spelling, IDE inspection, external-link/heading-anchor check, production load test,
  CVE scan or penetration test. Manual/source/static/coverage/schema/wire/integration/diagram/file-target evidence
  compensates within this audit; no vulnerability-free or production-deployment attestation.
- Existing metadata-controlled content type, accepted uncertain upload reconciliation/residual orphan handling,
  parse rollback/retry and private readiness versus public health remain accepted contracts/tradeoffs, not new findings.
  C4/functional diagrams include expressly planned frontend/feed/scheduling/backup concepts, not a false implemented inventory.
- Initial executed startup/password/HTTP/binary/literal-SQL probes remain in the preserved initial report.
  Source-derived pre-fix concurrency cases are still honestly classified; no retrospective failing run is claimed.

## Frozen-baseline decisions

All four remaining requirements stay inside the original owner-approved rule alignment, coherent-state locking,
bounded complete inspection and semantic documentation families. No new ADR, dependency direction, named interface,
cross-module persistence access, schema/index/SQL change or unrelated business behavior is required.
No finding is accepted as debt. The initial alternatives rejected by the owner remain rejected.

If implementation needs broader architecture, a new total ingestion cap or any other expansion, **stop and return
OWNER_DECISION_REQUIRED** rather than using this report to broaden ADR-0018. Existing BA15-13/14 stop conditions remain.

## Final disposition

**REMEDIATION_REQUIRED**: 13 original findings closed, four authorized Medium remnants open. No new Critical/High
issue established. Full/static/architecture evidence is completed, but green tests and earlier handoff-specific
acceptance cannot close the demonstrated runtime/generated-contract gaps.

Only this dated audit, the same handoff and current governance summaries are updated by Codex; production/tests/POM,
configurations and frozen artifacts are not remediated. Ignored diagnostic helpers/results are audit-only.
No archive/reset, owner implementation commit, push/tag/PR or Phase16/17 preparation is permitted by this verdict.

Next step: run Antigravity `/antigravity-implement-handoff` against the same CHANGES_REQUESTED handoff, correcting
only the four listed remnants and preserving accepted repairs/evidence, then return through final review and closure audit.
