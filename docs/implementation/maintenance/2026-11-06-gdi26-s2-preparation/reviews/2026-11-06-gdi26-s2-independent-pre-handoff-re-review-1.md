# Independent pre-handoff re-review 1 — GDI26-S2

- **Date / reviewer / mode:** 2026-11-06 / Codex / Owner-approved maintenance preparation, review only.
- **Result: READY_FOR_HANDOFF.** PH-01..03 are **VERIFIED / CLOSED for preparation**. The PH-04 compatibility question investigated during this review is **RESOLVED by primary documentation**, as recorded below; it is not an open finding. PH-L01 is LOW / OPEN / nonblocking. No blocking preparation finding remains. No S2 implementation or runtime acceptance is claimed.
- **Authority:** unchanged [S2 D08–D10](../owner-decision-record-s2-2026-11-06.md) and D00–D07; [initial independent review](2026-11-06-gdi26-s2-independent-pre-handoff-review.md), Owner policy and documentation-synchronization contract. No new material product choice is required for the minimal correction below.
- **Baseline:** local HEAD and freshly queried `origin/main` both `1386ef07d659a69183cc651b98fba5ac6366e938`. Entry worktree remains docs-only S2 preparation plus its maintenance-index entry. ACTIVE stays **NO_ACTIVE_HANDOFF**.
- **Counter:** S2 Antigravity **0/3 consumed**; takeover **NOT_AUTHORIZED**. This is preparation re-review 1, not implementation remediation 1. S1's accepted exceptional takeover is separate and stopped.
- **Write boundary:** new review and current S2 README/risk/index status only. Technical corrections remain ChatGPT's work; no code/tests/migrations/handoff, Owner records, accepted history, hooks or logs are modified.

## Prior finding adjudication

| Finding | Independent preparation result |
| --- | --- |
| PH-01 — READY required before first root | **VERIFIED / CLOSED.** Folder lifecycle and security now distinguish the common S1 Owner/subject/epoch/fence predicate from content-only READY requirements. The ABSENT/PENDING/RECONCILIATION_REQUIRED/READY matrix admits first ensure, reserves a durable root identity and rechecks both root CAS and S1 authority before READY. S2-03..05 trace first creation, concurrent/restarted ensure and no content access before READY. |
| PH-02 — inconsistent/unbound provenance | **VERIFIED / CLOSED.** Roots and files now require private markers bound to random per-root UUID and exact provider ID; file creation also records an action digest. Internal provider field projections include parents/trashed/appProperties, while public DTOs omit them. S2-06/08/13/15 include U-created file inserted in V root, copied/wrong-ID/missing proof and moved-out negatives. No invented Google subject field, filename authority or project-wide marker substitutes for current subject/root checks. Root-parent compatibility is the separate new PH-04 below. |
| PH-03 — lost upload response has no stable identity | **VERIFIED / CLOSED for the required duplicate-prevention design.** The existing multipart route now requires a stable timestamped UUID key. A bounded durable `(subject, root binding, key)` intent retains server-observed payload/metadata fingerprint and one provider ID before dispatch; CAS records dispatch before remote mutation. After dispatch, the same action only reconciles that ID by reads, including after response loss/restart. Changed payload conflicts, uncertainty is not evicted, terminal pruning cannot re-admit an aged absent key, and S2-11 requires the actual duplicate-bypass failing control. The proposed second metadata table and Portability/schema impact are explicit throughout the preparation. Pre-dispatch retry wording has the nonblocking PH-L01 below. |

These are design/traceability closures. The 20 prospective S2 cases remain NOT_RUN. Neither a passing fake nor a completed documentation review certifies Cloud/consent/browser/real provider operation.

## Resolved provider compatibility question

### GDI26-S2-PH-04 — canonical My Drive root lookup under drive.file

**RESOLVED / SOURCE-DOCUMENTED; S2-03/05/06 and all content-route root checks.** [folder-lifecycle-and-persistence.md:7](../folder-lifecycle-and-persistence.md) requires resolving U's **actual My Drive root ID** through U's credential; its READY/provenance rules and [security-and-concurrency.md](../security-and-concurrency.md) make correct My Drive parent verification mandatory. This review investigated whether that technical requirement could be fulfilled without broadening the selected scope.

Google describes drive.file as per-file app access, not general metadata access. A maintained primary implementation provides concrete contrary evidence to assuming canonical-root lookup always works: rclone calls `Files.Get("root").Fields("id")`, but explicitly handles a 404 as scope denial and uses the root alias instead. [Google scope contract](https://developers.google.com/workspace/drive/api/guides/api-specific-auth), [rclone getRootID and root initialization](https://github.com/rclone/rclone/blob/master/backend/drive/drive.go)

**Resolution:** Google v2 `about.get` explicitly accepts `drive.file`, and its About resource exposes `rootFolderId`. The later handoff can therefore select the bounded authenticated read **`GET https://www.googleapis.com/drive/v2/about?fields=rootFolderId`** using the current U credential, validate the nonblank bounded opaque result, and compare the PPV root's actual parent with that value. This fulfills the existing proposal without requiring access to the pre-existing root File resource, extra OAuth permission, Picker, a new SDK, a public route or an Owner product decision. All folder/file create/list/media operations remain their selected v3 operations. [Google v2 about.get and accepted scopes](https://developers.google.com/workspace/drive/api/reference/rest/v2/about/get), [About.rootFolderId](https://developers.google.com/workspace/drive/api/reference/rest/v2/about)

**Required handoff refinement:** include this exact trusted endpoint/field projection in the provider allowlist and bounded metadata budget; keep current subject/epoch/fence checks around dispatch/publication. Treat absent/malformed/refused account metadata as unresolved authority, never as a successful parent match. The documented `root` alias is valid for placing the initial created folder in U's My Drive; do not compare the literal alias against a returned opaque parent ID. A bounded root-membership query is also a documented technical alternative if explicitly selected instead, not a reason to add both mechanisms or a general fallback framework. [Root alias](https://developers.google.com/workspace/drive/api/guides/folder), [Parent/private-property queries](https://developers.google.com/workspace/drive/api/guides/search-files)

**Evidence limit:** these are primary-documentation feasibility facts, not a newly executed Google request or a claim that all File-root lookups fail. Do not equate a 404 with a deleted PPV folder or successful proof. No broader scope, name scan/adoption, caller-selected parent or live verification is authorized.

**Future deterministic proof:** the handoff must trace S2-03/05/06 to account-root metadata success/failure, a denied `files.get(root)` with valid about/rootFolderId and usable app-created folder, moved-out PPV root, wrong ID/private proof, malformed/oversized About response and stale local authority. Creation uses U's root alias and subsequent verification uses the exact account root ID. No mandatory preparation unknown remains: the exact lookup is documented here for handoff composition; implementation/fake/live evidence retain their proper later gates.

## Nonblocking clarification

**GDI26-S2-PH-L01 — LOW / OPEN.** [folder-lifecycle-and-persistence.md:47](../folder-lifecycle-and-persistence.md) permits resuming a provably pre-dispatch PREPARING/RESERVED row after a crash. [file-api-and-io.md:45](../file-api-and-io.md) groups those states with DISPATCHED and says all same-key retries perform no create. Clarify the recovered/no-live-owner pre-dispatch case separately from a concurrent loser and from any dispatched/ambiguous action, with one claimant/CAS and one retained provider ID. This does not defeat the specified post-dispatch duplicate prevention: both current descriptions forbid another create after DISPATCHED and fail closed on ambiguity. Align the table during the bounded correction or precise handoff; no separate LOW-only rework loop is required.

## Proportionality, scope and finite handoff requirements

The [initial necessity/effort matrix](2026-11-06-gdi26-s2-independent-pre-handoff-review.md#source-backed-feasibility-and-necessity) remains applicable, with the corrected mechanisms below. Effort is a relative design estimate, not measured performance or a delivery-time promise.

| Owner-visible behavior / mechanism | Disposition / simplest adequate implementation | Implementation and verification burden / authority |
| --- | --- | --- |
| Two-account authority and four private routes | **REUSE / KEEP** existing JWT, Drive crypto/current-epoch/fence boundary, root ApiResponse and binary-stream conventions; no new login or public matcher | Medium authority/barrier work; existing S1 regression evidence qualified by changed inputs. D01/D04; projected 235/239/243 route counts remain future measurements. |
| Initial ensure and later root reuse | **KEEP / SIMPLIFY** one per-subject durable root intent, pre-generated ID, root-bound marker and documented narrow account-root lookup | Medium state/PG/provider work; include the resolved PH-04 call/proof in the handoff. D06/D08; no name scan/adoption/deletion or multi-JVM coordinator. |
| Direct binary list | **KEEP** fixed-parent query, bounded pages/cursor and private proof fields projected to safe DTO | Small–medium validation/cursor/provenance tests. D08; no recursion, imported-file discovery or full file index. |
| Original-byte upload / duplicate prevention | **KEEP / SIMPLIFY** required stable key, bounded protected spool and minimal durable action ledger; one provider identity, read-only recovery after dispatch | Largest S2 area: fingerprint/PG/CAS, response-loss/restart/capacity/prune/slow-body tests and real bypass control. D09/D10 and original S2-11; no persisted Google session, resume UI, background job or new route. |
| Binary download | **KEEP / REUSE** authorized metadata followed by bounded attachment stream and safe truncation semantics | Medium stream/size/disconnect/headers tests. D09; no native export, abuse override, token URL or rollback of emitted bytes. |
| Two metadata tables / one additive migration | **KEEP** explicit root and upload-action relations, bounded retention, narrowly excluded from Portability | Medium constraints/schema/export/docs proof. A second table is justified by durable lost-response identity; Owner publication of reviewed preparation and a separate handoff must precede implementation. V25/v1 and accepted S1 history stay unchanged. |
| Dependencies, agents, hooks, benchmarks | **REUSE / REMOVE** existing Java/Spring/JDK/Nimbus, backend-implementer, testing/self-review and watchdog; no proven need for a new SDK, custom agent, hook or benchmark suite | No added framework or upgrade project. Existing native-hook/trusted-root limits remain limitations. Python/RAG coding and evaluation are N/A. |
| Explorer/nested folders/Picker/share/delete/copy/sync/RAG | **DEFER / EXCLUDE** | No S2 implementation/test burden; D02/D03/D05/D08/D09 and current Owner holds. |

**Delivery sequence unchanged:** published S1 → separately authorized S2 backend vertical slice → separately gated S3 UI. Implement S2 in authority/root, list, upload and download increments, then its bounded slice review/Owner commit. No second active handoff, automatic S3 or RAG milestone is created. Eventual final maintenance aggregation reuses source-applicable accepted slice evidence.

The later handoff must fix exact numeric streaming/admission/cursor/spool budgets, provider session-URI validation, unambiguous fingerprint encoding, enum/SQL constraints and exact selectors. Preserve the five planned focused groups (folder/PG; adapter/I/O; auth; architecture/export/adjacent/S1 drift; docs/config), normal-profile **300s** watchdog, fresh selected XML/source hashes and source-qualified reuse. No full-backend run is justified for this docs-only correction. Include the S2-11 duplicate-bypass actual assertion failure and restored green proof; do not add production bypass flags. Antigravity self-review precedes independent final review; three implementation correction/re-review rounds remain the ordinary limit before an explicit Owner takeover decision.

## Inspection and evidence

- Re-read all eight current S2 preparation documents, the maintenance-index change and the immutable initial review; traced the revised predicates, markers, ledger, four endpoints and S2-01..20 acceptance. No new source/agent/skill/rule/hook/dependency was introduced.
- Final scoped documentation check covered ten Markdown files (including both reviews), 44 local links and their referenced Markdown anchors: no missing target/anchor or trailing whitespace. `git diff --check` passed; protected artifacts and all 50 reviewed backend inputs remain unchanged.
- Reused the prior actual inspection of root/scoped AGENTS, policy/Docs Sync, ADR/module/schema/package/API ownership, Java/security/persistence/HTTP/reuse/testing skills, backend-implementer and native hook/watchdog limitations. Unchanged Graphify cache predates Drive and supplies no S2 authority.
- Independently compared all **50 accepted S1 backend input hashes** again: no drift. S1's 224 tests/control are reused S1 history, not fresh S2 evidence. No build/test/provider/model execution was performed. Runtime/static/IDE validation is NOT_VERIFIED; no warning-free claim.
- Preserved ACTIVE SHA-256 `166752b2248b9a8c0b9810d1280e9f90becab8dba3d1df179395cdb932aba646`, S2 Owner-record hash `b56b3533bc7aab59a899139bf17b121e9c2fcd40a62288d21b1911984cbdb9c4`, and initial-review hash `d3404d1f2a1242eb0085b666a897da4364bd2dbd932bbe52fa62a8bcef834b12`.

## Shared coverage and Docs Sync Check

| Dimensions | Re-review disposition / rationale |
| --- | --- |
| 1 intent; 2 traceability | **PASS** for unchanged D00–D10, PH-01..03 closure and source-documented PH-04 feasibility/proof refinement. |
| 3 architecture; 4 ownership | **PASS:** explicit two-table additive proposal, existing leaf module, scoped Portability exclusion, no business-module edge or V25 rewrite. |
| 5 packages; 6 shared APIs/exceptions | **PASS for preparation:** current inventory/new capability packages and root-envelope/local-advice strategy unchanged. S1 LOW descriptor/citation/anchor debt remains deferred. |
| 7 SOLID; 8 DRY/KISS; 9 patterns | **PASS:** correction has a concrete bounded persistence purpose, no connector framework or duplicate S1 custody hierarchy. PH-L01 is a minor state-table clarification. |
| 10 business; 11 API; 12 validation | **PASS as plans:** documented narrow parent proof, corrected authority, private/public projections, upload key/fingerprint/retention and input/error contracts. PH-L01 remains a nonblocking state-table clarification. |
| 13 auth/privacy; 14 credentials/retention | **PASS** for current-subject/fence, mandatory per-root/file proof, narrow account-root lookup, token-free bounded metadata, protected spool and default OFF; no broader scope. |
| 15 concurrency; 16 recovery; 17 database | **PASS for corrected plan:** durable uniqueness/dispatch-before-I/O/CAS, no SQL over HTTP, same-ID uncertain reconciliation, terminal-only prune and expired absent-key denial. PH-L01 LOW; all S2 runtime/PG proof remains **NOT_VERIFIED / NOT_RUN**. |
| 18 bounds | **PASS for plan:** fixed 50MB/55MB, finite pages/streams/intents (8 nonterminal/1024 total per root), retained uncertainty and no full buffer/scan. Exact handoff budgets and measured cleanup remain future gates. |
| 19 retrieval; 20 parsing/OCR; 24 evaluation | **N/A:** opaque original binary storage, no model/RAG/parser/evaluation work. |
| 21 security tests; 22 test quality | **PASS for prospective deterministic PG/security/HTTP/barrier/control plan**, with PH-04 fixture addition required. No S2 runtime pass asserted. |
| 23 benchmarks; 25 dependencies | **N/A** for unapproved benchmarks; **PASS** for managed baseline reuse/no new dependency or upgrade. No fresh supply-chain audit claimed. |
| 26 build/static/tooling; 27 logging | **PASS for bounded future harness and safe diagnostics plan**. IDE/static and all new runtime/stream evidence **NOT_VERIFIED**; no new warning/observability project required. |
| 28 docs/governance; 29 hygiene | **PASS with PH-L01 LOW:** docs-only changes, prior review/Owner/counters/source/logs preserved, current status synchronized and scoped links checked. |

- **UPDATED:** this new independent review, current S2 README/risk/navigation status to READY_FOR_HANDOFF, independent PH-01..03 closure and resolved PH-04 provider lookup. The initial CHANGES_REQUESTED report remains immutable; this review supersedes its current finding state only.
- **ALREADY_CONSISTENT:** actual baseline, D00–D10, S1 published acceptance, ACTIVE=NO_ACTIVE_HANDOFF, default Drive/files/Feed OFF, no source/activation/RAG change. No material Owner decision remains open; the exact compatible lookup is a handoff refinement documented by this review.
- **DEFERRED_OUT_OF_SCOPE:** S1 FR-L01/02/PH-L01, legacy notices in root/scoped AGENTS/policy/backend-implementer, submitted diagram badges and unrelated backlog; no repository-wide cleanup or future feature authorization.
- **BLOCKED_OWNER_DECISION:** none. No broader scope/Picker/scan is required.
- **NOT_VERIFIED:** all S2 runtime/stream/PG/security evidence and actual Google/Cloud/browser/key/backup/one-JVM/HTTPS operation. These remain future acceptance/operational gates, not invented runtime passes or unresolved mandatory preparation proof.

The preparation is ready for Owner publication. The later executable handoff must incorporate the concrete provider lookup/proof above and align PH-L01's pre-dispatch retry wording; this is ordinary handoff precision, not another preparation or LOW-only review loop. Preserve both reviews and Owner decisions. Only actual Owner publication and a separately invoked `$codex-create-handoff` may open S2 implementation authority; this review creates no handoff and enables nothing.

Commit message: `docs(drive): prepare bounded folder and file operations`

**Next step:** Owner | normal git add/commit/push of reviewed preparation, then Codex `$codex-create-handoff` | GDI26-S2 README and this re-review | READY_FOR_HANDOFF; require actual preparation publication, incorporate documented lookup/proof, keep ACTIVE=NO_ACTIVE_HANDOFF until handoff creation | S2 Antigravity 0/3 consumed; takeover NOT_AUTHORIZED.
