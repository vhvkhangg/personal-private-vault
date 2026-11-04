# DTR26-S2 — Independent preparation and future acceptance matrix

**Status:** **READY_FOR_HANDOFF (DTR26-S2A ONLY)** under the [independent preparation review and its explicit proposal resolutions](reviews/2026-11-04-dtr26-s2a-independent-pre-handoff-review.md). Future checks are **NOT_RUN**. The review inspected local source/dependency APIs and public specifications; it does not claim executable quality, provider availability or approved S2 implementation.

## Preparation gate (Codex `$codex-pre-handoff-review`)

- Select **explicit Owner-approved maintenance/feature-preparation mode**, not numbered-phase or historical Phase 15/RAG mode. Verify actual `main` SHA/clean worktree, `ACTIVE=NO_ACTIVE_HANDOFF`, S1 acceptance and current source applicability.
- Inspect all six new Markdown files and all applicable current Feed source, item, infrastructure, web, exception, test and package trees; include immutable prior S1 FR-05 physical-transaction proof/negative control, S1 API, current P10/13 contracts, dependency matrix, root `AGENTS.md`, Feed `AGENTS.md`, current Owner delivery policy and Docs Sync requirements. Do not rewrite past reviews, handbacks or frozen ADR/DBML.
- **Resolve design decisions without guessing:** public-host TLS/DNS/redirect/XXE feasibility in actual dependency graph; article-URL identity and deterministic collision behavior; HN-only backward compatibility versus additive RSS/combined Radar; source `feedUrl` authority through commit; finite caps/deadlines. Mark proposal changes with `KEEP/REUSE/SIMPLIFY/DEFER/REJECT/OWNER_DECISION_REQUIRED` plus minimum verification.
- If source-level or security contracts cannot be met, return **`CHANGES_REQUESTED`** or **`OWNER_DECISION_REQUIRED`** with one finite list. Only when all S2A requirements are fully scoped may reviewer write **`READY_FOR_HANDOFF (DTR26-S2A ONLY)`**, with a dated review under this directory's `reviews/`. S2B remains prospective; no review result is implementation acceptance.
- No new code, migration, schema, dependencies, local provider network call, Docker, real external smoke, QA, RAG or model execution in preparation. Review may correct approved design docs, not create an active handoff. No remediation rounds consumed by preparation.

## DTR26-S2A future acceptance (mandatory only after explicit executable handoff)

| ID | Mandatory scenario and negative evidence | Data / location of proof |
| --- | --- | --- |
| S2A-01 | Source CRUD/enum/frozen schedule semantics unchanged; RSS manual endpoint authenticated and only RSS enabled sources eligible. Missing, disabled, HN, unsupported, malformed target/parameter cases get stable bounded safe errors. | Focused Feed web real Spring + source integration tests and actual OpenAPI. |
| S2A-02 | HTTP actual-peer SSRF admission: HTTPS public host/443 only, validated mixed A+AAAA, rebinding, proxy/redirect disabled, TLS hostname identity; malicious metadata URL never requested. No second uncontrolled DNS resolution, fallback or per-item article fetch. | Injected resolver/client tests, unsafe destinations mocked; never issue real unsafe calls. |
| S2A-03 | Bounded one-document fetch: 1 MiB proposal, deadline, encoding, body/depth/item/text budget, no response retries; abort and release on timeout/oversize; no unbounded parser allocations or leaked worker threads. | Deterministic fake transport, clock/deadline and parser resource-limit tests; measured caps in receipts. |
| S2A-04 | Hardened RSS 2.0/Atom 1.0 XML parser; DTD/XXE/DOCTYPE, external entities, malformed namespaces, compressed bombs, extension/attachment dereference and malformed XML denied. Valid empty feed distinguished from failure. | Local hostile XML fixtures; zero file/network access by parser, finite bound assertions. |
| S2A-05 | Identity collisions same URL/different RSS GUID or Atom IDs in one document; deterministic newest representative/ties; across refresh URL change; existing legacy externalId row; no changed generic ingest/unique keys. | Real PostgreSQL upsert/unique and failure rollback tests; exact saved link assertion. |
| S2A-06 | RSS/Atom dates: pubDate/published only, missing dates stay per-source but not fabricated as published; Atom updated-only not treated as publication; UTC 24h inclusive boundary, future-date exclusion, HTML/title-safe string data. | Fixed-clock parser and PostgreSQL read fixtures. |
| S2A-07 | RSS fetch state changes mid-flight: disabling, RSS→HN, changed `feedUrl`, source update allowed during HTTP; finalizer same physical transaction/lock and rollback of staged inserts/updates/fetch timestamps; `REQUIRES_NEW` test-only negative control detects illegal independent commit. | Fake HTTP barrier + real PostgreSQL transaction/backend PID+XID proof after Spring advice, no production test hook. |
| S2A-08 | Shared single-flight and global capacity across manual HN/RSS, timeouts/interruption/unsafe XML release, no DB lock over network, source owner public module boundaries. | Deterministic concurrency barriers and architecture checks. |
| S2A-09 | Additive cached combined Radar HN+RSS only, deterministic SQL cap, no network on GET; RSS `sourceRank=null` and HN-only `/radar` unchanged; null/legacy provenance handled safely. | Real DB+HTTP tests; current HN fixture diff and strict actual generated OpenAPI including `ApiResponse`/ErrorResponse, operationIds and security. |
| S2A-10 | Existing HN manual refresh, HN rank metadata, Feed source/item, Save-to-Inbox duplicates, UI26 paging and global search/Knowledge conversions still work within unchanged contracts. | Focused source-applicable regressions and changed-tree/packaging evidence. |
| S2A-11 | No schema/DBML/migration/POM/auth/root API changes unless separately authorized; source exposure, privacy-safe logs, no content/attachment downloads, package-info and package-tree documentation coherent. | Diff hygiene, repository architecture/test/HTTP contract receipts, Docs Sync and self-review. |
| S2A-12 | Implementer self-review + independent **separate** Codex `$codex-final-review`; all required cases PASS with exact commands/counts/limits, issued `READY_FOR_OWNER_COMMIT` before Owner git publication. | Dated handback and independent Codex review; no agent publishing. |

Use existing `scripts/run-bounded-command.py`, normal **300-second** cap per focused command and unique ignored receipts. Avoid indiscriminate full-suite tests and suspended RAG/QA. Full suite may be separately authorized only for demonstrated integration risk. Reviewer must distinguish 1) implementer-selected tests, 2) fresh independent tests and 3) source-applicable reused evidence; no double counting or inflated confidence.

## DTR26-S2B future acceptance (not executable yet)

| ID | Prospective gate |
| --- | --- |
| S2B-01 | Scheduler component/tick globally OFF by default; no outbound requests at startup unless global and source opt-ins; default single-instance assumption documented. |
| S2B-02 | Due predicate, source type filtering, caps, ordering, pagination fairness; no starvation behind permanently failed/busy/unsupported first rows. |
| S2B-03 | Shared per-source/global admission across HN and RSS, no duplicate manual/scheduled executions, bounded executor and clean shutdown; no DB transaction/lock across HTTP. |
| S2B-04 | Scheduled final authority verifies enabled/type/scheduled-flag and RSS URL under row lock through same commit; interval edits use latest value; failure leaves `last_fetched_at` and `next_fetch_at` unchanged. |
| S2B-05 | Safe cooldown and finite retry rate after network failures; failure flood/restart behavior accurate; no hidden cross-instance lock/lease claim. |
| S2B-06 | HN manual API/RSS manual API, cached GET, Feed CRUD and Vault/Inbox behavior remain unchanged; no new migration/framework/unauthorized RAG; independent tests/review/Owner publication. |

**Owner gating:** S2A acceptance and publication do **not** authorize S2B execution. Require an Owner confirmation, exact current baseline, independent re-check of scheduler scope and one separately executable `$codex-create-handoff` contract. Each implementation slice gets its own remediation 0/3 and independent final review, not S1's exhausted 3/3 allowance.

## Forward status and unresolved ledger

`S2A: READY_FOR_HANDOFF / NOT_IMPLEMENTED` ; `S2B: PROSPECTIVE / NOT_AUTHORIZED_FOR_IMPLEMENTATION`; `ACTIVE=NO_ACTIVE_HANDOFF`. New `DTR26-S2-PH-L01`, S1 `DTR26-FR-L01` and all prior non-RAG LOW/INFO remain open for approved cleanup/final exhaustive audit, not silently closed by S2. RAG Phase 19 S19-2 remains suspended/archival; no active worker or QA evaluation.

**Next step:** Owner -> Codex | Owner commits/pushes the reviewed preparation, then invokes `$codex-create-handoff` for S2A only | DTR26-S2A preparation + independent review; S2B prospective | reviewed package published, current baseline checked, `ACTIVE=NO_ACTIVE_HANDOFF` | S2A NOT_STARTED/future remediation 0/3, no takeover; S1 counters preserved.
