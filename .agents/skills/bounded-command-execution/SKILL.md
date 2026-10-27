---
name: bounded-command-execution
description: Run potentially long Java/Python build, test, OCR and benchmark commands with an executable watchdog, bounded timeout, owned process-tree cleanup and factual receipts.
---

# Bounded Command Execution — Windows first

**MAINTENANCE B1/B2 INDEPENDENTLY ACCEPTED AND OWNER-PUBLISHED.** Existing runner limits and Windows interception remain bounded by their documented supported-client conditions.
Approved execution policy in [Owner policy](../../../docs/workflow/current-owner-delivery-policy.md):
- Default ordinary command hard cap: **300 seconds (5 minutes)**.
- Pre-approved full-test-suite commands: **at most 2,100 seconds (35 minutes)** using `--profile full-suite`.
- Inactivity diagnosis: after 300s (or configured threshold) **without output, `--progress-file` movement, or attributable process CPU/IO deltas**, a diagnostic episode inspects owned process health. On Windows, native Wait Chain Traversal (`advapi32.dll`) detects wait-chain cycles/deadlocks.
- Objective deadlock proof triggers early owned-tree termination, `HUNG_CONFIRMED`, and exit code 125.
- If evidence is inconclusive or unsupported, the watchdog reports `DIAGNOSTIC_INCONCLUSIVE` and permits quiet healthy execution to continue until exit or the hard deadline.
- Output streaming: stdout and stderr are streamed chunkwise (<=64 KiB chunks) to fresh run-scoped files under ignored `logs/` without whole-stream memory accumulation.
- Non-clobber receipts: pre-existing receipt/log paths are never overwritten or truncated; collision fails before launch.
- Safe metadata: receipts record executable basename, argv SHA-256, UTC timestamps, durations, exit status, and diagnostic summaries without raw commands, environments, or sensitive tokens.

Use `scripts/run-bounded-command.py` for potentially long-running commands. A prose instruction alone does **not** automatically interrupt an external process; launch commands through the watchdog.

- Do not apply the 35-minute profile to installation, download, dependencies, shell scripts, benchmarking, or arbitrary commands; the wrapper verifies full-suite-shaped invocations (`mvn clean verify`, `python -m pytest`).
- Examples from repository root, PowerShell:

```powershell
$py = (Get-Command python -ErrorAction Stop).Source
$runDir = Join-Path 'logs/maintenance-b' ('b1-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $runDir -ErrorAction Stop | Out-Null
& $py scripts/run-bounded-command.py --profile normal --timeout-seconds 300 --receipt (Join-Path $runDir 'focused.json') -- $py -m unittest discover -s scripts -p test_run_bounded_command.py -v
& $py scripts/run-bounded-command.py --profile full-suite --receipt (Join-Path $runDir 'full-java.json') -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify
```

Use `--timeout-seconds N` for a *shorter* budget and `--profile normal` for non-full-suite commands. Optional `--progress-file PATH` tracks file metadata advancement.

On timeout or confirmed hang, the wrapper targets **only its own launched process tree** (Windows `taskkill /T /F` of the tracked child PID, POSIX process group), exits nonzero, and records diagnostic observations; it never kills arbitrary `java.exe`, Docker containers or unrelated processes. The caller's host/tool timeout must also permit the execution budget. A tool that bypasses this wrapper is **not automatically controlled** by it.
