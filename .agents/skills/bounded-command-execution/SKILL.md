---
name: bounded-command-execution
description: Run potentially long Java/Python build, test, OCR and benchmark commands with an executable watchdog, bounded timeout, owned process-tree cleanup and factual receipts.
---

# Bounded Command Execution — Windows first

**MAINTENANCE B NOT YET IMPLEMENTED.** Approved forward execution policy in [Owner policy](../../../docs/workflow/current-owner-delivery-policy.md): default 300s wall-time hard cap; pre-approved complicated/large-test commands up to 2,100s; after 300s **without output/progress**, diagnose logs, child CPU/I/O/thread and test-report movement before deciding the owned process tree is hung. Do not kill merely because a valid long test is quiet. **Current script still waits with fixed 1,200/2,100s caps, no liveness monitor.** Until a separate Antigravity implementation and independent Codex review replace it, call only the existing working runner and report its real limits; no phase historical receipt is rewritten. Console short, full raw output/receipts preserved in ignored `logs/` (Owner decided keep every existing log).


For Phases 18–22, prefer `scripts/run-bounded-command.py` for potentially long-running commands. A prose instruction alone does **not** automatically interrupt an external process; actually launch the command through the watchdog.

- Ordinary commands: **at most 1,200 seconds (20 minutes)**. For explicitly required **full test suites only**, the owner authorizes up to **2,100 seconds (35 minutes)** using `--profile full-suite`.
- Frozen Phase 17's previous per-command 1,200-second gate is historical and is **not** retroactively changed by this policy.
- Do not apply the 35-minute profile to installation, download, Gradle/Maven dependencies, shell scripts, benchmarking, open-ended investigation or an arbitrary command; the wrapper verifies full-suite-shaped invocations.
- Example from repository root, PowerShell:

```powershell
py -3 scripts/run-bounded-command.py --profile full-suite --receipt logs/phase18/full-java.json -- mvn.cmd -ntp -f backend/pom.xml '-DargLine=--enable-native-access=ALL-UNNAMED' clean verify
py -3 scripts/run-bounded-command.py --profile normal --receipt logs/phase18/python-unit.json -- python -m pytest rag-engine/tests -q
```

Use `--timeout-seconds N` for a *shorter* budget and `--profile normal` for all non-full-suite commands. In Python/PowerShell, do not nest opaque shell strings when argv form suffices; preserve stderr/stdout in the actual tool console/log. The wrapper writes an atomic minimal JSON receipt containing the executable name and SHA-256 of its argv, UTC timestamps, exit, duration and timeout without raw arguments/environment variables/secrets; choose paths under gitignored `logs/`. Do not pass passwords or tokens as CLI arguments.

On a timeout, the wrapper targets **only its own launched process tree** (POSIX process group, Windows `taskkill /T /F` of the tracked child PID), records a timeout, exits nonzero and instructs diagnosis; it never kills arbitrary `java.exe`, Docker containers or other unrelated processes. Windows tree termination is best-effort if the OS denies termination; check for owned survivors rather than claiming universal cleanup. The caller's tool/host timeout must also permit 2,100 seconds for the full-suite profile. A plugin/agent that bypasses this wrapper is **not automatically controlled** by it.

Do not retry the same hung command unchanged to get a favorable result; inspect logs and fix or raise a bounded blocker. No timeout is evidence that application tests passed.
