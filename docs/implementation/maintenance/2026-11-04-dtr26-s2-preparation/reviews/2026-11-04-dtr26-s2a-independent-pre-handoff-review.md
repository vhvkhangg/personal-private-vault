# Independent pre-handoff review — DTR26-S2A

- **Date / reviewer / mode:** 2026-11-04 / Codex / explicitly approved maintenance feature preparation, not a numbered RAG phase.
- **Source baseline:** local HEAD and local origin/main both resolve to 8502a30b38c48fb812397b3a5de216be804fd958 on main. No remote ref refresh was performed. Publication provenance is the existing [S1 Owner closeout](../../2026-11-02-daily-trends-radar-preparation/owner-publication-closeout-2026-11-04.md), not a new publication claim by this reviewer.
- **Owner authority:** [S2 preparation decision](../owner-scope-and-decisions.md), read with [Owner policy](../../../../workflow/current-owner-delivery-policy.md) and [ACTIVE](../../../handoffs/ACTIVE.md). The standalone skill invocation requests this preparation review. Implementation remains unauthorized.
- **Entry tree:** only the six new Markdown preparation files were untracked; no tracked source, test, tooling or documentation delta. All six were inspected.
- **Disposition:** **READY_FOR_HANDOFF — DTR26-S2A ONLY**, with the explicit review resolutions below. No CRITICAL/HIGH/MEDIUM preparation finding remains open. This is readiness to write a handoff after Owner publication, not implementation acceptance or READY_FOR_IMPLEMENTATION.
- **S2B:** prospective design only; not ready for an executable handoff. S2A publication plus current Owner confirmation and the documented S2B applicability/decision check are still required.
- **Counters:** S2A implementation NOT_STARTED; future remediation 0/3, no round consumed here and no takeover authorized. S1 Antigravity 3/3 exhausted and its one independently accepted Codex takeover remain finished historical allowances.

## Required capabilities and simplest adequate mechanism

| Owner-visible requirement | Preparation authority / simplest implementation | Reuse and required proof |
| --- | --- | --- |
| Configure an RSS source | Existing source CRUD and RSS enum; reject unsafe targets only when fetching | Preserve FeedSourceService validation, defaults, positive intervals and metadata-only due reads; S2A-01/11 |
| Manually refresh RSS 2.0 / Atom 1.0 | One configured feed_url, one bounded document request, Feed-owned parser/normalizer | Existing generic ingestFetch, source/item named interfaces, refresh admission and finalizer shape; S2A-01–08 |
| Keep one article and accurate provenance | Exact trimmed article URL per source; feed IDs are metadata | Existing source-scoped URL hash and atomic upsert, including legacy external-ID rows; S2A-05/06 |
| View cached HN and RSS together | Add combined read; retain existing HN-only read and refresh routes | Existing projection/read/DTO pattern, UTC clock, SQL cap and safe provenance fallback; S2A-06/09 |
| Save an RSS article to Inbox | Existing Save FeedItem operation; no new save API | SavedResourceService, Vault identity and immutable saved snapshot; S2A-10 |
| Refresh on a schedule later | Separate S2B using accepted provider use cases, global OFF plus source opt-in | Existing due metadata and shared admission; future S2B-01–06, not S2A acceptance |

## Necessity, alternatives and effort

Effort below is relative implementation/verification work, not a measured delivery estimate. S2A is one usable vertical slice; the acceptance rows are considerations, not twelve separate builds.

| Feature / mechanism | Disposition | Simplest adequate choice and effort / approval boundary |
| --- | --- | --- |
| Source CRUD, enum, scheduling metadata | REUSE | Low implementation; focused regression only. A second configuration API adds no capability. |
| Manual RSS/Atom adapter and secure fetch | KEEP | Medium implementation, high negative-test effort: arbitrary configured hosts materially differ from fixed-host HN. No article fetching. |
| Article identity and URL validation | REUSE / SIMPLIFY | Medium normalization/upsert proof; use ARTICLE_URL as the reviewed proposal, not inherited HN consent. No entry-ID reconciliation subsystem. |
| Combined cached Radar | KEEP | Medium query/web proof; two additive routes rather than changing accepted HN semantics. No popularity/ranking synthesis. |
| Existing Inbox capture | REUSE | Low implementation; exact-link, cross-source conflict and saved-snapshot regressions. No duplicate bookmarks API. |
| Admission and final authority | REUSE | Medium concurrency/physical-transaction proof. One shared capacity budget; no second RSS limiter or long HTTP transaction. |
| Parser / transport dependencies | REUSE | Existing Apache HttpClient and JDK StAX suffice at API/source level. No POM changes, parser framework or runtime downloads. |
| XML/resource negative fixtures | KEEP | Necessary current-slice evidence; bounded local fake-transport tests and PostgreSQL barriers. No real feed smoke or benchmark service. |
| Scheduler, fairness, cooldown, shutdown | DEFER | Separate medium implementation/high concurrency effort after S2A publication and S2B authority. No S2A worker pool, timer or future-proof job abstraction. |
| Distributed leases, durable retries, new schema | REJECT for S2A / DEFER | No current single-instance need. A later requirement needs an Owner decision and architecture review. |
| Relative IRI/xml:base resolution, extra dialects, feed auth | DEFER | Explicit compatibility limits; no need to build a resolver/auth subsystem for this slice. |
| New agents, hooks, skills, shared framework, full-suite quota | REMOVE from the proposed implementation burden | Existing backend implementer, relevant Java/Feed/testing skills, self-review and bounded runner suffice. Python/RAG skills and takeover execution are N/A. |

## Review resolutions of marked proposals

These are explicit preparation choices for the Owner to review when publishing this package. They are not claims about delivered RSS behavior, retroactive HN approval or a substitute executable handoff.

1. **Identity:** accept the recommended RSS ARTICLE_URL design. It matches the requested article/link Inbox capture and the actual SavedResource global URL-hash rule. Set externalId=null, group exact trimmed links before generic ingestion, and retain GUID/Atom ID only as bounded provenance. URL changes produce another row; do not equate unlike strings, strip tracking parameters or change generic dual-key rules. For equal publication times, use ascending lexical entry ID, missing IDs after present IDs, then document order. Missing publication times sort after valid times. This resolves the unspecified lexical direction without a new product capability.
2. **Routes and errors:** retain the proposed POST /api/v1/feed/sources/{sourceId}/refresh-rss and GET /api/v1/feed/radar/combined. Follow actual FeedExceptionAdvice: missing source 404 FEED_SOURCE_NOT_FOUND; disabled/type mismatch 422 SOURCE_DISABLED / UNSUPPORTED_SOURCE_TYPE; running/configuration change 409; unsafe destination/invalid response/limit 502; unavailable/rate limit/capacity/finalizer 503; malformed/bounded parameters 400 and missing bearer 401. Reuse the existing reason codes and root envelope. Do not copy the erroneous S1 documentation rows recorded below or change HN's current source behavior.
3. **Dates:** resolve the draft's broad “otherwise null” against its explicit future-entry exclusion: missing or invalid publication date yields null and remains available per source; a successfully parsed future publication instant makes that entry ineligible, rather than removing its date and retaining it as undated. Atom updated is never publication fallback. Future and boundary comparisons use one captured UTC clock for the normalization batch. RSS date fixtures must include the specification's two-/four-digit-year and timezone forms; supported parsing must not silently turn those valid dates into undated items. [RSS specification](https://www.rssboard.org/rss-specification), [Atom published semantics](https://www.rfc-editor.org/rfc/rfc4287#section-4.2.9).
4. **Complete bounded parse and selection:** retain 1 MiB response bytes, depth 32, title 1000, URL 2048, provenance ID at most 500 and serialized metadata at most 2 KiB. Refine the draft's illustrative “200 elements” to **200 item/entry records**, with at most **20,000 XML events** and **1,048,576 accumulated text code units** for the entire document. Select at most 50 distinct eligible article URLs in first-occurrence document order after representative grouping. Finish parsing to document end even after filling selection. A structural/global cap or malformed tail fails the fetch atomically; valid empty feeds can commit. The 200-record/event/text limits are reviewed design proposals, not measured safe-host results. The handoff must copy these units and exact-cap/one-over fixtures, not treat 200 child XML elements as 200 entries.
5. **Truthful counters:** parsedEntries counts all item/entry records scanned in the accepted complete document; eligibleEntries counts individually eligible records before URL grouping; distinctArticleUrls counts all groups before the 50-group selection cap; processedItems counts normalized representatives returned by committed ingestion. Do not relabel processedItems as newly inserted rows. Entry-local optional metadata must fit generic validation (including author at most 500) before ingestion; the handoff must specify deterministic omission/truncation rather than altering that validation.
6. **DNS/process limits:** 25 seconds is the proposed application refresh budget, including a reserved 5-second finalizer; exchange cap 10 seconds and DNS caller cap 2 seconds are nested, not added afterward. Reuse the bounded resolver approach with at most two DNS workers and four queued tasks, fast saturation failure, cancellation ownership and shutdown. Native name lookup is not universally interruptible. “No task leakage” means no unbounded replacements, abandoned application work or post-deadline connection/ingest, not guaranteed kernel-level native DNS termination. Preserve that existing [S1 host limitation](../../2026-11-02-daily-trends-radar-preparation/reviews/2026-11-02-dtr26-s1-independent-re-review-2.md); RSS must establish its own deadline/ownership negatives. No stronger native containment guarantee is accepted here.
7. **Combined provenance:** branch on current source type. RSS never exposes HN story/rank/discussion fields even if a legacy row happens to carry hn-prefixed metadata. Use RSS_SOURCE_CACHE with persisted fetch capture; HN preserves its current validated provenance/fallback. Existing HN response fields and source behavior remain unchanged. The current HN-specific publishedAt schema description must not be reused unchanged as the combined endpoint's RSS description; use a separate wire DTO if preserving the old schema requires it.

No material S2A Owner decision remains beyond review/publication of these explicit preparation choices. They were selected within the package's delegated review targets and preserve its requested capability. If the Owner requires feed-entry identity, relative links, authenticated feeds, real provider calls or stronger native DNS termination, stop and amend this preparation instead of inferring that requirement.

## Architecture, source applicability and feasibility

The inspected source supports the proposed reuse. FeedItemService still performs item upserts first, then locks/refreshes the source and records fetch times in its transaction. HackerNewsRefreshFinalizer wraps generic ingestion and its final authority check with REQUIRED propagation and a five-second timeout. RSS can add fetched-feedUrl equality in that same transaction without changing generic ingestion, CRUD, lock order, DBML or migrations. A pre-ingest-only check or REQUIRES_NEW ingestion would not satisfy S2A-07.

The actual SavedResource path copies the FeedItem URL, metadata and timestamps into a Vault-backed snapshot; later feed refreshes need no new Inbox operation. Existing source metadata accepts arbitrary scheme strings, so safe-fetch validation belongs in the new refresh boundary. Combined reads can remain Feed-owned bounded projections. [Module matrix](../../../../architecture/module-dependency-matrix.md), [Phase 10](../../../phase-10/README.md), [Phase 13](../../../phase-13/README.md), [ADR-0016](../../../../adr/0016-root-http-contract-module-local-adapters.md) and the [package inventory](../../../../repository/repository-package-tree.md) require no new top-level module/dependency edge or root transport type.

Local dependency inspection traced backend/pom.xml -> software.amazon.awssdk:apache-client:2.29.50 -> AWS parent HTTP client 4.5.13; Boot 4.1.1 manages httpcore 4.4.16. Java reports OpenJDK 25.0.2. Local JAR javap and the 4.5.13 source JAR confirm the pooling-manager constructor accepts DnsResolver, the connection operator passes validated InetAddress values to InetSocketAddress, and authentication/redirect/cookie/compression/retry controls exist. Thus a connection-bound resolver and direct HTTPS route are feasible; builder-level DNS settings alone are insufficient when a separate manager is supplied. Preserve hostname/SNI/verification, constrain all fallback addresses, and never reuse HN's fixed-host policy unchanged. This is source/API feasibility, **not RSS actual-peer proof**. [Apache manager API](https://hc.apache.org/httpcomponents-client-4.5.x/current/httpclient/apidocs/org/apache/http/impl/conn/PoolingHttpClientConnectionManager.html), [SSRF guidance](https://cheatsheetseries.owasp.org/cheatsheets/Server_Side_Request_Forgery_Prevention_Cheat_Sheet.html).

Local javap confirms JDK StAX factory/resolver/security-property APIs. Use the JDK factory, namespace-aware parsing, disabled DTD/external entities, denied external DTD access, a rejecting resolver and explicit DOCTYPE/event checks; no DOM, entity resolution or auxiliary content fetch is needed. Verify actual effective settings and hostile fixtures during implementation; availability of a property is not proof of safe configuration. [Java 25 XMLInputFactory](https://docs.oracle.com/en/java/javase/25/docs/api/java.xml/javax/xml/stream/XMLInputFactory.html), [XXE guidance](https://cheatsheetseries.owasp.org/cheatsheets/XML_External_Entity_Prevention_Cheat_Sheet.html).

## Inspected scope and preexisting harness

- All six preparation files: README, owner-scope-and-decisions, source-reuse-and-api-design, rss-atom-fetch-security, scheduled-refresh-design and acceptance-and-review-plan.
- Root, backend, Feed, test and docs AGENTS; current Owner policy/Docs Sync; active register; S1 closeout, independent takeover review and applicable re-review-2 native limits; existing HN API note; Phase 10/13 requirements; ADR-0016; module dependency matrix; actual Feed DBML and V1 SQL sections; current package inventory.
- Relevant Feed application services, HN transport/policy/normalizer/admission/finalizer, source/item repositories and projection, entity/JSON snapshot/command/view/public-interface structure, controllers, mapper/DTO/advice organization, saved-resource capture; existing refresh/concurrency/Radar tests and exact OpenAPI inventory.
- Recursive inventory: **103 production Java files, 25 packages, all with package-info.java, no .gitkeep; 16 test Java files including the existing fixture**. Cross-module imports remain public Vault/parent Knowledge APIs plus ADR-0016 root HTTP types. No forbidden internal import was found in that source inventory.
- Existing backend-implementer agent, Feed/HTTP rules, .codex/config.toml, available hook/rule inventory, Feed modeling, modular architecture, reuse, HTTP, backend-testing and bounded-command guidance. No changed/new skill, rule, agent, hook or build file exists in this package. Existing native-hook/root/trust limits are not recertified. Backend-implementer and the accepted bounded runner are sufficient; Python/RAG tooling and a current takeover path are not needed for S2A.
- Graphify CLI failed with its Windows launcher-path error; the saved interpreter lacked NetworkX. A read-only traversal using the already-installed Graphify environment succeeded. Its older Feed relationships were navigation only; important facts were checked in current source. No install, graph rebuild or cache rewrite occurred.

## Finite verification and delivery plan

The [acceptance matrix](../acceptance-and-review-plan.md) covers S2A-01–12. All RSS runtime requirements remain **NOT_RUN / NOT_VERIFIED**. No S2 implementation or implementer test report was supplied, and no fresh application test was executed in this preparation review. Historical S1 results are source-applicable baseline evidence only; they do not certify RSS.

The future S2A handoff should group verification into: (1) local parser/normalizer/transport policy and resource negatives; (2) real PostgreSQL refresh/upsert/finalizer/concurrency/Radar/Inbox behavior; (3) web/auth/OpenAPI and affected architecture/compatibility checks. Each focused command uses scripts/run-bounded-command.py, normal profile, at most 300 seconds and fresh ignored receipts. No full suite or external smoke is justified by this docs-only gate. Update the actual route manifest from 233 to the expected 235 only with exact generated schema/security/error evidence; two additional operations do not themselves prove contract compatibility.

For transaction proof, the existing test observes actual ingestion after Spring advice, source timestamp update and finalizer beforeCommit with PostgreSQL backend PID plus transaction ID. Its test-only REQUIRES_NEW control must fail the same identity assertion and establish the separate commit. Reuse that technique for RSS URL/type/enabled edits, staged insert/update/timestamp rollback and retained source locks, without production test seams. Actual RSS manager/socket/TLS/proxy/DNS negatives must exercise the configured connector, not only a mocked policy or a regex.

S2A then receives Antigravity implementation/tests and mandatory self-review, a separate Codex final review, Owner publication and brief closeout. Only afterward may S2B be confirmed/rechecked. Its exact fair scan, bounded cooldown, process-count/activation and shutdown choices remain intentionally pending. No fourth S1 remediation, inherited takeover permission, additional phase gate or mandatory benchmark project is created.

## Shared coverage ledger

PASS below means preparation/source compatibility and specified future proof, not implemented RSS acceptance. Runtime NOT_VERIFIED is explicitly assigned to future S2A acceptance.

| Dimension | Preparation status | Evidence / limit |
| --- | --- | --- |
| 1 intent; 2 traceability | PASS | All six files, capability table and S2A-01–12; S2B gated separately. |
| 3 architecture; 4 ownership | PASS | Matrix, P10/13, ADR-0016, module descriptor and source; no new edge/schema. |
| 5 tree | PASS | Recursive 103-file/25-package inventory, 16 test files, descriptors and package-tree comparison. |
| 6 shared contracts | PASS | Existing root envelope, local advice/DTOs, exact error/schema future gate; no shared module. |
| 7 SOLID; 8 principles; 9 patterns | PASS | Provider adapter + existing use cases/admission; no generic provider/job framework or duplicate Inbox behavior. |
| 10 domain | PASS | Reviewed URL representative/date/count rules, empty-feed success and preserved schedule metadata; S2A-05/06/10. |
| 11 API | PASS plan; runtime NOT_VERIFIED | Additive routes, exact old HN behavior, generated operation/error/security inventory; S2A-01/09. |
| 12 validation | PASS plan; runtime NOT_VERIFIED | URI/XML/namespace/encoding/field/body/event/depth limits and malformed-tail cases; S2A-02–06. |
| 13 auth/privacy | PASS plan; runtime NOT_VERIFIED | Bearer-only, no feed credentials, no item/content retrieval; S2A-01/02/11. |
| 14 secrets/egress | PASS feasibility; runtime NOT_VERIFIED | Actual-manager DNS API/source, direct route/TLS/proxy checks, no URL/XML logs; S2A-02/04/11. |
| 15 transactions | PASS plan; runtime NOT_VERIFIED | Existing REQUIRED shape/lock order; fresh RSS physical identity and URL-change rollback required by S2A-07/08. |
| 16 recovery | PASS plan; runtime NOT_VERIFIED | Atomic failure, owned cancellation/admission release and stated native DNS limit; S2A-03/07/08. |
| 17 persistence | PASS | Frozen Feed DBML/V1 and generic upsert compatible; fresh PostgreSQL proof still required. |
| 18 complexity/resources | PASS proposed plan; measurements NOT_VERIFIED | One document, byte/event/text/item/depth caps, shared capacity and nested deadlines; S2A-03/08. |
| 19 data/provenance quality | PASS plan; runtime NOT_VERIFIED | No synthetic rank/date; source-specific metadata and safe legacy fallback; S2A-05/06/09. ML retrieval N/A. |
| 20 format fidelity | PASS plan; runtime NOT_VERIFIED | RSS/Atom roots/namespaces/date fixtures and explicit relative-link exclusions; OCR/Office/RAG N/A. |
| 21 security tests | PASS specified; NOT_RUN | Actual connector negatives and local hostile XML in S2A-02–04, no unsafe live call. |
| 22 test quality | PASS plan; NOT_RUN | PostgreSQL barriers, shared-assertion negative control, fixed clock and exact web inventory; no count quota. |
| 23 benchmarks | N/A standalone benchmark | No performance claim; bounded-resource behavior must be measured by S2A-03/08. |
| 24 ML evaluation | N/A | No model, judge, held-out retrieval or recommendation capability. |
| 25 dependencies | PASS API/source applicability | Existing AWS/Apache/JDK dependency path, no POM/download; no new vulnerability/license audit claim. |
| 26 build/tooling | PASS plan; application checks NOT_RUN | Existing watchdog/source inspection, focused groups and architecture/HTTP gates. IDE inspection/static analyzer execution NOT_VERIFIED; no IDE-clean claim. |
| 27 observability | PASS plan; runtime NOT_VERIFIED | Categorical failures, truthful counters, safe logs and receipt separation; S2A-03/11/12. |
| 28 docs/governance | PASS with deferred LOW | Status/authority and links checked; resolutions above govern marked proposals; scoped ledger below. |
| 29 hygiene/delivery | PASS | Entry source clean, only preparation/status/review edits; no archive, schema, tool or historical-evidence mutation. |

## Finding register and Docs Sync Check

**DTR26-S2-PH-L01 — LOW / OPEN / NONBLOCKING / DEFERRED_OUT_OF_SCOPE.** The preexisting [S1 API error table](../../../../architecture/daily-trends-radar-api-extension.md) lines 112–113 says missing source uses NOT_FOUND and disabled source uses 409. Actual [FeedExceptionAdvice](../../../../../backend/src/main/java/com/vhvkhangg/personalprivatevault/feed/internal/web/advice/FeedExceptionAdvice.java) lines 27–28 and 95 returns 404 FEED_SOURCE_NOT_FOUND and 422 SOURCE_DISABLED. Reproduced by source/table comparison; no fresh HTTP run. A reader copying the table could write the wrong S2 expectations. Smallest repair: ChatGPT updates those documentation rows against unchanged source/accepted HTTP evidence in an approved docs cleanup, with a scoped link/contract check. Do not change production HN behavior to match the prose. This preexisting documentation drift does not block the independently specified additive S2A contract. Carry it to the final comprehensive non-RAG audit alongside all other actionable LOW/INFO.

| Docs Sync disposition | Paths / ownership / action |
| --- | --- |
| UPDATED | This review plus canonical README and acceptance-plan forward status; Codex review/status ownership only. Six original proposal files remain preparation, with explicit review resolutions linked from status. |
| ALREADY_CONSISTENT | ACTIVE=NO_ACTIVE_HANDOFF, actual baseline, published S1 closeout/acceptance boundary, Feed ownership/schema, S2A versus S2B exclusions. |
| DEFERRED_OUT_OF_SCOPE | PH-L01 above; older CURRENT notices in root/backend/docs AGENTS, Owner policy, Feed's historical Phase-10 deferrals, and backend-implementer's stale B1/B2 pending wording. Current Owner decisions/ACTIVE govern; ChatGPT/future authorized cleanup owns routine repairs. No repository-wide cleanup here. |
| DEFERRED_OUT_OF_SCOPE | Existing DTR26-FR-L01, UI26-FR-L02, SAD-FR-L01, CSA26-FR-L01 and older backlog; unchanged, not closed. Root personal-private-vault.zip was neither opened nor modified. |
| NOT_VERIFIED | RSS runtime/parser/transport/database/web resource measurements; all remain future mandatory handoff acceptance. No tests, Docker, provider fetch, QA/model/RAG worker, install or dependency resolution command was run. Only public specification/API documentation was browsed. |

Review artifacts are not Owner publication. No production code, test, migration, frozen contract, existing receipt/log, ACTIVE or Git index was changed. The maintenance route needs no numbered-phase predecessor/milestone gate. RAG remains suspended/deferred under its existing authority.

**Suggested commit:** docs(feed): prepare DTR26-S2A RSS refresh

**Next step:** Owner -> Codex | Owner commits/pushes the reviewed preparation, then invokes $codex-create-handoff for S2A only | DTR26-S2A preparation and this review | reviewed package published, current baseline checked, ACTIVE=NO_ACTIVE_HANDOFF | S2A NOT_STARTED/future 0/3; no takeover; S1 counters preserved.
