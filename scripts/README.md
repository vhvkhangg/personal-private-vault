# Repository Scripts

## Graphify setup

```powershell
powershell -ExecutionPolicy Bypass -File scripts/setup-graphify.ps1
```

Graphify is installed separately by `uv tool`. Deleting `setup-graphify.ps1` after installation does **not**
uninstall Graphify, but keep the script in Git so setup remains reproducible on another machine or after reinstall.

Refresh after meaningful source changes:

```powershell
powershell -ExecutionPolicy Bypass -File scripts/refresh-graphify.ps1
```

## Antigravity Windows settings

```powershell
powershell -ExecutionPolicy Bypass -File scripts/configure-antigravity.ps1
```

The script backs up and merges safe repository settings instead of replacing unrelated configuration.
