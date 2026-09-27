$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$settingsPath = Join-Path $HOME ".gemini\antigravity-cli\settings.json"
$settingsDir = Split-Path $settingsPath -Parent

if (-not (Test-Path $settingsDir)) {
    New-Item -ItemType Directory -Path $settingsDir -Force | Out-Null
}

if (Test-Path $settingsPath) {
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $backupPath = "$settingsPath.$timestamp.bak"
    Copy-Item $settingsPath $backupPath -Force
    Write-Host "Backup: $backupPath"
    $settings = Get-Content $settingsPath -Raw | ConvertFrom-Json
} else {
    $settings = [pscustomobject]@{}
}

function Ensure-Property {
    param($Object, [string]$Name, $Value)
    if ($null -eq $Object.PSObject.Properties[$Name]) {
        $Object | Add-Member -NotePropertyName $Name -NotePropertyValue $Value
    } else {
        $Object.$Name = $Value
    }
}

function Ensure-ArrayProperty {
    param($Object, [string]$Name)
    if ($null -eq $Object.PSObject.Properties[$Name]) {
        $Object | Add-Member -NotePropertyName $Name -NotePropertyValue @()
    } elseif ($null -eq $Object.$Name) {
        $Object.$Name = @()
    }
}

function Add-Unique {
    param($Current, [string[]]$Values)
    $result = @($Current)
    foreach ($value in $Values) {
        if ($result -notcontains $value) {
            $result += $value
        }
    }
    return ,$result
}

function Remove-Exact {
    param($Current, [string[]]$Values)
    return ,@($Current | Where-Object { $Values -notcontains $_ })
}

Ensure-Property $settings "enableTerminalSandbox" $true
Ensure-Property $settings "toolPermission" "proceed-in-sandbox"

if ($null -eq $settings.PSObject.Properties["permissions"]) {
    $settings | Add-Member -NotePropertyName "permissions" -NotePropertyValue ([pscustomobject]@{})
}
Ensure-ArrayProperty $settings.permissions "allow"
Ensure-ArrayProperty $settings.permissions "deny"

$repoPermissionPath = $repoRoot -replace "\\", "/"

# Maven should remain sandboxed. Broad host execution makes Windows approval
# behavior worse and is not required for normal build/test/verify.
$settings.permissions.allow = Remove-Exact $settings.permissions.allow @(
    "unsandboxed(mvn)",
    "command(docker compose)",
    "unsandboxed(docker compose)"
)

$allowRules = @(
    "read_file($repoPermissionPath)",
    "write_file($repoPermissionPath)",
    "command(git status)",
    "command(git diff)",
    "command(git ls-files)",
    "command(git log)",
    "command(git show)",
    "command(git rev-parse)",
    "command(git branch)",
    "command(mvn)",
    "command(java -version)",
    "command(docker info)",
    "command(docker version)",
    "command(docker ps)",
    "command(docker compose ps)",
    "command(docker compose logs)",
    "command(docker compose images)",
    "command(docker compose top)",
    "unsandboxed(docker info)",
    "unsandboxed(docker version)",
    "unsandboxed(docker ps)",
    "unsandboxed(docker compose ps)",
    "unsandboxed(docker compose logs)",
    "unsandboxed(docker compose images)",
    "unsandboxed(docker compose top)",
    "command(Get-ChildItem)",
    "command(Get-Item)",
    "command(Get-Location)",
    "command(Get-Command)",
    "command(Get-Content)",
    "command(Select-Object)",
    "command(Select-String)",
    "command(Where-Object)",
    "command(Sort-Object)",
    "command(Measure-Object)"
)

$denyRules = @(
    "command(git commit)",
    "command(git push)",
    "command(git tag)",
    "command(gh pr create)",
    "command(gh pr merge)",
    "unsandboxed(git commit)",
    "unsandboxed(git push)",
    "unsandboxed(git tag)",
    "unsandboxed(gh pr create)",
    "unsandboxed(gh pr merge)"
)

$settings.permissions.allow = Add-Unique $settings.permissions.allow $allowRules
$settings.permissions.deny = Add-Unique $settings.permissions.deny $denyRules

Ensure-ArrayProperty $settings "trustedWorkspaces"
if (@($settings.trustedWorkspaces) -notcontains $repoRoot) {
    $settings.trustedWorkspaces = @($settings.trustedWorkspaces) + $repoRoot
}

$settings | ConvertTo-Json -Depth 100 | Set-Content $settingsPath -Encoding UTF8

Write-Host ""
Write-Host "Updated: $settingsPath" -ForegroundColor Green
Write-Host "Sandbox enabled; auto execution = proceed-in-sandbox."
Write-Host "Broad unsandboxed(mvn) removed; Maven remains sandboxed."
Write-Host "Fully restart Antigravity CLI before testing permissions."
