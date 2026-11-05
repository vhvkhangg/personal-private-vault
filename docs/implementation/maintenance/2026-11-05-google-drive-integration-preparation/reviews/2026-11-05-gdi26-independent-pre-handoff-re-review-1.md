# GDI26 independent pre-handoff re-review 1

- **Date / reviewer / mode:** 2026-11-05 / Codex / maintenance preparation only.
- **Final status:** **CHANGES_REQUESTED**. Original **GDI26-PH-01 and GDI26-PH-02 CLOSED for preparation**; new **GDI26-PH-03 MEDIUM / OPEN** blocks READY_FOR_HANDOFF. No further Owner product selection is needed to make this bounded technical correction.
- **Baseline:** HEAD and local `origin/main` remain `f4144e89ebc27b59225fd21f59dcb3dc6f1ebbf4`. Tracked source/build/test/architecture diff is empty. Entry changes are eight preparation/decision documents plus the original review, all untracked in this directory.
- **Authority:** [Owner decision D00-D07](../owner-decision-record-2026-11-05.md), approved for preparation/design, and the explicit re-review invocation. No implementation, live Google consent, frontend or activation authority follows.
- **Execution / counters:** `ACTIVE=NO_ACTIVE_HANDOFF`; S1 NOT_STARTED; Antigravity remediation **0/3 unchanged**; takeover NOT_AUTHORIZED. This is preparation correction/re-review, not an implementation remediation round.

## Original findings adjudication

| Finding | Disposition / evidence |
| --- | --- |
| GDI26-PH-01, scope selection | **CLOSED for preparation.** The dated Owner record and corrected scope consistently select two independent accounts, `drive.file`/OIDC, separate Drive-managed file storage with Media S3 retained, three bounded slices, backend-first, app-created folders in S2 and localhost-first operation. S1 is connection authority only. No unresolved product choice is inferred from the initial historical review. |
| GDI26-PH-02, concrete security/ownership proposal | **CLOSED for its original preparation ambiguity.** The corrected documents now specify the exact callback, short-lived browser-bound state, PKCE/nonce, external AES-256-GCM key file, encrypted two-slot relation, stable subject, credential epoch/CAS, reconnect/disconnect and a dependency-free Drive module. D04 approves the bounded amendment for review. Runtime proof remains prospective; the separate defect below concerns remote ordering in that concrete design. |

The original [review](2026-11-05-gdi26-independent-pre-handoff-review.md) is immutable. Its entry SHA-256 was `7a7a9cd6ff6fc2306b462fa36b2e288e18ef0e811f18876e414dd5f630b1f5c4`; the new adjudication belongs here, not in that historical report.

## GDI26-PH-03 — local credential CAS does not order project-grant revocation against reconnect

- **Severity / lifecycle / gate:** **MEDIUM / OPEN / blocks READY_FOR_HANDOFF**. This is a concrete preparation contract gap, not a claimed production failure in unimplemented code.
- **Locations:** [oauth-security-and-data-governance.md](../oauth-security-and-data-governance.md), disconnect and concurrency rules at **lines 27-28**; [drive-api-ui-and-storage-plan.md](../drive-api-ui-and-storage-plan.md), transaction/CAS rule at **line 40**; [acceptance-and-review-plan.md](../acceptance-and-review-plan.md), **GDI26-08 and GDI26-13 at lines 14 and 19**.
- **Specified behavior:** disconnect deletes local credentials/pending attempts, then revokes the captured old token remotely. Fresh grant, refresh and disconnect use local slot/epoch checks; no SQL lock spans external HTTP. No rule prevents fresh credentials for the same Google subject from being committed while the earlier remote revoke remains in flight.
- **Provider fact:** Google documents revocation of the associated user's project grants across clients, rather than deletion of just the supplied token; its effect can also be delayed. See [Google web-server OAuth, Token revocation](https://developers.google.com/identity/protocols/oauth2/web-server#tokenrevoke). This concerns the **same Google subject/project**, not the other independent Google account.
- **Design counterexample (reasoned interleaving, not an executed test):**
  1. Slot 1 has subject U, epoch E1 and token R1.
  2. Disconnect deletes the local row and starts revoking R1; the provider request is delayed.
  3. A new callback grants U token R2 and commits CONNECTED in the now-empty slot 1, or in slot 2. Unique-subject and local epoch checks can all pass.
  4. The delayed R1 revoke is processed and invalidates U's project grant, including R2. No local stale write occurs, so the proposed CAS protection does not prevent this self-induced loss of the new connection.
- **Impact:** a successful fresh reconnect can be invalidated by PPV's own older disconnect operation. The current tests address stale local writes and future dispatch, but do not specify this provider-side race or the result of an ambiguous revoke timeout.
- **Smallest correction:** document subject/project-aware ordering between locally outstanding revocations and new credential commitment, including reconnect into the other slot. Bounded in-process coordination is adequate to consider under the approved single-active-process S1; do not add SQL locks across HTTP, distributed locks, durable jobs or a generic orchestration framework. State what the API reports while revocation is pending and after uncertain timeout/failure, and prevent a later retry for an obsolete disconnect from targeting a newly accepted grant. Preserve best-effort remote revoke and immediate local invalidation. Do not promise that a successful remote response establishes instantaneous or permanent grant validity.
- **Required future proof:** extend GDI26-08/13 with a controlled provider barrier that models **subject/project grant revocation**, not merely invalidation of R1. Exercise a fresh U grant in the same slot and the other slot while old revocation is delayed; the callback must not report a usable new connection that PPV knowingly revokes afterward. Also specify/test bounded failure/timeout reporting and late-response handling. Retain the existing stale refresh/callback CAS tests. No real account, token or Google API call is needed for this correction or its eventual deterministic tests.
- **Owner/architecture impact:** **none required for the technical correction as scoped**. It implements the already-approved D04 lifecycle in one local process. A proposed scope expansion or new persistence design would require separate justification; it is not the requested remedy.

## Proportionality and complete preparation coverage

| Owner-visible capability / mechanism | Disposition / effort / approval |
| --- | --- |
| Two independent Google subjects and connection slots | KEEP; Owner-selected, moderate implementation and focused PostgreSQL/security test effort. |
| Connect/status/same-subject reconnect/disconnect | KEEP S1; fix PH-03 through one bounded ordering rule and regression schedule. No new feature required. |
| Existing JWT, root ApiResponse, framework HTTP/security and test harness | REUSE; preserve bearer behavior. Exactly four proposed routes, one exact public GET callback with its deliberate browser-response exception; review the API inventory delta explicitly. |
| AES-GCM/key file and Drive-owned two-slot relation | KEEP approved D04 design; no secret in Account/Settings or PPV refresh-token storage. Actual crypto, key, migration and recovery proof NOT_RUN. |
| Ephemeral pending state, local single-process coordination | SIMPLIFY; bounded TTL/capacity, no durable OAuth attempts, queue or distributed subsystem. PH-03 must account for provider effects as well as local writes. |
| App-created folders, bounded list/upload/download | DEFER to separately authorized S2; its folder ancestry, streaming and ambiguous-upload proof are not S1 acceptance. No broad-scope fallback. |
| Explorer/browser file UI | DEFER to S3 and its frontend authorization. Same-browser callback testing is not delivery of the Explorer. |
| Media provider replacement, delete/move/share/copy, sync and RAG | REMOVE from current scope / REJECT as implicit additions. Media S3 and suspended RAG remain unchanged. |
| Agents, hooks, skills, models, benchmarks and broad regression | REUSE existing backend implementer, mandatory self-review, independent review and watchdog. No new agent/model/tooling or terabyte-scale benchmark; source-qualified focused checks suffice. |

Reviewed all eight corrected preparation/decision documents and the original review. Complete local-link validation checked **23 targets, 0 missing**, using UTF-8 output after a console-encoding issue interrupted an earlier source-excerpt print. No file corruption or failed backend test is implied by that console issue. Rechecked the existing security matcher source and source/build baseline. The initial review's current-source/package/harness inventory remains applicable because those tracked files have not changed; no repeated Graphify rebuild, broad source audit or backend runtime suite was warranted.

Public source verification additionally checked Google's [OIDC discovery metadata](https://accounts.google.com/.well-known/openid-configuration), which advertises S256 and the code/refresh grant endpoints; the [Drive scope guide](https://developers.google.com/workspace/drive/api/guides/api-specific-auth); and the [folder-creation guide](https://developers.google.com/workspace/drive/api/guides/folder), whose `drive.file` example supports the planned app-created folder direction. These are public reference/discovery reads, not OAuth exchanges, live Drive operations, personal account verification or application tests.

`PASS` in the ledger means a checked preparation/source fact or adequate future proof obligation. **All implementation/runtime cases remain NOT_RUN**, and static analysis/IDE status remains NOT_VERIFIED.

| Shared coverage dimensions | Re-review status / evidence |
| --- | --- |
| 1 intent; 2 requirement traceability | 1 PASS: D00-D07 and S1 boundaries settled. 2 FINDING: PH-03 needs one explicit lifecycle/acceptance addition. |
| 3 architecture; 4 module ownership | PASS for the approved additive proposal and zero new business edges; actual ADR/canonical delta, descriptors and module proof must accompany authorized implementation. |
| 5 package/folder structure; 6 shared contracts/exceptions | PASS: unchanged complete relevant subtree inventory reused; Drive owns adapters/DTOs, root envelope retained, callback response exception explicit. No new package exists yet. |
| 7 SOLID | N/A to unchanged code; concrete ownership reviewed without requiring speculative interfaces. |
| 8 simplicity/reuse; 9 patterns | PASS: small dedicated integration, no generic connector framework, extra jobs or agents. |
| 10 lifecycle correctness | FINDING: PH-03 remote grant ordering, distinct from the otherwise specified local CAS. |
| 11 API compatibility; 12 validation | PASS for preparation: four bounded routes, slot 1/2, exact callback, state/cookie/nonce/scope checks; concrete handler/status inventory proof remains future. |
| 13 authorization/privacy; 14 credential custody | PASS for selected design and prospective negatives; actual verifier, ciphertext/AAD/key/ACL/logging behavior NOT_RUN. |
| 15 concurrency; 16 recovery/operations | FINDING: PH-03 in-flight revoke/regrant and uncertain completion; deployment, live consent and browser cookie proof remain separately NOT_VERIFIED. |
| 17 database | PASS for proposed slot/sub/status/epoch constraints and independent ownership; migration/runtime NOT_RUN. |
| 18 resource bounds | PASS for finite pending-state/TTL, exactly two slots and bounded provider work; exact selected transport budgets belong in the executable handoff. No performance claim. |
| 19 retrieval/recommendation; 20 document/OCR | N/A: no RAG, parsing, ingestion or recommendations. |
| 21 security negatives; 22 test quality | FINDING only for the missing PH-03 remote-effect schedule; existing fake OIDC/JWT/PostgreSQL/key/disabled-mode gates retained. No runtime PASS asserted. |
| 23 benchmarks; 24 evaluation science | N/A: no approved benchmark or ML/evaluation capability. |
| 25 dependencies | NOT_VERIFIED selection/runtime compatibility: use one compatible OAuth client with the existing managed baseline; no unrelated upgrade or competing SDK framework. |
| 26 build/tooling/static checks | PASS for focused future harness reuse; tests/static/IDE NOT_VERIFIED. Ordinary watchdog cap 300s, preapproved full-suite-only cap at most 2,100s. No extended verification authorized here. |
| 27 logging | PASS for prospective secret/code/state/body exclusions and tests; actual behavior NOT_RUN. |
| 28 documentation/governance | FINDING PH-03 correction; product decisions and links PASS; current status/Docs Sync recorded below. |
| 29 hygiene/delivery | PASS: only canonical scope status and this new review written; initial review/decision/source retained, no handoff/publishing. |

## Finite correction and future handoff boundary

ChatGPT should correct only the S1 revocation/reconnect rules, affected acceptance/risk entries and current status/navigation. Keep D00-D07, the two-slot table design, localhost-first scope, deferred S2/S3 and immutable reviews intact. Codex re-review should inspect that delta and reuse unchanged evidence. This needs no production patch, live credential, new implementation handoff or new Owner questionnaire.

When the scope later reaches READY_FOR_HANDOFF, the handoff must include the ordinary approved amendment/synchronization work: a bounded new/updated ADR for the Drive module/schema/security delta, its canonical additive schema representation while preserving the historical v1 DBML, affected module/package/architecture references and any applicable derived diagram. Do not substitute an undocumented schema fork for canonical synchronization. Normal additive Flyway application is distinct from disabled-mode **Drive connection mutations/provider I/O**; the feature flag must not make the database migration history conditional. These are normal handoff requirements within D04, not additional product approvals or separate findings.

Keep S1 verification at the existing six focused groups plus PH-03's controlled schedule. Real browser/local OAuth or remote deployment cannot be claimed complete from mocks. Each implemented slice still needs Antigravity self-review, independent Codex review and Owner-only publication; final feature aggregation reuses applicable evidence. No new remediation allowance is created here.

## Docs Sync Check

- **UPDATED:** canonical README disposition and next action; this dated re-review. PH-01/02 preparation closure is recorded here, not retroactively in the initial report.
- **ALREADY_CONSISTENT:** recorded D00-D07, connection-only S1, deferred S2/S3, S3 frontend gate, Media S3/Account/JWT/RAG boundaries, S2B OFF and separate activation gate, ACTIVE=NO_ACTIVE_HANDOFF.
- **FINDING / REQUIRED CORRECTION:** PH-03's security/acceptance/risk documentation; ChatGPT owns the bounded repair. Pre-re-review labels elsewhere in the submitted draft are superseded by the README/current review and should be synchronized with that repair.
- **NOT_VERIFIED:** all runtime/crypto/DB/Google/browser/deployment proof, Cloud/client/key provisioning and implementation dependency selection. None blocks preparation merely for lacking future execution evidence; PH-03 is the actual current design blocker.
- **DEFERRED_OUT_OF_SCOPE:** the initial review's ledger of legacy Owner/AGENTS/workflow/package notices and unrelated LOW/INFO backlog. No wider cleanup, historical finding closure or counter reset.

**Next step:** ChatGPT / Codex | ChatGPT makes the bounded PH-03 documentation correction, then Codex `$codex-pre-handoff-review` | GDI26 S1 security/acceptance/risk plan and this re-review | remote revoke/reconnect ordering and deterministic proof contract resolved before READY_FOR_HANDOFF; keep ACTIVE=NO_ACTIVE_HANDOFF | implementation NOT_STARTED; remediation 0/3 unchanged; takeover NOT_AUTHORIZED.
