# Expose the local service (http://localhost:8080) with a fixed ngrok URL.
# Prerequisites: a free ngrok account, its authtoken, and a claimed static domain
# (https://dashboard.ngrok.com/domains) such as xlj-mall.ngrok-free.app.
# Usage:
#   powershell -ExecutionPolicy Bypass -File scripts\start-tunnel.ps1 -Authtoken <token> -Domain xlj-mall.ngrok-free.app
param(
  [Parameter(Mandatory = $true)][string]$Authtoken,
  [Parameter(Mandatory = $true)][string]$Domain,
  [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$ngrok = Join-Path $root '.tools\bin\ngrok.exe'
$logDir = Join-Path $root 'logs'
if (-not (Test-Path $ngrok)) {
  throw 'ngrok.exe not found under .tools\bin. Download it from https://bin.equinox.io/c/bNyj1mQVY4c/ngrok-v3-stable-windows-amd64.zip'
}
New-Item -ItemType Directory -Force -Path $logDir | Out-Null

& $ngrok config add-authtoken $Authtoken | Out-Null
Write-Host 'Authtoken saved.' -ForegroundColor Cyan

Get-Process ngrok -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 1

$arguments = @('http', "$Port", '--url', "https://$Domain",
  '--log', (Join-Path $logDir 'ngrok.log'), '--log-format', 'logfmt')
Start-Process -FilePath $ngrok -ArgumentList $arguments -WorkingDirectory $root -WindowStyle Hidden

$publicUrl = $null
for ($i = 0; $i -lt 30; $i++) {
  Start-Sleep -Seconds 1
  try {
    $tunnels = Invoke-RestMethod -Uri 'http://127.0.0.1:4040/api/tunnels' -TimeoutSec 2
    $publicUrl = ($tunnels.tunnels | Where-Object { $_.public_url -like 'https://*' } | Select-Object -First 1).public_url
    if ($publicUrl) { break }
  } catch {
    # wait for the local API
  }
}

if (-not $publicUrl) {
  Write-Host 'Tunnel did not start. Check logs\ngrok.log:' -ForegroundColor Red
  Get-Content (Join-Path $logDir 'ngrok.log') -Tail 20 -ErrorAction SilentlyContinue
  exit 1
}

Write-Host ("Public URL: " + $publicUrl) -ForegroundColor Green
Write-Host 'Keep this machine and the Java service running; the URL stays the same after restarts.'
Write-Host 'Stop with: Get-Process ngrok | Stop-Process -Force'
