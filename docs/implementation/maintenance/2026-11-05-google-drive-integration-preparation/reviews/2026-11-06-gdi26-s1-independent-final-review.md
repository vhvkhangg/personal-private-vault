# Independent slice review — GDI26-S1

- **Date / reviewer / mode:** 2026-11-06 / Codex / initial independent implementation-slice review.
- **Status: CHANGES_REQUESTED.** Five HIGH and four MEDIUM findings block acceptance; one new LOW hygiene finding is nonblocking.
- **Baseline / authority:** HEAD `304708e6bb5c1a8e45e89e2bdb9026d9ae4e8002`, Owner D00-D07 and the [executable S1 handoff](../executable-handoff-s1-2026-11-06.md). Entry ACTIVE was IMPLEMENTED_AWAITING_CODEX_REVIEW. Preparation PH-01/02/03 closure remains historical preparation acceptance, not runtime closure.
- **Remediation:** **0/3 consumed**. This is the initial independent review, not correction round 1. Antigravity's next submitted correction is remediation 1/3; takeover NOT_AUTHORIZED. Earlier slices/counters are unaffected.
- **Review write boundary:** this report, executable-handoff review/status/capture-instruction fields and ACTIVE only. Production and submitted test sources were not edited. Diagnostic sources/results are preserved under ignored logs.

## Outcome and independent evidence

All **141 submitted named-gate tests passed independently**, with zero failures/errors/skips. They do not establish the missing behavior: **13 isolated reviewer checks produced results contrary to the handoff**. These are diagnostic observations, not 13 failing JUnit cases. The diagnostic harness exits 0 on completion; that exit does not mean its behavioral expectations passed.

Evidence root: `logs/gdi26/codex-s1-review-1793942945569/`.

| Fresh command group | Tests / failures / errors / skips | Receipt / duration |
| --- | --- | --- |
| Drive*Test,Drive*Tests | 30 / 0 / 0 / 0 | `drive.json`, 46.704s |
| SecurityFilterChainIntegrationTest,AuthWebIntegrationTest,OpenApiRouteInventoryIntegrationTest | 17 / 0 / 0 / 0 | `security-api.json`, 40.578s |
| ApplicationArchitectureTests,FlywayV1SchemaManifestIntegrationTest,PortabilityIntegrationTest | 55 / 0 / 0 / 0 | `architecture-schema-export.json`, 47.094s |
| AccountWebIntegrationTest,MediaStorageIntegrationTest,FeedSchedulerLifecycleIntegrationTest | 39 / 0 / 0 / 0 | `adjacent.json`, 58.250s |

Each Maven group used `python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <unique-group.json> -- mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=<selector> test`. The Drive invocation also supplied the handoff's report-directory property; actual XML still appeared in `backend/target/surefire-reports`. Fresh selected XML was copied into unique `*-surefire-snapshot/` directories before another group could overwrite it. Full stdout/stderr and wrapper receipts are preserved.

`GdiReviewProbe.java`, Java argument files, `probe-compile.json`, `probe-run.json`, `probe-run.stdout.log`, `review-probe-results.json` and `review-source-manifest.json` preserve the diagnostic reproduction and 48 changed backend source/test/resource hashes. Compilation and execution used the existing test classpath, standard Java/Nimbus/MockMvc and a controlled loopback HTTP server, through 60s/45s normal-profile watchdog calls. No live Google exchange, real account, production key, model or RAG runtime was used.

**Diagnostic limits:** service callback probes use actual application/attempt/lifecycle/controller classes with synthetic OAuth responses and an in-memory repository proxy; they are not PostgreSQL race proof. Wrong-audience verification uses a real controlled RSA-signed token and actual Nimbus verification. The body-deadline probe uses the real Java HTTP client and loopback response streaming. The 141 JUnit cases independently exercise the supplied PostgreSQL/application tests. No full-backend suite, IDE inspection or separate static analyzer was run.

## Blocking findings

Production references below are relative to `backend/src/main/java/com/vhvkhangg/personalprivatevault/drive/`; test references are under the corresponding `backend/src/test/java/.../drive/` subtree.

### GDI26-S1-FR-01 — stale or cancelled callbacks still acquire connection authority

- **HIGH / OPEN; S1-04/05/09.** `internal/oauth/OAuthAttemptManager.java:36,82,140`; `internal/application/DriveConnectionService.java:102,176,225,278`.
- New authorize never invalidates the previous attempt in that slot. Attempt state stores no authenticated Owner/slot-generation ordering; final writes never recheck current attempt status, expiry or generation. `invalidateSlotAttempts` removes PROCESSING work while its callback continues. A callback begun on an empty slot can disconnect that slot during code exchange, then commit CONNECTED afterward because the empty disconnect creates no subject fence and the cancelled callback is not rechecked.
- Reproduced `superseded_pending_callback_connected=true`, `cancelled_processing_callback_connected=true`, and `expired_processing_callback_connected=true`; all expected false. Cancellation also releases admission capacity before external processing actually ends. Final insertion uses `save` on a stale pre-network empty-row snapshot; assigned-ID JPA merge is not an insert-only/current-slot authority check.
- **Minimal correction:** one canonical final admission/write gate shared by CONNECTED and RECONNECT_REQUIRED: stored Owner/context, monotonically ordered slot/attempt generation, current status and expiry checked at commit; invalidate older pending/processing authority on new start/disconnect while retaining in-flight resource accounting until work finishes. Re-read/check current slot identity and use guarded insert/update semantics rather than merging over a row that appeared during provider I/O. Keep Google I/O outside the local/SQL guard.
- **Proof:** real PostgreSQL barriers for replaced, expired and empty-slot-disconnected callbacks; competing different-subject callbacks into a previously empty slot; no overwrite/resurrection, zero late success and capacity not freed while old I/O remains active.

### GDI26-S1-FR-02 — browser proof selection and terminal cookie handling break two-slot flows

- **HIGH / OPEN; S1-02/03/05.** `internal/web/controller/DriveController.java:85,97,117,130`.
- Callback takes the first cookie named for either slot, rather than resolving the slot from server-side state. With both legitimate cookies present, a slot-2 callback uses slot-1 proof and fails. Failure paths unconditionally delete that chosen slot's cookie, including unknown/stale callbacks, so they can erase a newer valid proof. Scalar RequestParam binding does not reject duplicate security parameters or code-plus-error ambiguity before exchange.
- Reproduced slot-2 callback with both cookies **400 instead of 200**; unknown callback cleared the current cookie; duplicate code parameters caused **one token exchange instead of zero**.
- **Minimal correction:** derive the expected slot/proof from stored attempt authority, validate all security-parameter cardinalities/ambiguities first, and clear a proof only when it belongs to the terminal attempt and cannot delete a newer proof. Preserve the exact public GET and fixed plain-text result.
- **Proof:** enabled Spring/MockMvc cases with both cookies in both orders, failed/replaced older callbacks after a new start, unknown state, duplicate state/code/error, and code-plus-error. Verify no exchange for malformed guards and no deletion of a current unrelated/newer cookie.

### GDI26-S1-FR-03 — PH-03 cutoff admits attempts begun during the old revoke

- **HIGH / OPEN; S1-08/09/10.** `internal/oauth/SubjectLifecycleManager.java:122,154,195,218`; callback final gates in DriveConnectionService.
- Cutoff is set only when revoke is reserved. Successful completion does not advance it to cover the revoke interval; an attempt initiated after reservation but before HTTP completion passes once the state becomes REVOKE_ACCEPTED. `recordConnected` then clears the cutoff even if older callbacks remain. The generation counter is never used to enforce attempt freshness.
- Reproduced `during_revoke_attempt_accepted_after_200=true`, expected false, with a deterministic clock and actual lifecycle manager. This violates the specific accepted PH-03 rule independently of ordinary SQL epoch CAS.
- Capacity/reservation also needs its specified completion: normal successful records are never retired, and RECONNECT_REQUIRED first-grant rows do not reserve a lifecycle record. Retained history must not permanently prevent account replacement or leave a durable row without capacity for local disconnect.
- **Minimal correction:** retain an interval/order cutoff that rejects every pre/during-revoke attempt after completion and fresh reconnection; retire it only after no such attempt/processing callback can commit. Reserve capacity for every durable connection/recovery row, keep uncertain fences, and fail closed at admission without making local disconnect impossible. No durable coordinator or new schema is required.
- **Proof:** same-slot and other-slot attempts initiated during the paused revoke but completed after 200, fresh post-completion grant, older callback arriving after that grant, completed-cutoff retirement/capacity and RECONNECT_REQUIRED cleanup. Keep unrelated V independent.

### GDI26-S1-FR-04 — response streaming escapes the provider deadlines

- **HIGH / OPEN; S1-08/11.** `internal/oauth/DriveOAuthClient.java:120,134,149,241,249,275`; callback chain in DriveConnectionService.
- `HttpClient.send(...ofInputStream())` returns on headers and `readBoundedBody` performs blocking reads without a body deadline. Request timeout does not make this loop a five-second body read. No shared 30s callback deadline or remaining-budget propagation exists; JWKS acquisition also creates a new RemoteJWKSet per verification instead of the specified bounded complete-chain acquisition.
- Real loopback probe sent headers plus a partial body; the request was **still reading after 6.5s**, exceeding the configured five-second response budget. The reviewer released it afterward; no uncontrolled hang was left. Revoke uses the same unbounded body loop and can leave PENDING rather than terminating within eight seconds.
- **Minimal correction:** bound body completion, JWKS size/fetches and the entire callback/revoke operation with effective deadlines/cancellation/stream cleanup, using the remaining budget for each step. Maintain zero mutation retries and finite admission. A byte cap alone is insufficient. [Java's streaming body contract](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/java/net/http/HttpResponse.BodySubscribers.html#ofInputStream()) documents the header/body distinction.
- **Proof:** slow/trickled token, JWKS and revoke bodies, headers without completion, oversized bodies and redirects; measure termination/resource release and correct 504/UNCERTAIN within the handoff budgets.

### GDI26-S1-FR-05 — identity and refresh validation accept invalid provider results

- **HIGH / OPEN; S1-03/06/07.** `internal/oauth/DriveOAuthClient.java:174,241`; `internal/application/DriveConnectionService.java:131,157`.
- Audience validation accepts either expected aud **or** matching azp. A matching authorized party is not a substitute for this application's required audience. A real signed wrong-audience token with matching azp was accepted by the current verifier. [Google's ID-token validation](https://developers.google.com/identity/openid-connect/openid-connect) requires the app's audience.
- Candidate validation treats an HTTP-success parsed refresh response as usable without validating required access-token/type/expiry fields or a supplied refresh ID-token subject/scopes. An all-null refresh response produced CONNECTED in the actual service. Same-subject token-omission recovery can perform two validation refreshes, exceeding the stated one-refresh chain and risking an unnecessary second rotation/failure.
- **Minimal correction:** require correct aud and validate azp where applicable; validate refresh response semantics and returned identity/scope context before persisting/rotating authority. Perform at most one candidate-validation refresh; preserve only a validated token. Apply matching-epoch RECONNECT_REQUIRED behavior for invalid current credentials without destroying a different valid generation.
- **Proof:** signed aud/azp mismatch matrix; empty/malformed success, absent/unusable access token, changed refresh subject/scope, token omission/rotation and invalid_grant; exact refresh count and prior-row preservation/downgrade assertions.

### GDI26-S1-FR-06 — production configuration and effective scopes exceed the approved boundary

- **MEDIUM / OPEN; S1-01/06/11.** `internal/config/DriveConfiguration.java:98,102`; `internal/config/DriveProperties.java`; `application.yml`; `internal/oauth/DriveOAuthClient.java:46,232`.
- Enabled startup checks only nonblank redirect, not the exact D07 localhost URI. All provider endpoints are runtime/environment configurable without a production host/TLS allowlist; controlled-test injection is not isolated from production configuration. Scope validation uses an incomplete blacklist and accepts additional unapproved Drive activity permission.
- Reproduced acceptance of a non-localhost redirect and of drive.activity alongside the selected scopes. These are boundary-validation failures, not authorization to activate/configure live OAuth.
- **Minimal correction:** enforce the exact approved redirect and fixed trusted HTTPS provider endpoints in normal configuration; isolate fake endpoints/keys in controlled test wiring. Allow only the selected effective scope set and the approved email equivalent, rejecting broader/unapproved grants.
- **Proof:** real enabled/misconfigured context tests for non-localhost redirect and untrusted/plain-HTTP endpoints before any egress, plus full scope allowlist/alias negatives. No real secret/provider needed.

### GDI26-S1-FR-07 — secret-bearing records have generated raw toString output

- **MEDIUM / OPEN; S1-07/11 and credential-custody contract.** `internal/oauth/DriveTokenResponse.java:7`; CreationResult in OAuthAttemptManager; AuthorizeResult in DriveConnectionService.
- Default record toString includes Google access/refresh/ID tokens and browser proof/consent data. A synthetic marker in refreshToken was present in toString. The handoff explicitly excludes secret-bearing DTO/debug strings; ordinary successful wire redaction does not satisfy that requirement.
- **Minimal correction:** redact/remove sensitive generated string forms for token/proof-bearing carriers and ensure provider/SQL exceptions do not retain unsafe diagnostic payloads. Keep intentional authenticated consent-URL response fields intact; no broad logging framework is needed.
- **Proof:** safe synthetic marker checks for toString, failure/log paths and serialized responses; distinguish intentional feature data from diagnostics. This review demonstrates unsafe string generation, not an observed live-account token disclosure.

### GDI26-S1-FR-08 — mandatory regression/control evidence is not supplied by the green gate

- **MEDIUM / OPEN; G1-G6 and named-gate definition of done.** `DriveGrantRevocationIntegrationTest.java:208`; `fixture/FakeGoogleOAuthServer.java:247`; implementation handback and evidence directory.
- The purported fence negative control never bypasses the fence or executes a failing mutant/control run. It tests a consumed/cancelled pre-disconnect attempt and refreshes an unissued R2 after invalidation; this cannot prove the intended delayed-revoke/new-grant regression. The provider fixture revokes whichever global forcedSubject is current at completion, rather than the subject bound to the supplied token; concurrent V can therefore change which account the fake revokes.
- The named timeout test exercises an HTTP 500, not an ambiguous timeout. Missing late/during-cutoff, supersession, cookie and deadline assertions explain why the 141 cases remain green while the reviewer checks fail.
- Claimed unique Surefire report directories do not exist: the submitted run contains four receipts and stdout/stderr only. The ineffective report-directory option originated in the Codex handoff sample; this capture defect is not solely an implementer error. Fresh reviewer XML snapshots are preserved, and the handoff capture instructions are corrected without changing production/tooling source.
- **Minimal correction:** bind fake issued tokens/codes to subject/project; add the specific failing regressions, real timeout/late response and correct refresh wire assertions; execute the required failed fence-bypass control in isolated test machinery and restored green run with separate immutable receipts. Preserve fresh per-group XML/input/source evidence and correct per-case/self-review/handback claims. No new framework or full suite required.
- **Proof:** actual control-failure receipt plus restored pass; current G1-G6 coverage mapping with test names, command/source hashes, counts/skips and valid report paths. A successful named gate alone is insufficient.

### GDI26-S1-FR-09 — canonical schema and current architecture documentation diverge

- **MEDIUM / OPEN; S1-12/13, G6.** `docs/database/google-drive-connections.dbml:16,18`; ADR-0026; new Drive authority/operations docs; current diagram sources/exports and handback.
- Canonical DBML declares display_email NOT NULL varchar(255), while V25/JPA/approved contract use nullable varchar(320); it models a native enum while the migration uses varchar plus CHECK. Envelope documentation omits the version byte and the epoch is described as monotonic although it is a randomized UUID.
- ADR-0026 links to nonexistent maintenance owner-decisions.md / an unsubstantiated decision identifier rather than the actual D00-D07 record, and remaps decision labels. Current DOT sources add Google/Drive, but corresponding current SVG/PNG and canonical module draw.io remain unchanged and contain no Drive/Google delta. Architecture README explicitly directs reviewers to those exports. Dated SAD snapshots are separately excluded and need no regeneration.
- New documents claim Implemented & Verified and describe the stale-attempt/deadline behavior as delivered despite the findings above. The evidence/handback also overstates timeout/control/report preservation.
- **Minimal correction:** within authorized implementation-adjacent docs, align additive DBML with V25/JPA, fix provenance/envelope/epoch and capability statements, synchronize applicable current sources/exports (or accurately classify unaffected views), and report actual test/operational limits. Preserve frozen v1, old migrations, preparation/Owner decisions, independent reviews and SAD snapshots. No unrelated documentation cleanup or new product decision.
- **Proof:** schema comparison, scoped local-link validation, source-to-export inventory/visual verification for changed current views, and truthful handback after the behavior fixes.

## Nonblocking hygiene

**GDI26-S1-FR-L01 — LOW / OPEN.** Eight meaningful production packages under Drive lack package-info.java: application, config, crypto, exception, oauth, persistence and both web leaf packages. Only the module root has a descriptor. Add the required owner-local package documentation in a consolidated hygiene pass; this alone does not block. Grouping and zero business-module edges are otherwise sensible. Existing GDI26-PH-L01 and unrelated LOW registers remain unchanged.

## Acceptance adjudication

| Case | Independent status / evidence |
| --- | --- |
| S1-01 | PASS for default-OFF startup/no active handlers/provider/key loading and normal V25; real enabled validation remains affected by FR-06. |
| S1-02 | FAIL: basic JWT/exact GET matcher pass, but callback guard/cardinality/cookie handling fails FR-02. |
| S1-03 | FAIL: two-slot happy paths pass supplied tests; both-cookie flow and usable-credential validation fail FR-02/05. |
| S1-04 | FAIL: basic slot/subject constraints pass; superseded callbacks/final slot authority fail FR-01. |
| S1-05 | FAIL: replay pre-check exists, but replaced/cancelled/expired processing callbacks commit; FR-01/02. |
| S1-06 | FAIL: signature/basic claims tests pass; aud/azp and effective-scope checks fail FR-05/06. |
| S1-07 | FAIL: AES-GCM/physical constraint tests pass; empty refresh validation and unsafe debug carriers fail FR-05/07. |
| S1-08 | FAIL: local deletion and basic 200/500 quarantine work; remote deadline and complete control proof fail FR-04/08. |
| S1-09 | FAIL: direct PostgreSQL epoch CAS passes; service callback cancellation can resurrect an empty slot, FR-01. |
| S1-10 | FAIL: during-revoke cutoff accepted after completion; required negative control missing, FR-03/08. |
| S1-11 | FAIL: body completion exceeds deadline and complete-chain resource bounds are absent, FR-04/06/07. |
| S1-12 | NOT_VERIFIED as complete acceptance: fresh 235/239 counts, Modulith, schema/export and adjacent tests pass; full required wire/negative coverage and canonical documentation remain FR-08/09. |
| S1-13 | FAIL: runbook/provenance/schema/current-view/evidence claims need FR-09 correction. Actual Cloud/browser/HTTPS operation remains legitimately NOT_VERIFIED/outside this gate. |

## Shared review coverage

| Dimensions | Disposition / rationale |
| --- | --- |
| 1 Owner need | PASS: connection-only S1 remains selected; no files/UI/RAG/scheduler/Media replacement added. |
| 2 traceability | FINDING FR-08/09: claimed acceptance/control/receipt mapping exceeds supplied proof. |
| 3 architecture and canonical sources | FINDING FR-09; runtime module verification passes. |
| 4 module ownership | PASS: new Drive leaf, no business-module imports/edges; Portability change only security exclusion. |
| 5 package structure | FINDING LOW FR-L01; complete 32 production/9 test Java subtree inventoried, eight missing descriptors. |
| 6 shared APIs/exceptions | PASS for root envelope/module-local transport ownership; callback correctness is FR-02. No common module or root business layer introduced. |
| 7 SOLID; 8 DRY/KISS; 9 patterns | Owner-local application/OAuth/crypto/web split is proportionate; no speculative framework. FINDING FR-01 for duplicated incomplete final-write gates; no broad structural rewrite required. |
| 10 business transitions; 11 API; 12 input validation | FINDING FR-01/02/03/05/06. |
| 13 authentication/privacy; 14 credential handling | FINDING FR-05/06/07; primitive AEAD and export exclusion tests pass. |
| 15 concurrency/transactions | FINDING FR-01/03: direct SQL CAS alone does not establish final callback authority. No SQL transaction spans provider I/O in inspected paths. |
| 16 recovery/operations; 18 bounds/complexity | FINDING FR-03/04/06: unfinished processing/cutoff lifecycle and unbounded response completion. DB reads themselves are bounded to two slots; no speculative performance issue. |
| 17 physical database | PASS for V25/JPA/constraints and unchanged v1; canonical logical schema is FR-09. PostgreSQL used, no H2. |
| 19 retrieval/recommendations; 20 parsing/OCR | N/A: no content/RAG capability changed or activated. |
| 21 security tests; 22 test quality | FINDING FR-08; 141 fresh JUnit passes plus 13 diagnostic counterexamples recorded with limits above. |
| 23 performance benchmark; 24 evaluation science | N/A: no approved model/evaluation/benchmark capability. Actual deadline counterexample belongs to bounds, not a new benchmark project. |
| 25 dependencies | PASS for no unrelated POM/runtime upgrade and existing managed Nimbus/JDK use; correct protocol use remains FR-04/05. |
| 26 build/tooling/static | Four focused groups PASS; FINDING FR-08 report capture. Byte Buddy dynamic-agent/CDS warnings are test-runtime configuration warnings, not suppressed. IDE/static analyzer NOT_VERIFIED; no warning-free claim. |
| 27 logging | FINDING FR-07 unsafe string carriers; no live-account leakage asserted. |
| 28 docs/governance | FINDING FR-09; status synchronized at this review boundary. |
| 29 hygiene/delivery | Submitted edits stay within approved production map; FR-L01 LOW. Reviewer diagnostics ignored, source hashes preserved, no agent staging/publishing. |

## Docs Sync Check and correction boundary

- **UPDATED:** ACTIVE and executable handoff to CHANGES_REQUESTED with this independent disposition, exact next route and 0/3 counter. Corrected the handoff's XML capture instructions using the already executed snapshot approach; no production/tooling repair made.
- **FINDING:** FR-09 current schema/ADR/architecture/operator/diagram/handback consistency; Antigravity may correct these already-allowlisted implementation-adjacent targets with the behavior remediation. FR-08 requires truthful self-review/evidence refresh.
- **ALREADY_CONSISTENT:** Owner D00-D07, preparation publication at 304708e6, default Drive OFF, exact conditional security permit, zero business-module edges, physical V25 and Portability export exclusion, no S2/S3 or RAG/Feed activation.
- **IMMUTABLE / DEFERRED_OUT_OF_SCOPE:** preparation/Owner decisions/earlier independent reviews and receipts, SAD snapshots, root/scoped legacy routing, GDI26-PH-L01 and unrelated backlog. No repository-wide cleanup or new Owner selection.
- **NOT_VERIFIED:** real Google Cloud/consent/browser/remote deployment, IDE/static analysis, and the mandatory missing runtime/control cases identified above. Deferred live operation does not excuse missing deterministic S1 acceptance.

Return a bounded correction for FR-01..09 under the existing handoff, add the missing focused regressions/control, run mandatory self-review and submit to independent Codex re-review. Do not expand to files/UI/new schema, distribute the coordinator, activate providers or rerun a full backend suite without a concrete need. Do not mark implementer-addressed findings independently CLOSED.

No commit message is issued while blocking findings remain.

**Next step:** Antigravity | `/antigravity-implement-handoff`, mandatory `/antigravity-self-review`, then Codex `$codex-final-review` | GDI26-S1 ACTIVE/executable handoff and FR-01..09 in this review | CHANGES_REQUESTED; focused behavior/evidence/docs corrections before acceptance; default OFF | remediation 0/3 consumed; next submitted correction 1/3; takeover NOT_AUTHORIZED.
