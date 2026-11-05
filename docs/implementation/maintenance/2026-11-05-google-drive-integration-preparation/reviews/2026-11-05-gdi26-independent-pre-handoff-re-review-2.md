# GDI26 independent pre-handoff re-review 2

- **Date / reviewer / mode:** 2026-11-05 / Codex / maintenance preparation for connection-only GDI26-S1.
- **Final status: READY_FOR_HANDOFF.** PH-01/PH-02/PH-03 **CLOSED for preparation**; no open CRITICAL/HIGH/MEDIUM preparation finding. GDI26-PH-L01 is LOW/OPEN and nonblocking.
- **Authority:** [Owner D00-D07 decision](../owner-decision-record-2026-11-05.md) and this explicit skill invocation. Approval covers the bounded S1 design and its eventual separate handoff; not S2/S3 execution, frontend, live consent or deployment.
- **Baseline:** HEAD, local `origin/main` and freshly queried remote `main` all resolve to `f4144e89ebc27b59225fd21f59dcb3dc6f1ebbf4`. Tracked source/test/build/tooling/architecture diff remains empty; the preparation and review files are untracked pending Owner publication.
- **Execution / counters:** ACTIVE remains **NO_ACTIVE_HANDOFF**, S1 NOT_STARTED, Antigravity remediation **0/3 unchanged**, takeover NOT_AUTHORIZED. Preparation re-reviews do not consume implementation remediation rounds.

## Findings disposition

| Finding | Independent disposition |
| --- | --- |
| GDI26-PH-01 | **CLOSED for preparation**, unchanged from re-review 1. D00-D07 select the product, narrow scopes, connection-only S1, later app-folder file operations/Explorer, retained Media S3 and localhost-first sequencing. |
| GDI26-PH-02 | **CLOSED for preparation**, unchanged from re-review 1. Exact browser callback/binding, independent credential custody, external AES-GCM key, two-slot table/module and focused proof obligations are concrete. |
| GDI26-PH-03 | **CLOSED for preparation** on the correction inspected below. Runtime concurrency and provider behavior are not yet tested or accepted. |

### PH-03 correction checked

The [OAuth contract, section 3.1](../oauth-security-and-data-governance.md#31-ph-03--same-subjectproject-revoke--reconnect-ordering-s1-mandatory), at submission lines 32-43, now provides:

1. A project/verified-subject fence reserved before local deletion, with short database work and final callback commit checks under the same bounded local guard. No guard or SQL lock spans Google HTTP; local delete failure prevents remote revoke.
2. Same-subject commitment denied in either slot while revoke is outstanding; different subjects remain independent. A rejected new token is not revoked as cleanup, avoiding another grant-wide effect.
3. An attempt-generation/cutoff rule rejecting consent initiated before/during the old revoke even if its callback finishes afterward, plus bounded credential validation before CONNECTED.
4. One revoke request only; REQUEST_ACCEPTED is distinct from full propagation. Definitive failure/ambiguous timeout keeps a fail-closed local quarantine, with no automatic old-token retry or background work.
5. Explicit controlled recovery, lost ephemeral state on restart, no claim that restart proves remote completion, and later `invalid_grant` downgrade without credential resurrection. Permanent crash-durable/provider-validity guarantees are not claimed or silently added.
6. Local disconnect reported separately from transient remote status in the existing routes. No extra table, endpoint, distributed coordinator or frontend required.

The [API plan](../drive-api-ui-and-storage-plan.md) carries the same ordering and response distinction. [GDI26-08/13 and the six-group gate](../acceptance-and-review-plan.md) now require a fake provider that models subject/project grant invalidation, barriers for reconnect in both slots, stale attempts, unrelated subject V, delayed HTTP 200, uncertain failure, restart and delayed `invalid_grant`. A bypassed-fence negative control must expose the old race. These address the specific counterexample in [re-review 1](2026-11-05-gdi26-independent-pre-handoff-re-review-1.md); a live account is unnecessary for the eventual deterministic regression.

The provider premise remains the previously checked [Google revocation contract](https://developers.google.com/identity/protocols/oauth2/web-server#tokenrevoke). Acceptance here is of the proposed mechanism and proof contract, not a claim that Google propagation is instantaneous or that code exists.

### GDI26-PH-L01 — historical navigation anchor drift

- **Severity / lifecycle / gate:** LOW / OPEN / **nonblocking**.
- **Evidence:** the immutable initial review links to `../owner-scope-and-decisions.md#material-owner-choices--do-not-silently-decide-for-the-owner`. The corrected scope no longer contains that former heading; the linked file still exists and contains the decisions.
- **Impact:** loss of direct heading navigation only; Owner approval, reviewed evidence and S1 contracts remain available. No runtime or mandatory acceptance impact.
- **Minimal later remedy / owner:** ChatGPT may add a compatible anchor or a historical navigation pointer in a bounded docs maintenance/audit scope. Do not rewrite the immutable review or repeat pre-handoff review for this LOW alone. Record in the future non-RAG audit backlog.

## Necessity, reuse and bounded delivery

| Owner-visible behavior / mechanism | Decision / simplest adequate approach / effort |
| --- | --- |
| Two connections; connect/status/reconnect/disconnect | KEEP S1; two fixed slots and verified subjects. Moderate implementation and focused security/PostgreSQL verification; approved D00-D04. |
| PPV access control, JSON envelope, HTTP/security libraries and tests | REUSE JWT and ADR-0016 root transport conventions. Four planned operations; only the exact GET callback is public and has its explicit minimal browser response. No parallel PPV login. |
| Credential custody and persistence | KEEP approved external key/AES-GCM and Drive-owned additive table/module. No secrets in Account, Settings or PPV refresh-token rows; no Media S3 change. |
| Pending OAuth attempts and PH-03 ordering | SIMPLIFY to bounded ephemeral state and local single-process coordination; enough for the demonstrated local/provider race. No durable job subsystem or cross-HTTP database transaction. |
| Folder/list/upload/download and Explorer | DEFER implementation to separately gated S2/S3. `drive.file` and app-root policy retained; no arbitrary existing-file discovery, content work, Picker or UI in S1. |
| Destructive operations, background sync, broad scopes, RAG, storage replacement | REMOVE from S1 / DEFER or REJECT under D05 and existing frozen boundaries. No new scope or approval docket. |
| Agents, hooks, models and benchmarks | REUSE backend-implementer, current skills, self-review, watchdog and independent review. No new agent/framework/runtime download or full-Drive benchmark. Python/RAG skills are N/A. |

The finite slice sequence remains S1 connection authority, S2 app-folder file operations, then separately authorized S3 web Explorer. Each implementation slice needs Antigravity self-review, independent Codex review and Owner publication; feature-final aggregation reuses applicable slice evidence. One approved future slice has at most three remediation/re-review rounds. No RAG milestone or numbered-phase predecessor gate is invented for this maintenance scope.

## Coverage and verification evidence

Read all eight current preparation/decision files and reused the two preserved independent reviews. Verified unchanged source baseline and inspected the complete PH-03 security/API/acceptance/risk correction. The initial review's source/package/harness inspection remains applicable: 191 production and 19 test Java files inventoried across Authentication/Account/Media, package descriptors checked, existing root ApiResponse/JWT/S3 ownership verified. No source drift requires a repeated broad scan, Graphify rebuild, backend suite or hook test.

The pre-status-edit documentation check covered **43 local links**, **zero missing files**, and the single historical anchor mismatch above. Every new PH-03 section anchor resolved. Original review, re-review 1 and Owner decision hashes were captured before status edits and must remain unchanged. This invocation writes only review/status/next-action fields in the seven preparation documents, the LOW ledger entry, and this review; it does not alter the submitted technical design or acceptance cases.

`PASS` below concerns preparation coverage/source evidence; **all S1 runtime tests remain NOT_RUN / NOT_IMPLEMENTED**, and IDE/static results remain NOT_VERIFIED. Mandatory runtime unknowns are carried into the future handoff, not misreported as PASS.

| Shared review dimensions | Disposition / evidence |
| --- | --- |
| 1 intent; 2 traceability | PASS: D00-D07, selected S1, GDI26-01..16 slice allocation, PH-03 mapped to 08/13. |
| 3 architecture; 4 ownership | PASS for approved additive design: Drive owns credentials/table/adapters, no business-module edges; future ADR/canonical delta and Modulith proof required. |
| 5 folder/package structure; 6 shared APIs/exceptions | PASS: unchanged full relevant inventory reused; root envelope/module-local web conventions and callback exception explicit. No production package added in this review. |
| 7 SOLID | N/A to unchanged implementation; selected owner-local responsibilities considered without speculative interfaces. |
| 8 DRY/KISS; 9 patterns | PASS: existing framework reuse, small local authority/fence, no generic provider platform or jobs. |
| 10 lifecycle; 11 public API; 12 validation | PASS for contract: slot/subject reconnect, revoke ordering/status, exact callback, one-use state/cookie/PKCE/nonce and finite bounds. Actual handlers NOT_RUN. |
| 13 authorization/privacy; 14 credentials | PASS for concrete design and future negatives; verifier/AEAD/key/ACL/secret-exposure runtime NOT_VERIFIED. |
| 15 concurrency; 16 recovery/operations | PASS for PH-03 mechanism and deterministic proof specification, including uncertainty/restart limits. Runtime and actual deployment NOT_VERIFIED. |
| 17 database; 18 resources | PASS for proposed constraints/CAS, bounded memory/TTL/provider work and no SQL lock across HTTP. Migration, capacity and actual transport measurements NOT_RUN. |
| 19 retrieval/recommendations; 20 parsing/OCR | N/A: no RAG, extraction, indexing or content acceptance in S1. |
| 21 security negatives; 22 test quality | PASS for six focused future groups, fake project-grant barriers and real PostgreSQL planned; no executed security/DB/backend result asserted. |
| 23 performance benchmarks; 24 evaluation science | N/A: no approved benchmark, model or evaluation capability. No 5-TB performance claim. |
| 25 dependencies | NOT_VERIFIED until implementation selection: one compatible OAuth client on the current Boot-managed baseline, no unrelated upgrade. Not a missing product decision. |
| 26 build/tooling/static diagnostics | PASS for existing harness reuse; execution/IDE/static NOT_VERIFIED. Normal watchdog 300s, preapproved full-suite-only maximum 2,100s; no extended run authorized here. |
| 27 logging | PASS for scoped exclusions and future capture tests; actual leak prevention NOT_RUN. |
| 28 docs/governance | PASS for current readiness/decisions/next-action synchronization; PH-L01 LOW/OPEN and legacy drift deferred. |
| 29 hygiene/delivery | PASS: scoped documentation/status changes only; immutable evidence and Owner-only publication preserved. |

## Exact handoff boundary and remaining future proof

After Owner publication, `$codex-create-handoff` may translate this accepted preparation into **connection-only S1**, keeping ACTIVE empty until that separate invocation. Fix concrete timeout/retry budgets, cookie/attempt ordering, API status/body inventory and bounded-state admission behavior there. Capacity exhaustion must fail closed rather than evict live/uncertain fences. These are ordinary finite implementation parameters inside the approved design, not new features or approval gates.

Include the accepted additive Drive amendment's ADR, canonical additive schema representation preserving historical v1, module/package/architecture references and applicable derived documentation. Normal Flyway migration application is distinct from disabled-mode Drive connection mutation/provider I/O. Preserve existing JWT behavior, Account metadata, Media S3, Feed default OFF and RAG inactivity. No cross-module internal imports, blanket public matcher or auto-scheduling.

Retain all six focused verification groups, especially GDI26-08/13 remote-effect and local-CAS cases, key/crypto failures, disabled context, API inventory and Modulith/schema checks. Use new unique receipt/log paths and the existing watchdog. Real Google consent, client/key provisioning, localhost browser cookie verification and any remote HTTPS/deployment activation remain NOT_VERIFIED and separately gated. No real token is required to create the handoff or prove deterministic local contracts.

## Docs Sync Check and publication route

- **UPDATED:** readiness, PH-03 closure, LOW ledger and next-action fields in the seven preparation documents; this independent review. No technical requirement or acceptance case relaxed.
- **ALREADY_CONSISTENT:** D00-D07, source baseline, connection-only S1, later S2/S3 and frontend gates, default OFF, frozen Account/Media/JWT/RAG and S2B operational separation.
- **IMMUTABLE:** original review, re-review 1 and dated Owner decision. Their old review-time status labels describe their submission snapshots; current readiness is this review and canonical README.
- **NOT_VERIFIED:** every application/runtime/Google/browser/deployment case and dependency choice. These remain explicit implementation/operational obligations.
- **DEFERRED_OUT_OF_SCOPE:** PH-L01 historical anchor and prior legacy notices/LOW backlog listed in the initial review. No repository-wide cleanup or additional preparation re-review is required for those.

Suggested Owner commit: `docs(drive): prepare GDI26-S1 connection authority`.

**Next step:** Owner → Codex | Owner stages/commits/pushes reviewed preparation, then Codex `$codex-create-handoff` | GDI26 connection-only S1, canonical README and this re-review | READY_FOR_HANDOFF; verify publication before creating the executable handoff; no implementation/live activation now | implementation NOT_STARTED; remediation 0/3 unchanged; takeover NOT_AUTHORIZED.
