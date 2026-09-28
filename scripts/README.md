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


## Phase-review migration cleanup

`apply-phase-review-migration.ps1` is a one-time compatibility helper for repositories where an older ZIP was
extracted over an existing tree and legacy `docs/reviews/` copies remained.

```powershell
powershell -ExecutionPolicy Bypass -File scripts/apply-phase-review-migration.ps1
```

The helper is fail-closed: mapped review files are deleted only when their phase-local destination exists and the
SHA-256 hashes are identical. Unexpected files are preserved. Once every working copy has the Git-represented
phase-local move, this helper may be removed in a later housekeeping slice.
