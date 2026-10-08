# Phase 15 Backend Audit — 2026-10-08, final closure re-audit

Status: **BACKEND_AUDIT_READY**

All 17 mandatory dimensions were covered. **BA15-1–BA15-17 are CLOSED; no unresolved actionable finding,
owner-decision item or owner-accepted debt remains.** FR15-9 remains CLOSED; FR15-10/11/12 and every earlier
final-review closure are preserved. No new actionable finding was established.

This is the repository-wide gate after [final acceptance](2026-10-08-phase-15-final-codex-acceptance.md),
superseding the four pending dispositions in the [Oct7 closure audit](2026-10-07-phase-15-closure-backend-audit.md)
without rewriting it. The single accepted handoff is [archived](../handoff.md);
[ACTIVE](../../handoffs/ACTIVE.md) is NO_ACTIVE_HANDOFF. The owner may commit/push. Phase15 is not yet
COMPLETE — FROZEN: publication and ChatGPT closeout remain. Phase16/17 and deployment remain owner-gated.

## Baseline

- HEAD/local origin/main: `6a89a998c512dda27c3e494a3525bf6118ee981e`; committed preparation remains READY FOR AUDIT.
  Remote publication evidence is the local tracking reference, not a fresh fetch.
- Phase14 implementation `3bb3f2e78a38eb66bec955219ced634d3cfddd9d` and closeout
  `0a8f3d101f39dc8cf8f5e1d6dcc46d29e6182606` are ancestors; both checks exit0. Phase14 remains frozen.
  ACTIVE entered READY_FOR_OWNER_COMMIT, satisfying the closure exception.
- Dirty worktree retains Antigravity's bounded production/test/POM remediation and prior documentation/evidence.
  Codex changes only this report, handoff/current governance docs and ignored diagnostics—not source/tests/POM,
  runtime config, SQL/DBML, diagrams, ownership or dependency directions. No publishing operation performed.
- Java25.0.2, Maven3.9.15, PowerShell5.1.26100.9549, Python3.11.7, ripgrep15.2.0, Git2.45.1.windows.1,
  Graphify0.9.69. Actual classpath/runtime: Boot4.1.1, Framework7.0.9, Modulith2.1.1, Security7.1.1,
  Hibernate7.4.5.Final, Flyway12.4.0, PostgreSQL JDBC42.7.13, SpringDoc3.1.1, Testcontainers2.0.5,
  PostgreSQL18.6, Docker server29.8.1. Existing MinIO image: `minio/minio:RELEASE.2024-11-07T00-52-28Z`.
  Disposable diagnostics use no development-vault data.

## Evidence executed

Commands run from backend unless marked root. Completed earlier audit/review work is retained; necessary
full/static/coverage/live-contract evidence was refreshed against the changed accepted baseline.
No permanent audit plugin/profile, suppression, CI or runtime configuration was added.

| Evidence | Exact command / actual tool version | Result / notes |
| --- | --- | --- |
| Git prerequisites | Root `git status --short`; `git rev-parse HEAD`; `git rev-parse origin/main`; `git merge-base --is-ancestor <each full Phase14 commit above> HEAD` | Same preparation baseline; both ancestor checks0. |
| Full build/tests/coverage | `mvn -ntp -l ../phase-15-closure-audit-2026-10-08-verify.log clean org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.15:report` | Exit0, BUILD SUCCESS, **1021/0/0/0**, **04:07**, finished **2026-10-08T13:54:16+07:00**. |
| Compiler warnings | Same log, compiler plugin3.15.0 | Deprecated API/unchecked notes and Jackson2 converter removal warning inspected; Lombok Unsafe/Byte Buddy/CDS/Tomcat/SpringDoc notices not suppressed. No warning-free claim. |
| Dependency/static reports | `mvn -ntp -l ../phase-15-closure-audit-2026-10-08-static.log org.apache.maven.plugins:maven-dependency-plugin:3.10.0:analyze "-DfailOnWarning=false" org.apache.maven.plugins:maven-dependency-plugin:3.10.0:build-classpath "-Dmdep.outputFile=target/phase15-closure-oct8-classpath.txt" com.github.spotbugs:spotbugs-maven-plugin:4.10.3.0:spotbugs org.apache.maven.plugins:maven-pmd-plugin:3.28.0:cpd org.apache.maven.plugins:maven-pmd-plugin:3.28.0:pmd "-DfailOnViolation=false"` | Exit0, **35.243s**, finished13:57:18+07. SpotBugs engine4.10.3: **239**; PMD engine7.17.0: **57**; CPD **70** clone blocks. Actual diagnostic paths/types/source triaged, not zero-warning checks. |
| Coverage navigation | JaCoCo0.8.15; root PowerShell XML `report.counter` inspection |810 classes. Covered/missed: lines11596/2948, branches3732/3090, instructions53210/14715. Navigation, not an acceptance percentage. |
| Test accounting | Root XML `SelectNodes('//testcase')`, `//failure`, `//error`, `//skipped` over `backend/target/surefire-reports/TEST-*.xml` |95 XMLs,1021 actual cases,0/0/0;101 audit cases/14 suites, including25 Web DTO cases. Nested testcase nodes counted. |
| Modulith/schema/PostgreSQL/MinIO/wire | Included in full verification, not rerun separately | Top-level/nested/consumer architecture, manifest, deterministic database races and HTTP contracts pass; MediaStorage33, Portability15, embedded-wire8 green. |
| Production startup/live spec | `$auditProbeClasspath = 'target/classes;' + (Get-Content -LiteralPath 'target/phase15-closure-oct8-classpath.txt' -Raw -Encoding UTF8).Trim(); & 'C:/Users/VU KHANG/.jdks/openjdk-25.0.2/bin/java.exe' --class-path $auditProbeClasspath target/phase15-closure-oct8/ClosureSpecProbe.java` | Exit0; synthetic PostgreSQL/signing key, storage disabled, no manual migrator. Boot applies V1/V2,2 successful history rows, Hibernate validate, real GET `/v3/api-docs`200; context/container closed. |
| Generated OpenAPI | Root `& './backend/target/phase15-closure-oct8/InspectOpenApi.ps1'`, audit-only PowerShell5.1 helper over captured JSON |155 paths/214 operations/254 schemas; no missing summaries/duplicate IDs or canonical400/406/500 declarations; exactly5 anonymous auth operations. Repaired owner/schema semantics and image/export binary schemas correct. |
| Repository/docs/diagrams | Root `& './backend/target/phase15-closure-oct8/AuditRepositoryChecks.ps1'`; `git diff --check` | Pre-doc-edit1282 production Java/365 packages/99 test Java/345 Markdown/603 local file-target links; zero missing descriptors/stale placeholders/unexpected tracked-or-unignored generated/secret paths/missing targets.7 XMLs parse; DOT/Draw.io/matrix19 nodes/38 edges agree. |
| Governance safety hooks | Root `python .agents/hooks/test_repository_safety.py` |13 tests pass; no policy change. |
| Spelling substitute | Root `Get-Command codespell,cspell,python -ErrorAction SilentlyContinue`; `python -m codespell --version`; targeted `rg -n -i 'teh |recieve|seperate|occurence|succesful|PortabilityExportController'` over current README/AGENTS/architecture/Phase15 status docs | Dictionary checker unavailable (no codespell module/cspell executable); manual terminology/current-state review plus targeted substitute, not full automated spelling. |
| Source/design/static triage | PowerShell `Get-Content -LiteralPath ... -Encoding UTF8`, targeted `rg -n`/`rg --files` at paths below; XML grouping of actual SpotBugs/PMD/CPD | Main plus3 explicitly read-only workers inspect all17 closures/adjacent risks. Workers inspect root-run results, not duplicate builds. |
| Frozen/history integrity | Root `git diff --name-only -- docs/database docs/architecture/diagrams docs/architecture/structurizr docs/repository backend/src/main/resources backend/compose.dev.yml .agents .codex`; sorted path:SHA256 and `Get-FileHash` | Only the already approved migration `.gitkeep` deletion on these paths; no SQL/DBML/diagram/package-tree/config/tool baseline rewrite. Accepted source fingerprints match below. |

The scratch repository helper initially stopped on an empty root-file parent. Only that helper was corrected and
the incomplete check rerun successfully; this was not a product finding. Sandbox setup failures were resolved
through authorized read/test reruns. Earlier successful checks were not repeated merely to recreate history.
Ignored root full/static logs and target XML/coverage/static reports/helpers/live `openapi.json` retain diagnostics;
later clean removes target artifacts. This formal report preserves the material evidence.

## Audit coverage

PASS means no unresolved actionable defect established, not exhaustive absence proof. Retained comprehensive
foundation evidence is checked against current boundaries; remediation and adjacent risks receive fresh inspection.

| Mandatory dimension | Result | Evidence / disposition |
| --- | --- | --- |
| 1. Build/compiler/dependency/static | PASS / LIMITATION | Fresh full/static/coverage and warning triage; spelling/IDE/CVE limits below. |
| 2. Domain/business correctness | PASS |19 modules and Knowledge/Collection capabilities; retained foundation audit plus current owner/default/identity/state/ledger/SRS/invariant inspection. |
| 3. SOLID/cohesion/coupling | PASS | Owner-local policies/transactions/persistence; narrow guard inversion preserves Knowledge→Account. |
| 4. Pattern fitness | PASS | Real public facades, storage/parser adapters, orchestration and synchronous invariant guard, not hypothetical variants. |
| 5. Overengineering/YAGNI | PASS | No concrete harmful speculative generic/base/lock/validation framework; boundary interfaces remain justified. |
| 6. Duplication/competing policies | PASS | Named DTO rules match owners. CPD mostly owner-local Search SQL mechanics; no divergent business invariant proved from similarity. |
| 7. Validation/errors | PASS | True maxima/max+1, optional/default/Unicode/order/raw HTTP and exact numeric controls; impossible offset handled before JPA. |
| 8. Logging/exceptions/privacy | PASS | Redaction and narrow known-error mapping retained; oversized page no unexpected500; committed binary aborts, no JSON append. |
| 9. REST/OpenAPI | PASS | Actual155/214/254 inventory, anonymous/Bearer/errors/binary, corrected maxima/defaults/Software/ledger and bounded-review parameters. |
| 10. Authentication/security | PASS / LIMITATION | Full-input/legacy credentials, singleton/JWT/PIN/refresh/private-route/privacy tests; no penetration/CVE attestation. |
| 11. JPA/Flyway/DBML/query/index | PASS | Unchanged SQL/indexes/schema, migrated validated manifest, ownership/UTC/native enums, fresh locked reader and safe offsets. |
| 12. Transactions/concurrency/I/O | PASS | Observed PostgreSQL waits/Future outcomes/both orders/prior-managed state, atomic rollback and confirmed/uncertain storage/snapshot/stream lifetimes. |
| 13. Performance/resources | PASS | Bounded Search batch/top-K fan-out, complete paged imports/long-offset guard, temp-file streaming/cleanup. No new ingestion cap/speculative schema change. |
| 14. Tests/reliability | PASS |1021 green/101 audit cases; raw inputs, independent reseeding, owning/Vault/provenance no-write and fresh state; deterministic database/wire/provider coverage. |
| 15. Package/file hygiene | PASS | Descriptors, only approved placeholder removals, no new boundary annotations/tracked generated-or-secret path candidate. |
| 16. Docs/spelling/diagrams | PASS / LIMITATION | Canonical PortabilityController/contracts, local links/19-node38-edge diagrams, current gate synchronized/history preserved; manual spelling substitute. |
| 17. Configuration/operations | PASS | Managed migrations/validate/UTC/OSIV, external JWT key, enabled-only storage validation, private readiness/public health/liveness split; deployment/providers/schedulers deferred. |

## Prior finding closure ledger

`J/` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/audit/` = corresponding test audit package.
Original severity/evidence/consequence/required correction/frozen impact remain in the immutable
[initial audit](2026-10-06-phase-15-backend-audit.md), bounded by [owner decisions](../owner-decisions.md)
and [ADR-0018](../../../adr/0018-phase-15-bounded-backend-remediation.md). No owner-accepted debt.

| Finding / original severity | Status | Current source / executed regression evidence |
| --- | --- | --- |
| BA15-1 / High | CLOSED | Boot starter-Flyway; unchanged SQL/validate; FlywayStartupIntegrationTest2 incl restart; independent production startup confirms2 migrated rows/no manual prerequisite. |
| BA15-2 / Medium | CLOSED | Named HTTP families align with owners. WebDtoValidationAuditIntegrationTest25 covers original/remnant create/PUT maxima/defaults/optional fields, raw padded500/501/Unicode order, independently reseeded VND→null/space clearing, fresh GET/unchanged fields, owning/Vault/provenance counts and actual generated schema. Captured spec agrees. |
| BA15-3 / Medium | CLOSED | SecurityConfiguration:44–50 standard PBKDF2/delegating prefixed+unprefixed bcrypt; PasswordRangeIntegrationTest8:12/72/73/128, multibyte/full-input suffix distinction, invalid bounds/legacy login; PIN/JWT/refresh preserved. |
| BA15-4 / Medium | CLOSED | Element NotNull/Valid in5 original list families; NullCollectionMemberValidationTest5 indexed400/no-write and supported null/empty schedule controls. Prior weekday500 hypothesis remains disproved. |
| BA15-5 / Medium | CLOSED | ApiExceptionHandler safe direct JSON415/406 and OpenApiConfiguration schemas; ContentNegotiationEnvelopeIntegrationTest4 incl unacceptable Accept; no broad mapping/committed reset. |
| BA15-6 / Medium | CLOSED | FeedExceptionAdvice:77/82/87 and ImportDataExceptionAdvice:41/46/51 public Knowledge422/409/404 only. Exception suite4: fixture-before-count and complete target/Vault/provenance/job rollback. |
| BA15-7 / Medium | CLOSED | Image/root precommit header reset, postcommit abort/idempotent cleanup; wire suite8 incl20/50000-byte first-read framing, failures/disconnect; baseline timeout/storage controls retained. |
| BA15-8 / Medium | CLOSED | ImageController:157–162 provider length>=0 including0, fallback only unknown; full bytes after smaller/larger/null metadata edits, zero/unknown controls. No mismatch-rejection policy. |
| BA15-9 / Medium | CLOSED | BusinessHoursService:110–124 refresh at117 under existing findByIdForShare before intervals. Concurrency suite7 incl prior-managed readers:443/537, both directions/observed waits/consumed Futures/coherent fresh state; writer repairs retained. |
| BA15-10 / Medium | CLOSED | VaultMetadataService:124–140 owner capability/trash checks, RatingRepository native atomic upsert/createdAt; first-set/timestamp3 plus baseline remove/lifecycle/capabilities. |
| BA15-11 / Medium | CLOSED | LocationCategoryService:121–147 both references/conflict-safe insert/specific FK mapping; unknown integrity rethrown. Suite5 incl actual PostgreSQL FK through deliberate repository proxy, no rows/safe404 and race convergence. Proxy not mislabeled live2-transaction removal race. |
| BA15-12 / Medium | CLOSED | Owner Brand/Location/Study/Shopping/Software exact width/scale guards; numeric suite6 HTTP create/update/independent min-max/true max/four-decimal reload. No rounding/schema widening; trailing-zero predicate changes no input. |
| BA15-13 / Medium | CLOSED | Account-owned ExternalAccountMutationGuard/Study-owned consumer; Account update:109–115/findAndLock:183–190 lock+refresh, StudyItemService:309 public lock. Suite12 incl4 real competing writer pairs/both winners/waits/Futures/rollback and ordinary valid edits. No reverse edge/schema/cross-module JPA. |
| BA15-14 / Medium | CLOSED | ImportJobService:334–347 long offset returns empty above Integer.MAX_VALUE before JPA; legacy page0 overload. Suite5 contiguous100/101 review/full decisions/atomic rollback and int extreme/threshold+1 at50/100. Defaults0/50/HTTP1–100/meta=null, generated overflow prose; no total cap. |
| BA15-15 / Medium | CLOSED | Search literal/tag-all; Finance type/sign/cardinality/distinct/category/frequency; Collection replacement/default/clearing match owners. Separate recurring schemas/entry described; Software type required/exact APPLICATION-EXTENSION/no default. Actual schema controls/captured spec green, no sum-zero/runtime policy invention. |
| BA15-16 / Low | CLOSED | Ten ownership-only Collection descriptors/no boundary annotations; exactly2 approved placeholders removed; architecture entry prose corrected; repository/diagram/link checks green. |
| BA15-17 / Medium | CLOSED | Journal/Personal shared owner guards+refresh/update-deleted rejection; FeedSourceService:60–63/FeedItemService:131–141 authoritative locked schedule/saveAndFlush. Lifecycle7 actual waits/Futures/both orders/exact Markdown/state/config; active-self uniqueness retained. |

### Previous remnants and final-review follow-ups

BA15-2's incompatible Location/snapshot maxima, mandatory relationship/feed fields, email syntax/nationality blank
policy and named Collection/conversion normalization rules are corrected. Location blank-before-length deliberately
differs from Account/Collection trim-then-empty and Feed length-before-Unicode-blank storage; raw HTTP and exact
historical reads prove those owner differences. No global blank policy/generic validation framework.
BA15-9's two stale-flag/current-interval counterexamples are closed by fresh parent state under the existing lock.
BA15-14's supported int extreme offsets produce bounded empty200, not500; all items/decisions remain reachable.
No Long.MAX_VALUE200 test is claimed. BA15-15's independent generated recurring/Software contracts now match owners.

FR15-9 stays CLOSED: canonical notes correctly name `PortabilityController`/`exportSnapshotArchive`, no production
rename. FR15-10/11/12 remain closed: accepted raw payloads, eight independently bracketed Vault assertions and
precise explicit-null/create-count/update-field/conversion evidence. Historical reports are not rewritten.

## Observations / tool limitations

- SpotBugs239:98 deliberate constructor-throw,65+53 representation,20 possible-null,2 broad catches,1 redundant
  nullcheck. Real DI/provider boundaries/deep JSON isolation, persisted PK/encoder required values and guarded stable
  constraint accessors inspected; prior resolved-Hibernate accessor bytecode evidence retained. Parsing catches
  produce safe invalid items; export catches preserve rollback/cleanup/primary error. No actionable defect/suppression.
- PMD57:27 imports,19 collapsible conditionals,4 ternaries,2 qualifications,1 parentheses and4 small unused
  method/local/parameter diagnostics. CPD70, largest140 lines, mostly independent owner-local Search mechanics.
  These retained non-actionable observations do not justify unauthorized cleanup/common SQL infrastructure.
  Counts alone do not prove identity/quality; current diagnostic paths/types/source and unchanged boundaries were checked.
- Dependency analysis cannot infer all starter/autoconfiguration/runtime/service-loader/test uses. Managed Flyway,
  PostgreSQL, SpringDoc, AWS HTTP and starter/transitive warnings are not dependency-deletion authority.
- No automated dictionary spelling, owner IDE inspection, external URL/heading-anchor validation, production load
  test, CVE or penetration scan. Manual/source/static/coverage/live-schema/wire/database/storage/diagram evidence
  compensates within this audit; no exhaustive correctness/vulnerability-free/deployment attestation.
- Metadata-controlled content type, uncertain-upload residual orphan/reconciliation, atomic import bounds and
  expressly planned frontend/feed/scheduling/backup diagram concepts remain accepted contracts, not new findings.
- Graphify0.9.69 cached Oct5 graph6566 nodes: main `query 'validation snapshot currency schedule pagination'
  --budget 900` (5 verified tokens, BFS2,142 found/21 shown). Workers queried `schedule rating category price study
  account guard --budget 700` (236/17), `encoder flyway http password pin security --budget 700` (564/22),
  `import pagination recurring ledger feed lifecycle --budget 700` (182/28). Installed executable
  `C:/Users/VU KHANG/.local/bin/graphify.exe`; `reflect --if-stale`/lessons read. Explicit truncation, navigation only;
  no rebuild/new extraction/token cost or stale-edge architectural authority.

## Frozen-baseline decisions and integrity

All remediation remains within17 recorded approvals; no further frozen-baseline decision needed.
SQL/DBML/indexes/ownership/named interfaces/allowed directions/diagrams stay unchanged. BA15-13 architecture
expansion, BA15-14 additional total limits and all future expansion still stop for owner disposition.
Closing these findings authorizes no future production implementation.

Pre-doc-edit SHA256 of sorted UTF8 `relative-path:SHA256` lines joined with newline:

- Source/tests/resources/POM1405: `47A470AFAEA560EF32BEDCDE79E54519AF91CBD6F0F0CEFEF58AAABAC2266E39`.
- Production/resources/POM1305: `D528052C0C3D3BFE0A215BFE913BE3C9E90F8C64C8E9979BCA07AC4154226B62`.
- Tests/resources100: `30698F8F6CB548C37C0207D31D26C0D14EC5C98FD7111FFB3A312730FBD642FB`.
- All15 pre-existing formal reviews: `E931EC968C93D02A938EA5CC09E9D891362C6B5B7982A8AEB06B1FD2B504119F`.
- Owner decisions: `E113B3111B6DE5FA40A72C8ECEFEBF59F1FCA9BB26BA124C6FF9353CED3DA732`.
- Initial audit: `5DDA60D92B4F0166E3827A5971DB360FC97393EFF620F6642834FF5BA7456C63`.
- Accepted ACTIVE before archival: `45A4AA716CF24E68D2B921423D1DEDE907D84927E615432A8EB5586B81ACE8B8`.

Source/production/test digests exactly match latest acceptance. Formal history/owner decisions and original
source-derived versus runtime/failing evidence distinctions remain preserved. Archive retains the accepted handoff
body and submission/review lineage with explicit final header/gate only; no second live handoff/history rewrite.

Post-governance checks completed successfully: the repository helper inspected **347 Markdown files / 627 local
file-target links, zero issues**, with the same1282 production Java/365 packages/99 test Java/7 XMLs/19 nodes/38 edges.
All13 safety-hook tests and `git diff --check` passed. Post-edit source/production/test and all15 pre-existing formal
review fingerprints exactly equal the values above; owner decisions and initial audit are unchanged. In-memory
removal of only the archive's final header/gate additions reconstructs the accepted ACTIVE SHA256 above exactly,
proving its body and lineage were preserved. Final archive SHA256:
`13E61352DC9A61F5CABB73DE650E0206C375BCD49CD647FEF21918B8C10DEBCF`;
NO_ACTIVE_HANDOFF SHA256: `B01D3C734F28C5810747A009B7DDA0FB11494573FB734186F6E069EC5FAC1783`.
Graphify query feedback was saved to its ignored memory cache; it remains navigation evidence, not repository truth.

## Final disposition

**BACKEND_AUDIT_READY**. Required full/static/coverage/architecture/database/storage/wire/live-OpenAPI/repository
evidence is complete and green or transparently triaged/compensated. All17 findings closed; no actionable remnant.
The handoff is archived and ACTIVE reset as the audit skill requires. Current governance synchronized; Phase15
awaits owner publication and ChatGPT closeout/freeze only. Codex does not commit/push.

Single owner commit message:

```text
fix(backend): remediate phase 15 audit findings
```

Next step: owner commit/push using that message, then give ChatGPT the latest package for **Phase15 closeout only**.
Phase16/17 preparation remains owner-gated.
