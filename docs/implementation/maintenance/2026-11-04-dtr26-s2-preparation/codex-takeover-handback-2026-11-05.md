# Codex takeover handback — DTR26-S2A FR-01

- **Date / implementer:** 2026-11-05 / Codex, bounded implementation takeover; not the independent reviewer.
- **Status:** `IMPLEMENTED_AWAITING_CODEX_REVIEW / INDEPENDENT_REVIEW_REQUIRED / NOT_ACCEPTED`.
- **Authority:** the Owner's explicit 2026-11-05 instruction authorizes **ONE** `$codex-implementation-takeover` for **DTR26-S2A-FR-01 only**: correct the 2003::/18 admission boundary, add focused actual-manager regressions, verify pinned IANA allocation widths, run bounded focused tests and update necessary implementation-adjacent status/handback documents. It grants no additional remediation, acceptance or publication authority.
- **Controlling artifacts:** [ACTIVE](../../handoffs/ACTIVE.md), [executable contract](executable-handoff-2026-11-04.md), [independent re-review 3](reviews/2026-11-05-dtr26-s2a-independent-re-review-3.md).
- **Entry:** main HEAD `43ca772054cfc9c2546cae87b949bcc065a1066a`, with the existing uncommitted S2A implementation/reviews. Entry state, index fingerprint and 160 scoped file hashes were captured before edits.
- **Counter:** Antigravity **3/3 EXHAUSTED / STOPPED**, 0/3 remaining; Codex takeover **1/1 implemented**. No fourth Antigravity round or automatic additional takeover.
- **Finding disposition:** FR-01 HIGH remains **OPEN pending separate independent verification**. FR-02–06 CLOSED retained. This implementer closes no finding and makes no commit-readiness claim.

## Earned escalation and role transition

The [initial independent review](reviews/2026-11-04-dtr26-s2a-independent-final-review.md), dated 2026-11-04, returned CHANGES_REQUESTED with FR-01–06 OPEN; it consumed no remediation round. The three actual corrective/re-review pairs below are preserved unchanged:

| Round | Corrective submission / date | Independent CHANGES_REQUESTED / date | Remaining blockers at that review |
| --- | --- | --- | --- |
| 1/3 | [Round-1 handback](implementation-handback-2026-11-04.md), 2026-11-04 | [Re-review 1](reviews/2026-11-04-dtr26-s2a-independent-re-review-1.md), 2026-11-04 | FR-01 HIGH; FR-03/04/06 MEDIUM. FR-02/05 closed. |
| 2/3 | [Round-2 handback](implementation-handback-remediation-round2-2026-11-04.md), 2026-11-04 | [Re-review 2](reviews/2026-11-04-dtr26-s2a-independent-re-review-2.md), 2026-11-04 | FR-01 HIGH; FR-06 MEDIUM. FR-03/04 closed; FR-02/05 retained. |
| 3/3 | [Round-3 handback](implementation-handback-remediation-round3-2026-11-04.md), 2026-11-04 | [Re-review 3](reviews/2026-11-05-dtr26-s2a-independent-re-review-3.md), 2026-11-05 | FR-01 HIGH only. FR-06 closed with independent supplement; FR-02–05 retained. |

The Owner then explicitly authorized this single FR-01 takeover. Codex is now the implementer for this correction. A **separate Codex session** must disclose that authorship and independently review the change using `$codex-final-review`. No session interruption or capacity error creates another allowance.

## Minimal correction and regression proof

[RssFeedDestinationPolicy.java](../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedDestinationPolicy.java) changes only the 2003 comment and predicate: retain the first 16 bits and require `(b[2] & 0xC0) == 0`, giving exactly **2003::/18**. Addresses beginning at **2003:4000::** now fall through to the existing unsafe result. No other production behavior changes.

- [RssFeedDestinationPolicyTest.java](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedDestinationPolicyTest.java) strengthens the existing public/unsafe cases with the exact assigned endpoints and reserved examples.
- [RssFeedClientTest.java](../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedClientTest.java) adds four admitted controls: `2003::`, `2003::1`, `2003:3fff::1` and the exact last address `2003:3fff:ffff:ffff:ffff:ffff:ffff:ffff`. Fake DNS is called once; the production-created pooling manager invokes the recording connector once with that exact peer, port 443 and original HTTPS hostname. The connector throws locally; no network connection is opened.
- Four reserved inputs are tested: `2003:4000::`, `2003:4000::1`, `2003:ffff::1` and `2003:ffff:ffff:ffff:ffff:ffff:ffff:ffff`. Each is checked alone and mixed with public IPv4 or the assigned IPv6 endpoint in **both orders**: **20 rejected DNS answers**. Each answer resolves once, returns `UPSTREAM_DESTINATION_REJECTED`, and produces **zero socket creations and zero connector calls**.

These tests inject only DNS/socket collaborators into `new RssFeedClient(policy, dns, null, sockets)`. The null custom manager selects the actual production manager and policy-enforcing resolver; these cases do not substitute the mocked high-level HTTP client used by other existing tests.

Before the correction, the eight new manager cases ran against the original production predicate: four admitted controls passed and all four reserved cases failed because each invoked the connector once. That failed receipt, source snapshot and XML remain preserved. After the correction, all singleton/mixed cases pass.

## Authoritative allocation-width verification

Consulted 2026-11-05: [IANA IPv6 Global Unicast Address Space](https://www.iana.org/assignments/ipv6-unicast-address-assignments/) (updated 2025-10-10) and [IANA IPv6 Special-Purpose Address Space](https://www.iana.org/assignments/iana-ipv6-special-registry/) (updated 2025-10-09). The unicast registry assigns **2003::/18** and reserves unlisted space.

Source inspection verified the masks for all **38 pinned accepting allocation branches**, plus the `/23` IETF container and `/48` AS112 exception: **40 width/containment checks** against retained authoritative CSV snapshots. The checks confirm numeric containment of the source-reviewed masks; they are not an automatic extraction of Java predicates or a general routability audit.

| Pinned code coverage | Registry comparison |
| --- | --- |
| 2003::/18 | Exact; sole production correction. |
| 2001:800::/23 + 2001:a00::/23 | Together equal allocated 2001:800::/22. |
| 2001:1400::/23 + 2001:1600::/23 | Together equal allocated 2001:1400::/22. |
| 2001:1c00::/23 + 2001:1e00::/23 | Together equal allocated 2001:1c00::/22. |
| 2001:2000::/20, 2001:3000::/21, 2001:3800::/22, 2001:3c00::/22 | Together equal allocated 2001:2000::/19. |
| Other pinned 2001 allocations; 2002::/16; 2400/2600/2800/2a00/2c00 ::/12; 2610::/23; 2620::/23 | Widths match their registry allocations; individual entries recorded in iana-width-check.json. |
| 2001::/23 and 2001:4:112::/48 | Exact container/exception widths. The code does not admit the whole container. |

Existing conservative omissions and unrelated transition-address rules remain unchanged. No registry service, dependency or accepted-scope expansion was introduced. The first Python CSV capture failed local certificate validation; its failure/traceback are retained. A successful bounded Windows HTTPS capture used normal certificate verification with **no trust/configuration changes or bypass**; numeric verification then passed against those same official registries.

## Actual verification evidence

Fresh, ignored evidence directory:

`logs/daily-trends-radar/codex-s2a-takeover1-20261105-7f072fd413a64b8991263179531be8a0/`

All Maven runs used the existing runner with `--profile normal --timeout-seconds 300`, and arguments `mvn.cmd -o -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' '-Dtest=<selector>' test`. The receipt filename and exact selector below identify each command; use a fresh receipt path for any rerun.

| Receipt | Exact selector / purpose | Tests | Exit | Watchdog seconds | Failures / errors / skips |
| --- | --- | --- | --- | --- | --- |
| boundary-before-fix.json | RssFeedClientTest#allocated2003BoundariesReachProductionConnector+reserved2003DnsAnswersAreRejectedBeforeAnyConnectorCall | 8 | 1 | 28.093 | 4 / 0 / 0; intentional defect reproduction, not acceptance |
| rss-provider-after-fix.json | RssFeedDestinationPolicyTest,RssFeedClientTest,RssAtomParserTest,RssFeedNormalizerTest | 178 | 0 | 41.766 | 0 / 0 / 0 |
| hn-transport-compatibility.json | HackerNewsClientTest,HackerNewsDestinationPolicyTest | 76 | 0 | 9.562 | 0 / 0 / 0 |

**Fresh final implementer total: 254 passing tests**, zero failures/errors/skips. Full stdout/stderr, copied per-class Surefire XML and parsed summaries agree with the receipts. `boundary-before-fix.xml` retains the original failures. Registry capture/verification used separate normal-profile **60s** receipts; `iana-width-check.json`, raw CSV hashes and the executed diagnostic sources are retained.

The native hook rejected an inline final-check command for ambiguous quoting before execution. The checks are supplied as a literal ignored `final-check.py` and invoked directly through the bounded runner, with normal hooks and a 60s receipt. No hook/startup/trust setting was changed.

The narrowed policy and its caller are covered by fresh RSS transport tests; HN transport compatibility is freshly green. Prior independent PostgreSQL/web/combined Radar/Feed-Inbox/architecture/search evidence remains credited under review 3 and its linked reviews. Those groups were **not rerun or added to the fresh total**. Their source/tests are unchanged from takeover entry; `entry-hashes.json`, `takeover-delta.json` and `final-boundary.json` establish the scoped change and unchanged index/history. The closed FR-06 configured-TLS/lifecycle/serialized-envelope supplement remains in the immutable review-3 evidence.

Compiler/runtime notices remain visible: the unrelated MediaBinaryFramingWireIntegrationTest deprecation, Lombok Unsafe deprecation and Mockito/dynamic-agent/CDS notices. No warning-free or IDE-clean claim is made. No whole-suite/extended run, PostgreSQL startup, real provider/private-endpoint request, schema/module/dependency change, scheduler, RAG/QA/inference/runtime download, Git staging or publishing occurred.

## Implementer self-check and Docs Sync Check

The production delta is **two lines changed in one file**, with existing line endings retained. The two RSS test changes add boundary and manager assertions without weakening existing cases. Actual transport/TLS/proxy/auth/cookie controls, HN code, FR-02–06 production repairs, schema and module/dependency/API/package boundaries are unchanged. Historical reviews, handbacks and entry evidence were hash-checked; only the necessary current registers and this additive handback changed. This is an implementer self-check, **not independent acceptance**.

- **UPDATED:** ACTIVE and executable contract current status, implementer/counter, result/next action; this handback. No routine governance cleanup.
- **ALREADY_CONSISTENT:** API extension/index, package tree, module/schema/dependency boundaries; FR-02–06 closures and reviewed proof. The allocation correction changes no API or package inventory.
- **DEFERRED_OUT_OF_SCOPE:** existing PH-L01/backlog and stale dated governance notices remain with their existing owners/future approved cleanup; no finding closed.
- **NOT_VERIFIED:** independent acceptance of Codex's correction and future Owner publication. The remaining FR-01 lifecycle stays OPEN for the separate reviewer.

**Next step:** Owner | open a SEPARATE Codex session with `$codex-final-review` | DTR26-S2A FR-01, this handback and executable contract | independently verify Codex-authored correction; NOT_ACCEPTED until that review | Antigravity 3/3 EXHAUSTED; Codex takeover 1/1 implemented; no additional takeover authorized.
