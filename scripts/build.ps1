# Build the frontend bundle and package the Spring Boot jar (frontend dist is embedded into the jar).
$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot

function Resolve-Maven {
  $cmd = Get-Command mvn.cmd -ErrorAction SilentlyContinue
  if ($cmd) { return $cmd.Source }
  $local = Join-Path $root '.tools\apache-maven-3.9.9\bin\mvn.cmd'
  if (Test-Path $local) { return $local }
  throw 'Maven not found. Install Maven or place it under .tools\apache-maven-3.9.9.'
}

$mvn = Resolve-Maven

Write-Host '[1/2] building frontend...' -ForegroundColor Cyan
Push-Location (Join-Path $root 'frontend')
try {
  if (-not (Test-Path 'node_modules')) {
    cmd /c 'npm install --no-audit --no-fund'
    if ($LASTEXITCODE -ne 0) { throw 'npm install failed' }
  }
  cmd /c 'npm run build'
  if ($LASTEXITCODE -ne 0) { throw 'vite build failed' }
} finally {
  Pop-Location
}

Write-Host '[2/2] packaging backend...' -ForegroundColor Cyan
Push-Location (Join-Path $root 'backend')
try {
  cmd /c "`"$mvn`" -B -DskipTests package"
  if ($LASTEXITCODE -ne 0) { throw 'maven package failed' }
} finally {
  Pop-Location
}

$jar = Join-Path $root 'backend\target\xlj-mall-admin.jar'
Write-Host ('Build done: ' + $jar) -ForegroundColor Green
Write-Host 'Start it with: powershell -ExecutionPolicy Bypass -File scripts\start.ps1'
