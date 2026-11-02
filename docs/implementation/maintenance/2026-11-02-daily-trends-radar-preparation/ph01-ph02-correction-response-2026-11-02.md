# DTR26-S1 preparation — PH-01/PH-02 bounded correction response

**Date:** 2026-11-02  
**Baseline:** Owner-published main `6da26d4a457f3465e8b43489b95202d886787036` (subject to fresh Codex HEAD check).  
**Review:** [2026-11-02 independent pre-handoff review](reviews/2026-11-02-independent-pre-handoff-review.md) = `CHANGES_REQUESTED`, PH-01/02 MEDIUM/OPEN. This response **does not** overwrite or supersede the review; only Codex re-review can close it.  
**Mode:** documentation correction only; `NO_ACTIVE_HANDOFF`; implementation `0/3` NOT_STARTED, takeover NOT_AUTHORIZED.

## Owner-confirmed identity decision (PH-01)

- **Decision date and statement:** 2026-11-02. The Owner explicitly stated: “Tôi xác nhận chọn ARTICLE_URL cho DTR26-S1.” This is an affirmative Owner selection, not an assistant-inferred default.
- **Selected/binding S1 design:** `ARTICLE_URL` — **one FeedItem per exact trimmed external article URL**. Deduplicate identical stored article URLs before generic ingestion; representative is newest valid HN submission time, then greatest HN item ID on a tie. Normalize `externalId=null`, keep representative HN ID/discussion/rank/score/capture time as bounded provenance; existing Inbox operation saves the original article URL.
- **Rejected alternative for S1:** `HN_DISCUSSION_URL` would store one FeedItem per story and cause Inbox to save an HN discussion link. It is **not Owner-selected** and must not be silently mixed into the S1 contract.
- **Review status:** Owner choice is now **RECORDED/CONFIRMED** in `owner-scope-and-decisions.md`; independent Codex re-review must still verify PH-01 collision/upsert and PH-02 transactional source authority before returning `READY_FOR_HANDOFF`. No implementation permission follows from the choice alone.

## PH-01 correction mapping

| Contract | Proposed bounded resolution | Proof required in S1 |
| --- | --- | --- |
| Unique keys | Existing per-source URL-hash; `externalId=null` on S1 normalization; no new table/constraint/hash algorithm | Two HN IDs, one identical URL → one stored FeedItem, no batch rejection |
| Representative | Greatest valid HN submission `time` seconds, then greatest `id` on ties; rank captured from selected top-list position | Identical URL with changed winner across refreshes; persistent FeedItem identity and updated provenance |
| Existing/preexisting rows | By-hash upsert handles prior `hn:<id>` external ID; same HN story with changed URL becomes a different article | Real PostgreSQL regression for legacy alias and changed URLs |
| Missing article URL | Skip from article-only Radar; no mixed HN-discussion fallback | Jobs/dead/deleted/Ask HN/malformed record distinctions and safe skip/fail policy |
| Save to Inbox | Existing Feed Item save copies **article URL** and metadata at time of save; global URL-hash conflict preserved | Exact saved URL and existing duplicate conflict; no automatic resync |

## PH-02 correction mapping

| Risk | Proposed bounded mechanism | Evidence |
| --- | --- | --- |
| Source changes while HTTP runs | Source HN/enabled **preflight** is not authority; no database transaction/lock during HTTP | Fake HTTP barrier permits concurrent source type switch/disable to commit |
| Finalization | A **separate Spring-proxied owner-local `@Transactional` finalizer** enters one transaction; calls **unchanged** `FeedItemOperations.ingestFetch` with REQUIRED propagation; generic ingestion obtains row lock under `findByIdForUpdate`/refresh | Assert one transaction + PostgreSQL lock persists through finalizer commit |
| Final authority | Re-read locked authoritative source after ingest staging, verify both `enabled=true` and `type=HACKER_NEWS` immediately before finalizer return; mismatch throws, **entire transaction rolls back** | HN→RSS and disable cases produce no new/updated items and no fetch-time change |
| Lock order | Keep existing generic `item upsert → source lock` order; **do not pre-lock source** before generic ingest or create new transactions | Concurrent generic ingestion remains viable; deterministic tests do not deadlock |
| Admission/cleanup | Single-JVM bounded per-source admission released on every path, not a durable distributed lease | Same-source busy response; other sources progress; cleanup on exception/cancellation |

If actual transaction proxy/proper propagation or locked-state recheck cannot be guaranteed, the independent reviewer must return `CHANGES_REQUESTED` or `OWNER_DECISION_REQUIRED` for the smallest proven alternative; **never** substitute an unlocked check-before-ingest.

## Other reviewer observations addressed

- S1 GET restricted to **enabled HN sources**, cached rows with a fixed rolling 24h **HN submission** time, deterministic order and database limit; no `hours`/calendar-day knob; no claims of globally current rank.
- Selected story `title` is HN-provided HTML-shaped untrusted data; no content execution or external article body download.
- Bounded network requests and actual-connect DNS/peer enforcement remain **future implementation validation**. Preparation has not run HTTP, PostgreSQL or provider tests.
- Generic `ingestFetch` stays accepted; no source/table/ADR/migration/schema/POM/other module changes authorized by this correction.
- Explicit acceptance negatives revised in `acceptance-and-review-plan.md` (DTR-01,04–10). All DTR-01–12 are still NOT_RUN.

## File preservation

This package intentionally does **not** replace `README.md`: the independent reviewer already changed its current disposition to `CHANGES_REQUESTED` in the Owner's working tree, and that local reviewed version was not supplied for byte-accurate replacement. **One consistency edit is still required in the current local README**, without replacing the reviewer's status/history: replace the old S1 capability sentence claiming that a top story can be shown with no external article URL with the exact current rule: **“DTR26-S1 presents only live HN story records with a valid HTTP(S) external article URL; text-only/Ask HN entries or entries without such an external article URL are skipped, and Save for Later saves the external article URL.”** Preserve `CHANGES_REQUESTED` until Codex issues the new independent disposition; then update the README status/next action in the reviewer-owned preparation update. The local README must refer to the **Owner-confirmed `ARTICLE_URL`** decision. Preserve all dated reviews as immutable history, and do not overwrite the current README with the original pre-review archive version. The package contains only the four proposal documents plus this response; production, ACTIVE, scripts and accepted historical evidence remain untouched.

**Next step:** Owner | apply Owner-confirmed `ARTICLE_URL` corrected proposal files, preserve/reconcile current local README, then rerun Codex `$codex-pre-handoff-review` | DTR26-S1 and immutable original independent review | Owner decision recorded; PH-01/PH-02 remain MEDIUM / OPEN until independently closed; `NO_ACTIVE_HANDOFF`, implementation 0/3, takeover unauthorized.
