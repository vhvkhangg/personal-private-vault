$ErrorActionPreference = "Stop"

$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
$obsolete = @(
    "INITIAL_BASELINE.md",
    "PHASE0_BUILD_FIX_NOTES.md",
    "PHASE0_REVISION_NOTES.md",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\account\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\authentication\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\collection\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\collection\\music\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\collection\\shopping\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\collection\\software\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\feed\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\fiction\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\film\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\finance\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\importdata\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\journal\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\knowledge\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\knowledge\\information\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\knowledge\\note\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\knowledge\\study\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\knowledge\\vocabulary\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\location\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\media\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\people\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\personal\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\reference\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\reference\\CountryView.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\reference\\CurrencyView.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\reference\\LanguageView.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\reference\\PlatformKind.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\reference\\PlatformView.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\reference\\ReferenceCatalog.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\reference\\StoryArchetypeView.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\reference\\WorldSettingView.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\reference\\internal\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\reference\\internal\\application\\ReferenceCatalogService.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\search\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\settings\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\RatingGrade.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\TagView.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\VaultEntryOperations.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\VaultEntryType.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\VaultEntryView.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\VaultMetadataOperations.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\VaultMetadataView.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\internal\\.gitkeep",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\internal\\application\\TagCreator.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\internal\\application\\VaultEntryService.java",
    "backend\\src\\main\\java\\com\\vhvkhangg\\personalprivatevault\\vault\\internal\\application\\VaultMetadataService.java",
    "docs\\implementation\\backend-phase-0-governance-test-evidence.md",
    "docs\\implementation\\backend-phase-0.md",
    "docs\\implementation\\backend-phase-1-flyway-manifest.md",
    "docs\\implementation\\backend-phase-1-owner-files.md",
    "docs\\implementation\\backend-phase-1-test-evidence.md",
    "docs\\implementation\\backend-phase-1.md",
    "docs\\implementation\\handoffs\\backend-phase-1-reference-vault-foundation.md"
)

foreach ($relative in $obsolete) {
    $path = Join-Path $repoRoot $relative
    if (Test-Path $path) {
        Remove-Item $path -Force
        Write-Host "Removed legacy path: $relative"
    }
}

Write-Host "Legacy package/document paths cleaned." -ForegroundColor Green
