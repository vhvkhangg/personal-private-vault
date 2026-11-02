# Independent pre-handoff review — DTR26-S1

- **Date / reviewer:** 2026-11-02 / Codex, preparation review only.
- **Source baseline:** local `main` and remote `main` verified at `6da26d4a457f3465e8b43489b95202d886787036`; UI26 implementation was Owner-published at `50aaf7d5b48bf71f61dcccb476790421c01c64f7` and closed by this baseline.
- **Disposition:** **`CHANGES_REQUESTED`**. Two **MEDIUM / OPEN / BLOCKING** preparation findings; item identity also requires an Owner decision. No implementation was reviewed or accepted.
- **Authority:** [Owner scope record](../owner-scope-and-decisions.md) and the Owner's `$codex-pre-handoff-review` invocation authorize this maintenance/feature preparation review. They do not authorize production implementation, provider execution, a scheduler or a handoff.
- **Execution register:** [ACTIVE](../../../handoffs/ACTIVE.md) remains `NO_ACTIVE_HANDOFF`, byte-for-byte unchanged. Numbered-phase predecessor/milestone prerequisites are N/A to this maintenance review. No Phase 19 resumption is inferred.
- **Counters:** preparation consumes no implementation remediation round; future DTR26-S1 remains `0/3 / NOT_STARTED`, takeover `NOT_AUTHORIZED`. UI26, CSA26 and RAG counters are unchanged.

## Required user-visible capabilities and proportionality

The smallest useful first slice is authenticated manual HN refresh, a cached rolling-24-hour preview, and the existing Save to Inbox action. It needs neither a new module nor a generic provider framework. These are design dispositions, not approval to implement.

| Owner-visible function / mechanism | Disposition and shortest adequate solution | Implementation / verification effort | Approval and proof |
| --- | --- | --- | --- |
| Configure a source | **REUSE** `FeedSourceOperations` and existing type/enabled/configuration fields. Do not tighten previously accepted metadata URLs merely because fetching is new. | Low / focused compatibility checks | Existing contract; DTR-01. Configured URLs are not outbound targets. |
| Manually refresh HN | **KEEP** one authenticated per-source POST, fixed official API paths, bounded fetch and normalization, then existing ingest. | Medium / fake transport plus PostgreSQL and HTTP tests | Prospective S1 only; DTR-01–07. PH-01/02 must be resolved first. |
| Identify and save a result | **OWNER DECISION REQUIRED**: one item per exact article URL versus one item per HN discussion. **REUSE** existing FeedItem-to-SavedResource flow. | Low–medium / identity and save-target regression cases | PH-01; DTR-04/05/10. No new bookmark API or automatic saving. |
| Read recent links | **KEEP / SIMPLIFY** one cached, SQL-bounded preview: fixed rolling 24h, proposed default 50/max 100, `publishedAt DESC, id DESC`, source attribution. Defer the optional `hours` knob and extra ranking modes. | Low–medium / deterministic cutoff, filter, cap and tie tests | DTR-08/09/11; restrict S1 to enabled HN sources and specify unsupported/absent source behavior. Preserve legacy per-source reads. |
| Explain rank and freshness | **KEEP** per-item HN rank/score and capture time as provenance; label the preview and submission recency honestly. | Low / fixtures and API assertions | DTR-09. Cached rows omitted from a later fetch remain stored; they are not proof of current top-list membership. |
| Concurrent refresh and failure reporting | **KEEP / SIMPLIFY** bounded single-JVM per-source admission; short final database work and safe categorical errors. | Medium / deterministic barriers and rollback tests | DTR-06/07 and PH-02. No distributed lease, durable queue or new job subsystem. |
| HTTP security and resource limits | **KEEP / REUSE** an existing compatible client, fixed targets, validated resolution used by the actual connection, zero redirects, finite bytes/operations/deadlines. | Medium / resolver/transport and parser negatives | DTR-02/03; no dependency installation or real unsafe-target request. Runtime proof remains future work. |
| More providers and scheduling | **DEFER** RSS/Atom and optional scheduler to separately approved S2; defer GitHub/Reddit and their access decisions. Existing scheduling fields do not execute a scheduler. | None in S1 / none in S1 | Later explicit approval and preparation. Their policy claims are not accepted by this S1 review. |
| Frontend, bodies, comments, media, AI, personalization and global popularity | **REJECT for S1**; metadata/link-only backend meets this request. | None / scope check | Explicit non-goals. No RAG, models, workers, Drive or external indexing. |
| New framework, schema, custom agent or benchmark project | **REJECT for S1**; reuse Feed, root HTTP conventions, backend implementer and bounded focused tests. | None / source and dependency diff | No demonstrated need for new architecture, migration, queue, agent, skill or hook. |

Effort is a relative planning judgment, not measured duration or a new benchmark obligation. S1 is one publishable vertical slice. S2 is not part of its acceptance gate.

## Blocking findings

### DTR26-PH-01 — MEDIUM / OPEN / OWNER_DECISION_REQUIRED: story identity conflicts with URL uniqueness

**Evidence.** The [proposed mapping](../source-reuse-and-api-design.md), line 56, combines `externalId = hn:<id>` with the external article URL. Existing [FeedItemService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java), lines 234–236, rejects non-equivalent candidates with the same trimmed-URL hash. Its equivalence check at line 277 includes the external ID and metadata. Its by-hash update at lines 99–102 can replace an existing row's external ID across fetches.

For example, two otherwise valid selected submissions `hn:101` and `hn:102` pointing to the same exact article URL produce different candidates and abort the entire batch. This is a counterexample established from source, not a claim that a live provider batch was fetched. The [official HN contract](https://github.com/HackerNews/API) identifies items by integer ID and does not promise unique article URLs. Consequently, neither silently discarding this as malformed provider data nor claiming stable HN-story identity is justified.

**Smallest alternatives, preserving both frozen per-source unique keys:**

1. **Recommended: one item per exact trimmed article URL.** Select one deterministic representative before ingest, for example the latest selected HN submission with an explicit tie-break; retain that submission's ID/rank/time as provenance. A nullable external ID can let existing URL-hash upsert own identity without inventing another hashing rule. Define replacement behavior across refreshes and for any preexisting HN rows. Existing Save to Inbox keeps saving the article URL; URL variants remain distinct under the existing trim-only contract.
2. **One item per HN story.** Always use its canonical HN discussion URL as the FeedItem URL, retain `hn:<id>`, and keep any article URL in bounded metadata. Existing Save to Inbox then saves the HN discussion URL. Saving the article instead would be a separate behavior change and is not silently included.

**Decision status.** These choices change visible duplicate and Save behavior. They were presented together to the Owner; no answer was available when this review was written. The [invoked review skill](../../../../../.agents/skills/codex-pre-handoff-review/SKILL.md) requires: “Ask material decisions together before implementation”. Record the answer in the scope decisions before resolving this finding. No migration, uniqueness relaxation or competing URL canonicalization is needed for either alternative.

**Documentation correction and future proof.** Reconcile the owner decisions, mapping, examples and DTR-04/05/10. Require same-URL/different-ID submissions in one batch, representative changes across refreshes, revised story URLs, preexisting-row behavior and the actual saved URL. Use PostgreSQL for uniqueness/upsert proof. Tests are **NOT_RUN** during preparation.

### DTR26-PH-02 — MEDIUM / OPEN: provider authority is checked before HTTP, but not through commit

**Evidence.** The [refresh sequence](../source-reuse-and-api-design.md), lines 30–37, validates HN type before network I/O and then delegates to generic `ingestFetch`. [FeedSourceService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedSourceService.java), lines 61–75, locks/refreshes the source and permits type changes. [FeedItemService](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/application/FeedItemService.java), lines 131–135, locks/refreshes the source before fetch-state commit but rechecks only enabled state. It intentionally accepts generic normalized input; it has no HN-type requirement.

Thus an enabled source can change from HN to RSS while the request awaits HTTP, and the proposed flow can publish HN items and advance fetch state under the now-RSS source. A second unlocked check before calling ingest still leaves a check-to-commit gap. This is a missing requirement in the new provider design, not a request to reopen the frozen generic ingestion contract.

**Smallest correction.** Specify provider-specific finalization within Feed: after all outbound work, lock and refresh the authoritative source, verify it remains enabled and `HACKER_NEWS`, and keep that authority protected through the ingest transaction's commit. Reuse the existing source lock and ingest behavior. One feasible shape is a short owner-local transactional finalizer; the handoff must make its transaction propagation and lock order explicit, including interaction with existing ingest. HTTP must actually run outside any ambient database transaction. Do not hold a database lock during HTTP, prohibit accepted source-type edits, specialize all generic ingestion to HN, or add a distributed coordination service.

**Future proof.** With deterministic fake-HTTP barriers, change HN to enabled RSS while a refresh is waiting, then release HTTP: finalization must reject safely and preserve items, `lastFetchedAt` and `nextFetchAt`. Also test disable-during-fetch, unlocked source updates during the wait, same-source admission release on failure and other-source usability. Specify one stable owner-local changed-source error without redesigning global advice. Add these cases to DTR-01/06/07; actual transaction/concurrency proof is **NOT_RUN** here.

## Specification checks and bounded design decisions

- **Official API:** use the documented HN v0 `topstories` and item endpoints only. Item `time` is HN submission creation time, not the linked article's publication time; titles can contain HTML, and top-story candidates can include jobs. Correct the API wording and define safe title normalization. Expected null/dead/deleted/non-story skips must be distinguished from malformed required story fields and transport failures; do not turn failures into a successful empty fetch. These facts were checked against the [official HN API documentation](https://github.com/HackerNews/API), not by running a provider refresh.
- **Client feasibility:** the current resolved dependency graph already contains Apache HttpClient **4.5.13** through AWS SDK `apache-client` **2.29.50**. Read-only `javap` of the installed classes confirmed `setDnsResolver`, `disableRedirectHandling`, `disableContentCompression` and `disableAutomaticRetries`. The [Apache builder documentation](https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/client/HttpClientBuilder.html) describes these hooks and warns that a supplied connection manager can override resolver configuration. No POM change or upgrade is justified for preparation.
- **Actual connection matters:** a separate DNS precheck followed by independent client resolution does not establish the promised destination control. The [JDK 25 builder API](https://docs.oracle.com/en/java/javase/25/docs/api/java.net.http/java/net/http/HttpClient.Builder.html), also inspected locally, has no equivalent public per-client resolver hook; `localAddress` binds locally. A feasible client exists, but safe peer selection, TLS identity, proxy policy, deadline cancellation and byte limits remain **NOT_VERIFIED runtime gates**. Keep the [documented SSRF controls](https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html) proportional to fixed-host S1; no generic crawler or OS firewall project is needed.
- **Finite fetch:** retain proposed first 30 IDs / absolute maximum 50 as a planning cap, plus one bounded top-list request. The handoff must give concrete per-response/aggregate byte, JSON depth, metadata and total-time limits and test their enforcement. Those are proposed limits until measured, not performance claims. No automatic retries or comment traversal are needed.
- **Read semantics:** fix S1 to rolling 24h of HN submission time, exclude unknown/future times, use deterministic SQL ordering/cap and return server UTC bounds. Label it a bounded preview. Per-item rank belongs to that item's capture time, not necessarily source `lastFetchedAt`; retained items are not a current complete HN list. An optional source filter must validate its supported/enabled policy explicitly. No cursor or ranking engine is needed for this preview.
- **Truthful counts/errors:** existing ingest returns item views, not an insert/update classification. Return only counts whose meaning the implementation can prove; do not invent “inserted” counts. Distinguish valid empty selection, unsupported/disabled/changed source, busy refresh and safe upstream failure using existing module-local advice and root envelope conventions.

These refinements belong in the bounded preparation correction and later handoff. They do not grant new scope or authorize a provider smoke test. Deferred-provider documentation was not used as evidence of permission for GitHub or Reddit.

## Audited sources and existing harness

All five new preparation files were reviewed: [README](../README.md), [Owner decisions](../owner-scope-and-decisions.md), [source/API design](../source-reuse-and-api-design.md), [provider/security policy](../provider-security-and-ranking.md), and [acceptance plan](../acceptance-and-review-plan.md). Their ten relative local file targets existed before adding this review.

The applicable existing context was checked without changing it:

| Area | Evidence and disposition |
| --- | --- |
| Authority/workflow | Root `AGENTS.md`, Feed-scoped `AGENTS.md`, ACTIVE, UI26 publication closeout, current Owner policy, documentation synchronization contract and preparation template. Maintenance review mode applies; accepted historical reviews remain intact. |
| Frozen ownership | `docs/architecture/module-dependency-matrix.md`, repository/package tree, Schema v1 Feed constraints, P10 Feed contracts and P13/root HTTP conventions including ADR-0016. Feed continues to own entities/repositories and may use only approved public dependencies. No new ADR/migration is required by the proposed minimal alternatives. |
| Source and packaging | Complete relevant Feed production tree: 87 Java files in 24 packages, package-info present for all actual packages, no stale `.gitkeep`. Inspected source/item/save operations and views, source/item entities and repositories, application services, controller/DTO/advice and their public/internal boundaries. No cross-module persistence access or new shared exception hierarchy is proposed. |
| Tests and build | Feed integration/concurrency/web tests, SavedResource Inbox tests and OpenAPI inventory contracts; `backend/pom.xml` and locally resolved HTTP/Jackson classes. Existing 231-route inventory is the compatibility baseline, not a fresh executed result; two new operations would require an expected count of 233 if retained. |
| Skills/rules | Pre-handoff review, review coverage, documentation writing, Feed/import modeling, Java/Spring, persistence, REST contracts, backend testing, modular architecture, reuse, pragmatic SOLID and bounded execution; applicable Feed/REST/reuse rules. They cover S1 without a new skill. |
| Agents/hooks/tooling | `.agents/agents/backend-implementer/agent.md`, existing `.agents` safety hook/rules and `.codex` configuration, `scripts/run-bounded-command.py`. Existing implementer plus self-review and separate Codex review suffice. No hook bypass or new agent is needed. Python/RAG implementation and takeover are N/A. |
| Navigation/static limits | Existing Graphify context was navigation only and source was verified directly; no graph rebuild. No IDE inspection was run. Existing deprecation/JDK warnings are not reclassified as clean or silently repaired by this review. |

## Future acceptance and evidence boundary

**DTR-01–DTR-12: NOT_RUN, prospective only.** No Java tests/build, live-provider smoke, unsafe network request, model inference, QA run or worker was executed for this preparation review. Library API introspection and documentation browsing establish feasibility, not runtime enforcement.

| Required future check | Gate and proportional verification |
| --- | --- |
| DTR-01, 06, 07 | Add PH-02's source-type/disable barriers and final transaction authority; prove rollback and no database lock while awaiting fake HTTP. |
| DTR-04, 05, 10 | Record PH-01's Owner choice, then test duplicate mapping, repeated refresh and exact Save-to-Inbox target against existing PostgreSQL contracts. |
| DTR-02, 03 | Inject deterministic resolver/transport; prove fixed targets, unsafe resolved-address and redirect denial, finite operations/body/decompression/JSON/deadline bounds. Do not request real unsafe destinations or relax production checks for localhost fixtures. |
| DTR-08, 09 | Fixed-clock cached SQL preview, time boundaries, ties/caps/source filter, empty output, null/future times and per-item provenance. GET must perform no HTTP. |
| DTR-11, 12 | Focused HTTP/auth/error and actual generated OpenAPI inventory checks; source-applicable Feed/Inbox regressions, changed-tree and dependency checks; Antigravity self-review followed by an independent Codex final review. |

Use the accepted watchdog's normal **300-second maximum per focused command**, with unique ignored receipts and owned process cleanup. Select actual new test classes plus relevant existing Feed/concurrency/web/Inbox/OpenAPI classes; no blanket full-suite run, RAG selection, new benchmark suite or real-provider smoke is required. Broaden only for an evidenced change risk and appropriate authorization. Implementation corrections use the current three-round policy; preparation does not consume or reset it.

## Shared review coverage ledger

`PASS` below means the preparation obligation was reviewed, never that unimplemented behavior passed runtime tests. Runtime gates remain explicit above; no dimension creates an additional subsystem.

| Dimensions | Status | Evidence / limit |
| --- | --- | --- |
| 1 Owner intent | PASS | Manual source-attributed links and existing save path; explicit non-goals and approved review mode. |
| 2 Traceability | FINDING | PH-01/02 require amendments to DTR-01/04/05/06/07/10. All twelve cases remain prospective. |
| 3 Architecture | PASS | Existing Feed owner, frozen schema/module dependencies and root HTTP contracts retained. |
| 4 Ownership/transactions | FINDING | PH-02 requires an owner-local finalization boundary without network in a database transaction. |
| 5 Repository/package structure | PASS | Full relevant production subtree inventoried; grouping/package-info/visibility baseline reusable, no speculative packages. |
| 6 Shared contracts/exceptions | PASS | Reuse application-root ApiResponse under ADR-0016 and Feed-local DTO/advice; no common module. |
| 7 SOLID; 8 programming principles; 9 patterns | PASS | Small provider adapter/orchestrator and existing persistence; no provider registry, ServiceImpl, generic utilities or parallel save layer. |
| 10 Domain correctness | FINDING | PH-01 identity and PH-02 changed-source behavior are concrete new-design conflicts. |
| 11 Wire/API compatibility | FINDING | PH-01 determines returned/saved link semantics; caps, errors and truthful count/rank wording must be fixed in the handoff. Existing routes preserved. |
| 12 Validation | PASS | Finite payload/title/metadata/time and malformed-record gates specified; exact executable caps and tests still future. |
| 13 Authentication/privacy | PASS | Existing Owner bearer boundary; metadata/link only, no article bodies, private/RAG sources or credentials. Runtime auth tests NOT_RUN. |
| 14 Egress/secrets/retention | PASS | Fixed official target, safe resolver plan and redacted errors; feasible existing client verified, actual transport enforcement NOT_VERIFIED until implementation. |
| 15 Atomicity/concurrency | FINDING | PH-02; existing enabled-only lock cannot enforce a new provider-type invariant. |
| 16 Recovery/operations | PASS | Failure-before-ingest and rollback semantics, valid-empty distinction, bounded admission/deadlines and cleanup are future acceptance gates. |
| 17 Persistence | FINDING | PH-01 conflicts with existing source+external-ID/source+URL-hash uniqueness; both constraints must remain intact. No migration proposed. |
| 18 Resource complexity | PASS | Finite candidate list and SQL preview; numeric executable budgets and enforcement remain proposed/unmeasured. |
| 19 Data/ranking quality | PASS | Submission recency and captured HN rank distinguished; no retrieval/recommendation quality claims or cross-provider score. |
| 20 Document/OCR fidelity | N/A | No document parsing, OCR, multilingual retrieval or geometry in S1. |
| 21 Security negatives; 22 test quality | FINDING | Add the two findings' identity/race proofs to the existing fake transport/real PostgreSQL risk-based plan. No test-count or coverage-percent target. |
| 23 Performance measurements | NOT_VERIFIED | No implementation/benchmark or latency claim. Bounded-resource tests are required later; a performance research project is not. |
| 24 Evaluation science | N/A | No ML generation, judge, held-out relevance evaluation or model selection. |
| 25 Dependencies/lifecycle | PASS | Compatible hooks confirmed in installed client; no install/POM/lockfile change or incidental upgrade. No new vulnerability audit claimed. |
| 26 Build/tooling/static diagnostics | PASS | Preparation consistency checks only; no code changes requiring builds. IDE inspection NOT_RUN and runtime/static cleanliness not claimed. |
| 27 Logging/observability | PASS | Safe source IDs and categorical failures planned; no provider bodies/tokens/query details in logs. Future tests/review verify implementation. |
| 28 Documentation/governance | FINDING | Record Owner identity decision and repair the bounded proposal; this review and README carry the current CHANGES_REQUESTED status. Deferred stale notices below are not active authority. |
| 29 Hygiene/delivery | PASS | Only this preparation directory changed; no ACTIVE/frozen/source edits, staging or publication. Input hashes and ignored receipts preserve the boundary. |

## Docs Sync Check and next action

**Updated here:** the scope README's disposition/review link/next action and this independent review. The other four proposal files remain unchanged for bounded correction by ChatGPT; their earlier proposed Next steps do not override the README disposition or ACTIVE. No executable handoff was created.

**Boundary verification:** input SHA-256 manifest covers the five proposal files plus ACTIVE. ACTIVE and the four design/acceptance files remained unchanged; tracked production, agents/hooks/configuration, scripts, architecture, ADR, database and repository-tree files have no diff. Relative local file links and whitespace were checked. The review receipts are under ignored `logs/daily-trends-radar/codex-preparation-20261102-9be1f75ab48444daaab040f1efaecd52/`.

**Deferred ledger:** obsolete CSA26/older phase status notices in broader AGENTS/policy/roadmap and older pending wording in the implementer are historical documentation cleanup for ChatGPT under separate scope, not grounds to rewrite acceptance history or hooks. Existing LOW/INFO backlogs, UI26-FR-L02, CSA26-FR-L01 and SAD-FR-L01 remain open; this review does not consume, close or promote them. P17 stays FROZEN, P18 CLOSED and S19-2 SUSPENDED / NOT_ACCEPTED / ARCHIVAL_ONLY; external indexing remains INACTIVE and live originals DISABLED/DEFERRED.

**Stop conditions:** unresolved item identity, any schema/uniqueness relaxation or frozen contract change, inability to enforce the selected transport boundary, loss of final source authority, unexpected live ACTIVE, or unauthorized RAG/provider execution. No automatic implementation, takeover or publication follows this review.

**Next step:** Owner + ChatGPT | Owner records article-URL versus HN-story identity; ChatGPT corrects PH-01/02 and the bounded design details, then Codex `$codex-pre-handoff-review` | DTR26-S1, this review and the scope README | both MEDIUM findings closed by independent re-review before readiness; `NO_ACTIVE_HANDOFF` | preparation rounds not consumed; future implementation `0/3`, takeover `NOT_AUTHORIZED`.
