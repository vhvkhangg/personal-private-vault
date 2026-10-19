# P18-FINAL — One integration gate for current Phase 18 contract

**Status:** Future execution **NOT RUN / NOT ACCEPTED**. Contract: [ADR-0023](../../adr/0023-phase-18-application-level-snapshot-worker-trust.md) + [acceptance](acceptance-and-evidence.md), effective 2026-10-19. The old native-ABA/Restricted Token/DACL/worker-denying ACL acceptance has been **removed** for external snapshots; this does **not** close functional, output-verification or lifecycle defects. Snapshot ingestion inactive. Historical strict-gate reviews are preserved unchanged.

## Exact existing Windows runner and five final commands (retained)

Run from repository root using **actual verified installed pinned** `rag-engine/.venv/Scripts/python.exe`, `scripts/run-bounded-command.py`, Maven/Java and appropriate PostgreSQL/Testcontainers. Verify current code/environment before use. Missing interpreter/runner/test, nonzero exit, timeout or skipped required coverage is **FAIL**, not automatic PASS. The CLI watchdog is not a user indexing-time limit.

```powershell
$Root = (Get-Location).Path # must be the repository root
$Python = Join-Path $Root 'rag-engine/.venv/Scripts/python.exe'
$Runner = Join-Path $Root 'scripts/run-bounded-command.py'
$Receipts = Join-Path $Root 'logs/phase18'
New-Item -ItemType Directory -Force -Path $Receipts | Out-Null
if (-not (Test-Path $Python)) { throw 'Pinned worker Python venv missing; verify the locked environment' }
if (-not (Test-Path $Runner)) { throw 'Bounded runner missing' }

# Python: run exactly from rag-engine so ruff/pytest/pyproject settings apply.
Push-Location (Join-Path $Root 'rag-engine')
try {
  & $Python $Runner --receipt (Join-Path $Receipts 'ruff-check.json') -- $Python -m ruff check .
  if ($LASTEXITCODE -ne 0) { throw 'P18-FINAL: ruff check failed' }
  & $Python $Runner --receipt (Join-Path $Receipts 'ruff-format.json') -- $Python -m ruff format --check .
  if ($LASTEXITCODE -ne 0) { throw 'P18-FINAL: ruff format failed' }
  & $Python $Runner --receipt (Join-Path $Receipts 'mypy.json') -- $Python -m mypy src/ppv_rag
  if ($LASTEXITCODE -ne 0) { throw 'P18-FINAL: mypy failed' }
  & $Python $Runner --receipt (Join-Path $Receipts 'pytest.json') -- $Python -m pytest -q
  if ($LASTEXITCODE -ne 0) { throw 'P18-FINAL: pytest failed' }
} finally { Pop-Location }

# Java: full integration gate, extended profile ONLY for actual full clean verify.
& $Python $Runner --profile full-suite --receipt (Join-Path $Receipts 'full-java.json') -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify
if ($LASTEXITCODE -ne 0) { throw 'P18-FINAL: full Java clean verify failed' }
```

The existing normal command cap is 1,200 s; genuine Maven full-suite may use 2,100 s when supported. Extended pytest profile only if permitted by current runner's full-suite checks; do not silently alter this preserved executable list. Commands require a separately authorized implementation/acceptance run.

## Linked **minimal focused evidence** (develop earlier; no duplicated final suites)

| Receipt | Concrete required result |
| --- | --- |
| `P18-QUALITY` | Representative sanitized source-grouped dev/held-out TXT/MD/Obsidian/DOCX/PDF native+scan+mixed/XLSX/XLS; vi/en/zh-Hans/zh-Hant, OCR reading/table/merged/hidden material, exact mandatory locators. Counts for all failures/skips/pending and original unchanged. |
| `P18-SNAPSHOT-APP` | Explicit whole-file grant, practical regular-path/reparse/size validation, exact copy SHA-256+length and before/after detectable-change checks; no-clobber R/manifest, check on reuse, same/different-byte/NO_CHANGE, revoke/tamper/missing/disk-full/cancel/restart/orphan/cleanup. **No Restricted Token, per-attempt DACL, NTFS ABA/hard-link/mapping/writer attack proof or forensic durability certificate.** |
| `P18-PUBLISH-PG` | Real Python-produced/staged text/chunks/locators/model/vector and trusted Java verification of actual result (FR18-3); PostgreSQL `rag_worker` SQL grant negatives, source-first guards, full union and own-G closure, mixed 8-success/2-fail, all-fail/cancel, changed/unchanged, child changes, rollback, genuine unknown-C and exactly one final physical publication transaction or none. |
| `P18-OFFLINE-MODEL` | Installed model/OCR artifacts pinned, no required runtime API/network pull, actual finite pgvector rows, native GPU and **same model** bounded CPU fallback, failed-resource cases. |
| `P18-RESOURCES` | Actual representative 16 GB system RAM usage and 8 GB VRAM **measured separately**, CPU/wall/temp disk, upper limits for file/page/row/chunks/expanded data, cancellation and safe work cleanup. No fabricated limits. |
| `P18-REPORT` | Individual file failures/successes and warnings, selected/pending R, NO_CHANGE/retained vs new G, qualified SNAPSHOT_CURRENT and original UNVERIFIED, measured GPU fallback, controlled purge/unknown backups and truthful recovery status. |

## Aggregation

Require five commands' genuine exit `0`, accurate counts and receipts, and independent review of **the focused cases above**. Record OS/hardware, model/fixture/version hashes, measured RAM/VRAM, unresolved issues and retest disposition. No private source contents/paths/credentials in reports. No extra C_D/C_N forensic-proof matrix; maintain actual race/SQL/unknown-outcome correctness and working Phase 17 fences. Do not claim universal power-loss durability from ordinary filesystem APIs.

A full-suite failure including `AUTH-CLOCK`, missing model/library or unrun focused case is not PASS; record cause for an explicit later owner decision. G1–G4 and FR18-1–11 are not closed here. No automatic code execution, commit/push or capability activation is authorized by editing these docs.
