# Antigravity CLI — Windows Permissions

Persistent host grants stay narrow; the repository PreToolUse hook is defense in depth.

## Baseline

```json
{
  "enableTerminalSandbox": true,
  "toolPermission": "proceed-in-sandbox"
}
```

Do not use `command(*)`, broad shell grants, or `toolPermission: "always-proceed"` just to suppress prompts.

## Maven

Keep Maven sandboxed with `command(mvn)`. Do not grant broad `unsandboxed(mvn)`.
The hook requires owner confirmation for `mvn deploy`.

## Docker

Direct read-only inspection may use `docker info`, `docker version`, and `docker ps` with matching narrow
`unsandboxed(...)` resources.

Never persist prefix-wide `command(docker compose)` or `unsandboxed(docker compose)`: token-prefix matching would
also cover state-changing/destructive/publishing Compose commands.

Only these read-only Compose operations receive host overrides:

```text
command(docker compose ps)
command(docker compose logs)
command(docker compose images)
command(docker compose top)
unsandboxed(docker compose ps)
unsandboxed(docker compose logs)
unsandboxed(docker compose images)
unsandboxed(docker compose top)
```

Read-only `config`/`version` need no host override. `up`, `down`, `rm`, `exec`, `build`, `pull`, `push`, and other
non-read-only Compose operations require explicit owner confirmation.

## PowerShell inspection

Allow read-oriented cmdlets individually (`Get-ChildItem`, `Get-Content`, `Select-Object`, `Select-String`, etc.).
Do not broadly allow `powershell`, `pwsh`, or `cmd`.

## Evidence

The current hook suite and captured result are retained in
[`agent-tooling-governance-test-evidence.md`](agent-tooling-governance-test-evidence.md).

Apply/merge local settings with:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/configure-antigravity.ps1
```

The script removes obsolete broad Compose rules if present. Fully restart Antigravity CLI afterward.
