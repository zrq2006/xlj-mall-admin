# Stop the application started by scripts\start.ps1.
# It stops the process that currently listens on the HTTP port (plus its children),
# which also covers the Oracle "javapath" launcher that proxies to the real JVM.
param(
  [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$pidFile = Join-Path $root 'logs\app.pid'

$stopped = $false

$connections = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
foreach ($connection in $connections) {
  $ownerPid = $connection.OwningProcess
  $process = Get-Process -Id $ownerPid -ErrorAction SilentlyContinue
  if ($process -and $process.ProcessName -eq 'java') {
    cmd /c "taskkill /PID $ownerPid /T /F" | Out-Null
    Write-Host ("Stopped process " + $ownerPid + " listening on port " + $Port) -ForegroundColor Green
    $stopped = $true
  }
}

if (-not $stopped -and (Test-Path $pidFile)) {
  $appPid = (Get-Content $pidFile | Select-Object -First 1).Trim()
  if (Get-Process -Id $appPid -ErrorAction SilentlyContinue) {
    cmd /c "taskkill /PID $appPid /T /F" | Out-Null
    Write-Host ("Stopped process " + $appPid + " from logs\app.pid") -ForegroundColor Green
    $stopped = $true
  }
}

if (-not $stopped) {
  Write-Host ("No java process is listening on port " + $Port + ".") -ForegroundColor Yellow
}

Remove-Item $pidFile -Force -ErrorAction SilentlyContinue
