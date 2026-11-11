param([Parameter(Mandatory=$true)][string]$ArtifactDir)
$ErrorActionPreference = "Stop"

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$repoRoot = (Resolve-Path (Join-Path $scriptDir "..\..")).Path
Set-Location $repoRoot

$logsDir = [System.IO.Path]::GetFullPath((Join-Path $repoRoot $ArtifactDir))
$allowedRoot = [System.IO.Path]::GetFullPath((Join-Path $repoRoot "logs\gdi26-s3-h1-f")) + [System.IO.Path]::DirectorySeparatorChar
if (-not $logsDir.StartsWith($allowedRoot, [System.StringComparison]::OrdinalIgnoreCase)) { throw "Invalid fixture output path" }
if (Test-Path -LiteralPath $logsDir) { throw "Fixture output already exists" }
New-Item -ItemType Directory -Path $logsDir | Out-Null

$outDir = Join-Path $logsDir "fixture-classes"
if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

$cpFile = Join-Path $logsDir "fixture-classpath.txt"

Write-Output "[compile-fixture] Building dependency classpath..."
$mvnCmd = (Get-Command "mvn.cmd" -ErrorAction SilentlyContinue).Source
if (-not $mvnCmd) {
    $mvnCmd = "mvn.cmd"
}

& $mvnCmd -f (Join-Path $repoRoot "backend\pom.xml") -q org.apache.maven.plugins:maven-dependency-plugin:3.9.0:build-classpath "-Dmdep.outputFile=$cpFile" "-DincludeScope=test"
if ($LASTEXITCODE -ne 0) {
    throw "Failed to build Maven test classpath"
}

$depsClasspath = (Get-Content $cpFile -Raw).Trim()
$backendClasses = (Join-Path $repoRoot "backend\target\classes")
$backendTestClasses = (Join-Path $repoRoot "backend\target\test-classes")
$fullClasspath = "$depsClasspath;$backendClasses;$backendTestClasses"

$src1 = Join-Path $repoRoot "frontend\tests\fixtures\spring\H1FFixtureConfiguration.java"
$src2 = Join-Path $repoRoot "frontend\tests\fixtures\spring\H1FLocalFixture.java"

Write-Output "[compile-fixture] Compiling fixture classes with javac..."
$javacCmd = (Get-Command "javac.exe" -ErrorAction SilentlyContinue).Source
if (-not $javacCmd) {
    $javacCmd = "javac.exe"
}

& $javacCmd -cp $fullClasspath -d $outDir $src1 $src2
if ($LASTEXITCODE -ne 0) {
    throw "Javac compilation of fixture classes failed"
}

Write-Output "[compile-fixture] Fixture compiled successfully to $outDir"
$sourceHashes = @{}
foreach ($source in @($src1, $src2)) {
    $hash = [System.Security.Cryptography.SHA256]::Create()
    try { $digest = [BitConverter]::ToString($hash.ComputeHash([System.IO.File]::ReadAllBytes($source))).Replace("-", "").ToLowerInvariant() }
    finally { $hash.Dispose() }
    $sourceHashes[[System.IO.Path]::GetFileName($source)] = $digest
}
$sourceHashes | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $logsDir "source-hashes.json") -Encoding UTF8
