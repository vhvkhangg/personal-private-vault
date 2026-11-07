# Independent final review — GDI26-S2

- **Date / reviewer / mode:** 2026-11-06 / Codex / initial independent implementation-slice review, resumed without restarting or advancing a counter.
- **Disposition: CHANGES_REQUESTED.** Two **MEDIUM / OPEN** findings, FR-01 and FR-02, block acceptance. No READY_FOR_OWNER_COMMIT or commit message.
- **Scope / baseline:** [GDI26-S2 executable handoff](../executable-handoff-s2-2026-11-06.md), D00–D10 and its explicit exceptional initial-implementation authorization. Local HEAD remains `fb6994a0e83d34cb442ca4a1f1e694f17c0db1f6`. Entry ACTIVE was IMPLEMENTED_AWAITING_CODEX_REVIEW. This reviewer authored neither the submitted implementation nor its self-check.
- **Counter:** Antigravity **0/3 consumed**, unchanged. **One exceptional initial Codex implementation pass** is complete and independently **CHANGES_REQUESTED**; no corrective round, exhaustion, second takeover or S1 authority transfer is invented. The next ordinary Antigravity corrective/re-review cycle would be 1/3.
- **Write boundary:** this report and current review/status fields in ACTIVE and the executable handoff. Production and submitted tests remain unchanged; reviewer probes and receipts are isolated under ignored logs. Prior reviews, Owner decisions, implementer handback and all earlier evidence remain preserved.

## Interruption recovery and verified checkpoint

The Owner's resume instruction continues this same review. Read from repository root: current HEAD/Git status, AGENTS.md, ACTIVE, S2 Owner scope/executable/handback, `docs/workflow/resume-prompts/COMMON.md` and `docs/workflow/documentation-synchronization.md`. The last scope-specific Docs Sync Check is the implementer handback's scoped source/schema/API/package/operations/diagram checkpoint, followed by the independent checks below. No unrelated documentation cleanup follows.

| Step | Recovery classification / actual state |
| --- | --- |
| Scope, role, HEAD, root instructions and handoff | **COMPLETED / RECHECKED:** same HEAD/scope, valid implementation-review entry and explicit initial exception; no AGENTS/agent/hook/config diff. Older CSA26/phase routing notices remain historical, not new execution authority. |
| Source/test inspection and recursive package audit | **COMPLETED / REUSED:** all 1,841 recorded backend inputs still match `backend-inputs-before.json`; no invalidating source drift. New file subtree, controller/DTO, S1 boundary amendments and related tests inspected. |
| Eight named Maven groups | **COMPLETED / REUSED:** actual finished receipts, exit 0, complete capture/cleanup and fresh selected XML; 369 cases pass. No command remains interrupted and no unchanged test group was rerun on resume. |
| Independent counterexamples and duplicate control | **COMPLETED / REUSED:** two genuine reviewer assertion failures, actual expected duplicate-bypass assertion failure and restored green, all finished/captured/cleaned. Nonzero results remain nonzero, not reclassified as passing evidence. |
| Scoped docs/source/export inspection | **COMPLETED / REUSED:** protected Owner/preparation-review/handback hashes match; 14 changed Markdown files/140 prose links resolved before this report; module/functional inventories, aliases and four exported views checked. |
| Formal finding record and current status | **PENDING at resume, now UPDATED:** this report and ACTIVE/handoff to CHANGES_REQUESTED, preserving submission/history. |
| Invalidated / execution blocker | **NONE / NONE:** no source drift, unfinished process or broken guard requires a rerun/repair. Acceptance is blocked by the two confirmed defects, not by missing permission or an infrastructure failure. |

## Independent verification and preserved evidence

Evidence root: `logs/gdi26/codex-s2-independent-review-1793968776298/`. All required groups executed independently in this review before interruption, through the existing normal-profile **300s** watchdog. Selected freshly written XML was copied immediately after each group. Source fingerprints and receipt exit/capture/cleanup were checked again on resume.

| Exact selector | Tests / failures / errors / skips | Receipt / elapsed |
| --- | --- | --- |
| DriveS2FolderIntegrationTest,DriveS2UploadIntentIntegrationTest | 20 / 0 / 0 / 0 | `g1-pg.json`, 43.453s |
| DriveS2ProviderTest | 50 / 0 / 0 / 0 | `g2-provider.json`, 50.313s |
| DriveS2StreamingIntegrationTest,DriveS2UploadRecoveryIntegrationTest | 28 / 0 / 0 / 0 | `g2-io-recovery.json`, 65.641s |
| DriveS2WebIntegrationTest,DriveS2AuthorityIntegrationTest,DriveS2ConfigurationIntegrationTest | 47 / 0 / 0 / 0 | `g3-auth.json`, 47.110s |
| DriveApiInventoryIntegrationTest,DriveConfigurationIntegrationTest,DriveCredentialCryptoTest,DriveGoogleOAuthClientTest,DriveOAuthSecurityTest,DriveGrantRevocationIntegrationTest,DriveProviderRegressionTest,DriveWebIntegrationTest,DrivePersistenceIntegrationTest | 113 / 0 / 0 / 0 | `g4-s1.json`, 114.281s |
| SecurityFilterChainIntegrationTest,AuthWebIntegrationTest,OpenApiRouteInventoryIntegrationTest | 17 / 0 / 0 / 0 | `g4-security-api.json`, 45.625s |
| ApplicationArchitectureTests,FlywayV1SchemaManifestIntegrationTest,PortabilityIntegrationTest | 55 / 0 / 0 / 0 | `g4-schema-export.json`, 56.797s |
| AccountWebIntegrationTest,MediaStorageIntegrationTest,FeedSchedulerLifecycleIntegrationTest | 39 / 0 / 0 / 0 | `g4-adjacent.json`, 62.156s |
| **Distinct named-gate total** | **369 / 0 / 0 / 0** | **145 S2 + 224 compatibility**; no full-backend suite |

Each group used this command template with the exact selector and receipt above:

~~~powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt logs/gdi26/codex-s2-independent-review-1793968776298/GROUP.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=SELECTOR' test
~~~

`selected-suite-summary.json`, eight `GROUP-surefire/` directories and receipt/stdout/stderr retain actual counts and exits. `backend-inputs-before.json`, `backend-inputs-after.json` and `backend-input-drift.json` cover 1,841 tracked/untracked backend inputs with **zero drift**; protected historical-document hashes are in `protected-docs-before.json`. Tests use the existing Java 25/Boot 4.1.1/Modulith 2.1.1/JUnit/PostgreSQL Testcontainers baseline and controlled signed-OAuth/Drive HTTP fixtures. No live Google, real credentials, model, RAG or deployment execution.

**Independently repeated actual duplicate control:** same `DriveS2UploadRecoveryIntegrationTest#lostResponseAndRestartKeepsOneProviderIdentity`, with `'-Dgdi26.s2.test.reallocateOnRetry=true'`. The test-only fixture deletes its disposable action row after fake Google actually accepts F and loses the reply. Restart/retry actually creates G. `duplicate-control.json`: exit **1**, 31.078s; fresh XML **1 test, 1 assertion failure, 0 errors/skips**, AtomicInteger(2) versus required 1 mutation. Same selector without the flag: `duplicate-restored.json`, exit **0**, 28.078s, **1/0/0/0** with original ID and GET-only recovery. No production bypass flag exists. Both runs are separate from the 369 distinct positive cases.

**Additional reviewer checks:** ignored `GdiS2IdleProbe.java` reproduces the actual controller copy loop against real loopback HTTP with DB authority isolated; its incremental-reader control completes the identical bytes. Then ignored `GdiS2ReviewCases.java` executes two extra JUnit cases through the existing Spring/Tomcat/PostgreSQL HTTP fixture. Both extra cases fail actual assertions (**2 found, 0 passed, 2 failed, 0 skipped**); there is no setup/compilation failure. `extra-run.json` records exit **1**; `extra-cases-summary.json` retains assertion types/messages. It uses the already installed matching JUnit Platform Launcher 6.0.3, not a new project dependency. `probe-compile/run.json` and `extra-compile/run.json` use 60s/45s/60s/180s watchdog bounds and separate argument files/classes. Diagnostic completion exit 0 is not behavioral acceptance; the actual failed JUnit run exits 1.

## Blocking findings

### GDI26-S2-FR-01 — active download progress is mistaken for an idle transfer

- **MEDIUM / OPEN; S2-12/14.** `backend/src/main/java/com/vhvkhangg/personalprivatevault/drive/internal/web/controller/DriveFileController.java:110,112`; `drive/internal/files/http/DriveFileResources.java:85–95`.
- The controller blocks in `input.readNBytes(... up to 64KiB/remaining)` and updates `op.progress()` only after that full requested block returns. The idle timer observes no progress while a healthy provider keeps delivering partial bytes, so it cancels the response before that read completes. This introduces an undeclared minimum per-block throughput, rather than enforcing the handoff's no-progress limit.
- **Reproduction:** the actual configured HTTP fixture has a 2s idle / 15s transfer budget. A 100-byte response delivers one byte every 50ms: no 2s idle gap, roughly 5s total. Real Spring/Tomcat download returns **504 after 2,107ms**, not 200/original bytes (`real-http-continuous-progress.json`). The isolated actual-controller reproduction returns 504 after 2,217ms; the same production provider/limits with progress observed per actual partial read completes all bytes in **5,106ms**, uncancelled (`continuous-progress-probe.json`). Existing large TRICKLE tests only establish cancellation of transfers far beyond the total budget; they do not test this valid continuous-progress case.
- **Minimal correction:** observe progress when bytes are actually read, including partial reads inside bounded block accumulation. Preserve the 64KiB memory cap, expected-length/EOF validation before releasing the final chunk, current-authority checks, total/idle deadlines, and incomplete-response behavior. Do not merely increase timeouts or disable cancellation.
- **Required regression:** a real HTTP continuously progressing small body whose total duration exceeds idle but remains below transfer/operation caps must complete exactly. Retain headers-only/no-progress and excessive-duration/size/disconnect tests so stalled transfers still stop and release admission/spool/streams. No new feature or Owner choice is needed.

### GDI26-S2-FR-02 — OpenAPI exposes the async framework task instead of JSON response payloads

- **MEDIUM / OPEN; precise HTTP/OpenAPI contract and S2-20, with S2-01/02 inventory coverage limits.** `backend/src/main/java/com/vhvkhangg/personalprivatevault/drive/internal/web/controller/DriveFileController.java:45,54,67–74`; actual generated `/v3/api-docs`.
- Ensure/list/upload return `WebAsyncTask<Void>` and manually serialize root ApiResponse payloads. Their response annotations do not describe the concrete JSON envelope/data schemas. The generated contract consequently declares **`*/* → WebAsyncTaskVoid`** for ensure 200, list 200 and upload 200/201. That schema exposes `callable`, `timeout`, `executor` and `beanFactory` instead of the actual root/status, file/page DTO and ApiResponse fields. This is a false wire contract, not just a missing cosmetic description or a request to move shared APIs.
- **Reproduction:** the independent real HTTP OpenAPI capture is `openapi.json`; `json-openapi-probe.json` records all four incorrect success response entries. `jsonSuccessResponsesMustDescribeTheirWireSchemas` fails an actual assertion. The submitted test verifies 243 operations, security, upload request and binary download schema, but not the JSON success response shapes.
- **Minimal correction:** describe the actual `application/json` success responses using the existing root ApiResponse contract and module-local Root/Page/File DTOs for their correct 200/201 statuses. Keep the internal async task out of the public schema. Use a proportionate annotation/typed-contract solution; no new common module, alternate envelope or global HTTP framework rewrite.
- **Required regression:** inspect actual generated OpenAPI and resolve the success schemas through their references; prove envelope/data fields and JSON media types for each response, exclude framework-task fields, and retain 235/239/243 counts, bearer security and binary/callback exceptions. Runtime JSON and published schema must agree.

## Acceptance adjudication and prior LOW

| Case | Independent disposition |
| --- | --- |
| S2-01 | **PASS** for actual 235 OFF / 239 S1-only / 243 full contexts, contradictory flags and default-OFF behavior. Response-schema completeness is FR-02, not proved by counts. |
| S2-02 | **PASS** for actual JWT/Owner/slot denials, no Google dispatch, unchanged S1 callback matcher; no new login/permission. |
| S2-03 / 04 / 05 | **PASS:** first U/V roots, durable IDs/UUIDs, About lookup, concurrent winner, retained unknown/late response and restart/read-only reconciliation; stale creation cannot publish READY. |
| S2-06 / 07 / 08 | **PASS:** conjunctive root proof, bounded direct listing and context-bound cursors, private field projection and copied/native/foreign/moved negatives. |
| S2-09 / 10 | **PASS:** seven opaque original types; zero/exact 50MiB content; known/chunked excess rejected before provider mutation/intent; bounded admission/spool/cleanup. |
| S2-11 | **PASS:** durable fingerprint/action/provider identity, pre-dispatch recovery, live-loser exclusion, current-generation finalization, retained uncertainty, 8/1024 capacity/prune boundaries and actual failed/restored duplicate control. |
| S2-12 | **BLOCKED FR-01:** normal original bytes/attachment/security headers pass, but a supported continuously progressing download fails. |
| S2-13 | **PASS:** native/export/shortcut/folder/copied/foreign/moved/marker denials; no content dispatch for unapproved metadata. |
| S2-14 | **FAIL FR-01:** actual idle semantics are incorrect despite green interruption/short/excess/stall cleanup cases. |
| S2-15 / 16 | **PASS:** cross-subject/root proof; current epoch/root/fence checks at dispatch/finalization, paused disconnect/regrant and matching-epoch invalid_grant handling. |
| S2-17 | **PASS** for classified provider refusals/quota/malformed/redirect/header/size failures, no automatic replay or sensitive reflection. False-idle functional failure is FR-01. |
| S2-18 / 19 | **PASS:** actual V26/JPA/DBML/integrity and security export exclusions, module verification, unchanged v1/V25/POM and selected S1/Account/Media/Feed compatibility. |
| S2-20 | **PARTIAL / BLOCKED FR-02:** source/package/ADR/schema/operations/current diagrams and separate live-operation limits are truthful; generated API JSON payload contract is wrong. |

**GDI26-S2-PH-L01 — independently VERIFIED / CLOSED.** The executable matrix distinguishes a recovered never-dispatched PREPARING/RESERVED row from a live loser and any ever-dispatched action. Source CAS/live-claim ownership and both variants of `durablePredispatchClaimResumesExactlyOnceAfterRestart`, repeated-loser, lost-response and retention tests verify that clarification. This closure is narrow; it does not accept the whole slice. S1 FR-L01/FR-L02/PH-L01 and unrelated historic LOW registers remain unchanged.

## Scope, architecture and shared coverage

Recursive inspection covers the **24 new files-subtree Java files**, including its meaningful package descriptors, plus the new controller/DTO in established web packages, related S1 boundary amendments, V26 and all eight S2 test classes/three fixtures. Existing S1 descriptor debt remains its separate LOW; no new missing file-capability package descriptor or stray generated source was found. SC-01 placement preserves the established controller-package rule and green architecture test; no architecture relaxation is required.

| Dimensions | Disposition / evidence and limits |
| --- | --- |
| 1 Owner intent | **PASS:** four connection-owned file operations, D00–D10/non-goals and two default-OFF flags; no S3/Picker/native conversion/delete/sync/RAG activation. |
| 2 traceability | **FINDING FR-01/02:** the green gate misses healthy progress and actual JSON response schema; case dispositions above retain the distinction. |
| 3 architecture; 4 module boundaries | **PASS:** same Drive leaf/zero business edge, explicit V26/ADR-0027 and narrow Portability exclusions. S1 nonce/custody/revoke behavior retains separate validation and fresh compatibility evidence. |
| 5 tree; 6 shared contracts | **PASS for package/root-envelope ownership**, with separate S1 LOW debt; **FINDING FR-02** for generated wire representation. No arbitrary common module required. |
| 7 SOLID; 8 DRY/KISS; 9 patterns | **PASS:** concrete authority/folder/intent/proof/transport/resources/web roles; shared S1 guard/CAS; bounded action ledger, no connector platform/interface hierarchy/dependency. New servlet bridge serves the approved preparse/deadline boundary; FR-01 needs a narrow correction, not a framework rewrite. |
| 10 business; 11 public API | **FINDING FR-01 / FR-02** respectively. |
| 12 input validation | **PASS:** strict slot/key/metadata/provenance/native MIME/filename/multipart/header/page/cursor/size boundaries, exact projections and tested negatives. |
| 13 auth/privacy; 14 credential/key handling | **PASS for inspected deterministic boundary:** current S1 subject/epoch/fence at dispatch, private token lease, signed refresh identity/scope, no token/marker/SQL/provider-body reflection, external key/spool defaults and security export exclusions. Actual host ACL/backup/topology is separately NOT_VERIFIED. |
| 15 persistence/concurrency | **PASS:** root-locked bounded admission/prune, unique global key plus subject/root tuple, CAS epochs, dispatch recorded before mutation, no SQL/guard over provider waits, safe exact-ID uncertainty and late-write denials. |
| 16 failure/recovery; 18 resources | **FINDING FR-01** for real no-progress semantics. Durable recovery, actual 50MiB/excess, complete metadata bodies, stream/spool/admission cleanup and finite request/cursor/intent counts otherwise pass; no benchmark/SLA extrapolation. |
| 17 database | **PASS:** V26-only two metadata relations/JPA CHAR/status/null/FK/CAS agreement, real PostgreSQL/schema/export tests, no H2 or older migration rewrite. |
| 19 retrieval; 20 document/OCR fidelity; 24 evaluation | **N/A:** originals are opaque bytes; no parser/model/index/recommendation/evaluation capability changed or executed. |
| 21 adversarial/security tests | **PASS for scoped source/epoch/two-account/provenance/duplicate/negative evidence**; no live provider or multi-JVM certification inferred. |
| 22 tests | **FINDING FR-01/02 coverage gaps:** 369 named cases pass, two new real assertions fail; actual expected duplicate mutation-control failure/restoration is separately proved. No skipped mandatory case is hidden. |
| 23 performance benchmarks | **N/A:** no approved benchmark/model-quality project; actual finite size/deadline/admission checks were executed. Repeated DB authority checks while streaming are bounded/deliberate; no speculative cache or optimization imposed. |
| 25 dependencies | **PASS:** POM unchanged, existing managed Java/Spring/Nimbus/JDK/JUnit reused; diagnostic launcher already installed and not a shipped dependency. No new download/upgrade/supply-chain audit claimed. |
| 26 build/static diagnostics | **PASS for fresh focused Maven/architecture/schema/API/adjacent gates.** Reviewer IDE/static analyzer NOT_VERIFIED; supplied IDE SQL-resolution errors concern unchanged Portability queries with no configured datasource, contrasted with passing real PG/export evidence. Byte Buddy/CDS/Lombok/unchecked-test warnings are disclosed, not suppressed; no warning-free claim. |
| 27 logging | **PASS for inspected paths:** redacted lease/spool/file/action carriers and fixed errors; no credentials, file bytes, filenames, private markers or provider payloads added to diagnostics. |
| 28 docs/governance | **PASS for scoped source/ADR/schema/operations/link/export consistency and preserved history; FINDING FR-02** for actual OpenAPI output. Current acceptance/status now records these findings. |
| 29 delivery/hygiene | **PASS:** scoped changes/SC-01 placement, new package documentation, ignored reviewer probes, zero source drift and no agent staging/publishing. |

## Docs Sync Check and bounded correction route

- **UPDATED:** this review plus ACTIVE/handoff independent disposition, two exact findings/next route and PH-L01 closure. Implementer handback remains the original non-independent submission, not rewritten acceptance.
- **ALREADY_CONSISTENT:** verified scope/HEAD/Owner decisions, default Drive/files/Feed OFF, no module edge/new dependency, v1/V25 and accepted S1/phase history. `docs-link-check-final.json` found no missing target among 140 prose links in 14 touched Markdown files before this report; fenced PowerShell was correctly excluded. Final review/status verification is recorded in `final-review-check-02.json`: 38 local links resolve, source/protected-document hashes match, all 14 command receipts are finished/captured/cleaned, `git diff --check` passes and the index is empty. The preserved first saved-state check failed only its literal finding-ID check against ACTIVE's combined shorthand; both full IDs are now explicit. Git's CRLF-normalization notices are disclosed, not whitespace errors or source changes.
- **DIAGRAMS:** `diagram-check.json` confirms module DOT/draw.io/SVG exactly agree at 21 nodes/41 edges, functional draw.io is 41/40, both module SVG/PNG aliases are byte-identical and affected XML parses. Four delivered PNGs were viewed and retain suspended/inactive RAG/default-OFF boundaries. Actual generator provenance/limits remain in handback; formal Graphviz/Structurizr semantic regeneration NOT_VERIFIED. Historical S1 REVIEW_PENDING badges remain deferred under their original disposition.
- **DEFERRED_OUT_OF_SCOPE:** S1 LOW records, legacy root/scoped AGENTS/policy/agent routing, submitted S1 badges, SAD/history, unrelated repository cleanup B and S3/later features. No broad synchronization or new handoff is opened by this resumed review.
- **BLOCKED_OWNER_DECISION:** none for FR-01/02's minimal fixes. If Antigravity is still unavailable, selecting/authorizing another exceptional corrective implementer is the Owner's staffing action; the prior initial exception does not automatically authorize a new Codex correction.
- **NOT_VERIFIED:** actual Google/Cloud/client/browser/permission/quota/key/spool ACL/backup/one-JVM/HTTPS/deployment operation, all expressly separately gated. They are not counted as passing fake evidence or newly imposed prerequisites for these deterministic fixes.

Return only FR-01/02 to the authorized implementer under this handoff: correct real read-progress accounting and actual OpenAPI JSON schemas, add the two focused regressions, retain existing authority/EOF/cancellation/duplicate controls, update relevant handback/API/operations evidence as needed and self-review. Run affected S2 streaming/HTTP/OpenAPI groups and any genuinely affected boundaries through the existing watchdog; qualify unchanged evidence by source inputs. No full-backend rerun, production redesign, activation or unrelated LOW cleanup is required. Then obtain a separate independent `$codex-final-review`; do not declare self-acceptance.

**Next step:** Antigravity | `/antigravity-implement-handoff`, mandatory `/antigravity-self-review`, then separate Codex `$codex-final-review` | GDI26-S2 ACTIVE/executable contract and FR-01/02 in this review | CHANGES_REQUESTED; implementer availability required, otherwise Owner must separately assign/authorize correction; no automatic Codex takeover | Antigravity 0/3 consumed; exceptional initial Codex implementation 1 complete, independently CHANGES_REQUESTED.
