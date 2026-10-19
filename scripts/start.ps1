# Start the packaged Spring Boot application (serves both REST API and the admin UI).
# The JVM runs through javaw.exe (GUI subsystem, no console) so that closing this shell or
# sending Ctrl+C to it cannot terminate the service.
param(
  [int]$Port = 8080,
  [string]$JavaExe = 'java'
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$jar = Join-Path $root 'backend\target\xlj-mall-admin.jar'
$logDir = Join-Path $root 'logs'

if (-not (Test-Path $jar)) {
  throw 'Jar not found. Run scripts\build.ps1 first.'
}

$existing = Get-NetTCPConnection -State Listen -LocalPort $Port -ErrorAction SilentlyContinue
if ($existing) {
  $owner = (Get-Process -Id $existing[0].OwningProcess -ErrorAction SilentlyContinue).ProcessName
  throw "Port $Port is already in use by $owner."
}

function Resolve-Javaw([string]$exe) {
  $candidates = @()
  $resolved = Get-Command $exe -ErrorAction SilentlyContinue
  if ($resolved -and $resolved.Source) {
    $candidates += (Join-Path (Split-Path $resolved.Source -Parent) 'javaw.exe')
  }
  if ($env:JAVA_HOME) {
    $candidates += (Join-Path $env:JAVA_HOME 'bin\javaw.exe')
  }
  $candidates += 'D:\program_files\bin\javaw.exe'
  $javawCmd = Get-Command javaw.exe -ErrorAction SilentlyContinue
  if ($javawCmd -and $javawCmd.Source) {
    $candidates += $javawCmd.Source
  }
  foreach ($candidate in $candidates) {
    if ($candidate -and (Test-Path $candidate)) { return $candidate }
  }
  return $null
}

New-Item -ItemType Directory -Force -Path $logDir | Out-Null
$outLog = Join-Path $logDir 'app.log'
$errLog = Join-Path $logDir 'app.err.log'

$javaw = Resolve-Javaw $JavaExe

if ($javaw) {
  # javaw has no console, and the process is created by WMI (parent: WmiPrvSE), so it is
  # fully detached from this shell: closing the window or Ctrl+C cannot kill it.
  $commandLine = '"' + $javaw + '" -jar "' + $jar + '" --logging.file.name="' + $outLog + '"'
  $created = Invoke-CimMethod -ClassName Win32_Process -MethodName Create -Arguments @{ CommandLine = $commandLine }
  if ($created.ReturnValue -ne 0) {
    throw ("Launch failed, Win32_Process.Create returned " + $created.ReturnValue)
  }
} else {
  # Fallback: no javaw on this machine, use java with redirected output.
  $process = Start-Process -FilePath $JavaExe -ArgumentList @('-jar', $jar) -WorkingDirectory $root `
    -WindowStyle Hidden -RedirectStandardOutput $outLog -RedirectStandardError $errLog -PassThru
  $process.Id | Set-Content (Join-Path $logDir 'app.pid')
}

$exeName = if ($javaw) { Split-Path $javaw -Leaf } else { Split-Path $JavaExe -Leaf }
Write-Host ("Starting Xlj Mall admin with " + $exeName + " ...") -ForegroundColor Cyan
$ready = $false
for ($i = 0; $i -lt 45; $i++) {
  Start-Sleep -Seconds 1
  try {
    $response = Invoke-WebRequest -Uri ("http://127.0.0.1:" + $Port + "/login") -UseBasicParsing -TimeoutSec 2
    if ($response.StatusCode -ge 200) { $ready = $true; break }
  } catch {
    # keep waiting
  }
}

if (-not $ready) {
  Write-Host 'Service did not become ready. Last log lines:' -ForegroundColor Yellow
  Get-Content $outLog -Tail 20 -ErrorAction SilentlyContinue
  Get-Content $errLog -Tail 20 -ErrorAction SilentlyContinue
  exit 1
}

$connection = Get-NetTCPConnection -State Listen -LocalPort $Port | Select-Object -First 1
Write-Host ("Admin UI:  http://localhost:" + $Port) -ForegroundColor Green
Write-Host 'Login:     admin / admin123'
Write-Host ("API docs:  http://localhost:" + $Port + "/doc.html")
Write-Host ("PID:       " + $connection.OwningProcess + "  (logs: logs\app.log)")
Write-Host 'Stop with: powershell -ExecutionPolicy Bypass -File scripts\stop.ps1'
