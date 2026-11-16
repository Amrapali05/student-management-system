@echo off
title EduCore - Enterprise Student Management System
cd /d "%~dp0"

echo =======================================================
echo   EduCore Student Management System (Java 21 LTS)
echo   Modular Enterprise Architecture
echo =======================================================

if not exist "bin" mkdir bin
if not exist "data" mkdir data

REM Compile Java source files if not already compiled or if modified
dir /s /b src\main\java\*.java > sources.txt
javac -encoding UTF-8 -d bin @sources.txt
del sources.txt

if %ERRORLEVEL% NEQ 0 (
    echo [ERROR] Compilation failed. Please ensure JDK 21+ is in your PATH.
    pause
    exit /b %ERRORLEVEL%
)

if exist "src\main\resources" (
    xcopy /s /y "src\main\resources\*" "bin\" >nul
)

echo [OK] Compilation successful. Starting GUI application...
start javaw -cp bin com.sms.Main
exit /b 0
