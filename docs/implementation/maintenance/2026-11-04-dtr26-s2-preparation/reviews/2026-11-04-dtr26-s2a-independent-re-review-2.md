# Independent re-review 2 — DTR26-S2A

- **Date / reviewer / mode:** 2026-11-04 / Codex / independent remediation round-2 implementation review; no submitted production/test change authored by this reviewer.
- **Authority:** [ACTIVE](../../../handoffs/ACTIVE.md) entered as IMPLEMENTED_AWAITING_CODEX_REVIEW (round 2); [contract](../executable-handoff-2026-11-04.md), [prior re-review](2026-11-04-dtr26-s2a-independent-re-review-1.md), [additive handback](../implementation-handback-remediation-round2-2026-11-04.md).
- **Baseline:** local main HEAD 43ca772054cfc9c2546cae87b949bcc065a1066a, implementation/remediation uncommitted. Entry hashes captured before verification.
- **Disposition:** **CHANGES_REQUESTED / NOT_ACCEPTED**. FR-03/04 close here; FR-02/05 remain CLOSED. **FR-01 HIGH and FR-06 MEDIUM remain OPEN/BLOCKING.** Historical reviews and original IDs are unchanged.
- **Counter:** **2/3 consumed; next correction/re-review round 3/3**, last Antigravity allowance. No automatic takeover/fourth round. If independently still blocked after round 3, stop Antigravity and request separately bounded Owner disposition.

## Findings and closures

| Original ID | Independent disposition |
| --- | --- |
| DTR26-S2A-FR-01 HIGH | OPEN / partial repair: earlier examples denied; other explicitly reserved IPv6 ranges still reach the production connector. |
| DTR26-S2A-FR-02 MEDIUM | CLOSED, retained: cookie/auth controls and proof unchanged. |
| DTR26-S2A-FR-03 MEDIUM | CLOSED here: exactly one RSS channel required at completion, including bare-root rejection; valid empty channel remains valid. |
| DTR26-S2A-FR-04 MEDIUM | CLOSED here: actual Clock path captures after eligibility/grouping/selection, then stamps final output. Advancing-clock test and retained independent probe pass; DNS/cancellation repairs retained. |
| DTR26-S2A-FR-05 MEDIUM | CLOSED, retained: strict calendar behavior remains green. |
| DTR26-S2A-FR-06 MEDIUM | OPEN / partial repair: full-ID selection and added identity/interrupt cases verified; actual TLS/lifecycle and remaining bounded privacy/evidence proof incomplete. |

### FR-01 — reserved-space admission is still incomplete

[RssFeedDestinationPolicy](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedDestinationPolicy.java) now denies the earlier 2001:4::1 and 3000::1 examples. Replacing 2000::/3 with **2000::/4** does not finish public-only admission: production calls still admit **2d00::1**, **2e00::1** and the unallocated **2001:1000::1** gap. Ordinary public 2606:4700::1111 remains allowed.

IANA explicitly lists **2d00::/8** and **2e00::/7** as reserved, and reserves unlisted allocation gaps. Broad enclosing unicast membership is insufficient. [IANA IPv6 allocation registry](https://www.iana.org/assignments/ipv6-unicast-address-assignments/ipv6-unicast-address-assignments.xhtml).

The current production RSS manager, given fake DNS returning 2e00::1, calls the recording connector **once** instead of denying the answer. The connector throws without making a real connection. This is a confirmed remaining S2A-02 admission defect, not a completed exploit/private-endpoint access claim.

**Minimum remedy:** finish the selected predicate against the referenced allocation/special-purpose classification, including gaps, rather than adding only the latest literals. A small pinned public-prefix policy and explicit exclusions suffice; no runtime registry service, dependency, framework or general security audit. Add boundaries/mixed-answer tests for reserved ranges/gaps and ordinary allocated public controls through the actual manager, requiring zero connector calls for denied answers. Preserve HN and reviewed mapping behavior.

### FR-06 — repaired behavior credited; mandatory evidence remains incomplete

The parser now retains the full **8,193-unit** original IDs in the split-CDATA fixture. The independently retained complete pipeline selects **Second A**, rather than First Z, before output provenance truncation. No remaining production ID-selection failure was reproduced. Parser retention, full-ID and surrogate tests are credited.

Fresh PostgreSQL checks now establish legacy externalId clearing, the same provenance ID changing article URL across refreshes while preserving the old row, and interruption already present before refresh. Snapshot, staged UPDATE/INSERT rollback, type flip, actual lock timeout and physical-transaction/independent-commit controls remain credited. The new dated handback preserves the round-1 file and replaces unsupported literal PID/XID claims with dynamic assertion descriptions.

The finite remaining docket is already in the handoff/initial review:

| Mandatory case | Current gap / minimum proof |
| --- | --- |
| Actual TLS hostname-verifier path | [tlsHostnameMismatchFailsWithUpstreamUnavailable](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedClientTest.java), line 450, injects a custom socket factory that unconditionally throws SSLPeerUnverifiedException, then calls a separate new DefaultHostnameVerifier outside the client. This proves exception translation and library behavior separately; it still passes if the production default verifier is weakened. Exercise the configured production verification path with a simulated TLS peer/certificate, mismatch denial and valid-host control. Keep traffic fake/local. |
| Slow exchange and in-flight lifecycle | Body overrun, queue saturation, DNS caller cap, denied answers, cookies/auth, 401 and rebinding are tested. Slow header/trickle abort, in-flight cancellation/close and owned worker/task cleanup observations remain absent. An already-interrupted thread is not a mid-flight case. Use short local fake exchanges/streams and observable ownership, retaining native-DNS limitations; no stronger OS preemption. |
| Remaining exact bounds | Record/depth/event boundaries, malformed tail after 50 groups and surrogate truncation are now supplied. Exact body/one-byte-over proof remains absent: the existing overrun is max+100. Exact/one-over title/link/author, isolated text and serialized Unicode/escaping envelope evidence remain unsubstantiated. Supply minimal local fixtures or precise source-applicable existing proof, without benchmarks/full-suite repetition. |
| Actual error/log markers | [hostileUrlMarkersAbsentFromExceptionsAndLogs](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/RssFeedRefreshIntegrationTest.java), line 442, checks only the exception message for a credential-bearing configured URL. It captures neither logs nor public HTTP error and supplies no hostile XML marker. Observe the required channels with synthetic markers. Exact stored article URLs/source metadata remain the contract; do not strip query parameters or change persistence to satisfy unrelated “not stored in database” prose. |
| Accurate evidence mapping | The additive report still names nonexistent methods/types (including UpstreamFeedException/UpstreamFeedErrorCode and refreshClearsExternalIdOnMatchingExistingRow) and claims ACCESS_EXTERNAL_DTD/ACCESS_EXTERNAL_SCHEMA setters absent from source. Match claims to actual methods, settings and receipts. Current fail-closed DTD/resolver behavior is credited; do not add unsupported StAX settings merely to match inaccurate prose. Dynamic PG assertions are sufficient without invented numeric samples. |

The remaining blocker is mandatory control/resource/evidence coverage, not an inferred TLS production defect. A green selector cannot supply missing observations. Preserve receipts/current submissions and use another additive dated correction artifact; mark unverified cases honestly. No new feature or Owner product decision is imposed.

## Closure evidence

**FR-03:** channelCount==1 is now checked before returning RSS. The prior bare-root probe rejects; valid-empty fixtures pass. Existing namespace/context and exact event repairs remain green.

**FR-04:** entry eligibility, URL grouping and representative selection now precede the clock read. The advancing-iteration test checks returned fetchedAt and rssCapturedAt. The independent controlled-list probe no longer observes capture before entry processing. Final output formatting/stamping does not require a new clock protocol. Prior DNS caller clamp/interruption repairs remain source-applicable.

## Independent verification and applicability

Fresh ignored directory: logs/daily-trends-radar/codex-s2a-rereview2-20261104-b741fc727fd749cd8f047027bcdd3bf1/.

| Fresh independent group | Tests | Exit | Seconds | Failures / errors / skips |
| --- | --- | --- | --- | --- |
| RSS provider/parser/normalizer | 148 | 0 | 9.141 | 0 / 0 / 0 |
| RSS PostgreSQL refresh/concurrency | 23 | 0 | 34.562 | 0 / 0 / 0 |
| **Fresh total** | **171** | **all 0** | **43.703** | **0 / 0 / 0** |

Both ran once through the existing bounded runner, normal profile, 300-second cap and unique receipts, with offline Maven/native-access flag and exact handoff selectors. Selected XML copies agree with stdout/receipts. The retained Java pipeline/destination probe uses current compiled classes under its own 60-second receipt; diagnostic exit 0 is not acceptance PASS. It adds reserved vectors without changing old evidence.

Prior independent web/OpenAPI/envelope 41, combined Radar 13, HN 126, Feed/Inbox 87 and architecture/search 69 (**336**) remain applicable to unchanged source/tests/contracts and were not rerun. The implementer's separate seven receipts reconcile to **507 tests / 236.673 seconds**, all exit 0; these are not 507 new independent executions.

Round-2 comparison identifies **three production files**, **five tests**, two current registers and one new additive handback changed. Prior reviews/handbacks are preserved. Inventory remains **114 production Java files / 26 packages / 24 test Java files**, descriptors present, no .gitkeep. Generic ingestion, HN paths, public interfaces/DTOs, module graph, schema/POM and manifest are unchanged from applicable checks. Local PostgreSQL image: sha256:61785b8f5971c01669cce360d07e91aeed46f1225ae99eefe6cb6d863f57987c; only ephemeral Testcontainers/local fakes used.

Evidence: entry-hashes/state, round2-delta, supplied-summary, independent-summary, tree-inspection, per-group XML/receipts, round2-probes.json and probe-observations.json. All entry hashes matched before review/status writes. No production/test repair, actual provider/private-endpoint request, full suite, runtime/dependency/image download, QA/inference/worker execution, Git staging or publishing.

## Shared coverage and Docs Sync Check

Retain the [initial ledger](2026-11-04-dtr26-s2a-independent-final-review.md) and [round-1 checkpoint](2026-11-04-dtr26-s2a-independent-re-review-1.md) for unchanged evidence. Every dimension remains explicit:

| Dimensions | Current disposition / evidence |
| --- | --- |
| 1 intent; 2 traceability | FINDING — same manual S2A scope, remaining FR-06 claim/proof mismatch. |
| 3 architecture; 4 ownership; 5 tree | PASS scoped — actual 114/26/24 layout/descriptors and unchanged module/interfaces/schema. |
| 6 shared contracts | PASS — ADR-0016 root envelope/local DTO/advice, retained exact 235 manifest/web proof. |
| 7 SOLID; 8 principles; 9 patterns | PASS structural review — RSS-local fixes/existing mechanisms, no speculative framework. |
| 10 domain; 19 provenance/data quality | PASS demonstrated repairs — channel, full-ID winner, identities/snapshots, dates and clock capture. Metadata/bound proof remainder in FR-06. |
| 11 API | PASS demonstrated / NOT_VERIFIED marker subset — unchanged wire/source and retained web; FR-06. |
| 12 validation; 20 formats | PASS demonstrated / NOT_VERIFIED remainder — current parser cases; bounded docket FR-06. OCR/Office/RAG N/A. |
| 13 auth/privacy | PASS demonstrated — FR-02 retained; log/error observations NOT_VERIFIED under FR-06. |
| 14 egress | FINDING — reserved-space admission FR-01. |
| 15 transactions; 17 persistence | PASS demonstrated — fresh 23-case PostgreSQL run, unchanged generic/schema/source evidence. |
| 16 lifecycle; 18 resources | PASS credited subset / NOT_VERIFIED — remaining slow-exchange/cleanup/body/field observations FR-06. No universal native containment claim. |
| 21 security tests; 22 test quality | FINDING — improved tests; substituted TLS error is not configured-verifier proof; other named observations absent. |
| 23 benchmarks; 24 ML evaluation | N/A — no such capability or acceptance project. |
| 25 dependencies; 26 build/tooling | PASS scoped — unchanged dependencies/tools, bounded offline groups pass. IDE/static inspection NOT_VERIFIED; no warning-free/suppression claim. Existing JVM/Mockito/line-ending notices remain runtime/configuration observations. |
| 27 observability; 28 docs/governance | FINDING / UPDATED — evidence/prose gaps FR-06, current dispositions synchronized. |
| 29 hygiene/delivery | PASS — permitted change map, source/tests/history unchanged by reviewer, no publishing/archive access. |

- **UPDATED:** this dated review plus ACTIVE/contract current result, counter and next step only. FR-03/04 close, FR-02/05 retained; historical reviews immutable.
- **ALREADY_CONSISTENT:** Owner authority/baseline, manual-only/two-route boundary, unchanged module/schema/dependency/package/API/compatibility evidence.
- **FINDING / Antigravity:** address predicate and finite missing security/resource/privacy proof; correct authorized evidence/API claims in an additive dated handback, without unrelated cleanup or contract relaxation.
- **DEFERRED_OUT_OF_SCOPE:** PH-L01, DTR26-FR-L01, UI26-FR-L02, SAD-FR-L01, CSA26-FR-L01, AUTH-CLOCK/older backlog and historical notices. None closes; ZIP untouched. S2B, Drive/frontend, RAG/S19-2 and old counters remain gated/unchanged.
- **NOT_VERIFIED:** listed mandatory remainder and future Owner publication. No new Owner decision is needed for existing corrections; stop only for an actual frozen/scope conflict.

**Next step:** Antigravity | /antigravity-implement-handoff, then /antigravity-self-review and separate Codex $codex-final-review | DTR26-S2A contract and FR-01/06 in this review | resolve blockers and provide bounded source-applicable proof | 2/3 consumed; next round 3/3, last allowance; takeover NOT_AUTHORIZED.
