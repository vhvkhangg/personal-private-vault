**D23 CURRENT — 2026-10-21: D21 Markdown-only Slice 2 producer-local scope CLOSED by Owner, NOT YET OWNER-PUBLISHED.** Based on the [independent final closeout review](reviews/2026-10-21-slice-2-d22-01-independent-final-closeout-re-review.md), all MD17-01–12 remain VERIFIED and S2-D22-01 is CLOSED. See [D23](owner-decisions.md#2026-10-21--d23-bounded-phase-18-slice-2-markdown-only-owner-closeout) and the [bounded closeout](slice-2-markdown-only-d23-closeout-2026-10-21.md). Delivery is NOT READY_FOR_OWNER_COMMIT until exact hunk-level staging of the reviewed 34-change source candidate and independently verified docs scope; no bulk staging. D22 original captures remain NOT_RECOVERED. ADR-0024 accepted only for this slice. FR18-3 OPEN, indexing INACTIVE, Java publication and P18-FINAL NOT ACCEPTED, original Java gate FAILED, Slices 3/4 unauthorized.

**D22 controlling update — 2026-10-21:** The original D21 readiness and PROPOSED-ADR language below is preserved in its preparation context. [D22](owner-decisions.md#2026-10-21--d22-bounded-s2-d21-e01-evidence-disposition-and-adr-0024-adoption) **accepts ADR-0024 for Slice 2 applicability only** and explicitly disposes of S2-D21-E01 as a **bounded evidence gap, not restored bytes or preservation PASS**. The 2026-10-21 independent `CHANGES_REQUESTED` decision is historical and awaits a fresh independent [closeout re-review](slice-2-markdown-only-d22-closeout-rereview-handoff-2026-10-21.md). No CLOSED or READY_FOR_OWNER_COMMIT status.

# D21 governance requirement traceability — 2026-10-20

**Documentation-only. Current recommendation: READY_FOR_INDEPENDENT_SLICE_2_CLOSEOUT_REVIEW.**
No independent self-acceptance, Slice 2 CLOSED, commit readiness or Phase 18 acceptance is recorded.
Binding authority: [D21](owner-decisions.md#2026-10-20--d21-slice-2-obsidian-markdown-only-scope-rebase).
Current acceptance assignment: [scope/matrix](slice-2-markdown-only-d21-scope-and-acceptance.md).

## Modified requirements, precise effect and retained guarantees

| Requirement/reference | Earlier applicability | D21 prospective applicability | Verified basis / retained unresolved work |
| --- | --- | --- | --- |
| D15 G+H / Slice 2 G1–G3/H and original S2-01–08 review | Multi-format parser/OCR/chunker/embedding checkpoint | Current file acceptance is `.md` only; Markdown parsing/dependencies/chunking/embedding and finite complete-root output remain | All MD17-01–12 independently VERIFIED; original broad S2 findings/reviews untouched; excluded rows are not CLOSED/PASS |
| D16 Slice 2 vi/en OCR and D17 deferral notes | OCR mandatory vi/en; several other formats temporarily deferred | TXT/DOCX/PDF/XLS/XLSX/OCR explicitly **excluded** from Slice 2; future reintroduction requires another Owner Decision | Preserve every candidate/model/dependency experiment, failure and deferred-format implementation/test |
| D17 frozen static profile | Twelve mandatory syntax/source/provenance/all-or-nothing requirements | All twelve unchanged and mandatory | R03 machine matrix and D20 accepted result; no new weakened profile |
| D19 human package / D20 quality acceptance | D19 pending human gate, prospectively replaced by D20 | D20 remains governing final D17 quality acceptance; no human gate is reimposed | Original 193-file package/manifest and blank human forms unchanged; NOT_REQUIRED_UNDER_D20, not signed or fabricated |
| S2-07 / MD17-11–12 resources and cancellation | Mixed native OCR/parser/producer demands | Applicable Markdown producer controls remain required and credited; OCR-specific obligations excluded from this slice | Independently reviewed CPU/source/cleanup/interruption receipts, truthful device observations and bounded workload applicability preserved |
| S2-08 pipeline identity / AC-05–06 / FR18-3 | Frozen producer identity plus later Java output admission | Verified producer-local configuration remains mandatory; current Java rejection is retained as a **future FR18-3 publication obligation** | No unknown identity admission, compact-metadata validation shortcut or Java change; no actual PG publication claim |
| Original broad held-out/OCR program / AC-01–04 | Multi-format, source-family quality and format-specific locators | D17 static quality is accepted under D20's explicit independent-review rule; no claim that exposed fixtures became held-out or OCR passed | Broader Phase 18 quality obligations remain unaccepted; no extra Markdown gate is invented after independent closure |
| AC-05/08, original H, P18-OFFLINE-MODEL/RESOURCES | GPU/CPU, database and end-to-end resource requirements | Preserve actual CPU evidence and original H's honest GPU UNAVAILABLE/NOT_RUN option for bounded producer review | No GPU PASS inferred; native GPU/same-model fallback, PG and complete Phase 18 runtime/resource acceptance remain future gates |
| AC-07, ADR-0020/22/23 source authority, D14 | Frozen Foundation and accepted Slice 1 guarantees | Unchanged | No DB/schema/module/security/consent/snapshot/lifecycle modification; Phase 17 FROZEN and Slice 1/D14 CLOSED |
| P18-FINAL commands and historical full Java gate | Five final commands plus focused quality/model/publication/resource/report proof; failed aggregate run | Unchanged broader Phase 18 gate, not a current documentation-only Slice 2 implementation task | Java 1,433 / 1 failure / 2 errors / 0 skips remains FAILED; aggregate PostgreSQL investigation and repeat verification still required before P18-FINAL |
| ADR-0021/22/23 broader format clauses | Overall Phase 18 all-format/G2 targets and prior Slice 2 OCR note | Current Slice 2 applicability is explicitly annotated under D21; overall target is not silently removed | [ADR-0024](../../adr/0024-phase-18-slice-profile-acceptance-boundaries.md) is ACCEPTED prospectively under D22 for Slice 2 applicability only; broader targets remain unaccepted |

## Controlling/derived documents synchronized

Current D21 notices and canonical links are added to:

- [ACTIVE](../handoffs/ACTIVE.md), [Owner decisions](owner-decisions.md),
  [D17 contract](obsidian-markdown-d17-contract.md), [Slice 2 handoff](slice-2-parser-ocr-embedding-handoff.md),
  [Phase 18 index](README.md) and [streamlined slice plan](simplified-implementation-handoff.md).
- [Acceptance/AC map](acceptance-and-evidence.md), [quality/locator oracle](held-out-quality-and-locators.md)
  and [P18-FINAL](final-verification-gate.md): current per-slice assignment is qualified; the original
  overall obligations and final runner commands remain intact.
- [ADR-0021](../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md),
  [ADR-0022](../../adr/0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md),
  [ADR-0023](../../adr/0023-phase-18-application-level-snapshot-worker-trust.md) and the
  [ADR index](../../adr/README.md): applicability notices and explicitly PROPOSED clarification,
  with no broad contract clause silently deleted or declared approved.
- [RAG architecture](../../architecture/rag-architecture.md),
  [Phase 18 API/worker inventory](../../architecture/rag-phase-18-api-extension.md),
  [RAG phase plan](../rag-phase-plan.md), [roadmap](../../roadmap.md), repository/docs/implementation/
  handoff indexes, and the RAG runtime README: current slice scope/status is qualified while
  earlier plans and runtime history are preserved. No API, model/configuration or capability change.
- [Agent launcher prompt](../agent-launcher-prompt.md): current gate is independent documentation/
  closeout review; inherited broader implementation prompts are historical and not new authority.

The [machine-readable evidence](slice-2-markdown-only-d21-evidence-2026-10-20.json) lists **every
changed/new path and before/after hash**, plus the full documentation-reference inventory with
original line references. The broad search found 122 Markdown documents containing relevant
format/Slice 2 terms; a match is not automatically an editable current contract.

## References intentionally preserved

Historical preparation and takeover handbacks, original S2/D17/Phase 18 reviews, interpretations,
test-evidence receipts, D19 package/gold/fixtures and the standalone D20 acceptance record are
immutable evidence. The pre-existing Owner-decision text is retained beneath the additive D21
record, including D19/D20's truthful historical disposition. Slice 1 closeout/handoff, Phase 17,
other completed phases, repository/package-tree and earlier sequencing/deferred-decision plans
are not reopened by a scope-word match. Global AGENTS/skills policies are unchanged; explicit
current Owner authority and ACTIVE determine this task rather than stale historical launch text.

The search/reference inventory and current applicability notices identify old all-format statements
without mass-rewriting historical tests or asserting that they passed. P18-FINAL broad requirements
remain future unaccepted obligations. The Owner has not silently assigned excluded-format work to
Slice 3 or Slice 4; either overall retargeting or reintroduction would require a future explicit decision.

## Closure-readiness recommendation and stop

**Remaining Markdown-only Slice 2 producer-local blockers: NONE identified.** Existing D20 acceptance
and independent D17 proof satisfy the complete revised bounded scope. Recommend the
[proposed closeout](slice-2-markdown-only-d21-proposed-closeout-2026-10-20.md) for separate independent
review. This preparation neither independently accepts its own scope reconciliation nor declares
Slice 2 CLOSED/READY_FOR_OWNER_COMMIT. FR18-3, aggregate Java/PG final verification, overall Phase 18
quality/model/resource/lifecycle and P18-FINAL remain unaccepted and separately gated.
