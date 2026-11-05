# GDI26 independent pre-handoff review

- **Date / reviewer:** 2026-11-05 / Codex.
- **Review mode:** maintenance preparation; review/status artifacts only. This is neither a numbered-phase gate nor an executable handoff.
- **Final status:** **CHANGES_REQUESTED / OWNER_DECISION_REQUIRED**. Two MEDIUM preparation blockers remain; no production defect or runtime acceptance is asserted.
- **Baseline:** local HEAD, local `origin/main`, and read-only `git ls-remote origin refs/heads/main` all resolve to `f4144e89ebc27b59225fd21f59dcb3dc6f1ebbf4`.
- **Entry working tree:** only the seven untracked Markdown preparation files in this maintenance directory. No production, test, build, migration, hook or frozen architecture edits.
- **Owner authority:** the explicit `$codex-pre-handoff-review` invocation authorizes this review. The [scope register](../owner-scope-and-decisions.md) records preparation approval and the two-Google-account goal; it explicitly leaves D00-D04 unresolved. Neither the invocation nor this report selects those implementation choices.
- **Execution / counters:** `ACTIVE=NO_ACTIVE_HANDOFF`; implementation NOT_STARTED; Antigravity remediation **0/3**, unchanged by preparation review; takeover NOT_AUTHORIZED. S2B and all earlier counters remain historical and separate.

## Required capabilities and simplest adequate mechanism

| Capability | Approval / simplest adequate mechanism | Reuse / verification effort / prerequisite |
| --- | --- | --- |
| Two separate Google connections for one PPV owner | KEEP the recorded goal; two independently verified Google subjects, with a maximum of two connection slots | Existing PPV JWT gate; medium implementation and medium-high security/persistence verification. Exact lifecycle belongs in the selected S1 contract. |
| Connect, status, reconnect, disconnect | SIMPLIFY to a connection-only first slice if Owner selects it | Existing HTTP/test conventions; medium effort. D00-D03 must establish that this is the desired first deliverable. |
| Selected-file metadata | KEEP CONDITIONAL; `drive.file` for app-authorized files, bounded listing through the selected connection | Existing transport conventions, not the Account catalog. Medium effort; D01, D02 and usable selection sequencing required. |
| Browse all existing files | OWNER DECISION; broader scopes must match metadata-only versus content access | Higher consent/verification and test effort. Do not describe `drive.file` as full-Drive access. |
| Store PPV media on Drive | OWNER DECISION / DEFER pending D00 | Media's internal binary port is relevant only to an approved storage use case. Current S3 remains the baseline; a separate provider change and any music-storage contract require their own approved scope. |
| Browser selection, preview, upload | DEFER unless expressly selected; separate browser/streaming acceptance | Picker needs browser integration. Preview/upload add content, destination, resource and ambiguous-completion risks; medium-high effort. No present frontend exception. |

Google's [scope guidance](https://developers.google.com/workspace/drive/api/guides/api-specific-auth) supports the preparation's distinction between per-file permission and broader restricted scopes. The [Picker guide](https://developers.google.com/workspace/drive/picker/guides/web-picker) confirms a browser JavaScript integration; a backend metadata endpoint does not deliver that interface. These public documentation checks do not verify a Google Cloud project or either personal account.

## Blocking findings

### GDI26-PH-01 — the executable product scope is not selected

- **Severity / lifecycle / gate:** MEDIUM / OWNER_DECISION_REQUIRED / blocks READY_FOR_HANDOFF.
- **Evidence:** [owner-scope-and-decisions.md:18](../owner-scope-and-decisions.md#material-owner-choices--do-not-silently-decide-for-the-owner), particularly D00-D03 and the explicit unresolved status at line 33; [decision-and-risk-ledger.md](../decision-and-risk-ledger.md), material-decision table. The source proposal conditions S1-S3 on these choices.
- **Impact:** Explorer versus storage, selected-file versus account-wide permission, initial operations, and browser sequencing materially change consent, boundaries, APIs and acceptance. A handoff written now would invent Owner approval. This is an acknowledged preparation prerequisite, not evidence of a current security failure.
- **Smallest correction:** Owner selects the product role, permission/operation set and first deliverable together. ChatGPT records that decision and makes the existing seven documents describe one consistent first slice, with later capabilities explicitly deferred. D05 should be recorded as the selected non-goals rather than inferred approval for destructive operations.
- **Proof for re-review:** a dated Owner decision or explicit Owner instruction traceable from this scope, one capability-to-scope/API/acceptance mapping, and consistent first-slice/UI boundaries. Do not demand a new phase, full implementation plan for every alternative, or runtime tests for this correction.

### GDI26-PH-02 — S1's credential, callback and ownership contracts remain alternatives

- **Severity / lifecycle / gate:** MEDIUM / OPEN; D04 OWNER_DECISION_REQUIRED / blocks READY_FOR_HANDOFF.
- **Evidence:** [oauth-security-and-data-governance.md](../oauth-security-and-data-governance.md), flow step 2 (line 10) and reviewer gates (line 28); [source-reuse-and-design.md](../source-reuse-and-design.md), proposed ownership and S1; [drive-api-ui-and-storage-plan.md](../drive-api-ui-and-storage-plan.md), proposed `drive_connection` (line 27). The documents themselves require these decisions before S1 handoff.
- **Source confirmation:** `authentication/internal/infrastructure/security/SecurityConfiguration.java:107` disables CSRF globally for the existing bearer API, uses `STATELESS`, and authenticates unmatched routes. A browser callback needs an explicit, narrowly reviewed binding/exposure design. `backend/pom.xml` declares the OAuth2 resource-server starter, not OAuth2 client wiring. The frozen module matrix has no `drive` module; current Account and Media contracts do not own these delegated credentials.
- **Impact:** an implementer would have to choose the credential store/key source, security-sensitive callback bridge, and frozen ownership/schema changes. Generic instructions to encrypt tokens or bind a session are insufficient to approve the specific change. No code has yet violated these contracts.
- **Smallest correction:** after PH-01, ChatGPT prepares one bounded proposal for (a) OAuth client reuse and exact callback/browser binding while preserving existing JWT behavior; (b) credential persistence and external key custody, integrity, key loss/rotation/recovery and reconnect/disconnect semantics; and (c) the owning module/package and minimal amendment footprint. Identify the affected ADR, DBML/Flyway contract, module matrix, package tree and security/API tests where applicable. Obtain Owner approval for the concrete frozen-boundary/schema/key proposal; do not ask the Owner to design cryptography or implement it during preparation.
- **Proof for re-review:** a concrete reviewed proposal and approval pointers, plus acceptance covering replay/mismatched browser, two-slot uniqueness, secret leakage, invalid/missing key, token-refresh/reconnect versus disconnect races, and persistence failure. Actual implementation tests, live consent, target deployment and Google verification remain explicitly prospective. Do not require production credentials or a live Drive call to finish the document correction.

There are no additional confirmed CRITICAL/HIGH findings in this preparation review. Existing unrelated LOW/INFO backlog remains under its original registers.

## Proportionality and non-goals

| Item | Disposition | Implementation / verification effort and approval |
| --- | --- | --- |
| PPV login, response envelope and error conventions | REUSE | Low incremental effort; retain ADR-0016's root transport types and module-local DTO/advice. Do not add another PPV login or `common` module. |
| Account catalog / Media S3 | KEEP boundaries | Reuse only compatible public contracts. No credential-in-notes workaround, internal cross-module imports or silent S3 replacement. |
| Durable Google credential protection | KEEP, concrete design REQUIRED | Medium implementation / high-value focused negative and race tests; PH-02 and D04 approval. Hash-only PPV refresh-token storage cannot supply a replayable Google refresh credential. |
| OAuth / Drive dependencies | REUSE framework capabilities where sufficient | Select a stable compatible client using the current Boot-managed baseline; do not add parallel clients or upgrade unrelated dependencies. No new dependency is approved here. |
| Page, response, timeout, retry and concurrency bounds | KEEP | Small-medium focused transport tests; select finite values in the corrected slice plan. No full-Drive or 5-TB scan benchmark. |
| Picker and browser token delivery | DEFER / D03 | Browser access-token handling must be explicit when UI is approved; Picker's token requirement is not permission to expose refresh tokens. No new UI/token-delivery endpoint now. |
| Preview and uploads/resumable recovery | DEFER / D02 | Separate bounded content tests if selected. No requirement to build streaming infrastructure for connection-only S1. |
| Delete, move, sharing, cross-account copy, scheduled sync | DEFER / D05 | No first-slice need established; no background job engine or destructive operation acceptance added. |
| Generic provider framework, queue or distributed locks | REMOVE from proposed scope / REJECT | No demonstrated need in this single-owner integration. Necessary local slot/token races still require focused proof. |
| New custom agent, rule, hook or skill | REUSE existing harness | No capability gap demonstrated. `backend-implementer`, existing Java/security/persistence/testing skills, self-review, watchdog and independent review suffice. Python/RAG engineering is N/A. |
| Full backend suite, performance dashboard, ML evaluation | SIMPLIFY / DEFER | Focus on selected changed behavior, relevant architecture/security/API/schema gates; reuse applicable evidence. No arbitrary test-count target, benchmark project, OCR/GPU/judge or model runtime. |

Explicit current non-goals: production implementation, migrations or architecture edits, Cloud provisioning, personal-account consent/API calls, frontend implementation, Feed activation, RAG ingestion/indexing/Q&A, media-provider replacement, unattended crawling and agent Git publication.

## Reviewed paths and existing harness

- Read all seven preparation Markdown files. Checked all **19 local link targets** after path normalization: **0 missing**. Checked public Google scope, web-server OAuth, token-expiration, Picker, quota and upload documentation; no authenticated Google API requests.
- Read `ACTIVE.md`, current Owner delivery policy, Docs Sync contract, S2B publication closeout, root/backend/docs `AGENTS.md`, and Authentication/Account/Media scoped instructions. `ACTIVE` is the current execution register; no maintenance-to-RAG or Phase 16 authorization is inferred.
- Used existing Graphify navigation (graph dated 2026-10-18, 8,894 nodes). The 1,500-token query was truncated, so its results were navigation only; verified cited relationships in current source. Did not rebuild the graph or infer coverage of newer features.
- Recursively inventoried Authentication, Account and Media production/test subtrees: **191 production Java files, 19 test Java files**, plus three module instruction files. Every production package containing Java has `package-info.java`; no `.gitkeep` in these subtrees. This is a structural inventory, not a review of every unchanged class.
- Inspected current SecurityConfiguration, the three module descriptors, ExternalAccount/ExternalAccountType, MediaStoragePort/MediaStorageConfiguration, root ApiResponse, and `backend/pom.xml`. Source confirms Java 25, Boot 4.1.1, Modulith 2.1.1, Account profile ownership and S3/disabled Media adapters.
- Compared the module dependency matrix, relevant DBML ownership, package-tree guidance and ADR-0008/0009/0013/0016. No approved Drive table/module or frozen amendment is established by the preparation.
- Inspected `ApplicationArchitectureTests`, `authentication/SecurityFilterChainIntegrationTest`, and `web/OpenApiRouteInventoryIntegrationTest` as reusable gate locations. The latter contains the **235-operation** baseline; any future approved routes require an explicit inventory delta, not preservation of an obsolete count.
- Read relevant review/documentation/security/architecture/testing/watchdog skills, backend implementer instructions and self-review contract; inspected `.agents/rules/agent-handoff-workflow.md`, `.agents/hooks.json`, `.codex/config.toml` and `.codex/rules/owner-git.rules`. Located `scripts/run-bounded-command.py` and repository-safety hook. No tooling change or new agent is justified; no subagents were used.

## Coverage ledger

`PASS` below means adequate preparation coverage or a checked source fact, **not** a runtime pass. `FINDING` is a preparation gate; `NOT_VERIFIED` is not acceptance. No backend, security, migration, provider, browser, static-analysis or IDE test was executed.

| Dimensions | Status / evidence and limit |
| --- | --- |
| 1 intent; 2 traceability | FINDING — PH-01; GDI26-01..16 are candidate cases until a scope is selected. |
| 3 architecture; 4 ownership | FINDING — PH-02; current descriptors/matrix checked, proposed ownership/amendment not approved. |
| 5 folder/package structure | PASS — complete relevant subtree inventory above; no new code/package exists. |
| 6 shared APIs/exceptions | PASS — root ApiResponse and ADR-0016 reuse; module-local translation planned. Final error/API contract awaits selected scope. |
| 7 SOLID | N/A to unchanged implementation; proposed credential/transport responsibilities considered under PH-02. |
| 8 DRY/KISS; 9 patterns | PASS — preserve existing framework/port boundaries; reject generic framework and credential-catalog conflation. |
| 10 domain lifecycle; 11 public API | FINDING — PH-01/02; reconnect/replacement, callback, permission and route behavior not selected. |
| 12 input validation | PASS for prospective gates — state, redirect, page, file and response bounds specified; exact selected-slice limits still required. |
| 13 auth/privacy; 14 credential custody | FINDING — PH-02; secure intentions are present, concrete binding/key/store proposal unresolved. |
| 15 atomicity/concurrency | PASS for prospective gates — GDI26-03/04/13 cover uniqueness and disconnect races; future tests must also prevent late refresh/callback responses from restoring disconnected credentials. No runtime proof. |
| 16 operations/recovery; 17 database | FINDING — PH-02; recovery/key/migration ownership not fixed. Live project/deployment behavior remains NOT_VERIFIED. |
| 18 bounded complexity/resources | PASS for planned bounds; actual timing/memory NOT_VERIFIED and no terabyte-scale performance claim accepted. |
| 19 retrieval/recommendations; 20 parser/OCR fidelity | N/A — no RAG, extraction, content ingestion or recommendation scope. |
| 21 security negatives; 22 test quality | PASS for prospective contract — fake signed identities, HTTP auth, PostgreSQL race fixtures and provider fault controls; all GDI cases NOT_RUN. |
| 23 performance benchmarks; 24 evaluation science | N/A — no approved performance target or model/evaluation capability. Bounded transport behavior remains required. |
| 25 dependency lifecycle | NOT_VERIFIED — no OAuth client dependency selected. Compatibility/pinning belongs in the concrete proposal and later implementation checks; no incidental upgrades. |
| 26 build/tooling/static diagnostics | PASS for reuse plan; runtime/static/IDE results NOT_VERIFIED. Watchdog normal cap 300s, preapproved full-suite-only cap up to 2,100s; no extended run authorized here. |
| 27 safe logging | PASS for planned exclusions and negative tests; provider body/token/callback/upload-URI leakage checks remain NOT_RUN. |
| 28 docs/governance | FINDING for PH-01/02 readiness; PASS for local links and baseline. Scoped Docs Sync dispositions below. |
| 29 hygiene/delivery | PASS — preparation/review/status files only; no production edit or publication. No credential, generated fixture or new `.gitkeep` needed. |

## Conditional slice and verification route

This is a review of the existing proposed sequence, not an implementation handoff or approval to create one.

1. **S1, only if selected:** connection authority. Keep GDI26-01..08, S1-applicable token-transport faults from 12, refresh/disconnect races from 13, and relevant 15/16. Reuse MockMvc, controlled OAuth/OIDC transport and Testcontainers PostgreSQL. Include the selected callback, key failure, reconnect and persistence rollback negatives. No file contents, listing, preview or upload acceptance in a connection-only contract.
2. **S2, separately scoped:** permission-correct metadata/selection. Add applicable 09..13 and focused compatibility checks; backend-only metadata does not prove Picker/browser operation. Bind credentials/caches/cursors to a connection; test unauthorized A-only resources under B. Shared files legitimately authorized to both subjects must not be rejected merely because their Google file ID is identical.
3. **S3, optional/deferred:** only expressly selected UI/content operations, their bounds and acceptance. No mandatory preview/upload suite for S1/S2.

Each implemented slice requires Antigravity self-review, independent Codex slice review and Owner publication; up to three remediation/re-review rounds on that future active slice. A final feature aggregation should reuse still-applicable slice evidence and run only justified changed-source checks. Neither this preparation nor an initial implementation consumes a remediation round.

Distinguish deterministic fake-provider acceptance from any later Owner-authorized real OAuth/target-deployment verification. No live account access or production enablement follows from this review. Project consent status and the [documented Testing-mode refresh-token lifetime](https://developers.google.com/identity/protocols/oauth2#expiration) require factual operator documentation. The selected design must describe revocation accurately: Google's [web-server OAuth guide](https://developers.google.com/identity/protocols/oauth2/web-server#tokenrevoke) explains that revocation affects the associated user's project grants across clients; local disconnect must fail closed even when remote revocation fails. This does not imply revoking a different Google subject's grant.

## Docs Sync Check and correction boundary

- **UPDATED:** this review and the canonical scope README status only. Routine correction of the seven preparation documents remains ChatGPT's responsibility after Owner decisions.
- **ALREADY_CONSISTENT:** `ACTIVE=NO_ACTIVE_HANDOFF`, actual published baseline, S2B default-OFF and its separate operational gate, single-owner PPV login, distinct Account/Media contracts, deferred frontend and suspended S19-2. No accepted historical review or counter changed.
- **BLOCKED_OWNER_DECISION:** PH-01 / D00-D03, and approval of the concrete PH-02 / D04 frozen-boundary and credential proposal. The clarification docket was presented during this review; no selection is inferred from an unanswered question.
- **NOT_VERIFIED:** every GDI26 runtime case, Google Cloud client/project/consent status, real account capacity, target callback/deployment/key custody, dependency selection and production operation. These are not silently converted to PASS.
- **DEFERRED_OUT_OF_SCOPE:** old CSA26 current-status headings in root `AGENTS.md` and Owner policy; backend/docs historical phase notices; legacy Phase 15 routing in `.agents/rules/agent-handoff-workflow.md`; stale B1/B2 pending wording in `.agents/agents/backend-implementer/agent.md`; package-tree historical wording and unrelated LOW/INFO backlog. Current execution authority comes from policy and `ACTIVE`; ChatGPT may repair these notices only under separately approved bounded documentation scope. They do not authorize Drive implementation or reopen older handoffs.

Re-review only the Owner decision and bounded corrected preparation, retaining this baseline evidence where source-applicable. No production/tooling repair, canonical architecture amendment or live handoff is authorized by the findings themselves. No commit message is issued because preparation is not ready.

**Next step:** Owner / ChatGPT | Owner resolves D00-D03; ChatGPT prepares the concrete D04/callback/ownership proposal and bounded docs correction, obtains required Owner decisions, then Codex `$codex-pre-handoff-review` | GDI26 scope README, decision register and this review | PH-01/02 resolved before READY_FOR_HANDOFF; keep ACTIVE=NO_ACTIVE_HANDOFF | implementation NOT_STARTED; remediation 0/3 unchanged; takeover NOT_AUTHORIZED.
