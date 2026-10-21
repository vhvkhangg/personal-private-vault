# D22 — S2-D21-E01 missing-capture disposition and bounded ADR approval

**Date:** 2026-10-21. **Authority:** explicit Owner approval of the proposed documentation-only remedy. **Status:** `OWNER_DISPOSITION_ACCEPTED_WITH_EVIDENCE_GAP / READY_FOR_INDEPENDENT_CLOSEOUT_RE_REVIEW`. This is **not** a recovery receipt, an independent re-review result, formal Slice 2 closeout or an owner commit authorization.

## 1. Incident and irreplaceable original-byte gap

The [2026-10-21 independent closeout review](reviews/2026-10-21-slice-2-markdown-only-independent-closeout-review.md#s2-d21-e01---high---historical-evidence-overwritten-by-this-review) determined that its **own fresh pytest rerun** reused a historical fixed output directory, overwriting three JSON files under `logs/phase18/d17-r03-independent-2026-10-20/`. The actual source files, parser/chunker/model identities and verified implementation behavior were not changed by the incident. The three missing historical captures are:

| Historical raw filename (under the above directory) | Original SHA-256 preserved in historical ledger | Current status |
| --- | --- | --- |
| `fresh-producer-bound-True.json` | `48b1383e46bbf508bd25faa1075db857d1daac018efc8ccd4bef708a16bcc0b0` | Exact original bytes missing; overwritten |
| `fresh-producer-bound-False.json` | `2909e393b18648b3b4b4ad3bf77b35e6e2ca1d7693505aabf022af99c26f72e1` | Exact original bytes missing; overwritten |
| `fresh-negatives.json` | `3727b91410965e67df87074d0384e12a86e2f4e3d013cf056051845ae1ed91e4` | Exact original bytes missing; overwritten |

**Do not** edit the old manifests, historical review, old hashes, command receipts or tests to pretend these bytes were recovered. The reviewer had verified original hashes **before** its rerun; after overwrite, preservation **FAILED**. Current rerun files have different bytes/hashes, and their correct destinations and provenance are recorded in the 2026-10-21 independent evidence ledger. At present no trustworthy exact-byte recovery is evidenced in the provided `docs/` package; the reviewer found no identical backup locally and reported the Windows shadow-copy inventory unavailable. This is **not** a claim of exhaustive backup search. If an authentic future backup becomes available, restore originals with the exact above hashes and append a truthful recovery receipt; do not retroactively alter the D22 disposition.

## 2. Available substitute evidence and explicit gaps

- The independently inspected D19 documentation projection preserves all **52** relevant cases' source texts, structural/locator metadata, statuses/reasons, chunk projections and embedding-value digests. Its equality comparison with the rerun's content was reported by the reviewer, but it **cannot reconstruct the missing original byte strings** or host/resource measurements intentionally absent from D19.
- Earlier trusted original receipt/log digests, frozen source hashes, accepted independent D17/R03 reviews and reviewer-scoped new JSON captures remain traceable. An independent 25-case current real-CPU/JSON test rerun **PASSED** on the unmodified candidate; that does not prove the original three bytes remained intact.
- This package contains documentation and the independent reviewer **assertions** about runtime receipts, not the entire untracked `logs/phase18/` tree or deployed source. Do not describe a docs-only inspection as an independent execution of producer tests or recovery of the originals.

**Owner decision:** the *known* three-file historical raw-capture gap is accepted **only as a bounded evidence exception for determining D21 `.md` producer-local Slice 2 closeout readiness**. Existing independently verified source fidelity/structure and regression proof may be relied upon to the documented extent. Original-byte preservation remains marked **FAILED / NOT_RECOVERED**, and lost original machine/resource telemetry is **NOT_RECONSTRUCTABLE**. This acceptance does not apply to other evidence corruption, create a generic historical-preservation waiver or certify full repository/Phase 18 integrity. An independent reviewer must assess whether this narrowly stated substitution is adequate before formal Slice 2 closeout.

To prevent recurrence, independent reruns should write to a fresh uniquely named output directory, inspect any hard-coded destinations first, verify that historical assets remain immutable, and report post-run before/after hashes. This is documentation guidance, **not a modified script or an already executed safeguard**.

## 3. ADR-0024 adoption, exact scope

[D22](owner-decisions.md#2026-10-21--d22-bounded-s2-d21-e01-evidence-disposition-and-adr-0024-adoption) accepts [ADR-0024](../../adr/0024-phase-18-slice-profile-acceptance-boundaries.md) as a **prospective slice-profile applicability qualifier**, strictly for the D21 Markdown-only Slice 2. Its precise qualification is adopted without loosening all twelve MD17-01–12 requirements. TXT/DOCX/PDF/XLS/XLSX/OCR are excluded from **this Slice 2 acceptance**, not removed or declared passed under the still-unaccepted **overall Phase 18** feature set. Reintroduction of excluded formats or overall retargeting requires a different explicit Owner decision.

ADR-0021/22/23 original broad contract clauses and security/source/store/lifecycle rules remain retained historical/future obligations. FR18-3, Java parser/chunker/compact-metadata verification and trusted PostgreSQL publication, full GPU/resource/quality/recovery, and P18-FINAL including the previously failed Java clean gate are **not closed or waived**. No missing Java capability, schema/API change or new slice is made permissible by D22.

## 4. Formal follow-up and delivery containment

The preceding independent review remains `CHANGES_REQUESTED` on its dated record. D22 supplies the Owner dispositions it requested; it does **not** rewrite that review or declare final closeout. Next: a **separate independent documentation closeout re-review** verifying (a) the bounded evidence exception, (b) adopted ADR-0024 and additive canonical notices, (c) unchanged approved D17 code/evidence scope, and (d) a precise mixed-tree path/hunk-level owner delivery boundary. 

If these pass, return `READY_FOR_OWNER_CLOSEOUT_DECISION` as a recommendation; any contradiction or independently unacceptable evidence gap must be reported `CHANGES_REQUESTED`. No automatic `SLICE_2_CLOSED` / commit readiness. Preserve original 329-dirty-path inventory and classify 22 Markdown, 12 mixed, 35 excluded, 258 governance/history and 2 policy paths independently before proposing any staging. No wholesale `git add .`.

**Frozen boundaries:** Phase 17 FROZEN; Slice 1/D14 CLOSED; D17/D20 accepted; D21 `.md` only; FR18-3 OPEN; external indexing INACTIVE; live originals DISABLED/DEFERRED; Slices 3/4 unauthorized; historical Java full clean gate FAILED; Phase 18/P18-FINAL not accepted. No code, tests, dependencies, runtime behavior, evidence capture, commit or push changed by this document.
