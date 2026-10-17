$ErrorActionPreference = 'Stop'
$ragRepoRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot '../../..')).Path
Push-Location -LiteralPath $ragRepoRoot
try {
    $ragClasspathFile = Join-Path $ragRepoRoot 'backend/target/rag-dev-classpath.txt'
    if (-not (Test-Path -LiteralPath $ragClasspathFile)) {
        throw 'Build backend runtime classpath first; see backend/dev/rag-db/README.md.'
    }
    $ragClasspath = (Get-Content -LiteralPath $ragClasspathFile -Raw).Trim() + ';backend/target/classes'
    & java --class-path $ragClasspath backend/dev/rag-db/ProvisionRagDevelopment.java
    if ($LASTEXITCODE -ne 0) { throw 'Local RAG provisioning failed.' }
} finally { Pop-Location }
