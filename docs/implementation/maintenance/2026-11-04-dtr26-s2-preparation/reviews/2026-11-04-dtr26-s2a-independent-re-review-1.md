# Independent re-review 1 — DTR26-S2A

- **Date / reviewer / mode:** 2026-11-04 / Codex / independent implementation remediation re-review, round 1.
- **Authority:** [ACTIVE](../../../handoffs/ACTIVE.md) entered as IMPLEMENTED_AWAITING_CODEX_REVIEW (round 1); [executable contract](../executable-handoff-2026-11-04.md), [initial findings](2026-11-04-dtr26-s2a-independent-final-review.md) and [round-1 handback](../implementation-handback-2026-11-04.md). The stale contract header did not supersede ACTIVE's execution state.
- **Baseline:** main HEAD remains 43ca772054cfc9c2546cae87b949bcc065a1066a; implementation/remediation uncommitted. Compared with the initial review's entry hashes, round 1 changes five production files, seven test files and three status/evidence documents. No new production package, module, dependency or schema change.
- **Disposition:** **CHANGES_REQUESTED / NOT_ACCEPTED**. FR-02 and FR-05 are CLOSED. FR-01 HIGH and FR-03/04/06 MEDIUM remain OPEN/BLOCKING. These are continuations of the original findings, not six new findings or a new scope.
- **Counter:** **1/3 consumed; next correction/re-review round 2/3**. Takeover NOT_AUTHORIZED. S1's exhausted allowance and accepted takeover remain unchanged and nontransferable.

## Finding disposition and remaining correction

| Original ID | Independent disposition | Evidence / minimum remaining work |
| --- | --- | --- |
| DTR26-S2A-FR-01 HIGH | OPEN / partial fix | Initial three IPv6 examples now denied, but non-public IETF and reserved global-unicast space still admitted; actual connector proof below. |
| DTR26-S2A-FR-02 MEDIUM | CLOSED | Production disableCookieManagement/disableAuthCaching and authenticationEnabled=false; fresh real-client two-exchange cookie/auth and 401/no-retry tests pass. |
| DTR26-S2A-FR-03 MEDIUM | OPEN / partial fix | Foreign/nested field spoofing, unqualified Atom rejection and event count repaired; channel-less RSS still accepted as successful empty. |
| DTR26-S2A-FR-04 MEDIUM | OPEN / partial fix | DNS wait clamp and post-stage interruption guards added; completion timestamp is still captured before normalization by the new overload. |
| DTR26-S2A-FR-05 MEDIUM | CLOSED | Strict RSS calendar resolution passes invalid-day cases, existing format variants and valid leap-date control. |
| DTR26-S2A-FR-06 MEDIUM | OPEN / partial evidence | New PostgreSQL cases credited. Full-ID comparison demonstrably fails; required TLS/lifecycle/boundary/marker evidence still absent and handback claims remain inaccurate. |

The round-1 submission interchanges the meanings of FR-04 and FR-05. The stable IDs retain the **initial independent review's** meanings: FR-04 is deadlines/cancellation/completion timing; FR-05 is calendar validity. Do not silently reassign historical IDs.

### FR-01: broad global-unicast membership is not sufficient public-address admission

[RssFeedDestinationPolicy](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedDestinationPolicy.java), lines 155–261, now denies the original local-translation/documentation/outside-unicast examples. However, after the 2000::/3 check and a few special exclusions it still allows by default. Actual production calls admit **2001:4::1** and **3000::1**, while the ordinary public IPv6 control remains allowed.

The IANA special-purpose registry gives 2001::/23 default non-global status except more-specific assignments; 2001:4::1 is not in the separately listed global 2001:4:112::/48 exception. The global-unicast registry reserves unlisted allocations; 3000:: is not a public allocation. These classifications are checked against primary sources, not inferred solely from the bitmask. [IANA special-purpose registry](https://www.iana.org/assignments/iana-ipv6-special-registry/iana-ipv6-special-registry.xhtml), [IANA unicast allocations](https://www.iana.org/assignments/ipv6-unicast-address-assignments/ipv6-unicast-address-assignments.xhtml).

A reviewer fake DNS answer containing 2001:4::1 reaches the **production manager's recording connector once**, rather than being denied before connection. The connector deliberately throws without contacting any endpoint. This confirms the remaining admission defect; it does not claim a completed private-network exploit.

**Minimum remedy:** finish a conservative public-address predicate covering special/non-public and reserved space, with boundary/mixed-answer tests through the actual manager. Preserve ordinary public IPv4/IPv6 and reviewed safe mapping semantics; do not treat every address in 2000::/3 as allocated public space. No registry-download service, new dependency or frozen HN change is requested.

### FR-03: an RSS document with zero channels is still accepted

[RssAtomParser](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssAtomParser.java) tracks channelCount at line 70 and rejects extra channels, but lines 324–328 check only that a root format was recognized before returning. The complete document **&lt;rss version="2.0"/&gt;** returns RSS_2_0 with zero records. It can therefore follow the valid-empty success path and advance fetch timestamps even though the required channel is missing.

The added missingChannelRejected test supplies an item directly under rss, so it exercises the depth-2 unexpected-element rejection rather than the zero-channel completion case. The repaired foreign-title, description-descendant and unqualified-Atom cases and exact 20,000/20,001 event boundary are credited.

**Minimum remedy:** enforce exactly one proper channel at RSS document completion, including the bare root case; add that regression and retain valid empty channel success. Keep this distinct from XML well-formedness and valid-empty ingestion. The broader format/source contract is unchanged.

### FR-04: the clock overload merely moves the early capture

[RssFeedNormalizer](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedNormalizer.java), lines 89–90, immediately reads Instant.now(clock) and delegates to the Instant overload **before** entry eligibility, grouping, representative selection and metadata generation. The service now reuses that returned fetchedAt. It has not become a completion timestamp.

A local probe calls the actual Clock overload with a controlled record list that advances the clock when entry processing starts. Returned fetchedAt still precedes that processing. This verifies call order through the real normalizer, rather than mocking normalize itself. The source remains contrary to the required post-normalization timestamp for result, items/source and rssCapturedAt.

**Minimum remedy:** finish normalization work before capturing the single completion instant and stamping the final output. Add a changing-clock regression through the same production overload; a fixed clock or moving the clock read into a wrapper cannot prove this. Keep the correction RSS-local without a generic pipeline framework.

Other parts of FR-04 improved: the same fake 400 ms DNS delegate with a 50 ms request budget now returns after **58 ms**, with **zero connector calls**, versus 480 ms previously. Caller wait is clamped and ThreadLocal deadline state is cleared in finally. Post-fetch/parse/normalize interruption checks are visible in source. These improvements are credited; no native DNS preemption guarantee is inferred.

### FR-06: full-ID selection still fails and the proof docket is not complete

The parser's **currentText.length() &lt; 8192** condition at line 256 silently stops retaining a field before the normalizer compares original GUIDs. A bounded, legal fixture uses two records sharing an article URL, each GUID having an 8,192-character common prefix in one CDATA section and a different final character in a second CDATA section. The originals end in z and a. The parser retains 8,192 rather than 8,193 units; both IDs become equal, so the normalizer chooses **First Z** by document order instead of the required lexical winner **Second A**. The entire fixture is well below document/event/depth caps.

**Minimum remedy:** preserve the complete bounded original ID for representative comparison, applying the 500-unit provenance truncation only afterward. Global document/text limits already bound the input; an undocumented silent field truncation is not equivalent. Add the split-CDATA pipeline regression, not only a normalizer test built directly from untruncated RawEntry records.

Round 1 adds and independently passes useful real PostgreSQL cases: RSS saved-snapshot retention, legacy row matching plus another article, staged UPDATE/INSERT rollback on URL change, RSS-to-HN type flip and an actual lock-timeout rollback. Shared physical transaction/negative-control and retained-lock checks continue to pass. Credit these cases rather than rerunning unrelated source merely for a new review label.

The finite remainder of the **existing** proof docket still includes:

- Actual TLS hostname mismatch through the configured TLS/manager path, safe fallback/rebinding and bounded trickle/cancellation/shutdown cases. The current connector tests either record a peer and throw before TLS, or substitute in-memory protocol sockets. They do not prove certificate verification. This asks for local simulated collaborators, not a real provider or unsafe destination.
- The still-unprovided exact/one-over record, depth, body, field and isolated text cases; malformed tail after 50 selected groups; Unicode/escaping metadata-byte and surrogate boundaries; full-original-ID comparison above. Event exact/one-over is now covered and need not be rebuilt.
- A genuine changed-URL-across-refresh identity case. The new legacy test updates URL A and introduces URL B with **different GUIDs in one response**; it does not prove the same provenance ID changing article URL across refreshes, nor assert clearing the matched legacy externalId. Existing generic tests are reusable only where they prove the specific claim.
- The missing hostile-marker/log/error evidence and precise remaining nullable/schema checks already requested, tied to actual new RSS/combined contracts. Preserve the passing 235-operation manifest and old rows.
- An additive, accurate correction handback/self-review. Current documentation names tests that do not exist (for example refreshHonorsThreadInterruption), swaps FR-04/05 and again supplies literal PG PID/XID 81/883 without emitted observations. The dynamic identity assertions are credited; the literal sample is NOT_VERIFIED. Report assertions or actual captured samples. The initial handback path was rewritten for round 1 rather than preserved as an additive dated correction; original raw receipts and the independent failed review remain. Preserve current evidence and use a new dated/additive artifact for the next correction.

These are current authorized requirements, not new features, formats, enterprise infrastructure, a full-suite quota or an Owner adjudication gate. All twelve rows cannot yet be independently PASS.

## Verification and source applicability

Fresh ignored directory: logs/daily-trends-radar/codex-s2a-rereview1-20261104-10996ac316fd4e44a2aa3451ae40c819/.

Entry hashes were captured before testing. Round-1 delta is five RSS production files, seven tests and three status/evidence files; unchanged HN, generic Feed/Inbox, combined-query, search, architecture/shared contract and module/package artifacts were inspected by source applicability and their prior valid evidence reused. No package/module/schema/dependency change was introduced. Actual recursive inventory remains **114 production Java files / 26 packages / 24 test Java files**, all production package descriptors present, no .gitkeep.

Each fresh group used the existing bounded runner, normal profile, 300-second cap, unique receipt and offline Maven/native-access flag with the exact handoff selector. Selected XML snapshots agree with stdout and receipts. Deep Windows snapshot paths were read with absolute extended-path syntax; no test/source change was made to work around that local reporting constraint.

| Fresh independent group | Tests | Exit | Seconds | Failures / errors / skips |
| --- | --- | --- | --- | --- |
| RSS provider/parser/normalizer | 127 | 0 | 9.047 | 0 / 0 / 0 |
| RSS PostgreSQL refresh/concurrency | 20 | 0 | 32.078 | 0 / 0 / 0 |
| Feed/Inbox/RSS web and OpenAPI/envelope | 41 | 0 | 50.094 | 0 / 0 / 0 |
| **Total fresh** | **188** | **all 0** | **91.219** | **0 / 0 / 0** |

The initial independent combined Radar 13, HN 126, Feed/Inbox 87 and architecture/search 69 checks (**295**) remain applicable to unchanged source/contracts. They were not re-executed here. The current implementer's seven receipts separately reconcile to **483 tests / 269.170 seconds**, all exit 0. Do not call that 483 fresh independent tests or add executions into an acceptance count.

Round1ReviewProbe.java and the retained DNS probe use current compiled classes/existing test classpath, each under a fresh 60-second bounded Java receipt. Source, stdout/stderr and observations are preserved. Probe exit 0 means the diagnostic completed, not acceptance PASS. New probes establish the remaining failures and the credited controls above. No actual unsafe/public feed request, full suite, dependency/image/runtime download, QA/inference, worker activation or publishing occurred. PostgreSQL used the existing local image sha256:61785b8f5971c01669cce360d07e91aeed46f1225ae99eefe6cb6d863f57987c.

Evidence includes entry-hashes.json, entry-state.json, round1-delta.json, supplied-summary.json, independent-summary.json, tree-inspection.json, per-group receipts/XML, round1-probes.json, dns-budget-probe.json and probe-observations.json. Submitted source/tests/history were unchanged during this review; only review/status writes follow verification.

## Acceptance and shared coverage checkpoint

The [initial full coverage ledger](2026-11-04-dtr26-s2a-independent-final-review.md) is retained for unchanged categories. This checkpoint explicitly covers every dimension without creating new subsystems.

| Dimensions | Disposition / evidence |
| --- | --- |
| 1 intent; 2 traceability | FINDING — existing S2A requirements, stable IDs and incomplete claims; FR-06. |
| 3 architecture; 4 ownership | PASS scoped reuse — no module/API/dependency/schema delta; retained architecture/source evidence. |
| 5 folder tree; 6 shared contracts | PASS — actual 114/26/24 tree/descriptors; ADR-0016 root envelope and local DTO/advice unchanged; fresh web gate. |
| 7 SOLID; 8 principles; 9 patterns | PASS structure — narrow provider-local fixes and bounded deadline context; no speculative framework. Correctness failures recorded separately. |
| 10 domain; 12 validation | FINDING — missing channel, early capture and truncated-ID winner; FR-03/04/06. |
| 11 API | PASS demonstrated / NOT_VERIFIED remainder — 235 manifest and fresh web, remaining exact schema cases in FR-06. |
| 13 auth/privacy | PASS demonstrated — FR-02 closed, existing PPV bearer boundaries retained. |
| 14 egress | FINDING — production connector accepts non-public answer; FR-01. No universal egress claim. |
| 15 transactions; 17 persistence | PASS demonstrated — fresh PostgreSQL rollback/timeout/snapshot/identity cases; unchanged generic/schema baseline. Remaining identity fixture in FR-06. |
| 16 recovery | PASS demonstrated / NOT_VERIFIED remainder — DNS caller clamp and database rollback improved; remaining lifecycle proof FR-06. |
| 18 resources; 19 provenance/data quality | FINDING — original-ID retention and completion timing; FR-04/06. Event boundary repaired; cached read bounds unchanged. |
| 20 XML/format fidelity | FINDING — FR-03/06; initial field spoofing repaired. OCR/Office/RAG N/A. |
| 21 security tests; 22 test quality | FINDING — useful 33 additional tests and 188 fresh executions, but finite mandatory negatives remain. |
| 23 benchmarks; 24 ML evaluation | N/A — no such capability or claim. Diagnostic timings are bounds evidence, not benchmarks. |
| 25 dependencies; 26 build/tooling | PASS scoped — no dependency/tool mutation; fresh bounded offline checks pass. IDE/static inspection NOT_VERIFIED; no warning-free claim or suppression. Existing JVM/Mockito and line-ending notices remain configuration/runtime observations. |
| 27 logs/observability; 28 docs/governance | FINDING / UPDATED — fixed public messages retained; marker proof and accurate handback remain FR-06; current review/status synchronized. |
| 29 hygiene/delivery | PASS — permitted files, unchanged source/tests during review, no Git staging/publishing or archive access. |

S2A-01 and demonstrated S2A-09 behavior remain credited. S2A-02–08/10–12 stay FAIL/PARTIAL/NOT_VERIFIED for the specific open findings above. A passing selector is not full acceptance. FR-02/05 closure does not activate scheduling, finish RAG or accept deferred future capabilities.

## Docs Sync Check and next route

- **UPDATED:** this dated re-review, ACTIVE and executable contract current review/remediation/counter/status only. Keep the original independent review immutable and use its finding meanings.
- **ALREADY_CONSISTENT:** same Owner authority/baseline, manual-only/two-route boundary, old manifest/routes, package/module/schema/dependency ownership; S2B remains prospective.
- **FINDING / Antigravity:** inaccurate closure/testing/ID claims in the round-1 handback; preserve current submission and supply a new dated/additive correction artifact. Synchronize authorized API/evidence prose to actual repaired behavior; no unrelated governance sweep.
- **DEFERRED_OUT_OF_SCOPE:** prior PH-L01, DTR26-FR-L01, UI26-FR-L02, SAD-FR-L01, CSA26-FR-L01, AUTH-CLOCK/older backlog and historical notices. No finding silently closed. ZIP untouched; RAG/S19-2 and old counters unchanged.
- **NOT_VERIFIED:** remaining mandatory transport/resource/privacy/identity proof listed in FR-06 and future Owner publication. No Owner decision is needed to fix the existing authorized behavior; stop only if an actual frozen/scope conflict emerges.

**Next step:** Antigravity | /antigravity-implement-handoff, then /antigravity-self-review and separate Codex $codex-final-review | DTR26-S2A contract and remaining FR-01/03/04/06 in this review | fix blockers and provide bounded source-applicable evidence | 1/3 consumed; next round 2/3; takeover NOT_AUTHORIZED.
