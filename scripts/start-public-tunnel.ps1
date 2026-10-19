# Start a Cloudflare quick tunnel for the local service and print the public URL.
# The URL stays the same while this process keeps running; restarting it yields a new URL.
# Usage: powershell -ExecutionPolicy Bypass -File scripts\start-public-tunnel.ps1
param(
  [int]$Port = 8080
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$exe = Join-Path $root '.tools\bin\cloudflared.exe'
$logDir = Join-Path $root 'logs'
$log = Join-Path $logDir 'cloudflared.log'
$urlFile = Join-Path $logDir 'public-url.txt'

if (-not (Test-Path $exe)) {
  throw 'cloudflared.exe not found under .tools\bin. Run the download step from the README first.'
}
New-Item -ItemType Directory -Force -Path $logDir | Out-Null
if (Test-Path $log) { Remove-Item $log -Force }

$running = Get-Process cloudflared -ErrorAction SilentlyContinue
if ($running) {
  $running | Stop-Process -Force
  Start-Sleep -Seconds 1
}

Start-Process -FilePath $exe -WorkingDirectory $root -WindowStyle Hidden `
  -ArgumentList @('tunnel', '--url', "http://localhost:$Port", '--no-autoupdate', '--logfile', $log, '--loglevel', 'info')

$publicUrl = $null
for ($i = 0; $i -lt 40; $i++) {
  Start-Sleep -Seconds 1
  if (Test-Path $log) {
    $match = Select-String -Path $log -Pattern 'https://[a-z0-9-]+\.trycloudflare\.com' -AllMatches |
      Select-Object -First 1
    if ($match) {
      $publicUrl = ($match.Matches | Select-Object -First 1).Value
      break
    }
  }
}

if (-not $publicUrl) {
  Write-Host 'Tunnel did not report a URL. Check logs\cloudflared.log' -ForegroundColor Red
  Get-Content $log -Tail 20 -ErrorAction SilentlyContinue
  exit 1
}

$publicUrl | Set-Content $urlFile
Write-Host ("Public URL: " + $publicUrl) -ForegroundColor Green
Write-Host 'Keep this machine and the Java service running while others access the link.'
