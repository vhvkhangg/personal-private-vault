# Phase 18 — Simplified implementation handoff (planning only)

**2026-10-20 scope notice:** Slice 1 A+B+C was separately authorized, reviewed and [closed](slice-1-closeout.md) under D14 at owner commit `7e1a4cd`. This four-slice table remains **planning only** for Slices 2–4, which are **NOT AUTHORIZED**. The original 2026-10-19 design-only note below is historical for the then-unimplemented Slice 1.

**Status:** `SLICE_1_SEPARATELY_OWNER_AUTHORIZED / SLICES_2_TO_4_NOT_AUTHORIZED`, 2026-10-19. The owner approved [D13](owner-decisions.md) to begin **only** [P18-S1 Import & Snapshot](slice-1-import-snapshot-handoff.md) under [ADR-0023](../../adr/0023-phase-18-application-level-snapshot-worker-trust.md). This document remains **planning, not the executable active handoff**; use [ACTIVE](../handoffs/ACTIVE.md). Earlier ADR-0022 native/Restricted Token proof handoffs and independent reviews are historical; do not backdate PASS. Snapshot indexing remains inactive and P18-FINAL not accepted.

## Working rules

- Treat `backend/`, `rag-engine/`, Flyway, tests and pinned environment as **uninspected in this docs-only review**. Inventory *actual repo HEAD and working tree* before implementation; do not overwrite local dirty changes, open findings or valid previously implemented code. Existing native safeguards need not be deleted merely because no longer mandatory.
- Single owner account, Java orchestrator and local Python same Windows user. **Trust assumption:** no malicious same-user process is within the guaranteed threat model. Python receives only selected snapshots by application convention/contract, but OS does not confine its filesystem reads. This risk must be visible in security docs and cannot be labeled 'sandboxed'.
- Reuse Phase 17 one-job/process/job attempt/reservation, source-first transaction order and PostgreSQL least-privilege staging. Keep v2 compatibility, typed v3 semantics and actual migration tip, no generated new status/enums unless strictly required. Do not recreate an authentication/consent service or second incident subsystem.

## Deliverable slices and concrete exit conditions

| Slice | Implementation target | Must demonstrate |
| --- | --- | --- |
| 1 — Import & Snapshot (A+B+C) | Java explicit whole-file selection+consent, regular-file/path/reparse/size validation, temp streaming copy/hash/length and basic before/after consistency; no-overwrite R, S/R manifest and DB selection/approval. | Different bytes → new R; identical valid copy → NO_CHANGE candidate; changed-in-copy, missing/invalid file, symlink/reparse, stale/revoked intent, hash mismatch, existing target and disk-full/cancel fail safely; selected file not modified. |
| 2 — Parser, OCR & Embedding (G+H) | Text/MD/native PDF → DOCX/XLSX/XLS → scan/mixed PDF; approved Obsidian dependencies, one structure-aware chunker, exact typed locators, one multilingual model, valid pgvector; offline GPU and same-model CPU fallback. | Per-format vi/en/zh-Hans/zh-Hant fixtures, nested Obsidian grants/cycles, merged/hidden tables, realistic OCR errors, real per-format coordinates, no external fetch/code execution, no silent material loss; memory/resource measurements. |
| 3 — Verified Indexing (D+F) | Java validates actual result text/chunks/locators/closure/model/vector, checked staging, source-first guard/attempt fence, trusted PostgreSQL C_P, per-file outcomes; reuse Phase 17 operation/recovery. | Real Python-produced output, non-finite/dimension/forged locator/input mismatch rejection, restricted SQL negative cases, 8-good/2-failed/all-fail, rollback, same-file mutation/revoke conflict, unknown commit reconciliation, only one publish transaction. |
| 4 — Lifecycle & Final (E+H) | Accurate guarded NO_CHANGE, same-R reindex, snapshot qualification, revoke/purge/restore and cleanup, error/retry policy, real reports, bounded CPU/RAM/VRAM/disk/time/cancellation. | No worker on valid NO_CHANGE; child/vector/pipeline changes defeat false NO_CHANGE; no generation on failed file; previously current R/G only usable while verified; orphan not adopted; P18-FINAL 5 commands plus concise required receipts. |

## Essential state/transaction rules

- `O` is the user original (untrusted after capture); `S` logical approved source; `R` immutable app-managed **ORIGINAL_BYTES**, verified length/SHA-256; `G` publication from an actual chosen R and separately approved dependencies. Repeated same-bytes imports of two distinct sources never merge grants/storage. Never parse from arbitrary live original after seal.
- Valid Update NO_CHANGE needs selected R integrity, same fresh exact captured bytes, unrevoked source and real child-vector/edge/pipeline predicates; no new R/source version/G or needless parsing. Reindex reads verified R and creates a new G only if trusted final publication succeeds.
- SQL remains the source of truth for **selected/usable G**, with real CAS/version guards and one atomically committed Java finalization of valid new generations + all outcomes + source release. Cannot atomically commit filesystem and PostgreSQL; incomplete artifacts stay hidden and are reconciled. When commit outcome is uncertain, query DB/recover before retry, never assume ABORTED/COMPLETED.
- Error/incident handling: avoid a separate giant C_D/C_N/C_R engine. Reuse existing transaction and authority state where logically sufficient, but a real revoke or integrity failure must deny affected consumers and maintain durable anti-resurrection state. Distinguish one file's parse failure (other files may publish) from system trust/fencing/finalization failure (job cannot publish).
- Do not claim `SNAPSHOT_CURRENT` because a job once COMPLETED. Check selected R integrity, exact authorized child closure, consent/reset/retention, current published G. Original freshness is UNVERIFIED.

## Tests and evidence budget

Run parser/quality oracle, focused Java/Python PG transactions + cancellation/recovery, offline GPU/CPU with separately observed RAM/VRAM, controlled cleanup and one P18-FINAL: ruff check, ruff format --check, mypy, pytest, Maven clean verify. Use exact pinned runner and environment from [final gate](final-verification-gate.md), but **do not** require Restricted Token startup probes, per-file DACL/handle/mapping/writer-ABA attacks, attacker-process arbitrary-file isolation, native forensic crash certification or extra full-suite rounds. Real SQL role denial and real worker-output validation **remain required**.

## Mapping inherited historical findings (not auto-closure)

- `CP18-AB-2` and any requirement *solely* for Windows worker confinement/OS ACL native isolation: **superseded as acceptance criteria** by D12; historical independent `OPEN/FAIL` observations stand as evidence of *old* goal, not newly obtained PASS.
- Other `CP18-AB-*`, `FR18-1–11` and G1–G4: triage individually against current failure mode; practical no-overwrite, consistent bytes, temp cleanup, cancellation, source grants and post-use integrity may remain defects. No finding closure without fresh code/test review under the amended contract. Especially Critical FR18-3 remains substantive.
- If source code needs a change beyond this contract, stop and request a new owner decision. No automatic scope increase, worker privilege research, migration, commit/push or Phase 19 handoff.
