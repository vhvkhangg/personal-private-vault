# Independent artifact review — Spring-only Archify SAD-01–SAD-04

- **Date / reviewer:** 2026-11-01 / Codex, separate independent review session; not the diagram author.
- **Review mode:** `SLICE`, explicitly authorized documentation maintenance with `NO_ACTIVE_HANDOFF`.
- **Reviewed HEAD:** `main` at `29ffc45c4a7079dfb4e155ee55590ff8fca6dfb0`; local `origin/main` matches. The accepted application/CSA26 baseline remains `4cf62041796d0b510255ee7014261221cafe3d5d`.
- **Authority:** [Owner scope](../owner-scope-and-authorization.md), [diagram plan and A1–A8](../diagram-plan.md), [one-off authoring task](../diagram-authoring-task.md), [published preparation review](2026-11-01-independent-preparation-review.md), and the Owner's explicit artifact-review request.
- **Submission:** the 13 files under [the Spring index](../../../../architecture/diagrams/interactive/spring/README.md), plus [diagram-handback.md](../diagram-handback.md). This review adds one documentation file. No implementation was edited.
- **Status: `READY_FOR_OWNER_COMMIT` — A1–A8 PASS.** No CRITICAL, HIGH or MEDIUM finding or mandatory unverified case remains. One LOW evidence-description discrepancy is OPEN / NONBLOCKING under the [Owner severity policy](../../../../workflow/current-owner-delivery-policy.md).

## User requirement and simplest-mechanism check

The delivery supplies exactly four additive views with typed editable JSON, portable Archify HTML, actual native SVG exports and a relative source index. It introduces no application feature, tool installation, project dependency, replacement canonical diagram or active software handoff. The representative flows are sufficient for the approved documentation purpose; no application test suite, database or RAG runtime is needed to establish their source-derived claims.

The index and cards distinguish explicit imports from runtime calls, framework inference from measured execution, and optional storage capability from deployment. Local-only source passports and companion clickable index links match the reviewed preparation's documented mechanism.

## Finding register

| ID | Severity | Blocking? | Evidence and consequence | Minimal correction / disposition | Status |
| --- | --- | --- | --- | --- | --- |
| `SAD-FR-L01` | LOW | No | [Handback line 45](../diagram-handback.md#L45) calls the cancelled codeload scratch file zero bytes. The retained `logs/spring-archify-20261101-8f3fe0c1/upstream-a4065941.zip` is **11,264,000 bytes**, SHA-256 `24961ecde3c79459a8decb3c70f12c95721be5f9c4b36f07688c03c4118a238c`. The stated scratch-file size is inaccurate; the separately acquired, clean, pinned Git tool tree and its delivered output bindings verify correctly. | Queue a bounded documentation clarification describing the retained partial archive accurately. Do not infer successful archive acquisition/use, alter the tool fingerprints, delete evidence or reopen authoring. | OPEN / NONBLOCKING |

No artifact correctness or safety defect was found. The existing module-map density advisory is a documented limitation, not a newly imposed layout requirement. Existing `CSA26-FR-L01`, `AUTH-CLOCK` and unrelated LOW backlog remain unchanged.

## Acceptance matrix

| Gate | Result | Independent evidence and limits |
| --- | --- | --- |
| **A1 — scope** | **PASS** | Fresh traversal of non-RAG Java packages and explicit imports yields **19 owners / 38 directed pairs**, exactly matching SAD-01. Authentication, journal and portability remain present without invented import edges. All 13 diagram/index files pass excluded-content and local-path checks. Background status references in the maintenance handback are not diagram content. |
| **A2 — fidelity** | **PASS** | Checked all **99 native source ranges**, **43 selected NamedInterface links**, all 38 exact import citations and the request/authentication/persistence chains below. All **173 relative links** in index plus handback resolve. Committed Java/configuration sources are unchanged at the stated revision. Framework filter dispatch, JPA I/O and default REQUIRED propagation are expressly inferred; no live application trace is claimed. |
| **A3 — interactivity** | **PASS** | Fresh Edge browser input exercised hover, relationship facts, focus, every one of the **44 source passports**, exact finder selection, light/dark, Live/Still and native SVG export across all four HTML files. `search → account → reference` resolves. A separate exact negative test establishes `reference` as source, zero candidate targets, then `search` as target: state **error**, no active route, and the explicit cannot-reach message. A clicked README-relative ImageController link opens the expected Java source offline. Local passports intentionally display paths/ranges; clickable navigation is provided by the companion index. |
| **A4 — typed ownership / determinism** | **PASS** | Four retained typed sources match the native finalizer receipts and submitted HTML hashes. Verified clean Archify **3.2.0**, upstream commit and 206-file fingerprint below. A fresh SAD-02 `finalize sequence` passes validate/deliver/strict provenance/browser stages and produces **byte-identical HTML**. No production file, authored source or submitted HTML was rewritten. |
| **A5 — static fallback** | **PASS** | Opened all four real SVGs in light/dark at 1800×1100; inspected labels, directions, transaction boundary and long authentication sections. XML, all **44 node identities / 106 relationship identities**, text bounds, absent scripts/event handlers and source/export alignment check correctly. All 42 authentication message identities survive, including repeated endpoint/label triples. Fresh actual Export → SVG Blobs match delivered architecture SVGs byte-for-byte; the two sequence exports differ only by an empty legend `style=""` attribute, with identical diagram content. |
| **A6 — privacy / safety** | **PASS** | No remote executable/resource/CSS dependency or detected secret/local absolute path in the delivery. Each HTML's two executable scripts match the vetted pinned template exactly; the other script blocks are typed JSON data/notices. No fetch/XHR/WebSocket/EventSource/beacon/eval/Function-constructor call was found. Embedded fonts/notices remain present. Update checks were explicitly disabled; the selected updater returns before spawning its checker. Fresh HTML and SVG sessions, with CDP offline and HTTP(S)/WS(S) blocked before navigation, observed **zero remote request attempts and zero JavaScript exceptions**. No project data was submitted remotely by this review. This is source/artifact/session evidence, not an OS packet capture or universal vulnerability claim. |
| **A7 — preservation** | **PASS** | Independently hashed all **2,807 tracked files** against the author-entry manifest and this review's entry snapshot; all unchanged. Tracked diff and index are empty. Existing canonical source/exports, application/schema/build/runtime/QA/hook files and [ACTIVE](../../../handoffs/ACTIVE.md) are preserved. All 14 incoming submission files retain their reviewed hashes. The pre-existing root ZIP is outside this delivery. |
| **A8 — independent disposition** | **PASS** | This separate session inspected sources, output, native receipts and fresh browser/render evidence, then wrote this dated report without modifying author artifacts or publishing. Author-time pending acceptance is resolved by this review; Owner publication remains pending. |

No required gate is FAIL, NOT_RUN or NOT_VERIFIED.

## Code and ADR provenance

| View | Independently checked source facts |
| --- | --- |
| SAD-01 | Recomputed explicit cross-owner imports directly from the 19 non-RAG Java module subtrees; all 38 pairs and their index citations agree. Selected interface descriptors contain `@NamedInterface`. The [frozen dependency matrix](../../../../architecture/module-dependency-matrix.md) defines allowed dependencies; the drawing correctly describes only observed imports and does not equate arrows with deployed services or runtime invocation. |
| SAD-02 | [ImageController](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/media/internal/web/controller/ImageController.java#L187) maps the protected GET through ImageOperations; [ImageService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/media/internal/application/ImageService.java#L142) owns the read-only repository call and missing-image exception. [SecurityConfiguration](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/infrastructure/security/SecurityConfiguration.java#L58) supplies HS256 issuer/time validation and the 401 envelope; [MediaExceptionAdvice](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/media/internal/web/advice/MediaExceptionAdvice.java#L39) supplies the 404. Shared root ApiResponse plus module-local DTO mapping agrees with [ADR-0016](../../../../adr/0016-root-http-contract-module-local-adapters.md). |
| SAD-03 | [AuthController](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/web/controller/AuthController.java#L43), security matchers, BootstrapService/UserBootstrapper, [SessionService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/application/session/SessionService.java#L37), refresh repository locks, TokenGenerator/JwtTokenService, [PrivatePinService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/authentication/internal/application/privatepin/PrivatePinService.java#L53) and AuthExceptionAdvice support every depicted scenario and status. Refresh rotation stores hashes, locks and replaces the predecessor transactionally; supplied-token revocation does not blacklist JWTs. PIN verification returns a boolean; change locks/refreshes the singleton and checks the current PIN. No cookie/storage/unlock-session policy is invented. Abbreviated SVG route labels are explicitly qualified by the index and HTML card. |
| SAD-04 | [ImageService.create](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/media/internal/application/ImageService.java#L33) invokes the public Vault facade before owner-local persistence. VaultEntryService's transactional annotation supports the explicitly inferred joined REQUIRED boundary. [ImageUploadService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/media/internal/application/storage/ImageUploadService.java#L45) rejects ambient transactions, spools/hashes, writes storage before metadata, compensates confirmed failure with at most three attempts and retains uncertain outcomes. MediaStorageConfiguration/Properties and S3MediaStorageAdapter support optional, disabled-by-default S3 capability. [PortabilitySnapshotAdapter](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/portability/internal/infrastructure/snapshot/PortabilitySnapshotAdapter.java#L184) explicitly owns read-only JDBC REPEATABLE READ export, ZIP writing and commit/rollback, consistent with [ADR-0017](../../../../adr/0017-portability-readonly-snapshot-module.md). Flyway/Hibernate configuration supports SQL migration versus validation ownership. |

## Tool identity and verification

Archify upstream **`a4065941f12be7967436261d9934784e381d423c`**, direct local Node entrypoint; Node **v24.14.0**, installed Edge **156.0.4314.58**. No npm/npx installer, runtime download, global skill installation or sandbox opt-out was used in this review. The existing ignored checkout is clean.

| Fingerprint | Independently matched SHA-256 |
| --- | --- |
| 206-file `archify/` tree, handback's canonical path/NUL/hash/LF algorithm | `f4cc7d62491d16dffec8775ab9b6f889af5217e99a75ae7350e84132b3b42f81` |
| `archify/package.json` | `ea99289562037cae2deeced0b45e1374ce4c4b4f23a53a607a6aada62c803cd0` |
| `archify/package-lock.json` | `ab7a1438a8edbd26ceda9cf9648e3ad50542bffd7cc87f9ec83793cde1f26287` |
| Root and skill Apache-2.0 LICENSE, each | `cfc7749b96f63bd31c3c42b5c471bf756814053e847c10f3eb003417bc523d30` |
| Incoming `diagram-handback.md` | `99ed83aa18ad5c16938302c5930aa4134fb630654bd83650d78dec1888a87219` |

All evidence below is new ignored scratch under `logs/spring-archify-independent-review-20261101-53d8c0e4/`. Earlier author receipts and unsuccessful attempts were read, not overwritten. The [input hash manifest and artifact audit](../../../../../logs/spring-archify-independent-review-20261101-53d8c0e4/final-check-before-review.json) binds the 14 submitted files and governing preparation. Per-file delivered hashes in the handback independently match. The subsequent saved-review result is `final-check-after-review.json` in the same evidence directory.

All potentially long verification commands used `python scripts/run-bounded-command.py --profile normal --timeout-seconds <cap> --receipt <fresh-path> -- <command>` from repository root. Exact command bodies are:

| Command after `--` | Cap / exit | Evidence |
| --- | --- | --- |
| `python logs/spring-archify-independent-review-20261101-53d8c0e4/audit.py` | 120s / **0**; 45 checks | [Receipt](../../../../../logs/spring-archify-independent-review-20261101-53d8c0e4/audit-run.json), [observations](../../../../../logs/spring-archify-independent-review-20261101-53d8c0e4/audit.json) |
| `node logs/spring-archify-20261101-8f3fe0c1/tool-git/archify/bin/archify.mjs finalize sequence docs/architecture/diagrams/interactive/spring/sources/request-lifecycle.sequence.json logs/spring-archify-independent-review-20261101-53d8c0e4/rerender/request-lifecycle.html --repo-root . --quality showcase --out-dir logs/spring-archify-independent-review-20261101-53d8c0e4/rerender-review --json` | 180s / **0** | [Watchdog](../../../../../logs/spring-archify-independent-review-20261101-53d8c0e4/rerender-run.json), [native stages](../../../../../logs/spring-archify-independent-review-20261101-53d8c0e4/rerender-review/request-lifecycle.finalize.json) |
| `node logs/spring-archify-independent-review-20261101-53d8c0e4/browser-review.mjs` | 240s / **0** | [Receipt](../../../../../logs/spring-archify-independent-review-20261101-53d8c0e4/browser-run.json), [browser/static observations](../../../../../logs/spring-archify-independent-review-20261101-53d8c0e4/browser-review.json) |
| `node logs/spring-archify-independent-review-20261101-53d8c0e4/reverse-route-v2.mjs` | 120s / **0** | [Receipt](../../../../../logs/spring-archify-independent-review-20261101-53d8c0e4/reverse-route-v2-run.json), [exact negative state](../../../../../logs/spring-archify-independent-review-20261101-53d8c0e4/reverse-route-v2.json), [screenshot](../../../../../logs/spring-archify-independent-review-20261101-53d8c0e4/reverse-route-explicit-v2.png) |
| `python logs/spring-archify-independent-review-20261101-53d8c0e4/final-check.py before-review` and subsequent `after-review` | 120s each / **0** | [Before receipt](../../../../../logs/spring-archify-independent-review-20261101-53d8c0e4/final-check-before-run.json), followed by `final-check-after-run.json` in the same directory; final checks include review links and preservation |

`ARCHIFY_UPDATE_CHECK_DISABLED=1` and the installed Edge executable were selected for native/browser execution; `ARCHIFY_CHROME_NO_SANDBOX` was absent. Read the pinned updater's early return, package scripts/lock/license information and browser-launch flags. Executable HTML scripts match pinned `assets/template.html`; no author-injected script is present.

Reused author finalizer receipts are hash-bound to all four unchanged source/HTML pairs, with all four native stages PASS and updater disabled. Relocated portable HTML intentionally does not carry scratch-path strict-provenance sidecars; fresh finalization reproduces strict provenance without fabricating relocated receipts.

**Evidence qualification:** the general browser harness's broad `/no|unreachable|cannot/` check can match “nodes” and is not accepted as reverse-route proof. The targeted follow-up starts from a fresh page and verifies exact source, candidate set, error state, target message and absent active route. Its first attempt exited **1** because the assertion expected “no directed route” while the actual correct message said “cannot be reached”; that failed receipt remains preserved. The corrected v2 checks the actual exact endpoint message and passes without any artifact change. This follow-up resolves the evidence gap.

Reviewer image inspection covered all four HTMLs in both themes at 1600×1000 and all four SVGs in both themes at 1800×1100, including the authentication middle/end sections. Native author multi-viewport measurements remain applicable to the same hashed HTML; they are reused rather than represented as new reviewer runs.

## Shared review coverage and limits

| Applicable dimensions | Result / rationale |
| --- | --- |
| 1–6: scope, traceability, architecture, ownership, recursive folder inspection, shared HTTP contract | **PASS:** complete submitted-tree inventory, 19-owner import reconstruction, source/ADR table and root envelope ownership above. No new Java package or shared abstraction. |
| 10–17: depicted business/API behavior, validation, privacy, credentials, transaction/recovery/database boundaries | **PASS for documentation fidelity and artifact safety:** source inspection and A2/A6. No live application, database or storage certification is inferred. |
| 18, 21–22, 25–29: bounded verification, negative checks, evidence quality, tool identity/licenses, diagnostics, safe receipts, docs and delivery hygiene | **PASS with stated limits:** new finite checks, native/browser evidence, input hashes and link checks. No warning-free, mobile/accessibility, exhaustive viewer-feature, benchmark, OS-wide egress or vulnerability-audit claim. |
| 7–9, 19–20, 23–24: implementation SOLID/pattern changes, retrieval/OCR, performance benchmarks and model evaluation | **N/A:** no production design change, RAG capability, parser or measured performance/quality deliverable. |

The dense module overview retains the reported 49 crossover / 14 bend-count / 4 stretch advisories. Labels remain available, and focus/relationships/route controls clarify individual dependencies; no second placement loop is required. SVGs contain the diagram and inline notes, while title/cards/clickable source navigation remain in HTML/index. These disclosed format limits do not remove a required view or relationship.

No Maven, pytest, application request, QA evaluation, worker, model inference, runtime download, database/migration or object-storage operation was executed. No all-backend audit acceptance follows.

## Scope-bounded Docs Sync Check and preservation

- **UPDATED:** this dated independent review only. Author index, JSON, HTML, SVG and handback bytes are unchanged; author-time pending-review statements remain the original submission record, superseded for A8 by this report. Owner publication is still pending.
- **ALREADY_CONSISTENT:** published preparation, actual execution HEAD, [ACTIVE](../../../handoffs/ACTIVE.md) `NO_ACTIVE_HANDOFF`, accepted canonical diagrams and current phase boundaries. Phase 17 frozen, Phase 18 closed, S19-1 accepted/published and S19-2 suspended/not accepted/archival-only remain intact; indexing inactive and live originals disabled/deferred.
- **DEFERRED_OUT_OF_SCOPE:** `SAD-FR-L01`, existing CSA26/Authentication/LOW backlog, older routing notices, future backend audit/features/frontend. No status/counter or frozen history was rewritten.
- **NOT_VERIFIED:** future Owner publication and authorship/purpose of the root `personal-private-vault.zip`. That ZIP was already untracked on entry, remains untouched and is explicitly excluded from the reviewed commit set. It is not a diagram gate.

The author delivery contains 14 new files; adding this report makes **15 reviewed documentation files**. Ignored review scripts, screenshots, rerenders, receipts and external tool bytes are evidence only, not commit artifacts. Final verification confirms input preservation, resolving review links, no review trailing whitespace and `git diff --check` success. No agent staged, committed, pushed, tagged or created a PR.

## Owner next action

Owner may commit/push the 13 files under `docs/architecture/diagrams/interactive/spring/`, `diagram-handback.md` and this review. Keep the root ZIP and ignored evidence outside that commit. LOW `SAD-FR-L01` is queued, not a new correction/re-review loop.

Suggested commit message: `docs(architecture): add reviewed Spring-only Archify views`

Next step: Owner | commit/push the 15 reviewed documentation files, then record a brief publication closeout against the actual commit | Spring-only Archify maintenance and this review | READY_FOR_OWNER_COMMIT; A1–A8 PASS; publication pending | no Spring remediation round consumed; CSA26 2/3 consumed and stopped; S19-2 counters unchanged.
