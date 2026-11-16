# PowerShell runner for EduCore Enterprise Student Management System
$ErrorActionPreference = "Stop"

$PSScriptRoot = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition
Set-Location $PSScriptRoot

Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "  EduCore Student Management System (Java 21 LTS)" -ForegroundColor Cyan
Write-Host "  Enterprise Architecture • Collections • Concurrency" -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

if (-not (Test-Path "bin")) { New-Item -ItemType Directory -Path "bin" | Out-Null }
if (-not (Test-Path "data")) { New-Item -ItemType Directory -Path "data" | Out-Null }

Write-Host "Compiling Java source files..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Path "src\main\java" -Filter *.java -Recurse | ForEach-Object { $_.FullName }
javac -encoding UTF-8 -d bin $javaFiles

if ($LASTEXITCODE -eq 0) {
    if (Test-Path "src\main\resources") {
        Copy-Item -Path "src\main\resources\*" -Destination "bin" -Recurse -Force
    }
    Write-Host "[OK] Compilation successful." -ForegroundColor Green
    Write-Host "Launching application GUI..." -ForegroundColor Green
    Start-Process -FilePath "javaw" -ArgumentList "-cp", "bin", "com.sms.Main"
} else {
    Write-Host "[ERROR] Compilation failed with exit code $LASTEXITCODE." -ForegroundColor Red
}
