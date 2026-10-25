## D26 prospective CPU-only applicability - 2026-10-25

**OWNER_APPROVED scope amendment; final acceptance NOT GRANTED.** [D26](../implementation/phase-18/owner-decisions.md#d26-cpu-only)
selects pinned offline CPU inference ONLY. Actual CUDA inference, GPU OOM-to-CPU fallback
and process-attributed GPU VRAM remain **NOT VERIFIED** and are
**NOT_APPLICABLE_UNDER_OWNER_APPROVED_CPU_ONLY_SCOPE**, never PASS. See the
[exact execution profile and reconciliation](../implementation/phase-18/cpu-only-d26-applicability-2026-10-25.md).

**Affected clauses: Mandatory user-visible feature set GPU/fallback/VRAM wording and retained N10 matrix row.** The effective embedding requirement is one pinned compatible multilingual offline CPU space with finite valid vectors and unchanged trusted verification/publication. Preserve every CPU/RAM/disk/time/process-stop/reporting requirement. GPU-specific inference/fallback/VRAM subcases alone are excluded by D26; application-level same-user risk, SQL least privilege, whole-file snapshots and exact children remain unchanged.

This is prospective acceptance applicability only. Unaffected Markdown/authority/publication,
CPU/RAM/disk/deadline/cancellation/security and five-command requirements remain mandatory.
Future GPU enablement requires separate Owner authorization and independent verification.
No source/runtime/test/model/lock/runner change, activation or closeout follows. Original
GPU_EVIDENCE_BLOCKED and failed/timeout evidence remains unchanged historical truth.
Slice 4/P18-FINAL/Phase 18 remain NOT ACCEPTED / NOT CLOSED; indexing INACTIVE.

## Preserved pre-D26 text - unaffected rules remain effective

> **Accepted prospective D24 amendment - 2026-10-21:** The Owner now chooses the **overall Phase 18 `.md`-only target**, supplying the overall-format decision left open by D21/D22/ADR-0024. The exact [applicability table](../implementation/phase-18/slice-3-markdown-scope-and-baseline-2026-10-21.md#prospective-applicability-reconciliation) and [D24](../implementation/phase-18/owner-decisions.md#2026-10-21--d24-overall-markdown-only-phase-18-and-slice-3-verified-indexing) prospectively supersede conflicting format/OCR and future-preservation clauses below. D17 Markdown fidelity/security/child grants, actual producer validation, Java/PG atomic publication, fencing/recovery, offline/model/resources and lifecycle/final verification remain. New initial Slice 3 D+F is authorized only through ACTIVE; Slice 4 implementation is deferred, with one required excluded-format cleanup at its end before P18-FINAL. No activation or acceptance follows. Historical clauses/decisions remain unchanged below; ADR-0020/Phase 17 remain frozen.

> **D21 Slice 2 applicability, 2026-10-20:** The Owner-approved current Slice 2 acceptance scope is Obsidian Markdown `.md` only; TXT/DOCX/PDF/XLS/XLSX/OCR are excluded from that slice and require a future explicit Owner Decision for reintroduction. See the [scope/matrix](../implementation/phase-18/slice-2-markdown-only-d21-scope-and-acceptance.md). The original broader Phase 18 clauses below remain unaccepted reference obligations; this notice does not silently delete or pass them. Precise conflicts and higher-level qualification are [ACCEPTED in ADR-0024 under Owner D22](0024-phase-18-slice-profile-acceptance-boundaries.md), strictly for D21 Slice 2 applicability, not as an overall Phase 18 rebase. Phase 17/Slice 1 and Java/FR18-3/P18-FINAL guarantees are unchanged.

# ADR-0023 — Phase 18 proportionate application-level snapshot and worker trust

- **Status:** ACCEPTED by owner, 2026-10-19 — **contract change only**; implementation not accepted.
- **Decision:** Phase 18 G1 acquisition and G3 immutable snapshot are protected by application ownership, checksum and recovery, **not** by OS-grade confinement against malicious same-Windows-user processes.
- **Scope:** Phase 18 RAG-owned *external immutable-snapshot* sources and the associated Python worker on a personal, single-user Windows machine. This ADR **amends** [ADR-0022](0022-phase-18-immutable-snapshot-whole-file-consent-no-change.md) and the external-file-specific portion of [ADR-0021](0021-phase-18-bounded-multi-file-verified-indexing.md). Phase 17 frozen capabilities and Vault policies are unchanged.
- **Priority:** In any conflict affecting the scope above, this ADR governs. Retain ADR-0022 for its still-effective D1–D11, N/WF rules and rationale **as qualified below**; its historical implementation checkpoints are not retroactive evidence of this amended contract.

## Decision and explicit accepted risk

The owner explicitly accepts running Java and the Python RAG worker under the **same normal Windows user account**. The application will only instruct the worker to process explicitly selected and approved document snapshots. However, a Python process with ordinary user privileges may technically access other files accessible to that Windows user. Java's file capture and completed snapshots can likewise be tampered with or raced by an **actively malicious same-user process**. These are accepted residual risks, **not guarantees of isolation, anti-tamper or complete filesystem security**.

**Out of scope as mandatory acceptance:** Restricted Token, LPAC/AppContainer, private DACL per capture, dedicated service/user, Docker/WSL sandbox, chain-of-native-NTFS-handle/ABA root-to-stream adversarial proof, malicious hard-link/mapping/writer races, attempt-specific OS object security proofs, Windows forensic durability across all namespace operations, malicious worker/parent-token/handle escape tests. Existing functioning protective code may remain if it does not harm correctness or maintainability; **do not delete or rewrite code blindly**. Reopening these protections needs a new owner decision, not automatic escalation from CP18-AB-2.

**Still in scope:** explicit source authorization, safe path/file-type validation, size/complexity limits, symlink/reparse checks where practicable, real bytes copied, checksums, revision immutability *by application API*, no overwrites, trusted Java publication, PostgreSQL role restrictions, finite subprocess lifetime, recovery, deny on observable mismatch, and no remote fetching/script execution. An accidental modification, stale request, OS/process crash, malformed document, SQL failure or compromised *result content* must not become silent valid index data.

## G1 — Ordinary acquisition and exact bytes

1. One authenticated, explicit, finite owner action chooses a complete external regular file; record logical S, intended original locator, selection/authorization/reset/version context and input options. No auto-watcher/import; independent transclusion files require separate approval. Reject unsupported/non-file path, directory, absent/unreadable target, unsafe symlink/reparse traversal when detectable, oversize file and stale/withdrawn authorization. Do not recursively crawl or open unselected child paths.
2. Java copies original bytes into **an application-managed unique temporary file** with streaming SHA-256/length and finite file/time/disk limits. Check the original's usable identity/basic metadata *before and after* copying when supported and compare observed size/last-modified/file key as practical. If read fails, size changes, captured data does not match the completed temp or a detectable race occurs, abort that file and clean temp. Neither path/metadata nor SHA-256 is an adversarial NTFS object-identity proof. A concurrent malicious writer may escape these checks; record this as the accepted limitation.
3. Verify the **captured temp** bytes against the recorded digest/length before final sealing. Never transform the retained ORIGINAL_BYTES. Parse only from approved sealed application copies; Java-owned transfer should not grant the worker new permission to crawl external originals. Private/NSFW/hidden material *inside* an approved file remains within whole-file consent; no macros, formula evaluation, executable embeds, Dataview, document instructions or URL fetching.

## G3 — Application-managed immutable revisions and cleanup

1. Preserve distinct **S** (logical source), **R** (exact ORIGINAL_BYTES revision), and **G** (verified index generation). Store R under an app-managed private root with a random immutable ID; keep manifest of SHA-256, byte length, source/revision/approval association and minimal integrity metadata. No content-based deduplication across sources. A changed approved update creates a new R; qualified NO_CHANGE creates none. Reindex may reuse R.
2. Write to a unique temp; flush/close and verify; **seal using a no-overwrite operation** (`CREATE_NEW`/no-clobber rename or equivalent feasible on target filesystem) and never modify a sealed R through the application. Do not claim that Windows `fsync`/atomic rename gives universal power-loss durability or that same-user external processes cannot edit files. PostgreSQL and filesystem are **not one atomic transaction**.
3. Mark a revision *usable* only when file verification and required metadata/DB state agree. On each admission/use/reindex of R, revalidate existence, expected length/hash and current source/revision/consent/selection/incident conditions; if mismatched, fail closed, invalidate affected index/read eligibility and require explicit owner recovery. Hash consistency detects modifications **at verification time** but is not continuous anti-tamper protection. Avoid reopening original O during reindex or missing-snapshot repair.
4. On cancellation, crash, disk-full, revoke, failed update or interrupted cleanup, treat pending temp/orphan/incomplete R as **not usable**. On controlled restart, reconcile file/DB inventory, incomplete publications and cleanup with idempotent bounded operations; do not automatically adopt or reconstruct an orphan and do not silently resurrect revoked R. Verify controlled active-storage deletion; separately disclose that DB backups/WAL, Windows caches, owner copies and other external copies are outside complete purge guarantees.

## Orchestration, NO_CHANGE and trusted publication — correctness retained

- **Reuse Phase 17** one-operation/worker, source-first reservation/guard order, admission and cancellation, positive worker/process-tree termination when necessary, staging writer fencing and no-active-writer finalization, durable unknown-outcome reconciliation. Avoid introducing new job engine, queue, general incident transaction framework or unnecessary epochs/receipts. Genuine source/authorization/integrity denial must persist sufficiently to prevent reads/processing until explicitly reconciled.
- **NO_CHANGE:** manual Update compares actual freshly captured hash + length against **verified** selected R, *plus* fresh source authorization/selection/reset, actual approved dependency vector, policy and pipeline/reindex applicability. With everything unchanged: finish without new R, source version, generation or embedding/OCR/parser invocation. Changes to parser/model/chunker on the same R use explicit Reindex; changed child/dependency must not be masked by parent hash equality. No automatic authority restoration via checksum.
- **Publication:** Python is trusted as same-user application code, **not trusted as an infallible result**. Keep restricted PostgreSQL `rag_worker` role/grant (logical SQL least privilege, not Windows filesystem sandbox), task/attempt identity and one-use grant, validated worker schema/results, actual text/excerpt/locator/model/pipeline/vector dimension/finite elements/closure and input/revision binding. Reject incorrect output rather than fake provenance; **FR18-3 remains OPEN** until tested with *real produced/staged results*. Individual file failures do not publish that file's new G. All publishable new generations, outcomes and source-union release occur in **one trusted Java physical PostgreSQL transaction**. No success on an unknown commit; reconcile database first. Revocation/integrity uncertainty blocks affected indices. Preserve real lock ordering/CAS, not a second generic C_D/C_N engine; use existing Phase 17 guards and minimal additional state proven necessary.
- **Freshness:** `SNAPSHOT_CURRENT` qualifies only G bound to selected R, verified store bytes, exact approved children and current authority. `originalFreshness=UNVERIFIED`: later changes/deletion of original do not automatically invalidate an already sealed snapshot. Manual update/reindex only.

## Mandatory user-visible feature set (not relaxed)

**D17 delivery-scope amendment, 2026-10-20:** the active bounded Slice 2 implementation/acceptance
checkpoint prioritizes Obsidian Markdown under its
[frozen syntax profile](../implementation/phase-18/obsidian-markdown-d17-contract.md), with whole-document
all-or-nothing extraction. OCR/PDF/DOCX/XLS/XLSX acceptance is deferred, with existing code/tests/failures
preserved. This changes the current delivery/acceptance scope, not the frozen Phase 17 contract or
an assertion that the original broader Slice 2/Phase 18 feature set below is accepted or complete.

**Prospective D16 amendment, 2026-10-20:** mandatory **Slice 2 OCR** languages are **Vietnamese and
English**; Chinese OCR is no longer required. Native Chinese parsing/embedding support and every
other fidelity, locator, human-adjudication, offline/resource and publication gate remain unchanged.
The four-language statement below records the original decision; historical failures are preserved,
not relabeled PASS. See [D16](../implementation/phase-18/owner-decisions.md#2026-10-20--d16-slice-2-vietnameseenglish-ocr-and-official-runtime-feasibility).

TXT, Markdown/Obsidian (frontmatter/headings/tags/wikilinks/aliases/block links/callouts/approved transclusions), DOCX, PDF native/scanned/mixed with OCR, XLSX and XLS; Vietnamese, English, Simplified and Traditional Chinese; hidden/approved metadata, text and tabular content; original reading order, merged cells and accurate structural/table/page/sheet/cell/bounding-box locators where applicable. Use one structure-aware chunker, one compatible multilingual embedding space stored in PostgreSQL/pgvector; offline artifacts, GPU and bounded **same-model CPU fallback**; manual batch import/update/reindex, partial per-file outcomes, cancellation, revoke/purge, reports and bounded RAM/VRAM/CPU/disk/time. Unsupported or materially inaccurate extraction/mandatory locator fails that file instead of silently publishing.

## Acceptance and supersession matrix

| Existing rule/gate | Amended decision | Required evidence |
| --- | --- | --- |
| ADR-0022 N2/WF-2/G1 and original-file root/ancestor/stream proof | **Replace OS-native continuous chain** with application-level file selection/validation, observed before/after properties, exact streaming copy/hash and detectable-race fail closed. | Good/bad paths, symlink/reparse/permission/size/mid-copy-change where reproducible; copy/hash/length and whole-file consent. |
| N3/G3/IS OS worker-denying ACL, Restricted Token, protected handle throughout use | **Replace** with application no-overwrite snapshot/manifest/checksum, repeated integrity checks, guarded selection and reconciliation. | Temp/seal/no-clobber/tamper detection; crash/disk-full/orphan, reopen/reindex, purge/restart. No sandbox test. |
| N8/WF publication and FR18-3 | **Retain correctness**; drop OS-level malicious-worker attestation. Trusted Java still verifies actual parsed/chunked/staged data, model/vector/locator/input binding and fences producer writers. | Real Python+Java+PG role tests, output tamper, partial success and rollback/unknown commit. |
| ADR-0022 C_D/C_N/C_R/C_P | **Retain outcomes and guards**, permit minimal consolidated state/transaction path using Phase 17 mechanisms rather than four new transaction infrastructures. | Cancel/revoke/update/NO_CHANGE races, retained/purged selected G, idempotent recovery, final atomic publish. |
| N1/N4–N7/N9/N10 and WF-1/WF-3–8 | **Retain** except the explicitly superseded OS-native isolation/security proof and redundant metadata/evidence. | Feature tests, child authorization/closure, selection, lifecycle, provenance, quality, offline/resource reporting. |
| Historical CP18-AB-1–8 and independent reviews | **Preserve unchanged historical findings/evidence.** CP18-AB-2 sandbox obligation is removed **prospectively**, not retroactively `PASS` or code-fixed. Other defects map to current practical invariants; no automatic finding closure. | New contract-scoped gap inventory against current repository before code changes. |

**Scope and status rule:** No claim that existing implementation already meets the amendment; ZIP contains docs, not complete code, tests or Flyway state. Reconcile actual repository before selecting code changes. Existing `rag` ingestion stays **INACTIVE**, live-original ingestion **DISABLED/DEFERRED**, P18-FINAL **NOT EXECUTED / NOT ACCEPTED**. The owner approved this **documentation-contract change**, not code rollout, production activation, finding closure or commit/push on their behalf. Implementation work remains separately gated. See [Phase 18 streamlined implementation plan](../implementation/phase-18/simplified-implementation-handoff.md).
