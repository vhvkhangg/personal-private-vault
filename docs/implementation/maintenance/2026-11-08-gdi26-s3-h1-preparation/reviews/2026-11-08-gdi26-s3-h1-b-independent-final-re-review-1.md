# Independent final re-review 1 — GDI26-S3-H1-B

- **Date / reviewer / mode:** 2026-11-08 / independent Codex / SLICE, Antigravity remediation round 1.
- **Status:** **CHANGES_REQUESTED**. FR-01–03 and FR-05–07 CLOSED; **FR-04 and FR-08 MEDIUM / OPEN / blocking**. One new LOW observation is nonblocking.
- **Baseline:** local HEAD, origin/main and fresh live refs/heads/main matched `b0cb5f7a27b8515af7324e6ef8998dad5c059416`. Entry has 46 unstaged modified/untracked paths; 25 changed since the preceding review checkpoint. The [initial final review](2026-11-08-gdi26-s3-h1-b-independent-final-review.md) is unchanged.
- **Authority:** [executable H1-B handoff](../executable-handoff-h1-b-2026-11-08.md), [Owner D26–D30](../owner-decision-addendum-v0.5.md), [current policy](../../../../workflow/current-owner-delivery-policy.md), inspected [remediation handback](../implementation-handback-h1-b-2026-11-08.md). Implementer resolution/self-review claims are input to this independent adjudication.
- **Counter:** H1-B Antigravity **1/3 consumed**, first correction/re-review completed but blocked. Next correction/re-review is **2/3**. Takeover NOT_AUTHORIZED; no transfer/reset of other slices' counters.
- **Provenance:** fresh local ignored [entry SHA-256 manifest](../../../../../logs/gdi26/h1-b/codex-rereview1-84fa5063e41d408c8b6de07e8e0bebe9/entry-source-manifest.json) identifies all 46 inspected entry paths. No production/test source was edited by the reviewer.

## User requirement and simplest-mechanism check

The source changes address the approved one-level Drive Explorer backend and preserve the internal module, root ApiResponse/ADR-0016, additive V27, credential authority and normal OFF configuration. Explicit root/child proofs, strict local JSON parsing and expected-state repository updates are proportionate corrections. No new framework, dependency, module or frontend work is warranted.

The original ten probes now all PASS on freshly compiled current classes. This supports six finding closures, but does not prove that other malformed image formats or all mandatory concurrency/recovery cases are covered. A new actual controlled-HTTP probe accepted an empty WEBP chunk as an image; the remaining finite evidence/documentation gaps are set out below.

Source shorthand: `D` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/drive`; `T` = `backend/src/test/java/com/vhvkhangg/personalprivatevault/drive`. Line references refer to this entry version. `L` = `logs/gdi26/h1-b/codex-rereview1-84fa5063e41d408c8b6de07e8e0bebe9/`.

## Finding adjudication

| Stable ID suffix (GDI26-S3-H1-B-) | Severity | Disposition | Independent basis |
| --- | --- | --- | --- |
| FR-01 | MEDIUM | CLOSED | Search now propagates 5xx/409 child-proof failures and validates supplied incompleteSearch type; original error probe PASS |
| FR-02 | HIGH | CLOSED | Explicit rootFile/childFile parent arguments, legacy root-only proof and selected-parent comparison; original wrong-parent root-list probe PASS and current parent-browse tests PASS |
| FR-03 | MEDIUM | CLOSED | Local strict duplicate detection and complete input consumption; duplicate/trailing controller probes PASS with zero creates |
| FR-04 | MEDIUM | **OPEN / blocking** | PNG signature-only and idle probes improve, but WEBP still accepts empty image data; decoder pre-allocation guard remains unestablished |
| FR-05 | MEDIUM | CLOSED | Dedicated 5s progress watchdog, cancellation/finally cleanup; existing six-second controlled body-gap probe now PASS |
| FR-06 | MEDIUM | CLOSED | Strict shape/integer/overflow handling preserves optional absence; three original malformed quota member probes and fresh quota tests PASS |
| FR-07 | MEDIUM | CLOSED | Database expected-status/epoch predicates in transitionFrom/transitionFromAny; real PostgreSQL stale/terminal overwrite denials PASS |
| FR-08 | MEDIUM | **OPEN / blocking** | Several supplied tests do not exercise the mandatory races/continuation claimed; current docs still contain material contradictions |
| FR-L01 | LOW | OPEN / nonblocking backlog | Date-filter test hardcodes 2026-11-08 while fake metadata uses wall-clock Instant.now; source-based determinism observation |

### FR-04 — WEBP validation still accepts a container with no image

**Observed fact:** `D/internal/files/http/DriveFileHttpClient.java:370–380` checks RIFF magic/size and the first chunk name only. The submitted `T/fixture/FakeGoogleThumbnailServer.java:defaultWebp` returns 20 bytes: RIFF size 12, WEBP, a VP8 chunk with declared payload length zero. `CodexH1BR1ImageProbe.emptyWebpPayloadMustFallBack` served these bytes through the actual controlled HTTP transport under real PostgreSQL/current file authority. The service returned success; the required fallback assertion failed. `L/combined-probes.json` records exit 1, 10 PASS and this sole FAIL among 11 cases. No live provider or browser exploit is claimed.

**Requirement:** the approved thumbnail contract and B-06 require verifiable image content, reject empty/truncated/suspicious formats, and permit fallback for an unsupported verification path. A RIFF/chunk header does not meet this requirement.

**Additional source limit:** PNG/JPEG branches call ImageIO.read before checking the 2048 dimension limit (`DriveFileHttpClient.java:354–369`). A bounded pre-decode allocation check is not present at that call site. Two separate 64 MiB JVM probes (a malformed 69-byte PNG header and a valid 388,871-byte grayscale PNG declaring 20,000×20,000 pixels) both returned false without an escaping OutOfMemoryError. These results are preserved; **no JVM crash or demonstrated allocation failure is asserted**. They do not establish a pre-allocation resource bound.

**Smallest correction:** reject WEBP unless the existing runtime can actually verify the bounded complete image; unsupported WEBP may simply fall back. Do not add a codec framework. Apply dimensions/pixel/allocation bounds before decoding PNG/JPEG, retaining the 1 MiB encoded limit. Use real valid supported images and empty/truncated/chunk-length mismatch negatives; do not describe the empty WEBP fixture as decodable. Re-run the new controlled-HTTP negative plus source-applicable positive/cap/idle tests.

### FR-08 — the remaining mandatory evidence and adjacent docs are not complete

Credit the new real seeded V26→V27 migration, persisted pre-H1 field preservation, PostgreSQL state guards, eight-nonterminal admission check, seeded RESERVED first-dispatch and DISPATCHED/404 GET-only recovery, explicit parent browse/relocation, NFC/NFD search, corpora=user/spaces=drive request assertions, quota late-epoch denial, and actual private PNG thumbnail route. These are improvements and need not be rebuilt into a larger test framework.

The following approved obligations remain **NOT_VERIFIED**; green test titles/aggregate counts do not substitute for them:

1. **B-02 race boundaries:** `T/DriveH1BAuthorityIntegrationTest.java:285–327` seeds seven nonterminal rows and synchronizes only task start. It does not hold the first admitted action nonterminal at a DB/provider barrier. If it completes before the other admission, two 201 results are legal; expecting exactly 201/503 depends on scheduling. There is still no different-key concurrent proof at 255→256 retained records. Add deterministic bounded gates at the actual admission/provider seam, assert committed counts and provider mutation/ID counters at both 8 and 256 boundaries, and include existing-key recovery at capacity. Preserve the current root-row lock, not a new lock system.
2. **B-03/B-04 child races:** `childOperationEpochBarrierRejectsStaleDispatchAndPublication` at lines 400 onward creates a child but does not use its ID in an in-flight child operation. It changes a DB epoch and directly invokes generic authority closures/store.take. This confirms stale authority helpers, not the required child disconnect/reassignment barriers before dispatch and final publication. Add bounded actual child-operation/provider gates and assert no unauthorized mutation/content/COMPLETED publication. Existing S1/S2 tests remain credited where their paths apply.
3. **B-05 continuation evidence:** `parentScopeBrowseWithRelocationAndEmptyContinuation` never supplies or consumes a next-page token; its empty result is a terminal relocated-item case. The six-child test proves 8/4 operation counts, but the approved later substring hit after an empty filtered page and full mixed root/child-file eligible corpus across pages are still not demonstrated. Add those finite cases and actual fake-server dispatch counts, including the already-required poison/incomplete/error/current-scope checks. Do not add buffering, a larger provider page or a ninth call.
4. **B-06 transfer/authority evidence:** genuine private PNG success and the six-second idle negative now pass. The new image path still lacks the required progressing-stream total-8s, abort/cancellation and slot/epoch-change transport/publication negatives. Reuse unchanged URI/size/302 controls and shared authority tests where applicable; add only the missing image-path proofs.
5. **Provenance:** the handback still contains no source/schema/test SHA-256 manifest or reference to one. It can cite the review's existing manifest/next exact-source checkpoint rather than duplicate a new hash inventory. Tie receipt claims to the reviewed final source and distinguish implementer reruns of reviewer-authored probes from independent acceptance.

**Material current documentation conflicts:**

- `docs/database/google-drive-files.dbml:57` says child records are pruned after 30 days. The approved H1-B contract forbids automatic child-authority row deletion, and the actual child store does not prune. Correct the child note only; preserve the separate valid legacy upload-pruning rule.
- `docs/architecture/google-drive-files.md` still describes 243 S2-enabled and 248 H1-B-enabled as selectable current inventories; there is no separate H1-B flag. Label 243 historical; current files ON has 248 total/13 Drive, S1-only 239/4, OFF 235/0.
- `google-drive-file-operations.md` claims remote child ownership is revalidated before and after upload. Source does a fresh child proof before upload, then validates returned file affinity and local current-authority guards. Document that actual guarantee and non-atomic provider limitation; do not impose an extra remote snapshot guarantee or change request budgets.
- Handback claims `verifyCompleteImage`/complete WEBP decodability, while the actual method is `verifyImageSignature` with the defect in FR-04. Adjacent ADR/architecture/package text repeats complete-decoder claims. Align prose with the corrected actual supported/fallback behavior and mark missing cases NOT_VERIFIED.
- ACTIVE/executable/handback say all B-01–08 acceptance is established. Independent acceptance is still blocked; review overlays correct the current execution status, while Antigravity must correct its authored evidence/docs inside the existing allowlist.

**To close:** deliver only these residual approved proofs and accurate current documentation, with actual receipts and source identity. No new feature, live Google run, UI, native-hook audit, broad suite or Owner waiver is required. Six CLOSED findings must not be reopened or refactored without a new concrete regression.

### FR-L01 — date-filter test depends on the execution date (LOW)

**Source fact:** `T/DriveH1BSearchIntegrationTest.java:100–103` calls a fixed 2026-11-08 range “today” and expects three newly created folders; `T/fixture/FakeGoogleDriveServer.java:94,181` assigns their timestamps with Instant.now. **Inference:** on a different local calendar day the expected three-item assertion can fail even with correct production filtering. No changed system clock or future-date test run was performed. At the next authorized LOW cleanup, give the fixture explicit fixed timestamps/date bounds. This observation does not consume an additional round or block delivery by itself.

## Acceptance matrix

| Gate | Result | Current evidence / remaining limit |
| --- | --- | --- |
| B-01 | PASS (scoped) | Fresh schema/JPA and actual seeded V26→V27 migration/field preservation plus state-update checks pass |
| B-02 | NOT_VERIFIED | Static capacity/recovery and same-key tests pass; deterministic different-key 8/256 contention proof still missing |
| B-03 | NOT_VERIFIED | Explicit parent proofs and static denials pass; actual child disconnect/reassignment publication barriers missing |
| B-04 | NOT_VERIFIED | Child bytes/parent conflicts and legacy root proof credited; child-race obligations pending. 50 MiB case passed isolated after a full-batch error |
| B-05 | NOT_VERIFIED | Original false completeness fixed; 8/4 budget and current parent/filter tests pass; finite continuation/mixed-corpus/server-count proof incomplete |
| B-06 | FAIL / NOT_VERIFIED | Empty WEBP accepted; total deadline/abort/slot transport cases pending. Complete PNG/private route and idle denial pass |
| B-07 | PASS | Shape/absence/raw quota projection and actual in-flight credential-epoch denial verified in fresh 47-test batch |
| B-08 | PASS (boundary checks) | Fresh 248/13 OpenAPI and Modulith in H1B web tests; source-qualified prior S1 flags and real Portability no-export remain applicable |

## Verification and evidence reuse

**Fresh environment:** same installed Maven 3.9.16 / Oracle Java 25 / Python runner on Windows; no dependency, environment, global PATH, trust or normal feature-flag changes.

All potentially long commands used `python scripts/run-bounded-command.py --profile normal --timeout-seconds 300` with fresh receipts in L. Maven suffix: `-- 'C:/Users/ADMIN/.m2/wrapper/dists/apache-maven-3.9.16-bin/5grr65jo27hi51sujmtcldfovl/apache-maven-3.9.16/bin/mvn.cmd' -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<selector>' test`.

| Fresh selector / command | Receipt | Actual result |
| --- | --- | --- |
| DriveH1B*Test | h1b-new-tests.json | exit 0; 47 tests, zero failures/errors/skips; 101.78s |
| DriveS2*Test | s2-regression.json | exit 1; 148 tests, 147 PASS/1 ERROR, zero skips; 291.56s |
| DriveS2StreamingIntegrationTest#exact50MiBUsesResumableWireAndDownloadStreamsTheExactOriginal | streaming-recheck.json | exit 0; isolated one-case repeat PASS, 111.53s |
| Java @L/probes/runtime-combined.args | combined-probes.json | exit 1; 11 cases, ten original probes PASS, new empty-WEBP probe FAIL; 88.69s |
| Java @L/probes/runtime-allocation.args | allocation-probe.json | exit 0; valid PNG control and oversized malformed-header rejection; 1.94s |
| Java @L/probes/runtime-compressed.args | compressed-allocation-probe.json | exit 0; 388,871-byte large-dimension valid PNG rejected under -Xmx64m; 2.70s |

Java executable is `C:/Program Files/Java/jdk-25/bin/java.exe`; each table command has the same normal/300s runner prefix and its named receipt. `probe-compile.json`, `compressed-probe-compile.json` and `combined-probe-compile.json` preserve successful javac invocations using fresh owned argument/class directories. `prepare-compressed.json` records bounded generation of a synthetic streaming-compressed fixture; no full-size pixel raster was allocated by the generator. All receipts have complete captures and completed cleanup; none hit its watchdog deadline.

The first S2 run's exact-50 MiB download ended at 46,989,312 of 52,428,800 expected bytes (`IOException: closed`). The isolated repeat passed unchanged. No production cause is established; concurrent reviewer compilation/fixture preparation during the first suite and the fixture's shortened deadlines are possible timing factors, not confirmed explanations. Both receipts are retained. **This review does not describe that original batch as green or count the repeat as an extra distinct suite case.**

**Reused independent evidence, not fresh reruns:** the initial Codex `logs/gdi26/h1-b/codex-final-9141cf2333914927ade5023af0880844/s1-regression.json` (62 PASS) and `boundaries.json` (53 PASS), linked in the [initial review](2026-11-08-gdi26-s3-h1-b-independent-final-review.md). The S1 sources/tests/fixtures and files-disabled route/configuration branches did not change. Portability adapter/test, V27/exclusion, shared transport, module edges/package membership and architecture test source did not change since that checkpoint; new H1B web tests freshly verify 248/13 routes and ApplicationModules.verify. Changed JPQL is exercised by current PostgreSQL contexts. These 115 cases are source-qualified reuse, not a new full four-batch 310-test PASS claim.

Implementer receipts at `logs/gdi26/h1-b/{h1b-new-tests,s2-regression,s1-regression,boundaries}.json` and `logs/h1b-probe-final-{run,additional-run}.json` were checked: PASS/exit 0, complete capture/cleanup. They are credited as implementer runs, not double-counted as independent acceptance.

Fresh probe sources are copies of the original ten unchanged assertions plus one new service/real controlled-HTTP negative; they live only under ignored L/probes and were freshly compiled. JSON tests use actual controller callables/servlet fixtures, not full wire requests. No live provider or user data was used. The original failed probe logs and initial review remain unchanged.

Build diagnostics contain Mockito/ByteBuddy dynamic-agent/CDS and Springdoc development-endpoint warnings. Defaults/public matchers were not changed; no warning suppression was added. No IDE-wide or vulnerability/static-analysis-clean claim is made.

## Risk-applicable shared coverage

| Dimensions | Result | Evidence / limits |
| --- | --- | --- |
| 1–2 intent/traceability; 10 business; 12 validation; 16 recovery; 18 bounds; 21–22 adversarial/test quality; 28 docs | FINDING | Residual FR-04/08; LOW date determinism observation. Original ten failures resolved, but format/evidence gaps remain |
| 3 architecture consistency | FINDING (docs) | Accepted schema/module/ADR scope retained; current DBML/route/proxy prose still conflicts, FR-08 |
| 4–9 ownership/tree/shared contract/SOLID/principles/patterns | PASS (scoped) | Recursive 70-file relevant inventory and all 25 changed-since-review paths inspected; no new package/module/framework, root ApiResponse/ADR-0016 retained; guarded updates and explicit proofs are cohesive |
| 11 wire; 14 credentials; 17 persistence | PASS (scoped) | Strict parser and real PG migration/constraints; exact-origin-before-bearer; no accepted debt or later activation inferred |
| 13 authority; 15 transactions/concurrency | NOT_VERIFIED | Current parent/state guards inspected and helper tests pass; required actual child-race/admission proofs remain incomplete under FR-08 |
| 26 build/tooling; 27 diagnostics; 29 hygiene | PASS (scoped execution/hygiene) | Watchdog/capture/cleanup, source hashes, unchanged prior review, source-only inspection/default checks and local links. This is not a statement that every acceptance test passed |
| 19–20 retrieval/document fidelity; 23–25 benchmark/evaluation/dependency change | N/A | No RAG/OCR/model/benchmark deliverable or dependency change; bounded image verification belongs to 12/18, not a new imaging/evaluation platform |

## Scope-bounded Docs Sync Check

- **UPDATED:** this new re-review; ACTIVE and executable result/status/review/counter/next-action only. Current execution now records CHANGES_REQUESTED, six closures, FR-04/08 OPEN and round 1/3.
- **ALREADY_CONSISTENT:** Owner-published preparation at b0cb5f7; D23–D30, visual approval/hash, feature exclusions and OFF defaults; no Auth/POM/shared-security/agent/hook/runner/frontend or historical migration changes. Prior initial review/checkpoint and failed evidence preserved.
- **NOT_VERIFIED / correction required:** precisely the residual FR-08 proof/provenance items and named current adjacent docs. The handback is preserved as inspected implementer input; no silent correction of its all-PASS claims by the reviewer.
- **DEFERRED_OUT_OF_SCOPE:** new FR-L01 LOW and prior PH-L02/H0 LOW/INFO/global-index/configuration issues keep their existing future cleanup/disposition route. No historical cleanup or new approval gate is added.
- **NOT_VERIFIED (operational holds):** live Google/lh3/TLS coverage, actual key/spool ACL/deployment, H1-AUTH and H1-F remain outside this acceptance.
- **Publication:** no acceptance, commit message, staging or publishing. No new Owner decision is needed for the two residual already-authorized corrections.

## Scope and next action

All 46 entry paths and the recursively inventoried Drive/file/web/test structure were checked directly; no Graphify graph/frontend/root package exists. Hash comparison identifies 25 remediation-changed paths; 21 entry paths including the initial independent review remain unchanged. Review changes are restricted to this new artifact and the two current status documents; ignored probe/log artifacts are separate from delivery.

Return only FR-04/FR-08 residual work to Antigravity within the existing allowlist. After meaningful focused verification, mandatory self-review and saved/reread review-ready status, use a separate independent Codex re-review. Do not restart CLOSED findings, start later slices, or infer takeover/Owner publication.

**Next step:** Antigravity | `/antigravity-implement-handoff`, then `/antigravity-self-review`, then separate Codex `$codex-final-review` | GDI26-S3-H1-B, [executable handoff](../executable-handoff-h1-b-2026-11-08.md), residual FR-04/08 in this review | CHANGES_REQUESTED; verified complete/bounded image fallback and remaining approved evidence/docs before independent re-review | H1-B Antigravity 1/3 consumed; next correction/re-review 2/3; takeover NOT_AUTHORIZED.
