> **Accepted prospective D24 amendment - 2026-10-21:** The Owner now chooses the **overall Phase 18 `.md`-only target**, supplying the overall-format decision left open by D21/D22/ADR-0024. The exact [applicability table](../implementation/phase-18/slice-3-markdown-scope-and-baseline-2026-10-21.md#prospective-applicability-reconciliation) and [D24](../implementation/phase-18/owner-decisions.md#2026-10-21--d24-overall-markdown-only-phase-18-and-slice-3-verified-indexing) prospectively supersede conflicting format/OCR and future-preservation clauses below. D17 Markdown fidelity/security/child grants, actual producer validation, Java/PG atomic publication, fencing/recovery, offline/model/resources and lifecycle/final verification remain. New initial Slice 3 D+F is authorized only through ACTIVE; Slice 4 implementation is deferred, with one required excluded-format cleanup at its end before P18-FINAL. No activation or acceptance follows. Historical clauses/decisions remain unchanged below; ADR-0020/Phase 17 remain frozen.

# ADR-0024 — Explicit Phase 18 slice-profile acceptance boundaries

**Status: ACCEPTED prospectively for the D21 Slice 2 Markdown-only applicability only, by Owner D22 on 2026-10-21.**
This is not an approved replacement of the overall Phase 18 feature set. The Owner has already
approved **Slice 2 `.md`-only scope through D21**; this accepted qualification makes its relationship to broader
frozen contracts explicit. No production, SQL, dependency, API or runtime activation follows.

## Context and precise conflicts

| Source clause | Existing obligation / conflict if treated as a current Slice 2 gate | D21 authority and retained scope |
| --- | --- | --- |
| ADR-0021, D18-A.4 and release/implementation gates | Document/parser/OCR/model tuple and full Phase 18 source/quality integration; linked broad parser target can be read as requiring all formats before any slice closes | Slice 2 `.md` producer-local evidence is accepted separately; the complete protocol tuple, Java validation and overall release guarantees remain |
| ADR-0022, “Acceptance, consequences and remaining gates”, G2; FR18-3 producer/finalization section | Every required format/language/OCR/table case; actual producer provenance/publication mandatory | No non-MD requirement belongs to current Slice 2; broader Phase 18 G2 and FR18-3 remain unaccepted and are not auto-closed |
| ADR-0023, “Mandatory user-visible feature set (not relaxed)”, including D16/D17 applicability notes | Original TXT/MD/DOCX/PDF/XLS/XLSX/OCR target; D16 explicitly calls vi/en OCR mandatory for Slice 2; D17 only deferred broader acceptance | D21 prospectively replaces the Slice 2 format assignment with `.md` only, superseding that Slice 2 OCR obligation. Original feature-set acceptance is not asserted |
| AC-01/02/04 and FCNC-08/09/10, IS-23/24/25 | Broad format/OCR/geometry acceptance rows | Current per-slice applicability is the D21 matrix; older all-format rows remain Phase 18 reference requirements, not fictional Slice 2 passes |
| P18-FINAL “Linked minimal focused evidence”: P18-QUALITY and P18-OFFLINE-MODEL | Multi-format/held-out/OCR and GPU/model final evidence | No unchanged broad final gate is executed or waived; no new all-format promise is silently dropped from P18-FINAL |

These are **acceptance-assignment conflicts**, not changes to database logical schema, module
ownership/dependency direction, repository/package structure, authentication, storage strategy,
source authority or frozen Phase 17/Slice 1 guarantees. No DBML/diagram migration is required.

## Accepted exact prospective qualification — D22 (Slice 2 only)

The Owner explicitly **accepts** the following limited qualification under D22; the original higher-ADR clauses remain in place, with additive applicability notices rather than retroactive edits:

> A separately Owner-approved slice profile determines that slice's mandatory input formats and
> quality evidence. Under D21, Phase 18 Slice 2 admits only Obsidian Markdown `.md` acceptance,
> retaining all MD17-01–12 obligations and D20's accepted independent evidence. TXT, DOCX, PDF,
> XLS, XLSX and OCR are excluded from Slice 2 acceptance and require a future explicit Owner
> Decision before reintroduction. A slice's closure does not satisfy the overall Phase 18 format,
> Java producer-validation/publication, offline GPU/fallback, resource, lifecycle or final-gate
> requirements. Retained historical failures are never reclassified as passes.

For the **overall Phase 18 format obligation**, the Owner would later need explicitly to choose
either a separately approved Markdown-only Phase 18 target or a separately authorized return of
additional formats. **Neither choice is made or authorized by D21/D22/ADR-0024.** This accepted qualification
does not assign additional implementation to Slice 3/4 or change P18-FINAL's commands/thresholds.

The already authorized D21 slice boundary is documented in current applicability notices; the
older ADR clauses themselves are preserved. This **accepted prospective qualification** resolves only D21's higher-contract *slice-applicability* conflict. It is not evidence that overall Phase 18 is complete; the future overall-format target still needs a separate explicit Owner decision before Phase 18 acceptance. Slice 2 closeout additionally depends on D22's bounded evidence-loss adjudication and independent re-review.

## Alternatives and consequences

- Requiring every historical format for Slice 2 would contradict the Owner's D21 scope decision.
- Silently deleting broad Phase 18 requirements would exceed the Slice 2-only authorization.
- Explicit per-slice applicability plus this accepted higher-level qualification preserves both
  the binding Markdown-only scope and the unaccepted broader contract.

Existing format code, tests, models/dependencies, reviews and failed receipts remain untouched.
D17/D20 acceptance and independently CLOSED findings stand. FR18-3, external activation and
P18-FINAL remain unaccepted. Phase 17 FROZEN; Slice 1/D14 CLOSED; external indexing INACTIVE;
historical Java full verification FAILED; Slices 3/4 unauthorized. No commit or publishing.

Authority and traceability: [D22](../implementation/phase-18/owner-decisions.md#2026-10-21--d22-bounded-s2-d21-e01-evidence-disposition-and-adr-0024-adoption), [D21](../implementation/phase-18/owner-decisions.md#2026-10-20--d21-slice-2-obsidian-markdown-only-scope-rebase),
[scope/acceptance matrix](../implementation/phase-18/slice-2-markdown-only-d21-scope-and-acceptance.md),
[traceability](../implementation/phase-18/slice-2-markdown-only-d21-governance-traceability-2026-10-20.md).
