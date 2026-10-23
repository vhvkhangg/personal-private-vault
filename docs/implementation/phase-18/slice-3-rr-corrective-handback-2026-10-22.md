# Slice 3 RR-01–03 bounded corrective handback

Checkpoint opened 2026-10-22; final handback completed 2026-10-23 on resume of the same authorization. **Codex is the implementer, not the independent reviewer.**
This is the separately Owner-authorized RR-01–03 checkpoint. Earlier checkpoints,
Antigravity 2/2, CHANGES_REQUESTED reviews, failed receipts and submitted readiness
claims remain historical. No staging, commit, push, tag, PR or activation.

**READY_FOR_INDEPENDENT_SLICE_3_RE_REVIEW**. RR-01-03 implementation and all applicable mandatory Slice 3 rows have passing final-candidate evidence; independent adjudication remains pending. Entry: main / HEAD
`ef32d41b33dd00e5fa08cf63977e91b8936ef3d5` plus the actual dirty candidate.
The entry inventory, complete applicable source copies, old Surefire archive,
original counterexamples and all new receipts are isolated under
`logs/phase18/s3-rr-corrective-20261022/`. No historical output path was reused.

## Original reproductions

The original independent Java diagnostic sources were copied with only output/
class-directory substitutions. They ran against the actual application, selected
Python/model and native PostgreSQL before production edits.

| Finding | Original observed result | New regression |
| --- | --- | --- |
| RR-01 | Real native vector sign reversal, administrative re-seal: COMPLETED, published 1, CURRENT; unchanged squared norm `0.9999999562494295`. | Original probe now rejects with zero G. Actual native sign flip, vector reassignment and component permutation tests require no G/outcomes/promotion. |
| RR-02 | Ordinary DOMAIN parent → approved external `.md` child published script-containing Markdown as plaintext, selected child R and became CURRENT. | Original probe now rejects admission. Safe structural and mandatory-rejection Markdown children both deny the entire admission before dispatch; an independent second root cannot partially complete. |
| RR-03 | Ordered finishers: both automatic-first finishes persisted ABORTED after known cancellation; cancellation-first persisted CANCELLED but competing cleanup falsely failed after the stopped session was forgotten. | Three deterministic orderings retain CANCELLED and successful safe teardown. The original actual-staging CANCELLED assertion is unchanged. |

Process exit zero on the original diagnostics means the observation was captured;
it is **not** acceptance PASS. Before/after JSON, command/argfile/Java-source hashes
and test receipts remain separate. The first new race-test compilation failed due
to a missing injected test field; the corrected pre-production reproduction then
recorded 3 cases / 1 failure / 2 errors with the exact durable statuses above.

## Minimal corrections

**RR-01.** `document_verification.py` previously checked vector shape/norm and then
removed embeddings before comparing source-derived chunks. The existing trusted,
credential-free replay now independently embeds the exact source-derived chunk
text through the existing pinned model/tokenizer/session. It compares each vector
component at the corresponding chunk position after exact text/source/locator/
structure/roster comparison. Dimensions, numeric types, finiteness, normalization
and existing Java pipeline/artifact checks remain. No model or embedding algorithm
changed; no new worker proof field or second parser was introduced.

The comparison is `abs(actual-expected) <= max(2e-6, 2e-5 * max(abs(actual),abs(expected)))`
per component (`math.isclose`). The absolute guard band is approximately 17 float32
machine epsilons; relative tolerance permits reduction/normalization roundoff,
while remaining far below semantic vector differences. This covers finite float32
inference plus JSON/pgvector conversion; it is not a norm/cosine acceptance rule.
Actual independent CPU sessions on multilingual source chunks had **0.0 maximum
component difference**. Real CPU→Java→PostgreSQL positives pass the same comparison.
Unit regressions also accept float32 round-trips/small rounding and reject larger
component errors, sign reversals, permutations, swaps and malformed vectors.

The selected ORT has CPU/Azure providers; CUDA inference is UNAVAILABLE. No real
CUDA numeric/fallback proof is claimed. Another provider is not permitted an
unbounded error or accepted merely because metadata names the same model: values
outside the explicit component bounds fail closed. The model/tokenizer hashes,
D17 normalization, singleton inference, duplicate-text cache and CPU threading
are unchanged. Replay retains one session across roots and call-local reuse.
Parsing, comparison and actual inference now share each root's existing finite
60-second scope; existing Java native-process memory/time/cancellation controls
remain. No original input-size or deadline relaxation.

**RR-02.** One canonical Java closure check rejects a mixture of external snapshot
and DOMAIN processing contracts within a root. It runs during admission before
any lease/reservation/producer dispatch, and again in canonical input construction
to protect transfer/replay. It does not forbid independent differently typed roots
in the same batch. Existing DOMAIN-only and MD→MD children remain supported;
external Markdown is never converted into DOMAIN `prepared_fields`.

**RR-03.** The initial lease-locked STOPPING transaction now durably records
`cancel_requested=true` in the existing run's operational `metrics` JSON before
waiting for process shutdown. Competing automatic finishers read it under the
same final lease/run guards and choose CANCELLED. Ordinary failures with no intent
remain ABORTED. Unknown stop retains RECOVERY_REQUIRED/reservations and the marker;
automatic reconciliation reads it from PostgreSQL and still cancels after proof.
Known terminal completion is re-read when another caller has retired the local
stopped session, preventing a false teardown failure. Requests after an already
committed terminal result retain the existing idempotent API behavior; they do not
record a new intent or rewrite ABORTED as CANCELLED. The intent commit on a
nonterminal job is the ordering point.

No new table, column, migration, route, public status, scheduler or job engine.
Source-first lock order, positive process stop, attempt/writer drain, terminal
staging seal, transactional cleanup/release and durable unknown-outcome handling
remain. Publication C_P is unchanged; inference occurs before it.

## Exact code paths changed in this checkpoint

Java application prefix:
`backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/`.

| Path | Correction |
| --- | --- |
| Java `RagDocumentInput.java` | Canonical same-processing-contract closure check; enforced at input construction. |
| Java `RagDocumentBatchService.java` | Invoke that check during pre-dispatch admission. |
| Java `RagPurgeService.java` | Durable intent, ordered terminal outcome and concurrent known-finish reconciliation. |
| `rag-engine/src/ppv_rag/document_verification.py` | Trusted exact-chunk embedding recomputation and component comparison in finite root scope. |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSlice3CorrectiveIntegrationTest.java` | Native vector/typed-closure positives and negatives; controlled cancellation/ordinary-abort/unknown-stop ordering. Existing expected CANCELLED assertion retained. |
| `rag-engine/tests/test_slice3_rr.py` (new) | Real independent-session numeric calibration, vector association, storage rounding, strict malformed values and inference cancellation. |

Production parser/chunker/model, original Source authority/storage, S3-F02/F04
eligibility/format fixes, SQL migrations/grants and dependency files are unchanged.
Documentation updates are prospective current status/contract inventory notices,
this handback, the new executed matrix and machine inventory. Historical handbacks,
reviews and evidence are unchanged.

## Verification and evidence boundary

Before the final selections: corrected native focused **10/0/0/0** and durable
intent/ordinary failure **2/0/0/0**; Python **198 passed** (39.56 s); Ruff/format,
fresh-cache mypy, selected-environment dependency and offline lock checks pass.
One overlong new test literal failed Ruff in the earlier static run; that failure
remains preserved, and the same literal was split without changing its value.

The first complete Core run is **FAILED: 99 / 0 failures / 3 errors / 0 skips**.
All three errors occurred in restart fixture cleanup after durable recovery had
already committed ABORTED. An overstrict change to late terminal cancellation
broke existing idempotence. That change was reverted; original terminal outcomes
remain immutable, while active known cancellation still requires CANCELLED. The
new ordinary-abort test was corrected to the existing contract, not used to weaken
the original staging CANCELLED assertion. The failed run/resources remain intact;
fresh focused and full selections follow the demonstrated correction.

The first final-source compatibility rerun is also **FAILED: 150 / 3 failures /
51 errors / 0 skips** (`java-compat-final-v2`). All observed failed native move
targets were exactly 260 characters: the long owned evidence temp root plus a
20-digit randomized fixture suffix reached Windows MAX_PATH. Three assertions
were downstream capture failures (500 instead of 201, failure before the intended
DB injection, and zero import winners). The prior passing fixture path was shorter.
The diagnosis, all failed logs and the unchanged observer script are retained.
The identical selection is rerun as `java-compat-final-v3` under new short isolated
`<OUT>/t-c3`; production/test source and assertions are unchanged. This is an
execution-path correction, not an implementation of extended Windows paths or a
claim that arbitrary managed-root lengths are supported.

Complete Java verification is partitioned into two bounded selections to retain
the ordinary command limit, with source manifests and separate owned-process
observations for each. It is not a full clean backend/P18-FINAL claim. Exact argv,
cwd, timeouts, logs, test counts and preserved failed attempts are recorded in
`commands.jsonl` and the new machine inventory. The final counts and resource observations below come from completed receipts.

The frozen D17 corpus is reused only for unchanged producer/parser/chunker/model
code: each prior parser/producer capture has 1,329 dispositions, 1,267 supported
plus 62 required rejections. Its mocked SQL/seal boundary does not establish this
new trust fix; actual native Java/PostgreSQL tests establish that separately.
Old resource observations remain attributed history. Changed replay inference is
measured again; no old profile is relabelled a current benchmark.

All required S3-01–16 observations and unchanged evidence applicability are mapped
in the new executed matrix. Independently CLOSED S3-F02/F04 remain closed for their
original defects; no finding is independently closed by this implementation.


## Final executed gates and receipts

All paths in this section are under `logs/phase18/s3-rr-corrective-20261022/`.
Every gate has its own bounded JSON receipt and log. `commands.jsonl` records
exact argv, working directory and watchdog invocation, including failed attempts.

| Receipt stem | Result |
| --- | --- |
| `java-core-final-v2` | 99 tests / 0 failures / 0 errors / 0 skips; actual native/PG and RR regressions. |
| `java-compat-final-v3` | 150 / 0 / 0 / 0; source, snapshot, v2, API, architecture, privileges, physical connection-loss and recovery. |
| `java-core-resources-v2`, `java-compat-resources-v3` | PASS; independent owned-process/temporary-tree observations wrapping each final selection. |
| `python-final-v2` | 198 passed; no failures/skips. D17, worker, producer, embedding and new RR tests. |
| `ruff-final-v2`, `format-final-v2` | PASS; 80 files already formatted. |
| `mypy-final-v2` | PASS; fresh isolated cache, 26 source files. |
| `dependency-final-v2`, `lock-final-v2` | PASS; Python 3.14.4, 67 installed compatible packages, offline lock 73 resolved. |
| `diff-check-final` | PASS; no whitespace errors. |

The final Java total is **249**. Earlier focused 10-, 2- and 8-case passes overlap
these selections and are not added to it. The first full Core 99/0/3/0,
pre-fix race 3/1/2/0, initial compilation error and initial Ruff failure remain
FAILED, with their exact logs and receipts. Original diagnostic exit 0 captured
wrong acceptance; it does not become PASS by being a successful command.

Commands use the selected `.venv-d17/Scripts/python.exe`. Maven uses
`mvn.cmd -ntp -f backend/pom.xml -DargLine=--enable-native-access=ALL-UNNAMED -Dtest=<selection> test`
with 1150-second inner/1200-second outer watchdogs. The Core selector is
`RagSlice3CorrectiveIntegrationTest`; the complete compatibility selector and
Python test path list are literal argv in `commands.jsonl` and machine inventory.
Static commands are `python -B -m ruff check rag-engine`,
`python -B -m ruff format --check rag-engine`,
`python -B -m mypy --config-file rag-engine/pyproject.toml --cache-dir <OUT>/fresh-mypy-final-v2 rag-engine/src`,
`uv pip check --python <selected-python>`,
`uv lock --directory rag-engine --check --offline`, and `git diff --check`.
All output/cache/temporary paths are isolated; historical capture scripts were
not rerun against their fixed paths.

## Current resource observations

Raw `java-core-final-v2-resources.json` and `java-compat-final-v3-resources.json` retains every process identity and limitation.
RAM and disk below are sampled peaks; CPU is observed owned-tree CPU seconds.
Process lifetime peak sums are separately retained and are not concurrent RAM.
NVML VRAM is device-wide usage, not attributable model VRAM. CUDA inference is
UNAVAILABLE in the selected ORT; no GPU fallback or numerical result is claimed.

| Final selection | Wall s | CPU s | Tree WS MiB | Tree private MiB | Minimum host free MiB | Device-wide VRAM MiB | Owned temp MiB | Capture/validator temp MiB |
| --- | ---: | ---: | ---: | ---: | ---: | ---: | ---: | ---: |
| Core 99 | 760.15 | 1012.66 | 2312.465 | 2583.832 | 1751.625 | 1561.027 | 2.501 | 2.279 |
| Compatibility 150 | 411.29 | 713.36 | 2650.211 | 2802.098 | 803.727 | 1484.039 | 0.707 | 0.203 |

Both observations report zero owned survivors, unreadable admitted process handles or remaining managed capture/validator temporary files. Sampling targets 50 ms and can miss short-lived processes/disk spikes; initial unidentified candidates are recorded without assuming ownership. Core measured 183 of 184 recorded producer/verifier identities; compatibility measured all 4. One short-lived verifier in actual-source-validation cancellation escaped the Core sampler. Its original peak/CPU is UNKNOWN and is not backfilled. A same-source focused rerun passed but cancelled before a verifier was registered, so it does not fill that measurement gap. The subsequent `rr-verifier-cancel-native` external diagnostic waits for the observer to acquire the exact live verifier handle before API cancellation. It passes CANCELLED/zero G/no selected R/no reservation/verified termination/empty managed temp; OS peak working set 15,974,400 bytes and CPU 0.0625 seconds for that verifier. This is supplemental scenario evidence, not retroactive measurement of the missed process. Identity and creation FILETIME, observer acknowledgement and complete raw profile are retained. All other full-run recorded native identities were measured. No claim of zero global Windows writes or deferred Slice 4 maximum-workload certification.

## Final source and preservation binding

Both final Java partitions hash the same 1,741 code-tree files. Final-candidate model/tokenizer, source, native input/output/result/roster/receipt and evidence hashes are in the machine inventory; its source manifests must still match the actual tree. The unchanged publication finalizer retains the existing one-physical-C_P transaction.

| Checkpoint code path | Final SHA-256 |
| --- | --- |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDocumentBatchService.java` | `9c91844e1c95e01970a8d793bd86b4a72cd9dd920bd21472a50ed2c3b140ad8d` |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagDocumentInput.java` | `d480d727807b912ba1e925f31e6d00ab6c85fc9218d492f5c28bdf941f4f6882` |
| `backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/RagPurgeService.java` | `b5a8278d6c0fddeda273cfd085afd8342c6fac3378943e8b846f6d6445526ff6` |
| `backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagSlice3CorrectiveIntegrationTest.java` | `496ecce9fd608c6c916c99918deee31a1fd89cab1cd97e0648c0117de883305d` |
| `rag-engine/src/ppv_rag/document_verification.py` | `a8904c1c01b7628e270671c3c6bde35a792139e6bca5f9b42e918e3b846a0819` |
| `rag-engine/tests/test_slice3_rr.py` | `d4f853ae6080021394af7490b2c7fa88eaf556ab2282da57a92eee9421141807` |

`entry.json` and `entry-source/` preserve the reviewed dirty baseline; `checkpoint-code-changes.patch` is entry-relative and normalizes line endings for review. Historical independent reviews, prior handbacks/matrices/evidence and 14,454 prior ignored evidence assets remain hash-checked. HEAD and semantic Git index are unchanged. No old failure is overwritten. An external delivery hash capsule binds the completed handback/matrix/machine inventory and final receipt artifacts without a self-hash cycle.

The original two reviewer probes were rerun again after final Java verification (`after-final-probe-native`, `after-final-mixed-native`): forged vectors reject with zero G; mixed child closure denies admission. All 1,741 source hashes still match. The live-verifier diagnostic and one focused overlapping JUnit case are additional evidence and are not added to the 249 final selected Java tests.

No known implementation blocker remains for this bounded checkpoint. Numerical tolerance and all matrix qualifications, original missing stdout, unavailable CUDA and measurement/fault-injection limits remain explicit independent-review scope; no human approval or independent closure is fabricated.

## Independent review scope and retained governance

Review trusted embedding/text association and numerical bounds; both unsupported
closure checks versus valid mixed independent roots; durable cancellation ordering,
both finish callers, late requests, unknown outcomes and restart/cleanup; and final
source/receipt/resource continuity across every mandatory matrix row.

Phase 17 FROZEN; Slice 1/D14 and Slice 2/D23 Markdown-only CLOSED; D24 `.md` only;
D22 original captures NOT_RECOVERED; FR18-3 OPEN pending independent verification;
external indexing INACTIVE; live originals DISABLED/DEFERRED; historical full Java
gate FAILED (1,433 / 1 failure / 2 errors / 0 skips); P18-FINAL NOT ACCEPTED;
Slice 4/excluded-format cleanup NOT AUTHORIZED. No dependency upgrades, OCR work,
independent self-review, agent publishing or automatic further checkpoint.
