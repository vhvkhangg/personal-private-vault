param(
    [string]$Version = "0.9.69"
)

$ErrorActionPreference = "Stop"

if (-not (Get-Command uv -ErrorAction SilentlyContinue)) {
    Write-Host "Graphify setup requires uv." -ForegroundColor Yellow
    Write-Host "Install it once with: winget install astral-sh.uv"
    exit 2
}

$package = "graphifyy==$Version"
Write-Host "Installing/updating $package ..."
uv tool install --force $package

if (-not (Get-Command graphify -ErrorAction SilentlyContinue)) {
    Write-Host "graphify is installed but not yet on PATH." -ForegroundColor Yellow
    Write-Host "Run: uv tool update-shell"
    Write-Host "Then reopen the terminal and rerun this script."
    exit 3
}

Write-Host "Building local code-only graph..."
graphify extract . --code-only

Write-Host ""
Write-Host "Graphify ready. Generated graphify-out/ is intentionally gitignored."
