# P19 — Owner-confirmed requirements and phase boundary

**Date / authority:** Owner's direct choices during the 2026-10-27 Phase 19 preparation discussion; this is a prospective **scope record**, not a new numbered Phase 18 decision, accepted ADR amendment or implementation authorization. Cross-check with [current policy](../../workflow/current-owner-delivery-policy.md).

## User-visible MUST requirements

| ID | Owner-confirmed requirement | Concrete behavior / boundary |
| --- | --- | --- |
| R19-01 | Q&A about ordinary/Obsidian `.md`, including Ideaverse via generic import | No Ideaverse-specific parser/module, no web/GitHub sync in P19. Phase 18 immutable source/snapshot and approved children only. |
| R19-02 | Directly documented facts only | Summarize or combine explicitly stated facts across files; **no speculative simulation, inferred winner/power scaling, new lore or answer from model memory**. |
| R19-03 | Evidence and grounding | Name source file, heading and precise locator/line where genuinely available, show supporting excerpt. All substantive answer claims must be supported, not merely accompanied by a generic link. |
| R19-04 | Insufficient/partial/conflicting evidence | Abstain on missing facts, answer independently supported parts and identify missing parts; show conflicting statements with citations, never silently choose last-edited. Closest matching passage is not proof. |
| R19-05 | Multilingual | Default Vietnamese; if user asks in another language answer in that language; **preserve exact names/terms** and source identities. Test Vietnamese, English and at least one Han-script case as actually applicable. |
| R19-06 | Conversation context | Owner-controlled client may send **bounded recent turns** with each query. Server need not persist chat/session DB; no automatic memory or cross-session content. Ignore untrusted conversation instructions to override permissions. |
| R19-07 | Query source selection | Default **all eligible, approved, correctly indexed Markdown** (subject to snapshot/dependency/currentness proofs); owner may select narrower file/folder/source IDs. No implicit filesystem scan on query. |
| R19-08 | Ideaverse canonical source semantics | For an **Ideaverse canon-mode request**, default **explicitly Owner-labeled, approved `CORE/` source IDs** only; require a known revision-bound roster rather than inferring folder identity from stored names/digests. Non-CORE source IDs only on explicit noncanon opt-in and labeled as such. Every contributing embedded child must itself pass the query evidence selection; empty/template/waypoint-only text is not lore. See [selection contract](query-consumer-lifetime-and-selection.md#canonsource-selection-contract-p19-pr-03). |
| R19-09 | Current, consented snapshot authority | Use explicit approved selected revisions even if live originals have changed on disk **only if the accepted source/generation remains `CURRENT`**. Manual recapture/reindex for new content; never bypass revocation, version mismatch or failed integrity. |
| R19-10 | Privacy by **source authority**, whole-file Markdown consent and offline inference | Protect/authenticate single Owner, no cloud AI or runtime model fetch; deliberate checksum/license-checked one-time install. **Deny unapproved/revoked/unknown source-level authority, invalid/currentness/child, wrong scope.** For a separately approved/current external `.md` **WHOLE_FILE** snapshot, ADR-0022 N6/WF-1/WF-6 permits internal `private`/`nsfw`/hidden markers and metadata **as part of the authorized full file**; these markers alone do not veto/redact. Parent approval never approves a child. Fiction/Film field-level rules are a distinct excluded P19 source class. See [paired proof](query-consumer-lifetime-and-selection.md#whole-file-consent-versus-source-level-classification-p19-pr-02). |
| R19-11 | Initial performance/resource choice | Existing Windows 11 desktop: i5-9600K, 16 GiB class RAM, RTX 2070 8 GB (unchanged). **CPU-first; do not require GPU**. Prefer correct answers over speed; >30 seconds acceptable, but each request/cancel/resource remains finite. |
| R19-12 | Model approach | Compare at most **two** practical CPU-capable LLM candidates on the approved fixture set; choose model and pinned runtime/checksum **after** evidence. Reuse one local instance during a bounded Q&A session where safe; no fleet of model servers. |
| R19-13 | Verification | ~20–30 independently verifiable question cases + deterministic privilege/source/citation tests, with Codex examination of a small set of actual responses. No automatic LLM judge or mandatory human signature. |
| R19-14 | No long-term chat content or sensitive logs | Do not store conversation, raw prompts, raw answers or source excerpts in application DB/operational logs by default. Only safe statuses/errors/provenance identifiers when necessary. |
| R19-15 | Enablement | Owner permits **bounded testing** of import/index/Q&A with expressly approved Markdown samples, not a blanket production external indexing activation. Feature stays disabled until separately reviewed/authorized. |
| R19-16 | Execution order | Backend/API before Frontend (deferred Phase 16); no chat UI required in Phase 19. Fiction/Film recommendations remain Phase 20, not bundled with Phase 19. |

## Source semantics: generic Markdown versus Ideaverse

- **Generic Q&A:** default all properly indexed, approved Markdown sources under actual authorization; no Ideaverse `CORE/` filter imposed on unrelated Markdown folders.
- **Ideaverse canon Q&A:** Owner supplies and retains a versioned, reviewed, **source-ID/selected-revision-bound roster**, with explicit collection identity and `IDEAVERSE_CORE`/`IDEAVERSE_NONCORE` labels from manual import review. A canon query supplies that roster or a validated narrow source-ID selection; if missing/stale/ambiguous, fail closed rather than silently search globally. The backend intersects it with fresh authoritative RAG consent, currentness and complete contributing provenance **before** rank/context/metadata. No invented server-side folder classifier or new Ideaverse database module. See [selection contract](query-consumer-lifetime-and-selection.md#canonsource-selection-contract-p19-pr-03).
- **Ideaverse noncanon/transclusions:** `DOCS/` design/decision records, `IDEAS/`, `SANDBOX/`, `REFERENCE/` and `TEMPLATES/` excluded from canon by default. A CORE parent **does not promote** transcluded non-CORE child evidence; non-CORE parent likewise does not promote its CORE child. Mixed-provenance chunks with no provable per-span selection are **excluded in entirety**. Explicit noncanon opt-in is labeled outside canon, never silently merged with `CORE` truth. Duplicate basenames remain separate source IDs.
- Future independent Ideaverse module is **read-only frontend** rendered from public GitHub repository (Git remains source of truth); manual read-only sync/pinned Git commit is a possible **later design**. It is **not authorized for P19**. Current test data is manually approved local `.md`, not an implicit network fetch.

## KEEP / REUSE / SIMPLIFY / REMOVE / DEFER

| Choice | Disposition | Rationale |
| --- | --- | --- |
| P17/P18 consent, immutable snapshot, dependencies, `CURRENT` generation, ordered mutation guards | **KEEP/REUSE** | Approved protections remain binding; point-in-time index currentness does **not** protect Q&A model or HTTP lifetime. P19 proposes one [durable source-scoped consumption exclusion](query-consumer-lifetime-and-selection.md); its bounded **prospective design** is **P19-OD-01 option A OWNER_APPROVED**, with implementation and actual concurrency proof still gated. |
| P18 approved Markdown parser/chunker/384-dim ONNX embeddings/Postgres pgvector | **REUSE** | No reparse, re-embedding or vector-space switch solely to implement query. |
| Small scoped PostgreSQL exact vector top-K | **PREFER / PROVE** | Corpus is small; no second vector DB or ANN index until measured. Add lexical matching only if important fact lookup fails baseline tests. |
| One pinned local CPU-capable generation model + minimal prompt evidence packaging | **NEW / SIMPLE** | Actual Q&A not implemented in P18. Test model quality before freezing dependency. |
| Model-generated free-form SQL, autonomous agents, source discovery watcher, arbitrary tools | **REMOVE / OUT OF SCOPE** | Not requested; unnecessary trust surface. |
| Hybrid fusion, reranker, multiple retrieval retries, LLM-as-judge, dashboard | **DEFER** | Only revisit on verified failure and Owner-scoped need; not mandatory from older historical plan alone. |
| OCR, PDF, DOCX, TXT, XLS/XLSX, GPU inference | **OUT OF SCOPE** | P18 accepted `.md` CPU-only; no reintroduction via P19 preparation. |
| Fiction/Film Q&A and recommendations | **DEFER** | Owner confirmed P19 Markdown Q&A only; recommendations Phase 20, no domain-source expansion for P19. |
| Ideaverse frontend, database schema/module, git polling/webhooks | **DEFER** | Separate later workflow; owner knows repo is public. |

## Explicitly rejected mistaken implications

- Large model ≠ necessarily more truthful. Citation presence ≠ support. Recent timestamps ≠ canonical authority. Approved import ≠ perpetual read permission. Approved *test* activation ≠ production indexing enablement.
- A multi-file factual union is acceptable; introducing a new unstated conclusion is not.
- Prompt text, Markdown source and client-supplied history are **untrusted data**; they do not authorize a path, query scope, secrets access or model instruction changes.

## Owner decisions and remaining feasibility gates

**User-visible behavior and P19-OD-01 are resolved:** On 2026-10-27 the Owner approved **option A**, the minimum prospective durable active-reader exclusion integrated with existing authoritative source guards plus strictly necessary additive reader storage/SQL guarding if safe reuse cannot prove the lifetime. See [Owner-approved decision](owner-decision-p19-reader-exclusion.md). This is not source-change permission or Codex acceptance. **Independent pre-handoff re-review is still required**, and any **wider** delta requires a fresh Owner decision. Model/runtime choice and finite deadlines require real later measurement; other frozen/security amendments, new formats or production activation also need their own separate approval.
