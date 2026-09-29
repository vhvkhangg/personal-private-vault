$ErrorActionPreference = "Stop"
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

$mappings = @(
    @{ Old = "docs\owner-phase-workflow.md"; New = "docs\workflow\owner-phase-workflow.md"; Sha256 = "260E55C09B6B22301AF159F2C52D1A5E93F39DD9A0EB14662DB272A46ABA3431" },
    @{ Old = "docs\agent-development-workflow.md"; New = "docs\workflow\agent-development-workflow.md"; Sha256 = "8FC2D57FC303E26D1E2674814BC5E2DD823F7B25153CCE59DF648B816FEA7185" }
)

foreach ($mapping in $mappings) {
    $oldPath = Join-Path $repoRoot $mapping.Old
    $newPath = Join-Path $repoRoot $mapping.New

    if (-not (Test-Path -LiteralPath $oldPath)) { continue }
    if (-not (Test-Path -LiteralPath $newPath)) {
        throw "Destination missing; refusing to delete $($mapping.Old)"
    }

    $actual = (Get-FileHash -Algorithm SHA256 -LiteralPath $oldPath).Hash
    if ($actual -ne $mapping.Sha256) {
        throw "Legacy workflow file changed unexpectedly; refusing deletion: $($mapping.Old)"
    }

    Remove-Item -LiteralPath $oldPath -Force
    Write-Host "Removed legacy workflow file: $($mapping.Old)"
}

Write-Host "Workflow document migration cleanup complete." -ForegroundColor Green
