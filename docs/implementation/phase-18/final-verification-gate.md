# P18-FINAL — One executable final integration gate (future Phase 18 implementation)

**Status:** Named **future acceptance gate**, not executed/accepted by Phase 18 preparation. One gate **after** focused internal slices converge; no separate full suite per parser slice. See [AC-01–AC-08](acceptance-and-evidence.md), [held-out protocol](held-out-quality-and-locators.md) and [D18-A](../../adr/0021-phase-18-bounded-multi-file-verified-indexing.md).

## Pinned Windows runner and exact commands

From **repository root** in PowerShell, with the existing pinned `rag-engine/.venv/Scripts/python.exe` (CPython 3.14.4, locked environment) and Maven/Java/PG environment needed for real Testcontainers integration. Use actual native `mvn.cmd` as on the reviewed Windows host. The runner is `scripts/run-bounded-command.py`. `logs/phase18/` contains bounded JSON command receipts (exit/result/duration/command digest, not raw args). Errors must halt and remain visible, not be treated as PASS.

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

Normal commands are capped at **1,200 seconds each**. Genuine full Maven `clean verify` may use `full-suite` **2,100 seconds**, only if host execution permits. A genuinely complete `pytest` suite may use the extended profile **only if necessary**, using the runner's `is_full_suite` check; the default above is normal. **These are agent command limits, not user-indexing lease/resource limits.** An absent pinned interpreter/Maven/PG, timeout, skip of a required test, nonzero exit or missing receipt is a gate failure, not an invented PASS.

## Mandatory linked focused receipts (produce earlier; do not repeat just to aggregate)

| Evidence | Exact minimum |
| --- | --- |
| `P18-QUALITY` | Frozen sanitized dev/held-out **source-grouped** manifest/sha, zero cross-split leakage, human text/table/locator labels, each introduced format/vi-en-zh-Hans-zh-Hant denominator, errors/failures/skips/pending, material-failure threshold decision, original-file unchanged. See [oracle](held-out-quality-and-locators.md). |
| `P18-R7-WINDOWS` | Native NTFS handle/identity/root/reparse/hard-link/pre-existing writer-mapping proof across actual Java physical COMMIT; unsupported path denied; rollback and unknown C; disabled-path smoke **not accepted**. |
| `P18-PUBLISH-PG` | Real PostgreSQL changed-boundary tests: multi-root union reservation, independent A/B vs child dependency, 8-success/2-fail and all-fail/cancel, one P commit/rollback, lost finish ACK, attempt-writer seal both orders, released entire set exactly on terminal C, restricted `rag_worker` negative grants, new content/vector purge. |
| `P18-OFFLINE-MODEL` | Actual pinned CPython 3.14 Windows + selected OCR/model artifacts/license hashes/ABI and native CPU/GPU proof; no required runtime egress/download; vector finite/dimension/dedup/model identity, one same-model CPU fallback and bounded failure. |
| `P18-RESOURCES` | Native Windows 11 i5-9600K **16 GB system RAM** peak worker + owned subprocess RAM working set/host free RAM vs RTX 2070 **8 GB VRAM** measured separately; CPU time, temp disk, bounded per-file/job size/pages/rows/chunks/time, cancellation/unknown process; finite empirically chosen limits and outcomes. |
| `P18-REPORT` | Job state is existing `COMPLETED` (>=1 published), `ABORTED` (0 published), `CANCELLED` or nonterminal `RECOVERY_REQUIRED` (unknown); report-only `PARTIAL_SUCCESS`; sanitized file results and **independently recomputed CURRENT/OUTDATED**. |

## Aggregation policy

- Require all **five listed commands** to return real exit code `0`, truthful counts/skips and bounded JSON receipts, plus each linked focused receipt accepted by independent Codex. No live Phase 18 behavior may pass on docs alone. No unconditional rerun of 420 unchanged Phase 17 benchmark jobs.
- Preserve existing architecture/Modulith, effective Flyway roles and source/version restrictions as part of full integration; separately link focused native R7 and PG tests even if those cannot run within Maven on every environment. Run tests for changed native/SQL boundaries on the owner Windows host before acceptance.
- `AUTH-CLOCK` remains OPEN in unrelated Authentication; **there is no automatic Phase 18 exception**. If `clean verify` exits nonzero for any reason, mark the Java component **FAIL**, record the cause and require an explicit new owner gate decision rather than calling the suite PASS.
- Emit one `P18-FINAL` aggregation note with each command's exact exit/duration, Python/Java test pass/fail/error/skip counts, fixture dataset/config/engine hashes, focused evidence links, resource measured max, unresolved findings, and overall `PASS` **only when all conditions pass**. Preparation has **no** runtime pass assertion.
