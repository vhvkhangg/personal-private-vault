# Independent final review — DTR26-S2A

- **Date / reviewer / mode:** 2026-11-04 / Codex / initial independent implementation-slice review. Codex authored no submitted production/test change; ignored diagnostic probes are reviewer evidence only.
- **Authority:** [ACTIVE](../../../handoffs/ACTIVE.md), [executable handoff](../executable-handoff-2026-11-04.md), [Owner scope](../owner-scope-and-decisions.md), [published preparation review](2026-11-04-dtr26-s2a-independent-pre-handoff-review.md) and current Owner policy. Entry ACTIVE was IMPLEMENTED_AWAITING_CODEX_REVIEW.
- **Baseline:** main HEAD 43ca772054cfc9c2546cae87b949bcc065a1066a plus the uncommitted implementation. Entry hashes were captured before verification; no entry file changed during inspection/tests/probes.
- **Disposition:** **CHANGES_REQUESTED / NOT_ACCEPTED**. Six blocking findings: one HIGH, five MEDIUM. Passing test counts do not establish the missing or contradicted mandatory acceptance.
- **Counter:** initial review consumes no remediation. **0/3 consumed; remediation round 1 is next**, followed by mandatory Antigravity self-review and independent Codex re-review. Takeover NOT_AUTHORIZED. S1 exhausted 3/3 and its accepted takeover remain historical/finished.

## Findings

### DTR26-S2A-FR-01 — HIGH / OPEN / BLOCKING: non-public IPv6 answers pass the destination policy

**Evidence:** [RssFeedDestinationPolicy](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedDestinationPolicy.java), isUnsafeIpv6 at lines 155–249. The copied enumerated exclusions end in allow-by-default. Local calls to the actual production predicate admit 64:ff9b:1::a00:1, 3fff::1 and 4000::1. The resolver uses this predicate to admit every actual connector answer. IANA classifies 64:ff9b:1::/48 as local-use translation without global reachability and 3fff::/20 as documentation; 4000:: is outside the assignable 2000::/3 global-unicast block. [Special-purpose registry](https://www.iana.org/assignments/iana-ipv6-special-registry/iana-ipv6-special-registry.xhtml), [global-unicast allocation](https://www.iana.org/assignments/ipv6-unicast-address-assignments/ipv6-unicast-address-assignments.xhtml).

**Impact / contract:** S2A-02 requires rejection of the entire mixed answer if any destination is non-public/reserved. The local-use translation example also embeds private IPv4; this must not become an allowed arbitrary-host route. This is a confirmed admission-policy defect, not a claim that a private endpoint was contacted or an exploit completed. The old fixed-host HN acceptance does not certify this RSS policy.

**Minimum remedy / proof:** make RSS address classification conservatively fail closed for non-public/unallocated/special-purpose IPv6 and unsafe translation forms, including prefix boundaries. Keep the fixed HN behavior outside this correction. Test the listed values, mixed answers, mapped/translation forms and ordinary public IPv4/IPv6 through the actual RSS manager with recording sockets; denied answers must yield zero connector calls. No live destination, new dependency or registry-download subsystem is needed.

### DTR26-S2A-FR-02 — MEDIUM / OPEN / BLOCKING: cookies and automatic authentication remain enabled

**Evidence:** [RssFeedClient](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedClient.java), builder at line 107 and RequestConfig at line 168. Neither disables cookie management or authentication. A reviewer probe uses the production manager and in-memory recording sockets: the first synthetic HTTPS response sets a Secure cookie; the second refresh sends Cookie: ppv_probe=synthetic. Captured effective request configuration also reports authenticationEnabled=true. No real socket or server was used; this probe does not substitute for TLS verification evidence.

**Impact / contract:** S2A-02 and the exact transport contract prohibit cookies/authentication. The shared RSS client currently retains upstream tracking state between manual refreshes and sources on the same host. Authentication being enabled is a configuration-contract failure; no PPV credential-forwarding or actual credential disclosure was observed.

**Minimum remedy / proof:** use the existing Apache cookie/auth controls, preserving direct routes, redirects/retry/decompression restrictions and TLS identity. Add two real-client fake-exchange tests showing Set-Cookie is not retained/replayed, effective authentication is disabled and a challenge cannot cause automatic authenticated follow-up. Replacing the entire HTTP client with a mock cannot prove these controls.

### DTR26-S2A-FR-03 — MEDIUM / OPEN / BLOCKING: XML fields are selected by local name without their required context

**Evidence:** [RssAtomParser](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssAtomParser.java), record detection at lines 136–137 and field extraction at lines 211–239. The parser does not require rss/channel/item ancestry, permits unqualified Atom entries and generally ignores field namespaces/depth. Local complete-document probes demonstrate: a foreign x:title wins over the real RSS title; title/link descendants inside description become the article fields; RSS item directly under rss is accepted without channel; an entry with xmlns cleared under the Atom root is accepted. Line 220 additionally promotes dc:date to RSS publication time although this contract authorizes pubDate only.

**Impact / contract:** S2A-04/05/06 require exact supported roots/namespaces, ignored content/extensions and faithful article identity. A stored/saved article can take its title/link/date from ignored payload rather than the actual entry. Root-only namespace tests do not cover this behavior.

**Minimum remedy / proof:** track the actual element/namespace ancestry and permitted field context; enforce one proper RSS channel, direct supported records and exact Atom namespaces/case. Ignore extension/content descendants for core fields while retaining their resource cost. Only authorized publication fields supply dates. Test each reproduced document and ordinary RSS/Atom, including first eligible direct Atom alternate-link selection; no auxiliary fetch or new format capability.

### DTR26-S2A-FR-04 — MEDIUM / OPEN / BLOCKING: deadline, cancellation and completion-time ownership are incomplete

**Evidence:** [RssFeedClient](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedClient.java) line 353 always waits two seconds for DNS, independently of the current remaining exchange budget. A fake 400 ms resolver with a 50 ms request budget returned after **480 ms**; zero connector calls occurred, so the request abort prevents late connection but does not implement the required caller bound. This is controllable Future waiting, not a demand to preempt native OS DNS.

[RssFeedRefreshService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/RssFeedRefreshService.java) lines 119–134 has no cancellation check after parsing/normalization. A test-local parser setting the interrupted flag still reaches the finalizer. This is an observed finalizer invocation, not a measured PostgreSQL commit after interruption. The service also captures fetchedAt at line 122 **before** normalize at line 123. A clock advanced during normalization leaves the returned/passed timestamp earlier than completion.

**Impact / contract:** S2A-03/08 require nested DNS caller waits, cancellation before finalization and no post-deadline work. The HTTP contract explicitly captures fetchedAt only after complete fetch/parse/normalization, using that one instant for items, source timestamps and provenance. Current checks do not meet those requirements.

**Minimum remedy / proof:** propagate the current remaining deadline to DNS caller waiting, cancel/remove owned pending work and retain the bounded native-DNS limitation. Observe interruption/deadlines through parsing/normalization and immediately before the transactional call. Complete normalization before capturing/stamping the single completion instant. Add short-budget, parse/normalize cancellation, completion-clock and admission-release regressions; real DB state must remain unchanged on cancellation. No stronger OS containment, new worker framework or HN rewrite is authorized.

### DTR26-S2A-FR-05 — MEDIUM / OPEN / BLOCKING: impossible RSS dates become real publication timestamps

**Evidence:** [RssFeedNormalizer](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedNormalizer.java) lines 226–280 and formatter construction at lines 359–382 use date resolvers that normalize invalid calendar dates. The actual production parseRssDate returns **2026-02-28T00:00:00Z** for **30 Feb 2026 00:00:00 GMT**. The positive two-digit-year example 1 Jan 99 correctly produced 1999; that is not a finding.

**Impact / contract:** S2A-06 requires an invalid publication date to remain null, with per-source visibility and no fabricated Radar publication. SMART calendar correction invents a valid date and can incorrectly qualify data for a rolling window.

**Minimum remedy / proof:** enforce strict calendar resolution while preserving the authorized two-/four-digit years and timezone variants. Test impossible days/months, leap and non-leap February, valid existing format variants and normalization to null without dropping an otherwise eligible undated entry. Do not use fetch/updated time as fallback.

### DTR26-S2A-FR-06 — MEDIUM / OPEN / BLOCKING: mandatory acceptance evidence remains incomplete

**Evidence:** [RssFeedClientTest](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssFeedClientTest.java) replaces the private HTTP client at line 62 for most cases. Its one real-manager case throws from a recording socket before a TLS exchange; it does not prove certificate mismatch, unsafe fallback/rebinding or state controls. [RssAtomParserTest](../../../../../backend/src/test/java/com/vhvkhangg/personalprivatevault/feed/internal/infrastructure/rss/RssAtomParserTest.java) has no event-cap exact/one-over case. The parser starts eventCount at zero and counts only reader.next(), omitting initial START_DOCUMENT; the probe accepts a document with **20,001 actual events** despite the explicit 20,000 cap including START_DOCUMENT.

The submitted RSS DB selectors do establish shared physical transactions, an independent-commit negative control, retained lock, mid-fetch URL rollback and admission. They do **not** establish the required actual RSS lock-timeout rollback, type flip, or staged UPDATE plus INSERT rollback. The disable test seeds an old item but commits disable before ingestion: generic ingest rejects the disabled source before staging writes, so preservation of that item is not staged-update rollback proof. The URL-change test stages an INSERT only. The new RSS integration tests also omit the requested legacy non-null external-ID/collision and changed-article-URL fixtures.

**Minimum remedy / finite proof docket:** close the missing mandatory cases already named in the handoff, rather than adding new features or test quotas:

- Actual RSS connector negatives: unsafe/mixed/fallback/rebinding answers, TLS hostname mismatch, proxy/direct route and state controls, plus measured trickle/interrupt/shutdown ownership. Retain the positive recording-peer proof; do not call a fake TLS socket a certificate test.
- Exact and one-over byte/record/event/text/depth/field limits, including START_DOCUMENT; malformed tail after filling 50 distinct groups; Unicode/escaping metadata byte cap and original-ID representative comparison before output truncation. Correct the event counter as part of this docket.
- RSS PostgreSQL type/URL authority barriers with staged UPDATE and INSERT, retained timestamps, actual finalizer timeout/error/rollback; legacy identity and changed article URL; RSS Save-to-Inbox exact link and immutable saved metadata after refresh. Reuse unchanged generic evidence where it genuinely proves the stated case.
- Tie new default/body/security/error/nullable OpenAPI assertions and privacy-marker checks to actual RSS/combined schemas and captured behavior, supplementing the passing 235-row manifest. Keep prior rows and HN behavior unchanged.
- Supply an additive corrected handback/self-review mapping PASS/FAIL/NOT_VERIFIED to actual commands/assertions. The supplied 81/883 physical-ID values are not emitted in these RSS test receipts and were not independently verified as literal observations; report the passing dynamic identity assertions or preserve a real observed sample, rather than copying a sample from S1. Correct stale method names such as tryAcquire/findCombinedRadarCandidates against actual source. Preserve the original submission and receipts.

This is the existing S2A-02–08/10–12 obligation, not an additional subsystem, benchmark, full suite, human-adjudication gate or new scope. Blanket implementer PASS for all twelve rows is not independently substantiated.

## Independent verification and evidence

Fresh ignored directory: logs/daily-trends-radar/codex-s2a-review1-20261104-59ca66dfe5784ad08668359a448e049c/.

Each group ran once with scripts/run-bounded-command.py, normal profile, 300-second cap and a unique receipt, executing mvn.cmd -o -ntp -f backend/pom.xml, native-access test JVM flag, its exact handoff -Dtest selector, and test. No full suite or download was run. Existing local PostgreSQL image was verified as sha256:61785b8f5971c01669cce360d07e91aeed46f1225ae99eefe6cb6d863f57987c. All DB tests used ephemeral PostgreSQL/Testcontainers; provider destinations remained mocked/recorded.

| Fresh independent group | Tests | Exit | Seconds | Failures / errors / skips |
| --- | --- | --- | --- | --- |
| rss-provider-unit | 99 | 0 | 8.062 | 0 / 0 / 0 |
| rss-refresh-postgres | 15 | 0 | 32.093 | 0 / 0 / 0 |
| combined-radar-postgres | 13 | 0 | 30.563 | 0 / 0 / 0 |
| hn-compatibility | 126 | 0 | 37.109 | 0 / 0 / 0 |
| feed-inbox-compatibility | 87 | 0 | 36.093 | 0 / 0 / 0 |
| web-openapi | 41 | 0 | 46.593 | 0 / 0 / 0 |
| architecture-search | 69 | 0 | 51.625 | 0 / 0 / 0 |
| **Total** | **450** | **all 0** | **242.138** | **0 / 0 / 0** |

Counts were reconciled to the selected classes' XML and per-group stdout/receipts. XML snapshots include other preexisting report files; those are not counted. The supplied implementer receipts separately reconcile to 450 tests/238.282 seconds, all exit 0. These are separate executions, not an inflated 900-test acceptance claim.

Additional source-file-mode Java probes under the same ignored directory use the compiled submitted classes and existing test classpath. behavior-probes.json and dns-budget-probe.json each use a 60-second bounded receipt. Their drivers, stdout/stderr and probe-observations.json preserve the failures described above; their exit 0 means the diagnostic ran, not that acceptance passed. All traffic in those probes is an in-memory fake exchange or recording socket; no public feed/private endpoint was reached. Literal IP parsing did not perform hostname lookup.

Source applicability/tree evidence: entry-state.json, entry-hashes.json, pre-status-boundary.json, verified-source-applicability.json, tree-inspection.json and source/doc inspection. The generic ingest body compares equal to HEAD with recorded hashes. No old OpenAPI manifest row was removed/changed: 233 existing plus exactly two additions. Recursive Feed inventory is **114 production Java files / 26 packages / 24 test Java files**, including the existing fixture; every production package has package-info.java and no .gitkeep. Current cross-module imports remain public Vault/parent Knowledge APIs plus ADR-0016 root HTTP support. No source, test, POM, migration, hook, rule, accepted history or archive was changed by this review.

## Acceptance disposition

| Acceptance | Independent result / limit |
| --- | --- |
| S2A-01 | PASS for inspected source CRUD/preflight/auth/web behavior; no source-validation broadening. |
| S2A-02 | FAIL / NOT_VERIFIED — FR-01/02 and remaining actual-connector negatives in FR-06. |
| S2A-03 | FAIL / NOT_VERIFIED — FR-04 plus exact-cap/ownership proof in FR-06. |
| S2A-04 | FAIL — FR-03; basic DTD/XXE/XInclude and root-only negatives pass. |
| S2A-05 | PARTIAL / NOT_VERIFIED — basic URL grouping/upsert pass; FR-03/06 remain. |
| S2A-06 | FAIL — FR-03/05; ordinary date/UTC read fixtures pass. |
| S2A-07 | PARTIAL / NOT_VERIFIED — physical identity, same-assertion REQUIRES_NEW negative and retained-lock checks pass; FR-06 missing failure paths. |
| S2A-08 | PARTIAL / FAIL — shared same-source/max-two admission passes; FR-04/06 lifecycle gaps. |
| S2A-09 | PASS within scoped cached-read/source-provenance and SQL-bound behavior; exact new schema proof remains in FR-06. |
| S2A-10 | PARTIAL / NOT_VERIFIED — unchanged HN/Feed/Inbox/search regressions pass; RSS-specific missing fixtures in FR-06. |
| S2A-11 | PASS for inspected changed-tree, ownership, no automatic polling and unchanged schema/dependencies; specific privacy-marker proof remains in FR-06. |
| S2A-12 | NOT_SATISFIED — handback/self-review and green focused gate exist, but mandatory acceptance is contradicted/incomplete. |

## Shared risk-applicable coverage ledger

| Dimension | Result | Evidence / limit |
| --- | --- | --- |
| 1 intent; 2 traceability | FINDING | Approved manual-only scope retained; FR-01–06 prevent full acceptance traceability. |
| 3 architecture; 4 ownership | PASS | Actual Feed/public API layout, dependency imports, unchanged schema/POM and 37 architecture cases within the 69-test group. |
| 5 tree/folders | PASS | Recursive 114/26/24 inventory, descriptors, no .gitkeep; package inventory additive/coherent. |
| 6 shared transport/exceptions | PASS | ADR-0016 root ApiResponse reused, Feed-local DTO/advice/reasons, no new common module or exception catalog. |
| 7 SOLID; 8 principles; 9 patterns | PASS structural review | Cohesive provider/parser/normalizer/orchestration/finalizer roles, existing facades/admission/ingest reused; no speculative queue/provider framework. Concrete correctness defects remain separately recorded. |
| 10 domain correctness | FINDING | XML field authority, invalid date and completion timestamp: FR-03/04/05. |
| 11 API/wire | PASS / NOT_VERIFIED subset | Fresh authenticated web/235 exact manifest, all old rows preserved; remaining precise schema/privacy evidence FR-06. |
| 12 hostile input | FINDING | Core XML context and exact event count; basic body/DTD/encoding rejections pass; FR-03/06. |
| 13 authentication/privacy | FINDING | Existing PPV bearer boundary preserved; prohibited upstream cookie/auth state FR-02. No credential-disclosure claim. |
| 14 egress/secrets | FINDING | Non-public IPv6 admission and cookies FR-01/02; no live egress certification. |
| 15 transactions/concurrency | PASS demonstrated subset / NOT_VERIFIED | Dynamic PG identities/negative, lock and URL rollback pass; failure/staged-update evidence FR-06. |
| 16 recovery/lifecycle | FINDING | DNS remaining-budget and parse interruption fail; FR-04/06. |
| 17 persistence/schema | PASS inspected source | Generic ingest unchanged, source schema constraints retained, PostgreSQL regressions; no migration/index/DBML change. |
| 18 resource efficiency | FINDING | SQL/body/record bounds present; nested DNS wait and exact event cap fail, lifecycle evidence incomplete; FR-04/06. |
| 19 data/provenance quality | FINDING | Correct source-specific combined mapping; XML/date/capture defects FR-03/04/05. ML retrieval N/A. |
| 20 format fidelity | FINDING | RSS/Atom core context/namespace and date validity defects; OCR/Office/RAG format work N/A. |
| 21 security testing; 22 test quality | FINDING | Passing tests include strong physical-transaction negatives; FR-06 identifies finite missing mandatory negatives. |
| 23 benchmarks | N/A | No performance/benchmark claim; focused bounds measured where stated. |
| 24 ML evaluation | N/A | No model, recommendation, judge or RAG execution. |
| 25 dependencies | PASS scoped | No POM/lock/runtime/dependency change; no new vulnerability or license-audit claim. |
| 26 build/tooling/static diagnostics | PASS execution / NOT_VERIFIED IDE | Seven bounded offline groups exit 0. No IDE inspection or warning-free claim; Mockito self-attach/dynamic-agent/CDS warnings in probe stderr are tool/runtime observations, not suppressed. Git CRLF-to-LF notices do not show a source defect. |
| 27 logging/observability | PASS source subset / NOT_VERIFIED | Fixed categorical public errors/no new payload logging; explicit hostile-marker capture still in FR-06. |
| 28 docs/governance | FINDING / UPDATED | Unsupported blanket acceptance and submission/header status drift; review/ACTIVE/contract synchronized; corrected evidence handback required by FR-06. |
| 29 hygiene/delivery | PASS | Allowed file boundaries, no index/publishing/archive/history mutation; no production/test repair by reviewer. |

## Docs Sync Check and bounded remediation route

- **UPDATED:** this dated independent review and current status/remediation/final-review sections in ACTIVE and executable handoff only. The original implementation handback/self-review and receipts remain submission history, not independent PASS.
- **ALREADY_CONSISTENT:** Owner authority/baseline, two new routes, prior manifest preservation, same public module contracts, schema/dependencies, actual package inventory and tested HN/Feed/Inbox/search compatibility. Scoped inspection checked 44 local documentation links with no missing file before review writes.
- **DEFERRED_OUT_OF_SCOPE:** existing DTR26-S2-PH-L01, DTR26-FR-L01, UI26-FR-L02, SAD-FR-L01, CSA26-FR-L01, AUTH-CLOCK/older backlog and stale historical AGENTS/policy notices. This review closes none. The root ZIP was neither opened nor modified. S2B, Drive, frontend, RAG/S19-2 and all prior counters remain gated/unchanged.
- **NOT_VERIFIED / remediation owner:** Antigravity corrects the six findings within the existing handoff, supplies additive results/self-review and aligns authorized implementation-adjacent API/evidence prose to actual behavior. Routine governance/preparation/Owner-publication closeout remains ChatGPT/Owner work when requested. Do not rewrite failed evidence or silently relax the contract.

No additional Owner product/architecture decision is required to apply these existing requirements. Stop for Owner disposition only if a genuine frozen-boundary/dependency/authorized-capability conflict is established. No new framework, full suite, runtime download, real provider request or takeover is authorized. This initial review is not a remediation round; no commit message is issued while blockers remain.

**Next step:** Antigravity | /antigravity-implement-handoff, then /antigravity-self-review and separate Codex $codex-final-review | DTR26-S2A executable handoff and FR-01–06 in this review | resolve blockers and supply bounded source-applicable evidence | 0/3 consumed, next round 1/3; takeover NOT_AUTHORIZED.
