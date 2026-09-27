$ErrorActionPreference = "Stop"

if (-not (Get-Command graphify -ErrorAction SilentlyContinue)) {
    Write-Host "Graphify is not installed; skipping refresh." -ForegroundColor Yellow
    exit 0
}

graphify extract . --code-only
