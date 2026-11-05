# Independent re-review 3 — DTR26-S2A

- **Date / reviewer / mode:** 2026-11-05 / Codex / independent third remediation review. No submitted production or test change authored by this reviewer.
- **Authority:** [ACTIVE](../../../handoffs/ACTIVE.md) entered as IMPLEMENTED_AWAITING_CODEX_REVIEW (round 3); [executable contract](../executable-handoff-2026-11-04.md), [round-2 review](2026-11-04-dtr26-s2a-independent-re-review-2.md) and [round-3 submission](../implementation-handback-remediation-round3-2026-11-04.md).
- **Baseline:** main HEAD `43ca772054cfc9c2546cae87b949bcc065a1066a`; uncommitted S2A implementation. Entry hashes/index state recorded before checks.
- **Disposition:** **CHANGES_REQUESTED / NOT_ACCEPTED / STOPPED / OWNER_DECISION_REQUIRED**. **FR-01 HIGH is the sole remaining blocker.** FR-06 closes with the independent supplemental evidence below; FR-02–05 remain CLOSED.
- **Counter:** **3/3 consumed; 0/3 remaining; Antigravity remediation EXHAUSTED; takeover NOT_AUTHORIZED.** No fourth round or automatic implementation. Owner must separately decide any bounded Codex takeover.

## Stable finding ledger

| Original ID | Independent result |
| --- | --- |
| DTR26-S2A-FR-01 HIGH | **OPEN/BLOCKING** — prior reserved examples fixed, but the purported allocation allowlist admits reserved space outside 2003::/18. |
| DTR26-S2A-FR-02 MEDIUM | CLOSED retained — cookie/auth controls and real-manager regressions remain applicable. |
| DTR26-S2A-FR-03 MEDIUM | CLOSED retained — RSS channel completion, namespace/context and complete-document limits remain correct. |
| DTR26-S2A-FR-04 MEDIUM | CLOSED retained — post-processing capture, remaining-budget DNS clamp and orchestration interruption guards remain applicable. |
| DTR26-S2A-FR-05 MEDIUM | CLOSED retained — invalid calendar dates remain null; valid leap date preserved. |
| DTR26-S2A-FR-06 MEDIUM | **CLOSED here with reviewer supplement** — full-ID/identity repairs retained; actual configured TLS, measured deadlines/cancellation/cleanup, exact bounds and observed privacy channels now supported. Read the evidence corrections below, not the submission's overstatements. |

## FR-01 — incorrect prefix width admits reserved IPv6 space

[RssFeedDestinationPolicy.java](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedDestinationPolicy.java), lines **233–236**, treats **2003::/16** as an allocated RIPE block and returns safe for every address whose first two bytes are 20:03. IANA allocates **2003::/18**; unlisted space remains reserved. The assigned portion ends at 2003:3fff:ffff:ffff:ffff:ffff:ffff:ffff. [IANA IPv6 Global Unicast registry](https://www.iana.org/assignments/ipv6-unicast-address-assignments/ipv6-unicast-address-assignments.xhtml), consulted 2026-11-05, last updated 2025-10-10.

The current compiled predicate returns **safe** for both **2003:4000::1** and **2003:ffff::1**. Given fake DNS returning 2003:4000::1, the **production RSS connection manager invokes the recording connector once**, instead of rejecting before connection. The recording connector throws locally; no real connection or completed exploit is claimed. This remains the original S2A-02 public-only admission defect, with a prefix-boundary counterexample.

The latest repair does deny 2d00::1, 2e00::1, 2001:1000::1 and all earlier reported examples; ordinary public 2606:4700::1111 and the assigned boundary 2003:3fff::1 remain admitted. Those repairs are credited.

**Smallest remaining correction:** constrain this allocation to its actual /18 boundary and verify the pinned policy's prefix widths against the cited allocation table. Cover the assigned last boundary, the first reserved boundary and mixed public/reserved answers through the production manager: reserved answers must yield zero connector calls. Keep the existing HN policy, transport mapping, frozen boundaries and other closures. No registry service, dependency, new feature or general security audit is required. Any implementation now requires the separate Owner disposition below.

## FR-06 closure and evidence corrections

The new tests are credited where their assertions observe the required behavior. Independent probes fill the two places where the submitted tests alone were insufficient. These are ignored reviewer diagnostics, not production/test repairs.

| Finite docket item | Source-applicable independent evidence and limits |
| --- | --- |
| Configured TLS hostname verification | The revised client test now exercises Apache SSLConnectionSocketFactory with simulated SSL sessions and mismatch/valid-host controls. In addition, `ConfiguredTlsReviewProbe.java` obtains the **actual factory registered in the current production-created pooling manager**, observes its DefaultHostnameVerifier, and calls that factory's real connectSocket/handshake/verification path with mock SSLSockets. Mismatch rejects; matching CN=example.com succeeds; both handshake calls observed. This is configured-verifier proof with fake peers, not a live TLS/trust-store or public-provider availability claim. |
| Slow exchange / in-flight lifecycle | The submitted “slow trickle” test immediately throws SocketTimeoutException; that test proves translation, not timing. `LifecycleReviewProbe.java` instead drives the real HTTP client/manager with in-memory sockets. With a 200ms injected deadline, stalled headers abort at **211ms** and a genuinely trickling body aborts at **209ms** after **17 body bytes**. Both report UPSTREAM_UNAVAILABLE and close the socket. Mid-body external thread interruption finishes, preserves the flag and closes the socket. Client DNS executors terminate after close. A separate active cooperative DNS task is cancelled, observes interruption and terminates with its executor. The production finally block cancels the scheduled abort future. Native OS DNS preemption and global watchdog-thread termination are not asserted or newly required. |
| Exact bounds / serialized envelope | Fresh tests prove exact/one-over body 1,048,576/1,048,577, text code units, title 1000/1001, URL 2048/2049, author 500/501 and output ID 500/501; earlier record/event/depth/tail/surrogate cases remain credited. The independent normalizer probe adds an escaped control-character ID that would exceed 2KiB: optional ID is omitted and final metadata is **63 bytes**. A 500-unit Chinese ID is retained at **1,580 bytes**; mixed Unicode/quotes/newline round-trips at **107 bytes**. No serialization-cap failure reproduced. |
| Actual privacy channels | `credentialAndHostileMarkersAbsentFromExceptionsAndLogs` now captures OutputCaptureExtension output and exceptions for synthetic URL credentials and an XXE marker. `errorResponseDoesNotLeakCredentialsOrHostileMarkers` observes authenticated public HTTP errors. Fresh PostgreSQL/web runs pass both, with expected error codes and no markers in the checked channels. Exact stored article/source URLs are still preserved; no blanket claim that tokens cannot exist in configured DB metadata. |
| Accurate current evidence | This report distinguishes supplied, fresh independent, reused and supplemental observations. FeedRefreshException/FeedRefreshFailureReason are the actual types. Dynamic PG PID/XID equality and the independent-commit negative control remain assertion-based evidence, not a claim of fixed recorded sample IDs. All historical submissions/receipts are retained. |

For clarity, the submission's S2A-05 prose **“publishedAt DESC, title ASC” is incorrect**: the canonical contract and actual comparator use latest publication, ascending **full trimmed original GUID/Atom ID**, missing after present, then document order. The retained 8,193-unit split-CDATA pipeline still selects **Second A** before 500-unit output truncation. There is no remaining ID-selection failure. The current policy helper is named `isUnsafeIpv6`/`isAllocated2001Suballocation`, not the submission's `isPublicIpv6Address`.

The pinned policy is a conservative subset of allocated IPv6 space, not a complete public-address availability guarantee. Its omission of some allocated blocks is not used to excuse the reserved-space admission above. These explicit corrections supersede inaccurate submission summaries; the canonical contract is unchanged.

## Verification receipts and applicability

Fresh ignored directory: `logs/daily-trends-radar/codex-s2a-rereview3-20261105-afe3d4542b414975bfdfce6521955c07/`.

| Fresh independent group | Tests | Exit | Seconds | Failures / errors / skips |
| --- | --- | --- | --- | --- |
| RSS provider/parser/normalizer | 163 | 0 | 11.140 | 0 / 0 / 0 |
| RSS PostgreSQL refresh/concurrency | 24 | 0 | 34.156 | 0 / 0 / 0 |
| Web/OpenAPI/envelope | 42 | 0 | 47.578 | 0 / 0 / 0 |
| **Fresh total** | **229** | **all 0** | **92.874** | **0 / 0 / 0** |

Each selection ran once via `scripts/run-bounded-command.py --profile normal --timeout-seconds 300`, offline Maven, the native-access flag and exact handoff selectors. Receipt/stdout totals and copied per-class Surefire XML agree. Three Java diagnostics have separate 60s receipts; their process exit 0 is not S2A acceptance. Local PostgreSQL image remained sha256:61785b8f5971c01669cce360d07e91aeed46f1225ae99eefe6cb6d863f57987c; no image pull.

**Reused independent:** combined Radar 13, HN 126, Feed/Inbox 87 and architecture/search 69 = **295**. Source/test hashes and dependency flow establish unchanged applicability; current RSS production deltas do not change those cached/generic/HN paths. They were not rerun. The implementer's separate seven receipts reconcile to **524 tests / 234.624s**, all exit 0, zero failures/errors/skips; these are not 524 fresh independent executions.

Compared with round-2 entry, remediation changes **two production RSS files**, **six RSS tests**, two current status registers and one additive handback. The previous review was added by Codex after that earlier entry. Prior review/handback contents remain intact. Actual recursive Feed inventory remains **114 production Java files / 26 packages / 24 test Java files**, all package descriptors present, no .gitkeep. Root ApiResponse, module-local adapters/advice and exception/command/view grouping remain consistent with [ADR-0016](../../../../adr/0016-root-http-contract-module-local-adapters.md). No new module edge, generic provider hierarchy, schema, POM, root HTTP or auth change.

Evidence includes entry-hashes/state, round3-delta, supplied-summary, independent-summary, source-applicability, tree-inspection, per-group XML copies, three probe sources/arguments/receipts and their stdout/stderr. All submitted input hashes matched before review/status writes. A first XML-copy attempt hit Windows relative long-path handling; the corrected absolute-path copies succeeded. No test failure or missing evidence is concealed by that file-copy retry.

## Shared review coverage and Docs Sync Check

Retain the [initial ledger](2026-11-04-dtr26-s2a-independent-final-review.md) and [prior checkpoint](2026-11-04-dtr26-s2a-independent-re-review-2.md) for unchanged evidence. All dimensions remain explicit and proportionate:

| Dimensions | Current disposition and evidence |
| --- | --- |
| 1 intent; 2 traceability | FINDING — same authorized manual S2A scope; S2A-02 still fails FR-01. Other case mappings use the corrected evidence above. |
| 3 architecture; 4 ownership; 5 actual tree | PASS scoped — recursive inventory/descriptors; unchanged public interfaces, dependency matrix and frozen schema. |
| 6 shared contracts | PASS — single root envelope per ADR-0016, Feed-local DTO/advice; fresh exact 235-route OpenAPI/web proof. |
| 7 SOLID; 8 principles; 9 patterns | PASS scoped — cohesive RSS adapter/parser/normalizer/finalizer, existing admission/ingest reuse; no new framework, abstraction hierarchy or speculative scheduler. |
| 10 domain; 19 data/provenance | PASS demonstrated — full-ID representative, strict dates, completion capture, exact URL identity, snapshots/rollback and cached provider separation. ML/retrieval claims N/A. |
| 11 API; 12 validation | PASS demonstrated — fresh web plus parser/field/byte boundaries; relative links stay ineligible and old HN contract remains compatible. |
| 13 auth/privacy | PASS scoped — unchanged bearer boundary; current hostile-marker exception/log/HTTP observations pass. No global secret-store/privacy audit claimed. |
| 14 egress / credentials | FINDING — FR-01 reserved destination still reaches connector; configured TLS, direct route, auth/cookie controls credited. |
| 15 transactions; 17 persistence | PASS demonstrated — fresh 24 PostgreSQL cases; physical PID/XID, independent-commit negative control, source lock/rollback/identity and timeout evidence retained. Bounded cache query and schema unchanged. |
| 16 lifecycle; 18 resources | PASS within approved bounds — measured fake exchange abort/interruption/worker cleanup, fixed caps/admission; native-DNS limitations explicit. No universal OS containment claim. |
| 20 document formats | PASS for changed RSS/Atom subset — source-applicable hostile namespace/XML and full-tail tests. OCR/Office/external RAG documents N/A. |
| 21 security testing; 22 test quality | FINDING limited to FR-01 boundary omission; remaining mandatory docket now has runtime/source-applicable proof, including configured TLS and actual measured lifecycle observations. |
| 23 benchmarks; 24 evaluation science | N/A — no approved benchmark/ML capability or acceptance project. Short timing diagnostics verify deadlines, not throughput. |
| 25 dependencies; 26 build/tooling | PASS scoped — no dependency/tool change; bounded offline selections pass. No compiler [WARNING] lines in the fresh summaries. Mockito dynamic-agent/CDS runtime notices remain. IDE/static inspection NOT_VERIFIED; no warning-free claim or suppression. |
| 27 observability; 28 docs/governance | PASS for corrected evidence / FINDING FR-01 acceptance claim — observed markers, truthful supplement, current status/counter synchronized; submission overstatements explicitly corrected above. |
| 29 hygiene/delivery | PASS — entry hash/index boundary, scoped files, retained history/receipts; production/tests unchanged by reviewer and no staging/publishing. |

- **UPDATED:** this new review and ACTIVE/contract current result, finding ledger, exhausted counter and Owner next step only.
- **ALREADY_CONSISTENT:** manual-only/two-route intent; source/package/API/module/schema boundaries and reviewed compatibility evidence. Creation/preparation history remains dated history.
- **BLOCKED_OWNER_DECISION:** FR-01 after three independently blocked rounds. The [final-review skill](../../../../../.agents/skills/codex-final-review/SKILL.md) requires: “After all three correction/re-review rounds are independently blocked, escalate to Owner for a separately bounded `$codex-implementation-takeover` decision. No automatic takeover.”
- **DEFERRED_OUT_OF_SCOPE:** PH-L01, DTR26-FR-L01, UI26-FR-L02, SAD-FR-L01, CSA26-FR-L01, AUTH-CLOCK/older backlog and stale historical notices. None closes. S2B, frontend/Drive, RAG/S19-2 and prior counters remain gated. Root ZIP untouched.
- **NOT_VERIFIED:** future Owner publication, real-provider availability and unrelated IDE/audit coverage; none is claimed as PASS. Mandatory S2A proof is covered except the **confirmed** FR-01 failure.

No production/test fix, real provider/private-endpoint request, full suite, runtime/dependency/image download, QA/inference/worker activation, Git staging or publishing occurred. Reviewer diagnostics used fake sockets/certificates/DNS and ephemeral PostgreSQL only. Review is complete; implementation remains not accepted.

**Next step:** Owner | decide whether to authorize a separately bounded `$codex-implementation-takeover` | DTR26-S2A FR-01 in this review and executable contract | correct the remaining admission defect only after explicit authorization; subsequent independent review required | 3/3 consumed, 0/3 remaining; Antigravity STOPPED; takeover NOT_AUTHORIZED.
