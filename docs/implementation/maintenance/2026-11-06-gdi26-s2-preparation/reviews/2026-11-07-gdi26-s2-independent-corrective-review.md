# Independent corrective review — GDI26-S2

- **Date / reviewer / mode:** 2026-11-07 / Codex / independent implementation-slice corrective review in a separate session. This reviewer authored neither the implementation nor its corrective pass.
- **HEAD:** `fb6994a0e83d34cb442ca4a1f1e694f17c0db1f6`, unchanged; the reviewed delivery is the existing uncommitted working tree.
- **Authority:** [executable GDI26-S2 contract and corrective exception](../executable-handoff-s2-2026-11-06.md#exceptional-owner-authorized-corrective-codex-pass--2026-11-07), D00–D10, [corrective handback](../implementation-handback-s2-2026-11-06.md#exceptional-corrective-pass--2026-11-07), and Owner invocation of `$codex-final-review`. ACTIVE entered as IMPLEMENTED_AWAITING_CODEX_REVIEW.
- **Disposition: CHANGES_REQUESTED / ENVIRONMENT_BLOCKED.** GDI26-S2-FR-01 and GDI26-S2-FR-02 are independently **VERIFIED / CLOSED**. No new blocking code defect was found. Mandatory S2-19 Media compatibility remains **NOT_VERIFIED**; therefore there is **no READY_FOR_OWNER_COMMIT and no commit message**.
- **Counter:** Antigravity **0/3**, unchanged. Exceptional initial Codex implementation **1 COMPLETE**, historically independently CHANGES_REQUESTED; separate exceptional corrective pass **1 COMPLETE**, its two corrections independently verified, slice acceptance still blocked by Media. No Antigravity remediation round, exhaustion, additional takeover or S1 authority transfer is claimed.
- **Write boundary:** this report and current review/status fields in ACTIVE and the executable handoff. Submitted source/tests, handback, prior review, Owner decisions, hooks/configuration, historical evidence and unrelated RAG changes are preserved.

## Requirement and simplest-mechanism check

The corrective scope remains two concrete fixes: observe positive partial download reads as progress, and describe the actual JSON success payloads in OpenAPI. The controller uses an inner bounded read loop and module-local schema annotations over the existing root `ApiResponse`; there is no new envelope, generic framework, dependency, database change, timeout increase or activation. Both Drive flags remain OFF by default.

The existing four-operation S2 contract is still the acceptance boundary. Real Google/Cloud/browser credentials, host key/spool ACLs, one-JVM deployment and operational activation are expressly separate gates. They are not inferred from fake-provider tests, and are not newly imposed as prerequisites for these two corrections.

## Finding and verification register

| ID / requirement | Severity / blocking now | Independent adjudication | Evidence / minimal remaining action |
| --- | --- | --- | --- |
| GDI26-S2-FR-01 | MEDIUM; no longer blocking | **VERIFIED / CLOSED** | `DriveFileController.java:120–132` checks cancellation before each partial read and calls `op.progress()` only for a positive count. The final block remains withheld until exact EOF/current-authority checks. Real HTTP known-length and chunked 100-byte bodies, arriving one byte per 50ms, finish exactly after exceeding the injected 2s idle budget and before the unchanged 15s transfer cap. Fresh streaming tests also retain headers-only/partial stalls, the total-deadline TRICKLE assertion, premature EOF/excess, disconnect, 50MiB hash equality and cleanup. |
| GDI26-S2-FR-02 | MEDIUM; no longer blocking | **VERIFIED / CLOSED** | Controller annotations at lines 46–48, 58–60 and 77–83 describe ensure/list 200 and upload 201/200 as `application/json`, composing root ApiResponse with module-local Root/Page/File DTOs. `DriveS2WebIntegrationTest.java:109–174` resolves actual generated `$ref`/`allOf`, compares schemas with real HTTP success payloads/statuses and excludes async-task fields. Fresh inventories retain 235/239/243, bearer protection, the exact public callback and binary schema. |
| S2-19 / mandatory MediaStorageIntegrationTest | Mandatory verification; **blocking**, no code severity assigned | **NOT_VERIFIED / ENVIRONMENT_BLOCKED** | Supplied fresh-laptop XML records `ContainerFetchException` in `startMinioAndCreateBucket`, before Media assertions, for `minio/minio:RELEASE.2024-11-07T00-52-28Z`. This reviewer independently confirms that exact image is absent locally. Owner must make the exact approved image available; then rerun the selected adjacent gate. No image substitution, registry/configuration repair, Media edit, blind repeated pull or waiver occurred. |

The [2026-11-06 initial review](2026-11-06-gdi26-s2-independent-final-review.md) remains immutable, including its original CHANGES_REQUESTED finding descriptions and historical results. This report supersedes only the current FR-01/02 disposition. GDI26-S2-PH-L01 stays narrowly VERIFIED/CLOSED under that prior review; all S1 LOW registers remain unchanged.

## Independent verification

New evidence root: `logs/gdi26/s2-independent-corrective-review-1794050210348/`. All new Maven runs used the existing `scripts/run-bounded-command.py`, normal profile, **300s** cap, repository-root cwd, Java **25**, Maven **3.10.0**, real PostgreSQL/Testcontainers and controlled OAuth/Drive HTTP fixtures. No full backend suite, live account, RAG/model/QA execution or runtime installation was performed.

| Exact Maven selector | Tests / failures / errors / skips | Receipt / exit / elapsed |
| --- | --- | --- |
| DriveS2StreamingIntegrationTest,DriveS2UploadRecoveryIntegrationTest,DriveS2WebIntegrationTest,DriveS2AuthorityIntegrationTest,DriveS2ConfigurationIntegrationTest,SecurityFilterChainIntegrationTest,AuthWebIntegrationTest,OpenApiRouteInventoryIntegrationTest | **95 / 0 / 0 / 0** | `affected-http-security.json`; **0**, 110.657s |
| ApplicationArchitectureTests,FlywayV1SchemaManifestIntegrationTest,PortabilityIntegrationTest | **55 / 0 / 0 / 0** | `architecture-schema-export.json`; **0**, 47.094s |
| **Distinct fresh independent total** | **150 / 0 / 0 / 0** | Complete capture/cleanup; no full-suite claim |

Both commands use this exact form with the selectors above:

~~~powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt EVIDENCE_ROOT/GROUP.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=SELECTOR' test
~~~

Fresh selected XML was copied before a subsequent Maven invocation and checked against receipt start times; copied XML hashes and exact counts are in each `GROUP-surefire/selected-suites.json`. `fresh-verification-summary.json` records both completed receipts. The corrected progress cases take approximately five seconds, rather than falsely cancelling at the idle threshold. Their original failed assertions were also inspected in the supplied evidence; they were genuine HTTP 504 versus 200 failures, not setup failures.

### Supplied evidence inspected and source applicability

The corrective evidence root `logs/gdi26/s2-codex-corrective-20261107T103655Z-539778d6d4904fe4824bff6e084e0557/` is available and was independently inspected. `supplied-evidence-audit.json` verifies all 12 named receipt exits, finished/capture/cleanup fields, stdout/stderr byte lengths and selected XML hashes. Eight final groups contain **339 distinct passing cases plus one Media initialization error**, with no duplicate identities, assertion failures or skips. This is source-qualified implementer evidence, **not 339 newly executed independent tests**; it overlaps the 150 fresh cases and must not be added to them.

The same audit inspects the genuine three-case pre-fix failure, three-case post-fix pass, actual duplicate-bypass assertion failure at **two remote mutations versus one**, and identical restored passing case. Those controls remain separate from positive counts. No control was weakened or replayed by modifying production code in this review.

`supplied-source-applicability.json` directly rehashes all **1,842** named backend inputs from the corrective verification manifest: **zero missing inputs and zero hash drift**. Git-visible inventory contains 1,841 backend files; the additional manifest input is ignored `backend/.env`, which was hashed directly without exposing its contents. The preliminary Git-only audit's apparent omission is therefore an inventory-method difference, not source drift. Compared with the corrective pass entry, the only changed backend files are the controller and its streaming/web regressions; the other four differences are the declared current docs. The reviewed existing S2 schema, provider, authority, resources, S1 and adjacent-module inputs are unchanged from that verification.

Initial-implementation and initial-review raw evidence roots remain unavailable on this laptop. Their dated review documents are historical reviewed records, not freshly inspected receipts. This review does not recreate or silently substitute those missing artifacts.

## Acceptance matrix

| Cases | Result | Evidence / limits |
| --- | --- | --- |
| S2-01 / S2-02 | **PASS** | Fresh real configuration, 235/239/243 OpenAPI, JWT/Owner/slot/callback/security tests; default OFF and no broader public matcher. |
| S2-03–S2-06 | **PASS** | Current folder/authority/provider code inspected; hash-qualified supplied G1/G2 evidence proves first ensure, one retained identity, restart/read-only reconciliation and conjunctive root proof. |
| S2-07–S2-11 | **PASS** | Current cursor/proof/upload-ledger/parser/transport inspected; supplied PostgreSQL/provider cases and actual failed/restored duplicate control checked. Upload recovery and original-byte boundary/cleanup cases also rerun independently. |
| S2-12 / S2-14 | **PASS; FR-01 CLOSED** | Fresh progressing known-length/chunked downloads, stall/total-deadline/short/excess/disconnect tests, exact zero/50MiB bytes and cleanup. |
| S2-13 | **PASS** | Source-qualified provider negative matrix excludes native/folder/shortcut/copied/foreign/moved/invalid-marker content; no native export/abuse path. |
| S2-15 / S2-16 / S2-17 | **PASS** | Fresh authority/HTTP/recovery denials and inspected S1 guard/lease boundary; source-qualified provider/refresh negative cases. No live-provider certification inferred. |
| S2-18 | **PASS** | Fresh 38 architecture, 2 schema and 15 Portability tests; V26/JPA/DBML inspection, 69/67 baselines and sensitive integration exclusions. |
| S2-19 | **NOT_VERIFIED / BLOCKING for Media** | S1 113, Account 1 and Feed 5 are source-qualified passing supplied evidence; mandatory Media assertions did not execute. Historical previous-machine Media pass cannot substitute for this failed required gate. |
| S2-20 | **PASS for scoped technical documentation / FR-02 CLOSED** | Actual generated JSON success schemas match HTTP payloads; source/package/ADR/schema/runbook links checked. Separately gated provider/host/deployment/formal-diagram-tool proof remains NOT_VERIFIED, as already authorized. |

## Shared coverage and mandatory dimensions

The actual recursive inspection includes **all 24 Java files** under `drive/internal/files`, all six package descriptors, and the controller/DTO in the established web packages. The application exception remains Drive-local, repositories/entities stay internal, and no new public business API/enum/command hierarchy was introduced. `recursive-tree-inspection.json` records paths, hashes, packages and imports; no missing descriptor or unexpected artifact was found. Accepted S1 package-descriptor LOW debt remains separate.

| Checklist dimensions | Disposition / rationale and evidence |
| --- | --- |
| 1 intent; 2 traceability | **PASS for two corrections; NOT_VERIFIED for mandatory S2-19.** Four selected operations/non-goals and the acceptance matrix above remain authoritative. |
| 3 architecture; 4 module ownership | **PASS:** same Drive leaf, no business dependency edge, approved additive V26/ADR-0027 and unchanged S1 ownership; fresh architecture/export/schema tests. |
| 5 recursive tree; 6 shared contracts | **PASS:** complete subtree inspection, coherent capability roles/placement; root ApiResponse/ApiResponses deliberately reused under ADR-0016; actual OpenAPI correction verified. No arbitrary common module requested. |
| 7 SOLID; 8 DRY/KISS; 9 patterns | **PASS:** concrete authority/folder/intent/proof/provider/resources/servlet roles; existing S1 guard/CAS reused; partial-read loop and annotations solve the demonstrated defects without a new abstraction. |
| 10 business; 11 public API | **PASS for corrected behavior:** exact final EOF, original bytes, correct success media/envelope/DTO/status, bearer/private routes and binary exception. Whole-slice acceptance still awaits S2-19. |
| 12 validation | **PASS:** inspected slot/key/name/MIME/ID/marker/multipart/size/header/cursor bounds plus source-qualified negative tests; no broadened input path. |
| 13 auth/privacy; 14 credentials | **PASS for deterministic changed boundary:** current S1/subject/epoch/root/fence checks, redacted lease and token custody, unchanged nonce validation; fresh denials. Actual host/provider permissions/ACL/deployment are separately **NOT_VERIFIED**. |
| 15 transactions/concurrency; 16 recovery; 17 database | **PASS:** short guard/CAS/root-row transactions, no provider wait under SQL/guard, original-ID uncertainty retention, bounded prune and real PostgreSQL verification; V1–V25/POM unchanged. |
| 18 resources/performance | **PASS for approved bounds:** <=64KiB copy accumulation, fixed total/idle/operation deadlines, two admissions/spools, bounded page/ledger/cursor work and exact 50MiB proof. No new per-item request loop or speculative optimization. |
| 19 retrieval; 20 parser/OCR; 24 evaluation | **N/A:** opaque original binary storage; no RAG, parser, model or evaluation capability changes. |
| 21 security tests; 22 test quality | **PASS for inspected/rerun scoped cases and genuine controls; NOT_VERIFIED for Media.** Real HTTP/PostgreSQL, actual expected assertion failures, authority/EOF/stall/boundary/recovery cases; no H2 or hidden skip. |
| 23 benchmarks | **N/A:** no approved benchmark/SLA/model-quality work; finite resource/deadline acceptance is covered under 18. |
| 25 dependencies | **PASS:** POM unchanged and existing managed Java/Spring/JUnit facilities reused; no dependency/runtime installation or broader supply-chain audit claimed. |
| 26 build/static diagnostics | **PASS:** 150 fresh focused cases and architecture/schema/export compile/run successfully. **NOT_VERIFIED:** complete IDE lint; the actual lint call returns `items:[]`, `more:true`, so it proves no warning-free result. Existing Maven metadata 401, JVM/test diagnostics remain disclosed, not suppressed. |
| 27 logging | **PASS for inspected paths:** fixed sanitized errors and redacted carriers; no new credential/header/marker/file-content logging or provider-body reflection. |
| 28 docs/governance | **PASS for scoped current status/evidence/link consistency:** current independent closure and remaining mandatory blocker are recorded here/ACTIVE/executable; submission/history remains preserved. |
| 29 hygiene/delivery | **PASS:** no production/test/config edits by reviewer, no generated source/stray .gitkeep in the new subtree, no agent staging/publishing, and final source/protected-document/link/whitespace checks recorded below. |

## Scope-bounded Docs Sync Check

- **UPDATED:** this new independent corrective review, ACTIVE and executable current review/status/next action. FR-01/02 are CLOSED here, while mandatory Media remains NOT_VERIFIED and the slice stays CHANGES_REQUESTED. The implementer handback remains its original submission, not rewritten as acceptance.
- **ALREADY_CONSISTENT:** D00–D10, both default-OFF flags, unchanged schema/DBML/package/module/API/operations structure and frozen history. `google-drive-files.md` accurately describes positive partial progress and explicit JSON schemas. Derived submission/review-pending labels remain acceptance-pending; ACTIVE and this report give the current independent disposition. No READY or publication claim follows.
- **DEFERRED_OUT_OF_SCOPE:** S1 LOW/badges, legacy AGENTS/policy notices, unrelated RAG and `.codex/config.toml` entry drift, historical cleanup and S3/activation. No broad docs rewrite, hook/trust repair or scope expansion.
- **BLOCKED_OWNER_DECISION / ENVIRONMENT:** Owner must supply the exact approved MinIO image before remaining mandatory verification. The captured prior Docker pull 404/access denial does not establish whether tag availability or registry access policy caused it; current independent inspection proves local absence only. Any image/source/registry/configuration substitution needs a separate concrete disposition, not reviewer improvisation.
- **NOT_VERIFIED:** mandatory Media compatibility, unavailable historical raw roots, complete IDE/current-client native interception, actual Google/Cloud/browser/host key/spool ACL/topology/deployment and formal diagram-tool validation. Diagram/source structure is unchanged from the corrective entry; no redundant regeneration or wider certification is claimed.
- **Saved-state verification:** `final-state-check.json` records current review/status links, unchanged backend/protected submission/Owner/history inputs, scoped whitespace check, index preservation and completed receipt capture/cleanup after saving the review. No existing log was overwritten, truncated, relocated or deleted.

## Exact remaining action

There is no remaining FR-01/02 code correction and no reason to spend a production remediation round on the image prerequisite. Owner first makes `minio/minio:RELEASE.2024-11-07T00-52-28Z` available locally. The remaining expressly **test-only** action is the existing `g4-adjacent` selector `AccountWebIntegrationTest,MediaStorageIntegrationTest,FeedSchedulerLifecycleIntegrationTest`, through the unchanged normal 300s watchdog, with new receipts/fresh selected XML and source applicability. Preserve failed initialization evidence; do not retag/substitute, change Media or waive the gate. Antigravity test-only handback/self-review then independent Codex final review completes the ordinary route. If Antigravity remains unavailable, the Owner may invoke Codex `$codex-final-review` after provisioning the exact image so the reviewer can run the missing named verification without implementing code. No new exceptional implementation authorization is inferred.

**Next step:** Owner | make the exact approved MinIO image available locally | GDI26-S2 S2-19 / this review and executable handoff | then Antigravity `/antigravity-test-slice` (selected adjacent gate only), self-review and separate Codex `$codex-final-review`; if Antigravity remains unavailable, Owner invokes `$codex-final-review` after provisioning | Antigravity 0/3; exceptional initial implementation 1, separate corrective pass 1; no new round/takeover.
