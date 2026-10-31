# S19-2 M10 read-only forensic report — 2026-10-30

- **Disposition:** `FORENSICS_COMPLETE / ROOT_CAUSE_NOT_VERIFIED / BLOCKED / OWNER_DECISION_REQUIRED / S19-2_NOT_ACCEPTED`.
- **Role/authority:** Codex implementer, same incomplete takeover 1, under the [one-time read-only Owner decision](owner-decision-s19-2-m10-read-only-forensics-2026-10-30.md). This is neither a corrective implementation nor an independent review. HEAD/main `d5e0a044b98652ae79faffa2caa1588645c12a81`; index empty.
- **Finding state:** F01–F05/F08/F09 OPEN; F06/F07/F10 CLOSED. Antigravity 3/3, historical resource corrections 3/3, C2 full starts 2/2 and infrastructure restart 1/1 remain exhausted; independent Codex corrective passes 0/3. No allowance is replenished.

## Evidence and provenance

Existing evidence root: `logs/phase-19/s19-2/codex-takeover-1-host-resume-1793349483900/`; `c2r1/` is logical full run 2, the sole infrastructure restart. This report uses that M10 only, without merging the first run's different pre-generation failure.

| Correlation | Verified existing observation |
| --- | --- |
| Request / native identity | `daf732a7-89d4-489c-ad27-4d189baab162`; worker PID `22884`, creation FILETIME `134378404045228170`. Slot result, worker-identity file and terminal reader agree; one native start and one generation-method call. |
| Timing / command | Watchdog starts `2026-10-30T13:26:21.460Z`, finishes `13:27:13.035Z`; Java PID `21028`, exit 1, 51.575s; capture/cleanup complete. Saved stdout lines 56–57 record worker HTTP 500 / `CANDIDATE_INVALID`, then `RagQueryUnavailableException`; JUnit lines 74–79 show expected 200, actual 503. The watchdog deadline was not reached. |
| Frozen source | Approved synthetic source ID `7`, file-source UUID `79bf4233-fa18-4f94-998e-72253c221547`, revision `df1ccdf6-602f-441c-b491-afcc32608970`, generation `c86a9a21-a158-4359-85b6-b4fab1a3d24e`; epoch 2, source version 2, reset 1, field policy 2. Captured citations `[1]`/`[2]` match this frozen identity. `[2]` has character span 30–165 / lines 2–4 and contains the approved 500-W capacity fact. |
| Required result / captured input | Frozen M10 is development/PARTIAL, with capacity `500W` and the battery-brand question unanswered. Java's captured generator input has five evidence units, no history, requested English. This proves the capacity reached that capture boundary; it does **not** prove Python's final packed/tokenized model prompt retained it. No final packed-prompt/token measurements were returned. |
| Actual result / safety | Root HTTP 503, `RAG_QUERY_UNAVAILABLE`, `data=null`; no answer emission. Reader TERMINAL / EXECUTION_FAILURE / ABORTED, with native/transport/context/sender quiescence recorded. Minimum available RAM 6,407,983,104 bytes; Job peak 6,695,649,280 bytes; resource-stop=false. Saved GPU monitor reports no failure/stop. These facts do not establish the absence of every possible native/runtime error. |

Hashes were read from the actual local files, rather than copied from the previous session's narrative:

| Existing file, relative to evidence root | SHA-256 |
| --- | --- |
| `c2r1/M10-command.json` | `d7b70c9eeabc26eb084fc1a24dbd484786bca71d6f99d64abbe6c09af08faa96` |
| `c2r1/M10-command.stdout.log` | `e7ff2c8b6d7464b7d322fcd946be23af335776473235ac6a70d3554495b6eb17` |
| `c2r1/M10-command.stderr.log` | `912a47375377dace6680faccff2a32e6e8d1db7923e6176d04bc97494c08e020` |
| `c2r1/c2/M10.json` | `18608ec12c9cbe2dc1110adbafdecd93c751a93f11b0343f0dc136be0abf6515` |
| `c2r1/c2/M10-request.json` | `20231837e06537b325820e83cd241d85cdf6c340a1bf9b96c1ca73acb5df5ccd` |
| `c2r1/c2/M10-worker-identity.json` | `ad41e3d54b5e32801d14b06f0e5f5fe82c106f26d964e7a41339a88809546292` |
| `c2r1/pre-answer-freeze.json` | `c6f56124b2e7178ce06ccce78fe9e168510490b18045a0dc890509bcd202d938` |

Freeze seal time is `2026-10-30T13:12:10.054937Z`, before request creation at `13:26:42.501Z`. M10 argument-file SHA `cce108ac443aed7a6a578c6da31d75691843ad4ad2349928bdc2c23cc705d9b7` matches the pre-answer fixed argument ledger. The original source freeze SHA `e0b413f6a7fca0faa64541caa754640f7328c245e9cd9919dd274612c9dccf45` and manifest SHA `2bdcbc395f301a74fc48d24424b4b5bb98f353f9ed4ae55e0e9a934b5962d81a` remain applicable. C2 is the same Qwen2.5-7B-Instruct Q4_K_M, profile `C2_OFFLINE_CUDA_TEST_V1` / `555ee729dc9b2636822f3b18ff5bbafe570f152a94f2cd04bdea24822ff008b6`, CUDA runtime lock `219c116bdce8582ac0a7a83929c1bb88e2e90105e4d6e7d39264c07e8c2d0663`. All ten frozen source/lock controls match current bytes; no new weight/native verification was executed.

## Static call chain and error boundary

| Stage | Exact source location and consequence |
| --- | --- |
| HTTP → owned sender | [RagQaController.java:32–52](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/web/controller/RagQaController.java#L32) dispatches the request asynchronously. [RagQaService.java:270,329–338,365–378](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/qa/RagQaService.java#L270) admits accepted snapshot scope, starts the same native child, embeds, retrieves/verifies, then calls generation with [Java packing:90–129](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/qa/RagQaContextPacker.java#L90). |
| Java → worker | [RagQaSupervisor.java:491–554](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/qa/RagQaSupervisor.java#L491) serializes evidence/history, computes the scope digest and calls loopback `/v1/qa/generate`. Non-200 responses are read with a 1-KiB bound; only an allowlisted category is logged. |
| Worker admission → generator | [grounded_qa.py:376–490](../../../rag-engine/src/ppv_rag/grounded_qa.py#L376) performs request schema, identity/nonce, replay, digest, question and history checks before generation. Their ordinary failures use 400/403/409, rather than this category. |
| Ambiguous catch | [grounded_qa.py:492–504](../../../rag-engine/src/ppv_rag/grounded_qa.py#L492) catches **every ValueError raised by the whole `generate_grounded_answer` call**, logs its exception class only, and sends HTTP 500 / `CANDIDATE_INVALID` without traceback. This does not identify candidate parsing as the origin. Other exceptions use the separate path at 505–515. |
| Generator work | [qa_generation.py:386–620](../../../rag-engine/src/ppv_rag/qa_generation.py#L386) includes model acquisition, rendering/token counting, packing, completion, finish checks, JSON parsing, semantic-structure checks and metric conversion inside that same catch boundary. |
| Worker failure → root 503 | Supervisor 547–554 throws before its successful-response parser at 562. Service [430–437 and 661–664](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/qa/RagQaService.java#L430) emits the sanitized root error, then stops/finishes at 447–464. The later Java [candidate validator at service 388](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/qa/RagQaService.java#L388) was not reached on this path. Its unanswered-label mapping cannot be the origin of the observed Python category. |

The generator has **33 explicit `raise ValueError` sites**, all potentially mapped to the same category:

| `qa_generation.py` line(s) | Concrete condition |
| --- | --- |
| 417 | Non-evidence prompt exceeds context budget. |
| 462 | Completion finish reason is not `stop`. |
| 471 → 479; 479 | Duplicate JSON key, or any exception while decoding the cleaned completion; rethrown as ValueError. |
| 482,488,492 | Decoded root is not an object; exact root keys differ; outcome is invalid. |
| 498,502,507,511,515 | Claims are not a list; claim is not an object; wrong claim keys; non-string ID/text; blank ID/text. |
| 518,520,524,529,533 | Supports are not a list; non-abstaining claim has no support; support is not an object; wrong support keys; non-string citation/quote. |
| 540,542,544 | Citation is not in active evidence; blank quote; quote is not an exact substring. |
| 550,554,557,559,561 | Unanswered parts are not a list; non-string entry; blank entry; entry exceeds 120 characters; forbidden control character. |
| 568,571,576,587,592 | Conflict pairs are not a list; non-object pair; wrong pair keys; non-string pair fields; blank claim IDs. |
| 597,602,605 | CONFLICT lacks two claims/pair; ABSTAIN contains claims; ANSWERED/PARTIAL lacks a claim. |

Additional ValueErrors can propagate from `int(...)` at 245/246/390/618, runtime JSON decoding at 65, and the model/formatter/tokenizer/completion calls. Static inspection of the **pinned installed sources**, without importing them, confirms examples: `llama_cpp/_internals.py:62,67,266,482` rejects model/vocabulary/context/batch construction; `llama_chat_format.py:267–268` exposes a template error as ValueError; `llama.py:1344–1346` rejects excessive prompt tokens. These are possible paths, **not claims that one occurred**. All three inspected supplier files match their CUDA-lock hashes. The category does not distinguish such an infrastructure/preparation error from rejected generated content.

## Missing evidence and classification

- **Absent:** raw completion, finish reason, rejected parsed structure, Python traceback/raising line, exception message, and final rendered/tokenized/packed model input. `worker_measurements={}` does not locate the failure: generation headers are produced only after success (`grounded_qa.py:517–518`); the test spy captures those measurements only after `callRealMethod` returns (`RagQaProspectiveEvaluationNativeIntegrationTest.java:173–174`).
- **Retention boundary:** saved M10 JVM stderr contains JDK/Mockito warnings, not the worker traceback. Both M10 GPU monitor streams are empty. The native launcher ([NativeWorkerProcess.java:100–107](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/infrastructure/client/NativeWorkerProcess.java#L100)) configures no worker stdout/stderr file capture. The slot driver saves input, root response, identity and counters ([lines 244–266](../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/rag/RagQaProspectiveEvaluationNativeIntegrationTest.java#L244)), rather than the failed completion. No matching separately retained worker traceback/completion was found in the owned run. No replay or synthetic reconstruction was attempted.
- **Contract check:** PARTIAL is representable: Python permits supported claims plus an unanswered string; Java maps nonblank unanswered text to server-owned `UNKNOWN_REMAINDER` and requires a claim/remainder ([RagQaValidator.java:41–46,120–125](../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/rag/internal/application/qa/RagQaValidator.java#L41)). No static invariant was found that necessarily rejects every compliant M10 partial response. This is not proof that the implementation is defect-free. The existing error-category test explicitly maps an arbitrary injected ValueError to `CANDIDATE_INVALID` (`test_grounded_qa.py:108–127`); it is not M10 branch evidence. Tests were read, not run.
- **Classification/confidence:** `ROOT_CAUSE_NOT_VERIFIED`; high confidence that the retained evidence cannot distinguish the competing origins. `IMPLEMENTATION_DEFECT`, `CANDIDATE_OUTPUT_INVALID`, `INFRASTRUCTURE_ERROR`, or a causal combination is **not demonstrated for M10**. Verified facts are the generation-call ValueError category, safe failed response, and mandatory acceptance failure.
- **Qualification impact:** M10 remains FAIL; C2 remains NOT_QUALIFIED, not proven intrinsically incapable by this category. M01–M09 retain their prior implementer-only assessment; M11–E24 remain NOT_RUN. C1 REJECTED, its original ledger NOT_RECOVERED, and the [latest independent review's](reviews/2026-10-29-s19-2-independent-final-re-review-3.md) finding state remain unchanged. No independent acceptance or further evaluation follows.

**Focused verification — actual results:** ten frozen source/lock hashes, three inspected supplier-source hashes and 57 immutable historical hashes match; the M10 fixed argument hash matches its ledger. Before/after checks confirm all 2,778 project files outside the four authorized documents and all 176 selected existing run/evidence files unchanged. The previous handback body retains SHA `ffeb827dd15433fcbcd9a226e69ec75f7722510840a6b3e0829ea0c7a699c81b`. All 263 local references across the four scoped documents resolve. `git diff --check` exits 0 with existing CRLF notices; index remains empty, HEAD unchanged. No tests, imports, native worker or model execution. These are static integrity checks, not product acceptance.

## Owner options and Docs Sync Check

1. **Suspend:** retain the current failure/evidence and leave S19-2 blocked; no further code or inference.
2. **Authorize a separate bounded diagnostic/correction scope:** expressly define privacy-safe stage/reason diagnostics and deterministic parser/contract controls, with no automatic generation. Any later acquisition of new completion evidence or requalification requires an explicit new inference/evaluation decision defining budget, provenance and independent review. This report grants none; it does not propose a third candidate or weaker gate.

**Docs Sync Check:** recovered `final-blocked-docs-sync-checkpoint.json`; only the two Owner-updated current pointers and unchanged current handback were invalidated for this scoped report. UPDATED this report, ACTIVE, executable-handoff forensic completion/status/route, and a concise current-handback pointer. ALREADY_CONSISTENT original receipts/failed outputs, ten frozen source/lock hashes, 57 immutable historical hashes, C1/closed-case/counter records, unchanged notes/model-selection. NOT_VERIFIED precise M10 origin/raw completion and remaining mandatory/independent acceptance. BLOCKED_OWNER_DECISION any corrective edit or new inference after this one-time inspection. DEFERRED_OUT_OF_SCOPE AGENTS/skills/navigation, LOW/cleanup B, later phases and frozen-baseline changes. No source/test/harness/runtime/config/log change, model/library execution, test run, staging or publication occurred.

**Next step: Owner | choose suspension or explicitly authorize a bounded diagnostic/correction scope | P19-S19-2 + this canonical forensic report | ROOT_CAUSE_NOT_VERIFIED; mandatory M10 FAIL; STOP before code changes or inference; incomplete handback cannot enter final review | same takeover1 INCOMPLETE; Antigravity3/3; resource corrections3/3; C2 starts2/2; restart1/1 exhausted; independent Codex passes0/3.**
