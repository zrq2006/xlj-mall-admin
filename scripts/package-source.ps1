# Package the source tree into a ZIP, excluding build outputs, dependencies and local state.
# Usage: powershell -ExecutionPolicy Bypass -File scripts\package-source.ps1 [-Output <path>] [-IncludeDist]
param(
  [string]$Output = '',
  [switch]$IncludeDist
)

$ErrorActionPreference = 'Stop'
$root = Split-Path -Parent $PSScriptRoot
$folderName = Split-Path $root -Leaf
if (-not $Output) {
  $Output = Join-Path (Split-Path $root -Parent) ($folderName + '-source.zip')
}
if (Test-Path $Output) { Remove-Item $Output -Force }

# Relative-path patterns that never belong to the source archive.
$excludedDirs = @(
  'frontend/node_modules',
  'frontend/dist',
  'tools/node_modules',
  'backend/target',
  '.tools',
  'logs',
  '.idea',
  'docs_src/_libs',
  'docs_src/_build',
  'docs_src/_preview',
  'docs_src/__pycache__'
)
if ($IncludeDist) {
  # Drop the dist exclusion: the built frontend is included so the archive can be unpacked and served directly.
  $excludedDirs = $excludedDirs | Where-Object { $_ -ne 'frontend/dist' }
}
$excludedFiles = @('*.log', '*.pid', 'Thumbs.db', '.DS_Store')

function Test-Excluded([string]$relativePath) {
  $normalized = $relativePath -replace '\\', '/'
  foreach ($dir in $excludedDirs) {
    if ($normalized -eq $dir -or $normalized.StartsWith($dir + '/')) { return $true }
  }
  foreach ($pattern in $excludedFiles) {
    if ($normalized -like $pattern -or $normalized -like ('*/' + $pattern)) { return $true }
  }
  return $false
}

Add-Type -AssemblyName System.IO.Compression.FileSystem
$zip = [System.IO.Compression.ZipFile]::Open($Output, 'Create')
$count = 0
$bytes = 0
try {
  $files = Get-ChildItem -Path $root -Recurse -File -Force -ErrorAction SilentlyContinue
  foreach ($file in $files) {
    $relative = $file.FullName.Substring($root.Length + 1)
    if (Test-Excluded $relative) { continue }
    $entryName = $folderName + '/' + ($relative -replace '\\', '/')
    [void][System.IO.Compression.ZipFileExtensions]::CreateEntryFromFile(
      $zip, $file.FullName, $entryName, [System.IO.Compression.CompressionLevel]::Optimal)
    $count++
    $bytes += $file.Length
  }
} finally {
  $zip.Dispose()
}

$zipSize = (Get-Item $Output).Length
Write-Host ("Included {0} files, {1:N1} MB before compression" -f $count, ($bytes / 1MB))
Write-Host ("Archive: {0} ({1:N1} MB)" -f $Output, ($zipSize / 1MB)) -ForegroundColor Green
