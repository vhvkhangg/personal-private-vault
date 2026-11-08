# Independent final re-review 2 — GDI26-S3-H1-B

- **Date / reviewer / mode:** 2026-11-08 / independent Codex / SLICE, Antigravity remediation round 2.
- **Status:** **CHANGES_REQUESTED**. **FR-04 MEDIUM / OPEN remains the only blocker**, now narrowed to malformed JPEG accepted despite decoder warning. FR-08 and FR-L01 CLOSED; the six earlier closures remain intact.
- **Baseline:** local HEAD, origin/main and fresh live refs/heads/main matched `b0cb5f7a27b8515af7324e6ef8998dad5c059416`. Entry: 47 unstaged modified/untracked paths; 13 changed since re-review 1, including only one production file. Both prior independent reviews are unchanged.
- **Authority:** [executable handoff](../executable-handoff-h1-b-2026-11-08.md), [Owner D26–D30](../owner-decision-addendum-v0.5.md), [Owner policy](../../../../workflow/current-owner-delivery-policy.md), [re-review 1](2026-11-08-gdi26-s3-h1-b-independent-final-re-review-1.md), inspected [second remediation handback](../implementation-handback-h1-b-2026-11-08.md).
- **Counter:** H1-B Antigravity **2/3 consumed**. Round 2 correction/re-review is complete but blocked. Next correction/re-review is **3/3**; takeover NOT_AUTHORIZED. No counter reset or transfer from another slice.
- **Current source provenance:** [entry SHA-256 manifest](../../../../../logs/gdi26/h1-b/codex-rereview2-0a6a9584608a4a05868e45d0588f3197/entry-source-manifest.json). The older manifest cited in the handback identifies that earlier baseline, not the newly changed source. This review and its saved checkpoint bind current evidence to current source. Reviewer edited no production/test source.

## User requirement and simplest-mechanism check

The remaining Owner-approved work is bounded image verification and the finite evidence/docs corrections in FR-08. The new PNG/JPEG pre-decode dimension checks and unsupported-WEBP fallback address the preceding defects without dependencies or a codec framework. Capacity gates, real child in-flight publication denial, search continuation and thumbnail time/cancellation tests now substantiate the previously missing paths.

Fresh **53 H1-B + 148 S2 tests PASS**, including the full S2 batch that had an intermittent error in re-review 1. All **11 existing reviewer probes PASS**. The extra malformed-JPEG check below independently exposes a remaining B-06 defect. Aggregate positives do not establish rejection of that content.

Source shorthand: `D` = `backend/src/main/java/com/vhvkhangg/personalprivatevault/drive`; `T` = `backend/src/test/java/com/vhvkhangg/personalprivatevault/drive`. `L` = `logs/gdi26/h1-b/codex-rereview2-0a6a9584608a4a05868e45d0588f3197/`. Line references describe the inspected entry version.

## Finding adjudication

| Stable suffix (GDI26-S3-H1-B-) | Severity | Disposition | Evidence / limit |
| --- | --- | --- | --- |
| FR-01 | MEDIUM | CLOSED | Source unchanged since accepted correction; original search-failure probe PASS again |
| FR-02 | HIGH | CLOSED | Explicit root/child proofs unchanged; original wrong-parent root-list denial and current parent tests PASS |
| FR-03 | MEDIUM | CLOSED | Strict local parser unchanged; duplicate/trailing probes PASS |
| FR-04 | MEDIUM | **OPEN / blocking** | WEBP and dimension repairs credited; JPEG with missing entropy/premature EOI returns private HTTP 200 |
| FR-05 | MEDIUM | CLOSED | Idle watchdog unchanged; six-second gap probe and added progressing-stream total-deadline/cancel cases PASS |
| FR-06 | MEDIUM | CLOSED | Strict quota parsing unchanged; original malformed member probes/current quota tests PASS |
| FR-07 | MEDIUM | CLOSED | Conditional repository writes unchanged; source-qualified PG guards and current startup PASS |
| FR-08 | MEDIUM | **CLOSED** | Required finite proof and named material documentation corrections independently adjudicated below |
| FR-L01 | LOW | **CLOSED** | Date filter now uses the current Asia/Ho_Chi_Minh date, removing the guaranteed fixed-2026-11-08 failure |

### FR-04 — JPEG decoder tolerance is treated as complete-image verification

**Observed facts:**

1. `D/internal/files/http/DriveFileHttpClient.java:373–416` checks SOI/EOI, parses a bounded SOF dimension, then accepts a nonnull `ImageIO.read` result. The call at line 414 does not inspect decoder warnings about incomplete data.
2. `CodexH1BR2CodecProbe` generated a synthetic complete 64×64 JPEG (**3,005 bytes**). Both complete PNG and complete JPEG controls were accepted. It retained the JPEG headers and only eight entropy bytes, adding an immediate EOI marker (**633 bytes total**).
3. The standard runtime reader returned an image **and one warning** for that malformed JPEG. Current `verifyImageSignature` returned true. `L/codec-probe.json`: exit 1; two positive controls PASS, incomplete JPEG rejection FAIL.
4. `CodexH1BR2JpegHttpProbe.truncatedJpegMustFallBackThroughPrivateHttp` used actual PostgreSQL, controlled provider HTTP, an authenticated real private thumbnail HTTP route and the same malformed image. **Expected 404, actual 200**. `L/full-probes.json`: 12 JUnit cases, the eleven earlier probes PASS and this sole new assertion FAIL.

This is an observed content-verification defect, not a live Google exploit, a size/deadline breach or a memory-crash claim. The controlled HTTP body length is exact; the missing data is within the encoded JPEG image.

**Approved requirement:** [thumbnail policy](../h1-thumbnail-security-contract-v0.4.md), content rule 5, and executable B-06 explicitly require rejection/fallback for truncated or suspicious image content. Producing a raster despite a premature-end warning does not establish complete content.

**Smallest correction:** retain the now-correct encoded/dimension/origin/deadline bounds. Use the existing standard reader's completion/warning/error information, or another bounded completeness check, so an incomplete JPEG is rejected. Unsupported verification may use the already-approved THUMBNAIL_UNAVAILABLE fallback. Do not suppress warnings, add a codec framework, transcode, or refactor unrelated paths.

**Proof to close:** complete JPEG and PNG still succeed; the supplied incomplete-entropy/premature-EOI fixture returns sanitized 404 through the real private route. Preserve the existing malformed WEBP/PNG, allocation, transport and authority positives/negatives. Only this residual behavior and its truthful evidence/handback update require correction; no closed finding is reopened.

### FR-08 — residual finite evidence and documentation now accepted

**Source facts and fresh executed evidence:**

- **B-02:** `T/DriveH1BAuthorityIntegrationTest.java` now holds the admitted action at `rootEntered/rootRelease`, starts a different-key contender while that action remains nonterminal, and observes the loser before releasing the winner. The separate 7→8 nonterminal and 255→256 retained cases pass; committed counts, mutation/ID controls and existing-key replay at full capacity are credited. This is the required database/provider admission proof, not a task-start race.
- **B-03/B-04:** the new actual child upload pauses at `uploadEntered/uploadRelease`, changes the root-binding epoch before releasing the provider response, receives UPLOAD_OUTCOME_UNKNOWN and proves the intent does not become COMPLETED. This tests an actual in-flight child publication path. Fresh S2 guard tests also exercise disconnect, credential/root epoch and fence rejection before dispatch/publication.
- **Composite inference, explicitly limited:** current child provider dispatch/completion uses those same inspected authority guards; actual child integration plus shared pre-dispatch/disconnect guard evidence establishes the approved fencing contract. The new child test itself changes a DB root epoch; it is not described as a literal Google disconnect or account-reassignment protocol test.
- **B-05:** a mixed root/child corpus now spans an empty filtered page with nonnull cursor and later matching child file on page 2, with actual controlled-provider call counts ≤8 on both requests. Prior six-child 8/4 operation-count and cursor/filter/proof tests remain applicable. Four additional pure cursor checks independently PASS: repeated provider token becomes 502 incomplete, capacity 128, 300s expiry and restart invalidation. These pure protocol checks make no DB/HTTP claim.
- **B-06:** progressing chunks every three seconds trigger the total eight-second deadline, separate operation cancellation fails closed, and in-flight credential-epoch change denies image publication. Existing private PNG success, five-second idle, origin/size/redirect and current-authority checks pass. The newly found JPEG content defect belongs solely to FR-04.
- **Docs/provenance:** child DBML no longer claims automatic pruning; 243 is historical rather than a current flag mode; operations accurately distinguish pre-upload remote child proof from returned-file affinity/current local authority at publication; thumbnail prose documents the actual supported-runtime/fallback policy. This review's exact-source manifest and saved checkpoint supply current source identity. The prior raw reviews/logs are preserved.

The finite gaps identified in FR-08 are closed by this source/applicable evidence combination. No live provider, UI, hook, model, full repository audit or new framework gate is introduced. Handback tests/probe reruns remain implementer evidence, not independent acceptance.

## Acceptance matrix

| Gate | Result | Basis / limit |
| --- | --- | --- |
| B-01 | PASS | Unchanged V27/JPA/DBML contract; fresh PG constraints, seeded V26→V27 preservation and state-update tests |
| B-02 | PASS | Fresh gated 8/256 admission, existing-key capacity recovery and durable restart/404 tests |
| B-03 | PASS (bounded contract) | Explicit current parent proofs, actual child epoch barrier and source-applicable fresh S2 guards |
| B-04 | PASS (bounded contract) | Child bytes/affinity, no stale COMPLETED publication, seeded compatibility and full 148-test S2 pass |
| B-05 | PASS | Fresh later-hit empty continuation/mixed corpus and real dispatch bounds; original six-child/filter/cursor tests plus four protocol checks |
| B-06 | **FAIL** | All preceding format/time/authority corrections pass, but malformed JPEG returns 200 |
| B-07 | PASS | Strict optional quota and current-epoch projection unchanged; fresh quota tests PASS |
| B-08 | PASS | Fresh 248/13 OpenAPI and Modulith in H1B web; source-qualified prior S1 flags/Portability no-export |

## Verification

**Environment:** Windows 11 amd64, Maven 3.9.16, Oracle Java 25, existing Python runner. No install, global environment/PATH/trust change, normal feature activation, production data or live provider call.

All potentially long commands use `python scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt <L>/<name>.json -- <executable> <arguments>`, fresh paths, owned capture/cleanup.

Exact Maven suffix: `'C:/Users/ADMIN/.m2/wrapper/dists/apache-maven-3.9.16-bin/5grr65jo27hi51sujmtcldfovl/apache-maven-3.9.16/bin/mvn.cmd' -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<selector>' test`.

| Fresh selector / Java argument file | Receipt | Actual result / watchdog duration |
| --- | --- | --- |
| DriveH1B*Test | h1b-new-tests.json | exit 0; 53 tests, no failure/error/skip; 136.88s |
| DriveS2*Test | s2-regression.json | exit 0; 148 tests, no failure/error/skip; 169.83s |
| Java @L/probes/runtime-full.args | full-probes.json | exit 1; 12 cases, 11 old probes PASS/new private JPEG FAIL; 58.61s |
| Java @L/probes/runtime-codec.args | codec-probe.json | exit 1; complete PNG/JPEG controls PASS, incomplete JPEG FAIL; 0.53s |
| Java @L/probes/runtime-allocation.args | allocation-probe.json | exit 0; complete-PNG/oversized-header checks; 0.53s |
| Java @L/probes/runtime-compressed.args | compressed-allocation-probe.json | exit 0; synthetic 388,871-byte large-dimension PNG rejection; 0.53s |
| Java @L/probes/runtime-cursor.args | cursor-probe.json | exit 0; four pure cursor protocol checks |

Java executable is `C:/Program Files/Java/jdk-25/bin/java.exe`. Allocation/codec/cursor argument files bound the child heap to 64 MiB. Successful javac receipts are `probe-compile.json`, `full-probe-compile.json`, `cursor-probe-compile.json`; fresh class/argument directories and source hashes are local ignored evidence. All runner captures/cleanup are complete, with no deadline or forced cleanup. Defect probes exit 1 because their correctness assertions fail, not because of tooling failure.

**Source-qualified reused independent evidence:** 62 S1 and 53 Portability/architecture cases in `logs/gdi26/h1-b/codex-final-9141cf2333914927ade5023af0880844/{s1-regression,boundaries}.json`, linked in the [initial review](2026-11-08-gdi26-s3-h1-b-independent-final-review.md). S1 sources/tests/fixtures and files-disabled branches, Portability source/test, V27/exclusion, shared transport and module edges did not change. Current H1B tests freshly validate startup/JPQL, OpenAPI and ApplicationModules.verify. These 115 cases are reuse, not fresh runs.

Thus **212 fresh existing JUnit cases PASS** (201 suite cases plus eleven prior probes), with 115 qualified prior passes. The newly added private JPEG case FAILS; standalone checks are not added to that JUnit total. No all-green acceptance is inferred from 327 prior/selected positives.

The implementer handback's duration column is not used as watchdog wall time: its referenced current H1B/S2 receipts report 82.12s/90.62s, while its table lists 24.23s/43.27s. Counts and successful outcomes are confirmed; this review's exact receipt times above control independent verification. Keep future handback timing labels tied to the actual measurement.

Mockito/ByteBuddy dynamic-agent, CDS and Springdoc development-endpoint warnings remain observed; no blanket suppression or IDE/static-analyzer-clean claim. The preceding S2 failed batch/isolated pass are historical; this fresh full S2 pass resolves that run's repeatability concern without rewriting it.

## Risk-applicable shared coverage

| Dimensions | Result | Evidence / applicability |
| --- | --- | --- |
| 1–2 intent/traceability; 10–12 business/wire/validation; 21–22 adversarial/testing | FINDING | Only residual malformed JPEG / FR-04 blocks; positive controls and actual HTTP prove the distinction |
| 3–9 architecture/ownership/tree/shared contract/SOLID/principles/patterns | PASS (scoped) | Same 70-file relevant tree/packages and ADR-0016 root envelope; approved additive V27; one changed production file; no new module/framework/dependency |
| 13–18 authority/credentials/transactions/recovery/database/resource bounds | PASS (bounded scope) | Explicit proofs/guards unchanged, fresh gated capacity/child/thumbnail tests, PG upgrade/constraints, bounded pre-decode checks and allocation negatives; no live-operation claim |
| 26–29 build/diagnostics/docs/hygiene | PASS (reviewed boundary) | Fresh watchdog evidence, hashes, links, current schema/operations corrections and preserved prior reviews; current acceptance status synchronized. Does not mean B-06 passes or warnings are absent |
| 19–20 retrieval/document fidelity; 23–25 benchmark/evaluation/dependency change | N/A | No RAG/OCR/model/evaluation/benchmark deliverable or dependency change. Image validation is covered under 12/18 |

## Scope-bounded Docs Sync Check

- **UPDATED:** this re-review and ACTIVE/executable status/result/review/counter/next action only: CHANGES_REQUESTED, FR-08/L01 closures, residual JPEG FR-04 OPEN, round 2/3. No acceptance or commit message.
- **ALREADY_CONSISTENT:** named DBML/ADR/architecture/operations/package corrections, published preparation/Owner decisions/visual hash, internal ownership, source scope and normal OFF defaults. Initial review and re-review 1 remain unchanged.
- **NOT_VERIFIED / correction required:** only complete JPEG rejection and its actual proof/handback result update. Decoder bounds/unsupported WEBP fixes and FR-08's finite evidence are credited, not reopened.
- **DEFERRED_OUT_OF_SCOPE:** prior H0/PH-L02 LOW/INFO, global summary/index/configuration matters retain their existing disposition. No historical cleanup or new Owner approval gate.
- **Operational NOT_VERIFIED holds:** real Google/lh3/TLS coverage, key/spool ACL/deployment, H1-AUTH/H1-F remain outside this bounded implementation acceptance.
- **Publication/hygiene:** 47 entry paths; 13 remediation-changed paths inspected against the prior checkpoint, recursive inventory retained. Reviewer edits only this artifact and two authorized status files; probes/classes/fixtures/logs stay ignored. No production/test edit, staging, commit, push, tag or PR.

## Next action

Return only residual FR-04 to Antigravity: treat incomplete JPEG decoder output as fallback, add the actual private-route regression/valid-image controls and update truthful evidence. Keep FR-01–03/05–08 and FR-L01 closed absent a new concrete regression. This is the remaining authorized correction/re-review round, not an automatic takeover decision.

**Next step:** Antigravity | `/antigravity-implement-handoff`, then `/antigravity-self-review`, then separate Codex `$codex-final-review` | GDI26-S3-H1-B, [executable handoff](../executable-handoff-h1-b-2026-11-08.md), residual JPEG FR-04 in this review | CHANGES_REQUESTED; malformed JPEG must return safe fallback with supported positives preserved before independent re-review | H1-B Antigravity 2/3 consumed; next correction/re-review 3/3; takeover NOT_AUTHORIZED.
