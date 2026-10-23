# D25 Slice 3 closeout / Slice 4 preparation — documentation patch

**Repository:** `vhvkhangg/personal-private-vault`  
**Verified published `main`:** `e2e00534fb35534a298e8fb15d174f18cb63b5a0` (90 changed paths).  
**Scope:** 3 new canonical docs, exact archived old ACTIVE, and 8 current-status docs updated with prospective notices. No runtime change, migration, dependency, implementation test, commit or push.

## Apply (Windows PowerShell)

1. Back up any uncommitted changes. Extract this ZIP into a **temporary directory outside the repository** (do **not** blindly overwrite repository files).
2. Open PowerShell at the **repository root**. Confirm `git rev-parse HEAD` is the verified SHA and branch is `main`.
3. Run `python "C:\\path\\to\\extracted\\PATCH_TOOLS\\apply_d25_closeout.py"` (replace this with the actual extracted path). The script loads the adjacent `docs/` files, validates exact target Git blob hashes and current status, and aborts if the baseline has drifted or any target has local modifications.
4. Inspect `git diff --stat`, `git diff -- docs/implementation/handoffs/ACTIVE.md`, new docs, and `git -c core.whitespace=cr-at-eol diff --check`.
5. **Owner only** commits/pushes this docs-only closeout/preparation after review. Suggested message: `docs(rag): close Slice 3 and prepare Slice 4 lifecycle handoff`.
6. Open a new Codex CLI session and run `$codex-create-handoff` against `P18-S4-LIFECYCLE-FINAL` and the new D25 handoff preparation. It must check actual published baseline and refine the executable work/test plan before implementation; do not treat `PENDING_CODEX_HANDOFF` as an implementation PASS.

## Preservation and state

Earlier review/failed receipts, D22 unrecovered originals, applied Flyway migration history and historical ACTIVE are retained verbatim. `D25` only updates prospective statuses; older dated lines marked CURRENT remain historical at their date. Phase 18/P18-FINAL remain NOT ACCEPTED, external indexing INACTIVE, historic Java aggregate FAILED. One late Slice 4 cleanup removes *unwanted unaccepted feature implementation* for TXT/PDF/Office/OCR before final gates; do not wipe history merely to save space.
