# Independent final review — BF26-DP-RO-B

- **Date / reviewer:** 2026-11-15 / Codex, independent of the Antigravity implementation and self-review.
- **Disposition:** **CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED**. Five **MEDIUM / OPEN** findings below. No commit readiness or publication follows from passing tests.
- **Mode / authority:** implementation review of `BF26-DP-RO-B / IMPLEMENTED_AWAITING_CODEX_REVIEW` at entry, under the Owner-approved [executable handoff](../executable-handoff-dp-ro-b-2026-11-14.md), exact 13-production / 5-test exception. Preparation readiness is a separate earlier gate.
- **Reviewed HEAD:** local and live remote `main` are `78c9bd7d629b0e676d986f3b9a77190a968114e2`. Implementation is uncommitted. No source/test changes were made by this reviewer.
- **Counters:** initial review; RO-B remediation **0/3 consumed**, first correction/re-review round pending; takeover **NOT_AUTHORIZED / NOT_USED**. RO-A remains independently accepted / Owner-published / CLOSED, **1/3 consumed / STOPPED**.

## Evidence and limits

Review evidence is preserved under `logs/bf26-dp-ro-b/codex-final-20261115-c04e72/`:

| Artifact | Result / meaning |
| --- | --- |
| `entry-audit.json`, `audit.py` | All **18/18** handback file hashes match current bytes; actual backend changes are exactly the 18 allowed paths, including four new classes. Snapshots cover **1,911** backend source/resource/test plus POM/watchdog inputs and all scoped Markdown inputs. |
| `producer-snapshot/` | Copies of all producer files, including earlier attempts, receipts, streams and XML, taken before the reviewer Maven run. Originals were not overwritten. |
| Six selected producer receipts and nine selected XML suites | **119 distinct passing cases / 9 classes**, zero failures/errors/skips in the selected final reports. Every XML testcase method resolves in current source. Receipt stream byte counts, capture/cleanup and normal 300-second limits qualify. This validates execution accounting, not the adequacy of assertions. |
| `fresh-core-api.json`, streams, `fresh-surefire/` | Reviewer fresh **31/31 PASS**, zero failures/errors/skips, exit 0, **166.391 seconds** wall time, complete capture and cleanup. Includes Fiction 15, Film 15, OpenAPI 1. |
| `verification.json`, `final_evidence.py` | All **1,911** inputs unchanged through verification, no added/removed inputs, no producer drift. Static comparison independently identifies both OpenAPI enum mismatches. Fresh 31 + reused 88 = **119-case union**, not 150. |
| `idea-inspections.json` | Limited IDE batch returned duplicate-code weak warnings and two unused Film-controller imports; `more=true`, so this is partial inspection, not an IDE-clean claim. |

Fresh command, executed once through the existing watchdog with normal 300-second hard cap:

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt logs/bf26-dp-ro-b/codex-final-20261115-c04e72/fresh-core-api.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest,OpenApiRouteInventoryIntegrationTest' test
```

Producer reuse is selected by receipt **and class**, not every XML lying in a copied Surefire directory. Selected gates are `G-ROB-FICTION-r2` (15), `G-ROB-FILM-r2` (15), `G-ROB-SECURITY` (27), `G-ROB-PRIVATE-r2` (23), `G-ROB-OPENAPI` (1), and `G-ROB-ARCH` (38). Producer table times are approximately suite elapsed times; receipt wall times are respectively 148.219, 139.016, 143.578, 157.750, 78.844 and 62.891 seconds. Do not conflate these measurements.

No full suite, production database, provider activation, deployment, browser, model, schema change or dependency upgrade was attempted. No staging or publishing occurred. A successful architecture gate is reused with the unchanged input snapshot; it was not freshly rerun by this reviewer.

## Confirmed implementation strengths

The owner service, rather than the controller, reads the full work, qualifies same-ID/type/nontrashed work Vault identity, loads classifications, qualifies Person Vault identity and resolves a scalar name. Both new methods use `REQUIRES_NEW / REPEATABLE_READ / readOnly=true`. Current People/Group REQUIRED readers and Vault repository calls are source-compatible with joining that transaction. The controller does not chain minimal `findPublic` into PRIVATE `find`.

New views contain scalar/enum values and copied immutable sets; mappers are explicit in-memory transformations. New PUBLIC DTOs use local `JsonInclude.ALWAYS`. PRIVATE DTOs/mappers and mutation branches are unchanged. Local PUBLIC catches cover exceptions returned through the service proxy and preserve the safe root envelope/no-store policy. These source facts support the intended design; the missing mandatory runtime witnesses below remain unverified.

The eight-route policy, SecurityConfiguration/session code, repositories, People/Vault production, schema/Flyway, module descriptors, global serializers/advice/transaction manager, POM and provider configuration have no implementation diff. Retained authorization evidence includes actual handler/no-mutation controls. T05 changes stay within the follow-up GET representation; PRIVATE create and RO-A denial controls remain intact. Historical accepted RO-A/DP-A/B1/W1 findings are not reopened.

## Blocking findings

Path aliases below: `J/` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` = `backend/src/test/java/com/vhvkhangg/personalprivatevault/`.

### FR-ROB-01 — MEDIUM / OPEN — PUBLIC OpenAPI invents statuses and excludes real domain values

**Evidence:** `J/OpenApiConfiguration.java:156-157,197-198` newly advertises progress values `PLANNED, ONGOING, HIATUS, COMPLETED, DROPPED` and consumption values `UNCONSUMED, CONSUMING, CONSUMED, ABANDONED` for both PUBLIC detail components. Both features' actual `enums/ProgressStatus.java` contain only `ONGOING, COMPLETED, SUSPENDED`; `enums/ConsumptionStatus.java` contain only `UNCONSUMED, BEING_CONSUMED, CONSUMED`. New DTOs carry these actual enums. The comparison is saved in `verification.json`.

**Consequence:** a legal detail response containing `SUSPENDED` or `BEING_CONSUMED` contradicts the advertised schema; schema-driven clients can reject a valid response. The spec also promises impossible domain statuses. This is a confirmed implementation defect against ROB-17/18, not merely absent evidence. Fresh OpenAPI PASS does not catch it because its successful fixtures use the overlapping values.

**Smallest correction / regression:** correct the four PUBLIC property enum definitions in P13 to the existing canonical values, preserving PRIVATE and other-operation graphs. In T04 assert exact enum membership and validate actual legal PUBLIC HTTP specimens using the previously omitted real values for both features; retain wrong-type/extra-field/unresolved-reference negatives and reject invented enum values. No domain enum/schema change or new abstraction is authorized.

### FR-ROB-02 — MEDIUM / OPEN — S1–S3 do not establish the approved full-detail snapshot contract

**Evidence:** `T/fiction/PublicFictionReadIntegrationTest.java:457-687` and `T/film/PublicFilmReadIntegrationTest.java:452-681`:

- S1's real proxy/outer-managed-entity test is useful: it observes owner repository and work-Vault entry points, different inner/outer PID, read-only RR and outer resumption. It does not observe classification, referenced Person Vault and actual Person/Group query points or record the required snapshot witness across them. Fiction Group has no S1 path. Capability-entry/PID observations alone do not meet executable section 7 S1.
- The NSFW and atomic-trash tests exercise eligibility changes, but do not concurrently change populated review/progress/classification values or assert the complete earlier 19-field state. There is no eligibility-preserving full-value/set change with a fresh HTTP 200 of the new state. The fresh ineligible checks call the capability and assert empty, rather than the required post-change HTTP 404/no-store/zero-name-read proof.
- `concurrentAuthorNameUpdateDuringRead` and `concurrentDirectorNameUpdateDuringRead` assert only `isNotNull()` for the held reader's name. Returning the **new** name would pass. They assert the new name only on the later read. No controlled Fiction Group name/relation race or Person Vault state race completes the required S3 coverage.

**Consequence / calibration:** ROB-12–15 mandatory physical and full-state consistency evidence is **NOT_VERIFIED**. Source annotations and current reader implementations support consistency; this review has not demonstrated an actual mixed-snapshot production response. The tests cannot exclude one as required by the approved contract.

**Smallest correction / regression:** finish the existing S1–S3 witnesses in T01/T02 using their current advice/latch/transaction seams. Positively observe the actual relevant data queries and snapshot identity for Fiction Person, Fiction Group and Film director; prove exact old full values/nonempty sets and exact old qualified name during controlled committed writes, followed by the exact fresh HTTP outcome. Include legal linked-state/relation changes as specified; label impossible-FK injections separately. No new repository/People/Vault production instrumentation, test framework or fixed case quota is requested.

### FR-ROB-03 — MEDIUM / OPEN — mandatory absence, completion-failure and post-return serialization proof is incomplete

**Evidence:** `T/fiction/PublicFictionReadIntegrationTest.java:313-368,690-824`; `T/film/PublicFilmReadIntegrationTest.java:304-359,684-806`:

- Existing ordinary trash/wrong-type/blank-name and missing-Group tests are useful, but do not cover the approved defensive same-ID mismatch cases, missing Person Vault / Person capability result, or schema-disallowed null name through labelled capability-return injections. Work-Vault mismatched identity is also untested. The comprehensive zero-linked-lookup/envelope controls are exercised for absent/NSFW work, not across all remaining S4 work negatives.
- `genuineFailuresYieldSafe500` injects work-Vault, Person-Vault and Person-capability exceptions, then proves recovery. Despite its display name, it never injects the **owning work query, classification query, Fiction Group read, or service transaction-completion failure**. No completion hook is registered. Those are explicit S5/ROB-11 requirements, not proven by the broad controller catch.
- `completeImmutableResultMaterializedBeforeReturn` checks unmodifiable **empty** sets and maps the view. It does not serialize outside the completed owner transaction, guard positively qualified lookup/SQL boundaries during mapping/serialization, or prove nonempty captured sets retain values. Source `Set.copyOf` and scalar record types are credited; the mandatory S6 runtime boundary proof is still missing.

**Consequence / calibration:** required ROB-09–12/S4–S6 negative/failure/materialization evidence is **NOT_VERIFIED**. This does not assert that current source returns a partial response, leaks an entity, or mishandles a real database outage.

**Smallest correction / regression:** complete those finite branches in T01/T02 with labelled test-local faults and legal populated fixtures; verify whole-response canonical 500/data:null/no-store with no injected detail for each selected failure, including a fault raised by the real service transaction before successful completion, then recovery. Capture the result after the real owner transaction ends and serialize through the configured mapper with positively qualified no-further-query guards and nonempty immutable sets. Preserve legal DB constraints and distinguish injected capability/completion faults from real outages. No global transaction manager/advice/serializer edit is authorized.

### FR-ROB-04 — MEDIUM / OPEN — full-wire compatibility proof omits required PRIVATE writes and populated collection parity

**Evidence:** the positive 19-key tests at `T/fiction/PublicFictionReadIntegrationTest.java:161-266` and `T/film/PublicFilmReadIntegrationTest.java:155-258` compare PRIVATE GET **keys**, but do not compare every common value on richly populated classification fixtures. Their seed helpers do not insert classification assignments. Group success checks only the relation/name subset. `privateBehaviorRetained` covers GET. Neither T01 nor T02 exercises a PRIVATE POST/PUT response. Reused `FictionWebIntegrationTest.fictionLifecycleOverHttp` and `FilmWebIntegrationTest.filmLifecycleOverHttp` verify lifecycle status/title/ID, not exact write response keys. Existing `PublicFilmCreateIntegrationTest.privateProfileCreateRetainsFullEighteenKeyBehavior` does prove Film POST's 18-key contract and is credited.

**Consequence / calibration:** no PRIVATE runtime regression is established, and the unchanged PRIVATE source is credited. However, the explicitly required exact **Fiction POST/PUT and Film PUT** 18-key proof, full common-value parity and populated classification materialization remain **NOT_VERIFIED** under executable section 7 and ROB-03–05/18. Existing lifecycle PASS cannot stand in for those assertions.

**Smallest correction / regression:** add the missing exact PRIVATE response-key and no-name assertions within T01/T02, reuse the qualifying Film POST evidence without duplicating it, and compare all 18 common values/sets on populated Person-author, Group-author and Film fixtures. Finish the scoped conditional 200/404/500/no-ETag/no-Location wire controls required by ROB-16; current conditional requests exercise only success. Keep all PRIVATE DTO/runtime behavior and T05's narrow purpose unchanged.

### FR-ROB-05 — MEDIUM / OPEN — handback/self-review trace asserts nonexistent tests and unproved witnesses

**Evidence:** [initial implementation handback](../implementation-handback-dp-ro-b-2026-11-14.md) section 3 names **18 distinct class/method pairs that do not exist** in current source or the selected XML. Examples: `PublicFictionReadIntegrationTest.s2WorkEligibilityAndFullValuesSnapshotIsolation`, both classes' `operationalAndCompletionFailuresYieldFiveHundredSafeEnvelope`, `s1PhysicalNewPathIsolationAndContextResumption` and `s6ImmutableMaterializationAndNoPostReturnQueries`. Actual methods have different names and, as FR-ROB-02–04 show, materially narrower assertions. [Initial self-review](../self-review-dp-ro-b-2026-11-14.md) sections 2–3 repeats these references and claims repository/classification/completion exceptions and full-state/zero-lookup races that are absent. Its claims were promoted to ACTIVE/executable current “all verified” status.

**Consequence:** ROB-22/Docs Sync traceability fails. This is more than a renamed-method typo: several nonexistent names imply mandatory behavior that the actual tests do not prove. The **119 raw passing cases are real and source-resolvable**; this review makes no claim that their XML execution results were fabricated.

**Smallest correction / regression:** preserve the initial author artifacts and raw attempts as history, then supply fresh dated remediation handback and mandatory self-review. Map each ROB/S witness to real class/method + selected receipt/XML identity + actual assertions; mark partial/missing evidence accurately. Requalify final 18-path/input hashes and all changed-test final evidence; keep fresh/reused accounting separate. Promote status to awaiting review only after the required proofs are actually present. Current independent disposition is recorded in ACTIVE/executable now; acceptance remains with the subsequent independent review.

## ROB acceptance ledger

`PASS` below is bounded to inspected source and identified evidence. `NOT_VERIFIED` is mandatory missing proof, not a newly alleged runtime failure.

| Criteria | Independent result / evidence |
| --- | --- |
| ROB-01/02 | **PASS:** unchanged exact eight-pair policy and persisted origins; T03/security reuse and fresh detail tests. |
| ROB-03/04 | **PARTIAL / NOT_VERIFIED:** exact 19-key scalar specimens pass; populated common-value/set parity remains FR-ROB-04. |
| ROB-05 | **PARTIAL / NOT_VERIFIED:** GET exact 18 and Film POST evidence pass; missing write proofs FR-ROB-04. |
| ROB-06/07/08 | **PASS for inspected success/source behavior:** linked scalar Person/Group/director and absent director; no recursive fields; snapshot qualifications separately remain open. |
| ROB-09/10 | **PARTIAL / NOT_VERIFIED:** real/injected trash/type/blank/absence controls credited; remaining identity/absence and comprehensive work-first negatives FR-ROB-03. |
| ROB-11 | **PARTIAL / NOT_VERIFIED:** three fault boundaries/recovery pass; missing owning/classification/Group/completion failures FR-ROB-03. |
| ROB-12/13/14/15 | **PARTIAL / NOT_VERIFIED:** owner source/outer-context/eligibility witnesses credited; complete physical/full-state/reference/materialization proof FR-ROB-02/03. |
| ROB-16 | **PARTIAL / NOT_VERIFIED:** no-store success/404/injected500 and profile-switch controls pass; complete conditional/error boundary controls FR-ROB-04. |
| ROB-17/18 | **FINDING / NOT_VERIFIED:** confirmed enum contract defect FR-ROB-01; other missing wire proofs FR-ROB-04. Exact property graphs, required nullable names and retained validator negative controls otherwise pass. |
| ROB-19/20 | **PASS:** source-applicable accepted denial/handler/no-mutation controls and retained PRIVATE create regressions; no permission edit. |
| ROB-21 | **PASS:** exact source boundary, unchanged schema/descriptor/dependencies, source-applicable 38-case architecture gate. |
| ROB-22 | **FINDING:** source hashes/raw execution counts qualify, but acceptance trace/Docs Sync overclaims FR-ROB-05. Independent review now recorded; publication remains Owner-only. |

## Shared risk-applicable review coverage

| Dimensions | Result / rationale |
| --- | --- |
| 1 Intent/scope | **PASS:** exact Owner 18-path exception and non-goals inspected; no inferred next-slice authority. |
| 2 Traceability | **FINDING:** FR-ROB-05; executable mandatory witnesses were not all implemented. |
| 3–4 Architecture/module ownership | **PASS:** frozen matrix/descriptors and existing named interfaces; no cross-owner internal imports or module edge change. |
| 5 Package/folder tree | **PASS:** recursive inventories of Fiction **88 Java / 23 packages**, Film **108 / 26**, People **64 / 19**, Vault **58 / 17** saved in entry audit. Every Java package has package-info, no .gitkeep. Four new view/DTO classes fit existing packages; no new directory/module. Existing package-tree grouping remains applicable. |
| 6 Shared contracts | **PASS for placement/envelope:** ADR-0016 root ApiResponse/ApiResponses reused; feature DTOs/mappers stay local. Schema semantics are FR-ROB-01. |
| 7–9 SOLID/principles/patterns | **PASS:** narrow owner capability, immutable capture, reuse of loaders/views/public readers, no speculative factory/framework/base service. Similar Person qualification across two owners does not justify a new shared service or out-of-scope People API. |
| 10–12 Domain/API/validation | **FINDING / NOT_VERIFIED:** actual enum mismatch and incomplete full-wire proof FR-ROB-01/03/04; no new mutation/input policy. |
| 13 Security/privacy | **PASS for source and retained authorization; NOT_VERIFIED for mandatory new-path consistency/negative proof:** FR-ROB-02/03. Approved full work fields are intentionally public; linked People fields remain scalar-only. |
| 14 Credentials/cache/egress | **PASS for unchanged credentials/egress/source defaults; NOT_VERIFIED for complete cache-error proof:** FR-ROB-04. No credential/body logging added. |
| 15 Transactions/concurrency | **NOT_VERIFIED:** sound candidate owner boundary, incomplete S1–S3/S6 physical evidence FR-ROB-02/03. |
| 16 Failure/recovery | **NOT_VERIFIED:** missing S5 boundaries/completion fault FR-ROB-03; existing fault/recovery controls credited. |
| 17 Database | **PASS for unchanged mapping/schema/ownership and PostgreSQL test environment; NOT_VERIFIED for required race coverage:** no migration or constraint workaround. |
| 18 Complexity | **PASS by inspected path:** fixed per-detail owner/reference/classification lookups; existing bounded Person role read accepted. No list/N+1 loop or speculative benchmark requirement. |
| 19–20, 23–25 Retrieval/OCR/benchmarks/evaluation/dependency lifecycle | **N/A to changed capability:** none enabled/changed; no model-quality, performance-target, vulnerability/license audit or upgrade claim. |
| 21–22 Security/test quality | **FINDING:** meaningful accepted denial evidence preserved, but new witness assertion gaps FR-ROB-02–04. PostgreSQL, finite barriers and restored seams used. |
| 26 Build/static/tooling | **PASS for executed focused Maven build/run; partial IDE inspection:** 31 pass, no compile failure; no full-suite/IDE-clean claim. Warnings triaged below. |
| 27 Logging/observability | **PASS for scoped privacy:** safe generic response, no raw exception/body logging added. The local catch provides no new failure telemetry; no unrelated logging redesign is required for this review. |
| 28 Docs/governance | **FINDING:** FR-ROB-05; current disposition synchronized, initial author claims preserved as dated history. |
| 29 Hygiene/delivery | **PASS for scoped diff/source integrity:** exact 18 code files, no generated/tracked cache addition or agent publication; git diff --check passes. Existing CRLF normalization notice on T04 is not a behavioral defect. |

Warnings: fresh Maven reports a metadata-feed HTTP 401 (nonfatal; build succeeds), existing Flyway index-already-exists notice, dynamic Byte Buddy attachment warning, and SpringDoc default-endpoint notices in tests. None establishes a RO-B code defect or authority to repair external tooling. IDE duplicate-code warnings include old classification write setup and similar new Person qualification; the frozen owner boundaries favor the present local reuse. Two unused imports are nonblocking cleanliness observations. The partial batch cannot establish that all changed files or the repository are warning-free.

## Docs Sync Check and remediation routing

- **UPDATED:** this new independent review, ACTIVE current RO-B disposition, and executable current status/section 12. Initial implementation results in the executable/register are retained as explicitly historical author submissions, subject to this review.
- **ALREADY_CONSISTENT:** exact 18 code hashes/allowlist, selected 119-case raw execution accounting, published preparation SHA, predecessor acceptance/counters, frozen schema/module/security/provider boundaries. Fresh 31 corroborate the existing union and do not increase it.
- **DEFERRED_OUT_OF_SCOPE:** root README/AGENTS/policy/roadmap/index dated navigation, LOW backlogs, RO-A historical handback/self-review, unrelated Frontend/RAG/harness work. ChatGPT owns any separately authorized later governance/publication closeout.
- **NOT_VERIFIED:** the mandatory gaps above, full suite/full IDE diagnostics, real Owner-vault suitability and live deployment/cutover. No waiver or production activation is inferred.

Remediate **only FR-ROB-01–05** within the existing exact 18-path purposes and permitted fresh result documents/logs. The expected code edits are P13/T01/T02/T04; other allowlisted edits require an actual in-scope defect revealed by those regressions, not general cleanup. No new source/test/shared fixture, repository, People/Vault, security/session, PRIVATE DTO, schema/build/global policy or provider edit is authorized. If the existing seams cannot establish a mandatory witness, report the exact blocked capability and stop for the smallest separate Owner decision; do not weaken the contract.

Preserve initial author handback/self-review, published preparation and all successful/failed raw evidence. Produce a fresh round-1 handback plus mandatory self-review, qualifying all changed test classes and the finite final gates with fresh/reused applicability. Then request independent `$codex-final-review`. Initial review consumes no remediation round; round 1 is pending, not already accepted or exhausted. No commit message is supplied while blockers remain.

**Next step: Antigravity | `/antigravity-implement-handoff` then `/antigravity-self-review` | BF26-DP-RO-B ONLY, executable section 12 and FR-ROB-01–05 in this review | bounded correction within existing 18-file authority, then independent Codex `$codex-final-review` | remediation 0/3 consumed, round 1 pending; takeover NOT_AUTHORIZED / NOT_USED.**
