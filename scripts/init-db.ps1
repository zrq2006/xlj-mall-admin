# Initialise the MySQL database for the Xlj Mall admin: database, project user, schema and demo data.
# Usage: powershell -ExecutionPolicy Bypass -File scripts\init-db.ps1 [-RootPassword 123456]
param(
  [string]$RootPassword = '123456',
  [string]$Server = '127.0.0.1',
  [int]$Port = 3306,
  [string]$MysqlExe = ''
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$dbDir = Join-Path $root 'db'

if (-not $MysqlExe) {
  $candidates = @(
    'D:\program_files\mysql\mysql-commercial-9.7.1-winx64\mysql-commercial-9.7.1-winx64\bin\mysql.exe',
    'C:\Program Files\MySQL\MySQL Server 8.4\bin\mysql.exe'
  )
  foreach ($c in $candidates) {
    if (Test-Path $c) { $MysqlExe = $c; break }
  }
}
if (-not $MysqlExe) {
  $cmd = Get-Command mysql -ErrorAction SilentlyContinue
  if ($cmd) { $MysqlExe = $cmd.Source }
}
if (-not $MysqlExe -or -not (Test-Path $MysqlExe)) {
  throw 'mysql client not found. Pass -MysqlExe <path to mysql.exe>.'
}

function Invoke-SqlFile {
  param([string]$User, [string]$Password, [string]$Database, [string]$File)
  $env:MYSQL_PWD = $Password
  try {
    $target = ''
    if ($Database) { $target = ' ' + $Database }
    $line = '"' + $MysqlExe + '" -h ' + $Server + ' -P ' + $Port + ' -u ' + $User + ' --default-character-set=utf8mb4' + $target + ' < "' + $File + '"'
    cmd /c $line
    if ($LASTEXITCODE -ne 0) { throw "Failed applying $File" }
  } finally {
    Remove-Item Env:\MYSQL_PWD -ErrorAction SilentlyContinue
  }
}

Write-Host "[1/3] bootstrap (database, project user, grants)..." -ForegroundColor Cyan
Invoke-SqlFile -User 'root' -Password $RootPassword -Database '' -File (Join-Path $dbDir '00-bootstrap.sql')

Write-Host "[2/3] schema..." -ForegroundColor Cyan
Invoke-SqlFile -User 'xlj_mall' -Password 'XljMall_2026' -Database 'xlj_mall' -File (Join-Path $dbDir '01-schema.sql')

Write-Host "[3/3] demo data..." -ForegroundColor Cyan
Invoke-SqlFile -User 'xlj_mall' -Password 'XljMall_2026' -Database 'xlj_mall' -File (Join-Path $dbDir '02-seed.sql')

Write-Host 'Database ready: xlj_mall / user xlj_mall (password XljMall_2026)' -ForegroundColor Green
Write-Host 'Admin account is created by the backend on first start: admin / admin123'
