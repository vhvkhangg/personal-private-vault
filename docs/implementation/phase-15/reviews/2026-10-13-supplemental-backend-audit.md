# Supplemental repository-wide backend audit — 2026-10-13

Status: **SUPPLEMENTAL AUDIT COMPLETE — MAINTENANCE RECOMMENDED**

Scope: the current implementation and governance of frozen Backend Phases 0–15. This is a separately
owner-requested residual-issue audit, not a repetition, reopening, or replacement of the historical Phase 15 gate.
The historical acceptance, finding dispositions, owner decisions, audit status and handoff remain unchanged.

**Result: seven verified defects — six Medium and one Low.** No Critical or High defect was established.
Fresh full verification passed **1,021 tests, zero failures/errors/skips**. Isolated counterexamples establish
issues that the passing suite does not cover. Only **SBA-001** should gate current Phase 17 runtime acceptance;
the other findings have independent or later-integration priorities. Phase 17 preparation acceptance is preserved.

See the [prioritized maintenance recommendation](../../maintenance/supplemental-backend-audit-2026-10-13/README.md).
No production code, tests, POM, migrations, accepted ADRs, historical evidence or active handoff were changed.
This report grants no implementation authority and does not return a new `BACKEND_AUDIT_READY` verdict.

## Baseline and method

- Initial `HEAD` and local `origin/main`: `dd414861e2f4cf0a07f141c56c979a7bd4cc5f9d`; working tree clean.
  No remote fetch was performed; the equality concerns the local tracking reference.
- Phase 15 remediation/closeout are present as `0b1ab11` / `cbd5963`; accepted RAG preparation is `dd41486`.
- `docs/implementation/handoffs/ACTIVE.md`: `NO_ACTIVE_HANDOFF` throughout.
- Java 25.0.2, Oracle OpenJDK; Maven 3.9.15; Windows 11 amd64; Docker client/server 29.8.1.
- Audit shell: Windows PowerShell 5.1.26100.9549; ripgrep 15.2.0; Python 3.11.7; Graphify 0.9.69.
- Spring Boot 4.1.1, Spring Modulith 2.1.1, Springdoc 3.1.1; Testcontainers 2.0.5.
- Tests use PostgreSQL `18.6-alpine` and MinIO `RELEASE.2024-11-07T00-52-28Z`.
- Applied the current `codex-backend-audit` and `codex-review-coverage` skills. All 29 dimensions are explicitly
  dispositioned below. Read-only specialist review supplemented the root review; builds/probes ran centrally.
- Graphify was queried first as navigation context. Findings were verified in source and, for every actionable
  item, by executable probes. The graph is not evidence of correctness or current architecture.
- The complete production/test tree was inventoried. Automated enumeration and package/import scans covered
  every relevant file; manual method-level inspection was risk-driven across all module owners. This is not a
  claim that every method or every historical document was independently re-derived.

Finding IDs use **SBA-001 onward**, deliberately separate from immutable historical `BA15-N` IDs.
`PASS` below means no actionable issue established in the stated inspection, not proof against all possible defects.
`NOT_VERIFIED` is not a passing result.

## Executed evidence

Maven commands ran from `backend/`; other commands ran from the repository root. Diagnostic helpers and raw
outputs are ignored under `logs/supplemental-backend-audit-2026-10-13/` and `backend/target/`. The material outcomes
are retained in this report so its conclusions do not depend on those disposable files remaining available.

| Evidence | Exact command / tool version | Observed result and limit |
| --- | --- | --- |
| Baseline | `git status --short`; `git rev-parse HEAD`; `git rev-parse origin/main`; Git 2.45.1.windows.1 | Clean initial tree; both refs equal the commit above. |
| Graph navigation | `& 'C:/Users/VU KHANG/.local/bin/graphify.exe' query 'backend module services transactions privacy search portability storage' --budget 1200`; `& 'C:/Users/VU KHANG/.local/bin/graphify.exe' --version` | Graphify 0.9.69; existing 6,566-node graph traversed; truncated navigation output, verified against source. |
| Runtime | `mvn -version`; `docker version --format '{{.Client.Version}} / {{.Server.Version}}'` | Java/Maven/Docker versions above. |
| Full build / coverage | `mvn -ntp -l ../logs/supplemental-backend-audit-2026-10-13/verify.log clean org.jacoco:jacoco-maven-plugin:0.8.15:prepare-agent verify org.jacoco:jacoco-maven-plugin:0.8.15:report` | Exit 0, `BUILD SUCCESS`; **1021/0/0/0**, 04:27, finished 15:44:02 +07. Compiler 3.15.0, Surefire 3.5.6. Modulith, PostgreSQL, MinIO, wire, security, OpenAPI and existing audit regressions ran within this suite. |
| Dependency / static / duplication | `mvn -ntp -l ../logs/supplemental-backend-audit-2026-10-13/static.log org.apache.maven.plugins:maven-dependency-plugin:3.10.0:analyze "-DfailOnWarning=false" org.apache.maven.plugins:maven-dependency-plugin:3.10.0:build-classpath "-Dmdep.outputFile=target/supplemental-audit-classpath.txt" com.github.spotbugs:spotbugs-maven-plugin:4.10.3.0:spotbugs org.apache.maven.plugins:maven-pmd-plugin:3.28.0:cpd org.apache.maven.plugins:maven-pmd-plugin:3.28.0:pmd "-DfailOnViolation=false"` | Exit 0, 34.345 seconds. SpotBugs engine 4.10.3: **239** diagnostics; PMD engine 7.17.0: **57**; CPD: **70** clone blocks. Reports generated and triaged, not zero-warning gates. No build configuration or suppressions added. |
| Tree / coverage summary | `python logs/supplemental-backend-audit-2026-10-13/audit_summary.py`; Python 3.11.7 | 1,282 production Java files, 365 packages, 99 test Java files; no package/path mismatch. Retained full path/type inventory and source hashes in local JSON. |
| Repository / diagrams / links | `& './logs/supplemental-backend-audit-2026-10-13/AuditRepositoryChecks.ps1'` | Existing read-only checker copied to ignored audit space. Before new report: 363 Markdown files, 798 local file-target links, 7 diagram XML files, 19 module nodes / 38 edges; zero issues. DOT/Draw.io/matrix agree. External URLs and heading anchors were not checked. |
| Governance hook | `python .agents/hooks/test_repository_safety.py` | **13 tests passed**. This proves tested hook behavior, not resistance to every possible shell wrapper. |
| Generated OpenAPI | `java --class-path $auditClasspath logs/supplemental-backend-audit-2026-10-13/LiveOpenApiAudit.java logs/supplemental-backend-audit-2026-10-13/openapi.json` | Disposable PostgreSQL-backed app, loopback-only port, synthetic JWT configuration. HTTP 200 capture; **155 paths / 214 operations / 254 schemas**, OpenAPI 3.1.0; zero missing summaries or duplicate operation IDs; precisely five public auth operations. Existing runtime route/schema tests also passed. |
| Residual behavior | `java --class-path $auditClasspath logs/supplemental-backend-audit-2026-10-13/ResidualProbes.java` | Exit 0; synthetic fixtures in disposable PostgreSQL. Reproduced SBA-001–006, including committed competing writes and stale persistence contexts, without modifying source/tests. |
| Numeric overflow | `java --class-path $auditClasspath logs/supplemental-backend-audit-2026-10-13/ScalarProbe.java` | Exit 0; pure validator and Jackson 2/3 DTO deserialization reproduce SBA-007. Extreme value was deliberately not sent to persistence. |
| Spelling / terminology | `Get-Command codespell,cspell,python -ErrorAction SilentlyContinue`; `python -m codespell --version`; targeted `rg -n -i 'TODO|FIXME|HACK|XXX|teh |recieve|seperate|occurence|succesful|PortabilityExportController'` over current code/governance/docs | Dictionary checker unavailable. Manual terminology/status review plus targeted scan; not a full dictionary-based spelling pass. |
| Security tool availability | `Get-Command osv-scanner,trivy,grype,syft,codespell,cspell -ErrorAction SilentlyContinue` | No listed scanner available. No fresh CVE database, full transitive license audit or SBOM verification was performed. |
| Patch integrity | `git diff --check`; `git diff --name-only`; source/POM/migration SHA-256 comparison; explicit new-document whitespace/ledger/field assertions | No existing tracked file changed. Delivery checks passed: exactly 29 ledger rows, seven findings with frozen-impact/Phase 17 fields, no new-document trailing whitespace; final link check 365 Markdown files / 800 targets / zero issues. Only the new report and recommendation are untracked delivery files. |

For the three Java-source launch commands, the exact PowerShell classpath setup was:

```powershell
$auditClasspath = Get-Content -LiteralPath 'backend/target/supplemental-audit-classpath.txt' -Raw
$auditClasspath = 'backend/target/classes;backend/target/test-classes;' + $auditClasspath.Trim()
```

The probes used real Spring public operations and `TransactionTemplate` with outer and inner `REQUIRES_NEW`
transactions. The inner transaction committed a competing change before the outer called the affected mutation.
That is a deterministic reproduction of a previously managed entity, without timing sleeps. It does not replace
the two-thread lock-contention regressions required for a repair.

JaCoCo analyzed **810 classes**: 11,596/14,544 covered lines (**79.7%**) and 3,732/6,822 covered branches (**54.7%**).
Coverage guided inspection; no percentage threshold is inferred. Maven's 1,021 total was corroborated by **1,021
XML testcase elements**. Summing only XML suite-header `tests` attributes gives 1,008 in this run; those headers
under-count the emitted cases and were not used to change the test total.

Warnings remain: Lombok's deprecated `sun.misc.Unsafe` use, a deprecated test-only
`MappingJackson2HttpMessageConverter`, dependency analyzer starter/transitive classifications, and report-rendering
warnings. None was converted into a production defect without a demonstrated consequence.

## Complete structure and responsibility inventory

Counts include `package-info.java`; of 1,282 files, **365 are package descriptors and 917 are other Java files**.

| Owner | Java files | Owner | Java files |
| --- | ---: | --- | ---: |
| Application root | 10 | Account | 71 |
| Authentication | 59 | Collection | 127 |
| Feed | 84 | Fiction | 79 |
| Film | 97 | Finance | 118 |
| ImportData | 52 | Journal | 28 |
| Knowledge | 159 | Location | 99 |
| Media | 61 | People | 64 |
| Personal | 31 | Portability | 7 |
| Reference | 41 | Search | 19 |
| Settings | 25 | Vault | 51 |

Actual class/interface/record/enum paths, package declarations, NamedInterface descriptors, internal layers and
test paths were recursively enumerated against `docs/repository/repository-package-tree.md`, ADR-0002/0015/0016/0017
and the dependency matrix. Public operations/commands/views/enums/exceptions are capability-owned; web DTOs,
mappers, advice and persistence types remain under owner internals. Knowledge and Collection nested owners retain
their approved parent facades. The tree is capability-oriented rather than a flat global technical hierarchy.

The root has the application bootstrap, package descriptor and the small accepted HTTP contract/error/OpenAPI set.
**ADR-0016 deliberately owns `ApiResponse` there.** There is no demonstrated competing business envelope or concrete
need for a new `common` module. Swagger's own `ApiResponse` is a separate OpenAPI model, not a duplicate wire contract.
Module-local exception packages belong with their exposed owner contract; framework transport failures belong at
the existing root advice boundary. No actionable placement or visibility defect was established.

No production stale `.gitkeep`, tracked generated/cache artifact, package/path casing mismatch or competing
predecessor implementation was established. Modulith verification passed. Source inspection preserved the accepted
read-only JDBC portability exception and found no cross-module repository sharing or reversed dependency requiring
an ADR amendment.

SOLID/GRASP assessment followed concrete responsibilities: application services own transactions and coordination,
domain objects own state rules, repositories own persistence, DTO mappers own transport conversion, parent facades
own module composition, and provider ports isolate actual S3 integration. Single-implementation interfaces at these
boundaries have a current purpose. No blanket interface/layer collapse, `ServiceImpl` conversion, generic base service,
global utility package or pattern-driven rewrite is recommended.

## All 29 review dimensions

| # | Dimension | Status | Evidence / limit |
| ---: | --- | --- | --- |
| 1 | Owner intent / non-goals | PASS | Audit-only authorization; root/docs/backend instructions; frozen Phases 0–15; no source edits or handoff. |
| 2 | Requirement traceability | FINDING | Current-PIN, null-preserving settings, enabled-source, trash-metadata and safe import contracts mapped to SBA-001–007 and required tests below. No exhaustive new requirements database created. |
| 3 | Architecture / ADR / diagrams | PASS | ADR-0015/0016/0017/0018 and accepted 0019 distinguished; actual source graph, DOT/Draw.io/matrix checks; no newly authorized runtime claimed. |
| 4 | Ownership / NamedInterfaces / direction | PASS | Recursive descriptors/import review; Modulith tests; leaf Search/Portability; no cross-owner internals/repository violation established. Transaction defects are recorded under 15. |
| 5 | Repository / package / folder organization | PASS | Complete 1,282/365/99 inventory; owner grouping and canonical package tree reviewed, including public commands/views/enums/exceptions and internal HTTP/persistence. |
| 6 | Shared contracts / exception placement | PASS | Root HTTP allowlist, canonical envelope and module advice agree with ADR-0016. Fresh OpenAPI and route tests; no taste-based `common/` recommendation. |
| 7 | SOLID / GRASP / cohesion / coupling | PASS | Concrete owner services, public ports, facades, provider adapters and mappers inspected across owners; no harmful interface or misplaced policy established. |
| 8 | DRY / KISS / YAGNI / invariants | FINDING | State/invariant evaluation is not authoritative in SBA-001–003/005; numeric arithmetic in SBA-007. CPD clones separately calibrated as observations. |
| 9 | Patterns / anti-patterns | PASS | Actual Repository/Facade/Adapter/provider-port boundaries justified; no gratuitous generic hierarchy found. |
| 10 | Domain behavior / states / idempotency | FINDING | Runtime counterexamples for trash/restore, partial settings, disabled ingestion and current PIN; existing domain regressions passed. |
| 11 | REST / wire / OpenAPI compatibility | PASS | Fresh 214-operation schema, exact existing route inventory/HTTP/security tests and owner DTO/advice inspection. Unknown endpoint-by-endpoint fuzz cases are not claimed covered. |
| 12 | Input / format / numeric validation | FINDING | SBA-004/006/007 reproduce delimiter, trailing-input and width-overflow errors; existing DTO/numeric/pagination tests green. |
| 13 | Authentication / authorization / privacy | FINDING | SBA-005 violates current-PIN check/write consistency; no JWT bypass claimed. JWT/refresh/public-route and privacy tests passed; RAG runtime privacy remains unimplemented. |
| 14 | Credentials / logs / retention / egress | PASS | Production log-call scan, redacted credentials, hashed tokens, security table export exclusions, S3-only actual network adapter; no content/secret leakage established. Comprehensive supply-chain verification is 25. |
| 15 | Transactions / locks / concurrent writes | FINDING | Deterministic committed-interleaving probes for SBA-001/002/003/005; inspection of remaining owner locks, Finance/SRS/import/storage composition and existing race tests. |
| 16 | Failure / recovery / operations | PASS | Fresh MinIO/stream/compensation/import atomicity tests and source cleanup inspection; no new recovery defect established. Host-kill/power-loss and real backup-restore drills were not run. |
| 17 | Flyway / PostgreSQL / JPA / schema | FINDING | Fresh migration/Hibernate/PostgreSQL checks and V1/V2–DBML numeric/constraint inspection; SBA-007 is validator mismatch with unchanged numeric(6,2). No planner-dependent index finding asserted. |
| 18 | Algorithmic / memory / I/O resources | NOT_VERIFIED | Source bounds, JDBC cursor fetch size, per-row buffers, import materialization, search qualification loops and export spool observed; no measured worst-case RAM/disk/latency budget established. |
| 19 | Data / index / recommendation quality | PASS | Existing lexical/trigram ordering/filter/ranking tests and owner search code inspected. ANN, reranker, generated recommendations and RAG eligible-corpus runtime are N/A to this implemented baseline. |
| 20 | Document / multilingual fidelity | FINDING | SBA-004 raw Markdown preserved but structured metadata lost/valid input rejected. OCR/PDF/XLSX/DOCX extraction and RAG locators are not implemented and are N/A here. |
| 21 | Security / adversarial tests | FINDING | Existing auth/negative HTTP/private logging tests green; new trailing-input, extreme exponent and superseded-PIN fixtures expose missing controls. RAG prompt-injection/worker-publication tests are future gates, not executed tests. |
| 22 | Test sufficiency / determinism / risk coverage | FINDING | 1,021 passing cases plus separate reproduced defects; required regression gaps attached to each finding. Concurrency tests use authoritative barriers in reviewed sensitive paths; no blanket flakiness claim from polling sleeps. |
| 23 | Performance benchmarks | NOT_VERIFIED | No representative p50/p95, cold-start, large-corpus EXPLAIN/throughput or memory/disk workload benchmark executed. No performance guarantee or speculative schema change offered. |
| 24 | Evaluation science / held-out model set | N/A | Phases 0–15 contain no model/reranker/OCR/local-judge evaluation engine. Phase 17–22 plans are not runtime evidence. |
| 25 | Dependencies / licenses / vulnerabilities | NOT_VERIFIED | Build compatibility, managed/pinned versions and dependency analysis checked; no reliable fresh CVE/license/SBOM scan. No vulnerability-free claim. |
| 26 | Build / static / tooling / CI | PASS | Fresh clean verify/JaCoCo/SpotBugs/PMD/CPD, Modulith and DB/storage/HTTP tests; hook tests. No hosted CI pipeline present or required by an accepted current contract; local governance is the current gate. |
| 27 | Logging / observability | PASS | Safe exception/event severity and production log scan; privacy fixtures green. Trace correlation optional under current security/API docs; no operational alert/drill claim. |
| 28 | Documentation / governance | PASS | Current root/README/roadmap/ADR/package-tree/workflow instructions cross-checked; local links and diagrams green; historical phase/status narratives remain historical. Full dictionary/external-link checking unavailable/unperformed. |
| 29 | Hygiene / delivery | PASS | Full tracked tree, placeholders/caches/casing, source hashes and clean diff checks; only new supplemental audit/recommendation documents permitted. |

## Findings

All findings are **OPEN**, verified defects rather than architectural preferences. Each affects frozen code;
**Frozen baseline impact: owner approval required** for a new bounded maintenance scope. The smallest proposed
corrections need no schema change, new module dependency or reversal of an accepted ADR. Historical ADR-0018
authorization does not automatically authorize these newly discovered items.

### SBA-001 — Vault lifecycle and metadata mutations use stale trash state

- **Severity:** Medium. Categories 8/10/15/22; highest relevance to Phase 17 source authority.
- **Exact locations:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/entry/VaultEntryService.java:61`
  and `:77` (`moveToTrash` / `restore`); `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/application/metadata/VaultMetadataService.java:192`
  and `:228` (`attachTag` / `getEntryOrThrow`); `backend/src/main/java/com/vhvkhangg/personalprivatevault/vault/internal/infrastructure/persistence/VaultEntryRepository.java:6`.
- **Evidence:** outer transaction reads active entry; another transaction commits trash; outer `attachTag`
  succeeds and persists a tag on the trashed entry. On a second entry, outer `restore` returns
  `deletedAt=null` while a fresh JDBC check still reports the row trashed. Both were executed against PostgreSQL.
- **Root cause:** `findById` returns an already managed entity; mutation gates and idempotent transition decisions
  have no parent-row lock/authoritative refresh or version check. Statement-level fresh database state does not
  refresh the first-level JPA cache automatically.
- **Impact / risk:** existing trash-write prohibition can be bypassed in public-operation composition;
  successful restore results can contradict persisted state. Future RAG must not authorize source activation or
  restoration from these stale projections. No active RAG leakage is asserted because that runtime does not exist.
- **Proposed remediation:** one Vault-owned guard on the existing physical `vault_entries` row, held to transaction
  completion; refresh before lifecycle/metadata state checks. Use the same guard as the accepted Phase 17 source
  fence design, with an explicit order for parent and metadata locks. Preserve owner contracts and schema.
- **Regression risk / required tests:** Medium; new locking can change contention/order and deadlock behavior.
  PostgreSQL barriers must cover preloaded active/trashed entities, trash/restore in both orders, all six protected
  metadata mutations versus trash, no-write rejection, idempotent favorites/ratings/tags and actual competing locks.
  Keep existing first-rating/unique-tag tests. Phase 17 must also exercise its final-check-to-commit gate against
  lifecycle changes using this same physical guard.
- **Phase 17 blocking:** **Yes, before runtime implementation acceptance** if these owning operations participate
  in source eligibility. Preparation remains accepted; a separate completed maintenance slice is not mandatory if
  the owner explicitly includes and verifies this repair in the bounded Phase 17 guard work.
- **Frozen baseline impact:** owner approval required for new maintenance scope; the narrow correction preserves
  schema, dependency directions and accepted ADRs. **Status:** OPEN.

### SBA-002 — Partial settings updates lose unrelated committed changes

- **Severity:** Medium. Categories 2/8/10/15/22.
- **Exact locations:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/settings/internal/application/configuration/AppSettingsService.java:54`
  (unlocked lookup), `:108` / `:113` / `:122` (omitted-field merge), `:125` (whole state update);
  `backend/src/main/java/com/vhvkhangg/personalprivatevault/settings/internal/domain/AppSettings.java:14`;
  `backend/src/main/java/com/vhvkhangg/personalprivatevault/settings/internal/infrastructure/persistence/AppSettingsRepository.java`.
- **Evidence:** outer transaction reads pagination 20; independent partial update commits pagination 50;
  outer update supplies only timezone `Asia/Bangkok`. Final row: `{timezone=Asia/Bangkok, pagination_size=20}`.
  The successful unrelated pagination update is discarded.
- **Root cause:** omitted fields merge from cached/unlocked singleton state, followed by a full entity write;
  no locking/version check protects read–merge–write. This is a partial-update preservation breach rather than an
  objection to intentional full replacement semantics.
- **Impact / risk:** preferences, auto-lock or backup configuration changes can silently disappear when requests
  overlap or public services compose in a transaction.
- **Proposed remediation:** lock and refresh the existing singleton before deriving omitted values, hold through
  commit; retain the separate missing-row initialization/default path. Do not introduce a generic settings framework.
- **Regression risk / required tests:** Medium; preserve initialization, currency/reference/default and backup
  interval semantics. Test two disjoint updates in both orders, a preloaded persistence context, true two-thread
  contention, final persisted fields and backup enable/interval coherence. Check no unrelated field changes.
- **Phase 17 blocking:** **No** for the current foundation source-authority scope. Schedule bounded maintenance;
  require correction before a later feature treats settings mutation as an atomic configuration authority.
- **Frozen baseline impact:** owner approval required for new maintenance scope; no schema/ADR change needed for
  the proposed correction. **Status:** OPEN.

### SBA-003 — Feed ingestion bypasses a committed source disable

- **Severity:** Medium. Categories 2/8/10/15/22.
- **Exact locations:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java:47`
  / `:50` (early enabled check), `:131` / `:133` (later lock/refresh), `:139` / `:140` (fetch mutation).
  Required contract: `docs/implementation/phase-10/README.md:170` — source must exist and be enabled.
- **Evidence:** outer transaction caches enabled source; independent transaction disables it; outer calls
  `ingestFetch`. Returned item count 1; final row has `enabled=false`, non-null `last_fetched_at` and persisted
  item count 1. The committed disabled state was observed by the final refresh but not enforced.
- **Root cause:** enabled is checked before the authoritative lock/refresh; refreshed state only controls the
  scheduling timestamp. The decision is never revalidated before commit.
- **Impact / risk:** a disabled source still accepts ingestion and fetch bookkeeping. Reusing this pattern for
  future RAG source authority would fail a revocation gate, although Feed is excluded from the initial RAG corpus.
- **Proposed remediation:** recheck enabled immediately after the existing locked refresh and reject atomically,
  rolling back the already staged batch. Moving the lock ahead of the entire batch is a broader alternative and
  must preserve existing unique-contention outcomes; the final recheck is the smaller initial correction.
- **Regression risk / required tests:** Medium; assert no item insert/update or fetch/schedule timestamp change
  when the source was disabled before the authoritative gate. Include preloaded source, actual disable contention,
  both permitted serialization orders, atomic rollback, dual-key conflicts and authoritative schedule regressions.
- **Phase 17 blocking:** **No** for initial Fiction/Film/external-file authority. Fix before later Feed integration
  or any consumer reuses this enabled gate for authorized ingestion.
- **Frozen baseline impact:** owner approval required for new maintenance scope; preserve existing ingestion
  semantics and dependency/schema contracts. **Status:** OPEN.

### SBA-004 — Markdown frontmatter detection truncates metadata and rejects valid empty blocks

- **Severity:** Medium. Categories 12/20/22; document-fidelity risk for later RAG reuse.
- **Exact locations:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/importdata/internal/parsing/MarkdownImportParser.java:43`
  / `:47` / `:59`. Preservation contract: `backend/src/main/java/com/vhvkhangg/personalprivatevault/importdata/AGENTS.md:42`
  and `docs/implementation/phase-10/README.md:522`.
- **Evidence:** the valid YAML key `---custom: retained` is mistaken for the closing delimiter. The first fixture
  below returns `VALID`, frontmatter `{title=Example}`, summary `null`; raw Markdown is still exactly preserved.
  The second fixture is valid empty frontmatter followed by a body thematic break; it returns `INVALID`.

```text
---
title: Example
---custom: retained
summary: Keep me
---
Body
```

```text
---
---
Paragraph
---
More
```

- **Root cause:** substring search for `\n---` accepts delimiter prefixes, and its start index skips the newline
  preceding an immediately adjacent empty-block terminator. A later body thematic break may then be parsed as
  part of YAML.
- **Impact / risk:** structured summary/frontmatter silently disagree with stored source text; valid notes may be
  rejected. This is metadata loss, not deletion of the original Markdown. A later corpus adapter copying the parser
  or trusting its parsed metadata would inherit incomplete provenance/content interpretation.
- **Proposed remediation:** recognize complete delimiter lines and handle adjacent empty frontmatter, LF/CRLF
  and delimiter-at-EOF. Preserve the raw string unchanged and keep the existing safe YAML parsing policy.
- **Regression risk / required tests:** Medium; delimiter changes must not alter raw text or reinterpret ordinary
  body thematic breaks. Include both fixtures, normal/empty LF and CRLF blocks, prefix-looking keys, closing marker
  at EOF, unknown/nested keys, malformed YAML and numeric precision. Verify preview, execution and persisted Note
  frontmatter/summary, with no target writes on rejection.
- **Phase 17 blocking:** **No** for foundation authority. Fix before reusing this importer/its structured metadata
  in an admitted corpus. Phase 18's separately implemented external Markdown adapter must independently satisfy
  fidelity tests; this report does not authorize that implementation or require it to reuse this parser.
- **Frozen baseline impact:** owner approval required for new maintenance scope; preserve exact raw text and
  existing schema/module/format contracts. **Status:** OPEN.

### SBA-005 — PIN changes accept a superseded current PIN and overwrite a successful change

- **Severity:** Medium. Categories 13/15/21/22.
- **Exact locations:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/application/privatepin/PrivatePinService.java:51`
  / `:52` / `:58` / `:75`; `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/domain/AppUser.java:18`;
  `backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/infrastructure/persistence/AppUserRepository.java:16`.
  Current-PIN requirement: `docs/implementation/phase-2/handoff.md:80`.
- **Evidence:** outer authenticated transaction verifies old PIN and retains the managed user; independent change
  commits a new PIN; outer change still supplies the old PIN and succeeds. Both operations report success; fresh
  verification retains only the outer change. Probe output: `bothChangesSucceeded=true`,
  `firstCommittedChangeRetained=false`, `lastChangeRetained=true`.
- **Root cause:** current hash validation and write use an unlocked, unversioned singleton which can remain stale
  across a competing commit. The current-PIN check is not atomic with the credential change.
- **Impact / risk:** a superseded credential satisfies a later change and one successful change disappears.
  This is a credential-consistency defect; it does not demonstrate JWT bypass, unauthenticated access or database
  encryption failure. The PIN remains the accepted UI/application gate under ADR-0008.
- **Proposed remediation:** lock the existing owner user row, refresh before checking the current hash, and retain
  the guard through the update commit. Preserve ordinary verification reads and password/hash compatibility.
- **Regression risk / required tests:** Medium; avoid deadlocks or changes to singleton/bootstrap/refresh behavior.
  PostgreSQL barrier tests in both winner orders, a cached-user variant, wrong-current and anonymous denials,
  exactly one accepted competing change, correct final PIN and unchanged username/email/password. Retain full
  password-range and refresh-token regressions. Existing `PrivatePinIntegrationTest.java:100` is sequential only.
- **Phase 17 blocking:** **No** for preparation or the current RAG foundation runtime. RAG privacy must use its
  separately approved source authority, not PIN state.
- **Frozen baseline impact:** owner approval required for new maintenance scope; no schema change or reversal of
  ADR-0008's PIN model needed. **Status:** OPEN.

### SBA-006 — JSON import accepts an invalid document's trailing content silently

- **Severity:** Medium. Categories 12/21/22.
- **Exact locations:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/importdata/internal/parsing/JsonImportParser.java:22`
  / `:29`; existing malformed-input test `backend/src/test/java/com/vhvkhangg/personalprivatevault/importdata/ImportDataValidationTest.java:185`.
- **Evidence:** both inputs below return a `VALID` Note item, importing only the first root array:

```text
[{"title":"Example","contentMarkdown":"Body"}] {"ignored":true}
[{"title":"Example","contentMarkdown":"Body"}] garbage
```

- **Root cause:** `readTree` is used without requiring end-of-input. The root-array check validates the consumed
  first value but never checks the remaining supplied document.
- **Impact / risk:** malformed/concatenated input is reported as successful while trailing data is ignored;
  imported records need not represent the whole supplied file. This is parsing/completeness failure, not an
  established arbitrary-code or SQL-injection exploit.
- **Proposed remediation:** require exactly one complete JSON document, using Jackson's trailing-token failure
  setting or an explicit end-of-input check. Preserve existing privacy-safe invalid-JSON errors and transaction
  failure behavior; do not change accepted object-field or numeric precision semantics.
- **Regression risk / required tests:** Low–Medium; valid trailing whitespace must remain accepted. Cover a
  second object/array/scalar, arbitrary trailing garbage, normal whitespace/newlines, malformed first value,
  decimal fidelity, empty arrays and complete prepare/preview/execute rollback/no-target-write assertions.
- **Phase 17 blocking:** **No** for current foundation. Fix before reusing existing parsed imports as trusted
  complete-document provenance or expanding the eligible module set to their results.
- **Frozen baseline impact:** owner approval required for new maintenance scope; enforce the existing valid-JSON
  contract without adding a new format or API. **Status:** OPEN.

### SBA-007 — Person numeric-width validation overflows on extreme positive exponents

- **Severity:** Low. Categories 12/17/21/22.
- **Exact locations:** `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/internal/application/PersonService.java:221`
  / `:222`; numeric request fields at `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/internal/web/dto/CreatePersonRequest.java:18`
  and `backend/src/main/java/com/vhvkhangg/personalprivatevault/people/internal/web/dto/UpdatePersonRequest.java:18`.
  Physical widths: `backend/src/main/resources/db/migration/V1__create_schema_v1.sql:154` / `:155`.
- **Evidence:** `new BigDecimal("1E+2147483647")` has precision 1 and scale -2147483647. The owning validator accepts
  it because integer-digit arithmetic yields **-2147483648**. Both Jackson 2 and Jackson 3 deserialize the same
  numeric token into `CreatePersonRequest.heightCm`; there is no DTO numeric constraint that compensates.
- **Root cause:** `precision() - scale()` is evaluated as `int` and wraps before comparison with the four-integer-digit
  limit. The value is positive and has no fractional-scale violation, so the other guards do not catch it.
- **Impact / risk:** an impossible `numeric(6,2)` measurement bypasses intended pre-persistence domain rejection.
  Downstream serialization/database failure is a risk; the extreme value was not sent to JDBC and no particular
  HTTP 500, OOM, crash or persisted corruption is claimed. Low severity reflects the extreme authenticated input.
- **Proposed remediation:** overflow-safe digit arithmetic, e.g. widen before subtraction, or a bounded magnitude
  comparison. Preserve accepted precision/scale and positive-value rules; no schema widening or invented human
  height/weight limit is justified.
- **Regression risk / required tests:** Low; height and weight, create and update, extreme exponent signs and
  ordinary `9999.99`/`10000`/fractional boundaries. Assert `InvalidPersonException` before any Person/Vault write
  and existing HTTP rejection semantics. Existing `PersonValidationTest.java:114` / `:140` cover ordinary widths,
  not overflow.
- **Phase 17 blocking:** **No**; People is outside the initial approved corpus.
- **Frozen baseline impact:** owner approval required for new maintenance scope; unchanged numeric(6,2) schema
  and existing positive-value contract. **Status:** OPEN.

## Observations, architectural preferences and limits

These do not enter a remediation scope automatically.

1. **Duplicated search text helpers / coverage concentration.** CPD identifies repeated snippet/cleaning/escaping
   logic, including 140-line blocks at `fiction/internal/application/search/FictionSearchService.java:216` and
   `location/internal/application/search/LocationSearchService.java:272` beneath the normal backend package.
   JaCoCo shows many owner `extractSnippet` methods unexecuted while Knowledge Note/global integration carries
   most snippet coverage. No semantic drift was established: the inspected matcher uses `regionMatches(true, ...)`,
   not unsafe offsets into a separately lowercased string. Prefer a behavior matrix across current adapters before
   considering consolidation. A new global helper/module would need a concrete ownership/dependency-safe ADR
   case; repetition alone is not permission to violate the root/shared boundary.
2. **Static classifications are not 239 bugs.** SpotBugs reports 20 nullable-return warnings, 98 constructor-throw
   warnings, 118 exposed-representation warnings, two broad catches and one redundant null check. Guarded stable
   getters, constructor validation, JPA/framework collaborators and deliberate privacy-safe parser/cleanup catches
   explain reviewed categories. PMD's 57 include imports/simplifications and bounded dead-code observations.
   No blanket suppressions, warning cleanup project or unproved critical defect is recommended.
3. **Accepted snapshot/search choices.** Phase 12 permits mixed committed snapshots; source
   `docs/implementation/phase-12/README.md:497` explains it. Account follower reads are deliberately bounded first-page
   previews; complete enumeration would be a separately approved public capability, not a demonstrated acceptance
   breach. `VaultSearchService.java:114` / `:115` combines best tag similarity with independently selected tag text;
   provenance clarity may merit future owner review, but the current contract does not promise winner-tag citation.
4. **Portability and source revisions are not RAG authorization.** ADR-0017 intentionally exports retained owner
   content including private/trash/history, while excluding security tables. It must not be passed wholesale into
   a RAG corpus. Existing Vault `updatedAt` is not a domain-content revision; accepted Phase 17 already requires
   owning revision projections and eligibility/field allowlists. These are understood integration boundaries,
   not grounds to reopen P17-1/2/3 or assert a currently implemented RAG leak.
5. **Resource/operations measurement remains open.** Search qualifies tag/trash candidates by page loops;
   imports intentionally materialize full jobs; exports use cursor batches but materialize row values and spool
   a complete ZIP. No representative workload benchmark, heap/disk admission experiment, API-timeout fault
   campaign or host-crash recovery drill was run. Measure before selecting RAG workload budgets. Do not impose the
   ingestion cap rejected under ADR-0018 or change indexes from intuition.
6. **Deployment/SCA/observability scope.** No hosted CI pipeline or fresh vulnerability/license/SBOM scan was
   verified. Current local Maven/manual owner governance is the accepted workflow. Internet throttling, public
   bootstrap hardening, TLS/cookies and deployment protections remain expressly deferred; optional safe trace
   correlation would improve diagnosis but is not an unmet frozen contract. This report makes no deployment or
   vulnerability-free certification.
7. **Programmatic blank password policy.** `BootstrapService` accepts the 12–128-character rule while login/HTTP
   DTOs reject blank values. HTTP bootstrap prevents that trigger. Clarify the narrow programmatic whitespace
   policy before altering the accepted password contract; this observation is not a reopening of historical BA15-3.
8. **Historical documentation.** ADR-0018/phase closeout narratives retain intermediate pending statuses alongside
   later closure sections. Current root/roadmap/Phase 17 documents establish the current gate. Preserve historical
   records; optional editorial clarification must not rewrite their evidence or revive old finding dispositions.

No recommendation requires an ADR change as its smallest correction. If implementation instead proposes an
entity-version column, new module edge, global technical package or changed public business/error contract,
stop for an explicit ADR/schema/compatibility decision and synchronized canonical artifacts.

## Final disposition

This supplemental audit establishes residual implementation defects despite a passing full suite. Its strongest
RAG integration consequence is **SBA-001: authoritative Vault lifecycle state must be guarded before Phase 17
runtime acceptance**. The current preparation acceptance remains intact. The proposed correction should use the
already accepted source guard design rather than a competing infrastructure layer.

All seven corrections require a new bounded owner scope decision; no remediation handoff was created. The
historical Phase 15 `COMPLETE — FROZEN` status and `BACKEND_AUDIT_READY` evidence remain preserved. Unmeasured
performance and unperformed SCA/license checks remain explicit limitations; this is not a clean-system or release
certification. The accompanying maintenance recommendation provides the priority and acceptance gates.

**Next step:** decide the bounded maintenance scope, including how SBA-001 will be verified before Phase 17 runtime
acceptance; create a handoff only in a separate authorized workflow.
