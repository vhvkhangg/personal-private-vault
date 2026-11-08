# Independent final review — GDI26-S3-H1-B

- **Date / reviewer:** 2026-11-08 / independent Codex, implementation review (`SLICE`).
- **Status:** **CHANGES_REQUESTED**. Eight blocking findings OPEN (one HIGH, seven MEDIUM); mandatory evidence remains NOT_VERIFIED. No commit message or acceptance.
- **Baseline:** local HEAD, origin/main and fresh `git ls-remote origin refs/heads/main` all matched `b0cb5f7a27b8515af7324e6ef8998dad5c059416`. Implementation is an uncommitted delta: 45 entry paths, nothing staged.
- **Authority:** [executable H1-B handoff](../executable-handoff-h1-b-2026-11-08.md), [D23–D25](../owner-decisions-and-open-questions.md), [D26–D30](../owner-decision-addendum-v0.5.md), [published preparation acceptance](2026-11-08-gdi26-s3-h1-independent-pre-handoff-re-review-2.md), [Owner policy](../../../../workflow/current-owner-delivery-policy.md).
- **Implementer evidence:** [handback](../implementation-handback-h1-b-2026-11-08.md); declared self-review is implementation input, not independent acceptance.
- **Counter:** H1-B Antigravity **0/3 consumed**. This initial review consumes zero; the next implementation correction/re-review is round 1/3. Takeover NOT_AUTHORIZED. Other slices' counters/exceptions are unchanged.
- **Source provenance:** entry SHA-256 hashes for all 45 paths are preserved in the local ignored [entry manifest](../../../../../logs/gdi26/h1-b/codex-final-9141cf2333914927ade5023af0880844/entry-source-manifest.json). Reviewer changed no production/test source.

## User requirement and simplest-mechanism check

The approved work is the bounded Drive backend extension: one app-owned child level, immutable parent-affine original files, complete bounded search, truthful quota and an optional safe thumbnail. The implementation reuses S1 lease/fences and S2 resources/proofs, adds only V27 and the allowed Portability exclusion, and introduces no new module, dependency or general framework.

The four required Maven batches genuinely pass **295/295** tests. That proves the selected assertions, not all B-01–08 obligations. Independent adversarial probes instead produced **1 PASS / 9 FAIL across 10 cases**. A complete PNG control passed. The failures below are current-source behavior; no live Google exploit, production readiness or unsupported deployment model is inferred.

Source shorthand: `D` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/drive`; `T` = `backend/src/test/java/com/vhvkhangg/personalprivatevault/drive`. Line references describe the inspected entry version.

## Finding register

| ID | Severity | Blocking | Behavior / smallest correction | Disposition |
| --- | --- | --- | --- | --- |
| GDI26-S3-H1-B-FR-01 | MEDIUM | YES | Mandatory child-proof failure becomes exhaustive successful search; distinguish unavailable proof from definitive ineligibility | OPEN |
| GDI26-S3-H1-B-FR-02 | HIGH | YES | Shared parent inference weakens legacy root-only authority; require explicit parent scope before projecting metadata | OPEN |
| GDI26-S3-H1-B-FR-03 | MEDIUM | YES | Duplicate fields and trailing JSON create folders; use a strict module-local parser before mutation | OPEN |
| GDI26-S3-H1-B-FR-04 | MEDIUM | YES | Image signatures alone admit truncated thumbnails; verify bounded complete image structure and use valid fixtures | OPEN |
| GDI26-S3-H1-B-FR-05 | MEDIUM | YES | Thumbnail body idle cap is absent; enforce the approved 5s cap without changing legacy limits | OPEN |
| GDI26-S3-H1-B-FR-06 | MEDIUM | YES | Malformed present quota values become absent limits; reject invalid shapes/empty values | OPEN |
| GDI26-S3-H1-B-FR-07 | MEDIUM | YES | Folder state updates do not compare the expected database state; add narrow conditional transitions | OPEN |
| GDI26-S3-H1-B-FR-08 | MEDIUM | YES | Mandatory finite proofs are missing and current handback/architecture claims contradict the contract/source | OPEN |

### FR-01 — search cannot certify exhaustion after a failed proof

**Fact / evidence:** `D/internal/files/application/DriveItemSearchService.java:185–195,231–249` catches every `DriveFileException` from child GET/proof and omits the candidate. Lines 254–272 then return `hasMore=false` when the provider supplies no token. Probe `failedChildReadMustNotBecomeExhaustiveSearch` injected a child-proof 503 at the HTTP-client seam while using actual PostgreSQL and controlled root/list HTTP; the service returned success rather than an error. This injection does not claim a real remote 503 was observed.

**Impact / approved requirement:** B-05 expressly forbids false terminal success on unavailable mandatory verification. A matching eligible child can silently disappear and the caller is told the search is complete.

**Smallest repair / proof to close:** distinguish definitive malformed/foreign/moved candidates from timeout/rate-limit/provider/stale-authority failures; propagate an honest safe error/incomplete result. Check malformed supplied `incompleteSearch` types rather than coercing them to false at line 156. Cover last-page folder and child-file proof failures and valid definitive denials, preserving five candidates, at most eight dispatches and no retained candidate buffer.

### FR-02 — candidate metadata cannot choose its own authority scope

**Fact / evidence:** `D/internal/files/application/DriveFileProof.java:47–61` derives the expected parent from the candidate itself when no upload intent is supplied. `DriveFileService.java:33–46` uses that branch in the unchanged root-only list operation without proving a child registry/current placement or asserting the root parent. Probe `legacyRootListMustRejectChildMetadata` created a marked child/file, moved the child outside the PPV root in the controlled provider, and supplied that child-file metadata in the root-list page. The old root-only service returned the file. This is a controlled stale/adversarial provider-response test; it does not establish cross-account access or a live Google exploit.

**Source observation:** the new `DriveItemSearchService.java:148–152,165–249` puts a selected parent in the provider query but does not independently compare every projected candidate with that selected parent. Its child/root branches can admit otherwise eligible items from another parent if the response violates the query. That branch was inspected, not separately runtime-probed.

**Impact / requirement:** B-03/B-04 require fresh current-parent authority and preservation of legacy root-only behavior. A provider query is not independent item proof.

**Smallest repair / proof to close:** make the expected root/selected parent explicit. Preserve the root-only branch and require completed child registry plus current child proof only in child-accepting paths. Add root-list wrong-parent/moved-child fixtures and parent=root/A versus root/B response-scope negatives. Reuse existing proofs rather than a new authority framework.

### FR-03 — strict folder JSON validation is incomplete

**Fact / evidence:** `D/internal/web/controller/DriveFileController.java:46–47,185–207` bounds the body and checks tree field names, but its default mapper collapses duplicate keys and does not reject trailing documents. Actual controller callables with synthetic JWT/current operation, PostgreSQL and controlled provider returned **201** for both `{"name":"First","name":"Second"}` and `{"name":"First"}{"name":"Second"}`. The expected 400 assertions failed. These were controller/servlet-fixture calls, not full wire requests; the later zero-create assertions were not reached.

**Impact / requirement:** the executable contract's folder-body rule explicitly requires duplicate/malformed rejection before mutation.

**Smallest repair / proof to close:** configure strict duplicate detection and complete-input consumption locally to this bounded request parser. Preserve the 4 KiB/name-only/normalization rules and avoid shared MVC configuration changes. Prove 400 with zero ledger/provider mutation for duplicate, unknown, trailing, truncated and malformed inputs through the actual private HTTP path.

### FR-04 — a magic prefix is not a verified thumbnail

**Fact / evidence:** `D/internal/files/http/DriveFileHttpClient.java:295–334` accepts PNG/JPEG/WEBP by MIME and a short prefix only. The controlled HTTP probe `truncatedPngMustFallBack` returned only the eight PNG magic bytes; the service accepted them. A separate complete 1×1 PNG control succeeded. `T/fixture/FakeGoogleThumbnailServer.java:89–104` also calls incomplete signature-only payloads valid images; the submitted positive thumbnail test therefore does not establish valid-image handling.

**Impact / requirement:** B-06 requires bounded signature/decoder sanity and rejection of truncated/compressed/mismatched content.

**Smallest repair / proof to close:** validate complete supported image structure with bounded allocation/decoder checks; reject unsupported verification paths through fallback. Explicitly reject unsupported content encoding. Use real tiny valid images and valid-prefix truncated/malformed/length-mismatch bodies, plus the existing 1 MiB cap. No codec framework, rendering stack, speculative dimension policy or transcoding is requested.

### FR-05 — the approved thumbnail idle limit is not enforced

**Fact / evidence:** `DriveFileHttpClient.java:264–286` keeps the operation in metadata mode. `D/internal/files/http/DriveFileResources.java:16–17,87–95,121` only applies idle checking in body mode and the legacy idle value is 15s. The image's total deadline is 8s, but there is no 5s idle deadline. A controlled HTTP server sent eight bytes, paused the body for six seconds, then completed a valid PNG. The service accepted **69 bytes after 6,068ms**.

**Impact / requirement:** B-06 and the thumbnail policy separately require connect ≤2s, total ≤8s and idle ≤5s.

**Smallest repair / proof to close:** add a thumbnail-specific shorter idle deadline using owned operation cancellation/timers. Preserve legacy phase limits and two admissions. Verify a >5s body gap below the 8s total fails/falls back, while valid progress succeeds; include cancellation/current-slot change and cleanup evidence on actual transport.

### FR-06 — malformed quota values are treated as missing

**Fact / evidence:** `D/internal/files/application/DriveExplorerService.java:49–58` calls `asText().strip()` and returns null for empty text. The actual service/fake-about HTTP probes for supplied `limit={}`, `limit=[]` and `limit=""` all returned success rather than the expected 502. Existing tests reject a malformed whole storageQuota object, not these malformed members.

**Impact / requirement:** B-07 distinguishes a valid absent/pooled limit from a failed/malformed read. These cases incorrectly become a successful unknown-limit projection.

**Smallest repair / proof to close:** accept only the approved optional nonnegative int64 representation; preserve legitimate absence/null semantics, reject present invalid shapes, empty/noninteger/overflow/negative values, and retain final current-authority verification. Add focused member-value and late-slot/epoch cases without inventing percentages or fallback capacity.

### FR-07 — folder transitions do not guard expected database state

**Source fact / evidence:** `D/internal/files/persistence/DriveChildFolderIntentRepository.java:58–70` filters updates by key and immutable intent epoch only. Unlike `reserve` at lines 44–55, `transition` has no expected-status condition. Store calls at `DriveChildFolderIntentStore.java:105–133` check some in-memory statuses, but `complete` accepts any non-COMPLETED claim and `unknown` ignores the update result. The database update cannot reject a stale claim merely because the durable state has advanced under the same immutable epoch.

**Requirement / limit:** the executable schema contract explicitly requires expected-state guarded application updates. This is a missing source guard, not a claim of an observed duplicate create or authorization for multi-process deployment.

**Smallest repair / proof to close:** add permitted predecessor-state conditions to the existing update operations, validate outcomes, and preserve immutable identity/name/fingerprint/provider/parent values. Prove valid transitions and rejected stale/terminal-state overwrites against PostgreSQL, including RESERVED first dispatch and GET-only recovery after DISPATCHED. No new state-machine framework or historical S2 rewrite.

### FR-08 — complete the finite mandatory proof and correct the handback

**Evidence gaps (NOT_VERIFIED, not runtime failures):**

- **B-01/B-04:** the single persistence test proves real fresh-schema constraints. It does not run the mandatory seeded V26→V27 upgrade or compare fixed pre-H1 root intent/fingerprint values before/after migration. Add that finite upgrade/compatibility proof and application-write guard checks; do not modify V26.
- **B-02:** the two-task same-key test has no synchronization barrier. The capacity test seeds 256 COMPLETED rows; it does not establish the separate eight-nonterminal limit or different-key contention at either boundary. No new-ledger RESERVED/restart or DISPATCHED/restart/404 GET-only proof is supplied. Use deterministic PostgreSQL/provider barriers and counters, including replay at capacity.
- **B-03/B-04:** static moved/trashed/parent-affinity denials and actual child HTTP transfer passes are credited. Child-specific disconnect/reassignment/epoch barriers at dispatch and final publication remain missing; inherited S1/S2 tests are reusable only where their exercised source path applies.
- **B-05:** the six-child test genuinely records **8 then 4** operation dispatches; accent/type/date and one-use cursor assertions are credited. Complete the approved later-hit/full mixed eligible corpus, empty continuation, NFC/NFD/date-edge, parent-scope/failure/poison/relocation and actual fake-server accounting checks. Required `corpora=user&spaces=drive` are absent from `DriveFileHttpClient.java:116–120`; add them. The changed fake server's parent-query parsing takes the first quoted term, which is the property key for the new compound query; repair this limited fixture emulation before using it as parent-browse proof.
- **B-06/B-07:** close FR-04–06 and supply the already-required actual private thumbnail HTTP success/current-authority/abort/time controls and quota late-epoch evidence. The submitted web test lists thumbnail in its title but never calls that route. Reuse existing source-applicable URI/size/redirect tests.

**Material documentation conflicts:** handback lines 15–38/130–133 claim all requirements verified, a broad Google-host/IP policy, 2 MiB/2048×2048 bounds and no credential forwarding. Its additive ADR-0027 paragraph, operations guide and package inventory repeat these claims. The approved/source policy is the exact `https://lh3.googleusercontent.com:443` origin, **1 MiB**, and bearer only after validation; no such wildcard/IP/dimension implementation is present or required. Do not implement the incorrect prose. The operations guide also documents nonexistent `scope=root|child` modes instead of `all|parent`, and the architecture's 243-operation count is a historical S2 baseline, not a separately selectable current mode.

At entry, executable top status claimed all cases PASS while its bottom implementation/result/review still said NOT_STARTED. Reviewer status overlays correct that conflict; Antigravity must still correct its authored handback/adjacent docs, provide the required source/schema/test hash manifest and map each mandatory case to actual evidence. Missing proof remains NOT_VERIFIED until exercised, irrespective of aggregate green tests. No additional feature, suite, safety-hook audit, live-provider test or UI gate is added.

## Acceptance matrix

| Requirement | Result | Credited evidence / acceptance limit |
| --- | --- | --- |
| B-01 | NOT_VERIFIED | Fresh Flyway/JPA/PG constraints pass; seeded upgrade and write invariants pending FR-07/08 |
| B-02 | NOT_VERIFIED | Same-key and lost-response assertions pass; 256/8 barrier/restart obligations pending FR-07/08 |
| B-03 | FAIL / NOT_VERIFIED | Static denials credited; root-list proof regression FR-02 and child fencing proof gaps FR-08 |
| B-04 | FAIL / NOT_VERIFIED | Real child HTTP bytes and 148 S2 regressions pass; legacy proof FR-02 and seeded compatibility missing |
| B-05 | FAIL | Six-child 8/4 budget, filters and cursor tests pass; false completeness FR-01, scoped proof FR-02, finite gaps FR-08 |
| B-06 | FAIL | Exact-origin negatives/size/302 tests and complete PNG control pass; invalid-image/idle FR-04/05, route/abort proof pending |
| B-07 | FAIL / NOT_VERIFIED | Real and absent quota fields pass; invalid members FR-06 and late-epoch proof missing |
| B-08 | PASS (boundary checks) | 248 OpenAPI operations including 13 Drive IDs; S1-only/OFF/configuration regressions, actual Portability no-export and architecture checks pass. Does not override endpoint defects above |

## Verification

Fresh local receipts live under the ignored directory `logs/gdi26/h1-b/codex-final-9141cf2333914927ade5023af0880844/` (`L` below). Existing implementer receipts remain unchanged and are not counted again as independent cases.

**Environment:** Windows 11 amd64, Oracle Java 25 build 25+37-LTS-3491, Maven 3.9.16 from its already-installed wrapper cache, Python 3.12 runner. Maven was absent from the initial PATH lookup; that attempt did not launch tests. Using the existing absolute executable required no installation or global configuration/trust/PATH change.

Exact common invocation, each selector run separately/sequentially:

```powershell
& python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <L>/<receipt>.json -- 'C:/Users/ADMIN/.m2/wrapper/dists/apache-maven-3.9.16-bin/5grr65jo27hi51sujmtcldfovl/apache-maven-3.9.16/bin/mvn.cmd' -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<selector>' test
```

| Selector | Receipt | Fresh result | Watchdog wall time |
| --- | --- | --- | --- |
| DriveH1B*Test | h1b-new-tests.json | exit 0, 32 tests, 0 failure/error/skip | 108.80s |
| DriveS2*Test | s2-regression.json | exit 0, 148 tests, 0 failure/error/skip | 165.28s |
| DriveApiInventoryIntegrationTest,DriveConfigurationIntegrationTest,DriveWebIntegrationTest,DriveOAuthSecurityTest,DriveGrantRevocationIntegrationTest | s1-regression.json | exit 0, 62 tests, 0 failure/error/skip | 102.34s |
| PortabilityIntegrationTest,ApplicationArchitectureTests | boundaries.json | exit 0, 53 tests, 0 failure/error/skip | 73.50s |

Independent probes live only in `L/probes/CodexH1BReviewProbes.java` and `CodexH1BAdditionalProbes.java`. They use the submitted Spring/PostgreSQL fixtures and controlled HTTP; request parsing uses the actual controller callable with servlet fixtures. They do not alter submitted tests. Compile/runtime argument files preserve the existing Surefire classpath plus the already-cached JUnit launcher and owned output directory.

```powershell
& python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <L>/independent-probes.json -- 'C:/Program Files/Java/jdk-25/bin/java.exe' '@<L>/probes/runtime.args'
& python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <L>/additional-probes.json -- 'C:/Program Files/Java/jdk-25/bin/java.exe' '@<L>/probes/runtime-additional.args'
```

- `probe-compile.json` and `additional-probe-compile.json`: exit 0, 2.03s each.
- `independent-probes.json`: exit **1**, 56.12s; six cases, one complete-PNG PASS, five failed correctness assertions (FR-01–05).
- `additional-probes.json`: exit **1**, 20.05s; four failed assertions (three malformed quota member cases and one trailing-JSON case). These are reproduced defects, not tooling failures.
- All eight runner receipts have capture_complete/cleanup_complete=true, cleanup=not_needed. No timeout, detached worker or forced tree cleanup was needed.
- Mockito/ByteBuddy dynamic-agent and CDS/bootstrap warnings were observed. No new dependency/configuration workaround or blanket warning suppression was introduced. An IDE-wide/static-analyzer inspection was not run; no warning-free claim is made.
- Source review, frozen-file/default checks, recursive inventory, local link checks and `git diff --check` are part of the saved review checkpoint. No unrelated full backend/RAG/browser suite or live provider call was run.

## Risk-applicable review coverage

All 29 shared dimensions were considered; these are grouped results, not new implementation gates.

| Dimensions | Result | Evidence / rationale |
| --- | --- | --- |
| 1–3 intent/traceability/architecture; 8 invariants; 10–13 business/API/validation/authority | FINDING | Approved H1-B boundaries checked; FR-01–04/06–08, especially weakened root proof and inaccurate adjacent docs |
| 15–16 transactions/recovery; 18 resource complexity; 21–22 adversarial/testing quality; 28 documentation | FINDING | Root-row admission and bounded pages are retained; state guard, idle, finite missing proofs and false claims FR-05/07/08 |
| 17 database evolution | NOT_VERIFIED | V27/JPA/DBML and fresh PostgreSQL constraints inspected/passed; seeded V26 upgrade remains mandatory |
| 4–7 ownership/tree/shared contracts/SOLID; 9 patterns | PASS | Recursive relevant inventory, all entry delta reviewed; same packages/package-info, internal Drive ownership, no new module edge/generic framework. Root ApiResponse/ADR-0016 retained; explicit DTOs and permitted byte exceptions |
| 14 credential handling; 26 build/tooling; 27 safe diagnostics; 29 delivery hygiene | PASS (scoped) | Exact-origin-before-bearer dispatch, sanitized DTO/errors, actual four batches and receipts, no dependency/security/default changes, reviewer production/test hashes unchanged. Does not waive FR-02 or claim whole-repository security/static cleanliness |
| 19–20 retrieval/document fidelity; 23–25 benchmark/evaluation/dependency changes | N/A | No RAG/OCR/index/model/evaluation, benchmark deliverable or dependency change. Operation budgets are covered under 18/21; a fresh vulnerability/license audit is outside this delta |

## Scope-bounded Docs Sync Check

- **UPDATED:** this dated independent review; ACTIVE and executable handoff status/result/counter/review/next-action sections only. Both now route CHANGES_REQUESTED to Antigravity. The implementer-authored handback is preserved as inspected evidence, with its required correction in FR-08.
- **ALREADY_CONSISTENT:** preparation remains accepted and Owner-published at b0cb5f7; D23–D30/current search v0.6 and approved visual hash are not reopened. V27 is additive; V1/V26, module boundaries, normal OFF defaults and publication restrictions are unchanged. References in the new review/status edits resolve locally.
- **NOT_VERIFIED / correction required:** FR-08's exact handback/ADR/operations/package-inventory conflicts and finite acceptance gaps; live Google/lh3/TLS coverage, operational key/spool ACL/deployment, H1-AUTH and H1-F remain outside this acceptance.
- **DEFERRED_OUT_OF_SCOPE:** prior PH-L02 LOW summary wording, H0 FR-L01/PH-I01, old global banners and unrelated configuration exception retain their existing owners/dispositions. No historical review/log is rewritten.
- **Publication:** no H1-B delivery is accepted or published by this review. No Owner decision is needed to correct these already-approved behaviors, and no new waiver/architecture choice is requested.

## Scope and hygiene

The entry delta has 25 tracked modifications and 20 untracked files. Relevant production/test subtree inventory contains 70 files; the actual delta, packages, sibling boundaries, migration, affected DTO/error/configuration/resource paths, fixture changes, Portability and named adjacent docs were inspected. No frontend/root package/Graphify graph exists, so direct source navigation was used. No Authentication/POM/shared security/hook/agent/runner or historical migration edits occurred.

Reviewer-created probes/classes/arguments/receipts/manifest remain under ignored logs, separate from the delivery patch. Only the new review and two authorized status artifacts are reviewer changes. No agent staged, committed, pushed, tagged or created a PR. Preserve failed probe logs and implementer receipts for the next reviewer.

## Owner next action

Return this same H1-B slice to Antigravity for the eight bounded corrections and missing approved evidence. Do not publish or start H1-AUTH/H1-F. After implementation and mandatory self-review, persist/reread IMPLEMENTED_AWAITING_CODEX_REVIEW in ACTIVE, executable handoff and corrected handback; a separate independent Codex session re-reviews the actual corrected source. Initial review is not remediation round 1.

**Next step:** Antigravity | `/antigravity-implement-handoff`, then `/antigravity-self-review`, then separate Codex `$codex-final-review` | GDI26-S3-H1-B, [executable handoff](../executable-handoff-h1-b-2026-11-08.md) and FR-01–08 in this review | CHANGES_REQUESTED; preserve exact scope/defaults and supply truthful B-01–08 evidence before independent re-review | H1-B Antigravity 0/3 consumed; next correction/re-review 1/3; takeover NOT_AUTHORIZED.
