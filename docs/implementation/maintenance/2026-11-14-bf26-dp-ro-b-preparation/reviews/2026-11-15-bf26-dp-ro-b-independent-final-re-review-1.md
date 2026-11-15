# Independent final re-review 1 — BF26-DP-RO-B

- **Date / reviewer / mode:** 2026-11-15 / Codex / SLICE implementation re-review, independent of Antigravity remediation/self-review.
- **Disposition:** **CHANGES_REQUESTED / NOT_INDEPENDENTLY_ACCEPTED**. **FR-ROB-01 CLOSED**; **FR-ROB-02–05 MEDIUM / OPEN**, narrowed below. No new finding or acceptance requirement is introduced.
- **Scope:** Round 1 correction of the five findings in the [initial independent final review](2026-11-15-bf26-dp-ro-b-independent-final-review.md), under the same Owner-approved [18-path executable](../executable-handoff-dp-ro-b-2026-11-14.md). ACTIVE was IMPLEMENTED_AWAITING_CODEX_REVIEW at entry. Preparation readiness and implementer CLOSED labels are not independent acceptance.
- **Baseline:** local HEAD/origin-main at `78c9bd7d629b0e676d986f3b9a77190a968114e2`; live remote main independently confirmed at that SHA. Delivery remains uncommitted. Reviewer made no production/test changes.
- **Counters:** RO-B **1/3 remediation consumed**, Round 1 independently reviewed with changes requested; Round 2 pending, two rounds remain. Takeover **NOT_AUTHORIZED / NOT_USED**. RO-A remains Owner-published/CLOSED, **1/3 consumed / STOPPED**; other predecessor counters unchanged.

## Verification and preserved evidence

Evidence directory: `logs/bf26-dp-ro-b/codex-rereview1-20261115-9b7c20/`.

| Evidence | Independent result |
| --- | --- |
| `entry-audit.json`, `delta.json` | All **18/18** submitted file hashes match. Exactly four code inputs changed since initial review: P13/T01/T02/T04. No added/removed input among **1,911** source/resource/test/POM/watchdog inputs. Total uncommitted code boundary remains exact 18 paths. Initial author handback/self-review and initial independent review hashes remain unchanged. |
| `producer-snapshot/` | All Round 1 producer artifacts, including the earlier attempt and raw receipts/streams/XML, copied before reviewer Maven execution. Originals preserved. |
| Selected six final gates / nine XML classes | **114 distinct passing cases**, zero failures/errors/skips; all XML methods resolve in current source. Receipt streams match byte counts; complete capture/cleanup, normal 300-second bounds, exit 0. No invalid class-qualified method reference remains in the Round 1 handback. Assertion adequacy is adjudicated separately below. |
| `fresh-core-api.json`, streams, `fresh-surefire/` | Reviewer fresh **26/26 PASS**, zero failures/errors/skips, exit 0, **92.172 seconds wall time**; Fiction 12 + Film 13 + OpenAPI 1. |
| `verification.json` | All 1,911 inputs unchanged through review execution; no producer evidence drift. Canonical/public enum sets now match. **26 fresh + 88 reused = 114 current cases**. Neither the prior 119 nor the new 26 is added to this total. |
| `idea-inspections.json` | Partial inspection of P13/T01/T02, `more=true`; T04 was not returned in the batch. No whole-project or warning-free claim. |

Fresh command, once, through the existing watchdog:

```powershell
python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt logs/bf26-dp-ro-b/codex-rereview1-20261115-9b7c20/fresh-core-api.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=PublicFictionReadIntegrationTest,PublicFilmReadIntegrationTest,OpenApiRouteInventoryIntegrationTest' test
```

Producer final gates in `logs/bf26-dp-ro-b/remediation-round1/`: FICTION 12, FILM 13, SECURITY 27, PRIVATE 23, OPENAPI 1, ARCH 38. Each XML is selected by gate **and class**, excluding stale collateral reports in copied directories. Receipt wall times are 61.594, 40.578, 62.609, 47.625, 36.078 and 27.062 seconds; the author's 201.45-second total describes approximate suite time, not total watchdog wall time.

No full suite, new runtime/provider activation, production database, dependency/schema change, staging or publishing. The unchanged authorization/private-create/architecture cases are source-qualified reuse, not freshly rerun reviewer gates. Historical initial evidence remains historical; changed/replaced detail tests account for the current 114-case total.

## Finding adjudication

`J/` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/`; `T/` = corresponding test package root.

| ID | Independent disposition | Verified progress / remaining blocker |
| --- | --- | --- |
| FR-ROB-01 | **CLOSED** | P13 has the canonical three progress and three consumption values for both features. T04 asserts exact generated enum membership and validates new legal SUSPENDED/BEING_CONSUMED specimens plus invented-value negatives. Fresh OpenAPI passes; existing actual HTTP specimens still reconcile with their graph. The original schema defect is repaired. |
| FR-ROB-02 | **MEDIUM / OPEN** | Exact old-name Person/Group/director races and Person Vault trash races now pass. S1 actual-query/snapshot evidence and S2 concurrent set/full-state proof remain incomplete. |
| FR-ROB-03 | **MEDIUM / OPEN** | Owning query, classification, work/Person Vault, Person/Group and real beforeCommit failure/recovery proof is now accepted. Remaining S4 identity/absence and S6 captured-set/query-guard proof below. |
| FR-ROB-04 | **MEDIUM / OPEN** | Exact PRIVATE POST/PUT shapes, populated Person/Film parity and conditional 200/404/500 controls now pass. Group full common-value parity remains absent. |
| FR-ROB-05 | **MEDIUM / OPEN** | Method identities and raw execution/hash accounting repaired. The new docs still overclaim absent assertions and misattribute PRIVATE write evidence. |

### FR-ROB-02 — finish S1 actual query/snapshot and S2 full-state witnesses

**Evidence:** `T/fiction/PublicFictionReadIntegrationTest.java:556-825`; `T/film/PublicFilmReadIntegrationTest.java:519-746`.

- S1 now advises classification repositories and samples reference capability entry points. However, variables named `pidAtPersonRepo` / `pidAtDirectorRepo` / `pidAtGroupRepo` are populated in `PersonOperations.find` / `CreatorGroupOperations.find` spy callbacks **before** `inv.callRealMethod()`, not at their real data queries. No snapshot identity/SQL witness is recorded across those actual reads. A reference operation starting a different transaction after this sample could evade that observation. This is the precise gap already prohibited by executable section 7 S1.
- Fiction Group's S1 branch records only work-repository, work-Vault and Group-capability entry PID. It does not record its classification queries, RR/read-only state, snapshot witness or an actual managed outer Group-work entity/resumption. The Person branch's stronger outer-context checks are credited, not presumed to execute for the Group branch.
- S2 writers update only the work row (NSFW/title/review, or title/review/currentProgressText). **No classification assignment changes**, work-Vault state change or progress-status change occur in these races. Held set assertions use `isNotEmpty()`, not exact earlier set membership, and only a subset of scalar fields is compared. The fresh eligibility-preserving response checks three scalar values, not the required new full state/sets. A mixed old-work/new-classification result would still pass these assertions.

**Consequence / limit:** remaining ROB-12/13/15 physical/full-state proof is **NOT_VERIFIED**. No production mixed-snapshot response is demonstrated; owner transaction source remains unchanged and plausible. The source-supported design and now-exact S3 races are credited.

**Smallest correction:** within T01/T02, finish the existing S1 query-boundary instrumentation and snapshot witness for all three author/director paths, including Group outer-context proof. Extend the existing finite S2 barriers with legal committed populated set/state changes and exact earlier/new full-state expectations. Keep the already passing exact old-name S3 assertions; do not reopen them without new evidence. No new production instrumentation, framework, file or case-count quota is required.

### FR-ROB-03 — distinguish missing identity from mismatch and complete captured-set safeguards

**Evidence:** Fiction lines 368-552 and Film lines 350-515 contain work/Person negatives. Their case labelled “Mismatched work Vault entry” actually returns `Optional.empty()` (Fiction 429, Film 406). No returned VaultEntryView carries a **different ID**. Person-Vault missing-result and mismatched-ID faults are absent; null Person/Group name-return injections are absent. Existing same-ID wrong-type, legal trash, blank name, missing Person/Group and work-first zero-name controls are accepted. A missing value does not execute the same-ID rejection branch.

S5 at Fiction 951-1078 and Film 833-945 now includes the previously missing owning/classification/Group fault paths and registers a real transaction synchronization whose `beforeCommit` throws. Each selected failure gives safe 500/data:null/no-store, then clean recovery. **The S5 part of this finding is satisfied; do not require another failure mechanism.**

S6 at Fiction 1082-1116 and Film 949-986 now serializes configured DTOs after the real owner call and rejects mutations of nonempty sets. This is useful progress. Its set assertions still check only nonemptiness, and serialized content is checked only for the name. It resets Vault/People spies and asserts those `find` calls never occur, with no positively recorded pre-reset observation in this witness, no observation/guard for work/classification reads and no exact captured-set comparison through serialization. The comment that “all repository spies/interceptions” were reset is broader than the actual two/three capability resets. Source scalar records/Set.copyOf/pure mapping remain accepted; full approved S6 proof is not yet established.

**Smallest correction:** add clearly labelled inconsistent-ID/missing-reference/null-name capability fixtures specified by S4, preserving schema constraints and whole-response semantics. In the existing S6 cases, capture exact expected nonempty sets, positively qualify the selected lookup/SQL observations during the real acquisition, then prove no further scoped reads and unchanged captured values through outside-transaction mapping/serialization. Reuse current source and test seams. No global serializer/transaction-manager/People/Vault production edit is authorized.

### FR-ROB-04 — Group full-value parity is still missing

**Evidence:** `T/fiction/PublicFictionReadIntegrationTest.java:233-252` compares all 18 PRIVATE/ PUBLIC common values for the **Person** fixture. Lines 255-273 fetch the populated Group fixture only as PUBLIC and check keys, relation/name and nonempty sets. There is **no corresponding PRIVATE GET of this Group work and no all-common-field comparison**. Person and Film parity and both features' exact PRIVATE write response keys now pass and are accepted. Conditional success/404/500 tests now exercise the stated statuses/no-store and are credited; the initial broad missing-write/conditional criticism is resolved.

**Smallest correction:** reuse the populated Group fixture, request its PRIVATE detail, assert the 18-key/no-authorName contract and compare every common value/set with the PUBLIC result, as already done for Person. This is the existing executable section 7 wire requirement, not a new Group feature or PRIVATE runtime change. Existing fixed PRIVATE write/conditional cases need no new behavior.

### FR-ROB-05 — names now resolve, but the assertion trace still overstates source

**Evidence:** [Round 1 handback](../implementation-handback-dp-ro-b-round-1-2026-11-15.md) section 1 and [Round 1 self-review](../self-review-dp-ro-b-round-1-2026-11-15.md) section 2 claim all physical queries, exact full 19-field/set races and mismatched Vault IDs were tested. The source limitations above contradict those claims. Both ROB-05 tables attribute PRIVATE POST/PUT key assertions to `s2WorkEligibilityAndFullValuesSnapshotIsolation`; these assertions actually reside in `eligibleFictionHasExactlyNineteenKeysAndResolvedAuthor` / `eligibleFilmHasExactlyNineteenKeysAndResolvedDirector`. Both docs claim `verifyNoMoreInteractions`; current S6 uses selected `verify(..., never()).find(...)` calls. R1 also swaps the canonical T03/T05 file IDs: T03 is ProfileRouteAuthorizationIntegrationTest; T05 is PublicFilmCreateIntegrationTest. Full paths/hashes still qualify; that ID swap is bookkeeping, not an unauthorized source change.

**Consequence / calibration:** all current class-qualified method references resolve and **114 raw passing cases are real**. The remaining blocker is the semantic acceptance trace, not nonexistent execution. Implementer CLOSED labels do not adjudicate these independent findings. ACTIVE/executable also contain prior current-looking dispositions; the current state is synchronized with this re-review below.

**Smallest correction:** preserve initial and R1 author artifacts/receipts as history. After the bounded remaining corrections, supply fresh Round 2 handback/self-review with canonical file IDs and requirement → real method → actual assertions → selected receipt/XML identities. Distinguish repaired subparts from pending proof; do not claim absent instrumentation or self-close findings from a green total. No unrelated historical cleanup is required.

## Acceptance and shared review coverage

All conclusions from the initial review are reused where source/evidence is unchanged. Actual recursive inventories were refreshed: Fiction **88 Java / 23 packages**, Film **108 / 26**, People **64 / 19**, Vault **58 / 17**; no missing package-info or .gitkeep. Four original new DTO/view classes remain in existing packages. No package-tree baseline, shared envelope or module graph change is introduced.

| Coverage | Result / evidence and limit |
| --- | --- |
| ROB-01/02/06–08/19–21 | **PASS for inspected source and identified evidence:** exact eight-pair auth policy, linked-name successes, denied POST and handler/no-mutation safeguards, original frozen ownership/schema/architecture. S1 qualification proof is separately pending. |
| ROB-03–05/18 | **PARTIAL / NOT_VERIFIED:** PRIVATE writes and Person/Film populated parity repaired; Group parity FR-ROB-04 remains. |
| ROB-09/10/12/13/15 | **PARTIAL / NOT_VERIFIED:** S1/S2/S4/S6 remainders FR-ROB-02/03; credited passing subparts above. |
| ROB-11/14/16/17 | **PASS for the reviewed correction/source:** operational/completion recovery, exact old-name/Person-trash races, conditional statuses/no-store, canonical generated enum graphs. No claim of production deployment or universal cache audit. |
| ROB-22 | **FINDING:** hashes, raw counts, independent re-review and preservation qualify; acceptance assertion trace remains FR-ROB-05. |
| Dimensions 1, 3–9 | **PASS:** unchanged intent/authority, ADR-0016 root transport, module/named-interface boundaries, refreshed tree, simple owner service/immutable DTO design; no speculative abstractions. |
| Dimensions 10–12 | **PASS for repaired enum/source contracts; NOT_VERIFIED for remaining wire proof:** FR-ROB-04. No new input or mutation policy. |
| Dimensions 13–14 | **PASS for retained auth/privacy/credentials/provider/cache source; NOT_VERIFIED for full qualification evidence:** FR-ROB-02/03. Approved full work fields are intentional, scalar People-only restriction retained. |
| Dimensions 15, 17 | **NOT_VERIFIED for mandatory snapshot witnesses**, while unchanged PostgreSQL mappings/constraints and source ownership pass: FR-ROB-02/03. No migration or DB workaround. |
| Dimension 16 | **PASS:** newly complete selected operational/completion failure and recovery proof. |
| Dimension 18 | **PASS by source reuse:** bounded per-detail lookups, no list/N+1/loop/lock expansion or new performance target. |
| Dimensions 19–20, 23–25 | **N/A to this capability:** no retrieval/OCR/model/benchmark/dependency-lifecycle change or audit claim. |
| Dimensions 21–22 | **FINDING:** required assertion quality gaps FR-ROB-02–04, despite real PostgreSQL and finite barrier tests passing. |
| Dimension 26 | **PASS for executed focused Maven; partial IDE results:** compilation/tests pass; no full-suite or warning-free assertion. |
| Dimension 27 | **PASS by unchanged source:** safe generic error responses/no raw payload logging; no new telemetry requirement imposed. |
| Dimensions 2, 28 | **FINDING:** FR-ROB-05; current state synchronized, historical submissions preserved. |
| Dimension 29 | **PASS:** exact source boundary, unchanged historical hashes, no index change/publishing, git diff --check passes. |

Diagnostics triage: IDE raw/unchecked Swagger schema generics and diamond suggestions are not the repaired enum defect; no broad generic/schema refactor is warranted. SQL warnings state no IDE datasource is configured; PostgreSQL integration actually passed, so do not rewrite native SQL for those warnings. Unused imports/parameters are nonblocking hygiene. Executor try-with-resources suggestions are not proof of leaks: tested actors have finite waits and finally shutdownNow cleanup. Maven metadata-feed 401, Byte Buddy attach and SpringDoc/default-index notices remain nonfatal test/environment warnings. Partial IDE inspection cannot establish a clean repository.

## Docs Sync Check and exact next scope

- **UPDATED:** this new independent re-review, ACTIVE current disposition/counter routing and executable current disposition. The initial review plus initial/R1 author reports remain unchanged historical records.
- **ALREADY_CONSISTENT:** exact 18-path/hash boundary, four-file R1 delta, 114 current raw cases (26 fresh reviewer + 88 reused), canonical enum fix, retained source/security/schema/provider boundaries and predecessor counters.
- **DEFERRED_OUT_OF_SCOPE:** root AGENTS/README/policy/roadmap/index dated navigation, LOW backlog and unrelated Frontend/RAG/harness cleanup; ChatGPT owns separately authorized governance/publication closeout.
- **NOT_VERIFIED:** remaining mandatory proofs above, full suite/full IDE diagnostics, real Owner-vault suitability and live deployment/cutover. No contract waiver is inferred.

Correction is **only the remainders of FR-ROB-02–05**, expected in T01/T02 and fresh result documents/logs, within existing 18-path purposes. **FR-ROB-01 stays CLOSED** unless new evidence warrants reopening. Reuse accepted S3/S5/PRIVATE-write/conditional improvements; do not add new feature/framework, relax a proof, edit shared fixtures or extend frozen production authority. If an existing seam cannot prove a mandatory requirement, stop and identify the exact smallest separate Owner decision. No automatic takeover or agent publication.

**Next step: Antigravity | `/antigravity-implement-handoff` then `/antigravity-self-review` | BF26-DP-RO-B ONLY, FR-ROB-02–05 remainders in this review and executable current review section | bounded Round 2 correction, fresh handback/self-review, then independent Codex `$codex-final-review` | remediation 1/3 consumed, Round 2 pending; takeover NOT_AUTHORIZED / NOT_USED.**
