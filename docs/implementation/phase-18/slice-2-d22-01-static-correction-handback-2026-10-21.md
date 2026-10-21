# S2-D22-01 static-gate corrective handback — 2026-10-21

**READY_FOR_INDEPENDENT_CLOSEOUT_RE_REVIEW — Codex corrective implementation authorship.**
The owner authorized one finite S2-D22-01 checkpoint against the
[D22 independent closeout re-review](reviews/2026-10-21-slice-2-d22-independent-closeout-re-review.md).
Both actual source and the isolated Markdown-only delivery tree pass the required fresh-cache mypy
gate. This session does not independently close the finding or Slice 2, grant owner-commit readiness,
stage anything or authorize another corrective cycle. Stop for separate independent closeout review.

## Baseline and precise root cause

HEAD remains `c3b46351507803053850e3c2711602ee9c9ec419`, branch `main`, with inherited mixed dirty work.
The [evidence inventory](slice-2-d22-01-static-correction-evidence-2026-10-21.json) records entry/final
hashes, exact commands, receipts, preserved assets and the delivery patch. The selected interpreter
is **Python 3.14.4**, mypy **2.4.0**, with **67 installed locked distributions**. RapidOCR distribution
and import specification are absent. It deliberately belongs to the deferred Python `>=3.12,<3.13`
group and must not be installed in this selected Markdown environment.

Fresh-cache reproduction fails with **import-not-found** on the existing local RapidOCR import:
actual `ocr/engine.py:50`, projected HEAD adapter `ocr/engine.py:43`. The existing
`type: ignore[import-untyped]` handles an installed untyped package; it does not cover an absent
module's different error code. The import graph still includes the module in the 24-file static
gate although Markdown does not initialize OCR. Runtime already catches unavailable backend
initialization and sets `_engine=None`.

This establishes the current failure without declaring older green receipts invalid. Source and
configuration identity are preserved; the historical cache/environment reason for those earlier
results is **not established**. All older passes, original S2-D22-01 failures and new failure receipts
remain unchanged. D22 is not used as a static-gate waiver.

## Minimal implementation

Only one production import statement's annotation/layout changes, in
`rag-engine/src/ppv_rag/ocr/engine.py`:

```python
# Optional backend: absent from the selected Markdown runtime.
from rapidocr_onnxruntime import (  # type: ignore[import-not-found, import-untyped]
    RapidOCR,
)
```

This is explicit handling of exactly two optional-backend import diagnostics at one statement.
There is no global ignore, new `ignore_missing_imports`, package exclusion, dynamic import bypass,
dependency upgrade, new runtime abstraction or new type cast. The existing `_engine: Any` boundary
and constructor/catch behavior remain unchanged. AST comparison excluding source locations is
identical before/after for **both** adapters. No OCR recognition, quality, admission or resource
behavior was implemented or accepted.

The only new test file, `rag-engine/tests/test_optional_ocr_dependency.py`, has four cases: absent
backend, simulated present constructor, and constructor ImportError/RuntimeError fallback. No real
OCR backend is installed or invoked; the present path uses a scoped synthetic module. Tests also
assert the selected source tree when run by this checkpoint harness. Existing parser, chunker,
embedding, worker, provenance, configuration, lock, model/tokenizer and accepted D17 tests are
unchanged. Current status notices change only ACTIVE and the D21 scope document; new handback,
evidence and delivery inventory are separately dated. Exact file/hunk hashes are in the inventory.

## Executed verification

All long checks use `scripts/run-bounded-command.py`, normal profile, **60–240-second** budgets and
new paths under `logs/phase18/s2-d22-01-static-correction-2026-10-21/`. Mypy cache directories are
asserted absent before launch. Pytest caches/temp, Ruff/Python caches and model-read paths are
explicit. Historical scripts with fixed capture outputs were not run.

| Check | Actual current source | Isolated Markdown-only delivery |
| --- | --- | --- |
| Pre-correction fresh-cache mypy | FAILED, original diagnostic reproduced | FAILED, same absent optional import |
| Corrected fresh-cache mypy | **PASS twice**, independent empty caches, 24 source files each | **PASS**, then **PASS** on fresh reconstruction and **PASS** on final reconstructed artifact, each with a new cache, 24 source files |
| Optional backend focused tests | **4 passed**, 0 failures/errors/skips | **4 passed**, 0 failures/errors/skips |
| Relevant complete Markdown/producer/compatibility group, including new tests | **172 passed**, 37.94 s | **172 passed**, 40.78 s |
| Additional legacy selection | **25 passed / 30 deferred-format deselections**, 15.05 s | **7 passed / 5 excluded-format deselections**, 0.98 s; retained HEAD test file |
| Ruff / formatting | PASS; **74 files** formatted | PASS; **63 files** formatted |
| Dependency / offline lock | Python 3.14.4, **67 compatible installed distributions**, inventory matches lock | Same interpreter/package set; **73 resolved lock packages**, offline check PASS; selected pyproject/lock unchanged |
| `git diff --check` / index and preservation | PASS; no staging | Rebuild/apply checks and exact selected-tree hashes PASS; no index changes |

Representative exact static commands (complete absolute argv, cwd and timeout receipts are in the
inventory):

```text
rag-engine/.venv-d17/Scripts/python.exe -m mypy --cache-dir logs/phase18/s2-d22-01-static-correction-2026-10-21/cache-current-fresh-1 --config-file rag-engine/pyproject.toml rag-engine/src
rag-engine/.venv-d17/Scripts/python.exe -m mypy --cache-dir logs/phase18/s2-d22-01-static-correction-2026-10-21/cache-projection-fresh-final --config-file logs/phase18/s2-d22-01-static-correction-2026-10-21/projection-rebuilt-final/rag-engine/pyproject.toml logs/phase18/s2-d22-01-static-correction-2026-10-21/projection-rebuilt-final/rag-engine/src
```

These groups overlap; counts are not disjoint new acceptance evidence. Formatting later adjusted
only layout in the new test's source-tree assertion; its AST is unchanged. Final Ruff/format gates
and the final artifact rebuild cover that layout. No unchanged million-character, OCR, historical
capture writer or Java full gate was rerun.

## Reproducible selective delivery

The [updated delivery scope](slice-2-d22-01-static-delivery-scope-2026-10-21.json) reuses the
reviewer's **336-path/hunk inventory** and exact existing Markdown patch, not wholesale dirty files.
All **32 originally selected files** retain their reviewed projected hashes and shared-hunk choices.
The new patch keeps that original patch as an unchanged byte prefix and adds exactly:

1. A **HEAD-based optional-import annotation hunk** for `ocr/engine.py`; all current unaccepted OCR
   feature hunks remain excluded. Restoring this annotation reproduces the HEAD adapter's AST.
2. The four-case optional dependency test file.

There are **34 selected source/support paths**. Models.py's DOCX validator, archive/PDF/input-byte
helpers, dirty Office/PDF/OCR adapters, mixed-format test files and native-OCR fixtures remain
excluded as before. The prior 35 excluded implementation paths remain preserved; the OCR adapter
receives only the separately authorized static hunk. AGENTS and unrelated configuration deltas are
not selected. The existing one-line Markdown venv ignore hunk and reviewed pyproject/lock hunks are
retained; no configuration was changed by this correction. Governance/history paths retain their
separate inventory and do not certify excluded functionality.

Final artifacts under the new log directory:

- `corrected-markdown-only-final.patch`: original source patch plus the two minimal additions.
- `delivery-projection-final.json`: exact hashes, original hunk inventory, selected/excluded spans
  and the two new zero-context diffs.
- `rebuild_delivery_final.py`: reconstructs from the recorded HEAD into a **new ignored directory**,
  applies the patch without touching the index and verifies every resulting file hash.
- `projection-rebuilt-final/`: independently reconstructed, final fresh-cache mypy PASS.

Rebuild from the repository root, choosing an unused destination name:

```text
python -X utf8 -B logs/phase18/s2-d22-01-static-correction-2026-10-21/rebuild_delivery_final.py projection-owner-replay
```

Seven reviewed Python working files use CRLF while patch transport uses normalized lines. The
manifest/rebuild records their explicit, hash-checked newline restoration to reproduce exact review
bytes. **Markdown fixtures are never newline-normalized.** The inherited fixture's authored trailing
spaces are retained, not “fixed.” Reconstruction is self-contained from HEAD, final patch and
manifest; it does not depend on rerunning the old review script or rewriting historical outputs.
No files are staged, committed or made owner-commit-ready by this proposal.

## Evidence disposition and stop

Original D17/D20 accepted source, gold, model/tokenizer and source-tree semantics remain applicable.
The only checked-source change is runtime-AST-identical optional-import typing. Original D22 review,
failed static receipts, all old handbacks, 193 D19 files and accepted evidence remain preserved.
The three reviewer-overwritten raw captures remain **NOT_RECOVERED / original preservation FAILED**;
D22's bounded substitute disposition remains valid, and is not relabelled restoration or a global
exception. No new human signature is created or required under D20.

All required selected static checks now pass; no remaining S2-D22-01 implementation blocker is
known. Separate independent closeout re-review must decide that finding's closure and the selective
delivery result. **Slice 2 is not self-closed.** D17/D20 ACCEPTED; D21 Markdown-only; D22 valid;
ADR-0024 ACCEPTED for that slice; Phase 17 FROZEN; Slice 1/D14 CLOSED; FR18-3 OPEN; indexing INACTIVE;
live originals DISABLED/DEFERRED; historical Java gate FAILED (1,433 / 1 failure / 2 errors / 0 skips);
P18-FINAL NOT ACCEPTED; Slices 3/4 NOT AUTHORIZED. Historical Antigravity **2/2 exhausted** remains,
with [round 1](reviews/2026-10-18-phase-18-codex-final-re-review-1.md) and
[round 2](reviews/2026-10-18-phase-18-codex-final-re-review-2.md) unchanged. No OCR experiment,
architecture change, contract weakening, publication, stage/commit/push/tag/PR or further cycle.

All unsuccessful setup/check receipts are retained: fresh static reproductions, initial reconstruction
checks that exposed tool-cache entries/CRLF bookkeeping, initial pytest source-path setup failures
and the new test's formatting correction. Those harness failures are distinguished from the single
production static blocker. No failed historical evidence is converted into PASS.
