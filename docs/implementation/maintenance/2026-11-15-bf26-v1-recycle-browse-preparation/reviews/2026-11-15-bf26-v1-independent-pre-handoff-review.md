# Independent pre-handoff review — BF26-V1-RECYCLE-BROWSE

- **Date / reviewer / mode:** 2026-11-15 / independent Codex / explicitly approved maintenance preparation; V1 only.
- **Outcome: `READY_FOR_HANDOFF` — TECHNICAL PREPARATION ONLY.** No blocking preparation finding was identified. The proposed contract is technically feasible within the qualified map below. **Owner product-contract ratification, exact frozen-source/API/test authorization and publication of reviewed preparation remain prerequisites to an executable handoff.** This review supplies none of those permissions and does not accept an implementation.
- **Authority:** [Owner preparation approval](../owner-approval-for-pre-handoff-review-2026-11-15.md) authorizes independent scrutiny of the six-document draft, preserving its proposed status. Selection of V1 does not ratify its fields, order, cursor or source map. Numbered-phase and RAG milestone prerequisites are N/A for this maintenance review.
- **Verified baseline:** branch `main`; HEAD, `origin/main` and live `git ls-remote origin refs/heads/main` all **`2d088445e6323b091741cf370e1e0c8cf13928a2`**. Entry tracked worktree/index diffs were empty; only this seven-file preparation directory was untracked. `ACTIVE = NO_ACTIVE_HANDOFF`. No branch/worktree was created.
- **Execution / counters:** no backend edits, compilation, runtime tests, database queries, generated OpenAPI, IDE inspection or publication. V1 implementation/remediation **NOT_STARTED / 0 rounds consumed**, no active V1 allowance opened; takeover **NOT_AUTHORIZED / NOT_USED**. Prior slice counters remain unchanged.

## Owner-visible capabilities and proportionality

Effort below is a **relative static estimate**, not measured duration, performance or a test quota. One additive vertical slice is sufficient; separate infrastructure or a new numbered phase would add no necessary capability.

| Requirement / mechanism | Decision and simplest adequate mechanism | Implementation / verification effort; approval and non-goal |
| --- | --- | --- |
| Discover existing trashed identities without retaining IDs | **KEEP / REUSE** Vault's existing controller, named entry API, repository and immutable views | Moderate / moderate; exact Phase 1/13 additive exception required; one PRIVATE GET only |
| Three item fields, no titles | **OWNER DECISION REQUIRED / SIMPLIFY** to `id,entryType,deletedAt`; dedicated web item DTO mapped from existing `VaultEntryView` | Low / low; useful for finding IDs and restoring, less convenient for recognizing content; no feature hydration |
| Continue a bounded list | **KEEP / REUSE** Feed's cursor-page concept within Vault ownership; explicit page DTO, `items,nextCursor`, `meta:null` | Moderate / moderate; Owner ratifies default 25, range 1–100 and bounded versioned cursor; no Feed internal import or shared paging framework |
| Order items | **OWNER DECISION REQUIRED**: proposed `id DESC` keyset with last-emitted ID | Low / moderate; stable identity order, not deletion chronology; no claim of cross-request snapshot |
| PRIVATE access, PUBLIC denial | **KEEP / REUSE** existing PRIVATE fallback and exact-eight PUBLIC GET sweep | No security implementation / moderate verification; no ninth PUBLIC route or session/PIN change |
| Existing trash/restore and metadata behavior | **KEEP / REUSE** existing operations and lifecycle/concurrency tests | No intended mutation change / focused regression; no purge, cascade or retention redesign |
| Error and cache behavior | **KEEP / REUSE** root envelope/error factories; route-local handling for the new GET where necessary | Low–moderate / moderate; prove safe 400/500 and no-store, preserve old Vault handlers/advice |
| SQL cost | **KEEP** bounded owner query; **DEFER** any index/tuning until actual plan evidence warrants an Owner decision | Low query work / moderate bounded synthetic verification; no latency SLA or new migration assumed |
| OpenAPI | **KEEP / REUSE** typed controller/DTO metadata and current generic customizer | Low / moderate; exact manifest and generated graph checks; root production edit not presently justified |
| New agents, skills, hooks, dependencies, generic paging library | **REMOVE from candidate scope / REUSE** existing backend implementer and review/test tooling | No implementation or upgrade; no new module, build change or infrastructure |
| Titles, recently-deleted ordering, count, filters, search, hard delete, UI, RAG and deployment | **DEFER** to a separately selected scope | No implementation or verification in V1; do not infer Owner demand |

Doing nothing remains a valid alternative if the Owner does not accept metadata-only browsing: existing restore-by-ID works. Reusing the existing five-field HTTP DTO would save one small DTO but would violate this draft's exact three-field candidate. A titled collection would require a materially different cross-owner design and approval. No optional enhancement is classified as a defect.

## Source, schema and harness qualification

`J/` means `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` means the analogous test root. Source references below are relative to those roots.

- The **17 file/blob claims** in [source baseline](../source-baseline-and-risk-inventory.md) match the current Git index/HEAD, including both shared API factory/page types and Vault instructions. Empty tracked diffs qualify this checkout against those committed inputs. No source drift was found. The four proposed NEW paths are absent; each existing parent directory already has `package-info.java`.
- Recursively enumerated Vault production subtree: **59 files / 17 package descriptors / zero `.gitkeep`**. Existing `entry` and `view` named interfaces and internal application/web/persistence packages fit all proposed additions. `vault/package-info.java` has zero allowed business dependencies, matching the dependency matrix. No descriptor, folder reorganization or new module is justified. Package-tree architecture remains applicable; its unrelated historical RAG wording is not V1 authority.
- `vault/entry/VaultEntryOperations.java:9` has create/find/findAll(ids)/trash/restore, not a trash collection. `vault/internal/web/controller/VaultController.java:32` owns the ID-based routes. Targeted controller/source searches found no equivalent Vault trash-list route. `vault/internal/infrastructure/persistence/VaultEntryRepository.java:12` is the owner-local query seam.
- `VaultEntryService.java:18` is class-level read-only; its write methods and source guards are separate. A single bounded scalar/view query, materialized inside that service transaction, does not require RO-B's multi-owner REPEATABLE_READ mechanism. Preserve existing isolation/guard semantics; no controller transaction, entity serialization or mutation lock is needed for browsing.
- **Existing indexes are present**, not merely inferred: `V1__create_schema_v1.sql:115` defines the identity table; lines 935–936 define `idx_vault_entries_entry_type` and `idx_vault_entries_deleted_at`, alongside the primary key. Frozen DBML `vault_entries` also lists `deleted_at`. Migration inventory found no later change to these indexes. This supports a no-migration candidate, **not** a claim that the proposed seek/order plan is fast on sparse trash.
- `authentication/internal/infrastructure/security/SecurityConfiguration.java:253–262` retains PRIVATE ASYNC/ERROR restrictions, exactly eight PUBLIC GET pairs and PRIVATE fallback. The new Vault GET falls under that fallback. `ProfileRouteAuthorizationIntegrationTest` already uses a registered-handler sweep with both persisted PUBLIC origins, a positively controlled MVC handler spy and domain snapshots. Retain these witnesses and add an explicit new-route assertion; a mock Vault method alone does not prove pre-handler denial.
- `ApiResponse`, `ApiResponses`, `ApiPageMeta` and ADR-0016 support a typed cursor page in `data` with `meta:null`. Do not expose the application view as the HTTP DTO, alter shared envelope types, or fabricate offset metadata. Feed's page/cursor is a conceptual precedent; it is neither a dependency nor a guarantee of this draft's stricter codec validation.
- `OpenApiConfiguration.java:252–329,406` generically preserves typed success schemas and adds protected-operation errors. It does not need a hardcoded registration for this new GET. Controller/DTO metadata can express PRIVATE scope, parameters and payload. Its existing generic extra GET 404 documentation does not require a V1-wide cleanup. **Exclude `J/OpenApiConfiguration.java` from the preferred source map.** If the actual generated graph later proves annotations insufficient, STOP with that evidence for an exact additional grant.
- Relevant source/tests inspected include the full Vault controller/service/repository/entity/view/mapper/advice, shared envelope/error handling, security matchers, Feed paging precedent, existing Vault lifecycle/concurrency/metadata fixtures, profile sweep, OpenAPI manifest, architecture test, web/PostgreSQL support and Search trash exclusion. Graphify was consulted as navigation only; all conclusions use canonical files.
- Applicable instructions/tooling inspected: root/docs/backend/test/Vault `AGENTS.md`; current Owner policy and Docs Sync; ADR-0016/API architecture, dependency matrix, package tree, frozen DBML and migration inventory; pre-handoff, coverage, documentation, REST, backend-testing, bounded-command and Graphify skills; existing backend-implementer role and foundation/REST/handoff rules. Existing backend implementer, watchdog and independent Codex review suffice. Python/RAG skills and takeover implementation are N/A; no takeover is authorized. No hook/configuration update or dependency selection is necessary.

## Qualified minimum candidate map — not permission

Retain **nine production / four test paths**. Separate cursor and page/item records fit existing conventions and have concrete responsibilities; no additional production file or new test class is presently necessary. Hashes are **Git blob IDs**, not SHA-256/runtime evidence.

| ID | Exact candidate path | Baseline / qualified purpose |
| --- | --- | --- |
| P01 | `J/vault/entry/VaultEntryOperations.java` | `22f1392dd16206422146ac770ec6d251f55e7975`; one bounded read capability; old signatures unchanged |
| P02 | `J/vault/internal/application/entry/VaultEntryService.java` | `648100017d024b7bea19c9cc9e7496b1c04ebe28`; validate, query, materialize immutable page |
| P03 | `J/vault/internal/infrastructure/persistence/VaultEntryRepository.java` | `b605742a45c95ff8a348407e724a63162d090531`; parameterized deleted-only ID seek and DB limit |
| P04 | `J/vault/internal/application/entry/VaultTrashCursor.java` **NEW** | Package-local bounded canonical codec; no shared cursor abstraction |
| P05 | `J/vault/view/VaultTrashPageView.java` **NEW** | Immutable defensive copy of existing item views plus nullable cursor |
| P06 | `J/vault/internal/web/controller/VaultController.java` | `de14f47df09f3ac1726d3c8655663eaf5c8c42e5`; single GET, typed wire/annotations, route-local validation/error/cache handling |
| P07 | `J/vault/internal/web/dto/VaultTrashItemResponse.java` **NEW** | Exact proposed three fields; no existing DTO shrinkage |
| P08 | `J/vault/internal/web/dto/VaultTrashPageResponse.java` **NEW** | Immutable items plus always-present nullable nextCursor |
| P09 | `J/vault/internal/web/mapper/VaultWebMapper.java` | `4f3b3a9ae7678d703a828c163fdfa7beaed35775`; pure new page/item mapping; old mapping preserved |
| T01 | `T/vault/VaultEntryIntegrationTest.java` | `6ae55cd88a3d856c50ac8cd2f311ad551b93bd51`; real owner query, pagination, SQL/plan and lifecycle witnesses |
| T02 | `T/vault/VaultWebIntegrationTest.java` | `63c7d7dd659559c0b590355f425a0483686acd93`; exact HTTP payload, input/failure/cache and read-only witnesses |
| T03 | `T/authentication/ProfileRouteAuthorizationIntegrationTest.java` | `d173776b8b128287efff1f3bb177cbd181d0f3af`; new PRIVATE positive/PUBLIC/anonymous denials; original eight and dispatch guards |
| T04 | `T/web/OpenApiRouteInventoryIntegrationTest.java` | `e72b4142e9c362bb0d1ce1ca7acf85525930eeca`; one manifest addition, coupled counts and generated graph/specimen assertions |

Read-only regression selectors additionally include `VaultMutationConcurrencyTest`, `VaultMetadataIntegrationTest`, `ApplicationArchitectureTests` and **`GlobalSearchIntegrationTest#verifiesRecycleBinExclusion`**. The last selector directly proves V1-15's existing text/tag trash exclusion; its source at lines 1578 onward was inspected. These are execution candidates after authorization, not additional edit paths. Qualify source/evidence applicability before reuse, especially lifecycle tests whose service input will change. No full Frontend/RAG suite follows from this review.

## Required carry-forward precision

These are preparation qualifications for the later Owner decision/handoff, not source repairs or newly granted features. Preserve V1-01–19; do not substitute these comments for runtime proof.

1. **No-store must cover failure paths, not just successful handler return.** `ApiExceptionHandler.java:57–64` can reset an uncommitted response; `vault/internal/web/advice/VaultExceptionAdvice.java:16–24` maps IllegalState/data-integrity exceptions to conflicts. Consequently, merely setting a header at successful entry/exit does not establish V1-13/17 or guarantee a new operational failure becomes 500. P06 can own the new route's safe validation/failure responses using existing factories, keeping parser validation inside the controlled path where necessary. Prove malformed numeric input (including conversion before handler if used), bad cursor, repository failure and transaction-completion failure give the intended safe envelope/status/no-store. Do not reinterpret a genuine read/transaction failure as empty success, invalid user input or a lifecycle 409; preserve all old route/advice semantics. Whether existing security header writers retain headers after reset is **NOT_VERIFIED**, not a proven defect. No global advice/filter change is authorized.
2. **Qualify what the SQL limit proves.** The original assessment explicitly prohibited whole-table **in-memory** filtering. Read V1-12's shortened wording with that distinction: one parameterized owner query, server-side seek/order/limit, no count, no growing offset and no Java-side whole-table paging. `limit+1` bounds qualifying rows fetched, not rows examined or latency. Record actual SQL and bounded synthetic sparse-trash EXPLAIN evidence; do not require a specific planner node on an empty/tiny fixture or claim a benchmark PASS without a defined workload/threshold. If measured scan cost is unacceptable, stop for an Owner decision on a concrete index/order change; do not silently expand the schema map. Runtime plan/latency remain NOT_VERIFIED here.
3. **Test the new operation and honest transaction boundaries.** T01 is currently class-level `@Transactional`. That existing fixture alone does not prove between-request committed restore/re-trash. Add an appropriately isolated committed sequence within the allowed test file (or the existing web fixture); use finite barriers only when an actual overlapping race is tested. Materialize immutable views before service return. Stable membership must cover three or more pages, all supported enum types, active gaps, max/min/default limit and last-emitted/lookahead boundaries. A restore or new high-ID trash can change later membership; no exactly-once snapshot claim is appropriate.
4. **Keep security and wire proof precise.** A forbidden request must never invoke the MVC business handler or new Vault read; authentication/session authority checks may themselves read persisted session state. Do not turn “no repository work” for invalid list input into an impossible claim of zero authentication SQL. Test both persisted PUBLIC origins, anonymous/invalid/expired credentials, PRIVATE access, exact three keys and explicit null cursor; reject hostile cursor input before the business repository read without logging raw tokens. Conditional HTTP must not turn a sensitive success into a cacheable or bodyless disclosure.
5. **Update all coupled API assertions within T04.** Its manifest lists existing Vault routes and has hardcoded operation/unique-ID totals at lines 405–406. Add exactly one route and adjust the relevant subset/manifest checks based on the actual generated graph; do not guess a repository-wide route total from the historical 239 subset. `listVaultTrash` has no current source/manifest collision. Verify actual data/page/item refs, nullable cursor, integer bounds, PRIVATE description and canonical 200/400/401/403/500 envelopes. Existing generic extra error documentation is not a new runtime behavior.

## Finite verification and review sequence

All six groups in [finite acceptance](../finite-acceptance-v1.md) are **proposed / NOT_RUN**. They are a useful decomposition of one slice, not six implementation approvals.

| Future group | Required evidence / mapped criteria |
| --- | --- |
| V1-DB | T01; V1-04/05/07–12,14: real PostgreSQL keyset, canonical codec, bounded SQL and representative plan; restore/re-trash and immutable materialization |
| V1-HTTP | T02; V1-01/03–07/09/13/14/17: actual JSON, invalid input before business read, no writes, operational/completion failure and cache behavior |
| V1-SEC | T03 and source-applicable unchanged security controls; V1-02/13: persisted sessions, both PUBLIC origins, MVC-spy denial and eight-route/dispatch lock |
| V1-API | T04; V1-01/03/05–07/16: generated graph and route manifest reconciled with actual HTTP specimens |
| V1-ARCH | `ApplicationArchitectureTests`; V1-18: module/package boundary, no schema/POM/descriptor/security drift |
| V1-REG | `VaultMutationConcurrencyTest`, `VaultMetadataIntegrationTest`, selected Search exclusion; V1-14/15; source-qualified reuse only |

The later executable must use existing `scripts/run-bounded-command.py`, **normal 300 seconds per command**, fresh non-clobber stdout/stderr/receipt/XML paths, and sequential focused selectors. A timeout/nonzero result remains evidence; split finite selectors if justified, without silent timeout escalation. No test-method totals are promised. V1-19 requires the final file/evidence map, mandatory Antigravity self-review, then independent Codex final slice review. Initial implementation is not remediation; only a valid later active contract initializes its own maximum three correction/re-review rounds. No automatic takeover or use of predecessor budgets. As a maintenance slice, V1 does not create a RAG phase-final/milestone gate.

## Coverage ledger

`PASS` below means the **preparation obligation** was reviewed, not that future code or runtime acceptance passed. Unimplemented runtime cases are NOT_VERIFIED and become mandatory later gates after Owner ratification.

| Dimension | Status and evidence / limits |
| --- | --- |
| 1. Owner intent/scope | PASS; explicit review approval, one browse capability, execution withheld; product tradeoffs below remain Owner decisions |
| 2. Traceability | PASS; V1-01–19 map to six finite groups plus qualifications above |
| 3. Architecture/frozen baseline | PASS; ADR-0016, matrix, DBML/migrations; exact additive Phase 1/13 exception still required |
| 4. Ownership/public boundaries | PASS; Vault owns query/application/web; existing named entry/view; no reverse dependency |
| 5. Folder/package structure | PASS; entire 59-file Vault inventory, 17 descriptors, four absent NEW paths in existing packages |
| 6. Shared API/exceptions | PASS; root envelope reused, existing local advice inspected, no new shared transport module |
| 7. SOLID/cohesion | PASS; repository/service/codec/view/web roles are concrete; no speculative interface hierarchy |
| 8. DRY/KISS/immutability | PASS; reuse item view, separate explicit web DTO, copied lists, no generic paging framework |
| 9. Patterns | PASS; existing repository/application facade sufficient; no new strategy/event/worker system |
| 10. Domain/lifecycle | PASS; deleted-only membership, weak consistency and unchanged trash/restore; runtime NOT_VERIFIED |
| 11. HTTP/OpenAPI | PASS; typed envelope/params/refs and inventory gates; actual generation NOT_VERIFIED |
| 12. Validation | PASS; bounded canonical UTF-8/Base64url/version/ID codec and limit cases; new parser NOT_IMPLEMENTED |
| 13. Authorization/privacy | PASS; exact-eight matcher/fallback source and required persisted-origin denial proof; runtime NOT_VERIFIED |
| 14. Secrets/cache/egress | PASS; no source hydration/provider work, cursor redaction/no-store obligations; headers NOT_VERIFIED |
| 15. Transactions/concurrency | PASS; one read query/materialized page, committed inter-page witnesses; no snapshot/lock upgrade |
| 16. Failure/operations | PASS; safe whole-response failure and watchdog/cleanup gates; no job/recovery subsystem introduced |
| 17. Database | PASS; table/index/entity compatibility and owner query seam; generated SQL/plan NOT_VERIFIED |
| 18. Complexity/resources | PASS; page output bounded by 100, query-fetch 101; scan cost remains unmeasured |
| 19. Retrieval/recommendation quality | N/A; no retrieval/ranking/recommendation capability |
| 20. Document/OCR fidelity | N/A; no document parser, source file, OCR or multilingual extraction |
| 21. Security testing | PASS; adversarial input, no-handler denial, unchanged dispatch and no-write witnesses specified |
| 22. Test quality | PASS; PostgreSQL/MockMvc, lifecycle and committed-sequence fixture constraints inspected; tests NOT_RUN |
| 23. Performance evaluation | PASS for bounded SQL/plan gate; latency/throughput NOT_VERIFIED; no invented SLA or benchmark platform |
| 24. Evaluation science | N/A; no model/corpus/labels/judge; deterministic synthetic list cases suffice |
| 25. Dependencies/lifecycle | N/A to dependency changes; candidate uses existing stack and permits no dependency/lockfile changes |
| 26. Build/static/tooling | PASS for future focused/architecture/watchdog plan; build and IDE diagnostics NOT_VERIFIED, no IDE-clean claim |
| 27. Logging/observability | PASS; canonical safe error factories/class-only unexpected logging; no raw cursor/content logs or new telemetry |
| 28. Documentation/governance | PASS; seven input docs, authority, links and directly affected references reviewed; Docs Sync below |
| 29. Hygiene/delivery | PASS; entry tracked diffs empty, no collision/gitkeep, review/status writes only; no stage/publish |

## Findings, remaining Owner decisions and stop conditions

**No CRITICAL/HIGH/MEDIUM or LOW defect is opened by this review.** The carry-forward qualifications are part of the requested map/feasibility review, not evidence that an unimplemented runtime feature failed. Existing unrelated stale harness notices remain deferred; no new remediation round follows.

Resolve the following **together before executable handoff**, using this review and candidate contract/map as the concrete decision docket:

1. Ratify PRIVATE-only identity metadata **exactly `id,entryType,deletedAt`**, accepting absence of recognizable titles, with existing old Vault responses unchanged.
2. Ratify **`id DESC` / weak cross-request consistency**, default 25 and range 1–100, explicit `items,nextCursor`/`meta:null`, and the proposed bounded canonical `v1|<positive-id>` unpadded Base64url cursor (maximum 128 characters), including the validation/cache/SQL-proof qualifications above. A chronological or titled alternative requires revised preparation.
3. Grant the **exact nine production/four test** additive frozen Phase 1/13 exception, if desired; no root OpenAPI/security/shared advice/entity/schema/dependency changes. Confirm use of the normal existing `main` checkout or explicitly choose a separately authorized branch/worktree exception; no such exception is presumed. Publish the reviewed preparation before invoking `$codex-create-handoff`.

STOP for an incompatible Owner choice, material baseline drift, another active handoff, a required extra file/module/schema/security capability, an unacceptable measured plan needing an unapproved change, or exhausted future remediation budget. There is no authority to implement around these conditions, create ACTIVE now, or reopen paused Frontend/RAG work.

## Docs Sync Check and preserved evidence

- **UPDATED:** this new independent review plus current disposition/next-action overlays only in `README.md` and `review-workflow-and-status.md`. SHA-256 entry/exit comparison confirms that the other five submitted documents, including the Owner approval record, remain unchanged proposals/authority evidence. Final scoped checks resolve **28/28 local links**, find zero trailing-whitespace files, and confirm no tracked source/ACTIVE/harness delta. No routine draft repair or historical review rewriting was performed.
- **ALREADY_CONSISTENT:** actual local/remote baseline, current ACTIVE NO_ACTIVE_HANDOFF, RO-B published 19-field PUBLIC/18-field PRIVATE boundary, denied PUBLIC Film POST, exact eight PUBLIC business GETs, source blob inventory and all **21 original local input links** at entry. Current touched links are rechecked after the status/review writes. Future runtime statements are labelled not run.
- **DEFERRED_OUT_OF_SCOPE:** root/current-policy/roadmap/maintenance-index/ACTIVE navigation update to V1; ChatGPT owns any separately requested publication/authorization sync. Historical notices in `backend/AGENTS.md`, `docs/AGENTS.md`, `.agents/rules/agent-handoff-workflow.md`, `.agents/rules/backend-phase-13-rest-api.md`, the backend-implementer's old B1/B2 bullet and the REST skill's historical auth/default wording do not supersede current Owner policy or source. No general harness cleanup is included.
- **PRESERVED:** RO-B independently accepted/Owner-published/CLOSED, **114 distinct accepted cases = 25 reviewer fresh + 89 qualified reused**, DPROB-L01/L02 LOW backlog, remediation **2/3 STOPPED**, no takeover; RO-A **1/3 STOPPED** and earlier DP-A/B1/W1 statuses/counters/backlogs. Prior independent reviews, failed evidence and logs remain untouched. Frontend H1-F/FR-13 remains paused/blocked, P17 frozen, P18 closed, S19-2 suspended, provider flags OFF/unset.
- **NOT_VERIFIED:** V1 runtime behavior, generated SQL/OpenAPI, real data/plan/latency, full suite, IDE diagnostics, production deployment and real Owner-data suitability. No runtime PASS count or new implementation acceptance is asserted.

One attempted read-only here-string inventory command was rejected by the native PreToolUse hook for ambiguous quoting and did not execute. Simple native PowerShell/Git reads completed the relevant inventory/link checks; no hook/configuration change, override or retry of that rejected command was used. One migration-glob read also failed under Windows path parsing and was replaced with a directory-scoped `rg --glob '*.sql'` read. Neither event consumed a remediation round or produced runtime evidence.

**Suggested preparation commit after Owner ratification:** `docs(backend): prepare private vault trash browsing`.

**Next step: Owner | ratify the V1 contract and exact source/test exception, then commit/push reviewed preparation | BF26-V1-RECYCLE-BROWSE + this review and candidate map | explicit decisions/publication before Codex $codex-create-handoff; ACTIVE stays NO_ACTIVE_HANDOFF | V1 remediation NOT_STARTED / 0 consumed; takeover NOT_AUTHORIZED.**
