# PowerShell runner for EduCore Student Management System
$ErrorActionPreference = "Stop"

$PSScriptRoot = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition
Set-Location $PSScriptRoot

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "  EduCore Student Management System (Java 21)" -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
if (-not (Test-Path "data")) { New-Item -ItemType Directory -Path "data" | Out-Null }

Write-Host "Compiling Java source files..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d bin $javaFiles

if ($LASTEXITCODE -eq 0) {
    Write-Host "[OK] Compilation successful." -ForegroundColor Green
    Write-Host "Launching application GUI..." -ForegroundColor Green
    Start-Process -FilePath "javaw" -ArgumentList "-cp", "bin", "com.sms.Main"
} else {
    Write-Host "[ERROR] Compilation failed with exit code $LASTEXITCODE." -ForegroundColor Red
}
