$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path

# One-time compatibility cleanup for a repository where an older ZIP was
# extracted in place and left legacy docs\reviews files behind.
#
# Fail-closed rules:
# - a mapped source is deleted only if its phase-local destination exists;
# - source and destination SHA-256 hashes must be identical;
# - any mismatch stops the script before that deletion;
# - unexpected files are never deleted;
# - rerunning the script is safe.

$mappings = @(
    @{ Old = "docs\reviews\2026-09-27-backend-phase-0-codex-review.md"; New = "docs\implementation\phase-0\reviews\2026-09-27-backend-phase-0-codex-review.md" }
    @{ Old = "docs\reviews\2026-09-27-backend-phase-0-final-governance-codex-review.md"; New = "docs\implementation\phase-0\reviews\2026-09-27-backend-phase-0-final-governance-codex-review.md" }
    @{ Old = "docs\reviews\2026-09-27-backend-phase-0-governance-fix-codex-review.md"; New = "docs\implementation\phase-0\reviews\2026-09-27-backend-phase-0-governance-fix-codex-review.md" }
    @{ Old = "docs\reviews\2026-09-27-backend-phase-0-governance-parser-codex-review.md"; New = "docs\implementation\phase-0\reviews\2026-09-27-backend-phase-0-governance-parser-codex-review.md" }
    @{ Old = "docs\reviews\2026-09-27-backend-phase-1-final-codex-review.md"; New = "docs\implementation\phase-1\reviews\2026-09-27-backend-phase-1-final-codex-review.md" }
    @{ Old = "docs\reviews\2026-09-27-backend-phase-1-reference-vault-codex-review.md"; New = "docs\implementation\phase-1\reviews\2026-09-27-backend-phase-1-reference-vault-codex-review.md" }
    @{ Old = "docs\reviews\2026-09-27-backend-phase-1-remediation-codex-review.md"; New = "docs\implementation\phase-1\reviews\2026-09-27-backend-phase-1-remediation-codex-review.md" }
    @{ Old = "docs\reviews\2026-09-28-phase-1-package-layout-refactor-codex-review.md"; New = "docs\implementation\phase-1\reviews\2026-09-28-phase-1-package-layout-refactor-codex-review.md" }
    @{ Old = "docs\reviews\2026-09-27-agent-tooling-governance-codex-review.md"; New = "docs\implementation\phase-1\reviews\2026-09-27-agent-tooling-governance-codex-review.md" }
    @{ Old = "docs\reviews\2026-09-28-backend-phase-2-authentication-settings-codex-review.md"; New = "docs\implementation\phase-2\reviews\2026-09-28-backend-phase-2-authentication-settings-codex-review.md" }
)

function Get-Sha256([string]$Path) {
    (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash
}

foreach ($mapping in $mappings) {
    $oldPath = Join-Path $repoRoot $mapping.Old
    $newPath = Join-Path $repoRoot $mapping.New

    if (-not (Test-Path -LiteralPath $oldPath)) {
        continue
    }

    if (-not (Test-Path -LiteralPath $newPath)) {
        throw "Destination missing; refusing deletion: $($mapping.Old)"
    }

    $oldHash = Get-Sha256 $oldPath
    $newHash = Get-Sha256 $newPath
    if ($oldHash -ne $newHash) {
        throw "Content mismatch; refusing deletion: $($mapping.Old) -> $($mapping.New)"
    }

    Remove-Item -LiteralPath $oldPath -Force
    Write-Host "Removed byte-identical legacy review: $($mapping.Old)"
}

$legacyDir = Join-Path $repoRoot "docs\reviews"
$legacyIndex = Join-Path $legacyDir "README.md"

if (Test-Path -LiteralPath $legacyIndex) {
    $remainingMapped = @(
        $mappings |
            ForEach-Object { Join-Path $repoRoot $_.Old } |
            Where-Object { Test-Path -LiteralPath $_ }
    )
    if ($remainingMapped.Count -gt 0) {
        throw "Mapped legacy reviews remain; refusing to remove the legacy review index."
    }

    # The legacy index has no 1:1 destination. Validate that it references no
    # unknown review Markdown files before deleting this obsolete index.
    $text = Get-Content -LiteralPath $legacyIndex -Raw
    $linked = [regex]::Matches($text, '(?i)([A-Za-z0-9._-]+\.md)') |
        ForEach-Object { $_.Groups[1].Value } |
        Where-Object { $_ -ne "README.md" } |
        Sort-Object -Unique
    $known = $mappings | ForEach-Object { Split-Path $_.Old -Leaf } | Sort-Object -Unique
    $unknown = @($linked | Where-Object { $_ -notin $known })

    if ($unknown.Count -gt 0) {
        throw "Legacy index references unknown Markdown files; refusing deletion: $($unknown -join ', ')"
    }

    Remove-Item -LiteralPath $legacyIndex -Force
    Write-Host "Removed validated obsolete review index: docs\reviews\README.md"
}

if (Test-Path -LiteralPath $legacyDir) {
    $remaining = @(Get-ChildItem -LiteralPath $legacyDir -Force)
    if ($remaining.Count -eq 0) {
        Remove-Item -LiteralPath $legacyDir -Force
        Write-Host "Removed empty legacy directory: docs\reviews"
    } else {
        Write-Warning "Unexpected files remain under docs\reviews; directory preserved."
    }
}

Write-Host "Phase-local review migration cleanup complete." -ForegroundColor Green
